/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/21  V1.00.00   Yang Han     Initial   
* 112/03/26  V1.00.01   machao       部分畫面調整                          *
* 112/05/04  V1.00.02   Zuwei Su     新增不寫入欄位cal_def_date                    *    *
* 112/05/04  V1.00.02   Zuwei Su     [活動代碼]不可重複
* 112/06/02  V1.00.03   Ryan         增加資料篩選日期欄位維護
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import busi.ecs.CommFunction;
import busi.ecs.CommRoutine;
import taroko.com.TarokoCommon;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Date;

// ************************************************************************
public class Mktm0900Func extends FuncEdit
{
  private final String PROGNAME = "稅務活動回饋參數維護 112/05/04  V1.00.02 ";
  String kk1,kk2,kk3,kk4;
  String orgControlTabName = "mkt_tax_parm";
  String controlTabName = "mkt_tax_parm_t";
  double feedbackAllTotcnt;

 public Mktm0900Func(TarokoCommon wr)
 {
  wp = wr;
  conn = wp.getConn();
 }
// ************************************************************************
 @Override
 public int querySelect()
 {
  // TODO Auto-generated method
  return 0;
 }
// ************************************************************************
 @Override
 public int dataSelect()
 {
  // TODO Auto-generated method stub
  strSql= " select "
		  + "hex(a.rowid) as rowid, "
          + "a.active_code,"
          + "a.active_name,"
          + "a.active_type,"
          + "a.purchase_date_s,"
          + "a.purchase_date_e,"
          + "a.cal_def_date,"
          + "a.crt_user,"
          + "a.crt_date"
          + "a.mod_seqno"
          + " from mkt_tax_parm "
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("rowid")
       };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) {
      errmsg("查無資料，讀取 "+ controlTabName +" 失敗");
  }
  return 1;
 }
// ****************************************p*******************************
 @Override
 public void dataCheck()
 {
//	 String allCntStr = empty(wp.itemStr("feedback_all_totcnt")) ?"0":wp.itemStr("feedback_all_totcnt");
//	 String emoCntStr = empty(wp.itemStr("feedback_emp_totcnt")) ?"0":wp.itemStr("feedback_emp_totcnt");
//	 String noEmpCntStr = empty(wp.itemStr("feedback_nonemp_totcnt")) ?"0":wp.itemStr("feedback_nonemp_totcnt");
//     BigDecimal feedbackAllTotcnt = new BigDecimal (allCntStr);
//     BigDecimal feedbackEmpTotcnt = new BigDecimal (emoCntStr);
//     BigDecimal feedbackNoEmpTotcnt =  new BigDecimal (noEmpCntStr);
//     if (!feedbackAllTotcnt.equals(feedbackEmpTotcnt.add(feedbackNoEmpTotcnt))){
//         errmsg(" 員工總筆數加非員工總筆數不等於全部總筆數! 請檢查!");
//     }
     
     if (this.ibAdd) {
       kk1 = wp.itemStr("active_code");
       if (empty(kk1)) {
         errmsg("活動代碼 不可空白");
         return;
       }
       strSql =
           "select count(*) as qua " + "from " + controlTabName + " where active_code = ? ";
       Object[] param = new Object[] {kk1};
       sqlSelect(strSql, param);
       int qua = Integer.parseInt(colStr("qua"));
       if (qua > 0) {
         errmsg("[活動代碼] 不可重複(" + orgControlTabName + "), 請重新輸入!");
         return;
       }
     }
    
     feedbackAllTotcnt = wp.itemNum("feedback_all_totcnt");
     double feedbackEmpTotcnt = wp.itemNum("feedback_emp_totcnt");
     double feedbackNonempTotcnt = wp.itemNum("feedback_nonemp_totcnt");
     if(feedbackAllTotcnt == 0) {
    	 feedbackAllTotcnt = feedbackEmpTotcnt + feedbackNonempTotcnt;
     }
     else {
    	 if ( (feedbackEmpTotcnt!=0) || (feedbackNonempTotcnt!=0) ) 
    		 if (feedbackAllTotcnt != feedbackEmpTotcnt + feedbackNonempTotcnt ) {
    	 errmsg(" 員工總筆數加非員工總筆數不等於全部總筆數! 請檢查!");
    	 }
     }
     return; 
 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  actionInit("A");
  dataCheck();
  if (rc!=1) {
      return rc;
  }

  strSql= " insert into  " + controlTabName + " ("
          + " active_code, "
          + " aud_type, "
          + " active_name, "
          + " active_type, "
          + " purchase_date_s, "
          + " purchase_date_e, "
          + " feedback_all_totcnt, "
          + " feedback_emp_totcnt, "
          + " feedback_nonemp_totcnt, "
          + " feedback_peremp_cnt, "
          + " feedback_pernonemp_cnt, "
          + " purchase_amt_s, "
          + " purchase_amt_e, "  
          + " gift_type, "
          + " feedback_id_type, "
          + " crt_user, "
          + " crt_date, "
          + " cal_def_date, "
          + " mod_user, "
          + " mod_time, "
          + " mod_seqno, "
          + " mod_pgm "
          + " ) values ("
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "sysdate,"
          + "?,"
          + "? )";
  Object[] param =new Object[]
       {
        wp.itemStr("active_code"),
        wp.itemStr("aud_type"),
        wp.itemStr("active_name"),
        wp.itemStr("kk_active_type"),
        wp.itemStr("exDateS"),
        wp.itemStr("exDateE"),
//        empty(wp.itemStr("feedback_all_totcnt")) ?0:Integer.parseInt(wp.itemStr("feedback_all_totcnt")),
        feedbackAllTotcnt,
        empty(wp.itemStr("feedback_emp_totcnt")) ?0:Integer.parseInt(wp.itemStr("feedback_emp_totcnt")),
        empty(wp.itemStr("feedback_nonemp_totcnt")) ?0:Integer.parseInt(wp.itemStr("feedback_nonemp_totcnt")),
        empty(wp.itemStr("feedback_peremp_cnt")) ?0:Integer.parseInt(wp.itemStr("feedback_peremp_cnt")),
        empty(wp.itemStr("feedback_pernonemp_cnt")) ?0:Integer.parseInt(wp.itemStr("feedback_pernonemp_cnt")),
        empty(wp.itemStr("purchase_amt_s")) ?0:Integer.parseInt(wp.itemStr("purchase_amt_s")),
        empty(wp.itemStr("purchase_amt_e")) ?0:Integer.parseInt(wp.itemStr("purchase_amt_e")),
        wp.itemStr("gift_type"),
        wp.itemStr("feedback_id_type"),
        wp.loginUser,
        wp.itemStr("cal_def_date"),
        wp.loginUser,
        wp.modSeqno(),
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
      errmsg("新增 "+ controlTabName +" 重複錯誤");
  }

  return rc;
 }

// ************************************************************************
 @Override
 public int dbUpdate()
 {
  rc = dataSelect();
  if (rc!=1) {
      return rc;
  }
  actionInit("U");
  dataCheck();
  if (rc!=1) {
      return rc;
  }

  strSql= "update " + controlTabName + " set "
          + " active_code = ?, "
          + " aud_type = ?, "
          + " active_name = ?, "
          + " active_type = ?, "
          + " purchase_date_s = ?, "
          + " purchase_date_e = ?, "
          + " feedback_all_totcnt = ?, "
          + " feedback_emp_totcnt = ?, "
          + " feedback_nonemp_totcnt = ?, "
          + " feedback_peremp_cnt = ?, "
          + " feedback_pernonemp_cnt = ?, "
          + " purchase_amt_s = ?, "
          + " purchase_amt_e = ?, "  
          + " gift_type = ?, "
          + " feedback_id_type = ?, "    
          + " crt_user  = ?, "
          + " crt_date  = to_char(sysdate,'yyyymmdd'), "
          + " mod_user  = ?, "
          + " mod_seqno = nvl(mod_seqno,0)+1, "
          + " mod_time  = sysdate, "
          + " mod_pgm   = ?,"
          + " cal_def_date = ? "
          + " where rowid = ? "
          + " and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
         wp.itemStr("active_code"),
         wp.itemStr("aud_type"),
         wp.itemStr("active_name"),
         wp.itemStr("kk_active_type"),
         wp.itemStr("exDateS"),
         wp.itemStr("exDateE"),
//         wp.itemStr("feedback_all_totcnt"),
         feedbackAllTotcnt,
         wp.itemNum("feedback_emp_totcnt"),
         wp.itemNum("feedback_nonemp_totcnt"),
         wp.itemNum("feedback_peremp_cnt"),
         wp.itemNum("feedback_pernonemp_cnt"),
         wp.itemStr("purchase_amt_s"),
         wp.itemStr("purchase_amt_e"),
         wp.itemStr("gift_type"),
         wp.itemStr("feedback_id_type"),  
     wp.loginUser,
     wp.loginUser,
     wp.itemStr("mod_pgm"),
     wp.itemStr("cal_def_date"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
      errmsg("更新 "+ controlTabName +" 錯誤");
  }

  if (sqlRowNum <= 0) { 
      rc=0;
  } else {
      rc=1;
  }
  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  actionInit("D");
  dataCheck();
  if (rc!=1) {
      return rc;
  }


  strSql = "delete " + controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {
      rc=0;
  } else {
      rc=1;
  }
  if (sqlRowNum <= 0) 
     {
      errmsg("刪除 "+ controlTabName +" 錯誤");
      return(-1);
     }

  return rc;
 }


// ************************************************************************

}  // End of class
