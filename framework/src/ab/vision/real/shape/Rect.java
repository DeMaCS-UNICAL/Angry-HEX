/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision.real.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;

import ab.vision.ABType;
import ab.vision.real.ImageSegmenter;

public class Rect extends Body
{
	private static final long serialVersionUID = 1L;
	// width and height of the rectangle
    public Polygon p;
   protected double pwidth = -1, plength = -1;
    
    public double getpWidth()
    {
	   	 if(pwidth != -1)
	   		 return pwidth;
	   	 return width;
    }
    
    public double getpLength()
    {
	   	 if(plength != -1)
	   		 return plength;
	   	 return height;
    }
    public Rect(double xs, double ys,  double w, double h, double theta, ABType type)
    {
        
        
        if (h >= w)
        {
            angle = theta;
            pwidth = w;
            plength = h;
        }
        else
        {
            angle = theta + Math.PI / 2;
            pwidth = h;
            plength = w;
        }
        
        centerY = ys;
        centerX = xs;
        
        
        area = (int) (pwidth * plength);
        this.type  = type;
      
        createPolygon();
        super.setBounds(p.getBounds());
        width = p.getBounds().width;
  	  	height = p.getBounds().height;

    } 



    private void createPolygon()
    {
    	 
    	 double angle1 = angle;
         double angle2 = perpendicular(angle1);
         
         // starting point for drawing
         double _xs, _ys;
         _ys = centerY + Math.sin(angle) * plength / 2 + 
              Math.sin(Math.abs(Math.PI/2 - angle)) * pwidth / 2;
         if (angle < Math.PI / 2)
             _xs = centerX + Math.cos(angle) * plength / 2 -
                 Math.sin(angle) * pwidth / 2;
         else if (angle > Math.PI / 2)
             _xs = centerX + Math.cos(angle) * plength / 2 +
                 Math.sin(angle) * pwidth / 2;
         else
             _xs = centerX - pwidth / 2;
             
         p = new Polygon();
         p.addPoint(round(_xs), round(_ys));
         
        
         
         _xs -= Math.cos(angle1) * plength;
         _ys -= Math.sin(angle1) * plength;
         p.addPoint(round(_xs), round(_ys));
         
       
         
         _xs -= Math.cos(angle2) * pwidth;
         _ys -= Math.sin(angle2) * pwidth;
         p.addPoint(round(_xs), round(_ys));
         
         
         
         _xs += Math.cos(angle1) * plength;
         _ys += Math.sin(angle1) * plength;
         p.addPoint(round(_xs), round(_ys));
   
    }
    @Override
    public Rectangle getBounds()
    {
    	return p.getBounds();
    }

    public Rect(int box[], ABType type)
    {
        centerX = (box[0] + box[2]) / 2.0;
        centerY = (box[3] + box[1]) / 2.0;
        pwidth = box[2] - box[0];
        plength = box[3] - box[1];
        angle = Math.PI / 2;
        
        if (plength < pwidth)
        {
            pwidth = plength;
            plength = box[2] - box[0];
            angle = 0;
        }
       
        
        width = (int)pwidth;
        height = (int)plength;
        
        this.type = type;
        
        area = width * height;
        createPolygon();
      
    }
    public Rect(double centerX, double centerY, double pwidth, double plength, double angle, ABType type, int area)
    {
    	  this.centerX = centerX;
    	  this.centerY = centerY;
    	  this.pwidth = pwidth;
    	  this.plength = plength;
    	  this.type = type;
    	  this.angle = angle;
    	  this.area = area;
    	  createPolygon();
    	  super.setBounds(p.getBounds());
    	  width = p.getBounds().width;
    	  height = p.getBounds().height;
          	
    }

    
    /* draw the rectangle onto canvas */
    public void draw(Graphics2D g, boolean fill, Color boxColor)
    {        
    
        
        if (fill) {
            g.setColor(ImageSegmenter._colors[type.id]);
            g.fillPolygon(p);
        }
        else {
            g.setColor(boxColor);
            g.drawPolygon(p);
        }
    }
    
    public static double perpendicular(double angle)
    {
        return angle > Math.PI / 2 ? angle - Math.PI / 2 : angle + Math.PI / 2;
    }
	
	public String toString()
	{
		return String.format("Rect: id:%d type:%s hollow:%b Area:%d w:%7.3f h:%7.3f a:%3.3f at x:%3.1f y:%3.1f", id, type, hollow, area, pwidth, plength, angle, centerX, centerY);
	}
}
