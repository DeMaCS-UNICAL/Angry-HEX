/*******************************************************************************
 * Angry-HEX - an artificial player for Angry Birds based on declarative knowledge bases
 * Copyright (C) 2012-2016 Francesco Calimeri, Michael Fink, Stefano Germano, Andreas Humenberger, Giovambattista Ianni, Christoph Redl, Daria Stepanova, Peter Schueller, Andrea Tucci, Anton Wimmer.
 *
 * This file is part of Angry-HEX.
 *
 * Angry-HEX is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Angry-HEX is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package angryhexclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import ab.vision.BirdType;

public class Configuration {
	private static String configfile = "config.properties";

	private static final Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static Properties prop = null;

	public static int getFastshootThreshold() {
		Configuration.initialize();
		if (Configuration.prop == null)
			return 0;
		return Integer.parseInt(Configuration.prop.getProperty("fastshootthreshold"));
	}

	public static String getHexAdditionalArguments() {
		Configuration.initialize();

		if (Configuration.prop == null)
			return "";

		return Configuration.prop.getProperty("hexaditionalarguments");
	}

	public static String getHexpath() {
		Configuration.initialize();

		if (Configuration.prop == null)
			return "dlvhex2";

		return Configuration.prop.getProperty("hexpath");
	}

	public static String getReasoningFilename() {
		Configuration.initialize();
		return Configuration.prop.getProperty("reasoningfilename");

	}

	public static String getReasoningFixedKnowledgeFilename() {
		Configuration.initialize();
		return Configuration.prop.getProperty("reasoningFKfilename");

	}

	public static String getReasoningWhiteFilename() {
		Configuration.initialize();
		return Configuration.prop.getProperty("reasoningWhitefilename");

	}

	public static int getShotMagnitude() {
		Configuration.initialize();
		if (Configuration.prop == null)
			return 0;
		return Integer.parseInt(Configuration.prop.getProperty("shotmagnitude"));
	}

	public static double getSpeedRatio() {
		Configuration.initialize();
		if (Configuration.prop == null)
			return 0.0;

		return Double.parseDouble(Configuration.prop.getProperty("speedRatio"));

	}

	public static double getSpeedRatioByBird(final BirdType b) {
		Configuration.initialize();
		if (Configuration.prop == null)
			return 0.0;

		return Double.parseDouble(Configuration.prop.getProperty("speedRatio." + b.name()));

	}

	public static double getTargetYOffset() {
		Configuration.initialize();
		return Double.parseDouble(Configuration.prop.getProperty("targetyoffset"));
	}

	public static int getTeamID() {
		Configuration.initialize();
		if (Configuration.prop == null)
			return 0;
		return Integer.parseInt(Configuration.prop.getProperty("teamID"));
	}

	public static boolean getTournamentMode() {
		Configuration.initialize();
		if (Configuration.prop == null)
			return false;
		final String b = Configuration.prop.getProperty("tournamentmode");
		final boolean retVal = !b.equals("false") && !b.equals("no") && !b.equals("0");
		return retVal;
	}

	public static boolean getUseDlv() {
		Configuration.initialize();

		if (Configuration.prop == null)
			return true;

		return !Configuration.prop.getProperty("usedlv").equals("false")
				&& !Configuration.prop.getProperty("usedlv").equals("no");
	}

	private static void initialize() {

		if (Configuration.prop == null)
			try {
				Configuration.prop = new Properties();
				Configuration.Log.info("loading configuration from '" + Configuration.configfile + "'");
				Configuration.prop.load(new FileInputStream(Configuration.configfile));
			} catch (final IOException e) {
				Configuration.Log.warning("Could not open configuration file.");
				Configuration.Log.warning(e.toString());
				Configuration.Log.warning("Falling back to defaults.");

				Configuration.prop = null;
			}

	}

	public static boolean isCalibrationMode() {
		Configuration.initialize();
		final String b = Configuration.prop.getProperty("calibrationmode");
		final boolean retVal = !b.equals("false") && !b.equals("no") && !b.equals("0");
		// System.out.println("Calibration mode:"+b+"->"+retVal);
		return retVal;
	}

	public static boolean isDebugMode() {
		Configuration.initialize();
		final String b = Configuration.prop.getProperty("debug");
		final boolean retVal = !b.equals("false") && !b.equals("no") && !b.equals("0");
		return retVal;
	}

}
