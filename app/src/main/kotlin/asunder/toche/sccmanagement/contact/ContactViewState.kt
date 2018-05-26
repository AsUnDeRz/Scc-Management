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
    SHOWFORM,
    TRIGGERFROMTRANSACTION,
    TRIGGERFROMISSUE,
    SAVED
}

interface ComponentListener{
    fun OnNumberClick(number: Model.Channel,isAction:Boolean,position:Int)
    fun OnAddressClick(address:Model.Address,isAction: Boolean,position:Int)
    fun OnEmailClick(email:Model.Channel,isAction: Boolean,position:Int)
    fun OnWebsiteClick(web:Model.Channel,isAction: Boolean,position:Int)
    fun updateTypeList(type:String)
}