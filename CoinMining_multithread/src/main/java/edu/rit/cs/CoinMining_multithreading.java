package edu.rit.cs;

import org.apache.commons.codec.digest.DigestUtils;

public class CoinMining_multithreading extends Thread {
    static final int num_processors = 2;
    static long nonce;
    static String blockHash;
    static String targetHash;
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


    public static long pow(long start, long end) {

        long temp_nonce=0;
        String tmp_hash="undefined";
        for(temp_nonce= start; temp_nonce<=end; temp_nonce++) {
            if(Thread.interrupted()){
                return -1;
            }
            tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(blockHash+String.valueOf(temp_nonce)));
            if(targetHash.compareTo(tmp_hash)>0) {
                System.out.println("Hash: "+tmp_hash);
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
            System.out.println("Time taken :"+(System.currentTimeMillis()-start_time));
                        for (int i =0; i<num_processors;i++){
                if(i != index){
                    threads[i].interrupt();
                }
            }
            return;
        }
    }

    public static void main(String[] args) {
        blockHash = DigestUtils.sha256Hex(args[0]);
        targetHash = args[1];
        System.out.println("BlockHash: " + blockHash);
        System.out.println("TargetHash: " + targetHash);
        System.out.println("Performing Proof-of-Work...wait...");
        start_time = System.currentTimeMillis();
        long start_nonce = Integer.MIN_VALUE;
        long block = (Long.parseLong("4294967295")/Long.parseLong(num_processors+""));

        for (int index = 0;index<num_processors;index++){
            threads[index] = new CoinMining_multithreading(start_nonce,start_nonce+block,index);
            threads[index].start();
            start_nonce+=(block+1);
        }
    }
}