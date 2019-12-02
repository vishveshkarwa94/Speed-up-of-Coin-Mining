package edu.rit.cs;

import mpi.MPI;
import mpi.MPIException;
import mpi.Status;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.nio.LongBuffer;

public class Multinode {

    static String tmpBlockHash;
    static String tmpTargetHash;
    static boolean thread_flag;

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

    public static long master(int size) throws MPIException {

        long start_nonce = Integer.MIN_VALUE;
        long block = (Long.parseLong("4294967295")/(size-1));
        for(int count = 0;count<size;count++){
            LongBuffer send_buffer = MPI.newLongBuffer(2);
            send_buffer.put(0,start_nonce);
            send_buffer.put(1,(start_nonce+block));
            start_nonce+=(block+1);
            MPI.COMM_WORLD.send(send_buffer,2,MPI.LONG,count,0);
        }
        LongBuffer receive_buffer = MPI.newLongBuffer(1);
        MPI.COMM_WORLD.recv(receive_buffer, 1, MPI.LONG, MPI.ANY_SOURCE,2);
        return receive_buffer.get(0);

    }

    public static void slave(long start, long end, int rank) throws MPIException {

        String tmp_hash;
        thread_flag = true;
        // omp parallel for
        for(long temp_nonce= start; temp_nonce<=end; temp_nonce++) {
            if(thread_flag){
                Status flag =  MPI.COMM_WORLD.iProbe(0,1);
                if(flag != null){
                    MPI.COMM_WORLD.recv(MPI.newIntBuffer(1),1,MPI.INT,0,1);
                }
                tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(tmpBlockHash+ temp_nonce));
                if(tmpTargetHash.compareTo(tmp_hash)>0) {
                    thread_flag = false;
                    MPI.COMM_WORLD.send(MPI.newLongBuffer(1).put(0,temp_nonce),1,MPI.LONG,0,2);
                }
            }
            else {
                temp_nonce = end;
            }
        }
    }

    public static void main(String[] args) throws MPIException {

        MPI.Init(args);
        int size = MPI.COMM_WORLD.getSize();
        int rank = MPI.COMM_WORLD.getRank();

        int num_blocks = 10;
        double avgBlockGenerationTimeInSec = 30.0;


        tmpBlockHash = DigestUtils.sha256Hex(args[0]);
        tmpTargetHash = args[1];

        int currentBlockID = 1;

        if(rank == 0){

            while (currentBlockID<=num_blocks)
            {
                MyTimer myTimer = new MyTimer("CurrentBlockID:"+currentBlockID);
                myTimer.start_timer();
                System.out.println("Block hash : "+tmpBlockHash);
                System.out.println("Target Hash :"+tmpTargetHash);
                Long nonce = master(size);
                System.out.println("Found nonce : "+nonce);
                myTimer.stop_timer();
                myTimer.print_elapsed_time();


                for(int index = 1;index<size;index++){
                    MPI.COMM_WORLD.iSend(MPI.newIntBuffer(1).put(0,1),1,MPI.INT,index,1);
                }

                tmpBlockHash = DigestUtils.sha256Hex(tmpBlockHash+"|"+nonce);
                if(myTimer.get_elapsed_time_in_sec()<avgBlockGenerationTimeInSec)
                    tmpTargetHash = HexValueDivideBy(tmpTargetHash, 2);
                else
                    tmpTargetHash = HexValueMultipleBy(tmpTargetHash, 2);

                currentBlockID++;
                System.out.println();
                System.out.println();
            }
        }
        else{

            while (true){
                LongBuffer send_buffer = MPI.newLongBuffer(2);
                MPI.COMM_WORLD.recv(send_buffer,2,MPI.LONG,0,0);
                long start_nonce = send_buffer.get(0);
                long end_nonce = send_buffer.get(1);
                slave(start_nonce,end_nonce,rank);
            }

        }
        MPI.COMM_WORLD.abort(0);

    }

}
