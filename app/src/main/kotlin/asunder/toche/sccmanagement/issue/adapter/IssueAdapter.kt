package asunder.toche.sccmanagement.issue.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ContactService
import asunder.toche.sccmanagement.service.IssueService
import asunder.toche.sccmanagement.transactions.IssueListener

/**
 *Created by ToCHe on 15/3/2018 AD.
 */
class IssueAdapter : RecyclerView.Adapter<IssueAdapter.IssueHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_issue,parent,false)
        return IssueHolder(view)

    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: IssueHolder, position: Int) {
    }


    class IssueHolder(itemView: View) : RecyclerView.ViewHolder(itemView),ContactService.ContactCallBack {


        val txtIssue :TxtMedium = itemView.findViewById(R.id.txtIssue)
        val txtCompany:TxtMedium = itemView.findViewById(R.id.txtCompany)

        fun bind(issue:Model.Issue,listener: IssueListener){
            txtIssue.text = issue.issue_name
            Utils.findCompany(issue.company_id,object : Utils.OnFindCompanyListener{
                override fun onResults(results: MutableList<Model.Contact>) {
                    if(results.size > 0) {
                        txtCompany.text = results[0].company
                    }
                }
            },ContactService(this).getContactInDb())

            itemView.setOnClickListener {
                listener.onClickIssue(issue)
            }
        }
        override fun onSuccess() {}

        override fun onFail() {}

    }

}