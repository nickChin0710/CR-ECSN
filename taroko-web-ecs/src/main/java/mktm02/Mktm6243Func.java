/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 110/07/09  V1.00.03   Allen Ho      Initial                              *
 * 112/03/22  V1.00.04   jiangyingdong      sync code from mega             *
 *                                                                          *
 ***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;
// ************************************************************************
public class Mktm6243Func extends FuncEdit
{
  private final String PROGNAME = "同發卡日卡片優先順序維護處理程式110/07/09 V1.00.01";
  String kk1;
  String controlTabName = "mkt_bn_data";

  public Mktm6243Func(TarokoCommon wr)
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
      kk1 = wp.itemStr("kk_data_type");
    }
    else
    {
      kk1 = wp.itemStr("data_type");
    }
    if (this.ibAdd)
      if (kk1.length()>0)
      {
        strSql = "select count(*) as qua "
                + "from " + controlTabName
                + " where data_type = ? "
                + " and   table_name  =  'MKT_FSTP_PARM_CARD' "
                + " and   data_key  =  'FSTP_CARD_PRIORITY' "
        ;
        Object[] param = new Object[] {kk1};
        sqlSelect(strSql,param);
        int qua =  Integer.parseInt(colStr("qua"));
        if (qua > 0)
        {
          errmsg("[優先順序] 不可重複("+ controlTabName +") ,請重新輸入!");
          return;
        }
      }



    if (this.ibAdd)
    {
      if (wp.itemStr("kk_data_type").length()==0)
      {
        errmsg("[優先順序] 不可為空白 !");
        return;
      }
      if (wp.itemStr("kk_data_type").length()==0) wp.itemSet("kk_data_type", "0");
      wp.colSet("kk_data_type", String.format("%04d",(int)wp.itemNum("kk_data_type")));
      wp.itemSet("kk_data_type", wp.colStr("kk_data_type"));
      kk1 = wp.itemStr("kk_data_type");
    }

    if ((this.ibAdd)||(this.ibUpdate))
    {
      if (wp.itemStr("data_code").length()==0)
      {
        errmsg("[團代] 不可為空白 !");
        return;
      }
      strSql = "select  "
              + " data_code "
              + " from  mkt_bn_data "
              + " where table_name = 'MKT_FSTP_PARM_CARD'  "
              + " and   data_key   = 'FSTP_CARD_PRIORITY'  "
              + " and   data_code   = ?  "
              + " and   data_code2  = ?  "
      ;
      Object[] param = new Object[] {wp.itemStr("data_code"),wp.itemStr("data_code2")};
      sqlSelect(strSql,param);

      if (sqlRowNum > 0)
      {
        errmsg("[團代+卡種] 重複 ");
        return;
      }
      if (wp.itemStr("data_code2").length()==0)
      {
        strSql = "select  "
                + " nvl(min(data_type),'') as data_type "
                + " from  mkt_bn_data "
                + " where table_name = 'MKT_FSTP_PARM_CARD'  "
                + " and   data_key   = 'FSTP_CARD_PRIORITY'  "
                + " and   data_code   = ?  "
                + " and   data_code2  != ''   "
        ;
        param = new Object[] {wp.itemStr("data_code")};
        sqlSelect(strSql,param);

        if ((colStr("data_type").length()>0)&&
                (colStr("data_type").compareTo(wp.itemStr("kk_data_type"))>0))
        {
          errmsg("[同團代,非空白卡種優先順序("+colStr("data_type")+")必須較前]");
          return;
        }
      }
      else
      {
        strSql = "select  "
                + " nvl(min(data_type),'') as data_type "
                + " from  mkt_bn_data "
                + " where table_name = 'MKT_FSTP_PARM_CARD'  "
                + " and   data_key   = 'FSTP_CARD_PRIORITY'  "
                + " and   data_code   = ?  "
                + " and   data_code2  = ''   "
        ;
        param = new Object[] {wp.itemStr("data_code")};
        sqlSelect(strSql,param);

        if ((colStr("data_type").length()>0)&&
                (colStr("data_type").compareTo(wp.itemStr("kk_data_type"))<0))
        {
          errmsg("[同團代,空白卡種優先順序("+colStr("data_type")+")必須較後]");
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
            + " data_type, "
            + " data_code, "
            + " data_code2, "
            + " table_name, "
            + " data_key, "
            + " crt_date, "
            + " crt_user, "
            + " mod_seqno, "
            + " mod_user, "
            + " mod_time,mod_pgm "
            + " ) values ("
            + "?,?,?,"
            + "?,?,"
            + "to_char(sysdate,'yyyymmdd'),"
            + "?,"
            + "?,"
            + "?,"
            + "sysdate,?)";

    Object[] param =new Object[]
            {
                    kk1,
                    wp.itemStr("data_code"),
                    wp.itemStr("data_code2"),
                    "MKT_FSTP_PARM_CARD",
                    "FSTP_CARD_PRIORITY",
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
            + "data_code = ?, "
            + "data_code2 = ?, "
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
                    wp.itemStr("data_code"),
                    wp.itemStr("data_code2"),
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
  public int dbInsertI2Aaa2(String tableName, String[] columnCol, String[] columnDat) throws Exception
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
  public int dbDeleteD2Aaa2(String tableName) throws Exception
  {
    strSql = "delete  "+tableName+" "
            + "where table_name = ? "
            + "and   data_key = ? "
    ;

    Object[] param =new Object[]
            {
                    "MKT_FSTP_PARM_CARD",
                    "FSTP_CARD_PRIORITY"
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
  public int dbInsertEcsNotifyLog(String tranSeqstr, int errorCnt ) throws Exception
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
                    "程式 "+wp.modPgm()+" 轉 "+wp.itemStr("zz_file_name")+" 有"+errorCnt+" 筆錯誤",
                    "請至 mktq0040 檔案轉入錯誤紀錄檔查詢 檢視錯誤",
                    tranSeqstr,
                    wp.sysDate + wp.sysTime,
                    wp.modPgm()
            };

    wp.dupRecord = "Y";
    sqlExec(strSql, param, false);
    if (sqlRowNum <= 0) errmsg("新增5 ecs_modify_log 錯誤");
    return rc;
  }
  // ************************************************************************
  public int updateMktBnDataReset(String dataType, String dataCode, String dataCode2, String newDataType) throws Exception
  {
    strSql= "update mkt_bn_data set "
            + "data_type  = ? "
            + "where table_name = 'MKT_FSTP_PARM_CARD'  "
            + "and   data_key   = 'FSTP_CARD_PRIORITY'  "
            + "and   data_type  = ? "
            + "and   data_code  = ? "
            + "and   data_code2 = ? "
    ;

    Object[] param =new Object[]
            {
                    newDataType,
                    dataType,
                    dataCode,
                    dataCode2
            };

    sqlExec(strSql, param);
    if (sqlRowNum <= 0) errmsg("更新 mkt_bn_data 錯誤");

    if (sqlRowNum <= 0) rc=0;else rc=1;
    return rc;
  }

// ************************************************************************

}  // End of class
