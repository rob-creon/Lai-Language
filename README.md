# Lai-Language
An overly ambitious attempt to improve C++, made mainly for educational purposes.

It uses no external libraries, only Java. All code is written by me (for fun and learning), although a lot of inspiration and help is taken from an assortment of online articles and resources for compiler design.

Currently it compiles to extremely basic C. The goal will be to make it compile to C code that is platform independent. The compiler has support for automatically compiling the generated C code using either gcc (on linux) or VS (on windows). 
(Credit to https://stackoverflow.com/users/4954175/jamars for writing the batch file used to find the VS installation).

In the future, I may extend this to compile to x86 when I have time to more completely learn assembler. In the mean time I want to flesh out the language to where it can be seriously used.

# How to Use
The compiler takes file arguments and a few flags. 

Compile helloworld.lai example: 
```
    java --class-path bin lai.Main tests/helloworld.lai -gcc
```
Compile using VS:
```
    java --class-path bin lai.Main tests/helloworld.lai -visualstudio
```

Currently the compiler does accept multiple files through the command line, but it will likely not actually compile them.

Note: VS will give "output.exe" and GCC will give "a.obj"