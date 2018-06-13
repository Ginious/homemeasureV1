package ginious.home.measure.recorder;

import java.util.Properties;

import ginious.home.measure.cache.MeasureListener;

public interface Recorder extends MeasureListener {

	String getId();
	
	void startRecording();

	void stopRecording();
	
	void initialize(Properties inProps);
}