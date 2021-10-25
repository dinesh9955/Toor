package com.wisam.taxi.view.home.adapter

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wisam.taxi.R
import com.wisam.taxi.model.driverResponse.getRoutes.Route
import com.wisam.taxi.view.home.interfaces.RvClickPostion
import kotlinx.android.synthetic.main.explore_rv_design.view.*
import org.apache.commons.lang3.time.DurationFormatUtils
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class DriverExploreRvAdapter(val ctx: Context, val exploreList: ArrayList<Route>, val  onClickListener : RvClickPostion) :
    RecyclerView.Adapter<DriverExploreRvAdapter.ExploreViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.explore_rv_design, parent, false)
        return ExploreViewHolder(v)
    }

    override fun getItemCount(): Int {

        return exploreList.size
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        holder.bindItems(exploreList)
        var timeDifference:String=""
        var tiemInMin : Int=0

        holder.itemView.tvRouteName.text = exploreList[position].name

        try {
           tiemInMin = getDistance(
                exploreList[position].startPoint[0].coordinates[0],  exploreList[position].startPoint[0].coordinates[1],
                exploreList[position].endPoint[0].coordinates[0],exploreList[position].endPoint[0].coordinates[1]
            )
        }
        catch (e:Exception){
            e.printStackTrace()
        }

        if (exploreList[position].startTime.equals("Active",true))
        {
            holder.itemView.ivClock.visibility = View.INVISIBLE
            holder.itemView.tvTime.visibility = View.INVISIBLE
            holder.itemView.tvTimeActive.visibility = View.VISIBLE
            holder.itemView.clCircleBg?.setBackgroundResource(R.drawable.circle_stroke_green)
        }

        holder.itemView.tvTime.text = "$tiemInMin Min"
        holder.itemView.tvTimeActive.text = ""+tiemInMin
        holder.itemView.ivPerson.visibility = View.INVISIBLE
        holder.itemView.tvPerson.visibility = View.INVISIBLE
        holder.itemView.tvIRD.text = ""+exploreList[position].totalFare
        holder.itemView.tvSource.text = ""+exploreList[position].startPoint[0].address
        holder.itemView.tvDestination.text = ""+exploreList[position].endPoint[0].address

        holder.itemView.setOnClickListener {

            holder.itemView.clExploreRvDesign?.setBackgroundResource(R.drawable.roundrect_explore_rv)
            holder.itemView.ivCircle?.setImageResource(R.drawable.circle_border)
            holder.itemView.ivExploreRvLine?.setImageResource(R.drawable.line_19_2)
            holder.itemView.ivmapDestination?.setImageResource(R.drawable.pin_2_3)

            holder.itemView.tvSource.setTextColor(Color.WHITE)
            holder.itemView.tvDestination.setTextColor(Color.WHITE)

            onClickListener.onItemClicked(position)

            var handler = Handler()
            handler.postDelayed({

                holder.itemView.clExploreRvDesign?.setBackgroundColor(Color.WHITE)
                holder.itemView.ivCircle?.setImageResource(R.drawable.ellipse_497)
                holder.itemView.ivExploreRvLine?.setImageResource(R.drawable.line_19)
                holder.itemView.ivmapDestination?.setImageResource(R.drawable.pin_2)

                holder.itemView.tvSource.setTextColor(Color.parseColor("#000000"))
                holder.itemView.tvDestination.setTextColor(Color.parseColor("#000000"))

            },60)
        }
    }

    class ExploreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(explore: ArrayList<Route>) {

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