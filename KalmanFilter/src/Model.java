import java.util.Random;
import java.lang.Math;

//Describe vital information for the bird to use to learn
class ActionState {
	boolean action;	//True for flap, false for no flap
	int y_pos;
	int vert_velocity;
	int x_pos;
	
	public ActionState(boolean action1, int y_pos1, int vert_velocity1) {
		action = action1;
		y_pos = y_pos1;
		vert_velocity = vert_velocity1;
		x_pos = 100;
	}
	
	//Copy constructor
		public ActionState copyActionState(ActionState act){
			return new ActionState(act.action, act.y_pos, act.vert_velocity);
		}
		
		public void printActionState(ActionState act){
			System.out.println("Action:" + act.action + " YPosition:" + act.y_pos + " Velocity:" + act.vert_velocity);
		}
}

class Bird
{
	int x;
	int y;
	double vert_vel;
	int time_since_flap;

	Bird() {
		x = 100;
		y = 100;
	}

	boolean update() {
		time_since_flap++;
		y += vert_vel;
		vert_vel += 1.5;
		if(y < -55)
			return false;
		else if(y > 500)
			return false;
		return true;
	}

	void flap() {
		vert_vel = -12.0;
		if(time_since_flap > 4)
			time_since_flap = 0;
		else
			time_since_flap = 5;
	}
	
	double[] distance(){
		double distances[] = new double[2];
		int xDist = 400;
		//Location 0,1 refer to distance from top and bottom corners respectively
		distances[0] = Math.sqrt(Math.pow((500 - this.y), 2) + Math.pow(xDist, 2)) * 0.001;
		distances[1] = Math.sqrt(Math.pow(this.y, 2) + Math.pow(xDist, 2)) * 0.001;
		return distances;
	}
}


class Model
{
	Bird bird;
	Random rand;
	int frame;
	int score;

	Model() {
		rand = new Random(0);
		bird = new Bird();
		frame = 38;
	}

	void reset() {
		rand = new Random(0);
		bird = new Bird();
		frame = 38;
	}

	public boolean update() {
		if(!bird.update())
			return false;
		return true;
	}

	public void flap() {
		bird.flap();
	}
}








