package com.example.appportfolio.ui.main.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.adapters.CommentAdapter
import com.example.appportfolio.adapters.ImagesAdapter
import com.example.appportfolio.adapters.VoteResultAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.*
import com.example.appportfolio.databinding.FragmentPostBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.TimeValue
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.services.audioService
import com.example.appportfolio.ui.main.viewmodel.*
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.round
import kotlin.math.roundToInt

@AndroidEntryPoint
class PostFragment: BaseCommentFragment(R.layout.fragment_post) {
    override val baseCommentViewModel: BaseCommentViewModel
        get() {
            val vm: CommentViewModel by viewModels()
            return vm
        }
    protected val vmComment: CommentViewModel
        get() = baseCommentViewModel as CommentViewModel
    override val srLayout: SwipeRefreshLayout
        get() = binding.srLayout
    override val scrollView: NestedScrollView
        get() = binding.scrollView
    override val rvComments: RecyclerView
        get() = binding.rvComment
    override val cbAnony: CheckBox
        get() = binding.cbAnony
    override val edtComment: EditText
        get() = binding.edtComment
    override val sendcomment: ImageButton
        get() = binding.sendcomment
    override val postcommentprogress: ProgressBar
        get() = binding.postcommentprogress
    override val commentprogress: ProgressBar
        get() = binding.commentprogress
    override val noComment: ConstraintLayout?
        get() = binding.noComment
    override val rgComment: RadioGroup?
        get() = binding.rgcomment
    override val post: Post
        get() = postcontents

    override val commentAdapter:CommentAdapter
        get() = commentadapter
    @Inject
    lateinit var imagesAdapter: ImagesAdapter
    @Inject
    lateinit var loadingDialog: LoadingDialog
    @Inject
    lateinit var preferences: UserPreferences

    lateinit var commentadapter: CommentAdapter

    lateinit var voteresultadapter:VoteResultAdapter

    lateinit var binding:FragmentPostBinding
    var aService:audioService?=null

    var connection=object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as audioService.mBinder
            aService=binder.getService()
            aService?.setMedia(post.audio, (activity as MainActivity).binding.title.text.toString())
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("serviceDisabled","서비스 비정상종료")
        }
    }
    private var mRootView:View?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postcontents=arguments?.getParcelable<Post>("post")!!
        (activity as MainActivity).setToolBarVisible("postFragment")
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null){
            binding= DataBindingUtil.inflate<FragmentPostBinding>(inflater,
                R.layout.fragment_post,container,false)


            sendcomment.setOnClickListener {
                addtolast=false
                sendComment()
                hideKeyboard()
            }
            commentadapter=CommentAdapter()
            init()
            voteresultadapter=VoteResultAdapter()

            if(!post.audio.equals("NONE"))
            {
                servicebind()
                binding.playpause.visibility=View.VISIBLE
                binding.progressMedia.visibility=View.VISIBLE
            }
            else
            {
                binding.playpause.visibility=View.GONE
                binding.progressMedia.visibility=View.GONE
            }
            binding.playpause.setOnClickListener {
                aService?.toggle_play()
            }
            rgComment?.setOnCheckedChangeListener { group, checkedId ->
                addtolast=false
                refreshComments()
            }
            commentAdapter.setOnrootClickListener { comment->
                vmComment.checkSelectedComment(comment.userid,post.userid,comment.commentid,comment.postid,api)
            }
            binding.rgVote.setOnCheckedChangeListener { group, checkedId ->
                val color = getColor(requireContext(),R.color.skinfore)
                binding.btnVote.isEnabled=true
                binding.btnVote.setBackgroundColor(color)
            }
            binding.btnVote.onSingleClick {

                vmInteract.vote(post.postid!!,binding.rgVote.checkedRadioButtonId,api)

            }
            binding.imgProfile.setOnClickListener {
                if(post.userid!=vmAuth.userid.value!!)
                {
                    selecteduserid=post.userid
                    if(post.anonymous!="")
                        showprofile(null,post.gender!!,null,post.anonymous!!,true)
                    else
                        vmInteract.getuserprofile(post.userid,api)
                }
            }
            binding.likepost.onSingleClick {
                toggleLike()
            }
            binding.bookmarkpost.onSingleClick {
                toggleBookmark()
            }
            setupRecyclerView()
            bindPostInfo()
            binding.fragment=this@PostFragment
            mRootView=binding.root

        }


        return mRootView
    }

    private fun bindvote()
    {
        if(postcontents.vote=="exist")
        {
            if(post.userid!=vmAuth.userid.value!!)
            {
                vmInteract.getpolloptions(post.postid!!,api)
            }
            else
            {
                vmInteract.getvoteresult(post.postid!!,api)
            }
        }
    }
    override fun scrollRefresh() {
        super.scrollRefresh()
        addtolast=false
        vmInteract.getSelectedPost(post.postid!!,null,null,api)

    }
    private fun bindPostInfo()
    {
        bindTags()
        bindProfileImage()
        bindSpaceTime()
        bindText()
        bindLikeCommentCount()
        bindLikeBookmark()
        setupViewPager()
        bindImages()
    }
    private fun setupRecyclerView(){
        binding.rvComment.apply{
            adapter=commentAdapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
        }
        binding.rvvote.apply {
            adapter=voteresultadapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
        }

    }
    override fun loadNewComments()
    {
        val curComments=commentAdapter.differ.currentList
        if(!curComments.isEmpty())
        {
            val lastComment=curComments.last()
        when(rgComment!!.checkedRadioButtonId){
            R.id.hotcomment->{
                //최신순
                vmComment.getHotComment(lastComment.commentid,post.postid!!,lastComment.likecount,api)
            }
            R.id.timecomment->{
                //등록순
                vmComment.getComment(lastComment.commentid,post.postid!!,lastComment.time,api)
            }
        }
        }
    }
    override fun applyList(comments: List<Comment>) {
        super.applyList(comments)
        commentAdapter.differ.submitList(comments)
    }
    override fun refreshComments()
    {
        isLast=false
        when(rgComment!!.checkedRadioButtonId){
            R.id.hotcomment->{
                //최신순
                vmComment.getHotComment(null,post.postid!!,null,api)
            }
            R.id.timecomment->{
                //등록순
                vmComment.getComment(null,post.postid!!,null,api)
            }
        }
    }
    fun toggleLike()
    {
        var togglemy=post.userid==vmAuth.userid.value!!
        vmInteract.toggleLikePost(getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),togglemy,post.userid,post.postid!!,post.isLiked!!,api)
    }
    fun toggleBookmark()
    {
        vmInteract.toggleBookmarkPost(post.postid!!,post.bookmarked!!,api)
    }
    private fun setupViewPager()=binding.vpimg.apply{
        adapter=imagesAdapter
        orientation= ViewPager2.ORIENTATION_HORIZONTAL
        offscreenPageLimit=1
        getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
    }
    private fun bindImages()
    {
        if(!post.image.equals("NONE"))
        {
            binding.vpimg.visibility=View.VISIBLE
            var lst:List<String> = listOf()
            val array= JSONArray(post.image)
            for(i in 0 until array.length())
            {
                val jsonObj=array.getJSONObject(i)
                val image=jsonObj.optString("imageUri")
                lst+=image
            }
            if(lst.size>1)
                setupIndicators(lst.size)
            imagesAdapter.differ.submitList(lst)
        }
    }
    private fun setupIndicators(count:Int){
        val params= LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(6,20,6,8)

        for(i in 0..count-1)
        {
            var indicator=ImageView(requireContext())
            indicator.setImageDrawable(ContextCompat.getDrawable(requireContext(),
                R.drawable.bg_indicator_inactive))
            indicator.layoutParams=params
            binding.layoutIndicators.addView(indicator)
        }
        setCurrentIndicator(0)
    }
    private fun setCurrentIndicator(position:Int){

        val childCount=binding.layoutIndicators.childCount
        for(i in 0..childCount-1){
            val imageView=binding.layoutIndicators.getChildAt(i) as ImageView
            if(i==position){
                imageView.setImageDrawable(ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_indicator_active
                ))
            }else{
                imageView.setImageDrawable(ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_indicator_inactive
                ))
            }
        }
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        if(post.anonymous!="")
        {
            (activity as MainActivity).binding.title.text="익명["+post.anonymous+"]"
            binding.tvNick.text="익명["+post.anonymous+"]"
        }
        else
        {
            (activity as MainActivity).binding.title.text=post.nickname
            binding.tvNick.text=post.nickname
        }
        super.onResume()
    }
    override fun postComment(anony: String) {
        var postuserid:Int?
        if(post.userid==vmAuth.userid.value!!)
            postuserid=null
        else
            postuserid=post.userid
        vmComment.postComment(postuserid,post.postid!!,getTodayString(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),anony,edtComment.text.toString(),api)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.post_tools, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        menu.findItem(R.id.delete).isVisible = post.userid==vmAuth.userid.value!!
        menu.findItem(R.id.requestChat).isVisible=post.userid!=vmAuth.userid.value!!
        menu.findItem(R.id.block).isVisible=post.userid!=vmAuth.userid.value!!
        menu.findItem(R.id.report).isVisible=post.userid!=vmAuth.userid.value!!
        bindvote()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
            R.id.requestChat->{
                vmInteract.requestchat(post.userid,UUID.randomUUID().toString(),api)
            }
            R.id.block->{
                showBlock(null)
            }
            R.id.delete->{
                showdeletepost()
            }
            R.id.report->{
                showReport(post.postid,null)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showdeletepost()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="게시물을 삭제하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            vmInteract.deletepost(post.postid!!,api)

            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    override fun blockcommentuser(selectedComment: Comment) {
        super.blockcommentuser(selectedComment)
        var anonymous:Boolean=false
        anonymous= selectedComment.anonymous!=null
        vmInteract.blockcommentuser(anonymous,selectedComment.userid,selectedComment.platform==post.platform&&selectedComment.account==post.account,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),api)
    }

    override fun blockpostuser() {
        super.blockpostuser()
        var anonymous:Boolean=false
        anonymous= post.anonymous!=null

        vmInteract.blockpostuser(anonymous,post.userid,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),api)
    }
    private fun bindTags()
    {
        binding.cgTag.removeAllViews()
        post.tags?.let{
            binding.cgTag.visibility= View.VISIBLE
            var tags:List<String> = listOf()
            if(it.contains("#"))
                tags=it.split("#")
            else
                tags+=it

            for(tag in tags)
            {
                val chip= Chip(binding.cgTag.context).apply{
                    text="#"+tag
                    chipStrokeWidth=0f
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP , 16f)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        chipBackgroundColor=
                            AppCompatResources.getColorStateList(binding.cgTag.context, R.color.chipback)
                        setChipStrokeColorResource(R.color.black)
                        setTextColor(ContextCompat.getColor(binding.cgTag.context, R.color.chiptext))
                        setTextSize(12f)
                    }
                    setOnClickListener {
                        val bundle=Bundle()
                        bundle.putString("tag",tag)
                        (activity as MainActivity).replaceFragment("tagPostsFragment",TagPostsFragment(),bundle)
                    }
                }
                binding.cgTag.addView(chip)
            }
        }
    }
    private fun bindProfileImage()
    {
        if(post.anonymous!="")
        {
            when(post.gender)
            {
                "남자"->binding.imgProfile.setImageResource(R.drawable.icon_male)
                "여자"->binding.imgProfile.setImageResource(R.drawable.icon_female)
                else->binding.imgProfile.setImageResource(R.drawable.icon_none)
            }
        }
        else
        {
            //익명이 아닌경우
            if(post.profileimage==null)
            {
                when(post.gender)
                {
                    "남자"->binding.imgProfile.setImageResource(R.drawable.icon_male)
                    "여자"->binding.imgProfile.setImageResource(R.drawable.icon_female)
                    else->binding.imgProfile.setImageResource(R.drawable.icon_none)
                }
            }
            else
            {
                Glide.with(requireContext())
                    .load(post.profileimage)
                    .into(binding.imgProfile)
            }
        }

    }
    private fun bindText()
    {
        binding.text.text=post.text
    }
    private fun bindSpaceTime()
    {
        val formatter= SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val trans_date=formatter.parse(post.date)
        val postedmillis=trans_date.time
        val curTime=System.currentTimeMillis()
        var diffTime=(curTime-postedmillis)/1000
        var str:String?=null
        if(diffTime< TimeValue.SEC.value)
            str="방금 전"
        else{
            for(i in TimeValue.values()){
                diffTime/=i.value
                if(diffTime<i.maximum){
                    str=diffTime.toString()+i.msg
                    break
                }
            }
        }
        if (post.distance != null) {
            if(post.distance!!>=0)
            {
                str=str+" · "+(round(post.distance!!).toInt()).toString()+"km"
            }
        }
        binding.tvspacetime.text=str

    }

    override fun shownotexist() {
        super.shownotexist()
        snackbar("삭제된 게시물입니다")
    }
    private fun bindLikeCommentCount()
    {
        if(post.commentcount==0)
        {
            noComment!!.visibility=View.VISIBLE
        }
        else{
            rgComment!!.visibility=View.VISIBLE
            refreshComments()

        }
        binding.likecount.text="좋아요 ${post.likecount}개"
        binding.commentcount.text="댓글 ${post.commentcount}개"
    }
    private fun bindLikeBookmark()
    {
        if(post.isLiked==1)
            binding.imgLike.setImageResource(R.drawable.favorite_on)
        else
            binding.imgLike.setImageResource(R.drawable.favorite_off)
        if(post.bookmarked==1)
            binding.imgBookmark.setImageResource(R.drawable.ic_bookmarkon)
        else
            binding.imgBookmark.setImageResource(R.drawable.ic_bookmarkoff)

    }
    override fun subscribeToObserver()
    {
        super.subscribeToObserver()

       vmInteract.getVoteResultResponse.observe(viewLifecycleOwner,Event.EventObserver(

           onLoading={
             binding.loadvote.visibility=View.VISIBLE
               binding.votelayout.visibility=View.GONE
           },
           onError={
               snackbar(it)
               binding.loadvote.visibility=View.GONE
           }
       ){
           binding.loadvote.visibility=View.GONE
           handleResponse(requireContext(),it.resultCode) {
               if (it.resultCode == 200) {

                   binding.rgVote.visibility = View.GONE
                   binding.votelayout.visibility = View.VISIBLE
                   binding.rvvote.visibility = View.VISIBLE
                   binding.btnVote.visibility = View.GONE
                   var sum = 0
                   val vresult: List<Voteresult> = it.voteresult
                   for (i in it.voteresult) {
                       sum += i.votecount
                   }
                   for (i in it.voteresult.indices) {
                       if (it.voteresult[i].votecount == 0)
                           vresult[i].proportion = 0
                       else {
                           val a: Double =
                               (it.voteresult[i].votecount.toDouble() / sum.toDouble()) * 100
                           vresult[i].proportion = a.roundToInt()
                       }

                   }
                   voteresultadapter.differ.submitList(vresult)

               }
           }

       })
        vmInteract.getPollResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
              binding.loadvote.visibility=View.VISIBLE
            },
            onError={
                snackbar(it)
                binding.loadvote.visibility=View.GONE
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    binding.loadvote.visibility=View.GONE
                    binding.votelayout.visibility = View.VISIBLE

                    for (i in it.polloptions) {
                        val radiobtn = RadioButton(requireContext())
                        radiobtn.apply {
                            val colorList = ColorStateList(
                                arrayOf(
                                    intArrayOf(-android.R.attr.state_enabled),  // Disabled
                                    intArrayOf(android.R.attr.state_enabled)    // Enabled
                                ),
                                intArrayOf(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.inactive
                                    ),     // The color for the Disabled state
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.skinfore
                                    )      // The color for the Enabled state
                                )
                            )
                            this.buttonTintList = colorList
                            val param = RadioGroup.LayoutParams(
                                RadioGroup.LayoutParams.WRAP_CONTENT,
                                RadioGroup.LayoutParams.WRAP_CONTENT
                            )
                            this.text = i.choicetext

                            this.id = i.optionid
                            binding.rgVote.addView(radiobtn, param)

                        }
                    }
                } else if (it.resultCode == 300) {
                    //투표결과얻어오기
                    vmInteract.getvoteresult(post.postid!!, api)
                }
            }

        })
        vmComment.deletecommentResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    var templist=commentAdapter.differ.currentList.toList()
                    templist-=curdeletingcomm
                    commentAdapter.differ.submitList(templist)
                } else {
                    snackbar("서버 오류가 발생했습니다")
                }
            }

        })
        vmInteract.deletepostResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
              loadingDialog.show()
            },
            onError = {
                snackbar(it)
                loadingDialog.dismiss()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    Toast.makeText(requireContext(), "게시물이 삭제되었습니다", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    snackbar("서버 오류가 발생했습니다")
                }
            }
        })
        vmComment.checkSelectedCommentResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError = {
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                when (it.resultCode) {
                    100 -> Toast.makeText(requireContext(), "해당 댓글은 삭제되었습니다", Toast.LENGTH_SHORT)
                        .show()
                    400 -> Toast.makeText(
                        requireContext(),
                        "해당 게시물에 댓글을 게시할수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    500 -> Toast.makeText(
                        requireContext(),
                        "해당 댓글에 답글을 게시할수 없습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        val bundle=Bundle()
                        bundle.putParcelable("comment",it.comments[0])
                        bundle.putParcelable("post",post)
                        (activity as MainActivity).replaceFragment("replyFragment",ReplyFragment(),bundle)

                    }
                }
            }
        })
        vmInteract.getPostResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            if(srLayout.isRefreshing)
                srLayout.isRefreshing=false
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 100) {
                    Toast.makeText(requireActivity(), "삭제된 게시물입니다", Toast.LENGTH_SHORT).show()
                } else {
                    val temppost = postcontents

                    postcontents = it.posts[0]
                    postcontents.distance = temppost.distance
                    bindPostInfo()
                    bindvote()
                }
            }
        })
        vmInteract.toggleLikeResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 100) {
                    Toast.makeText(requireContext(), "삭제된 글입니다", Toast.LENGTH_SHORT).show()
                } else {
                    if (it.toggle == 1) {
                        post.likecount = post.likecount - 1
                        binding.likecount.text = "좋아요 ${post.likecount}개"
                        post.isLiked = 0
                        binding.imgLike.setImageResource(R.drawable.favorite_off)
                    } else {
                        post.likecount = post.likecount + 1
                        binding.likecount.text = "좋아요 ${post.likecount}개"
                        post.isLiked = 1
                        binding.imgLike.setImageResource(R.drawable.favorite_on)
                    }
                }
            }
        })
        vmInteract.toggleBookmarkResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 100) {
                    Toast.makeText(requireContext(), "삭제된 글입니다", Toast.LENGTH_SHORT).show()
                } else {
                    if (it.toggle == 1) {
                        post.bookmarked = 0
                        binding.imgBookmark.setImageResource(R.drawable.ic_bookmarkoff)
                    } else {
                        post.bookmarked = 1
                        binding.imgBookmark.setImageResource(R.drawable.ic_bookmarkon)
                    }
                }
            }
        })
        audioService.isplaying.observe(viewLifecycleOwner){

            if(it==false)
            {
                binding.playpause.setImageResource(R.drawable.ic_play)
            }
            else
            {
                binding.playpause.setImageResource(R.drawable.ic_pause)
            }

        }
        audioService.mediamax.observe(viewLifecycleOwner){
            binding.progressMedia.max=it
        }
        audioService.curpos.observe(viewLifecycleOwner,Event.EventObserver(

        ){
            binding.progressMedia.progress=it
        })
    }
    private fun servicebind()
    {
        var intent=Intent(requireContext(), audioService::class.java)
        activity?.bindService(intent,connection, Context.BIND_AUTO_CREATE)
    }
    fun serviceUnbind()
    {
        activity?.unbindService(connection)
    }

    override fun onDestroy() {
        if(!post.audio.equals("NONE")) {
            serviceUnbind()
            binding.progressMedia.progress=0
        }
        (activity as MainActivity).setupTopBottom()
        super.onDestroy()
    }
}