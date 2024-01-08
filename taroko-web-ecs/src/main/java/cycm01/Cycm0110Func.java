/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 110/07/07  V1.00.01   Allen Ho      Initial                              *
* 111/10/28  V1.00.02  Yang Bo        sync code from mega                  *
***************************************************************************/
package cycm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Cycm0110Func extends FuncEdit
{
 private final String PROGNAME = "掛失費率參數資料維護處理程式110/07/07 V1.00.01";
  String kk1,kk2;
  String controlTabName = "cyc_lostfee";

 public Cycm0110Func(TarokoCommon wr)
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
      kk1 = wp.itemStr("kk_acct_type");
      if (empty(kk1))
         {
          errmsg("帳戶類別 不可空白");
          return;
         }
      kk2 = wp.itemStr("kk_lost_code");
      if (empty(kk2))
         {
          errmsg("掛失原因 不可空白");
          return;
         }
     }
  else
     {
      kk1 = wp.itemStr("acct_type");
      kk2 = wp.itemStr("lost_code");
     }
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where acct_type = ? "
             + " and   lost_code = ? "
             ;
      Object[] param = new Object[] {kk1,kk2};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[帳戶類別][掛失原因] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
         }
     }

  if (!wp.itemStr("onus_bank").equals("Y")) wp.itemSet("onus_bank","N");
  if (!wp.itemStr("onus_auto_pay").equals("Y")) wp.itemSet("onus_auto_pay","N");
  if (!wp.itemStr("other_auto_pay").equals("Y")) wp.itemSet("other_auto_pay","N");
  if (!wp.itemStr("salary_acct").equals("Y")) wp.itemSet("salary_acct","N");
  if (!wp.itemStr("credit_acct").equals("Y")) wp.itemSet("credit_acct","N");
  if (!wp.itemStr("credit_limit").equals("Y")) wp.itemSet("credit_limit","N");
  if (!wp.itemStr("bonus_sel").equals("Y")) wp.itemSet("bonus_sel","N");

  if ((this.ibAdd)||(this.ibUpdate))
     {
      String s1=wp.colStr("acct_type");
      String s2=wp.colStr("lost_code");


      wp.colSet("lost_fee_cnt" , "0");
      if (wp.colStr("acct_type").length()==0)
         s1 = wp.itemStr("kk_acct_type");
      if (wp.colStr("lost_code").length()==0)
         s2 = wp.itemStr("kk_lost_code");

      String isSql = "select "
                    + " acct_type "
                    + " from cyc_lostfee_acct "
                    + " where acct_type =  ? "
                    + " and   lost_code = ? "
                    ;
      Object[] param = new Object[] {s1,s2};
      sqlSelect(isSql,param);

      if (sqlRowNum <= 0)
         {
          errmsg("標準匯率未設定["+s1+"]["+s2+"]");
          return;
         }
     }

  int checkInt = checkDecnum(wp.itemStr("credit_amt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 格式超出範圍 : 整數[9]位");
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


  strSql= " insert into  " + controlTabName + " ("
          + " acct_type, "
          + " lost_code, "
          + " onus_bank, "
          + " onus_auto_pay, "
          + " other_auto_pay, "
          + " salary_acct, "
          + " credit_acct, "
          + " credit_limit, "
          + " credit_amt, "
          + " bonus_sel, "
          + " bonus, "
          + " lost_limit, "
          + " apr_date, "
          + " apr_user, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        kk1,
        kk2,
        wp.itemStr("onus_bank"),
        wp.itemStr("onus_auto_pay"),
        wp.itemStr("other_auto_pay"),
        wp.itemStr("salary_acct"),
        wp.itemStr("credit_acct"),
        wp.itemStr("credit_limit"),
        wp.itemNum("credit_amt"),
        wp.itemStr("bonus_sel"),
        wp.itemNum("bonus"),
        wp.itemNum("lost_limit"),
        wp.itemStr("zz_apr_user"),
        wp.loginUser,
        wp.modSeqno(),
        wp.loginUser,
        wp.modPgm()
       };

  sqlExec(strSql, param);
  if (sqlRowNum <= 0) errmsg("新增 "+ controlTabName +" 重複錯誤");

  return rc;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  strSql= "update " + controlTabName + " set "
         + "onus_bank = ?, "
         + "onus_auto_pay = ?, "
         + "other_auto_pay = ?, "
         + "salary_acct = ?, "
         + "credit_acct = ?, "
         + "credit_limit = ?, "
         + "credit_amt = ?, "
         + "bonus_sel = ?, "
         + "bonus = ?, "
         + "lost_limit = ?, "
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
     wp.itemStr("onus_bank"),
     wp.itemStr("onus_auto_pay"),
     wp.itemStr("other_auto_pay"),
     wp.itemStr("salary_acct"),
     wp.itemStr("credit_acct"),
     wp.itemStr("credit_limit"),
     wp.itemNum("credit_amt"),
     wp.itemStr("bonus_sel"),
     wp.itemNum("bonus"),
     wp.itemNum("lost_limit"),
     wp.loginUser,
     wp.itemStr("zz_apr_user"),
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
  actionInit("D");
  dataCheck();
  if (rc!=1)return rc;

  strSql = "delete " + controlTabName + " " 
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
 public int checkDecnum(String decStr, int colLength, int colScale)
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
 public int dbInsertI2() throws Exception
 {
   msgOK();

  strSql = "insert into CYC_LOSTFEE_ACCT ( "
          + "acct_type,"
          + "lost_code,"
          + "card_note,"
          + "sup_flag,"
          + "lostfee_amt,"
          + "crt_date, "
          + "crt_user, "
          + " mod_time, "
          + " mod_user, "
          + " mod_seqno, "
          + " mod_pgm "
          + ") values ("
          + "?,?,?,?,?," 
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + " sysdate, "
          + "?,"
          + "1,"
          + " ? "
          + ")";

   Object[] param =new Object[]
     {
      wp.itemStr("acct_type"),
      wp.itemStr("lost_code"),
      varsStr("card_note"),
      varsStr("sup_flag"),
      varsStr("lostfee_amt"),
      wp.loginUser,
        wp.loginUser,
      wp.modPgm()
     };

   wp.dupRecord = "Y";
   sqlExec(strSql, param);
   if (sqlRowNum <= 0) rc=0;else rc=1;

   if (rc!=1) errmsg("新增8 CYC_LOSTFEE_ACCT_T 錯誤");

   return rc;
 }
// ************************************************************************
 public int dbDeleteD2() throws Exception
 {
   msgOK();

   //如果沒有資料回傳成功2
   Object[] param = new Object[]
     {
      wp.itemStr("acct_type"),
      wp.itemStr("lost_code")
     };
   if (sqlRowcount("CYC_LOSTFEE_ACCT" 
                   , "where acct_type = ? "
                   + "and   lost_code = ? "
                    , param) <= 0)
       return 1;

   strSql = "delete CYC_LOSTFEE_ACCT "
          + "where acct_type = ?  "
          + "and   lost_code = ?  "
          ;
   sqlExec(strSql,param);


   return 1;

 }
// ************************************************************************

}  // End of class
