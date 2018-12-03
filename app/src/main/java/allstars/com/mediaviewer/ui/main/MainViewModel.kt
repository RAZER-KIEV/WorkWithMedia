package allstars.com.mediaviewer.ui.main

import allstars.com.mediaviewer.model.dto.Content
import allstars.com.mediaviewer.model.repository.Repository
import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import io.reactivex.rxkotlin.subscribeBy
import java.util.*

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private var repository: Repository = Repository(app)

    var contentList = MutableLiveData<MutableList<Content>>()
        @SuppressLint("CheckResult")
        get() {
            if (field.value == null || field.value?.isEmpty()!!) {
                repository.getLocalContent().subscribeBy(onSuccess = {
                    field.value = it
                }, onError = {
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

    fun saveSettingsToShPref(value: Int) {
        repository.saveSettingsToShPref(value = value)
        viewTime.value = value
    }

    fun getPictureFromInternet(): UUID {
        return repository.getPictureFromInternet()
    }
}
