package scc.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AdvanceFeatures {

	/**
	 * Returns true if the property = true
	 * 
	 * @param property
	 * @return true or false
	 */
	public static boolean getProperty(String property) {
		boolean value = false;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("AdvFeatures"));
			for (int i = 0; i < 4; i++) {
				String[] line = reader.readLine().split("=");
				if (line[0].toUpperCase().equals(property.toUpperCase()))
					value = Boolean.parseBoolean(line[1]);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
}
