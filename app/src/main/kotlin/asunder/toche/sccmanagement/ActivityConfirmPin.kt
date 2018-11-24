package asunder.toche.sccmanagement

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import asunder.toche.sccmanagement.main.ActivityMain
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.Utils
import com.afollestad.materialdialogs.MaterialDialog
import com.thefinestartist.utils.preferences.Pref
import kotlinx.android.synthetic.main.activity_confirm_pin.*

/**
 *Created by ToCHe on 12/11/2018 AD.
 */
class ActivityConfirmPin : AppCompatActivity(){

    private lateinit var loading: MaterialDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_pin)


        btnConfirmPin.setOnClickListener {
            if (edtPin.text.toString() == "X*98@t"){
                showDialog("ยืนยันรหัสผ่านสำเร็จ",true)
            }else{
                showDialog("รหัสผ่านไม่ถูกต้องกรุณาลองใหม่อีกครั้ง",false)
            }
        }
    }

    fun showDialog(msg: String,isSuccess:Boolean) {
        loading = MaterialDialog.Builder(this)
                .title("แจ้งเตือน")
                .content(msg)
                .typeface(Utils.getTypeFaceMedium(this), Utils.getTypeFaceMedium(this))
                .cancelable(false)
                .positiveText("ตกลง")
                .onPositive { dialog, which ->
                    if (isSuccess){
                        Prefer.setConfirmPin(this)
                        startActivity(Intent().setClass(this, ActivityMain::class.java))
                        finish()
                        loading.dismiss()
                        overridePendingTransition( R.anim.fade_in, R.anim.fade_out )
                    }else{
                        dialog.dismiss()
                    }

                }
                .build()
        loading.show()
    }

    fun hideDialog() {
        loading.dismiss()
    }
}