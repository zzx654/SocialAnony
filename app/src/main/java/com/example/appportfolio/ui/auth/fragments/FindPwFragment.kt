package com.example.appportfolio.ui.auth.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.SocialApplication.Companion.showAlert
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.databinding.FragmentFindpasswordBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.auth.activity.AuthActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import javax.inject.Inject

class FindPwFragment: Fragment(R.layout.fragment_findpassword) {
    private lateinit var viewModel: AuthViewModel
    lateinit var api: AuthApi
    lateinit var binding: FragmentFindpasswordBinding
    @Inject
    lateinit var loadingDialog: LoadingDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_findpassword,container,false)
        api= RemoteDataSource().buildApi(AuthApi::class.java)
        viewModel= ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        binding.edtmail.addTextChangedListener { editable ->

            editable?.let {

                if (binding.edtmail.text.toString().trim().isEmpty()) {
                    binding.tilEmail.apply{
                        isErrorEnabled=false
                        error=null
                    }
                    val color = ContextCompat.getColor(requireContext(), R.color.inactive)
                    binding.btnConfirm.isClickable = false
                    binding.btnConfirm.setBackgroundColor(color)
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(binding.edtmail.text.toString()).matches())
                {
                    val color = ContextCompat.getColor(requireContext(), R.color.inactive)
                    binding.btnConfirm.isClickable = false
                    binding.btnConfirm.setBackgroundColor(color)
                    binding.tilEmail.apply{
                        isErrorEnabled=true
                        error="올바른 이메일 주소를 입려해주세요"
                    }

                }
                else{
                    binding.tilEmail.apply{
                        isErrorEnabled=false
                        error=null
                    }
                    val color = ContextCompat.getColor(requireContext(), R.color.skinfore)
                    binding.btnConfirm.isClickable = true
                    binding.btnConfirm.setBackgroundColor(color)
                }
            }
        }
        binding.btnConfirm.setOnClickListener {
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
                loadingDialog.show()
            },
            onError = {
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as AuthActivity).isConnected!!,
                    it
                )
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==200)
                {
                    showAlert(requireContext(),"임시비밀번호가 발급되었습니다 이메일에서 확인해주세요"){
                        findNavController().popBackStack()
                    }
                }
                else
                {
                    showAlert(requireContext(),"등록된 이메일이 아닙니다")
                }
            }

        })
    }
}