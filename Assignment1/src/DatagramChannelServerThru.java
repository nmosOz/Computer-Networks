import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DatagramChannelServerThru   {

   static long key = 123456;
   static int byteCounter = 0;

   public static void main(String[] args) throws IOException {

      SocketAddress remoteAdd;
      //Open the dg server
      DatagramChannel server = DatagramChannel.open();

      //Open the socket on the port you want 
      InetSocketAddress iAdd = new InetSocketAddress("localhost", 8989);

      //Bind the server to the port
      server.bind(iAdd);

      int NUM_LONG_SM = 8;
      int NUM_LONG_MED = 32;
      int NUM_LONG_LG = 128;
  
      long ack = 1234567;
      long bad = 1234568;
      long[] tenTwentyFour = new long[NUM_LONG_LG];
      long[] twoFiftySix = new long[NUM_LONG_MED];
      long[] sixtyFour = new long[NUM_LONG_SM];
      int counter = 0;
  
      ByteBuffer lgBuf = ByteBuffer.allocate(1024);
      ByteBuffer mdBuf = ByteBuffer.allocate(256);
      ByteBuffer smBuf = ByteBuffer.allocate(64);
      ByteBuffer respBuf = ByteBuffer.allocate(8);
  
      long response = xorShift8((long)10101010);
      long endTime;
      double time;
  
      for(int i = 0; i < 30; i++){

        for(int j = 0; j < 4096; j++){

          //Clear the buffers (just to be sure)
          //smBuf.clear();
          mdBuf.clear();
          //lgBuf.clear();
  
          //Receive the buffer from client 
          //remoteAdd = server.receive(smBuf);
          remoteAdd = server.receive(mdBuf);
          //remoteAdd = server.receive(lgBuf);
  
          //Flip the buffer
          //smBuf.flip();
          mdBuf.flip();
          //lgBuf.flip();
  
          //Get the message from the buffer
          // for(int k = 0; k < 8; k++){
          //   sixtyFour[k] = smBuf.getLong();
          // }
          for(int k = 0; k < 32; k++){
            twoFiftySix[k] = mdBuf.getLong();
          }
          /*for(int i = 0; i < 128; i++){
            tenTwentyFour[k] = buffer.getLong();
          }*/
  
          //Decode the message 
          //sixtyFour = encodeString(sixtyFour);
          twoFiftySix = encodeString(twoFiftySix);
          //tenTwentyFour = encodeString(tenTwentyFour);

          //System.out.println(sixtyFour[0]);
          //Validate the message
          if(twoFiftySix[0] == (long)10101010){

            //rewind response buffer
            respBuf.clear();

            //put response message in buffer
            respBuf.putLong((long)10101010);

            //Send response 
            server.send(respBuf, remoteAdd);
          }


        }
  
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