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
package angryhexclient.strategy;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import angryhexclient.Reasoner;
import angryhexclient.util.DebugUtils;
import angryhexclient.util.Utils;

public class StrategyReasoner extends Reasoner {

	private static final String filterPred = "newlevel";
	private static final Pattern filterRegex = Pattern
			.compile("\\{newlevel\\((\\d+)\\)\\}");
	private static final String encodingFile = Utils.DLV_DIR + File.separator + "strategy.dlv";

	private static StrategyReasoner instance;

	public static StrategyReasoner getInstance() {
		if (instance == null) {
			instance = new StrategyReasoner();
		}
		return instance;
	}

	private byte newLevel;
	private int callCount;

	public StrategyReasoner() {
		super(filterPred, filterRegex);
		setEncodingFile(encodingFile);

		this.newLevel = 0;
		this.callCount = 0;
	}

	public byte getNewLevel() {
		return newLevel;
	}

	@Override
	protected String getFactFilename() {
		return String.format("strategy%d.hex", callCount++);
	}

	@Override
	protected void clear() {
		newLevel = 0;
	}

	@Override
	protected void storeAtom(Matcher m) {
		// FIXME always just one answer set???
		newLevel = Byte.parseByte(m.group(1));
	}
	
	@Override
	protected void saveDebugHexWithInfo(String file){
	    DebugUtils.saveHexWithInfo(file,"Strategy");
	}
}
