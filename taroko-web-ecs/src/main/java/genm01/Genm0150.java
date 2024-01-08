/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 112-11-29  V1.00.00  Zuwei Su   program initial                            *
 * 112-12-21  V1.00.01  Zuwei Su   增加檢核PTR_BUSINDAY.vouch_close_flag(會計已軋帳(日結))='Y' 者                            *
 * 113-01-05  V1.00.02  Zuwei Su   無mod_log 的判讀                            *
 * 113-01-05  V1.00.03  Grace      1) 已過帳者, 不提供維護, query 以條件排除                              *
 *                                 2) 不檢核營業日是否已軋帳(ptr_businday)           *
 ******************************************************************************/

package genm01;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import busi.SqlPrepare;
import ofcapp.BaseProc;
import taroko.com.TarokoCommon;
import taroko.com.TarokoExcel;
import taroko.com.TarokoPDF;

public class Genm0150 extends BaseProc {
    String mProgName = "genm0150";
    String msg = "", mailBranch = "", mailType = "", isSendPost = "", isBarcode = "", isOldRefno = "",
            isOldDate = "";
    String lsDate = "", lsRefno = "", lsDeptno = "", lsBrn = "", reportSubtitle = "", lsAprUser = "",
            isModFlag = "";
    String lsWhere = "";
    String dbMemo3Flag = "", dbCrFlag = "", dbDrFlag = "", dbMemo3Kind = "", dbBrnRptFlag = "",
            dbBrief1 = "";
    int rr = -1, li_extn = 0;
    int ilOk = 0, chkInv = 0;
    int ilErr = 0, ilErr2 = 0;
    int i = 0;
    double ilMailNo = 0, ilEndMailno = 0;
    String hGsvhModWs = "";
    String hGsvhMemo1 = "";
    String hGsvhMemo2 = "";
    String hGsvhMemo3 = "";
    public List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    public int rptSeq = 0;
    String noTrim = "";
    String hGsvhStdVouchCd = "";
    String hSystemVouchDate = "";
    String hVoucRefno = "";
    String hGsvhCurr = "";
    Integer vouchPageCnt = 0;
    String hVoucIdNo = "";
    String hVoucIfrsFlag = "";
    String hGsvhModUser = "";
    String hGsvhModPgm = "";
    String hTempAcBriefName = "";
    String hMemo3FlagCom = "";
    String hVoucAcNo = "";
    String hGsvhDbcr = "";
    String hAccmCrFlag = "";
    String hAccmDrFlag = "";
    double hVoucAmt = 0;
    String hEngName = "";
    String tempX06 = "";
    String optionKey = "";

    String[] opt = null;
    String[] aaRowid = null;
    String[] aaModSeqno = null;
    String[] aaBrno = null;
    String aaTxDate = "";
    String[] aaDept = null;
    String[] aaDepno = null;
    String[] aaRefno = null;
    String[] aaCurr = null;
    String[] aaSeqno = null;
    String[] aaVoucherCnt = null;
    String[] aaAcNo = null;
    String[] aaDbcr = null;
    String[] aaDbDbcr = null;
    String[] aaSignFlag = null;
    String[] aaAmt = null;
    String[] aaCrtUser = null;
    String[] aaAprUser = null;
    String[] aaMemo1 = null;
    String[] aaMemo2 = null;
    String[] aaMemo3 = null;
    String[] aaJrnStatus = null;
    String[] aaPostFlag = null;
    String[] aaDbOptcode = null;
    String[] aaDbBrief1 = null;
    String[] aaDbCrFlag = null;
    String[] aaDbDrFlag = null;
    String[] aaDbMemo3Flag = null;
    String[] aaDbMemo3Kind = null;
    String[] aaDbInsplist = null;
    String[] aaDbBrnRptFlag = null;
    String[] aaKeyValue = null;

    String[] aaDbAcctNo = null;
    String[] aaDbAcctName = null;
    String[] aaDbIas24Id = null;

    String[] aaSysRem = null;
    String[] aaIdNo = null;
    String[] aaDbNocode = null;
    String[] aaDbOldMemoChg = null;
    String[] aaIfrsFlag = null;
    String[] aaCurrCodeDc = null;
    String[] aaDbBrief = null;

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

        strAction = wp.buttonCode;
        // ddd("action=" + is_action + ", level=" + wp.levelCode + ", resp=" +
        // wp.respCode + ",rHtml=" + wp.respHtml);
        switch (wp.buttonCode) {
            case "X":
                /* 轉換顯示畫面 */
                strAction = "new";
                clearFunc();
                break;
            case "C":
                // -資料處理-
                dataProcess();
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
            // case "A":
            // /* 新增功能 */
            // saveFunc();
            // break;
            // case "U":
            // /* 更新功能 */
            // saveFunc();
            // break;
            // case "D":
            // /* 刪除功能 */
            // saveFunc();
            // break;
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
            case "XLS":
                // -Excel-
                strAction = "XLS";
                // wp.setExcelMode();
                xlsPrint();
                break;
            case "PDF":
                // -PDF-
                strAction = "PDF";
                // wp.setExcelMode();
                pdfPrint();
                break;
            case "S2":
                // 存檔
                strAction = "S2";
                dataProcess();
                break;
            case "Q2":
                // 標準分錄代碼
                strAction = "Q2";
                // queryFunc();
                wfReadStdcode(wp.itemStr("ex_std_cd"), "");
                break;
            case "M1":
                // 幣別
                strAction = "M1";
                wfReport1();
                break;
            case "AJAX":
                strAction = "AJAX";
                //processAjaxOption();
                wfChkCol(wp);
                break;
            case "ItemChanged":
                strAction = "ItemChanged";
                ItemChanged();
                break;
            case "RR":
                /* TEST */
                strAction = "R";
                reSerno();
                break;
            default:
                break;
        }

        dddwSelect();
        initButton();
    }

    @Override
    public void initPage() {
        String sqlSelect = " select online_date "
                + " ,vouch_date "
                + " ,decode(VOUCH_CHK_FLAG,'','N',VOUCH_CHK_FLAG) ls_vouch_chk_flag "
                + " ,decode(VOUCH_CLOSE_FLAG,'','N',VOUCH_CLOSE_FLAG) ls_vouch_close_flag"
                + " from ptr_businday "
                + " fetch first 1 row only ";
        sqlSelect(sqlSelect);
//        if(sqlStr("ls_vouch_chk_flag").equals("Y")){
//            alertMsg("軋帳中!!不能作業!!");
//            wp.colSet("set_btn", "disabled='disabled'");
//        }
//        if(sqlStr("ls_vouch_close_flag").equals("Y")){
            //alertMsg("已軋帳, 起次日帳 !!");
        //}
        wp.colSet("ls_vouch_chk_flag", sqlStr("ls_vouch_chk_flag"));
        wp.colSet("ls_vouch_close_flag", sqlStr("ls_vouch_close_flag"));
        String lsDate = sqlStr("online_date");
        String lsVouchDate = sqlStr("vouch_date");
        String lsVouchCloseFlag = sqlStr("ls_vouch_close_flag");

        sqlSelect = "select count(*) li_cnt from ptr_holiday b , ptr_businday a where b.holiday =:online_date ";
        setString("online_date", lsDate);
        sqlSelect(sqlSelect);
        String liCnt = sqlStr("li_cnt");
        if (lsVouchCloseFlag.equals("Y") || this.toInt(liCnt) > 0) {
            lsDate = lsVouchDate;
        }
        wp.colSet("ex_tx_date", lsDate);
        wp.colSet("ex_tx_date2", lsDate);
        wp.colSet("new_flag", "Y");
    }

    @Override
    public void dddwSelect() {
        try {
            // 標準分錄代碼
//            wp.initOption = "--";
//            wp.optionKey = wp.itemStr("ex_std_cd");
//            dddwList("dddw_ex_std_cd", "gen_std_vouch", "std_vouch_cd", "std_vouch_desc","where 1=1 group by std_vouch_cd,std_vouch_desc order by std_vouch_cd ");

            // 幣別
//            wp.initOption = "--";
//            wp.optionKey = empty(wp.itemStr("ex_curr")) ? "00" : wp.itemStr("ex_curr");
//            if (!empty(optionKey)) {
//                wp.optionKey = optionKey;
//            }
//            dddwList("dddw_curr", "ptr_currcode", "curr_code_gl", "curr_chi_name", "where 1=1 and curr_code_gl in('00','01','08') group by curr_code_gl,curr_chi_name  order by curr_code_gl ");

            // 分行
//            wp.initOption = "--";
//            wp.optionKey = empty(wp.itemStr("ex_brn")) ? "3144" : wp.itemStr("ex_brn");
//            dddwList("dddw_ex_brn", "gen_brn", "branch", "full_chi_name", "where 1=1 group by branch,full_chi_name order by branch");

            // 科目
            // wp.initOption = "--";
            // wp.optionKey = wp.item_ss("ex_gen_acct");
            // dddw_list("dddw_ex_gen_acct", "gen_acct_m", "ac_no",
            // "acNo||ac_full_name", "where 1=1 order by acNo");

        } catch (Exception ex) {
        }
    }

    int getWhereStr() {

        String exRefno = lsRefno;
        if (empty(exRefno)) {
            exRefno = wp.itemStr("ex_refno");
        }
        if (empty(exRefno)) {
            alertErr("套號不可空白");
            return -1;
        }
        String exTxDate = wp.itemStr("ex_tx_date");
        wp.whereStr = " where 1=1 ";
        // 固定條件
        wp.whereStr += "and jrn_status != '1' ";
        wp.whereStr += "and post_flag <>'Y'  ";		//已過帳者, 不提供維護
//        wp.whereStr += "and mod_log not in ('D','U') ";
        // 自選條件
        wp.whereStr += sqlCol(exRefno, "refno");
        wp.whereStr += sqlCol(exTxDate, "tx_date");


        wp.whereStr += sqlCol(wp.itemStr("ex_curr"), "curr");
        /*
         * wp.whereStr += sql_col(ex_brn, "brno"); wp.whereStr += sql_col(ex_dept, "dept"); wp.whereStr
         * += sql_col(ex_deptno, "depno");
         */

        return 1;
    }

    @Override
    public void queryFunc() throws Exception {
        // wp.whereStr = "where 1=1 ";
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        wp.selectSQL = " hex(rowid) AS rowid, "
                + "brno, "
                + "tx_date, "
                + "dept, "
                + "depno, "
                + "refno, "
                + "curr, "
                + "seqno, "
                + "voucher_cnt, "
                + "ac_no, "
                + "dbcr, "
                + "decode(dbcr,'C','貸方','D','借方', '') as db_dbcr,"
				+ "decode(sign_flag,'', 0, sign_flag) as sign_flag, "
//                + "sign_flag, "
                + "amt, "
                + "crt_user, "
                + "'' db_cname, "
                + "apr_user as apr_user2, "
                + "memo1, "
                + "memo2, "
                + "memo3, "
                + "jrn_status, "
                + "post_flag, "
                + "mod_user, "
                + "mod_time, "
                + "mod_pgm, "
                + "mod_seqno, "
                + "rowid, "
                + "'0' db_optcode, "
                + "lpad (' ', 30) as db_brief1, "
                + "' ' as db_cr_flag, "
                + "' ' as db_dr_flag, "
                + "' ' as db_memo3_flag, "
                + "' ' as db_memo3_kind, "
                + "' ' as db_insplist, "
                + "' ' as db_brn_rpt_flag, "
                + "key_value, "
                + "acct_no, "
                + "acct_name, "
                + "ias24_id, "
                + "sys_rem, "
                + "id_no, "
                + "'O' as db_nocode, "
                + "' ' as db_old_memo_chg, "
                + "ifrs_flag ";
        wp.daoTable = "gen_vouch ";

        wp.whereOrder = " order by seqno ";
        if (getWhereStr() != 1)
            return;

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        listWkdata();
        // wp.totalRows = wp.dataCnt;
        // wp.listCount[1] = wp.dataCnt;
        log("ALL rowcnt:" + wp.selectCnt);
        wp.setPageValue();
        wp.colSet("new_flag", "N"); // 計算頁面rowIndex用
    }

    void listWkdata() throws Exception {
        int selCt = wp.selectCnt;
        for (int ii = 0; ii < selCt; ii++) {
            wfChkAcNo(ii, wp.colStr(ii, "ac_no"));
            wp.colSet(ii, "db_cname", sellectUsrcname(wp.colStr(ii, "crt_user")));
        }
    }

    @Override
    public void querySelect() throws Exception {
        dataRead();
    }

    @Override
    public void dataRead() throws Exception {

    }

    @Override
    public void dataProcess() throws Exception {
        String mRowid = "", mModSeqno = "";
        opt = wp.itemBuff("opt");
        aaRowid = wp.itemBuff("rowid");
        aaModSeqno = wp.itemBuff("mod_seqno");
        aaDbAcctNo = wp.itemBuff("acct_no");
        aaDbAcctName = wp.itemBuff("acct_name");
        aaDbIas24Id = wp.itemBuff("ias24_id");

        aaBrno = wp.itemBuff("brno");
        aaTxDate = wp.itemStr("ex_tx_date");
        aaDept = wp.itemBuff("dept");
        aaDepno = wp.itemBuff("depno");
        aaRefno = wp.itemBuff("refno");
        aaCurr = wp.itemBuff("curr");
        aaSeqno = wp.itemBuff("seqno");
        aaVoucherCnt = wp.itemBuff("voucher_cnt");
        aaAcNo = wp.itemBuff("ac_no");
        aaDbcr = wp.itemBuff("dbcr");
        aaDbDbcr = wp.itemBuff("db_dbcr");
        aaSignFlag = wp.itemBuff("sign_flag");
        aaAmt = wp.itemBuff("amt");
        aaCrtUser = wp.itemBuff("crt_user");
        aaAprUser = wp.itemBuff("apr_user2");
        aaMemo1 = wp.itemBuff("memo1");
        aaMemo2 = wp.itemBuff("memo2");
        aaMemo3 = wp.itemBuff("memo3");
        aaJrnStatus = wp.itemBuff("jrn_status");
        aaPostFlag = wp.itemBuff("post_flag");
        aaDbOptcode = wp.itemBuff("db_optcode");
        aaDbBrief = wp.itemBuff("db_brief");
        aaDbCrFlag = wp.itemBuff("db_cr_flag");
        aaDbDrFlag = wp.itemBuff("db_dr_flag");
        aaDbMemo3Flag = wp.itemBuff("db_memo3_flag");
        aaDbMemo3Kind = wp.itemBuff("db_memo3_kind");
        aaDbInsplist = wp.itemBuff("db_insplist");
        aaDbBrnRptFlag = wp.itemBuff("db_brn_rpt_flag");
        aaKeyValue = wp.itemBuff("key_value");
        aaSysRem = wp.itemBuff("sys_rem");
        aaIdNo = wp.itemBuff("id_no");
        aaDbNocode = wp.itemBuff("db_nocode");
        aaDbOldMemoChg = wp.itemBuff("db_old_memo_chg");
        aaIfrsFlag = wp.itemBuff("ifrs_flag");
        aaCurrCodeDc = wp.itemBuff("curr_code_dc");
        wp.listCount[0] = aaAcNo.length;
        // check ptr_businday (20240105 不檢核, grace) ---------------------------------
        /*
        String sqlSelect = " select online_date "
                + " ,vouch_date "
                + " ,decode(VOUCH_CHK_FLAG,'','N',VOUCH_CHK_FLAG) ls_vouch_chk_flag "
                + " ,decode(VOUCH_CLOSE_FLAG,'','N',VOUCH_CLOSE_FLAG) ls_vouch_close_flag"
                + " from ptr_businday "
                + " fetch first 1 row only ";
        sqlSelect(sqlSelect);
        String lsDate = sqlStr("online_date");
        String lsVouchDate = sqlStr("vouch_date");
        String lsVouchChkFlag = sqlStr("ls_vouch_chk_flag");
        String lsVouchCloseFlag = sqlStr("ls_vouch_close_flag");

        if (lsVouchChkFlag.equals("Y")) {
            alertErr("該營業日已過帳, 不得處理異常維護 !!");
            ofcCleardw();
            return;
        }
        */
        // check ptr_holiday
//        sqlSelect = "select count(*) li_cnt "
//                + "from ptr_holiday b , ptr_businday a "
//                + "where b.holiday = a.online_date ";
//        sqlSelect(sqlSelect);
        // if(sql_num("li_cnt") > 0){
        // alert_err("'Error','今天為假日 不能作業!!");
        // return -1;
        // }
//        String liCnt = sqlStr("li_cnt");
//		if (ls_vouch_close_flag.equals("Y") || this.to_Int(li_cnt) > 0) {
//			ls_date = ls_vouch_date;
//		}
//		wp.colSet("ex_tx_date", ls_date);
        // check curr
//        if (empty(wp.itemStr("ex_curr"))) {
//            alertErr("存檔失敗,幣別不可為空白 !!");
//            return;
//        }
        // for (int ii = 0; ii < aaAcNo.length; ii++) {
        // m_rowid = aaRowid[ii];
        // m_mod_seqno = aaModSeqno[ii];
        // if (checkBox_opt_on(ii, opt)) continue;
        // }
        // check
        if (ofValidation() != 1) {
            return;
        }
        if (ofcUpdatebefore() != 1) {
            return;
        }
        if (ofcUpdate() < 0) {
            sqlCommit(0);
            return;
        }
        sqlCommit(1);
        queryFunc();
        //Mantis7020 依存檔後重新讀取資料之筆數決定顯示訊息  >0 :資料處理成功  =0: 資料錯誤
        if(wp.selectCnt > 0){
            alertMsg("");
        }

    }

    void ofcCleardw() {
        isModFlag = "";
        wp.colSet("ex_curr", "00");
    }

    public int ofValidation() {
        double llD = 0, llC = 0;
        String lsMemo3Brn = "", lsSql = "";
        int llErr = 0;
        int lsAmt = 0;
        double lsAmt1 = 0;
        wp.listCount[0] = aaAcNo.length;
        for (int ii = 0; ii < aaAcNo.length; ii++) {
            if(aaJrnStatus[ii].equals("3")){
                alertMsg("已放行資料需解放行才可異動!");
                wp.colSet(ii, "err_msg", "已放行資料需解放行才可異動!");
                wp.colSet(ii, "ok_flag", "!");
                return -1;
            }
            if (checkBoxOptOn(ii, opt)) {
                continue;
            }
            if(!aaCurr[ii].equals("01")){
                lsAmt1 = Double.parseDouble(aaAmt[ii]);
                if(!isIntegerForDouble(lsAmt1)){
                    alertMsg("台幣、日幣之金額不可有小數點值");
                    wp.colSet(ii, "err_msg", "台幣、日幣之金額不可有小數點值");
                    wp.colSet(ii, "ok_flag", "!");
                    return -1;
                }
//				if(ls_amt1 > 0){
//					alert_msg("台幣、日幣之金額不可有小數點值");
//					wp.colSet(ii, "err_msg", "台幣、日幣之金額不可有小數點值");
//					wp.colSet(ii, "ok_flag", "!");
//					return -1;
//				}
            }
            if (this.toNum(aaAmt[ii]) == 0) {
                // wp.colSet(ii, "err_msg", "金額不可為0"); //Mantis4077
                // user說不用顯示錯誤檢核,直接跳過此筆資料
                // wp.colSet(ii, "ok_flag", "!");
                continue;
            }


            if (wfChkAcNo(ii, aaAcNo[ii]) != 1) {
                alertMsg("存檔失敗,科目代號不存在!");
                wp.colSet(ii, "err_msg", "科目代號不存在!");
                wp.colSet(ii, "ok_flag", "!");
                return -1;
            } else {
                wfChkAcNo(ii, aaAcNo[ii]);
                // aa_db_brief1[ii] = db_brief1;
                aaDbMemo3Flag[ii] = dbMemo3Flag;
                aaDbCrFlag[ii] = dbCrFlag;
                aaDbDrFlag[ii] = dbDrFlag;
                aaDbMemo3Kind[ii] = dbMemo3Kind;
                aaDbBrnRptFlag[ii] = dbBrnRptFlag;
                if (aaDbBrnRptFlag[ii].equals("Y")) {
                    aaDbInsplist[ii] = "1";
                } else {
                    aaDbInsplist[ii] = "0";
                }
            }
            if (aaJrnStatus[ii].equals("1")) {
                alertMsg("存檔失敗,此套號已被修改,不可修改");
                wp.colSet(ii, "ok_flag", "!");
                return -1;
            }
            if (aaJrnStatus[ii].equals("3")) {
                alertMsg("存檔失敗,此套號已放行,不可修改");
                wp.colSet(ii, "err_msg", "此套號已放行,不可修改");
                wp.colSet(ii, "ok_flag", "!");
                return -1;
            }
            if (aaDbBrnRptFlag[ii].equals("Y")) {
                if (empty(aaMemo3[ii])) {
                    alertMsg("存檔失敗,聯行科目-" + aaAcNo[ii] + "摘要三不得為空白!!");
                    wp.colSet(ii, "err_msg", "聯行科目-" + aaAcNo[ii] + "摘要三不得為空白!!");
                    wp.colSet(ii, "ok_flag", "!");
                    return -1;
                }
                lsMemo3Brn = strMid(aaMemo3[ii], 3, 3);

                lsSql = "select count(*) ct from gen_brn "
                        + "where 1=1 ";
                lsSql += sqlCol(lsMemo3Brn, "branch");
                sqlSelect(lsSql);
                if (sqlNum("ct") < 1) {
                    alertMsg("存檔失敗,分行代號-" + lsMemo3Brn + "不存在 !!");
                    wp.colSet(ii, "err_msg", "分行代號-" + lsMemo3Brn + "不存在 !!");
                    wp.colSet(ii, "ok_flag", "!");
                    return -1;
                }

            }

            // 處理 聯行報單
            lsMemo3Brn = strMid(aaMemo3[ii], 0, 3);
            lsSql = "select count(*) ct from gen_brn "
                    + "where 1=1 "
                    + " and branch = :ls_memo3_brn";
            setString("ls_memo3_brn", lsMemo3Brn);
            sqlSelect(lsSql);
            if ((sqlNum("ct") < 1 || aaMemo3[ii].length() > 8) && !isModFlag.equals("Y")) {
                wp.colSet(ii, "db_insplist", "0");
            }

            if (aaDbcr[ii].equals("D")) {
                if (!checkBoxOptOn(ii, opt)) {
                    llD = BigDecimalAdd(llD,toNum(aaAmt[ii]));
                }
            }
            if (aaDbcr[ii].equals("C")) {
                if (!checkBoxOptOn(ii, opt)) {
                    llC = BigDecimalAdd(llC,toNum(aaAmt[ii]));
                }
            }

        }
        log("ll_d all :" + llD);
        log("ll_c all:" + llC);
        double llE = llD - llC;
        if (llD != llC) {
            alertErr("存檔失敗,借方金額 不等於 貸方金額  差額 = " + llE);
            return -1;
        }
        return 1;
    }

    public int ofcUpdatebefore() throws Exception {
        long liDate = 0;
        int liCntSeq = 0;
        int ll = 0, l1 = 0;
        lsDate = wp.itemStr("ex_tx_date");
        String lsSql = "";
        try {
            liDate = Long.parseLong(lsDate) - 19110000;
        } catch (Exception ex) {
            liDate = 0;
        }
        // 待確認 Andy 20171222
        lsRefno = wp.itemStr("ex_refno");
        isOldRefno = wp.itemStr("ex_refno");
        isOldDate = wp.itemStr("ex_tx_date");

        // ptr_classcode ==>ptr_dept_code 20190513 Andy
//        lsSql = "select nvl(substr(b.gl_code,1,1),'1') gl_code "
//                + "from sec_user a join ptr_dept_code b on a.usr_deptno = b.dept_code "
//                + "where a.usr_id =:ls_user ";
//        setString("ls_user", wp.loginUser);
//        sqlSelect(lsSql);
//        lsDeptno = sqlStr("gl_code");
//        if (empty(lsDeptno)) {
//            lsDeptno = "0";
//        }
//        // ls_refno
//        // 套號計算
//        lsSql = "select substr(to_char(to_number(nvl(max(substr(refno,4,3)),'000'))+ 1,'000'),2,3) as refno "
//                + "from gen_vouch "
//                + "where tx_date = :ls_date "
//                + "and substr(refno,3,1) = :ls_deptno";
//        setString("ls_date", lsDate);
//        setString("ls_deptno", lsDeptno);
//        sqlSelect(lsSql);
//        lsRefno = "UB" + lsDeptno + sqlStr("refno");
//        // delete gen_memo3
//        String dsSql = "delete gen_memo3 where refno = :ls_refno and tx_date = :ls_date ";
//        setString("ls_refno", lsRefno);
//        setString("ls_date", lsDate);
//        sqlExec(dsSql);
        wp.listCount[0] = aaAcNo.length;
        for (int ii = 0; ii < aaAcNo.length; ii++) {
            if (checkBoxOptOn(ii, opt) || this.toNum(aaAmt[ii]) == 0) {
                ll = ll + 1;
                continue;
            }
            String strLiDate = Long.toString(liDate);
            lsBrn = strMid(aaMemo3[ii], 3, 3);

            if (aaDbBrnRptFlag[ii].equals("Y")) {
                if (aaMemo3[ii].length() != 6 && aaMemo3[ii].length() < 20) {
                    alertMsg("存檔失敗,聯行科目 銷帳鍵值長度錯誤(小於 20) !!");
                    wp.colSet(ii, "err_msg", "聯行科目 銷帳鍵值長度錯誤(小於 20) !!");
                    wp.colSet(ii, "ok_flag", "!");
                    return -1;
                }
                if (!strMid(aaMemo3[ii], 0, 3).equals("109") && aaMemo3[ii].length() < 20) {
                    alertMsg("存檔失敗,聯行科目 銷帳鍵值錯誤~ -- 起帳發報行或銷帳鍵值長度小於 20 !");
                    wp.colSet(ii, "err_msg", "聯行科目 銷帳鍵值錯誤~ -- 起帳發報行或銷帳鍵值長度小於 20 !");
                    wp.colSet(ii, "ok_flag", "!");
                    return -1;
                }
                // R950802-035配合中銀與交銀整併事宜
                if (strMid(aaAcNo[ii], 0, 6).equals("175101") && !aaAcNo[ii].equals("17510100")) {
                    alertMsg("存檔失敗,聯行科目請用17510100");
                    wp.colSet(ii, "err_msg", "聯行科目請用17510100");
                    wp.colSet(ii, "ok_flag", "!");
                    return -1;
                }
                if (strMid(aaMemo3[ii], 0, 3).equals("109") && aaMemo3[ii].length() < 20) {
                    l1 = ii - ll;
                    aaKeyValue[ii] = "109" + lsBrn + String.format("%06d", this.toInt(strMid(strLiDate, strLiDate.length() - 6, strLiDate.length()))) + lsRefno + String.format("%02d", l1 + 1);
                    aaMemo3[ii] = aaKeyValue[ii];
                    wp.colSet(ii, "key_value", aaKeyValue[ii]);// Mantis:0003800
                    // 儲存顯示銷帳鍵值
                    wp.colSet(ii, "memo3", aaMemo3[ii]);

                }
                if (isOldRefno.length() > 0 && !aaDbOldMemoChg[ii].equals("Y")) {
                    if (strMid(aaMemo3[ii], 0, 3).equals("109") && aaMemo3[ii].length() == 3) {
                        l1 = ii - ll;
                        aaKeyValue[ii] = "109" + lsBrn + String.format("%06d", this.toInt(strMid(strLiDate, strLiDate.length() - 6, strLiDate.length()))) + lsRefno + String.format("%02d", l1 + 1);
                        aaMemo3[ii] = aaKeyValue[ii];
                        wp.colSet(ii, "key_value", aaKeyValue[ii]);// Mantis:0003800
                        // 儲存顯示銷帳鍵值
                        wp.colSet(ii, "memo3", aaMemo3[ii]);
                    }
                }
            }
            // 全部
            if (empty(aaMemo3[ii]) && !aaAcNo[ii].equals("600000300")) {
                if (aaDbMemo3Kind[ii].equals("3")) {
                    if ((aaDbDrFlag[ii].equals("Y") && aaDbcr[ii].equals("C")) ||
                            (aaDbCrFlag[ii].equals("Y") && aaDbcr[ii].equals("D"))) {
                        l1 = ii - ll;
                        aaMemo3[ii] = String.format("%06d", this.toInt(strMid(strLiDate, strLiDate.length() - 6, strLiDate.length()))) + lsRefno + String.format("%02d", l1 + 1);
                    }
                }
            }
            /*
             * //需求修改_20180430 if memo3_flag = Y and memo3_kind = 3 then
             * memo3一定要有值 if(aa_db_memo3_flag[ii].equals("Y") &&
             * !aa_ac_no.equals("60000300") && ((aa_db_dr_flag[ii].equals("Y")
             * && aa_dbcr[ii].equals("C")) || (aa_db_cr_flag[ii].equals("Y") &&
             * aa_dbcr[ii].equals("D"))) && aa_db_memo3_kind[ii].length() > 0 &&
             * empty(aa_memo3[ii])){ alert_msg("存檔失敗,MEMO3 銷帳鍵值未輸入 !!");
             * wp.colSet(ii,"ok_flag", "!"); return -1; }
             */
            if (aaDbMemo3Flag[ii].equals("Y") && !aaAcNo.equals("60000300") &&
                    aaDbMemo3Kind[ii].equals("3") && empty(aaMemo3[ii])) {
                alertMsg("存檔失敗,MEMO3 銷帳鍵值未輸入 !!");
                wp.colSet(ii, "err_msg", "MEMO3 銷帳鍵值未輸入 !!");
                wp.colSet(ii, "ok_flag", "!");
                return -1;
            }

            // 聯行科目
            if (!aaDbBrnRptFlag[ii].equals("Y") && !empty(aaMemo3[ii])) {
                aaKeyValue[ii] = aaMemo3[ii];
                wp.colSet(ii, "key_value", aaKeyValue[ii]);// Mantis:0003800
                // 儲存顯示銷帳鍵值
            }
            //20210616 update
            if (!aaDbBrnRptFlag[ii].equals("Y") && empty(aaMemo3[ii])) {
                aaKeyValue[ii] = aaMemo3[ii];
                wp.colSet(ii, "key_value", aaKeyValue[ii]);// Mantis:0003800  儲存顯示銷帳鍵值
            }
            liCntSeq++;
//            aaRefno[ii] = lsRefno;
//            wp.colSet("ex_refno", lsRefno);
//			aa_tx_date[ii] = wp.item_ss("ex_tx_date");
//            aaCurr[ii] = wp.itemStr("ex_curr");
//            aaBrno[ii] = wp.itemStr("ex_brn");
//            aaDept[ii] = wp.itemStr("ex_dept");
//            aaDepno[ii] = wp.itemStr("ex_deptno");
//            aaVoucherCnt[ii] = aaBrno.length + "";
            aaSeqno[ii] = liCntSeq + "";
            aaSignFlag[ii] = aaDbInsplist[ii];
            log("aa_ac_no : "+aaAcNo[ii]);
            log("aa_db_memo3_flag :"+aaDbMemo3Flag[ii]);
            log("aa_db_dr_flag :"+aaDbDrFlag[ii]);
            log("aa_db_cr_flag :"+aaDbCrFlag[ii]);
            log("dbcr :"+wp.itemStr(ii,"dbcr"));
            log("aa_dbcr :"+aaDbcr[ii]);

//            if (!aaAcNo[ii].equals("60000300")) {
//                if (aaDbMemo3Flag[ii].equals("Y")) {
//                    if (aaDbDrFlag[ii].equals("Y") && aaDbcr[ii].equals("C")) {
//                        if (wfInsertMemo3(ii, aaAcNo[ii]) != 1) {
//                            return -1;
//                        }
//                    }
//                    if (aaDbCrFlag[ii].equals("Y") && aaDbcr[ii].equals("D")) {
//                        if (wfInsertMemo3(ii, aaAcNo[ii]) != 1) {
//                            return -1;
//                        }
//                    }
//                }
//            }
        }

        return 1;
    }

//    public int wfInsertMemo3(int alRow, String asAcNo) throws Exception {
//        wp.logSql = false;
//        String isSql = "insert into	gen_memo3( "
//                + "brno,		tx_date,		dept,		depno,		curr, "
//                + "refno,		ac_no,			dbcr,		amt,		crt_user, "
//                + "memo3,		memo1,			memo2,		mod_pgm,	mod_time,"
//                + "mod_user,	mod_seqno "
//                + ") values ("
//                + ":ls_brno,	:ls_tx_date,	:ls_dept,	:ls_depno,	:ls_curr, "
//                + ":ls_refno,	:ls_acno,		:ls_dbcr,	:li_amt,	:ls_user, "
//                + ":ls_memo3,	:ls_memo1,		:ls_memo2,	:ls_mod_pgm,	sysdate, "
//                + ":ls_user,	1 )";
//        setString("ls_brno", aaBrno[alRow]);
//        setString("ls_tx_date", aaTxDate);
//        setString("ls_dept", aaDept[alRow]);
//        setString("ls_depno", aaDepno[alRow]);
//        setString("ls_curr", aaCurr[alRow]);
//        setString("ls_refno", aaRefno[alRow]);
//        setString("ls_acno", aaAcNo[alRow]);
//        setString("ls_dbcr", aaDbcr[alRow]);
//        setString("li_amt", aaAmt[alRow]);
//        setString("ls_user", wp.loginUser);
//        setString("ls_memo3", aaMemo3[alRow]);
//        setString("ls_memo1", aaMemo1[alRow]);
//        setString("ls_memo2", aaMemo2[alRow]);
//        setString("ls_mod_pgm", wp.modPgm());
//
//
//        setString("ls_user", wp.loginUser);
//        sqlExec(isSql);
//        if (sqlRowNum <= 0) {
//            alertErr("存檔失敗,insert gen_memo3 資料處理失敗!!");
//            wp.colSet(alRow, "err_msg", "insert gen_memo3 資料處理失敗!!");
//            wp.colSet(alRow, "ok_flag", "!");
//            return -1;
//        }
//        return 1;
//    }

    public int ofcUpdate() throws Exception {
        String lsJrnStatus = "";
        for (int ii = 0; ii < aaBrno.length; ii++) {
            busi.SqlPrepare sp = new SqlPrepare();
            lsJrnStatus = aaJrnStatus[ii];
            // s980717-035資訊處修改廢除會計科目1751,4005,5012之細目銷帳規則
            if (strMid(aaAcNo[ii], 0, 4).equals("4005") || strMid(aaAcNo[ii], 0, 4).equals("5012")) {
                wp.colSet(ii, "ok_flag", "!");
                alertMsg("存檔失敗,不可使用4005*或5012*會計科目");
                wp.colSet(ii, "err_msg", "不可使用4005*或5012*會計科目");
                wp.colSet(ii, "ok_flag", "!");
                return -1;
            }
            //
            // if ((checkBox_opt_on(ii, opt) || this.to_Num(aa_amt[ii]) == 0) &&
            // empty(aa_rowid[ii])) {
            // continue;
            // }

            // --刪除指定資料--
            // if ((checkBox_opt_on(ii, opt) || this.to_Num(aa_amt[ii]) == 0) &&
            // !empty(aa_rowid[ii])) {
            if ((checkBoxOptOn(ii, opt))) {
                if (!empty(aaRowid[ii])) { // 非新增資料標註"D"
                    sp.sql2Update("gen_vouch");
                    aaJrnStatus[ii] = "1";
                    sp.ppstr("mod_log", "D");
                    if (lsJrnStatus.equals("2")) {
                        aaJrnStatus[ii] = "3";
                    }
                  sp.ppstr("jrn_status", aaJrnStatus[ii]);
                  sp.ppstr("mod_user", wp.loginUser);
                  sp.ppstr("mod_pgm", wp.modPgm());
                  sp.addsql(", mod_time =sysdate");
                  sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
                  sp.sql2Where("where hex(rowid)=?", aaRowid[ii]);
                  sqlExec(sp.sqlStmt(), sp.sqlParm());
  
                  if (sqlRowNum <= 0) {
                      alertMsg(" update gen_vouch err ");
                      wp.colSet(ii, "err_msg", "update gen_vouch err");
                      wp.colSet(ii, "ok_flag", "!");
                      return -1;
                  }
                }
//                if (empty(aaRowid[ii])) {
//                    continue; // 新增資料勾刪除
//                }
                continue;
            }
            // --update資料--
//            if ((!checkBoxOptOn(ii, opt) && this.toNum(aaAmt[ii]) != 0) && !empty(aaRowid[ii])) {
//                sp.sql2Update("gen_vouch");
//                sp.ppstr("sys_rem", wp.itemStr("ex_refno"));
//                aaJrnStatus[ii] = "1";
//                sp.ppstr("mod_log", "U");
//                if (lsJrnStatus.equals("2")) {
//                    aaJrnStatus[ii] = "3";
//                }
//            }
            //Mantis8616 已存檔資料異動
//            if (!empty(aaRowid[ii])) {
//                sp.ppstr("jrn_status", aaJrnStatus[ii]);
//                sp.ppstr("mod_user", wp.loginUser);
//                sp.ppstr("mod_pgm", wp.modPgm());
//                sp.addsql(", mod_time =sysdate");
//                sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
//                sp.sql2Where("where hex(rowid)=?", aaRowid[ii]);
//                sqlExec(sp.sqlStmt(), sp.sqlParm());
//
//                if (sqlRowNum <= 0) {
//                    alertMsg(" update gen_vouch err ");
//                    wp.colSet(ii, "err_msg", "update gen_vouch err");
//                    wp.colSet(ii, "ok_flag", "!");
//                    return -1;
//                }
//            }
            // update
			if (!empty(aaRowid[ii])) {
			    sp.sql2Update("gen_vouch");
				sp.ppstr("ac_no", aaAcNo[ii]);
				sp.ppstr("dbcr", aaDbcr[ii]);
				sp.ppstr("sign_flag", aaSignFlag[ii]);
				sp.ppnum("amt", this.toNum(aaAmt[ii]));
				sp.ppstr("id_no", aaIdNo[ii]);
				sp.ppstr("memo1", aaMemo1[ii]);
				sp.ppstr("memo2", aaMemo2[ii]);
				sp.ppstr("memo3", aaMemo3[ii]);
				sp.ppstr("key_value", aaKeyValue[ii]);
				sp.ppstr("post_flag", aaPostFlag[ii]);
				sp.ppstr("ifrs_flag", aaIfrsFlag[ii]);
				sp.ppstr("curr_code_dc", aaCurrCodeDc[ii]);
				sp.ppstr("curr", aaCurr[ii]);
				sp.ppstr("brno", aaBrno[ii]);
				sp.ppstr("dept", aaDept[ii]);
				sp.ppstr("depno", aaDepno[ii]);
				sp.ppnum("voucher_cnt", this.toInt(aaVoucherCnt[ii]));
				sp.ppstr("jrn_status", aaJrnStatus[ii]);
				sp.ppstr("apr_user", wp.itemStr("zz_apr_user"));
				sp.ppstr("mod_user", wp.loginUser);
				sp.ppstr("mod_pgm", wp.modPgm());
				sp.addsql(", mod_time =sysdate");
				sp.addsql(", mod_seqno =nvl(mod_seqno,0)+1");
				sp.sql2Where("where hex(rowid)=?", aaRowid[ii]);
				sqlExec(sp.sqlStmt(), sp.sqlParm());

				if (sqlRowNum <= 0) {
					alertMsg(" update gen_vouch err ");
					wp.colSet(ii, "err_msg", "update gen_vouch err");
					wp.colSet(ii, "ok_flag", "!");
					return -1;
				}
			} else {
            // --insert資料--
//            if ((checkBoxOptOn(ii, opt) || this.toNum(aaAmt[ii]) == 0)) {
//                continue;
//            }
                sp.sql2Insert("gen_vouch");
                sp.ppstr("mod_log", "0");
                sp.ppstr("sign_flag", aaDbInsplist[ii]);
                sp.ppstr("ac_no", aaAcNo[ii]);
                sp.ppstr("dbcr", aaDbcr[ii]);
                sp.ppnum("amt", toNum(aaAmt[ii]));
                sp.ppstr("id_no", aaIdNo[ii]);
                sp.ppstr("memo1", aaMemo1[ii]);
                sp.ppstr("memo2", aaMemo2[ii]);
                sp.ppstr("memo3", aaMemo3[ii]);
                sp.ppstr("key_value", aaKeyValue[ii]);
                sp.ppstr("post_flag", aaPostFlag[ii]);
                sp.ppstr("ifrs_flag", aaIfrsFlag[ii]);
                sp.ppstr("curr_code_dc", aaCurrCodeDc[ii]);
                sp.ppstr("refno", wp.itemStr("ex_refno"));
                sp.ppstr("sys_rem", wp.itemStr("ex_refno"));
                sp.ppstr("tx_date", aaTxDate);
                sp.ppstr("curr", "00"); // aaCurr[ii]);
                sp.ppstr("brno", "3144"); //aaBrno[ii]);
                sp.ppstr("dept", "UB"); //aaDept[ii]);
                sp.ppstr("depno", "1"); //aaDepno[ii]);
                sp.ppnum("voucher_cnt", this.toInt(aaVoucherCnt[ii]));
                sp.ppnum("seqno", this.toInt(aaSeqno[ii]));
                sp.ppstr("jrn_status", "2");
                sp.ppstr("apr_user", "");
                sp.ppstr("acct_no", aaDbAcctNo[ii]);
                sp.ppstr("acct_name", aaDbAcctName[ii]);
                sp.ppstr("ias24_id", aaDbIas24Id[ii]);
                sp.ppstr("crt_user", wp.loginUser);
                sp.ppstr("mod_user", wp.loginUser);
                sp.ppstr("mod_pgm", wp.modPgm());
                sp.ppstr("mod_seqno", "1");
                sp.addsql(", mod_time ", ", sysdate ");
                sqlExec(sp.sqlStmt(), sp.sqlParm());
                if (sqlRowNum <= 0) {
                    alertMsg(" insert gen_vouch err ");
                    wp.colSet(ii, "err_msg", " insert gen_vouch err ");
                    wp.colSet(ii, "ok_flag", "!");
                    return -1;
                }
			}
            // memo3 report
            try {
                hGsvhDbcr = aaDbcr[ii];
                hAccmCrFlag = aaDbCrFlag[ii];
                hAccmDrFlag = aaDbDrFlag[ii];
                hVoucAcNo = aaAcNo[ii];
                hVoucRefno = aaRefno[ii];
                hVoucAmt = toNum(aaAmt[ii]);
                hGsvhCurr = aaCurr[ii];
                hGsvhMemo1 = aaMemo1[ii];
                hGsvhMemo2 = aaMemo2[ii];
                hGsvhMemo3 = aaMemo3[ii];
                hGsvhModWs = "Genm0150";
                int memo3RetpetErr = detailVouch(ii + 1, aaDbcr[ii], aaAcNo[ii], aaAmt[ii]);
                if (memo3RetpetErr < 0) {
                    alertMsg(" memo3 report err ");
                    wp.colSet(ii, "err_msg", " memo3 report err ");
                    wp.colSet(ii, "ok_flag", "!");
                    return -1;
                }

            } catch (Exception e) {
                alertMsg(" memo3 c report err ");
                wp.colSet(ii, "err_msg", " memo3 report err ");
                wp.colSet(ii, "ok_flag", "!");
                return -1;
            }

        }
        return 1;
    }

    int wfChkAcNo(int alRow, String asAcNo) {
        // wp.dddSql_log = false;
        String lsSql = "select ac_no, "
                // + "ac_brief_name, " //多數為空白改用ac_full_name
                + "ac_full_name, "
                + "memo3_flag, "
                + "cr_flag, "
                + "dr_flag, "
                + "memo3_kind, "
                + "brn_rpt_flag "
                + "from gen_acct_m "
                + "where 1=1 and ac_no = :as_ac_no ";
        setString("as_ac_no", asAcNo);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
            // wp.colSet(al_row, "db_brief1", sql_ss("ac_brief_name"));
            wp.colSet(alRow, "db_brief1", sqlStr("ac_full_name")); // ac_brief_name多為空值,暫以full_name取代
            wp.colSet(alRow, "db_memo3_flag", sqlStr("memo3_flag"));
            wp.colSet(alRow, "db_cr_flag", sqlStr("cr_flag"));
            wp.colSet(alRow, "db_dr_flag", sqlStr("dr_flag"));
            wp.colSet(alRow, "db_memo3_kind", sqlStr("memo3_kind"));
            wp.colSet(alRow, "db_brn_rpt_flag", sqlStr("brn_rpt_flag"));
            dbBrief1 = sqlStr("ac_full_name");
            dbMemo3Flag = sqlStr("memo3_flag");
            dbCrFlag = sqlStr("cr_flag");
            dbDrFlag = sqlStr("dr_flag");
            dbMemo3Kind = sqlStr("memo3_kind");
            dbBrnRptFlag = sqlStr("brn_rpt_flag");

        } else {
            return -1;
        }
        if (sqlStr("brn_rpt_flag").equals("Y")) {
            wp.colSet(alRow, "db_insplist", "1");
        } else {
            wp.colSet(alRow, "db_insplist", "0");
        }

        return 1;
    }

    int wfReadStdcode(String asStdcode, String asCurr) throws Exception {
        if (empty(asStdcode)) {
            return 0;
        }

        wp.pageControl();
        wp.selectSQL = " std_vouch_cd, "
                + "std_vouch_desc, "
                + "curr, "
                + "dbcr, "
                + "dbcr_seq, "
                + "ac_no, "
                + "memo1, "
                + "memo2, "
                + "memo3, "
                + "memo3_kind,"
                + "'0' as amt, "
                + "'' as rowid ";
        wp.daoTable = "gen_std_vouch ";
        // wp.whereOrder = " order by decode(dbcr,'D','A',dbcr) ";
        // wp.whereOrder = "";
        wp.whereStr = "where 1=1 "
                + " and std_vouch_cd = :as_stdcode ";
        wp.whereOrder = " order by dbcr_seq ";
        setString("as_stdcode", asStdcode.trim());
        pageQuery();
        if (ofcRetrieve() == 0) {
            if (!empty(asCurr)) {
                alertMsg("no found");
                return -1;
            } else {
                return 1;
            }
        }
        wp.setListCount(1);
        // wp.totalRows = wp.dataCnt;
        // wp.listCount[1] = wp.dataCnt;
        wp.setPageValue();
        // --move data to DW_data--
        int sel_ct = wp.selectCnt;
        for (int i = 0; i < sel_ct; i++) {
            wfChkAcNo(i, wp.colStr(i, "ac_no"));
        }
        optionKey = wp.colStr(0, "curr");
        wp.colSet("new_flag", "N");
        // 套號計算
        // ptr_classcode ==>ptr_dept_code 20190513 Andy //20200905 user要求停用
        // String ls_sql = "select nvl(substr(b.gl_code,1,1),'1') gl_code "
        // + "from sec_user a join ptr_dept_code b on a.usr_deptno = b.dept_code
        // "
        // + "where a.usr_id =:ls_user ";
        // setString("ls_user", wp.loginUser);
        // sqlSelect(ls_sql);
        // ls_deptno = sql_ss("gl_code");
        // if (empty(ls_deptno)) {
        // ls_deptno = "0";
        // }
        // ls_date = wp.item_ss("ex_tx_date");
        // ls_sql = "select
        // substr(to_char(to_number(nvl(max(substr(refno,4,3)),'000'))+
        // 1,'000'),2,3) as refno "
        // + "from gen_vouch "
        // + "where tx_date = :ls_date "
        // + "and substr(refno,3,1) = :ls_deptno";
        // setString("ls_date", ls_date);
        // setString("ls_deptno", ls_deptno);
        // sqlSelect(ls_sql);
        // ls_refno = "UB" + ls_deptno + sql_ss("refno");
        // wp.colSet("ex_refno", ls_refno);
        return 1;
    }

    int ofcRetrieve() {
        if (sqlRowNum < 0) {
            return 0;
        }
        isModFlag = "Y";
        return 1;
    }

    @Override
    public void initButton() {
        // if (wp.respHtml.indexOf("_detl") > 0) {
        // this.btnMode_aud();
        // }
        // this.btnMode_aud("XX");
        btnUpdateOn(wp.autUpdate());
    }

    void subTitle() {

    }

    void xlsPrint() {
        try {
            log("xlsFunction: started--------");
            wp.reportId = mProgName;
            // -cond-
            subTitle();
            wp.colSet("cond_1", reportSubtitle);

            // ===================================
            TarokoExcel xlsx = new TarokoExcel();
            wp.fileMode = "N";
            xlsx.excelTemplate = mProgName + ".xlsx";

            // ====================================
            // -明細-
            xlsx.sheetName[0] = "明細";
            queryFunc();
            wp.setListCount(1);
            log("Detl: rowcnt:" + wp.listCount[0]);
            xlsx.processExcelSheet(wp);
            /*
             * //-合計- xlsx.sheetName[1] ="合計"; query_Summary(cond_where);
             * wp.listCount[1] =sqlRowNum; ddd("Summ: rowcnt:" +
             * wp.listCount[1]); //xlsx.sheetNo = 1; xlsx.processExcelSheet(wp);
             */
            xlsx.outputExcel();
            xlsx = null;
            log("xlsFunction: ended-------------");

        } catch (Exception ex) {
            wp.expMethod = "xlsPrint";
            wp.expHandle(ex);
        }
    }

    void pdfPrint() throws Exception {
        wp.reportId = mProgName;
        // -cond-
        // subTitle();
        wp.colSet("cond_1", reportSubtitle);
        // ===========================
        wp.pageRows = 99999;
        // queryFunc();
        TarokoPDF pdf = new TarokoPDF();
        String exPrintKind = wp.itemStr("ex_print_kind");
        String exRefno = wp.itemStr("ex_refno");
        String exTxDate = wp.itemStr("ex_tx_date");

        lsWhere = " where 1=1 ";

        // 自選條件
        lsWhere += sqlCol(exRefno, "a.refno");
        lsWhere += sqlCol(exTxDate, "a.tx_date");

        switch (exPrintKind) {
            case "1":
                pdf.excelTemplate = "genm0150r02.xlsx";
                wfReport();
                break;
            case "2":
                pdf.excelTemplate = "genm0150r01.xlsx";
                wfReportMemo3();
                break;
            case "3":
                pdf.excelTemplate = "genm0150r03.xlsx";
                lsWhere += " and b.brn_rpt_flag ='Y' ";
                wfReport();
                break;
        }
        // 計算報表頁面

        pageQuery();
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            wp.respHtml = "TarokoErrorPDF";
            return;
        }

        wp.setListCount(1);
        int selCt = wp.selectCnt;
        for (int ii = 0; ii < selCt; ii++) {
            wfChkAcNo(ii, wp.colStr(ii, "ac_no"));
            wp.colSet(ii, "db_cname", sellectUsrcname(wp.colStr(ii, "crt_user")));
        }
//        if(exPrintKind.equals("1")){
//            for (int ii = 0; ii < selCt; ii++) {
//                if(ii > 0){
//                    if(wp.colStr(ii,"dbcr").equals(wp.colStr(ii - 1,"dbcr"))){
//                        wp.colSet(ii, "db_dbcr", "");
//                    }
//                }
//            }
//        }

        wp.fileMode = "N";
        pdf.sheetNo = 0;
        pdf.pageCount = 40;
        pdf.pageVert = true; // 直印
        pdf.procesPDFreport(wp);
        pdf = null;
    }

    void wfReport1() throws Exception {

        // if(empty(wp.item_ss("ex_std_cd"))){
        // alert_err("分錄代碼不可空白!");
        // return;
        // }
        // String[] aa_dbcr = wp.itemBuff("dbcr");
        // String[] aa_ac_no = wp.itemBuff("ac_no");
        // String[] aa_amt = wp.itemBuff("amt");
        // for (int ll = 0; ll < aa_dbcr.length; ll++) {
        // detail_vouch(ll+1,aa_dbcr[ll],aa_ac_no[ll],aa_amt[ll]);
        // }
        // alert_msg("處理完成!");

    }

    void wfReport() throws Exception {
        wp.sqlCmd = "select d.brno, "
                + "d.r_brno, "
                + "d.tx_date, "
                + "d.dept, "
                + "d.depno, "
                + "d.curr, "
                + "d.refno, "
                + "d.seqno, "
                + "d.voucher_cnt, "
                + "d.ac_no, "
                + "d.dbcr, "
                + "d.dbcr1, "
                + "decode(d.sign_flag,'0',null,d.sign_flag) as sign_flag,"                
//              + "decode(d.sign_flag,0,null) as sign_flag,"
//				+ "d.sign_flag, "
                + "d.amt, "
                + "d.crt_user, "
                + "'' db_cname, "
                + "d.apr_user, "
                + "d.sys_rem, "
                + "d.ias24_id, "
                + "d.acct_no, "
                + "d.acct_name, "
                + "d.memo1, "
                + "d.memo2, "
                + "d.memo3, "
                + "d.sys_rem, "
                + "d.jrn_status, "
                + "d.post_flag, "
                + "d.mod_user, "
                + "d.mod_time, "
                + "d.mod_pgm, "
                + "d.mod_seqno, "
                + "d.key_value, "
                + "d.ac_full_name, "
                + "d.curr_desc, "
                + "e.brief_chi_name AS db_brn_name, "	//TCB 
//                + "decode(d.ac_no,'17510100',e.brief_chi_name,'') db_brn_name, " // 20200117 詢問  U-1310 VICKY answer
                + "(e.chi_addr_1 || e.chi_addr_2) db_brn_addr1, "
                + "(e.chi_addr_5) db_brn_addr2, "
                + "to_char(sysdate,'YYYY/MM/DD') sys_date1, "
                + "to_char(sysdate,'HH24:MM:SS') sys_time1 "
                + "from ("
                + "SELECT a.brno, "
                + "SUBSTR(a.memo3,4,3) r_brno, "
                + "a.tx_date, "
                + "a.dept, "
                + "a.depno, "
                + "a.curr, "
                + "a.refno, "
                + "a.seqno, "
                + "a.voucher_cnt, "
                + "a.ac_no, "
                + "decode (a.dbcr,'C','DEBIT','D','CREDIT') dbcr, "
                + "decode (a.dbcr,'C','CREDIT','D','DEBIT') dbcr1, "
//				+ "a.sign_flag, "
//              + "decode(a.sign_flag,0,null) as sign_flag,"
                + "decode(a.sign_flag,'0',null, a.sign_flag) as sign_flag,"
                + "a.amt, "
                + "a.crt_user, "
                // + "(select usr_cname from sec_user where usr_id = a.crt_user)
                // db_crt_user, "
                + "a.apr_user, "
                // + "SUBSTR(a.memo1,1,10) memo1, " //20200720 USER反映 拿掉
                // + "SUBSTR(a.memo2,1,10) memo2, " //20200720 USER反映 拿掉
                + "a.memo1, " // 20200720 add
                + "a.memo2, " // 20200720 add
                + "a.memo3, "
                + "a.sys_rem, "
                + "a.ias24_id, "
                + "a.acct_no, "
                + "a.acct_name, "
                + "a.jrn_status, "
                + "a.post_flag, "
                + "a.mod_user, "
                + "a.mod_time, "
                + "a.mod_pgm, "
                + "a.mod_seqno, "
                + "a.key_value, "
                + "b.ac_full_name, "
                + "'' as db_empty, "
                + "(c.curr_eng_name || c.curr_chi_name) curr_desc "
                + "FROM gen_vouch a "
                + "LEFT JOIN gen_acct_m b ON a.ac_no = b.ac_no "
                + "LEFT JOIN ptr_currcode c ON a.curr = c.curr_code_gl ";
        wp.sqlCmd += lsWhere;
        wp.sqlCmd += " ) as d ";
        wp.sqlCmd += "LEFT JOIN gen_brn e ON d.r_brno = e.branch ";
        wp.sqlCmd += "order by d.seqno ";

    }

    void wfReportMemo3() throws Exception {
        wp.sqlCmd = "select a.ac_no,"
                + "ROW_NUMBER() OVER(ORDER BY a.ac_no) AS page_no, "
                + "b.ac_full_name, "
                + "a.brno, "
                + "a.memo1, "
                + "a.memo2, "
                + "a.memo3, "
                + "to_char(sysdate,'YYYY/MM/DD') sys_date, "
                + "to_char(sysdate,'HH24:MM:SS') sys_time, "
                + "decode(a.dbcr,'C','借','D','貸') as db_dbcr, "
                + "(c.bill_curr_code||c.curr_chi_name) as curr_desc, "
                + "a.amt, "
                + "a.crt_user "
                + "FROM gen_memo3 a "
                + "LEFT JOIN gen_acct_m b ON a.ac_no = b.ac_no "
                + "LEFT JOIN ptr_currcode c ON a.curr = c.curr_code_gl "
                + "";
        wp.sqlCmd += lsWhere;

    }

    public int detailVouch(int seqNo, String dbcr, String acNo, String amt) throws Exception {

        String szTmp = "", szBuffer = "";
        String blk = "　";
        String hBusinssChiDate = "";
        String rptId = hGsvhModWs;
        String rptName = hGsvhModWs;
        String cDate = "";
        int actCnt = 0;
        String lsSql = " select ac_no,ac_brief_name,memo3_flag,cr_flag,dr_flag,memo3_flag,memo3_kind";
        lsSql += " from gen_acct_m";
        lsSql += " where  ac_no = :ac_no ";
        setString("ac_no", acNo);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
            hTempAcBriefName = sqlStr("ac_brief_name");
            hMemo3FlagCom = sqlStr("memo3_flag");
            hVoucAcNo = sqlStr("ac_no");
            hAccmCrFlag = sqlStr("cr_flag");
            hAccmDrFlag = sqlStr("dr_flag");
        }

        if ((((hGsvhDbcr.equals("D")) && (hAccmCrFlag.equals("Y")) ||
                (hGsvhDbcr.equals("C")) && (hAccmDrFlag.equals("Y"))) &&
                hMemo3FlagCom.equals("Y")) ||
                hVoucAcNo.equals("60000300")) {
            if (wp.loginUser.length() != 10) {
                hGsvhModUser = "";
            }

            vouchPageCnt++;
            szBuffer = "";
            szBuffer = insertStr(szBuffer, "報表名稱:", 1);
            szBuffer = insertStr(szBuffer, hGsvhModWs, 11);
            szBuffer = insertStr(szBuffer, "＊＊＊ Ｍ Ｅ Ｍ Ｏ ＊＊＊", 26);
            szBuffer = insertStr(szBuffer, "頁    次:", 58);
            szTmp = String.format("%4d", vouchPageCnt);
            szBuffer = insertStr(szBuffer, szTmp, 70);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));
            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "   ", 40);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "待沖聯", 36);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "   ", 40);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            String lsSql5 = "select substr(to_char(to_number(?)- 19110000,'0000000'),2,7) as vouch_date";
            lsSql5 += " from ptr_businday";
            setString(1, wp.itemStr("ex_tx_date"));
            sqlSelect(lsSql5);
            if (sqlRowNum > 0) {
                hBusinssChiDate = sqlStr("vouch_date");
            }
            szBuffer = "";
            szBuffer = insertStr(szBuffer, "單    位:", 1);
            szBuffer = insertStr(szBuffer, "3144", 11);
            szBuffer = insertStr(szBuffer, "交易日期:", 58);

            cDate = hBusinssChiDate.substring(0, 3) + "年" + hBusinssChiDate.substring(3, 5) + "月" + hBusinssChiDate.substring(5) + "日";
            szBuffer = insertStr(szBuffer, cDate, 68);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, "交易序號:", 58);
            szBuffer = insertStr(szBuffer, hVoucRefno, 68);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "   ", 40);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "會計科子細目 :", 10);
            szBuffer = insertStr(szBuffer, hVoucAcNo, 25);
            szBuffer = insertStr(szBuffer, hTempAcBriefName, 35);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "摘        要 :", 10);
            szBuffer = insertStr(szBuffer, hGsvhMemo1, 25);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, hGsvhMemo2, 25);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, hGsvhMemo3, 25);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "借 貸 性 質  :", 10);
            if (hGsvhDbcr.equals("C"))
                szBuffer = insertStr(szBuffer, "DR  借方", 25);
            else
                szBuffer = insertStr(szBuffer, "CR  貸方", 25);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            String ls_sql6 = "select CURR_ENG_NAME||' '||CURR_CHI_NAME as CURR_ENG_NAME ";
            ls_sql6 += " from ptr_currcode";
            ls_sql6 += " where  curr_code_gl = :curr_code_gl ";
            setString("curr_code_gl", hGsvhCurr);
            sqlSelect(ls_sql6);
            if (sqlRowNum > 0) {
                hEngName = sqlStr("CURR_ENG_NAME");
            }

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "幣       別  :", 10);
            szBuffer = insertStr(szBuffer, hEngName, 25);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "金        額 :", 10);

            // szTmp=String.format("$" + num_2str(h_vouc_amt,
            // "####,###,###.##"));
            szTmp = String.format("%,.2f", hVoucAmt);
            szBuffer = insertStr(szBuffer, szTmp, 30);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "   ", 40);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "------------------------------------------------------------------------------";
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, "銷 帳 摘 要  :", 1);
            szBuffer = insertStr(szBuffer, hGsvhMemo3, 16);
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "------------------------------------------------------------------------------";
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, "經副襄理 :             主管 :      ", 1);
            szBuffer = insertStr(szBuffer, "覆核 :            經辦員 :", 45);
            // String type = h_vouc_refno.substring(2, 3);
            // switch (type)
            // {
            // case "0" :temp_x06="共用" ;break;
            // case "1" :temp_x06="作業" ;break;
            // case "2" :temp_x06="作業" ;break;
            // case "3" :temp_x06="風管" ;break;
            // case "4" :temp_x06="催收" ;break;
            // case "5" :temp_x06="發卡" ;break;
            // case "6" :temp_x06="客服" ;break;
            // case "7" :temp_x06="行銷" ;break;
            // case "8" :temp_x06="授權" ;break;
            // default :temp_x06="共用" ;break;
            // }
            tempX06 = wp.loginUser;
            szBuffer += tempX06;
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));
            szBuffer = "------------------------------------------------------------------------------";
            lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            // showLogMessage("I", "err_rtn :", "8888888=" + rptSeq + "," +
            // (33-rptSeq) );
            int cnt_space = 27 * vouchPageCnt - rptSeq;
            for (int inti = 0; inti < cnt_space; inti++) {
                szBuffer = "";
                szBuffer = insertStr(szBuffer, blk, 1);
                lpar.add(putReport(rptId, rptName, wp.sysDate + wp.sysTime, rptSeq++, "0", szBuffer));

            }
            actCnt = insertPtrBatchMemo3(lpar, vouchPageCnt);
        }

        hGsvhMemo1 = "";
        hGsvhMemo2 = "";
        hGsvhMemo3 = "";
        return actCnt;
    }

    public static String insertStr(String sbuf, String str, int ps) {
        String rtn = "";
        int len = 0;
        for (int i = 0; i < sbuf.length(); i++) {

            int acsii = sbuf.charAt(i);
            int n = (acsii < 0 || acsii > 128) ? 2 : 1;
            if (len + n >= ps)
                break;
            len += n;
            rtn += sbuf.charAt(i);
        }
        for (int i = len + 1; i < ps; i++)
            rtn += " ";
        rtn += str;

        return rtn;
    }

    public static Map<String, Object> putReport(String prgmid, String prgmName, String sysDate, int seq, String knd, String ctn) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("prgmId", prgmid);
        temp.put("prgmName", prgmName);
        temp.put("sysDate", sysDate);
        temp.put("seq", seq);
        temp.put("kind", knd);
        temp.put("content", ctn);
        return temp;
    }

    public int insertPtrBatchMemo3(List<Map<String, Object>> lpar, int pageCnt) throws Exception {
        int actCnt = 0;
        noTrim = "Y";
        int pageSize = 27;
        int j = pageSize * (pageCnt - 1);
        busi.SqlPrepare sp = new SqlPrepare();

        for (int i = j; i < lpar.size(); i++) {
            sp.sql2Insert("ptr_batch_memo3");

            String tmpStr = lpar.get(i).get("sysDate").toString();
            if (tmpStr.length() > 8) {
                sp.ppstr("start_date", tmpStr.substring(0, 8));
                sp.ppstr("start_time", tmpStr.substring(8));
            } else {
                sp.ppstr("start_date", tmpStr.substring(0));
                sp.ppstr("start_time", "");
            }
            int lineCnt = i + 1;

            sp.ppstr("program_code", lpar.get(i).get("prgmId").toString());
            sp.ppstr("rptname", lpar.get(i).get("prgmName").toString());

            // setValue("seq" , lpar.get(i).get("seq").toString());
            sp.ppstr("seq", lineCnt + " ");
            sp.ppstr("kind", lpar.get(i).get("kind").toString());
            sp.ppstr("txt_content", lpar.get(i).get("content").toString());

            sqlExec(sp.sqlStmt(), sp.sqlParm());

            if (sqlRowNum <= 0) {
                return -1;
            }
        }
        noTrim = "";
        return actCnt;
    }

    public void processAjaxOption() throws Exception {
        wp.varRows = 1000;
        String lsSql = "select ac_no,ac_full_name "
                + " ,ac_no||'_'||ac_full_name as inter_desc "
                + " from gen_acct_m "
                + " where 1=1 and ac_no like :ac_no "
                + " order by ac_no ";
        setString("ac_no", wp.getValue("ex_gen_acct", 0) + "%");
        sqlSelect(lsSql);

        for (int i = 0; i < sqlRowNum; i++) {
            wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
            wp.addJSON("OPTION_VALUE", sqlStr(i, "ac_no"));
        }
        return;
    }

    void ItemChanged() throws Exception {
        String exGenAcct = wp.itemStr("ex_gen_acct");
        String lsSql = "";
        lsSql = "select ac_full_name from gen_acct_m "
                + "where ac_no =:ac_no ";
        setString("ac_no", exGenAcct);
        sqlSelect(lsSql);
        if (sqlRowNum > 0) {
            wp.colSet("ex_ac_full_name", sqlStr("ac_full_name"));
        } else {
            wp.colSet("ex_ac_full_name", "");
        }
        // 解決純新增資時自動增加一筆問題
        int rowcnt = 0;
        aaAcNo = wp.itemBuff("ac_no");
        if (!(aaAcNo == null) && !empty(aaAcNo[0]))
            rowcnt = aaAcNo.length;
        wp.listCount[0] = rowcnt;
        // wf_read_stdcode(wp.item_ss("ex_std_cd"), "");
    }

    String sellectUsrcname(String userid) throws Exception {
        String sqlSelect = " select usr_id||'['||usr_cname||']' as userid  from sec_user where usr_id = :usr_id ";
        setString("usr_id", userid);
        sqlSelect(sqlSelect);
        if (sqlRowNum > 0) {
            userid = sqlStr("userid");
        }
        return userid;
    }

    public void wfChkCol(TarokoCommon wr) throws Exception {
        super.wp = wr;
        String textData1 = wp.itemStr("text_data1");
        String textData2 = wp.itemStr("text_data2");
        String textData3 = wp.itemStr("text_data3");
        String rowIndex = wp.itemStr("row_index");
        String lsSql = "";
        //System.out.println("1:"+text_data1+" 2:"+text_data2+" 3:"+text_data3+" 4:"+row_index);
        switch (textData1) {
            case "dddw_ac_no":
                wp.varRows = 1000;
                lsSql = "select ac_no,ac_full_name "
                        + " ,ac_no||'_'||ac_full_name as inter_desc"
                        + " from gen_acct_m "
                        + " where 1=1 and ac_no like :ac_no "
                        + " order by ac_no ";
//			setString("ac_no", wp.getValue("ex_gen_acct", 0) + "%");
                setString("ac_no", textData2 + "%");
                sqlSelect(lsSql);

                for (int i = 0; i < sqlRowNum; i++) {
                    wp.addJSON("OPTION_TEXT", sqlStr(i, "inter_desc"));
                    wp.addJSON("OPTION_VALUE", sqlStr(i, "ac_no"));
                }
                // 科子細目中文(每筆資料)
            case "ac_no":
                lsSql = "select ac_full_name "
                        + ",memo3_flag "
                        + "from gen_acct_m "
                        + "where 1=1 ";
                lsSql += sqlCol(textData2, "ac_no");
                sqlSelect(lsSql);
                if (sqlRowNum <= 0) {
                    wp.colSet("data_msg", "科目不存在");
                    wp.addJSON("data_msg", "科目不存在");
                    wp.colSet("ac_full_name", "");
                    wp.addJSON("ac_full_name", "");
                    wp.colSet("chk_flag", "err");
                    wp.addJSON("chk_flag", "err");
                } else {
                    wp.colSet("chk_flag", "OK");
                    wp.addJSON("chk_flag", "OK");
                    wp.colSet("ac_full_name", sqlStr("ac_full_name"));
                    wp.addJSON("ac_full_name", sqlStr("ac_full_name"));
                    wp.colSet("db_memo3_flag", sqlStr("memo3_flag"));
                    wp.addJSON("db_memo3_flag", sqlStr("memo3_flag"));
                }
                break;
            //
            case "ac_no1":
                lsSql = "select ac_full_name "
                        + ",memo3_flag "
                        + "from gen_acct_m "
                        + "where 1=1 ";
                if (empty(textData2)) {
                    return;
                }
                lsSql += sqlCol(textData2, "ac_no");
                sqlSelect(lsSql);
                if (sqlRowNum <= 0) {
                    wp.colSet("ac_full_name", "");
                    wp.addJSON("ac_full_name", "");
                    wp.colSet("data_msg", "科目不存在");
                    wp.addJSON("data_msg", "科目不存在");
                } else {
                    wp.colSet("ac_full_name", sqlStr("ac_full_name"));
                    wp.addJSON("ac_full_name", sqlStr("ac_full_name"));
                    wp.colSet("db_memo3_flag", sqlStr("memo3_flag"));
                    wp.addJSON("db_memo3_flag", sqlStr("memo3_flag"));
                }
                break;
        }
        wp.colSet("text_data1", textData1);
        wp.colSet("text_data2", textData2);
        wp.colSet("text_data3", textData3);
        wp.addJSON("text_data1", textData1);
        wp.addJSON("text_data2", textData2);
        wp.addJSON("text_data3", textData3);
        // 新增批號第2筆資料的index要+1
        // if(text_data1.equals("acct_key")){
        // if(!serno.equals("01")){
        // row_index = int_2Str(Integer.parseInt(row_index) + 1);
        // }
        // }
//		wp.colSet("row_index", row_index);
//		wp.addJSON("row_index", row_index);
    }
    public boolean isIntegerForDouble(double obj) {
        double eps = 1e-10;
        return obj-Math.floor(obj) < eps;
    }
    /***********************************************************************/
    public double BigDecimalAdd(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }
    /***********************************************************************/
    public double BigDecimalSub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    void reSerno() throws Exception {
        log("GG");
        opt = wp.itemBuff("opt");
        aaRowid = wp.itemBuff("rowid");
        aaModSeqno = wp.itemBuff("mod_seqno");

        aaBrno = wp.itemBuff("brno");
        aaTxDate = wp.itemStr("ex_tx_date");
        aaDept = wp.itemBuff("dept");
        aaDepno = wp.itemBuff("depno");
        aaRefno = wp.itemBuff("refno");
        aaCurr = wp.itemBuff("curr");
        aaSeqno = wp.itemBuff("seqno");
        aaVoucherCnt = wp.itemBuff("voucher_cnt");
        aaAcNo = wp.itemBuff("ac_no");
        aaDbcr = wp.itemBuff("dbcr");
        aaDbDbcr = wp.itemBuff("db_dbcr");
        aaSignFlag = wp.itemBuff("sign_flag");
        aaAmt = wp.itemBuff("amt");
        aaCrtUser = wp.itemBuff("crt_user");
        aaAprUser = wp.itemBuff("apr_user2");
        aaMemo1 = wp.itemBuff("memo1");
        aaMemo2 = wp.itemBuff("memo2");
        aaMemo3 = wp.itemBuff("memo3");
        aaJrnStatus = wp.itemBuff("jrn_status");
        aaPostFlag = wp.itemBuff("post_flag");
        aaDbOptcode = wp.itemBuff("db_optcode");
        aaDbBrief = wp.itemBuff("db_brief");
        aaDbCrFlag = wp.itemBuff("db_cr_flag");
        aaDbDrFlag = wp.itemBuff("db_dr_flag");
        aaDbMemo3Flag = wp.itemBuff("db_memo3_flag");
        aaDbMemo3Kind = wp.itemBuff("db_memo3_kind");
        aaDbInsplist = wp.itemBuff("db_insplist");
        aaDbBrnRptFlag = wp.itemBuff("db_brn_rpt_flag");
        aaKeyValue = wp.itemBuff("key_value");
        aaSysRem = wp.itemBuff("sys_rem");
        aaIdNo = wp.itemBuff("id_no");
        aaDbNocode = wp.itemBuff("db_nocode");
        aaDbOldMemoChg = wp.itemBuff("db_old_memo_chg");
        aaIfrsFlag = wp.itemBuff("ifrs_flag");
        aaCurrCodeDc = wp.itemBuff("curr_code_dc");
        aaDbBrief1 = wp.itemBuff("db_brief1");
        wp.listCount[0] = aaAcNo.length;

        for (int ii = 0; ii < aaAcNo.length; ii++) {
            log("db_brief1 :"+aaDbcr[ii]);
            wp.colSet(ii,"dbcr",aaDbcr[ii]);
            wp.colSet(ii,"db_brief1",aaDbBrief1[ii]);
            if(ii < 9){
                wp.colSet(ii,"SER_NUM","0"+(ii+1));
            } else{
                wp.colSet(ii,"SER_NUM",(ii+1)+"");
            }
        }
        wp.colSet("new_flag", "N");
        return;
    }
}
