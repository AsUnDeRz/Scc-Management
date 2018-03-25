package asunder.toche.sccmanagement.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

/**
 *Created by ToCHe on 25/3/2018 AD.
 */
class ControllViewModel:ViewModel(){

    val currentUI: MutableLiveData<String> = MutableLiveData()

    fun updateCurrentUI(current: String){
        currentUI.value = current
    }
}