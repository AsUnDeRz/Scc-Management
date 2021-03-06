package asunder.toche.sccmanagement.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.main.ActivityMain
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.service.ContactService
import java.util.*

/**
 *Created by ToCHe on 26/3/2018 AD.
 */
class PhoneStateReceive : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        try {
            println("Receiver")
            val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
            val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            println("onReceive number =" + number!!)
            var state = 0
            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }
            onCallStateChanged(context, state, number)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                onIncomingCallReceived(context, number, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    onOutgoingCallStarted(context, savedNumber, callStartTime)
                } else {
                    isIncoming = true
                    callStartTime = Date()
                    onIncomingCallAnswered(context, savedNumber, callStartTime)
                }
            TelephonyManager.CALL_STATE_IDLE ->
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime)
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, Date())
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, Date())
                }
        }
        lastState = state
    }

    protected fun onIncomingCallReceived(ctx: Context, number: String?, start: Date?) {
            if (number != null) {
                //val displayname = filterContact(number, ctx)
                val displayname = filterContact(ctx,number)
                /*
                if(!displayname.number.isEmpty()){
                    checkActivity(displayname,ctx)
                }
                */
                //HoverService.updateCurrent(displayname)
                FloatingViewService.updateCurrent(displayname)

            }

        println("onInComingCallReceived")
    }

    fun checkActivity(displayname:PlaceholderContent.User,context: Context){
        if (ActivityMain.isActivityResume != null && ActivityMain.isActivityResume!!){
            val intent = Intent()
            intent.putExtra(ROOT.MOBILE,displayname.number)
            context.startActivity(intent.setClass(context,ActivityMain::class.java))
        }else {
            HoverService.expenHover(displayname.number, displayname.name)
        }
    }

    protected fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date?) {
        if (number != null) {
            //val displayname = filterContact(number,ctx)
            //HoverService.expenHover(number,displayname.name)
        }
        println("onInComingCallAnswered")

    }

    protected fun onIncomingCallEnded(ctx: Context, number: String?, start: Date?, end: Date?) {
        println("onInComingCallEnded")

    }

    protected fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date?) {
        println("onOutgoingCallStarted")

    }

    protected fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date?, end: Date?) {
        println("onOutgoingCallEnded")
    }

    protected fun onMissedCall(ctx: Context, number: String?, start: Date?) {
        println("onMissedCall")
    }

    private fun filterContact(numberReceive: String,context: Context): PlaceholderContent.User {
        val users :MutableList<PlaceholderContent.User> = mutableListOf()
        val service = ContactService(object : ContactService.ContactCallBack{
            override fun onDeleteSuccess() {
            }

            override fun onSuccess() {
            }
            override fun onFail() {
            }
        })
        val contacts = service.getContactInDb()
        contacts.asSequence().forEach {
            val result = it.numbers.filter { it.data == numberReceive }
            if (result.isNotEmpty()) {
                val user = PlaceholderContent.User(numberReceive, it.contact_name)
                users.add(user)
            }
        }
        return if(users.isNotEmpty()){
            users.first()
        }else{
            PlaceholderContent.User("","")
        }
    }


    private fun filterContact(context: Context,numberReceive: String): Model.Contact {
        var users :Model.Contact? = null
        val service = ContactService(object : ContactService.ContactCallBack{
            override fun onDeleteSuccess() {
            }
            override fun onSuccess() {
            }
            override fun onFail() {
            }
        })
        val contacts = service.getContactInDb()
        contacts.asSequence().forEach {
            val result = it.numbers.filter { it.data == numberReceive }
            if (result.isNotEmpty()) {
                users = it
            }
        }

        return if(users == null){
            Model.Contact()
        }else{
            users!!
        }
    }


    companion object {

        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming: Boolean = false
        private var savedNumber: String? = null
    }

}

