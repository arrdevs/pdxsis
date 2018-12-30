# Documentasi PD
* Install Requirment
	* Manifest declarations
		* Camera Permission 
		* Storage Permission
		* Location Permission
* Take A Photo
* Detect Location
* Report File To Server
	* Retrofit 2
* Server Information
 	
## Install Requirment
### Manifest Declaration
```xml
	<uses-feature android:name="android.hardware.camera" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!-- Needed only if your app targets Android 5.0 (API level 21) or higher. -->
    <uses-feature android:name="android.hardware.location.gps" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```


## Take A Photo
### Using Existing Camera apps
#### Take a photo with a camera app
```kotlin
val REQUEST_IMAGE_CAPTURE = 1
private fun dispatchTakePictureIntent() {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        takePictureIntent.resolveActivity(packageManager)?.also {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }
}
```

### Get the thumbnail
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        val imageBitmap = data.extras.get("data") as Bitmap
        mImageView.setImageBitmap(imageBitmap)
    }
}
```

### Save the full-size photo
#### Manifest File 
```xml
<manifest ...>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
                     android:maxSdkVersion="18" />
    ...
</manifest>

<application>
   ...
   <provider
        android:name="android.support.v4.content.FileProvider"
        android:authorities="com.example.android.fileprovider"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths"></meta-data>
    </provider>
    ...
</application>
```

##### Creating File Photo Directory
```kotlin
var mCurrentPhotoPath: String

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
```

#### Setup and Provide File Photo 
```kotlin
val REQUEST_TAKE_PHOTO = 1

private fun dispatchTakePictureIntent() {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        // Ensure that there's a camera activity to handle the intent
        takePictureIntent.resolveActivity(packageManager)?.also {
            // Create the File where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ...
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }
}
```

## Detect Location
```kotlin
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
```

## Report To Server
### Retrofit 2
#### Requirment Dependency
```xml
implementation 'com.squareup.retrofit2:retrofit:2.3.0'
implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
implementation 'com.squareup.okhttp3:okhttp:3.6.0'
```

#### EndPointInterface
```kotlin
public interface EndpointInterface {
    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter

    @Multipart
    @POST("/photo/uploadFile")
    fun uploadFile (@Part file: MultipartBody.Part,
                    @Part("country") country: String,
                    @Part("state") state: String,
                    @Part("city") city: String,
                    @Part("postalCode") postalCode: String): Call<UploadFileResponse>;
}
```
#### PresenterRequest
```kotlin
fun save(filePart: MultipartBody.Part?, omd: ObjectMapDescription) {
        val req = RetroRequest()
        req.save(filePart!!, omd)
        val tView = this.context as MyFotoCapture
        tView.responseOk()
    }
```

## Server Information
* SpringBoot java framework
* [ServerApp](https://github.com/arrdevs/example-upload-spring-boot.git)
* snippet code java retriev data
```java
@PostMapping("/uploadFile")
@ResponseBody
public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file, 
				@RequestParam("country") String country,
				@RequestParam("state") String state,
				@RequestParam("city") String city,
				@RequestParam("postalCode") String postalCode
		) {
	String fileName = fileStorageService.storeFile(file);
	
	String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
			.path("/downloadFile/")
			.path(fileName)
			.toUriString();

	return new UploadFileResponse(fileName, fileDownloadUri,
			file.getContentType(), file.getSize());
}
```

## Reference 
	* https://developer.android.com/guide/topics/media/camera
