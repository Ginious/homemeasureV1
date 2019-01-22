package ginious.home.measure.device.volkszaehler;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.OctetString;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlList;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.SmlMessageBody;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.transport.SerialReceiver;

public class DeviceTest {

	private TestVolkszaehlerMeasurementDevice deviceUnderTest;
	private SerialReceiver receiverMock;

	@Before
	public void setup() {

		deviceUnderTest = new TestVolkszaehlerMeasurementDevice();

		receiverMock = Mockito.mock(SerialReceiver.class);
		deviceUnderTest.setReceiver(receiverMock);
	}

	@Test
	public void testMe() {

		try {
			Mockito.when(receiverMock.getSMLFile()).thenReturn(createSmlFile());
			deviceUnderTest.switchOn();
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} // ctach
	}

	private SmlFile createSmlFile() {

		SmlMessage lMessage = Mockito.mock(SmlMessage.class);
		SmlMessageBody lBody = Mockito.mock(SmlMessageBody.class);
		Mockito.when(lMessage.getMessageBody()).thenReturn(lBody);

		Mockito.when(lBody.getTag()).thenReturn(EMessageBody.GET_LIST_RESPONSE);
		SmlGetListRes lList = Mockito.mock(SmlGetListRes.class);
		Mockito.when(lBody.getChoice()).thenReturn(lList);

		SmlList lValList = Mockito.mock(SmlList.class);
		Mockito.when(lList.getValList()).thenReturn(lValList);

		SmlListEntry lEntry1 = Mockito.mock(SmlListEntry.class);
		Mockito.when(lEntry1.getObjName()).thenReturn(new OctetString("TEST"));

		SmlListEntry[] lListEntry = new SmlListEntry[] { lEntry1 };

		Mockito.when(lValList.getValListEntry()).thenReturn(lListEntry);

		return new SmlFile(lMessage);
	}
}
