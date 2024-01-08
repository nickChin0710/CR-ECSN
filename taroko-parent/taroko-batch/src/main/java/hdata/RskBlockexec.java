
/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskBlockexec extends BaseBin {

  public String paramType = "";
  public String acctType = "";
  public String validDate = "";
  public String execDate = "";
  public String execMode = "";
  public int execTimes = 0;
  public String execDates = "";
  public String execDateE = "";
  public double tAcctCnt = 0;
  public double tBlockCnt = 0;
  public double tBlockCnt2 = 0;
  public double tBlockCnt3 = 0;
  public double tBlockCnt4 = 0;
  public double tBlockCnt5 = 0;
  public double tBlockCnt6 = 0;
  public double tBlocknot1Cnt = 0;
  public double tBlocknot2Cnt = 0;
  public double tBlocknot3Cnt = 0;
  public double tBlocknot4Cnt = 0;
  public double tBlocknot5Cnt = 0;
  public String execMsg = "";
  public String printFlag = "";
  public String printFlag2 = "";
  public String printFlag3 = "";
  public String printFlag4 = "";
  public String printFlag5 = "";

  @Override
  public void initData() {
    paramType = "";
    acctType = "";
    validDate = "";
    execDate = "";
    execMode = "";
    execTimes = 0;
    execDates = "";
    execDateE = "";
    tAcctCnt = 0;
    tBlockCnt = 0;
    tBlockCnt2 = 0;
    tBlockCnt3 = 0;
    tBlockCnt4 = 0;
    tBlockCnt5 = 0;
    tBlockCnt6 = 0;
    tBlocknot1Cnt = 0;
    tBlocknot2Cnt = 0;
    tBlocknot3Cnt = 0;
    tBlocknot4Cnt = 0;
    tBlocknot5Cnt = 0;
    execMsg = "";
    printFlag = "";
    printFlag2 = "";
    printFlag3 = "";
    printFlag4 = "";
    printFlag5 = "";

    rowid = "";
  }

}
