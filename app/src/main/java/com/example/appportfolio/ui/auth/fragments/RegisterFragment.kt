package com.example.appportfolio.ui.auth.fragments

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.*
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.databinding.FragmentRegisterBinding
import com.example.appportfolio.other.Event
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class RegisterFragment: Fragment(R.layout.fragment_register) {
    lateinit var inputMethodManager: InputMethodManager
    private lateinit var viewModel: AuthViewModel
    lateinit var binding: FragmentRegisterBinding
    lateinit var api: AuthApi
    var code:String?=null
    var isverified:Boolean=false
    var emailchecked=false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentRegisterBinding>(inflater,
            R.layout.fragment_register,container,false)
        api= RemoteDataSource().buildApi(AuthApi::class.java)
        binding.back.setOnClickListener {
          toLogin()
        }
        viewModel=ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        binding.btnRegister.setOnClickListener {
            if(binding.etPassword.text.toString().equals("")||binding.etReapeatPassword.text.toString().equals(""))
            {
                snackbar("비밀번호를 입력해주세요")
            }
            else if(!binding.etPassword.text.toString().equals(binding.etReapeatPassword.text.toString()))
            {
                snackbar(requireContext().getString(R.string.error_incorrectly_repeated_password))

            }
            else if(binding.etPassword.text.toString().length<8)
            {
                snackbar(requireContext().getString(R.string.error_password_too_short))
            }
            else if(!isverified)
            {
                snackbar("휴대폰 번호 인증을 진행해주세요")
            }
            else if(!emailchecked)
            {
                snackbar("이메일 중복체크를 진행해주세요")
            }
            else {
                viewModel.register(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString(),
                    api
                )
            }
        }
        binding.btnEmail.setOnClickListener {
            hideKeyboard()
            if(!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches())
            {
                //snackbar("이메일 형식이 올바르지 않습니다.")
                binding.tilEmail.apply{
                    isHelperTextEnabled=false
                    helperText=null
                    isErrorEnabled=true
                    error="이메일 형식이 올바르지 않습니다."
                }
            }
            else
            {
                //api보내기
                viewModel.requestEmail(binding.etEmail.text.toString(),api)
            }


        }
        binding.btnPhone.setOnClickListener {
            hideKeyboard()
            if(Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$",binding.etPhone.text.toString()))
            {
                viewModel.requestVerify(binding.etPhone.text.toString(),api)
                binding.tilPhone.apply{
                    isHelperTextEnabled=false
                    helperText=null
                    isErrorEnabled=false
                    error=null
                }
            }
            else
            {
                binding.tilPhone.apply{
                    isHelperTextEnabled=false
                    helperText=null
                    isErrorEnabled=true
                    error="휴대폰번호를 정확히 입력해주세요"
                }
            }

        }
        binding.btnAuth.setOnClickListener {
            hideKeyboard()
            if(!binding.etAuth.text.toString().isNullOrEmpty())
                viewModel.verifycode(binding.etPhone.text.toString(),binding.etAuth.text.toString(),api)
            else
                snackbar("인증번호를 입력해주세요")

        }
        binding.etPhone.addTextChangedListener {text: Editable? ->
            text?.let {
                if(isverified==true)
                {
                    viewModel.setverified(false)
                    binding.imgCheck.visibility=View.INVISIBLE
                    binding.btnPhone.visibility=View.VISIBLE
                }

            }
            binding.tilPhone.apply{
                isErrorEnabled=false
                error=null
                isHelperTextEnabled=true
                helperText="휴대폰 번호 인증을 해주세요"
            }

        }
        binding.etEmail.addTextChangedListener{text: Editable? ->
            text?.let {
                if(emailchecked==true)
                {
                    viewModel.setemailChecked(false)
                }
            }
            binding.tilEmail.apply{
                isErrorEnabled=false
                error=null
                isHelperTextEnabled=true
                helperText=context.getString(R.string.nickname_guide)
            }
        }
        subsribeToObserver()
        return binding.root
    }

    private fun toLogin()
    {
        if(findNavController().previousBackStackEntry!=null){
            findNavController().popBackStack()
        }else{
            findNavController().navigate(
                RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            )
        }
    }
    private fun subsribeToObserver(){
        viewModel.emailChecked.observe(viewLifecycleOwner){
            emailchecked=it
        }
        viewModel.codeResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            },
            onLoading={
            }
        ){
            if(it.resultCode==200)
            {
                binding.tilAuth.visibility=View.INVISIBLE
                binding.btnAuth.visibility=View.INVISIBLE
                binding.tvCount.visibility=View.INVISIBLE
                binding.imgCheck.visibility=View.VISIBLE
                viewModel.timerStop()
                viewModel.setverified(true)
            }
            else
            {

                snackbar("인증번호가 틀렸습니다")
            }
        })
        viewModel.verifyResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            },
            onLoading={
            }
        ){
            if(it.resultCode==200)
            {//인증문자 받고나서
                binding.tilAuth.visibility=View.VISIBLE
                binding.btnAuth.visibility=View.VISIBLE
                viewModel.timerStart()
                binding.tvCount.visibility=View.VISIBLE
                binding.btnPhone.visibility=View.INVISIBLE
            }
            else
            {
                snackbar("죄송합니다 서버 오류가 발생했습니다.")
            }
        })
        viewModel.verified.observe(viewLifecycleOwner){
            isverified=it!!
        }
        viewModel.timerString.observe(viewLifecycleOwner){
            binding.tvCount.text=it
            if(it.equals("0:00"))
            {
                viewModel.timerStop()
                binding.tilAuth.visibility=View.INVISIBLE
                binding.btnAuth.visibility=View.INVISIBLE
                snackbar(requireContext().getString(R.string.verify_excess))
                binding.tvCount.visibility=View.INVISIBLE
                binding.btnPhone.visibility=View.VISIBLE
            }
            binding.tvCount.text=it
        }
        viewModel.verifyCode.observe(viewLifecycleOwner){
            code=it
            viewModel.timerStart()
            binding.tvCount.visibility=View.VISIBLE

        }
        viewModel.emailVerifyResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                binding.registerProgressBar.isVisible=false
                snackbar(it)
            },
            onLoading={
                binding.registerProgressBar.isVisible=true
            }
        ){
            binding.registerProgressBar.isVisible=false
            if(it.resultCode==100)
            {//이미존재하는 이메일일경우
                        binding.tilEmail.apply{
                            isHelperTextEnabled=false
                            helperText=null
                            isErrorEnabled=true
                            error="이미 등록된 이메일입니다"
                        }
            }
            else{

                viewModel.setemailChecked(true)
                binding.tilEmail.apply{
                    isErrorEnabled=false
                    error=null
                    isHelperTextEnabled=true
                    helperText="사용가능한 이메일입니다"
                }

            }
        })
        viewModel.registerResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError = {
                binding.registerProgressBar.isVisible=false
                snackbar(it)
            },
            onLoading={
                binding.registerProgressBar.isVisible=true
            }
        ){
            binding.registerProgressBar.isVisible=false
            snackbar(it!!)
            if(it.equals("회원가입에 성공했습니다."))
            {
                toLogin()
            }
        })
    }
    fun hideKeyboard() {
        if (::inputMethodManager.isInitialized.not()) {
            inputMethodManager=activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        }
        inputMethodManager.hideSoftInputFromWindow(binding.etEmail.windowToken, 0)
    }
}