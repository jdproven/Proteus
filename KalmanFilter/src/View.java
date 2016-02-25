import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;
import java.io.File;
import java.awt.Color;

class View extends JPanel {
	Model model;
	Controller con;
	Image image_bird1;
	Image image_bird2;
	Image image_tube_up;
	Image image_tube_down;

	View(Model m, Controller c) throws IOException {
		this.model = m;
		this.image_bird1 = ImageIO.read(new File("bird1.png"));
		this.image_bird2 = ImageIO.read(new File("bird2.png"));
		this.image_tube_up = ImageIO.read(new File("tube_up.png"));
		this.image_tube_down = ImageIO.read(new File("tube_down.png"));
		this.con = c;
	}

	public void paintComponent(Graphics g) {
		// Draw the sky
		g.setColor(Color.cyan);
		g.fillRect(0, 0, 500, 500);
		g.setColor(Color.black);

		// Draw the bird
		Bird bird = model.bird;
		if(bird.time_since_flap < 4)
			g.drawImage(this.image_bird2, bird.x, bird.y, null);
		else
			g.drawImage(this.image_bird1, bird.x, bird.y, null);
		double radius = 0.0;
		for(int i = 0; i < con.errorCovariance.rows(); i++)
			radius += con.errorCovariance.row(i)[i];
		radius /= con.errorCovariance.rows();
		g.drawOval((int)(con.stateEstimate[3]*1000), (int)(con.stateEstimate[1]*1000), 100, 100);
	}
}
