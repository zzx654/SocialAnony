package com.example.appportfolio.ui.main.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.appportfolio.R
import com.example.appportfolio.databinding.ActivityLocationBinding
import com.example.appportfolio.ui.main.viewmodel.LocViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationActivity: AppCompatActivity() {
    lateinit var binding: ActivityLocationBinding
    private lateinit var vmLoc:LocViewModel
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_location)
        vmLoc= ViewModelProvider(this)[LocViewModel::class.java]
        setSupportActionBar(binding.toolbar)
    }
    override fun onBackPressed() {
    }
}
