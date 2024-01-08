/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 107-03-06  V1.00.00  yash       program initial                            *
* 108-12-20  V1.00.00  Zuwei      check id when update                       *
* 109-03-24  V1.00.01  Zhenwu Zhu 統一證號檢核                               *
* 109-03-26  V1.00.02  Wilson     統一證號檢核規則調整                       *
* 109-04-28  V1.00.03  YangFang   updated for project coding standard        *
* 109-06-03  V1.00.04  Shiyuqi    checkId方法調用變更                        *   
* 109-06-09  V1.00.05  shiyuqi    修改程式名稱                               *
* 109-12-17  V1.00.06  Justin     dataRead after update                      *
* 111-04-20  V1.00.07  Justin     修改「修改ID」邏輯                         *
* 111-04-21  V1.00.08  Justin     修正left join條件                          *
******************************************************************************/

package crdm01;

import busi.ecs.CommBusiCrd;
import busi.ecs.CommFunction;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

public class Crdm0511 extends BaseEdit {
  CommFunction comm = new CommFunction();
  CommBusiCrd comcrd = new CommBusiCrd();
  String mExIdPSeqno = "";

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
      updateRetrieve = true;
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

    initButton();
  }

  // for query use only
  private boolean getWhereStr() throws Exception {
    if (empty(wp.itemStr("ex_old_id_no")) && empty(wp.itemStr("ex_id_no"))) {
      alertErr("新舊身分證字號至少輸入一項!!");
      return false;
    }

    wp.whereStr = " where 1=1 ";

    // if(empty(wp.item_ss("ex_id_no")) == false){
    // String sql_select = "SELECT count(*) as cnt from crd_chg_id where id_no like :id_no2";
    // setString("id_no2", wp.item_ss("ex_id_no")+"%");
    // sqlSelect(sql_select);
    // if(sql_num("cnt")>0){
    // wp.whereStr += " and i.mod_time = (SELECT max(mod_time) from crd_chg_id where id_no like
    // :id_no3 ) ";
    // setString("id_no3", wp.item_ss("ex_id_no")+"%");
    // }
    // wp.whereStr += " and c.id_no like :id_no ";
    // setString("id_no", wp.item_ss("ex_id_no")+"%");
    // }
    if (!empty(wp.itemStr("ex_id_no"))) {
      wp.whereStr += sqlCol(wp.itemStr("ex_id_no"), "c.id_no", "like%");
    }
    String exOldIdNo = wp.itemStr("ex_old_id_no");
    if (!empty(exOldIdNo)) {
      wp.whereStr += sqlCol(exOldIdNo, "i.old_id_no", "like%");
    }



    return true;
  }
  
  private boolean getWhereStr2() throws Exception {
	    if (empty(wp.itemStr("ex_old_id_no")) && empty(wp.itemStr("ex_id_no"))) {
	      alertErr("新舊身分證字號至少輸入一項!!");
	      return false;
	    }

	    wp.whereStr = " where 1=1 ";

	    if (!empty(wp.itemStr("ex_id_no"))) {
	      wp.whereStr += sqlCol(wp.itemStr("ex_id_no"), "c.id_no", "like%");
	    }
	    String exOldIdNo = wp.itemStr("ex_old_id_no");
	    if (!empty(exOldIdNo)) {
	      wp.whereStr += sqlCol(exOldIdNo, "i.ID", "like%");
	    }

	    return true;
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
    wp.pageControl();

    if (!getWhereStr()) {
      return;
    }
    StringBuilder sb = new StringBuilder();
	sb.append("  c.id_p_seqno" );
	sb.append(", c.id_no" );
	sb.append(", c.id_no_code" );
	sb.append(", c.chi_name" );
	sb.append(", c.birthday");
	sb.append(", i.id_no as new_id_no " );
	sb.append(", i.id_no_code as new_id_no_code " );
	sb.append(", i.old_id_no");
	sb.append(", i.old_id_no_code ");
	
    wp.selectSQL = sb.toString();
    wp.daoTable = "crd_idno c left join crd_chg_id i on c.id_p_seqno = i.id_p_seqno";
    wp.whereOrder = " order by c.id_no,i.mod_time  ";

    pageQuery();
    if (sqlNotFind()) {
    	// 2022/04/18 Justin: crd_idno無值，dbc_idno有值 -> 畫面顯示dbc_idno join dbc_chg_id
		if (!getWhereStr2()) {
			return;
		}
		
		sb = new StringBuilder();
		sb.append("  c.id_p_seqno" );
		sb.append(", c.id_no" );
		sb.append(", c.id_no_code" );
		sb.append(", c.chi_name" );
		sb.append(", c.birthday");
		sb.append(", i.AFT_ID as new_id_no " );
		sb.append(", i.AFT_ID_CODE as new_id_no_code " );
		sb.append(", i.ID as old_id_no ");
		sb.append(", i.ID_CODE as old_id_no_code ");

		wp.selectSQL = sb.toString();
		wp.daoTable = " dbc_idno c LEFT JOIN dbc_chg_id i ON c.ID_NO = i.AFT_ID AND c.ID_NO_CODE = i.AFT_ID_CODE ";
		wp.whereOrder = " order by c.id_no,i.mod_time  ";

		pageQuery();
		if (sqlNotFind()) {
			alertErr(appMsg.errCondNodata);
	        return;
		}
		wp.notFound = "";
    }

    wp.setListCount(1);
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();

  }

  @Override
  public void querySelect() throws Exception {
    mExIdPSeqno = wp.itemStr("id_p_seqno");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    // m_ex_id_p_seqno = wp.item_ss("kk_id_no");
	if (empty(mExIdPSeqno)) {
		mExIdPSeqno = itemKk("data_k1");
		if (empty(mExIdPSeqno)) {
			mExIdPSeqno = wp.itemStr("id_p_seqno");
		}
	}
    
	StringBuilder sb = new StringBuilder();
	sb.append("hex(rowid) as rowid, mod_seqno ")
	  .append(", id_p_seqno ")
	  .append(", id_p_seqno as h_id_p_seqno" )
	  .append(", id_no" )
	  .append(", id_no_code")
	  .append(", id_no as h_id_no ")
	  .append(", id_no_code as h_id_no_code" )
	  .append(", chi_name")
	  .append(", birthday")
	  .append(", 'N' as isDebit ");

    wp.selectSQL =  sb.toString();
    wp.daoTable = "crd_idno";
    wp.whereStr = "where 1=1";
    wp.whereStr += " and  id_p_seqno = :id_p_seqno ";
    setString("id_p_seqno", mExIdPSeqno);

    pageSelect();
    if (sqlNotFind()) {
    	sb = new StringBuilder();
    	sb.append("hex(rowid) as rowid, mod_seqno ")
    	  .append(", id_p_seqno ")
    	  .append(", id_p_seqno as h_id_p_seqno" )
    	  .append(", id_no" )
    	  .append(", id_no_code")
    	  .append(", id_no as h_id_no ")
    	  .append(", id_no_code as h_id_no_code" )
    	  .append(", chi_name")
    	  .append(", birthday")
    	  .append(", 'Y' as isDebit ");
    	
		wp.selectSQL = sb.toString();
		wp.daoTable = "dbc_idno";
		wp.whereStr = "where 1=1";
		wp.whereStr += " and  id_p_seqno = :id_p_seqno ";
		setString("id_p_seqno", mExIdPSeqno);
		pageSelect();
		if (sqlNotFind()) {
			alertErr("查無資料, id_p_seqno=" + mExIdPSeqno);
		}
    }
  }

  @Override
  public void saveFunc() throws Exception {

    if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
      return;
    }

    // 2019/12/25 check id 檢核輸入的”身分證字號-新”邏輯是否正確
    boolean error = false;
    if (eqIgno(strAction, "A") || eqIgno(strAction, "U")) {
      String id = wp.itemStr("id_no");

      error = comcrd.checkId(id);
      if (error) {
        alertErr2("輸入的身分證字號邏輯有誤");
        return;
      }
    }

    Crdm0511Func func = new Crdm0511Func(wp);

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
