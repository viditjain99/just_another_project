import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Assembler
{
    public static String convertToBinary(int num)
    {
        String revBinary="";
        String binary="";
        int temp=num;
        while(num/2!=0)
        {
            int remainder=num%2;
            num=num/2;
            revBinary=revBinary+remainder;
        }
        if(temp!=0)
        {
            revBinary=revBinary+"1";
        }
        for(int i=revBinary.length()-1;i>=0;i--)
        {
            binary=binary+revBinary.charAt(i);
        }
        if(binary.length()!=8)
        {
            int diff=8-binary.length();
            for(int i=0;i<diff;i++)
            {
                binary=String.valueOf(0)+binary;
            }
        }
        return binary;
    }

    public static void passOne(ArrayList<Symbol> symbolTable, HashMap<String, String> opcodeTable, BufferedReader bufferedReader,FileWriter fileWriter,HashMap<String,Integer> pseudoInstructionTable)
    {
        try
        {
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
            boolean stopOccured=false;

            String lineString=bufferedReader.readLine();    //read line from file
            int lineCounter=1;           //counts the lines
            int locationCounter=0;     //keeps track of the length of instructions in words (1 word=12 bits)

//            String output="";            //output that is written to the file
            while(lineString!=null)
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
//                output=output+convertToBinary(locationCounter)+'\t';  //memory address of an instruction
                if(!stopOccured)                     //if STP has not occurred
                {
                    if(!symbol.equals(""))           //checks if there is a symbol
                    {
                        Symbol symbol1=new Symbol(symbol,locationCounter,null);
                        symbolTable.add(symbol1);
                    }

                    if(!opcode.equals(""))                 //checks if there is an opcode given
                    {
                        instructionLength=instructionLength+4;       //4 bits for opcode
                        if(opcode.equals("STP"))
                        {
                            stopOccured=true;
                        }
                    }
                    if(!operands.equals(""))            //checks if there are operands given
                    {
                        instructionLength=instructionLength+8;            //8 bits for operands
                    }
                }
                else                //if STP has occurred
                {
                    Symbol symbol1=new Symbol(symbol,locationCounter,operands);
                    symbolTable.add(symbol1);
                    instructionLength=12;
                }
                lineCounter++;       //increase line number
                int numOfWords;
                if(instructionLength%12!= 0)    //number of words an instruction would occupy
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
            String output="";
            for(int i=0;i<symbolTable.size();i++)
            {
                output=output+symbolTable.get(i).symbol+'\t'+convertToBinary(symbolTable.get(i).location)+'\t'+symbolTable.get(i).value+'\n';
            }
            fileWriter.write(output);            //write in file
            fileWriter.flush();
            fileWriter.close();
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

    public static void main(String[] args)
    {
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("assembly.txt"));
            File machineCode = new File("machine.txt");
            FileWriter fileWriter = new FileWriter(machineCode);
            ArrayList<Symbol> symbolTable = new ArrayList<>();
            HashMap<String,Integer> pseudoInstructionTable=new HashMap<>();
            HashMap<String, String> opcodeTable = new HashMap<>();
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
            passOne(symbolTable,opcodeTable,bufferedReader,fileWriter,pseudoInstructionTable);
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
