package asunder.toche.sccmanagement.contact.adapter

import android.provider.UserDictionary
import android.support.v7.widget.RecyclerView
import android.view.View
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.custom.textview.TxtThin
import me.thanel.swipeactionview.SwipeActionView
import me.thanel.swipeactionview.SwipeGestureListener

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class CompanyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtCompany: TxtMedium
    private val txtContact: TxtThin
    private val swipeView : SwipeActionView

    init {
        txtContact = itemView.findViewById(R.id.txtContact)
        txtCompany = itemView.findViewById(R.id.txtCompany)
        swipeView = itemView.findViewById(R.id.swipeCompany)
    }
    fun bind(contact: Model.Contact){
        txtCompany.text = validateCompany(contact)
        txtContact.text = validateContact(contact)

        val btnEdt = swipeView.findViewById<BtnMedium>(R.id.btnEdit)
        val btnDelete = swipeView.findViewById<BtnMedium>(R.id.btnDelete)

        swipeView.swipeGestureListener = object : SwipeGestureListener {
            override fun onSwipedLeft(swipeActionView: SwipeActionView): Boolean {
                btnDelete.setOnClickListener {

                }
                btnEdt.setOnClickListener {

                }

                return false
            }

            override fun onSwipedRight(swipeActionView: SwipeActionView): Boolean {
                //swipeActionView.moveToOriginalPosition(2000)
                return true
            }
        }

    }

    fun validateCompany(contact: Model.Contact) :String{
        return if (contact.company == ""){
            contact.contact_name.capitalize()
        }else{
            contact.company.capitalize()
        }
    }

    fun validateContact(contact: Model.Contact):String{
        return when {
            contact.contact_name == "" -> "- ${contact.mobile}"
            contact.company == "" -> "- ${contact.mobile}"
            else -> "- ${contact.contact_name.capitalize()}"
        }
    }

}