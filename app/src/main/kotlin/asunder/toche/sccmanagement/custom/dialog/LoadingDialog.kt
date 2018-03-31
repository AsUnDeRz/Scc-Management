package asunder.toche.sccmanagement.custom.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import asunder.toche.sccmanagement.R

/**
 *Created by ToCHe on 25/3/2018 AD.
 */
class LoadingDialog : DialogFragment() {


    var isShow = false
    override fun onStart() {
        super.onStart()
        setupBackDrop()
    }

    private fun setupBackDrop() {
        val window = dialog.window
        val windowParams = window!!.attributes
        windowParams.dimAmount = 0.0f
        window.attributes = windowParams
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isCancelable = false
        return inflater.inflate(R.layout.dialog_loading, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        super.show(manager, tag)
        isShow = true
    }

    override fun dismiss() {
        super.dismiss()
        isShow = false
    }

    /*
    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
    */


    /*
    override fun show(manager: FragmentManager, tag: String) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commit()
        } catch (e: IllegalStateException) {
        }

    }
    */

    companion object {

        val TAG = LoadingDialog::class.java.simpleName
        private const val MESSAGE = "message"
        private const val TITLE = "title"
        fun newInstance() : LoadingDialog{
            val fragment = LoadingDialog()
            val bundle = Bundle()
            bundle.putString(MESSAGE, "")
            bundle.putString(TITLE, "")
            fragment.arguments = bundle
            return fragment
        }
    }
}// Empty constructor is required for DialogFragment
// Make sure not to add arguments to the constructor
// Use `newInstance` instead as shown below