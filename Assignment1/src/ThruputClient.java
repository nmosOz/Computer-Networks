import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class ThruputClient {

    static int byteCounter = 0;
    static long key = 1234567;
    static long message = 10101010;
    static int shiftCounter = 0;
    public static void main(String[] args) {
    
    String host = "localhost";
    int echoServicePortNumber = 26921;

    Socket echoSocket = null;

    long response;

    long sixtyFour[];
    long twoFiftySix[];
    long tenTwentyFour[];

    DataOutputStream out = null;
    DataInputStream in = null;

    //time
    long startTime;
    long elapsedTime;

    ByteBuffer lgBuf = ByteBuffer.allocate(1024);
    byte []longMsg = new byte[1024];
    ByteBuffer mdBuf = ByteBuffer.allocate(256);
    byte []medMsg = new byte[256];
    ByteBuffer smBuf = ByteBuffer.allocate(64);
    byte []smMsg = new byte[64];



        try {
        echoSocket = new Socket(host, echoServicePortNumber);
        out = new DataOutputStream(echoSocket.getOutputStream());
        out.flush();
        in =
          new DataInputStream(echoSocket.getInputStream());
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
            BufferedReader stdIn = 
                new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            while ((userInput = stdIn.readLine()) != null){

                sixtyFour = sixtyFourMessage();
                //twoFiftySix = twoFifitySixMessage();
                //tenTwentyFour = tenTwentyFourMessage();
                //System.out.println("Starting data transfer...");

                //Start the timer
                startTime = System.nanoTime();
                //Loop through the array and send each message 
                for(int i = 0; i < 16384; i++){

                    smBuf.clear();
                    smBuf.asLongBuffer().put(sixtyFour);
                    smBuf.get(smMsg);
                    out.write(smMsg);
                    out.flush();

                    //System.out.println("First num -" + tenTwentyFour[0]);
                    response = in.readLong();

                    if(response == (long)1234567){
                        sixtyFour = sixtyFourMessage();
                        //System.out.println("good");
                    }
                    else{
                        System.out.println("Breaking");
                        //System.exit(0);
                    }

                }
                //System.out.println(shiftCounter);

                /*for(int i = 0; i < 4096; i++){

                    mdBuf.clear();
                    mdBuf.asLongBuffer().put(twoFiftySix);
                    mdBuf.get(medMsg);
                    out.write(medMsg);
                    out.flush();

                    //System.out.println("First num -" + tenTwentyFour[0]);
                    response = in.readLong();

                    if(response == (long)1234567){
                        twoFiftySix = twoFifitySixMessage();
                        //System.out.println("good");
                    }
                    else{
                        System.out.println("current key -" + key);
                        System.out.println("Breaking");
                        //System.exit(0);
                    }
                }*/
                System.out.println("current key -" + key);

                /*for(int i = 0; i < 1024; i++){
                    lgBuf.clear();
                    lgBuf.asLongBuffer().put(tenTwentyFour);
                    lgBuf.get(longMsg);
                    out.write(longMsg);
                    out.flush();
                    
                    //System.out.println("First num -" + tenTwentyFour[0]);
                    response = in.readLong();

                    if(response == (long)1234567){
                        tenTwentyFour = tenTwentyFourMessage();
                        //System.out.println("good");
                    }
                    else{
                        //System.out.println(key);
                        //System.out.println(shiftCounter);
                        System.out.println("Breaking");
                        System.exit(0);
                    }
                }*/
                

                //Measure time 
                elapsedTime = System.nanoTime() - startTime;

                //Print out time
                double elapsedTimeS = (double) elapsedTime / 1000000000;
                System.out.println("Time to transfer 16384 messages (seconds)-- " + elapsedTimeS);
                System.out.println(key);

                out.flush();

                
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

    static long[] sixtyFourMessage(){

    //String that will be the message
    long[] deMes = new long[8];
    Arrays.fill(deMes, message);

    long[] xorMessage = encodeString(deMes);

    return xorMessage;

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

    static long xorShift(long r) {
        //System.out.println("Shifting key");
        r ^= r << 13; r ^= r >>> 7; r ^= r << 17; 
        //System.out.println("New key --- " + r);
        shiftCounter++;
        return r; 
      }
}

