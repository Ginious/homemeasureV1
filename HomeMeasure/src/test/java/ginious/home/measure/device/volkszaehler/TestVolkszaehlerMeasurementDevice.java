package ginious.home.measure.device.volkszaehler;

import java.util.Set;

import org.openmuc.jsml.transport.SerialReceiver;

import ginious.home.measure.cache.MeasureCacheAdapter;
import ginious.home.measure.device.MeasurementDevice;
import ginious.home.measure.device.volkszaehler.VolkszaehlerMeasurementDevice;

/**
 * Testing device delegating all calls to the device under test providing a mock
 * receiver providing test measures.
 */
public class TestVolkszaehlerMeasurementDevice implements MeasurementDevice {

	private VolkszaehlerMeasurementDevice device;

	public TestVolkszaehlerMeasurementDevice() {
		super();

		device = new VolkszaehlerMeasurementDevice("test");
	}

	@Override
	public String getId() {
		return device.getId();
	}

	@Override
	public void switchOn() {
		device.switchOn();
	}

	@Override
	public void switchOff() {
		device.switchOff();
	}

	@Override
	public void setMeasureCacheAdapter(MeasureCacheAdapter inAdapter) {
		device.setMeasureCacheAdapter(inAdapter);
	}

	@Override
	public void setSetting(String inSettingId, String inSettingValue) {
		device.setSetting(inSettingId, inSettingValue);
	}

	@Override
	public Set<String> getSupportedMeasureIds() {
		return device.getSupportedMeasureIds();
	}
	
	void setReceiver(SerialReceiver inReceiver) {
		device.setReceiver(inReceiver);
	}

	@Override
	public void initDevice() {
		
	}
}