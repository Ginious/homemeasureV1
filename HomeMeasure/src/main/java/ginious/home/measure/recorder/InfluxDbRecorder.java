package ginious.home.measure.recorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import ginious.home.measure.Measure;

/**
 * Recorder for writing measure data into an InfluxDB.
 * One InfluxDB measure will be created per device containing the HMServer measure id and its value.
 */
public final class InfluxDbRecorder extends AbstractRecorder {

  private static final String POINT_FIELD_VALUE = "value";
  private static final String POINT_TAG_MEASURE_ID = "measure_id";
  private static final String QUERY_CREATE_DATABASE = "CREATE DATABASE %s";
  private static final String QUERY_SHOW_DATABASES = "SHOW DATABASES";

  private static final String DB_NAME = "DBNAME";
  private static final String DB_URL = "URL";
  private static final String DB_USER = "USER";
  private static final String DB_PASSWORD = "PASSWORD";
  private static final String DB_FLUSH_DURATION = "FLUSHDURATION";

  private static final String DEFAULT_DATABASE_NAME = "HMServer";
  private static final String DEFAULT_USER = "root";
  private static final String DEFAULT_PASSWORD = "root";
  private static final int DEFAULT_FLUSH_DURATION = 60_000; // 1 minute

  private InfluxDB influxDB;

  private String url;
  private String user;
  private String password;
  private String dbname;
  private int flushDuration;

  /**
   * Default constructor.
   * 
   * @param inProperties
   *          The overall application properties.
   */
  public InfluxDbRecorder(Properties inProperties) {
    super("influxdb-recorder", inProperties);
  }

  @Override
  public void initialize(Properties inProperties) {

    url = getProperty(inProperties, DB_URL, true);

    user = getProperty(inProperties, DB_USER, false);
    if (StringUtils.isBlank(user)) {
      user = DEFAULT_USER;
    } // if

    password = getProperty(inProperties, DB_PASSWORD, false);
    if (StringUtils.isBlank(password)) {
      password = DEFAULT_PASSWORD;
    } // if

    dbname = getProperty(inProperties, DB_NAME, false);
    if (StringUtils.isBlank(dbname)) {
      dbname = DEFAULT_DATABASE_NAME;
    } // if

    flushDuration = NumberUtils.toInt(getProperty(inProperties, DB_FLUSH_DURATION, false),
        DEFAULT_FLUSH_DURATION);
  }

  @Override
  protected void measureChangedCustom(Measure inChangedMeasure) {

    Number valueAsNumber = null;
    try {
      valueAsNumber = NumberUtils.createNumber(inChangedMeasure.getValue().trim());
    }
    catch (NumberFormatException e) {
      // ignore and do not write measure
    } // catch

    if (valueAsNumber != null) {

      getInfluxDB().write(Point//
          .measurement(inChangedMeasure.getDeviceId()) //
          .tag(POINT_TAG_MEASURE_ID, inChangedMeasure.getId()) //
          .addField(POINT_FIELD_VALUE, valueAsNumber).build());
    } // if
  }

  /**
   * Gets a connection to the InfluxDB.
   * 
   * @return The connection to the InfluxDB.
   */
  private InfluxDB getInfluxDB() {

    if (influxDB == null) {

      influxDB = InfluxDBFactory.connect(url, user, password);

      // gather existing database names
      final List<String> existingDbs = new ArrayList<String>();
      QueryResult dbList = influxDB.query(new Query(QUERY_SHOW_DATABASES));
      dbList.getResults().forEach( //
          result -> result.getSeries().forEach( //
              serie -> serie.getValues().forEach( //
                  value -> value.forEach(//
                      dbname -> existingDbs.add(String.valueOf(dbname))))));

      // create database if not yet existing
      if (!existingDbs.contains(DEFAULT_DATABASE_NAME)) {
        influxDB.query(new Query(String.format(QUERY_CREATE_DATABASE, DEFAULT_DATABASE_NAME)));
      } // if

      influxDB.setDatabase(dbname);
      influxDB.enableBatch(BatchOptions.DEFAULTS.flushDuration(flushDuration));
      influxDB.enableGzip();
    } // if

    return influxDB;
  }
}