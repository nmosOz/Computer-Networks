import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.UnsupportedCharsetException;
import java.security.SecureRandom;
import java.util.Random;

public class Test {
    static long key = 0;
    public static void main(String[] args) {
        
        try{
        String message = "Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello " + 
        "Hello Hello Hello Hello hihi Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello hihi " +
        "Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello Hello ";

        byte[] bytes = message.getBytes("UTF-8");
        byte[] bytes2 = message.getBytes("UTF-8");

        System.out.println(bytes);
        System.out.println(bytes2);
        message = new String(bytes, "UTF-8");

        System.out.println(message);
        }
        catch(UnsupportedEncodingException ex){
           ex.printStackTrace();

        }


    }

    static void incKey(){
        key = key + 1;
    }
}
