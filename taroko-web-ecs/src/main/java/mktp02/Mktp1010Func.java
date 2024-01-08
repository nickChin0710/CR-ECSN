/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/08/17  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktp02;

import busi.DataSet;
import busi.FuncEdit;
import busi.ecs.MktBonus;

import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktp1010Func extends busi.FuncProc
{
 private final String PROGNAME = "紅利基點異動明細檔覆核處理程式111-11-30  V1.00.01";
  String kk1;
  String approveTabName = "mkt_tr_bonus";
  String controlTabName = "mkt_tr_bonus_t";

 public Mktp1010Func(TarokoCommon wr)
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
 }
// ************************************************************************
 @Override
 public int dataProc()
 {
  return rc;
 }
// ************************************************************************
 public int dbInsertA4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);

  MktBonus comb = new MktBonus();
  comb.setConn(wp);
  busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

  double endTranBp = comb.bonusSum(colStr("p_seqno"),colStr("bonus_type"));

  colSet("proc_date" ,wp.sysDate);
  if (endTranBp < colNum("bonus_pnt"))
     {
      colSet("proc_Desc","[紅利類別："+colStr("bonus_type")+"][餘額："+String.format("%.0f",endTranBp)
             +"]小於兌換金額"+colNum("bonus_pnt")+"]!");
      colSet("proc_code" ,"N");
     }
  else
     {
      selectMktBonusDtlFrom();

      colSet("proc_Desc","紅利積點移轉成功");

      if (colNum("fee_amt")>0)  
         {
          selectCrdCard();
          insertBilSysexp();
         }
     }

  strSql= " insert into  " + approveTabName+ " ("
          + " tran_seqno, "
          + " trans_date, "
          + " acct_type, "
          + " bonus_type, "
          + " to_acct_type, "
          + " bonus_pnt, "
          + " fee_amt, "
          + " id_p_seqno, "
          + " p_seqno, "
          + " to_p_seqno, "
          + " to_id_p_seqno, "
          + " proc_code, "
          + " proc_desc, "
          + " proc_date, "
          + " method, "
          + " apr_flag, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,?,?,"
          + "'Y',"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + " timestamp_format(?,'yyyymmddhh24miss'), "
          + "?,"
          + "?,"
          + " ?) ";

  Object[] param =new Object[]
       {
        colStr("tran_seqno"),
        colStr("trans_date"),
        colStr("acct_type"),
        colStr("bonus_type"),
        colStr("to_acct_type"),
        colStr("bonus_pnt"),
        colStr("fee_amt"),
        colStr("id_p_seqno"),
        colStr("p_seqno"),
        colStr("to_p_seqno"),
        colStr("to_id_p_seqno"),
        colStr("proc_code"),
        colStr("proc_desc"),
        comr.getBusinDate(),
        colStr("method"),
        wp.loginUser,
        colStr("crt_date"),
        colStr("crt_user"),
        wp.sysDate + wp.sysTime,
        wp.loginUser,
        colStr("mod_seqno"),  
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增資料 "+ controlTabName +" 失敗");

  return rc;
 }
// ************************************************************************
 public int dbSelectS4() throws Exception
 {
  String procTabName="";
     procTabName = controlTabName;
  strSql= " select "
          + " tran_seqno, "
          + " trans_date, "
          + " acct_type, "
          + " bonus_type, "
          + " to_acct_type, "
          + " bonus_pnt, "
          + " fee_amt, "
          + " id_p_seqno, "
          + " p_seqno, "
          + " to_p_seqno, "
          + " to_id_p_seqno, "
          + " proc_code, "
          + " proc_desc, "
          + " proc_date, "
          + " method, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
          + " from " + procTabName 
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("wprowid")
       };

  sqlSelect(strSql, param);
   if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

  return rc;
 }
// ************************************************************************
 public int dbUpdateU4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  String aprFlag = "Y";
  strSql= "update " +approveTabName + " set "
         + "trans_date = ?, "
         + "acct_type = ?, "
         + "bonus_type = ?, "
         + "to_acct_type = ?, "
         + "bonus_pnt = ?, "
         + "fee_amt = ?, "
         + "id_p_seqno = ?, "
         + "crt_user  = ?, "
         + "crt_date  = ?, "
         + "apr_user  = ?, "
         + "apr_date  = to_char(sysdate,'yyyymmdd'), "
         + "apr_flag  = ?, "
         + "mod_user  = ?, "
         + "mod_time  = timestamp_format(?,'yyyymmddhh24miss'),"
         + "mod_pgm   = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1 "
         + "where 1     = 1 " 
         + "and   tran_seqno  = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("trans_date"),
     colStr("acct_type"),
     colStr("bonus_type"),
     colStr("to_acct_type"),
     colStr("bonus_pnt"),
     colStr("fee_amt"),
     colStr("id_p_seqno"),
     colStr("crt_user"),
     colStr("crt_date"),
     wp.loginUser,
     aprFlag,
     colStr("mod_user"),
     colStr("mod_time"),
     colStr("mod_pgm"),
     colStr("tran_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;

  return rc;
 }
// ************************************************************************
 public int dbUpdateMktUploadfileCtlProcFlag(String transSeqno) throws Exception
 {
  strSql= "update mkt_uploadfile_ctl set "
        + " proc_flag = 'Y', "
        + " proc_date = to_char(sysdate,'yyyymmdd') "
        + "where trans_seqno = ?";

  Object[] param =new Object[]
    {
     transSeqno
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("更新 mkt_uploadfile_ctl 錯誤");

  return 1;
 }
// ************************************************************************
 public int dbDeleteD4() throws Exception
 {
  rc = dbSelectS4();
  if (rc!=1) return rc;
  strSql = "delete " +approveTabName + " " 
         + "where 1 = 1 "
         + "and tran_seqno = ? "
         ;

  Object[] param =new Object[]
    {
     colStr("tran_seqno")
    };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) rc=0;else rc=1;
  if (rc!=1) errmsg("刪除 "+ approveTabName +" 錯誤");

  return rc;
 }
// ************************************************************************
 public int dbDelete() throws Exception
 {
  strSql = "delete " +controlTabName + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.itemRowId("wprowid")
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
 int insertMktBonusDtl(int intType,String tranSeqno) throws Exception
 {
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);

  colSet("tran_date"     , wp.sysDate);
  colSet("tran_time"     , wp.sysTime);

  String acctType ="";
  String pSeqno ="";
  String idPSeqno ="";

  if (intType==0)
      {
       acctType  = colStr("acct_type");
       pSeqno    = colStr("p_seqno");
      }
  else
      {
       acctType  = colStr("to_acct_type");
       pSeqno    = colStr("to_p_seqno");
      }
  strSql= " insert into mkt_bonus_dtl("
          + " acct_type, "
          + " bonus_type, "
          + " active_code, "
          + " active_name, "
          + " tran_code, "
          + " beg_tran_bp, "
          + " end_tran_bp, "
          + " tax_flag, "
          + " effect_e_date, "
          + " mod_desc, "
          + " mod_reason, "
          + " mod_memo, "
          + " tran_date, "
          + " tran_time, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " tran_pgm, "
          + " tran_seqno, "
          + " proc_month, "
          + " acct_date, "
          + " apr_flag, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_time,mod_user,mod_pgm,mod_seqno "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "timestamp_format(?,'yyyymmddhh24miss'),?,?,?)";

  Object[] param =new Object[]
       {
        acctType,
        colStr("bonus_type"),
        "",                              // colStr("active_code"),
        colStr("active_name"),           // colStr("active_name"),
        colStr("tran_code"),
        colNum("beg_tran_bp"),
        colNum("end_tran_bp"),
        colStr("tax_flag"),       // colStr("tax_flag"),
        colStr("effect_e_date"),                     // colStr("effect_e_date"),
        colStr("mod_desc"),                          // mod_desc
        colStr("mod_reason"),                        // mod_reason
        colStr("mod_memo"),                          // mod_memo
        colStr("tran_date"),
        colStr("tran_time"),
        pSeqno,
        colStr("id_p_seqno"),
        wp.modPgm(),
        tranSeqno,
        comr.getBusinDate().substring(0,6),
        comr.getBusinDate(),
        "Y",
        wp.loginUser,
        colStr("crt_date"),
        colStr("crt_user"),
        wp.sysDate + wp.sysTime,
        wp.loginUser,
        wp.modPgm(),
        colStr("mod_seqno")
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg(sqlErrtext);

  return(1);
 }
// ************************************************************************
 public int insertBilSysexp() throws Exception
 {
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);

  strSql= " insert into bil_sysexp ("
          + "acct_type, "
          + "p_seqno, "
          + "card_no, "
          + "bill_type, "
          + "txn_code, "
          + "purchase_date, "
          + "src_amt, "
          + "dest_amt, "
          + "dc_dest_amt, "
          + "dest_curr, "
          + "curr_code, "
          + "bill_desc, "
          + "post_flag, "
          + "ref_key, "
          + "mod_user, "
          + "mod_time, "
          + "mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,"
          + " timestamp_format(?,'yyyymmddhh24miss'), "
          + " ?) ";

  Object[] param =new Object[]
       {
        colStr("acct_type"),
        colStr("p_seqno"),
        colStr("card_no"),
        "OICU",
        "PF", 
        comr.getBusinDate(),
        colNum("fee_amt"),
        colNum("fee_amt"),
        colNum("fee_amt"),
        "901",
        "901",
        "紅利移轉手續費",
        "N",
        colStr("tran_seqno"),
        wp.loginUser,
        wp.sysDate + wp.sysTime,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) {errmsg(sqlErrtext);return(0);}

  return(1);
 }
// ************************************************************************
 int selectCrdCard() throws Exception
 {
   strSql = " select "
          + " card_no "
          + " from crd_card  a"
          + " where p_seqno = ?  "
          + " and   id_p_seqno = major_id_p_seqno  "
          + " and   issue_date = (select max(issue_date) from crd_card  "
          + "                     where p_Seqno    = a.p_seqno "
          + "                     and   id_p_Seqno = a.id_p_seqno) ";

  Object[] param =new Object[]
       {
        colStr("p_seqno")
       };

  sqlSelect(strSql, param);

  return(1);
 }
// ************************************************************************
 int selectMktBonusDtlFrom() throws Exception
 {
  MktBonus comb = new MktBonus();
  comb.setConn(wp);
  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);

  String sqlStr="";

  sqlStr = " select "
         + " tran_seqno    as from_tran_seqno, "
         + " end_tran_bp   as from_end_tran_bp, "
         + " effect_e_date as from_effect_e_date, "
         + " tax_flag      as from_tax_flag "
         + " from mkt_bonus_dtl "
         + " where  p_seqno     = '" + colStr("p_seqno") +"' "
         + " and    bonus_type  = '" + colStr("bonus_type") +"' "
         + " and   end_tran_bp > 0 "
         + " order by decode(effect_e_date,'','99999999',effect_e_date),tax_flag"
         ;

  DataSet ds1 =new DataSet();
  ds1.colList = this.sqlQuery(sqlStr,new Object[]{});

  int rowNum= sqlRowNum;

  int  lastBp = 0;
  int  nowBp = (int)colNum("bonus_pnt");
  String tranSeqno = "";
  for ( int inti=0; inti<rowNum; inti++ )
    {
     ds1.listFetch(inti);

     if (nowBp >ds1.colNum("from_end_tran_bp"))
        {
         lastBp =  (int)ds1.colNum("from_end_tran_bp");
         nowBp  = nowBp - (int)ds1.colNum("from_end_tran_bp");
        }
     else
        {
         lastBp = nowBp;
         nowBp  = 0;
        }
     colSet("beg_tran_bp"   , String.format("%d",lastBp*-1));
     colSet("end_tran_bp"   , String.format("%d",lastBp*-1));
     colSet("tax_flag"      , "");
     colSet("effect_e_date" , "");
     colSet("active_name"   , "紅利積點移轉（轉出)");
     colSet("mod_desc","紅利轉入製帳戶類別["+colStr("to_acct_type")+"]");

     colSet("active_code"   , "");
     colSet("mod_reason"    , "");
     colSet("tran_code"     , "0");
     colSet("mod_memo"      , "");
     colSet("proc_month"    , "");
 
     tranSeqno = comr.getSeqno("MKT_MODSEQ");
     insertMktBonusDtl(0,tranSeqno);
     comb.bonusFunc(tranSeqno);

     colSet("beg_tran_bp"   , String.format("%d",lastBp));
     colSet("end_tran_bp"   , String.format("%d",lastBp));
     colSet("tax_flag"      , ds1.colStr("from_tax_flag"));
     colSet("effect_e_date" , ds1.colStr("from_effect_e_date"));
     colSet("active_name"   , "紅利積點移轉（轉入)");
     colSet("mod_desc","紅利移出從帳戶類別["+colStr("acct_type")+"]");
     colSet("mod_memo"      , ds1.colStr("from_tran_seqno"));

     tranSeqno = comr.getSeqno("MKT_MODSEQ");
     insertMktBonusDtl(1,tranSeqno);
     if (nowBp ==0) break;
    }
  return(0);
 }
// ************************************************************************


// ************************************************************************

}  // End of class
