class Opcode_output
{
    String opcode;
    int ilc;
    String operand;
    String binary;
    boolean errorOccured;

    public Opcode_output(String opcode, int ilc,String operand,String binary,boolean errorOccured)
    {
        this.opcode = opcode;
        this.ilc = ilc;
        this.operand=operand;
        this.binary=binary;
        this.errorOccured=errorOccured;
    }

    @Override
    public String toString() {
        return this.opcode + '\t' + this.binary + '\t' + this.ilc + '\t' + this.operand + '\n';
    }
}
