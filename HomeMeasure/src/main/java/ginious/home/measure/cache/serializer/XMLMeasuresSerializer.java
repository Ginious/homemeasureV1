package ginious.home.measure.cache.serializer;

import java.util.Collection;

import ginious.home.measure.Measure;
import ginious.home.measure.cache.MeasureCache;

/**
 * Serializer that generates measures as XML.
 */
final class XMLMeasuresSerializer extends AbstractMeasuresSerializer {

	/**
	 * Standard constructor.
	 */
	public XMLMeasuresSerializer() {
		super("xml");
	}

	@Override
	public String serialize(MeasureCache inCache) {

		StringBuilder lBuilder = new StringBuilder("<devices>\n");
		for (String lCurrDeviceId : inCache.getDeviceIds()) {

			lBuilder.append("\t<device id=\"");
			lBuilder.append(lCurrDeviceId);
			lBuilder.append("\">\n");
			lBuilder.append(serializeMeasures(inCache.getMeasures(lCurrDeviceId)));
			lBuilder.append("\t</device>\n");
		} // for
		lBuilder.append("</devices>");

		return lBuilder.toString();
	}

	/**
	 * Serializes the given collection of measures.
	 * 
	 * @param inMeasures
	 *            The measures to serialize.
	 * @return The resulting XML.
	 */
	private String serializeMeasures(Collection<Measure> inMeasures) {

		StringBuilder lBuilder = new StringBuilder("\t\t<measures>\n");
		for (Measure lCurrMeasure : inMeasures) {

			lBuilder.append("\t\t\t<measure>\n");
			lBuilder.append("\t\t\t\t<id>");
			lBuilder.append(lCurrMeasure.getId());
			lBuilder.append("</id>\n");
			lBuilder.append("\t\t\t\t<value>");
			lBuilder.append(lCurrMeasure.getValue());
			lBuilder.append("</value>\n");
			lBuilder.append("\t\t\t</measure>\n");
		} // for
		lBuilder.append("\t\t<measures>\n");

		return lBuilder.toString();
	}
}