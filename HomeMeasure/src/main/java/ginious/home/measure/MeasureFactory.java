package ginious.home.measure;

public class MeasureFactory {

	private MeasureFactory() {
		super();
	}

	public static Measure createMeasure(String inId) {

		return new MeasureImpl(inId);
	}
}
