/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-02-28   V1.00.01  Zuwei       add OnlineApprove                          *
* 109-04-20   V1.00.02  Tanwei     updated for project coding standard     *
* 109-12-08   V1.00.03  Justin        add RCRATE_DAY_PLUS                      *
* 109-12-10   V1.00.04  Justin        add old_rcrate_day_plus
* 110-01-05   V1.00.05  Justin        updated for XSS
* 112-07-31   V1.00.06  Ryan       信評等級不能修改
******************************************************************************/

package ptrm01;

import ofcapp.BaseEdit;
import taroko.base.CommString;
import taroko.com.TarokoCommon;

public class Ptrm0010 extends BaseEdit {
  CommString commString = new CommString();

  @Override
  public void actionFunction(TarokoCommon wr) throws Exception {
    super.wp = wr;
    rc = 1;
    strAction = wp.buttonCode;
    specInputCharParser();
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
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
      // } else if (eq_igno(wp.buttonCode, "M")) {
      // /* 瀏覽功能 :skip-page */
      // queryRead();
    } else if (eqIgno(wp.buttonCode, "S")) {
      /* 動態查詢 */
      querySelect();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }
    
    specOutputCharParser();

    initButton();
  }

  private void specInputCharParser() {		
		if ( ! wp.itemEmpty("ex_holding_period")) {
			String exHoldingPeriod = wp.itemStr("ex_holding_period");
			exHoldingPeriod = commString.decode(exHoldingPeriod, ",&gt=N,&ltN,X", ",>=N,<N,X");
			wp.itemSet("ex_holding_period", exHoldingPeriod);
			wp.colSet("ex_holding_period", exHoldingPeriod);
		}
	}
	private void specOutputCharParser() {		
		if ( ! wp.colEmpty("ex_holding_period")) {
			String exHoldingPeriod = wp.colStr("ex_holding_period");
			wp.colSet("ex_holding_period_desc", exHoldingPeriod);
			exHoldingPeriod = commString.decode(exHoldingPeriod, ",>=N,<N,X", ",&gt=N,&ltN,X");
			wp.colSet("ex_holding_period", exHoldingPeriod);
		}
	}

@Override
  public void queryFunc() throws Exception {
    String creditRating = wp.itemStr("ex_credit_rating");
    String holdingPeriod = wp.itemStr("ex_holding_period");
//    holdingPeriod = commString.decode(holdingPeriod, ",&gt=N,&ltN,X", ",>=N,<N,X");
    wp.whereStr = " where 1=1 ";
    if (eqIgno(creditRating, "0") == false) {
      wp.whereStr += "and ptr_rcrate.credit_rating = :credit_rating ";
      setString("credit_rating", creditRating);
    }
    if (eqIgno(holdingPeriod, "0") == false) {
      setString("holding_period", holdingPeriod);
      wp.whereStr += "and ptr_rcrate.holding_period = :holding_period ";
    }
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();
    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL =
        "credit_rating, " + "holding_period, " + "rating,  " 
     + "to_char(rcrate_year, '99.99') as rcrate_year , " 
     + "to_char(rcrate_day, '0.999') as rcrate_day , " 
     + "to_char(rcrate_day_plus, '0.999') as rcrate_day_plus " ;
    wp.daoTable = " ptr_rcrate ";
    wp.whereOrder =
        " order by credit_rating,rating, decode(holding_period,'>=N','1','<N','2','X','3','4') ";
    pageQuery();
    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }
    wp.totalRows = wp.dataCnt;
    wp.listCount[1] = wp.dataCnt;
    wp.setPageValue();
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      String holding = wp.colStr(ii, "holding_period");
//      wp.colSet(ii, "holding_period", commString.decode(holding, ",>=N,<N,X", ",&gt=N,&ltN,X"));
    }
  }

  @Override
  public void querySelect() throws Exception {
    String creditRating = wp.itemStr("data_k1");
    String holdingPeriod = wp.itemStr("data_k2");
    wp.setInBuffer("ex_credit_rating", new String[] {creditRating});
    wp.setInBuffer("ex_holding_period", new String[] {holdingPeriod});
    wp.colSet("ex_credit_rating", creditRating);
    wp.colSet("ex_holding_period", holdingPeriod);
//    holdingPeriod = commString.decode(holdingPeriod, ",&gt=N,&ltN,X", ",>=N,<N,X");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    String creditRating = wp.itemStr("ex_credit_rating");
    String holdingPeriod = wp.itemStr("ex_holding_period");
//    holdingPeriod = commString.decode(holdingPeriod, ",&gt=N,&ltN,X", ",>=N,<N,X");
    if(eqIgno(creditRating,"0")) {
    	creditRating = wp.itemStr("credit_rating");
    }
    if(eqIgno(holdingPeriod,"0")) {
    	holdingPeriod = wp.itemStr("holding_period");
    }
    if (eqIgno(creditRating, "0") == true) {
      errmsg("信評等級：不可空白");
      return;
    }
    if (eqIgno(holdingPeriod, "0") == true) {
      errmsg("持卡年限：不可空白");
      return;
    }
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno," + "credit_rating, " + "holding_period, "
        + "rating as db_rating,'display:none' as rating_hide ," 
    	+ "to_char(rcrate_year, '99.99') as rcrate_year, " 
    	+ "to_char(rcrate_day_plus, '0.999') as old_rcrate_day_plus, " 
        + "to_char(rcrate_day, '0.999') as rcrate_day, " 
    	+ "to_char(rcrate_day_plus, '0.999') as rcrate_day_plus, " 
        + "mod_user, "
        + "to_char(mod_time,'yyyy/mm/dd') as mod_time_desc," + "mod_time ";
    wp.daoTable = " ptr_rcrate ";
    wp.whereStr =
        " where 1=1 and credit_rating = :creditRating and holding_period = :holdingPeriod";
    // 設置sql參數值
    setString("creditRating", creditRating);
    setString("holdingPeriod", holdingPeriod);

    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, creditRating=" + creditRating + ", holdingPeriod=" + holdingPeriod);
      return;
    }
//    wp.colSet("holding_period_desc",
//        commString.decode(wp.colStr("holding_period"), ",>=N,<N,X", ",&gt=N,&ltN,X"));
    wp.colSet("holding_period_desc",wp.colStr("holding_period"));
  }

  @Override
  public void saveFunc() throws Exception {
    if (eqIgno(strAction, "U") || eqIgno(strAction, "D")) {
//      wp.colSet("holding_period_desc",
//          commString.decode(wp.itemStr("holding_period"), ",>=N,<N,X", ",&gt=N,&ltN,X"));
      wp.colSet("holding_period_desc",wp.colStr("holding_period"));
    }
    // 覆核
    if (this.checkApproveZz() == false) {
      return;
    }
    String creditRating = wp.itemStr("ex_credit_rating");
    String holdingPeriod = wp.itemStr("ex_holding_period");
//    holdingPeriod = commString.decode(holdingPeriod, ",&gt=N,&ltN,X", ",>=N,<N,X");

    Ptrm0010Func func = new Ptrm0010Func();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    }
    this.sqlCommit(rc);
    // 設置保存後是否重新獲取記錄詳情並展示
    // this.addRetrieve = true;
    this.updateRetrieve = true;
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }
}
