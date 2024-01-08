/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*                                                                            *  
******************************************************************************/
package ofcapp;

/**
 * 2019-1014 JH clearFunc()
 * */
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

@SuppressWarnings({"unchecked", "deprecation"})
public abstract class BaseReport extends BasePage {

  public BaseExcelX excel;
  public String strReportId = "";
  public String strOutFile = "";

  // public abstract void showScreen(TarokoCommon wr) throws Exception;
  // public abstract void clearFunc() throws Exception;
  /* 查詢--基本method */
  public abstract void actionFunction(TarokoCommon wr) throws Exception;

  public abstract void queryFunc() throws Exception;

  public abstract void queryRead() throws Exception;

  public abstract void querySelect() throws Exception;

  public void initPage() {}

  public void dddwSelect() {}

  public void initButton() {}

  public void showScreen(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.respHtml = wp.requHtml;
    initPage();
    dddwSelect();
    initButton();
  }

  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
    queryModeClear();
    initPage();
  }

  public void setFilename(String id) {
    strReportId = id;
    wp.exportFile =
        wp.loginUser + "_" + strReportId + "_" + wp.sysDate.substring(4) + "_" + wp.sysTime
            + ".xlsx";
    wp.linkURL = "./WebData/work/" + wp.exportFile;
    strOutFile = TarokoParm.getInstance().getDataRoot() + "/work/" + wp.exportFile;
  }

}
