package ginious.home.measure.device;

import ginious.home.measure.device.AbstractMeasurementDevice;

public class TestDeviceB extends AbstractMeasurementDevice {

	public TestDeviceB() {
		super("B");

		registerMeasure("bm1");
		registerMeasure("bm2");
		registerMeasure("bm3");
		registerMeasure("bm4");
	}

	@Override
	public void switchOn() {

		setMeasureValue("bm1", "WertBM1");
		setMeasureValue("bm2", "WertBM2");
		setMeasureValue("bm3", "WertBM3");
		setMeasureValue("bm4", "WertBM4");
	}

	@Override
	protected void switchOffCustom() {
	}
}