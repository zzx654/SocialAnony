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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.TagAdapter
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
    private lateinit var vmAuth: AuthViewModel
    private val vmTag:TagViewModel by viewModels()
    lateinit var binding:FragmentTagBinding
    lateinit var mainapi: MainApi
    lateinit var popularAdapter:TagAdapter
    lateinit var favoriteAdapter:TagAdapter
    lateinit var searchedAdapter:TagAdapter
    private var mRootView:View?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        popularAdapter= TagAdapter()
        favoriteAdapter= TagAdapter()
        searchedAdapter= TagAdapter()
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
                            binding.linearsearched.visibility=View.GONE
                            binding.rvsearchedtags.visibility=View.GONE
                            if(!popularAdapter.tags.isEmpty())
                            {
                                binding.linearpopular.visibility=View.VISIBLE
                                binding.rvpoptags.visibility=View.VISIBLE
                            }
                            if(!favoriteAdapter.tags.isEmpty())
                            {
                                binding.linearlike.visibility=View.VISIBLE

                                binding.rvfavtags.visibility=View.VISIBLE
                            }
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
                if(popularAdapter.differ.currentList.size>0)
                    showpopular()
                if(favoriteAdapter.differ.currentList.size>0)
                    showfavorite()
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
    private fun showfavorite()
    {
        binding.linearlike.visibility = View.VISIBLE
        binding.rvfavtags.visibility = View.VISIBLE
    }
    private fun showpopular()
    {
        binding.linearpopular.visibility = View.VISIBLE
        binding.rvpoptags.visibility = View.VISIBLE
    }
    private fun setupRecyclerView(){
        binding.rvfavtags.apply{
                adapter=favoriteAdapter
                layoutManager= LinearLayoutManager(requireContext())
                itemAnimator=null
        }
        binding.rvpoptags.apply {
            adapter=popularAdapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
        }
        binding.rvsearchedtags.apply{
            adapter=searchedAdapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
        }
    }
    private fun subscribeToObserver()
    {
        vmTag.toggleTagResponse.observe(viewLifecycleOwner,Event.EventObserver(

        ){
            handleResponse(requireContext(),it.resultCode!!) {
                if (favoriteAdapter.tags.isEmpty()) {
                    binding.linearlike.visibility = View.VISIBLE
                    binding.rvfavtags.visibility = View.VISIBLE
                }
                if (favoriteAdapter.tags.size == 1) {
                    if (it.isLiked == 1) {
                        binding.linearlike.visibility = View.GONE
                        binding.rvfavtags.visibility = View.GONE
                    }
                }
                if (it.isLiked == 0) {
                    favoriteAdapter.tags += TagResult(null,it.tagname, null, null)//즐겨찾기목록에 추가
                } else {
                    favoriteAdapter.tags -= TagResult(null,it.tagname, null, null)
                    //즐겨찾기에서 삭제
                }
                for (i in searchedAdapter.tags.indices) {
                    if (it.tagname == searchedAdapter.tags[i].tagname) {
                        searchedAdapter.tags[i].apply {
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
                for (i in popularAdapter.tags.indices) {//인기태그목록 순회
                    if (it.tagname == popularAdapter.tags[i].tagname) {//인기태그목록의 태그명과 응답받은 태그명이 같으면
                        popularAdapter.tags[i].apply {
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
                    binding.linearlike.visibility = View.GONE
                    binding.rvfavtags.visibility = View.GONE
                } else {
                    binding.linearlike.visibility = View.VISIBLE
                    binding.rvfavtags.visibility = View.VISIBLE
                    favoriteAdapter.differ.submitList(it.tags)
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
                    binding.linearpopular.visibility = View.GONE
                    binding.rvpoptags.visibility = View.GONE
                } else {
                    binding.linearpopular.visibility = View.VISIBLE
                    binding.rvpoptags.visibility = View.VISIBLE
                    popularAdapter.differ.submitList(it.tags)
                }
            }
        })
        vmTag.tagSearchResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    binding.linearlike.visibility = View.GONE
                    binding.rvfavtags.visibility = View.GONE
                    binding.linearpopular.visibility = View.GONE
                    binding.rvpoptags.visibility = View.GONE
                    binding.linearsearched.visibility = View.VISIBLE
                    binding.rvsearchedtags.visibility = View.VISIBLE
                    searchedAdapter.differ.submitList(it.tags)
                } else {
                    searchedAdapter.differ.submitList(it.tags)
                    binding.linearsearched.visibility = View.GONE
                    binding.rvsearchedtags.visibility = View.GONE
                }
            }
        })
    }
}