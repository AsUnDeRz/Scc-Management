package asunder.toche.sccmanagement.preference

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.util.Log
import android.widget.Filter
import asunder.toche.sccmanagement.Model
import java.text.SimpleDateFormat
import java.util.*

/**
 *Created by ToCHe on 9/3/2018 AD.
 */
object Utils{

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

    fun getDateStringWithDate(date: Date) : String{
        val fmtOut = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("th","TH"))
        return fmtOut.format(date)
    }

    fun getCurrentDate():Date{
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)+543)
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
                                    it.mobile.contains(constraint.toString()) ||
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
                        masterData.filterTo(suggestionList) { it.issue_name.startsWith(constraint.toString()) }
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
                        it.product_name.contains(constraint.toString()) ||
                                it.product_desc.contains(constraint.toString())
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

    fun findTransaction(query: String, listener : OnFindTransactionsListener, masterData:MutableList<Model.Transaction>){
        object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val suggestionList = mutableListOf<Model.Transaction>()
                if(!(constraint == null || constraint.isEmpty())){
                    masterData.filterTo(suggestionList){
                        it.company_id.contains(constraint.toString()) ||
                        it.product_id.contains(constraint.toString())
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
                    listener.onResults(masterData)
                }
            }

        }.filter(query)
    }






}