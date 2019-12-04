package com.example.test

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

//Location and firebase stuff
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.log
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.LocationManager
import android.os.Looper
import java.util.*
import java.time.LocalDate


@IgnoreExtraProperties
data class Report(
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    var severity: String? = "",
    var description: String? = "",
    var symptoms: String? = "",
    var note: String? = "",
    var dayOfYear: Long? = 0,
    var year: Long? = 0
)

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private lateinit var allReports: MutableList<Report>
    public var toSend = Report()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_report, R.id.navigation_map
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //button1 = findViewById<Button>(R.id.navigation_report)
        //More firebase code

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //fab.setOnClickListener {
        //    getGeoData()
        //}
        allReports = mutableListOf()
        database = FirebaseDatabase.getInstance().reference
        // Attach a listener to read the data at our posts reference
        database.addValueEventListener(object : ValueEventListener {
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

                    allReports.add(Report(dblLat, dblLon,
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
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            2 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getGeoData()
                } else {
                    Log.i("a", "Dangerous App won't open --- Permission was not granted")
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if (locationResult == null) {
                return
            }
        }
    }

    fun getGeoData(){
        var permissionCheck = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION")
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.ACCESS_FINE_LOCATION"), 2)
            return
        } else {
            Log.i("a", "t2")
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if(location != null && location!!.accuracy < 70) {
                    val user = Report(
                        location?.latitude,
                        location?.longitude,
                        toSend.severity,
                        toSend.description,
                        toSend.symptoms,
                        toSend.note,
                        LocalDate.now().dayOfYear as Long?,
                        LocalDate.now().year as Long?
                    )
                    database.child((Math.random()).toString().replace(".", "F")).setValue(user)
                    val t = Toast.makeText(
                        this,
                        "Successfully sent report! Thank you for your time!",
                        Toast.LENGTH_LONG
                    )
                    t.show()
                    fusedLocationClient.removeLocationUpdates(mLocationCallback)
                }else{
                    val t = Toast.makeText(
                        this,
                        "Location data is insufficiently accurate, please wait and try again.",
                        Toast.LENGTH_LONG
                    )
                    t.show()
                    val mLocationRequest = LocationRequest.create()
                    mLocationRequest.interval = 60000
                    mLocationRequest.fastestInterval = 5000
                    mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                    fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                        Looper.getMainLooper())
                }
            }
    }

}


