/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 111-04-20  V1.00.00  Justin     copy the file based on Ptrm8020            *
* 111-04-22  V1.00.01  Justin     調整新增資料方式                           *
* 111-04-25  V1.00.02  Justin     修改顯示訊息方式                           *
******************************************************************************/
package ptrm02;
/* 系統參數對照表維護 */
import java.util.Arrays;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoParm;

public class Ptrm8025 extends BaseEdit {
  Ptrm8025Func func;
  String wfParm = "IDTAB", wfKey = ""; // kk3 = "";

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
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {
      /* 瀏覽功能 :skip-page */
//      queryRead();
      dataReadDetl();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      insertByAJAX();
    }

    dddwSelect();
    initButton();
    if (wp.respHtml.indexOf("_detl") > 0) {
      // wp.col_set("IND_NUM", "0");
      showScreenDetl();
    }

  }

	private void insertByAJAX() throws Exception {
		if (isWfIdInKey(wp.itemStr("newWfId"))) {
			alertMsg("新增資料失敗[資料代碼重複]");
			wp.addJSON("ajaxMsg", "新增資料失敗[資料代碼重複]");
			wp.addJSON("isAJAXOk", "N");
			return;
		}
		
		func = new Ptrm8025Func();
	    func.setConn(wp);
	    func.modPgm = wp.modPgm();
	    func.modUser = wp.loginUser;
		func.varsSet("wf_id", wp.itemStr("newWfId"));
		func.varsSet("wf_desc", wp.itemStr("newWfDesc"));
		func.varsSet("id_code", wp.itemStr("newIdCode"));
		func.varsSet("id_code2", wp.itemStr("newIdCode2"));
		
		if (func.dbInsert() == 1) {
			sqlCommit(1);
			if (wp.itemEq("A_wf_key", TarokoParm.LDAP_URL_WF_TYPE)) {
				TarokoParm.getInstance().setLdapIPAndDomain(wp.getConn());
			}
			alertMsg("資料新增成功");
			wp.addJSON("ajaxMsg", "資料新增成功");
			wp.addJSON("isAJAXOk", "Y");
		} else {
			sqlCommit(0);
			alertMsg("新增資料失敗");
			wp.addJSON("ajaxMsg", "新增資料失敗");
			wp.addJSON("isAJAXOk", "N");
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
	StringBuilder sb = new StringBuilder();
	sb.append(" where wf_parm ='IDTAB'")
	  .append(sqlCol(wp.itemStr("ex_key"), "wf_key", "like%"))
	  .append(sqlCol(wp.itemStr("ex_dept_no"), "wf_value5"));
    wp.whereStr =  sb.toString();
    wp.whereOrder = " order by wf_key ";
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "wf_parm, wf_key, wf_desc, wf_value5, wf_value6 ";
    wp.daoTable = "ptr_sys_parm";
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
    
    if (empty(wfKey)) {
      wfKey = wp.itemStr("A_wf_key");
    }

    this.daoTid = "A_";
    StringBuilder sb = new StringBuilder();
    sb.append("hex(rowid) as rowid, mod_seqno, " );
    sb.append("wf_key,   " );
    sb.append("wf_desc , " );
    sb.append("wf_value5,");
    sb.append("wf_value6," );
    sb.append("mod_seqno," );
    sb.append("mod_user, " );
    sb.append("uf_2ymd(mod_time) as mod_date ");
    sb.append(", mod_pgm");
    wp.selectSQL = sb.toString();
    wp.daoTable = "ptr_sys_parm";
    wp.whereStr = "where 1=1" 
                 + sqlCol(wfParm, "wf_parm") 
                 + sqlCol(wfKey, "wf_key");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + wfParm + ", " + wfKey);
      return;
    }

    wp.colSet("mod_user", wp.colStr("A_mod_user"));
    wp.colSet("mod_date", wp.colStr("A_mod_date"));
    
    
    wp.whereStr = "WHERE 1=1" 
                 + sqlCol(wfKey, "wf_type") ;
    if (wp.itemEmpty("kk_wf_id") == false) {
    	 wp.whereStr += sqlCol(wp.itemStr("kk_wf_id") + "%", "wf_id", " like ");
    	 
    }
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    dataReadDetl();
    
  }

  void dataReadDetl() throws Exception {
	if (empty(wfKey)) {
		wfKey = wp.itemStr("A_wf_key");
	}
	wp.pageControl();
	// this.selectNoLimit();
    wp.daoTable = "ptr_sys_idtab";
    wp.selectSQL = "wf_id, wf_desc, id_code, id_code2 ";
//    wp.whereStr = "WHERE 1=1" + sqlCol(wfKey, "wf_type");
//    wp.queryWhere = wp.whereStr;
    wp.whereOrder = " order by wf_id";
    pageQuery();
    wp.totalRows = wp.dataCnt;
    wp.setListCount(1);
    wp.notFound = "";
    wp.setPageValue();
  }


  @Override
  public void saveFunc() throws Exception {
    func = new Ptrm8025Func();
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
        continue;
      }

    }
    
    if (llErr > 0) {
      alertErr("資料值不可重複，共[" + llErr + "]筆重複");
      return;
    }
    
    for (int ll = 0; ll < aaId.length; ll++) {
        // -option-ON-
        if (checkBoxOptOn(ll, aaOpt)) {
          continue;
        }
        
        if (empty(aaId[ll])) {
          continue;
        }

        // -desc<>''-
        if (empty(aaDesc[ll])) {
          wp.colSet(ll, "ok_flag", "!");
          llErr++;
          continue;
        }

    }
      
    if (llErr > 0) {
    	alertErr("「說明」不可空白，共[" + llErr + "]筆空白");
        return;
    }
    
    // not allowed to delete all system parameters
    if (getTotalCnt() <= delCnt) {
    	optNumKeep(aaId.length, aaOpt);
    	alertErr("系統必要參數，不可全部刪除，請至少保留1個參數，建議採取修改內容的方式。");
        return;
	}

    // -insert-
    for (int ll = 0; ll < aaId.length; ll++) {
      if (empty(aaId[ll])) {
        continue;
      }
      
      func.varsSet("wf_id", aaId[ll]);
      func.varsSet("wf_desc", aaDesc[ll]);
      func.varsSet("id_code", aaCode[ll]);
      func.varsSet("id_code2", aaCode2[ll]);
      
      // -delete no-approve-
      if (func.dbDelete() < 0) {
	        alertErr(func.getMsg());
	        return;
	  }
      
      // -option-ON-
      if (checkBoxOptOn(ll, aaOpt)) {
        continue;
      }

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

	private boolean isWfIdInKey(String wfId) {
		if (empty(wfKey)) {
			wfKey = wp.itemStr("A_wf_key");
		}
		
		String sql = " select count(*) as cnt from ptr_sys_idtab where 1=1 " 
		             + sqlCol(wfKey, "wf_type")
		             + sqlCol(wfId, "wf_id");
		sqlSelect(sql);
		if (sqlNotFind()) {
			return true;
		}
		return sqlInt("cnt") > 0;
	}
	
	private int getTotalCnt() {
		if (empty(wfKey)) {
			wfKey = wp.itemStr("A_wf_key");
		}
		
		String sql = " select count(*) as cnt from ptr_sys_idtab where 1=1 " 
		             + sqlCol(wfKey, "wf_type");
		sqlSelect(sql);
		if (sqlNotFind()) {
			return -1;
		}
		return sqlInt("cnt");
	}

	@Override
	public void initButton() {
		if (wp.respHtml.indexOf("_detl") > 0)
			this.btnModeAud("XX");

	}
}
