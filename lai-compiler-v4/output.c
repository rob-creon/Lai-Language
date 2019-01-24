#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
int16_t product0;
int16_t globalVar1;
int16_t recursiveMultiply1(int16_t va7, int16_t vb8);
int16_t myFunction0(int16_t ctr4, int16_t a5, int16_t b6);
int16_t recursiveMultiply1(int16_t va7, int16_t vb8){
	int16_t value_a2;
	int16_t value_b3;
	value_a2 = (va7);
	value_b3 = (vb8);
	myFunction0((1), (value_a2), (value_b3));
	printf(("%d * %d = %d\n"), (value_a2), (value_b3), (product0));
}
int16_t myFunction0(int16_t ctr4, int16_t a5, int16_t b6){
	product0 = ((product0) + (a5));
	if(((ctr4) != (b6))){	myFunction0(((ctr4) + (1)), (a5), (b6));
};
}

int main() {
	product0 = (0);
	globalVar1 = (100);
	printf(("Hello World\n"));
	recursiveMultiply1((1000), (5));
	system(("PAUSE"));

}