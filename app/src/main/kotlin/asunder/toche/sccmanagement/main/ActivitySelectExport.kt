package asunder.toche.sccmanagement.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.multiselect.ComponentMultiSelect
import asunder.toche.sccmanagement.preference.ROOT
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_select_export.*

/**
 *Created by ToCHe on 14/8/2018 AD.
 */
class ActivitySelectExport : AppCompatActivity(),ComponentMultiSelect {

    lateinit var selectDataAdapter: SelectDataAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_export)
        if (intent.hasExtra(ROOT.EXPORT)){
            if (intent.getStringExtra(ROOT.EXPORT) == ROOT.CONTACTS){
                initData(isContact = true)
            }else{
                initData(isContact = false)
                headerProduct.visibility = View.VISIBLE
            }
        }
        btnBack.setOnClickListener {
            finish()
        }
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
    }

}