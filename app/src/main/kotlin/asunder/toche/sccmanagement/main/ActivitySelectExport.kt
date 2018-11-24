package asunder.toche.sccmanagement.main

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.multiselect.ComponentMultiSelect
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ContactService
import asunder.toche.sccmanagement.service.ProductService
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.folderselector.FileChooserDialog
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog
import com.bumptech.glide.Glide
import com.thefinestartist.utils.preferences.Pref
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_select_export.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import org.zeroturnaround.zip.ZipUtil
import java.io.File

/**
 *Created by ToCHe on 14/8/2018 AD.
 */
class ActivitySelectExport :
        AppCompatActivity(),
        ComponentMultiSelect,
        FolderChooserDialog.FolderCallback,
        FileChooserDialog.FileCallback{

    lateinit var selectDataAdapter: SelectDataAdapter
    private lateinit var loading: MaterialDialog
    var isExpoertContact :Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_export)
        if (intent.hasExtra(ROOT.EXPORT)){
            if (intent.getStringExtra(ROOT.EXPORT) == ROOT.CONTACTS){
                initData(isContact = true)
                isExpoertContact = true
            }else{
                initData(isContact = false)
                headerProduct.visibility = View.VISIBLE
                isExpoertContact = false
            }
        }
        btnBack.setOnClickListener {
            finish()
        }
        searchTextChanged()
    }






    fun initData(isContact:Boolean){
        selectDataAdapter = SelectDataAdapter(this)
        if (isContact) {
            val contacts = Paper.book().read<Model.ContactUser>(ROOT.CONTACTS)
            contacts?.let {
                selectDataAdapter.isContact = true
                selectDataAdapter.updateContactData(it.contacts)
            }
        }else{
            val product = Paper.book().read<Model.ProductUser>(ROOT.PRODUCTS)
            product?.let {
                selectDataAdapter.isContact = false
                selectDataAdapter.updateProductData(it.products)
            }
        }


        rvDataList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@ActivitySelectExport)
            adapter = selectDataAdapter
        }
    }



    override fun onUserEnableMultiSelect() {
        root_component_menu.visibility = View.VISIBLE
        updateTxtSelect()
        btnHideAction.setOnClickListener {
            clearSelected()
            root_component_menu.visibility = View.GONE
        }
    }

    override fun onComponentSelected() {
        updateTxtSelect()
    }

    override fun onComponentClicked(any: Any) {
    }

    fun clearSelected(){
        selectDataAdapter.mMultiSelector.clearSelections()
        selectDataAdapter.mMultiSelector.isSelectable = false
    }

    fun updateTxtSelect(){
        txtSelectSize.text = "Selected ${selectDataAdapter.mMultiSelector.selectedPositions.size}"
        if (selectDataAdapter.mMultiSelector.selectedPositions.size == selectDataAdapter.getSizeData()){
            btnSelectAll.text = "ยกเลิกทั้งหมด"
            btnSelectAll.setOnClickListener {
                selectDataAdapter.mMultiSelector.clearSelections()
                updateTxtSelect()
            }
        }else{
            btnSelectAll.text = "เลือกทั้งหมด"
            btnSelectAll.setOnClickListener {
                for (i in 0 until selectDataAdapter.getSizeData()){
                    selectDataAdapter.mMultiSelector.setSelected(i,123,true)
                }
                updateTxtSelect()
            }
        }

        if (selectDataAdapter.mMultiSelector.selectedPositions.isNotEmpty()){
            btnConfirm.setOnClickListener {
                showSelecterFolder()
            }
        }

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
        val initPath = if (isExpoertContact){
            Prefer.getLastFolderType(this,ROOT.LASTFOLDERCONTACT)
        }else{
            Prefer.getLastFolderType(this,ROOT.LASTFOLDERPRODUCT)
        }

        FolderChooserDialog.Builder(this)
                .cancelButton(R.string.cancel)
                .chooseButton(R.string.select_folder)  // changes label of the choose button
                .initialPath(initPath)  // changes initial path, defaults to external storage directory
                .tag("optional-identifier")
                .allowNewFolder(true, R.string.newfolder)
                .show(this)
    }

    override fun onFolderSelection(dialog: FolderChooserDialog, folder: File) {
        showDialog("กำลังส่งออกไฟล์สำรองข้อมูล\nกรุณารอสักครู่")
        GlobalScope.launch(Dispatchers.IO){
            val result = GlobalScope.launch(Dispatchers.Default) {
                val tag = dialog.tag// gets tag set from Builder, if you use multiple dialogs
                if (tag != null){
                    if (isExpoertContact){
                        if (selectDataAdapter.contacts.isNotEmpty()) {
                            Utils.exportContact(this@ActivitySelectExport,
                                    selectDataAdapter.getContactSelected(),folder)
                            Prefer.saveLastFolderType(this@ActivitySelectExport,
                                    ROOT.LASTFOLDERCONTACT,folder.absolutePath)
                        }
                    }else{
                        if (selectDataAdapter.products.isNotEmpty()){
                            Utils.exportProduct(this@ActivitySelectExport,
                                    selectDataAdapter.getProductSelected(),folder)
                            Prefer.saveLastFolderType(this@ActivitySelectExport,
                                    ROOT.LASTFOLDERPRODUCT,folder.absolutePath)
                        }
                    }
                }
            }
            result.join()
            runOnUiThread {
                hideDialog()
                clearSelected()
                root_component_menu.visibility = View.GONE
            }
        }
    }

    override fun onFolderChooserDismissed(dialog: FolderChooserDialog) {
    }

    override fun onFileSelection(dialog: FileChooserDialog, file: File) {
    }

    override fun onFileChooserDismissed(dialog: FileChooserDialog) {
    }



    fun searchTextChanged(){
        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isExpoertContact){
                    filterContact(s.toString())

                }else{
                    filterProduct(s.toString())
                }

                //change icon search
                if (s.isNullOrEmpty()){
                    Glide.with(this@ActivitySelectExport)
                            .load(R.drawable.ic_search_black_36dp)
                            .into(imgSearch)
                }else{
                    Glide.with(this@ActivitySelectExport)
                            .load(R.drawable.ic_close_black_24dp)
                            .into(imgSearch)
                    imgSearch.setOnClickListener {
                        txtSearch.text.clear()
                    }
                }
            }
        })
    }


    fun filterContact(query:String){
        Utils.findCompany(query,object :Utils.OnFindCompanyListener{
            override fun onResults(results: MutableList<Model.Contact>) {
                selectDataAdapter.updateContactData(results)
            }
        },ContactService(object : ContactService.ContactCallBack{
            override fun onSuccess() {

            }

            override fun onFail() {
            }

            override fun onDeleteSuccess() {
            }
        }).getContactInDb())
    }

    fun filterProduct(query:String){
        Utils.findProduct(query,object : Utils.OnFindProductListener{
            override fun onResults(results: MutableList<Model.Product>) {
                selectDataAdapter.updateProductData(results)
            }
        },ProductService(object : ProductService.ProductCallback{
            override fun onSuccess() {

            }

            override fun onFail() {
            }
        }).getProductsInDb())
    }






}