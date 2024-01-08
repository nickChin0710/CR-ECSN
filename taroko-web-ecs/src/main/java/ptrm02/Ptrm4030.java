/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-06-01  V1.00.01  ryan       program initial                            *
* 109-04-20  V1.00.02  YangFang   updated for project coding standard        *
******************************************************************************/
package ptrm02;

import java.util.Arrays;

import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Ptrm4030 extends BaseProc {

  int rr = -1;
  int ilOk = 0;
  int ilErr = 0;

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
    // wp.respCode + ",rHtml=" + wp.respHtml);
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      dataProcess();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
      // } else if (eq_igno(wp.buttonCode, "A")) {
      // /* 新增功能 */
      // insertFunc();
      // } else if (eq_igno(wp.buttonCode, "U")) {
      // /* 更新功能 */
      // updateFunc();
      // } else if (eq_igno(wp.buttonCode, "D")) {
      // /* 刪除功能 */
      // deleteFunc();
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
  public void initPage() {

  }

  @Override
  public void dddwSelect() {

    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.itemStr("ex_member_id");
        this.dddwList("dddw_member_id", "ptr_member_list", "member_id", "",
            " where  1=1  order by member_id");
      }
    } catch (Exception ex) {
    }
  }

  // for query use only
  private int getWhereStr() throws Exception {
	sqlParm.clear();  
    wp.whereStr = " where 1=1 and stop_flag = 'N' ";
    if (empty(wp.itemStr("ex_group_id")) == false) {
      wp.whereStr += " and group_id = :ex_group_id ";
      setString("ex_group_id", wp.itemStr("ex_group_id"));
    }
    return 1;
  }

  @Override
  public void queryFunc() throws Exception {
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    if (getWhereStr() != 1)
      return;

    wp.pageControl();

    wp.selectSQL = " group_id, " + " group_name, " + " stop_flag ";
    wp.daoTable = " ptr_group_list ";
    wp.whereOrder = "  ";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr("ptr_group_list:" + appMsg.errCondNodata);
      return;
    }

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.pageControl();
    String kkGroupId = itemKk("data_k1");
    if (empty(kkGroupId)) {
      kkGroupId = wp.itemStr("group_id");
    }
    String kkGroupName = itemKk("data_k2");
    if (empty(kkGroupName)) {
      kkGroupName = wp.itemStr("group_name");
    }
    wp.colSet("group_id", kkGroupId);
    wp.colSet("group_name", kkGroupName);

    wp.selectSQL = " hex(rowid) as rowid, " + " member_id, " + " member_id as member_id_o, "
        + " to_char(modify_date,'yyyymmdd') modify_date, " + " modify_date as modify_date2, "
        + " mail_type, " + " mail_type as mail_type_o ";
    wp.daoTable = " ptr_group_member ";
    wp.whereStr = " where 1=1 and group_id = :group_id";
    setString("group_id", kkGroupId);
    wp.whereOrder = "  ";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      wp.notFound = "N";
    }

    // wp.totalRows = wp.dataCnt;
    // wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
  }

  @Override
  public void dataProcess() throws Exception {
    busi.SqlPrepare sp = new SqlPrepare();
    String[] opt = wp.itemBuff("opt");
    String[] aaRowid = wp.itemBuff("rowid");
    String[] aaMemberId = wp.itemBuff("member_id");
    String[] aaMemberIdO = wp.itemBuff("member_id_o");
    String[] aaMailType = wp.itemBuff("mail_type");
    String[] aaMailTypeO = wp.itemBuff("mail_type_o");
    String[] aaModifyDate2 = wp.itemBuff("modify_date2");
    String groupId = wp.itemStr("group_id");
    wp.listCount[0] = aaMemberId.length;

    // delete
    String sqldelete = "delete ptr_group_member where group_id = :group_id ";
    setString("group_id", groupId);

    sqlExec(sqldelete);
    if (sqlRowNum < 0) {
      wp.colSet(rr, "errmsg", "delete ptr_group_member err");
      sqlCommit(0);
      return;
    }

    // -insert-
    for (int rr = 0; rr < aaMemberId.length; rr++) {

      if (checkBoxOptOn(rr, opt)) {
        continue;
      }
      sp.sql2Insert("ptr_group_member");
      sp.ppstr("group_id", groupId);
      sp.ppstr("member_id", aaMemberId[rr]);
      sp.ppstr("mail_type", aaMailType[rr]);
      if (empty(aaRowid[rr]) || aaMailTypeO[rr].equals(aaMailType[rr])
          && aaMemberIdO[rr].equals(aaMemberId[rr])) {
        sp.addsql(" ,modify_date", " ,sysdate");
      } else {
        sp.ppstr("modify_date", aaModifyDate2[rr]);
      }
      sqlExec(sp.sqlStmt(), sp.sqlParm());

      if (sqlRowNum <= 0) {
        wp.colSet(rr, "ok_flag", "!");
        wp.colSet(rr, "errmsg", "update ptr_group_member err");
        sqlCommit(0);
        return;
      }
    }
    sqlCommit(1);
    dataRead();
    errmsg("存檔成功");
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

}
