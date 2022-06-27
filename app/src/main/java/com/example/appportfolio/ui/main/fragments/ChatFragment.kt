package com.example.appportfolio.ui.main.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.datetostr
import com.example.appportfolio.SocialApplication.Companion.getTodayString
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.SocialApplication.Companion.strtodate
import com.example.appportfolio.adapters.ChatAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.auth.UserPreferences
import com.example.appportfolio.data.entities.*
import com.example.appportfolio.databinding.FragmentChatBinding
import com.example.appportfolio.other.Event
import com.example.appportfolio.other.Keyboard
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.activity.LocationActivity
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.LoadingDialog
import com.example.appportfolio.ui.main.viewmodel.ChatViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment: Fragment(R.layout.fragment_chat) {
    private var mRootView:View?=null
    lateinit var vmAuth: AuthViewModel
    lateinit var vmChat: ChatViewModel
    private var gson: Gson = Gson()
    private var scrollbottom=true
    private var shouldScrollBottom=false
    private var shouldScroll=false
    private var unreadIndex=0
    private var loading=false
    lateinit var chatAdapter: ChatAdapter
    private var userprofileimage:String?="none"
    private var usernickname:String=""
    private var usergender:String=""
    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    lateinit var api: MainApi
    private var beforechatsSize=0
    private var lastFirstVisiblePosition:Int=0
    private var prescrollbottom=false
    private  var opponentid:Int=0
    private var snackbar: Snackbar?=null
    private var curChatContents:List<ChatData> = listOf()
    @Inject
    lateinit var userPreferences: UserPreferences
    @Inject
    lateinit var loadingDialog:LoadingDialog
    private val roomid:String
        get(){
            return arguments?.getString("roomid","")!!
        }
    lateinit var binding: FragmentChatBinding
    val keyboard = Keyboard() // 키보드 클래스의 객체
    lateinit var rootView: View // 루트 뷰
    var showcontainer=false
    var navigationBarHeight = 0
    var statusBarHeight = 0
    var isKeyboardShowing: Boolean = false // 현재 키보드 보이는지 여부
    val imm: InputMethodManager by lazy { // 키보드 매니저
        activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        showcontainer=false
        isKeyboardShowing=false
        binding.toolbox.visibility=View.GONE
        binding.toolBtn.setImageResource(R.drawable.tooladd)
        scrollbottom=true
        if (result.resultCode == Activity.RESULT_OK) {
            val myData: Intent? = result.data
            val lat = myData?.getFloatExtra("lat",0.toFloat())
            val lon=myData?.getFloatExtra("lon",0.toFloat())
            if (lat != null) {
                if(lat>0) {
                    val latlngstr=lat.toString()+"&"+lon.toString()
                    sendContent(latlngstr,"LOCATION")
                }
            }
        }
    }
    private val requestPermissionLauncher = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions() ) { result:Map<String, Boolean> ->
        val deniedList: List<String> = result.filter {
            !it.value
        }.map {
            it.key
        }
        when {
            deniedList.isNotEmpty() -> {
                val map = deniedList.groupBy { permission ->
                    if (shouldShowRequestPermissionRationale(permission)) "DENIED" else "EXPLAINED"
                }
                map["DENIED"]?.let {
                }
                map["EXPLAINED"]?.let {
                }
            } else -> {
            dispatchTakePictureIntent()
        }
        }
    }
    private lateinit var cropContent: ActivityResultLauncher<Any?>
    private val cropActivityResultContract = object: ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity()
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .getIntent(requireContext())
        }
        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }
    var m_imageFile: File? = null
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            var bmap:Bitmap?=null
            if(result.resultCode == Activity.RESULT_OK)
            {
                m_imageFile?.let{ file->
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val source = ImageDecoder.createSource(activity?.contentResolver!!, Uri.fromFile(file))
                        ImageDecoder.decodeBitmap(source).let {
                            bmap=it
                        }
                    }else{
                        MediaStore.Images.Media.getBitmap(activity?.contentResolver!!, Uri.fromFile(file))?.let {
                            bmap=it
                        }
                    }
                    bmap?.let{
                        val exif = ExifInterface(file.path)
                        val exifOrientation: Int = exif!!.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                        val exifDegree = exifOrientationToDegrees(exifOrientation)
                        bmap = rotate(bmap, exifDegree)!!
                        showcontainer=false
                        isKeyboardShowing=false
                        binding.toolbox.visibility=View.GONE
                        binding.toolBtn.setImageResource(R.drawable.tooladd)
                        uploadImage(getImageUri(requireContext(),bmap)!!,requireContext())
                    }
                }
            }
        }
    fun getImageUri(inContext: Context?, inImage: Bitmap?): Uri? {
        val bytes = ByteArrayOutputStream()
        if (inImage != null) {
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        }
        val path = MediaStore.Images.Media.insertImage(inContext?.getContentResolver(), inImage, "Title" + " - " + Calendar.getInstance().getTime(), null)
        return Uri.parse(path)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api= RemoteDataSource().buildApi(MainApi::class.java,
            runBlocking { userPreferences.authToken.first() })
        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
            vmChat= ViewModelProvider(this).get(ChatViewModel::class.java)
        }

        val adapterDataObserver = object:RecyclerView.AdapterDataObserver(){

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if(shouldScroll)
                {
                    shouldScroll=false
                    if(shouldScrollBottom)
                    {
                        activity?.runOnUiThread {
                            binding.rvChat.scrollToPosition(chatAdapter.currentList.size-1)
                            shouldScrollBottom=false
                        }
                    }
                    else
                    {
                        binding.rvChat.smoothScrollToPosition(unreadIndex)

                        var templst=chatAdapter.currentList.toList()
                        for(i in templst.indices)
                        {
                            templst[i].isread=1
                        }
                        chatAdapter.submitList(templst)
                    }
                }
            }
        }
        chatAdapter=ChatAdapter(requireActivity().cacheDir)
        chatAdapter.registerAdapterDataObserver(adapterDataObserver)
        chatAdapter.setMyId(vmAuth.userid.value!!)
        chatAdapter.setOnImageClickListener {
            val bundle=Bundle()
            bundle.putString("image",it.content)
            (activity as MainActivity).replaceFragment("imageFragment",ImageFragment(),bundle)
        }
        chatAdapter.setOnProfileImageClickListener {
            if(it.profileimage!="none")
            {
                it.profileimage?.let{
                    val bundle=Bundle()
                    bundle.putString("image",it)
                    (activity as MainActivity).replaceFragment("imageFragment",ImageFragment(),bundle)
                }
            }
        }
        chatAdapter.setOnLocationClickListener {
            val token=it.content.split('&')
            val lat=token[0].toFloat()
            val lon=token[1].toFloat()
            val location=LocationLatLngEntity(lat,lon)
            val bundle=Bundle()
            bundle.putParcelable("location",location)
            (activity as MainActivity).replaceFragment("mapFragment",MapFragment(),bundle)
        }
        try{
            (activity as MainActivity).mSocket.emit(
                "enter",
                gson.toJson(read(roomid,vmAuth.userid.value!!))
            )
        }catch(e: Exception){
            e.printStackTrace();
            Toast.makeText(requireContext(),"연결 오류가 발생했습니다",Toast.LENGTH_SHORT).show()
        }
    }
    private fun previewMessage(data:MessageData)
    {
        activity?.runOnUiThread {
            var message=""
            when(data.type)
            {
                "IMAGE"->message="${data.nickname}:사진을 보냈습니다"
                "EXIT"->message="상대방이 대화방을 나갔습니다"
                "LOCATION"->message="${data.nickname}:(위치정보)"
                "TEXT"->message="${data.nickname}:${data.content}"
            }
            binding.tvmsg.apply {
                visibility=View.VISIBLE
                text=message
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(mRootView==null) {
            (activity as MainActivity).setToolBarVisible("chatFragment")
            rootView = activity?.window!!.decorView

            rootView.viewTreeObserver.addOnGlobalLayoutListener { // 뷰에 변화가 있을 때마다 실행되는 리스너
                var resourceId =
                    resources.getIdentifier("navigation_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    navigationBarHeight = resources.getDimensionPixelSize(resourceId)
                }
                resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    statusBarHeight = resources.getDimensionPixelSize(resourceId)
                }
                isKeyboardShowing = keyboard.isShowing(rootView)
            }

            binding = DataBindingUtil.inflate<FragmentChatBinding>(
                inflater,
                R.layout.fragment_chat, container, false
            )
            opponentid = arguments?.getInt("userid", 0)!!

            val firstChatLoad = vmChat.loadchatContents(roomid)
            firstChatLoad.observe(viewLifecycleOwner) {
                curChatContents = it
                var oppoid = opponentid
                if (opponentid == 0) {
                    (activity as MainActivity).binding.title.text = "대화상대없음"
                    val opponentcontent =
                        it.find { content -> content.senderid != vmAuth.userid.value!! }
                    opponentcontent?.let {
                        oppoid = it.senderid!!
                    }
                }
                vmChat.readChats(roomid)//읽음
                vmChat.getuserprofile(oppoid, api)
                firstChatLoad.removeObservers(viewLifecycleOwner)
            }
            setupRecyclerView()
            subsribeToObserver()
            mRootView = binding.root
        }
       else{
            if(opponentid==0)
                (activity as MainActivity).binding.title.text="대화상대없음"
            else
                (activity as MainActivity).binding.title.text=usernickname
        }
        var job: Job? = null
        binding.edtText.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(view: View, hasFocus: Boolean) {
                if (hasFocus) {
                    prescrollbottom=scrollbottom
                    lastFirstVisiblePosition=(binding.rvChat.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    binding.toolBtn.setImageResource(R.drawable.tooladd)
                    showcontainer = false
                    job?.cancel()
                    job = lifecycleScope.launch {
                        activity?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                        delay(100)
                        binding.toolbox.visibility = View.GONE
                        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    }
                }
            }
        })
        binding.edtText.setOnClickListener {
            prescrollbottom=scrollbottom

        }
        binding.toolBtn.setOnClickListener {
            job?.cancel()
            job = lifecycleScope.launch {
                if (!showcontainer && keyboard.keyboardHeight < 200) {
                    prescrollbottom=scrollbottom
                    //최초 키보드띄우지않고 컨테이너 띄울경우
                    binding.toolbox.setHeight(600)
                    binding.toolbox.visibility=View.VISIBLE
                    showcontainer=true
                    binding.toolBtn.setImageResource(R.drawable.toolcancel)
                    if(chatAdapter.currentList.size>0)
                        binding.rvChat.smoothScrollToPosition(chatAdapter.currentList.size - 1)
                }
                else{
                    if(showcontainer)
                    {
                        binding.edtText.requestFocus()
                        showcontainer=false
                        binding.toolBtn.setImageResource(R.drawable.tooladd)
                        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                        showKeyboard()
                        delay(100)
                        binding.toolbox.visibility = View.GONE
                        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    }
                    else{
                        binding.edtText.clearFocus()
                        showcontainer=true
                        binding.toolBtn.setImageResource(R.drawable.toolcancel)
                        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                        binding.toolbox.setHeight(keyboard.keyboardHeight-(navigationBarHeight+statusBarHeight))
                        binding.toolbox.visibility = View.VISIBLE
                        hideKeyboard()
                        delay(100)
                        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    }
                }
            }
        }
        binding.edtText.addTextChangedListener {editable->

            editable?.let{
                if(binding.edtText.text.toString().trim().isEmpty())
                {
                    binding.sendcomment.visibility=View.GONE
                }
                else
                {
                    binding.sendcomment.visibility=View.VISIBLE
                }
            }
        }
        cropContent=registerForActivityResult(cropActivityResultContract){uri->
            uri?.let{
                showcontainer=false
                isKeyboardShowing=false
                binding.toolbox.visibility=View.GONE
                binding.toolBtn.setImageResource(R.drawable.tooladd)
                uploadImage(uri,requireContext())
            }
        }
        binding.album.setOnClickListener {
            cropContent.launch(null)
        }
        binding.camera.setOnClickListener {
            if (checkPermission()) {
                dispatchTakePictureIntent()
            } else {
                requestPermissionLauncher.launch(permissions)
            }
        }
        binding.sendcomment.setOnClickListener {
            sendText()
        }
        binding.location.setOnClickListener {
            val intent = Intent(requireContext(), LocationActivity::class.java)
            resultLauncher.launch(intent)
        }
        return mRootView
    }
    private fun updatechat(chat:MessageData)
    {
        if(chat.type.equals("EXIT"))
        {
            activity?.runOnUiThread {
                (activity as MainActivity).binding.title.text="대화상대없음"
            }
            opponentid=0
        }
        var dateChanged=false
        var templst=chatAdapter.currentList.toList()
        var maxchatid=0
        if(chatAdapter.currentList.isNotEmpty())
        {
            dateChanged=isDateChanged(chatAdapter.currentList.last().date)
            maxchatid=chatAdapter.currentList.last().num!!
        }
        if (dateChanged) {
            templst+=
                listOf(
                    MessageData(
                        maxchatid+1,
                        null,
                        "",
                        "DATE",
                        getTodayString(SimpleDateFormat("yyyy년 M월 d일 E요일")),
                        1,
                        null,
                        null,
                        null
                    ), MessageData(
                        maxchatid+2,
                        chat.senderid,
                        chat.date,
                        chat.type,
                        chat.content,
                        chat.isread,
                        chat.nickname,
                        chat.gender,
                        chat.profileimage
                    )
                )
        } else {
            templst+=MessageData(
                maxchatid+1,
                chat.senderid,
                chat.date,
                chat.type,
                chat.content,
                chat.isread,
                chat.nickname,
                chat.gender,
                chat.profileimage
            )
        }
        if(scrollbottom)
            setchatcontents(templst,true)
        else
            setchatcontents(templst,false)

    }
    private fun sendText()
    {
        sendContent(binding.edtText.text.toString(),"TEXT")
        binding.edtText.setText("")
    }
    private fun exitroom()
    {
        sendContent("EXIT","EXIT")
        (activity as MainActivity).deleteroom(roomid)
    }
    private fun sendContent(content:String,Type:String){
        var dateChanged:Boolean=false
        var templst=chatAdapter.currentList.toList()
        var maxchatid=0
        if(chatAdapter.currentList.isNotEmpty())
        {
            maxchatid=chatAdapter.currentList.last().num!!
            dateChanged=isDateChanged(chatAdapter.currentList.last().date)
        }
        val sendData= SendData(
            roomid,
            vmAuth.userid.value!!,
            opponentid,
            content,
            Type,
            getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
            dateChanged,
            getTodayString(SimpleDateFormat("yyyy년 M월 d일 E요일"))
        )
        try{
            (activity as MainActivity).mSocket.emit("newMessage",gson.toJson(
                sendData
            ))
        }catch (e:Exception){
            Toast.makeText(requireContext(),"연결 오류가 발생했습니다",Toast.LENGTH_SHORT).show()
            return
        }

        if(Type.equals("EXIT"))
        {
            vmChat.deleteroom(roomid)
            parentFragmentManager.popBackStack()
        }
        else
        {
            vmChat.insertChat(ChatData(null,vmAuth.userid.value!!,roomid,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),Type,content,1),if(dateChanged)1 else 0)
            (activity as MainActivity).updatechatroom(ChatData(null,vmAuth.userid.value!!,roomid,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),Type,content,1),
                userprofileimage!!,usergender,usernickname,1,opponentid)
            if (dateChanged) {
                templst+=
                    listOf(
                        MessageData(
                            maxchatid+1,
                            null,
                            "",
                            "DATE",
                            getTodayString(SimpleDateFormat("yyyy년 M월 d일 E요일")),
                            1,
                            null,
                            null,
                            null
                        ), MessageData(
                            maxchatid+2,
                            vmAuth.userid.value!!,
                            getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
                            Type,
                            content,
                            1,
                            null,
                            null,
                            null
                        )
                    )
            } else {
                templst+=MessageData(
                    maxchatid+1,
                    vmAuth.userid.value!!,
                    getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
                    Type,
                    content,
                    1,
                    null,
                    null,
                    null
                )
            }
            scrollbottom=true
            setchatcontents(templst,true)
        }
    }
    fun isDateChanged(dateString:String):Boolean{
        val date=datetostr(
            strtodate(dateString,SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
            SimpleDateFormat("yyyy-MM-dd")
        )
        return !date.equals(getTodayString(SimpleDateFormat("yyyy-MM-dd")))
    }
    private fun uploadImage(imageUri: Uri, context: Context)
    {
        var requestImage:MultipartBody.Part
        val file= File(getRealPathFromURI(imageUri,context))
        val requestBody=file.asRequestBody("image/*".toMediaTypeOrNull())
        var body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestBody)//이거
        requestImage=body

        vmChat.uploadimg(requestImage,api)
    }
    private fun getRealPathFromURI(contentUri: Uri, context: Context):String?
    {
        val contentResolver = context.contentResolver ?: return null
        // 파일생성
        val filePath = (context.applicationInfo.dataDir + File.separator
                + System.currentTimeMillis())
        val file = File(filePath)
        try {
            val inputStream = contentResolver.openInputStream(contentUri) ?: return null
            val outputStream: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return filePath
    }
    private fun setupRecyclerView()=binding.rvChat.apply{
        adapter=chatAdapter
        layoutManager= LinearLayoutManager(requireContext())
        itemAnimator=null
        addOnScrollListener(this@ChatFragment.scrollListener)
        addOnLayoutChangeListener(this@ChatFragment.layoutChangeListener)
    }
    fun View.setHeight(value: Int) {
        val lp = layoutParams
        lp?.let {
            lp.height = value
            layoutParams = lp
        }
    }
    val layoutChangeListener=
        View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if(bottom<oldBottom&&prescrollbottom&&chatAdapter.currentList.size>0) {
                binding.rvChat.smoothScrollToPosition(chatAdapter.currentList.size - 1)
            }
        }
    val scrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(!recyclerView.canScrollVertically(-1)&&(beforechatsSize!=chatAdapter.currentList.size)){
                beforechatsSize=chatAdapter.currentList.size
                val num=chatAdapter.currentList[0].num
                val loadbefore=vmChat.loadbeforechatContents(roomid,num!!)
                loadbefore.observe(viewLifecycleOwner){
                    var chatlist:List<MessageData> = listOf()
                    for(i in it)
                    {
                        chatlist+=MessageData(i.id,i.senderid,i.date,i.type,i.content,i.isread!!,usernickname,usergender,userprofileimage)

                    }
                    activity?.runOnUiThread{
                        setchatcontents(chatlist.reversed()+chatAdapter.currentList.toList(),false)
                    }
                    loadbefore.removeObservers(viewLifecycleOwner)
                }

            }
            scrollbottom = !recyclerView.canScrollVertically(1)
            if(scrollbottom)
                binding.tvmsg.visibility=View.GONE
        }
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        super.onResume()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chat_tools, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
            }
            R.id.images->{
                navigateToImages()
            }
            R.id.exit->{
                showexit()
            }
            R.id.block->{
                showblock()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun navigateToImages()
    {
        val loadimgs=vmChat.loadimages(roomid)
        loadimgs.observe(viewLifecycleOwner){
            val images=it.map { ChatImage(it.date,it.content,false)  }
            val bundle=Bundle()
            if(it.isEmpty())
                bundle.putParcelable("chatimages",ChatImages(null))
            else
                bundle.putParcelable("chatimages",ChatImages(images))
            //(activity as MainActivity).replaceFragment("chatContentsFragment",ChatContentsFragment(),bundle)
            loadimgs.removeObservers(viewLifecycleOwner)
        }
    }
    private fun showblock()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="상대방을 차단하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            vmChat.blockchatuser(false,opponentid,getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),api)

            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun showexit()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="채팅방을 나가겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {

            exitroom()
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun setchatcontents(contents:List<MessageData>,scrolltoposition:Boolean)
    {
        loading=false
        if(scrolltoposition){
            shouldScroll=true
            var unreadindex:Int=0
            var unreadexist=false
            for((index,content) in contents.withIndex())
            {
                if(content.isread==0&&content.senderid!=vmAuth.userid.value!!)
                {
                    unreadexist=true
                    unreadindex=index
                    break
                }
            }
            if(unreadexist)
            {
                unreadIndex=unreadindex
            }
            else {
                if(contents.size!=0&&scrollbottom)
                    shouldScrollBottom=true
            }
        }
        chatAdapter.submitList(contents)
    }
    private fun subsribeToObserver()
    {
        vmChat.blockuserResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onError={
                Toast.makeText(requireContext(),it,Toast.LENGTH_SHORT).show()
            }
        ){
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==300)
                {
                    Toast.makeText(requireContext(),"차단이 완료되었습니다",Toast.LENGTH_SHORT).show()
                    exitroom()
                }
            }
        })
        vmChat.uploadimgResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
              loadingDialog.show()
            },
            onError = {
                loadingDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200)
                {
                    sendContent(it.imageUri,"IMAGE")
                }
            }

        })
        vmChat.getprofileResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
                loadingDialog.show()
            },
            onError = {
                loadingDialog.dismiss()
                var error:String
                if(!(activity as MainActivity).isConnected!!)
                    error= requireContext().getString(R.string.networkdisdconnected)
                else
                    error=it
                snackbar=snackbar(error+"\n잠시후에 다시 시도해주세요",true,"다시시도"){
                    vmChat.getuserprofile(opponentid,api)
                }
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    if (it.account == null) { //상대방이 탈퇴한상황
                        binding.edtText.hint = "대화가 불가능한 사용자입니다"
                        binding.edtText.isClickable = false
                        binding.edtText.isEnabled = false
                        binding.toolBtn.isClickable = false
                        binding.toolBtn.setColorFilter(getColor(requireContext(), R.color.gray))
                    }
                    usernickname = it.nickname
                    usergender = it.gender
                    userprofileimage = it.profileimage
                    if (userprofileimage == null)
                        userprofileimage = "none"
                    if (opponentid != 0)
                        (activity as MainActivity).binding.title.text = usernickname
                    var chatlist: List<MessageData> = listOf()
                    for (i in curChatContents) {
                        chatlist += MessageData(
                            i.id,
                            i.senderid,
                            i.date,
                            i.type,
                            i.content,
                            i.isread!!,
                            usernickname,
                            usergender,
                            userprofileimage
                        )
                    }
                    val oldchats = chatAdapter.currentList

                    activity?.runOnUiThread {
                        if (oldchats.isEmpty())
                            setchatcontents(chatlist.reversed(), true)
                        else
                            setchatcontents(chatlist.reversed() + oldchats, false)
                    }
                    vmChat.getAddedChats(roomid).observe(viewLifecycleOwner){
                        if(it.isread==0&&it.senderid!=vmAuth.userid.value!!&&it.type!="DATE")
                        {
                            val data=MessageData(it.id,it.senderid,it.date,it.type,it.content,1,usernickname,usergender,userprofileimage)
                            if(!scrollbottom)
                                previewMessage(data)
                            updatechat(data)
                            vmChat.readChats(roomid)
                        }
                    }
                }
            }
        })
    }
    fun showKeyboard() {
        imm.showSoftInput(binding.edtText, 0)
    }
    fun hideKeyboard() {
        val webToken: IBinder? = activity?.currentFocus?.windowToken // 현재 포커스를 가진 뷰의 웹 토큰
        if (binding.edtText.windowToken == null) { // 웹 토큰이 null인 경우 toggleSoftInput 메소드 사용
            if (isKeyboardShowing) { // 현재 키보드 보일 경우 키보드 토글
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        } else { // 키보드 내리기
            imm.hideSoftInputFromWindow(binding.edtText.windowToken, 0)
        }
    }
    private fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                createImageFile().let{
                    val photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.example.appportfolio.fileprovider", it)
                    m_imageFile=it
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    getContent.launch(takePictureIntent)
                }
            }
        }
    }
    fun exifOrientationToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }
    fun rotate(bitmap: Bitmap?, degrees: Int): Bitmap? { // 이미지 회전 및 이미지 사이즈 압축
        var bitmap = bitmap
        if (degrees != 0 && bitmap != null) {
            val m = Matrix()
            m.setRotate(degrees.toFloat(), bitmap.width.toFloat() / 2,
                bitmap.height.toFloat() / 2)
            try {
                val converted = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.width, bitmap.height, m, true)
                if (bitmap != converted) {
                    bitmap.recycle()
                    bitmap = converted
                    val options = BitmapFactory.Options()
                    options.inSampleSize = 4
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1280, 1280, true) // 이미지 사이즈 줄이기
                }
            } catch (ex: OutOfMemoryError) {
            }
        }
        return bitmap
    }
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PHOTO_${timeStamp}.jpg"
        val storageDir=activity?.cacheDir
        return File(storageDir, imageFileName)
    }
    override fun onStop() {
        super.onStop()
        if(isKeyboardShowing)
            hideKeyboard()
    }
    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).mSocket.emit(
            "left",
            gson.toJson(Roomconnect(roomid,null))
        )
        (activity as MainActivity).mSocket.off("update")
        (activity as MainActivity).mSocket.off("getuploadedimg")
        (activity as MainActivity).setupTopBottom()
        snackbar?.dismiss()
    }
}
