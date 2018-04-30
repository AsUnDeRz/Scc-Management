package asunder.toche.sccmanagement.contact.adapter

import android.provider.UserDictionary
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.custom.textview.TxtThin
import me.thanel.swipeactionview.SwipeActionView
import me.thanel.swipeactionview.SwipeDirection
import me.thanel.swipeactionview.SwipeGestureListener

/**
 *Created by ToCHe on 12/3/2018 AD.
 */
class CompanyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtCompany: TxtMedium
    private val txtContact: TxtThin
    private val swipeView : SwipeActionView
    private val rootContent :RelativeLayout

    init {
        txtContact = itemView.findViewById(R.id.txtContact)
        txtCompany = itemView.findViewById(R.id.txtCompany)
        swipeView = itemView.findViewById(R.id.swipeCompany)
        rootContent = itemView.findViewById(R.id.rootContentCompany)
    }
    fun bindWithEditable(contact: Model.Contact,listener: CompanyAdapter.CompanyListener){
        txtCompany.text = validateCompany(contact)
        //txtContact.text = validateContact(contact)
        val btnEdt = swipeView.findViewById<BtnMedium>(R.id.btnEdit)
        val btnDelete = swipeView.findViewById<BtnMedium>(R.id.btnDelete)

        swipeView.swipeGestureListener = object : SwipeGestureListener {
            override fun onSwipedLeft(swipeActionView: SwipeActionView): Boolean {
                btnDelete.setOnClickListener {
                    listener.onClickDelete(contact)
                }
                btnEdt.setOnClickListener {
                    listener.onClickEdit(contact)
                }
                return false
            }

            override fun onSwipedRight(swipeActionView: SwipeActionView): Boolean {
                return true
            }
        }

        rootContent.setOnClickListener {
            listener.onSelectContact(contact)
        }
        txtContact.setOnClickListener {
            listener.onSelectContact(contact)
        }
        txtCompany.setOnClickListener {
            listener.onSelectContact(contact)
        }
    }

    fun bind(contact: Model.Contact,listener: CompanyAdapter.CompanyOnClickListener){
        txtCompany.text = validateCompany(contact)
        txtContact.text = validateContact(contact)
        swipeView.setDirectionEnabled(SwipeDirection.Left, false)

        rootContent.setOnClickListener {
            listener.onClickCompany(contact)
        }
        txtCompany.setOnClickListener {
            listener.onClickCompany(contact)
        }
        txtContact.setOnClickListener {
            listener.onClickCompany(contact)
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
            contact.contact_name == "" -> "- ${contact.numbers.let { it[0] }}"
            contact.company == "" -> "- ${contact.numbers.let { it[0] }}"
            else -> "- ${contact.contact_name.capitalize()}"
        }
    }

}