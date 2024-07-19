import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;
import java.util.Arrays;

public class DatagramChannelClient {

      static long key = 123456;
      static int byteCounter = 0;
      static long message = 10101010;

      static long[] sixtyFour = new long[8];
      static long[] fiveTwelve = new long[64];
   public static void main(String[] args) throws IOException {

      long response;
      long endTime;
      double time;

      //Make the new datagram channel
      DatagramChannel client = null;

      //Open the client
      client = DatagramChannel.open();

      //Bind the client
      client.bind(null);

      //Open the socket 
      InetSocketAddress serverAddress = new InetSocketAddress("pi.cs.oswego.edu",
        8989);

      //Allocate the correct amount of bytes to the buffer
      ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
      //ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 8);
      //ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 64);

      for(int i = 0; i < 30; i++){
      //Encrypt the message
      message = xor8(message);
      //sixtyFour = sixtyFourMessage();
      //fiveTwelve = fiveTwelveMessage();

      //Put the message in the byte buffer
      buffer.asLongBuffer().put(message);
      //buffer.asLongBuffer().put(sixtyFour);
      //buffer.asLongBuffer().put(fiveTwelve);

      //Start the timer 
      long startTime = System.nanoTime();

      //Send the message to the server
      client.send(buffer, serverAddress);

      //Clear the buffer
      buffer.clear();

      //Receive the response message from the server
      client.receive(buffer);

      //Stop the timer
      endTime = System.nanoTime() - startTime;

      //Print out the time 
      time = (double)endTime / 1000000;
      System.out.println("Time in milliseconds - " + time);
      //Flip the buffer to 'read mode'
      buffer.flip();

      //String respose = bufferToString(buffer);
      //Read the message from the buffer
      response = buffer.getLong();
      /*for(int j = 0; j < sixtyFour.length; j++){
        sixtyFour[j] = buffer.getLong();
      }*/   

      /*for(int j = 0; j < fiveTwelve.length; j++){
        fiveTwelve[j] = buffer.getLong();
      }*/

      //Decrypt the message
      response = xor8(response);
      //sixtyFour = encodeString(sixtyFour);
      //fiveTwelve = encodeString(fiveTwelve);

      //System.out.println(response);

      buffer.flip();
      }
      //Close the connection to the server
      client.close();
    
   }

  //XOR shift (8 Bytes)
  static long xor8(long r) { 
   r ^= key; 
   r ^= key; 
   r ^= key; 
   byteCounter += 8;
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
   return input;
  }

  static long eightByteMessage(){

    //Create random number generator
    SecureRandom sr = new SecureRandom();

    //Create a message that is 8 bytes long 
    byte[] message = new byte[8];
    
    //Populate the byte array 
    sr.nextBytes(message);

    //Convert the byte array to a long for XORing 
    long xorMessage = new BigInteger(message).longValue();

    //Print the number before sending it so we know what it is
    System.out.println("Generated Number - " + xorMessage);
    //XOR the number before sending it  
    xorMessage = xor8(xorMessage);

    //Print the xor'd number
    //System.out.println("XOR'd Number - " + xorMessage);

    return xorMessage;
  }

  static long[] sixtyFourMessage(){

    //String that will be the message
    long[] deMes = new long[8];
    Arrays.fill(deMes, message);

    long[] xorMessage = encodeString(deMes);

    return xorMessage;

  }

  static long[] fiveTwelveMessage(){

    //String that will be the message
    long[] deMes = new long[64];
    Arrays.fill(deMes, message);

    //Encode string before returning it 
    long[] xorMessage = encodeString(deMes);

    //Print the xor'd number for debugging
    //System.out.println("XOR'd Message - " + xorMessage);

    return xorMessage;
  }

  static long xorShift(long r) {
   //System.out.println("Shifting key");
   r ^= r << 13; r ^= r >>> 7; r ^= r << 17; 
   //System.out.println("New key --- " + r);
   return r; 
 }
}