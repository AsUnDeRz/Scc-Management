package asunder.toche.sccmanagement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.service.FirebaseManager
import asunder.toche.sccmanagement.service.ManageUserService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.io.File


/**
 *Created by ToCHe on 27/2/2018 AD.
 */
class testFirebase : AppCompatActivity(){

    private var mGoogleSignInClient : GoogleSignInClient? = null
    val TAG : String ="TEST_FIREBASE"
    private val RC_SIGN_IN = 9001
    lateinit var ts :FirebaseManager
    lateinit var mUser : ManageUserService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val name = account.displayName
            val email = account.email
            Toast.makeText(this, "name: $name, email: $email", Toast.LENGTH_LONG).show()
            Log.d(TAG, "name: $name, email: $email")
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

        }
    }

    fun process() = async(UI) {
        try {
            val job = async(CommonPool) {

            }
            job.await()
            //We're back on the main thread here.
            //Update UI controls such as RecyclerView adapter data.
        }
        catch (e: Exception) {
        }
        finally {
        }
    }
}