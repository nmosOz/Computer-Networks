import java.net.*;
import java.nio.charset.Charset;
import java.io.*;

public class SimpleService2 {
  static final int PORT = 26921;
  static long byteCounter = 0;
  static long key = 1234567;
  static int shiftCounter = 0;

  public static void main(String[] args) {

    long echo;
    long ack = 1234567;
    long bad = 1234568;
    String message;
    String cmd;


    try {
      ServerSocket serverSocket = new ServerSocket(PORT);
        Socket client = serverSocket.accept();

        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        out.flush();
        BufferedReader in =
          new BufferedReader(new InputStreamReader(client.getInputStream()));
	
      //encrypt the response messages
      //ack = xorShift8(ack);
      //bad = xorShift8(bad);
      for (int i = 0; i < 30; i++) {

        //For the eight byte long, we must first grab it out of the string
        //message = Long.parseLong(cmd);
        //System.out.println(message);

        /****** This part is for measuring RTT *******/
        /*message = (long[])in.readObject();
        //message = Long.parseLong(cmd);
        //Un xor the message
        //decryptedMessage = xorShift8(message);   //8 Byte Message
        message = encodeString(message);
        
        //Validate the message 
        for(int j = 0; j < message.length; j++){
          if(message[j] != (long)10101010){
            System.out.println(message[j]);
            out.writeObject("bad");
          }
        }

        
        message = encodeString(message);
        out.writeObject(message);*/

        /************ This part is for measuring throughput ************/
        //for (int j = 0; j < 1024; i++){
        for(int j = 0; j < 16384; j++){
        //for (int j = 0; j < 4096; j++){
          message = in.readLine();

          out.println(message);
          message = encodeString(message);
          System.out.println(message.getBytes().length);
          //Making sure the message is the same number of bytes
          if(message.getBytes().length == 64){
            message = encodeString(message);
            System.out.println(message);
            out.println((long)12345);
            serverSocket.close();
        }
          else{
            System.out.println("bad");
          }
          System.out.println("tttt");
        }
        xorShift(key);

        System.out.println("current key -" + key);

      }

    out.close();
    in.close();
    client.close();
        
    }
    catch (IOException ex) {
      ex.printStackTrace();
      System.exit(-1);
    }
  }

  
    static String encodeString(String input){
    
        //System.out.println("encoding with - " + key);
        char c[] = input.toCharArray(); 
        for(int i = 0; i < c.length; i++){
          c[i] ^= key; 
          c[i] ^= key; 
          c[i] ^= key;  
          byteCounter ++;
          //System.out.println("Encoded messages - " + i);
          if(byteCounter == 64){
            byteCounter = 0;
            key = xorShift(key);
          }
        }
        return String.valueOf(c);
    }

  static long xorShift(long r) {
    //System.out.println("Shifting key");
    r ^= r << 13; r ^= r >>> 7; r ^= r << 17; 
    //System.out.println("New key --- " + r);
    //System.out.println(shiftCounter++);
    return r; 
  }

}
