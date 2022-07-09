package com.example.appportfolio.api.build

import com.example.appportfolio.api.responses.*
import com.example.appportfolio.data.entities.TagResult
import okhttp3.MultipartBody
import retrofit2.http.*

interface MainApi {
    @FormUrlEncoded
    @POST("/toggleFollow")
    suspend fun toggleFollow(
        @Field("userid")userid : Int,
        @Field("following")following:Int
    ):intResponse
    @FormUrlEncoded
    @POST("/checknick")
    suspend fun checknick(
        @Field("nickname")nickname : String
    ):intResponse
    @FormUrlEncoded
    @POST("/getSearchedFollowingPerson")
    suspend fun getSearchedFollowingPerson(
        @Field("lastuserid")lastuserid : Int?,
        @Field("nickname")nickname:String
    ):getpersonResponse
    @FormUrlEncoded
    @POST("/getHotUsers")
    suspend fun getHotUsers(
        @Field("lastuserid")lastuserid : Int?,
        @Field("lastuserfollow")lastuserfollow:Int?
    ):getpersonResponse
    @FormUrlEncoded
    @POST("/getFollowingPerson")
    suspend fun getFollowingPerson(
        @Field("lastuserid")lastuserid : Int?,
        @Field("userid")userid : Int?
    ):getpersonResponse
    @FormUrlEncoded
    @POST("/getFollowerPerson")
    suspend fun getFollowerPerson(
        @Field("lastuserid")lastuserid : Int?,
        @Field("userid")userid : Int?
    ):getpersonResponse
    @FormUrlEncoded
    @POST("/getSearchedPerson")
    suspend fun getSearchedPerson(
        @Field("lastuserid")lastuserid : Int?,
        @Field("nickname")nickname:String
    ):getpersonResponse
    @FormUrlEncoded
    @POST("/postContents")
    suspend fun postContents(
        @Field("postid")postid : String,
        @Field("anonymous")anonymous:String,
        @Field("text")text:String,
        @Field("tags")tags:String?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?,
        @Field("image")image:String,
        @Field("audio")audio:String,
        @Field("voteoptions")voteoptions:String?
    ): String
    @FormUrlEncoded
    @POST("/getSelectedPost")
    suspend fun getSelectedPost(
        @Field("postid")postid:String,
        @Field("latitude")latitude: Double?,
        @Field("longitude")longitude: Double?
    ):getPostResponse
    @FormUrlEncoded
    @POST("/getVoteResult")
    suspend fun getVoteResult(
        @Field("postid")postid:String
    ):getvoteresultResponse
    @FormUrlEncoded
    @POST("/vote")
    suspend fun vote(
        @Field("postid")postid:String,
        @Field("optionid")optionid:Int
    ):getvoteresultResponse

    @FormUrlEncoded
    @POST("/acceptchat")
    suspend fun acceptchat(
        @Field("roomid")roomid: String,
        @Field("organizer")organizer: Int,
        @Field("participant")participant: Int
    ):getchatrequestsResponse
    @FormUrlEncoded
    @POST("/refusechat")
    suspend fun refusechat(
        @Field("roomid")roomid: String,
        @Field("userid")userid: Int
    ):getchatrequestsResponse

    @POST("/getchatrequests")
    suspend fun getchatrequests(
    ):getchatrequestsResponse
    @FormUrlEncoded
    @POST("/requestchat")
    suspend fun requestchat(
        @Field("userid")userid:Int,
        @Field("roomid")roomid:String
    ):intResponse
    @FormUrlEncoded
    @POST("/getuserprofile")
    suspend fun getuserprofile(
        @Field("userid")userid: Int
    ):getprofileResponse
    @FormUrlEncoded
    @POST("/deletepost")
    suspend fun deletepost(
        @Field("postid")postid:String
    ):intResponse
    @FormUrlEncoded
    @POST("/deletereply")
    suspend fun deletereply(
        @Field("commentid")commentid:Int
    ):intResponse
    @FormUrlEncoded
    @POST("/deletecomment")
    suspend fun deletecomment(
        @Field("ref")ref:Int
    ):intResponse
    @FormUrlEncoded
    @POST("/report")
    suspend fun report(
        @Field("postid")postid:String?,
        @Field("commentid")commentid:Int?,
        @Field("reporttype")reporttype: String
    ):intResponse
    @FormUrlEncoded
    @POST("/blockcommentuser")
    suspend fun blockcommentuser(
        @Field("anonymous")anonymous:Boolean,
        @Field("blockuserid")blockuserid: Int,
        @Field("popback")popback:Boolean

    ):intResponse
    @FormUrlEncoded
    @POST("/blockpostuser")
    suspend fun blockpostuser(
        @Field("anonymous")anonymous:Boolean,
        @Field("blockuserid")blockuserid:Int
    ):intResponse
    @FormUrlEncoded
    @POST("/blockchatuser")
    suspend fun blockchatuser(
        @Field("anonymous")anonymous:Boolean,
        @Field("blockuserid")blockuserid:Int
    ):intResponse
    @POST("/readAllNoti")
    suspend fun readAllNoti(
        ):intResponse
    @POST("/getBlocks")
    suspend fun getBlocks(
    ):blocksResponse
    @FormUrlEncoded
    @POST("/deleteBlock")
    suspend fun deleteBlock(
        @Field("userid")userid:Int,
        @Field("blockeduserid")blockeduserid:Int
    ):intResponse
    @POST("/deleteAllNoti")
    suspend fun deleteAllNoti(
    ):intResponse

    @FormUrlEncoded
    @POST("/readNoti")
    suspend fun readNoti(
        @Field("notiid")notiid:Int
    ):intResponse

    @POST("/checkNotiUnread")
    suspend fun checkNotiUnread(
    ):intResponse
    @FormUrlEncoded
    @POST("/getNotis")
    suspend fun getNotis(
        @Field("notiid")notiid: Int?,
        @Field("date")date:String?
    ):getNotiResponse
    @FormUrlEncoded
    @POST("/getReply")
    suspend fun getReply(
        @Field("ref")ref: Int,
        @Field("commentid")commentid:Int?,
        @Field("time")time:String?
        ):commentResponse
    @FormUrlEncoded
    @POST("/checkSelectedComment")
    suspend fun checkSelectedComment(
        @Field("commentuserid")commentuserid:Int?,
        @Field("postuserid")postuserid: Int?,
        @Field("commentid")commentid:Int,
        @Field("postid")postid: String
    ):commentResponse
    @FormUrlEncoded
    @POST("/postReply")
    suspend fun postReply(
        @Field("ref")ref: Int,
        @Field("postid")postid: String,
        @Field("commentid")commentid:Int,
        @Field("anonymous")anonymous: String,
        @Field("text")text:String,
        @Field("postuserid")postuserid:Int,
        @Field("commentuserid")commentuserid:Int
    ):commentResponse
    @FormUrlEncoded
    @POST("/toggleComment")
    suspend fun toggleComment(
        @Field("rootcommentid")rootcommentid:Int?,
        @Field("commentuserid")commentuserid: Int?,
        @Field("depth")depth: Int?,
        @Field("postid")postid:String?,
        @Field("commentid")commentid: Int,
        @Field("isLiked")isLiked:Int
    ):intResponse

    @FormUrlEncoded
    @POST("/postComment")
    suspend fun postComment(
        @Field("postuserid")postuserid: Int?,
        @Field("postid")postid: String,
        @Field("anonymous")anonymous: String,
        @Field("text")text:String
    ):commentResponse

    @FormUrlEncoded
    @POST("/getComment")
    suspend fun getComment(
        @Field("commentid")commentid:Int?,
        @Field("postid")postid:String,
        @Field("time")time:String?
    ):commentResponse

    @FormUrlEncoded
    @POST("/getHotComment")
    suspend fun getHotComment(
        @Field("commentid")commentid:Int?,
        @Field("postid")postid:String,
        @Field("likecount")likecount:Int?
    ):commentResponse

    @FormUrlEncoded
    @POST("/getAnonymous")
    suspend fun getAnonymous(
        @Field("postid")postid: String
    ):NicknameResponse
    @FormUrlEncoded
    @POST("/toggleLikePost")
    suspend fun toggleLikePost(
        @Field("togglemy")togglemy:Boolean,
        @Field("postuserid")postuserid:Int,
        @Field("postid")postid:String,
        @Field("isLiked")isLiked:Int
    ): togglepostResponse

    @FormUrlEncoded
    @POST("/toggleBookmarkPost")
    suspend fun toggleBookmarkPost(
        @Field("postid")postid:String,
        @Field("isMarked")isMarked:Int
    ): togglepostResponse

    @FormUrlEncoded
    @POST("/toggleLikeTag")
    suspend fun toggleLikeTag(
        @Field("tagname")tagname:String,
        @Field("count")count:Int,
        @Field("isLiked")isLiked:Int
    ): TagResult

    @FormUrlEncoded
    @POST("/getpolloptions")
    suspend fun getpolloptions(
        @Field("postid")postid:String
    ): getpolloptionResponse

    @POST("/getFavoriteTag")
    suspend fun getFavoriteTag(
        ): TagSearchResponse

    @FormUrlEncoded
    @POST("/getSearchedTag")
    suspend fun getSearchedTag(
        @Field("tagname")tagname:String
    ): TagSearchResponse


    @POST("/getPopularTag")
    suspend fun getPopularTag(
    ): TagSearchResponse

    @POST("/getChatProfiles")
    suspend fun getChatProfiles(
    ):getRoomProfilesResponse

    @FormUrlEncoded
    @POST("/searchTag")
    suspend fun searchTag(
        @Field("tagname")tagname : String
    ): TagSearchResponse

    @FormUrlEncoded
    @POST("/getNearPosts")
    suspend fun getNearPosts(
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastpostdate")lastpostdate:String?,
        @Field("distancemax")distancemax : Int,
        @Field("latitude")latitude:Double,
        @Field("longitude")longitude:Double

    ): getPostResponse

    @FormUrlEncoded
    @POST("/getTagNewPosts")
    suspend fun getTagNewPosts(
        @Field("tagname")tagname: String,
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastpostdate")lastpostdate:String?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?

    ): getPostResponse

    @FormUrlEncoded
    @POST("/getTagLiked")
    suspend fun getTagLiked(
        @Field("tagname")tagname: String
    ):intResponse

    @FormUrlEncoded
    @POST("/getNewPosts")
    suspend fun getNewPosts(
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastpostdate")lastpostdate:String?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?

    ): getPostResponse

    @FormUrlEncoded
    @POST("/getuserPosts")
    suspend fun getuserPosts(
        @Field("userid")userid:Int?,
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastpostdate")lastpostdate:String?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?,
        @Field("limit")limit:Int

    ): getPostResponse
    @FormUrlEncoded
    @POST("/getuserContents")
    suspend fun getuserContents(
        @Field("userid")userid:Int?,
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastpostdate")lastpostdate:String?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?,
        @Field("limit")limit:Int,
        @Field("type")type:String
    ): getPostResponse
    @FormUrlEncoded
    @POST("/checkuser")
    suspend fun checkuser(
        @Field("userid")userid:Int?
    ): checkUserResponse
    @FormUrlEncoded
    @POST("/getFollowingPosts")
    suspend fun getFollowingPosts(
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastpostdate")lastpostdate:String?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?

    ): getPostResponse
    @FormUrlEncoded
    @POST("/getmyPosts")
    suspend fun getmyPosts(
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastpostdate")lastpostdate:String?
    ): getPostResponse
    @FormUrlEncoded
    @POST("/getBookmarkedPosts")
    suspend fun getBookmarkedPosts(
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastpostdate")lastpostdate:String?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?
    ): getPostResponse
    @FormUrlEncoded
    @POST("/getTagHotPosts")
    suspend fun getTagHotPosts(
        @Field("tagname")tagname: String,
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastposthot")lastposthot:Int?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?
    ): getPostResponse
    @FormUrlEncoded
    @POST("/editprofile")
    suspend fun editprofile(
        @Field("imageuri")imageuri:String?,
        @Field("nickname")nickname:String
    ): intResponse

    @FormUrlEncoded
    @POST("/getHotPosts")
    suspend fun getHotPosts(
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastposthot")lastposthot:Int?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?,
        @Field("limit")limit:Int
    ): getPostResponse

    @FormUrlEncoded
    @POST("/getHotContents")
    suspend fun getHotContents(
        @Field("lastpostnum")lastpostnum:Int?,
        @Field("lastposthot")lastposthot:Int?,
        @Field("latitude")latitude:Double?,
        @Field("longitude")longitude:Double?,
        @Field("limit")limit:Int,
        @Field("type")type:String
    ): getPostResponse


    @Multipart
    @POST("/uploadmultiple")
    suspend fun uploadImageRequest(
        @Part imageFile: List<MultipartBody.Part>): uploadImagesResponse
    @Multipart
    @POST("/uploadimg")
    suspend fun uploadimg(
        @Part imageFile: MultipartBody.Part): uploadImageResponse
    @Multipart
    @POST("/uploadaudio")
    suspend fun postImageRequest(
        @Part media: MultipartBody.Part): uploadAudioResponse
}