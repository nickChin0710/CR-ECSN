package cmsm01;

/** 
 * 20-1008    JustinWu     add zip2
 * 20-0828:   JustinWu     add autopay_id_and_desc
 * 20-0212:   JustinWu     modify userAction
 * 19-1219:   Alex               remove dddw , fix dataRead Acno
 * 19-1205:   Alex               code ->chinese
 * 19-1202:   Alex               code ->chinese
 * 19-0613:   JH                   p_xxx >>acno_p_xxx
 * 109-04-27  shiyuqi       updated for project coding standard     *  
 * 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
 * 111-02-27  V1.00.04   Sunny         公司帳單註記(bill_apply_flag)改為4改2，5改3       -->
 * 112-03-24  V1.00.05   Zuwei Su      增加檢核邏輯，覆核主管不可同為維護經辦
 * */
import ofcapp.BaseAction;

public class Cmsp2020 extends BaseAction {
  String isKkCorpPSeqno = "", isKkPSeqno = "", corppseqno = "";

  @Override
  public void userAction() throws Exception {
    switch (wp.buttonCode) {
      case "X":
        /* 轉換顯示畫面 */
        strAction = "new";
        clearFunc();
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
        // -資料讀取-
        strAction = "R";
        // dataRead_ACNO();
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
      case "L":
        /* 清畫面 */
        strAction = "";
        clearFunc();
        break;
      case "C1":
        // -資料處理-
        procFunc();
        break;
      case "C2":
        // -資料處理-
        procFuncAcno();
        break;
      case "C3":
        // -資料處理-
        procFuncRela();
        break;
    }
  }

  @Override
  public void dddwSelect() {

  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere = " where 1=1 ";

    lsWhere += " and A.tmp_key = B.corp_p_seqno and A.tmp_table ='CRD_CORP' "
        + sqlCol(wp.itemStr("ex_mod_user"), "A.mod_user", "like%")
        + sqlCol(wp.itemStr("ex_mod_date1"), "A.mod_date", ">=")
        + sqlCol(wp.itemStr("ex_mod_date2"), "A.mod_date", "<=");

    wp.whereStr = lsWhere;
    wp.queryWhere = wp.whereStr;
    wp.setQueryMode();

    queryRead();

  }

  @Override
  public void queryRead() throws Exception {
    wp.pageControl();
    daoTid = "A.";
    wp.selectSQL =
        " A.tmp_pgm , A.tmp_table , A.tmp_key , A.mod_user , A.mod_date ,"
            + " A.mod_time2 ,  B.corp_no , B.chi_name , B.corp_p_seqno ";
    wp.daoTable = "crd_corp B , ecs_moddata_tmp A ";
    if (empty(wp.whereStr)) {
      wp.whereStr = " ORDER BY 1";
    }
    wp.whereOrder = " order by A.tmp_key ";
    logSql();
    pageQuery();
    wp.setListCount(1);
    wp.notFound = "N";
    queryReadAcno();
    queryReadRela();

  }

  void queryReadAcno() throws Exception {
    daoTid = "B.";
    wp.sqlCmd = " select A.tmp_table ,  A.tmp_key as acno_p_seqno ,  A.mod_user , "
        + " A.mod_date ,  A.mod_time2 ,  B.acct_type ,  B.acct_key , "
        + " hex(A.rowid) as rowid  from act_acno B , ecs_moddata_tmp A "
        + " where A.tmp_key = B.acno_p_seqno  and A.tmp_table = 'ACT_ACNO:CORP' "
        + sqlCol(wp.itemStr("ex_mod_user"), "A.mod_user", "like%")
        + sqlCol(wp.itemStr("ex_mod_date1"), "A.mod_date", ">=")
        + sqlCol(wp.itemStr("ex_mod_date2"), "A.mod_date", "<=") + " order by A.tmp_key ";
    logSql();
    pageQuery();
    wp.setListCount(2);
    wp.notFound = "N";
  }

  void queryReadRela() throws Exception {
    daoTid = "C.";
    wp.sqlCmd = " select " + " substr(tmp_key,1,8) as corp_no , "
        + " substr(tmp_key,9,1) as rela_type , "
        + " decode(substr(tmp_key,9,1),'1','實質受益人','2','高階管理人','3','具控制權人') as tt_rela_type , "
        + " substr(tmp_key,10,20) as id_no ," + " substr(tmp_moddata,1,76) as chi_name ,"
        + " substr(tmp_moddata,77,38) as eng_name ," + " substr(tmp_moddata,115,8) as birthday ,"
        + " substr(tmp_moddata,123,2) as cntry_code ," + " tmp_audcode as mod_type , "
        + " hex(rowid) as rowid " + " from ecs_moddata_tmp " + " where tmp_pgm = 'cmsm2020' "
        + " and tmp_table = 'CRD_CORP_RELA' "
        + sqlCol(wp.itemStr("ex_mod_user"), "mod_user", "like%")
        + sqlCol(wp.itemStr("ex_mod_date1"), "mod_date", ">=")
        + sqlCol(wp.itemStr("ex_mod_date2"), "mod_date", "<=");
    pageQuery();
    wp.setListCount(3);
    wp.notFound = "N";
  }

  @Override
  public void querySelect() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void dataRead() throws Exception {
    if (empty(isKkCorpPSeqno)) {
      isKkCorpPSeqno = wp.itemStr("kks_corp_p_seqno");
    }
    if (isKkCorpPSeqno.length() < 10) {
      alertMsg("這筆為最後一筆！");
      return;
    }

    String corpPSeqno = isKkCorpPSeqno.substring(0, 10);
    dataReadCorp(corpPSeqno);

    cmsm01.Cmsm2020Corp corp = new cmsm01.Cmsm2020Corp();
    corp.setConn(wp);
    if (corp.selectModdataTmp() <= 0) {
      alertErr2("無異動資料");
      wp.colSet("kks_corp_p_seqno", isKkCorpPSeqno.substring(10));
      return;
    }
    corp.cmsp2020SetModifyData();
    wp.colSet("mod_user", corp.colStr("mod_user"));
    wp.colSet("kks_corp_p_seqno", isKkCorpPSeqno.substring(10));

    if (wp.colEq("spec_busi_code", "01")) {
      wp.colSet("tt_spec_busi_code", "軍火業");
    } else if (wp.colEq("spec_busi_code", "02")) {
      wp.colSet("tt_spec_busi_code", "虛擬貨幣業務");
    } else if (wp.colEq("spec_busi_code", "03")) {
      wp.colSet("tt_spec_busi_code", "空殼公司");
    } else if (wp.colEq("spec_busi_code", "99")) {
      wp.colSet("tt_spec_busi_code", "非以上特殊行業別");
    }

    if (wp.colEq("ubo_flag", "1")) {
      wp.colSet("tt_ubo_flag", "Y: (1) 直接、間接持有法人股份或資本超過25%者，即為具控制權之最終自然人。");
    } else if (wp.colEq("ubo_flag", "2")) {
      wp.colSet("tt_ubo_flag", "Y: (2) 目前未具持有法人股份或資本超過25%具控制權之自然人者，但有透過其他方式對法人行使控制權之最終自然人。");
    } else if (wp.colEq("ubo_flag", "3")) {
      wp.colSet("tt_ubo_flag", "Y: (3) 擔任高階管理職位之自然人。");
    } else if (wp.colEq("ubo_flag", "N")) {
      wp.colSet("tt_ubo_flag", "N: 請選擇不需確認之身份別。");
    }

    if (wp.colEq("non_ubo_type", "1")) {
      wp.colSet("tt_non_ubo_type", "1.我國政府機關");
    } else if (wp.colEq("non_ubo_type", "2")) {
      wp.colSet("tt_non_ubo_type", "2.我國公營事業機構");
    } else if (wp.colEq("non_ubo_type", "3")) {
      wp.colSet("tt_non_ubo_type", "3.外國政府機關");
    } else if (wp.colEq("non_ubo_type", "4")) {
      wp.colSet("tt_non_ubo_type", "4.我國公開發行公司或其子公司");
    } else if (wp.colEq("non_ubo_type", "5")) {
      wp.colSet("tt_non_ubo_type", "5.於國外掛牌並依掛牌所在地規定，應揭露其主要股東之股票上市、上櫃公司及其子公司");
    } else if (wp.colEq("non_ubo_type", "6")) {
      wp.colSet("tt_non_ubo_type", "6.受我國監理之金融機構及其管理之投資工具");
    } else if (wp.colEq("non_ubo_type", "7")) {
      wp.colSet("tt_non_ubo_type", "7.設立於境外，且受FATF所定防制洗錢及打擊資恐標準一致之金融機構，及該金融機構管理之投資工具");
    } else if (wp.colEq("non_ubo_type", "8")) {
      wp.colSet("tt_non_ubo_type", "8.我國政府機關管理之基金");
    } else if (wp.colEq("non_ubo_type", "9")) {
      wp.colSet("tt_non_ubo_type", "9.員工持股信託、員工福利儲蓄信託");
    }

    if (wp.colEq("risk_level", "H")) {
      wp.colSet("tt_risk_level", "高");
    } else if (wp.colEq("risk_level", "M")) {
      wp.colSet("tt_risk_level", "中");
    } else if (wp.colEq("risk_level", "L")) {
      wp.colSet("tt_risk_level", "低");
    }

    if (wp.colEq("incorporated_type", "1")) {
      wp.colSet("tt_incorporated_type", "自然人");
    } else if (wp.colEq("incorporated_type", "2")) {
      wp.colSet("tt_incorporated_type", "政府機關");
    } else if (wp.colEq("incorporated_type", "3")) {
      wp.colSet("tt_incorporated_type", "營利組織(公營事業單位)");
    } else if (wp.colEq("incorporated_type", "4")) {
      wp.colSet("tt_incorporated_type", "營利組織(上市櫃公司/公開發行公司)");
    } else if (wp.colEq("incorporated_type", "5")) {
      wp.colSet("tt_incorporated_type", "營利組織(非公開發行公司)");
    } else if (wp.colEq("incorporated_type", "6")) {
      wp.colSet("tt_incorporated_type", "非營利組織(慈善團體之社團法人、基金會、宗教組織)");
    } else if (wp.colEq("incorporated_type", "7")) {
      wp.colSet("tt_incorporated_type", "非營利組織(非為慈善團體之社團法人、基金會、宗教組織)");
    } else if (wp.colEq("incorporated_type", "8")) {
      wp.colSet("tt_incorporated_type", "特殊目的實體(股東為信託合約者)");
    }

    switch (wp.colStr("corp_act_type")) {
      case "1":
        wp.colSet("corp_act_type_desc", "總繳");
        break;
      case "2":
        wp.colSet("corp_act_type_desc", "個繳");
        break;
    }

    switch (wp.colStr("empoly_type")) {
      case "1":
        wp.colSet("empoly_type_desc", "法人卡－民營");
        break;
      case "2":
        wp.colSet("empoly_type_desc", "法人卡－公營");
        break;
      case "3":
        wp.colSet("empoly_type_desc", "法人卡－政府");
        break;
      case "4":
        wp.colSet("empoly_type_desc", "法人卡－金融");
        break;
      case "5":
        wp.colSet("empoly_type_desc", "法人卡－其他");
        break;
    }
  }

  @Override
  public void saveFunc() throws Exception {
      // --
      if (checkAprUser(0, "mod_user")) {
        alertErr2(" [覆核主管/維護經辦] 不可同一人");
        return;
      }

    if (wp.respHtml.indexOf("_corp") > 0) {
      cmsm01.Cmsm2020Corp func = new cmsm01.Cmsm2020Corp();
      func.setConn(wp);
      rc = func.dataProc();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    } else if (wp.respHtml.indexOf("_acno") > 0) {
      cmsm01.Cmsm2020Acno func = new cmsm01.Cmsm2020Acno();
      func.setConn(wp);
      rc = func.dataProc();
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }
    sqlCommit(rc);

    if (rc == 1 && (wp.respHtml.indexOf("_corp") > 0)) {
      dataRead();
    }

    if (rc == 1 && (wp.respHtml.indexOf("_acno") > 0)) {
      dataReadAcno();
    }

  }

  @Override
  public void procFunc() throws Exception {
    String[] corpOpt = wp.itemBuff("opt1");
    String[] corpCorpPSeqno = wp.itemBuff("A.corp_p_seqno");

    wp.listCount[0] = corpCorpPSeqno.length;

    isKkCorpPSeqno = "";
    for (int rr = 0; rr < corpCorpPSeqno.length; rr++) {
      // -option-ON-
      if (!checkBoxOptOn(rr, corpOpt)) {
        continue;
      }
      log("E:" + corpCorpPSeqno[rr]);
      isKkCorpPSeqno += corpCorpPSeqno[rr];
    }
    if (isKkCorpPSeqno.length() > 0) {
      dataRead();
      wp.respMesg = "";
    } else {
      alertErr2("請點選欲覆核資料");
    }

  }

  public void procFuncAcno() throws Exception {
    String[] corpOpt = wp.itemBuff("opt2");
    String[] acnoPSeqno = wp.itemBuff("B.acno_p_seqno");

    wp.listCount[1] = acnoPSeqno.length;

    isKkCorpPSeqno = "";
    for (int rr = 0; rr < acnoPSeqno.length; rr++) {
      // -option-ON-
      if (!checkBoxOptOn(rr, corpOpt)) {
        continue;
      }
      log("E:" + acnoPSeqno[rr]);
      isKkPSeqno += acnoPSeqno[rr];
    }
    if (isKkPSeqno.length() > 0) {
      dataReadAcno();
    } else {
      alertErr2("請點選欲覆核資料");
    }

  }

  @Override
  public void initButton() {
    // TODO Auto-generated method stub

  }

  @Override
  public void initPage() {
    // TODO Auto-generated method stub

  }

  void dataReadCorp(String isCorpPSeqno) {

    wp.selectSQL = " *  ";
    wp.daoTable = "crd_corp";
    wp.whereStr = "where 1=1" + sqlCol(isCorpPSeqno, "corp_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corppseqno);
      return;
    }
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] commZipArr = commString.splitZipCode(wp.colStr("comm_zip"));
    wp.colSet("comm_zip", commZipArr[0]);
    wp.colSet("comm_zip2", commZipArr[1]);
    String[] regZipArr = commString.splitZipCode(wp.colStr("reg_zip"));
    wp.colSet("reg_zip", regZipArr[0]);
    wp.colSet("reg_zip2", regZipArr[1]);
    
    dataReadCorpAfter(isCorpPSeqno);
  }

  void dataReadCorpAfter(String isCorpPSeqno) {
    wp.selectSQL = " * ";
    wp.daoTable = "crd_corp_ext";
    wp.whereStr = "where 1=1" + sqlCol(isCorpPSeqno, "corp_p_seqno");
    pageSelect();
    if (sqlNotFind()) {
      selectOK();
      return;
    }
  }

  public void dataReadAcno() throws Exception {
    if (empty(isKkPSeqno)) {
      isKkPSeqno = wp.itemStr("kks_p_seqno");
    }
    if (isKkPSeqno.length() < 10) {
      alertMsg("這筆為最後一筆！");
      return;
    }

    String kk1 = isKkPSeqno.substring(0, 10);
    readActAcno(kk1);

    cmsm01.Cmsm2020Acno acno = new cmsm01.Cmsm2020Acno();
    acno.setConn(wp);
    if (acno.selectModdataTmp() <= 0) {
      alertErr2("無異動資料");
      wp.colSet("kks_p_seqno", isKkPSeqno.substring(10));
      return;
    }
    if (empty(wp.colStr("class_code")))
      wp.colSet("class_code", "F");

    switch (wp.colStr("bill_apply_flag")) {
      case "2":
        wp.colSet("bill_apply_flag_desc", "同公司通訊地(法人)");
        break;
      case "3":
        wp.colSet("bill_apply_flag_desc", "同公司登記地(法人)");
        break;
    }

    if (!empty(wp.colStr("mod_bill_sending_zip")) || !empty(wp.colStr("mod_bill_sending_addr1"))
        || !empty(wp.colStr("mod_bill_sending_addr2"))
        || !empty(wp.colStr("mod_bill_sending_addr3"))
        || !empty(wp.colStr("mod_bill_sending_addr4"))
        || !empty(wp.colStr("mod_bill_sending_addr5"))) {
      wp.colSet("chg_addr_date", this.getSysDate());
    }

    selectTab3Acno(kk1);
    wp.colSet("kks_p_seqno", isKkPSeqno.substring(10));

  }

  void readActAcno(String isPSeqno) {

    wp.selectSQL = " A.*  ," + " A.accept_dm as hh_accept_dm ";
    wp.daoTable = "act_acno A ";
    wp.whereStr = "where 1=1" + sqlCol(isPSeqno, "A.acno_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corppseqno);
    }
  }

  void selectTab3Acno(String isPSeqno) throws Exception {
    wp.sqlCmd = "SELECT  p_seqno ,  acct_type ,  curr_code , "
        + " autopay_indicator ,  autopay_acct_bank ,  autopay_acct_no , "
        + " autopay_id ,  autopay_id_code , " 
        + " decode( nvl(autopay_id,''),'','', autopay_id || ' - ' || autopay_id_code) as autopay_id_and_desc , "
        + " autopay_dc_flag ,  no_interest_flag , "
        + " no_interest_s_month ,  no_interest_e_month , hex(rowid) as rowid , mod_seqno "
        + " FROM act_acct_curr where 1=1 " + sqlCol(isPSeqno, "p_seqno")
        + " order by curr_code Asc";
    logSql();
    this.pageQuery();
    wp.setListCount(1);
    wp.notFound = "N";
  }

  void procFuncRela() throws Exception {
    int ilCnt = 0, ilOk = 0, ilErr = 0;
    wp.listCount[0] = wp.itemRows("A.corp_p_seqno");
    wp.listCount[1] = wp.itemRows("B.acno_p_seqno");
    wp.listCount[2] = wp.itemRows("C.rowid");

    cmsm01.Cmsm2020Rela func = new cmsm01.Cmsm2020Rela();
    func.setConn(wp);

    String[] aaOpt = wp.itemBuff("opt3");
    String[] lsRowid = wp.itemBuff("C.rowid");
    String[] lsCorpNo = wp.itemBuff("C.corp_no");
    String[] lsRelaType = wp.itemBuff("C.rela_type");
    String[] lsIdNo = wp.itemBuff("C.id_no");
    String[] lsChiName = wp.itemBuff("C.chi_name");
    String[] lsEngName = wp.itemBuff("C.eng_name");
    String[] lsBirthday = wp.itemBuff("C.birthday");
    String[] lsCntryCode = wp.itemBuff("C.cntry_code");
    String[] lsModType = wp.itemBuff("C.mod_type");

    int rr = -1;
    rr = optToIndex(aaOpt[0]);

    if (rr < 0) {
      alertErr2("請點選欲覆核資料");
      return;
    }

    for (int ii = 0; ii < aaOpt.length; ii++) {
      rr = (int) optToIndex(aaOpt[ii]);
      if (rr < 0) {
        continue;
      }
      wp.colSet(rr, "ok_flag", "-");

      func.varsSet("rowid", lsRowid[rr]);
      func.varsSet("corp_no", lsCorpNo[rr]);
      func.varsSet("rela_type", lsRelaType[rr]);
      func.varsSet("id_no", lsIdNo[rr]);
      func.varsSet("chi_name", lsChiName[rr]);
      func.varsSet("eng_name", lsEngName[rr]);
      func.varsSet("birthday", lsBirthday[rr]);
      func.varsSet("cntry_code", lsCntryCode[rr]);
      func.varsSet("mod_type", lsModType[rr]);

      rc = func.dataProc2();
      if (rc != 1) {
        ilErr++;
        dbRollback();
        wp.colSet(ii, "C.ok_flag", "X");
        continue;
      }

      ilOk++;
      sqlCommit(1);
      wp.colSet(ii, "C.ok_flag", "V");
      continue;
    }

    alertMsg("覆核完畢 , 成功:" + ilOk + " 失敗:" + ilErr);

  }



}
