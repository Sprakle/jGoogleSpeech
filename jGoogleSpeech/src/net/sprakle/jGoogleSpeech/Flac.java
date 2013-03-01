package net.sprakle.jGoogleSpeech;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

class Flac {
	private final int SAMPLE_RATE;
	private final int BIT_RATE;

	private Logger logger;

	public Flac(Logger logger, int SAMPLE_RATE, int BIT_RATE) {
		this.logger = logger;
		this.SAMPLE_RATE = SAMPLE_RATE;
		this.BIT_RATE = BIT_RATE;
	}

	public File saveFlac(byte[] data) {
		long startTime = System.currentTimeMillis();

		AudioFormat format = new AudioFormat(SAMPLE_RATE, BIT_RATE, 1, true, false);
		String filename = "recorded.wav";

		File WAV = new File(filename);
		WAV.deleteOnExit();

		// now save the file
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			AudioInputStream ais = new AudioInputStream(bais, format, data.length);
			AudioSystem.write(ais, AudioFileFormat.Type.WAVE, WAV);
		} catch (Exception e) {
			logger.log("Problem saving WAV file");
			e.printStackTrace();
		}
		logger.log("WAV file saved");

		File source = WAV;
		File output = new File("recorded.flac");
		output.deleteOnExit();

		// set attributes
		EncodingAttributes attrib = new EncodingAttributes();
		attrib.setFormat("flac");

		AudioAttributes audAttrib = new AudioAttributes();
		audAttrib.setCodec("flac");
		audAttrib.setBitRate(BIT_RATE);
		audAttrib.setChannels(1);
		audAttrib.setSamplingRate(SAMPLE_RATE);

		attrib.setAudioAttributes(audAttrib);

		Encoder encoder = new Encoder();

		try {
			encoder.encode(source, output, attrib);
		} catch (IllegalArgumentException | EncoderException e) {
			System.err.println("Unable to encode speech WAV file");
			e.printStackTrace();
		}

		logger.log("WAV file encoded to FLAC");

		long totalTime = System.currentTimeMillis() - startTime;
		logger.log("Total time taken to create FLAC: " + totalTime + "ms");

		return output;
	}
}
