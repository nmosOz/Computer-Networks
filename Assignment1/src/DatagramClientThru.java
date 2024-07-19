import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;
import java.util.Arrays;

public class DatagramClientThru {

      static long key = 123456;
      static int byteCounter = 0;
      static long message = 10101010;

      static long[] sixtyFour = new long[8];
      static long[] fiveTwelve = new long[64];
  public static void main(String[] args) throws IOException {

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
    InetSocketAddress serverAddress = new InetSocketAddress("localhost",
      8989);


    for(int i = 0; i < 30; i++){

      //Start the timer 
      long startTime = System.nanoTime();

      //for(int j = 0; j < 16384; j++){
      for(int j = 0; j < 4096; j++){
      //for(int j = 0; j < 1024; j++){
        //Encrypt the message
        //sixtyFour = sixtyFourMessage();
        twoFiftySix = twoFifitySixMessage();
        //tenTwentyFour = tenTwentyFourMessage();

        //Put the message in the byte buffer
        //smBuf.asLongBuffer().put(sixtyFour);
        mdBuf.asLongBuffer().put(twoFiftySix);
        //lgBuf.asLongBuffer().put(tenTwentyFour);

        //Send the message to the server
        //client.send(smBuf, serverAddress);
        client.send(mdBuf, serverAddress);
        //client.send(lgBuf, serverAddress);

        //Clear the buffer
        //smBuf.clear();
        mdBuf.clear();
        //lgBuf.clear();

        //Receive the response message from the server
        respBuf.rewind();
        client.receive(respBuf);

        //System.out.println(respBuf.position());

        //respBuf.flip();

        //Read the message from the buffer
        response = respBuf.getLong();

        if(response != ack){
          System.exit(0);
        }
      }

        //Stop the timer
        endTime = System.nanoTime() - startTime;

        //Print out the time 
        time = (double)endTime / 1000000000;
        System.out.println("Time in seconds - " + time);

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

  static long[] twoFifitySixMessage(){

    //String that will be the message
    long[] deMes = new long[32];
    Arrays.fill(deMes, message);

    long[] xorMessage = encodeString(deMes);

    return xorMessage;

  }

  static long[] tenTwentyFourMessage(){

    //String that will be the message
    long[] deMes = new long[128];
    Arrays.fill(deMes, message);

    long[] xorMessage = encodeString(deMes);

    // for(int i = 0; i < xorMessage.length; i++){
    //     System.out.println(xorMessage[i]);
    // }

    return xorMessage;

}
}