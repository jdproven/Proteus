import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.io.IOException;
import java.awt.event.WindowEvent;
import java.awt.Robot;
import java.util.Scanner;

public class Game extends JFrame implements ActionListener {
	Model model;
	View view;
	Timer timer;
	int ttl;
	Robot robot;
	int frame;
	Controller c;
	static double std;
	
	public Game() throws IOException, Exception {
		this.model = new Model();
		//Make a new Controller and give it a model to work with
		c = new Controller(this.model);
		this.view = new View(this.model, this.c);
		addMouseListener(c);
		this.setTitle("Snappy Bird");
		this.setSize(500, 500);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.robot = new Robot();
		timer = new Timer(30, this);
		timer.start();
		std = 0.0;
		c.error = std/1000;
	}
	
	public void train(){
		timer.stop();
		for(int i = 0; i < 10000; i++){
			c.Kalman();
			this.model.reset();
			if(i%100 == 0)
				System.out.println(i/100 + "%");
			c.gameCount++;
		}
		timer.start();// Indirectly calls actionPerformed at regular intervals
	}
	
	public void actionPerformed(ActionEvent evt) {
		//train();
		//c.exploreRate = 0.0;
		if(!this.c.update()){
			this.model.reset();
			System.out.println();
		}
		//robot.mouseMove(470 + (int)(20 * Math.cos(frame)), 70 + (int)(20 * Math.sin(frame++)));
		this.model.update();
		if(c.frame > 1000){
			view.invalidate();
			repaint(); // Indirectly calls View.paintComponent
		}
	}

	public static void main(String[] args) throws IOException, Exception {
		//System.out.print("Specify a standard deviation (in number of pixels):");
		//Scanner sc = new Scanner(System.in);
		//std = sc.nextDouble();
		std = 500;
		new Game();
	}
}










