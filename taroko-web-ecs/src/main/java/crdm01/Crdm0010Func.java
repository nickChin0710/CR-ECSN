/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR      DESCRIPTION                               *
* ---------  --------  ---------- ------------------------------------------ *
* 106-10-23  V1.00.00  Andy       program initial                            *
* 109-01-02	 V1.00.01  Andy       update : UI                                
 
******************************************************************************/

package crdm01;

import busi.FuncEdit;
import busi.SqlPrepare;
import taroko.com.TarokoCommon;

public class Crdm0010Func extends FuncEdit {
  String mKkWarehouseNo = "";
  String mLotNo = "";
  String gsWhYear = "";
  String gsWhMonth = "";
  String gsPlace = "";
  String gsCardItem = "";
  String gsTnsType = "";
  String gsTransReason = "";
  int seqNo = 0;
  double gsPrevTotal = 0;

  public Crdm0010Func(TarokoCommon wr) {
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
    mKkWarehouseNo = wp.itemStr("warehouse_no");
    mLotNo = wp.itemStr("lot_no");
    gsWhYear = wp.itemStr("wh_year");
    gsWhMonth = wp.itemStr("wh_month");
    gsPlace = wp.itemStr("place");
    gsCardItem = wp.itemStr("card_item");
    gsPrevTotal = varsNum("aa_prev_total");
    gsTnsType = wp.itemStr("tns_type");
    if (gsTnsType.equals("1")) {
      gsTransReason = wp.itemStr("trans_reason1");
    } else {
      gsTransReason = wp.itemStr("trans_reason2");
    }

    // tns_type = '1' 入庫 & trans_reason1 = '2'新購時編lot_no 批號 //20180820 Andy
    // lot_no 日期+流水號
    // if(this.isAdd()){
    // if (gs_tns_type.equals("1") && gs_trans_reason1.equals("2")) {
    // String ls_sql = "SELECT :ls_warehouse_date ||
    // substr(to_char(to_number(nvl(substr(max(lot_no),9,2),'0'))+1 , '00'),2,2) as lot_no "
    // + "FROM crd_whtrans "
    // + "where lot_no like :ls_warehouse_date||'%' ";
    // setString("ls_warehouse_date", get_sysDate());
    // sqlSelect(ls_sql);
    // wp.col_set("lot_no", col_ss("lot_no"));
    // m_lot_no = col_ss("lot_no");
    // }
    // }
    if (this.isAdd()) {
      if (gsTnsType.equals("1") && gsTransReason.equals("2")) {
        mLotNo = wp.itemStr("lot_no");
      } else {
        mLotNo = "";
      }
    }
    //
    if (this.isAdd()) {
      // 檢查新增資料是否重複
      String lsSql = "select count(*) as tot_cnt from crd_whtrans where warehouse_no = ? ";
      Object[] param = new Object[] {mKkWarehouseNo};
      sqlSelect(lsSql, param);
      if (colNum("tot_cnt") > 0) {
        errmsg("資料已存在，無法新增");
      }
      return;
    } else {
      // -other modify-
      sqlWhere = " where warehouse_no = ? and nvl(mod_seqno,0) = ?";
      Object[] param = new Object[] {mKkWarehouseNo, wp.modSeqno()};
      isOtherModify("crd_whtrans", sqlWhere, param);
    }
  }

  @Override
  public int dbInsert() {
    // 出入庫均新增1筆crd_whtrans
    actionInit("A");
    dataCheck();

    if (rc != 1) {
      return rc;
    }
    String wkTransReason = "", wkTnsType = "";
    wkTnsType = wp.itemStr("tns_type");
    if (wkTnsType.equals("1")) {
      wkTransReason = wp.itemStr("trans_reason1");
    } else {
      wkTransReason = wp.itemStr("trans_reason2");
    }
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_whtrans");
    sp.ppstr("warehouse_no", mKkWarehouseNo);
    sp.ppstr("lot_no", mLotNo);
    sp.ppstr("card_item", wp.itemStr("card_item"));
    sp.ppstr("card_type", varsStr("aa_card_type")); // 20200102 add
    sp.ppstr("unit_code", varsStr("aa_unit_code")); // 20200102 add
    sp.ppstr("warehouse_date", wp.itemStr("warehouse_date"));
    sp.ppstr("place", wp.itemStr("place"));
    sp.ppstr("tns_type", wp.itemStr("tns_type"));
    sp.ppstr("trans_reason", wkTransReason);
    sp.ppnum("prev_total", gsPrevTotal);
    sp.ppnum("use_total", wp.itemNum("use_total"));
    sp.ppnum("item_amt", wp.itemNum("item_amt"));
    sp.ppstr("crt_date", getSysDate());
    sp.ppstr("crt_user", wp.loginUser);
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppstr("mod_seqno", "1");
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return -1;
    }
    // 入庫 : 直接新增crd_warehouse & crd_whtx_dtl
    if (wkTnsType.equals("1")) {
      rc = dbInsert1();
      try {
        rc = insertCrdWhtxDtl(mLotNo, wp.itemNum("use_total"), gsPrevTotal, wp.itemNum("item_amt"));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return rc;
  }

  public int dbInsert1() {
    dataCheck();
    // check crd_warehouse 是否已存在
    String lsSql = "select count(*) ct from crd_warehouse " + "where wh_year =:wh_year "
        + "and card_item =:card_item " + "and place =:place " + "and lot_no =:lot_no ";
    setString("wh_year", gsWhYear);
    setString("card_item", gsCardItem);
    setString("place", gsPlace);
    setString("lot_no", mLotNo);
    sqlSelect(lsSql);
    if (colNum("ct") > 0) {
      // 已有crd_warehouse直接update
      busi.SqlPrepare sp = new SqlPrepare();
      sp.sql2Update("crd_warehouse");
      sp.ppnum("in_qty01", varsNum("h_ware_in_qty01"));
      sp.ppnum("in_qty02", varsNum("h_ware_in_qty02"));
      sp.ppnum("in_qty03", varsNum("h_ware_in_qty03"));
      sp.ppnum("in_qty04", varsNum("h_ware_in_qty04"));
      sp.ppnum("in_qty05", varsNum("h_ware_in_qty05"));
      sp.ppnum("in_qty06", varsNum("h_ware_in_qty06"));
      sp.ppnum("in_qty07", varsNum("h_ware_in_qty07"));
      sp.ppnum("in_qty08", varsNum("h_ware_in_qty08"));
      sp.ppnum("in_qty09", varsNum("h_ware_in_qty09"));
      sp.ppnum("in_qty10", varsNum("h_ware_in_qty10"));
      sp.ppnum("in_qty11", varsNum("h_ware_in_qty11"));
      sp.ppnum("in_qty12", varsNum("h_ware_in_qty12"));
      if (gsTnsType.equals("1") && gsTransReason.equals("2")) {
        sp.ppnum("in_qty" + wp.itemStr("wh_month") + "_buy", wp.itemNum("use_total"));
      }
      sp.addsql(", mod_time =sysdate", "");
      sp.ppstr("mod_user", wp.loginUser);
      sp.ppstr("mod_pgm", wp.modPgm());
      sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
      sp.sql2Where("where wh_year=?", gsWhYear);
      sp.sql2Where("and card_item=?", gsCardItem);
      sp.sql2Where("and place=?", gsPlace);
      sp.sql2Where("and lot_no=?", mLotNo);
      rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
      if (sqlRowNum <= 0) {
        errmsg(this.sqlErrtext);
        return rc;
      }
      return rc;
    }
    // 每筆入庫均新增1筆crd_warehouse
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_warehouse");
    sp.ppstr("wh_year", wp.itemStr("wh_year"));
    sp.ppstr("card_item", wp.itemStr("card_item"));
    sp.ppstr("place", wp.itemStr("place"));
    sp.ppstr("lot_no", mLotNo);
    sp.ppnum("item_amt", wp.itemNum("item_amt"));
    sp.ppnum("pre_total", 0);
    sp.ppnum("in_qty" + wp.itemStr("wh_month"), wp.itemNum("use_total"));
    if (wp.itemStr("trans_reason1").equals("2")) {
      sp.ppnum("in_qty" + wp.itemStr("wh_month") + "_buy", wp.itemNum("use_total"));
    }
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppstr("mod_seqno", "1");

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return -1;
    }

    return rc;
  }

  public int dbInsert2() {
    // 卡樣前年無結轉庫存或當年度無入庫資料時,當年第1筆出庫時先新增1筆crd_warehouse
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_warehouse");
    sp.ppstr("wh_year", wp.itemStr("wh_year"));
    sp.ppstr("card_item", wp.itemStr("card_item"));
    sp.ppstr("place", wp.itemStr("place"));
    sp.ppstr("lot_no", mLotNo);
    sp.ppnum("pre_total", varsNum("aa_prev_total"));
    sp.addsql(", mod_time ", ", sysdate ");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.ppstr("mod_seqno", "1");

    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return -1;
    }
    return rc;
  }

  @Override
  public int dbUpdate() {
    return 1;
  }

  public int dbUpdate1() {
    // 出庫 update crd_warehouse
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_warehouse");
    sp.ppnum("out_qty01", varsNum("h_ware_out_qty01"));
    sp.ppnum("out_qty02", varsNum("h_ware_out_qty02"));
    sp.ppnum("out_qty03", varsNum("h_ware_out_qty03"));
    sp.ppnum("out_qty04", varsNum("h_ware_out_qty04"));
    sp.ppnum("out_qty05", varsNum("h_ware_out_qty05"));
    sp.ppnum("out_qty06", varsNum("h_ware_out_qty06"));
    sp.ppnum("out_qty07", varsNum("h_ware_out_qty07"));
    sp.ppnum("out_qty08", varsNum("h_ware_out_qty08"));
    sp.ppnum("out_qty09", varsNum("h_ware_out_qty09"));
    sp.ppnum("out_qty10", varsNum("h_ware_out_qty10"));
    sp.ppnum("out_qty11", varsNum("h_ware_out_qty11"));
    sp.ppnum("out_qty12", varsNum("h_ware_out_qty12"));
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where hex(rowid)=?", varsStr("h_ware_rowid"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }
    // 出庫新增crd_whtx_dtl其 lot_no為crd_warehouse入庫資料的編號,use_total為該筆入庫資料的出庫數,
    try {
      rc = insertCrdWhtxDtl(varsStr("h_ware_lot_no"), varsNum("tmp_use_total"),
          varsNum("h_pre_total_bal"), varsNum("h_ware_item_amt"));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return rc;

  }

  public int dbUpdate2() {
    // update crd_warehouse
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_warehouse");
    sp.ppnum("out_qty01", varsNum("h_ware_out_qty01"));
    sp.ppnum("out_qty02", varsNum("h_ware_out_qty02"));
    sp.ppnum("out_qty03", varsNum("h_ware_out_qty03"));
    sp.ppnum("out_qty04", varsNum("h_ware_out_qty04"));
    sp.ppnum("out_qty05", varsNum("h_ware_out_qty05"));
    sp.ppnum("out_qty06", varsNum("h_ware_out_qty06"));
    sp.ppnum("out_qty07", varsNum("h_ware_out_qty07"));
    sp.ppnum("out_qty08", varsNum("h_ware_out_qty08"));
    sp.ppnum("out_qty09", varsNum("h_ware_out_qty09"));
    sp.ppnum("out_qty10", varsNum("h_ware_out_qty10"));
    sp.ppnum("out_qty11", varsNum("h_ware_out_qty11"));
    sp.ppnum("out_qty12", varsNum("h_ware_out_qty12"));
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where hex(rowid)=?", varsStr("h_ware_rowid"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }
    return rc;

  }

  public int dbUpdate3() {
    // update crd_warehouse
    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Update("crd_warehouse");
    sp.ppnum("in_qty01", varsNum("h_ware_in_qty01"));
    sp.ppnum("in_qty02", varsNum("h_ware_in_qty02"));
    sp.ppnum("in_qty03", varsNum("h_ware_in_qty03"));
    sp.ppnum("in_qty04", varsNum("h_ware_in_qty04"));
    sp.ppnum("in_qty05", varsNum("h_ware_in_qty05"));
    sp.ppnum("in_qty06", varsNum("h_ware_in_qty06"));
    sp.ppnum("in_qty07", varsNum("h_ware_in_qty07"));
    sp.ppnum("in_qty08", varsNum("h_ware_in_qty08"));
    sp.ppnum("in_qty09", varsNum("h_ware_in_qty09"));
    sp.ppnum("in_qty10", varsNum("h_ware_in_qty10"));
    sp.ppnum("in_qty11", varsNum("h_ware_in_qty11"));
    sp.ppnum("in_qty12", varsNum("h_ware_in_qty12"));
    sp.addsql(", mod_time =sysdate", "");
    sp.ppstr("mod_user", wp.loginUser);
    sp.ppstr("mod_pgm", wp.modPgm());
    sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1", "");
    sp.sql2Where("where hex(rowid)=?", varsStr("h_ware_rowid"));
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }
    return rc;

  }

  public int insertCrdWhtxDtl(String iLotNo, double tmpQty, double iPrevTotal, double iItemAmt)
      throws Exception {
    seqNo++;
    mKkWarehouseNo = wp.itemStr("warehouse_no");
    gsPlace = wp.itemStr("place");
    gsCardItem = wp.itemStr("card_item");

    busi.SqlPrepare sp = new SqlPrepare();
    sp.sql2Insert("crd_whtx_dtl");
    sp.ppstr("warehouse_no", mKkWarehouseNo);
    sp.ppnum("seq_no", seqNo);
    sp.ppstr("warehouse_date", getSysDate());
    sp.ppstr("card_item", gsCardItem);
    sp.ppstr("lot_no", iLotNo);
    sp.ppstr("tns_type", wp.itemStr("tns_type"));
    sp.ppstr("place", gsPlace);
    if (wp.itemStr("tns_type").equals("1")) {
      sp.ppnum("prev_total", 0);
    } else {
      sp.ppnum("prev_total", iPrevTotal);
    }
    sp.ppnum("use_total", tmpQty);
    sp.ppnum("item_amt", iItemAmt);
    rc = sqlExec(sp.sqlStmt(), sp.sqlParm());
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return -1;
    }
    return 1;
  }

  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1) {
      return rc;
    }
    strSql = "delete crd_whtrans " + " where warehouse_no= ? " + " and nvl(mod_seqno,0) = ? ";
    Object[] param = new Object[] {mKkWarehouseNo, wp.modSeqno()};
    rc = sqlExec(strSql, param);

    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }
    // delete crd_whtx_dtl if tns_type == "2"
    // if (gs_tns_type.equals("2")) {
    strSql = "delete crd_whtx_dtl where warehouse_no =:warehouse_no";
    setString("warehouse_no", mKkWarehouseNo);
    rc = sqlExec(strSql);
    if (sqlRowNum <= 0) {
      errmsg(this.sqlErrtext);
      return rc;
    }
    // }

    return rc;
  }

}
