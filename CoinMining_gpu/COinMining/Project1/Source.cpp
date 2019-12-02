#include <iostream>
#include <string> 
#include <iomanip>
#include <math.h>
#include <sstream>
#include <sys/time.h>
#include "sha256.h"

using namespace std;

int main() {
		
	string initBlock = sha256("CSCI-654 Foundations of Parallel Computing");
	cout << initBlock << endl;
}