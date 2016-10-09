package william;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Client
{
	public Socket mySocket = null;
    public void sendList(String fromR, String toR, InetAddress IP, int port, String list) throws InterruptedException
    {
		try
		{
			System.out.println(fromR + "Connecting to " + toR + " ...");
			Thread.sleep(3000);
			// Create a socket to connect server
			mySocket = new Socket(IP, port);
			System.out.println(fromR + "Connected to " + toR);

			System.out.println(fromR + "Sending message \"" + list + "\" to the "+ toR +"...");
			Thread.sleep(2000);
			// Write message to server.
			DataOutputStream dataOutputStream = new DataOutputStream(mySocket.getOutputStream());
			dataOutputStream.writeUTF(fromR);
			dataOutputStream.writeUTF(list);
			System.out.println(fromR + "message sent successfuly !");
			mySocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    };
	
}
