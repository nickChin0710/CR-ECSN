/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-07-28  V1.00.00   ryan                  program initial                            *
* 109-01-16  V1.00.01   Justin Wu        PP卡 -> 貴賓卡                                                                        *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard        * 
* 111-09-16  V1.00.03   Ryan      調整寄件別、卡片寄送地址                                                                    *  
* 112-06-05  V1.00.04   Ryan      增加卡片寄送地址註記欄位、處理邏輯
******************************************************************************/
package crdm01;

import java.text.SimpleDateFormat;
import java.util.Date;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0170Func extends FuncEdit {
  int i = 0;
  String kkPpCardNo = "";
  String kkReturnDate = "";
  double returndate = 0;
  double maildate = 0;
  String packagedate = "";
  String mailno = "";
  String packageflag = "";

  public Crdm0170Func(TarokoCommon wr) {
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
    String condCol = null;
    packagedate = wp.itemStr("package_date");
    mailno = strMid(wp.itemStr("barcode_num"),0, 6);
    kkPpCardNo = wp.itemStr("pp_card_no");
    kkReturnDate = wp.itemStr("return_date");
    packageflag = wp.itemStr("package_flag");



    if (empty(wp.itemStr("return_date")) == true) {
      returndate = 0;
    } else
      returndate = wp.itemNum("return_date");

    if (empty(wp.itemStr("mail_date")) == true) {
      maildate = 0;
    } else
      maildate = wp.itemNum("mail_date");

    if (this.isAdd()) {
      // 驗證是否已用查詢條件(卡號或掛號號碼或掛號條碼)其中一項帶出基本資料
      String[] condInputArr = {"pp_card_no", "mail_no", "barcode_num"};
      // 找出填寫的欄位是哪一個
      for (int i = 0; i < condInputArr.length; i++) {
        if (!empty(wp.itemStr(condInputArr[i]))) {
          condCol = condInputArr[i];
          break;
        }
      }
      if (empty(condCol)) {
        errmsg("請輸入貴賓卡號、掛號條碼其中一項，以讀取基本資料");
        return;
      }

//      String returnType = wp.itemStr("return_type");
//      // 檢查退卡性質別
//      if (empty(returnType)) {
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

    }


    if (empty(kkPpCardNo) == true) {
      errmsg("貴賓卡號不得為空白 !!");
      return;
    }


    // check mail_date
    if (wp.itemStr("proc_status").equals("3") || wp.itemStr("proc_status").equals("6")) {
      if (empty(wp.itemStr("mail_date"))) {
        errmsg("寄出 , 必須有退卡重寄日期 !!");
        return;
      }
      if (returndate > maildate) {
        errmsg("退卡日期 不可大於 退卡重寄日期 !!");
        return;
      }
      if (wp.itemEmpty("mail_type")) {
        errmsg("未填寫寄件別 !!");
        return;
      }
    } else {
      wp.itemSet("mail_date", "");
    }

    // if (!wp.item_ss("proc_status").equals("3") && !wp.item_ss("proc_status").equals("6")) {
    // if (empty(wp.item_ss("mail_type")) == false) {
    // errmsg("處理結果 3或6 , 才~能~有退卡重寄寄件別 !!");
    // return;
    // }
    // }


    // if (!wp.item_ss("proc_status").equals("3") && !wp.item_ss("proc_status").equals("6")) {
    // if (empty(wp.item_ss("mail_date")) == false) {
    // errmsg("處理結果 3或6 , 才~能~有退卡重寄日期 !!");
    // return;
    // }
    // }

    if (wp.itemStr("proc_status").equals("3") || wp.itemStr("proc_status").equals("6")) {
      if (wp.itemStr("mail_type").equals("5")) {
        errmsg("寄出 , 寄件別不可為暫不寄 !!");
        return;
      }
    }
    
    if(!this.ibDelete) {
    	if(wp.itemEq("mail_type", "1") || wp.itemEq("mail_type", "2"))
    		if(wp.itemEmpty("mail_addr_flag")){
    		   errmsg("寄件別為1或2時,卡片寄送地址註記不可為空值!!");
    	          return;
    	}		
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

    // if (!wp.item_ss("proc_status").equals("4") && empty(wp.item_ss("mail_branch")) == false) {
    // errmsg("寄件別不為4.分行時, 分行別需為空白");
    // return;
    // }
    // if (wp.item_ss("proc_status").equals("4") && empty(wp.item_ss("mail_branch")) == true) {
    // errmsg("寄件別為4.分行時, 分行別不可為空白");
    // return;
    // }

    // if (wp.item_ss("return_date").equals(wp.item_ss("old_return_date"))) {
    // if (empty(wp.item_ss("package_date")) == false){
    // errmsg("資料已寄出, 不可異動");
    // return;
    // }
    // }
    // // else {
    // // packagedate = "";
    // // mailno = "";
    // // }

    if (wp.itemStr("proc_status").equals("3")) {
      packageflag = "Y";
    }

    if (packageflag.equals("Y")) {
      if (wp.itemStr("mail_type").equals("1") || wp.itemStr("mail_type").equals("2")) {
        if (empty(wp.itemStr("zip_code"))) {
          errmsg("郵遞區號不可空白!!");
          return;
        }
      }
    }


    if (this.isAdd()) {

      // 檢查新增資料是否重複
      String lsSql =
          "select count(*) as tot_cnt from crd_return_pp where pp_card_no = ? and return_date= ?";
      Object[] param = new Object[] {kkPpCardNo, kkReturnDate};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
        return;
      }
    } else {
      // -other modify-
      sqlWhere = " where pp_card_no = ? and return_date= ? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {kkPpCardNo, kkReturnDate, wp.modSeqno()};
      if (this.isOtherModify("crd_return_pp", sqlWhere, param)) {
        errmsg("請重新查詢 !");
        return;
      }
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "insert into crd_return_pp (" + " pp_card_no, " + " return_date, " + " id_p_seqno, "
        + " group_code, " + " beg_date, " + " end_date, " + " mail_type, " + " mail_branch, "
        + " return_type, " + " reason_code, " + " zip_code, " + " mail_addr1, " + " mail_addr2, "
        + " mail_addr3, " + " mail_addr4, " + " mail_addr5, " + " proc_status, " + " mail_date, "
        + " mail_no, " + " package_flag, " + " package_date, " + " return_note, " + " mod_time, "
        + " mod_user, " + " mod_pgm, " + " mod_seqno, " + " barcode_num," + " return_seqno ,mail_addr_flag "
        + " ) values (" + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "sysdate,?,?,?,?,?,?)";

    // -set ?value-
    Object[] param = new Object[] {kkPpCardNo, kkReturnDate, wp.itemStr("id_p_seqno"),
        wp.itemStr("group_code"), wp.itemStr("beg_date"), wp.itemStr("end_date"),
        wp.itemStr("mail_type"), wp.itemStr("mail_branch"), wp.itemStr("return_type"),
        wp.itemStr("reason_code"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("zip_code"):wp.itemStr("db_zip_code"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr1"):wp.itemStr("db_mail_addr1"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr2"):wp.itemStr("db_mail_addr2"),
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr3"):wp.itemStr("db_mail_addr3"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr4"):wp.itemStr("db_mail_addr4"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr5"):wp.itemStr("db_mail_addr5"),
        wp.itemStr("proc_status"), wp.itemStr("mail_date"), mailno,
        packageflag, packagedate, wp.itemStr("return_note"), wp.loginUser, wp.modPgm(),
        wp.modSeqno(), wp.itemStr("barcode_num"), wp.colStr("return_seqno"), wp.itemStr("mail_addr_flag")};
    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
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
    strSql = "update crd_return_pp set"
        // + " return_date=?, "
        // + " beg_date=?, "
        // + " end_date=?, "
        + " mail_type=?, " + " return_type=?, " + " reason_code=?, " 
        + " mail_branch=?, "+ " zip_code=?, "
        + " mail_addr1=?, " + " mail_addr2=?, " + " mail_addr3=?, " + " mail_addr4=?, "
        + " mail_addr5=?, " + " proc_status=?, " + " mail_date=?, " + " package_date=?, "
        + " return_note=?, " + " barcode_num=?, " + " mail_no=? " 
        + " , mod_user =?, mod_time=sysdate, mod_pgm =? " + " , mod_seqno =nvl(mod_seqno,0)+1 ,mail_addr_flag = ? "
        + sqlWhere;

    Object[] param = new Object[] {
        // wp.item_ss("return_date"),
        // wp.item_ss("beg_date"),
        // wp.item_ss("end_date"),
        wp.itemStr("mail_type"), wp.itemStr("return_type"), wp.itemStr("reason_code"),
        wp.itemStr("mail_branch"),
        wp.itemEmpty("mail_branch")?wp.itemStr("zip_code"):wp.itemStr("db_zip_code"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr1"):wp.itemStr("db_mail_addr1"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr2"):wp.itemStr("db_mail_addr2"),
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr3"):wp.itemStr("db_mail_addr3"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr4"):wp.itemStr("db_mail_addr4"), 
        wp.itemEmpty("mail_branch")?wp.itemStr("mail_addr5"):wp.itemStr("db_mail_addr5"),
        wp.itemStr("proc_status"), wp.itemStr("mail_date"), packagedate, wp.itemStr("return_note"),
        wp.itemStr("barcode_num"), mailno , wp.loginUser, wp.modPgm(), wp.itemStr("mail_addr_flag"),
        kkPpCardNo,
        kkReturnDate, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
      return rc;
    }

    return rc;

  }

  @Override
  public int dbDelete() {

    return rc;
  }

}
