package FiveStage
import chisel3._
import chisel3.util.BitPat
import chisel3.util.ListLookup


/**
  * This module is mostly done, but you will have to fill in the blanks in opcodeMap.
  * You may want to add more signals to be decoded in this module depending on your
  * design if you so desire.
  *
  * In the "classic" 5 stage decoder signals such as op1select and immType
  * are not included, however I have added them to my design, and similarily you might
  * find it useful to add more
 */
class Decoder() extends Module {

  val io = IO(new Bundle {
                val instruction    = Input(new Instruction)

                val controlSignals = Output(new ControlSignals)
                val branchType     = Output(UInt(3.W))
                val op1Select      = Output(UInt(1.W))
                val op2Select      = Output(UInt(1.W))
                val PcOpSelect     = Output(UInt(1.W))
                val immType        = Output(UInt(3.W))
                val ALUop          = Output(UInt(4.W))
              })

  import lookup._
  import Op1Select._
  import Op2Select._
  import PcOpSelect._
  import branchType._
  import ImmFormat._

  val N = 0.asUInt(1.W)
  val Y = 1.asUInt(1.W)

  /**
    * In scala we sometimes (ab)use the `->` operator to create tuples.
    * The reason for this is that it serves as convenient sugar to make maps.
    *
    * This doesn't matter to you, just fill in the blanks in the style currently
    * used, I just want to demystify some of the scala magic.
    *
    * `a -> b` == `(a, b)` == `Tuple2(a, b)`
    */
  val opcodeMap: Array[(BitPat, List[UInt])] = Array(

    // signal     mem2reg regWrite, memRead, memWrite, branch,  jump, branchType,    Op1Select, Op2Select, ImmSelect,    ALUOp,     PcOpSelect,
    LW     -> List(Y,     Y,        Y,       N,        N,       N,    branchType.DC, rs1,       imm,       ITYPE,        ALUOps.ADD, PcOpSelect.DC),
    LI     -> List(N,     Y,        Y,       N,        N,       N,    branchType.DC, rs1,       imm,       ITYPE,        ALUOps.ADD, PcOpSelect.DC),
    LUI    -> List(Y,     Y,        Y,       N,        N,       N,    branchType.DC, Op1Select.DC,       imm,       UTYPE,        ALUOps.ADD, PcOpSelect.DC),
    AUIPC  -> List(Y,     Y,        Y,       N,        N,       N,    branchType.DC, PC,        imm,       UTYPE,        ALUOps.ADD, PcOpSelect.DC),

    SW     -> List(N,     N,        N,       Y,        N,       N,    branchType.DC, rs1,       imm,       STYPE,        ALUOps.ADD, PcOpSelect.DC),
    JAL    -> List(N,     Y,        N,       N,        N,       Y,    jump,          PC,        imm,       JTYPE,        ALUOps.JAL,  PC),
    JALR   -> List(N,     Y,        N,       N,        N,       Y,    jump,          rs1,       imm,       ITYPE,        ALUOps.ADD,  rs1),
    J      -> List(N,     N,        N,       N,        N,       Y,    jump,          PC,        imm,       JTYPE,        ALUOps.DC,  PC),
    JR     -> List(N,     N,        N,       N,        N,       Y,    jump,          rs1,       imm,       ITYPE,        ALUOps.ADD,  rs1),

    BEQ    -> List(N,     N,        N,       N,        Y,       N,    beq,           rs1,       rs2,       BTYPE,        ALUOps.SUB, PcOpSelect.DC),
    BNE    -> List(N,     N,        N,       N,        Y,       N,    neq,           rs1,       rs2,       BTYPE,        ALUOps.SUB, PcOpSelect.DC),
    BLT    -> List(N,     N,        N,       N,        Y,       N,    lt,            rs1,       rs2,       BTYPE,        ALUOps.SLT, PcOpSelect.DC),
    BGE    -> List(N,     N,        N,       N,        Y,       N,    gte,           rs1,       rs2,       BTYPE,        ALUOps.GTE, PcOpSelect.DC), //
    BLTU   -> List(N,     N,        N,       N,        Y,       N,    ltu,           rs1,       rs2,       BTYPE,        ALUOps.SLTU, PcOpSelect.DC),
    BGEU   -> List(N,     N,        N,       N,        Y,       N,    gteu,          rs1,       rs2,       BTYPE,        ALUOps.SLT, PcOpSelect.DC), //
    BLEZ   -> List(N,     N,        N,       N,        Y,       N,    lte,           rs1,       DC,       BTYPE,        ALUOps.LEZ, PcOpSelect.DC), //
    
    MV     -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       DC,       ImmFormat.DC, ALUOps.NEZ, PcOpSelect.DC), //

    ADD    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.ADD, PcOpSelect.DC),
    ADDI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.ADD, PcOpSelect.DC),
    SUB    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SUB, PcOpSelect.DC),

    AND    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.AND, PcOpSelect.DC),
    ANDI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.AND, PcOpSelect.DC),
    OR     -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.OR, PcOpSelect.DC),
    ORI    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.OR, PcOpSelect.DC),
    XOR    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.XOR, PcOpSelect.DC),
    XORI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.XOR, PcOpSelect.DC),

    SLT    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SLT, PcOpSelect.DC),
    SLTI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SLT, PcOpSelect.DC),
    SLTU   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SLTU, PcOpSelect.DC),
    SLTIU  -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SLTU, PcOpSelect.DC),
    SRA    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SRA, PcOpSelect.DC),
    SRAI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SRA, PcOpSelect.DC),
    SRL    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SRL, PcOpSelect.DC),
    SRLI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SRL, PcOpSelect.DC),
    SLL    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SLL, PcOpSelect.DC),
    SLLI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SLL, PcOpSelect.DC),
    SNEZ   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       DC,       ImmFormat.DC, ALUOps.NEZ, PcOpSelect.DC), //
            
        

    /**
      TODO: Fill in the blanks
      */
    )


  val NOP = List(N, N, N, N, N, N, branchType.DC, rs1, rs2, ImmFormat.DC, ALUOps.DC, PcOpSelect.DC)

  val decodedControlSignals = ListLookup(
    io.instruction.asUInt(),
    NOP,
    opcodeMap)

  io.controlSignals.memtoReg   := decodedControlSignals(0)
  io.controlSignals.regWrite   := decodedControlSignals(1)
  io.controlSignals.memRead    := decodedControlSignals(2)
  io.controlSignals.memWrite   := decodedControlSignals(3)
  io.controlSignals.branch     := decodedControlSignals(4)
  io.controlSignals.jump       := decodedControlSignals(5)

  io.branchType := decodedControlSignals(6)
  io.op1Select  := decodedControlSignals(7)
  io.op2Select  := decodedControlSignals(8)
  io.immType    := decodedControlSignals(9)
  io.ALUop      := decodedControlSignals(10)
  io.PcOpSelect := decodedControlSignals(11)
}
