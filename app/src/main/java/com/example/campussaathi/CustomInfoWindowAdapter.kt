package com.example.campussaathi

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Activity) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        val view = context.layoutInflater.inflate(R.layout.map_info_window, null)
        val tvName = view.findViewById<TextView>(R.id.txtServiceName)
        tvName.text = marker.title?.uppercase()
        return view
    }

    override fun getInfoContents(marker: Marker): View? = null
}