package ginious.home.measure.recorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ginious.home.measure.Measure;
import ginious.home.measure.util.ConfigHelper;

/**
 * Base implementation of a recorder providing basic setting initialization.
 */
public abstract class AbstractRecorder implements Recorder {

	private static final String INCLUDE_MEASURES = "INCLUDE_MEASURES";

	private String id;

	private Map<String, List<String>> measuresToInclude = new HashMap<>();

	/**
	 * Default recorder constructor.
	 * 
	 * @param inId         The id of the recorder.
	 * @param inProperties The recorder properties as defined in hmserver ini.
	 */
	protected AbstractRecorder(String inId, Properties inProperties) {
		super();

		id = inId;

		init(ConfigHelper.extractProperties(inProperties, inId));
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * Gets the value of a property from the underlying configuration.
	 * 
	 * @param inProps       The set of properties.
	 * @param inPropName    The name of the property to get.
	 * @param inIsMandatory Defines whether the property is required or not.
	 * @return The property value.
	 */
	protected final String getProperty(Properties inProps, String inPropName, boolean inIsMandatory) {

		String outProperty = inProps.getProperty(inPropName);
		Validate.isTrue(inIsMandatory && StringUtils.isNotBlank(outProperty),
				"Property [" + getId() + "." + inPropName + "] is not provided!");

		return outProperty;
	}

	/**
	 * Performs the initialization based on the given application properties.
	 * 
	 * @param inProps The application properties.
	 */
	private void init(Properties inProps) {

		String lInclusionMeasures = (String) inProps.get(INCLUDE_MEASURES);
		String[] lDeviceMeasuresToInclude = StringUtils.split(lInclusionMeasures, ",;");
		if (lDeviceMeasuresToInclude != null) {

			for (String lCurrDeviceMeasure : lDeviceMeasuresToInclude) {
				Validate.isTrue(lCurrDeviceMeasure.contains("."), "The device measure [" + lCurrDeviceMeasure
						+ "] is invalid - please prefix measures with the device id like [my_device_id.my_measure_id]!");
				String lDeviceId = StringUtils.substringBefore(lCurrDeviceMeasure, ".");
				String lMeasureId = StringUtils.substringAfter(lCurrDeviceMeasure, ".");
				List<String> lDeviceMeasures = measuresToInclude.get(lDeviceId);
				if (lDeviceMeasures == null) {
					lDeviceMeasures = new ArrayList<>();
					measuresToInclude.put(lDeviceId, lDeviceMeasures);
				} // if
				lDeviceMeasures.add(lMeasureId);
			} // for
		} // if

		initialize(inProps);
	}

	/**
	 * Overwrite in case specific initialization is required.
	 */
	@Override
	public void initialize(Properties inProperties) {

	}

	/**
	 * This callback will be called for each measure of which the value changed. The
	 * call will be delegated to the subclass implementation
	 * <code>measureChangedCustom</code> as long as the measure was not explicitly
	 * excluded.
	 */
	@Override
	public void measureChanged(Measure inChangedMeasure) {

		// skip empty measures from recording
		if (inChangedMeasure.getValue() == null || "null".equalsIgnoreCase(inChangedMeasure.getValue())) {
			return;
		} // if

		// include measure when ...
		// (1) no measure is specifically included
		// (2) measure is specifically included
		// (3) all measures of a device are included
		List<String> lMeasureIds = measuresToInclude.get(inChangedMeasure.getDeviceId());
		if (lMeasureIds == null || //
				lMeasureIds.isEmpty() || //
				lMeasureIds.contains(inChangedMeasure.getId()) || //
				lMeasureIds.contains("*")) {
			measureChangedCustom(inChangedMeasure);
		} // if
	}

	/**
	 * This callback will be called for measures that are explicitly not excluded.
	 * 
	 * @param inChangedMeasure The measure that has changed.
	 */
	protected abstract void measureChangedCustom(Measure inChangedMeasure);

	/**
	 * Overwrite in case specific functionality is required for starting.
	 */
	@Override
	public final void startRecording() {

	}

	/**
	 * Overwrite in case specific functionality is required for stopping.
	 */
	@Override
	public final void stopRecording() {

	}
}