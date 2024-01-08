/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 108-12-03  V1.00.01  Alex       add OnlineApprove                          *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm02;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Ptrm0330 extends BaseEdit {
  Ptrm0330Func func;
  String mediaName = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" + wp.respCode + ",rHtml=" +
    // wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_media_name"), "media_name")

    ;
    wp.whereOrder = " order by media_name ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "media_name, " + "from_path, " + "to_path, " + "external_name, " + "dest_cname, "
        + "media_type, " + "trans_type, " + "in_out, " + "cycle_time, " + "mod_user, "
        + "mod_time, " + "mod_pgm, " + "mod_seqno ";
    wp.daoTable = "ptr_media";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    mediaName = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(mediaName)) {
      mediaName = wp.itemStr("media_name");
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "media_name,   " + "from_path, "
        + "to_path, " + "external_name," + "dest_cname," + "media_type," + "trans_type," + "in_out,"
        + "cycle_time," + "mod_user," + "to_char(mod_time,'yyyymmdd') as mod_date"

    ;
    wp.daoTable = "ptr_media";
    wp.whereStr = " where 1=1" + sqlCol(mediaName, "media_name");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + mediaName);
      return;
    }
  }

  @Override
  public void saveFunc() throws Exception {

    if (this.checkApproveZz() == false)
      return;

    func = new ptrm02.Ptrm0330Func(wp);
    rc = func.dbSave(strAction);
    log(func.getMsg());
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }
}
