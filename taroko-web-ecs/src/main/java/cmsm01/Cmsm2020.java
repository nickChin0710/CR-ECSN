package cmsm01;
/** 法人相關資料維護
 * 109-12-23     JustinWu   fix where statement
 * 109-08-28    JustinWu   ++ autopay_id_and_desc
 * 109-08-07    JustinWu   fix 關係人頁面bug 
 * 109-08-05    JustinWu   cancel check idno
 * 109-07-29    JustinWu  ++ajax function and checkIdnoOrCorpno
 * 109-06-30    tanwei       身份證和統一編號校驗 
 * 109-06-29    zuwei         負責人ID欄位增加ID檢核邏輯
 * 109-06-28    zuwei         聯絡人增加主管復核功能
 * 109-06-22    sunny        fix alertErr
 * 109-04-19    shiyuqi       updated for project coding standard 
 * V01.05 JustinWu 109-02-12  modify ajax and saveFunc
 * V01.04 Alex 108-12-31      add dddw
 * V01.03 Alex 108-12-25      add corp online approve
 * V01.02 Alex 108-12-09      ptr_branch -> gen_brn
 * V01.01 Alex 108-12-05 		add ajax , add dddw , initButton
 * 2019-0613:  JH    p_xxx >>acno_p_xxx
 * v01.00 Alex 107-05-14
 * V00.00 JH 106-11xx
  * 109-12-30  V1.00.01  shiyuqi       修改无意义命名                                                                                     *
 * */

import java.util.Arrays;

import busi.ecs.CommBusiCrd;
import busi.func.EcsComm;
import ofcapp.BaseAction;

public class Cmsm2020 extends BaseAction {
  String corpPSeqno = "", corpPSeqno1 = "";
  CommBusiCrd commBusiCrd = new CommBusiCrd();

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
        break;
      case "X2":
        /* 轉換顯示畫面 */
        strAction = "new2";
        // clearFunc();
        break;
      case "Q":
        /* 查詢功能 */
        strAction = "Q";
        queryFunc();
        break;
      case "R":
        // -資料讀取-
        strAction = "R";
        dataRead();
        break;
      case "R2":
        // -關係人資料讀取-
        strAction = "R";
        dataReadRela();
        break;
      case "R3":
        // -Acno資料讀取-
        strAction = "R";
        dataReadAcno();
        break;
      case "A":
        /* 新增功能 */
        saveFunc();
        break;
      case "U":
        /* 更新功能 */
        saveFunc();
        break;
      case "D":
        /* 刪除功能 */
        saveFunc();
        break;
      case "M":
        /* 瀏覽功能 :skip-page */
        queryRead();
        break;
      case "S":
        /* 動態查詢 */
        querySelect();
        break;
      case "S1":
        /* 動態查詢 */
        querySelectACNO();
        break;
      case "S2":
        /* 動態查詢 */
        querySelectCONTR();
        break;
      case "S3":
        /* 動態查詢 */
        querySelectCURR();
        break;
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C":
        // -資料處理-
        procFunc();
        break;
      case "U1":
        // -關係人異動-
        procFuncRela();
        break;
      case "AJAX":
    	if(wp.itemEq("methodCode", "1")) {
    		checkIdnoAJAX();
    	}else {
    		wfAjaxKey();
    	}
        break;
    }
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsm2020_corp")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "business_code");
        dddwList("dddw_business_code", "CRD_MESSAGE", "msg_value", "msg",
            "where MSG_TYPE ='BUS_CODE'");
        // wp.initOption = "--";
        wp.optionKey = wp.colStr("reg_zip");
        dddwList("dddw_reg_zip",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
        // wp.initOption = "--";
        wp.optionKey = wp.colStr("comm_zip");
        dddwList("dddw_comm_zip",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
      }
    } catch (Exception ex) {
    }

    // // 2020-02-14 JustinWu
    // try {
    // if (eq_igno(wp.respHtml, "cmsm2020_acno")) {
    // wp.initOption = "--";
    // wp.optionKey = wp.col_ss(0, "bill_sending_zip");
    // dddw_list("d_dddw_zipcode", "select zip_code as db_code , zip_code||' '||zip_city||'
    // '||zip_town as db_desc from ptr_zipcode where 1=1 ");
    // }
    // }
    // catch (Exception ex) {
    // }

    try {
      if (eqIgno(wp.respHtml, "cmsm2020_acno")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "risk_bank_no");
        dddwList("d_dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1");
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "reg_bank_no");
        dddwList("d_dddw_reg_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "cmsm2020_curr")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "kk_curr_code");
        dddwList("dddw_curr_code", "ptr_sys_idtab", "wf_id", "wf_desc",
            "where wf_type = 'DC_CURRENCY'");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "cmsM2020_contr")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "contact_zip");
        dddwList("d_dddw_zipcode",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "cmsm2020_add")) {
        wp.optionKey = wp.colStr(0, "business_code");
        dddwList("d_dddw_bus_code", "CRD_MESSAGE", "msg_value", "msg",
            "where MSG_TYPE ='BUS_CODE'");
        wp.optionKey = wp.colStr(0, "acct_type");
        dddwList("d_dddw_accttype", "ptr_acct_type", "acct_type", "acct_type",
            "where 1=1 and card_indicator='2' order by acct_type");
        wp.optionKey = wp.colStr(0, "reg_zip");
        dddwList("dddw_reg_zip",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
        wp.optionKey = wp.colStr(0, "comm_zip");
        dddwList("dddw_comm_zip",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
        wp.optionKey = wp.colStr(0, "stmt_cycle");
        dddwList("d_dddw_cycle", "ptr_workday", "stmt_cycle", "stmt_cycle", "where 1=1");
        wp.optionKey = wp.colStr(0, "risk_bank_no");
        dddwList("dddw_risk_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1");
        wp.optionKey = wp.colStr(0, "reg_bank_no");
        dddwList("dddw_reg_bank_no", "gen_brn", "branch", "full_chi_name", "where 1=1");
        // wp.optionKey = wp.col_ss(0, "bill_sending_zip");
        // dddw_list("d_dddw_zipcode", "ptr_zipcode", "zip_code", "zip_code", "where 1=1");
        wp.optionKey = wp.colStr(0, "contact_zip");
        dddwList("d_dddw_contact_zip", "ptr_zipcode", "zip_code", "zip_code", "where 1=1");
        wp.optionKey = wp.colStr(0, "autopay_acct_bank");
        dddwList("d_dddw_bankcode", "act_ach_bank", "substring(bank_no,1,3)", "bank_name",
            "where 1=1");
      }
    } catch (Exception ex) {
    }
  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = "";
    if (!empty(wp.itemStr("ex_corp_no"))) {
        lsWhere += sqlCol(wp.itemStr("ex_corp_no"), "corp_no");
      }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += sqlCol(wp.itemStr("ex_chi_name"), "chi_name", "like%");
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    daoTid = "A.";
    wp.sqlCmd = "SELECT corp_no ,  corp_p_seqno ,  chi_name  FROM crd_corp"
              + " where 1=1 " + lsWhere;
    logSql();
    this.pageQuery();
    wp.setListCount(1);

    lsWhere = "";
    if (!empty(wp.itemStr("ex_corp_no"))) {
        lsWhere += sqlCol(wp.itemStr("ex_corp_no"), "B.corp_no");
      }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += sqlCol(wp.itemStr("ex_chi_name"), "B.chi_name", "like%");
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    lsWhere += " and A.acno_flag ='2' ";

    daoTid = "B.";
    wp.sqlCmd =
              "SELECT A.acno_p_seqno , A.acct_type , A.acct_key , A.corp_p_seqno ,"
            + " A.card_indicator , B.chi_name FROM act_acno A, crd_corp B "
            + " where A.corp_p_seqno = B.corp_p_seqno " + lsWhere + " order by A.acct_key";
    logSql();
    this.pageQuery();
    if (sqlRowNum > 0)
      queryAfter2();
    wp.setListCount(2);

    lsWhere = "";
    if (!empty(wp.itemStr("ex_corp_no"))) {
        lsWhere += sqlCol(wp.itemStr("ex_corp_no"), "corp_no");
      }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += sqlCol(wp.itemStr("ex_chi_name"), "chi_name", "like%");
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    daoTid = "C.";
    wp.sqlCmd = "SELECT corp_no ,  corp_p_seqno ,  chi_name  FROM crd_corp"
        + " where 1=1 " + lsWhere;
    logSql();
    this.pageQuery();
    wp.setListCount(3);
    
    lsWhere = "";
    if (!empty(wp.itemStr("ex_corp_no"))) {
        lsWhere += sqlCol(wp.itemStr("ex_corp_no"), "corp_no");
      }

    if (!empty(wp.itemStr("ex_chi_name"))) {
        lsWhere += sqlCol(wp.itemStr("ex_chi_name"), "chi_name", "like%");
      }

    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    daoTid = "D.";
    wp.sqlCmd = "SELECT corp_no ,  corp_p_seqno ,  chi_name  FROM crd_corp"
        + " where 1=1 " + lsWhere;
    logSql();
    this.pageQuery();
    wp.setListCount(4);

  }

  void queryAfter2() {
    String sql1 = " select  chin_name  from ptr_acct_type  where acct_type = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "B.acct_type")});
      if (sqlRowNum <= 0)
        continue;
      wp.colSet(ii, "B.tt_acct_type", "." + sqlStr("chin_name"));
    }
  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    corpPSeqno = wp.itemStr("data_k1");
    dataRead();

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(corpPSeqno)) {
      corpPSeqno = wp.itemStr("corp_p_seqno");
    }
    wp.selectSQL = " *  ";
    wp.daoTable = "crd_corp";
    wp.whereStr = "where 1=1" + sqlCol(corpPSeqno, "corp_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno);
      return;
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] commZipArr = commString.splitZipCode(wp.colStr("comm_zip"));
    wp.colSet("comm_zip", commZipArr[0]);
    wp.colSet("comm_zip2", commZipArr[1]);
    String[] regZipArr = commString.splitZipCode(wp.colStr("reg_zip"));
    wp.colSet("reg_zip", regZipArr[0]);
    wp.colSet("reg_zip2", regZipArr[1]);
    String[] contactZipArr = commString.splitZipCode(wp.colStr("contact_zip"));
    wp.colSet("contact_zip", contactZipArr[0]);
    wp.colSet("contact_zip2", contactZipArr[1]);

    wp.selectSQL = " *  ";
    wp.daoTable = "crd_corp_ext";
    wp.whereStr = "where 1=1" + sqlCol(corpPSeqno, "corp_p_seqno");
    pageSelect();
    if (sqlNotFind()) {
      this.selectOK();
    }

    cmsm01.Cmsm2020Corp func = new cmsm01.Cmsm2020Corp();
    func.setConn(wp);
    // ddd("A:" + func.select_moddata_tmp());
    if (func.selectModdataTmp() == 1) {
      alertMsg("此筆資料待覆核.....");
    }
  }

  public void dataReadRela() throws Exception {

    wp.selectSQL = " * , "
    		     + " rela_type||id_no as mix_data , "
    		     + " '' as mod_type , "
                 + " decode(rela_type,'1','實質受益人','2','高階管理人','3','具控制權人') as tt_rela_type ";
    wp.daoTable = "crd_corp_rela";
    wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("corp_no"), "corp_no");

    pageQuery();
    if (sqlNotFind()) {
      selectOK();
      wp.colSet("ind_num", "0");
    }else {
    	for (int i = 0; i < wp.selectCnt ; i++) {
    		setRelaOldData( i, wp.colStr(i,"chi_name"),wp.colStr(i,"eng_name"),wp.colStr(i,"birthday"),wp.colStr(i,"cntry_code") );
            wp.colSet(i , "data_status", "N");
    	}
    	
    }
    wp.setListCount(0);
    wp.colSet("ind_num", wp.selectCnt);
    dataReadRelaAfter();

  }

  void dataReadRelaAfter() {
    int ilSelectCnt = 0, ilTmpCnt = 0, rr = 0;
    String lsTmpType = "", lsTmpIdNo = "", lsTmpChiName = "";
    String lsTmpEngName = "", lsTmpBirthday = "", lsTmpCntryCode = "", lsTmpAudcode = "";
    boolean ibDup = false;
    ilSelectCnt = wp.selectCnt;
    rr = wp.selectCnt;
    String sql1 = " select * "
    		+ " from ecs_moddata_tmp "
    		+ " where 1=1 "
    		+ " and tmp_pgm ='cmsm2020' "
            + " and tmp_table = 'CRD_CORP_RELA' "
            + " and tmp_key like ? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("corp_no") + "%"});
    ilTmpCnt = sqlRowNum;
    if (ilTmpCnt == 0)
      return;

    for (int ii = 0; ii < ilTmpCnt; ii++) {
      ibDup = false;
      lsTmpType = commString.mid(sqlStr(ii, "tmp_key"), 8, 1);
      lsTmpIdNo = commString.mid(sqlStr(ii, "tmp_key"), 9, 20);
      lsTmpChiName = commString.mid(sqlStr(ii, "tmp_moddata"), 0, 76);
      lsTmpEngName = commString.mid(sqlStr(ii, "tmp_moddata"), 76, 38);
      lsTmpBirthday = commString.mid(sqlStr(ii, "tmp_moddata"), 114, 8);
      lsTmpCntryCode = commString.mid(sqlStr(ii, "tmp_moddata"), 122, 2);
      lsTmpAudcode = sqlStr(ii, "tmp_audcode");
      for (int zz = 0; zz < ilSelectCnt; zz++) {
    	  // 找重複
        if (wp.colEq(zz, "rela_type", lsTmpType) && wp.colEq(zz, "id_no", lsTmpIdNo)) {	
          wp.colSet(zz, "chi_name", lsTmpChiName);
          wp.colSet(zz, "eng_name", lsTmpEngName);
          wp.colSet(zz, "birthday", lsTmpBirthday);
          wp.colSet(zz, "cntry_code", lsTmpCntryCode);
          wp.colSet(zz, "mod_type", lsTmpAudcode);
          
          setRelaOldData( zz, lsTmpChiName,lsTmpEngName,lsTmpBirthday,lsTmpCntryCode );
          wp.colSet(zz , "data_status", "N");

          ibDup = true;
          continue;
        }
        
      }

      if (ibDup == true)
        continue;

      if (rr > 9) {
        wp.colSet(rr, "ser_num", (rr + 1));
      } else {
        wp.colSet(rr, "ser_num", "0" + (rr + 1));
      }
      wp.colSet(rr, "mix_data", lsTmpType + lsTmpIdNo);
      wp.colSet(rr, "rela_type", lsTmpType);
      if (eqIgno(lsTmpType, "1")) {
        wp.colSet(rr, "tt_rela_type", "實質受益人");
      } else if (eqIgno(lsTmpType, "2")) {
        wp.colSet(rr, "tt_rela_type", "高階管理人");
      } else if (eqIgno(lsTmpType, "3")) {
        wp.colSet(rr, "tt_rela_type", "具控制權人");
      }

      wp.colSet(rr, "id_no", lsTmpIdNo);
      wp.colSet(rr, "chi_name", lsTmpChiName);
      wp.colSet(rr, "eng_name", lsTmpEngName);
      wp.colSet(rr, "birthday", lsTmpBirthday);
      wp.colSet(rr, "cntry_code", lsTmpCntryCode);
      wp.colSet(rr, "mod_type", lsTmpAudcode);

      setRelaOldData( rr, lsTmpChiName,lsTmpEngName,lsTmpBirthday,lsTmpCntryCode );
      wp.colSet(rr , "data_status", "N");
      
      rr++;
    }

    wp.listCount[0] = rr;
    wp.colSet("ind_num", rr);
  }

  public void querySelectACNO() throws Exception {
    corpPSeqno = wp.itemStr("data_k1");
    corpPSeqno1 = wp.itemStr("data_k2"); // corp_p_seqno
    wp.colSet("corp_p_seqno", corpPSeqno1);
    dataReadAcno();
  }

  public void dataReadAcno() throws Exception {
    if (empty(corpPSeqno)) {
      corpPSeqno = wp.itemStr("acno_p_seqno");
    }

    if (empty(corpPSeqno)) {
      alertErr2("acno_p_seqno empty");
      return;
    }

    if (empty(corpPSeqno1)) {
      corpPSeqno1 = wp.itemStr("corp_p_seqno");
    }
    if (empty(corpPSeqno1)) {
      alertErr2("corp_p_seqno empty");
      return;
    }

    wp.selectSQL = " *  ";
    wp.daoTable = "act_acno";
    wp.whereStr = "where 1=1" + sqlCol(corpPSeqno, "acno_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno);
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] billSendingZipArr = commString.splitZipCode(wp.colStr("bill_sending_zip"));
    wp.colSet("bill_sending_zip", billSendingZipArr[0]);
    wp.colSet("bill_sending_zip2", billSendingZipArr[1]);

    cmsm01.Cmsm2020Acno func = new cmsm01.Cmsm2020Acno();
    func.setConn(wp);
    // ddd("A:" + func.select_moddata_tmp());
    if (func.selectModdataTmp() == 1) {
      alertMsg("此筆資料待覆核.....");
    }
    selectBlockAcnoN();
    acnoTab12Desc();
    // this.wp = (TarokoCommon) func.wp;
    selectTab3Acno();
    selectAddressFromCrdCorp();
  }

  private void selectAddressFromCrdCorp() {
    // TODO Auto-generated method stub
    wp.selectSQL = "" + " reg_zip," + " reg_addr1, " + " reg_addr2, " + " reg_addr3, "
        + " reg_addr4, " + " reg_addr5, " + " comm_zip," + " comm_addr1, " + " comm_addr2, "
        + " comm_addr3, " + " comm_addr4, " + " comm_addr5 ";
    wp.daoTable = " crd_corp ";
    wp.whereStr = "where 1=1" + sqlCol(corpPSeqno1, "corp_p_seqno");
    pageSelect();
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] commZipArr = commString.splitZipCode(wp.colStr("comm_zip"));
    wp.colSet("comm_zip", commZipArr[0]);
    wp.colSet("comm_zip2", commZipArr[1]);
    String[] regZipArr = commString.splitZipCode(wp.colStr("reg_zip"));
    wp.colSet("reg_zip", regZipArr[0]);
    wp.colSet("reg_zip2", regZipArr[1]);
  }

  void acnoTab12Desc() {
    if (wp.colEq("special_stat_code", "1")) {
      wp.colSet("tt_special_stat_code", ".航空");
    } else if (wp.colEq("special_stat_code", "2")) {
      wp.colSet("tt_special_stat_code", ".掛號");
    } else if (wp.colEq("special_stat_code", "3")) {
      wp.colSet("tt_special_stat_code", ".人工處理");
    } else if (wp.colEq("special_stat_code", "4")) {
      wp.colSet("tt_special_stat_code", ".行員");
    } else if (wp.colEq("special_stat_code", "5")) {
      wp.colSet("tt_special_stat_code", ".其他");
    }
  }


  void selectBlockAcnoN() {
    String sql1 = " select " + " block_status , " + " block_reason1 , " + " block_reason2 , "
        + " block_reason3 , " + " block_reason4 , " + " block_reason5 " + " from cca_card_acct "
        + " where acno_p_seqno = ? " + " and debit_flag <> 'Y' ";

    sqlSelect(sql1, new Object[] {wp.colStr("acno_p_seqno")});
    if (sqlRowNum <= 0)
      return;
    wp.colSet("block_status", sqlStr("block_status"));
    wp.colSet("block_reason1", sqlStr("block_reason1"));
    wp.colSet("block_reason2", sqlStr("block_reason2"));
    wp.colSet("block_reason3", sqlStr("block_reason3"));
    wp.colSet("block_reason4", sqlStr("block_reason4"));
    wp.colSet("block_reason5", sqlStr("block_reason5"));

  }

  void selectTab3Acno() throws Exception {
    wp.sqlCmd = "SELECT " + " p_seqno , " + " acct_type , " + " curr_code , "
        + " autopay_indicator , " + " autopay_acct_bank , " + " autopay_acct_no , "
        + " autopay_id , " + " autopay_id_code , " 
        + " decode( nvl(autopay_id,''),'','', autopay_id || ' - ' || autopay_id_code) as autopay_id_and_desc , "
        + " autopay_dc_flag , " + " no_interest_flag , "
        + " no_interest_s_month , " + " no_interest_e_month ," + " hex(rowid) as rowid , mod_seqno "
        + " FROM act_acct_curr" + " where 1=1 " + sqlCol(corpPSeqno, "p_seqno")
        + " order by curr_code Asc";
    // sql_ddd();
    this.pageQuery();
    acnoTab3After();
    wp.setListCount(1);
    wp.notFound = "N";
  }

  void acnoTab3After() {

    String sql1 = " select " + " wf_desc " + " from ptr_sys_idtab "
        + " where wf_type = 'DC_CURRENCY' " + " and wf_id = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "autopay_indicator", "1")) {
        wp.colSet(ii, "tt_autopay_indicator", ".扣 TTL");
      } else if (wp.colEq(ii, "autopay_indicator", "2")) {
        wp.colSet(ii, "tt_autopay_indicator", ".扣 MP");
      }

      sqlSelect(sql1, new Object[] {wp.colStr(ii, "curr_code")});

      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_curr_code", "." + sqlStr("wf_desc"));
      }

    }
  }

  public void querySelectCURR() throws Exception {
    corpPSeqno = wp.itemStr("data_k2");
    corpPSeqno1 = wp.itemStr("data_k3");
    dataReadCURR();

  }

  public void dataReadCURR() throws Exception {
    if (empty(corpPSeqno)) {
      corpPSeqno = wp.itemStr("curr_code");
    }
    if (empty(corpPSeqno1)) {
      corpPSeqno = wp.itemStr("acno_p_seqno");
    }

    if (empty(corpPSeqno) || empty(corpPSeqno1)) {
      alertErr2("請從前一頁面重新讀取資料");
      return;
    }

    wp.selectSQL = " p_seqno , " + " acct_type , " + " curr_code , " + " autopay_indicator , "
        + " autopay_acct_bank , " + " autopay_acct_no , " + " autopay_id , " + " autopay_id_code , "
        + " decode( nvl(autopay_id,''),'','', autopay_id || ' - ' || autopay_id_code) as autopay_id_and_desc , "
        + " autopay_dc_flag , " + " no_interest_flag , " + " no_interest_s_month , "
        + " no_interest_e_month  ";
    wp.daoTable = "act_acct_curr";
    wp.whereStr = "where 1=1" + sqlCol(corpPSeqno, "curr_code") + sqlCol(corpPSeqno1, "p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno);
    }
  }

  public void querySelectCONTR() throws Exception {
    corpPSeqno = wp.itemStr("data_k1");
    dataReadCONTR();

  }

  public void dataReadCONTR() throws Exception {
    if (empty(corpPSeqno)) {
      corpPSeqno = wp.itemStr("corp_p_seqno");
    }
    wp.selectSQL = " corp_no ," + " chi_name ," + " contact_name ," + " contact_area_code , "
        + " contact_tel_no , " + " contact_tel_ext , " + " contact_zip , " + " contact_addr1 , "
        + " contact_addr2 , " + " contact_addr3 , " + " contact_addr4 , " + " contact_addr5 ,"
        + " corp_p_seqno ," + " mod_seqno  ";
    wp.daoTable = "crd_corp";
    wp.whereStr = "where 1=1" + sqlCol(corpPSeqno, "corp_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno);
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] contactZipArr = commString.splitZipCode(wp.colStr("contact_zip"));
    wp.colSet("contact_zip", contactZipArr[0]);
    wp.colSet("contact_zip2", contactZipArr[1]);

  }

  @Override
  public void saveFunc() throws Exception {
    if (eqIgno(wp.respHtml, "cmsm2020_contr")) {
    	// 增加主管復核
    	if (checkApproveZz() == false) {
    		return;
    	}
      cmsm01.Cmsm2020Contr func = new cmsm01.Cmsm2020Contr();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    } else if (eqIgno(wp.respHtml, "cmsm2020_add")) {
      dbSaveAdd();
    } else if (eqIgno(wp.respHtml, "cmsm2020_corp")) {
      cmsm01.Cmsm2020Corp func = new cmsm01.Cmsm2020Corp();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }

      if (isDelete() && rc == 1) {
        corpPSeqno = wp.itemStr2("corp_p_seqno");
      }
    } else if (eqIgno(wp.respHtml, "cmsm2020_acno")) {
      cmsm01.Cmsm2020Acno func = new cmsm01.Cmsm2020Acno();
      func.setConn(wp);
      func.varsSet("is_respHtml", "cmsm2020_acno");
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
      if (isUpdate() && rc == 1) {
        corpPSeqno = wp.itemStr2("acno_p_seqno");
        corpPSeqno1 = wp.itemStr2("corp_p_seqno");
      }
      if (isDelete() && rc == 1) {
        corpPSeqno = wp.itemStr2("acno_p_seqno");
        corpPSeqno1 = wp.itemStr2("corp_p_seqno");
      }
    } else if (eqIgno(wp.respHtml, "cmsm2020_curr")) {
      cmsm01.Cmsm2020Acno func = new cmsm01.Cmsm2020Acno();
      func.setConn(wp);
      if (checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd")) == false) {
        return;
      }
      func.varsSet("is_respHtml", "cmsm2020_curr");
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    } else if (eqIgno(wp.respHtml, "cmsm2020_rela")) {
      cmsm01.Cmsm2020Rela func = new cmsm01.Cmsm2020Rela();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }
    this.sqlCommit(rc);
    this.saveAfter(false);
    if (eqIgno(wp.respHtml, "cmsm2020_corp")) {
      if (isDelete()) {
        dataRead();
        wp.respMesg = "取消修改完成";
      } else if (isUpdate()) {
        dataRead();
      }
    } else if (eqIgno(wp.respHtml, "cmsm2020_acno")) {
      if (isDelete()) {
        dataReadAcno();
        wp.respMesg = "取消修改完成";
      } else if (isUpdate()) {
        dataReadAcno();
      }
    }
  }

  void dbSaveAdd() {

    if (dataCheckAdd() == false) {
      rc = -1;
      return;
    }

    if (checkApproveZz() == false)
      return;

    cmsm01.Cmsm2020Add func = new cmsm01.Cmsm2020Add();
    func.setConn(wp);
    rc = func.dbSave(strAction);
    if (rc != 1) {
      alertErr2(func.getMsg());
    } else {
      wp.itemSet("approval_user", "");
      wp.itemSet("approval_passwd", "");
    }
  }

  boolean dataCheckAdd() {

    busi.func.EcsComm func = new busi.func.EcsComm();
    String lsCorpno = wp.itemStr("corp_no");

    // 檢查統一編號不能為空
    if (empty(lsCorpno)) {
      alertErr("統一編號不能為空");
      return false;
    }

    // 檢查統一編號為號碼數為8~11碼
    if (lsCorpno.length() < 8 || lsCorpno.length() > 11) {
      alertErr("統一編號需要8~11碼");
      return false;
    }

    // 統一編號若為8碼需檢查是否符合規則
    if (lsCorpno.length() == 8) {
      if (func.checkCorpNo(lsCorpno) == false) {
        alertErr("統一編號(8碼)不符合規則");
        return false;
      }
    }


    if (wp.itemEmpty("chi_name")) {
      alertErr2("[公司名稱]: 不可空白");
      return false;
    }
    if (wp.itemEmpty("abbr_name")) {
      alertErr2("[簡稱]: 不可空白");
      return false;
    }
    if (wp.itemEmpty("acct_type")) {
      alertErr2("[帳戶類別]: 不可空白");
      return false;
    }
    if (wp.itemNum("line_of_credit_amt") <= 0) {
      alertErr2("[月限額]: 不可空白");
      return false;
    }
    if (wp.itemEmpty("bill_sending_addr1") || wp.itemEmpty("bill_sending_addr5")) {
      alertErr2("[帳單寄送地址]: 不可空白");
      return false;
    }

    // if (wp.item_empty("corp_no") ||
    // wp.item_empty("chi_name") ||
    // wp.item_empty("abbr_name") ||
    // wp.item_empty("acct_type") ||
    // wp.item_num("line_of_credit_amt")<=0 ||
    // wp.item_empty("bill_sending_addr1") ||
    // wp.item_empty("bill_sending_addr5") ||
    // 1==2
    // ) {
    // err_alert("[統一編號,公司名稱,簡稱,帳戶類別,月限額,帳單寄送地址]: 不可空白");
    // return false;
    // }
    
    // 負責人ID欄位增加ID檢核邏輯
    String chargeId = wp.itemStr("charge_id");
    boolean error = commBusiCrd.checkId(chargeId);
    if (error) {
    	errmsg("負責人ID檢核邏輯有誤");
    	return false;
    }

    return true;
  }

  void procFuncRela() throws Exception {
    int ilOk = 0, ilErr = 0, ilCnt = 0, ii = 0;
    boolean lbApr = false;
    cmsm01.Cmsm2020Rela func = new cmsm01.Cmsm2020Rela();
    EcsComm ecsComm = new EcsComm();
    CommBusiCrd commBusiCrd = new CommBusiCrd();
    func.setConn(wp);

    String lsCorpNo = wp.itemStr("corp_no");
    String[] aaOpt = wp.itemBuff("opt");
    String[] lsRelaType = wp.itemBuff("rela_type");
    String[] lsIdNo = wp.itemBuff("id_no");
    String[] lsChiName = wp.itemBuff("chi_name");
    String[] lsEngName = wp.itemBuff("eng_name");
    String[] lsBirthday = wp.itemBuff("birthday");
    String[] lsCntryCode = wp.itemBuff("cntry_code");
    String[] lsOldChiName = wp.itemBuff("old_chi_name");
    String[] lsOldEngName = wp.itemBuff("old_eng_name");
    String[] lsOldBirthday = wp.itemBuff("old_birthday");
    String[] lsOldCntryCode = wp.itemBuff("old_cntry_code");
    String[] lsMixData = wp.itemBuff("mix_data");
    String[] lsModType = wp.itemBuff("mod_type");
    String[] lsDataStatus = wp.itemBuff("data_status");
    int indNum = (int)wp.itemNum("ind_num");
    
    wp.listCount[0] = wp.itemRows("rela_type");
    
//    JustinWu: cancel check idno
//    for(String idNo : lsIdNo) {
//      Boolean isIdnoValid = checkIdnoOrCorpno(idNo);
//      
//      if( ! isIdnoValid) 
//    	  return ; 
//    }
//    
//    if (lsCorpNo.length() == 8) {
//      if (ecsComm.checkCorpNo(lsCorpNo) == false) {
//        alertErr("身分證字號/證照號碼/統編一欄，8碼統編檢核邏輯有誤");
//        return;
//      }
//    }
    

    ii = -1;
    for (String parm : lsMixData) {
      ii++;
      wp.colSet(ii, "ok_flag", "");
      // -option-ON-
      if (checkBoxOptOn(ii, aaOpt)) {
        continue;
      }

      // 檢查資料是否重複
      if (ii != Arrays.asList(lsMixData).indexOf(parm)) {
        wp.colSet(ii, "ok_flag", "!");
        ilErr++;
      }
    }
    if (ilErr > 0) {
      alertErr("資料值重複: " + ilErr);
      return;
    }

    func.varsSet("corp_no", lsCorpNo);
    
    int theNoOfDel = 0;
    for (int rr = 0; rr < wp.itemRows("rela_type"); rr++) {

    	    if (lsDataStatus[rr].equals("D")) {
    	    	ilErr++;
				wp.colSet(rr, "ok_flag", "X");
				wp.colSet(rr, "error_msg", "資料已刪除，請重新讀取");				
				continue;
    	    }
    	    
			func.varsSet("rela_type", lsRelaType[rr]);
			func.varsSet("id_no", lsIdNo[rr]);
			func.varsSet("chi_name", lsChiName[rr]);
			func.varsSet("eng_name", lsEngName[rr]);
			func.varsSet("birthday", lsBirthday[rr]);
			func.varsSet("cntry_code", lsCntryCode[rr]); 
            
			if (!checkBoxOptOn(rr, aaOpt)) { // 未打勾
				if (lsDataStatus[rr].equals("A")) { 
					// 新增資料：
					rc = func.dataProc();
					if (rc == 1) {
						setRelaOldData( rr, lsChiName[rr],lsEngName[rr],lsBirthday[rr],lsCntryCode[rr] );
						wp.colSet( rr , "data_status", "N");
						indNum++;
					}
				}else {
					// 資料是否有異動
					if (isDataModified(lsOldChiName[rr], lsOldEngName[rr], lsOldBirthday[rr], lsOldCntryCode[rr],
							lsChiName[rr], lsEngName[rr], lsBirthday[rr], lsCntryCode[rr])) { //資料已異動
						
						if (empty(lsModType[rr])) {
							// 新增資料至temp：已有主檔，則新增至temp
							rc = func.dataProc();
							setRelaOldData( rr, lsChiName[rr],lsEngName[rr],lsBirthday[rr],lsCntryCode[rr] );
						} else {
							// 更新temp資料：沒有主檔，則更新temp
							rc = func.updateTemp();
							setRelaOldData( rr, lsChiName[rr],lsEngName[rr],lsBirthday[rr],lsCntryCode[rr] );	
						}	
						
					}else {
						//資料未異動
						continue;
					}
				}
				
				if (rc != 1) {
					ilErr++;
					wp.colSet(rr, "ok_flag", "X");
					dbRollback();
					continue;
				} else {
					ilOk++;
					wp.colSet(rr , "ok_flag", "V");
					dbCommit();
					continue;
				}
				
			} else { // 有打勾(表示要刪除)
				if (lsDataStatus[rr].equals("A")) {
					rc = 1;
				}else if (empty(lsModType[rr])) { 
					// 直接刪除主檔
					if (lbApr == false) {
						if (checkApproveZz() == false) {
							ilErr++;
							wp.colSet(rr, "ok_flag", "X");
							wp.colSet(rr, "error_msg", "線上覆核錯誤");	
							continue;
						}
					}
					lbApr = true;

					rc = func.deleteCrdCorpRela();
					
				} else { 
					// 刪除temp：沒有主檔
					if ( lsDataStatus[rr].equals("N")) { 
						rc = func.deleteTemp();
					}else {
						continue;
					}	
				}
				
				if (rc != 1) {
					ilErr++;
					wp.colSet(rr, "ok_flag", "X");
					dbRollback();
					continue;
				} else {
					ilOk++;
					wp.colSet(rr , "ok_flag", "V");
					wp.colSet( rr , "data_status", "D");  // 標記為已刪除
					theNoOfDel++; // 若刪除成功，則記錄刪除筆數
					dbCommit();
					continue;
				}
				
			}
    }

    if (ilErr > 0) {
      this.dbRollback();
    } else {
    	sqlCommit(1);
    }
    
	wp.colSet("ind_num", indNum);
	alertMsg("存檔完成,成功:" + ilOk + "(刪除:" + theNoOfDel + ")" + " 失敗:" + ilErr);
  }

  /**
   * 關係人的舊資料(用來確認資料是否有變動)
   * @param row
   * @param chiName
   * @param engName
   * @param birthday
   * @param cntryCode
   */
	private void setRelaOldData(int row, String chiName, String engName, String birthday, String cntryCode) {
		wp.colSet(row, "old_chi_name", chiName);
		wp.colSet(row, "old_eng_name", engName);
		wp.colSet(row, "old_birthday", birthday);
		wp.colSet(row, "old_cntry_code", cntryCode);
	}

/**
   * 資料是否異動
   * @param oldChiName
   * @param oldEngName
   * @param oldBirthday
   * @param oldCntryCode
   * @param chiName
   * @param engName
   * @param birthday
   * @param cntryCode
   * @return
   */
  private boolean isDataModified(String oldChiName, String oldEngName, String oldBirthday, String oldCntryCode, 
		  String chiName, String engName, String birthday, String cntryCode) {
	
	if ( ! oldChiName.equals(chiName)) {
		return true;
	}  
	if ( ! oldEngName.equals(engName)) {
		return true;
	}    
	if ( ! oldBirthday.equals(birthday)) {
		return true;
	}    
	if ( ! oldCntryCode.equals(cntryCode)) {
		return true;
	}    
	return false;
  }

private boolean checkIdnoOrCorpno(String idNo) {
	  EcsComm ecsComm = new EcsComm();
	  CommBusiCrd commBusiCrd = new CommBusiCrd();
	  Boolean isError = false; 
	  
      if (idNo.length() == 8) {
    	  isError = ecsComm.checkCorpNo(idNo);
    	  if (isError) {
              errmsg("身分證字號/證照號碼/統編一欄，統一編號檢核邏輯有誤");
           }
      }else
      if (idNo.length() == 10) {
    	  isError = commBusiCrd.checkId(idNo);
        if (isError) {
          errmsg("身分證字號/證照號碼/統編一欄，10碼證號檢核邏輯有誤");
        }
      }
      
      if (isError)
    	  return false;
      else
    	  return true;
	
}

private void checkIdnoAJAX() throws Exception {
	 boolean isIdnoValid = checkIdnoOrCorpno(wp.itemStr("idnoAJAX"));
	 wp.addJSON("isIdnoValid", isIdnoValid ? "Y" : "N");
}

@Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    if (eqIgno(wp.respHtml, "cmsm2020_curr")) {
      this.btnModeAud(wp.colStr("curr_code"));
    } else if (eqIgno(wp.respHtml, "cmsm2020_add")) {
      btnModeAud();
    } else
      this.btnModeAud("XX");

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub
    wp.initFlag = "Y";
  }

  public void wfAjaxKey() throws Exception {

    // String ls_winid =
    selectData(wp.itemStr("ax_zip_code"));
    if (rc != 1) {
      wp.addJSON("addr1", "");
      wp.addJSON("addr2", "");
      return;
    }
    wp.addJSON("idCode", wp.itemStr("idCode"));
    wp.addJSON("addr1", sqlStr("addr1"));
    wp.addJSON("addr2", sqlStr("addr2"));

  }

  void selectData(String zipCode) {
    String sql1 = " select " + " zip_city as addr1 , " + " zip_town as addr2 "
        + " from ptr_zipcode " + " where zip_code = ? ";

    sqlSelect(sql1, new Object[] {zipCode});

    if (sqlRowNum <= 0) {
      alertErr2("郵遞區號不存在:" + zipCode);
      return;
    }

  }

}
