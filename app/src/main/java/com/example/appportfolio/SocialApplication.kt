package com.example.appportfolio

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.appportfolio.data.entities.LocationLatLngEntity
import com.example.appportfolio.other.AppLifecycleManager
import com.example.appportfolio.other.OnSingleClickListener
import com.example.appportfolio.ui.auth.activity.AuthActivity
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.FileOutputStream
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
    val Context.dataStore by preferencesDataStore("my_data_store")
    companion object{
        lateinit var instance: SocialApplication

        fun imageExternalSave(context: Context, bitmap: Bitmap, path: String): Boolean {
            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED == state) {

                val rootPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString()
                val dirName = "/" + path
                val fileName = System.currentTimeMillis().toString() + ".png"
                val savePath = File(rootPath + dirName)
                savePath.mkdirs()

                val file = File(savePath, fileName)
                if (file.exists()) file.delete()

                try {
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                    //갤러리 갱신
                    context.sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://" + rootPath+"/"+dirName+"/"+fileName)
                        )
                    )
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return false
        }
        fun handleResponse(context: Context,resultCode:Int,action:(()->Unit))
        {
            if(resultCode==505)
            {
                val intent = Intent(
                    context,
                    AuthActivity::class.java
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(this)
                }
                Toast.makeText(context,"다시 로그인해주세요", Toast.LENGTH_SHORT).show()
            }
            else
                action()

        }

        fun checkGeoPermission(context:Context):Boolean
        {
            var permission:Boolean=false
            permission = !(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return permission
        }
        fun checkIOstoragePermission(context:Context):Boolean
        {
            var permission:Boolean=false
            permission= !(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    &&ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
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