package cmsm02;
/**
 * 2020-0727   JustinWu  getCaseSeqno(): public -> package
 * 2020-0721   JustinWu  remove the code in dbDelete
 * 2020-0720   JustinWu  remove useless code
 * 2019-1122:  Alex   send_code = Y , case_result = 9
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * 109-04-27   shiyuqi       updated for project coding standard     
 * 111-11-08  V1.00.01  Machao     頁面bug調整* 
 * 111-11-25  V1.00.02  sunny      調整取得序號getCaseSeqno的sql語法
 * */
import busi.FuncAction;


public class Cmsm6020Func extends FuncAction {
  //String kk1 = "", kk2 = "", kk3 = "", lsChgAcctType = "";

  String lsMarketAgreeAct = "";

  @Override
  public void dataCheck() {
    if (wp.itemEmpty("case_idno")) {
      errmsg("正卡ID: 不可空白");
      return;
    }

    if (wp.itemEq("case_result", "9") && wp.itemEq("send_code", "Y")) {
      errmsg("案件已完成, 不可再移送");
      return;
    }

    if (wp.itemEq("tab_click", "2")) {
      wp.itemSet("case_type", "99999");
      String lsDesc = "", lsDesc2 = "";
      lsDesc = wp.itemStr2("mail_desc");
      lsDesc2 = wp.itemStr2("mail_desc2");
      if (empty(lsDesc))
        lsDesc = "補寄資料";
      wp.itemSet("case_desc", lsDesc);
      wp.itemSet("case_desc2", lsDesc2);

      String lsAddr = "";
      lsAddr = wp.itemStr2("bill_zip") + wp.itemStr2("bill_addr1") + wp.itemStr2("bill_addr2")
          + wp.itemStr2("bill_addr3") + wp.itemStr2("bill_addr4") + wp.itemStr2("bill_addr5");
      if (empty(lsAddr)) {
        errmsg("郵寄地址不可空白");
        return;
      }

      if (wp.itemEmpty("bill_addr5")) {
        errmsg("地址輸入不完整");
        return;
      }

      String lsType = "";
      lsType = wp.itemStr2("db_case_id_A") + wp.itemStr2("db_case_id_B")
          + wp.itemStr2("db_case_id_C") + wp.itemStr2("db_case_id_D");
      if (empty(lsType)) {
        errmsg("請指定補寄送之代碼資料");
        return;
      }

    }

    // --是否回電/處理日期--
    if (wp.itemEq("reply_flag", "Y")) {
      if (wp.itemEmpty("eta_date")) {
        errmsg("勾選要回電, 預計回電日期不可空白");
        return;
      }
      if (wp.itemStr("eta_date").compareTo(this.sysDate) < 0) {
        errmsg("預計回電日期, 預計回電日期須 >= 今日");
        return;
      }
    }

    // --是否追蹤/追蹤日期--
    if (wp.itemEq("case_trace_flag", "Y")) {
      if (wp.itemEmpty("case_trace_date")) {
        errmsg("預計追蹤日期不可空白");
        return;
      }
      if (wp.itemStr("case_trace_date").compareTo(this.sysDate) < 0) {
        errmsg("追蹤日期需 >= 今日");
        return;
      }
    }

    wp.itemSet("case_conf_flag", "N");
//    if (wp.itemEq("ugcall_flag", "Y")) {
//      wp.itemSet("case_conf_flag", "V");
//    } else {
//    	String caseType = wp.itemStr("case_type");
//      strSql = "select count(*) as db_cnt" + " from cms_casetype" + " where case_type ='1'"
//          + commSqlStr.col(caseType, "case_id") + " and conf_mark ='Y'";
//      setString(caseType);
//      sqlSelect(strSql);
//      if (colNum("db_cnt") > 0) {
//        wp.itemSet("case_conf_flag", "V");
//      }
//    }
    
    String caseType = wp.itemStr("case_type");
    strSql = "select count(*) as db_cnt"
            + " from cms_casetype"
            + " where case_type ='1'"
            + commSqlStr.col(caseType, "case_id")
            + " and conf_mark ='Y'"
      ;
    setString(caseType);
	  sqlSelect(strSql);
	  if (colNum("db_cnt") > 0) {
	    wp.itemSet("case_conf_flag", "V");
	  }	else	{
      	rc = 1;
      }      

    if (wp.itemEmpty("case_trace_date") == false) {
      if (eqIgno(wp.itemStr2("case_trace_flag"), "Y") == false) {
        errmsg("請勾選案件追蹤日期");
        return;
      }
    }

    if (wp.itemEmpty("case_type")) {
      errmsg("案件類別:不可空白");
      return;
    }

  }

   void getCaseSeqno() {
   // strSql = "select " + sqlID + "uf_case_seqno() as case_seqno" + " from " + this.sqlDual;
	strSql = "select right('00000'||cms_case.nextval,6) as case_seqno from " + this.sqlDual;
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("無法取得自動編號: cms_case");
      return;
    }
    wp.itemSet("case_seqno", colStr("case_seqno"));
  }


  @Override
  public int dbInsert() {
    this.dateTime();
    this.actionCode = "A1";
    // -案件存檔-
    dataCheck();
    if (rc != 1)
      return rc;

    if (wp.itemEmpty("case_seqno")) {
      getCaseSeqno();
      if (rc != 1)
        return rc;
    }
    
    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("cms_casemaster", wp);
    sp.ppymd("case_date");
    sp.ppstr("case_seqno");
    sp.ppstr("case_idno");
    sp.ppstr("card_no");
    sp.ppstr("case_type");
    sp.ppstr("case_desc");
    sp.ppstr("case_desc2");
    if (wp.itemEq("case_type", "99999")) {
      sp.ppstr("case_result", "9"); // -9-
      if (wp.itemEq("case_result", "9")) {
        sp.ppymd("finish_date");
      }
    } else if (wp.itemEq("send_code", "Y") == false) {
      sp.ppstr("case_result", "9");
      sp.ppymd("finish_date");
    } else {
      sp.ppstr("case_result", "0");
    }

    sp.ppnvl("send_code", "N"); // * 客服案件傳送碼 */ --
    sp.ppstr("case_user");
    // post_code VARCHAR(1) , /* 客服案件郵件號碼 */ --
    sp.ppstr("debit_flag");
    sp.ppnvl("ugcall_flag", "N");
    sp.ppstr("eta_date");
    sp.ppnvl("reply_flag", "N");
    sp.ppstr("reply_phone");
    sp.ppnvl("case_trace_flag", "N");
    sp.ppstr("case_trace_date");
    sp.modxxx();

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      errmsg("insert CMS_CASEMASTER[案件存檔] error");
      return -1;
    }

    if (rc == 1) {
      insertCmsCaseDetail();
    }

    if (wp.itemEq("case_type", "99999")) {
      if (wp.itemEmpty("db_case_id_A") == false) {
        insertCmsCasePost(wp.itemStr("db_case_id_A"));
        if (rc != 1)
          return rc;
      }

      if (wp.itemEmpty("db_case_id_B") == false) {
        insertCmsCasePost(wp.itemStr("db_case_id_B"));
        if (rc != 1)
          return rc;
      }

      if (wp.itemEmpty("db_case_id_C") == false) {
        insertCmsCasePost(wp.itemStr("db_case_id_C"));
        if (rc != 1)
          return rc;
      }

      if (wp.itemEmpty("db_case_id_D") == false) {
        insertCmsCasePost(wp.itemStr("db_case_id_D"));
        if (rc != 1)
          return rc;
      }
    }

    return rc;
  }

private  void insertCmsCaseDetail() {
    if (wp.itemEq("send_code", "Y") == false) {
      return;
    }
    busi.SqlPrepare sp = new busi.SqlPrepare();
    int llOptRows = wp.itemRows("opt_dept");
    String[] aaDept = wp.itemBuff("dept_code");
    // String[] aa_opt = wp.item_buff("opt_dept");
    String lsConfMark = "";
    if (wp.itemEq("case_type_conf", "Y"))
      lsConfMark = "V";
    else
      lsConfMark = "N";
    int rr = 0;
    for (int ll = 0; ll < llOptRows; ll++) {
      rr = (int) (wp.itemNum(ll, "opt_dept") - 1);

      wp.colSet(ll, "opt_dept", "1");

      sp.sql2Insert("cms_casedetail");
      sp.ppstr("case_date", wp.itemStr("case_date"));
      sp.ppstr("case_seqno", wp.itemStr("case_seqno"));
      sp.ppstr("proc_deptno", aaDept[rr]);
      sp.ppstr("proc_result", "0");
      sp.ppstr("case_conf_flag", lsConfMark);
      sp.ppymd("crt_date");
      sp.ppstr("crt_user", wp.loginUser);
      sp.ppstr("apr_flag", "N");
      sp.modxxx(modUser, modPgm);

      sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        errmsg("案件移送失敗: (CMS_CASEDETAIL); dept_no=" + wp.itemStr(ll, "dept_code"));
        return;
      }
    }
  }

private  void insertCmsCasePost(String caseId) {
    if (empty(caseId)) {
      return;
    }
    busi.SqlPrepare sp = new busi.SqlPrepare();
    sp.sql2Insert("cms_casepost", wp);
    sp.ppymd("case_date");
    sp.ppstr("case_seqno");
    sp.ppstr("proc_deptno", caseId);
    sp.ppstr("proc_id", "99999");
    sp.ppstr("proc_desc", wp.itemStr("mail_desc"));
    sp.ppstr("result_flag", wp.itemStr("result_flag"));
    sp.ppstr("bill_sending_zip", wp.itemStr("bill_zip"));
    sp.ppstr("bill_sending_addr1", wp.itemStr("bill_addr1"));
    sp.ppstr("bill_sending_addr2", wp.itemStr("bill_addr2"));
    sp.ppstr("bill_sending_addr3", wp.itemStr("bill_addr3"));
    sp.ppstr("bill_sending_addr4", wp.itemStr("bill_addr4"));
    sp.ppstr("bill_sending_addr5", wp.itemStr("bill_addr5"));
    // print_date VARCHAR(20) , /* 列印日期時間 */ --
    // print_seqno VARCHAR(22) , /* 列印序號 */ --
    // print_user VARCHAR(10) , /* 列印經辦 */ --
    sp.ppstr("recv_cname");
    sp.ppstr("case_sale_id");
    sp.ppstr("mail_send_type");
    sp.modxxx();

    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum != 1) {
      errmsg("insert CMS_CASEPOST error");
      return;
    }

    return;
  }

@Override
  public int dbDelete() {
	return 0;
  }

  @Override
  public int dataProc() {
    actionInit("C");

    strSql = " update cms_casemaster set case_trace_flag ='F' where case_seqno =:case_seqno ";
    var2ParmStr("case_seqno");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update cms_casemaster error !");
    }

    return rc;
  }

@Override
public int dbUpdate() {
	// TODO Auto-generated method stub
	return 0;
}


}
