ANGRYBIRDS AI AGENT FRAMEWORK
Copyright © 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz, Sahan Abeyasinghe, Jim Keys, Andrew Wang, Peng Zhang. All rights reserved.
This software contains a framework for developing AI agents capable of
playing Angry Birds. The framework is composed of a javascript plugin
for the Chrome web browser and Java client/server code for interfacing
to the game and implementing the AI strategy. A sample agent is provided.

* The details of the server/client protocols can be found in ./doc/ServerClientProtocols.pdf

* We provide a sample agent communicating with the server by the pre-defined protocols. The implementation can be found in ./src/ab/demo/ClientNaiveAgent.java

* We provide a wrapper class in java that can encode/decode the communicating messages. The wrapper can be found in ./src/ab/demo/other/{ClientActionRobot.java , ClientActionRobotJava.java}

* We provide a sample C++ client that demonstrates how to connect to the server and send a doScreenShot message. It can be found in ./src/ab/demo/abClientExample.cpp

===================   Commands of the server/client version =====================================================

* Please start the server first, and then start your client. 

To start the server: java -jar ABServer.jar

To start the naive agent (server/client version): java -jar ABSoftware.jar -nasc [IP]

*You do not need to specify the IP when the server is running on localhost 

=====================  Commands of the standalone version =======================================================
  
  java -jar ABSoftware.jar -na                // run the agent from level 1
   
  java -jar ABSoftware.jar -na [0-21]       	// run the agent from the specified level.

  java -jar ABSoftware.jar -na [0-21] -showMBR     // run the agent with the real-time vision output (MBR segmentation)

  java -jar ABSoftware.jar -na [0-21] -showReal // run the agent with the real-time vision output (Real shape segmentation)

  java -jar ABSoftware.jar -showMBR        // show the real-time MBR segmentation

  java -jar ABSoftware.jar -showReal       // show the real-time real shape segmentation

  java -jar ABSoftware.jar -showTraj        // show the real-time trajectory prediction

  java -jar ABSoftware.jar -recordImg [directory] // save the current game image to the specified directory


================================  Outline of the source files ====================================================

The src folder contains all the java source codes of the software. 

The following are the files you may want to modify:

========= Files under ./src/ab/demo/ =====================				
ClientNaiveAgent.java : A server/client version of the Naive agent that interacts with the server by the pre-defined protocols

NaiveAgent.java : A standardalone implementation of the Naive agent. You can run this agent without the server.

======== Files under ./src/ab/demo/other/ ================

ActionRobot.java : A java util class for the standalone version. It provides common functions an agent would use. E.g. get the screenshot

ClientActionRobot.java : A server/client version of the java util class that encodes client messages and decodes the corresponding server messages complying with the protocols. Its subclass is ClientActionRobotJava.java which decodes the received server messages into java objects.

LoadingLevelSchema.java / RestartLevelSchema.java / ShootingSchema.java : Those files are only for the standalone version. A standalone agent can use the schemas respectively to load levels, restart levels, and launch birds. 

======== Files under ./src/ab/planner/ ====================

TrajectoryPlanner.java : Implementation of the trajectory module

=======  Files under ./src/ab/vision ======================

Vision.java ： the entry of the vision module. 

VisionMBR.java : image segmenting component that outputs a list of MBRs of a screenshot

VisionRealShape.java: image segmenting component that outputs a list of real shapes of a screenshot

=======  Other Useful methods in /src/ab/utils/ABUtil.java =========================================

isSupport(ABObject object, ABObject support): returns true if support directly supports object

getSupports(ABObject object, List blocksList): returns a list containing the subset of blocksList that support object 
 
isReachable(Vision vision, Point target, Shot shot): returns true if the target is reachable (without obstruction) by the shot

====== Files under ./src/external ===========================

ClientMessageEncoder.java : encode the client messages according to the protocols

ClientMessageTable.java : a table that maintains all the client messages and its corresponding MIDs. 

