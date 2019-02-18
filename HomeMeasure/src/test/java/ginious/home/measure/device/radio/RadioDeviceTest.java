package ginious.home.measure.device.radio;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ginious.home.measure.device.radio.RadioItem;

public class RadioDeviceTest {

	@Test
	public void testCreate() {
		
		RadioItem lDevice = new RadioItem("JUNIT", "a:1,b:2,c:3", "x,y,z");
		assertEquals(3, lDevice.getIds().size());
		assertEquals(3, lDevice.getMeasureIds().size());
		assertEquals("JUNIT", lDevice.getName());
	}
}
