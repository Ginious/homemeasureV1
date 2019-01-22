package ginious.home.measure.server.service;

/**
 * Interface for a service that can be started and stopped.
 */
public interface Service {

	/**
	 * Starts the service.
	 */
	void startService();

	/**
	 * Stops the service.
	 */
	void stopService();
}
