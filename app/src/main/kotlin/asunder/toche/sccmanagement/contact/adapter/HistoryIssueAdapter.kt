package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class HistoryIssueAdapter(var issues:MutableList<Model.Issue>): RecyclerView.Adapter<HistoryIssueHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryIssueHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_issue
                ,parent,false)
        return HistoryIssueHolder(view)
    }

    override fun getItemCount(): Int {
        return issues.size
    }

    override fun onBindViewHolder(holder: HistoryIssueHolder, position: Int) {
        holder.bind(issues[position])
    }

}