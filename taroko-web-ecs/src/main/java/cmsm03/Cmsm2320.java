/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-08-10  V1.00.01  tanwei        新增                                                                                                       *
* 109-08-10  V1.00.02  tanwei        插入數據，刪除解除鎖定                                                                     *
* 109-09-08  V1.00.03  tanwei        原預借現金密碼錯誤次數重置日期修改                                               *
* 109-09-08  V1.00.04  shiyuqi       bug fix                                 *
* 109-09-09  v1.00.05  sunny         修改文字                                                                                               *
* 109-10-16  V1.00.06  tanwei        updated for project coding standard     *
* 109-11-09  V1.00.07  sunny         fix delete by card_no(old by rowid)     *
* 109-11-09  V1.00.08  Justin          prevent inserting the same card_no
******************************************************************************/
package cmsm03;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import ofcapp.BaseAction;

public class Cmsm2320 extends BaseAction {

  String isOppType = "1"; // 一般停用
  String cardNo = "", debitFlag = "", currentCode = "";
  double isAssetValue = 0;
  String sysDate = "",sysTime = "";
  ccam01.Ccam2010Func func = null;

  @Override
  public void userAction() throws Exception {

    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      wp.colSet("pagetype", "list");
      queryFunc();
    }  else if (eqIgno(wp.buttonCode, "A")) {
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
    }
  }

  @Override
  public void dddwSelect() {
    
  }

  @Override
  public void initPage() {
    wp.colSet("btnUpdate_disable", "");
  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_idno")) {
      alertErr2("卡號, 身分證ID: 不可全部空白");
      return;
    }
    
    String lsWhere = 
        " where a.ID_P_SEQNO=b.ID_P_SEQNO "
            + " and a.CURRENT_CODE='0'"
            + " and a.passwd_err_count > '0'"
            + sqlCol(wp.itemStr("ex_card_no"), "card_no");
            
    if (wp.itemEmpty("ex_idno") == false) {
      lsWhere += sqlCol(wp.itemStr("ex_idno"), "id_no");
    }

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    
    wp.selectSQL = " a.card_no , " + " a.new_end_date , " + " a.passwd_err_count , "
        + " b.id_no , " + " b.chi_name , " + " b.BIRTHDAY ";
    wp.daoTable = " crd_card a , crd_idno b"; 
    pageQuery();
    
    if (sqlNotFind()) {
      alertErr2("此條件查無資料");
      return;
    }
    wp.setListCount(1);
    //queryAfter();
    wp.setPageValue();
    
    /*
     * if (wp.selectCnt == 1) { cardNo = wp.colStr("a.card_no"); wp.javascript(" detailScreen('1','"
     * + cardNo + "'); "); }
     */

    colReadOnly("cond_edit");
  }

  void queryAfter() {
    String sql1 = " select " + " oppo_user , " + " logic_del_date , " + " logic_del_time , "
        + " logic_del_user " + " from cca_opposition " + " where card_no = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "A-card_no")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "A-oppo_user", sqlStr("oppo_user"));
        wp.colSet(ii, "A-logic_del_date", sqlStr("logic_del_date"));
        wp.colSet(ii, "A-logic_del_time", sqlStr("logic_del_time"));
        wp.colSet(ii, "A-logic_del_user", sqlStr("logic_del_user"));
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.resetInputData();
    wp.colSet("pagetype", "detl");

    if (isEmpty(cardNo)) {
      cardNo = wp.itemStr2("card_no");
    }
    if (isEmpty(cardNo)) {
      alertErr("卡號: 不可空白");
      return;
    }

    wp.selectSQL = "" + "  C.card_no " + ", C.id_p_seqno " + ", C.corp_p_seqno "+ ", HEX(C.rowid) as rowid"
        + ", C.acno_p_seqno " + ", C.acct_type " + ", C.card_type " + ", C.bin_type " + ", C.passwd_err_count_resetdate "
        + ", C.current_code " + ", C.sup_flag " + ", C.new_end_date " + ", C.lost_fee_code"
        + ", 0 as sup_card_num " + ", A.chi_name as idno_name " + ", A.id_no "
        + ", A.birthday as bir_date " + ", A.home_area_code1,home_tel_no1,home_tel_ext1 "
        + ", A.office_area_code1,office_tel_no1,office_tel_ext1 "
        + ", A.cellar_phone as cell_phone " + ", B.acct_key " + ", B.bill_sending_zip "
        + ", B.bill_sending_addr1 " + ", B.bill_sending_addr2 " + ", B.bill_sending_addr3 "
        + ", B.bill_sending_addr4 " + ", B.bill_sending_addr5 "
        + ", uf_corp_no(C.corp_p_seqno) as corp_no " + ", C.passwd_err_count"
        + ", C.son_card_flag" + ", C.group_code "
        + ", uf_tt_group_code(C.group_code) as tt_group_code "
        + ", B.acct_type";
    wp.daoTable = "crd_idno A, act_acno B, crd_card C";
    wp.whereStr = "where 1=1" + " and A.id_p_seqno = C.id_p_seqno "
        + " and B.acno_p_seqno = C.acno_p_seqno" + sqlCol(cardNo, "C.card_no"); 

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNo);
      return;
    }
    dataReadAfter();
    wp.colSet("action_code", "Q");
  }

  void dataReadAfter() {
    selectTypeChiname();
    wp.colSet("wk_telno_h", wp.colStr("home_area_code1") + "-" + wp.colStr("home_tel_no1") + "-"
        + wp.colStr("home_tel_ext1"));
    wp.colSet("wk_telno_o", wp.colStr("office_area_code1") + "-" + wp.colStr("office_tel_no1") + "-"
        + wp.colStr("office_tel_ext1"));
    
    cntSupCard();
    isApproved();

  }
  
  public void cntSupCard() {
    if (wp.colEq("sup_flag", "1")) {
      return;
    }

    String sql1 = "select count(*) as ll_cnt from crd_card" + " where major_card_no =?"
        + " and current_code ='0' and sup_flag='1'";
    setString2(1, wp.colStr("card_no"));
    sqlSelect(sql1);
    if (sqlRowNum > 0) {
      wp.colSet("sup_card_num", sqlStr("ll_cnt"));
    }
  }

  void selectTypeChiname() {
    String sql1 = " select " + " chin_name " + " from vall_acct_type " + " where acct_type = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});
    if (sqlRowNum <= 0)
      return;
    wp.colSet("tt_acct_type", sqlStr("chin_name"));
  }
  
  boolean isApproved() {
    String sql = "SELECT  HEX(rowid) as cms_rowid, * FROM CMS_CARD_PWCNTRESET WHERE apr_flag<>'Y' AND card_no=:card_no";
    setString("card_no", cardNo);
    sqlSelect(sql);
    wp.colSet("cms_rowid", sqlStr("cms_rowid"));
    wp.colSet("MOD_DATE", sqlStr("MOD_DATE"));
    wp.colSet("MOD_USER", sqlStr("MOD_USER"));
    wp.colSet("APR_FLAG", sqlStr("APR_FLAG"));
    if (sqlRowNum > 0) {
      alertMsg("此筆資料待覆核.....");
      btnOnAud(false, false, true);
      return false;
    }else {
      return true;
    }

  }

  @Override
  public void saveFunc() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyyMMdd");
    sysDate = df.format(new Date());
    df = new SimpleDateFormat("hhmmss");
    sysTime = df.format(new Date());
      if (eqIgno(wp.buttonCode, "A")){
        insertPwcntreset();
      }
      if (eqIgno(wp.buttonCode, "D")){
        deletePwcntreset();
      }
       
  }
  
  int  insertPwcntreset() {
	
	cardNo = wp.itemStr("card_no");
	boolean isApr = isApproved();
	if ( ! isApr) {
		errmsg("此筆資料已是待覆核狀態");
		return rc;
	}
	  
    int modSeqnoInt = 1;
    String sql1 = " select " + " max(mod_seqno) as mod_seqno "
        + " from CMS_CARD_PWCNTRESET " + " where card_no = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
    
    if (sqlRowNum > 0) {
      String modSeqno = sqlStr("mod_seqno");
      if(!empty(modSeqno)) {
        modSeqnoInt = Integer.parseInt(modSeqno) + 1;
      }
    }
    String strSql = "insert into CMS_CARD_PWCNTRESET (" + " MOD_PGM , " + " MOD_SEQNO , " + " MOD_DATE , "
        + " MOD_TIME , " + " MOD_USER , " + " APR_FLAG , " + " APR_USER , " + " APR_DATE , "
        + " APR_TIME , " + " ID_P_SEQNO ," + " CARD_NO ," + " OLD_PASSWD_ERR_COUNT ," + " OLD_PW_ERR_COUNT_RESETDATE ,"
        + " PASSWD_ERR_COUNT ," + " PASSWD_ERR_COUNT_RESETDATE" + ") values (" + ":MOD_PGM, "
        + " :mod_seqno , " + ":MOD_DATE , " + ":MOD_TIME , " + ":mod_user, " + ":APR_FLAG , " + ":APR_USER, "
        + ":APR_DATE, " + ":APR_TIME, " + ":id_p_seqno, " + ":card_no, " + ":old_passwd_err_count, "
        + ":old_pw_err_count_resetdate, " + ":PASSWD_ERR_COUNT , "+ ":PASSWD_ERR_COUNT_RESETDATE " + " )";
    
    if(!wp.itemEmpty("mod_seqno")) {
      modSeqnoInt = Integer.parseInt(wp.itemStr("mod_seqno")) + 1;
    }
    
    setString("MOD_PGM", "Cmsm2320");
    setString("APR_FLAG", "N");
    setString("MOD_DATE", sysDate);
    setString("MOD_TIME", sysTime);
    setString("PASSWD_ERR_COUNT", "0");
    
    setString("APR_USER", "");
    setString("APR_DATE", "");
    setString("APR_TIME", "");
    setString("PASSWD_ERR_COUNT_RESETDATE", "");
    
    setString("mod_seqno", String.valueOf(modSeqnoInt));
    setString("mod_user", wp.loginUser);
    setString("id_p_seqno", wp.itemStr("id_p_seqno"));
    setString("card_no", wp.itemStr("card_no"));
    setString("old_passwd_err_count", wp.itemStr("passwd_err_count"));
    setString("old_pw_err_count_resetdate", wp.itemStr("passwd_err_count_resetdate"));
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }
    isApproved();
    return rc;
  }
  
  int deletePwcntreset(){
    //String sqlStr = "delete CMS_CARD_PWCNTRESET where HEX(rowid)=:cms_rowid ";
    //setString("cms_rowid", wp.itemStr("cms_rowid"));
	  String sqlStr = "delete CMS_CARD_PWCNTRESET where apr_flag='N' and card_no = :cms_cardno ";
	  setString("cms_cardno", wp.colStr("card_no"));
    sqlExec(sqlStr);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }
    return rc;
  }

  boolean selectAssetValue() {
    String sql1 = "";
    if (wp.itemEq("debit_flag", "Y")) {
      sql1 = " select asset_value from dbc_idno where id_no = ? ";
    } else {
      sql1 = " select asset_value from crd_idno where id_no = ? ";
    }

    sqlSelect(sql1, new Object[] {wp.itemStr("id_no")});

    if (sqlRowNum <= 0)
      return true;
    isAssetValue = sqlNum("asset_value");
    if (isAssetValue == 0)
      return true;

    return false;
  }

  @Override
  public void initButton() {
      //btnOnAud(false, true, false);

      //btnModeAud("XX");
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }
}
