package asunder.toche.sccmanagement.transactions

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.preference.ROOT

/**
 *Created by ToCHe on 26/4/2018 AD.
 */
class ActivityHistory:AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        println("onCreate")

    }

    override fun onResume() {
        super.onResume()
        println("onResume")

    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        println("onResumeFragment")
    }


    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)
        println("onAttachFragment")
    }

    fun getProductID():String{
        return if (intent.hasExtra(ROOT.PRODUCTS)){
            intent.getStringExtra(ROOT.PRODUCTS)
        }else{
            ""
        }
    }
}