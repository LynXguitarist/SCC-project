package scc.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class AdvancedFeatures {

	public static final String REDIS = "REDIS";
	public static final String FUNCTION = "FUNCTION";
	public static final String MONGODB = "MONGODB";

	public static final String PROPS_FILE = "AdvFeatures.props";
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
