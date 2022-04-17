package com.example.appportfolio.ui.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.appportfolio.R
import com.example.appportfolio.databinding.DialogInteractionBinding
import com.example.appportfolio.other.Constants.BLOCK
import com.example.appportfolio.other.Constants.CHAT
import com.example.appportfolio.other.Constants.DELETE
import com.example.appportfolio.other.Constants.REPORT
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InteractionDialog(val isMine:Boolean,val itemClick:(String)->Unit): BottomSheetDialogFragment() {

    //신고,차단,대화요청,삭제
    lateinit var binding: DialogInteractionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<DialogInteractionBinding>(inflater,
            R.layout.dialog_interaction,container,false)
        if(isMine)
        {
            binding.block.visibility=View.GONE
            binding.chat.visibility=View.GONE
            binding.report.visibility=View.GONE
        }
        else{
            binding.delete.visibility=View.GONE
        }
        binding.block.setOnClickListener {
            itemClick(BLOCK)
            dismiss()
        }
        binding.chat.setOnClickListener {
            itemClick(CHAT)
            dismiss()
        }
        binding.delete.setOnClickListener {
            itemClick(DELETE)
            dismiss()
        }
        binding.report.setOnClickListener {
            itemClick(REPORT)
            dismiss()
        }
        return binding.root
    }
    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme
}