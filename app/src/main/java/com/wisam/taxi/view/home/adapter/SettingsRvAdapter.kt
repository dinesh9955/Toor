package com.wisam.taxi.view.home.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wisam.taxi.R
import com.wisam.taxi.view.home.interfaces.RvClickPostion
import com.wisam.taxi.view.home.model.SettingsDataModel
import kotlinx.android.synthetic.main.settings_rv_design.view.*

class SettingsRvAdapter (val ctx: Context, val settingsDataList: ArrayList<SettingsDataModel>,var onclickPostion : RvClickPostion) :
    RecyclerView.Adapter<SettingsRvAdapter.SettingsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.settings_rv_design, parent, false)
        return SettingsViewHolder(v)
    }

    override fun getItemCount(): Int {

        return settingsDataList.size
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {

        if (position == settingsDataList.size.minus(1))
        {
            holder.itemView.tvSettingTitle.setTextColor(Color.RED)
            holder.itemView.ivSettingsArrow.visibility = View.INVISIBLE
        }

        holder.bindItems(settingsDataList.get(position),position, onclickPostion)

        holder.itemView.tvSettingTitle.text = settingsDataList[position].title

    }

    class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(explore: SettingsDataModel,position : Int ,onclickposition : RvClickPostion) {

            itemView.setOnClickListener {

                onclickposition.onItemClicked(position)
            }
        }
    }

}