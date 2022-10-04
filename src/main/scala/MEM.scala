package FiveStage
import chisel3._
import chisel3.util._
import chisel3.experimental.MultiIOModule


class MemoryFetch() extends MultiIOModule {


  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val DMEMsetup      = Input(new DMEMsetupSignals)
      val DMEMpeek       = Output(UInt(32.W))

      val testUpdates    = Output(new MemUpdates)
    })

  val io = IO(
    new Bundle {
      val controlSignals_In = Input(new ControlSignals)
      val dataIn            = Input(UInt(32.W))
      val dataAddress       = Input(SInt(32.W))

      val controlSignals_Out = Output(new ControlSignals)
      val dataOut            = Output(UInt(32.W))
    })


  val DMEM = Module(new DMEM)


  /**
    * Setup. You should not change this code
    */
  DMEM.testHarness.setup  := testHarness.DMEMsetup
  testHarness.DMEMpeek    := DMEM.io.dataOut
  testHarness.testUpdates := DMEM.testHarness.testUpdates


  /**
    * Your code here.
    */
  // DMEM.io.dataIn      := 0.U
  // DMEM.io.dataAddress := 0.U
  // DMEM.io.writeEnable := false.B

  DMEM.io.dataIn      := io.dataIn     
  DMEM.io.dataAddress := io.dataAddress(11,0)
  DMEM.io.writeEnable := io.controlSignals_In.memWrite

  io.controlSignals_Out := io.controlSignals_In
  io.dataOut            := DMEM.io.dataOut
}
