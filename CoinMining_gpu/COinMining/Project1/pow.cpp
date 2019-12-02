#include <iostream>
#include <string> 
#include <iomanip>
#include <math.h>
#include <sstream>
#include "sha256.h"

using namespace std;
int pow(string initBlockHash, string initTargetHash) {
	cout<<"Performing Proof-of-Work...wait..."<<endl;
	int nonce = 0;
	string tmp_hash = "undefined";
	for (nonce = INT_MIN; nonce <= INT_MAX; nonce++) {
		//cout << " Checking for Nonce : " << nonce << endl;
		tmp_hash = sha256(sha256(initBlockHash+ std::to_string(nonce)));
		if (initTargetHash.compare(tmp_hash) > 0) {
			break;
		}
	}
	cout << "Resulting Hash: " << tmp_hash << endl;
	return nonce;	
}
int main() {

	string initBlockHash = sha256("CSCI-654 Foundations of Parallel Computing");
	cout << initBlockHash << endl;
	string initTargetHash = "0000092a6893b712892a41e8438e3ff2242a68747105de0395826f60b38d88dc";
	int nonce = 0;
	nonce = pow(initBlockHash, initTargetHash);
	cout << "Nonce : " << nonce << endl;
}