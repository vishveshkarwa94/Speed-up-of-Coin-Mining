package edu.rit.cs;

import org.apache.commons.codec.digest.DigestUtils;
import java.math.BigInteger;



public class CoinMining_multithread_omp4j extends Thread {

    static long nonce;
    static String tmpBlockHash;
    static String tmpTargetHash;
    static MyTimer myTimer;
    static boolean flag;

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String HexValueDivideBy(String hexValue, int val) {
        BigInteger tmp = new BigInteger(hexValue,16);
        tmp = tmp.divide(BigInteger.valueOf(val));
        String newHex = bytesToHex(tmp.toByteArray());
        while (newHex.length() < hexValue.length()) {
            newHex = '0' + newHex;
        }
        return newHex;
    }

    public static String HexValueMultipleBy(String hexValue, int val) {
        BigInteger tmp = new BigInteger(hexValue,16);
        tmp = tmp.multiply(BigInteger.valueOf(val));
        String newHex = bytesToHex(tmp.toByteArray());
        while (newHex.length() < hexValue.length()) {
            newHex = '0' + newHex;
        }
        return newHex;
    }

    public static void pow() {

        long temp_nonce;
        String tmp_hash;
        flag = true;
        // omp parallel for
        for(temp_nonce= Integer.MIN_VALUE; temp_nonce<=Integer.MAX_VALUE; temp_nonce++) {
            if(flag){
                tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(tmpBlockHash+temp_nonce));
                if(tmpTargetHash.compareTo(tmp_hash)>0) {
                    System.out.println("Nonce Found: "+temp_nonce);
                    myTimer.stop_timer();
                    myTimer.print_elapsed_time();
                    flag = false;
                }
            }
            else {
                temp_nonce = Integer.MAX_VALUE;
            }
        }

    }

    public static void main(String[] args) throws InterruptedException {

        int num_blocks = 10;
        double avgBlockGenerationTimeInSec = 30.0;


        String blockHash = DigestUtils.sha256Hex(args[0]);
        String targetHash = args[1];

        int currentBlockID = 1;

        tmpBlockHash = blockHash;
        tmpTargetHash = targetHash;


        System.out.println("BlockHash: " + blockHash);
        System.out.println("TargetHash: " + targetHash);

        while (currentBlockID<= num_blocks) {

            myTimer = new MyTimer("CurrentBlockID:"+currentBlockID);
            myTimer.start_timer();

            pow();

            tmpBlockHash = DigestUtils.sha256Hex(tmpBlockHash+"|"+nonce);
            if(myTimer.get_elapsed_time_in_sec()<avgBlockGenerationTimeInSec)
                tmpTargetHash = HexValueDivideBy(tmpTargetHash, 2);
            else
                tmpTargetHash = HexValueMultipleBy(tmpTargetHash, 2);

            if(currentBlockID!=num_blocks){
                System.out.println("New Block Hash:  " + tmpBlockHash);
                System.out.println("New Target Hash: " + tmpTargetHash);
            }

            currentBlockID++;
        }
    }
}