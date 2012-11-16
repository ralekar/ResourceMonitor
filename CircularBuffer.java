import java.io.*;  
import java.lang.*;
import java.util.HashMap;

public class CircularBuffer {
      
      private int rear , front;
      private  static final int MAX_SIZE = 10000 ;
      public DataObject Queue[];
      CircularBuffer() {
    	  this.rear = this.front = -1;
    	  Queue = new DataObject[MAX_SIZE];
      }
      public void enqueue(DataObject obj,HashMap tempServerInfo) {
    	  this.rear = (this.rear + 1) % MAX_SIZE;
    	  tempServerInfo.put(obj.serverName, 0); // the key is server_name , but value is a counter if down.
    	  if ( this.rear != this.front)
    	  Queue[this.rear] = obj;
    	  //System.out.println(" in enqueue " + obj.memoryUsed);
      }
      public DataObject dequeue(int index) {
    	 return Queue[index];
      }
      public int forwardPointer(){
    	  return this.rear;
      }
      public void QueueDisplay() {
    	  for(int i = 0; i < 10  ; i++) {
    		  try {
    		  System.out.println(" the CPUUsage is :" + Queue[i].cpuUsage + " Memory Used :" + Queue[i].memoryUsed) ;
    	       }
    		  catch(NullPointerException np){
    		   }
    	    }
    	}
      }
