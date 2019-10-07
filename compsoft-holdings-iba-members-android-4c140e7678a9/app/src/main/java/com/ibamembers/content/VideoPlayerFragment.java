package com.ibamembers.content;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import com.ibamembers.R;
import com.ibamembers.app.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoPlayerFragment extends BaseFragment implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        MediaController.MediaPlayerControl, View.OnTouchListener {

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.video_surface)
    protected SurfaceView videoSurface;

    public static Bundle getVideoPlayerFragmentArgs(String videoUrl) {
        Bundle bundle = new Bundle();
        bundle.putString(VideoPlayerActivity.KEY_VIDEO_URL, videoUrl);
        return bundle;
    }

    private MediaPlayer mediaPlayer;
    private SurfaceHolder videoHolder;
    private String videoStringUrl;
    private MediaController mediaController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.video_player_fragment, container, false);
        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        if (args != null) {
            videoStringUrl = args.getString(VideoPlayerActivity.KEY_VIDEO_URL);
            prepareVideo();
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void prepareVideo() {
        videoSurface.setOnTouchListener(this);
        videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mediaController.show();
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(videoHolder);
            mediaPlayer.setDataSource(videoStringUrl);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaController = new MediaController(getActivity());
        }
        catch(Exception e){
            e.printStackTrace();
            showOKErrorDialog(getString(R.string.content_detail_fail_to_load_media), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            });
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(videoSurface);
        new Handler().post(new Runnable() {

            public void run() {
                mediaController.setEnabled(true);
                mediaController.show();
                mediaPlayer.start();
            }
        });
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
