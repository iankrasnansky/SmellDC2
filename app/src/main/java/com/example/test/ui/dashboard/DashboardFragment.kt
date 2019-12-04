package com.example.test.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.test.R
import com.example.test.Report
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

//THIS IS THE MAP FRAGMENT

class DashboardFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var allReports: MutableList<Report>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        var mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        if (mapFragment == null) {
            val mFragmentManager = fragmentManager
            val mFragmentTransaction = mFragmentManager!!.beginTransaction()
            mapFragment = SupportMapFragment.newInstance()
            mFragmentTransaction.replace(R.id.map, mapFragment).commit()
        }
        mapFragment?.getMapAsync(this)

        allReports = mutableListOf()
        database = FirebaseDatabase.getInstance().reference
        database.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allReports = mutableListOf()
                var dblLat: Double?
                var dblLon: Double?
                for (messageSnapshot in dataSnapshot.children) {
                    //Checks to allow for evened out coords (eg 31.000 or 100.000)
                    if (messageSnapshot.child("latitude").value is Long){
                        dblLat = (messageSnapshot.child("latitude").value as Long).toDouble()
                    } else {
                        dblLat = messageSnapshot.child("latitude").value as Double?
                    }
                    if (messageSnapshot.child("longitude").value is Long){
                        dblLon = (messageSnapshot.child("longitude").value as Long).toDouble()
                    } else {
                        dblLon = messageSnapshot.child("longitude").value as Double?
                    }

                    val report = Report(dblLat, dblLon,
                        messageSnapshot.child("severity").value as String?,
                        messageSnapshot.child("description").value as String?,
                        messageSnapshot.child("symptoms").value as String?,
                        messageSnapshot.child("note").value as String?)
                    allReports.add(report)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        var customInfoWindow = CustomInfoWindow()
        mMap.setInfoWindowAdapter(customInfoWindow)

        //Go through all the reports and add them to the map
        for (report in allReports){
            //Get the position to add the marker
            val location = LatLng(report.latitude!!, report.longitude!!)

            //Associate a number with the smell severity to display it during in the snippet
            var smellRatingNum = 1
            //Change the color of the marker depending on the severity of the smell
            var marker = MarkerOptions().position(location)
            when (report.severity.toString()){
                getString(R.string.just_fine) -> marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                getString(R.string.barely_noticeable) -> {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    smellRatingNum = 2
                }
                getString(R.string.noticeable) -> {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    smellRatingNum = 3
                }
                getString(R.string.its_getting_pretty_bad) -> {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    smellRatingNum = 4
                }
                getString(R.string.horrible) -> {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    smellRatingNum = 5
                }
            }

            //Get the information associated with the report (ie severity, symptoms, description)
            val info = "Smell Rating: " + smellRatingNum.toString() + " (" + report.severity.toString() + ") " +
                    "\nSymptoms: " + report.symptoms.toString() +
                    "\nSmell Description: " + report.description.toString()
            marker.snippet(info)

            //Add the marker to the map
            mMap.addMarker(marker)
//            mMap.addMarker(MarkerOptions().position(location).snippet(info))
        }

        //Zoom options
        var zoomLevel = 8.0f
        val mid = LatLng(39.3343, -76.4394) //place the camera over the middle of maryland
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mid, zoomLevel))
    }

    //Custom Info Window to show the info related to the report when clicking on the coordinates of the report
    inner class CustomInfoWindow: GoogleMap.InfoWindowAdapter {

        override fun getInfoWindow(marker: Marker?): View? {
            return null
        }

        override fun getInfoContents(marker: Marker?): View? {
            var view = layoutInflater.inflate(R.layout.custom_infowindow, null)

            var info = view.findViewById<TextView>(R.id.marker_info)
            info.text = (marker!!.snippet)

            return view
        }
    }
}