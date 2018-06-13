package ginious.home.measure.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ginious.home.measure.cache.MeasureCache;
import ginious.home.measure.cache.MeasureCacheFactory;
import ginious.home.measure.cache.MeasureListener;
import ginious.home.measure.cache.serializer.MeasuresSerializer;
import ginious.home.measure.cache.serializer.MeasuresSerializerFactory;
import ginious.home.measure.device.MeasurementDevice;
import ginious.home.measure.device.MeasurementDeviceNotFoundException;
import ginious.home.measure.recorder.Recorder;
import io.javalin.Context;
import io.javalin.Javalin;

/**
 *
 */
public class HMServer {

	private static final Logger LOG = Logger.getLogger(HMServer.class.getName());

	private static final String CONFIG_FILE = "hmserver.properties";

	private static final String CONFIG_ETC = "/etc";
	private static final String CONFIG_HOME = "/hmserver/config";

	private static final String CONFIG_PARAM_RECORDERS = "RECORDERS";
	private static final String CONFIG_PARAM_SERIALIZERS = "SERIALIZERS";

	private static final String DEVICE_CONFIG_SEPARATOR = ".";

	private static final String DEVICE_TYPE_POSTFIX = DEVICE_CONFIG_SEPARATOR + "TYPE";
	private static final String WEB_PARAM_SERIALIZER_ID = "serializer";

	public static void main(String[] args) throws MeasurementDeviceNotFoundException {

		LOG.info("HMServer starting ...");
		try {
		new HMServer().startup();
		} catch(Throwable t) {
			LOG.log(Level.SEVERE, "Server reported a serious problem!", t);
		}
	}

	private MeasureCache cache;

	private List<MeasurementDevice> runningDevices = new ArrayList<>();

	public HMServer() {
		super();
	}

	private void switchDevicesOn(List<MeasurementDevice> inDevices) {

		for (MeasurementDevice lCurrDevice : inDevices) {

			runningDevices.add(lCurrDevice);

			Runnable lRunnableDevice = new RunnableMeasureDeviceSupport(lCurrDevice);
			Thread lDeviceThread = new Thread(lRunnableDevice, lCurrDevice.getClass().getSimpleName());
			lDeviceThread.start();
		} // for
	}

	private void initDeviceConfig(MeasurementDevice aDevice, Properties aDeviceConfig) {

		for (String lCurrSettingName : aDeviceConfig.stringPropertyNames()) {
			String lDeviceSettingPrefix = aDevice.getId() + DEVICE_CONFIG_SEPARATOR;
			if (lCurrSettingName.startsWith(lDeviceSettingPrefix)
					&& !StringUtils.endsWith(lCurrSettingName, DEVICE_TYPE_POSTFIX)) {
				String lDeviceSettingName = StringUtils.substringAfter(lCurrSettingName, lDeviceSettingPrefix);
				String lDeviceSettingValue = aDeviceConfig.getProperty(lCurrSettingName);
				aDevice.setSetting(lDeviceSettingName, lDeviceSettingValue);
			} // if
		} // for
	}

	/**
	 * Loads configuration from <b>USER_HOME/hmserver/config/hmserver.properties</b>
	 * oder from <b>/etc/hmserver/config/hmserver.properties</b> if the file is not
	 * provided in USER_HOME.
	 * 
	 * @return The loaded configuration.
	 */
	private Properties loadConfiguration() {

		Properties outProperties = new Properties();

		String CONFIG_USER_HOME = "user.home";
		File lConfigFile = new File(System.getProperty(CONFIG_USER_HOME) + CONFIG_HOME, CONFIG_FILE);
		if (!lConfigFile.exists()) {
			lConfigFile = new File(DEVICE_CONFIG_SEPARATOR, CONFIG_ETC + CONFIG_HOME + "/" + CONFIG_FILE);
		} // if
		if (!lConfigFile.exists()) {
			lConfigFile = new File(CONFIG_ETC + CONFIG_HOME + "/" + CONFIG_FILE);
		}
		Validate.isTrue(lConfigFile.exists(), "Config folder [" + lConfigFile + "] could not be found!");

		LOG.info("Loading configuration from " + lConfigFile.getAbsolutePath());
		try {
			outProperties.load(new FileInputStream(lConfigFile));
		} catch (IOException e) {
			throw new RuntimeException(
					"Properties could not be loaded from file [" + lConfigFile.getAbsolutePath() + "]!");
		} // catch

		return outProperties;
	}

	private Properties extractProperties(Properties inProperties, String inPrefix) {

		Properties outProps = new Properties();

		for (String lCurrPropName : inProperties.stringPropertyNames()) {
			if (lCurrPropName.startsWith(inPrefix)) {
				String lPropNameWithoutPrefix = StringUtils.remove(lCurrPropName, inPrefix + ".");
				outProps.setProperty(lPropNameWithoutPrefix, inProperties.getProperty(lCurrPropName));
			} // if
		} // while

		return outProps;
	}

	private List<MeasurementDevice> prepareDevices(Properties aOverallDeviceConfig)
			throws MeasurementDeviceNotFoundException {

		List<MeasurementDevice> lDevices = new ArrayList<>();
		for (String lCurrSetting : aOverallDeviceConfig.stringPropertyNames()) {

			if (lCurrSetting.endsWith(DEVICE_TYPE_POSTFIX)) {

				MeasurementDevice lDevice;
				String lDeviceTypeName = aOverallDeviceConfig.getProperty(lCurrSetting);
				try {
					Class<?> lDeviceClass = Class.forName(lDeviceTypeName);
					lDevice = (MeasurementDevice) lDeviceClass.newInstance();
				} catch (Throwable t) {
					throw new MeasurementDeviceNotFoundException(lDeviceTypeName, t.getMessage());
				} // catch

				initDeviceConfig(lDevice, aOverallDeviceConfig);

				lDevices.add(lDevice);
				LOG.info("Added Device [" + lDevice.getId() + "] of type [" + lDevice.getClass().getName() + "].");
			} // if
		} // while

		return lDevices;
	}

	private String serializeMeasures(Context inContext) {

		String lSerializerId = inContext.request().getParameter(WEB_PARAM_SERIALIZER_ID);
		MeasuresSerializer lSerializer = MeasuresSerializerFactory.getSerializer(lSerializerId);

		return lSerializer.serialize(cache);
	}

	private void shutdown() {

		for (MeasurementDevice lCurrDevice : runningDevices) {
			lCurrDevice.switchOff();
		} // for
	}

	private void startup() throws MeasurementDeviceNotFoundException {

		Properties lApplicationProps = loadConfiguration();
		List<MeasurementDevice> lDevices = prepareDevices(lApplicationProps);
		List<MeasureListener> lMeasureListeners = prepareMessageListeners(lApplicationProps);
		cache = MeasureCacheFactory.createCache(lDevices, lMeasureListeners);
		initSerializers(lApplicationProps);
		switchDevicesOn(lDevices);
		startWeb();
	}

	/**
	 * Initializes custom serializers that are provided in the settings as separated
	 * values.
	 * 
	 * @param inApplicationProps
	 *            The application properties.
	 */
	private void initSerializers(Properties inApplicationProps) {

		String lSerializerClassNames = inApplicationProps.getProperty(CONFIG_PARAM_SERIALIZERS);
		String[] lClassNames = StringUtils.split(lSerializerClassNames, " ,;:");
		if (lClassNames != null) {
			MeasuresSerializerFactory.init(lClassNames);
		} // if
	}

	/**
	 * Initializes listeners that are provided in the settings as separated values.
	 * 
	 * @param inApplicationProps
	 *            The application properties.
	 * @return The listeners.
	 */
	private List<MeasureListener> prepareMessageListeners(Properties inApplicationProps) {

		List<MeasureListener> outListeners = new ArrayList<>();

		// Recorders
		String lRecorderClassNames = inApplicationProps.getProperty(CONFIG_PARAM_RECORDERS);
		String[] lClassNames = StringUtils.split(lRecorderClassNames, " ,;:");
		if (lClassNames != null) {
			for (String lCurrRecorderClassName : lClassNames) {
				try {
					Class<?> lRecorderClass = Class.forName(lCurrRecorderClassName);
					Recorder lRecorder = (Recorder) lRecorderClass.newInstance();
					lRecorder.initialize(extractProperties(inApplicationProps, lRecorder.getId()));
					outListeners.add(lRecorder);
				} catch (Throwable t) {
					throw new IllegalArgumentException(
							"Recorder [" + lCurrRecorderClassName + "] could not be found or initialized!", t);
				} // catch
			} // for
		} // if

		return outListeners;
	}

	private void startWeb() {

		Javalin lServiceBuilder = Javalin.create();
		lServiceBuilder.enableStandardRequestLogging();
		Javalin app = Javalin.start(7000);
		app.get("/", ctx -> ctx.result(serializeMeasures(ctx)));
	}
}