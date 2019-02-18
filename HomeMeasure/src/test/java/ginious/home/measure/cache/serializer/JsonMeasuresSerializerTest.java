package ginious.home.measure.cache.serializer;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import ginious.home.measure.Measure;
import ginious.home.measure.MeasureFactory;
import ginious.home.measure.cache.MeasureCache;
import ginious.home.measure.cache.MeasureCacheHelper;
import ginious.home.measure.device.AbstractMeasurementDevice;
import ginious.home.measure.device.MeasurementDevice;

public class JsonMeasuresSerializerTest {

	class TestDevice extends AbstractMeasurementDevice {
		protected TestDevice(String inId) {
			super(inId);
		}

		@Override
		public void switchOnCustom() {
		}

		@Override
		public void initDevice() {
		}

		@Override
		protected void switchOffCustom() {
			
		}
	}

	MeasuresSerializer classUnderTest = new JsonMeasuresSerializer();

	@Test
	public void testSerialize() {

		MeasurementDevice lDevice1 = new TestDevice("junit-1");
		MeasurementDevice lDevice2 = new TestDevice("junit-2");
		MeasureCache lCache = MeasureCacheHelper.createCache(Arrays.asList(lDevice1, lDevice2), new ArrayList<>());

		Measure lMeasure = MeasureFactory.createMeasure(lDevice1, "test-1");
		lMeasure.setValue("1");

		lMeasure = MeasureFactory.createMeasure(lDevice2, "test-1");
		lMeasure.setValue("1");

		lCache.getMeasures("junit-1").add(lMeasure);
	}
}