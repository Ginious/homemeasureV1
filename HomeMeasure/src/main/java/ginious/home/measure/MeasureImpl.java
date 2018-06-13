package ginious.home.measure;

class MeasureImpl implements Measure {

	private String id;
	private String value;

	MeasureImpl(String inId) {
		super();

		id = inId;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String inValue) {
		value = inValue;
	}
}