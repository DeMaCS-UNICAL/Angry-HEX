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

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import angryhexclient.util.Utils;

public class Memory {
	
	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	final public static String MEMORY_DIR = System.getProperty("user.dir") + File.separator + "memory";
	final public static String DELIMITER = ";";
	
	public static void init() {
		try {
			// it's not needed because we delete the directory in the StrategyManager
//			if (DebugUtils.DEBUG) {
//				Utils.deleteDir(MEMORY_DIR);
//			}
			if (!new File(MEMORY_DIR).exists()) {
				Utils.createDir(MEMORY_DIR);
			}
		} catch (IOException e) {
			Log.severe("cannot init memory");
		} catch (InterruptedException e) {
			Log.severe("cannot init memory");
		}
	}
	
	private static String getMemFilename(int level) {
		return String.format("%s%slevel%d.mem", MEMORY_DIR, File.separator, level);
	}
	
	public static List<Point.Double> load(int level) {
		List<Point.Double> points = new LinkedList<Point.Double>();
		
		File file = new File(getMemFilename(level));
		if (!file.exists()) {
			Log.info("memory file does not exist: " + file.getName());
			return points;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] coord = line.split(DELIMITER);
				double x = Double.parseDouble(coord[0]);
				double y = Double.parseDouble(coord[1]);
				points.add(new Point.Double(x, y));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			Log.warning("could not read memory: " + e.getMessage());
		} catch (IOException e) {
			Log.warning("could not read memory: " + e.getMessage());
		}
		return points;
	}
	
	public static void store(int level, Point p) {
		try {
			PrintWriter printer = new PrintWriter(new FileWriter(getMemFilename(level), true));
			printer.println(p.x + DELIMITER + p.y);
			printer.close();
		} catch (IOException e) {
			Log.warning("could not store point for level " + level);
		}
	}
}
