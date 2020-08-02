package yorseSnake;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements KeyListener{
	
	private String playerDirection;
	private String opponentDirection;
	private Point playerPos;
	private Point opponentPos;
	private List<Point> playerRectangles=new ArrayList<>();
	private List<Point> opponentRectangles=new ArrayList<>();
	private boolean end;
	private int ballX;
	private int ballY;
	private boolean opponentLost=false;
	private boolean playerLost=false;
	private boolean playerHit=false;
	private boolean opponentHit=false;
	private WrapperPanel wrapperPanel;
	//Connectivity
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dout;
	private Thread readOpponent;
	
	public GamePanel(Socket socket,String startDirection,String ball,WrapperPanel wrapperPanel) {
		this.wrapperPanel=wrapperPanel;
		this.setBackground(Color.black);
		getBallCoordinate(ball);
		try {
			this.socket=socket;
			dis=new DataInputStream(socket.getInputStream());
			dout=new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(startDirection.equals("right")) {
			playerPos=new Point(50,250);
			opponentPos=new Point(450,250);
			opponentDirection="left";
			playerDirection="right";
		}
		else {
			opponentPos=new Point(50,250);
			playerPos=new Point(450,250);
			playerDirection="left";
			opponentDirection="right";
		}
		this.end=false;
		this.setFocusable(true);
		Runnable read=()->{
			while(!end && socket.isConnected()) {
				try {
					String s=dis.readUTF();
					if(s.contains(" ")) s=s.substring(0, s.indexOf(" "));
					if(s.equals("update")) updateScreen();
					else if(s.equals("up") || s.equals("down") || s.equals("right") || s.equals("left"))	opponentDirection=s;
					else getBallCoordinate(s);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
		readOpponent=new Thread(read);
	}
	private void getBallCoordinate(String ball) {
		ballX=Integer.parseInt(ball.substring(0, ball.indexOf("-")));
		ballY=Integer.parseInt(ball.substring(ball.indexOf("-")+1));
	}
	private void updateScreen() {
		this.repaint();
		cutTail(playerRectangles,playerHit);
		cutTail(opponentRectangles,opponentHit);
		//CASES, TIE WIN LOSE
		if(opponentLost && playerLost) {
			JOptionPane.showMessageDialog(this, "There was a TIE! :|");
			try {
				dout.writeUTF("end");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(playerLost) {
			JOptionPane.showMessageDialog(this, "You LOST! :(");
			try {
				dout.writeUTF("end");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(opponentLost) {
			JOptionPane.showMessageDialog(this, "You WON congratulationss! :)");
			try {
				dout.writeUTF("end");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void start() {
		this.addKeyListener(this);
		readOpponent.start();
	}
	private void setDirection(String direction) {
		this.playerDirection=direction;
		try {
			dout.writeUTF(playerDirection);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void paintComponent (Graphics g) {
		super.paintComponent(g);	//TO PAINT THE BACKGROUND OF THE PANEL
		g.setColor(Color.red);
		g.fillRect(ballX, ballY, 5, 5);
		//Drawing the two players snake
		drawPlayer(g,Color.green,playerDirection,playerPos,playerRectangles);
		drawPlayer(g,Color.blue,opponentDirection,opponentPos,opponentRectangles);
		if(touchedOpponent()) end=true;
		hitBall();
	}
	private void hitBall() {
		//getting the head of both players
		Point playerHead=playerRectangles.get(playerRectangles.size()-1);
		Point opponentHead=opponentRectangles.get(opponentRectangles.size()-1);
		//checking if the head of any player hit the ball
		if(playerHead.getX()==ballX && playerHead.getY()==ballY) {
			playerHit=true;
			wrapperPanel.playerScored();
		}
		if(opponentHead.getX()==ballX && opponentHead.getY()==ballY) {
			opponentHit=true;
			wrapperPanel.opponentScored();
		}
		//if your player did a point tell it to the server
		if(playerHit) {
			try {
				dout.writeUTF("new ball");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private boolean touchedOpponent() {
		//PLAYER HEAD
		Point playerHead=playerRectangles.get(playerRectangles.size()-1);
		Point opponentHead=opponentRectangles.get(opponentRectangles.size()-1);
		//CHECKING IF A PLAYER HIT HIMSELF
		if(playerRectangles.stream().filter(e->e!=playerHead).anyMatch(e->e.getX()==playerHead.getX() && e.getY()==playerHead.getY())) playerLost=true;
		if(opponentRectangles.stream().filter(e->e!=opponentHead).anyMatch(e->e.getX()==opponentHead.getX() && e.getY()==opponentHead.getY())) opponentLost=true;
		//CHECKING IF THE HEAD OF A PLAYER HITTED THE OPPONENT
		if(opponentRectangles.stream().anyMatch(e->(e.getX()==playerHead.getX()) && (e.getY()==playerHead.getY())))	playerLost=true;
		if(playerRectangles.stream().anyMatch(e->(e.getX()==opponentHead.getX()) && (e.getY()==opponentHead.getY()))) opponentLost=true;
		if(playerLost || opponentLost) return true;
		return false;
	}
	private void cutTail(List<Point> rectangles,boolean hit) {
		if((!hit) && rectangles.size()>5) rectangles.remove(0);
		if(hit){
			playerHit=false;
			opponentHit=false;
		}
	}
	private void drawPlayer(Graphics g, Color color,String direction,Point pos,List<Point> rectangles) {
		g.setColor(color);
		rectangles.forEach(e->g.fillRect((int) e.getX(), (int) e.getY(), 5, 5));
		boolean border=false;
		if(direction.equals("up")) {
			if(pos.getY()-5<0) border=true;
			g.setColor(Color.yellow);
			g.fillOval((int) pos.getX(), (int) pos.getY()-5, 5, 5);
			rectangles.add(new Point((int) pos.getX(),(int) pos.getY()-5));
			if(!border) pos.setLocation(pos.getX(),pos.getY()-5 );
			if(border) pos.setLocation(pos.getX(),this.getHeight());
		}
		if(direction.equals("down")) {
			if(this.getHeight()<pos.getY()+5) border=true;
			g.setColor(Color.yellow);
			g.fillOval((int) pos.getX(), (int) pos.getY(), 5, 5);
			rectangles.add(new Point((int) pos.getX(),(int) pos.getY()+5));
			if(!border) pos.setLocation(pos.getX(),pos.getY()+5 );
			if(border) pos.setLocation(pos.getX(),0 );
		}
		if(direction.equals("left")) {
			if(pos.getX()-5<0) border=true;
			g.setColor(Color.yellow);
			g.fillOval((int) pos.getX()-5, (int) pos.getY(), 5, 5);
			rectangles.add(new Point((int) pos.getX()-5,(int) pos.getY()));
			if(!border) pos.setLocation(pos.getX()-5,pos.getY() );
			if(border) pos.setLocation(this.getWidth(),pos.getY() );
		}
		if(direction.equals("right")) {
			if(this.getWidth()<pos.getX()+5) border=true;
			g.setColor(Color.yellow);
			g.fillOval((int) pos.getX(), (int) pos.getY(), 5, 5);
			rectangles.add(new Point((int) pos.getX()+5,(int) pos.getY()));
			if(!border) pos.setLocation(pos.getX()+5,pos.getY() );
			if(border) pos.setLocation(0,pos.getY() );
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_W || e.getKeyCode()==KeyEvent.VK_UP)		setDirection("up");
		if(e.getKeyCode()==KeyEvent.VK_A || e.getKeyCode()==KeyEvent.VK_LEFT)	setDirection("left");
		if(e.getKeyCode()==KeyEvent.VK_D || e.getKeyCode()==KeyEvent.VK_RIGHT)	setDirection("right");
		if(e.getKeyCode()==KeyEvent.VK_S || e.getKeyCode()==KeyEvent.VK_DOWN)	setDirection("down");
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_W || e.getKeyCode()==KeyEvent.VK_UP)		setDirection("up");
		if(e.getKeyCode()==KeyEvent.VK_A || e.getKeyCode()==KeyEvent.VK_LEFT)	setDirection("left");
		if(e.getKeyCode()==KeyEvent.VK_D || e.getKeyCode()==KeyEvent.VK_RIGHT)	setDirection("right");
		if(e.getKeyCode()==KeyEvent.VK_S || e.getKeyCode()==KeyEvent.VK_DOWN)	setDirection("down");
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}
}
