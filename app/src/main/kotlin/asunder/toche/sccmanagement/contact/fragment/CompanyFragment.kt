package asunder.toche.sccmanagement.contact.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.contact.adapter.CompanyAdapter
import asunder.toche.sccmanagement.custom.TriggerUpdate
import asunder.toche.sccmanagement.service.ContactService
import kotlinx.android.synthetic.main.fragment_contact_company.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *Created by ToCHe on 26/2/2018 AD.
 */
class CompanyFragment : Fragment() {

    private val TAG = this::class.java.simpleName
    lateinit var service : ContactService
    lateinit var adapter: CompanyAdapter
    val uid = "155434134123"

    companion object {
        fun newInstance(): CompanyFragment = CompanyFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"OnCreate")
        service = ContactService()
        EventBus.getDefault().register(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_contact_company, container, false)
        Log.d(TAG,"OnCreateView")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"OnViewCreated")
        rvContact.apply {
            setUpAdapter()
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = this@CompanyFragment.adapter
        }
    }

    fun setUpAdapter(){
        adapter = CompanyAdapter()
        adapter.setContact(service.getContactInDb(uid))
    }


    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public fun onTriggerUpdate(trigger : TriggerUpdate) {
        val stickyEvent = EventBus.getDefault().getStickyEvent(TriggerUpdate::class.java)
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent)
        }

        if(trigger.update){
            adapter.setContact(service.getContactInDb(uid))
        }
    }
}