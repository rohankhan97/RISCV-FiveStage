package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule


class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    })


  val io = IO(
    new Bundle {
      /**
        * TODO: Your code here.
        */
      val instruction_In = Input(new Instruction)
      val PC_In = Input(UInt(32.W))
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io


  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  /**
    * TODO: Your code here.
    */
  decoder.instruction := io.instruction_In

  registers.io.readAddress1 := io.instruction_In.registerRs1
  registers.io.readAddress2 := io.instruction_In.registerRs2
  registers.io.writeAddress := io.instruction_In.registerRd

  // registers.io.readAddress1 := 0.U
  // registers.io.readAddress2 := 0.U
  registers.io.writeEnable  := false.B
  // registers.io.writeAddress := 0.U
  registers.io.writeData    := 0.U

  // decoder.instruction := 0.U.asTypeOf(new Instruction)
}
