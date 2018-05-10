package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.transactions.IssueListener

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class HistoryIssueAdapter(var issues:MutableList<Model.Issue>,var listener : IssueListener?): RecyclerView.Adapter<HistoryIssueHolder>() {

    fun updateIssues(newIssues : MutableList<Model.Issue>,newListener: IssueListener){
        issues = newIssues
        listener = newListener
        notifyDataSetChanged()
    }
    fun updateIssues(newIssues : MutableList<Model.Issue>){
        issues = newIssues
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryIssueHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_issue
                ,parent,false)
        return HistoryIssueHolder(view)
    }

    override fun getItemCount(): Int {
        return issues.size
    }

    override fun onBindViewHolder(holder: HistoryIssueHolder, position: Int) {
        if (listener != null) {
            holder.bind(issues[position], listener!!)
        }else{
            holder.bind(issues[position])
        }
    }

}