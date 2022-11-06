package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup }
import chisel3.experimental.MultiIOModule

class InstructionFetch extends MultiIOModule {

  // Don't touch
  val testHarness = IO(
    new Bundle {
      val IMEMsetup = Input(new IMEMsetupSignals)
      val PC        = Output(UInt())
    }
  )


  /**
    * TODO: Add input signals for handling events such as jumps

    * TODO: Add output signal for the instruction. 
    * The instruction is of type Bundle, which means that you must
    * use the same syntax used in the testHarness for IMEM setup signals
    * further up.
    */
  val io = IO(
    new Bundle {
      val adderIn      = Input(UInt(32.W))
      val branchResult = Input(UInt(1.W))
      // val notStall     = Input(UInt(1.W))
      val insertNOP    = Input(UInt(1.W))

      val PC          = Output(UInt())
      val instruction = Output(new Instruction)
    })

  val IMEM = Module(new IMEM)
  val PC   = RegInit(UInt(32.W), 0.U)

  val NOP = RegInit(UInt(1.W), 0.U)

  NOP := io.insertNOP


  /**
    * Setup. You should not change this code
    */
  IMEM.testHarness.setupSignals := testHarness.IMEMsetup
  testHarness.PC := IMEM.testHarness.requestedAddress


  /**
    * TODO: Your code here.
    * 
    * You should expand on or rewrite the code below.
    */
  io.PC := PC

  // when(io.insertNOP.asBool){
  //   when(io.branchResult.asBool){
  //     PC := io.adderIn
  //   }.otherwise{
  //     PC := PC + 4.U
  //   }
  // }

  when(NOP.asBool){
    when(io.branchResult.asBool){
      PC := io.adderIn
    }.otherwise{
      PC := PC + 4.U
    }
  }.otherwise{
    PC := PC
  }

  // PC := PC + 4.U

  val instruction = Wire(new Instruction)
  // instruction := IMEM.io.instruction.asTypeOf(new Instruction)
  
  when(NOP.asBool){
    IMEM.io.instructionAddress := PC
    instruction := IMEM.io.instruction.asTypeOf(new Instruction)
  }.otherwise{
    instruction := Instruction.NOP
  }

  // val instMap = Array(
  //   0.U(1.W)      -> IMEM.io.instruction.asTypeOf(new Instruction),
  //   1.U(1.W)      -> Instruction.NOP
  //  )
  // instruction := MuxLookup(io.insertNOP, Instruction.NOP, instMap)

  // io.instruction := IMEM.io.instruction.asTypeOf(new Instruction)
  io.instruction := instruction

  /**
    * Setup. You should not change this code.
    */
  when(testHarness.IMEMsetup.setup) {
    PC := 0.U
    instruction := Instruction.NOP
  }
}
