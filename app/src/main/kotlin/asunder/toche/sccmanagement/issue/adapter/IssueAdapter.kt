package asunder.toche.sccmanagement.issue.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.preference.Utils
import asunder.toche.sccmanagement.service.ContactService
import asunder.toche.sccmanagement.transactions.IssueListener
import kotlinx.android.synthetic.main.item_issue.view.*
import me.thanel.swipeactionview.SwipeActionView
import me.thanel.swipeactionview.SwipeGestureListener

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
        val txtIssue :TxtMedium = itemView.txtIssue
        val txtCompany:TxtMedium = itemView.txtCompany
        //private val swipeView : SwipeActionView = itemView.findViewById(R.id.swipeIssue)
        private val rootContent :RelativeLayout = itemView.rootIssue

        fun bind(issue:Model.Issue,listener: IssueItemListener){
            txtIssue.text = issue.issue_name
            Utils.findCompany(issue.company_id,object : Utils.OnFindCompanyListener{
                override fun onResults(results: MutableList<Model.Contact>) {
                    if(results.size > 0) {
                        txtCompany.text = results[0].company
                    }
                }
            },ContactService(this).getContactInDb())

            itemView.setOnClickListener {
                println("ItemView ONClick")
                listener.onClickEdit(issue)
                //listener.onSelectIssue(issue)
            }

            /*
            val btnEdt = swipeView.findViewById<BtnMedium>(R.id.btnEdit)
            val btnDelete = swipeView.findViewById<BtnMedium>(R.id.btnDelete)
            swipeView.swipeGestureListener = object : SwipeGestureListener {
                override fun onSwipedLeft(swipeActionView: SwipeActionView): Boolean {
                    btnDelete.setOnClickListener {
                        listener.onClickDelete(issue)
                    }
                    btnEdt.setOnClickListener {
                        listener.onClickEdit(issue)
                    }
                    return false
                }

                override fun onSwipedRight(swipeActionView: SwipeActionView): Boolean {
                    return true
                }
            }
            */
        }
        override fun onSuccess() {}

        override fun onFail() {}
        override fun onDeleteSuccess() {
        }

    }

    interface IssueItemListener{
        fun onSelectIssue(issue: Model.Issue)
        fun onClickEdit(issue: Model.Issue)
        fun onClickDelete(issue: Model.Issue)
    }

}