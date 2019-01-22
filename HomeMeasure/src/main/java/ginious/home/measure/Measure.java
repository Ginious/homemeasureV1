package ginious.home.measure;

/**
 * Interface for a measure that can be performed by an underlying device.
 */
public interface Measure {

	/**
	 * Gets the id of the device this measure is originating from.
	 * 
	 * @return The id of the device.
	 */
	String getDeviceId();

	/**
	 * Gets the id of the measure.
	 * 
	 * @return The id of the measure.
	 */
	String getId();

	/**
	 * Gets the current value of the measure.
	 * 
	 * @return The current value.
	 */
	String getValue();

	/**
	 * Sets the current value of the measure.
	 * 
	 * @param inValue The value to set.
	 */
	void setValue(String inValue);
}
