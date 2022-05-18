package com.example.appportfolio.ui.auth.activity

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.example.appportfolio.*
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.ActivityAuthcompleteBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AuthCompleteActivity: AppCompatActivity() {
    lateinit var binding:ActivityAuthcompleteBinding
    private val viewModel: AuthViewModel by viewModels()

    lateinit var api: AuthApi
    var nicknameChecked:Boolean=false
    var curplatform:String?=null
    var curaccount:String?=null
    var curgender:String?=null
    var curBirth:String?=null
    @Inject
    lateinit var userPreferences: UserPreferences
    val PERMISSION_REQUEST_CODE=26
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this, R.layout.activity_authcomplete)
        api = RemoteDataSource().buildApi(AuthApi::class.java,
            runBlocking { userPreferences.authToken.first() } )
        subscribeToObserver()

        binding.etNickname.addTextChangedListener { text: Editable? ->
            text?.let {
                binding.tilNickname.apply{
                    isErrorEnabled=false
                    error=null
                    isHelperTextEnabled=true
                    helperText=context.getString(R.string.nickname_guide)
                }
                viewModel.setNicknameChecked(false)
            }

        }
        binding.btnCheck.setOnClickListener {
            var nickname:String
            nickname=binding.etNickname.text.toString()
            if(nickname.isEmpty())
            {
                binding.tilNickname.apply{
                    isHelperTextEnabled=false
                    helperText=null
                    isErrorEnabled=true
                    error="닉네임을 입력해주세요"
                }
            }
            else{
                viewModel.checkNickname(nickname,api)
            }
        }
        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->
            var checkedtext:String?=null
            when(checkedId){
                R.id.rbMale ->checkedtext=binding.rbMale.text.toString()
                R.id.rbFemale ->checkedtext=binding.rbFemale.text.toString()
                R.id.rbNone ->checkedtext=binding.rbNone.text.toString()
            }
            viewModel.setCurGender(checkedtext!!)
        }
        binding.btnBirth.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            val edialog:LayoutInflater=LayoutInflater.from(this)
            val mView : View = edialog.inflate(R.layout.dialog_datepicker,null)
            val year:NumberPicker=mView.findViewById(R.id.yearpicker_datepicker)
            val cancel: Button =mView.findViewById(R.id.cancel)
            val save : Button = mView.findViewById(R.id.save_button_datepicker)

            //  순환 안되게 막기
            year.wrapSelectorWheel = false

            //  editText 설정 해제
            year.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

            //  최소값 설정
            year.minValue = Calendar.getInstance().get(Calendar.YEAR)-80
            //  최대값 설정
            year.maxValue = Calendar.getInstance().get(Calendar.YEAR)-15

            year.value=year.maxValue

            //  취소 버튼 클릭 시
            cancel.setOnClickListener {
                dialog.dismiss()
                dialog.cancel()
            }

            //  완료 버튼 클릭 시
            save.setOnClickListener {

                var birth:String
                birth=(year.value).toString()
                viewModel.setCurBirth(birth)
                binding.btnBirth.text="출생 선택("+birth+")"

                dialog.dismiss()
                dialog.cancel()
            }

            dialog.setView(mView)
            dialog.create()
            dialog.show()
        }
        binding.btnStart.setOnClickListener {
            if(nicknameChecked==false)
            {
                Toast.makeText(this,"닉네임 중복체크를 해주세요",Toast.LENGTH_SHORT).show()
            }
            else if(curgender.isNullOrEmpty())
            {
                Toast.makeText(this,"성별을 선택해주세요",Toast.LENGTH_SHORT).show()
            }
            else if(curBirth.isNullOrEmpty())
            {
                Toast.makeText(this,"생년월을 선택해주세요",Toast.LENGTH_SHORT).show()
            }
            else{
                val dialog=AlertDialog.Builder(this).create()
                val edialog:LayoutInflater= LayoutInflater.from(this)
                val mView:View=edialog.inflate(R.layout.dialog_complete,null)
                val cancel:Button=mView.findViewById(R.id.cancel)
                val save:Button=mView.findViewById(R.id.save)

                val tvNick: TextView =mView.findViewById(R.id.tvNick)
                val tvGender: TextView =mView.findViewById(R.id.tvGender)
                val tvBirth: TextView =mView.findViewById(R.id.tvBirth)

                tvNick.text="닉네임:"+binding.etNickname.text.toString()
                tvGender.text="성별:"+curgender
                tvBirth.text="출생:"+curBirth
                cancel.setOnClickListener {
                    dialog.dismiss()
                    dialog.cancel()
                }

                save.setOnClickListener {
                    viewModel.AuthComplete(
                        binding.etNickname.text.toString(),
                        curgender!!,
                        curBirth!!,
                        api
                    )
                    dialog.dismiss()
                    dialog.cancel()
                }
                dialog.setView(mView)
                dialog.create()
                dialog.show()
            }
        }
        if(!SocialApplication.checkGeoPermission(this))
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    private fun subscribeToObserver()
    {
        viewModel.curBirth.observe(this){
            curBirth=it
        }
        viewModel.nicknameChecked.observe(this){
            nicknameChecked=it
        }

        viewModel.nicknameResponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            },
            onLoading={

            }
        ){
            handleResponse(this,it.resultCode){
                if(it.resultCode==100)
                {
                    viewModel.setNicknameChecked(true)
                    binding.tilNickname.apply{
                        helperText=context.getString(R.string.nickname_success)
                    }

                }
                else{
                    binding.tilNickname.apply{
                        isHelperTextEnabled=false
                        helperText=context.getString(R.string.nickname_guide)
                        isErrorEnabled=true
                        error=context.getString(R.string.nickname_using)
                    }

                }
            }

        })
        viewModel.curGender.observe(this){
            curgender=it
        }

        viewModel.authCompleteResponse.observe(this, Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT)
            },
            onLoading = {
                binding.registerProgressBar.visibility= View.VISIBLE
            }
        ){
            binding.registerProgressBar.visibility=View.GONE
            if(it.equals("저장 완료")){
                Toast.makeText(this,"완료되었습니다",Toast.LENGTH_SHORT).show()
                Intent(this, MainActivity::class.java).also{
                startActivity(it)
                finish()
                }
            }
            else{
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        })
    }
}