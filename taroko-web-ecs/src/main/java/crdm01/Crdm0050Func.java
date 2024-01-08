/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 109-02-06  V1.00.00  zuwei           program initial                       *
* 109-03-02  V1.00.01  Wilson          卡號不可空白                                                                                  *                      
* 109-03-05  V1.00.02  JustinWu     seqno.substring(0,9)                     *
* 109-03-16  V1.00.03  Wilson       card_flag = '1'                          * 
* 109-04-09  V1.00.04  Wilson       post_flag = 'Y'                          *
* 109-04-28  V1.00.05  YangFang   updated for project coding standard        *
* 109-07-27  V1.00.06  Wilson     卡號用途註記修改                                                                                         *
******************************************************************************/

package crdm01;

import busi.FuncEdit;
import busi.ecs.CommFunction;
import taroko.com.TarokoCommon;

public class Crdm0050Func extends FuncEdit {
  CommFunction comm = new CommFunction();

  String groupCode = "";
  String cardType = "";

  public Crdm0050Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  @Override
  public int querySelect() {
    return 0;
  }

  @Override
  public int dataSelect() {
    return 0;
  }

  @Override
  public void dataCheck() {}

  @Override
  public int dbInsert() {
    actionInit("A");
    // 卡號  由使用者輸入，固定16碼，前6碼放到bin_no，7~16碼放到seqno
    String binNo = "", seqno = "";
    String cardNo = wp.itemStr("card_no");
    String reasonCode = wp.itemStr("reason_code");
    String groupCode = wp.itemStr("group_code");
    String cardType = wp.itemStr("card_type");
    try {
      if (empty(cardNo)) {
        errmsg("卡號不可空白!");
        return -1;
      }
      binNo = cardNo.substring(0, 6);
      seqno = cardNo.substring(6);
      checkData(binNo, seqno, reasonCode, groupCode, cardType);
    } catch (Exception e) {
      errmsg(e.getMessage());
    }
    if (rc != 1) {
      return rc;
    }
    strSql = " insert into crd_seqno_log ( " + "bin_no," + "seqno," + "group_code," + "card_type,"
        + "unit_code," + "card_item," + "card_type_sort," + "card_flag," + "reserve,"
        + "reserve_date," + "reserve_id," + "reason_code," + "use_date," + "use_id," + "crt_date,"
        + "trans_no," + "seqno_old," + "mod_user," + "mod_time," + "mod_pgm," + "mod_seqno"
        + ") values ( ?, ?, ?, ?, ?" + ", ?, ?, ?, ?, to_char(sysdate,'yyyymmdd')"
        + ", ?, ?, ?, ?, to_char(sysdate,'yyyymmdd')" + ", ?, ?, ?, sysdate, ?" + ", ? )";
    // -set ?value-
    Object[] param = new Object[] {binNo, // bin_no
        seqno, // seqno
        wp.itemStr("group_code"), // "group_code,"
        wp.itemStr("card_type"), // "card_type,"
        "0000", // "unit_code,"
        "", // "card_item,"
        0, // "card_type_sort,"
        wp.itemStr("card_flag"), // "card_flag,"
        "Y", // reserve
        // wp.item_ss("reserve_date"), // reserve_date
        wp.loginUser, // reserve_id
        wp.itemStr("reason_code"), // reason_code
        "", // "use_date,"
        "", // "use_id,"
        // null, // "crt_date,"
        "", // "trans_no,"
        seqno.substring(0, 9), // "seqno_old,"
        "", // "mod_user,"
//        null, // "mod_time,"
        wp.itemStr("mod_pgm"), // "mod_pgm,"
        wp.modSeqno() // "mod_seqno"
    };
    sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(sqlErrtext);
    }

    return rc;
  }

  @Override
  public int dbUpdate() {
    actionInit("U");
    String binNo = wp.itemStr("bin_no");
    String seqno = wp.itemStr("seqno");
    String reasonCode = wp.itemStr("reason_code");
    String groupCode = wp.itemStr("group_code");
    String cardType = wp.itemStr("card_type");
    try {
      checkData(binNo, seqno, reasonCode, groupCode, cardType);
    } catch (Exception e) {
      errmsg(e.getMessage());
    }
    if (rc != 1) {
      return rc;
    }

    strSql = " update crd_seqno_log set " + " card_flag=? " + " ,group_code=? " + " ,card_type=? "
        + " ,reason_code=? " + " ,mod_user=? " + " ,mod_time=sysdate "
        + " ,mod_seqno =nvl(mod_seqno,0)+1  " + "where  bin_no = ?  and seqno = ? ";
    Object[] param = new Object[] {wp.itemStr("card_flag"), wp.itemStr("group_code"),
        wp.itemStr("card_type"), wp.itemStr("reason_code"), wp.loginUser, binNo, seqno};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    } else {
      wp.setInBuffer("s_bin_no", new String[] {binNo});
      wp.setInBuffer("s_seqno", new String[] {seqno});
    }
    return rc;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    String binNo = wp.itemStr("bin_no");
    String seqno = wp.itemStr("seqno");
    String reasonCode = wp.itemStr("reason_code");
    String groupCode = wp.itemStr("group_code");
    String cardType = wp.itemStr("card_type");
    try {
      checkData(binNo, seqno, reasonCode, groupCode, cardType);
    } catch (Exception e) {
      errmsg(e.getMessage());
    }
    if (rc != 1) {
      return rc;
    }
    strSql = "delete crd_seqno_log where  bin_no = ?  and seqno = ? ";
    Object[] param = new Object[] {binNo, seqno};
    rc = sqlExec(strSql, param);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
    }
    return rc;
  }

  /**
   * 數據檢核
   * 
   * @param binNo
   * @param seqno
   * @param reasonCode
   * @param groupCode 團代
   * @param cardType 卡種
   * @throws Exception
   */
  private void checkData(String binNo, String seqno, String reasonCode, String groupCode,
      String cardType) throws Exception {
    String cardNo = binNo + seqno;
    if (this.ibAdd || this.ibUpdate) {

      if (empty(reasonCode)) {
        errmsg("保留原因不可空白!");
        return;
      }
    }

    if (this.isAdd()) {
      // 撈crd_prohibit(where條件: card_no = 使用者輸入的卡號)，若有撈到代表此卡號為禁號，則不可新增，並跳訊息“該卡號為禁號”
      String lsSql = "select count(*) as tot_cnt from crd_prohibit where card_no = ?";
      Object[] param = new Object[] {cardNo};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("該卡號為禁號");
        return;
      }
      // 判斷使用者輸入的卡號是否已保留或已使用，若有撈到則不可新增，並跳訊息“該卡號已存在”
      lsSql = "select count(*) as tot_cnt from crd_seqno_log where bin_no = ? and seqno = ? ";
      param = new Object[] {binNo, seqno};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("該卡號已存在");
        return;
      }

      // 判斷卡號檢查碼(使用者輸入的卡號第16碼)是否正確，可參考下圖CrdA001.java的220~229行檢查碼規則，若有誤則不可新增，並跳訊息“該卡號檢查碼有誤”
      String tmpX15 = cardNo.substring(0, 15);
      String dif = cardNo.substring(15);
      String chkdif = comm.cardChkCode(tmpX15);
      if (!comm.isNumber(chkdif) || !dif.equals(chkdif)) {
        // String err1 = "Error: 檢查碼錯誤=[" + tmpX15 + "][" + chkdif + "]";
        errmsg("該卡號檢查碼有誤");
        return;
      }

      // 判斷團代、卡種是否存在於crd_cardno_range，若不存在則不可新增
      lsSql =
          "select count(*) as tot_cnt from crd_cardno_range where group_code = ? and card_type = ? and card_flag = '1' and post_flag = 'Y'";
      param = new Object[] {groupCode, cardType};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") == 0) {
        errmsg("團代、卡種未設卡號區間");
        return;
      }

      // 比對輸入的卡號是否在crd_cardno_ange該團代、卡種設定的卡號區間內 (beg_seqno = 區間起、 end_seqno =
      // 區間迄)且Bin_no跟輸入的卡號前6碼是否相同
      lsSql =
          "select count(*) as tot_cnt from crd_cardno_range where bin_no = ? and group_code = ? and card_type = ? and card_flag = '1' and post_flag = 'Y' and cast(char(end_seqno) as bigint) >= ? and cast(char(beg_seqno) as bigint) <= ?  ";
      Long lseqno = Long.valueOf(seqno.substring(0, 9));
      param = new Object[] {binNo, groupCode, cardType, lseqno, lseqno};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") == 0) {
        errmsg("卡號不在團代、卡種設定的卡號區間內");
        return;
      }
    }

    // 檢核卡號是否已使用
    if (this.isDelete() || this.isUpdate()) {
      String lsSql = "select use_date from crd_seqno_log where bin_no = ? and seqno = ? ";
      Object[] params = new Object[] {binNo, seqno};
      sqlSelect(lsSql, params);
      if (!this.isEmpty(colStr("use_date"))) {
        if (this.isUpdate()) {
          errmsg("該卡號已使用，不可修改");
        } else {
          errmsg("該卡號已使用，不可刪除");
        }
        return;
      }
    }

    if (this.isUpdate()) {

    }
  }

}
