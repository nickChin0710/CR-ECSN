/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-11-23  V1.00.00  yash       program initial                            *
* 108-12-03  V1.00.01  Amber	  Update init_button Authority 	     		 *
*  109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*                        
******************************************************************************/

package dbcp01;



import java.util.Arrays;

import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;

public class Dbcp0020 extends BaseProc {
  String contractNo = "", contractSeqNo = "";

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
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
      // insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      // updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 執行 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {

    wp.whereStr = " where 1=1 ";


    if (empty(wp.itemStr("ex_card_type_t")) == false) {
      wp.whereStr += " and  apply_source = :ex_card_type_t ";
      setString("ex_card_type_t", wp.itemStr("ex_card_type_t"));
    }


    return true;
  }

  @Override
  public void queryFunc() throws Exception {
    getWhereStr();
    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = " apply_source " + ",apply_source_name " + ",crt_date " + ",crt_user"
        + ",mod_user" + ",hex(rowid) as rowid, mod_seqno ";

    wp.daoTable = "dbc_apply_source";
    wp.whereOrder = " order by apply_source";
    getWhereStr();

    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {

    dataRead();
  }

  @Override
  public void dataRead() throws Exception {

  }


  public void dataProcess() throws Exception {

    // -check approve-
    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }


    String[] opt = wp.itemBuff("opt");
    String[] rowid = wp.itemBuff("rowid");
    String[] modSeqno = wp.itemBuff("mod_seqno");
    String[] applySource = wp.itemBuff("apply_source");
    String[] applySourceName = wp.itemBuff("apply_source_name");


    String[] happlySource = wp.itemBuff("h_apply_source");
    String[] hApplySourceName = wp.itemBuff("h_apply_source_name");



    wp.listCount[0] = applySource.length;

    int isOk = 0, err = 0;

    // -check duplication-
    for (int ll = 0; ll < applySource.length; ll++) {
      wp.colSet(ll, "ok_flag", "");


      if (empty(applySource[ll])) {
        wp.colSet(ll, "ok_flag", "!");
        err++;
        continue;
      }

      if (ll != Arrays.asList(applySource).indexOf(applySource[ll])) {
        wp.colSet(ll, "ok_flag", "!");
        err++;
        continue;
      }

    }

    if (err > 0) {
      alertErr("不可重複或空白: " + err);
      return;
    }

    // save
    for (int ll = 0; ll < applySource.length; ll++) {

      if (checkBoxOptOn(ll, opt)) {
        if (empty(rowid[ll])) {
          wp.colSet(ll, "ok_flag", "V");
          isOk++;
          continue;
        } else {
          if (dbDelete(rowid[ll], modSeqno[ll]) <= 0) {
            wp.colSet(ll, "ok_flag", "!");
            err++;
          } else {
            wp.colSet(ll, "ok_flag", "V");
            isOk++;
          } ;

        }
      } else {
        if (empty(rowid[ll])) {
          // insert
          if (dbInsert(applySource[ll], applySourceName[ll]) <= 0) {
            wp.colSet(ll, "ok_flag", "!");
            err++;
          } else {
            wp.colSet(ll, "ok_flag", "V");
            isOk++;
          }
        } else {
          // up
          if (!applySource[ll].equals(happlySource[ll])
              || !applySourceName[ll].equals(hApplySourceName[ll])) {
            if (dbUpdate(applySource[ll], applySourceName[ll], rowid[ll],
                modSeqno[ll]) <= 0) {
              wp.colSet(ll, "ok_flag", "!");
              err++;
            } else {
              wp.colSet(ll, "ok_flag", "V");
              isOk++;
            }

          }

        }

      }

      // 有失敗rollback，無失敗commit
      sqlCommit(isOk > 0 ? 1 : 0);
      alertMsg("處理: 成功筆數=" + isOk + "; 失敗筆數=" + err + ";");
    }

  }

  @Override
  public void initButton() {
    // if (wp.respHtml.indexOf("_detl") > 0) {
    // this.btnMode_aud();
    // }
    btnUpdateOn(wp.autUpdate());
  }

  @Override
  public void dddwSelect() {

  }


  public int dbInsert(String apply_source, String apply_source_name) throws Exception {

    String isSql = " insert into dbc_apply_source( " + "  apply_source " + ", apply_source_name "
        + ", crt_user " + ", crt_date " + ", mod_pgm " + ", mod_seqno " + " ) values ("
        + "  ?,?,?,?,?,?" + " )";
    Object[] param = new Object[] {apply_source, apply_source_name, wp.loginUser, getSysDate(),
        wp.itemStr("mod_pgm"), "1"};

    sqlExec(isSql, param);
    if (sqlRowNum <= 0) {
      alertErr(sqlErrtext);
      return -1;
    }
    return 1;

  }

  public int dbDelete(String rowid, String mod_seqno) throws Exception {
    String lsSql = " delete dbc_apply_source  where 1=1 ";
    lsSql += sqlCol(rowid, "hex(rowid)");
    lsSql += sqlCol(mod_seqno, "mod_seqno");
    sqlExec(lsSql);
    if (sqlRowNum <= 0) {
      alertErr(sqlErrtext);
      return -1;
    }
    return 1;

  }

  public int dbUpdate(String apply_source, String apply_source_name, String rowid, String mod_seqno)
      throws Exception {
    String luSql = "update dbc_apply_source set " + "   apply_source =:apply_source "
        + " , apply_source_name =:apply_source_name " + " , mod_user =:mod_user, mod_time=sysdate "
        + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + "where  hex(rowid) = :rowid  and  mod_seqno = :mod_seqno ";
    setString("apply_source", apply_source);
    setString("apply_source_name", apply_source_name);
    setString("mod_user", wp.loginUser);
    setString("rowid", rowid);
    setString("mod_seqno", mod_seqno);
    sqlExec(luSql);
    System.out.println("TTS:" + sqlRowNum);
    if (sqlRowNum <= 0) {
      alertErr(sqlErrtext);
      return -1;
    }
    return 1;

  }

}
