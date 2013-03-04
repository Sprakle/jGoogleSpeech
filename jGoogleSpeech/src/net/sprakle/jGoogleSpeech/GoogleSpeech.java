package net.sprakle.jGoogleSpeech;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class GoogleSpeech implements CaptureObserver {
	private String speech;

	private AudioFormat format;
	private DataLine.Info info;

	private TargetDataLine line;

	private Capture capture;
	private RecordThresholds thresholds;

	private final int SAMPLE_RATE;
	private final int BIT_RATE = 8;

	private Flac flac;
	private GoogleSubmit googleSubmit;

	private Logger logger;

	public GoogleSpeech(Logger logger, RecordThresholds thresholds, int SAMPLE_RATE) {
		this.logger = logger;
		this.thresholds = thresholds;
		this.SAMPLE_RATE = SAMPLE_RATE;

		// setup encoder and speech recognition engine
		flac = new Flac(logger, SAMPLE_RATE, BIT_RATE);
		googleSubmit = new GoogleSubmit(logger, "http://www.google.com/speech-api/v1/recognize?lang=en&client=chromium");

		// setup microphone
		format = new AudioFormat(SAMPLE_RATE, BIT_RATE, 1, true, true);
		info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object

		if (!AudioSystem.isLineSupported(info)) {
			logger.log("Line not supported");
		}

		// Obtain and open microphone line.
		try {
			line = AudioSystem.getTargetDataLine(format);
			line.open(format);
		} catch (LineUnavailableException ex) {
			logger.log("line unavailable");
		}

		line.start();

		// make sure audio is closed when finished
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				line.close();
			}
		}));
	}

	// captureThread will monitor sound levels for pauses, and notify the main thread 
	public void listenForSpeech() {
		if (capture == null) {
			speech = null;

			capture = new Capture(logger, line, thresholds);
			capture.addObserver(this);
			capture.start();
		} else {
			logger.log("Cannot start again thread untill it is finished");
		}
	}

	// called when speech has ended
	@Override
	public void update(ByteArrayOutputStream out) {
		logger.log("recording ended");

		File audio = flac.saveFlac(out.toByteArray());

		String result = null;
		try {
			result = googleSubmit.submit(audio, SAMPLE_RATE);
		} catch (IOException e) {
			logger.log("Problem sending FLAC to Google speech API");
			e.printStackTrace();
		}

		capture = null;

		// only update if speech was detected
		if (result == null) {
			listenForSpeech();
		} else {
			speech = result;
		}
	}

	public String getSpeech() {
		return speech;
	}
}
