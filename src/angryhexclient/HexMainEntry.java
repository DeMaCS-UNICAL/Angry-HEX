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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import angryhexclient.util.LogFormatter;
import angryhexclient.util.OutConsoleHandler;

public class HexMainEntry {

	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	// the entry of the software
	public static void main(String args[]) {

		// setup logger
		// TODO distinguish debug levels
		OutConsoleHandler ch = new OutConsoleHandler();
		ch.setFormatter(new LogFormatter());
		ch.setLevel(Level.ALL);
		Log.addHandler(ch);
		Log.setLevel(Level.ALL);

		Log.info("starting HexMainEntry main");
		try {

			// Rips off any dlvhex2 processes which were left
			Log.info("killing leftover processes");
			try {
				Runtime.getRuntime().exec("killall -9 dlvhex2");
			} catch (IOException e1) {
				Log.warning("cannot killall dlvhex2");
			}

			// check arguments for ip address and initial level
			byte initialLevel = 1;
			String ipaddress = "127.0.0.1";
			if (args.length > 0) {
				ipaddress = args[0];

				if (args.length > 2 && args[1].equals("-level")) {
					initialLevel = (byte) Integer.parseInt(args[2]);
				}
			}

			// setup agent
			try {
				Log.info("creating HexActionRobot at '"+ipaddress+"'");
				HexActionRobot ar = new HexActionRobot(ipaddress);
				Log.info("creating HexAgent with level "+Integer.toString(initialLevel));
				HexAgent ha = new HexAgent(ar, initialLevel);
				Thread haThread = new Thread(ha);
				haThread.start();
			} catch (Exception e) {
				Log.severe("Cannot init agent: " + e.getMessage());
			}

		} catch (Exception e) {
			Log.severe("General Error: " + e.getMessage());
		}
	}
}
