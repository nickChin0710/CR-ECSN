/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-17  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*                                                                            *  
******************************************************************************/
package ofcapp;
/**
 * 2019-1014 JH clearFunc()
 * */

import taroko.com.TarokoCommon;

@SuppressWarnings({"unchecked", "deprecation"})
public abstract class BaseProc extends BasePage {

  /* 基本功能method */
  // public abstract void showScreen(TarokoCommon wr) throws Exception;
  // public abstract void clearFunc() throws Exception;
  public abstract void actionFunction(TarokoCommon wr) throws Exception;

  public abstract void queryFunc() throws Exception;

  public abstract void queryRead() throws Exception;

  public abstract void querySelect() throws Exception;

  public abstract void dataRead() throws Exception;

  public abstract void dataProcess() throws Exception;

  public void dddwSelect() {}

  public void initButton() {}

  public void initPage() {}

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

  public String itemKk(String col) {
    String ss = wp.itemStr("kk_" + col);
    if (empty(ss) == false) {
      return ss;
    }

    return wp.itemStr(col);
  }

  public void alertMsg(String msg1) {
    if (rc == 1) {
      if (isEmpty(msg1)) {
        wp.respMesg = "資料處理成功";
      } else {
        wp.respMesg = msg1;
      }
      // wp.alertMesg(wp.respMesg);
    } else {
      alertErr2(msg1);
    }
  }

}
