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
      val aluOp = Input(UInt(4.W))

      val aluResult = Output(UInt(32.W))
      val adderOut  = Output(UInt(32.W))

      val rdAddress_In  = Input(UInt(5.W))
      val rdAddress_Out = Output(UInt(5.W))

      val branchResult = Output(UInt(1.W))
      val controlSignals_Out = Output(new ControlSignals)

    }
  )

  val op1 = Wire(SInt(32.W))
  val op2 = Wire(SInt(32.W))

  val op1Map = Array(
    Op1Select.rs1      -> io.readData1.asSInt,
    Op1Select.PC      -> io.PC_In.asSInt
    )

  op1 := MuxLookup(io.op1Select, 0.S(32.W), op1Map)
  // op1 := io.readData1.asSInt

  val op2Map = Array(
    Op2Select.rs2      -> io.readData2.asSInt,
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
    ALUOps.SLL      -> (op1.asUInt << op2(4, 0).asUInt).asUInt,
    ALUOps.SRL      -> (op1.asUInt >> op2(4, 0).asUInt).asUInt,
    ALUOps.SRA      -> (op1 >> op2(4, 0).asUInt).asUInt,
    ALUOps.LTE      -> (op1 <= op2).asUInt,
    ALUOps.LTEU     -> (op1.asUInt <= op2.asUInt).asUInt,
    ALUOps.LEZ      -> (op1 <= 0.S).asUInt,
    ALUOps.GEZ      -> (op1 >= 0.S).asUInt,
    ALUOps.JAL      -> (op1 + 4.S).asUInt,
    // ALUOps.NEZ      -> (op1 =/= 0.S).asUInt,
    ALUOps.COPY_A   -> op1.asUInt
    )

  // val registers = Module(new Registers)
  // val decoder   = Module(new Decoder).io

  // val controlSignals   = Wire(new ControlSignals)


  val zeroReg = RegInit(0.U(1.W))
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

  val constant = RegInit(-1.S(32.W))

  val PcAddMap = Array(
    PcOpSelect.PC      -> (add_op + (io.immediate << 1)).asUInt,
    PcOpSelect.rs1     -> ((add_op + (io.immediate << 1)) & constant).asUInt
  )

  io.adderOut := MuxLookup(io.PcOpSelect, 0.U(32.W), PcAddMap)
  // io.adderOut := (io.PC_In.asSInt + (io.immediate << 1)).asUInt

  val ZeroMap = Array(
    0.U(32.W)      -> 1.U(1.W)
   ) 

  zeroReg := MuxLookup(io.aluResult, 0.U(1.W), ZeroMap)

  io.branchResult := (zeroReg & io.controlSignals_In.branch) | io.controlSignals_In.jump

  io.controlSignals_Out := io.controlSignals_In

  // controlSignals  := io.controlSignals_In
  // io.controlSignals_Out  := controlSignals


  io.rdAddress_Out := io.rdAddress_In
 
}
