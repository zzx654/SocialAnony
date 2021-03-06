package com.example.appportfolio.ui.main.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.getLocation
import com.example.appportfolio.data.entities.LocationLatLngEntity
import com.example.appportfolio.data.entities.SearchResultEntity
import com.example.appportfolio.databinding.FragmentSearchmapBinding
import com.example.appportfolio.other.Constants.CAMERA_ZOOM_LEVEL
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.activity.LocationActivity
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.LocViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchMapFragment: Fragment(R.layout.fragment_searchmap),OnMapReadyCallback,MenuProvider {
    lateinit var binding: FragmentSearchmapBinding
    private lateinit var vmLoc: LocViewModel
    private val permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)
    private lateinit var map: GoogleMap
    private var currentSelectMarker: Marker? = null
    private lateinit var curloc: LocationLatLngEntity
    lateinit var gpsTracker: GpsTracker

    private val requestPermissionLauncher = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions() ) { result: Map<String, Boolean> ->
        val deniedList: List<String> = result.filter {
            !it.value
        }.map {
            it.key
        }
        when {
            deniedList.isNotEmpty() -> {
                val map = deniedList.groupBy { permission ->
                    if (shouldShowRequestPermissionRationale(permission)) "DENIED" else "EXPLAINED"
                }
                map["DENIED"]?.let {
                }
                map["EXPLAINED"]?.let {
                }
            } else -> {
            gpsTracker= GpsTracker(requireContext())
            if(vmLoc.curloc.value==null)
            {
                //??????????????? ??????
                    vmLoc.setLoc(LocationLatLngEntity(gpsTracker.latitude!!.toFloat(),gpsTracker.longitude!!.toFloat()))
                curloc=LocationLatLngEntity(gpsTracker.latitude!!.toFloat(),gpsTracker.longitude!!.toFloat())
                getLocation(gpsTracker.latitude!!,gpsTracker.longitude!!)?.let{
                    showmarker(it)
                }
            }
            else
            {
                //?????? ????????? ??????
                curloc= LocationLatLngEntity(vmLoc.curloc.value!!.lat,vmLoc.curloc.value!!.lon)
                getLocation(curloc.lat.toDouble(),curloc.lon.toDouble())?.let{
                    showmarker(it)
                }
            }
        }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate(inflater,
            R.layout.fragment_searchmap,container,false)
        activity?.run{
            vmLoc= ViewModelProvider(this)[LocViewModel::class.java]
        }
        setupGoogleMap()

        binding.btnSearch.setOnClickListener {
            if(findNavController().previousBackStackEntry!=null){

                findNavController().popBackStack()
            }else{
                findNavController().navigate(
                    SearchMapFragmentDirections.actionSearchMapFragmentToSearchLocFragment())
            }
        }
        return binding.root
    }
    private fun showmarker(loc:String){
        currentSelectMarker = setupMarker(
            SearchResultEntity(
                fullAddress = loc,
                name = "??? ?????? ??????",
                locationLatLng = curloc
            )
        )
        // ?????? ????????????
        currentSelectMarker?.showInfoWindow()
    }
    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map.setOnMapClickListener {
            curloc = LocationLatLngEntity(it.latitude.toFloat(), it.longitude.toFloat())
            vmLoc.setLoc(curloc)
            getLocation(curloc.lat.toDouble(), curloc.lon.toDouble())?.let {
                showmarker(it)
            }
        }
        if(SocialApplication.checkGeoPermission(requireContext()))
        {
            gpsTracker= GpsTracker(requireContext())
            if(vmLoc.curloc.value==null)
            {
                //??????????????? ??????
                vmLoc.setLoc(LocationLatLngEntity(gpsTracker.latitude!!.toFloat(),gpsTracker.longitude!!.toFloat()))
                curloc=LocationLatLngEntity(gpsTracker.latitude!!.toFloat(),gpsTracker.longitude!!.toFloat())
                getLocation(gpsTracker.latitude!!,gpsTracker.longitude!!)?.let{
                    showmarker(it)
                }
            }
            else
            {
                curloc= LocationLatLngEntity(vmLoc.curloc.value!!.lat,vmLoc.curloc.value!!.lon)
                getLocation(curloc.lat.toDouble(),curloc.lon.toDouble())?.let{
                    showmarker(it)
                }
            }
        }
        else
            requestPermissionLauncher.launch(permissions)
    }
    private fun setupGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mfragment) as SupportMapFragment
        mapFragment.getMapAsync(this) // callback ?????? (onMapReady)
        // ?????? ????????? ????????????
    }
    private fun setupMarker(searchResult: SearchResultEntity): Marker {
        currentSelectMarker?.remove()
        // ????????? ?????? ??????/?????? ??????
        val positionLatLng = LatLng(
            searchResult.locationLatLng!!.lat.toDouble(),
            searchResult.locationLatLng!!.lon.toDouble()
        )

        // ????????? ?????? ?????? ??????
        val markerOptions = MarkerOptions().apply {
            position(positionLatLng)
            title(searchResult.name)
            snippet(searchResult.fullAddress)
        }
        // ????????? ??? ??????
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(positionLatLng, CAMERA_ZOOM_LEVEL))

        return map.addMarker(markerOptions)!!
    }

    override fun onResume() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
            setDisplayShowTitleEnabled(false)
        }
        (activity as LocationActivity).binding.title.text="?????? ??????"
        super.onResume()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.upload_tools, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return  when (menuItem.itemId) {
            android.R.id.home -> {
                //????????????
                activity?.finish()
                true
            }
            R.id.complete -> {
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.putExtra("lat", vmLoc.curloc.value!!.lat)
                intent.putExtra("lon", vmLoc.curloc.value!!.lon)
                activity?.let{
                    it.setResult(Activity.RESULT_OK,intent)
                    it.finish()
                }
                true
            }
            else->false
        }
    }
}