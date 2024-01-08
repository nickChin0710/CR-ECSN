/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 109/12/04  V1.00.01   Allen Ho      Initial                              *
* 111-11-30  V1.00.01  Machao    sync from mega & updated for project coding standard                                                                         *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm0810Func extends FuncEdit
{
 private final String PROGNAME = "IBON專案卡友可兌贈品資料維護　處理程式111-11-30  V1.00.01";
  String kk1;
  String orgControlTabName = "ibn_prog_list";
  String controlTabName = "ibn_prog_list_t";

 public Mktm0810Func(TarokoCommon wr)
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
          + " group_type, "
          + " prog_code, "
          + " prog_s_date, "
          + " prog_e_date, "
          + " gift_no, "
          + " id_no, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " card_no, "
          + " gift_cnt, "
          + " gift_s_date, "
          + " gift_e_date, "
          + " data_type, "
          + " vd_flag, "
          + " proc_flag, "
          + " from_type, "
          + " up_flag, "
          + " create_date, "
          + " create_time, "
          + " p_seqno, "
          + " id_p_seqno, "
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
      kk1 = wp.itemStr2("txn_seqno");
     }
  else
     {
      kk1 = wp.itemStr2("txn_seqno");
     }
  if (wp.respHtml.indexOf("_nadd") > 0)
  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + orgControlTabName
             + " where txn_seqno = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[資料序號] 不可重複("+orgControlTabName+"), 請重新輸入!");
          return;
         }
     }

  if (this.ibAdd)
  if (kk1.length()>0)
     {
      strSql = "select count(*) as qua "
             + "from " + controlTabName
             + " where txn_seqno = ? "
             ;
      Object[] param = new Object[] {kk1};
      sqlSelect(strSql,param);
      int qua =  Integer.parseInt(colStr("qua"));
      if (qua > 0)
         {
          errmsg("[資料序號] 不可重複("+controlTabName+") ,請重新輸入!");
          return;
         }
     }


  if (wp.itemStr2("control_tab_name").equals(orgControlTabName))
     {
      errmsg("該筆資料已覆核, 不可異動!");
      return;
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

  if (this.ibAdd)
     {
      if ((wp.itemStr2("apr_date").length()!=0)&&
          (wp.itemStr2("aud_type").equals("D"))&&
          (wp.itemStr2("control_tab_name").equals(orgControlTabName)))
         {
          errmsg("已覆核資料, 只可修改不可刪除 !");
          return;
         }
     }
  
  if (wp.itemStr2("gift_cnt").length()==0) 
      wp.itemSet("gift_cnt","0");

  if ((this.ibAdd)||(this.ibUpdate))
     {
      if (wp.itemNum("gift_cnt")==0)
         {
          errmsg("[商品數量] 不可為0 ,請重新輸入!");
          return;
         }
      if (wp.itemStr2("prog_code").length()==0)
         {
          errmsg("[活動代碼] 未輸入 ,請重新輸入!");
          return;
         }
      if (wp.itemStr2("gift_no").length()==0)
         {
          errmsg("[商品代號] 未輸入 ,請重新輸入!");
          return;
         }
      if (wp.itemStr2("group_type").equals("4"))
         {
          if (wp.itemStr2("card_no").length()==0)
             {
              errmsg("[卡號] 未輸入 ,請重新輸入!");
              return;
             }
          wp.colSet("data_type" , "3");
         }
      else
         {
          if (wp.itemStr2("id_no").length()==0)
             {
              errmsg("[身分證號] 未輸入 ,請重新輸入!");
              return;
             }
          wp.colSet("data_type" , "1");
         }

      Object[] param = null;
      if (wp.itemStr2("group_type").equals("1"))
         {
          strSql = "select a.p_seqno,a.id_p_seqno "
                 + "from act_acno a,crd_idno b "
                 + " where b.id_no = ? "
                 + " and a.id_p_seqno = b.id_p_seqno "
                 + " order by id_no_code "
                 ;
          param = new Object[] {wp.itemStr2("id_no")};
          sqlSelect(strSql,param);
          if (sqlRowNum <= 0)
             {
              errmsg("[帳戶] 不存在 ,請重新輸入!");
              return;
             }
          wp.itemSet("id_p_seqno",colStr("id_p_seqno"));
          wp.colSet("vd_flag"   , "N");
         }

      if (wp.itemStr2("group_type").equals("2"))
         {
          strSql = "select a.p_seqno,a.id_p_seqno "
                 + "from dba_acno a,dbc_idno b "
                 + " where b.id_no = ? "
                 + " and a.id_p_seqno = b.id_p_seqno "
                 + " order by id_no_code "
                 ;
          param = new Object[] {wp.itemStr2("id_no")};
          sqlSelect(strSql,param);
          if (sqlRowNum <= 0)
             {
              errmsg("[帳戶] 不存在 ,請重新輸入!");
              return;
             }
          wp.itemSet("id_p_seqno",colStr("id_p_seqno"));
          wp.colSet("vd_flag"   , "Y");
         }

      if (wp.itemStr2("group_type").equals("3"))
         {
          strSql = "select a.p_seqno,a.id_p_seqno "
                 + "from act_acno a,crd_idno b "
                 + " where b.id_no = ? "
                 + " and a.id_p_seqno = b.id_p_seqno "
                 + " order by id_no_code "
                 ;
          param = new Object[] {wp.itemStr2("id_no")};
          sqlSelect(strSql,param);
          if (sqlRowNum <= 0)
             {
              strSql = "select a.p_seqno,a.id_p_seqno "
                     + "from dba_acno a,dbc_idno b "
                     + " where b.id_no = ? "
                     + " and a.id_p_seqno = b.id_p_seqno "
                     + " order by id_no_code "
                     ;
              param = new Object[] {wp.itemStr2("id_no")};
              sqlSelect(strSql,param);
              if (sqlRowNum <= 0)
                 {
                  errmsg("[帳戶] 不存在 ,請重新輸入!");
                  return;
                 }
             }
          wp.itemSet("id_p_seqno" , "");
          wp.colSet("vd_flag"     , "");
         }

      if (wp.itemStr2("group_type").equals("4"))
         {
          strSql = "select p_seqno,id_p_seqno "
                 + "from crd_card "
                 + " where card_no = ? "
                 ;
          param = new Object[] {wp.itemStr2("card_no")};
          sqlSelect(strSql,param);
          if (sqlRowNum <= 0)
             {
              strSql = "select p_seqno,id_p_seqno "
                     + "from dbc_card "
                     + " where card_no = ? "
                     ;
              param = new Object[] {wp.itemStr2("card_no")};
              sqlSelect(strSql,param);
              if (sqlRowNum <= 0)
                 {
                  errmsg("[Debit 卡號] 不存在 ,請重新輸入!");
                  return;
                 }
              wp.colSet("vd_flag"   , "Y");
             }
          else
             {
              wp.colSet("vd_flag"   , "n");
             }
          wp.colSet("data_type"   , "3");
         }

      strSql = "select gift_s_date,gift_e_date "
             + "from ibn_prog_gift "
             + " where prog_code = ? "
             + " and prog_s_date = ? "
             + " and gift_no     = ? "
             ;
      param = new Object[] {wp.itemStr2("prog_code"),wp.itemStr2("prog_s_date"),wp.itemStr2("gift_no")};
      sqlSelect(strSql,param);

      if ((wp.itemStr2("txn_seqno").length()==0)||
          (this.ibAdd))
         {
          busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
          comr.setConn(wp);
          wp.itemSet("txn_seqno" , comr.getSeqno("COL_MODSEQ"));
          kk1 = wp.itemStr2("txn_seqno");
         }    
      String dataType ="",dataId="";
      if ( wp.itemStr2("group_type").equals("4"))
         {
          dataType = "3";
          dataId   =  wp.itemStr2("card_no");
         }
      else
         {
          dataType = "1";
          dataId   =  wp.itemStr2("id_no");
         }
      strSql = " select "
             + " tot_gift_cnt ,"
             + " rem_gift_cnt "
             + " from ibn_prog_dtl "
             + " where data_type   = ? "
             + " and   data_id     = ? "
             + " and   group_type  = ? "
             + " and   prog_code   = ? "
             + " and   prog_s_date = ? "
             ;

      param = new Object[] {dataType,
                            dataId,
                            wp.itemStr2("group_type"),
                            wp.itemStr2("prog_code"),
                            wp.itemStr2("prog_s_date")};
      sqlSelect(strSql,param);
      if (sqlRowNum <= 0)
         {
          if (wp.itemNum("gift_cnt")<0)
             {
              errmsg("累計贈品數量小於調整異動數量, 請按[可兌贈品數量查詢]鍵 !");
              return;
             }
         }
      if (wp.itemNum("gift_cnt")+colNum("rem_gift_cnt")<0)
         {
          errmsg("可兌贈品數量小於調整異動數量, 請按[可兌贈品數量查詢]鍵 !");
          return;
         }


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
          + " txn_seqno, "
          + " apr_flag, "
          + " aud_type, "
          + " group_type, "
          + " prog_code, "
          + " prog_s_date, "
          + " prog_e_date, "
          + " gift_no, "
          + " id_no, "
          + " card_no, "
          + " gift_cnt, "
          + " gift_s_date, "
          + " gift_e_date, "
          + " data_type, "
          + " vd_flag, "
          + " proc_flag, "
          + " from_type, "
          + " up_flag, "
          + " create_date, "
          + " create_time, "
          + " p_seqno, "
          + " id_p_seqno, "
          + " crt_date, "
          + " crt_user, "
          + " mod_seqno, "
          + " mod_user, "
          + " mod_time,mod_pgm "
          + " ) values ("
          + "?,?,?,?,?,?,?,?,?,?,?,"
          + "?,?,?,?,?,?,?,to_char(sysdate,'yyyymmdd'),to_char(sysdate,'hh24miss'),?,?,"
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
        wp.itemStr2("group_type"),
        wp.itemStr2("prog_code"),
        wp.itemStr2("prog_s_date"),
        wp.itemStr2("prog_e_date"),
        wp.itemStr2("gift_no"),
        wp.itemStr2("id_no"),
        wp.itemStr2("card_no"),
        wp.itemNum("gift_cnt"),
        colStr("gift_s_date"),
        colStr("gift_e_date"),
        wp.colStr("data_type"),
        wp.colStr("vd_flag"),
        "N",
        "O",
        "1",
        colStr("p_seqno"),
        colStr("id_p_seqno"),
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
         + "group_type = ?, "
         + "gift_no = ?, "
         + "id_no = ?, "
         + "card_no = ?, "
         + "gift_cnt = ?, "
         + "prog_e_date = ?, "
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
     wp.itemStr2("apr_flag"),
     wp.itemStr2("group_type"),
     wp.itemStr2("gift_no"),
     wp.itemStr2("id_no"),
     wp.itemStr2("card_no"),
     wp.itemNum("gift_cnt"),
     wp.itemStr2("prog_e_date"),
     colStr("p_seqno"),
     colStr("id_p_seqno"),
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
