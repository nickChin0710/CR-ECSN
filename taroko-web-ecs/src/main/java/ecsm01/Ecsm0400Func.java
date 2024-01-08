/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/07/08  V1.00.02   Allen Ho      Initial                              *
 * 111/11/28  V1.00.03  jiangyigndong  updated for project coding standard  *
 *                                                                          *
 ***************************************************************************/
package ecsm01;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Ecsm0400Func extends FuncEdit
{
  private final String PROGNAME = "航空公司 PGP參數維護處理程式110/07/08 V1.00.01";
  String kk1;
  String controlTabName = "mkt_pgp_parm";

  public Ecsm0400Func(TarokoCommon wr)
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
          errmsg("[航空公司] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
        }
      }


    if ((this.ibAdd)||(this.ibUpdate))
    {
      if (kk1.length()==0)
      {
        errmsg("[航空公司] 必須選擇!");
        return;
      }

      if ((wp.itemStr("public_key_name_i").length()==0)&&
              (wp.itemStr("private_key_name_i").length()==0)&&
              (wp.itemStr("public_key_name_o").length()==0))
      {
        errmsg("KEY至少必須輸入一項 !");
        return;
      }

      if ((wp.itemStr("public_key_name_i").length()!=0)||
              (wp.itemStr("private_key_name_i").length()!=0))
        if (wp.itemStr("t_passphase_i").length()==0)
        {
          errmsg("內部密碼必須輸入 !");
          return;
        }

      if (!wp.itemStr("t_passphase_i").equals(wp.itemStr("t_passphase_i_c")))
      {
        errmsg("內部密碼輸入不一致,請重新確認 !");
        return;
      }

      if (wp.colStr("hide_ref_code").length()==0)
      {
        busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
        comr.setConn(wp);
        wp.colSet("hide_ref_code" , comr.getSeqno("MKT_MODSEQ"));
      }
      busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
      wp.colSet("passphase_i"   , comm.hideZipData(wp.itemStr("t_passphase_i") , wp.colStr("hide_ref_code")));
      wp.colSet("passphase_i_c" , wp.colStr("passphase_i"));
    }

    if (this.ibAdd)
    {
      if (wp.itemStr("kk_ref_ip_code").length()==0)
      {
        strSql = " select air_type "
                + " from   mkt_pgp_parm "
                + " where ref_ip_code !=''  "
                + " and   air_type = ?  "
        ;
        Object[] param = new Object[] {wp.itemStr("kk_air_type")};
        sqlSelect(strSql,param);

        if (sqlRowNum > 0)
        {
          errmsg("1參考IP代碼不可空白非空白並存 !");
          return;
        }
      }
      else
      {
        strSql = " select air_type "
                + " from   mkt_pgp_parm "
                + " where ref_ip_code =''  "
                + " and   air_type = ?  "
        ;
        Object[] param = new Object[] {wp.itemStr("kk_air_type")};
        sqlSelect(strSql,param);

        if (sqlRowNum > 0)
        {
          errmsg("2參考IP代碼不可空白非空白並存 !");
          return;
        }
      }
    }
    if ((this.ibAdd)||(this.ibUpdate))
    {
      if (wp.itemStr("public_key_name_i").length()!=0)
      {
        strSql = " select air_type "
                + " from   mkt_pgp_keydtl "
                + " where air_type    = ? "
                + " and   key_type    = '1' "
        ;
        Object[] param = new Object[] {kk1};
        sqlSelect(strSql,param);

        if (sqlRowNum <=0 )
        {
          errmsg("內部 public KEY ["+kk1+"]["+sqlRowNum+"] 並未上傳 !");
          return;
        }
      }

      if (wp.itemStr("private_key_name_i").length()!=0)
      {
        strSql = " select air_type "
                + " from   mkt_pgp_keydtl "
                + " where air_type = ?  "
                + " and   key_type = '2'  "
        ;
        Object[] param = new Object[] {kk1};
        sqlSelect(strSql,param);

        if (sqlRowNum <=0 )
        {
          errmsg("內部 private KEY ["+kk1+"]["+sqlRowNum+"] 並未上傳 !");
          return;
        }
      }

      if (wp.itemStr("public_key_name_o").length()!=0)
      {
        strSql = " select air_type "
                + " from   mkt_pgp_keydtl "
                + " where air_type = ?  "
                + " and   key_type = '3'  "
        ;
        Object[] param = new Object[] {kk1};
        sqlSelect(strSql,param);

        if (sqlRowNum <=0 )
        {
          errmsg("外部 public KEY ["+kk1+"]["+sqlRowNum+"] 並未上傳 !");
          return;
        }
      }



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
            + " pgp_name, "
            + " public_key_name_i, "
            + " private_key_name_i, "
            + " public_key_name_o, "
            + " hide_ref_code, "
            + " passphase_i, "
            + " ref_ip_code, "
            + " crt_date, "
            + " crt_user, "
            + " mod_seqno, "
            + " mod_user, "
            + " mod_time,mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,"
            + "?,?,'',"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "?,"
            + "sysdate,?)";

    Object[] param =new Object[]
            {
                    kk1,
                    wp.itemStr("pgp_name"),
                    wp.itemStr("public_key_name_i"),
                    wp.itemStr("private_key_name_i"),
                    wp.itemStr("public_key_name_o"),
                    wp.colStr("hide_ref_code"),
                    wp.colStr("passphase_i"),
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
  public int dbUpdate() {
    actionInit("U");
    dataCheck();
    if (rc!=1) return rc;

    strSql= "update " + controlTabName + " set "
            + "pgp_name = ?, "
            + "public_key_name_i = ?, "
            + "private_key_name_i = ?, "
            + "public_key_name_o = ?, "
            + "hide_ref_code = ?, "
            + "passphase_i = ?, "
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
                    wp.itemStr("pgp_name"),
                    wp.itemStr("public_key_name_i"),
                    wp.itemStr("private_key_name_i"),
                    wp.itemStr("public_key_name_o"),
                    wp.colStr("hide_ref_code"),
                    wp.colStr("passphase_i"),
                    wp.loginUser,
                    wp.loginUser,
                    wp.itemStr("mod_pgm"),
                    wp.itemRowId("rowid"),
                    wp.itemNum("mod_seqno")
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("更新 "+ controlTabName +" 錯誤");

    try {
      procImpMsg();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
  public int dbInsertI2Pubi(String tableName, String[] columnCol, String[] columnDat) throws Exception
  {
    String[] columnData = new String[50];
    String   stra="",strb="";
    int      skipLine= 0;
    long     listCnt   = 50;
    strSql= " insert into  " + tableName + " (";
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " mod_user, "
            + " mod_time,mod_pgm "
            + " ) values (";
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      strSql = strSql + "?," ;
    }
    strSql = strSql
            + "?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 =new Object[50];
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      stra = columnDat[inti];
      param1[skipLine]= stra ;
      skipLine++;
    }
    param1[skipLine++]= "Y";
    param1[skipLine++]= wp.loginUser;
    param1[skipLine++]= wp.loginUser;
    param1[skipLine++]= wp.sysDate + wp.sysTime;
    param1[skipLine++]= wp.modPgm();
    Object[] param = Arrays.copyOf(param1,skipLine);
    wp.dupRecord = "Y";
    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    return rc;
  }
  // ************************************************************************
  public int dbDeleteD2Pubi(String tableName) throws Exception
  {
    strSql = "delete  "+tableName+" "
            + "where air_type = ? "
            + "and   ref_ip_code = ? "
            + "and   key_type = ? "
    ;

    Object[] param =new Object[]
            {
                    wp.itemStr("air_type"),
                    wp.itemStr("ref_ip_code"),
                    "1"
            };

    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) rc=0;else rc=1;
    if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbInsertI2Prii(String tableName, String[] columnCol, String[] columnDat) throws Exception
  {
    String[] columnData = new String[50];
    String   stra="",strb="";
    int      skipLine= 0;
    long     listCnt   = 50;
    strSql= " insert into  " + tableName + " (";
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " mod_user, "
            + " mod_time,mod_pgm "
            + " ) values (";
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      strSql = strSql + "?," ;
    }
    strSql = strSql
            + "?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 =new Object[50];
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      stra = columnDat[inti];
      param1[skipLine]= stra ;
      skipLine++;
    }
    param1[skipLine++]= "Y";
    param1[skipLine++]= wp.loginUser;
    param1[skipLine++]= wp.loginUser;
    param1[skipLine++]= wp.sysDate + wp.sysTime;
    param1[skipLine++]= wp.modPgm();
    Object[] param = Arrays.copyOf(param1,skipLine);
    wp.dupRecord = "Y";
    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    return rc;
  }
  // ************************************************************************
  public int dbDeleteD2Prii(String tableName) throws Exception
  {
    strSql = "delete  "+tableName+" "
            + "where air_type = ? "
            + "and   ref_ip_code = ? "
            + "and   key_type = ? "
    ;

    Object[] param =new Object[]
            {
                    wp.itemStr("air_type"),
                    wp.itemStr("ref_ip_code"),
                    "2"
            };

    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) rc=0;else rc=1;
    if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbInsertI2Pubo(String tableName, String[] columnCol, String[] columnDat) throws Exception
  {
    String[] columnData = new String[50];
    String   stra="",strb="";
    int      skipLine= 0;
    long     listCnt   = 50;
    strSql= " insert into  " + tableName + " (";
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      strSql = strSql + stra + ",";
    }

    strSql = strSql
            + " apr_flag, "
            + " apr_date, "
            + " apr_user, "
            + " mod_user, "
            + " mod_time,mod_pgm "
            + " ) values (";
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      strSql = strSql + "?," ;
    }
    strSql = strSql
            + "?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param1 =new Object[50];
    for (int inti=0;inti<listCnt;inti++)
    {
      stra = columnCol[inti];
      if (stra.length()==0) continue;
      stra = columnDat[inti];
      param1[skipLine]= stra ;
      skipLine++;
    }
    param1[skipLine++]= "Y";
    param1[skipLine++]= wp.loginUser;
    param1[skipLine++]= wp.loginUser;
    param1[skipLine++]= wp.sysDate + wp.sysTime;
    param1[skipLine++]= wp.modPgm();
    Object[] param = Arrays.copyOf(param1,skipLine);
    wp.dupRecord = "Y";
    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) rc=0;else rc=1;

    return rc;
  }
  // ************************************************************************
  public int dbDeleteD2Pubo(String tableName) throws Exception
  {
    strSql = "delete  "+tableName+" "
            + "where air_type = ? "
            + "and   ref_ip_code = ? "
            + "and   key_type = ? "
    ;

    Object[] param =new Object[]
            {
                    wp.itemStr("air_type"),
                    wp.itemStr("ref_ip_code"),
                    "3"
            };

    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) rc=0;else rc=1;
    if (rc!=1) errmsg("刪除 "+ tableName +" 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbInsertEcsMediaErrlog(String tranSeqStr, String[] errMsg ) throws Exception
  {
    dateTime();
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    comr.setConn(wp);

    if (!comm.isNumber(errMsg[10])) errMsg[10]="0";
    if (!comm.isNumber(errMsg[1])) errMsg[1]="0";
    if (!comm.isNumber(errMsg[2])) errMsg[2]="0";

    strSql= " insert into ecs_media_errlog ("
            + " crt_date, "
            + " crt_time, "
            + " file_name, "
            + " unit_code, "
            + " main_desc, "
            + " error_seq, "
            + " error_desc, "
            + " line_seq, "
            + " column_seq, "
            + " column_data, "
            + " trans_seqno, "
            + " column_desc, "
            + " program_code, "
            + " mod_time, "
            + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?,?," // 10 record
            + "?,?,?,"               // 4 trvotfd
            + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param =new Object[]
            {
                    wp.sysDate,
                    wp.sysTime,
                    wp.itemStr("zz_file_name"),
                    comr.getObjectOwner("3",wp.modPgm()),
                    errMsg[0],
                    Integer.valueOf(errMsg[1]),
                    errMsg[4],
                    Integer.valueOf(errMsg[10]),
                    Integer.valueOf(errMsg[2]),
                    errMsg[3],
                    tranSeqStr,
                    errMsg[5],
                    wp.modPgm(),
                    wp.sysDate + wp.sysTime,
                    wp.modPgm()
            };

    wp.dupRecord = "Y";
    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) errmsg("新增4 ecs_media_errlog 錯誤");

    return rc;
  }
  // ************************************************************************
  public int dbInsertEcsNotifyLog(String tranSeqStr, int error_cnt ) throws Exception
  {
    busi.ecs.CommRoutine comr = new busi.ecs.CommRoutine();
    comr.setConn(wp);
    dateTime();
    strSql= " insert into ecs_notify_log ("
            + " crt_date, "
            + " crt_time, "
            + " unit_code, "
            + " obj_type, "
            + " notify_head, "
            + " notify_name, "
            + " notify_desc1, "
            + " notify_desc2, "
            + " trans_seqno, "
            + " mod_time, "
            + " mod_pgm "
            + " ) values ("
            + "?,?,?,?,?,?,?,?,?," // 9 record
            + "timestamp_format(?,'yyyymmddhh24miss'),?)";

    Object[] param =new Object[]
            {
                    wp.sysDate,
                    wp.sysTime,
                    comr.getObjectOwner("3",wp.modPgm()),
                    "3",
                    "媒體檔轉入資料有誤(只記錄前100筆)",
                    "媒體檔名:"+wp.itemStr("zz_file_name"),
                    "程式 "+wp.modPgm()+" 轉 "+wp.itemStr("zz_file_name")+" 有"+error_cnt+" 筆錯誤",
                    "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤",
                    tranSeqStr,
                    wp.sysDate + wp.sysTime,
                    wp.modPgm()
            };

    wp.dupRecord = "Y";
    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) errmsg("新增5 ecs_modify_log 錯誤");
    return rc;
  }
  // ************************************************************************
  void procImpMsg() throws Exception
  {
    Object[] param = null;
    if (wp.itemStr("public_key_name_i").length()==0)
    {
      strSql= "delete mkt_pgp_keydtl "
              + "where ref_ip_code = ? "
              + "and   key_type   = '1' "
      ;

      param = new Object[] {wp.colStr("ref_ip_code")};
      sqlExec(strSql,param);

      wp.colSet("chk_imp_pubi" , "尚未匯入");
    }
    else
    {
      strSql= "select max(to_char(mod_time,'yyyy/mm/dd hh24:mi:ss'))  as mod_time "
              + "from mkt_pgp_keydtl "
              + "where ref_ip_code = ? "
              + "and   key_type   = '1' "
      ;

      param = new Object[] {wp.colStr("ref_ip_code")};
      sqlSelect(strSql,param);

      if (colStr("mod_time").length()==0)
        wp.colSet("chk_imp_pubi" , "尚未匯入");
      else
        wp.colSet("chk_imp_pubi" , "[" + colStr("mod_time") + "] 匯入");
    }

    if (wp.itemStr("private_key_name_i").length()==0)
    {
      strSql= "delete mkt_pgp_keydtl "
              + "where ref_ip_code = ? "
              + "and   key_type   = '2' "
      ;

      param = new Object[] {wp.colStr("ref_ip_code")};
      sqlExec(strSql,param);

      wp.colSet("chk_imp_prii" , "尚未匯入");
    }
    else
    {
      strSql= "select max(to_char(mod_time,'yyyy/mm/dd hh24:mi:ss'))  as mod_time "
              + "from mkt_pgp_keydtl "
              + "where ref_ip_code = ? "
              + "and   key_type   = '2' "
      ;
      param = new Object[] {wp.colStr("ref_ip_code")};
      sqlSelect(strSql,param);

      if (colStr("mod_time").length()==0)
        wp.colSet("chk_imp_prii" , "尚未匯入");
      else
        wp.colSet("chk_imp_prii" , "[" + colStr("mod_time") + "] 匯入");
    }

    if (wp.itemStr("public_key_name_o").length()==0)
    {
      strSql= "delete mkt_pgp_keydtl "
              + "where ref_ip_code = ? "
              + "and   key_type   = '3' "
      ;

      param = new Object[] {wp.colStr("ref_ip_code")};
      sqlExec(strSql,param);

      wp.colSet("chk_imp_pubo" , "尚未匯入");
    }
    else
    {
      strSql= "select max(to_char(mod_time,'yyyy/mm/dd hh24:mi:ss'))  as mod_time "
              + "from mkt_pgp_keydtl "
              + "where ref_ip_code = ? "
              + "and   key_type   = '3' "
      ;

      param = new Object[] {wp.colStr("ref_ip_code")};
      sqlSelect(strSql,param);

      if (colStr("mod_time").length()==0)
        wp.colSet("chk_imp_pubo" , "尚未匯入");
      else
        wp.colSet("chk_imp_pubo" , "[" + colStr("mod_time") + "] 匯入");
    }

    return;
  }
// ************************************************************************

// ************************************************************************

}  // End of class
