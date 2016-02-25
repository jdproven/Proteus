import java.util.Random;

class QLearningController {
	
	Model model;
	double input[];
	double output[];
	NeuralNet nn;
	double exploreRate;
	double discount;
	Matrix inMatrix;
	Matrix outMatrix;
	int gameCount;

	QLearningController(Model m) throws Exception {
		this.model = m;
		nn = new NeuralNet();
		input = new double[4];
		output = new double[2];
		nn.layers.add(new Layer(4, 32));
		nn.layers.add(new Layer(32, 2));
		nn.init(rand);
		exploreRate = .2;
		discount = .99;
		inMatrix = new Matrix(10000, 4);
		outMatrix = new Matrix(10000, 2);
		gameCount = 0;
	}
	
	//Create global Random number generator
	Random rand = new Random(5);
	
	//Describe vital information for the bird to use to learn
	class ActionState {
		boolean action;	//True for flap, false for no flap
		int y_pos;
		int vert_velocity;
		int tube1;
		boolean up;
		
		public ActionState(boolean action1, int y_pos1, int vert_velocity1, int tube11, boolean up1) {
			action = action1;
			y_pos = y_pos1;
			vert_velocity = vert_velocity1;
			tube1 = tube11;
			up = up1;
		}
	}
	
	//Copy constructor
	public ActionState copyActionState(ActionState act){
		return new ActionState(act.action, act.y_pos, act.vert_velocity, act.tube1, act.up);
	}
	
	public void printActionState(ActionState act){
		System.out.println("Action:" + act.action + " YPosition:" + act.y_pos + " Velocity:" + act.vert_velocity + " TubeY:" + act.tube1 + " Up:" + act.up);
	}
	
	//Determine and return the current ActionState
	public ActionState determineAS(){
		boolean flap = false;
		boolean up = true;
		int birdY, velo;
		int tube1 = 0;
		if(this.model.bird.time_since_flap == 0)
			flap = true;
		birdY = this.model.bird.y/1;
		velo = (int)this.model.bird.vert_vel;
		if(this.model.tubes.size() > 0)
			for(int i = 0; i < this.model.tubes.size(); i++){
				if(this.model.tubes.get(i).x < 100){tube1 = 0;}
				else{
					tube1 = this.model.tubes.get(i).y;
					up = this.model.tubes.get(i).up;
					break;
				}
			}
		return new ActionState(flap, birdY, velo, tube1, up);
	}
	
	public ActionState executeBestDecision(ActionState act){
		ActionState bestAct = act;
		double maxQ[], r;
		double flapChance = .08;
		maxQ = getQValues(act);
		//MaxQ[0] is value of flapping, and maxQ[1] is value of not flapping
		//Determine the best action
		if(maxQ[0] == maxQ[1]){
			r = rand.nextDouble();
			if(act.action == true){
				if(r < flapChance)
					bestAct.action = true;
				else
					bestAct.action = false;
			}
			else{
				if(r < flapChance)
					bestAct.action = false;
				else
					bestAct.action = true;
			}
		}
		else if(maxQ[0] > maxQ[1])
			bestAct.action = true;
		else
			bestAct.action = false;
		return bestAct;
	}
	
	double[] vec(ActionState j){
		input[0] = (double)j.vert_velocity * 0.001;
		input[1] = (double)j.y_pos * 0.001;
		input[2] = (double)j.tube1 * 0.001;
		//input[3] = (j.action ? 0.0 : 1.0);
		input[3] = (j.up ? 0.0 : 1.0);
		return input;
	}
	
	//Get Q-values neural net
	double[] getQValues(ActionState j){
			vec(j);
			return nn.forwardProp(input);
		}
	
	void updateStateValue(NeuralNet nn, ActionState i, ActionState j, boolean alive){
		double reward = .00001;
		if(!alive)
			reward = -.01;
		input = vec(i);
		output = getQValues(i);
		double[] target = new double[1];
		target[0] = reward + discount * measureStateValue(nn, j);
		nn.refine(input, target, discount);
	}
	
	double measureStateValue(NeuralNet nn, ActionState j){
		double m = -9999999;
		double[] in = null;
		in = getQValues(j);
		double[] out = nn.forwardProp(in);
		if(out[0] > m)
			m = out[0];
		j.action = j.action;
		in = getQValues(j);
		out = nn.forwardProp(in);
		if(out[0] > m)
			m = out[0];
		return m;
	}
	
	boolean update() {
		//Get the current ActionState
		ActionState i = determineAS();
		//With 95% probability, do not explore
		boolean flap = false;
		double flapChance = .08;
		//Explore
		if(rand.nextDouble() < exploreRate){
			if(rand.nextDouble() < flapChance)
				flap = true;
		}
		//Find best decision and execute it
		else{
			//i = executeBestDecision(i);
			double[] qVals = getQValues(i);
			double qFlap = qVals[1];
			double qNoflap = qVals[0];
			if(qFlap > qNoflap) // is flapping better?
				flap = true;
			else if(qNoflap > qFlap)
				flap = false;
			else if(rand.nextDouble() < 0.05)
				flap = true;
			else
				flap = false;
		}
		i.action = flap;
		if(flap)
			model.flap();
		boolean alive = this.model.update();
		ActionState j = determineAS();
		double reward = 0.00001;
		if(!alive)
			reward = -0.001;
		
		// Learn
		//updateStateValue(nn, i, j, alive);
		double[] qVals = getQValues(i);
		output[0] = qVals[0];
		output[1] = qVals[1];
		qVals = getQValues(j);
		double qBest = Math.max(qVals[1], qVals[0]);
		double qNew = reward + discount * qBest;
		output[i.action ? 1 : 0] = qNew;
		input = vec(i);
		if(gameCount < 10000){
			for(int z = 0; z < 4; z++)
				inMatrix.row(gameCount)[z] = input[z];
			for(int y = 0; y < 2; y++)
				outMatrix.row(gameCount)[y] = output[y];
		}
		else{
			for(int z = 0; z < 4; z++)
				inMatrix.row(gameCount%10000)[z] = input[z];
			for(int y = 0; y < 2; y++)
				outMatrix.row(gameCount%10000)[y] = output[y];
			for(int a = 0; a < 10; a++){
				int aRow = rand.nextInt(10000);
				input = inMatrix.row(aRow);
				output = outMatrix.row(aRow);
				nn.refine(input, output, 0.1);
			}
		}
		return alive;
	}
	
	
	//Run Q-learning algorithm
	public void QLearning(){
		int ITERMAX = 500000;
		for(int count = 0; count < ITERMAX; count++){
			if(!update())
				return;
		}
		return;
	}
}






