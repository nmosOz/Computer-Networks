import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ThruputClient2 {

    static int byteCounter = 0;
    static long key = 1234567;
    static int shiftCounter = 0;
    //static int shiftCounter = 0;
    public static void main(String[] args) {
    
    String host = "localhost";
    int echoServicePortNumber = 26921;

    Socket echoSocket = null;

    long response;

    String sixtyFour;
    String twoFiftySix;
    String tenTwentyFour;

    DataInputStream in = null;
    DataOutputStream out = null;

    //time
    long startTime;
    long elapsedTime;

    byte[] msg = new byte[256];
    long ACK = 12345678;

    ByteBuffer buff = ByteBuffer.allocate(256);

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

                //sixtyFour = sixtyFourMessage();
                twoFiftySix = twoFifitySixMessage();
                //tenTwentyFour = tenTwentyFourMessage();
                System.out.println("Starting data transfer...");

                //Start the timer
                startTime = System.nanoTime();
                //Loop through the array and send each message 
                /*for(int i = 0; i < 16384; i++){
                    out.println(sixtyFour);
                    out.flush();

                    response = in.readLine();

                    //System.out.println(response);
                    //response = encodeString(response);

                    //System.out.println(response);
                    if(response.equals(ACK)){
                         sixtyFour = sixtyFourMessage();
                         System.out.println(sixtyFour);
                         //System.out.println("good");
                         //continue;
                    }

                }*/
                for(int i = 0; i < 3; i++){

                    twoFiftySix = twoFifitySixMessage();
                    msg = twoFiftySix.getBytes("UTF-8");
                    out.write(msg);

                    //System.out.println(msg);
                    //out.println(key);
                    out.flush();
                    
                    response = in.readLong();

                    //response = encodeString(response);
                    //System.out.println("Resp - " + response);
                    if(response == ACK){
                        twoFiftySix = twoFifitySixMessage();
                        //System.out.println(twoFiftySix);
                        //System.out.println("good");
                        //continue;
                   }
                }
                
                System.out.println("current key -" + key);
                /*for(int i = 0; i < 1024; i++){
                    out.println(tenTwentyFour);
                    out.flush();

                    response = in.readLine();

                    response = encodeString(response);

                    System.out.println(response);
                }*/
                

                //Measure time 
                elapsedTime = System.nanoTime() - startTime;

                //Print out time
                double elapsedTimeS = (double) elapsedTime / 1000000000;
                System.out.println("Time to transfer 16384 messages (seconds)-- " + elapsedTimeS);

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

    static String encodeString(String input){
    
        System.out.println("encoding with - " + key);
        char c[] = input.toCharArray(); 
        for(int i = 0; i < input.length(); i++){
          //System.out.println(key);
          c[i] ^= key; 
          c[i] ^= key; 
          c[i] ^= key;  
          byteCounter ++;
          //System.out.println("Encoded messages - " + i);
          if(byteCounter == 63){
            byteCounter = 0;
            key = xorShift(key);
          }
        }
        String output = String.valueOf(c);
        //System.out.println("length of string" + output.length());
        //System.out.println("length of array" + c.length);
        return output;
    }

    static String sixtyFourMessage(){

    //String that will be the message
    String message = "Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello hihi";

    //System.out.println(message.length());
    message = encodeString(message);

    return message;

    }

    static String twoFifitySixMessage(){

        //String that will be the message
        String message = "Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello " + 
        "Hello Hello Hello Hello hihi Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello hihi " +
        "Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello ";

    
        String xorMessage = encodeString(message);
    
        return xorMessage;
    
    }

    static String tenTwentyFourMessage(){

        //String that will be the message
        String message = "This is a joke I do not actually believe the industrial revolution has been a " +
        "disaster for the human race. The Industrial Revolution and its consequences have been a disaster " +
        "for the human race. They have greatly increased the life-expectancy of those of us who live in “advanced” " +
        "countries, but they have destabilized society, have made life unfulfilling, have subjected human beings to " +
        "indignities, have led to widespread psychological suffering (in the Third World to physical suffering as well) " +
        "and have inflicted severe damage on the natural world. The continued development of technology will worsen the " +
        "situation. It will certainly subject human being to greater indignities and inflict greater damage on the natural " +
        "world, it will probably lead to greater social disruption and psychological suffering, and it may lead to increased " +
        "physical suffering even in “advanced” countries. The industrial-technological system may survive or it may break down. " + 
        "That is the last sentence I could fit before the limit hi ";
    
        String xorMessage = encodeString(message);
    
        System.out.println(xorMessage.getBytes().length);
        return xorMessage;
    
    }

    static long xorShift(long r) {
        //System.out.println("Shifting key");
        r ^= r << 13; r ^= r >>> 7; r ^= r << 17; 
        System.out.println("New key --- " + r);
        System.out.println(shiftCounter++);
        return r; 
      }
}


