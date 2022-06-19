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
import com.example.appportfolio.api.responses.getpersonResponse
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.Person
import com.example.appportfolio.other.Constants
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
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
    @Inject
    lateinit var loadingDialog:LoadingDialog
    protected abstract val searchedAdapter: PersonAdapter?
    //protected abstract val followingAdapter:PersonAdapter?
    //protected abstract val rvSearched:RecyclerView
    protected abstract val edtSearch: EditText?
    //protected abstract val rvFollowed:RecyclerView?
    protected abstract val loadfirstprogress:ProgressBar
    protected var curTogglinguser=0
    var isLoading=false
    var isLast=false
    protected lateinit var vmToggle:applyFollowViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vmToggle=ViewModelProvider(requireActivity()).get(applyFollowViewModel::class.java)
    }
    protected fun setView()
    {
        var job: Job? = null
        edtSearch?.let{ edt->
            edt.addTextChangedListener { editable ->
                job?.cancel()
                job = lifecycleScope.launch {

                    delay(Constants.SEARCH_TIME_DELAY)

                    editable?.let {
                        if (!edt.text.toString().trim().isEmpty()) {

                            if(searchingperson!=it.toString())
                            {
                                firstloading=true
                                isLast=false
                                isLoading=false
                                isScrolling=false
                                searchedAdapter?.submitList(listOf())
                                searchingperson=it.toString()
                                basePersonViewModel.getsearchedPersons(null,it.toString(),api)
                            }
                        }
                    }
                    if (editable!!.isEmpty()) {
                        hideSearchedRv()
                        isScrolling=false
                        isLast=false
                        isLoading=false
                        searchedAdapter?.submitList(listOf())
                        searchingperson=null
                    }
                }
            }
        }

        searchedAdapter?.setOnPersonClickListener { person->
            clickperson(person)
        }
        searchedAdapter?.setOnFollowClickListener { person->
            clickfollow(person)
        }
    }
    protected fun clickperson(person:Person)
    {
        curTogglinguser=person.userid!!
        curselectedfollowing=person.following
        basePersonViewModel.checkuser(person.userid!!,api)

    }
    protected fun clickfollow(person:Person)
    {
        curTogglinguser=person.userid!!
        if(person.following==1)
            showToggleDialog(curTogglinguser)
        else
            basePersonViewModel.toggleFollow(curTogglinguser,person.following,api)
    }
    protected val searchedscrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1
            if(searchedAdapter?.currentList!!.isNotEmpty()){
                searchedAdapter?.currentList!!.last().userid?.let{
                    if(!recyclerView.canScrollVertically(1)&&lastVisibleItemPosition == totalItemCount&&isScrolling&&!isLoading&&!isLast){
                        isScrolling=false
                        basePersonViewModel.getsearchedPersons(it,edtSearch!!.text.toString(),api)
                    }
                }
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
    }

    protected fun setupPersonRv(rv:RecyclerView,personadapter:PersonAdapter,scrollListener: RecyclerView.OnScrollListener)
    {
        rv.apply {
            adapter=personadapter
            layoutManager=LinearLayoutManager(requireContext())
            itemAnimator=null
            addOnScrollListener(scrollListener)
        }

    }
    open fun hideSearchedRv()=Unit
    //{
     //   rvFollowed?.visibility=View.VISIBLE
      //  rvSearched.visibility=View.GONE
    //}
    open fun showSearchedRv()=Unit
    //{
      //  rvFollowed?.visibility=View.GONE
       // rvSearched.visibility=View.VISIBLE
    //}
    open fun subscribeToObserver()
    {
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
                loadingDialog.dismiss()
                snackbar(it)
            },
            onLoading ={
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==200)
                {
                    applyFollowingState()
                }
                else
                {
                    Toast.makeText(requireContext(),"서버 오류발생",Toast.LENGTH_SHORT).show()
                }
            }

        })
        basePersonViewModel.getsearchedPersonResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                getPersonLoading(searchedAdapter!!)
            },
            onError = {
                getPersonError(searchedAdapter!!,it)

            }
        ) {
            getPersonSuccess(searchedAdapter!!, it)
            showSearchedRv()

        })
        /**basePersonViewModel.getsearchedPersonResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                isLoading=false
                if(firstloading)
                    loadfirstprogress.visibility=View.GONE
                else
                {
                    var currentlist=searchedAdapter?.currentList!!.toMutableList()
                    currentlist.removeLast()
                    searchedAdapter?.submitList(currentlist)
                }

                firstloading=false

                snackbar(it)
            },
            onLoading={
                isLoading=true
                if(firstloading)
                    loadfirstprogress.visibility=View.VISIBLE
                else
                {
                    if(searchedAdapter?.currentList!!.size>0)
                    {
                        var templist=searchedAdapter?.currentList!!.toList()
                        templist+=listOf(Person(null,"","","",0,null))
                        searchedAdapter?.submitList(templist)
                    }
                }
            }
        ){


        })**/
    }
    protected fun getPersonSuccess(adapter: PersonAdapter,getpersonResponse: getpersonResponse){
        if(firstloading)
            loadfirstprogress.visibility=View.GONE

        handleResponse(requireContext(),getpersonResponse.resultCode){
            isLoading=false
            when(getpersonResponse.resultCode){
                400->{
                    snackbar("서버 에러 발생")
                }
                300->{
                    isLast=true
                    adapter.submitList(adapter.currentList!!.filter { person -> person.userid!=null  })
                    if(adapter.currentList!!.isNotEmpty())
                        snackbar("더이상 표시할 목록이 없습니다")
                }
                200->{
                    if(firstloading)
                    {
                        adapter.submitList(getpersonResponse.persons)
                        firstloading=false
                    }
                    else
                    {
                        var templist=adapter.currentList.toList()
                        templist+=getpersonResponse.persons
                        adapter.submitList(templist.filter { person-> person.userid!=null })
                    }


                }
                else-> {
                    adapter.submitList(adapter.currentList.filter{person-> person.userid!=null})
                }
            }
        }

    }
    protected fun getPersonLoading(adapter:PersonAdapter){
        isLoading=true
        if(firstloading)
            loadfirstprogress.visibility=View.VISIBLE
        else
        {
            if(adapter.currentList!!.size>0)
            {
                var templist=adapter.currentList!!.toList()
                templist+=listOf(Person(null,"","","",0,null))
                adapter.submitList(templist)
            }
        }
    }
    protected fun getPersonError(adapter: PersonAdapter,errormsg:String)
    {
        isLoading=false
        if(firstloading)
            loadfirstprogress.visibility=View.GONE
        else
        {
            adapter.submitList(adapter.currentList.filter{person-> person.userid!=null})
        }

        firstloading=false

        snackbar(errormsg)
    }
    abstract fun applyFollowingState()

    /**protected fun applyFollowingState(curTogglinguser:Int,followingstate:Int)
    { abstract로바꾸고 아래것들 각각프래그먼트에서 다시작성하기
        for(i in searchedAdapter.currentList.indices)
        {
            if(curTogglinguser==searchedAdapter.currentList[i].userid)
            {
                searchedAdapter.currentList[i].apply{
                    this.following=followingstate
                }
                searchedAdapter.notifyItemChanged(i)
                break
            }
        }
        rvFollowed?.let{
            followingAdapter?.let{ followingAdapter->
                for(i in followingAdapter.currentList.indices)
                {
                    if(curTogglinguser==followingAdapter.currentList[i].userid)
                    {
                        followingAdapter.currentList[i].apply{
                            this.following=followingstate
                        }
                        followingAdapter.notifyItemChanged(i)
                        break
                    }
                }
            }
        }
    }**/
}