/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-23  V1.00.00  Andy       program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
* 111-09-16  V1.00.02   Ryan      調整寄件別、卡片寄送地址                                                                    *  
* 112-06-05  V1.00.03   Ryan      增加卡片寄送地址註記欄位、處理邏輯
******************************************************************************/

package crdm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdm0160Func extends FuncEdit {
  String mKkCardNo = "";
  String mKkIdPSeqno = "";
  String mPackageDate = "";
  String mMailNo = "";
  String mBarcodeNum = "";
  String mPackageFlag = "";
  String mReturnDate = "";
  String mMailDate = "";

  public Crdm0160Func(TarokoCommon wr) {
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
    mKkIdPSeqno = wp.itemStr("id_p_seqno");
    mReturnDate = wp.itemStr("return_date");
    mKkCardNo = wp.itemStr("card_no");
    mPackageDate = wp.itemStr("package_date");
    mMailNo = strMid(wp.itemStr("barcode_num"),0, 6);
    mBarcodeNum = wp.itemStr("barcode_num");

    String sNewDate = "", sOldDate = "", ss = "";
    if (this.isAdd()) {
      // 驗證是否已用查詢條件(卡號或掛號號碼或掛號條碼)其中一項帶出基本資料
      String[] condInputArr = {"card_no", "mail_no", "barcode_num"};
      // 找出填寫的欄位是哪一個
      for (int i = 0; i < condInputArr.length; i++) {
        if (!empty(wp.itemStr(condInputArr[i]))) {
          condCol = condInputArr[i];
          break;
        }
      }
      if (empty(condCol)) {
        errmsg("請輸入卡號、掛號條碼其中一項，以讀取基本資料");
        return;
      }

    }

    // if (!wp.item_ss("proc_status").equals("3") && !wp.item_ss("proc_status").equals("6")) {
    // if (!(empty(wp.item_ss("mail_type"))) || !(empty(wp.item_ss("mail_date"))) ) {
    // errmsg("處理結果為3或6 , 才能填寫寄件別及退卡重寄日期 !!");
    // return;
    // }
    // }

    if (wp.itemStr("proc_status").equals("3") && empty(wp.itemStr("mail_date")) == true) {
      errmsg("寄出 , 必須有退卡重寄日期 !!");
      return;
    }

    // check mail_date
    if (wp.itemStr("proc_status").equals("3") || wp.itemStr("proc_status").equals("6")) {
      if (empty(wp.itemStr("mail_date"))) {
        errmsg("未填寫 退卡重寄日期!!");
        return;
      } else {
        mMailDate = wp.itemStr("mail_date");
      }
      if (Integer.parseInt(mReturnDate) > Integer.parseInt(mMailDate)) {
        errmsg("退卡日期 不可 大於 退卡重寄日期 !!");
        return;
      }
      if (wp.itemEmpty("mail_type")) {
        errmsg("未填寫寄件別 !!");
        return;
      }
    }

    // check card_no
    strSql = "select id_p_seqno from crd_card where card_no =:card_no ";
    setString("card_no", mKkCardNo);
    sqlSelect(strSql);
    if (sqlRowNum <= 0) {
      errmsg("卡號不存在，請重新輸入!!");
      return;
    } else {
      mKkIdPSeqno = colStr("id_p_seqno");
    }


    // // check mail_no
    // // 若是使用修改更能，則會先比對mail_no是否有修改。
    // // 若沒被修改過，則不需要驗證。
    // // 若有則有檢查資料庫中是否已存在此修改後的mail_no。
    // if(! m_mail_no.equals(wp.item_ss("old_mail_no")) ) {
    // is_sql = "select mail_no from crd_return where mail_no =:mail_no ";
    // setString("mail_no", m_mail_no);
    // sqlSelect(is_sql);
    // if (sql_nrow > 0) {
    // errmsg("掛號號碼已存在，請輸入其他掛號號碼!!");
    // return;
    // }
    // }

    // mail_typ & mail_branch
    if (!wp.itemStr("mail_type").equals("4") && !empty(wp.itemStr("mail_branch"))) {
      errmsg("寄件別不為分行，寄件分行需為空白!!");
      return;
    }
    if (wp.itemStr("mail_type").equals("4") && empty(wp.itemStr("mail_branch"))) {
      errmsg("寄件別為分行, 分行別不可為空白!!");
      return;
    }
    //
    sNewDate = getSysDate();
    sOldDate = wp.itemStr("return_date");
    if (sNewDate.equals(sOldDate)) {
      if (notEmpty(wp.itemStr("package_date"))) {
        errmsg("資料已寄出, 不可異動!!");
        return;
      } else {
        mPackageDate = "";
      }
    }
    // package_flag
    mPackageFlag = "N";
    String lsMailType = wp.itemStr("mail_type");
    String lsZipCode = wp.itemStr("zip_code");
    ss = wp.itemStr("proc_status");
    if (ss.equals("3")) {
      mPackageFlag = "Y";
    }
    if (mPackageFlag.equals("Y")) {
      if (lsMailType.equals("1") || lsMailType.equals("2")) {
        if (empty(lsZipCode)) {
          errmsg("郵遞區號不可空白!!");
          return;
        }
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
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      // String lsSql = "select count(*) as tot_cnt from crd_return_pp
      // where pp_card_no = ? and return_date=? ";
      // Object[] param = new Object[] { m_kk_card_no,m_return_date };
      // sqlSelect(lsSql, param);
      // if (col_num("tot_cnt") > 0)
      // {
      // //errmsg("資料已存在，無法新增");
      // }
      // return;
    } else {
      // -other modify-
      // sql_where = " where card_no = ? and nvl(mod_seqno,0) = ?";
      // Object[] param = new Object[] { m_kk_card_no, wp.mod_seqno() };
      // ***本程式以card_no+return_date為查詢KEY值
      sqlWhere = " where card_no = ? and return_date = ?";
      Object[] param = new Object[] {mKkCardNo, mReturnDate};
      isOtherModify("crd_return", sqlWhere, param);
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
    sp.sql2Insert("crd_return");
    sp.ppstr("card_no", mKkCardNo);
    sp.ppstr("id_p_seqno", mKkIdPSeqno);
    sp.ppstr("ic_flag", wp.itemStr("ic_flag"));
    sp.ppstr("beg_date", wp.itemStr("beg_date"));
    sp.ppstr("barcode_num", mBarcodeNum);
    sp.ppstr("end_date", wp.itemStr("end_date"));
    sp.ppstr("return_date", wp.itemStr("return_date"));
    sp.ppstr("return_type", wp.itemStr("return_type"));
    sp.ppstr("reason_code", wp.itemStr("reason_code"));
    sp.ppstr("group_code", wp.itemStr("group_code"));
    sp.ppstr("mail_type", wp.itemStr("mail_type"));
    sp.ppstr("mail_branch", wp.itemStr("mail_branch"));
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
    sp.ppstr("mail_no", mMailNo);
    sp.ppstr("proc_status", wp.itemStr("proc_status"));
    sp.ppstr("return_note", wp.itemStr("return_note"));
    sp.ppstr("return_seqno", wp.colStr("return_seqno"));
    // sp.ppss("mail_date", wp.item_ss("mail_date"));
    sp.ppstr("package_flag", mPackageFlag);
    sp.ppstr("package_date", mPackageDate);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppnum("mod_seqno", 1);
    sp.ppstr("mail_addr_flag", wp.itemStr("mail_addr_flag"));
    sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
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
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_return");
    sp.ppstr("return_type", wp.itemStr("return_type"));
    sp.ppstr("mail_type", wp.itemStr("mail_type"));
    sp.ppstr("mail_branch", wp.itemStr("mail_branch"));
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
    sp.ppstr("mail_no", mMailNo);
    sp.ppstr("barcode_num", wp.itemStr("barcode_num"));
    sp.ppstr("proc_status", wp.itemStr("proc_status"));
    sp.ppstr("return_note", wp.itemStr("return_note"));
    sp.ppstr("mail_date", wp.itemStr("mail_date"));
    sp.ppstr("return_date", wp.itemStr("return_date"));
    if (wp.respHtml.indexOf("_add") > 0) {
      sp.ppstr("ic_flag", wp.itemStr("ic_flag"));
      sp.ppstr("beg_date", wp.itemStr("beg_date"));
      sp.ppstr("end_date", wp.itemStr("end_date"));
      sp.ppstr("group_code", wp.itemStr("group_code"));
    }
    sp.ppstr("reason_code", wp.itemStr("reason_code"));
    sp.ppstr("package_flag", mPackageFlag);
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.ppstr("mail_addr_flag", wp.itemStr("mail_addr_flag"));
    sp.sql2Where(" where card_no=?", mKkCardNo);
    // sp.sql2Where(" and nvl(mod_seqno,0)=?", wp.mod_seqno());
    sp.sql2Where(" and return_date=?", mReturnDate);
    sqlExec(sp.sqlStmt(), sp.sqlParm());

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;

  }

  @Override
  public int dbDelete() {

    return rc;
  }

}
