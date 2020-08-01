package yorseSnake;

import java.awt.GridLayout;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;

public class MyPlayer {
	private static final String ip="ip";
	private static final int port=0;
	public static void main(String[] args) {
			Socket socket;
			try {
				socket = new Socket(ip,port);
				JFrame frame=new JFrame();
				frame.setLayout(new GridLayout());
				WrapperPanel wp=new WrapperPanel(socket);
				frame.add(wp);
				frame.revalidate();
				frame.repaint();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setSize(500, 500);
				frame.setVisible(true);
				wp.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
