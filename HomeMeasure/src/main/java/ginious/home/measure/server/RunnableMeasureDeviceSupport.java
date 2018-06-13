package ginious.home.measure.server;

import ginious.home.measure.device.MeasurementDevice;

class RunnableMeasureDeviceSupport implements Runnable {

	private MeasurementDevice device;

	RunnableMeasureDeviceSupport(MeasurementDevice inDevice) {
		super();

		device = inDevice;
	}

	@Override
	public void run() {

		device.switchOn();
	}
}