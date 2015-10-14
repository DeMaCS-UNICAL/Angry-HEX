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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import ab.demo.other.ClientActionRobotJava;

public class BenchmarkStrategy extends StrategyManager {

	final public static boolean BENCHMARK = false;

	private static Logger Log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static String BENCHMARK_FILE = "benchmark.csv";

	// levels to benchmark
	private int[] levels = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
			13, 14, 15, 16, 17, 18, 19, 20, 21 };
	// number of runs per level
	private int runs = 5;
	// index of current level in array 'levels'
	private int currIdx = 0;
	// count of runs of levels[currIdx]
	private int currRun = 0;

	int[][] scores = new int[levels.length][runs];

	public BenchmarkStrategy(ClientActionRobotJava ar, byte startingLevel,
			byte[] configureData) throws Exception {
		super(ar, startingLevel, configureData);
		if (levels.length < 1) {
			throw new Exception("No levels for benchmark specified");
		} else if (runs < 1) {
			throw new Exception("No runs for benchmark specified");
		}
	}

	@Override
	protected byte findNextLevelToPlay() {
		if (currRun < runs) {
			currRun++;
			return (byte) levels[currIdx];
		} else if (currIdx < levels.length - 1) {
			currRun = 1;
			currIdx++;
			return (byte) levels[currIdx];
		}
		saveResult();
		Log.warning("All levels in benchmark strategy covered.");
		return (byte) -1;
	}

	@Override
	public void updateScore(int score) {
		scores[currIdx][currRun] = score;
	}

	private int[][] evaluate() {
		int[][] res = new int[levels.length][4];
		for (int i = 0; i < levels.length; i++) {
			int sum = 0;
			int won = 0;
			int best = 0;
			for (int j = 0; j < runs; j++) {
				if (scores[i][j] > 0) {
					won++;
				}
				if (scores[i][j] > best) {
					best = scores[i][j];
				}
				sum += scores[i][j];
			}
			res[i][0] = won;
			res[i][1] = sum / won;
			res[i][2] = sum / runs;
			res[i][3] = best;
		}
		return res;
	}

	//commented out because it's not used
//	private void printResult() {
//		int[][] res = evaluate();
//		for (int i = 0; i < res.length; i++) {
//			Log.warning(String.format("L%d   %d   %d   %d   %d", levels[i],
//					res[i][0], res[i][1], res[i][2], res[i][3]));
//		}
//	}

	private void saveResult() {
		int[][] res = evaluate();
		try {
			PrintWriter wr = new PrintWriter(new FileWriter(BENCHMARK_FILE,
					false));
			wr.println(String
					.format("Level;Number of WON runs;AVG excl. LOST;AVG incl. LOST;Best score"));
			for (int i = 0; i < res.length; i++) {

				wr.println(String.format("%d;%d;%d;%d;%d", levels[i],
						res[i][0], res[i][1], res[i][2], res[i][3]));
			}
			wr.close();
		} catch (IOException e) {
			Log.warning("couldn't save benchmark results");
		}
	}
}
