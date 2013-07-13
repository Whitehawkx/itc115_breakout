package com.jsteadman.Breakout;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.graphics.GRoundRect;
import acm.program.GraphicsProgram;
import acm.util.RandomGenerator;

@SuppressWarnings("serial")
public class Breakout extends GraphicsProgram {

	// constants for applet size
	private final int APPLET_WIDTH = 400;
	private final int APPLET_HEIGHT = 600;

	// constants for paddle
	private final int PADDLE_Y_OFFSET = 30;
	private final int PADDLE_WIDTH = 60;
	private final int PADDLE_HEIGHT = 10;
	private final int PADDLE_X = APPLET_WIDTH / 2 - PADDLE_WIDTH / 2;
	private final int PADDLE_Y = APPLET_HEIGHT - PADDLE_Y_OFFSET;

	// constants for bricks
	private int BRICK_X = 2;
	private int BRICK_Y = 70;
	private final int BRICK_ROWS = 10;
	private final int BRICK_COLUMNS = 10;
	private final int BRICK_SEPARATION = 4;
	private final int BRICK_HEIGHT = 8;
	private final int BRICK_WIDTH = (APPLET_WIDTH / BRICK_COLUMNS)
			- BRICK_SEPARATION;

	// constants for ball
	final private int BALL_RADIUS = 10;
	final private int BALL_X = APPLET_WIDTH / 2 - BALL_RADIUS;
	final private int BALL_Y = APPLET_HEIGHT / 2 - BALL_RADIUS;
	final private int BALL_DIAMETER = 2 * BALL_RADIUS;

	private static Color BRICK_COLOR;
	private static Color BORDER_COLOR;
	
	// constants for counter and keeping score
	private int BRICK_COUNTER = BRICK_COLUMNS * BRICK_ROWS;
	private int POINTS;
	GLabel wordScore;
	GLabel displayPoints;

	// constants for each object
	private GRect brick;
	private GOval ball;
	private GRoundRect paddle;

	// constants for ball velocity
	private double ballVX = 3;
	private double ballVY;

	// random generator used to determine initial ball direction
	RandomGenerator rand = new RandomGenerator();

	public void run() {
		setSize(APPLET_WIDTH, APPLET_HEIGHT);
		wordScore();
		createBricks();
		theBall();
		thePaddle();
		addKeyListeners();
		waitForClick();
		moveBall();
	}

	public void createBricks() {

		/*
		 * adjust the color of the bricks based every two rows
		 * 
		 * TODO - create an array using % instead
		 */

		for (int j = 1; j <= BRICK_ROWS; j++) {
			if (j <= 2) {
				BRICK_COLOR = Color.RED;
				BORDER_COLOR = Color.RED;
			} else if (j > 2 && j <= 4) {
				BRICK_COLOR = Color.ORANGE;
				BORDER_COLOR = Color.ORANGE;
			} else if (j > 4 && j <= 6) {
				BRICK_COLOR = Color.YELLOW;
				BORDER_COLOR = Color.YELLOW;
			} else if (j > 6 && j <= 8) {
				BRICK_COLOR = Color.GREEN;
				BORDER_COLOR = Color.GREEN;
			} else if (j > 8) {
				BRICK_COLOR = Color.CYAN;
				BORDER_COLOR = Color.CYAN;
			}
			for (int i = 1; i <= (BRICK_COLUMNS); i++) {
				brick = new GRect(BRICK_WIDTH, BRICK_HEIGHT);
				brick.setFillColor(BRICK_COLOR);
				brick.setColor(BORDER_COLOR);
				brick.setFilled(true);
				brick.setLocation(BRICK_X, BRICK_Y);
				BRICK_X += BRICK_WIDTH + BRICK_SEPARATION;
				add(brick);
			}
			/*
			 * Since the offset changes as the above loop adds a new brick,
			 * reset it back to the start for each new row that is created.
			 */
			BRICK_X = BRICK_SEPARATION / 2;
			BRICK_Y += BRICK_HEIGHT + BRICK_SEPARATION;

		}

	}

	/*
	 * Create the paddle.
	 */
	public void thePaddle() {
		paddle = new GRoundRect(PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFillColor(Color.DARK_GRAY);
		paddle.setFilled(true);
		paddle.setLocation(PADDLE_X, PADDLE_Y);
		add(paddle);
	}

	/*
	 * Handles controlling the paddle with the arrow keys
	 * 
	 * TODO - add key events to pause game
	 * 
	 * TODO - add mouse controls
	 * 
	 * (non-Javadoc)
	 * 
	 * @see acm.program.Program#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		double x = paddle.getX();
		double y = 0;

		switch (e.getKeyCode()) {
		case KeyEvent.VK_RIGHT:
			if (x < (APPLET_WIDTH - PADDLE_WIDTH)) {
				paddle.move(PADDLE_WIDTH, y);
			}

			break;
		case KeyEvent.VK_LEFT:
			if (x > 0) {
				paddle.move(-PADDLE_WIDTH, y);
			}
			break;			
		default:
			break;
		}
	}

	/*
	 * Create the ball.
	 */
	private void theBall() {
		// launches ball in random direction between 1 and 3
		ballVY = rand.nextDouble(1.0, 3.0);
		ball = new GOval(BALL_DIAMETER, BALL_DIAMETER);
		ball.setFillColor(Color.DARK_GRAY);
		ball.setFilled(true);
		ball.setLocation(BALL_X, BALL_Y);
		add(ball);
	}

	/*
	 * This accounts for all four "corners" of the ball and returns each element
	 * that the ball interacts with. If no element is detected, return null.
	 */
	private GObject detectCollision() {
		if (getElementAt(ball.getX(), ball.getY()) != null) {
			return getElementAt(ball.getX(), ball.getY());
		} else if (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY()) != null) {
			return getElementAt(ball.getX() + BALL_DIAMETER, ball.getY());
		} else if (getElementAt(ball.getX(), ball.getY() + BALL_DIAMETER) != null) {
			return getElementAt(ball.getX(), ball.getY() + BALL_DIAMETER);
		} else if (getElementAt(ball.getX() + BALL_DIAMETER, ball.getY()
				+ BALL_DIAMETER) != null) {
			return getElementAt(ball.getX() + BALL_DIAMETER, ball.getY()
					+ BALL_DIAMETER);
		} else {
			return null;
		}

	}

	/*
	 * Here we make the ball move. First we make it change directions when it
	 * touches the walls and ceiling. Then we use the detectCollision method to
	 * determine what object the ball has hit. If that object is not the paddle,
	 * then we remove it. The infinite loop is broken when either all the bricks
	 * are destroyed or the ball touches the bottom of the screen.
	 */
	private void moveBall() {
		boolean play = true;
		
		while (play) {
			// bounce ball off walls and ceiling
			if (ball.getX() > APPLET_WIDTH - BALL_DIAMETER || ball.getX() <= 0) {
				ballVX = -ballVX;
			}
			if (ball.getY() >= APPLET_HEIGHT - BALL_DIAMETER
					|| ball.getY() <= 0) {
				ballVY = -ballVY;
			}
			GObject collider = detectCollision();
			if (collider == paddle) {
				ballVY = -ballVY;
			/*
			 * If the ball touches anything other than the walls and paddle, remove it.
			 */
			} else if (collider != null) {				
				ballVY = -ballVY;
				/*
				 * Count down from the total number of bricks each time one is removed.
				 */
				BRICK_COUNTER--;
				/*
				 * The displayPoints must first be removed before setting the new value.
				 * Otherwise, the new value is always written on top of the previous.
				 */
				remove(displayPoints);
				/*
				 * The GObejct collider is sent to the track points method so we can get
				 * the color of the object for tracking the points.
				 */
				trackPoints(collider);
				/*
				 * Remove the object.
				 */
				remove(collider);
				if (BRICK_COUNTER == 0) {
					play = false;
				}
			/*
			 * Break the while loop if the ball touches the bottom of the screen
			 * thus ending the game.
			 */
			} else if (ball.getY() >= APPLET_HEIGHT - BALL_DIAMETER) {
				play = false;
				/*
				 * Pause briefly to prevent the ball from bouncing off the bottom
				 * of the screen.
				 */
				pause(10);
			}

			// move the ball
			ball.move(ballVX, ballVY);
			// set the speed of the moving ball
			pause(10);
		}

		// Call the endGame() method if the while loop is broken
		endGame();
	}
	
	/*
	 * Use two separate methods for the score. One for just the word "Score"
	 * and another to track the points. This is so we don't redraw the word
	 * every time we score which causes a minor blip. It looks much cleaner.
	 */
	private void wordScore() {
		/* 
		 * We have to set the initial points to zero here. If we don't
		 * then there is nothing to remove and the program will crash. 
		 * A null check could be used instead, but I feel setting the 
		 * initial value to zero looks better overall.
		 */
		POINTS = 0;
		displayPoints = new GLabel("" + POINTS);
		displayPoints.setLocation(65, 25);
		displayPoints.setFont(new Font("Arial", Font.PLAIN, 20));
		add(displayPoints);
		/*
		 * This just adds the word "Score" which is never changed.
		 */
		wordScore = new GLabel("Score: ");
		wordScore.setLocation(5, 25);
		wordScore.setFont(new Font("Arial", Font.PLAIN, 20));
		add(wordScore);
		
	}
	
	/*
	 * This method keeps track of the points accumulated. We use the
	 * getColor() method to return the color of the collider and adjust
	 * the points based on that color.
	 */
	private void trackPoints(GObject collider) {
		
		BRICK_COLOR = collider.getColor();
		
		if (BRICK_COLOR == Color.CYAN) {
			POINTS +=10;
		}
		else if (BRICK_COLOR == Color.GREEN) {
			POINTS += 20;
		}
		else if (BRICK_COLOR == Color.YELLOW) {
			POINTS += 30;
		}
		else if (BRICK_COLOR == Color.ORANGE) {
			POINTS += 40;
		}
		else if (BRICK_COLOR == Color.RED) {
			POINTS += 50;
		}
		
		displayPoints = new GLabel("" + POINTS);
		displayPoints.setLocation(65, 25);
		displayPoints.setFont(new Font("Arial", Font.PLAIN, 20));
		add(displayPoints);
	}

	/*
	 * This handles what action to take once the game ends.
	 */
	private void endGame() {
		GLabel end;
		/*
		 * If the game ends and the brick counter is 0 then all the 
		 * bricks have been removed and the user has won the game.
		 */
		if (BRICK_COUNTER == 0) {
			end = new GLabel("Congratulations! You won!");
			end.setFont(new Font("Arial", Font.BOLD, 20));
			end.setColor(Color.BLUE);
		/*
		 * If there are bricks remaining when the game has ended then
		 * it is a game over.
		 */
		} else {
			// a tribute to Hudson
			end = new GLabel("Game Over, Man!");
			// TODO - fix fonts
			end.setFont(new Font("Arial", Font.BOLD, 20));
			end.setColor(Color.RED);
		}
		end.setLocation(getWidth() / 2 - end.getWidth() / 2, getHeight() / 2
				- end.getHeight() / 2);
		add(end);
		pause(500);
		// TODO - make the game restart instead of closing
		GLabel click = new GLabel(
				"Click anywhere on the screen to close the application");
		click.setLocation(getWidth() / 2 - click.getWidth() / 2, getHeight()
				/ 2 + end.getHeight());
		add(click);
		waitForClick();
		System.exit(0);
	}

}
