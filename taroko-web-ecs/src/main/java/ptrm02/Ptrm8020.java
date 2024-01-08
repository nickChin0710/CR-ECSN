/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-12-31  V1.00.03  shiyuqi    修改無意義命名                                                                                      *
* 110-02-23  V1.00.04  Justin     若修改LDAP_URL_WF_TYPE，則需重新讀取系統參數    
* 110-03-01  V1.00.05  Justin     reload system parameter if deleting LDAP_URL_WF_TYPE
* 110-10-20  V1.00.05  Sunny      修改查詢時不論是否有where條件，一律採用相同的排序，讓分頁跨頁查詢結果一致* 
* 110-10-26  V1.00.06  Justin     修改讀取AD系統參數順序                     *   
* 111-04-21  V1.00.07  Justin     顯示查詢超過100筆請改用Ptrm8025訊息        *
******************************************************************************/
package ptrm02;
/* 系統參數對照表維護 V.2019-1203
 * 2019-1203:  Alex  fix initButton
 * 2018-0322:	JH		++id_code,id_code2
 * 
 * */
import java.util.Arrays;

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.base.CommSqlStr;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

public class Ptrm8020 extends BaseEdit {
Ptrm8020Func func;
  String wfParm = "IDTAB", wfKey = "";// kk3 = "";

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
    if (wp.respHtml.indexOf("_detl") > 0) {
      // wp.col_set("IND_NUM", "0");
      showScreenDetl();
    }

  }

  void showScreenDetl() {
    // -set new-
    int rr = 0;
    rr = wp.listCount[0];
    wp.colSet(0, "IND_NUM", "" + rr);
  }

  @Override
  public void queryFunc() throws Exception {
    wp.whereStr = " where wf_parm ='IDTAB'" + sqlCol(wp.itemStr("ex_key"), "wf_key", "like%")
        + sqlCol(wp.itemStr("ex_dept_no"), "wf_value5");
    wp.whereOrder = " order by wf_key ";

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "wf_parm, " + "wf_key," + "wf_desc, " + "wf_value5, " + "wf_value6 ";
    wp.daoTable = "ptr_sys_parm";
//    if (empty(wp.whereStr)) {
//      wp.whereStr = " ORDER BY 1";
//    }
    wp.whereOrder = " order by wf_key ";
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
    wfParm = wp.itemStr("data_k1");
    wfKey = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    // if(empty(kk1)){
    // kk1=wp.item_ss("A_wf_parm");
    // }
    if (empty(wfKey)) {
      wfKey = wp.itemStr("A_wf_key");
    }

    this.daoTid = "A_";
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "wf_key,   " + "wf_desc , " + "wf_value5,"
        + "wf_value6," + "mod_seqno," + "mod_user, " + "uf_2ymd(mod_time) as mod_date "
        + ", mod_pgm";
    wp.daoTable = "ptr_sys_parm";
    wp.whereStr = "where 1=1" + sqlCol(wfParm, "wf_parm") + sqlCol(wfKey, "wf_key");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + wfParm + ", " + wfKey);
      return;
    }

    wp.colSet("mod_user", wp.colStr("A_mod_user"));
    wp.colSet("mod_date", wp.colStr("A_mod_date"));

    dataReadDetl();
  }

  void dataReadDetl() throws Exception {
    this.selectNoLimit();
    // wp.sel.varRows = 99999;
    wp.daoTable = "ptr_sys_idtab";
    wp.selectSQL = "hex(rowid) as b_rowid" + ", wf_id     " + ", wf_desc " + ", id_code, id_code2";
    wp.whereStr = "WHERE 1=1" + sqlCol(wfKey, "wf_type");
    wp.whereOrder = " order by wf_id";
    pageQuery();
    if (sqlRowNum >= 100) {
    	alertErr("參數值已達100筆資料，請至PTRM8025進行維護");
        return;
    }
    wp.setListCount(1);
    wp.notFound = "";
  }


  @Override
  public void saveFunc() throws Exception {
    func = new Ptrm8020Func();
    func.setConn(wp);
    func.modPgm = wp.modPgm();
    func.modUser = wp.loginUser;

    int llOk = 0, llErr = 0;
    int delCnt = 0;
    // String ls_opt="";

    String[] aaId = wp.itemBuff("wf_id");
    String[] aaDesc = wp.itemBuff("wf_desc");
    String[] aaCode = wp.itemBuff("id_code");
    String[] aaCode2 = wp.itemBuff("id_code2");
    String[] aaOpt = wp.itemBuff("opt");
    wp.listCount[0] = aaId.length;
    wp.colSet("IND_NUM", "" + aaId.length);

    // -check duplication-
    for (int ll = 0; ll < aaId.length; ll++) {
      wp.colSet(ll, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
    	delCnt++;
        continue;
      }
      if (empty(aaId[ll])) {
        continue;
      }

      if (ll != Arrays.asList(aaId).indexOf(aaId[ll])) {
        wp.colSet(ll, "ok_flag", "!");
        llErr++;
        alertErr("資料值重複: " + llErr);
        break;
      }

      // -desc<>''-
      if (empty(aaDesc[ll])) {
        llErr++;
        alertErr("說明:不可空白 " + llErr);
        break;
      }

    }
    if (llErr > 0) {
      return;
    }
    
    // not allowed to delete all system parameters
    if (aaId.length <= delCnt) {
    	optNumKeep(aaId.length, aaOpt);
    	alertErr("系統必要參數，不可全部刪除，請至少保留1個參數，建議採取修改內容的方式。");
        return;
	}

    // -delete no-approve-
    if (func.dbDelete() < 0) {
      alertErr(func.getMsg());
      return;
    }

    // -insert-
    for (int ll = 0; ll < aaId.length; ll++) {
      if (empty(aaId[ll])) {
        continue;
      }

      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
        continue;
      }

      func.varsSet("wf_id", aaId[ll]);
      func.varsSet("wf_desc", aaDesc[ll]);
      func.varsSet("id_code", aaCode[ll]);
      func.varsSet("id_code2", aaCode2[ll]);

      if (func.dbInsert() == 1) {
        llOk++;
      } else {
        llErr++;
      }
    }
    if (llOk > 0) {
      sqlCommit(1);
	  if (wp.itemEq("A_wf_key", TarokoParm.LDAP_URL_WF_TYPE)) {
		  TarokoParm.getInstance().setLdapIPAndDomain(wp.getConn());
	  }
    }
    alertMsg("資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr);
    dataRead();
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0)
      this.btnModeAud("XX");

  }
}
