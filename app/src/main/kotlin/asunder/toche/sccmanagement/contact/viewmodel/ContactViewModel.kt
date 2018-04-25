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
import java.io.File

/**
 *Created by ToCHe on 14/3/2018 AD.
 */
class ContactViewModel : ViewModel(),ContactService.ContactCallBack {


    val service = ContactService(this)
    val firebase = FirebaseManager()
    var isGranted = false
    private var pathPicture  =""
    private var contactId = ""
    var isSaveContactComplete  = MutableLiveData<ContactState>()
    val contacts : MutableLiveData<MutableList<Model.Contact>> = MutableLiveData()
    val contact : MutableLiveData<Model.Contact> = MutableLiveData()


    fun saveContact(data:Model.Contact){
        val fileName = Uri.fromFile(File(pathPicture))
        data.url_img_map = "${ROOT.IMAGES}/${Prefer.getUUID(firebase.context!!)}/${fileName.lastPathSegment}"
        data.path_img_map = pathPicture
        data.id = contactId
        if(pathPicture != ""){
            firebase.pushFileToFirebase(data.path_img_map,"")
        }
        if(contactId == "") {
                service.pushNewContact(data)
            }else{
                service.updateContact(data)
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



    override fun onSuccess() {
        updateViewState(ContactState.ALLCONTACT)
        loadContacts()

    }

    override fun onFail() {
        updateViewState(ContactState.ALLCONTACT)
        loadContacts()
    }



}