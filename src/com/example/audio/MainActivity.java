package com.example.audio;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;

import ui.Shimmer;
import ui.ShimmerTextView;
import ui.ViewProxy;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView tvPlay;
    private TextView recordTimeText;
    private ImageButton audioSendButton;
    private View recordPanel;
    private View slideText;
    private float startedDraggingX = -1;
    private final int DEFAULT_X_CAN_MOVE = dp(200);
    private float distCanMove = DEFAULT_X_CAN_MOVE;
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private Timer timer;
    private AudioRecordAndPlay audioRecordAndPlay;
    private ShimmerTextView shimmerTextView;
    private Shimmer shimmer = null;
    private boolean isRecordingCancelled = false;

    public static final int RECORDING_TIME_LIMIT_SEC = 30;

    private ImageView ivMic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	audioRecordAndPlay = new AudioRecordAndPlay(RECORDING_TIME_LIMIT_SEC);
	ivMic = (ImageView) findViewById(R.id.iv_mic);
	tvPlay = (TextView) findViewById(R.id.tv_play);
	recordPanel = findViewById(R.id.record_panel);
	recordTimeText = (TextView) findViewById(R.id.recording_time_text);
	slideText = findViewById(R.id.slideText);
	audioSendButton = (ImageButton) findViewById(R.id.chat_audio_send_button);
	shimmerTextView = (ShimmerTextView) findViewById(R.id.slideToCancelTextView);
	shimmer = new Shimmer();

	audioSendButton.setOnTouchListener(new View.OnTouchListener() {
	    @Override
	    public boolean onTouch(View view, MotionEvent motionEvent) {
		view.performClick();
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
		    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
			    .getLayoutParams();
		    params.leftMargin = dp(30);
		    slideText.setLayoutParams(params);
		    slideText.setVisibility(View.VISIBLE);
		    // ViewProxy.setAlpha(slideText, 1);
		    startedDraggingX = -1;
		    startRecording();
		    audioSendButton.getParent()
			    .requestDisallowInterceptTouchEvent(true);
		    recordPanel.setVisibility(View.VISIBLE);
		} else if (motionEvent.getAction() == MotionEvent.ACTION_UP
			|| motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
		    startedDraggingX = -1;
		    stopRecording();
		} else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
		    float x = motionEvent.getX();
		    if (!isRecordingCancelled && x < -distCanMove) {
			isRecordingCancelled = true;
			stopRecording();
		    }
		    x = x + ViewProxy.getX(audioSendButton);
		    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
			    .getLayoutParams();
		    if (startedDraggingX != -1) {
			float dist = (x - startedDraggingX);
			params.leftMargin = dp(30) + (int) dist;
			slideText.setLayoutParams(params);
			// float alpha = 1.0f + dist / distCanMove;
			// if (alpha > 1) {
			// alpha = 1;
			// } else if (alpha < 0) {
			// alpha = 0F;
			// }
			// ViewProxy.setAlpha(slideText, alpha);
		    }

		    if (x <= ViewProxy.getX(slideText) + slideText.getWidth()
			    + dp(90)) {
			if (startedDraggingX == -1) {
			    startedDraggingX = x;
			    distCanMove = (recordPanel.getMeasuredWidth()
				    - slideText.getMeasuredWidth() - dp(48)) / 2.0f;
			    if (distCanMove <= 0) {
				distCanMove = DEFAULT_X_CAN_MOVE;
			    } else if (distCanMove > DEFAULT_X_CAN_MOVE) {
				distCanMove = DEFAULT_X_CAN_MOVE;
			    }
			}
		    }
		    if (params.leftMargin > dp(30)) {
			params.leftMargin = dp(30);
			slideText.setLayoutParams(params);
			// ViewProxy.setAlpha(slideText, 1);
			startedDraggingX = -1;
		    }
		}
		view.onTouchEvent(motionEvent);
		return true;
	    }
	});
	tvPlay.setOnClickListener(new OnClickListener() {
	    boolean mStartPlaying = true;

	    @Override
	    public void onClick(View v) {
		audioRecordAndPlay.onPlay(mStartPlaying);
		if (mStartPlaying) {
		    tvPlay.setText("Stop playing");
		} else {
		    tvPlay.setText("Start playing");
		}
		mStartPlaying = !mStartPlaying;

	    }
	});

    }

    private void startRecording() {
	isRecordingCancelled = false;
	shimmer.start(shimmerTextView);
	audioRecordAndPlay.onRecord(true);
	startTime = SystemClock.uptimeMillis();
	timer = new Timer();
	MyTimerTask myTimerTask = new MyTimerTask();
	timer.schedule(myTimerTask, 1000, 1000);
	vibrate();
    }

    private void stopRecording() {
	slideText.setVisibility(View.GONE);
	shimmer.cancel();
	audioRecordAndPlay.onRecord(false);
	if (timer != null) {
	    timer.cancel();
	    timer.purge();
	    timer = null;
	}
	// if (recordTimeText.getText().toString().equals("00:00")) {
	// return;
	// }
	if (isRecordingCancelled) {
	    recordTimeText.setText("00:00");
	    vibrate();
	}
	YoYo.with(Techniques.My).duration(700)
		.interpolate(new DecelerateInterpolator())
		.withListener(new AnimatorListener() {
		    @Override
		    public void onAnimationStart(Animator arg0) {
			Toast.makeText(getApplicationContext(),
				"animation start", Toast.LENGTH_SHORT).show();
		    }

		    @Override
		    public void onAnimationRepeat(Animator arg0) {

		    }

		    @Override
		    public void onAnimationEnd(Animator arg0) {
			Toast.makeText(getApplicationContext(),
				"animation end", Toast.LENGTH_SHORT).show();

		    }

		    @Override
		    public void onAnimationCancel(Animator arg0) {
			Toast.makeText(getApplicationContext(), "animation cancel", Toast.LENGTH_SHORT).show();

		    }
		}).playOn(ivMic);

    }

    private void startAnim() {
	PropertyValuesHolder translateY = PropertyValuesHolder.ofFloat(
		"translationY", 0, -dp(200), 0);
	ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(ivMic,
		translateY);
	anim.setInterpolator(new DecelerateInterpolator());
	anim.setDuration(800);
	anim.start();
    }

    private void vibrate() {
	try {
	    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    v.vibrate(200);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static int dp(float value) {
	return (int) Math.ceil(1 * value);
    }

    class MyTimerTask extends TimerTask {

	@Override
	public void run() {
	    timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
	    updatedTime = timeSwapBuff + timeInMilliseconds;
	    final String hms = String.format(
		    "%02d:%02d",
		    TimeUnit.MILLISECONDS.toMinutes(updatedTime)
			    - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
				    .toHours(updatedTime)),
		    TimeUnit.MILLISECONDS.toSeconds(updatedTime)
			    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
				    .toMinutes(updatedTime)));
	    final long lastsec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
		    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
			    .toMinutes(updatedTime));
	    System.out.println(lastsec + " hms " + hms);
	    runOnUiThread(new Runnable() {

		@Override
		public void run() {
		    try {
			if (timer != null && recordTimeText != null) {
			    recordTimeText.setText(hms);
			}
			if (lastsec >= RECORDING_TIME_LIMIT_SEC) {
			    stopRecording();
			    // send recording
			}
		    } catch (Exception e) {

		    }

		}
	    });
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// Handle action bar item clicks here. The action bar will
	// automatically handle clicks on the Home/Up button, so long
	// as you specify a parent activity in AndroidManifest.xml.
	int id = item.getItemId();
	if (id == R.id.action_settings) {
	    return true;
	}
	return super.onOptionsItemSelected(item);
    }

}
