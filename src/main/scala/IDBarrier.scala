package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class InstructionDecodeBarrier extends MultiIOModule {

	val io = IO(
    new Bundle {
      val controlSignals_In = Input(new ControlSignals)
      val branchType_In     = Input(UInt(3.W))
      val op1Select_In      = Input(UInt(1.W))
      val op2Select_In      = Input(UInt(1.W))
      val immType_In        = Input(UInt(3.W))
      val ALUop_In          = Input(UInt(4.W))

      val readData1_In    = Input(UInt(32.W))
      val readData2_In    = Input(UInt(32.W))
      val immediate_In    = Input(SInt(32.W))

      val controlSignals_Out = Output(new ControlSignals)
      val branchType_Out     = Output(UInt(3.W))
      val op1Select_Out      = Output(UInt(1.W))
      val op2Select_Out      = Output(UInt(1.W))
      val immType_Out        = Output(UInt(3.W))
      val ALUop_Out          = Output(UInt(4.W))

      val readData1_Out    = Output(UInt(32.W))
      val readData2_Out    = Output(UInt(32.W))
      val immediate_Out    = Output(SInt(32.W))
    })

  // val controlSignals   = RegInit(0.U(5.W)) 
  val controlSignals   = Wire(new ControlSignals)
  val branchType       = RegInit(0.U(3.W))
  val op1Select        = RegInit(0.U(1.W))
  val op2Select        = RegInit(0.U(1.W))
  val immType          = RegInit(0.U(3.W))
  val ALUop            = RegInit(0.U(4.W))
  val readData1        = RegInit(0.U(32.W))
  val readData2        = RegInit(0.U(32.W))
  val immediate        = RegInit(0.S(32.W))


  controlSignals  := io.controlSignals_In
  branchType      := io.branchType_In
  op1Select       := io.op1Select_In
  op2Select       := io.op2Select_In
  immType         := io.immType_In
  ALUop           := io.ALUop_In

  readData1       := io.readData1_In
  readData2       := io.readData2_In
  immediate       := io.immediate_In

  io.controlSignals_Out  := controlSignals
  io.branchType_Out      := branchType    
  io.op1Select_Out       := op1Select     
  io.op2Select_Out       := op2Select     
  io.immType_Out         := immType       
  io.ALUop_Out           := ALUop  

  io.readData1_Out       := readData1     
  io.readData2_Out       := readData2     
  io.immediate_Out       := immediate     

}
