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
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.SocialApplication.Companion.showAlert
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
    ): View {
        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_signup,container,false)
        api= RemoteDataSource().buildApi(AuthApi::class.java)
        viewModel= ViewModelProvider(requireActivity())[AuthViewModel::class.java]
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
                        error="????????? ????????? ????????? ??????????????????"
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
                      //  activebutton()//????????? ???????????? ???????????? ?????????
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
                { //???????????? ????????? ?????? ???????????? ????????????
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
                                error="?????????????????? ????????? ??????????????????"
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
                            error="??????????????? ???????????? ????????????"
                        }
                } 
                else{
                    binding.tilPassword.apply{
                        if(binding.etPassword.text.toString().length<6){
                            isErrorEnabled=true
                            error="6?????? ????????? ??????????????? ??????????????????"
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
                    binding.tilReapeatPassword.apply{
                        if(binding.etReapeatPassword.text.toString()!=binding.etPassword.text.toString()&&binding.etReapeatPassword.text.toString().trim().isNotEmpty()){
                            inactiveSignup()
                            isErrorEnabled=true
                            error="??????????????? ???????????? ????????????"
                        }
                        else{
                            isErrorEnabled=false
                            error=null
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
                        error="??????????????? ???????????? ????????????"
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
                showAlert(requireContext(),"????????? ??????????????? ??????????????????")
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
            {//???????????? ????????????
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
                showAlert(requireContext(),requireContext().getString(R.string.verify_excess))
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
                    showAlert(requireContext(),"?????? ???????????? ???????????????")
                }
                400->{
                    showAlert(requireContext(),"??????????????? ???????????? ????????????")
                }
                else->{
                    viewModel.timerStop()
                    showAlert(requireContext(),"??????????????? ??????????????????\n ????????? ???????????? ?????????????????????"){
                        findNavController().popBackStack()
                    }
                }
            }
            loadingDialog.dismiss()
        })
    }

}