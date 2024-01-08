/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskPdrAcno extends hdata.BaseBin {
  public String acctMonth = "";
  public String pSeqno = "";
  public String idPSeqno = "";
  public String acctType = "";
  public String stmtCycle = "";
  public String pdrClass = "";
  public String pdrType = "";
  public int mcode = 0;
  public String currentCode = "";
  public String oppostDate = "";
  public double oweAmt = 0;
  public String forecastDate = "";
  public String actualProcDate = "";
  public String actualAcctMonth = "";
  public double pdrRate = 0;
  public double currOweAmt = 0;
  public int currMcode = 0;
  public String currOppostFlag = "";
  public String currPurchaseFlag = "";
  public String currEffectFlag = "";
  public String liacFlag = "";
  public String liadFlag = "";
  public String liquFlag = "";
  public String sampleFlag = "";
  public String currSampleFlag = "";
  public String moduleFlag = "";
  public int contEffectMonths = 0;
  public double totPdrScore = 0;


  @Override
  public void initData() {
    acctMonth = "";
    pSeqno = "";
    idPSeqno = "";
    acctType = "";
    stmtCycle = "";
    pdrClass = "";
    pdrType = "";
    mcode = 0;
    currentCode = "";
    oppostDate = "";
    oweAmt = 0;
    forecastDate = "";
    actualProcDate = "";
    actualAcctMonth = "";
    pdrRate = 0;
    currOweAmt = 0;
    currMcode = 0;
    currOppostFlag = "";
    currPurchaseFlag = "";
    currEffectFlag = "";
    liacFlag = "";
    liadFlag = "";
    liquFlag = "";
    sampleFlag = "";
    currSampleFlag = "";
    moduleFlag = "";
    contEffectMonths = 0;
    totPdrScore = 0;
    rowid = "";
  }


}
