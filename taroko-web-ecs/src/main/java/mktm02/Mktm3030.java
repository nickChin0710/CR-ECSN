/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *   DATE        Version    AUTHOR              DESCRIPTION                   *
 * ---------    --------  ----------   -------------------------------------- *
 * 110/09/17    V1.00.00   Yang Bo                initial                     *
 * 112/03/20    V1.00.01   Yang Bo           add approval feature             *
 ******************************************************************************/
package mktm02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;

import java.util.Arrays;

public class Mktm3030 extends BaseEdit {
    String rowid;
    int qFrom = 0;

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

        strAction = wp.buttonCode;
        switch (wp.buttonCode) {
            case "X":
                /* 轉換顯示畫面 */
                strAction = "new";
                clearFunc();
                break;
            case "Q":
                /* 查詢功能 */
                strAction = "Q";
                queryFunc();
                break;
            case "R":
                // -資料讀取-
                strAction = "R";
                dataRead();
                break;
            case "A":
                /* 新增功能 */
                strAction = "A";
                saveFunc();
                break;
            case "U":
                /* 更新功能 */
                strAction = "U";
                saveFunc();
                break;
            case "D":
                /* 刪除功能 */
                strAction = "D";
                saveFunc();
                break;
            case "R2":
                /* 參數頁面 資料讀取 */
                strAction = "R2";
                dataRead2();
                break;
            case "U2":
                /* 參數頁面 資料讀取 */
                strAction = "U2";
                dataSave2();
                break;
            case "M":
                /* 瀏覽功能 :skip-page */
                queryRead();
                break;
            case "S":
                /* 動態查詢 */
                querySelect();
                break;
            case "L":
                /* 清畫面 */
                strAction = "";
                clearFunc();
                break;
            case "C1":
                // -覆核處理-
                procApprove();
                break;
            default:
                break;
        }

        dddwSelect();
        initButton();
    }

    @Override
    public void queryFunc() throws Exception {
        wp.whereStr = " where 1=1 "
                + sqlCol(wp.itemStr("ex_proj_code"), "program_code")
                + sqlCol(wp.itemStr("ex_proj_date1"), "apply_date_s")
                + sqlCol(wp.itemStr("ex_proj_date2"), "apply_date_e");
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        wp.selectSQL = "hex(rowid) as rowid, program_code as proj_code, chi_name as proj_desc, " +
                "case when apr_flag = 'Y' then '有效' else '無效' end as active_status, " +
                "consume_type, apply_date_s, apply_date_e, apr_flag";

        wp.daoTable = " mkt_intr_fund";
        wp.whereOrder = " order by proj_code ";

        pageQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr2("此條件查無資料");
            return;
        }

        wp.setPageValue();
    }

    @Override
    public void querySelect() throws Exception {
        rowid = itemKk("data_k1");
        qFrom = 1;

        dataRead();
    }

    @Override
    public void dataRead() throws Exception {
        if (qFrom == 0) {
            if (wp.itemStr("kk_proj_code").length() == 0) {
                alertErr("查詢鍵必須輸入");
                return;
            }
        }

        wp.selectSQL = "hex(rowid) as rowid, program_code as proj_code, apply_date_s, apply_date_e, chi_name as proj_desc, " +
                "reward_bank, reward_finance, exclude_bank, exclude_finance, acct_type_flag, group_code_flag, card_type_flag, " +
                "debut_sup_flag_0, debut_sup_flag_1, debut_year_flag, debut_month1, consume_type, item_ename_ao, item_ename_bl, " +
                "item_ename_ca, item_ename_id, item_ename_it, item_ename_ot, consume_flag, curr_month, next_month, curr_amt, " +
                "curr_tot_cond, curr_tot_cnt, feedback_type, feedback_rate, feedback_amt, feedback_score, feedback_score_amt, " +
                "sale_cond, consume_cnt, current_cnt, current_cond, current_score, unit_cond, unit_curr_amt, unit_score, " +
                "memo, cal_date_type, cal_date_days, apr_flag, mod_seqno ";

        wp.daoTable = " mkt_intr_fund";
        wp.whereStr = " where 1=1 ";
        if (qFrom == 0) {
            wp.whereStr = wp.whereStr + sqlCol(wp.itemStr("kk_proj_code"), "program_code");
        } else if (qFrom == 1) {
            wp.whereStr = wp.whereStr + sqlRowId(rowid, "rowid");
        }

        pageSelect();
        if (sqlNotFind()) {
            alertErr2("此條件查無資料");
        }
    }

    @Override
    public void saveFunc() throws Exception {
        if (isAdd()) {
            if (wp.itemEmpty("approval_user") && wp.itemEmpty("approval_passwd")) {
                wp.itemSet("apr_flag", "N");
            } else {
                if (checkApproveZz()) {
                    wp.itemSet("apr_flag", "Y");
                } else {
                    return;
                }
            }
        } else if (isUpdate()) {
            if (wp.itemEmpty("approval_user") && wp.itemEmpty("approval_passwd")) {
                wp.itemSet("apr_flag", "N");
            } else {
                if (checkApproveZz()) {
                    wp.itemSet("apr_flag", "Y");
                } else {
                    return;
                }
            }
        } else if (isDelete() && wp.itemEq("apr_flag", "Y")) {
            if (!checkApproveZz()) {
                return;
            }
        }

        mktm02.Mktm3030Func func = new mktm02.Mktm3030Func(wp);
        rc = func.dbSave(strAction);
        sqlCommit(rc);

        if (rc != 1) {
            alertErr(func.getMsg());
        } else {
            if (isUpdate()) {
                qFrom = 1;
                rowid = wp.itemStr("rowid");
                saveAfter(true);
            } else {
                saveAfter(true);
            }
        }
    }

    public void saveAfter(boolean bRetrieve) throws Exception {
        if (rc != 1) {
            return;
        }
        if (isAdd()) {
            if (bRetrieve) {
                dataRead();
            } else {
                clearFunc();
            }
        } else if (isUpdate()) {
            if (bRetrieve) {
                dataRead();
            } else {
                modSeqnoAdd();
            }
        } else {
            clearFunc();
        }
    }

    public void dataRead2() throws Exception {
        String projCode2 = wp.itemStr("data_k1");
        if (empty(projCode2)) {
            projCode2 = wp.itemStr("proj_code");
        }

        String rowId2 = wp.itemStr("data_k2");
        if (empty(rowId2)) {
            rowId2 = wp.itemStr("rowid");
        }

        switch (wp.respHtml) {
            case "mktm3030_detl_01":
                wp.sqlCmd = "select a.program_code, a.data_type, a.data_code1||'_'||c.chin_name as data_code1, " +
                        "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                        "from mkt_intr_dtl a " +
                        "inner join mkt_intr_fund b on a.program_code = b.program_code " +
                        "inner join ptr_acct_type c on a.data_code1 = c.acct_type " +
                        "where 1 = 1 and data_type = '01' " +
                        sqlCol(projCode2, "a.program_code") +
                        sqlCol(rowId2, "hex(b.rowid)") +
                        "union " +
                        "select a.program_code, a.data_type, a.data_code1||'_'||c.chin_name as data_code1, " +
                        "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                        "from mkt_intr_dtl a " +
                        "inner join mkt_intr_fund b on a.program_code = b.program_code " +
                        "inner join dbp_acct_type c on a.data_code1 = c.acct_type " +
                        "where 1 = 1 and data_type = '01' " +
                        sqlCol(projCode2, "a.program_code") +
                        sqlCol(rowId2, "hex(b.rowid)");
                break;
            case "mktm3030_detl_02":
                wp.sqlCmd = "select a.program_code, a.data_type, a.data_code1||'_'||c.group_name as data_code1, " +
                        "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                        "from mkt_intr_dtl a " +
                        "inner join mkt_intr_fund b on a.program_code = b.program_code " +
                        "inner join ptr_group_code c on a.data_code1 = c.group_code " +
                        "where 1 = 1 and data_type = '02' " +
                        sqlCol(projCode2, "a.program_code") +
                        sqlCol(rowId2, "hex(b.rowid)");
                break;
            case "mktm3030_detl_03":
                wp.sqlCmd = "select a.program_code, a.data_type, a.data_code1||'_'||c.name as data_code1, " +
                        "a.data_code1 as ex_data_code1, b.apr_flag, hex(b.rowid) as rowid " +
                        "from mkt_intr_dtl a " +
                        "inner join mkt_intr_fund b on a.program_code = b.program_code " +
                        "inner join ptr_card_type c on a.data_code1 = c.card_type " +
                        "where 1 = 1 and data_type = '03' " +
                        sqlCol(projCode2, "a.program_code") +
                        sqlCol(rowId2, "hex(b.rowid)");
                break;
            default:
                break;
        }

        this.selectNoLimit();
        pageQuery();
        if (sqlRowNum == 0) {
            this.selectOK();
        }

        wp.setListCount(1);
        wp.colSet("row_num", "" + wp.selectCnt);
    }

    /**
      *  參數頁存檔
      */
    public void dataSave2() throws Exception {
        int llOk = 0;
        int llErr = 0;

        String lsType = wp.itemStr("data_type");
        if (empty(lsType)) {
            errmsg("無法取得資料類別[data_type]");
            return;
        }

        mktm02.Mktm3030Func func = new mktm02.Mktm3030Func(wp);

        String[] aaCode = wp.itemBuff("ex_data_code1");
        String[] aaOpt = wp.itemBuff("opt");
        wp.listCount[0] = aaCode.length;
        wp.colSet("row_num", "" + aaCode.length);

        // -check duplication-
        int ii = -1;
        for (String tmpStr : aaCode) {
            ii++;
            wp.colSet(ii, "ok_flag", "");
            // -option-ON-
            if (checkBoxOptOn(ii, aaOpt)) {
                aaCode[ii] = "";
                continue;
            }

            if (ii != Arrays.asList(aaCode).indexOf(tmpStr)) {
                wp.colSet(ii, "ok_flag", "!");
                llErr++;
            }
        }

        if (llErr > 0) {
            alertErr("資料值重複: " + llErr);
            return;
        }

        // -delete no-approve-
        if (func.deleteAllDetl(lsType) < 0) {
            this.dbRollback();
            alertErr(func.getMsg());
            return;
        }

        for (int ll = 0; ll < aaCode.length; ll++) {
            wp.colSet(ll, "ok_flag", "");

            // -option-ON-
            if (checkBoxOptOn(ll, aaOpt)) {
                llOk++;
                continue;
            }

            if (empty(aaCode[ll])) {
                llOk++;
                continue;
            }

            func.varsSet("ex_data_code1", aaCode[ll]);
            if (func.insertDetl(lsType) == 1) {
                llOk++;
            } else {
                llErr++;
            }
        }

        if (llOk > 0) {
            sqlCommit(1);
        }

        alertMsg("資料存檔處理完成; OK=" + llOk + ", ERR=" + llErr);
        dataRead2();
    }

    void procApprove() {
        int ilOk = 0, ilErr = 0;

        mktm02.Mktm3030Func func = new mktm02.Mktm3030Func(wp);
        func.setConn(wp);

        String[] lsProjCode = wp.itemBuff("proj_code");
        String[] lsAprFlag = wp.itemBuff("apr_flag");
        String[] aaOpt = wp.itemBuff("opt");
        this.optNumKeep(lsProjCode.length, aaOpt);
        wp.listCount[0] = wp.itemRows("proj_code");

        int rr = -1;
        rr = optToIndex(aaOpt[0]);

        if (rr < 0) {
            alertErr2("請點選欲覆核資料");
            return;
        }

        if (!checkApproveZz()) {
            return;
        }

        for (int ii = 0; ii < aaOpt.length; ii++) {
            rr = (int) optToIndex(aaOpt[ii]);
            if (rr < 0) {
                continue;
            }
            // 若已覆核，則無法再覆核。
            if(lsAprFlag[rr].equals("Y")) {
                ilErr++;
                wp.colSet(rr, "ok_flag", "X");
                continue;
            }
            wp.colSet(rr, "ok_flag", "-");

            func.varsSet("proj_code", lsProjCode[rr]);

            rc = func.dataApprove();
            sqlCommit(rc);
            if (rc == 1) {
                wp.colSet(rr, "ok_flag", "V");
                ilOk++;
                continue;
            }
            ilErr++;
            wp.colSet(rr, "ok_flag", "X");
        }

        alertMsg("覆核處理: 成功筆數=" + ilOk + "; 失敗筆數=" + ilErr);
    }

    @Override
    public void dddwSelect() {
        try {
            switch (wp.respHtml) {
                case "mktm3030_detl_01":
                    wp.initOption = "";
                    wp.optionKey = "";
                    dddwList("dddw_ptr_acct_type", "ptr_acct_type", "acct_type", "chin_name", "where 1 = 1");
                    wp.initOption = "";
                    wp.optionKey = "";
                    dddwList("dddw_dbp_acct_type", "dbp_acct_type", "acct_type", "chin_name", "where 1 = 1");
                    break;
                case "mktm3030_detl_02":
                    wp.initOption = "";
                    wp.optionKey = "";
                    dddwList("dddw_ptr_group_code", "ptr_group_code", "group_code", "group_name", "where 1 = 1");
                    break;
                case "mktm3030_detl_03":
                    wp.initOption = "";
                    wp.optionKey = "";
                    dddwList("dddw_ptr_card_type", "ptr_card_type", "card_type", "name", "where 1 = 1");
                    break;
                default:
                    break;
            }
        } catch (Exception exception) {
            System.out.println("error [Mktm3030] : " + exception.getMessage());
        }
    }

    @Override
    public void initButton() {
        if (wp.respHtml.indexOf("_detl") > 0) {
            this.btnModeAud();
            if (wp.colEq("apr_flag", "Y")) {
                buttonOff("btnUpdate_disable");
                buttonOff("btnDelete_disable");
            }
        }

        if (empty(wp.colStr("rowid"))) {
            buttonOff("btnparm_disable");
        }

        if (wp.respHtml.indexOf("_detl_0") > 0) {
            if (wp.colEq("apr_flag", "Y")) {
                buttonOff("btnUpdate_disable");
                buttonOff("newDetail_disable");
            }
        }

        int rr;
        rr = wp.listCount[0];
        wp.colSet(0, "IND_NUM", "" + rr);
    }

    @Override
    public void initPage() {
    }
}
