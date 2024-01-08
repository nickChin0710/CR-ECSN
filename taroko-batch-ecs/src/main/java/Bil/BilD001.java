/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/26  V1.00.01    shiyuqi       updated for project coding standard   *  
*  111/09/22  V1.00.02    JeffKung     updated for TCB.                       * 
******************************************************************************/

package Bil;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*新分期費用年百分率計算處理程式*/
public class BilD001 extends AccessDAO {
    private String progname = "新分期費用年百分率計算處理程式  111/09/22  V1.00.02";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "BilD001";
    String prgmName = "新分期費用年百分率計算處理程式";
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hPreBusinessDate = "";
    int hContPostCycleDd = 0;
    int hPre29Dd = 0;
    int hPre30Dd = 0;
    int hPre31Dd = 0;
    String hTempContractNo = "";
    String hContContractNo = "";
    String hContProductNo = "";
    String hContMerchantNo = "";
    String hContPtrMerchantNo = "";
    double hContTotAmt = 0;
    double hContUnitPrice = 0;
    int hContInstallTotTerm = 0;
    double hContRemdAmt = 0;
    double hContFirstRemdAmt = 0;
    double hContCltUnitPrice = 0;
    double hContTransRate = 0;
    String hContInstallmentKind = "";
    String hContPurchaseDate = "";
    String hContNewItFlag = "";
    String hContCpsFlag = "";
    String hContRowid = "";
    String hContBatchNo = "";
    String hMercMerchantType = "";
    double hProdTransRate = 0;
    double hAgenRevolvingInterest1 = 0;
    double intAndFeeAmt = 0;
    double hContCltRemdAmt = 0;
    String hContPaymentType = "";

    double[] installPerAmt = new double[300];
    int hPreDd = 0;
    int totalCnt = 0;
    double r0RATE = 20;
    String stderr = "";
    private String hContBillProdType = "";
    private double hContYearFeesRate = 0;
    // *************************************************************************

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1 && args.length != 2) {
                comc.errExit("Usage : BilD001 [[business_date] [contract_no]]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            hTempContractNo = "";
            if (args.length >= 1) {
                if (args.length == 7) {
                    hBusiBusinessDate = args[0];
                } else {
                    hTempContractNo = args[0];
                }
            }

            if (args.length == 2)
                hTempContractNo = args[1];

            selectPtrActgeneral();
            selectPtrBusinday();

            hContPostCycleDd = comcr.str2int(hBusiBusinessDate.substring(6));
            hPreDd = comcr.str2int(hPreBusinessDate.substring(6));

            hPre29Dd = 99;
            hPre30Dd = 99;
            hPre31Dd = 99;

            if (hContPostCycleDd == 1) {
                if (hPreDd == 28) {
                    hPre29Dd = 29;
                    hPre30Dd = 30;
                    hPre31Dd = 31;
                } else if (hPreDd == 29) {
                    hPre30Dd = 30;
                    hPre31Dd = 31;
                } else if (hPreDd == 30) {
                    hPre31Dd = 31;
                }
            }
            
            totalCnt = 0;
            selectBilContract();
            showLogMessage("I", "", String.format("合約檔處理筆數 [%d]", totalCnt));

            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    int selectPtrActgeneral() throws Exception {
        sqlCmd = "select max(round(revolving_interest1*365/100,2)) h_agen_revolving_interest1 ";
        sqlCmd += " from ptr_actgeneral_n ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_actgeneral_n not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hAgenRevolvingInterest1 = getValueDouble("h_agen_revolving_interest1");
        }
        return 0;
    }

    /**********************************************************************/
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select decode(cast(? as varchar(10)),'',business_date,cast(? as varchar(10))) as h_busi_business_date,";
        sqlCmd += "to_char(to_date(decode(cast(? as varchar(10)),'',business_date,cast(? as varchar(10))),'yyyymmdd')-1 days,'yyyymmdd') as h_pre_business_date ";
        sqlCmd += " from ptr_businday ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);
        setString(3, hBusiBusinessDate);
        setString(4, hBusiBusinessDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("h_busi_business_date");
            hPreBusinessDate = getValue("h_pre_business_date");
        }
    }

    /**********************************************************************
     * Inupt : bil_contract  合約明細檔
     * ====================================================
     * Fields : auth_code                                 - 授權碼
     *             contract_kind                            - 合約類別 1:分期  2:郵購
     *             install_tot_term                         - 分期付款總期數
     *             install_curr_term                       - 分期付款目前期數
     *             auto_delv_flag                          - 自動出貨旗標          Y=自動出貨
     *             delv_date                                  - 出貨日期
     *             delv_confirm_date                     - 出貨放行日期 
     *             post_cycle_dd                           - 入帳日 (default 0)
     *             apr_date                                   - 合約放行日期 
     *             first_post_date                          - 第一次入帳日期
     *             year_fees_date                         - 總費用年百分率處理日
     *             new_it_flag                               - 新分期註記
     * ====================================================
     * 資料條件:  1. 分期總期數大於1期
     *                2. 總費用年百分率處理日等於空值
     *                3. 合約類別為分期 或 合約類別為郵購且授權碼不為空或reject
     *                4. 合約確定成立,準備要開始入帳的分期合約
    ***********************************************************************/
    void selectBilContract() throws Exception {
        int int1a;
        double doubleAmt;
        long longAmt1;

        sqlCmd = "select ";
        sqlCmd += "contract_no,";
        sqlCmd += "product_no,";
        sqlCmd += "mcht_no,";
        sqlCmd += "ptr_mcht_no,";
        sqlCmd += "tot_amt,";
        sqlCmd += "unit_price,";
        sqlCmd += "install_tot_term,";
        sqlCmd += "remd_amt,";
        sqlCmd += "first_remd_amt,";
        sqlCmd += "clt_unit_price,";
        sqlCmd += "decode(installment_kind,'','N',installment_kind) h_cont_installment_kind,";
        sqlCmd += "purchase_date,";
        sqlCmd += "decode(new_it_flag     ,'','N',new_it_flag) h_cont_new_it_flag,";
        sqlCmd += "decode(cps_flag        ,'','N',cps_flag)    h_cont_cps_flag,";
        sqlCmd += "trans_rate,";  //分期利率
        sqlCmd += "rowid  as rowid,";
        sqlCmd += "batch_no ";
        sqlCmd += "from bil_contract ";
        sqlCmd += " where  1=1 ";
        sqlCmd += " and install_tot_term > 1 ";
        sqlCmd += " and year_fees_date = '' ";
        sqlCmd += " and contract_no = decode(cast(? as varchar(10)),'',contract_no,cast(? as varchar(10))) ";
        sqlCmd += " and (contract_kind = '1'  ";
        sqlCmd	+= "    or (contract_kind = '2'  and auth_code not in ('' , 'REJECT','P','reject')) ) ";
        sqlCmd += " and ((install_curr_term = 0 ";
        sqlCmd += "       and decode(auto_delv_flag,'','N',auto_delv_flag) = 'Y' ";
        sqlCmd += "       and post_cycle_dd = 0 ";
        sqlCmd += "       and apr_date != '') ";
        sqlCmd += "   or (install_curr_term = 0 ";
        sqlCmd += "       and decode(auto_delv_flag,'','N',auto_delv_flag) != 'Y' ";
        sqlCmd += "       and delv_date  != '' ";
        sqlCmd += "       and post_cycle_dd  = 0 ";
        sqlCmd += "       and delv_confirm_date != '' ) ";
        sqlCmd += "  or (post_cycle_dd = ? ";
        sqlCmd += "       and decode(all_post_flag,'','N',all_post_flag) != 'Y' ";
        sqlCmd += "       and install_curr_term != install_tot_term ) ";
        sqlCmd += "  or (post_cycle_dd = ? ";
        sqlCmd += "      and decode(all_post_flag,'','N',all_post_flag) != 'Y' ";
        sqlCmd += "      and install_curr_term != install_tot_term ) ";
        sqlCmd += "  or (post_cycle_dd = ? ";
        sqlCmd += "      and decode(all_post_flag,'','N',all_post_flag) != 'Y' ";
        sqlCmd += "      and install_curr_term != install_tot_term ) ";
        sqlCmd += "  or (post_cycle_dd = ? ";
        sqlCmd += "      and decode(all_post_flag,'','N',all_post_flag) != 'Y' ";
        sqlCmd += "      and install_curr_term != install_tot_term ) ";
        sqlCmd += "  or (first_post_date >= to_char(add_months( ";
        sqlCmd += "      to_date(?,'yyyymmdd'),-12),'yyyymmdd'))) ";
        

        setString(1, hTempContractNo);
        setString(2, hTempContractNo);
        setInt(3, hContPostCycleDd);
        setInt(4, hPre29Dd);
        setInt(5, hPre30Dd);
        setInt(6, hPre31Dd);
        setString(7, hBusiBusinessDate);

        int cursorIndex = openCursor();
        while (fetchTable(cursorIndex)) {
            hContContractNo = getValue("contract_no");
            hContProductNo = getValue("product_no");
            hContMerchantNo = getValue("mcht_no");
            hContPtrMerchantNo = getValue("ptr_mcht_no");
            hContTotAmt = getValueDouble("tot_amt");
            hContUnitPrice = getValueDouble("unit_price");
            hContInstallTotTerm = getValueInt("install_tot_term");
            hContRemdAmt = getValueDouble("remd_amt");
            hContFirstRemdAmt = getValueDouble("first_remd_amt");
            hContCltUnitPrice = getValueDouble("clt_unit_price");
            hContTransRate = getValueDouble("trans_rate");
            hContInstallmentKind = getValue("h_cont_installment_kind");
            hContPurchaseDate = getValue("purchase_date");
            hContNewItFlag = getValue("h_cont_new_it_flag");
            hContCpsFlag = getValue("h_cont_cps_flag");
            hContRowid = getValue("rowid");
            hContBatchNo = getValue("batch_no");

            intAndFeeAmt = 0;
            /*** 特店歸屬類別 X:無法歸類 ***/
            hContBillProdType = "X";

            totalCnt++;

            /*** 若分期付款種類為 A:自動分期 或 D:指定卡片分期 ***/
            if ((hContInstallmentKind.equals("A"))
                    || (hContInstallmentKind.equals("D"))) {
                /*** 由分期產品檔取得 分期利率 與 總費用年百分率 ***/
                if (selectBilProd1() != 0) {
                    showLogMessage("I", "", String.format("select bil_prod_1 not found error"));
                    showLogMessage("I", "", String.format("cont_no[%s] mcht_no[%s] kind[%s] prod_no[%s]"
                            , hContContractNo, hContMerchantNo, hContInstallmentKind
                            , hContProductNo));
                    /*** 若找不到產品檔且為新制分期，則視為異常結束 ***/
                    if (hContNewItFlag.equals("Y"))
                    	continue;
                       // comcr.errRtn("找不到產品檔且為新制分期 1", hContNewItFlag, hCallBatchSeqno);
                }
            }
            /*** 若收單註記為 C:NCCC ***/
            else if (hContInstallmentKind.equals("C") ) {
                /*** 由NCCC產品檔取得 分期利率 與 總費用年百分率 ***/
                if (selectBilProdNccc() != 0) {
                    //showLogMessage("I", "", "select bil_prod_nccc not found error\n");
                    //showLogMessage("I", "", String.format("cont_no[%s] mcht_no[%s] installment_kind[%s] prod_no[%s]", hContContractNo, hContMerchantNo, hContInstallmentKind, hContProductNo));
                    
                    hProdTransRate = 0;
                    hContYearFeesRate = 0;
                    
                 }
            } else if (hContInstallmentKind.equals("O")) {      /*** 若收單註記為 O:ONUS ***/
            
            	hProdTransRate = 0;
                hContYearFeesRate = 0;
                
        	} else {
                /*** 由分期產品檔取得 分期利率 與 總費用年百分率 ***/
        		if (hContTransRate != 0) {
        			hProdTransRate = hContTransRate;
        		} else {
        			if (selectBilProd() != 0) {
        				stderr = String.format("select bil_prod not found error\n");
        				showLogMessage("I", "", stderr);
        				stderr = String.format("cont_no[%s] mcht_no[%s] installment_kind[%s] prod_no[%s]", hContContractNo, hContMerchantNo, hContInstallmentKind, hContProductNo);
        				showLogMessage("I", "", stderr);
        				/*** 若找不到產品檔且為新制分期，則視為異常結束 ***/
        				if (hContNewItFlag.equals("Y"))
        					continue;
                        //comcr.errRtn("找不到產品檔且為新制分期 2", hContNewItFlag, hContContractNo);
        			}
                }
            }

            /*** 若為新制分期 ***/
            if (hContNewItFlag.equals("Y")) {
                hContYearFeesRate = 0;
                /*** 由0期計算至最後一期 ***/
                for (int1a = 0; int1a < hContInstallTotTerm; int1a++) {
                    /*** 取得每期金額*剩餘期數 ***/
                    doubleAmt = hContUnitPrice * (hContInstallTotTerm - int1a);
                    /*** 若為首期 則再加上首期餘數 ***/
                    if (int1a == 0)
                        doubleAmt = doubleAmt + hContFirstRemdAmt;
                    /*** 若為末期 則再加上尾數 ***/
                    if (int1a == hContInstallTotTerm - 1)
                        doubleAmt = doubleAmt + hContRemdAmt;
                    /*** 將剩餘總金額*分期產品年利率/12個月/100 ***/
                    longAmt1 = (long) (doubleAmt * hProdTransRate / 1200 + 0.5);

                    /*** 累計每期總利息 ***/
                    intAndFeeAmt = intAndFeeAmt + longAmt1;

                    /*** 加總當期分期金額與利息 ***/
                    doubleAmt = hContUnitPrice + longAmt1;

                    /*** 若為首期 則再加上首期餘數 ***/
                    if (int1a == 0)
                        doubleAmt = doubleAmt + hContFirstRemdAmt;

                    /*** 若為末期 則再加上尾數 ***/
                    if (int1a == hContInstallTotTerm - 1)
                        doubleAmt = doubleAmt + hContRemdAmt;

                    /*** 存入本金加利息 ***/
                    installPerAmt[int1a] = doubleAmt;
                }

                /*** 總費用 = 手續費 + 手續費尾數 + 總利息 ***/
                intAndFeeAmt = hContCltUnitPrice + hContCltRemdAmt + intAndFeeAmt;
                /*** 計算總費用年百分率 ***/
                calData();
            }
            if (hProdTransRate == 0)
                hContPaymentType = "I";
            else
                hContPaymentType = "E";
            /*** 更新總費用年百分率 ***/
            updateBilContract();
        }
        
        closeCursor(cursorIndex);

    }

    /***********************************************************************/
    int selectBilProd1() throws Exception {
        /*** 使用ptr_merchant_no ***/
        hProdTransRate = 0;
        hContYearFeesRate = 0;
        sqlCmd = "select trans_rate,";
        sqlCmd += "year_fees_rate ";
        sqlCmd += " from bil_prod  ";
        sqlCmd += "where mcht_no    = ?  ";
        sqlCmd += "  and product_no = ? ";
        sqlCmd += "  fetch first 1 rows only ";
        setString(1, hContPtrMerchantNo);
        setString(2, hContProductNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        if (recordCnt > 0) {
            hProdTransRate = getValueDouble("trans_rate");
            hContYearFeesRate = getValueDouble("year_fees_rate");
        }

        hContBillProdType = "S";
        return 0;
    }

    /**********************************************************************/
    int selectBilProdNccc() throws Exception {
        hProdTransRate = 0;
        hContYearFeesRate = 0;
        sqlCmd = "select trans_rate,";
        sqlCmd += "year_fees_rate ";
        sqlCmd += " from bil_prod_nccc  ";
        sqlCmd += "where mcht_no     = ?  ";
        sqlCmd += "  and product_no  = ?  ";
        sqlCmd += "  and start_date <= ?  ";
        sqlCmd += "  and end_date   >= ? ";
        setString(1, hContMerchantNo);
        setString(2, hContProductNo);
        setString(3, hContPurchaseDate);
        setString(4, hContPurchaseDate);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        if (recordCnt > 0) {
            hProdTransRate = getValueDouble("trans_rate");
            hContYearFeesRate = getValueDouble("year_fees_rate");
        }

        /*** 特店歸屬類別 N:NCCC ***/
        hContBillProdType = "N";
        return 0;
    }

    /**********************************************************************/
    int selectBilProd() throws Exception {
        hProdTransRate = 0;
        hContYearFeesRate = 0;
        sqlCmd = "select trans_rate,";
        sqlCmd += "year_fees_rate ";
        sqlCmd += " from bil_prod  ";
        sqlCmd += "where mcht_no = ?  ";
        sqlCmd += "  and product_no = ? ";
        setString(1, hContMerchantNo);
        setString(2, hContProductNo);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 1;
        }
        if (recordCnt > 0) {
            hProdTransRate = getValueDouble("trans_rate");
            hContYearFeesRate = getValueDouble("year_fees_rate");
        }

        hContBillProdType = "S";
        return 0;
    }

    /***********************************************************************/
    int calData() {
        double r0, aa, bb, rate1, cc;
        double r0Rate;
        int int1a, int1b;
        long preRate = 99999999, nowRate;

        r0Rate = r0RATE;
        r0 = (r0Rate / 100) / 12.0;
        
        /* debug
            stderr = String.format("分期總金額   = %.0f\n", hContTotAmt);
            showLogMessage("D", "", stderr);
            stderr = String.format("期數         = %d\n", hContInstallTotTerm);
            showLogMessage("D", "", stderr);
            stderr = String.format("利率         = %.2f\n", hProdTransRate);
            showLogMessage("D", "", stderr);
            stderr = String.format("手續費       = %.0f\n", hContCltRemdAmt + hContCltUnitPrice);
            showLogMessage("D", "", stderr);
            stderr = String.format("每期還款金額 = %.0f\n", hContUnitPrice);
            showLogMessage("D", "", stderr);
            stderr = String.format("首期還款餘數 = %.0f\n", hContFirstRemdAmt);
            showLogMessage("D", "", stderr);
            stderr = String.format("------------------------------------\n");
            showLogMessage("D", "", stderr);
            stderr = String.format("R0 = (%.2f/100)/12 = %f\n", r0Rate, r0);
            showLogMessage("D", "", stderr);
            stderr = String.format("SUM NPV(0) = %f\n", intAndFeeAmt);
            showLogMessage("D", "", stderr);
            stderr = String.format("----------------------------00000000\n");
            showLogMessage("D", "", stderr);
        */
        
        int1a = 0;
        while (true) {
            int1a++;
            aa = 0;
            
            /* debug
                stderr = String.format("  期數     本金加利息  / (1+R)指數  =     結    果\n");
                showLogMessage("D", "", stderr);
                stderr = String.format("--------------------------------------------------\n");
                showLogMessage("D", "", stderr);
            */
            
            for (int1b = 0; int1b < hContInstallTotTerm; int1b++) {
                rate1 = Math.pow(r0 + 1, int1b + 1);
                cc = installPerAmt[int1b] / rate1;
                aa = aa + cc;
                
                /* debug
                    stderr = String.format("   %02d  %14f %12f  %14f\n", int1b + 1, installPerAmt[int1b], rate1, cc);
                    showLogMessage("D", "", stderr);
                */
            }
            
            /* debug
                stderr = String.format("--------------------------------------------------\n");
                showLogMessage("D", "", stderr);
                stderr = String.format("                                        %f\n", aa);
                showLogMessage("D", "", stderr);
            */
            
            bb = aa + hContCltUnitPrice + hContCltRemdAmt - hContTotAmt;
            if (bb == 0)
                break;
            
            /* debug
                stderr = String.format("SUM NPV(%f) = %f\n", r0, bb);
                showLogMessage("D", "", stderr);
            */
            
            r0 = 0 + (intAndFeeAmt / (intAndFeeAmt - bb)) * r0;
            nowRate = (long) (r0 * 100000000.0 + 0.5);
            if (nowRate == preRate) {
                
            	/* debug
                    stderr = String.format("R%d = %.8f (精準度誤差值已在範圍內, 不再運算)\n\n", int1a, r0);
                    showLogMessage("D", "", stderr);
                */
            	
                break;
            }
            
            /* debug
                stderr = String.format("R%d = %.8f\n\n", int1a, r0);
                showLogMessage("D", "", stderr);
            */
            
            preRate = (long) (r0 * 100000000.0 + 0.5);
            if (int1a > 499)
                break;
        }
        nowRate = (long) (12.0 * r0 * 10000.0 + 0.5);
        hContYearFeesRate = nowRate / 100.0;

        if (hContYearFeesRate > hAgenRevolvingInterest1) {
            /*** 依金管銀（三）字第09700107080號函 以循環利率代替年百分率 ***/
            showLogMessage("I", "", String.format("合約編號  總費用年百分率 => (%2.2f)\n", hContYearFeesRate));
            showLogMessage("I", "", String.format("%s  %.2f\n", hContContractNo, hAgenRevolvingInterest1));
            hContYearFeesRate = hAgenRevolvingInterest1;
        }
        
        /* debug
            stderr = String.format("總費用年百分率 = 12 x %.8f(月) = %f(年) = %.2f%%\n", r0, r0 * 12, hContYearFeesRate);
            showLogMessage("D", "", stderr);
        */

        return 0;
    }

    /**********************************************************************/
    void updateBilContract() throws Exception {
    	
        daoTable   = "bil_contract";
        updateSQL  = " year_fees_rate  = ?,";
        updateSQL += " year_fees_date  = ?,";
        updateSQL += " trans_rate      = ?,";
        updateSQL += " payment_type    = ?,";
        updateSQL += " bill_prod_type  = ?,";
        updateSQL += " mod_time        = sysdate,";
        updateSQL += " mod_pgm         = decode(mod_pgm,'BilA031',mod_pgm,'CpsA302',mod_pgm,'BilD001') ";
        whereStr   = "where rowid      = ? ";
        setDouble(1, hContYearFeesRate);
        setString(2, hBusiBusinessDate);
        setDouble(3, hProdTransRate);
        setString(4, hContPaymentType);
        setString(5, hContBillProdType);
        setRowId(6, hContRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_bil_contract not found!", "", hContContractNo);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilD001 proc = new BilD001();
        int  retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
