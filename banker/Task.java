package banker;

/**
 * Class storing information of a task object
 * 
 * @author aubreyzhou
 *
 */
public class Task {
	int num; 	//index of the task
	String state; 	//state of the task, including initiated, aborted and terminated
	int waitTime;	//total time this task has waited
	int terminateTime;	//time that this task terminates
	
	public Task(int num) {
		this.num = num;
		this.state = "initiated";
	}
	
	
	public int getNum() {
		return this.num;
	}
	
	public void addWaitTime() {
		waitTime++;
	}
	
	public int getWaitTime() {
		return this.waitTime;
	}
	
	public int getTerminateTime() {
		return this.terminateTime;
	}
	
	public void setTerminateTime(int t) {
		this.terminateTime = t;
	}
	
	public void setTerminated() {
		this.state = "terminated";
	}
	
	public void setAborted() {
		this.state = "aborted";
	}
	
	public String getState() {
		return this.state;
	}
	
}
