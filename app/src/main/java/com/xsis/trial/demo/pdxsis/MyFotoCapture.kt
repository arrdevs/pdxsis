package com.xsis.trial.demo.pdxsis

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.location.*
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.xsis.trial.demo.pdxsis.com.xsis.trial.demo.pdxsis.serverrequest.RetroRequest
import com.xsis.trial.demo.pdxsis.entity.ObjectMapDescription
import com.xsis.trial.demo.pdxsis.presenter.PresenterServerRequest
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.MultipartBody




class MyFotoCapture: AppCompatActivity(), PresenterServerRequest.ResponseStatus {

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1
    lateinit var hasilFoto: ImageView
    lateinit var mCurrentPhotoPath: String
    lateinit var textCity: TextView
    lateinit var textState: TextView
    lateinit var textCountry: TextView
    lateinit var textPostalCode: TextView
    lateinit var myLocation: Location
    lateinit var mLocationManager: LocationManager
    lateinit var currentFilePhoto: File
    lateinit var presenterServerRequest: PresenterServerRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.foto_capture)
        textCity = findViewById(R.id.textCity) as TextView
        textCountry = findViewById(R.id.textCountry) as TextView
        textState = findViewById(R.id.textState) as TextView
        textPostalCode = findViewById(R.id.textPostalCode) as TextView
        presenterServerRequest = PresenterServerRequest(this)
        currentFilePhoto = File("")
        myLocation = this!!.getLastKnownLocation()!!
        hasilFoto = findViewById(R.id.hasilFoto)
        mCurrentPhotoPath = ""
        dispatchTakePictureIntent()
        getLocationCode(myLocation.latitude.toDouble(), myLocation.longitude.toDouble())

        //back icon home
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }

                currentFilePhoto = photoFile!!

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.xsis.trial.demo.pdxsis",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    //galleryAddPic()
                }

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val bmp = BitmapFactory.decodeFile(mCurrentPhotoPath)
            //setPic()
            hasilFoto.setImageBitmap(bmp)
        } else{
            onBackPressed()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    private fun getLocationCode(ltd: Double, lng: Double){
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(this, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            ltd,
            lng,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        val address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        val city = addresses[0].getLocality()
        val state = addresses[0].getAdminArea()
        val country = addresses[0].getCountryName()
        val postalCode = addresses[0].getPostalCode()
        val knownName = addresses[0].getFeatureName()

        textCity.text = city
        textPostalCode.text = postalCode
        textCountry.text = country
        textState.text = state
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l = mLocationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                // Found best last known location: %s", l);
                bestLocation = l
            }
        }
        return bestLocation
    }

    fun sendPhotoFileToServer(view: View){

        val imageFile = currentFilePhoto
        var omd: ObjectMapDescription = ObjectMapDescription(textState.text as String, textCity.text as String, textPostalCode.text as String, textCountry.text as String)
        val filePart = MultipartBody.Part.createFormData(
            "file",
            imageFile.getName(),
            RequestBody.create(MediaType.parse("image/*"), imageFile)
        )

        presenterServerRequest.save(filePart, omd)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            android.R.id.home ->{
                onBackPressed()
            }
        }

        return true
    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = 128
        val targetH: Int = 128

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(mCurrentPhotoPath, this)
            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)?.also { bitmap ->
            hasilFoto.setImageBitmap(bitmap)
        }
    }

    override fun responseOk() {
        Toast.makeText(this, "Photo successfully sent..", Toast.LENGTH_SHORT).show()
        onBackPressed()
    }

}