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
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.adapter.CompanyAdapter
import asunder.toche.sccmanagement.contact.viewmodel.ContactViewModel
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.main.ControlViewModel
import asunder.toche.sccmanagement.main.FilterViewPager
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.transactions.IssueListener
import com.bumptech.glide.Glide
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
import java.util.*


/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class IssueFragment : Fragment(),CompanyAdapter.CompanyOnClickListener,IssueListener{

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
    var selectedDate : Date = Date()
    private lateinit var adapter: CompanyAdapter
    private var loading = LoadingDialog.newInstance()
    private lateinit var sectionIssueAdapter : SectionedRecyclerViewAdapter

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
                initViewCreated()
            }
        })
    }

    fun initViewCreated(){
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
        onClickNewIssue()
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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_PHOTO -> if (resultCode == Activity.RESULT_OK && data != null) {
                selectedPhoto.clear()
                selectedPhoto.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                if(selectedPhoto.size > 0) {
                    Glide.with(context!!)
                            .load(File(selectedPhoto[0]))
                            .into(imgIssue)
                }
            }
            FilePickerConst.REQUEST_CODE_DOC -> if (resultCode == Activity.RESULT_OK && data != null) {
                selectedFile.clear()
                selectedFile.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS))
                if(selectedFile.size > 0){
                    edtFileIssue.setText(Uri.fromFile(File(selectedFile[0])).lastPathSegment)
                }
            }
        }
    }

    fun observerTabFilterIssue(){
        val viewPager = ViewPager(context!!)
        viewPager.adapter = FilterViewPager(fragmentManager,true)
        tabLayoutFilterIssue.setupWithViewPager(viewPager)
        tabLayoutFilterIssue.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {
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

    fun onClickNewIssue(){
        imgNewIssue.setOnClickListener {
            showIssueForm()
            clearFormIssue()
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
        btnSaveIssue.setOnClickListener {
            if(validateInput()) saveIssue()
        }
        btnCancelIssue.setOnClickListener {
            clearFormIssue()
            showIssueList()
        }
        btnAddIssueInfo.setOnClickListener {
            if(validateInput()) saveIssue()
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

        edtFileIssue.setOnClickListener {
            selectedFile.clear()
            FilePickerBuilder.getInstance().setMaxCount(1)
                    .setSelectedFiles(selectedFile)
                    .setActivityTheme(R.style.AppTheme)
                    .pickFile(this)
        }

        imgIssue.setOnClickListener {
            selectedPhoto.clear()
            FilePickerBuilder.getInstance().setMaxCount(1)
                    .setSelectedFiles(selectedPhoto)
                    .setActivityTheme(R.style.AppTheme)
                    .pickPhoto(this)
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
        issueScrollView.fullScroll(ScrollView.FOCUS_UP)
        rootIssueForm.visibility = View.VISIBLE
        rootLayoutInput.visibility = View.GONE
        imgNewIssue.visibility = View.GONE
    }

    fun showIssueList(){
        rootIssueForm.visibility = View.GONE
        rootLayoutInput.visibility = View.GONE
        imgNewIssue.visibility = View.VISIBLE
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
            bottomSheetDialog.dismiss()
        }
        btnDone.setOnClickListener {
            edtProcess.setText("ทำแล้ว")
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
        val spinner = SpinnerDatePickerDialogBuilder()
                .context(context)
                .callback { _, yearOf, monthOfYear, dayOfMonth ->
                    val dateSelect = Calendar.getInstance()
                    dateSelect.set(yearOf,monthOfYear,dayOfMonth,0,0,0)
                    selectedDate = dateSelect.time
                    edtIssueDate.setText(Utils.getDateStringWithDate(selectedDate))
                }
                .spinnerTheme(R.style.DatePickerSpinner)
                .year(year)
                .monthOfYear(mount)
                .dayOfMonth(dOfm)
                .build()

        spinner.show()

    }


    fun setUpIssueForm(issue:Model.Issue){
        edtProcess.setText(issue.status)
        edtIssue.setText(issue.issue_name)
        edtIssueDetail.setText(issue.issue_desc)
        issueVM.currentIssueId = issue.id
        if(issue.company_id == "") {
            edtCompany.setText(issue.company_id)
        }else{
            //filter company with id
            issueVM.findCompanyWithKey(issue.company_id)
        }
        edtIssueDate.setText(issue.date)
        selectedDate = Utils.getDateWithString(issue.date)
        if(issue.image_path == ""){
            Glide.with(context!!)
                    .load("")
                    .into(imgIssue)
            selectedPhoto.clear()
        }else{
            Glide.with(context!!)
                    .load(File(issue.image_path))
                    .into(imgIssue)
            selectedPhoto.add(issue.image_path)
        }

        if(issue.file_path == ""){
            edtFileIssue.setText("")
            selectedFile.clear()
        }else{
            edtFileIssue.setText(Uri.fromFile(File(issue.file_path)).lastPathSegment)
            selectedFile.add(issue.file_path)
        }

    }

    fun clearFormIssue(){
        edtProcess.setText("รอทำ")
        edtIssue.setText("")
        edtIssueDetail.setText("")
        edtCompany.setText("")
        selectedDate = Utils.getCurrentDate()
        edtIssueDate.setText(Utils.getCurrentDateString())
        Glide.with(context!!)
                .load("")
                .into(imgIssue)
        edtFileIssue.setText("")
        selectedPhoto.clear()
        selectedFile.clear()
        issueVM.updateCompany(Model.Contact())
        issueVM.currentIssueId = ""
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
        })

        issueVM.companyReference.observe(this, Observer {
            edtCompany.setText(it?.company)
        })

        issueVM.isSaveIssueComplete.observe(this, Observer {
            when (it){
                IssueState.ALLISSUE ->{
                    showIssueList()
                    loading.dismiss()
                }
                IssueState.NEWISSUE ->{

                }
                IssueState.NEWFROMCONTACT ->{
                    showIssueForm()
                    issueVM.updateCompany(contactVm.contact.value!!)
                }
                IssueState.TRIGGERFROMSERVICE ->{
                    showIssueForm()
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
        System.out.println("Check selected $selectedPhoto  $selectedFile")
        val photoPath = if (selectedPhoto.isEmpty()) "" else selectedPhoto[0]
        val filePath = if (selectedFile.isEmpty()) "" else selectedFile[0]
        val data = Model.Issue(issueVM.currentIssueId,edtProcess.text.toString(),"",edtIssue.text.toString()
                ,edtIssueDetail.text.toString(),Utils.getDateStringWithDate(selectedDate),"",
                photoPath,"",filePath)
        async(UI) {
            issueVM.saveIssue(data).await()
        }
        loading.show(fragmentManager, LoadingDialog.TAG)
    }

    fun validateInput() : Boolean{
        if(TextUtils.isEmpty(edtCompany.text)){
            edtCompany.error = "กรุณาเลือกบริษัท"
            return false
        }
        if(TextUtils.isEmpty(edtIssue.text)){
            edtIssue.error = "กรุณากรอกข้อมูล ประเด็น"
            return false
        }
        return true
    }

    override fun onClickIssue(issue: Model.Issue) {
        showIssueForm()
        setUpIssueForm(issue)

    }


}