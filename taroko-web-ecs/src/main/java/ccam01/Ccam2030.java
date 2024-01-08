package ccam01;
/**ccam2030 簡訊內容明細檔維護
 * 2020-0107:  Ru    modify AJAX
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
  * 109-12-30  V1.00.03  shiyuqi       修改无意义命名                                                                                     *  
 **/

import busi.func.OutgoingBlock;
/** 特殊指示戶維護(卡片)
 * 2019-1223   JH    UAT
 * 2019-0611: JH    p_seqno >>acno_pxxx
 * */
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Ccam2030 extends BaseAction {
  Ccam2030Func func;
  String cardNo = "", dataK2 = "", lsWhere1 = "", lsWhere2 = "";

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
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 動態查詢 */
      selectSmsFlag(wp.itemStr("card_no"));
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R2";
      cardNo = wp.colStr("card_no");
      if (empty(cardNo)) {
        alertErr("卡號: 不可空白");
        return;
      }
      outgoingQuery();
    }
    // 20200107 modify AJAX
    else if (eqIgno(wp.buttonCode, "AJAX")) {
      if ("1".equals(wp.getValue("ID_CODE"))) {
        wfAjaxFunc1();
      }
    } else if (eqIgno(wp.buttonCode, "C")) {
//    	procFunc();
    }
    
  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_idno") && wp.itemEmpty("ex_card_no")) {
      errmsg("身份證ID, 卡號: 不可全部空白");
      return;
    }

    lsWhere1 = " where current_code ='0' and new_end_date >= to_char(sysdate,'yyyymmdd') ";
    lsWhere2 = " where current_code ='0' and new_end_date >= to_char(sysdate,'yyyymmdd') ";

    if (!wp.itemEmpty("ex_card_no")) {
      lsWhere1 += sqlCol(wp.itemStr("ex_card_no"), "card_no");
      lsWhere2 += sqlCol(wp.itemStr("ex_card_no"), "card_no");
    } else if (!wp.itemEmpty("ex_idno")) {
      lsWhere1 += " and major_id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"id_no")+")";
      lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"id_no")+")";
    }

    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {

    wp.pageControl();

    wp.sqlCmd = " select " + " 'N' as debit_flag , " + " card_no , " + " acct_type , "
        + " card_type , " + " new_end_date , " + " sup_flag , " + " '' as spec_status , "
        + " uf_corp_no(corp_p_seqno) as corp_no , " + " uf_idno_id(id_p_seqno) as id_no , "
        + " bank_actno , " + " combo_acct_no as acct_no " + " from crd_card " + lsWhere1
        + " union " + " select " + " 'Y' as debit_flag , " + " card_no , " + " acct_type , "
        + " card_type , " + " new_end_date , " + " sup_flag , " + " '' as spec_status , "
        + " uf_corp_no(corp_p_seqno) as corp_no , " + " uf_idno_id(id_p_seqno) as id_no , "
        + " bank_actno , " + " acct_no " + " from dbc_card " + lsWhere2 + " order by card_no";

    pageQuery();

    if (sqlRowNum <= 0) {
      alertErr2("該卡己停掛 or 該卡己過期");
      return;
    }
    queryAfter();
    wp.setListCount(1);
    wp.setPageValue();
  }

  void queryAfter() {
    String sql1 = " select " + " spec_status , " + " spec_user , " + " spec_date , "
        + " spec_del_date " + " from cca_card_base " + " where card_no = ? ";

    String sql2 = " select " + " spec_desc " + " from cca_spec_code " + " where spec_code = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no")});
      if (sqlRowNum <= 0)
        continue;
      wp.colSet(ii, "spec_status", sqlStr("spec_status"));
      wp.colSet(ii, "spec_user", sqlStr("spec_user"));
      wp.colSet(ii, "spec_date", sqlStr("spec_date"));
      wp.colSet(ii, "spec_del_date", sqlStr("spec_del_date"));
      sqlSelect(sql2, new Object[] {wp.colStr(ii, "spec_status")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_spec_status", sqlStr("spec_desc"));
      }
    }
  }

  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    dataK2 = wp.itemStr("data_k2");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNo)) {
      cardNo = wp.itemStr("card_no");
    }

    if (empty(dataK2)) {
      dataK2 = wp.itemStr("debit_flag");
    }

    if (eqIgno(dataK2, "N")) {
      wp.selectSQL = " hex(B.rowid) as rowid , " + " A.bin_type , " + " A.card_no , "
          + " A.card_type , " + " A.bank_actno , " + " B.spec_status , "
          + " uf_corp_no(A.corp_p_seqno) as corp_no , " + " uf_idno_id(A.id_p_seqno) as id_no , "
          + " B.spec_mst_vip_amt , " + " B.spec_remark , " + " B.spec_user , " + " B.spec_date , "
          + " to_char(A.mod_time ,'yyyymmdd') as mod_date , " + " A.mod_user , " + " A.mod_pgm , "
          + " A.new_end_date , " + " uf_date_add(A.new_end_date,0,0,1) as new_end_date_add , "
          + " 'N' as debit_flag , " + " A.group_code , "
          + " uf_tt_group_code(A.group_code) as tt_group_code , " + " A.combo_acct_no as acct_no , "
//          + " decode(B.spec_del_date,'',to_char(sysdate + 3 months , 'yyyymmdd'),B.spec_del_date) as spec_del_date , "
//          + " decode(B.spec_del_date,'',to_char(sysdate + 3 months , 'yyyymmdd'),B.spec_del_date) as spec_del_date_o , "
		  + " decode(B.spec_del_date,'',A.new_end_date,B.spec_del_date) as spec_del_date , "
		  + " decode(B.spec_del_date,'',A.new_end_date,B.spec_del_date) as spec_del_date_o , "
          + " B.spec_dept_no , A.acct_type , A.acno_p_seqno , A.id_p_seqno , A.corp_p_seqno ";      	  
      wp.daoTable = " crd_card A , cca_card_base B ";
      wp.whereStr = " where 1=1 " + " and A.card_no = B.card_no " + sqlCol(cardNo, "A.card_no");
    } else if (eqIgno(dataK2, "Y")) {
      wp.selectSQL = " hex(B.rowid) as rowid , " + " A.bin_type , " + " A.card_no , "
          + " A.card_type , " + " A.bank_actno , " + " A.acct_no ,  " + " B.spec_status , "
          + " uf_corp_no(A.corp_p_seqno) as corp_no , " + " uf_idno_id(A.id_p_seqno) as id_no , "
          + " B.spec_mst_vip_amt , " + " B.spec_remark , " + " B.spec_user , " + " B.spec_date , "
          + " to_char(A.mod_time ,'yyyymmdd') as mod_date , " + " A.mod_user , " + " A.mod_pgm , "
          + " A.new_end_date , " + " uf_date_add(A.new_end_date,0,0,1) as new_end_date_add , "
          + " 'Y' as debit_flag , " + " A.group_code , "
          + " uf_tt_group_code(A.group_code) as tt_group_code , "
//          + " decode(B.spec_del_date,'',to_char(sysdate + 3 months , 'yyyymmdd'),B.spec_del_date) as spec_del_date , "
//          + " decode(B.spec_del_date,'',to_char(sysdate + 3 months , 'yyyymmdd'),B.spec_del_date) as spec_del_date_o , "
		  + " decode(B.spec_del_date,'',A.new_end_date,B.spec_del_date) as spec_del_date , "
		  + " decode(B.spec_del_date,'',A.new_end_date,B.spec_del_date) as spec_del_date_o , "
          + " B.spec_dept_no , A.acct_type , A.p_seqno as acno_p_seqno , A.id_p_seqno , A.corp_p_seqno ";
      wp.daoTable = " dbc_card A , cca_card_base B ";
      wp.whereStr = " where A.card_no = B.card_no " + sqlCol(cardNo, "A.card_no");
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNo);
      return;
    }

    selectSmsFlag(wp.colStr("card_no"));
    selectCellarPhone(wp.colStr("id_no"),wp.colStr("debit_flag"));
    outgoingQuery();
  }
  
  void selectCellarPhone(String idNo , String debitFlag) {
	  String sql1 = "";
	  
	  if(debitFlag.equals("Y")) {
		  sql1 = "select cellar_phone , chi_name from dbc_idno where id_no = ?";
	  }	else	{
		  sql1 = "select cellar_phone , chi_name from crd_idno where id_no = ?";
	  }
	  
	  sqlSelect(sql1,new Object[] {idNo});
	  if(sqlRowNum>0) {
		  wp.colSet("cellar_phone", sqlStr("cellar_phone"));	
		  wp.colSet("chi_name", sqlStr("chi_name"));
	  }
	  
	  return ;	  
  }
  
  void outgoingQuery() {
    OutgoingBlock ooOutgo = new OutgoingBlock();
    ooOutgo.setConn(wp);
    ooOutgo.wpCallStatus("");
    if (empty(cardNo))
      return;
    if (eqIgno(strAction, "R2") == false)
      return;

    // -NEG-
    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.blockNegId("5");
    // -VMJ-
    String lsBinType = wp.colStr("bin_type");
    String lsOppoDate = wp.colStr("oppo_date");
    // String ls_neg_reason =wp.col_ss("mst_reason_code");
    String lsVisReason = wp.colStr("vis_reason_code");
    String lsArea = "";

    ooOutgo.p2BinType = lsBinType;
    ooOutgo.p4Reason = lsVisReason;
    if (eqIgno(lsBinType, "M")) {
      ooOutgo.blockVmjReq("5");
    } else if (eqIgno(lsBinType, "J")) {
      lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
          + wp.colStr("vis_area_4") + wp.colStr("vis_area_5");
      ooOutgo.p5DelDate = lsOppoDate;
      ooOutgo.p7Region = lsArea;
      ooOutgo.blockVmjReq("5");
    } else {
      lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
          + wp.colStr("vis_area_4") + wp.colStr("vis_area_5") + wp.colStr("vis_area_6")
          + wp.colStr("vis_area_7") + wp.colStr("vis_area_8") + wp.colStr("vis_area_9");
      ooOutgo.p5DelDate = lsOppoDate;
      if (empty(lsArea)) {
        ooOutgo.p4Reason = "41";
        ooOutgo.p7Region = "0" + commString.space(8);
      } else {
        ooOutgo.p7Region = lsArea;
      }
      ooOutgo.blockVmjReq("5");
    }
  }

  @Override
  public void saveFunc() throws Exception {
    if (eqIgno(wp.respHtml, "ccam2030_detl")) {
      String lsCardNo = wp.itemStr2("card_no");
      String lsDebitFlag = wp.itemStr2("debit_flag");
      func = new Ccam2030Func();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      this.sqlCommit(rc);
      if (rc != 1) {
        alertErr2(func.getMsg());
      } else
        this.saveAfter(false);
      wp.colSet("card_no", lsCardNo);
      wp.colSet("debit_flag", lsDebitFlag);
    } 
  }

  @Override
  public void procFunc() throws Exception {
    ccam01.Ccam2030Func func = new ccam01.Ccam2030Func();
    func.setConn(wp);
    String lsCardNo = wp.itemStr2("card_no");
    String lsDebitFlag = wp.itemStr2("debit_flag");
    rc = func.dataProc();
    sqlCommit(rc);
    
    if(rc!=1) {
    	alertErr2(func.getMsg());
    }	else	{
    	this.saveAfter(false);
    	wp.itemSet("card_no", lsCardNo);
        wp.itemSet("debit_flag", lsDebitFlag);
        dataRead();
        wp.respMesg = "發送簡訊完成 !";
    }    
  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      this.btnModeAud();
    }
  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  @Override
  public void dddwSelect() {
    try {
      if (wp.respHtml.indexOf("_detl") > 0) {
        wp.optionKey = wp.colStr("spec_status");
        dddwList("ddw_spec_code_list", "cca_spec_code", "spec_code", "spec_desc",
            "where spec_type ='2'");

        wp.optionKey = wp.colStr("spec_dept_no");
        dddwList("dddw_spec_dept", "ptr_dept_code", "dept_code", "dept_name", "where 1=1");

      }
    } catch (Exception ex) {
    }

    try {
      if ((wp.respHtml.equals("ccam2030_nadd"))) {
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("msg_dept").length() > 0) {
          wp.optionKey = wp.colStr("msg_dept");
        }
        this.dddwList("dddw_dept_code", "ptr_dept_code", "trim(dept_code)", "trim(dept_name)",
            " where 1 = 1 ");
        wp.optionKey = "";
        wp.initOption = "";
        if (wp.colStr("ex_id").length() > 0) {
          wp.optionKey = wp.colStr("ex_id");
        }

        this.dddwList("dddw_msg_ex",
            "select trim(msg_id) as db_code , " + "trim(msg_id)||'  '||trim(msg_desc) as db_desc "
                + "from sms_msg_id where apr_date <>'' ");
      }
    } catch (Exception ex) {
    }
  }

  // 20200107 modify AJAX
  public void wfAjaxFunc1() throws Exception {
    // super.wp = wr;

    if (wp.itemStr("ax_win_ex_id").length() == 0)
      return;

    selectAjaxFunc10(wp.itemStr("ax_win_ex_id"));

    if (rc != 1) {
      wp.addJSON("msg_userid", "");
      wp.addJSON("msg_id", "");
      wp.addJSON("msg_desc", "");
      // wp.addJSON("chi_name_flag","");
      return;
    }

    wp.addJSON("msg_userid", sqlStr("msg_userid"));
    wp.addJSON("msg_id", sqlStr("msg_id"));
    wp.addJSON("msg_desc", sqlStr("msg_desc"));
    // wp.addJSON("chi_name_flag",sql_ss("chi_name_flag"));
  }

  // ************************************************************************
  void selectAjaxFunc10(String msgId) {
    wp.sqlCmd = " select " + " a.msg_userid ," + " a.msg_id ," + " a.msg_desc "
    // + " a.chi_name_flag as chi_name_flag "
        + " from  sms_msg_id a " + " where 1=1 "+sqlCol(msgId,"a.msg_id");;

    this.sqlSelect();
    if (sqlRowNum <= 0)
      alertErr2("簡訊範例[" + msgId + "]查無資料");

    return;
  }

  void selectData() {
    wp.sqlCmd = " select " + " a.cellar_phone as cellar_phone ," + " a.chi_name as chi_name ,"
        + " a.id_p_seqno as id_p_seqno " + " from  crd_idno a " + " where 1=1 "+sqlCol(wp.itemStr("id_no"),"a.id_no");

    this.sqlSelect();
    if (sqlRowNum <= 0)
      alertErr2("持卡者ID:[" + wp.itemStr("id_no") + "]查無資料");

    wp.colSet("cellar_phone", sqlStr("cellar_phone"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));

    return;
  }

  void selectSmsFlag(String lsCardNo) {
    String sql1 = " select " + " sms_flag " + " from cca_special_visa " + " where card_no = ? ";

    sqlSelect(sql1, new Object[] {lsCardNo});

    if (sqlRowNum <= 0) {
      wp.colSet("sms_flag", "N");
      return;
    }

    wp.colSet("sms_flag", sqlStr("sms_flag"));

  }

}
