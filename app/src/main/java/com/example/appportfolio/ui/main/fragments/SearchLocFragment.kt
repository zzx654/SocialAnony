package com.example.appportfolio.ui.main.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.adapters.SearchRecyclerAdapter
import com.example.appportfolio.api.build.RetrofitLoc
import com.example.appportfolio.data.entities.*
import com.example.appportfolio.databinding.FragmentSearchlocBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.LocationActivity
import com.example.appportfolio.ui.main.viewmodel.LocViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchLocFragment: Fragment(R.layout.fragment_searchloc) {
    lateinit var binding: FragmentSearchlocBinding
    lateinit var searchadapter: SearchRecyclerAdapter
    lateinit var inputMethodManager: InputMethodManager
    lateinit var vmLoc: LocViewModel
    private var isLast=false
    private var isLoading=false
    private var itemclicked=false
    private var firstLoading=true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate<FragmentSearchlocBinding>(inflater,
            R.layout.fragment_searchloc,container,false)
        activity?.run{
            vmLoc= ViewModelProvider(this).get(LocViewModel::class.java)
        }
        binding.edtText.addTextChangedListener {editable->

            editable?.let{
                if(binding.edtText.text.toString().trim().isEmpty())
                {
                    binding.btnsearch.visibility=View.GONE
                }
                else
                {
                    binding.btnsearch.visibility=View.VISIBLE
                }
            }
        }
        binding.btnsearch.setOnClickListener {
            //searchKeyword(binding.searchBarInputView.text.toString())
            firstLoading=true
            isLast=false
            searchadapter.currentPage=1
            vmLoc.getSearchLocation(binding.edtText.text.toString(),1, RetrofitLoc.apiService)
            searchadapter.currentSearchString=binding.edtText.text.toString()
            binding.edtText.setText("")
            // 키보드 숨기기
            hideKeyboard()
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

    override fun onStop() {
        super.onStop()
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
        adapter=searchadapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
        addOnScrollListener(scrollListener)

    }
    val scrollListener= object: RecyclerView.OnScrollListener(){
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
                    searchadapter.results+=listOf(SearchResultEntity(null,"",null))
                    searchadapter.notifyItemInserted(searchadapter.itemCount)
                }

            },
            onError={
                isLoading=false
                Toast.makeText(requireContext(),it, Toast.LENGTH_SHORT).show()
                var currentlist=searchadapter.differ.currentList.toMutableList()
                currentlist.removeLast()
                searchadapter.differ.submitList(currentlist)
            }
        ){
            isLoading=false
            var currentlist=searchadapter.differ.currentList.toMutableList()
            if(!firstLoading)
            {
                currentlist.removeLast()
            }
            if(it.body()==null)
            {
                isLast=true
                searchadapter.differ.submitList(currentlist)
                snackbar("더 이상 표시할 목록이 없습니다")
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
            searchadapter.differ.submitList(dataList)
        else
            searchadapter.differ.submitList(oldlist+dataList)
        searchadapter.currentPage = searchInfo.page.toInt()


    }
}