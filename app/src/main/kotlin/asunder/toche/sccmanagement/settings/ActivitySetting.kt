package asunder.toche.sccmanagement.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.ContactsContract
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.support.v4.content.FileProvider.getUriForFile
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.MimeTypeMap
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.main.ActivitySelectExport
import asunder.toche.sccmanagement.preference.KEY
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ContactService
import au.com.jtribe.shelly.Shelly
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
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.lang.Exception
import java.net.URLConnection


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
    val IMPORTALL = "import_all"
    val IMPORTCONTACT = "import_contact"
    val IMPORTPRODUCT = "import_product"

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
            showSelecterFile(IMPORTALL)
        }

        btnImportContact.setOnClickListener {
            showSelecterFile(IMPORTCONTACT)
        }
        btnImportProduct.setOnClickListener {
            showSelecterFile(IMPORTPRODUCT)
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

        val initFolder = Prefer.getLastFolderType(this,ROOT.LASTFOLDERALL)
        FolderChooserDialog.Builder(this)
                .cancelButton(R.string.cancel)
                .chooseButton(R.string.select_folder)  // changes label of the choose button
                .initialPath(initFolder)  // changes initial path, defaults to external storage directory
                .tag("optional-identifier")
                .allowNewFolder(true, R.string.newfolder)
                .show(this)
    }

    override fun onFolderSelection(dialog: FolderChooserDialog, folder: File) {
        runOnUiThread {
            showDialog("กำลังส่งออกไฟล์สำรองข้อมูล\nกรุณารอสักครู่")
        }
        val tag = dialog.tag// gets tag set from Builder, if you use multiple dialogs
        val resultPath = File("${folder.path}/backup${Utils.getCurrentDateForBackupFile()}.zip")
        Prefer.saveLastFolderType(this,ROOT.LASTFOLDERALL,folder.absolutePath)
        GlobalScope.launch(Dispatchers.IO){
            val result = GlobalScope.launch(Dispatchers.Default) {
                if (tag != null){
                    ZipUtil.pack(File(Utils.getPath(this@ActivitySetting)),resultPath)
                }
            }
            result.join()
            runOnUiThread {
                hideDialog()
            }
        }
    }

    override fun onFolderChooserDismissed(dialog: FolderChooserDialog) {
    }

    fun showSelecterFile(tag:String) {
        FileChooserDialog.Builder(this)
                .cancelButton(R.string.cancel)
                .initialPath(Environment.getExternalStorageDirectory().absolutePath)
                //.mimeType()
                .extensionsFilter(".zip")
                .tag(tag)
                .goUpLabel("ย้อนกลับ")
                .show(this)



    }

    override fun onFileSelection(dialog: FileChooserDialog, file: File) {
        val stor = Storage(applicationContext)
        runOnUiThread {
            showDialog("กำลังนำเข้าไฟล์สำรองข้อมูล\nกรุณารอสักครู่")
        }
        GlobalScope.launch(Dispatchers.IO){
            val result = GlobalScope.launch(Dispatchers.Default){
                when(dialog.tag){
                    IMPORTALL ->{
                        ZipUtil.unpack(file, File(Utils.getPath(this@ActivitySetting)))
                        Utils.importDB(this@ActivitySetting)
                    }
                    IMPORTCONTACT ->{
                        val newDir = Utils.getPath(this@ActivitySetting) +File.separator+"ContactExport"
                        stor.createDirectory(newDir)
                        ZipUtil.unpack(file, File(newDir))
                        Utils.importContact(this@ActivitySetting)
                    }
                    IMPORTPRODUCT ->{
                        val newDir = Utils.getPath(this@ActivitySetting) +File.separator+"ProductExport"
                        stor.createDirectory(newDir)
                        ZipUtil.unpack(file, File(newDir))
                        Utils.importProduct(this@ActivitySetting)
                    }
                }
            }
            result.join()
            runOnUiThread {
                hideDialog()
            }
        }
    }

    override fun onFileChooserDismissed(dialog: FileChooserDialog) {
    }

    /* Checks if external storage is available for read and write */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /* Checks if external storage is available to at least read */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    override fun onResume() {
        super.onResume()
        val stor = Storage(this)
        val newDir = Utils.getPath(this) +File.separator+"ContactExport"
        val productDir = Utils.getPath(this) +File.separator+"ProductExport"

        try {
            if (File(newDir).delete()){
                println("Delete Contact Export Folder Success")
            }
            if(stor.deleteDirectory(newDir)){
                println("Delete Contact Export Folder Success")
            }
            if (File(productDir).delete()){
                println("Delete Product Export Folder Success")
            }
            if(stor.deleteDirectory(productDir)){
                println("Delete Product Export Folder Success")
            }
        }catch (e: Exception){
            println(e.message)
        }
    }

}