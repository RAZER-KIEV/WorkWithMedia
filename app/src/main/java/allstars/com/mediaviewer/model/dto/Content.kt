package allstars.com.mediaviewer.model.dto

import java.io.File
import java.util.*

data class Content(var path: String, var type: ContentType) : Comparable<Content> {

    var lastModDate: Date

    override fun compareTo(other: Content): Int {
        return other.lastModDate.compareTo(lastModDate)
    }

    init {
        lastModDate = getLastModDate(path)
    }

    private fun getLastModDate(filePath: String): Date {
        val file = File(filePath)
        return Date(file.lastModified())
    }
}
