/**
 *112-05-09  V1.00.33   Sunny     [行員註記及所屬單位]兩欄調整為唯讀不可修改
 *112-08-09  V1.00.32   Ryan        mark selectCmsChgColumnLog, 郵件信箱, 最後異動日期改為讀取act_acno/dba_acno *
 *112-05-09  V1.00.31   Yang Bo   [拒絕行銷註記]一欄調整為唯讀不可修改
 *112-02-02  V1.00.30   Sunny     增加顯示【自動結匯台幣帳號】【繳款編號】【繳款編號II】
 *111-11-17  V1.00.29   Sunny     調整帳單訂閱註記為定義為1-4及處理邏輯。
 *111-03-02  V1.00.28   JustinWu  新增異動日期及時間在待覆核資訊
 *111-02-24  V1.00.27   JustinWu  修改select欄位
 *110-10-05  V1.00.26   JustinWu  add zip2
 *110-08-20  V1.00.25   JustinWu  fix error messages
 *110-02-08  V1.00.24   JustinWu  correct the wrong error conditions: crd and dbc
 *110-01-04  V1.00.23   JustinWu  fix a bug of setServiceYear
 *109-12-22  V1.00.22   JustinWu  concate sql  -> parameterize sql
 *109-12-16  V1.00.21   JustinWu  add cleanModCol()
 *109-12-15  V1.00.20   JustinWu   修正國別碼
 *109-12-09  V1.00.19   JustinWu   change to select eng_name from dbc_idno instead of dbc_card, remove 外幣卡
 *109-10-15  V1.00.18  JustinWu   modify the method to query the detail of acct  
 *109-09-03  V1.00.17  JustinWu   調整vip_desc
 *109-09-01  V1.00.16  JustinWu   modify the the parameter name of ename
 *109-08-28  V1.00.15  JustinWu   wk_ -> wk_autopay_id_and_desc
 *109-08-18  V1.00.14  JustinWu   修改options html的錯誤,bill_apply_flag -> bill_apply_flag_options, bill_apply_flag
 *109-08-03  V1.00.13  JustinWu   add ajax
 *109-07-30  V1.00.12  JustinWu   remove db_web_card_member   
 *109-06-29  V1.00.11  zuwei          idno改爲判斷是不是10碼，使用等於條件查詢,「証」為「證」
 *109-06-28  V1.00.10  zuwei         增加AJAX調用
 *109-06-23  V1.00.09  tanwei        添加判斷值
 *109-06-11  V1.00.08  tanwei        默認下拉選中
 *109-05-29  V1.00.07  shiyuqi       新增欄位    
 *109-04-19  V1.00.06  shiyuqi       updated for project coding standard     
 *2019-1219  V1.00.05  Alex            ptr_branch -> gen_brn
 *2019-1211  V1.00.04  Alex            bug fix
 *2019-1205  V1.00.03  Alex            add initButton
 *2019-1203  V1.00.02  Alex            add father_name,mother_name,risk_level,risk_level_date 
 *2019-0418  V1.00.01  JH
 * */
package cmsm01;
import busi.func.CmsFunc;
import ecsfunc.DeCodeAct;
import ofcapp.BaseAction;
import taroko.com.TarokoCommon;

public class Cmsm2010 extends BaseAction {
  String acnoPSeqno = "", idPSeqno = "", dataKK3 = "",corpPSeqno = "";

  @Override
  public void userAction() throws Exception {
	  wp.pageRows = 900; //限制查詢筆數
    if (eqIgno(wp.buttonCode, "X")) {
      /* 轉換顯示畫面 */
      strAction = "new";
      clearFunc();
    } else if (eqIgno(wp.buttonCode, "Q")) {
      /* 查詢功能 */
      strAction = "Q";
      queryFunc();
    } else if (eqIgno(wp.buttonCode, "Q1")) {
      strAction = "idno";
      wp.colSet("pageType", "idno");
      resetBtn();
      wp.colSet("bt_class1", "btOther2");
      queryIdno();
    } else if (eqIgno(wp.buttonCode, "Q2")) {
      strAction = "acno";
      wp.colSet("pageType", "acno");
      resetBtn();
      wp.colSet("bt_class2", "btOther2");
      queryFuncAcno();
    } else if (eqIgno(wp.buttonCode, "Q3")) {
      strAction = "card";
      wp.colSet("pageType", "card");
      resetBtn();
      wp.colSet("bt_class3", "btOther2");
      queryFuncCard();
    } else if (eqIgno(wp.buttonCode, "R")) {
      // -資料讀取-
      strAction = "R";
      if (eqIgno(wp.itemStr("debit_flag"), "Y")) {
        dataReadDbidno();
      } else if (eqIgno(wp.itemStr("debit_flag"), "N")) {
        dataRead();
      }
    } else if (eqIgno(wp.buttonCode, "R2")) {
      // -資料讀取-
      strAction = "R";
      if (eqIgno(wp.itemStr("debit_flag"), "Y")) {
        dataReadDbAcno();
      } else if (eqIgno(wp.itemStr("debit_flag"), "N")) {
        dataReadACNO();
      }

    } else if (eqIgno(wp.buttonCode, "R3")) {
      // -資料讀取-
      strAction = "R";
      if (eqIgno(wp.itemStr("debit_flag"), "Y")) {
        dataReadDbCARD();
      } else if (eqIgno(wp.itemStr("debit_flag"), "N")) {
        dataReadCARD();
      }

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
    } else if (eqIgno(wp.buttonCode, "S1")) {
      /* 動態查詢 */
      querySelectACNO();
    } else if (eqIgno(wp.buttonCode, "S2")) {
      /* 動態查詢 */
      querySelectCARD();
    } else if (eqIgno(wp.buttonCode, "L")) {
      /* 清畫面 */
      strAction = "";
      if (eqIgno(wp.respHtml, "cmsm2010_card"))
        idPSeqno = wp.itemStr2("id_p_seqno");
      clearFunc();
      if (eqIgno(wp.respHtml, "cmsm2010_card"))
        selcetDetl();
    } else if (eqIgno(wp.buttonCode, "C")) {
      // -資料處理-
      procFunc();
    } else if (eqIgno(wp.buttonCode, "AJAX")) {
        // -AJAX-
    	wfAjaxKey(wp);
      }
		if (getIsQueryOverLimit()) {
			alertErr2("查詢筆數超出網頁可顯示筆數，請增加查詢條件");
		}
  }

  @Override
  public void dddwSelect() {
    try {
      if (eqIgno(wp.respHtml, "cmsm2010_IDNO")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "staff_br_no");
        dddwList("d_dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1");
        
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "resident_zip");
        dddwList("d_dddw_zipcode",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");

        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "company_zip");
        dddwList("d_dddw_zipcode1",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
     
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "mail_zip");
        dddwList("d_dddw_zipcode2",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
     
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "business_code");
        dddwList("d_dddw_bussmsg", "CRD_MESSAGE", "msg_value", "MSG", "where MSG_TYPE ='BUS_CODE'");
     
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "education");
        dddwList("d_dddw_education", "CRD_MESSAGE", "msg_value", "MSG", "where MSG_TYPE ='EDUCATION'");

        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "other_cntry_code");
        dddwList("d_dddw_other_cntry_code", "CCA_COUNTRY", "COUNTRY_CODE", "COUNTRY_REMARK", "where 1=1 ");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "cmsm2010_ACNO")) {
    	  
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "vip_code");
        dddwList("dddw_vip_code",
            "select distinct vip_code as db_code , vip_code || '.' ||vip_desc as db_desc from ptr_vip_code where 1=1");
     
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "risk_bank_no");
        dddwList("d_dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1");
        
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "bill_sending_zip");
        dddwList("d_dddw_zipcode",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
      
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "cmsm2010_DBACNO")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "bill_sending_zip");
        dddwList("d_dddw_zipcode",
            "select zip_code as db_code , zip_code||' '||zip_city||' '||zip_town as db_desc from ptr_zipcode where 1=1 ");
      }
    } catch (Exception ex) {
    }

    try {
      if (eqIgno(wp.respHtml, "cmsm2010_CARD")) {
        wp.initOption = "--";
        wp.optionKey = wp.colStr(0, "reg_bank_no");
        dddwList("d_dddw_branch", "gen_brn", "branch", "full_chi_name", "where 1=1");
      }
    } catch (Exception ex) {
    }

  }

  void queryIdno() throws Exception {
    String lsWhere1 = "", lsWhere2 = "";
    String lsIdno = wp.itemStr("ex_idno");
    int parmCnt = 0;
    // 2020/06/29 改爲判斷是不是10碼，使用等於條件查詢
    if (lsIdno.length() == 10) {
      lsWhere1 += " and id_no = :id_no ";
      lsWhere2 += " and id_no = :id_no ";
      setString("id_no", lsIdno);
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_card where card_no = :card_no )";
        lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_card where card_no = :card_no ) ";
        setString("card_no", wp.itemStr("ex_card_no"));
    }

    if (!empty(wp.itemStr("ex_id_name"))) {
        lsWhere1 += " and chi_name = :chi_name ";
        lsWhere2 += " and chi_name = :chi_name ";
        setString("chi_name", wp.itemStr("ex_id_name"));
    }

    if (empty(lsWhere1) || empty(lsWhere2)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    
    daoTid = "A.";
    wp.sqlCmd = "SELECT " + " 'N' as debit_flag ," + " id_no , " + " id_no_code , " + " chi_name , "
        + " sex ," + " birthday ," + " id_p_seqno ," + " '' as db_ename " + " FROM crd_idno"
        + " where 1=1 " + lsWhere1;
    // -ecs_act_acno-
    wp.sqlCmd += " UNION " + "SELECT " + " 'Y' as debit_flag," + " id_no, " + " id_no_code,"
        + " chi_name," + " sex," + " birthday," + " id_p_seqno," + " eng_name as db_ename"
        + " FROM dbc_idno" + " where 1=1 " + lsWhere2 + " order by 1 ";

    this.pageQuery();
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "A.debit_flag", "Y")) {
    	  // 2020/12/09 Justin change to select eng_name from dbc_idno
//       selectDbcename(ii, wp.colStr(ii, "A.id_p_seqno"));
      } else if (wp.colEq(ii, "A.debit_flag", "N")) {
        selectCrdename(ii, wp.colStr(ii, "A.id_p_seqno"));
      }
    }
    wp.setListCount(1);
  }

  void queryFuncAcno() throws Exception {
    String lsWhere1 = "", lsWhere2 = "";
    lsWhere1 = "";
    lsWhere2 = "";
    String lsIdno1 = wp.itemStr("ex_idno");
    // 2020/06/29 改爲判斷是不是10碼，使用等於條件查詢
    if (lsIdno1.length() == 10) {
      lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = :id_no ) " ;
      lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where id_no = :id_no ) " ;
      setString("id_no", lsIdno1);
    } else if (lsIdno1.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere1 += " and acno_p_seqno in (select acno_p_seqno from crd_card where card_no = :card_no ) ";
        lsWhere2 += " and p_seqno in (select p_seqno from dbc_card where card_no = :card_no )" ;
        setString("card_no", wp.itemStr("ex_card_no"));
      }

    if (!empty(wp.itemStr("ex_id_name"))) {
        lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_idno where chi_name = :chi_name ) " ;
        lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where chi_name = :chi_name )";
        setString("chi_name", wp.itemStr("ex_id_name"));
      }

    if (empty(lsWhere1) || empty(lsWhere2)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    daoTid = "B.";
    wp.sqlCmd = "SELECT acno_p_seqno , " 
        + " acct_type , " 
    	+ " acct_key , " 
        + " corp_p_seqno ,"
        + " id_p_seqno ," 
        + " card_indicator ," 
        + " uf_acno_name(acno_p_seqno) as db_acno_name,"
        + " combo_acct_no as acct_no," 
        + " 'N' as debit_flag " 
        + " FROM act_acno" 
        + " where 1=1 " + lsWhere1;
    // -ecs_act_acno-
    wp.sqlCmd += " UNION " 
        + "SELECT p_seqno as acno_p_seqno, " 
    	+ " acct_type," 
        + " acct_key,"
        + " corp_p_seqno," 
        + " id_p_seqno," 
        + " card_indicator,"
        + " uf_VD_acno_name(p_seqno) as db_acno_name ," 
        + " acct_no," 
        + " 'Y' as debit_flag "
        + " FROM dba_acno" 
        + " where 1=1 " + lsWhere2 
        + " order by acct_type";

    this.pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("查無正卡帳戶資料,請確認是否為本行卡友或是否為正卡持卡人");
    }
    list2Data();
    wp.setListCount(2);
  }

  void queryFuncCard() throws Exception {
    String lsWhere3 = "";
    String lsWhere4 = "";
    String lsIdno2 = wp.itemStr("ex_idno");
    // 2020/06/29 改爲判斷是不是10碼，使用等於條件查詢
    if (lsIdno2.length() == 10) {
      lsWhere3 += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = :id_no)" ;
      lsWhere4 += " and id_p_seqno in (select id_p_seqno from dbc_idno where id_no = :id_no)" ;
      setString("id_no", lsIdno2);
    } else if (lsIdno2.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere3 += " and card_no = :card_no ";
        lsWhere4 += " and card_no = :card_no ";
        setString("card_no", wp.itemStr("ex_card_no"));
     }

    if (!empty(wp.itemStr("ex_id_name"))) {
        lsWhere3 += " and id_p_seqno in (select id_p_seqno from crd_idno where chi_name = :chi_name ) " ;
        lsWhere4 += " and id_p_seqno in (select id_p_seqno from dbc_idno where chi_name = :chi_name ) " ;
        setString("chi_name", wp.itemStr("ex_id_name"));
      }

    if (empty(lsWhere3) || empty(lsWhere4)) {
      alertErr2("請輸入查詢條件");
      return;
    }

    daoTid = "C.";
    wp.sqlCmd = "SELECT " + " 'N' as debit_flag ," + " acct_type,"
        + " uf_acno_key(acno_p_seqno) as acct_key, " + " current_code ,"
        + " decode(current_code,'0',issue_date,oppost_date) , " + " card_no , " + " issue_date , "
        + " oppost_date , " + " id_p_seqno , " + " corp_p_seqno , " + " card_type ,"
        + " group_code ," + " oppost_reason ,"
        + " uf_opp_status(oppost_reason) as tt_oppost_reason, " + " curr_code,"
        + " uf_curr_name(curr_code) as tt_curr_code " + " FROM crd_card" + " where 1=1 "
        + lsWhere3;
    // -ecs_act_acno-
    wp.sqlCmd += " UNION " + "SELECT " + " 'Y' as debit_flag , " + " acct_type,"
        + " uf_VD_acno_key(p_seqno) as acct_key ," + " current_code,"
        + " decode(current_code,'0',issue_date,oppost_date)," + " card_no, " + " issue_date , "
        + " oppost_date , " + " id_p_seqno," + " corp_p_seqno," + " card_type," + " group_code,"
        + " oppost_reason," + " uf_opp_status(oppost_reason) as tt_oppost_reason, "
        + " '901' as curr_code," + " uf_curr_name('901') as tt_curr_code " + " FROM dbc_card"
        + " where 1=1 " + lsWhere4 + " order by 1,2,3,4,5 Desc ,6"
    // + " order by 1,2 Desc ,3 Desc "
    ;

    logSql();
    this.pageQuery();
    // queryAfter_Card();
    wp.setListCount(3);
  }

  @Override
  public void queryFunc() throws Exception {
    String lsWhere1 = "", lsWhere2 = "";
    String lsIdno = wp.itemStr("ex_idno");
    // 2020/06/29 改爲判斷是不是10碼，使用等於條件查詢
    if (lsIdno.length() == 10) {
      lsWhere1 += " and id_no = :id_no ";
      lsWhere2 += " and id_no = :id_no ";
      setString("id_no", lsIdno);
    } else if (lsIdno.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_card where card_no = :card_no )";
        lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_card where card_no = :card_no )";
        setString("card_no", wp.itemStr("ex_card_no"));
      }

    if (!empty(wp.itemStr("ex_id_name"))) {
        lsWhere1 += " and chi_name = :chi_name ";
        lsWhere2 += " and chi_name = :chi_name ";
        setString("chi_name", wp.itemStr("ex_id_name"));
    }
    String zipCode = wp.itemStr("ax_zip_code");
    if ((empty(lsWhere1) || empty(lsWhere2)) && empty(zipCode)) {
      alertErr2("請輸入查詢條件");
      return;
    }
    daoTid = "A.";
    wp.sqlCmd = "SELECT " + " 'N' as debit_flag ," + " id_no , " + " id_no_code , " + " chi_name , "
        + " sex ," + " birthday ," + " id_p_seqno ," + " '' as db_ename " + " FROM crd_idno"
        + " where 1=1 " + lsWhere1;
    // -ecs_act_acno-
    wp.sqlCmd += " UNION " + "SELECT " + " 'Y' as debit_flag," + " id_no, " + " id_no_code,"
        + " chi_name," + " sex," + " birthday," + " id_p_seqno," + " '' as db_ename"
        + " FROM dbc_idno" + " where 1=1 " + lsWhere2 + " order by 1 ";
    logSql();
    this.pageQuery();
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "A.debit_flag", "Y")) {
        selectDbcename(ii, wp.colStr(ii, "A.id_p_seqno"));
      } else if (wp.colEq(ii, "A.debit_flag", "N")) {
        selectCrdename(ii, wp.colStr(ii, "A.id_p_seqno"));
      }
    }
    wp.setListCount(1);

    lsWhere1 = "";
    lsWhere2 = "";
    String lsIdno1 = wp.itemStr("ex_idno");
    // 2020/06/29 改爲判斷是不是10碼，使用等於條件查詢
    if (lsIdno1.length() == 10) {
      lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = :id_no ) " ;
      lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where id_no = :id_no ) " ;
      setString("id_no", lsIdno);
    } else if (lsIdno1.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere1 += " and acno_p_seqno in (select acno_p_seqno from crd_card where card_no = :card_no ) " ;
        lsWhere2 += " and acno_p_seqno in (select acno_p_seqno from dbc_card where card_no = :card_no ) " ;
        setString("card_no", wp.itemStr("ex_card_no"));
    }

    if (!empty(wp.itemStr("ex_id_name"))) {
        lsWhere1 += " and id_p_seqno in (select id_p_seqno from crd_idno where chi_name = :chi_name ) " ;
        lsWhere2 += " and id_p_seqno in (select id_p_seqno from dbc_idno where chi_name = :chi_name ) " ;
        setString("chi_name", wp.itemStr("ex_id_name"));
    }

    daoTid = "B.";
    wp.sqlCmd = "SELECT acno_p_seqno , " + " acct_type , " + " acct_key , " + " corp_p_seqno ,"
        + " id_p_seqno ," + " card_indicator ," + " uf_acno_name(acno_p_seqno) as db_acno_name,"
        + " combo_acct_no as acct_no," + " 'N' as debit_flag " + " FROM act_acno" + " where 1=1 "
        + lsWhere1;
    // -ecs_act_acno-
    wp.sqlCmd += " UNION " + "SELECT p_seqno, " + " acct_type," + " acct_key," + " corp_p_seqno,"
        + " id_p_seqno," + " card_indicator," + " uf_VD_acno_name(p_seqno) as db_acno_name ,"
        + " acct_no," + " 'Y' as debit_flag " + " FROM dba_acno" + " where 1=1 " + lsWhere2;

    this.pageQuery();
    if (sqlRowNum <= 0) {
      alertErr2("查無正卡帳戶資料,請確認是否為本行卡友或是否為正卡持卡人");
    }
    list2Data();
    wp.setListCount(2);

    String lsWhere3 = "";
    String lsWhere4 = "";
    String lsIdno2 = wp.itemStr("ex_idno");
    // 2020/06/29 改爲判斷是不是10碼，使用等於條件查詢
    if (lsIdno2.length() == 10) {
      lsWhere3 += " and id_p_seqno in (select id_p_seqno from crd_idno where id_no = :id_no ) " ;
      lsWhere4 += " and id_p_seqno in (select id_p_seqno from dbc_idno where id_no = :id_no ) " ;
      setString("id_no", lsIdno2);
    } else if (lsIdno2.length() > 0) {
      alertErr2("身分證號 輸入不完整");
      return;
    }

    if (!empty(wp.itemStr("ex_card_no"))) {
        lsWhere3 += " and card_no = :card_no ";
        lsWhere4 += " and card_no = :card_no ";
        setString("card_no", wp.itemStr("ex_card_no"));
    }

    if (!empty(wp.itemStr("ex_id_name"))) {
        lsWhere3 += " and id_p_seqno in (select id_p_seqno from crd_idno where chi_name = :chi_name ) ";
        lsWhere4 += " and id_p_seqno in (select id_p_seqno from dbc_idno where chi_name = :chi_name ) "; 
        setString("chi_name", wp.itemStr("ex_id_name"));
      }

    daoTid = "C.";
    wp.sqlCmd = "SELECT " + " 'N' as debit_flag ," + " acct_type,"
        + " uf_acno_key(acno_p_seqno) as acct_key, " + " current_code ,"
        + " decode(current_code,'0',issue_date,oppost_date) , " + " card_no , " + " issue_date , "
        + " oppost_date , " + " id_p_seqno , " + " corp_p_seqno , " + " card_type ,"
        + " group_code ," + " oppost_reason ,"
        + " uf_opp_status(oppost_reason) as tt_oppost_reason, " + " curr_code,"
        + " uf_curr_name(curr_code) as tt_curr_code " + " FROM crd_card" + " where 1=1 "
        + lsWhere3;
    // -ecs_act_acno-
    wp.sqlCmd += " UNION " + "SELECT " + " 'Y' as debit_flag , " + " acct_type,"
        + " uf_VD_acno_key(p_seqno) as acct_key ," + " current_code,"
        + " decode(current_code,'0',issue_date,oppost_date)," + " card_no, " + " issue_date , "
        + " oppost_date , " + " id_p_seqno," + " corp_p_seqno," + " card_type," + " group_code,"
        + " oppost_reason," + " uf_opp_status(oppost_reason) as tt_oppost_reason, "
        + " '901' as curr_code," + " uf_curr_name('901') as tt_curr_code " + " FROM dbc_card"
        + " where 1=1 " + lsWhere4 + " order by 1,2,3,4,5 Desc ,6"
    // + " order by 1,2 Desc ,3 Desc "
    ;

    this.pageQuery();
    // queryAfter_Card();
    wp.setListCount(3);

  }
  // void queryAfter_Card(){
  // String sql1 = " select opp_remark from cca_opp_type_reason where opp_status = ? ";
  // String sql2 = " select curr_chi_name from ptr_currcode where curr_code = ? ";
  // for(int ii=0;ii<wp.selectCnt;ii++){
  // if(!empty(wp.col_ss(ii,"C.oppost_reason"))){
  // sqlSelect(sql1,new Object[]{wp.col_ss(ii,"C.oppost_reason")});
  // if(sql_nrow>0){
  // wp.col_set(ii,"C.tt_oppost_reason","."+sql_ss("opp_remark"));
  // }
  // }

  // if(!empty(wp.col_ss(ii,"C.curr_code"))){
  // sqlSelect(sql2,new Object[]{wp.col_ss(ii,"C.curr_code")});
  // if(sql_nrow>0){
  // wp.col_set(ii,"C.tt_curr_code","."+sql_ss("curr_chi_name"));
  // }
  // }
  // }
  // }
  void selectDbcename(int ii, String lsIdPSeqno) {
    String sql1 = "select eng_name  from dbc_card  where id_p_seqno =?";
    sqlSelect(sql1, new Object[] {lsIdPSeqno});
    wp.colSet(ii, "A.db_ename", sqlStr("eng_name"));
  }

  void selectCrdename(int ii, String lsIdPSeqno) {
    String sql1 = "select eng_name  from crd_card  where id_p_seqno =?";
    sqlSelect(sql1, new Object[] {lsIdPSeqno});
    wp.colSet(ii, "A.db_ename", sqlStr("eng_name"));

  }

  void list2Data() {
    String sql1 = " select chi_name from crd_corp where corp_p_seqno = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "B.card_indicator", "1")) {
        wp.colSet(ii, "B.card_indicator", "1.一般卡");
      } else if (wp.colEq(ii, "B.card_indicator", "2")) {
        wp.colSet(ii, "B.card_indicator", "2.商務卡");
      } 

      if (wp.colEmpty(ii, "B.corp_p_seqno") == false) {
        sqlSelect(sql1, new Object[] {wp.colStr(ii, "B.corp_p_seqno")});
        if (sqlRowNum > 0) {
          wp.colSet(ii, "B.db_acno_name", sqlStr("chi_name"));
        }
      }

    }
  }

  @Override
  public void queryRead() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void querySelect() throws Exception {
    acnoPSeqno = wp.itemStr("data_k1");
    dataKK3 = wp.itemStr("data_k3");
    if (eqIgno(dataKK3, "Y")) {
      dataReadDbidno();
    } else if (eqIgno(dataKK3, "N")) {
      dataRead();
    }

    commMarketAgreeBase("market_agree_base");
    
    // --行員單位
    String sql4 = " select full_chi_name from gen_brn where branch = ? ";
    sqlSelect(sql4, new Object[] {wp.colStr("staff_br_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_staff_br_no", sqlStr("full_chi_name"));
    } else {
      wp.colSet("tt_staff_br_no", "");
    }
  }

  private void commMarketAgreeBase(String cde1) {
    if (cde1 == null || cde1.trim().length() == 0) {
      return;
    }
    String[] cde = {"0", "1", "2"};
    String[] txt = {"不同意", "同意共銷", "同意共享"};

    wp.colSet("comm_" + cde1, "");
    String colStr = wp.colStr(cde1);
    if (!empty(colStr)) {
      for (int inti = 0; inti < cde.length; inti++) {
        if (colStr.equals(cde[inti])) {
          wp.colSet("comm_" + cde1, txt[inti]);
          break;
        }
      }
    }
  }

  @Override
  public void dataRead() throws Exception {
    if (empty(acnoPSeqno)) {
      acnoPSeqno = wp.itemStr("id_p_seqno");
    }

    if (empty(acnoPSeqno)) {
      alertErr2("請從瀏覽頁面重新讀取資料");
      return;
    }

    wp.selectSQL = " *  ";
    wp.daoTable = "crd_idno";
//    wp.whereStr = "where 1=1" 
//                                + sqlCol(dataKK1, "id_p_seqno");
    wp.whereStr = "where 1=1 ";
    if (!empty(acnoPSeqno)) {
    	wp.whereStr+= " and  id_p_seqno = ? ";
        setString(acnoPSeqno);
	}

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=[" + acnoPSeqno + "], dataRead");
    }
    
    setServiceYear(wp.colStr("service_year"));
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] residentZipArr = commString.splitZipCode(wp.colStr("resident_zip"));
    wp.colSet("resident_zip", residentZipArr[0]);
    wp.colSet("resident_zip2", residentZipArr[1]);
    String[] mailZipArr = commString.splitZipCode(wp.colStr("mail_zip"));
    wp.colSet("mail_zip", mailZipArr[0]);
    wp.colSet("mail_zip2", mailZipArr[1]);
    String[] companyZipArr = commString.splitZipCode(wp.colStr("company_zip"));
    wp.colSet("company_zip", companyZipArr[0]);
    wp.colSet("company_zip2", companyZipArr[1]);
    
    cmsm01.Cmsm2010Idno idno = new cmsm01.Cmsm2010Idno();
    String [] arr=new String[] {};
    idno.setConn(wp);    
    arr=idno.selectCmsChgColumnLog("crd_idno","cellar_phone",wp.colStr("id_no"));
    wp.colSet("c_phone_chg_date", arr[0]);
    wp.colSet("c_phone_chg_time", arr[1]);
    
    selectIdnoExt();
    selectIdnoOnline();
    if (wp.colEq("nation", "2") == false) {
      wp.colSet("disa_cntry_code", "disabled");
    }
  }

	void selectIdnoExt() {
    wp.selectSQL = " *  ";
    wp.daoTable = "crd_idno_ext";
    wp.whereStr = "where 1=1 ";
    if (!empty(acnoPSeqno)) {
    	wp.whereStr+= " and  id_p_seqno = ? ";
        setString(1, acnoPSeqno);
	}
    setString(1, acnoPSeqno);

    pageSelect();
    wp.notFound = "N";
  }

  void selectIdnoOnline() {
    cmsm01.Cmsm2010Idno func = new cmsm01.Cmsm2010Idno();
    func.setConn(wp);
    if (func.idnoOnline() == 1) {
        alertMsg(String.format("此筆資料待覆核(%s).....", wp.colStr("userModDateTime")));
    }else {
    	func.cleanModCol();
    }
  }

  // --ACNO
  public void querySelectACNO() throws Exception {
    acnoPSeqno = wp.itemStr("data_k1");
    idPSeqno = wp.itemStr("data_k2");
    dataKK3 = wp.itemStr("data_k3");
    if (eqIgno(dataKK3, "Y")) {
      dataReadDbAcno();
    } else if (eqIgno(dataKK3, "N")) {
      dataReadACNO();
    }


  }


  public void dataReadACNO() throws Exception {
     
    if (empty(acnoPSeqno)) {
      acnoPSeqno = wp.itemStr("acno_p_seqno");
    }
    if (empty(idPSeqno)) {
      idPSeqno = wp.itemStr("id_p_seqno");
    }
    if (empty(acnoPSeqno) || empty(idPSeqno)) {
      alertErr2("請從瀏覽頁面重新讀取資料");
      return;
    }
    if (empty(corpPSeqno)) {
      corpPSeqno = wp.itemStr("corp_p_seqno");
    }
    selcetDetl();
    wp.selectSQL = " aa.* , "
    		            + " cca.block_status , " 
    		            + " cca.block_reason1 , " 
    		            + " cca.block_reason2 , "
                        + " cca.block_reason3 , " 
    		            + " cca.block_reason4 , " 
                        + " cca.block_reason5 ";
    wp.daoTable = "act_acno as aa , cca_card_acct as cca ";
    wp.whereStr = "where 1=1 "
    		          + "AND aa.acno_p_seqno = cca.acno_p_seqno "
    		          + "AND cca.debit_flag <> 'Y' " 
    		          ;
    if (!empty(acnoPSeqno)) {
    	wp.whereStr+= "AND aa.acno_p_seqno = ? ";
        setString(1, acnoPSeqno);
	} 		    
    
    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=[" + acnoPSeqno + "], dataReadACNO");
      return;
    }
    
    String[] billSendingZipArr = commString.splitZipCode(wp.colStr("bill_sending_zip"));
    wp.colSet("bill_sending_zip", billSendingZipArr[0]);
    wp.colSet("bill_sending_zip2", billSendingZipArr[1]);
    
    wp.colSet("wk_acctkey", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));
    
    decodeAcno();
    
    selectActAcctCurr();
    cmsm01.Cmsm2010Acno func = new cmsm01.Cmsm2010Acno();
    func.setConn(wp);
    if (func.acnoOnline() == 1) {
    	alertMsg(String.format("此筆資料待覆核(%s).....", wp.colStr("userModDateTime")));
    }else {
    	func.cleanModCol();
    }
   
    //selectAddressFromCrdCorp();
	
//	 cmsm01.Cmsm2010Idno idno = new cmsm01.Cmsm2010Idno();
//    String [] arr=new String[] {};
//    idno.setConn(wp);    
//    arr=idno.selectCmsChgColumnLog("act_acno","e_mail_ebill",wp.itemStr("ex_idno"));
//    wp.colSet("e_mail_ebill_date", arr[0]);
    
    setBillApplyFlagDes();

  }
  
	private void setBillApplyFlagDes() {
		String billApplyFlag = wp.colStr("bill_apply_flag");
	    String billSendingZip = wp.colStr("bill_sending_zip");
	    String billSendingZip2 = wp.colStr("bill_sending_zip2");
	    String billSendingAddr1 = wp.colStr("bill_sending_addr1");
	    String billSendingAddr2 = wp.colStr("bill_sending_addr2");
	    String billSendingAddr3 = wp.colStr("bill_sending_addr3");
	    String billSendingAddr4 = wp.colStr("bill_sending_addr4");
	    String billSendingAddr5 = wp.colStr("bill_sending_addr5");
	    String bill_apply_flag_desc = "";
	    
	    StringBuilder sbOption = new StringBuilder();
	  //  String data4 = wp.itemStr("data_k4");
//	     if(data4.equals("2.商務卡"))
//	     {  
//	       sbOption.append("<option value=''>").append("--").append("</option> ");
//	       if("1".equals(billApplyFlag)) {
//	    	 bill_apply_flag_desc = "1.同戶籍";
//	         sbOption.append("<option value='1' ${bill_apply_flag-1} selected='selected'>").append(bill_apply_flag_desc).append("</option> ");
//	       }else{
//	         sbOption.append("<option value='1' ${bill_apply_flag-1}>").append(bill_apply_flag_desc).append("</option> ");
//	       }
//	       if("2".equals(billApplyFlag)) {
//	    	 bill_apply_flag_desc = "2.同居住";
//	         sbOption.append("<option value='2' ${bill_apply_flag-2} selected='selected'>").append(bill_apply_flag_desc).append("</option> ");
//	       }else{
//	         sbOption.append("<option value='2' ${bill_apply_flag-2}>").append(bill_apply_flag_desc).append("</option> ");
//	       }
//	       if("3".equals(billApplyFlag)) {
//	    	   bill_apply_flag_desc = "3.同公司";
//	         sbOption.append("<option value='3' ${bill_apply_flag-3} selected='selected'>").append(bill_apply_flag_desc).append("</option> ");
//	       }else{
//	         sbOption.append("<option value='3' ${bill_apply_flag-3}>").append(bill_apply_flag_desc).append("</option> ");
//	       }
//	       if("4".equals(billApplyFlag)) {
//	    	   bill_apply_flag_desc = "同通訊(法人)";
//	         sbOption.append("<option value='4' ${bill_apply_flag-4} selected='selected'>").append("同通訊(法人)").append("</option> ");
//	       }else{
//	         sbOption.append("<option value='4' ${bill_apply_flag-4}>").append("同通訊(法人)").append("</option> "); 
//	       }
//	       if("5".equals(billApplyFlag)) {
//	    	   bill_apply_flag_desc = "同公司(法人)";
//	         sbOption.append("<option value='5' ${bill_apply_flag-5} selected='selected'>").append("同公司(法人)").append("</option> ");
//	       }else{
//	         sbOption.append("<option value='5' ${bill_apply_flag-5}>").append("同公司(法人)").append("</option> ");
//	       }
//	       wp.colSet("bill_apply_flag_options", sbOption.toString());
//	     }
//	     else {
//一般卡及商務卡帳單註記的定義均相同
	       sbOption.append("<option value=''>").append("--").append("</option> ");
	       if("1".equals(billApplyFlag)) {
	    	   bill_apply_flag_desc = "1.同戶籍";
	         sbOption.append("<option value='1' ${bill_apply_flag-1} selected='selected'>").append("1.同戶籍").append("</option> ");
	       }else {
	         sbOption.append("<option value='1' ${bill_apply_flag-1}>").append("1.同戶籍").append("</option> ");
	       }
	       if("2".equals(billApplyFlag)) {
	    	   bill_apply_flag_desc = "2.同居住";
	         sbOption.append("<option value='2' ${bill_apply_flag-2} selected='selected'>").append("2.同居住").append("</option> ");
	       }else {
	         sbOption.append("<option value='2' ${bill_apply_flag-2} >").append("2.同居住").append("</option> ");
	       }
	       if("3".equals(billApplyFlag)) {
	    	   bill_apply_flag_desc = "3.同公司";
	         sbOption.append("<option value='3' ${bill_apply_flag-3} selected='selected'>").append("3.同公司").append("</option> ");
	       }else {
	         sbOption.append("<option value='3' ${bill_apply_flag-3}>").append("3.同公司").append("</option> ");
	       }
	       if("4".equals(billApplyFlag)) {
	    	   bill_apply_flag_desc = "4.其他";
	         sbOption.append("<option value='4' ${bill_apply_flag-4} selected='selected'>").append("4.其他").append("</option> ");
	       }else{
	         sbOption.append("<option value='4' ${bill_apply_flag-4}>").append("4.其他").append("</option> ");
	       }       
	       wp.colSet("bill_apply_flag_options", sbOption.toString());
//	      }
	     
		 // split zipCode into two parts, zip_code1 and zip_code2.
		 String[] billSendingZipArr = commString.splitZipCode(billSendingZip);
		 
	     wp.colSet("bill_apply_flag_desc", bill_apply_flag_desc);
//	     wp.colSet("bill_sending_zip", billSendingZip);
	     wp.colSet("bill_sending_zip",   billSendingZipArr[0]);
		 wp.colSet("bill_sending_zip2",  isEmpty(billSendingZip2) ? billSendingZipArr[1] : billSendingZip2);
	     wp.colSet("bill_sending_addr1", billSendingAddr1);
	     wp.colSet("bill_sending_addr2", billSendingAddr2);
	     wp.colSet("bill_sending_addr3", billSendingAddr3);
	     wp.colSet("bill_sending_addr4", billSendingAddr4);
	     wp.colSet("bill_sending_addr5", billSendingAddr5);
	}

//private void selectAddressFromCrdCorp() {
//    // TODO Auto-generated method stub
//    wp.selectSQL = "" + " reg_zip," + " reg_addr1, " + " reg_addr2, " + " reg_addr3, "
//        + " reg_addr4, " + " reg_addr5, " + " comm_zip," + " comm_addr1, " + " comm_addr2, "
//        + " comm_addr3, " + " comm_addr4, " + " comm_addr5 ";
//    wp.daoTable = " crd_corp ";
//    wp.whereStr = "where 1=1 " ;
//    if (! empty(corpPSeqno)) {
//    	wp.whereStr += " and corp_p_seqno = :corp_p_seqno " ;
//        setString("corp_p_seqno", corpPSeqno);
//	}
//
//    pageSelect();
//    
//    if (sqlRowNum > 0) {
//    	// split zipCode into two parts, zip_code1 and zip_code2.  
//        String[] regZipArr = commString.splitZipCode(wp.colStr("reg_zip"));
//        wp.colSet("reg_zip", regZipArr[0]);
//        wp.colSet("reg_zip2", regZipArr[1]);
//        String[] commZipArr = commString.splitZipCode(wp.colStr("comm_zip"));
//        wp.colSet("comm_zip", commZipArr[0]);
//        wp.colSet("comm_zip2", commZipArr[1]);
//	}
//  }

  void selcetDetl() {
    String sql1 = "select id_no ," + "id_no_code," + "birthday ," + "sex , " + "chi_name , "
        +  " resident_zip ," + " resident_addr1  ," + " resident_addr2  ,"
        + " resident_addr3  ," + " resident_addr4  ," + " resident_addr5  ," + " company_zip ,"
        + " company_addr1  ," + " company_addr2  ," + " company_addr3  ," + " company_addr4 ,"
        + " company_addr5  ," + " mail_zip  ," + " mail_addr1 ," + " mail_addr2 ," + " mail_addr3  ,"
        + " mail_addr4  ," + "mail_addr5 , " 
        + " resident_no , " + "other_cntry_code , " + "passport_no , " + "id_p_seqno  "
        + " from crd_idno " 
        + " where id_p_seqno =?";
    sqlSelect(sql1, new Object[] {idPSeqno});
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] residentZipArr = commString.splitZipCode(sqlStr("resident_zip"));
    String[] mailZipArr = commString.splitZipCode(sqlStr("mail_zip"));
    String[] companyZipArr = commString.splitZipCode(sqlStr("company_zip"));

    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
//    wp.colSet("resident_zip", sqlStr("resident_zip"));
    wp.colSet("resident_zip", residentZipArr[0]);
    wp.colSet("resident_zip2", residentZipArr[1]);
    wp.colSet("resident_addr1", sqlStr("resident_addr1"));
    wp.colSet("resident_addr2", sqlStr("resident_addr2"));
    wp.colSet("resident_addr3", sqlStr("resident_addr3"));
    wp.colSet("resident_addr4", sqlStr("resident_addr4"));
    wp.colSet("resident_addr5", sqlStr("resident_addr5"));
//    wp.colSet("company_zip", sqlStr("company_zip"));
    wp.colSet("company_zip", companyZipArr[0]);
    wp.colSet("company_zip2", companyZipArr[1]);
    wp.colSet("company_addr1", sqlStr("company_addr1"));
    wp.colSet("company_addr2", sqlStr("company_addr2"));
    wp.colSet("company_addr3", sqlStr("company_addr3"));
    wp.colSet("company_addr4", sqlStr("company_addr4"));
    wp.colSet("company_addr5", sqlStr("company_addr5"));
//    wp.colSet("mail_zip", sqlStr("mail_zip"));
    wp.colSet("mail_zip", mailZipArr[0]);
    wp.colSet("mail_zip2", mailZipArr[1]);
    wp.colSet("mail_addr1", sqlStr("mail_addr1"));
    wp.colSet("mail_addr2", sqlStr("mail_addr2"));
    wp.colSet("mail_addr3", sqlStr("mail_addr3"));
    wp.colSet("mail_addr4", sqlStr("mail_addr4"));
    wp.colSet("mail_addr5", sqlStr("mail_addr5"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));
    wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
  }

  void selectActAcctCurr() throws Exception {
    String lsAcctPseqno = acctPseqno(acnoPSeqno);
    wp.selectSQL = "" + " p_seqno ," + " acct_type ," + " curr_code ," + " autopay_indicator ,"
        + " autopay_acct_bank ," + " autopay_acct_no ," + " autopay_id ," + " autopay_id_code ,"
        + " autopay_dc_flag ," + " no_interest_flag ," + " no_interest_s_month ,"
        + " no_interest_e_month, " + " autopay_dc_indicator, " + " curr_change_accout ";
    wp.daoTable = "act_acct_curr";
    wp.whereStr = " where 1=1 ";
    
	if (! empty(lsAcctPseqno)) {
		wp.whereStr += " and p_seqno = ? ";
		setString(1, lsAcctPseqno);
	}
    
    this.pageQuery();
    if (sqlRowNum == 0) {
      wp.notFound = "N";
    }
    queryAfterAcctCurr();
    listWkdataAcno();
    wp.setListCount(1);
  }

  void queryAfterAcctCurr() {
    String sql1 = "select curr_chi_name from ptr_currcode where curr_code = ? ";
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      if (wp.colEq(ii, "autopay_indicator", "1")) {
        wp.colSet(ii, "tt_autopay_indicator", ".扣 TTL");
      } else if (wp.colEq(ii, "autopay_indicator", "2")) {
        wp.colSet(ii, "tt_autopay_indicator", ".扣 MP");
      }

      if (wp.colEq(ii, "autopay_dc_indicator", "1")) {
        wp.colSet(ii, "tt_autopay_dc_indicator", ".扣 TTL");
      } else if (wp.colEq(ii, "autopay_dc_indicator", "2")) {
        wp.colSet(ii, "tt_autopay_dc_indicator", ".扣 MP");
      }

      sqlSelect(sql1, new Object[] {wp.colStr(ii, "curr_code")});
      if (sqlRowNum > 0) {
        wp.colSet(ii, "tt_curr_code", "." + sqlStr("curr_chi_name"));
      }

    }
  }

  void decodeAcno() {

    if ( ! wp.colEmpty("acct_status")) {
		wp.colSet("tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("acct_status")));
    }

    if (wp.colEq("rc_use_indicator", "1")) {
      wp.colSet("tt_rc_use_indicator", ".正常允用");
    } else if (wp.colEq("rc_use_indicator", "2")) {
      wp.colSet("tt_rc_use_indicator", ".例外允用");
    } else if (wp.colEq("rc_use_indicator", "3")) {
      wp.colSet("tt_rc_use_indicator", ".不準允用 RC");
    }

    if (wp.colEq("rc_use_b_adj", "1")) {
      wp.colSet("tt_rc_use_b_adj", ".正常允用");
    } else if (wp.colEq("rc_use_b_adj", "2")) {
      wp.colSet("tt_rc_use_b_adj", ".例外允用");
    } else if (wp.colEq("rc_use_b_adj", "3")) {
      wp.colSet("tt_rc_use_b_adj", ".不準允用 RC");
    }

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

    // --帳戶屬性
    if (wp.colEq("corp_act_flag", "Y")) {
      wp.colSet("tt_corp_act_flag", ".總繳");
    } else if (wp.colEq("corp_act_flag", "N")) {
      wp.colSet("tt_corp_act_flag", ".個繳");
    } else if (wp.colEq("corp_act_flag", "個繳")) {
      wp.colSet("tt_corp_act_flag", ".個繳");
    }
    // --往來狀態
    if ( ! wp.colEmpty("acct_status")) {
		wp.colSet("tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("acct_status")));
    }
    // --凍結
    if (wp.colEq("block_status", "Y")) {
      wp.colSet("tt_block_status", ".凍結");
    } else if (wp.colEq("block_status", "N")) {
      wp.colSet("tt_block_status", "");
    }
    // --自動繳指示碼
    if (wp.colEq("autopay_indicator", "1")) {
      wp.colSet("tt_autopay_indicator", ".扣 TTL");
    } else if (wp.colEq("autopay_indicator", "2")) {
      wp.colSet("tt_autopay_indicator", ".扣 MP");
    }

    String sql1 =
        " select sum(decode(sup_flag,'0',1,0)) as valid_card_num," + " count(*) as valid_card_num2 "
            + " from crd_card" + " where (acno_p_seqno = ?) and (current_code = '0')";
    sqlSelect(sql1, new Object[] {wp.colStr("acno_p_seqno")});
    if (sqlRowNum > 0) {
      wp.colSet("valid_card_num", sqlStr("valid_card_num"));
      wp.colSet("valid_card_num2", sqlStr("valid_card_num2"));
    }

    // --風險行
    String sql2 = "select full_chi_name " + " from gen_brn" + " where branch =? ";
    sqlSelect(sql2, new Object[] {wp.colStr("risk_bank_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_risk_bank_no", "." + sqlStr("full_chi_name"));
    } else {
      wp.colSet("tt_risk_bank_no", "");
    }
    // --帳務屬性
    if (wp.colEq("corp_act_flag", "Y")) {
      wp.colSet("tt_corp_act_flag", ".總繳");
    } else if (wp.colEq("corp_act_flag", "N")) {
      wp.colSet("tt_corp_act_flag", ".個繳");
    }
    // --公司統編 名稱
    String sql4 = " select corp_no , chi_name from crd_corp where corp_p_seqno = ? ";
    if (wp.colEmpty("corp_p_seqno") == false) {
      sqlSelect(sql4, new Object[] {wp.colStr("corp_p_seqno")});
      if (sqlRowNum > 0) {
        wp.colSet("corp_no", sqlStr("corp_no"));
        wp.colSet("db_corpname", sqlStr("chi_name"));
      }
    }
    
  }

  void listWkdataAcno() {
    for (int ii = 0; ii < wp.selectCnt; ii++) {
      wp.colSet(ii, "wk_autopay_id_and_desc", wp.colEmpty(ii, "autopay_id") ? "" : wp.colStr(ii, "autopay_id") + "-" + wp.colStr(ii, "autopay_id_code"));
      wp.colSet(ii, "wk_no_int_month",
      wp.colStr(ii, "no_interest_s_month") + "-" + wp.colStr(ii, "no_interest_e_month"));
    }
  }

  // --CARD
  public void querySelectCARD() throws Exception {
    acnoPSeqno = wp.itemStr("data_k1");
    idPSeqno = wp.itemStr("data_k2");
    dataKK3 = wp.itemStr("data_k3");
    if (eqIgno(dataKK3, "Y")) {
      dataReadDbCARD();
    } else if (eqIgno(dataKK3, "N")) {
      dataReadCARD();
    }

  }

  public void dataReadCARD() throws Exception {
    if (empty(acnoPSeqno)) {
      acnoPSeqno = itemkk("card_no");
    }

    if (empty(acnoPSeqno)) {
      alertErr2("卡號不可空白");
      return;
    }

    wp.selectSQL = " A.* ," +" B.chi_name as introduce_chi_name ," + " uf_acno_key(A.acno_p_seqno) as acct_key , "
        + " A.card_no as B_card_no , " + " A.eng_name as B_eng_name ";
    wp.daoTable = "crd_card A left join CRD_EMPLOYEE B on A.introduce_id= B.id" ;
    wp.whereStr = "where 1=1 ";
    if (!empty(acnoPSeqno)) {
    	wp.whereStr+= " and card_no=? " ;
        setString(1, acnoPSeqno);
	}
    

    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, key=[" + acnoPSeqno + "], dataReadCARD");
      return;
    }
    cmsm01.Cmsm2010Card func = new cmsm01.Cmsm2010Card();
    func.setConn(wp);
    if (func.cardOnline() == 1) {
    	alertMsg(String.format("此筆資料待覆核(%s).....", wp.colStr("userModDateTime")));
    }else {
    	func.cleanModCol();
    }

    idPSeqno = wp.colStr("id_p_seqno");
    selcetDetl();
    wkdataCard();
  }

  public void dataReadDbCARD() throws Exception {
    if (empty(acnoPSeqno)) {
      acnoPSeqno = wp.itemStr("card_no");
    }
    if (empty(idPSeqno)) {
      idPSeqno = wp.itemStr("id_p_seqno");
    }

    if (empty(acnoPSeqno) || empty(idPSeqno)) {
      alertErr2("請從瀏覽頁面重新讀取資料");
      return;
    }

    selectDbDetl();
    
    wp.selectSQL = " A.*,"
                                 +" B.chi_name as introduce_chi_name ,"
    		                     +" uf_acno_key2(A.p_seqno,'Y') as acct_key ";
    wp.daoTable = "dbc_card A left join CRD_EMPLOYEE B on A.introduce_id= B.id  ";
    wp.whereStr = "where 1=1 ";
    
    if (!empty(acnoPSeqno)) {
    	wp.whereStr += "and card_no = ? ";
        setString(1, acnoPSeqno);
	}


    pageSelect();

    if (sqlNotFind()) {
      alertErr("查無資料, key=[" + acnoPSeqno + "], dataReadDbCARD");
      return;
    }
    wkdataDBCard();

    // busi.cmsm01.Cmsm2010Card func = new busi.cmsm01.Cmsm2010Card();
    // func.setConn(wp);
    // if(func.card_Online()==1){
    // alert_msg("此筆資料待覆核.....");
    // }
  }

  void wkdataDBCard() {
	  
    wp.colSet("wk_acctkey", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));
    
    if (wp.colEq("mail_type", "1")) {
      wp.colSet("tt_mail_type", ".普掛");
    } else if (wp.colEq("mail_type", "2")) {
      wp.colSet("tt_mail_type", ".限掛");
    } else if (wp.colEq("mail_type", "3")) {
      wp.colSet("tt_mail_type", ".自取");
    } else if (wp.colEq("mail_type", "4")) {
      wp.colSet("tt_mail_type", ".分行");
    } else if (wp.colEq("mail_type", "N")) {
      wp.colSet("tt_mail_type", ".退件");
    } else if (wp.colEq("mail_type", "Q")) {
      wp.colSet("tt_mail_type", ".其他");
    }
    String sql1 = " select opp_remark from cca_opp_type_reason where opp_status = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("oppost_reason")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_oppost_reason", "." + sqlStr("opp_remark"));
    }

    if (wp.colEq("change_status", "1")) {
      wp.colSet("tt_change_status", ".續卡待製卡");
    } else if (wp.colEq("change_status", "2")) {
      wp.colSet("tt_change_status", ".製卡中");
    } else if (wp.colEq("change_status", "3")) {
      wp.colSet("tt_change_status", ".製卡完成");
    } else if (wp.colEq("change_status", "4")) {
      wp.colSet("tt_change_status", ".製卡失敗");
    }

    if (wp.colEq("change_reason", "1")) {
      wp.colSet("tt_change_reason", ".系統續卡");
    } else if (wp.colEq("change_reason", "2")) {
      wp.colSet("tt_change_reason", ".提前續卡");
    } else if (wp.colEq("change_reason", "3")) {
      wp.colSet("tt_change_reason", ".人工續卡");
    }

    if (wp.colEq("expire_chg_flag", "1")) {
      wp.colSet("tt_expire_chg_flag", ".系統不續卡");
    } else if (wp.colEq("expire_chg_flag", "2")) {
      wp.colSet("tt_expire_chg_flag", ".預約不續卡");
    } else if (wp.colEq("expire_chg_flag", "3")) {
      wp.colSet("tt_expire_chg_flag", ".人工不續卡");
    }


    String sql2 = " select full_chi_name from gen_brn where branch = ? ";
    sqlSelect(sql2, new Object[] {wp.colStr("branch")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_branch", "." + sqlStr("full_chi_name"));
    } else {
      wp.colSet("tt_branch", "");
    }
    sqlSelect(sql2, new Object[] {wp.colStr("reg_bank_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_reg_bank_no", "." + sqlStr("full_chi_name"));
    } else {
      wp.colSet("tt_reg_bank_no", "");
    }

    // --major_id , major_id_no_code
    String sql3 = " select id_no , id_no_code from dbc_idno where id_p_seqno = ? ";
    sqlSelect(sql3, new Object[] {wp.colStr("id_p_seqno")});
    if (sqlRowNum > 0) {
      wp.colSet("major_id", sqlStr("id_no"));
      wp.colSet("major_id_no_code", sqlStr("id_no_code"));
    }

    // --spec_status
    String sql4 = " select spec_status from cca_card_base where card_no = ? ";
    sqlSelect(sql4, new Object[] {wp.colStr("card_no")});
    if (sqlRowNum > 0) {
      wp.colSet("spec_status", sqlStr("spec_status"));
    }

    // --qr_code , last_mod_date
    // String sql5 = " select "
    // + " decode(ec_flag,'Q',status_code,'') as qr_code , "
    // + " to_char(last_mod_time,'yyyymmdd') as last_mod_date "
    // + " from crd_ec_flag "
    // + " where card_no = ? ";
    // sqlSelect(sql5,new Object[]{wp.col_ss("card_no")});
    // if(sql_nrow>0){
    // wp.col_set("qr_code", sql_ss("qr_code"));
    // wp.col_set("last_mod_date", sql_ss("last_mod_date"));
    //
    // if(eq_igno(sql_ss("qr_code"),"A")){
    // wp.col_set("tt_qr_code", ".已登錄");
    // } else if(eq_igno(sql_ss("qr_code"),"U")){
    // wp.col_set("tt_qr_code", ".已登錄");
    // } else if(eq_igno(sql_ss("qr_code"),"D")){
    // wp.col_set("tt_qr_code", ".已註銷");
    // }
    // }

    // --third_rsn , third_rsn_ibm
    String sql6 = " select " + " third_rsn , " + " third_rsn_ibm " + " from dbc_emboss "
        + " where card_no = ? ";
    sqlSelect(sql6, new Object[] {wp.colStr("card_no")});
    if (sqlRowNum > 0) {
      wp.colSet("db_third_rsn", sqlStr("third_rsn"));
      wp.colSet("db_third_rsn_ibm", sqlStr("third_rsn_ibm"));
    }

    // --不續卡原因
    String sql7 = " select " + " wf_desc " + " from ptr_sys_idtab " + " where wf_type = ? "
        + " and wf_id = ? ";
    if (wp.colEq("expire_chg_flag", "1")) {
      sqlSelect(sql7, new Object[] {"NOTCHG_VD_S", wp.colStr("expire_reason")});
    } else if (wp.colEq("expire_chg_flag", "2")) {
      sqlSelect(sql7, new Object[] {"NOTCHG_VD_O", wp.colStr("expire_reason")});
    } else if (wp.colEq("expire_chg_flag", "3")) {
      sqlSelect(sql7, new Object[] {"NOTCHG_VD_M", wp.colStr("expire_reason")});
    }
    if (sqlRowNum > 0) {
      wp.colSet("tt_expire_reason", "." + sqlStr("wf_desc"));
    }

    // --db
    String sql8 = " select " + " decode(b.mail_zip  ,'',a.mail_zip  ,b.mail_zip) as db_mail_zip , "
        + " decode(b.mail_addr1,'',a.mail_addr1,b.mail_addr1) as db_mail_addr1 , "
        + " decode(b.mail_addr2,'',a.mail_addr2,b.mail_addr2) as db_mail_addr2 , "
        + " decode(b.mail_addr3,'',a.mail_addr3,b.mail_addr3) as db_mail_addr3 , "
        + " decode(b.mail_addr4,'',a.mail_addr4,b.mail_addr4) as db_mail_addr4 , "
        + " decode(b.mail_addr5,'',a.mail_addr5,b.mail_addr5) as db_mail_addr5 , "
        + " a.mail_type as ls_mail_type , " + " a.emboss_source , "
        + " nvl(a.digital_flag,'N') as digital_flag , " + " a.old_card_no "
        + " from dbc_card_addr b ,dbc_emboss a " + " where b.card_no = a.card_no "
        + " and a.emboss_source in ('3','4')" + " and a.card_no = ? ";
    sqlSelect(sql8, new Object[] {wp.colStr("card_no")});

    String sql9 = " select " + " return_date , " + " proc_status , " + " mail_no , "
        + " mail_type  , " + " mail_date , " + " mail_branch , " + " reason_code , "
        + " zip_code , " + " mail_addr1 , " + " mail_addr2 , " + " mail_addr3 , " + " mail_addr4 , "
        + " mail_addr5 " + " from dbc_return " + " where card_no = ? "
        + " and proc_status in ('3','6') " + " and mail_type in ('1','2','6','7','8') "
        + " and mod_time in ( select max(mod_time) as mod_time from dbc_return where card_no = ? ) ";
    sqlSelect(sql9, new Object[] {wp.colStr("card_no"), wp.colStr("card_no")});


    String sql10 = " select " + " return_date as return_date_2 , "
        + " proc_status as proc_status_2 , " + " mail_no as mail_no_2 , "
        + " mail_type as mail_type_2 , " + " mail_date as mail_date_2 , "
        + " mail_branch as mail_branch_2 , " + " reason_code as reason_code_2 "
        + " from dbc_return " + " where card_no = ? " + " and mod_time in ( "
        + " select max(mod_time) as mod_time from dbc_return where card_no = ? " + " ) ";
    sqlSelect(sql10, new Object[] {wp.colStr("card_no"), wp.colStr("card_no")});

    wp.colSet("db_return_date", sqlStr("return_date_2"));
    wp.colSet("db_proc_status", sqlStr("proc_status_2"));
    wp.colSet("db_mail_no", sqlStr("mail_no_2"));
    wp.colSet("db_return_mail_type", sqlStr("mail_type_2"));
    wp.colSet("db_mail_date", sqlStr("mail_date_2"));
    wp.colSet("db_mail_branch", sqlStr("mail_branch_2"));
    wp.colSet("db_reason_code", sqlStr("reason_code_2"));

    if (eqIgno(sqlStr("digital_flag"), "N")) {
      if ((eqIgno(sqlStr("emboss_source"), "3") || eqIgno(sqlStr("emboss_source"), "4"))
          && (!eqIgno(sqlStr("ls_mail_type"), "3") && !eqIgno(sqlStr("ls_mail_type"), "4")
              && !eqIgno(sqlStr("ls_mail_type"), "5"))) {
        wp.colSet("db_mail_zip", sqlStr("db_mail_zip"));
        wp.colSet("db_mail_addr1", sqlStr("db_mail_addr1"));
        wp.colSet("db_mail_addr2", sqlStr("db_mail_addr2"));
        wp.colSet("db_mail_addr3", sqlStr("db_mail_addr3"));
        wp.colSet("db_mail_addr4", sqlStr("db_mail_addr4"));
        wp.colSet("db_mail_addr5", sqlStr("db_mail_addr5"));
      }

      if (!eqIgno(sqlStr("mail_type"), "3") && !eqIgno(sqlStr("mail_type"), "4")
          && !eqIgno(sqlStr("mail_type"), "5")) {
        wp.colSet("db_retl_zip", sqlStr("zip_code"));
        wp.colSet("db_ret_addr1", sqlStr("mail_addr1"));
        wp.colSet("db_ret_addr2", sqlStr("mail_addr2"));
        wp.colSet("db_ret_addr3", sqlStr("mail_addr3"));
        wp.colSet("db_ret_addr4", sqlStr("mail_addr4"));
        wp.colSet("db_ret_addr5", sqlStr("mail_addr5"));
      }
    }

    if (eqIgno(sqlStr("digital_flag"), "Y") && (!eqIgno(sqlStr("ls_mail_type"), "3")
        && !eqIgno(sqlStr("ls_mail_type"), "4") && !eqIgno(sqlStr("ls_mail_type"), "5"))) {
      wp.colSet("db_mail_zip", sqlStr("db_mail_zip"));
      wp.colSet("db_mail_addr1", sqlStr("db_mail_addr1"));
      wp.colSet("db_mail_addr2", sqlStr("db_mail_addr2"));
      wp.colSet("db_mail_addr3", sqlStr("db_mail_addr3"));
      wp.colSet("db_mail_addr4", sqlStr("db_mail_addr4"));
      wp.colSet("db_mail_addr5", sqlStr("db_mail_addr5"));
      wp.colSet("db_retl_zip", sqlStr("zip_code"));
      wp.colSet("db_ret_addr1", sqlStr("mail_addr1"));
      wp.colSet("db_ret_addr2", sqlStr("mail_addr2"));
      wp.colSet("db_ret_addr3", sqlStr("mail_addr3"));
      wp.colSet("db_ret_addr4", sqlStr("mail_addr4"));
      wp.colSet("db_ret_addr5", sqlStr("mail_addr5"));
    }

  }

  void wkdataCard() {
    wp.colSet("wk_acct", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));
    if (wp.colEq("mail_type", "1")) {
      wp.colSet("tt_mail_type", ".普掛");
    } else if (wp.colEq("mail_type", "2")) {
      wp.colSet("tt_mail_type", ".限掛");
    } else if (wp.colEq("mail_type", "3")) {
      wp.colSet("tt_mail_type", ".自取");
    } else if (wp.colEq("mail_type", "4")) {
      wp.colSet("tt_mail_type", ".分行");
    } else if (wp.colEq("mail_type", "N")) {
      wp.colSet("tt_mail_type", ".退件");
    } else if (wp.colEq("mail_type", "Q")) {
      wp.colSet("tt_mail_type", ".其他");
    }
    String sql1 = " select opp_remark from cca_opp_type_reason where opp_status = ? ";
    sqlSelect(sql1, new Object[] {wp.colStr("oppost_reason")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_oppost_reason", "." + sqlStr("opp_remark"));
    }

    if (wp.colEq("reissue_reason", "1")) {
      wp.colSet("tt_reissue_reason", ".掛失");
    } else if (wp.colEq("reissue_reason", "2")) {
      wp.colSet("tt_reissue_reason", ".毀損");
    } else if (wp.colEq("reissue_reason", "3")) {
      wp.colSet("tt_reissue_reason", ".重製");
    }

    if (wp.colEq("reissue_status", "1")) {
      wp.colSet("tt_reissue_status", ".重製");
    } else if (wp.colEq("reissue_status", "2")) {
      wp.colSet("tt_reissue_status", ".重製中");
    } else if (wp.colEq("reissue_status", "3")) {
      wp.colSet("tt_reissue_status", ".重製完成");
    }

    if (wp.colEq("change_status", "1")) {
      wp.colSet("tt_change_status", ".續卡待製中");
    } else if (wp.colEq("change_status", "2")) {
      wp.colSet("tt_change_status", ".製卡中");
    } else if (wp.colEq("change_status", "3")) {
      wp.colSet("tt_change_status", ".製卡完成");
    } else if (wp.colEq("change_status", "4")) {
      wp.colSet("tt_change_status", ".製卡失敗");
    }

    if (wp.colEq("change_reason", "1")) {
      wp.colSet("tt_change_reason", ".系統續卡");
    } else if (wp.colEq("change_reason", "2")) {
      wp.colSet("tt_change_reason", ".提前續卡 - 客戶來電");
    } else if (wp.colEq("change_reason", "3")) {
      wp.colSet("tt_change_reason", ".提前續卡 - 本行調整");
    }

    String sql2 = " select id_no , id_no_code from crd_idno where id_p_seqno = ? ";
    sqlSelect(sql2, new Object[] {wp.colStr("major_id_p_seqno")});
    if (sqlRowNum > 0) {
      wp.colSet("major_id", sqlStr("id_no"));
      wp.colSet("major_id_code", sqlStr("id_no_code"));
    }

    String sql3 = " select full_chi_name from gen_brn where branch = ? ";
    sqlSelect(sql3, new Object[] {wp.colStr("mail_branch")});

    if (sqlRowNum > 0) {
      wp.colSet("tt_mail_branch", sqlStr("full_chi_name"));
    } else {
      wp.colSet("tt_mail_branch", "");
    }

    if (wp.colEq("expire_chg_flag", "1")) {
      wp.colSet("tt_expire_chg_flag", ".系統不續卡");
    } else if (wp.colEq("expire_chg_flag", "2")) {
      wp.colSet("tt_expire_chg_flag", ".提前不續卡 - 客戶來電");
    } else if (wp.colEq("expire_chg_flag", "3")) {
      wp.colSet("tt_expire_chg_flag", ".提前不續卡 - 本行調整");
    }

  }

  @Override
  public void saveFunc() throws Exception {
    if (eqIgno(wp.respHtml, "cmsm2010_idno")) {
      cmsm01.Cmsm2010Idno func = new cmsm01.Cmsm2010Idno();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    } else if (eqIgno(wp.respHtml, "cmsm2010_acno")) {
      cmsm01.Cmsm2010Acno func = new cmsm01.Cmsm2010Acno();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    } else if (eqIgno(wp.respHtml, "cmsm2010_card")) {
      cmsm01.Cmsm2010Card func = new cmsm01.Cmsm2010Card();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    } else if (eqIgno(wp.respHtml, "cmsm2010_dbcard")) {
      cmsm01.Cmsm2010Card func = new cmsm01.Cmsm2010Card();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    } else if (eqIgno(wp.respHtml, "cmsm2010_dbidno")) {
      cmsm01.Cmsm2010Dbidno func = new cmsm01.Cmsm2010Dbidno();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    } else if (eqIgno(wp.respHtml, "cmsm2010_dbacno")) {
      cmsm01.Cmsm2010Dbacno func = new cmsm01.Cmsm2010Dbacno();
      func.setConn(wp);
      rc = func.dbSave(strAction);
      if (rc != 1) {
        alertErr2(func.getMsg());
      }
    }

    this.sqlCommit(rc);
    if (eqIgno(wp.respHtml, "cmsm2010_idno")) {
      dataRead();
    } else if (eqIgno(wp.respHtml, "cmsm2010_dbidno")) {
      dataReadDbidno();
    } else if (eqIgno(wp.respHtml, "cmsm2010_acno")) {
      dataReadACNO();
    } else if (eqIgno(wp.respHtml, "cmsm2010_dbacno")) {
      dataReadDbAcno();
    } else if (eqIgno(wp.respHtml, "cmsm2010_card")) {
      dataReadCARD();
    } else if (eqIgno(wp.respHtml, "cmsm2010_dbcard")) {
      dataReadDbCARD();
    } else
      this.saveAfter(false);

  }

  @Override
  public void procFunc() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void initButton() {
    //btnModeAud("XX");
    this.btnModeAud();
    
    switch(wp.respHtml) {
    case "cmsm2010_idno":
    case "cmsm2010_dbidno":
    case "cmsm2010_acno":
    case "cmsm2010_card":
    case "cmsm2010_dbcard":
    	btnDeleteOn(true);
    	btnUpdateOn(true);
    	break;
    case "cmsm2010_dbacno":
    	btnDeleteOn(false);
    	btnUpdateOn(false);
    	break;
    }
  
  }

  @Override
  public void initPage() {
    resetBtn();
  }

  // **2017/11/21 Alex************************************************************
  public void dataReadDbidno() throws Exception {
    if (empty(acnoPSeqno)) {
      acnoPSeqno = wp.itemStr("id_p_seqno");
    }

    if (empty(acnoPSeqno)) {
      alertErr2("請從瀏覽頁面重新讀取資料");
      return;
    }

    wp.selectSQL = " *  ";
    wp.daoTable = "dbc_idno";
    wp.whereStr = "where 1=1  ";
    
    if (!empty(acnoPSeqno)) {
		wp.whereStr+= "and id_p_seqno = ? ";
		setString(acnoPSeqno);
	}
    

    pageSelect();
    if (sqlNotFind()) {
      alertErr("查無資料, key=[" + acnoPSeqno + "], dataReadDbidno");
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] residentZipArr = commString.splitZipCode(wp.colStr("resident_zip"));
    wp.colSet("resident_zip", residentZipArr[0]);
    wp.colSet("resident_zip2", residentZipArr[1]);
    String[] mailZipArr = commString.splitZipCode(wp.colStr("mail_zip"));
    wp.colSet("mail_zip", mailZipArr[0]);
    wp.colSet("mail_zip2", mailZipArr[1]);
    String[] companyZipArr = commString.splitZipCode(wp.colStr("company_zip"));
    wp.colSet("company_zip", companyZipArr[0]);
    wp.colSet("company_zip2", companyZipArr[1]);
    
    cmsm01.Cmsm2010Idno dbidno = new cmsm01.Cmsm2010Idno();
    String [] arr1=new String[] {};
    dbidno.setConn(wp);    
    arr1=dbidno.selectCmsChgColumnLog("dbc_idno","cellar_phone",wp.colStr("id_no"));
    wp.colSet("c_phone_chg_date", arr1[0]);
    wp.colSet("c_phone_chg_time", arr1[1]);
    selectDbcidnoExt();
    selectDbidnoOnline();
    dataReadDbidnoAfter();
  }

  void dataReadDbidnoAfter() {
	  CmsFunc cmsFunc = new CmsFunc(wp);
	  
	  if ( ! wp.colEmpty("education")) {
		  wp.colSet("tt_education", "." + cmsFunc.getEducationDesc(wp.colStr("education")));
	  }
	  if ( ! wp.colEmpty("business_code")) {
		  wp.colSet("tt_business_code", "." + cmsFunc.getBusinessDesc(wp.colStr("business_code")));
	  }
	  if ( ! wp.colEmpty("marriage")) {
		  wp.colSet("tt_marriage", "." + ecsfunc.DeCodeCms.marriageCode(wp.colStr("marriage")));
	  }

    if (wp.colEq("nation", "1")) {
      wp.colSet("tt_nation", "本國");
    } else if (wp.colEq("nation", "2")) {
      wp.colSet("tt_nation", "外國");
    }

  }

  void selectDbidnoOnline() {
    cmsm01.Cmsm2010Dbidno func = new cmsm01.Cmsm2010Dbidno();
    func.setConn(wp);
    if (func.dbIdnoOnline() == 1) {
    	alertMsg(String.format("此筆資料待覆核(%s).....", wp.colStr("userModDateTime")));
    }else {
    	func.cleanModCol();
    }

  }

  void selectDbcidnoExt() {
    wp.selectSQL = " *  ";
    wp.daoTable = "dbc_idno_ext";
    wp.whereStr = "where 1=1 " ;
    
    if (!empty(acnoPSeqno)) {
		wp.whereStr += " and id_p_seqno = ? ";
		setString(1, acnoPSeqno);
	}
    

    pageSelect();
    wp.notFound = "N";
  }

  // *****************************************************************************
  public void dataReadDbAcno() throws Exception {
    if (empty(acnoPSeqno)) {
      acnoPSeqno = wp.itemStr("p_seqno");
    }
    if (empty(idPSeqno)) {
      idPSeqno = wp.itemStr("id_p_seqno");
    }

    if (empty(acnoPSeqno) || empty(idPSeqno)) {
      alertErr2("請從瀏覽頁面重新讀取資料");
      return;
    }

    selectDbDetl();

    wp.selectSQL = " A.*  "
    // + " A.bill_sending_zip as bill_zip ,"
    // + " A.bill_sending_addr1 as bill_addr1 ,"
    // + " A.bill_sending_addr2 as bill_addr2 ,"
    // + " A.bill_sending_addr3 as bill_addr3 ,"
    // + " A.bill_sending_addr4 as bill_addr4 ,"
    // + " A.bill_sending_addr5 as bill_addr5 "
    ;
    wp.daoTable = "dba_acno A";
    wp.whereStr = "where 1=1 " ;

    if (!empty(acnoPSeqno)) {
		wp.whereStr += " and A.p_seqno = ? ";
		setString(1, acnoPSeqno);
	}

    pageSelect();
    
    String[] billSendingZipArr = commString.splitZipCode(wp.colStr("bill_sending_zip"));
    wp.colSet("bill_sending_zip", billSendingZipArr[0]);
    wp.colSet("bill_sending_zip2", billSendingZipArr[1]);
    
    ttdataDBacno();
    
//    if (sqlNotFind()) {
//      alertErr("查無資料, key=" + dataKK1);
//    }

    cmsm01.Cmsm2010Dbacno func = new cmsm01.Cmsm2010Dbacno();
    func.setConn(wp);
    if (func.dbAcnoOnline() == 1) {
    	alertMsg(String.format("此筆資料待覆核(%s).....", wp.colStr("userModDateTime")));
    }else {
    	func.cleanModCol();
    }
    
//    cmsm01.Cmsm2010Idno idno = new cmsm01.Cmsm2010Idno();
//    String [] arr=new String[] {};
//    idno.setConn(wp);    
//    arr=idno.selectCmsChgColumnLog("dba_acno","e_mail_ebill",wp.itemStr("ex_idno"));
//    wp.colSet("e_mail_ebill_date", arr[0]);
    
    setBillApplyFlagDes();
  }

  void selectDbDetl() {
    String sql1 = "select id_p_seqno ," + "id_no," + "id_no_code ," + "chi_name , " + "sex , "
        + "birthday ," + "resident_no , " + "other_cntry_code , " + "passport_no , " + "eng_name  "
        + " from dbc_idno " 
        + " where id_p_seqno =? ";
    sqlSelect(sql1, new Object[] {idPSeqno});

    wp.colSet("wk_idno", sqlStr("id_no") + "-" + sqlStr("id_no_code"));
    wp.colSet("birthday", sqlStr("birthday"));
    wp.colSet("sex", sqlStr("sex"));
    wp.colSet("chi_name", sqlStr("chi_name"));
    wp.colSet("resident_no", sqlStr("resident_no"));
    wp.colSet("other_cntry_code", sqlStr("other_cntry_code"));
    wp.colSet("passport_no", sqlStr("passport_no"));
    wp.colSet("id_p_seqno", sqlStr("id_p_seqno"));
    wp.colSet("idno.eng_name", sqlStr("eng_name"));  // change to select eng_name from dbc_idno instead of dbc_card
  }

  void selectCardNum() {
    String sql1 =
        "select count(*) as card_num ," + " sum(decode(sup_flag,'0',1,0)) as cardNum_major "
            + " from dbc_card " + " where id_p_seqno =? " + " and current_code='0' ";
    sqlSelect(sql1, new Object[] {idPSeqno});

    wp.colSet("valid_card_num", sqlStr("card_num"));
    wp.colSet("valid_card_num2", sqlStr("cardNum_major"));
  }

  // *****************************************************************************
  void ttdataDBacno() {
    wp.colSet("wk_acctkey", wp.colStr("acct_type") + "-" + wp.colStr("acct_key"));

    StringBuffer sb = new StringBuffer();
    sb.append(" select  block_status ,  block_reason1 ,  block_reason2 , ")
      .append(" block_reason3 ,  block_reason4 ,  block_reason5 ")
      .append(" from cca_card_acct ")
      .append(" where acno_p_seqno = ? ")
      .append(" and debit_flag <>'Y' ")
      ;
    
    String sql3 = sb.toString();

    sqlSelect(sql3, new Object[] {wp.colStr("acno_p_seqno")});

    if (sqlRowNum > 0) {
      wp.colSet("block_status", sqlStr("block_status"));
      wp.colSet("block_reason", sqlStr("block_reason1"));
      wp.colSet("block_reason2", sqlStr("block_reason2"));
      wp.colSet("block_reason3", sqlStr("block_reason3"));
      wp.colSet("block_reason4", sqlStr("block_reason4"));
      wp.colSet("block_reason5", sqlStr("block_reason5"));
    }
    
    if ( ! wp.colEmpty("acct_status")) {
		wp.colSet("tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("acct_status")));
    }

    if (wp.colEq("rc_use_indicator", "1")) {
      wp.colSet("tt_rc_use_indicator", ".正常允用");
    } else if (wp.colEq("rc_use_indicator", "2")) {
      wp.colSet("tt_rc_use_indicator", ".例外允用");
    } else if (wp.colEq("rc_use_indicator", "3")) {
      wp.colSet("tt_rc_use_indicator", ".不準允用 RC");
    }

    if (wp.colEq("rc_use_b_adj", "1")) {
      wp.colSet("tt_rc_use_b_adj", ".正常允用");
    } else if (wp.colEq("rc_use_b_adj", "2")) {
      wp.colSet("tt_rc_use_b_adj", ".例外允用");
    } else if (wp.colEq("rc_use_b_adj", "3")) {
      wp.colSet("tt_rc_use_b_adj", ".不準允用 RC");
    }

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

    // --帳戶屬性
    if (wp.colEq("corp_act_flag", "Y")) {
      wp.colSet("tt_corp_act_flag", ".總繳");
    } else if (wp.colEq("corp_act_flag", "N")) {
      wp.colSet("tt_corp_act_flag", ".個繳");
    } else if (wp.colEq("corp_act_flag", "個繳")) {
      wp.colSet("tt_corp_act_flag", ".個繳");
    }
    // --往來狀態
    if ( ! wp.colEmpty("acct_status")) {
		wp.colSet("tt_acct_status", "." + DeCodeAct.acctStatus(wp.colStr("acct_status")));
    }
    // --凍結
    if (wp.colEq("block_status", "Y")) {
      wp.colSet("tt_block_status", ".凍結");
    } else if (wp.colEq("block_status", "N")) {
      wp.colSet("tt_block_status", "");
    }
    // --自動繳指示碼
    if (wp.colEq("autopay_indicator", "1")) {
      wp.colSet("tt_autopay_indicator", ".扣 TTL");
    } else if (wp.colEq("autopay_indicator", "2")) {
      wp.colSet("tt_autopay_indicator", ".扣 MP");
    }

    String sql1 =
        " select sum(decode(sup_flag,'0',1,0)) as valid_card_num," + " count(*) as valid_card_num2 "
            + " from dbc_card" 
        	+ " where p_seqno = ? " 
            + " and current_code = '0' ";
    sqlSelect(sql1, new Object[] {wp.colStr("p_seqno")});
    if (sqlRowNum > 0) {
      wp.colSet("valid_card_num", sqlStr("valid_card_num"));
      wp.colSet("valid_card_num2", sqlStr("valid_card_num2"));
    }

    // --風險行
    String sql2 = "select full_chi_name " 
                          + " from gen_brn" 
    		              + " where branch =? ";
    sqlSelect(sql2, new Object[] {wp.colStr("risk_bank_no")});
    if (sqlRowNum > 0) {
      wp.colSet("tt_risk_bank_no", "." + sqlStr("full_chi_name"));
    } else {
      wp.colSet("tt_risk_bank_no", "");
    }    

  }

  void resetBtn() {
    wp.colSet("bt_class1", "btQuery");
    wp.colSet("bt_class2", "btQuery");
    wp.colSet("bt_class3", "btQuery");
    wp.colSet("bt_class4", "btQuery");
    wp.colSet("bt_class5", "btQuery");
    wp.colSet("bt_class6", "btQuery");
    wp.colSet("bt_class7", "btQuery");
    wp.colSet("bt_class8", "btQuery");
    wp.colSet("bt_class9", "btQuery");
    wp.colSet("bt_class10", "btQuery");
    wp.colSet("bt_class11", "btQuery");
    wp.colSet("bt_class12", "btQuery");
  }

  public void wfAjaxKey(TarokoCommon wr) throws Exception {
    super.wp = wr;

    // String ls_winid =
    selectAddrByZipCode(wp.itemStr("ax_zip_code"));
    wp.addJSON("changeTarget", wp.itemStr("changeTarget"));
    if (rc != 1) {
      wp.addJSON("ajax_addr1", "");
      wp.addJSON("ajax_addr2", "");
      return;
    }
    wp.addJSON("ajax_addr1", sqlStr("ajax_addr1"));
    wp.addJSON("ajax_addr2", sqlStr("ajax_addr2"));

  }

  void selectAddrByZipCode(String zipCode) {
    String sql1 = " select " 
        + " zip_city as ajax_addr1 , " 
        + " zip_town as ajax_addr2 "
        + " from ptr_zipcode " 
        + " where zip_code = ? ";

    sqlSelect(sql1, new Object[] {zipCode});

    if (sqlRowNum <= 0) {
      alertErr2("郵遞區號不存在:" + zipCode);
      return;
    }

  }

	private void setServiceYear(String serviceYear) {
		if (serviceYear.length() != 4) {
			serviceYear = commString.lpad(serviceYear,4,"0");
		}
		wp.colSet("service_year_YY", serviceYear.substring(0,2));
		wp.colSet("service_year_MM", serviceYear.substring(2,4));
	}

}
