/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class RskPdrClass extends BaseBin {

  public String pdrClass = "";
  public String pdrType = "";
  public int mcode = 0;
  public double pdrMinValue = 0;
  public double pdrMaxValue = 0;
  public double pdrRate = 0;
  public double divineDreamAmt = 0;
  public double pattExplRateBef = 0;
  public double pattExplRateAft = 0;
  public double pattBadRateBef = 0;
  public double pattBadRateAft = 0;
  public String aprFlag = "";
  public String aprDate = "";
  public String aprUser = "";
  public String crtDate = "";
  public String crtUser = "";

  @Override
  public void initData() {

    pdrClass = "";
    pdrType = "";
    mcode = 0;
    pdrMinValue = 0;
    pdrMaxValue = 0;
    pdrRate = 0;
    divineDreamAmt = 0;
    pattExplRateBef = 0;
    pattExplRateAft = 0;
    pattBadRateBef = 0;
    pattBadRateAft = 0;
    aprFlag = "";
    aprDate = "";
    aprUser = "";
    crtDate = "";
    crtUser = "";

    rowid = "";

  }

}
