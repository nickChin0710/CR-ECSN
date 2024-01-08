/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/07  V1.00.00   Zuwei Su      Initial                              *
* 112/03/13  V1.00.01   Zuwei Su      測試問題修訂，修改已覆核資料明細丟失                              *
***************************************************************************/
package mktm02;

import busi.FuncEdit;
import java.util.*;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Mktm6210Func extends FuncEdit {
    private final String PROGNAME = "通路類別代碼維護程式 112/03/13  V1.00.01";
    String channelTypeId;
    String orgControlTabName = "mkt_chantype_parm";
    String controlTabName = "mkt_chantype_parm_t";

    public Mktm6210Func(TarokoCommon wr) {
        wp = wr;
        this.conn = wp.getConn();
    }

    // ************************************************************************
    @Override
    public int querySelect() {
        // TODO Auto-generated method
        return 0;
    }

    // ************************************************************************
    @Override
    public int dataSelect() {
        // TODO Auto-generated method stub
        String procTabName = "";
        procTabName = wp.itemStr("control_tab_name");
        strSql = " select "
                + " channel_type_desc, "
                + " to_char(mod_time,'yyyymmddhh24miss') as mod_time,mod_user,mod_pgm,mod_seqno "
                + " from "
                + procTabName
                + " where rowid = ? ";

        Object[] param = new Object[] {
                wp.itemRowId("rowid")
        };

        sqlSelect(strSql, param);
        if (sqlRowNum <= 0)
            errmsg("查無資料，讀取 " + controlTabName + " 失敗");

        return 1;
    }

    // ************************************************************************
    @Override
    public void dataCheck() {
        if (this.ibAdd) {
            channelTypeId = wp.itemStr("channel_type_id");
        } else {
            channelTypeId = wp.itemStr("channel_type_id");
        }
        if (wp.respHtml.indexOf("_nadd") > 0) {
            if (this.ibAdd) {
                if (channelTypeId.length() > 0) {
                    strSql = "select count(*) as qua "
                            + "from "
                            + orgControlTabName
                            + " where channel_type_id = ? ";
                    Object[] param = new Object[] {
                            channelTypeId
                    };
                    sqlSelect(strSql, param);
                    int qua = Integer.parseInt(colStr("qua"));
                    if (qua > 0) {
                        errmsg("[通路類別代碼] 不可重複(" + orgControlTabName + "), 請重新輸入!");
                        return;
                    }
                }
            }
        }

        if (this.ibUpdate) {
            String bnTable = "";
            if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
                bnTable = "mkt_chantype_data";
            } else {
                bnTable = "mkt_chantype_data_t";
            }
            String strSql = "select count(*) as data_cnt "
                          + "from " + bnTable + " "
                          + "where channel_type_id = ? "
                          ;
            Object[] param = new Object[] {channelTypeId};
            sqlSelect(strSql,param);

            int dataCnt = (Integer.parseInt(colStr("data_cnt")));
            if (dataCnt == 0) {
                errmsg("[交易判斷] 明細沒有設定, 筆數不可為 0  !");
                return;
            }
        }

        if ((this.ibAdd) || (this.ibUpdate))
            if (wp.itemEmpty("channel_type_desc")) {
                errmsg("通路類別說明： 不可空白");
                return;
            }


        if (this.isAdd())
            return;

    }

    // ************************************************************************
    @Override
    public int dbInsert() {
        rc = dataSelect();
        if (rc != 1)
            return rc;
        actionInit("A");
        dataCheck();
        if (rc != 1)
            return rc;

        dbInsertD2T();
        dbInsertI2T();

        strSql = " insert into  "
                + controlTabName
                + " ("
                + " channel_type_id, "
                + " channel_type_desc, "
                + " aud_type, "
                + " crt_date, "
                + " crt_user, "
                + " mod_seqno, "
                + " mod_time,mod_user,mod_pgm "
                + " ) values ("
                + "?,?,?,"
                + "to_char(sysdate,'yyyymmdd'),"
                + "?,"
                + "?,"
                + "sysdate,?,?)";

        Object[] param = new Object[] {
                channelTypeId,
                wp.itemStr("channel_type_desc"),
                wp.itemStr("aud_type"),
                wp.loginUser,
                wp.modSeqno(),
                wp.loginUser,
                wp.modPgm()
        };

        sqlExec(strSql, param);
        if (sqlRowNum <= 0)
            errmsg("新增 " + controlTabName + " 錯誤");

        return rc;
    }

    // ************************************************************************
    @Override
    public int dbUpdate() {
        rc = dataSelect();
        if (rc != 1)
            return rc;
        actionInit("U");
        dataCheck();
        if (rc != 1)
            return rc;

        strSql = "update "
                + controlTabName
                + " set "
                + "channel_type_desc = ?, "
                + "crt_user  = ?, "
                + "crt_date  = to_char(sysdate,'yyyymmdd'), "
                + "mod_user  = ?, "
                + "mod_seqno = nvl(mod_seqno,0)+1, "
                + "mod_time  = sysdate, "
                + "mod_pgm   = ? "
                + "where rowid = ? "
                + "and   mod_seqno = ? ";

        Object[] param = new Object[] {
                wp.itemStr("channel_type_desc"),
                wp.loginUser,
                wp.loginUser,
                wp.itemStr("mod_pgm"),
                wp.itemRowId("rowid"),
                wp.itemNum("mod_seqno")
        };

        sqlExec(strSql, param);
        if (sqlRowNum <= 0)
            errmsg("更新 " + controlTabName + " 錯誤");

        if (sqlRowNum <= 0)
            rc = 0;
        else
            rc = 1;
        return rc;
    }

    // ************************************************************************
    @Override
    public int dbDelete() {
        rc = dataSelect();
        if (rc != 1)
            return rc;
        actionInit("D");
        dataCheck();
        if (rc != 1)
            return rc;

        dbInsertD2T();

        strSql = "delete " + controlTabName + " " + "where rowid = ?";

        Object[] param = new Object[] {
                wp.itemRowId("rowid")
        };

        sqlExec(strSql, param);
        if (sqlRowNum <= 0)
            rc = 0;
        else
            rc = 1;
        if (sqlRowNum <= 0) {
            errmsg("刪除 " + controlTabName + " 錯誤");
            return (-1);
        }

        return rc;
    }

    // ************************************************************************
    public int dbInsertD2T() {
      msgOK();

      strSql = "delete mkt_chantype_data_t "
              + " where channel_type_id = ? "
              + "";
      // 如果沒有資料回傳成功
      Object[] param = new Object[] {wp.itemStr("channel_type_id"),};

      sqlExec(strSql, param);
      if (sqlRowNum <= 0)
        rc = 0;
      else
        rc = 1;

      if (rc != 1)
        errmsg("刪除 mkt_chantype_data_t 錯誤");

      return rc;

    }

    // ************************************************************************
    public int dbInsertI2T() {
      msgOK();

      strSql = "insert into mkt_chantype_data_t " + "select * " + "from mkt_chantype_data "
          + "where channel_type_id = ? " + "";

      Object[] param = new Object[] {wp.itemStr("channel_type_id")};
      
      wp.dupRecord = "Y";
      sqlExec(strSql, param);

      
      return 1;
    }

    // ************************************************************************
    public int dbInsertI2() throws Exception {
        msgOK();

        String dataType = "";
        if (wp.respHtml.equals("mktm6210_mrcd"))
            dataType = "1";
        strSql = "insert into mkt_chantype_data_t ( "
                + "channel_type_id, "
                + "txcode_sel, "
                + "tx_desc_type, "
                + "tx_desc_name, "
                + "mccc_sel, "
                + "mccc_code, "
                + "crt_date, "
                + "crt_user, "
                + " mod_time, "
                + " mod_user, "
                + " mod_seqno, "
                + " mod_pgm "
                + ") values ("
                + "?,?,?,?,?,?,"
                + "to_char(sysdate,'yyyymmdd'),"
                + "?,"
                + " sysdate, "
                + "?,"
                + "1,"
                + " ? "
                + ")";

        Object[] param = new Object[] {
                wp.itemStr("channel_type_id"),
                "undefined".equals(varsStr("txcode_sel")) ? "" : varsStr("txcode_sel"),
                "undefined".equals(varsStr("tx_desc_type")) ? "" : varsStr("tx_desc_type"),
                varsStr("tx_desc_name"),
                "undefined".equals(varsStr("mccc_sel")) ? "" : varsStr("mccc_sel"),
                varsStr("mccc_code"),
                wp.loginUser,
                wp.loginUser,
                wp.modPgm()
        };

        sqlExec(strSql, param);
        if (sqlRowNum <= 0)
            rc = 0;
        else
            rc = 1;

        if (rc != 1)
            errmsg("新增 mkt_chantype_data_t 錯誤");

        return rc;
    }

    // ************************************************************************
    public int dbDeleteD2() {
        msgOK();

        // 如果沒有資料回傳成功
        Object[] param = new Object[] { wp.itemStr("channel_type_id") };
        strSql = "delete mkt_chantype_data_t "
                + "where channel_type_id = ? ";
        sqlExec(strSql, param);

        return 1;
    }
    // ************************************************************************

} // End of class
