package com.example.appportfolio.ui.main.dialog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.appportfolio.R
import com.example.appportfolio.databinding.DialogRecordBinding
import com.example.appportfolio.other.Constants.ACTION_RECORD_PLAY
import com.example.appportfolio.other.Constants.ACTION_STOP_SERVICE
import com.example.appportfolio.ui.main.services.RecordService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordFragment(val save:(String)->Unit):BottomSheetDialogFragment() {

    lateinit var binding: DialogRecordBinding

    private val recordPermission= Manifest.permission.RECORD_AUDIO
    private val PERMISSION_CODE=21

    private var isRecording = false
    private var isRecorded=false


    private var isPlaying:Boolean=false

    var path:String?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate<DialogRecordBinding>(inflater,
            R.layout.dialog_record,container,false)

        binding.audioRecordImageBtn.setOnClickListener {
            if(checkAudioPermission())
            {
                sendCommandToService(ACTION_RECORD_PLAY)
                if(isRecording)
                {
                    //서비스에서 타이머로 녹음시간멈추게함
                    //서비스에서 isRecorded=true
                    binding.audioRecordImageBtn.setImageResource(R.drawable.ic_play)
                }
                else
                {
                    if(!isRecorded)
                    {
                        binding.audioRecordImageBtn.setImageResource(R.drawable.ic_stop)
                    }
                    else
                    {
                        if(isPlaying)
                        {
                            binding.progressRecord.progress=0
                            binding.audioRecordImageBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_play,null))

                        }
                        else{
                            binding.audioRecordImageBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_stop,null))
                        }

                    }

                }
            }

        }
        binding.btnSave.setOnClickListener {
            if(isRecording||path==null)
            {
                Toast.makeText(requireContext(),"녹음된 내용이 없습니다",Toast.LENGTH_SHORT).show()
            }
            else
            {
              save(path!!)
                dialog?.dismiss()
            }

        }
        binding.btnexit.setOnClickListener {

            dialog?.dismiss()
        }
        subscribeToObserver()
        return binding.root
    }

    private fun subscribeToObserver()
    {
        RecordService.path.observe(viewLifecycleOwner){
            path=it
        }
        RecordService.isRecording.observe(viewLifecycleOwner){

            isRecording=it
        }
        RecordService.isRecorded.observe(viewLifecycleOwner){

            isRecorded=it
        }
        RecordService.isPlaying.observe(viewLifecycleOwner){
            isPlaying=it
            if(!it && RecordService.isRecorded.value!!)
            {
                binding.audioRecordImageBtn.setImageResource(R.drawable.ic_play)
            }
        }
        RecordService.elapsedStr.observe(viewLifecycleOwner){
            binding.timetxt.text=it
        }
        RecordService.curpos.observe(viewLifecycleOwner){
            binding.progressRecord.progress=it
        }
        RecordService.mediamax.observe(viewLifecycleOwner){
            binding.progressRecord.max=it
        }
    }

    private fun sendCommandToService(action:String)=
        Intent(requireContext(),RecordService::class.java).also{
            it.action=action
            requireContext().startService(it)
        }
    private fun checkAudioPermission():Boolean{
        return if(ActivityCompat.checkSelfPermission(requireContext(),recordPermission)== PackageManager.PERMISSION_GRANTED){
            true
        }else{
            ActivityCompat.requestPermissions(requireActivity(),  arrayOf((recordPermission)), PERMISSION_CODE)
            false
        }
    }
    override fun onDestroy() {
        sendCommandToService(ACTION_STOP_SERVICE)
        super.onDestroy()
    }


}