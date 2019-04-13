package banker;

/**
 * Class storing an activity's information
 * 
 * @author aubreyzhou
 *
 */
public class Activity {
	private String name;//what this activity does, including initiate, request, release, compute, terminate
	private int task;	//index of task that this activity belongs to 
	private int rc;		//index of resource that this activity is for
	private int num;	//number of resource that this activity asks
	
	public Activity(String n) {
		this.name = n;
	}
	
	public Activity(String n, int t) {
		this.name = n;
		this.task = t;
	}
	
	public Activity(String n, int num, int t) {
		this.name = n;
		this.num = num;
		this.task = t;
	}
	
	public Activity(String n, int r, int num, int t) {
		this.name = n;
		this.rc = r;
		this.num = num;
		this.task = t;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getTask() {
		return this.task;
	}
	
	public int getRc() {
		return this.rc;
	}
	
	public int getNum() {
		return this.num;
	}
	
}
