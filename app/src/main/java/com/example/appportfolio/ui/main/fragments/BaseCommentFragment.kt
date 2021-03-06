package com.example.appportfolio.ui.main.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.getAge
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.CommentAdapter
import com.example.appportfolio.adapters.PostDetailsAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.Comment
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.other.Constants.BLOCK
import com.example.appportfolio.other.Constants.CHAT
import com.example.appportfolio.other.Constants.DELETE
import com.example.appportfolio.other.Constants.REPORT
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.InteractionDialog
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.BaseCommentViewModel
import com.example.appportfolio.ui.main.viewmodel.interactViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

abstract class BaseCommentFragment (layoutId:Int
): Fragment(layoutId) {
    val vmInteract: interactViewModel by viewModels()
    lateinit var vmAuth: AuthViewModel
    lateinit var postcontents:Post
    protected var addtolast=true
    protected var blockingid:Int?=null
    protected abstract val baseCommentViewModel: BaseCommentViewModel
    protected abstract val commentAdapter: CommentAdapter
    protected abstract val postAdapter:PostDetailsAdapter?
    protected abstract val srLayout: SwipeRefreshLayout
    protected abstract val rvComments: RecyclerView
    protected abstract val cbAnony: CheckBox
    protected abstract val edtComment:EditText
    protected abstract val post: Post
    protected abstract val sendcomment:ImageButton
    protected abstract val postcommentprogress:ProgressBar

    var isScrolling=false
    private var anonymousnick:String?=null
    var curdeletingcomm:Comment?=null
    var isLoading=false
    var selecteduserid=0
    lateinit var api: MainApi
    private lateinit var inputMethodManager: InputMethodManager
    @Inject
    lateinit var userPreferences: UserPreferences
    @Inject
    lateinit var loadingDialog: LoadingDialog
    val scrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val lastVisibleItemPosition =
                (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            val totalItemCount = recyclerView.adapter!!.itemCount - 1

            if(commentAdapter.currentList.isNotEmpty())
            {
                if(!recyclerView.canScrollVertically(1)&&( lastVisibleItemPosition == totalItemCount)&&commentAdapter.currentList.last().commentid!=null&&!isLoading&&isScrolling){
                    isScrolling=false
                    loadNewComments()
                }
            }

        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling=true
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //init()

        commentAdapter.setOnFavoriteClickListener { comment->
            togglecomment(comment)
        }

        srLayout.setOnRefreshListener {
            scrollRefresh()
        }

        edtComment.addTextChangedListener {editable->

            editable?.let{
                if(edtComment.text.toString().trim().isEmpty())
                {
                    sendcomment.visibility=View.GONE
                }
                else
                {
                    sendcomment.visibility=View.VISIBLE
                }
            }
        }
        commentAdapter.setOnMenuClickListener { comment->
            var ismine=false

            if(comment.userid==vmAuth.userid.value!!)
            {
                ismine=true
            }
            val interactionDialog = InteractionDialog(ismine){
                when(it)
                {
                    BLOCK->{

                        showBlock(comment)
                    }
                    REPORT->{
                        showReport(null,comment.commentid)

                    }
                    DELETE->{
                        showdeletecomment(comment)

                    }
                    CHAT->{
                        vmInteract.requestchat(comment.userid,UUID.randomUUID().toString(),api)
                    }
                }
            }
            interactionDialog.show(parentFragmentManager,interactionDialog.tag)
        }
        commentAdapter.setOnProfileClickListener { comment->
            if(comment.userid!=vmAuth.userid.value!!)
            {
                selecteduserid=comment.userid
                if(comment.anonymous!="")
                    showprofile(null,comment.gender,null,comment.anonymous!!,true)
                else
                     vmInteract.getuserprofile(comment.userid,api)
            }


        }

        subscribeToObserver()

    }
    protected fun hideKeyboard() {
        if (::inputMethodManager.isInitialized.not()) {
            inputMethodManager=activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        }
        inputMethodManager.hideSoftInputFromWindow(edtComment.windowToken, 0)
    }
    private fun showdeletecomment(comment:Comment){
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="?????????????????????????"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            curdeletingcomm=comment
            if(comment.depth==0)
                baseCommentViewModel.deletecomment(comment.ref,api)
            else
                deletereply(comment.commentid!!)

            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    open fun deletereply(commentid:Int)
    {

    }
    fun showBlock(selectedComment:Comment?)
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="?????????????????????????"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            if(selectedComment==null)
                blockpostuser()
            else
                blockcommentuser(selectedComment!!)

            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    abstract fun blockcommentuser(selectedComment: Comment)

    abstract fun blockpostuser()

    open fun togglecomment(com:Comment)
    {
        if(com.userid==vmAuth.userid.value!!)
            baseCommentViewModel.toggleComment(null,null,null,null,com.commentid!!,com.commentliked,api)
        else
            baseCommentViewModel.toggleComment(null,com.userid,com.depth,com.postid,com.commentid!!,com.commentliked,api)


    }
    protected fun sendComment()
    {
        var anony=""
        anony = if(!cbAnony.isChecked)
            "NONE"
        else
            anonymousnick!!
        postComment(anony)
        edtComment.text = null
    }
    abstract fun scrollRefresh()
    abstract fun loadNewComments()
    abstract fun refreshComments()
    abstract fun postComment(anony:String)
    protected fun init()
    {
        activity?.run{
            vmAuth= ViewModelProvider(this)[AuthViewModel::class.java]
        }
        api= RemoteDataSource().buildApi(MainApi::class.java, runBlocking { userPreferences.authToken.first() })
        getMyAnony()
        setupRecyclerView()
    }
    private fun getMyAnony()
    {
        if(post.userid==vmAuth.userid.value!!&&post.anonymous!=null)
        {
            //????????? ????????????
            baseCommentViewModel.setAnony(post.anonymous!!)
        }
        else
        {
            baseCommentViewModel.getAnonymous(post.postid!!,api)
        }
    }
    abstract fun setupRecyclerView()
    abstract fun applyList(comments:List<Comment>)
    abstract fun shownotexist()
    abstract fun fixtotop(postedcomment:Comment)
    fun showprofile(profileimage:String?,gender:String,age:Int?,nickname:String,anonymous:Boolean)
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_profile,null)
        val requestchat: Button =mView.findViewById(R.id.chatbtn)
        val nickText: TextView =mView.findViewById(R.id.tvnickname)
        val agegenderText:TextView=mView.findViewById(R.id.tvagegender)
        val profileimg:ImageView=mView.findViewById(R.id.imgProfile)
        if(anonymous)
        {
            nickText.text= "??????[$nickname]"
            agegenderText.text="????????? ?? $gender"
            when(gender)
            {
                "??????"->profileimg.setImageResource(R.drawable.icon_male)
                "??????"->profileimg.setImageResource(R.drawable.icon_female)
                else->profileimg.setImageResource(R.drawable.icon_none)
            }
        }
        else
        {

            nickText.text=nickname
            agegenderText.text="${getAge(age!!)} ?? $gender"
            //????????? ????????????
            if(profileimage==null)
            {
                when(gender)
                {
                    "??????"->profileimg.setImageResource(R.drawable.icon_male)
                    "??????"->profileimg.setImageResource(R.drawable.icon_female)
                    else->profileimg.setImageResource(R.drawable.icon_none)
                }
            }
            else
            {
                profileimg.setOnClickListener {
                    val bundle=Bundle()
                    bundle.putString("image",profileimage)
                    (activity as MainActivity).replaceFragment("imageFragment",ImageFragment(),bundle)
                    dialog.dismiss()
                    dialog.cancel()
                }
                Glide.with(requireContext())
                    .load(profileimage)
                    .placeholder(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.gray)))
                    .error(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.gray)))
                    .into(profileimg)
            }
        }
        requestchat.setOnClickListener {
            vmInteract.requestchat(selecteduserid,UUID.randomUUID().toString(),api)
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    open fun subscribeToObserver()
    {
        vmInteract.requestchatResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
              loadingDialog.show()
            },
            onError={
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode){
                when(it.resultCode)
                {
                    200->Toast.makeText(requireContext(),"?????? ????????? ?????????????????????",Toast.LENGTH_SHORT).show()
                    300->Toast.makeText(requireContext(),"??????????????? ??????????????? ????????? ???????????????",Toast.LENGTH_SHORT).show()
                    500->Toast.makeText(requireContext(),"??????????????? ??????????????? ???????????? ???????????????",Toast.LENGTH_SHORT).show()
                    else->Toast.makeText(requireContext(),"?????????????????? ?????? ????????? ??????????????????",Toast.LENGTH_SHORT).show()
                }
            }

        })
        vmInteract.getprofileResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
              loadingDialog.show()
            },
            onError={
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==200)
                {
                    showprofile(it.profileimage,it.gender,it.age,it.nickname,false)
                }
                else
                    snackbar("???????????? ??????")
            }


        })
        vmInteract.reportResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==200)
                {
                    Toast.makeText(requireContext(),"????????? ?????????????????????",Toast.LENGTH_SHORT).show()
                }
                else
                    Toast.makeText(requireContext(),"?????? ????????? ??????????????????",Toast.LENGTH_SHORT).show()
            }

        })
        vmInteract.blockuserResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                loadingDialog.show()
            },
            onError={
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            blockingid?.let{
                (activity as MainActivity).deleteBlockedcChatUser(it)
            }
            handleResponse(requireContext(),it.resultCode){
                Toast.makeText(requireContext(),"????????? ?????????????????????",Toast.LENGTH_SHORT).show()
                if(it.resultCode==300)
                    parentFragmentManager.popBackStack()
                if(it.resultCode==200)
                {
                        refreshComments()
                }
            }

        })
        baseCommentViewModel.toggleCommentResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading={
              loadingDialog.show()
            },
            onError={
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==100)
                {
                    snackbar("????????? ???????????????")
                }
                else
                {
                    for(i in commentAdapter.currentList.indices)
                    {
                        if(it.value==commentAdapter.currentList[i].commentid)
                        {
                            commentAdapter.currentList[i].apply {
                                if(this.commentliked==0)
                                {
                                    this.commentliked=1
                                    this.likecount+=1
                                }
                                else
                                {
                                    this.commentliked=0
                                    this.likecount-=1
                                }
                            }
                            commentAdapter.notifyItemChanged(i)
                            break
                        }
                    }
                }
            }
        })
        baseCommentViewModel.postCommentResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                loadingDialog.show()
                sendcomment.visibility=View.GONE
            },
            onError={
                snackbar(it)
                loadingDialog.dismiss()
            }

        ){

            loadingDialog.dismiss()
            postAdapter?.let{ adapter->
                if(adapter.noCommentVis)
                {
                    adapter.noCommentVis=false
                    adapter.rgCommentVis=true
                    rvComments.findViewHolderForAdapterPosition(0)?.let { holder ->
                        (holder as PostDetailsAdapter.postViewHolder).binding.rgcomment.visibility=View.VISIBLE
                        (holder as PostDetailsAdapter.postViewHolder).binding.noComment.visibility=View.GONE
                    }

                }
            }
            //postcommentprogress.visibility=View.GONE
            handleResponse(requireContext(),it.resultCode){
                when(it.resultCode)
                {
                    100->shownotexist()
                    400-> Toast.makeText(requireContext(),"?????? ???????????? ????????? ???????????? ????????????.",Toast.LENGTH_SHORT).show()
                    500->Toast.makeText(requireContext(),"?????? ????????? ????????? ???????????? ????????????",Toast.LENGTH_SHORT).show()
                    else-> {
                        val postcontent=postcontents
                        postcontent.commentcount+=1
                        postcontents=postcontent
                        val postedcomment=it.comments[0]
                        postedcomment.topfixed=true
                        fixtotop(it.comments[0])
                    }
                }
            }
        })
        baseCommentViewModel.getCommentResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading={
                isLoading=true
                val templist=commentAdapter.currentList.toMutableList()
                templist+=listOf(Comment(false,null,"",0,"",0,"",
                    0,"","","","","",0,"",0,0,0))
                commentAdapter.submitList(templist.toList())
            },
            onError = {
                addtolast=true
                isLoading=false
                val templist=commentAdapter.currentList
                commentAdapter.submitList(templist.filter { comment -> comment.commentid!=null  })
                snackbar(it)
            }
        ){
            if(srLayout.isRefreshing)
                srLayout.isRefreshing=false

            handleResponse(requireContext(),it.resultCode){

                if(it.resultCode==200) {
                    if(commentAdapter.currentList.size==0)
                    {
                        rvComments.scrollToPosition(0)
                    }
                    if(commentAdapter.currentList.none { comment -> comment.commentid != null }) {
                        postAdapter?.let{ adapter->
                                adapter.noCommentVis=false
                        }

                    }
                    var templist=commentAdapter.currentList.toList()
                    postAdapter?.let{ adapter->
                            adapter.rgCommentVis=true
                    }
                    if(!addtolast)
                        templist=it.comments
                    else
                        templist+=it.comments
                    applyList(templist.filter{ comment-> !comment.topfixed&&comment.commentid!=null})

                    isLoading=false
                }
                else{
                    //=true
                    isLoading=false
                    val templist=commentAdapter.currentList
                    commentAdapter.submitList(templist.filter { comment -> comment.commentid!=null  })
                    if(commentAdapter.currentList.none { comment -> comment.commentid != null }) {
                        postAdapter?.let{ adapter->
                            adapter.noCommentVis=true
                            adapter.rgCommentVis=false
                        }
                    }
                }
                postAdapter?.let{ adapter->
                    rvComments.findViewHolderForAdapterPosition(0)?.let{ holder->
                        if(adapter.noCommentVis)
                            (holder as PostDetailsAdapter.postViewHolder).binding.noComment.visibility=View.VISIBLE
                        else
                            (holder as PostDetailsAdapter.postViewHolder).binding.noComment.visibility=View.GONE
                        if(adapter.rgCommentVis)
                            holder.binding.rgcomment.visibility=View.VISIBLE
                        else
                            holder.binding.rgcomment.visibility=View.GONE


                    }
                }
                addtolast=true
            }
        })
        baseCommentViewModel.anonymousnick.observe(viewLifecycleOwner){
            anonymousnick=it
        }
        baseCommentViewModel.getAnonymousResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==100)
                {
                    baseCommentViewModel.setAnony(genAnonymous())
                }
                else{
                    baseCommentViewModel.setAnony(it.message)
                }
            }
        })
    }
    fun showReport(postid:String?,commentid:Int?)
    {
        val dialog = AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView : View = edialog.inflate(R.layout.dialog_report,null)
        val rgReport:RadioGroup=mView.findViewById(R.id.rgReport)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val report : Button = mView.findViewById(R.id.report)

        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }

        //  ?????? ?????? ?????? ???
        report.setOnClickListener {

            val reporttype=when(rgReport.checkedRadioButtonId){
                R.id.rb1->"??????/??????/??????/??? sns??? ??????"
                R.id.rb2->"???????????? ??????"
                R.id.rb3->"??????,??????"
                R.id.rb4->"????????? ????????? ??????"
                R.id.rb5->"??????,??????????????? ???????????? ??????"
                R.id.rb6->"????????? ?????? ??????"
                R.id.rb7->"????????? ??????"
                else->""
            }
            vmInteract.report(postid,commentid,reporttype,api)

            dialog.dismiss()
            dialog.cancel()
        }

        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun genAnonymous():String
    {
        var resultstr=""

        resultstr=""
        val datas=listOf("a", "b", "c", "d", "e","f","g","h","i","j","1","2","3","4","0","5","6","7","8","9")
        for(i in 0 until 6)
        {
            resultstr+=datas.random()
        }

        return resultstr
    }
}