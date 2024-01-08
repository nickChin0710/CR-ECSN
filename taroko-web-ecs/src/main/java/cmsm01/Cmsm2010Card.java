/*
 * 2019-1223  V1.00.01  Alex  son_card_flag = N , indiv_crd_lmt=0 , indiv_inst_lmt =0
 * 109-04-19  V1.00.02  shiyuqi       updated for project coding standard   
 * 109-05-29  V1.00.03  shiyuqi       新增欄位
 * 109-06-01  V1.00.04  tanwei        覆核
 * 109-06-11  V1.00.05  tanwei        bug修改
 * 109-08-05  V1.00.06  JustinWu   comment debit's card_fee_date, fee_code, and lost_fee_code
 * 109-08-21  V1.00.07  JustinWu   修改新增資料修改前後時的mod_date, mod_time
 * 109-09-01  V1.00.08  JustinWu   不比較indiv_inst_lmt and add auto_installment
 * 109-09-09  V1.00.09  JustinWu   主檔apr_user=approveUser, mod_user=modifyUser, and update_user=modifyUser
 * 109-12-16  V1.00.10  JustinWu   add cleanModCol() and add colSet("mod_col", "")
 * 110-01-06  V1.00.11  JustinWu   updated for XSS
 * 110-12-01  V1.00.12  JustinWu   update msg_flag and msg_purchase_amt
 * 111-03-02  V1.00.13  JustinWu   新增異動日期及時間在待覆核資訊
 */
package cmsm01;

import busi.FuncAction;

public class Cmsm2010Card extends FuncAction {

  String[] aaCol = new String[] { "eng_name", "source_code", "reg_bank_no", "member_id",
       "indiv_crd_lmt", // "decimal",true) , // "indiv_inst_lmt", // "decimal",true)
//      "promote_emp_no", 
//      "introduce_id", 
//      "introduce_name",
//      "introduce_emp_no",
//      "promote_dept",
      "card_fee_date"};
  
  String[] checkboxCol = new String[] {
		  "son_card_flag"
  };

  public int cardOnline() {
    if (empty(wp.colStr("card_no"))) {
      errmsg("卡號: 不可空白");
      return -1;
    }
    strSql = "select * , to_char(to_date(MOD_DATE || MOD_TIME2, 'yyyy/mm/dd hh24:mi:ss') , 'yyyy/mm/dd hh24:mi:ss') as userModDateTime "
    	+ " from crd_card_online" 
        + " where card_no =?" 
    	+ " and apr_YN ='Y' "
        + " and apr_flag <>'Y'" + " and data_image = '2' ";
    this.sqlSelect(strSql, new Object[] {wp.colStr("card_no")});

    convMemberId();
    if (sqlRowNum <= 0) {
      return 0;
    }
    wp.colSet("userModDateTime", this.colStr("userModDateTime"));
    return checkModifyAppr();
  }

  int checkModifyAppr() {
    int liMod = 0;
    
    for (int ii = 0; ii < aaCol.length; ii++) {
      String col = aaCol[ii].trim();
      log("col:" + colStr(col) + " wp:" + wp.colStr(col));
      if (this.colEqIgno(col, wp.colStr(col)) == true) {
    	wp.colSet("mod_" + col, "");
        continue;
      }

      wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
      wp.colSet(col, colStr(col));
      liMod = 1;
    }
    
    // checkbox
    for (int ii = 0; ii < checkboxCol.length; ii++) {
        String col = checkboxCol[ii].trim();
        log("col:" + colStr(col) + " wp:" + wp.colStr(col));
        
        if (this.colNvl(col, "N").equalsIgnoreCase(wp.colNvl(col, "N")) == true) {
        	wp.colSet("mod_" + col, "");
			continue;
		}
        
        wp.colSet("mod_" + col, "background-color: rgb(255,191,128)");
        wp.colSet(col, colStr(col));
        liMod = 1;
      }

    wp.colSet("mod_date", colStr("mod_date"));
    wp.colSet("mod_time2", colStr("mod_time2"));
    wp.colSet("mod_user", colStr("mod_user"));
    log("li_mod:" + liMod);
    return liMod;
  }

  void convMemberId() {
    if (wp.colEq("group_code", "2222") && wp.colEq("card_type", "MS")) {
      wp.colSet("member_id", "");
      wp.colSet("member_id_memo", "同持卡人ID");
    } else if (wp.colEq("card_type", "SM") && pos("|2223|2224", wp.colStr("group_code")) > 0) {
      wp.colSet("member_id", "");
      wp.colSet("member_id_memo", "同持卡人ID");
    } else
      wp.colSet("member_id_memo", "");
  }

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
    dataCheck();
    if (rc != 1)
      return rc;

    if (wp.itemEq("debit_flag", "N")) {
      strSql = "select * from crd_card" 
                  + " where card_no =?";
      sqlSelect(strSql, new Object[] {wp.itemStr("card_no")});
      updateCrdCard();
      delOnline();
      if (checkModifyAppr() == 1) {
				getSysDate();
				insertOnlineColN();
				insertOnlineColY();
				insertOnlineItemN();
				insertOnlineItemY();
      }
    } else if (wp.itemEq("debit_flag", "Y")) {
    	// update 不須覆核資料
      
      StringBuffer sb = new StringBuffer();
      
      sb.append(" update dbc_card set " );
      sb.append(" promote_emp_no =:promote_emp_no , ");
      sb.append(" introduce_id =:introduce_id , ");
      sb.append(" introduce_emp_no =:introduce_emp_no , ");
      sb.append(" promote_dept =:promote_dept , ");
      sb.append(" clerk_id =:clerk_id , ");
      // 2021/12/01 Justin update msg_flag and msg_purchase_amt
      sb.append(" msg_flag =:msg_flag , ");
      sb.append(" msg_purchase_amt =:msg_purchase_amt , ");
      // 2020-09-08 JustinWu
      sb.append(" mod_user =:mod_user , ");
      sb.append(" mod_pgm =:mod_pgm , ");
      sb.append(" mod_time = sysdate , ");
      sb.append(" mod_seqno = nvl(mod_seqno, 0)+1 "  );
      // 2020-09-08 JustinWu
//      sb.append(" lost_fee_code =:lost_fee_code " );
      sb.append(" where card_no = :card_no ");
      
      strSql = sb.toString();
      
      item2ParmStr("promote_emp_no");
      item2ParmStr("introduce_id");
      item2ParmStr("introduce_emp_no");
      item2ParmStr("promote_dept");
      item2ParmStr("clerk_id");
      // 2021/12/01 Justin update msg_flag and msg_purchase_amt
      item2ParmNvl("msg_flag", "Y");
      item2ParmNum("msg_purchase_amt");
 
      // 2020-09-08 JustinWu
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", "cmsm2010");
      // 2020-09-08 JustinWu
//      item2ParmStr("card_fee_date");
//      item2ParmStr("fee_code");
//      item2ParmNvl("lost_fee_code", "N");
      item2ParmStr("card_no");
      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg(sqlErrtext);
      }
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    dataCheck();
    if (rc != 1)
      return rc;

    delOnline();

    return rc;
  }



  void insertOnlineColN() {
    strSql = "insert into crd_card_online (" + " apr_YN , " + " apr_flag , " + " data_image , "
        + " card_no , " + " eng_name , " + " source_code , " + " reg_bank_no , " + " member_id , "
        + " fee_code , " + " son_card_flag , " + " indiv_crd_lmt , " + " indiv_inst_lmt , "
        + " lost_fee_code , " + " promote_emp_no , " + " introduce_id , " + " introduce_name , "
        + " introduce_emp_no , " + " promote_dept , " +" card_fee_date, "
        + " major_relation , " + " auto_installment , " + " mod_date , " + " mod_time2 , "
        + " mod_user , " + " mod_deptno " + " ) values (" + " 'N' , " + " 'N' , " + " '1' , "
        + " :card_no , " + " :eng_name , " + " :source_code , " + " :reg_bank_no , "
        + " :member_id , " + " :fee_code , " + " :son_card_flag , " + " :indiv_crd_lmt , "
        + " :indiv_inst_lmt , " + " :lost_fee_code , " + " :promote_emp_no , " + " :introduce_id , "
        + " :introduce_name , "  + " :introduce_emp_no , " + " :promote_dept , " +" :card_fee_date ,"
        +" :major_relation , " + " :auto_installment , "
        + " :mod_date , " + " :mod_time , " + " :mod_user , "
        + " '' " + " )";

    col2ParmStr("card_no");
    col2ParmStr("eng_name");
    col2ParmStr("source_code");
    col2ParmStr("reg_bank_no");
    col2ParmStr("member_id");
    col2ParmStr("fee_code");
    col2ParmNvl("son_card_flag", "N");
    col2ParmNum("indiv_crd_lmt");
    col2ParmNum("indiv_inst_lmt");
    col2ParmNvl("lost_fee_code", "N");
    col2ParmStr("promote_emp_no");
    col2ParmStr("introduce_id");
    col2ParmStr("introduce_name");
    col2ParmStr("introduce_emp_no");
    col2ParmStr("promote_dept");
    col2ParmStr("card_fee_date");
    col2ParmStr("major_relation");
    col2ParmNvl("auto_installment", "N");
    setString("mod_date", sysDate);
    setString("mod_time", sysTime);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_card_online error insertOnlineColN" );
      return;
    }
  }

  void insertOnlineColY() {
    strSql = "insert into crd_card_online (" + " apr_YN , " + " apr_flag , " + " data_image , "
        + " card_no , " + " eng_name , " + " source_code , " + " reg_bank_no , " + " member_id , "
        + " fee_code , " + " son_card_flag , " + " indiv_crd_lmt , " + " indiv_inst_lmt , "
        + " lost_fee_code , " + " promote_emp_no , " + " introduce_id , " + " introduce_name , "
        + " introduce_emp_no , " + " promote_dept , "+ " card_fee_date , "
        + " major_relation , " + " auto_installment , " + " mod_date , " + " mod_time2 , "
        + " mod_user , " + " mod_deptno " + " ) values (" + " 'Y' , " + " 'N' , " + " '1' , "
        + " :card_no , " + " :eng_name , " + " :source_code , " + " :reg_bank_no , "
        + " :member_id , " + " :fee_code , " + " :son_card_flag , " + " :indiv_crd_lmt , "
        + " :indiv_inst_lmt , " + " :lost_fee_code , " + " :promote_emp_no , " + " :introduce_id , "
        + " :introduce_name , "    + " :introduce_emp_no , " + " :promote_dept , " + " :card_fee_date ,"
        + " :major_relation , " + " :auto_installment , "
        + " :mod_date , " + " :mod_time , " + " :mod_user , "
        + " '' " + " )";

    col2ParmStr("card_no");
    col2ParmStr("eng_name");
    col2ParmStr("source_code");
    col2ParmStr("reg_bank_no");
    col2ParmStr("member_id");
    col2ParmStr("fee_code");
    col2ParmNvl("son_card_flag", "N");
    col2ParmNum("indiv_crd_lmt");
    col2ParmNum("indiv_inst_lmt");
    col2ParmNvl("lost_fee_code", "N");
    col2ParmStr("promote_emp_no");
    col2ParmStr("introduce_id");
    col2ParmStr("introduce_name");
    col2ParmStr("introduce_emp_no");
    col2ParmStr("promote_dept");
    col2ParmStr("card_fee_date");
    col2ParmStr("major_relation");
    col2ParmNvl("auto_installment", "N");
    setString("mod_date", sysDate);
    setString("mod_time", sysTime);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_card_online error insertOnlineColY");
      return;
    }
  }

  void insertOnlineItemN() {
    strSql = "insert into crd_card_online (" + " apr_YN , " + " apr_flag , " + " data_image , "
        + " card_no , " + " eng_name , " + " source_code , " + " reg_bank_no , " + " member_id , "
        + " fee_code , " + " son_card_flag , " + " indiv_crd_lmt , " + " indiv_inst_lmt , "
        + " lost_fee_code , " + " promote_emp_no , " + " introduce_id , " + " introduce_name , "
        + " introduce_emp_no , " + " promote_dept , " + " card_fee_date , "
        + " major_relation , " + " auto_installment , " + " mod_date , " + " mod_time2 , "
        + " mod_user , " + " mod_deptno " + " ) values (" + " 'N' , " + " 'N' , " + " '2' , "
        + " :card_no , " + " :eng_name , " + " :source_code , " + " :reg_bank_no , "
        + " :member_id , " + " :fee_code , " + " :son_card_flag , " + " :indiv_crd_lmt , "
        + " :indiv_inst_lmt , " + " :lost_fee_code , " + " :promote_emp_no , " + " :introduce_id , "
        + " :introduce_name , "      + " :introduce_emp_no , " + " :promote_dept , " + ":card_fee_date , "
        + " :major_relation , " + " :auto_installment , "
        + " :mod_date , " + " :mod_time , " + " :mod_user , "
        + " '' " + " )";

    item2ParmStr("card_no");
    item2ParmStr("eng_name");
    item2ParmStr("source_code");
    item2ParmStr("reg_bank_no");
    item2ParmStr("member_id");
    item2ParmStr("fee_code");
    item2ParmNvl("son_card_flag", "N");
    if (wp.itemEq("son_card_flag", "N")) {
      setNumber("indiv_crd_lmt", 0);
      setNumber("indiv_inst_lmt", 0);
    } else {
      item2ParmNum("indiv_crd_lmt");
      item2ParmNum("indiv_inst_lmt");
    }
    item2ParmNvl("lost_fee_code", "N");
    item2ParmStr("promote_emp_no");
    item2ParmStr("introduce_id");
    item2ParmStr("introduce_name");
    item2ParmStr("introduce_emp_no");
    item2ParmStr("promote_dept");
    item2ParmStr("card_fee_date");
    item2ParmStr("major_relation");
    item2ParmNvl("auto_installment", "N");
    setString("mod_date", sysDate);
    setString("mod_time", sysTime);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_card_online error insertOnlineItemN");
      return;
    }
  }

  void insertOnlineItemY() {
    strSql = "insert into crd_card_online (" + " apr_YN , " + " apr_flag , " + " data_image , "
        + " card_no , " + " eng_name , " + " source_code , " + " reg_bank_no , " + " member_id , "
        + " fee_code , " + " son_card_flag , " + " indiv_crd_lmt , " + " indiv_inst_lmt , "
        + " lost_fee_code , " + " promote_emp_no , " + " introduce_id , " + " introduce_name , "
        + " introduce_emp_no , " + " promote_dept , " +" card_fee_date , "
        + " major_relation , " + " auto_installment , " + " mod_date , " + " mod_time2 , "
        + " mod_user , " + " mod_deptno " + " ) values (" + " 'Y' , " + " 'N' , " + " '2' , "
        + " :card_no , " + " :eng_name , " + " :source_code , " + " :reg_bank_no , "
        + " :member_id , " + " :fee_code , " + " :son_card_flag , " + " :indiv_crd_lmt , "
        + " :indiv_inst_lmt , " + " :lost_fee_code , " + " :promote_emp_no , " + " :introduce_id , "
        + " :introduce_name , " + " :introduce_emp_no , " + " :promote_dept , " +" :card_fee_date , "
        + " :major_relation , " + " :auto_installment , "
        + " :mod_date , " + " :mod_time , " + " :mod_user , "
        + " '' " + " )";

    item2ParmStr("card_no");
    item2ParmStr("eng_name");
    item2ParmStr("source_code");
    item2ParmStr("reg_bank_no");
    item2ParmStr("member_id");
    item2ParmStr("fee_code");
    item2ParmNvl("son_card_flag", "N");
    if (wp.itemEq("son_card_flag", "N")) {
      setNumber("indiv_crd_lmt", 0);
      setNumber("indiv_inst_lmt", 0);
    } else {
      item2ParmNum("indiv_crd_lmt");
      item2ParmNum("indiv_inst_lmt");
    }
    item2ParmNvl("lost_fee_code", "N");
    item2ParmStr("promote_emp_no");
    item2ParmStr("introduce_id");
    item2ParmStr("introduce_name");
    item2ParmStr("introduce_emp_no");
    item2ParmStr("promote_dept");
    item2ParmStr("card_fee_date");
    item2ParmStr("major_relation");
    item2ParmNvl("auto_installment", "N");
    setString("mod_date", sysDate);
    setString("mod_time", sysTime);
    setString("mod_user", wp.loginUser);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert crd_card_online error insertOnlineItemY");
      return;
    }
  }

  void delOnline() {
    strSql = "delete crd_card_online " 
                + " where card_no =:card_no " 
    		    + " and apr_YN='Y' "
                + " and apr_flag<>'Y' ";

    item2ParmStr("card_no");
    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete crd_card_online error");
      return;
    }
  }

  /**
   * update 不須覆核資料
   */
  void updateCrdCard() {
    
    StringBuffer sb = new StringBuffer();
    
    sb.append(" update crd_card set " );
    sb.append(" promote_emp_no =:promote_emp_no , ");
    sb.append(" introduce_id =:introduce_id , ");
    sb.append(" introduce_emp_no =:introduce_emp_no , ");
    sb.append(" promote_dept =:promote_dept , ");
    sb.append(" clerk_id =:clerk_id , ");
    sb.append(" major_relation =:major_relation ,");
    sb.append(" lost_fee_code =:lost_fee_code ," );
    sb.append(" auto_installment =:auto_installment ,");
    // 2021/12/01 Justin update msg_flag and msg_purchase_amt
    sb.append(" msg_flag =:msg_flag ,"); 
    sb.append(" msg_purchase_amt =:msg_purchase_amt ,");
    sb.append(" mod_time =sysdate ," );
    sb.append(" mod_user =:mod_user ," );
    sb.append(" mod_pgm =:mod_pgm ,");
    sb.append(" mod_seqno =nvl(mod_seqno,0)+1 " );
    sb.append(" where card_no =:card_no ");
    
    strSql = sb.toString();

    log("lost_fee:" + wp.itemStr2("lost_fee_code"));
    item2ParmStr("promote_emp_no");
    item2ParmStr("introduce_id");
    item2ParmStr("introduce_emp_no");
    item2ParmStr("promote_dept");
    item2ParmStr("clerk_id");
    item2ParmStr("major_relation");
    item2ParmNvl("lost_fee_code", "N");
    item2ParmNvl("auto_installment", "N");
    // 2021/12/01 Justin update msg_flag and msg_purchase_amt
    item2ParmNvl("msg_flag", "Y");
    item2ParmNum("msg_purchase_amt");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "cmsm2010");
    item2ParmStr("card_no");
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update crd_card error ");
      return;
    }

  }

  // *****
  @Override
  public int dataProc() {
    checkCnt();
    if (rc != 1)
      return rc;

    strSql = "select *, mod_user as online_mod_user from crd_card_online" + " where card_no =?" + " and apr_YN ='Y' "
        + " and apr_flag <>'Y' " + " and data_image = '2' ";
    this.sqlSelect(strSql, new Object[] {wp.itemStr("card_no")});

    updateCrdCard1();
    if (rc != 1)
      return rc;

    updateCrdCardOnline();

    return rc;
  }

  void checkCnt() {
    String sql1 = " select count(*) as ll_cnt from crd_card_online " + " where card_no = ? "
        + " and mod_date = ? " + " and mod_time2 = ? " + " and mod_user = ? " + " and apr_yn='Y' "
        + " and nvl(apr_flag,'N') <> 'Y' ";

    sqlSelect(sql1, new Object[] {wp.itemStr("card_no"), wp.itemStr("mod_date"),
        wp.itemStr("mod_time2"), wp.itemStr("mod_user")});

    if (colNum("ll_cnt") != 2) {
      errmsg("此筆資料已有人修改, 請結束再覆核 !");
      rc = -1;
      return;
    }
  }

  /**
   * update 需覆核資料
   */
  void updateCrdCard1() {
    msgOK();
    strSql = " update crd_card set " 
        + " source_code =:source_code ,"
        + " reg_bank_no =:reg_bank_no ," 
        + " member_id =:member_id ," 
        + " fee_code =:fee_code ,"
        + " indiv_crd_lmt =:indiv_crd_lmt ," 
        + " indiv_inst_lmt =:indiv_inst_lmt ,"
        + " son_card_flag =:son_card_flag ," 
        + " eng_name =:eng_name ,"
        + " curr_fee_code =:curr_fee_code ," 
//        + " promote_emp_no =:promote_emp_no ,"
//        + " introduce_id =:introduce_id ," 
        + " introduce_name =:introduce_name ,"
//        + " introduce_emp_no =:introduce_emp_no , "
//        + " promote_dept =:promote_dept , " 
        + " card_fee_date =:card_fee_date , "
        + " apr_date =to_char(sysdate,'yyyymmdd') ," 
        + " apr_user =:apr_user ,"
        + " mod_user =:mod_user ," 
        + " mod_time =sysdate ," 
        + " mod_pgm =:mod_pgm ,"
        + " mod_seqno = nvl(mod_seqno, 0)+1 " 
        + " where card_no =:card_no ";

    setString("source_code", colStr("source_code"));
    setString("reg_bank_no", colStr("reg_bank_no"));
    setString("member_id", colStr("member_id"));
    setString("fee_code", colStr("fee_code"));
    setString("indiv_crd_lmt", colStr("indiv_crd_lmt"));
    setString("indiv_inst_lmt", colStr("indiv_inst_lmt"));
    setString("son_card_flag", colStr("son_card_flag"));
    setString("eng_name", colStr("eng_name"));
    item2ParmStr("curr_fee_code");
//    setString("promote_emp_no", colStr("promote_emp_no"));
//    setString("introduce_id", colStr("introduce_id"));
    setString("introduce_name", colStr("introduce_name"));
    
//    setString("introduce_emp_no",colStr("introduce_emp_no"));
//    setString("promote_dept",colStr("promote_dept"));
    setString("card_fee_date",colStr("card_fee_date"));
    
    setString("apr_user", wp.loginUser);
    setString("mod_user", colStr("online_mod_user"));
    setString("mod_pgm", "cmsp2010");
    item2ParmStr("card_no");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update crd_card error ! ");
      rc = -1;
      return;
    }
  }

  void updateCrdCardOnline() {
    msgOK();
    strSql = " update crd_card_online set " + " apr_flag = 'Y' ," + " apr_user =:apr_user ,"
        + " apr_date = to_char(sysdate,'yyyymmdd') ," + " apr_time = to_char(sysdate,'hh24miss')  "
        + " where card_no =:card_no " + " and mod_date =:mod_date " + " and mod_time2 =:mod_time2 "
        + " and apr_yn = 'Y' " + " and apr_flag <> 'Y' ";

    setString("apr_user", wp.loginUser);
    item2ParmStr("card_no");
    item2ParmStr("mod_date");
    item2ParmStr("mod_time2");
    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update crd_card_online error");
      rc = -1;
      return;
    }

  }

	public void cleanModCol() {
		for (int i = 0; i < aaCol.length; i++) {
			wp.colSet("mod_"+aaCol[i], "");
		}
		
		for (int i = 0; i < checkboxCol.length; i++) {
			wp.colSet("mod_"+checkboxCol[i], "");
		}
	}


}
