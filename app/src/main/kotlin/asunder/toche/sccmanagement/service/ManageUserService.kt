package asunder.toche.sccmanagement.service

import android.os.Handler
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import com.crashlytics.android.Crashlytics
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.google.firebase.database.*
import com.twitter.sdk.android.core.TwitterSession
import io.paperdb.Paper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.*


/**
 *Created by ToCHe on 9/3/2018 AD.
 */
class ManageUserService{

    var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = this::class.java.simpleName
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var runnable:Runnable
    val handler = Handler()


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
                        listener.authFail(it.exception?.message.toString())
                        Crashlytics.log(it.exception?.message)
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
                        listener.currentStatus(Model.UserAuth("",ROOT.REGISTER,"",
                                "","",""))
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
                        listener.authFail(it.exception?.message.toString())
                        Crashlytics.log(it.exception?.message)
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
                        listener.currentStatus(Model.UserAuth("",ROOT.REGISTER,"",
                                "","",""))
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
                        listener.currentStatus(Model.UserAuth("",ROOT.REGISTER,"",
                                "","",""))
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
                        listener.authFail(it.exception?.message.toString())
                        Crashlytics.log(it.exception?.message)
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
                        listener.currentStatus(Model.UserAuth("",ROOT.REGISTER,"",
                                "","",""))
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
                        listener.authFail(it.exception?.message.toString())
                        Crashlytics.log(it.exception?.message)
                    }
                }
    }

    fun authWithAnymously(listener:Auth){
        mAuth.signInAnonymously()
                .addOnCompleteListener { it ->
                    if(it.isSuccessful){
                        val user = mAuth.currentUser
                        if (user != null) {
                            pushRequestSignUp(Model.UserAuth(user.uid,ROOT.REQUEST,
                                    Utils.getCurrentDateString(),"",
                                    "test_email",ROOT.EMAIL),listener)
                        }
                    }else{
                        Log.w(TAG, "signIn:failure", it.exception)
                        listener.authFail(it.exception?.message.toString())
                        Crashlytics.log(it.exception?.message)
                    }
                }
    }

    fun signOut(){
        val user = mAuth.currentUser
        Log.d(TAG,"Error with "+user?.email)
        Paper.book().destroy()
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
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Contact,Managemnt saved successfully.")
            }
        })

    }

    fun rejectUser(userAuth: Model.UserAuth){
        firebase.child("${ROOT.MANAGEMENT}/${userAuth.uid}").removeValue({ databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
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
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data Contact,Managemnt saved successfully.")
            }
        })

        firebase.child("${ROOT.MANAGEMENT}/${userAuth.uid}").removeValue({ databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
            } else {
                System.out.println("Data deleted successfully.")
            }
        })

    }

    fun pushRequestSignUp(userAuth: Model.UserAuth,listener:Auth){
        firebase.child("${ROOT.MANAGEMENT}/${userAuth.uid}").setValue(userAuth,{ databaseError, _ ->
            if (databaseError != null) {
                Crashlytics.log(databaseError.message)
                System.out.println("Data could not be saved " + databaseError.message)
                listener.authFail(databaseError.message)
                signOut()
            } else {
                System.out.println("Data management saved successfully.")
                listener.authSuccess(userAuth.auth_with,userAuth.email,userAuth.uid)
            }
        })
    }

    fun checkAdmin(){
        ROOT.listAdmin.clear()
        firebase.child(ROOT.ADMIN).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(data: DatabaseError?) {
                Log.d(TAG, data?.message)
                Crashlytics.log(data?.message)
            }
            override fun onDataChange(data: DataSnapshot?) {
                val emails = mutableListOf<String>()
                data?.children?.mapNotNullTo(emails){
                    it.child(ROOT.EMAIL).value.toString()
                }
                ROOT.listAdmin.addAll(emails)
                ROOT.listAdmin.forEach {
                    Log.d(TAG,it)
                }
            }
        })
    }

    fun fetchUsers(callBack : ServiceState){
        val usersAuth : MutableList<Model.UserAuth> = mutableListOf()
            firebase.child("${ROOT.MANAGEMENT}/").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(data: DatabaseError?) {
                    Log.d(TAG, data?.message)
                    Crashlytics.log(data?.message)
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
        runnable = Runnable {
            listener?.currentStatus(Model.UserAuth("",ROOT.REGISTER,
                    "","","",""))
        }
        firebase.child("${ROOT.MANAGEMENT}/$uid").addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onCancelled(data: DatabaseError?) {
                Log.d(TAG, data?.message)
                listener?.currentStatus(Model.UserAuth("", ROOT.REGISTER,
                        "","","",""))
                handler.removeCallbacks(runnable)
                Crashlytics.log(data?.message)
            }
            override fun onDataChange(data: DataSnapshot?) {
                val userAuth = data?.getValue(Model.UserAuth::class.java)
                if(userAuth == null){
                    listener?.currentStatus(Model.UserAuth("",ROOT.REGISTER,
                            "","","",""))
                    handler.removeCallbacks(runnable)
                }
                userAuth?.let {
                    listener?.currentStatus(it)
                    handler.removeCallbacks(runnable)
                }

            }
        })
        handler.postDelayed(runnable,4000)
    }


    fun synceDatabase(uid: String,listener :SyncData){
        Log.d(TAG,"Uid  "+uid)
        firebase.child(ROOT.USERS+"/"+uid).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError?) {
            }

            override fun onDataChange(data: DataSnapshot?) {
                val contacts = mutableListOf<Model.Contact>()
                val issues = mutableListOf<Model.Issue>()
                val products = mutableListOf<Model.Product>()
                val transactions = mutableListOf<Model.Transaction>()
                async(UI){
                    val jobContact = async(CommonPool) {
                        data?.child(ROOT.CONTACTS)?.children?.mapNotNullTo(contacts) {
                            it.getValue<Model.Contact>(Model.Contact::class.java)
                        }
                    }
                    val jobIssue = async(CommonPool){
                        data?.child(ROOT.ISSUE)?.children?.mapNotNullTo(issues) {
                            it.getValue<Model.Issue>(Model.Issue::class.java)
                        }
                    }
                    val jobProduct = async(CommonPool){
                        data?.child(ROOT.PRODUCTS)?.children?.mapNotNullTo(products) {
                            it.getValue<Model.Product>(Model.Product::class.java)
                        }
                    }
                    val jobTransaction = async(CommonPool){
                        data?.child(ROOT.TRANSACTIONS)?.children?.mapNotNullTo(transactions) {
                            it.getValue<Model.Transaction>(Model.Transaction::class.java)
                        }
                    }
                    async {
                        jobContact.await()?.let {
                            ContactService(object : ContactService.ContactCallBack{
                                override fun onSuccess() {

                                }

                                override fun onFail() {
                                }
                            }).pushNewContactToDb(it)
                        }
                    }.await()

                    async {
                        jobIssue.await()?.let {
                            IssueService(object :IssueService.IssueCallBack{
                                override fun onIssueSuccess() {

                                }

                                override fun onIssueFail() {
                                }
                            }).pushNewIssueToDb(it)
                        }
                    }.await()

                    async {
                        jobProduct.await()?.let {
                            ProductService(object : ProductService.ProductCallback{
                                override fun onSuccess() {

                                }

                                override fun onFail() {
                                }
                            }).pushNewProductToDb(it)
                        }
                    }.await()

                    async {
                        jobTransaction.await()?.let {
                            TransactionService(object : TransactionService.TransactionCallback{
                                override fun onSuccess() {

                                }

                                override fun onFail() {
                                }
                            }).pushNewTransactionToDb(it)
                        }
                    }.await()
                    Log.d(TAG,contacts.toString())
                    Log.d(TAG,issues.toString())
                    Log.d(TAG,products.toString())
                    Log.d(TAG,transactions.toString())
                    listener.syncSuccess()
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
        fun authSuccess(authWith:String,email:String,uid: String)
        fun authFail(message:String)
    }
    interface SyncData{
        fun syncSuccess()
    }

}