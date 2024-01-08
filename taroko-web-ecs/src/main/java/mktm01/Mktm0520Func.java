/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 111/12/16  V1.00.01   Zuwei Su      Initial                              *
***************************************************************************/
package mktm01;

import busi.FuncEdit;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Mktm0520Func extends FuncEdit {
    public Mktm0520Func(TarokoCommon wr) {
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
        return 1;
    }

    // ************************************************************************
    @Override
    public void dataCheck() {
        if (!this.ibAdd) {
        }
        if (this.ibDelete) {
//            if (wp.itemStr("apr_flag").equals("Y")) {
//                errmsg("該筆資料已覆核, 不可刪除!");
//                return;
//            }
        }

        if (this.isAdd())
            return;

        // -other modify-
//        sqlWhere = "where rowid = x'"
//                + wp.itemStr("rowid")
//                + "'"
//                + " and nvl(mod_seqno,0)="
//                + wp.modSeqno();
//
//        if (this.isOtherModify(controlTabName, sqlWhere)) {
//            errmsg("請重新查詢 !");
//            return;
//        }
    }

    // ************************************************************************
    @Override
    public int dbInsert() {
        return 1;
    }

    // ************************************************************************
    @Override
    public int dbUpdate() {
        return rc;
    }

    // ************************************************************************
    @Override
    public int dbDelete() {
        actionInit("D");
        dataCheck();
        if (rc != 1)
            return rc;

        if ("Y".equals(wp.itemStr("apr_flag"))) {
            dbInsertCrdEmployeeAT();
        } else {
            strSql = "delete crd_employee_a_t where hex(rowid) = ?";
            Object[] param = new Object[] {wp.itemStr("rowid")};
            sqlExec(strSql, param);
            if (sqlRowNum <= 0) {
                rc = 0;
                errmsg("刪除 crd_employee_a_t 錯誤");
            } else  {
                rc = 1;
            }
        }

        return rc;
    }

    // ************************************************************************
    private void dbInsertCrdEmployeeAT() {
        // check  2.6.  insert之前,檢核該筆員編是否已存在,【CRD_EMPLOYEE_A_T】 是:再檢核AUD_TYPE=U,D(任一項) 是:顯示資料已重覆,不可異動db
        strSql = "select aud_type from crd_employee_a_t where employ_no = ?";
        sqlSelect(strSql, new Object[] {wp.itemStr("employ_no")});
        if (sqlRowNum > 0) {
            String audType = colStr("aud_type");
            if ("U".equals(audType) || "D".equals(audType)) {
                errmsg("資料已重覆,不可異動db");
                rc = -1;
                return;
            }
        }

        dbSelectCrdEmployeeA();
        strSql = " insert into crd_employee_a_t("
                + "mod_date, "
                + "employ_no, "
                + "chi_name, "
                + "id, "
                + "id_code, "
                + "acct_no, "
                + "unit_no, "
                + "unit_name, "
                + "subunit_no, "
                + "subunit_name, "
                + "position_id, "
                + "position_name, "
                + "status_id, "
                + "status_name, "
                + "file_name, "
                + "corp_no, "
                + "subsidiary_no, "
                + "error_code, "
                + "error_desc, "
                + "aud_type, "
                + "description, "
                + "apr_date, "
                + "apr_user, "
                + "apr_flag, "
                + "crt_date, "
                + "crt_user, "
                + "mod_user, "
                + "mod_time, "
                + "mod_pgm"
                + ") " + 
                "values("
                + "to_char(sysdate, 'yyyymmdd'), "
                + "?, ?, ?, ?, ?, ?, ?, ?, "
                + "?, ?, ?, ?, ?, ?, ?, ?, "
                + "'', '', "
                + "'D', '', '', '', 'N', to_char(sysdate, 'yyyymmdd'), ?, ?, "
                + "sysdate, 'mktm0520'"
                + ")";

        Object[] param = new Object[] {
                colStr("employ_no"), 
                colStr("chi_name"), 
                colStr("id"), 
                colStr("id_code"), 
                colStr("acct_no"), 
                colStr("unit_no"), 
                colStr("unit_name"), 
                colStr("subunit_no"), 
                colStr("subunit_name"), 
                colStr("position_id"), 
                colStr("position_name"), 
                colStr("status_id"), 
                colStr("status_name"), 
                colStr("file_name"), 
                colStr("corp_no"), 
                colStr("subsidiary_no"), 
                wp.loginUser, 
                wp.loginUser
                };

        sqlExec(strSql, param);
        if (sqlRowNum <= 0) {
            errmsg("新增 crd_employee_a_t 錯誤");
            return;
        }
        return;
    }

    // ************************************************************************
    private int dbSelectCrdEmployeeA() {
        strSql = " select "
                + "employ_no, "
                + "chi_name, "
                + "id, "
                + "id_code, "
                + "acct_no, "
                + "unit_no, "
                + "unit_name, "
                + "subunit_no, "
                + "subunit_name, "
                + "position_id, "
                + "position_name, "
                + "status_id, "
                + "status_name, "
                + "file_name, "
                + "corp_no, "
                + "subsidiary_no, "
                + "aud_type, "
                + "description, "
                + "apr_date, "
                + "apr_user, "
                + "apr_flag, "
                + "crt_date, "
                + "crt_user, "
                + "mod_user, "
                + "mod_time, "
                + "mod_pgm"
                + " from crd_employee_a "
                + " where hex(rowid) = ?";

        Object[] param = new Object[] {wp.itemStr("rowid")};
        sqlSelect(strSql, param);

        return rc;
    }
    // ************************************************************************

} // End of class
