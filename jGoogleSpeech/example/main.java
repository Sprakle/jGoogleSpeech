package main;

import net.sprakle.jGoogleSpeech.GoogleSpeech;
import net.sprakle.jGoogleSpeech.Logger;
import net.sprakle.jGoogleSpeech.RecordThresholds;

public class Main {
	// RMS of audio that must be above to begin recording
	private final float RECORD_START_RMS_THRESHOLD = 10;
	private final float RECORD_START_TIME_THRESHOLD = 100; // does nothing yet, recording starts as soon as threshold is passed

	// RMS of audio that must be less than to end recording
	private final float RECORD_END_RMS_THRESHOLD = 2;
	private final float RECORD_END_TIME_THRESHOLD = 250; // milliseconds RMS must be below threshold for

	// test have shown that changing this does not significantly change the time taken to process speech
	private final int SAMPLE_RATE = 44100;

	GoogleSpeech gs;

	Main() {
		RecordThresholds thresholds = new RecordThresholds(
				RECORD_START_RMS_THRESHOLD,
				RECORD_START_TIME_THRESHOLD,
				RECORD_END_RMS_THRESHOLD,
				RECORD_END_TIME_THRESHOLD);

		Output output = new Output();
		gs = new GoogleSpeech(output, thresholds, SAMPLE_RATE);
		gs.listenForSpeech();

		Listener listener = new Listener();
		listener.start();
	}

	class Listener extends Thread {
		@Override
		public void run() {

			while (true) {

				String s = gs.getSpeech();
				if (s != null) {
					System.out.println("Got speech: " + s);

					// continue listening
					gs.listenForSpeech();
				}

				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class Output implements Logger {
		@Override
		public void log(String s) {
			System.out.println(">> " + s);
		}
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Main main = new Main();
	}
}
