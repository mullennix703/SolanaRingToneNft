package com.solanamobile.mintyfresh.gallery.viewmodel

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val TAG = "MediaViewModel"
data class Media(
    val path: String,
    val dateAdded: String,
    val mimeType: String,
    val title: String,
    val size: String
)

@HiltViewModel
class MediaViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            loadAllMediaFiles()
        }
    }

    private var mediaStateFlow: MutableStateFlow<List<Media>> = MutableStateFlow(listOf())

    fun getMediaList(): StateFlow<List<Media>> {
        return mediaStateFlow.asStateFlow()
    }

    fun registerContentObserver() {
        getApplication<Application>().contentResolver.registerContentObserver(
            URI,
            true,
            contentObserver
        )
    }

    fun unregisterContentObserver() {
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }

    /**
     * Load all Images from contentResolver.
     *
     * Required Storage Permission
     */
    private fun loadMediaFromSDCard(): ArrayList<Media> {
        val cursor: Cursor?
        val mediaFiles = ArrayList<Media>()
        val context = getApplication<Application>().applicationContext

        val projection =
            arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.TITLE
            )

        cursor = context.contentResolver.query(
            AUDIO_URI,
            projection,
            null,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )

        val columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val columnIndexDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val columnIndexMimeType = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        val columnIndexTitle = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)

        while (cursor.moveToNext()) {
            val absolutePathOfImage = cursor.getString(columnIndexData)
            val dateAddedFormatted = formatTimeFromTimestamp(cursor.getLong(columnIndexDateAdded))
            val mimeType = cursor.getString(columnIndexMimeType)
            val title = cursor.getString(columnIndexTitle)
            val fileSize = formatFileSize(getFileSize(absolutePathOfImage))

            mediaFiles.add(
                Media(
                    path = absolutePathOfImage,
                    dateAdded = dateAddedFormatted,
                    mimeType = mimeType,
                    title = title,
                    size = fileSize
                )
            )
        }
        cursor.close()
        return mediaFiles
    }

    private fun formatTimeFromTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp * 1000) // 将时间戳转换为毫秒
        return dateFormat.format(date)
    }
    private fun formatFileSize(sizeInBytes: Long): String {
        val kilobytes = sizeInBytes / 1024.0
        val megabytes = kilobytes / 1024.0
        val gigabytes = megabytes / 1024.0

        return when {
            gigabytes >= 1.0 -> String.format("%.2f GB", gigabytes)
            megabytes >= 1.0 -> String.format("%.2f MB", megabytes)
            kilobytes >= 1.0 -> String.format("%.2f KB", kilobytes)
            else -> String.format("%d Bytes", sizeInBytes)
        }
    }

    private fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        if (file.exists() && file.isFile) {
            return file.length()
        }
        return 0
    }

    fun loadAllMediaFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            mediaStateFlow.update {
                loadMediaFromSDCard()
            }
        }
    }

    companion object {
        private val URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        private val AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }
}
