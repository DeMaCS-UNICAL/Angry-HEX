/*******************************************************************************
 * Angry-HEX - an artificial player for Angry Birds based on declarative knowledge bases
 * Copyright (C) 2012-2015 Francesco Calimeri, Michael Fink, Stefano Germano, Andreas Humenberger, Giovambattista Ianni, Christoph Redl, Daria Stepanova, Andrea Tucci, Anton Wimmer.
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

import ab.vision.BirdType;
 
public class Configuration
{
	private static Properties prop = null;

	private static void initialize()
	{

		//if (prop == null) {
		try {
			prop = new Properties();
			prop.load(new FileInputStream("config.properties"));
		}
		catch(IOException e)
		{
			System.err.println("Could not open configuration file.");
			System.err.println(e);

			System.err.println("Falling back to defaults.");

			prop = null;
		}
		//}
	}

	public static double getSpeedRatio()
	{
		initialize();
		if (prop == null) return 0.0;

        return Double.parseDouble(prop.getProperty("speedRatio"));
		
	}
	public static double getSpeedRatioByBird(BirdType b)
	{
		initialize();
		if (prop == null) return 0.0;

        return Double.parseDouble(prop.getProperty("speedRatio."+b.name()));
		
	}
	public static boolean getUseDlv()
	{
		initialize();

		if (prop == null) return true;

                return !prop.getProperty("usedlv").equals("false") && !prop.getProperty("usedlv").equals("no");
	}

	public static String getOpencvNativePath()
	{
		initialize();

		if (prop == null) return null;

                return prop.getProperty("opencvnativepath").equals("USELDPATH") ? null : prop.getProperty("opencvnativepath");
	}

	public static String getHexpath()
	{
		initialize();

		if (prop == null) return "dlvhex2";

                return prop.getProperty("hexpath");
	}

	public static String getHexAdditionalArguments()
	{
		initialize();

		if (prop == null) return ""; 

                return prop.getProperty("hexaditionalarguments");
	}

	public static boolean isCalibrationMode() {
		initialize();
		String b = prop.getProperty("calibrationmode"); 
		boolean retVal =  !b.equals("false") && !b.equals("no") && !b.equals("0");
//		System.out.println("Calibration mode:"+b+"->"+retVal);
		return retVal;
	}
	
	public static String getReasoningFilename()
	{
        initialize();
		return prop.getProperty("reasoningfilename");

	}
	
	public static String getReasoningWhiteFilename()
	{
        initialize();
		return prop.getProperty("reasoningWhitefilename");

	}

	public static double getTargetYOffset() {
		initialize();
        return Double.parseDouble(prop.getProperty("targetyoffset"));
	}
	
	public static int getTeamID() {
		initialize();
		if (prop == null) return 0;
		return Integer.parseInt(prop.getProperty("teamID"));
	}
	
	public static boolean getTournamentMode() {
		initialize();
		if (prop == null) return false;
		String b = prop.getProperty("tournamentmode"); 
		boolean retVal =  !b.equals("false") && !b.equals("no") && !b.equals("0");
		return retVal;
	}

	public static int getFastshootThreshold() {
		initialize();
		if (prop == null) return 0;
		return Integer.parseInt(prop.getProperty("fastshootthreshold"));
	}

	public static boolean isDebugMode() {
		initialize();
		String b = prop.getProperty("debug"); 
		boolean retVal =  !b.equals("false") && !b.equals("no") && !b.equals("0");
		return retVal;
	}

	public static int getShotMagnitude() {
		initialize();
		if (prop == null) return 0;
		return Integer.parseInt(prop.getProperty("shotmagnitude"));
	}
	
}
