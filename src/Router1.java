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

public class Router1
{
	int router0Cost[] = new int[rMap.length];
	int router1Cost[] = new int[rMap.length];
	int router2Cost[] = new int[rMap.length];
	int router3Cost[] = new int[rMap.length];
	String Router0IP, Router2IP, Router3IP;
	int Router0Port, Router2Port, Router3Port;
	ServerSocket myServerSocket;
	ArrayList<String> R1PosssiblePath = new ArrayList<String>();
	boolean FirstSendOut = false;

	public Router1() throws InterruptedException, IOException
	{
		//find all possible path
		findPath("R1", "R1", R1PosssiblePath);

        //initialize r1 table
		router1Cost = initRouter1Cost;
		for(int i = 0 ; i < rMap.length ; i++)
		{
			router0Cost[i] = -1;
			router2Cost[i] = -1;
			router3Cost[i] = -1;
		}

		displaytable(router0Cost, router1Cost, router2Cost, router3Cost, "1", "I");

		Thread thread = new Thread(waitForUpdate);
		thread.start();

		String port0, port2, port3;
		
        Scanner scanner = new Scanner(System.in);
        System.out.println("Router 0 IP?");
        Router0IP = scanner.next();
        System.out.println("Router 0 port?");
        port0 = scanner.next();
        System.out.println("Router 2 IP?");
        Router2IP = scanner.next();
        System.out.println("Router 2 port?");
        port2 = scanner.next();
        System.out.println("Router 3 IP?");
        Router3IP = scanner.next();
        System.out.println("Router 3 port?");
        port3 = scanner.next();
    	Router0Port = Integer.parseInt(port0);
    	Router2Port = Integer.parseInt(port2);
    	Router3Port = Integer.parseInt(port3);
        scanner.close();

        FirstSendOut = sendList(initRouter1CostList);
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
		for(int i = 0 ; i < r1directConnected.length ; i++)
		{
			Client myClient = new Client();
			if (r1directConnected[i].equals("R0"))
				myClient.sendList("R1", "R0", InetAddress.getByName(Router0IP), Router0Port, list);
			if (r1directConnected[i].equals("R2"))
				myClient.sendList("R1", "R2", InetAddress.getByName(Router2IP), Router2Port, list);
			if (r1directConnected[i].equals("R3"))
				myClient.sendList("R1", "R3", InetAddress.getByName(Router3IP), Router3Port, list);
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
			else if (rToken.equals("R2"))
				router2Cost = gotCostList;
			else if (rToken.equals("R3"))
				router3Cost = gotCostList;

			Update();
		}
	}

	public void Update() throws UnknownHostException, InterruptedException
	{
		ArrayList<String> R1toR0 = new ArrayList<String>();
		ArrayList<String> R1toR2 = new ArrayList<String>();
		ArrayList<String> R1toR3 = new ArrayList<String>();
		for (int i = 0 ; i < R1PosssiblePath.size() ; i++)
		{
			String path = R1PosssiblePath.get(i);
			String List[] = path.split(",");
			if (List[List.length-1].equals("R0"))
				R1toR0.add(path);
			if (List[List.length-1].equals("R2"))
				R1toR2.add(path);
			if (List[List.length-1].equals("R3"))
				R1toR3.add(path);
		}

		int[] preRouter1Cost = new int[4];
		for (int i = 0 ; i < preRouter1Cost.length ; i++)
			preRouter1Cost[i] = router1Cost[i];

		router1Cost[0] = sortMin(R1toR0, router0Cost, router1Cost, router2Cost, router3Cost);
		router1Cost[2] = sortMin(R1toR2, router0Cost, router1Cost, router2Cost, router3Cost);
		router1Cost[3] = sortMin(R1toR3, router0Cost, router1Cost, router2Cost, router3Cost);

		if (preRouter1Cost[0] != router1Cost[0] || preRouter1Cost[1] != router1Cost[1]
				|| preRouter1Cost[2] != router1Cost[2] || preRouter1Cost[3] != router1Cost[3])
		{
			displaytable(router0Cost, router1Cost, router2Cost, router3Cost, "1", "U");
			
			while (!FirstSendOut)
				Thread.sleep(500);
			
			sendList(createList(router1Cost));
		}
	}
}
