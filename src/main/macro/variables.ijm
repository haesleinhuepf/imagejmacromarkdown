/*
# Variables

In ImageJ Macro you can have variables which can hold numbers, strings or arrays.
This is how they work.

## Numbers

Numeric variables can be combined to do some math
*/

a = 5;
b = 6;

c = a + b;

print(c);

/*
## Strings

You can also combine strings to assemble texts using variables.
*/

name = "Robet";
welcome = "Hello";

print(welcome + name);

print(welcome + " " + name + "!");

/*
## Arrays
 
Last but note least there are arrays. Arrays are containers which hold several 
other variables. In the following example there is an array called `array` which
holds three numbers. You can access these numbers by putting [] behind the array
variable. Note that the entries in the container are numbered starting with 0:
*/

array = newArray(3);
array[0] = 6;
array[1] = 7;
array[2] = 8;

Array.print(array);