package ccam01;
/** 單筆開卡(card_open_single)
 * 2019-0719   JH    call-autoAuth.outgoing
 * 19-0611: JH    p_seqno >>acno_p_xxx
 * V.2018-0506-JH
 * 109-04-20 V1.00.00  Zhenwu Zhu       updated for project coding standard
 * 109-06-10 V1.00.01  Wilson           ark Neg、解除mark update_crd_card
 * 109-07-20 V1.00.02  Wilson           activate_date <> '' 不可重複開卡 
 * 109-07-21 V1.00.03  Wilson           開卡不insert cca_outgoing  
 * 109-01-04  V1.00.04   shiyuqi       修改无意义命名
* 110-01-05  V1.00.05  Tanwei     zzDate,zzStr,zzComm,zzCurr變量更改         *  *  
* 111-03-23  V1.00.06  Ryan       拿掉 combo卡: 不可做開卡   檢核    *  *  
* 111-04-07  V1.00.07  Justin           check whether the card was open through activate_date and old_activate_date
 * */
import busi.FuncAction;
import busi.func.OutgoingOppo;

public class Ccam2080Func extends FuncAction {
  taroko.base.CommDate commDate = new taroko.base.CommDate();
  String cardNo = "", kk2 = "";

  @Override
  public void dataCheck() {
    cardNo = wp.itemStr("card_no");

    if (wp.itemEmpty("ex_last_card")) {
      errmsg("卡號後四碼:不可空白");
      return;
    }
    String cardNo1 = commString.right(cardNo, 4);
    if (wp.itemEq("ex_last_card", cardNo1) == false) {
      errmsg("卡號後四碼: 輸入錯誤");
      return;
    }

    if (wp.itemEq("debit_flag", "Y")) {
      errmsg("DEBIT CARD不可做開卡");
      return;
    }


    if (wp.itemEq("kk_new_old", "N")) { // --開新卡
//    if (!wp.itemEmpty("new_open_date")) {
      if (!wp.itemEmpty("activate_date")) { // act_date
        errmsg("此卡已開過卡");
        return;
      }

      if (commDate.sysComp(wp.itemStr("new_end_date")) > 0) {
        errmsg("過期卡!!!");
        return;
      }

    } else if (wp.itemEq("kk_new_old", "O")) { // --開舊卡
//    if (!wp.itemEmpty("old_open_date")) {
      if (!wp.itemEmpty("old_activate_date")) { // old_act_date
        errmsg("此卡已開過卡");
        return;
      }

      if (commDate.sysComp(wp.itemStr("old_end_date")) > 0) {
        errmsg("過期卡!!!");
        return;
      }

    }

    selectCrdCard();
    if (rc != 1)
      return;

    if (colNeq("A.current_code", "0")) {
      errmsg("該卡已掛失, 不可開卡!");
      return;
    }

//    if (!colEq("A.combo_indicator", "N")) {
//      errmsg("combo卡: 不可做開卡");
//      return;
//    }

    if (!colEq("A.activate_date", "")) {
        errmsg("該卡已開卡, 不可重複開卡");
        return;
      }    

  }

  void selectCrdCard() {
    strSql =
        "select current_code, sup_flag, uf_nvl(combo_indicator,'N') as combo_indicator, activate_flag"
            + ", activate_date, new_beg_date, new_end_date, old_beg_date, old_end_date" + ", bin_type"
            + ", mod_seqno" + " from crd_card" + " where card_no =?";
    setString2(1, cardNo);
    daoTid = "A.";
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      sqlErr("crd_card.Select");
      return;
    }
  }

  @Override
  public int dbInsert() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dbUpdate() {
    // actionInit("U");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
//    if (wfOutgoing() == false) {
//      return rc;
//    }

     updateCrdCard();
    // if (rc==1)
    updateCardOpen();

    if (rc == 1)
      insertOnbat2ecs();

    // -call CrdG001開卡-

    return rc;
  }

  boolean wfOutgoing() {
    // 傳檔至NEG失敗! [MSG_TYPE="+gs_msg_type+",RESP_CODE="+gs_resp_code+"]")
    OutgoingOppo outgo = new OutgoingOppo();
    outgo.setConn(wp);
    String lsBinType = colStr("A.bin_type");
    if (outgo.openNegId(cardNo, "3", "", "", lsBinType) != 1) {
      errmsg(outgo.getMsg());
      return false;
    }
//    if (outgo.respOkNegId("3") == false) {
//      errmsg("Neg error:" + outgo.strCallStatus);
//      return false;
//    }

    return true;
  }

  @Override
  public int dbDelete() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int dataProc() {
    // TODO Auto-generated method stub
    return 0;
  }

   public int updateCrdCard() {
       if (wp.itemEq("kk_new_old", "O")) {
    	   strSql = " update crd_card set "
             + " old_activate_flag ='2' ,"
             + " old_activate_type ='O',"
             + " old_activate_date =to_char(sysdate,'yyyymmdd') ,"
             + commSqlStr.setModxxx(modUser, modPgm)
             + " where card_no=:k_card_no ";
  
    	   setString2("k_card_no", cardNo);
       }
       else {
    	   strSql = " update crd_card set "
             + " activate_flag ='2' ,"
             + " activate_type ='O',"
             + " activate_date =to_char(sysdate,'yyyymmdd'),"
             + commSqlStr.setModxxx(modUser, modPgm)
             + " where card_no=:k_card_no ";
  
    	   setString2("k_card_no",cardNo);
       }
  
       sqlExec(strSql);
       if (sqlRowNum <= 0) {
       errmsg("開卡失敗 , kk[%s]; "+this.sqlErrtext, cardNo);
       }
  
       return rc;
   }

  boolean selectCcaCardOpen() {
    cardNo = wp.itemStr("card_no");
    if (wp.itemEq("kk_new_old", "O")) {
      kk2 = wp.itemStr("old_end_date");
    } else if (wp.itemEq("kk_new_old", "N")) {
      kk2 = wp.itemStr("new_end_date");
    }
    String sql1 = "select count(*) as db_cnt " + " from cca_card_open" + " where card_no =? "
        + " and new_end_date =? ";

    sqlSelect(sql1, new Object[] {cardNo, kk2});

    if (colNum("db_cnt") > 0)
      return true;

    return false;
  }

  void updateCardOpen() {
    sql2Insert("cca_card_open");
    addsqlParm("?", "card_no", cardNo);
    if (wp.itemEq("kk_new_old", "O")) {
      addsqlParm(",?", ", new_end_date", colStr("A.old_end_date"));
      addsqlParm(",?", ", new_beg_date", colStr("A.old_beg_date"));
    } else {
      addsqlParm(",?", ", new_end_date", colStr("A.new_end_date"));
      addsqlParm(",?", ", new_beg_date", colStr("A.new_beg_date"));
    }
    addsqlParm(",?", ", new_old_flag", wp.itemStr2("kk_new_old"));
    addsqlParm(", open_type", ", 'O'");
    addsqlYmd(", open_date");
    addsqlTime(", open_time");
    addsqlParm(",?", ", open_user", modUser);
    addsqlDate2(", mod_time");
    addsqlParm(",?", ", mod_pgm", modPgm);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum == 1)
      return;

    if (sqlRowNum < 0) {
      sqlErr("cca_card_open.ADD, kk=" + cardNo);
      return;
    }

    rc = 1; // 2022/04/07 Justin update when insert duplicate data

    // -Update-
    String lsKkDate = colStr("A.new_end_date");
    sql2Update("cca_card_open");
    if (wp.itemEq("kk_new_old", "O")) {
      addsqlParm(" new_end_date =?", colStr("A.old_end_date"));
      addsqlParm(", new_beg_date =?", colStr("A.old_beg_date"));
      lsKkDate = colStr("A.old_end_date");
    } else {
      addsqlParm(" new_end_date =?", colStr("A.new_end_date"));
      addsqlParm(", new_beg_date =?", colStr("A.new_beg_date"));
    }
    addsql2(", open_type ='O'");
    addsql2(", open_date =" + commSqlStr.sysYYmd);
    addsql2(", open_time =" + commSqlStr.sysTime);
    addsqlParm(", open_user =?", modUser);
    addsql2(", mod_time =" + commSqlStr.sysdate);
    addsqlParm(" where card_no =?", cardNo);
    addsqlParm(" and new_end_date =?", lsKkDate);

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("cca_card_open.Update, kk[%s] ;" + this.sqlErrtext, cardNo);
    }

    return;
  }

  /*
   * ??? public int insert_auth_txlog(){ is_sql = "insert into cca_auth_txlog (" + " card_no , " +
   * " new_end_date , " + " new_beg_date , " + " new_old_flag , " + " open_type , " +
   * " open_date , " + " open_time , " + " open_user , " + " mod_time , " + " mod_pgm " +
   * " ) values (" + " :card_no , " + " :new_end_date , " + " :new_beg_date , " +
   * " :new_old_flag , " + " 'O' , " + " to_char(sysdate,'yyyymmdd') , " +
   * " to_char(sysdate,'hh24miss') , " + " :open_user , " + " sysdate , " + " :mod_pgm " + " )";
   * 
   * rc = sqlExec(is_sql); if (sql_nrow <= 0) { errmsg(this.sql_errtext); }
   * 
   * return rc;
   * 
   * }
   */

  public int insertOnbat2ecs() {
    sql2Insert("onbat_2ecs");
    addsqlParm("?", "trans_type", "9");
    addsqlParm(", to_which", ", '1'");
    addsqlDate2(", dog");
    addsqlDate2(", dop");
    addsqlParm(", proc_mode", ", 'O'");
    addsqlParm(", proc_status", ", '0'");
    addsqlParm(",?", ", card_no", cardNo);
    addsqlParm(",?", ", card_valid_to", colStr("A.new_end_date"));
    addsqlParm(", card_launch_type", ", 'O'");
    addsqlYmd(", card_launch_date");

    sqlExec(sqlStmt(), sqlParms());
    if (sqlRowNum <= 0) {
      errmsg("onbat_2ecs.Add, err=" + this.sqlErrtext);
    }

    return rc;
  }

}
