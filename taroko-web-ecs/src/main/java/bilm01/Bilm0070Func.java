/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-10  V1.00.00  Andy Liu      program initial                         *
* 108/12/04  V1.00.01  Amber         Update                                  *
* 109-04-23  V1.00.02  shiyuqi       updated for project coding standard     * 
* 109-01-04  V1.00.04   shiyuqi       修改无意义命名                         *
* 111-04-12  V1.00.05  Justin        修正insert錯誤型態bug                   *    
* 112/03/08  V1.00.06  yingdong  Erroneous String Compare Issue              *
******************************************************************************/

package bilm01;

import busi.FuncEdit;
import ecsfunc.DEncryptForDB;
import taroko.com.TarokoCommon;

public class Bilm0070Func extends FuncEdit {
  String mKMchtNo = "";
  String ecPawd = "";

  public Bilm0070Func(TarokoCommon wr) {
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
    // check PK
    if (this.ibAdd) {
      // PK check
      mKMchtNo = wp.itemStr("kk_mcht_no");
      if (empty(wp.itemStr("kk_mcht_no"))) {
        errmsg("請輸入特店代號!!");
        return;
      }
    } else {
      mKMchtNo = wp.itemStr("mcht_no");
    }

    // ***********欄位判斷Start
    // 郵購特店必要欄位 mcht_type = 1 check
    if ("1".equals(wp.itemStr("mcht_type"))) {
      if (empty(wp.itemStr("mcht_eng_name")) || empty(wp.itemStr("sign_date"))
          || empty(wp.itemStr("mcht_address")) || empty(wp.itemStr("mcht_board_name"))
          || empty(wp.itemStr("mcht_open_addr")) || empty(wp.itemStr("tx_type"))
          || empty(wp.itemStr("pos_flag")) || empty(wp.itemStr("mcht_setup_date"))
          || empty(wp.itemStr("advance_flag")) || empty(wp.itemStr("mcht_property"))
          || empty(wp.itemStr("card_type_name")) || empty(wp.itemStr("owner_id"))
          || empty(wp.itemStr("installment_delay"))) {
        errmsg("郵購特店必要欄位 有誤~ !!");
        return;
      }
      if (empty(wp.itemStr("installment_delay"))) {
        errmsg("分期入帳時間未選~ !!");
        return;
      }
    }

    // 解約資料欄位check
    // andy 20180803新增查核
    if (empty(wp.itemStr("broken_date")) == false
        && wp.itemStr("mcht_status").equals("2") == false) {
      errmsg("特店狀態有誤~ !!");
      return;
    }
    if (empty(wp.itemStr("broken_date")) == false) {
      if (empty(wp.itemStr("rsecind_kind")) == true || empty(wp.itemStr("rsecind_flag")) == true) {
        errmsg("解約資料 1 有誤~ !!");
        return;
      }
    }

    if (empty(wp.itemStr("rsecind_kind")) == false) {
      if (empty(wp.itemStr("broken_date")) == true || empty(wp.itemStr("rsecind_flag")) == true) {
        errmsg("解約資料2 有誤~ !!");
        return;
      }
    }

    if (empty(wp.itemStr("rsecind_flag")) == false) {
      if (empty(wp.itemStr("broken_date")) == true || empty(wp.itemStr("rsecind_kind")) == true) {
        errmsg("解約資料3 有誤~ !!");
        return;
      }
    }

    // 特店類別mcht_type = 0 or 2
    /*
    if ("0".equals(wp.itemStr("mcht_type")) || "2".equals(wp.itemStr("mcht_type"))) {
      if ("Y".equals(wp.itemStr("loan_flag"))) {
        errmsg("特店類別為0或2時貸款特店旗標不可為Y !!!");
        return;
      }
    }
    */

    // 統一編號check
    if (wp.itemStr("uniform_no").length() < 8) {
      errmsg("統一編號尚未打滿 !!!");
      return;
    }

    // 帳號check
    if (empty(wp.itemStr("bank_name")) == false || empty(wp.itemStr("assign_acct")) == false) {
      if (empty(wp.itemStr("oth_bank_name")) == false || empty(wp.itemStr("oth_bank_id")) == false
          || empty(wp.itemStr("oth_bank_acct")) == false) {
        errmsg("帳號只能本行或他行二選一!");
        return;
      }
    }
    if (empty(wp.itemStr("oth_bank_name")) == false || empty(wp.itemStr("oth_bank_id")) == false
        || empty(wp.itemStr("oth_bank_acct")) == false) {
      if (empty(wp.itemStr("bank_name")) == false || empty(wp.itemStr("assign_acct")) == false) {
        errmsg("帳號只能本行或他行二選一!");
        return;
      }
    }

    // mp_rate >100
    int rate = Integer.parseInt(wp.itemStr("mp_rate"));
    int dbMpRate = Integer.parseInt(wp.itemStr("db_mp_rate"));
    if (rate > 100 || rate < dbMpRate) {
      errmsg("當期分期款納入最低應繳金額比例 需 >= ptrm0140帳務一般參數維護(帳戶類別):本金最低應繳金額 1.當期消費金額%且 < 100%");
      return;
    }

    // tx_type交易型態
    String wkTxType = wp.itemStr("tx_type");
    String cType = "1234";
    for (int i = 0; i < wkTxType.length(); i++) {
      String wkTxTypes = strMid(wkTxType, i, 1);
      if (cType.indexOf(wkTxTypes) == -1) {
        errmsg("交易型態錯誤~!!");
        return;
      }
    }

    // 20191204add ZIP檔密碼檢核
    if (!empty(wp.itemStr("gift_file_passwd"))) {
      if (empty(wp.itemStr("gift_file_passwd1"))) {
        errmsg("未輸入檢核密碼!");
        return;
      }
      if (!empty(wp.itemStr("gift_file_passwd1"))) {
        if (!wp.itemStr("gift_file_passwd").equals(wp.itemStr("gift_file_passwd1"))) {
          errmsg("2次密碼不一致!!");
          return;
        }
      }
    }

    // 20191204add ZIP檔密碼加密
    DEncryptForDB ec = new DEncryptForDB();
    ecPawd = ec.encryptForDb(wp.itemStr("gift_file_passwd"));

    // wk_tx_type.indexOf("2") == -1 &&
    // wk_tx_type.indexOf("3") == -1 &&
    // wk_tx_type.indexOf("4") == -1 ){
    // errmsg("交易型態錯誤~!!");
    // return;
    // }
    // 20180516 vik
    // if (rate == 100){
    // errmsg("當期分期款納入最低應繳金額比例 需 >= ptrm0140帳務一般參數維護(帳戶類別):本金最低應繳金額
    // 1.當期消費金額%且 < 100%");
    // return;
    // }
    // wp.ddd("conf_flag="+wp.item_ss("conf_flag"));
    // if (rate == 100) {
    // if (item_eq("conf_flag","Y")==false) {
    // wp.respMesg ="是否暫停執行";
    // wp.col_set("conf_mesg","Y");
    // return false;
    // }
    // else {
    // wp.ddd("暫停執行中.....");
    // return false;
    // }
    // }
    // if dw_data.object.mp_rate[1] = 100 then
    // if MessageBox("警示", '當期分期款納入最低應繳金額比例 =
    // 100%，銷帳時當期分期款優先且起息日為繳款截止日。是否存檔?!' , Question! , YesNo! , 2) = 2 THEN
    // Return -1
    // end if
    // end if

    // check 卡別card_type_name ADEGJMNVO
    String cardTypeNameAll = "ADEGJMNVO";
    String wkCardTypeName = wp.itemStr("card_type_name");
    for (int i = 0; i < wkCardTypeName.length(); i++) {
      String wkCardTypeNames = strMid(wkCardTypeName, i, 1);
      if (cardTypeNameAll.indexOf(wkCardTypeNames) == -1) {
        errmsg("卡別錯誤~ !!");
        return;
      }
    }

    // 傳輸參考代碼 gift_file_dir
    String giftFileDir = wp.itemStr("gift_file_dir");
    if (empty(wp.itemStr("gift_file_dir")) == false) {
      String lsSql = "select count(*) as tot_cnt from cs_ip_addr a, ecs_ref_ip_addr b "
          + "where a.ecs_ip = b.ecs_ip and  b.ref_ip_code = ? ";
      Object[] param = new Object[] {giftFileDir};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") == 0) {
        errmsg("傳輸參考代碼 不存在~!!");
        return;
      }
    }
    // ***********欄位判斷End
    // check duplicate
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from bil_merchant_t where mcht_no = ? ";
      Object[] param = new Object[] {mKMchtNo};
      sqlSelect(lsSql, param);
      if (sqlRowNum > 0) {
        if (colNum("tot_cnt") > 0) {
          errmsg("資料已存在，無法新增");
        }
      }
      return;
    }

    // -other modify-
    sqlWhere = " where mcht_no= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {mKMchtNo, wp.modSeqno()};
    if (this.isOtherModify("bil_merchant_t", sqlWhere, param)) {
      errmsg("請重新查詢 !");
      return;
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into bil_merchant_t (" + " mcht_no " // 1 pk
        + " , uniform_no" // FK
        + " , forced_flag" + " , mcht_eng_name" + " , sign_date" + " , mcht_chi_name"
        + " , broken_date" + " , mcht_type" + " , loan_flag" + " , trans_flag" + " , mcht_zip" // 11
        + " , mcht_address" + " , owner_name" + " , owner_id" + " , contract_name" + " , mcht_tel1"
        + " , mcht_tel1_1" + " , mcht_tel1_2" + " , mcht_tel2" + " , mcht_tel2_1" + " , mcht_tel2_2" // 21
        + " , mcht_fax1" + " , mcht_fax1_1" + " , mcht_fax2" + " , mcht_fax2_1" + " , e_mail"
        + " , mcc_code" + " , bank_name" + " , assign_acct" + " , oth_bank_name" + " , oth_bank_id" // 31
        + " , oth_bank_acct" + " , mp_rate" + " , clr_bank_id" + " , mcht_acct_name"
        + " , mcht_city" + " , mcht_country" + " , mcht_state" + " , mcht_status"
        + " , mcht_board_name" + " , mcht_open_addr" // 41
        + " , mcht_capital" + " , mcht_setup_date" + " , advance_flag" + " , tx_type"
        + " , chain_type" + " , mcht_property" + " , pos_flag" + " , card_type_name"
        + " , video_flag" + " , rsecind_kind" // 51
        + " , rsecind_flag" + " , gift_file_name" + " , gift_file_passwd" + " , gift_file_dir"
        + " , confirm_flag" // FK
        + " , contract_head" // FK
        + " , borrow_flag" // FK
        + " , crt_user" + " , crt_date" + " , mod_user, mod_time , mod_pgm , mod_seqno "
        + " , stmt_inst_flag " // 0511
        + " , installment_delay " + " , chk_online " // 20180927 add Andy
        + " ) values (" + " ?,?,?,?,?,?,?,?,?,?," + " ?,?,?,?,?,?,?,?,?,?,"
        + " ?,?,?,?,?,?,?,?,?,?," + " ?,?,?,?,?,?,?,?,?,?," + " ?,?,?,?,?,?,?,?,?,?,"
        + " ?,?,?,?,?,?,?,?," + " ?,to_char(sysdate,'yyyymmdd')," + " ?,sysdate,?,1,?,?,?" + " )";
    // -set ?value-

    Object[] param = new Object[] {mKMchtNo, // 1
        wp.itemStr("uniform_no"), wp.itemStr("forced_flag"), wp.itemStr("mcht_eng_name"),
        wp.itemStr("sign_date"), wp.itemStr("mcht_chi_name"), wp.itemStr("broken_date"),
        wp.itemStr("mcht_type"), wp.itemStr("loan_flag"), wp.itemStr("trans_flag"),
        wp.itemStr("mcht_zip"), // 11
        wp.itemStr("mcht_address"), wp.itemStr("owner_name"), wp.itemStr("owner_id"),
        wp.itemStr("contract_name"), wp.itemStr("mcht_tel1"), wp.itemStr("mcht_tel1_1"),
        wp.itemStr("mcht_tel1_2"), wp.itemStr("mcht_tel2"), wp.itemStr("mcht_tel2_1"),
        wp.itemStr("mcht_tel2_2"), // 21
        wp.itemStr("mcht_fax1"), wp.itemStr("mcht_fax1_1"), wp.itemStr("mcht_fax2"),
        wp.itemStr("mcht_fax2_1"), wp.itemStr("e_mail"), wp.itemStr("mcc_code"),
        wp.itemStr("bank_name"), wp.itemStr("assign_acct"), wp.itemStr("oth_bank_name"),
        wp.itemStr("oth_bank_id"), // 31
        wp.itemStr("oth_bank_acct"), wp.itemStr("mp_rate"), wp.itemStr("clr_bank_id"),
        wp.itemStr("mcht_acct_name"), wp.itemStr("mcht_city"), wp.itemStr("mcht_country"),
        wp.itemStr("mcht_state"), wp.itemStr("mcht_status"), wp.itemStr("mcht_board_name"),
        wp.itemStr("mcht_open_addr"), // 41
        wp.itemNum("mcht_capital"),  // 2022/04/12: fix a bug : wp.itemStr("mcht_capital"),
        wp.itemStr("mcht_setup_date"), wp.itemStr("advance_flag"),
        wp.itemStr("tx_type"), wp.itemStr("chain_type"), wp.itemStr("mcht_property"),
        wp.itemStr("pos_flag"), wp.itemStr("card_type_name"), wp.itemStr("video_flag"),
        wp.itemStr("rsecind_kind"), // 51
        wp.itemStr("rsecind_flag"), wp.itemStr("gift_file_name"),
        // wp.item_ss("gift_file_passwd"), //20191204 change
        ecPawd, wp.itemStr("gift_file_dir"), "N", "N", "N", wp.loginUser, wp.loginUser,
        wp.itemStr("mod_pgm"),
        !wp.itemStr("stmt_inst_flag").equals("Y") ? "N" : wp.itemStr("stmt_inst_flag"),
        wp.itemStr("installment_delay"), wp.itemStr("chk_online")};

    // System.out.println("is_sql:" + is_sql);
    // for (int i = 0; i <= param.length; i++) {
    // System.out.println(param[i] + ",");
    // }

    sqlExec(strSql, param);
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

    strSql = "update bil_merchant_t set " + " uniform_no =?" // 1
        + " , forced_flag =?" + " , mcht_eng_name =?" + " , sign_date =?" + " , mcht_chi_name =?"
        + " , broken_date =?" + " , mcht_type =?" + " , loan_flag =?" + " , trans_flag =?"
        + " , mcht_zip =?" + " , mcht_address =?" // 11
        + " , owner_name =?" + " , owner_id =?" + " , contract_name =?" + " , mcht_tel1 =?"
        + " , mcht_tel1_1 =?" + " , mcht_tel1_2 =?" + " , mcht_tel2 =?" + " , mcht_tel2_1 =?"
        + " , mcht_tel2_2 =?" + " , mcht_fax1 =?" // 21
        + " , mcht_fax1_1 =?" + " , mcht_fax2 =?" + " , mcht_fax2_1 =?" + " , e_mail =?"
        + " , mcc_code =?" + " , bank_name =?" + " , assign_acct =?" + " , oth_bank_name =?"
        + " , oth_bank_id =?" + " , oth_bank_acct =?" // 31
        + " , mp_rate =?" + " , clr_bank_id =?" + " , mcht_acct_name =?" + " , confirm_flag =?"
        + " , mcht_city =?" + " , mcht_country =?" + " , mcht_state =?" + " , mcht_status =?"
        + " , mcht_board_name =?" + " , mcht_open_addr =?" // 41
        + " , mcht_capital =?" + " , mcht_setup_date =?" + " , advance_flag =?" + " , tx_type =?"
        + " , chain_type =?" + " , mcht_property =?" + " , pos_flag =?" + " , card_type_name =?"
        + " , video_flag =?" + " , rsecind_kind =?" // 51
        + " , rsecind_flag =?" + " , gift_file_name =?" + " , gift_file_passwd =?"
        + " , gift_file_dir =?" + " , contract_head =?" + " , borrow_flag =?"
        // + " , crt_user =?"
        // + " , crt_date =?"
        // + " , apr_user =?"
        // + " , apr_date =?"
        + " , stmt_inst_flag =?" // 0511
        + " , installment_delay =?" // 0511
        + " , chk_online =?" // 20180927 add Andy
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 "
        + sqlWhere;

    Object[] param = new Object[] {wp.itemStr("uniform_no"), // 1 fk
        wp.itemStr("forced_flag"), wp.itemStr("mcht_eng_name"), wp.itemStr("sign_date"),
        wp.itemStr("mcht_chi_name"), wp.itemStr("broken_date"), wp.itemStr("mcht_type"),
        wp.itemStr("loan_flag"), wp.itemStr("trans_flag"), wp.itemStr("mcht_zip"),
        wp.itemStr("mcht_address"), // 11
        wp.itemStr("owner_name"), wp.itemStr("owner_id"), wp.itemStr("contract_name"),
        wp.itemStr("mcht_tel1"), wp.itemStr("mcht_tel1_1"), wp.itemStr("mcht_tel1_2"),
        wp.itemStr("mcht_tel2"), wp.itemStr("mcht_tel2_1"), wp.itemStr("mcht_tel2_2"),
        wp.itemStr("mcht_fax1"), // 21
        wp.itemStr("mcht_fax1_1"), wp.itemStr("mcht_fax2"), wp.itemStr("mcht_fax2_1"),
        wp.itemStr("e_mail"), wp.itemStr("mcc_code"), wp.itemStr("bank_name"),
        wp.itemStr("assign_acct"), wp.itemStr("oth_bank_name"), wp.itemStr("oth_bank_id"),
        wp.itemStr("oth_bank_acct"), // 31
        wp.itemStr("mp_rate"), wp.itemStr("clr_bank_id"), wp.itemStr("mcht_acct_name"),
        wp.itemStr("h_confirm_flag"), // fk
        wp.itemStr("mcht_city"), wp.itemStr("mcht_country"), wp.itemStr("mcht_state"),
        wp.itemStr("mcht_status"), wp.itemStr("mcht_board_name"), wp.itemStr("mcht_open_addr"), // 41
        wp.itemNum("mcht_capital"), // 2022/04/12: fix a bug : wp.itemStr("mcht_capital"),
        wp.itemStr("mcht_setup_date"), wp.itemStr("advance_flag"),
        wp.itemStr("tx_type"), wp.itemStr("chain_type"), wp.itemStr("mcht_property"),
        wp.itemStr("pos_flag"), wp.itemStr("card_type_name"), wp.itemStr("video_flag"),
        wp.itemStr("rsecind_kind"), // 51
        wp.itemStr("rsecind_flag"), wp.itemStr("gift_file_name"),
        // wp.item_ss("gift_file_passwd"), //20191204 change
        ecPawd, wp.itemStr("gift_file_dir"),
        // wp.item_ss("crt_user"),
        // wp.item_ss("crt_date"),
        // wp.item_ss("apr_user"),
        // wp.item_ss("apr_date"),
        wp.itemStr("h_contract_head"), // fk
        wp.itemStr("h_borrow_flag"), // fk
        wp.itemStr("stmt_inst_flag"), wp.itemStr("installment_delay"), wp.itemStr("chk_online"),
        wp.loginUser, wp.itemStr("mod_pgm"), mKMchtNo, wp.modSeqno()};
    // System.out.println("is_sql:"+is_sql);
    // for (int i = 0;i <= param.length;i++){
    // System.out.println(param[i]+",");
    // }

    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    // dataCheck();
    mKMchtNo = wp.itemStr("mcht_no");
    if (!empty(wp.itemStr("apr_user"))) {
      errmsg("已覆核資料不可刪除!!");
      return 0;
    }
    // if (rc != 1) {
    // return rc;
    // }
    strSql = "delete bil_merchant_t " + " where mcht_no= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {mKMchtNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

}
