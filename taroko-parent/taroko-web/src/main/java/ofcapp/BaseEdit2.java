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

import taroko.com.TarokoCommon;

/**
 * 母版: 維護類型二
 */
@SuppressWarnings({"unchecked", "deprecation"})
public abstract class BaseEdit2 extends ofcapp.BasePage {

  public boolean updateRetrieve = false;

  // public boolean isAdd()=false, isUpdate()=false, isDelete()=false;

  public abstract void actionFunction(TarokoCommon wr) throws Exception;

  public abstract void queryFunc() throws Exception;

  public abstract void queryRead() throws Exception;

  public abstract void querySelect() throws Exception;

  public abstract void dataRead() throws Exception;

  public abstract void initButton();

  public abstract void insertFunc() throws Exception;

  public abstract void updateFunc() throws Exception;

  public abstract void deleteFunc() throws Exception;

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

  public void actionOk() {
    if (rc != 1) {
      return;
    }

    try {
      if (isUpdate()) {
        if (updateRetrieve) {
          dataRead();
        } else {
          int liSeqno = (int) wp.itemNum("mod_seqno") + 1;
          wp.colSet("mod_seqno", "" + liSeqno);
        }
      } else if (isAdd() || isDelete()) {
        clearFunc();
      }
    } catch (Exception ex) {
    }
  }

  public String itemKk(String col) {
    String ss = wp.itemStr("kk_" + col);
    if (empty(ss) == false) {
      return ss;
    }

    return wp.itemStr(col);
  }

  public void alertMsg(String msg1) {
    if (rc != 1) {
      alertErr2(msg1);
      return;
    }

    if (isEmpty(msg1)) {
      if (isAdd()) {
        wp.respMesg = "資料新增成功";
      } else if (isUpdate()) {
        wp.respMesg = "資料修改成功";
      } else if (isDelete()) {
        wp.respMesg = "資料刪除成功";
      }
    } else {
      wp.respMesg = msg1;
    }
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


}
