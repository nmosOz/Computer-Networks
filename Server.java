import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.Random;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Server {

  static String fileName;
  static long key;
  static ByteBuffer buffers[];
  static int counter = 0;
  static int shiftCount = 0;
  static short windowSize;
  static short toDrop;

  static double rttVar;
  static double srtt;
  static double rto;
  static double alpha = .125;
  static double beta = .25;
  static int k = 4;

    public static void main(String[] args) throws Exception {

      Random rand = new Random();

      int PACKET_SIZE = 512;

      int request;
      int rem;
      int expectedNum = 0;
      long xor;
      long clientKey;
      short opcode;
      short blockNum;
      short laf;
      short lfr;
      short seqNumToAck;
      short lar;
      
      byte[] imgData = new byte[512];
      byte[] remData;
      byte[] img;
      byte[] ack;

      //Total time
      long startTime;
      long endTime;
      long elapsedTime;
      long startTimeRTO = 0;
      long endTimeRTO = 0;

      int numKeys;

      //int windowSize = 5; 
      short[] ackArr = new short[25];

      //Initial packet size will always be 12 bytes
      ByteBuffer sidPack = ByteBuffer.allocate(12);
      ByteBuffer clientXor = ByteBuffer.allocate(Long.BYTES);
      ByteBuffer initialPacket = ByteBuffer.allocate(100);
      ByteBuffer image = ByteBuffer.allocate(5000000);
      ByteBuffer dataPacket = ByteBuffer.allocate(516 );
      ByteBuffer ackPack = ByteBuffer.allocate(4);

      boolean hasDropped = false;

      Path path = Paths.get("./leaves1.jpg");
        
      SocketAddress remoteAdd;
      //Open the dg server
      Selector selector = Selector.open();
      DatagramChannel server = DatagramChannel.open();

      //Open the socket on the port you want 
      InetSocketAddress iAdd = new InetSocketAddress("localhost", 8989);

      //Bind the server to the port
      server.bind(iAdd);
      server.socket().setSoTimeout(10);

      //Print out a message that the server started
      System.out.println("Server Started: " + iAdd);

      //Receive the packet with the sender ID and the random number
      remoteAdd = server.receive(sidPack);

      //Send an ACK with a random number
      xor = rand.nextLong();
      clientXor.putLong(xor);
      remData = clientXor.array();
      clientXor = ByteBuffer.wrap(remData);
      System.out.println(clientXor.toString());
      rto = calculateRTO(startTimeRTO, endTimeRTO);
      startTimeRTO = System.nanoTime();
      server.send(clientXor, remoteAdd);

      //Get the key
      clientKey = sidPacket(sidPack);
      endTimeRTO = System.nanoTime();
      rto = calculateRTO(startTimeRTO, endTimeRTO);
      key = xor ^ clientKey;

      System.out.println(xor);
      System.out.println(clientKey);
      System.out.println(key);

      //Receive the first packet with the OPCODE 
      remoteAdd = server.receive(initialPacket);

      request = initial_packetR(initialPacket);

      laf = (short)(windowSize + 0);
      lfr = 0;
      seqNumToAck = 0;
      //START RECEIVING IMAGE
      if(request == 2){ 
        startTime = System.nanoTime();
        while(true){
            dataPacket.clear();
            server.receive(dataPacket);
            dataPacket.flip();

            //System.out.println(dataPacket.hashCode());
            //System.out.println(dataPacket.toString());
            //Check to see if the size is <516
            if(dataPacket.limit() < 516){

              //System.out.println("last packet");
              ackPack.clear();
              //Grab all the data
              opcode = dataPacket.getShort();
              blockNum = dataPacket.getShort();
              //System.out.println(opcode);
              //System.out.println(blockNum);
              rem = dataPacket.remaining();
              remData = new byte[rem];
              dataPacket.get(remData);

              //Send last ack
              ackPack.putShort((short)4);
              ackPack.putShort(blockNum);
              
              server.send(ackPack, remoteAdd);

              //close connection 
              //server.close();

              //Put the remaining bytes in the buffer
              image.put(blockNum*PACKET_SIZE, remData);
              image.position(blockNum*PACKET_SIZE+remData.length);
              //System.out.println(image.toString());

              //break 
              break;
            }
            else{

              //Grab opcode
              opcode = dataPacket.getShort();

              //Grab block number
              blockNum = dataPacket.getShort();
              //System.out.println(opcode);
              //System.out.println(blockNum);

              //Check to make sure the frame received is w/in the window
              if(lfr < blockNum && blockNum <= laf || blockNum == 0){
                if(blockNum == toDrop && hasDropped == false && blockNum != 0){
                  System.out.println("dropped");
                  hasDropped = true;
                  continue;
                }

                //System.out.println("block num " + blockNum);
                //Grab data 
                dataPacket.get(imgData);
                //System.out.println("Putting image at " + blockNum*PACKET_SIZE);
                image.put(blockNum*PACKET_SIZE, imgData);
                if(image.position() > blockNum*PACKET_SIZE){
                  System.out.println("continuing");
                  //continue;
                }
                else{
                  image.position(blockNum*(PACKET_SIZE));
                }
                if(blockNum == lfr + 1|| lfr == 0 || ackArr[0] != 0){
                  
                  //Check to see if you have any buffered acks
                  if(ackArr[0] != 0){
                    blockNum = ackArr[0];
                    ackArr[0] = 0;
                  }
                  System.out.println("sending ack for " + blockNum);
                  //Send the ack 
                  ackPack.clear();
                  ackPack.putShort((short)4);
                  ackPack.putShort(blockNum);
                  ack = ackPack.array();
                  ackPack = ByteBuffer.wrap(ack);
                  //change largest acceptable frame
                  //
                  System.out.println("lfr " + lfr);
                  System.out.println("laf " + laf);
                  lfr = blockNum;
                  laf = (short)(lfr + windowSize);
                  //System.out.println(ackPack.toString());
                  server.send(ackPack, remoteAdd);
                    
                }
                //Else the seqNumToAck is not what we expected, so we buffer until the one we get is what we expect
                else{
                  //Buffer the ack in an array 
                  //System.out.println("storing ack at " + (laf-blockNum));
                  ackArr[laf - blockNum] = blockNum;
                  
                }

              }
              else{
                continue;
              }
            }
        }
        endTime = System.nanoTime();
        elapsedTime = endTime - startTime;
        System.out.println((double)(elapsedTime / 1000000) + " milliseconds");        

        //Write out the stuff to an image file 
        try {

            //decode the data 
            System.out.println(image.toString());
            image.flip();
            img = new byte[image.remaining()];
            image.get(img);
            img = decode(img);
            Files.write(path, img, StandardOpenOption.CREATE);
            System.out.println("writing");
          
        } catch (Exception e) {
          // TODO: handle exception
          e.printStackTrace();
        }
      }
      else if(request == 1){
            try{
              byte[] bytes = Files.readAllBytes(Paths.get("./leaves.jpg"));

                //Construct buffers2
                server.configureBlocking(false);
                server.register(selector, SelectionKey.OP_READ);
                constructBuffers(bytes);
                //writeFile(buffers);
                int base = 0;  
                int counter = 0; 
                int lastFrameSent = 0;
                //*************************SLIDING WINDOW PROTOCOL START NO PACKET DROPS**********************************//
                //start timer
                startTime = System.nanoTime();

                lar = 0;

                while(base < buffers.length-1){
                  startTime = System.nanoTime();
                  while(base < buffers.length-1){
                      for(int i = base; i < base + windowSize && i < buffers.length; i++){
                          //Check to make sure the frame you are sending hasnt been sent already
  
                          if(i <= lastFrameSent && i != 0){
                              //System.out.println("cont");
                              continue;
                          }
                          //Send the buffer
                          //System.out.println("Buffer " + i + " " + buffers[i].hashCode());
                          startTimeRTO = System.nanoTime();
                          server.send(buffers[i], remoteAdd);
                          lastFrameSent = i;
                          //System.out.println("LFS " + lastFrameSent);
                      }
                      ackPack.clear();
  
                      selector.selectedKeys().clear();
                      //System.out.println(rto);
                      numKeys = selector.select((long)rto);
                      //System.out.println("Keys " + numKeys);
                      //System.out.println("RTO " + (long)calculateRTO(startTimeRTO, endTimeRTO));
                      //CCheck to see if you get an ack for what you want 
                      if(numKeys != 0){
                          server.receive(ackPack);
                          //System.out.println(ack.toString());
                          endTimeRTO = System.nanoTime();
                          rto = calculateRTO(startTimeRTO, endTimeRTO);
                          //System.out.println("RTO " + rto);
                          ackPack.flip();
                          opcode = ackPack.getShort();
                          blockNum = ackPack.getShort();
  
                          //make sure the ack is w/in the window size
                          if(blockNum > base+windowSize){
                              System.out.println("here");
                              continue;
                          }

                          //Shift the window 
                          lar = blockNum;
                          //System.out.println("lar " + lar);
                          base = lar + 1;
                      }
                      //shift the window by one if it does 
                      else{
                          //System.out.println("Shifting window by 1");
                          //Rewind the ack that you need to resend
                          buffers[lar+1].rewind();
  
                          //Resend the buffer
                          System.out.println("resending buffer" + (lar+1));
                          server.send(buffers[lar+1], remoteAdd);
                      }
                  }
                }
                //*************************SLIDING WINDOW PROTOCOL STOP********************************//

                endTime = System.nanoTime();
                elapsedTime = endTime - startTime;
                System.out.println((double)(elapsedTime / 1000000) + " milliseconds");
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
      }

    }

    private static short initial_packetR(ByteBuffer packet){

      short request;
      byte zero;
      byte[] fileNameBytes = new byte[10];
      String fileName;
      String mode;
      byte[] modeBytes = new byte[8];
      String wSize;
      byte[] wSizeBytes = new byte[8];
      String drop;
      byte[] dropBytes = new byte[8];

      //flip the buffer 
      packet.flip();

      request = packet.get();
      //Read the file name (until the 0)
      packet.get(fileNameBytes, 0, 10);

      fileName = new String(fileNameBytes);

      System.out.println(fileName);

      //skip the 0
      zero = packet.get();

      //Read the mode (always octet)
      packet.get(modeBytes, 0, 5);

      mode = new String(modeBytes);

      System.out.println(mode);
      zero = packet.get();

      //Read the window size
      packet.get(wSizeBytes, 0, 5);
      wSize = new String(wSizeBytes);
      System.out.println(wSize);
      windowSize = (short)packet.get();
      System.out.println(windowSize);

      //Get the 0
      zero = packet.get();

      //Read if they want to drop packets
      packet.get(dropBytes, 0, 4);
      drop = new String(dropBytes);
      toDrop = (short)packet.get();
      System.out.println(toDrop);


      //Return the request 
      return request;
      
    }

    private static long sidPacket(ByteBuffer sidBuffer){
        long sid;
        long xorNum;

        System.out.println(sidBuffer.toString());
        sidBuffer.flip();

        sid = sidBuffer.getInt();

        xorNum = sidBuffer.getLong();

        return xorNum;
    }

    private static void constructBuffers(byte[] fileBytes) throws IOException{

        //Constants
        short OPCODE = 3;
        int packetSize = 512;

        //Variables
        short blockNum = 0;
        int byteCounter = 0;
        int iter = fileBytes.length / packetSize;
        byte[] someBytes;
        ByteArrayOutputStream outputStream;

        buffers = new ByteBuffer[iter+1];
        ByteBuffer header = ByteBuffer.allocate(4);
        byte[] headerBytes = new byte[4];

        //loop through the file byte array 
        for(int i = 0; i < iter; i++){
            //allocate the correct amount of bytes (512 + 4)
            buffers[i] = ByteBuffer.allocate(packetSize+4);
            outputStream = new ByteArrayOutputStream(512+4);
            header.clear();
            header.putShort(OPCODE);
            header.putShort(blockNum);
            headerBytes = header.array();
            outputStream.write(headerBytes);

            for(int j = 0; j < packetSize; j++){
                fileBytes[byteCounter+j] = encode(fileBytes[byteCounter+j]);
                outputStream.write(fileBytes[byteCounter+j]);
            }

            someBytes = outputStream.toByteArray(); 
            buffers[i] = ByteBuffer.wrap(someBytes);
            //System.out.println("Buffer " + i + " " + buffers[i].toString());

            byteCounter += 512;

            blockNum++;
            
        }
        //Allocate for the last bit of data
        buffers[iter] = ByteBuffer.allocate(fileBytes.length - byteCounter + 4);

        //put the opcode in the buffer
        outputStream = new ByteArrayOutputStream(fileBytes.length-byteCounter+4);
        header.clear();
        header.putShort(OPCODE);
        header.putShort(blockNum);
        headerBytes = header.array();
        outputStream.write(headerBytes);
        for(int j = 0; j < (fileBytes.length-byteCounter); j++){
            fileBytes[byteCounter+j] = encode(fileBytes[byteCounter+j]);
            outputStream.write(fileBytes[byteCounter+j]);
        }

        someBytes = outputStream.toByteArray();

        buffers[iter] = ByteBuffer.wrap(someBytes);
    }

    static byte encode(byte input){
   
      input ^= key; 
      input ^= key; 
      input ^= key;  
      if(counter == 64){
          //System.out.println("encoding with - " + key);
          counter = 0;
          key = xorShift(key);
      }
      counter++;
      return input;   
  }

    static byte[] decode(byte[] input){
    
      System.out.println("decoding with - " + key);
      System.out.println("input length - " + input.length);
      for(int i = 0; i < input.length; i++){
          input[i] ^= key; 
          input[i] ^= key; 
          input[i] ^= key;
          if(counter == 64){
            counter = 0;
            key = xorShift(key);
        }
        counter++;
      }  
      return input;   
  }
   
    static long xorShift(long r) {
        //System.out.println("Shifting key");
        r ^= r << 13; r ^= r >>> 7; r ^= r << 17; 
        //System.out.println("New key --- " + r);
        shiftCount++;
        //System.out.println(shiftCount);
        return r;
    }

    //Calculates RTO in milliseconds
    static double calculateRTO(long startTime, long endTime){

      //Convert to ms


      //Base case
      //System.out.println("RTT " + ((endTime - startTime)/1000000));
      if(srtt == 0L){
          //System.out.println("HERE");
          srtt = 10;
      }
      //SRTT = (startTime - endTime) * alpha + SRTT * (1 - alpha)
      else{
          srtt = ((double)((endTime - startTime)/1000000) * alpha + srtt * (1 - alpha));
          //System.out.println("srtt " + srtt);
      }

      
      //Base case
      if(rttVar == 0L){
          //System.out.println("hi");
          rttVar = srtt / 2;
      }//rttVar = abs((startTime - endTime) - SRTT) * beta + RTTVAR * (1 - beta)
      else{
          rttVar = Math.abs((endTime - startTime)/1000000 - srtt) * beta + rttVar * (1 - beta);
          //System.out.println("rttVar " + rttVar);
      }

      //RTO = SRTT + max(K * RTTVAR, G)
      rto = srtt + Math.max((k * rttVar), 17);

      return rto;
    }
  }
