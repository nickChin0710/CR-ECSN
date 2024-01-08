/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskTrcorpBankStat extends BaseBin {

  public String dataYymm = "";
  public String corpNo = "";
  public String corpPSeqno = "";
  public String branchExchgType = "";
  public String inrateType = "";
  public String inrateDate = "";
  public String inrateRefDate = "";
  public String inrateFinalCode = "";
  public double creditLimit = 0;
  public double destAmt0106 = 0;
  public double destAmt0712 = 0;
  public int m1Cnt0106 = 0;
  public int m1Cnt0712 = 0;
  public int m2Cnt0106 = 0;
  public int m2Cnt0712 = 0;
  public String maxCrBalBrch = "";
  public String maxCrLimitBrch = "";
  public String maxDeposBrch = "";
  public String maxExchgBrch = "";
  public String worstCrBal06 = "";
  public String worstCrBal12 = "";
  public double crOvdueBal30 = 0;
  public String freeReportNpl = "";
  public double totDeposBal = 0;
  public double totCrLimit = 0;
  public double totCrBal = 0;
  public double totExchgAmt = 0;
  public double avgDeposBal0103 = 0;
  public double avgDeposBal0406 = 0;
  public double avgDeposBal0106 = 0;
  public double avgDeposBal0712 = 0;
  public double avgDeposBal0112 = 0;
  public double avgDeposBal1324 = 0;
  public double avgCrBal0103 = 0;
  public double avgCrBal0406 = 0;
  public double avgCrBal0106 = 0;
  public double avgCrBal0712 = 0;
  public double avgCrBal0112 = 0;
  public double avgCrBal1324 = 0;
  public double avgCrLimit0103 = 0;
  public double avgCrLimit0406 = 0;
  public double avgCrLimit0106 = 0;
  public double avgCrLimit0712 = 0;
  public double avgCrLimit0112 = 0;
  public double avgCrLimit1324 = 0;
  public double avgExchg0103 = 0;
  public double avgExchg0406 = 0;
  public double avgExchg0106 = 0;
  public double avgExchg0712 = 0;
  public double avgExchg0112 = 0;
  public double avgExchg1324 = 0;
  public double rateDepos03 = 0;
  public double rateDepos06 = 0;
  public double rateDepos12 = 0;
  public double rateCrBal03 = 0;
  public double rateCrBal06 = 0;
  public double rateCrBal12 = 0;
  public double rateCrLimit03 = 0;
  public double rateCrLimit06 = 0;
  public double rateCrLimit12 = 0;
  public double rateExchg03 = 0;
  public double rateExchg06 = 0;
  public double rateExchg12 = 0;
  public String procDate = "";

  @Override
  public void initData() {

    dataYymm = "";
    corpNo = "";
    corpPSeqno = "";
    branchExchgType = "";
    inrateType = "";
    inrateDate = "";
    inrateRefDate = "";
    inrateFinalCode = "";
    creditLimit = 0;
    destAmt0106 = 0;
    destAmt0712 = 0;
    m1Cnt0106 = 0;
    m1Cnt0712 = 0;
    m2Cnt0106 = 0;
    m2Cnt0712 = 0;
    maxCrBalBrch = "";
    maxCrLimitBrch = "";
    maxDeposBrch = "";
    maxExchgBrch = "";
    worstCrBal06 = "";
    worstCrBal12 = "";
    crOvdueBal30 = 0;
    freeReportNpl = "";
    totDeposBal = 0;
    totCrLimit = 0;
    totCrBal = 0;
    totExchgAmt = 0;
    avgDeposBal0103 = 0;
    avgDeposBal0406 = 0;
    avgDeposBal0106 = 0;
    avgDeposBal0712 = 0;
    avgDeposBal0112 = 0;
    avgDeposBal1324 = 0;
    avgCrBal0103 = 0;
    avgCrBal0406 = 0;
    avgCrBal0106 = 0;
    avgCrBal0712 = 0;
    avgCrBal0112 = 0;
    avgCrBal1324 = 0;
    avgCrLimit0103 = 0;
    avgCrLimit0406 = 0;
    avgCrLimit0106 = 0;
    avgCrLimit0712 = 0;
    avgCrLimit0112 = 0;
    avgCrLimit1324 = 0;
    avgExchg0103 = 0;
    avgExchg0406 = 0;
    avgExchg0106 = 0;
    avgExchg0712 = 0;
    avgExchg0112 = 0;
    avgExchg1324 = 0;
    rateDepos03 = 0;
    rateDepos06 = 0;
    rateDepos12 = 0;
    rateCrBal03 = 0;
    rateCrBal06 = 0;
    rateCrBal12 = 0;
    rateCrLimit03 = 0;
    rateCrLimit06 = 0;
    rateCrLimit12 = 0;
    rateExchg03 = 0;
    rateExchg06 = 0;
    rateExchg12 = 0;
    procDate = "";

    rowid = "";

  }

}
