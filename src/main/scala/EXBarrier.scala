package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class ExecuteBarrier extends MultiIOModule {

	val io = IO(
    new Bundle {
      val controlSignals_In = Input(new ControlSignals)
      val dataIn_In         = Input(UInt(32.W))
      val dataAddress_In    = Input(UInt(12.W))

      val controlSignals_Out = Output(new ControlSignals)
      val dataIn_Out         = Output(UInt(32.W))
      val dataAddress_Out    = Output(UInt(12.W))
    })

  val controlSignals   = RegInit(0.U(5.W))
  val dataIn           = RegInit(0.U(32.W))
  val dataAddress      = RegInit(0.U(32.W))


  controlSignals  := io.controlSignals_In
  dataIn          := io.dataIn_In
  dataAddress     := io.dataAddress_In

  io.controlSignals_Out  := controlSignals
  io.dataIn_Out          := dataIn     
  io.dataAddress_Out     := dataAddress     

}
