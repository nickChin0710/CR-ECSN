/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class PtrStopparam extends BaseBin {
  public String paramType = "";
  public String acctType = "";
  public String validDate = "";
  public String aprFlag = "";
  public String pauseFlag = "";
  public String execMode = "";
  public int execDay = 0;
  public int execCycleNday = 0;
  public String execDate = "";
  public int n0Month = 0;
  public int n1Cycle = 0;
  public int mcodeValue = 0;
  public double debtAmt = 0;
  public String nonAf = "";
  public String nonRi = "";
  public String nonPn = "";
  public String nonPf = "";
  public String nonLf = "";


  @Override
  public void initData() {
    paramType = "";
    acctType = "";
    validDate = "";
    aprFlag = "";
    pauseFlag = "";
    execMode = "";
    execDay = 0;
    execCycleNday = 0;
    execDate = "";
    n0Month = 0;
    n1Cycle = 0;
    mcodeValue = 0;
    debtAmt = 0;
    nonAf = "";
    nonRi = "";
    nonPn = "";
    nonPf = "";
    nonLf = "";
    rowid = "";
  }

}
