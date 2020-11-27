package scc.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class AzureProperties {
	public static final String BLOB_KEY = "DefaultEndpointsProtocol=https;AccountName=sc42764;AccountKey=A/ACsdA6CYx4UrS0D2Z329z9AykGT2MJicbcxqPbH/fTnEXKVKxJLhE4csZyfsWKHZpLG4cchjspctwEMBq+oA==;EndpointSuffix=core.windows.net";
	public static final String COSMOSDB_KEY = "7Vp8kAIdWCueWcMkRNuBUcMoNu93VXj3HMEbdo0aT1TwXnstcUiOdYvunokuZ1P3cMJpxKQ4zAYmYbcd4NdKHw==";
	public static final String COSMOSDB_URL = "https://scc-group-db.documents.azure.com:443/";
	public static final String COSMOSDB_DATABASE = "sc42764Db";
	public static final String REDIS_URL = "scc-group-cache.redis.cache.windows.net";
	public static final String REDIS_KEY = "Nl31CTkaOIK3SsXUnbQzcWJwUM3jihjHvYwRZ2jYv2Y=";

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
