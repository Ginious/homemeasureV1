package ginious.home.measure.device.sma;

import java.io.IOException;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import de.re.easymodbus.modbusclient.ModbusClient.RegisterOrder;
import ginious.home.measure.device.AbstractMeasurementDevice;
import ginious.home.measure.util.LogHelper;

public final class SmaConverterMeasurementDevice extends AbstractMeasurementDevice {

	/**
	 * Modbus data types.
	 */
	public enum DataType {
	S32, //
	U32, //
	U64;
	}

	/**
	 * Existing measures including addressing information and refresh interval in
	 * milliseconds.
	 */
	public enum Measure {

		CURRENT_W("Current_W", 30775, 2, DataType.S32, 10_000), // 10 Sekunden
		DAY_WH("Day_Wh", 30535, 2, DataType.U32, 60_000), // 1 Minute
		DAY_SALE("Day_Sale"), //
		TOTAL_KWH("Total_MWh", 30531, 4, DataType.U32, 300_000), // 5 Minuten
		TOTAL_SALE("Total_Sale");

		public String id;
		private int start;
		private int length;
		private DataType type;
		private int refreshAfter;

		private Measure(String inId) {
			id = inId;
		}

		private Measure(String inId, int inStart, int inLength, DataType inType, int inRefreshAfter) {
			id = inId;
			start = inStart;
			length = inLength;
			type = inType;
			refreshAfter = inRefreshAfter;
		}
	}

	/**
	 * Parameters that can optionally be passed when calling the server including
	 * default values when not provided.
	 */
	private enum Setting {

		ID("3"), //
		IP("192.168.2.51"), //
		PORT("502"), //
		TIMEOUT("60000"), //
		SALESPERKWH("0.11");

		private String defaultValue;

		private Setting(String inDefaultValue) {
			defaultValue = inDefaultValue;
		}

		private String getDefaultValue() {
			return defaultValue;
		}
	}

	private int REQUEST_INTERVAL_MS = 10_000; // 10 seconds

	private int INTERVAL_DAY_WH = 6; // 1 minute

	private int INTERVAL_TOTAL_KWH = 30; // 5 minutes

	private ModbusClient testClient;

	public SmaConverterMeasurementDevice() {
		super("sma_converter");
	}

	/**
	 * Connects to the SMA converter.
	 * 
	 * @return The client used to communicate with the SMA converter.
	 */
	private ModbusClient connect() {

		ModbusClient outClient = createModbusClient();

		String lIpAddress = getSettingAsText(Setting.IP.name(), Setting.IP.defaultValue);
		int lPort = getSettingAsInteger(Setting.PORT.name(), Setting.PORT.defaultValue);

		try {
			outClient.Connect(lIpAddress, lPort);
		} catch (Throwable t) {
			LogHelper.logError(this, "Modbus connection to [{0}:{1}] failed - reason: {2}", lIpAddress, lPort,
					t.getMessage());
			return null;
		} // catch

		outClient.setConnectionTimeout(getSettingAsInteger(Setting.TIMEOUT.name(), Setting.TIMEOUT.defaultValue));
		int lModbusID = getSettingAsInteger(Setting.ID.name(), Setting.ID.defaultValue);
		outClient.setUnitIdentifier((byte) Byte.valueOf((byte) lModbusID));

		if (outClient.isConnected()) {
			LogHelper.logInfo(this, "SMA converter [ID={0}] successfully connected: ip={1}, port={2}", lModbusID,
					lIpAddress, lPort);
		} else {
			LogHelper.logWarning(this,
					"SMA converter [ID={0}] could not be connected: ip={1}, port={2} - unknown reason", lModbusID,
					lIpAddress, lPort);
			outClient = null;
		} // else

		return outClient;
	}

	/**
	 * Creates a new Modbus Client or returns the mocked test client.
	 * 
	 * @return A new Modbus Client or the mocked test client.
	 */
	private ModbusClient createModbusClient() {

		ModbusClient outClient;

		if (testClient == null) {
			outClient = new ModbusClient();
		} else {
			outClient = testClient;
		} // else

		return outClient;
	}

	private String getValueFromSMA(Measure inValueId) {

		String outValue = "0";

		ModbusClient lClient = null;
		int[] lRegisters = null;
		try {
			lClient = connect();
			if (lClient != null) {
				lRegisters = lClient.ReadHoldingRegisters(inValueId.start, inValueId.length);
			}
		} catch (ModbusException | IOException e) {
			return outValue;
		} finally {
			if (lClient != null) {
				try {
					lClient.Disconnect();
				} catch (IOException e) {
					// ignore
				} // catch
			} // if
		} // finally

		if (lRegisters != null) {

			// decode register bytes
			if (inValueId.type == DataType.S32) {
				outValue = String.valueOf(ModbusClient.ConvertRegistersToDouble(lRegisters, RegisterOrder.HighLow));
			} else if (inValueId.type == DataType.U32) {
				outValue = String.valueOf(ModbusClient.ConvertRegistersToDouble(lRegisters, RegisterOrder.HighLow));
			} else if (inValueId.type == DataType.U64) {
				outValue = String.valueOf(ModbusClient.ConvertRegistersToLong(lRegisters, RegisterOrder.HighLow));
			} // else if
		} // if

		return outValue;
	}

	protected void initDevice() {

		// register all mesaures provided by SMA converter
		for (Measure lCurrMeasure : Measure.values()) {
			registerMeasure(lCurrMeasure.id);
		} // for

		// define default settings
		for (Setting lCurrSetting : Setting.values()) {
			setSetting(lCurrSetting.name(), lCurrSetting.defaultValue);
		} // for
	}

	/**
	 * Setter for test purpose.
	 * 
	 * @param inMockClient The modbus client mock for testing.
	 */
	protected void setModbusClient(ModbusClient inMockClient) {
		testClient = inMockClient;
	}

	@Override
	protected void switchOffCustom() {

	}

	@Override
	protected void switchOnCustom() {

		// determine sales per KWh
		Float lSalesPerKWh = Float
				.valueOf(getSettingAsText(Setting.SALESPERKWH.name(), Setting.SALESPERKWH.defaultValue));

		// use lDeviceJustStarted && lIntervalCounter to read
		// values that change less frequently
		boolean lDeviceJustStarted = true;
		int lIntervalCounter = 0;
		for (;;) {

			// quit when device was switched off
			if (wasSwitchedOff()) {
				break;
			} // if

			if (lDeviceJustStarted || lIntervalCounter == INTERVAL_DAY_WH) {

				// Day Wh
				Integer lDayWh = Integer.valueOf(getValueFromSMA(Measure.DAY_WH));
				setMeasureValue(Measure.DAY_WH.id, String.valueOf(lDayWh));

				// Daily Sales
				Float lSalesPerDay = (float) lDayWh / 1000 * lSalesPerKWh;
				setMeasureValue(Measure.DAY_SALE.id, String.valueOf(lSalesPerDay));
			} // if

			if (lDeviceJustStarted || lIntervalCounter == INTERVAL_TOTAL_KWH) {

				// Total MWh
				Integer lTotalKWh = Integer.valueOf(getValueFromSMA(Measure.TOTAL_KWH));
				setMeasureValue(Measure.TOTAL_KWH.id, String.valueOf((float) lTotalKWh / 1000));

				// Total Sales
				Float lSalesTotal = (float) lTotalKWh * lSalesPerKWh;
				setMeasureValue(Measure.TOTAL_SALE.id, String.valueOf(lSalesTotal));

				lIntervalCounter = 0;
			} // if

			// Current Wh
			Integer lCurrentW = Integer.valueOf(getValueFromSMA(Measure.CURRENT_W));
			if (lCurrentW < 0) {
				lCurrentW = 0;
			} // if
			setMeasureValue(Measure.CURRENT_W.id, String.valueOf(lCurrentW));

			sleep(REQUEST_INTERVAL_MS);

			lIntervalCounter++;
			lDeviceJustStarted = false;
		} // for
	}
}