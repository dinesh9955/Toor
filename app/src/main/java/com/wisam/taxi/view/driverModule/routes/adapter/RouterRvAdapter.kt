package com.wisam.taxi.view.driverModule.routes.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wisam.taxi.R
import com.wisam.taxi.model.driverResponse.getDriverRoutes.Data
import com.wisam.taxi.model.driverResponse.routeslist.RoutesData
import kotlinx.android.synthetic.main.routes_layout_design.view.*

class RouterRvAdapter(val ctx: Context, val routesList: ArrayList<RoutesData>) :
    RecyclerView.Adapter<RouterRvAdapter.RoutesViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutesViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.routes_layout_design, parent, false)
        return RoutesViewHolder(v)
    }

    override fun getItemCount(): Int {

        return routesList.size
    }

    override fun onBindViewHolder(holder: RoutesViewHolder, position: Int) {
        holder.bindItems(routesList)

        holder.itemView.tvRoutesRvDesign.text = routesList[position].name
    }

    class RoutesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(explore: ArrayList<RoutesData>) {

        }
    }

}