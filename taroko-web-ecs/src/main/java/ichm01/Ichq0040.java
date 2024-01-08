/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-22  V1.00.01  YangFang   updated for project coding standard        *
*                                                                            *
******************************************************************************/
package ichm01;

import ofcapp.BaseAction;

public class Ichq0040 extends BaseAction {

  @Override
  public void userAction() throws Exception {
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
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 更新功能 */
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 刪除功能 */
      saveFunc();
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
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {	  	 
	  if (wp.itemEmpty("ex_send_date1") && wp.itemEmpty("ex_send_date2")) {
		  alertErr2("傳送日期:不可空白");
		  return;
	  }

	  if (chkStrend(wp.itemStr2("ex_send_date1"), wp.itemStr2("ex_send_date2")) == false) {
		  alertErr2("傳送日期:起迄錯誤");
		  return;
	  }

	  String lsWhere = " where 1=1 "
			  		 + sqlCol(wp.itemStr2("ex_send_date1"), "send_date",">=")
			  		 + sqlCol(wp.itemStr2("ex_send_date2"), "send_date","<=") 
			  		 + sqlCol(wp.itemStr("ex_refuse_type"),"refuse_type")
        			 ;
	  
	  if(wp.itemEq("ex_send_reason", "10")) {
		  lsWhere += " and from_type like '3%' ";
	  }	else if(wp.itemEq("ex_send_reason", "20")) {
		  lsWhere += " and from_type = '21' ";
	  }	else if(wp.itemEq("ex_send_reason", "30")) {
		  lsWhere += " and from_type = '22' ";
	  }	else if(wp.itemEq("ex_send_reason", "40")) {
		  lsWhere += " and from_type = '1' ";
	  }
	  
	  if (wp.itemEmpty("ex_idno") == false) {
		  lsWhere += " and card_no in "
				  + " (select A.card_no from crd_card A join crd_idno B  on A.id_p_seqno = B.id_p_seqno where 1=1 "
				  + sqlCol(wp.itemStr2("ex_idno"), "B.id_no") + ") ";
	  } else {
		  lsWhere += sqlCol(wp.itemStr2("ex_card_no"), "card_no");
	  }

	  if (wp.itemEq("ex_resp_code", "1")) {
		  lsWhere += " and rpt_resp_code='0' ";
	  } else if (wp.itemEq("ex_resp_code", "2")) {
		  lsWhere += " and rpt_resp_code='1' ";
	  }

	  wp.whereStr = lsWhere;
	  wp.queryWhere = wp.whereStr;
	  wp.setQueryMode();

	  queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.selectSQL = ""
            + " crt_date ,"
            + " crt_time ,"
            + " card_no ,"
            + " ich_card_no ,"
            + " txn_code ,"
            + " send_date ,"
            + " send_time ,"
            + " rpt_resp_code ,"
            + " rpt_resp_date ,"
            + " from_type , "
            + " refuse_type , "
            + " decode(from_type,'1','人工指定','20','批次拒絕代行取消','21','凍結','22','特指',from_type) as tt_from_type , "
            + " decode(refuse_type,'5','停卡','R','拒絕代行','Q','取消拒絕代行') as tt_refuse_type , "
            + " decode(rpt_resp_code,'0','成功','1','失敗') as tt_rpt_resp_code ";

    wp.daoTable = "ich_refuse_log";
    wp.whereOrder = " order by send_date , send_time , card_no ";
    pageQuery();

    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }

    wp.setListCount(0);
    wp.setPageValue();
    queryAfter(wp.listCount[0]);
  }
  
  void queryAfter(int ll_nrow) throws Exception {
	   for (int ll=0; ll<ll_nrow; ll++) {
	      String lsFrom=wp.colStr(ll,"from_type");
	      if (pos(",|31|,|32|,|33|,|34|,|35|,|36|","|"+lsFrom+"|")>0) {
	         String ss=commString.decode(lsFrom,"1,人工指定,20,批次拒絕代行取消,21,凍結,22,特指"
	               +",31,申停,32,掛失,33,強制停用,34,更換卡號停用,35,偽卡,36,毀損停用");
	         wp.colSet(ll,"tt_from_type",ss);
	         wp.colSet(ll,"from_type",lsFrom.substring(1));
	      }
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

  @Override
  public void saveFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

}
