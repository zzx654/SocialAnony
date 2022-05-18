package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.PersonAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.other.Constants
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BasePersonViewModel
import com.example.appportfolio.ui.main.viewmodel.applyFollowViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


abstract class BasePersonFragment (layoutId:Int
): Fragment(layoutId) {
    private lateinit var vmAuth: AuthViewModel
    private var searchingperson:String?=null
    protected var curselectedfollowing:Int=0
    protected var firstloading=true
    protected var isScrolling=false
    protected var curfrag:String=""
    protected abstract val basePersonViewModel: BasePersonViewModel
    lateinit var api: MainApi
    @Inject
    lateinit var userPreferences: UserPreferences
    protected var beforeitemSize=0
    protected abstract val searchedAdapter: PersonAdapter
    protected abstract val followingAdapter:PersonAdapter?
    protected abstract val rvSearched:RecyclerView
    protected abstract val loadSearched:ProgressBar
    protected abstract val edtSearch: EditText
    protected abstract val rvFollowed:RecyclerView?
    protected abstract val loadfirstprogress:ProgressBar
    protected var curTogglinguser=0
    protected lateinit var vmToggle:applyFollowViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vmToggle=ViewModelProvider(requireActivity()).get(applyFollowViewModel::class.java)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    protected fun setView()
    {
        var job: Job? = null
        edtSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = lifecycleScope.launch {

                delay(Constants.SEARCH_TIME_DELAY)

                editable?.let {
                    if (!edtSearch.text.toString().trim().isEmpty()) {

                        if(searchingperson!=it.toString())
                        {
                            firstloading=true
                            beforeitemSize=0
                            isScrolling=false
                            searchedAdapter.differ.submitList(listOf())
                            searchingperson=it.toString()
                            basePersonViewModel.getsearchedPersons(null,it.toString(),api)
                        }
                    }
                }
                if (editable!!.isEmpty()) {
                    rvFollowed?.let{

                        showFollowedRv()
                    }
                    isScrolling=false
                    beforeitemSize=0
                    searchedAdapter.differ.submitList(listOf())
                    searchingperson=null
                }
            }
        }
        searchedAdapter.setOnPersonClickListener { person->
            curTogglinguser=person.userid
            curselectedfollowing=person.following
            basePersonViewModel.checkuser(person.userid,api)
        }
        searchedAdapter.setOnFollowClickListener { person->
            curTogglinguser=person.userid
            if(person.following==1)
                showToggleDialog(curTogglinguser)
            else
                basePersonViewModel.toggleFollow(curTogglinguser,person.following,api)
        }
        followingAdapter?.setOnPersonClickListener { person->
            curTogglinguser=person.userid
            curselectedfollowing=person.following
            basePersonViewModel.checkuser(person.userid,api)
        }
        followingAdapter?.setOnFollowClickListener { person ->
            curTogglinguser = person.userid
            if (person.following == 1)
                showToggleDialog(curTogglinguser)
            else
                basePersonViewModel.toggleFollow(curTogglinguser, person.following, api)
        }
    }
    private val searchedscrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager=recyclerView.layoutManager as LinearLayoutManager
            val totalItemCount=layoutManager.itemCount//화면을 무시한 전체 아이템의 수

            if(!recyclerView.canScrollVertically(1)&&beforeitemSize!=searchedAdapter.differ.currentList.size&&isScrolling){
                isScrolling=false
                beforeitemSize=searchedAdapter.differ.currentList.size
                val lastuserid=searchedAdapter.differ.currentList[beforeitemSize-1].userid

                basePersonViewModel.getsearchedPersons(lastuserid,edtSearch.text.toString(),api)
            }
        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling=true
        }
    }
    protected fun showToggleDialog(toggleuser:Int)
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView:View=edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="팔로우를 해제하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            basePersonViewModel.toggleFollow(toggleuser,1,api)
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    protected fun init()
    {
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        api= RemoteDataSource().buildApi(
            MainApi::class.java,
            runBlocking { userPreferences.authToken.first() })
        setupSearchedRcv()
    }
    protected fun setupSearchedRcv()=rvSearched.apply {
        adapter=searchedAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
        addOnScrollListener(this@BasePersonFragment.searchedscrollListener)
    }
    protected fun showFollowedRv()
    {
        rvFollowed?.visibility=View.VISIBLE
        rvSearched.visibility=View.GONE
    }
    protected fun hideFollowedRv()
    {
        rvFollowed?.visibility=View.GONE
        rvSearched.visibility=View.VISIBLE
    }
    open fun subscribeToObserver()
    {
        vmToggle.curtoggling.observe(viewLifecycleOwner){
            applyFollowingState(it.curtoggleuser,it.following)
        }
        basePersonViewModel.checkuserResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError ={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode){
                when(it.resultCode)
                {
                    200->{
                        val bundle=Bundle()
                        bundle.putInt("userid",it.value)
                        bundle.putInt("follow",curselectedfollowing)
                        bundle.putString("from",curfrag)
                        (activity as MainActivity).replaceFragment("othersProfileFragment",OthersProfileFragment(),bundle)
                    }
                    400->{
                        Toast.makeText(requireContext(),"탈퇴한 회원입니다",Toast.LENGTH_SHORT).show()
                    }
                    500->{
                        Toast.makeText(requireContext(),"해당유저를 차단했거나 차단당했습니다",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        basePersonViewModel.togglefollowResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onError={
                snackbar(it)
            },
            onLoading ={

            }
        ){
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==200)
                {
                    var alerted=false
                    var message=""
                    var followingstate:Int=0
                    for(i in searchedAdapter.persons.indices)
                    {
                        if(curTogglinguser==searchedAdapter.persons[i].userid)
                        {
                            alerted=true
                            searchedAdapter.persons[i].apply{
                                if(this.following==0)
                                {
                                    message=searchedAdapter.persons[i].nickname+"님을 팔로우했습니다"
                                    this.following=1
                                    followingstate=1
                                }
                                else{
                                    message=searchedAdapter.persons[i].nickname+"님을 언팔로우했습니다"
                                    this.following=0
                                    followingstate=0
                                }
                                Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
                            }
                            searchedAdapter.notifyItemChanged(i)
                            break
                        }
                    }
                    rvFollowed?.let{
                        followingAdapter?.let{ followingAdapter->
                            for(i in followingAdapter.persons.indices)
                            {
                                if(curTogglinguser==followingAdapter.persons[i].userid)
                                {
                                    followingAdapter.persons[i].apply{
                                        if(this.following==0)
                                        {
                                            message=followingAdapter.persons[i].nickname+"님을 팔로우했습니다"
                                            this.following=1
                                            followingstate=1
                                        }
                                        else{
                                            message=followingAdapter.persons[i].nickname+"님을 언팔로우했습니다"
                                            this.following=0
                                            followingstate=0
                                        }
                                        if(!alerted)
                                            Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
                                    }
                                    followingAdapter.notifyItemChanged(i)
                                    break
                                }
                            }
                        }

                    }
                    vmToggle.setcurtoggle(curTogglinguser,followingstate)
                }
                else
                {
                    Toast.makeText(requireContext(),"서버 오류발생",Toast.LENGTH_SHORT).show()
                }
            }

        })
        basePersonViewModel.getsearchedPersonResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                if(firstloading)
                    loadfirstprogress.visibility=View.GONE
                else
                    loadSearched.visibility=View.GONE
                firstloading=false

                snackbar(it)
            },
            onLoading={
                if(firstloading)
                    loadfirstprogress.visibility=View.VISIBLE
                else
                    loadSearched.visibility=View.VISIBLE
            }
        ){
            if(firstloading)
                loadfirstprogress.visibility=View.GONE
            else
                loadSearched.visibility=View.GONE
            firstloading=false
            handleResponse(requireContext(),it.resultCode){
                when(it.resultCode){
                    400->{
                        snackbar("서버 에러 발생")
                    }
                    300->{

                    }
                    200->{
                        hideFollowedRv()
                        var searchedlist=searchedAdapter.differ.currentList.toList()
                        if(firstloading)
                            searchedlist=listOf()



                        searchedlist+=it.persons
                        searchedAdapter.differ.submitList(searchedlist)
                    }
                    else-> null
                }
            }

        })
    }
    protected fun applyFollowingState(curTogglinguser:Int,followingstate:Int)
    {
        for(i in searchedAdapter.persons.indices)
        {
            if(curTogglinguser==searchedAdapter.persons[i].userid)
            {
                searchedAdapter.persons[i].apply{
                    this.following=followingstate
                }
                searchedAdapter.notifyItemChanged(i)
                break
            }
        }
        rvFollowed?.let{
            followingAdapter?.let{ followingAdapter->
                for(i in followingAdapter.persons.indices)
                {
                    if(curTogglinguser==followingAdapter.persons[i].userid)
                    {
                        followingAdapter.persons[i].apply{
                            this.following=followingstate
                        }
                        followingAdapter.notifyItemChanged(i)
                        break
                    }
                }
            }
        }
    }
}