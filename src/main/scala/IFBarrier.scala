package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class InstructionFetchBarrier extends MultiIOModule {

	val io = IO(
    new Bundle {
      val PC_In = Input(UInt())
      val instruction_In = Input(new Instruction)
      val notStall   = Input(UInt(1.W))

      val PC_Out = Output(UInt())
      val instruction_Out = Output(new Instruction)
    })

  val PC   = RegInit(0.U(32.W))

  when(io.notStall.asBool){
    PC := io.PC_In
  }

  io.instruction_Out := io.instruction_In
  io.PC_Out := PC

}
