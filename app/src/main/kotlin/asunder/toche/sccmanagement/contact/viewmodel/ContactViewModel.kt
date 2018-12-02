package asunder.toche.sccmanagement.contact.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.contact.ContactState
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.service.ContactService
import asunder.toche.sccmanagement.service.FirebaseManager
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.util.*
import kotlin.properties.Delegates

/**
 *Created by ToCHe on 14/3/2018 AD.
 */
class ContactViewModel : ViewModel(),ContactService.ContactCallBack {


    val service = ContactService(this)
    val firebase = FirebaseManager()
    var isGranted = false
    private var pathPicture  =""
    private var contactId by Delegates.observable("") { _, oldValue, newValue ->
        println("Oldvalues = $oldValue \nNewValue = $newValue")
    }
    var isSaveContactComplete : MutableLiveData<ContactState> by Delegates.observable(MutableLiveData()){
        _, oldValue, newValue ->
        println("State ${oldValue.value} to  ${newValue.value}")
    }
    val contacts : MutableLiveData<MutableList<Model.Contact>> = MutableLiveData()
    val contact : MutableLiveData<Model.Contact> = MutableLiveData()
    val address : MutableLiveData<Model.Address> = MutableLiveData()
    val base64 : MutableLiveData<String> = MutableLiveData()



    fun saveContact(data:Model.Contact) = runBlocking{
        try {
            val job = async {
                data.addresses.forEach{
                    val fileName = Uri.fromFile(File(it.path_img_map))
                    //it.url_img_map = "${ROOT.IMAGES}/${Prefer.getUUID(firebase.context!!)}/${fileName.lastPathSegment}"
                    if(it.path_img_map.isNotEmpty()){
                        firebase.pushFileToFirebase(it.path_img_map,"")
                        it.path_img_map = firebase.getPathClone(it.path_img_map)
                    }
                }
            }
            job.await()
            data.id = contactId
            if (isSaveContactComplete.value != ContactState.SAVED) {
                if (contactId == "") {
                    val randomID = UUID.randomUUID().toString()
                    data.id = randomID
                    contactId = randomID
                    service.pushNewContact(data)
                } else {
                    service.updateContact(data)
                }
            }
        }
        catch (e: Exception) {
        }
        finally {

        }
    }

    fun deleteContact(data: Model.Contact){
        service.deleteContact(data)
        service.deleteContactInDb(data.id)

    }

    fun deleteContact(){
        contact.value?.let {
            service.deleteContact(it)
            service.deleteContactInDb(it.id)
            it.addresses.forEach {
                //firebase.deleteFile(it.base64_img_map,it.path_img_map)
            }
        }

    }


    fun loadContacts(){
        val data = service.getContactInDb()
        updateContacts(data)
    }

    fun updateContacts(data : MutableList<Model.Contact>){
        contacts.value = data
    }

    fun updateGranted(update : Boolean){
        isGranted = update
    }

    fun updatePathPicture(path:String){
        pathPicture = path
    }

    fun updateContactId(id:String){
        contactId = id
    }

    fun updateViewState(viewState: ContactState){
        isSaveContactComplete.value = viewState
    }

    fun updateContact(data : Model.Contact){
        contact.value = data
    }

    fun getContact():Model.Contact{
        return contact.value!!
    }

    fun updateAddress(data:Model.Address){
        address.value =  data
    }

    fun updateBase64(data:String){
        base64.value = data
    }


    override fun onSuccess() {
        updateViewState(ContactState.SAVED)
        loadContacts()
        val result = contacts.value?.find {
            it.id == contactId
        }
        result?.let {
            updateContact(it)
        }

    }

    override fun onFail() {
        updateViewState(ContactState.SAVED)
        loadContacts()
    }

    override fun onDeleteSuccess() {
        updateViewState(ContactState.ALLCONTACT)
        loadContacts()

    }


}