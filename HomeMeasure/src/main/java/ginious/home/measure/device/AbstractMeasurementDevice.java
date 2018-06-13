package ginious.home.measure.device;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ginious.home.measure.cache.MeasureCacheAdapter;

public abstract class AbstractMeasurementDevice implements MeasurementDevice {

	private MeasureCacheAdapter cache;

	private Map<String, String> configuration = new HashMap<>();
	private String id;
	private Set<String> measureIds = new HashSet<>();

	private boolean switchedOff;

	protected AbstractMeasurementDevice(String inId) {
		super();

		Validate.isTrue(StringUtils.isNotBlank(inId), "inId is required!");

		id = inId.toLowerCase();
	}

	@Override
	public final String getId() {
		return id;
	}

	protected final float getSettingAsFloat(String inSettingId, String inDefault) {

		float outNumber;

		try {
			outNumber = Float.valueOf(getSettingAsText(inSettingId, inDefault));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Setting [" + inSettingId + "] must be a floating point number!");
		} // catch

		return outNumber;
	}

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

	protected final String getSettingAsText(String inSettingId, String inDefault) {

		Validate.isTrue(configuration != null, "Configuration not yet initialized!");
		Validate.isTrue(StringUtils.isNotBlank(inSettingId), "aSettingId ist required!");
		Validate.isTrue(configuration.containsKey(inSettingId.toUpperCase()),
				"Setting [" + inSettingId + "] does not exist!");

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

	protected final void logError(String inMessage) {

	}

	protected final void logInfo(String inMessage) {

	}

	protected final void registerMeasure(String inId) {
		measureIds.add(inId);
	}

	@Override
	public final void setMeasureCacheAdapter(MeasureCacheAdapter inAdapter) {
		cache = inAdapter;
	}

	protected final void setMeasureValue(String inMeasureId, String inValue) {

		cache.setValue(inMeasureId, inValue);
	}

	public void setSetting(String inSettingId, String inSettingValue) {
		configuration.put(inSettingId, inSettingValue);
	}

	/**
	 * Lets the current Thread sleep for the given amount of milliseconds.
	 * 
	 * @param inMilliseconds
	 *            The amount of milliseconds to sleep.
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

	protected void switchOffCustom() {

	}

	protected final boolean wasSwitchedOff() {
		return switchedOff;
	}
}