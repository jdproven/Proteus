import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

public class Assgn1_astar {	
	static class MyState implements Comparable<MyState> {
	 public double cost;
	 MyState parent;
	 public int x;
	 public int y;

 	MyState(double cost, MyState par, int my_x, int my_y) {
 		//cost = cost;
 		par = parent;
 		x = my_x;
 		y = my_y;
 	}
 	
 	boolean isEqual(MyState currentState, MyState goalState){
 		if(currentState.x == goalState.x && currentState.y == goalState.y)  //same x and y means same pixel
 			return true;
 		else
 			return false;
 	}
 
 	public int compareTo(MyState other) {
 		int other_hueristic = 0;
 		int this_hueristic = 0;
 		other_hueristic = 14 * (Math.abs(other.x-400) + Math.abs(other.y-400));
 		this_hueristic = 14 * (Math.abs(this.x-400) + Math.abs(this.y-400));
 		if(this.cost + this_hueristic < other.cost + other_hueristic)
 			return -1;
 		else if(this.cost + this_hueristic > other.cost + other_hueristic)
 			return 1;
 		else
 			return 0;
 	}

 	public MyStateIterator iterator() {
 		return new MyStateIterator(this); //create a new iterator for this specific MyState
 	}
 
 	public int ActionCost(MyState currentState, MyState child, BufferedImage image) {
	 int cost = 0;
	 
	 int rgb = image.getRGB(child.x, child.y);
	 cost = (rgb >> 8) & 0xFF;
	 return cost;
 }  
}

	static class MyNameComp implements Comparator<MyState>{
		public int compare(MyState e1, MyState e2) {
			if(e1.y < e2.y) return -1;
			else if(e1.y > e2.y) return 1;
			else if(e1.x < e2.x) return -1;
			else if(e1.x > e2.x) return 1;
			else return 0;
		}
	}

	static class MyStateIterator implements Iterator<MyState> {
		MyState m_s;
		int i;
		int counter;
		int max;
 
		MyStateIterator(MyState s){
			m_s = s;
			counter = 0;
			max = 499;
			
			if(s.x == 0 && s.y == 0)
				i = 0;
			else if(s.x == max && s.y == 0)
				i = 1;
			else if(s.x == 0 && s.y == max)
				i = 2;
			else if(s.x == max && s.y == max)
				i = 3;
			else if(s.x == 0)
				i = 4;
			else if(s.x == max)
				i = 5;
			else if(s.y == 0)
				i = 6;
			else if(s.y == max)
				i = 7;
			else
				i = 8;
		}
 
		public boolean hasNext(){
			if(i == 9)  //all neighboring states have been visited
				return false;
			else
				return true;
		}
		
		public void remove(){
			//do nothing
		}
 
		public MyState next(){
			if(i == 0){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y + 1);  //return pixel below
				}
				else if(counter == 1){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x + 1, m_s.y);  // return pixel to the right
				}	
				else
					return m_s;
			}
 
			else if(i == 1){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x - 1, m_s.y); //return pixel to the left
				}
				else if(counter == 1){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x, m_s.y + 1);  //return pixel below
				}
				else
					return m_s;
			}
 
			else if(i == 2){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y - 1);  //return pixel above
				}
				else if(counter == 1){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x + 1, m_s.y);  // return pixel to the right
				}
				else
					return m_s;
			}
			
			else if(i == 3){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y - 1);  //return pixel above
				}
				else if(counter == 1){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x - 1, m_s.y); //return pixel to the left
				}
				else
					return m_s;
			}
 
			else if(i == 4){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y - 1);  //return pixel above
				}
				else if(counter == 1){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y + 1);  //return pixel below
				}
				else if(counter == 2){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x + 1, m_s.y);  // return pixel to the right
				}
				else
					return m_s;
			}
 
			else if(i == 5){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y - 1);  //return pixel above
				}
				else if(counter == 1){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y + 1);  //return pixel below
				}
				else if(counter == 2){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x - 1, m_s.y); //return pixel to the left
				}
				else
					return m_s;
			}
 
			else if(i == 6){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y + 1);  //return pixel below
				}
				else if(counter == 1){
					counter++;
					return new MyState(0, m_s, m_s.x - 1, m_s.y); //return pixel to the left
				}
				else if(counter == 2){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x + 1, m_s.y);  // return pixel to the right
				}
				else
					return m_s;
			}
 
			else if(i == 7){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y - 1);  //return pixel above
				}
				else if(counter == 1){
					counter++;
					return new MyState(0, m_s, m_s.x - 1, m_s.y); //return pixel to the left
				}
				else if(counter == 2){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x + 1, m_s.y);  // return pixel to the right
				}
				else
					return m_s;
			}
 
			else if(i == 8){
				if(counter == 0){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y - 1);  //return pixel above
				}
				else if(counter == 1){
					counter++;
					return new MyState(0, m_s, m_s.x, m_s.y + 1);  //return pixel below
				}
				else if(counter == 2){
					counter++;
					return new MyState(0, m_s, m_s.x - 1, m_s.y); //return pixel to the left
				}
				else if(counter == 3){
					counter++;
					i = 9;
					return new MyState(0, m_s, m_s.x + 1, m_s.y);  // return pixel to the right
				}
				else
					return m_s;
			}
 
			else
				return m_s;
 
		}
	}
 
	static int aStar(MyState startState, MyState goalState, BufferedImage image) throws Exception {
		//System.out.println("State cost:" + startState.cost + " State x:" + startState.x + " State y:" + startState.y);
		PriorityQueue<MyState> frontier = new PriorityQueue<MyState>(); // lowest cost comes out first
		TreeSet<MyState> beenThere = new TreeSet<MyState>(new MyNameComp());
		startState.cost = 0.0;
		startState.parent = null; //the first pixel will have no parent
		beenThere.add(startState); //mark startState as having been examined
		frontier.add(startState);
		int counter = 1;
		while(frontier.size() > 0) {
			MyState s = (MyState)frontier.poll(); //retrieve the next pixel to be examined
			counter++;
			if(counter % 5000 < 1000){
				Color myGreen = new Color(0, 255, 0);
		        int rgb=myGreen.getRGB();
		        image.setRGB(s.x, s.y, rgb);
			}

			if(s.isEqual(s, goalState)){ //if this pixel is the goal pixel, end
				return counter;
			}
			beenThere.add(s) ; //set pixel to identify as having been visited
			MyStateIterator it = s.iterator();
			while(it.hasNext()) {
				MyState child = it.next(); //compute the next state.  same as transition(s,a) in psudocode
				int acost = child.ActionCost(s, child, image); //compute the total cost of the action
				if(beenThere.contains(child)) { //if that location is in beenthere...
					MyState oldChild = beenThere.ceiling(child);
					if(s.cost + acost < oldChild.cost) {
						oldChild.cost = s.cost + acost;
						oldChild.parent = s;
					}
				}
				else {
					child.cost = s.cost + acost;
					child.parent = s;
					frontier.add(child);
					beenThere.add(child);
				}
			}
		}
		throw new Exception("There is no path to the goal");
	}

	static byte[] terrain;
 
	public static void main(String[] args) throws Exception {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("terrain.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		terrain = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
 
        MyState start = new MyState(0, null, 100, 100);
        MyState goal = new MyState(0, null, 400, 400);
        int real_goal = aStar(start, goal, image);
        
        System.out.println("astar1=" + real_goal);
 
	}
}