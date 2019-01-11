The goal of this project is primarily to make a working compiler that takes an input language and compiles to C or C++ (undecided as of yet). 
The general design of the language will be "C++ without the BS" which is most definitely over ambitious, but it's nice to set high goals.
Heavy inspiration taken from Johnathan Blow's Jai (that much is probably obvious).

Once I have a more clear definition of the language it will go here, but for now I will put some code samples that 
demonstrate the syntax I have in mind:

int i = 10; //Declares a int16_t, initialized to 10.
float j; //Declares a float16_t, intialized to 0. (why?? uninitialized declarations should be more explicit.)
int k = ---; //Declares a int16_t, uninitialized.

string emptyString; //becomes string emptyString = ""
string s = "String!";

print("String value is %s\n", s); //printf like syntax

/* 
print() is equivalent to console.write(). 
Other less common print and console related 
commands will be stored in the console namespace. 
*/

string getMessage = (int[] params) {
  string stringToReturn;
  
  for(int i = 0; i < params.size; i++) {
    stringToReturn += params[i] + ", "; //Java style handling of inserting number types into strings
  }
  
  //This for loop does the same thing as the one above.
  for(params) {
    stringToReturn += it + ", ";
    //i is a hidden local integer in this for loop. The compiler will optimize i to be a unsigned char or whatever, depending on the for loop.
  }
}

//TODO pointers
