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

    public static String passOne(ArrayList<Symbol> symbolTable,ArrayList<Literal> literalTable, HashMap<String, String> opcodeTable, BufferedReader bufferedReader,ArrayList<String> errors)
    {
        String output="";
        try
        {
            boolean stopOccurred=false;

            String lineString=bufferedReader.readLine();    //read line from file
            int lineCounter=1;           //counts the lines
            int locationCounter=0;     //keeps track of the length of instructions in words (1 word=12 bits)
            HashMap<String,Integer> opcode_output=new HashMap<>();


            while(lineString!=null)
            {
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
                        String opcode=instruction[1];
                        String operands="";
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

                            if(!opcode.equals(""))                 //checks if there is an opcode given
                            {
                                try
                                {
                                    if(opcodeTable.containsKey(opcode))
                                    {
                                        instructionLength=instructionLength+4;          //4 bits for opcode
                                        opcode_output.put(opcode+'\t'+opcodeTable.get(opcode),locationCounter);
                                        if(opcode.equals("STP"))
                                        {
                                            stopOccurred=true;
                                        }
                                    }
                                    else           //invalid opcode
                                    {
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
                                        throw new CustomException("Opcode supplied with too may operands at line "+lineCounter);
                                    }
                                }
                                catch(CustomException e)
                                {
                                    errors.add(e.getMessage());
                                }
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
                                        errors.add("Invalid literal at line "+lineCounter);
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
                literal.location=locationCounter;
            }

           
            HashMap<String,Integer> operandsDeclaration=new HashMap<>();
            for(int i=0;i<symbolTable.size();i++)
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
                    if(operandsDeclaration.get(key)>1)
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
                if(!stopOccurred)
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
                for(int i=0;i<symbolTable.size();i++)
                {
                    output=output+symbolTable.get(i).symbol+'\t'+symbolTable.get(i).type+'\t'+symbolTable.get(i).location+'\t'+symbolTable.get(i).value+'\n';
                }

                output=output+'\n'+'\n'+"Literal_Table"+'\n';
                for(int i=0;i<literalTable.size();i++)
                {
                    output=output+literalTable.get(i).value+'\t'+literalTable.get(i).location+'\n';
                }

                output=output+'\n'+'\n'+"Opcode_Table"+'\n';
                Set set=opcode_output.entrySet();
                Iterator iterator1=set.iterator();
                while(iterator1.hasNext())
                {
                    Map.Entry mentry=(Map.Entry) iterator1.next();
                    output=output+mentry.getKey()+'\t'+mentry.getValue()+'\n';
                }
                output=output+"\n"+'\n';
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
        return output;
    }

    public static String passTwo(ArrayList<Symbol> symbolTable,ArrayList<Literal> literalTable,HashMap<String,String> opcodeTable,BufferedReader bufferedReader,ArrayList<String> errors)
    {
        String output="";
        try
        {
            String lineString=bufferedReader.readLine();
            boolean stopOccurred=false;
            output="Machine_Code"+"\n";
            int locationCounter=0;
            while(lineString!=null)
            {
                if(lineString.equals(""))
                {
                    lineString=bufferedReader.readLine();
                }
                else
                {
                    if(!stopOccurred)
                    {
                        int instructionLength=0;
                        String[] instruction=lineString.split("\t");
                        String symbol=instruction[0];
                        String opcode=instruction[1];
                        String operands="";
                        if(instruction.length>2)
                        {
                            operands=instruction[2];
                        }
                        String comment="";
                        if(instruction.length==4)
                        {
                            comment=instruction[3].substring(2);
                        }
                        if(!opcode.equals("CLA") && !opcode.equals("STP"))
                        {
                            output=output+opcodeTable.get(opcode)+"\t";
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
                                boolean operandFound=false;
                                for(int i=0;i<symbolTable.size();i++)
                                {
                                    if(symbolTable.get(i).symbol.equals(operands))
                                    {
                                        output=output+convertToBinary(symbolTable.get(i).location)+"\t";
                                        operandFound=true;
                                    }
                                }
                                if(!operandFound)
                                {
                                    errors.add("Symbol "+operands+" not defined");
                                }
                            }
                        }
                        else if(opcode.equals("CLA"))
                        {
                            output=output+opcodeTable.get("CLA")+"\t";
                        }
                        else if(opcode.equals("STP"))
                        {
                            output=output+opcodeTable.get("STP")+"\t";
                            stopOccurred=true;
                        }
                        output=output+"\n";
                    }
                    locationCounter++;
                    lineString=bufferedReader.readLine();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if(errors.size()!=0)
        {
            for(int i=0;i<errors.size();i++)
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
            BufferedReader bufferedReader=new BufferedReader(new FileReader("/Users/vidit/Desktop/assembly.txt"));
            ArrayList<String> errors=new ArrayList<>();
            BufferedReader bufferedReader1=new BufferedReader(new FileReader("/Users/vidit/Desktop/assembly.txt"));
            ArrayList<String> errors1=new ArrayList<>();
            ArrayList<Symbol> symbolTable=new ArrayList<>();
            ArrayList<Literal> literalTable=new ArrayList<>();
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

            String passOneOutput=passOne(symbolTable,literalTable,opcodeTable,bufferedReader,errors);
            String passTwoOutput=passTwo(symbolTable,literalTable,opcodeTable,bufferedReader1,errors1);
            if(!passOneOutput.equals("") && !passTwoOutput.equals(""))
            {
                File machineCode=new File("/Users/vidit/Desktop/machine.txt");
                FileWriter fileWriter=new FileWriter(machineCode);
                fileWriter.write(passOneOutput+passTwoOutput);            //write in file
                fileWriter.flush();
                fileWriter.close();
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
