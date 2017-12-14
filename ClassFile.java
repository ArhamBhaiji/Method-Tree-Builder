import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Parses and stores a Java .class file.
 *
 * @author David Cooper
 * @author Fatema Shabbir - 19201960
 */
public class ClassFile
{
    private final String filename;
    private DataInputStream dis;
    private final long magic;
    private final int minorVersion;
    private final int majorVersion;
    private final ConstantPool constantPool;
  
    //private String attribute[];
    private ArrayList methods = new ArrayList();
    private String accessF;
    private String className;
    private String superclassName;
    private String fields;
    private final int methodsCount;
    
    //holds all the called methods to check recursion
    private ArrayList parents = new ArrayList();
    //counts the methods under the entered method name
    private int methodPrintCount;
    //holds method name
    private String methodName;
    //holds attributes for methods to reference in signature
    private int [] attrCount;
    //holds all the attributes for all the methods. 
    //Need reference these via attrCount
    private ArrayList signatures = new ArrayList();
        
    public ClassFile(String filename) throws ClassFileParserException,
                                             IOException
    {
        Scanner sc = new Scanner(System.in);
        
        DataInputStream dis =
            new DataInputStream(new FileInputStream(filename));

        this.filename = filename;
        magic = (long)dis.readUnsignedShort() << 16 | dis.readUnsignedShort();
        minorVersion = dis.readUnsignedShort();
        majorVersion = dis.readUnsignedShort();
        constantPool = new ConstantPool(dis);
        
        //access flag for class
        int accessFlag = dis.readUnsignedShort();
        
        //name of class
        int thisClass = dis.readUnsignedShort();
        String m = constantPool.getEntry(thisClass).getValues();
        thisClass = Integer.parseInt(m, 16);
        className = constantPool.getEntry(thisClass).getValues();
        
        if (className.contains("."))
        {
            int dot = className.indexOf(".");
            className = className.substring(0,dot);
        }
        
        //get super class
        int superClass = dis.readUnsignedShort();
        
        //get interface count
        int interfaceCount = dis.readUnsignedShort();
        //int interfaces[interfaceCount];
        
        //fields
        int fieldsCount = dis.readUnsignedShort();
        
        if (fieldsCount > 0) 
        {
            //makes an array out of all the fields
            for (int i = 0; i < fieldsCount; i++)
            {
                makeField(dis);
            }
        }
        
        //methods
        methodsCount = dis.readUnsignedShort();
        attrCount = new int[methodsCount];
        
        //makes an array out of all the methods
        for (int i = 0; i < methodsCount; i++)
        {
            makeMethod(dis, i);
        }
        
        System.out.println("------------------------------------------------");
        System.out.println("METHODS FOUND IN " + className);
        System.out.println("------------------------------------------------");
        
        //prints methods to let user see what is available
        for (int t = 0; t < methodsCount; t++)
        {
            System.out.println(methods.get(t));
        }
        
        //takes the method name to form tree
        System.out.println("------------------------------------------------");
        System.out.println("Enter correct method name to build call tree.");
        System.out.print("Please be mindful of the spelling and case. \n> ");
        String methodName = sc.nextLine();
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println("CALL TREE FOR " + methodName + " IN " + className);
        System.out.println("------------------------------------------------");
        
        //prints call tree
        print(methodName);
        
        //displays total number of methods in the tree
        System.out.println("------------------------------------------------");
        System.out.println("TOTAL NUMER OF METHODS PRINTED UNDER " + methodName + " : " + methodPrintCount);
        System.out.println("------------------------------------------------");
        
    }
    
    //starts the print tree
    private void print (String key)
    {
        //get the index of the method to pass to the printNested method
        int methodIndex = getMethodIndex(key);
        methodPrintCount = 0;
        
        //set start value of attributes in signature array
        int countAtrs = getAttrCountIndex(methodIndex);
        
        //start printing
        if (methodIndex != -99)
        {
            System.out.println(methods.get(methodIndex));
            //adds to the calledArray list to compare
            parents.add(methods.get(methodIndex)); 
            printNested(countAtrs, methodIndex, 1);
            
            parents.clear();
        }
        //display error if method does not exist
        else
        {
            System.err.println("Invalid method. Please try again with an existing method.");
        }
    }
    
    //prints the tree recursively
    private void printNested(int countAtrs, int x, int loopCount)
    {
        ArrayList calledN = new ArrayList();
        boolean missing = true;
        boolean calledB = false;       
        
        for (int y = countAtrs; y < (attrCount[x] + countAtrs); y++)
        {
            String tabs = "\t";
            for (int t = 1; t < loopCount; t++)
                tabs = tabs + "\t";
            
            //checks for recursive
            calledB = isRecursive(y);
            
            //checks for missing
            missing = isMissing(y);
            
            
            if (calledN.size() == 0)
            {
                if (calledB)
                {
                    System.out.println(tabs + signatures.get(y) + " [recursive]"); 
                    methodPrintCount++;
                    calledN.add(signatures.get(y));
                }

                else if (missing)
                {
                    System.out.println(tabs + signatures.get(y) + " [missing]"); 
                    methodPrintCount++; 
                    calledN.add(signatures.get(y));
                }
                else if (!calledB)
                {
                    System.out.println(tabs + signatures.get(y)); 
                    methodPrintCount++; 
                    calledN.add(signatures.get(y));

                    int mIndex = getMethodIndex(signatures.get(y).toString());
                    int Count = loopCount;
                    if (mIndex != -99)
                    {
                        int aCount = getAttrCountIndex(mIndex);
                        Count++;
                        parents.add(signatures.get(y).toString());
                        printNested(aCount, mIndex, Count);
                        parents.remove(parents.size() - 1);
                    }                            
                }

                calledB = false;
                //break;
                
            }
            else
            {
                for (int z = 0; z < calledN.size(); z++)
                {                    
                    if (calledN.get(z).equals(signatures.get(y)))
                    {
                        break;
                    }
                    else if (calledB)
                    {
                        System.out.println(tabs + signatures.get(y) + " [recursive]"); 
                        methodPrintCount++; 
                        calledN.add(signatures.get(y));
                        
                        break;
                    }
                    else
                    {
                        for (int m = 0; m < methodsCount; m++)
                        {
                            if (signatures.get(y).equals(methods.get(m)))
                            {
                                missing = false;
                                break;
                            }
                        }
                        
                        if (missing && !calledB)
                        {
                            System.out.println(tabs + signatures.get(y) + " [missing]"); 
                            methodPrintCount++; 
                            //called.add(signatures.get(y));
                            calledN.add(signatures.get(y));
                            break;
                        }
                        else if  (!calledB)
                        {
                            System.out.println(tabs + signatures.get(y)); 
                            methodPrintCount++; 
                            //called.add(signatures.get(y));
                            calledN.add(signatures.get(y));
                            int mIndex;
                            mIndex = getMethodIndex(signatures.get(y).toString());
                            int Count = loopCount;
                            if (mIndex != -99)
                            {
                                int aCount = getAttrCountIndex(mIndex);
                                Count++;
                                parents.add(signatures.get(y).toString());
                                printNested(aCount, mIndex, Count);
                                parents.remove(parents.size() - 1);
                            }
                            break;
                        }
                        calledB = false;
                        //break;
                    }
                }
            }
            
        }
        
        calledN.clear();
    }
    
    //gets the index in the signature array list of the starting value
    //for the relevant method
    private int getAttrCountIndex (int methodIndex)
    {
        int countAtrs = 0;
        
        for (int x = 0; x < methodIndex; x++)
        {
            countAtrs += attrCount[x];
        }
        
        return countAtrs;
    }
    
    //gets in the index of the method to be printed
    private int getMethodIndex(String methodName)
    {
        
        for (int x = 0; x < methodsCount; x++)
        {
            if (methods.get(x).toString().contains(methodName))
                return x;
        }
        
        return -99;
    }
    
    //checks if method is missing
    private boolean isMissing(int index)
    {
        for (int m = 0; m < methodsCount; m++)
        {
            if (signatures.get(index).equals(methods.get(m)))
            {
                return false;
            }
        }
        return true;
    }
    
    //checks if method is recursive
    private boolean isRecursive(int index)
    {
        for (int c = 0; c < parents.size(); c++)
        {
            if (signatures.get(index).equals(parents.get(c)))
            {
                return true;
            }
        }
        return false;
    }
    
    //makes the field and returns in string
    private String makeField(DataInputStream dis) throws IOException, NullPointerException, InvalidConstantPoolIndex
    {
        //get access flag
        int accessFlag = dis.readUnsignedShort();
        
        //get name
        int nameIndex = dis.readUnsignedShort();
        String name = constantPool.getEntry(nameIndex).getValues();
        
        //get descriptor
        int descriptorIndex = dis.readUnsignedShort();
        String descriptor = constantPool.getEntry(descriptorIndex).getValues();
        
        int attributeCount = dis.readUnsignedShort();
        if (attributeCount > 0)
        {
            int attribute_name_index = dis.readUnsignedShort();
            int attribute_length = dis.readUnsignedShort();
    //u1 info[attribute_length];*/
        }
        
        String field = accessFlag + " " + descriptor + " " + name;
        return field;
    }
    
    //makes method and returns in string
    public void makeMethod(DataInputStream dis, int index) throws IOException, NullPointerException, InvalidConstantPoolIndex
    {        
        //get access flag
        int accessFlag = dis.readUnsignedShort();
        
        //get name
        int nameIndex = dis.readUnsignedShort();
        nameIndex = Integer.parseInt(Integer.toHexString(nameIndex), 16);
        String name = constantPool.getEntry(nameIndex).getValues();        
        
        //get descriptor
        int descriptorIndex = dis.readUnsignedShort();
        String descriptor = Integer.toHexString(descriptorIndex);
        descriptorIndex = Integer.parseInt(descriptor, 16);
        descriptor = constantPool.getEntry(descriptorIndex).getValues();
        
        if (descriptor.contains("/"))
        {
            descriptor = descriptor.split("/")[2];
            descriptor = descriptor.substring(0, descriptor.length() - 1); 
        }
        
        int parameterCount = 0;
        
        //attribute count
        int attributeCount = dis.readUnsignedShort();
        
        if (attributeCount > 0)
        {
            for (int x = 0; x < attributeCount; x++)
            {
                makeAttribute(dis, index);                
            }
        }
        
        String method = className + "." + name + " " + descriptor;
        methods.add(method);
    }
    
    public String[] makeAttribute(DataInputStream dis, int index) throws InvalidConstantPoolIndex, IOException
    {
        String[] str = new String[2];
        String attribute_name = Integer.toHexString(dis.readUnsignedShort());
        int attribute_name_index = Integer.parseInt(attribute_name, 16);
        attribute_name = constantPool.getEntry(attribute_name_index).getValues();
        str [0] = attribute_name;
        
        int attribute_length = dis.readUnsignedShort();
        attribute_length = attribute_length + dis.readUnsignedShort();
        str [1] = attribute_name;

        if (attribute_name.equalsIgnoreCase("Code") && attribute_length > 0)
        {
            int max_stack = dis.readUnsignedShort();
            int max_locals = dis.readUnsignedShort();
            int code_length = dis.readUnsignedShort();
            code_length = code_length + dis.readUnsignedShort();
            
            int[] opCode = new int[code_length];
            for (int i = 0; i < code_length; i++)
            {
                opCode[i] = dis.readUnsignedByte();

                if (opCode[i] == 182 || opCode[i] == 183 || opCode[i] == 184 || opCode[i] == 185 || opCode[i] == 186)
                {
                    String signature = "";
                    int getIt;
                    getIt = dis.readUnsignedShort();
                    String thing = constantPool.getEntry(getIt).getValues();
                    
                    if (thing.matches("=?[0-9a-fA-F,]+"))
                    {
                        String nameL = thing.substring(0,2); 
                        String typeL = thing.substring(3,thing.length());
                        getIt = Integer.parseInt(nameL, 16);
                        nameL = constantPool.getEntry(getIt).getValues();
                        getIt = Integer.parseInt(typeL, 16);
                        typeL = constantPool.getEntry(getIt).getValues();
                        String nameX = "", typeX = "";
                        
                        getIt = Integer.parseInt(nameL, 16);
                        nameL = constantPool.getEntry(getIt).getValues();
                        
                        if (nameL.contains("/"))
                        {
                            nameL = nameL.split("/")[2];
                        }
                        
                        
                        if (typeL.matches("=?[0-9a-fA-F,]+"))
                        {
                            nameX = typeL.substring(0,2); 
                            getIt = Integer.parseInt(nameX, 16);
                            nameX = constantPool.getEntry(getIt).getValues();
                            typeX = typeL.substring(3,typeL.length()); 
                            getIt = Integer.parseInt(typeX, 16);
                            typeX = constantPool.getEntry(getIt).getValues();
                        }
                        
                        signature = nameL + "." + nameX + " " + typeX;
                        
                        signatures.add(signature);
                        
                        attrCount[index] = attrCount[index] + 1;
                        
                    }
                    i = i + 2;
                }
            }
            
            int exception_table_length = dis.readUnsignedShort();
            
            if (exception_table_length > 0)
            {
                for (int x = 0; x < exception_table_length; x++)
                {
                    int start_pc = dis.readUnsignedShort();
                    int end_pc = dis.readUnsignedShort();
                    int handler_pc = dis.readUnsignedShort();
                    int catch_type = dis.readUnsignedShort();
                }
                
            }
            int attributes_count2 = dis.readUnsignedShort();
            
            for (int x = 0; x < attributes_count2; x++)
            {
                    makeAttribute(dis, index);
            }
                
        }
        
        else 
        {
            for (int x  = 0; x < attribute_length; x++)
                dis.readUnsignedByte();
        }
        
        return str;
    }
}
