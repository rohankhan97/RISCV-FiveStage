package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class MemoryBarrier extends MultiIOModule {

	val io = IO(
    new Bundle {
      val controlSignals_In = Input(new ControlSignals)
      val aluResult_In      = Input(UInt(32.W))
      val dataOut_In        = Input(UInt(32.W))
      val rdAddress_In      = Input(UInt(5.W))

      val controlSignals_Out = Output(new ControlSignals)
      val aluResult_Out      = Output(UInt(32.W))
      val dataOut_Out        = Output(UInt(32.W))
      val rdAddress_Out      = Output(UInt(5.W))
    })

  val controlSignals   = RegInit(0.U(6.W))
  val aluResult        = RegInit(0.U(32.W))
  val rdAddress        = RegInit(0.U(5.W))

  controlSignals  := io.controlSignals_In.asUInt
  aluResult       := io.aluResult_In
  rdAddress       := io.rdAddress_In

  io.controlSignals_Out  := controlSignals.asTypeOf(new ControlSignals)
  io.dataOut_Out         := io.dataOut_In
  io.aluResult_Out       := aluResult
  io.rdAddress_Out       := rdAddress     
}
