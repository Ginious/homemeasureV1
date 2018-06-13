package ginious.home.measure.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ginious.home.measure.Measure;
import ginious.home.measure.MeasureFactory;
import ginious.home.measure.device.MeasurementDevice;

class MeasureCacheImpl implements MeasureCache {

	private List<MeasureListener> measureListeners;

	private Map<String, List<Measure>> measures = new HashMap<>();

	MeasureCacheImpl(List<MeasurementDevice> inDevices, List<MeasureListener> inListeners) {
		super();

		measureListeners = inListeners;
		inDevices.stream().forEach(d -> registerDevice(d));
	}

	public Set<String> getDeviceIds() {
		return measures.keySet();
	}

	public Collection<Measure> getMeasures(String inDeviceId) {
		return measures.get(inDeviceId);
	}

	private void registerDevice(MeasurementDevice inDevice) {

		for (String lCurrMeasureId : inDevice.getSupportedMeasureIds()) {

			List<Measure> lDeviceMeasures = measures.get(inDevice.getId());
			if (lDeviceMeasures == null) {
				lDeviceMeasures = new ArrayList<>();
				measures.put(inDevice.getId(), lDeviceMeasures);
			} // if
			lDeviceMeasures.add(MeasureFactory.createMeasure(lCurrMeasureId));
		} // for

		MeasureCacheAdapter lCacheAdapter = new MeasureCacheAdapterImpl(inDevice.getId(),
				measures.get(inDevice.getId()));
		measureListeners.forEach(l -> lCacheAdapter.addMeasureListener(l));
		inDevice.setMeasureCacheAdapter(lCacheAdapter);
	}
}
