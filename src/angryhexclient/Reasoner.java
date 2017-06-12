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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Reasoner {
	
	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	// dlvhex arguments
	protected static String DLVHEX_CMDARGS = join(new String[] {
			Configuration.getHexpath(),
			Configuration.getUseDlv() ? "--solver=dlv" : "",
			Configuration.getHexAdditionalArguments(),
			Configuration.isCalibrationMode() ? "-n=1" : "",
      "-e old", // old is much faster than easy which is much faster than default (greedy)
			"--weak-enable", "--silent",
      Configuration.isDebugMode() ? "--verbose=8": "--verbose=0" });

	// parent dir of dlv files
	protected static String clientDir = System.getProperty("user.dir")
			+ System.getProperty("file.separator");

	protected String filterPred;
	protected Pattern filterRegex;
	protected String encFile;
	protected List<String> facts;
	protected String dlvhexOutput;
	protected int callCount;

	public Reasoner(String filterPred, Pattern filterRegex) {
		this.filterPred = filterPred;
		this.filterRegex = filterRegex;
		this.facts = new ArrayList<String>();
	}

	public void setEncodingFile(String encFile) {
		this.encFile = encFile;
	}

	public void addFact(String fact) {
		facts.add(fact);
	}

	public void reason() throws UnsupportedOperationException, IOException, InterruptedException {

		// create file containing just facts
		String factsFile = clientDir
				+ getFactFilename();
		PrintWriter writer = new PrintWriter(factsFile, "UTF-8");
		for (String string : facts) {
			writer.println(string);
		}
		writer.close();

		// call dlvhex
		String cmd = String.format("%s --filter=%s %s %s", DLVHEX_CMDARGS,
				filterPred, factsFile, encFile);
		System.out.println("Calling dlvhex2: " + cmd);
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		System.out.println("Called dlvhex2");

		// read output of dlvhex and store in dlvhexOutput
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
		BufferedReader hexerr = new BufferedReader(new InputStreamReader(
				process.getErrorStream()));

		dlvhexOutput = "";
		String currentLine;
		String currentErrLine;
		while ((currentLine = bufferedReader.readLine()) != null) {
			dlvhexOutput += currentLine + "\n";
		}
		while ((currentErrLine = hexerr.readLine()) != null) {
			Log.severe(currentErrLine);
		}

		// cleanup
		facts.clear();
		// keep facts file if debug mode
		saveDebugHexWithInfo(factsFile);
		// delete facts file
		new File(factsFile).delete();
		// parse response of dlvhex
		parseOutput();
	}

	protected void parseOutput() {
		clear();
		Matcher m = filterRegex.matcher(dlvhexOutput);
		while (m.find()) {
			storeAtom(m);
		}
	}
	
	protected String getFactFilename() {
		return String.format("program%d.hex", callCount++);
	}
	
	protected abstract void clear();
	
	protected abstract void storeAtom(Matcher m);

	private static String join(String[] arr) {
		String str = "";
		for (String s : arr) {
			str += s + " ";
		}
		return str;
	}
	
	protected abstract void saveDebugHexWithInfo(String file);
}
