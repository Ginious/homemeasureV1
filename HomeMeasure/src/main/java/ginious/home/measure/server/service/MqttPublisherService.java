package ginious.home.measure.server.service;

import java.util.Properties;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import ginious.home.measure.Measure;
import ginious.home.measure.cache.MeasureCache;
import ginious.home.measure.cache.MeasureListener;
import ginious.home.measure.util.ConfigHelper;
import ginious.home.measure.util.LogHelper;

/**
 * Service publishing measure changes to a MQTT broker.
 */
public final class MqttPublisherService implements Service, MeasureListener {

	private static final String TOPIC_PATH_SEPARATOR = "/";
	private static final String DEFAULT_TOPIC_ROOT = "homemeasure/";

	private static final String BROKER_PASSWORD = "BROKERPASSWORD";
	private static final String BROKER_URL = "BROKERURL";
	private static final String BROKER_USER = "BROKERUSER";
	private static final String TOPIC_ROOT = "MQTT_TOPIC_ROOT";

	private String brokerUrl;
	private String brokerUser;
	private String brokerPassword;
	private String topicRoot;

	/**
	 * MQTT broker client for publishing measures.
	 */
	private MqttClient brokerClient;

	/**
	 * Default constructor.
	 * 
	 * @param inMCache       The cache about to be published by this web service.
	 * @param inServiceProps The properties for this service.
	 */
	public MqttPublisherService(MeasureCache inMCache, Properties inServiceProps) {
		super();

		inMCache.addMeasureListener(this);

		ConfigHelper.validatePropertyExistence(inServiceProps, BROKER_URL);
		brokerUrl = ConfigHelper.getTextProperty(inServiceProps, BROKER_URL, null);
		brokerUser = ConfigHelper.getTextProperty(inServiceProps, BROKER_USER, null);
		brokerPassword = ConfigHelper.getTextProperty(inServiceProps, BROKER_PASSWORD, null);
		topicRoot = ConfigHelper.getTextProperty(inServiceProps, TOPIC_ROOT, DEFAULT_TOPIC_ROOT);
		if (!topicRoot.endsWith(TOPIC_PATH_SEPARATOR)) {
			topicRoot += TOPIC_PATH_SEPARATOR;
		} // if
	}

	@Override
	public void startService() {
		// nothing needs to be done here since devices are started and are keeping the
		// service running
	}

	@Override
	public void stopService() {

	}

	@Override
	public void measureChanged(Measure inChangedMeasure) {

		try {
			MqttClient lClient = getBrokerClient();
			if (lClient != null) {
				lClient.publish(topicRoot + inChangedMeasure.getId(), inChangedMeasure.getValue().getBytes(), 0, false);
			} // if
		} catch (MqttException e) {
			LogHelper.logError(this,
					"Failed to publish changed measure [{0}={1}] of device [{2}] to MQTT broker - Reason: {3}",
					inChangedMeasure.getId(), inChangedMeasure.getValue(), inChangedMeasure.getDeviceId(),
					e.getMessage());
		} // catch
	}

	/**
	 * Creates and gets the client used for accessing the MQTT broker. Creation will
	 * only take place if this is the first call or the connection is not open
	 * anymore.
	 * 
	 * @return The client or <code>null</code> in case that no connection could be
	 *         established.
	 */
	private MqttClient getBrokerClient() {

		if (brokerClient != null || !brokerClient.isConnected()) {

			MqttConnectOptions lOptions = new MqttConnectOptions();
			lOptions.setConnectionTimeout(60);
			lOptions.setAutomaticReconnect(true);

			// User
			if (brokerUser != null) {
				lOptions.setUserName(brokerUser);
			} // if

			// Password
			if (brokerPassword != null) {
				lOptions.setPassword(brokerPassword.toCharArray());
			} // if

			// connect
			try {
				brokerClient = new MqttClient(brokerUrl, getClass().getName());
				brokerClient.connect(lOptions);
			} catch (MqttException e) {
				LogHelper.logError(this, "Failed to connect to MQTT broker [{0}] - Reason: {1}", brokerUrl,
						e.getMessage());
			} // catch
		} // if

		return brokerClient;
	}
}