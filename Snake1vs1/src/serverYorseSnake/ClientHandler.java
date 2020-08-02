package serverYorseSnake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class ClientHandler extends Thread {
	private Socket player1;
	private Socket player2;
	private DataInputStream disP1;
	private DataInputStream disP2;
	private DataOutputStream doutP1;
	private DataOutputStream doutP2;
	private boolean newBallP1;
	private boolean newBallP2;
	private Random random;
	private String firstBall;
	private Thread p1;
	private Thread p2;
	
	public ClientHandler(Socket player1,Socket player2,DataInputStream disP1,
			DataInputStream disP2,DataOutputStream doutP1,DataOutputStream doutP2) {
		this.player1=player1;
		this.player2=player2;
		this.disP1=disP1;
		this.disP2=disP2;
		this.doutP1=doutP1;
		this.doutP2=doutP2;
		this.newBallP1=false;
		this.newBallP2=false;
		this.firstBall="250-250";
		Runnable readP1=()->{
			while(player1.isConnected()) {
				try {
					String s=disP1.readUTF();
					if(s.equals("new ball"))	newBallP1=true;
					else if(s.equals("end")) {
						player1.close();
						disP1.close();
						doutP1.flush();
						doutP1.close();
					}
					else doutP2.writeUTF(s);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
		Runnable readP2=()->{
			while(player2.isConnected()) {
				try {
					String s=disP2.readUTF();
					if(s.equals("new ball"))	newBallP2=true;
					else if(s.equals("end")) {
						player2.close();
						disP2.close();
						doutP2.flush();
						doutP2.close();
					}
					else doutP1.writeUTF(s);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
		p1=new Thread(readP1);
		p2=new Thread(readP2);
		random=new Random();
	}
	@Override
	public void run() {
		try {
			doutP1.writeUTF("right-"+firstBall);
			doutP2.writeUTF("left-"+firstBall);
			doutP1.writeUTF("start");
			doutP2.writeUTF("start");
		} catch (IOException e) {
			e.printStackTrace();
		}
		p1.start();
		p2.start();
		boolean first=true;
		long mil=System.currentTimeMillis();
		while(player1.isConnected() && player2.isConnected()) {
			if(!first && System.currentTimeMillis()-mil>100) {
				try {
					doutP1.writeUTF("update");
					doutP2.writeUTF("update");
					mil=System.currentTimeMillis();
					if(newBallP1 || newBallP2) {
						String ball=getNewBall();
						doutP1.writeUTF(ball);
						doutP2.writeUTF(ball);
						newBallP1=false;
						newBallP2=false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(first && System.currentTimeMillis()-mil>5000) {
				try {
					doutP1.writeUTF("update");
					doutP2.writeUTF("update");
					first=false;
					mil=System.currentTimeMillis();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String getNewBall() {
		int ballX=random.nextInt(100)*5;
		int ballY=random.nextInt(100)*5;
		return ballX+"-"+ballY;
	}
}
