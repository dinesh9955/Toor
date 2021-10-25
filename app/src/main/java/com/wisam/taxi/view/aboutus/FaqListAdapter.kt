package com.wisam.taxi.view.aboutus

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wisam.taxi.R
import com.wisam.taxi.model.response.logout.FAQData
import kotlinx.android.synthetic.main.faq_rv_layout.view.*

class FaqListAdapter  (private val ctx: Context, private val faqList : ArrayList<FAQData>) :
    RecyclerView.Adapter<FaqListAdapter.FaqViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.faq_rv_layout, parent, false)
        return FaqViewHolder(v)
    }

    override fun getItemCount(): Int {
        return faqList.size
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {

        holder.itemView.tvFaqQues.text = faqList[position].question
        holder.itemView.tvFaqAns.text = faqList[position].answer

        if (faqList[position].isVisible == true)
        {
            holder.itemView.faqMainBg.setBackgroundColor(ContextCompat.getColor(ctx,R.color.colorWhite))
            holder.itemView.ivFaqArrow.rotation = 0f
            holder.itemView.tvFaqAns.visibility = View.GONE
        }
        else
        {
            holder.itemView.faqMainBg.setBackgroundColor(ContextCompat.getColor(ctx,R.color._1E009A24))
            holder.itemView.ivFaqArrow.rotation = 180f
            holder.itemView.tvFaqAns.visibility = View.VISIBLE
        }

        holder.itemView.tvFaqQues.setOnClickListener{

            if (faqList[position].isVisible == true)
            {
                faqList[position].isVisible = false
                notifyDataSetChanged()
            }
            else
            {
                faqList[position].isVisible = true
                notifyDataSetChanged()
            }
        }

        holder.itemView.tvFaqAns.setOnClickListener{

            if (faqList[position].isVisible == true)
            {
                faqList[position].isVisible = false
                notifyDataSetChanged()
            }
            else
            {
                faqList[position].isVisible = true
                notifyDataSetChanged()
            }
        }

    }

    class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

}