package com.example.appportfolio.ui.auth.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.databinding.FragmentSignupBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.auth.activity.AuthActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment:Fragment(R.layout.fragment_signup){
    lateinit var binding:FragmentSignupBinding
    lateinit var api: AuthApi
    private lateinit var viewModel: AuthViewModel
    var code:String?=null
    @Inject
    lateinit var loadingDialog: LoadingDialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentSignupBinding>(inflater,
            R.layout.fragment_signup,container,false)
        api= RemoteDataSource().buildApi(AuthApi::class.java)
        viewModel= ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        binding.goback.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.etEmail.addTextChangedListener { editable->
            if(binding.etEmail.text.toString().trim().isEmpty()){
                binding.tilEmail.apply {
                    isErrorEnabled=false
                    error=null
                }
                inactiveSignup()
            }
            else{
                if(!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()){
                    binding.tilEmail.apply{
                        isErrorEnabled=true
                        error="올바른 이메일 주소를 입력해주세요"
                    }
                    inactiveSignup()
                }
                else{
                    binding.tilEmail.apply {
                        isErrorEnabled=false
                        error=null
                    }
                    if(binding.tvCount.text.toString()!="0:00"&&binding.etAuth.text.toString().trim().isNotEmpty()&&binding.etPassword.text.toString().trim().isNotEmpty()
                        &&binding.etReapeatPassword.text.toString().trim().isNotEmpty()&&binding.tilPassword.error==null&&binding.tilReapeatPassword.error==null)
                        activeSignup()
                    //if(binding.etPassword.text.toString().trim().isNotEmpty())
                      //  activebutton()//이부분 모든조건 확인후로 해야함
                }
            }
        }

        binding.etAuth.addTextChangedListener { editable ->
            editable?.let{
                if(binding.tvCount.text.toString()!="0:00"&&binding.etAuth.text.toString().trim().isNotEmpty()&&binding.etEmail.text.toString().trim().isNotEmpty()&&binding.tilEmail.error==null&&
                    binding.etPassword.text.toString().trim().isNotEmpty() &&binding.etReapeatPassword.text.toString().trim().isNotEmpty()&&
                    binding.tilPassword.error==null&&binding.tilReapeatPassword.error==null){
                    activeSignup()
                }
                else
                    inactiveSignup()
            }
        }
        binding.etPhone.addTextChangedListener { editable->
            editable?.let{
                if(binding.tvCount.text.toString()=="0:00")
                { //인증번호 받기전 또는 인증시간 초과된후
                    if(binding.etPhone.text.toString().trim().isEmpty()){
                        binding.tilPhone.apply {
                            isErrorEnabled=false
                            error=null
                        }
                        inactiveVerify()
                    }
                    else{
                        if(Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$",binding.etPhone.text.toString()))
                        {
                            binding.tilPhone.apply{
                                isErrorEnabled=false
                                error=null
                            }
                            activeVerify()
                        }
                        else
                        {
                            inactiveVerify()
                            binding.tilPhone.apply{
                                isErrorEnabled=true
                                error="휴대폰번호를 정확히 입력해주세요"
                            }
                        }
                    }
                }

            }

        }
        binding.etPassword.addTextChangedListener { editable->
            editable?.let{
                if(binding.etPassword.text.toString().trim().isEmpty()){
                    inactiveSignup()
                    binding.tilPassword.apply {
                        isErrorEnabled=false
                        error=null
                    }
                    if(binding.etReapeatPassword.text.toString().trim().isNotEmpty())
                        binding.tilReapeatPassword.apply {
                            isErrorEnabled=true
                            error="비밀번호가 일치하지 않습니다"
                        }
                } 
                else{
                    if(binding.etReapeatPassword.text.toString()!=binding.etPassword.text.toString()&&binding.etReapeatPassword.text.toString().trim().isNotEmpty()){
                        binding.tilReapeatPassword.apply{
                            isErrorEnabled=true
                            error="비밀번호가 일치하지 않습니다"
                        }
                        inactiveSignup()
                    }
                    else
                        binding.tilReapeatPassword.apply{
                            isErrorEnabled=false
                            error=null
                        }
                        binding.tilPassword.apply {
                            if(binding.etPassword.text.toString().length<6){
                                isErrorEnabled=true
                                error="6자리 이상의 비밀번호를 입력해주세요"
                                inactiveSignup()
                            }
                            else{
                                isErrorEnabled=false
                                error=null
                                if(binding.etReapeatPassword.text.toString()==binding.etPassword.text.toString()&&binding.etAuth.text.toString().trim().isNotEmpty()&&
                                        binding.tvCount.text.toString()!="0:00"&&binding.etEmail.text.toString().trim().isNotEmpty()&&binding.tilEmail.error==null)
                                    activeSignup()
                                else
                                    inactiveSignup()
                            }

                        }
                }
            }
        }

        binding.etReapeatPassword.addTextChangedListener { editable->
            if(binding.etReapeatPassword.text.toString().trim().isEmpty()){
                binding.tilReapeatPassword.apply{
                    isErrorEnabled=false
                    error=null
                }
                inactiveSignup()
            }

            else{
                if(binding.etReapeatPassword.text.toString()==binding.etPassword.text.toString())
                    binding.tilReapeatPassword.apply{
                        isErrorEnabled=false
                        error=null
                        if(binding.tilPassword.error==null&&binding.etAuth.text.toString().trim().isNotEmpty()&&binding.etEmail.text.toString().trim().isNotEmpty()&&binding.tilEmail.error==null
                            &&binding.tvCount.text.toString()!="0:00")
                            activeSignup()
                        else
                            inactiveSignup()
                    }
                else
                    binding.tilReapeatPassword.apply{
                        isErrorEnabled=true
                        error="비밀번호가 일치하지 않습니다"
                        inactiveSignup()
                    }
            }

        }
        binding.btnPhone.onSingleClick {
            viewModel.requestVerify(binding.etPhone.text.toString(),api)
        }
        binding.signup.onSingleClick {
            if(binding.etPhone.text.toString().isNotEmpty()&&Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$",binding.etPhone.text.toString())){
                viewModel.register(binding.etEmail.text.toString(),binding.etPassword.text.toString(),binding.etAuth.text.toString(),binding.etPhone.text.toString(),api)
            }
            else{
                showAlert("적절한 전화번호를 입력해주세요")
            }

        }
        subscribeToObserver()
        return binding.root
    }
    private fun inactiveSignup()
    {
        val color = ContextCompat.getColor(requireContext(), R.color.inactive)
        binding.signup.isClickable=false
        binding.signup.setBackgroundColor(color)
    }
    private fun activeSignup()
    {
        val color = ContextCompat.getColor(requireContext(), R.color.skinfore)
        binding.signup.isClickable=true
        binding.signup.setBackgroundColor(color)
    }
    private fun activeVerify()
    {
        val color = ContextCompat.getColor(requireContext(), R.color.skinfore)
        binding.btnPhone.isClickable=true
        binding.btnPhone.setBackgroundColor(color)
    }
    private fun inactiveVerify()
    {
        val color = ContextCompat.getColor(requireContext(), R.color.inactive)
        binding.btnPhone.isClickable=false
        binding.btnPhone.setBackgroundColor(color)
    }
    private fun subscribeToObserver()
    {
        viewModel.verifyResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as AuthActivity).isConnected!!,
                    it
                )
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            if(it.resultCode==200)
            {//인증문자 받고나서
                inactiveVerify()
                viewModel.timerStart()
                binding.tvCount.visibility=View.VISIBLE
            }
        })
        viewModel.timerString.observe(viewLifecycleOwner){
            binding.tvCount.text=it
            if(it.equals("0:00"))
            {
                viewModel.timerStop()
                if(binding.etPhone.text.toString().trim().isNotEmpty()&&Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$",binding.etPhone.text.toString()))
                    activeVerify()
                //snackbar(requireContext().getString(R.string.verify_excess))
                inactiveSignup()
                showAlert(requireContext().getString(R.string.verify_excess))
                binding.tvCount.visibility=View.INVISIBLE
            }
            binding.tvCount.text=it
        }
        viewModel.registerResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError = {
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as AuthActivity).isConnected!!,
                    it
                )
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            when(it.resultCode){
                500->{
                    showAlert("이미 사용중인 계정입니다")
                }
                400->{
                    showAlert("인증번호가 유효하지 않습니다")
                }
                else->{
                    viewModel.timerStop()
                    showAlert("회원가입에 성공했습니다\n 가입된 계정으로 로그인해주세요"){
                        findNavController().popBackStack()
                    }
                }
            }
            loadingDialog.dismiss()
        })
    }
    private fun showAlert(text:String,action:(()->Unit)?=null)
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text=text
        cancel.visibility=View.GONE
        positive.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            action?.let{ act->
                act()
            }
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
}