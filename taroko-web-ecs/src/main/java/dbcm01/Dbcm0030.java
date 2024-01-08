/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-12  V1.00.00  yash       program initial                            *
* 108-12-03  V1.00.01  Amber	  Update init_button Authority 	     		 *
*  109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*                        
******************************************************************************/

package dbcm01;

import ofcapp.AppMsg;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;


public class Dbcm0030 extends BaseProc {


  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;

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
      /* 存檔 */
      strAction = "S2";
      dataProcess();
    }

    dddwSelect();
    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {

    wp.whereStr = " where 1=1 ";


    if (empty(wp.itemStr("ex_telcode")) == false) {
      wp.whereStr += " and  card_no like :ex_telcode ";
      setString("ex_telcode", wp.itemStr("ex_telcode") + "%");
    } else {
      wp.whereStr += " and eng_name  = '' ";
      wp.whereStr += " and chi_name != '' ";
    }

    wp.whereStr += " and to_nccc_date = '' ";
    wp.whereStr += " and reject_code  = '' ";
    wp.whereStr += " and nccc_filename  = '' ";
    wp.whereStr += " and card_no != '' ";

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

    wp.selectSQL = " card_no  " + ",apply_id " + ",chi_name " + ",eng_name " + ",mod_user"
        + ",mod_time" + ",mod_pgm" + ",mod_seqno" + ",hex(rowid) as rowid";

    wp.daoTable = "dbc_emboss";
    wp.whereOrder = " order by card_no";
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


  @Override
  public void dataProcess() throws Exception {

    String[] aaRowid = wp.itemBuff("rowid");
    String[] aModSeqno = wp.itemBuff("mod_seqno");
    String[] aaEngName = wp.itemBuff("eng_name");
    String[] aCardNo = wp.itemBuff("card_no");

    String[] hEngName = wp.itemBuff("h_eng_name");

    wp.listCount[0] = aCardNo.length;


    // save

    for (int ll = 0; ll < aCardNo.length; ll++) {
      int sa = 0;

      // check
      if (empty(hEngName[ll])) {
        continue;
      }

      // up
      if (!empty(hEngName[ll]) && !hEngName[ll].equals(aaEngName[ll])) {
        if (dbUpdate(aaEngName[ll], aaRowid[ll], aModSeqno[ll]) <= 0) {
          wp.colSet(ll, "ok_flag", "!");
          sa++;

        } else {
          wp.colSet(ll, "ok_flag", "V");
        }

      }

      // 有失敗rollback，無失敗commit
      sqlCommit(sa > 0 ? 0 : 1);
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
    try {

      // wp.initOption="--";
      // wp.optionKey = wp.item_ss("ex_telcode");
      // this.dddw_list("dddw_group_code", "ptr_group_code", "group_code", "group_name", "where 1=1
      // order by group_code");


    } catch (Exception ex) {
    }
  }



  public int dbUpdate(String eng_name, String rowid, String mod_seqno) throws Exception {
    String luSql = "update dbc_emboss set " + "  eng_name =:eng_name "
        + " , mod_user =:mod_user, mod_time=sysdate " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + "where  hex(rowid) = :rowid  and  nvl(mod_seqno,0)= :mod_seqno ";

    setString("eng_name", eng_name);
    setString("mod_user", wp.loginUser);
    setString("rowid", rowid);
    setString("mod_seqno", mod_seqno);
    sqlExec(luSql);
    if (sqlRowNum <= 0) {
      alertErr(sqlErrtext);
      return -1;
    }
    return 1;

  }

}
