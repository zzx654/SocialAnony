package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.adapters.PersonAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.databinding.FragmentSearchpersonBinding
import com.example.appportfolio.ui.main.viewmodel.BasePersonViewModel
import com.example.appportfolio.ui.main.viewmodel.SearchPersonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
@AndroidEntryPoint
class SearchPersonFragment: BasePersonFragment(R.layout.fragment_searchperson) {
    //공통적으로 수행해야하는 코드 검색된사람 load, 검색된사람 loadmore 사람item 눌렀을떄 사람프래그먼트로 이동하기, 사람 팔로우 팔로우취소 하기 ok?
     lateinit var binding: FragmentSearchpersonBinding
     private var mRootView:View?=null
    lateinit var searchedadapter: PersonAdapter
    override val basePersonViewModel: BasePersonViewModel
        get() {
            //val vm: SearchPersonViewModel by viewModels()
            val vm=ViewModelProvider(requireActivity()).get(SearchPersonViewModel::class.java)
            return vm
        }
    override val searchedAdapter: PersonAdapter
        get() = searchedadapter
    override val followingAdapter: PersonAdapter?
        get() =null
    override val rvSearched: RecyclerView
        get() = binding.rvSearchedPerson
    override val edtSearch: EditText
        get() = binding.edtNick
    override val rvFollowed: RecyclerView?
        get() = null
    override val loadfirstprogress: ProgressBar
        get() = binding.firstloadprogress
    protected val viewModel: SearchPersonViewModel
        get() = basePersonViewModel as SearchPersonViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null)
        {
            curfrag="searchPersonFragment"
            binding= DataBindingUtil.inflate<FragmentSearchpersonBinding>(inflater,
                R.layout.fragment_searchperson,container,false)
            searchedadapter=PersonAdapter()
            setView()
            init()
            mRootView=binding.root
        }
        subscribeToObserver()
        return mRootView
    }
}