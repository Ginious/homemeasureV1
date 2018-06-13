package ginious.home.measure.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ginious.home.measure.Measure;

class MeasureCacheAdapterImpl implements MeasureCacheAdapter {

	private List<MeasureListener> measureListeners = new ArrayList<>();

	private List<Measure> measures;

	private String deviceId;

	protected MeasureCacheAdapterImpl(String inDeviceId, List<Measure> inMeasures) {
		super();

		deviceId = inDeviceId;
		measures = inMeasures;
	}

	@Override
	public void addMeasureListener(MeasureListener inListener) {
		measureListeners.add(inListener);
	}

	@Override
	public void setValue(String inId, String inValue) {

		boolean lValueSet = false;

		for (Measure lCurrMeasure : measures) {
			if (StringUtils.equals(lCurrMeasure.getID(), inId)) {
				String lOldValue = lCurrMeasure.getValue();
				lCurrMeasure.setValue(inValue);
				lValueSet = true;

				if (!StringUtils.equals(lOldValue, inValue)) {
					measureListeners.forEach(l -> l.measureChanged(deviceId, lCurrMeasure));
				} // if
			} // if
		} // for

		Validate.isTrue(lValueSet, "Measure with ID [" + inId + "] could not be found!");
	}
}