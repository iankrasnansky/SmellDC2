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



@IgnoreExtraProperties
data class Report(
    var longitude: Double? = 0.0,
    var latitude: Double? = 0.0,
    var severity: String? = "",
    var description: String? = "",
    var symptoms: String? = "",
    var note: String? = ""
)

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
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

        database = FirebaseDatabase.getInstance().reference
        // Attach a listener to read the data at our posts reference
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (messageSnapshot in dataSnapshot.children) {
                    val long = messageSnapshot.child("longitude").value as Double?
                    println(long)
                    //val message = messageSnapshot.child("message").value as String?
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
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
                Log.i("a", location?.altitude.toString())
                //GRAB DATA FROM FORM INSTEAD OF JUST 0 AND EMPTY STRING
                val user = Report(location?.latitude, location?.longitude, toSend.severity , toSend.description, toSend.symptoms, toSend.note)
                database.child((Math.random()).toString().replace(".", "F")).setValue(user)
                val t = Toast.makeText(this,"Successfully sent report! Thank you for your time!", Toast.LENGTH_LONG)
                t.show()
            }
    }

}


