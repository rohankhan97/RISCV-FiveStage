package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase }
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

      val op1 = Input(UInt(32.W))
      val op2 = Input(UInt(32.W))
      val aluOp = Input(UInt(4.W))
      val aluResult = Output(UInt(32.W))

      val controlSignals_Out = Output(new ControlSignals)

    }
  )

  val ALUOpMap = Array(
    ADD      -> (io.op1 + io.op2),
    SUB      -> (io.op1 - io.op2),
    AND      -> (io.op1 & io.op2),
    OR       -> (io.op1 | io.op2),
    XOR      -> (io.op1 ^ io.op2),
    XOR      -> (io.op1 ^ io.op2),
    SLT      -> (io.op1 < io.op2),
    SLL      -> (io.op1 << io.op2),
    SLTU     -> (io.op1 < io.op2),
    SRL      -> (io.op1 >> io.op2),
    SRA      -> (io.op1 >>> io.op2)
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
