package ginious.home.measure.server.service;

import java.util.Properties;

import ginious.home.measure.cache.MeasureCache;
import ginious.home.measure.cache.serializer.MeasuresSerializer;
import ginious.home.measure.cache.serializer.MeasuresSerializerFactory;
import ginious.home.measure.util.ConfigHelper;
import io.javalin.Javalin;

/**
 * Service publishing measure changes using a Javalin based Web Service.
 */
public class WebService implements Service {

	private static final String WEB_PARAM_SERIALIZER_ID = "serializer";

	private static final String CONFIG_PARAM_PORT = "PORT";
	private static final String CONFIG_PARAM_PATH = "PATH";

	private MeasureCache mCache;

	/**
	 * The port on which the web service will be accessible.
	 */
	private int port;

	/**
	 * The path used to access this web service (optional). Default is "/" resulting
	 * in something like http://mydomain.com:7000/
	 */
	private String path;

	/**
	 * Default constructor.
	 * 
	 * @param inMCache       The cache about to be published by this web service.
	 * @param inServiceProps The properties for this service.
	 */
	public WebService(MeasureCache inMCache, Properties inServiceProps) {
		super();

		mCache = inMCache;

		port = ConfigHelper.getNumberProperty(inServiceProps, CONFIG_PARAM_PORT, 7000);
		path = ConfigHelper.getTextProperty(inServiceProps, CONFIG_PARAM_PATH, "/");
	}

	/**
	 * Starts the web service.
	 */
	public void startService() {

		Javalin lServiceBuilder = Javalin.create();
		lServiceBuilder.enableStandardRequestLogging();
		Javalin app = Javalin.start(port);

		app.get(path, ctx -> {
			String lSerializerId = ctx.request().getParameter(WEB_PARAM_SERIALIZER_ID);
			MeasuresSerializer lSerializer = MeasuresSerializerFactory.getSerializer(lSerializerId);
			ctx.result(lSerializer.serialize(mCache));
		});
	}

	@Override
	public void stopService() {

		// TODO implement stop
	}
}