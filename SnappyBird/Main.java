import java.util.Random;

class Main {
	public static void main(String[] args) {

		// Make a neural network
		NeuralNet nn = new NeuralNet();
		nn.layers.add(new Layer(3, 16));
		nn.layers.add(new Layer(16, 2));
		Random rand = new Random(0);
		nn.init(rand);

		// Train it to approximate some simple functions
		System.out.println("Training...");
		double[] in = new double[3];
		double[] out = new double[2];
		for(int i = 0; i < 100000; i++)
		{
			in[0] = rand.nextDouble();
			in[1] = rand.nextDouble();
			in[2] = rand.nextDouble();
			out[0] = (in[0] + in[1] + in[2]) / 3.0;
			out[1] = (in[0] * in[1] - in[2]);
			nn.refine(in, out, 0.02);
		}

		// Test it
		System.out.println("Testing...");
		double sse = 0.0;
		int testPatterns = 100;
		for(int i = 0; i < testPatterns; i++)
		{
			in[0] = rand.nextDouble();
			in[1] = rand.nextDouble();
			in[2] = rand.nextDouble();
			double[] prediction = nn.forwardProp(in);
			out[0] = (in[0] + in[1] + in[2]) / 3.0;
			out[1] = (in[0] * in[1] - in[2]);
			double err0 = out[0] - prediction[0];
			double err1 = out[1] - prediction[1];
			sse += (err0 * err0) + (err1 * err1);
		}
		double rmse = Math.sqrt(sse / testPatterns);
		if(rmse < 0.05)
			System.out.println("Passed.");
		else
			System.out.println("Failed!!! Got " + Double.toString(rmse));
	}
}
