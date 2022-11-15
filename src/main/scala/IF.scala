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
      // val notStall     = Input(UInt(1.W))         // Not stall indication from ID

      val PC           = Output(UInt())
      val instruction  = Output(new Instruction)
    })

  val IMEM = Module(new IMEM)
  val PC   = RegInit(UInt(32.W), 0.U)


  /**
    * Setup. You should not change this code
    */
  IMEM.testHarness.setupSignals := testHarness.IMEMsetup
  testHarness.PC := IMEM.testHarness.requestedAddress


  io.PC := PC

  // when(io.notStall.asBool){         // Update the PC only when Pipeline is not stalled becasue of load instruction
    when(io.branchResult.asBool){   
      PC := io.adderIn              // Jump the PC to branch address
    }.otherwise{
      PC := PC + 4.U                // Increment the PC to next instruction
    }
  // }.otherwise{
    // PC := PC                        // Do not update the PC when Pipeline is stalled
  // }


  val instruction = Wire(new Instruction)
  instruction := IMEM.io.instruction.asTypeOf(new Instruction)
  
  IMEM.io.instructionAddress := PC
  io.instruction := instruction

  /**
    * Setup. You should not change this code.
    */
  when(testHarness.IMEMsetup.setup) {
    PC := 0.U
    instruction := Instruction.NOP
  }
}
