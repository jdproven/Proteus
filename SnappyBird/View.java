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
	Image image_bird1;
	Image image_bird2;
	Image image_tube_up;
	Image image_tube_down;

	View(Model m) throws IOException {
		this.model = m;
		this.image_bird1 = ImageIO.read(new File("bird1.png"));
		this.image_bird2 = ImageIO.read(new File("bird2.png"));
		this.image_tube_up = ImageIO.read(new File("tube_up.png"));
		this.image_tube_down = ImageIO.read(new File("tube_down.png"));
	}

	public void paintComponent(Graphics g) {
		// Draw the sky
		g.setColor(Color.cyan);
		g.fillRect(0, 0, 500, 500);

		// Draw the bird
		Bird bird = model.bird;
		if(bird.time_since_flap < 4)
			g.drawImage(this.image_bird2, bird.x, bird.y, null);
		else
			g.drawImage(this.image_bird1, bird.x, bird.y, null);

		// Draw the tubes
		for(int i = 0; i < model.tubes.size(); i++) {
			Tube t = model.tubes.get(i);
			if(t.up)
				g.drawImage(this.image_tube_up, t.x, t.y, null);
			else
				g.drawImage(this.image_tube_down, t.x, t.y - 400, null);
		}
	}
}
