/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
 
package ab.vision.real;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ab.vision.ABType;
import ab.vision.real.shape.Body;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;

public class ConnectedComponent {
    
    // neighbour lookup table
    private static Point _connectedPoint[][][] = null;
    private static boolean _firstTime = true;
    
    // atan lookup table
    private static double atan[][];
    public final static double ANGLE_UNDEFINED = 2 * Math.PI;
    private final static int WINDOW_SIZE = 10;
    
    // types of points in the image
    private final static int EMPTY = 0;
    private final static int FILLED = 1;
    private final static int EDGE = 2;
    
    // define small object as min(width, height) < constant
    private final static int SMALL = 20;
    
    // smoothing constant
    private final static int SMOOTH = 3;
    
    // component parameters
    private int _area;
    private int _perimeter;
    private int _type;
    private int _image[][];
    private double _angleThreshold = 0;
    
    // points and lines in the component
    private ArrayList<LineSegment> _lines = null;
    private ArrayList<Point> _edgePoints = null;
    
    // size of the bounding box
    private int _left, _top, _width, _height;
    
    // extreme points
    private Point _extrema[];
    
    /* Create a new connected component
     * @param   map - class map of the screenshot
     *          x,y - starting coordinate of the connected component
     *          ignore - points on the map to ignore
     *          isEightConnect - whether the component is eight-connected or four-connected
     */
    public ConnectedComponent(final int map[][], int x, int y, boolean ignore[][], boolean isEightConnect)
    {
        // initialise the neighbour map
        initialise(map);
        
        // set object type and size
        _type = map[y][x];
        _extrema = new Point[4];
        _extrema[0] = _extrema[1] = _extrema[2] = _extrema[3] = new Point(x, y);
        
        // apply BFS to find all connected pixels
        boolean searched[][] = new boolean[map.length][map[0].length];
        Queue<Point> q = new LinkedList<Point>();
        q.add(new Point(x, y));
        searched[y][x] = true;
        
        int connectivity = isEightConnect ? 1 : 2;
        
        _edgePoints = new ArrayList<Point>();
        ArrayList<Point> points = new ArrayList<Point>();
        while (!q.isEmpty())
        {
            Point p = q.poll();
            
            if (map[p.y][p.x] == _type ||
                (_type == ImageSegmenter.ICE && map[p.y][p.x] == ImageSegmenter.BLUE_BIRD) ||
                (_type == ImageSegmenter.WHITE_BIRD && map[p.y][p.x] == ImageSegmenter.TRAJECTORY))
            {
                points.add(p);
                ignore[p.y][p.x] = true;
                boolean added = false;
                for (int i = 0; i < 8; i+=connectivity)
                {
                    Point np = _connectedPoint[p.y][p.x][i];
                    
                    // test for image boundaries
                    if (np.x == p.x && np.y == p.y & !added)
                    {
                        _edgePoints.add(p);
                        added = true;
                    }   
                    if (!searched[np.y][np.x])
                    {
                        q.add(np);
                        searched[np.y][np.x] = true;
                    }
                }
            }
            // add point to edge
            else
                _edgePoints.add(p);
        }
        
        // update extremas
        for (Point p : _edgePoints)
        {
            if (p.x < _extrema[0].x)
                _extrema[0] = p;
            if (p.x > _extrema[1].x)
                _extrema[1] = p;
            if (p.y < _extrema[2].y)
                _extrema[2] = p;
            if (p.y > _extrema[3].y)
                _extrema[3] = p;
        }
        
        // set size and location
        _top = _extrema[2].y - 2;
        _left = _extrema[0].x - 2;
        _height = _extrema[3].y - _top + 3;
        _width = _extrema[1].x - _left + 3;
        _area = points.size();
        
        for (int i = 0; i < 4; i++)
            _extrema[i] = new Point(_extrema[i].x - _left, _extrema[i].y - _top);
        
        // generate the image
        _image = new int[_height][_width];
        for (Point p : points)
            _image[p.y-_top][p.x-_left] = FILLED;   
        for (Point p : _edgePoints)
            _image[p.y-_top][p.x-_left] = EDGE;
         
        _perimeter = _edgePoints.size();
    }
    
    /* Trace the contour using Moore-Neighbour tracing 
     * and partition contour points into line segments
     * @return  list of line segments which form a cycle around
     *          outer border of the connected component
     */
    private ArrayList<LineSegment> findLines()
    {
        if (_type == ImageSegmenter.HILLS)        
            _angleThreshold = Math.toRadians(150);
        else
            _angleThreshold = Math.toRadians(85);
        
        // tracing points for Moore-Neighbour algorithm
        Point current = null;
        Point prev = null;
        Point next = null;
        
        // search for starting point
        for (int x = 0; x < _width && current == null; x++)
        for (int y = 0; y < _height; y++)
        {
            if (_image[y][x] != EMPTY)
            {
                current = new Point(x, y);
                prev = new Point(x, y-1);
                break;
            }
        }
        
        Point path[] = new Point[_perimeter];
        path[0] = current;
        int length = 1;
        
        // walk in the anticlockwise direction until initial point
        // is returned to
        next = clockwise(current, prev);
        while (!(next.equals(path[0])))
        {
            if (_image[next.y][next.x] != EMPTY)
            {
                current = next;
                next = prev;
                path[length] = current;
                length++;
            }
            else
            {
                prev = next;
                next = clockwise(current, next);
            }
        }
        
        // partition contour into line segments
        _lines = new ArrayList<LineSegment>();
        LineSegment line = new LineSegment(path[0], ANGLE_UNDEFINED);
        for (int i = 1; i < length - SMOOTH; i++)
        {
            // approximate local angle by looking ahead
            Point p = path[i];
            Point ahead = path[i + SMOOTH];
            
            int yDiff = ahead.y - p.y;
            int xDiff = ahead.x - p.x;
            double angle = atan[yDiff + WINDOW_SIZE][xDiff + WINDOW_SIZE];
            
            // if point adding unsuccessful
            double change = line.addPoint(p, angle, _angleThreshold);
            if (change != 0)
            {
                line.removeEndPoint();
                _lines.add(line);
                
                line = new LineSegment(path[i-1], angle);
            }
        }
        _lines.add(line);
        
        // join lines with similar angle
        if (_lines != null)
        {
            // join _lines with similar angle
            ArrayList<LineSegment> newlines = new ArrayList<LineSegment>();
            LineSegment prevline = _lines.get(0);
            for (int i = 1; i < _lines.size(); i++)
            {
                if (!prevline.join(_lines.get(i)))
                {
                    newlines.add(prevline);
                    prevline = _lines.get(i);
                }
            }
            newlines.add(prevline);
            _lines = newlines;
        }
        return _lines;
    }
    
    /* Determine if the connected component is a Rectangle, 
     * Circle or in fact multiple objects stacked together.
     *
     * @param   list of corners the component contains
     *          (points where contour orientation changes)
     * @return  Most likely shape of the object, null if it is noise
     */
    private Body findShape(List<Point> corners)
    {
        final int RESOLUTION = 50;
        final double res = Math.PI / 2 / RESOLUTION;
        double width = 0;
        double height = 0;
        double angle = 0;
        double areaMin = 99999;
        double areaMax = 0;
        
        for (int i = 0; i < RESOLUTION; i++)
        {
            double theta = i * res;
            double min1 = 99999;
            double max1 = -99999;
            double min2 = 99999;
            double max2 = -99999;
            
            // rotate each point about the origin and record min/max x,y coordinates
            for (Point p : corners)
            {
                double p1 = p.x * Math.cos(theta) + p.y * Math.sin(theta);
                double p2 = p.x * Math.sin(theta) - p.y * Math.cos(theta);
                
                if (p1 < min1) min1 = p1;
                if (p1 > max1) max1 = p1;
                if (p2 < min2) min2 = p2;
                if (p2 > max2) max2 = p2;
            }
            
            // width and height are calculated as difference between
            // min and max distances
            double h = max1 - min1;
            double w = max2 - min2;
            double a = w * h;
            
            if (a < areaMin)
            {
                height = h;
                width = w;
                angle = theta;
                areaMin = a;
            }
            if (a > areaMax)
                areaMax = a;
        }
        
        final double JOIN_THRESHOLD = 1.2;//1.4;
        final int SMALL_SIZE = 14;
        
        // relax circle threshold for objects with larger size
        // and objects which are lying at angles close to 45 degrees
        double tc;
        if (Math.max(width, height) > SMALL_SIZE || Math.abs(angle - Math.PI/4) < Math.PI/8)
            tc = 1.2; //1.3
        else
            tc = 1.1;
            
        double x = _left + _width / 2.0 - 0.5;
        double y = _top + _height / 2.0 - 0.5;
        
        // test for noise
        if (width <= 3 || height <= 3)
            return null;
        

    	Poly poly = new Poly(findLines(), _left, _top, assignType(_type), _left + _width/2, _top + _height/2);
    	int polyArea = getArea(poly.polygon);
        int actualArea = Math.max((_area + _perimeter), polyArea);

        // test for joined component
        
        //if (areaMin > (_area + _perimeter) * JOIN_THRESHOLD)
        if (areaMin > actualArea * JOIN_THRESHOLD)
        {  
        
        	if ( (_area + _perimeter) > 400 )
        	{
            	return poly;
        	} 
        	else
        	{
        		 if (polyArea > (_area ) * 1.1)
        	        	poly.hollow = true;
        		 return poly;
     
        	}
        	
        }
        // test for circle
        if (areaMin * tc > areaMax && Math.abs(_width - _height) <= 3)
        {
            int r = (_width + _height) / 4 - 2;
            return new Circle(x, y, r, assignType(_type));
        }
        Rect rect = new Rect(x, y, width, height, angle, assignType(_type));
        double HOLLOW_THRESHOLD = 1.2; 
        if (areaMin > (_area + _perimeter) * HOLLOW_THRESHOLD)
        {
        	rect.hollow = true;
        }
        return rect;
    }


    public int getArea(Polygon poly)
    {
    	int area = 0;
    	Rectangle rect = poly.getBounds();
    	for (int x = rect.x; x < rect.x + rect.width; x++)
    		for (int y = rect.y; y < rect.y + rect.height; y++)
    		{
    			if (poly.contains(x, y))
    				area ++;
    		}
    	return area;
    	
    }
    //public boolean testHollow(Shape shape){}
    
    /* find the most likely shape of the component
     * @return  most likely shape, null if it is noise
     */
    public Body getBody()
    {
        if (_type == ImageSegmenter.SLING)
            return new Rect(boundingBox(), assignType(_type));
        
        if ((_type > ImageSegmenter.SLING &&
            _type <= ImageSegmenter.PIG) ||
            _type == ImageSegmenter.TRAJECTORY)
            return new Circle(boundingBox(), assignType(_type));
        
        if (_type == ImageSegmenter.HILLS)
            return new Poly(findLines(), _left, _top, assignType(_type), _left+_width/2, _top+_height/2);
        
        ArrayList<Point> corners = new ArrayList<Point>();
        // use all edge points if the shape is small
        if (_width < SMALL || _height < SMALL)
        {
            corners = _edgePoints;
        }
        else
        {   
            // otherwise find corners first by border tracking
            findLines();
         
            for (LineSegment line : _lines)
            {
            		corners.add(line._start);
            		corners.add(line._end);
            }
            for (Point p : _extrema)
            	if (!corners.contains(p))
            		corners.add(p);
            

        }
        return findShape(corners);
    }
    

    
    // return number of internal points in the component
    public int getArea()
    {
        return _area;
    }
    
    // number of border points
    public int getType()
    {
        return _type;
    }
    
    /* the bounding box {left, top, right, bottom} */
    public int[] boundingBox()
    {
        return new int[] { _left + 2, _top + 2, _left + _width - 3, _top + _height - 3};
    }
    
    /* draw the connected component
     * @param   canvas
     *          drawEdge - if border should be drawn
     *          drawCorner - if the corner points should be drawn
     */
    public void draw(BufferedImage canvas, boolean drawEdge, boolean drawCorners)
    {
        for (int y = 2; y < _height-2; y++)
        for (int x = 2; x < _width-2; x++)
        {
            if (_image[y][x] == FILLED)
            {
                canvas.setRGB(x + _left, y + _top, ImageSegmenter._drawColor[_type]);
            }
            else if (drawEdge && _image[y][x] == EDGE)
            {
                canvas.setRGB(x+_left, y+_top, 0x000000);
            }
        }
        
        
        if (drawCorners)
        {
            findLines();
            if (_lines != null)
            for (LineSegment line : _lines)
                line.draw(canvas.createGraphics(), _left, _top);
        }
    }
    
    
    /* initialise neighbour and atan lookup table
     * @param   class map of the current game
     */
    private static void initialise(int map[][])
    {
        if (_connectedPoint != null &&
            _connectedPoint.length == map.length &&
            _connectedPoint[0].length == map[0].length)
            return;
        
        // initialise atan lookup
        if (_firstTime)
        {
            atan = new double[WINDOW_SIZE * 2][WINDOW_SIZE * 2];
                        
            for (int y = 0; y < 2*WINDOW_SIZE; y++)
            for (int x = 0; x < 2*WINDOW_SIZE; x++)
            {
                if (x-WINDOW_SIZE == 0 && y-WINDOW_SIZE == 0)
                    atan[y][x] = ANGLE_UNDEFINED;
                else if (x-WINDOW_SIZE == 0)
                    atan[y][x] = Math.PI / 2;
                else
                {
                    atan[y][x] = Math.atan((double)(y-WINDOW_SIZE) / (x-WINDOW_SIZE));
                    
                    if (atan[y][x] < 0)
                        atan[y][x] += Math.PI;
                }
            }
            _firstTime = false;
        }
        
        // initialise neighbour point map
        int width = map[0].length;
        int height = map.length;
        _connectedPoint = new Point[height][width][8];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                _connectedPoint[y][x][0] = new Point(x, y-1);
                _connectedPoint[y][x][1] = new Point(x+1, y-1);
                _connectedPoint[y][x][2] = new Point(x+1, y);
                _connectedPoint[y][x][3] = new Point(x+1, y+1);
                _connectedPoint[y][x][4] = new Point(x, y+1);
                _connectedPoint[y][x][5] = new Point(x-1, y+1);
                _connectedPoint[y][x][6] = new Point(x-1, y);
                _connectedPoint[y][x][7] = new Point(x-1, y-1);
                for (int i = 0; i < 8; i++)
                {
                    Point p = _connectedPoint[y][x][i];
                    if (p.x >= width || p.y >= height || p.x < 0 || p.y < 0)
                    {
                        _connectedPoint[y][x][i] = new Point(x, y);
                    }
                }
            }
        }
    }
    public ABType assignType(int vision_type)
    {
    	ABType type = ABType.Unknown;
    	switch(vision_type)
    	{
    		case ImageSegmenter.PIG: type = ABType.Pig; break;
    		case ImageSegmenter.STONE: type = ABType.Stone;break;
    		case ImageSegmenter.WOOD: type = ABType.Wood; break;
    		case ImageSegmenter.ICE: type = ABType.Ice; break;
    		case ImageSegmenter.HILLS: type = ABType.Hill; break;
    		case ImageSegmenter.RED_BIRD: type = ABType.RedBird; break;
    		case ImageSegmenter.YELLOW_BIRD: type = ABType.YellowBird; break;
    		case ImageSegmenter.BLUE_BIRD: type = ABType.BlueBird; break;
    		case ImageSegmenter.BLACK_BIRD: type = ABType.BlackBird; break;
    		case ImageSegmenter.WHITE_BIRD: type = ABType.WhiteBird; break;
    		default: type = ABType.Unknown;
    	}
    	return type;
    }
        
    private final static int xClock[][] = {{0, -1, -1},
                                           {0, 0, 0},
                                           {1, 1, 0}};
    private final static int yClock[][] = {{1, 0, 0},
                                           {1, 0, -1},
                                           {0, 0, -1}};
    /* find the next point to trace in Moore-Neighbourhood
     * @param   p - the current contour point
     *          prev - point just examined
     * @return  point connected to p which is in the anticlockwise
     *          direction from p to prev
     */
    private static Point clockwise(Point p, Point prev)
    {
        int dx = prev.x - p.x + 1;
        int dy = prev.y - p.y + 1;
        return new Point(prev.x + xClock[dy][dx], prev.y + yClock[dy][dx]);
    }
}
