package ginious.home.measure.server.service;

import ginious.home.measure.cache.MeasureCache;

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

	/**
	 * Lets the service connect to the cache of measurements. The cache is required
	 * in order to let a service ie. publish or print (or whatever) changed
	 * measurements.
	 * 
	 * @param aCache The cache the service should use to publish measurements using
	 *               any channel.
	 */
	void setMeasureCache(MeasureCache aCache);
}
