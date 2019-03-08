package com.example.exoplayer

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util

class VideoPlayer : Player.DefaultEventListener() {
    private var player: ExoPlayer? = null
    private lateinit var mUri: String
    private lateinit var mContext: Context
    private var shouldAutoPlay: Boolean = false
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
    private var trackSelector: DefaultTrackSelector? = null
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    private var currentWindow: Int = 0

    companion object {
        fun with(context: Context, uri: String, progressBar: ProgressBar): VideoPlayer {
            var videoPlayer = VideoPlayer()
            return videoPlayer.setData(context, uri,progressBar)

        }
    }

    private lateinit var mProgressBar: ProgressBar
    private fun setData(context: Context, uri: String, progressBar: ProgressBar): VideoPlayer {
        mUri = uri
        mContext = context
        mProgressBar = progressBar
        shouldAutoPlay = true
        mediaDataSourceFactory = DefaultDataSourceFactory(
            mContext,
            Util.getUserAgent(mContext, "mediaPlayerSample"),
            bandwidthMeter as TransferListener<in DataSource>
        )
        return this
    }
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_IDLE) {
            mProgressBar.visibility = View.VISIBLE
        }
        if (playbackState == Player.STATE_BUFFERING)
        {
            mProgressBar.visibility = View.VISIBLE

        }
        if (playbackState == Player.STATE_READY)
        {
            mProgressBar.visibility = View.GONE
        }
        if (playbackState == Player.STATE_ENDED)
        {
            mProgressBar.visibility = View.GONE
        }
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        // The video tracks are no supported in this device.
        if (trackGroups !== lastSeenTrackGroupArray) {
            val mappedTrackInfo = trackSelector!!.currentMappedTrackInfo
            if (mappedTrackInfo != null) {
                if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                    Toast.makeText(mContext, "Error unsupported track", Toast.LENGTH_SHORT).show()
                }
            }
            lastSeenTrackGroupArray = trackGroups
        }
    }


    private var playbackPosition: Long = 0

    public fun initializePlayer(playerView: PlayerView) {
        playerView.requestFocus()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        lastSeenTrackGroupArray = null
        player = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector)
        playerView.player = player
        with(player!!) {
            addListener(this@VideoPlayer)
            playWhenReady = shouldAutoPlay
        }

        val mediaSource = ExtractorMediaSource
            .Factory(mediaDataSourceFactory)
            .createMediaSource(Uri.parse(mUri))

        val haveStartPosition = currentWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player!!.seekTo(currentWindow, playbackPosition)
        }

        player!!.prepare(mediaSource, !haveStartPosition, false)

    }



    public fun releasePlayer() {
        if (player != null) {
            updateStartPosition()
            player!!.release()
            player = null
            trackSelector = null
        }
    }

    private fun updateStartPosition() {
        with(player!!) {
            playbackPosition = currentPosition
            currentWindow = currentWindowIndex
            shouldAutoPlay=playWhenReady
            playWhenReady = playWhenReady
        }
    }
}