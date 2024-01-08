/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                     
* 112/04/21  V1.00.04  Ryan       增加名單匯入功能 ,增加LIST_COND,LIST_FLAG,LIST_USE_SEL欄位維護  *    
* 112/04/24  V1.00.05  Ryan       增加辦別活動序號為00時不可新增活動
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6250Func extends FuncEdit {
  private String PROGNAME = "首刷禮活動回饋參數處理程式108/12/12 V1.00.01";
  String activeCode, activeSeq;
  String orgControlTabName = "mkt_fstp_parmseq";
  String controlTabName = "mkt_fstp_parmseq_t";

  public Mktm6250Func(TarokoCommon wr) {
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
    String procTabName = "";
    procTabName = wp.itemStr("control_tab_name");
    strSql = " select " + " level_seq, " + " record_cond, " + " record_group_no, "
        + " active_type, " + " bonus_type, " + " tax_flag, " + " fund_code, " + " group_type, "
        + " prog_code, " + " prog_s_date, " + " prog_e_date, " + " gift_no, " + " spec_gift_no, "
        + " per_amt_cond, " + " per_amt, " + " perday_cnt_cond, " + " perday_cnt, "
        + " sum_amt_cond, " + " sum_amt, " + " sum_cnt_cond, " + " sum_cnt, " + " threshold_sel, "
        + " purchase_type_sel, " + " purchase_amt_s1, " + " purchase_amt_e1, " + " feedback_amt_1, "
        + " purchase_amt_s2, " + " purchase_amt_e2, " + " feedback_amt_2, " + " purchase_amt_s3, "
        + " purchase_amt_e3, " + " feedback_amt_3, " + " purchase_amt_s4, " + " purchase_amt_e4, "
        + " feedback_amt_4, " + " purchase_amt_s5, " + " purchase_amt_e5, " + " feedback_amt_5, "
        + " feedback_limit, "
        + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno,"
        + " stop_date,"+ " stop_desc,"+ " purchase_days,"+ " merchant_sel,"
        + " mcht_group_sel,"+ " pur_date_sel, "+ " stop_flag, "
        + " list_cond, "
        + " list_flag, "
        + " list_use_sel "
        + " from "
        + procTabName + " where rowid = ? ";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlSelect(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("查無資料，讀取 " + controlTabName + " 失敗");

    return 1;
  }

  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd) {
      activeCode = wp.itemStr("active_code");
      if (empty(activeCode)) {
        errmsg("活動代碼 不可空白");
        return;
      }
      activeSeq = wp.itemStr("active_seq");
      if (empty(activeSeq)) {
        errmsg("活動序號 不可空白");
        return;
      }
      if ("00".equals(activeSeq)) {
          errmsg("活動序號'00'僅為單一贈品活動使用(mktm6240)!!");
          return;
      }
    } else {
      activeCode = wp.itemStr("active_code");
      activeSeq = wp.itemStr("active_seq");
    }
    if (wp.respHtml.indexOf("_nadd") > 0)
      if (this.ibAdd)
        if (activeCode.length() > 0) {
          strSql = "select count(*) as qua " + "from " + orgControlTabName
              + " where active_code = ? " + "and   active_seq = ? ";
          Object[] param = new Object[] {activeCode, activeSeq};
          sqlSelect(strSql, param);
          int qua = Integer.parseInt(colStr("qua"));
          if (qua > 0) {
            errmsg("[活動代碼][活動序號] 不可重複(" + orgControlTabName + "), 請重新輸入!");
            return;
          }
        }

    if (this.ibAdd)
      if (activeCode.length() > 0) {
        strSql = "select count(*) as qua " + "from " + controlTabName + " where active_code = ? "
            + " and   active_seq = ? ";
        Object[] param = new Object[] {activeCode, activeSeq};
        sqlSelect(strSql, param);
        int qua = Integer.parseInt(colStr("qua"));
        if (qua > 0) {
          errmsg("[活動代碼][活動序號] 不可重複(" + controlTabName + ") ,請重新輸入!");
          return;
        }
      }

    if (this.ibUpdate)
    {
     if ((wp.itemStr("merchant_sel").equals("1"))||
         (wp.itemStr("merchant_sel").equals("2")))
        {
         if (wp.colNum("merchant_sel_cnt")==0)
            {
             errmsg("[特店代號] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
     if ((wp.itemStr("mcht_group_sel").equals("1"))||
         (wp.itemStr("mcht_group_sel").equals("2")))
        {
         if (wp.colNum("mcht_group_sel_cnt")==0)
            {
             errmsg("[特店群組] 明細沒有設定, 筆數不可為 0  !");
             return;
            }
        }
    }   
    
    if (!wp.itemStr("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");
    
    if (!wp.itemStr("record_cond").equals("Y"))
      wp.itemSet("record_cond", "N");
    if (!wp.itemStr("per_amt_cond").equals("Y"))
      wp.itemSet("per_amt_cond", "N");
    if (!wp.itemStr("perday_cnt_cond").equals("Y"))
      wp.itemSet("perday_cnt_cond", "N");
    if (!wp.itemStr("sum_amt_cond").equals("Y"))
      wp.itemSet("sum_amt_cond", "N");
    if (!wp.itemStr("sum_cnt_cond").equals("Y"))
      wp.itemSet("sum_cnt_cond", "N");

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("active_code").length() == 0) {
        errmsg("[" + colStr("active_code") + "]首刷禮活動代碼未建立 !");
        return;
      }

      if (wp.itemStr("active_seq").length() != 2) {
        errmsg("[" + colStr("active_code") + "]首刷禮活動序號需為2碼資料 !");
        return;
      }
      if (wp.itemStr("active_seq").compareTo("00") == 0) {
        errmsg("[" + colStr("active_code") + "]首刷禮活動序號不可為'00' !");
        return;
      }

      if (wp.itemStr("active_type").equals("3")) {
        if ((wp.itemStr("group_type").length() == 0) || (wp.itemStr("prog_code1").length() == 0)
            || (wp.itemStr("gift_no").length() == 0)) {
          errmsg("[回饋型態: 群組代號,活動代碼,贈品代碼 必須輸入 !");
          return;
        }
      }

      if (wp.itemStr("active_type").equals("4")) {
        if (wp.itemStr("spec_gift_no").length() == 0) {
          errmsg("[回饋型態: 商品代號 必須輸入 !");
          return;
        }
      }
      if (wp.itemStr("per_amt_cond").equals("Y")) {
        if (wp.itemStr("per_amt").length() == 0)
          wp.itemSet("per_amt", "0");
        if (wp.itemNum("per_amt") == 0) {
          errmsg("[單筆最低消費金額] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("sum_amt_cond").equals("Y")) {
        if (wp.itemStr("sum_amt").length() == 0)
          wp.itemSet("sum_amt", "0");
        if (wp.itemNum("sum_amt") == 0) {
          errmsg("[累積最低消費金額] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("purch_rec_amt_cond").equals("Y")) {
        if (wp.itemStr("purch_rec_amt").length() == 0)
          wp.itemSet("purch_rec_amt", "0");
        if (wp.itemNum("purch_rec_amt") == 0) {
          errmsg("[累積最低消費筆數] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("sum_cnt_cond").equals("Y")) {
        if (wp.itemStr("sum_cnt").length() == 0)
          wp.itemSet("sum_cnt", "0");
        if (wp.itemNum("sum_cnt") == 0) {
          errmsg("[累積最低消費筆數] 不可為 0 !");
          return;
        }
      }

      if (wp.itemStr("purchase_amt_e1").length() == 0)
        wp.itemSet("purchase_amt_e1", "0");
      if (wp.itemNum("purchase_amt_e1") == 0) {
        errmsg("[門檻一:迄累積金額] 不可為 0 !");
        return;
      }

      if (wp.itemStr("feedback_amt_1").length() == 0)
        wp.itemSet("feedback_amt_1", "0");
      if (wp.itemNum("feedback_amt_1") == 0) {
        errmsg("[門檻一:給點數/現金回饋/贈品] 不可為 0 !");
        return;
      }
      

      strSql = "select active_code,purchase_days,mcht_seq_flag " + " from mkt_fstp_parm " + " where active_code =  ? ";
      Object[] param = new Object[] {wp.itemStr("active_code")};
      sqlSelect(strSql, param);

      if (sqlRowNum <= 0) {
        errmsg("[" + colStr("active_code") + "]首刷禮活動代碼尚未建立 !");
        return;
      }
      if (wp.itemStr("pur_date_sel").equals("2"))
      {
       if (wp.itemStr("purchase_days").length()==0)  wp.itemSet("purchase_Days","0");
       if (wp.itemNum("purchase_days")>colNum("purchase_days"))
         {
          errmsg("[消費日期期間 發卡日次日起] 不可大於主參數["+(int)colNum("purchase_days")+"]值 !");
          return;
         }
      }     
      
    }


    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s1").length() == 0)
        wp.itemSet("purchase_amt_s1", "0");
      if (wp.itemStr("PURCHASE_AMT_E1").length() == 0)
        wp.itemSet("PURCHASE_AMT_E1", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s1")) > Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_E1"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E1")) != 0)) {
        errmsg("門檻一：(" + wp.itemStr("purchase_amt_s1") + ")>purchase_amt_e1("
            + wp.itemStr("PURCHASE_AMT_E1") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e1").length() == 0)
        wp.itemSet("purchase_amt_e1", "0");
      if (wp.itemStr("PURCHASE_AMT_S2").length() == 0)
        wp.itemSet("PURCHASE_AMT_S2", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e1")) >= Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_S2"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S2")) != 0)) {
        errmsg("purchase_amt_e1(" + wp.itemStr("purchase_amt_e1") + ")>=purchase_amt_s2("
            + wp.itemStr("PURCHASE_AMT_S2") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s2").length() == 0)
        wp.itemSet("purchase_amt_s2", "0");
      if (wp.itemStr("PURCHASE_AMT_E2").length() == 0)
        wp.itemSet("PURCHASE_AMT_E2", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s2")) > Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_E2"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E2")) != 0)) {
        errmsg("門檻二：(" + wp.itemStr("purchase_amt_s2") + ")>purchase_amt_e2("
            + wp.itemStr("PURCHASE_AMT_E2") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e2").length() == 0)
        wp.itemSet("purchase_amt_e2", "0");
      if (wp.itemStr("PURCHASE_AMT_S3").length() == 0)
        wp.itemSet("PURCHASE_AMT_S3", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e2")) >= Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_S3"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S3")) != 0)) {
        errmsg("purchase_amt_e2(" + wp.itemStr("purchase_amt_e2") + ")>=purchase_amt_s3("
            + wp.itemStr("PURCHASE_AMT_S3") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s3").length() == 0)
        wp.itemSet("purchase_amt_s3", "0");
      if (wp.itemStr("PURCHASE_AMT_E3").length() == 0)
        wp.itemSet("PURCHASE_AMT_E3", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s3")) > Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_E3"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E3")) != 0)) {
        errmsg("門檻三：(" + wp.itemStr("purchase_amt_s3") + ")>purchase_amt_e3("
            + wp.itemStr("PURCHASE_AMT_E3") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e3").length() == 0)
        wp.itemSet("purchase_amt_e3", "0");
      if (wp.itemStr("PURCHASE_AMT_S4").length() == 0)
        wp.itemSet("PURCHASE_AMT_S4", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e3")) >= Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_S4"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S4")) != 0)) {
        errmsg("purchase_amt_e3(" + wp.itemStr("purchase_amt_e3") + ")>=purchase_amt_s4("
            + wp.itemStr("PURCHASE_AMT_S4") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s4").length() == 0)
        wp.itemSet("purchase_amt_s4", "0");
      if (wp.itemStr("PURCHASE_AMT_E4").length() == 0)
        wp.itemSet("PURCHASE_AMT_E4", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s4")) > Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_E4"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E4")) != 0)) {
        errmsg("門檻四：(" + wp.itemStr("purchase_amt_s4") + ")>purchase_amt_e4("
            + wp.itemStr("PURCHASE_AMT_E4") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e4").length() == 0)
        wp.itemSet("purchase_amt_e4", "0");
      if (wp.itemStr("PURCHASE_AMT_S5").length() == 0)
        wp.itemSet("PURCHASE_AMT_S5", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e4")) >= Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_S5"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S5")) != 0)) {
        errmsg("purchase_amt_e4(" + wp.itemStr("purchase_amt_e4") + ")>=purchase_amt_s5("
            + wp.itemStr("PURCHASE_AMT_S5") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_s5").length() == 0)
        wp.itemSet("purchase_amt_s5", "0");
      if (wp.itemStr("PURCHASE_AMT_E4").length() == 0)
        wp.itemSet("PURCHASE_AMT_E4", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_s5")) > Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_E4"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_E4")) != 0)) {
        errmsg("門檻五：(" + wp.itemStr("purchase_amt_s5") + ")>purchase_amt_e4("
            + wp.itemStr("PURCHASE_AMT_E4") + ") 起迄值錯誤!");
        return;
      }
    }

    if ((this.ibAdd) || (this.ibUpdate)) {
      if (wp.itemStr("purchase_amt_e5").length() == 0)
        wp.itemSet("purchase_amt_e5", "0");
      if (wp.itemStr("PURCHASE_AMT_S5").length() == 0)
        wp.itemSet("PURCHASE_AMT_S5", "0");
      if (Double.parseDouble(wp.itemStr("purchase_amt_e5")) >= Double
          .parseDouble(wp.itemStr("PURCHASE_AMT_S5"))
          && (Double.parseDouble(wp.itemStr("PURCHASE_AMT_S5")) != 0)) {
        errmsg("purchase_amt_e5(" + wp.itemStr("purchase_amt_e5") + ")>=purchase_amt_s5("
            + wp.itemStr("PURCHASE_AMT_S5") + ") 起迄值錯誤!");
        return;
      }
    }

    if (checkDecnum(wp.itemStr("per_amt"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("perday_cnt"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("sum_amt"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("sum_cnt"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s1"), 11, 3) != 0) {
      errmsg("門檻一： 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e1"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_amt_1"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s2"), 11, 3) != 0) {
      errmsg("門檻二： 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e2"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_amt_2"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s3"), 11, 3) != 0) {
      errmsg("門檻三： 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e3"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_amt_3"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s4"), 11, 3) != 0) {
      errmsg("門檻四： 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e4"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_amt_4"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_s5"), 11, 3) != 0) {
      errmsg("門檻五： 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("purchase_amt_e5"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if (checkDecnum(wp.itemStr("feedback_amt_5"), 11, 3) != 0) {
      errmsg(" 格式超出範圍 : [11][3]");
      return;
    }

    if ((this.ibAdd) || (this.ibUpdate))
      if (wp.itemEmpty("level_seq")) {
        errmsg("贈品階層序號: 不可空白");
        return;
      }


    if (this.isAdd())
      return;

  }

  // ************************************************************************
  @Override
  public int dbInsert() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("A");
    dataCheck();
    if (rc != 1)
      return rc;

   try { 
    dbInsert_D3T();
    dbInsert_I3T();
   } catch(Exception e) {
	   
   }
    strSql = " insert into  " + controlTabName + " (" + " active_code, " + " active_seq, "
        + " aud_type, " + " level_seq, " + " record_cond, " + " record_group_no, "
        + " active_type, " + " bonus_type, " + " tax_flag, " + " fund_code, " + " group_type, "
        + " prog_code, " + " prog_s_date, " + " prog_e_date, " + " gift_no, " + " spec_gift_no, "
        + " per_amt_cond, " + " per_amt, " + " perday_cnt_cond, " + " perday_cnt, "
        + " sum_amt_cond, " + " sum_amt, " + " sum_cnt_cond, " + " sum_cnt, " + " threshold_sel, "
        + " purchase_type_sel, " + " purchase_amt_s1, " + " purchase_amt_e1, " + " feedback_amt_1, "
        + " purchase_amt_s2, " + " purchase_amt_e2, " + " feedback_amt_2, " + " purchase_amt_s3, "
        + " purchase_amt_e3, " + " feedback_amt_3, " + " purchase_amt_s4, " + " purchase_amt_e4, "
        + " feedback_amt_4, " + " purchase_amt_s5, " + " purchase_amt_e5, " + " feedback_amt_5, "
        + " feedback_limit, " + " crt_date, " + " crt_user, " + " mod_seqno, "
        + " mod_time,mod_user,mod_pgm,"
        + " stop_date,"+ " stop_desc,"+ " purchase_days,"
        + " merchant_sel,"+ " mcht_group_sel," + " pur_date_sel,"+ " stop_flag, "
        + " list_cond, "
        + " list_flag, "
        + " list_use_sel "
        + " ) values ("
        + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + "to_char(sysdate,'yyyymmdd')," + "?," + "?," + "sysdate,?,?,?,?,?,?,?,?,?,?,?,?)";

    Object[] param = new Object[] {activeCode, activeSeq, wp.itemStr("aud_type"), wp.itemStr("level_seq"),
        wp.itemStr("record_cond"), wp.itemStr("record_group_no"), wp.itemStr("active_type"),
        wp.itemStr("bonus_type"), wp.itemStr("tax_flag"), wp.itemStr("fund_code"),
        wp.itemStr("group_type"), wp.itemStr("prog_code"), wp.itemStr("prog_s_date"),
        wp.itemStr("prog_e_date"), wp.itemStr("gift_no"), wp.itemStr("spec_gift_no"),
        wp.itemStr("per_amt_cond"), wp.itemNum("per_amt"), wp.itemStr("perday_cnt_cond"),
        wp.itemNum("perday_cnt"), wp.itemStr("sum_amt_cond"), wp.itemNum("sum_amt"),
        wp.itemStr("sum_cnt_cond"), wp.itemNum("sum_cnt"), wp.itemStr("threshold_sel"),
        wp.itemStr("purchase_type_sel"), wp.itemNum("purchase_amt_s1"),
        wp.itemNum("purchase_amt_e1"), wp.itemNum("feedback_amt_1"), wp.itemNum("purchase_amt_s2"),
        wp.itemNum("purchase_amt_e2"), wp.itemNum("feedback_amt_2"), wp.itemNum("purchase_amt_s3"),
        wp.itemNum("purchase_amt_e3"), wp.itemNum("feedback_amt_3"), wp.itemNum("purchase_amt_s4"),
        wp.itemNum("purchase_amt_e4"), wp.itemNum("feedback_amt_4"), wp.itemNum("purchase_amt_s5"),
        wp.itemNum("purchase_amt_e5"), wp.itemNum("feedback_amt_5"), wp.itemNum("feedback_limit"),
        wp.loginUser, wp.modSeqno(), wp.loginUser, wp.modPgm(),wp.itemStr("stop_date"),
        wp.itemStr("stop_desc"),wp.itemNum("purchase_days"),wp.itemStr("merchant_sel"),
        wp.itemStr("mcht_group_sel"),wp.itemStr("pur_date_sel"),wp.itemStr("stop_flag"),
        wp.itemNvl("list_cond","N"),
        wp.itemStr("list_flag"),
        wp.itemNvl("list_use_sel","0")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("新增 " + controlTabName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  @Override
  public int dbUpdate() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("U");
    dataCheck();
    if (rc != 1)
      return rc;

    strSql = "update " + controlTabName + " set " + "level_seq = ?, " + "record_cond = ?, "
        + "record_group_no = ?, " + "active_type = ?, " + "bonus_type = ?, " + "tax_flag = ?, "
        + "fund_code = ?, " + "group_type = ?, " + "gift_no = ?, " + "spec_gift_no = ?, "
        + "per_amt_cond = ?, " + "per_amt = ?, " + "perday_cnt_cond = ?, " + "perday_cnt = ?, "
        + "sum_amt_cond = ?, " + "sum_amt = ?, " + "sum_cnt_cond = ?, " + "sum_cnt = ?, "
        + "threshold_sel = ?, " + "purchase_type_sel = ?, " + "purchase_amt_s1 = ?, "
        + "purchase_amt_e1 = ?, " + "feedback_amt_1 = ?, " + "purchase_amt_s2 = ?, "
        + "purchase_amt_e2 = ?, " + "feedback_amt_2 = ?, " + "purchase_amt_s3 = ?, "
        + "purchase_amt_e3 = ?, " + "feedback_amt_3 = ?, " + "purchase_amt_s4 = ?, "
        + "purchase_amt_e4 = ?, " + "feedback_amt_4 = ?, " + "purchase_amt_s5 = ?, "
        + "purchase_amt_e5 = ?, " + "feedback_amt_5 = ?, " + "feedback_limit = ?, "
        + "crt_user  = ?, " + "crt_date  = to_char(sysdate,'yyyymmdd'), " + "mod_user  = ?, "
        + "mod_seqno = nvl(mod_seqno,0)+1, " + "mod_time  = sysdate, " + "mod_pgm   = ?,"
        + "stop_date = ?,"+ "stop_desc = ?,"+ "purchase_days = ?,"
        + "merchant_sel = ?,"+ "mcht_group_sel = ?,"+ "pur_date_sel = ?,"
        + "stop_flag = ?, "
        + "list_cond = ?, "
        + "list_flag = ?, "
        + "list_use_sel = ? "
        + "where rowid = ? " + "and   mod_seqno = ? ";

    Object[] param = new Object[] {wp.itemStr("level_seq"), wp.itemStr("record_cond"),
        wp.itemStr("record_group_no"), wp.itemStr("active_type"), wp.itemStr("bonus_type"),
        wp.itemStr("tax_flag"), wp.itemStr("fund_code"), wp.itemStr("group_type"),
        wp.itemStr("gift_no"), wp.itemStr("spec_gift_no"), wp.itemStr("per_amt_cond"),
        wp.itemNum("per_amt"), wp.itemStr("perday_cnt_cond"), wp.itemNum("perday_cnt"),
        wp.itemStr("sum_amt_cond"), wp.itemNum("sum_amt"), wp.itemStr("sum_cnt_cond"),
        wp.itemNum("sum_cnt"), wp.itemStr("threshold_sel"), wp.itemStr("purchase_type_sel"),
        wp.itemNum("purchase_amt_s1"), wp.itemNum("purchase_amt_e1"), wp.itemNum("feedback_amt_1"),
        wp.itemNum("purchase_amt_s2"), wp.itemNum("purchase_amt_e2"), wp.itemNum("feedback_amt_2"),
        wp.itemNum("purchase_amt_s3"), wp.itemNum("purchase_amt_e3"), wp.itemNum("feedback_amt_3"),
        wp.itemNum("purchase_amt_s4"), wp.itemNum("purchase_amt_e4"), wp.itemNum("feedback_amt_4"),
        wp.itemNum("purchase_amt_s5"), wp.itemNum("purchase_amt_e5"), wp.itemNum("feedback_amt_5"),
        wp.itemNum("feedback_limit"), wp.loginUser, wp.loginUser, wp.itemStr("mod_pgm"),
        wp.itemStr("stop_date"),wp.itemStr("stop_desc"), wp.itemNum("purchase_days"),
        wp.itemStr("merchant_sel"), wp.itemStr("mcht_group_sel"),
        wp.itemStr("pur_date_sel"),wp.itemStr("stop_flag"),
        wp.itemNvl("list_cond","N"),
        wp.itemStr("list_flag"),
        wp.itemNvl("list_use_sel","0"),
        wp.itemRowId("rowid"), wp.itemNum("mod_seqno")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      errmsg("更新 " + controlTabName + " 錯誤");

    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    return rc;
  }

  // ************************************************************************
  @Override
  public int dbDelete() {
    rc = dataSelect();
    if (rc != 1)
      return rc;
    actionInit("D");
    dataCheck();
    if (rc != 1)
      return rc;
    
    try {
		dbInsert_D3T();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    strSql = "delete " + controlTabName + " " + "where rowid = ?";

    Object[] param = new Object[] {wp.itemRowId("rowid")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (sqlRowNum <= 0) {
      errmsg("刪除 " + controlTabName + " 錯誤");
      return (-1);
    }

    return rc;
  }
  
  // ************************************************************************
  public int dbDeleteD2List(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where active_code = ? and active_seq = ? ";

    Object[] param = new Object[] {wp.itemStr("active_code"),wp.colStr("active_seq")};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }
  
  // ************************************************************************
  public int dbInsertI2List(String tableName, String[] columnCol, String[] columnDat) {
    String[] columnData = new String[50];
    String stra = "", strb = "";
    int skipLine = 0;
    long listCnt = 50;
    strSql = " insert into  " + tableName + " (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql + " mod_time,mod_pgm " + " ) values (";
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      strSql = strSql + "?,";
    }
    strSql = strSql + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 = new Object[50];
    for (int inti = 0; inti < listCnt; inti++) {
      stra = columnCol[inti];
      if (stra.length() == 0)
        continue;
      stra = columnDat[inti];
      param1[skipLine] = stra;
      skipLine++;
    }
    param1[skipLine++] = wp.sysDate + wp.sysTime;
    param1[skipLine++] = wp.modPgm();
    Object[] param = Arrays.copyOf(param1, skipLine);
    wp.dupRecord = "Y";
    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;

    return rc;
  }
  
//************************************************************************
 public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg) {
   dateTime();
   busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
   busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
   comr.setConn(wp);

   if (!comm.isNumber(errMsg[10]))
     errMsg[10] = "0";
   if (!comm.isNumber(errMsg[1]))
     errMsg[1] = "0";
   if (!comm.isNumber(errMsg[2]))
     errMsg[2] = "0";

   strSql = " insert into ecs_media_errlog (" + " crt_date, " + " crt_time, " + " file_name, "
       + " unit_code, " + " main_desc, " + " error_seq, " + " error_desc, " + " line_seq, "
       + " column_seq, " + " column_data, " + " trans_seqno, " + " column_desc, "
       + " program_code, " + " mod_time, " + " mod_pgm " + " ) values (" + "?,?,?,?,?,?,?,?,?,?," // 10
                                                                                                  // record
       + "?,?,?," // 4 trvotfd
       + "timestamp_format(?,'yyyymmddhh24miss'),?)";

   Object[] param = new Object[] {wp.sysDate, wp.sysTime, wp.itemStr("zz_file_name"),
       comr.getObjectOwner("3", wp.modPgm()), errMsg[0], Integer.valueOf(errMsg[1]), errMsg[4],
       Integer.valueOf(errMsg[10]), Integer.valueOf(errMsg[2]), errMsg[3], tranSeqStr, errMsg[5],
       wp.modPgm(), wp.sysDate + wp.sysTime, wp.modPgm()};

   wp.logSql = false;
   sqlExec(strSql, param);
   if (sqlRowNum <= 0)
     errmsg("新增 ecs_media_errlog 錯誤");

   return rc;
 }
 
 // ************************************************************************
 public int dbInsertEcsNotifyLog(String tranSeqStr, int errorCnt) {
   busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
   comr.setConn(wp);
   dateTime();
   strSql = " insert into ecs_notify_log (" + " crt_date, " + " crt_time, " + " unit_code, "
       + " obj_type, " + " notify_head, " + " notify_name, " + " notify_desc1, "
       + " notify_desc2, " + " trans_seqno, " + " mod_time, " + " mod_pgm " + " ) values ("
       + "?,?,?,?,?,?,?,?,?," // 9 record
       + "timestamp_format(?,'yyyymmddhh24miss'),?)";

   Object[] param = new Object[] {wp.sysDate, wp.sysTime, comr.getObjectOwner("3", wp.modPgm()),
       "3", "媒體檔轉入資料有誤(只記錄前100筆)", "媒體檔名:" + wp.itemStr("zz_file_name"),
       "程式 " + wp.modPgm() + " 轉 " + wp.itemStr("zz_file_name") + " 有" + errorCnt + " 筆錯誤",
       "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤", tranSeqStr, wp.sysDate + wp.sysTime, wp.modPgm()};

   sqlExec(strSql, param);
   if (sqlRowNum <= 0)
     errmsg("新增 ecs_modify_log 錯誤");
   return rc;
 }
 
 // ************************************************************************
 public int dbInsertI2Aaa1(String tableName, String[] columnCol, String[] columnDat) {
   String[] columnData = new String[50];
   String stra = "", strb = "";
   int skipLine = 0;
   long listCnt = 50;
   strSql = " insert into  " + tableName + " (";
   for (int inti = 0; inti < listCnt; inti++) {
     stra = columnCol[inti];
     if (stra.length() == 0)
       continue;
     strSql = strSql + stra + ",";
   }

   strSql = strSql + " mod_user, " + " mod_time,mod_pgm " + " ) values (";
   for (int inti = 0; inti < listCnt; inti++) {
     stra = columnCol[inti];
     if (stra.length() == 0)
       continue;
     strSql = strSql + "?,";
   }
   strSql = strSql + "?," + "timestamp_format(?,'yyyymmddhh24miss'),?)";

   Object[] param1 = new Object[50];
   for (int inti = 0; inti < listCnt; inti++) {
     stra = columnCol[inti];
     if (stra.length() == 0)
       continue;
     stra = columnDat[inti];
     param1[skipLine] = stra;
     skipLine++;
   }
   param1[skipLine++] = wp.loginUser;
   param1[skipLine++] = wp.sysDate + wp.sysTime;
   param1[skipLine++] = wp.modPgm();
   Object[] param = Arrays.copyOf(param1, skipLine);
   wp.logSql = false;
   sqlExec(strSql, param);
   if (sqlRowNum <= 0)
     rc = 0;
   else
     rc = 1;

   return rc;
 }
  
  // ************************************************************************
  public int dbDeleteD2Aaa1(String tableName) throws Exception {
    strSql = "delete  " + tableName + " " + "where table_name = ? " + "and   data_key = ? "
        + "and   data_type = ? ";

    Object[] param = new Object[] {"MKT_FSTP_PARM", wp.itemStr("active_code")+wp.itemStr("active_seq"), "7"};

    sqlExec(strSql, param);
    if (sqlRowNum <= 0)
      rc = 0;
    else
      rc = 1;
    if (rc != 1)
      errmsg("刪除 " + tableName + " 錯誤");

    return rc;
  }

  // ************************************************************************
  public int checkDecnum(String decStr, int colLength, int colScale) {
    String[] parts = decStr.split("[.^]");
    if ((parts.length == 1 && parts[0].length() > colLength)
        || (parts.length == 2 && (parts[0].length() > colLength || parts[1].length() > colScale)))
      return (1);
    return (0);
  }
  // ************************************************************************
//************************************************************************
public int dbInsertI3() throws Exception
{
  msgOK();

  String data_type="";
  if (wp.respHtml.equals("mktm6250_mrcd"))
     data_type = "7" ;
 String isSql = "insert into MKT_BN_DATA_T ( "
         + "table_name, "
         + "data_key, "
         + "data_type, "
         + "data_code,"
         + "data_code2,"
         + "crt_date, "
         + "crt_user, "
         + " mod_time, "
         + " mod_user, "
         + " mod_seqno, "
         + " mod_pgm "
         + ") values ("
         + "'MKT_FSTP_PARMSEQ', "
         + "?, "
         + "?, "
         + "?,?," 
         + "to_char(sysdate,'yyyymmdd'),"
         + "?,"
         + " sysdate, "
         + "?,"
         + "1,"
         + " ? "
         + ")";

  Object[] param =new Object[]
    {
     wp.itemStr("active_code")+wp.itemStr("active_seq"), 
     data_type, 
     varsStr("data_code"),
     varsStr("data_code2"),
     wp.loginUser,
       wp.loginUser,
     wp.modPgm()
    };

  wp.dupRecord = "Y";
  sqlExec(isSql, param );
  if (sqlRowNum <= 0) rc=0;else rc=1;

  if (rc!=1) errmsg("新增8 MKT_BN_DATA_T 錯誤");

  return rc;
}
public int dbDeleteD3() throws Exception
{
  msgOK();

  String data_type="";
  if (wp.respHtml.equals("mktm6250_mrcd"))
     data_type = "7" ;
  //如果沒有資料回傳成功2
  Object[] param = new Object[]
    {
     wp.itemStr("active_code")+wp.itemStr("active_seq"), 
     data_type  
    };
  if (sqlRowcount("MKT_BN_DATA_T" 
                    , "where data_key = ? "
                   + "and   data_type = ? "
                   + "and   table_name = 'MKT_FSTP_PARMSEQ' "
                   , param) <= 0)
      return 1;

  String isSql = "delete MKT_BN_DATA_T "
         + "where data_key = ? "
         + "and   data_type = ? "
         + "and   table_name = 'MKT_FSTP_PARMSEQ'  "
         ;
  sqlExec(isSql,param);


  return 1;

}


//************************************************************************
public int dbDeleteD2() throws Exception
{
msgOK();

String data_type="";
if (wp.respHtml.equals("mktm6250_aaa1"))
   data_type = "8" ;
//如果沒有資料回傳成功2
Object[] param = new Object[]
  {
   wp.itemStr("active_code")+wp.itemStr("active_seq"), 
   data_type  
  };
if (sqlRowcount("MKT_BN_DATA_T" 
                  , "where data_key = ? "
                 + "and   data_type = ? "
                 + "and   table_name = 'MKT_FSTP_PARMSEQ' "
                 , param) <= 0)
    return 1;

String isSql = "delete MKT_BN_DATA_T "
       + "where data_key = ? "
       + "and   data_type = ? "
       + "and   table_name = 'MKT_FSTP_PARMSEQ'  "
       ;
sqlExec(isSql,param);


return 1;

}

//************************************************************************
public int dbInsertI2() throws Exception
{
msgOK();

String data_type="";
if (wp.respHtml.equals("mktm6250_aaa1"))
   data_type = "8" ;
String isSql = "insert into MKT_BN_DATA_T ( "
       + "table_name, "
       + "data_key, "
       + "data_type, "
       + "data_code,"
       + "crt_date, "
       + "crt_user, "
       + " mod_time, "
       + " mod_user, "
       + " mod_seqno, "
       + " mod_pgm "
       + ") values ("
       + "'MKT_FSTP_PARMSEQ', "
       + "?, "
       + "?, "
       + "?," 
       + "to_char(sysdate,'yyyymmdd'),"
       + "?,"
       + " sysdate, "
       + "?,"
       + "1,"
       + " ? "
       + ")";

Object[] param =new Object[]
  {
   wp.itemStr("active_code")+wp.itemStr("active_seq"), 
   data_type, 
   varsStr("data_code"),
   wp.loginUser,
     wp.loginUser,
   wp.modPgm()
  };

wp.dupRecord = "Y";
sqlExec(isSql, param );
if (sqlRowNum <= 0) rc=0;else rc=1;

if (rc!=1) errmsg("新增8 MKT_BN_DATA_T 錯誤");

return rc;
}

public int dbInsert_D3T() throws Exception
{
  msgOK();

  String isSql = "delete MKT_BN_DATA_T "
        + " where table_name  =  'MKT_FSTP_PARMSEQ' "
        + "and   data_key = ? "
         + "";
  //如果沒有資料回傳成功1
  Object[] param = new Object[]
    {
     wp.itemStr("active_code")+wp.itemStr("active_seq"), 
    };

  sqlExec(isSql,param);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  if (rc!=1) errmsg("刪除 MKT_BN_DATA_T 錯誤");

  return rc;

}

public int dbInsert_I3T() throws Exception
{
  msgOK();

 String isSql = "insert into MKT_BN_DATA_T "
        + "select * "
        + "from MKT_BN_DATA "
        + "where table_name  =  'MKT_FSTP_PARMSEQ' "
        + "and   data_key = ? "
        + "";

  Object[] param =new Object[]
    {
     wp.itemStr("active_code")+wp.itemStr("active_seq"), 
    };

 wp.dupRecord = "Y";
 sqlExec(isSql, param );


  return 1;
}

} // End of class
