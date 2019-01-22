package ginious.home.measure.device;

/**
 * Exception type used for the escalation of device initialization problems.
 */
public final class MeasurementDeviceInitializationException extends Exception {

	private static final long serialVersionUID = 1L;

	public MeasurementDeviceInitializationException(String inType, String inReason) {
		super("Device [" + inType + "] could not be created: " + inReason);
	}
}