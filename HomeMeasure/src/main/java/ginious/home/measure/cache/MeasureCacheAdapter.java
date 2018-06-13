package ginious.home.measure.cache;

public interface MeasureCacheAdapter {

	void setValue(String inId, String inValue);

	void addMeasureListener(MeasureListener inListener);
}