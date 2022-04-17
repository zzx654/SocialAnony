package com.example.appportfolio.repositories

import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.other.Resource
import com.example.appportfolio.safeCall
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

@ServiceScoped
class UploadRepository {
    suspend fun postContent(
        postid:String,
        anonymous:String,
        text:String,
        tags:String?,
        latitude:Double?,
        longitude:Double?,
        date:String,
        image:String,
        audio:String,
        voteoptions:String?,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.postContents(postid, anonymous, text,tags, latitude, longitude, date, image, audio,voteoptions))
        }
    }
    suspend fun postImage(images:List<MultipartBody.Part>, api: MainApi)
            = withContext(Dispatchers.IO){
        safeCall{

            Resource.Success(api.uploadImageRequest(images))
        }
    }
    suspend fun postAudio(media: MultipartBody.Part, api: MainApi)
            = withContext(Dispatchers.IO){
        safeCall{

            Resource.Success(api.postImageRequest(media))
        }
    }
}