package com.example.exoplayer

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.TrackSelectionView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util


class VideoPlayerActivity : Activity() {
    private val playerView: PlayerView by lazy { findViewById<PlayerView>(R.id.player_view) }
    private lateinit var videoUri: String
    private val progressBar: ProgressBar by lazy { findViewById<ProgressBar>(R.id.progress_bar) }
    private lateinit var videoControl: VideoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        val intent = intent
        videoUri = intent.extras!!.getString("uri")
        videoControl= VideoPlayer.with(this,videoUri,progressBar)
    }


    public override fun onResume() {
        super.onResume()
            videoControl.initializePlayer(playerView);
    }

    public override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) videoControl.releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) videoControl.releasePlayer()
    }
    public override fun onBackPressed() {
        super.onBackPressed()
        videoControl.releasePlayer()
    }
}