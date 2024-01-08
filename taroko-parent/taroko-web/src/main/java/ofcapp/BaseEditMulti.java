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

import taroko.com.*;

@SuppressWarnings({"unchecked", "deprecation"})
public abstract class BaseEditMulti extends BasePage {

  public abstract void actionFunction(TarokoCommon wr) throws Exception;

  public abstract void dataRead() throws Exception;

  public abstract void saveFunc() throws Exception;

  public abstract void deleteFunc() throws Exception;

  public abstract void initButton();

  public void initPage() {}

  public void dddwSelect() {}

  /* 維護--基本method */
  public void showScreen(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.respHtml = wp.requHtml;
    initPage();
    dddwSelect();
    initButton();
  }

  // @override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
    queryModeClear();

    initPage();
  }

  public void btnModeUd(boolean bUpd, boolean bDel) {
    btnOnAud(bUpd, bUpd, bDel);
  }

  public boolean isAdd() {
    return eqIgno(strAction, "A");
  }

  public boolean isUpdate() {
    return eqIgno(strAction, "U");
  }

  public boolean isDelete() {
    return eqIgno(strAction, "D");
  }

  public void alertMsg(String msg1) {
    if (rc == 1) {
      if (isEmpty(msg1)) {
        if (isAdd()) {
          wp.respMesg = "資料新增成功";
        } else if (isUpdate()) {
          wp.respMesg = "資料存檔成功";
        } else if (isDelete()) {
          wp.respMesg = "資料刪除成功";
        }
      } else {
        wp.respMesg = msg1;
      }
      // wp.alertMesg(wp.respMesg);
    } else {
      alertErr2(msg1);
    }
  }

}
