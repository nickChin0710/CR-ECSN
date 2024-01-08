/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/12  V1.00.01   Allen Ho      Initial                              *
* 111-11-28  V1.00.02  Machao    sync from mega & updated for project coding standard  
* 111/12/16  V1.00.03   Machao        命名规则调整后测试修改                                                                           *
* 111-12-26  V1.00.01  Zuwei Su       insert無法成功，id/姓名不能帶出寫入                                                                       *
* 112/10/30  V1.00.04   Ryan          判斷有無流通卡 增加p_seqno 
***************************************************************************/
package dbmm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Dbmm0100Func extends FuncEdit
{
 private final String PROGNAME = "帳戶DEBT紅利明細檔線上調整作業處理程式111-12-16  V1.00.03";
  String kk1;
  String orgControlTabName = "dbm_bonus_dtl";
  String controlTabName = "dbm_bonus_dtl_t";

 public Dbmm0100Func(TarokoCommon wr)
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
          + " apr_flag, "
          + " acct_type, "
          + " tran_date, "
          + " tran_time, "
          + " id_p_seqno, "
          + " active_code, "
          + " active_name, "
          + " tran_code, "
          + " beg_tran_bp, "
          + " tax_flag, "
          + " effect_e_date, "
          + " mod_reason, "
          + " mod_desc, "
          + " mod_memo, "
          + " end_tran_bp, "
          + " tran_pgm, "
          + " bonus_type, "
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
  if (sqlRowNum <= 0) errmsg("查無資料，讀取 "+ controlTabName +" 失敗");

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


 if (this.ibAdd)
     {
      if (wp.itemStr2("control_tab_name").equals(orgControlTabName))
         {
          errmsg("已覆核資料, 只可查詢不可異動 !");
          return;
         }
     }

   if (wp.itemStr2("aud_type").equals("A"))
      {
       if (wp.itemStr2("apr_flag").equals("Y"))
          {
           wp.colSet("apr_flag" , "N");
           wp.itemSet("apr_flag" , "N");
          }
      }
   else
      {
       if (wp.itemStr2("apr_flag").equals("Y"))
          {
           wp.colSet("apr_flag" , "N");
           wp.itemSet("apr_flag" , "N");
          }
      }

   if (wp.itemEmpty("apr_flag")) {
       wp.colSet("apr_flag", "N");
       wp.itemSet("apr_flag", "N");
   }
   if ((this.ibDelete)||
       (wp.itemStr2("aud_type").equals("D"))) return;

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemNum("beg_tran_bp")==0)
         {
          errmsg("調整紅利點數, 不可為 0 !");
          return;
         }
      if (wp.itemNum("beg_tran_bp")<0)
         wp.itemSet("effect_e_date" , "");

//      if (wp.item_ss("active_code").length()==0)
//         {
//          errmsg("活動代碼, 不可空白 !");
//          return;
//         }
      if (wp.itemStr2("tran_code").length()==0)
         {
          errmsg("交易類別, 不可空白 !");
          return;
         }
      if (wp.itemNum("beg_tran_bp")>0)
      if (wp.itemStr2("effect_e_date").length()==0)
         {
          errmsg("可兌換有效迄日, 不可空白 !");
          return;
         }
      if (wp.itemStr2("mod_reason").length()==0)
         {
          errmsg("異動原因, 不可空白 !");
          return;
         }
//      if (wp.item_ss("mod_desc").length()==0)
//         {
//          errmsg("異動說明, 不可空白 !");
//          return;
//         }
      String s1 = wp.itemStr2("acct_type");
      String s2 = wp.itemStr2("id_no");
      
      String idNoCode = "0";
      if (s2.length()>10)
         {
          idNoCode = s2.substring(10,11);
          s2 =s2.substring(0,10);
         }
      strSql = " select "
             + " a.chi_name as chi_name ,"
             + " b.id_p_seqno as id_p_seqno "
             + " from  dbc_idno a,dba_acno b "
             + " where a.id_p_seqno=b.id_p_seqno "
             + " and   b.acct_type  = ? "
             + " and   a.id_no      = ? "
             + " and   a.id_no_code = ? "
             ;
   
       Object[] param = new Object[] {s1,s2,idNoCode};
      sqlSelect(strSql,param);
      if (sqlRowNum<=0)
         {
          errmsg("帳戶類別:["+s1+"]["+s2+"]查無資料");
          return;
         }
          strSql = " select a.p_seqno from dbc_card a join dbc_idno b on a.id_p_seqno = b.id_p_seqno ";
          strSql += " where a.acct_type ='90' and a.current_code = '0' and b.id_no = ? ";
        
          param = new Object[] {s2};
          sqlSelect(strSql,param);
    	  if (sqlRowNum <= 0) {
    		 errmsg("該戶不存在有效流通卡");
    		 return;
    	  }
    	  wp.colSet("p_seqno", colStr("p_seqno"));
     }
  if (this.ibAdd)
     {
      if (wp.itemStr2("active_code").length()>0)
         {
          strSql = "select "
                 + " active_name as active_name "
                 + " from  vdbm_bonus_active_name"
                 + " where active_code = ? "
                 ;
          Object[] param = new Object[] {wp.itemStr2("active_code")};
          sqlSelect(strSql,param);

          if (sqlRowNum <= 0) 
             {colSet("active_name"  , "人工線上調整");}
             {wp.itemSet("active_name",colStr("active_name"));}
         }
     else 
         {
          colSet("active_name"  , "人工線上調整");
         }
     wp.itemSet("active_name",colStr("active_name"));

     String idNo      = wp.itemStr2("id_no");
     if (idNo.length()>10)
         idNo      = idNo.substring(0,10);

     strSql = "select "
             + " a.chi_name as chi_name ,"
             + " b.id_p_seqno as id_p_seqno "
             + " from  dbc_idno a,dba_acno b "
             + " where a.id_p_seqno=b.id_p_seqno "
             + " and   b.acct_type  = ? "
             + " and   a.id_no      = ? "
             + " order by id_no,id_no_code "
            ;
     Object[] param = new Object[] {wp.itemStr2("acct_type"),idNo};
     sqlSelect(strSql,param);

     if (sqlRowNum <= 0) 
        {
         errmsg("身分證號/帳戶查詢碼]["+wp.itemStr2("id_no")+"] 不存在 !");
         return;
        }

     if (wp.itemStr2("tran_seqno").length()==0)
        {
         busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
         comr.setConn(wp);
         wp.itemSet("tran_seqno" , comr.getSeqno("ECS_DBMSEQ"));
         kk1 = wp.itemStr2("tran_seqno");
        }
    }
  int checkInt = checkDecnum(wp.itemStr2("beg_tran_bp"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("異動紅利點數： 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("異動紅利點數： 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("異動紅利點數： 非數值");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("acct_type"))
     {
      errmsg("帳戶類別: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("id_no"))
     {
      errmsg("身分證號/帳戶查詢碼： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("tran_code"))
     {
      errmsg("交易類別： 不可空白");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("tax_flag"))
     {
      errmsg("是否應稅： 不可空白");
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
//  rc = dataSelect();
//  if (rc!=1) return rc;
  actionInit("A");
  dataCheck();
  if (rc!=1) return rc;


  strSql= " insert into  " + controlTabName+ " ("
          + " tran_seqno, "
          + " apr_flag, "
          + " aud_type, "
          + " acct_type, "
          + " tran_date, "
          + " id_p_seqno, "
          + " p_seqno, "
          + " active_code, "
          + " active_name, "
          + " tran_code, "
          + " beg_tran_bp, "
          + " tax_flag, "
          + " effect_e_date, "
          + " mod_reason, "
          + " mod_desc, "
          + " mod_memo, "
          + " end_tran_bp, "
          + " tran_pgm, "
          + " bonus_type, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,"
          + "'',"
          + "?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.itemStr2("apr_flag"),
        wp.itemStr2("aud_type"),
        wp.itemStr2("acct_type"),
//        colStr("tran_date"),
        wp.itemStr2("id_p_seqno"),
        wp.colStr("p_seqno"),
        wp.itemStr2("active_code"),
        wp.itemStr2("active_name"),
        wp.itemStr2("tran_code"),
        wp.itemNum("beg_tran_bp"),
        wp.itemStr2("tax_flag"),
        wp.itemStr2("effect_e_date"),
        wp.itemStr2("mod_reason"),
        wp.itemStr2("mod_desc"),
        wp.itemStr2("mod_memo"),
        wp.itemNum("beg_tran_bp"),
        wp.modPgm(),
        "BONU",
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
//  rc = dataSelect();
//  if (rc!=1) return rc;
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " +controlTabName + " set "
         + "apr_flag = ?, "
         + "acct_type = ?, "
         + "id_p_seqno = ?, "
         + "p_seqno = ?, "
         + "active_code = ?, "
         + "active_name = ?, "
         + "tran_code = ?, "
         + "beg_tran_bp = ?, "
         + "tax_flag = ?, "
         + "effect_e_date = ?, "
         + "mod_reason = ?, "
         + "mod_desc = ?, "
         + "mod_memo = ?, "
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
     wp.itemStr2("apr_flag"),
     wp.itemStr2("acct_type"),
     wp.itemStr("id_p_seqno"),
     wp.colStr("p_seqno"),
     wp.itemStr2("active_code"),
     wp.itemStr("active_name"),
     wp.itemStr2("tran_code"),
     wp.itemNum("beg_tran_bp"),
     wp.itemStr2("tax_flag"),
     wp.itemStr2("effect_e_date"),
     wp.itemStr2("mod_reason"),
     wp.itemStr2("mod_desc"),
     wp.itemStr2("mod_memo"),
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

}  // End of class
