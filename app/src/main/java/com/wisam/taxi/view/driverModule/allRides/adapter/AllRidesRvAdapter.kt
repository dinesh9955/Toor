package com.wisam.taxi.view.driverModule.allRides.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wisam.taxi.R
import com.wisam.taxi.view.driverModule.allRides.models.AllRidesDataModel
import com.wisam.taxi.view.home.interfaces.RvClickPostion
import kotlinx.android.synthetic.main.allrides_rv_design.view.*

class AllRidesRvAdapter (val ctx: Context, val allRidesList: ArrayList<AllRidesDataModel>, val  onClickListener : RvClickPostion) :
    RecyclerView.Adapter<AllRidesRvAdapter.AllRidesViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllRidesViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.allrides_rv_design, parent, false)
        return AllRidesViewHolder(v)
    }

    override fun getItemCount(): Int {

        return allRidesList.size
    }

    override fun onBindViewHolder(holder: AllRidesViewHolder, position: Int) {
        holder.bindItems(allRidesList)

        if (allRidesList[position].status.equals("complete",true))
        {
            holder.itemView.tvAllRidesStatus.visibility = View.INVISIBLE
            holder.itemView.tvAllRidesStatwithGreen.visibility = View.INVISIBLE
            holder.itemView.tvAllRidesStatwithBg.visibility = View.VISIBLE
        }
        else if (allRidesList[position].status.equals("start",true))
        {
            holder.itemView.tvAllRidesStatus.visibility = View.INVISIBLE
            holder.itemView.tvAllRidesStatwithGreen.visibility = View.VISIBLE
            holder.itemView.tvAllRidesStatwithBg.visibility = View.INVISIBLE
        }
        else
        {
            holder.itemView.tvAllRidesStatus.visibility = View.VISIBLE
            holder.itemView.tvAllRidesStatwithGreen.visibility = View.INVISIBLE
            holder.itemView.tvAllRidesStatwithBg.visibility = View.INVISIBLE
        }

        holder.itemView.tvallRidesSource.text = allRidesList[position].sourceAddress
        holder.itemView.tvAllRidesDestination.text = allRidesList[position].destinationAddress
        holder.itemView.tvAllRidesDistance.text = allRidesList[position].distance
        holder.itemView.tvAllRidesStatus.text = allRidesList[position].status
        holder.itemView.tvAllRidesStatwithGreen.text = allRidesList[position].status
        holder.itemView.tvAllRidesStatwithBg.text = allRidesList[position].status
        holder.itemView.tvAllRidesTime.text = allRidesList[position].time

        holder.itemView.tvAllRidesStatus.setOnClickListener {
            onClickListener.onItemClicked(position)
        }
        holder.itemView.tvAllRidesStatwithGreen.setOnClickListener {
            onClickListener.onItemClicked(position)
        }
        holder.itemView.tvAllRidesStatwithBg.setOnClickListener {
            onClickListener.onItemClicked(position)
        }

    }

    class AllRidesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(explore: ArrayList<AllRidesDataModel>) {

        }
    }

}