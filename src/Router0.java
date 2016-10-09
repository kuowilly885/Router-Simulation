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

public class Router0
{
	int router0Cost[] = new int[rMap.length];
	int router1Cost[] = new int[rMap.length];
	int router2Cost[] = new int[rMap.length];
	int router3Cost[] = new int[rMap.length];
	String Router1IP, Router2IP, Router3IP;
	int Router1Port, Router2Port, Router3Port;
	ServerSocket myServerSocket;
	ArrayList<String> R0PosssiblePath = new ArrayList<String>();
	boolean FirstSendOut = false;

	public Router0() throws InterruptedException, IOException
	{
		//find all possible path
		findPath("R0", "R0", R0PosssiblePath);
		
        //initialize ro table
		router0Cost = initRouter0Cost;
		for(int i = 0 ; i < rMap.length ; i++)
		{
			router1Cost[i] = -1;
			router2Cost[i] = -1;
			router3Cost[i] = -1;
		}

		//display initial table
		displaytable(router0Cost, router1Cost, router2Cost, router3Cost, "0", "I");

		Thread thread = new Thread(waitForUpdate);
		thread.start();

		String port1, port2, port3;

        Scanner scanner = new Scanner(System.in);
        System.out.println("Router 1 IP?");
        Router1IP = scanner.next();
        System.out.println("Router 1 port?");
        port1 = scanner.next();
        System.out.println("Router 2 IP?");
        Router2IP = scanner.next();
        System.out.println("Router 2 port?");
        port2 = scanner.next();
        System.out.println("Router 3 IP?");
        Router3IP = scanner.next();
        System.out.println("Router 3 port?");
        port3 = scanner.next();
    	Router1Port = Integer.parseInt(port1);
    	Router2Port = Integer.parseInt(port2);
    	Router3Port = Integer.parseInt(port3);
        scanner.close();

        FirstSendOut = sendList(initRouter0CostList);
	}

    private Runnable waitForUpdate = new Runnable()
    {
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
		for(int i = 0 ; i < r0directConnected.length ; i++)
		{
			Client myClient = new Client();
			if (r0directConnected[i].equals("R1"))
				myClient.sendList("R0", "R1", InetAddress.getByName(Router1IP), Router1Port, list);
			if (r0directConnected[i].equals("R2"))
				myClient.sendList("R0", "R2", InetAddress.getByName(Router2IP), Router2Port, list);
			if (r0directConnected[i].equals("R3"))
				myClient.sendList("R0", "R3", InetAddress.getByName(Router3IP), Router3Port, list);
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

			if (rToken.equals("R1"))
				router1Cost = gotCostList;
			else if (rToken.equals("R2"))
				router2Cost = gotCostList;
			else if (rToken.equals("R3"))
				router3Cost = gotCostList;

			Update();
		}
	}

	public void Update() throws UnknownHostException, InterruptedException
	{
		ArrayList<String> R0toR1 = new ArrayList<String>();
		ArrayList<String> R0toR2 = new ArrayList<String>();
		ArrayList<String> R0toR3 = new ArrayList<String>();
		for (int i = 0 ; i < R0PosssiblePath.size() ; i++)
		{
			String path = R0PosssiblePath.get(i);
			String List[] = path.split(",");
			if (List[List.length-1].equals("R1"))
				R0toR1.add(path);
			if (List[List.length-1].equals("R2"))
				R0toR2.add(path);
			if (List[List.length-1].equals("R3"))
				R0toR3.add(path);
		}

		int[] preRouter0Cost = new int[4];
		for (int i = 0 ; i < preRouter0Cost.length ; i++)
			preRouter0Cost[i] = router0Cost[i];

		router0Cost[1] = sortMin(R0toR1, router0Cost, router1Cost, router2Cost, router3Cost);
		router0Cost[2] = sortMin(R0toR2, router0Cost, router1Cost, router2Cost, router3Cost);
		router0Cost[3] = sortMin(R0toR3, router0Cost, router1Cost, router2Cost, router3Cost);

		if (preRouter0Cost[0] != router0Cost[0] || preRouter0Cost[1] != router0Cost[1]
				|| preRouter0Cost[2] != router0Cost[2] || preRouter0Cost[3] != router0Cost[3])
		{
			displaytable(router0Cost, router1Cost, router2Cost, router3Cost, "0", "U");

			while (!FirstSendOut)
				Thread.sleep(500);

			sendList(createList(router0Cost));
		}
	}
}
