package ginious.home.measure.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Represents the source of a configuration file.
 */
final class ConfigSource {

	/**
	 * Type of config file location.
	 */
	enum LocationType {
	WorkingDirectory, Classpath, UserHome, ConfDirectory, EtcDirectory;
	}

	private LocationType type;
	private String path;
	private boolean available;
	private InputStream inputStream;

	/**
	 * File constructor.
	 * 
	 * @param inType The type of location.
	 * @param inFile The specific configuration file.
	 */
	ConfigSource(LocationType inType, File inFile) {
		super();

		type = inType;
		available = inFile != null && inFile.exists();
		path = inFile != null ? inFile.getAbsolutePath() : null;
	}

	/**
	 * Stream constructor.
	 * 
	 * @param inType        The type of location.
	 * @param inPath        The path from where the stream originates.
	 * @param inInputStream The stream.
	 */
	ConfigSource(LocationType inType, String inPath, InputStream inInputStream) {
		super();

		type = inType;
		path = inPath;
		inputStream = inInputStream;

		available = inInputStream != null;
	}

	/**
	 * Gets the input stream from the underlying source.
	 * 
	 * @return The input stream.
	 */
	InputStream getInputStream() {

		if (inputStream != null) {
			return inputStream;
		} else {
			try {
				return new FileInputStream(path);
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Configuration file could not be loaded from " + getPath());
			} // catch
		} // else
	}

	/**
	 * Gets the type of location.
	 * 
	 * @return The type of location.
	 */
	LocationType getLocationType() {
		return type;
	}

	/**
	 * Gets the path of the underlying file or the path from where the stream
	 * originated.
	 * 
	 * @return The path.
	 */
	String getPath() {
		return path;
	}

	/**
	 * Gets whether or not the source is available.
	 * 
	 * @return <code>true</code> when a config could be found, <code>false</code>
	 *         otherwise.
	 */
	boolean isAvailable() {
		return available;
	}
}