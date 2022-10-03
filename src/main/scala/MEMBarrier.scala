package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class MemoryBarrier extends MultiIOModule {

	val io = IO(
    new Bundle {
      val controlSignals_In = Input(new ControlSignals)
      val aluResult_In = Input(UInt(32.W))
      val dataOut_In = Input(UInt(32.W))

      val controlSignals_Out = Output(new ControlSignals)
      val aluResult_Out = Output(UInt(32.W))
      val dataOut_Out = Output(UInt(32.W))
    })

  // val controlSignals   = RegInit(0.U(5.W))
  val controlSignals   = Wire(new ControlSignals)
  val aluResult   = RegInit(0.U(32.W))

  controlSignals  := io.controlSignals_In
  aluResult := io.aluResult_In

  io.controlSignals_Out  := controlSignals
  io.dataOut_Out := io.dataOut_In
  io.aluResult_Out := aluResult

}
