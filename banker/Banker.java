package banker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * Class simulating banker's algorithm of Dijkstra
 * Contains two methods, one for the whole resource allocating process, one for checking whether a request is safe
 * @author Yutong Zhou
 *
 */
public class Banker {
	
	private static int task;
	private static int rc;
	private static int nonterminated;
	private static int cycle = 0;
	static Activity nil = new Activity("null");
	static Activity no = new Activity("no");
	
	static ArrayList<Task> tasks = new ArrayList<Task>(100);//array list storing all tasks
	static ArrayList<Activity> running = new ArrayList<Activity>(100);//array list storing each task's running activity
	static int[][]claim;//array storing each task's claim for each resource
	static int[]unit;	//array storing each resource's available unit
	static int[][]own; //2d array containing number of each task's owning resource [taskNum][resourceNum]
	
	static ArrayList<Queue<Activity>> initiate = new ArrayList<Queue<Activity>>(100);//array list of each task's initiate activity
	static ArrayList<Queue<Activity>> all = new ArrayList<Queue<Activity>>(100);//array list of queues that contains each task's pending activities
	static Queue<Activity> blocked = new LinkedList<Activity>();//queue storing all current blocked activities
	
	public static void Banker(Scanner input) {
		tasks.add(new Task(0));//preset null task
		
		initiate.add(new LinkedList<Activity>());	//preset array lists
		all.add(new LinkedList<Activity>());
		
		
		String[]s = input.nextLine().split(" ");
		//number of tasks
		task = Integer.parseInt(s[0]);
		if(task>99)
			System.err.println("Task number exceeds 99.");
		nonterminated = task;
		
		//array indicating whether a task has blocked activities
		boolean[] hasBlocked = new boolean[task+1];
		for(int i = 0;i<=task;i++) {
			initiate.add(i, new LinkedList<Activity>());
			
			running.add(new Activity("null"));	//null activity that ensures running has number of elements equal to task
			hasBlocked[i]=false;
		}
		
		//array storing remaining computing time of each task
		int[]compute = new int[task+1];
		for(int i=0;i<task+1;i++) {
			compute[i]=1;	//preset all remaining computing time to 1, which means finished computing for computed tasks 
		}
		
		//number of resources
		int resourceNum = Integer.parseInt(s[1]);
		rc = resourceNum;
		
		own = new int[task+1][resourceNum+1];
		claim = new int[task+1][resourceNum+1];
		
		unit = new int[resourceNum+1];
		for(int i=1;i<=resourceNum;i++)
			unit[i] = Integer.parseInt(s[i+1]);
	
		//array storing pending resource
		int[]pending = new int[resourceNum+1];
		
		
		///put all activities to corresponding data structures that store them
		
		while(input.hasNext()) {
			String name = input.next();
			int a = input.nextInt();
			int b = input.nextInt();
			int c = input.nextInt();
			if(name.equals("initiate")) {
				if(tasks.size()<=a) {
					Task t = new Task(a);
					
					tasks.add(a,t);
				}
				claim[a][b]=c;
				
				//if initial claim exceeds the resources present, abort the task
				if(c>unit[b]) {
					tasks.get(a).setAborted();
					nonterminated--;
					System.out.println("Banker aborts task "+a+" before run begins:");
					System.out.println("claim for resources "+b+" ("+c+") exceeds number of unit present ("+unit[b]+")");
				}
				
				Activity i = new Activity("initiate");
				initiate.get(a).add(i);
				Queue<Activity> q = new LinkedList<Activity>();//build a queue for storing activities of current task
				all.add(a, q);
			}
			else if(name.equals("request")) {
				Activity req = new Activity("request",b,c,a);
				all.get(a).add(req);
			}
			else if(name.equals("release")) {
				Activity rel = new Activity("release",b,c,a);
				all.get(a).add(rel);
			}
			else if(name.equals("compute")) {
				Activity com = new Activity("compute",b,a);
				all.get(a).add(com);
			}
			else if(name.equals("terminate")) {
				Activity ter = new Activity("terminate",a);
				all.get(a).add(ter);
			}
		}
		
		
		while(initiate.get(1).poll()!=null) {
			cycle++;
		}
//		System.out.println("finish initiating at cycle " + cycle );
		
		
		///each cycle
		
		while(nonterminated!=0) {
			
			//update cycle
			cycle++;
//			System.out.println("cycle:"+cycle);
			
			//update resource unit
			for(int i = 1;i<=resourceNum;i++) {
				unit[i]+=pending[i];
				pending[i] = 0;
			}
			
			
			///check blocked activities by calling isSafe() defined at the bottom,
			///if it's safe, release it; if not, add it back to block queue
			
			int size = blocked.size();
			for(int i = 0; i<size;i++) {
				Activity b = blocked.poll();
				//only process if the task is not computing
				if(compute[b.getTask()]==1) {
					//blocked request can be granted (is safe), release it
					if(isSafe(b)) {
						int t = b.getTask();
						unit[b.getRc()]-=b.getNum();
						own[t][b.getRc()]+=b.getNum();
						running.set(t,no);	//put a no in the running list
						hasBlocked[t] = false;	//set this task as hasBlocked 
//						System.out.println("Task "+t+" blocked is granted");
					}
					else {
						tasks.get(b.getTask()).addWaitTime();//update waiting time
						blocked.add(b);	//put it back to block queue
//						System.out.println("Task "+b.getTask()+" blocked is still blocked, requested resource "+b.getRc()+"for "+b.getNum()+"avail "+unit[b.getRc()]);
					}
						
				}
				
			}
			
			
			///process each task's next activity in queue
			
			for(int i = 1;i<=task;i++) {
				
				//if the task is still computing, skip
				if(compute[i]>1) {
					compute[i]--;
//					System.out.println("Task"+i+" computing -"+compute[i]);
					continue;
				}
				
				//if no activity of this task is running or being blocked, 
				//and if this task is not terminated or aborted, process its next activity
				if(hasBlocked[i]==false&&!tasks.get(i).getState().equals("terminated")&&!tasks.get(i).getState().equals("aborted")) {
					if(running.get(i).getName().equals("null")) {
						Activity act = all.get(i).poll();
						
						// if a task’s requests exceed its claims, abort it
						if(act.getName().equals("request")) {
							int rcType = act.getRc();
							if(own[i][rcType]+act.getNum()>claim[i][rcType]) {
								tasks.get(i).setAborted();
								pending[rcType]+=own[i][rcType];
								nonterminated--;
								System.out.println("During cycle "+(cycle-1)+"-"+cycle+" of Banker's algorithm");
								System.out.println("Task "+i+"'s request exceeds its claim; aborted; "+own[i][rcType]+" units available next cycle");
								continue;
							}
							//request is safe, can be granted
							if(isSafe(act)) {
								running.set(i,act);
							}
								
							//add unsafe request to block queue
							else {
								blocked.add(act);
								hasBlocked[i] = true;
								tasks.get(i).addWaitTime();
//								System.out.println("Task "+act.getTask()+" request resource"+rcType+" "+act.getNum()+"blocked, avail "+unit[rcType]);
								continue;
							}
								
						}
						else
							running.set(i, act);	//put the activity in running list
					}
					

					
					///do running
					
					Activity act = running.get(i);
					if(act.getName().equals("request")) {
						int rcType = act.getRc();
						//update available unit of resource
						unit[rcType]-=act.getNum();
						own[act.getTask()][rcType]+=act.getNum();
//						System.out.println("Task "+act.getTask()+" request resource"+rcType+" "+act.getNum()+"granted, "+unit[rcType]+" available");
					}
					else if(act.getName().equals("release")) {
						pending[act.getRc()]+=act.getNum();
						own[act.getTask()][act.getRc()]-=act.getNum();
//						System.out.println("Task "+act.getTask()+"release "+act.getRc()+" for "+act.getNum() );
					}
					
					else if(act.getName().equals("compute")) {
						compute[act.getTask()] = act.getNum();
//						System.out.println("Task"+act.getTask()+ "starts computing "+act.getNum());
					}
					
					else if(act.getName().equals("terminate")) {
						tasks.get(act.getTask()).setTerminateTime(cycle-1);
						tasks.get(act.getTask()).setTerminated();
						nonterminated--;
//						System.out.println("Task " +act.getTask()+"terminates");
					}
					//reset running to null
					running.set(i,nil);
				}			
			}

	}
		
		///print result
		
		System.out.printf("%17s","Banker");
		System.out.println();
		int totalWait=0;
		int totalTime=0;
		for(int i = 1;i<=task;i++) {
			
			if(tasks.get(i).getState().equals("aborted")) {
				System.out.print("Task "+i);
				System.out.printf( "%16s","aborted");
				System.out.println();
			}
			else {
				int time = tasks.get(i).getTerminateTime();
				totalTime+=time;
				int wait =  tasks.get(i).getWaitTime();
				totalWait+=wait;
				System.out.printf("Task "+i);
				System.out.printf( "%10d %4d %4d",time,wait,(int)Math.round(wait*100.0/time));
				System.out.println("%");
			}
			
		}
		System.out.printf("total %10d %4d %4d",totalTime,totalWait,(int)Math.round(totalWait*100.0/totalTime));
		System.out.println("%");
	}
	
	
	/*
	 * A method that checks if a request is safe according to banker's algorithm
	 */
	public static boolean isSafe(Activity a) {
		int t = a.getTask();
		for(int i =1;i<=rc;i++) {
			//if a task's maximum need is greater than available resource, it's not safe
			if(claim[t][i]-own[t][i]>unit[i]) 
				return false;
		}
		return true;
	}
}
