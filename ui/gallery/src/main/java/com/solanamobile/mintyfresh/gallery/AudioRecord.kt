package com.solanamobile.mintyfresh.gallery

import android.annotation.SuppressLint
import android.os.Environment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hernandazevedo.androidmp3recorder.MP3Recorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "AudioRecord"
private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
private var mp3Recorder: MP3Recorder? = null
private var filePath = ""
private var isRecording = false

private fun stopTimer(scope: CoroutineScope) {
    scope.coroutineContext.cancelChildren()
}

private fun startTimer(updateTime: () -> Unit) = CoroutineScope(Dispatchers.Default).launch {
    while (true) {
        updateTime()
        delay(1000)
    }
}

private fun handleMp3Recording() {
    if(!isRecording){
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        filePath =  Environment.getExternalStorageDirectory().absolutePath + "/" + Environment.DIRECTORY_RINGTONES+"/${name}.mp3"
        mp3Recorder = MP3Recorder(File(filePath))
        mp3Recorder?.start()
        isRecording = true
    }
    else {
        mp3Recorder?.stop()
        isRecording = false
    }
}

@Composable
private fun Timer(timeElapsed: Int) {
    Text(
        text =  stringResource(R.string.audio_duration) +" $timeElapsed seconds",
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 16.dp),
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartRecord(navigateToDetails: (String) -> Unit = { },) {
    var timeElapsed by remember { mutableStateOf(0) }
    var isRecordRunning by remember { mutableStateOf(false) }
    var job: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.record_title)) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Timer(timeElapsed)

            // Buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                                job = startTimer { timeElapsed++ }
                                handleMp3Recording()
                                isRecordRunning = isRecording
                              },
                    modifier = Modifier.weight(1f).padding(8.dp),
                    enabled = !isRecordRunning
                ) {
                    Text(text =  stringResource(R.string.record_start))
                }

                Button(
                    onClick = {
                                stopTimer(scope)
                                handleMp3Recording()
                                isRecordRunning = isRecording
                                navigateToDetails(filePath)
                              },
                    modifier = Modifier.weight(1f).padding(8.dp),
                    enabled = isRecordRunning
                ) {
                    Text(text =  stringResource(R.string.record_stop))
                }
            }
        }
    }
}