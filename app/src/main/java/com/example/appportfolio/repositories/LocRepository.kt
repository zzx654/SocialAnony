package com.example.appportfolio.repositories

import com.example.appportfolio.api.build.LocApi
import com.example.appportfolio.other.Resource
import com.example.appportfolio.safeCall
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ActivityScoped
class LocRepository {
    suspend fun getSearchLocation(keyword:String,page:Int,api:LocApi)= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getSearchLocation(keyword=keyword,page=page))
        }
    }

    suspend fun getReverseGeoCode(lat:Double,lon:Double,api:LocApi)= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getReverseGeoCode(lat=lat,lon=lon))
        }
    }
}