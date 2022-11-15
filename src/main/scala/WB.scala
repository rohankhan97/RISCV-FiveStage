package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
import chisel3.experimental.MultiIOModule


class WriteBack extends MultiIOModule {

  val io = IO(
    new Bundle {
      /**
        * TODO: Your code here.
        */
      val controlSignals_In = Input(new ControlSignals)
      val aluResult_In      = Input(UInt(32.W))
      val dataOut_In        = Input(UInt(32.W))
      val rdAddress_In      = Input(UInt(5.W))

      val controlSignals_Out = Output(new ControlSignals)
      val rdData_Out         = Output(UInt(32.W))
      val rdAddress_Out      = Output(UInt(5.W))

    }
  )

   when(io.controlSignals_In.memtoReg){
    io.rdData_Out := io.dataOut_In
  }.otherwise{
    io.rdData_Out := io.aluResult_In
  }

  io.controlSignals_Out := io.controlSignals_In
  io.rdAddress_Out      := io.rdAddress_In
 
}
