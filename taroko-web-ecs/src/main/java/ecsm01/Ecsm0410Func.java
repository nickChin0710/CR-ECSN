/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/04/16  V1.00.01   Allen Ho      Initial                              *
 * 111/11/28  V1.00.02  jiangyigndong  updated for project coding standard  *
 *                                                                          *
 ***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0410Func extends FuncEdit
{
  private final String PROGNAME = "航空公司代碼維護處理程式110/04/16 V1.00.01";
  String kk1;
  String controlTabName = "mkt_air_parm";

  public Ecsm0410Func(TarokoCommon wr)
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
  public int dataSelect() {
    // TODO Auto-generated method stub
    return 1;
  }
  // ************************************************************************
  @Override
  public void dataCheck() {
    if (this.ibAdd)
    {
      kk1 = wp.itemStr("kk_air_type");
    }
    else
    {
      kk1 = wp.itemStr("air_type");
    }
    if (this.ibAdd)
      if (kk1.length()>0)
      {
        strSql = "select count(*) as qua "
                + "from " + controlTabName
                + " where air_type = ? "
        ;
        Object[] param = new Object[] {kk1};
        sqlSelect(strSql,param);
        int qua =  Integer.parseInt(colStr("qua"));
        if (qua > 0)
        {
          errmsg("[航空公司代碼] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
        }
      }

    if (!wp.itemStr("stop_flag").equals("Y")) wp.itemSet("stop_flag","N");

    if ((this.ibAdd)||(this.ibUpdate))
    {
      if (!wp.itemStr("t_in_air_pwd").equals(wp.itemStr("t_in_air_pwd_c")))
      {
        errmsg("接收密碼輸入不一致,請重新確認 !");
        return;
      }

      if (!wp.itemStr("t_out_air_pwd").equals(wp.itemStr("t_out_air_pwd_c")))
      {
        errmsg("傳送密碼輸入不一致,請重新確認 !");
        return;
      }

      if (wp.colStr("hide_ref_code").length()==0)
      {
        busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
        comr.setConn(wp);
        wp.colSet("hide_ref_code" , comr.getSeqno("MKT_MODSEQ"));
      }
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
      wp.colSet("in_air_pwd"   , comm.hideZipData(wp.itemStr("t_in_air_pwd") , wp.colStr("hide_ref_code")));
      wp.colSet("in_air_pwd_c" , wp.colStr("in_air_pwd"));

      wp.colSet("out_air_pwd"   , comm.hideZipData(wp.itemStr("t_out_air_pwd") , wp.colStr("hide_ref_code")));
      wp.colSet("out_air_pwd_c" , wp.colStr("out_air_pwd"));
    }

    if ((this.ibAdd)||(this.ibUpdate))
      if (wp.itemEmpty("pwd_type"))
      {
        errmsg("加密方式: 不可空白");
        return;
      }


    if (this.isAdd()) return;

  }
  // ************************************************************************
  @Override
  public int dbInsert() {
    actionInit("A");
    dataCheck();
    if (rc!=1) return rc;


    strSql= " insert into  " + controlTabName + " ("
            + " air_type, "
            + " air_name, "
            + " stop_flag, "
            + " stop_date, "
            + " stop_desc, "
            + " pwd_type, "
            + " out_ref_ip_code, "
            + " out_file_name, "
            + " out_zip_file_name, "
            + " in_ref_ip_code, "
            + " in_zip_file_name, "
            + " in_file_name, "
            + " hide_ref_code, "
            + " in_air_pwd, "
            + " out_air_pwd, "
            + " crt_date, "
            + " crt_user, "
            + " mod_user, "
            + " mod_time,mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?,?,?,"
            + "?,?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "sysdate,?)";

    Object[] param =new Object[]
            {
                    kk1,
                    wp.itemStr("air_name"),
                    wp.itemStr("stop_flag"),
                    wp.itemStr("stop_date"),
                    wp.itemStr("stop_desc"),
                    wp.itemStr("pwd_type"),
                    wp.itemStr("out_ref_ip_code"),
                    wp.itemStr("out_file_name"),
                    wp.itemStr("out_zip_file_name"),
                    wp.itemStr("in_ref_ip_code"),
                    wp.itemStr("in_zip_file_name"),
                    wp.itemStr("in_file_name"),
                    wp.colStr("hide_ref_code"),
                    wp.colStr("in_air_pwd"),
                    wp.colStr("out_air_pwd"),
                    wp.loginUser,
                    wp.loginUser,
                    wp.modPgm()
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("新增 "+ controlTabName +" 重複錯誤");

    return rc;
  }
  // ************************************************************************
  @Override
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc!=1) return rc;

    strSql= "update " + controlTabName + " set "
            + "air_name = ?, "
            + "stop_flag = ?, "
            + "stop_date = ?, "
            + "stop_desc = ?, "
            + "pwd_type = ?, "
            + "out_ref_ip_code = ?, "
            + "out_file_name = ?, "
            + "out_zip_file_name = ?, "
            + "in_ref_ip_code = ?, "
            + "in_zip_file_name = ?, "
            + "in_file_name = ?, "
            + "hide_ref_code = ?, "
            + "in_air_pwd = ?, "
            + "out_air_pwd = ?, "
            + "crt_user  = ?, "
            + "crt_date  = to_char(sysdate,'yyyymmdd'), "
            + "mod_user  = ?, "
            + "mod_time  = sysdate, "
            + "mod_pgm   = ? "
            + "where rowid = ? ";

    Object[] param =new Object[]
            {
                    wp.itemStr("air_name"),
                    wp.itemStr("stop_flag"),
                    wp.itemStr("stop_date"),
                    wp.itemStr("stop_desc"),
                    wp.itemStr("pwd_type"),
                    wp.itemStr("out_ref_ip_code"),
                    wp.itemStr("out_file_name"),
                    wp.itemStr("out_zip_file_name"),
                    wp.itemStr("in_ref_ip_code"),
                    wp.itemStr("in_zip_file_name"),
                    wp.itemStr("in_file_name"),
                    wp.colStr("hide_ref_code"),
                    wp.colStr("in_air_pwd"),
                    wp.colStr("out_air_pwd"),
                    wp.loginUser,
                    wp.loginUser,
                    wp.itemStr("mod_pgm"),
                    wp.itemRowId("rowid")
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

    if (sqlRowNum <= 0) rc=0;else rc=1;
    return rc;
  }
  // ************************************************************************
  @Override
  public int dbDelete() {
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

}  // End of class
