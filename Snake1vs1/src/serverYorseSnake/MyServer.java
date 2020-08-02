package serverYorseSnake;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
	public static void main(String[] args) {
		try {
			int nPlayers=0;
			ServerSocket server=new ServerSocket(6666);
			while(nPlayers<4) {
				Socket player1=server.accept();
				DataInputStream disP1=new DataInputStream(player1.getInputStream());
				DataOutputStream doutP1=new DataOutputStream(player1.getOutputStream());
				Socket player2=server.accept();
				DataInputStream disP2=new DataInputStream(player2.getInputStream());
				DataOutputStream doutP2=new DataOutputStream(player2.getOutputStream());
				ClientHandler clientHandler=new ClientHandler(player1,player2,disP1,disP2,doutP1,doutP2);
				clientHandler.start();
				nPlayers+=2;
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
