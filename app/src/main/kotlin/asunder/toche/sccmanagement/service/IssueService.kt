package asunder.toche.sccmanagement.service

import android.content.Context
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class IssueService{


    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName


    fun pushNewIssue(Issue: Model.Issue, context: Context){
        val keyAuth = firebase.child("${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.ISSUE}").push().key
        Issue.id = keyAuth
        Log.d(TAG,"PushNewIssue with $Issue")
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.ISSUE}/$keyAuth"] = Issue
        firebase.updateChildren(childUpdates,{databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Issue,Managemnt saved successfully.")
            }
        })

    }

    fun updateIssue(Issue: Model.Issue, context: Context){
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.ISSUE}/${Issue.id}"] = Issue
        firebase.updateChildren(childUpdates,{ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Update Issue successfully.")
            }
        })

    }

    fun deleteIssue(Issue : Model.Issue, context: Context){
        firebase.child("${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.ISSUE}/${Issue.id}")
                .removeValue({ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data deleted successfully.")
            }
        })
    }


}