/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/09/16  V1.01.04   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0320Func extends FuncEdit
{
 private final String PROGNAME = "紅利媒體轉入參數檔維護處理程式111-11-30  V1.00.01";
  String kk1;
  String controlTabName = "mkt_transbp_parm";

 public Mktm0320Func(TarokoCommon wr)
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
  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck() 
 {
  if (this.ibAdd)
     {
      kk1 = wp.itemStr2("kk_active_code");
     }
  else
     {
      kk1 = wp.itemStr2("active_code");
     }

   if (wp.itemStr2("map_bp").length()==0) wp.itemSet("map_bp" , "0");
   if (wp.itemNum("map_bp")==0)
      {
       errmsg("檔案一點= N 點, N 必須大於 0!");
       return;
      }
   if (wp.itemStr2("effect_months").length()==0) wp.itemSet("effect_months","0");
   if ((wp.itemStr2("tax_flag").equals("Y"))&&
       (wp.itemNum("effect_months")!=0))
      {
       errmsg("應稅紅利不可有效期["+(int)wp.itemNum("effect_months")+"]!");
       return;
      }
   if (wp.itemStr2("bp_amt").length()==0) wp.itemSet("bp_amt" , "0");
   if ((wp.itemStr2("trans_type").equals("2"))&&
       (wp.itemStr2("tax_flag").equals("Y")))
      {
       if (wp.itemNum("bp_amt")==0)
          {
           errmsg("每點紅利= N 元(僅VD需要設定) , N 必須大於 0!");
           return;
          }
      }
   else
      {
       if (wp.itemNum("bp_amt")!=0)
          {
           errmsg("每點紅利= N 元(僅VD需要設定) , N 不可輸入值!");
           return;
          }
      }
  int checkInt = checkDecnum(wp.itemStr2("bp_amt"),6,2);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[6]位 小數[2]位");
      if (checkInt==2) 
         errmsg(" 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 非數值");
      return;
     }


  if (this.isAdd()) return;

 }
// ************************************************************************
 @Override
 public int dbInsert()
 {
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;


  strSql= " insert into  " + controlTabName+ " ("
          + " active_code, "
          + " active_name, "
          + " trans_type, "
          + " effect_months, "
          + " map_bp, "
          + " min_pt, "
          + " tax_flag, "
          + " bp_amt, "
          + " withhold_code, "
          + " apr_date, "
          + " apr_flag, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr2("active_name"),
        wp.itemStr2("trans_type"),
        wp.itemNum("effect_months"),
        wp.itemNum("map_bp"),
        wp.itemNum("min_pt"),
        wp.itemStr2("tax_flag"),
        wp.itemNum("bp_amt"),
        wp.itemStr2("withhold_code"),
        "Y",
        wp.itemStr2("zz_apr_user"),
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
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " +controlTabName + " set "
         + "active_name = ?, "
         + "trans_type = ?, "
         + "effect_months = ?, "
         + "map_bp = ?, "
         + "min_pt = ?, "
         + "tax_flag = ?, "
         + "bp_amt = ?, "
         + "withhold_code = ?, "
         + "crt_user  = ?, "
         + "crt_date  = to_char(sysdate,'yyyymmdd'), "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.itemStr2("active_name"),
     wp.itemStr2("trans_type"),
     wp.itemNum("effect_months"),
     wp.itemNum("map_bp"),
     wp.itemNum("min_pt"),
     wp.itemStr2("tax_flag"),
     wp.itemNum("bp_amt"),
     wp.itemStr2("withhold_code"),
     wp.loginUser,
     wp.itemStr2("zz_apr_user"),
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
 public int checkDecnum(String decStr,int colLength,int colScale)
 {
  if (decStr.length()==0) return(0);
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
  if (!comm.isNumber(decStr.replace("-","").replace(".",""))) return(3);
  decStr = decStr.replace("-","");
  if ((colScale==0)&&(decStr.toUpperCase().indexOf(".")!=-1)) return(2);
  String[]  parts = decStr.split("[.^]");
  if ((parts.length==1&&parts[0].length()>colLength)||
      (parts.length==2&&
       (parts[0].length()>colLength||parts[1].length()>colScale)))
      return(1);
  return(0);
 }
// ************************************************************************
 public int dbInsertI4() throws Exception
 {
   msgOK();

  strSql = "insert into MKT_TAXPAR_VD ( "
          + "program_code,"
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "?," 
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      varsStr("program_code"),
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param , false);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 MKT_TAXPAR_VD_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD4() throws Exception
 {
   msgOK();

   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
     };
   if (sqlRowcount("MKT_TAXPAR_VD" 
                    , " "
                    , param) <= 0)
       return 1;

   strSql = "delete MKT_TAXPAR_VD "
          ;
   sqlExec(strSql,param,false);


   return 1;

 }
// ************************************************************************
 public int dbUpdateMktUploadfileCtl() throws Exception
 {
  strSql= "update mkt_uploadfile_ctl set "
        + " apr_flag   = 'R', "
        + " apr_user   = ?, "
         + "apr_date   = to_char(sysdate,'yyyymmdd'), "
         + "error_memo = '媒體經mktm0320作廢' "
        + "where trans_seqno = ?";

  Object[] param =new Object[]
    {
     wp.itemStr2("zz_apr_user"),
     wp.itemStr2("trans_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 mkt_uploadfile_ctl 錯誤");

  return 1;
 }
// ************************************************************************
 int dbDeleteMktBonusDtl(String tranSeqno) throws Exception
 {
  strSql= "delete mkt_bonus_dtl "
         + "where tran_seqno  = ? "
         ;

  Object[] param =new Object[]
    {
     tranSeqno
    };

  rc = sqlExec(strSql, param);

  return rc;
 }
// ************************************************************************
 int dbDeleteDbmBonusDtl(String tranSeqno) throws Exception
 {
  strSql= "delete dbm_bonus_dtl "
         + "where tran_seqno  = ? "
         ;

  Object[] param =new Object[]
    {
     tranSeqno
    };

  rc = sqlExec(strSql, param);

  return rc;
 }

// ************************************************************************

}  // End of class
