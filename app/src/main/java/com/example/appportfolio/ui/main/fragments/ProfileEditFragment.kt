package com.example.appportfolio.ui.main.fragments
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentProfileeditBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
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
import javax.inject.Inject

@AndroidEntryPoint
class ProfileEditFragment: Fragment(R.layout.fragment_profileedit) {
    lateinit var binding: FragmentProfileeditBinding
    private val args:ProfileEditFragmentArgs by navArgs()
    lateinit var inputMethodManager: InputMethodManager
    lateinit var nick:String
    var profileurl:String?=null
    private var curImageUri: Uri?=null
    private val vmEdit: ProfileEditViewModel by viewModels()
    lateinit var api: MainApi
    private lateinit var cropContent: ActivityResultLauncher<Any?>
    lateinit var vmAuth: AuthViewModel
    @Inject
    lateinit var preferences: UserPreferences
    private val cropActivityResultContract = object: ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentProfileeditBinding>(inflater,
            R.layout.fragment_profileedit,container,false)
        api= RemoteDataSource().buildApi(MainApi::class.java,
            runBlocking { preferences.authToken.first() })
        nick=args.nickname
        profileurl=args.profileurl
        binding.edtnick.setText(nick)
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        if(profileurl==null)
        {
            when(args.gender){
                "남자"->binding.imgProfile.setImageResource(R.drawable.icon_male)
                "여자"->binding.imgProfile.setImageResource(R.drawable.icon_female)
                else->binding.imgProfile.setImageResource(R.drawable.icon_none)
            }
        }
        else{
            Glide.with(requireContext())
                .load(profileurl!!)
                .into(binding.imgProfile)
        }
        cropContent=registerForActivityResult(cropActivityResultContract){uri->
            uri?.let{
                activebutton()
                vmEdit.setCurImageUri(it)
            }
        }
        binding.edtnick.addTextChangedListener {editable->

            editable?.let{
                if(binding.edtnick.text.toString().trim().isEmpty())
                {
                    val color = getColor(requireContext(),R.color.inactive)
                    binding.complete.isClickable=false
                    binding.complete.setBackgroundColor(color)
                }
                else
                {
                    if(binding.edtnick.text.toString().equals(nick)&&curImageUri==null)
                    {
                        //프사도 변경안하고 닉네임도 변화가없는경우
                        inactivebutton()
                    }
                    else
                    {
                        //닉네임의 변화가있거나 변화가없어도 프사가 변경되어있는경우
                        activebutton()
                    }

                }
            }
        }
        binding.complete.setOnClickListener {

            showComplete()
        }
        binding.imgProfile.setOnClickListener{
            cropContent.launch(null)
        }
        subscribeToObserver()
        return binding.root
    }
    fun showComplete()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="정말 수정하시겠어요?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            if(curImageUri!=null)
                uploadImages(curImageUri!!,requireContext())
            else
                vmEdit.editprofile(null,binding.edtnick.text.toString(),api)
            dialog.dismiss()
            dialog.cancel()


        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="프로필 수정"
        super.onResume()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                findNavController().popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun activebutton()
    {
        val color = getColor(requireContext(),R.color.skinfore)
        binding.complete.isClickable=true
        binding.complete.setBackgroundColor(color)
    }
    private fun inactivebutton()
    {
        val color = getColor(requireContext(),R.color.inactive)
        binding.complete.isClickable=false
        binding.complete.setBackgroundColor(color)
    }
    private fun subscribeToObserver()
    {
        vmEdit.profileeditResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading = {
                binding.editprogressbar.visibility=View.VISIBLE
            },
            onError = {
                binding.editprogressbar.visibility=View.GONE
                snackbar(it)

            }
        ){
            binding.editprogressbar.visibility=View.GONE
            if(it.resultCode==200)
            {
                Toast.makeText(requireContext(),"변경이 완료되었습니다",Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            else{
                snackbar("서버오류 발생")
            }

        })
        vmEdit.curImageUri.observe(viewLifecycleOwner){
            curImageUri=it
            Glide.with(requireContext())
                .load(it)
                .into(binding.imgProfile)
        }
        vmEdit.uploadimgResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading = {
                binding.editprogressbar.visibility=View.VISIBLE
            },
            onError = {
                binding.editprogressbar.visibility=View.GONE
                snackbar(it)

            }
        ){
            binding.editprogressbar.visibility=View.GONE
            if(it.resultCode==200)
            {
                it.imageUri
                binding.edtnick.text.toString()
                //프로필에디트함수호출
                vmEdit.editprofile(it.imageUri,binding.edtnick.text.toString(),api)
            }
            else{
                snackbar("서버오류 발생")
            }
        })
    }
    private fun uploadImages(imageUri: Uri, context: Context)
    {
        var requestImage:MultipartBody.Part


            val file= File(getRealPathFromURI(imageUri,context))
            val requestBody=file.asRequestBody("image/*".toMediaTypeOrNull())
            var body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestBody)//이거
            requestImage=body

        vmEdit.uploadprofileimg(requestImage,api)
    }
    fun hideKeyboard() {
        if (::inputMethodManager.isInitialized.not()) {
            inputMethodManager=activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        }
        inputMethodManager.hideSoftInputFromWindow(binding.edtnick.windowToken, 0)
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

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

}