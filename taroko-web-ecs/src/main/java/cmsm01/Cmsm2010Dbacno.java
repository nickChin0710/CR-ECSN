package cmsm01;
/**
 * 111-03-02  V1.00.14   JustinWu      新增異動日期及時間在待覆核資訊
 * 110-10-05  V1.00.13   JustinWu      add zip2
 * 110-01-06  V1.00.12   JustinWu      updated for XSS
 * 109-12-16   V1.00.11  JustinWu      add cleanModCol()
 * 109-09-09  V1.00.10  JustinWu     主檔apr_user=approveUser, mod_user=modifyUser, and update_user=modifyUser
 * 109-04-19  V1.00.06  shiyuqi       updated for project coding standard     
 * 2019-1211:  Alex  bug fix
 * 2019-0613:  JH    p_xxx >>acno_p_xxx
 * 2019-0418:  JH    modify
* */
import busi.FuncAction;

public class Cmsm2010Dbacno extends FuncAction {
  String lsAddr3 = "", lsKey = "", lsCode = "";
  String[] bbCol = new String[] {"bill_sending_zip", "bill_sending_addr1", "bill_sending_addr2",
      "bill_sending_addr3", "bill_sending_addr4", "bill_sending_addr5"};

  @Override
  public void dataCheck() {
    // TODO Auto-generated method stub

  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    String sql1 = "select * from dba_acno" + " where p_seqno =?";
    sqlSelect(sql1, new Object[] {wp.itemStr("p_seqno")});
    if (sqlRowNum <= 0) {
      errmsg("dba_acno not find, kk=");
      return rc;
    }
    delDbonline();
    insertDbonlineItem();
    return rc;
  }

  @Override
  public int dbDelete() {
    delDbonline();
    return rc;
  }


  // **2017/11/21*****************************************************************
  public int dbAcnoOnline() {
    if (empty(wp.colStr("p_seqno"))) {
      errmsg("帳戶流水號(p_seqno): 不可空白");
      return -1;
    }

    strSql = " select " 
        + " bill_sending_zip , " 
    	+ " bill_sending_addr1 ," 
        + " bill_sending_addr2 ,"
        + " bill_sending_addr3 ," 
        + " bill_sending_addr4 ," 
        + " bill_sending_addr5 ,"
        + " to_char(to_date(MOD_DATE || MOD_TIME2, 'yyyy/mm/dd hh24:mi:ss') , 'yyyy/mm/dd hh24:mi:ss') as userModDateTime , " 
        + " mod_user"
        + " from dbs_modlog_main" 
        + " where p_seqno =?" 
        + " and nvl(apr_flag,'N') <>'Y'"
        + " and mod_table ='DBA_ACNO' ";
    this.sqlSelect(strSql, new Object[] {wp.colStr("p_seqno")});
    if (sqlRowNum <= 0) {
      return 0;
    }
    
    // split zipCode into two parts, zip_code1 and zip_code2.  
    String[] billSendingZipArr = commString.splitZipCode(colStr("bill_sending_zip"));
    colSet("bill_sending_zip", billSendingZipArr[0]);
    colSet("bill_sending_zip2", billSendingZipArr[1]);

    wp.colSet("mod_user", colStr("mod_user"));
    wp.colSet("userModDateTime", this.colStr("userModDateTime"));

    return checkModifyDbAppr();
  }

  int checkModifyDbAppr() {
    int liMod = 0;
    for (int ii = 0; ii < bbCol.length; ii++) {
      String col = bbCol[ii].trim();
      if (this.colEqIgno(col, wp.colStr(col)) == true) {
        continue;
      }
      wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
      wp.colSet(col, colStr(col));
      liMod = 1;
    }

    return liMod;
  }

  void delDbonline() {
    strSql = "delete dbs_modlog_main " + " where p_seqno =:p_seqno " + " and mod_table='DBA_ACNO' "
        + " and apr_flag<>'Y' ";

    item2ParmStr("p_seqno");
    sqlExec(strSql);
  }


  void insertDbonlineItem() {
    strSql = "insert into dbs_modlog_main (" + " mod_table ," + " mod_date ," + " mod_time2 ,"
        + " mod_user ," + " mod_pgm ," + " mod_audcode ," + " mod_deptno ," + " apr_flag ,"
        + " p_seqno ," + " bill_sending_zip ," + " bill_sending_addr1 ," + " bill_sending_addr2 ,"
        + " bill_sending_addr3 ," + " bill_sending_addr4 ," + " bill_sending_addr5 " + " ) values ("
        + " 'DBA_ACNO' ," + " to_char(sysdate,'yyyymmdd') ," + " to_char(sysdate,'hh24miss') ,"
        + " :mod_user ," + " 'cmsm2010_dbacno' ," + " 'U' ," + " :mod_deptno ," + " 'N' ,"
        + " :p_seqno ," + " :bill_sending_zip ," + " :bill_sending_addr1 ,"
        + " :bill_sending_addr2 ," + " :bill_sending_addr3 ," + " :bill_sending_addr4 ,"
        + " :bill_sending_addr5 " + " )";

    setString("mod_user", wp.loginUser);
    setString("mod_deptno", userDeptNo());
    item2ParmStr("p_seqno");
    item2ParmStr("bill_sending_zip");
    item2ParmStr("bill_sending_addr1");
    item2ParmStr("bill_sending_addr2");
    item2ParmStr("bill_sending_addr3");
    item2ParmStr("bill_sending_addr4");
    item2ParmStr("bill_sending_addr5");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(getMsg());
      return;
    }
  }


  @Override
  public int dataProc() {

    String sql1 = " select " + " bill_sending_zip , " + " bill_sending_addr1 ,"
        + " bill_sending_addr2 ," + " bill_sending_addr3 ," + " bill_sending_addr4 ,"
        + " bill_sending_addr5" + " from dbs_modlog_main" + " where p_seqno =?"
        + " and nvl(apr_flag,'N') <>'Y'" + " and mod_table ='DBA_ACNO' ";
    this.sqlSelect(strSql, new Object[] {wp.itemStr("p_seqno")});

    updateDbaAcno();
    if (rc != 1)
      return rc;
    updateModLog();
    if (rc != 1)
      return rc;
    insertIBM();
    return rc;
  }

  /**
   * update 不須覆核資料
   */
  void updateDbaAcno() {

    msgOK();
    strSql = " update dba_acno set " 
        + " bill_sending_zip =:bill_sending_zip , "
        + " bill_sending_addr1 =:bill_sending_addr1 , "
        + " bill_sending_addr2 =:bill_sending_addr2 , "
        + " bill_sending_addr3 =:bill_sending_addr3 , "
        + " bill_sending_addr4 =:bill_sending_addr4 , "
        + " bill_sending_addr5 =:bill_sending_addr5 , "
        // 2020-09-08 JustinWu
        + " update_user =:update_user , "
        + " update_date = to_char(sysdate, 'yyyyMMdd') , "
        + " mod_user =:mod_user , "
        + " mod_pgm =:mod_pgm , "
        + " mod_time = sysdate , "
        + " mod_seqno = nvl(mod_seqno, 0)+1 "  
        // 2020-09-08 JustinWu
        + " where p_seqno =:p_seqno ";

    setString("bill_sending_zip", colStr("bill_sending_zip"));
    setString("bill_sending_addr1", colStr("bill_sending_addr1"));
    setString("bill_sending_addr2", colStr("bill_sending_addr2"));
    setString("bill_sending_addr3", colStr("bill_sending_addr3"));
    setString("bill_sending_addr4", colStr("bill_sending_addr4"));
    setString("bill_sending_addr5", colStr("bill_sending_addr5"));
    // 2020-09-08 JustinWu
    setString("update_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm2010");
    // 2020-09-08 JustinWu
    setString2("p_seqno", wp.itemStr2("p_seqno"));
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update dba_acno error !");
      rc = -1;
      return;
    }
  }

  void updateModLog() {
    msgOK();

    strSql = " update dbs_modlog_main set " + " apr_user =:apr_user ,"
        + " apr_date =to_char(sysdate,'yyyymmdd') ," + " apr_time =to_char(sysdate,'hh24miss') ,"
        + " apr_flag ='Y' ," + " mod_date =to_char(sysdate,'yyyymmdd') ,"
        + " mod_time2 =to_char(sysdate,'hh24miss') ," + " mod_user =:mod_user ,"
        + " mod_pgm =:mod_pgm " + " where mod_table ='DBA_ACNO' " + " and p_seqno =:p_seqno "
        + " and apr_flag <> 'Y' ";

    setString("apr_user", wp.loginUser);
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());
    setString2("p_seqno", wp.itemStr2("p_seqno"));
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update 異動記錄檔 error !");
      rc = -1;
      return;
    }
  }

  void insertIBM() {
    msgOK();

    lsAddr3 = "";
    lsAddr3 = wp.itemStr("bill_sending_addr3");
    lsKey = "";
    lsKey = wp.itemStr("acct_key").substring(0, 10);
    lsCode = wp.itemStr("acct_key").substring(10, 11);
    strSql = "insert into dbs_main2ibm (" + " crd_date , " + " id_p_seqno , " + " p_seqno , "
        + " acct_no , " + " card_id , " + " card_id_code , " + " mail_zip , " + " mail_addr1 , "
        + " mail_addr2 , " + " mail_addr3 , " + " mail_addr5 , " + " mod_user , " + " apr_user "
        + " ) values (" + " sysdate , " + " :id_p_seqno , " + " :p_seqno , " + " :acct_no , "
        + " :card_id , " + " :card_id_code , " + " :mail_zip , " + " :mail_addr1 , "
        + " :mail_addr2 , " + " :mail_addr3 , " + " :mail_addr5 , " + " :mod_user , "
        + " :apr_user " + " )";

    item2ParmStr("id_p_seqno");
    setString2("p_seqno", wp.itemStr2("p_seqno"));
    item2ParmStr("acct_no");
    setString("card_id", lsKey);
    setString("card_id_code", lsCode);
    item2ParmStr("mail_zip", "bill_sending_zip");
    item2ParmStr("mail_addr1", "bill_sending_addr1");
    item2ParmStr("mail_addr2", "bill_sending_addr2");
    if (empty(lsAddr3)) {
      setString("mail_addr3", "ｏｏ里");
    } else {
      setString("mail_addr3", lsAddr3);
    }
    item2ParmStr("mail_addr5", "bill_sending_addr5");
    setString("mod_user", wp.loginUser);
    setString("apr_user", wp.loginUser);
    sqlExec(strSql);

    if (rc != 1) {
      errmsg("Insert 傳送 IBM 資料檔失敗 !");
      rc = -1;
      return;
    }


  }

	public void cleanModCol() {
		for (int i = 0; i < bbCol.length; i++) {
			wp.colSet("mod_"+bbCol[i], "");
		}

	}

}
