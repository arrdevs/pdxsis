package com.xsis.trial.demo.pdxsis.com.xsis.trial.demo.pdxsis.serverrequest;

import com.xsis.trial.demo.pdxsis.entity.ObjectMapDescription;
import com.xsis.trial.demo.pdxsis.entity.UploadFileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

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