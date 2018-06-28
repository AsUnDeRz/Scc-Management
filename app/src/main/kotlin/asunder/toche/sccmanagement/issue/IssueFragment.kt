package asunder.toche.sccmanagement.issue

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ScrollView
import android.widget.Toast
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.contact.adapter.CompanyAdapter
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.dialog.ConfirmDialog
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.issue.adapter.FileAdapter
import asunder.toche.sccmanagement.issue.adapter.IssueAdapter
import asunder.toche.sccmanagement.issue.adapter.PictureAdapter
import asunder.toche.sccmanagement.main.ActivityImageViewer
import asunder.toche.sccmanagement.main.ControlViewModel
import asunder.toche.sccmanagement.main.FilterViewPager
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.Crashlytics
import com.google.firebase.storage.FirebaseStorage
import com.snatik.storage.Storage
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_issue.*
import kotlinx.android.synthetic.main.fragment_issue_add.*
import kotlinx.android.synthetic.main.layout_input.*
import kotlinx.android.synthetic.main.section_issue_confirm.*
import kotlinx.android.synthetic.main.section_issue_info.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.io.File
import java.io.IOException
import java.util.*


/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class IssueFragment : Fragment(),
        CompanyAdapter.CompanyOnClickListener,
        IssueAdapter.IssueItemListener,
        ConfirmDialog.ConfirmDialogListener,
        ComponentListener {


    companion object {
        fun newInstance(): IssueFragment = IssueFragment()
    }


    private lateinit var rootLayoutInput : ScrollView
    private lateinit var rootIssueForm : ConstraintLayout
    private lateinit var sheetDisableCard: BottomSheetBehavior<View>
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var issueVM: IssueViewModel
    private lateinit var contactVm : ContactViewModel
    private lateinit var controlViewModel: ControlViewModel
    val selectedFile = arrayListOf<String>()
    val selectedPhoto = arrayListOf<String>()
    var selectedDate : Date = Utils.getCurrentDate()
    private lateinit var adapter: CompanyAdapter
    private var loading = LoadingDialog.newInstance()
    private lateinit var sectionIssueAdapter : SectionedRecyclerViewAdapter
    private var isInitView = false
    private val pictures = mutableListOf<Model.Content>()
    private val files = mutableListOf<Model.Content>()
    private lateinit var pictureAdapter:PictureAdapter
    private lateinit var fileAdapter:FileAdapter
    lateinit var viewPager:ViewPager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        issueVM = ViewModelProviders.of(activity!!).get(IssueViewModel::class.java)
        contactVm = ViewModelProviders.of(activity!!).get(ContactViewModel::class.java)
        controlViewModel = ViewModelProviders.of(activity!!).get(ControlViewModel::class.java)
        initControllState()
    }

    fun initControllState(){
        controlViewModel.currentUI.observe(this, Observer {
            if (it == ROOT.ISSUE){
                if (!isInitView) {
                    initViewCreated()
                }
                when {
                    issueVM.isSaveIssueComplete.value == IssueState.SHOWFROM -> {
                        showIssueForm()
                        viewPager.currentItem = 1
                        tabLayoutFilterIssue.setScrollPosition(1, 0f, true)
                    }
                    issueVM.isSaveIssueComplete.value == IssueState.TRIGGERFROMSERVICE -> {

                    }
                    else -> {
                        async(UI) {
                            val data = async(CommonPool) {
                                issueVM.sortToday(this@IssueFragment)
                            }
                            separateSection(data.await())
                        }
                        viewPager.currentItem = 1
                        tabLayoutFilterIssue.setScrollPosition(1, 0f, true)
                        issueVM.updateViewState(IssueState.ALLISSUE)
                    }
                }
            }else{
                if (issueVM.isSaveIssueComplete.value == IssueState.SHOWFROM){
                    saveIssue()
                }
            }
        })
    }

    fun initViewCreated(){
        isInitView = true
        issueVM.loadIssue()
        async(UI) {
            val data = async(CommonPool) {
                issueVM.sortAll(this@IssueFragment)
            }
            separateSection(data.await())
        }
        rvSectionIssue.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        observerTabFilterIssue()
        initFilterWithButton()
        observerIssue()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_issue,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateStubIssueAdd()
        inflateStubLayoutInput()
        initViewCreated()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> if (resultCode == Activity.RESULT_OK && data != null) {
                selectedPhoto.clear()
                selectedPhoto.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                if(selectedPhoto.size > 0) {
                    val pictureList= mutableListOf<Model.Content>()
                    selectedPhoto.forEach {
                        pictureList.add(Model.Content(it))
                    }
                    pictureAdapter.addPictures(pictureList)
                }
            }
            FilePickerConst.REQUEST_CODE_DOC -> if (resultCode == Activity.RESULT_OK && data != null) {
                selectedFile.clear()
                selectedFile.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS))
                if(selectedFile.size > 0) {
                    val fileList = mutableListOf<Model.Content>()
                    selectedFile.forEach {
                        fileList.add(Model.Content(it))
                    }
                    fileAdapter.addFiles(fileList)
                }
            }
            KEY.EDIT_ISSUE_DETAIL ->{
                if (resultCode == Activity.RESULT_OK && data != null){
                    edtIssueDetail.setText(data.getStringExtra(KEY.EDIT_ISSUE_DETAIL.toString()))
                }
            }
        }
    }

    fun observerTabFilterIssue(){
        viewPager = ViewPager(context!!)
        viewPager.adapter = FilterViewPager(fragmentManager,true)
        tabLayoutFilterIssue.setupWithViewPager(viewPager)
        tabLayoutFilterIssue.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
                println("onTabReselected ${tab?.position}")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 ->{
                        //all
                        async(UI) {
                            val data = async(CommonPool) {
                                issueVM.sortAll(this@IssueFragment)
                            }
                            separateSection(data.await())
                        }
                    }
                    1 ->{
                        //today
                        async(UI) {
                            val data = async(CommonPool) {
                                issueVM.sortToday(this@IssueFragment)
                            }
                            separateSection(data.await())
                        }
                    }
                    2 ->{
                        //tomorrow
                        async(UI) {
                            val data = async(CommonPool) {
                                issueVM.sortTomorrow(this@IssueFragment)
                            }
                            separateSection(data.await())
                        }
                    }
                    3 ->{
                        //yesterday
                        async(UI) {
                            val data = async(CommonPool) {
                                issueVM.sortYesterday(this@IssueFragment)
                            }
                            separateSection(data.await())
                        }
                    }
                }
            }
        })
    }

    fun separateSection(sectionIssueAdapter : SectionedRecyclerViewAdapter){
        rvSectionIssue.adapter = sectionIssueAdapter
    }

    fun initFilterWithButton(){
        btnAll.setOnClickListener {
            showIssueList()
        }

        btnToDay.setOnClickListener {
            showIssueList()
        }

        btnTomorrow.setOnClickListener {
            showIssueList()
        }

        btnYesterday.setOnClickListener {
            showIssueList()
        }
    }



    fun inflateStubIssueAdd(){
        stubIssueAdd.setOnInflateListener { _, v ->
            rootIssueForm = v as ConstraintLayout
            rootIssueForm.visibility = View.GONE
        }
        stubIssueAdd.inflate()
        initIssueForm()
    }


    fun inflateStubLayoutInput(){
        stubLayoutInput.setOnInflateListener { _, v ->
            rootLayoutInput = v as ScrollView
            rootLayoutInput.visibility = View.GONE
        }
        stubLayoutInput.inflate()
        initLayoutInput()
    }

    fun initIssueForm(){
        initContentAdapter()
        btnAddIssueInfo.setOnClickListener {
            when {
                issueVM.isSaveIssueComplete.value == IssueState.TRIGGERFROMSERVICE -> {
                    saveIssue()
                    contactVm.updateContact(contactVm.contact.value!!)
                    contactVm.updateViewState(ContactState.SELECTCONTACT)
                }
                issueVM.isSaveIssueComplete.value == IssueState.NEWFROMCONTACT -> {
                    saveIssue()
                    contactVm.updateViewState(ContactState.SELECTCONTACT)
                }
                else -> saveIssue()
            }
        }
        btnCancelIssueInfo.setOnClickListener {
            when {
                issueVM.isSaveIssueComplete.value == IssueState.TRIGGERFROMSERVICE -> {
                    saveIssue()
                    contactVm.updateContact(contactVm.contact.value!!)
                    contactVm.updateViewState(ContactState.SELECTCONTACT)
                }
                issueVM.isSaveIssueComplete.value == IssueState.NEWFROMCONTACT -> {
                    contactVm.updateViewState(ContactState.SELECTCONTACT)
                }
                else -> {
                    clearFormIssue()
                    issueVM.updateViewState(IssueState.ALLISSUE)
                }
            }
        }
        btnDeleteIssueInfo.setOnClickListener {
            showConfirmDialog(edtIssue.text.toString(),issueVM.companyReference.value?.company)
        }

        edtProcess.setOnClickListener {
            showSheetProcess()
        }

        edtCompany.setOnClickListener {
            showSheetCompany()
        }

        edtIssueDate.setOnClickListener {
            showSpinner()
        }
        edtIssueDetail.DisableClick()
        edtIssueDetail.setOnClickListener {
            val intent = Intent()
            intent.putExtra(KEY.EDIT_ISSUE_DETAIL.toString(),edtIssueDetail.text.toString())
            startActivityForResult(intent.setClass(activity,ActivityDetailIssue::class.java), KEY.EDIT_ISSUE_DETAIL)
        }

        addFile.setOnClickListener {
            selectedFile.clear()
            FilePickerBuilder.getInstance()
                    .setSelectedFiles(selectedFile)
                    .setActivityTheme(R.style.AppTheme)
                    .pickFile(this)
        }

        addPicture.setOnClickListener {
            selectedPhoto.clear()
            FilePickerBuilder.getInstance()
                    .setSelectedFiles(selectedPhoto)
                    .setActivityTheme(R.style.AppTheme)
                    .pickPhoto(this)
        }

        btnOpenContact.setOnClickListener {
            contactVm.updateContact(issueVM.companyReference.value!!)
            contactVm.updateViewState(ContactState.TRIGGERFROMISSUE)
        }

    }
    fun initLayoutInput(){
        btnSaveInput.setOnClickListener {
            showIssueForm()
        }
        btnCancelInput.setOnClickListener {
            showIssueForm()
        }

    }

    fun showIssueForm(){
        println("Show Issue Form")
        issueScrollView.fullScroll(ScrollView.FOCUS_UP)
        edtCompany.requestFocus()
        issueVM.updateViewState(IssueState.SHOWFROM)
        rootIssueForm.visibility = View.VISIBLE
        rootLayoutInput.visibility = View.GONE
    }

    fun showIssueList(){
        rootIssueForm.visibility = View.GONE
        rootLayoutInput.visibility = View.GONE
    }

    fun showLayoutInput(){
        rootLayoutInput.visibility = View.VISIBLE
        rootIssueForm.visibility = View.GONE
    }

    fun showSheetProcess(){
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_issue, null)
        bottomSheetDialog = BottomSheetDialog(context!!)
        bottomSheetDialog.setContentView(bottomSheetView)
        sheetDisableCard = BottomSheetBehavior.from(bottomSheetView.parent as View)
        if (sheetDisableCard.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetDialog.show()
        } else {
            bottomSheetDialog.dismiss()
        }

        val btnWaiting = bottomSheetView.findViewById<BtnMedium>(R.id.btnWaiting)
        val btnDone = bottomSheetView.findViewById<BtnMedium>(R.id.btnDone)
        btnWaiting.setOnClickListener {
            edtProcess.setText("รอทำ")
            edtProcess.setTextColor(ContextCompat.getColor(this.context!!,R.color.colorDark))
            edtProcess.setBackgroundColor(ContextCompat.getColor(this.context!!,R.color.colorWaiting))
            bottomSheetDialog.dismiss()
        }
        btnDone.setOnClickListener {
            edtProcess.setText("ทำแล้ว")
            edtProcess.setTextColor(ContextCompat.getColor(this.context!!,android.R.color.white))
            edtProcess.setBackgroundColor(ContextCompat.getColor(this.context!!,R.color.colorConfirm))
            bottomSheetDialog.dismiss()
        }

    }

    fun showSheetCompany(){
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_company, null)
        val rvFilterCompany = bottomSheetView.findViewById<RecyclerView>(R.id.rvFilterCompany)
        val txtFilter = bottomSheetView.findViewById<EdtMedium>(R.id.txtCompanyFilter)
        val btnCancel = bottomSheetView.findViewById<BtnMedium>(R.id.btnCancel)
        bottomSheetDialog = BottomSheetDialog(context!!)
        bottomSheetDialog.setContentView(bottomSheetView)
        sheetDisableCard = BottomSheetBehavior.from(bottomSheetView.parent as View)
        if (sheetDisableCard.state != BottomSheetBehavior.STATE_EXPANDED) {
            setUpAdapterCompany()
            bottomSheetDialog.show()

        } else {
            bottomSheetDialog.dismiss()
        }
        rvFilterCompany.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = this@IssueFragment.adapter
        }

        txtFilter.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Utils.findCompany(s.toString(),object : Utils.OnFindCompanyListener{
                    override fun onResults(results: MutableList<Model.Contact>) {
                        adapter.setContact(results)
                    }
                },issueVM.getContact())
            }
        })

        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
    }

    fun setUpAdapterCompany(){
        adapter = CompanyAdapter(false)
        adapter.setUpOnClickListener(this)
        adapter.setContact(issueVM.getContact())
    }

    fun showSpinner(){

        val c = Calendar.getInstance()
        c.time = selectedDate
        val mount = c.get(Calendar.MONTH)
        val dOfm = c.get(Calendar.DAY_OF_MONTH)
        var year = c.get(Calendar.YEAR)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val spinner = SpinnerDatePickerDialogBuilder()
                .context(context)
                .callback { _, yearOf, monthOfYear, dayOfMonth, hourOf, minuteOf ->
                    val dateSelect = Calendar.getInstance()
                    dateSelect.set(yearOf,monthOfYear,dayOfMonth,hourOf,minuteOf,0)
                    selectedDate = dateSelect.time
                    edtIssueDate.setText(Utils.getDateStringWithDate(selectedDate).substring(0,10))
                }
                .spinnerTheme(R.style.DatePickerSpinner)
                .year(year)
                .monthOfYear(mount)
                .dayOfMonth(dOfm)
                .hour(hour)
                .minute(minute)
                .build()

        spinner.show()
    }



    fun setUpIssueForm(issue:Model.Issue){
        if(issueVM.currentIssue.value?.id != issue.id){
            issueVM.updateCurrentIssue(issue)
        }
        edtIssue.setText(issue.issue_name)
        edtIssueDetail.setText(issue.issue_desc)
        issueVM.currentIssueId = issue.id
        if(issue.company_id == "") {
            edtCompany.setText(issue.company_id)
        }else{
            //filter company with id
            issueVM.findCompanyWithKey(issue.company_id)
        }
        edtIssueDate.setText(issue.date.substring(0,10))
        selectedDate = Utils.getDateWithString(issue.date)
        if (issue.pictures.isNotEmpty()){
            pictureAdapter.updatePictures(issue.pictures)
        }else{
            pictureAdapter.clear()
            selectedPhoto.clear()
        }
        if (issue.files.isNotEmpty()){
            fileAdapter.updateFiles(issue.files)
        }else{
            fileAdapter.clear()
            selectedFile.clear()
        }
        if (issue.status == "รอทำ"){
            edtProcess.setText("รอทำ")
            edtProcess.setTextColor(ContextCompat.getColor(this.context!!,R.color.colorDark))
            edtProcess.setBackgroundColor(ContextCompat.getColor(this.context!!,R.color.colorWaiting))
        }else{
            edtProcess.setText("ทำแล้ว")
            edtProcess.setTextColor(ContextCompat.getColor(this.context!!,android.R.color.white))
            edtProcess.setBackgroundColor(ContextCompat.getColor(this.context!!,R.color.colorConfirm))
        }
    }

    fun clearFormIssue(){
        edtProcess.setText("รอทำ")
        edtProcess.setTextColor(ContextCompat.getColor(this.context!!,R.color.colorDark))
        edtProcess.setBackgroundColor(ContextCompat.getColor(this.context!!,R.color.colorWaiting))
        edtIssue.setText("")
        edtIssueDetail.setText("")
        edtCompany.setText("")
        selectedDate = Utils.getCurrentDate()
        edtIssueDate.setText(Utils.getCurrentDateShort())
        val options = RequestOptions().centerCrop()
        selectedPhoto.clear()
        selectedFile.clear()
        issueVM.updateCompany(Model.Contact())
        issueVM.currentIssueId = ""
        pictureAdapter.clear()
        fileAdapter.clear()
    }

    override fun onClickCompany(contact: Model.Contact) {
        edtCompany.setText(contact.company)
        issueVM.updateCompany(contact)
        bottomSheetDialog.dismiss()
    }

    fun observerIssue(){
        issueVM.currentIssue.observe(this, Observer {
            if (issueVM.isSaveIssueComplete.value == IssueState.TRIGGERFROMSERVICE){
                it?.let { it1 -> setUpIssueForm(it1) }
            }
            if (issueVM.isSaveIssueComplete.value == IssueState.SHOWFROM){
                println("Setup IssueFrom with $it")
                it?.let { it1 -> setUpIssueForm(it1) }
            }
            println("Current State ${issueVM.isSaveIssueComplete.value}")
        })

        issueVM.companyReference.observe(this, Observer {
            edtCompany.setText(it?.company)
        })

        issueVM.isSaveIssueComplete.observe(this, Observer {
            when (it){
                IssueState.ALLISSUE ->{
                    showIssueList()
                    if (loading.isShow){
                        loading.dismiss()
                    }
                }
                IssueState.NEWISSUE ->{
                    showIssueForm()
                    clearFormIssue()
                }
                IssueState.NEWFROMCONTACT ->{
                    println("Show Issue Form")
                    issueScrollView.fullScroll(ScrollView.FOCUS_UP)
                    edtCompany.requestFocus()
                    rootIssueForm.visibility = View.VISIBLE
                    rootLayoutInput.visibility = View.GONE
                    clearFormIssue()
                    issueVM.updateCompany(contactVm.contact.value!!)
                }
                IssueState.TRIGGERFROMSERVICE ->{
                    println("Trigger from service")
                    issueScrollView.fullScroll(ScrollView.FOCUS_UP)
                    edtCompany.requestFocus()
                    rootIssueForm.visibility = View.VISIBLE
                    rootLayoutInput.visibility = View.GONE
                }

            }
        })
        issueVM.issues.observe(this, Observer {
            when(tabLayoutFilterIssue.selectedTabPosition){
                0 ->{
                    //all
                    async(UI) {
                        val data = async(CommonPool) {
                            issueVM.sortAll(this@IssueFragment)
                        }
                        separateSection(data.await())
                    }
                }
                1 ->{
                    //today
                    async(UI) {
                        val data = async(CommonPool) {
                            issueVM.sortToday(this@IssueFragment)
                        }
                        separateSection(data.await())
                    }
                }
                2 ->{
                    //tomorrow
                    async(UI) {
                        val data = async(CommonPool) {
                            issueVM.sortTomorrow(this@IssueFragment)
                        }
                        separateSection(data.await())
                    }
                }
                3 ->{
                    //yesterday
                    async(UI) {
                        val data = async(CommonPool) {
                            issueVM.sortYesterday(this@IssueFragment)
                        }
                        separateSection(data.await())
                    }
                }
            }
        })
    }

    fun saveIssue(){
        if (validateInput()) {
            val data = Model.Issue(issueVM.currentIssueId, edtProcess.text.toString()
                    , "", edtIssue.text.toString()
                    , edtIssueDetail.text.toString()
                    , Utils.getDateStringWithDate(selectedDate)
                    , pictureAdapter.pictures, fileAdapter.files)
            issueVM.saveIssue(data)
            loading.show(fragmentManager, LoadingDialog.TAG)
        }
    }

    fun validateInput() : Boolean{
        if(TextUtils.isEmpty(edtCompany.text)){
            edtCompany.error = "กรุณาเลือกบริษัท"
            return false
        }
        /*
        if(TextUtils.isEmpty(edtIssue.text)){
            edtIssue.error = "กรุณากรอกข้อมูล ประเด็น"
            return false
        }
        */
        return true
    }

    override fun onSelectIssue(issue: Model.Issue) {

    }

    override fun onClickEdit(issue: Model.Issue) {
        showIssueForm()
        setUpIssueForm(issue)
    }

    override fun onClickDelete(issue: Model.Issue) {
        issueVM.deleteIssue(issue)
    }

    fun showConfirmDialog(issue:String?,company:String?){
        val confirmDialog = ConfirmDialog.newInstance("คุณต้องการลบประเด็น $issue \nจาก ${company?.lines()?.first()} ใช่หรือไหม","แจ้งเตือน",true)
        confirmDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        confirmDialog.customListener(this)
        confirmDialog.show(fragmentManager, ConfirmDialog::class.java.simpleName)
    }

    override fun onClickConfirm() {
        when {
            issueVM.isSaveIssueComplete.value == IssueState.TRIGGERFROMSERVICE -> {
                issueVM.currentIssue.value?.let {
                    issueVM.deleteIssue(it)
                }
                contactVm.updateContact(contactVm.contact.value!!)
                contactVm.updateViewState(ContactState.SELECTCONTACT)
            }
            issueVM.isSaveIssueComplete.value == IssueState.NEWFROMCONTACT -> {
                issueVM.currentIssue.value?.let {
                    issueVM.deleteIssue(it)
                }
                contactVm.updateViewState(ContactState.SELECTCONTACT)
            }
            else -> {
                issueVM.currentIssue.value?.let {
                    issueVM.deleteIssue(it)
                }
                clearFormIssue()
            }
        }
    }

    override fun onClickCancel() {
    }

    override fun OnFileClick(file: Model.Content,isDeleteOrShare:Boolean, position: Int) {
        if (isDeleteOrShare) {
            fileAdapter.remove(position)
        }else {
            openFile(file)
        }
    }
    override fun OnPictureClick(picture: Model.Content,isDeleteOrShare:Boolean, position: Int) {
        if (isDeleteOrShare) {
            pictureAdapter.remove(position)
        }else{
            openPicture(picture)
        }
    }
    fun initContentAdapter(){
        pictureAdapter = PictureAdapter(this@IssueFragment)
        fileAdapter = FileAdapter(this@IssueFragment)
        rvFile.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = fileAdapter
        }
        rvPicture.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = pictureAdapter
        }
    }

    fun openFile(path: Model.Content){
        val f = File(path.local_path)
        val fileWithinMyDir = File(path.local_path)
        if (fileWithinMyDir.exists()) {
            val intent = Intent(Intent.ACTION_VIEW)
            val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(MimeTypeMap
                            .getFileExtensionFromUrl(f.path))
            intent.setDataAndType(Uri.fromFile(f), mimeType)
            this.startActivity(Intent.createChooser(intent, "Open file with"))
        }else{
            downloadInLocalFile(path.cloud_url,true)
        }
    }

    fun openPicture(picture: Model.Content){
        val fileWithinMyDir = File(picture.local_path)
        if (fileWithinMyDir.exists()) {
            val intent = Intent()
            intent.putExtra("path",picture.local_path)
            activity?.startActivity(intent.setClass(activity,ActivityImageViewer::class.java))
        }else{
            downloadInLocalFile(picture.cloud_url,false)
        }
    }

    fun downloadInLocalFile(path: String,isFile:Boolean) {
        val stor = Storage(context)
        val externalPath = stor.externalStorageDirectory
        val newDir = externalPath + File.separator + Prefer.getUUID(this.context!!)
        stor.createDirectory(newDir)

        val storageRef = FirebaseStorage.getInstance().reference
        val content = storageRef.child(path)

        val file = File(newDir, content.name)
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val fileDownloadTask = content.getFile(file)

        loading.show(fragmentManager,LoadingDialog.TAG)
        fileDownloadTask.addOnSuccessListener {
            loading.dismiss()
            println("DownloadFileSuccess"+file.absolutePath)

            if (isFile) {
                val intent = Intent(Intent.ACTION_VIEW)
                val mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(MimeTypeMap
                                .getFileExtensionFromUrl(file.path))
                intent.setDataAndType(Uri.fromFile(file), mimeType)
                this.startActivity(Intent.createChooser(intent, "Open file with"))
            }else{
                val intent = Intent()
                intent.putExtra("path",file.path)
                activity?.startActivity(intent.setClass(activity,ActivityImageViewer::class.java))
            }
        }.addOnFailureListener { exception ->
            loading.dismiss()
            Crashlytics.log(exception.message)
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            println("onDownloadProgress $progress")
        }
    }



}