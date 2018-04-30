package asunder.toche.sccmanagement.contact

import asunder.toche.sccmanagement.Model

/**
 *Created by ToCHe on 15/3/2018 AD.
 */
enum class ContactState{
    ALLCONTACT,
    NEWCONTACT,
    EDITCONTACT,
    SELECTCONTACT,
    NEWISSUE,
    NEWTRANSACTION,
    SHOWFORM
}

interface ComponentListener{
    fun OnNumberClick(number: Model.Number,isAction:Boolean,position:Int)
    fun OnAddressClick(address:Model.Address,isAction: Boolean,position:Int)
    fun OnEmailClick(email:String,isAction: Boolean,position:Int)
    fun OnWebsiteClick(web:String,isAction: Boolean,position:Int)
}