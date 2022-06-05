package com.example.appportfolio.ui.main.fragments

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.ChatContentsAdpater
import com.example.appportfolio.data.entities.ChatImage
import com.example.appportfolio.data.entities.ChatImages
import com.example.appportfolio.databinding.FragmentChatcontentsBinding
import com.example.appportfolio.other.ImageLoader
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.DownloadProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ChatContentsFragment: Fragment(R.layout.fragment_chatcontents) {

    @Inject
    lateinit var progressDialog:DownloadProgressDialog
    lateinit var binding:FragmentChatcontentsBinding
    private var chatimages:ChatImages? = null
    val PERMISSION_REQUEST_CODE=26
    private lateinit var chatContentsAdpater:ChatContentsAdpater
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatContentsAdpater= ChatContentsAdpater()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentChatcontentsBinding>(inflater,
            R.layout.fragment_chatcontents,container,false)
        (activity as MainActivity).setToolBarVisible("chatContentsFragment")
        chatimages=arguments?.getParcelable("chatimages")!!
        setupRecyclerView()
        chatimages?.chatimages?.let{
            chatContentsAdpater.differ.submitList(it)
        }
        chatContentsAdpater.setOnImageClickListener { position->
            val bundle=Bundle()
            bundle.putParcelable("chatimages",chatimages!!)
            bundle.putInt("position",position)
            (activity as MainActivity).replaceFragment("fullImagesFragment",FullImagesFragment(),bundle)
        }
        return binding.root
    }

    private fun setupRecyclerView()=binding.RvImages.apply {
        adapter=chatContentsAdpater
        layoutManager= GridLayoutManager(requireContext(),3)
        itemAnimator=null
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="사진"
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if(!chatContentsAdpater.activecheck)
        {
            inflater.inflate(R.menu.chatcontent_checkoff, menu)
            val images=chatContentsAdpater.chatimages.map{
                ChatImage(it.date,it.imageUrl,false)
            }
            chatContentsAdpater.differ.submitList(images)

        }
        else
        {
            inflater.inflate(R.menu.chatcontent_checkon, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                parentFragmentManager.popBackStack()
            }
            R.id.activecheck->{
                activity?.invalidateOptionsMenu()
                chatContentsAdpater.updateCheckbox(true)
                chatContentsAdpater.notifyDataSetChanged()

            }
            R.id.inactivecheck->{
                activity?.invalidateOptionsMenu()
                chatContentsAdpater.updateCheckbox(false)
                chatContentsAdpater.notifyDataSetChanged()
            }
            R.id.download->{
                //다운로드하기
                if(
                chatContentsAdpater.chatimages.any{
                    it.isChecked==true
                })
                    downloadimages()
                else
                    Toast.makeText(requireContext(),"사진을 선택해주세요",Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun downloadimages()
    {
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
        }
        else
        {
            val downloadimgs=chatContentsAdpater.chatimages.filter {
                it.isChecked==true
            }
            var bitmaps:List<Bitmap> = listOf()
            progressDialog.show()
            CoroutineScope(Dispatchers.Main).launch{
                for(i in downloadimgs)
                {
                    val bitmap = withContext(Dispatchers.IO){
                        ImageLoader.loadImage(i.imageUrl)
                    }
                    bitmap?.let{
                        bitmaps+=it
                    }
                }
                var error=false
                for(bitmap in bitmaps)
                {
                    if(!SocialApplication.imageExternalSave(
                            requireContext(),
                            bitmap,
                            requireContext().getString(R.string.app_name)
                        )
                    ){
                        error=true
                        break
                    }

                }
                if(error)
                    Toast.makeText(requireContext(), "그림 저장을 실패하였습니다", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(activity, "그림이 갤러리에 저장되었습니다", Toast.LENGTH_SHORT).show()

                progressDialog.dismiss()
                activity?.invalidateOptionsMenu()
                chatContentsAdpater.updateCheckbox(false)
                chatContentsAdpater.notifyDataSetChanged()
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}