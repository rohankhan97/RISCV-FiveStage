package FiveStage
import chisel3._
import chisel3.util.{ BitPat, MuxCase, MuxLookup }
import chisel3.experimental.MultiIOModule


class InstructionDecode extends MultiIOModule {

  // Don't touch the test harness
  val testHarness = IO(
    new Bundle {
      val registerSetup = Input(new RegisterSetupSignals)
      val registerPeek  = Output(UInt(32.W))

      val testUpdates   = Output(new RegisterUpdates)
    })


  val io = IO(
    new Bundle {
      /**
        * TODO: Your code here.
        */
      val instruction_In = Input(new Instruction)
      val PC_In = Input(UInt(32.W))

      val controlSignals2 = Input(new ControlSignals)
      val writeData = Input(UInt(32.W))

      val controlSignals = Output(new ControlSignals)
      val branchType     = Output(UInt(3.W))
      val op1Select      = Output(UInt(1.W))
      val op2Select      = Output(UInt(1.W))
      val immType        = Output(UInt(3.W))
      val ALUop          = Output(UInt(4.W))

      val readData1    = Output(UInt(32.W))
      val readData2    = Output(UInt(32.W))

      val immediate    = Output(UInt(32.W))
    }
  )

  val registers = Module(new Registers)
  val decoder   = Module(new Decoder).io

  /**
    * Setup. You should not change this code
    */
  registers.testHarness.setup := testHarness.registerSetup
  testHarness.registerPeek    := registers.io.readData1
  testHarness.testUpdates     := registers.testHarness.testUpdates


  /**
    * TODO: Your code here.
    */
  decoder.instruction := io.instruction_In

  registers.io.readAddress1 := io.instruction_In.registerRs1
  registers.io.readAddress2 := io.instruction_In.registerRs2
  registers.io.writeAddress := io.instruction_In.registerRd
  registers.io.writeData    := io.writeData
  registers.io.writeEnable  := io.controlSignals2.regWrite

  // registers.io.readAddress1 := 0.U
  // registers.io.readAddress2 := 0.U
  // registers.io.writeEnable  := false.B
  // registers.io.writeAddress := 0.U
  // registers.io.writeData    := 0.U

  // decoder.instruction := 0.U.asTypeOf(new Instruction)

  io.controlSignals := decoder.controlSignals
  io.branchType     := decoder.branchType    
  io.op1Select      := decoder.op1Select     
  io.op2Select      := decoder.op2Select     
  io.immType        := decoder.immType       
  io.ALUop          := decoder.ALUop      

  io.readData1 := registers.io.readData1  
  io.readData2 := registers.io.readData2
  val immMap = Array(
    // ALUOps.ADD      -> (io.op1 + io.op2),
    ImmFormat.ITYPE   -> (io.instruction_In.immediateIType),
    ImmFormat.STYPE   -> (io.instruction_In.immediateSType)
    )


  io.immediate := MuxLookup(decoder.immType, 0.U(32.W), immMap)

}
