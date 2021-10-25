package com.wisam.taxi.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ProgressBar
import com.wisam.taxi.R

class CustomProgressBar(context: Context?) : Dialog(context!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_bar)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

}