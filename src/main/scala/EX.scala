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
      val immediate = Input(SInt(32.W))
      val PC_In     = Input(UInt(32.W))
      val op1Select = Input(UInt(1.W))
      val op2Select = Input(UInt(1.W))
      val PcOpSelect = Input(UInt(1.W))
      val aluOp     = Input(UInt(5.W))
      val branchType_In     = Input(UInt(4.W))

      val aluResult = Output(UInt(32.W))
      val adderOut  = Output(UInt(32.W))
      val MEMaluResult_in = Input(UInt(32.W))
      val WBaluResult_in  = Input(UInt(32.W))

      val rs1Address_In = Input(UInt(5.W))
      val rs2Address_In = Input(UInt(5.W))
      val rdAddress_In  = Input(UInt(5.W))
      val rdAddress_Out = Output(UInt(5.W))
      val MEMrdAddress_In  = Input(UInt(5.W))
      val WBrdAddress_In   = Input(UInt(5.W))

      val branchResult = Output(UInt(1.W))
      val controlSignals_Out = Output(new ControlSignals)
      val notStall   = Output(UInt(1.W))

    }
  )

  //////////////////////////////////////////////////////////
  //////////////////// FORWARDING UNIT /////////////////////
  //////////////////////////////////////////////////////////

  val rs1 = Wire(UInt(32.W))
  val rs2 = Wire(UInt(32.W))
  val mem1 = Wire(UInt(5.W))
  val mem2 = Wire(UInt(5.W))
  val wb1  = Wire(UInt(5.W))
  val wb2  = Wire(UInt(5.W))

  mem1 := io.rs1Address_In - io.MEMrdAddress_In
  mem2 := io.rs2Address_In - io.MEMrdAddress_In
  wb1  := io.rs1Address_In - io.WBrdAddress_In
  wb2  := io.rs2Address_In - io.WBrdAddress_In

  val zeroMem1 = Wire(UInt(1.W))
  val zeroMem2 = Wire(UInt(1.W))
  val zeroMemMap = Array(
    0.U(32.W)      -> 0.U(1.W)
   ) 
  zeroMem1 := MuxLookup(mem1, 1.U(1.W), zeroMemMap)
  zeroMem2 := MuxLookup(mem2, 1.U(1.W), zeroMemMap)

  val zeroWb1 = Wire(UInt(1.W))
  val zeroWb2 = Wire(UInt(1.W))
  val zeroWbMap = Array(
    0.U(32.W)      -> 0.U(1.W)
   ) 
  zeroWb1 := MuxLookup(wb1, 1.U(1.W), zeroWbMap)
  zeroWb2 := MuxLookup(wb2, 1.U(1.W), zeroWbMap)

  val delayed_CS_reg   = RegInit(0.U(6.W))
  val delayed_CS_wir   = Wire(new ControlSignals)

  delayed_CS_reg := io.controlSignals_In.asUInt
  delayed_CS_wir := delayed_CS_reg.asTypeOf(new ControlSignals)

  // val stalled = Wire(UInt(1.W))
  // when(zeroMem1.asBool){
  //   when(io.controlSignals_In.memRead){
  //     when(stalled.asBool){
  //       io.notStall := 0.U
  //       stalled := 0.U
  //     }.otherwise{
  //       io.notStall := 1.U
  //       stalled := 1.U
  //     }
  //     rs1 := io.WBaluResult_in
  //   }.otherwise{
  //     io.notStall := 1.U
  //     rs1 := io.MEMaluResult_in
  //     stalled := 1.U
  //   }
  // }.otherwise{
  //   io.notStall := 1.U
  //   stalled := 1.U
  //   when(zeroWb1.asBool){
  //     rs1 := io.WBaluResult_in
  //   }.otherwise{
  //     rs1 := io.readData1
  //   }
  // }

  // when(zeroMem1.asBool){
  //   rs1 := io.MEMaluResult_in
  // }.otherwise{
  //   when(zeroWb1.asBool){
  //     rs1 := io.WBaluResult_in
  //   }.otherwise{
  //     rs1 := io.readData1
  //   }
  // }

  when(zeroMem1.asBool){
    when(zeroWb1.asBool){
      rs1 := io.readData1
    }.otherwise{
      rs1 := io.WBaluResult_in
    }
    io.notStall := 1.U
  }.otherwise{
    when(delayed_CS_wir.memRead){
      rs1 := io.WBaluResult_in
      io.notStall := 0.U
    }.otherwise{
      rs1 := io.MEMaluResult_in
      io.notStall := 1.U
    }
  }

  // when(zeroMem2.asBool){
  //   rs2 := io.MEMaluResult_in
  // }.otherwise{
  //   when(zeroWb2.asBool){
  //     rs2 := io.WBaluResult_in
  //   }.otherwise{
  //     rs2 := io.readData2
  //   }
  // }

  when(zeroMem2.asBool){
    when(zeroWb2.asBool){
      rs2 := io.readData2
    }.otherwise{
      rs2 := io.WBaluResult_in
    }
    // io.notStall := 1.U
  }.otherwise{
    // when(delayed_CS_wir.memRead){
    //   rs2 := io.WBaluResult_in
    //   io.notStall := 0.U
    // }.otherwise{
    rs2 := io.MEMaluResult_in
      // io.notStall := 1.U
    }
  }

  val op1 = Wire(SInt(32.W))
  val op2 = Wire(SInt(32.W))

  val op1Map = Array(
    // Op1Select.rs1      -> io.readData1.asSInt,
    Op1Select.rs1      -> rs1.asSInt,
    Op1Select.PC       -> io.PC_In.asSInt
    )

  op1 := MuxLookup(io.op1Select, 0.S(32.W), op1Map)
  // op1 := io.readData1.asSInt

  val op2Map = Array(
    // Op2Select.rs2      -> io.readData2.asSInt,
    Op2Select.rs2      -> rs2.asSInt,
    Op2Select.imm      -> io.immediate
    )

  op2 := MuxLookup(io.op2Select, 0.S(32.W), op2Map)
  // op2 := io.immediate

  // when(io.op2Select.asBool){
  //   op2 := io.immediate
  // }.otherwise{
  //   op2 := io.readData2
  // }

  // val ALUOpMap = Array(
  //   ALUOps.ADD      -> (op1 + op2),
  //   ALUOps.SUB      -> (op1 - op2),
  //   ALUOps.AND      -> (op1 & op2),
  //   ALUOps.OR       -> (op1 | op2),
  //   ALUOps.XOR      -> (op1 ^ op2),
  //   ALUOps.SLT      -> (op1 < op2).asSInt,
  //   ALUOps.SLTU     -> (op1.asUInt < op2.asUInt).asSInt,
  //   ALUOps.SLL      -> (op1.asUInt << op2(4, 0).asUInt).asSInt,
  //   ALUOps.SRL      -> (op1.asUInt >> op2(4, 0).asUInt).asSInt,
  //   ALUOps.SRA      -> (op1 >> op2(4, 0).asUInt)
  //   )

  val ALUOpMap = Array(
    ALUOps.ADD      -> (op1 + op2).asUInt,
    ALUOps.SUB      -> (op1 - op2).asUInt,
    ALUOps.AND      -> (op1 & op2).asUInt,
    ALUOps.OR       -> (op1 | op2).asUInt,
    ALUOps.XOR      -> (op1 ^ op2).asUInt,
    ALUOps.SLT      -> (op1 < op2).asUInt,
    ALUOps.GTE      -> (op2 < op1).asUInt,
    ALUOps.SLTU     -> (op1.asUInt < op2.asUInt).asUInt,
    ALUOps.GTEU     -> (op2.asUInt < op1.asUInt).asUInt,
    ALUOps.SLL      -> (op1.asUInt << op2(4, 0).asUInt).asUInt,
    ALUOps.SRL      -> (op1.asUInt >> op2(4, 0).asUInt).asUInt,
    ALUOps.SRA      -> (op1 >> op2(4, 0).asUInt).asUInt,
    ALUOps.LTE      -> (op1 <= op2).asUInt,
    ALUOps.LTEU     -> (op1.asUInt <= op2.asUInt).asUInt,
    ALUOps.LEZ      -> (op1 <= 0.S).asUInt,
    ALUOps.GEZ      -> (op1 >= 0.S).asUInt,
    ALUOps.JAL      -> (op1 + 4.S).asUInt,
    // ALUOps.NEZ      -> (op1 =/= 0.S).asUInt,
    ALUOps.COPY_A   -> op1.asUInt,
    ALUOps.COPY_B   -> op2.asUInt
    )

  // val registers = Module(new Registers)
  // val decoder   = Module(new Decoder).io

  // val controlSignals   = Wire(new ControlSignals)


  // reg := op1 < op2

  /**
    * Setup. You should not change this code
    */
  // registers.testHarness.setup := testHarness.registerSetup
  // testHarness.registerPeek    := registers.io.readData1
  // testHarness.testUpdates     := registers.testHarness.testUpdates

  io.aluResult := MuxLookup(io.aluOp, 0.U(32.W), ALUOpMap)
  // io.aluResult := MuxLookup(io.aluOp, 0.S(32.W), ALUOpMap)

  val add_op = Wire(SInt(32.W))

  val addMap = Array(
    PcOpSelect.PC       -> io.PC_In.asSInt,
    PcOpSelect.rs1      -> io.readData1.asSInt
    )

  add_op := MuxLookup(io.PcOpSelect, 0.S(32.W), addMap)

  val constant = 4294967294l.U(32.W)

  val PcAddMap = Array(
    PcOpSelect.PC      -> (add_op + (io.immediate)).asUInt,
    PcOpSelect.rs1     -> ((add_op + (io.immediate)) & constant.asSInt).asUInt
  )

  io.adderOut := MuxLookup(io.PcOpSelect, 0.U(32.W), PcAddMap)
  // io.adderOut := (io.PC_In.asSInt + (io.immediate << 1)).asUInt

  val zeroReg = Wire(UInt(1.W))
  val LTReg = Wire(UInt(1.W))
  val unEqReg = Wire(UInt(1.W))

  val branchReg = Wire(UInt(1.W))

  val ZeroMap = Array(
    0.U(32.W)      -> 1.U(1.W)
   ) 

  zeroReg := MuxLookup(io.aluResult, 0.U(1.W), ZeroMap)

  val LTMap = Array(
    1.U(32.W)      -> 1.U(1.W)
   ) 

  LTReg := MuxLookup(io.aluResult, 0.U(1.W), LTMap)

  val unEqMap = Array(
    0.U(32.W)      -> 0.U(1.W)
   ) 

  unEqReg := MuxLookup(io.aluResult, 1.U(1.W), unEqMap)

  val branchMap = Array(
    branchType.beq      -> zeroReg,
    branchType.neq      -> unEqReg,
    branchType.lt       -> LTReg,
    branchType.ltu      -> LTReg,
    branchType.gte      -> LTReg,
    branchType.gteu     -> LTReg
   )

  branchReg := MuxLookup(io.branchType_In, 0.U(1.W), branchMap)

  io.branchResult := (branchReg & io.controlSignals_In.branch) | io.controlSignals_In.jump

  io.controlSignals_Out := io.controlSignals_In

  // controlSignals  := io.controlSignals_In
  // io.controlSignals_Out  := controlSignals


  io.rdAddress_Out := io.rdAddress_In
 
}
