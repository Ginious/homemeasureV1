package ginious.home.measure.device;

public class MeasurementDeviceNotFoundException extends Exception {

	public MeasurementDeviceNotFoundException(String inType, String inReason) {
		super("Device [" + inType + "] could not be created: " + inReason);
	}
}