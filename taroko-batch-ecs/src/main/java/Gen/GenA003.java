/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/19  V1.00.02  yanghan       修改了變量名稱和方法名稱                                                                              *
*  111/11/16  V1.00.03  Zuwei       sync from mega                                                                              *
*  112/01/16  V1.00.04  Zuwei       報表的金額, 因有外幣, 需有小數位數.                                                                             *
******************************************************************************/

package Gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

@SuppressWarnings("unchecked")
/* 會計分錄清單作業 */
public class GenA003 extends AccessDAO {
    // 一定要有
    private final String PROGNAME = "會計分錄清單作業  111/11/17 V1.00.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    final int DEBUG = 1;
    String tempStr = "";

    String blk = "      ";
    String prgmId = "GenA003";
    String rtpName1 = "會計分錄清單";
    String rptIdR1 = "GEN_A003R1";

    int actCnt = 0;
    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String hCallBatchSeqno = "";
    String errCode = "";
    String errDesc = "";
    String procDesc = "";
    int rptSeq = 0;
    int errCnt = 0;
    String errMsg = "";

    String hTempUser = "";
    String hTempManager = "";
    String hTempName = "";
    String hBusinessDate = "";
    String hVouchDate = "";
    String hSystemDate = "";
    String hSystemDatef = "";
    String hVoucTxDate = "";
    String hVoucRefno = "";
    String hVoucSeqno = "";
    String hVoucCurr = "";
    String hVoucUserId = "";
    String hVoucManager = "";
    String hVoucJrnStatus = "";
    String hVoucBrno = "";
    String hVoucModPgm = "";
    String hVoucAcNo = "";
    String hVoucDbcr = "";
    String hVoucMemo1 = "";
    String hVoucMemo2 = "";
    String hVoucMemo3 = "";
    String hDate = "";
    String hTime = "";
    int hVoucVoucherCnt = 0;
    double hAmtCr = 0;
    String hTempX22 = "";
    String hTempX04 = "";
    String hPrintName = "";
    String hRptName = "";

    int tempInt = 0;
    int totCnt = 0;
    int drCnt = 0;
    int crCnt = 0;
    int pageCnt = 0;
    int lineCnt = 0;
    double totAmtDr = 0;
    double totAmtCr = 0;
    private long hModSeqno;
    private String hModUser;
    private String hModTime;
    private String hModPgm;
    private String hVoucModTime;
    private String hVoucModUser;
    private long hVoucModSeqno;

    public static void main(String[] args) throws Exception {
        GenA003 proc = new GenA003();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================

            if (args.length > 2) {
                comc.errExit("Usage : GenA003 callbatch_seqno", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
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

            commonRtn();
            if (args.length > 0) {
                if (args[0].length() == 8) {
                    hBusinessDate = args[0];
                }
            }

            showLogMessage("I", "", "處理日期=[" + hBusinessDate + "]" + args.length);

            sqlCmd  = "select crt_user  h_temp_manager ";
            sqlCmd += "  from gen_user_log  ";
            sqlCmd += " where progran_cd = 'GenA002' ";
            hTempManager = selectTable() > 0 ? getValue("h_temp_manager") : "";

            sqlCmd  = "select usr_cname  h_temp_name ";
            sqlCmd += "  from sec_user  ";
            sqlCmd += " where usr_id =? ";
            setString(1, hTempUser);
            hTempName = selectTable() > 0 ? getValue("h_temp_name") : "";

            sqlCmd  = "select usr_cname  h_temp_manager ";
            sqlCmd += "  from sec_user  ";
            sqlCmd += " where usr_id =? ";
            setString(1, hTempManager);
            hTempManager = selectTable() > 0 ? getValue("h_temp_manager") : "";

            hModPgm = prgmId;
            hVoucModPgm   = hModPgm;
            hVoucModTime  = hModTime;
            hVoucModUser  = hModUser;
            hVoucModSeqno = hModSeqno;

            selectGenVouch2();

            String buf = "";
            lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++,"0",buf));

            buf = comcr.insertStr(buf, "合  計      借 方:", 01);
            tempStr = String.format("%5.5s", comcr.formatNumber(drCnt + "", 1, 0));
            buf = comcr.insertStr(buf, tempStr, 20);
            buf = comcr.insertStr(buf, comcr.formatNumber(totAmtDr + "", 2, 2), 31);
            buf = comcr.insertStr(buf, "貸 方:", 50);
            tempStr = String.format("%5.5s", comcr.formatNumber(crCnt + "", 1, 0));
            buf = comcr.insertStr(buf, tempStr, 56);
            buf = comcr.insertStr(buf, comcr.formatNumber(totAmtCr + "", 2, 2), 64);
            lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

            buf = "";
            buf = comcr.insertStr(buf, "經副襄理              會計                營業:", 1);
            buf = comcr.insertStr(buf, hTempManager, 48);
            buf = comcr.insertStr(buf, "經辦:", 60);
            buf = comcr.insertStr(buf, hTempName, 66);
            lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

            buf = "(甲級主管)                                (乙級主管)";
            lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

            actCnt = comcr.deletePtrBatchRpt(rptIdR1, sysDate);
            actCnt = comcr.insertPtrBatchRpt(lpar);
            String filename = comc.getECSHOME() + "/reports/GEN_A003R1";
            comc.writeReportForTest(filename, lpar); // for test
            // ==============================================
            // 固定要做的

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
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
    void selectGenVouch2() throws Exception {
        sqlCmd = "select ";
        sqlCmd += "tx_date, ";
        sqlCmd += "refno, ";
        sqlCmd += "seqno, ";
        sqlCmd += "curr , ";
        sqlCmd += "CRT_USER, ";
        sqlCmd += "APR_USER, ";
        sqlCmd += "jrn_status, ";
        sqlCmd += "brno, ";
        sqlCmd += "mod_pgm, ";
        sqlCmd += "ac_no, ";
        sqlCmd += "dbcr , ";
        sqlCmd += "memo1, ";
        sqlCmd += "memo2, ";
        sqlCmd += "memo3, ";
        sqlCmd += "to_char(to_number(to_char(mod_time,'yyyymmdd'))-19110000) h_date, ";
        sqlCmd += "to_char(mod_time,'hh24:mi') h_time, ";
        sqlCmd += "count(refno) h_vouc_voucher_cnt, ";
        sqlCmd += "sum(amt) h_amt_cr ";
        sqlCmd += " from gen_vouch ";
        sqlCmd += "where decode(post_flag,'','N',post_flag)   in ('N','n')  ";
        sqlCmd += "  and decode(mod_log  ,'','1',mod_log) not in ('D','U')  ";
        sqlCmd += "  and jrn_status                           in ('3')  ";
        sqlCmd += "  and tx_date   <= ? ";
        sqlCmd += "group by ";
        sqlCmd += "tx_date, ";
        sqlCmd += "refno, ";
        sqlCmd += "seqno, ";
        sqlCmd += "curr , ";
        sqlCmd += "CRT_USER, ";
        sqlCmd += "APR_USER, ";
        sqlCmd += "jrn_status, ";
        sqlCmd += "brno, ";
        sqlCmd += "mod_pgm, ";
        sqlCmd += "ac_no, ";
        sqlCmd += "dbcr , ";
        sqlCmd += "memo1, ";
        sqlCmd += "memo2, ";
        sqlCmd += "memo3, ";
        sqlCmd += "to_char(to_number(to_char(mod_time,'yyyymmdd'))-19110000), ";
        sqlCmd += "to_char(mod_time,'hh24:mi') ";
        sqlCmd += "order by tx_date,curr,refno,seqno,ac_no,decode(dbcr,'D','A') ";
        setString(1, hBusinessDate);
        int recordCnt = selectTable();

        if (DEBUG == 1)
            showLogMessage("I", "", "888 All cnt=[" + recordCnt + "]" + hBusinessDate);

        String tempX06 = "";
        String buf = "";
//        printHead2();
        for (int i = 0; i < recordCnt; i++) {
            hVoucTxDate     = getValue("tx_date", i);
            hVoucRefno       = getValue("refno", i);
            hVoucSeqno       = getValue("seqno", i);
            hVoucCurr        = getValue("curr", i);
            hVoucUserId     = getValue("CRT_USER", i);
            hVoucManager     = getValue("APR_USER", i);
            hVoucJrnStatus  = getValue("jrn_status", i);
            hVoucBrno        = getValue("brno", i);
            hVoucModPgm     = getValue("mod_pgm", i);
            hVoucAcNo       = getValue("ac_no", i);
            hVoucDbcr        = getValue("dbcr", i);
            hVoucMemo1       = getValue("memo1", i);
            hVoucMemo2       = getValue("memo2", i);
            hVoucMemo3       = getValue("memo3", i);
            hDate             = getValue("h_date", i);
            hTime             = getValue("h_time", i);
            hVoucVoucherCnt = getValueInt("h_vouc_voucher_cnt", i);
            hAmtCr           = getValueDouble("h_amt_cr", i);

            totCnt++;
            lineCnt++;
            if(totCnt == 1 || lineCnt > 24)
              {
if(DEBUG == 1)
  showLogMessage("I", "", "   9999 line=[" + lineCnt + "]" + totCnt);
               if(lineCnt > 2 )
                  lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", "##PPP"));
               printHead2();
               lineCnt = 0;
              }

            if (i == 0) {
                sqlCmd  = "select usr_cname  h_temp_manager ";
                sqlCmd += "  from sec_user  ";
                sqlCmd += " where usr_id = ? ";
                setString(1, hVoucManager);
                hTempManager = selectTable() > 0 ? getValue("h_temp_manager") : "";
            }

            sqlCmd  = "select substrb(AC_BRIEF_NAME,1,22)  h_temp_x22 ";
            sqlCmd += "from gen_acct_m  ";
            sqlCmd += "where ac_no = ?";
            setString(1, hVoucAcNo);
            hTempX22 = selectTable() > 0 ? getValue("h_temp_x22") : "";

            sqlCmd  = "select substrb(curr_eng_name,1,4)  h_temp_x04 ";
            sqlCmd += "  from ptr_currcode ";
            sqlCmd += " where curr_code_gl = ? ";
            setString(1, hVoucCurr);
            int cnt = selectTable();
            hTempX04 = cnt > 0 ? getValue("h_temp_x04") : "";

            buf = "";
            if (!hVoucRefno.equals(tempX06)) {
                buf = comcr.insertStr(buf, hVoucRefno, 1);
                tempX06 = hVoucRefno;
            } else
                buf = comcr.insertStr(buf, blk, 1);
            buf = comcr.insertStr(buf, hVoucCurr , 8);
            buf = comcr.insertStr(buf, "TWD"       , 8);
            buf = comcr.insertStr(buf, hTempX04  , 8);
            buf = comcr.insertStr(buf, hVoucAcNo, 13);
            buf = comcr.insertStr(buf, hTempX22  , 22);
            buf = comcr.insertStr(buf, hVoucDbcr , 44);
            if (hVoucDbcr.equals("D")) {
                drCnt += hVoucVoucherCnt;
                totAmtDr += hAmtCr;
            } else {
                crCnt += hVoucVoucherCnt;
                totAmtCr += hAmtCr;
            }
//if(DEBUG == 1)
//  showLogMessage("I", "", "  888 tot amt=[" + totAmtDr + "]" + hAmtCr + "," + hVoucDbcr);

            tempStr = String.format("%14.14s", comcr.formatNumber(hAmtCr + "", 2, 2));
            buf = comcr.insertStr(buf, tempStr, 49);

            if (hVoucModPgm.equals("genp0110")) {
                buf = comcr.insertStr(buf, "自由格式", 64);
            } else {
                buf = comcr.insertStr(buf, "R6起帳"  , 64);
            }

            buf = comcr.insertStr(buf, hVoucMemo1  , 64);
            lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++,"0",buf));
//if(DEBUG == 1)
//  showLogMessage("I", "", "  888 BUF=[" + buf + "]" + buf.length());
        }
    }

    // ********************************************************************************
    void printHead2() {
        pageCnt++;
        String buf = "";
        buf = comcr.insertStr(buf, blk, 1);
        buf = comcr.insertStr(buf, "合作金庫商業銀行", 30);
        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, blk, 1);
        buf = comcr.insertStr(buf, "  會計分錄清單", 30);
        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "製表日期:", 1);
        buf = comcr.insertStr(buf, comcr.formatDate(hBusinessDate, 2), 11);
        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "報表名稱: GEN_A003R1", 1);
        buf = comcr.insertStr(buf, "頁    次:", 60);
        buf = comcr.insertStr(buf, pageCnt + "", 70);
        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "分 行 別:", 1);
        buf = comcr.insertStr(buf, "3144      ", 11);
        buf = comcr.insertStr(buf, "部 門 別:", 30);
        buf = comcr.insertStr(buf, "銀 行 部 ", 40);
        buf = comcr.insertStr(buf, "業 務 別:", 60);
        buf = comcr.insertStr(buf, "簽 帳 卡 ", 70);
        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

        buf = "序  號 幣別 科子細目代號   名     稱       D/C 金           額 摘要一 ";
        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));

        buf = "====== ==== ============================== === =============== ==================";
        lpar.add(comcr.putReport(rptIdR1, rtpName1, sysDate + sysTime, rptSeq++, "0", buf));
    }

    // ************************************************************************
    void commonRtn() throws Exception {
        selectSQL = "business_date,vouch_date ";
        daoTable = "ptr_businday";

        if (selectTable() > 0) {
            hBusinessDate = getValue("business_date");
            hVouchDate = getValue("vouch_date");
        }

        // =============================
        selectSQL = "to_char(sysdate,'yyyymmdd') date1";
        daoTable = "dual";

        if (selectTable() > 0) {
            hSystemDate = getValue("date1");
        }

        hModSeqno = comcr.getModSeq();
        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }
    // ************************************************************************
}