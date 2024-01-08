/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
* 109-07-06   V1.00.00  shiyuqi       updated for project coding standard     *
******************************************************************************/
package hdata;

public class PtrSysParm extends BaseBin {

  public String wfParm = "";
  public String wfKey = "";
  public String wfDesc = "";
  public String wfValue = "";
  public String wfValue2 = "";
  public String wfValue3 = "";
  public String wfValue4 = "";
  public String wfValue5 = "";
  public double wfValue6 = 0;
  public double wfValue7 = 0;
  public double wfValue8 = 0;
  public double wfValue9 = 0;
  public double wfValue10 = 0;

  @Override
  public void initData() {
    wfParm = "";
    wfKey = "";

    wfDesc = "";
    wfValue = "";
    wfValue2 = "";
    wfValue3 = "";
    wfValue4 = "";
    wfValue5 = "";
    wfValue6 = 0;
    wfValue7 = 0;
    wfValue8 = 0;
    wfValue9 = 0;
    wfValue10 = 0;

    rowid = "";
  }

}
