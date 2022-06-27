package com.example.appportfolio.other

object Constants {
    const val TAG_HOME="home_fragment"
    const val TAG_CHAT="chatroom_fragment"
    const val TAG_NOTI="notification_fragment"
    const val TAG_MYPAGE="mypage_fragment"


    const val SEARCH_TIME_DELAY=500L
    const val  ACTION_STOP_SERVICE="ACTION_STOP_SERVICE"
    const val  ACTION_RECORD_PLAY="ACTION_RECORD_PLAY"
    const val RECORD_MAX=300000
    const val NOTIFICATION_CHANNEL_ID="media_channel"
    const val NOTIFICATION_CHANNEL_NAME="media"
    const val NOTIFICATION_ID=1
    const val TOGGLE_PLAY="TOGGLE_PLAY"

    const val PAGE_SIZE:Long=20

    const val COMMENT_VIEW_TYPE=0
    const val REPLY_VIEW_TYPE=1
    const val LOADING_VIEW_TYPE=3

    const val COMMENTADDED=1
    const val POSTLIKED=2
    const val COMMENTLIKED=3
    const val REPLYADDED=5
    //신고,차단,대화요청,삭제

    const val REPORT="REPORT"
    const val BLOCK="BLOCK"
    const val CHAT="CHAT"
    const val DELETE="DELETE"
    const val TMAP_URL="https://apis.openapi.sk.com"

    const val GET_TMAP_LOCATION="/tmap/pois"

    const val GET_TMAP_REVERSE_GEO_CODE="/tmap/geo/reversegeocoding"

    const val CAMERA_ZOOM_LEVEL = 17f

    const val NONE_HEADER=0
    const val RG_HEADER=1


    const val FOLLOWING=0
    const val FOLLOWER=1

    const val IMAGECONTENT=0
    const val AUDIOCONTENT=1
    const val VOTECONTENT=2
}