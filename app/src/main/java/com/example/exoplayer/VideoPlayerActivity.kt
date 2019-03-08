package com.example.exoplayer

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video_player.*

class VideoPlayerActivity : Activity() {

    companion object {
        private const val KEY_PLAY_WHEN_READY = "play_when_ready"
        private const val KEY_WINDOW = "window"
        private const val KEY_POSITION = "position"
    }

    private var player: ExoPlayer? = null
    private var shouldAutoPlay: Boolean = true
    private var trackSelector: DefaultTrackSelector? = null
    private var lastSeenTrackGroupArray: TrackGroupArray? = null
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()

    private var playWhenReady: Boolean = true
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0

    private lateinit var  videoUri:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        val intent = intent
        videoUri = intent.extras.getString("uri")

        if (savedInstanceState != null) {

            with(savedInstanceState) {
                playWhenReady = getBoolean(KEY_PLAY_WHEN_READY)
                currentWindow = getInt(KEY_WINDOW)
                playbackPosition = getLong(KEY_POSITION)
            }
        }
        shouldAutoPlay = playWhenReady
        mediaDataSourceFactory = DefaultDataSourceFactory(this,
            Util.getUserAgent(this, "mediaPlayerSample"),
            bandwidthMeter as TransferListener<in DataSource>
        )
    }

    public override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23 || player == null) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }



    override fun onSaveInstanceState(outState: Bundle) {
        updateStartPosition()

        with(outState) {
            putBoolean(KEY_PLAY_WHEN_READY,shouldAutoPlay)
            putInt(KEY_WINDOW, currentWindow)
            putLong(KEY_POSITION, playbackPosition)
        }

        super.onSaveInstanceState(outState)
    }


    private fun initializePlayer() {

        player_view.requestFocus()

        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)

        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        lastSeenTrackGroupArray = null

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)

        player_view.player = player

        with(player!!) {
            addListener(PlayerEventListener())
            playWhenReady = shouldAutoPlay
        }

        // Use Hls, Dash or other smooth streaming media source if you want to test the track quality selection.
        /*val mediaSource: MediaSource = HlsMediaSource(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"),
                mediaDataSourceFactory, mainHandler, null)*/

        val mediaSource = ExtractorMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(Uri.parse(videoUri))

        val haveStartPosition = currentWindow != C.INDEX_UNSET
        if (haveStartPosition) {
            player!!.seekTo(currentWindow, playbackPosition)
        }

        player!!.prepare(mediaSource, !haveStartPosition, false)


    }

    private fun releasePlayer() {
        if (player != null) {
            updateStartPosition()
            shouldAutoPlay = player!!.playWhenReady
            player!!.release()
            player = null
            trackSelector = null
        }
    }

    private fun updateStartPosition() {

        with(player!!) {
            playbackPosition = currentPosition
            currentWindow = currentWindowIndex
            playWhenReady = playWhenReady
        }
    }



    private inner class PlayerEventListener : Player.DefaultEventListener() {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playbackState == Player.STATE_IDLE) {

                progress_bar.visibility = View.VISIBLE
            }
            if (playbackState == Player.STATE_BUFFERING)
            {

                progress_bar.visibility = View.VISIBLE

            }
            if (playbackState == Player.STATE_READY)
            {
               shouldAutoPlay=playWhenReady
                progress_bar.visibility = View.GONE
            }
            if (playbackState == Player.STATE_ENDED)
            {

                progress_bar.visibility = View.GONE
            }
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

            // The video tracks are no supported in this device.
            if (trackGroups !== lastSeenTrackGroupArray) {
                val mappedTrackInfo = trackSelector!!.currentMappedTrackInfo
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        Toast.makeText(this@VideoPlayerActivity, "Error unsupported track", Toast.LENGTH_SHORT).show()
                    }
                }
                lastSeenTrackGroupArray = trackGroups
            }
        }
    }

}