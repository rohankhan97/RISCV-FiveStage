package FiveStage
import chisel3._
import chisel3.core.Wire
import chisel3.util.{ BitPat, Cat }


class Instruction extends Bundle(){
  val instruction = UInt(32.W)

  def opcode      = instruction(6, 0)
  def registerRd  = instruction(11, 7)
  def funct3      = instruction(14, 12)
  def registerRs1 = instruction(19, 15)
  def registerRs2 = instruction(24, 20)
  def funct7      = instruction(31, 25)
  def funct6      = instruction(26, 31)

  def immediateIType = instruction(31, 20).asSInt
  def immediateSType = Cat(instruction(31, 25), instruction(11,7)).asSInt
  def immediateBType = Cat(instruction(31), instruction(7), instruction(30, 25), instruction(11, 8), 0.U(1.W)).asSInt
  def immediateUType = Cat(instruction(31, 12), 0.U(12.W)).asSInt
  def immediateJType = Cat(instruction(31), instruction(19, 12), instruction(20), instruction(30, 25), instruction(24, 21), 0.U(1.W)).asSInt
  def immediateZType = instruction(19, 15).zext

  def bubble(): Instruction = {
    val bubbled = Wire(new Instruction)
    bubbled.instruction := instruction
    bubbled.instruction(6, 0) := BitPat.bitPatToUInt(BitPat("b0010011"))
    bubbled
  }
}
object Instruction {
  def NOP: Instruction = {
    val w = Wire(new Instruction)
    w.instruction := BitPat.bitPatToUInt(BitPat("b00000000000000000000000000010011"))
    w
  }
}


class ControlSignals extends Bundle(){
  val memtoReg   = Bool()
  val regWrite   = Bool()
  val memRead    = Bool()
  val memWrite   = Bool()
  val branch     = Bool()
  val jump       = Bool()
}


object ControlSignals {
  def nop: ControlSignals = {
    val b = Wire(new ControlSignals)
    b.memtoReg   := false.B
    b.regWrite   := false.B
    b.memRead    := false.B
    b.memWrite   := false.B
    b.branch     := false.B
    b.jump       := false.B
    b
  }
}


object branchType {
  val beq  =  0.asUInt(4.W)
  val neq  =  1.asUInt(4.W)
  val gt   =  2.asUInt(4.W)
  val gte  =  3.asUInt(4.W)
  val lt   =  4.asUInt(4.W)
  val lte  =  5.asUInt(4.W)
  val gtu  =  6.asUInt(4.W)
  val gteu =  7.asUInt(4.W)
  val ltu  =  8.asUInt(4.W)
  val lteu =  9.asUInt(4.W)
  val jump = 10.asUInt(4.W)
  val DC   = 11.asUInt(4.W)
}


/**
  these take the role of the alu source signal.
  Used in the decoder.
  In the solution manual I use these to select signals at the decode stage.
  You can choose to instead do this in the execute stage, and you may forego
  using them altogether.
  */
object Op1Select {
  val rs1 = 0.asUInt(1.W)
  val PC  = 1.asUInt(1.W)
  val DC  = 0.asUInt(1.W)
}

object Op2Select {
  val rs2 = 0.asUInt(1.W)
  val imm = 1.asUInt(1.W)
  val DC  = 0.asUInt(1.W)
}

object PcOpSelect {
  val rs1 = 0.asUInt(1.W)
  val PC  = 1.asUInt(1.W)
  val DC  = 0.asUInt(1.W)
}


/**
  Used in the decoder
  */
object ImmFormat {
  val ITYPE  = 0.asUInt(3.W)
  val STYPE  = 1.asUInt(3.W)
  val BTYPE  = 2.asUInt(3.W)
  val UTYPE  = 3.asUInt(3.W)
  val JTYPE  = 4.asUInt(3.W)
  val SHAMT  = 5.asUInt(3.W)
  val DC     = 6.asUInt(3.W)
}


object ALUOps {
  val ADD    =  0.U(5.W)
  val SUB    =  1.U(5.W)
  val AND    =  2.U(5.W)
  val OR     =  3.U(5.W)
  val XOR    =  4.U(5.W)
  val SLT    =  5.U(5.W)
  val SLL    =  6.U(5.W)
  val SLTU   =  7.U(5.W)
  val SRL    =  8.U(5.W)
  val SRA    =  9.U(5.W)
  val COPY_A = 10.U(5.W)
  val COPY_B = 11.U(5.W)
  val LTE    = 12.U(5.W)  // less than equal to
  val LTEU   = 13.U(5.W)  // less than equal to (unsigned)
  val LEZ    = 14.U(5.W)  // less than equal to 0
  val GEZ    = 15.U(5.W)  // greater than equal to 0
  val JAL    = 16.U(5.W)  // 
  val GTE    = 17.U(5.W)  // greater than equal to
  val NEZ    = 18.U(5.W)  // not equal to 0
  val GTEU   = 19.U(5.W)  // greater than equal to (unsigned)

  val DC     = 20.U(5.W)
}
