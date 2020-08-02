package yorseSnake;

import java.awt.BorderLayout;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class WrapperPanel extends JPanel {
	
	private JLabel playerScore;
	private int playerPoints=0;
	private int opponentPoints=0;
	private JLabel opponentScore;
	private JPanel header;
	private JPanel gamePanel;
	private JButton start;
	private Socket socket;
	
	public WrapperPanel(Socket socket,String startDirection,String ball) {
		playerScore=new JLabel("Your score: "+playerPoints);
		opponentScore=new JLabel("Opponent score: "+opponentPoints);
		gamePanel=new GamePanel(socket,startDirection,ball,this);
		// Setting up the header panel, giving it a BorderLayout
		header=new JPanel();
		header.setLayout(new BorderLayout());
		//Adding the player score to the header
		header.add(playerScore,BorderLayout.EAST);
		header.revalidate();
		header.repaint();
		//Adding the player score to the header
		header.add(opponentScore,BorderLayout.WEST);
		header.revalidate();
		header.repaint();
		//Setting up the main panel layout and adding to it the header and the game panel
		this.setLayout(new BorderLayout());
		this.add(header,BorderLayout.NORTH);
		this.revalidate();
		this.repaint();
		this.add(gamePanel,BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	public void start() {
		((GamePanel) gamePanel).start();
	}
	
	public void playerScored() {
		playerPoints++;
		playerScore.setText("Your score: "+playerPoints);
	}

	public void opponentScored() {
		opponentPoints++;
		opponentScore.setText("Opponent score: "+opponentPoints);
	}
}
