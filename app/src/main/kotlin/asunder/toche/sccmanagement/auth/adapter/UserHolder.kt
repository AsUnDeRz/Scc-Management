package asunder.toche.sccmanagement.auth.adapter

import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.button.BtnMedium
import asunder.toche.sccmanagement.custom.textview.TxtBold
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import asunder.toche.sccmanagement.custom.textview.TxtThin
import asunder.toche.sccmanagement.service.ManageUserService
import kotlinx.android.synthetic.main.item_manage_user.view.*
import me.thanel.swipeactionview.SwipeActionView
import me.thanel.swipeactionview.SwipeDirection
import me.thanel.swipeactionview.SwipeGestureListener


/**
 *Created by ToCHe on 10/3/2018 AD.
 */
class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val txtEmail:TxtMedium
    private val txtCompany:TxtThin
    private val txtStatus:TxtThin
    private val swipeView :SwipeActionView

    init {
        txtEmail = itemView.findViewById(R.id.txtEmail)
        txtCompany = itemView.findViewById(R.id.txtCompany)
        txtStatus = itemView.findViewById(R.id.txtStatus)
        swipeView = itemView.findViewById(R.id.swipe_view)

    }

    fun bind(user:Model.UserAuth,listener :ManageUserService.ServiceState){
        txtEmail.text = user.email
        txtCompany.text = "บริษัท สงวนชัยเคมีอิมปอร์ตจำกัด"

        val btnLeft = swipeView.findViewById<BtnMedium>(R.id.btnLeft)
        val btnRight = swipeView.findViewById<BtnMedium>(R.id.btnRight)
        if(user.status_user == "approve"){
            txtStatus.text = "สถานะ : อนุญาตใช้งานระบบ"
            btnLeft.text = "Terminate"
            btnRight.text = "Cancel"
            btnLeft.setBackgroundColor(ContextCompat.getColor(itemView.context,R.color.colorGoogle))
            btnRight.setBackgroundColor(ContextCompat.getColor(itemView.context,R.color.light_grey))

            btnRight.setOnClickListener {

            }
        }else{
            txtStatus.text = "สถานะ : รอการอนุญาตใช้งานระบบ"
            btnLeft.text = "Approve"
            btnRight.text = "Reject"
            btnLeft.setBackgroundColor(ContextCompat.getColor(itemView.context,R.color.green_pressed))
            btnRight.setBackgroundColor(ContextCompat.getColor(itemView.context,R.color.colorGoogle))


        }

        swipeView.swipeGestureListener = object :SwipeGestureListener{
            override fun onSwipedLeft(swipeActionView: SwipeActionView): Boolean {
                if(user.status_user == "approve") {
                    btnLeft.setOnClickListener {
                        listener.onTerminate(user)
                    }
                }else{
                    btnRight.setOnClickListener {
                        listener.onReject(user)
                    }
                    btnLeft.setOnClickListener {
                        listener.onApprove(user)
                    }
                }

                return false
            }

            override fun onSwipedRight(swipeActionView: SwipeActionView): Boolean {
                //swipeActionView.moveToOriginalPosition(2000)
                return true
            }
        }

    }

}