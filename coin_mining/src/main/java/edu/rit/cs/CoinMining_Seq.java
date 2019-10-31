package edu.rit.cs;
import org.apache.commons.codec.digest.*;

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
            tmp_hash = DigestUtils.sha256Hex(DigestUtils.sha256Hex(blockHash+String.valueOf(nonce)));
            if(targetHash.compareTo(tmp_hash)>0)
                break;
        }
        System.out.println("Resulting Hash: " + tmp_hash);
        return nonce;
    }


    public static void main(String[] args) {
        String blockHash = DigestUtils.sha256Hex(args[0]);
        String targetHash = args[1];
        System.out.println("BlockHash: " + blockHash);
        System.out.println("TargetHash: " + targetHash);
        long start_time = System.currentTimeMillis();
        int nonce = pow(blockHash, targetHash);
        System.out.println("Nonce:" + nonce);
        System.out.println("Time taken :"+(System.currentTimeMillis()-start_time));
    }


}
