package com.example.appportfolio.ui.main.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.CommentAdapter
import com.example.appportfolio.adapters.PostDetailsAdapter
import com.example.appportfolio.data.entities.Comment
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.databinding.FragmentReplyBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.viewmodel.BaseCommentViewModel
import com.example.appportfolio.ui.main.viewmodel.ReplyViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReplyFragment:BaseCommentFragment(R.layout.fragment_reply), MenuProvider {
    lateinit var binding: FragmentReplyBinding
    override val baseCommentViewModel: BaseCommentViewModel
        get()  {
            val vm: ReplyViewModel by viewModels()
            return vm
        }
    private val vmReply: ReplyViewModel
        get() = baseCommentViewModel as ReplyViewModel
    override val commentAdapter: CommentAdapter
        get() = commentadapter
    override val postAdapter: PostDetailsAdapter?
        get() = null
    override val srLayout: SwipeRefreshLayout
        get() = binding.srLayout
    override val rvComments: RecyclerView
        get() = binding.rvComment
    override val cbAnony: CheckBox
        get() = binding.cbAnony
    override val edtComment: EditText
        get() = binding.edtComment
    override val post: Post
        get() = postcontents
    override val sendcomment: ImageButton
        get() = binding.sendcomment
    override val postcommentprogress: ProgressBar
        get() = binding.postcommentprogress
    private lateinit var commentadapter: CommentAdapter
    lateinit var comment: Comment

    private var mRootView:View?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postcontents=arguments?.getParcelable("post")!!
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null){
            binding= DataBindingUtil.inflate<FragmentReplyBinding>(inflater,
                R.layout.fragment_reply,container,false)
            sendcomment.setOnClickListener {
                sendComment()
                hideKeyboard()
            }
            comment=arguments?.getParcelable("comment")!!

            commentadapter= CommentAdapter()
            init()
            commentadapter.submitList(listOf(comment))
            addtolast=false
            refreshComments()
            mRootView=binding.root
        }

        return mRootView
    }


    override fun blockcommentuser(selectedComment: Comment) {
        val anonymous=selectedComment.anonymous!=""
        blockingid=selectedComment.userid
        vmInteract.blockcommentuser(anonymous,selectedComment.userid,comment.platform==selectedComment.platform&&comment.account==selectedComment.account,api)
    }

    override fun blockpostuser() {
    }

    override fun loadNewComments() {
        val curComments=commentAdapter.currentList
        if(curComments.isNotEmpty())
        {
            val lastComment=curComments.last()
            vmReply.getReply(comment.ref,lastComment.commentid,lastComment.time,api)
        }
    }

    override fun deletereply(commentid: Int) {
        super.deletereply(commentid)
        addtolast=false
        vmReply.deletereply(commentid,api)
    }

    override fun scrollRefresh() {
        addtolast=false
        vmReply.checkSelectedComment(comment.userid,post.userid,comment.commentid!!,comment.postid,api)
    }

    override fun refreshComments() {
        vmReply.getReply(comment.ref,null,null,api)
    }
    override fun postComment(anony: String) {
        post.userid
        comment.userid
        vmReply.postReply(comment.ref,post.postid!!,comment.commentid!!,anony,edtComment.text.toString(),post.userid,comment.userid,api)
    }

    override fun togglecomment(com: Comment) {
        if(com.userid==vmAuth.userid.value!!)
            baseCommentViewModel.toggleComment(null,null,null,null,com.commentid!!,com.commentliked,api)
        else
            baseCommentViewModel.toggleComment(comment.commentid,com.userid,com.depth,com.postid,com.commentid!!,com.commentliked,api)
    }
    override fun onResume() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="답글쓰기"
        super.onResume()

    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
                true
            }
            else->false
        }
    }
    override fun shownotexist(){
        snackbar("해당 댓글이 삭제되었습니다")
    }

    override fun applyList(comments: List<Comment>) {
        var newComments:List<Comment> = listOf()
        newComments+=comment
        newComments+=comments
        commentAdapter.submitList(newComments)
    }
    override fun setupRecyclerView(){
        binding.rvComment.apply{
            adapter=commentAdapter
            layoutManager= LinearLayoutManager(requireContext())
            itemAnimator=null
            addOnScrollListener(scrollListener)
            setHasFixedSize(true)
        }
    }
    override fun fixtotop(postedcomment: Comment) {
        var oldcomments=commentAdapter.currentList.toList()
        oldcomments-=comment//최상단 댓글제거
        val newcomments = listOf(postedcomment)+oldcomments
        applyList(newcomments)
    }
    override fun subscribeToObserver() {
        super.subscribeToObserver()
        vmReply.deletecommentResponse.observe(viewLifecycleOwner,Event.EventObserver(

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
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        })
        vmReply.deletereplyResponse.observe(viewLifecycleOwner,Event.EventObserver(
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
                    var templist=commentAdapter.currentList.toList()
                    templist-=curdeletingcomm
                    commentAdapter.submitList(templist)
                } else {
                    Toast.makeText(requireContext(), "서버오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        })
        vmReply.checkSelectedCommentResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError={
                snackbar(it)
            }
        ){
            if(it.resultCode!=200)
                srLayout.isRefreshing=false
            handleResponse(requireContext(),it.resultCode) {
                when (it.resultCode) {
                    100 -> Toast.makeText(requireContext(), "댓글이 삭제되었습니다\".", Toast.LENGTH_SHORT)
                        .show()
                    400 -> Toast.makeText(
                        requireContext(),
                        "해당 게시물의 정보에 접근할 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    500 -> Toast.makeText(
                        requireContext(),
                        "해당 댓글에 대한 권한이 없습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        comment = it.comments[0]
                        comment.commentliked = commentAdapter.currentList[0].commentliked
                        comment.likecount = commentAdapter.currentList[0].likecount
                        refreshComments()
                    }
                }
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}