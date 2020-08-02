package yorseSnake;

import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;

public class MyPlayer {
	private static final String ip="localhost";
	private static final int port=6666;
	public static void main(String[] args) {
			Socket socket;
			try {
				socket = new Socket(ip,port);
				DataInputStream dis=new DataInputStream(socket.getInputStream());
				String startDirectionAndBall=dis.readUTF();
				System.out.println(startDirectionAndBall);
				JFrame frame=new JFrame();
				frame.setLayout(new GridLayout());
				WrapperPanel wp=new WrapperPanel(socket,
						startDirectionAndBall.substring(0, startDirectionAndBall.indexOf("-")),
						startDirectionAndBall.substring(startDirectionAndBall.indexOf("-")+1));
				frame.add(wp);
				frame.revalidate();
				frame.repaint();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setSize(500, 500);
				frame.setVisible(true);
				boolean start=false;
				String s;
				while(!start) {
					s=dis.readUTF();
					System.out.println(s);
					if(s.equals("start")) {
						System.out.println("START");
						start=true;
					}
				}
				wp.start();
//				socket.close();
//				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
