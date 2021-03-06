package ginious.home.measure.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;

import ginious.home.measure.util.ConfigSource.LocationType;

/**
 * Helper for common configuration tasks.
 */
public final class ConfigHelper {

	private static final String CONFIG_FILE_NAME = "hmserver.ini";
	private static final String CONFIG_USER_HOME = System.getProperty("user.home");
	private static final String CONFIG_ETC = "/etc/";
	private static final String CONFIG_CONF = "/conf/";

	/**
	 * Gets a named number setting value from the given set of properties or the
	 * default value if no value is provided.
	 * 
	 * @param aPropertySet   The configuration properties.
	 * @param inPropertyName The name of the setting.
	 * @param inDefaultValue The default value.
	 * @return The setting value or the given value when no setting was provided.
	 */
	public static int getNumberProperty(Properties aPropertySet, String inPropertyName, int inDefaultValue) {

		String lTextValue = getTextProperty(aPropertySet, inPropertyName, String.valueOf(inDefaultValue));
		Validate.isTrue(NumberUtils.isNumber(lTextValue), "No valid number was provided: " + inPropertyName);

		return Integer.valueOf(lTextValue);
	}

	/**
	 * Gets a named number setting value from the given set of properties or the
	 * default value if no value is provided.
	 * 
	 * @param aPropertySet   The configuration properties.
	 * @param inPropertyName The name of the setting.
	 * @param inDefaultValue The default value.
	 * @return The setting value or the given value when no setting was provided.
	 */
	public static String getTextProperty(Properties aPropertySet, String inPropertyName, String inDefaultValue) {

		String outValue = aPropertySet.getProperty(inPropertyName);

		if (outValue == null) {
			outValue = inDefaultValue;
		} // if

		return outValue;
	}

	/**
	 * Ensures the existence of the named property in the given set of properties.
	 * 
	 * @param aPropertySet   The set of properties.
	 * @param inPropertyName The name of the property for which to ensure its
	 *                       existence.
	 */
	public static void validatePropertyExistence(Properties aPropertySet, String inPropertyName) {

		Validate.isTrue(StringUtils.isNotBlank(aPropertySet.getProperty(inPropertyName)),
				"The property [" + inPropertyName + "] is required but not provided!");
	}

	/**
	 * Extracts properties from a given set of properties based on the given prefix.
	 * 
	 * @param inProperties The overall defined properties.
	 * @param inPrefix     The prefix of the properties to extract.
	 * @return The set of properties that have been extracted from the overall
	 *         properties.
	 */
	public static Properties extractProperties(Properties inProperties, String inPrefix) {

		Properties outProps = new Properties();

		for (String lCurrPropName : inProperties.stringPropertyNames()) {
			if (lCurrPropName.startsWith(inPrefix)) {
				String lPropNameWithoutPrefix = StringUtils.remove(lCurrPropName, inPrefix + ".");
				outProps.setProperty(lPropNameWithoutPrefix, inProperties.getProperty(lCurrPropName));
			} // if
		} // while

		return outProps;
	}

	/**
	 * Tries to load the configuration from the following locations in the given
	 * order:
	 * <ul>
	 * <li>classpath://hmserver.ini</li>
	 * <li>%USER_HOME%/.hmserver.ini</li>
	 * <li>%CURR_WORKING_DIR%/conf/hmserver.ini</li>
	 * <li>/etc/hmserver.ini</li>
	 * </ul>
	 * 
	 * @return The loaded configuration.
	 */
	public static Properties loadConfiguration() {

		Properties outProperties = new Properties();

		// create list with config file locations in order of lookup
		List<ConfigSource> lConfigSources = new ArrayList<>();
		lConfigSources.add(new ConfigSource(LocationType.WorkingDirectory, new File(CONFIG_FILE_NAME)));
		lConfigSources.add(new ConfigSource(LocationType.Classpath, "classpath://" + CONFIG_FILE_NAME,
				ConfigHelper.class.getResourceAsStream("/" + CONFIG_FILE_NAME)));
		lConfigSources.add(new ConfigSource(LocationType.UserHome, new File(CONFIG_USER_HOME, "." + CONFIG_FILE_NAME)));
		lConfigSources.add(new ConfigSource(LocationType.ConfDirectory, new File(CONFIG_CONF, CONFIG_FILE_NAME)));
		lConfigSources.add(new ConfigSource(LocationType.EtcDirectory, new File(CONFIG_ETC, CONFIG_FILE_NAME)));

		// determine first file being available
		ConfigSource lConfigSourceToUse = null;
		for (ConfigSource currIniFile : lConfigSources) {

			if (currIniFile.isAvailable()) {
				lConfigSourceToUse = currIniFile;
				break;
			} else {
				LogHelper.logInfo(ConfigHelper.class, "Tried {0}: {1}", currIniFile.getLocationType().name(),
						currIniFile.getPath());
			} // else
		} // for

		// report missing configuration file
		if (lConfigSourceToUse == null) {
			throw new IllegalArgumentException("Config file could not be found in any of the above locations!");
		} // if

		// report and load configuration
		LogHelper.logInfo(ConfigHelper.class, "Loading configuration from [{0}]: {1}",
				lConfigSourceToUse.getLocationType().name(), lConfigSourceToUse.getPath());
		try {
			outProperties.load(lConfigSourceToUse.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(
					"Properties could not be loaded from file [" + lConfigSourceToUse.getPath() + "]!");
		} // catch

		return outProperties;
	}

	/**
	 * Utility constructor.
	 */
	private ConfigHelper() {
		super();
	}
}