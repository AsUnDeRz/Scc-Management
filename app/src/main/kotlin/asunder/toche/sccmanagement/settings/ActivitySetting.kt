package asunder.toche.sccmanagement.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.main.ActivitySelectExport
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ContactService
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.folderselector.FileChooserDialog
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.onegravity.contactpicker.contact.Contact
import com.onegravity.contactpicker.contact.ContactDescription
import com.onegravity.contactpicker.contact.ContactSortOrder
import com.onegravity.contactpicker.core.ContactPickerActivity
import com.onegravity.contactpicker.picture.ContactPictureType
import kotlinx.android.synthetic.main.activity_settings.*
import org.zeroturnaround.zip.ZipUtil
import java.io.File


/**
 *Created by ToCHe on 11/3/2018 AD.
 */
class ActivitySetting: AppCompatActivity(),
        ContactService.ContactCallBack,
        FolderChooserDialog.FolderCallback,
        FileChooserDialog.FileCallback{


    override fun onDeleteSuccess() {
    }


    private val TAG = this::class.java.simpleName
    lateinit var service: ContactService
    val uid = "155434134123"
    private var contactUser: MutableList<Model.Contact> = mutableListOf()
    private lateinit var loading: MaterialDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        checkPermission()
        service = ContactService(this)
        contactUser = service.getContactInDb()



        Utils.exportDB(this)


    }

    private fun checkPermission() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.isAnyPermissionPermanentlyDenied) {
                            //Snackbar.make(rootSettings,"PermissionDenied",Snackbar.LENGTH_SHORT).show()
                        } else {
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

    fun setUpOnClickListener() {
        btnSync.setOnClickListener {
            contactUser = service.getContactInDb()
            contactUser.forEach {
                Log.d(TAG, "Contact in DB $it")
            }

            val intent = Intent().setClass(this, ContactPickerActivity::class.java)
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name)
                    .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, true)
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.PHONE.name)
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .putExtra(ContactPickerActivity.EXTRA_ONLY_CONTACTS_WITH_PHONE, true)
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name)
            startActivityForResult(intent, KEY.OPEN_CONTACT)
        }

        btnExportData.setOnClickListener {
            showSelecterFolder()

        }

        btnExportContact.setOnClickListener {
            //showSelecterFolder()
            val intent = Intent()
            intent.putExtra(ROOT.EXPORT,ROOT.CONTACTS)
            startActivity(intent.setClass(this,ActivitySelectExport::class.java))
        }

        btnExportProduct.setOnClickListener {
            val intent = Intent()
            intent.putExtra(ROOT.EXPORT,ROOT.PRODUCTS)
            startActivity(intent.setClass(this,ActivitySelectExport::class.java))
        }

        btnImportData.setOnClickListener {
            showSelecterFile()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY.OPEN_CONTACT && resultCode == Activity.RESULT_OK &&
                data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {
            val contact: MutableList<Model.Contact> = mutableListOf()
            val result = data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA) as List<Contact>
            result.mapTo(contact) {
                val numbers = mutableListOf<Model.Channel>()
                numbers.add(Model.Channel(it.getPhone(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE), ROOT.Mobile))
                Model.Contact("", "", "", it.displayName, numbers
                        , mutableListOf(), mutableListOf(), mutableListOf())
            }

            val groups = data.getSerializableExtra(ContactPickerActivity.RESULT_GROUP_DATA) as List<Contact>
            for (group in groups) {

            }

            service.pushNewContactToDb(service.syncContact(contact, service.getContactInDb()))

        }
    }


    override fun onSuccess() {
    }

    override fun onFail() {
    }

    fun showDialog(msg: String) {
        loading = MaterialDialog.Builder(this)
                .title("แจ้งเตือน")
                .content(msg)
                .progress(true, 0)
                .typeface(Utils.getTypeFaceMedium(this), Utils.getTypeFaceMedium(this))
                .cancelable(false)
                .build()
        loading.show()
    }

    fun hideDialog() {
        loading.dismiss()
    }

    fun showSelecterFolder() {
        FolderChooserDialog.Builder(this)
                .cancelButton(R.string.cancel)
                .chooseButton(R.string.select_folder)  // changes label of the choose button
                .initialPath(Environment.getExternalStorageDirectory().path)  // changes initial path, defaults to external storage directory
                .tag("optional-identifier")
                .allowNewFolder(true, R.string.newfolder)
                .show(this)
    }

    override fun onFolderSelection(dialog: FolderChooserDialog, folder: File) {
        showDialog("กำลังส่งออกไฟล์สำรองข้อมูล\nกรุณารอสักครู่")
        val tag = dialog.tag// gets tag set from Builder, if you use multiple dialogs
        if (tag != null){
            ZipUtil.pack(File(Utils.getPath(this)),
                    File("${folder.path}/backup${Utils.getCurrentDateForBackupFile()}.zip"))
        }
        Handler().postDelayed({
            hideDialog()
        },2000)
    }

    override fun onFolderChooserDismissed(dialog: FolderChooserDialog) {
    }

    fun showSelecterFile() {
        FileChooserDialog.Builder(this)
                .cancelButton(R.string.cancel)
                .initialPath(Environment.getExternalStorageDirectory().path)
                //.mimeType()
                .extensionsFilter(".zip")
                .tag("file-selecter")
                .goUpLabel("ย้อนกลับ")
                .show(this)

    }

    override fun onFileSelection(dialog: FileChooserDialog, file: File) {
        showDialog("กำลังนำเข้าไฟล์สำรองข้อมูล\nกรุณารอสักครู่")
        val externalPath = Environment.getExternalStorageDirectory().absolutePath
        val newDir = externalPath + File.separator + Prefer.getUUID(this)
        ZipUtil.unpack(file, File(newDir))
        Utils.importDB(this)
        Handler().postDelayed({
            hideDialog()
        },2000)

    }

    override fun onFileChooserDismissed(dialog: FileChooserDialog) {
    }
}