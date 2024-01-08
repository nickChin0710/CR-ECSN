/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/08/06  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-30  V1.00.03   shiyuqi       修改无意义命名             
* 110-11-02  V1.00.03  machao     SQL Injection                             
* 112/12/27  V1.00.04  Ryan       modify                                           *  
***************************************************************************/
package mktq02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktq4270 extends BaseEdit {
  // ************************************************************************
  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;

    strAction = wp.buttonCode;
    if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
      strAction = "A";
      insertFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
      strAction = "U";
      updateFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
      deleteFunc();
    } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
      queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
      strAction = "";
      clearFunc();
    }

    dddwSelect();
    initButton();
  }

  // ************************************************************************
  @Override
  public void queryFunc() throws Exception {
    // -page control-
	queryWhere();  
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  // ************************************************************************
  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.selectSQL = " hex(a.rowid) as rowid " ;
    wp.selectSQL += " ,a.id_no ,a.chi_name ,a.use_month,a.free_tot_cnt,a.card_no  ";
    wp.selectSQL += " ,decode(DEBT_FLAG,'Y',c.birthday,b.birthday) as birthday,a.tot_amt,a.mod_type ";
    wp.selectSQL += " ,decode(DEBT_FLAG,'Y','DEBIT卡','信用卡')  as comm_debt_flag ";
    wp.daoTable = " mkt_ticket_card a left join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
    wp.daoTable += " left join dbc_idno c on a.id_p_seqno = c.id_p_seqno ";
    wp.whereOrder = " order by a.id_no ";

    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    wp.setPageValue();
    
    queryReadAfter();
  }
  
  private void queryReadAfter() throws Exception {
	  queryWhere();  
	  wp.sqlCmd = "select count(*) tot_cnt,sum(a.tot_amt) sum_tot_amt from mkt_ticket_card a ";
	  wp.sqlCmd += wp.whereStr;
	  sqlSelect();
	  wp.colSet("tot_cnt", sqlNum("tot_cnt"));
	  wp.colSet("sum_tot_amt", sqlNum("sum_tot_amt"));
  }

  // ************************************************************************
  @Override
  public void querySelect() throws Exception {
    dataRead();
  }

  // ************************************************************************
  @Override
  public void dataRead() throws Exception {

  }

  // ************************************************************************
  public void saveFunc() throws Exception {

  }

  // ************************************************************************
  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  // ************************************************************************
  @Override
  public void dddwSelect() {}

  // ************************************************************************
  void queryWhere() throws Exception {
	wp.whereStr = "WHERE 1=1 ";
	String exUseMonth = wp.itemStr("ex_use_month");
	String exCardNo = wp.itemStr("ex_card_no");
	String exIdNo = wp.itemStr("ex_id_no");
	String exDebtFlag = wp.itemStr("ex_debt_flag");
	if(!empty(exUseMonth)) {
		wp.whereStr += " and a.use_month = :exUseMonth ";
		setString("exUseMonth",exUseMonth);
	}
	if(!empty(exCardNo)) {
		wp.whereStr += " and a.card_no = :exCardNo ";
		setString("exCardNo",exCardNo);
	}
	if(!empty(exIdNo)) {
		wp.whereStr += " and a.id_no = :exIdNo ";
		setString("exIdNo",exIdNo);
	}
	if(!empty(exDebtFlag)) {
		wp.whereStr += " and decode(a.debt_flag,'','N',a.debt_flag) = :exDebtFlag ";
		setString("exDebtFlag",exDebtFlag);
	}

  }

  // ************************************************************************
  public void checkButtonOff() throws Exception {
    return;
  }

  // ************************************************************************
  @Override
  public void initPage() {
    return;
  }
  // ************************************************************************

}  // End of class
