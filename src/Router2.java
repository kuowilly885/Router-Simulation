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

public class Router2
{
	int router0Cost[] = new int[rMap.length];
	int router1Cost[] = new int[rMap.length];
	int router2Cost[] = new int[rMap.length];
	int router3Cost[] = new int[rMap.length];
	String Router0IP, Router1IP, Router3IP;
	int Router0Port, Router1Port, Router3Port;
	ServerSocket myServerSocket;
	ArrayList<String> R2PosssiblePath = new ArrayList<String>();
	boolean FirstSendOut = false;

	public Router2() throws InterruptedException, IOException
	{
		//find all possible path
		findPath("R2", "R2", R2PosssiblePath);

        //initialize r2 table
		router2Cost = initRouter2Cost;
		for(int i = 0 ; i < rMap.length ; i++)
		{
			router0Cost[i] = -1;
			router1Cost[i] = -1;
			router3Cost[i] = -1;
		}

		displaytable(router0Cost, router1Cost, router2Cost, router3Cost, "2", "I");

		Thread thread = new Thread(waitForUpdate);
		thread.start();

		String port0, port1, port3;
		
        Scanner scanner = new Scanner(System.in);
        System.out.println("Router 0 IP?");
        Router0IP = scanner.next();
        System.out.println("Router 0 port?");
        port0 = scanner.next();
        System.out.println("Router 1 IP?");
        Router1IP = scanner.next();
        System.out.println("Router 1 port?");
        port1 = scanner.next();
        System.out.println("Router 3 IP?");
        Router3IP = scanner.next();
        System.out.println("Router 3 port?");
        port3 = scanner.next();
    	Router0Port = Integer.parseInt(port0);
    	Router1Port = Integer.parseInt(port1);
    	Router3Port = Integer.parseInt(port3);
        scanner.close();

        FirstSendOut = sendList(initRouter2CostList);

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
		for(int i = 0 ; i < r2directConnected.length ; i++)
		{
			Client myClient = new Client();
			if (r2directConnected[i].equals("R0"))
				myClient.sendList("R2", "R0", InetAddress.getByName(Router0IP), Router0Port, list);
			if (r2directConnected[i].equals("R1"))
				myClient.sendList("R2", "R1", InetAddress.getByName(Router1IP), Router1Port, list);
			if (r2directConnected[i].equals("R3"))
				myClient.sendList("R2", "R3", InetAddress.getByName(Router3IP), Router3Port, list);
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
			else if (rToken.equals("R3"))
				router3Cost = gotCostList;

			Update();
		}
	}

	public void Update() throws UnknownHostException, InterruptedException
	{
		ArrayList<String> R2toR0 = new ArrayList<String>();
		ArrayList<String> R2toR1 = new ArrayList<String>();
		ArrayList<String> R2toR3 = new ArrayList<String>();
		for (int i = 0 ; i < R2PosssiblePath.size() ; i++)
		{
			String path = R2PosssiblePath.get(i);
			String List[] = path.split(",");
			if (List[List.length-1].equals("R0"))
				R2toR0.add(path);
			if (List[List.length-1].equals("R1"))
				R2toR1.add(path);
			if (List[List.length-1].equals("R3"))
				R2toR3.add(path);
		}

		int[] preRouter2Cost = new int[4];
		for (int i = 0 ; i < preRouter2Cost.length ; i++)
			preRouter2Cost[i] = router2Cost[i];

		router2Cost[0] = sortMin(R2toR0, router0Cost, router1Cost, router2Cost, router3Cost);
		router2Cost[1] = sortMin(R2toR1, router0Cost, router1Cost, router2Cost, router3Cost);
		router2Cost[3] = sortMin(R2toR3, router0Cost, router1Cost, router2Cost, router3Cost);

		if (preRouter2Cost[0] != router2Cost[0] || preRouter2Cost[1] != router2Cost[1]
				|| preRouter2Cost[2] != router2Cost[2] || preRouter2Cost[3] != router2Cost[3])
		{
			displaytable(router0Cost, router1Cost, router2Cost, router3Cost, "2", "U");
			
			while (!FirstSendOut)
				Thread.sleep(500);
			
			sendList(createList(router2Cost));
		}
	}
}
