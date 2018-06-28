package asunder.toche.sccmanagement.products

import asunder.toche.sccmanagement.Model

/**
 *Created by ToCHe on 19/3/2018 AD.
 */
enum class ProductState{
    NEWPRODUCT,
    SHOWLIST,
    SHOWFORM,
    SHOWINPUT,
    SHOWMEDIUMFORM,
    SHOWPRODUCT,
    SELECTMEDIUM,
    NEWMEDIUM,
    SHOWFORMWITHPRODUCT,
    TRIGGERFROMTRANSACTION

}
interface ComponentListener{
    fun OnFileClick(file: Model.ContentForProduct, isDeleteOrShare:Boolean, position: Int)
    fun OnPictureClick(picture: Model.ContentForProduct, isDeleteOrShare:Boolean, position: Int)
    fun updateTypeList(type:String)

}