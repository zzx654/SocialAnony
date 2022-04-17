package com.example.appportfolio.ui.auth.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.databinding.FragmentFindpasswordBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar

class FindPwFragment: Fragment(R.layout.fragment_findpassword) {
    private lateinit var viewModel: AuthViewModel
    lateinit var api: AuthApi
    lateinit var binding: FragmentFindpasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentFindpasswordBinding>(inflater,
            R.layout.fragment_findpassword,container,false)
        api= RemoteDataSource().buildApi(AuthApi::class.java)
        viewModel= ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        binding.edtmail.addTextChangedListener { editable ->

            editable?.let {
                if (binding.edtmail.text.toString().trim().isEmpty()) {
                    val color = ContextCompat.getColor(requireContext(), R.color.inactive)
                    binding.btnConfirm.isClickable = false
                    binding.btnConfirm.setBackgroundColor(color)
                }
                else
                {
                    val color = ContextCompat.getColor(requireContext(), R.color.skinfore)
                    binding.btnConfirm.isClickable = true
                    binding.btnConfirm.setBackgroundColor(color)
                }
            }
        }
        binding.btnConfirm.setOnClickListener {
            if(!Patterns.EMAIL_ADDRESS.matcher(binding.edtmail.text.toString()).matches())
                snackbar("이메일 형식이 올바르지 않습니다.")
            else
                viewModel.findpassword(binding.edtmail.text.toString(),api)
                //서버에 이메일 보내면서 새비번설정하기
        }
        binding.btnback.setOnClickListener {
            findNavController().popBackStack()
        }
        subscribeToObserver()
        return binding.root


    }
    private fun subscribeToObserver()
    {

        viewModel.findpasswordResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading = {
                binding.progress.visibility=View.VISIBLE
            },
            onError = {
                binding.progress.visibility=View.GONE
                Toast.makeText(requireContext(),it,Toast.LENGTH_SHORT).show()
            }
        ){
            binding.progress.visibility=View.GONE
            if(it.resultCode==200)
            {
                Toast.makeText(requireContext(),"임시비밀번호가 발급되었습니다 이메일에서 확인해주세요.",Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            else
            {
                Toast.makeText(requireContext(),"등록된 이메일이 아닙니다",Toast.LENGTH_SHORT).show()

            }
        })
    }
}