package com.example.appportfolio

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.appportfolio.data.entities.LocationLatLngEntity
import com.example.appportfolio.other.AppLifecycleManager
import com.example.appportfolio.other.OnSingleClickListener
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ActivityContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@HiltAndroidApp
class SocialApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleManager)
        KakaoSdk.init(this, "22b8fb238964b0aac0bf46d8bfd21a6b")
    }
    init{

        instance = this

    }
    companion object{
        lateinit var instance: SocialApplication
        fun checkGeoPermission(context:Context):Boolean
        {
            var permission:Boolean=false
            permission = !(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return permission
        }
        fun getLocation(lat:Double,lon:Double):String?{

            val curloc= LocationLatLngEntity(
                lat.toFloat(),
                lon.toFloat()
            )

            var mGeoCoder =  Geocoder(instance.applicationContext, Locale.KOREAN)
            var mResultList: List<Address>? = null
            try{
                mResultList = mGeoCoder.getFromLocation(
                    lat, lon, 1
                )
            }catch(e: IOException){
                e.printStackTrace()
            }
            if(mResultList != null&&mResultList.size>0){
                return mResultList[0].getAddressLine(0)

            }
            return null

        }
        fun View.onSingleClick(action: (v: View) -> Unit) {
            val listener = View.OnClickListener { action(it) }
            setOnClickListener(OnSingleClickListener(listener))
        }

        fun getAge(birthyear:Int):Int
        {
            val cal= Calendar.getInstance()
            val year=cal.get(Calendar.YEAR).toString()
            return year.toInt()-birthyear+1
        }
        fun getTodayString(format: SimpleDateFormat):String
        {
            var today= Calendar.getInstance()
            var todaystr= datetostr(today.time,format)

            return todaystr
        }
        fun datetostr(date: Date, format: SimpleDateFormat):String
        {
            return format.format(date)
        }
        fun strtodate(data:String,format:SimpleDateFormat):Date
        {
            var date=format.parse(data)
            return date

        }
    }

}