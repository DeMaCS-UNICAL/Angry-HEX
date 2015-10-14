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

import java.io.IOException;
import java.util.Random;
import java.util.logging.Logger;

import ab.demo.other.ClientActionRobotJava;

/**
 * @author Stefano, Dasha
 * 
 */
public class DeclarativeStrategy extends StrategyManager {

	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public DeclarativeStrategy(ClientActionRobotJava ar, byte startingLevel,
			byte[] configureData) throws Exception {
		super(ar, startingLevel, configureData);
	}

	/**
	 * Find the next level to play
	 * 
	 * @return next level to play
	 */
	protected byte findNextLevelToPlay() {

		System.out.println("SM: searching for the next level to play");

		byte newLevel = (byte) (1);

		String fact;

		// add facts diff(i,j,k) reflecting the difference j between best scores
		// and ours for level i if k=1 that we played best, otherwise k=0.
		for (int i = 0; i < bestScores.length; i++) {
			int diff = bestScores[i] - myScores[i];
			if (diff > 0) {
				fact = "diff(" + i + "," + diff + "," + 0 + ").";
			} else {
				fact = "diff(" + i + "," + Math.abs(diff) + "," + 1 + ").";
			}

			StrategyReasoner.getInstance().addFact(fact);
		}

		// add facts timeslevelplayed(i,j) reflecting that level i was played j
		// times
		for (int i = 0; i < howManyTimes.length; i++) {
			fact = "timeslevelplayed(" + i + "," + howManyTimes[i] + ").";
			StrategyReasoner.getInstance().addFact(fact);
		}

		// add facts myscore(i,j) reflecting our score j for level i
		for (int i = 0; i < myScores.length; i++) {
			fact = "myscore(" + i + "," + myScores[i] + ").";
			StrategyReasoner.getInstance().addFact(fact);
		}

		// add fact with round info
		fact = "roundinfo(" + roundInfo + ").";
		StrategyReasoner.getInstance().addFact(fact);

		// call hex to compute next level to be played
		try {
			StrategyReasoner.getInstance().reason();
		} catch (UnsupportedOperationException e) {
			Log.severe("could not call dlvhex: " + e.getMessage());
		} catch (IOException e) {
			Log.severe("could not call dlvhex: " + e.getMessage());
		} catch (InterruptedException e) {
			Log.severe("could not call dlvhex: " + e.getMessage());
		}

		newLevel = StrategyReasoner.getInstance().getNewLevel();
		if (newLevel!=111)
			newLevel+=1;
		else {
			newLevel = (byte) (1 + (new Random()).nextInt(21));
		}

		System.out.println("SM: next level to be played " + newLevel);

		return newLevel;
	}
}
