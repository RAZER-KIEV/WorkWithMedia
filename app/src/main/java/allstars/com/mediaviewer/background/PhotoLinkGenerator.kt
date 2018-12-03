package allstars.com.mediaviewer.background

import allstars.com.mediaviewer.model.EXTRA_PICTURE_STRING
import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class PhotoLinkGenerator(var context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val data = Data.Builder()
            .putString(EXTRA_PICTURE_STRING, getRandomAvatarUrl())
            .build()
        outputData = data
        return Result.SUCCESS
    }

    private fun getRandomAvatarUrl(): String {
        return "http://graph.facebook.com/v2.5/" + getRandomInt() + "/picture?height=200&height=200"
    }

    private fun getRandomInt(): Int {
        return (Math.floor(Math.random() * (10000 - 5)) + 4).toInt()
    }
}