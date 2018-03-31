package asunder.toche.sccmanagement.issue

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.issue.adapter.SectionIssueAdapter
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ContactService
import asunder.toche.sccmanagement.service.FirebaseManager
import asunder.toche.sccmanagement.service.IssueService
import asunder.toche.sccmanagement.transactions.IssueListener
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.io.File

/**
 *Created by ToCHe on 16/3/2018 AD.
 */
class IssueViewModel : ViewModel(),ContactService.ContactCallBack,
                    Utils.OnFindCompanyListener,
                    IssueService.IssueCallBack {

    val service = IssueService(this)
    val contactService = ContactService(this)
    val firebase = FirebaseManager()
    val issues : MutableLiveData<MutableList<Model.Issue>> = MutableLiveData()
    val currentIssue : MutableLiveData<Model.Issue> = MutableLiveData()
    val companyReference : MutableLiveData<Model.Contact> = MutableLiveData()
    var isSaveIssueComplete  = MutableLiveData<IssueState>()
    var currentIssueId = ""


    fun getContact():MutableList<Model.Contact>{
        return contactService.getContactInDb()
    }

    fun findCompanyWithKey(key:String){
        Utils.findCompany(key,this,getContact())
    }

    override fun onResults(results: MutableList<Model.Contact>) {
        if(results.size == 1){
            updateCompany(results[0])
        }
    }

    fun updateCompany(company : Model.Contact){
        companyReference.value = company
    }

    fun saveIssue(data:Model.Issue) = async(UI) {
        try {
            val job = async(CommonPool) {
                if(data.file_path != ""){
                    val filePath = Uri.fromFile(File(data.file_path))
                    data.file_url = "${ROOT.IMAGES}/${Prefer.getUUID(firebase.context!!)}/${filePath.lastPathSegment}"
                    firebase.pushFileToFirebase(data.file_path,"")
                }
                if(data.image_path != ""){
                    val imgPath = Uri.fromFile(File(data.image_path))
                    data.image_url = "${ROOT.IMAGES}/${Prefer.getUUID(firebase.context!!)}/${imgPath.lastPathSegment}"
                    firebase.pushFileToFirebase(data.image_path,"")
                }
                data.company_id = companyReference.value!!.id
            }
            job.await()
            if (currentIssueId == "") {
                service.pushNewIssue(data)
            }else{
                service.updateIssue(data)
            }
        }
        catch (e: Exception) {
        }
        finally {
        }
    }

    fun tranformFormat() : List<String> {
        val tranformFormat :MutableList<String> = mutableListOf()
        issues.value?.forEach {
            tranformFormat.add(it.date.substring(0,10))
        }
        return tranformFormat.distinctBy { it }
    }

    fun setSectionAdapter(sectionList: List<String>,
                          results: MutableMap<String,
                                  List<Model.Issue>?>,
                          listener: IssueListener)
            :SectionedRecyclerViewAdapter{
        val sectionIssueAdapter = SectionedRecyclerViewAdapter()
        sectionList.forEach {
            val section = SectionIssueAdapter(it, results[it] as MutableList<Model.Issue>,listener)
            sectionIssueAdapter.addSection(section)
        }
        return sectionIssueAdapter
    }

    fun sortAll(listener: IssueListener) : SectionedRecyclerViewAdapter{
        val sectionList = tranformFormat().sortedByDescending { Utils.getDateString(it).time }
        val resultIssue = separateSection(sectionList, false)
        return setSectionAdapter(sectionList, resultIssue,listener)
    }
    fun sortToday(listener: IssueListener): SectionedRecyclerViewAdapter{
        val sectionList = tranformFormat().filter { Utils.getDateString(it).time <= Utils.getCurrentDate().time }.sortedByDescending { Utils.getDateString(it).time }
        val resultIssue = separateSection(sectionList,true)
        return setSectionAdapter(sectionList,resultIssue,listener)
    }
    fun sortTomorrow(listener: IssueListener): SectionedRecyclerViewAdapter{
        val sectionList = tranformFormat().filter { Utils.getDateString(it).time > Utils.getCurrentDate().time }
                .sortedByDescending { Utils.getDateString(it).time }
        val resultIssue = separateSection(sectionList,false)
        return setSectionAdapter(sectionList,resultIssue,listener)
    }
    fun sortYesterday(listener: IssueListener): SectionedRecyclerViewAdapter{
        val sectionList = tranformFormat().filter { Utils.getDateString(it).time < Utils.getPreviusDate().time }
                .sortedByDescending { Utils.getDateString(it).time }
        val resultIssue = separateSection(sectionList,false)
        return setSectionAdapter(sectionList,resultIssue,listener)
    }

    fun separateSection(sectionList : List<String>,isToday:Boolean) : MutableMap<String,List<Model.Issue>?>{
        val masterSection :MutableMap<String,List<Model.Issue>?> = mutableMapOf()
        if(!isToday) {
            sectionList.forEach { section ->
                masterSection[section] = issues.value?.filter { section == it.date.substring(0, 10) }
            }
        }else{
            sectionList.forEach { section ->
                masterSection[section] = issues.value?.filter { section == it.date.substring(0, 10) &&
                it.status == "รอทำ"}
            }
        }
        println("separateSection success")
        return masterSection
    }

    fun loadIssue(){
        val data = service.getIssueInDb()
        updateIssues(data)
    }

    fun updateViewState(state :IssueState){
        isSaveIssueComplete.value = state
    }

    fun updateIssues(data:MutableList<Model.Issue>){
        issues.value = data
    }

    fun updateCurrentIssue(data: Model.Issue){
        currentIssue.value = data
    }

    override fun onIssueSuccess() {
        updateViewState(IssueState.ALLISSUE)
        loadIssue()
    }

    override fun onIssueFail() {
        updateViewState(IssueState.ALLISSUE)
        loadIssue()
    }


    override fun onSuccess() {
    }

    override fun onFail() {
    }



}