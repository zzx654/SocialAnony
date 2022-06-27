package com.example.appportfolio.ui.main.fragments
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.*
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentTagpostsBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.TagViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class TagPostsFragment: Fragment(R.layout.fragment_tagposts) {
    private lateinit var vmTag: TagViewModel
    lateinit var mainapi: MainApi
    @Inject
    lateinit var preferences: UserPreferences
    @Inject
    lateinit var loadingDialog:LoadingDialog
    val tagname:String?
    get() = arguments?.getString("tag","")
    private lateinit var vmAuth: AuthViewModel
    lateinit var binding: FragmentTagpostsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentTagpostsBinding>(inflater,
            R.layout.fragment_tagposts,container,false)
        (activity as MainActivity).setToolBarVisible("tagPostsFragment")
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        vmTag=ViewModelProvider(requireActivity()).get(TagViewModel::class.java)
        mainapi= RemoteDataSource().buildApi(MainApi::class.java,
            runBlocking { preferences.authToken.first() })
        vmTag.getTagLiked(tagname!!,mainapi)
        binding.vp.apply{
            adapter= TagPagerAdapter(this@TagPostsFragment)
            getChildAt(0).overScrollMode= View.OVER_SCROLL_NEVER
        }
        TabLayoutMediator(binding.tab,binding.vp){ tab,position->
            tab.text=getTabTitle(position)
        }.attach()
        subscribeToObserver()
        return binding.root
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text=tagname
        super.onResume()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if(vmTag.isLiked.value==1)
        {
            inflater.inflate(R.menu.tag_likeon, menu)
        }
        else
        {
            inflater.inflate(R.menu.tag_likeoff, menu)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                goback()
            }
            R.id.tagtoggle -> {
                vmTag.isLiked.value?.let{
                    toggleLike()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun goback()
    {
        parentFragmentManager.popBackStack()
    }
    fun toggleLike()
    {
        if(vmTag.isLiked.value==1)
            showToggleDialog()
        else
            vmTag.toggleLikeTag(tagname!!,0,vmTag.isLiked.value!!,mainapi)
    }
    fun showToggleDialog()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog:LayoutInflater= LayoutInflater.from(requireContext())
        val mView:View=edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="즐겨찾기를 해제하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            vmTag.toggleLikeTag(tagname!!,0,vmTag.isLiked.value!!,mainapi)
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun subscribeToObserver()
    {
        vmTag.isLiked.observe(viewLifecycleOwner){
            activity?.invalidateOptionsMenu()
        }
        vmTag.getisLikedResponse.observe(viewLifecycleOwner,Event.EventObserver(

        ){
            handleResponse(requireContext(),it.resultCode) {
                vmTag.setisLiked(it.value)
            }
        })
        vmTag.toggleTagResponse.observe(viewLifecycleOwner, Event.EventObserver(

            onLoading={
                loadingDialog.show()
            },
            onError={
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it

                )
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode!!) {
                if (it.isLiked == 0) {
                    vmTag.setisLiked(1)
                } else {
                    vmTag.setisLiked(0)
                }
            }
        })
    }
    private fun getTabTitle(position: Int):String?{
        return when(position){
            TAGHOT_INDEX ->requireContext().getString(R.string.hot)
            TAGNEW_INDEX ->requireContext().getString(R.string.newposts)
            else->null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}