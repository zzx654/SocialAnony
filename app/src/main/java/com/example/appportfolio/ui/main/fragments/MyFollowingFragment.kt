package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.PersonAdapter
import com.example.appportfolio.databinding.FragmentSearchpersonBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePersonViewModel
import com.example.appportfolio.ui.main.viewmodel.MyFollowingViewModel
import com.example.appportfolio.ui.main.viewmodel.SearchPersonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyFollowingFragment:BasePersonFragment(R.layout.fragment_searchperson) {
    lateinit var binding: FragmentSearchpersonBinding
    lateinit var searchedadapter: PersonAdapter
    lateinit var followingadapter:PersonAdapter
    private var mRootView:View?=null
    override val basePersonViewModel: BasePersonViewModel
        get() {
        //val vm: MyFollowingViewModel by viewModels()
            val vm= ViewModelProvider(requireActivity()).get(MyFollowingViewModel::class.java)
        return vm
    }
    override val searchedAdapter: PersonAdapter
        get() = searchedadapter
    override val followingAdapter: PersonAdapter?
        get() =followingadapter
    override val rvSearched: RecyclerView
        get() = binding.rvSearchedPerson
    override val loadSearched: ProgressBar
        get() = binding.loadMoreSearcedProgressbar
    override val edtSearch: EditText
        get() = binding.edtNick
    override val rvFollowed: RecyclerView?
        get() = binding.rvFollowedPerson
    override val loadfirstprogress: ProgressBar
        get() = binding.firstloadprogress

    protected val viewModel: MyFollowingViewModel
        get() = basePersonViewModel as MyFollowingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null) {
            binding = DataBindingUtil.inflate<FragmentSearchpersonBinding>(
                inflater,
                R.layout.fragment_searchperson, container, false
            )
            (activity as MainActivity).setToolBarVisible("myFollowingFragment")
            curfrag = "myFollowingFragment"
            searchedadapter = PersonAdapter()
            followingadapter = PersonAdapter()
            setupFollowingRcv()
            showFollowedRv()



            setView()

            firstloading=true
            init()
            viewModel.getFollowingPersons(null,api)
            mRootView=binding.root
        }
        subscribeToObserver()

        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="팔로잉"
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
    private fun setupFollowingRcv()=rvFollowed?.apply {
        adapter=followingAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
        addOnScrollListener(this@MyFollowingFragment.followingscrollListener)
    }
    private val followingscrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(!recyclerView.canScrollVertically(1)&&(beforeitemSize!=followingAdapter!!.differ.currentList.size)&&isScrolling){
                isScrolling=false
                beforeitemSize=followingAdapter!!.differ.currentList.size
                val lastuserid=followingAdapter!!.differ.currentList[beforeitemSize-1].userid
                viewModel.getFollowingPersons(lastuserid,api)
            }
        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling=true
        }
    }

    override fun subscribeToObserver() {
        super.subscribeToObserver()
        viewModel.getfollowingPersonResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                if(firstloading)
                    loadfirstprogress.visibility=View.GONE
                else
                    binding.loadMoreFollowdProgressbar.visibility=View.GONE
                firstloading=false
                snackbar(it)
            },
            onLoading={
                if(firstloading)
                    loadfirstprogress.visibility=View.VISIBLE
                else
                    binding.loadMoreFollowdProgressbar.visibility=View.VISIBLE
            }
        ){
            if(firstloading)
                loadfirstprogress.visibility=View.GONE
            else
                binding.loadMoreFollowdProgressbar.visibility=View.GONE
            firstloading=false
            handleResponse(requireContext(),it.resultCode) {
                when (it.resultCode) {
                    400 -> {
                        snackbar("서버 에러 발생")
                    }
                    300 -> {

                    }
                    200 -> {
                        showFollowedRv()


                        var list = followingAdapter!!.differ.currentList.toList()
                        list += it.persons
                        followingadapter.differ.submitList(list)
                    }
                    else -> null
                }
            }
        })

    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}