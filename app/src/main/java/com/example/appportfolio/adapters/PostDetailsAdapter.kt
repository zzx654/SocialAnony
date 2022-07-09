package com.example.appportfolio.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.onSingleClick
import com.example.appportfolio.data.entities.Post
import com.example.appportfolio.data.entities.Voteresult
import com.example.appportfolio.data.entities.polloption
import com.example.appportfolio.databinding.FragmentPostdetailsBinding
import com.example.appportfolio.other.TimeValue
import com.google.android.material.chip.Chip
import org.json.JSONArray
import java.text.SimpleDateFormat
import kotlin.math.round
import kotlin.math.roundToInt

class PostDetailsAdapter(val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater= LayoutInflater.from(parent.context)
        return DataBindingUtil.inflate<FragmentPostdetailsBinding>(
            layoutInflater,
            R.layout.fragment_postdetails,
            parent,
            false
        ).let {
            postViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as postViewHolder).onbind()
    }

    override fun getItemCount(): Int=1
    inner class postViewHolder(val binding: FragmentPostdetailsBinding):RecyclerView.ViewHolder(binding.root) {
        fun onbind() {
            post?.let { it ->
                setVisibilities()
                binding.rgcomment.check(checkedRadioComment)
                binding.rgVote.setOnCheckedChangeListener { _, checkedid ->
                    if(checkedid>0)
                        checkedRadioVote = checkedid
                    val color = ContextCompat.getColor(context, R.color.skinfore)
                    binding.btnVote.isEnabled = true
                    binding.btnVote.setBackgroundColor(color)
                }

                bindNickname(it.anonymous,it.nickname)

                if (it.audio != "NONE") {
                    binding.playpause.visibility = View.VISIBLE
                    binding.progressMedia.visibility = View.VISIBLE
                } else {
                    binding.playpause.visibility = View.GONE
                    binding.progressMedia.visibility = View.GONE
                }
                if(isplaying)
                    binding.playpause.setImageResource(R.drawable.ic_pause)
                else
                    binding.playpause.setImageResource(R.drawable.ic_play)
                binding.progressMedia.max=progressMediaMax
                binding.progressMedia.progress=progressMediaProgress
                binding.tvspacetime.text = getSpaceTime()
                binding.text.text = it.text
                binding.text.text = it.text
                if (it.commentcount == 0) {
                    binding.noComment.visibility = View.VISIBLE
                } else {
                    binding.rgcomment.visibility = View.VISIBLE
                }
                binding.likecount.text = "좋아요 ${it.likecount}개"
                binding.commentcount.text = "댓글 ${it.commentcount}개"
                if (it.isLiked == 1)
                    binding.imgLike.setImageResource(R.drawable.favorite_on)
                else
                    binding.imgLike.setImageResource(R.drawable.favorite_off)
                if (it.bookmarked == 1)
                    binding.imgBookmark.setImageResource(R.drawable.ic_bookmarkon)
                else
                    binding.imgBookmark.setImageResource(R.drawable.ic_bookmarkoff)
                binding.playpause.onSingleClick {
                    playPauseClickListener?.let { click ->
                        click()
                    }
                }

                binding.rgcomment.setOnCheckedChangeListener { radioGroup, checkedid ->

                    rgcommentChangedListener?.let { check ->
                        check(checkedid)
                    }
                }

                binding.btnVote.onSingleClick {
                    btnVoteClickListener?.let { click ->
                        click()

                    }
                }
                binding.imgProfile.onSingleClick {
                    postprofileClickListener?.let { click ->
                        click()

                    }
                }
                binding.likepost.onSingleClick {
                    likepostClickListener?.let { click ->
                        click()

                    }
                }
                binding.bookmarkpost.onSingleClick {
                    bookmarkClickListener?.let { click ->
                        click()

                    }
                }
                bindVpImg()
                bindImages()
                binding.cgTag.removeAllViews()
                it.tags?.let {
                    bindTags(it)
                }
                if (it.anonymous != "") {
                    when (post?.gender) {
                        "남자" -> binding.imgProfile.setImageResource(R.drawable.icon_male)
                        "여자" -> binding.imgProfile.setImageResource(R.drawable.icon_female)
                        else -> binding.imgProfile.setImageResource(R.drawable.icon_none)
                    }
                } else {
                    //익명이 아닌경우
                    if (it.profileimage == null) {
                        when (post?.gender) {
                            "남자" -> binding.imgProfile.setImageResource(R.drawable.icon_male)
                            "여자" -> binding.imgProfile.setImageResource(R.drawable.icon_female)
                            else -> binding.imgProfile.setImageResource(R.drawable.icon_none)
                        }
                    } else {
                        Glide.with(context)
                            .load(it.profileimage)
                            .placeholder(ColorDrawable(ContextCompat.getColor(context, R.color.gray)))
                            .error(ColorDrawable(ContextCompat.getColor(context, R.color.gray)))
                            .into(binding.imgProfile)
                    }
                }
                bindpolloptions()
                binding.rvvote.apply {
                    voteresultadapter=VoteResultAdapter()
                    adapter=voteresultadapter
                    layoutManager= LinearLayoutManager(context)
                    itemAnimator=null
                }
                bindvoteresult()
            }

        }
        private fun setVisibilities(){
            binding.rvvote.visibility=if(rvVoteVis) View.VISIBLE else View.GONE
            binding.rgcomment.visibility=if(rgCommentVis) View.VISIBLE else View.GONE
            binding.rgVote.visibility=if(rgVoteVis) View.VISIBLE else View.GONE
            binding.loadvote.visibility=if(loadVoteVis) View.VISIBLE else View.GONE
            binding.votelayout.visibility=if(loadVoteVis) View.VISIBLE else View.GONE
            binding.btnVote.visibility=if(btnVoteVis) View.VISIBLE else View.GONE
            binding.btnVote.isEnabled=btnVoteEnabled
            binding.noComment.visibility=if(noCommentVis) View.VISIBLE else View.GONE
            binding.votelayout.visibility=if(voteLayoutVis) View.VISIBLE else View.GONE
        }
        private fun bindTags(Tags:String){
            binding.cgTag.visibility = View.VISIBLE
            var tags: List<String> = listOf()
            if (Tags.contains("#"))
                tags = Tags.split("#")
            else
                tags += Tags

            for (tag in tags) {
                val chip = Chip(context).apply {
                    text = "#$tag"
                    chipStrokeWidth = 0f
                    setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        chipBackgroundColor =
                            AppCompatResources.getColorStateList(
                                context,
                                R.color.chipback
                            )
                        setChipStrokeColorResource(R.color.black)
                        setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.chiptext
                            )
                        )
                        textSize = 12f
                    }
                    setOnClickListener {
                        tagClickListener?.let { click ->
                            click(tag)

                        }
                    }
                }
                binding.cgTag.addView(chip)
            }
        }
        private fun bindVpImg()=binding.vpimg.apply{
            imagesadapter= ImagesAdapter()
            adapter=imagesadapter
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
        fun bindNickname(anony:String?,nickname:String)
        {
            if(anony!="")
                binding.tvNick.text= "익명[$anony]"
            else
                binding.tvNick.text=nickname
        }
        fun bindvoteresult()
        {

            voteResult?.let{
                var sum = 0
                for (i in it) {
                    sum += i.votecount
                }
                for (i in it.indices) {
                    if (it[i].votecount == 0)
                        it[i].proportion = 0
                    else {
                        val a: Double =
                            (it[i].votecount.toDouble() / sum.toDouble()) * 100
                        it[i].proportion = a.roundToInt()
                    }

                }
                voteresultadapter.submitList(it)
            }
        }
        fun bindpolloptions()
        {
            binding.rgVote.clearCheck()
            binding.rgVote.removeAllViews()
            polloptions?.let{
                for (i in it) {
                    val radiobtn = RadioButton(context)
                    radiobtn.apply {
                        val colorList = ColorStateList(
                            arrayOf(
                                intArrayOf(-android.R.attr.state_enabled),  // Disabled
                                intArrayOf(android.R.attr.state_enabled)    // Enabled
                            ),
                            intArrayOf(
                                ContextCompat.getColor(
                                    context,
                                    R.color.inactive
                                ),     // The color for the Disabled state
                                ContextCompat.getColor(
                                    context,
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
            }
            checkedRadioVote?.let{ id->
                binding.rgVote.check(id)
            }


        }
        private fun bindImages()
        {
            if(post!!.image != "NONE")
            {
                binding.vpimg.visibility= View.VISIBLE
                var lst:List<String> = listOf()
                val array= JSONArray(post!!.image)
                for(i in 0 until array.length())
                {
                    val jsonObj=array.getJSONObject(i)
                    val image=jsonObj.optString("imageUri")
                    lst+=image
                }
                if(lst.size>1)
                    setupIndicators(lst.size)
                imagesadapter.submitList(lst)
            }
        }
        private fun setupIndicators(count:Int){
            binding.layoutIndicators.removeAllViews()
            val params= LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(6,20,6,8)

            for(i in 0 until count)
            {
                val indicator= ImageView(context)
                indicator.setImageDrawable(
                    ContextCompat.getDrawable(context,
                    R.drawable.bg_indicator_inactive))
                indicator.layoutParams=params
                binding.layoutIndicators.addView(indicator)
            }
            setCurrentIndicator(0)
        }
        private fun setCurrentIndicator(position:Int){

            val childCount=binding.layoutIndicators.childCount
            for(i in 0 until childCount){
                val imageView=binding.layoutIndicators.getChildAt(i) as ImageView
                if(i==position){
                    imageView.setImageDrawable(
                        ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_indicator_active
                    ))
                }else{
                    imageView.setImageDrawable(
                        ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_indicator_inactive
                    ))
                }
            }
        }
    }
    lateinit var imagesadapter:ImagesAdapter
    lateinit var voteresultadapter:VoteResultAdapter

    fun setOnPostProfileClickListener(listener:()-> Unit){
        postprofileClickListener=listener
    }
    fun setOnlikePostClickListener(listener:()-> Unit){
        likepostClickListener=listener
    }
    fun setOnBookmarkClickListener(listener:()-> Unit){
        bookmarkClickListener=listener
    }
    fun setPlayPauseClick(listener: () -> Unit){
        playPauseClickListener=listener
    }
    fun setrgcommentChangedListener(listener: (Int) -> Unit){
        rgcommentChangedListener=listener
    }
    fun setbtnVoteClickListener(listener: () -> Unit){
        btnVoteClickListener=listener
    }
    fun settagClickListener(listener: (String) -> Unit){
        tagClickListener=listener
    }

    var tagClickListener:((String)->Unit)? = null
    var postprofileClickListener:(()->Unit)? = null
    var btnVoteClickListener:(()->Unit)? = null
    var rgcommentChangedListener:((Int)->Unit)? = null
    var playPauseClickListener:(()->Unit)? = null
    var likepostClickListener:(()->Unit)? = null
    var bookmarkClickListener:(()->Unit)? = null


    var rvVoteVis=false
    var rgCommentVis=false
    var isplaying=false
    var rgVoteVis=false
    var loadVoteVis=false
    var voteLayoutVis=false
    var btnVoteVis=false
    var btnVoteEnabled=false
    var noCommentVis=false
    var progressMediaMax:Int=0
    var progressMediaProgress:Int=0
    var checkedRadioVote:Int?=null
    var checkedRadioComment:Int=R.id.timecomment
    var polloptions:List<polloption>?=null
    var voteResult:List<Voteresult>?=null

    var post: Post?=null

    fun getSpaceTime():String
    {
        val formatter= SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val trans_date=formatter.parse(post!!.date)
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
        if (post!!.distance != null) {
            if(post!!.distance!!>=0)
            {
                str=str+" · "+(round(post!!.distance!!).toInt()).toString()+"km"
            }
        }
        return str!!
    }
}