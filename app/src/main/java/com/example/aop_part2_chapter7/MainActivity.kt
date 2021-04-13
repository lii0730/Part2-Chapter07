package com.example.aop_part2_chapter7

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.visualizer.amplitude.AudioRecordView
import java.util.*

class MainActivity : AppCompatActivity() {

//    private val Reco : RecordButton by lazy {
//        findViewById(R.id.Reco)
//    }

    private val audioRecordView: AudioRecordView by lazy {
        findViewById(R.id.audioRecordView)
    }

    private val timeTextView: TextView by lazy {
        findViewById(R.id.timeTextView)
    }

    private val recordButton: AppCompatButton by lazy {
        findViewById(R.id.recordButton)
    }

    private val resetButton: Button by lazy {
        findViewById(R.id.resetButton)
    }

    private var timer: CountUpTimer? = null

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var filename: String = ""

    private var state = currentState.BEFORE_RECORDING

    private val PERMISSIONS_REQUIRED_CODE = 100 // RECORD_AUDIO Permission 체크

    private lateinit var drawTimer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        filename = "${externalCacheDir?.absolutePath}/recording.3gp"
        getPermission()
    }


    fun recordButtonClicked(view: View) {
        //TODO: 버튼 클릭시 녹음 시작
        //TODO: 카운트 업 -> timeTextView 초마다 갱신
        //TODO: 녹음중 버튼 클릭 시 녹음 중지 및 reset 버튼 표시
        //TODO: 녹음된 내용 재생

        if (view.id == R.id.recordButton) {
            when (state) {
                currentState.BEFORE_RECORDING -> {
                    startRecord()
                }
                currentState.ON_RECORDING -> {
                    stopRecord()
                }
                currentState.AFTER_RECORDING -> {
                    playAudio()
                }
                currentState.ON_PLAYING -> {
                    stopAudio()

                }
            }
        }
    }

    private fun startRecord() {
        resetButton.isVisible = false

        //TODO: create recorder
        recorder = createMediaRecorder()
        recorder?.start()

        //TODO: 경과 시간 표출 매 초마다 갱신
        updateTextView()

        state = currentState.ON_RECORDING

        startVisualizing()
        recordButton.background =
            ResourcesCompat.getDrawable(resources, R.drawable.on_record_layout, null)
        Toast.makeText(this, "녹음 시작", Toast.LENGTH_SHORT).show()
    }

    private fun createMediaRecorder(): MediaRecorder {
        val tmpRecorder = MediaRecorder()
            .apply {
                //TODO: Recorder 속성 초기화
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(filename)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                try {
                    prepare()
                    // prepare Async -> 비동기 진행
                } catch (e: Exception) {
                    Log.i("AudioRecord", "prepare() Error" + e.toString())
                }
            }

        return tmpRecorder
    }

    private fun stopRecord() {
        // TODO : 녹음 중지
        recorder?.run {
            stop()
            release()
        }
        recorder = null
        timer?.stop()

        state = currentState.AFTER_RECORDING
        resetButton.isVisible = true

        stopVisualizing()
        recordButton.background =
            ResourcesCompat.getDrawable(resources, R.drawable.after_record_layout, null)
        Toast.makeText(this, "녹음 중지", Toast.LENGTH_SHORT).show()
    }

    private fun playAudio() {
        //TODO: 녹음 내용 재생
        player = MediaPlayer()
            .apply {
                setDataSource(filename)
                prepare()
            }

        player?.start()

        state = currentState.ON_PLAYING
        recordButton.background =
            ResourcesCompat.getDrawable(resources, R.drawable.on_playing_layout, null)
        Toast.makeText(this, "재생 시작", Toast.LENGTH_SHORT).show()

        player?.setOnCompletionListener {
            //TODO: 미디어 끝에 도달 했을 경우
            stopAudio()
        }
    }

    private fun stopAudio() {
        player?.release()
        player = null

        state = currentState.AFTER_RECORDING
        Toast.makeText(this, "재생 중지", Toast.LENGTH_SHORT).show()

        recordButton.background =
            ResourcesCompat.getDrawable(resources, R.drawable.after_record_layout, null)
    }


    fun resetButtonClicked(view: View) {
        //TODO: 녹음된 내용 reset
        //TODO: 녹음 진행 시에는 reset Button invisible

        when (view.id) {
            R.id.resetButton -> {
                resetRecord()
            }
        }
    }

    private fun resetRecord() {
        //TODO : 녹음내용 초기화
        stopAudio()
        resetButton.isVisible = false

        resetTimer()
        timeTextView.text = "%02d:%02d".format(0, 0)
        state = currentState.BEFORE_RECORDING

        recordButton.background =
            ResourcesCompat.getDrawable(resources, R.drawable.before_record_layout, null)
        Toast.makeText(this, "초기화", Toast.LENGTH_SHORT).show()
    }

    private fun updateTextView() {
        //TODO: 경과 시간 갱신
        timer = object : CountUpTimer(1000L) {
            override fun onTick(elapsedTime: Long) {
                val min = (elapsedTime / 1000) / 60
                val sec = (elapsedTime / 1000) % 60

                runOnUiThread {
                    timeTextView.text = "%02d:%02d".format(min, sec)
                }
            }
        }
        timer?.start()
    }

    private fun resetTimer() {
        timer?.reset()
        timer = null
        audioRecordView.recreate()
    }

    private fun startVisualizing() {
        drawTimer = Timer()
        drawTimer.schedule(object : TimerTask() {
            override fun run() {
                val currentMaxAmplitude = recorder?.maxAmplitude
                audioRecordView.update(currentMaxAmplitude ?: 0)
            }
        }, 0, 100)
    }

    private fun stopVisualizing() {
        drawTimer.cancel()
    }

    private fun getPermission() {
        //TODO: Permission 요청
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                )
            ) {
                //TODO: 교육용 UI 제공
                showPopUpDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    PERMISSIONS_REQUIRED_CODE
                )
            }

        } else {
            //TODO: 권한을 부여 받음
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUIRED_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO: 권한 허용

                } else {
                    //TODO: 권한 거부
                    Toast.makeText(this, "권한을 거부하였습니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showPopUpDialog() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("녹음기 앱에서 마이크를 사용하기 위해 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    PERMISSIONS_REQUIRED_CODE
                )
            }
            .setNegativeButton("취소하기") { _, _ ->
                finish()
            }
            .create()
            .show()
    }
}