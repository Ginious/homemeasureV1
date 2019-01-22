package ginious.home.measure.cache;

import java.util.List;

import ginious.home.measure.device.MeasurementDevice;

/**
 * Helper for creating a measure cache.
 */
public final class MeasureCacheHelper {

	/**
	 * Helper constructor.
	 */
	private MeasureCacheHelper() {
		super();
	}

	/**
	 * Creates a measure cache.
	 * 
	 * @param inDevices   The devices producing measures.
	 * @param inListeners The listener being notified when a measure value changes.
	 * @return The created cache.
	 */
	public static MeasureCache createCache(List<MeasurementDevice> inDevices, List<MeasureListener> inListeners) {
		return new MeasureCacheImpl(inDevices, inListeners);
	}
}