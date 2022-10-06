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
                val immType        = Output(UInt(3.W))
                val ALUop          = Output(UInt(4.W))
              })

  import lookup._
  import Op1Select._
  import Op2Select._
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

    // signal     mem2reg regWrite, memRead, memWrite, branch,  jump, branchType,    Op1Select, Op2Select, ImmSelect,    ALUOp
    LW     -> List(Y,     Y,        Y,       N,        N,       N,    branchType.DC, rs1,       imm,       ITYPE,        ALUOps.ADD),
    LUI    -> List(Y,     Y,        Y,       N,        N,       N,    branchType.DC, Op1Select.DC,       imm,       UTYPE,        ALUOps.ADD),
    AUIPC  -> List(Y,     Y,        Y,       N,        N,       N,    branchType.DC, PC,        imm,       UTYPE,        ALUOps.ADD),

    SW     -> List(N,     N,        N,       Y,        N,       N,    branchType.DC, rs1,       imm,       STYPE,        ALUOps.ADD),
    JAL    -> List(N,     Y,        N,       N,        N,       Y,    jump,          PC,        imm,       JTYPE,        ALUOps.ADD),
    JALR   -> List(N,     Y,        N,       N,        N,       Y,    jump,          rs1,       imm,       ITYPE,        ALUOps.ADD),

    BEQ    -> List(N,     N,        N,       N,        Y,       N,    beq,           rs1,       rs2,       BTYPE,        ALUOps.SUB),
    BNE    -> List(N,     N,        N,       N,        Y,       N,    neq,           rs1,       rs2,       BTYPE,        ALUOps.SUB),
    BLT    -> List(N,     N,        N,       N,        Y,       N,    lt,            rs1,       rs2,       BTYPE,        ALUOps.SLT),
    BGE    -> List(N,     N,        N,       N,        Y,       N,    gte,           rs1,       rs2,       BTYPE,        ALUOps.SLT), //
    BLTU   -> List(N,     N,        N,       N,        Y,       N,    ltu,           rs1,       rs2,       BTYPE,        ALUOps.SLTU),
    BGEU   -> List(N,     N,        N,       N,        Y,       N,    gteu,          rs1,       rs2,       BTYPE,        ALUOps.SLT), //

    ADD    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.ADD),
    ADDI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.ADD),
    SUB    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SUB),

    AND    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.AND),
    ANDI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.AND),
    OR     -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.OR),
    ORI    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.OR),
    XOR    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.XOR),
    XORI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.XOR),

    SLT    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SLT),
    SLTI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SLT),
    SLTU   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SLTU),
    SLTIU  -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SLTU),
    SRA    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SRA),
    SRAI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SRA),
    SRL    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SRL),
    SRLI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SRL),
    SLL    -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       rs2,       ImmFormat.DC, ALUOps.SLL),
    SLLI   -> List(N,     Y,        N,       N,        N,       N,    branchType.DC, rs1,       imm,       ImmFormat.DC, ALUOps.SLL),
            
        

    /**
      TODO: Fill in the blanks
      */
    )


  val NOP = List(N, N, N, N, N, N, branchType.DC, rs1, rs2, ImmFormat.DC, ALUOps.DC)

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
}
