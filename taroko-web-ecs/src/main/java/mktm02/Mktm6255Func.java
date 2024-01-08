/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/07  V1.00.01   Allen Ho      Initial                              *
* 111/11/14  V1.00.02   Machao        欄位名稱調整                                                                              *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6255Func extends FuncEdit
{
 private final String PROGNAME = "紅利折抵費用線上確認作業處理程式109/12/07 V1.00.01";
  String kk1;
  String orgControlTabName = "mkt_fstp_carddtl";
  String controlTabName = "mkt_fstp_carddtl_t";

 public Mktm6255Func(TarokoCommon wr)
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
          + " active_code, "
          + " acct_type, "
          + " error_code, "
          + " linebc_flag, "
          + " banklite_flag, "
          + " selfdeduct_flag, "
          + " feedback_date, "
          + " execute_date, "
          + " active_type, "
          + " mod_date, "
          + " bonus_type, "
          + " beg_tran_bp, "
          + " fund_code, "
          + " beg_tran_amt, "
          + " tran_pt, "
          + " spec_gift_no, "
          + " spec_gift_cnt, "
          + " mod_desc, "
          + " id_p_seqno, "
          + " group_type, "
          + " prog_code, "
          + " prog_s_date, "
          + " proc_flag, "
          + " purchase_flag, "
          + " achieve_cond, "
          + " anulfee_date, "
          + " anulfee_flag, "
          + " card_no, "
          + " card_note, "
          + " card_type, "
          + " dest_amt, "
          + " dest_cnt, "
          + " group_code, "
          + " in_dest_amt, "
          + " proc_date, "
          + " tran_seqno, "
          + " sms_send_date, "
          + " sms_send_flag, "
          + " active_seq, "
          + " error_desc, "
          + " gift_no, "
          + " half_msg_pgm, "
          + " issue_date, "
          + " last_execute_date, "
          + " match_active_seq, "
          + " multi_fb_type, "
          + " nopurc_msg_pgm, "
          + " prog_e_date, "
          + " p_seqno, "
          + " record_flag, "
          + " record_group_no, "
          + " record_no, "
          + " send_msg_pgm, "
          + " sms_half_date, "
          + " sms_half_flag, "
          + " sms_nopurc_date, "
          + " sms_nopurc_flag, "
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
      kk1 = wp.itemStr2("card_no");
     }
  else
     {
      kk1 = wp.itemStr2("card_no");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where card_no = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[卡號] 不可重複("+orgControlTabName+"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where card_no = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[卡號] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  if (this.ibAdd)
     {
      if ((wp.itemStr2("aud_type").equals("D"))&&
          (wp.itemStr2("control_tab_name").equals(orgControlTabName)))
         {
          errmsg("原始資料, 不可刪除!");
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

   if ((this.ibDelete)||
       (wp.itemStr2("aud_type").equals("D"))) return;

  if ((this.ibAdd)||(this.ibUpdate))
     {
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();

      if (wp.itemStr2("active_type").length()==0)
        {
         wp.itemSet("bonus_type","");
         wp.itemSet("beg_tran_bp","0");
         wp.itemSet("fund_code","");
         wp.itemSet("beg_tran_amt","0");
         colSet("group_type"  , "");
         colSet("prog_code"   , "");
         colSet("prog_s_date" , "");
         colSet("prog_e_date" , "");
         colSet("gift_no"     , "");
         wp.itemSet("tran_pt","0");
         wp.itemSet("spec_gift_no","");
         wp.itemSet("spec_gift_cnt","0");
        }
      else if (wp.itemStr2("active_type").equals("1"))
        {
         wp.itemSet("fund_code","");
         wp.itemSet("beg_tran_amt","0");
         colSet("group_type"  , "");
         colSet("prog_code"   , "");
         colSet("prog_s_date" , "");
         colSet("prog_e_date" , "");
         colSet("gift_no"     , "");
         wp.itemSet("tran_pt","0");
         wp.itemSet("spec_gift_no","");
         wp.itemSet("spec_gift_cnt","0");
         if (wp.itemStr2("bonus_type").length()==0)
            {
             errmsg("欲更換兌換紅利類別代碼, 不可空白!");
             return;
            }
         if (wp.itemStr2("beg_tran_bp").length()==0) wp.itemSet("beg_tran_bp","0");
         if (wp.itemNum("beg_tran_bp")==0)
            {
             errmsg("欲更換兌換變更贈送點數, 不可為0!");
             return;
            }
        }
     else if (wp.itemStr2("active_type").equals("2"))
        {
         wp.itemSet("bonus_type","");
         wp.itemSet("beg_tran_bp","0");
         colSet("group_type"  , "");
         colSet("prog_code"   , "");
         colSet("prog_s_date" , "");
         colSet("prog_e_date" , "");
         colSet("gift_no"     , "");
         wp.itemSet("tran_pt","0");
         wp.itemSet("spec_gift_no","");
         wp.itemSet("spec_gift_cnt","0");
         if (wp.itemStr2("fund_code").length()==0)
            {
             errmsg("欲更換兌換基金代碼, 不可空白!");
             return;
            }
         if (wp.itemStr2("beg_tran_amt").length()==0) wp.itemSet("beg_tran_amt","0");
         if  (wp.itemNum("beg_tran_amt")==0)
            {
             errmsg("欲更換兌換變更回饋金額, 不可為0!");
             return;
            }
        }
     else if (wp.itemStr2("active_type").equals("3"))
        {
         wp.itemSet("fund_code","");
         wp.itemSet("beg_tran_amt","0");
         wp.itemSet("bonus_type","");
         wp.itemSet("beg_tran_bp","0");
         wp.itemSet("spec_gift_no","");
         wp.itemSet("spec_gift_cnt","0");
         if (wp.itemStr2("gift_no1").length()==0)
            {
             errmsg("欲更換兌換豐富點商品代號, 不可空白!");
             return;
            }
         if (wp.itemStr2("tran_pt").length()==0) wp.itemSet("tran_pt","0");
         if  (wp.itemNum("tran_pt")==0)
            {
             errmsg("欲更換兌換變更豐富贈點數, 不可為0!");
             return;
            }

         colSet("group_type"  , comm.getStr(wp.itemStr2("gift_no1"),1,"-"));
         colSet("prog_code"   , comm.getStr(wp.itemStr2("gift_no1"),2,"-"));
         colSet("prog_s_date" , comm.getStr(wp.itemStr2("gift_no1"),3,"-"));
         colSet("gift_no"     , comm.getStr(wp.itemStr2("gift_no1"),4,"-"));

         strSql = "select prog_e_date "
                + " from ibn_prog_gift "
                + " where prog_code   =  ? "
                + " and   prog_s_date =  ? "
                + " and   gift_no     =  ? "
                ;
         Object[] param = new Object[] {colStr("prog_code"),colStr("prog_s_date"),colStr("gift_no")};
         sqlSelect(strSql,param);

         if (sqlRowNum <= 0)
            {
             errmsg("["+colStr("prog_code")+"-"+colStr("prog_s_date")+"-"+colStr("gift_no")+" 不存在於IBN_PROG_GIFT]");
             return;
            }
         colSet("prog_e_date" , colStr("prog_e_date"));

         busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
         comr.setConn(wp);

         if (colStr("prog_e_date").compareTo(comr.getBusinDate())<0)
            {
//             errmsg("商品兌換效期已過, 不可選取!");
//             return;
            }
        }
     else if (wp.itemStr2("active_type").equals("4"))
        {
         wp.itemSet("fund_code","");
         wp.itemSet("beg_tran_amt","0");
         wp.itemSet("bonus_type","");
         wp.itemSet("beg_tran_bp","0");
         colSet("group_type"  , "");
         colSet("prog_code"   , "");
         colSet("prog_s_date" , "");
         colSet("prog_e_date" , "");
         colSet("gift_no"     , "");
         wp.itemSet("tran_pt","0");
         if (wp.itemStr2("spec_gift_no").length()==0)
            {
             errmsg("欲更換兌換特殊商品代號, 不可空白!");
             return;
            }
         if (wp.itemStr2("spec_gift_cnt").length()==0) wp.itemSet("spec_gift_cnt","0");
         if  (wp.itemNum("spec_gift_cnt")==0)
            {
             errmsg("欲更換兌換特殊商品件數, 不可為0!");
             return;
            }
        }
     if (wp.itemStr2("mod_desc").length()==0)
        {
         errmsg("欲更換兌換異動說明, 不可空白!");
         return;
        }
    }
  int checkInt = checkDecnum(wp.itemStr2("beg_tran_bp"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　變更贈送點數: 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　變更贈送點數: 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　變更贈送點數: 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("beg_tran_amt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg(" 　變更基金金額: 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg(" 　變更基金金額: 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg(" 　變更基金金額: 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("tran_pt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　變更豐富點數: 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　變更豐富點數: 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　變更豐富點數: 非數值");
      return;
     }

  checkInt = checkDecnum(wp.itemStr2("spec_gift_cnt"),9,0);
  if (checkInt!=0) 
     {
      if (checkInt==1) 
         errmsg("　件數: 格式超出範圍 : 整數[9]位");
      if (checkInt==2) 
         errmsg("　件數: 格式超出範圍 : 不可有小數位");
      if (checkInt==3) 
         errmsg("　件數: 非數值");
      return;
     }

  if ((this.ibAdd)||(this.ibUpdate))
  if (wp.itemEmpty("apr_flag"))
     {
      errmsg("覆核狀態: 不可空白");
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
          + " apr_flag, "
          + " aud_type, "
          + " active_code, "
          + " acct_type, "
          + " error_code, "
          + " linebc_flag, "
          + " banklite_flag, "
          + " selfdeduct_flag, "
          + " feedback_date, "
          + " execute_date, "
          + " active_type, "
          + " mod_date, "
          + " bonus_type, "
          + " beg_tran_bp, "
          + " fund_code, "
          + " beg_tran_amt, "
          + " tran_pt, "
          + " spec_gift_no, "
          + " spec_gift_cnt, "
          + " mod_desc, "
          + " id_p_seqno, "
          + " group_type, "
          + " prog_code, "
          + " prog_s_date, "
          + " proc_flag, "
          + " purchase_flag, "
          + " achieve_cond, "
          + " anulfee_date, "
          + " anulfee_flag, "
          + " card_no, "
          + " card_note, "
          + " card_type, "
          + " dest_amt, "
          + " dest_cnt, "
          + " group_code, "
          + " in_dest_amt, "
          + " proc_date, "
          + " tran_seqno, "
          + " sms_send_date, "
          + " sms_send_flag, "
          + " active_seq, "
          + " error_desc, "
          + " gift_no, "
          + " half_msg_pgm, "
          + " issue_date, "
          + " last_execute_date, "
          + " match_active_seq, "
          + " multi_fb_type, "
          + " nopurc_msg_pgm, "
          + " prog_e_date, "
          + " p_seqno, "
          + " record_flag, "
          + " record_group_no, "
          + " record_no, "
          + " send_msg_pgm, "
          + " sms_half_date, "
          + " sms_half_flag, "
          + " sms_nopurc_date, "
          + " sms_nopurc_flag, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "?,"
          + "sysdate,?)";

  Object[] param =new Object[]
       {
        wp.itemStr2("apr_flag"),
        wp.itemStr2("aud_type"),
        wp.itemStr2("active_code"),
        wp.itemStr2("acct_type"),
        wp.itemStr2("error_code"),
        wp.itemStr2("linebc_flag"),
        wp.itemStr2("banklite_flag"),
        wp.itemStr2("selfdeduct_flag"),
        colStr("feedback_date"),
        colStr("execute_date"),
        wp.itemStr2("active_type"),
        wp.itemStr2("mod_date"),
        wp.itemStr2("bonus_type"),
        wp.itemNum("beg_tran_bp"),
        wp.itemStr2("fund_code"),
        wp.itemNum("beg_tran_amt"),
        wp.itemNum("tran_pt"),
        wp.itemStr2("spec_gift_no"),
        wp.itemNum("spec_gift_cnt"),
        wp.itemStr2("mod_desc"),
        colStr("id_p_seqno"),
        colStr("group_type"),
        colStr("prog_code"),
        colStr("prog_s_date"),
        colStr("proc_flag"),
        colStr("purchase_flag"),
        colStr("achieve_cond"),
        colStr("anulfee_date"),
        colStr("anulfee_flag"),
        colStr("card_no"),
        colStr("card_note"),
        colStr("card_type"),
        colStr("dest_amt"),
        colStr("dest_cnt"),
        colStr("group_code"),
        colStr("in_dest_amt"),
        colStr("proc_date"),
        colStr("tran_seqno"),
        colStr("sms_send_date"),
        colStr("sms_send_flag"),
        colStr("sactive_seq"),
        colStr("error_desc"),
        colStr("gift_no"),
        colStr("half_msg_pgm"),
        colStr("issue_date"),
        colStr("last_execute_dat"),
        colStr("match_active_seq"),
        colStr("multi_fb_type"),
        colStr("nopurc_msg_pgm"),
        colStr("prog_e_date"),
        colStr("p_seqno"),
        colStr("record_flag"),
        colStr("record_group_no"),
        colStr("record_no"),
        colStr("send_msg_pgm"),
        colStr("sms_half_date"),
        colStr("sms_half_flag"),
        colStr("sms_nopurc_date"),
        colStr("sms_nopurc_flag"),
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
         + "apr_flag = ?, "
         + "active_type = ?, "
         + "bonus_type = ?, "
         + "beg_tran_bp = ?, "
         + "fund_code = ?, "
         + "beg_tran_amt = ?, "
         + "tran_pt = ?, "
         + "spec_gift_no = ?, "
         + "spec_gift_cnt = ?, "
         + "mod_desc = ?, "
         + "prog_code = ?, "
         + "prog_s_date = ?, "
         + "prog_e_date = ?, "
         + "gift_no = ?, "
         + "group_type = ?, "
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
     wp.itemStr2("active_type"),
     wp.itemStr2("bonus_type"),
     wp.itemNum("beg_tran_bp"),
     wp.itemStr2("fund_code"),
     wp.itemNum("beg_tran_amt"),
     wp.itemNum("tran_pt"),
     wp.itemStr2("spec_gift_no"),
     wp.itemNum("spec_gift_cnt"),
     wp.itemStr2("mod_desc"),
     colStr("prog_code"),
     colStr("prog_s_date"),
     colStr("prog_e_date"),
     colStr("gift_no"),
     colStr("group_type"),
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
