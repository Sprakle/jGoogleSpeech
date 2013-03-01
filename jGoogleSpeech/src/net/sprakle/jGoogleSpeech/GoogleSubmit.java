package net.sprakle.jGoogleSpeech;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

class GoogleSubmit {

	Logger logger;

	HttpClient httpClient;

	private final String URL;
	GoogleSubmit(Logger logger, String URL) {
		this.logger = logger;
		this.URL = URL;
	}

	public String submit(File audio, int sampleRate) throws ClientProtocolException, IOException {
		String result = null;
		long startTime = System.currentTimeMillis();
		logger.log("Begun submit to Google speech API");

		httpClient = new DefaultHttpClient();

		// make sure it's a flac file
		String fileName = audio.getName();
		String extension = null;
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);

			if (!extension.equals("flac")) {
				logger.log("Can only submit flac files");
			}
		} else {
			logger.log("Can only submit flac files");
		}

		HttpPost httppost = new HttpPost(URL);
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("audio", new FileBody(audio));
		httppost.setEntity(reqEntity);

		httppost.addHeader("Content-type: audio/x-flac; rate=" + sampleRate, "Header");

		HttpResponse response = httpClient.execute(httppost);
		String status = response.getStatusLine().toString();
		if (status.contains("failed")) {
			logger.log("No speech detected");
			return null;
		}

		String responseString = getResponseString(response);
		result = parseForUtterance(responseString);

		logger.log("Finished submit to Google speech API. Result: " + status);
		long endTime = System.currentTimeMillis();
		logger.log("Total time taken to communicate with Google: " + (endTime - startTime) + "ms");
		return result;
	}
	private String getResponseString(HttpResponse httpResponse) throws IllegalStateException, IOException {
		String result = "";

		HttpEntity entity = httpResponse.getEntity();

		InputStream is = entity.getContent();
		int inByte;
		while ((inByte = is.read()) != -1) {
			result += (char) inByte;
		}
		is.close();

		return result;
	}

	// parses response string for utterance
	private String parseForUtterance(String s) {
		String result = null;

		String preamble = "\"hypotheses\":[{\"utterance\":\"";
		String addendum = "\",\"confidence\":";
		int preambleLength = preamble.length();

		int preambleIndex = s.indexOf(preamble);
		int addendumIndex = s.lastIndexOf(addendum);

		int utteranceIndex = preambleIndex + preambleLength;

		result = s.substring(utteranceIndex, addendumIndex);

		return result;
	}
}
