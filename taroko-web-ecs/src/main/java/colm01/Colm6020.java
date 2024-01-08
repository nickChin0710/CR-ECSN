/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 112-01-11  V1.00.00  Yang Bo      program initial                          *
 * 112-09-22  V1.00.01  Ryan         業績分行查詢權限調整,調整分頁100筆,調整協商狀態顯示                    *
 * 112-09-25  V1.00.02  Ryan         免列報 Y顯示紅色粗體                                                                             *
 * 112-10-20  V1.00.03  Ryan         修正催理紀錄讀取固定acct_type = 01                                                                            *
 ******************************************************************************/
package colm01;

import busi.func.CmsFunc;
import ofcapp.BaseAction;

public class Colm6020 extends BaseAction {
    private String lsWhere = "";

    @Override
    public void userAction() throws Exception {
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) {
            // -資料讀取-
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {
            /* 新增功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page */
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "C")) {
            // -資料處理-
            procFunc();
        }
    }

    @Override
    public void dddwSelect() {
        try {
            if (eqIgno(wp.respHtml, "colm6020")) {
                // 業績分行
                wp.optionKey = wp.itemStr2("ex_reg_bank_no");
                dddwList("d_dddw_reg_bank_no", "col_cs_rpt", "distinct reg_bank_no",
                        "reg_bank_no||'_'||reg_bank_name", " where 1 = 1 ");
                // 帳務類別
                wp.optionKey = wp.itemStr2("ex_acct_type");
                dddwList("d_dddw_acct_type",
                        "select distinct t1.acct_type as db_code, t1.acct_type||'_'||t2.chin_name as db_desc" +
                                " from col_cs_rpt t1 " +
                                " inner join ptr_acct_type t2 on t1.acct_type = t2.acct_type " +
                                " where 1=1 ");
                // 雙幣幣別
                wp.optionKey = wp.itemStr2("ex_curr_code");
                dddwList("d_dddw_curr_code", "ptr_sys_idtab", "wf_id",
                        "wf_desc", "where wf_type = 'DC_CURRENCY' order by wf_id");
            }
        } catch (Exception e) {
        }

        try {
            if (eqIgno(wp.respHtml, "colm6020_detl")) {
                // 處理結果類型
                wp.optionKey = wp.itemStr2("proc_code");
                dddwList("dddw_proc_code", "ptr_sys_idtab", "wf_id||'.'||wf_desc",
                        "wf_id||'.'||wf_desc", " where wf_type = 'COLM6020' ");
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void queryFunc() throws Exception {
        if (!getWhereStr()) {
            selectMaxCreateDate();
            return;
        }

        wp.whereStr = lsWhere;
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();
        selectMaxCreateDate();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        wp.selectSQL = " c1.id_no, c1.chi_name, c1.corp_no, c1.corp_chi_name, c1.int_rate_mcode, c1.acct_type, p1.chin_name, " +
                " c1.acct_status, c1.card_no, c1.reg_bank_no, c1.reg_bank_name, c1.risk_bank_no, c1.risk_bank_name, c1.curr_code, " +
                " p2.curr_chi_name, c1.dc_ttl_amt_bal, c1.dc_min_pay_bal, c1.collect_flagx, c1.create_date ," +
                // 內頁欄位
                " c1.annual_income, c1.birthday, c1.education, c1.business_code, c1.job_position, c1.service_year, " +
                " c1.home_area_code1, c1.home_tel_no1, c1.home_tel_ext1, c1.office_area_code1, c1.office_tel_no1, " +
                " c1.office_tel_ext1, c1.cellar_phone, c1.line_of_credit_amt, c1.autopay_acct_bank, c1.autopay_acct_no, " +
                " c1.no_delinquent_flag, c1.no_tel_coll_flag, c1.no_collection_flag, c1.pay_by_stage_flag, c1.no_sms_flag, " +
                " c1.ttl_amt, c1.stmt_over_due_amt, c1.id_p_seqno, c1.acno_p_seqno as p_seqno, c1.corp_p_seqno, c1.company_name ";
        wp.daoTable = " col_cs_rpt c1 " +
                " left join ptr_acct_type p1 on c1.acct_type = p1.acct_type " +
                " left join ptr_currcode p2 on c1.curr_code = p2.curr_code ";
        wp.whereOrder = " order by c1.id_no ";
        pageQuery();

        if (sqlNotFind()) {
            alertErr2("此條件查無資料");
            return;
        }

        listWkdata();
        listWkdataA();
        wp.setListCount(1);
        wp.setPageValue();
    }

    @Override
    public void querySelect() throws Exception {
        String idNo = wp.itemStr("data_k1");
        String cardNo = wp.itemStr("data_k2");
        String createDate = wp.itemStr("data_k3");
        if (empty(idNo) && empty(cardNo)) {
            alertErr2("正卡人證號/代表卡號: 不可同時空白");
            return;
        }

        lsWhere = " where 1 = 1 ";
        if (!empty(idNo)) {
        	lsWhere += " and c1.id_no = :idNo ";
        	setString("idNo",idNo);
        }
        if (!empty(cardNo)) {
        	lsWhere += " and c1.card_no = :cardNo ";
          	setString("cardNo",cardNo);
        }
        if (!empty(createDate)) {
        	lsWhere += " and c1.create_date = :createDate ";
          	setString("createDate",createDate);
        }
        wp.whereStr = lsWhere;
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();

        String idPSeqno = wp.colStr("id_p_seqno");
        String pSeqno = wp.colStr("p_seqno");
        if (empty(idPSeqno) && empty(pSeqno)) {
            alertErr2("查無正卡人資料");
        }
        wp.whereStr = " where 1 = 1 ";
        wp.whereStr += " and cs.acct_type = :acct_type ";
        wp.whereStr += " and cs.id_p_seqno = :idPSeqno ";
        wp.whereStr += " and cs.p_seqno = :pSeqno ";
        setString("acct_type",wp.colStr("acct_type"));
        setString("idPSeqno",idPSeqno);
        setString("pSeqno",pSeqno);
        wp.queryWhere = wp.whereStr;
        selectCsLog();
        selectPaymentNo();
        CmsFunc cmsFunc = new CmsFunc(wp);
        wp.colSet("education_desc", cmsFunc.getEducationDesc(wp.colStr("education")));
        wp.colSet("business_desc", cmsFunc.getBusinessDesc(wp.colStr("business_code")));
    }

    @Override
    public void dataRead() throws Exception {
    }

    @Override
    public void saveFunc() throws Exception {
        String idNo = wp.itemStr("id_no");
        String cardNo = wp.itemStr("card_no");
        colm01.Colm6020Func func = new colm01.Colm6020Func();
        func.setConn(wp);

        if (eqAny(strAction, "A")) {
            String sql = " select usr_id||'_'||usr_cname as usr_id, usr_deptno from sec_user where usr_id = ?";
            sqlSelect(sql, new Object[]{wp.loginUser});
            if (sqlRowNum <= 0) {
                errmsg("無法取得 [員工編號] [員工所屬單位代號]!");
                return;
            }

            if (empty(sqlStr("usr_id")) || empty(sqlStr("usr_deptno"))) {
                errmsg("無法取得 [員工編號] [員工所屬單位代號]!");
                return;
            }

            wp.itemSet("crt_date", wp.sysDate);
            wp.itemSet("crt_time", wp.sysTime);
            wp.itemSet("proc_user", sqlStr("usr_id"));
            wp.itemSet("proc_user_deptno", sqlStr("usr_deptno"));
            String procCode = wp.itemStr("proc_code");
            wp.itemSet("proc_code", procCode.split("\\.")[0]);
            wp.itemSet("proc_code_desc", procCode.split("\\.")[1]);
            rc = func.dbInsert();
        }
        sqlCommit(rc);
        if (rc != 1) {
            alertErr2(func.getMsg());
        }

        wp.itemSet("data_k1", idNo);
        wp.itemSet("data_k2", cardNo);
        querySelect();
        initTab2();
    }

    @Override
    public void procFunc() throws Exception {
    }

    @Override
    public void initButton() {
    }

    @Override
    public void initPage() {
        if (eqIgno(wp.respHtml, "colm6020")) {
            selectMaxCreateDate();
            selectUserBankNo();
            wp.colSet("ex_rate_mcode_s", "00");
        }
    }

    private boolean getWhereStr() {
        String exRegBankNo = wp.itemStr("ex_reg_bank_no");
        String exAcctType = wp.itemStr("ex_acct_type");
        String exAcctStatus = wp.itemStr("ex_acct_status");
        String exIdNo = wp.itemStr("ex_id_no");
        String exCorpNo = wp.itemStr("ex_corp_no");
        String exCurrCode = wp.itemStr("ex_curr_code");
        String exCreateDate = wp.itemStr("ex_create_date");
        String exRateMcodeS = wp.itemStr("ex_rate_mcode_s");
        String exRateMcodeE = wp.itemStr("ex_rate_mcode_e");

        if (empty(exRegBankNo) && empty(exAcctType) && empty(exAcctStatus)
                && empty(exIdNo) && empty(exCorpNo) && empty(exCurrCode)
                && (empty(exRateMcodeS) || empty(exRateMcodeE))
        ) {
            alertErr("請至少輸一項查詢條件");
            return false;
        }

        lsWhere = " where 1 = 1";
        if (!empty(exCreateDate)) {
            lsWhere += sqlCol(exCreateDate, "c1.create_date");
        }

        if (!empty(exRegBankNo)) {
            lsWhere += sqlCol(exRegBankNo, "c1.reg_bank_no");
        }

        if (!empty(exAcctType)) {
            lsWhere += sqlCol(exAcctType, "c1.acct_type");
        }

        if (!empty(exAcctStatus)) {
            lsWhere += sqlCol(exAcctStatus, "c1.acct_status");
        }

        if (!empty(exIdNo) && exIdNo.length() != 10) {
            alertErr("正卡人證號 需輸入10碼");
        } else {
            lsWhere += sqlCol(exIdNo, "c1.id_no");
        }

        if (!empty(exCorpNo) && exCorpNo.length() < 8) {
            alertErr("商務卡統編 至少輸入8碼");
        } else {
            lsWhere += sqlCol(exCorpNo, "c1.corp_no");
        }

        if (!empty(exCurrCode)) {
            lsWhere += sqlCol(exCurrCode, "c1.curr_code");
        }

        if (!empty(exRateMcodeS)) {
            exRateMcodeS = Integer.valueOf(exRateMcodeS).toString();
            lsWhere += " and cast(c1.int_rate_mcode as integer) >= cast(? as integer) ";
            setString(exRateMcodeS);
        }

        if (!empty(exRateMcodeE)) {
            exRateMcodeE = Integer.valueOf(exRateMcodeE).toString();
            lsWhere += " and cast(c1.int_rate_mcode as integer) <= cast(? as integer) ";
            setString(exRateMcodeE);
        }

        return true;
    }

    private void selectMaxCreateDate() {
        String lsSql = " select max(create_date) as create_date from col_cs_rpt ";
        sqlSelect(lsSql);

        String createDate = sqlStr("create_date");
        if (empty(createDate)) {
            alertErr("此條件查無資料");
        } else {
            wp.colSet("create_date", createDate);
        }
    }

    private void selectUserBankNo() {
        String lsSql = " select bank_unitno from sec_user where usr_id = ? ";
        sqlSelect(lsSql, new Object[]{wp.loginUser});

        wp.colSet("bank_unitno", sqlStr("bank_unitno"));
    }

    private void listWkdata() {
        String wkData;
        String[] code = new String[]{"1", "2", "3", "4"};
        String[] text = new String[]{"1.正常", "2.逾放", "3.催收", "4.呆帳"};
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            wp.colSet(ii, "SER_NUM", String.format("%02d", ii + 1));

            wkData = wp.colStr(ii, "acct_status");
            wp.colSet(ii, "acct_status", commString.decode(wkData, code, text));

            wkData = wp.colStr(ii, "int_rate_mcode");
            if (!empty(wkData)) {
                if (Integer.parseInt(wkData) > 99) {
                    wp.colSet(ii, "int_rate_mcode", "M99");
                } else {
                    wp.colSet(ii, "int_rate_mcode", "M" + wkData);
                }
            }
            if(wp.colEq(ii,"collect_flagx", "Y")) {
            	if(wp.respHtml.indexOf("_detl")>0) {
            	 	wp.colSet("css_font"," col_key");
            	}else {
            	   	wp.colSet(ii,"css_font"," col_key");
            	}
            }
        }
    }

    private void selectCsLog() throws Exception {
//        wp.pageControl();

        daoTid = "C.";
        wp.selectSQL = " cs.crt_date, cs.crt_time, cs.proc_user, cs.proc_user_deptno, cs.acct_type, csp1.chin_name, " +
                " cs.curr_code, csp2.curr_chi_name, cs.proc_code, cs.proc_code_desc, cs.callout_tel, cs.proc_desc, " +
                " cs.id_p_seqno ";
        wp.daoTable = " col_cs_cslog cs " +
                " left join ptr_acct_type csp1 on cs.acct_type = csp1.acct_type " +
                " left join ptr_currcode csp2 on cs.curr_code = csp2.curr_code ";
        wp.whereOrder = " limit 30 ";
        pageSelect();

        if (sqlRowNum > 0) {
            for (int ii = 0; ii < wp.selectCnt; ii++) {
                wp.colSet(ii, "C.ser_num", String.format("%02d", ii + 1));
            }
        }
        wp.setListCount(1);
        selectOK();
    }

    private void selectPaymentNo() {
        String lsSql = " select payment_no, payment_no_ii from act_acno where acno_p_seqno = ? ";
        sqlSelect(lsSql, new Object[]{wp.colStr("p_seqno")});

        wp.colSet("payment_no", sqlStr("payment_no"));
        wp.colSet("payment_no_ii", sqlStr("payment_no_ii"));
    }

    private void initTab2() {
        wp.itemSet("proc_code", "");
        wp.itemSet("sel_callout_tel", "");
        wp.itemSet("callout_tel", "");
        wp.itemSet("proc_desc", "");
    }
    
	  void listWkdataA() {
          String negoType = "";
          String[] negoCde = {};
          String[] negoTxt = {};
          for (int ii = 0; ii < wp.selectCnt; ii++) {
        	  negoType = strMid(wp.colStr(ii, "pay_by_stage_flag"),0,1);
        	  negoCde = new String[] {"1", "2", "3", "4", "5", "6", "7"};
        	  negoTxt = new String[] {"債務協商", "前置協商", "更生", "清算", "個別協商", "消金無擔保展延", "前置調解"};
              wp.colSet(ii, "tt_liab_type", commString.decode(negoType, negoCde, negoTxt));
            if (negoType.equals("1")) {
//              cde = new String[] {"1", "205", "3", "4"};
//              txt = new String[] {"1.停催", "2.復催", "3.協商成功", "4.結案"};
            	negoCde = new String[] {"1", "2", "3", "4", "5","6"};
            	negoTxt = new String[] {"受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
            }
            if (negoType.equals("2")) {
//              negoCde = new String[] {"1", "2", "3", "4", "5"};
//              txt = new String[] {"1.受理申請", "2.停催", "3.簽約成功", "4.結案/復催", "5.結案/結清"};
            	negoCde = new String[] {"1", "2", "3", "4", "5","6"};
            	negoTxt = new String[] {"受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
            }
            if (negoType.equals("3")) {
            	negoCde = new String[] {"1", "2", "3", "4", "5", "6", "7"};
            	negoTxt = new String[] {"更生開始", "更生撤回", "更生認可", "更生履行完畢", "更生裁定免責", "更生調查程序",
                  "更生駁回"};
            }
            if (negoType.equals("4")) {
            	negoCde = new String[] {"1", "2", "3", "4", "5", "6", "7","8"};
            	negoTxt = new String[] {"清算程序開始", "清算程序終止", "清算程序開始同時終止", "清算撤銷免責", "清算調查程序",
                  "清算駁回", "清算撤回", "清算復權"};
            }
            if (negoType.equals("5")) {
//              cde = new String[] {"1", "2", "3", "4"};
//              txt = new String[] {"1.達成個別協商", "2.提前清償", "3.毀諾", "4.毀諾後清償"};
            	negoCde = new String[] {"1", "2", "3", "4", "5", "6"};
            	negoTxt = new String[] {"受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
            }
//            if (negoType.equals("6")) {
//            	negoCde = new String[] {"1", "2", "3"};
//            	negoTxt = new String[] {"1.受理申請", "2.展延成功", "3.取消或結案"};
//            }
            if (negoType.equals("7")) {
//              cde = new String[] {"1", "3", "4", "5", "6"};
//              txt = new String[] {"1.受理申請", "3.簽約成功", "4.結案/復催", "5.結案/結清", "6.本行無債權"};
            	negoCde = new String[] {"1", "2", "3", "4", "5", "6"};
            	negoTxt = new String[] {"受理申請", "停催", "簽約成功", "結案/復催", "結案/毀諾","結案/結清"};
            }
            String negoType2 = strMid(wp.colStr(ii, "pay_by_stage_flag"),1,1);
            wp.colSet(ii, "tt_pay_by_stage_flag", commString.decode(negoType2, negoCde, negoTxt));
            wp.colSet(ii, "nego_type", negoType + negoType2);
          }
        }
}
