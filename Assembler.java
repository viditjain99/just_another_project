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

    public static void passOne(ArrayList<Symbol> symbolTable,ArrayList<Literal> literalTable, HashMap<String, String> opcodeTable, BufferedReader bufferedReader,HashMap<String,Integer> pseudoInstructionTable,ArrayList<String> errors)
    {
        try
        {
            boolean stopOccurred=false;

            String lineString=bufferedReader.readLine();    //read line from file
            int lineCounter=1;           //counts the lines
            int locationCounter=0;     //keeps track of the length of instructions in words (1 word=12 bits)

            while(lineString!=null)
            {
                int instructionLength=0;
                if(!stopOccurred)       //if stop has not occurred
                {
                    String[] instruction=lineString.split("\t");
                    String symbol=instruction[0];
                    String opcode=instruction[1];
                    String operands=instruction[2];
                    String comment="";
                    if(instruction.length==4)
                    {
                        comment=instruction[3].substring(2);
                    }
                    {
                        if(!symbol.equals(""))           //checks if there is a symbol
                        {
                            if(opcodeTable.containsKey(symbol))      //if an opcode is used as a symbol
                            {
                                errors.add("Keyword "+symbol+" cannot be used at line "+lineCounter);
                            }
                            else
                            {
                                Symbol symbol1=new Symbol(symbol,locationCounter,null,"label");
                                symbolTable.add(symbol1);
                            }
                        }

                        if(!opcode.equals(""))                 //checks if there is an opcode given   //HANDLE CASE IF OPCODE IS NOT SUPPLIED
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
                                errors.add("Opcode "+opcode+" not found at line "+lineCounter);
                            }
                        }
                        if(!operands.equals(""))            //checks if there are operands given
                        {
                            instructionLength=instructionLength+8;      //8 bits for operands
                            if(operands.charAt(0)=='=')
                            {
                                String value=operands.substring(1);
                                try
                                {
                                    double doubleValue=Double.parseDouble(value);
                                    Literal literal=new Literal(doubleValue,locationCounter);
                                    literalTable.add(literal);
                                }
                                catch(NumberFormatException e)
                                {

                                }
                            }
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
                    instructionLength=12;   //TO BE CHANGED ACCORDING TO DS,DW,DB
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
                locationCounter=locationCounter+numOfWords;   //increasing locationCounter by number of words occupied
                lineString=bufferedReader.readLine();         //read next line
            }

            HashMap<String,Integer> operandsDeclaration=new HashMap<>();
            for(int i=0;i<symbolTable.size();i++)
            {
                if(symbolTable.get(i).type.equals("operand"))
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
            }
            Collection<String> collection=operandsDeclaration.keySet();
            Iterator iterator=collection.iterator();
            while(iterator.hasNext())
            {
                String key=(String) iterator.next();
                if(operandsDeclaration.get(key)>1)
                {
                    errors.add("Multiple declarations of "+key);
                }
            }
            if(!stopOccurred)
            {
                errors.add("STP statement missing");
            }

            if(errors.size()==0)
            {
                File machineCode=new File("/Users/vidit/Desktop/machine.txt");
                FileWriter fileWriter=new FileWriter(machineCode);
                String output="Symbol_Table"+'\n';
                for(int i=0;i<symbolTable.size();i++)
                {
                    output=output+symbolTable.get(i).symbol+'\t'+convertToBinary(symbolTable.get(i).location)+'\t'+symbolTable.get(i).value+'\n';
                }

                output=output+'\n'+"Literal_Table"+'\n';
                for(int i=0;i<literalTable.size();i++)
                {
                    output=output+literalTable.get(i).value+'\t'+convertToBinary(literalTable.get(i).location)+'\n';
                }

                output=output+"\n";
                fileWriter.write(output);            //write in file
                fileWriter.flush();
                fileWriter.close();
            }
            else
            {
                for(int i=0;i<errors.size();i++)
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
    }

    public static void passTwo(ArrayList<Symbol> symbolTable,ArrayList<Literal> literalTable,HashMap<String,String> opcodeTable,BufferedReader bufferedReader)
    {
        try
        {
            String lineString=bufferedReader.readLine();
            boolean stopOccurred=false;
            String output="Machine_Code"+"\n";
            int locationCounter=0;
            while(lineString!=null)
            {
                 if(!stopOccurred)
                 {
                     int instructionLength=0;
                     String[] instruction=lineString.split("\t");
                     String symbol=instruction[0];
                     String opcode=instruction[1];
                     String operands=instruction[2];
                     String comment="";
                     if(instruction.length==4)
                     {
                         comment=instruction[3].substring(2);
                     }
                     if(!opcode.equals("CLA") && !opcode.equals("STP"))
                     {
                         output=output+convertToBinary(locationCounter)+"\t"+opcodeTable.get(opcode)+"\t";
                         if(operands.charAt(0)=='=')
                         {
                             String value=operands.substring(1);
                             for(int i=0;i<literalTable.size();i++)
                             {
                                 if(Double.parseDouble(value)==literalTable.get(i).value)
                                 {
                                     output=output+convertToBinary(literalTable.get(i).location)+"\t";
                                 }
                             }
                         }
                         else
                         {
                             for(int i=0;i<symbolTable.size();i++)
                             {
                                 if(symbolTable.get(i).symbol.equals(operands))
                                 {
                                     output=output+convertToBinary(symbolTable.get(i).location)+"\t";
                                 }
                             }
                         }
                     }
                     else if(opcode.equals("CLA"))
                     {
                         output=output+convertToBinary(locationCounter)+"\t"+opcodeTable.get("CLA")+"\t"+operands+"\t";
                     }
                     else if(opcode.equals("STP"))
                     {
                         output=output+convertToBinary(locationCounter+1)+"\t"+opcodeTable.get("STP")+"\t"+operands+"\t";
                         stopOccurred=true;
                     }
                     output=output+"\n";
                 }
                 else
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
                     for(int i=0;i<symbolTable.size();i++)
                     {
                         if(symbolTable.get(i).symbol.equals(operand))
                         {
                             output=output+convertToBinary(symbolTable.get(i).location)+"\t"+operand+"\t"+symbolTable.get(i).value+"\n";
                         }
                     }
                 }
                 locationCounter++;
                 lineString=bufferedReader.readLine();
            }
            File machineCode=new File("/Users/vidit/Desktop/machine.txt");
            FileWriter fileWriter=new FileWriter(machineCode,true);
            fileWriter.write(output);
            fileWriter.flush();
            fileWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        try
        {
            BufferedReader bufferedReader=new BufferedReader(new FileReader("/Users/vidit/Desktop/assembly.txt"));
            ArrayList<String> errors=new ArrayList<>();
            ArrayList<Symbol> symbolTable=new ArrayList<>();
            ArrayList<Literal> literalTable=new ArrayList<>();
            HashMap<String,Integer> pseudoInstructionTable=new HashMap<>();
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

            passOne(symbolTable,literalTable,opcodeTable,bufferedReader,pseudoInstructionTable,errors);
            BufferedReader bufferedReader1=new BufferedReader(new FileReader("/Users/vidit/Desktop/assembly.txt"));
            passTwo(symbolTable,literalTable,opcodeTable,bufferedReader1);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
