package ginious.home.measure.cache;

import ginious.home.measure.Measure;

public interface MeasureListener {

	void measureChanged(String inDeviceId, Measure inChangedMeasure);
}