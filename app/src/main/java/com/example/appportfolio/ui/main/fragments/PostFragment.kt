package com.example.appportfolio.ui.main.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.CommentAdapter
import com.example.appportfolio.adapters.PostDetailsAdapter
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.*
import com.example.appportfolio.databinding.FragmentPostBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.services.AudioService
import com.example.appportfolio.ui.main.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
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
    override val post: Post
        get() = postcontents

    override val commentAdapter:CommentAdapter
        get() = commentadapter
    override val postAdapter: PostDetailsAdapter?
        get() = postadapter

    @Inject
    lateinit var preferences: UserPreferences

    lateinit var commentadapter: CommentAdapter

    lateinit var postadapter:PostDetailsAdapter
    lateinit var binding:FragmentPostBinding
    lateinit var concatAdapter:ConcatAdapter
    var aService:AudioService?=null

    var connection=object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.mBinder
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
                sendComment()
                hideKeyboard()
            }
            postadapter= PostDetailsAdapter(requireContext())
            postAdapter!!.post=post
            commentadapter=CommentAdapter()
            concatAdapter= ConcatAdapter(postAdapter,commentAdapter)
            init()


            if(post.audio != "NONE")
            {
                servicebind()
            }
            postadapter.setPlayPauseClick {
                aService?.toggle_play()
            }
            postadapter.setrgcommentChangedListener { checkedid->
                if(checkedid!=postadapter.checkedRadioComment)
                {
                    postadapter.checkedRadioComment=checkedid
                    addtolast=false
                    refreshComments()
                }

            }
            commentAdapter.setOnrootClickListener { comment->
                vmComment.checkSelectedComment(comment.userid,post.userid,comment.commentid!!,comment.postid,api)
            }
            postadapter.setbtnVoteClickListener {
                postadapter.checkedRadioVote?.let{ voteoptionid->
                    vmInteract.vote(post.postid!!,voteoptionid,api)
                }
            }
            postadapter.setOnPostProfileClickListener {
                if(post.userid!=vmAuth.userid.value!!)
                {
                    selecteduserid=post.userid
                    if(post.anonymous!="")
                        showprofile(null,post.gender!!,null,post.anonymous!!,true)
                    else
                        vmInteract.getuserprofile(post.userid,api)
                }
            }
            postadapter.setOnlikePostClickListener {
                toggleLike()
            }
            postadapter.setOnBookmarkClickListener {
                toggleBookmark()
            }
            postadapter.settagClickListener { tag->
                val bundle=Bundle()
                bundle.putString("tag",tag)
                (activity as MainActivity).replaceFragment("tagPostsFragment",TagPostsFragment(),bundle)
            }
            setupRecyclerView()
            bindvote()
            if(post.commentcount>0)
                refreshComments()
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
        addtolast=false
        vmInteract.getSelectedPost(post.postid!!,null,null,api)
    }
    override fun setupRecyclerView() {
        binding.rvComment.apply{
            adapter=concatAdapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
            addOnScrollListener(scrollListener)
        }
    }
    override fun loadNewComments()
    {
        val curComments=commentAdapter.currentList
        if(!curComments.isEmpty())
        {
            val lastComment=curComments.last()
            lastComment.commentid?.let{
                when(postadapter.checkedRadioComment){
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
    }
    override fun applyList(comments: List<Comment>) {
        commentAdapter.submitList(comments)
    }
    override fun refreshComments()
    {
        when(postadapter!!.checkedRadioComment){
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
        }
        else
        {
            (activity as MainActivity).binding.title.text=post.nickname
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

    override fun fixtotop(postedcomment: Comment) {
        var oldcomments=commentAdapter.currentList.toList()
        var newcomments = listOf(postedcomment)+oldcomments
        applyList(newcomments)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.post_tools, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        menu.findItem(R.id.delete).isVisible = post.userid==vmAuth.userid.value!!
        menu.findItem(R.id.requestChat).isVisible=post.userid!=vmAuth.userid.value!!
        menu.findItem(R.id.block).isVisible=post.userid!=vmAuth.userid.value!!
        menu.findItem(R.id.report).isVisible=post.userid!=vmAuth.userid.value!!
        //bindvote()
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
        var anonymous:Boolean=false
        anonymous= selectedComment.anonymous!=null
        vmInteract.blockcommentuser(anonymous,selectedComment.userid,selectedComment.platform==post.platform&&selectedComment.account==post.account,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),api)
    }

    override fun blockpostuser() {
        var anonymous:Boolean=false
        anonymous= post.anonymous!=null

        vmInteract.blockpostuser(anonymous,post.userid,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),api)
    }

    override fun shownotexist() {
        snackbar("삭제된 게시물입니다")
    }
    override fun subscribeToObserver()
    {
        super.subscribeToObserver()

        vmInteract.getVoteResultResponse.observe(viewLifecycleOwner,Event.EventObserver(

            onLoading={
                postadapter.loadVoteVis=true
                postadapter.voteLayoutVis=false
                rvComments.findViewHolderForAdapterPosition(0)?.let{
                    (it as PostDetailsAdapter.postViewHolder).binding.loadvote.visibility=View.VISIBLE
                    it.binding.votelayout.visibility=View.GONE

                }
            },
            onError={
                snackbar(it)
                postadapter.loadVoteVis=false
                rvComments.findViewHolderForAdapterPosition(0)?.let{
                    (it as PostDetailsAdapter.postViewHolder).binding.loadvote.visibility=View.GONE
                }
            }
        ){
            postadapter.loadVoteVis=false
            rvComments.findViewHolderForAdapterPosition(0)?.let{
                (it as PostDetailsAdapter.postViewHolder).binding.loadvote.visibility=View.GONE
            }
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    postadapter.rgVoteVis=false
                    postadapter.voteLayoutVis=true
                    postadapter.rvVoteVis=true
                    postadapter.btnVoteVis=false

                    rvComments.findViewHolderForAdapterPosition(0)?.let{ viewholder->
                        (viewholder as PostDetailsAdapter.postViewHolder).binding.rgVote.visibility=View.GONE
                        viewholder.binding.votelayout.visibility=View.VISIBLE
                        viewholder.binding.rvvote.visibility=View.VISIBLE
                        viewholder.binding.btnVote.visibility=View.GONE
                        postadapter.voteResult=it.voteresult
                        viewholder.bindvoteresult()

                    }

                }
            }

        })
        vmInteract.getPollResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                postadapter.loadVoteVis=true
                postadapter.voteLayoutVis=false
                rvComments.findViewHolderForAdapterPosition(0)?.let{
                    (it as PostDetailsAdapter.postViewHolder).binding.loadvote.visibility=View.VISIBLE
                    it.binding.votelayout.visibility=View.GONE
                }
            },
            onError={
                snackbar(it)
                postadapter.loadVoteVis=false
                rvComments.findViewHolderForAdapterPosition(0)?.let{
                    (it as PostDetailsAdapter.postViewHolder).binding.loadvote.visibility=View.GONE
                }
            }
        ){
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    postadapter.btnVoteEnabled=true
                    postadapter.btnVoteVis=true
                    postadapter.loadVoteVis=false
                    postadapter.voteLayoutVis=true
                    postadapter.rgVoteVis=true
                    postadapter.polloptions=it.polloptions
                    rvComments.findViewHolderForAdapterPosition(0)?.let{
                        (it as PostDetailsAdapter.postViewHolder).binding.btnVote.isEnabled=true
                        it.binding.btnVote.visibility=View.VISIBLE
                        it.binding.loadvote.visibility=View.GONE
                        it.binding.votelayout.visibility=View.VISIBLE
                        it.binding.rgVote.visibility=View.VISIBLE
                        it.bindpolloptions()
                    }

                } else if (it.resultCode == 300) {
                    //투표결과얻어오기
                    vmInteract.getvoteresult(post.postid!!, api)
                }
            }

        })
        vmComment.deletecommentResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                loadingDialog.show()
            },
            onError={
                loadingDialog.dismiss()
                snackbar(it)

            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    var templist=commentAdapter.currentList.toList()
                    templist-=curdeletingcomm
                    commentAdapter.submitList(templist)
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
                    postadapter.post=postcontents
                    rvComments.findViewHolderForAdapterPosition(0)?.let{
                        (it as PostDetailsAdapter.postViewHolder).bindNickname(postcontents.anonymous,postcontents.nickname)

                        it.binding.tvspacetime.text = postadapter.getSpaceTime()
                        it.binding.likecount.text = "좋아요 ${postcontents.likecount}개"
                        it.binding.commentcount.text = "댓글 ${postcontents.commentcount}개"
                    }
                    bindvote()
                    refreshComments()
                }
            }
        })
        vmInteract.toggleLikeResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError= {
                snackbar(it)
                loadingDialog.dismiss()
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 100) {
                    Toast.makeText(requireContext(), "삭제된 글입니다", Toast.LENGTH_SHORT).show()
                } else {
                    if (it.toggle == 1) {
                        post.likecount = post.likecount - 1
                        post.isLiked = 0
                    } else {
                        post.likecount = post.likecount + 1
                        post.isLiked = 1
                    }
                    postadapter.post=post
                    rvComments.findViewHolderForAdapterPosition(0)?.let{
                        if(post.isLiked==1)
                            (it as PostDetailsAdapter.postViewHolder).binding.imgLike.setImageResource(R.drawable.favorite_on)
                        else
                            (it as PostDetailsAdapter.postViewHolder).binding.imgLike.setImageResource(R.drawable.favorite_off)
                        it.binding.likecount.text="좋아요 ${post.likecount}개"
                    }
                }
            }
        })
        vmInteract.toggleBookmarkResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                snackbar(it)
                loadingDialog.dismiss()
            },
            onLoading={
                loadingDialog.show()
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 100) {
                    Toast.makeText(requireContext(), "삭제된 글입니다", Toast.LENGTH_SHORT).show()
                } else {
                    post.bookmarked=if(it.toggle==1) 0 else 1
                    postadapter.post=post
                    rvComments.findViewHolderForAdapterPosition(0)?.let{ viewholder->
                        if(post.bookmarked==1)
                            (viewholder as PostDetailsAdapter.postViewHolder).binding.imgBookmark.setImageResource(R.drawable.ic_bookmarkon)
                        else
                            (viewholder as PostDetailsAdapter.postViewHolder).binding.imgBookmark.setImageResource(R.drawable.ic_bookmarkoff)
                    }
                }
            }
        })
        AudioService.isplaying.observe(viewLifecycleOwner){ playing->

            postadapter.isplaying = playing
            rvComments.findViewHolderForAdapterPosition(0)?.let{
                if(playing)
                    (it as PostDetailsAdapter.postViewHolder).binding.playpause.setImageResource(R.drawable.ic_pause)
                else
                    (it as PostDetailsAdapter.postViewHolder).binding.playpause.setImageResource(R.drawable.ic_play)
            }
        }
        AudioService.mediamax.observe(viewLifecycleOwner){ max->
            postadapter.progressMediaMax=max
            rvComments.findViewHolderForAdapterPosition(0)?.let {
                (it as PostDetailsAdapter.postViewHolder).binding.progressMedia.max=max
            }
        }
        AudioService.curpos.observe(viewLifecycleOwner,Event.EventObserver(

        ){ progress->
            postadapter.progressMediaProgress=progress
            rvComments.findViewHolderForAdapterPosition(0)?.let {
                (it as PostDetailsAdapter.postViewHolder).binding.progressMedia.progress=progress
            }
        })
    }
    private fun servicebind()
    {
        var intent=Intent(requireContext(), AudioService::class.java)

        activity?.bindService(intent,connection, Context.BIND_AUTO_CREATE)
    }
    fun serviceUnbind()
    {
        activity?.unbindService(connection)
    }
    override fun onDestroy() {
        if(!post.audio.equals("NONE")) {
            serviceUnbind()
            postadapter.progressMediaProgress=0
            postadapter.notifyItemChanged(0)
        }
        (activity as MainActivity).setupTopBottom()
        super.onDestroy()
    }
}