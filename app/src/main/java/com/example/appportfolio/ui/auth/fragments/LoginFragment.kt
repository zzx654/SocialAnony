package com.example.appportfolio.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.*
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.SignManager
import com.example.appportfolio.auth.Status
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentLoginBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.ui.auth.activity.AuthCompleteActivity
import com.example.appportfolio.ui.main.activity.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.fragment_login) {
    lateinit var fcmToken:String
    private lateinit var viewModel: AuthViewModel
    lateinit var binding:FragmentLoginBinding
    lateinit var api: AuthApi
    @Inject
    lateinit var signManager: SignManager
    @Inject
    lateinit var userPreferences: UserPreferences
    lateinit var curplatform:String
    lateinit var curaccount:String

    lateinit var mGoogleSignInClient: GoogleSignInClient
    val resultListener=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.resultCode== AppCompatActivity.RESULT_OK)
        {
            val task= GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate<FragmentLoginBinding>(inflater,
            R.layout.fragment_login,container,false)
        viewModel=ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        checklogout()
        binding.register.setOnClickListener {
            if(findNavController().previousBackStackEntry!=null){
                findNavController().popBackStack()
            }else
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                )

        }
        initGoogleSignResource()
        api= RemoteDataSource().buildApi(AuthApi::class.java)
        binding.signGeneral.setOnClickListener {
            viewModel.login(binding.etEmail.text.toString(),binding.etPassword.text.toString(),fcmToken,api)
        }
        val tvgoogle=binding.signGoogle.getChildAt(0) as TextView
        tvgoogle.text="Sign in with Google"

        binding.signGoogle.setOnClickListener {
            resultListener.launch(mGoogleSignInClient.signInIntent)
        }
        binding.tvfindpassword.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionGlobalFindPwFragment())
        }
        getFirebaseToken()
        subscribeToObserver()
        return binding.root
    }
    private fun oginCheck(){

        var applyResult={ status: Status, resultdata1: String?, resultdata2: String? ->
            if(status!= Status.NOTFOUND && status!=Status.ERROR) {//sns로그인이 안되어있는경우
                viewModel.autologin(api)//이메일로 자동로그인시도
            }
        }
        signManager.getCurAccountInfo(applyResult)
    }
    fun checklogout(deletetoken:Boolean=true)
    {
        var applyresult:(Status, String?, String?)->Unit={ status,str1,str2->
        }
        signManager.signout(applyresult)
    }
    private fun initGoogleSignResource()
    {
        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient=GoogleSignIn.getClient(requireContext(),gso)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try{
            val account=
                completedTask.getResult(ApiException::class.java)
        viewModel.signWithSocial("GOOGLE",account.id,fcmToken,api)

        }catch (e: ApiException){
            snackbar(e.message!!)
        }
    }
    private fun subscribeToObserver()
    {
        viewModel.fcmToken.observe(viewLifecycleOwner){
            fcmToken=it
        }

        viewModel.loginResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                binding.loginProgressBar.isVisible=false
                snackbar(it)
            },
            onLoading = {
                binding.loginProgressBar.isVisible=true
            }

        ){
            binding.loginProgressBar.isVisible=false
            if(it.restoken.equals("")){
                snackbar(it.message)
            }
            else{
                lifecycleScope.launch {
                    userPreferences.saveAuthToken(it.restoken)
                }
                api= RemoteDataSource().buildApi(AuthApi::class.java,it.restoken)
                viewModel.checkProfile(api)
            }
        })
        viewModel.SocialSignResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError = {
                      Log.d("socialSignErr",it)
            },
            onLoading = {
            }
        ){

            if(it.restoken.equals("")){
                snackbar(it.message)
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
            },
            onLoading = {
            }
        ) {
            if (it.resultcode == 200) {//프로필 완료된거
                Intent(requireContext(), MainActivity::class.java).apply{
                    startActivity(this)
                    requireActivity().finish()
                }
            } else {//프로필완료안된거
                    Intent(requireContext(), AuthCompleteActivity::class.java).apply{
                    startActivity(this)
                    requireActivity().finish()
                }
            }
        })
    }
    private fun getFirebaseToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                viewModel.setfcmtoken(task.result!!)
            }
        }
    }
}