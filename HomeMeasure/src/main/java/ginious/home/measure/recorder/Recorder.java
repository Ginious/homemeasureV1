package ginious.home.measure.recorder;

import java.util.Properties;

import ginious.home.measure.cache.MeasureListener;

/**
 * A recorder is capable of recording measures just after measurement.
 */
public interface Recorder extends MeasureListener {

	/**
	 * Gets the id of the recorder as provided in hmserver.ini.
	 * 
	 * @return The id of the recorder.
	 */
	String getId();

	/**
	 * Performs the initialization based on the given recorder specific properties.
	 * 
	 * @param inProperties The recorder properties.
	 */
	void initialize(Properties inProperties);

	/**
	 * Starts recording.
	 */
	void startRecording();

	/**
	 * Stops recording.
	 */
	void stopRecording();
}