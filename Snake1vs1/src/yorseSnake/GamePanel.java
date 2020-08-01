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
	private int lenght=0;
	private int time=0;
	private boolean opponentLost=false;
	private boolean playerLost=false;
	//Connectivity
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dout;
	private Thread readOpponent;
	
	public GamePanel(Socket socket) {
		this.setBackground(Color.lightGray);
		try {
			this.socket=socket;
			dis=new DataInputStream(socket.getInputStream());
			dout=new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		playerPos=new Point(50,250);
		opponentPos=new Point(450,250);
		opponentDirection="left";
		playerDirection="right";
		this.end=false;
		this.setFocusable(true);
		Runnable read=()->{
			while(!end && socket.isConnected()) {
				try {
					String s=dis.readUTF();
					opponentDirection=s;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
		readOpponent=new Thread(read);
	}
	public void start() {
		this.addKeyListener(this);
		long mil=System.currentTimeMillis();
		readOpponent.start();
		while(!end) {
			if(System.currentTimeMillis()-mil>100) {
				mil=System.currentTimeMillis();
				time++;
				cutTail();
				this.repaint();
			}
		}
		//CASES, TIE WIN LOSE
		if(opponentLost && playerLost)	JOptionPane.showMessageDialog(this, "There was a TIE! :|");
		else if(playerLost)	JOptionPane.showMessageDialog(this, "You LOST! :(");
		else if(opponentLost)	JOptionPane.showMessageDialog(this, "You WON congratulationss! :)");
	}
	private void setDirection(String direction) {
		this.playerDirection=direction;
	}
	@Override
	public void paintComponent (Graphics g) {
		super.paintComponent(g);	//TO PAINT THE BACKGROUND OF THE PANEL
		//Drawing the two players snake
		drawPlayer(g,Color.green,playerDirection,playerPos,playerRectangles);
		drawPlayer(g,Color.blue,opponentDirection,opponentPos,opponentRectangles);
		if(touchedOpponent()) end=true;
	}
	private boolean touchedOpponent() {
		Point playerHead=playerRectangles.get(playerRectangles.size()-1);
		Point opponentHead=opponentRectangles.get(opponentRectangles.size()-1);
		if(opponentRectangles.stream().anyMatch(e->e.getX()==playerHead.getX() && e.getY()==playerHead.getY()))	playerLost=true;
		if(playerRectangles.stream().anyMatch(e->e.getX()==opponentHead.getX() && e.getY()==opponentHead.getY())) opponentLost=true;
		return opponentLost || playerLost;
	}
	private void cutTail() {
		if(lenght>3 && time<10) {
			System.out.println((playerRectangles.size()-lenght-1)+" - "+lenght+" - "+time);
			playerRectangles.remove(playerRectangles.size()-lenght-1);
			opponentRectangles.remove(opponentRectangles.size()-lenght-1);
		}
		else {
			lenght++;
			if(time>=10)	time=0;
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
			rectangles.add(new Point((int) pos.getX(),(int) pos.getY()));
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
			rectangles.add(new Point((int) pos.getX(),(int) pos.getY()));
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
