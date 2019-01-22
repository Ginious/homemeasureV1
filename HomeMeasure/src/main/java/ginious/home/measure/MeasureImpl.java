package ginious.home.measure;

import ginious.home.measure.device.MeasurementDevice;

class MeasureImpl implements Measure {

	private String id;
	private String value;
	private String deviceId;

	MeasureImpl(MeasurementDevice inDevice, String inId) {
		super();

		deviceId = inDevice.getId();
		id = inId;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String inValue) {
		value = inValue;
	}

	@Override
	public String getDeviceId() {
		return deviceId;
	}
}