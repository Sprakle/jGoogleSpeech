						    jGoogleSpeech
					    Ben Cracknell (Sprakle)
========================================================================

Uses Google's speech recognition API to return microphone input as text.
WARNING: Google speech API is not official,and may change, breaking this
software.

jGoogleSpeech is just a small side project for my home automation system,
so expect bugs. It's a fairly simple library though (not far from being
a wrapper), so it shoudn't be to difficult to fix.

How the library works:
	1) GoogleSpeech is initialized. The microphone line is opened.
	
	2) When GoogleSpeech.listenForSpeech() is called, the capture thread
		is started, which listens for a certain volume (RMS). Once a
		threshold is passed, recording begins.
		
	3) Recording continues until the RMS falls below a second threshold
		for a period of time. GoogleSpeech is alerted of this, and passed
		the recorded data.
		
	4) GoogleSpeech, using Flac.java, saves the recorded data as a WAV
		file, then encodes it as flac. This is necessary because it is
		difficult to save recordings directly as flac, and the Google
		speech API only accepts flac (the entire process usually only
		takes 20-40ms for a sentence). Note that the *.flac and *.wav
		files are deleted once the JVM is closed
		
	5) GoogleSpeech then uses GoogleSubmit.java to send the .flac to
		Google, who sends back a json formatted file. GoogleSubmit
		returns the spoken phrase in a String. 
		
	6) GoogleSpeech alerts any observers, passing the spoken phrase.
	
Things you should know:
	To get updates when things are spoken, simply implement
		GoogleSpeechObserver. You can call GoogleSpeech.listenForSpeech()
		after being updated if you want continuous recognition.

	In order for the library to work, you need to create a class
		implementing "net.sprakle.jGoogleSpeech.Logger". This was done
		because my home automation system uses a graphical logger. You
		of course could just pass a Logger class that does nothing, and
		everything would still work.

	This library uses a separate thread to capture audio, and by extension
		update observers, so observer updates may not sync up with your
		application. I don't know a lot about multi-threading, so
		hopefully I haven't done anything to make this library difficult
		to use.
