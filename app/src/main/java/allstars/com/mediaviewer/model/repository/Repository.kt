package allstars.com.mediaviewer.model.repository

import allstars.com.mediaviewer.background.PhotoLinkGenerator
import allstars.com.mediaviewer.model.EXTRA_VIEW_TIME_SEC
import allstars.com.mediaviewer.model.SHPREF_NAME
import allstars.com.mediaviewer.model.VIEW_TIME_SEC
import allstars.com.mediaviewer.model.dto.Content
import allstars.com.mediaviewer.model.dto.ContentType
import android.app.Application
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import io.reactivex.Single
import java.util.*

class Repository(var app: Application) {

    //Cash
    private var localContentList = ArrayList<Content>()

    fun saveSettingsToShPref(value: Int) {
        val sharedPref = app.getSharedPreferences(SHPREF_NAME, Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt(EXTRA_VIEW_TIME_SEC, value)
            apply()
        }
    }

    fun getSettingFromShPref(): Int {
        val sharedPref = app.getSharedPreferences(SHPREF_NAME, Context.MODE_PRIVATE) ?: return 0
        return sharedPref.getInt(EXTRA_VIEW_TIME_SEC, (VIEW_TIME_SEC).toInt())
    }

    fun getLocalContent(): Single<ArrayList<Content>> {
        createVideoCursor()?.let { addToContentList(it, ContentType.VIDEO) }
        createImagesCursor()?.let { addToContentList(it, ContentType.PHOTO) }
        localContentList.sort()
        return Single.just(localContentList)
    }

    private fun addToContentList(cursor: Cursor, type: ContentType) {
        val imagesPath = ArrayList<String>()
        if (cursor.moveToFirst()) {
            val dataColumn = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media.DATA
            )
            do {
                val contentItem = Content(path = cursor.getString(dataColumn), type = type)
                localContentList.add(contentItem)
                imagesPath.add(cursor.getString(dataColumn))
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    private fun createVideoCursor(): Cursor? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val images = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        return app.contentResolver?.query(
            images,
            projection,
            "",
            null,
            ""
        )
    }

    private fun createImagesCursor(): Cursor? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        return app.contentResolver?.query(
            images,
            projection,
            "",
            null,
            ""
        )
    }

    fun getPictureFromInternet(): UUID {
        val internetConst = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<PhotoLinkGenerator>()
            .setConstraints(internetConst)
            .build()

        WorkManager.getInstance().enqueue(request)
        return request.id
    }


}