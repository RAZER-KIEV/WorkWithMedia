package allstars.com.mediaviewer.ui.main

import allstars.com.mediaviewer.model.dto.Content
import allstars.com.mediaviewer.model.repository.Repository
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import io.reactivex.rxkotlin.subscribeBy

class MainViewModel(var app: Application) : AndroidViewModel(app) {
    private var repository: Repository = Repository(app)
    var toastMessage = MutableLiveData<String>()
    //cache

    var contentList = MutableLiveData<MutableList<Content>>()
        @SuppressLint("CheckResult")
        get() {
            if (field.value == null || field.value?.isEmpty()!!) {
                repository.getLocalContent().subscribeBy(onSuccess = {
                    field.value = it
                }, onError = {
                    toastMessage.value = it.toString()
                    it.printStackTrace()
                })
            }
            return field
        }

    var viewTime = MutableLiveData<Int>()
        get() {
            if (field.value == null || field.value == 0) {
                field.value = repository.getSettingFromShPref()
            }
            return field
        }

    public fun saveSettingsToShPref(value: Int) {
        repository.saveSettingsToShPref(value = value)
        viewTime.value = value
    }

//    @SuppressLint("CheckResult")
//    private fun getLocalContent() {
//        repository.getLocalContent().subscribeBy(onSuccess = {
//            contentList.value = it
//        }, onError = {
//            toastMessage.value = it.toString()
//            it.printStackTrace()
//        })
//    }

//    fun getBills(): Single<ArrayList<BaseBill>> {
//        return if (cachedBills.size == 0) {
//            val response = RetroServiceStub().getBills()
//            response.subscribeBy(onSuccess = { cachedBills = it }, onError = {})
//            response
//        } else
//            Single.just(cachedBills)
//    }
}
