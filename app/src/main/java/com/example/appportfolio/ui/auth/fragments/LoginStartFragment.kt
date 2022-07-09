package com.example.appportfolio.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.example.appportfolio.auth.Status
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentLoginstartBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.auth.activity.AuthActivity
import com.example.appportfolio.ui.auth.activity.FillProfileActivity
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginStartFragment: Fragment(R.layout.fragment_loginstart) {

    lateinit var binding:FragmentLoginstartBinding
    private lateinit var viewModel: AuthViewModel
    @Inject
    lateinit var signManager: SignManager
    @Inject
    lateinit var loadingDialog: LoadingDialog
    @Inject
    lateinit var userPreferences: UserPreferences
    lateinit var api: AuthApi
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val resultListener=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
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
    ): View {
        binding= DataBindingUtil.inflate<FragmentLoginstartBinding>(inflater,
            R.layout.fragment_loginstart,container,false)

        checklogout()
        initGoogleSignResource()
        api= RemoteDataSource().buildApi(AuthApi::class.java)
        viewModel= ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        binding.signGoogle.setOnClickListener {
            resultListener.launch(mGoogleSignInClient.signInIntent)
        }
        binding.mailStart.onSingleClick {
            if(findNavController().previousBackStackEntry!=null){
                findNavController().popBackStack()
            }else
                findNavController().navigate(
                    LoginStartFragmentDirections.actionLoginStartFragmentToEmailLoginFragment()
                )
        }
        subscribeToObserver()
        return binding.root
    }
    private fun checklogout(deletetoken:Boolean=true)
    {
        val applyresult:(Status, String?, String?)->Unit={ status, str1, str2->
        }
        signManager.signout(applyresult)
    }
    private fun initGoogleSignResource()
    {
        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(requireContext(),gso)
    }
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>){
        try{
            val account=
                completedTask.getResult(ApiException::class.java)
            (activity as AuthActivity).fcmToken?.let{ fcmtoken->
                account.id?.let { id-> viewModel.signWithSocial("GOOGLE", id,fcmtoken,api) }
            }

        }catch (e: ApiException){
            snackbar(e.message!!)
        }
    }
    private fun subscribeToObserver()
    {
        viewModel.SocialSignResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError = {
                Log.d("socialSignErr",it)
                loadingDialog.dismiss()
                snackbar(it)
            },
            onLoading = {
                loadingDialog.show()
            }
        ){

            if(it.restoken == ""){
                loadingDialog.dismiss()
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
                loadingDialog.dismiss()
                snackbar(it)
            }
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
}