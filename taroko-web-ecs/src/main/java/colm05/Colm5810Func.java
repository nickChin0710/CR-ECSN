/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-05-06  V1.00.00  Tanwei       updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      * 
******************************************************************************/
package colm05;
/*
 * rskm0810: 整批凍結參數維護
 * */
import busi.FuncEdit;


public class Colm5810Func extends FuncEdit {

  String acctType = "", validDate = "";

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
    String lsMa1 = "", lsMa14 = "", lsMb1 = "", lsMb14 = "", lsMc1 = "", lsMc14 = "";
    if (this.ibAdd) {
      acctType = wp.itemStr("kk_acct_type");
      validDate = wp.itemStr("kk_valid_date");
    } else {
      acctType = wp.itemStr("acct_type");
      validDate = wp.itemStr("valid_date");
    }

    if (empty(acctType)) {
      errmsg("帳戶類別不可空白");
      return;
    }

    if (empty(validDate)) {
      errmsg("生效日期不可空白");
      return;
    }

    if (eqIgno(wp.itemStr("apr_flag"), "Y")) {
      errmsg("已放行，不可修改及刪除，需先請主管維護[取消放行]後再修改。");
      return;
    }

    if (this.ibDelete) {
      if (!empty(wp.itemStr("exec_date")) || !empty(wp.itemStr("b_exec_date"))) {
        errmsg("參數曾執行過, 不可刪除");
        return;
      }
    }

    if (this.ibAdd) {
      if (this.chkStrend(this.getSysDate(), validDate) == -1) {
        errmsg("生效日期不可小於系統日 !");
        return;
      }
    }

    // --Ma 執行方式
    lsMa1 = wp.itemNvl("exec_flag_m1", "N") + wp.itemNvl("b_exec_flag_m1", "N");
    if (eqIgno(lsMa1, "YY") || eqIgno(lsMa1, "NN")) {
      errmsg("Ma檢核必須在一種執行方式中檢核，且不可同時在兩種執行方式檢核");
      return;
    } else {

      if (eqIgno(lsMa1.substring(0, 1), "Y") && wp.itemNum("mcode_value1") == 0) {
        errmsg("Ma檢核之 Mcode不可為0");
        return;
      }

      if (eqIgno(lsMa1.substring(1, 2), "Y") && wp.itemNum("b_mcode_value1") == 0) {
        errmsg("Ma檢核之 Mcode不可為0");
        return;
      }
    }

    lsMa14 = wp.itemNvl("exec_flag_m14", "N") + wp.itemNvl("b_exec_flag_m14", "N");
    if (eqIgno(lsMa14, "YY")) {
      errmsg("Ma[4]檢核 只可在一種執行方式中檢核");
      return;
    }
    if (eqIgno(lsMa14.substring(0, 1), "Y")) {
      if (wp.itemNum("mcode_value14") == 0) {
        errmsg("Ma[4]檢核之 Mcode不可為0");
        return;
      }
      if (empty(wp.itemStr("block_reason14"))) {
        errmsg("Ma[4]檢核之 凍結碼 不可空白");
        return;
      }
    } else if (eqIgno(lsMa14.substring(1, 2), "Y")) {
      if (wp.itemNum("b_mcode_value14") == 0) {
        errmsg("Ma[4]檢核之 Mcode不可為0");
        return;
      }
      if (empty(wp.itemStr("b_block_reason14"))) {
        errmsg("Ma[4]檢核之 凍結碼 不可空白");
        return;
      }
    }

    // --Mb執行方式
    lsMb1 = wp.itemNvl("exec_flag_m2", "N") + wp.itemNvl("b_exec_flag_m2", "N");
    if (eqIgno(lsMb1, "NN") || eqIgno(lsMb1, "YY")) {
      errmsg("Mb檢核必須在一種執行方式中檢核，且不可同時在兩種執行方式檢核");
      return;
    } else {
      if (eqIgno(lsMb1.substring(0, 1), "Y") && wp.itemNum("mcode_value2") == 0) {
        errmsg("Mb檢核之 Mcode不可為0");
        return;
      }

      if (eqIgno(lsMb1.substring(1, 2), "Y") && wp.itemNum("b_mcode_value2") == 0) {
        errmsg("Mb檢核之 Mcode不可為0");
        return;
      }
    }

    lsMb14 = wp.itemNvl("exec_flag_m24", "N") + wp.itemNvl("b_exec_flag_m24", "N");
    if (eqIgno(lsMb14, "YY")) {
      errmsg("Mb[4]檢核 只可在一種執行方式中檢核");
      return;
    }

    if (eqIgno(lsMb14.substring(0, 1), "Y") && empty(wp.itemStr("block_reason24"))) {
      errmsg("Mb[4]檢核之 凍結碼 不可空白");
      return;
    } else if (eqIgno(lsMb14.substring(1, 2), "Y") && empty(wp.itemStr("b_block_reason24"))) {
      errmsg("Mb[4]檢核之 凍結碼 不可空白");
      return;
    }

    // --MC執行方式
    lsMc1 = wp.itemNvl("exec_flag_m3", "N") + wp.itemNvl("b_exec_flag_m3", "N");
    if (eqIgno(lsMc1, "YY") || eqIgno(lsMc1, "NN")) {
      errmsg("Mc檢核必須在一種執行方式中檢核，且不可同時在兩種執行方式檢核");
      return;
    } else {
      if (eqIgno(lsMc1.substring(0, 1), "Y") && wp.itemNum("mcode_value3") == 0) {
        errmsg("Mc檢核之 Mcode不可為0");
        return;
      }
      if (eqIgno(lsMc1.substring(1, 2), "Y") && wp.itemNum("b_mcode_value3") == 0) {
        errmsg("Mc檢核之 Mcode不可為0");
        return;
      }
    }

    lsMc14 = wp.itemNvl("exec_flag_m34", "N") + wp.itemNvl("b_exec_flag_m34", "N");
    if (eqIgno(lsMc14, "YY")) {
      errmsg("Mc[4]檢核 只可在一種執行方式中檢核");
      return;
    }

    if (eqIgno(lsMc14.substring(0, 1), "Y")) {
      if (wp.itemNum("mcode_value34") == 0) {
        errmsg("Mc[4]檢核之 Mcode不可為0");
        return;
      }
      if (empty(wp.itemStr("block_reason34"))) {
        errmsg("Mc[4]檢核之 凍結碼 不可空白");
        return;
      }
    } else if (eqIgno(lsMc14.substring(1, 2), "Y")) {
      if (wp.itemNum("b_mcode_value34") == 0) {
        errmsg("Mc[4]檢核之 Mcode不可為0");
        return;
      }
      if (empty(wp.itemStr("b_block_reason34"))) {
        errmsg("Mc[4]檢核之 凍結碼 不可空白");
        return;
      }
    }

    // --value
    wp.itemSet("b_mcode_value4", wp.itemStr("mcode_value4"));
    wp.itemSet("b_debt_amt4", wp.itemStr("debt_amt4"));

    if (!eqIgno(wp.itemNvl("exec_flag_m1", "N"), "Y")) {
      wp.itemSet("mcode_value1", "0");
      wp.itemSet("debt_amt1", "0");
    }

    if (!eqIgno(wp.itemNvl("exec_flag_m14", "N"), "Y")) {
      wp.itemSet("mcode_value14", "0");
      wp.itemSet("debt_amt14", "0");
      wp.itemSet("block_reason14", "");
    }

    if (!eqIgno(wp.itemNvl("exec_flag_m2", "N"), "Y")) {
      wp.itemSet("mcode_value2", "0");
      wp.itemSet("debt_amt2", "0");
    }

    if (!eqIgno(wp.itemNvl("exec_flag_m24", "N"), "Y")) {
      wp.itemSet("mcode_value24", "0");
      wp.itemSet("debt_amt24", "0");
      wp.itemSet("block_reason24", "");
    }

    if (!eqIgno(wp.itemNvl("exec_flag_m3", "N"), "Y")) {
      wp.itemSet("mcode_value3", "0");
      wp.itemSet("debt_amt3", "0");
    }

    if (!eqIgno(wp.itemNvl("exec_flag_m34", "N"), "Y")) {
      wp.itemSet("mcode_value34", "0");
      wp.itemSet("debt_amt34", "0");
      wp.itemSet("block_reason34", "");
    }

    if (!eqIgno(wp.itemNvl("b_exec_flag_m1", "N"), "Y")) {
      wp.itemSet("b_mcode_value1", "0");
      wp.itemSet("b_debt_amt1", "0");
    }

    if (!eqIgno(wp.itemNvl("b_exec_flag_m14", "N"), "Y")) {
      wp.itemSet("b_mcode_value14", "0");
      wp.itemSet("b_debt_amt14", "0");
      wp.itemSet("b_block_reason14", "");
    }

    if (!eqIgno(wp.itemNvl("b_exec_flag_m2", "N"), "Y")) {
      wp.itemSet("b_mcode_value2", "0");
      wp.itemSet("b_debt_amt2", "0");
    }

    if (!eqIgno(wp.itemNvl("b_exec_flag_m24", "N"), "Y")) {
      wp.itemSet("b_mcode_value24", "0");
      wp.itemSet("b_debt_amt24", "0");
      wp.itemSet("b_block_reason24", "");
    }

    if (!eqIgno(wp.itemNvl("b_exec_flag_m3", "N"), "Y")) {
      wp.itemSet("b_mcode_value3", "0");
      wp.itemSet("b_debt_amt3", "0");
    }

    if (!eqIgno(wp.itemNvl("b_exec_flag_m34", "N"), "Y")) {
      wp.itemSet("b_mcode_value34", "0");
      wp.itemSet("b_debt_amt34", "0");
      wp.itemSet("b_block_reason34", "");
    }

  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = " insert into ptr_blockparam ( " + " param_type ," + " acct_type ," + " valid_date ,"
        + " exec_mode ," + " apr_flag ," + " pause_flag ," + " exec_day ," + " exec_cycle_nday ,"
        + " exec_date ," + " mcode_value1 ," + " debt_amt1 ," + " mcode_value2 ," + " debt_amt2 ,"
        + " mcode_value3 ," + " debt_amt3 ," + " exec_flag_m1 ," + " exec_flag_m2 ,"
        + " exec_flag_m3 ," + " debt_fee3 ," + " exec_flag_m14 ," + " mcode_value14 ,"
        + " debt_amt14 ," + " block_reason14 ," + " exec_flag_m24 ," + " mcode_value24 ,"
        + " debt_amt24 ," + " block_reason24 ," + " exec_flag_m34 ," + " mcode_value34 ,"
        + " debt_amt34 ," + " debt_fee34 ," + " block_reason34 ," + " mod_user ," + " mod_time ,"
        + " mod_pgm ," + " mod_seqno " + " ) values ( " + " '1' ," + " :kk1 ," + " :kk2 ,"
        + " '2' ," + " 'N' ," + " 'N' ," + " 0 ," + " :exec_cycle_nday ," + " :exec_date ,"
        + " :mcode_value1 ," + " :debt_amt1 ," + " :mcode_value2 ," + " :debt_amt2 ,"
        + " :mcode_value3 ," + " :debt_amt3 ," + " :exec_flag_m1 ," + " :exec_flag_m2 ,"
        + " :exec_flag_m3 ," + " :debt_fee3 ," + " :exec_flag_m14 ," + " :mcode_value14 ,"
        + " :debt_amt14 ," + " :block_reason14 ," + " :exec_flag_m24 ," + " :mcode_value24 ,"
        + " :debt_amt24 ," + " :block_reason24 ," + " :exec_flag_m34 ," + " :mcode_value34 ,"
        + " :debt_amt34 ," + " :debt_fee34 ," + " :block_reason34 ," + " :mod_user ," + " sysdate ,"
        + " :mod_pgm ," + " 1 " + " ) ";
    setString("kk1", acctType);
    setString("kk2", validDate);
    item2ParmNum("exec_cycle_nday");
    item2ParmStr("exec_date");
    item2ParmNum("mcode_value1");
    item2ParmNum("debt_amt1");
    item2ParmNum("mcode_value2");
    item2ParmNum("debt_amt2");
    item2ParmNum("mcode_value3");
    item2ParmNum("debt_amt3");
    item2ParmNvl("exec_flag_m1", "N");
    item2ParmNvl("exec_flag_m2", "N");
    item2ParmNvl("exec_flag_m3", "N");
    item2ParmNum("debt_fee3");
    item2ParmNvl("exec_flag_m14", "N");
    item2ParmNum("mcode_value14");
    item2ParmNum("debt_amt14");
    item2ParmNvl("block_reason14", "N");
    item2ParmNvl("exec_flag_m24", "N");
    item2ParmNum("mcode_value24");
    item2ParmNum("debt_amt24");
    item2ParmStr("block_reason24");
    item2ParmNvl("exec_flag_m34", "N");
    item2ParmNum("mcode_value34");
    item2ParmNum("debt_amt34");
    item2ParmNum("debt_fee34");
    item2ParmStr("block_reason34");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "colm5810");

    sqlExec(strSql);
    if (sqlRowNum > 0) {
      insertPtrBlockparam1();
    } else {
      errmsg("insert ptr_blockparam error; " + this.getMsg());
    }
    return rc;
  }

  void insertPtrBlockparam1() {
    msgOK();

    strSql = " insert into ptr_blockparam ( " + " param_type ," + " acct_type ," + " valid_date ,"
        + " exec_mode ," + " apr_flag ," + " pause_flag ," + " exec_day ," + " exec_cycle_nday ,"
        + " exec_date ," + " mcode_value1 ," + " debt_amt1 ," + " mcode_value2 ," + " debt_amt2 ,"
        + " mcode_value3 ," + " debt_amt3 ," + " exec_flag_m1 ," + " exec_flag_m2 ,"
        + " exec_flag_m3 ," + " debt_fee3 ," + " exec_flag_m14 ," + " mcode_value14 ,"
        + " debt_amt14 ," + " block_reason14 ," + " exec_flag_m24 ," + " mcode_value24 ,"
        + " debt_amt24 ," + " block_reason24 ," + " exec_flag_m34 ," + " mcode_value34 ,"
        + " debt_amt34 ," + " debt_fee34 ," + " block_reason34 ," + " mod_user ," + " mod_time ,"
        + " mod_pgm ," + " mod_seqno " + " ) values ( " + " '1' ," + " :kk1 ," + " :kk2 ,"
        + " '1' ," + " 'N' ," + " 'N' ," + " '01' ," + " 0 ," + " :exec_date ," + " :mcode_value1 ,"
        + " :debt_amt1 ," + " :mcode_value2 ," + " :debt_amt2 ," + " :mcode_value3 ,"
        + " :debt_amt3 ," + " :b_exec_flag_m1 ," + " :b_exec_flag_m2 ," + " :b_exec_flag_m3 ,"
        + " :debt_fee3 ," + " :b_exec_flag_m14 ," + " :mcode_value14 ," + " :debt_amt14 ,"
        + " :block_reason14 ," + " :b_exec_flag_m24 ," + " :mcode_value24 ," + " :debt_amt24 ,"
        + " :block_reason24 ," + " :b_exec_flag_m34 ," + " :mcode_value34 ," + " :debt_amt34 ,"
        + " :debt_fee34 ," + " :block_reason34 ," + " :mod_user ," + " sysdate ," + " :mod_pgm ,"
        + " 1 " + " ) ";
    setString("kk1", acctType);
    setString("kk2", validDate);
    item2ParmStr("exec_date", "b_exec_date");
    item2ParmNum("mcode_value1", "b_mcode_value1");
    item2ParmNum("debt_amt1", "b_debt_amt1");
    item2ParmNum("mcode_value2", "b_mcode_value2");
    item2ParmNum("debt_amt2", "b_debt_amt2");
    item2ParmNum("mcode_value3", "b_mcode_value3");
    item2ParmNum("debt_amt3", "b_debt_amt3");
    item2ParmNvl("b_exec_flag_m1", "N");
    item2ParmNvl("b_exec_flag_m2", "N");
    item2ParmNvl("b_exec_flag_m3", "N");
    item2ParmNum("debt_fee3", "b_debt_fee3");
    item2ParmNvl("b_exec_flag_m14", "N");
    item2ParmNum("mcode_value14", "b_mcode_value14");
    item2ParmNum("debt_amt14", "b_debt_amt14");
    item2ParmNvl("block_reason14", "N");
    item2ParmNvl("b_exec_flag_m24", "N");
    item2ParmNum("mcode_value24", "b_mcode_value24");
    item2ParmNum("debt_amt24", "b_debt_amt24");
    item2ParmStr("block_reason24", "b_block_reason24");
    item2ParmNvl("b_exec_flag_m34", "N");
    item2ParmNum("mcode_value34", "b_mcode_value34");
    item2ParmNum("debt_amt34", "b_debt_amt34");
    item2ParmNum("debt_fee34", "b_debt_fee34");
    item2ParmStr("block_reason34", "b_block_reason34");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "colm5810");

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("insert ptr_blockparam.[1] error; " + this.getMsg());
    }
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;
    // --exec_mode : 2 cycle 後 X 天
    strSql = " update ptr_blockparam set "
        // + " exec_day = :exec_day , "
        + " exec_cycle_nday = :exec_cycle_nday , " // 1
        + " exec_date = :exec_date , " + " mcode_value1 = :mcode_value1 , "
        + " debt_amt1 = :debt_amt1 , " + " mcode_value2 = :mcode_value2 , " // 5
        + " debt_amt2 = :debt_amt2 , " + " mcode_value3 = :mcode_value3 , "
        + " debt_amt3 = :debt_amt3 , " + " exec_flag_m1 = :exec_flag_m1 , "
        + " exec_flag_m2 = :exec_flag_m2 , " // 10
        + " exec_flag_m3 = :exec_flag_m3 , " + " debt_fee3 = :debt_fee3 , "
        + " exec_flag_m14 = :exec_flag_m14 , " + " mcode_value14 = :mcode_value14 , "
        + " debt_amt14 = :debt_amt14 , " // 15
        + " block_reason14 = :block_reason14 , " + " exec_flag_m24 = :exec_flag_m24 , "
        + " mcode_value24 = :mcode_value24 , " + " debt_amt24 = :debt_amt24 , "
        + " block_reason24 = :block_reason24 , " // 20
        + " exec_flag_m34 = :exec_flag_m34 , " + " mcode_value34 = :mcode_value34 , "
        + " debt_amt34 = :debt_amt34 , " + " debt_fee34 = :debt_fee34 , "
        + " block_reason34 = :block_reason34 , " // 25
        + " mod_user = :mod_user , " + " mod_time = sysdate , " + " mod_pgm = :mod_pgm , "
        + " mod_seqno = nvl(mod_seqno,0)+1 " // 29
        + " where 1=1 " + " and param_type = '1' " + " and exec_mode = '2' "
        + " and acct_type =:kk1  " + " and valid_date =:kk2 ";

    item2ParmNum("exec_cycle_nday"); // 1
    item2ParmStr("exec_date");
    item2ParmNum("mcode_value1");
    item2ParmNum("debt_amt1");
    item2ParmNum("mcode_value2"); // 5
    item2ParmNum("debt_amt2");
    item2ParmNum("mcode_value3");
    item2ParmNum("debt_amt3");
    item2ParmNvl("exec_flag_m1", "N");
    item2ParmNvl("exec_flag_m2", "N"); // 10
    item2ParmNvl("exec_flag_m3", "N");
    item2ParmNum("debt_fee3");
    item2ParmNvl("exec_flag_m14", "N");
    item2ParmNum("mcode_value14");
    item2ParmNum("debt_amt14"); // 15
    item2ParmStr("block_reason14");
    item2ParmNvl("exec_flag_m24", "N");
    item2ParmNum("mcode_value24");
    item2ParmNum("debt_amt24");
    item2ParmStr("block_reason24"); // 20
    item2ParmNvl("exec_flag_m34", "N");
    item2ParmNum("mcode_value34");
    item2ParmNum("debt_amt34");
    item2ParmNum("debt_fee34");
    item2ParmStr("block_reason34"); // 25
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "colm5810"); // 27
    setString("kk1", acctType);
    setString("kk2", validDate);

    sqlExec(strSql);


    if (sqlRowNum > 0) {
      if (checkBlockParam1() == false) {
        insertPtrBlockparam1();
      } else {
        updatePtrBlockparam1();
      }
    } else {
      errmsg("update ptr_blockparam.[2] error; " + this.getMsg());
    }

    return rc;
  }

  boolean checkBlockParam1() {
    String sql1 = " select " + " count(*) as db_cnt " + " from ptr_blockparam " + " where 1=1 "
        + " and param_type = '1' " + " and acct_type = ? " + " and valid_date = ? "
        + " and exec_mode = '1' ";
    sqlSelect(sql1, new Object[] {acctType, validDate});
    if (sqlRowNum <= 0 || colNum("db_cnt") == 0)
      return false;
    return true;
  }

  void updatePtrBlockparam1() {
    msgOK();
    // --exec_mode : 1.每月固定日 01
    strSql = " update ptr_blockparam set " + " exec_date = :exec_date , "
        + " mcode_value1 = :mcode_value1 , " + " debt_amt1 = :debt_amt1 , "
        + " mcode_value2 = :mcode_value2 , " + " debt_amt2 = :debt_amt2 , "
        + " mcode_value3 = :mcode_value3 , " + " debt_amt3 = :debt_amt3 , "
        + " exec_flag_m1 = :exec_flag_m1 , " + " exec_flag_m2 = :exec_flag_m2 , "
        + " exec_flag_m3 = :exec_flag_m3 , " + " debt_fee3 = :debt_fee3 , "
        + " exec_flag_m14 = :exec_flag_m14 , " + " mcode_value14 = :mcode_value14 , "
        + " debt_amt14 = :debt_amt14 , " + " block_reason14 = :block_reason14 , "
        + " exec_flag_m24 = :exec_flag_m24 , " + " mcode_value24 = :mcode_value24 , "
        + " debt_amt24 = :debt_amt24 , " + " block_reason24 = :block_reason24 , "
        + " exec_flag_m34 = :exec_flag_m34 , " + " mcode_value34 = :mcode_value34 , "
        + " debt_amt34 = :debt_amt34 , " + " debt_fee34 = :debt_fee34 , "
        + " block_reason34 = :block_reason34 , " + " mod_user = :mod_user , "
        + " mod_time = sysdate , " + " mod_pgm = :mod_pgm , " + " mod_seqno = nvl(mod_seqno,0)+1 "
        + " where 1=1 " + " and param_type = '1' " + " and exec_mode = '1' "
        + " and acct_type =:kk1  " + " and valid_date =:kk2 ";

    item2ParmStr("exec_date", "b_exec_date");
    item2ParmNum("mcode_value1", "b_mcode_value1");
    item2ParmNum("debt_amt1", "b_debt_amt1");
    item2ParmNum("mcode_value2", "b_mcode_value2");
    item2ParmNum("debt_amt2", "b_debt_amt2");
    item2ParmNum("mcode_value3", "b_mcode_value3");
    item2ParmNum("debt_amt3", "b_debt_amt3");
    item2ParmNvl("exec_flag_m1", "b_exec_flag_m1", "N");
    item2ParmNvl("exec_flag_m2", "b_exec_flag_m2", "N");
    item2ParmNvl("exec_flag_m3", "b_exec_flag_m3", "N");
    item2ParmNum("debt_fee3", "b_debt_fee3");
    item2ParmNvl("exec_flag_m14", "b_exec_flag_m14", "N");
    item2ParmNum("mcode_value14", "b_mcode_value14");
    item2ParmNum("debt_amt14", "b_debt_amt14");
    item2ParmStr("block_reason14", "b_block_reason14");
    item2ParmNvl("exec_flag_m24", "b_exec_flag_m24", "N");
    item2ParmNum("mcode_value24", "b_mcode_value24");
    item2ParmNum("debt_amt24", "b_debt_amt24");
    item2ParmStr("block_reason24", "b_block_reason24");
    item2ParmNvl("exec_flag_m34", "b_exec_flag_m34", "N");
    item2ParmNum("mcode_value34", "b_mcode_value34");
    item2ParmNum("debt_amt34", "b_debt_amt34");
    item2ParmNum("debt_fee34", "b_debt_fee34");
    item2ParmStr("block_reason34", "b_block_reason34");
    setString("mod_user", wp.loginUser);
    setString("mod_pgm", "colm5810");
    setString("kk1", acctType);
    setString("kk2", validDate);

    sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg("update ptr_blockparam.[1] error; " + this.getMsg());
    }
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete ptr_blockparam" + " where param_type ='1'" + " and exec_mode in ('1','2')"
    	+ " and acct_type =:acct_type and valid_date =:valid_date and nvl(mod_seqno,0) =:mod_seqno ";
    
    setString("acct_type",acctType);
    setString("valid_date",validDate);
    setString("mod_seqno",wp.modSeqno());

    this.sqlExec(strSql);
    if (sqlRowNum == 0) {
      errmsg("delete ptr_blockparam error; " + this.getMsg());
    }

    return rc;
  }

}
