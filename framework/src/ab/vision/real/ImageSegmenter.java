/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.vision.real;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class ImageSegmenter {

    // objects in game
    public static final int BACKGROUND = 0;
    public static final int GROUND = 1;
    public static final int HILLS = 2;
    public static final int SLING = 3;
    public static final int RED_BIRD = 4;
    public static final int YELLOW_BIRD = 5;
    public static final int BLUE_BIRD = 6;
    public static final int BLACK_BIRD = 7;
    public static final int WHITE_BIRD = 8;
    public static final int PIG = 9;
    public static final int ICE = 10;
    public static final int WOOD = 11;
    public static final int STONE = 12;
    public static final int DUCK = 13;
    public static final int EDGE = 14;
    public static final int WATERMELON = 15;
    public static final int TRAJECTORY = 16;
    public static final int CLASS_COUNT = 17;
    
    public static final int MAX_DIST = 100;
    
    // training color points
    private static final int _trainData[][] = {
        {144, 200, 216, BACKGROUND},
        {216, 232, 248, BACKGROUND},
        {208, 224, 240, BACKGROUND},
        {240, 248, 248, BACKGROUND},
        {184, 200, 240, BACKGROUND},
        {216, 232, 240, BACKGROUND},
        {168, 208, 224, BACKGROUND},
        {176, 216, 248, BACKGROUND},
        {208, 232, 248, BACKGROUND},
        {112, 160, 176, BACKGROUND},
        {8, 16, 56, GROUND},
        {24, 40, 96, GROUND},
        {48, 104, 16, GROUND},
        {192, 248, 8, GROUND},
        {24, 48, 11, GROUND},
        {64, 40, 24, HILLS},
        {48, 32, 16, HILLS},
        {144, 112, 80, HILLS},
        {104, 72, 48, HILLS},
        {120, 88, 64, HILLS},
        {160, 120, 88, HILLS},
        {88, 56, 40, HILLS},
        {72, 48, 32, HILLS},
        {48, 16, 8, SLING},
        {80, 40, 8, SLING},
        {120, 64, 32, SLING},
        {160, 112, 48, SLING},
        {200, 144, 88, SLING},
        {152, 88, 40, SLING},
        {176, 128, 64, SLING},
        {168, 112, 64, SLING},
        {208, 0, 40, RED_BIRD},
        {240, 216, 32, YELLOW_BIRD},
        {232, 176, 0, YELLOW_BIRD},
        {96, 168, 192, BLUE_BIRD},
        {80, 144, 168, BLUE_BIRD},
        {232, 232, 200, WHITE_BIRD},
        {248, 184, 72, WHITE_BIRD},
        {104, 224, 72, PIG},
        {160, 232, 0, PIG},
        {88, 176, 32, PIG},
        {110, 176, 12, PIG},
        {56, 104, 8, PIG},
        {88, 192, 240, ICE},
        {104, 192, 240, ICE},
        {120, 200, 240, ICE},
        {192, 240, 248, ICE},
        {144, 216, 240, ICE},
        {136, 208, 248, ICE},
        {128, 208, 248, ICE},
        {168, 224, 248, ICE},
        {69, 163, 187, ICE},
        {40, 160, 224, ICE},
        {176, 224, 248, ICE},
        {224, 144, 32, WOOD},
        {248, 184, 96, WOOD},
        {192, 112, 32, WOOD},
        {176, 88, 32, WOOD},
        {168, 96, 8, WOOD},
        {232, 160, 72, WOOD},
        {248, 192, 96, WOOD},
        {248, 176, 72, WOOD},
        {156, 112, 83, WOOD},
        {168, 88, 32, WOOD},
        {216, 146, 64, BACKGROUND},
        {80, 152, 8, BACKGROUND},
        {48, 56, 16, BACKGROUND},
        {40, 40, 8, BACKGROUND},
        {162, 126, 105, BACKGROUND},
        {168, 147, 139, BACKGROUND},
        {160, 112, 72, BACKGROUND},
        {152, 80, 24, BACKGROUND},
        {112, 112, 112, BACKGROUND},
        {160, 88, 24, BACKGROUND},
        {184, 160, 144, BACKGROUND},
        {136, 64, 16, BACKGROUND},
        {64, 176, 40, BACKGROUND},
        {184, 120, 48, BACKGROUND},
        {80, 80, 80, BACKGROUND},
        {0, 0, 0, BACKGROUND},
        {127, 138, 177, BACKGROUND},
        {81, 190, 232, BACKGROUND},
        {40, 160, 232, BACKGROUND},
        {248, 224, 80, DUCK},
        {248, 208, 32, DUCK},
        {248, 136, 32, DUCK},
        {240, 176, 16, DUCK},
        {128, 168, 24, WATERMELON},
        {88, 112, 16, WATERMELON},
        {56, 88, 16, WATERMELON}
    };
    
    private static int MIN_SIZE[];
    private static int MAX_SIZE[];
    
    // table mapping each 15 bit color code to a type
    private static int _assignedType[];
    
    // drawing color for different objects
    public static int _drawColor[];
    public static Color _colors[];
    private static boolean _firstTime = true;
    
    // edge detection thresholds
    private static final int EDGE_THRESHOLD1 = 300;
    private static final int EDGE_THRESHOLD2 = 125;
    
    // weights for edge strength calculation and maximum single response
    private static final int wh = 400;
    private static final int ws = 12;
    private static final int wv = 7;
    private static final int EDGE_BOUND = 180;
    
    private static final int NEIGHBOURS[][] = {
        {0, -1, 0, 1, 0, 0},  // horizontal neighbours
        {1, -1, -1, 1, 0, 0}, // 45 degress
        {-1, 0, 1, 0, 0, 0},  // vertical
        {-1, -1, 1, 1, 0, 0}  // 135 degrees
    };
       
    // dimension of the image
    private int _width;
    private int _height;
    
    // ground level
    private int _groundLevel = 0;
    
    // the labelled image using integer colours, indexed as [y][x]
    private int [][] _class = null;
    private boolean [][] _edges = null;
    
    // the compressed image
    public int [][] _image = null;
    private int [][] _val = null;
    private int [][] _hue = null;
    private int [][] _sat = null;
    
        
    // connected components in the scene
    private ArrayList<ConnectedComponent> _components = null;
    
    /* Build a segmentation from the given screenshot
     * @param   screenshot of the game
     */
    public ImageSegmenter(BufferedImage screenshot)
    {
        // if the structure builder is run for the first time
        if (_firstTime)
        {
            initialise();
            _firstTime = false;
        }
        
        // parse the screenshot
        _width = screenshot.getWidth();
        _height = screenshot.getHeight();
        _image = compressImage(screenshot);
                
        _hue = new int[_height][_width];
        _sat = new int[_height][_width];
        _val = new int[_height][_width];
        for (int y = 0; y < _height; y++)
        for (int x = 0; x < _width; x++)
        {
            int color = screenshot.getRGB(x, y);
            int r = (color >> 16) & 0xff;
            int g = (color >> 8) & 0xff;
            int b = color & 0xff;
            
            _hue[y][x] = getHue(r, g, b);
            _sat[y][x] = getSaturation(r, g, b);
            _val[y][x] = getValue(r, g, b);
        }
        classifyPixels();
        findGroundLevel();
    }
    
    /* Assign a class label to every point in the screenshot
     */
    private void classifyPixels()
    {
        _class = new int[_height][_width];
        
        for (int y = 0; y < _height; y++)
        for (int x = 0; x < _width; x++)
            _class[y][x] = _assignedType[_image[y][x]];
    }
    
    /* find edges in the image, using the custom edge detector
     * @return  boolean map in the form isEdge[y][x], where isEdge[y][x]
     *          means point (x, y) is an edge
     */
    private boolean[][] findEdges()
    {
        int G[][][] = new int[_height][_width][4];
        int G1[][][] = new int[_height][_width][4];
        int G2[][][] = new int[_height][_width][4];
        boolean isEdge[][][] = new boolean[_height][_width][4];
       
        // calculate individual edge strength in each direction
        for (int y = _height - 2; y > 0; y--)
	        for (int x = 1; x < _width - 1; x++)
	        {
	            for (int o = 0; o < 4; o++)
	            {
	                int x2 = x + NEIGHBOURS[o][0];
	                int y2 = y + NEIGHBOURS[o][1];
	                int x3 = x + NEIGHBOURS[o][2];
	                int y3 = y + NEIGHBOURS[o][3];
	                
	                G[y][x][o] = distance(x, y, x2, y2) + distance(x, y, x3, y3);
	            }
	            G[y][x][0] *= 1.5;
	            G[y][x][2] *= 1.5;
	        }
        
        // cross-correlate with neighbouring points
        for (int y = _height - 3; y > 1; y--)
        for (int x = 2; x < _width - 2; x++)
        {
            for (int o = 0; o < 4; o++)
            {    
                int o2 = (o + 2) % 4;
                int x2 = x + NEIGHBOURS[o2][0];
                int y2 = y + NEIGHBOURS[o2][1];
                int x3 = x + NEIGHBOURS[o2][2];
                int y3 = y + NEIGHBOURS[o2][3];
                
                G1[y][x][o] = (G[y][x][o] + G[y2][x2][o] + G[y3][x3][o]) / 3;
            }
        } 
        
        // apply non-maximum suppression for each direction
        for (int y = _height-3; y > 1; y--)
        	for (int x = 2; x < _width-2; x++)
        	{
	            for (int o = 0; o < 4; o++)
	            {
	                G2[y][x][o] = G1[y][x][o];
	                   
	                int x1 = x + NEIGHBOURS[o][0];
	                int y1 = y + NEIGHBOURS[o][1];
	                int x2 = x + NEIGHBOURS[o][2];
	                int y2 = y + NEIGHBOURS[o][3];
	                
	                if (G1[y][x][o] <= G1[y1][x1][o] || G1[y][x][o] < G1[y2][x2][o])
	                    G2[y][x][o] = 0;
	            }
        }
        
        // Trace edge using two thresholds       
        for (int y = _height - 3; y > 1; y--)
        for (int x = 2; x < _width - 2; x++)
        {
            // add pixel if gradient is greater than threshold1
            for (int o = 0; o < 4; o++)                   
            if (G2[y][x][o] > EDGE_THRESHOLD1 && !isEdge[y][x][o])
            {
                isEdge[y][x][o] = true;
                
                // perform BFS for edge pixels
                Queue<Point> q = new LinkedList<Point>();
                q.add(new Point(x, y));
                
                while (!q.isEmpty())
                {
                    Point p = q.poll();
                    
                    for (int i = -1; i < 2; i++)
                    for (int j = -1; j < 2; j++)
                    {
                        if (i == 0 && j == 0)
                            continue;
                                
                        int ny = p.y + i;
                        int nx = p.x + j;
                        
                        // if the gradient is greater than threshold2
                        if (G2[ny][nx][o] > EDGE_THRESHOLD2 && !isEdge[ny][nx][o])
                        {
                            isEdge[ny][nx][o] = true;
                            q.add(new Point(nx, ny));
                        }
                    }
                }
            }
        }
        
        // combine edge in all four directions
        boolean ret[][] = new boolean[_height][_width];
        for (int y = _height - 3; y > 1; y--)
        for (int x = 2; x < _width - 2; x++)
        {
            if (isEdge[y][x][0] || isEdge[y][x][1] ||
                isEdge[y][x][2] || isEdge[y][x][3])
                ret[y][x] = true;
        }
                
        return ret;
    }
    
    /* find all connected components in the game
     */
    public ArrayList<ConnectedComponent> findComponents()
    {        
        // find edges and add to the class map        
        _edges = findEdges();
        for (int y = _groundLevel-1; y > 0; y--)
        for (int x = 0; x < _width; x++)
        {
            if (!(_class[y][x] >= ICE && _class[y][x] <= STONE))
                continue;
            
            if (_edges[y][x])
                _class[y][x] = EDGE;
        }
        
        // search for connected components
        _components = new ArrayList<ConnectedComponent>();
        boolean searched[][] = new boolean[_height][_width];
        
        for (int x = 50; x < _width - 50; x++)
        for (int y = _groundLevel - 1; y > _height * 0.2; y--)
        {
            int cls = _class[y][x];
            if (!searched[y][x] && cls > GROUND && cls < EDGE)
            {
                ConnectedComponent cc;
                
                // use 8-connect for birds and sling, 4-connect otherwise
                if (cls >= SLING && cls <= BLACK_BIRD)
                    cc = new ConnectedComponent(_class, x, y, searched, true);
                else
                    cc = new ConnectedComponent(_class, x, y, searched, false);
                
                // verify component has the correct size
                if (cc.getArea() >= MIN_SIZE[cls] && cc.getArea() <= MAX_SIZE[cls])
                    _components.add(cc);
            }  
        }
        //_edges = null;
        return _components;
    }

    /* find all connected components with type trajectory */
    public ArrayList<ConnectedComponent> findTrajectory()
    {
        ArrayList<ConnectedComponent> traj = new ArrayList<ConnectedComponent>();
        
        boolean searched[][] = new boolean[_height][_width];
        for (int x = 50; x < _width - 50; x++)
        for (int y = _groundLevel - 1; y > _height * 0.1; y--)
        {
            int cls = _class[y][x];
            if (!searched[y][x] && cls == TRAJECTORY)
            {
                ConnectedComponent cc;
                cc = new ConnectedComponent(_class, x, y, searched, false);
                    
                if (cc.getArea() >= MIN_SIZE[TRAJECTORY] && cc.getArea() <= MAX_SIZE[TRAJECTORY])
                    traj.add(cc);
            }  
        }
        return traj;
    }
    
    /* find the ground level */
    public int findGroundLevel()
    {
        if (_groundLevel != 0)
            return _groundLevel;
        
        for (int y = _height-1; y > 0; y--)
        {
            int counter = 0;
            for (int x = 0; x < _width; x++)
            {
                if (_class[y][x] == GROUND)
                    counter++;
            }
            if (counter < _width * 0.8)
            {
                _groundLevel = y;
                break;
            }
        }
        return _groundLevel;
    }
    
    /* draw all found components
     * @param   canvas to draw onto
     *          if the corners should be indicated
     */
    public void drawComponents(BufferedImage canvas, boolean drawCorner)
    {
        if (_components == null)
            findComponents();
            
        BufferedImage image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < _width; x++)
        for (int y = 0; y < _height; y++)
            image.setRGB(x, y, 0xffffff);
        
        // draw connected components      
        for (ConnectedComponent cc : _components)
            cc.draw(image, true, drawCorner);
        
        canvas.createGraphics().drawImage(image, 0, 0, null);
    }
    
    // draw the segmentation onto canvas
    public void drawClassification(BufferedImage canvas)
    {
        BufferedImage image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < _height; y++)
        {
            for (int x = 0; x < _width; x++)
            {
                int c = _class[y][x];
                image.setRGB(x, y, _drawColor[c]);
            }
        }
        Graphics2D g = canvas.createGraphics();
        g.drawImage(image, 0, 0, null);
    }
    
    // draw the edge image onto canvas
    public void drawEdges(BufferedImage canvas)
    {
        BufferedImage image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
        
        _edges = findEdges();
        for (int y = 0; y < _height; y++)
        {
            for (int x = 0; x < _width; x++)
            {
                if (_edges[y][x])
                    image.setRGB(x, y, 0x000000);
                else
                    image.setRGB(x, y, 0xffffff);
            }
        }
        Graphics2D g = canvas.createGraphics();
        g.drawImage(image, 0, 0, null);
    }
    
    // draw the compressed image
    public void drawImage(BufferedImage canvas)
    {
        BufferedImage image = decompressImage(_image);
        Graphics2D g = canvas.createGraphics();
        g.drawImage(image, 0, 0, null);
    }
    
     /* compress the given image to 15 bit per pixel
      * @param   image to be compressed (type RGB)
      * @return  compressed bits (indexed as [y][x])
      */
    public static int[][] compressImage(BufferedImage image)
    {
        int height = image.getHeight();
        int width = image.getWidth();
        
        int ret[][] = new int[height][width];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                // decode the integer color to and compress it to 15 bits
                int color = image.getRGB(x, y);
                int r = (color >> 19) & 31;
                int g = (color >> 11) & 31;
                int b = (color >> 3) & 31;
                
                ret[y][x] = (r << 10) | (g << 5) | b;
            }
        }
        return ret;
    }
    
    /* reconstruct the image from 15 bit compression
     * @param   compressed image (15 bit integer array indexed as [y][x])
     * @return  reconstructed BufferedImage object
     */
    public static BufferedImage decompressImage(int map[][])
    {
        int height = map.length;
        int width = map[0].length;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                int c = map[y][x];
                int r = (c >> 10) & 31;
                int g = (c >> 5) & 31;
                int b = c & 31;
                int color = (r << 19) | (g << 11) | (b << 3);
                image.setRGB(x, y, color);
            }
        }
        return image;
    }
    
    /* - Calculate the corresponding class for every possible 15-bit color
     * - Initialise the drawing color for each types
     * - Set minimum and maximum size of each type of object
     */
    public static void initialise()
    {
        // initialise drawing colors
        _drawColor = new int[CLASS_COUNT];
        _colors = new Color[CLASS_COUNT];
        _drawColor[BACKGROUND] = 0xdddddd;
        _drawColor[GROUND] = 0x152053;
        _drawColor[HILLS] = 0x342213;
        _drawColor[SLING] = 0x7f4120;
        _drawColor[EDGE] = 0x000000;
        _drawColor[STONE] = 0xa0a0a0;
        _drawColor[ICE] = 0x6ecdf8;
        _drawColor[WOOD] = 0xe09020;
        _drawColor[PIG] = 0x60e048;
        _drawColor[TRAJECTORY] = 0xffffff;
        _drawColor[BLUE_BIRD] = 0x60a8c0;
        _drawColor[RED_BIRD] = 0xd00028;
        _drawColor[YELLOW_BIRD] = 0xf0d820;
        _drawColor[BLACK_BIRD] = 0x0f0f0f;
        _drawColor[WHITE_BIRD] = 0xe8e8c8;
        _drawColor[DUCK] = 0xf0d820;
        _drawColor[WATERMELON] = 0x80a818;
        
        for (int i = 0; i < CLASS_COUNT; i++)
            _colors[i] = new Color(_drawColor[i]);
        
        // initialse minimum sizes
        MIN_SIZE = new int[CLASS_COUNT];
        MAX_SIZE = new int[CLASS_COUNT];
        for (int i = 0; i < CLASS_COUNT; i++)
        {
            MIN_SIZE[i] = 15;
            MAX_SIZE[i] = 4000;
        }
        MIN_SIZE[PIG] = 30;
        MIN_SIZE[HILLS] = 250;
        MAX_SIZE[HILLS] = 1000000;
        MIN_SIZE[SLING] = 150;
        MIN_SIZE[BLUE_BIRD] = 2;
        MAX_SIZE[BLUE_BIRD] = 30;
        MIN_SIZE[RED_BIRD] = 20;
        MIN_SIZE[YELLOW_BIRD] = 20;
        MIN_SIZE[BLACK_BIRD] = 20;
        MIN_SIZE[TRAJECTORY] = 1;
        MAX_SIZE[TRAJECTORY] = 60;
            
        _assignedType = new int[(1 << 15)];
        for (int color = 0; color < (1 << 15); color++)
        {            
            _assignedType[color] = assignType(color);
        }
    }
    
    /* Assign the 15-bit color to the most likely type
     * using nearest neighbour in the training data
     * @param   color - color code of the pixel
     * @return  the assigned type
     */
    private static int assignType(int color)
    {
        int type = BACKGROUND;
        int minDist = 999999;
      
        
        // extract the r,g,b components
        int r = color >> 10;
        int g = (color >> 5) & 31;
        int b = color & 31;
        
        // convert to 8 bit per component
        r = r << 3;
        g = g << 3;
        b = b << 3;
        
        // special cases where pixel is grayscale
        if (r == g && r == b)
        {
            if (r >= 88 && r <= 208)
                return STONE;
            if (r > 232)
                return TRAJECTORY;
            if (r == 64)
                return BLACK_BIRD;
        }
     
        for (int i = 0; i < _trainData.length; i++)
        {
            // calculate squared distance between the color and training data
            int d1 = r - _trainData[i][0];
            int d2 = g - _trainData[i][1];
            int d3 = b - _trainData[i][2];
            int dist = d1 * d1 + d2 * d2 + d3 * d3;
            
            // assign pixel to the types with shortest distance
            if (dist < minDist && dist < MAX_DIST*MAX_DIST)
            {
                minDist = dist;
                type = _trainData[i][3];
            }
        }
        return type;
    }
    
    private static final double ROOT3 = Math.sqrt(3);
    
    // calculate hue from the r, g, b color
    public static int getHue(int r, int g, int b)
    {
        double alpha = 2 * r - g - b;
        double beta = ROOT3 * (g - b);
                
        int hue = (int) Math.toDegrees(Math.atan2(beta, alpha));
        if (hue < 0)
            hue += 360;
        return hue;
    }
    
    // calculate saturation from the r, g, b color
    public static int getSaturation(int r, int g, int b)
    {                
        int V = Math.max(Math.max(r, g), b);
        int C = V - Math.min(Math.min(r, g), b);
        
        if (V != 0)
            return C * 100 / V;
        else
            return 0;
    }
    
    // calculate intensity from the r, g, b color
    public static int getValue(int r, int g, int b)
    {
        return 100 * (r + g + b) / 768;
    }
    
    
    /* custom defined distance metric
     * @param   coordinates of the two point to compare
     * @return  weighted distance between color of the points in HSV space
     */
    private int distance(int x1, int y1, int x2, int y2)
    {
        int d;
        
        // if hue is undefined
        if (_hue[y1][x1] == 0 && _hue[y2][x2] == 0)
        {
            int dv = _val[y1][x1] - _val[y2][x2];
            d = wv * Math.abs(dv);
        }
        else
        {
            int ds = _sat[y1][x1] - _sat[y2][x2];
            int dh = _hue[y1][x1] - _hue[y2][x2];
            d = (int) Math.sqrt(wh*dh*dh + ws*ds*ds);
        }
            
        return d > EDGE_BOUND ? EDGE_BOUND : d;
    }
    
    

}
