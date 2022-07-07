package com.example.appportfolio.ui.auth.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.ActivityFillprofileBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.NetworkConnection
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.ProfileEditViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
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
class FillProfileActivity: AppCompatActivity() {
    @Inject
    lateinit var loadingDialog: LoadingDialog
    var isConnected:Boolean?=null
    var curBirth:String?=null
    lateinit var binding: ActivityFillprofileBinding
    private val viewModel: AuthViewModel by viewModels()
    private val vmEdit: ProfileEditViewModel by viewModels()
    lateinit var authapi: AuthApi
    lateinit var mainapi: MainApi
    private var curImageUri: Uri?=null
    private lateinit var cropContent: ActivityResultLauncher<Any?>
    private val cropActivityResultContract = object: ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(this@FillProfileActivity)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }
    @Inject
    lateinit var userPreferences: UserPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_fillprofile)
        val connection = NetworkConnection(this)
        connection.observe(this) { isconnected ->
            isConnected=isconnected
        }
        authapi = RemoteDataSource().buildApi(AuthApi::class.java,
            runBlocking { userPreferences.authToken.first() } )
        mainapi= RemoteDataSource().buildApi(MainApi::class.java,
            runBlocking { userPreferences.authToken.first() })
        cropContent=registerForActivityResult(cropActivityResultContract){uri->
            uri?.let{
                vmEdit.setCurImageUri(it)
            }
        }
        binding.imgProfile.setOnClickListener{
            if(curImageUri==null)
                cropContent.launch(null)
            else
                showEditprofileimage()
        }
        binding.etNickname.addTextChangedListener { editable->
            editable?.let{
                binding.tilNickname.apply{
                    if(binding.etNickname.text.toString().trim().isEmpty())
                    {
                        isHelperTextEnabled=false
                        helperText=null
                        isErrorEnabled=false
                        error =null
                        inactivebutton()
                    }
                    else if(binding.etNickname.text.toString().length>8)
                    {
                        isHelperTextEnabled=false
                        helperText=null
                        isErrorEnabled=true
                        error ="8자 이하의 닉네임을 입력해주세요"
                        inactivebutton()
                    }
                    else
                        vmEdit.checknick(binding.etNickname.text.toString(),mainapi)
                }
            }
        }
        binding.etBirth.onSingleClick {
            val dialog = AlertDialog.Builder(this).create()
            val edialog:LayoutInflater=LayoutInflater.from(this)
            val mView : View = edialog.inflate(R.layout.dialog_datepicker,null)
            val year: NumberPicker =mView.findViewById(R.id.yearpicker_datepicker)
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
                binding.etBirth.setText("출생 년도("+birth+")")
                if(binding.rgGender.checkedRadioButtonId!=-1&&binding.etNickname.text.toString().trim().isNotEmpty()&&binding.tilNickname.error==null)
                    activebutton()

                dialog.dismiss()
                dialog.cancel()
            }

            dialog.setView(mView)
            dialog.create()
            dialog.show()
        }
        binding.rgGender.setOnCheckedChangeListener { radioGroup, i ->
            if(binding.etNickname.text.toString().trim().isNotEmpty()&&binding.tilNickname.error==null&&!curBirth.isNullOrEmpty())
                activebutton()
        }
        binding.complete.onSingleClick {
            val dialog=AlertDialog.Builder(this).create()
            val edialog:LayoutInflater= LayoutInflater.from(this)
            val mView:View=edialog.inflate(R.layout.dialog_complete,null)
            val cancel:Button=mView.findViewById(R.id.cancel)
            val save:Button=mView.findViewById(R.id.save)

            val tvNick: TextView =mView.findViewById(R.id.tvNick)
            val tvGender: TextView =mView.findViewById(R.id.tvGender)
            val tvBirth: TextView =mView.findViewById(R.id.tvBirth)

            val curgender=when(binding.rgGender.checkedRadioButtonId)  {
                R.id.rbMale ->binding.rbMale.text.toString()
                R.id.rbFemale ->binding.rbFemale.text.toString()
                R.id.rbNone ->binding.rbNone.text.toString()
                else->null
            }

            tvNick.text="닉네임:"+binding.etNickname.text.toString()
            tvGender.text="성별:"+curgender!!
            tvBirth.text="출생:"+curBirth
            cancel.setOnClickListener {
                dialog.dismiss()
                dialog.cancel()
            }

            save.setOnClickListener {
                if(curImageUri!=null)
                    uploadImages(curImageUri!!,this)
                else{
                    viewModel.AuthComplete(
                        null,
                        binding.etNickname.text.toString(),
                        curgender!!,
                        curBirth!!,
                        authapi
                    )
                }

                dialog.dismiss()
                dialog.cancel()
            }
            dialog.setView(mView)
            dialog.create()
            dialog.show()
        }
        subscribeToObserver()
    }
    private fun subscribeToObserver()
    {
        viewModel.authCompleteResponse.observe(this, Event.EventObserver(
            onError={
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    this,
                    isConnected!!,
                    it
                )
            },
            onLoading = {
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            if(it.equals("저장 완료")){
                Toast.makeText(this,"완료되었습니다", Toast.LENGTH_SHORT).show()
                Intent(this, MainActivity::class.java).also{
                    startActivity(it)
                    finish()
                }
            }
            else{
                Toast.makeText(this,it, Toast.LENGTH_SHORT).show()
            }
        })
        vmEdit.checknickResponse.observe(this,Event.EventObserver(
            onLoading = {
               // loadingDialog.show()
            },
            onError={
                //loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    this,
                    isConnected!!,
                    it
                )
            }
        ){
            //loadingDialog.dismiss()
            SocialApplication.handleResponse(this, it.resultCode) {
                binding.tilNickname.apply{
                    if(it.resultCode==200)
                    {
                        if(binding.etNickname.text.toString().isNotEmpty())
                        {
                            isErrorEnabled=false
                            error =null
                            if(binding.rgGender.checkedRadioButtonId!=-1&&!curBirth.isNullOrEmpty())
                                activebutton()
                            isHelperTextEnabled=true
                            helperText="사용가능한 닉네임입니다"
                        }

                    }
                    else{
                        inactivebutton()
                        isErrorEnabled=true
                        error="이미 사용중인 닉네임입니다"
                        isHelperTextEnabled=false
                        helperText=null
                    }

                }
            }

        })
        vmEdit.uploadimgResponse.observe(this,Event.EventObserver(
            onLoading = {
                loadingDialog.show()
            },
            onError={
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    this,
                    isConnected!!,
                    it
                )
            }
        ){
            loadingDialog.dismiss()

            SocialApplication.handleResponse(this, it.resultCode) {

                if (it.resultCode == 200) {
                    val curgender=when(binding.rgGender.checkedRadioButtonId)  {
                        R.id.rbMale ->binding.rbMale.text.toString()
                        R.id.rbFemale ->binding.rbFemale.text.toString()
                        R.id.rbNone ->binding.rbNone.text.toString()
                        else->null
                    }
                    viewModel.AuthComplete(
                        it.imageUri,
                        binding.etNickname.text.toString(),
                        curgender!!,
                        curBirth!!,
                        authapi
                    )
                }
            }

        })
        viewModel.curBirth.observe(this){
            curBirth=it
        }
        vmEdit.curImageUri.observe(this){
            curImageUri=it
            Glide.with(this)
                .load(it)
                .placeholder(ColorDrawable(ContextCompat.getColor(this, R.color.gray)))
                .error(ColorDrawable(ContextCompat.getColor(this, R.color.gray)))
                .into(binding.imgProfile)
        }
        vmEdit.uploadimgResponse.observe(this, Event.EventObserver(
            onLoading = {
                loadingDialog.show()
            },
            onError = {
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    this,
                    isConnected!!,
                    it
                )
            }
        ){
            loadingDialog.dismiss()
            SocialApplication.handleResponse(this, it.resultCode) {
                if (it.resultCode == 200) {
                    it.imageUri
                }
            }
        })
    }
    private fun uploadImages(imageUri: Uri, context: Context)
    {
        var requestImage: MultipartBody.Part
        val file= File(getRealPathFromURI(imageUri,context))
        val requestBody=file.asRequestBody("image/*".toMediaTypeOrNull())
        var body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestBody)//이거
        requestImage=body
        vmEdit.uploadimg(requestImage,mainapi)
    }
    private fun activebutton()
    {
        val color = ContextCompat.getColor(this, R.color.skinfore)
        binding.complete.isClickable=true
        binding.complete.setBackgroundColor(color)
    }
    private fun inactivebutton()
    {
        val color = ContextCompat.getColor(this, R.color.inactive)
        binding.complete.isClickable=false
        binding.complete.setBackgroundColor(color)
    }
    private fun showEditprofileimage()
    {
        val dialog= AlertDialog.Builder(this).create()
        val edialog: LayoutInflater = LayoutInflater.from(this)
        val mView: View =edialog.inflate(R.layout.dialog_editpfimg,null)
        val change: Button =mView.findViewById(R.id.btnchange)
        val delete: Button =mView.findViewById(R.id.btndelete)

        change.setOnClickListener {
            cropContent.launch(null)
            dialog.dismiss()
            dialog.cancel()
        }
        delete.setOnClickListener {
            curImageUri=null
            binding.imgProfile.setImageResource(R.drawable.icon_none)
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun getRealPathFromURI(contentUri: Uri, context: Context):String?
    {

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

}