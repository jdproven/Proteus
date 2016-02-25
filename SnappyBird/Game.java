import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.io.IOException;
import java.awt.event.WindowEvent;
import java.awt.Robot;
import java.util.HashMap;

public class Game extends JFrame implements ActionListener {
	Model model;
	View view;
	Timer timer;
	int ttl;
	Robot robot;
	int frame;
	QLearningController qlearn;
	
	public Game() throws IOException, Exception {
		this.model = new Model();
		//Controller controller = new Controller(this.model);
		this.view = new View(this.model);
		//addMouseListener(controller);
		//addKeyListener(controller);
		this.setTitle("Snappy Bird");
		this.setSize(500, 500);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.robot = new Robot();
		//Make a new QLearningController and give it a model to work with
		qlearn = new QLearningController(this.model);
		timer = new Timer(30, this);
		timer.stop();
		for(int i = 0; i < 100000; i++){
			qlearn.QLearning();
			this.model.reset();
			if(i%1000 == 0)
				System.out.println(i/1000 + "%");
			qlearn.gameCount++;
		}
		timer.start();// Indirectly calls actionPerformed at regular intervals
	}
	public void actionPerformed(ActionEvent evt) {
		if(!this.qlearn.update()){
			this.model.reset();
			System.out.println();
		}
		qlearn.exploreRate = 0.0;
		//robot.mouseMove(470 + (int)(20 * Math.cos(frame)), 70 + (int)(20 * Math.sin(frame++)));
		view.invalidate();
		repaint(); // Indirectly calls View.paintComponent
	}
/*
	public void actionPerformed(ActionEvent evt) {
		if(ttl > 0)
		{
			if(--ttl == 0)
			{
				timer.stop();
				dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			}
			return;
		}
		if(!this.model.update())
			ttl = 50;
		//robot.mouseMove(470 + (int)(20 * Math.cos(frame)), 70 + (int)(20 * Math.sin(frame++)));
		view.invalidate();
		repaint(); // Indirectly calls View.paintComponent
	}
*/
	public static void main(String[] args) throws IOException, Exception {
		new Game();
	}
}










