package com.example.spacediary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_creatememo.*
import java.util.*

class CreateMemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.`activity_creatememo`)

        supportActionBar?.hide()

        Log.i("Debug", "WRITE lat: ${Globals.currentLatitude} long: ${Globals.currentLongitude}")

        btn_home.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_submit.setOnClickListener {
            // when submit button is clicked, check that both title and memo are not empty
            if (input_title.text.toString().isEmpty()) {
                Toast.makeText(this@CreateMemoActivity, "Please enter a title", Toast.LENGTH_SHORT).show()
            } else if (input_memo.text.toString().isEmpty()){
                Toast.makeText(this@CreateMemoActivity, "Please enter a story", Toast.LENGTH_SHORT).show()
            }
            else {
                // store inputs
                val title = input_title.text.toString()
                val memo = input_memo.text.toString()

                // write to db
                saveFireStore(title, memo)

                // return to main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

    }

    fun saveFireStore(title: String, memo: String) {
        val db = FirebaseFirestore.getInstance()

        val log:HashMap<String, Any> = hashMapOf(
            "title" to title,
            "memo" to memo,
            "latitude" to Globals.currentLatitude,
            "longitude" to Globals.currentLongitude,
            "timestamp" to Timestamp.now()
        )

        db.collection("logs")
            .add(log)
            .addOnSuccessListener {
                Toast.makeText(this, "Record added! :)", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this, "Uh oh - record could not be added :(", Toast.LENGTH_SHORT).show()
            }

    }
}