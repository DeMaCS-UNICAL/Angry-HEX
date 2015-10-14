package ab.demo;

import ab.planner.abTrajectory;
import ab.utils.GameImageRecorder;
import ab.vision.ShowSeg;

/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Jochen Renz,Stephen Gould,
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/

public class MainEntry {
	// the entry of the software.
	public static void main(String args[])
	{
		String command = "";
		if(args.length > 0)
		{
			command = args[0];
			if (args.length == 1 && command.equalsIgnoreCase("-na"))
			{
				NaiveAgent na = new NaiveAgent();
				na.run();
			}
			else
				if(command.equalsIgnoreCase("-cshoot"))
				{
					ShootingAgent.shoot(args, true);
				}
				else
					if(command.equalsIgnoreCase("-pshoot"))
					{
						ShootingAgent.shoot(args, false);
					}

					else	
						if (args.length == 1 && command.equalsIgnoreCase("-nasc"))
						{
							ClientNaiveAgent na = new ClientNaiveAgent();
							na.run();
						} 
						else 
							if (args.length == 2 && command.equalsIgnoreCase("-nasc"))
							{
								ClientNaiveAgent na = new ClientNaiveAgent(args[1]);
								na.run();
							}
							else
								if(args.length == 3 && command.equalsIgnoreCase("-nasc"))
								{
									int id = Integer.parseInt(args[2]);
									ClientNaiveAgent na = new ClientNaiveAgent(args[1],id);
									na.run();
								}

							else 
						if (args.length == 2 && command.equalsIgnoreCase("-na"))
						{
							NaiveAgent na = new NaiveAgent();
							if(! (args[1].equalsIgnoreCase("-showMBR") || args[1].equals("-showReal")))
							{
								int initialLevel = 1;
								try{
									initialLevel = Integer.parseInt(args[1]);
								}
								catch (NumberFormatException e)
								{
									System.out.println("wrong level number, will use the default one");
								}
								na.currentLevel = initialLevel;
								na.run();
							}
							else
							{
								Thread nathre = new Thread(na);
								nathre.start();										   
								if(args[1].equalsIgnoreCase("-showReal"))
									ShowSeg.useRealshape = true;
								Thread thre = new Thread(new ShowSeg());
								thre.start();
							}
						} 
						else if (args.length == 3 && (args[2].equalsIgnoreCase("-showMBR") || args[2].equalsIgnoreCase("-showReal")) && command.equalsIgnoreCase("-na"))
						{
							NaiveAgent na = new NaiveAgent();
							int initialLevel = 1;
							try{
								initialLevel = Integer.parseInt(args[1]);
							}
							catch (NumberFormatException e)
							{
								System.out.println("wrong level number, will use the default one");
							}
							na.currentLevel = initialLevel;
							Thread nathre = new Thread(na);
							nathre.start();
							if(args[2].equalsIgnoreCase("-showReal"))
								ShowSeg.useRealshape = true;
							Thread thre = new Thread(new ShowSeg());
							thre.start();

						}

						else if(command.equalsIgnoreCase("-showMBR"))
						{
							ShowSeg showseg = new ShowSeg();
							showseg.run();						
						}
						else if (command.equalsIgnoreCase("-showReal"))
						{
							ShowSeg showseg = new ShowSeg();
							ShowSeg.useRealshape = true;
							showseg.run();
						}
						else if (command.equalsIgnoreCase("-showTraj"))
						{
							String[] param = {};
							abTrajectory.main(param);
						} 
						else if (command.equalsIgnoreCase("-recordImg"))
						{

							if(args.length < 2)
								System.out.println("please specify the directory");
							else
							{
								String[] param = {args[1]};   

								GameImageRecorder.main(param);
							}
						}
						else 
							System.out.println("Please input the correct command");
		}
		else 
			System.out.println("Please input the correct command");
		

	}
}
