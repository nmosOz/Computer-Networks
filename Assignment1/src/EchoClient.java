import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class EchoClient {

  static int byteCounter = 0;
  static long key = 1234567;
  static long message = 10101010;
  static int shiftCounter = 0;
  public static void main(String[] args) {
    String host = "pi.cs.oswego.edu";
    int echoServicePortNumber = 26921;

    Socket echoSocket = null;
    DataOutputStream out = null;
    DataInputStream in = null;

    long startTime;
    long elapsedTime;
    long[] sixtyFour = new long[8];
    long[] fiveTwelve = new long[64];

    ByteBuffer smBuf = ByteBuffer.allocate(64);
    byte []smMsg = new byte[64];

    ByteBuffer fiveTwelveBuff = ByteBuffer.allocate(512);
    byte []fiveTwelveArray = new byte[512];


    int count = 0;
    int counter = 0;

    try {
      echoSocket = new Socket(host, echoServicePortNumber);

      out = new DataOutputStream(echoSocket.getOutputStream());
      in = new DataInputStream(echoSocket.getInputStream());
      // out = new PrintWriter(echoSocket.getOutputStream(), true);
      // in = new BufferedReader(new InputStreamReader(
      //                                echoSocket.getInputStream()));
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
      e.printStackTrace();
      System.exit(1);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection.");
      e.printStackTrace();
      System.exit(1);
    }

    try {
      // BufferedReader reader = new BufferedReader(
      //       new InputStreamReader(System.in));
      String userInput;

      while (count < 30){
        //
        System.out.println("Starting timer...");
        //Generate the message
        //long eight = 10101010;
        //sixtyFour = sixtyFourMessage();
        fiveTwelve = fiveTwelveMessage();

        //eight = xor8(eight);

        //System.out.println("Message before sending - " + sixtyFour);
        //Start time
        startTime = System.nanoTime();

        //Send message
        //out.writeLong(eight);
        //out.writeObject(sixtyFour);
        //out.writeObject(fiveTwelve);
        //out.flush();
        //Read back message
        //eight = in.readLong();

        //64 BYTES
        //in.read(smMsg, 0, 64);
        // smBuf.asLongBuffer().put(sixtyFour);
        // smBuf.get(smMsg);
        // out.write(smMsg);

        // smBuf.flip();

        //512 BYTES
        fiveTwelveBuff.asLongBuffer().put(fiveTwelve);
        fiveTwelveBuff.get(fiveTwelveArray);
        out.write(fiveTwelveArray);

        //smBuf.flip();

        fiveTwelveBuff.clear();
        in.read(fiveTwelveArray, 0, 512);
        fiveTwelveBuff.put(fiveTwelveArray);

        fiveTwelveBuff.flip();

        //in.read(smMsg); 
        //smBuf.put(smMsg);

        //smBuf.flip();

        // while(counter < 8){
        //   sixtyFour[count] = smBuf.getLong();
        //   counter++;
        // }
        while(counter < 64){
          fiveTwelve[counter] = fiveTwelveBuff.getLong();
          counter++;
        }
        counter = 0;

        //sixtyFour = encodeString(sixtyFour);

        fiveTwelve = encodeString(fiveTwelve);

        // if(sixtyFour[0] == (long)10101010){
        //   System.out.println("here");
        // }

        if(fiveTwelve[0] == (long)10101010){
          fiveTwelve = encodeString(fiveTwelve);
          fiveTwelveBuff.clear();
          fiveTwelveBuff.asLongBuffer().put(fiveTwelve);
          fiveTwelveBuff.get(fiveTwelveArray);
          out.write(fiveTwelveArray);
        }

        //smBuf.clear();
        fiveTwelveBuff.clear();

        //xorShift(key);

        //Stop time
        elapsedTime = System.nanoTime() - startTime;
        //Decrypt the echo'd message for validation 
        //long echoMsg = Long.parseLong(echoMessage);
        //eight = xor8(eight);
        //sixtyFour = encodeString(sixtyFour);
        //fiveTwelve = encodeString(fiveTwelve);
        //echoMessage = encodeString512(echoMessage);

        //Validate that the message is the same
        //System.out.println("echo: " + sixtyFour[0]);
        //Convert time to seconds 
        double elapsedTimeSeconds = (double) elapsedTime / 1000000;

        System.out.println("Elapsed time in milliseconds - " + elapsedTimeSeconds);
        //System.out.println(shiftCounter);
        count++;
      }
      
      out.close();
      in.close();
      echoSocket.close();
    }
    catch (IOException ex) {
      System.err.println("IO failure.");
      ex.printStackTrace();
    }
  }

  static long unXorShift(long r, long  seed) { r ^= seed >> 13; r ^= seed << 7; r ^= seed >> 17; return r; }

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

  //XOR shift (8 Bytes)
  static long xor8(long r) { 
    r ^= key; 
    r ^= key; 
    r ^= key; 
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

  //XOR the big string
  static String encodeString512(String input){
    
    
    char[] charArray = input.toCharArray();
    for(int i = 0; i < input.length(); i++){
      charArray[i] ^= key << 13; 
      charArray[i] ^= key >>> 7; 
      charArray[i] ^= key << 17;
      byteCounter += 8;
      if(byteCounter == 64){
        byteCounter = 0;
        key = xorShift(key);
      }
    }
    String output = String.valueOf(charArray);

    return output;   
  }

  static long xorShift(long r) {
    //System.out.println("Shifting key");
    r ^= r << 13; r ^= r >>> 7; r ^= r << 17; 
    shiftCounter++;
    //System.out.println("New key --- " + r);
    return r; 
  }

}
