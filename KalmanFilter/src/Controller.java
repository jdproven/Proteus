import java.util.Random;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

class Controller implements MouseListener, KeyListener {
	
	Model model;
	double transitionInput[];
	double transitionOutput[];
	double observationInput[];
	double observationOutput[];
	NeuralNet transitionModel;
	NeuralNet observationModel;
	double exploreRate;
	double discount;
	Matrix transInMatrix;
	Matrix transOutMatrix;
	Matrix obsInMatrix;
	Matrix obsOutMatrix;
	int gameCount;
	Random rand;
	int frame;
	double error;
	double observationNoise;
	Matrix obsNoise;
	Matrix transNoise;
	int numTransitionInputs;
	int numTransitionOutputs;
	int numObservationInputs;
	int numObservationOutputs;
	int burnInFrames;
	Matrix errorCovariance;
	double[] stateEstimate;
	boolean goHigher;

	Controller(Model m) throws Exception {
		this.model = m;
		numTransitionInputs = 4;
		numTransitionOutputs = 4;
		numObservationInputs = 4;
		numObservationOutputs = 2;
		transitionModel = new NeuralNet();
		observationModel = new NeuralNet();
		transitionInput = new double[numTransitionInputs];
		transitionOutput = new double[numTransitionOutputs];
		transitionModel.layers.add(new Layer(numTransitionInputs, 16));
		transitionModel.layers.add(new Layer(16, numTransitionOutputs));
		observationModel.layers.add(new Layer(numObservationInputs, 16));
		observationModel.layers.add(new Layer(16, numObservationOutputs));
		rand = new Random(5);
		transitionModel.init(rand);
		observationModel.init(rand);
		exploreRate = .2;
		discount = .99;
		transInMatrix = new Matrix(1000, numTransitionInputs);
		transOutMatrix = new Matrix(1000, numTransitionOutputs);
		obsInMatrix = new Matrix(1000, numObservationInputs);
		obsOutMatrix = new Matrix(1000, numObservationOutputs);
		gameCount = 0;
		frame = 0;
		burnInFrames = 0;
		//Create covariance matrix and inject noise (P)
		//error = .1;
		observationNoise = .001;
		obsNoise = new Matrix(numObservationInputs, numObservationOutputs);
		obsNoise.setAll(observationNoise);
		transNoise = new Matrix(numTransitionInputs, numTransitionOutputs);
		transNoise.setAll(observationNoise);
		errorCovariance = new Matrix(numTransitionInputs, numTransitionOutputs);
		for(int i = 0; i < errorCovariance.rows(); i++)
			errorCovariance.row(i)[i] = error;
		stateEstimate = new double[numTransitionOutputs];
		goHigher = false;
	}
	
	public void mousePressed(MouseEvent e) {
		this.model.flap();
	}
	public void keyPressed(KeyEvent e) {
		this.model.flap();
	}
	public void mouseReleased(MouseEvent e) {    }
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }

	public void mouseClicked(MouseEvent e) {    }
	public void keyTyped(KeyEvent e) {    }
	public void keyReleased(KeyEvent e) {    }
	
	//Determine and return the current ActionState
	public ActionState determineAS(){
		boolean flap = false;
		int birdY, velo;
		if(this.model.bird.time_since_flap == 0)
			flap = true;
		birdY = this.model.bird.y/1;
		velo = (int)this.model.bird.vert_vel;
		return new ActionState(flap, birdY, velo);
	}
	
	double[] vec(ActionState a){
		double[] vec = new double[4];
		vec[0] = (double)a.vert_velocity * 0.001;
		vec[1] = (double)a.y_pos * 0.001;
		vec[2] = (a.action ? 0.0 : 1.0);
		vec[3] = (double)a.x_pos * 0.001;
		return vec;
	}
	
	//Predict the state of the next frame
	double[] predictState(ActionState a){
		double predict[] = vec(a);
		return transitionModel.forwardProp(predict);
	}
	
	//Predict the distance to the corners based on the state
	double[] predictDistances(ActionState a){
		double predict[] = vec(a);
		return observationModel.forwardProp(predict);
	}
	
	void updateMatricies(Matrix m1, Matrix m2, boolean useTransInput){
		if(useTransInput){
			for(int z = 0; z < transitionInput.length; z++)
				m1.row(frame%1000)[z] = transitionInput[z];
			for(int y = 0; y < transitionOutput.length; y++)
				m2.row(frame%1000)[y] = transitionOutput[y];
		}
		else{
			for(int z = 0; z < observationInput.length; z++)
				m1.row(frame%1000)[z] = observationInput[z];
			for(int y = 0; y < observationOutput.length; y++)
				m2.row(frame%1000)[y] = observationOutput[y];
		}
	}
	
	void refineWithMatricies(double[] previousState, double[] currentState, double[] previousDistances, double[] currentDistances){
		transitionInput = previousState;
		transitionOutput = currentState;
		observationInput = previousDistances;
		observationOutput = currentDistances;
		if(frame < 100){
			updateMatricies(transInMatrix, transOutMatrix, true);
			updateMatricies(obsInMatrix, obsOutMatrix, false);
		}
		else{
			updateMatricies(transInMatrix, transOutMatrix, true);
			updateMatricies(obsInMatrix, obsOutMatrix, false);
			for(int a = 0; a < 10; a++){
				int randomRow = rand.nextInt(1000);
				transitionInput = transInMatrix.row(randomRow);
				transitionOutput = transOutMatrix.row(randomRow);
				transitionModel.refine(transitionInput, transitionOutput, 0.1);
				observationInput = obsInMatrix.row(randomRow);
				observationOutput = obsOutMatrix.row(randomRow);
				observationModel.refine(observationInput, observationOutput, 0.1);
			}
		}
	}
	
	void print_error(double[] currState, double[] trueState, boolean transition)
	{
		double[] prediction = null;
		if(transition)
			prediction = transitionModel.forwardProp(currState);
		else
			prediction = observationModel.forwardProp(currState);
	    //double[] trueState = vec(j);
	    double sse = 0.0;
	    for(int a = 0; a < trueState.length; a++)
	    {
	        double diff = trueState[a] - prediction[a];
	        sse += diff * diff;
	    }
	    System.out.println(sse);
	}
	
	Matrix projectCoVar(double[] input){
		Matrix jac = transitionModel.jacobian(input);
		Matrix coVar = Matrix.multiply(errorCovariance, jac, false, false);
		System.out.println();
		coVar = Matrix.multiply(coVar, jac, false, true);
		coVar = Matrix.add(coVar, transNoise);
		return coVar;
	}
	
	Matrix computeKGain(Matrix H){
		Matrix PHt = new Matrix(4, 2);
		PHt = Matrix.multiply(errorCovariance, H, false, true);
		Matrix HPHt = new Matrix(2, 2);
		HPHt = Matrix.multiply(H, PHt, false, false);
		HPHt = Matrix.add(HPHt, obsNoise);
		HPHt.pseudoInverse();
		Matrix K = new Matrix(4, 2);
		K = Matrix.multiply(PHt, HPHt, false, false);
		return K;
	}
	
	void updateStateEstimate(Matrix K, ActionState a, double[] aprioriState){
		double[] obsPrediction = predictDistances(a);
		double[] trueDistances = model.bird.distance();
		double[] result = new double[numTransitionInputs];
		Matrix obsTemp = new Matrix(obsPrediction.length, 1);
		for(int i = 0; i < obsPrediction.length; i++)
			obsTemp.row(i)[0] = trueDistances[i] - obsPrediction[i];
		obsTemp = Matrix.multiply(K, obsTemp, false, false);
		for(int i = 0; i < result.length; i++)
			result[i] = obsTemp.row(i)[0];
		for(int i = 0; i < aprioriState.length; i++)
			stateEstimate[i] = result[i] + aprioriState[i];
	}
	
	void updateErrorCovariance(Matrix K, Matrix H){
		Matrix I = new Matrix(K.rows(), H.cols());
		I.setToIdentity();
		H = Matrix.multiply(K, H, false, false);
		//System.out.println("Intermediate step:");
		//H.print();
		for(int i = 0; i < I.rows(); i++)
			for(int j = 0; j < I.cols(); j++)
				I.row(i)[j] -= H.row(i)[j];
		errorCovariance = Matrix.multiply(I, errorCovariance, false, false);
		
	}
	
	boolean update() {
		//Get the current ActionState
		ActionState previousState = determineAS();
		double[] prevVec = vec(previousState);
		boolean alive = this.model.update();
		frame++;
		ActionState currentState = determineAS();
		double[] currVec = vec(currentState);
		//for(int i = 0; i < 4; i ++)
			//System.out.println(currVec[i]);
		//System.out.println();
		double[] currDist = model.bird.distance();
		/* Inject noise
		for(int a = 0; a < prevVec.length; a++){
			prevVec[a] = prevVec[a] + observationNoise * rand.nextGaussian();
			currVec[a] = currVec[a] + observationNoise * rand.nextGaussian();
		}
		*/

		if(frame < 1000){
			for(int i = 0; i < 4;i++)
				stateEstimate[i] = 0.0;
			if(frame % 10 == 0)
				System.out.println(frame / 10 + "%");
			//transitionModel.refine(prevVec, currVec, .1);
			//observationModel.refine(currVec, currDist, .1);
			refineWithMatricies(prevVec, currVec, currVec, currDist);
		}
		else{
			//Current vector for refining later...
			double[] refiningPreviousVector = stateEstimate;
			//Predict the next state
			double[] aprioriState = transitionModel.forwardProp(stateEstimate);
			
			//Using the previous covariance, determine an apriori estimation of covariance
			//Includes computing the Jacobian of the transitionModel
			errorCovariance = projectCoVar(currVec);
			//System.out.println("P-:");
			//errorCovariance.print();
			
			//Compute Jacobian of observationModel
			//double[] predictedDistances = observationModel.forwardProp(aprioriState);
			Matrix obsJacobian = observationModel.jacobian(aprioriState);
			//System.out.println("H:");
			//obsJacobian.print();
			
			//Compute Kalman gain
			Matrix K = new Matrix(2, 2);
			K = computeKGain(obsJacobian);
			//System.out.println("K:");
			//K.print();
			
			//Update the estimate of the current state
			updateStateEstimate(K, currentState, aprioriState);
			for(int i = 0; i < stateEstimate.length; i++){
				if(stateEstimate[i] > 1)
					stateEstimate[i] = 1;
				else if(stateEstimate[i] < -1)
					stateEstimate[i] = -1;
			}
			//System.out.println("State Estimate");
			//System.out.println(stateEstimate[0] + ", " + stateEstimate[1] + ", " + stateEstimate[2] + ", " + stateEstimate[3]);
			
			updateErrorCovariance(K, obsJacobian);
			//System.out.println("Final Error Covariance:");
			//errorCovariance.print();
			
			//Update estimate
			//updateEstimate(K, i);
			
			//if(frame == 150)
				//System.out.println("---------------------------------------------------------------------------------------------------------------");
			//Choose a refining method
			//for(int i = 0; i < prevVec.length; i++)
				//System.out.print("PrevVec[" + i + "]:" + prevVec[i] + " ");
			//System.out.println();
			//for(int i = 0; i < currVec.length; i++)
				//System.out.print("CurrVec[" + i + "]:" + currVec[i] + " ");
			//System.out.println();
			
			refineWithMatricies(refiningPreviousVector, stateEstimate, stateEstimate, observationModel.forwardProp(stateEstimate));
	    	//transitionModel.refine(refiningPreviousVector, stateEstimate, 0.1);
	    	//observationModel.refine(stateEstimate, observationModel.forwardProp(stateEstimate), 0.1);
	    	
			//System.out.print("State_Error: "); print_error(stateEstimate, vec(currentState), true);
			//System.out.print("Dist_Error: "); print_error(stateEstimate, currDist, false);
		}
		//If you get tired of clicking
		boolean flap = false;
		if(goHigher){
			if(frame % 8 == 0)
				flap = true;
		}
		else if(!goHigher)
			if(frame % 9 == 0){
				flap = true;
		}
		if(frame % 200 == 0)
			goHigher = !goHigher;
		if(flap)
			model.flap();
		return alive;
	}
	
	//Run Q-learning algorithm
	public void Kalman(){
		int ITERMAX = 500000;
		for(int count = 0; count < ITERMAX; count++){
			if(!update())
				return;
		}
		return;
	}
}






