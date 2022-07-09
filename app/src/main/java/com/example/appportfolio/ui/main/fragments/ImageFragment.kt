package com.example.appportfolio.ui.main.fragments

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.imageExternalSave
import com.example.appportfolio.databinding.FragmentImgBinding
import com.example.appportfolio.other.ImageLoader
import com.example.appportfolio.ui.main.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageFragment: Fragment(R.layout.fragment_img) {
    lateinit var binding: FragmentImgBinding
    private val PERMISSION_REQUEST_CODE=26
    private var bitmap:Bitmap?=null
    private val imageurl:String
        get(){
            return arguments?.getString("image","")!!
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding= DataBindingUtil.inflate<FragmentImgBinding>(inflater,
            R.layout.fragment_img,container,false)

        (activity as MainActivity).setToolBarVisible("imageFragment")
        binding.close.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        CoroutineScope(Dispatchers.Main).launch{
            bitmap = withContext(Dispatchers.IO){
                ImageLoader.loadImage(imageurl)
            }
            binding.img.setImageBitmap(bitmap)
        }
        binding.btnsave.setOnClickListener {
            //권한 체크
            bitmap?.let{ bitmap->
                if(!SocialApplication.checkIOstoragePermission(requireContext()))
                {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        PERMISSION_REQUEST_CODE
                    )
                    return@setOnClickListener
                }
                //그림 저장
                if(!imageExternalSave(requireContext(), bitmap, requireContext().getString(R.string.app_name))){
                    Toast.makeText(requireContext(), "그림 저장을 실패하였습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                Toast.makeText(activity, "그림이 갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()
            }

        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }

}