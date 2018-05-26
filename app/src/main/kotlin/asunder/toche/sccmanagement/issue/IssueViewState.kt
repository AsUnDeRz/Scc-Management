package asunder.toche.sccmanagement.issue

import asunder.toche.sccmanagement.Model

/**
 *Created by ToCHe on 17/3/2018 AD.
 */
enum class IssueState{
    ALLISSUE,
    NEWISSUE,
    NEWFROMCONTACT,
    TRIGGERFROMSERVICE,
    SHOWFROM,

}
interface ComponentListener{
    fun OnFileClick(file:Model.Content,isDeleteOrShare:Boolean,position: Int)
    fun OnPictureClick(picture:Model.Content,isDeleteOrShare:Boolean,position: Int)
}