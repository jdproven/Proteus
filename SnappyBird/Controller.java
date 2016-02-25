import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

class Controller implements MouseListener, KeyListener
{
	Model model;

	Controller(Model m) throws Exception {
		this.model = m;
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
}
