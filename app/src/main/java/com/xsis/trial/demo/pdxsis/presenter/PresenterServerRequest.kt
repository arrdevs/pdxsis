package com.xsis.trial.demo.pdxsis.presenter

import android.content.Context
import com.xsis.trial.demo.pdxsis.MainActivity
import com.xsis.trial.demo.pdxsis.MyFotoCapture
import com.xsis.trial.demo.pdxsis.com.xsis.trial.demo.pdxsis.serverrequest.RetroRequest
import com.xsis.trial.demo.pdxsis.entity.ObjectMapDescription
import okhttp3.MultipartBody

public class PresenterServerRequest(_context: Context){

    var context: Context
    init {
        this.context = _context
    }

    fun save(filePart: MultipartBody.Part?, omd: ObjectMapDescription) {
        val req = RetroRequest()
        req.save(filePart!!, omd)
        val tView = this.context as MyFotoCapture
        tView.responseOk()
    }

    interface ResponseStatus{
        fun responseOk()
    }
}