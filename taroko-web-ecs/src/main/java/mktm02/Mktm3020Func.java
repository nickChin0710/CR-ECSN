/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *   DATE        Version    AUTHOR              DESCRIPTION                   *
 * ---------    --------  ----------   -------------------------------------- *
 * 110/12/28    V1.00.00   Machao     全體員工年招攬卡年度總目標參數檔維護       
 * 111/03/22    V1.00.01   machao     页面bug处理 *
 * 112/03/03    V1.00.02   Zuwei Si   覆核=N時可修改，新增失敗，新增修改時，如覆核主管密碼有輸入，check各項明細是否有輸入                                 *    
 ******************************************************************************/
package mktm02;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Mktm3020Func extends FuncEdit {
  String acctyear = "";

  public Mktm3020Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    return 0;
  }

  @Override
  public int dataSelect() {
    return 1;
  }

  @Override
  public int dbInsert() {
    actionInit("A");

    dataCheck();
    if (rc != 1) {
      return rc;
    }

    String tableName = "mkt_year_target";
    strSql = "insert into " + tableName + " " +
            "( acct_year, target_card_cnt, spread_months, crt_user, crt_date, apr_flag, apr_user, apr_date, mod_user, mod_time, mod_pgm, " +
            " mod_seqno, branch_cnt, employee_cnt, acct_type_flag, card_type_flag, group_code_flag, branch_flag ) " +
            "values " +
            "( :ex_acct_year, :target_card_cnt, '0', :crt_user, :crt_date, :apr_flag, " +
            ":apr_user, :apr_date, :mod_user, sysdate, :mod_pgm, '0', " +
            ":branch_cnt, :employee_cnt, :acct_type_flag, :card_type_flag, :group_code_flag, :branch_flag )";
    item2ParmStr("ex_acct_year");
    item2ParmNum("target_card_cnt");
    setString("crt_user", wp.loginUser);
    setString("crt_date", getSysDate());
    item2ParmStr("apr_flag");
    setString("apr_user", wp.itemStr("approval_user"));
    setString("apr_date", getSysDate());
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", wp.modPgm());   
    item2ParmNum("branch_cnt");
    item2ParmNum("employee_cnt");
    item2ParmStr("acct_type_flag");
    item2ParmStr("card_type_flag");
    item2ParmStr("group_code_flag");
    item2ParmStr("branch_flag");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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

    if (wp.itemEmpty("approval_user") && wp.itemEmpty("approval_passwd") && wp.itemEq("apr_flag", "Y")) {
      errmsg("已覆核不可修改");
      return rc;
    } else {
      String tableName = "mkt_year_target";
      strSql = "update " + tableName + " set " +
              "target_card_cnt = :target_card_cnt, spread_months = '0', " +
              "apr_flag = :apr_flag, apr_user = :apr_user, apr_date = :apr_date," +
              "mod_user = :mod_user, mod_time = sysdate, mod_pgm = :mod_pgm, mod_seqno = :mod_seqno, branch_cnt = :branch_cnt, " + 
              "employee_cnt = :employee_cnt, acct_type_flag = :acct_type_flag, card_type_flag = :card_type_flag, " + 
              "group_code_flag = :group_code_flag, branch_flag = :branch_flag " +
              "where " +
              "acct_year = :acct_year and hex(rowid) = :rowid";

//      item2ParmStr("acct_year");
      item2ParmNum("target_card_cnt");
//      setString("crt_user", wp.loginUser);
//      setString("crt_date", getSysDate());
      item2ParmStr("apr_flag");
      setString("apr_user", wp.itemStr("approval_user"));
      setString("apr_date", getSysDate());
      setString("mod_user", wp.loginUser);
//      setString("mod_time", getSysDate());
      setString("mod_pgm", wp.modPgm()); 
      setNumber("mod_seqno", wp.itemNum("mod_seqno") + 1);
      item2ParmNum("branch_cnt");
      item2ParmNum("employee_cnt");
      item2ParmStr("acct_type_flag");
      item2ParmStr("card_type_flag");
      item2ParmStr("group_code_flag");
      item2ParmStr("branch_flag");
      item2ParmStr("acct_year");
      item2ParmStr("rowid");

      sqlExec(strSql);
      if (sqlRowNum <= 0) {
        errmsg(sqlErrtext);
      }
    }

    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    if (rc != 1) {
      return rc;
    }

    strSql = "delete mkt_year_target "
            + "where 1 = 1 and acct_year = :acct_year";
    item2ParmStr("acct_year");
//    item2ParmStr("rowid");
//    item2ParmStr("mod_seqno");

    log(strSql);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("[mkt_year_target] delete error , rowid=" + wp.itemStr("rowid"));
    } else {
      delRelatedDtl();
    }
    return rc;
  }

  /**
    * 刪除相關數據
    */
  public void delRelatedDtl() {
    Object[] param = new Object[] {wp.itemStr("acct_year")};
    if (sqlRowcount("mkt_year_dtl", "acct_year = ? ", param) <= 0) {
      return;
    }

    strSql = "delete mkt_year_dtl "
            + "where 1 = 1 and acct_year = :acct_year";
    item2ParmStr("acct_year");

    log(strSql);
    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("[mkt_year_dtl] delete error , acct_year=" + wp.itemStr("acct_year"));
    }
  }

  @Override
  public void dataCheck() {
    if (ibDelete) {
      return;
    }
    if (this.ibAdd) {
      acctyear = wp.itemStr2("ex_acct_year");
    } else {
    	acctyear = wp.itemStr2("acct_year");
    }

    if (empty(acctyear)) {
      errmsg("年份：不可空白");
      wp.colSet("ex_acct_year_pink", "pink");
    } else {
    	if (this.ibAdd) {
    	    String sql = "select 1 from mkt_year_target where acct_year = ?";
    	    super.sqlSelect(sql, new Object[] {acctyear});
            if(!sqlNotfind) {
            	  errmsg("年度重覆!!請重新輸入"); 
            }
//          } else {
////            wp.colSet("ex_acct_year", " ");
//          }
         // --目標總新卡數
            if (wp.itemEmpty("target_card_cnt")) {
              errmsg("年度總目標新卡數：不可空白");
              wp.colSet("target_card_cnt_pink", "pink ");
            }else if(wp.itemNum("target_card_cnt")<=0) {
              errmsg("目標數要大於>0的值");
              wp.colSet("target_card_cnt_pink", "pink ");
            }

            // --分行目標數 
            if (wp.itemEmpty("branch_cnt")) {
              errmsg("分行員工目標不可空白");
              wp.colSet("branch_cnt_pink", "pink");
            }else if (wp.itemNum("branch_cnt") <= 0) {
              errmsg("目標數要大於>0的值 ");
              wp.colSet("branch_cnt_pink", "pink");
                }   

            // --員工每人卡數  
              if (wp.itemEmpty("employee_cnt")) {
                errmsg("員工每人卡不可空白");
                wp.colSet("employee_cnt_pink", "pink");
              } else if (wp.itemNum("employee_cnt") <= 0) {
                errmsg("目標數要大於>0的值");
                wp.colSet("employee_cnt_pink", "pink");
              } 
          }
    }
    
    
    
    
//    if (wp.getValue("apr_flag").equals("N") & !wp.getValue("acct_type_flag").equals("0") ) {
//    	errmsg("已選全部,不需再指定帳戶類別");
//    }
//    if (wp.getValue("apr_flag").equals("N") & !wp.getValue("group_code_flag").equals("0") ) {
//    	errmsg("已選全部,不需再指定團代");
//    }
//    if (wp.getValue("apr_flag").equals("N") & !wp.getValue("card_type_flag").equals("0") ) {
//    	errmsg("已選全部,不需再指定卡種");
//    }
//    if (wp.getValue("apr_flag").equals("N") & !wp.getValue("branch_flag").equals("0") ) {
//    	errmsg("已選全部,不需再指定分行");
//    }
  }


  public int insertDetl(String dataType) {
    msgOK();

    strSql = "insert into mkt_year_dtl ( acct_year, data_type, data_code1, mod_time, mod_pgm ) " +
            "values ( ?, ?, ?, sysdate, 'Mktm3020' )";

    Object[] param = new Object[] { wp.itemStr("acct_year"), dataType, varsStr("ex_data_code1") };

    sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg("Insert mkt_year_dtl.%s error, program_code = %s", dataType, wp.itemStr("acct_year"));
    }

    return rc;
  }

  public int deleteAllDetl(String dataType) {
    msgOK();

    strSql = "delete mkt_year_dtl where acct_year = ? and data_type = ?";

    setString(wp.itemStr("acct_year"));
    setString(dataType);

    sqlExec(strSql);
    if (sqlRowNum < 0) {
      errmsg("delete mkt_year_dtl error !");
      return rc;
    } else {
      rc = 1;
    }

    return rc;
  }
}
