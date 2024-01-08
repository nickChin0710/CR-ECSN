/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 108/12/12  V1.00.01   Allen Ho      Initial                              *
* 109-04-28  V1.00.02  YangFang   updated for project coding standard      *
* 109-12-31  V1.00.03   shiyuqi       修改无意义命名                                                                                      *    
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6270Func extends FuncEdit
{
 private  String PROGNAME = "雙幣卡外幣基金明細檔處理程式108/12/12 V1.00.01";
  String tranSeqno;
  String orgControlTabName = "cyc_dc_fund_dtl";
  String controlTabName = "cyc_dc_fund_dtl_t";

 public Mktm6270Func(TarokoCommon wr)
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
  procTabName = wp.itemStr("control_tab_name");
  strSql= " select "
          + " acct_type, "
          + " fund_code, "
          + " tran_code, "
          + " beg_tran_amt, "
          + " effect_e_date, "
          + " mod_reason, "
          + " mod_desc, "
          + " mod_memo, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " end_tran_amt, "
          + " tran_pgm, "
          + " curr_code, "
          + " fund_name, "
          + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
          + " from " + procTabName 
          + " where rowid = ? ";

  Object[] param =new Object[]
       {
        wp.itemRowId("rowid")
       };

  sqlSelect(strSql, param);
  if (sqlRowNum <= 0) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

  return 1;
 }
// ************************************************************************
 @Override
 public void dataCheck()
 {
  if (this.ibAdd)
     {
      tranSeqno = wp.itemStr("tran_seqno");
     }
  else
     {
      tranSeqno = wp.itemStr("tran_seqno");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (tranSeqno.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where tran_seqno = ? "
             ;
      Object[] param = new Object[] {tranSeqno};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[交易序號] 不可重複("+orgControlTabName+"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (tranSeqno.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where tran_seqno = ? "
             ;
      Object[] param = new Object[] {tranSeqno};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[交易序號] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  if (this.ibAdd)
     {
      if (wp.itemStr("control_tab_name").equals(orgControlTabName))
         {
          errmsg("已覆核資料, 只可查詢不可異動 !");
          return;
         }
     }

  if ((this.ibAdd)||(this.ibUpdate))
     {
	  
	  if (wp.itemStr("fund_code").length() == 0) {
	      errmsg("[基金代碼] 必須選取 !");
	      return;
	    }  
	  if (wp.itemStr("mod_reason").length() == 0) {
		  
	      errmsg("異動原因, 不可空白 !");
	      return;
	    }	  
      strSql = "select "
             + " curr_code,fund_name "
             + " from  cyc_dc_fund_parm b"
             + " where b.fund_code = ? "
             ;
      Object[] param = new Object[] {wp.itemStr("fund_code")};
      sqlSelect(strSql,param);

      strSql = "select "
             + " a.curr_code as aa "
             + " from  act_acct_curr a"
             + " where a.p_seqno = ? "
             + " and   a.curr_code = ? "
             ;
      param = new Object[] {wp.itemStr("p_seqno"),colStr("curr_code")};
      sqlSelect(strSql,param);


      if (sqlRowNum <= 0)
         {
          strSql = "select "
                 + " b.curr_chi_name "
                 + " from  ptr_currcode b "
                 + " where b.curr_code = ? "
             ;
          param = new Object[] {colStr("curr_code")};
          sqlSelect(strSql,param);

          errmsg("卡友無["+colStr("curr_chi_name")+"]帳戶 !");
          return;
         }

     }


  if (wp.itemStr("tran_seqno").length()==0)
     {
      busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
      comr.setConn(wp);
      wp.itemSet("tran_seqno" , comr.getSeqno("MKT_MODSEQ"));
      tranSeqno = wp.itemStr("tran_seqno");
     }

  if (wp.itemNum("beg_tran_amt") == 0) {
	  
      errmsg("異動金額, 不可為 0 !");
      return;
   }  
   
  if (checkDecnum(wp.itemStr("beg_tran_amt"),9,2)!=0)
     {
      errmsg("異動金額： 格式超出範圍 : [9][2]");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("acct_type"))
     {
      errmsg("帳戶類別: 不可空白");
      return;
     }


  if (this.isAdd()) return;

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
          + " fund_code, "
          + " tran_code, "
          + " beg_tran_amt, "
          + " effect_e_date, "
          + " mod_reason, "
          + " mod_desc, "
          + " mod_memo, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " end_tran_amt, "
          + " tran_pgm, "
          + " curr_code, "
          + " fund_name, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_time,mod_user,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "sysdate,?,?)";

  Object[] param =new Object[]
       {
        tranSeqno,
        wp.itemStr("aud_type"),
        wp.itemStr("acct_type"),
        wp.itemStr("fund_code"),
        wp.itemStr("tran_code"),
        wp.itemNum("beg_tran_amt"),
        wp.itemStr("effect_e_date"),
        wp.itemStr("mod_reason"),
        wp.itemStr("mod_desc"),
        wp.itemStr("mod_memo"),
        wp.itemStr("p_seqno"),
        wp.itemStr("id_p_seqno"),
        wp.itemNum("beg_tran_amt"),
        wp.modPgm(),
        colStr("curr_code"),
        colStr("fund_name"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+controlTabName+" 錯誤");

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
         + "fund_code = ?, "
         + "tran_code = ?, "
         + "beg_tran_amt = ?, "
         + "effect_e_date = ?, "
         + "mod_reason = ?, "
         + "mod_desc = ?, "
         + "mod_memo = ?, "
         + "curr_code = ?, "
         + "fund_name = ?, "
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
     wp.itemStr("acct_type"),
     wp.itemStr("fund_code"),
     wp.itemStr("tran_code"),
     wp.itemNum("beg_tran_amt"),
     wp.itemStr("effect_e_date"),
     wp.itemStr("mod_reason"),
     wp.itemStr("mod_desc"),
     wp.itemStr("mod_memo"),
     colStr("curr_code"),
     colStr("fund_name"),
     wp.loginUser,
     wp.loginUser,
     wp.itemStr("mod_pgm"),
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
 public int checkDecnum(String decStr,int colLength,int colScale)
 {
  String[]  parts = decStr.split("[.^]");
  if ((parts.length==1&&parts[0].length()>colLength)||
      (parts.length==2&&
       (parts[0].length()>colLength||parts[1].length()>colScale)))
      return(1);
  return(0);
 }
// ************************************************************************

}  // End of class
