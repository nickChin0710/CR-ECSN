/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-23  V1.00.00  Andy       program initial                            *
* 107-07-31  V1.00.01  Andy		  update : add col digital_flag              *	
*  109-04-22  V1.00.02  yanghan  修改了變量名稱和方法名稱*                        
* 111-09-16  V1.00.03   Ryan      調整寄件別、卡片寄送地址                                                                    *  
* 112-06-05  V1.00.04   Ryan      增加卡片寄送地址註記欄位、處理邏輯
******************************************************************************/

package dbcm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Dbcm0160Func extends FuncEdit {
  String cardNo = "";
  String idPSeqno = "";
  String rowid = "", modSeqno = "";
  String mReturnDate = "";

  public Dbcm0160Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    String condCol = null;
    idPSeqno = wp.itemStr("id_p_seqno");
    mReturnDate = wp.itemStr("return_date");

    if (this.isAdd()) {
      // 驗證是否已用查詢條件(卡號或掛號號碼或掛號條碼)其中一項帶出基本資料
      String[] cond_input_arr = {"card_no", "mail_no", "barcode_num"};
      // 找出填寫的欄位是哪一個
      for (int i = 0; i < cond_input_arr.length; i++) {
        if (!empty(wp.itemStr(cond_input_arr[i]))) {
          condCol = cond_input_arr[i];
          break;
        }
      }
      if (empty(condCol)) {
        errmsg("請輸入卡號、掛號條碼其中一項，以讀取基本資料");
        return;
      }

    }


    // check mail_date
    if (wp.itemStr("proc_status").equals("3") || wp.itemStr("proc_status").equals("6")) {
      if (empty(wp.itemStr("mail_date"))) {
        errmsg("未填寫 退卡重寄日期!!");
        return;
      }
      if (Integer.parseInt(mReturnDate) > Integer.parseInt(wp.itemStr("mail_date"))) {
        errmsg("退卡日期 不可 大於 退卡重寄日期 !!");
        return;
      }
      if (wp.itemEmpty("mail_type")) {
        errmsg("未填寫寄件別 !!");
        return;
      }
    } else {
      wp.itemSet("mail_date", "");
    }

    cardNo = wp.itemStr("card_no");
    if (empty(cardNo)) {
      cardNo = wp.itemStr("kk_card_no");
    }
    if (empty(cardNo)) {
      errmsg("卡號為空值，無法新增/修改");
      return;
    }
//    if (wp.itemEmpty("mail_type") || wp.itemStr("mail_type").equals("3")
//        || wp.itemStr("mail_type").equals("4") || wp.itemStr("mail_type").equals("5")) {
//
//      wp.itemSet("db_zip_code", "");
//      wp.itemSet("db_mail_addr1", "");
//      wp.itemSet("db_mail_addr2", "");
//      wp.itemSet("db_mail_addr3", "");
//      wp.itemSet("db_mail_addr4", "");
//      wp.itemSet("db_mail_addr5", "");
//    } else {
//      wp.itemSet("db_zip_code", wp.itemStr("zip_code"));
//      wp.itemSet("db_mail_addr1", wp.itemStr("mail_addr1"));
//      wp.itemSet("db_mail_addr2", wp.itemStr("mail_addr2"));
//      wp.itemSet("db_mail_addr3", wp.itemStr("mail_addr3"));
//      wp.itemSet("db_mail_addr4", wp.itemStr("mail_addr4"));
//      wp.itemSet("db_mail_addr5", wp.itemStr("mail_addr5"));
//    }

    // 新增檢查開始
    if (this.isAdd()) {
//      String return_type = wp.itemStr("return_type");
//      // 檢查退卡性質別
//      if (empty(return_type)) {
//        errmsg("退卡性質別為空值，無法新增");
//        return;
//      }
      // 檢查退卡原因
      if (empty(wp.itemStr("reason_code"))) {
        errmsg("退卡原因為空值，無法新增");
        return;
      }

      // 檢查處理結果
      if (empty(wp.itemStr("proc_status"))) {
        errmsg("處理結果為空值，無法新增");
        return;
      }
      if (!wp.itemStr("proc_status").equals("1")) {
        errmsg("新增時，處理結果只能為1.處理中");
        return;
      }
      if(!this.ibDelete) {
      	if(wp.itemEq("mail_type", "1") || wp.itemEq("mail_type", "2"))
      		if(wp.itemEmpty("mail_addr_flag")){
      		   errmsg("寄件別為1或2時,卡片寄送地址註記不可為空值!!");
      	          return;
      	}		
      }
      
      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from dbc_return where card_no = ?  and return_date = ? ";
      Object[] param = new Object[] {cardNo, mReturnDate};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
      // 新增檢查結束
    } else {
      // 非新增
      // -other modify-
      sqlWhere = " where card_no = ? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {cardNo, wp.modSeqno()};
      isOtherModify("dbc_return", sqlWhere, param);
    }


  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }


    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("dbc_return");
    sp.ppstr("card_no", cardNo);
    sp.ppstr("return_date", wp.itemStr("return_date"));
    sp.ppstr("acct_no", wp.itemStr("acct_no"));
    sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
    sp.ppstr("ic_flag", wp.itemStr("ic_flag"));
    sp.ppstr("group_code", wp.itemStr("group_code"));
    sp.ppstr("beg_date", wp.itemStr("beg_date"));
    sp.ppstr("end_date", wp.itemStr("end_date"));
    sp.ppstr("mail_type", wp.itemStr("mail_type"));
    sp.ppstr("mail_branch", wp.itemStr("mail_branch"));
    sp.ppstr("return_type", wp.itemStr("return_type"));
    sp.ppstr("reason_code", wp.itemStr("reason_code"));
    if(wp.itemEmpty("mail_branch")) {
        sp.ppstr("zip_code", wp.itemStr("zip_code"));
        sp.ppstr("mail_addr1", wp.itemStr("mail_addr1"));
        sp.ppstr("mail_addr2", wp.itemStr("mail_addr2"));
        sp.ppstr("mail_addr3", wp.itemStr("mail_addr3"));
        sp.ppstr("mail_addr4", wp.itemStr("mail_addr4"));
        sp.ppstr("mail_addr5", wp.itemStr("mail_addr5"));
    }else {
        sp.ppstr("zip_code", wp.itemStr("db_zip_code"));
        sp.ppstr("mail_addr1", wp.itemStr("db_mail_addr1"));
        sp.ppstr("mail_addr2", wp.itemStr("db_mail_addr2"));
        sp.ppstr("mail_addr3", wp.itemStr("db_mail_addr3"));
        sp.ppstr("mail_addr4", wp.itemStr("db_mail_addr4"));
        sp.ppstr("mail_addr5", wp.itemStr("db_mail_addr5"));
    }
    sp.ppstr("proc_status", wp.itemStr("proc_status"));
    sp.ppstr("mail_date", wp.itemStr("mail_date"));
    sp.ppstr("mail_no", strMid(wp.itemStr("barcode_num"),0, 6));
    sp.ppstr("package_flag", wp.itemStr("package_flag"));
    sp.ppstr("return_note", wp.itemStr("return_note"));
    sp.ppstr("digital_flag", wp.itemStr("digital_flag"));
    sp.ppstr("return_seqno", wp.colStr("return_seqno"));
    sp.ppstr("barcode_num", wp.colStr("barcode_num"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sp.ppstr("mail_addr_flag", wp.itemStr("mail_addr_flag"));
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
//    }
//
//    // insert dbc_return_log
//    sp.sql2Insert("dbc_return_log");
//    sp.ppstr("card_no", cardNo);
//    sp.ppstr("acct_no", wp.itemStr("acct_no"));
//    sp.ppstr("return_date", wp.itemStr("return_date"));
//    sp.ppstr("group_code", wp.itemStr("group_code"));
//    sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
//    sp.ppstr("ic_flag", wp.itemStr("ic_flag"));
//    sp.ppstr("reason_code", wp.itemStr("reason_code"));
//    sp.ppstr("return_type", wp.itemStr("return_type"));
//    sp.ppstr("mail_type", wp.itemStr("mail_type"));
//    sp.ppstr("mail_branch", wp.itemStr("mail_branch"));
//    // sp.ppss("mail_date",wp.item_ss("mail_date"));
//    sp.ppstr("mail_no", strMid(wp.itemStr("barcode_num"),0, 6));
//    if(wp.itemEmpty("mail_branch")) {
//        sp.ppstr("zip_code", wp.itemStr("zip_code"));
//        sp.ppstr("mail_addr1", wp.itemStr("mail_addr1"));
//        sp.ppstr("mail_addr2", wp.itemStr("mail_addr2"));
//        sp.ppstr("mail_addr3", wp.itemStr("mail_addr3"));
//        sp.ppstr("mail_addr4", wp.itemStr("mail_addr4"));
//        sp.ppstr("mail_addr5", wp.itemStr("mail_addr5"));
//    }else {
//        sp.ppstr("zip_code", wp.itemStr("db_zip_code"));
//        sp.ppstr("mail_addr1", wp.itemStr("db_mail_addr1"));
//        sp.ppstr("mail_addr2", wp.itemStr("db_mail_addr2"));
//        sp.ppstr("mail_addr3", wp.itemStr("db_mail_addr3"));
//        sp.ppstr("mail_addr4", wp.itemStr("db_mail_addr4"));
//        sp.ppstr("mail_addr5", wp.itemStr("db_mail_addr5"));
//    }
//    sp.ppstr("proc_status", wp.itemStr("proc_status"));
//    sp.ppstr("package_flag", wp.itemStr("package_flag"));
//    sp.ppstr("package_date", wp.itemStr("package_date"));
//    sp.ppstr("return_note", wp.itemStr("return_note"));
//    sp.ppstr("digital_flag", wp.itemStr("digital_flag"));
//    sp.ppstr("return_seqno", wp.colStr("return_seqno"));
//    sp.ppstr("barcode_num", wp.colStr("barcode_num"));
//    sp.ppstr("beg_date", wp.itemStr("beg_date"));
//    sp.ppstr("end_date", wp.itemStr("end_date"));
//    sp.addsql(", mod_date ", ", to_char(sysdate,'yyyymmdd') ");
//    sp.addsql(", mod_time ", ", sysdate ");
//    sp.ppstr("mod_user", wp.loginUser);
//
//    sqlExec(sp.sqlStmt(), sp.sqlParm());
//    if (sqlRowNum <= 0) {
//      errmsg(sqlErrtext);
    } else {
      String msg = "資料新增成功，此筆資料的退卡編號為" + wp.colStr("return_seqno");
      wp.alertMesg = "<script language='javascript'> alert('" + msg + "'); </script>";
      wp.respMesg = msg;
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    rowid = wp.itemStr("rowid");
    modSeqno = wp.itemStr("mod_seqno");
    cardNo = wp.itemStr("card_no");
    String us_sql = "update dbc_return set " + "card_no =:card_no , " + "acct_no =:acct_no, "
        + "return_date =:return_date, " + "group_code =:group_code, " + "id_p_seqno =:id_p_seqno, "
        + "ic_flag =:ic_flag, " + "reason_code =:reason_code, " + "return_type =:return_type, "
        + "mail_type =:mail_type, " + "mail_branch =:mail_branch, " + "mail_date =:mail_date, "
        + "zip_code =:zip_code, " + "mail_addr1 =:mail_addr1, " + "mail_addr2 =:mail_addr2, "
        + "mail_addr3 =:mail_addr3, " + "mail_addr4 =:mail_addr4, " + "mail_addr5 =:mail_addr5, "
        + "proc_status =:proc_status, " + "package_flag =:package_flag, "
        + "package_date =:package_date, " + "return_note =:return_note, " + "beg_date =:beg_date, "
        + "end_date =:end_date, " + "mail_no =:mail_no, " + "barcode_num =:barcode_num, "
        + "digital_flag =:digital_flag, " + "mod_user =:mod_user, " + "mod_time = sysdate, "
        + "mod_pgm =:mod_pgm, " + "mod_seqno = nvl(mod_seqno,0)+1 , mail_addr_flag =:mail_addr_flag ";
    setString("card_no", wp.itemStr("card_no"));
    setString("acct_no", wp.itemStr("acct_no"));
    setString("return_date", wp.itemStr("return_date"));
    setString("group_code", wp.itemStr("group_code"));
    setString("id_p_seqno", wp.itemStr("id_p_seqno"));
    setString("ic_flag", wp.itemStr("ic_flag"));
    setString("reason_code", wp.itemStr("reason_code"));
    setString("return_type", wp.itemStr("return_type"));
    setString("mail_type", wp.itemStr("mail_type"));
    setString("mail_branch", wp.itemStr("mail_branch"));
    setString("mail_date", wp.itemStr("mail_date"));
    if(wp.itemEmpty("mail_branch")) {
    	setString("zip_code", wp.itemStr("zip_code"));
    	setString("mail_addr1", wp.itemStr("mail_addr1"));
    	setString("mail_addr2", wp.itemStr("mail_addr2"));
    	setString("mail_addr3", wp.itemStr("mail_addr3"));
    	setString("mail_addr4", wp.itemStr("mail_addr4"));
    	setString("mail_addr5", wp.itemStr("mail_addr5"));
    }else {
    	setString("zip_code", wp.itemStr("db_zip_code"));
    	setString("mail_addr1", wp.itemStr("db_mail_addr1"));
    	setString("mail_addr2", wp.itemStr("db_mail_addr2"));
    	setString("mail_addr3", wp.itemStr("db_mail_addr3"));
    	setString("mail_addr4", wp.itemStr("db_mail_addr4"));
    	setString("mail_addr5", wp.itemStr("db_mail_addr5"));
    }
    setString("proc_status", wp.itemStr("proc_status"));
    setString("package_flag", wp.itemStr("package_flag"));
    setString("package_date", wp.itemStr("package_date"));
    setString("return_note", wp.itemStr("return_note"));
    setString("beg_date", wp.itemStr("beg_date"));
    setString("end_date", wp.itemStr("end_date"));
    setString("mail_no", strMid(wp.itemStr("barcode_num"),0, 6));
    setString("barcode_num", wp.itemStr("barcode_num"));
    setString("digital_flag", wp.itemStr("digital_flag"));
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "dbcm0160");
    setString("mail_addr_flag",wp.itemStr("mail_addr_flag"));
    us_sql += "where 1=1 and hex(rowid) =:rowid and mod_seqno =:mod_seqno";
    setString("rowid", rowid);
    setString("mod_seqno", modSeqno);

    // System.out.println(" us_sql : "+us_sql);
    sqlExec(us_sql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }

//    // insert dbc_return_log
//    busi.SqlPrepare sp = new SqlPrepare();
//    sp.sql2Insert("dbc_return_log");
//    sp.ppstr("card_no", cardNo);
//    sp.ppstr("acct_no", wp.itemStr("acct_no"));
//    sp.ppstr("return_date", wp.itemStr("return_date"));
//    sp.ppstr("group_code", wp.itemStr("group_code"));
//    sp.ppstr("id_p_seqno", wp.itemStr("id_p_seqno"));
//    sp.ppstr("ic_flag", wp.itemStr("ic_flag"));
//    sp.ppstr("reason_code", wp.itemStr("reason_code"));
//    sp.ppstr("return_type", wp.itemStr("return_type"));
//    sp.ppstr("mail_type", wp.itemStr("mail_type"));
//    sp.ppstr("mail_branch", wp.itemStr("mail_branch"));
//    sp.ppstr("mail_date", wp.itemStr("mail_date"));
//    if(wp.itemEmpty("mail_branch")) {
//        sp.ppstr("zip_code", wp.itemStr("zip_code"));
//        sp.ppstr("mail_addr1", wp.itemStr("mail_addr1"));
//        sp.ppstr("mail_addr2", wp.itemStr("mail_addr2"));
//        sp.ppstr("mail_addr3", wp.itemStr("mail_addr3"));
//        sp.ppstr("mail_addr4", wp.itemStr("mail_addr4"));
//        sp.ppstr("mail_addr5", wp.itemStr("mail_addr5"));
//    }else {
//        sp.ppstr("zip_code", wp.itemStr("db_zip_code"));
//        sp.ppstr("mail_addr1", wp.itemStr("db_mail_addr1"));
//        sp.ppstr("mail_addr2", wp.itemStr("db_mail_addr2"));
//        sp.ppstr("mail_addr3", wp.itemStr("db_mail_addr3"));
//        sp.ppstr("mail_addr4", wp.itemStr("db_mail_addr4"));
//        sp.ppstr("mail_addr5", wp.itemStr("db_mail_addr5"));
//    }
//    sp.ppstr("proc_status", wp.itemStr("proc_status"));
//    sp.ppstr("package_flag", wp.itemStr("package_flag"));
//    sp.ppstr("package_date", wp.itemStr("package_date"));
//    sp.ppstr("return_note", wp.itemStr("return_note"));
//    sp.ppstr("beg_date", wp.itemStr("beg_date"));
//    sp.ppstr("end_date", wp.itemStr("end_date"));
//    sp.ppstr("digital_flag", wp.itemStr("digital_flag"));
//    sp.ppstr("return_seqno", wp.itemStr("return_seqno"));
//    sp.ppstr("mail_no", strMid(wp.itemStr("barcode_num"),0, 6));
//    sp.ppstr("barcode_num", wp.itemStr("barcode_num"));
//
//    // sp.addsql(", mod_date =to_char(sysdate,'yyyymmdd')","");
//    // sp.addsql(", mod_time =sysdate","");
//    //
//    sp.addsql(", mod_date ", ", to_char(sysdate,'yyyymmdd') ");
//    sp.addsql(", mod_time ", ", sysdate ");
//
//    sp.ppstr("mod_user", wp.loginUser);
//    // sp.sql2Where("where card_no=?", m_kk_card_no);
//    sqlExec(sp.sqlStmt(), sp.sqlParm());
//    if (sqlRowNum <= 0) {
//      errmsg(this.sqlErrtext);
//    }
//
    return rc;

  }

  @Override
  public int dbDelete() {
    String ds_sql = "";
    cardNo = wp.itemStr("card_no");
    if (empty(cardNo) == false) {
      ds_sql = "delete dbc_return where card_no =:card_no and return_date=:return_date";
      setString("card_no", cardNo);
      setString("return_date", mReturnDate);
      sqlExec(ds_sql);
      if (sqlRowNum <= 0) {
        errmsg(this.sqlErrtext);
      }

      ds_sql = "delete dbc_return_log where card_no =:card_no and return_date=:return_date";
      setString("card_no", cardNo);
      setString("return_date", mReturnDate);
      sqlExec(ds_sql);
      if (sqlRowNum <= 0) {
        errmsg(this.sqlErrtext);
      }
    }

    return rc;
  }

}
