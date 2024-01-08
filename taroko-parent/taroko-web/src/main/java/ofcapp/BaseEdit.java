/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
*  109-04-16  V1.00.01  Zuwei       updated for project coding standard      *
* 109-04-21  V1.00.01  Zuwei       code format                              *
*  109-07-27  V1.00.01  Zuwei      coding standard      *
*                                                                            *  
******************************************************************************/
package ofcapp;

import taroko.com.TarokoCommon;

// import taroko.com.*;
@SuppressWarnings({"unchecked", "deprecation"})
public abstract class BaseEdit extends BasePage {

  public boolean updateRetrieve = false;
  public boolean addRetrieve = false;
  public boolean userAction = false;

  // public String is_pageMode="";
  public abstract void actionFunction(TarokoCommon wr) throws Exception;

  public abstract void queryFunc() throws Exception;

  public abstract void queryRead() throws Exception;

  public abstract void querySelect() throws Exception;

  public abstract void dataRead() throws Exception;

  public abstract void saveFunc() throws Exception;

  public abstract void initButton();

  public void initPage() {}

  public void dddwSelect() {}

  /* 維護--基本method */
  public void showScreen(TarokoCommon wr) throws Exception {
    super.wp = wr;
    wp.respHtml = wp.requHtml;
    wp.initFlag = "Y";
    initPage();
    dddwSelect();
    initButton();
  }

  // @override
  public void clearFunc() throws Exception {
    wp.resetInputData();
    wp.resetOutputData();
    wp.initFlag = "Y";
    queryModeClear();

    initPage();
  }

  public void insertFunc() throws Exception {
    saveFunc();
    if (rc == 1 && userAction == false) {
      if (addRetrieve)
        dataRead();
      else
        clearFunc();
    }
  }

  public void updateFunc() throws Exception {
    saveFunc();
    if (rc == 1 && userAction == false) {
      if (updateRetrieve) {
        dataRead();
      } else {
        modSeqnoAdd();
      }
    }
  }

  public void deleteFunc() throws Exception {
    saveFunc();
    if (rc == 1 && userAction == false) {
      clearFunc();
    }
  }

  public void modSeqnoAdd() {
    if (rc == 1) {
      int liSeqno = (int) wp.itemNum("mod_seqno") + 1;
      wp.colSet("mod_seqno", "" + liSeqno);
    }
  }

  // public String sql_mod_xxx(String s1_pgm)
  // {
  // return ", mod_user ='"+loginUser()+"'"
  // +", mod_time =sysdate"
  // +", mod_pgm ='"+s1_pgm+"'"
  // +", mod_seqno =nvl(mod_seqno,0)+1";
  // }

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
