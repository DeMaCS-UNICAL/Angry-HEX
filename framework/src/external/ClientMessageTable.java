/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2014, XiaoYu (Gary) Ge, Jochen Renz, Stephen Gould,
**  Sahan Abeyasinghe,Jim Keys,   Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
*****************************************************************************/

package external;
/**
 * This class maintains all the client messages and its corresponding MIDs.
 * */
public enum ClientMessageTable {
	 configure(1), doScreenShot(11),loadLevel(51),restartLevel(52),cshoot(31), pshoot(32),
	 cFastshoot(41), pFastshoot(42), shootSeqFast(43),clickInCentre(36),
	 getState(12), getMyScore(23),fullyZoomOut(34),fullyZoomIn(35),
	 getCurrentLevel(14), getBestScores(13) , shootSeq(33);
   
   @SuppressWarnings("unused")
   private int message_code;
   private  ClientMessageTable(int message_code)
   {
	   this.message_code = message_code;
   }
 //map message from int to enum
  public static ClientMessageTable getValue(int message_code)
   {
	   switch (message_code)
	   {
	   		case 1:
	   			return configure;
	   		case 11:
	   			return doScreenShot;
	   		case 51:
	   			return loadLevel;
	   		case 52:
	   			return restartLevel;
	   		case 31:
	   			return cshoot;
	   		case 32:
	   			return pshoot;
	   		case 12:
	   			return getState;
	   		case 34:
	   			return fullyZoomOut;
	   		case 35:
	   			return fullyZoomIn;
	   		case 14:
	   			return getCurrentLevel;
	   		case 13:
	   			return getBestScores;
	   		case 23:
	   			return getMyScore;
	   		case 33:
	   			return shootSeq;
	   		case 41:
	   			return cFastshoot;
	   		case 42:
	   			return pFastshoot;
	   		case 43:
	   			return shootSeqFast;
	   		case 36:
	   			return clickInCentre;
	   			
	   }
       return null;
   }
//map message from enum to byte
  public static byte getValue(ClientMessageTable message)
  {
	  switch (message)
	   {
	   		case doScreenShot:	
	   			return 11;
	   		case configure :
	   			return 1;
	   		case loadLevel:
	   			return 51;
	   		case restartLevel:
	   			return 52;
	   		case cshoot :
	   			return 31;
	   		case pshoot :
	   			return 32;
	   		case getState:
	   		    return 12;
	   		case fullyZoomOut:
	   			return 34;
	   		case getCurrentLevel:
	   			return 14;
	   		case getBestScores:
	   			return 13;
	   		case shootSeq:
	   			return 33;
	   		case cFastshoot:
	   			return 41;
	   		case pFastshoot:
	   			return 42;
	   		case shootSeqFast:
	   			return 43;
	   		case getMyScore:
	   			return 23;
	   		case clickInCentre:
	   			return 36;
	   		case fullyZoomIn:
	   			return 35;
	   }
      return 0;
  }
}
