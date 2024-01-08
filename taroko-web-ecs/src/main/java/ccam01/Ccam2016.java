package ccam01;
/** 卡片停用類別維護
 * 2019-1219:  Alex  ptr_branch -> gen_brn
 * 20191210:   Alex  fix initButton
 * 20190610:   JH    p_seqno >>acno_xxx
   19-0308:    JH    outgoing-Query()
   2018-1112:     JH    outgoing_Query()
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * * 109-01-04  V1.00.01   shiyuqi       修改无意义命名                                                                                      *  
 * 110-01-30     V1.00.02   Justin          取消單日撤掛限制
 * 110-03-26 V1.00.02   Justin        add oppo_time  
 * */

import busi.func.OutgoingOppo;
import ofcapp.BaseAction;

public class Ccam2016 extends BaseAction {
  String cardNo = "", debitFlag = "";

  @Override
  public void userAction() throws Exception {
    // if (eq_igno(wp.buttonCode, "X")) {
    // /* 轉換顯示畫面 */
    // is_action = "new";
    // clearFunc();
    // }
    // else
    if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      wp.colSet("pagetype", "list");
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      dataRead();
    } else if (eqIgno(wp.buttonCode, "A")) {
      /* 新增功能 */
      strAction = "A";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "U")) {
      /* 修改功能 */
      strAction = "U";
      saveFunc();
    } else if (eqIgno(wp.buttonCode, "D")) {
      /* 撤掛功能 */
      strAction = "D";
      saveFunc();
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
     // outgoingQuery();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
      wp.colSet("pagetype", "list");
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
      strAction = "AJAX";
      String lsAxType = wp.itemStr2("ax_type");
      if (eqIgno(lsAxType, "1"))
        wfAjaxOpptype();
      else if (eqIgno(lsAxType, "2"))
        wfAjaxOppreason();
    }


  }

  @Override
  public void dddwSelect() {
    if (wp.respHtml.indexOf("_detl") <= 0) {
      return;
    }

    try {
      wp.optionKey = wp.colStr("mail_branch");
      dddwList("dddw_mail_branch", "gen_brn", "branch", "full_chi_name", " where 1=1");

      if (!wp.colEmpty("oppo_type2")) {
        String lsWhere = "where 1=1" + sqlCol(wp.itemStr("oppo_type2"), "onus_opp_type");
        wp.optionKey = wp.itemStr("oppo_reason2");
        dddwList("dddw_oppo_reason2", "cca_opp_type_reason", "opp_status", "opp_remark", lsWhere);
      }

      // -VISA-
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
        // -MasterCard-
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
  public void queryFunc() throws Exception {
    if (wp.itemEmpty("ex_card_no") && wp.itemEmpty("ex_idno")) {
      alertErr2("卡號, 身分證ID: 不可全部空白");
      return;
    }

    // wp.whereStr = "WHERE 1=1";
    // if ( !wp.item_empty("ex_card_no")) {
    // wp.whereStr +=" and card_no in (select A.card_no from crd_card A, crd_idno B where
    // A.id_p_seqno =B.id_p_seqno"
    // +commSqlStr.col(wp.sss("ex_idno"),"B.id_no")
    // +" union select C.card_no from dbc_card C, dbc_idno D where C.id_p_seqno=D.id_p_seqno"
    // +commSqlStr.col(wp.sss("ex_idno"),"D.id_no")
    // +")";
    // }
    // else {
    // wp.whereStr +=commSqlStr.col(wp.sss("ex_card_no"),"card_no");
    // }

    // -page control-
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();
  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();

    wp.sqlCmd = "select" + " card_no, 'N' as debit_flag," + " new_end_date, " + " current_code, "
        + " sup_flag, " + " bin_type, " + " card_type, " 
        + " combo_acct_no as bank_acct_no, oppost_date,oppost_reason,"
        + " '' as oppo_date, '' as oppo_time, '' as oppo_remark" + " from crd_card"
        + " where current_code <>'0'" + sqlCol(wp.itemStr2("ex_card_no"), "card_no")
        + " and id_p_seqno in (select id_p_seqno from crd_idno where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"id_no")+") ";
//        + commSqlStr.inIdnoCrd("", wp.itemStr2("ex_idno"), "");
    wp.sqlCmd += " union select" + " card_no, 'Y' as debit_flag," + " new_end_date, "
        + " current_code, " + " sup_flag, " + " bin_type, " + " card_type, "
        + " acct_no as bank_acct_no, oppost_date,oppost_reason,"
        + " '' as oppo_date, '' as oppo_time, '' as opp_remark" + " from dbc_card"
        + " where current_code <>'0'" + sqlCol(wp.itemStr2("ex_card_no"), "card_no")
        + " and id_p_seqno in (select id_p_seqno from dbc_idno where 1=1 "+sqlCol(wp.itemStr("ex_idno"),"id_no")+") ";
    wp.sqlCmd += " order by oppost_date desc, card_no" + commSqlStr.rownum(999);

    pageQuery();

    wp.setListCount(1);
    if (sqlRowNum == 0) {
      alertErr("無停用卡片");
      return;
    }

    colReadOnly("cond_edit");

    listWkdata(sqlRowNum);
    wp.setPageValue();
    if (wp.selectCnt == 1) {
      cardNo = wp.colStr("card_no");
      debitFlag = wp.colStr("debit_flag");
      wp.javascript(" detailScreen('1','" + cardNo + "','" + debitFlag + "',''); ");
    }
  }

  void listWkdata(int llCnt) throws Exception {
    String sql1 = "";
    for (int ii = 0; ii < llCnt; ii++) {
      String lsCardNo = wp.colStr(ii, "card_no");
      if (empty(lsCardNo))
        continue;
      sql1 = "select oppo_date, " + " oppo_time, " + " opp_remark" + " from cca_opposition"
          + " where 1=1" + sqlCol(lsCardNo, "card_no");
      sqlSelect(sql1);
      if (sqlRowNum <= 0)
        continue;

//      wp.colSet(ii, "oppo_date", sqlStr("oppo_date"));
//      wp.colSet(ii, "oppo_time", sqlStr("oppo_time"));
      wp.colSet(ii, "opp_remark", sqlStr("opp_remark"));
      if (wp.colEmpty(ii, "oppost_date") == false) {
			wp.colSet(ii, "oppost_time", sqlStr("oppo_time"));
	  }else {
			wp.colSet(ii, "oppost_time", "");
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
    debitFlag = wp.itemStr("data_k2");
    dataRead();
  }

  @Override
  public void dataRead() throws Exception {
    wp.colSet("pagetype", "detl");

    if (isEmpty(cardNo)) {
      alertErr("卡號: 不可空白");
      return;
    }
    wp.dataClear("oppo_type2");
    wp.dataClear("oppo_reason2");

    if (empty(debitFlag))
      debitFlag = wp.itemStr2("debit_flag");

    if (eqIgno(debitFlag, "Y")) {
      wp.selectSQL = " hex(C.rowid) as rowid , C.mod_seqno " + ", C.card_no, 'Y' as debit_flag "
          + ", C.id_p_seqno " + ", C.corp_p_seqno " + ", C.p_seqno as acno_p_seqno"
          + ", C.acct_type " + ", C.card_type " + ", C.bin_type " + ", C.current_code "
          + ", C.sup_flag " + ", C.new_end_date " + ", C.lost_fee_code " + ", 0 as sup_card_num "
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
          + ", C.oppost_date  "
          + ", C.electronic_code " + ", C.reissue_status "
          + ", C.oppost_reason " + ", C.reg_bank_no "
          + ", C.oppost_reason as oppo_reason "
      	  + ", decode(C.electronic_code,'01','悠遊卡','02','一卡通','03','愛金卡','') as tt_electronic_code ";
      wp.daoTable = "dbc_idno A, dba_acno B, dbc_card C";
      wp.whereStr = "where 1=1" + " and A.id_p_seqno = C.id_p_seqno " + " and B.p_seqno = C.p_seqno"
          + " and C.card_no =?"; // " and C.card_no ='"+cardNo+"'";
      setString2(1, cardNo);
    } else {
      wp.selectSQL = " hex(C.rowid) as rowid , C.mod_seqno " + ", C.card_no, 'N' as debit_flag "
          + ", C.id_p_seqno " + ", C.corp_p_seqno " + ", C.acno_p_seqno " + ", C.acct_type "
          + ", C.card_type " + ", C.bin_type " + ", C.current_code, C.new_end_date "
          + ", C.sup_flag " + ", 0 as sup_card_num " + ", A.chi_name as idno_name " + ", A.id_no "
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
          + ", C.oppost_date " + ", C.combo_indicator"
          + ", C.reissue_status " + ", C.oppost_reason "+ ", C.electronic_code " + ", C.reg_bank_no "
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
    if (sqlRowNum <= 0) {
      alertErr("查無資料, key=" + cardNo);
      return;
    }

    // -H/O: tel_no-
    String telNo = wp.colStr("home_area_code1") + "-" + wp.colStr("home_tel_no1");
    if (colIsEmpty("home_tel_ext1") == false) {
      telNo += "-" + wp.colStr("home_tel_ext1");
    }
    wp.colSet("wk_telno_h", telNo);
    telNo = wp.colStr("office_area_code1") + "-" + wp.colStr("office_tel_no1");
    if (colIsEmpty("office_tel_ext1") == false) {
      telNo += "-" + wp.colStr("office_tel_ext1");
    }
    wp.colSet("wk_telno_o", telNo);
    
    telNo = wp.colStr("current_code");
    telNo = telNo + "." + ecsfunc.DeCodeCcas.ncccOpptype(telNo);
    wp.colSet("tt_oppo_type", telNo);

    ecsfunc.CodeDescCcas cdds = new ecsfunc.CodeDescCcas();
    telNo = wp.colStr("oppost_reason") + "."
        + cdds.oppoReason(wp.getConn(), wp.colStr("current_code"), wp.colStr("oppost_reason"));
    wp.colSet("tt_oppo_reason", telNo);
    
    dataReadAfter();

    selectCcaOpposition();
    cntSupCard();
    cntTpanCard();
    // 0308: outgoing_Query();

  }

  void dataReadAfter() {
    if (wp.colEq("debit_flag", "Y")) {
      String sql1 =
          " select spec_status as card_spec_status from cca_card_base where card_no = ? and debit_flag ='Y' ";
      sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
      if (sqlRowNum > 0)
        wp.colSet("card_spec_status", sqlStr("card_spec_status"));

      String sql2 =
          " select block_reason1||','||block_reason2||','||block_reason3||','||block_reason4||','||block_reason5 as block_reason15 , spec_status as acno_spec_status "
              + " from cca_card_acct where acno_p_seqno = ? and debit_flag ='Y' ";
      sqlSelect(sql2, new Object[] {wp.colStr("acno_p_seqno")});
      if (sqlRowNum > 0) {
        wp.colSet("block_reason15", sqlStr("block_reason15"));
        wp.colSet("acno_spec_status", sqlStr("acno_spec_status"));
      }
    } else {
      String sql1 =
          " select spec_status as card_spec_status from cca_card_base where card_no = ? and debit_flag <>'Y' ";
      sqlSelect(sql1, new Object[] {wp.colStr("card_no")});
      if (sqlRowNum > 0)
        wp.colSet("card_spec_status", sqlStr("card_spec_status"));
      String sql2 =
          " select block_reason1||','||block_reason2||','||block_reason3||','||block_reason4||','||block_reason5 as block_reason15 , spec_status as acno_spec_status "
              + " from cca_card_acct where acno_p_seqno = ? and debit_flag <>'Y' ";
      sqlSelect(sql2, new Object[] {wp.colStr("acno_p_seqno")});
      if (sqlRowNum > 0) {
        wp.colSet("block_reason15", sqlStr("block_reason15"));
        wp.colSet("acno_spec_status", sqlStr("acno_spec_status"));
      }
    }
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

  void selectCcaOpposition() throws Exception {
    String sql1 = "select A.*, oppo_type as tt_oppo_type," + " '' as tt_oppo_reason, "
        + " excep_flag as db_excep_flag_0 " + " from cca_opposition A" + " where card_no =?";
    setString(1, cardNo);
    daoTid = "AA.";
    this.sqlSelect(sql1);
    if (sqlRowNum <= 0) {
      return;
    }
//    wp.colSet("oppo_type", sqlStr("AA.oppo_type"));
//    wp.colSet("oppo_reason", sqlStr("AA.oppo_status"));
    wp.colSet("oppo_user", sqlStr("AA.oppo_user"));
//    wp.colSet("oppo_date", sqlStr("AA.oppo_date"));
    wp.colSet("oppo_time", sqlStr("AA.oppo_time"));
    wp.colSet("neg_del_date", sqlStr("AA.neg_del_date"));
    wp.colSet("renew_urgen", sqlStr("AA.renew_urgen"));
    wp.colSet("opp_remark", sqlStr("AA.opp_remark"));
    wp.colSet("mail_branch", sqlStr("AA.mail_branch"));
    wp.colSet("lost_fee_flag", sqlStr("AA.lost_fee_flag"));
    wp.colSet("db_excep_flag_0", sqlStr("AA.db_excep_flag_0"));
    wp.colSet("excep_flag", "");
    wp.colSet("mod_seqno", sqlStr("AA.mod_seqno"));
    wp.colSet("renew_flag", sqlStr("AA.renew_flag"));
    wp.colSet("chg_date", sqlStr("AA.chg_date"));
    wp.colSet("mst_reason_code", sqlStr("AA.mst_reason_code"));
    wp.colSet("vis_reason_code", sqlStr("AA.vis_reason_code"));
    wp.colSet("fisc_reason_code", sqlStr("AA.fisc_reason_code"));

    setVmjArea();
  }

  void cntSupCard() {
    wp.colSet("sup_card_num", "");
    if (wp.colEq("sup_flag", "1") || eqIgno(debitFlag, "Y")) {
      return;
    }
    String sql1 =
        "" + " where 1=1 "+sqlCol(cardNo,"major_card_no")+ "and current_code ='0' and sup_flag='1'";
    int llCnt = (int) this.selectCount("crd_card", sql1);
    wp.colSet("sup_card_num", "" + llCnt);
    return;
  }

  void cntTpanCard() {
    int liSumCnt = 0, liMobCnt = 0, liAppleCnt = 0;
    wp.colSet("tpan_card_num", "0");
    if (eqIgno(debitFlag, "Y"))
      return;
    String sql1 =
        " select count(*) as db_cnt from hce_card where card_no = ? and status_code = '0' ";
    sqlSelect(sql1, new Object[] {cardNo});
    liMobCnt = sqlInt("db_cnt");
    String sql2 =
        " select count(*) as db_cnt2 from oempay_card where card_no = ? and status_code = '0' ";
    sqlSelect(sql2, new Object[] {cardNo});
    liAppleCnt = sqlInt("db_cnt2");
    liSumCnt = liMobCnt + liAppleCnt;
    wp.colSet("tpan_card_num", liSumCnt);
  }

  void saveCheck() {
    if (this.isDelete()) {
//      if (wp.itemEq("oppo_date", this.getSysDate()) == false) {
//        alertErr2("不是當日停掛, 不可[撤掛]");
//        return;
//      }
      return;
    }

    return;
  }

  @Override
  public void saveFunc() throws Exception {

    saveCheck();
    if (rc != 1)
      return;

    Ccam2016Func func = new Ccam2016Func();
    func.setConn(wp);

    rc = func.dbSave(this.strAction);
    this.sqlCommit(rc);
    if (rc != 1) {
      alertErr2(func.getMsg());
      return;
    }

    if (this.isDelete()) {
      alertMsg("卡片[撤掛] 成功");
    } else
      this.alertMsg("卡片 [變更停用類別] 成功");
    this.saveAfter(false);
  }

  @Override
  public void procFunc() throws Exception {

  }

  @Override
  public void initButton() {
    if (wp.respHtml.indexOf("_detl") > 0) {
      btnOnAud(false, false, false);
      if (wp.autUpdate() == false)
        return;
      this.btnUpdateOn(!wp.colEmpty("card_no"));
      this.btnDeleteOn(!wp.colEmpty("card_no"));
    }
    if(isvdOnlineOn() && (!wp.colEq("combo_indicator", "N")||wp.colEq("debit_flag","Y"))) {
        this.btnUpdateOn(false);
        this.btnDeleteOn(false);
    } 
  }

  @Override
  public void initPage() {
    return;
  }

  public void wfAjaxOpptype() throws Exception {

    wp.addJSON("excep_flag", "");
    if (itemIsempty("ax_opp_type")) {
      return;
    }

    String lsOppType = wp.itemStr2("ax_opp_type");

    wp.sqlCmd = "select opp_status, opp_remark" + " from cca_opp_type_reason" + " where 1=1"
        + sqlCol(lsOppType, "ncc_opp_type") + " order by 1";
    this.sqlSelect();
    for (int ii = 0; ii < sqlRowNum; ii++) {
      wp.addJSON("OPTION_TEXT", sqlStr(ii, "opp_status") + "_" + sqlStr(ii, "opp_remark"));
      wp.addJSON("OPTION_VALUE", sqlStr(ii, "opp_status"));
    }
  }

  public void wfAjaxOppreason() throws Exception {
    String lsOppType = wp.itemStr2("ax_opp_type");
    String axOppReason = wp.itemStr2("ax_opp_reason");

    if (empty(axOppReason) || empty(lsOppType)) {
      return;
    }

    // String ls_winid =
    selectOpptypeReason(lsOppType, axOppReason);

    if (rc != 1) {
      return;
    }

    // wp.ddd("bin=%s, V=%s, M=%s, J=%s",wp.item_ss("ax_bin_type")
    // , sql_ss("vis_excep_code"),sql_ss("mst_auth_code"),sql_ss("jcb_excp_code"));

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

  void selectOpptypeReason(String lsOppType, String lsOp4Reason) {

    String sql1 = "select vis_excep_code, mst_auth_code, jcb_excp_code"
        + " from cca_opp_type_reason" + " where 1=1 " + " and opp_status =?";
    sqlSelect(sql1, new Object[] {lsOp4Reason});

  }

  void setVmjArea() {
    String lsExcepFlag = sqlStr("AA.excep_flag");
    if (empty(lsExcepFlag)) {
      return;
    }

    if (eqAny(lsExcepFlag, "V")) {
      wp.colSet("vis_area_1", sqlStr("AA.vis_area_1"));
      wp.colSet("vis_area_2", sqlStr("AA.vis_area_2"));
      wp.colSet("vis_area_3", sqlStr("AA.vis_area_3"));
      wp.colSet("vis_area_4", sqlStr("AA.vis_area_4"));
      wp.colSet("vis_area_5", sqlStr("AA.vis_area_5"));
      wp.colSet("vis_area_6", sqlStr("AA.vis_area_6"));
      wp.colSet("vis_area_7", sqlStr("AA.vis_area_7"));
      wp.colSet("vis_area_8", sqlStr("AA.vis_area_8"));
      wp.colSet("vis_area_9", sqlStr("AA.vis_area_9"));
      wp.colSet("vis_purg_date_1", sqlStr("AA.vis_purg_date_1"));
      return;
    }
    if (eqAny(lsExcepFlag, "M")) {
      wp.colSet("mast_area_1", sqlStr("AA.vis_area_1"));
      wp.colSet("mast_area_2", sqlStr("AA.vis_area_2"));
      wp.colSet("mast_area_3", sqlStr("AA.vis_area_3"));
      wp.colSet("mast_area_4", sqlStr("AA.vis_area_4"));
      wp.colSet("mast_area_5", sqlStr("AA.vis_area_5"));
      wp.colSet("mast_area_6", sqlStr("AA.vis_area_6"));
      wp.colSet("mast_date1", sqlStr("AA.vis_purg_date_1"));
      wp.colSet("mast_date2", sqlStr("AA.vis_purg_date_2"));
      wp.colSet("mast_date3", sqlStr("AA.vis_purg_date_3"));
      wp.colSet("mast_date4", sqlStr("AA.vis_purg_date_4"));
      wp.colSet("mast_date5", sqlStr("AA.vis_purg_date_5"));
      wp.colSet("mast_date6", sqlStr("AA.vis_purg_date_6"));
      return;
    }
    if (eqAny(lsExcepFlag, "J")) {
      wp.colSet("jcb_area_1", sqlStr("AA.vis_area_1"));
      wp.colSet("jcb_area_2", sqlStr("AA.vis_area_2"));
      wp.colSet("jcb_area_3", sqlStr("AA.vis_area_3"));
      wp.colSet("jcb_area_4", sqlStr("AA.vis_area_4"));
      wp.colSet("jcb_area_5", sqlStr("AA.vis_area_5"));
      wp.colSet("jcb_area_6", sqlStr("AA.vis_area_6"));
      wp.colSet("jcb_date1", sqlStr("AA.vis_purg_date_1"));
      return;
    }
  }

}
