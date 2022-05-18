package com.example.appportfolio.ui.main.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.appportfolio.other.Constants.ACTION_RECORD_PLAY
import com.example.appportfolio.other.Constants.ACTION_STOP_SERVICE
import com.example.appportfolio.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.appportfolio.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.appportfolio.other.Constants.NOTIFICATION_ID
import com.example.appportfolio.other.Constants.RECORD_MAX
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class recordService:LifecycleService() {
    @Inject
    @Named("recordNoti")
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    lateinit var curNotificationBuilder: NotificationCompat.Builder

    private var audioRecorder: AudioRecord? = null
    var mediaPlayer: MediaPlayer? = null
    lateinit var recordingjob: Job
    lateinit var timerJob: Job
    lateinit var progressJob: Job
    var isFirstRun: Boolean? = null
    lateinit var buffer:ByteArray

    companion object {
        val isRecording = MutableLiveData<Boolean>()
        val isRecorded =MutableLiveData<Boolean>()
        val isPlaying = MutableLiveData<Boolean>()
        val mediamax = MutableLiveData<Int>()
        val path = MutableLiveData<String>()
        val elapsed = MutableLiveData<Int>()
        val duration = MutableLiveData<Int>()
        val curpos = MutableLiveData<Int>()
        val elapsedStr = MutableLiveData<String>()
    }
    private fun postInitialValues() {
        isRecording.value=false
        isRecorded.value=false
        isPlaying.value=false
        isFirstRun = true
        elapsedStr.value="0:00"
        curpos.value=0

    }
    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        curNotificationBuilder=baseNotificationBuilder
        elapsedStr.observe(this, androidx.lifecycle.Observer {
            if (isRecording.value!!) {
                updateNoti("녹음중",it)
            }
            if (isPlaying.value!!) {
                updateNoti("재생중",it)
            }
        })
        isPlaying.observe(this, androidx.lifecycle.Observer {
            if(!it)
            {
                if(isRecorded.value!!)
                {
                    updateNoti("녹음완료",timetoString(duration.value!!))
                }
            }
        })
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_RECORD_PLAY -> {
                    if (isFirstRun!!) {
                        isFirstRun = false
                        startForegroundService()
                    }
                    record_play()
                }
                ACTION_STOP_SERVICE->{
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    fun record_play() {
        if (isRecording.value!!) {
            stopSetTime()
            stopRecording()
            updateNoti("녹음 완료됨",elapsedStr.value!!)
            curpos.postValue(0)
            isRecorded.postValue(true)
        } else {
            if (!isRecorded.value!!) {
                startRecording()
                setElapsedTime()

            } else {
                if (isPlaying.value!!) {
                    stopSetTime()
                    isPlaying.postValue(false)
                    stopAudio()
                    stopPlayback()

                } else {
                    setPlaybackElapsed()
                    isPlaying.postValue(true)
                    playAudio()
                }
            }
        }
    }
    private fun playAudio() {
        val file= File(path.value!!)
        val fis= FileInputStream(file)
        val fd=fis.fd
        fis.close()
        mediaPlayer = MediaPlayer()
            .apply {
                reset()
                setDataSource(path.value!!)
                prepare() // 재생 할 수 있는 상태 (큰 파일 또는 네트워크로 가져올 때는 prepareAsync() )
            }
        // 전부 재생 했을 때
        mediaPlayer?.setOnCompletionListener {
            stopAudio()
            isPlaying.postValue(false)
            curpos.value=0
            stopPlayback()
            stopSetTime()
        }
        mediaPlayer?.start() // 재생
        mediamax.value=mediaPlayer?.duration!!
        startPlayback()
    }
    private fun stopAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    fun setElapsedTime() {
        if (::timerJob.isInitialized) timerJob.cancel()
        elapsed.postValue(0)
        curpos.value=0
        mediamax.value=RECORD_MAX
        timetoString(0)
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while ((curpos.value!!<mediamax.value!!)&& isRecording.value!!) {
                delay(20L)
                curpos.value= curpos.value!!+20
                if (curpos.value!!%1000==0)
                {
                    elapsed.value=elapsed.value!!.plus(1)
                    timetoString(elapsed.value!!)
                    duration.postValue(elapsed.value!!)
                }
            }
            if(isRecording.value!!) {
                stopSetTime()
                stopRecording()
                updateNoti("녹음 완료됨",elapsedStr.value!!)
                isRecorded.postValue(true)
                curpos.postValue(0)
            }
        }
    }
    fun stopSetTime() {
        if (::timerJob.isInitialized) timerJob.cancel()
        elapsed.postValue(duration.value!!)
        timetoString(duration.value!!)
    }
    fun timetoString(time: Int) :String{
        val m=time/60
        val s=time%60
        val timetxt="%d:%02d".format(m,s)
        elapsedStr.postValue(timetxt)
        return "%d:%02d".format(m,s)
    }
    fun setPlaybackElapsed() {
        if (::timerJob.isInitialized) timerJob.cancel()
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (elapsed.value!! > 0) {
                delay(1000L)
                elapsed.postValue(elapsed.value!!.minus(1))
                timetoString(elapsed.value!!)
            }
        }
    }
    fun startPlayback() {
        if (::progressJob.isInitialized) progressJob.cancel()
        curpos.postValue(0)
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (mediaPlayer!!.currentPosition < mediaPlayer!!.duration) {
                delay(20L)
                curpos.postValue(mediaPlayer!!.currentPosition)
            }
        }
    }
    fun stopPlayback() {
        if (::progressJob.isInitialized) progressJob.cancel()
        curpos.postValue(0)
    }
    fun startRecording() {
        audioRecorder=createAudioRecord()
        audioRecorder?.let{
            val timeStamp = SimpleDateFormat("yyyyMMDD_HHmmss").format(Date())
            path.value="${this.externalCacheDir?.absolutePath}/" + timeStamp + "audio.wav"
            val os = FileOutputStream(path.value!!)
            writeWavHeader(os!!,1,it.sampleRate,16)
            it.startRecording()
            isRecording.value=true
            if(::recordingjob.isInitialized)recordingjob.cancel()
            recordingjob= CoroutineScope(Dispatchers.Default).launch {
                while(isRecording.value!!)
                {
                    val size = it.read(buffer, 0, buffer.size);
                    processCapture(os!!,buffer,size)
                }
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                updateWavHeader(File(path.value!!))
            }
        }
    }
    private fun stopRecording() {
        audioRecorder?.run {
            isRecording.value=false
            stop()
            release()
            recordingjob.cancel()
            audioRecorder = null
        }
        //updateNoti("녹음 완료됨",elapsedStr.value!!)
    }
    private fun processCapture(os:FileOutputStream,buffer: ByteArray, status: Int) {
        if (status == AudioRecord.ERROR_INVALID_OPERATION || status == AudioRecord.ERROR_BAD_VALUE) return
        try {
            os!!.write(buffer, 0, buffer.size)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    @Throws(IOException::class)
    fun updateWavHeader(wav: File) {
        val sizes = ByteBuffer
            .allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN) // 아마 이 두 개를 계산할 때 좀 더 좋은 방법이 있을거라 생각하지만..
            .putInt(((wav.length() - 8) ).toInt()) // ChunkSize
            .putInt(((wav.length() - 44)).toInt()) // Chunk Size
            .array()
        var accessWave: RandomAccessFile? = null
        try {
            accessWave = RandomAccessFile(wav, "rw") // 읽기-쓰기 모드로 인스턴스 생성
            // ChunkSize
            accessWave.seek(4) // 4바이트 지점으로 가서
            accessWave.write(sizes, 0, 4) // 사이즈 채움
            // Chunk Size
            accessWave.seek(40) // 40바이트 지점으로 가서
            accessWave.write(sizes, 4, 4) // 채움
        } catch (ex: IOException) {
            // 예외를 다시 던지나, finally 에서 닫을 수 있음
            throw ex
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close()
                } catch (ex: IOException) {
                }
            }
        }
    }
    fun writeWavHeader(out:FileOutputStream,channels:Short,sampleRate:Int,bitDepth:Short)
    {
        val littleBytes: ByteArray = ByteBuffer
            .allocate(14)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(channels)
            .putInt(sampleRate)
            .putInt(sampleRate * channels * (bitDepth / 8))
            .putShort((channels * (bitDepth / 8)).toShort())
            .putShort(bitDepth)
            .array()
        out.write(
            byteArrayOf(
                'R'.toByte(), 'I'.toByte(), 'F'.toByte(), 'F'.toByte(),  // Chunk ID
                0, 0, 0, 0,  // Chunk Size (나중에 업데이트 될것)
                'W'.toByte(), 'A'.toByte(), 'V'.toByte(), 'E'.toByte(),  // Format
                'f'.toByte(), 'm'.toByte(), 't'.toByte(), ' '.toByte(),  //Chunk ID
                16, 0, 0, 0,  // Chunk Size
                1, 0,  // AudioFormat
                littleBytes[0], littleBytes[1],  // Num of Channels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5],  // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9],  // Byte Rate
                littleBytes[10], littleBytes[11],  // Block Align
                littleBytes[12], littleBytes[13],  // Bits Per Sample
                'd'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte(),  // Chunk ID
                0, 0, 0, 0
            )
        )
    }
    fun createAudioRecord(): AudioRecord?
    {
        val sample_rate_canditates = arrayOf(8000,16000,11025,22050,44100)
        for(sampleRate in sample_rate_canditates)
        {
            val sizeIntBytes = AudioRecord.getMinBufferSize(sampleRate,android.media.AudioFormat.CHANNEL_IN_MONO,android.media.AudioFormat.ENCODING_PCM_16BIT)
            if(sizeIntBytes==AudioRecord.ERROR_BAD_VALUE){

                continue
            }
            val audioRecord=AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,android.media.AudioFormat.CHANNEL_IN_MONO,android.media.AudioFormat.ENCODING_PCM_16BIT,sizeIntBytes)
            if(audioRecord.state==AudioRecord.STATE_INITIALIZED){

                buffer=ByteArray(sizeIntBytes)
                return audioRecord
            }else{
                audioRecord.release()
            }
        }
        return null
    }
    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
    }
    private fun updateNoti(title:String,time: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val notification = curNotificationBuilder
            .setContentTitle(title)
            .setContentText(time)
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
    override fun onDestroy() {
        mediaPlayer?.let{
            if(mediaPlayer!!.isPlaying)
            {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        }
        if(isRecording.value!!)
        {
            stopRecording()
        }
        isFirstRun=true
        postInitialValues()
        audioRecorder=null
        mediaPlayer=null
        if(::recordingjob.isInitialized) recordingjob.cancel()
        if(::timerJob.isInitialized) timerJob.cancel()
        if(::progressJob.isInitialized) progressJob.cancel()
        stopForeground(true)
        super.onDestroy()
    }
}