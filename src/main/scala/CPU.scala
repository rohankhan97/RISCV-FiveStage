package FiveStage

import chisel3._
import chisel3.core.Input
import chisel3.experimental.MultiIOModule
import chisel3.experimental._


class CPU extends MultiIOModule {

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new SetupSignals)
      val testReadouts = Output(new TestReadouts)
      val regUpdates   = Output(new RegisterUpdates)
      val memUpdates   = Output(new MemUpdates)
      val currentPC    = Output(UInt(32.W))
    }
  )

  /**
    You need to create the classes for these yourself
    */
  val IFBarrier  = Module(new InstructionFetchBarrier).io
  val IDBarrier  = Module(new InstructionDecodeBarrier).io
  val EXBarrier  = Module(new ExecuteBarrier).io
  val MEMBarrier = Module(new MemoryBarrier).io

  val ID  = Module(new InstructionDecode)
  val IF  = Module(new InstructionFetch)
  val EX  = Module(new Execute)
  val MEM = Module(new MemoryFetch)
  val WB  = Module(new WriteBacK) 


  /**
    * Setup. You should not change this code
    */
  IF.testHarness.IMEMsetup     := testHarness.setupSignals.IMEMsignals
  ID.testHarness.registerSetup := testHarness.setupSignals.registerSignals
  MEM.testHarness.DMEMsetup    := testHarness.setupSignals.DMEMsignals

  testHarness.testReadouts.registerRead := ID.testHarness.registerPeek
  testHarness.testReadouts.DMEMread     := MEM.testHarness.DMEMpeek

  /**
    spying stuff
    */
  testHarness.regUpdates := ID.testHarness.testUpdates
  testHarness.memUpdates := MEM.testHarness.testUpdates
  testHarness.currentPC  := IF.testHarness.PC


  /**
    TODO: Your code here
    */

  //////////////////////////////////////////////////////////////////////
  ////////////// Barrier Between IF and ID /////////////////////////////
  //////////////////////////////////////////////////////////////////////

  IFBarrier.PC_In := IF.io.PC
  IFBarrier.instruction_In := IF.io.instruction
  IF.io.branchResult  := EXBarrier.branchResult_Out
  IF.io.adderIn       := EXBarrier.adderOut_Out

  ID.io.instruction_In := IFBarrier.instruction_Out
  ID.io.PC_In := IFBarrier.PC_Out

  ID.io.controlSignals2 := WB.io.controlSignals_Out
  ID.io.writeData       := WB.io.rdData_Out
  ID.io.rdAddress_In    := WB.io.rdAddress_Out


  //////////////////////////////////////////////////////////////////////
  ////////////// Barrier Between ID and EX /////////////////////////////
  //////////////////////////////////////////////////////////////////////

  IDBarrier.controlSignals_In := ID.io.controlSignals
  IDBarrier.branchType_In     := ID.io.branchType 
  IDBarrier.op1Select_In      := ID.io.op1Select 
  IDBarrier.op2Select_In      := ID.io.op2Select  
  IDBarrier.PcOpSelect_In     := ID.io.PcOpSelect  
  IDBarrier.immType_In        := ID.io.immType   
  IDBarrier.ALUop_In          := ID.io.ALUop      

  IDBarrier.readData1_In      := ID.io.readData1
  IDBarrier.readData2_In      := ID.io.readData2
  IDBarrier.immediate_In      := ID.io.immediate
  IDBarrier.rs1Address_In     := ID.io.rs1Address
  IDBarrier.rs2Address_In     := ID.io.rs2Address
  IDBarrier.rdAddress_In      := ID.io.rdAddress
  IDBarrier.PC_In             := ID.io.PC_Out

  EX.io.readData1         := IDBarrier.readData1_Out
  EX.io.readData2         := IDBarrier.readData2_Out
  EX.io.immediate         := IDBarrier.immediate_Out
  EX.io.PC_In             := IDBarrier.PC_Out
  EX.io.rs1Address_In     := IDBarrier.rs1Address_Out
  EX.io.rs2Address_In     := IDBarrier.rs2Address_Out
  EX.io.rdAddress_In      := IDBarrier.rdAddress_Out
  EX.io.op1Select         := IDBarrier.op1Select_Out
  EX.io.op2Select         := IDBarrier.op2Select_Out
  EX.io.PcOpSelect        := IDBarrier.PcOpSelect_Out
  EX.io.aluOp             := IDBarrier.ALUop_Out
  EX.io.controlSignals_In := IDBarrier.controlSignals_Out
  EX.io.branchType_In     := IDBarrier.branchType_Out


  //////////////////////////////////////////////////////////////////////
  ////////////// Barrier Between EX and MEM ////////////////////////////
  //////////////////////////////////////////////////////////////////////

  EXBarrier.controlSignals_In := EX.io.controlSignals_Out
  EXBarrier.dataIn_In         := IDBarrier.readData2_Out
  EXBarrier.dataAddress_In    := EX.io.aluResult
  EXBarrier.rdAddress_In      := EX.io.rdAddress_Out
  EXBarrier.adderOut_In       := EX.io.adderOut
  EXBarrier.branchResult_In   := EX.io.branchResult

  MEM.io.controlSignals_In := EXBarrier.controlSignals_Out
  MEM.io.dataIn            := EXBarrier.dataIn_Out
  MEM.io.dataAddress       := EXBarrier.dataAddress_Out
  MEM.io.rdAddress_In      := EXBarrier.rdAddress_Out


  //////////////////////////////////////////////////////////////////////
  ////////////// Barrier Between MEM and WB ////////////////////////////
  //////////////////////////////////////////////////////////////////////

  MEMBarrier.aluResult_In       := EXBarrier.dataAddress_Out
  MEMBarrier.rdAddress_In       := MEM.io.rdAddress_Out
  MEMBarrier.dataOut_In         := MEM.io.dataOut
  MEMBarrier.controlSignals_In  := MEM.io.controlSignals_Out
  
  WB.io.aluResult_In      := MEMBarrier.aluResult_Out
  WB.io.controlSignals_In := MEMBarrier.controlSignals_Out
  WB.io.rdAddress_In      := MEMBarrier.rdAddress_Out
  WB.io.dataOut_In        := MEMBarrier.dataOut_Out

  /////////////////////////////////////////////////////////////////////
  //////////////////////// FORWARDING UNIT ////////////////////////////
  /////////////////////////////////////////////////////////////////////

  EX.io.MEMrdAddress_In := MEM.io.rdAddress_Out
  EX.io.MEMaluResult_in := MEM.io.aluResult_Out
  EX.io.WBrdAddress_In  := WB.io.rdAddress_Out
  EX.io.WBaluResult_in  := WB.io.rdData_Out

}
