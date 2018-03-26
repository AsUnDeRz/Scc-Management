package asunder.toche.sccmanagement.hover

import android.content.Context
import android.provider.ContactsContract
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.hover.theme.HoverTheme
import io.mattcarroll.hover.Content
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayList

/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class PlaceholderContent(context: Context, private val mBus: EventBus) : FrameLayout(context), Content {
    private val mTitleTextView: TextView? = null
    private var rvDemo: RecyclerView? = null

    init {
        init()
    }

    data class User(var number: String,
                    var name:String)


    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.view_placeholder_content, this, true)
        rvDemo = findViewById(R.id.rvDemo)
        rvDemo!!.layoutManager = LinearLayoutManager(context)
        getContact()
    }


    private fun getContact() {
        val users : MutableList<User> = mutableListOf()
        val phones = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        if (phones != null) {
            while (phones.moveToNext()) {
                val name = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                Log.d("TEST", "$name  $phoneNumber")
                if (phoneNumber.length >= 10) {
                    val user = User(phoneNumber,name)
                    users.add(user)
                }
            }
            phones.close()
        }

        rvDemo!!.adapter = ContactAdapter(users)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    override fun getView(): View {
        return this
    }

    override fun isFullscreen(): Boolean {
        return true
    }

    override fun onShown() {

    }

    override fun onHidden() {

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUser(user: User) {
        filterContact(user.number)
        val stickyEvent = EventBus.getDefault().getStickyEvent(User::class.java)
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent)
        }
    }

    fun onEventMainThread(newTheme: HoverTheme) {
        mTitleTextView!!.setTextColor(newTheme.accentColor)
    }

    private fun filterContact(numberReceive: String) {
        val users :MutableList<User> = mutableListOf()
        val phones = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        if (phones != null) {
            while (phones.moveToNext()) {
                val name = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                Log.d("TEST", "$name  $phoneNumber")
                if (phoneNumber == numberReceive) {
                    val user = User(phoneNumber,name)
                    users.add(user)
                }
            }
            phones.close()
        }

        rvDemo!!.adapter = ContactAdapter(users)
    }


    class ContactAdapter(users: MutableList<User>) : RecyclerView.Adapter<ContactAdapter.ContactHolder>() {

        internal var users: MutableList<User> = mutableListOf()

        init {
            this.users = users
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
            return ContactHolder(view)
        }

        override fun onBindViewHolder(holder: ContactHolder, position: Int) {
            holder.bind(users[position])
        }

        override fun getItemCount(): Int {
            return users.size
        }

        inner class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            lateinit var name: TextView
            lateinit var number: TextView
            fun bind(user: User) {
                name = itemView.findViewById(R.id.txtname)
                number = itemView.findViewById(R.id.txtNumber)
                number.setText(user.number)
                name.setText(user.name)
            }
        }
    }
}
