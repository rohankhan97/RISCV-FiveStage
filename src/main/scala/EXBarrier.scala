package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class ExecuteBarrier extends MultiIOModule {

	val io = IO(
    new Bundle {
      val controlSignals_In = Input(new ControlSignals)
      val dataIn_In         = Input(UInt(32.W))
      val dataAddress_In    = Input(UInt(32.W))
      val rdAddress_In      = Input(UInt(5.W))
      val adderOut_In       = Input(UInt(32.W))
      val branchResult_In   = Input(UInt(1.W))

      // val notStall          = Input(UInt(1.W))

      val controlSignals_Out = Output(new ControlSignals)
      val dataIn_Out         = Output(UInt(32.W))
      val dataAddress_Out    = Output(UInt(32.W))
      val rdAddress_Out      = Output(UInt(5.W))
      val adderOut_Out       = Output(UInt(32.W))
      val branchResult_Out   = Output(UInt(1.W))
    })

  val controlSignals   = RegInit(0.U(6.W))
  val dataIn           = RegInit(0.U(32.W))
  val dataAddress      = RegInit(0.U(32.W))
  val rdAddress        = RegInit(0.U(5.W))
  val adderOut         = RegInit(0.U(32.W))
  val branchResult     = RegInit(0.U(1.W))

  // when(io.notStall.asBool){
    controlSignals  := io.controlSignals_In.asUInt
    dataIn          := io.dataIn_In
    dataAddress     := io.dataAddress_In
    rdAddress       := io.rdAddress_In
    adderOut        := io.adderOut_In
    branchResult    := io.branchResult_In
  // }

  io.controlSignals_Out  := controlSignals.asTypeOf(new ControlSignals)
  io.dataIn_Out          := dataIn     
  io.dataAddress_Out     := dataAddress     
  io.rdAddress_Out       := rdAddress     
  io.adderOut_Out        := adderOut     
  io.branchResult_Out    := branchResult     

}
