/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/06/13  V1.00.01   Ray Ho        Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* * 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1120Func extends FuncEdit {
  private String PROGNAME = "新貴通貴賓室申請參數維護處理程式108/06/13 V1.00.01";
  String groupCode, cardType, vipKind, vipGroupCode, binType;
  String controlTabName = "mkt_ppcard_apply_t";

  public Mktm1120Func(TarokoCommon wr) {
    wp = wr;
    this.conn = wp.getConn();
  }

  // ************************************************************************
  @Override
  public int querySelect() {
    // TODO Auto-generated method
    return 0;
  }

  // ************************************************************************
  @Override
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      groupCode = wp.itemStr("kk_group_code");
      cardType = wp.itemStr("kk_card_type");
      vipKind = wp.itemStr("kk_vip_kind");
      vipGroupCode = wp.itemStr("kk_vip_group_code");
      binType = "";
    } else {
      groupCode = wp.itemStr("group_code");
      cardType = wp.itemStr("card_type");
      vipKind = wp.itemStr("vip_kind");
      vipGroupCode = wp.itemStr("vip_group_code");
      binType = wp.itemStr("bin_type");
    }
    if (this.ibAdd)
      if (groupCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where group_code = ? "
            + " and   card_type = ? " + " and   vip_kind = ? " + " and   vip_group_code = ? "
            + " and   bin_type = ? ";
        Object[] param = new Object[] {groupCode, cardType, vipKind, vipGroupCode, binType};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[團體代號][卡種][貴賓卡][核發貴賓卡團代][卡別] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("first_cond").equals("Y"))
      wp.itemSet("first_cond", "N");
    if (!wp.itemStr("fir_item_ename_bl").equals("Y"))
      wp.itemSet("fir_item_ename_bl", "N");
    if (!wp.itemStr("fir_item_ename_ca").equals("Y"))
      wp.itemSet("fir_item_ename_ca", "N");
    if (!wp.itemStr("fir_item_ename_it").equals("Y"))
      wp.itemSet("fir_item_ename_it", "N");
    if (!wp.itemStr("fir_item_ename_id").equals("Y"))
      wp.itemSet("fir_item_ename_id", "N");
    if (!wp.itemStr("fir_item_ename_ao").equals("Y"))
      wp.itemSet("fir_item_ename_ao", "N");
    if (!wp.itemStr("fir_item_ename_ot").equals("Y"))
      wp.itemSet("fir_item_ename_ot", "N");
    if (!wp.itemStr("fir_amt_cond").equals("Y"))
      wp.itemSet("fir_amt_cond", "N");
    if (!wp.itemStr("fir_cnt_cond").equals("Y"))
      wp.itemSet("fir_cnt_cond", "N");
    if (!wp.itemStr("item_ename_bl").equals("Y"))
      wp.itemSet("item_ename_bl", "N");
    if (!wp.itemStr("item_ename_ca").equals("Y"))
      wp.itemSet("item_ename_ca", "N");
    if (!wp.itemStr("item_ename_it").equals("Y"))
      wp.itemSet("item_ename_it", "N");
    if (!wp.itemStr("item_ename_id").equals("Y"))
      wp.itemSet("item_ename_id", "N");
    if (!wp.itemStr("item_ename_ao").equals("Y"))
      wp.itemSet("item_ename_ao", "N");
    if (!wp.itemStr("item_ename_ot").equals("Y"))
      wp.itemSet("item_ename_ot", "N");
    if (!wp.itemStr("last_amt_cond").equals("Y"))
      wp.itemSet("last_amt_cond", "N");
    if (!wp.itemStr("nofir_cond").equals("Y"))
      wp.itemSet("nofir_cond", "N");
    if (!wp.itemStr("amt_cond").equals("Y"))
      wp.itemSet("amt_cond", "N");
    if (!wp.itemStr("cnt_cond").equals("Y"))
      wp.itemSet("cnt_cond", "N");

    if (!"D".equals(wp.buttonCode)) {
      if (wp.itemStr("first_cond").equals("Y")) {
        if (wp.itemStr("fir_purch_mm").length() == 0)
          wp.itemSet("fir_purch_mm", "0");
        if (wp.itemNum("fir_purch_mm") == 0) {
          errmsg("[首年消費條件-近n月刷卡消費] 月份數不可為0!");
          return;
        }
        if ((!wp.itemStr("fir_item_ename_bl").equals("Y"))
            && (!wp.itemStr("fir_item_ename_ca").equals("Y"))
            && (!wp.itemStr("fir_item_ename_it").equals("Y"))
            && (!wp.itemStr("fir_item_ename_id").equals("Y"))
            && (!wp.itemStr("fir_item_ename_ot").equals("Y"))
            && (!wp.itemStr("fir_item_ename_ao").equals("Y"))) {
          errmsg("[首年消費條件-消費項目] 六大本金類指少要選一項!");
          return;
        }
        if (wp.itemStr("fir_amt_cond").equals("Y")) {
          if (wp.itemStr("fir_tot_amt").length() == 0)
            wp.itemSet("fir_tot_amt", "0");
          if (wp.itemNum("fir_tot_amt") == 0) {
            errmsg("[首年消費條件-累積消費金額] 金額不可為0!");
            return;
          }
        }
        if (wp.itemStr("fir_cnt_cond").equals("Y")) {
          if (wp.itemStr("fir_tot_cnt").length() == 0)
            wp.itemSet("fir_tot_cnt", "0");
          if (wp.itemNum("fir_tot_cnt") == 0) {
            errmsg("[首年消費條件-累積消費筆數] 筆數不可為0!");
            return;
          }
        }
      }

      if ((wp.itemStr("last_amt_cond").equals("Y")) || (wp.itemStr("nofir_cond").equals("Y"))) {
        if ((!wp.itemStr("item_ename_bl").equals("Y")) && (!wp.itemStr("item_ename_ca").equals("Y"))
            && (!wp.itemStr("item_ename_it").equals("Y"))
            && (!wp.itemStr("item_ename_id").equals("Y"))
            && (!wp.itemStr("item_ename_ot").equals("Y"))
            && (!wp.itemStr("item_ename_ao").equals("Y"))) {
          errmsg("[新貴通核卡次年-消費項目] 六大本金類指少要選一項!");
          return;
        }
        if (wp.itemStr("last_amt_cond").equals("Y")) {
          if (wp.itemStr("last_tot_amt").length() == 0)
            wp.itemSet("last_tot_amt", "0");
          if (wp.itemNum("last_tot_amt") == 0) {
            errmsg("[前一年~累積消費金額] 金額不可為0!");
            return;
          }
        }
        if (wp.itemStr("nofir_cond").equals("Y")) {
          if (wp.itemStr("purch_mm").length() == 0)
            wp.itemSet("purch_mm", "0");
          if (wp.itemNum("purch_mm") == 0) {
            errmsg("[非當年消費條件-近n月刷卡消費] 月份數不可為0!");
            return;
          }
          if (wp.itemStr("amt_cond").equals("Y")) {
            if (wp.itemStr("tot_amt").length() == 0)
              wp.itemSet("tot_amt", "0");
            if (wp.itemNum("tot_amt") == 0) {
              errmsg("[非當年消費條件-累積消費金額] 金額不可為0!");
              return;
            }
          }
          if (wp.itemStr("cnt_cond").equals("Y")) {
            if (wp.itemStr("tot_cnt").length() == 0)
              wp.itemSet("tot_cnt", "0");
            if (wp.itemNum("tot_cnt") == 0) {
              errmsg("[非當年消費條件-累積消費筆數] 筆數不可為0!");
              return;
            }
          }
        }
      }

    }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

    String audType = wp.buttonCode;

    if (!"A".equals(audType)) {
      groupCode = wp.itemStr("group_code");
      cardType = wp.itemStr("card_type");
      vipKind = wp.itemStr("vip_kind");
      vipGroupCode = wp.itemStr("vip_group_code");
      binType = wp.itemStr("bin_type");
    }

    strSql = " insert into  " + controlTabName + " (" + " group_code, " + " card_type, "
        + " proj_desc, " + " major_flag, " + " first_cond, " + " fir_purch_mm, "
        + " fir_item_ename_bl, " + " fir_item_ename_ca, " + " fir_item_ename_it, "
        + " fir_it_type, " + " fir_item_ename_id, " + " fir_item_ename_ao, "
        + " fir_item_ename_ot, " + " fir_min_amt, " + " fir_amt_cond, " + " fir_tot_amt, "
        + " fir_cnt_cond, " + " fir_tot_cnt, " + " item_ename_bl, " + " item_ename_ca, "
        + " item_ename_it, " + " it_type, " + " item_ename_id, " + " item_ename_ao, "
        + " item_ename_ot, " + " last_amt_cond, " + " last_tot_amt, " + " nofir_cond, "
        + " purch_mm, " + " min_amt, " + " amt_cond, " + " tot_amt, " + " cnt_cond, " + " tot_cnt, "
        + " vip_kind, " + " vip_group_code, " + " bin_type, " + " card_purch_code, " + " aud_type, "
        + " crt_date, " + " crt_user, " + " mod_seqno, " + " mod_pgm " + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "?)";

    Object[] param = new Object[] {groupCode, cardType, wp.itemStr("proj_desc"), wp.itemStr("major_flag"),
        wp.itemStr("first_cond"), wp.itemNum("fir_purch_mm"), wp.itemStr("fir_item_ename_bl"),
        wp.itemStr("fir_item_ename_ca"), wp.itemStr("fir_item_ename_it"), wp.itemStr("fir_it_type"),
        wp.itemStr("fir_item_ename_id"), wp.itemStr("fir_item_ename_ao"),
        wp.itemStr("fir_item_ename_ot"), wp.itemNum("fir_min_amt"), wp.itemStr("fir_amt_cond"),
        wp.itemNum("fir_tot_amt"), wp.itemStr("fir_cnt_cond"), wp.itemNum("fir_tot_cnt"),
        wp.itemStr("item_ename_bl"), wp.itemStr("item_ename_ca"), wp.itemStr("item_ename_it"),
        wp.itemStr("it_type"), wp.itemStr("item_ename_id"), wp.itemStr("item_ename_ao"),
        wp.itemStr("item_ename_ot"), wp.itemStr("last_amt_cond"), wp.itemNum("last_tot_amt"),
        wp.itemStr("nofir_cond"), wp.itemNum("purch_mm"), wp.itemNum("min_amt"),
        wp.itemStr("amt_cond"), wp.itemNum("tot_amt"), wp.itemStr("cnt_cond"),
        wp.itemNum("tot_cnt"), vipKind, vipGroupCode, binType, wp.itemStr("card_purch_code"), audType, wp.loginUser,
        wp.modSeqno(), wp.modPgm()};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "proj_desc = ?, " + "major_flag = ?, "
        + "first_cond = ?, " + "fir_purch_mm = ?, " + "fir_item_ename_bl = ?, "
        + "fir_item_ename_ca = ?, " + "fir_item_ename_it = ?, " + "fir_it_type = ?, "
        + "fir_item_ename_id = ?, " + "fir_item_ename_ao = ?, " + "fir_item_ename_ot = ?, "
        + "fir_min_amt = ?, " + "fir_amt_cond = ?, " + "fir_tot_amt = ?, " + "fir_cnt_cond = ?, "
        + "fir_tot_cnt = ?, " + "item_ename_bl = ?, " + "item_ename_ca = ?, "
        + "item_ename_it = ?, " + "it_type = ?, " + "item_ename_id = ?, " + "item_ename_ao = ?, "
        + "item_ename_ot = ?, " + "last_amt_cond = ?, " + "last_tot_amt = ?, " + "nofir_cond = ?, "
        + "purch_mm = ?, " + "min_amt = ?, " + "amt_cond = ?, " + "tot_amt = ?, " + "cnt_cond = ?, "
        + "tot_cnt = ?, " + "card_purch_code = ?, " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("proj_desc"), wp.itemStr("major_flag"),
        wp.itemStr("first_cond"), wp.itemNum("fir_purch_mm"), wp.itemStr("fir_item_ename_bl"),
        wp.itemStr("fir_item_ename_ca"), wp.itemStr("fir_item_ename_it"), wp.itemStr("fir_it_type"),
        wp.itemStr("fir_item_ename_id"), wp.itemStr("fir_item_ename_ao"),
        wp.itemStr("fir_item_ename_ot"), wp.itemNum("fir_min_amt"), wp.itemStr("fir_amt_cond"),
        wp.itemNum("fir_tot_amt"), wp.itemStr("fir_cnt_cond"), wp.itemNum("fir_tot_cnt"),
        wp.itemStr("item_ename_bl"), wp.itemStr("item_ename_ca"), wp.itemStr("item_ename_it"),
        wp.itemStr("it_type"), wp.itemStr("item_ename_id"), wp.itemStr("item_ename_ao"),
        wp.itemStr("item_ename_ot"), wp.itemStr("last_amt_cond"), wp.itemNum("last_tot_amt"),
        wp.itemStr("nofir_cond"), wp.itemNum("purch_mm"), wp.itemNum("min_amt"),
        wp.itemStr("amt_cond"), wp.itemNum("tot_amt"), wp.itemStr("cnt_cond"),
        wp.itemNum("tot_cnt"), wp.itemStr("card_purch_code"), wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0)
      errmsg("刪除 " + controlTabName + " 錯誤");

    return rc;
  }
  // ************************************************************************

} // End of class
