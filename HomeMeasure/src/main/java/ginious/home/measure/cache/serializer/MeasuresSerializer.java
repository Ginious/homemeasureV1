package ginious.home.measure.cache.serializer;

import ginious.home.measure.cache.MeasureCache;

public interface MeasuresSerializer {

	String serialize(MeasureCache inCache);
	
	String getId();
}
