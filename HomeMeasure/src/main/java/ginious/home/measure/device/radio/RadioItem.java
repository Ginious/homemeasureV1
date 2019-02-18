package ginious.home.measure.device.radio;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * An item represents the definition of a sensor like device sending measures
 * via radio. It provides the id/ value pairs used for proper sensor
 * identification and the measures to provide.
 */
final class RadioItem {

	private static final String SEPARATOR_ID_AND_VALUE = ":";
	private static final String SEPARATOR_CSV = ",";

	private String name;

	private Map<String, String> ids = new HashMap<>();
	private Set<String> measureIds = new HashSet<>();

	/**
	 * Default constructor.
	 * 
	 * @param inName       The name of the item as defined in hmserver.ini.
	 * @param inIdValues   The id/ value pairs as defined in hmserver.ini.
	 * @param inMeasureIds The id of the measures to provide as defined in
	 *                     hmserver.ini.
	 */
	RadioItem(String inName, String inIdValues, String inMeasureIds) {
		super();

		Validate.isTrue(StringUtils.isNotBlank(inName), "Parameter inName is required!");
		Validate.isTrue(StringUtils.isNotBlank(inIdValues), "Parameter inIdValues is required!");
		Validate.isTrue(StringUtils.isNotBlank(inMeasureIds), "Parameter inMeasures is required!");

		name = inName;

		// extract id/ value pairs
		String[] lIdValuePairs = StringUtils.split(inIdValues, SEPARATOR_CSV);
		for (String lCurrPair : lIdValuePairs) {
			String[] lPairSplitted = StringUtils.split(lCurrPair, SEPARATOR_ID_AND_VALUE);
			ids.put(lPairSplitted[0], lPairSplitted[1]);
		} // for

		// extract measure ids
		for (String lCurrMeasureId : StringUtils.split(inMeasureIds, SEPARATOR_CSV)) {
			measureIds.add(lCurrMeasureId);
		} // for
	}

	/**
	 * Gets the ids that are used to identify the item properly.
	 * 
	 * @return The ids.
	 */
	final Map<String, String> getIds() {
		return Collections.unmodifiableMap(ids);
	}

	/**
	 * Gets the ids of the measures to publish.
	 * 
	 * @return The measure ids.
	 */
	final Set<String> getMeasureIds() {
		return Collections.unmodifiableSet(measureIds);
	}

	/**
	 * Gets the name of the item.
	 * 
	 * @return The name.
	 */
	final String getName() {
		return name;
	}
}