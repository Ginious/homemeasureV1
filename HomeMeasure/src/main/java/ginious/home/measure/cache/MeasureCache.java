package ginious.home.measure.cache;

import java.util.Collection;
import java.util.Set;

import ginious.home.measure.Measure;

public interface MeasureCache {

	Set<String> getDeviceIds();

	Collection<Measure> getMeasures(String inDeviceId);
}