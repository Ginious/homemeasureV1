package ginious.home.measure.device.radio;

import org.junit.Test;
import org.mockito.Mockito;

import ginious.home.measure.cache.MeasureCacheAdapter;

public class RadioReceiverDeviceTest {

	private RadioReceiverDevice deviceUnderTest = new RadioReceiverDevice("radio");

	@Test
	public void testDevice() {

		String[] lOutput = { "trying device  0:  Realtek, RTL2838UHIDIR, SN: 00000001", //
				"Detached kernel driver", //
				"Found Rafael Micro R820T tuner", //
				"Using device 0: Generic RTL2832U OEM", //
				"Exact sample rate is: 250000.000414 Hz", //
				"[R82XX] PLL not locked!", //
				"Sample rate set to 250000 S/s.", //
				"Bit detection level set to 0 (Auto).", //
				"Tuner gain set to Auto.", //
				"Reading samples in async mode...", //
				"Tuned to 433.920MHz.", //
				//
				// 3 data records
				"{\"time\" : \"2019-02-12 18:51:37\", \"model\" : \"Prologue sensor\", \"id\" : 5, \"rid\" : 139, \"channel\" : 3, \"battery\" : \"OK\", \"button\" : 0, \"temperature_C\" : 25.100, \"humidity\" : 25}", //
				"{\"time\" : \"2019-02-12 18:51:59\", \"model\" : \"Solight TE44\", \"id\" : 68, \"channel\" : 1, \"temperature_C\" : 22.100, \"mic\" : \"CRC\"}", //
				"{\"time\" : \"2019-02-12 18:52:11\", \"model\" : \"Prologue sensor\", \"id\" : 5, \"rid\" : 139, \"channel\" : 3, \"battery\" : \"OK\", \"button\" : 0, \"temperature_C\" : 25.100, \"humidity\" : 25}", //
				null // needed otherwise test will block
		};

		// mock binary executable
		RTL433Stub lProcessStubMock = Mockito.mock(RTL433Stub.class);
		Mockito.when(lProcessStubMock.getOutput()).thenReturn("....", lOutput);

		// define single item (normally defined in hmserver.ini)
		deviceUnderTest.setSetting("DEVICE_PROTOCOLS", "68,5");
		deviceUnderTest.setSetting("prologue.IDS", "id:5,channel:3,rid:139");
		deviceUnderTest.setSetting("prologue.MEASURES", "temperature_C");

		// mock measure cache adapter
		MeasureCacheAdapter lCacheMock = Mockito.mock(MeasureCacheAdapter.class);
		deviceUnderTest.setMeasureCacheAdapter(lCacheMock);

		// start device
		deviceUnderTest.setProcessStubForTesting(lProcessStubMock);
		deviceUnderTest.initDevice();
		deviceUnderTest.switchOn();

		// ensure that for two data records the cache was called
		Mockito.verify(lCacheMock, Mockito.times(2)).setValue(Mockito.anyString(), Mockito.anyString());
	}
}
