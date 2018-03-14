package asunder.toche.sccmanagement.auth.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.service.ManageUserService
import com.arlib.floatingsearchview.util.Util

/**
 *Created by ToCHe on 10/3/2018 AD.
 */
class ManageAdapter : RecyclerView.Adapter<UserHolder>() {

    var usersAuth : MutableList<Model.UserAuth> = mutableListOf()
    private lateinit var listener : ManageUserService.ServiceState
    private var mLastAnimatedItemPosition = -1


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_manage_user,parent,false)
        return UserHolder(view)
    }

    override fun getItemCount(): Int {
        return usersAuth.size
    }

    override fun onBindViewHolder(holder: UserHolder?, position: Int) {
        holder?.bind(usersAuth[holder.adapterPosition],listener)

        if (mLastAnimatedItemPosition < position) {
            animateItem(holder?.itemView)
            mLastAnimatedItemPosition = position
        }
    }

    fun setCallBack(callback : ManageUserService.ServiceState){
        listener = callback
    }

    fun setUserAuth(users : MutableList<Model.UserAuth>){
        this.usersAuth = users
        split()
        notifyDataSetChanged()

    }

    fun updateUsers(){
        split()
        notifyDataSetChanged()
    }

    fun clear(){
        this.usersAuth = mutableListOf()
        notifyDataSetChanged()
    }

    fun split(){
        val requestUser : MutableList<Model.UserAuth> = mutableListOf()
        val approveUser : MutableList<Model.UserAuth> = mutableListOf()

        usersAuth.forEach {
           if(it.status_user == "request"){
               requestUser.add(it)
           }else if(it.status_user == "approve"){
               approveUser.add(it)
           }
        }
        requestUser.sortBy { it.email }
        approveUser.sortBy { it.email }
        usersAuth = mutableListOf()
        usersAuth.apply {
            addAll(requestUser)
            addAll(approveUser)
        }
    }

    private fun animateItem(view: View?) {
        view?.translationY = Util.getScreenHeight(view?.context as Activity).toFloat()
        view.animate()
                .translationY(0f)
                .setInterpolator(DecelerateInterpolator(3f))
                .setDuration(700)
                .start()
    }

}