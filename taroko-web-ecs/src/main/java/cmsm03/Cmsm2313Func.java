/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *    DATE    Version    AUTHOR              DESCRIPTION                      *
 *  --------  -------------------  ------------------------------------------ *
 * 112/05/05  V1.00.00  Ryan      Program Initial & Naming rule update     *
 ******************************************************************************/
package cmsm03;

import busi.FuncAction;

public class Cmsm2313Func extends FuncAction {
    @Override
    public void dataCheck() {

    }

    @Override
    public int dbInsert() {
        return 0;
    }

    @Override
    public int dbUpdate() {
        return 0;
    }

    @Override
    public int dbDelete() {
        return 0;
    }

    @Override
    public int dataProc() {
        msgOK();

        strSql = "update hce_apply_data set" +
                " opt_ver_cnt= 0, opt_ver_send_cnt= 0" +
                " where rowid =?";
        setRowId(1, wp.itemStr("rowid"));
        sqlExec(strSql);
        if (sqlRowNum <= 0) {
            sqlErr("update HCE_APPLY_DATA.error");
        }

        return rc;
    }
}
