package com.example.appportfolio.ui.auth.fragments

import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.SignManager
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentEmailloginBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.auth.activity.AuthActivity
import com.example.appportfolio.ui.auth.activity.FillProfileActivity
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EmailLoginFragment: Fragment(R.layout.fragment_emaillogin) {
    lateinit var binding: FragmentEmailloginBinding
    private lateinit var viewModel: AuthViewModel
    @Inject
    lateinit var signManager: SignManager
    @Inject
    lateinit var loadingDialog: LoadingDialog
    @Inject
    lateinit var userPreferences: UserPreferences
    lateinit var api: AuthApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding= DataBindingUtil.inflate<FragmentEmailloginBinding>(inflater,
            R.layout.fragment_emaillogin,container,false)
        api= RemoteDataSource().buildApi(AuthApi::class.java)
        viewModel= ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        binding.goback.onSingleClick {
            toLoginStart()
        }
        binding.tvRegister.onSingleClick {
            findNavController().navigate(
                EmailLoginFragmentDirections.actionEmailLoginFragmentToSignUpFragment()
            )
        }
        binding.tvfindpassword.setOnClickListener {
            findNavController().navigate(EmailLoginFragmentDirections.actionGlobalFindPwFragment())
        }
        binding.etEmail.addTextChangedListener { editable->
            editable?.let{
                binding.tilPassword.apply {
                    isErrorEnabled=false
                    error=null
                }
                if(binding.etEmail.text.toString().trim().isEmpty()){
                    binding.tilEmail.apply {
                        isErrorEnabled=false
                        error=null
                    }
                }
                else{
                    if(!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()){
                        binding.tilEmail.apply{
                            isErrorEnabled=true
                            error="올바른 이메일 주소를 입력해주세요"
                        }
                        inactivebutton()
                    }
                    else{

                        binding.tilEmail.apply {
                            isErrorEnabled=false
                            error=null
                        }
                        if(binding.etPassword.text.toString().trim().isNotEmpty())
                            activebutton()
                    }
                }
            }

        }
        binding.etPassword.addTextChangedListener { editable->
            editable?.let{
                binding.tilPassword.apply {
                    isErrorEnabled=false
                    error=null
                }
                if(binding.etPassword.text.toString().trim().isEmpty())
                    inactivebutton()
                else{
                    if(binding.etEmail.text.toString().trim().isNotEmpty()&&binding.tilEmail.error==null)
                        activebutton()//이메일이 비어있지않으면서 이메일형식의 오류가 없는경우

                }
            }
        }
        binding.login.setOnClickListener {
            (activity as AuthActivity).fcmToken?.let{ fcmToken->
                viewModel.login(binding.etEmail.text.toString(),binding.etPassword.text.toString(),fcmToken,api)
            }

        }
        subscribeToObserver()
        return binding.root
    }
    private fun subscribeToObserver()
    {
        viewModel.loginResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as AuthActivity).isConnected!!,
                    it
                )
            },
            onLoading = {
                loadingDialog.show()
            }

        ){
            if(it.restoken == ""){
                binding.tilPassword.apply{
                    isErrorEnabled=true
                    error="비밀번호가 일치하지 않습니다"
                }
                //binding.tilPassword.error="비밀번호가 일치하지 않습니다"
                loadingDialog.dismiss()
            }
            else{
                lifecycleScope.launch {
                    userPreferences.saveAuthToken(it.restoken)
                }
                api= RemoteDataSource().buildApi(AuthApi::class.java,it.restoken)
                viewModel.checkProfile(api)
            }
        })

        viewModel.checkProfileResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError = {
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as AuthActivity).isConnected!!,
                    it
                )
            },
        ) {
            loadingDialog.dismiss()
            SocialApplication.handleResponse(requireContext(), it.resultCode) {
                if (it.resultCode == 200) {//프로필 완료된거
                    Intent(requireContext(), MainActivity::class.java).apply {
                        startActivity(this)
                        requireActivity().finish()
                    }
                } else {//프로필완료안된거
                    Intent(requireContext(), FillProfileActivity::class.java).apply {
                        startActivity(this)
                        requireActivity().finish()
                    }
                }
            }

        })
    }
    private fun activebutton()
    {
        val color = ContextCompat.getColor(requireContext(), R.color.skinfore)
        binding.login.isClickable=true
        binding.login.setBackgroundColor(color)
    }
    private fun inactivebutton()
    {
        val color = ContextCompat.getColor(requireContext(), R.color.inactive)
        binding.login.isClickable=false
        binding.login.setBackgroundColor(color)
    }
    private fun toLoginStart() {
        if (findNavController().previousBackStackEntry != null) {
            findNavController().popBackStack()
        } else {
            findNavController().navigate(
                EmailLoginFragmentDirections.actionEmailLoginFragmentToLoginStartFragment()
            )
        }
    }

}