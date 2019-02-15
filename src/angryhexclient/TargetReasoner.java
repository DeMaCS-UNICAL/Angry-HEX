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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ab.vision.ABType;
import angryhexclient.Configuration;
import angryhexclient.util.DebugUtils;
import angryhexclient.util.Utils;

public class TargetReasoner extends Reasoner {

	public class TargetData {
		// The trajectory, 0 for low, 1 for high.
		public int trajectory;
		// The target point that the bird will be shot at.
		public int id;
		public int tapCoeff;
		public int yoffsetRatio;
		public boolean eggMode;

		public TargetData(final int id, final int tr, final int tap, final int yoffset, final boolean eggMode) {
			trajectory = tr;
			this.id = id;
			tapCoeff = tap;
			yoffsetRatio = yoffset;
			this.eggMode = eggMode;
		}

		@Override
		public String toString() {
			return "ID:" + id + " TRAJ:" + trajectory + " TAP:" + tapCoeff + " YOFF:" + yoffsetRatio + " MODE:"
					+ eggMode;
		}
	}

	/*
	 * Class made for Debugging Purposes
	 * There is a "marked" atom, that stores visual debugging information about various atoms
	 * These atoms are then received from the answerstes and retrieved by the Debug class
	 */
	public class MarkedData {
		public String identifier;
		public int id;
		public int time;
		public Color color;

		public MarkedData(final String identifier, final String color, final int id, final int time) {
			// example for a marked atom: marked(shootable,c6600ff,35,0).
			this.identifier = identifier;
			this.color = hex2rgb(color);
			this.id = id;
			this.time = time;
		}

		@Override
		public String toString() {
			return "Identifier: " + identifier + " Color: " + color + " ID: " + id + " Time: " + time;
		}

		private Color hex2rgb(String colorStr) {
    			return new Color(
        			Integer.valueOf( colorStr.substring(1,3),16),
        			Integer.valueOf( colorStr.substring(3,5),16),
        			Integer.valueOf( colorStr.substring(5,7),16));
		}
	}

	// TODO: fix inspection feature
	private static String inspect = Configuration.getInspection();

	private static String filterPred = "targetData" + (Configuration.isDebugMode()?",marked" + ((inspect.equals(""))? "":","+inspect):"");

	private static Pattern filterRegex = Pattern
			.compile("targetData\\((\\d+),(high|low),(\\d+),(\\d+),(\\d+)\\)");

	private static TargetReasoner instance;

	public static TargetReasoner getInstance() {
		if (TargetReasoner.instance == null)
			TargetReasoner.instance = new TargetReasoner();
		return TargetReasoner.instance;
	}

	private final List<TargetData> targets;

	private final List<List<MarkedData>> marked; //NEW

	private int callCount;

	private TargetReasoner() {
		super(TargetReasoner.filterPred, TargetReasoner.filterRegex);

		targets = new ArrayList<>();
		marked = new ArrayList<>();

		callCount = 0;
	}

	@Override
	protected void clear() {
		targets.clear();
	}

	@Override
	protected String getFactFilename() {
		return String.format("targetData%d.hex", callCount++);
	}

	public List<TargetData> getTargets() {
		return targets;
	}

	public List<List<MarkedData>> getMarkedData() {
		return marked;
	}

	public void reason(final ABType birdType) throws UnsupportedOperationException, IOException, InterruptedException {
		// get suitable encoding file
		String encFile = Reasoner.clientDir + Utils.DLV_DIR + File.separator;
		if (Configuration.isCalibrationMode())
			encFile += "calibrate.dlv";
		else {
			encFile += Configuration.getReasoningFilename();
			encFile += " " + Reasoner.clientDir + Utils.DLV_DIR + File.separator;
			encFile += Configuration.getReasoningFixedKnowledgeFilename();

			if (birdType == ABType.WhiteBird) {
				encFile += " " + Reasoner.clientDir + Utils.DLV_DIR + File.separator;
				encFile += Configuration.getReasoningWhiteFilename();
			}
		}
		setEncodingFile(encFile);
		reason();

		if( dlvhexOutput != null && Configuration.isDebugMode()) {
			storeMarked();
			inspectAtom();
		}
	}

	@Override
	protected void saveDebugHexWithInfo(final String file) {
		DebugUtils.saveHexWithInfo(file, "TargetF");
		int i = 0;
		for(String encSingleFile: file.split(" ")) {
			DebugUtils.saveHexWithInfo(encSingleFile, "Target"+Integer.toString(i));
			i++;
		}
	}

	@Override
	protected void storeAtom(final Matcher m) {
		final int id = Integer.parseInt(m.group(1));
		final int trajectory = m.group(2).equals("low") ? 0 : 1;
		final int tapCoeff = Integer.parseInt(m.group(3));
		final int yoffset = Integer.parseInt(m.group(4));
		final int eggMode = Integer.parseInt(m.group(5));

		final TargetData a = new TargetData(id, trajectory, tapCoeff, yoffset, eggMode != 0);
		targets.add(a);
	}

	/*
	 * Essentially the same as storeAtom, but only for the "marked" atoms
	 */
	protected void storeMarked(){
		Pattern regex = Pattern.compile("marked\\((\\w+),(\\w+),(\\d+),(\\d+)\\)");
		String[] answersets = dlvhexOutput.split("\n");
		for(String out: answersets){
			ArrayList<MarkedData> dat = new ArrayList<>();
			Matcher m = regex.matcher(out);
			while(m.find()){
				final String identifier = m.group(1);
				final String color = m.group(2);
				final int id = Integer.parseInt(m.group(3));
				final int time = Integer.parseInt(m.group(4));

				final MarkedData a = new MarkedData(identifier, color, id, time);
				dat.add(a);
			}
			marked.add(dat);
		}
	}

	/*
	 * Added for Debugging
	 * This method receives the name of an atom to be inspected
	 * The list is written in the Debug HTML file
	 */
	protected void inspectAtom(){
		if(inspect.equals(""))
			return;
		ArrayList<String> list = new ArrayList<>();
		Pattern ins = Pattern.compile(inspect + "\\((\\w+)\\)");
		Matcher m = ins.matcher(dlvhexOutput);
		while(m.find()){
			list.add(inspect + "(" + m.group(1) + ")");
		}
		DebugUtils.addInspectedAtom(list);
	}
}
