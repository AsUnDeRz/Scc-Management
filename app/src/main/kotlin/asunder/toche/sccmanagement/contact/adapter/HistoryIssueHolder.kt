package asunder.toche.sccmanagement.contact.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtBold
import asunder.toche.sccmanagement.custom.textview.TxtMedium

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class HistoryIssueHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtDate : TxtMedium
    private val txtCircleState : TxtBold
    private val txtIssue : TxtMedium

    init {
        txtDate = itemView.findViewById(R.id.txtDate)
        txtCircleState = itemView.findViewById(R.id.txtCircleState)
        txtIssue = itemView.findViewById(R.id.txtIssue)
    }

    fun bind(position :Int){

        if(position < 3){
            txtCircleState.isSelected = true
            txtCircleState.text = "C"
        }

    }

}