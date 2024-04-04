import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Client4 {

    private static final short PACKET_SIZE = 512;
    //Constants
    static int INIT_PACKET = 12;
    static int counter = 0;
    static long number;
    static String filename;
    static ByteBuffer buffers[];
    static ByteBuffer acks[];
    static long key;
    static short opcode;  
    static int shiftCount = 0;  
    static short drop;
    static int windowSize = 50;


    static double rttVar;
    static double srtt;
    static double rto;
    static double alpha = .125;
    static double beta = .25;
    static int k = 4;

    public static void main(String[] args) throws IOException{

        //Constants
        short RRQ = 1;
        short WRQ = 2;
        Scanner scan = new Scanner(System.in);

        //Variables
        long serverSID;
        int lastFrameSent = 0;
        short lastAckReceived;
        //int windowSize = 5;
        short blockNum;
        short lar;
        short laf;
        short lfr;
        byte[] imgData = new byte[512];
        byte[] remData;
        byte[] img;
        byte[] acknowledge;
        short[] ackArr = new short[windowSize];
        int expectedNum = 0;

        //Total time
        long startTime;
        long endTime;
        long elapsedTime;
        long startTimeRTO = 0;
        long endTimeRTO = 0;

        //Make the new datagram channel
        DatagramChannel client = null;

        short toDrop = 16;
        boolean hasDropped = false;

        //Open the client
        Selector selector = Selector.open();
        client = DatagramChannel.open();

        //Bind the client
        client.bind(null);
        client.socket().setSoTimeout(10);

        //Open the socket 
        InetSocketAddress serverAddress = new InetSocketAddress("localhost",
        8989);

        //Build first packet to send to server containing opcode
        ByteBuffer buffer;
        ByteBuffer sidBuffer;
        ByteBuffer serverKey = ByteBuffer.allocate(8);
        ByteBuffer ack = ByteBuffer.allocate(4);
        ByteBuffer sidPack = ByteBuffer.allocate(12);
        ByteBuffer clientXor = ByteBuffer.allocate(Long.BYTES);
        ByteBuffer initialPacket = ByteBuffer.allocate("catImg.jpg".getBytes().length+"octet".getBytes().length+4+1);
        ByteBuffer image = ByteBuffer.allocate(5000000);
        ByteBuffer dataPacket = ByteBuffer.allocate(516 );
        ByteBuffer ackPack = ByteBuffer.allocate(4);
        Path path = Paths.get( "./leavesClient.jpg");

        int numKeys;

        sidBuffer = sidPacket();
        buffer = initial_packetW();

        //Send the SID packet
        client.send(sidBuffer, serverAddress);

        //Receive the server packet
        sidBuffer.clear();
        client.receive(serverKey);
        System.out.println(serverKey.toString());
        serverKey.flip();
        serverSID = serverKey.getLong();

        //Get the key
        System.out.println(serverSID);
        System.out.println(number);
        key = serverSID ^ number;

        System.out.println(key);

        //Send the initial packet 
        client.send(buffer, serverAddress);

        System.out.println(opcode);
        if(opcode == 2){
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            //Get the bytes from the file
            try{
                byte[] bytes = Files.readAllBytes(Paths.get("./leaves.jpg"));

                //Construct buffers2
                System.out.println(bytes.length);
                constructBuffers(bytes);
                //writeFile(buffers);
                int base = 0;  
                int counter = 0; 
                lar = 0;
                //Loop through buffer array to send them and receive ACKs\
                //***********************SLIDING WINDOW PROTOCOL START*********************************//\
                //START TIMER
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
                        client.send(buffers[i], serverAddress);
                        lastFrameSent = i;
                        //System.out.println("LFS " + lastFrameSent);
                    }
                    ack.clear();

                    selector.selectedKeys().clear();
                    //System.out.println(rto);
                    numKeys = selector.select(100);
                    //System.out.println("Keys " + numKeys);
                    //System.out.println("RTO " + (long)calculateRTO(startTimeRTO, endTimeRTO));
                    //CCheck to see if you get an ack for what you want 
                    if(numKeys != 0){
                        client.receive(ack);
                        //System.out.println(ack.toString());
                        endTimeRTO = System.nanoTime();
                        ack.flip();
                        opcode = ack.getShort();
                        blockNum = ack.getShort();

                        if(blockNum <= 15){
                            calculateRTO(startTimeRTO, endTimeRTO);
                        }
                        //make sure the ack is w/in the window size
                        if(blockNum > base+windowSize){
                            //System.out.println("here");
                            continue;
                        }
                        //Shift the window 
                        lar = blockNum;
                        System.out.println("lar " + lar);
                        base = lar + 1;
                    }
                    //shift the window by one if it does 
                    else{
                        //System.out.println("Shifting window by 1");
                        //Rewind the ack that you need to resend
                        buffers[lar+1].rewind();

                        //Resend the buffer
                        System.out.println("resending buffer" + (lar+1));
                        client.send(buffers[lar+1], serverAddress);
                    }
                }
                //*************************SLIDING WINDOW PROTOCOL END************************************//
                //Grab th rest of the ACKS
                //STOP TIMER and print time
                endTime = System.nanoTime();
                elapsedTime = endTime - startTime;
                System.out.println(elapsedTime / 1000000 + " milliseconds");
                client.close();

                //TODO - Loop through ACKS to make sure you have them all correctly
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
        } 
        else if(opcode == 1){
            System.out.println("read request");
            laf = (short)(windowSize + 0);
            lfr = 0;
            //hasDropped = true;
            //Start reading from the server
            for(;;){
                //System.out.println("here");
                dataPacket.clear();
                client.receive(dataPacket);
                //System.out.println(dataPacket.toString());
                dataPacket.flip();
    
                //System.out.println(dataPacket.toString());
                //System.out.println(dataPacket.toString());
                //Check to see if the size is <516
                if(dataPacket.limit() < 516){
    
                    System.out.println("last packet");
                    ackPack.clear();
                    //Grab all the data
                    opcode = dataPacket.getShort();
                    blockNum = dataPacket.getShort();
                    //System.out.println(opcode);
                    //System.out.println(blockNum);
                    
                    remData = new byte[dataPacket.remaining()];
                    dataPacket.get(remData);
    
                    if(expectedNum == blockNum){
                        //Send last ack
                        ackPack.putShort((short)4);
                        ackPack.putShort(blockNum);
                        
                        client.send(ackPack, serverAddress);
                    }
    
                    //close connection 
                    client.close();
    
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
                      //System.out.println();
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
                        //System.out.println("sending ack for " + blockNum);
                        //Send the ack 
                        ackPack.clear();
                        ackPack.putShort((short)4);
                        ackPack.putShort(blockNum);
                        acknowledge = ackPack.array();
                        ackPack = ByteBuffer.wrap(acknowledge);
                        //change largest acceptable frame
                        //
                        //System.out.println("lfr " + lfr);
                        //System.out.println("laf " + laf);
                        lfr = blockNum;
                        laf = (short)(lfr + windowSize);
                        //System.out.println(ackPack.toString());
                        client.send(ackPack, serverAddress);
                          
                      }
                      //Else the seqNumToAck is not what we expected, so we buffer until the one we get is what we expect
                      else{
                        //Buffer the ack in an array 
                        System.out.println("storing ack at " + (laf-blockNum));
                        ackArr[laf - blockNum] = blockNum;
                        
                      }
                    }
                }
            }
    
            //Write out the stuff to an image file 
            try {
                System.out.println(image.toString());
                image.flip();
                img = new byte[image.remaining()];
                image.get(img);
                img = decode(img);
                Files.write(path, img, StandardOpenOption.CREATE);
                System.out.println("writing");
            
            } catch (Exception e) {
            e.printStackTrace();
            }
        
    }
}

    private static ByteBuffer initial_packetW() throws IOException{
        //Constants
        String OPCODE = "octet";
        String option = "wSize";
        String DROP = "drop";
        String SWS = "SWS";
        short RRQ = 1;
        short WRQ = 2;
        //short windowSize = 4;
        Scanner scan = new Scanner(System.in);

        //Variables
        ByteBuffer initial;
        byte[] requestArr;

        //Put in rrq or wrq
        System.out.print("Read(1) or Write(2)? - ");
        opcode = scan.nextShort();

        //
        System.out.print("\nDrop packets N(0) Y(1)? - ");
        drop = scan.nextShort();

        //Get the file name 
        System.out.print("\nInput file name - ");
        filename = scan.next();

        ByteArrayOutputStream initPacket = new ByteArrayOutputStream(filename.getBytes().length + OPCODE.getBytes().length + SWS.getBytes().length + 6);

        //Put opcode in buffer
        if(opcode == RRQ){
            initPacket.write(RRQ);
            System.out.println("Read request");
        }
        else if (opcode == WRQ){
            initPacket.write(WRQ);
            System.out.println("Write request");
        }

        //Put the file name in the byte buffer
        initPacket.write(filename.getBytes());

        //put a zero in the byte buffer 
        initPacket.write((byte)0);

        //Put the opcode in the buffer
        initPacket.write(OPCODE.getBytes());

        //put another zero in the buffer 
        initPacket.write((byte)0);

        //Send window size
        initPacket.write(option.getBytes());
        initPacket.write(windowSize);

        //Put a zero
        initPacket.write((byte)0);

        //Send whether or not to drop packets
        initPacket.write(DROP.getBytes());
        if(drop == 0){
            initPacket.write((byte)0);
        }
        else if(drop == 1){
            //Send a number between 1-60 to be dropped
            initPacket.write((byte)56);
        }
        requestArr = initPacket.toByteArray();
        initial = ByteBuffer.wrap(requestArr);

        return initial;
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
        acks = new ByteBuffer[iter+1];
        ByteBuffer header = ByteBuffer.allocate(4);
        byte[] headerBytes = new byte[4];

        //loop through the file byte array 
        for(int i = 0; i < iter; i++){
            //allocate the correct amount of bytes (512 + 4)
            buffers[i] = ByteBuffer.allocate(packetSize+4);
            acks[i] = ByteBuffer.allocate(4);
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

    private static ByteBuffer sidPacket(){
        Random rand = new Random();
        ByteBuffer packet = ByteBuffer.allocate(12);

        int sid;
        byte[] test = new byte[8];

        sid = 101010;
        number = rand.nextLong();

        packet.putInt(sid);
        packet.putLong(number);

        test = packet.array();

        packet = ByteBuffer.wrap(test);

        return packet;

    }

    static byte encode(byte input){
   
        //System.out.println("encoding with - " + key);
        input ^= key; 
        input ^= key; 
        input ^= key;  
        if(counter == 64){
            counter = 0;
            key = xorShift(key);
        }
        counter++;
        return input;   
    }

    static byte[] decode(byte[] input){
   
        for(int i = 0; i < input.length; i++){
            input[i] ^= key; 
            input[i] ^= key; 
            input[i] ^= key;
            if(counter == 64){
                //System.out.println("decoding with - " + key);
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
        //System.out.println("RTT " + (endTime - startTime));
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
            System.out.println("hi");
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

