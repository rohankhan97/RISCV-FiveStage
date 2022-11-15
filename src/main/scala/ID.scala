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

      // val insertNOP    = Output(UInt(1.W))
      val notStall   = Output(UInt(1.W))
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
  // registers.io.writeAddress := io.instruction_In.registerRd
  registers.io.writeAddress := io.rdAddress_In
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
  
  // io.readData1 := registers.io.readData1  
  // io.readData2 := registers.io.readData2

  io.rs1Address := io.instruction_In.registerRs1
  io.rs2Address := io.instruction_In.registerRs2
  io.rdAddress  := io.instruction_In.registerRd

  val immMap = Array(
    // ALUOps.ADD      -> (io.op1 + io.op2),
    ImmFormat.ITYPE   -> (io.instruction_In.immediateIType),
    ImmFormat.STYPE   -> (io.instruction_In.immediateSType),
    ImmFormat.BTYPE   -> (io.instruction_In.immediateBType),
    ImmFormat.UTYPE   -> (io.instruction_In.immediateUType),
    // ImmFormat.UTYPE   -> (io.instruction_In.immediateUType << 12),
    ImmFormat.JTYPE   -> (io.instruction_In.immediateJType),
    ImmFormat.SHAMT   -> (io.instruction_In.immediateZType)
    )


  io.immediate := MuxLookup(decoder.immType, 0.S(32.W), immMap)

  io.PC_Out := io.PC_In

  // val delayed_rd = RegInit(0.U(5.W))
  val delayed_CS_reg   = RegInit(0.U(6.W))
  val delayed_CS_wir   = Wire(new ControlSignals)
  val stalled_not = RegInit(1.U(1.W))
  
  // delayed_rd := io.instruction_In.registerRd
  delayed_CS_reg := decoder.controlSignals.asUInt
  delayed_CS_wir := delayed_CS_reg.asTypeOf(new ControlSignals)

  when(delayed_CS_wir.memRead & stalled_not.asBool){
      // rs1 := io.WBaluResult_in
    io.notStall := 0.U
    stalled_not := 0.U
  }.otherwise{
    io.notStall := 1.U
    stalled_not := 1.U
  }

  // val diff  = Wire(UInt(5.W))
  // diff  := io.rs1Address - delayed_rd

  // val zeroWb2 = Wire(UInt(1.W))
  // val zeroWbMap = Array(
  //   0.U(32.W)      -> 1.U(1.W)
  //  ) 
  // zeroWb2 := MuxLookup(diff, 0.U(1.W), zeroWbMap)


  // when(zeroWb2.asBool & delayed_CS_wir.memRead){
  //   io.insertNOP := 0.U
  // }.otherwise{
  //   io.insertNOP := 1.U
  // }

  // when(decoder.controlSignals.memRead){
  //   io.insertNOP := 0.U
  // }.otherwise{
  //   io.insertNOP := 1.U
  // }

  // val instMap = Array(
  //   1.U(1.W)    -> 0.U(1.W)
  // )

  // io.insertNOP := MuxLookup(decoder.controlSignals.memRead, 0.U(1.W), instMap)

}
