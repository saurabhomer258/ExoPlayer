package com.example.exoplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_TAKE_GALLERY_VIDEO = 1111;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_video_player.setOnClickListener {
            pickVideo();
        }


    }

    private fun pickVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java)
                val videoUri = data?.data.toString()
                intent.putExtra("uri", videoUri)
                startActivity(intent)

            }
        }
    }

}

