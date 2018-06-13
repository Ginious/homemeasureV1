package ginious.home.measure.device;

import ginious.home.measure.device.AbstractMeasurementDevice;

public class TestDeviceA extends AbstractMeasurementDevice {

	public TestDeviceA() {
		super("A");

		registerMeasure("am1");
		registerMeasure("am2");
		registerMeasure("am3");
		registerMeasure("am4");
	}

	@Override
	public void switchOn() {

		setMeasureValue("am1", "WertAM1");
		setMeasureValue("am2", "WertAM2");
		setMeasureValue("am3", "WertAM3");
		setMeasureValue("am4", "WertAM4");
	}

	@Override
	protected void switchOffCustom() {
	}
}