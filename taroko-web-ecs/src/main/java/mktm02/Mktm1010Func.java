/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/17  V1.00.01   Allen Ho      Initial                              *
* 111-11-28  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import busi.ecs.MktBonus;

import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm1010Func extends FuncEdit
{
 private final String PROGNAME = "紅利基點異動明細檔維護處理程式111-11-28  V1.00.01";
  String kk1;
  String orgControlTabName = "mkt_tr_bonus";
  String controlTabName = "mkt_tr_bonus_t";

 public Mktm1010Func(TarokoCommon wr)
 {
  wp = wr;
  this.conn = wp.getConn();
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
  String procTabName="";
  procTabName = wp.itemStr2("control_tab_name");
  if (procTabName.length()==0) return(1);
  strSql= " select "
          + " acct_type, "
          + " bonus_type, "
          + " to_acct_type, "
          + " bonus_pnt, "
          + " fee_amt, "
          + " proc_code, "
          + " proc_date, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " method, "
          + " trans_date, "
          + " to_p_seqno, "
          + " to_id_p_seqno, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
          + " from " + procTabName 
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("rowid")
       };

  sqlSelect(strSql, param);
   if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck() 
 {
  if (!this.ibDelete)
     {
      if (wp.colStr("storetype").equals("Y"))
        {
         errmsg("[查原資料]模式中, 請按[還原異動] 才可儲存 !");
         return;
        }
     }
  if (this.ibAdd)
     {
      kk1 = wp.itemStr2("tran_seqno");
     }
  else
     {
      kk1 = wp.itemStr2("tran_seqno");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where tran_seqno = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[交易序號] 不可重複("+orgControlTabName+"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where tran_seqno = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[交易序號] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  if (((this.ibAdd)||(this.ibUpdate))&&(!wp.itemStr2("aud_type").equals("D")))
     {
      if (wp.itemStr2("acct_type").equals(wp.itemStr2("to_acct_type")))
         {
          errmsg("轉出入帳戶類別：不可相同");
          return;
         }
     }

  if ((this.ibUpdate)&&(!wp.itemStr2("aud_type").equals("D")))
     {
      if (!wp.itemStr2("acct_type").equals(wp.colStr("acct_type")))
         {
          errmsg("轉出帳戶類別：不可異動");
          return;
         }
     }

  if (wp.itemStr2("proc_date").length()!=0)
     {
      errmsg("已覆核資料不可異動");
      return;
     }

   if ((this.ibDelete)||
       (wp.itemStr2("aud_type").equals("D"))) return;

   strSql = " select "
          + " b.p_seqno as new_p_seqno "
          + " from  crd_idno a,act_acno b "
          + " where a.id_p_seqno=b.id_p_seqno "
          + " and   b.acct_type = ? "
          + " and   a.id_no     = ? "
          ;

  Object[] param = new Object[] {wp.itemStr2("acct_type"),wp.itemStr2("id_no")};
  sqlSelect(strSql,param);

   if (sqlRowNum <= 0)
      {
       errmsg("轉出帳戶不存在!");
       return;
      }

   strSql = " select "
          + " b.p_seqno as to_p_seqno, "
          + " b.id_p_seqno as to_id_p_seqno "
          + " from  crd_idno a,act_acno b "
          + " where a.id_p_seqno=b.id_p_seqno "
          + " and   b.acct_type = ? "
          + " and   a.id_no     = ? "
          ;
     
   param = new Object[] {wp.itemStr2("to_acct_type"),wp.itemStr2("id_no")};
   sqlSelect(strSql,param);

   if (sqlRowNum <= 0)
      {
       errmsg("轉入帳戶不存在!");
       return;
      }

  wp.colSet("to_p_seqno"    , colStr("to_p_Seqno")); 
  wp.colSet("to_id_p_seqno" , colStr("to_id_p_Seqno")); 

  if (wp.itemStr2("bonus_pnt").length()==0) wp.itemSet("bonus_pnt" , "0");
  MktBonus comc = new MktBonus();
  comc.setConn(wp);
  wp.itemSet("end_tran_bp",String.format("%,.0f",comc.bonusSum(colStr("new_p_seqno"),wp.itemStr2("bonus_type"))));
  double end_tran_bp = comc.bonusSum(colStr("new_p_seqno"),wp.itemStr2("bonus_type"));
  if (wp.itemNum("bonus_pnt")>end_tran_bp)
     {
      errmsg("紅利餘額：["+end_tran_bp+"] 小於轉出紅利點數["+wp.itemNum("bonus_pnt")+"]");
      return;
     }

  if (wp.itemStr2("tran_seqno").length()==0)
     {
      busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
      comr.setConn(wp);
      wp.itemSet("tran_seqno" , comr.getSeqno("MKT_MODSEQ"));
      kk1 = wp.itemStr2("tran_seqno");
      dateTime();
      wp.itemSet("trans_date",wp.sysDate);
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("acct_type"))
     {
      errmsg("轉出帳戶類別： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("id_no"))
     {
      errmsg("身分證號/統一編號： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("to_acct_type"))
     {
      errmsg("轉入帳戶類別： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("bonus_pnt"))
     {
      errmsg("轉出點數： 不可空白");
      return;
     }


  if (this.isAdd()) return;

  if (this.ibDelete)
     {
      wp.colSet("storetype" , "N");
     }
 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;


  strSql= " insert into  " + controlTabName+ " ("
          + " tran_seqno, "
          + " aud_type, "
          + " acct_type, "
          + " bonus_type, "
          + " to_acct_type, "
          + " bonus_pnt, "
          + " fee_amt, "
          + " proc_code, "
          + " proc_date, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " method, "
          + " trans_date, "
          + " to_p_seqno, "
          + " to_id_p_seqno, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr2("aud_type"),
        wp.itemStr2("acct_type"),
        wp.itemStr2("bonus_type"),
        wp.itemStr2("to_acct_type"),
        wp.itemNum("bonus_pnt"),
        wp.itemNum("fee_amt"),
        wp.itemStr2("proc_code"),
        colStr("proc_date"),
        wp.itemStr2("p_seqno"),
        wp.itemStr2("id_p_seqno"),
        "0",
        wp.itemStr2("trans_date"),
        wp.colStr("to_p_seqno"),
        wp.colStr("to_id_p_seqno"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 重複錯誤");

  return rc;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " +controlTabName + " set "
         + "acct_type = ?, "
         + "bonus_type = ?, "
         + "to_acct_type = ?, "
         + "bonus_pnt = ?, "
         + "fee_amt = ?, "
         + "p_seqno = ?, "
         + "id_p_seqno = ?, "
         + "crt_user  = ?, "
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr2("acct_type"),
     wp.itemStr2("bonus_type"),
     wp.itemStr2("to_acct_type"),
     wp.itemNum("bonus_pnt"),
     wp.itemNum("fee_amt"),
     wp.colStr("p_seqno"),
     wp.colStr("id_p_seqno"),
     wp.loginUser,
     wp.loginUser,
     wp.itemStr2("mod_pgm"),
     wp.itemRowId("rowid"),
     wp.itemNum("mod_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

  if (sqlRowNum <= 0) rc=0;else rc=1;
  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  rc = dataSelect();
  if (rc!=1) return rc;
  actionInit("D");
  dataCheck();
  if (rc!=1)return rc;

  strSql = "delete " +controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("rowid")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (sqlRowNum <= 0) 
     {
      errmsg("刪除 "+ controlTabName +" 錯誤");
      return(-1);
     }

  return rc;
 }
// ************************************************************************

}  // End of class
