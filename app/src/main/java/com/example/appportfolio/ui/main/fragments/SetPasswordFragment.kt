package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.SocialApplication.Companion.showAlert
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentChangepwBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class SetPasswordFragment:Fragment(R.layout.fragment_changepw) {
    private lateinit var viewModel: AuthViewModel
    lateinit var api: AuthApi
    lateinit var binding: FragmentChangepwBinding
    @Inject
    lateinit var preferences: UserPreferences
    @Inject
    lateinit var loadingDialog:LoadingDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_changepw,container,false)
        (activity as MainActivity).setToolBarVisible("setPasswordFragment")
        api= RemoteDataSource().buildApi(AuthApi::class.java,
            runBlocking { preferences.authToken.first() })
        viewModel= ViewModelProvider(requireActivity())[AuthViewModel::class.java]
        //activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        binding.edtcurpw.addTextChangedListener { editable->
            editable?.let{
                if(binding.edtcurpw.text.toString().trim().isEmpty())
                    inactivebutton()
                else{
                    if(binding.edtnewrepeatpw.text.toString().trim().isNotEmpty()
                        &&binding.edtnewpw.text.toString().trim().isNotEmpty()&&
                        binding.edtnewpw.text.toString()==binding.edtnewrepeatpw.text.toString()&&
                        binding.tilnewRepeatpw.error==null&&binding.tilnewpw.error==null)
                    activebutton()
                }

            }
        }
        binding.edtnewpw.addTextChangedListener { editable->
            editable?.let {
                if (binding.edtnewpw.text.toString().trim().isEmpty()) {
                    inactivebutton()
                    binding.tilnewpw.apply {
                        isErrorEnabled = false
                        error = null
                    }
                    if (binding.edtnewrepeatpw.text.toString().trim().isNotEmpty())
                        binding.tilnewRepeatpw.apply {
                            isErrorEnabled = true
                            error = "비밀번호가 일치하지 않습니다"

                        }
                }
                else{
                    binding.tilnewpw.apply {
                        if(binding.edtnewpw.text.toString().length<6){
                            isErrorEnabled=true
                            error="6자리 이상의 비밀번호를 입력해주세요"
                            inactivebutton()
                        }
                        else{
                            isErrorEnabled=false
                            error=null
                            if(binding.edtnewrepeatpw.text.toString()==binding.edtnewpw.text.toString()&&binding.edtcurpw.text.toString().trim().isNotEmpty())
                                activebutton()//6자리 이상의 비밀번호를 입력했고 새비밀번호와 다시입력된 비밀번호가 일치하고 현재비밀번호도 입력된경우
                            else
                                inactivebutton()
                        }

                    }
                    binding.tilnewRepeatpw.apply{
                        if(binding.edtnewrepeatpw.text.toString()!=binding.edtnewpw.text.toString()&&binding.edtnewrepeatpw.text.toString().trim().isNotEmpty()){
                            isErrorEnabled=true
                            error="비밀번호가 일치하지 않습니다"
                            inactivebutton()
                        }
                        else{
                            isErrorEnabled=false
                            error=null
                        }
                    }
                }
            }
        }
        binding.edtnewrepeatpw.addTextChangedListener { editable->
            editable?.let{
                if(binding.edtnewrepeatpw.text.toString().trim().isEmpty()){
                    binding.tilnewRepeatpw.apply{
                        isErrorEnabled=false
                        error=null
                    }
                    inactivebutton()
                }
                else{
                    if(binding.edtnewrepeatpw.text.toString()==binding.edtnewpw.text.toString())
                        binding.tilnewRepeatpw.apply{
                            isErrorEnabled=false
                            error=null
                            if(binding.edtcurpw.text.toString().trim().isNotEmpty()&&binding.tilnewpw.error==null)
                                activebutton()
                            else
                                inactivebutton()
                        }
                    else
                        binding.tilnewRepeatpw.apply{
                            isErrorEnabled=true
                            error="비밀번호가 일치하지 않습니다"
                            inactivebutton()
                        }
                }
            }
        }
        binding.complete.setOnClickListener {
                viewModel.changepassword(binding.edtcurpw.text.toString(),binding.edtnewpw.text.toString(),api)
        }
        binding.btnback.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        subscribeToObserver()
        return binding.root
    }
    private fun subscribeToObserver()
    {
        viewModel.changepasswordResponse.observe(viewLifecycleOwner, Event.EventObserver(

            onError={
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                when (it.resultCode) {
                    200 -> {
                        showAlert(requireContext(),"비밀번호가 변경되었습니다"){
                            parentFragmentManager.popBackStack()
                        }
                    }
                    300 -> {
                        showAlert(requireContext(),"입력된 비밀번호가 현재비밀번호와 같습니다")
                    }
                    else -> {
                        showAlert(requireContext(),"현재 비밀번호가 틀렸습니다")

                    }
                }
            }

        })
    }
    private fun activebutton()
    {
        val color = ContextCompat.getColor(requireContext(), R.color.skinfore)
        binding.complete.isClickable=true
        binding.complete.setBackgroundColor(color)
    }
    private fun inactivebutton()
    {
        val color = ContextCompat.getColor(requireContext(), R.color.inactive)
        binding.complete.isClickable=false
        binding.complete.setBackgroundColor(color)
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}