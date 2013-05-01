package main;

import net.sprakle.jGoogleSpeech.Main;
import net.sprakle.jGoogleSpeech.Main.Listener;
import net.sprakle.jGoogleSpeech.Main.Output;

public class Main {
	// RMS of audio that must be above to begin recording
	private final float RECORD_START_RMS_THRESHOLD = 4;
	private final float RECORD_START_TIME_THRESHOLD = 250; // recording starts as soon as threshold is passed

	// RMS of audio that must be less than to end recording
	private final float RECORD_END_RMS_THRESHOLD = 3;
	private final float RECORD_END_TIME_THRESHOLD = 600; // milliseconds RMS must be below threshold for

	// Number of bytes to prepend before recording begins. This compensates for late triggering caused by starting thresholds
	private final int PREPEND_BYTES = 50000;

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
		gs = new GoogleSpeech(output, thresholds, SAMPLE_RATE, PREPEND_BYTES);
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
		public void log(boolean criticalError, String s) {
			System.out.println(">> " + s);
		}
	}

	public static void main(String[] args) {
		new Main();
	}
}
