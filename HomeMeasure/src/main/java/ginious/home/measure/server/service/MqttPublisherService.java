package ginious.home.measure.server.service;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
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

	private static final String BROKER_PASSWORD = "PASSWORD";
	private static final String BROKER_URI = "URI";
	private static final String BROKER_USER = "USER";
	private static final String TOPIC_ROOT = "TOPIC_ROOT";

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
	 * @param inServiceProps The properties for this service.
	 */
	public MqttPublisherService(Properties inServiceProps) {
		super();

		ConfigHelper.validatePropertyExistence(inServiceProps, BROKER_URI);
		brokerUrl = ConfigHelper.getTextProperty(inServiceProps, BROKER_URI, null);
		brokerUser = ConfigHelper.getTextProperty(inServiceProps, BROKER_USER, null);
		brokerPassword = ConfigHelper.getTextProperty(inServiceProps, BROKER_PASSWORD, null);
		topicRoot = ConfigHelper.getTextProperty(inServiceProps, TOPIC_ROOT, DEFAULT_TOPIC_ROOT);
		if (!topicRoot.endsWith(TOPIC_PATH_SEPARATOR)) {
			topicRoot += TOPIC_PATH_SEPARATOR;
		} // if
	}

	@Override
	public void setMeasureCache(MeasureCache inMCache) {

		inMCache.addMeasureListener(this);
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
				lClient.publish(topicRoot + inChangedMeasure.getDeviceId() + "/" + inChangedMeasure.getId(),
						inChangedMeasure.getValue().getBytes(), 0, false);
				LogHelper.logInfo(this, "Published measure for device [{0}]: name={1}, value={2}",
						inChangedMeasure.getDeviceId(), inChangedMeasure.getId(), inChangedMeasure.getValue());
			} else {
				LogHelper.logWarning(this,
						"Skipped publishing of measure for device [{0}] due to previous problem: name={1}, value={2}",
						inChangedMeasure.getDeviceId(), inChangedMeasure.getId(), inChangedMeasure.getValue());
			} // else
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

		if (brokerClient == null || !brokerClient.isConnected()) {

			MqttConnectOptions lOptions = new MqttConnectOptions();
			lOptions.setConnectionTimeout(60);
			lOptions.setAutomaticReconnect(false);

			// User
			if (StringUtils.isNotBlank(brokerUser)) {
				lOptions.setUserName(brokerUser);
			} // if

			// Password
			if (StringUtils.isNotBlank(brokerPassword)) {
				lOptions.setPassword(brokerPassword.toCharArray());
			} // if

			// connect
			try {
				brokerClient = new MqttClient(brokerUrl, getClass().getName());
				brokerClient.connect(lOptions);
			} catch (MqttException e) {
				LogHelper.logError(this, e, "Failed to connect to MQTT broker [{0}]", brokerUrl);
			} // catch
		} // if

		return brokerClient;
	}
}