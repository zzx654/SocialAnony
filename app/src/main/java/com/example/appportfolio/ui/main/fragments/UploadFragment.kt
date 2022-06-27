package com.example.appportfolio.ui.main.fragments

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.example.appportfolio.SocialApplication.Companion.handleResponse
import com.example.appportfolio.adapters.PostImageAdapter
import com.example.appportfolio.api.build.MainApi
import com.example.appportfolio.api.build.RemoteDataSource
import com.example.appportfolio.data.entities.TagResult
import com.example.appportfolio.data.entities.Voteoption
import com.example.appportfolio.data.entities.Voteoptions
import com.example.appportfolio.databinding.FragmentUploadBinding
import com.example.appportfolio.other.Constants
import com.example.appportfolio.other.Event
import com.example.appportfolio.snackbar
import com.example.appportfolio.ui.main.GpsTracker
import com.example.appportfolio.ui.main.activity.MainActivity
import com.example.appportfolio.ui.main.dialog.RecordFragment
import com.example.appportfolio.ui.main.dialog.SetVoteoptionFragment
import com.example.appportfolio.ui.main.dialog.UploadProgressDialog
import com.example.appportfolio.ui.main.services.UploadService
import com.example.appportfolio.ui.main.viewmodel.UploadViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UploadFragment : Fragment(R.layout.fragment_upload){

    var postService: UploadService?=null
    lateinit var inputMethodManager: InputMethodManager
    var connection=object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as UploadService.mBinder
            postService=binder.getService()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("serviceDisabled","서비스 비정상종료")
        }
    }
    private var gpsAllowed:Boolean?=null
    private var recordedPath:String?=null
    private var anonymous:Boolean=false
    var curplatform:String?=null
    var curaccount:String?=null
    //@Inject
    lateinit var postimageAdapter: PostImageAdapter
    @Inject
    lateinit var progressDialog: UploadProgressDialog
    @Inject
    lateinit var gpsTracker: GpsTracker
    lateinit var binding: FragmentUploadBinding
    val PERMISSION_REQUEST_CODE=26
    lateinit var api: MainApi
    private lateinit var vmUpload: UploadViewModel
    private lateinit var vmAuth: AuthViewModel
    private var voteoptions:List<Voteoption>?=null
    private var mRootView:View?=null
    private var mIsFirstLoad=false
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            var selectedImages:MutableList<Uri> = mutableListOf()
            if(result.data?.clipData!=null)
            {//사진 여러개 선택한 경우
                val count=result.data?.clipData!!.itemCount
                if(count>9)
                {
                    snackbar(requireContext().getString(R.string.warn_image))
                }
                else{
                    selectedImages.clear()
                    for(i in 0 until count){
                        val imageUri=result.data?.clipData!!.getItemAt(i).uri
                        selectedImages.add(imageUri)
                    }
                }
            }
            else{//단일선택
                result.data?.data?.let{
                    selectedImages.clear()
                    selectedImages.add(it)
                }
            }
            var templist=postimageAdapter.currentList.toList()
            templist+=selectedImages.toList()
            postimageAdapter.submitList(templist)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postimageAdapter= PostImageAdapter()

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activity?.run{
            vmAuth= ViewModelProvider(this).get(AuthViewModel::class.java)
        }
        vmUpload=ViewModelProvider(requireActivity()).get(UploadViewModel::class.java)
        (activity as MainActivity).setToolBarVisible("uploadFragment")
        if(mRootView==null){
            binding= DataBindingUtil.inflate<FragmentUploadBinding>(inflater,
                R.layout.fragment_upload,container,false)
            mRootView=binding.root
            mIsFirstLoad=true
        }else{
            mIsFirstLoad=false
        }


        serviceBind()
        var job: Job?=null
        api= RemoteDataSource().buildApi(MainApi::class.java)
        binding.edtTag.addTextChangedListener { editable->
            job?.cancel()
            job=lifecycleScope.launch {
                delay(Constants.SEARCH_TIME_DELAY)
                editable?.let{
                    if(!binding.edtTag.text.toString().trim().isEmpty())
                    {
                        if(!it.toString().equals("#")) {
                            var tag=it.toString()
                            if(tag.contains("#"))
                            {
                                tag.replace("#","")
                            }
                            vmUpload.searchTag(tag, api)
                        }
                    }
                }
                if(editable!!.isEmpty())
                {
                    binding.cgSearched.removeAllViews()
                    binding.cgSearched.visibility= View.GONE
                    binding.tvTag.visibility= View.GONE
                }
            }
        }
        binding.btnSelectimg.setOnClickListener {
            getImages()
        }
        postimageAdapter.setOnDeleteClickListener {
            var templist=postimageAdapter.currentList.toList()
            templist-=it
            postimageAdapter.submitList(templist)
        }

        binding.fragment=this@UploadFragment

        binding.cbAn.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                showToast("익명이 활성화되었습니다")
            else
                showToast("익명이 비활성화되었습니다")
            vmUpload.setAnonymous(isChecked)
        }
        binding.btnLocation.setOnClickListener {
            if(SocialApplication.checkGeoPermission(requireContext()))
            {
                if(gpsAllowed!!)
                {
                    showToast("위치가 비활성화 되었습니다")
                    vmUpload.setGpsAllowed(false)
                }
                else
                {
                    showToast("위치가 함께 저장됩니다")
                    vmUpload.setGpsAllowed(true)
                }
            }
            else
            {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
                vmUpload.setGpsAllowed(false)
            }
        }


        binding.btnVote.setOnClickListener {
            var voptions:Voteoptions?=null
            voteoptions?.let{
                voptions=Voteoptions(voteoptions)
            }
            val bundle=Bundle()
            bundle.putParcelable("voteoptions",voptions)
            (activity as MainActivity).replaceFragment("setVoteOptionFragment",
                SetVoteoptionFragment(),bundle)
        }
        binding.deletevote.setOnClickListener {

            showDeleteVoteWarn()
        }
        setupRecyclerView()
        subsribeToObserver()
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(mIsFirstLoad)
            initGeo()
    }
    override fun onResume() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_baseline_close_24)
            setDisplayShowTitleEnabled(false)
        }
        (activity as MainActivity).binding.title.text="글 작성하기"
        super.onResume()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.upload_tools, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                showCancel()
            }
            R.id.complete -> {
                showComplete()

            }
        }
        return super.onOptionsItemSelected(item)
    }
    fun serviceBind()
    {
        var intent= Intent(requireContext(), UploadService::class.java)
        activity?.bindService(intent,connection, Context.BIND_AUTO_CREATE)
    }
    fun serviceUnbind()
    {
        activity?.unbindService(connection)
    }
    fun initGeo()
    {
        if(SocialApplication.checkGeoPermission(requireContext()))
        {
            vmUpload.setGpsAllowed(true)
            binding.btnLocation.setImageResource(R.drawable.ic_locationon)
        }
        else
        {
            vmUpload.setGpsAllowed(false)
            binding.btnLocation.setImageResource(R.drawable.ic_locationoff)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showToast("권한이 허용됨")
                    Toast.makeText(requireActivity(),"권한이 허용됨", Toast.LENGTH_SHORT).show()
                } else {
                    showToast("Permission denied, Set as the Default Location")
                }
            }
        }
    }
    fun showRecord()
    {
        if(recordedPath.isNullOrEmpty())
        {
            val recordDialog: RecordFragment = RecordFragment {
                binding.imgrecorded.visibility= View.VISIBLE
                vmUpload.setRecordedPath(it)
            }
            recordDialog.isCancelable=false
            recordDialog.show(parentFragmentManager,recordDialog.tag)
        }
        else
        {
            showDeleteRecordWarn()
        }
    }
    fun showDeleteRecordWarn()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text=this.getString(R.string.warn_deleterecord)
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            vmUpload.setRecordedPath("")
            binding.imgrecorded.visibility= View.INVISIBLE
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()

    }
    private fun showDeleteVoteWarn()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text="투표를 삭제하시겠습니까?"
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            vmUpload.setvoteoptions(null)
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()

    }
    private fun showComplete()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text=this.getString(R.string.warn_completepost)
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            if(binding.edtContent.text.isNullOrEmpty())
            {
                showToast("내용을 입력해주세요")
                dialog.dismiss()
                dialog.cancel()
            }
            else
            {
                dialog.dismiss()
                dialog.cancel()
                progressDialog.show()
                postService?.startPosting(
                    postimageAdapter.currentList,
                    recordedPath,
                    UUID.randomUUID().toString(),
                    genAnonymous(),
                    binding.edtContent.text.toString(),
                    getTagsstr(),
                    getLatitude(),
                    getLongitude(),
                    getTodayString(SimpleDateFormat("yyyy-MM-dd HH:mm:ss")),
                    getVoteoptionstr()
                )
            }
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun getLatitude():Double?
    {
        var latitude:Double?=null
        if(gpsAllowed!!)
        {
            latitude=gpsTracker.latitude!!
        }
        return latitude
    }
    private fun getLongitude():Double?
    {
        var longitude:Double?=null
        if(gpsAllowed!!)
        {
            longitude=gpsTracker.longitude!!
        }
        return longitude
    }
    fun getTodayString(format: SimpleDateFormat):String
    {
        var today= Calendar.getInstance()//오늘의 calendar객체
        var todaystr= datetostr(today.time,format)

        return todaystr
    }
    fun datetostr(date: Date, format: SimpleDateFormat):String
    {
        return format.format(date)
    }
    private fun getTagsstr():String?
    {
        var tags:String?=""
        if(binding.cgselected.childCount==0)
        {
            tags=null
        }
        else
        {
            for(i in 0 until binding.cgselected.childCount)
            {
                if(i!=0)
                {
                    tags+="#"
                }
                tags+=(binding.cgselected.getChildAt(i) as Chip).text.toString().replace("#","")
            }
        }
        return tags
    }
    fun showCancel()
    {
        val dialog= AlertDialog.Builder(requireContext()).create()
        val edialog: LayoutInflater = LayoutInflater.from(requireContext())
        val mView: View =edialog.inflate(R.layout.dialog_alert,null)
        val cancel: Button =mView.findViewById(R.id.cancel)
        val positive: Button =mView.findViewById(R.id.positive)
        val alertText: TextView =mView.findViewById(R.id.tvWarn)
        alertText.text=this.getString(R.string.warn_cancelpost)
        cancel.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
        }
        positive.setOnClickListener {
            parentFragmentManager.popBackStack()
            dialog.dismiss()
            dialog.cancel()
        }
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }
    private fun setupRecyclerView()=binding.recvImages.apply{
        adapter=postimageAdapter
        layoutManager= LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL,false)
        itemAnimator=null
    }
    private fun getImages()
    {
        val imageIntent= Intent(Intent.ACTION_PICK).also{
            it.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
            it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            getContent.launch(it)
        }
    }
    private fun subsribeToObserver()
    {
        UploadService.postResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onLoading={
            },
            onError = {
                progressDialog.dismiss()
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
            }
        ){
            if(it.equals("200"))
            {
                progressDialog.dismiss()
                //binding.rlbottom.visibility=View.GONE
                //findNavController().popBackStack()
                parentFragmentManager.popBackStack()
            }
            else
            {
                showToast("오류가 발생했습니다 죄송합니다")
            }
        })
        vmUpload.voteoptions.observe(viewLifecycleOwner){
            if(it==null)
            {
                binding.votelayout.visibility=View.GONE
                binding.imgvoteset.visibility=View.INVISIBLE
            }
            else{
                binding.votelayout.visibility=View.VISIBLE
                binding.imgvoteset.visibility=View.VISIBLE
            }
            voteoptions=it
        }
        vmUpload.anonymous.observe(viewLifecycleOwner){
            anonymous=it
        }
        vmUpload.curAccount.observe(viewLifecycleOwner){
            curaccount=it
        }
        vmUpload.curPlatform.observe(viewLifecycleOwner){
            curplatform=it
        }

        vmUpload.gpsAllowed.observe(viewLifecycleOwner){
            gpsAllowed=it
            if(gpsAllowed!!)
                binding.btnLocation.setImageResource(R.drawable.ic_locationon)
            else
                binding.btnLocation.setImageResource(R.drawable.ic_locationoff)
        }
        vmUpload.recordedPath.observe(viewLifecycleOwner){
            recordedPath=it
        }
        vmUpload.tagSearchResponse.observe(viewLifecycleOwner, Event.EventObserver(
            onError = {
                SocialApplication.showError(
                    binding.root,
                    requireContext(),
                    (activity as MainActivity).isConnected!!,
                    it
                )
                binding.progresstag.visibility=View.GONE
            },
            onLoading={
              binding.progresstag.visibility=View.VISIBLE
            }
        ){
            binding.progresstag.visibility=View.GONE
            handleResponse(requireContext(),it.resultCode) {
                binding.cgSearched.removeAllViews()
                binding.cgSearched.visibility = View.VISIBLE
                binding.tvTag.visibility = View.VISIBLE
                if (it.resultCode == 100) { //검색결과 연관태그가 없을시
                    var tagstr = binding.edtTag.text.toString()
                    var changedtag = tagstr
                    if (tagstr.contains("#")) {
                        changedtag = tagstr.replace("#", "")
                    }
                    val chip = Chip(requireContext()).apply {
                        text = changedtag + "(0)"
                        chipStrokeWidth = 1f
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            chipBackgroundColor =
                                AppCompatResources.getColorStateList(requireContext(), R.color.skin)
                            setChipStrokeColorResource(R.color.chiptext)
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.chiptext))
                        }
                        setOnClickListener {
                            binding.edtTag.setText(null)
                            hideKeyboard()
                            genSelectedChip(this.text.toString())
                        }
                    }
                    binding.cgSearched.addView(chip)
                } else {
                    genSearchedChip(it.tags)
                }
            }
        })
    }
    private fun showToast(msg:String)
    {
        val toast= Toast.makeText(requireContext(),msg, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM,0,120)
        toast.show()
    }
    private fun genSelectedChip(tag:String)
    {
        var exist:Boolean=false
        binding.cgselected.visibility= View.VISIBLE
        var token=tag.split('(')
        val selectedTag=token[0]
        for(i in 0 until binding.cgselected.childCount)
        {
            val chip=binding.cgselected.getChildAt(i) as Chip
            val chiptext=chip.text.toString()
            if(chiptext.equals("#"+selectedTag))
            {
                exist=true
            }
        }
        if(!exist)
        {
            val chip= Chip(requireContext()).apply {
                text="#"+selectedTag
                isCloseIconVisible=true
                setOnCloseIconClickListener {
                    binding.cgselected.removeView(this)
                }
            }
            binding.cgselected.addView(chip)
        }
    }
    fun hideKeyboard() {
        if (::inputMethodManager.isInitialized.not()) {
            inputMethodManager=activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        }
        inputMethodManager.hideSoftInputFromWindow(binding.edtTag.windowToken, 0)
    }
    private fun genSearchedChip(tags:List<TagResult>)
    {
        for(i in tags)
        {
            val chip= Chip(requireContext()).apply{
                text=i.tagname+"("+i.count+")"
                chipStrokeWidth=1f
                setTextSize(TypedValue.COMPLEX_UNIT_DIP , 16f)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    chipBackgroundColor=
                        AppCompatResources.getColorStateList(requireContext(), R.color.skin)
                    setChipStrokeColorResource(R.color.black)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
                setOnClickListener {
                    binding.edtTag.setText(null)
                    hideKeyboard()
                    genSelectedChip(this.text.toString())
                }
            }
            binding.cgSearched.addView(chip)
        }
    }
    fun <T> List<T>.random() : T {
        val random = Random().nextInt((size))
        return get(random)
    }
    private fun genAnonymous():String
    {
        var resultstr="NONE"
        if(anonymous)
        {
            resultstr=""
            val datas=listOf("a", "b", "c", "d", "e","f","g","h","i","j","1","2","3","4","0","5","6","7","8","9")
            for(i in 0 until 6)
            {
                resultstr+=datas.random()
            }
        }
        return resultstr
    }
    private fun getVoteoptionstr():String?
    {
        var jsonstr:String?=null
        voteoptions?.let{ options->
            val jsonObjectList = JSONArray()
            for(i in options)
            {
                if(i.option!="")
                {
                    val tempJsonObject = JSONObject()
                    tempJsonObject.put("voteoption",i.option)
                    jsonObjectList.put(tempJsonObject)
                }
            }
            jsonstr=jsonObjectList.toString()
        }
        return jsonstr
    }
    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }
    override fun onDestroy() {
        serviceUnbind()
        super.onDestroy()
        (activity as MainActivity).setupTopBottom()
    }
}