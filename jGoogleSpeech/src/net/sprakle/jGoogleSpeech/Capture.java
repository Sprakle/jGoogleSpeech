package net.sprakle.jGoogleSpeech;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.TargetDataLine;

class Capture extends Thread {
	private Logger logger;

	// RMS of audio that must be above to begin recording
	private RecordThresholds thresholds;

	private ByteArrayOutputStream out;
	private TargetDataLine line;

	// bytes the will be prepended to the record stream after triggering
	private ByteArrayOutputStream prebuffer;

	private int bytesToPrepend;

	//An arbitrary-size temporary holding buffer
	private byte tempBuffer[] = new byte[500];

	// used to check if speech is over
	private boolean silence = false;
	private long silenceTime = 0;
	private long prevSilenceCheckTime;

	private boolean capture = true;
	private boolean record = false;

	// the period of noise is after the RMS has just passed the threshold, but before it has triggered recording
	private long noiseStartTime;
	private boolean noise;

	private ArrayList<CaptureObserver> observers;

	Capture(Logger logger, TargetDataLine line, RecordThresholds thresholds, int bytesToPrepend) {
		this.logger = logger;
		this.line = line;
		this.thresholds = thresholds;
		this.bytesToPrepend = bytesToPrepend;

		observers = new ArrayList<CaptureObserver>();
		out = new ByteArrayOutputStream();

		prebuffer = new ByteArrayOutputStream();
	}

	@Override
	public void run() {

		try {
			while (capture) {
				capture();
			}

			updateObservers();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void capture() {
		//Read data from the internal buffer of
		// the data line.
		int cnt = line.read(tempBuffer, 0, tempBuffer.length);

		if (cnt > 0) {

			// records audio before the trigger, so it can be added to the start of the recoding
			prebuffer.write(tempBuffer, 0, cnt);

			float rms = calculateRMSLevel(tempBuffer);

			// if the RMS level passes the threshold, begin the timer to ensure it stays past for the required time
			if (rms > thresholds.RECORD_START_RMS_THRESHOLD && !record && !noise) {
				noise = true;
				noiseStartTime = System.currentTimeMillis();
			}

			// as long as the RMS is past the threshold
			if (noise) {

				// check if the RMS has been past the threshold for long enough yet
				long totalTime = System.currentTimeMillis() - noiseStartTime;
				if (totalTime > thresholds.RECORD_START_TIME_THRESHOLD) {

					// start recording
					record = true;
					noise = false;

					// takes recent noise and adds it to the record stream before recording
					prependNoise();

					logger.log(false, "Recording started. Prepended " + bytesToPrepend + " bytes.");
				}

				// check if the RMS has dropped below the threshold
				if (rms < thresholds.RECORD_START_RMS_THRESHOLD) {

					// cancel timer
					noise = false;
				}
			}

			if (record) {
				out.write(tempBuffer, 0, cnt);

				if (shouldEnd(rms)) {
					record = false;
					noise = false;
					capture = false;

					logger.log(false, "Recording ended");
				}
			}
		}
	}

	private void prependNoise() {
		byte[] preBufferArray = prebuffer.toByteArray();
		ByteArrayOutputStream preNoise = new ByteArrayOutputStream();

		int prependIndex = preBufferArray.length - bytesToPrepend;
		if (prependIndex < 0)
			prependIndex = 0;

		for (int i = prependIndex; i < preBufferArray.length; i++) {
			byte b = preBufferArray[i];
			preNoise.write(b);
		}

		try {
			preNoise.writeTo(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Boolean shouldEnd(float rms) {
		if (rms < thresholds.RECORD_END_RMS_THRESHOLD) {

			if (!silence) {
				silence = true;
				prevSilenceCheckTime = System.currentTimeMillis();
			}
		} else {
			silenceTime = 0;
		}

		if (silence) {
			long difference = System.currentTimeMillis() - prevSilenceCheckTime;
			silenceTime += difference;

			if (silenceTime > thresholds.RECORD_END_TIME_THRESHOLD) {
				silence = false;
				return true;
			}

			prevSilenceCheckTime = System.currentTimeMillis();
		}

		return false;
	}

	private float calculateRMSLevel(byte[] audioData) {

		// audioData might be buffered data read from a data line
		long lSum = 0;
		for (int i = 0; i < audioData.length; i++)
			lSum = lSum + audioData[i];

		double dAvg = lSum / audioData.length;

		double sumMeanSquare = 0d;
		for (int j = 0; j < audioData.length; j++)
			sumMeanSquare = sumMeanSquare + Math.pow(audioData[j] - dAvg, 2d);

		double averageMeanSquare = sumMeanSquare / audioData.length;
		return (float) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
	}

	public void addObserver(CaptureObserver observer) {
		observers.add(observer);
	}
	public void removeObserver(CaptureObserver observer) {
		observers.remove(observer);
	}

	private void updateObservers() {
		for (CaptureObserver co : observers) {
			co.update(out);
		}
	}
}