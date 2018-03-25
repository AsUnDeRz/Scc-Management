package asunder.toche.sccmanagement.auth

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import asunder.toche.sccmanagement.Model
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
import com.twitter.sdk.android.core.*
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


/**
 *Created by ToCHe on 25/2/2018 AD.
 */
class ActivityLogin : AppCompatActivity(),ManageUserService.Sign,
        ConfirmDialog.ConfirmDialogListener{


    private val loadingDialog = LoadingDialog.newInstance()

    override fun onClickConfirm() {
    }


    override fun currentStatus(userAuth: Model.UserAuth) {
        when(userAuth.status_user){
            ROOT.APPROVE ->{
                loadingDialog.dismiss()
                val intent = Intent()
                intent.putExtra(ROOT.ADMIN,false)
                startActivity(Intent().setClass(this@ActivityLogin,ActivityMain::class.java))
                finish()
            }
            ROOT.REQUEST ->{
                showConfirmDialog("แจ้งเตือน","บัญชีของท่านอยู่ในระหว่างยืนยันตัวตน")
            }
            ROOT.REJECT ->{
                showConfirmDialog("แจ้งเตือน","บัญชีของท่านถูกปฏิเสธการเข้าใช้งานแอฟพลิเคชั่น")
            }
            ROOT.REGISTER ->{
                showConfirmDialog("แจ้งเตือน","ไม่พบบัญชีของท่านในระบบกรุณาลงทะเบียน")
                authManager.signOut()
            }
            ROOT.ADMIN ->{
                loadingDialog.dismiss()
                val intent = Intent()
                intent.putExtra(ROOT.ADMIN,true)
                startActivity(intent.setClass(this@ActivityLogin,ActivityMain::class.java))
                finish()
            }
        }

    }

    private val TAG= this::class.java.simpleName
    private var mGoogleSignInClient : GoogleSignInClient? = null
    private var mCallbackManager: CallbackManager? = null
    lateinit var authManager : ManageUserService
    private val RC_SIGN_IN = 9001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSDK()
        setContentView(R.layout.activity_login)
        authManager = ManageUserService()
        initTwitter()
        initFacebook()

        btnGoogle.setOnClickListener {
            val signInIntent = mGoogleSignInClient?.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        btnSignup.setOnClickListener {
            startActivity(Intent().setClass(this@ActivityLogin,ActivitySignup::class.java))
        }

        val user = authManager.mAuth.currentUser
        if(user != null){
            Log.d(TAG,"Current User /"+user.email)
            Log.d(TAG,"Current Uid /"+user.uid)
            Prefer.saveUUID(user.uid,this)
            loadingDialog.show(supportFragmentManager,LoadingDialog.TAG)
            authManager.checkLogin(user.uid,this)
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
                loadingDialog.show(supportFragmentManager,LoadingDialog.TAG)
                authManager.signWithTwitter(result!!.data,this@ActivityLogin)
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
                loadingDialog.show(supportFragmentManager,LoadingDialog.TAG)
                authManager.signWithFacebook(loginResult.accessToken,this@ActivityLogin)
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
                //socialType = "Facebook"
                //userID = accessToken.userId
                //token = accessToken.token

            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val name = account.displayName
            val email = account.email
            Log.d(TAG, "name: $name, email: $email")
            loadingDialog.show(supportFragmentManager,LoadingDialog.TAG)
            authManager.signWithGoogle(account,this@ActivityLogin)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                Log.w(TAG, "Google sign in Success")
                handleSignInResult(task)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)

            }
        }else{

            btnTwitter.onActivityResult(requestCode, resultCode, data)
            // Pass the activity result back to the Facebook SDK
            mCallbackManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showConfirmDialog(title:String,msg:String) {
        loadingDialog.dismiss()
        val fragment = ConfirmDialog.newInstance(msg,title)
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        fragment.show(supportFragmentManager, fragment::class.java.simpleName)
    }


}