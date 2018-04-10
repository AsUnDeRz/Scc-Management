package asunder.toche.sccmanagement.preference

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.util.Log
import android.widget.Filter
import asunder.toche.sccmanagement.Model
import java.text.SimpleDateFormat
import java.util.*
import android.content.ContextWrapper
import android.app.Activity
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


/**
 *Created by ToCHe on 9/3/2018 AD.
 */
object Utils{

    fun scanForActivity(cont: Context?): Activity? {
        return when (cont) {
            null -> null
            is Activity -> cont
            is ContextWrapper -> scanForActivity(cont.baseContext)
            else -> null
        }
    }

    fun getCurrentDateShort() :String{
        val fmtOut = SimpleDateFormat("dd/MM/yyyy", Locale("th","TH"))
        return fmtOut.format(getCurrentDate())
    }

    fun getCurrentDateString() : String{
            val fmtOut = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("th","TH"))
            return fmtOut.format(getCurrentDate())
    }

    fun getDateWithString(date :String) : Date{
        val fmtOut = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("th","TH"))
        return fmtOut.parse(date)
    }
    fun getDateString(date :String) : Date{
        val fmtOut = SimpleDateFormat("dd/MM/yyyy", Locale("th","TH"))
        return fmtOut.parse(date)
    }

    fun getPreviusDate() :Date{
        val calendar = Calendar.getInstance()
        calendar.time = getCurrentDate()
        calendar.add(Calendar.DATE,-1)
        return calendar.time

    }

    fun getDateStringWithDate(date: Date) : String{
        val fmtOut = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("th","TH"))
        return fmtOut.format(date)
    }

    fun getCurrentDate():Date{
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)+543)
        calendar.set(calendar[Calendar.YEAR],calendar[Calendar.MONTH],calendar[Calendar.DAY_OF_MONTH],0,0)
        return calendar.time
    }

    fun getTypeFaceMedium(context : Context):Typeface {
        return Typeface.createFromAsset(context.assets, "fonts/set_medium.ttf")
    }

    interface OnFindCompanyListener {
        fun onResults(results: MutableList<Model.Contact>)
    }
    interface OnFindIssueListener {
        fun onResults(results: MutableList<Model.Issue>)
    }
    interface OnFindProductListener{
        fun onResults(results: MutableList<Model.Product>)
    }
    interface OnFindTransactionsListener{
        fun onResults(results: MutableList<Model.Transaction>)
    }


    fun findCompany(query: String, listener: OnFindCompanyListener, masterData:MutableList<Model.Contact>) {
            object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val suggestionList = mutableListOf<Model.Contact>()
                    if (!(constraint == null || constraint.isEmpty())) {
                        masterData.filterTo(suggestionList) {
                            it.company.contains(constraint.toString()) ||
                                    it.contact_name.contains(constraint.toString()) ||
                                    it.numbers.any { it.number.contains(constraint.toString()) } ||
                                    it.id.contains(constraint.toString())
                        }
                    }
                    val results = FilterResults()
                    results.values = suggestionList
                    results.count = suggestionList.size
                    return results
                }

                override fun publishResults(constraint: CharSequence, results: FilterResults) {
                    if (results.count != 0) {
                        listener.onResults(results.values as MutableList<Model.Contact>)
                    }else{
                        listener.onResults(masterData)
                    }
                }
            }.filter(query)

    }

    fun findIssue(query: String, listener: OnFindIssueListener,masterData:MutableList<Model.Issue>) {
            object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val suggestionList = mutableListOf<Model.Issue>()
                    if (!(constraint == null || constraint.isEmpty())) {
                        masterData.filterTo(suggestionList) {
                            it.issue_name.contains(constraint.toString()) ||
                                    it.issue_name.contains(constraint.toString()) ||
                                            it.date.contains(constraint.toString())
                        }
                    }
                    val results = FilterResults()
                    results.values = suggestionList
                    results.count = suggestionList.size
                    return results
                }

                override fun publishResults(constraint: CharSequence, results: FilterResults) {
                    if (results.count != 0) {
                        listener.onResults(results.values as MutableList<Model.Issue>)
                    }else{
                        listener.onResults(masterData)
                    }
                }
            }.filter(query)
    }
    fun findProduct(query: String, listener: OnFindProductListener,masterData:MutableList<Model.Product>){
        object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val suggestionList = mutableListOf<Model.Product>()
                if(!(constraint == null || constraint.isEmpty())){
                    masterData.filterTo(suggestionList){
                        it.product_name.contains(constraint.toString(),true) ||
                                it.product_desc.contains(constraint.toString(),true)
                    }
                }
                val result = FilterResults()
                result.values = suggestionList
                result.count = suggestionList.size
                return result
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.count != 0){
                    listener.onResults(results?.values as MutableList<Model.Product>)
                }else{
                    listener.onResults(masterData)
                }
            }
        }.filter(query)
    }

    fun findTransaction(query: String, listener : OnFindTransactionsListener,
                        masterData:MutableList<Model.Transaction>,optional:String?){
        object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val suggestionList = mutableListOf<Model.Transaction>()
                if(!(constraint == null || constraint.isEmpty())){
                    when(optional){
                        ROOT.PRODUCTS ->{
                            masterData.filterTo(suggestionList){
                                it.product_id == constraint.toString()
                            }
                        }
                        ROOT.CONTACTS ->{
                            masterData.filterTo(suggestionList){
                                it.company_id == constraint.toString()
                            }
                        }
                        else ->{
                            /*
                           masterData.filterTo(suggestionList) {
                                        it.company_name.contains(constraint.toString(), true) ||
                                                it.product_name.contains(constraint.toString(), true)
                           }
                           */
                            val sectionAll = masterData.filter {
                                it.company_name.contains(constraint.toString(), true) ||
                                        it.product_name.contains(constraint.toString(), true)
                            }
                            val contacts = sectionAll.filter {
                                it.company_name.contains(constraint.toString(), true)
                            }
                            val products = sectionAll.filter {
                                it.product_name.contains(constraint.toString(), true)
                            }

                            when {
                                contacts.size == products.size -> {
                                    suggestionList.addAll(sectionAll)
                                }
                                contacts.size > products.size -> {
                                    suggestionList.addAll(contacts)
                                }
                                else -> {
                                    suggestionList.addAll(products)
                                }
                            }


                        }
                    }

                }
                val result = FilterResults()
                result.values = suggestionList
                result.count = suggestionList.size
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.count != 0){
                    listener.onResults(results?.values as MutableList<Model.Transaction>)
                }else{
                    when(optional){
                        ROOT.PRODUCTS ->{
                            listener.onResults(mutableListOf())
                        }
                        else ->{
                            listener.onResults(masterData)
                        }
                    }
                }
            }

        }.filter(query)
    }






}