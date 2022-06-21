package com.example.appportfolio.ui.main.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.ui.main.activity.MainActivity

class settingpreFragment:PreferenceFragmentCompat() {
    lateinit var vmAuth: AuthViewModel
    lateinit var prefs:SharedPreferences
    var bookmarkPreference: Preference? = null
    var mypostPreference:Preference? = null
    var blockPreference:Preference? = null
    var chatonoffPreference:Preference?=null
    var pushonoffPreference:Preference?=null
    var logoffPreference:Preference?=null
    var outPreference:Preference?=null
    var changepwPreference:Preference?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.overScrollMode=View.OVER_SCROLL_NEVER
        vmAuth.platform.observe(viewLifecycleOwner){
            if(it.equals("GOOGLE"))
                changepwPreference?.isVisible=false
        }
    }
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.mysettingpreference,rootKey)
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        if(rootKey==null)
        {
            bookmarkPreference=findPreference("bookmark")
            mypostPreference=findPreference("mypost")
            blockPreference=findPreference("blockuser")
            chatonoffPreference=findPreference("chatonoff")
            pushonoffPreference=findPreference("pushonoff")
            logoffPreference=findPreference("logoff")
            outPreference=findPreference("out")
            changepwPreference=findPreference("changepw")

            prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        }
        bookmarkPreference?.setOnPreferenceClickListener {
            (activity as MainActivity).replaceFragment("bookmarkFragment",BookmarkFragment(),null)
            true
        }
        mypostPreference?.setOnPreferenceClickListener {
            (activity as MainActivity).replaceFragment("myPostsFragment",MyPostsFragment(),null)
            true
        }
        blockPreference?.setOnPreferenceClickListener {
            (activity as MainActivity).replaceFragment("blockFragment",BlockFragment(),null)
            true
        }
        changepwPreference?.setOnPreferenceClickListener {
            (activity as MainActivity).replaceFragment("setPasswordFragment",SetPasswordFragment(),null)
            true
        }

        vmAuth.platform.value?.let {
            if(it.equals("GOOGLE"))
                changepwPreference?.isVisible=false
        }


        logoffPreference?.setOnPreferenceClickListener {
            val dialog= AlertDialog.Builder(requireContext()).create()
            val edialog: LayoutInflater = LayoutInflater.from(requireContext())
            val mView: View =edialog.inflate(R.layout.dialog_alert,null)
            val cancel: Button =mView.findViewById(R.id.cancel)
            val positive: Button =mView.findViewById(R.id.positive)
            val alertText: TextView =mView.findViewById(R.id.tvWarn)
            alertText.text="로그아웃 하시겠습니까?"
            cancel.setOnClickListener {
                dialog.dismiss()
                dialog.cancel()
            }
            positive.setOnClickListener {

                (activity as MainActivity).logout()
                dialog.dismiss()
                dialog.cancel()
            }
            dialog.setView(mView)
            dialog.create()
            dialog.show()

            true
        }
        outPreference?.setOnPreferenceClickListener {
            val dialog= AlertDialog.Builder(requireContext()).create()
            val edialog: LayoutInflater = LayoutInflater.from(requireContext())
            val mView: View =edialog.inflate(R.layout.dialog_alert,null)
            val cancel: Button =mView.findViewById(R.id.cancel)
            val positive: Button =mView.findViewById(R.id.positive)
            val alertText: TextView =mView.findViewById(R.id.tvWarn)
            alertText.text="회월탈퇴 하시겠습니까?"
            cancel.setOnClickListener {
                dialog.dismiss()
                dialog.cancel()
            }
            positive.setOnClickListener {

                (activity as MainActivity).withdrawal()
                dialog.dismiss()
                dialog.cancel()
            }
            dialog.setView(mView)
            dialog.create()
            dialog.show()

            true
        }
    }
}