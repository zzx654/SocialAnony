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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.ui.auth.viewmodel.AuthViewModel
import com.example.appportfolio.R
import com.example.appportfolio.SocialApplication
import com.example.appportfolio.SocialApplication.Companion.handleResponse
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment: Fragment(R.layout.fragment_chat),MenuProvider {
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
    private var userprofileimage:String?=null
    private var usernickname:String=""
    private var usergender:String=""
    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    lateinit var api: MainApi
    private lateinit var inputMethodManager: InputMethodManager
    private var beforechatsSize=0
    private var blockingid:Int?=null
    private var lastFirstVisiblePosition:Int=0
    private var prescrollbottom=false
    private  var opponentid:Int=0
    private var opponentsender:Int=0
    private var snackbar: Snackbar?=null
    private var initChatContents:List<ChatData> = listOf()
    private var getRealtimeChat:LiveData<ChatData>?=null
    @Inject
    lateinit var userPreferences: UserPreferences
    @Inject
    lateinit var loadingDialog:LoadingDialog
    private val roomid:String
        get(){
            return arguments?.getString("roomid","")!!
        }
    lateinit var binding: FragmentChatBinding
    private val keyboard = Keyboard() // ????????? ???????????? ??????
    private lateinit var rootView: View // ?????? ???
    private var showcontainer=false
    private var navigationBarHeight = 0
    private var statusBarHeight = 0
    private var isKeyboardShowing: Boolean = false // ?????? ????????? ???????????? ??????
    private val imm: InputMethodManager by lazy { // ????????? ?????????
        activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        showcontainer=false
        isKeyboardShowing=false
        binding.toolbox.visibility=View.GONE
        binding.toolBtn.setImageResource(R.drawable.tooladd)
        scrollbottom=true
        if (result.resultCode == Activity.RESULT_OK) { //?????? ?????? ????????????api ??? ???????????? ?????? ?????? ???????????? ?????????
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
    private var m_imageFile: File? = null //???????????? ?????? ?????? ??????
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
                }//???????????? ???????????? ?????? ????????? uri??? ???????????? ?????? ?????????
            }
        }
    private fun getImageUri(context:Context, image:Bitmap?):Uri?{
        val imagesFolder=File(requireContext().cacheDir,"images")
        var uri:Uri?=null
        try{
            imagesFolder.mkdirs()
            val file=File(imagesFolder,"Title" + " - " + Calendar.getInstance().time)
            val stream=FileOutputStream(file)
            image?.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
            uri=FileProvider.getUriForFile(context.applicationContext,"com.example.appportfolio.fileprovider",file)

        } catch (e:FileNotFoundException){
            e.printStackTrace()
        } catch (e:IOException){
            e.printStackTrace()
        }
        return uri
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api= RemoteDataSource().buildApi(MainApi::class.java,
            runBlocking { userPreferences.authToken.first() })
        activity?.run{
            vmAuth= ViewModelProvider(this)[AuthViewModel::class.java]
            vmChat= ViewModelProvider(this)[ChatViewModel::class.java]
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

                        val templst=chatAdapter.currentList.toList()
                        for(i in templst.indices)
                        {
                            templst[i].isread=1
                        }
                        chatAdapter.submitList(templst)
                    }
                }
            }
        }
        chatAdapter=ChatAdapter()
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
            e.printStackTrace()
            Toast.makeText(requireContext(),"?????? ????????? ??????????????????",Toast.LENGTH_SHORT).show()
        }
    }
    private fun previewMessage(data:MessageData)
    {
        activity?.runOnUiThread {
            var message=""
            when(data.type)
            {
                "IMAGE"->message="${data.nickname}:????????? ???????????????"
                "EXIT"->message="???????????? ???????????? ???????????????"
                "LOCATION"->message="${data.nickname}:(????????????)"
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

            rootView.viewTreeObserver.addOnGlobalLayoutListener { // ?????? ????????? ?????? ????????? ???????????? ?????????
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

            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_chat, container, false
            )
            opponentid = arguments?.getInt("userid", 0)!!
            opponentsender=opponentid
            setupRecyclerView()

            (activity as MainActivity).mSocket.on("sendResponse"){   args: Array<Any>->
                val sentdata=gson.fromJson(
                    args[0].toString(),
                    SentData::class.java
                )
                vmChat.insertChat(ChatData(null,vmAuth.userid.value!!,roomid,sentdata.date,sentdata.type,sentdata.content,1))
            }
            mRootView = binding.root
        }
       else{
            if(opponentid==0)
                (activity as MainActivity).binding.title.text="??????????????????"
            else
                (activity as MainActivity).binding.title.text=usernickname
        }
        var job: Job? = null
        binding.edtText.onFocusChangeListener =
            View.OnFocusChangeListener { view, hasFocus ->
                //edittext??? ???????????? ???????????? ???????????? ?????? ui??? ?????????????????? show(toolbox??? ????????????????????? ?????????) ?????? toolbox??? ??????????????????
                //?????? ???????????? ui??? ????????? ??????????????????
                if (hasFocus) {
                    prescrollbottom=scrollbottom
                    lastFirstVisiblePosition=(binding.rvChat.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    binding.toolBtn.setImageResource(R.drawable.tooladd)
                    showcontainer = false
                    job?.cancel()
                    job = lifecycleScope.launch {
                        activity?.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)//???????????? ui??? ????????? ??????
                        delay(100)
                        binding.toolbox.visibility = View.GONE
                        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)//???????????? ?????? ui??? ?????????
                    }
                }
            }
        binding.edtText.setOnClickListener {
            prescrollbottom=scrollbottom

        }
        binding.toolBtn.setOnClickListener {
            job?.cancel()
            job = lifecycleScope.launch {
                if (!showcontainer && keyboard.keyboardHeight < 200) {
                    prescrollbottom=scrollbottom
                    //?????? ???????????????????????? ???????????? ????????????
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
                        //edittext??? ?????? ?????????,???????????? ???????????????????????? ???????????? ?????? ui??? ???????????? ????????? ?????? ??? ????????? show?????? toolbox??? ???????????????
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
                        //edittext ???????????? ?????? ?????????
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
        subsribeToObserver()
        return mRootView
    }

    private fun updatechat(chat:MessageData)
    {
        if(chat.type == "EXIT")
        {
            activity?.runOnUiThread {
                (activity as MainActivity).binding.title.text="??????????????????"
            }
            opponentid=0
        }
        var templst=chatAdapter.currentList.toList()
        templst+=chat
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
        val sendData= SendData(
            roomid,
            vmAuth.userid.value!!,
            opponentid,
            content,
            Type
        )
        try{
            (activity as MainActivity).mSocket.emit("newMessage",gson.toJson(
                sendData
            ))
        }catch (e:Exception){
            Toast.makeText(requireContext(),"?????? ????????? ??????????????????",Toast.LENGTH_SHORT).show()
            return
        }
        if(Type == "EXIT")
        {
            vmChat.deleteroom(roomid)
            parentFragmentManager.popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        snackbar?.dismiss()
        getRealtimeChat?.removeObservers(viewLifecycleOwner)
    }
    private fun uploadImage(imageUri: Uri, context: Context)
    {
        val requestImage:MultipartBody.Part
        val file= File(getRealPathFromURI(imageUri,context))
        val requestBody=file.asRequestBody("image/*".toMediaTypeOrNull())
        val body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestBody)//??????
        requestImage=body

        vmChat.uploadimg(requestImage,api)
    }
    private fun getRealPathFromURI(contentUri: Uri, context: Context):String?
    {
        val contentResolver = context.contentResolver ?: return null
        // ????????????
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
    private fun View.setHeight(value: Int) {
        val lp = layoutParams
        lp?.let {
            lp.height = value
            layoutParams = lp
        }
    }
    private val layoutChangeListener=
        View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if(bottom<oldBottom&&prescrollbottom&&chatAdapter.currentList.size>0) {
                binding.rvChat.smoothScrollToPosition(chatAdapter.currentList.size - 1)
            }
        }
    private val scrollListener= object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if(!recyclerView.canScrollVertically(-1)&&(beforechatsSize!=chatAdapter.currentList.size)){
                beforechatsSize=chatAdapter.currentList.size
                val num=chatAdapter.currentList[0].num
                val loadbefore=vmChat.loadbeforechatContents(roomid,num!!)
                loadbefore.observe(viewLifecycleOwner){
                    if(it.isNotEmpty()){
                        val prevchatlist=createMessageData(it.reversed())
                        val curchatlist=chatAdapter.currentList.toMutableList()
                        prevchatlist[0].dateChanged=isDateChanged(prevchatlist.last().date,curchatlist[0].date)
                        activity?.runOnUiThread{
                            setchatcontents(prevchatlist+curchatlist,false)
                        }
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
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.goback)
            setDisplayShowTitleEnabled(false)
        }
        if(chatAdapter.currentList.isEmpty())
        { //?????? ?????? ???????????? ???????????? 20?????? ?????? ?????? load
            val firstChatLoad = vmChat.loadchatContents(roomid)
            firstChatLoad.observe(viewLifecycleOwner) {
                initChatContents = it.reversed()
                if (opponentid == 0) //??????????????? ????????? ????????????
                    (activity as MainActivity).binding.title.text = "??????????????????"
                vmChat.readChats(roomid)//??????
                if(userprofileimage==null)
                {
                    val getoppochat=vmChat.getOpponentChat(roomid,vmAuth.userid.value!!)
                    getoppochat.observe(viewLifecycleOwner){ oppochat->
                        //???????????? userid??? 0????????????????????? ????????????db ?????? ???????????? ???????????? senderid??? ??????
                        if(oppochat.isNotEmpty())
                            opponentsender=oppochat[0].senderid!!
                        vmChat.getuserprofile(opponentsender, api)
                        getoppochat.removeObservers(viewLifecycleOwner)
                    }
                }
                else
                    initContents(initChatContents,true, subscribeNewChat = true)
                firstChatLoad.removeObservers(viewLifecycleOwner)
            }
        }
        else{//??????????????? ?????? ?????? load????????? ??????????????? ?????? ???????????????????????? ?????? ????????? ?????? ?????? load
            val lastChatLoad = vmChat.getLastChats(roomid,chatAdapter.currentList.last().num!!)
            lastChatLoad.observe(viewLifecycleOwner){
                initContents(it.reversed(), addtoLast = true, subscribeNewChat = true)
                vmChat.readChats(roomid)
                lastChatLoad.removeObservers(viewLifecycleOwner)
            }
        }
        super.onResume()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.chat_tools, menu)
    }
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return  when(menuItem!!.itemId){
            android.R.id.home->{
                parentFragmentManager.popBackStack()
                true
            }
            R.id.images->{
                navigateToImages()
                true
            }
            R.id.exit->{
                showexit()
                true
            }
            R.id.block->{
                showblock()
                true
            }
            else->false
        }
    }
    private fun navigateToImages()
    {
        val loadimgs=vmChat.loadimages(roomid)
        loadimgs.observe(viewLifecycleOwner){ it ->
            val images=it.map { ChatImage(it.date,it.content,false)  }
            val bundle=Bundle()
            if(it.isEmpty())
                bundle.putParcelable("chatimages",ChatImages(null))
            else
                bundle.putParcelable("chatimages",ChatImages(images))
            (activity as MainActivity).replaceFragment("chatContentsFragment",ChatContentsFragment(),bundle)
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
        alertText.text="???????????? ?????????????????????????"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            blockingid=opponentid
            vmChat.blockchatuser(false,opponentid,api)

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
        alertText.text="???????????? ???????????????????"
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
            var unreadindex =0
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
                if(contents.isNotEmpty() &&scrollbottom)
                    shouldScrollBottom=true
            }
        }
        chatAdapter.submitList(contents)
    }
    private fun subsribeToObserver()
    {
        vmChat.blockuserResponse.observe(viewLifecycleOwner,Event.EventObserver(
            onLoading={
              loadingDialog.show()
            },
            onError={
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
            handleResponse(requireContext(),it.resultCode){
                if(it.resultCode==300)
                {
                    blockingid?.let{
                        (activity as MainActivity).deleteBlockedcChatUser(it)
                    }
                    Toast.makeText(requireContext(),"????????? ?????????????????????",Toast.LENGTH_SHORT).show()
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
                val error:String = if ((activity as MainActivity).isConnected!!)
                    it
                else
                    requireContext().getString(R.string.networkdisdconnected)
                initContents(initChatContents, addtoLast = false, subscribeNewChat = true)//??????????????? ???????????????????????? observe??? ?????????????????? ?????? observer ??????
                snackbar=snackbar("$error\n???????????? ?????? ??????????????????",true,"????????????"){
                    vmChat.getuserprofile(opponentsender,api)
                }
            }
        ){
            loadingDialog.dismiss()
            handleResponse(requireContext(),it.resultCode) {
                if (it.resultCode == 200) {
                    if (it.account == null) { //???????????? ???????????????
                        binding.edtText.hint = "????????? ???????????? ??????????????????"
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
                    if(getRealtimeChat!=null)
                        initContents(initChatContents, addtoLast = false, subscribeNewChat = false)
                    else
                        initContents(initChatContents,false, subscribeNewChat = true)
                }
            }
        })
    }
    private fun createMessageData(chatcontents: List<ChatData>):List<MessageData>{
        var chatlist: List<MessageData> = listOf()
        chatcontents.forEachIndexed { index, chatData ->
            val dateChanged=if(index>0)isDateChanged(chatcontents[index-1].date,chatData.date)else false
            chatlist += MessageData(
                chatData.id,
                chatData.senderid,
                chatData.date,
                chatData.type,
                chatData.content,
                chatData.isread!!,
                usernickname,
                usergender,
                userprofileimage,
                dateChanged
            )
        }
        return chatlist
    }
    private fun initContents(chatcontents:List<ChatData>,addtoLast:Boolean,subscribeNewChat:Boolean)
    {
        val chatlist=createMessageData(chatcontents)
        if(userprofileimage!=null)
        {
            val oldchats = chatAdapter.currentList

            activity?.runOnUiThread {
                if (oldchats.isEmpty())
                    setchatcontents(chatlist, true)
                else if(addtoLast)
                    setchatcontents(oldchats + chatlist, false)
                else
                    setchatcontents(chatlist + oldchats, false)
            }
        }

        if(subscribeNewChat){
            getRealtimeChat?.removeObservers(viewLifecycleOwner)
            getRealtimeChat=vmChat.getAddedChats(roomid)
            getRealtimeChat?.observe(viewLifecycleOwner){

                if(it.type!="start"&&chatAdapter.currentList.find { chat-> chat.num==it.id }==null
                    &&chatlist.find {chat-> chat.num==it.id  }==null&&initChatContents.find{chat-> chat.id==it.id}==null)
                { //?????? ???????????? ???????????? ??????????????????????????? ?????????????????? ????????? ?????????????????? ???????????????
                    if(userprofileimage==null)
                        initChatContents+=it//?????? ???????????? ???????????? ??????????????? ???????????? ??????????????? ????????????
                    else
                    {
                        var dateChanged=false
                        if(chatAdapter.currentList.isNotEmpty()){
                            dateChanged=isDateChanged(chatAdapter.currentList.last().date,it.date)
                        }
                        val data=MessageData(it.id,it.senderid,it.date,it.type,it.content,1,usernickname,usergender,userprofileimage,dateChanged)
                        if(!scrollbottom&&it.senderid!=vmAuth.userid.value!!)
                            previewMessage(data)
                        updatechat(data)
                    }
                    vmChat.readChats(roomid)
                }
            }
        }
    }
    private fun isDateChanged(prevdate:String,curdate:String):Boolean{
        val previousdate= SocialApplication.datetostr(
            SocialApplication.strtodate(prevdate, SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))!!,
            SimpleDateFormat("yyyy-MM-dd")
        )
        val currentdate= SocialApplication.datetostr(
            SocialApplication.strtodate(curdate, SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))!!,
            SimpleDateFormat("yyyy-MM-dd"))
        return previousdate != currentdate
    }
    private fun showKeyboard() {
        imm.showSoftInput(binding.edtText, 0)
    }
    private fun hideKeyboard() {
        if (::inputMethodManager.isInitialized.not()) {
            inputMethodManager=activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        }

        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
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
    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                90
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                180
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                270
            }
            else -> 0
        }
    }
    private fun rotate(bitmap: Bitmap?, degrees: Int): Bitmap? { // ????????? ?????? ??? ????????? ????????? ??????
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
                    bitmap = Bitmap.createScaledBitmap(bitmap, 1280, 1280, true) // ????????? ????????? ?????????
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
        (activity as MainActivity).mSocket.off("sendResponse")
        (activity as MainActivity).mSocket.off("getuploadedimg")
        (activity as MainActivity).setupTopBottom()
        snackbar?.dismiss()
    }
}
