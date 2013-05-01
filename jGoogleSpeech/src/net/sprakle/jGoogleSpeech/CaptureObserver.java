package net.sprakle.jGoogleSpeech;

import java.io.ByteArrayOutputStream;

interface CaptureObserver {
	void update(ByteArrayOutputStream out);
}
