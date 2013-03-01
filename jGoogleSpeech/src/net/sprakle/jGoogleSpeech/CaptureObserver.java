package net.sprakle.jGoogleSpeech;

import java.io.ByteArrayOutputStream;

interface CaptureObserver {
	public void update(ByteArrayOutputStream out);
}
