package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.databinding.FragmentImgBinding

class ImageFragment: Fragment(R.layout.fragment_img) {
    lateinit var binding: FragmentImgBinding
    private val args: ImageFragmentArgs by navArgs()
    private val imageurl:String
        get(){
            return args.imageurl
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentImgBinding>(inflater,
            R.layout.fragment_img,container,false)

        binding.close.setOnClickListener {
            findNavController().popBackStack()
        }
        Glide.with(binding.img.context)
            .load(imageurl)
            .into(binding.img)
        return binding.root
    }

}