package com.example.appportfolio.ui.main.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.api.responses.uploadAudioResponse
import com.example.appportfolio.api.responses.uploadImagesResponse
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Resource
import com.example.appportfolio.repositories.UploadRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class uploadService: LifecycleService(){
    @Inject
    lateinit var uploadRepository: UploadRepository

    lateinit var api:MainApi
    companion object {
        val Audiofile=MutableLiveData<Event<Resource<String>>>()
        val imgs=MutableLiveData<String?>()
        val imgUris=MutableLiveData<List<Uri>>()
        val recordedPath=MutableLiveData<String?>()
        val uuid=MutableLiveData<String>()
        val anonymous=MutableLiveData<String>()
        val voptions= MutableLiveData<String?>()
        val text=MutableLiveData<String>()
        val tags=MutableLiveData<String?>()
        val latitude=MutableLiveData<Double?>()
        val longitude=MutableLiveData<Double?>()
        val todayString=MutableLiveData<String>()
        val imageResponse= MutableLiveData<Event<Resource<uploadImagesResponse>>>()
        val audioResponse= MutableLiveData<Event<Resource<uploadAudioResponse>>>()
        val postResponse = MutableLiveData<Event<Resource<String>>>()
    }
    inner class mBinder: Binder(){
        fun getService():uploadService{
            return this@uploadService
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }
    var binder=mBinder()

    fun initialValues(
        imgs:List<Uri>,
        voice:String?,
        postid:String,
        nickanony:String,
        txt:String,
        tagstr:String?,
        lat:Double?,
        long:Double?,
        todayStr:String,
        voteoptions: String?
    ) {
        val preferences= UserPreferences(this)

        //authapi= RemoteDataSource().buildApi(AuthApi::class.java,runBlocking { preferences.authToken.first() })
       // mainapi=RemoteDataSource().buildApi(MainApi::class.java)
        api= RemoteDataSource().buildApi(MainApi::class.java,runBlocking { preferences.authToken.first() })
        imgUris.value=imgs
        recordedPath.value=voice
        uuid.value=postid
        anonymous.value=nickanony
        text.value=txt
        tags.value=tagstr
        latitude.value=lat
        longitude.value=long
        todayString.value=todayStr
        voptions.value=voteoptions
    }
    fun startPosting(
        imgUris:List<Uri>,
        recordedPath:String?,
        postuuid:String,
        anonymous:String,
        text:String,
        tags:String?,
        latitude:Double?,
        longitude:Double?,
        todayString:String,
        voteoptions:String?
    ) {
        initialValues(imgUris,recordedPath,postuuid,anonymous,text,tags,
        latitude,longitude,todayString,voteoptions)
        subsribeToObserver()
        if(imgUris.isNullOrEmpty())
        {
            if(recordedPath.isNullOrEmpty())
            {
               postContent(
                    postuuid,
                    anonymous,
                    text,
                    tags,
                    latitude,
                    longitude,
                    todayString,
                    "NONE",
                    "NONE",
                   voptions.value,
                   api
                )
            }
            else
            {
                uploadAudio(recordedPath!!)
            }
        }
        else
        {
            uploadImages(imgUris,this)
        }

    }
    fun uploadAudio(recordedPath: String)
    {
        var file= File(recordedPath)
        val requestBody=file.asRequestBody("audio/wav".toMediaTypeOrNull())
        var body: MultipartBody.Part=
            MultipartBody.Part.createFormData("media",file.name,requestBody)
        postAudio(body,api)
    }
    private fun uploadImages(imageUris: List<Uri>, context: Context)
    {
        var requestImages:MutableList<MultipartBody.Part> = mutableListOf()

        for(imageUri in imageUris)
        {
            val file= File(getRealPathFromURI(imageUri,context))
            val requestBody=file.asRequestBody("image/*".toMediaTypeOrNull())
            var body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestBody)//이거
            requestImages.add(body)
        }
        postImage(requestImages,api)
    }
    private fun getRealPathFromURI(contentUri: Uri, context: Context):String?
    {
        val context = applicationContext
        val contentResolver = context.contentResolver ?: return null
        // 파일생성
        val filePath = (context.applicationInfo.dataDir + File.separator
                + System.currentTimeMillis())
        val file = File(filePath)
        try {
            val inputStream = contentResolver.openInputStream(contentUri) ?: return null
            val outputStream: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return filePath
    }
    fun postImage(
        images:List<MultipartBody.Part>,
        api:MainApi
    ){
        imageResponse.postValue(Event(Resource.Loading()))
        CoroutineScope(Dispatchers.Main).launch {
            val result: Resource<uploadImagesResponse> = uploadRepository.postImage(images,api)
            imageResponse.postValue(Event(result))
        }
    }
    fun postAudio(
        media:MultipartBody.Part,
        api:MainApi
    ){
        audioResponse.postValue(Event(Resource.Loading()))
        CoroutineScope(Dispatchers.Main).launch {
            val result:Resource<uploadAudioResponse> = uploadRepository.postAudio(media,api)
            audioResponse.postValue(Event(result))
        }
    }
    fun postContent(
        postid:String,
        anonymous:String,
        postText:String,
        tags:String?,
        latitude: Double?,
        longitude: Double?,
        date:String,
        image:String,
        audio:String,
        voteoptions: String?,
        api:MainApi
    ){
        postResponse.postValue(Event(Resource.Loading()))
        CoroutineScope(Dispatchers.Main).launch {
            val result= uploadRepository.postContent(postid,anonymous,postText,tags,latitude,
                longitude,date,image,audio,voteoptions,api)
            postResponse.postValue(Event(result))
        }
    }

    fun subsribeToObserver()
    {
        Audiofile.observe(this,Event.EventObserver(

        ){
            if(imgs.value.isNullOrEmpty())
            {
                imgs.value="NONE"
            }
            postContent(
                uuid.value!!,
                anonymous.value!!,
                text.value!!,
                tags.value,
                latitude.value,
                longitude.value,
                todayString.value!!,
                imgs.value!!,
                it,
                voptions.value,
                api
            )
        })


        audioResponse.observe(this,Event.EventObserver(
            onError = {
                Toast.makeText(this,"오류가 발생했습니다 죄송합니다", Toast.LENGTH_SHORT).show()
            }
        ){
            Audiofile.value=Event(Resource.Success(it.uri))
        })
       imageResponse.observe(this, Event.EventObserver(
           onError={
               Log.d("이미지 저장 에러",it)
           }

       ){
           imgs.value=it.imageUris
           if(!recordedPath.value.isNullOrEmpty())
           {
               uploadAudio(recordedPath.value!!)   //녹음파일 업로드
           }
           else
           {
               Audiofile.value = Event(Resource.Success("NONE"))
           }
       })
    }

    override fun onDestroy() {
        recordedPath.value=null
        imgs.value=null
        super.onDestroy()
    }

}