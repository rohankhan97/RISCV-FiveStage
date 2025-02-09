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

      val controlSignals2 = Input(new ControlSignals)  // Control signal coming back for register write back to use write enable signal
      val writeData       = Input(UInt(32.W))
      val rdAddress_In    = Input(UInt(5.W))

      val controlSignals = Output(new ControlSignals)
      val branchType     = Output(UInt(4.W))
      val op1Select      = Output(UInt(1.W))
      val op2Select      = Output(UInt(1.W))
      val PcOpSelect     = Output(UInt(1.W))
      val immType        = Output(UInt(3.W))
      val ALUop          = Output(UInt(5.W))

      val readData1    = Output(UInt(32.W))
      val readData2    = Output(UInt(32.W))

      val immediate    = Output(SInt(32.W))
      val rs1Address   = Output(UInt(5.W))
      val rs2Address   = Output(UInt(5.W))
      val rdAddress    = Output(UInt(5.W))

      val PC_Out       = Output(UInt(32.W))

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
  registers.io.writeAddress := io.rdAddress_In
  registers.io.writeData    := io.writeData
  registers.io.writeEnable  := io.controlSignals2.regWrite

  io.controlSignals := decoder.controlSignals
  io.branchType     := decoder.branchType    
  io.op1Select      := decoder.op1Select     
  io.op2Select      := decoder.op2Select     
  io.PcOpSelect     := decoder.PcOpSelect     
  io.immType        := decoder.immType       
  io.ALUop          := decoder.ALUop      

  ////////////// MY LOGIC //////////////
  val rs1 = Wire(UInt(32.W))
  val rs2 = Wire(UInt(32.W))
  rs1 := io.instruction_In.registerRs1 - io.rdAddress_In
  rs2 := io.instruction_In.registerRs2 - io.rdAddress_In
  val zeroRs1 = Wire(UInt(1.W))
  val zeroRs2 = Wire(UInt(1.W))
  val zeroRsMap = Array(
    0.U(32.W)      -> 1.U(1.W)
   ) 
  zeroRs1 := MuxLookup(rs1, 0.U(1.W), zeroRsMap)
  zeroRs2 := MuxLookup(rs2, 0.U(1.W), zeroRsMap)

  when(zeroRs1.asBool){
    io.readData1 := io.writeData
  }.otherwise{
    io.readData1 := registers.io.readData1
  }

  when(zeroRs2.asBool){
    io.readData2 := io.writeData
  }.otherwise{
    io.readData2 := registers.io.readData2
  }
  /////////////////////////////////////////
  
  io.rs1Address := io.instruction_In.registerRs1
  io.rs2Address := io.instruction_In.registerRs2
  io.rdAddress  := io.instruction_In.registerRd

  val immMap = Array(
    ImmFormat.ITYPE   -> (io.instruction_In.immediateIType),
    ImmFormat.STYPE   -> (io.instruction_In.immediateSType),
    ImmFormat.BTYPE   -> (io.instruction_In.immediateBType),
    ImmFormat.UTYPE   -> (io.instruction_In.immediateUType),
    ImmFormat.JTYPE   -> (io.instruction_In.immediateJType),
    ImmFormat.SHAMT   -> (io.instruction_In.immediateZType)
    )


  io.immediate := MuxLookup(decoder.immType, 0.S(32.W), immMap)

  io.PC_Out := io.PC_In

}
