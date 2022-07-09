package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.BlockAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.Block
import com.example.appportfolio.databinding.FragmentBlockBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.BlockViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BlockFragment: Fragment(R.layout.fragment_block) {
    lateinit var binding: FragmentBlockBinding
    @Inject
    lateinit var blockAdapter: BlockAdapter
    @Inject
    lateinit var preferences: UserPreferences
    @Inject
    lateinit var loadingDialog:LoadingDialog
    lateinit var vmAuth: AuthViewModel
    lateinit var api: MainApi
    private var curBlock:Block?=null
    private val vmBlock:BlockViewModel by viewModels()
    private var snackbar: Snackbar?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_block,container,false)
        (activity as MainActivity).setToolBarVisible("blockFragment")
        activity?.run{
            vmAuth= ViewModelProvider(this)[AuthViewModel::class.java]
        }

        blockAdapter.setOnItemClickListener {

            showdelete(it)
        }
        setupRecyclerView()
        api= RemoteDataSource().buildApi(MainApi::class.java,
            runBlocking { preferences.authToken.first() })
        vmBlock.getBlocks(api)
        subscribeToObserver()

        return binding.root
    }
    private fun setupRecyclerView()=binding.rvBlock.apply{
        adapter=blockAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
    }
    private fun showdelete(block:Block)
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="차단을 해제하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            curBlock=block
            vmBlock.deleteBlock(block.userid,block.blockeduserid,api)

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
        (activity as MainActivity).binding.title.text="차단유저 관리"
        super.onResume()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun subscribeToObserver()
    {
        vmBlock.getBlockResponse.observe(viewLifecycleOwner, Event.EventObserver(

            onLoading={
              loadingDialog.show()
            },
            onError={
                loadingDialog.dismiss()
                val error:String = if(!(activity as MainActivity).isConnected!!)
                    requireContext().getString(R.string.networkdisdconnected)
                else
                    it
                snackbar=snackbar("$error\n잠시후에 다시 시도해주세요",true,"다시시도"){
                    vmBlock.getBlocks(api)
                }
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==400)
                {
                    snackbar("서버 오류 발생")
                }
                else
                {
                    blockAdapter.submitList(it.blocks)
                }
            }
        })
        vmBlock.deleteBlockResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onLoading={
              loadingDialog.show()
            },
            onError = {
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it,
                )
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==400)
                {
                    snackbar("서버 오류 발생")
                }
                else{
                    val templist= blockAdapter.currentList.toList().toMutableList()
                    templist-=curBlock
                    blockAdapter.submitList(templist)
                }
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        snackbar?.dismiss()
        (activity as MainActivity).setupTopBottom()
    }

}