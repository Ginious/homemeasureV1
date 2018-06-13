package ginious.home.measure.cache;

import java.util.List;

import ginious.home.measure.device.MeasurementDevice;

public final class MeasureCacheFactory {

	public static MeasureCache createCache(List<MeasurementDevice> inDevices, List<MeasureListener> inListeners) {
		return new MeasureCacheImpl(inDevices, inListeners);
	}
}