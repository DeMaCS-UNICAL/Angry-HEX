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
package angryhexclient.tactic;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Queue;

import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;

/**
 * TacticManager contains data about the current level and the current turn. it
 * maintains data useful to play the level
 */
public class TacticManager {

	// level data
	private Queue<ABObject> birdsQueue;
	private Rectangle sling;
	private TrajectoryPlanner tp;

	// turn data
	private BufferedImage screenshot;

	/**
	 * Each bird shoot corresponds to a turn.
	 */
	private byte currentTurn;

	private ABType currentBird;

	private boolean firstShoot;

	public TacticManager() {
		reset();
	}

	/**
	 * @return the birdsQueue
	 */
	public Queue<ABObject> getBirdsQueue() {
		return birdsQueue;
	}

	public ABType getCurrentBird() {
		return currentBird;
	}

	/**
	 * @return the currentTurn
	 */
	public byte getCurrentTurn() {
		return currentTurn;
	}

	/**
	 * @return the screenshot
	 */
	public BufferedImage getScreenshot() {
		return screenshot;
	}

	/**
	 * @return the sling
	 */
	public Rectangle getSling() {
		return sling;
	}

	/**
	 * @return the tp
	 */
	public TrajectoryPlanner getTp() {
		return tp;
	}

	public void increaseCurrentTurn() {
		currentTurn++;
	}

	public void initForNextTurn() {
		resetCurrentTurn();
	}

	/**
	 * @return the firstShoot
	 */
	public boolean isFirstShoot() {
		return firstShoot;
	}

	public void reset() {
		tp = new TrajectoryPlanner();
		birdsQueue = null;
		sling = null;
		screenshot = null;
		firstShoot = true;
		currentBird = null;

		resetCurrentTurn();
	}

	/**
	 * reset the currentTurn
	 */
	public void resetCurrentTurn() {
		currentTurn = 0;
	}

	/**
	 * @param birdsQueue
	 *            the birdsQueue to set
	 */
	public void setBirdsQueue(final Queue<ABObject> birdsQueue) {
		this.birdsQueue = birdsQueue;
	}

	public void setCurrentBird(final ABType currentBird) {
		this.currentBird = currentBird;
	}

	/**
	 * @param firstShoot
	 *            the firstShoot to set
	 */
	public void setFirstShoot(final boolean firstShoot) {
		this.firstShoot = firstShoot;
	}

	public void setNextBird() {

		currentBird = null;
		final ABObject bird = getBirdsQueue().poll();
		if (bird != null)
			setCurrentBird(bird.getType());

	}

	/**
	 * @param screenshot
	 *            the screenshot to set
	 */
	public void setScreenshot(final BufferedImage screenshot) {
		this.screenshot = screenshot;
	}

	/**
	 * @param sling
	 *            the sling to set
	 */
	public void setSling(final Rectangle sling) {
		this.sling = sling;
	}

}
