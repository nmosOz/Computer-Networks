import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DatagramChannelServer {

   static long key = 123456;
   static int byteCounter = 0;

   static long[] sixtyFour = new long[8];
   static long[] fiveTwelve = new long[64];

   public static void main(String[] args) throws IOException {

      SocketAddress remoteAdd;
      //Open the dg server
      DatagramChannel server = DatagramChannel.open();

      //Open the socket on the port you want 
      InetSocketAddress iAdd = new InetSocketAddress("localhost", 8989);

      //Bind the server to the port
      server.bind(iAdd);

      //Print out a message that the server started
      System.out.println("Server Started: " + iAdd);

      //Allocate some bytes to the buffer  
      //ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 8);
      ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 64);

      //buffer.asLongBuffer();

      for(int i = 0; i < 30; i++){
      //receive buffer from client.
      remoteAdd = server.receive(buffer);

      //change mode of buffer
      buffer.flip();

      //Get the message from the buffer
      //long msg = buffer.getLong();
      /*for(int i = 0; i < 8; i++){
        sixtyFour[i] = buffer.getLong();
      }*/

      for(int j = 0; j < 64; j++){
        fiveTwelve[j] = buffer.getLong();
        //System.out.println(fiveTwelve[i]);
      }
      
      //Decode the message
      //msg = xorShift8(msg);
      //sixtyFour = encodeString(sixtyFour);
      fiveTwelve = encodeString(fiveTwelve);

      System.out.println("Message from client - " + fiveTwelve[0]);
      //Validate the message
      /*if(msg != (long)10101010){
        server.close();
      }*/
      /*for(int i = 0; i < sixtyFour.length; i++){
        if(sixtyFour[i] != (long)10101010){
          server.close();
        }
      }*/

      for(int j = 0; j < fiveTwelve.length; j++){
        if(fiveTwelve[j] != (long)10101010){
          server.close();
        }
      }

      //Flip the buffer again
      buffer.flip();

      //Re-encode the message 
      //msg = xorShift8(msg);
      //sixtyFour = encodeString(sixtyFour);
      fiveTwelve = encodeString(fiveTwelve);

      //Put the message back in the buffer
      //buffer.asLongBuffer().put(msg);
      //buffer.asLongBuffer().put(sixtyFour);
      buffer.asLongBuffer().put(fiveTwelve);

      //Send the message back 
      server.send(buffer,remoteAdd);

      buffer.clear();

     }

      //buffer.flip();

      //Close the server 
      server.close();
   }

  //XOR shift (8 Bytes)
  static long xorShift8(long r) { 
   r ^= key; 
   r ^= key; 
   r ^= key;
   byteCounter += 8; 
   if (byteCounter == 64){
     key = xorShift(key);
     byteCounter = 0;
   }
   return r; }
 
 //XOR shift (64 Bytes)
 static long[] encodeString(long []input){
   
   //System.out.println("encoding with - " + key);
   for(int i = 0; i < input.length; i++){
     input[i] ^= key; 
     input[i] ^= key; 
     input[i] ^= key;  
     byteCounter += 8;
     //System.out.println("Encoded messages - " + i);
     if(byteCounter == 64){
       byteCounter = 0;
       key = xorShift(key);
     }
   }
   
   //System.out.println(input[0]);
   return input;   
 } 

 static long xorShift(long r) {
   //System.out.println("Shifting key");
   r ^= r << 13; r ^= r >>> 7; r ^= r << 17; 
   //System.out.println("New key --- " + r);
   return r;
 }
}