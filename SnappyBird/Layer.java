import java.util.Random;

public class Layer {
	public Matrix weights; // rows are inputs, cols are outputs
	public double[] bias;
	public double[] net;
	public double[] activation;
	public double[] error;


	Layer(int inputs, int outputs) {
		weights = new Matrix();
		weights.setSize(inputs, outputs);
		bias = new double[outputs];
		net = new double[outputs];
		activation = new double[outputs];
		error = new double[outputs];
	}


	Layer(Layer that) {
		weights = new Matrix(that.weights);
		bias = Vec.copy(that.bias);
		net = Vec.copy(that.net);
		activation = Vec.copy(that.activation);
		error = Vec.copy(that.error);
	}


	void copy(Layer src) {
		if(src.weights.rows() != weights.rows() || src.weights.cols() != weights.cols())
			throw new IllegalArgumentException("mismatching sizes");
		weights.copyBlock(src.weights, 0, 0, src.weights.rows(), src.weights.cols());
		for(int i = 0; i < bias.length; i++) {
			bias[i] = src.bias[i];
		}
	}


	int inputCount() { return weights.rows(); }
	int outputCount() { return weights.cols(); }


	void initWeights(Random r) {
		double dev = Math.max(0.3, 1.0 / weights.rows());
		for(int i = 0; i < weights.rows(); i++) {
			double[] row = weights.row(i);
			for(int j = 0; j < weights.cols(); j++) {
				row[j] = dev * r.nextGaussian();
			}
		}
		for(int j = 0; j < weights.cols(); j++) {
			bias[j] = dev * r.nextGaussian();
		}
	}


	void feedForward(double[] in) {
		if(in.length != weights.rows())
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(in.length) + " != " + Integer.toString(weights.rows()));
		for(int i = 0; i < net.length; i++)
			net[i] = bias[i];
		for(int j = 0; j < weights.rows(); j++) {
			double v = in[j];
			double[] w = weights.row(j);
			for(int i = 0; i < weights.cols(); i++)
				net[i] += v * w[i];
		}
	}


	void feedForward2(double[] in1, double[] in2) {
		if(in1.length + in2.length != weights.rows())
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(in1.length) + " + " + Integer.toString(in2.length) + " != " + Integer.toString(weights.rows()));
		for(int i = 0; i < net.length; i++)
			net[i] = bias[i];
		for(int j = 0; j < in1.length; j++) {
			double v = in1[j];
			double[] w = weights.row(j);
			for(int i = 0; i < weights.cols(); i++)
				net[i] += v * w[i];
		}
		for(int j = 0; j < in2.length; j++) {
			double v = in2[j];
			double[] w = weights.row(in1.length + j);
			for(int i = 0; i < weights.cols(); i++)
				net[i] += v * w[i];
		}
	}


	void activate() {
		for(int i = 0; i < net.length; i++) {
			activation[i] = Math.tanh(net[i]);
		}
	}


	void computeError(double[] target) {
		if(target.length != activation.length)
			throw new IllegalArgumentException("size mismatch. " + Integer.toString(target.length) + " != " + Integer.toString(activation.length));
		for(int i = 0; i < activation.length; i++) {
			if(target[i] < -1.0 || target[i] > 1.0)
				throw new IllegalArgumentException("target value out of range for the tanh activation function");
			error[i] = target[i] - activation[i];
		}
	}


	void deactivate() {
		for(int i = 0; i < error.length; i++) {
			error[i] *= (1.0 - activation[i] * activation[i]);
		}
	}


	void feedBack(double[] upstream) {
		if(upstream.length != weights.rows())
			throw new IllegalArgumentException("size mismatch");
		for(int j = 0; j < weights.rows(); j++) {
			double[] w = weights.row(j);
			double d = 0.0;
			for(int i = 0; i < weights.cols(); i++) {
				d += error[i] * w[i];
			}
			upstream[j] = d;
		}
	}


	void refineInputs(double[] inputs, double learningRate) {
		if(inputs.length != weights.rows())
			throw new IllegalArgumentException("size mismatch");
		for(int j = 0; j < weights.rows(); j++) {
			double[] w = weights.row(j);
			double d = 0.0;
			for(int i = 0; i < weights.cols(); i++) {
				d += error[i] * w[i];
			}
			inputs[j] += learningRate * d;
		}
	}


	void updateWeights(double[] in, double learningRate) {
		for(int i = 0; i < bias.length; i++) {
			bias[i] += learningRate * error[i];
		}
		for(int j = 0; j < weights.rows(); j++) {
			double[] w = weights.row(j);
			double x = learningRate * in[j];
			for(int i = 0; i < weights.cols(); i++) {
				w[i] += x * error[i];
			}
		}
	}


	// Applies both L2 and L1 regularization to the weights and bias values
	void regularizeWeights(double lambda) {
		for(int i = 0; i < weights.rows(); i++) {
			double[] row = weights.row(i);
			for(int j = 0; j < row.length; j++) {
				row[j] *= (1.0 - lambda);
				if(row[j] < 0.0)
					row[j] += lambda;
				else
					row[j] -= lambda;
			}
		}
		for(int j = 0; j < bias.length; j++) {
			bias[j] *= (1.0 - lambda);
			if(bias[j] < 0.0)
				bias[j] += lambda;
			else
				bias[j] -= lambda;
		}
	}
}

