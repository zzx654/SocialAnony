package com.example.appportfolio.ui.main.fragments
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentProfileeditBinding
import com.example.appportfolio.other.Constants
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.ProfileEditViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
class ProfileEditFragment: Fragment(R.layout.fragment_profileedit),MenuProvider {
    lateinit var binding: FragmentProfileeditBinding
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var nick:String
    private var profileurl:String?=null
    private var curImageUri: Uri?=null
    private var imgdeleted=false
    private var alreadyexist=false
    private val vmEdit: ProfileEditViewModel by viewModels()
    lateinit var api: MainApi
    private lateinit var cropContent: ActivityResultLauncher<Any?>
    lateinit var vmAuth: AuthViewModel
    @Inject
    lateinit var preferences: UserPreferences
    @Inject
    lateinit var loadingDialog: LoadingDialog
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
    ): View {
        binding= DataBindingUtil.inflate<FragmentProfileeditBinding>(inflater,
            R.layout.fragment_profileedit,container,false)
        (activity as MainActivity).setToolBarVisible("profileEditFragment")
        api= RemoteDataSource().buildApi(MainApi::class.java,
            runBlocking { preferences.authToken.first() })
        nick=arguments?.getString("nickname")!!
        profileurl=arguments?.getString("profileurl")
        binding.edtnick.setText(nick)
        activity?.run{
            vmAuth= ViewModelProvider(this)[AuthViewModel::class.java]
        }
        if(profileurl==null)
        {
            when(arguments?.getString("gender")!!){
                "남자"->binding.imgProfile.setImageResource(R.drawable.icon_male)
                "여자"->binding.imgProfile.setImageResource(R.drawable.icon_female)
                else->binding.imgProfile.setImageResource(R.drawable.icon_none)
            }
        }
        else{
            Glide.with(requireContext())
                .load(profileurl!!)
                .placeholder(ColorDrawable(getColor(requireContext(), R.color.gray)))
                .error(ColorDrawable(getColor(requireContext(), R.color.gray)))
                .into(binding.imgProfile)
        }
        cropContent=registerForActivityResult(cropActivityResultContract){uri->
            uri?.let{
                imgdeleted=false
                if(!alreadyexist)
                    activebutton()
                vmEdit.setCurImageUri(it)
            }
        }
        var job: Job? = null
        binding.edtnick.addTextChangedListener {editable->
            job?.cancel()
            job = lifecycleScope.launch {
                delay(Constants.SEARCH_TIME_DELAY)
                editable?.let{
                    if(binding.edtnick.text.toString().trim().isEmpty())
                    {
                        val color = getColor(requireContext(),R.color.inactive)
                        binding.complete.isClickable=false
                        binding.complete.setBackgroundColor(color)
                        binding.tvexist.visibility=View.GONE
                        binding.tvguide.visibility=View.VISIBLE
                    }
                    else
                        if(binding.edtnick.text.toString() == nick &&curImageUri==null&&!imgdeleted)
                        {
                            binding.tvexist.visibility=View.GONE
                            binding.tvguide.visibility=View.VISIBLE
                            //프사도 변경안하고 닉네임도 변화가없는경우
                            inactivebutton()
                        }
                        else
                        {
                            vmEdit.checknick(binding.edtnick.text.toString(),api)
                        }
                }
            }
        }
        binding.complete.setOnClickListener {
            showComplete()
        }
        binding.imgProfile.setOnClickListener{
            if((curImageUri==null&&profileurl==null)||imgdeleted)
                cropContent.launch(null)
            else
                showEditprofileimage()
        }
        subscribeToObserver()
        return binding.root
    }
    private fun showEditprofileimage()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_editpfimg,null)
        val change: Button =mView.findViewById(R.id.btnchange)
        val delete: Button =mView.findViewById(R.id.btndelete)

        change.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        delete.setOnClickListener {
            curImageUri=null
            if(!alreadyexist)
                activebutton()
            imgdeleted=true
            when(arguments?.getString("gender")!!){
                "남자"->binding.imgProfile.setImageResource(R.drawable.icon_male)
                "여자"->binding.imgProfile.setImageResource(R.drawable.icon_female)
                else->binding.imgProfile.setImageResource(R.drawable.icon_none)
            }
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun showComplete()
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
            {
                if(imgdeleted)
                    vmEdit.editprofile(null,binding.edtnick.text.toString(),api)
                else
                    vmEdit.editprofile("notchanged",binding.edtnick.text.toString(),api)

            }

            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    override fun onResume() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="프로필 수정"
        super.onResume()
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
                true
            }
            else->false
        }
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
        vmEdit.checknickResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    binding.tvguide.visibility = View.VISIBLE
                    binding.tvexist.visibility = View.GONE
                    alreadyexist = false
                    activebutton()
                } else {
                    alreadyexist = true
                    inactivebutton()
                    binding.tvguide.visibility = View.GONE
                    binding.tvexist.visibility = View.VISIBLE
                }
            }

        })
        vmEdit.profileeditResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading = {
                loadingDialog.show()
            },
            onError = {
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )

            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    Toast.makeText(requireContext(), "변경이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
        })
        vmEdit.curImageUri.observe(viewLifecycleOwner){
            curImageUri=it
            Glide.with(requireContext())
                .load(it)
                .placeholder(ColorDrawable(getColor(requireContext(), R.color.gray)))
                .error(ColorDrawable(getColor(requireContext(), R.color.gray)))
                .into(binding.imgProfile)
        }
        vmEdit.uploadimgResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading = {
                loadingDialog.show()
            },
            onError = {
               loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    it.imageUri
                    binding.edtnick.text.toString()
                    //프로필에디트함수호출
                    vmEdit.editprofile(it.imageUri, binding.edtnick.text.toString(), api)
                }
            }
        })
    }
    private fun uploadImages(imageUri: Uri, context: Context)
    {
        val requestImage:MultipartBody.Part
        val file= File(getRealPathFromURI(imageUri,context))
        val requestBody=file.asRequestBody("image/*".toMediaTypeOrNull())
        val body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestBody)//이거
        requestImage=body
        vmEdit.uploadimg(requestImage,api)
    }
    private fun hideKeyboard() {
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

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }

}