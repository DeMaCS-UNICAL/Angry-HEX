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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ab.vision.ABType;
import angryhexclient.util.DebugUtils;
import angryhexclient.util.Utils;

public class TargetReasoner extends Reasoner {

	private static String filterPred = "targetData";
	private static Pattern filterRegex = Pattern
			.compile("\\{targetData\\((\\d+),(high|low),(\\d+),(\\d+),(\\d+)\\)\\}");

	private static TargetReasoner instance;

	public static TargetReasoner getInstance() {
		if (instance == null) {
			instance = new TargetReasoner();
		}
		return instance;
	}

	private List<TargetData> targets;
	private int callCount;
	
	private TargetReasoner() {
		super(filterPred, filterRegex);

		this.targets = new ArrayList<TargetData>();
		this.callCount = 0;
	}

	public List<TargetData> getTargets() {
		return targets;
	}

	public void reason(ABType birdType) throws UnsupportedOperationException,
			IOException, InterruptedException {
		// get suitable encoding file
		String encFile = clientDir + Utils.DLV_DIR + File.separator;
		if (Configuration.isCalibrationMode()) {
			encFile += "calibrate.dlv";
		} else if (birdType == ABType.WhiteBird) {
			encFile += Configuration.getReasoningWhiteFilename();
		} else {
			encFile += Configuration.getReasoningFilename();
		}
		setEncodingFile(encFile);
		reason();
	}

	@Override
	protected String getFactFilename() {
		return String.format("targetData%d.hex", callCount++);
	}
	
	@Override
	protected void clear() {
		targets.clear();
	}

	@Override
	protected void storeAtom(Matcher m) {
		int id = Integer.parseInt(m.group(1));
		int trajectory = m.group(2).equals("low") ? 0 : 1;
		int tapCoeff = Integer.parseInt(m.group(3));
		int yoffset = Integer.parseInt(m.group(4));
		int eggMode = Integer.parseInt(m.group(5));

		TargetData a = new TargetData(id, trajectory, tapCoeff, yoffset,
				eggMode != 0);
		targets.add(a);
	}

	public class TargetData {
		// The trajectory, 0 for low, 1 for high.
		public int trajectory;
		// The target point that the bird will be shot at.
		public int id;
		public int tapCoeff;
		public int yoffsetRatio;
		public boolean eggMode;

		public TargetData(int id, int tr, int tap, int yoffset, boolean eggMode) {
			this.trajectory = tr;
			this.id = id;
			this.tapCoeff = tap;
			this.yoffsetRatio = yoffset;
			this.eggMode = eggMode;
		}

		public String toString() {
			return "ID:" + id + " TRAJ:" + trajectory + " TAP:" + tapCoeff
					+ " YOFF:" + yoffsetRatio + " MODE:" + eggMode;
		}
	}
	
	@Override
	protected void saveDebugHexWithInfo(String file){
	    DebugUtils.saveHexWithInfo(file,"Target");
	}
}
