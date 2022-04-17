package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentChangepwBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentChangepwBinding>(inflater,
            R.layout.fragment_changepw,container,false)
        api= RemoteDataSource().buildApi(AuthApi::class.java,
            runBlocking { preferences.authToken.first() })
        viewModel= ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);



        binding.complete.setOnClickListener {
            if(binding.edtcurpw.text.toString().equals("")||binding.edtnewpw.text.toString().equals("")||binding.edtnewrepeatpw.text.toString().equals(""))
            {
                snackbar("비밀번호를 전부 입력해주세요")
            }else if(!binding.edtnewpw.text.toString().equals(binding.edtnewrepeatpw.text.toString())){
                snackbar(requireContext().getString(R.string.error_incorrectly_repeated_password))
            }else if(binding.edtnewpw.text.toString().length<8){
                snackbar(requireContext().getString(R.string.error_password_too_short))
            }
            else {
                //비밀번호 변경하기기
                viewModel.changepassword(binding.edtcurpw.text.toString(),binding.edtnewpw.text.toString(),api)
            }
        }
        binding.btnback.setOnClickListener {
            findNavController().popBackStack()
        }
        subscribeToObserver()
        return binding.root
    }
    private fun subscribeToObserver()
    {
        viewModel.changepasswordResponse.observe(viewLifecycleOwner, Event.EventObserver(

        ){
            if(it.resultCode==200)
            {
                Toast.makeText(requireContext(),"비밀번호가 변경되었습니다.",Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            else if(it.resultCode==300)
            {
                snackbar("입력된 비밀번호가 현재비밀번호와 같습니다")
            }

        })
    }
}