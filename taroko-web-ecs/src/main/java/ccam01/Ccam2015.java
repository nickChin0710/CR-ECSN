package ccam01;
/**ccam2015 卡片偽卡停用維護
 * 2020-0107:  Ru    modify AJAX
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-06-16 V2.00.00  ryan             updated for project coding standard
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名  
 * 110-03-26 V1.00.02   Justin        add oppo_time                                                                                      *  
 **/ 

import busi.func.OutgoingOppo;
/** 偽卡停用維護
 * 2019-1219:  Alex  ptr_branch -> gen_brn
 * 2019-1210:  Alex  fix initButton
 * 2019-0610:  p_seqno >>acno_xxx
   190308:  JH    outgoing-Query
 * 2018-0712  Alex  fix oppo_user
 * 
 * */
import taroko.com.TarokoCommon;

public class Ccam2015 extends ofcapp.BaseAction {

  String cardNo = "", debitFlag = "", currentFlag = "";
  Ccam2010Func func = null;

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
    } else if (eqIgno(wp.buttonCode, "R2")) {
      strAction = "R";
      cardNo = wp.colStr("card_no");
      if (isEmpty(cardNo)) {
        alertErr("卡號: 不可空白");
        return;
      }
    //  outgoingQuery();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
      wp.colSet("pagetype", "list");
    }
    // 20200107 modify AJAX
    else if (eqIgno(wp.buttonCode, "AJAX")) {
      if ("1".equals(wp.getValue("ID_CODE"))) {
        wfAjaxOpptype();
      }
    }
  }

  @Override
  public void dddwSelect() {
    try {
      wp.optionKey = wp.colStr("oppo_reason");
      this.dddwList("dddw_oppo_reason", "cca_opp_type_reason", "opp_status", "opp_remark",
          " where onus_opp_type='5'");

      wp.optionKey = wp.colStr("mail_branch");
      dddwList("dddw_mail_branch", "gen_brn", "branch", "full_chi_name", " where 1=1");

      if (wp.colEq("bin_type", "V")) {
        wp.optionKey = wp.colStr("vis_area_1");
        dddwList("dddw_visa_area1", "cca_sys_parm2", "sys_key", "sys_data1",
            " where sys_id='VISA' and sys_key in('0','A','B','C','D','E','F')");
        wp.optionKey = wp.colStr("vis_area_2");
        dddwShare("dddw_visa_area2");
        wp.optionKey = wp.colStr("vis_area_3");
        dddwShare("dddw_visa_area3");
        wp.optionKey = wp.colStr("vis_area_4");
        dddwShare("dddw_visa_area4");
        wp.optionKey = wp.colStr("vis_area_5");
        dddwShare("dddw_visa_area5");
        wp.optionKey = wp.colStr("vis_area_6");
        dddwShare("dddw_visa_area6");
        wp.optionKey = wp.colStr("vis_area_7");
        dddwShare("dddw_visa_area7");
        wp.optionKey = wp.colStr("vis_area_8");
        dddwShare("dddw_visa_area8");
        wp.optionKey = wp.colStr("vis_area_9");
        dddwShare("dddw_visa_area9");
      } else if (wp.colEq("bin_type", "M")) {
        wp.optionKey = wp.colStr("mast_area_1");
        dddwList("dddw_mast_area1", "cca_sys_parm2", "sys_key", "sys_data1",
            " where sys_id='MAST'");
        wp.optionKey = wp.colStr("mast_area_2");
        dddwShare("dddw_mast_area2");
        wp.optionKey = wp.colStr("mast_area_3");
        dddwShare("dddw_mast_area3");
        wp.optionKey = wp.colStr("mast_area_4");
        dddwShare("dddw_mast_area4");
        wp.optionKey = wp.colStr("mast_area_5");
        dddwShare("dddw_mast_area5");
        wp.optionKey = wp.colStr("mast_area_6");
        dddwShare("dddw_mast_area6");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void initPage() {
    return;
  }

  @Override
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_idno")) {
      alertErr2("卡號, 身分證ID: 不可全部空白");
      return;
    }

    // wp.whereStr = "WHERE 1=1"
    // +" and current_code='0' ";
    // if (wp.item_empty("ex_card_no")) {
    // wp.whereStr +=" and id_p_seqno ="+wp.sqlID+"uf_idno_pseqno(:ex_idno)";
    // }
    // else {
    // wp.whereStr +=" and card_no =:ex_card_no";
    // }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    // this.daoTid ="A-";
    wp.sqlCmd = "select" + " card_no, 'N' as debit_flag," + " new_end_date, " + " current_code, "
        + " sup_flag, " + " bin_type, " + " card_type, " 
        + " combo_acct_no as bank_acct_no, " + " oppost_date ,oppost_reason," + " group_code , "
        + " uf_idno_name(id_p_seqno) as chi_name " + " from crd_card A" + " where 1=1 "
        + sqlCol(wp.itemStr2("ex_card_no"), "A.card_no");
    if (!wp.itemEmpty("ex_idno")) {
      wp.sqlCmd += " and exists (select 1 from crd_idno B where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"B.id_no")
      +" and B.id_p_seqno in (A.id_p_seqno,A.major_id_p_seqno ))";
    }
    // if (wp.item_empty("ex_idno")) {
    // wp.sqlCmd +=" and card_no =?";
    // ppp(1,wp.item_ss("ex_card_no"));
    // }
    // else {
    // wp.sqlCmd +=" and id_p_seqno in (select id_p_seqno from crd_idno where id_no =?)";
    // ppp(1,wp.item_ss("ex_idno"));
    // }
    // -debit-
    wp.sqlCmd += " union select card_no," + " 'Y' as debit_flag," + " new_end_date,"
        + " current_code," + " sup_flag," + " bin_type," + " card_type,"
        + " acct_no as bank_acct_no , " + " oppost_date , oppost_reason,"
        + " group_code , " + " uf_idno_name2(id_p_seqno,'Y') as chi_name " + " from  dbc_card A "
        + " where 1=1 " 
        + sqlCol(wp.itemStr2("ex_card_no"), "A.card_no");
    if (!wp.itemEmpty("ex_idno")) {
      wp.sqlCmd += " and A.id_p_seqno in (select B.id_p_seqno from dbc_idno B where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"B.id_no")+") ";
    }
    // if (wp.item_empty("ex_idno")) {
    // wp.sqlCmd += " and card_no =?";
    // ppp(2, wp.item_ss("ex_card_no"));
    // }
    // else {
    // wp.sqlCmd += " and id_p_seqno in (select id_p_seqno from dbc_idno where id_no =?)";
    // ppp(2, wp.item_ss("ex_idno"));
    // }

    wp.sqlCmd += " order by 1 Asc ";
    pageQuery();

    wp.setListCount(1);
    if (sqlNotFind()) {
      alertErr(appMsg.errCondNodata + " 或  無流通卡");
      return;
    }
    queryAfter();
    // list_wkdata();
    // wp.setPageValue();
    if (wp.selectCnt == 1) {
      cardNo = wp.colStr("card_no");
      debitFlag = wp.colStr("debit_flag");
      currentFlag = wp.colStr("current_code");
      wp.javascript(" detailScreen('1','" + cardNo + "','" + debitFlag + "','" + currentFlag + "'); ");
    }

    colReadOnly("cond_edit");
  }

  void queryAfter() throws Exception {
    String sql1 = " select " + " oppo_user , " + " logic_del_date , " + " logic_del_time , "
        + " logic_del_user , " + " renew_flag , " + " oppo_time " + " from cca_opposition " + " where card_no = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "card_no")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "oppost_user", sqlStr("oppo_user"));
        wp.colSet(ii, "logic_del_date", sqlStr("logic_del_date"));
        wp.colSet(ii, "logic_del_time", sqlStr("logic_del_time"));
        wp.colSet(ii, "logic_del_user", sqlStr("logic_del_user"));
        wp.colSet(ii, "renew_flag", sqlStr("renew_flag"));
        if (wp.colEmpty(ii, "oppost_date") == false) {
			wp.colSet(ii, "oppost_time", sqlStr("oppo_time"));
		}else {
			wp.colSet(ii, "oppost_time", "");
		}
      }
    }
	for (int ii = 0; ii < wp.selectCnt; ii++) {
		ecsfunc.CodeDescCcas cdds = new ecsfunc.CodeDescCcas();
		String oppostReason = wp.colStr(ii, "oppost_reason") + "_"
				+ cdds.oppoReason(wp.getConn(), wp.colStr(ii, "current_code"), wp.colStr(ii, "oppost_reason"));
		wp.colSet(ii, "tt_oppost_reason", oppostReason);
	}
  }

  boolean isvdOnlineOn(){
	  
	  String sqlSelect = "select wf_value from ptr_sys_parm where wf_parm = 'COMBO_VD_ONLINE_OPPOSITION' and wf_key = 'CCAM_2010' ";
	  sqlSelect(sqlSelect);
	  
	  return eqAny(sqlStr("wf_value"), "NO");
  }
  
  @Override
  public void querySelect() throws Exception {
    cardNo = wp.itemStr("data_k1");
    debitFlag = wp.itemStr2("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(cardNo))
      cardNo = wp.itemStr2("card_no");
    if (empty(cardNo)) {
      alertErr("卡號: 不可空白");
      return;
    }

    if (empty(debitFlag))
      debitFlag = wp.itemStr2("debit_flag");

    if (eqIgno(debitFlag, "Y")) {
      wp.selectSQL = " C.mod_seqno " + ", C.card_no, 'Y' as debit_flag " + ", C.id_p_seqno "
          + ", C.corp_p_seqno " + ", C.p_seqno as acno_p_seqno " + ", C.acct_type "
          + ", C.card_type " + ", C.bin_type " + ", C.current_code " + ", C.sup_flag "
          + ", C.new_end_date " + ", C.lost_fee_code " + ", 0 as sup_card_num "
          + ", A.chi_name as idno_name " + ", A.id_no " + ", A.birthday as bir_date "
          + ", A.home_area_code1,home_tel_no1,home_tel_ext1 "
          + ", A.office_area_code1,office_tel_no1,office_tel_ext1 "
          + ", A.cellar_phone as cell_phone " + ", B.acct_key " + ", B.acct_type "
          + ", B.bill_sending_zip " + ", B.bill_sending_addr1 " + ", B.bill_sending_addr2 "
          + ", B.bill_sending_addr3 " + ", B.bill_sending_addr4 " + ", B.bill_sending_addr5 "
          + ", uf_corp_no(C.corp_p_seqno) as corp_no "
          + ", decode(C.oppost_date,'',to_char(current date,'yyyymmdd'),C.oppost_date) as oppo_date "
          + ", decode(C.oppost_date,'',to_char(sysdate, 'HH24miss'), '') as oppo_time "
          + ", uf_date_add(C.new_end_date,0,0,1) as neg_del_date " + ", '' as excep_flag"
          + ", 'N' as son_card_flag " + ", C.group_code "
          + ", uf_tt_group_code(C.group_code) as tt_group_code" + ", C.acct_no as bank_actno "
          + ", C.oppost_date "
          + ", C.electronic_code " + ", C.reissue_status "
          + ", C.oppost_reason " + ", C.reg_bank_no "
          + ", C.oppost_reason as oppo_reason "
      	  + ", decode(C.electronic_code,'01','悠遊卡','02','一卡通','03','愛金卡','') as tt_electronic_code ";
      wp.daoTable = "dbc_idno A, dba_acno B, dbc_card C";
      wp.whereStr = "where 1=1" + " and A.id_p_seqno = C.id_p_seqno " + " and B.p_seqno = C.p_seqno"
          + " and C.card_no =?"; // " and C.card_no ='"+cardNo+"'";
      setString2(1, cardNo);
    } else {
      wp.selectSQL = "C.mod_seqno " + ", C.card_no, 'N' as debit_flag " + ", C.id_p_seqno "
          + ", C.corp_p_seqno " + ", C.acno_p_seqno " + ", C.acct_type " + ", C.card_type "
          + ", C.bin_type " + ", C.current_code, C.new_end_date " + ", C.sup_flag "
          + ", 0 as sup_card_num " + ", A.chi_name as idno_name " + ", A.id_no "
          + ", A.birthday as bir_date " + ", A.home_area_code1,home_tel_no1,home_tel_ext1 "
          + ", A.office_area_code1,office_tel_no1,office_tel_ext1 "
          + ", A.cellar_phone as cell_phone " + ", B.acct_key " + ", B.acct_type "
          + ", B.bill_sending_zip " + ", B.bill_sending_addr1 " + ", B.bill_sending_addr2 "
          + ", B.bill_sending_addr3 " + ", B.bill_sending_addr4 " + ", B.bill_sending_addr5 "
          + ", B.int_rate_mcode " + ", uf_corp_no(C.corp_p_seqno) as corp_no "
          + ", decode(C.oppost_date,'',to_char(current date,'yyyymmdd'),C.oppost_date) as oppo_date "
          + ", decode(C.oppost_date,'',to_char(sysdate, 'HH24miss'), '') as oppo_time "
          + ", uf_date_add(C.new_end_date,0,0,1) as neg_del_date " + ", C.lost_fee_code"
          + ", '' as excep_flag" + ", C.son_card_flag" + ", C.group_code "
          + ", uf_tt_group_code(C.group_code) as tt_group_code " + ", C.combo_acct_no as bank_actno "
          + ", C.oppost_date " + ", C.combo_indicator" + ", C.electronic_code " + ", C.reissue_status "
          + ", C.oppost_reason " + ", C.reg_bank_no "
          + ", C.oppost_reason as oppo_reason "
          + ", decode(C.electronic_code,'01','悠遊卡','02','一卡通','03','愛金卡','') as tt_electronic_code ";
      wp.daoTable = "crd_idno A, act_acno B, crd_card C";
      wp.whereStr = "where 1=1" + " and A.id_p_seqno = C.id_p_seqno "
          + " and B.acno_p_seqno = C.acno_p_seqno" + " and C.card_no =?";
      setString2(1, cardNo);
    }

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + cardNo);
      return;
    }
    dataReadAfter();
    func = new ccam01.Ccam2010Func();
    func.setConn(wp);
    func.selectCcaCardAcct();
    func.cntSupCard();
    func.cntTpanCard();
    func.selectOpposition();
    // outgoing_Query();
  }

  void outgoingQuery() {
    OutgoingOppo ooOutgo = new OutgoingOppo();
    ooOutgo.setConn(wp);
    ooOutgo.wpCallStatus("");
    if (empty(cardNo))
      return;
    // -NEG-
    ooOutgo.parmClear();
    ooOutgo.p1CardNo = cardNo;
    ooOutgo.p2BinType = wp.colStr("bin_type");
    ooOutgo.oppoNegId("5");
    
    //是否送國際組織為N return
    if(wp.itemEq("tt_excep_flag", "N")){
        return;
    }
    // -VMJ-
    String lsBinType = wp.colStr("bin_type");
    String lsOppoDate = wp.colStr("oppo_date");
    // String ls_neg_reason =wp.col_ss("mst_reason_code");
    String lsVisReason = wp.colStr("vis_reason_code");
    String lsArea = "";

    ooOutgo.p4Reason = lsVisReason;
    if (eqIgno(lsBinType, "M")) {
      ooOutgo.oppoMasterReq2("5");
    } else if (eqIgno(lsBinType, "J")) {
      // f_ftp2jcb_req(is_card_no,as_date,as_time,is_opp_date_ori,'5',is_jcb_reason,is_outgoing_area_ori)
      lsArea = wp.colStr("vis_area_1") + wp.colStr("vis_area_2") + wp.colStr("vis_area_3")
          + wp.colStr("vis_area_4") + wp.colStr("vis_area_5");
      ooOutgo.p5DelDate = lsOppoDate;
      ooOutgo.p7Region = lsArea;
      ooOutgo.oppoJcbReq("5");
    } else if (eqIgno(lsBinType, "V")) {
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
      // f_ftp2visa_req(is_card_no, as_date, as_time, is_opp_date_ori, '5', is_vis_reason,
      // is_outgoing_area_ori)
      ooOutgo.oppoVisaReq("5");
    }
  }

  void selectTypeChiname() {
    String sql1 = " select " + " chin_name " + " from vall_acct_type " + " where acct_type = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("acct_type")});
    if (sqlRowNum <= 0)
      return;
    wp.colSet("tt_acct_type", sqlStr("chin_name"));
  }

  void checkAutoload() {
    String sql1 =
        " select autoload_flag as tsc_autoload_flag from tsc_card where 1=1 and card_no = ? order by crt_date Desc "
            + commSqlStr.rownum(1);
    String sql2 =
        " select autoload_flag as ips_autoload_flag from ips_card where 1=1 and card_no = ? order by crt_date Desc "
            + commSqlStr.rownum(1);

    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
    if (sqlRowNum > 0)
      wp.colSet("tsc_autoload_flag", sqlStr("tsc_autoload_flag"));
    sqlSelect(sql2, new Object[] {wp.colStr("card_no")});
    if (sqlRowNum > 0)
      wp.colSet("ips_autoload_flag", sqlStr("ips_autoload_flag"));


  }

  void dataReadAfter() {
	String binType = wp.colStr("bin_type");  
    selectTypeChiname();
    checkAutoload();
    if (wp.colEmpty("oppost_date"))
      return;
    String sql1 = " select " + " oppo_type , " + " oppo_status , " + " neg_del_date , "
        + " opp_remark , " + " oppo_date ," + " oppo_time ," + " mod_seqno ," + " mail_branch ," + " renew_flag , "
        + " lost_fee_flag , " + " hex(rowid) as rowid  " + " from cca_opposition "
        + " where card_no = ? ";

    sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
    if (sqlRowNum <= 0)
      return;

//    wp.colSet("oppo_date", sqlStr("oppo_date"));
//    wp.colSet("oppo_reason", sqlStr("oppo_status"));
    wp.colSet("oppo_time", sqlStr("oppo_time"));
    wp.colSet("neg_del_date", sqlStr("neg_del_date"));
    wp.colSet("opp_remark", sqlStr("opp_remark"));
    wp.colSet("oppo_type", sqlStr("oppo_type"));
    wp.colSet("mod_seqno", sqlStr("mod_seqno"));
    wp.colSet("mail_branch", sqlStr("mail_branch"));
    wp.colSet("renew_flag", sqlStr("renew_flag"));
    wp.colSet("lost_fee_code", sqlStr("lost_fee_flag"));
    wp.colSet("rowid", sqlStr("rowid"));
    wfOpptype(sqlStr("oppo_status"),binType);
  }

  @Override
  public void saveFunc() throws Exception {

    if (this.isDelete()) {
      ccam01.Ccam2016Func func = new ccam01.Ccam2016Func();
      func.setConn(wp);
      rc = func.dbDelete();
      this.sqlCommit(rc);
      if (rc != 1) {
        alertErr2(func.getMsg());
      } else
        alertMsg("撤掛完成");
    } else {
      func = new Ccam2010Func();
      func.setConn(wp);

      if (isAdd())
        rc = func.dbInsert();
      else if (isUpdate())
        rc = func.ccam2012Update();
      this.sqlCommit(rc);
      if (rc != 1) {
        alertErr2(func.getMsg());
      } else {
        if (isAdd())
          alertMsg("卡片掛失停用成功");
        else if (isUpdate())
          alertMsg("修改成功");
      }
    }
    // if(this.isDelete()){
    // alert_msg("撤掛完成");
    // } else {
    // alert_msg("停用完成");
    // }
    this.saveAfter(false);
  }


  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      btnOnAud(false, false, false);
      if (wp.colEmpty("card_no"))
        return;
      if (wp.autUpdate() == false)
        return;

      if (wp.colEq("current_code", "0")) {
        btnOnAud(true, false, false);
      } else {
        btnOnAud(false, true, true);
      }
      if(isvdOnlineOn() && (!wp.colEq("combo_indicator", "N")||wp.colEq("debit_flag","Y"))) {
          btnOnAud(false, false, false);
      } 
    }
  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  // 20200107 modify AJAX
  public void wfAjaxOpptype() throws Exception {
    // super.wp = wr;

    if (itemIsempty("ax_oppo")) {
      wp.addJSON("tt_excep_flag", "N");
      return;
    }

    // String ls_winid =
    selectOpptypeReason(wp.itemStr("ax_oppo"));
    if (rc != 1) {
      wp.addJSON("tt_excep_flag", "N");
      return;
    }
    if (itemEq("ax_bin_type", "V") && empty(sqlStr("vis_excep_code")) == false) {
      wp.addJSON("excep_flag", "V");
      wp.addJSON("tt_excep_flag", "Y");
    } else if (itemEq("ax_bin_type", "M") && empty(sqlStr("mst_auth_code")) == false) {
      wp.addJSON("excep_flag", "M");
      wp.addJSON("tt_excep_flag", "Y");
    } else if (itemEq("ax_bin_type", "J") && empty(sqlStr("jcb_excp_code")) == false) {
      wp.addJSON("excep_flag", "J");
      wp.addJSON("tt_excep_flag", "Y");
    } else {
      wp.addJSON("tt_excep_flag", "N");
    }
  }
  public void wfOpptype(String oppo,String binType){
	  
	    if (empty(oppo)) {
	        wp.colSet("tt_excep_flag", "N");
	        return;
	      }

	      // String ls_winid =
	      selectOpptypeReason(oppo);
	      if (rc != 1) {
	        wp.colSet("tt_excep_flag", "N");
	        return;
	      }
	      if (binType.equals("V") && empty(sqlStr("vis_excep_code")) == false) {
	        wp.colSet("excep_flag", "V");
	        wp.colSet("tt_excep_flag", "Y");
	      } else if (binType.equals("M") && empty(sqlStr("mst_auth_code")) == false) {
	        wp.colSet("excep_flag", "M");
	        wp.colSet("tt_excep_flag", "Y");
	      } else if (binType.equals("J") && empty(sqlStr("jcb_excp_code")) == false) {
	        wp.colSet("excep_flag", "J");
	        wp.colSet("tt_excep_flag", "Y");
	      } else {
	        wp.colSet("tt_excep_flag", "N");
	      }
  }
  void selectOpptypeReason(String lsOppo) {

    String sql1 = "select vis_excep_code, mst_auth_code, jcb_excp_code"
        + " from cca_opp_type_reason" + " where 1=1 " + " and opp_status =?";
    sqlSelect(sql1, new Object[] {lsOppo});

  }

}
