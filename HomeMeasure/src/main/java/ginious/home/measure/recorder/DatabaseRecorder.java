package ginious.home.measure.recorder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import ginious.home.measure.Measure;

/**
 * A recorder that is capable of writing measures into a relational database.
 */
public final class DatabaseRecorder extends AbstractRecorder {

	private static final String COLUMN_NAME_DATA = "measure_id";
	private static final String COLUMN_NAME_DEVICE = "device_id";
	private static final String COLUMN_NAME_TIMESTAMP = "measure_timestamp";
	private static final String COLUMN_NAME_VALUE = "measure_value";

	private static final String DB_PASSWORD = "PASSWORD";
	private static final String DB_URL = "URL";
	private static final String DB_USER = "USER";

	private static final String TABLE_NAME = "hmdata";

	private Connection connection;
	private String password;
	private String url;
	private String user;

	/**
	 * Default constructor.
	 * 
	 * @param inProperties The overall application properties.
	 */
	public DatabaseRecorder(Properties inProperties) {
		super("database-recorder", inProperties);
	}

	/**
	 * Creates a connection to the database.
	 * 
	 * @return The connection.
	 * @throws SQLException In case that no connection could be retrieved.
	 */
	private Connection getConnection() throws SQLException {

		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(true);
		} // if

		return connection;
	}

	@Override
	public void initialize(Properties inProps) {

		url = getProperty(inProps, DB_URL, true);
		user = getProperty(inProps, DB_USER, true);
		password = getProperty(inProps, DB_PASSWORD, true);
	}

	@Override
	protected void measureChangedCustom(Measure inChangedMeasure) {

		String lInsert = String.format("INSERT INTO %s (%s,%s,%s,%s) VALUES (?,?,?,?)", TABLE_NAME, COLUMN_NAME_DEVICE,
				COLUMN_NAME_DATA, COLUMN_NAME_VALUE, COLUMN_NAME_TIMESTAMP);
		try {
			PreparedStatement lPrepStmt = getConnection().prepareStatement(lInsert);
			lPrepStmt.setString(1, inChangedMeasure.getDeviceId());
			lPrepStmt.setString(2, inChangedMeasure.getId());
			lPrepStmt.setString(3, inChangedMeasure.getValue());
			lPrepStmt.setTimestamp(4, new Timestamp(new Date().getTime()));

			lPrepStmt.execute();
		} catch (SQLException e) {
			throw new RuntimeException("Error writing Measure to DB!", e);
		} // catch
	}
}