package asunder.toche.sccmanagement.transactions.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.edittext.EdtMedium
import asunder.toche.sccmanagement.custom.extension.DisableClick
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.products.adapter.ProductAdapter
import asunder.toche.sccmanagement.products.viewmodel.ProductViewModel
import asunder.toche.sccmanagement.transactions.ActivityHistory
import asunder.toche.sccmanagement.transactions.TransactionListener
import asunder.toche.sccmanagement.transactions.TransactionState
import asunder.toche.sccmanagement.transactions.adapter.TransactionHistoryAdapter
import asunder.toche.sccmanagement.transactions.viewmodel.TransactionViewModel
import kotlinx.android.synthetic.main.fragment_transactions_history.*

/**
 *Created by ToCHe on 18/3/2018 AD.
 */
class TransactionHistoryFragment : Fragment(),TransactionListener {
    override fun onClickNote(message: String) {
    }

    private lateinit var historyAdapter: TransactionHistoryAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var sheetDisableCard: BottomSheetBehavior<View>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var transactionVM: TransactionViewModel
    private lateinit var productVM: ProductViewModel
    var filter:String? = null


    companion object {
        fun newInstance(productFilter:String): TransactionHistoryFragment{
            val fragment = TransactionHistoryFragment()
            val bundle = Bundle()
            bundle.putString(ROOT.PRODUCTS, productFilter)
            fragment.arguments = bundle
            return fragment
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionVM = ViewModelProviders.of(activity!!).get(TransactionViewModel::class.java)
        productVM = ViewModelProviders.of(activity!!).get(ProductViewModel::class.java)
        arguments?.getString(ROOT.PRODUCTS)?.let {
            filter = it
        }
        println("Transaction onCreate")
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_transactions_history,container,false)
        println("Transaction onCreateView")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("Transaction onViewCreated")
        setUpSelectProduct()
        productAdapter = ProductAdapter(mutableListOf(),false)
        historyAdapter = TransactionHistoryAdapter()
        val activity = activity as ActivityHistory
        filterTransaction(activity.getProductID())
    }

    fun setUpAdapter(mapTransaction:MutableMap<Model.Transaction,Model.Contact>,
                     transaction:MutableList<Model.Transaction>){
        historyAdapter = TransactionHistoryAdapter()
        rvTransavtionHistory.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = historyAdapter
        }
        historyAdapter.updateTransaction(mapTransaction,
                transaction,this)

    }

    fun setUpSelectProduct(){

        edtSelectProduct.DisableClick()
        edtSelectProduct.setOnClickListener {
            showSheetProduct()
        }

    }



    fun setUpAdapterProduct(){
        productAdapter = ProductAdapter(productVM.service.getProductsInDb(),false)
        productAdapter.setUpOnClickListener(object : ProductAdapter.ProductOnClickListener{
            override fun onClickProduct(product: Model.Product) {
                edtSelectProduct.setText(product.product_name.trim())
                Utils.findTransaction(product.id,object : Utils.OnFindTransactionsListener{
                    override fun onResults(results: MutableList<Model.Transaction>) {
                        val mapTransaction:MutableMap<Model.Transaction,Model.Contact> = mutableMapOf()
                        val contacts = transactionVM.getContact()
                        results.forEach {transac ->
                            val company = contacts.first { transac.company_id == it.id  }
                            mapTransaction[transac] = company
                        }
                        setUpAdapter(mapTransaction,results)
                    }
                },transactionVM.service.getTransactionInDb(),ROOT.PRODUCTS)


                bottomSheetDialog.dismiss()
            }
        })
    }

    fun showSheetProduct(){
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_company, null)
        val rvFilterCompany = bottomSheetView.findViewById<RecyclerView>(R.id.rvFilterCompany)
        val txtFilter = bottomSheetView.findViewById<EdtMedium>(R.id.txtCompanyFilter)
        val btnCancel = bottomSheetView.findViewById<BtnMedium>(R.id.btnCancel)
        bottomSheetDialog = BottomSheetDialog(context!!)
        bottomSheetDialog.setContentView(bottomSheetView)
        sheetDisableCard = BottomSheetBehavior.from(bottomSheetView.parent as View)
        if (sheetDisableCard.state != BottomSheetBehavior.STATE_EXPANDED) {
            setUpAdapterProduct()
            bottomSheetDialog.show()

        } else {
            bottomSheetDialog.dismiss()
        }
        rvFilterCompany.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = this@TransactionHistoryFragment.productAdapter
        }

        txtFilter.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Utils.findProduct(s.toString(),object : Utils.OnFindProductListener{
                    override fun onResults(results: MutableList<Model.Product>) {
                        productAdapter.updateProduct(results)
                    }
                },productVM.service.getProductsInDb())

            }
        })

        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

    }

    fun filterTransaction(id:String){
        if (id.isNotEmpty()) {
            Utils.findTransaction(id, object : Utils.OnFindTransactionsListener {
                override fun onResults(results: MutableList<Model.Transaction>) {
                    val mapTransaction: MutableMap<Model.Transaction, Model.Contact> = mutableMapOf()
                    val contacts = transactionVM.getContact()
                    results.forEach { transac ->
                        val company = contacts.first { transac.company_id == it.id }
                        mapTransaction[transac] = company
                    }
                    setUpAdapter(mapTransaction, results)
                }
            }, transactionVM.service.getTransactionInDb(), ROOT.PRODUCTS)

            val results = productVM.service.getProductsInDb().filter { id == it.id }
            if (results.isNotEmpty()) {
                edtSelectProduct.setText(results.first().product_name.trim())
            }
            /*
            Utils.findProduct(id, object : Utils.OnFindProductListener {
                override fun onResults(results: MutableList<Model.Product>) {

                    if (results.isNotEmpty()) {
                        edtSelectProduct.setText(results.first().product_name.trim())
                    }

                }
            }, productVM.service.getProductsInDb())
            */
        }
    }


    override fun onClickTransaction(transaction: Model.Transaction) {
        transactionVM.updateStateView(TransactionState.SHOWTRANSACTION)
        transactionVM.updateTransaction(transaction)
    }



}