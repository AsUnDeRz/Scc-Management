package asunder.toche.sccmanagement.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

/**
 *Created by ToCHe on 25/3/2018 AD.
 */
class ControlViewModel:ViewModel(){

    val currentUI: MutableLiveData<String> = MutableLiveData()
    val currentState : MutableLiveData<UiState> = MutableLiveData()

    fun updateCurrentUI(current: String){
        currentUI.value = current
    }

    fun updateCurrentState(current : UiState){
        currentState.value = current
    }
}