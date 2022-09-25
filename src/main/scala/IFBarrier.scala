package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class IFBarrier extends MultiIOModule {

	val io = IO(
    new Bundle {
      val PC_In = Input(UInt())
      val instruction_In = Input(new Instruction)

      val PC_Out = Output(UInt())
      val instruction_Out = Output(new Instruction)
    })

  val PC   = RegInit(0.U(32.W))

  io.PC_In := PC

  io.instruction_Out := io.instruction_In
  io.PC_Out := PC

}
