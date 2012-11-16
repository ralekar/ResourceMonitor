
public class GraphInitiator extends Thread{
	private DynamicDataDemo Graph;
	public GraphInitiator(DynamicDataDemo graph){
		this.Graph=graph;
		
	}
	public void run(){
		while(true){
			Graph.summarize();
			try {
				sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
	
	


