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

	// overkill with constants to minimize math efforts throughout

	// constants for applet size
	private final int APPLET_WIDTH = 400;
	private final int APPLET_HEIGHT = 600;

	// constants for paddle
	private final int PADDLE_Y_OFFSET = 30;
	private final int PADDLE_WIDTH = 60;
	private final int PADDLE_HEIGHT = 10;
	private final int PADDLE_X_START = APPLET_WIDTH / 2 - PADDLE_WIDTH / 2;
	private final int PADDLE_Y_START = APPLET_HEIGHT - PADDLE_Y_OFFSET;

	// constants for bricks
	private final int BRICK_X_START = 2;
	private int BRICK_X_OFFSET = 2;
	private int BRICK_Y_OFFSET = 70;
	private final int BRICK_ROWS = 10;
	private final int BRICK_COLUMNS = 10;
	private final int BRICK_SEPARATION = 4;
	private final int BRICK_HEIGHT = 8;
	private final int BRICK_WIDTH = (APPLET_WIDTH / BRICK_COLUMNS)
			- BRICK_SEPARATION;

	// constants for ball
	final private int BALL_RADIUS = 10;
	final private int BALL_X_START = APPLET_WIDTH / 2 - BALL_RADIUS;
	final private int BALL_Y_START = APPLET_HEIGHT / 2 - BALL_RADIUS;
	final private int BALL_DIAMETER = 2 * BALL_RADIUS;

	private static Color BRICK_COLOR;

	private int BRICK_COUNTER = BRICK_COLUMNS * BRICK_ROWS;

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
			} else if (j > 2 && j <= 4) {
				BRICK_COLOR = Color.ORANGE;
			} else if (j > 4 && j <= 6) {
				BRICK_COLOR = Color.YELLOW;
			} else if (j > 6 && j <= 8) {
				BRICK_COLOR = Color.GREEN;
			} else if (j > 8) {
				BRICK_COLOR = Color.CYAN;
			}
			for (int i = 1; i <= (BRICK_COLUMNS); i++) {
				brick = new GRect(BRICK_WIDTH, BRICK_HEIGHT);
				brick.setFillColor(BRICK_COLOR);
				brick.setFilled(true);
				brick.setLocation(BRICK_X_OFFSET, BRICK_Y_OFFSET);
				BRICK_X_OFFSET += BRICK_WIDTH + BRICK_SEPARATION;
				add(brick);
			}
			/*
			 * since the offset changes as the above loop adds a new brick,
			 * reset it back to the start for each new row that is created
			 */
			BRICK_X_OFFSET = BRICK_X_START;
			BRICK_Y_OFFSET += BRICK_HEIGHT + BRICK_SEPARATION;

		}

	}

	/*
	 * creates the paddle
	 */
	public void thePaddle() {
		paddle = new GRoundRect(PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFillColor(Color.DARK_GRAY);
		paddle.setFilled(true);
		paddle.setLocation(PADDLE_X_START, PADDLE_Y_START);
		add(paddle);
	}

	/*
	 * handles controlling the paddle with the arrow keys
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
	 * creates the ball
	 */
	private void theBall() {
		// launches ball in random direction between 1 and 3
		ballVY = rand.nextDouble(1.0, 3.0);
		ball = new GOval(BALL_DIAMETER, BALL_DIAMETER);
		ball.setFillColor(Color.DARK_GRAY);
		ball.setFilled(true);
		ball.setLocation(BALL_X_START, BALL_Y_START);
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
			if (ball.getX() > APPLET_WIDTH - BALL_DIAMETER || ball.getX() <= 0) {
				ballVX = -ballVX;
			}
			if (ball.getY() >= APPLET_HEIGHT - BALL_DIAMETER
					|| ball.getY() <= 0) {
				ballVY = -ballVY;
			}
			GObject collision = detectCollision();
			if (collision == paddle) {
				ballVY = -ballVY;
			} else if (collision != null) {
				remove(collision);
				ballVY = -ballVY;
				BRICK_COUNTER--;
				if (BRICK_COUNTER == 0) {
					play = false;
				}
			} else if (ball.getY() >= APPLET_HEIGHT - BALL_DIAMETER) {
				play = false;
				pause(10);
			}

			ball.move(ballVX, ballVY);
			pause(10);
		}

		endGame();
	}

	/*
	 * This handles what action to take once the game ends.
	 */
	private void endGame() {
		GLabel end;
		if (BRICK_COUNTER == 0) {
			end = new GLabel("Congratulations! You won!");
			end.setFont(new Font("Comic Sans", Font.BOLD, 18));
			end.setColor(Color.BLUE);
		} else {
			end = new GLabel("Game Over.");
			// TODO - fix fonts
			end.setFont(new Font("Berlin Sans FB Demi Bold", Font.BOLD, 18));
			end.setColor(Color.RED);
		}
		end.setLocation(getWidth() / 2 - end.getWidth() / 2, getHeight() / 2
				- end.getHeight() / 2);
		add(end);
		pause(500);
		GLabel click = new GLabel(
				"Click anywhere on the screen to close the application");
		click.setLocation(getWidth() / 2 - click.getWidth() / 2, getHeight()
				/ 2 + end.getHeight());
		add(click);
		waitForClick();
		System.exit(0);
	}

}
