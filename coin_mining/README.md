#Coin Mining
Objective: Find a 32-bit nonce (Integer.MIN_VALUE to Integer.MAX_VALUE) that has a hash less than a target

###Pseudo-code
```
do {
     Nonce++;
     hash = SHA256(SHA256(BlockInfo, Nonce));
} while (hash < target)
```


###Example output
```
BlockHash: 0aca36d7d8e3bd46e6bab5bf3a47230e91e100ccd241c169e9d375f5b2a28f82
TargetHash: 0000092a6893b712892a41e8438e3ff2242a68747105de0395826f60b38d88dc
Performing Proof-of-Work...wait...
Resulting Hash: 000006cf83ec42b3c0cba07e2c2b7bf4a40e3af16041fcc8e2651eb686d5f97d
Nonce:-2145652355
```