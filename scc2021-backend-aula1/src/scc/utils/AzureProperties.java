package scc.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class AzureProperties {
	public static final String BLOB_KEY = "BLOB_KEY";
	public static final String COSMOSDB_KEY = "COSMOSDB_KEY";
	public static final String COSMOSDB_URL = "COSMOSDB_URL";
	public static final String COSMOSDB_DATABASE = "COSMOSDB_DATABASE";
	public static final String REDIS_URL = "REDIS_URL";
	public static final String REDIS_KEY = "REDIS_KEY";

	public static final String PROPS_FILE = "azurekeys-westeurope.props";
	private static Properties props;

	public static synchronized Properties getProperties() {
		if (props == null) {
			props = new Properties();
			try {
				props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPS_FILE));
			} catch (Exception e) {
				// do nothing
			}
			try {
				props.load(new FileInputStream(PROPS_FILE));
			} catch (Exception e) {
				// do nothing
			}
		}
		return props;
	}

	public static String getProperty(String key) {
		try {
			String val = System.getenv(key);
			if (val != null)
				return val;
		} catch (Exception e) {
			// do nothing
		}
		return getProperties().getProperty(key);
	}
}
