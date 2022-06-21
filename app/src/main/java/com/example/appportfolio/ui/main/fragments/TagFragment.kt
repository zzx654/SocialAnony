package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.TagAdapter
import com.example.appportfolio.adapters.TextHeaderAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.SignManager
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.TagResult
import com.example.appportfolio.databinding.FragmentTagBinding
import com.example.appportfolio.other.Constants

import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.TagViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class TagFragment: Fragment(R.layout.fragment_tag) {

    @Inject
    lateinit var signManager: SignManager
    @Inject
    lateinit var loadingDialog: LoadingDialog
    private lateinit var vmAuth: AuthViewModel
    private val vmTag:TagViewModel by viewModels()
    lateinit var binding:FragmentTagBinding
    lateinit var mainapi: MainApi
    lateinit var popularAdapter:TagAdapter
    lateinit var favoriteAdapter:TagAdapter
    lateinit var searchedAdapter:TagAdapter
    lateinit var FavoritePopularAdapter: ConcatAdapter
    lateinit var SearchedAdapter:ConcatAdapter
    lateinit var popularTextAdapter:TextHeaderAdapter
    lateinit var searchedTextAdapter:TextHeaderAdapter
    lateinit var favoriteTextAdapter:TextHeaderAdapter
    private var mRootView:View?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        popularTextAdapter= TextHeaderAdapter()
        favoriteTextAdapter=TextHeaderAdapter()
        searchedTextAdapter=TextHeaderAdapter()
        popularTextAdapter.title="인기태그"
        favoriteTextAdapter.title="즐겨찾기 태그"
        searchedTextAdapter.title="검색된 태그"
        popularAdapter= TagAdapter()
        favoriteAdapter= TagAdapter()
        searchedAdapter= TagAdapter()
        FavoritePopularAdapter= ConcatAdapter(favoriteTextAdapter,favoriteAdapter,popularTextAdapter,popularAdapter)
        SearchedAdapter= ConcatAdapter(searchedTextAdapter,searchedAdapter)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            if(mRootView==null)
            {
                binding= DataBindingUtil.inflate<FragmentTagBinding>(inflater,
                    R.layout.fragment_tag,container,false)
                activity?.run{
                    vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
                }
                var job: Job?=null
                binding.edtTag.addTextChangedListener { editable ->
                    job?.cancel()
                    job = lifecycleScope.launch {
                        delay(Constants.SEARCH_TIME_DELAY)

                        editable?.let {
                            if (!binding.edtTag.text.toString().trim().isEmpty())
                            {
                                if(!it.toString().equals("#")) {
                                    var tag=it.toString()
                                    if(tag.contains("#"))
                                    {
                                        tag.replace("#","")
                                    }
                                    vmTag.getSearchedTag(tag,mainapi)
                                }
                            }
                        }
                        if(editable!!.isEmpty())
                        {
                            binding.rvSearchedTag.visibility=View.GONE
                            binding.containeralert.visibility=View.GONE
                                binding.rvFavoritepopular.visibility=View.VISIBLE
                        }
                    }
                }
                val preferences= UserPreferences(requireContext())
                vmAuth.setAccessToken(runBlocking { preferences.authToken.first() })
                mainapi= RemoteDataSource().buildApi(MainApi::class.java,runBlocking { preferences.authToken.first() })

                popularAdapter.setOnFavoriteClickListener { tagResult ->
                    if(tagResult.isLiked==1)
                        showToggleDialog(tagResult.tagname,tagResult.count!!,tagResult.isLiked!!)
                    else
                        vmTag.toggleLikeTag(tagResult.tagname,tagResult.count!!,tagResult.isLiked!!,mainapi)

                }
                popularAdapter.setOnTagClickListener { tagResult ->
                    val bundle=Bundle()
                    bundle.putString("tag",tagResult.tagname)
                    (activity as MainActivity).replaceFragment("tagPostsFragment",TagPostsFragment(),bundle)
                }
                favoriteAdapter.setOnFavoriteClickListener { tagResult ->
                    showToggleDialog(tagResult.tagname,0,1)//삭제하게끔 1로설정
                }
                favoriteAdapter.setOnTagClickListener { tagResult ->
                    val bundle=Bundle()
                    bundle.putString("tag",tagResult.tagname)
                    (activity as MainActivity).replaceFragment("tagPostsFragment",TagPostsFragment(),bundle)
                }
                searchedAdapter.setOnFavoriteClickListener { tagResult ->
                    if(tagResult.isLiked==1)
                        showToggleDialog(tagResult.tagname,tagResult.count!!,tagResult.isLiked!!)
                    else
                        vmTag.toggleLikeTag(tagResult.tagname,tagResult.count!!,tagResult.isLiked!!,mainapi)
                }
                searchedAdapter.setOnTagClickListener { tagResult ->
                    val bundle=Bundle()
                    bundle.putString("tag",tagResult.tagname)
                    (activity as MainActivity).replaceFragment("tagPostsFragment",TagPostsFragment(),bundle)
                }
                mRootView=binding.root
                setupRecyclerView()
            }

        subscribeToObserver()
        vmTag.getFavoriteTag(mainapi)
        vmTag.getPopularTag(mainapi)


        return mRootView

    }
    fun showToggleDialog(tagname:String,count:Int,isLiked:Int)
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
            vmTag.toggleLikeTag(tagname,count,isLiked,mainapi)
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun setupRecyclerView(){
        binding.rvSearchedTag.apply {
            adapter = SearchedAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
        }
        binding.rvFavoritepopular.apply {
            adapter=FavoritePopularAdapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
        }
    }
    private fun subscribeToObserver()
    {
        vmTag.toggleTagResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onLoading={
                loadingDialog.show()
            },
            onError = {
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode!!) {
                if (favoriteAdapter.currentList.isEmpty()) {
                    favoriteTextAdapter.tvContainerVis=false
                    binding.rvFavoritepopular.findViewHolderForAdapterPosition(0)?.let{
                        (it as TextHeaderAdapter.TextViewHolder).binding.guideContainer.visibility=View.GONE
                    }
                }
                if (favoriteAdapter.currentList.size == 1) {
                    if (it.isLiked == 1) {
                        favoriteTextAdapter.guideText="태그를 추가해주세요"
                        favoriteTextAdapter.tvContainerVis=true
                        binding.rvFavoritepopular.findViewHolderForAdapterPosition(0)?.let{
                            (it as TextHeaderAdapter.TextViewHolder).binding.tvguide.text=favoriteTextAdapter.guideText
                            it.binding.guideContainer.visibility=View.VISIBLE
                        }
                    }
                }
                var templist=favoriteAdapter.currentList.toList()
                if (it.isLiked == 0) {
                    templist += TagResult(null,it.tagname, null, null)//즐겨찾기목록에 추가
                } else {
                    templist -= TagResult(null,it.tagname, null, null)
                    //즐겨찾기에서 삭제
                }
                favoriteAdapter.submitList(templist)
                for (i in searchedAdapter.currentList.indices) {
                    if (it.tagname == searchedAdapter.currentList[i].tagname) {
                        searchedAdapter.currentList[i].apply {
                            if (this.isLiked == 0) {
                                this.isLiked = 1
                            } else {
                                this.isLiked = 0
                            }
                        }
                        searchedAdapter.notifyItemChanged(i)
                        break
                    }
                }
                for (i in popularAdapter.currentList.indices) {//인기태그목록 순회
                    if (it.tagname == popularAdapter.currentList[i].tagname) {//인기태그목록의 태그명과 응답받은 태그명이 같으면
                        popularAdapter.currentList[i].apply {
                            //해당인덱스 의 즐겨찾기 표시 반대로 변경
                            if (this.isLiked == 0) {
                                this.isLiked = 1
                            } else {
                                this.isLiked = 0
                            }
                        }
                        popularAdapter.notifyItemChanged(i)
                        break
                    }
                }
            }

        })
        vmTag.favoritetagResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                      },
            onError = {
                //snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 100) {
                    favoriteTextAdapter.tvContainerVis=true
                    favoriteTextAdapter.guideText="태그를 추가해주세요"
                    binding.rvFavoritepopular.findViewHolderForAdapterPosition(0)?.let{
                        (it as TextHeaderAdapter.TextViewHolder).binding.tvguide.text=favoriteTextAdapter.guideText
                        it.binding.guideContainer.visibility=View.VISIBLE
                    }
                } else {
                    favoriteTextAdapter.tvContainerVis=false
                    binding.rvFavoritepopular.findViewHolderForAdapterPosition(0)?.let {
                        (it as TextHeaderAdapter.TextViewHolder).binding.guideContainer.visibility =
                            View.GONE
                    }
                    favoriteAdapter.submitList(it.tags)
                }

            }
        })
        vmTag.populartagResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 100) {
                    //binding.linearpopular.visibility = View.GONE
                    //binding.rvpoptags.visibility = View.GONE
                    popularTextAdapter.apply {
                        guideText="인기태그가 없습니다"
                        tvContainerVis=true
                    }
                    popularTextAdapter.notifyDataSetChanged()
                } else {
                    popularTextAdapter.apply {
                        tvContainerVis=false
                    }
                    popularTextAdapter.notifyDataSetChanged()
                    //binding.linearpopular.visibility = View.VISIBLE
                    //binding.rvpoptags.visibility = View.VISIBLE
                    popularAdapter.submitList(it.tags)
                }
            }
        })
        vmTag.tagSearchResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                binding.rvSearchedTag.visibility=View.VISIBLE
                binding.rvFavoritepopular.visibility=View.GONE
                if (it.resultCode == 200) {
                    binding.containeralert.visibility=View.GONE
                    searchedAdapter.submitList(it.tags)
                } else {
                    searchedAdapter.submitList(it.tags)
                    binding.containeralert.visibility=View.VISIBLE
                }
            }
        })
    }
}