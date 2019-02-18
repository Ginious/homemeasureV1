package ginious.home.measure.device.radio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ginious.home.measure.util.LogHelper;

/**
 * A kind of stub used for starting the native rtl_433 process including
 * gathering of console output.
 */
class RTL433Stub {

	/**
	 * Available options to start rtl_433.
	 */
	enum Option {
	ALL_DEVICES("-G", false), //
	DEVICE_PROTOCOL("-R ", true), //
	FREQUENCY("-f ", true), //
	SAMPLERATE("-s ", true), //
	OUTPUT_JSON("-F json", false);

		private String optionText;
		private boolean optionTextRequired;

		Option(String inOptionText, boolean inRequiresOptionText) {
			optionText = inOptionText;
			optionTextRequired = inRequiresOptionText;
		}
	}

	/**
	 * The name of the executable program.
	 */
	private static final String EXECUTABLE = "rtl_433";

	/**
	 * The options for starting the process.
	 */
	private List<String> options = new ArrayList<>();

	/**
	 * The reader for accessing console output.
	 */
	private BufferedReader reader;

	/**
	 * Default constructor.
	 */
	RTL433Stub() {
		super();

		options.add(EXECUTABLE);
	}

	/**
	 * Adds an option used for starting the underlying binary.
	 * 
	 * @param inOption     The option to add.
	 * @param inOptionText The additional option text or <code>null</code> if not
	 *                     required.
	 */
	void addOption(Option inOption, String inOptionText) {

		Validate.isTrue(inOption != null, "Parameter inOption is required!");
		if (inOption.optionTextRequired) {
			Validate.isTrue(StringUtils.isNotBlank(inOptionText),
					"Parameter inOptionText is required for Option [" + inOption.name() + "]!");
		} // if

		if (StringUtils.isBlank(inOptionText)) {
			options.add(inOption.optionText);
		} else {
			options.add(inOption.optionText + inOptionText);
		} // else
	}

	/**
	 * Starts the process with the defined options.
	 */
	void startProcess() {

		Process lProcess = null;

		ProcessBuilder lBuilder = new ProcessBuilder(options);
		try {
			lProcess = lBuilder.start();
		} catch (IOException e) {
			throw new RuntimeException("Failed to start process [" + EXECUTABLE + "] - reason " + e.getMessage(), e);
		} // catch

		reader = new BufferedReader(new InputStreamReader(lProcess.getInputStream()));
	}

	/**
	 * Gathers the console output line by line.
	 * 
	 * @return The console output or <code>null</code> when output finished.
	 */
	String getOutput() {

		String outLine;

		try {
			outLine = reader.readLine();
		} catch (IOException e) {
			LogHelper.logError(this, e, "Failed to read output of process [{0}]!", options.iterator().next());
			outLine = null;
		} // catch

		return outLine;
	}
}