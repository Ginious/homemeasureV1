package ginious.home.measure;

import ginious.home.measure.device.MeasurementDevice;

public class MeasureFactory {

	private MeasureFactory() {
		super();
	}

	public static Measure createMeasure(MeasurementDevice inDevice, String inId) {

		return new MeasureImpl(inDevice, inId);
	}
}
