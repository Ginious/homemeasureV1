package ginious.home.measure.device;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ginious.home.measure.cache.MeasureCacheAdapter;

/**
 * Base type for concrete device implementations providing basic functionality.
 */
public abstract class AbstractMeasurementDevice implements MeasurementDevice {

	/**
	 * The setting that can be used to turn on the demonstration mode.
	 */
	private static final String CONFIG_DEMO_MODE = "DEMO_ONLY";

	/**
	 * Cache adapter for measurements.
	 */
	private MeasureCacheAdapter cache;

	private String id;
	private boolean switchedOff;
	private Set<String> measureIds = new HashSet<>();
	private Map<String, String> configuration = new HashMap<>();

	/**
	 * Default constructor. Performs custom initialization tasks in initDevice
	 * operation.
	 * 
	 * @param inId The id of the device (required).
	 */
	protected AbstractMeasurementDevice(String inId) {
		super();

		Validate.isTrue(StringUtils.isNotBlank(inId), "inId is required!");
		id = inId.toLowerCase();
	}

	/**
	 * Device-specific initialization of measures and device settings.
	 */
	public void initDevice() {
		
	}

	@Override
	public final String getId() {
		return id;
	}

	/**
	 * Retrieves a device setting from the underlying device configuration as float
	 * value. This includes also the validation of the format.
	 * 
	 * @param inSettingId The id of the setting.
	 * @param inDefault   The default value when no value is configured.
	 * @return The setting value as float.
	 */
	protected final float getSettingAsFloat(String inSettingId, String inDefault) {

		float outNumber;

		try {
			outNumber = Float.valueOf(getSettingAsText(inSettingId, inDefault));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Setting [" + inSettingId + "] must be a floating point number!");
		} // catch

		return outNumber;
	}

	/**
	 * Retrieves a device setting from the underlying device configuration as
	 * integer value. This includes also the validation of the format.
	 * 
	 * @param inSettingId The id of the setting.
	 * @param inDefault   The default value when no value is configured.
	 * @return The setting value as integer.
	 */
	protected final int getSettingAsInteger(String inSettingId, String inDefault) {

		int outNumber;

		String lSettingAsText = getSettingAsText(inSettingId, inDefault);
		try {
			outNumber = Integer.valueOf(lSettingAsText);
		} catch (NumberFormatException e) {
			throw new RuntimeException(
					"Setting [" + inSettingId + "] with value [" + lSettingAsText + "] must be an integer number!");
		} // catch

		return outNumber;
	}

	/**
	 * Retrieves a device setting from the underlying device configuration as text
	 * value.
	 * 
	 * @param inSettingId The id of the setting.
	 * @param inDefault   The default value when no value is configured.
	 * @return The setting value as text.
	 */
	protected final String getSettingAsText(String inSettingId, String inDefault) {

		Validate.isTrue(configuration != null, "Configuration not yet initialized!");
		Validate.isTrue(StringUtils.isNotBlank(inSettingId), "aSettingId ist required!");
		if (inDefault == null) {
			Validate.isTrue(configuration.containsKey(inSettingId.toUpperCase()),
					"Setting [" + inSettingId + "] does not exist!");
		} // if

		String outSetting = configuration.get(inSettingId);
		if (outSetting == null) {
			return inDefault;
		} // if

		return outSetting;
	}

	@Override
	public Set<String> getSupportedMeasureIds() {
		return measureIds;
	}

	/**
	 * Registers the given id as measure id. It will later be used when building a
	 * cache.
	 * 
	 * @param inId The id of the measurement.
	 */
	protected final void registerMeasure(String inId) {
		measureIds.add(inId);
	}

	@Override
	public final void setMeasureCacheAdapter(MeasureCacheAdapter inAdapter) {
		cache = inAdapter;
	}

	/**
	 * Sets the given value of the given measure.
	 * 
	 * @param inMeasureId The id of the measurement.
	 * @param inValue     The value.
	 */
	protected final void setMeasureValue(String inMeasureId, String inValue) {
		cache.setValue(inMeasureId, inValue);
	}

	/**
	 * Sets the value of a setting.
	 * 
	 * @param inSettingId    The id of the setting.
	 * @param inSettingValue The value of the setting.
	 */
	public void setSetting(String inSettingId, String inSettingValue) {
		configuration.put(inSettingId, inSettingValue);
	}

	/**
	 * Lets the current Thread sleep for the given amount of milliseconds.
	 * 
	 * @param inMilliseconds The amount of milliseconds to sleep.
	 */
	protected final void sleep(int inMilliseconds) {

		try {
			Thread.sleep(inMilliseconds);
		} catch (InterruptedException e) {
		} // catch
	}

	@Override
	public final void switchOff() {
		switchedOff = true;
		switchOffCustom();
	}

	@Override
	public final void switchOn() {

		if (isDemoMode()) {

			for (;;) {
				measureIds.forEach(id -> setMeasureValue(id, String.valueOf(((int) (Math.random() * 100)))));
				sleep(10000);
			}
		} else {
			switchOnCustom();
		} // else
	}

	/**
	 * Switch OFF the device. Implementation Must be provided device-specific.
	 */
	protected abstract void switchOffCustom();

	/**
	 * Switch ON the device. Implementation Must be provided device-specific.
	 */
	protected abstract void switchOnCustom();

	/**
	 * Indicates whether the device was switched off by the server. This information
	 * should be used by a specific device to e.g. stop its measuring thread.
	 * 
	 * @return <code>true</code> when the device has been switched off by the
	 *         server, <code>false</code> otherwise.
	 */
	protected final boolean wasSwitchedOff() {
		return switchedOff;
	}

	/**
	 * Indicates whether the device is configured to be used in demonstration mode.
	 * If so the device measurements will be created randomly after device is was
	 * switched on.
	 * 
	 * @return <code>true</code> when the device should run in demonstration mode,
	 *         <code>false</code> otherwise.
	 */
	private boolean isDemoMode() {
		return Boolean.valueOf(getSettingAsText(CONFIG_DEMO_MODE, Boolean.FALSE.toString()));
	}

	/**
	 * Gets an unmodifiable view of the underlying configuration.
	 * 
	 * @return The unmodifiable configuration.
	 */
	protected final Map<String, String> getConfiguration() {
		return Collections.unmodifiableMap(configuration);
	}
}