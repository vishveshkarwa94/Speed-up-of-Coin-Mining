package edu.rit.cs;

import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;


public class CoinMining_multithreading extends Thread {
    static final int num_processors = 2;
    static long nonce;
    static String tmpBlockHash;
    static String tmpTargetHash;
    static CoinMining_multithreading[] threads = new CoinMining_multithreading[num_processors];
    static MyTimer myTimer;
    static ExecutorService es;
    long local_start;
    long local_end;
    int index;
    static CountDownLatch latch;

    CoinMining_multithreading(long start, long end, int index){
        local_start = start;
        local_end = end;
        this.index = index;
    }


    public static long pow(long start, long end) {

        long temp_nonce=0;
        String tmp_hash="undefined";
        for(temp_nonce= start; temp_nonce<=end; temp_nonce++) {
            if(Thread.interrupted()){
                return -1;
            }
            tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(tmpBlockHash+String.valueOf(temp_nonce)));
            if(tmpTargetHash.compareTo(tmp_hash)>0) {
                return temp_nonce;
            }
        }
        return -1;
    }

    @Override
    public void run() {
        long local_nonce = pow(local_start,local_end);
        if (local_nonce!=-1){
            nonce = local_nonce;
            System.out.println("Found nonce :"+nonce);
            for (int i =0; i<num_processors;i++){
                if(i != index){
                    threads[i].interrupt();
                }
            }
            myTimer.stop_timer();
            myTimer.print_elapsed_time();
            System.out.println();
        }
        latch.countDown();
        return;
    }

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
            latch = new CountDownLatch(num_processors);

            long start_nonce = Integer.MIN_VALUE;
            long block = (Long.parseLong("4294967295") / Long.parseLong(num_processors + ""));

            for (int index = 0; index < num_processors; index++) {
                threads[index] = new CoinMining_multithreading(start_nonce, start_nonce + block, index);
                threads[index].start();
                start_nonce += (block + 1);
            }

            latch.await();

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