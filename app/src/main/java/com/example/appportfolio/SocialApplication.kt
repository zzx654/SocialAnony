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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.appportfolio.other.OnSingleClickListener
import com.example.appportfolio.ui.auth.activity.AuthActivity
import com.google.android.material.snackbar.Snackbar
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


@HiltAndroidApp
class SocialApplication: Application(),LifecycleEventObserver {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        KakaoSdk.init(this, "22b8fb238964b0aac0bf46d8bfd21a6b")
    }
    init{
        instance = this
    }
    val Context.dataStore by preferencesDataStore("my_data_store")
    companion object{
        lateinit var instance: SocialApplication

        var isDestroyed=true
        var isForeground=false

        fun imageExternalSave(context: Context, bitmap: Bitmap, path: String): Boolean {
            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED == state) {

                val rootPath =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString()
                val dirName = "/$path"
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
                            Uri.parse("file://$rootPath/$dirName/$fileName")
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
            var permission =false
            permission = !(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return permission
        }
        fun checkIOstoragePermission(context:Context):Boolean
        {
            var permission =false
            permission= !(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    &&ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return permission
        }
        fun getLocation(lat:Double,lon:Double):String?{


            val mGeoCoder =  Geocoder(instance.applicationContext, Locale.KOREAN)
            var mResultList: List<Address>? = null
            try{
                mResultList = mGeoCoder.getFromLocation(
                    lat, lon, 1
                )
            }catch(e: IOException){
                e.printStackTrace()
            }
            if(mResultList != null&& mResultList.isNotEmpty()){
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
        fun getTodayString(format: SimpleDateFormat): String {
            val today = Calendar.getInstance()

            return datetostr(today.time, format)
        }
        fun datetostr(date: Date, format: SimpleDateFormat):String
        {
            return format.format(date)
        }
        fun strtodate(data: String, format: SimpleDateFormat): Date? {
            return format.parse(data)

        }
        fun showError(view:View,context: Context,isConnected:Boolean,msg:String,actiontext:String?="확인",action:(() -> Unit)?=null){
            val error: String = if(!isConnected) {
                context.getString(R.string.networkdisdconnected)
            } else {
                "$msg\n 잠시후 다시 시도해주세요"
            }
            Snackbar.make(view,error,Snackbar.LENGTH_INDEFINITE).apply {
                setAction(actiontext){
                    action?.let{  act->
                        act()
                    }
                }
                show()
            }
        }
        fun showAlert(context:Context,text:String,action:(()->Unit)?=null)
        {
            val dialog= AlertDialog.Builder(context).create()
            val edialog: LayoutInflater = LayoutInflater.from(context)
            val mView: View =edialog.inflate(R.layout.dialog_alert,null)
            val cancel: Button =mView.findViewById(R.id.cancel)
            val positive: Button =mView.findViewById(R.id.positive)
            val alertText: TextView =mView.findViewById(R.id.tvWarn)
            alertText.text=text
            cancel.visibility=View.GONE
            positive.setOnClickListener {
                dialog.dismiss()
                dialog.cancel()
                action?.let{ act->
                    act()
                }
            }
            dialog.setView(mView)
            dialog.create()
            dialog.show()
        }

    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event){
            Lifecycle.Event.ON_PAUSE -> {
                println("onpause")
                isForeground=false
            }
            Lifecycle.Event.ON_DESTROY-> {
                println("ondestroy")
                isForeground=false
                isDestroyed=true
            }
            Lifecycle.Event.ON_RESUME-> {
                println("onresume")
                isDestroyed=false
                isForeground=true
            }
            else -> Log.d("AppMain", "onStateChanged(): event=$event")
        }
    }

}