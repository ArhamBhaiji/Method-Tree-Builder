
--------------
Compilation
--------------
If the command-line is being used the the program may be compiled using the following after moving to the source code directory:

[user@pc]$ cd Code
[user@pc]$ javac *.java
[user@pc]$ java ClassFileParser

The compilation results in a warning due to an exception referenced in the class. This may be ignored as it does not affect the code or the results in any way.

The user will then be prompted to enter the class name and the method name to build to call tree for.

--------------
Functionality
--------------
- The code has been implemented to build the call tree for the method that is entered by the user.
- Both the class and the method for which the call tree is to be constructed shall be entered by the user. If these are invalid, an error message is displayed.
- Standard input is used to take in both parameters.
- The methods printed are unique.
- The total number of the methods called under the method entered are printed.
- If a method is called recursively it is indicated.
- If missing classes are initialized and methods from them are called it is marked with [missing].
- Overloaded methods are printed separately.
