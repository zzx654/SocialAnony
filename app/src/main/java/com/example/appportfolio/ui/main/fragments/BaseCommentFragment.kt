package com.example.appportfolio.ui.main.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.getAge
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.CommentAdapter
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
import com.example.appportfolio.ui.main.viewmodel.BaseCommentViewModel
import com.example.appportfolio.ui.main.viewmodel.interactViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

abstract class BaseCommentFragment (layoutId:Int
): Fragment(layoutId) {
    val vmInteract: interactViewModel by viewModels()
    lateinit var vmAuth: AuthViewModel
    lateinit var postcontents:Post
    protected var addtolast=true
    protected abstract val baseCommentViewModel: BaseCommentViewModel
    protected abstract val commentAdapter: CommentAdapter
    protected abstract val srLayout: SwipeRefreshLayout
    protected abstract val scrollView: NestedScrollView
    protected abstract val rvComments: RecyclerView
    protected abstract val cbAnony: CheckBox
    protected abstract val edtComment:EditText
    protected abstract val post: Post
    protected abstract val sendcomment:ImageButton
    protected abstract val postcommentprogress:ProgressBar
    protected abstract val commentprogress:ProgressBar
    protected abstract val noComment:ConstraintLayout?
    protected abstract val rgComment:RadioGroup?
    var anonymousnick:String?=null
    var curdeletingcomm:Comment?=null
    var isLoading=false
    var isLast=false
    var beforeitemssize=0
    var lastcomment=0
    var selecteduserid=0
    lateinit var api: MainApi
    lateinit var inputMethodManager: InputMethodManager
    @Inject
    lateinit var userPreferences: UserPreferences
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //init()
        scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if(!v.canScrollVertically(1)){
                if(!isLoading&&lastcomment!=commentAdapter.comments.last().commentid) {
                    lastcomment=commentAdapter.comments.last().commentid
                    loadNewComments()
                }
            }
        }
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
            val interactionDialog: InteractionDialog = InteractionDialog(ismine){
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
    fun showdeletecomment(comment:Comment){
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="삭제하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            curdeletingcomm=comment
            if(comment.depth==0)
                baseCommentViewModel.deletecomment(comment.ref,api)
            else
                deletereply(comment.commentid)

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
        alertText.text="차단하시겠습니까?"
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
    open fun blockcommentuser(selectedComment: Comment)
    {
    }
    open fun blockpostuser()
    {
    }
    open fun togglecomment(com:Comment)
    {
        if(com.userid==vmAuth.userid.value!!)
            baseCommentViewModel.toggleComment(null,null,null,null,null,com.commentid,com.commentliked,api)
        else
            baseCommentViewModel.toggleComment(null,com.userid,com.depth,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),com.postid,com.commentid,com.commentliked,api)


    }
    protected fun sendComment()
    {
        var anony=""
        if(!cbAnony.isChecked)
            anony="NONE"
        else
            anony=anonymousnick!!
        postComment(anony)
        edtComment.setText(null)
    }
    open fun scrollRefresh()
    {
    }
    open fun loadNewComments()
    {

    }
    open fun refreshComments()
    {
    }
    open fun postComment(anony:String)
    {

    }
    protected fun init()
    {
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        api= RemoteDataSource().buildApi(MainApi::class.java, runBlocking { userPreferences.authToken.first() })
        getMyAnony()
        setupRecyclerView()
    }
    private fun getMyAnony()
    {
        if(post.userid==vmAuth.userid.value!!&&post.anonymous!=null)
        {
            //익명닉 가져가기
            baseCommentViewModel.setAnony(post.anonymous!!)
        }
        else
        {
            baseCommentViewModel.getAnonymous(post.postid!!,api)
        }
    }

    private fun setupRecyclerView()=rvComments.apply{
        adapter=commentAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
    }
    open fun applyList(comments:List<Comment>)
    {

    }
    open fun shownotexist()
    {
    }
    open fun fixtotop(postedcomment:Comment)
    {

    }
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
            nickText.text="익명["+nickname+"]"
            agegenderText.text="비공개 · ${gender}"
            when(gender)
            {
                "남자"->profileimg.setImageResource(R.drawable.icon_male)
                "여자"->profileimg.setImageResource(R.drawable.icon_female)
                else->profileimg.setImageResource(R.drawable.icon_none)
            }
        }
        else
        {

            nickText.text=nickname
            agegenderText.text="${getAge(age!!)} · ${gender}"
            //익명이 아닌경우
            if(profileimage==null)
            {
                when(gender)
                {
                    "남자"->profileimg.setImageResource(R.drawable.icon_male)
                    "여자"->profileimg.setImageResource(R.drawable.icon_female)
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

            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode){
                when(it.resultCode)
                {
                    200->Toast.makeText(requireContext(),"대화 요청이 완료되었습니다",Toast.LENGTH_SHORT).show()
                    300->Toast.makeText(requireContext(),"해당유저가 대화요청을 차단한 상태입니다",Toast.LENGTH_SHORT).show()
                    500->Toast.makeText(requireContext(),"해당유저를 차단했거나 차단당한 상태입니다",Toast.LENGTH_SHORT).show()
                    else->Toast.makeText(requireContext(),"해당유저에게 이미 대화를 요청했습니다",Toast.LENGTH_SHORT).show()
                }
            }

        })
        vmInteract.getprofileResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==200)
                {
                    showprofile(it.profileimage,it.gender,it.age,it.nickname,false)
                }
                else
                    snackbar("서버오류 발생")
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
                    Toast.makeText(requireContext(),"신고가 접수되었습니다",Toast.LENGTH_SHORT).show()
                }
                else
                    Toast.makeText(requireContext(),"서버 오류가 발생했습니다",Toast.LENGTH_SHORT).show()
            }

        })
        vmInteract.blockuserResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode){
                Toast.makeText(requireContext(),"차단이 완료되었습니다",Toast.LENGTH_SHORT).show()
                if(it.resultCode==300)
                    parentFragmentManager.popBackStack()
                if(it.resultCode==200)
                {
                        refreshComments()
                }
            }

        })
        baseCommentViewModel.toggleCommentResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==100)
                {
                    snackbar("삭제된 댓글입니다")
                }
                else
                {
                    for(i in commentAdapter.comments.indices)
                    {
                        if(it.value==commentAdapter.comments[i].commentid)
                        {
                            commentAdapter.comments[i].apply {
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
                if(!commentAdapter.differ.currentList.isEmpty())
                    postcommentprogress.visibility=View.VISIBLE
                sendcomment.visibility=View.GONE
            },
            onError={
                snackbar(it)
                postcommentprogress.visibility=View.GONE
            }

        ){

            postcommentprogress.visibility=View.GONE
            handleResponse(requireContext(),it.resultCode){
                when(it.resultCode)
                {
                    100->shownotexist()
                    400-> Toast.makeText(requireContext(),"해당 게시물에 댓글을 게시할수 없습니다.",Toast.LENGTH_SHORT).show()
                    500->Toast.makeText(requireContext(),"해당 댓글에 답글을 게시할수 없습니다",Toast.LENGTH_SHORT).show()
                    else-> {
                        var postcontent=postcontents
                        postcontent.commentcount+=1
                        postcontents=postcontent
                        var postedcomment=it.comments[0]
                        postedcomment.topfixed=true
                        fixtotop(it.comments[0])
                    }
                }
            }
        })
        baseCommentViewModel.getCommentResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading={
                isLoading=true
                commentprogress.visibility=View.VISIBLE

            },
            onError = {
                addtolast=true
                isLoading=false
                commentprogress.visibility = View.GONE
                snackbar(it)
            }
        ){
            if(srLayout.isRefreshing)
                srLayout.isRefreshing=false

            handleResponse(requireContext(),it.resultCode){

                if(it.resultCode==200) {
                    if(commentAdapter.differ.currentList.isEmpty())
                    {
                        noComment?.let{it.visibility=View.GONE}
                    }
                    var templist=commentAdapter.differ.currentList.toList()
                    if(addtolast)
                        beforeitemssize=templist.size
                    else
                        beforeitemssize=0
                    if(beforeitemssize==0)
                        rgComment?.let{it.visibility=View.VISIBLE}
                    if(!addtolast)
                        templist=it.comments
                    else
                        templist+=it.comments
                        commentprogress.visibility=View.GONE

                    applyList(templist.filter{ comment-> !comment.topfixed})

                    isLoading=false
                }
                else{
                    lastcomment=0
                    //=true
                    isLoading=false
                    commentprogress.visibility=View.GONE
                    if(commentAdapter.differ.currentList.isEmpty()) {
                        noComment?.let{it.visibility = View.VISIBLE}
                        rgComment?.let{it.visibility=View.GONE}
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

        //  완료 버튼 클릭 시
        report.setOnClickListener {

            val reporttype=when(rgReport.checkedRadioButtonId){
                R.id.rb1->"만남/전화/구인/타 sns로 유도"
                R.id.rb2->"무분별한 비난"
                R.id.rb3->"욕설,비방"
                R.id.rb4->"상업적 목적의 내용"
                R.id.rb5->"음란,청소년에게 부적절한 내용"
                R.id.rb6->"사회적 갈등 조장"
                R.id.rb7->"불쾌한 내용"
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
    fun getTodayString(format: SimpleDateFormat):String
    {
        var today= Calendar.getInstance()
        var todaystr= datetostr(today.time,format)

        return todaystr
    }
    private fun datetostr(date: Date, format: SimpleDateFormat):String
    {
        return format.format(date)
    }
}