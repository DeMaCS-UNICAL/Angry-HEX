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

	public DeclarativeStrategy(final ClientActionRobotJava ar, final byte startingLevel, final byte[] configureData)
			throws Exception {
		super(ar, startingLevel, configureData);
	}

	/**
	 * Find the next level to play
	 *
	 * @return next level to play
	 */
	@Override
	protected byte findNextLevelToPlay() {

		System.out.println("SM: searching for the next level to play");

		byte newLevel = (byte) 1;

		String fact;

		// add facts diff(i,j,k) reflecting the difference j between best scores
		// and ours for level i if k=1 that we played best, otherwise k=0.
		for (int i = 0; i < bestScores.length; i++) {
			final int diff = bestScores[i] - myScores[i];
			if (diff > 0)
				fact = "diff(" + i + "," + diff + "," + 0 + ").";
			else
				fact = "diff(" + i + "," + Math.abs(diff) + "," + 1 + ").";

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

			newLevel = StrategyReasoner.getInstance().getNewLevel();
			if (newLevel != 111)
				newLevel += 1;
			else
				// newLevel = (byte) (1 + (new Random()).nextInt(21));
				newLevel = (byte) (1 + new Random().nextInt(numberOfLevels));

			System.out.println("SM: next level to be played " + newLevel);

			return newLevel;

		} catch (UnsupportedOperationException | IOException | InterruptedException e) {
			DeclarativeStrategy.Log.severe("could not call dlvhex: " + e.getMessage());
			DeclarativeStrategy.Log.severe("Using fall-back Strategy");

			/**
			 * First it tries to play each level one time
			 */
			for (int i = 0; i < numberOfLevels; i++)
				if (myScores[i] == 0 && howManyTimes[i] == 0)
					return (byte) (i + 1);

			/**
			 * Next it tries to play the levels that have the maximum difference
			 * from the best scores
			 */
			int maxDifference = 0;
			newLevel = (byte) 1;

			for (int i = 0; i < numberOfLevels; i++) {
				final int difference = bestScores[i] - myScores[i];
				if (difference >= maxDifference && howManyTimes[i] < 3) {
					maxDifference = difference;
					newLevel = (byte) (i + 1);
				}
			}

			if (maxDifference != 0)
				return newLevel;

			/**
			 * Finally it tries to play the levels that we perform better than
			 * best scores and that have the minimum difference from the best
			 * score
			 */
			int minDifference = Integer.MAX_VALUE;
			newLevel = (byte) (new Random().nextInt(numberOfLevels) + 1);

			for (int i = 0; i < numberOfLevels; i++) {
				final int difference = myScores[i] - bestScores[i];
				if (difference >= 0 && difference < minDifference && howManyTimes[i] < 5) {
					minDifference = difference;
					newLevel = (byte) (i + 1);
				}
			}

			return newLevel;

		}
	}
}
