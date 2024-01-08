/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-04-20  V1.00.01  YangFang   updated for project coding standard        *
* 109-06-01  V1.00.02  Alex           dataCheck fix								 *
* 109-12-23  V1.00.03  Justin         parameterize sql
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
******************************************************************************/
package ptrm02;

import busi.FuncEdit;
import taroko.base.CommSqlStr;
import taroko.com.TarokoCommon;

public class Ptrm0312Func extends FuncEdit {
  String acctType = "", classCode = "", aprFlag = "";

  public Ptrm0312Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      acctType = wp.itemStr("kk_acct_type");
    } else {
      acctType = wp.itemStr("acct_type");
    }
    if (this.ibAdd) {
      classCode = wp.itemStr("kk_class_code");
    } else {
      classCode = wp.itemStr("class_code");
    }
    aprFlag = wp.itemNvl("apr_flag", "N");

    if (empty(acctType)) {
      errmsg("帳戶類別: 不可空白");
      return;
    }

    if (empty(classCode)) {
      errmsg("卡人等級: 不可空白");
      return;
    }

    
    // --
    // if (ib_delete && eq_any(kk3,"Y")) {
    // errmsg("主管已覆核, 不可刪除");
    // return;
    // }
    
    if(ibAdd || ibUpdate) {
        // ----
        if (commString.pos(",M,N,J", classCode) > 0) {
          errmsg("卡人等級 不可為M,N,J");
          return;
        }

        // --
        if (wp.itemEmpty("card_year_cond1") && wp.itemEmpty("card_year_cond2")
            && wp.itemEmpty("mcode_cond1") && wp.itemEmpty("curr_mcode_cond1")
            && wp.itemEmpty("rc_rate_cond1") && wp.itemEmpty("rc_rate_cond2")
            && wp.itemEmpty("credit_limit_cond1") && wp.itemEmpty("credit_limit_cond2")
            && wp.itemEmpty("limit_use_cond1") && wp.itemEmpty("limit_use_cond2")
            && wp.itemEmpty("dest_amt_cond1") && wp.itemEmpty("dest_amt_cond2")
            && wp.itemEmpty("pay_ratio_cond1") && wp.itemEmpty("pay_ratio_cond2")
            && wp.itemEmpty("mcc_code_x6") && wp.itemEmpty("pd_rating_cond1")
            && wp.itemEmpty("pd_rating_mm2") && wp.itemEmpty("vip_code_cond")) {
          errmsg("所有條件不可均不設定");
          return;
        }

        // --持卡年限

        int[] liCond = new int[2];
        double[] ldCond = new double[2];
        liCond[0] = (int) wp.itemNum("card_year_cond1");
        liCond[1] = (int) wp.itemNum("card_year_cond2");
        ldCond[0] = wp.itemNum("card_year_x1");
        ldCond[1] = wp.itemNum("card_year_x2");
        if (wf_check_cond(liCond, ldCond) == false) {
          errmsg("持卡年限 條件設定錯誤");
          return;
        }

        // --N 個月內有 M-code-
        if (wp.itemNum("mcode_n1") < 0 || wp.itemNum("mcode_n1") > 24) {
          errmsg("b.M-Code月數, 須 0 -- 24");
          return;
        }

        liCond[0] = (int) wp.itemNum("mcode_cond1");
        liCond[1] = 0;
        ldCond[0] = wp.itemNum("mcode_n2");
        ldCond[1] = 0;

        if (wf_check_cond(liCond, ldCond) == false) {
          errmsg("N 個月內有M-code 條件設定錯誤");
          return;
        }

        if (wp.itemNum("mcode_n1") == 0 && ldCond[0] != 0) {
          errmsg("N 個月內有M-code 條件設定錯誤~");
          return;
        }

        if (wp.itemNum("mcode_n1") != 0 && ldCond[0] == 0) {
          errmsg("N 個月內有M-code 條件設定錯誤");
          return;
        }
        // -最近 M-code,且結欠本金-
        liCond[0] = (int) wp.itemNum("curr_mcode_cond1");
        liCond[1] = 0;
        ldCond[0] = wp.itemNum("curr_mcode_n3");
        ldCond[1] = 0;

        if (wf_check_cond(liCond, ldCond) == false) {
          errmsg("最近 M-code 條件設定錯誤");
          return;
        }
        ldCond[1] = wp.itemNum("curr_mcode_amt");
        if (liCond[0] == 0 && ldCond[1] != 0) {
          errmsg("最近 M-code 條件設定錯誤(本金結欠)");
          return;
        }

        // -RC動用率-
        liCond[0] = (int) wp.itemNum("rc_rate_cond1");
        liCond[1] = (int) wp.itemNum("rc_rate_cond2");
        ldCond[0] = wp.itemNum("rc_rate_n4");
        ldCond[1] = wp.itemNum("rc_rate_n5");

        if (wf_check_cond(liCond, ldCond) == false) {
          errmsg("RC動用率 條件設定錯誤");
          return;
        }

        // -信用額度-
        liCond[0] = (int) wp.itemNum("credit_limit_cond1");
        liCond[1] = (int) wp.itemNum("credit_limit_cond2");
        ldCond[0] = wp.itemNum("credit_limit_n6");
        ldCond[1] = wp.itemNum("credit_limit_n7");

        if (wf_check_cond(liCond, ldCond) == false) {
          errmsg("信用額度 條件設定錯誤");
          return;
        }

        // -平均額度動用率-
        if (wp.itemNum("limit_use_n8") < 0 || wp.itemNum("limit_use_n8") > 13) {
          errmsg("f.平均額度動用率 月數 須 0~~13");
          return;
        }

        liCond[0] = (int) wp.itemNum("limit_use_cond1");
        liCond[1] = (int) wp.itemNum("limit_use_cond2");
        ldCond[0] = wp.itemNum("limit_use_n9");
        ldCond[1] = wp.itemNum("limit_use_n10");
        if (wf_check_cond(liCond, ldCond) == false) {
          errmsg("平均額度動用率 條件設定錯誤");
          return;
        }

        if (wp.itemNum("limit_use_n8") == 0 && (liCond[0] != 0 || liCond[1] != 0)) {
          errmsg("平均額度動用率 條件設定錯誤");
          return;
        }

        if (wp.itemNum("limit_use_n8") != 0 && (liCond[0] == 0 && liCond[1] == 0)) {
          errmsg("平均額度動用率 條件設定錯誤");
          return;
        }

        // -累積消費-
        if ((wp.itemNum("dest_amt_n11") < 0 || wp.itemNum("dest_amt_n11") > 13)
            || wp.itemNum("dest_amt_n11e") < 0 || wp.itemNum("dest_amt_n11e") > 13) {
          errmsg("累積消費: 月數 須 0~~13");
          return;
        }

        if ((wp.itemNum("dest_amt_n11") == 0 && wp.itemNum("dest_amt_n11e") != 0)
            || (wp.itemNum("dest_amt_n11") != 0 && wp.itemNum("dest_amt_n11e") == 0)) {
          errmsg("累積消費: 起迄月數 須同為0 or 大於0");
          return;
        } else {
          if (wp.itemNum("dest_amt_n11") > wp.itemNum("dest_amt_n11e")) {
            errmsg("累積消費: 月數 起不可大於迄");
            return;
          }
        }

        liCond[0] = (int) wp.itemNum("dest_amt_cond1");
        liCond[1] = (int) wp.itemNum("dest_amt_cond2");
        ldCond[0] = wp.itemNum("dest_amt_n12");
        ldCond[1] = wp.itemNum("dest_amt_n13");

        if (wf_check_cond(liCond, ldCond) == false) {
          errmsg("累積消費 條件設定錯誤");
          return;
        }

        if ((wp.itemNum("dest_amt_n11") == 0 && wp.itemNum("dest_amt_n11e") == 0)
            && (liCond[0] != 0 || liCond[1] != 0)) {
          errmsg("累積消費 條件設定錯誤");
          return;
        }

        if ((wp.itemNum("dest_amt_n11") != 0 && wp.itemNum("dest_amt_n11e") != 0)
            && (liCond[0] == 0 && liCond[1] == 0)) {
          errmsg("累積消費 條件設定錯誤");
          return;
        }

        // -Payment Ratio-
        if (wp.itemNum("pay_ratio_x3") < 0 || wp.itemNum("pay_ratio_x3") > 13) {
          errmsg("h.Payment Ratio 月數 須 0~13");
          return;
        }

        liCond[0] = (int) wp.itemNum("pay_ratio_cond1");
        liCond[1] = (int) wp.itemNum("pay_ratio_cond2");
        ldCond[0] = wp.itemNum("pay_ratio_x4");
        ldCond[1] = wp.itemNum("pay_ratio_x5");

        if (wf_check_cond(liCond, ldCond) == false) {
          errmsg("Payment Ratio 條件設定錯誤");
          return;
        }

        if (wp.itemNum("pay_ratio_x3") == 0 && (liCond[0] != 0 || liCond[1] != 0)) {
          errmsg("Payment Ratio 條件設定錯誤");
          return;
        }

        if (wp.itemNum("pay_ratio_x3") != 0 && (liCond[0] == 0 && liCond[1] == 0)) {
          errmsg("Payment Ratio 條件設定錯誤");
          return;
        }

        // -近 X6 月曾高風險交易之 MCC Code-
        if (wp.itemNum("mcc_code_x6") < 0 || wp.itemNum("mcc_code_x6") > 13) {
          errmsg("i.高風險交易 月數 須 0~~13");
          return;
        }
    }
    


    if (this.isAdd()) {
        return;
    }

    String ls_where = "";
    sqlWhere = " where acct_type= ? and class_code= ? and nvl(mod_seqno,0) = ? ";
    if (eqIgno(aprFlag, "Y")) {
      ls_where = sqlWhere + " and apr_flag='Y'";
    } else {
      ls_where = sqlWhere + " and nvl(apr_flag,'N')<>'Y'";
    }

    if (this.isOtherModify("ptr_class_code2", ls_where, new Object[] {acctType, classCode, wp.modSeqno()})) {
      return;
    }
  }

  boolean wf_check_cond(int[] aCond, double[] bCond) {
    int liCond1 = 0, liCond2 = 0;
    double ldCond1 = 0, ldCond2 = 0;

    liCond1 = aCond[0];
    liCond2 = aCond[1];

    ldCond1 = bCond[0];
    ldCond2 = bCond[1];

    if (liCond1 == 0 && ldCond1 != 0)
      return false;
    if (liCond2 == 0 && ldCond2 != 0)
      return false;

    if (liCond1 == 0 || liCond2 == 0)
      return true;

    if (ldCond1 >= ldCond2)
      return false;

    return true;
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    insertPtrClassCode2();

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    deletePtrClassCode2();
    insertPtrClassCode2();
    if (rc != 1)
      return rc;

    // -copy-detl-data-
    this.sqlWhere =" where nvl(apr_flag,'x')<>'Y'" ;
    if (!wp.itemEmpty("acct_type")) {
    	this.sqlWhere += " and acct_type = ? ";
    	setString(wp.itemStr("acct_type"));
	}
	if (!wp.itemEmpty("class_code")) {
		this.sqlWhere += " and class_code = ? ";
    	setString(wp.itemStr("class_code"));
	}

    if (this.sqlRowcount("ptr_class_code_dtl", sqlWhere) > 0)
      return rc;
    
    String lsSQL = "insert into ptr_class_code_dtl " + " select acct_type,class_code,data_type, "
        + " data_value,data_value2,data_value3,'N' " + " from ptr_class_code_dtl"
        + " where nvl(apr_flag,'x')='Y'" ;
        if (!wp.itemEmpty("acct_type")) {
        	lsSQL += " and acct_type = ? ";
        	setString(wp.itemStr("acct_type"));
    	}
    	if (!wp.itemEmpty("class_code")) {
    		lsSQL += " and class_code = ? ";
        	setString(wp.itemStr("class_code"));
    	}

    this.sqlExec(lsSQL);
    if (rc == -1)
      return rc;
    else
      rc = 1;
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete ptr_class_code2 " + " where 1=1" ;
    if (!empty(acctType)) {
    	strSql += " and acct_type = ? ";
    	setString(acctType);
	}
    if (!empty(classCode)) {
    	strSql += " and class_code = ? ";
    	setString(classCode);
	}
    if (!empty(aprFlag)) {
    	strSql += " and apr_flag = ? ";
    	setString(aprFlag);
	}

    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete ptr_class_code2 error; " + sqlErrtext);
      return rc;
    }
    // --
    strSql = "delete ptr_class_code_dtl " + " where 1=1" ;
    if (!empty(acctType)) {
    	strSql += " and acct_type = ? ";
    	setString(acctType);
	}
    if (!empty(classCode)) {
    	strSql += " and class_code = ? ";
    	setString(classCode);
	}
    if (!empty(aprFlag)) {
    	strSql += " and apr_flag = ? ";
    	setString(aprFlag);
	}

    sqlExec(strSql);
    if (rc == -1) {
      errmsg("delete ptr_class_code_dtl error; " + sqlErrtext);
    } else
      rc = 1;
    return rc;
  }

  void insertPtrClassCode2() {
    strSql = "insert into ptr_class_code2 (" + " acct_type, "// 1
        + " class_code, " + " cond_type, " + " check_seqno, " + " card_year_x1, "
        + " card_year_cond1, " + " card_year_x2, " + " card_year_cond2, " + " mcode_n1, "
        + " mcode_cond1, "// 10
        + " mcode_n2, " + " curr_mcode_cond1, " + " curr_mcode_n3, " + " curr_mcode_cond2, "
        + " curr_mcode_amt, " + " rc_rate_cond1, " + " rc_rate_n4, " + " rc_rate_cond2, "
        + " rc_rate_n5, " + " credit_limit_cond1, "// 20
        + " credit_limit_n6, " + " credit_limit_cond2, " + " credit_limit_n7, " + " limit_use_n8, "
        + " limit_use_cond1, " + " limit_use_n9, " + " limit_use_cond2, " + " limit_use_n10, "
        + " dest_amt_n11, " + " dest_amt_cond1, "// 30
        + " dest_amt_n12, " + " dest_amt_cond2, " + " dest_amt_n13, " + " pay_ratio_x3, "
        + " pay_ratio_cond1, " + " pay_ratio_x4, " + " pay_ratio_cond2, " + " pay_ratio_x5, "
        + " mcc_code_x6 , " + " crt_user, "// 40
        + " crt_date, " + " apr_user, " + " apr_date, " + " apr_flag, " + " pd_rating_cond1, "
        + " pd_rating_cond2, " + " pd_rating_mm2, " + " vip_code_cond, " + " dest_amt_n11e"// 49
        + ", mod_time, mod_user, mod_pgm, mod_seqno" + " ) values (" + " ?,?,?,?,?,?,?,?,?,?"
        + ",?,?,?,?,?,?,?,?,?,?" + ",?,?,?,?,?,?,?,?,?,?" + ",?,?,?,?,?,?,?,?,?,?" // 40.crt_user
        + ",?,?,?,?,?,?,?,?,?" + ",sysdate,?,?,1" + " )";
    Object[] param = new Object[] {acctType// 1
        , classCode, wp.itemStr("cond_type"), wp.itemNum("check_seqno"), wp.itemNum("card_year_x1"),
        wp.itemNum("card_year_cond1"), wp.itemNum("card_year_x2"), wp.itemNum("card_year_cond2"),
        wp.itemNum("mcode_n1"), wp.itemNum("mcode_cond1")// 10
        , wp.itemNum("mcode_n2"), wp.itemNum("curr_mcode_cond1"), wp.itemNum("curr_mcode_n3"),
        wp.itemNum("curr_mcode_cond2"), wp.itemNum("curr_mcode_amt"), wp.itemNum("rc_rate_cond1"),
        wp.itemNum("rc_rate_n4"), wp.itemNum("rc_rate_cond2"), wp.itemNum("rc_rate_n5"),
        wp.itemNum("credit_limit_cond1")// 20
        , wp.itemNum("credit_limit_n6"), wp.itemNum("credit_limit_cond2"),
        wp.itemNum("credit_limit_n7"), wp.itemNum("limit_use_n8"), wp.itemNum("limit_use_cond1"),
        wp.itemNum("limit_use_n9"), wp.itemNum("limit_use_cond2"), wp.itemNum("limit_use_n10"),
        wp.itemNum("dest_amt_n11"), wp.itemNum("dest_amt_cond1")// 30
        , wp.itemNum("dest_amt_n12"), wp.itemNum("dest_amt_cond2"), wp.itemNum("dest_amt_n13"),
        wp.itemNum("pay_ratio_x3"), wp.itemNum("pay_ratio_cond1"), wp.itemNum("pay_ratio_x4"),
        wp.itemNum("pay_ratio_cond2"), wp.itemNum("pay_ratio_x5"), wp.itemNum("mcc_code_x6"),
        wp.itemStr2("crt_user") // 40
        , wp.itemStr("crt_date"), "", "", "N", wp.itemStr("pd_rating_cond1"),
        wp.itemStr("pd_rating_cond2"), wp.itemNum("pd_rating_mm2"), wp.itemStr("vip_code_cond"),
        wp.itemNum("dest_amt_n11e")// 49
        , wp.loginUser, wp.itemStr("mod_pgm")};
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg("Insert PTR_CLASS_CODE2.N error; " + sqlErrtext);
    }
    return;
  }

  void deletePtrClassCode2() {
    strSql = "delete ptr_class_code2 " + sqlWhere + " and apr_flag<>'Y'";
    rc = sqlExec(strSql, new Object[] {acctType, classCode, wp.modSeqno()});
    if (sqlRowNum < 0) {
      errmsg("delete PTR_CLASS_CODE2 error; " + this.sqlErrtext);
    }
  }

  public int dbDeleteDetl() {
    msgOK();
    String lsType = wp.itemStr("data_type");

    strSql = "Delete ptr_class_code_dtl" + " where data_type =:data_type"
        + " and acct_type =:acct_type " + " and class_code =:class_code " + " and apr_flag<>'Y'";
    item2ParmStr("acct_type");
    item2ParmStr("class_code");
    setString("data_type", lsType);

    sqlExec(strSql);
    if (rc == -1) {
      errmsg("Delete ptr_class_code_dtl." + lsType + " err; " + getMsg());
      return -1;
    }
    rc = 1;
    return rc;
  }

  // public int dbDelete_mcc() {
  // msgOK();
  // is_sql ="Delete ptr_class_code_dtl"
  // +" where data_type ='MCC-CODE'"
  // +" and acct_type =:acct_type "
  // +" and class_code =:class_code "
  // +" and apr_flag<>'Y'";
  // item2Parm_ss("acct_type");
  // item2Parm_ss("class_code");
  //
  // sqlExec(is_sql);
  // if (sql_nrow<0) {
  // errmsg("Delete ptr_class_code_dtl.MCC-CODE err; "+getMsg());
  // rc =-1;
  // }
  // else rc =1;
  //
  // return rc;
  // }

  public int dbInsertDetl() {
    msgOK();
    String lsType = wp.itemStr("data_type");

    strSql = "insert into ptr_class_code_dtl (" + " data_type, " // 1
        + " acct_type, " + " class_code, " + " apr_flag," // 4
        + " data_value, " + " data_value2, " + " data_value3 " + " ) values ("
        + " :data_type, :acct_type, :class_code, 'N'" + " ,:data_value, :data_value2, :data_value3"
        + " )";
    setString("data_type", lsType); // 1
    item2ParmStr("acct_type");
    item2ParmStr("class_code");
    var2ParmStr("data_value");
    var2ParmStr("data_value2");
    var2ParmStr("data_value3");

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert ptr_class_code_dtl." + lsType + " error; " + getMsg());
    }
    return rc;
  }

  public int updatePtrSysParm() {

    strSql = " update ptr_sys_parm set " + " wf_value6 =:newcard_day "
        + " where wf_parm='w_ptrm0312' " + " and wf_key ='NEWCARD_DAY' ";

    item2ParmStr("newcard_day", "ex_newcard_day");

    sqlExec(strSql);

    if (sqlRowNum <= 0) {
      errmsg("update ptr_sys_parm error !");
    }

    return rc;
  }

  public int insertDetl() {
    msgOK();
    String lsType = wp.itemStr("data_type");

    strSql = "insert into ptr_class_code_dtl (" + " data_type, " // 1
        + " acct_type, " + " class_code, " + " apr_flag," // 4
        + " data_value, " + " data_value2, " + " data_value3 " + " ) values ("
        + " :data_type, :acct_type, :class_code, 'N'" + " ,:data_value, :data_value2, :data_value3"
        + " )";
    setString("data_type", lsType); // 1
    item2ParmStr("acct_type");
    item2ParmStr("class_code");
    setString("data_value", wp.itemStr2("ex_data_code"));
    setString("data_value2", wp.itemStr2("ex_data_code2"));
    setString("data_value3", wp.itemStr2("ex_data_code3"));

    this.sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("Insert ptr_class_code_dtl." + lsType + " error; " + getMsg());
    }
    return rc;
  }

  public int deleteDetl() {
    msgOK();
    String ls_type = wp.itemStr("data_type");

    strSql = "Delete ptr_class_code_dtl" + " where data_type =:data_type"
        + " and acct_type =:acct_type " + " and class_code =:class_code "
        + " and data_value =:data_value " + " and data_value2 =:data_value2 "
        + " and data_value3 =:data_value3 " + " and apr_flag<>'Y'";
    item2ParmStr("acct_type");
    item2ParmStr("class_code");
    setString("data_type", ls_type);
    var2ParmStr("data_value");
    var2ParmStr("data_value2");
    var2ParmStr("data_value3");


    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("delete ptr_class_code error ");
    }
    return rc;
  }

}
