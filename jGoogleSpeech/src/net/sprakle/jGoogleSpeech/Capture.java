package net.sprakle.jGoogleSpeech;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.sound.sampled.TargetDataLine;

class Capture extends Thread {
	Logger logger;

	// RMS of audio that must be above to begin recording
	private RecordThresholds thresholds;

	ByteArrayOutputStream out;
	TargetDataLine line;

	//An arbitrary-size temporary holding buffer
	byte tempBuffer[] = new byte[500];

	// used to check if speech is over
	boolean silence = false;
	long silenceTime = 0;
	long prevSilenceCheckTime;

	boolean capture = true;
	boolean record = false;

	ArrayList<CaptureObserver> observers;

	public Capture(Logger logger, TargetDataLine line, RecordThresholds thresholds) {
		this.logger = logger;
		this.line = line;
		this.thresholds = thresholds;

		observers = new ArrayList<CaptureObserver>();
		out = new ByteArrayOutputStream();
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
			float rms = calculateRMSLevel(tempBuffer);

			// start recording when speech is detected
			if (rms > thresholds.RECORD_START_RMS_THRESHOLD && !record) {
				record = true;
				logger.log("recording");
			}

			if (record) {

				// record speech
				out.write(tempBuffer, 0, cnt);

				if (shouldEnd(rms)) {
					capture = false;
				}
			}
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
				return true;
			}

			prevSilenceCheckTime = System.currentTimeMillis();
		}

		return false;
	}

	private float calculateRMSLevel(byte[] audioData)
	{
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