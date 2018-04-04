package asunder.toche.sccmanagement.issue.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection


/**
 *Created by ToCHe on 15/3/2018 AD.
 */


class SectionIssueAdapter() : StatelessSection(SectionParameters.builder()
        .itemResourceId(R.layout.item_issue)
        .headerResourceId(R.layout.item_section_issue)
        .build()) {
    constructor(dateString: String, data:MutableList<Model.Issue>,newListener: IssueAdapter.IssueItemListener) : this(){
        sectionDate = dateString
        issues = data
        listener = newListener
    }
    var issues: MutableList<Model.Issue> = mutableListOf()
    var sectionDate:String =""
    lateinit var listener : IssueAdapter.IssueItemListener

    override fun getContentItemsTotal(): Int {
        return issues.size // number of items of this section
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        // return a custom instance of ViewHolder for the items of this section
        return IssueAdapter.IssueHolder(view)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemHolder = holder as IssueAdapter.IssueHolder
        itemHolder.bind(issues[position],listener)
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        return SectionHolder(view)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        val headerHolder = holder as SectionHolder
        headerHolder.sectionDate.text = sectionDate+" (${issues.size})"
    }

     class SectionHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         val sectionDate: TxtMedium = itemView.findViewById(R.id.txtSectionDate)


    }


}