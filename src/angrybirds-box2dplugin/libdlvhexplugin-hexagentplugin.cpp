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
#include <dlvhex2/ComfortPluginInterface.h>
#include "config.h"
#include <Box2D/Box2D.h>
#include <fstream>
#include <algorithm>

using namespace dlvhex;

enum Type { ice, wood, stone, pig, ground, tnt };


struct Sling
{
	float x;
	float y;
	float w;
	float h;
};

struct Object
{
    int id;
    float cx;
    float cy;
    float w;
    float h;
    float angle;

    Type type;

    Object()
    {
	    id = -1;
	    cx = 0;
	    cy = 0;
	    w = 0;
	    h = 0;
	    angle = 0;
    }

    b2Vec2 getCentroid() const
    {
        b2Vec2 v;
        v.Set(cx, cy);

        return v;
    }
    b2Vec2 getLeftFaceCenter() const
    {
        b2Vec2 v;
        v.Set(cx-w/2, cy);
        return v;
    }
    b2Vec2 getTopFaceCenter() const
    {
        b2Vec2 v;
        v.Set(cx, cy-h/2);
        return v;
    }
    b2Vec2 getRightFaceCenter() const
        {
            b2Vec2 v;
            v.Set(cx+w/2, cy);
            return v;
        }
    b2Vec2 getBottomFaceCenter() const
        {
			b2Vec2 v;
			v.Set(cx, cy+h/2);
			return v;
        }



    void scale(float factor)
    {
        cx = cx/factor;
        cy = cy/factor;
        w = w/factor;
        h = h/factor;
    }

    float getMass()
    {
        float area = w*h;

        switch (type)
        {
        case pig :
            return PIG_DENSITY * area;
        case ice :
            return ICE_DENSITY * area;
        case wood :
            return WOOD_DENSITY * area;
        case stone :
            return STONE_DENSITY * area;
        case tnt :
            return TNT_DENSITY * area;
        default:
            return 0;
        }
    }

    static Object findById(const std::vector<Object> & objects, int id)
    {
	for(std::vector<Object>::const_iterator i = objects.begin(); i != objects.end(); ++i)
		if ((*i).id == id)
			return (*i);

	Object ret;
	ret.id = -1;
	return ret;
    }

    static void removeById(std::vector<Object> & objects, int id)
    {
	for(std::vector<Object>::iterator i = objects.begin(); i != objects.end(); ++i)
		if ((*i).id == id)
		{
			objects.erase(i);
			return;
		}
    }
};

class Calculation
{
public:

    static void fillVector(const ComfortInterpretation & i, std::vector<Object> & objects)
    {
        for (ComfortInterpretation::iterator c = i.begin(); c != i.end(); ++c)
        {
            Object o;

            o.id = c->getArgument(1).intval;

            if (c->getArgument(2).getUnquotedString() == "ice")
                o.type = ice;
            else if (c->getArgument(2).getUnquotedString() == "wood")
                o.type = wood;
            else if (c->getArgument(2).getUnquotedString() == "stone")
                o.type = stone;
            else if (c->getArgument(2).getUnquotedString() == "pig")
                o.type = pig;
            else if (c->getArgument(2).getUnquotedString() == "ground")
                o.type = ground;
            else if (c->getArgument(2).getUnquotedString() == "tnt")
                o.type = tnt;
            else
            {
                std::cerr << "Unknown object type \"" << c->getArgument(2).getUnquotedString() <<
                          "\".";
                assert(true);
            }

            o.cx = c->getArgument(3).intval;
            o.cy = c->getArgument(4).intval;
            o.w = c->getArgument(5).intval;
            o.h = c->getArgument(6).intval;
            o.angle = std::atof(c->getArgument(7).getUnquotedString().c_str());

            objects.push_back(o);
        }
    }
};

#define BOUND 0.1
#define _scaleFactor 1.005
#define _angleAdjust 0.0

class TrajectoryPlanner
{
    static double x_offset;
    static double y_offset;

    static double _launchAngle [];
    static double _changeAngle [];
    static double _launchVelocity[];

    /*
    static double BOUND // = 0.1;
    // small modification to the scale and angle
    static double _scaleFactor // = 1.005;
    static double _angleAdjust // = 0.0;
 */

    // return scene scale determined by the sling size
    static double getSceneScale(Sling sling)
    {
        return sling.h + sling.w;
    }

    // find the reference point given the sling
    static b2Vec2 getReferencePoint(Sling sling)
    {
        return b2Vec2((int)(sling.x + x_offset * sling.w), (int)(sling.y + y_offset * sling.w));
    }

    static double getVelocity(double theta)
    {
        if (theta < _launchAngle[0])    
            return _scaleFactor * _launchVelocity[0];
        
        for (int i = 1; i < 13; i++)
        {
            if (theta < _launchAngle[i])
                return _scaleFactor * _launchVelocity[i-1];
        }
        
        return _scaleFactor * _launchVelocity[13 /*_launchVelocity.length*/-1];
    }

    // get the velocity for the desired angle
    static double getVelocity(double _velocity, double theta)
    {
        if (theta < _launchAngle[0])
            return _velocity * _launchVelocity[0];

        for (int i = 1; i < 13 /*_launchAngle.length*/; i++)
        {
            if (theta < _launchAngle[i])
                return _velocity * _launchVelocity[i-1];
        }

        return _velocity * _launchVelocity[13 /*_launchVelocity.length*/-1];
    }

    // take the initial angle of the desired trajectory and return the launch angle required
    static double actualToLaunch(double theta)
    {
        for (int i = 1; i < 13 /*_launchAngle.length*/; i++)
        {
            if (theta > _launchAngle[i-1] && theta < _launchAngle[i])
                return theta + _changeAngle[i-1];
        }
        return theta + _changeAngle[13/*_launchAngle.length*/-1];
    }

    // take the launch angle and return the actual angle of the resulting trajectory
    static double launchToActual(double theta)
    {
        for (int i = 1; i < 13 /*_launchAngle.length*/; i++)
        {
            if (theta > _launchAngle[i-1] && theta < _launchAngle[i])
                return theta - _changeAngle[i-1];
        }
        return theta - _changeAngle[/*_launchAngle.length*/-1];
    }

    // find the release point given the sling and launch angle
    //      theta - the launch angle in radians (positive means up)
    static b2Vec2 findReleasePoint(Sling sling, double theta)
    {
        double mag = sling.h * 10;
        b2Vec2 ref = getReferencePoint(sling);
        b2Vec2 release((int)(ref.x - mag * std::cos(theta)), (int)(ref.y + mag * std::sin(theta)));

        return release;
    }

	public:
    
    // predicts a trajectory
    static std::vector<b2Vec2> predictTrajectory(Sling slingshot, b2Vec2 launchPoint, int x_max, double _velocity)
    {

        // get slingshot reference point
        b2Vec2 slingLocation = getReferencePoint(slingshot);

        // launch vector
        b2Vec2 launchVector(slingLocation.x - launchPoint.x, launchPoint.y - slingLocation.y);
        if (launchVector.x < slingshot.w)
        {
            return std::vector<b2Vec2>();
        }

        // estimate scene scale
        double sceneScale = getSceneScale(slingshot);
        double theta = launchToActual(std::atan2(launchVector.y, launchVector.x));
        double velocity = getVelocity(_velocity, theta);

        //System.out.println("launch angle " + Math.toDegrees(theta));

        // initial velocities
        double u_x = velocity * std::cos(theta);
        double u_y = velocity * std::sin(theta);

        // the normalised coefficients
        double a = -0.5 / (u_x * u_x);
        double b = u_y / u_x;

        //System.out.println("plot trajectory: " + a + "x^2 + " + b + "x");

        std::vector<b2Vec2> trajectory;
        for (int x = 0; x < x_max; x++)
        {
            double xn = x / sceneScale;
            int y = slingLocation.y - (int)((a * xn * xn + b * xn) * sceneScale);
            trajectory.push_back(b2Vec2(x + slingLocation.x, y));
        }

        return trajectory;
    }

    static double fitAngle(double start, double end, double x, double y)
       {
       	double retAngle = 0;
       	double bestError = 100000;
           for (double theta = start; theta <= end; theta += 0.0001)
           {
               double velocity = getVelocity(theta);

               // initial velocities
               double u_x = velocity * std::cos(theta);
               double u_y = velocity * std::sin(theta);

               // the normalised coefficients
               double a = -0.5 / (u_x * u_x);
               double b = u_y / u_x;

               // the error in y-coordinate
               double error = std::abs(a*x*x + b*x - y);
               if (error < bestError)
               {
                   retAngle = theta;
                   bestError = error;
               }
           }
           return retAngle;
       }

       /* Estimate launch points given a desired target point using maximum velocity
        * If there are two launch point for the target, they are both returned in
        * the list {lower point, higher point)
        * Note - angles greater than 75 are not considered
        *
        * @param   slingshot - bounding rectangle of the slingshot
        *          targetPoint - coordinates of the target to hit
        * @return  A list containing 2 possible release points
        */
    static std::vector<b2Vec2> estimateLaunchPoint_new(const Sling& slingshot, const b2Vec2& targetPoint, double _velocity)

     {
           // calculate relative position of the target (normalized)
    	   _velocity++; // ;)
           double scale = getSceneScale(slingshot);
           b2Vec2 ref = getReferencePoint(slingshot);

           double x = (targetPoint.x - ref.x) / scale;
           double y = -(targetPoint.y - ref.y) / scale;
           double theta1 = 0;
           double theta2 = 0;

           // first estimate launch angle using the projectile equation (constant velocity)
           double v = _scaleFactor * _launchVelocity[6];
           double v2 = v * v;
           double v4 = v2 * v2;
           double tangent1 = (v2 - std::sqrt(std::abs(v4 - (x * x + 2 * y * v2)))) / x;
           double tangent2 = (v2 + std::sqrt(std::abs(v4 - (x * x + 2 * y * v2)))) / x;
           double t1 = actualToLaunch(std::atan(tangent1));
           double t2 = actualToLaunch(std::atan(tangent2));


           theta1 = fitAngle(t1-BOUND,t1+BOUND,x,y);
           theta2 = fitAngle(t2-BOUND,t2+BOUND,x,y);

           //System.out.println("FORMER THETAS: "+Math.toDegrees(theta1)+" "+Math.toDegrees(theta2));

           theta1 = actualToLaunch(theta1);
           theta2 = actualToLaunch(theta2);

           //System.out.println("NEW THETAS: " + Math.toDegrees(theta1) + ", " + Math.toDegrees(theta2));


           // add launch points to the list
            std::vector<b2Vec2> pts;
            pts.push_back(findReleasePoint(slingshot, theta1));


           // add the higher point if it is below 75 degrees and not same as first
           if (theta2 < (75)*(M_PI/180) && theta2 != theta1)
               pts.push_back(findReleasePoint(slingshot, theta2));

           return pts;
       }



    // estimate launch point given a desired target point
    // if there are two launch point for the target, they are both returned in
    // the list {lower point, higher point)
    // Note - angles greater than 80 are not considered due to their low initial velocity
    static std::vector<b2Vec2> estimateLaunchPoint(const Sling& slingshot, const b2Vec2& targetPoint, double _velocity)
    {
        // calculate relative position of the target (normalised)
        double scale = getSceneScale(slingshot);

        b2Vec2 ref = getReferencePoint(slingshot);

        double x = (targetPoint.x - ref.x) / scale;
        double y = -(targetPoint.y - ref.y) / scale;

        double bestError = 1000;
        double theta1 = 0;
        double theta2 = 0;

        // search tangents from -0.5 to 1.0
        //for (double tangent = -0.5; tangent < 1.0; tangent += 0.0001)
        for(double theta = -30*(M_PI/180); theta < 45*(M_PI/180); theta += 0.01*(M_PI/180) )
        {
            //double theta = std::atan(tangent);
            double velocity = getVelocity(_velocity, theta);

            // initial velocities
            double u_x = velocity * std::cos(theta);
            double u_y = velocity * std::sin(theta);

            // the normalised coefficients
            double a = -0.5 / (u_x * u_x);
            double b = u_y / u_x;

            // the error in y-coordinate
            double error = std::abs(a*x*x + b*x - y);
            if (error < bestError)
            {
                theta1 = theta;
                bestError = error;
            }
        }

        bestError = 1000;

        //for (double tangent = -1.0; tangent < 4.0; tangent += 0.0001)
        for(double theta = 45*(M_PI/180); theta < 75*(M_PI/180); theta += 0.01*(M_PI/180) )

        {
            // double theta = std::atan(tangent);
            double velocity = getVelocity(_velocity, theta);

            // initial velocities
            double u_x = velocity * std::cos(theta);
            double u_y = velocity * std::sin(theta);

            // the normalised coefficients
            double a = -0.5 / (u_x * u_x);
            double b = u_y / u_x;

            // the error in y-coordinate
            double error = std::abs(a*x*x + b*x - y);
            if (error < bestError)
            {
                theta2 = theta;
                bestError = error;
            }
        }

        theta1 = actualToLaunch(theta1);
        theta2 = actualToLaunch(theta2);

        //System.out.println("Two angles: " + Math.toDegrees(theta1) + ", " + Math.toDegrees(theta2));

        // add launch points to the list
        std::vector<b2Vec2> pts;
        pts.push_back(findReleasePoint(slingshot, theta1));

        // add the higher point if it is below 75 degrees and not same as first
        if (theta2 < 75 * (M_PI/180) && std::abs(theta1 - theta2) > 0.01)
            pts.push_back(findReleasePoint(slingshot, theta2));

        return pts;
    }
};

double TrajectoryPlanner::x_offset = 0.5;
double TrajectoryPlanner::y_offset = 0.65;
//                                             10,    15,    20,    25,    30,    35,    40,    45,    50,    55,    60,    65,    70
double TrajectoryPlanner::_launchAngle [] =   {0.13,  0.215, 0.296, 0.381, 0.476, 0.567, 0.657, 0.741, 0.832, 0.924, 1.014, 1.106, 1.197};
double TrajectoryPlanner::_changeAngle [] =   {0.052, 0.057, 0.063, 0.066, 0.056, 0.054, 0.050, 0.053, 0.052, 0.047, 0.043, 0.038, 0.034};
double TrajectoryPlanner::_launchVelocity[] = {2.9,   2.88,  2.866, 2.838, 2.810, 2.800, 2.790, 2.773, 2.753, 2.749, 2.744, 2.736, 2.728};


class WorldCreation
{
public:
    ///Creates a world. The objects are scaled wit scaleFactor, and if createAutoGround is specified, a ground plate is created below the objects. Each body has one fixture. createAutoGround specifies that we want to use automatically generated ground and not the one provided.
    static b2World createWorld(std::vector<Object> & objects, int scaleFactor, bool createAutoGround = false)
    {
        // Define the gravity vector.
        b2Vec2 gravity(0.0f, 9.8f);

        // Construct a world Object, which will hold and simulate the rigid bodies.
        b2World world(gravity);

        //First we add all the objects of the object list to the world, scaling them. At the end, we add the floor, based on the scaled objects.
        for(std::vector<Object>::iterator it = objects.begin(); it != objects.end(); ++it)
        {
            Object* o = &(*it);

	    if (createAutoGround && o->type == ground)
		    continue;

            o->scale(scaleFactor);

            // Define the dynamic body. We set its position and call the body factory.
            b2BodyDef bodyDef;
            bodyDef.type = b2_dynamicBody;
            bodyDef.position = o->getCentroid();
            b2Body* body = world.CreateBody(&bodyDef);

            // Define another box shape for our dynamic body.
            b2PolygonShape dynamicBox;
            dynamicBox.SetAsBox(o->w/2.0f, o->h/2.0f);

            // Define the dynamic body fixture.
            b2FixtureDef fixtureDef;
            fixtureDef.shape = &dynamicBox;

            // Set the box density to be non-zero, so it will be dynamic.
            fixtureDef.density = 1.0f;
            fixtureDef.restitution = 0;

            // Add the shape to the body.
            body->CreateFixture(&fixtureDef);

	    // Change the rotation
	    body->SetTransform(body->GetPosition(), o->angle);

            //The back link from body to object.
	    if (o->type != ground)
		    body->SetUserData(&(o->id));
        }

	if (createAutoGround)
	{
		// Define the ground body.
		b2BodyDef groundBodyDef;
		groundBodyDef.position.Set(0.0f, maxYPlusH(world));
		b2Body* groundBody = world.CreateBody(&groundBodyDef);
		groundBody->SetUserData(NULL);
		b2PolygonShape groundBox;
		groundBox.SetAsBox(1000.0f, 0.0f);
		groundBody->CreateFixture(&groundBox, 1.0f);
	}

        return world;
    }

    static int GetId(const b2Body* body)
    {
	    if (body->GetUserData() == NULL)
		    return -1;

	    return *(int*)body->GetUserData();
    }

    static float maxXPlusW(const b2World& world)
    {
        float m = 0;

	for (const b2Body * b = world.GetBodyList(); b != NULL; b = b->GetNext())
		for (const b2Fixture * f = b->GetFixtureList(); f != NULL; f = f->GetNext())
			m = std::max(f->GetAABB(0).upperBound.x, m);

        return m;
    }

private:

    static float maxYPlusH(const b2World& world)
    {
        float m = 0;

	for (const b2Body * b = world.GetBodyList(); b != NULL; b = b->GetNext())
		for (const b2Fixture * f = b->GetFixtureList(); f != NULL; f = f->GetNext())
			m = std::max(f->GetAABB(0).upperBound.y, m);

        return m;
    }
};




class DebugImage 
{
	private:
		std::ofstream file;
	public:
		DebugImage(const char* filename)
		{
			file.open(filename);

			file << "<?xml version=\"1.0\" standalone=\"no\"?>" << std::endl <<
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"" << std::endl <<
				"\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">" << std::endl <<
				std::endl <<
				"<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">" << std::endl;
		
			file << "<defs>" <<
				"<marker " <<
				"refX=\"0\" " <<
				"refY=\"0\" " <<
				"orient=\"auto\" " << 
				"id=\"Arrow2Lend\" " <<
				"style=\"overflow:visible\"> " <<
				"<path " <<
				"d=\"M 8.7185878,4.0337352 -2.2072895,0.01601326 8.7185884,-4.0017078 c -1.7454984,2.3720609 -1.7354408,5.6174519 -6e-7,8.035443 z\" " <<
				"transform=\"matrix(-1.1,0,0,-1.1,-1.1,0)\" " <<
				"style=\"fill-rule:evenodd;stroke-width:0.625;stroke-linejoin:round\" />" <<
				"</marker>" << 
				"</defs>";
		}

		void Save()
		{
			file << "</svg>" << std::endl;

			file.close();
		}

		void DrawWorld(const b2World& world, int highlightId, int scaleFactor = 1)
		{

			for (const b2Body * b = world.GetBodyList(); b != NULL; b = b->GetNext())
				for (const b2Fixture * f = b->GetFixtureList(); f != NULL; f = f->GetNext())
				{
					b2PolygonShape* s = (b2PolygonShape *)f->GetShape();
					std::vector<b2Vec2> vertices;
					for (int i = 0; i < s->GetVertexCount(); ++i)
						vertices.push_back(b->GetWorldPoint(s->GetVertex(i)));
					vertices.push_back(b->GetWorldPoint(s->GetVertex(0)));

					int id = WorldCreation::GetId(b);

					if (id != -1)
					{
						DrawPath(vertices, false, id == highlightId ? "#FF0000" : "none", scaleFactor);

						//Label
						b2Vec2 centroid = b->GetWorldCenter();
						file << "<text x=\"" << centroid.x * scaleFactor << "\" y=\"" << centroid.y * scaleFactor << "\" " <<
							"style=\"font-size:7px;fill:#0000FF;font-weight=bold\">" << id << "</text>" << std::endl;
					}
					else
						DrawPath(vertices, false, "fill", scaleFactor);
				}
		}

		void DrawPath(std::vector<b2Vec2> path, const bool& drawArrow = false, std::string fill = "none", const int& scaleFactor = 1, const std::string& color = "#000000")
		{
			file << "<path d=\"M ";
			for(std::vector<b2Vec2>::iterator i = path.begin(); i != path.end(); ++i)
				file << i->x * scaleFactor << "," << i->y * scaleFactor << " ";

			if (drawArrow)
				file << "m -1,-1 2,0 0,2";

			file << "\" style=\"fill:" << fill << ";stroke:" << color << ";stroke-width:1px;stroke-opacity:1";
			file << "\" />" << std::endl;
		}

		template <class T>
		void DrawText(const T& text)
		{
			file << "<text x=\"20\" y=\"20\" " <<
				"style=\"font-size:15px;fill:#0000FF;font-weight=bold\">" << text << "</text>" << std::endl;
		}

		template <class T>
		void DrawVector(const std::vector<T>& vector)
		{
			std::stringstream s;

			s << "{";

			for (typename std::vector<T>::const_iterator i =  vector.begin(); i != vector.end(); ++i)
			{
				if (i != vector.begin())
					s << "; ";

				s << *i;
			}

			s << "}";

			DrawText<std::string>(s.str());
		}

		void DrawGraph(const std::vector<std::pair<int,int> >& graph, const b2World& world, const int& scaleFactor)
		{
			for(std::vector<std::pair<int,int> >::const_iterator i = graph.begin(); i != graph.end(); ++i)
			{
				std::vector<b2Vec2> line;

				line.push_back(getCentroidForId(world,(*i).first));
				line.push_back(getCentroidForId(world,(*i).second));

				DrawPath(line, true, "none", scaleFactor, "#FF0000");
			}
		}

	private:

		static b2Vec2 getCentroidForId(const b2World& world, const int& id)
		{
			for (const b2Body * b = world.GetBodyList(); b != NULL; b = b->GetNext())
				if (WorldCreation::GetId(b) == id)
					return b->GetWorldCenter();

			return b2Vec2_zero;
		}
};

std::map<long,std::vector<std::pair<int,int> > > onTopCache;

class OnTop
{
public:

    static std::vector<std::pair<int,int> > calculateCached(std::vector<Object> & objects)
    {
        if (objects.size() <= 1)
            return std::vector<std::pair<int,int> >();

        long hash = calculateHash(objects);

        //Cache miss
        if (onTopCache.find(hash) == onTopCache.end())
            onTopCache[hash] = calculate(objects);

        return onTopCache[hash];
    }

private:

    static std::vector<std::pair<int,int> > calculate(std::vector<Object> & objects)
    {
        b2World world = WorldCreation::createWorld(objects, SCALE_FACTOR, true);

	if (debug)
	{
		DebugImage before("ontop-before.svg");
		before.DrawWorld(world, -1, SCALE_FACTOR);
		before.Save();
	}

        //Now we run the simulation until everything has settled. Box2D sets bodys asleep after they have been idle for a short amount of time. We loop the simulation until all objects have fallen asleep.
        bool allAsleep = false;
        while (!allAsleep)
        {
            world.Step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

            allAsleep = true;

            for(b2Body * b = world.GetBodyList(); b != NULL; b = b->GetNext())
            {
                //After each step, we set the speed of the objects to zero, because otherwise they would gain a lot of speed, crash in the ground and explode a little, thereby destroying a lot of similarity between original objects (in the game world) and the objects used for calculations. We want the objects to settle very slow.
                b->SetLinearVelocity(b2Vec2_zero);
                b->SetAngularVelocity(0);

                if (b->IsAwake() && b->GetType() == b2_dynamicBody)
                    allAsleep = false;
            }
        }

        //Contacts can only be calculated while objects are awake.
        world.SetAllowSleeping(false);
        //Final step to wake all objects.
        world.Step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

        std::vector<std::pair<int,int> > graph;

        //Now we can extract all the contacts from the world, filter them, and add them to our little graph.
        for (b2Contact* c = world.GetContactList(); c != NULL; c = c->GetNext())
        {
            //Filter contacts, that do not constitute a touch. Contacts are created as soon as two objects axis aligned bounding rectangles overlap. It is sometimes the case that the rectangles overlap while the objects are not touching (yet).
            if (!c->IsTouching())
                continue;

            //Extract the object ids.
	    int a = WorldCreation::GetId(c->GetFixtureA()->GetBody());
	    int b = WorldCreation::GetId(c->GetFixtureB()->GetBody());

            //If either of the objects is a static objects, which get their user data filled with NULL, we can filter the contact, because we don't reason about the floor.
            if (a == -1 || b == -1)
                continue;

	    //We need to use the world manifold, for the direction of the normal may be in some local coordinate system and therefore incorrect.
	    b2WorldManifold worldManifold;
	    c->GetWorldManifold(&worldManifold);

            //If the x part of the contact normal is not 0, the touch is not on top or bottom, but on the side. We do not consider these. Based on observations, even if one object is on top of a slightly inclined object, the contact normal will be straight up, so we can disregard any normals with x != 0 (>0.1 for floating point errors).
            if (worldManifold.normal.x > 0.1)
                continue;

            //The normal vector points from body a to body b, and we want the on_top_of(a,b)
            //relation to be true, if a is on top of b. If an object is nearer to the ground, and
            //therefore the bottom of the picture, it has a higher x value. Therefore the vector
            //a -> b needs to be positive, so that it points from the higher to the lower object.
            //Only then can we use on_top_of(a,b). Otherwise, the object b is actually on top of a.
            if (worldManifold.normal.y > 0)
                graph.push_back(std::pair<int,int>(a,b));
            else
                graph.push_back(std::pair<int,int>(b,a));

        }

	if (debug)
	{
		DebugImage after("ontop-after.svg");
		after.DrawWorld(world, -1, SCALE_FACTOR);
		after.DrawGraph(graph, world, SCALE_FACTOR);
		after.Save();
	}

        return graph;
    }


    static long calculateHash(std::vector<Object> & objects)
    {
        long hash = 0;

        for(std::vector<Object>::iterator i = objects.begin(); i != objects.end(); ++i)
        {
            hash += (*i).id;
            hash += (*i).cx;
            hash += (*i).cy;
            hash += (*i).w;
            hash += (*i).h;
            hash += (*i).angle;
        }

        return hash;
    }
};

class StabilityAtom : public ComfortPluginAtom
{
public:

    StabilityAtom() : ComfortPluginAtom("stability")
    {
        setOutputArity(1);
	addInputConstant(); //object w
	addInputConstant(); //object h
	addInputConstant(); //ojbect angle
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
	    Object o;
        o.w = ((ComfortTerm)(query.input[0])).intval;
        o.h = ((ComfortTerm)(query.input[1])).intval;
        o.angle = std::atof(((ComfortTerm)(query.input[2])).getUnquotedString().c_str());

	ComfortTuple ct;
	ct.push_back(ComfortTerm::createInteger(getStability(o)));
	answer.insert(ct);
    }

private:

	int getStability(const Object& o)
	{
		std::vector<Object> objects;
		objects.push_back(o);
		b2World world = WorldCreation::createWorld(objects, 1);
		
		b2AABB rect = world.GetBodyList()->GetFixtureList()->GetAABB(0);
		
		float width = rect.upperBound.x - rect.lowerBound.x;
		float height = rect.upperBound.y - rect.lowerBound.y;
		
		return std::min((int)((width / height)*50.0), 100);
	}
};

class ClearSkyAtom : public ComfortPluginAtom
{
public:

    ClearSkyAtom() : ComfortPluginAtom("clearsky")
    {
        setOutputArity(1);
	    addInputConstant(); //object id
        addInputPredicate(); //objects
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the object whose mass should be calculated.
        int oid = ((ComfortTerm)(query.input[0])).intval;

	std::vector<Object> objects;
	Calculation::fillVector(query.interpretation, objects);
	if (objects.empty())
		std::cerr << "&firstbelow: Called with no objects." << std::endl;

	Object object = Object::findById(objects, oid);
	if (object.id == -1)
	{
		std::cerr << "&firstbelow: Called with invalid target id " << oid << "." << std::endl;
		return;
	}

	b2Vec2 face = object.getTopFaceCenter();
	double width = object.w;
	int olx = object.getLeftFaceCenter().x;
	int orx = object.getRightFaceCenter().x;
    bool clearsky = true;
    const float OVERLAP_PERCENT = 0.7;
    if (debug)
    	std::cerr << "OBJ:" << object.id << " " << width << std::endl;
    for(std::vector<Object>::const_iterator i = objects.begin(); i != objects.end(); ++i)
    {
    	b2Vec2 bot = i->getBottomFaceCenter();
    	int lx = i->getLeftFaceCenter().x;
    	int rx = i->getRightFaceCenter().x;
    	if(debug)
    		std::cerr << "CK:" << i->id << "=" << lx << ":" << rx << ":" << olx << ":" << orx;
    	double overlap =    std::max(0,(-std::max(lx,olx)+std::min(rx,orx))) / width;
    	if (debug)
    	   std::cerr << " OVL:" << overlap << " " << OVERLAP_PERCENT << std::endl;
    	if (bot.y < face.y && overlap >= OVERLAP_PERCENT )
    	{
    		clearsky = false;
    		break;
    	}
    }

	if (clearsky)
	{
		ComfortTuple ct;
		ct.push_back(ComfortTerm::createInteger(1));
		answer.insert(ct);
	}
 }

};


class FirstBelowAtom : public ComfortPluginAtom
{
public:

    FirstBelowAtom() : ComfortPluginAtom("firstbelow")
    {
        setOutputArity(1);
	addInputConstant(); //object id
        addInputPredicate(); //objects
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the object whose mass should be calculated.
        int oid = ((ComfortTerm)(query.input[0])).intval;

	std::vector<Object> objects;
	Calculation::fillVector(query.interpretation, objects);
	if (objects.empty())
		std::cerr << "&firstbelow: Called with no objects." << std::endl;

	Object object = Object::findById(objects, oid);
	if (object.id == -1)
	{
		std::cerr << "&firstbelow: Called with invalid target id " << oid << "." << std::endl;
		return;
	}

	int firstaboveid = getFirstBelow(object, objects);

	if (firstaboveid != -1)
	{
		ComfortTuple ct;
		ct.push_back(ComfortTerm::createInteger(firstaboveid));
		answer.insert(ct);
	}
    }

private:
    class RCCB : public b2RayCastCallback
	{
		public:
			int firstAbove;

			RCCB()
			{
				firstAbove = -1;
			}

			virtual float32 ReportFixture(b2Fixture* fixture, const b2Vec2&, const b2Vec2&, float32 fraction)
			{
				firstAbove = WorldCreation::GetId(fixture->GetBody());
				return fraction;
			}
	};

    int getFirstBelow(const Object& target, std::vector<Object>& objects)
    {
	    //Now we create the world, scale factor 1 because we don't do any physics calculation anyways.
	    Object::removeById(objects, target.id);
	    b2World world = WorldCreation::createWorld(objects, 1);

	    b2Vec2 start = target.getCentroid();
	    b2Vec2 end(start.x,start.y+600);
	    RCCB cb;

	    world.RayCast(&cb, start, end);

	    drawDebug(world, target, cb.firstAbove);
	    return cb.firstAbove;
    }

    ///This function creates an svg image for a target, trajectory, and index, so you can visualize the processes and debug.
    static void drawDebug(const b2World& world, const Object& target, int result)
    {
	    if (!debug)
		    return;

	    std::stringstream name;
	    name << debugFolder << "firstbelow-" << target.id << "-";
	    if (result == -1)
		    name << "reject";
	    else
		    name << result;
	    name << ".svg";

	    DebugImage d(name.str().c_str());

	    d.DrawWorld(world, -1);

	    std::vector<b2Vec2> ray;
	    ray.push_back(target.getCentroid());
	    ray.push_back(b2Vec2(target.getCentroid().x,target.getCentroid().y+500));
	    d.DrawPath(ray);

	    d.Save();
    }
};




class FirstAboveAtom : public ComfortPluginAtom
{
public:

    FirstAboveAtom() : ComfortPluginAtom("firstabove")
    {
        setOutputArity(1);
	addInputConstant(); //object id
        addInputPredicate(); //objects
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the object whose mass should be calculated.
        int oid = ((ComfortTerm)(query.input[0])).intval;

	std::vector<Object> objects;
	Calculation::fillVector(query.interpretation, objects);
	if (objects.empty())
		std::cerr << "&firstabove: Called with no objects." << std::endl;

	Object object = Object::findById(objects, oid);
	if (object.id == -1)
	{
		std::cerr << "&firstabove: Called with invalid target id " << oid << "." << std::endl;
		return;
	}

	int firstaboveid = getFirstAbove(object, objects);

	if (firstaboveid != -1)
	{
		ComfortTuple ct;
		ct.push_back(ComfortTerm::createInteger(firstaboveid));
		answer.insert(ct);
	}
    }

private:
    class RCCB : public b2RayCastCallback
	{
		public:
			int firstAbove;

			RCCB()
			{
				firstAbove = -1;
			}	

			virtual float32 ReportFixture(b2Fixture* fixture, const b2Vec2&, const b2Vec2&, float32 fraction)
			{
				firstAbove = WorldCreation::GetId(fixture->GetBody());
				return fraction;
			}
	};

    int getFirstAbove(const Object& target, std::vector<Object>& objects)
    {
	    //Now we create the world, scale factor 1 because we don't do any physics calculation anyways.
	    Object::removeById(objects, target.id);
	    b2World world = WorldCreation::createWorld(objects, 1);

	    b2Vec2 start = target.getCentroid();
	    b2Vec2 end(start.x,0);
	    RCCB cb;

	    world.RayCast(&cb, start, end);

	    drawDebug(world, target, cb.firstAbove);
	    return cb.firstAbove;
    }

    ///This function creates an svg image for a target, trajectory, and index, so you can visualize the processes and debug.
    static void drawDebug(const b2World& world, const Object& target, int result)
    {
	    if (!debug)
		    return;

	    std::stringstream name;
	    name << debugFolder << "firstabove-" << target.id << "-";
	    if (result == -1)
		    name << "reject";
	    else 
		    name << result;
	    name << ".svg";

	    DebugImage d(name.str().c_str());

	    d.DrawWorld(world, -1);

	    std::vector<b2Vec2> ray;
	    ray.push_back(target.getCentroid());
	    ray.push_back(b2Vec2(target.getCentroid().x,0));
	    d.DrawPath(ray);
	    
	    d.Save();
    }
};


class ShootableAtom : public ComfortPluginAtom
{
public:

    ShootableAtom() : ComfortPluginAtom("shootable")
    {
     setOutputArity(3);
	addInputConstant(); //target id
	addInputConstant(); //trajectory identifier, low or high
        addInputConstant(); //velocity
	addInputConstant(); //slingshot x
	addInputConstant(); //slingshot y
	addInputConstant(); //slingshot width
	addInputConstant(); //slingshot height
	addInputConstant(); // birdtype
        addInputPredicate(); //objects
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the object whose mass should be calculated.
        int targetid = ((ComfortTerm)(query.input[0])).intval;
	    std::string trajectory = ((ComfortTerm)(query.input[1])).getUnquotedString();
        double velocity = std::atof(((ComfortTerm)(query.input[2])).getUnquotedString().c_str());

	Sling slingshot;

        slingshot.x = ((ComfortTerm)(query.input[3])).intval;
        slingshot.y = ((ComfortTerm)(query.input[4])).intval;
        slingshot.w = ((ComfortTerm)(query.input[5])).intval;
        slingshot.h = ((ComfortTerm)(query.input[6])).intval;

        std::string birdType = ((ComfortTerm)(query.input[7])).getUnquotedString();
        int size;
        if(birdType == "redbird")
        	size = RED_SIZE;
        else if (birdType == "yellowbird")
        	size = YELLOW_SIZE;
        else if (birdType == "bluebird")
        	size = BLUE_SIZE;
        else if (birdType == "blackbird")
        	 size = BLACK_SIZE;
        else if (birdType == "whitebird")
        	size = WHITE_SIZE;


	    std::vector<Object> objects;

	Calculation::fillVector(query.interpretation, objects);
	if (objects.empty())
		std::cerr << "&next: Called with no objects." << std::endl;

	Object target = Object::findById(objects, targetid);
	if (target.id == -1)
	{
		std::cerr << "&next: Called with invalid target id " << targetid << "." << std::endl;
		return;
	}

	//Ground is not shootable and nothing can be hit behind it.
	if (target.type == ground)
		return;

	const int POINTS = 9;
	const int GUARD = 2;
    int c[POINTS];
	b2Vec2 p[POINTS];

    b2Vec2 left = target.getLeftFaceCenter();
    b2Vec2 top = target.getTopFaceCenter();
    b2Vec2 right = target.getRightFaceCenter();
    b2Vec2 bottom = target.getBottomFaceCenter();

	if(trajectory == "high")
	{
		    p[0] = top;
			for(int i = 1; i < POINTS; i++)
				p[i] = p[0];
		    //
		    // p[2] is middle
		    //
		    // Group 1
		    p[0].x = (int) left.x + GUARD;
		    p[1].x = (int) std::min(left.x +GUARD + size/2,right.x);
		    p[2].x = (int) std::min(left.x +GUARD+ size,right.x);
		    
		    // Group 2
		    p[3].x = (int) std::max(top.x - size/2,left.x);
		    p[4].x = (int) top.x;
		    p[5].x = (int) std::min(top.x + size/2,right.x);
		    
		    // Group 3
		    p[6].x = (int) std::max(right.x - GUARD - size, left.x);;
		    p[7].x = (int) std::max(right.x - GUARD - size/2, left.x);
		    p[8].x = (int) right.x - GUARD;
		    
		   
	}
	else //  low trajectory. Alternatives are computed from the middle point upwards.
	{

		p[0] = left;
	    for(int i = 1; i < POINTS; i++)
				p[i] = p[0];

        // Group 1		    
		p[0].y = (int) (bottom.y-GUARD);
		p[1].y = (int) std::max(bottom.y- GUARD- size/2,top.y);
		p[2].y = (int) std::max(bottom.y- GUARD - size,top.y);;

        // Group 2		    
		p[3].y = (int) std::max(left.y - size/2,top.y);
		p[4].y = (int) left.y;
		p[5].y = (int) std::min(left.y + size/2,bottom.y);;
    
        // Group 3		    
		p[6].y = (int) std::min(top.y + GUARD + size,  bottom.y);
		p[7].y = (int) std::min(top.y + GUARD + size/2,bottom.y);
		p[8].y = (int) (top.y + GUARD);

		    
// Y axis growing downwards
	    //p[1].y = (int)(std::max(p[0].y - size/2, top.y));
	    //p[2].y = (int)(std::max(p[0].y - size,    top.y));
	    //p[3].y = (int)(std::max((p[0].y - size/2 - size),  top.y));
	    //p[4].y = (int)(std::max( (p[0].y - size*2),   top.y));

//	    p[1].y = std::min(p[0].y + size/2, top.y);
//	    p[2].y = std::min(p[0].y + size,    top.y);
//	    p[3].y = std::min((p[0].y + size/2 - size),  top.y);
//	    p[4].y = std::min( (p[0].y + size*2),   top.y);


	}

	if (debug)
		std::cerr << "Object:" << targetid << " -> " << target.cx << ":" << target.cy << ":" << target.w << ":" << target.h << std::endl;


    if (debug) {
    	std::cerr << (trajectory == "high" ? "H=" : "L=");
    	for(int i = 0; i < POINTS; i++)
    		std::cerr << p[i].x << ":" << p[i].y << " ";
    	std::cerr << std::endl;
    }


		for(int i = 0; i < POINTS; i++)
		{
		    if (debug)
		    	std::cerr << "GFO:" << targetid << " " << p[i].x << ":" << p[i].y ;

			c[i] = getFrontObject(p[i], trajectory, slingshot, velocity, objects);
			if(debug)
				std::cerr << "==>" << c[i] << std::endl;
		}
		if(debug)
			std::cerr << std::endl;

		int shift = -1;
		int retValues[POINTS/3];

		for (int i = 0; i < POINTS/3; i++)
			retValues[i] = -1;

		for (int i = 0; i < POINTS/3; i++)
		
			if (targetid == c[i*3] && c[i*3] == c[i*3+1] && c[i*3] == c[i*3+2])
			{
				retValues[i] = shift = (trajectory == "high" ? p[i*3+1].x :  p[i*3+1].y);
				if (debug) std::cerr << "TP:" << i << "=" << retValues[i] << std::endl;
			}
				
		
/*
		if (c[0] == c[1] && c[0] == c[2])
		{
			if (debug) std::cerr << " Downwards";
			retValues[0] = shift = trajectory == "high" ? p[1].x :  p[1].y;
		}
		if (c[2] == c[3] && c[2] == c[4])
		{
			if (debug) std::cerr << " Upwards";
			retValues[2] = shift = trajectory == "high" ? p[3].x  : p[3].y ;
		}
		if (c[1] == c[2] && c[1] == c[3])
				{
			if (debug)		std::cerr << " Center";
		    retValues[1] = shift = trajectory == "high" ? p[2].x :  p[2].y;
		}
		*/
		if (debug) std::cerr << std::endl;
		//
		// shift will contain in priority order, either center, upwards or downwards point.
		// For low trajectories the downwards point is the middle of left face
		// For high trajectories downwards is meant to be leftmost Y point
		//
		// The worst point of the three (the downward one) is never returned unless it is the unique
		//
		bool targetHit = false;
		for(int i = 0; i < POINTS; i++)
		{
			targetHit = targetHit || c[i] == targetid;
		}

		if (targetHit && shift >= 0 ) {
			ComfortTuple ct;
			ct.push_back(ComfortTerm::createInteger(targetid));

			//
			//  For low trajectories we propose center and upper trajectory first (the third one only if none is avail
			//
			if (trajectory == "low")
			{
					ct.push_back(ComfortTerm::createInteger(retValues[POINTS/3-2] >= 0 ? retValues[POINTS/3-2] : shift));
					ct.push_back(ComfortTerm::createInteger(retValues[POINTS/3-1] >= 0 ? retValues[POINTS/3-1] : shift));
			}
			//
			//  For high trajectories we propose center and leftward trajectory first (the third one only if none is avail
			//

			else
			{
				ct.push_back(ComfortTerm::createInteger(retValues[POINTS/3-2] >= 0 ? retValues[POINTS/3-2] : shift));
				ct.push_back(ComfortTerm::createInteger(retValues[POINTS/3-3] >= 0 ? retValues[POINTS/3-3] : shift));

			}
					//std::cerr << "RETURNING " << c[0] << " " << retValues[2] << shift << " " << targetid << std::endl;
			answer.insert(ct);
		}
    }

private:
class RCCB : public b2RayCastCallback
{
	public:
		///The hits reported by the ray cast. Key is a float for sorting in hit order, value is the object id. Note that though we try to minimize the number of objects reported behind floor, there might still be some of them in the map.
		std::map<float32, int> hits;

		virtual float32 ReportFixture(b2Fixture* fixture, const b2Vec2&, const b2Vec2&, float32 fraction)
		{
			int id = WorldCreation::GetId(fixture->GetBody());

			hits[fraction] = id;

			//In this case (when the floor is hit) we tell Box2D that henceforth we only want objects before this floor hit. Box2D takes the return value of this function, and afterwards only notifies intersections that have a lower fraction than the returned one.
			if (id == -1)
				return fraction;

			return 1;
		}
};

/**
 * Gets front object in a given trajectory.
 */

    static int getFrontObject(const b2Vec2& hitPoint, const std::string& trajectory, const Sling& slingshot, const double& velocity, std::vector<Object>& objects)
    {
	    //First, we calculate the two possible release points (if there are two).
    	std::vector<b2Vec2> launchPoints = TrajectoryPlanner::estimateLaunchPoint(slingshot, hitPoint, velocity);

	    //If only one trajectory is possible, it is always the lower one.
	    if (launchPoints.size() == 1 && trajectory == "high")
		    return -3;

	    //Extracting the correct launch point.
	    b2Vec2 launchPoint;
	    if (trajectory == "high")
		    launchPoint = launchPoints[1];
	    else
		    launchPoint = launchPoints[0];

	    //Now we create the world, scale factor 1 because we don't do any physics calculation anyways.
	    b2World world = WorldCreation::createWorld(objects, 1);

	    //And we calculate all the points in the trajectory.
	    int x_max = WorldCreation::maxXPlusW(world);
	    std::vector<b2Vec2> traj = TrajectoryPlanner::predictTrajectory(slingshot, launchPoint, x_max, velocity);

	    traj = simplifyTrajectory(traj);

		    //Now we fill the result vector. We step through the points of the trajectory one by one and check if any point intersects with an object. If so, we add the object to the result vector.
	    std::vector<int> result;

	    for (std::vector<b2Vec2>::iterator i = traj.begin(); i + 1 != traj.end(); ++i)
	    {
		    RCCB cb;

		    world.RayCast(&cb, *i, *(i + 1));

		    for (std::map<float32,int>::const_iterator hit = cb.hits.begin(); hit != cb.hits.end(); ++hit)
		    {
			    if ((*hit).second == -1)
			    {

				    return -1;
			    }

			   return ((*hit).second);
		    }
	    }
	    return -2;
    }

    static std::vector<b2Vec2> simplifyTrajectory(const std::vector<b2Vec2>& vector)
    {
	    const unsigned int size = vector.size();

	    //We initilize the vector to size/10, the number of vertices that we will keep, plus one for the last vertex.
	    std::vector<b2Vec2> result;
	    result.reserve(size/10 + 1);

	    for (unsigned int i = 0; i < size; i += 10)
	    {
		    result.push_back(vector[i]);

		    //If we hit the last point of the trajectory, we can return, so that it will not be added twice.
		    if (i == size - 1)
		    {
			    assert(size/10 + 1 < result.max_size());
			    return result;
		    }
	    }

	    //Add the last point of the trajectory.
	    result.push_back(vector[size - 1]);

	    //Make sure there have not been any resize operations. If there had been any, we would need to readjust our starting values.
	    assert(size/10 + 1 < result.max_size());

	    return result;
    }
};
/*
 *    Takes a target and finds the lowest usable trajectory for a white bird iterating from the object y up in 10 pixel steps.
 */ 

class BestWhiteAtom : public ComfortPluginAtom
{
public:

	BestWhiteAtom() : ComfortPluginAtom("bestwhite")
    {
     setOutputArity(1);
     addInputConstant(); //target id
     addInputConstant(); //trajectory identifier, low or high
     addInputConstant(); //velocity
	 addInputConstant(); //slingshot x
	 addInputConstant(); //slingshot y
	 addInputConstant(); //slingshot width
	 addInputConstant(); //slingshot height
     addInputPredicate(); //objects
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the object whose mass should be calculated.
        int targetid = ((ComfortTerm)(query.input[0])).intval;
	    std::string trajectory = ((ComfortTerm)(query.input[1])).getUnquotedString();
        double velocity = std::atof(((ComfortTerm)(query.input[2])).getUnquotedString().c_str());

	Sling slingshot;

        slingshot.x = ((ComfortTerm)(query.input[3])).intval;
        slingshot.y = ((ComfortTerm)(query.input[4])).intval;
        slingshot.w = ((ComfortTerm)(query.input[5])).intval;
        slingshot.h = ((ComfortTerm)(query.input[6])).intval;

        std::string birdType = ((ComfortTerm)(query.input[7])).getUnquotedString();
        int size;
        if(birdType == "redbird")
        	size = RED_SIZE;
        else if (birdType == "yellowbird")
        	size = YELLOW_SIZE;
        else if (birdType == "bluebird")
        	size = BLUE_SIZE;
        else if (birdType == "blackbird")
        	 size = BLACK_SIZE;
        else if (birdType == "whitebird")
        	size = WHITE_SIZE;


	    std::vector<Object> objects;

	Calculation::fillVector(query.interpretation, objects);
	if (objects.empty())
		std::cerr << "&next: Called with no objects." << std::endl;

	Object target = Object::findById(objects, targetid);
	if (target.id == -1)
	{
		std::cerr << "&next: Called with invalid target id " << targetid << "." << std::endl;
		return;
	}

	//Ground is not shootable and nothing can be hit behind it.
	if (target.type == ground)
		return;

	const int POINTS = 3;
	const int GUARD = 2;
    int c[POINTS];
    Object ob[POINTS];
	b2Vec2 p[POINTS];

    b2Vec2 left = target.getLeftFaceCenter();
    b2Vec2 top = target.getTopFaceCenter();
    b2Vec2 right = target.getRightFaceCenter();
    b2Vec2 bottom = target.getBottomFaceCenter();

    int shift = -1;
    int X_THRESHOLD = top.x + 20;
    for (p[0].y = top.y-10; p[0].y >= 40; p[0].y -= 20)
	{

		    for(int i = 1; i < POINTS; i++)
				p[i] = p[0];

		    p[0].x = (int) std::max(top.x - size/2,left.x);
		    p[1].x = (int) top.x;
		    p[2].x = (int) std::min(top.x + size/2,right.x);


	if (debug)
		std::cerr << "Object:" << targetid << " -> " << target.cx << ":" << target.cy << ":" << target.w << ":" << target.h << std::endl;


    if (debug) {
    	std::cerr << (trajectory == "high" ? "H=" : "L=");
    	for(int i = 0; i < POINTS; i++)
    		std::cerr << p[i].x << ":" << p[i].y << " ";
    	std::cerr << std::endl;
    }

		for(int i = 0; i < POINTS; i++)
		{
		    if (debug)
		    	std::cerr << "BW:" << targetid << " " << p[i].x << ":" << p[i].y ;

			c[i] = getFrontObject(p[i], trajectory, slingshot, velocity, objects);
	        ob[i] = Object::findById(objects, c[i]);

			if(debug)
				std::cerr << "==>" << c[i] << std::endl;
		}
		if(debug)
			std::cerr << std::endl;

   		shift = -1;

			if (ob[0].getLeftFaceCenter().x > X_THRESHOLD && ob[0].getLeftFaceCenter().x > X_THRESHOLD && ob[0].getLeftFaceCenter().x > X_THRESHOLD)
			{
				shift = p[1].y;
				if (debug) std::cerr << "TP << " << shift << std::endl;
				break;
			}

	} // for
		if (debug) std::cerr << std::endl;


		if (shift >= 0 ) {
			ComfortTuple ct;
			ct.push_back(ComfortTerm::createInteger(shift));
			answer.insert(ct);
		}
    }

private:
class RCCB : public b2RayCastCallback
{
	public:
		///The hits reported by the ray cast. Key is a float for sorting in hit order, value is the object id. Note that though we try to minimize the number of objects reported behind floor, there might still be some of them in the map.
		std::map<float32, int> hits;

		virtual float32 ReportFixture(b2Fixture* fixture, const b2Vec2&, const b2Vec2&, float32 fraction)
		{
			int id = WorldCreation::GetId(fixture->GetBody());

			hits[fraction] = id;

			//In this case (when the floor is hit) we tell Box2D that henceforth we only want objects before this floor hit. Box2D takes the return value of this function, and afterwards only notifies intersections that have a lower fraction than the returned one.
			if (id == -1)
				return fraction;

			return 1;
		}
};

/**
 * Gets front object in a given trajectory.
 */

    static int getFrontObject(const b2Vec2& hitPoint, const std::string& trajectory, const Sling& slingshot, const double& velocity, std::vector<Object>& objects)
    {
	    //First, we calculate the two possible release points (if there are two).
    	std::vector<b2Vec2> launchPoints = TrajectoryPlanner::estimateLaunchPoint(slingshot, hitPoint, velocity);

	    //If only one trajectory is possible, it is always the lower one.
	    if (launchPoints.size() == 1 && trajectory == "high")
		    return -3;

	    //Extracting the correct launch point.
	    b2Vec2 launchPoint;
	    if (trajectory == "high")
		    launchPoint = launchPoints[1];
	    else
		    launchPoint = launchPoints[0];

	    //Now we create the world, scale factor 1 because we don't do any physics calculation anyways.
	    b2World world = WorldCreation::createWorld(objects, 1);

	    //And we calculate all the points in the trajectory.
	    int x_max = WorldCreation::maxXPlusW(world);
	    std::vector<b2Vec2> traj = TrajectoryPlanner::predictTrajectory(slingshot, launchPoint, x_max, velocity);

	    traj = simplifyTrajectory(traj);

		    //Now we fill the result vector. We step through the points of the trajectory one by one and check if any point intersects with an object. If so, we add the object to the result vector.
	    std::vector<int> result;

	    for (std::vector<b2Vec2>::iterator i = traj.begin(); i + 1 != traj.end(); ++i)
	    {
		    RCCB cb;

		    world.RayCast(&cb, *i, *(i + 1));

		    for (std::map<float32,int>::const_iterator hit = cb.hits.begin(); hit != cb.hits.end(); ++hit)
		    {
			    if ((*hit).second == -1)
			    {

				    return -1;
			    }

			   return ((*hit).second);
		    }
	    }
	    return -2;
    }

    static std::vector<b2Vec2> simplifyTrajectory(const std::vector<b2Vec2>& vector)
    {
	    const unsigned int size = vector.size();

	    //We initilize the vector to size/10, the number of vertices that we will keep, plus one for the last vertex.
	    std::vector<b2Vec2> result;
	    result.reserve(size/10 + 1);

	    for (unsigned int i = 0; i < size; i += 10)
	    {
		    result.push_back(vector[i]);

		    //If we hit the last point of the trajectory, we can return, so that it will not be added twice.
		    if (i == size - 1)
		    {
			    assert(size/10 + 1 < result.max_size());
			    return result;
		    }
	    }

	    //Add the last point of the trajectory.
	    result.push_back(vector[size - 1]);

	    //Make sure there have not been any resize operations. If there had been any, we would need to readjust our starting values.
	    assert(size/10 + 1 < result.max_size());

	    return result;
    }
};



class NextAtom : public ComfortPluginAtom
{
public:

    NextAtom() : ComfortPluginAtom("next")
    {
        setOutputArity(2);
    	addInputConstant(); //object id

        addInputConstant(); //target hitting offset
	addInputConstant(); //trajectory identifier, low or high
        addInputConstant(); //velocity
	addInputConstant(); //slingshot x
	addInputConstant(); //slingshot y
	addInputConstant(); //slingshot width
	addInputConstant(); //slingshot height
        addInputPredicate(); //objects
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the object whose mass should be calculated.
        int targetid = ((ComfortTerm)(query.input[0])).intval;
	    int targetOffset = ((ComfortTerm)(query.input[1])).intval;
        std::string trajectory = ((ComfortTerm)(query.input[2])).getUnquotedString();
        double velocity = std::atof(((ComfortTerm)(query.input[3])).getUnquotedString().c_str());

	Sling slingshot;

        slingshot.x = ((ComfortTerm)(query.input[4])).intval;
        slingshot.y = ((ComfortTerm)(query.input[5])).intval;
        slingshot.w = ((ComfortTerm)(query.input[6])).intval;
        slingshot.h = ((ComfortTerm)(query.input[7])).intval;

	std::vector<Object> objects;

	Calculation::fillVector(query.interpretation, objects);
	if (objects.empty())
		std::cerr << "&next: Called with no objects." << std::endl;

	Object target = Object::findById(objects, targetid);
	if (target.id == -1)
	{
		std::cerr << "&next: Called with invalid target id " << targetid << "." << std::endl;
		return;
	}

	//Ground is not shootable and nothing can be hit behind it.
	if (target.type == ground)
		return;

	std::vector<int> inTrajectory = getNext(target,targetOffset, trajectory, slingshot, velocity, objects);


	//dlvhex2 allows to return multiple results per retrieve. This is sensible here, because in order to calculate the second intersection, we need to check the whole trajectory up until the second intersection and skip the first intersection. By calculating all intersections in one go, we can save a lot of time.
	int index = 0;
	for(std::vector<int>::const_iterator obj = inTrajectory.begin();
			obj != inTrajectory.end(); ++obj, ++index)
	{
		ComfortTuple ct;
		ct.push_back(ComfortTerm::createInteger(index));
		ct.push_back(ComfortTerm::createInteger(*obj));
		answer.insert(ct);
	}
    }

private:
class RCCB : public b2RayCastCallback
{
	public:
		///The hits reported by the ray cast. Key is a float for sorting in hit order, value is the object id. Note that though we try to minimize the number of objects reported behind floor, there might still be some of them in the map.
		std::map<float32, int> hits;

		virtual float32 ReportFixture(b2Fixture* fixture, const b2Vec2&, const b2Vec2&, float32 fraction)
		{
			int id = WorldCreation::GetId(fixture->GetBody());

			hits[fraction] = id;

			//In this case (when the floor is hit) we tell Box2D that henceforth we only want objects before this floor hit. Box2D takes the return value of this function, and afterwards only notifies intersections that have a lower fraction than the returned one.
			if (id == -1)
				return fraction;

			return 1;
		}
};

    static std::vector<int> getNext(const Object& target, int targetOffset, const std::string& trajectory, const Sling& slingshot, const double& velocity, std::vector<Object>& objects)
    {
	    //First, we calculate the two possible release points (if there are two).
//	    std::vector<b2Vec2> launchPoints = trajectory == "high" ?
//	                                   TrajectoryPlanner::estimateLaunchPoint(slingshot, target.getTopFaceCenter(), velocity) :
//                                       TrajectoryPlanner::estimateLaunchPoint(slingshot, target.getLeftFaceCenter(), velocity);

    	//
    	//  targetOffset is precomputed by the shootable module.
    	//
    	b2Vec2 realTarget = trajectory == "high" ? target.getTopFaceCenter() : target.getLeftFaceCenter();
    	if(trajectory == "high")
    		realTarget.x = targetOffset;
    	else
    		realTarget.y = targetOffset;

    	if (debug)
    	std::cerr << "getNext:" << target.id << ":" << targetOffset << std::endl;

    	std::vector<b2Vec2> launchPoints = TrajectoryPlanner::estimateLaunchPoint(slingshot, realTarget, velocity);
	    //If only one trajectory is possible, it is always the lower one.
	    if (launchPoints.size() == 1 && trajectory == "high")
		    return std::vector<int>();

	    //Extracting the correct launch point.
	    b2Vec2 launchPoint;
	    if (trajectory == "high")
		    launchPoint = launchPoints[1];
	    else
		    launchPoint = launchPoints[0];

	    //Now we create the world, scale factor 1 because we don't do any physics calculation anyways.
	    b2World world = WorldCreation::createWorld(objects, 1);

	    //And we calculate all the points in the trajectory.
	    int x_max = WorldCreation::maxXPlusW(world);
	    std::vector<b2Vec2> traj = TrajectoryPlanner::predictTrajectory(slingshot, launchPoint, x_max, velocity);

	    traj = simplifyTrajectory(traj);

	    DebugImage * di;
	    if (debug)
	    {
		    std::stringstream name;
		    name << debugFolder << "next-" << target.id << "-" << trajectory << ".svg";
		    di = new DebugImage(name.str().c_str());
		    di->DrawWorld(world, target.id);
		    di->DrawPath(traj);
	    }

	    //Now we fill the result vector. We step through the points of the trajectory one by one and check if any point intersects with an object. If so, we add the object to the result vector.
	    std::vector<int> result;

	    for (std::vector<b2Vec2>::iterator i = traj.begin(); i + 1 != traj.end(); ++i)
	    {
		    RCCB cb;

		    world.RayCast(&cb, *i, *(i + 1));

		    for (std::map<float32,int>::const_iterator hit = cb.hits.begin(); hit != cb.hits.end(); ++hit)
		    {
			    if ((*hit).second == -1)
			    {
				    if (debug)
				    {
					    di->DrawVector<int>(result);
					    di->Save();
					    delete di;
				    }

				    return result;
			    }

			    result.push_back((*hit).second);
		    }
	    }


	    if (debug)
	    {
		    di->DrawVector<int>(result);
		    di->Save();
		    delete di;
	    }

	    return result;
    }

    static std::vector<b2Vec2> simplifyTrajectory(const std::vector<b2Vec2>& vector)
    {
	    const unsigned int size = vector.size();

	    //We initilize the vector to size/10, the number of vertices that we will keep, plus one for the last vertex.
	    std::vector<b2Vec2> result;
	    result.reserve(size/10 + 1);

	    for (unsigned int i = 0; i < size; i += 10)
	    {
		    result.push_back(vector[i]);

		    //If we hit the last point of the trajectory, we can return, so that it will not be added twice.
		    if (i == size - 1)
		    {
			    assert(size/10 + 1 < result.max_size());
			    return result;
		    }
	    }

	    //Add the last point of the trajectory.
	    result.push_back(vector[size - 1]);

	    //Make sure there have not been any resize operations. If there had been any, we would need to readjust our starting values.
	    assert(size/10 + 1 < result.max_size());

	    return result;
    }
};

class CanPushAtom : public ComfortPluginAtom
{
public:

    CanPushAtom() : ComfortPluginAtom("canpush")
    {
        setOutputArity(2);
	    addInputPredicate(); //objects
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the object whose mass should be calculated.

	std::vector<Object> objects;

	Calculation::fillVector(query.interpretation, objects);
	if (objects.empty())
		std::cerr << "&next: Called with no objects." << std::endl;

	for(std::vector<Object>::iterator it = objects.begin(); it != objects.end(); ++it)
        {
            Object* o = &(*it);

	    if (o->type == ground)
		    continue;
	    for(std::vector<Object>::iterator it2 = objects.begin(); it2 != objects.end(); ++it2)
	        {
	            Object* i = &(*it2);
	            if (o->type == ground && o->id == i->id)
	            		    continue;

	            //
	            // Angle penalty: should  we assume a too much inclined object is not that lean to push anything?
	            //
	            //const float angle_threshold = 30;
	            b2Vec2 bottomO = o->getBottomFaceCenter();
	            b2Vec2 rightO = o->getRightFaceCenter();

	            b2Vec2 topI = i->getTopFaceCenter();
	            b2Vec2 bottomI = i->getBottomFaceCenter();
	            b2Vec2 leftI = i->getLeftFaceCenter();
	            //
	            //  Assumes a rotation of 90° degrees clock wise of object o
	            //
	            float rotatedX = bottomO.x + o->h + o->w/2;

	            //std::cerr << "rightO.x <= leftI.x &&  rotatedX > leftI.x && bottomO.y >= topI.y && bottomO.y <= bottomI.y " << std::endl;
	            //std::cerr << o->h << ":" << o->w << ":" << o->angle << "|" << rightO.x << ":" << leftI.x << ":" << rotatedX << ":" <<leftI.x << ":" <<bottomO.y << ":" <<topI.y << ":" <<bottomO.y << ":" <<bottomI.y << std::endl;

	            if (rightO.x <= leftI.x &&  rotatedX > leftI.x && bottomO.y >= topI.y && bottomO.y <= bottomI.y  )
	            {
	        	    ComfortTuple ct;
	        		ct.push_back(ComfortTerm::createInteger(o->id));
	        		ct.push_back(ComfortTerm::createInteger(i->id));
	        		answer.insert(ct);
	        		//std::cerr << o->id << " CAN PUSH " << i->id << std::endl;
	            }
	           // else
	           // 	std::cerr << o->id << " CANNOT PUSH " << i->id << std::endl;


	        } // for o
        } // for i
	} // method


};



class OnTopMassAtom : public ComfortPluginAtom
{
public:

    OnTopMassAtom() : ComfortPluginAtom("on_top_mass")
    {
        setOutputArity(1);
        addInputConstant();
        addInputPredicate();
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the object whose mass should be calculated.
        int object = ((ComfortTerm)(query.input[0])).intval;

        ComfortTuple ct;
        ct.push_back(ComfortTerm::createInteger(onTopMass(query.interpretation, object)));
        answer.insert(ct);
    }

private:
    static int onTopMass(ComfortInterpretation i, int oid)
    {
        std::vector<Object> objects;

        Calculation::fillVector(i, objects);

        std::vector<std::pair<int,int> > graph = OnTop::calculateCached(objects);

        std::vector<int> transitiveHull = calculateTransitiveHull(graph, oid);
        float total = 0;

        for(std::vector<int>::iterator i = transitiveHull.begin(); i != transitiveHull.end(); ++i)
        {
            total = mass(objects, (*i));
        }


        return (int)total;
    }

    static float mass(std::vector<Object> objects, int oid)
    {
        for(std::vector<Object>::iterator i = objects.begin(); i != objects.end(); ++i)
        {
            if ((*i).id == oid)
                return (*i).getMass();
        }

        return 0;
    }

    static std::vector<int> calculateTransitiveHull(std::vector<std::pair<int,int> > graph, int oid)
    {
        std::vector<int> hull;

        hull.push_back(oid);

        for(std::vector<std::pair<int,int> >::iterator i = graph.begin(); i != graph.end(); i++)
        {
            if ((*i).second == oid)
            {
                int first = (*i).first;
                i = graph.erase(i);
                --i;

                std::vector<int> subhull = calculateTransitiveHull(graph, first);

                hull.insert(hull.end(), subhull.begin(), subhull.end());
            }
        }

        return hull;
    }
};

class OnTopOfAtom : public ComfortPluginAtom
{
public:

    OnTopOfAtom() : ComfortPluginAtom("on_top_of")
    {
        setOutputArity(0);
        addInputConstant();
        addInputConstant();
        addInputPredicate();
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        // First we retrieve the two objects whose difference should be calculated.
        int firstObject = ((ComfortTerm)(query.input[0])).intval;
        int secondObject = ((ComfortTerm)(query.input[1])).intval;

        if(isOnTopOf(query.interpretation, firstObject, secondObject))
            answer.insert(ComfortTuple());
    }

private:

    bool isOnTopOf(ComfortInterpretation i, int a, int b)
    {
        std::vector<Object> objects;

        Calculation::fillVector(i, objects);

        std::vector<std::pair<int,int> > graph = OnTop::calculateCached(objects);

        return contains(graph, a, b);
    }

    bool contains(std::vector<std::pair<int,int> > graph, int a, int b)
    {
        for (std::vector<std::pair<int,int> >::iterator i = graph.begin(); i != graph.end(); ++i)
            if ((*i).first == a && (*i).second == b)
                return true;

        return false;
    }
};

class OnTopAllAtom : public ComfortPluginAtom
{
public:
	
	OnTopAllAtom() : ComfortPluginAtom("on_top_all")
	{
		setOutputArity(2);
		addInputPredicate();
	}
	
	virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
	{
		
		std::vector<Object> objects;
		
		Calculation::fillVector(query.interpretation, objects);
		
		std::vector<std::pair<int,int> > graph = OnTop::calculateCached(objects);
		
		for (std::vector<std::pair<int,int> >::iterator i = graph.begin(); i != graph.end(); ++i) {
			ComfortTuple ct;
			ct.push_back(ComfortTerm::createInteger((*i).first));
			ct.push_back(ComfortTerm::createInteger((*i).second));
			answer.insert(ct);
		}
		
	}
	
};

class DistanceAtom : public ComfortPluginAtom
{
public:

    DistanceAtom() : ComfortPluginAtom("distance")
    {
        setOutputArity(1);
        addInputConstant();
        addInputConstant();
        addInputPredicate();
    }

    virtual void retrieve(const ComfortQuery& query, ComfortAnswer& answer)
    {
        ComfortTuple tu;

        // First we retrieve the two objects whose difference should be calculated.
        int firstObject = ((ComfortTerm)(query.input[0])).intval;
        int secondObject = ((ComfortTerm)(query.input[1])).intval;

        //If there are no objects found, the programmer most likely called the predicated wrong.
        //We issue a usage information.
        if (query.interpretation.size() == 0)
        {
            std::cerr << "Please call \"distance\" like this: &distance[ID1, ID2, objects](X)." << std::endl;
            assert(true);
        }

        bool found[] = {false,false};
        int x[2];
        int y[2];

        //Now we go through each of the predicates (each "object(...)") and check if it denotes one of the
        //objects we need. If so, we save the coordinates of the center of the rectangle (coordinate of
        //left top corner plus half of width or height). firstObject may be equal to secondObject.
        for (ComfortInterpretation::iterator c = query.interpretation.begin(); c != query.interpretation.end(); ++c)
        {
            if (c->getArgument(1).intval == firstObject)
            {
                x[0] = c->getArgument(2).intval + c->getArgument(4).intval/2;
                y[0] = c->getArgument(3).intval + c->getArgument(5).intval/2;
                found[0] = true;
            }
            if (c->getArgument(1).intval == secondObject)
            {
                x[1] = c->getArgument(2).intval + c->getArgument(4).intval/2;
                y[1] = c->getArgument(3).intval + c->getArgument(5).intval/2;
                found[1] = true;
            }
        }

        //If either of the coordinates could not be found, issue warnings.
        if (found[0] == false)
        {
            std::cerr << "&distance: The object id " << firstObject << " was not found." << std::endl;
            assert(true);
        }

        if (found[1] == false)
        {
            std::cerr << "&distance: The object id " << secondObject << " was not found." << std::endl;
            assert(true);
        }

        //Calculate the distance and append.
        ComfortTerm ct = ComfortTerm::createInteger(DistanceAtom::distance(x[0],y[0],x[1],y[1]));

        tu.push_back(ct);

        answer.insert(tu);
    }

private:
    static int distance(int x0, int y0, int x1, int y1)
    {
        double distance;

        double distance_x = x0-x1;

        double distance_y = y0- y1;

        distance = sqrt( (distance_x * distance_x) + (distance_y * distance_y));

        return (int)distance;
    }

};

class TestPlugin:
    public PluginInterface
{

public:
    TestPlugin():
        PluginInterface()
    {
        setNameVersion("hexagentplugin", 0, 0, 1);
    }

    virtual std::vector<PluginAtomPtr> createAtoms(ProgramCtx&) const
    {
        std::vector<PluginAtomPtr> ret;

        // return smart pointer with deleter (i.e., delete code compiled into this plugin)
        ret.push_back(PluginAtomPtr(new DistanceAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new OnTopOfAtom, PluginPtrDeleter<PluginAtom>()));
		ret.push_back(PluginAtomPtr(new OnTopAllAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new OnTopMassAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new NextAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new ShootableAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new FirstAboveAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new FirstBelowAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new CanPushAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new ClearSkyAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new StabilityAtom, PluginPtrDeleter<PluginAtom>()));
        ret.push_back(PluginAtomPtr(new BestWhiteAtom, PluginPtrDeleter<PluginAtom>()));

        return ret;
    }
};

TestPlugin theTestplugin;

/* Major version number of DLVHEX_ABI */
#define DLVHEX_ABI_VERSION_MAJOR 7

/* Micro version number of DLVHEX_ABI */
#define DLVHEX_ABI_VERSION_MICRO 0

/* Minor version number of DLVHEX_ABI */
#define DLVHEX_ABI_VERSION_MINOR 1

//IMPLEMENT_PLUGINABIVERSIONFUNCTION

extern "C" int getDlvhex2ABIVersion()
{
    return 7*10000+ 1*100+ 0;
}


// return plain C type s.t. all compilers and linkers will like this code
extern "C" void * PLUGINIMPORTFUNCTION()
{
    return reinterpret_cast<void*>(& theTestplugin);
}

