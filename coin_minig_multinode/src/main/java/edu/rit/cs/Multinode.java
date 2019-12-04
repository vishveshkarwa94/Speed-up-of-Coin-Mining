package edu.rit.cs;

import mpi.MPI;
import mpi.MPIException;
import mpi.Status;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigInteger;
import java.nio.CharBuffer;
import java.nio.LongBuffer;

/*
Class containing Multinode Open MPI implementation of coin mining.
 */

public class Multinode{

    static String tmpBlockHash;
    static String tmpTargetHash;
    static boolean thread_flag;
    static int size;

    // Function to convert bytes to hex.
    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Function to divide hex value of 2.
    public static String HexValueDivideBy(String hexValue, int val) {
        BigInteger tmp = new BigInteger(hexValue,16);
        tmp = tmp.divide(BigInteger.valueOf(val));
        String newHex = bytesToHex(tmp.toByteArray());
        while (newHex.length() < hexValue.length()) {
            newHex = '0' + newHex;
        }
        return newHex;
    }

    // Function to multiply hex value of 2.
    public static String HexValueMultipleBy(String hexValue, int val) {
        BigInteger tmp = new BigInteger(hexValue,16);
        tmp = tmp.multiply(BigInteger.valueOf(val));
        String newHex = bytesToHex(tmp.toByteArray());
        while (newHex.length() < hexValue.length()) {
            newHex = '0' + newHex;
        }
        return newHex;
    }

    //Function called by master node.
    public static long master(int current_block) throws MPIException {
        // Setting start nonce and nonce chunk to be processes by each thread in nodes.
        long start_nonce = Integer.MIN_VALUE;
        long block = (Long.parseLong("4294967295")/(size-1));

        // Function to send nonce chunks and block and target hashes.
        for(int count = 0;count<size;count++){

            // Buffer to send start and end nonce along with current block id.
            LongBuffer send_buffer = MPI.newLongBuffer(3);
            send_buffer.put(0,start_nonce);
            send_buffer.put(1,(start_nonce+block));
            send_buffer.put(2,current_block);
            start_nonce+=(block+1);

            // Sending start and end nonce along with current block id.
            MPI.COMM_WORLD.iSend(send_buffer,3,MPI.LONG,count,0);

            // Sending block hash.
            MPI.COMM_WORLD.iSend(MPI.newCharBuffer(tmpBlockHash.length()).put(tmpBlockHash,0,tmpBlockHash.length()),
                    tmpBlockHash.length(),MPI.CHAR,count,0);

            // Sending target hash
            MPI.COMM_WORLD.iSend(MPI.newCharBuffer(tmpTargetHash.length()).put(tmpTargetHash,0,tmpTargetHash.length()),
                    tmpTargetHash.length(),MPI.CHAR,count,0);
        }

        //Buffer to receive the nonce.
        LongBuffer receive_buffer = MPI.newLongBuffer(1);

        // Blocking receive to receive the nonce from any node.
        MPI.COMM_WORLD.recv(receive_buffer, 1, MPI.LONG, MPI.ANY_SOURCE,current_block);
        return receive_buffer.get(0);

    }

    //Function called by slave nodes.
    public static void slave(long start, long end, String block, String target, int rank,int current_block) throws MPIException {


        String tmp_hash;
        thread_flag = true;
        // For loop performing proof of work.
        // omp parallel for
        for(long temp_nonce= start; temp_nonce<=end; temp_nonce++) {
            // If any thread finds the nonce stop the loop.
            if(thread_flag){

                // Checking if master node has interrupted.
                Status flag =  MPI.COMM_WORLD.iProbe(0,99);
                if(flag != null){
                    MPI.COMM_WORLD.recv(MPI.newIntBuffer(1),1,MPI.INT,0,99);
                    return;
                }

                // Check if nonce is valid.
                tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(block+ temp_nonce));
                if(target.compareTo(tmp_hash)>0) {
                    //If valid nonce send nonce to master node.
                    thread_flag = false;
                    MPI.COMM_WORLD.send(MPI.newLongBuffer(1).put(0,temp_nonce),1,MPI.LONG,0,current_block);
                }
            }
            // If nonce is found by other threads end the loop.
            else {
                temp_nonce = end;
            }
        }
    }


    // Main Function.
    public static void main(String[] args) throws MPIException {

        //MPI initialization.
        MPI.Init(args);

        //Getting the size and rank.
        size = MPI.COMM_WORLD.getSize();
        int rank = MPI.COMM_WORLD.getRank();

        // Parameters for 10 rounds of hashing.
        int num_blocks = 10;
        double avgBlockGenerationTimeInSec = 30.0;

        // Initial target and block hash.
        tmpBlockHash = DigestUtils.sha256Hex(args[0]);
        tmpTargetHash = args[1];

        // Tasks for master node.
        if(rank == 0){

            // Block id.
            int currentBlockID = 1;

            while (currentBlockID<=num_blocks)
            {

                // Timer to keep track of time.
                MyTimer myTimer = new MyTimer("CurrentBlockID:"+currentBlockID);
                myTimer.start_timer();

                System.out.println("Block hash : "+tmpBlockHash);
                System.out.println("Target Hash :"+tmpTargetHash);

                // Calling master function and storing the nonce.
                Long nonce = master(currentBlockID);
                System.out.println("Found nonce : "+nonce);
                myTimer.stop_timer();
                myTimer.print_elapsed_time();

                // Interrupting other running nodes.
                for(int index = 1;index<size;index++){
                    MPI.COMM_WORLD.iSend(MPI.newIntBuffer(1).put(0,1),1,MPI.INT,index,99);
                }

                // Code block to test the validity of nonce.
//                System.out.println("//////////////// Testing /////////////////////");
//                String tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(tmpBlockHash+nonce));
//                System.out.println(tmpTargetHash.compareTo(tmp_hash)>0);
//                System.out.println("////////////////End Testing //////////////////////");
//                System.out.println();

                // Method deployed ensure master was not receiving nonce from earlier blocks.
//                Status s = MPI.COMM_WORLD.iProbe(MPI.ANY_SOURCE,1);
//                while (s != null){
//                    LongBuffer temp = MPI.newLongBuffer(1);
//                    MPI.COMM_WORLD.iRecv(temp,1,MPI.LONG,s.getSource(),1);
//                    s = MPI.COMM_WORLD.iProbe(MPI.ANY_SOURCE,1);
//                }

                // Update block and target hashes.
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

        // Tasks for slave node.
        else{

            while (true){

                LongBuffer send_buffer = MPI.newLongBuffer(3);
                // Receive start and end nonce along with current block id.
                MPI.COMM_WORLD.recv(send_buffer,3,MPI.LONG,0,0);
                long start_nonce = send_buffer.get(0);
                long end_nonce = send_buffer.get(1);
                int current_block = (int) send_buffer.get(2);

                // Receiving block hash.
                Status s = MPI.COMM_WORLD.probe(0,0);
                CharBuffer block = MPI.newCharBuffer(s.getCount(MPI.CHAR));
                MPI.COMM_WORLD.recv(block,s.getCount(MPI.CHAR),MPI.CHAR,0,0);

                // Receiving target hash.
                s = MPI.COMM_WORLD.probe(0,0);
                CharBuffer target = MPI.newCharBuffer(s.getCount(MPI.CHAR));
                MPI.COMM_WORLD.recv(target,s.getCount(MPI.CHAR),MPI.CHAR,0,0);
                slave(start_nonce,end_nonce,block.toString(),target.toString(),rank,current_block);

            }

        }
        MPI.COMM_WORLD.abort(0);

    }


}
