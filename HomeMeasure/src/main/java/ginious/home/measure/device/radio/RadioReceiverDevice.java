package ginious.home.measure.device.radio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ginious.home.measure.device.AbstractMeasurementDevice;
import ginious.home.measure.device.radio.RTL433Stub.Option;
import ginious.home.measure.util.LogHelper;

/**
 * Device using rtl_433 (need to be installed separately) to gather data from
 * items via radio.
 * 
 * Sample configuration:
 * <ul>
 * <li><b>&lt;my-radio&gt;.PROTOCOLS=68,5</b><br/>
 * Optional: type(s) of item(s) for which data should be received.<br/>
 * Skip setting for receiving data of all items that are available.<br/>
 * Sample above will listen for item protocols 68 and 5.</li>
 * <li><b>&lt;my-radio&gt;.&lt;my-item-name&gt;.IDS=id:68,channel:1</b><br/>
 * Required: Define named item by listing all id/ value pairs for item
 * identification.<br/>
 * Id/ value must be separated by colon, pairs by comma.<br/>
 * Sample above identifies <i>my-item-name</i> by id = 68 and channel = 1.</li>
 * <li><b>&lt;my-radio&gt;.&lt;my-item-name&gt;.MEASURES=temperature_C,humidity</b><br/>
 * Required: define which value(s) to consume (comma separated).<br/>
 * Sample above will report the measure <i>temperature_C</i> and
 * <i>humidity</i></li>
 * </ul>
 */
public class RadioReceiverDevice extends AbstractMeasurementDevice {

	private static final String SETTING_IDS = "IDS";
	private static final String SETTING_MEASURES = "MEASURES";
	private static final String SETTING_PROTOCOLS = "PROTOCOLS";
	private static final String SETTING_SEPARATOR = ".";
	private static final String SETTING_SEPARATOR_CSV = ",";

	private static final String MESSAGE_READY = "Tuned to";
	private static final String MESSAGE_DATA = "{";

	private static final String MEASURE_NAME_SEPARATOR = "-";

	/**
	 * Registry containing all items defined in hmserver.ini.
	 */
	private Map<String, RadioItem> itemsRegistry = new HashMap<>();

	/**
	 * The process stub used for testing when set from outside.
	 */
	private RTL433Stub processStubForTesting;

	/**
	 * Default constructor.
	 * 
	 * @param inDeviceId The id of the device.
	 */
	public RadioReceiverDevice(String inDeviceId) {
		super(inDeviceId);
	}

	/**
	 * Creates and initializes the RTL_433 process and provides a stub for accessing
	 * the resulting data.
	 * 
	 * @return The process stub.
	 */
	private RTL433Stub createProcessStub() {

		RTL433Stub outStub;

		if (processStubForTesting != null) {

			// Testing mode
			outStub = processStubForTesting;
		} else {

			outStub = new RTL433Stub();

			// generally output JSON data
			outStub.addOption(Option.OUTPUT_JSON, null);

			// define item protocols to listen to
			Set<String> lDeviceProtocols = getConfiguredProtocols();
			if (!lDeviceProtocols.isEmpty()) {

				// specific item
				lDeviceProtocols.stream().forEach(p -> outStub.addOption(Option.DEVICE_PROTOCOL, p));
			} else {

				// listen to all items
				outStub.addOption(Option.ALL_DEVICES, null);
			} // else
		} // else

		return outStub;
	}

	/**
	 * Gets the protocols that are configured in hmserver.ini and as defined by
	 * RTL_433. This device will only listen for items resulting from to those
	 * protocols.
	 * 
	 * @return The list of configured protocols as defined by RTL_433.
	 */
	private Set<String> getConfiguredProtocols() {

		Set<String> outProtocols = new HashSet<>();
		String lDeviceProtocolsCsv = getSettingAsText(SETTING_PROTOCOLS, null);
		if (lDeviceProtocolsCsv != null) {
			for (String lCurrProtocol : StringUtils.split(lDeviceProtocolsCsv, SETTING_SEPARATOR_CSV)) {
				outProtocols.add(lCurrProtocol);
			} // for
		} // if

		return outProtocols;
	}

	/**
	 * Gets the RTL item of which all ids and their values match the ids and values
	 * of the given JSON object.
	 * 
	 * @param inJson The JSON object to get the RTL item for.
	 * @return The resolved RTL item or <code>null</code> when not all ids and/ or
	 *         values matched.
	 */
	private RadioItem getMatchingRTLItem(JSONObject inJson) {

		RadioItem outItem = null;

		for (String lCurrItemName : itemsRegistry.keySet()) {

			RadioItem lCurrItem = itemsRegistry.get(lCurrItemName);
			for (String lCurrId : lCurrItem.getIds().keySet()) {

				if (!inJson.containsKey(lCurrId)
						|| !StringUtils.equals(String.valueOf(inJson.get(lCurrId)), lCurrItem.getIds().get(lCurrId))) {

					// skip this item as at least one id/ value pair did not match
					outItem = null;
					break;
				} // if

				// remember item as all id/ value pairs matched
				outItem = lCurrItem;
			} // for
		} // for

		return outItem;
	}

	@Override
	public void initDevice() {

		// determine all configured items
		Set<String> lItemNames = new HashSet<>();
		getConfiguration().keySet().stream().filter(k -> StringUtils.countMatches(k, SETTING_SEPARATOR) == 1)
				.forEach(k -> {
					lItemNames.add(StringUtils.substringBefore(k, SETTING_SEPARATOR));
				});

		// determine IDs for each device
		lItemNames.forEach(deviceId -> {
			String lIdsSetting = getConfiguration().get(deviceId + SETTING_SEPARATOR + SETTING_IDS);
			String lMeasuresSetting = getConfiguration().get(deviceId + SETTING_SEPARATOR + SETTING_MEASURES);
			itemsRegistry.put(deviceId, new RadioItem(deviceId, lIdsSetting, lMeasuresSetting));
		});
	}

	/**
	 * Sets the process stub for testing purposes.
	 * 
	 * @param inProcessStub The process stub for testing.
	 */
	void setProcessStubForTesting(RTL433Stub inProcessStub) {
		processStubForTesting = inProcessStub;
	}

	@Override
	protected void switchOffCustom() {

	}

	@Override
	protected void switchOnCustom() {

		// create and start process
		RTL433Stub lProcessStub = createProcessStub();
		lProcessStub.startProcess();

		JSONParser lJsonParser = new JSONParser();

		// gather and process console output
		String lLine;
		boolean lDongleIsTuned = false;
		while ((lLine = lProcessStub.getOutput()) != null) {

			// wait until device is ready
			if (StringUtils.startsWith(lLine, MESSAGE_READY)) {
				lDongleIsTuned = true;
			} // if

			// read received json messages
			if (lDongleIsTuned && StringUtils.startsWith(lLine, MESSAGE_DATA)) {

				// parse json message
				JSONObject lJson;
				try {
					lJson = (JSONObject) lJsonParser.parse(lLine);
				} catch (ParseException e) {
					LogHelper.logError(this, e, "Failed to parse received JSON message [{0}]!", lLine);
					continue;
				} // catch

				// check whether message comes from item of interest
				// (as defined in hmserver.ini)
				RadioItem lMatchingItem = getMatchingRTLItem(lJson);
				if (lMatchingItem != null) {
					lMatchingItem.getMeasureIds().forEach(id -> {
						setMeasureValue(lMatchingItem.getName() + MEASURE_NAME_SEPARATOR + id,
								String.valueOf(lJson.get(id)));
					});
				} // if
			} // if
		} // while
	}
}