package asunder.toche.sccmanagement.service

import android.content.Context
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper
import java.util.*

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class ContactService{

    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName


    fun pushNewContact(contact: Model.Contact,context: Context){
        val keyAuth = firebase.child("${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.CONTACTS}").push().key
        contact.id = keyAuth
        Log.d(TAG,"PushNewContact with $contact")
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.CONTACTS}/$keyAuth"] = contact
        firebase.updateChildren(childUpdates,{databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Contact,Managemnt saved successfully.")
            }
        })

    }

    fun updateContact(contact: Model.Contact,context: Context){
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.CONTACTS}/${contact.id}"] = contact

        firebase.updateChildren(childUpdates,{ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Update Contact successfully.")
            }
        })

    }

    fun deleteContact(contact : Model.Contact,context: Context){
        firebase.child("${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.CONTACTS}/${contact.id}").removeValue({ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data deleted successfully.")
            }
        })
    }

    fun getContactInDb(uid: String) : MutableList<Model.Contact>{
        val contact = Paper.book().read<Model.ContactUser>(ROOT.CONTACTS)
        return if(contact != null) {
            contact.contacts.forEach {
                Log.d(TAG, it.toString())
            }
            contact.contacts
        }else{
            Log.d(TAG,"Not Found Contact in DB")
            mutableListOf()
        }
    }

    fun pushNewContactToDb(uid: String,contacts:MutableList<Model.Contact>){
        Paper.book().write(ROOT.CONTACTS,Model.ContactUser(contacts))
    }

    fun deleteContactInDb(uid:String){
        Paper.book().delete(ROOT.CONTACTS)

    }


}