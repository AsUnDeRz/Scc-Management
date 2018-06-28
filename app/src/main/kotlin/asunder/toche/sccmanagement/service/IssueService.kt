package asunder.toche.sccmanagement.service

import android.content.Context
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import com.crashlytics.android.Crashlytics
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.HashMap

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class IssueService(var listener : IssueCallBack){


    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName
    private val context = FirebaseApp.getInstance()?.applicationContext
    interface IssueCallBack{
        fun onIssueSuccess()
        fun onIssueFail()
    }



    fun pushNewIssue(issue: Model.Issue){
        val keyAuth = firebase.child("${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.ISSUE}").push().key
        issue.id = keyAuth!!
        Log.d(TAG,"PushNewIssue with $issue")
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.ISSUE}/$keyAuth"] = issue
        firebase.updateChildren(childUpdates) { databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Issue  saved successfully.")
            }
        }
        pushNewIssueToDb(updateIssueFromDb(issue,getIssueInDb()))


    }

    fun updateIssue(Issue: Model.Issue){
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.ISSUE}/${Issue.id}"] = Issue
        firebase.updateChildren(childUpdates) { databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Update Issue successfully.")
            }
        }
        pushNewIssueToDb(updateIssueFromDb(Issue,getIssueInDb()))

    }

    fun deleteIssue(Issue : Model.Issue){
        firebase.child("${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.ISSUE}/${Issue.id}")
                .removeValue { databaseError, _ ->
                    if (databaseError != null) {
                        Crashlytics.log(databaseError.message)
                        System.out.println("Data could not be saved " + databaseError.message)
                    } else {
                        System.out.println("Data deleted successfully $Issue.")
                    }
                }
    }

    fun getIssueInDb() : MutableList<Model.Issue>{
        val issue = Paper.book().read<Model.IssueUser>(ROOT.ISSUE)
        return if(issue != null) {
            issue.issues
        }else{
            Log.d(TAG,"Not Found Issue in DB")
            mutableListOf()
        }
    }


    fun pushNewIssueToDb(issues:MutableList<Model.Issue>) = async(UI) {
        try {
            val addIssue = async {
               Paper.book().write(ROOT.ISSUE,Model.IssueUser(issues))
                Log.d(TAG,"Paper write $issues")
            }
            addIssue.await()
            listener.onIssueSuccess()

        }
        catch (e: Exception) {
        }
        finally {
        }
    }

    fun deleteIssueInDb(uid:String){
            val currentIssue = getIssueInDb()
            currentIssue
                    .filter { it.id == uid }
                    .forEach {
                        currentIssue.remove(it)
                    }

            pushNewIssueToDb(currentIssue)
        //Paper.book().delete(ROOT.CONTACTS)
    }

    fun deleteIssueWithContact(contact:Model.Contact){
        val currentIssue = getIssueInDb()
        currentIssue
                .filter { it.company_id == contact.id }
                .forEach {
                    currentIssue.remove(it)
                    deleteIssue(it)
                }
        pushNewIssueToDb(currentIssue)
    }

    fun syncIssue(issueFromPhone:MutableList<Model.Issue>,issueFromDb:MutableList<Model.Issue>)
            : MutableList<Model.Issue>{
        val rawData = issueFromDb
        val newData = issueFromPhone

        return if(rawData.size > 0){
            for(raw in rawData){
                newData.filter { it.id == raw.id }
                        .forEach {
                            newData.remove(it)
                        }
            }
            rawData.addAll(newData)
            rawData
        }else{
            newData
        }
    }

    fun updateIssueFromDb(issue:Model.Issue,issueFromDb: MutableList<Model.Issue>)
            : MutableList<Model.Issue>{
        issueFromDb.filter { it.id == issue.id }
                .forEach {
                    issueFromDb.remove(it)
                }
        issueFromDb.add(issue)
        return issueFromDb
    }


}