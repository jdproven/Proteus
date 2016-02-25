# Proteus

Each file in this repo is a project I have worked on within about the last year.  I believe they are reflective of my current coding capabilities.  Brief descriptions of each project are listed in chronological order of completion and are as follows:

Assgn1_aStar:

This project contains one file called Assgn1_astar.java that computes the lowest "cost" of a pixel-by-pixel traversal of an image from pixel (100,100) to pixel (400,400) where the "cost" of moving from pixel a to pixel b is the grayscale of pixel b (thus, darker pixels are considered to be lower cost).  The project also contains two graphical representations; the first is terrain.png which shows the image before analyzation, and the second is path.png which shows the optimal path outlined in red.  The green rings represent equal-cost paths.

Assignment3:

This project contins one file called GeneticAlgorithm.java that expands on the previous project by implementing a genetic algorithm on a random path across the terrain image.  Unlike the A* project, the algorithm is not limited to traversing one pixel at a time; instead the algorithm can take multiple "steps" in any direction to traverse multiple pixels on a continuous range of directions.  These two values, number of "steps" and direction, were the two terms that were allowed to mutate to attempt to find the best path across the terrain.

SnappyBird:

This is a game my professor gave us the framework for based off of the once-popular game FlappyBird.  The professor provided us with the files and code to actually play a game of SnappyBird (being able to click on the screen to make the bird flap through the upcoming pipes) in a Model-View-Controller format.  Our original assignment for this game was to implement an AI technique called Q-Learning and modify other files as necessary to complete this, so the most pertinent file in this project is called QLearningController.java, but most files were modified in some way.  This eventually developed into replacing the Q-Table with a stochastic gradient-descent neural network that he provided for us.  It was made clear to us that we did not need to understand how to create our own neural network, only that we be able to interface with it to make it learn.  The concepts we learned and practiced in this project were to prepare us for the last project...

KalmanFilter:

In short, this project is an implementation of the Extended Kalman Filter that uses a neural network as the observation function.  The main files I developed were Game.java and Controller.java since most things that needed to be modified in other files had already been attended to in the previous assignments.  This was, by far, the most interesting problem that I was tasked with solving during my undergraduate career both because of its practical applications (and historical use of the Kalman Filter), and because of the intensity and caliber of programming capabilites required to solve it.
