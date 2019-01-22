package ginious.home.measure.cache;

/**
 * Cache adapter for measures of one single device. It enables reporting changed
 * measurements to the overall cache.
 */
public interface MeasureCacheAdapter {

	/**
	 * Sets the value of the measure given by id.
	 * 
	 * @param inId    The measure id.
	 * @param inValue The value.
	 */
	void setValue(String inId, String inValue);

	/**
	 * Adds a measure listener.
	 * 
	 * @param inListener The measure listener.
	 */
	void addMeasureListener(MeasureListener inListener);
}