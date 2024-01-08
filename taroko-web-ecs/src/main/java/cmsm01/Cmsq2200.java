package cmsm01;
/**
 * 19-1224:     Alex           n-find -> 此條件查無資料
 * 19-1219:     Alex           ptr_branch -> gen_brn
 * 19-0613:     JH             p_xxx >>acno_p_xxx
 * 109-04-20  shiyuqi       updated for project coding standard
 * 109-05-26  tanwei       updated for project coding standard      
 * 109-06-09  shiyuqi       簡體字修正為繁體  
 * 109-06-09  shiyuqi      顯示條件語法調整
 * 109-06-29  zuwei         簡體字改爲繁體字
 * 109-06-24  sunny        修改selectTab3 act_acct_curr網頁無法顯示資料
 * 109-07-01   tanwei       新增關係人相關方法 
 * 109-07-24   Sunny       LINE_OF_CREDIT_AMT 單位:元
 * 109-09-04   JustinWu   add acct_status_desc
 * 109-09-07   JustinWu   commentary useless code
 * 109-10-12    JustinWu   帳戶總覽 order by acno_flag
 * 109-12-02   JustinWu    change uf_tt_idtab into uf_opp_status
 * 109-12-23   JustinWu    parameterize sql and chg where statement
  * 109-12-30  V1.00.01  shiyuqi       修改无意义命名        
 * 111-05-05   JustinWu    修正帳務總覽明細只能查詢相對應的acct_type資料                   *
 * 112-06-09   Ryan        關係卡增加其他頁籤*
 */

import org.apache.commons.lang3.math.NumberUtils;

import ofcapp.BaseAction;
import taroko.base.CommString;

public class Cmsq2200 extends BaseAction {
  String corpPSeqno1 = "", corpPSeqno = "";
  CommString commString = new CommString();

  @Override
  public void userAction() throws Exception {
    if (wp.buttonCode.equals("X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (wp.buttonCode.equals("Q1")) {
      /* 查詢功能 */
      strAction = "idno";
      wp.colSet("pageType", "idno");
      queryIdno();
    } else if (wp.buttonCode.equals("Q2")) {
      /* 查詢功能 */
      strAction = "acno";
      wp.colSet("pageType", "acno");
      queryAcno();
    } else if (wp.buttonCode.equals("Q3")) {
      /* 查詢功能 */
      strAction = "relcard";
      wp.colSet("pageType", "relcard");
      queryRelcard();
    } else if (wp.buttonCode.equals("Q4")) {
      /* 查詢功能 */
      strAction = "relidno";
      wp.colSet("pageType", "relidno");
      queryRelidno();
    } else if (wp.buttonCode.equals("Q5")) {
      /* 查詢功能 */
      strAction = "accounting";
      wp.colSet("pageType", "accounting");
      queryAccounting();
    }else if (wp.buttonCode.equals("Q6")) {
      /* 查詢功能 */
      strAction = "relatedParties";
      wp.colSet("pageType", "relatedParties");
      queryRelatedParties();
    }else if (eqIgno(wp.buttonCode, "R")) {
      /* 資料讀取 */
      dataRead();
    } else if (wp.buttonCode.equals("M")) {
      /* 瀏覽功能 :skip-page */
      queryRead();
    } else if (wp.buttonCode.equals("S")) {
      /* 動態查詢 */
      querySelect();
    } else if (wp.buttonCode.equals("S1")) {
      /* 動態查詢 */
      querySelectIdno();
    } else if (wp.buttonCode.equals("S2")) {
      /* 動態查詢 */
      querySelectAcno();
    } else if (wp.buttonCode.equals("S3")) {
      /* 動態查詢 */
      querySelectRelcard();
    } else if (wp.buttonCode.equals("S4")) {
      /* 動態查詢 */
      querySelectRelidno();
    } else if(wp.buttonCode.equals("S5")) {
      /* 動態查詢 */
      selectAccountingDetail();
    } else if(wp.buttonCode.equals("S6")) {
      /* 動態查詢 */
      relatedPartiesDetails();
    }else if (wp.buttonCode.equals("L")) {
      /* 清畫面 */
      strAction = "";
      clearFunc();
    }

  }

  @Override
  public void dddwSelect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

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

  // --idno 查詢基本資料列表
  public void queryIdno() throws Exception {
    String lsWhere = "";

    if (!empty(wp.itemStr("ex_corp_no"))) {
        lsWhere += sqlCol(wp.itemStr("ex_corp_no"), "corp_no");
      }

    if (!empty(wp.itemStr("ex_cname"))) {
        lsWhere += sqlCol(wp.itemStr("ex_cname"), "chi_name", "like%");
      }
    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    wp.sqlCmd = " select " + " corp_p_seqno ," + " corp_no , " + " chi_name " + " FROM crd_corp "
        + " where 1=1 " + lsWhere + " order by corp_no";
    log("CMD:" + wp.sqlCmd);
    this.pageQuery();
    wp.setListCount(1);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
  }

  /**
   * 查詢基本資料詳情
   * 
   * @throws Exception
   */
  public void querySelectIdno() throws Exception {
    corpPSeqno1 = wp.itemStr("data_k1");
    corpPSeqno = wp.itemStr("data_k2");
    wp.selectSQL =
        " * , decode(B.spec_busi_code,'01','軍火業','02','虛擬貨幣業務','03','空殼公司','99','非以上特殊行業別') as tt_spec_busi_code ";
    wp.daoTable = "crd_corp A left join crd_corp_ext B on A.corp_p_seqno = B.corp_p_seqno ";
    wp.whereStr = " where 1=1 " + sqlCol(corpPSeqno1, "A.corp_p_seqno");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno1);
      return;
    }
    String uboFlag = wp.colStr("ubo_flag");
    wp.colSet("tt_ubo_flag",
        commString.decode(uboFlag, ",1,2,3,N",
            ",Y: (1) 直接、間接持有法人股份或資本超過25%者，即為具控制權之最終自然人。,"
                + "Y: (2) 目前未具持有法人股份或資本超過25%具控制權之自然人者，但有透過其他方式對法人行使控制權之最終自然人。,"
                + "Y: (3) 擔任高階管理職位之自然人。,N: 請選擇不需確認之身份別。"));

    String nonUboType = wp.colStr("non_ubo_type");
    wp.colSet("tt_non_ubo_type",
        commString.decode(nonUboType, ",1,2,3,4,5,6,7,8,9",
            ",1.我國政府機關,2.我國公營事業機構,3.外國政府機關,4.我國公開發行公司或其子公司,"
                + "5.於國外掛牌並依掛牌所在地規定，應揭露其主要股東之股票上市、上櫃公司及其子公司," + "6.受我國監理之金融機構及其管理之投資工具,"
                + "7.設立於境外，且受FATF所定防制洗錢及打擊資恐標準一致之金融機構，及該金融機構管理之投資工具," + "8.我國政府機關管理之基金,"
                + "9.員工持股信託、員工福利儲蓄信託"));

    String riskLevel = wp.colStr("risk_level");
    wp.colSet("tt_risk_level", commString.decode(riskLevel, ",H,M,L", ",高,中,低"));

    String incorporatedType = wp.colStr("incorporated_type");
    wp.colSet("tt_incorporated_type",
        commString.decode(incorporatedType, ",1,2,3,4,5,6,7,8",
            ",自然人,政府機關,營利組織(公營事業單位),營利組織(上市櫃公司/公開發行公司),"
                + "營利組織(非公開發行公司),非營利組織(慈善團體之社團法人、基金會、宗教組織),"
                + "非營利組織(非為慈善團體之社團法人、基金會、宗教組織),特殊目的實體(股東為信託合約者)"));

    String corpActType = wp.colStr("corp_act_type");
    wp.colSet("corp_act_type_desc", commString.decode(corpActType, ",1,2", ",總繳,個繳"));

    String empolyType = wp.colStr("empoly_type");
    wp.colSet("empoly_type_desc",
        commString.decode(empolyType, ",1,2,3,4,5", ",法人卡－民營,法人卡－公營,法人卡－政府,法人卡－金融,法人卡－其他"));

    selectBusCode("tt_business_code", wp.colStr("business_code"));
  }

  void selectBusCode(String col, String msgValue) {
    String sql1 = " select " + " msg " + " from crd_message " + " where msg_type ='BUS_CODE' "
        + " and msg_value = ? ";
    sqlSelect(sql1, new Object[] {msgValue});

    if (sqlRowNum <= 0)
      return;
    wp.colSet(col, sqlStr("msg"));
  }

  void selectBranch(String col, String branch) {
    String sql1 = " select " + " full_chi_name " + " from gen_brn " + " where branch = ? ";
    sqlSelect(sql1, new Object[] {branch});

    if (sqlRowNum <= 0)
      return;
    wp.colSet(col, sqlStr("full_chi_name"));

  }

  // --acno 查詢法人賬戶資料列表
  public void queryAcno() throws Exception {
    String lswhere = "";
    String corpNo = wp.itemStr("ex_corp_no");
    if (!empty(corpNo)) {
      int len = corpNo.length();
      if (len < 8 || len > 11) {
        alertErr2("統編碼只允許8~11碼");
        return;
      }
      if (len == 8) {
        if (!NumberUtils.isDigits(corpNo)) {
          alertErr2("統編碼8碼時只允許數字");
          return;
        }
      }
      lswhere += sqlCol(corpNo, "B.corp_no");
    }
    if (!empty(wp.itemStr("ex_cname"))) {
        lswhere += sqlCol(wp.itemStr("ex_cname"), "B.chi_name", "like%");
      }

    if (empty(lswhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    lswhere += " and A.acno_flag ='2' ";

    wp.sqlCmd =
        "SELECT A.acno_p_seqno , " + " A.acct_type , " + " A.acct_key , " + " A.corp_p_seqno ,"
            + " A.card_indicator ," + " B.chi_name " + " FROM act_acno A, crd_corp B "
            + " where A.corp_p_seqno = B.corp_p_seqno " + lswhere + " order by A.acct_key";
    logSql();
    this.pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
    queryAfterAcno();
    wp.setListCount(2);
  }

  void queryAfterAcno() {
    String sql1 = " select " + " chin_name " + " from ptr_acct_type " + " where acct_type = ? ";

    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "acct_type")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_acct_type", sqlStr("chin_name"));
      }
    }


  }

  /**
   * 查詢法人帳戶資料詳情
   * 
   * @throws Exception
   */
  public void querySelectAcno() throws Exception {
    corpPSeqno1 = wp.itemStr("data_k1");
    corpPSeqno = wp.itemStr("data_k2");
    wp.selectSQL = " corp_no ," + " chi_name ";
    wp.daoTable = "crd_corp ";
    wp.whereStr = " where 1=1 " + sqlCol(corpPSeqno, "corp_p_seqno");
    this.logSql();
    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno1);
      return;
    }
    selectTab1And2();
    selectTab3();
    dataReadAfter();
  }

  void selectTab1And2() {
    wp.selectSQL = " * ";
    wp.daoTable = "act_acno ";
    wp.whereStr = " where 1=1 " + sqlCol(corpPSeqno1, "acno_p_seqno");

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno1);
      return;
    }
    dataReadAfterAcno();
  }

  void dataReadAfterAcno() {
    wp.colSet("wk_acctkey", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));

//    2020-09-04: JustinWu
//    String acctStatus = wp.colStr("acct_status");
//    wp.colSet("tt_acct_status", commString.decode(acctStatus, ",1,2,3,4,5", ",正常,逾放,催收,呆帳,結清"));

    String newAcctFlag = wp.colStr("new_acct_flag");
    wp.colSet("tt_new_acct_flag", commString.decode(newAcctFlag, ",Y,N", ",新戶,舊戶"));

    selectBranch("tt_risk_bank_no", wp.colStr("risk_bank_no"));
    String specialStatCode = wp.colStr("special_stat_code");
    wp.colSet("tt_special_stat_code",
        commString.decode(specialStatCode, ",1,2,3,4,5", ",航空,掛號,人工處理,行員,其他"));

    // 增加「帳單註記」
    switch (wp.colStr("bill_apply_flag")) {
      case "4":
        wp.colSet("bill_apply_flag_desc", "同通訊(法人)");
        break;
      case "5":
        wp.colSet("bill_apply_flag_desc", "同公司(法人)");
        break;
    }
    
    wp.colSet("acct_status_desc", ecsfunc.DeCodeAct.acctStatus(wp.colStr("acct_status")));
    
  }

  void selectTab3() throws Exception {
    String sql1 = "select curr_code," + "autopay_indicator ," + " autopay_acct_bank,"
        + " autopay_acct_no ," + " autopay_id ," + " autopay_dc_flag ," + " no_interest_flag ,"
        + " no_interest_s_month , " + " no_interest_e_month " + " from act_acct_curr"
        + " where p_seqno =? " + " order by acct_type, curr_code ";
    sqlSelect(sql1, new Object[] {wp.colStr("acno_p_seqno")});

    int llRow = 0;
    llRow = sqlRowNum;

    String sql2 = " select " + " curr_chi_name " + " from ptr_currcode " + " where curr_code = ? ";

    for (int ii = 0; ii < llRow; ii++) {
      wp.colSet(ii, "curr_code", sqlStr(ii, "curr_code"));
      sqlSelect(sql2, new Object[] {sqlStr(ii, "curr_code")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_curr_code", sqlStr("curr_chi_name"));
      }
      wp.colSet(ii, "autopay_indicator", sqlStr(ii, "autopay_indicator"));
      wp.colSet(ii, "autopay_acct_no", sqlStr(ii, "autopay_acct_no"));
      wp.colSet(ii, "autopay_id", sqlStr(ii, "autopay_id"));
      wp.colSet(ii, "autopay_dc_flag", sqlStr(ii, "autopay_dc_flag"));
      wp.colSet(ii, "no_interest_flag", sqlStr(ii, "no_interest_flag"));
      wp.colSet(ii, "wk_no_int_month",
          sqlStr(ii, "no_interest_s_month") + "--" + sqlStr(ii, "no_interest_e_month"));
      wp.colSet(ii, "autopay_acct_bank", sqlStr(ii, "autopay_acct_bank"));
      if (wp.colEq(ii, "autopay_indicator", "1")) {
        wp.colSet(ii, "tt_autopay_indicator", "扣 TTL");
      } else if (wp.colEq(ii, "autopay_indicator", "2")) {
        wp.colSet(ii, "tt_autopay_indicator", "扣 MP");
      }
    }
    wp.selectCnt = (sqlRowNum);
    wp.setListSernum(0, "");
  }

  void dataReadAfter() {
    String sql1 = "select count(*) as db_valid_cardnum " + " from crd_card"
        + " where corp_p_seqno =?" + " and current_code = '0'";
    sqlSelect(sql1, new Object[] {wp.colStr("corp_p_seqno")});
    wp.colSet("txt_vldcardnbr", sqlStr("db_valid_cardnum"));
    /*---未有 table 
    		String sql2 ="select jcic_bad_debt_date as db_jcic_baddebt_date "				
    				+" from act_acno_ext"
    				+" where p_seqno =?"
    				;
    		sqlSelect(sql2,new Object[]{
    			wp.col_ss("p_seqno")	
    		});
    		wp.col_set("db_jcic_baddebt_date",sql_ss("db_jcic_baddebt_date"));		
    */
  }

  // --relcard 查詢關係卡列表
  public void queryRelcard() throws Exception {
    String lsWhere = "";

    if (!empty(wp.itemStr("ex_corp_no"))) {
        lsWhere += sqlCol(wp.itemStr("ex_corp_no"), "corp_no");
      }

    if (!empty(wp.itemStr("ex_cname"))) {
        lsWhere += sqlCol(wp.itemStr("ex_cname"), "chi_name", "like%");
      }
    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    wp.sqlCmd = " select " + " corp_p_seqno ," + " corp_no , " + " chi_name " + " FROM crd_corp "
        + " where 1=1 " + lsWhere + " order by corp_no";

    this.pageQuery();
    wp.setListCount(3);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
  }

  /**
   * 查詢關係卡詳情
   * 
   * @throws Exception
   */
  public void querySelectRelcard() throws Exception {
    corpPSeqno1 = wp.itemStr("data_k1");
    dataReadRelcard();
  }

  void dataReadRelcard() throws Exception {
    wp.selectSQL = " corp_no ," + " chi_name ";
    wp.daoTable = "crd_corp ";
    wp.whereStr = " where 1=1 " + sqlCol(corpPSeqno1, "corp_p_seqno");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno1);
      return;
    }
    relcard1();
    relcard2();
    relcard3();
    relcard4();
    relcard5();
    relcard6();
    wp.notFound = "N";
  }

  void relcard1() throws Exception {
    // db_line_credit_amt ????????
    daoTid = "A.";
    wp.sqlCmd += " select " + " card_no , " + " card_type , " + " group_code , " +"reg_bank_no , " +" sup_flag , "
        + " major_card_no , " + " acct_type , " + " uf_idno_id(id_p_seqno) as id_no , "
        + " uf_idno_name(id_p_seqno) as chi_name ," + " '' as db_line_credit_amt , "
        + " acno_flag , " + " acno_p_seqno " + " from crd_card " 
        + " where corp_p_seqno = ? "
        + " and   current_code ='0' " 
        + " order by card_no ";

    setString(corpPSeqno1);
    this.pageQuery();
    if (sqlRowNum <= 0) {
      wp.notFound = "N";
      return;
    }
    selectLineCreditAmt();
    wp.setListCount(1);
  }

  void selectLineCreditAmt() {
    int tlCnt = 0, tlAmt = 0;
    tlCnt = wp.selectCnt;
    String sql1 = " select " + " line_of_credit_amt"
        + " from act_acno " + " where acno_p_seqno = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      sqlSelect(sql1, new Object[] {wp.colStr(ii, "A.acno_p_seqno")});
      if (sqlRowNum <= 0)
        continue;
      wp.colSet(ii, "A.db_line_credit_amt", "" + sqlInt("line_of_credit_amt"));
      tlAmt += sqlInt("line_of_credit_amt");
    }

    wp.colSet("A.tl_cnt", tlCnt);
    wp.colSet("A.tl_amt", tlAmt);

  }

  void relcard2() throws Exception {

    daoTid = "B.";
    wp.sqlCmd +=
        " select " + " card_no , " + " sup_flag , " + " oppost_date , " + "reg_bank_no , " + " oppost_reason , "
            + wp.sqlID + "uf_opp_status(oppost_reason) as tt_oppost_reason "
            + " from crd_card " 
            + " where 1=1 " 
            + " and corp_p_seqno = ? "
            + " and current_code = '1' " 
            + " order by card_no Asc ";
    log("Cmd:" + wp.sqlCmd);
    setString(corpPSeqno1);
    this.pageQuery();
    wp.setListCount(2);
  }

  void relcard3() throws Exception {

    daoTid = "C.";
    wp.sqlCmd +=
        " select " + " card_no , " + " sup_flag , " + " oppost_date , " + "reg_bank_no , " + " oppost_reason , "
            + wp.sqlID + "uf_opp_status(oppost_reason) as tt_oppost_reason ,"
            + " new_card_no " + " from crd_card " 
            + " where 1=1 " 
            + " and corp_p_seqno = ? " 
            + " and current_code = '2' " 
            + " order by card_no Asc ";
    log("Cmd:" + wp.sqlCmd);
    setString(corpPSeqno1);
    this.pageQuery();
    wp.setListCount(3);
  }

  void relcard4() throws Exception {

    daoTid = "D.";
    wp.sqlCmd +=
        " select " + " card_no , " + " sup_flag , " + " oppost_date , " + "reg_bank_no , " + " oppost_reason , "
            + wp.sqlID + "uf_opp_status(oppost_reason) as tt_oppost_reason ,"
            + " new_card_no " + " from crd_card " 
            + " where 1=1 " 
            + " and corp_p_seqno = ? " 
            + " and current_code = '5' " 
            + " order by card_no Asc ";
    log("Cmd:" + wp.sqlCmd);
    setString(corpPSeqno1);
    this.pageQuery();
    wp.setListCount(4);
  }

  void relcard5() throws Exception {

    daoTid = "E.";
    wp.sqlCmd +=
        " select " + " card_no , " + " sup_flag , " + " oppost_date , " + "reg_bank_no , " + " oppost_reason , "
            + wp.sqlID + "uf_opp_status(oppost_reason) as tt_oppost_reason "
            + " from crd_card " 
            + " where 1=1 " 
            + " and corp_p_seqno = ? "
            + " and current_code = '3' " 
            + " order by card_no Asc ";
    log("Cmd:" + wp.sqlCmd);
    setString(corpPSeqno1);
    this.pageQuery();
    wp.setListCount(5);
  }
  
  void relcard6() throws Exception {

	    daoTid = "F.";
	    wp.sqlCmd +=
	        " select " + " card_no , " + " sup_flag , " + " oppost_date , " + "reg_bank_no , " + " oppost_reason , "
	            + wp.sqlID + "uf_opp_status(oppost_reason) as tt_oppost_reason "
	            + " from crd_card " 
	            + " where 1=1 " 
	            + " and corp_p_seqno = ? "
	            + " and current_code = '4' " 
	            + " order by card_no Asc ";
	    log("Cmd:" + wp.sqlCmd);
	    setString(corpPSeqno1);
	    this.pageQuery();
	    wp.setListCount(6);
  }

  // --Relidno 查詢聯絡人列表
  public void queryRelidno() throws Exception {
    String lswhere = "";

    if (!empty(wp.itemStr("ex_corp_no"))) {
        lswhere += sqlCol(wp.itemStr("ex_corp_no"), "corp_no");
      }

    if (!empty(wp.itemStr("ex_cname"))) {
        lswhere += sqlCol(wp.itemStr("ex_cname"), "chi_name", "like%");
      }
    if (empty(lswhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    wp.sqlCmd = " select " + " corp_p_seqno ," + " corp_no , " + " chi_name " + " FROM crd_corp "
        + " where 1=1 " + lswhere + " order by corp_no";
    log("CMD:" + wp.sqlCmd);
    this.pageQuery();
    wp.setListCount(5);
    if (sqlRowNum <= 0) {
      alertErr2("此條件查無資料");
      return;
    }
  }

  /**
   * 查詢聯絡人詳情
   * 
   * @throws Exception
   */
  public void querySelectRelidno() throws Exception {
    corpPSeqno1 = wp.itemStr("data_k1");
    dataReadRelidno();
  }
  
  /**
   * 查詢聯係人詳情
   * 
   * @throws Exception
   */
  void dataReadRelidno() throws Exception {
    wp.selectSQL = " *";
    wp.daoTable = "crd_corp ";
    wp.whereStr = " where 1=1 " + sqlCol(corpPSeqno1, "corp_p_seqno");
    this.logSql();
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=" + corpPSeqno1);
      return;
    }

  }
  
  
 //--RelatedParties 查詢關係人列表
 public void queryRelatedParties() throws Exception {
    String lsWhere = "";
    if (!empty(wp.itemStr("ex_corp_no"))) {
        lsWhere += sqlCol(wp.itemStr("ex_corp_no"), "corp_no");
      }
  
    if (!empty(wp.itemStr("ex_cname"))) {
        lsWhere += sqlCol(wp.itemStr("ex_cname"), "chi_name", "like%");
      }
  
    if (empty(lsWhere)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    
    wp.sqlCmd = " select " + " corp_p_seqno ," + " corp_no , " + " chi_name " + " FROM crd_corp "
        + " where 1=1 " + lsWhere + " order by corp_no";
    log("CMD:" + wp.sqlCmd);
    logSql();
    this.pageQuery();
    wp.setListCount(4);
 }

  
  public void relatedPartiesDetails() throws Exception {

    wp.selectSQL = " * , rela_type||id_no as mix_data , "
        + " decode(rela_type,'1','實質受益人','2','高階管理人','3','具控制權人') as tt_rela_type ";
    wp.daoTable = "crd_corp_rela";
    wp.whereStr = " where 1=1 " + sqlCol(wp.itemStr("corp_no"), "corp_no");

    pageQuery();
    if (sqlNotFind()) {
      selectOK();
      wp.colSet("ind_num", "0");
    }
    wp.setListCount(0);
    wp.colSet("ind_num", wp.selectCnt);
    relatedPartiesDetailsAfter();
  }

  void relatedPartiesDetailsAfter() {
    int ilSelectCnt = 0, ilTmpCnt = 0, rr = 0;
    String lsTmpType = "", lsTmpIdNo = "", lsTmpChiName = "";
    String lsTmpEngName = "", lsTmpBirthday = "", lsTmpCntryCode = "", lsTmpAudcode = "";
    boolean ibDup = false;
    ilSelectCnt = wp.selectCnt;
    rr = wp.selectCnt;
    String sql1 = " select * from ecs_moddata_tmp where 1=1 and tmp_pgm ='cmsm2020' "
        + " and tmp_table = 'CRD_CORP_RELA' and tmp_key like ? ";
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
        if (wp.colEq(zz, "rela_type", lsTmpType) && wp.colEq(zz, "id_no", lsTmpIdNo)) {
          wp.colSet(zz, "chi_name", lsTmpChiName);
          wp.colSet(zz, "eng_name", lsTmpEngName);
          wp.colSet(zz, "birthday", lsTmpBirthday);
          wp.colSet(zz, "cntry_code", lsTmpCntryCode);
          wp.colSet(zz, "mod_type", lsTmpAudcode);
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
      rr++;
    }

    wp.listCount[0] = rr;
    wp.colSet("ind_num", rr);
  }

  
//--accounting 查詢賬務總覽列表
 public void queryAccounting() throws Exception {
   String lswhere = "";
   String corpNo = wp.itemStr("ex_corp_no");
   if (!empty(corpNo)) {
     int len = corpNo.length();
     if (len < 8 || len > 11) {
       alertErr2("統編碼只允許8~11碼");
       return;
     }
     if (len == 8) {
       if (!NumberUtils.isDigits(corpNo)) {
         alertErr2("統編碼8碼時只允許數字");
         return;
       }
     }
     lswhere += sqlCol(corpNo, "B.corp_no");
   }
   if (!empty(wp.itemStr("ex_cname"))) {
       lswhere += sqlCol(wp.itemStr("ex_cname"), "B.chi_name", "like%");
     }

   if (empty(lswhere)) {
     alertErr2("請輸入查詢條件");
     return;
   }

   lswhere += " and A.acno_flag ='2' ";
   wp.sqlCmd =
       "SELECT A.acno_p_seqno , " + " A.acct_type , " + " A.acct_key , " + " A.corp_p_seqno ,"
         + "A.acct_key , " + "A.stmt_cycle , " 
         + "A.acct_status , " + "B.corp_no , "
//         + "decode(A.acct_status, '1', '1.正常', '') as status , "  // JustinWu 2020-09-04
         + "decode(B.corp_act_type, '1', '1.總繳  ', '2','2.個繳', '') as corp_act_type_desc , "
         + " A.card_indicator ," + " B.chi_name " 
         + " FROM act_acno A, crd_corp B "
         + " where A.corp_p_seqno = B.corp_p_seqno " + lswhere + " order by A.acct_key";
   logSql();
   this.pageQuery();
   if (sqlRowNum <= 0) {
     alertErr2("此條件查無資料");
     return;
   }
   
   setAcctStatusDesc();  // JustinWu 2020-09-04
   
   wp.setListCount(6);
 }
 
	private void setAcctStatusDesc() {
		
		for (int i = 0; i < sqlRowNum; i++) {
			wp.colSet(i, "acct_status_desc", ecsfunc.DeCodeAct.acctStatus(wp.colStr(i, "acct_status")));
		}

	}

/**
  * 查詢查詢賬務總覽詳情列表
  * 
  * @throws Exception
  */
 public void selectAccountingDetail() throws Exception {
     corpPSeqno1 = wp.itemStr("data_k1");
     corpPSeqno = wp.itemStr("data_k2");
     String accountType = wp.itemStr("data_k3");
     wp.selectSQL =
                "act_acno.acno_p_seqno as p_seqno , "
             + "act_acno.acno_flag as acno_flag , "
             + "crd_corp.corp_no , "
             + "crd_corp.chi_name as company_name, "
             + "crd_corp.corp_act_type , "
             + "act_acno.int_rate_mcode as int_rate_mcode , "
             + "act_acno.payment_rate1 as payment_rate1 , "
             + "decode(crd_corp.corp_act_type, '1', '1.總繳  ', '2','2.個繳', '') as corp_act_type_desc , "
             + "act_acno.acct_type , "
             + "acct_key , "
             + "acct_status , "
             + "act_acno.id_p_seqno , "
             + "act_acno.corp_p_seqno , "
             + "crd_idno.id_no , "
             //+ "crd_idno.chi_name , "
             + "uf_acno_name(act_acno.acno_p_seqno) as chi_name, "
             // (case when nvl(chkcard.ll_cnt, 0) > 0 then 'Y' else 'N' end) as db_curr
             // (case when nvl(chkcard.ll_cnt, 0) > 0 then '是' else '否' end) as db_curr_desc
             + "act_acno.acct_status,"
             + "decode(act_acno.ACNO_FLAG,'Y','0',act_acct.acct_jrnl_bal) AS acct_jrnl_bal , "
             + "decode(act_acno.ACNO_FLAG,'Y','0',act_acct.TTL_AMT) AS ttl_amt , "
             + "decode(act_acno.ACNO_FLAG,'Y','0',act_acct.TTL_amt_bal) AS ttl_amt_bal , "
             + "decode(act_acno.ACNO_FLAG,'Y','0',act_acct.MIN_PAY) AS  min_pay , "
             + "decode(act_acno.ACNO_FLAG,'Y','0',act_acct.MIN_PAY_bal) AS min_pay_bal";
     wp.daoTable = "act_acno left join crd_corp on act_acno.corp_p_seqno=crd_corp.corp_p_seqno "
         + "left join crd_idno on act_acno.id_p_seqno = crd_idno.id_p_seqno "
         + "left join act_acct on act_acno.p_seqno = act_acct.p_seqno "
         + "left join ("
         + "select p_seqno, count(*) as ll_cnt from crd_card where current_code = '0' group by p_seqno) chkcard"
         +" on act_acno.p_seqno = chkcard.p_seqno";
     wp.whereStr = "WHERE act_acno.acno_flag<>'1' "    //排除掉一般信用卡，僅顯示商務卡
         //+ "AND crd_corp.corp_no=:corp_no" 
         + sqlCol(corpPSeqno1, "crd_corp.corp_no")
         + sqlCol(accountType, "act_acno.acct_type");
     
     wp.whereOrder = " order by acno_flag ";
     this.logSql();
     pageQuery();
     if (sqlNotFind()) {
       alertErr("查無資料, key=" + corpPSeqno1);
       return;
     }
   wp.setListCount(1);
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
