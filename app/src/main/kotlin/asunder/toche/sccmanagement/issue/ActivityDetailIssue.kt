package asunder.toche.sccmanagement.issue

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.preference.KEY
import kotlinx.android.synthetic.main.activity_detail_issue.*

/**
 *Created by ToCHe on 1/6/2018 AD.
 */
class ActivityDetailIssue:AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_issue)
        if (intent.hasExtra(KEY.EDIT_ISSUE_DETAIL.toString())){
            edtIssueDetail.setText(intent.getStringExtra(KEY.EDIT_ISSUE_DETAIL.toString()))
        }


        btnAddIssueInfo.setOnClickListener {
            finishAppWithData()
        }
        btnCancelIssueInfo.setOnClickListener {
            finishAppWithData()
        }
    }


    fun finishAppWithData(){
        val resultIntent = Intent()
        resultIntent.putExtra(KEY.EDIT_ISSUE_DETAIL.toString(), edtIssueDetail.text.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        finishAppWithData()
    }

}