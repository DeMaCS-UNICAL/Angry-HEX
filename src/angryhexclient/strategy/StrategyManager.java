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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import ab.demo.other.ClientActionRobotJava;
import ab.vision.GameStateExtractor.GameState;
import angryhexclient.Configuration;
import angryhexclient.Memory;
import angryhexclient.TerminateAgentException;

/**
 * @author Stefano
 *
 */
public abstract class StrategyManager {

	protected static final Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	protected byte currentLevel;

	/**
	 * Round Info: a byte indicates the ongoing round of the competition. 1: 1st
	 * qualification round 2: 2nd qualification round 3: Group round (group of
	 * 4) 4: Knock-out round (group of 2) 0: Reject
	 */
	protected byte roundInfo;

	/**
	 * Time_limit: a byte indicates the time limit in minutes
	 */
	protected byte timeLimit;

	/**
	 * Number of Levels: a byte indicates the number of available levels.
	 */
	protected byte numberOfLevels;

	/**
	 * Our best scores for each level
	 */
	protected int[] myScores;

	/**
	 * Global best scores for each level
	 */
	protected int[] bestScores;

	/**
	 * How many times was played each level
	 */
	protected int[] howManyTimes;

	protected ClientActionRobotJava ar;

	public StrategyManager(final ClientActionRobotJava ar, final byte startingLevel, final byte[] configureData)
			throws Exception {

		this.ar = ar;

		currentLevel = startingLevel;

		roundInfo = configureData[0];
		// based on its value we can choose a different strategy

		Log.info("roundInfo: " + roundInfo);


		if (roundInfo != 2)
			try {

				final File theDir = new File(Memory.MEMORY_DIR);
				// if the directory exist, we remove it
				if (theDir.exists()) {
					final Process p = Runtime.getRuntime().exec("rm -rf " + Memory.MEMORY_DIR);
					p.waitFor();
					// boolean result = theDir.delete();
					// System.out.println(result);
					// if (result)
					// System.out.println("DIR memory deleted");
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}

		timeLimit = configureData[1];
		// based on this variable we can decide to play in a different way

		Log.info("timeLimit: " + timeLimit);

		numberOfLevels = configureData[2];

		Log.info("numberOfLevels: " + numberOfLevels);

		if (numberOfLevels > 21)
			throw new Exception("*** Number of Levels not allowed by the rule of the Angry Birds AI Competition ***");

		myScores = new int[numberOfLevels];

		initializeMyScores();

		bestScores = new int[numberOfLevels];

		updateBestScores();

		howManyTimes = new int[numberOfLevels];

		initializeHowManyTimes();

	}

	/**
	 * Find the next level to play
	 *
	 * @return next level to play
	 */
	protected abstract byte findNextLevelToPlay() throws TerminateAgentException;

	/**
	 * Returns the current level
	 *
	 * @return the currentLevel
	 */
	public byte getCurrentLevel() {
		return currentLevel;
	}

	/**
	 * 
	 * @return
	 */
	public int getHowManyTimesCurrentLevel() {
		//TODO check if currentLevel - 1 is correct
		return howManyTimes[currentLevel - 1];
	}

	/**
	 * Initialize howManyTimes
	 */
	private void initializeHowManyTimes() {
		for (int i = 0; i < numberOfLevels; i++)
			howManyTimes[i] = 0;
	}

	/**
	 * Initialize myScores
	 */
	private void initializeMyScores() {

		final int[] myNewScores = ar.checkMyScore();

		for (int i = 0; i < numberOfLevels; i++)
			myScores[i] = myNewScores[i];

	}

	/**
	 * Load the current level
	 */
	public void loadCurrentLevel() {
		ar.loadLevel(currentLevel);
	}

	/**
	 * Load the next level to play
	 *
	 * @param state
	 */
	public void loadNewLevel(final GameState state) throws TerminateAgentException {

		if(state == null){
			loadCurrentLevel();
			return;
		}
			
		
		if (roundInfo == 3 || roundInfo == 4)
			updateBestScores();

		boolean iHaveToLoadTheLevel = true;

		if (!Configuration.getTournamentMode()) {
			if (state == GameState.WON)
				currentLevel++;
		} else {

			howManyTimes[currentLevel - 1]++;

			iHaveToLoadTheLevel = updateCurrentLevel(state);
			Log.info("iHaveToLoadTheLevel="+iHaveToLoadTheLevel);
			Log.info("currentLevel="+currentLevel);
		}

		if (iHaveToLoadTheLevel){
			if (currentLevel!=-1){
				ar.loadLevel(currentLevel);
				Log.info("chose level - loading currentLevel="+currentLevel);
			}
			else{
				Log.info("currentLevel==-1 - stop playing");
				return;
			}	
		}
		else{
			// ZGS: below was restarting the level when everything is in order - commented out for now
			//ar.restartLevel();
			return;
		}
	}

	/**
	 * Save myScores on file
	 *
	 * @param fileName
	 * @throws IOException
	 */
	public void saveScores(final String fileName) throws IOException {

		final PrintWriter log = new PrintWriter(new FileWriter(fileName, false));

		for (int i = 0; i < numberOfLevels; i++)
			log.println("Level " + (i + 1) + ";" + myScores[i]);

		log.close();

	}

	/**
	 * Update bestScores
	 */
	private void updateBestScores() {

		final int[] newBestScores = ar.checkScore();

		for (int i = 0; i < numberOfLevels; i++)
			bestScores[i] = newBestScores[i];

	}

	/**
	 * Update currentLevel
	 *
	 * @param state
	 * @return
	 */
	public boolean updateCurrentLevel(final GameState state) throws TerminateAgentException {

		byte tentativeCurrentLevel = currentLevel;

		if (state == GameState.WON) {
			tentativeCurrentLevel = findNextLevelToPlay();
			Log.info("tentativeCurrentLevel: " + tentativeCurrentLevel);
			if (tentativeCurrentLevel==-1){
				Log.warning("tentativeCurrentLevel==-1 - stop playing");
				currentLevel = tentativeCurrentLevel;
				return true;
			}

			Log.fine("loading tentativeCurrentLevel="+tentativeCurrentLevel);
			ar.loadLevel(tentativeCurrentLevel);

			updateMyScores();

			Log.info("\nLevel: " + currentLevel + " NEW score: " + myScores[currentLevel - 1]);
			currentLevel = tentativeCurrentLevel;

		} else
			currentLevel = findNextLevelToPlay();

		// currentLevel = findNextLevelToPlay();

		Log.info("NEW currentLevel: " + currentLevel + "\n");

		if (state == GameState.LOST || currentLevel != tentativeCurrentLevel)
			return true;

		return false;

	}

	/**
	 * Update myScores
	 *
	 * @throws MyScoreUpdateException
	 */
	private void updateMyScores() {

		final int[] myNewScores = ar.checkMyScore();

		// System.out.println("CURRENT LEVEL " + currentLevel);
		// for (int i = 0; i < numberOfLevels; i++)
		// System.out.println("SCORE " + (i + 1) + ": " + myNewScores[i]);

		myScores[currentLevel - 1] = myNewScores[currentLevel - 1];

	}

	// updates score of current level
	public void updateScore(final int score) {
	}
}
