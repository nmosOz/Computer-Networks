import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;

public class SimpleService {
  static final int PORT = 26921;
  static long byteCounter = 0;
  static long key = 1234567;
  static int shiftCounter = 0;

  public static void main(String[] args) {
    
    int NUM_LONG_SM = 8;
    int NUM_LONG_MED = 32;
    int NUM_LONG_LG = 128;

    long echo;
    long ack = 1234567;
    long bad = 1234568;
    long mess;
    long[] tenTwentyFour = new long[NUM_LONG_LG];
    long[] twoFiftySix = new long[NUM_LONG_MED];
    long[] sixtyFour = new long[NUM_LONG_SM];
    long[] fiveTwelve = new long[64];
    String cmd;
    int counter = 0;

    ByteBuffer lgBuf = ByteBuffer.allocate(1024);
    byte []longMsg = new byte[1024];
    ByteBuffer mdBuf = ByteBuffer.allocate(256);
    byte []medMsg = new byte[256];
    ByteBuffer smBuf = ByteBuffer.allocate(64);
    byte []smMsg = new byte[64];

    ByteBuffer fiveTwelveBuff = ByteBuffer.allocate(512);
    byte []fiveTwelveArray = new byte[512];


    try {
      ServerSocket serverSocket = new ServerSocket(PORT);
        Socket client = serverSocket.accept();

        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        out.flush();
        DataInputStream in =
          new DataInputStream(client.getInputStream());
	
      //encrypt the response messages
      //ack = xorShift8(ack);
      //bad = xorShift8(bad);
      for (int i = 0; i < 30; i++) {

        //For the eight byte long, we must first grab it out of the string
        //message = Long.parseLong(cmd);
        //System.out.println(message);

        /****** This part is for measuring RTT *******/
        
        //8 BYTES
        //mess = in.readLong();

        //64 BYTES
        //in.read(smMsg, 0, 64);
        //smBuf.put(smMsg);

        //smBuf.flip();

        //512 BYTES
        in.read(fiveTwelveArray, 0, 512);
        fiveTwelveBuff.put(fiveTwelveArray);

        fiveTwelveBuff.flip();

        /*while(counter < NUM_LONG_SM){
          sixtyFour[counter] = smBuf.getLong();
          counter++;
        }*/
        while(counter < 64){
          fiveTwelve[counter] = fiveTwelveBuff.getLong();
          counter++;
        }
        counter = 0;

        //sixtyFour = encodeString(sixtyFour);

        fiveTwelve = encodeString(fiveTwelve);

        // if(sixtyFour[0] == (long)10101010){
        //   sixtyFour = encodeString(sixtyFour);
        //   //System.out.println(key);
        //   smBuf.clear();
        //   smBuf.asLongBuffer().put(sixtyFour);
        //   smBuf.get(smMsg);
        //   out.write(smMsg);
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
        //System.out.println(shiftCounter);

        //xorShift(key);


        //Un xor the message
        //mess = xorShift8(mess);   //8 Byte Message
        //message = encodeString(message);
        
        //Validate the message 
        /*if(mess == (long)10101010){
          out.writeLong(ack);
        }
        else{
          out.writeLong(bad);
        }*/



      }
        
        //message = encodeString(message);
        //out.writeObject(message);

        /************ This part is for measuring throughput ************/
        //for (int j = 0; j < 1024; i++){
        //for(int j = 0; j < 16384; j++){
        //for (int j = 0; j < 4096; j++){
          //***********READ IN MESSAGE ***************/
          //in.read(longMsg, 0, 1024);
          //lgBuf.put(longMsg);

          //in.read(medMsg, 0, 256);
          //mdBuf.put(medMsg);

          //in.read(smMsg, 0, 64);
          //smBuf.put(smMsg);

          //lgBuf.flip();
          //mdBuf.flip();
          //smBuf.flip();

       /*  while(counter < NUM_LONG_LG){
          tenTwentyFour[counter] = lgBuf.getLong();
          counter++;
           }*/
          /*while(counter < NUM_LONG_MED){
             twoFiftySix[counter] = mdBuf.getLong();
             counter++;
          }*/
          /*while(counter < NUM_LONG_SM){
            sixtyFour[counter] = smBuf.getLong();
            counter++;
          }*/
          //counter = 0;

          //********** DECODE NUMBERS *************//

          /*tenTwentyFour = encodeString(tenTwentyFour);
          //twoFiftySix = encodeString(twoFiftySix);
          //sixtyFour = encodeString(sixtyFour);
          if(tenTwentyFour[0] == (long)10101010){
            //System.out.println("good");
            out.writeLong(ack);
            out.flush();
          }
          else{
            //System.out.println(tenTwentyFour[0]);
            //System.out.println(twoFiftySix[0]);
            System.out.println(key);
            out.writeLong(key);
          }
          lgBuf.clear();
          //mdBuf.clear();
          //smBuf.clear();
          //
        }
        
        System.out.println("current key -" + key);
        //xorShift(key);

      }*/

      out.close();
      in.close();
      client.close();
        
    }
    catch (IOException ex) {
      ex.printStackTrace();
      System.exit(-1);
    }
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
      //System.out.println("Byte Counter - " + byteCounter);
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
    shiftCounter++;
    return r; 
  }

}
