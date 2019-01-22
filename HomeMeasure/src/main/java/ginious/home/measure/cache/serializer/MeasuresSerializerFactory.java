package ginious.home.measure.cache.serializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;

import ginious.home.measure.util.LogHelper;

/**
 * Factory for the creation of serializers.
 */
public final class MeasuresSerializerFactory {

	private static final Map<String, MeasuresSerializer> serializersRegistry = new HashMap<>();

	private static final String defaultSerializerId;

	static {
		JsonMeasuresSerializer lDefaultSerializer = new JsonMeasuresSerializer();
		defaultSerializerId = lDefaultSerializer.getId();
		registerSerializer(lDefaultSerializer);
		registerSerializer(new XMLMeasuresSerializer());
	}

	/**
	 * Gets a serializer for the given id.
	 * 
	 * @param inId The id of the serializer.
	 * @return The serializer.
	 * @throws IllegalArgumentException when Serializer does not exist.
	 */
	public static MeasuresSerializer getSerializer(String inId) {

		MeasuresSerializer outSerializer = null;

		if (inId != null) {

			outSerializer = serializersRegistry.get(inId);
			Validate.isTrue(outSerializer != null, "Serializer [" + inId + "] not found - try one of "
					+ serializersRegistry.keySet() + " or provide and configure a custom implementation!");
		} else {
			outSerializer = serializersRegistry.get(defaultSerializerId);
		} // else

		return outSerializer;
	}

	/**
	 * Initializes custom serializers that are provided by fully qualified class
	 * name.
	 * 
	 * @param lClassNames The names of serializer class implementations.
	 * @throws RuntimeException when initialization fails e.g. due to a class that
	 *                          can not be found.
	 */
	@SuppressWarnings("unchecked")
	public static void init(String... lClassNames) {

		for (String lCurrClassName : lClassNames) {

			try {
				Class<? extends MeasuresSerializer> lClass = (Class<? extends MeasuresSerializer>) Class
						.forName(lCurrClassName);
				MeasuresSerializer lSerializer = lClass.newInstance();
				serializersRegistry.put(lSerializer.getId(), lSerializer);

				LogHelper.logInfo(MeasuresSerializerFactory.class,
						"Created and registered serializer with id [{0}] from class [{1}]", lSerializer.getId(),
						lClass.getName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Serializer [" + lCurrClassName + "] could not be loaded!", e);
			} // catch
		} // for
	}

	/**
	 * Hidden factory constructor.
	 */
	private MeasuresSerializerFactory() {
		super();
	}

	/**
	 * Registers the given serializer.
	 * 
	 * @param xmlMeasuresSerializer
	 */
	private static void registerSerializer(MeasuresSerializer inSerializer) {

		serializersRegistry.put(inSerializer.getId(), inSerializer);
	}
}
