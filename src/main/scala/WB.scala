package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule


class WriteBacK extends MultiIOModule {

  // Don't touch the test harness
  // val testHarness = IO(
  //   new Bundle {
  //     val registerSetup = Input(new RegisterSetupSignals)
  //     val registerPeek  = Output(UInt(32.W))

  //     val testUpdates   = Output(new RegisterUpdates)
  //   })


  val io = IO(
    new Bundle {
      /**
        * TODO: Your code here.
        */
      val controlSignals_In = Input(new ControlSignals)
      val aluResult_In = Input(UInt(32.W))

      val controlSignals_Out = Output(new ControlSignals)
      val aluResult_Out = Output(UInt(32.W))

    }
  )

  // val registers = Module(new Registers)
  // val decoder   = Module(new Decoder).io


  /**
    * Setup. You should not change this code
    */
  // registers.testHarness.setup := testHarness.registerSetup
  // testHarness.registerPeek    := registers.io.readData1
  // testHarness.testUpdates     := registers.testHarness.testUpdates


  io.controlSignals_Out := io.controlSignals_In
  io.aluResult_Out := io.aluResult_In
 
}
