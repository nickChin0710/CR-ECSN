/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-05-24  V1.00.00  David FU   program initial                            *
* 109-04-28  V1.00.01  YangFang   updated for project coding standard        *
******************************************************************************/

package crdm01;


import busi.FuncEdit;
import taroko.com.TarokoCommon;

public class Crdm0020Func extends FuncEdit {
  String mKkTransNo = "";
  String mKkBinNo = "";
  int dataUpFlag = 0;


  public Crdm0020Func(TarokoCommon wr) {
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

    if (this.ibAdd) {
      mKkBinNo = wp.itemStr("bin_no");
      if (empty(mKkBinNo)) {
        errmsg("請選擇BIN_NO");
        return;
      }
      // 自動取得異動流水號
      mKkTransNo = wp.itemStr("trans_no");
      if (mKkTransNo == null) {
        errmsg("本異動日期流水號已超過99，請選擇其他異動日期再新增");
        return;
      }

    } else {
      if (wp.itemStr("post_flag").equals("Y")) {
        if (wp.itemStr("up_flag").equals("Y")) {
          dataUpFlag = 1;
          return;
        }

        // if(this.ib_delete){
        // String ls_sql = "select count(*) as tot_cnt from crd_seqno_log where reserve='Y' and
        // trans_no=:trans_no ";
        // setString("trans_no", wp.item_ss("trans_no"));
        // sqlSelect(ls_sql);
        //
        // if(col_num("tot_cnt") > 0){
        // errmsg("資料註記已處理2，無法修改");
        // return;
        // }else{
        // String ls_del = " delete crd_seqno_log where trans_no = :trans_no ";
        // setString("trans_no",wp.item_ss("trans_no"));
        // sqlExec(ls_del);
        // }
        // }

        if (this.ibUpdate) {
          errmsg("「已處理註記」為Y，無法修改「備註說明」內容，若要修改須點選「備註說明修改註記」，不須覆核 !");
          return;
        }
      }
      mKkTransNo = wp.itemStr("trans_no");
      mKkBinNo = wp.itemStr("bin_no");
    }

    // BIN_NO長度與卡號區間長度加起來不等於15
    int lenBinNo = wp.itemStr("bin_no").length();
    int lenBegSeqno = wp.itemStr("beg_seqno").length();
    int lenEndSeqno = wp.itemStr("end_seqno").length();
    if (lenBinNo + lenBegSeqno != 15 || lenBinNo + lenEndSeqno != 15) {
      errmsg("BIN_NO長度與卡號區間長度加起來不等於15");
      return;
    }

    if (wp.itemNum("end_seqno") < wp.itemNum("beg_seqno")) {
      errmsg("有效期迄需大於起號");
      return;
    }

    double dBegSeqno = Double.parseDouble(wp.itemStr("beg_seqno").substring(0, 6));
    if (dBegSeqno < wp.itemNum("bin_no_2_fm") || dBegSeqno > wp.itemNum("bin_no_2_to")) {
      errmsg("不可超過7-12碼!!");
      return;
    }

    double dEndSeqno = Double.parseDouble(wp.itemStr("end_seqno").substring(0, 6));
    if (dEndSeqno < wp.itemNum("bin_no_2_fm") || dEndSeqno > wp.itemNum("bin_no_2_to")) {
      errmsg("不可超過7-12碼!!");
      return;
    }



    // 檢查卡號區間
    int nRtn = checkAssignRange(wp.itemStr("card_type"), wp.itemStr("card_type"),
        wp.itemStr("beg_seqno"), wp.itemStr("end_seqno"), wp.itemStr("bin_no"), this.isAdd());
    if (nRtn < 0) {
      switch (nRtn) {
        case -1:
          errmsg("此卡號範圍已指定或已存在, 不可重複指定 !");
          break;
        case -2:
          errmsg("無法取得卡種之 BIN No!");
          break;
        default:
          break;
      }
      return;
    }



    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from crd_cardno_range where trans_no = ?";
      Object[] param = new Object[] {mKkTransNo};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where trans_no = ?  and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkTransNo, wp.modSeqno()};
      isOtherModify("crd_cardno_range", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1) {
      return rc;
    }

    strSql = "insert into crd_cardno_range (" + "  trans_no " + ", trans_date" + ", group_code"
        + ", card_type" + ", card_flag" + ", bin_no" + ", beg_seqno" + ", end_seqno" + ", post_flag"
        + ", crt_date " + ", crt_user " + ", remark_40" + ", mod_time, mod_user, mod_pgm, mod_seqno"
        + " ) values (" + " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?  " + ", sysdate,?,?,1" + " )";
    // -set ?value-
    Object[] param = new Object[] {mKkTransNo // 1
        , wp.itemStr("trans_date"), wp.itemStr("group_code"), wp.itemStr("card_type"),
        wp.itemStr("card_flag"), mKkBinNo,
        String.format("%-9s", wp.itemStr("beg_seqno")).replace(" ", "0"),
        String.format("%-9s", wp.itemStr("end_seqno")).replace(" ", "9"), wp.itemStr("post_flag")// 9
        , getSysDate(), wp.loginUser, wp.itemStr("remark_40"), wp.loginUser, wp.itemStr("mod_pgm")};
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
    if (dataUpFlag == 1) {
      String lsUp =
          " update crd_cardno_range set " + "    remark_40=:remark_40 " + " , mod_user =:mod_user "
              + " , mod_time=sysdate" + " , mod_pgm =:mod_pgm " + " where  trans_no=:trans_no ";
      setString("remark_40", wp.itemStr("remark_40"));
      setString("mod_user", wp.loginUser);
      setString("mod_pgm", wp.modPgm());
      setString("trans_no", wp.itemStr("trans_no"));
      rc = sqlExec(lsUp);
      if (sqlRowNum <= 0) {
        errmsg(this.sqlErrtext);
      }
    } else {
      strSql = "update crd_cardno_range set " + "trans_date = ?" + ", group_code = ?"
          + ", card_type = ?" + ", card_flag = ?" + ", beg_seqno = ?" + ", end_seqno = ?"
          + ", bin_no = ?" + ", remark_40 =?" + " , mod_user =?, mod_time=sysdate, mod_pgm =? "
          + " , mod_seqno =nvl(mod_seqno,0)+1 " + sqlWhere;
      Object[] param = new Object[] {wp.itemStr("trans_date"), wp.itemStr("group_code"),
          wp.itemStr("card_type"), wp.itemStr("card_flag"),
          String.format("%-9s", wp.itemStr("beg_seqno")).replace(" ", "0"),
          String.format("%-9s", wp.itemStr("end_seqno")).replace(" ", "9"), wp.itemStr("bin_no"),
          wp.itemStr("remark_40"), wp.loginUser, wp.itemStr("mod_pgm"), mKkTransNo, wp.modSeqno()};
      rc = sqlExec(strSql, param);
      if (sqlRowNum <= 0) {
        errmsg(this.sqlErrtext);
      }
    }
    return rc;

  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete crd_cardno_range " + sqlWhere;
    Object[] param = new Object[] {mKkTransNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  private int checkAssignRange(String sCardType, String sGroupCode, String sBegSeqno,
      String sEndSeqno, String sBinNo, boolean bAdd) {
    // --check beg_seqno--
    String lsSql =
        "select count(*) as tot_cnt from crd_cardno_range where card_type = ? and bin_no = ? and beg_seqno <= ? and end_seqno >= ?";
    Object[] param = new Object[] {sCardType, sBinNo, sBegSeqno, sBegSeqno};
    sqlSelect(lsSql, param);
    double totCnt = colNum("tot_cnt");
    if ((bAdd && totCnt > 0) || (bAdd == false && totCnt > 1)) {
      return -1;
    }

    // --check end_seqno--
    lsSql =
        "select count(*) as tot_cnt from crd_cardno_range where card_type = ? and bin_no = ? and beg_seqno <= ? and end_seqno >= ?";
    param = new Object[] {sCardType, sBinNo, sEndSeqno, sEndSeqno};
    sqlSelect(lsSql, param);
    totCnt = colNum("tot_cnt");
    if ((bAdd && totCnt > 0) || (bAdd == false && totCnt > 1)) {
      return -1;
    }

    // --check cardno_log--
    if (empty(sBinNo)) {
      return -2;
    }
    // lsSql = "select count(*) as tot_cnt from crd_seqno_log where bin_no = ? and seqno >= ? and
    // seqno <= ? ";
    // param = new Object[] { sBinNo, sBegSeqno, sEndSeqno };
    // sqlSelect(lsSql, param);
    // tot_cnt = col_num("tot_cnt");
    // if (tot_cnt > 0) {
    // return -1;
    // }

    return 0;
  }

}
