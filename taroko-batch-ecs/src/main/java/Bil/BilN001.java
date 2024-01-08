/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/12/03  V1.00.01    shiyuqi       updated for project coding standard   * 
******************************************************************************/

package Bil;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*特店商品複製批次處理*/
public class BilN001 extends AccessDAO {
    private String progname = "特店商品複製批次處理 109/12/03  V1.00.01";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String hTempUser = "";
    int debug = 0;

    String prgmId = "BilN001";
    String prgmName = "特店商品複製批次處理";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hPrevBusinessDate = "";
    String hSystemDateF = "";
    String hCmasMchtNo = "";
    int hCmasSeqNo = 0;
    String hCmasRowid = "";
    String hMchtNoCpy = "";
    String hProdProductNo = "";
    String hProdProductName = "";
    String hProdMchtNo = "";
    String hProdUnitPrice = "";
    double hProdTotAmt = 0;
    String hProdTotTerm = "";
    double hProdRemdAmt = 0;
    String hProdExtraFees = "";
    double hProdFeesFixAmt = 0;
    double hProdFeesMinAmt = 0;
    double hProdFeesMaxAmt = 0;
    String hProdInterestRate = "";
    String hProdInterestMinRate = "";
    String hProdInterestMaxRate = "";
    String hProdAutoDelvFlag = "";
    String hProdAutoPrintFlag = "";
    String hProdAgainstNum = "";
    String hProdConfirmFlag = "";
    String hProdModUser = "";
    String hProdModTime = "";
    String hProdModPgm = "";
    double hProdModSeqno = 0;
    double hProdCltFeesFixAmt = 0;
    String hProdCltInterestRate = "";
    double hProdCltFeesMinAmt = 0;
    double hProdCltFeesMaxAmt = 0;
    String hProdInstallmentFlag = "";
    String hProdRefundFlag = "";
    String hProdDtlFlag = "";
    String hProdRowid = "";
    String hPrddMchtNo = "";
    String hPrddProductNo = "";
    String hPrddDtlKind = "";
    String hPrddDtlValue = "";
    String hPrddRowid = "";
    String hPnccProductNo = "";
    String hPnccProductName = "";
    String hPnccMchtNo = "";
    int hPnccSeqNo = 0;
    String hPnccStartDate = "";
    String hPnccEndDate = "";
    String hPnccLimitMin = "";
    String hPnccUnitPrice = "";
    double hPnccTotAmt = 0;
    String hPnccTotTerm = "";
    double hPnccRemdAmt = 0;
    String hPnccExtraFees = "";
    double hPnccFeesFixAmt = 0;
    double hPnccFeesMinAmt = 0;
    double hPnccFeesMaxAmt = 0;
    String hPnccInterestRate = "";
    String hPnccInterestMinRate = "";
    String hPnccInterestMaxRate = "";
    double hPnccCltFeesFixAmt = 0;
    String hPnccCltInterestRate = "";
    String hPnccAgainstNum = "";
    String hPnccDtlFlag = "";
    String hPnccConfirmFlag = "";
    String hPnccModUser = "";
    String hPnccModTime = "";
    String hPnccModPgm = "";
    double hPnccModSeqno = 0;
    String hPnccInstallmentFlag = "";
    String hPnccRowid = "";
    int tempInt = 0;
    String hPbinMchtNo = "";
    String hPbinProductNo = "";
    String hPbinSeqNo = "";
    String hPbinBinNo = "";
    String hPbinDtlKind = "";
    String hPbinDtlValue = "";
    String hPbinRowid = "";

    int totalCnt = 0;
    int partCnt = 0;
    int rtn = 0;
    int recCnt = 0;

    // ***********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : BilN001 batch_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            commonRtn();
            
            //增加debug的開關
            for (int argi=0; argi < args.length ; argi++ ) {
            	  if (args[argi].equals("debug")) {
            		  debug=1;
            	  }
              }

            showLogMessage("I", "", String.format("\nProcess User = [%s]\n", hTempUser));

            selectBilProdCopyMas();

            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        hBusiBusinessDate = "";
        hPrevBusinessDate = "";
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(to_date(business_date,'yyyymmdd')-1 days, 'yyyymmdd') h_prev_business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hBusiBusinessDate = getValue("business_date");
            hPrevBusinessDate = getValue("h_prev_business_date");
        } else {
            comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
        }

        hSystemDateF = "";
        sqlCmd = "select to_char(sysdate,'yyyymmddhh24miss') h_system_date_f ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_dual not found!", "", comcr.hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hSystemDateF = getValue("h_system_date_f");
        }
    }

    /***********************************************************************/
    void selectBilProdCopyMas() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "mcht_no,";
        sqlCmd += "seq_no,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_prod_copy_mas ";
        sqlCmd += "where 1 = 1 ";
        sqlCmd += "  and decode(confirm_flag,'','N',confirm_flag) in ('Y','y') ";
        sqlCmd += "  and copy_flag in ('N','n') ";
        extendField = "bil_prod_copy_mas.";
        int recordCnt = selectTable();
        if (debug == 1)
            showLogMessage("I", "", "888 select main =[" + recordCnt + "]");
        for (int i = 0; i < recordCnt; i++) {
            hCmasMchtNo = getValue("bil_prod_copy_mas.mcht_no", i);
            hCmasSeqNo = getValueInt("bil_prod_copy_mas.seq_no", i);
            hCmasRowid = getValue("bil_prod_copy_mas.rowid", i);

            totalCnt++;
            partCnt++;
            if (partCnt == 10000) {
                showLogMessage("I", "", String.format("Current cnt=[%d] ", totalCnt));
                partCnt = 0;
            }

            rtn = selectBilProdCopyDtl();

            if(debug == 1)
            	showLogMessage("I", "", "888 Proc mcht=["+ hCmasMchtNo +"]"+ totalCnt +",rtn="+rtn);

            if (rtn == 0) {
                recCnt++;
                daoTable   = "bil_prod_copy_mas";
                updateSQL  = " copy_flag  = 'Y', ";
                updateSQL += " mod_pgm    = ?  , ";
                updateSQL += " mod_time   = sysdate ";
                whereStr   = "where rowid = ? ";
                setString(1, prgmId);
                setRowId(2, hCmasRowid);
                updateTable();
                if (notFound.equals("Y")) {
                    String stderr = "update_bil_prod_copy_mas not found!";
                    comcr.errRtn(stderr, "", comcr.hCallBatchSeqno);
                }
            }
        }

    }

    /***********************************************************************/
    int selectBilProdCopyDtl() throws Exception {

        sqlCmd  = "select ";
        sqlCmd += "mcht_no_cpy,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_prod_copy_dtl ";
        sqlCmd += "where mcht_no = ? ";
        sqlCmd += "  and seq_no  = ? ";
        setString(1, hCmasMchtNo);
        setInt(2, hCmasSeqNo);
        int recordCnt = selectTable();
        
        if(debug == 1)
        	showLogMessage("I", "", "  888 Dtl cnt=[" + hCmasMchtNo + "],seq="+ hCmasSeqNo);
        
        for (int i = 0; i < recordCnt; i++) {
            hMchtNoCpy = getValue("mcht_no_cpy", i);

            if(debug == 1) showLogMessage("I", "", "   888 Dtl mcht=[" + hMchtNoCpy + "]");

            copyBilProdRtn();
            copyBilProdDtlRtn();
            copyBilProdNcccRtn();
        }

        return 0;
    }

    /***********************************************************************/
    void copyBilProdRtn() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "product_no,";
        sqlCmd += "product_name,";
        sqlCmd += "mcht_no,";
        sqlCmd += "unit_price,";
        sqlCmd += "tot_amt,";
        sqlCmd += "tot_term,";
        sqlCmd += "remd_amt,";
        sqlCmd += "extra_fees,";
        sqlCmd += "fees_fix_amt,";
        sqlCmd += "fees_min_amt,";
        sqlCmd += "fees_max_amt,";
        sqlCmd += "interest_rate,";
        sqlCmd += "interest_min_rate,";
        sqlCmd += "interest_max_rate,";
        sqlCmd += "auto_delv_flag,";
        sqlCmd += "auto_print_flag,";
        sqlCmd += "against_num,";
        sqlCmd += "confirm_flag,";
        sqlCmd += "mod_user,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_seqno,";
        sqlCmd += "clt_fees_fix_amt,";
        sqlCmd += "clt_interest_rate,";
        sqlCmd += "clt_fees_min_amt,";
        sqlCmd += "clt_fees_max_amt,";
        sqlCmd += "installment_flag,";
        sqlCmd += "refund_flag,";
        sqlCmd += "dtl_flag,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_prod ";
        sqlCmd += "where mcht_no = ? ";
        setString(1, hCmasMchtNo);

        extendField = "bil_prod.";

        int recordCnt = selectTable();
        
        if(debug == 1)
        	showLogMessage("I","","   888 copy bil_prod=["+ hCmasMchtNo +"]"+recordCnt);
        
        for (int i = 0; i < recordCnt; i++) {
            hProdProductNo = getValue("bil_prod.product_no", i);
            hProdProductName = getValue("bil_prod.product_name", i);
            hProdMchtNo = getValue("bil_prod.mcht_no", i);
            hProdUnitPrice = getValue("bil_prod.unit_price", i);
            hProdTotAmt = getValueDouble("bil_prod.tot_amt", i);
            hProdTotTerm = getValue("bil_prod.tot_term", i);
            hProdRemdAmt = getValueDouble("bil_prod.remd_amt", i);
            hProdExtraFees = getValue("bil_prod.extra_fees", i);
            hProdFeesFixAmt = getValueDouble("bil_prod.fees_fix_amt", i);
            hProdFeesMinAmt = getValueDouble("bil_prod.fees_min_amt", i);
            hProdFeesMaxAmt = getValueDouble("bil_prod.fees_max_amt", i);
            hProdInterestRate = getValue("bil_prod.interest_rate", i);
            hProdInterestMinRate = getValue("bil_prod.interest_min_rate", i);
            hProdInterestMaxRate = getValue("bil_prod.interest_max_rate", i);
            hProdAutoDelvFlag = getValue("bil_prod.auto_delv_flag", i);
            hProdAutoPrintFlag = getValue("bil_prod.auto_print_flag", i);
            hProdAgainstNum = getValue("bil_prod.against_num", i);
            hProdConfirmFlag = getValue("bil_prod.confirm_flag", i);
            hProdModUser = getValue("bil_prod.mod_user", i);
            hProdModTime = getValue("bil_prod.mod_time", i);
            hProdModPgm = getValue("bil_prod.mod_pgm", i);
            hProdModSeqno = getValueDouble("bil_prod.mod_seqno", i);
            hProdCltFeesFixAmt = getValueDouble("bil_prod.clt_fees_fix_amt", i);
            hProdCltInterestRate = getValue("bil_prod.clt_interest_rate", i);
            hProdCltFeesMinAmt = getValueDouble("bil_prod.clt_fees_min_amt", i);
            hProdCltFeesMaxAmt = getValueDouble("bil_prod.clt_fees_max_amt", i);
            hProdInstallmentFlag = getValue("bil_prod.installment_flag", i);
            hProdRefundFlag = getValue("bil_prod.refund_flag", i);
            hProdDtlFlag = getValue("bil_prod.dtl_flag", i);
            hProdRowid = getValue("bil_prod.rowid", i);

            hProdMchtNo = hMchtNoCpy;
            setValue("product_no"            , hProdProductNo);
            setValue("product_name"          , hProdProductName);
            setValue("mcht_no"               , hProdMchtNo);
            setValue("unit_price"            , hProdUnitPrice);
            setValueDouble("tot_amt"         , hProdTotAmt);
            setValue("tot_term"              , hProdTotTerm);
            setValueDouble("remd_amt"        , hProdRemdAmt);
            setValue("extra_fees"            , hProdExtraFees);
            setValueDouble("fees_fix_amt"    , hProdFeesFixAmt);
            setValueDouble("fees_min_amt"    , hProdFeesMinAmt);
            setValueDouble("fees_max_amt"    , hProdFeesMaxAmt);
            setValue("interest_rate"         , hProdInterestRate);
            setValue("interest_min_rate"     , hProdInterestMinRate);
            setValue("interest_max_rate"     , hProdInterestMaxRate);
            setValue("auto_delv_flag"        , hProdAutoDelvFlag);
            setValue("auto_print_flag"       , hProdAutoPrintFlag);
            setValue("against_num"           , hProdAgainstNum);
            setValue("confirm_flag"          , hProdConfirmFlag);
            setValue("mod_user"              , "BATCH");
            setValue("mod_time"              , sysDate + sysTime);
            setValue("mod_pgm"               , prgmId);
            setValueDouble("mod_seqno"       , hProdModSeqno);
            setValueDouble("clt_fees_fix_amt", hProdCltFeesFixAmt);
            setValue("clt_interest_rate"     , hProdCltInterestRate);
            setValueDouble("clt_fees_min_amt", hProdCltFeesMinAmt);
            setValueDouble("clt_fees_max_amt", hProdCltFeesMaxAmt);
            setValue("installment_flag"      , hProdInstallmentFlag);
            setValue("refund_flag"           , hProdRefundFlag);
            setValue("dtl_flag"              , hProdDtlFlag);
            daoTable = "bil_prod";
            insertTable();
        }

    }

    /***********************************************************************/
    void copyBilProdDtlRtn() throws Exception {

        sqlCmd = "select ";
        sqlCmd += "mcht_no,";
        sqlCmd += "product_no,";
        sqlCmd += "dtl_kind,";
        sqlCmd += "dtl_value,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_prod_dtl ";
        sqlCmd += "where mcht_no = ? ";
        setString(1, hCmasMchtNo);

        extendField = "bil_prod_dtl.";

        int recordCnt = selectTable();
        
        if(debug == 1)
        	showLogMessage("I","","   888 copy bil_prod_dtl=["+ hCmasMchtNo +"]"+recordCnt);
        
        for (int i = 0; i < recordCnt; i++) {
            hPrddMchtNo = getValue("bil_prod_dtl.mcht_no", i);
            hPrddProductNo = getValue("bil_prod_dtl.product_no", i);
            hPrddDtlKind = getValue("bil_prod_dtl.dtl_kind", i);
            hPrddDtlValue = getValue("bil_prod_dtl.dtl_value", i);
            hPrddRowid = getValue("bil_prod_dtl.rowid", i);
            hPrddMchtNo = hMchtNoCpy;

            setValue("mcht_no"   , hPrddMchtNo);
            setValue("product_no", hPrddProductNo);
            setValue("dtl_kind"  , hPrddDtlKind);
            setValue("dtl_value" , hPrddDtlValue);
            daoTable = "bil_prod_dtl";
            insertTable();
        }

    }

    /**********************************************************************/
    void copyBilProdNcccRtn() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "product_no,";
        sqlCmd += "product_name,";
        sqlCmd += "mcht_no,";
        sqlCmd += "seq_no,";
        sqlCmd += "start_date,";
        sqlCmd += "end_date,";
        sqlCmd += "limit_min,";
        sqlCmd += "unit_price,";
        sqlCmd += "tot_amt,";
        sqlCmd += "tot_term,";
        sqlCmd += "remd_amt,";
        sqlCmd += "extra_fees,";
        sqlCmd += "fees_fix_amt,";
        sqlCmd += "fees_min_amt,";
        sqlCmd += "fees_max_amt,";
        sqlCmd += "interest_rate,";
        sqlCmd += "interest_min_rate,";
        sqlCmd += "interest_max_rate,";
        sqlCmd += "clt_fees_fix_amt,";
        sqlCmd += "clt_interest_rate,";
        sqlCmd += "against_num,";
        sqlCmd += "dtl_flag,";
        sqlCmd += "confirm_flag,";
        sqlCmd += "mod_user,";
        sqlCmd += "mod_time,";
        sqlCmd += "mod_pgm,";
        sqlCmd += "mod_seqno,";
        sqlCmd += "installment_flag,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_prod_nccc ";
        sqlCmd += "where mcht_no  = ? ";
        sqlCmd += "  and ? < decode(end_date,'','29991231',end_date) ";
        setString(1, hCmasMchtNo);
        setString(2, hBusiBusinessDate);

        extendField = "bil_prod_nccc.";

        int recordCnt = selectTable();
        
        if(debug == 1)
        	showLogMessage("I","","   888 copy bil_prod_nccc=["+ hCmasMchtNo + "]"+recordCnt);
        
        for (int i = 0; i < recordCnt; i++) {
            hPnccProductNo = getValue("bil_prod_nccc.product_no", i);
            hPnccProductName = getValue("bil_prod_nccc.product_name", i);
            hPnccMchtNo = getValue("bil_prod_nccc.mcht_no", i);
            hPnccSeqNo = getValueInt("bil_prod_nccc.seq_no", i);
            hPnccStartDate = getValue("bil_prod_nccc.start_date", i);
            hPnccEndDate = getValue("bil_prod_nccc.end_date", i);
            hPnccLimitMin = getValue("bil_prod_nccc.limit_min", i);
            hPnccUnitPrice = getValue("bil_prod_nccc.unit_price", i);
            hPnccTotAmt = getValueDouble("bil_prod_nccc.tot_amt", i);
            hPnccTotTerm = getValue("bil_prod_nccc.tot_term", i);
            hPnccRemdAmt = getValueDouble("bil_prod_nccc.remd_amt", i);
            hPnccExtraFees = getValue("bil_prod_nccc.extra_fees", i);
            hPnccFeesFixAmt = getValueDouble("bil_prod_nccc.fees_fix_amt", i);
            hPnccFeesMinAmt = getValueDouble("bil_prod_nccc.fees_min_amt", i);
            hPnccFeesMaxAmt = getValueDouble("bil_prod_nccc.fees_max_amt", i);
            hPnccInterestRate = getValue("bil_prod_nccc.interest_rate", i);
            hPnccInterestMinRate = getValue("bil_prod_nccc.interest_min_rate", i);
            hPnccInterestMaxRate = getValue("bil_prod_nccc.interest_max_rate", i);
            hPnccCltFeesFixAmt = getValueDouble("bil_prod_nccc.clt_fees_fix_amt", i);
            hPnccCltInterestRate = getValue("bil_prod_nccc.clt_interest_rate", i);
            hPnccAgainstNum = getValue("bil_prod_nccc.against_num", i);
            hPnccDtlFlag = getValue("bil_prod_nccc.dtl_flag", i);
            hPnccConfirmFlag = getValue("bil_prod_nccc.confirm_flag", i);
            hPnccModUser = getValue("bil_prod_nccc.mod_user", i);
            hPnccModTime = getValue("bil_prod_nccc.mod_time", i);
            hPnccModPgm = getValue("bil_prod_nccc.mod_pgm", i);
            hPnccModSeqno = getValueDouble("bil_prod_nccc.mod_seqno", i);
            hPnccInstallmentFlag = getValue("bil_prod_nccc.installment_flag", i);
            hPnccRowid = getValue("bil_prod_nccc.rowid", i);
            hPnccMchtNo = hMchtNoCpy;

            tempInt = 0;
            sqlCmd = "select count(*) temp_int ";
            sqlCmd += " from bil_prod_nccc  ";
            sqlCmd += "where mcht_no    = ?  ";
            sqlCmd += "  and product_no = ?  ";
            sqlCmd += "  and ((? between start_date and end_date) or  ";
            sqlCmd += "       (? between start_date and end_date) ) ";
            setString(1, hPnccMchtNo);
            setString(2, hPnccProductNo);
            setString(3, hPnccStartDate);
            setString(4, hPnccEndDate);
            int recordCnt1 = selectTable();
            if (notFound.equals("Y")) {
                comcr.errRtn("select_bil_prod_nccc not found!", "", comcr.hCallBatchSeqno);
            }
            if (recordCnt1 > 0) {
                tempInt = getValueInt("temp_int");
            }
            
            if(debug == 1)
            	showLogMessage("I","","    999 copy=["+ hPnccMchtNo +","+ hPnccProductNo +"]"+ hPnccStartDate +","+ hPnccEndDate +","+ tempInt);
            
            if (tempInt < 1) {
                setValue("product_no", hPnccProductNo);
                setValue("product_name", hPnccProductName);
                setValue("mcht_no", hPnccMchtNo);
                setValueInt("seq_no", hPnccSeqNo);
                setValue("start_date", hPnccStartDate);
                setValue("end_date", hPnccEndDate);
                setValue("limit_min", hPnccLimitMin);
                setValue("unit_price", hPnccUnitPrice);
                setValueDouble("tot_amt", hPnccTotAmt);
                setValue("tot_term", hPnccTotTerm);
                setValueDouble("remd_amt", hPnccRemdAmt);
                setValue("extra_fees", hPnccExtraFees);
                setValueDouble("fees_fix_amt", hPnccFeesFixAmt);
                setValueDouble("fees_min_amt", hPnccFeesMinAmt);
                setValueDouble("fees_max_amt", hPnccFeesMaxAmt);
                setValue("interest_rate", hPnccInterestRate);
                setValue("interest_min_rate", hPnccInterestMinRate);
                setValue("interest_max_rate", hPnccInterestMaxRate);
                setValueDouble("clt_fees_fix_amt", hPnccCltFeesFixAmt);
                setValue("clt_interest_rate", hPnccCltInterestRate);
                setValue("against_num", hPnccAgainstNum);
                setValue("dtl_flag", hPnccDtlFlag);
                setValue("confirm_flag", hPnccConfirmFlag);
                setValue("mod_user", "BATCH");
                setValue("mod_time", sysDate + sysTime);
                setValue("mod_pgm", prgmId);
                setValueDouble("mod_seqno", hPnccModSeqno);
                setValue("installment_flag", hPnccInstallmentFlag);
                daoTable = "bil_prod_nccc";
                insertTable();

                copyBilProdNcccBinRtn();
            }

        }

    }

    /***********************************************************************/
    void copyBilProdNcccBinRtn() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "mcht_no,";
        sqlCmd += "product_no,";
        sqlCmd += "seq_no,";
        sqlCmd += "bin_no,";
        sqlCmd += "dtl_kind,";
        sqlCmd += "dtl_value,";
        sqlCmd += "rowid  as rowid ";
        sqlCmd += " from bil_prod_nccc_bin ";
        sqlCmd += "where mcht_no    = ? ";
        sqlCmd += "  and seq_no     = ? ";
        sqlCmd += "  and product_no = ? ";
        setString(1, hCmasMchtNo);
        setInt(2, hPnccSeqNo);
        setString(3, hPnccProductNo);

        extendField = "bil_prod_nccc_bin.";

        int recordCnt = selectTable();
        
        if(debug == 1)
        	showLogMessage("I","","   888 copy bil_prod_nccc_bin=["+ hCmasMchtNo +"]"+recordCnt);
        
        for (int i = 0; i < recordCnt; i++) {
            hPbinMchtNo = getValue("bil_prod_nccc_bin.mcht_no", i);
            hPbinProductNo = getValue("bil_prod_nccc_bin.product_no", i);
            hPbinSeqNo = getValue("bil_prod_nccc_bin.seq_no", i);
            hPbinBinNo = getValue("bil_prod_nccc_bin.bin_no", i);
            hPbinDtlKind = getValue("bil_prod_nccc_bin.dtl_kind", i);
            hPbinDtlValue = getValue("bil_prod_nccc_bin.dtl_value", i);
            hPbinRowid = getValue("bil_prod_nccc_bin.rowid", i);

            hPbinMchtNo = hMchtNoCpy;
            setValue("mcht_no"   , hPbinMchtNo);
            setValue("product_no", hPbinProductNo);
            setValue("seq_no"    , hPbinSeqNo);
            setValue("bin_no"    , hPbinBinNo);
            setValue("dtl_kind"  , hPbinDtlKind);
            setValue("dtl_value" , hPbinDtlValue);
            daoTable = "bil_prod_nccc_bin";
            
            showLogMessage("I","","    999 bin copy=["+ hPbinMchtNo +","+ hPbinProductNo +"]");
            
            insertTable();
        }
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilN001 proc = new BilN001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
