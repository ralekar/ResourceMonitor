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
import java.util.HashMap;
public class DataReciever implements Runnable, ExceptionListener {
	
	private CircularBuffer Buff;
	private HashMap tempServerInfo;
	
	   DataReciever(CircularBuffer Buffer, HashMap tempHash) {
	     
		    this.Buff=Buffer;
	        this.tempServerInfo = tempHash;
	    }
        public void run() {
            try {
            	
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://129.79.49.181:61617");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                connection.setExceptionListener(this);

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("Group03_XX1.FOO");

                // Create a MessageConsumer from the Session to the Topic or Queue
                MessageConsumer consumer = session.createConsumer(destination);
                boolean val = true;
                // Wait for a message
                int counter = 0;
                int serverCount = 0;
                while (val) {
                	
               Message message = consumer.receive(1000);
               //if (counter == 30) {
            	 //  	System.out.println("----------------count----------");
             //  }
				
                 /*
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String text = textMessage.getText();
                    System.out.println("Received: " + text);
                } else {
                    System.out.println("Received: " + message);
                } 
               */
               if (message instanceof ObjectMessage) {
            	    System.out.println("recevied a Object Message");
                    ObjectMessage msg = (ObjectMessage)message; 
                    
                	DataObject obj = (DataObject) msg.getObject();
                	long currentTime = System.currentTimeMillis()/1000; //its in seconds
                	//if(currentTime - obj.timestamp < 500 )  // If old message appears
                	//{
                		Buff.enqueue(obj,tempServerInfo);
                	//}
                	 
                 }
               
               
              try{
             	  //do what you want to do before sleeping
             	  Thread.currentThread().sleep(500);//sleep for 1000 ms
             	  //do what you want to do after sleeping
             	}
             	catch(InterruptedException ie){
             	//If this thread was intrrupted by nother thread 
             	}
               //objectQ.QueueDisplay();
               } 
                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
		@Override
		public void onException(JMSException arg0) {
			// TODO Auto-generated method stub
			
		}

	}
