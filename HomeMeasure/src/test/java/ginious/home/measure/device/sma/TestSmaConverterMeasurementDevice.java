package ginious.home.measure.device.sma;

import java.util.Set;

import de.re.easymodbus.modbusclient.ModbusClient;
import ginious.home.measure.cache.MeasureCacheAdapter;
import ginious.home.measure.device.MeasurementDevice;

public class TestSmaConverterMeasurementDevice implements MeasurementDevice {

	private SmaConverterMeasurementDevice device;

	public TestSmaConverterMeasurementDevice() {
		super();

		device = new SmaConverterMeasurementDevice();
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
	
	void setModbusClient(ModbusClient inMockClient) {
		device.setModbusClient(inMockClient);
	}
}