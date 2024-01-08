package ccam01;
/*****************************************************************************

*                              MODIFICATION LOG                              *
*                                                                            *
*   DATE      Version   AUTHOR      DESCRIPTION                              *
*  109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
*/

import ofcapp.AppMsg;
import ofcapp.BaseQuery;
import taroko.com.TarokoCommon;


public class Ccaq3020 extends BaseQuery {

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
    if (this.chkStrend(wp.itemStr("ex_date1"), wp.itemStr("ex_date2")) == false) {
      alertErr2("交易日期起迄：輸入錯誤");
      return;
    }
    if (empty(wp.itemStr("ex_mcht_no")) && empty(wp.itemStr("ex_date1"))
        && empty(wp.itemStr("ex_date2"))) {
      alertErr2("條件不可全部空白");
      return;
    }
    wp.whereStr = " where 1=1" + sqlCol(wp.itemStr("ex_mcht_no"), "mcht_no")
        + sqlCol(wp.itemStr("ex_date1"), "tx_date", ">=")
        + sqlCol(wp.itemStr("ex_date2"), "tx_date", "<=");
    
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = "card_no," + "tx_date, " + "tx_time," + "nt_amt , " + "auth_type , "
        + "auth_no , " + "auth_user , " + "auth_status_code , " + " stand_in , " + " mcht_no , "
    	+ "mcht_name "
    	;
    wp.daoTable = "cca_auth_txlog";
    wp.whereOrder = " order by tx_date ASC, tx_time ASC ";
    pageQuery();
    
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.setListCount(0);
    wp.setPageValue();
    
    queryAfter();
  }

  void queryAfter() {
	  String sql1 = "select bin_type from cca_card_base where 1=1 and card_no = ? ";
	  String sql2 = "select mcht_name from cca_mcht_bill where acq_bank_id = ? and mcht_no = ? and bin_type = ? ";	  
	  for(int ii=0;ii<wp.selectCnt;ii++) {
		  sqlSelect(sql1,new Object[] {wp.colStr(ii,"card_no")});
		  if(sqlRowNum<=0 || empty(sqlStr("bin_type"))) continue ;
		  sqlSelect(sql2,new Object[] {wp.colStr(ii,"stand_in"),wp.colStr(ii,"mcht_no"),sqlStr("bin_type")});		  
		  if(sqlRowNum<=0 || empty(sqlStr("mcht_name"))) continue ;
		  wp.colSet(ii,"mcht_name", sqlStr("mcht_name"));		  		  		  
	  }
	  
  }
  
  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    // TODO Auto-generated method stub

  }

}
