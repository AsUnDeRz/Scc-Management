package asunder.toche.sccmanagement.auth

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.custom.dialog.ConfirmDialog
import asunder.toche.sccmanagement.custom.dialog.LoadingDialog
import asunder.toche.sccmanagement.custom.extension.hideLoading
import asunder.toche.sccmanagement.custom.extension.showLoading
import asunder.toche.sccmanagement.main.ActivityMain
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.service.ManageUserService
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.twitter.sdk.android.core.*
import kotlinx.android.synthetic.main.activity_signup.*
import java.util.*

/**
 *Created by ToCHe on 25/2/2018 AD.
 */
class ActivitySignup : AppCompatActivity(),
        ConfirmDialog.ConfirmDialogListener,
        ManageUserService.Auth{


    private val TAG= this::class.java.simpleName
    private var mGoogleSignInClient : GoogleSignInClient? = null
    private var mCallbackManager: CallbackManager? = null
    lateinit var authManager : ManageUserService
    private val RC_SIGN_IN = 9001
    private val loadingDialog = LoadingDialog.newInstance()
    private var isAuthSuccess = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSDK()
        setContentView(R.layout.activity_signup)
        authManager = ManageUserService()
        initTwitter()
        initFacebook()

        btnConfirm.setOnClickListener {
            if(validateEmailPassword()){
                authManager.authWithEmail(edtEmail.text.toString(),
                        edtPassword.text.toString(),
                        this@ActivitySignup)
                showLoading()
            }
        }


        btnGoogle.setOnClickListener {
            val signInIntent = mGoogleSignInClient?.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    fun initSDK(){
        val authConfig = TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret))
        val twitterConfig = TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build()
        Twitter.initialize(twitterConfig)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }


    fun initTwitter(){
        btnTwitter?.callback = object : Callback<TwitterSession>(){
            override fun success(result: Result<TwitterSession>?) {
                authManager.authWithTwitter(result!!.data,this@ActivitySignup)
                showLoading()
            }
            override fun failure(exception: TwitterException?) {
            }
        }
    }

    fun initFacebook(){
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult)
                authManager.authWithFacebook(loginResult.accessToken,this@ActivitySignup)
                showLoading()
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })

        btnFacebook.setOnClickListener {
            val accessToken = AccessToken.getCurrentAccessToken()
            if (accessToken == null || accessToken.isExpired) {
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
            } else {
                LoginManager.getInstance().logOut()
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val name = account.displayName
            val email = account.email
            authManager.authWithGoogle(account,this@ActivitySignup)
            showLoading()
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                handleSignInResult(task)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }else{

            btnTwitter.onActivityResult(requestCode, resultCode, data)
            mCallbackManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun validateEmailPassword() :Boolean {
        val emailPattern = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
        if(TextUtils.isEmpty(edtEmail.text) || !edtEmail.text.toString().matches(Regex(emailPattern))){
            edtEmail.error = "กรุณากรอกอีเมลให้ถูกต้อง"
            return false
        }
        if(TextUtils.isEmpty(edtPassword.text)){
            edtPassword.error = "กรุณากรอกรหัสผ่านให้ถูกต้อง"
            return false
        }
        return true
    }

    private fun showConfirmDialog(title:String,msg:String) {
        val fragment = ConfirmDialog.newInstance(msg,title,false)
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        fragment.show(supportFragmentManager, fragment::class.java.simpleName)
    }

    override fun authSuccess(authWith: String, email: String,uid:String) {
        val result = ROOT.listAdmin.filter { it == email }
        if (result.isEmpty()) {
            isAuthSuccess = true
            showConfirmDialog("แจ้งเตือน", "ท่านได้ส่งคำขอเข้าใช้ระบบด้วยบัญชี $authWith กรุณารอการยืนยันจากผู้ดูแลระบบ")
            hideLoading()
        }else{
            Prefer.saveUUID(uid,this)
            val intent = Intent()
            intent.putExtra(ROOT.ADMIN,true)
            startActivity(intent.setClass(this@ActivitySignup,ActivityMain::class.java))
            finish()
        }
    }

    override fun authFail(message: String) {
        isAuthSuccess = false
        showConfirmDialog("แจ้งเตือน",message)
        hideLoading()
    }

    fun showLoading(){
        loadingDialog.show(supportFragmentManager,LoadingDialog.TAG)
    }

    fun hideLoading(){
        loadingDialog.dismiss()
    }

    override fun onClickConfirm() {
        if (isAuthSuccess){
            authManager.signOut()
            finish()
        }
    }

    override fun onClickCancel() {
        if (isAuthSuccess){
            authManager.signOut()
            finish()
        }
    }
}