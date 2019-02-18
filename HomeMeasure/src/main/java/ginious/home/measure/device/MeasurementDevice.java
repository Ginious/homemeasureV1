package ginious.home.measure.device;

import java.util.Set;

import ginious.home.measure.cache.MeasureCacheAdapter;

public interface MeasurementDevice {

	void initDevice();
	
	String getId();
	
	void switchOn();

	void switchOff();

	void setMeasureCacheAdapter(MeasureCacheAdapter inAdapter);
	
	void setSetting(String inSettingId, String inSettingValue);
	
	Set<String> getSupportedMeasureIds();
}