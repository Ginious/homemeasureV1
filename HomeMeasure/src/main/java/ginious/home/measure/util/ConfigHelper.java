package ginious.home.measure.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;

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

		String lSourceLocation = "classpath://" + CONFIG_FILE_NAME;
		try {
			InputStream lConfigInputStream = ConfigHelper.class.getResourceAsStream("/" + CONFIG_FILE_NAME);
			if (lConfigInputStream == null) {

				// check all possible file locations
				File lConfigFile_1_user_home = new File(CONFIG_USER_HOME, "." + CONFIG_FILE_NAME);
				File lConfigFile_2_conf = new File(CONFIG_CONF, CONFIG_FILE_NAME);
				File lConfigFile_3_etc = new File(CONFIG_ETC, CONFIG_FILE_NAME);

				if (lConfigFile_1_user_home.exists()) {

					// (1) load absolute from ".hmserver.ini" in user home directory
					lConfigInputStream = new FileInputStream(lConfigFile_1_user_home);
					lSourceLocation = lConfigFile_1_user_home.getAbsolutePath();
				} else if (lConfigFile_2_conf.exists()) {

					// (2) load relative from "conf/hmserver.ini" where application was started
					lConfigInputStream = new FileInputStream(lConfigFile_2_conf);
					lSourceLocation = lConfigFile_2_conf.getAbsolutePath();
				} else if (lConfigFile_3_etc.exists()) {

					// (3) load absolute from "/etc/hmserver.ini"
					lConfigInputStream = new FileInputStream(lConfigFile_3_etc);
					lSourceLocation = lConfigFile_3_etc.getAbsolutePath();
				} else {

					throw new IllegalArgumentException("Config file could not be found - tried in following order: " //
							+ "\n\t1. classpath://" + CONFIG_FILE_NAME //
							+ "\n\t2. " + lConfigFile_1_user_home.getAbsolutePath() //
							+ "\n\t3. " + lConfigFile_2_conf.getAbsolutePath() //
							+ "\n\t4. " + lConfigFile_3_etc.getAbsolutePath());
				} // if
			} // if

			LogHelper.logInfo(ConfigHelper.class, "Loading configuration from [{0}] ...", lSourceLocation);

			outProperties.load(lConfigInputStream);
		} catch (IOException e) {
			throw new RuntimeException("Properties could not be loaded from file [" + lSourceLocation + "]!");
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