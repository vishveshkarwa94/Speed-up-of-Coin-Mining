package edu.rit.cs;
import org.apache.commons.codec.digest.*;

import java.math.BigInteger;

public class CoinMining_Seq {

    /**
     * perform the proof-of-work
     * @param blockHash hash of the blockinfo
     * @param targetHash target hash
     * @return nonce (a 32-bit integer) that satisfies the requirements
     */
    public static int pow(String blockHash, String targetHash) {
        System.out.println("Performing Proof-of-Work...wait...");
        int nonce=0;
        String tmp_hash="undefined";
        for(nonce=Integer.MAX_VALUE; nonce<=Integer.MAX_VALUE; nonce++) {
            tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(blockHash+nonce));
            if(targetHash.compareTo(tmp_hash)>0)
                break;
        }
        return nonce;
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


    public static void main(String[] args) {
        int num_blocks = 10;
        double avgBlockGenerationTimeInSec = 30.0;

        String blockHash = DigestUtils.sha256Hex(args[0]);
        String targetHash = args[1];

        int currentBlockID = 1;

        String tmpBlockHash = blockHash;
        String tmpTargetHash = targetHash;

        MyTimer myTimer;
        int  nonce = 0;

        System.out.println("BlockHash: " + blockHash);
        System.out.println("TargetHash: " + targetHash);

        while (currentBlockID<= num_blocks){

            myTimer = new MyTimer("CurrentBlockID:"+currentBlockID);
            myTimer.start_timer();
            nonce = pow(tmpBlockHash, tmpTargetHash);
            System.out.println("Nonce : "+nonce);
            myTimer.stop_timer();
            myTimer.print_elapsed_time();
            System.out.println();

            tmpBlockHash = DigestUtils.sha256Hex(tmpBlockHash+"|"+nonce);
            if(myTimer.get_elapsed_time_in_sec()<avgBlockGenerationTimeInSec)
                tmpTargetHash = HexValueDivideBy(tmpTargetHash, 2);
            else
                tmpTargetHash = HexValueMultipleBy(tmpTargetHash, 2);

            System.out.println("New Block Hash:  " + tmpBlockHash);
            System.out.println("New Target Hash: " + tmpTargetHash);
            currentBlockID++;
        }
    }


}
