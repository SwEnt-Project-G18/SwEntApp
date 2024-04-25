package com.github.se.gomeet.ui.mainscreens.create

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.se.gomeet.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class CustomPins {

    fun createCustomPin(
        context: Context,
        date: LocalDate,
        time: LocalTime,
        callback: (BitmapDescriptor, Bitmap) -> Unit
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customPinView = inflater.inflate(R.layout.custom_pin_layout, null, false)

        val eventDate = customPinView.findViewById<TextView>(R.id.eventDay)
        val eventTime = customPinView.findViewById<TextView>(R.id.eventTime)

        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        eventDate.text = dayOfWeek.uppercase(Locale.getDefault())
        eventTime.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))

        customPinView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        customPinView.layout(0, 0, customPinView.measuredWidth, customPinView.measuredHeight)

        val bitmap = Bitmap.createBitmap(customPinView.measuredWidth, customPinView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        customPinView.draw(canvas)

        callback(BitmapDescriptorFactory.fromBitmap(bitmap), bitmap)
    }








    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }


    fun uploadEventIcon(context: Context, byteArray: ByteArray, eventID: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val iconRef = storageRef.child("event_icons/$eventID.png")

        val uploadTask = iconRef.putBytes(byteArray)
        uploadTask.addOnSuccessListener {
            // Handle successful upload, e.g., store the download URL or path
            iconRef.downloadUrl.addOnSuccessListener { uri ->
                // Save or use the URI as needed, e.g., store it in your database
                println("Icon uploaded, URI: $uri")
            }
        }.addOnFailureListener {
            // Handle failure
            println("Upload failed: ${it.message}")
        }
    }




}