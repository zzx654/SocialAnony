package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.data.entities.LocationLatLngEntity
import com.example.appportfolio.data.entities.SearchResultEntity
import com.example.appportfolio.databinding.FragmentMapBinding
import com.example.appportfolio.other.Constants
import com.example.appportfolio.ui.main.activity.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {
    lateinit var binding: FragmentMapBinding
    private val location:LocationLatLngEntity
        get(){
            return arguments?.getParcelable("location")!!
        }
    private lateinit var map: GoogleMap
    private var currentSelectMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding= DataBindingUtil.inflate<FragmentMapBinding>(inflater,
            R.layout.fragment_map,container,false)
        (activity as MainActivity).setToolBarVisible("mapFragment")
        setupGoogleMap()

        return binding.root
    }
    override fun onMapReady(p0: GoogleMap) {
        map = p0!!
        SocialApplication.getLocation(location.lat.toDouble(), location.lon.toDouble())?.let{
            showmarker(location,it)
        }
    }
    private fun showmarker(location:LocationLatLngEntity,address:String){
        currentSelectMarker = setupMarker(
            SearchResultEntity(
                fullAddress = address,
                name = "",
                locationLatLng = location
            )
        )
        // 마커 보여주기
        currentSelectMarker?.showInfoWindow()

    }
    private fun setupGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mfragment) as SupportMapFragment
        mapFragment.getMapAsync(this) // callback 구현 (onMapReady)

        // 마커 데이터 보여주기

    }
    private fun setupMarker(searchResult: SearchResultEntity): Marker {
        currentSelectMarker?.remove()
        // 구글맵 전용 위도/경도 객체
        val positionLatLng = LatLng(
            searchResult.locationLatLng!!.lat.toDouble(),
            searchResult.locationLatLng!!.lon.toDouble()
        )

        // 구글맵 마커 객체 설정
        val markerOptions = MarkerOptions().apply {
            position(positionLatLng)
            snippet(searchResult.fullAddress)
        }

        // 카메라 줌 설정
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(positionLatLng,
                Constants.CAMERA_ZOOM_LEVEL
            ))

        return map.addMarker(markerOptions)!!
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="지도"
        super.onResume()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}