package com.example.spacediary

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_read_memo.*

class ReadMemoActivity : AppCompatActivity() {
    val logList = ArrayList<Logs>()

    class Memory {
        lateinit var title: Any
        lateinit var memo: Any
        lateinit var timestamp: Any
        lateinit var latitude: Any
        lateinit var longitude: Any
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_memo)

        supportActionBar?.hide()

        Log.i("Debug", "READ lat: ${Globals.currentLatitude} long: ${Globals.currentLongitude}")

        btn_home.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        retrieveLogs()
    }

    private fun setRecyclerView() {
        Log.i("Debug", "We're in recycler")
        val logAdapter = LogAdapter(logList)
        recyclerView.adapter = logAdapter
        recyclerView.setHasFixedSize(false)
    }

    private fun addLog(title: String, memo: String) {
        Log.i("Debug", "Adding ${title} and ${memo}")
        logList.add(
                Logs(
                        title,
                        memo
                )
        )
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

                        // if within radius of current location, add this log to list
                        if(isWithinRange){
                            Log.i("Debug", "within range! :)")
                            val title = document.data.getValue("title")
                            val memo = document.data.getValue("memo")
                            addLog(title as String, memo as String)
                        }
                    }

                    setRecyclerView()
                }
            }
    }
}
