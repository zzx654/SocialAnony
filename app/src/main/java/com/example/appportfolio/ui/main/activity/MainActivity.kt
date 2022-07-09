package com.example.appportfolio.ui.main.activity


import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.SignManager
import com.example.appportfolio.auth.Status
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.*
import com.example.appportfolio.databinding.ActivityMainBinding
import com.example.appportfolio.other.Constants.TAG_CHAT
import com.example.appportfolio.other.Constants.TAG_HOME
import com.example.appportfolio.other.Constants.TAG_MYPAGE
import com.example.appportfolio.other.Constants.TAG_NOTI
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.NetworkConnection
import com.example.appportfolio.ui.auth.activity.AuthActivity
import com.example.appportfolio.ui.main.fragments.*
import com.example.appportfolio.ui.main.viewmodel.ChatViewModel
import com.example.appportfolio.ui.main.viewmodel.NotiViewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import java.net.URISyntaxException
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    var isConnected:Boolean?=null
    private lateinit var sharedPreferences:SharedPreferences
    private lateinit var prefEditor:SharedPreferences.Editor
    @Inject
    lateinit var signManager: SignManager
    private lateinit var chatRooms:List<ChatData>
    lateinit var mSocket: Socket
    private lateinit var vmAuth: AuthViewModel
    private lateinit var vmNoti:NotiViewModel
    private lateinit var vmChat:ChatViewModel
    lateinit var binding: ActivityMainBinding
    private lateinit var curchatrequests:List<ChatRequests>
    private lateinit var applyresult:(Status, String?, String?)->Unit
    lateinit var authapi: AuthApi
    lateinit var mainapi:MainApi
    private lateinit var fcmToken:String
    private var roomProfiles:MutableList<RoomProfile> = mutableListOf()
    var platform:String?=null
    var account:String?=null
    private var token:String?=null
    private val backStack: ArrayDeque<String> = ArrayDeque()
    private var getChats: LiveData<List<ChatData>>?=null
    private var gson: Gson = Gson()
    private var currentTab:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val connection = NetworkConnection(this)
        connection.observe(this) { isconnected ->
            if(isConnected==null){
                changeFragment(TAG_HOME, HomeFragment())
                backStack.addLast("main")
            }
            isConnected=isconnected
        }
        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this)
        prefEditor=sharedPreferences.edit()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        binding.flFragmentContainer.setPadding(0,0,0,58.dp)
        vmAuth = ViewModelProvider(this)[AuthViewModel::class.java]
        vmChat = ViewModelProvider(this)[ChatViewModel::class.java]

        vmNoti= ViewModelProvider(this)[NotiViewModel::class.java]
        setAccessToken()


        binding.bottomNavigationView.apply{
            setOnItemSelectedListener { item->
                when(item.itemId)
                {
                    R.id.homeFragment->changeFragment(TAG_HOME,HomeFragment())
                    R.id.chatroomFragment->changeFragment(TAG_CHAT, ChatroomFragment())
                    R.id.mypageFragment->changeFragment(TAG_MYPAGE, MypageFragment())
                    R.id.notificationFragment->changeFragment(TAG_NOTI, NotificationFragment())
                }
                true
            }
            setOnItemReselectedListener {
            }
            menu.getItem(4).isCheckable=false
            menu.getItem(4).setOnMenuItemClickListener {
                replaceFragment("uploadFragment", UploadFragment(),null)
                true
            }
        }

        getFirebaseToken()

        subscribeToObserver()
    }
    fun setAccessToken()
    {
        val preferences= UserPreferences(this)
        vmAuth.setAccessToken(runBlocking { preferences.authToken.first() })
    }
    fun deleteBlockedcChatUser(userid:Int){
        roomProfiles.find{profile-> profile.userid==userid}?.let{
            roomProfiles-=it
            chatRooms.find{ room-> it.roomid==room.roomid}?.let{ chatdata->
                vmChat.deleteroom(chatdata.roomid)
            }
        }

    }
    override fun onResume() {
        super.onResume()
        if(vmAuth.userid.value!=null)
        {
            vmAuth.checkfcmtoken(fcmToken,authapi)
            checkunreadnoti()
            vmChat.getchatrequests(mainapi)
        }
    }
    private fun setChatRooms(){
        var newchatrooms:List<Chatroom> = listOf()
        chatRooms.map{ room->
            val profile=roomProfiles.find{profile-> profile.roomid == room.roomid }
            profile?.let{ roomprofile->
                val profileimage=if(roomprofile.profileimage==null) "none" else roomprofile.profileimage
                val ismy=if(room.senderid==vmAuth.userid.value!!)1 else 0
                val chatroom=Chatroom(if(room.type == "EXIT")0 else roomprofile.userid,if(room.type.equals("EXIT")) "none" else profileimage,
                    if(room.type == "EXIT") "비공개" else roomprofile.gender,if(room.type == "EXIT") "대화상대없음" else roomprofile.nickname,
                    ismy,room.senderid!!,room.roomid,room.date,
                    room.type,room.content,room.isread!!)
                newchatrooms+=chatroom
            }
        }
        if(chatRooms.isNotEmpty()&&newchatrooms.isEmpty())
            vmChat.getRoomProfiles(mainapi)//프로필목록이 없을시 불러옴
        else
            vmChat.setChats(newchatrooms.toList())
    }
    private fun subscribeToObserver() {

        vmNoti.getNewNotiResponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            handleResponse(this,it.resultCode){
                val oldNotis=vmNoti.curnotis.value
                oldNotis?.let{ notis->
                    if(notis.isNotEmpty()&&it.notis.isNotEmpty()&&(notis[0].notiid!=it.notis[0].notiid))
                        vmNoti.setNotis(it.notis)
                }
            }
        })
        vmChat.getroomProfilesResponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            handleResponse(this,it.resultCode){

                getChats?.removeObservers(this)
                        roomProfiles=it.profiles.toMutableList()
                    getChats=vmChat.getAllChats()
                    getChats!!.observe(this){ chatrooms->
                        chatRooms=chatrooms
                            if(chatrooms.isEmpty())
                                vmChat.setChats(listOf())
                            else
                                setChatRooms()

                    }
                }
        })
        vmAuth.withdrawalResponse.observe(this,Event.EventObserver(
            onError = {
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            handleResponse(this,it.resultCode){
                if(it.resultCode==200)
                {
                    Intent(this, AuthActivity::class.java).also{
                        startActivity(it)
                    }
                    Toast.makeText(this,"회원 탈퇴되었습니다 그동안 이용해주셔서 감사합니다",Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

        })
        vmChat.getchatrequestsResponse.observe(this,Event.EventObserver {
            handleResponse(this,it.resultCode){
                if(it.resultCode==200)
                {
                    vmChat.setChatRequests(it.requests)
                    curchatrequests=it.requests
                    showchatbadge()
                }
                else
                {
                    vmChat.setChatRequests(listOf())
                    curchatrequests= listOf()
                }
            }

        })
        vmChat.mychatRequests.observe(this){ requests->
            vmChat.mychats.value?.let{
                if(!vmChat.mychats.value!!.any { chatroom->
                        chatroom.isread==0
                    }){
                    //안읽은메시지가없다면
                    if(requests.isEmpty())
                        hidechatbadge()
                    else
                        showchatbadge()
                }
                else{
                    //안읽은메시지있으면
                    showchatbadge()
                }
            }
        }
        vmChat.mychats.observe(this){

            if(vmChat.mychatRequests.value!!.isEmpty())
            {
                if(it.any { room-> room.isread==0 })
                    showchatbadge()
                else
                    hidechatbadge()
            }
            else
                showchatbadge()

        }
        vmAuth.getChatonoffResponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            handleResponse(this,it.resultCode){
                if(it.resultCode==200)
                {
                    if(it.value==1)
                        prefEditor.putBoolean("chatonoff",true)
                    else
                        prefEditor.putBoolean("chatonoff",false)
                    prefEditor.commit()
                }
                else{
                    Toast.makeText(this,"서버 오류발생",Toast.LENGTH_SHORT).show()
                }
            }
        })
        vmAuth.checkfcmresponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            if(it.resultCode==100)
            {//저장되어있는 fcmtoken이 다른경우 다른 기기 로그인이 되어있는경우
                logout()
            }
        })
        vmNoti.checkNotiUnreadResponse.observe(this,Event.EventObserver(

            onError={
                Toast.makeText(this,"알림 에러발생",Toast.LENGTH_SHORT).show()
            }
        ){
            handleResponse(this,it.resultCode){
                if(it.resultCode==200)
                    shownotibadge()
                else
                    hidenotibadge()
            }
        })
        vmAuth.getUseridResponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            vmChat.getRoomProfiles(mainapi)
            vmChat.getchatrequests(mainapi)
            handleResponse(this,it.resultCode){
                if(it.resultCode==200)
                {
                    vmAuth.setUserid(it.userid)
                    vmAuth.setPlatform(it.platform)
                    try{
                        mSocket= IO.socket("https://socialanony.herokuapp.com")
                        Log.d("SOCKET", "Connection success : " + mSocket.id())

                    }catch (e: URISyntaxException){
                        e.printStackTrace()
                    }
                    mSocket.connect()

                    mSocket.on(
                        Socket.EVENT_CONNECT
                    ) { args: Array<Any?>? ->
                        mSocket.emit(
                            "recordsocket",
                            gson.toJson(MyAccount(it.userid))
                        )
                        vmNoti.getNewNotis(mainapi)
                        vmChat.getRoomProfiles(mainapi)
                        vmAuth.checkfcmtoken(fcmToken,authapi)
                        checkunreadnoti()
                    }
                    mSocket.on("updatechatrequest") { args: Array<Any> ->
                        runOnUiThread {
                            showchatbadge()
                        }
                        var lst:List<ChatRequests> = listOf()
                        val array=JSONArray(args[0].toString())
                        for(i in 0 until array.length())
                        {
                            val jsonObj=array.getJSONObject(i)
                            val roomid=jsonObj.optString("roomid")
                            val organizer=jsonObj.optInt("organizer")
                            val participant=jsonObj.optInt("participant")
                            val joined=jsonObj.optInt("joined")
                            val nickname=jsonObj.optString("nickname")
                            val gender=jsonObj.optString("gender")
                            val profileimage=jsonObj.optString("profileimage")
                            lst+=ChatRequests(roomid,organizer,participant,joined,nickname,gender,profileimage)
                            vmChat.setChatRequests(lst)
                        }
                    }
                    mSocket.on("updaterooms") { args: Array<Any> ->

                        val chatdata=gson.fromJson(
                            args[0].toString(),
                            ReceivedChat::class.java
                        )
                        runOnUiThread {
                            showchatbadge()
                        }
                            roomProfiles +=RoomProfile(chatdata.roomid,chatdata.senderid,chatdata.profileimage,chatdata.gender,chatdata.nickname)
                            roomProfiles.find { it.roomid==chatdata.roomid }?.let{ foundprofile->
                                roomProfiles!! -=foundprofile
                            }
                        setChatRooms()
                    }
                    mSocket.on("updatenoti"){ args:Array<Any> ->
                        val notidata=gson.fromJson(
                            args[0].toString(),
                            Noti::class.java
                        )
                        vmNoti.curnotis.value?.let{
                            var templist=it
                            val newnoti=listOf(notidata)
                            templist=newnoti+templist
                            vmNoti.setNotis(templist)

                        }
                        runOnUiThread{
                            shownotibadge()
                        }
                    }
                    mSocket.on("logout"){ args:Array<Any> ->
                        logout(false)
                    }
                    vmAuth.getChatonoff(authapi)
                }
            }
        })
        vmAuth.accessToken.observe(this) { accesstoken ->
            token = accesstoken
            authapi = RemoteDataSource().buildApi(AuthApi::class.java, token)
            mainapi=RemoteDataSource().buildApi(MainApi::class.java, token)
            vmAuth.getUserid(authapi)
        }
        vmAuth.logoutResponse.observe(this, Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            },
            onLoading = {
            }
        ){
            handleResponse(this,it.resultCode){
                if(it.resultCode==200){
                    Intent(this, AuthActivity::class.java).also{
                        startActivity(it)
                    }
                    finish()
                }
            }
        })
    }
    fun deleteroom(roomid:String)
    {
        var Chatlist=vmChat.mychats.value!!
        val foundchat=Chatlist.find{chatroom-> chatroom.roomid == roomid }
        foundchat?.let{
            Chatlist-=it
        }
        vmChat.setChats(Chatlist)
    }
    fun setupTopBottom()
    {
        this.window.statusBarColor=ContextCompat.getColor(this, R.color.skin)
        backStack.removeLast()
        if(backStack.last()=="main")
        {
            setupToolBarMenu(currentTab!!)
            binding.toolbar.visibility=if(currentTab== TAG_HOME) View.GONE else View.VISIBLE
            binding.linetop.visibility=if(currentTab== TAG_HOME) View.GONE else View.VISIBLE
            binding.bottomNavigationView.visibility=View.VISIBLE
            binding.linebottom.visibility=View.VISIBLE
            binding.flFragmentContainer.setPadding(0,0,0,58.dp)
        }
        else
            setToolBarVisible(backStack.last())

    }
    @SuppressLint("RestrictedApi")
    private fun setupToolBarMenu(tag:String)
    {
        when(tag)
        {
            TAG_CHAT->binding.title.text="대화"

            TAG_NOTI->binding.title.text="알림"

            TAG_MYPAGE->binding.title.text="마이페이지"
        }
        supportActionBar?.apply {
            if(tag== TAG_NOTI)
                setDisplayHomeAsUpEnabled(true)
            else
                setDisplayHomeAsUpEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_complete)
            setDisplayShowTitleEnabled(false)
            invalidateOptionsMenu()
        }
    }
     fun setToolBarVisible(tag:String)
    {
        when(tag)
        {
            "imageFragment","setPasswordFragment","fullImagesFragment"->{
                binding.toolbar.visibility=View.GONE
                binding.linetop.visibility=View.GONE
            }
            "tagPostsFragment"->{
                binding.toolbar.visibility=View.VISIBLE
                binding.linetop.visibility=View.GONE
            }
            else->{
                binding.linetop.visibility=View.VISIBLE
                binding.toolbar.visibility=View.VISIBLE
            }
        }
    }
    fun replaceFragment(tag:String,fragment: Fragment,bundle: Bundle?)
    {
        if(tag=="imageFragment"||tag=="fullImagesFragment")
            this.window.statusBarColor=ContextCompat.getColor(this, R.color.black)
        backStack.addLast(tag)

        bundle?.let{
            fragment.arguments = bundle
        }
        binding.flFragmentContainer.setPadding(0,0,0,0)
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.addToBackStack(null)
            .replace(R.id.navHostFragment, fragment)
            .commitAllowingStateLoss()
        binding.bottomNavigationView.visibility=View.INVISIBLE
        binding.linebottom.visibility=View.INVISIBLE
    }
    private fun changeFragment(tag:String, fragment: Fragment)
    {
        setupToolBarMenu(tag)
        if(tag==TAG_HOME)
        {
            binding.linetop.visibility=View.GONE
            binding.toolbar.visibility=View.GONE
        }
        else
        {
            binding.linetop.visibility=View.VISIBLE
            binding.toolbar.visibility=View.VISIBLE
        }

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        if(supportFragmentManager.findFragmentByTag(tag) ==null) {
            ft.add(R.id.navHostFragment, fragment!!, tag)
        }
        val home= supportFragmentManager.findFragmentByTag(TAG_HOME)
        val chat= supportFragmentManager.findFragmentByTag(TAG_CHAT)
        val noti= supportFragmentManager.findFragmentByTag(TAG_NOTI)
        val mypage=supportFragmentManager.findFragmentByTag(TAG_MYPAGE)
        //모든 프래그먼트 hide
        if(home!=null)
            ft.hide(home)
        if(chat!=null)
            ft.hide(chat)
        if(noti!=null)
            ft.hide(noti)
        if(mypage!=null)
            ft.hide(mypage)
        when(tag)
        {
            TAG_HOME->{
                home?.let{
                    ft.show(it)
                }
            }
            TAG_CHAT->{
                chat?.let{
                    ft.show(it)
                }
            }
            TAG_MYPAGE->{
                mypage?.let{
                    ft.show(it)
                }
            }
            TAG_NOTI->{
                noti?.let{
                    ft.show(it)
                }
            }
        }
         ft.commitNow()
        currentTab=tag
    }
    fun getmyprofile()
    {
        vmAuth.getmyprofile(authapi)
    }
    private fun shownotibadge()
    {
        binding.bottomNavigationView.getOrCreateBadge(R.id.notificationFragment).apply{
            clearNumber()
            isVisible=true
        }
    }
    private fun showchatbadge()
    {
        binding.bottomNavigationView.getOrCreateBadge(R.id.chatroomFragment).apply{
            clearNumber()
            isVisible=true
        }
    }
    private fun hidechatbadge(){
        binding.bottomNavigationView.getOrCreateBadge(R.id.chatroomFragment).apply{
            clearNumber()
            isVisible=false
        }
    }
    fun hidenotibadge()
    {
        binding.bottomNavigationView.getOrCreateBadge(R.id.notificationFragment).apply{
            clearNumber()
            isVisible=false
        }
    }
    fun checkunreadnoti()
    {
        vmNoti.checkNotiUnread(mainapi)
    }
    fun setChatReceive(toggle:Boolean)
    {
        prefEditor.putBoolean("chatonoff",toggle)
        prefEditor.commit()
    }
    fun toggleChatReceive(toggle:Boolean)
    {
        val toggleparam:Int = if(toggle) 1
        else
            0
        vmAuth.togglechat(toggleparam,authapi)
    }


    private val Int.dp:Int
        get() {
            val metrics=resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),metrics).toInt()
        }

    override fun onBackPressed() {
    }
    fun withdrawal()
    {
        applyresult={ status,str1,str2->
            if(status==Status.SUCCESS||status==Status.NOTFOUND)
            {
                vmAuth.withdrawal(authapi)
            }
            else{
                Toast.makeText(this, "로그아웃 실패$str1",Toast.LENGTH_SHORT).show()
            }
        }
        signManager.signout(applyresult)
    }
    fun logout(deletetoken:Boolean=true)
    {
        applyresult={ status,str1,str2->
            if(status==Status.SUCCESS||status==Status.NOTFOUND)
            {
                vmChat.deleteAllrooms()
                vmAuth.logout(authapi)
            }
            else{
                Toast.makeText(this, "로그아웃 실패$str1",Toast.LENGTH_SHORT).show()
            }
        }
        signManager.signout(applyresult)
    }
    private fun getFirebaseToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fcmToken=task.result!!
                vmAuth.setfcmtoken(task.result!!)
            }
        }
    }
}