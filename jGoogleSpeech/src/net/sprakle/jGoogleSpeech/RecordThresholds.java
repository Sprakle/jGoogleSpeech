package net.sprakle.jGoogleSpeech;

public class RecordThresholds {

	// RMS of audio that must be above to begin recording
	public final float RECORD_START_RMS_THRESHOLD;
	public final float RECORD_START_TIME_THRESHOLD;

	// RMS of audio that must be less than to end recording
	public final float RECORD_END_RMS_THRESHOLD;
	public final float RECORD_END_TIME_THRESHOLD; // milliseconds RMS must be below threshold for

	public RecordThresholds(float RECORD_START_RMS_THRESHOLD, float RECORD_START_TIME_THRESHOLD, float RECORD_END_RMS_THRESHOLD, float RECORD_END_TIME_THRESHOLD) {

		this.RECORD_START_RMS_THRESHOLD = RECORD_START_RMS_THRESHOLD;
		this.RECORD_START_TIME_THRESHOLD = RECORD_START_TIME_THRESHOLD;
		this.RECORD_END_RMS_THRESHOLD = RECORD_END_RMS_THRESHOLD;
		this.RECORD_END_TIME_THRESHOLD = RECORD_END_TIME_THRESHOLD;
	}
}
