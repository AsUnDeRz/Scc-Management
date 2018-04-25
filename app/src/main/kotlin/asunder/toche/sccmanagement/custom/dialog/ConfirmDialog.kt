package asunder.toche.sccmanagement.custom.dialog

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import asunder.toche.sccmanagement.R
import kotlinx.android.synthetic.main.dialog_confirm.*

/**
 *Created by ToCHe on 10/3/2018 AD.
 */
class ConfirmDialog : DialogFragment() {

    private var listener: ConfirmDialogListener? = null
    private var title:String = ""
    private var message:String =""
    private var txtPositive:String =""
    private var txtNegative:String =""
    private var isCancel = false
    private var isCustomTxtButton = false

    companion object {
        private const val MESSAGE = "message"
        private const val TITLE = "title"
        private const val CANCELABLE = "cancel_able"
        fun newInstance(message: String,
                        title:String,cancelAble:Boolean) : ConfirmDialog{
            val fragment = ConfirmDialog()
            val bundle = Bundle()
            bundle.putString(MESSAGE, message)
            bundle.putString(TITLE, title)
            bundle.putBoolean(CANCELABLE,cancelAble)
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener?.let {
            activity as ConfirmDialogListener
        }
         //listener = activity as ConfirmDialogListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            initData()
        } else {
            restoreData(savedInstanceState)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.dialog_confirm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListener()
        setText()
    }

    fun setListener(){
        btnConfirm.setOnClickListener {
            listener?.onClickConfirm()
            dismiss()
        }

        btnCancel.setOnClickListener {
            listener?.onClickCancel()
            dismiss()
        }
    }
    fun setText(){
        txtTitle.text = title
        txtMsg.text = message

        if (isCancel){
            btnCancel.visibility = View.VISIBLE
        }else{
            btnCancel.visibility = View.GONE
        }

        if (isCustomTxtButton){
            setTextButton(txtPositive,txtNegative)
        }
    }

    fun customTextButton(positive:String,negative:String){
        txtNegative = negative
        txtPositive = positive
        isCustomTxtButton =true

    }

    fun customListener(listener: ConfirmDialogListener){
        this.listener = listener
    }

    fun setTextButton(positive:String,negative:String){
        btnCancel.text = negative
        btnConfirm.text = positive
    }

    private fun initData() {
        message = arguments!!.getString(MESSAGE)
        title = arguments!!.getString(TITLE)
        isCancel = arguments!!.getBoolean(CANCELABLE)
    }

    private fun restoreData(savedInstanceState: Bundle) {
        message = savedInstanceState.getString(MESSAGE)
        title = savedInstanceState.getString(TITLE)
        isCancel = savedInstanceState.getBoolean(CANCELABLE)

    }

    interface ConfirmDialogListener{
        fun onClickConfirm()
        fun onClickCancel()
    }
}