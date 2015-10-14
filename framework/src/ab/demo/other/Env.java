/*****************************************************************************
** ANGRYBIRDS AI AGENT FRAMEWORK
** Copyright (c) 2013, XiaoYu (Gary) Ge, Jochen Renz,Stephen Gould,
**  Sahan Abeyasinghe,Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
** All rights reserved.
**This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
**To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
*or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
*****************************************************************************/
package ab.demo.other;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

// read the focus point from the file
public class Env {

private static HashMap<Integer,Point> focuslist = new HashMap<Integer,Point>();

public static HashMap<Integer, Point> getFocuslist() {
	return focuslist;
}


static
{
    File file = new File("Setup.ini");
    if(file.exists())
		{
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				while(br.ready())
				{
					String line = br.readLine();
				    if(!line.contains("#"))
				    {
				    	if(line.contains(("focus_pt")))
				    	{
				    		String str = line.substring(line.lastIndexOf(":")+1);
				    		String str_x = str.substring(0,str.indexOf(","));
				    		String str_y = str.substring(str.indexOf(",")+1,str.lastIndexOf(","));
				    		String str_z = str.substring(str.lastIndexOf(",") + 1);
				    		int x = Integer.parseInt(str_x);
				    		int y = Integer.parseInt(str_y);
				    		int z = Integer.parseInt(str_z);
				            focuslist.put(z, new Point(x,y));
				    	}
				    
				    }
				   
				}
			} catch (IOException e) {
			
				e.printStackTrace();
			}

		
	}

}






}