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

	private static String getMemFilename(final int level) {
		return String.format("%s%slevel%d.mem", Memory.MEMORY_DIR, File.separator, level);
	}

	public static void init() {
		try {
			// it's not needed because we delete the directory in the
			// StrategyManager
			// if (DebugUtils.DEBUG) {
			// Utils.deleteDir(MEMORY_DIR);
			// }
			if (!new File(Memory.MEMORY_DIR).exists())
				Utils.createDir(Memory.MEMORY_DIR);
		} catch (final IOException e) {
			Memory.Log.severe("cannot init memory");
		} catch (final InterruptedException e) {
			Memory.Log.severe("cannot init memory");
		}
	}

	public static List<Point.Double> load(final int level) {
		final List<Point.Double> points = new LinkedList<>();

		final File file = new File(Memory.getMemFilename(level));
		if (!file.exists()) {
			Memory.Log.info("memory file does not exist: " + file.getName());
			return points;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				final String[] coord = line.split(Memory.DELIMITER);
				final double x = Double.parseDouble(coord[0]);
				final double y = Double.parseDouble(coord[1]);
				points.add(new Point.Double(x, y));
			}
			reader.close();
		} catch (final FileNotFoundException e) {
			Memory.Log.warning("could not read memory: " + e.getMessage());
		} catch (final IOException e) {
			Memory.Log.warning("could not read memory: " + e.getMessage());
		}
		return points;
	}

	public static void store(final int level, final Point p) {
		try {
			final PrintWriter printer = new PrintWriter(new FileWriter(Memory.getMemFilename(level), true));
			printer.println(p.x + Memory.DELIMITER + p.y);
			printer.close();
		} catch (final IOException e) {
			Memory.Log.warning("could not store point for level " + level + " to file " + Memory.getMemFilename(level));
		}
	}
}
