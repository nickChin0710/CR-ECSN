/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskTrcorpBank extends BaseBin {

  public String dataYymm = "";
  public String corpNo = "";
  public String corpPSeqno = "";
  public String branchCode = "";
  public double totDeposBal = 0;
  public double totCrLimit = 0;
  public double totCrBal = 0;
  public double totExchgAmt = 0;
  public double avgDeposBal = 0;
  public double avgCrLimit = 0;
  public double avgCrBal = 0;
  public String crWorstCode = "";
  public double crOvdueBal = 0;
  public String freeReportNpl = "";
  public String inrateType = "";
  public String inrateDate = "";
  public String inrateRefDate = "";
  public String inrateFinal = "";
  public String impDate = "";
  public String procDate = "";

  @Override
  public void initData() {

    dataYymm = "";
    corpNo = "";
    corpPSeqno = "";
    branchCode = "";
    totDeposBal = 0;
    totCrLimit = 0;
    totCrBal = 0;
    totExchgAmt = 0;
    avgDeposBal = 0;
    avgCrLimit = 0;
    avgCrBal = 0;
    crWorstCode = "";
    crOvdueBal = 0;
    freeReportNpl = "";
    inrateType = "";
    inrateDate = "";
    inrateRefDate = "";
    inrateFinal = "";
    impDate = "";
    procDate = "";

    rowid = "";

  }

}
