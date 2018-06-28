package asunder.toche.sccmanagement.transactions

import asunder.toche.sccmanagement.Model

/**
 *Created by ToCHe on 20/3/2018 AD.
 */
enum class TransactionState{
    NEWTRANSACTION,
    SHOWLIST,
    SHOWFORM,
    SHOWINPUT,
    SHOWSALEFORM,
    SHOWTRANSACTION,
    TRIGGERTOMAIN,
    SORTALL,
    SORTYESTERDAY,
    SORTTOMORROW,
    NEWFROMCONTACT,
    SELECTSALEPRICE,
    TRIGGERFROMSERVICE
}

interface TransactionListener{
    fun onClickTransaction(transaction: Model.Transaction)
    fun onClickNote(message:String)
}

interface IssueListener{
    fun onClickIssue(issue: Model.Issue)
}