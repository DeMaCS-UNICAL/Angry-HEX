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
#ifndef CONFIG_H
#define CONFIG_H

//Defines the factor, by which objects from the dlv program will be scaled for physics calculations. The numbers will be divided by this constant.
#define SCALE_FACTOR 10

//The arguments of a simulation step.
#define TIME_STEP 1.0f / 30.0f
#define VELOCITY_ITERATIONS 6
#define POSITION_ITERATIONS 2

//Densities for mass calculation
#define PIG_DENSITY 1
#define ICE_DENSITY 1
#define WOOD_DENSITY 2
#define STONE_DENSITY 3
#define TNT_DENSITY 2

#define RED_SIZE 10
#define YELLOW_SIZE 14
#define BLUE_SIZE 9
#define BLACK_SIZE 18
#define WHITE_SIZE 18

//This flag enables the saving of various debug images. Look in debugFolder for SVGs.
const bool debug = false;

//The folder for all file debug output, if so enabled in other flags. Please be sure to append the trailing sperator (e.g. '/').
const char debugFolder[] = "/home/ianni/plugintest/";

#endif
