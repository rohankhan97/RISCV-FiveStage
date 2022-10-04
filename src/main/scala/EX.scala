package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup }
import chisel3.experimental.MultiIOModule


class Execute extends MultiIOModule {

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

      val readData1 = Input(UInt(32.W))
      val readData2 = Input(UInt(32.W))
      val immediate = Input(UInt(32.W))
      val op1Select = Input(UInt(1.W))
      val op2Select = Input(UInt(1.W))
      val aluOp = Input(UInt(4.W))
      val aluResult = Output(UInt(32.W))

      val controlSignals_Out = Output(new ControlSignals)

    }
  )

  val op1 = Wire(UInt(32.W))
  val op2 = Wire(UInt(32.W))

  op1 := io.readData1

  val op2Map = Array(
    Op2Select.rs2      -> readData2,
    Op2Select.imm      -> immediate
    )

  op2 := MuxLookup(io.op2Select, 0.U(32.W), op2Map)

  // when(io.op2Select){
  //   op2 := io.immediate
  // }.otherwise{
  //   op2 := io.readData2
  // }

  val ALUOpMap = Array(
    ALUOps.ADD      -> (op1 + op2),
    ALUOps.SUB      -> (op1 - op2),
    ALUOps.AND      -> (op1 & op2),
    ALUOps.OR       -> (op1 | op2),
    ALUOps.XOR      -> (op1 ^ op2),
    ALUOps.XOR      -> (op1 ^ op2),
    ALUOps.SLT      -> (op1 < op2),
    // ALUOps.SLL      -> (io.op1 << io.op2),
    ALUOps.SLTU     -> (op1 < op2)
    // ALUOps.SRL      -> (io.op1 >> io.op2)
    // ALUOps.SRA      -> (io.op1 >>> io.op2)
    )

  // val registers = Module(new Registers)
  // val decoder   = Module(new Decoder).io


  /**
    * Setup. You should not change this code
    */
  // registers.testHarness.setup := testHarness.registerSetup
  // testHarness.registerPeek    := registers.io.readData1
  // testHarness.testUpdates     := registers.testHarness.testUpdates

  io.aluResult := MuxLookup(io.aluOp, 0.U(32.W), ALUOpMap)

  io.controlSignals_Out := io.controlSignals_In
 
}
