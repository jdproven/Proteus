import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import javax.imageio.ImageIO;

public class NeuralNet {
	public ArrayList<Layer> layers;


	/// General-purpose constructor. (Starts with no layers. You must add at least one.)
	NeuralNet() {
		layers = new ArrayList<Layer>();
	}


	/// Copy constructor
	NeuralNet(NeuralNet that) {
		layers = new ArrayList<Layer>();
		for(int i = 0; i < that.layers.size(); i++) {
			layers.add(new Layer(that.layers.get(i)));
		}
	}


	/// Initializes the weights and biases with small random values
	void init(Random r) {
		for(int i = 0; i < layers.size(); i++) {
			layers.get(i).initWeights(r);
		}
	}


	/// Copies all the weights and biases from "that" into "this".
	/// (Assumes the corresponding topologies already match.)
	void copy(NeuralNet that) {
		if(layers.size() != that.layers.size())
			throw new IllegalArgumentException("Unexpected number of layers");
		for(int i = 0; i < layers.size(); i++) {
			layers.get(i).copy(that.layers.get(i));
		}
	}


	/// Feeds "in" into this neural network and propagates it forward to compute predicted outputs.
	double[] forwardProp(double[] in) {
		Layer l = null;
		for(int i = 0; i < layers.size(); i++) {
			l = layers.get(i);
			l.feedForward(in);
			l.activate();
			in = l.activation;
		}
		return l.activation;
	}


	/// Feeds the concatenation of "in1" and "in2" into this neural network and propagates it forward to compute predicted outputs.
	double[] forwardProp2(double[] in1, double[] in2) {
		Layer l = layers.get(0);
		l.feedForward2(in1, in2);
		l.activate();
		double[] in = l.activation;
		for(int i = 1; i < layers.size(); i++) {
			l = layers.get(i);
			l.feedForward(in);
			l.activate();
			in = l.activation;
		}
		return l.activation;
	}


	/// Backpropagates the error to the upstream layer.
	void backProp(double[] target) {
		int i = layers.size() - 1;
		Layer l = layers.get(i);
		l.computeError(target);
		l.deactivate();
		for(i--; i >= 0; i--) {
			Layer upstream = layers.get(i);
			l.feedBack(upstream.error);
			upstream.deactivate();
			l = upstream;
		}
	}


	/// Backpropagates the error from another neural network. (This is used when training autoencoders.)
	void backPropFromDecoder(NeuralNet decoder) {
		int i = layers.size() - 1;
		Layer l = decoder.layers.get(0);
		Layer upstream = layers.get(i);
		l.feedBack(upstream.error);
		l = upstream;
		//l.bendHinge(learningRate);
		l.deactivate();
		for(i--; i >= 0; i--) {
			upstream = layers.get(i);
			l.feedBack(upstream.error);
			//upstream.bendHinge(learningRate);
			upstream.deactivate();
			l = upstream;
		}
	}


	/// Updates the weights and biases
	void descendGradient(double[] in, double learningRate) {
		for(int i = 0; i < layers.size(); i++) {
			Layer l = layers.get(i);
			l.updateWeights(in, learningRate);
			in = l.activation;
		}
	}


	/// Keeps the weights and biases from getting too big
	void regularize(double learningRate, double lambda) {
		double amount = learningRate * lambda;
		double smallerAmount = 0.1 * amount;
		for(int i = 0; i < layers.size(); i++) {
			Layer lay = layers.get(i);
			//lay.straightenHinge(amount);
			lay.regularizeWeights(smallerAmount);
		}
	}


	/// Refines the weights and biases with on iteration of stochastic gradient descent.
	void refine(double[] in, double[] target, double learningRate) {
		forwardProp(in);
		backProp(target);
		descendGradient(in, learningRate);
	}


	/// Refines "in" with one iteration of stochastic gradient descent.
	void refineInputs(double[] in, double[] target, double learningRate) {
		forwardProp(in);
		backProp(target);
		layers.get(0).refineInputs(in, learningRate);
	}


	/// Returns the Jacobian matrix for this neural network at the specified input point.
	/// That is, element [i][j] of the returned matrix contains the partial derivative of
	/// the i{th} output value with respect to the j{th} input value.
	Matrix jacobian(double[] in) {
		int inputCount = layers.get(0).inputCount();
		Layer layOut = layers.get(layers.size() - 1);
		Matrix m = new Matrix(layOut.outputCount(), inputCount);
		forwardProp(in);
		for(int yy = 0; yy < layOut.outputCount(); yy++) {
			Vec.setAll(layOut.error, 0.0);
			layOut.error[yy] = 1.0;
			Layer l = layOut;
			l.deactivate();
			for(int i = layers.size() - 2; i >= 0; i--) {
				Layer upstream = layers.get(i);
				l.feedBack(upstream.error);
				upstream.deactivate();
				l = upstream;
			}
			l.feedBack(m.row(yy));
		}
		return m;
	}


	static void testMath() {
		NeuralNet nn = new NeuralNet();
		Layer l1 = new Layer(2, 3);
		l1.weights.row(0)[0] = 0.1;
		l1.weights.row(0)[1] = 0.0;
		l1.weights.row(0)[2] = 0.1;
		l1.weights.row(1)[0] = 0.1;
		l1.weights.row(1)[1] = 0.0;
		l1.weights.row(1)[2] = -0.1;
		l1.bias[0] = 0.1;
		l1.bias[1] = 0.1;
		l1.bias[2] = 0.0;
		nn.layers.add(l1);

		Layer l2 = new Layer(3, 2);
		l2.weights.row(0)[0] = 0.1;
		l2.weights.row(0)[1] = 0.1;
		l2.weights.row(1)[0] = 0.1;
		l2.weights.row(1)[1] = 0.3;
		l2.weights.row(2)[0] = 0.1;
		l2.weights.row(2)[1] = -0.1;
		l2.bias[0] = 0.1;
		l2.bias[1] = -0.2;
		nn.layers.add(l2);

		System.out.println("l1 weights:");
		l1.weights.print();
		System.out.println("l1 bias:");
		Vec.println(l1.bias);
		System.out.println("l2 weights:");
		l2.weights.print();
		System.out.println("l2 bias:");
		Vec.println(l2.bias);

		System.out.println("----Forward prop");
		double in[] = new double[2];
		in[0] = 0.3;
		in[1] = -0.2;
		double[] out = nn.forwardProp(in);
		System.out.println("activation:");
		Vec.println(out);

		System.out.println("----Back prop");
		double targ[] = new double[2];
		targ[0] = 0.1;
		targ[1] = 0.0;
		nn.backProp(targ);
		System.out.println("error 2:");
		Vec.println(l2.error);
		System.out.println("error 1:");
		Vec.println(l1.error);
		
		
		nn.descendGradient(in, 0.1);
		System.out.println("----Descending gradient");
		System.out.println("l1 weights:");
		l1.weights.print();
		System.out.println("l1 bias:");
		Vec.println(l1.bias);
		System.out.println("l2 weights:");
		l2.weights.print();
		System.out.println("l2 bias:");
		Vec.println(l2.bias);

		if(Math.abs(l1.weights.row(0)[0] - 0.10039573704287) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		if(Math.abs(l1.weights.row(0)[1] - 0.0013373814241446) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		if(Math.abs(l1.bias[1] - 0.10445793808048) > 0.0000000001)
			throw new IllegalArgumentException("failed");
		System.out.println("passed");
	}

	public static void testVisual() throws Exception {
		// Make some data
		Random rand = new Random(1234);
		Matrix features = new Matrix();
		features.setSize(1000, 2);
		Matrix labels = new Matrix();
		labels.setSize(1000, 2);
		for(int i = 0; i < 1000; i++) {
			
			double x = rand.nextDouble() * 2 - 1;
			double y = rand.nextDouble() * 2 - 1;
			features.row(i)[0] = x;
			features.row(i)[1] = y;
			labels.row(i)[0] = (y < x * x ? 0.9 : 0.1);
			labels.row(i)[1] = (x < y * y ? 0.1 : 0.9);
		}

		// Train on it
		NeuralNet nn = new NeuralNet();
		nn.layers.add(new Layer(2, 30));
		nn.layers.add(new Layer(30, 2));
		nn.init(rand);
		int iters = 10000000;
		double learningRate = 0.01;
		double lambda = 0.0001;
		for(int i = 0; i < iters; i++) {
			int index = rand.nextInt(features.rows());
			nn.regularize(learningRate, lambda);
			nn.refine(features.row(index), labels.row(index), 0.01);
			if(i % 1000000 == 0)
				System.out.println(Double.toString(((double)i * 100)/ iters) + "%");
		}

		// Visualize it
		for(int i = 0; i < nn.layers.size(); i++) {
			System.out.print("Layer " + Integer.toString(i) + ": ");
//			for(int j = 0; j < nn.layers.get(i).hinge.length; j++)
//				System.out.print(Double.toString(nn.layers.get(i).hinge[j]) + ", ");
			System.out.println();
		}
		BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
		double[] in = new double[2];
		for(int y = 0; y < 100; y++) {
			for(int x = 0; x < 100; x++) {
				in[0] = ((double)x) / 100 * 2 - 1;
				in[1] = ((double)y) / 100 * 2 - 1;
				double[] out = nn.forwardProp(in);
				int g = Math.max(0, Math.min(255, (int)(out[0] * 256)));
				image.setRGB(x, y, new Color(g, g, g).getRGB());
				g = Math.max(0, Math.min(255, (int)(out[1] * 256)));
				image.setRGB(x, y + 100, new Color(g, g, g).getRGB());
			}
		}
		ImageIO.write(image, "png", new File("viz.png"));
	}
}
