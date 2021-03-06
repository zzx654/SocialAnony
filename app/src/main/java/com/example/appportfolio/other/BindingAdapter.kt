package com.example.appportfolio.other

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.getLocation
import com.example.appportfolio.other.Constants.COMMENTADDED
import com.example.appportfolio.other.Constants.REPLYADDED
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

object BindingAdapter {

    @BindingAdapter("bindproportion")
    @JvmStatic
    fun bindproportion(view: TextView,proportion:Int){

        view.text="(${proportion}%)"
    }
    @BindingAdapter("bindvotecount")
    @JvmStatic
    fun bindvotecount(view: TextView,votecount:Int){

        view.text=votecount.toString()

    }
    @BindingAdapter("bindhint")
    @JvmStatic
    fun bindhint(view: EditText, position:String){
        view.hint="보기 $position"
    }
    @BindingAdapter("chatImage")
    @JvmStatic
    fun bindchatImage(view: ImageView, imagecontent: String?) {

        val option=MultiTransformation(CenterCrop(),RoundedCorners(8))

        Glide.with(view.context)
            .load(imagecontent)
            .placeholder(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
            .error(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
            .apply(RequestOptions.bitmapTransform(option))
            .into(view)
    }
    @BindingAdapter("bindvoteimg")
    @JvmStatic
    fun bindvoteImage(view:ImageView,vote:String)
    {
        if(vote == "none")
            view.visibility=View.GONE
        else
            view.visibility=View.VISIBLE
    }
    @BindingAdapter("vote","votecount")
    @JvmStatic
    fun bindvoteImage(view:TextView,vote:String,votecount:Int)
    {
        if(vote == "none")
            view.visibility=View.GONE
        else
        {
            view.visibility=View.VISIBLE
            view.text=votecount.toString()
        }

    }
    @BindingAdapter("notitype","read")
    @JvmStatic
    fun bindnotiImage(view:ImageView,notitype: Int,read:Int){

        when(notitype){
            COMMENTADDED,REPLYADDED->{
                view.setImageResource(R.drawable.comment)
            }
            else->view.setImageResource(R.drawable.favorite_off)
        }


        if(read==1)
            view.setColorFilter(ContextCompat.getColor(view.context,R.color.gray))
        else
            view.setColorFilter(ContextCompat.getColor(view.context,R.color.notitext))
    }
    @BindingAdapter("type","nickname","content")
    @JvmStatic
    fun bindtext(view:TextView,type:String,nickname:String?,content: String){

        if(type == "EXIT")
            view.text="${nickname}님이 나갔습니다"
        else
            view.text=content

    }
    @BindingAdapter("isread")
    @JvmStatic
    fun bindisread(view:TextView,isread:Int){
        if(isread==1)
            view.setTextColor(ContextCompat.getColor(view.context,R.color.gray))
        else
            view.setTextColor(ContextCompat.getColor(view.context,R.color.notitext))
    }
    @BindingAdapter("location")
    @JvmStatic
    fun bindlocation(view:TextView,location:String){
        val token=location.split('&')
        val lat=token[0].toDouble()
        val lon=token[1].toDouble()

        val locationstr=getLocation(lat,lon)
        view.text=locationstr
    }
    @BindingAdapter("blockednick","anonymous")
    @JvmStatic
    fun bindisread(view:TextView,blockednick:String,anonymous: Int){

        if(anonymous==1)
            view.text="익명사용자"
        else
            view.text=blockednick
    }

    @BindingAdapter("replytextvis")
    @JvmStatic
    fun bindreplytextvis(view:TextView,replycount: Int?){
        view.visibility=View.GONE
        replycount?.let{
            view.visibility=View.VISIBLE
        }
    }
    @BindingAdapter("bindreplycount")
    @JvmStatic
    fun bindreplycount(view: TextView, replycount:Int?) {

        replycount?.let{
            if(it==0)
                view.visibility=View.GONE
            else{
                view.visibility=View.VISIBLE
                view.text="답글 ${it}개"
            }
        }
    }
    @BindingAdapter("anonymous","gender","profileimage")
    @JvmStatic
    fun bindprofileImage(view: ImageView, anonymous: String?,gender:String,profileimage:String?) {

        if(anonymous!=null)
        {
            when(gender)
            {
                "남자"->view.setImageResource(R.drawable.icon_male)
                "여자"->view.setImageResource(R.drawable.icon_female)
                else->view.setImageResource(R.drawable.icon_none)
            }
        }
        else
        {
            //익명이 아닌경우
            if(profileimage==null)
            {
                when(gender)
                {
                    "남자"->view.setImageResource(R.drawable.icon_male)
                    "여자"->view.setImageResource(R.drawable.icon_female)
                    else->view.setImageResource(R.drawable.icon_none)
                }
            }
            else
            {
                Glide.with(view.context)
                    .load(profileimage)
                    .placeholder(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
                    .error(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
                    .into(view)
            }
        }
    }
    @BindingAdapter("gender","profileimage")
    @JvmStatic
    fun bindprofileImg(view: ImageView,gender:String?,profileimage:String?) {
        //익명이 아닌경우
        if(profileimage.equals("none"))
        {
            when(gender)
            {
                "남자"->view.setImageResource(R.drawable.icon_male)
                "여자"->view.setImageResource(R.drawable.icon_female)
                else->view.setImageResource(R.drawable.icon_none)
            }
        }
        else
        {
            Glide.with(view.context)
                .load(profileimage)
                .placeholder(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
                .error(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
                .into(view)
        }
    }
    @BindingAdapter("time")
    @JvmStatic
    fun bindtime(view:TextView,time:String)
    {
        val resdate=SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time)
        val resfinaldate=SimpleDateFormat("a h:mm").format(resdate!!)
        view.text=resfinaldate
    }
    @BindingAdapter("setTime")
    @JvmStatic
    fun bindTime(view: TextView,time:String)
    {
        val todayformat=SimpleDateFormat("a h:mm")
        val thisyearformat=SimpleDateFormat("M월 d일")
        val notthisyearformat=SimpleDateFormat("yyyy.M.d")
        val today= Calendar.getInstance()
        val yesterday= Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR,-1)

        val todaydate=SimpleDateFormat("yyyy-MM-dd").format(today.time)
        val yesterdaydate=SimpleDateFormat("yyyy-MM-dd").format(yesterday.time)

        val resdate=SimpleDateFormat("yyyy-MM-dd h:mm:ss").parse(time)
        val resfinaldate= resdate?.let { SimpleDateFormat("yyyy-MM-dd").format(it) }
        val resfinalyear= resdate?.let { SimpleDateFormat("yyyy").format(it) }
        val todayYear=SimpleDateFormat("yyyy").format(today.time)

        if(!resfinalyear.equals(todayYear))
            view.text= resdate?.let { notthisyearformat.format(it) }
        else{
            if(todaydate.equals(resfinaldate))
                view.text= resdate?.let { todayformat.format(it) }
            else if(yesterdaydate.equals(resfinaldate))
                view.text="어제"
            else
                view.text= resdate?.let { thisyearformat.format(it) }
        }
    }
    @BindingAdapter("anony","nickname")
    @JvmStatic
    fun bindNick(view: TextView, anony: String?,nickname: String) {
        var nick=""
        nick = if(anony!=null)
            "익명[${anony}]"
        else
            nickname
        view.text=nick
    }
    @SuppressLint("ResourceAsColor")
    @BindingAdapter("isliked","likecount")
    @JvmStatic
    fun bindlike(view: TextView, isliked:Int,likecount: Int) {

        var liketext=""
        liketext = if(likecount!=0)
            "좋아요 $likecount"
        else
            "좋아요"
        if(isliked==1)
            view.setTextColor(ContextCompat.getColor(view.context,R.color.black))
        else
            view.setTextColor(ContextCompat.getColor(view.context,R.color.gray))
        view.text=liketext
    }
    @BindingAdapter("ismy","isread","content","type")
    @JvmStatic
    fun bindchatroomcontent(view: TextView, ismy:Int,isread: Int,content:String,type:String) {
        when (type) {
            "IMAGE" -> view.text="사진을 보냈습니다."
            "LOCATION" -> view.text="(위치정보)"
            "EXIT" -> view.text="상대방이 대화방을 나갔습니다"
            else -> view.text=content
        }
        if(ismy==0&&isread==0&& type != "EXIT")
            view.setTextColor(ContextCompat.getColor(view.context,R.color.black))
    }
    @BindingAdapter("ismy","isread","type")
    @JvmStatic
    fun bindchatroomread(view: ImageView, ismy:Int,isread: Int,type: String) {
        if(ismy==0&&isread==0&& type != "EXIT")
            view.visibility=View.VISIBLE
        else
            view.visibility=View.INVISIBLE
    }
    @BindingAdapter("setroundImage")
    @JvmStatic
    fun bindroundImage(view: ImageView, imagecontent: String?) {

        val option= MultiTransformation(CenterCrop(), RoundedCorners(8))

        Glide.with(view.context)
            .load(imagecontent)
            .placeholder(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
            .error(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
            .apply(RequestOptions.bitmapTransform(option))
            .into(view)

        view.clipToOutline=true
    }
    @BindingAdapter("setImage")
    @JvmStatic
    fun bindImage(view: ImageView, imagecontent: String?) {
        Glide.with(view.context)
            .load(imagecontent)
            .placeholder(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
            .error(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
            .into(view)
        view.clipToOutline=true
    }
    @BindingAdapter("setPostImage")
    @JvmStatic
    fun bindPostImage(view:ImageView,imagecontents:String?){
        if(!imagecontents.equals("NONE"))
        {
            view.visibility=View.VISIBLE
            var lst:List<String> = listOf()
            val array=JSONArray(imagecontents)
            for(i in 0 until array.length())
            {
                val jsonObj=array.getJSONObject(i)
                val image=jsonObj.optString("imageUri")
                lst+=image
            }
            val option= MultiTransformation(CenterCrop(), RoundedCorners(8))

            Glide.with(view.context)
                .load(lst[0])
                .placeholder(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
                .error(ColorDrawable(ContextCompat.getColor(view.context, R.color.gray)))
                .apply(RequestOptions.bitmapTransform(option))
                .into(view)
            view.clipToOutline=true
            if(lst.size>1)
                view.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY)
        }
        else
            view.visibility=View.GONE
    }
    @BindingAdapter("followernum")
    @JvmStatic
    fun bindFollowerNum(view: TextView, follow:Int?)
    {
        val str="팔로워 ${follow}명"
        view.text=str

    }
    @BindingAdapter("imageNum")
    @JvmStatic
    fun bindImageNum(view: TextView, imagecontents:String?)
    {

        if(!imagecontents.equals("NONE"))
        {
            var lst:List<String> = listOf()
            val array=JSONArray(imagecontents)
            for(i in 0 until array.length())
            {
                val jsonObj=array.getJSONObject(i)
                val image=jsonObj.optString("imageUri")
                lst+=image
            }

            if(lst.size>1)
            {
                view.visibility=View.VISIBLE
                view.text="+"+(lst.size-1)
            }
            else
                view.visibility=View.GONE
        }
        else
            view.visibility=View.GONE
    }
    @BindingAdapter("voiceVis")
    @JvmStatic
    fun bindVoiceImage(view: ImageView, audioContent:String?)
    {
        if(audioContent.equals("NONE"))
            view.visibility=View.GONE
        else
            view.visibility=View.VISIBLE
    }
    @BindingAdapter("watchlater")
    @JvmStatic
    fun bindWatchLater(view:TextView,postedTime:String?)
    {
        val formatter= SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val trans_date=formatter.parse(postedTime)
        val postedmillis=trans_date.time
        val curTime=System.currentTimeMillis()
        var diffTime=(curTime-postedmillis)/1000
        var diffstr:String?=null
        if(diffTime<TimeValue.SEC.value)
            diffstr="방금 전"
        else{
            for(i in TimeValue.values()){
                diffTime/=i.value
                if(diffTime<i.maximum){
                    diffstr=diffTime.toString()+i.msg
                    break
                }
            }
        }
        view.text=diffstr
    }
    @BindingAdapter("nickname","anonymous","watchlater")
    @JvmStatic
    fun bindNick(view: TextView,nickname:String,anonymous:String?,postedTime: String?)
    {
        var nickstr=""
        if(anonymous=="")
            nickstr+=nickname
        else
            nickstr=nickstr+"익명["+anonymous+"]"
        val formatter= SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val trans_date=formatter.parse(postedTime)
        val postedmillis=trans_date.time
        val curTime=System.currentTimeMillis()
        var diffTime=(curTime-postedmillis)/1000
        var diffstr:String?=null
        if(diffTime<TimeValue.SEC.value)
            diffstr="방금 전"
        else{
            for(i in TimeValue.values()){
                diffTime/=i.value
                if(diffTime<i.maximum){
                    diffstr=diffTime.toString()+i.msg
                    break
                }
            }
        }
        "$nickstr · $diffstr".also { view.text = it }
    }
    @BindingAdapter("distance")
    @JvmStatic
    fun bindDistText(view:TextView,distance:Double?)
    {
        if (distance != null) {
            if(distance<0)
                view.visibility=View.GONE
            else
                view.visibility=View.VISIBLE
                view.text= (round(distance!!).toInt()).toString()+"km"
        }
        else
            view.visibility=View.GONE
    }
    @BindingAdapter("distanceImgVis")
    @JvmStatic
    fun bindDistImg(view:ImageView,distance:Double?)
    {
        if (distance != null) {
            if(distance<0)
                view.visibility=View.GONE
            else
                view.visibility=View.VISIBLE
        }
        else
            view.visibility=View.GONE
    }
    @BindingAdapter("countText")
    @JvmStatic
    fun bindCountText(view:TextView,count:Int)
    {
        view.text=count.toString()
    }
    @BindingAdapter("tagname")
    @JvmStatic
    fun bindtagname(view:TextView,tagname:String)
    {
        "#$tagname".also { view.text = it }
    }
    @BindingAdapter("tagcount")
    @JvmStatic
    fun bindtagname(view:TextView,tagcount:Int?)
    {
        if(tagcount==null)
            view.visibility=View.GONE
        else{
            view.visibility=View.VISIBLE
            view.text= "스토리$tagcount"
        }
    }
    @BindingAdapter("tagLiked")
    @JvmStatic
    fun bindtagLiked(view:ImageButton,tagLiked:Int?)
    {
        if(tagLiked==null||tagLiked==1)
        {
            view.visibility=View.VISIBLE
            view.setImageResource(R.drawable.like_on)
        }
        else
        {
            view.visibility=View.VISIBLE
            view.setImageResource(R.drawable.like_off)
        }
    }
    @BindingAdapter("followed")
    @JvmStatic
    fun bindfollowed(view:ImageButton,followed:Int)
    {
        if(followed==1)
            view.setImageResource(R.drawable.favorite_on)
        else
            view.setImageResource(R.drawable.favorite_off)
    }
}
enum class TimeValue(val value: Int,val maximum : Int, val msg : String) {
    SEC(60,60,"분 전"),
    MIN(60,24,"시간 전"),
    HOUR(24,30,"일 전"),
    DAY(30,12,"달 전"),
    MONTH(12,Int.MAX_VALUE,"년 전")
}