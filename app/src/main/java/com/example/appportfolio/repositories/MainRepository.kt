package com.example.appportfolio.repositories

import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.responses.blocksResponse
import com.example.appportfolio.api.responses.commentResponse
import com.example.appportfolio.api.responses.intResponse
import com.example.appportfolio.api.responses.uploadImageResponse
import com.example.appportfolio.other.Resource
import com.example.appportfolio.safeCall
import com.google.android.datatransport.cct.StringMerger
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.http.*

class MainRepository {
    suspend fun getHotUsers(
        lastuserid:Int?,
        lastuserfollow:Int?,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getHotUsers(lastuserid, lastuserfollow))
        }
    }
    suspend fun checkuser(
        userid:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.checkuser(userid))
        }
    }
    suspend fun checknick(
        nickname:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.checknick(nickname))
        }
    }
    suspend fun toggleFollow(
        userid:Int,
        following:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.toggleFollow(userid,following))
        }
    }
    suspend fun getSearchedPerson(
        lastuserid:Int?,
        nickname:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getSearchedPerson(lastuserid, nickname))
        }
    }
    suspend fun getSearchedFollowingPerson(
        lastuserid:Int?,
        nickname:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getSearchedFollowingPerson(lastuserid, nickname))
        }
    }
    suspend fun getFollowingPerson(
        lastuserid:Int?,
        userid:Int?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getFollowingPerson(lastuserid,userid))
        }
    }
    suspend fun getFollowerPerson(
        lastuserid:Int?,
        userid:Int?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getFollowerPerson(lastuserid,userid))
        }
    }
    suspend fun getChatProfiles(
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getChatProfiles())
        }
    }
    suspend fun getpolloptions(
        postid: String,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getpolloptions(postid))
        }
    }
    suspend fun vote(
        postid: String,
        optionid:Int,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.vote(postid,optionid))
        }
    }
    suspend fun getVoteResult(
        postid: String,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getVoteResult(postid))
        }
    }
    suspend fun acceptchat(
        roomid:String,
        organizer:Int,
        participant:Int,
        time:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.acceptchat(roomid, organizer, participant, time))
        }
    }
    suspend fun refusechat(
        roomid:String,
        userid:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.refusechat(roomid,userid))
        }
    }
    suspend fun getchatrequests(
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getchatrequests())
        }
    }
    suspend fun requestchat(
        userid:Int,
        roomid:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.requestchat(userid, roomid))
        }
    }
    suspend fun getuserprofile(
        userid:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getuserprofile(userid))
        }
    }
    suspend fun deleteBlock(
        userid:Int,
        blockeduserid:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.deleteBlock(userid,blockeduserid))
        }
    }
    suspend fun getBlocks(
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getBlocks())
        }
    }
    suspend fun deletepost(
        postid:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.deletepost(postid))
        }
    }
    suspend fun deletereply(
        commentid:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.deletereply(commentid))
        }
    }
    suspend fun deletecomment(
        ref:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.deletecomment(ref))
        }
    }

    suspend fun report(
        postid:String?,
        commentid:Int?,
        reporttype:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.report(postid, commentid, reporttype))
        }
    }
    suspend fun blockcommentuser(
        anonymous:Boolean,
        blockuserid:Int,
        popback:Boolean,
        time: String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.blockcommentuser(anonymous,blockuserid,popback,time))
        }
    }
    suspend fun blockpostuser(
        anonymous:Boolean,
        blockuserid:Int,
        time: String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.blockpostuser(anonymous,blockuserid,time))
        }
    }
    suspend fun blockchatuser(
        anonymous:Boolean,
        blockuserid:Int,
        time: String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.blockchatuser(anonymous,blockuserid,time))
        }
    }
    suspend fun readAllNoti(
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.readAllNoti())
        }
    }
    suspend fun deleteAllNoti(
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.deleteAllNoti())
        }
    }
    suspend fun readNoti(
        notiid:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.readNoti(notiid))
        }
    }
    suspend fun checkNotiUnread(
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.checkNotiUnread())
        }
    }
    suspend fun getNotis(
        notiid:Int?,
        date:String?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getNotis(notiid,date))
        }
    }
    suspend fun getReply(
        ref: Int,
        commentid:Int?,
        time:String?,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getReply(ref,commentid, time))
        }
    }
    suspend fun postReply(
        ref:Int,
        postid:String,
        commentid:Int,
        time:String,
        anonymous:String,
        text:String,
        postuserid:Int,
        commentuserid:Int,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.postReply(ref,postid,commentid, time,anonymous,text,postuserid,commentuserid))
        }
    }
    suspend fun checkSelectedComment(
        postuserid:Int?,
        commentuserid: Int?,
        commentid:Int,
        postid:String,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.checkSelectedComment(postuserid,commentuserid,commentid,postid))
        }
    }
    suspend fun uploadimg(image:MultipartBody.Part, api: MainApi)
            = withContext(Dispatchers.IO){
        safeCall{

            Resource.Success(api.uploadimg(image))
        }
    }

    suspend fun editprofile(
        imageuri:String?,
        nickname:String,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.editprofile(imageuri, nickname))
        }
    }
    suspend fun toggleComment(
        rootcommentid:Int?,
        commentuserid: Int?,
        depth:Int?,
        time:String?,
        postid:String?,
        commentid:Int,
        isLiked:Int,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.toggleComment(rootcommentid,commentuserid, depth, time, postid, commentid, isLiked))
        }
    }

    suspend fun postComment(
        postuserid: Int?,
        postid:String,
        time:String,
        anonymous:String,
        text:String,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.postComment(postuserid,postid,time,anonymous,text))
        }
    }

    suspend fun getComment(
        commentid:Int?,
        postid:String,
        time:String?,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getComment(commentid, postid, time))
        }
    }
    suspend fun getHotComment(
        commentid:Int?,
        postid:String,
        likecount:Int?,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.getHotComment(commentid, postid,likecount))
        }
    }
    suspend fun getAnonymous(
        postid: String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getAnonymous(postid))
        }
    }

    suspend fun toggleLikePost(
        date:String,
        togglemy:Boolean,
        postuserid:Int,
        postid: String,
        isLiked: Int,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.toggleLikePost(date,togglemy,postuserid,postid, isLiked))
        }
    }
    suspend fun toggleBookmarkPost(
        postid: String,
        isMarked: Int,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall{
            Resource.Success(api.toggleBookmarkPost(postid, isMarked))
        }
    }

    suspend fun getSelectedPost(
        postid:String,
        latitude: Double?,
        longitude: Double?,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getSelectedPost(postid, latitude, longitude))
        }
    }
    suspend fun toggleLikeTag(
        tagname: String,
        count:Int,
        isLiked:Int,
        api:MainApi
    )=withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.toggleLikeTag(tagname, count, isLiked))
        }
    }
    suspend fun getSearchedTag(
        tagname: String,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getSearchedTag(tagname))
        }
    }
    suspend fun getFavoriteTag(
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getFavoriteTag())
        }
    }
    suspend fun getPopularTag(
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getPopularTag())
        }
    }
    suspend fun searchTag(
        tagname: String,
        api: MainApi
    ) = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(api.searchTag(tagname))
        }
    }

    suspend fun getNearPosts(
        lastpostnum:Int?,
        lastpostdate:String?,
        distancemax: Int,
        latitude: Double,
        longitude: Double,
        api:MainApi
    ) = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(api.getNearPosts(lastpostnum,lastpostdate,distancemax, latitude, longitude))
        }
    }
    suspend fun getTagNewPosts(
        tagname:String,
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getTagNewPosts(tagname,lastpostnum, lastpostdate, latitude, longitude))
        }
    }
    suspend fun getNewPosts(
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getNewPosts(lastpostnum, lastpostdate, latitude, longitude))
        }
    }
    suspend fun getuserPosts(
        userid:Int,
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        limit:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getuserPosts(userid,lastpostnum, lastpostdate, latitude, longitude,limit))
        }
    }
    suspend fun getuserContents(
        userid:Int,
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        limit:Int,
        type:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getuserContents(userid,lastpostnum, lastpostdate, latitude, longitude,limit,type))
        }
    }
    suspend fun getFollowingPosts(
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getFollowingPosts(lastpostnum, lastpostdate, latitude, longitude))
        }
    }
    suspend fun getBookmarkedPosts(
        lastpostnum: Int?,
        lastpostdate: String?,
        latitude: Double?,
        longitude: Double?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getBookmarkedPosts(lastpostnum, lastpostdate, latitude, longitude))
        }
    }
    suspend fun getmyPosts(
        lastpostnum: Int?,
        lastpostdate: String?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getmyPosts(lastpostnum, lastpostdate))
        }
    }
    suspend fun getTagHotPosts(
        tagname: String,
        lastpostnum: Int?,
        lastposthot: Int?,
        latitude: Double?,
        longitude: Double?,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getTagHotPosts(tagname,lastpostnum, lastposthot, latitude, longitude))
        }
    }
    suspend fun getTagLiked(
        tagname: String,
        api: MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getTagLiked(tagname))
        }
    }
    suspend fun getHotPosts(
        lastpostnum: Int?,
        lastposthot: Int?,
        latitude: Double?,
        longitude: Double?,
        limit:Int,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getHotPosts(lastpostnum, lastposthot, latitude, longitude,limit))
        }
    }
    suspend fun getHotContents(
        lastpostnum: Int?,
        lastposthot: Int?,
        latitude: Double?,
        longitude: Double?,
        limit:Int,
        type:String,
        api:MainApi
    )= withContext(Dispatchers.IO){
        safeCall {
            Resource.Success(api.getHotContents(lastpostnum, lastposthot, latitude, longitude,limit,type))
        }
    }
}



