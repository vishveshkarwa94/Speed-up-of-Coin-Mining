#include <iostream>
#include <string> 
#include <iomanip>
#include <math.h>
#include <sstream>
#include "sha256.h"
#include "sha256.cuh"

using namespace std;

__managed__ int nonce;

__global__
void pow(string initBlockHash, string initTargetHash, int range) {

	cout<<"Performing Proof-of-Work...wait..."<<endl;
	// index = block index * number of threads per block + thread index
    int index = blockIdx.x * blockDim.x + threadIdx.x;
    // stride  = number threads per block * number of block per grid
    int stride = blockDim.x * gridDim.x;
	nonce = 0;
	string tmp_hash = "undefined";
	for (int tmp_nonce = index; tmp_nonce <= range; tmp_nonce= tmp_nonce+stride) {
		//cout << " Checking for Nonce : " << nonce << endl;
		tmp_hash = sha256(sha256(initBlockHash+ std::to_string(tmp_nonce)));
		if (initTargetHash.compare(tmp_hash) > 0) {
			nonce = tmp_nonce;
			break;
		}
	}
}
int main() {

	string initBlockHash = sha256("CSCI-654 Foundations of Parallel Computing");
	cout << initBlockHash << endl;
	string initTargetHash = "0000092a6893b712892a41e8438e3ff2242a68747105de0395826f60b38d88dc";
	int nonce = 0;	
	int range = INT_MAX - INT_MIN;
	int blockSize = 256;
	int numBlocks = (range + blockSize - 1) / blockSize;
	pow <<<numBlocks, blockSize >>> (initBlockHash,initTargetHash,range);
	cudaDeviceSynchronize();
	cout << "Nonce : " << nonce << endl;
}