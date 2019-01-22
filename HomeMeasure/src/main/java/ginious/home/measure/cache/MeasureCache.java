package ginious.home.measure.cache;

import java.util.Collection;
import java.util.Set;

import ginious.home.measure.Measure;

/**
 * A cache for measures.
 */
public interface MeasureCache {

	/**
	 * Adds a measure listener to be notified when the value one of the measures
	 * changed.
	 * 
	 * @param inListener The measure listener.
	 */
	void addMeasureListener(MeasureListener inListener);

	/**
	 * Gets the id of the underlying device.
	 * 
	 * @return The id of the underlying device.
	 */
	Set<String> getDeviceIds();

	/**
	 * Gets all meaasures of the given device from the cache.
	 * 
	 * @param inDeviceId The id of the device.
	 * @return The collection of measures.
	 */
	Collection<Measure> getMeasures(String inDeviceId);
}