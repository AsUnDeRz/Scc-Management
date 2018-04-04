package asunder.toche.sccmanagement.settings

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import asunder.toche.sccmanagement.R
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_settings.*
import android.provider.ContactsContract
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.service.ContactService
import com.onegravity.contactpicker.contact.Contact
import com.onegravity.contactpicker.contact.ContactDescription
import com.onegravity.contactpicker.contact.ContactSortOrder
import com.onegravity.contactpicker.core.ContactPickerActivity
import com.onegravity.contactpicker.group.Group
import com.onegravity.contactpicker.picture.ContactPictureType
import java.io.IOException


/**
 *Created by ToCHe on 11/3/2018 AD.
 */
class ActivitySetting: AppCompatActivity(),ContactService.ContactCallBack{


    private val TAG = this::class.java.simpleName
    lateinit var service:ContactService
    val uid = "155434134123"
    private var contactUser : MutableList<Model.Contact> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        checkPermission()
        service = ContactService(this)
        contactUser = service.getContactInDb()

    }

    private fun checkPermission(){
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE)
                .withListener(object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.isAnyPermissionPermanentlyDenied){
                            //Snackbar.make(rootSettings,"PermissionDenied",Snackbar.LENGTH_SHORT).show()
                        }else{
                            //Snackbar.make(rootSettings,"PermissionGranted",Snackbar.LENGTH_SHORT).show()
                            setUpOnClickListener()
                        }
                    }
                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?,
                                                                    token: PermissionToken?) {
                    }
                })
                .check()
    }

    fun setUpOnClickListener(){
        btnSync.setOnClickListener {
            contactUser = service.getContactInDb()
            contactUser.forEach {
                Log.d(TAG,"Contact in DB $it")
            }

            val intent = Intent().setClass(this, ContactPickerActivity::class.java)
            .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name)
            .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, true)
            .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.PHONE.name)
            .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .putExtra(ContactPickerActivity.EXTRA_ONLY_CONTACTS_WITH_PHONE,true)
            .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name)
        startActivityForResult(intent, KEY.OPEN_CONTACT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode ==KEY.OPEN_CONTACT && resultCode == Activity.RESULT_OK &&
        data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {
            val contact :MutableList<Model.Contact> = mutableListOf()
            val result = data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA) as List<Contact>
            result.mapTo(contact) {
                val numbers = mutableListOf<Model.Number>()
               numbers.add(Model.Number(it.getPhone(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE), ROOT.Mobile))
                Model.Contact("","","", it.displayName,numbers
                        , mutableListOf(), mutableListOf(),"","", mutableListOf(),"","",
                        "","")
            }

            val groups = data.getSerializableExtra(ContactPickerActivity.RESULT_GROUP_DATA) as List<Contact>
            for ( group in groups) {

            }

            service.pushNewContactToDb(service.syncContact(contact,service.getContactInDb()))

        }
    }


    override fun onSuccess() {
    }

    override fun onFail() {
    }

}