package asunder.toche.sccmanagement.service

import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.google.firebase.database.*
import com.twitter.sdk.android.core.TwitterSession
import java.util.*


/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class ManageUserService{

    var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()


    fun authWithGoogle(acct: GoogleSignInAccount,listener: Auth) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { it ->
                    if (it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            val email = if (user.email == null){""}else{user.email!!}
                                pushRequestSignUp(Model.UserAuth(user.uid, ROOT.REQUEST,
                                        Utils.getCurrentDateString(), "",
                                        email, ROOT.GOOGLE),listener)
                        }
                    }else{
                        Log.w(TAG, "signInWithCredential:failure", it.exception)
                    }
                }
    }

    fun signWithGoogle(acct: GoogleSignInAccount,listener: Sign) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { it ->
                    if (it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            val email = if (user.email == null){""}else{user.email!!}
                            checkLogin(user.uid,listener)
                        }
                    }else{
                        Log.w(TAG, "signInWithCredential:failure", it.exception)
                    }
                }
    }

    fun authWithFacebook(token: AccessToken,listener: Auth){
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { it ->
                    if(it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            val email = if (user.email == null){""}else{user.email!!}
                            pushRequestSignUp(Model.UserAuth(user.uid, ROOT.REQUEST,
                                        Utils.getCurrentDateString(), "",
                                        email, ROOT.FACEBOOK),listener)
                        }
                    }else{
                        Log.w(TAG, "signInWithCredential:failure", it.exception)
                    }
                }
    }

    fun signWithFacebook(token: AccessToken,listener: Sign){
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { it ->
                    if(it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            val email = if (user.email == null){""}else{user.email!!}
                            checkLogin(user.uid,listener)
                        }
                    }else{
                        Log.w(TAG, "signInWithCredential:failure", it.exception)
                    }
                }
    }

    fun signWithTwitter(session : TwitterSession,listener: Sign){
        val credential = TwitterAuthProvider.getCredential(
                session.authToken.token,
                session.authToken.secret)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { it ->
                    if(it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            val email = if (user.email == null){""}else{user.email!!}
                            checkLogin(user.uid,listener)
                        }
                    }else{
                        Log.w(TAG, "signInWithEmail:failure", it.exception)
                    }
                }
    }
    fun authWithTwitter(session : TwitterSession,listener: Auth){
        val credential = TwitterAuthProvider.getCredential(
                session.authToken.token,
                session.authToken.secret)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { it ->
                    if(it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            val email = if (user.email == null){""}else{user.email!!}
                            pushRequestSignUp(Model.UserAuth(user.uid, ROOT.REQUEST,
                                        Utils.getCurrentDateString(), "",
                                        email, ROOT.TWITTER),listener)
                        }
                    }else{
                        Log.w(TAG, "signInWithEmail:failure", it.exception)
                    }
                }
    }

    fun signWithEmail(email:String,password:String,listener: Sign){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { it ->
                    if(it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            val emailUser = if (user.email == null){""}else{user.email!!}
                            checkLogin(user.uid,listener)
                        }
                    }else{
                        Log.w(TAG, "signInWithEmail:failure", it.exception)
                    }
                }
    }

    fun authWithEmail(email:String,password:String,listener: Auth) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { it ->
                    if(it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            val emailUser = if (user.email == null){""}else{user.email!!}
                            pushRequestSignUp(Model.UserAuth(user.uid,ROOT.REQUEST,
                                    Utils.getCurrentDateString(),"",
                                    emailUser,ROOT.EMAIL),listener)
                        }
                    }else{
                        Log.w(TAG, "signInWithEmail:failure", it.exception)
                    }
                }
    }

    fun signOut(){
        val user = mAuth.currentUser
        Log.d(TAG,"Error with "+user?.email)
        mAuth.signOut()
    }

    fun approveUser(userAuth: Model.UserAuth){
        val childUpdates = HashMap<String,Any>()
        val dateString = Utils.getCurrentDateString()
        childUpdates["${ROOT.USERS}/${userAuth.uid}/uid"] = userAuth.uid
        childUpdates["${ROOT.USERS}/${userAuth.uid}/status_user"] = ROOT.APPROVE
        childUpdates["${ROOT.USERS}/${userAuth.uid}/approve_date"] = dateString
        childUpdates["${ROOT.USERS}/${userAuth.uid}/request_date"] = userAuth.request_date
        childUpdates["${ROOT.MANAGEMENT}/${userAuth.uid}/status_user"] = ROOT.APPROVE
        childUpdates["${ROOT.MANAGEMENT}/${userAuth.uid}/approve_date"] = dateString

        firebase.updateChildren(childUpdates,{databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Contact,Managemnt saved successfully.")
            }
        })

    }

    fun rejectUser(userAuth: Model.UserAuth){
        firebase.child("${ROOT.MANAGEMENT}/${userAuth.uid}").removeValue({ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data deleted successfully.")
            }
        })

    }

    fun terminateUser(userAuth: Model.UserAuth){
        val childUpdates = HashMap<String,Any>()
        childUpdates["${ROOT.USERS}/${userAuth.uid}/status_user"] = ROOT.TERMINATE

        firebase.updateChildren(childUpdates,{databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Contact,Managemnt saved successfully.")
            }
        })

        firebase.child("${ROOT.MANAGEMENT}/${userAuth.uid}").removeValue({ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data deleted successfully.")
            }
        })

    }

    fun pushRequestSignUp(userAuth: Model.UserAuth,listener:Auth){
        firebase.child("${ROOT.MANAGEMENT}/${userAuth.uid}").setValue(userAuth,{ databaseError, _ ->
            if (databaseError != null) {
                //Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data management saved successfully.")
                listener.authSuccess(userAuth.auth_with)
                signOut()
            }
        })
    }

    fun fetchUsers(callBack : ServiceState){
        val usersAuth : MutableList<Model.UserAuth> = mutableListOf()
            firebase.child("${ROOT.MANAGEMENT}/").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(data: DatabaseError?) {
                    Log.d(TAG, data?.message)
                    //Crashlytics.log(data?.message)
                }

                override fun onDataChange(data: DataSnapshot?) {
                    data?.children?.mapNotNullTo(usersAuth) {
                        it.getValue<Model.UserAuth>(Model.UserAuth::class.java)
                    }
                    callBack.loadUserSuccess(usersAuth)
                }
            })
    }

    fun checkLogin(uid: String,listener:Sign?){
        firebase.child("${ROOT.MANAGEMENT}/$uid").addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onCancelled(data: DatabaseError?) {
                Log.d(TAG, data?.message)
                //Crashlytics.log(data?.message)
            }
            override fun onDataChange(data: DataSnapshot?) {
                val userAuth = data?.getValue(Model.UserAuth::class.java)
                userAuth?.let {
                    listener?.currentStatus(it)
                }

            }
        })
    }

    interface ServiceState{
        fun loadUserSuccess(users:MutableList<Model.UserAuth>)
        fun onApprove(userAuth: Model.UserAuth)
        fun onReject(userAuth: Model.UserAuth)
        fun onTerminate(userAuth: Model.UserAuth)
    }
    interface Sign{
        fun currentStatus(userAuth: Model.UserAuth)
    }
    interface Auth{
        fun authSuccess(authWith:String)
    }

}