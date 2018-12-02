package asunder.toche.sccmanagement.service

import android.content.Context
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import com.crashlytics.android.Crashlytics
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.paperdb.Paper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.io.File
import java.util.*

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class ContactService(var listener:ContactCallBack){

    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName
    private val context = FirebaseApp.getInstance()?.applicationContext
    interface ContactCallBack{
        fun onSuccess()
        fun onFail()
        fun onDeleteSuccess()
    }

    fun pushNewContact(contact: Model.Contact)  {
        /*
        val keyAuth = firebase.child("${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.CONTACTS}").push().key
        contact.id = keyAuth!!
        Log.d(TAG,"PushNewContact with $contact")
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.CONTACTS}/$keyAuth"] = contact
        firebase.updateChildren(childUpdates) { databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Contact,Managemnt saved successfully.")
            }
        }
        */
        pushNewContactToDb(updateContactFromDb(contact,getContactInDb()))
    }

    fun updateContact(contact: Model.Contact){
        /*
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.CONTACTS}/${contact.id}"] = contact
        Log.d(TAG,"UpdateContact with $contact")
        firebase.updateChildren(childUpdates) { databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
                listener.onFail()
            } else {
                System.out.println("Data Update Contact successfully.")
                listener.onSuccess()
            }
        }
        */
        pushNewContactToDb(updateContactFromDb(contact,getContactInDb()))

    }

    fun deleteContact(contact : Model.Contact){

        contact.addresses.forEach {
            File(it.path_img_map).delete()
        }
        /*
        firebase.child("${ROOT.USERS}/${Prefer.getUUID(context!!)}/${ROOT.CONTACTS}/${contact.id}").removeValue { databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
                listener.onFail()
            } else {
                System.out.println("Data deleted successfully.")
                listener.onDeleteSuccess()
            }
        }
        */
    }

    fun getContactInDb() : MutableList<Model.Contact>{
        val contact = Paper.book().read<Model.ContactUser>(ROOT.CONTACTS)
        return if(contact != null) {
            contact.contacts
        }else{
            Log.d(TAG,"Not Found Contact in DB")
            mutableListOf()
        }
    }

    fun pushNewContactToDb(contacts:MutableList<Model.Contact>) = async(UI) {
        try {
            val addContact = async {
                Paper.book().write(ROOT.CONTACTS,Model.ContactUser(contacts))
            }

            addContact.await()
            listener.onSuccess()

        }
        catch (e: Exception) {
        }
        finally {
        }
    }

    fun deleteContactInDb(uid:String){
        val currentContact = getContactInDb()
        currentContact
                .filter { it.id == uid }
                .forEach {
                    currentContact.remove(it)
                }
        async(UI) {
            try {
                val addContact = async {
                    Paper.book().write(ROOT.CONTACTS,Model.ContactUser(currentContact))
                }
                addContact.await()
                listener.onDeleteSuccess()
            }
            catch (e: Exception) {
            }
            finally {
            }
        }
    }

    fun syncContact(contactFromPhone:MutableList<Model.Contact>,contactFromDb:MutableList<Model.Contact>)
            : MutableList<Model.Contact>{
        val rawData = contactFromDb
        val newData = contactFromPhone

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

    fun updateContactFromDb(contact:Model.Contact,contactFromDb: MutableList<Model.Contact>)
    : MutableList<Model.Contact>{
        contactFromDb.filter { it.id == contact.id }
                .forEach {
                    contactFromDb.remove(it)
                }
        contactFromDb.add(contact)
        return contactFromDb
    }


}