package edu.rit.cs;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class CoinMining_Seq {
    /**
     * convert byte[] to hex string
     * @param hash
     * @return hex string
     */
    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * get a sha256 of the input string
     * @param inputString
     * @return resulting hash in hex string
     */
    public static String SHA256(String inputString) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return bytesToHex(sha256.digest(inputString.getBytes(StandardCharsets.UTF_8)));
        }catch (NoSuchAlgorithmException ex) {
            System.err.println(ex.toString());
            return null;
        }
    }

    /**
     * get a randomized target hash
     * @return randomized target hash
     */
    public static String getTargetHash() {
        Random rand = new Random();
        int randInt = rand.nextInt(1000);
        return SHA256(String.valueOf(randInt));
    }

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
        for(nonce=Integer.MIN_VALUE; nonce<=Integer.MAX_VALUE; nonce++) {
            tmp_hash = SHA256(SHA256(blockHash+String.valueOf(nonce)));
            if(targetHash.compareTo(tmp_hash)>0)
                break;
        }
        System.out.println("Resulting Hash: " + tmp_hash);
        System.out.println("Nonce:" + nonce);
        return nonce;
    }


    public static void main(String[] args) {
        String blockHash = SHA256("CSCI-654 Foundations of Parallel Computing");
        System.out.println("BlockHash: " + blockHash);

        String targetHash = "0000092a6893b712892a41e8438e3ff2242a68747105de0395826f60b38d88dc";
        System.out.println("TargetHash: " + targetHash);

        int nonce = pow(blockHash, targetHash);

    }


}
