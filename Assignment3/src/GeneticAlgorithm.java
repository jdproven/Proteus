import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GeneticAlgorithm {

	static ArrayList<Chromosome> population = new ArrayList<Chromosome>();
	 //Pass buffered image vs global variable?
	static Random rand = new Random();
	
	GeneticAlgorithm(){
		for (int i = 0; i < 30; i++)
			population.add(new Chromosome());
	}

	Void mutate_add(){
		int r = rand.nextInt(population.size());
		int step = rand.nextInt(15) + 1;
		double direct = rand.nextDouble() * (2 * Math.PI);
		Chromosome c = population.get(r);
		c.insert(step, direct);
		return null;
	}

	Void tournament(BufferedImage image){
		int r = rand.nextInt(population.size());
		int location;
		Chromosome c1 = population.get(r);
		r = rand.nextInt(population.size());
		Chromosome c2 = population.get(r);
		//if(c1.chromosome.size() != 0 && c2.chromosome.size() != 0)
			//System.out.println("HI");
		int c1Cost = evaluate_Cost(image, c1);
		int c2Cost = evaluate_Cost(image, c2);
		double d = rand.nextDouble();
		if(d <= .95){
			if(c1Cost < c2Cost)
				location = population.indexOf(c2);
			else
				location = population.indexOf(c1);
		}
		else{
			if(c1Cost < c2Cost)
				location = population.indexOf(c1);
			else
				location = population.indexOf(c2);
		}
		//System.out.println("c1Cost:" + c1Cost + ", c2Cost:" + c2Cost);
		//System.out.println("d = " + d);
		//System.out.println("removed:" + location);
		population.remove(location);	//chromosome death
		replace_Death();
		
		//System.out.println();
		//System.out.println("POP SIZE:" + population.size());
		//System.out.println();
		
		return null;
	}
	
	Void replace_Death(){
		int mom = rand.nextInt(population.size());
		int dad = rand.nextInt(population.size());
		Chromosome mother = population.get(mom);
		Chromosome father = population.get(dad);
		Chromosome child = new Chromosome();
		if((mother.chromosome.size() == 0) && (father.chromosome.size() == 0)){
			population.add(child);
			return null;
		}
		int randomSplicePoint = 0;
		if(mother.chromosome.size() != 0)
			randomSplicePoint = rand.nextInt(mother.chromosome.size());
		//create new step direction so that if you mutuate the parent, it doesnt also mutate the child
		Step_direction toAdd;// = new Step_direction(0,0.0);
		for(int i = 0; i < randomSplicePoint; i++){
			toAdd = new Step_direction(mother.chromosome.get(i));
			child.insertSD(toAdd);
		}
		if(father.chromosome.size() < randomSplicePoint){
			//do nothing because the father chromosome is not long enough
		}
		else
			for(int j = randomSplicePoint; j < father.chromosome.size(); j++){
				toAdd = new Step_direction(father.chromosome.get(j));
				child.insertSD(toAdd);
			}
		population.add(child);	//add child to population
		//child.describe();
		
		return null;
	}
	
	Void mutate_drop(){
		int r = rand.nextInt(population.size());
		Chromosome c = population.get(r);
		c.remove();	//have chromosome object pick a random step_direction to drop
		return null;
	}
	
	Void mutate_direction(){
		int r = rand.nextInt(population.size());
		Chromosome c = population.get(r);
		c.changeRandomAngle();
		return null;
	}
	
	void mutate_step(){
		int r = rand.nextInt(population.size());
		Chromosome c = population.get(r);
		c.changeRandomStep();
	}

	void something_happens(BufferedImage image){
		Double d = rand.nextDouble();
		if(d < .1)
			mutate_add();
		else if( d < .15)
			mutate_step();
		else if(d < .9)
			tournament(image);
		else if(d < .95)
			mutate_drop();
		else if(d < 1)
			mutate_direction();
	}

	Void find_Path(){
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("terrain.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < 20000; i ++){
			//System.out.println("count:" + i);
			something_happens(image);
			Chromosome c;// = new Chromosome();
			int printCost = 300000;
			int cost;
			//if(i % 500 == 0)
				//System.out.println("count:" + i);
			if(i % 1000 == 0)
				for(int j = 0; j < 30; j++){
					c = population.get(j);
					cost = evaluate_Cost(image, c);
					if(cost < printCost)
						printCost = cost;
					System.out.println(cost);
				}
		}
		return null;
	}
	
	int evaluate_Cost(BufferedImage image, Chromosome c){
		int cost = 0;
		int stepCost = 0;
		int myX = 100;
		int myY = 100;
		int steps = 0;
		int rgb = 0;
		double xPosition = 100;
		double yPosition = 100;
		double direction = 0;
		int chromoSize = c.chromosome.size();	//is equal to the number of Step_directions in the chromosome
		for(int i = 0; i < chromoSize; i++){	//for every step_direction...
			Step_direction sd = c.chromosome.get(i);
			steps = sd.steps;
			direction = sd.direction;
			for(int j = 0; j < steps; j++){	//for every step...
				xPosition = xPosition + Math.cos(direction);
				yPosition = yPosition + Math.sin(direction);
				myX = (int)xPosition; 
				myY = (int)yPosition;
				rgb = image.getRGB(myX, myY);
				stepCost = (rgb >> 8) & 0xFF;	//individual step cost
				cost = cost + stepCost;		//running total
			}
		}
		return cost + 500 * (int)Math.sqrt((myX - 400) * (myX - 400) + (myY - 400) * (myY - 400));
		//return rand.nextInt(500);
	}
	
	public static class Chromosome{
		ArrayList<Step_direction> chromosome;
		
		Chromosome(){
			chromosome = new ArrayList<Step_direction>();
		}
		
		void insert(int step, double direction){
			Step_direction sd = new Step_direction(step, direction);
			chromosome.add(sd);
		}
		
		void insertSD(Step_direction sd){
			chromosome.add(sd);
		}
		
		void remove(){
			int location = 0;
			if(chromosome.size() != 0){
				location = rand.nextInt(chromosome.size());
				chromosome.remove(location);
			}
		}
		
		void changeRandomAngle(){
			double direct = rand.nextDouble() * (2 * Math.PI);
			int r = 0;
			if(chromosome.size() != 0){
				r = rand.nextInt(chromosome.size());
				Step_direction sd = chromosome.get(r);
				sd.direction = direct;
			}
			
		}
		
		void changeRandomStep(){
			int r;
			int step = rand.nextInt(15) + 1;
			if(chromosome.size() != 0){
				r = rand.nextInt(chromosome.size());
				Step_direction sd = chromosome.get(r);
				sd.steps = step;
			}
		}
		
		void describe(){
			int size = chromosome.size();
			for(int i = 0; i < size; i++){
				Step_direction sd = chromosome.get(i);
				System.out.println("Steps:" + sd.steps + ", direction:" + sd.direction);
			}
		}
	}
	
	static class Step_direction{
		double direction;
		int steps;
		
		Step_direction(int step, double dir){
			direction = dir;
			steps = step;
		}
		
		Step_direction(Step_direction that) {
			this.direction = that.direction;
			this.steps = that.steps;
		}
	}

	
	public static void main(String[] args) {
		GeneticAlgorithm ga = new GeneticAlgorithm();
		ga.find_Path();
		Chromosome c = population.get(0);
		for(int i = 0; i < 30; i++){
			c = population.get(i);
			System.out.println("Chromosome(" + i + ") is...");
			c.describe();
		}
			
		
		/*population.add(new Chromosome());
		Chromosome c = population.get(0);
		c.insert(5, 25.0);
		c.insert(6, 27.0);
		c.insert(30, 77.0);
		c.changeRandomAngle();
		c.changeRandomStep();
		c.describe();*/

	}

}
