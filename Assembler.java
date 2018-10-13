import java.io.*;
import java.util.*;

public class Assembler
{
    public static String convertToBinary(int num)   //Function to convert a number to 12 bit long binary form
    {
        String binary=Integer.toBinaryString(num);
        if(binary.length()!=12)     // If length is not 12, then add 0s to it.
        {
            int diff=12-binary.length();
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
                    int number_of_operands=1;
                    if(operands.contains(",")) {
                        number_of_operands = operands.split(",").length;
                    }
                    String comment="";
                    if(instruction.length==4)
                    {
                        comment=instruction[3].substring(2);
                    }

                    try {
                        if(opcode.equals("")) {    
                            throw new CustomException("Opcode not added" + " at line " + lineCounter);  // ERROR- if Opcode is not added
                        }
                    }
                    catch(CustomException e) {
                        errors.add(e.getMessage());
                    }
                   
                    {

                        if(!symbol.equals(""))           //checks if there is a symbol
                        {
                            try {
                                if(opcodeTable.containsKey(symbol))      
                                {
                                    throw new CustomException("Keyword "+symbol+" cannot be used at line "+lineCounter);    // ERROR- if an opcode is used as a symbol
                                }
                                else
                                {
                                    Symbol symbol1=new Symbol(symbol,locationCounter,null,"label");
                                    symbolTable.add(symbol1);
                                }
                            }
                            catch(CustomException e) {
                               errors.add(e.getMessage());
                            }   
                        }

                        
                        if(!opcode.equals(""))                 //checks if there is an opcode given   
                        {
                            try {
                                if(opcodeTable.containsKey(opcode))
                                {
                                    instructionLength=instructionLength+4;          //4 bits for opcode
                                    if(opcode.equals("STP"))
                                    {
                                        stopOccurred=true;
                                    }
                                }
                                else           
                                {
                                    throw new CustomException("Opcode "+opcode+" not found at line "+lineCounter);  //ERROR- invalid opcode
                                }
                            }
                            catch(CustomException e) {
                               errors.add(e.getMessage());
                            }
                        }


                        try {
                            if(operands.equals(" ")) {  //If there are no operands
                                if(!opcode.equals("CLA") && !opcode.equals("STP")) {
                                    throw new CustomException("Opcode supplied with insufficient operands at line " + lineCounter); //ERROR- insufficient operands
                                }
                            }
                        }
                        catch(CustomException e) {
                               errors.add(e.getMessage());
                        }
                        


                        if(!operands.equals(""))            //checks if there are operands given
                        {
                            try {
                                if(opcode.equals("CLA") || opcode.equals("STP") || number_of_operands>1) {
                                    throw new CustomException("Opcode supplied with too many operands at line " + lineCounter); //ERROR- Opcode supplied with too many operands
                                }
                            }
                            catch(CustomException e) {
                               errors.add(e.getMessage());
                            }
                            

                            instructionLength=instructionLength+8;      //8 bits for operands
                            if(operands.charAt(0)=='\'' && operands.charAt(opcode.length()-1)=='\'')
                            {
                                String value=operands.substring(1,opcode.length()-1);
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
                try {
                   String key=(String) iterator.next();
                    if(operandsDeclaration.get(key)>1) 
                    {
                        throw new CustomException("Multiple declarations of "+key);  // ERROR- Operand delcared multiple times
                    } 
                }
                catch(CustomException e) {
                    errors.add(e.getMessage());
                }
            }

            try {
                if(!stopOccurred) 
                {
                    throw new CustomException("STP statement missing");    // ERROR- STP statement not present
                }
            }
            catch(CustomException e) {
                errors.add(e.getMessage());
            }
            

            if(errors.size()==0)
            {
                File machineCode=new File("/Users/Sankalp Agrawal/Desktop/CAOS_assembler/machine.txt");
                FileWriter fileWriter=new FileWriter(machineCode);
                String output="Symbol_Table"+"\n";
                for(int i=0;i<symbolTable.size();i++)
                {
                    output=output+symbolTable.get(i).symbol+'\t'+convertToBinary(symbolTable.get(i).location)+'\t'+symbolTable.get(i).value+'\n';
                }

                output=output+'\n'+"Literal_Table"+"\n";
                for(int i=0;i<literalTable.size();i++)
                {
                    output=output+literalTable.get(i).value+'\t'+convertToBinary(literalTable.get(i).location)+'\n';
                }

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

    public static void main(String[] args)
    {
        try
        {
            BufferedReader bufferedReader=new BufferedReader(new FileReader("/Users/Sankalp Agrawal/Desktop/CAOS_assembler/assembly.txt"));
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
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
