package edu.rit.cs;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


class CoinMining_multithreading extends Thread {
    static final int num_processors = Runtime.getRuntime().availableProcessors()/2;
    static long nonce;
    static String blockHash = SHA256("CSCI-654 Foundations of Parallel Computing");
    static String targetHash = "0000092a6893b712892a41e8438e3ff2242a68747105de0395826f60b38d88dc";
    static CoinMining_multithreading[] threads = new CoinMining_multithreading[num_processors];
    static long start_time;
    long local_start;
    long local_end;
    int index;

    CoinMining_multithreading(long start, long end, int index){
        local_start = start;
        local_end = end;
        this.index = index;
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

    public static String SHA256(String inputString) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return bytesToHex(sha256.digest(inputString.getBytes(StandardCharsets.UTF_8)));
        }catch (NoSuchAlgorithmException ex) {
            System.err.println(ex.toString());
            return null;
        }
    }

    public static long pow(long start, long end) {

        long temp_nonce=0;
        String tmp_hash="undefined";
        for(temp_nonce= start; temp_nonce<=end; temp_nonce++) {
            if(Thread.interrupted()){
                return -1;
            }
            tmp_hash = SHA256(SHA256(blockHash+String.valueOf(temp_nonce)));
            if(targetHash.compareTo(tmp_hash)>0) {
                return temp_nonce;
            }
        }
        return -1;
    }

    @Override
    public void run() {
        long local_nonce = pow(local_start,local_end);
        if (local_nonce!=-1){
            for (int i =0; i<num_processors;i++){
                if(i != index){
                    threads[i].interrupt();
                }
            }
            nonce = local_nonce;
            System.out.println("Found nonce :"+nonce);
            System.out.println("Time taken :"+(System.currentTimeMillis()-start_time));
            return;
        }
    }

    public static void main(String[] args) {
        System.out.println("BlockHash: " + blockHash);
        System.out.println("TargetHash: " + targetHash);
        System.out.println("Performing Proof-of-Work...wait...");

        start_time = System.currentTimeMillis();

        long start_nonce = Long.parseLong(args[0]);
        long block = (Long.parseLong(args[1])/Long.parseLong(num_processors+""));

        for (int index = 0;index<num_processors;index++){
            threads[index] = new CoinMining_multithreading(start_nonce,start_nonce+block,index);
            threads[index].start();
            start_nonce+=(block+1);
        }
    }

}