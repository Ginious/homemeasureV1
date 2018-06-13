package ginious.home.measure.cache.serializer;

/**
 * Base class for serializers.
 */
public abstract class AbstractMeasuresSerializer implements MeasuresSerializer {

	/**
	 * The id of the serializer.
	 */
	private final String id;

	/**
	 * Default constructor.
	 * 
	 * @param inId
	 *            The Id of the serializer.
	 */
	protected AbstractMeasuresSerializer(String inId) {
		super();

		id = inId;
	}

	@Override
	public final String getId() {
		return id;
	}
}