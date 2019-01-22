package ginious.home.measure.cache;

import ginious.home.measure.Measure;

/**
 * Listener for changed measures.
 */
public interface MeasureListener {

	/**
	 * Called when a measure changed.
	 * 
	 * @param inChangedMeasure The measure that has changed.
	 */
	void measureChanged(Measure inChangedMeasure);
}