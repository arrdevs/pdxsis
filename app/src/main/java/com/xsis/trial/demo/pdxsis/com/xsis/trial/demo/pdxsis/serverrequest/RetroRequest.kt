package com.xsis.trial.demo.pdxsis.com.xsis.trial.demo.pdxsis.serverrequest;

import android.util.Log
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xsis.trial.demo.pdxsis.entity.ObjectMapDescription
import com.xsis.trial.demo.pdxsis.entity.UploadFileResponse
import okhttp3.MediaType;
import okhttp3.MultipartBody
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.File;

public class RetroRequest {

    fun create(): EndpointInterface {
        var retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.43.173:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build() as Retrofit;

        return retrofit.create(EndpointInterface::class.java)
    }

    fun save(theFile: MultipartBody.Part, theOmd: ObjectMapDescription )  {

        //var reqFile = RequestBody.create(MediaType.parse("image/*"), theFile) as RequestBody;
        create().uploadFile(theFile, theOmd.country, theOmd.state, theOmd.city, theOmd.postalCode).enqueue(object : Callback<UploadFileResponse> {
            override fun onFailure(call: Call<UploadFileResponse>, t: Throwable) {
                Log.d("contactserver", "failed!!!")
            }

            override fun onResponse(call: Call<UploadFileResponse>, response: Response<UploadFileResponse>) {

//                var myresp: UploadFileResponse = response.body() as UploadFileResponse
                Log.d("contactserver", "success : "+ response.body().toString())
            }

        });
    }
}
