package com.example.audio;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class AudioRecordAndPlay {
	private static final String LOG_TAG = "AudioRecordTest";
	private String mFileName = null;

	private MediaRecorder mRecorder = null;

	private MediaPlayer mPlayer = null;
	private int RECORDING_TIME_LIMIT_SEC;

	public AudioRecordAndPlay(int RECORDING_TIME_LIMIT_SEC) {
		this.RECORDING_TIME_LIMIT_SEC = RECORDING_TIME_LIMIT_SEC * 1000;
		File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

		if (file != null && !file.exists()) {
			file.mkdirs();
		}

		mFileName = file.getAbsolutePath();
		file = new File(mFileName);
		mFileName += "/audiorecordtest.amr";
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public AudioRecordAndPlay(String mFileName) {
		this.mFileName = mFileName;
	}

	public void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	public void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopPlaying() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	private void startRecording() {
		try {

			mRecorder = new MediaRecorder();

			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			mRecorder.setOutputFile(mFileName);
			mRecorder.setMaxDuration(RECORDING_TIME_LIMIT_SEC);

			try {
				mRecorder.prepare();
			} catch (IOException e) {
				Log.e(LOG_TAG, "prepare() failed");
			}

			mRecorder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopRecording() {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	public void pause() {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

}
