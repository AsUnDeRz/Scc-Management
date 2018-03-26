package asunder.toche.sccmanagement.hover

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
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
        if (HoverService.mHoverView == null) {
            HoverService.showFloatingMenu(ctx)
        } else {
            if (number != null) {
                val displayname = filterContact(number,ctx)
                HoverService.expenHover(number,displayname.name)

            }
        }
        println("onInComingCallReceived")
    }

    protected fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date?) {
        if (number != null) {
            val displayname = filterContact(number,ctx)
            HoverService.expenHover(number,displayname.name)
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
        val phones = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        if (phones != null) {
            while (phones.moveToNext()) {
                val name = phones.getString(phones.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                Log.d("TEST", "$name  $phoneNumber")
                if (phoneNumber == numberReceive) {
                    val user = PlaceholderContent.User(phoneNumber, name)
                    users.add(user)
                    break
                }
            }
            phones.close()
        }
        return if(users.isNotEmpty()){
            users.first()
        }else{
            PlaceholderContent.User(numberReceive,numberReceive)
        }
    }

    companion object {

        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming: Boolean = false
        private var savedNumber: String? = null
    }

}

