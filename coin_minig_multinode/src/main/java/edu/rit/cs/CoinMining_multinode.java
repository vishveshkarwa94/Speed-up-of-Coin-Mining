/*
Old single nonce finding code, have issues. New files Multinode.java
 */



//package edu.rit.cs;
//
//import mpi.*;
//import org.apache.commons.codec.digest.DigestUtils;
//
//import java.math.BigInteger;
//import java.nio.IntBuffer;
//import java.nio.LongBuffer;
//
//public class CoinMining_multinode extends Thread{
//    static final int num_processors = Runtime.getRuntime().availableProcessors()/2;
//    static String tmpBlockHash;
//    static String tmpTargetHash;
//    static CoinMining_multinode[] threads = new CoinMining_multinode[num_processors];
//    long local_start;
//    long local_end;
//    int index;
//
//    CoinMining_multinode(long start, long end, int index){
//        local_start = start;
//        local_end = end;
//        this.index = index;
//    }
//
//    private static String bytesToHex(byte[] hash) {
//        StringBuffer hexString = new StringBuffer();
//        for (int i = 0; i < hash.length; i++) {
//            String hex = Integer.toHexString(0xff & hash[i]);
//            if(hex.length() == 1) hexString.append('0');
//            hexString.append(hex);
//        }
//        return hexString.toString();
//    }
//
//    public static String HexValueDivideBy(String hexValue, int val) {
//        BigInteger tmp = new BigInteger(hexValue,16);
//        tmp = tmp.divide(BigInteger.valueOf(val));
//        String newHex = bytesToHex(tmp.toByteArray());
//        while (newHex.length() < hexValue.length()) {
//            newHex = '0' + newHex;
//        }
//        return newHex;
//    }
//
//    public static String HexValueMultipleBy(String hexValue, int val) {
//        BigInteger tmp = new BigInteger(hexValue,16);
//        tmp = tmp.multiply(BigInteger.valueOf(val));
//        String newHex = bytesToHex(tmp.toByteArray());
//        while (newHex.length() < hexValue.length()) {
//            newHex = '0' + newHex;
//        }
//        return newHex;
//    }
//
//    public static long pow(long start, long end) throws MPIException {
//
//        Status flag;
//        long temp_nonce=0;
//        String tmp_hash="undefined";
//        for(temp_nonce= start; temp_nonce<=end; temp_nonce++) {
//            if(Thread.interrupted()){
//                return -1;
//            }
//            flag =  MPI.COMM_WORLD.iProbe(0,1);
//            if(flag != null){
//                System.out.println("Interrupted by source.");
//                return -1;
//            }
//            tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(tmpBlockHash+String.valueOf(temp_nonce)));
//            if(tmpTargetHash.compareTo(tmp_hash)>0) {
//                 System.out.println("nonce found:"+temp_nonce);
//                 return temp_nonce;
//            }
//        }
//        return -1;
//    }
//
//    @Override
//    public void run() {
//        try {
//            long local_nonce = pow(local_start,local_end);
//            if (local_nonce!=-1){
//                for (int i =0; i<num_processors;i++){
//                    if(i != index){
//                        threads[i].interrupt();
//                    }
//                }
//                LongBuffer send_buffer = MPI.newLongBuffer(1);
//                send_buffer.put(0,local_nonce);
//                MPI.COMM_WORLD.send(send_buffer,1,MPI.LONG,0,0);
//            }
//        }
//        catch (MPIException e){
//                return;
//            }
//
//        }
//
//    public static void sync() throws MPIException {
//        MPI.COMM_WORLD.barrier();
//    }
//
//    public static long master(int size, LongBuffer receive_buffer) throws MPIException {
//        long start_nonce = Integer.MIN_VALUE;
//        long block = (Long.parseLong("4294967295")/(size-1));
//        for(int count = 0;count<size;count++){
//            LongBuffer send_buffer = MPI.newLongBuffer(2);
//            send_buffer.put(0,start_nonce);
//            send_buffer.put(1,(start_nonce+block));
//            start_nonce+=(block+1);
//            MPI.COMM_WORLD.send(send_buffer,2,MPI.LONG,count,0);
//        }
//
//        MPI.COMM_WORLD.recv(receive_buffer, 1, MPI.LONG, MPI.ANY_SOURCE,0);
//        return receive_buffer.get(0);
//
//    }
//
//    public static void slave(long start_nonce, long block) throws MPIException {
//
//        for (int index = 0;index<num_processors;index++){
//            threads[index] = new CoinMining_multinode(start_nonce,start_nonce+block,index);
//            threads[index].start();
//            start_nonce+=(block+1);
//        }
//
//    }
//
//
//
//    public static void main(String[] args) throws MPIException, InterruptedException {
//
//        int num_blocks = 10;
//        double avgBlockGenerationTimeInSec = 30.0;
//
//        tmpBlockHash = DigestUtils.sha256Hex(args[0]);
//        tmpTargetHash = args[1];
//
//
//        MPI.Init(args);
//        int size = MPI.COMM_WORLD.getSize();
//        int rank = MPI.COMM_WORLD.getRank();
//
//        LongBuffer receive_buffer = MPI.newLongBuffer(1);
//        int currentBlockID = 1;
//
//        if(rank == 0){
//
//            MyTimer myTimer = new MyTimer("CurrentBlockID:"+currentBlockID);
//
//            while (currentBlockID<=num_blocks)
//            {
//
//            System.out.println("Block hash : "+tmpBlockHash);
//            System.out.println("Target Hash :"+tmpTargetHash);
//
//            Long nonce = master(size,receive_buffer);
//
//            System.out.println("Found nonce : "+nonce);
//            myTimer.start_timer();
//            myTimer.print_elapsed_time();
//
//            for(int index = 1;index<size;index++){
//                System.out.println("Interuppting slaves");
//                IntBuffer s = MPI.newIntBuffer(1);
//                s.put(0,1);
//                MPI.COMM_WORLD.iSend(s,1,MPI.INT,index,1);
//            }
//
//            System.out.println("updating hashesh");
//            tmpBlockHash = DigestUtils.sha256Hex(tmpBlockHash+"|"+nonce);
//            if(myTimer.get_elapsed_time_in_sec()<avgBlockGenerationTimeInSec)
//                tmpTargetHash = HexValueDivideBy(tmpTargetHash, 2);
//            else
//                tmpTargetHash = HexValueMultipleBy(tmpTargetHash, 2);
//
//            currentBlockID++;
//            sync();
//            }
//        }
//        else {
//            while (true){
//                 LongBuffer send_buffer = MPI.newLongBuffer(2);
//                 MPI.COMM_WORLD.recv(send_buffer,2,MPI.LONG,0,0);
//                 System.out.println(rank+" Currrent blck :"+currentBlockID);
//                 long block = (send_buffer.get(1)-send_buffer.get(0))/num_processors;
//                 long start_nonce = send_buffer.get(0);
//                 slave(start_nonce,block);
//                 sync();
//            }
//        }
//        MPI.Finalize();
//    }
//
//}
