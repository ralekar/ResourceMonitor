import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import java.util.Map;
import java.lang.*;
import java.net.*;
import java.io.*;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.ptql.ProcessFinder;

public class DataProducer implements Runnable {
	private static final String CMD_TYPE_MESSAGETYPE_OBJECT = null;
	private Sigar sigar = new Sigar();
	public DataObject objData = new DataObject(); 
	
	public void run() {
		try {
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://129.79.49.181:61617");

			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination destination = session.createQueue("Group03_XX1.FOO");
						
			// Create a MessageProducer from the Session to the Topic or Queue
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			//-----------------------memory--------------------
			
			ObjectMessage msg = session.createObjectMessage(objData);
			msg.setObject( objData );
			msg.setJMSType(this.CMD_TYPE_MESSAGETYPE_OBJECT); 
			//------------------------------
			 boolean flag = true;
			//int value = 100;
			
			while(flag) {
				
				
				FetchInformationsOfMemory();
				FetchInformationOfMemoryPid();
				FetchInformationOfCPU();
				FetchInformationOfCPUPid();
				FetchInformationOfTime();
				FetchInformationOfServer();
				
				
				
				//objData.timer = System.nanoTime();
				msg.setObject( objData );
				//getInformationAboutServer();
				producer.send(msg);
				System.out.println( " Sent  message:" +  objData.memoryUsed +  " :" + Thread.currentThread().getName() );
				//---------------------memory------------------------

				
                            
                 try{
                	  //do what you want to do before sleeping
                	  Thread.currentThread().sleep(1000);//sleep for 1000 ms
                	  //do what you want to do after sleeping
                	}
                	catch(InterruptedException ie){
                	//If this thread was intrrupted by nother thread 
                	} 
                } 


                // Clean up
                session.close();
                //session.commit();
                connection.close();
            }
            catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
		public   void FetchInformationsOfMemory() {
			Mem mem = null;
			try {
				mem = sigar.getMem();	
			} catch (SigarException se) {
				se.printStackTrace();
			}
			objData.memoryUsed = (double)mem.getUsedPercent();
		}
		public void FetchInformationOfCPU() {
			CpuPerc cpu = null;
			try {
				cpu = sigar.getCpuPerc();
			} catch (SigarException se) {
				se.printStackTrace();
			}
			objData.cpuUsage = cpu.getCombined()*100;
			
		}
		public void FetchInformationOfServer() throws UnknownHostException {
			InetAddress addr = InetAddress.getLocalHost();
			String hostname = addr.getHostName();
			objData.serverName = hostname;
			
		}
		public void FetchInformationOfTime(){
			objData.timestamp = System.currentTimeMillis()/1000;
		}
		public void FetchInformationOfCPUPid() throws IOException{
			//ps -p <pid> -o %cpu,%mem,cmd
			
			//ProcessFinder pf =  null;
			try {
		
				
			final String query = "State.Name.eq=mpi_main";
			long []mpiPids= ProcessFinder.find(sigar, query);
			Double tempPid = 0.0;
			for(int pid = 0 ; pid < mpiPids.length ; pid ++) {
				
				String cmd = "ps -o\"%C\" -p" + mpiPids[pid];
				Process child = Runtime.getRuntime().exec(cmd);
		     
	     	    // hook up child process output to parent
				InputStream lsOut = child.getInputStream();
				InputStreamReader r = new InputStreamReader(lsOut);
				BufferedReader in = new BufferedReader(r);
				in.readLine();
				String line = in.readLine();
				line=line.replaceAll("\" ", "");
				line = line.replaceAll("\"","");
				tempPid += Double.parseDouble(line);
				//System.out.println("tempPid : " + tempPid);
				//System.out.println( " the cpu% is " +sigar.getProcCpu(mpiPids[pid]).getPercent());
			}
		   if (mpiPids.length != 0) {
		       objData.cpuPidUsage = tempPid / mpiPids.length;
		 	}
		   else {
			   objData.cpuPidUsage = 0;
		   }
			}
			catch(Exception e) {
				objData.cpuPidUsage = 0;
			}
			
		}
		public void FetchInformationOfMemoryPid(){
			//ps -p <pid> -o %cpu,%mem,cmd
			try {
				
				
				final String query = "State.Name.eq=mpi_main";
				long []mpiPids= ProcessFinder.find(sigar, query);
				double tempMPid= 0.0;
				for(int pid = 0 ; pid < mpiPids.length ; pid ++) {
					
					String cmd = "ps -o %mem -p" + mpiPids[pid];
					Process child = Runtime.getRuntime().exec(cmd);
			     
		     	    // hook up child process output to parent
					InputStream lsOut = child.getInputStream();
					InputStreamReader r = new InputStreamReader(lsOut);
					BufferedReader in = new BufferedReader(r);
					in.readLine();
					String line = in.readLine();
					line=line.replaceAll("\" ", "");
					line = line.replaceAll("\"","");
					tempMPid +=  Double.parseDouble(line);
					}
				if (mpiPids.length != 0) {
					 objData.memoryPidUsed = tempMPid / mpiPids.length ;
				 }
				else {
					objData.memoryPidUsed = 0;
				}
				
				
			}
			catch( Exception e) {
				objData.memoryPidUsed = 0;
			}
			
		}
    }