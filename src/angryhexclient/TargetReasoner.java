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

	private static String filterPred = "targetData";

	private static Pattern filterRegex = Pattern
			.compile("\\{targetData\\((\\d+),(high|low),(\\d+),(\\d+),(\\d+)\\)\\}");

	private static TargetReasoner instance;

	public static TargetReasoner getInstance() {
		if (TargetReasoner.instance == null)
			TargetReasoner.instance = new TargetReasoner();
		return TargetReasoner.instance;
	}

	private final List<TargetData> targets;

	private int callCount;

	private TargetReasoner() {
		super(TargetReasoner.filterPred, TargetReasoner.filterRegex);

		targets = new ArrayList<>();
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
	}

	@Override
	protected void saveDebugHexWithInfo(final String file) {
		DebugUtils.saveHexWithInfo(file, "Target");
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
}
