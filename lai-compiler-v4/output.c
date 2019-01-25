#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
int32_t FirstNum0;
int32_t SecondNum1;
int32_t getSum0(int32_t a2, int32_t b3);
int32_t getDifference1(int32_t a4, int32_t b5);
int32_t getProduct2(int32_t a6, int32_t b7);
int32_t getQuotient3(int32_t a8, int32_t b9);
int32_t getSum0(int32_t a2, int32_t b3){
	return ((a2) + (b3));
}
int32_t getDifference1(int32_t a4, int32_t b5){
	return ((a4) - (b5));
}
int32_t getProduct2(int32_t a6, int32_t b7){
	return ((a6) * (b7));
}
int32_t getQuotient3(int32_t a8, int32_t b9){
	return ((a8) / (b9));
}

int main() {
	FirstNum0 = (1600);
	SecondNum1 = (400);
	printf(("Numbers are: %d, %d\n"), (FirstNum0), (SecondNum1));
	printf(("Sum: %d\n"), (getSum0((FirstNum0), (SecondNum1))));
	printf(("Difference: %d\n"), (getDifference1((FirstNum0), (SecondNum1))));
	printf(("Product: %d\n"), (getProduct2((FirstNum0), (SecondNum1))));
	printf(("Quotient: %d\n"), (getQuotient3((FirstNum0), (SecondNum1))));

}