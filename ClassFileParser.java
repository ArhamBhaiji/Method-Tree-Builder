import java.io.*;
import java.util.Scanner;

/**
 * Parses and displays a Java .class file.
 *
 * @author David Cooper
 */
public class ClassFileParser
{
    public static void main(String[] args) throws ClassFileParserException, IOException
    {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("------------------------------------------------");
        System.out.println("WELCOME TO THE CALL TREE BUILDING PROGRAM");
        System.out.println("------------------------------------------------");
        System.out.print("Enter class name to parse with the extension: \n> ");
        String className = sc.nextLine();
        
        if (className.contains(".class"))
        {
            ClassFile cf = new ClassFile(className);
        }
        else
        {
            System.err.println("Invalid class type please try again!");
            System.exit(1);
        }
    }
}
