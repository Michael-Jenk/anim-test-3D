import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@SuppressWarnings("serial")
public class Main extends JPanel {
	
	private static JFrame frame;
	private static final int WIDTH = 800;
	private static final int HEIGHT = 450;
	
	private BufferedImage image;
	private Graphics g;
	private Timer timer;
	
	private static double dt = 0.01; // in seconds, multiply by 1000 to get milliseconds
	
	
	private static final double DEPTH = 100.0;
	
	private double[][] stars = new double[40][4]; // x, y, distance, speed
	private double[][] enemies = new double[1][3]; // x, y, distance; dist > 100 if dead, <100 if alive, <0 if game over
	private double[][] shots = new double[5][3]; // x, y, distance
	
	private static final int pSize = 100;
	private static final double eSpeed = 20.0;
	private static final int eSize = 125;
	private static final double sSpeed = 250.0;
	private static final int sLength = 20;
	private static final int sSize = eSize + 25;
	
	private boolean pressL = false;
	private boolean pressR = false;
	private boolean pressU = false;
	private boolean pressD = false;
	
	private boolean pressShoot = false;
	
	private boolean firstKey = false;
	
	private double pX;
	private double pY;
	private static final double pD = DEPTH;
	private static final double pSpeed = 1500.0;
	private static final double minSpeed = 10.0;
	private double xV;
	private double yV;
	
	private double gameTimer = 0.0;

	public static void main(String args[]) throws IOException {
		frame = new JFrame("3D Anim Test");
		frame.setSize(WIDTH + 18, HEIGHT + 47);
		frame.setLocation(1920/2 - WIDTH, 1200/2 - HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new Main());
		frame.setVisible(true);
		
		frame.setIconImage( ImageIO.read(new File("Icon.png")) );
		frame.setResizable(false);
		frame.toFront();
	}
	
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	}
	
	public Main() {
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = image.getGraphics();
		timer = new Timer((int)(dt*1000), new TimerListener());
		
		addKeyListener(new Keyboard());
		setFocusable(true);
		
		//put here any starting code necessary
		
		//initializing player
		pX = WIDTH/2;
		pY = HEIGHT/2;
		xV = 0.0;
		yV = 0.0;
		
		//creating enemies
		for (int i = 0; i < enemies.length; i++) {
			enemies[i][0] = Math.random() * WIDTH;
			enemies[i][1] = Math.random() * HEIGHT;
			enemies[i][2] = DEPTH;
			//TODO get enemy respawn timer working
		}
		
		//creating stars
		for (int i = 0; i < stars.length; i++) {
			stars[i][0] = Math.random() * (WIDTH*1.5);
			stars[i][1] = Math.random() * (HEIGHT*1.5);
			stars[i][2] = DEPTH * Math.random();
			stars[i][3] = 100.0 + (Math.random() * 50);
		}
		
		//creating shots
		for (int i = 0; i < shots.length; i++) {
			shots[i][2] = DEPTH * 2;
		}
		
		timer.start();
	}
	
	private class TimerListener implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			// This where everything happens
			
			gameTimer += dt;
			
			if (!firstKey) {
//				if (gameTimer % 2.0 < 1.0) {
				if (true) {
					g.setColor(Color.WHITE);
					//TODO
					g.drawString("Press any key to start", WIDTH/4,HEIGHT/2);
				}
				
				g.setColor(Color.BLACK);
				g.fillRect(0,0,WIDTH,HEIGHT);
				repaint();
				return;
			}
			
			// moving player
			// actually half good movement system with deceleration if button not pressed
			double targXV;
			double targYV;
			if (pressL) targXV = pSpeed*-1;
			else if (pressR) targXV = pSpeed;
			else targXV = 0;
			if (pressU) targYV = pSpeed*-1;
			else if (pressD) targYV = pSpeed;
			else targYV = 0;
			if ((pressU || pressD) && (pressL || pressR)) {
				targXV *= Math.cos(Math.PI/4);
				targXV *= Math.sin(Math.PI/4);
			}
			
			xV += (targXV - xV)*.1;
			yV += (targYV - yV)*.1;
			if (Math.abs(xV) < minSpeed) xV  = 0;
			if (Math.abs(yV) < minSpeed) yV  = 0;
			
			pX += xV * dt;
			pX = Math.min(WIDTH, Math.max(0, pX));
			pY += yV * dt;
			pY = Math.min(HEIGHT, Math.max(0, pY));
//			System.out.println("x: "+pX+", y: "+pY);
//			System.out.println("vx: "+xV+", vy: "+yV);
			
			if (pressShoot) {
//				System.out.println("PS");
				pressShoot = false;
				for (int i = 0; i < shots.length; i++) {
					if (shots[i][2] < DEPTH) continue;
//					System.out.println("NS");
					shots[i][0] = pX;
					shots[i][1] = pY;
					shots[i][2] = 0;
					break;
				}
			}
			
			//	draw background, everything else based on distance (shots, stars, enemies), then player
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, WIDTH, HEIGHT);
			for (int i = 0; i < DEPTH; i += 10) {
				double j = (i+gameTimer*15)%100;
				g.setColor(new Color(255,255,255, (int)(255.0*Math.pow(j/DEPTH,2)) ));
				g.drawRect((int)((WIDTH/-2) * ((j/DEPTH))) + (int)(WIDTH/2),
						(int)((HEIGHT/-2) * ((j/DEPTH))) + (int)(HEIGHT/2),
//						(int)((WIDTH/2) * ((i/DEPTH))) + (int)(WIDTH/2),
//						(int)((HEIGHT/2) * ((i/DEPTH))) + (int)(HEIGHT/2))
						(int)((j/DEPTH)*WIDTH),
						(int)((j/DEPTH)*HEIGHT));
				for (int c = 0; c < 4; c++) {
					g.drawLine(((int)((WIDTH/-2) * ((j/DEPTH))) + (int)(WIDTH/2))
							* ((c % 2 == 0) ? 1 : -1) + ((c % 2 == 0) ? 0 : WIDTH),
//								* (false ? 1 : -1) + (false ? 0 : WIDTH),
					(int)(((HEIGHT/-2) * ((j/DEPTH))) + (int)(HEIGHT/2))
//								* ((c < 2) ? 1 : -1) + ((c < 2) ? 0 : HEIGHT),
							* ((c < 2) ? 1 : -1) + ((c < 2) ? 0 : HEIGHT),
					(c % 2 == 0) ? 0 : WIDTH,
					(c < 2) ? 0 : HEIGHT);
				}
			}
			drawByDistance();
			drawPlayer();
			
			repaint();
		}
		
	}
	
	private void drawByDistance() {
		// draw by layer (each layer being a range of 10 units of distance)
		for (int i = 100; i >= 0; i--) {
			for (int s = 0; s < stars.length; s++) {
				// Stars by distance
				if (stars[s][2] > i && stars[s][2] < i+1) {
					g.setColor(new Color(255, 255, 255, (int)(255.0*Math.pow(1.0-(stars[s][2]/DEPTH),2)) ) );
					g.fillOval((int)((stars[s][0] - WIDTH/1.3) * (1.0 - (stars[s][2]/DEPTH))) + (int)(WIDTH/2), 
							(int)((stars[s][1] - HEIGHT/1.3) * (1.0 - (stars[s][2]/DEPTH))) + (int)(HEIGHT/2), 
							(int)((DEPTH-stars[s][2])/10.0), (int)((DEPTH-stars[s][2])/10.0));
//					g.fillOval((int)((stars[s][0] - WIDTH/1.3) * Math.pow(1.0 - (stars[s][2]/DEPTH), 2) ) + (int)(WIDTH/2), 
//							(int)((stars[s][1] - HEIGHT/1.3) * Math.pow(1.0 - (stars[s][2]/DEPTH), 2) ) + (int)(HEIGHT/2), 
//							(int)Math.pow((DEPTH-stars[s][2])/10.0,2), (int)Math.pow((DEPTH-stars[s][2])/10.0,2));
				}
			}
			
			//Enemies by distance
			for (int e = 0; e < enemies.length; e++) {
				//Enemies by distance
				if (enemies[e][2] > i && enemies[e][2] < i+1) {
					drawEnemy((int)enemies[e][0], (int)enemies[e][1], (int)enemies[e][2]);
//					System.out.println("x: " + enemies[e][0] + ", y: " + enemies[e][1] + ", depth: " + enemies[e][2]);
				}
			}
			
			//Shots by distance
			for (int sh = 0; sh < shots.length; sh++) {
				if (shots[sh][2] > i && shots[sh][2] < i+1) {
					g.setColor(new Color(3, 232, 252, (int)(255.0*Math.pow(1.0-(shots[sh][2]/DEPTH),0.5)) ) );
					for (int n = 0; n < 2; n++)
						g.drawLine(
								(int)((shots[sh][0] - WIDTH/2.0 + (n<1?1:-1)*pSize ) * (1.0 - ((shots[sh][2]-sLength)/DEPTH))) + (int)(WIDTH/2),
								(int)((shots[sh][1] - HEIGHT/2.0) * (1.0 - ((shots[sh][2]-sLength)/DEPTH))) + (int)(HEIGHT/2),
								(int)((shots[sh][0] - WIDTH/2.0 + (n<1?1:-1)*pSize ) * (1.0 - (shots[sh][2]/(DEPTH)))) + (int)(WIDTH/2),
								(int)((shots[sh][1] - HEIGHT/2.0) * (1.0 - (shots[sh][2]/DEPTH))) + (int)(HEIGHT/2)
								);
				}
			}
		}
		
		
		// move stars after drawn
		for (int i = 0; i < stars.length; i++) {
			stars[i][2] -= stars[i][3] * dt;
			if (stars[i][2] <= 0) {
				stars[i][0] = Math.random() * (WIDTH*1.5);
				stars[i][1] = Math.random() * (HEIGHT*1.5);
				stars[i][2] = DEPTH;
			}
		}
		
		//move enemies after drawn
		for (int e = 0; e < enemies.length; e++) {
//			System.out.println("timer: " + enemies[e][3]);
			if (enemies[e][2] < 0.0) { // if alive and hit screen
				enemies[e][3] = 5.0;
				enemies[e][2] = DEPTH;
				// TODO do something when enemy hits screen
				
			} else if (enemies[e][2] <= DEPTH) { // if alive, move closer to screen
				enemies[e][2] -= eSpeed * dt;
			}
//			else { // if dead, lower respawn counter
//				enemies[e][3] -= dt;
//				if (enemies[e][3] <= 0) {
//					enemies[e][0] = Math.random() * WIDTH;
//					enemies[e][1] = Math.random() * HEIGHT;
//					enemies[e][3] = 0.0;
//				}
//			}
			
			//TODO create live-enemy tracker, create timer for spawning new enemies, spawn them here
		}
		
		//move shots after drawn
		for (int i = 0; i < shots.length; i++) {
			if (shots[i][2] < DEPTH) {
//				System.out.println("sx: "+shots[i][0]+", sys: "+shots[i][1]+", sd: "+shots[i][2]);
				shots[i][2] += sSpeed * dt;
				
				for (int e = 0; e < enemies.length; e++) {
					if (enemies[e][2] <= DEPTH && 
							Math.abs(enemies[e][2]-shots[i][2]) < sLength/2 && 
							Math.sqrt( Math.pow(shots[i][0]-enemies[e][0],2) + Math.pow(shots[i][1]-enemies[e][1],2) ) < sSize/2.0
							) {
//						System.out.println("enemy hit");
						//TODO enemy hit
						enemies[e][2] = DEPTH+1;
					}
				}
				
			}
		}
	}
	
	private void drawEnemy(double x, double y, double dist) {
		// TODO actually make good looking enemies
		int[] xP = {(int)x-(eSize/2), (int)x-(eSize/2), (int)x+(eSize/2), (int)x+(eSize/2)};
		int[] yP = {(int)y-(eSize/2), (int)y+(eSize/2), (int)y+(eSize/2), (int)y-(eSize/2)};
		for (int i = 0; i < xP.length; i++) {
			xP[i] = (int)(((xP[i] - WIDTH/2.0) * (1.0-(dist/DEPTH))) + (WIDTH/2.0));
			yP[i] = (int)(((yP[i] - HEIGHT/2.0) * (1.0-(dist/DEPTH))) + (HEIGHT/2.0));
		}
		g.setColor(new Color(255,255,(int)(255*Math.pow(dist/DEPTH,1.7)) ));
		g.drawPolygon(xP, yP, 4);
//		System.out.println("x: " + xP[0] + ", y: " + yP[0] + ", depth: " + dist + ", multp: " + (1.0-(dist/DEPTH)));
	}
	
	private void drawPlayer() {
		// Front, top, right, bottom, left,
		int[] xP = {(int)pX,(int)pX,		(int)pX+pSize/2,(int)pX,		(int)pX-pSize/2,
				// right wing, left wing, right gun, left gun
				(int)(pX+pSize),(int)(pX-pSize),(int)(pX+pSize),(int)(pX-pSize)};
		int[] yP = {(int)pY,(int)pY+pSize/2,(int)pY,		(int)pY-pSize/2,(int)pY,
				(int)pY,		(int)pY,		(int)pY,		(int)pY};
		double[] pDep = {pD-15,	pD + 10,	pD + 15,		pD + 10,		pD + 15,
				pD + 17,		pD + 17,		pD - 10,		pD - 10};
		for (int i = 0; i < xP.length; i++) {
//			xP[i] = (int)(((xP[i] - WIDTH/2.0) * (1.0-(pDepth[i]/DEPTH))) + (WIDTH/2.0));
//			yP[i] = (int)(((yP[i] - HEIGHT/2.0) * (1.0-(pDepth[i]/DEPTH))) + (HEIGHT/2.0));
			xP[i] = (int)(((xP[i] - WIDTH/2.0) * (pDep[i]/DEPTH)) + (WIDTH/2.0));
			yP[i] = (int)(((yP[i] - HEIGHT/2.0) * (pDep[i]/DEPTH)) + (HEIGHT/2.0));
		}
		g.setColor(Color.RED);
		
		//order of which player lines are drawn
		int[][] pDrawOrder = {
				{1,2}, {2,3}, {3,4}, {4,1}, // initial rear diamond
				{1,3}, // rear line from top to bottom
				{0,1}, {0,2}, {0,3}, {0,4}, // lines from front to corners
				{2,5},{4,6}, // sides to wing tips
				{0,5}, {0,6}, // wing tips to front (temp)
				{5,7}, {6,8} // wing tips to guns
		};
		
		for (int p = 0; p < pDrawOrder.length; p++) {
			g.drawLine(xP[pDrawOrder[p][0]],yP[pDrawOrder[p][0]],xP[pDrawOrder[p][1]],yP[pDrawOrder[p][1]]);
		}
	}
	
	private class Keyboard implements KeyListener {

		public void keyTyped(KeyEvent e) {
			// don't rlly have to do anything here
		}

		public void keyPressed(KeyEvent e) {
			
			if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
				pressU = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
				pressD = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
				pressL = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
				pressR = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				pressShoot = true;
			}
			
			if (!firstKey)
				firstKey = true;
			
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
				pressU = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
				pressD = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
				pressL = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
				pressR = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				pressShoot = false;
			}
		}
		
	}
	
}