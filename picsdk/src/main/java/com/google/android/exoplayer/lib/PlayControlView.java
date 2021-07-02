package com.google.android.exoplayer.lib;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.picsdk.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.view.CSeekBar;
import java.util.Formatter;
import java.util.Locale;

/**
 * A view to control video playback of an {@link ExoPlayer}.
 */
public class PlayControlView extends FrameLayout {

  /**
   * Listener to be notified about changes of the visibility of the UI control.
   */
  public interface VisibilityListener {

    void onVisibilityChange(boolean visible);
  }

  public interface PlayPauseClickListener {

    void playPauseClicked();
  }

  public interface SeekListener {
    void seekTo(long position);
  }

  public static final int DEFAULT_FAST_FORWARD_MS = 15000;
  public static final int DEFAULT_REWIND_MS = 5000;
  public static final int DEFAULT_SHOW_DURATION_MS = 5000;

  private static final int PROGRESS_BAR_MAX = 1000;
  private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

  private final ComponentListener componentListener;
  private final ImageView playButton;
  private final View bottomBar;
  private final TextView time;
  private final TextView timeCurrent;
  private final CSeekBar progressBar;
  private final ImageView fullscreenView;
  private final ProgressView progressView;
  private final StringBuilder formatBuilder;
  private final Formatter formatter;
  private final Timeline.Window currentWindow;
  private long defaultDuration;

  private ExoPlayer player;
  private VisibilityListener visibilityListener;
  private PlayPauseClickListener playPauseClickListener;
  private SeekListener seekListener;

  private boolean dragging;
  private int rewindMs = DEFAULT_REWIND_MS;
  private int fastForwardMs = DEFAULT_FAST_FORWARD_MS;
  private int showDurationMs = DEFAULT_SHOW_DURATION_MS;

  private final Runnable updateProgressAction = new Runnable() {
    @Override
    public void run() {
      updateProgress();
    }
  };

  private final Runnable hideAction = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };

  public PlayControlView(Context context) {
    this(context, null);
  }

  public PlayControlView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayControlView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    currentWindow = new Timeline.Window();
    formatBuilder = new StringBuilder();
    formatter = new Formatter(formatBuilder, Locale.getDefault());
    componentListener = new ComponentListener();

    LayoutInflater.from(context).inflate(R.layout.play_control_view, this);
    time = findViewById(R.id.time);
    timeCurrent = findViewById(R.id.time_current);
    progressBar = findViewById(R.id.mediacontroller_progress);
    progressBar.setOnSeekBarChangeListener(componentListener);
    progressBar.setMax(PROGRESS_BAR_MAX);
    playButton = findViewById(R.id.play);
    playButton.setOnClickListener(componentListener);
    fullscreenView = findViewById(R.id.fullScreenView);
    progressView = findViewById(R.id.progress_view);
    progressView.setMax(PROGRESS_BAR_MAX);
    bottomBar = findViewById(R.id.bottom_bar);
    updateAll();
  }

  /**
   * Sets the {@link ExoPlayer} to control.
   *
   * @param player the {@code ExoPlayer} to control.
   */
  public void setPlayer(ExoPlayer player) {
    if (this.player == player) {
      return;
    }
    if (this.player != null) {
      this.player.removeListener(componentListener);
    }
    this.player = player;
    if (player != null) {
      player.addListener(componentListener);
    }
    updateAll();
  }

  /**
   * Sets the {@link VisibilityListener}.
   *
   * @param listener The listener to be notified about visibility changes.
   */
  public void setVisibilityListener(VisibilityListener listener) {
    this.visibilityListener = listener;
  }

  public void setFullScreenClickListener(OnClickListener listener) {
    fullscreenView.setOnClickListener(listener);
  }

  public void setPlayPauseClickListener(PlayPauseClickListener listener) {
    this.playPauseClickListener = listener;
  }

  public void setSeekListener(SeekListener seekListener) {
    this.seekListener = seekListener;
  }

  /**
   * Sets the rewind increment in milliseconds.
   *
   * @param rewindMs The rewind increment in milliseconds.
   */
  public void setRewindIncrementMs(int rewindMs) {
    this.rewindMs = rewindMs;
  }

  /**
   * Sets the fast forward increment in milliseconds.
   *
   * @param fastForwardMs The fast forward increment in milliseconds.
   */
  public void setFastForwardIncrementMs(int fastForwardMs) {
    this.fastForwardMs = fastForwardMs;
  }

  /**
   * Sets the duration to show the playback control in milliseconds.
   *
   * @param showDurationMs The duration in milliseconds.
   */
  public void setShowDurationMs(int showDurationMs) {
    this.showDurationMs = showDurationMs;
  }

  /**
   * Shows the controller for the duration last passed to {@link #setShowDurationMs(int)}, or for
   * {@link #DEFAULT_SHOW_DURATION_MS} if {@link #setShowDurationMs(int)} has not been called.
   */
  public void show() {
    show(showDurationMs);
  }

  /**
   * Shows the controller for the {@code durationMs}. If {@code durationMs} is 0 the controller is
   * shown until {@link #hide()} is called.
   *
   * @param durationMs The duration in milliseconds.
   */
  public void show(int durationMs) {
    playButton.setVisibility(VISIBLE);
    bottomBar.setVisibility(VISIBLE);
    progressView.setVisibility(GONE);
    if (visibilityListener != null) {
      visibilityListener.onVisibilityChange(true);
    }
    updateAll();
    setShowDurationMs(durationMs);
    hideDeferred();
  }

  public void toggleFullScreen(boolean isFullScreen) {
    fullscreenView
        .setImageResource(isFullScreen ? R.drawable.shrink_video : R.drawable.enlarge_video);
  }

  public void setFullScreenViewVisibility(int visibility) {
    fullscreenView.setVisibility(visibility);
  }

  public void setDefaultDuration(long defaultDuration) {
    this.defaultDuration = defaultDuration;
  }

  /**
   * Hides the controller.
   */
  public void hide() {
    playButton.setVisibility(GONE);
    bottomBar.setVisibility(GONE);
    progressView.setVisibility(VISIBLE);
    if (visibilityListener != null) {
      visibilityListener.onVisibilityChange(false);
    }
    //removeCallbacks(updateProgressAction);
    removeCallbacks(hideAction);
  }

  /**
   * Returns whether the controller is currently visible.
   */
  public boolean isVisible() {
    return playButton.getVisibility() == VISIBLE;
  }

  private void hideDeferred() {
    removeCallbacks(hideAction);
    if (showDurationMs > 0) {
      postDelayed(hideAction, showDurationMs);
    }
  }

  private void updateAll() {
    updatePlayPauseButton();
    updateNavigation();
    updateProgress();
  }

  private void updatePlayPauseButton() {
    if (!isVisible()) {
      return;
    }
    boolean playing = player != null && player.getPlayWhenReady() && player.getPlaybackState() == ExoPlayer.STATE_READY;
    playButton.setImageResource(
        playing ? R.drawable.new_pause_video : R.drawable.new_play_video);
  }

  private void updateNavigation() {
    if (!isVisible()) {
      return;
    }
    Timeline currentTimeline = player != null ? player.getCurrentTimeline() : null;
    boolean haveNonEmptyTimeline = currentTimeline != null && !currentTimeline.isEmpty();
    boolean isSeekable = false;
    if (haveNonEmptyTimeline) {
      int currentWindowIndex = player.getCurrentWindowIndex();
      currentTimeline.getWindow(currentWindowIndex, currentWindow);
      isSeekable = currentWindow.isSeekable;
    }
    if (progressBar != null) {
      progressBar.setEnabled(isSeekable);
    }
  }

  private void updateProgress() {
//        if (!isVisible()) {
//            return;
//        }
    long duration = player == null ? 0 : player.getDuration();
    long position = player == null ? 0 : player.getCurrentPosition();
    if (duration <= 0) {
      duration = defaultDuration;
    }
    time.setText(stringForTime(duration));
    if (!dragging) {
      timeCurrent.setText(stringForTime(position));
    }
    if (!dragging) {
      int p = progressBarValue(position);
      progressBar.setProgress(p);
      progressView.setProgress(p);
    }
    long bufferedPosition = player == null ? 0 : player.getBufferedPosition();
    progressBar.setSecondaryProgress(progressBarValue(bufferedPosition));
    // Remove scheduled updates.
    removeCallbacks(updateProgressAction);
    // Schedule an update if necessary.
    int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();
    if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
      long delayMs = 100;
      postDelayed(updateProgressAction, delayMs);
    }
  }

  private String stringForTime(long timeMs) {
    if (timeMs == C.TIME_UNSET) {
      timeMs = 0;
    }
    long totalSeconds = (timeMs + 500) / 1000;
    long seconds = totalSeconds % 60;
    long minutes = (totalSeconds / 60) % 60;
    long hours = totalSeconds / 3600;
    formatBuilder.setLength(0);
    return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        : formatter.format("%02d:%02d", minutes, seconds).toString();
  }

  private int progressBarValue(long position) {
    long duration = player == null ? C.TIME_UNSET : player.getDuration();
    return duration == C.TIME_UNSET || duration == 0 ? 0
        : (int) ((position * PROGRESS_BAR_MAX) / duration);
  }

  private long positionValue(int progress) {
    long duration = player == null ? C.TIME_UNSET : player.getDuration();
    return duration == C.TIME_UNSET ? 0 : ((duration * progress) / PROGRESS_BAR_MAX);
  }

  void rewind() {
    doSeek(Math.max(player.getCurrentPosition() - rewindMs, 0));
  }

  void fastForward() {
    doSeek(Math.min(player.getCurrentPosition() + fastForwardMs, player.getDuration()));
  }

  private void doSeek(long position) {
    if (seekListener != null) {
      seekListener.seekTo(position);
    } else {
      player.seekTo(position);
    }
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (player == null || event.getAction() != KeyEvent.ACTION_DOWN) {
      return super.dispatchKeyEvent(event);
    }
    switch (event.getKeyCode()) {
      case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        fastForward();
        break;
      case KeyEvent.KEYCODE_MEDIA_REWIND:
      case KeyEvent.KEYCODE_DPAD_LEFT:
        rewind();
        break;
      case KeyEvent.KEYCODE_MEDIA_PLAY:
        player.setPlayWhenReady(true);
        break;
      case KeyEvent.KEYCODE_MEDIA_PAUSE:
        player.setPlayWhenReady(false);
        break;
      default:
        return false;
    }
    show();
    return true;
  }

  private final class ComponentListener implements Player.EventListener,
      SeekBar.OnSeekBarChangeListener, OnClickListener {

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      removeCallbacks(hideAction);
      dragging = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (fromUser) {
        timeCurrent.setText(stringForTime(positionValue(progress)));
      }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      dragging = false;
      doSeek(positionValue(seekBar.getProgress()));
      hideDeferred();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
      updatePlayPauseButton();
      updateProgress();
    }

    @Override
    public void onPositionDiscontinuity() {
      updateNavigation();
      updateProgress();
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
      updateNavigation();
      updateProgress();
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
      // Do nothing.
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
      // Do nothing.
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onClick(View view) {
      if (playButton == view) {
        //player.setPlayWhenReady(!player.getPlayWhenReady());
        if (playPauseClickListener != null) {
          playPauseClickListener.playPauseClicked();
        }
      }
      setShowDurationMs(DEFAULT_SHOW_DURATION_MS);
      hideDeferred();
    }

  }

  public void setProgressData(boolean[] playTags) {
    Drawable drawable = getResources().getDrawable(R.drawable.player_seekbar_watched_bg);
    progressBar.setProgressDrawable(drawable);
    progressBar.setProgressData(playTags);
    progressView.setProgressData(playTags);
  }
}
