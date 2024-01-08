package ccam02;
/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-06-29  V1.00.01  ryan       program initial                            *
* 109-11-26  V1.00.02  ryan       調整dataCheck 端末機號必需為4碼                                           *
******************************************************************************/
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ccam5030 extends BaseEdit {
  Ccam5030Func func;
  String kkMccCode , kkMccLinkId;

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
      wp.colSet("risk_factor", "0");
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
  }

  @Override
  public void queryFunc() throws Exception {

    wp.whereStr = "where 1=1" 
        + sqlCol(wp.itemStr("ex_mcc_code"), "mcc_code")
        + sqlCol(wp.itemStr("ex_mcc_link_id"), "mcc_link_id")
        ;

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " hex(rowid) as rowid "
    			+ ", mcc_code "
    			+ ", mcc_link_id "
    			+ ", mcc_remark "
    			+ ", int_min_amt "
    			+ ", int_max_amt "
    			+ ", int_fix_amt "
    			+ ", int_percent "
    			+ ", cntry_code "
    			+ ", crt_date "
    			+ ", crt_user "
    			+ ", apr_date "
    			+ ", apr_user "
    			+ ", mod_user "
    			+ ", to_Char(mod_time,'yyyymmdd') as mod_date "
    			+ ", mod_pgm "
    			+ ", mod_seqno "
    			;
    wp.daoTable = "cca_mcc_egov_fee";
    if (empty(wp.whereStr)) {
        wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by mcc_code,mcc_link_id ";

    // sql_ddd();
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata);
      return;
    }

    // list_wkdata();
    wp.setPageValue();
  }

  @Override
  public void querySelect() throws Exception {
	  kkMccCode = wp.itemStr("data_k1");
	  kkMccLinkId = wp.itemStr("data_k2");
	  dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(kkMccCode)) {
    	kkMccCode = itemKk("mcc_code");
    }
    
    if (empty(kkMccLinkId)) {
    	kkMccLinkId = itemKk("mcc_link_id");
    }
    
    if (empty(kkMccCode)) {
      alertErr("MCC CODE 不可空白");
      return;
    }
    
    if(kkMccLinkId.trim().length()<4) {
    	errmsg("端末機號必需為4碼");
      	return ;
    }
    
    wp.selectSQL = " hex(rowid) as rowid "
			+ ", mcc_code "
			+ ", mcc_link_id "
			+ ", mcc_remark "
			+ ", int_min_amt "
			+ ", int_max_amt "
			+ ", int_fix_amt "
			+ ", int_percent "
			+ ", cntry_code "
			+ ", crt_date "
			+ ", crt_user "
			+ ", apr_date "
			+ ", apr_user "
			+ ", mod_user "
			+ ", to_Char(mod_time,'yyyymmdd') as mod_date "
			+ ", mod_pgm "
			+ ", mod_seqno "
			;
    wp.daoTable = "cca_mcc_egov_fee";
    wp.whereStr = "where 1=1" 
            + sqlCol(kkMccCode, "mcc_code")
            + sqlCol(kkMccLinkId, "mcc_link_id");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + kkMccCode + " ,key2=" + kkMccLinkId);
    }

  }

  @Override
  public void saveFunc() throws Exception {
    if (checkApproveZz() == false)
      return;
    func = new ccam02.Ccam5030Func(wp);

    rc = func.dbSave(strAction);
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
