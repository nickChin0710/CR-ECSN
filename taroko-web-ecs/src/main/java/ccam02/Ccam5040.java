package ccam02;
/* 國家別維護
 * 2019-1224  V1.00.01  Alex  querywhere like
 * 2020-0420  V1.00.02 yanghan 修改了變量名稱和方法名稱
 * */

import ofcapp.AppMsg;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;


public class Ccam5040 extends BaseEdit {
  Ccam5040Func func;
  String countryCode , ccasLinkType , binCountry;

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
    // wp.whereStr="where country_code >='"+wp.item_ss("ex_country_code")+"'"
    // +" and country_remark like '"+wp.item_ss("ex_country_remark")+"%'";
    wp.whereStr = "where 1=1 and ccas_link_type = 'FISC' " 
    	+ sqlCol(wp.itemStr("ex_country_code"), "country_code", "like%")
    	+ sqlCol(wp.itemStr("ex_bin_country"), "bin_country", "like%")
        + sqlCol(wp.itemStr("ex_country_remark"), "country_remark", "%like%")
        
        ;

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    wp.selectSQL = " ccas_link_type ," + " country_code ," + " country_remark ," + " rej_code ,"
        + " mcht_flag ," + " crt_date ," + " crt_user ,"
        + " to_char(mod_time,'yyyymmdd') as mod_date ," + " mod_user ," + " risk_factor , high_risk ,"
        + " decode(ccas_link_type,'V','VISA','M','MASTERCARD','J','JCB',ccas_link_type) as tt_ccas_link_type , "
        + " bin_country , country_no "
        ;
    wp.daoTable = "cca_country";
    wp.whereOrder = " order by country_code";
    if (empty(wp.whereStr)) {
      wp.whereStr = "ORDER BY 1";
    }

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
    countryCode = wp.itemStr("data_k1");
    ccasLinkType = wp.itemStr("data_k2");
    binCountry = wp.itemStr("data_k3");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(countryCode)) {
      countryCode = itemKk("country_code");
    }
    
    if (empty(ccasLinkType)) {
    	ccasLinkType = "FISC";
    }
    
    if (empty(binCountry)) {
    	binCountry = itemKk("bin_country");
    }
    
    if (isEmpty(countryCode)) {
      alertErr("ISO國家碼(2碼): 不可空白");
      return;
    }
    
    if(isEmpty(ccasLinkType)) {
    	alertErr("授權連線組織 不可空白");
    	return ;
    }
    
    if(isEmpty(binCountry)) {
    	alertErr("ISO國家碼(3碼): 不可空白");
    	return ;
    }
    
    wp.selectSQL = "hex(rowid) as rowid, mod_seqno, " + "ccas_link_type" + ", country_code"
        + ", country_remark" + ", rej_code" + ", mcht_flag" + ", crt_user" + ", crt_date"
        + ", mod_user, uf_2ymd(mod_time) as mod_date , risk_factor , high_risk ,"
        + " decode(ccas_link_type,'V','VISA','M','MASTERCARD','J','JCB',ccas_link_type) as tt_ccas_link_type , "
        + " bin_country , country_no "
        ;
    wp.daoTable = "cca_country";
    wp.whereStr = "where 1=1" 
    			+ sqlCol(countryCode, "country_code") 
    			+ sqlCol(ccasLinkType,"ccas_link_type")
    			+ sqlCol(binCountry,"bin_country")
    			;
    
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + countryCode);
    }

  }

  @Override
  public void saveFunc() throws Exception {
    if (checkApproveZz() == false)
      return;
    func = new ccam02.Ccam5040Func(wp);

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
