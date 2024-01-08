/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-27  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-23  V1.00.01   Justin         parameterize sql
******************************************************************************/
package rskm02;
/** 線上持卡人信用額度調整
 * 2019-1223:  Alex  emend_type = 2 , can't use sms
 * 2019-1213:  Alex  add checkBlockReason
 * 2019-0814   JH    xx:萬元簡訊
 * 2019-0618:  JH    p_xxx >>acno_p_xxx
 * 2018-0129:	JH		利害關係人confirm
 * 2018-0126:	JH		預借現金查核
 * 
 * */

public class Rskm0920Func extends busi.FuncAction {
 // String kk1 = "", kk2 = "", kk3 = "", kk4 = ""; 
  String  isCardNo = "", cardAcct = "";

  public String confMsg = "";

  void selectCrdCard() {
    if (wp.itemEq("emend_type", "4") == false) {
      return;
    }
    if (wp.itemEmpty("card_no")) {
      errmsg("卡號: 不可空白");
    }
    strSql = "SELECT son_card_flag," + " current_code, "
        + " acno_p_seqno, id_p_seqno, corp_p_seqno, acct_type, " + " indiv_crd_lmt "
        + " from crd_card " + " where card_no =?";
    sqlSelect(strSql, new Object[] {wp.itemStr("card_no")});
    if (sqlRowNum <= 0) {
      errmsg("卡號: 不存在卡檔");
      return;
    }
    if (colEq("current_code", "0") == false) {
      errmsg("此卡號不是有效卡");
      return;
    }
    if (colEq("son_card_flag", "Y") == false) {
      errmsg("此卡號非子卡卡號");
      return;
    }

    this.col2wpItem("acct_type");
    this.col2wpItem("acno_p_seqno");
    this.col2wpItem("id_p_seqno");
    this.col2wpItem("corp_p_seqno");
    wp.itemSet("db_bef_card", colStr("indiv_crd_lmt"));
  }

  void selectActAcno() {
    strSql = "select " + "  acct_type " + " ,acct_key " + ", line_of_credit_amt"
        + ", acno_p_seqno, id_p_seqno, corp_p_seqno"
        + ", no_adj_loc_high, no_adj_loc_high_s_date, no_adj_loc_high_e_date"
        + ", no_adj_loc_low, no_adj_loc_low_s_date, no_adj_loc_low_e_date"
        + ", no_adj_h_cash, no_adj_h_e_date_cash, no_adj_h_s_date_cash" + " from act_acno"
        + " where 1=1" ;
    
    if(wp.itemEq("emend_type", "3")) {
    	if (wp.itemEmpty("card_no")) {
    		strSql += " and acno_p_seqno in (select acno_p_seqno from crd_card ";
		}else {
			strSql += " and acno_p_seqno in (select acno_p_seqno from crd_card where 1=1 and card_no = ? )";
	    	setString(wp.itemStr("card_no"));
		}
    }	else	{
    	if (! wp.itemEmpty("acct_key")) {
    		strSql += " and acct_key = ? ";
    		setString(wp.itemStr("acct_key"));
		}
    	
		if (!wp.itemEmpty("acct_type")) {
			strSql += " and acct_type = ? ";
    		setString(wp.itemStr("acct_type"));
		}

    }
    
    sqlSelect(strSql);
    if (sqlRowNum == 0) {
      errmsg("查無帳戶資料: act_acno; kk=" + cardAcct);
      rc = -1;
    }
    this.col2wpItem("acct_key");
    if (wp.itemEq("emend_type", "4") == false) {
      this.col2wpItem("acct_type");
      this.col2wpItem("acno_p_seqno");
      this.col2wpItem("id_p_seqno");
      this.col2wpItem("corp_p_seqno");
    }

    strSql = "select block_reason1, block_reason2" + ", block_reason3, block_reason4, block_reason5"
        + " from cca_card_acct" + " where debit_flag<>'Y'";

    if (! empty(colStr("acno_p_seqno"))) {
		strSql += " and acno_p_seqno = ? ";
		setString(colStr("acno_p_seqno"));
	}

    sqlSelect(strSql);
    if (sqlRowNum == 0) {
      errmsg("查無帳戶資料: cca_card_acct; kk=" + cardAcct);
      rc = -1;
    }
    // --
    if (this.colEmpty("no_adj_loc_high_e_date")) {
      colSet("no_adj_loc_high_e_date", "20991231");
    }
    if (this.colEmpty("no_adj_loc_low_e_date")) {
      colSet("no_adj_loc_low_e_date", "20991231");
    }
    if (this.colEmpty("no_adj_h_e_date_cash")) {
      colSet("no_adj_h_e_date_cash", "20991231");
    }
    return;
  }

  @Override
  public void dataCheck() {
    if (wp.itemEmpty("card_no") && wp.itemEmpty("acct_key")) {
      errmsg("帳戶帳號及卡號: 不可同時空白");
      return;
    }

    // -select OLD.data-
    if (wp.itemEq("emend_type", "3")) {
      // select_crd_card();
      // if (rc != 1) {
      // return;
      // }
      isCardNo = wp.itemStr2("card_no");
    } else {
      isCardNo = "";
    }
    selectActAcno();

    if (rc != 1) {
      return;
    }

    if (ibAdd || ibUpdate) {

      // if(wp.item_eq("sms_flag", "1") || wp.item_eq("sms_flag", "5")){
      // if(wp.item_num("db_aft_amt")%10000!=0){
      // errmsg("額度仟元以下不發送簡訊，請人工發送");
      // return ;
      // }
      // }

      if (wp.itemEmpty("rowid") == false) {
        strSql = "select count(*) as db_cnt" 
            + " from rsk_acnolog where 1=1"
            + sqlRowId(wp.itemStr("rowid")) 
            + " and apr_flag<>'Y'";
        this.sqlSelect(strSql);
        if (colNum("db_cnt") <= 0) {
          errmsg("資料已覆核, 或不存在, 不可修改/刪除");
          return;
        }
      }
    }
    if (ibDelete) {
      return;
    }
    // --
    this.dateTime();
    double lmAdjAmt = wp.itemNum("db_aft_amt") - wp.itemNum("wk_bef_amt");
    wp.itemSet("log_reason", wp.itemStr("log_reason_up"));
    if (lmAdjAmt > 0) {
      wp.itemSet("adj_loc_flag", "1");
      if (wp.itemEmpty("log_reason_up")) {
        errmsg("調高原因: 不可空白");
        return;
      }
    } else if (lmAdjAmt < 0) {
      wp.itemSet("adj_loc_flag", "2");
      if (wp.itemEmpty("log_reason_down")) {
        errmsg("調低原因: 不可空白");
        return;
      }
      wp.itemSet("log_reason", wp.itemStr("log_reason_down"));
    } else {
      errmsg("調整前額度與調整後額度一樣");
      return;
    }

    // --1.個人額度 2.商務卡公司額度 3.商務卡個人額度 4.子卡額度 5.預借現金額度
    // -!=5.預借現金額度, 調整金額---
    if (!wp.itemEq("emend_type", "5") && wp.itemNum("db_aft_amt") <= 0) {
      errmsg("額度調整後金額: 不可為 0");
      return;
    }
    if (wp.itemEq("emend_type", "4")) {
      if (wp.itemNum("db_aft_amt") > wp.itemNum("db_bef_amt")) {
        errmsg("子卡額度: 超過帳戶額度");
        return;
      }
    } else if (wp.itemEq("emend_type", "5")) {
      if (wfChkCreditCash() != 1) {
        errmsg(getMsg());
        // errmsg("預借現金額度: 超過帳戶額度");
        rc = -1;
        return;
      }
    } else if (pos("|2|3", wp.itemStr("emend_type")) > 0) {
      if (wfCorpLimit() == -1) {
        rc = -1;
        return;
      }
    }
    // --不可調高調低(預借現金不管)--
    // -不可調高-
    
    if (commString.strIn(wp.itemStr("emend_type"), "1,2,3") && lmAdjAmt > 0 && colEq("no_adj_loc_high", "Y")) {
      if (sysDate.compareTo(colStr("no_adj_loc_high_s_date")) >= 0
          && sysDate.compareTo(colStr("no_adj_loc_high_e_date")) <= 0) {
        errmsg("此帳戶額度不可調高");
        return;
      }
    }
    // --不可調低--NVL(no_adj_loc_low,'N') ,no_adj_loc_low_s_date,
    // nvl(no_adj_loc_low_e_date,'99991231')
    if (commString.strIn(wp.itemStr("emend_type"), "1,2,3") && lmAdjAmt < 0 && colEq("no_adj_loc_low", "Y")) {
      if (sysDate.compareTo(colStr("no_adj_loc_low_s_date")) >= 0
          && sysDate.compareTo(colStr("no_adj_loc_low_e_date")) <= 0) {
        errmsg("此帳戶額度不可調低");
        return;
      }
    }
    // -預借現金不可調高-
    if (wp.itemEq("emend_type", "5") && lmAdjAmt > 0 && colEq("no_adj_h_cash", "Y")) {
      if (sysDate.compareTo(colStr("no_adj_h_s_date_cash")) >= 0
          && sysDate.compareTo(colStr("no_adj_h_e_date_cash")) <= 0) {
        errmsg("此帳戶預借現金額度不可調高");
        return;
      }
    }

//    if (wp.itemEq("emend_type", "1") && lmAdjAmt > 0) {
//      saveConfirmFh();
//    }

    if (lmAdjAmt > 0) {
      // --只要有任一凍結碼就不可調高
      if (checkBlockReason() == false) {        
        return;
      }
    }

    // --商務卡公司額度 不發送簡訊
    if (wp.itemEq("emend_type", "2") && wp.itemEmpty("sms_flag") == false) {
      errmsg("調整商務卡公司額度 , 不可發送簡訊");
      return;
    }

  }

  boolean checkBlockReason() {
    if (wp.itemEmpty("acct_key") == false) {
      String sql1 =
          " select acct_type , acno_p_seqno from act_acno where acct_key = ? and acct_type = ? ";
      sqlSelect(sql1, new Object[] {wp.itemStr("acct_key"), wp.itemStr("acct_type")});
    } else if (wp.itemEmpty("card_no") == false) {
      String sql2 = " select acct_type , acno_p_seqno from crd_card where card_no = ? ";
      sqlSelect(sql2, new Object[] {wp.itemStr("card_no")});
    }

    if (sqlRowNum <= 0) {
      errmsg("select act_acno error");
      return false;
    }

    String lsAcctType = "", lsAcnoPSeqno = "";
    lsAcctType = colStr("acct_type");
    lsAcnoPSeqno = colStr("acno_p_seqno");

    String sql3 =
        " select block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as wk_block_reason "
            + " from cca_card_acct where acct_type = ? and acno_p_seqno = ? ";

    sqlSelect(sql3, new Object[] {lsAcctType, lsAcnoPSeqno});
    if (sqlRowNum <= 0) {
      errmsg("select cca_card_acct error ");
      return false;
    }

    if (colEmpty("wk_block_reason") == false) {
    	errmsg("此帳戶已凍結, 額度不可調高");
    	return false;
    }
      


    return true;
  }

  public int saveConfirmFh() {
    // wf_chk_correlate() {
    // --check 金控關係人
    // 此功能於執行'個人額度調整'(emend_type='1')時,且調整額度>0時,才需Check
    // ldc_adj_amt=>本次 調整金額
    // ldc_line_of_credit_amt=>卡戶 未調整前總額度
    // ls_non_asset_bal=>IBM主機送來之 無擔保授信餘額
    // ldc_asset_value=>擔保品放款值 crd_idno
    // ldc_val6=>總無擔保授信額度 ofw_sysparm
    // ldc_val7=>最高分期付款額度 ofw_sysparm
    // ===========================================================================
    if (wp.itemEq("emend_type", "1") == false)
      return 1;

    wp.itemSet("fh_flag", "");
    wp.colSet("fh_flag", "");
    strSql = "select fh_flag, non_asset_balance" + " from crd_correlate" + " where correlate_id ="
        + commSqlStr.sqlID + "uf_idno_id(?)";
    sqlSelect(strSql, new Object[] {wp.itemStr("id_p_seqno")});
    if (sqlRowNum <= 0) {
      return 1;
    }
    wp.itemSet("fh_flag", "Y");
    wp.colSet("fh_flag", "Y");
    double lmNonAsset = colNum("non_asset_balance");

    // -調高才confirm-
    double lmAdjAmt = wp.itemNum("db_aft_amt") - wp.itemNum("wk_bef_amt");
    if (lmAdjAmt <= 0)
      return 1;

    strSql = "select sum(line_of_credit_amt) as db_amt" + " from act_acno" + " where id_p_seqno =?";
    sqlSelect(strSql, new Object[] {wp.itemStr("id_p_seqno")});
    double lmLineCreditAmt = 0;
    if (sqlRowNum > 0) {
      lmLineCreditAmt = colNum("db_amt");
    }

    strSql = "select asset_value from crd_idno" + " where id_p_seqno =?";
    sqlSelect(strSql, new Object[] {wp.itemStr("id_p_seqno")});
    double lmAssetVal = 0;
    if (sqlRowNum > 0) {
      lmAssetVal = colNum("asset_value");
    }

    strSql = "select wf_value6, wf_value7 from ptr_sys_parm"
        + " where wf_parm ='SYSPARM' and wf_key ='CORRELATE'";
    sqlSelect(strSql);
    double lmVal6 = 1000000, lmVal7 = 100000;
    if (sqlRowNum > 0) {
      lmVal6 = colNum("wf_value6");
      lmVal7 = colNum("wf_value7");
    }
    // double lm_adj = wp.item_num("db_aft_amt") - wp.item_num("wk_beg_amt");
    double lmTotAmt = lmAdjAmt + lmLineCreditAmt + lmVal7 + lmNonAsset - lmAssetVal;
    if (lmTotAmt > lmVal6) {
      errmsg("總無擔保授信額度 >" + lmVal6 + ", 是否存檔");
      return 2;
    }

    return 1;
  }

  int wfCorpLimit() {
    // emend_type:2.公司,3.公司個人
    // --公司總額調整----------------------------
    if (wp.itemEq("emend_type", "2")) {
      strSql = "select max(line_of_credit_amt) as max_limit" + " from act_acno"
          + " where corp_p_seqno =?" + " and acct_type =?" + " and acno_flag in ('3','Y')" // -個繳總繳個人-
      ;
      setString2(1, colStr("corp_p_seqno"));
      setString(colStr("acct_type"));
      sqlSelect(strSql);
      if (sqlRowNum > 0) {
        if (wp.itemNum("db_aft_amt") < colNum("max_limit")) {
          errmsg("商務卡公司調整後額度小於個人額度");
          return -1;
        }
      }
      return 1;
    }
    // --商務卡個人額度-----------------------
    if (wp.itemEq("emend_type", "3")) {
      strSql = "select line_of_credit_amt as corp_limit" + " from act_acno"
          + " where corp_p_seqno =?" + " and acct_type =?" + " and acno_flag ='2'"; // 公司戶額度
      setString2(1, colStr("corp_p_seqno"));
      setString(colStr("acct_type"));
      sqlSelect(strSql);
      if (sqlRowNum <= 0) {
        errmsg("查無公司戶額度, kk[%s]", colStr("corp_p_seqno"));
        return -1;
      }
      if (wp.itemNum("db_aft_amt") > colNum("corp_limit")) {
        errmsg("商務卡個人調整後額度超過公司額度");
        return -1;
      }
    }

    return 1;
  }

  int wfChkCreditCash() {
    // 1.預借現金額度調整，不可大於一般額度比率。,2.不可大於帳戶類別之最大預借現金額度。

    String sql1 =
        "select decode(nvl(b.new_acct_flag,'N'), 'Y',a.cashadv_loc_rate,a.cashadv_loc_rate_old ) as ldc_rate ,"
            + "a.cashadv_loc_maxamt as ldc_maxamt " + " from ptr_acct_type a, act_acno b "
            + " where a.acct_type = b.acct_type " + " and b.acct_key =? " + " and b.acct_type =? ";
    sqlSelect(sql1, new Object[] {wp.itemStr("acct_key"), wp.itemStr("acct_type")});

    if (sqlRowNum <= 0) {
      errmsg("select ptr_acct_type+act_acno error");
      return -1;
    }

    if (colNum("ldc_rate") > 0) {
      double ldcRateamt = 0.0;
      ldcRateamt = (double) (wp.itemNum("db_bef_amt") * colNum("ldc_rate") / 100);
      ldcRateamt = (int) Math.round(ldcRateamt);
      if (wp.itemNum("db_aft_amt") > ldcRateamt) {
        errmsg("預借現金額度調整, 不可大於一般額度比率");
        return -1;
      }
    } else {
      if (wp.itemNum("db_bef_amt") > 0) {
        errmsg("此帳戶類別 不可有預借現金");
        return -1;
      }
    }

    if (colNum("ldc_maxamt") > 0) {
      if (wp.itemNum("db_aft_amt") > colNum("ldc_maxamt")) {
        errmsg("預借現金額度調整, 不可大於帳戶類別之最大預借現金額度 : " + colNum("ldc_maxamt"));
        return -1;
      }
    }
    return 1;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    String lsKindFlag = "A";
    if (wp.itemEq("emend_type", "4"))
      lsKindFlag = "C";

    // -delete un-approve-data-
    strSql = "delete rsk_acnolog where 1=1" + " and kind_flag =?" + " and card_no =?"
        + " and acno_p_seqno =?" + " and log_mode ='1'" // -人工-
        + " and log_type ='1'" // -1.調額-
        + " and emend_type =?" + " and apr_flag <>'Y'";
    setString2(1, lsKindFlag);
    setString(wp.itemStr("card_no"));
    setString(wp.itemStr("acno_p_seqno"));
    setString(wp.itemStr("emend_type"));
    sqlExec(strSql);
    if (sqlRowNum <= 0)
      rc = 1;

    if (wp.itemEq("emend_type", "5")) {
      colSet("aft_loc_amt", wp.itemStr("db_bef_amt"));
      colSet("aft_loc_cash", wp.itemStr("db_aft_amt"));
      colSet("bef_loc_amt", wp.itemStr("db_bef_amt"));
      colSet("bef_loc_cash", wp.itemStr("wk_bef_amt"));
    } else {
      colSet("aft_loc_amt", wp.itemStr("db_aft_amt"));
      colSet("aft_loc_cash", wp.itemStr("db_bef_cash"));
      colSet("bef_loc_amt", wp.itemStr("wk_bef_amt"));
      colSet("bef_loc_cash", wp.itemStr("db_bef_cash"));
    }
    // -1.高,2.低-
    colSet("adj_loc_flag", "1");
    if (wp.itemNum("db_aft_amt") < wp.itemNum("wk_bef_amt")) {
      colSet("adj_loc_flag", "2");
    }

    strSql = "insert into rsk_acnolog (" + " kind_flag ," + " card_no ," + " acno_p_seqno ,"
        + " acct_type ," + " id_p_seqno ," + " corp_p_seqno ," + " param_no ," + " log_date ,"
        + " log_mode ," + " log_type ," + " log_reason ," + " bef_loc_amt ," + " aft_loc_amt ,"
        + " adj_loc_flag ," + " print_comp_yn ," + " mail_comp_yn ," + " emend_type ,"
        + " fh_flag ," + " bef_loc_cash ," + " aft_loc_cash ," + " send_ibm_flag ,"
        + " send_ibm_date ," + " sms_flag ," + " block_reason ," + " block_reason2 ,"
        + " block_reason3 ," + " block_reason4 ," + " block_reason5 ," + " apr_flag ,"
        + " mod_user ," + " mod_time ," + " mod_pgm ," + " mod_seqno " + " ) values ("
        + " :kind_flag ," + " :card_no ," + " :acno_p_seqno ," + " :acct_type ," + " :id_p_seqno ,"
        + " :corp_p_seqno ," + " '1' ," + " to_char(sysdate,'yyyymmdd') ," + " '1' ," + " '1' ,"
        + " :log_reason ," + " :bef_loc_amt ," + " :aft_loc_amt ," + " :adj_loc_flag ," + " 'N' ,"
        + " 'N' ," + " :emend_type ," + " :fh_flag ," + " :bef_loc_cash ," + " :aft_loc_cash ,"
        + " 'N' ," + " '' ," + " :sms_flag ," + " :block_reason ," + " :block_reason2 , "
        + " :block_reason3 ," + " :block_reason4 ," + " :block_reason5 ," + " 'N' ,"
        + " :mod_user ," + " sysdate ," + " 'rskm0920' ," + " '1' " + " )";
    wp.log("B:"+wp.itemStr("acno_p_seqno"));
    setString2("kind_flag", lsKindFlag);
    setString("card_no", isCardNo);
    item2ParmStr("acno_p_seqno");
    item2ParmStr("acct_type");
    item2ParmStr("id_p_seqno");
    item2ParmStr("corp_p_seqno");
    if (eqIgno(colStr("adj_loc_flag"), "1")) {
      setString2("log_reason", wp.itemStr("log_reason_up"));
    } else {
      setString2("log_reason", wp.itemStr("log_reason_down"));
    }
    setDouble2("bef_loc_amt", colNum("bef_loc_amt"));
    setDouble2("aft_loc_amt", colNum("aft_loc_amt"));
    setDouble2("bef_loc_cash", colNum("bef_loc_cash"));
    setDouble2("aft_loc_cash", colNum("aft_loc_cash"));
    setString2("adj_loc_flag", colStr("adj_loc_flag"));
    item2ParmStr("emend_type");
    setString2("fh_flag", wp.itemStr("fh_flag"));
    item2ParmNvl("sms_flag", "N");
    setString2("block_reason", colStr("block_reason1"));
    setString2("block_reason2", colStr("block_reason2"));
    setString2("block_reason3", colStr("block_reason3"));
    setString2("block_reason4", colStr("block_reason4"));
    setString2("block_reason5", colStr("block_reason5"));
    setString2("mod_user", modUser);

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert rsk_acnolog error; " + this.getMsg());
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    String lsKindFlag = "A";
    if (wp.itemEq("emend_type", "4"))
      lsKindFlag = "C";
    if (wp.itemEq("emend_type", "5")) {
      colSet("aft_loc_amt", wp.itemStr("db_bef_amt"));
      colSet("aft_loc_cash", wp.itemStr("db_aft_amt"));
      colSet("bef_loc_amt", wp.itemStr("db_bef_amt"));
      colSet("bef_loc_cash", wp.itemStr("wk_bef_amt"));

    } else {
      colSet("aft_loc_amt", wp.itemStr("db_aft_amt"));
      colSet("aft_loc_cash", wp.itemStr("db_bef_cash"));
      colSet("bef_loc_amt", wp.itemStr("wk_bef_amt"));
      colSet("bef_loc_cash", wp.itemStr("db_bef_cash"));
    }
    // -1.高,2.低-
    colSet("adj_loc_flag", "1");
    if (wp.itemNum("db_aft_amt") < wp.itemNum("wk_bef_amt")) {
      colSet("adj_loc_flag", "2");
    }

    strSql = " update rsk_acnolog set " + " log_reason =:log_reason ,"
        + " log_date = to_char(sysdate,'yyyymmdd') , " + " bef_loc_amt =:bef_loc_amt ,"
        + " aft_loc_amt =:aft_loc_amt ," + " adj_loc_flag =:adj_loc_flag ,"
        + " print_comp_yn ='N' ," + " mail_comp_yn ='N' ," + " aft_loc_cash =:aft_loc_cash ,"
        + " sms_flag =:sms_flag ," + " apr_flag ='N' ," + " apr_user ='' ," + " apr_date ='' ,"
        + commSqlStr.setModxxx(modUser, modPgm) + " where 1=1 and rowid = :rowid ";
    if (eqIgno(colStr("adj_loc_flag"), "1")) {
      setString2("log_reason", wp.itemStr("log_reason_up"));
    } else {
      setString2("log_reason", wp.itemStr("log_reason_down"));
    }
    setDouble2("bef_loc_amt", colNum("bef_loc_amt"));
    setDouble2("aft_loc_amt", colNum("aft_loc_amt"));
    setDouble2("bef_loc_cash", colNum("bef_loc_cash"));
    setDouble2("aft_loc_cash", colNum("aft_loc_cash"));
    setString2("adj_loc_flag", colStr("adj_loc_flag"));
    setString2("sms_flag", wp.itemNvl("sms_flag", "N"));
    setRowId("rowid", wp.itemStr("rowid"));
    // this.setRowId("rowid",wp.item_ss("rowid"));
    // setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
      return rc;
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    strSql = "delete rsk_acnolog where 1=1 "
    		+ " and hex(rowid) = ? ";
    setString(wp.itemStr("rowid"));
    
    if (wp.itemStr("mod_seqno").length() == 0)
    	strSql += " and nvl(mod_seqno,0)=0";
    else{
    	strSql += " and nvl(mod_seqno,0) =nvl(?,0)";
    	setString(wp.itemStr("mod_seqno"));
    }

    rc = this.sqlExec(strSql);
    if (sqlRowNum == 0) {
      errmsg("Delete rsk_acnolog error; " + this.getMsg());
    }

    return rc;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

}
