/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 107/03/19  V1.00.01   Ray Ho        Initial                              *
*                                                                          *
***************************************************************************/
package busi.smsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Smsm0030Func extends FuncEdit
{
 private  String PROGNAME = "簡訊內容迷戲檔維護處理程式107/03/19 V1.00.01";
  String kk1,kk2,kk3;
  String org_control_tab_name = "sms_msg_dtl";
  String control_tab_name = "sms_msg_dtl_t";

 public Smsm0030Func(TarokoCommon wr)
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
  return 0;
 }
// ************************************************************************
 @Override
 public void dataCheck()
 {
  if (this.ib_add)
     {
      kk1 = wp.item_ss("cellar_phone");
      if (empty(kk1))
         {
          errmsg("行動電話: 不可空白");
          return;
         }
      kk2 = wp.item_ss("id_no");
      if (empty(kk2))
         {
          errmsg("持卡者ID: 不可空白");
          return;
         }
      kk3 = wp.item_ss("msg_seqno");
     }
  else
     {
      kk1 = wp.item_ss("cellar_phone");
      kk2 = wp.item_ss("id_no");
      kk3 = wp.item_ss("msg_seqno");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ib_add)
     {
      is_sql = "select count(*) as qua "
             + "from " + org_control_tab_name
             + " where cellar_phone = ? "
             +"and   id_no = ? "
             +"and   msg_seqno = ? "
             ;
      Object[] param = new Object[] {kk1,kk2,kk3};
      sqlSelect(is_sql,param);
      int qua =  Integer.parseInt(col_ss("qua"));
      if (qua > 0)
         {
          errmsg("[行動電話:][持卡者ID:][新增序號:] 不可重複 ,請重新輸入!");
          return;
         }
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

  busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
  comr.setConn(wp);
  String msg_seqStr = comr.getSeqno("MKT_MODSEQ");

  is_sql= " insert into  " + control_tab_name+ " ("
          + " cellar_phone, "
          + " aud_type, "
          + " id_no, "
          + " msg_dept, "
          + " chi_name, "
          + " ex_id, "
          + " msg_userid, "
          + " msg_id, "
          + " msg_desc, "
          + " chi_name_flag, "
          + " resend_flag, "
          + " create_txt_date, "
          + " add_mode, "
          + " msg_seqno, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_time,mod_user,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,"
          + "to_char(sysdate,'yyyymmdd'),"
          + "?,"
          + "?,"
          + "sysdate,?,?)";

  Object[] param =new Object[]
       {
        kk1,
        wp.item_ss("aud_type"),
        kk2,
        wp.item_ss("msg_dept"),
        wp.item_ss("chi_name"),
        wp.item_ss("ex_id"),
        wp.item_ss("msg_userid"),
        wp.item_ss("msg_id"),
        wp.item_ss("msg_desc"),
        wp.item_ss("chi_name_flag"),
        wp.item_ss("resend_flag"),
        col_ss("create_txt_date"),
        "O",
        msg_seqStr,
        wp.loginUser,
        wp.mod_seqno(),
        wp.loginUser,
        wp.mod_pgm()
       };

  sqlExec(is_sql, param);
  if (sql_nrow <= 0) errmsg(sql_errtext);

  return rc;
 }
// ************************************************************************
 @Override
 public int dbUpdate()
 {
  actionInit("U");
  dataCheck();
  if (rc!=1) return rc;

  is_sql= "update " +control_tab_name + " set "
         + "aud_type = ?, "
         + "msg_dept = ?, "
         + "chi_name = ?, "
         + "ex_id = ?, "
         + "msg_desc = ?, "
         + "mod_user  = ?, "
         + "mod_seqno = nvl(mod_seqno,0)+1, "
         + "mod_time  = sysdate, "
         + "mod_pgm   = ? "
         + "where rowid = ? "
         + "and   mod_seqno = ? ";

  Object[] param =new Object[]
    {
     wp.item_ss("aud_type"),
     wp.item_ss("msg_dept"),
     wp.item_ss("chi_name"),
     wp.item_ss("ex_id"),
     wp.item_ss("msg_desc"),
     wp.loginUser,
     wp.item_ss("mod_pgm"),
     wp.item_RowId("rowid"),
     wp.item_num("mod_seqno")
    };

  sqlExec(is_sql, param);
  if (sql_nrow <= 0) errmsg(this.sql_errtext);

  return rc;
 }
// ************************************************************************
 @Override
 public int dbDelete()
 {
  actionInit("D");
  dataCheck();
  if (rc!=1)return rc;

  is_sql = "delete " +control_tab_name + " " 
         + "where rowid = ?";

  Object[] param =new Object[]
    {
     wp.item_RowId("rowid")
    };

  rc = sqlExec(is_sql, param);
  if (sql_nrow <= 0) errmsg(this.sql_errtext);

  return rc;
 }
// ************************************************************************

}  // End of class
