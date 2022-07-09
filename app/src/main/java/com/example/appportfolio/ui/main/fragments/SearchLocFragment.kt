package com.example.appportfolio.ui.main.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.adapters.SearchRecyclerAdapter
import com.example.appportfolio.api.build.RetrofitLoc
import com.example.appportfolio.data.entities.*
import com.example.appportfolio.databinding.FragmentSearchlocBinding
import com.example.appportfolio.other.CustomDecoration
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.LocationActivity
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.LocViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchLocFragment: Fragment(R.layout.fragment_searchloc) {
    lateinit var binding: FragmentSearchlocBinding
    private lateinit var searchadapter: SearchRecyclerAdapter
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var vmLoc: LocViewModel
    private var isLast=false
    private var isLoading=false
    private var itemclicked=false
    private var firstLoading=true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= DataBindingUtil.inflate<FragmentSearchlocBinding>(inflater,
            R.layout.fragment_searchloc,container,false)
        activity?.run{
            vmLoc= ViewModelProvider(this)[LocViewModel::class.java]
        }
        binding.edtText.addTextChangedListener {editable->

            editable?.let{
                firstLoading=true
                isLast=false
                searchadapter.currentPage=1
                searchadapter.currentSearchString=binding.edtText.text.toString()
                if(binding.edtText.text.toString().length>1)
                    vmLoc.getSearchLocation(binding.edtText.text.toString(),1, RetrofitLoc.apiService)
                else
                    searchadapter.submitList(listOf())

            }
        }
        searchadapter= SearchRecyclerAdapter()
        searchadapter.setOnItemClickListener {
            itemclicked=true
           vmLoc.setLoc(LocationLatLngEntity(it.locationLatLng!!.lat,it.locationLatLng!!.lon))

            //여기에 뷰모델에 result값 설정하고 설정되면 popbackstack
        }
        setupRecyclerView()
        subsrcibeToObserver()
        return binding.root
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as LocationActivity).binding.title.text="위치 검색"
        super.onResume()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                if(findNavController().previousBackStackEntry!=null){
                    findNavController().popBackStack()
                }else{
                    findNavController().navigate(
                        SearchLocFragmentDirections.actionSearchLocFragmentToSearchMapFragment())
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setupRecyclerView()=binding.rv.apply{
        val customDecoration= CustomDecoration(
            ContextCompat.getDrawable(requireContext(), R.drawable.divider),0f,false)
        adapter=searchadapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
        addOnScrollListener(scrollListener)
        addItemDecoration(customDecoration)

    }
    private val scrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.adapter ?: return

            val lastVisibleItemPosition =
                (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1

            // 페이지 끝에 도달한 경우
            if (!recyclerView.canScrollVertically(1) && lastVisibleItemPosition == totalItemCount&&!isLast&&!isLoading) {
                firstLoading=false
                loadNext()
            }
        }
    }
    private fun hideKeyboard() {
        if (::inputMethodManager.isInitialized.not()) {
            inputMethodManager=activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        }
        inputMethodManager.hideSoftInputFromWindow(binding.edtText.windowToken, 0)
    }
    private fun makeMainAddress(poi: Poi): String =
        if (poi.secondNo?.trim().isNullOrEmpty()) {
            (poi.upperAddrName?.trim() ?: "") + " " +
                    (poi.middleAddrName?.trim() ?: "") + " " +
                    (poi.lowerAddrName?.trim() ?: "") + " " +
                    (poi.detailAddrName?.trim() ?: "") + " " +
                    poi.firstNo?.trim()
        } else {
            (poi.upperAddrName?.trim() ?: "") + " " +
                    (poi.middleAddrName?.trim() ?: "") + " " +
                    (poi.lowerAddrName?.trim() ?: "") + " " +
                    (poi.detailAddrName?.trim() ?: "") + " " +
                    (poi.firstNo?.trim() ?: "") + " " +
                    poi.secondNo?.trim()
        }
    private fun subsrcibeToObserver()
    {
        vmLoc.curloc.observe(viewLifecycleOwner){
            if(itemclicked)
            {
                if(findNavController().previousBackStackEntry!=null){
                    findNavController().popBackStack()
                }else{
                    findNavController().navigate(
                        SearchLocFragmentDirections.actionSearchLocFragmentToSearchMapFragment())
                }
            }

        }
        vmLoc.getLocResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading={
              isLoading=true
                if(!firstLoading)
                {
                    var templist=searchadapter.currentList.toList()
                    templist+=listOf(SearchResultEntity(null,"",null))
                    searchadapter.submitList(templist)
                }

            },
            onError={
                isLoading=false
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
                val currentlist=searchadapter.currentList.toMutableList()
                currentlist.removeLast()
                searchadapter.submitList(currentlist)
            }
        ){
            isLoading=false
            val currentlist=searchadapter.currentList.toMutableList()
            if(!firstLoading)
            {
                currentlist.removeLast()
            }
            if(it.body()==null)
            {
                isLast=true
                searchadapter.submitList(currentlist)
                if(searchadapter.currentPage>1)
                    snackbar("더 이상 표시할 목록이 없습니다")
                else
                    searchadapter.submitList(listOf())
            }

            it.body()?.let{ searchResponse ->
                setData(currentlist.toList(),searchResponse.searchPoiInfo)

            }

        })
    }
    private fun loadNext() {
        if (binding.rv.adapter?.itemCount == 0)
            return

        vmLoc.getSearchLocation(searchadapter.currentSearchString,searchadapter.currentPage+1,RetrofitLoc.apiService)
    }
    private fun setData(oldlist:List<SearchResultEntity>,searchInfo: SearchPoiInfo){
        val pois: Pois = searchInfo.pois
        // mocking data
        val dataList = pois.poi.map {
            SearchResultEntity(
                name = it.name ?: "빌딩명 없음",
                fullAddress = makeMainAddress(it),
                locationLatLng = LocationLatLngEntity(
                    it.noorLat,
                    it.noorLon
                )
            )
        }
        if(searchInfo.page.toInt()==1)
            searchadapter.submitList(dataList)
        else
            searchadapter.submitList(oldlist+dataList)
        searchadapter.currentPage = searchInfo.page.toInt()


    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }
}