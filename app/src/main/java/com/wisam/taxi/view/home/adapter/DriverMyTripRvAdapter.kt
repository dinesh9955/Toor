package com.wisam.taxi.view.home.adapter

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.squareup.picasso.Picasso
import com.wisam.taxi.R
import com.wisam.taxi.base.WisamTaxiApplication
import com.wisam.taxi.model.driverResponse.getbooking.Booking
import com.wisam.taxi.view.home.interfaces.RvClickPostion
import kotlinx.android.synthetic.main.mytrips_rvdesign.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class DriverMyTripRvAdapter(
    val ctx: Context,
    private val bookingList: ArrayList<Booking>,
    private val onClicked: RvClickPostion,
    val type: String,
    val lang: String
) :
    RecyclerView.Adapter<DriverMyTripRvAdapter.MyTripsViewHolder>() {

    init {
        setResource()
    }

    private fun setResource() {
        val locale: Locale = Locale(lang)
        val res: Resources = ctx.resources
        val dm: DisplayMetrics = res.displayMetrics
        val configuration: Configuration = res.configuration
        configuration.setLocale(locale)
        res.updateConfiguration(configuration, dm)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTripsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.mytrips_rvdesign, parent, false)
        return MyTripsViewHolder(v)
    }

    override fun getItemCount(): Int {
        return bookingList.size
    }

    override fun onBindViewHolder(holder: MyTripsViewHolder, position: Int) {
        holder.bindItems(bookingList)

        try {
            var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            val bookingstartTime = sdf.format(bookingList[position].bookingStartTime)
            val bookingendTime = sdf.format(bookingList[position].bookingEndTime)

            val startDate = sdf.parse(bookingstartTime)
            val enddate = sdf.parse(bookingendTime)

            sdf = SimpleDateFormat("EEEE dd MMM, yyyy")
            val date = sdf.format(startDate)

            sdf = SimpleDateFormat("h:mm a")
            val startTime = sdf.format(startDate)

            val tiemInMin = getDistance(
                bookingList[position].source.latitude, bookingList[position].source.longitude,
                bookingList[position].destination.latitude
                , bookingList[position].destination.longitude
            )

            holder.itemView.tvBooingConfTime.text = "$date, $startTime"
            holder.itemView.tvDriverName.text = "" + bookingList[position].userId.fullName
            holder.itemView.tvBooingConfPas.text = "" + bookingList[position].seats + " "+ctx.resources.getString(R.string.passengers)

            if (type.equals("ongoing", true)) {
                holder.itemView.tvBooingConfRating.text =
                    "" + DecimalFormat("#.#").format(bookingList[position].userOverallRating)
            } else {
                holder.itemView.tvBooingConfRating.text =
                    "" + DecimalFormat("#.#").format(bookingList[position].userRating)
            }


            holder.itemView.tvSource.text = "" + bookingList[position].source.address
            holder.itemView.tvDestination.text = "" + bookingList[position].destination.address
            holder.itemView.tvBooingConfValue.text = "" + bookingList[position].totalAmount

            when (bookingList[position].status) {
                0 -> {
                    holder.itemView.tvBooingConfTimeVa.text = "$tiemInMin "+ctx.getString(R.string.stringmin)
                    holder.itemView.tvBooingConfTimeVa.setTextColor(Color.parseColor("#000000"))
                }
                2 -> {
                    holder.itemView.tvBooingConfTimeVa.text = "$tiemInMin "+ctx.getString(R.string.stringmin)
                    holder.itemView.tvBooingConfTimeVa.setTextColor(Color.parseColor("#000000"))
                }
                6 -> {
                    holder.itemView.tvBooingConfTimeVa.text = ctx.resources.getString(R.string.completed)
                    holder.itemView.tvBooingConfTimeVa.setTextColor(Color.parseColor("#009a24"))
                }
                3 -> {
                    holder.itemView.tvBooingConfTimeVa.text = ctx.resources.getString(R.string.completed)
                    holder.itemView.tvBooingConfTimeVa.setTextColor(Color.parseColor("#009a24"))
                }
                11 -> {
                    holder.itemView.tvBooingConfTimeVa.text = ctx.resources.getString(R.string.cancel)
                    holder.itemView.tvBooingConfTimeVa.setTextColor(Color.parseColor("#ff3030"))
                }
                12 -> {
                    holder.itemView.tvBooingConfTimeVa.text = ctx.resources.getString(R.string.cancel)
                    holder.itemView.tvBooingConfTimeVa.setTextColor(Color.parseColor("#ff3030"))
                }
            }

            val profilePic = "" + bookingList[position].userId.profilePic

            if (profilePic.isEmpty())
                holder.itemView.ivBooingConfProfile.setImageResource(R.drawable.group_445)
            else
                Picasso.get()
                    .load(WisamTaxiApplication.BASE_URL + "" + profilePic).fit().centerCrop()
                    .placeholder(R.drawable.group_445)
                    .into(holder.itemView.ivBooingConfProfile)

        } catch (e: Exception) {
            Log.e("Exception", "" + e.toString())
            e.printStackTrace()
        }

        holder.itemView.setOnClickListener {
            onClicked.onItemClicked(position)
        }

    }

    class MyTripsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(explore: ArrayList<Booking>) {

        }
    }

    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        var startPoint = Location("locationA")
        startPoint.latitude = lat1
        startPoint.longitude = lon1
        var endpoint = Location("locationB")
        endpoint.latitude = lat2
        endpoint.longitude = lon2

        val distancebtw = startPoint.distanceTo(endpoint)

        return (distancebtw / 800).toInt()
    }

}