package com.example.appportfolio.ui.main.dialog

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appportfolio.R
import com.example.appportfolio.adapters.VoteOptionAdapter
import com.example.appportfolio.data.entities.Voteoption
import com.example.appportfolio.data.entities.Voteoptions
import com.example.appportfolio.databinding.FragmentSetvoteBinding
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.UploadViewModel

class SetVoteoptionFragment:Fragment(R.layout.fragment_setvote) {
    lateinit var binding: FragmentSetvoteBinding
    private lateinit var voteoptionadapter: VoteOptionAdapter

    private var voteoptions:Voteoptions?=null

    private lateinit var vmUpload:UploadViewModel
    private lateinit var inputMethodManager: InputMethodManager
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_setvote,container,false)
        vmUpload= ViewModelProvider(requireActivity())[UploadViewModel::class.java]
        voteoptions=arguments?.getParcelable("voteoptions")
        (activity as MainActivity).setToolBarVisible("setVoteOptionFragment")
        voteoptionadapter= VoteOptionAdapter()
        setupRecyclerView()
        binding.btnadd.setOnClickListener {
            if(voteoptionadapter.currentList.size==19)
                binding.btnadd.visibility= View.GONE
            addoption()
        }
        if(voteoptions==null)
            initvoteoptions()
        else
            voteoptionadapter.submitList(voteoptions!!.options)

        return binding.root
    }
    private fun initvoteoptions()
    {
        val initiallists=listOf(Voteoption("","1"),Voteoption("","2"),Voteoption("","3"))
        voteoptionadapter.submitList(initiallists)
    }
    private fun addoption()
    {
        val templist= voteoptionadapter.currentList.toList().toMutableList()
        templist+=Voteoption("",(voteoptionadapter.currentList.size+1).toString())
        voteoptionadapter.submitList(templist)
        binding.scrollview.post(Runnable{
            binding.scrollview.fling(0)
            binding.scrollview.fullScroll(NestedScrollView.FOCUS_DOWN)
        })
    }
    private fun setupRecyclerView()=binding.rvvote.apply{
        adapter=voteoptionadapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="투표 만들기"
        super.onResume()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.upload_tools, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                showCancel()
            }
            R.id.complete -> {
                var options:List<Voteoption>?=null
                var isempty=true
                val templist:List<Voteoption> = voteoptionadapter.currentList.toList()
                for(i in templist)
                {
                    if(i.option != "")
                    {
                        isempty=false
                    }
                }
                if(!isempty)
                    options=voteoptionadapter.currentList.toList()
                //findNavController().previousBackStackEntry?.savedStateHandle?.set("return",Voteoptions(options))
                //findNavController().popBackStack()
                vmUpload.setvoteoptions(options)
                parentFragmentManager.popBackStack()

            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showCancel()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="투표 만들기를 그만두시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            parentFragmentManager.popBackStack()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }
    private fun hideKeyboard() {
        if (::inputMethodManager.isInitialized.not()) {
            inputMethodManager=activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        }

        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}