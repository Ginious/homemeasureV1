package ginious.home.measure.server;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ginious.home.measure.cache.MeasureCache;
import ginious.home.measure.cache.MeasureCacheHelper;
import ginious.home.measure.cache.MeasureListener;
import ginious.home.measure.cache.serializer.MeasuresSerializerFactory;
import ginious.home.measure.device.MeasurementDevice;
import ginious.home.measure.device.MeasurementDeviceInitializationException;
import ginious.home.measure.recorder.Recorder;
import ginious.home.measure.server.service.Service;
import ginious.home.measure.util.ConfigHelper;
import ginious.home.measure.util.LogHelper;

/**
 * Home Measure Server.
 */
public final class HMServer {

	/**
	 * The type of service handling the kind of output of measures (other service,
	 * text based).
	 */
	private enum ServiceType {
	MQTT("ginious.home.measure.server.service.MqttPublisherService"), //
	WS("ginious.home.measure.server.service.WebService");

		private String className;

		private ServiceType(String inServiceClassName) {
			className = inServiceClassName;
		}

		public String getClassName() {
			return className;
		}
	};

	private static final String CONFIG_KEY_RECORDERS = "RECORDERS";
	private static final String CONFIG_KEY_SEPARATOR = ".";
	private static final String CONFIG_KEY_SERIALIZERS = "SERIALIZERS";
	private static final String CONFIG_KEY_SERVICE = "SERVICE";
	private static final String DEVICE_TYPE_POSTFIX = CONFIG_KEY_SEPARATOR + "TYPE";

	/**
	 * Starts the server.
	 * 
	 * @param inArgs No properties required - configuration will be loaded from one
	 *               of the hmserver.ini files.
	 */
	public static void main(String[] inArgs) {

		LogHelper.logInfo(HMServer.class, "HMServer starting ...");
		try {
			new HMServer().startup();
		} catch (IllegalArgumentException e) {
			LogHelper.logError(HMServer.class, "Server could not be started - Reason: " + e.getMessage());
			System.exit(1);
		} catch (Throwable t) {
			LogHelper.logError(HMServer.class, t, "Server died due to a serious problem!");
			System.exit(1);
		} // catch
	}

	/**
	 * The cache of measures.
	 */
	private MeasureCache cache;

	/**
	 * The list of devices currently running.
	 */
	private List<MeasurementDevice> runningDevices = new ArrayList<>();

	/**
	 * The underlying service.
	 */
	private Service service;

	/**
	 * The type of the underlying service.
	 */
	private ServiceType typeOfService;

	/**
	 * All listeners that will finally be attached to the measure cache (i.e.
	 * Recorders, MQTT service).
	 */
	private List<MeasureListener> listeners = new ArrayList<>();

	/**
	 * Default application constructor.
	 */
	public HMServer() {
		super();
	}

	/**
	 * Initialization of the specific device configuration.
	 * 
	 * @param aDevice       The device to initialize.
	 * @param aDeviceConfig The device specific configuration.
	 */
	private void initDeviceConfig(MeasurementDevice aDevice, Properties aDeviceConfig) {

		for (String lCurrSettingName : aDeviceConfig.stringPropertyNames()) {
			String lDeviceSettingPrefix = aDevice.getId() + CONFIG_KEY_SEPARATOR;
			if (lCurrSettingName.startsWith(lDeviceSettingPrefix)
					&& !StringUtils.endsWith(lCurrSettingName, DEVICE_TYPE_POSTFIX)) {
				String lDeviceSettingName = StringUtils.substringAfter(lCurrSettingName, lDeviceSettingPrefix);
				String lDeviceSettingValue = aDeviceConfig.getProperty(lCurrSettingName);
				aDevice.setSetting(lDeviceSettingName, lDeviceSettingValue);
			} // if
		} // for
	}

	/**
	 * Initialization of available devices.
	 * 
	 * @param aOverallDeviceConfig The overall application configuration from
	 *                             hmserver.ini
	 * @return The list of devices that were created.
	 * @throws MeasurementDeviceInitializationException
	 */
	private List<MeasurementDevice> initDevices(Properties aOverallDeviceConfig)
			throws MeasurementDeviceInitializationException {

		List<MeasurementDevice> lDevices = new ArrayList<>();
		for (String lCurrSetting : aOverallDeviceConfig.stringPropertyNames()) {

			if (lCurrSetting.endsWith(DEVICE_TYPE_POSTFIX)) {

				String lDeviceId = StringUtils.substringBefore(lCurrSetting, CONFIG_KEY_SEPARATOR);
				MeasurementDevice lDevice;
				String lDeviceTypeName = aOverallDeviceConfig.getProperty(lCurrSetting);
				try {
					Class<?> lDeviceClass = Class.forName(lDeviceTypeName);
					Constructor<?> lDeviceConstructor = lDeviceClass.getConstructor(String.class);
					lDevice = (MeasurementDevice) lDeviceConstructor.newInstance(lDeviceId);
				} catch (Throwable t) {
					throw new MeasurementDeviceInitializationException(lDeviceTypeName, t.getMessage());
				} // catch

				initDeviceConfig(lDevice, aOverallDeviceConfig);
				lDevice.initDevice();

				lDevices.add(lDevice);
				LogHelper.logInfo(this, "Initialized device [{0}] from type [{1}]", lDevice.getId(),
						lDevice.getClass().getName());
			} // if
		} // while

		return lDevices;
	}

	/**
	 * Initialization of optionally defined recorders.
	 * 
	 * @param inApplicationProps The overall application properties.
	 * @param aDeviceList        The list of all devices that provide changed
	 *                           measures that finally will be recorded.
	 */
	private void initRecorders(Properties inApplicationProps, List<MeasurementDevice> aDeviceList) {

		// Recorders
		String lRecorderClassNames = inApplicationProps.getProperty(CONFIG_KEY_RECORDERS);
		String[] lClassNames = StringUtils.split(lRecorderClassNames, " ,;:");
		if (lClassNames != null) {
			for (String lCurrRecorderClassName : lClassNames) {
				try {
					Class<?> lRecorderClass = Class.forName(lCurrRecorderClassName);
					Constructor<?> lConstr = lRecorderClass.getConstructor(Properties.class);
					Recorder lRecorder = (Recorder) lConstr.newInstance(inApplicationProps);
					listeners.add(lRecorder);
				} catch (Throwable t) {
					LogHelper.logError(this, t, "Failed to create logger!");
					throw new IllegalArgumentException(
							"Recorder [" + lCurrRecorderClassName + "] could not be found or initialized!", t);
				} // catch
			} // for
		} // if
	}

	/**
	 * Initialization of optionally defined serializers.
	 * 
	 * @param inApplicationProps The overall application properties.
	 */
	private void initSerializers(Properties inApplicationProps) {

		String lSerializerClassNames = inApplicationProps.getProperty(CONFIG_KEY_SERIALIZERS);
		String[] lClassNames = StringUtils.split(lSerializerClassNames, " ,;:");
		if (lClassNames != null) {
			MeasuresSerializerFactory.init(lClassNames);
		} // if
	}

	/**
	 * Initialization and instantiation of defined service based on underling properties.
	 * 
	 * @param lApplicationProps The overall application properties.
	 */
	@SuppressWarnings("unchecked")
	private void initService(Properties lApplicationProps) {

		// get desired service ID from hmserver.ini and ensure that only one was defined
		typeOfService = ServiceType.WS;
		String serviceProperty = lApplicationProps.getProperty(CONFIG_KEY_SERVICE);
		if (StringUtils.contains(serviceProperty, ',')) {
			throw new IllegalArgumentException(CONFIG_KEY_SERVICE + "=" + serviceProperty
					+ " - only one service can be started for an instance of HMServer!");
		} // if

		// ensure that defined service exists
		try {
			typeOfService = ServiceType.valueOf(serviceProperty);
		} catch (Throwable t) {
			StringBuilder lBuilder = new StringBuilder();
			for (ServiceType lCurrMode : ServiceType.values()) {
				lBuilder.append(lCurrMode);
				lBuilder.append(" ");
			} // for
			throw new IllegalArgumentException("Service [" + serviceProperty + "] is not supported - use one of: "
					+ lBuilder.toString().trim().replace(' ', ','));
		} // catch

		// load service class and instantiate service
		try {
			Class<Service> serviceClass = (Class<Service>) Class.forName(typeOfService.getClassName());
			Constructor<Service> serviceConstructor = serviceClass.getConstructor(Properties.class);
			Properties lProperties = ConfigHelper.extractProperties(lApplicationProps,
					typeOfService.name().toLowerCase());
			service = serviceConstructor.newInstance(lProperties);
		} catch (Throwable t) {
			throw new RuntimeException("Web Service konnte nicht gestartet werden!", t);
		} // catch
		
		// some services require themselves to act as listener for measurements
		// all listeners will finally be attached to the central measure cache
		if (MeasureListener.class.isAssignableFrom(service.getClass())) {
			listeners.add((MeasureListener) service);
		} // if
	}

	/**
	 * Shuts down all devices and finally the service.
	 */
	private void shutdown() {

		for (MeasurementDevice lCurrDevice : runningDevices) {
			lCurrDevice.switchOff();
		} // for

		service.stopService();
	}

	/**
	 * Starts each of the given devices.
	 * 
	 * @param inDevices The devices to start.
	 */
	private void startDevices(List<MeasurementDevice> inDevices) {

		for (MeasurementDevice lCurrDevice : inDevices) {

			runningDevices.add(lCurrDevice);

			LogHelper.logInfo(this, "Starting device [{0}/{1}] ...", lCurrDevice.getClass().getSimpleName(),
					lCurrDevice.getId());

			Runnable lRunnableDevice = new RunnableMeasureDeviceSupport(lCurrDevice);
			Thread lDeviceThread = new Thread(lRunnableDevice, lCurrDevice.getClass().getSimpleName());
			lDeviceThread.start();
		} // for
	}

	/**
	 * Starts the service that is configured.
	 */
	private void startService() {

		LogHelper.logInfo(this, "Starting service [{0}] ...", typeOfService.name(), service.getClass().getSimpleName());

		service.setMeasureCache(cache);
		service.startService();
	}

	/**
	 * Loads the overall configuration, performs general initialization and starts
	 * devices and the corresponding data service.
	 * 
	 * @throws MeasurementDeviceInitializationException In case that a device could
	 *                                                  not be initialized properly.
	 */
	public void startup() throws MeasurementDeviceInitializationException {

		// load configuration
		Properties lApplicationProps = ConfigHelper.loadConfiguration();

		// perform initialization steps
		List<MeasurementDevice> lDevices = initDevices(lApplicationProps);
		initService(lApplicationProps);
		initSerializers(lApplicationProps);
		initRecorders(lApplicationProps, lDevices);

		// create central cache for measures
		cache = MeasureCacheHelper.createCache(lDevices, listeners);

		// start data service and devices
		startDevices(lDevices);
		startService();
	}
}