package asunder.toche.sccmanagement.transactions

import asunder.toche.sccmanagement.Model

/**
 *Created by ToCHe on 20/3/2018 AD.
 */
enum class TransactionState{
    SHOWLIST,
    SHOWFORM,
    SHOWINPUT,
    SHOWSALEFORM,
    SHOWTRANSACTION,
    TRIGGERTOMAIN,
    SORTALL,
    SORTYESTERDAY,
    SORTTOMORROW,
    NEWFROMCONTACT
}

interface TransactionListener{
    fun onClickTransaction(transaction: Model.Transaction)
}