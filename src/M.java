package william;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class M
{
	public static final int  listSize = 4;
	public static final String[] r0directConnected = {"R1", "R2", "R3"};
	public static final String[] r1directConnected = {"R0", "R2"};
	public static final String[] r2directConnected = {"R0", "R1", "R3"};
	public static final String[] r3directConnected = {"R0", "R2"};
	public static final String[] rMap = {"R0", "R1", "R2", "R3"};
	public final static int[] initRouter0Cost = {0, 1, 3, 7};
	public final static String initRouter0CostList = "0,1,3,7";
	public final static int[] initRouter1Cost = {1, 0, 1, -1};
	public final static String initRouter1CostList = "1,0,1,-1";
	public final static int[] initRouter2Cost = {3, 1, 0, 2};
	public final static String initRouter2CostList = "3,1,0,2";
	public final static int[] initRouter3Cost = {7, -1, 2, 0};
	public final static String initRouter3CostList = "7,-1,2,0";
	public static int port = 50005;

	public static void main(String[] args) throws InterruptedException, IOException
	{
		String r,p;
		int in_port = 50005;
        Scanner scanner = new Scanner(System.in);
        System.out.println("What router do you want to be");
        r = scanner.next();
        System.out.println("What port do you want to hold?");
        p = scanner.next();
        try
        {
        	in_port = Integer.parseInt(p);
        }
        catch (Exception e)
        {
            System.out.println("port must be integer...");
        }
        port = in_port;
        if (r.equals("0"))
    		new Router0();
        else if (r.equals("1"))
        	new Router1();
        else if (r.equals("2"))
        	new Router2();
        else if (r.equals("3"))
        	new Router3();
        scanner.close();
	}

	public static void findPath(String r, String previousPATH, ArrayList<String> rPossiblePath)
	{
		String[] connected = null;
		String newPATH = null;

		if (r.equals("R0"))
			connected = r0directConnected;
		else if (r.equals("R1"))
			connected = r1directConnected;
		else if (r.equals("R2"))
			connected = r2directConnected;
		else if (r.equals("R3"))
			connected = r3directConnected;

		for (int i = 0 ; i < connected.length ; i++)
		{
			if (!previousPATH.contains(connected[i]))
			{
				newPATH = previousPATH + "," + connected[i];
				rPossiblePath.add(newPATH);
				findPath(connected[i], newPATH, rPossiblePath);
			}
		}
	}

	// Sort Method
	public static int sortMin(ArrayList<String> PtoP, int[] router0Cost, int[] router1Cost,
			int[] router2Cost, int[] router3Cost)
	{
		ArrayList<Integer> PtoPPossiblePath = new ArrayList<Integer>();
		for (int i = 0 ; i < PtoP.size() ; i++)
		{
			String PtoPPoint[] = PtoP.get(i).split(",");
			int cost = 0;
			int storedCost = 0;
			for (int j = 0 ; j < PtoPPoint.length && j != PtoPPoint.length -1 ; j++)
			{
				if (PtoPPoint[j].equals(rMap[0]) && PtoPPoint[j+1].equals(r0directConnected[0]))
					storedCost = router0Cost[1];
				else if (PtoPPoint[j].equals(rMap[0]) && PtoPPoint[j+1].equals(r0directConnected[1]))
					storedCost = router0Cost[2];
				else if (PtoPPoint[j].equals(rMap[0]) && PtoPPoint[j+1].equals(r0directConnected[2]))
					storedCost = router0Cost[3];
				else if (PtoPPoint[j].equals(rMap[1]) && PtoPPoint[j+1].equals(r1directConnected[0]))
					storedCost = router1Cost[0];
				else if (PtoPPoint[j].equals(rMap[1]) && PtoPPoint[j+1].equals(r1directConnected[1]))
					storedCost = router1Cost[2];
				else if (PtoPPoint[j].equals(rMap[2]) && PtoPPoint[j+1].equals(r2directConnected[0]))
					storedCost = router2Cost[0];
				else if (PtoPPoint[j].equals(rMap[2]) && PtoPPoint[j+1].equals(r2directConnected[1]))
					storedCost = router2Cost[1];
				else if (PtoPPoint[j].equals(rMap[2]) && PtoPPoint[j+1].equals(r2directConnected[2]))
					storedCost = router2Cost[3];
				else if (PtoPPoint[j].equals(rMap[3]) && PtoPPoint[j+1].equals(r3directConnected[0]))
					storedCost = router3Cost[0];
				else if (PtoPPoint[j].equals(rMap[3]) && PtoPPoint[j+1].equals(r3directConnected[1]))
					storedCost = router3Cost[2];

				if (storedCost != -1)
					cost = cost + storedCost;
				else
				{
					cost = -1;
					break;
				}
			}
			if (cost != -1)
				PtoPPossiblePath.add(cost);
		}
		Collections.sort(PtoPPossiblePath);

		return PtoPPossiblePath.get(0);
	}

	public static void displaytable(int[] router0Cost, int[] router1Cost, int[] router2Cost, int[] router3Cost, String r, String flag)
	{
		String type = null;
		if (flag.equals("I"))
			type = "INITIAL";
		else if (flag.equals("U"))
			type = "UPDATE";
		System.out.println("****ROUTER" + r + " " + type + " TABLE****");
		System.out.println("		router0		router1		router2		router3");
		System.out.println("router0		" + (router0Cost[0]==-1?"¡Û":router0Cost[0]) + "		" + (router0Cost[1]==-1?"¡Û":router0Cost[1])+ "		" + (router0Cost[2]==-1?"¡Û":router0Cost[2])+ "		" + (router0Cost[3]==-1?"¡Û":router0Cost[3]));
		System.out.println("router1		" + (router1Cost[0]==-1?"¡Û":router1Cost[0]) + "		" + (router1Cost[1]==-1?"¡Û":router1Cost[1])+ "		" + (router1Cost[2]==-1?"¡Û":router1Cost[2])+ "		" + (router1Cost[3]==-1?"¡Û":router1Cost[3]));
		System.out.println("router2		" + (router2Cost[0]==-1?"¡Û":router2Cost[0]) + "		" + (router2Cost[1]==-1?"¡Û":router2Cost[1])+ "		" + (router2Cost[2]==-1?"¡Û":router2Cost[2])+ "		" + (router2Cost[3]==-1?"¡Û":router2Cost[3]));
		System.out.println("router3		" + (router3Cost[0]==-1?"¡Û":router3Cost[0]) + "		" + (router3Cost[1]==-1?"¡Û":router3Cost[1])+ "		" + (router3Cost[2]==-1?"¡Û":router3Cost[2])+ "		" + (router3Cost[3]==-1?"¡Û":router3Cost[3]));
	}
	
	public static String createList(int rCost[])
	{
		String list = "";
		for(int i = 0 ; i < rCost.length ; i++)
		{
			if(list.equals(""))
				list = list + rCost[i];
			else
				list = list + "," + rCost[i];
		}
		return list;
	}
	
}
