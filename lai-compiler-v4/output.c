#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
int16_t globalVar0;
int16_t printSum0(int16_t argA2, int16_t argB3);
int16_t recursiveMultiply3(int16_t Av7, int16_t Bv8);
int16_t localFunc1();
int16_t tick2(int16_t A4, int16_t sumSoFar5, int16_t ctr6);
int16_t printSum0(int16_t argA2, int16_t argB3){
	int16_t sum1;
	printf("%d + %d", argA2, argB3);
	printf(" = ");
	sum1 = argA2 + argB3;
	printf("%d", sum1);
	printf("\n");
}
int16_t recursiveMultiply3(int16_t Av7, int16_t Bv8){
	tick2(Av7, 0, Bv8);
	globalVar0 = 0;
}
int16_t localFunc1(){
}
int16_t tick2(int16_t A4, int16_t sumSoFar5, int16_t ctr6){
	printf("Tick: ");
	printf("%d", sumSoFar5);
	printf("\n");
	sumSoFar5 = sumSoFar5 + 1;
	ctr6 = ctr6 - 1;
	tick2(A4, sumSoFar5, ctr6);
}

int main() {
	globalVar0 = 100;
	printf("Hello World\n");
	printSum0(5, 6);
	recursiveMultiply3(1, 2);
	system("PAUSE");

}