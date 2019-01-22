package ginious.home.measure.device.sma;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.re.easymodbus.modbusclient.ModbusClient;
import ginious.home.measure.device.MeasurementDevice;

public class DeviceTest {

	private TestSmaConverterMeasurementDevice deviceUnderTest;
	private ModbusClient modbusClientMock;

	@Before
	public void setup() {

		deviceUnderTest = new TestSmaConverterMeasurementDevice();

		modbusClientMock = Mockito.mock(ModbusClient.class);
		deviceUnderTest.setModbusClient(modbusClientMock);
	}

	@Test
	public void testMe() {

		deviceUnderTest.switchOn();
	}
}
