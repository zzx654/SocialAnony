package com.example.appportfolio.ui.main.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.appportfolio.*
import com.example.appportfolio.api.build.AuthApi
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.databinding.ActivityMainBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.auth.SignManager
import com.example.appportfolio.auth.Status
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.*
import com.example.appportfolio.other.Constants.TAG_CHAT
import com.example.appportfolio.other.Constants.TAG_HOME
import com.example.appportfolio.other.Constants.TAG_MYPAGE
import com.example.appportfolio.other.Constants.TAG_NOTI
import com.example.appportfolio.other.Constants.TAG_POST
import com.example.appportfolio.ui.auth.activity.AuthActivity
import com.example.appportfolio.ui.auth.activity.AuthCompleteActivity
import com.example.appportfolio.ui.main.viewmodel.ChatViewModel
import com.example.appportfolio.ui.main.viewmodel.NavViewModel
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

    lateinit var sharedPreferences:SharedPreferences
    lateinit var prefEditor:SharedPreferences.Editor
    @Inject
    lateinit var signManager: SignManager
    lateinit var chatrooms:List<ChatData>
    lateinit var curReceivedChat: ReceivedChat
    lateinit var mSocket: Socket
    private lateinit var vmAuth:AuthViewModel
    private lateinit var vmNoti:NotiViewModel
    private lateinit var vmChat:ChatViewModel
    private lateinit var vmNav:NavViewModel
    lateinit var binding: ActivityMainBinding
    private lateinit var curchatrequests:List<ChatRequests>
    lateinit var applyresult:(Status, String?, String?)->Unit
    lateinit var authapi: AuthApi
    lateinit var mainapi:MainApi
    lateinit var fcmToken:String
    var platform:String?=null
    var account:String?=null
    var token:String?=null
    lateinit var navHostFragment:NavHostFragment
    private var gson: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this)
        prefEditor=sharedPreferences.edit()
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main)
        setSupportActionBar(binding.toolbar)
        vmAuth = ViewModelProvider(this).get(AuthViewModel::class.java)
        vmChat = ViewModelProvider(this).get(ChatViewModel::class.java)
        vmNav = ViewModelProvider(this).get(NavViewModel::class.java)
        val preferences= UserPreferences(this)
        vmNoti=ViewModelProvider(this).get(NotiViewModel::class.java)
        vmAuth.setAccessToken(runBlocking { preferences.authToken.first() })
        navHostFragment=supportFragmentManager.findFragmentByTag("navHostFragment")
                as NavHostFragment
        binding.bottomNavigationView.setOnItemSelectedListener { item->
            when(item.itemId)
            {
                R.id.homeFragment-> vmNav.setFrag(TAG_HOME)
                R.id.chatroomFragment->vmNav.setFrag(TAG_CHAT)
                R.id.notificationFragment->vmNav.setFrag(TAG_NOTI)
                R.id.mypageFragment->vmNav.setFrag(TAG_MYPAGE)
                R.id.uploadFragment->vmNav.setFrag(TAG_POST)
            }
            true
        }
        getFirebaseToken()
        navHostFragment.findNavController()
            //     navController
            .addOnDestinationChangedListener { controller, destination, arguments ->
                when(destination.id){
                    R.id.mainFragment->
                    {

                        binding.bottomNavigationView.visibility= View.VISIBLE
                        binding.linebottom.visibility=View.VISIBLE
                        binding.flFragmentContainer.setPadding(0,0,0,58.dp)
                    }
                    else-> {
                        binding.flFragmentContainer.setPadding(0,0,0,0)
                        binding.bottomNavigationView.visibility=View.GONE
                        binding.linebottom.visibility=View.GONE
                    }
                }
                when(destination.id)
                {
                    R.id.mainFragment->{
                        val curfrag=vmNav.destinationFragment.value
                        curfrag?.let{
                            when(it)
                            {
                                TAG_HOME->{
                                    binding.toolbar.visibility=View.GONE
                                    binding.linetop.visibility=View.GONE
                                }
                                else->{
                                    supportActionBar?.apply {
                                        setDisplayHomeAsUpEnabled(false)
                                        setDisplayShowTitleEnabled(false)
                                    }
                                    binding.linetop.visibility=View.VISIBLE
                                    binding.toolbar.visibility=View.VISIBLE
                                }
                            }
                        }

                    }
                    R.id.imageFragment,R.id.setPasswordFragment->{
                        binding.toolbar.visibility=View.GONE
                        binding.linetop.visibility=View.GONE
                    }
                    R.id.tagPostsFragment->{
                        supportActionBar?.apply {
                            setDisplayHomeAsUpEnabled(false)
                            setDisplayShowTitleEnabled(false)
                        }
                        binding.toolbar.visibility=View.VISIBLE
                        binding.linetop.visibility=View.GONE
                    }
                    else->{
                        supportActionBar?.apply {
                            setDisplayHomeAsUpEnabled(false)
                            setDisplayShowTitleEnabled(false)
                        }
                        binding.linetop.visibility=View.VISIBLE
                        binding.toolbar.visibility=View.VISIBLE
                    }
                }
            }
        subscribeToObserver()
    }
    fun getgooglemail():String
    {
        return signManager.getGoogleMail()
    }
    fun getmyprofile()
    {
        vmAuth.getmyprofile(authapi)
    }
    fun shownotibadge()
    {
        binding.bottomNavigationView.getOrCreateBadge(R.id.notificationFragment).apply{
            clearNumber()
            isVisible=true
        }
    }
    fun showchatbadge()
    {
        binding.bottomNavigationView.getOrCreateBadge(R.id.chatroomFragment).apply{
            clearNumber()
            isVisible=true
        }
    }
    fun hidechatbadge(){
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
        var toggleparam:Int
        if(toggle)toggleparam=1
        else
            toggleparam=0
        vmAuth.togglechat(toggleparam,authapi)
    }

    override fun onResume() {
        super.onResume()
        if(vmAuth.userid.value!=null&&mainapi!=null)
        {
            vmAuth.checkfcmtoken(fcmToken,authapi)
            checkunreadnoti()
            val getchats=vmChat.getAllChats()
            getchats.observe(this){
                chatrooms=it

                if(it.isEmpty())
                {
                    vmChat.setChats(listOf())
                    vmChat.getchatrequests(mainapi)
                }
                else{
                    vmChat.getRoomProfiles(mainapi)
                }
                getchats.removeObservers(this)
            }
        }
    }
    val Int.dp:Int
        get() {
            val metrics=resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),metrics).toInt()
        }
    private fun subscribeToObserver() {
        vmChat.mychats.observe(this){

            if(vmChat.mychatRequests.value!!.isEmpty())
            {
                var unreadexist=false
                for(i in it)
                {
                    if(i.isread==0)
                    {
                        unreadexist=true
                        break
                    }
                }
                if(unreadexist)
                    showchatbadge()
                else
                    hidechatbadge()
            }
        }
        vmChat.getroomProfilesResponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            if(it.resultCode==200)
            {
                var unreadexist=false
                var newchatrooms:List<Chatroom> = listOf()
                val profiles=it.profiles
                for(i in chatrooms.indices)
                {
                    if(chatrooms[i].isread==0&&chatrooms[i].senderid!=vmAuth.userid.value!!)
                        unreadexist=true
                    val profile=profiles.find{ profile-> profile.roomid.equals(chatrooms[i].roomid)}
                    val profileimage=if(profile!!.profileimage==null) "none" else profile!!.profileimage
                    val ismy=if(chatrooms[i].senderid==vmAuth.userid.value!!)1 else 0
                    val chatroom=Chatroom(profile!!.userid,profileimage,profile!!.gender,profile!!.nickname,
                        ismy,chatrooms[i].senderid!!,chatrooms[i].roomid,chatrooms[i].date,
                        chatrooms[i].type,chatrooms[i].content,chatrooms[i].isread!!)
                    newchatrooms+=chatroom
                }
                if(unreadexist)
                    showchatbadge()//처음 채팅방리스트불러올때 안읽은게 있으면 showchatbadge
                vmChat.setChats(newchatrooms.toList())
                vmChat.getchatrequests(mainapi)
            }
        })
        vmAuth.userid.observe(this){
        }
        vmAuth.withdrawalResponse.observe(this,Event.EventObserver(
            onError = {
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            if(it.resultCode==200)
            {
                Intent(this, AuthActivity::class.java).also{
                    startActivity(it)
                }
                Toast.makeText(this,"회원 탈퇴되었습니다 그동안 이용해주셔서 감사합니다",Toast.LENGTH_SHORT).show()
                finish()
            }
        })
        vmChat.getchatlistsResponse.observe(this,Event.EventObserver(
            onError = {
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            if(it.resultCode==200)
            {
                vmChat.setChats(it.rooms)
                if(curchatrequests.size==0)
                {
                    var unreadexist=false
                    for(room in it.rooms)
                    {
                        if(room.ismy==0&&room.isread==0)
                        {
                            unreadexist=true
                        }
                    }
                    if(unreadexist) showchatbadge()
                    else hidechatbadge()
                }
            }
            else
            {
                vmChat.setChats(listOf())
            }
        })
        vmChat.getchatrequestsResponse.observe(this,Event.EventObserver(
            onError ={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
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
        })
        vmChat.mychatRequests.observe(this){ requests->
            vmChat.mychats.value?.let{
                var unreadexist=false
                for(i in vmChat.mychats.value!!)
                {
                    if(i.isread==0)
                    {
                        unreadexist=true
                        break
                    }
                }
                if(!unreadexist)
                {
                    if(requests.size==0)
                        hidechatbadge()
                    else
                        showchatbadge()
                }
            }
        }
        vmAuth.getChatonoffResponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
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
                Toast.makeText(this,"알림 요청중 에러발생",Toast.LENGTH_SHORT).show()
            }
        ){
            if(it.resultCode==200)
                shownotibadge()
            else
                hidenotibadge()
            val getchats=vmChat.getAllChats()
            getchats.observe(this){
                chatrooms=it
                if(it.isEmpty())
                {
                    vmChat.setChats(listOf())
                    vmChat.getchatrequests(mainapi)
                }
                else{
                    vmChat.getRoomProfiles(mainapi)
                }
                getchats.removeObservers(this)
            }
        })
        vmAuth.getUseridResponse.observe(this,Event.EventObserver(
            onError={
                Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
            }
        ){
            if(it.resultCode==200)
            {
                vmAuth.setUserid(it.userid)
                vmAuth.setPlatform(it.platform)
                try{
                    mSocket= IO.socket("https://socialanony.herokuapp.com")
                    Log.d("SOCKET", "Connection success : " + mSocket.id());

                }catch (e: URISyntaxException){
                    e.printStackTrace();
                }
                mSocket.connect()

                mSocket.on(
                    Socket.EVENT_CONNECT
                ) { args: Array<Any?>? ->
                    mSocket.emit(
                        "recordsocket",
                        gson.toJson(MyAccount(it.userid))
                    )
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

                    curReceivedChat=chatdata
                    val chatcontent=ChatData(null,curReceivedChat.senderid,curReceivedChat.roomid,curReceivedChat.date,curReceivedChat.type,curReceivedChat.content,0)
                    vmChat.insertChat(chatcontent,curReceivedChat.dateChanged)
                    updatechatroom(chatcontent,
                        chatdata.profileimage ?: "none",chatdata.gender,chatdata.nickname,0,chatdata.senderid)
                }
                mSocket.on("updatenoti"){ args:Array<Any> ->
                    runOnUiThread{
                        shownotibadge()
                    }
                }
                mSocket.on("logout"){ args:Array<Any> ->
                    logout(false)
                }
                vmAuth.getChatonoff(authapi)
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
            if(it.resultcode==200){
                Intent(this, AuthActivity::class.java).also{
                    startActivity(it)
                }
                finish()
            }
        })
    }
    fun updatechatroom(chatdata:ChatData,profileimage:String,gender:String,nickname:String,isread:Int,opponentid:Int)
    {
        var oldChatlist= vmChat.mychats.value!!
        var newChatlist:List<Chatroom>
        val ismy=if(chatdata.senderid==vmAuth.userid.value!!)1 else 0
        val foundchat=oldChatlist.find{chatroom-> chatroom.roomid.equals(chatdata.roomid)}
        if(foundchat==null)
        {
            newChatlist=listOf(Chatroom(if(chatdata.type.equals("EXIT")) 0 else opponentid,if(chatdata.type.equals("EXIT")) "none" else profileimage,
                if(chatdata.type.equals("EXIT")) "비공개" else gender,if(chatdata.type.equals("EXIT")) "대화상대없음" else nickname,ismy,chatdata.senderid!!,chatdata.roomid,
                chatdata.date,chatdata.type,chatdata.content,isread))+oldChatlist
        }
        else
        {
            oldChatlist-=foundchat
            newChatlist=listOf(Chatroom(if(chatdata.type.equals("EXIT")) 0 else opponentid,if(chatdata.type.equals("EXIT")) "none" else profileimage,
                if(chatdata.type.equals("EXIT")) "비공개" else gender,if(chatdata.type.equals("EXIT")) "대화상대없음" else nickname,ismy,chatdata.senderid!!,chatdata.roomid,
                chatdata.date,chatdata.type,chatdata.content,isread))+oldChatlist
        }
        vmChat.setChats(newChatlist)
    }
    fun deleteroom(roomid:String)
    {
        var Chatlist=vmChat.mychats.value!!
        val foundchat=Chatlist.find{chatroom-> chatroom.roomid.equals(roomid)}
        foundchat?.let{
            Chatlist-=it
        }
        vmChat.setChats(Chatlist)
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
                Toast.makeText(this,"로그아웃 실패"+str1,Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this,"로그아웃 실패"+str1,Toast.LENGTH_SHORT).show()
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