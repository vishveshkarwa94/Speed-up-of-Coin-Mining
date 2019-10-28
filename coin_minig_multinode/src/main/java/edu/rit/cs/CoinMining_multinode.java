package edu.rit.cs;
import mpi.*;

import java.nio.LongBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CoinMining_multinode extends Thread{
    static final int num_processors = Runtime.getRuntime().availableProcessors()/2;
    static String blockHash = SHA256("CSCI-654 Foundations of Parallel Computing");
    static String targetHash = "0000092a6893b712892a41e8438e3ff2242a68747105de0395826f60b38d88dc";
    static CoinMining_multinode[] threads = new CoinMining_multinode[num_processors];
    long local_start;
    long local_end;
    int index;

    CoinMining_multinode(long start, long end, int index){
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

    public static long pow(long start, long end){

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
            send(local_nonce);
        }
    }

    public void send(Long nonce) throws MPIException{
        LongBuffer send_buffer = MPI.newLongBuffer(1);
        send_buffer.put(0,local_nonce);
        MPI.COMM_WORLD.send(send_buffer,1,MPI.LONG,0,0);
    }


    public static void main(String[] args) throws MPIException{


        MPI.Init(args);
        int size = MPI.COMM_WORLD.getSize();
        int rank = MPI.COMM_WORLD.getRank();

        LongBuffer send_buffer;
        LongBuffer receive_buffer = MPI.newLongBuffer(1);

        if(rank == 0){
            long start_nonce = Integer.MIN_VALUE;
            long block = (Long.parseLong("4294967295")/(size-1));
            for(int count = 0;count<size;count++){
                send_buffer = MPI.newLongBuffer(2);
                send_buffer.put(0,start_nonce);
                send_buffer.put(1,(start_nonce+block));
                start_nonce+=(block+1);
                MPI.COMM_WORLD.send(send_buffer,2,MPI.LONG,count,0);
            }

            MPI.COMM_WORLD.recv(receive_buffer, 1, MPI.LONG, MPI.ANY_SOURCE,0);
            System.out.println("Found nonce :"+receive_buffer.get(0));
            System.exit(0);
        }
        else {
            send_buffer = MPI.newLongBuffer(2);
            MPI.COMM_WORLD.recv(send_buffer,2,MPI.LONG,0,0);
            long block = (send_buffer.get(1)-send_buffer.get(0))/num_processors;
            long start_nonce = send_buffer.get(0);
            for (int index = 0;index<num_processors;index++){
                threads[index] = new CoinMining_multinode(start_nonce,start_nonce+block,index);
                threads[index].start();
                start_nonce+=(block+1);
            }
        }

    }

}
