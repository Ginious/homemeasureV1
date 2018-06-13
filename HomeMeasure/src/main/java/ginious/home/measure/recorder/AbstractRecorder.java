package ginious.home.measure.recorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ginious.home.measure.Measure;

public abstract class AbstractRecorder implements Recorder {

	private static final String INCLUSION_MEASURES = "INCLUSION_MEASURES";

	private String id;

	private Map<String, List<String>> measuresToInclude = new HashMap<>();

	protected AbstractRecorder(String inId) {
		super();

		id = inId;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public final void initialize(Properties inProps) {

		String lInclusionMeasures = (String) inProps.get(INCLUSION_MEASURES);
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

		initializeCustom(inProps);
	}

	protected abstract void initializeCustom(Properties inProps);

	@Override
	public void measureChanged(String inDeviceId, Measure inChangedMeasure) {

		// skip empty measures from recording
		if (inChangedMeasure.getValue() == null || "null".equalsIgnoreCase(inChangedMeasure.getValue())) {
			return;
		} // if

		// include measure when ...
		// (1) no measure is specifically included
		// (2) measure is specifically included
		// (3) all measures of a device are included
		List<String> lMeasureIds = measuresToInclude.get(inDeviceId);
		if (lMeasureIds == null || //
				lMeasureIds.isEmpty() || //
				lMeasureIds.contains(inChangedMeasure.getID()) || //
				lMeasureIds.contains("*")) {
			measureChangedCustom(inDeviceId, inChangedMeasure);
		} // if
	}

	protected abstract void measureChangedCustom(String inDeviceId, Measure inChangedMeasure);

	@Override
	public final void startRecording() {
		startRecordingCustom();
	}

	protected void startRecordingCustom() {

	}

	@Override
	public final void stopRecording() {
		stopRecordingCustom();
	}

	protected void stopRecordingCustom() {

	}
}