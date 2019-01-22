package ginious.home.measure.device.volkszaehler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.FlowControl;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlList;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.transport.SerialReceiver;

import ginious.home.measure.device.AbstractMeasurementDevice;
import ginious.home.measure.util.LogHelper;

/**
 * Measure device witch is capable of gathering measure data from a volkszaehler
 * USB device.
 */
public final class VolkszaehlerMeasurementDevice extends AbstractMeasurementDevice {

	private enum Measure {

		TOTAL("Total_Wh"), //
		NT("NT_Wh"), //
		HT("HT_Wh"), //
		SOLAR_OUT("Solar_Out_Wh"), //
		ACTUAL("Actual_Wh");

		private String id;

		private Measure(String inId) {
			id = inId;
		}
	}

	/**
	 * Parameters that can optionally be passed when calling the server including
	 * default values when not provided.
	 */
	private enum Setting {

		METER_TOTAL("01 00 01 08 00 FF"), //
		METER_NT("01 00 01 08 01 FF"), //
		METER_HT("01 00 01 08 02 FF"), //
		METER_SOLAR("01 00 02 08 00 FF"), //
		METER_ACTUAL("01 00 10 07 00 FF"), //

		COSTPERKWH_NT("0.15"), //
		COSTPERKWH_HT("0.24"), //
		SALESPERKWH_SOLAR("0.11"), //
		INTERVAL_START("01.01.2018"), //
		BAUDRATE("9600"), //
		DEVICE("/dev/ttyUSB0");

		private String defaultValue;

		private Setting(String inDefaultValue) {
			defaultValue = inDefaultValue;
		}
	}

	/**
	 * The serial receiver for receiving data from the USB device.
	 */
	private SerialReceiver receiver;

	/**
	 * Default constructor.
	 */
	public VolkszaehlerMeasurementDevice() {
		super("volkszaehler");
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

	@Override
	protected void switchOffCustom() {

	}

	@Override
	protected void switchOnCustom() {

		Map<String, Measure> lMeasuresByObjName = new HashMap<>();
		lMeasuresByObjName.put(getSettingAsText(Setting.METER_HT.name(), Setting.METER_HT.defaultValue), Measure.HT);
		lMeasuresByObjName.put(getSettingAsText(Setting.METER_NT.name(), Setting.METER_NT.defaultValue), Measure.NT);
		lMeasuresByObjName.put(getSettingAsText(Setting.METER_SOLAR.name(), Setting.METER_SOLAR.defaultValue),
				Measure.SOLAR_OUT);
		lMeasuresByObjName.put(getSettingAsText(Setting.METER_TOTAL.name(), Setting.METER_TOTAL.defaultValue),
				Measure.TOTAL);
		lMeasuresByObjName.put(getSettingAsText(Setting.METER_ACTUAL.name(), Setting.METER_ACTUAL.defaultValue),
				Measure.ACTUAL);

		for (;;) {

			SerialReceiver lReceiver = getReceiver();

			// quit when device was switched off
			if (wasSwitchedOff()) {
				try {
					lReceiver.close();
				} catch (IOException e) {
					// ignore
				} // catch
				break;
			} // if

			// read next data bucket
			SmlFile lSmlFile = null;
			try {
				if (lReceiver != null) {
					lSmlFile = lReceiver.getSMLFile();
				} // if
			} catch (IOException e) {
				continue;
			} // catch

			if (lSmlFile != null) {

				List<SmlMessage> lMessages = lSmlFile.getMessages();
				for (SmlMessage lCurrMessage : lMessages) {

					if (lCurrMessage.getMessageBody().getTag() == EMessageBody.GET_LIST_RESPONSE) {
						SmlGetListRes lResponse = (SmlGetListRes) lCurrMessage.getMessageBody().getChoice();
						SmlList lValueList = lResponse.getValList();
						SmlListEntry[] lListEntries = lValueList.getValListEntry();
						for (SmlListEntry lCurrEntry : lListEntries) {

							Measure lMeasure = lMeasuresByObjName.get(lCurrEntry.getObjName().toString());
							if (lMeasure != null) {
								setMeasureValue(lMeasure.id, lCurrEntry.getValue().toString());
							} // if
						} // for
					} // if
				} // for
			} // if

			sleep(1000);
		} // for
	}

	private SerialReceiver getReceiver() {

		if (receiver == null) {

			try {
				SerialPort lPort = SerialPortBuilder
						.newBuilder(getSettingAsText(Setting.DEVICE.name(), Setting.DEVICE.defaultValue))//
						.setBaudRate(getSettingAsInteger(Setting.BAUDRATE.name(), Setting.BAUDRATE.defaultValue))//
						.setDataBits(DataBits.DATABITS_8)//
						.setParity(Parity.NONE)//
						.setStopBits(StopBits.STOPBITS_1)//
						.setFlowControl(FlowControl.RTS_CTS).build();

				receiver = new SerialReceiver(lPort);
			} catch (UnsatisfiedLinkError e) {
				LogHelper.logError(getId(), "Failed loading device driver - reason: {0}", e.getMessage());
			} catch (Throwable t) {
				LogHelper.logError(getId(),
						"Failed to initiate communication with volkszaehler USB dongle - reason: {0}", t.getMessage());
			} // catch
		} // if

		return receiver;
	}

	/**
	 * Setter for test purpose.
	 * 
	 * @param inReceiver The test receiver mock.
	 */
	protected void setReceiver(SerialReceiver inReceiver) {
		receiver = inReceiver;
	}
}