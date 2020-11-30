package com.example.spacediary

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest

    private var PERMISSION_ID = 7777
    var numOfLogs: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        // initializing
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val handler = Handler()
        val delay: Long = 500 // in milliseconds


        handler.postDelayed(object : Runnable {
            override fun run() {
                getLastLocation()
                handler.postDelayed(this, delay)
            }
        }, delay)

        handler.postDelayed(object : Runnable {
            override fun run() {
                retrieveLogs()
                handler.postDelayed(this, 3000)
            }
        }, 3000)

        button_write.setOnClickListener{
            // go to create memo activity
            getLastLocation()
            val intent = Intent(this, CreateMemoActivity::class.java)
            startActivity(intent)
            finish()
        }

        button_read.setOnClickListener{
            // go to read memo activity
            getLastLocation()
            val intent = Intent(this, ReadMemoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun retrieveLogs() {
        val db = FirebaseFirestore.getInstance()
        db.collection("logs")
            .get()
            .addOnCompleteListener{
                if(it.isSuccessful) {

                    for(document in it.result!!) {

                        // find lat and long values of each log
                        val lat = document.data.getValue("latitude")
                        val long = document.data.getValue("longitude")


                        val results = FloatArray(1)
                        Location.distanceBetween(lat as Double, long as Double, Globals.currentLatitude, Globals.currentLongitude, results)
                        val distanceInMeters = results[0]
                        val isWithinRange = distanceInMeters < 3 // change this back to 3-5 for demo?
                        Log.i("Debug", "distance between: ${distanceInMeters}")

                        // if within radius of current location, increment num of notes
                        if(isWithinRange){
                            Log.i("Debug", "found note in main! :)")
                            numOfLogs += 1
                        }
                    }

                    // Change text to show num of logs
                    text_num.text = "${numOfLogs} logs found in your location"

                    // Reset num
                    numOfLogs = 0

                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun getNewLocation() {
//        Log.i("Debug", "getting NEW location")
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest, locationCallback, Looper.myLooper()
        )
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation

            // Set globals
            Globals.currentLatitude = lastLocation.latitude
            Globals.currentLongitude = lastLocation.longitude

            // Set new location in text view
            text_location.text = "Latitude: " + lastLocation.latitude + " Longitude: " + lastLocation.longitude
//            Log.i("Debug", "updated location to: lat ${lastLocation.latitude}, long ${lastLocation.longitude}")
        }
    }

    private fun getLastLocation() {
        if(checkPermission()) {
            if(isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener{ task ->
                    var location = task.result
                    getNewLocation()
                }
            } else {
                Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermission()
        }
    }


    // Check user permission
    private fun checkPermission():Boolean {
        if(
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Get user permission
    private fun requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_ID
        )
    }

    // Check if location services are enabled on user's device
    private fun isLocationEnabled():Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // For debugging - to check if permission has been granted
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if(requestCode == PERMISSION_ID) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Debug", "Permission granted!")
            }
        }
    }
}