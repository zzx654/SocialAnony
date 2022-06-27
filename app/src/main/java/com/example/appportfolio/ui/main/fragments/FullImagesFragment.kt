package com.example.appportfolio.ui.main.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.FullImageAdapter
import com.example.appportfolio.data.entities.ChatImages
import com.example.appportfolio.databinding.FragmentFullimagesBinding
import com.example.appportfolio.other.ImageLoader
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.DownloadProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class FullImagesFragment: Fragment(R.layout.fragment_fullimages) {
    lateinit var binding:FragmentFullimagesBinding
    private lateinit var imagesAdapter:FullImageAdapter
    private var chatimages: ChatImages? = arguments?.getParcelable("chatimages")
    private var curpage=0
    private val PERMISSION_REQUEST_CODE=26
    @Inject
    lateinit var progressDialog: DownloadProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagesAdapter= FullImageAdapter(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentFullimagesBinding>(inflater,
            R.layout.fragment_fullimages,container,false)
        (activity as MainActivity).setToolBarVisible("fullImagesFragment")
        chatimages=arguments?.getParcelable("chatimages")!!
        curpage=arguments?.getInt("position",0)!!
        chatimages?.chatimages?.let{
            imagesAdapter.submitList(it)
        }
        binding.btnsave.setOnClickListener {
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
            //다운하기
            CoroutineScope(Dispatchers.Main).launch{
                progressDialog.show()
                val bitmap = withContext(Dispatchers.IO){
                    ImageLoader.loadImage(imagesAdapter.currentList[curpage].imageUrl)
                }
                bitmap?.let{

                    //그림 저장
                    if(!SocialApplication.imageExternalSave(
                            requireContext(),
                            bitmap,
                            requireContext().getString(R.string.app_name)
                        )
                    ){
                        Toast.makeText(requireContext(), "그림 저장을 실패하였습니다", Toast.LENGTH_SHORT).show()
                    }
                    else
                        Toast.makeText(activity, "그림이 갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()
                }
                progressDialog.dismiss()
            }
        }
        setTime(imagesAdapter.currentList[curpage].date)


        binding.close.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        setupRecyclerView()
        return binding.root
    }
    private fun setupRecyclerView() = binding.vpimg.apply{
        adapter=imagesAdapter
        orientation= ViewPager2.ORIENTATION_HORIZONTAL
        offscreenPageLimit=1
        setCurrentItem(curpage,false)
        getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                curpage=position
                setTime(imagesAdapter.currentList[curpage].date)

            }
        })
    }
    fun setTime(time:String)
    {
        val resdate= SimpleDateFormat("yyyy-MM-dd h:mm:ss").parse(time)

        var fm=SimpleDateFormat("yyyy.M.d")
        binding.tvDate.text=fm.format(resdate)

    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}