package william;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import static william.M.*;

public class Router3
{
	int router0Cost[] = new int[rMap.length];
	int router1Cost[] = new int[rMap.length];
	int router2Cost[] = new int[rMap.length];
	int router3Cost[] = new int[rMap.length];
	String Router0IP, Router1IP, Router2IP;
	int Router0Port, Router1Port, Router2Port;
	ServerSocket myServerSocket;
	ArrayList<String> R3PosssiblePath = new ArrayList<String>();
	boolean FirstSendOut = false;

	public Router3() throws InterruptedException, IOException
	{
		//find all possible path
		findPath("R3", "R3", R3PosssiblePath);

        //initialize r3 table
		router3Cost = initRouter3Cost;
		for(int i = 0 ; i < rMap.length ; i++)
		{
			router0Cost[i] = -1;
			router1Cost[i] = -1;
			router2Cost[i] = -1;
		}

		displaytable(router0Cost, router1Cost, router2Cost, router3Cost, "3", "I");

		Thread thread = new Thread(waitForUpdate);
		thread.start();
		
		String port0, port1, port2;

        Scanner scanner = new Scanner(System.in);
        System.out.println("Router 0 IP?");
        Router0IP = scanner.next();
        System.out.println("Router 0 port?");
        port0 = scanner.next();
        System.out.println("Router 1 IP?");
        Router1IP = scanner.next();
        System.out.println("Router 1 port?");
        port1 = scanner.next();
        System.out.println("Router 2 IP?");
        Router2IP = scanner.next();
        System.out.println("Router 2 port?");
        port2 = scanner.next();
    	Router0Port = Integer.parseInt(port0);
    	Router1Port = Integer.parseInt(port1);
    	Router2Port = Integer.parseInt(port2);
        scanner.close();

        FirstSendOut = sendList(initRouter3CostList);

	}

    private Runnable waitForUpdate = new Runnable() {
        @Override
        public void run()
        {
    		try
    		{
				waitForUpdate();
			}
    		catch(IOException | InterruptedException e)
    		{
				e.printStackTrace();
			}
        }
    };

	public boolean sendList(String list) throws UnknownHostException, InterruptedException
	{
		for(int i = 0 ; i < r3directConnected.length ; i++)
		{
			Client myClient = new Client();
			if (r3directConnected[i].equals("R0"))
				myClient.sendList("R3", "R0", InetAddress.getByName(Router0IP), Router0Port, list);
			if (r3directConnected[i].equals("R1"))
				myClient.sendList("R3", "R1", InetAddress.getByName(Router1IP), Router1Port, list);
			if (r3directConnected[i].equals("R2"))
				myClient.sendList("R3", "R2", InetAddress.getByName(Router2IP), Router2Port, list);
		}
		return true;
	}

	public void waitForUpdate() throws IOException, InterruptedException
	{
		//establish as a server
		myServerSocket = new ServerSocket(M.port);

		while (true)
		{
			String rToken;
			String list;
			Socket mySocket = myServerSocket.accept();
			DataInputStream dis = new DataInputStream(mySocket.getInputStream());
			rToken = dis.readUTF();
			list = dis.readUTF();
			String gotRawCostList[] = list.split(",");
			int gotCostList[] = new int[4];

			for(int i = 0 ; i < gotRawCostList.length ; i++)
				gotCostList[i] = Integer.parseInt(gotRawCostList[i]);

			if (rToken.equals("R0"))
				router0Cost = gotCostList;
			else if (rToken.equals("R1"))
				router1Cost = gotCostList;
			else if (rToken.equals("R2"))
				router2Cost = gotCostList;

			Update();
		}
	}

	public void Update() throws UnknownHostException, InterruptedException
	{
		ArrayList<String> R3toR0 = new ArrayList<String>();
		ArrayList<String> R3toR1 = new ArrayList<String>();
		ArrayList<String> R3toR2 = new ArrayList<String>();
		for (int i = 0 ; i < R3PosssiblePath.size() ; i++)
		{
			String path = R3PosssiblePath.get(i);
			String List[] = path.split(",");
			if (List[List.length-1].equals("R0"))
				R3toR0.add(path);
			if (List[List.length-1].equals("R1"))
				R3toR1.add(path);
			if (List[List.length-1].equals("R2"))
				R3toR2.add(path);
		}

		int[] preRouter3Cost = new int[4];
		for (int i = 0 ; i < preRouter3Cost.length ; i++)
			preRouter3Cost[i] = router3Cost[i];

		router3Cost[0] = sortMin(R3toR0, router0Cost, router1Cost, router2Cost, router3Cost);
		router3Cost[1] = sortMin(R3toR1, router0Cost, router1Cost, router2Cost, router3Cost);
		router3Cost[2] = sortMin(R3toR2, router0Cost, router1Cost, router2Cost, router3Cost);

		if (preRouter3Cost[0] != router3Cost[0] || preRouter3Cost[1] != router3Cost[1]
				|| preRouter3Cost[2] != router3Cost[2] || preRouter3Cost[3] != router3Cost[3])
		{
			displaytable(router0Cost, router1Cost, router2Cost, router3Cost, "3", "U");

			while (!FirstSendOut)
				Thread.sleep(500);
			
			sendList(createList(router3Cost));
		}
	}
}
