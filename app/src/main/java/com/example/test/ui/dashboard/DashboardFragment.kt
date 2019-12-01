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
                for (messageSnapshot in dataSnapshot.children){
                    //get all the data from the database and store them in reports
                    allReports.add(Report(messageSnapshot.child("latitude").value as Double?,
                        messageSnapshot.child("longitude").value as Double?,
                        messageSnapshot.child("severity").value as String?,
                        messageSnapshot.child("description").value as String?,
                        messageSnapshot.child("symptoms").value as String?,
                        messageSnapshot.child("note").value as String?))
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

            //Get the information associated with the report (ie severity, symptoms, description)
            val info = "Smell Rating: " + report.severity.toString() +
                    "\nSymptoms: " + report.symptoms.toString() +
                    "\nSmell Description: " + report.description.toString()

            //Add the marker to the map
            mMap.addMarker(MarkerOptions().position(location).snippet(info))
        }

        //Zoom options
//        var zoomLevel = 0.0f
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(38.9, -76.9), zoomLevel))
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