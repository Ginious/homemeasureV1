package ginious.home.measure.recorder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ginious.home.measure.Measure;

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

	public DatabaseRecorder() {
		super("database-recorder");
	}

	private Connection getConnection() throws SQLException {

		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(true);
		} // if

		return connection;
	}

	/**
	 * 
	 * @param inProps
	 * @param inPropName
	 * @return
	 */
	private String getProperty(Properties inProps, String inPropName) {

		String outProperty = inProps.getProperty(inPropName);
		Validate.isTrue(StringUtils.isNotBlank(outProperty),
				"Property [" + getId() + "." + inPropName + "] is not provided!");

		return outProperty;
	}

	@Override
	protected void initializeCustom(Properties inProps) {

		url = getProperty(inProps, DB_URL);
		user = getProperty(inProps, DB_USER);
		password = getProperty(inProps, DB_PASSWORD);
	}

	@Override
	protected void measureChangedCustom(String inDeviceId, Measure inChangedMeasure) {

		String lInsert = String.format("INSERT INTO %s (%s,%s,%s,%s) VALUES (?,?,?,?)", TABLE_NAME, COLUMN_NAME_DEVICE,
				COLUMN_NAME_DATA, COLUMN_NAME_VALUE, COLUMN_NAME_TIMESTAMP);
		try {
			PreparedStatement lPrepStmt = getConnection().prepareStatement(lInsert);
			lPrepStmt.setString(1, inDeviceId);
			lPrepStmt.setString(2, inChangedMeasure.getID());
			lPrepStmt.setString(3, inChangedMeasure.getValue());
			lPrepStmt.setTimestamp(4, new Timestamp(new Date().getTime()));

			lPrepStmt.execute();
		} catch (SQLException e) {
			throw new RuntimeException("Error writing Measure to DB!", e);
		} // catch
	}
}