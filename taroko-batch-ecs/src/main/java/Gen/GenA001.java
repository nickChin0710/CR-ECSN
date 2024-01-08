/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Lai       program initial                           *
*  110/09/06  V1.01.01  Lai         M:8603  mod_log  not in ('D')             *
*                                                                             *
*  post_flag : 未日結  jrn_status != '3' 未覆核                               * 
*  jrn_status  not in ('1')       非刪除                                      *
*  mod_log     not in ('D','U')   未刪除,修改                                 *
*  109/11/19  V1.00.02  yanghan       修改了變量名稱和方法名稱                                                                            *
*  109/12/24  V1.00.03  yanghan       修改了變量名稱和方法名稱            *
*  111/11/15  V1.00.04  Zuwei       sync from mega                             *
*  111/11/17  V1.00.05  Zuwei       核查科目是否存在或平衡獨立成function
*  112/12/11  V1.00.06  Zuwei Su    errExit改為 show message & exit program  *  
*  112/12/19  V1.00.07  Zuwei Su    errRtn改為 show message & return 1  *  
******************************************************************************/

package Gen;

import com.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
/* 日結檢核作業 */
public class GenA001 extends AccessDAO {
    private final String PROGNAME = "日結檢核作業  111/11/17  V1.00.06";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    final int DEBUG = 1;
    String hTempUser = "";

    String blk = "　";
    String rtpName1 = "會計分錄日結檢核表";
    String rptIdR1 = "GEN_A001R1";
    String rtpName2 = "檢核明細表";
    String rptIdR2 = "GEN_A001R2";
    String tempStr = "";

    String sql = "";
    int actCnt = 0;
    Map<String, Object> parm = null;
    List<Map<String, Object>> mrs = null;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();
    String hCallBatchSeqno = "";
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq = 0;
    int rptSeq1 = 0;
    int errCnt = 0;
    String errMsg = "";

    int tempInt;
    String hBusinessDate;
    double hAmtDr;
    double hAmtCr;
    String hVouchDate;
    String hSystemDate;
    String hSystemDatef;
    String hAllCurr;
    String hTempCurr;
    String hVoucTxDate;
    String hVoucRefno;
    String hVoucMemo1;
    String hVoucMemo2;
    String hVoucMemo3;
    String hVoucAcNo;
    String hVoucCurr;
    String hVoucRowid;
    int tempCount;
    String hEngName;
    long hModSeqno;
    String hErrorCode;
    String hErrorDesc;
    String hPrintName;
    String hRptName;
    String fixDate;
    String fixTime;
    int totCnt = 0;
    int totr = 0;
    double totAmtDr = 0;
    double totAmtCr = 0;
    protected String hModUser;
    protected String hModTime;
    protected int cntCurr;
    protected int pageCnt = 0;
    protected int lineCnt = 0;
    protected String hCallErrorDesc = "";

    public static void main(String[] args) throws Exception {
        GenA001 proc = new GenA001();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ***************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            fixDate = sysDate;
            fixTime = sysTime;
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (args.length > 2) {
                String err1 = "Usage : GenA001 [date] batch_seqno";
//                comc.errExit(err1, "");
                showLogMessage("I", "", err1);
                return 0;
            }

            // 固定要做的

            if (!connectDataBase()) {
//                comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error");
                return 1;
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                comcr.hCallBatchSeqno = "no-call";
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

            // 營業日
            commonRtn();
            if (args.length > 0) {
                if (args[0].length() == 8) {
                    hBusinessDate = args[0];
                }
            }

            showLogMessage("I", "", "處理日期=[" + hBusinessDate + "]" + args.length);

            // ================================================================
            tempInt = 0;
            selectSQL = "count(*) temp_int";
            daoTable = "ptr_holiday";
            whereStr = "where holiday = ?";
            setString(1, hBusinessDate);

            int recordCnt = selectTable();

            tempInt = recordCnt > 0 ? getValueInt("temp_int") : 0;

            if (tempInt > 0) {
                String err1 = "錯誤: 營業日為假日!!";
//                comcr.errRtn(err1, "", comcr.hCallBatchSeqno);
                showLogMessage("I", "", err1);
                return 1;
            }

            // ================================================================

            selectGenVouchDc();
//            selectAllCurrCode();

            // print_head();

            selectGenVouch(); // 借貸未平衡

            // ================================================================
//            checkAcNoExists("like", "148199");
            // ================================================================
//            checkAcNoExists("like", "248199");
            // ================================================================
//          checkAcNoExists("=", "60000000");
            // ================================================================
//            checkAcNoExists("=", "60000100");
            // ================================================================
//          checkAcNoExists("=", "60000200");
            // ================================================================
//          checkAcNoExists("=", "60000300");
            // ================================================================
//          checkAcNoExists("=", "60000500");

            if(pageCnt > 0 ) {
               lpar1.add(comcr.putReport(rptIdR1, rtpName1, fixDate + fixTime, rptSeq++, "1", "##PPP"));
            }
            
            actCnt = comcr.deletePtrBatchRpt(rptIdR1, fixDate);
            comcr.insertPtrBatchRpt(lpar1);
            String filename = comc.getECSHOME() + "/reports/GEN_A001R1";
            comc.writeReportForTest(filename, lpar1); // for test

            if (errCnt > 0) {
                String err1 = String.format("%s%s", "產生錯誤報表:", "GEN_A001R1 及 R2");
                comcr.hCallErrorDesc = String.format("%s%s", "產生錯誤報表:", "GEN_A001R1 及 R2");
                showLogMessage("I", "", comcr.hCallErrorDesc);
                rollbackDataBase();

                if (comcr.hCallBatchSeqno.length() == 20)
                    try {
                        comcr.callbatch(1, 1001, 1);
                    } catch (Exception e) {
//                        comcr.errRtn(err1, "", comcr.hCallBatchSeqno);
                        showLogMessage("I", "", err1);
                        exitProgram(1);
                    }
                actCnt = comcr.deletePtrBatchRpt(rptIdR1, fixDate);
                comcr.insertPtrBatchRpt(lpar1);
                comcr.insertPtrBatchRpt(lpar2);
                commitDataBase();
                return 0;
            } else {
                if (updatePtrBusinday() < 0) {
                    String err1 = "update_ptr_businday錯誤";
//                    comcr.errRtn(err1, "", comcr.hCallBatchSeqno);
                    showLogMessage("I", "", err1);
                    return 1;
                }
                errCode = "OK";
                errDesc = "處理無誤 !! OK !!";
            }

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /**
     * 核查科目是否存在或平衡
     * @throws Exception
     */
	private void checkAcNoExists(String op, String acNo) throws Exception {
		int recordCnt;
		hAmtDr = 0;
		hAmtCr = 0;
		selectSQL = "sum(decode(dbcr,'D',amt,0)) h_amt_dr, " + "sum(decode(dbcr,'C',amt,0)) h_amt_cr ";
		daoTable = "gen_vouch";
		whereStr = "where (case when post_flag = '' then 'N' else post_flag end) in ('N','n')  "
		        + "  and decode(mod_log  ,'','0',mod_log) not in ('D','U') " + "  and jrn_status in ('3')   "
		        + "  and tx_date    <= ? ";
		switch (op) {
		case "=":
            whereStr += "  and ac_no        = '" + acNo + "' ";
			break;
		case "like":
			whereStr += "  and ac_no      like '" + acNo + "%' ";
			break;

		default:
			throw new RuntimeException("不支持的operation");
		}
		setString(1, hBusinessDate);

		recordCnt = selectTable();

		if (recordCnt > 0) {
		    hAmtDr = getValueDouble("h_amt_dr");
		    hAmtCr = getValueDouble("h_amt_cr");
		}

		switch (op) {
		case "=":
            if (hAmtCr != hAmtDr) {
                String msg = "科目-" + acNo + " 未平衡";
                showLogMessage("I", "", msg);
                printErr(msg, "");
            }
			break;
		case "like":
			if (hAmtCr > 0 || hAmtDr > 0) {
			    String msg = "科目-" + acNo + "xx 存在";
			    showLogMessage("I", "", msg);
			    printErr(msg, "");
			}
			break;

		default:
			throw new RuntimeException("不支持的operation");
		}
	}
    // ******************************************************************************
    void chkGenVouch() throws Exception {
        tempCount = 0;

        selectSQL = "count(*) temp_count";
        daoTable = "gen_vouch";
        whereStr = "where tx_date  = ? " + "  and refno    = ? "
                + "  and (case when apr_user  ='' then '0' else apr_user   end)  = '0' "
                + "  and (case when post_flag ='' then 'N' else post_flag  end) in ('N','n') "
                + "  and (case when jrn_status='' then '0' else jrn_status end) not in ('3','1') ";
        setString(1, hVoucTxDate);
        setString(2, hVoucRefno);

        int recordCnt = selectTable();

        if (notFound.equals("Y"))
            return;
        else {
            int tcnt = getValueInt("temp_count");
            if (tcnt > 0) {
                String msg = "主管未覆核";
                showLogMessage("I", "", msg);
                printErr(msg, hVoucRefno);
            }
        }
        return;
    }
    // *******************************************************************************
    void selectGenVouch() throws Exception {
        selectSQL = "tx_date, " + "refno, " + "sum(decode(dbcr,'D',amt,0)) h_amt_dr, "
                  + "sum(decode(dbcr,'C',amt,0)) h_amt_cr";
        daoTable  = "gen_vouch";
        whereStr  = "where (case when post_flag ='' then 'N' else post_flag  end)     in ('N','n') "
                  + "  and (case when jrn_status='' then 'N' else jrn_status end) not in ('1')  " 
                  + "  and tx_date  <= ? "
                  + "  and decode(mod_log  ,'','0',mod_log) not in ('D','U') " 
                  + "group by tx_date,refno ";
        setString(1, hBusinessDate);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            showLogMessage("I", "", "select_gen_vouch not found 1 = "+hBusinessDate);
            return;
        }

        for (int i = 0; i < recordCnt; i++) {
            totCnt++;

            hVoucTxDate = getValue("tx_date", i);
            hVoucRefno   = getValue("refno", i);
            hAmtDr       = getValueDouble("h_amt_dr", i);
            hAmtCr       = getValueDouble("h_amt_cr", i);

            if (hAmtDr != hAmtCr) {
                String msg = "借貸未平衡";
                showLogMessage("I", "", msg);
                printErr(msg, "");
            }

            chkGenVouch();
        }
    }

    // *******************************************************************************
    void selectGenVouchDc() throws Exception {
        selectSQL = "refno, " + "curr_code_gl, " + "a.rowid rowid";
        daoTable  = "gen_vouch a ";
        daoTable += "full outer join ptr_currcode b on b.curr_code =  ";
        daoTable += "           case when curr_code_dc = '' then '901' else curr_code_dc end";
        whereStr  = "where (CASE WHEN post_flag ='' THEN 'N' ELSE post_flag  END)     in ('N','n') "
                  + "  and (CASE WHEN jrn_status='' THEN 'N' ELSE jrn_status END) not in ('1') " 
                  + "  and tx_date   <= ? "
                  + "  and curr       = '' ";
        setString(1, hBusinessDate);

        int recordCnt = selectTable();

        for (int i = 0; i < recordCnt; i++) {
            updateSQL = "curr = ?";
            daoTable = "gen_vouch";
            whereStr = "where rowid = ?";

            setString(1, getValue("curr_code_gl", i));
            setRowId(2, getValue("rowid", i));

            int recCnt = updateTable();
            if (notFound.equals("Y")) {
//                comcr.errRtn("update1 " + daoTable, " error[not foundnd]", comcr.hCallBatchSeqno);
                showLogMessage("I", "", "update1 " + daoTable + " error[not foundnd]");
                exitProgram(1);
            }
        }
    }
    // *******************************************************************************
    void selectGenVouchr(String curr) throws Exception {
        String buf = "";
        hTempCurr = curr;
        totAmtDr = 0;
        totAmtCr = 0;

        totr = 0;
        selectSQL = "tx_date,refno,memo1,memo2,memo3,ac_no," + "decode(dbcr,'D',amt,0) h_amt_dr,"
                  + "decode(dbcr,'C',amt,0) h_amt_cr ";
        daoTable  = "gen_vouch";
        whereStr  = "where (case when post_flag='' then 'N' else post_flag  end)  in ('N','n')  "
                  + "  and decode(mod_log  ,'','0',mod_log) not in ('D','U') " 
                  + "  and jrn_status  in ('3')   "
                  + "  and tx_date     <= ? " 
                  + "  and (case when curr = '' then '00' else curr  end) = ?"
                  + "  and (ac_no = '60000300' or ac_no like '148199%' or ac_no like '248199%') ";
        setString(1, hBusinessDate);
        setString(2, hTempCurr);

        int recordCnt = selectTable();

        for (int j = 0; j < recordCnt; j++) {
            totr++;
            buf = "";
            buf = comcr.insertStr(buf, comcr.formatDate(getValue("tx_date", j), 1), 1);
            buf = comcr.insertStr(buf, getValue("refno", j), 10);
            tempStr = String.format("%14.14s",
                    comcr.formatNumber(String.valueOf(getValueDouble("h_amt_dr", j)) + "", 1, 2));
            buf = comcr.insertStr(buf, tempStr, 16);
            tempStr = String.format("%14.14s",
                    comcr.formatNumber(String.valueOf(getValueDouble("h_amt_cr", j)) + "", 1, 2));
            buf = comcr.insertStr(buf, tempStr, 30);
            buf = comcr.insertStr(buf, getValue("memo1", j), 45);
            buf = comcr.insertStr(buf, getValue("memo2", j), 63);
            buf = comcr.insertStr(buf, getValue("ac_no", j), 82);
            lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));
            totAmtDr += getValueDouble("h_amt_dr", j);
            totAmtCr += getValueDouble("h_amt_cr", j);
        }
    }
    // *******************************************************************************
    void selectAllCurrCode() throws Exception {
        selectSQL = "(case when curr='' then '00' else curr end) h_all_curr";
        daoTable = "gen_vouch";
        whereStr = "where (case when post_flag='' then 'N' else post_flag end)  in ('N','n')  "
                + "  and decode(mod_log  ,'','0',mod_log) not in ('D','U') " + "  and jrn_status  in ('3')   "
                + "  and tx_date     <= ? "
                + "  and (ac_no = '60000300' or ac_no like '148199%' or ac_no like '248199%' ) "
                + "group by (case when curr='' then '00' else curr end) ";
        setString(1, hBusinessDate);

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
        }

        int page = 0;
        String buf = "";

        for (int i = 0; i < recordCnt; i++) {
            buf = "";
            buf = comcr.insertStr(buf, "報表名稱: GEN_A001R2", 1);
            buf = comcr.insertStr(buf, "檢核明細表", 29);
            buf = comcr.insertStr(buf, "頁    次:", 62);
            buf = comcr.insertStr(buf, ++page + "", 72);
            lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));

            buf = "";
            buf = comcr.insertStr(buf, "印表日期:", 1);
            buf = comcr.insertStr(buf, comcr.formatDate(chinDate, 2), 11);
            buf = comcr.insertStr(buf, "啟帳日期:", 62);
            buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 2), 72);
            lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));

            // =====================================================

            String curr = getValue("h_all_curr", i);

            if (DEBUG == 1)
                showLogMessage("I", "", "  888 read curr=[" + curr + "]");
            selectSQL = "curr_eng_name";
            daoTable = "ptr_currcode";
            whereStr = "where curr_code_gl = ?";
            setString(1, curr);

            selectTable();

            String curremn = notFound.equals("Y") ? "" : getValue("curr_eng_name");
            if (notFound.equals("Y")) {
//                comcr.errRtn("select_ptr_currcode  not found", curr, comcr.hCallBatchSeqno);
                showLogMessage("I", "", "select_ptr_currcode  not found");
                exitProgram(1);
            }

            buf = "";
            buf = comcr.insertStr(buf, "幣  別  :", 1);
            buf = comcr.insertStr(buf, curr, 11);
            buf = comcr.insertStr(buf, curremn, 15);
            lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));

            buf = "日    期 套  號 借  方  金 額 貸  方  金 額 摘要一            摘要二             會計科目";
            lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));
            buf = "======== ====== ============= ============= ================= ================== ========";
            lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));

            selectGenVouchr(curr);

            if (totr > 0) {
                buf = "===========================================";
                lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));

                buf = "";
                buf = comcr.insertStr(buf, "合 計 :", 2);
                buf = comcr.insertStr(buf, comcr.formatNumber(totr + "", 1, 0), 11);
                tempStr = String.format("%14.14s", comcr.formatNumber(totAmtDr + "", 1, 2));
                buf = comcr.insertStr(buf, tempStr, 16);
                tempStr = String.format("%14.14s", comcr.formatNumber(totAmtCr + "", 1, 2));
                buf = comcr.insertStr(buf, tempStr, 30);
                lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));
                buf = " 備 註 : 此份明細表僅列示 60000300臨時存欠 及 14819900應收款待整理款項 及 24819900 應付款待整理款項";
                lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", buf));
                lpar2.add(comcr.putReport(rptIdR2, rtpName2, fixDate + fixTime, rptSeq1++, "0", "\f"));
            }
        }

        // ====================================================

        actCnt = comcr.deletePtrBatchRpt(rptIdR2, fixDate);
        // actCnt = comcr.insert_ptr_batch_rpt(lpar1);
        String filename = comc.getECSHOME() + "/reports/GEN_A001R2";
        comc.writeReportForTest(filename, lpar2); // for test

    }

    // *******************************************************************************
    private int updatePtrBusinday() {
        try {
            updateSQL = "VOUCH_CHK_FLAG = 'Y'";
            daoTable = "ptr_businday";

            updateTable();
            if (notFound.equals("Y")) {
                showLogMessage("I", "", "update ptr_businday error[not foundnd]");
                return -1;
            }
        } catch (Exception ex) {
            showLogMessage("I", "", "update_ptr_businday error : " + ex.getMessage());
            return -1;
        }

        return 0;
    }

    // *********************************************************************************
    void commonRtn() throws Exception {
        selectSQL = "business_date,vouch_date ";
        daoTable  = "ptr_businday";

        if (selectTable() > 0) {
            hBusinessDate = getValue("business_date");
            hVouchDate    = getValue("vouch_date");
        }

        // =============================
        selectSQL = "to_char(sysdate,'yyyymmdd') date1";
        daoTable  = "dual";

        if (selectTable() > 0) {
            hSystemDate = getValue("date1");
        }

        hModSeqno = comcr.getModSeq();
        hModUser  = comc.commGetUserID();
        hModTime  = hSystemDate;
    }

    // *********************************************************************************
    void printHead() {
        pageCnt++;
        lineCnt = 0;
        String buf = "";
        Map<String, Object> temp = new HashMap<String, Object>();

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: GEN_A001R1", 1);
        buf = comcr.insertStr(buf, "會計分錄日結檢核表", 34);
        buf = comcr.insertStr(buf, "頁    次:", 62);
        buf = comcr.insertStr(buf, pageCnt + "", 72);
        lpar1.add(comcr.putReport(rptIdR1, rtpName1, fixDate + fixTime, rptSeq++, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "印表日期:", 1);
        buf = comcr.insertStr(buf, comcr.formatDate(chinDate, 2), 11);
        buf = comcr.insertStr(buf, "啟帳日期:", 62);
        buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 2), 72);
        lpar1.add(comcr.putReport(rptIdR1, rtpName1, fixDate + fixTime, rptSeq++, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "錯誤筆數 啟帳日期 套    號 借  方  金  額 貸  方  金  額 錯 誤 原 因   ", 1);
        lpar1.add(comcr.putReport(rptIdR1, rtpName1, fixDate + fixTime, rptSeq++, "0", buf));
        buf = "";
        buf = comcr.insertStr(buf, "======== ======== ======== ============== ============== =====================", 1);
        lpar1.add(comcr.putReport(rptIdR1, rtpName1, fixDate + fixTime, rptSeq++, "0", buf));
    }

    // ************************************************************
    void printErr(String msg, String refnoRpt) {
        errCnt++;
        errMsg = errMsg + msg + ",";

        lineCnt++;

        if (errCnt == 1 || lineCnt > 45) {
            if (lineCnt > 2)
            //  lpar1.add(comcr.putReport(rptId_r1, rtpName1, fixDate + fixTime, rptSeq++, "1", "\f"));
                lpar1.add(comcr.putReport(rptIdR1, rtpName1, fixDate + fixTime, rptSeq++, "1", "##PPP"));
            printHead();
            lineCnt = 0;
        }

        String buf = "";
        buf = comcr.insertStr(buf, String.format("%4d", errCnt), 3);
        if (hVoucTxDate.length() != 0) {
            buf = comcr.insertStr(buf, String.format("%07d", Long.valueOf(hVoucTxDate) - 19110000), 10);
        }
        buf = comcr.insertStr(buf, refnoRpt, 19);
        tempStr = String.format("%14.14s", comcr.formatNumber(hAmtDr + "", 1, 2));
        buf = comcr.insertStr(buf, tempStr , 28);
        tempStr = String.format("%14.14s", comcr.formatNumber(hAmtCr + "", 1, 2));
        buf = comcr.insertStr(buf, tempStr , 43);
        buf = comcr.insertStr(buf, msg,       58);

        lpar1.add(comcr.putReport(rptIdR1, rtpName1, fixDate + fixTime, rptSeq++, "1", buf));

    }
    // *****************************************************************************************
}
