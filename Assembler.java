//Vidit Jain 2017370
//Sankalp Agrawal 2017363
import java.io.*;
import java.util.*;

public class Assembler
{
    public static String convertToBinary(int num)
    {
        String binary=Integer.toBinaryString(num);
        if(binary.length()!=8)
        {
            int diff=8-binary.length();
            for(int i=0;i<diff;i++)
            {
                binary="0"+binary;
            }
        }
        return binary;
    }

    public static String passOne(ArrayList<Symbol> symbolTable,ArrayList<Literal> literalTable, HashMap<String, String> opcodeTable, BufferedReader bufferedReader,ArrayList<Opcode_output> opcode_output_table,ArrayList<String> errors)
    {
        String output="";
        try
        {
            boolean stopOccurred=false;

            String lineString=bufferedReader.readLine();    //read line from file
            int lineCounter=1;           //counts the lines
            int locationCounter=0;     //keeps track of the length of instructions in words (1 word=12 bits)
            

            while(lineString!=null)
            {
                String opcode="";
                String operands="";
                
                if(lineString.equals(""))
                {
                    lineString=bufferedReader.readLine();
                }
                else
                {
                    int instructionLength=0;
                    if(!stopOccurred)       //if stop has not occurred
                    {
                        String[] instruction=lineString.split("\t");
                        String symbol=instruction[0];
                        opcode=instruction[1];
                        operands="";
                        int numberOfOperands=0;
                        if(instruction.length>2)
                        {
                            operands=instruction[2];
                            numberOfOperands=1;
                        }
                        if(operands.contains(","))
                        {
                            numberOfOperands=operands.split(",").length;
                        }
                        String comment="";
                        if(instruction.length==4)
                        {
                            comment=instruction[3].substring(2);
                        }

                        {
                            if(!symbol.equals(""))           //checks if there is a symbol
                            {
                                try
                                {
                                    if(opcodeTable.containsKey(symbol))      //if an opcode is used as a symbol
                                    {
                                        throw(new CustomException("Keyword "+symbol+" cannot be used at line "+lineCounter));
                                    }
                                    else
                                    {
                                        Symbol symbol1=new Symbol(symbol,locationCounter,null,"label");
                                        symbolTable.add(symbol1);
                                    }
                                }
                                catch(CustomException e)
                                {
                                    errors.add(e.getMessage());
                                }
                            }
                            boolean error=false;            //to check if error has occured in the statement

                            try
                            {
                                if(opcode.equals(""))
                                {
                                    error=true;
                                    throw new CustomException("Opcode not added" + " at line " + lineCounter);  // ERROR- if Opcode is not added
                                }
                            }
                            catch(CustomException e)
                            {
                                errors.add(e.getMessage());
                            }
                            if(!opcode.equals(""))                 //checks if there is an opcode given
                            {
                                try
                                {
                                    if(opcodeTable.containsKey(opcode))
                                    {
                                        instructionLength=instructionLength+4;          //4 bits for opcode
                                        if(opcode.equals("STP"))
                                        {
                                            stopOccurred=true;
                                        }
                                    }
                                    else           //invalid opcode
                                    {
                                        error=true;
                                        throw(new CustomException("Opcode not found at line "+lineCounter));
                                    }
                                }
                                catch(CustomException e)
                                {
                                    errors.add(e.getMessage());
                                }
                            }
                            try
                            {
                                if(operands.equals(""))
                                {
                                    if(!opcode.equals("CLA") && !opcode.equals("STP"))
                                    {
                                        error=true;
                                        throw(new CustomException("Opcode supplied with insufficient operands at line "+lineCounter));
                                    }
                                }
                            }
                            catch(CustomException e)
                            {
                                errors.add(e.getMessage());
                            }

                            if(!operands.equals(""))            //checks if there are operands given
                            {
                                try
                                {
                                    if(opcode.equals("CLA") || opcode.equals("STP") || numberOfOperands>1)
                                    {
                                        error=true;
                                        throw new CustomException("Opcode supplied with too may operands at line "+lineCounter);
                                    }
                                }
                                catch(CustomException e)
                                {
                                    errors.add(e.getMessage());
                                }
                                instructionLength=instructionLength+8;      //8 bits for operands
                                if(operands.charAt(0)=='=')                 //if operand is a literal
                                {
                                    String value=operands.substring(1);
                                    try
                                    {
                                        double doubleValue=Double.parseDouble(value);
                                        Literal literal=new Literal(doubleValue,locationCounter);
                                        literalTable.add(literal);
                                    }
                                    catch(NumberFormatException e)              //when invalid literal is used
                                    {
                                        errors.add("Invalid literal at line "+lineCounter);
                                    }
                                }
                            }
                            if(!error)        //if no error has occured, then add to opcode table for pass2
                            {
                                Opcode_output opcode_output=new Opcode_output(opcode,locationCounter,operands,opcodeTable.get(opcode),false);
                                opcode_output_table.add(opcode_output);
                            }
                        }
                    }

                    else                //if STP has occurred
                    {
                        String[] instruction=lineString.split("\t");
                        String operand=instruction[0];
                        String type=instruction[1];
                        String value;
                        if(instruction.length==2)
                        {
                            value=null;
                        }
                        else
                        {
                            value=instruction[2];
                        }
                        Symbol symbol1=new Symbol(operand,locationCounter,value,"operand");
                        symbolTable.add(symbol1);
                        instructionLength=12;
                    }
                    lineCounter++;       //increase line number
                    int numOfWords;
                    if(instructionLength%12!=0)    //number of words an instruction would occupy
                    {
                        numOfWords=(instructionLength/12)+1;
                    }
                    else
                    {
                        numOfWords=instructionLength/12;
                    }
                    locationCounter=locationCounter+numOfWords;                     //increasing locationCounter by number of words occupied
                    try
                    {
                        if(locationCounter>255)
                        {
                            throw new CustomException("Memory not available");
                        }
                    }
                    catch(CustomException e)
                    {
                        errors.add(e.getMessage());
                    }
                    
                    lineString=bufferedReader.readLine();         //read next line
                }
            }
            for(int i=0;i<literalTable.size();i++)
            {
                if(i>=1)
                {
                    locationCounter+=1;
                }
                Literal literal=literalTable.get(i);
                literal.location=locationCounter;              //assigning the correct addresses to literals
            }

           
            HashMap<String,Integer> operandsDeclaration=new HashMap<>();
            for(int i=0;i<symbolTable.size();i++)                        //hashmap with symbols and their frequencies
            {
                String symbol=symbolTable.get(i).symbol;
                if(operandsDeclaration.containsKey(symbol))
                {
                    operandsDeclaration.put(symbol,operandsDeclaration.get(symbol)+1);
                }
                else
                {
                    operandsDeclaration.put(symbol,1);
                }
            }
            Collection<String> collection=operandsDeclaration.keySet();
            Iterator iterator=collection.iterator();
            while(iterator.hasNext())
            {
                try
                {
                    String key=(String) iterator.next();
                    if(operandsDeclaration.get(key)>1)            //if frequency of symbol is more than 1
                    {
                        throw new CustomException("Multiple declarations of "+key);
                    }
                }
                catch(CustomException e)
                {
                    errors.add(e.getMessage());
                }
            }
            try
            {
                if(!stopOccurred)                //if stop has not written then throw an error
                {
                    throw new CustomException("STP statement missing");
                }
            }
            catch(CustomException e)
            {
                errors.add(e.getMessage());
            }

            if(errors.size()==0)
            {
                output="Symbol_Table"+'\n';
                for(int i=0;i<symbolTable.size();i++)             //making the symbol table
                {
                    output=output+symbolTable.get(i).symbol+'\t'+symbolTable.get(i).type+'\t'+symbolTable.get(i).location+'\t'+symbolTable.get(i).value+'\n';
                }

                output=output+'\n'+"Literal_Table"+'\n';
                for(int i=0;i<literalTable.size();i++)          //making the literal table
                {
                    output=output+literalTable.get(i).value+'\t'+literalTable.get(i).location+'\n';
                }

                output=output+'\n'+"Opcode_Table"+'\n';
                for(int i=0;i<opcode_output_table.size();i++)     //making the opcode table
                {
                    if(!opcode_output_table.get(i).operand.equals(""))
                    {
                        if(opcode_output_table.get(i).operand.charAt(0)=='=')
                        {
                            output=output+opcode_output_table.get(i).opcode +'\t'+ opcode_output_table.get(i).binary +'\t'+opcode_output_table.get(i).ilc+'\t' +opcode_output_table.get(i).operand+'\t'+"immed8"+'\n';
                        }
                        else
                        {
                            output=output+opcode_output_table.get(i).opcode +'\t'+ opcode_output_table.get(i).binary +'\t'+opcode_output_table.get(i).ilc +'\t'+opcode_output_table.get(i).operand+'\t'+"reg"+'\n';
                        }
                    }
                    else
                    {
                        output=output+opcode_output_table.get(i).opcode +'\t'+ opcode_output_table.get(i).binary +'\t'+opcode_output_table.get(i).ilc +'\t'+"-"+'\n';
                    }
                }
                output=output+"\n";
            }
            else
            {
                for(int i=0;i<errors.size();i++)             //printing errors if any
                {
                    System.out.println(errors.get(i));
                }
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return output;
    }

    public static String passTwo(ArrayList<Symbol> symbolTable,ArrayList<Literal> literalTable,ArrayList<Opcode_output> opcode_output,ArrayList<String> errors)
    {
        String output="Machine_Code"+"\n";
        for(int i=0;i<opcode_output.size();i++)
        {
            Opcode_output instruction=opcode_output.get(i);
            output=output+instruction.binary+"\t";
            if(!instruction.opcode.equals("CLA") && !instruction.opcode.equals("STP"))       //CLA and STP do not have operands
            {
                if(instruction.operand.charAt(0)=='=')          //checking for literal
                {
                    String literal=instruction.operand.substring(1);
                    for(int j=0;j<literalTable.size();j++)
                    {
                        try
                        {
                            if(Double.parseDouble(literal)==literalTable.get(j).value)      //checking if literal is in literalTable
                            {
                                output=output+convertToBinary(literalTable.get(j).location)+'\t';
                            }
                        }
                        catch(NumberFormatException e)
                        {

                        }
                    }
                }
                else if(!instruction.operand.equals("") && !instruction.operand.equals("-"))      //checking for operands other than literals
                {
                    boolean operandFound=false;
                    for(int j=0;j<symbolTable.size();j++)
                    {
                        if(instruction.operand.equals(symbolTable.get(j).symbol) && symbolTable.get(j).type.equals("operand"))   //checking if operand is in symbol table
                        {
                            output=output+convertToBinary(symbolTable.get(j).location)+"\t";      //printing the locationCounter
                            operandFound=true;
                            break;
                        }
                    }
                    try
                    {
                        if(!operandFound)           //if operand in not defined
                        {
                            throw new CustomException("Symbol "+instruction.operand+" not defined");
                        }
                    }
                    catch(CustomException e)
                    {
                        errors.add(e.getMessage());
                    }
                }
            }
            output=output+"\n";
        }
        if(errors.size()!=0)
        {
            for(int i=0;i<errors.size();i++)                 //printing errors if any
            {
                System.out.println(errors.get(i));
            }
            output="";
        }
        return output;
    }

    public static void main(String[] args)
    {
        try
        {
            BufferedReader bufferedReader=new BufferedReader(new FileReader("inputAssemblyCode.txt"));
            ArrayList<String> errors=new ArrayList<>();
            ArrayList<String> errors1=new ArrayList<>();
            ArrayList<Symbol> symbolTable=new ArrayList<>();
            ArrayList<Literal> literalTable=new ArrayList<>();
            ArrayList<Opcode_output> opcode_output_table = new ArrayList<>();
            HashMap<String, String> opcodeTable=new HashMap<>();
            opcodeTable.put("CLA", "0000");
            opcodeTable.put("LAC", "0001");
            opcodeTable.put("SAC", "0010");
            opcodeTable.put("ADD", "0011");
            opcodeTable.put("SUB", "0100");
            opcodeTable.put("BRZ", "0101");
            opcodeTable.put("BRN", "0110");
            opcodeTable.put("BRP", "0111");
            opcodeTable.put("INP", "1000");
            opcodeTable.put("DSP", "1001");
            opcodeTable.put("MUL", "1010");
            opcodeTable.put("DIV", "1011");
            opcodeTable.put("STP", "1100");

            String passOneOutput=passOne(symbolTable,literalTable,opcodeTable,bufferedReader,opcode_output_table,errors);   //pass1
            String passTwoOutput=passTwo(symbolTable,literalTable,opcode_output_table,errors1);   //pass2
            if(!passOneOutput.equals("") && !passTwoOutput.equals(""))      //checking if there are no errors in pass1 and pass2
            {
                File machineCode=new File("machineCode.txt");
                FileWriter fileWriter=new FileWriter(machineCode);
                fileWriter.write(passOneOutput+passTwoOutput);            //write in file
                fileWriter.flush();
                fileWriter.close();
                System.out.println("Assembled successfully!!");
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
