/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
* DATE       Version   AUTHOR              DESCRIPTION                       *
* ---------  -------------------  ------------------------------------------ *
* 112/03/21  V1.00.00  Wilson     Initial                                    *
* 112/06/16  V1.00.01  Wilson     5000筆commit                                *
* 112/09/05  V1.00.02  Wilson     selectDbcCard排序調整                                                                    *
*****************************************************************************/
package Dbc;

import com.*;

import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class DbcO010 extends AccessDAO {
    private String progname = "Debit卡補原始卡號資料處理程式 112/09/05  V1.00.02 ";

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;

    String hTempUser = "";
    String hBusiBBusinessDate = "";
    String hCardPSeqno = "";
    String hCardCardNo = "";
    String hCardOldCardNo = "";
    String hCardEndCardNo = "";
    String hCard1EndCardNo = "";
    String hCard2EndCardNo = "";
    String hCardOriCardNo = "";
    String hCardOriApplyNo = "";
    String hCardOriIssueDate = "";
    String hCardIssueDate = "";

    String[] hMCardCardNo = new String[100];
    String[] hmCardOldCardNo = new String[100];
    String[] hmCardIssueDate = new String[100];

    int checkCnt = 0, updateCnt = 0, allScanFlag = 0;
    long totalCnt = 0;

// ************************************************************************

public static void main(String[] args) throws Exception {
    DbcO010 proc = new DbcO010();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
}

// ************************************************************************

public int mainProcess(String[] args) {
    try {

        dateTime();
        setConsoleMode("N");
        javaProgram = this.getClass().getName();
        showLogMessage("I", "", javaProgram + " " + progname);

        if (comm.isAppActive(javaProgram))
            comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");

        if (args.length > 2) {
            comc.errExit("PARM 1 : [ALL]", "");
        }

        if (args.length == 0)
            if (javaProgram.equals("ALL"))
                allScanFlag = 1;

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

        selectPtrBusinday();

        updateDbcCard1();

        selectDbcCard();

        showLogMessage("I", "", "Total process record[" + totalCnt + "] check[" + checkCnt + "]");
        if (checkCnt != 0)
            showLogMessage("I", "", "卡號資訊有誤, 請檢查更正後再執行");

        if (checkCnt == 0) {
            showLogMessage("I", "", "更新最新卡號開始....");
            totalCnt = 0;
            selectDbcCard2();
            showLogMessage("I", "", "更新最新卡號完成");
            showLogMessage("I", "", "Process record[" + totalCnt + "] update_record[" + updateCnt + "]");
        }

        comcr.hCallErrorDesc = "程式執行結束";
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

} // End of mainProcess
// ************************************************************************
public void selectPtrBusinday() throws Exception 
{
    daoTable = "PTR_BUSINDAY";
    whereStr = "FETCH FIRST 1 ROW ONLY";

    int recordCnt = selectTable();

    if (notFound.equals("Y")) {
        showLogMessage("I", "", "select ptr_businday error!");
        comcr.errRtn("select_ptr_businday error!", "", comcr.hCallBatchSeqno);
    }

    hBusiBBusinessDate = getValue("BUSINESS_DATE");
    showLogMessage("I", "", "本日營業日 : [" + hBusiBBusinessDate + "]");
}
// ************************************************************************
public void selectDbcCard() throws Exception 
{
    selectSQL = "p_seqno, " + "CARD_NO, " + "OLD_CARD_NO ";
    daoTable = "DBC_CARD";
    whereStr = "WHERE ORI_CARD_NO = '' OR ORI_ISSUE_DATE = '' " 
             + "ORDER BY p_seqno,card_no";

    openCursor();

    while (fetchTable()) {
        hCardPSeqno = getValue("p_seqno");
        hCardCardNo = getValue("CARD_NO");
        hCardOldCardNo = getValue("OLD_CARD_NO");

        totalCnt++;
        processDisplay(50000); // every 50000 display message
        
        int retCode = selectDbcCard0();

        if (retCode == 1) {
            checkCnt++;
            selectDbcCard1();
            continue;
        } else if (retCode == 2) {
            updateDbcCard3();
            commitDataBase();
            
            continue;
        }
        
        updateDbcCard();
        commitDataBase();
    }
    closeCursor();
}
// ************************************************************************
public void selectDbcCard2() throws Exception 
{
    selectSQL = "p_seqno, " + "ORI_CARD_NO, " + "min(ori_apply_no) AS ORI_APPKY_NO, "
            + "min(end_card_no) as end_card_no1, " + "max(end_card_no) as  end_card_no2, "
            + "max(issue_date) as issue_date ";
    daoTable = "DBC_CARD";

    if (allScanFlag == 1)
        whereStr = "GROUP BY p_seqno,ori_card_no";
    else
        whereStr = "WHERE p_seqno in (select p_seqno  from DBC_CARD "
                + "                  where end_card_no = '') GROUP BY p_seqno,ori_card_no";

    openCursor();

    while (fetchTable()) {
        hCardPSeqno = getValue("p_seqno");
        hCardOriCardNo = getValue("ORI_CARD_NO");
        hCardOriApplyNo = getValue("ORI_APPLY_NO");
        hCard1EndCardNo = getValue("END_CARD_NO1");
        hCard2EndCardNo = getValue("END_CARD_NO2");
        hCardIssueDate = getValue("ISSUE_DATE");

        totalCnt++;

        if (selectDbcCard3() != 0)
            continue;

        if ((hCard1EndCardNo.equals(hCardEndCardNo)) && (hCard2EndCardNo.equals(hCardEndCardNo)))
            continue;

        updateCnt++;
        updateDbcCard2();

        processDisplay(50000); // every 50000 display message
        if ((updateCnt % 5000) == 0)
            commitDataBase();
    }
    closeCursor();
}
// ************************************************************************
public void updateDbcCard() throws Exception 
{
    updateSQL = "ori_card_no    = ?, " + "ori_apply_no   = ?, " + "end_card_no    = ?, " + "ori_issue_date = ? ";
    daoTable = "DBC_CARD";
    whereStr = "WHERE CARD_NO = ? ";

    setString(1, hCardOriCardNo);
    setString(2, hCardOriApplyNo);
    setString(3, hCardCardNo);
    setString(4, hCardOriIssueDate);
    setString(5, hCardCardNo);

    int recCnt = updateTable();

    if (notFound.equals("Y")) {
        showLogMessage("I", "", "update_DBC_CARD error!");
        showLogMessage("I", "", "card_no=[" + hCardCardNo + "]");
        comcr.errRtn("update_DBC_CARD error!", "", comcr.hCallBatchSeqno);
    }
    return;
}
// ************************************************************************
public void updateDbcCard1() throws Exception 
{
    updateSQL = "ori_card_no    = card_no, " + "ori_apply_no   = apply_no, " + "end_card_no    = card_no, "
            + "ori_issue_date = issue_date ";
    daoTable = "DBC_CARD";
    whereStr = "WHERE OLD_CARD_NO = '' " + "AND   ORI_CARD_NO = '' ";

    int recCnt = updateTable();

    showLogMessage("I", "", "首張卡號更新完成 共 " + recCnt + " 筆");
    return;
}
// ************************************************************************
public void updateDbcCard2() throws Exception 
{
    updateSQL = "end_card_no    = ? ";
    daoTable = "DBC_CARD";
    whereStr = "WHERE p_seqno     = ? " + "AND   ORI_CARD_NO = ? " + "AND   END_CARD_NO != ? ";

    setString(1, hCardEndCardNo);
    setString(2, hCardPSeqno);
    setString(3, hCardOriCardNo);
    setString(4, hCardEndCardNo);

    int recCnt = updateTable();

    return;
}
// ************************************************************************
public void updateDbcCard3() throws Exception 
{
    updateSQL = "ori_card_no    = card_no, " + "ori_apply_no   = apply_no, " + "end_card_no    = card_no, "
            + "ori_issue_date = issue_date ";
    daoTable = "DBC_CARD";
    whereStr = "WHERE CARD_NO = ? ";

    setString(1, hCardCardNo);
    int recCnt = updateTable();

    return;
}
// ************************************************************************
public int selectDbcCard0() throws Exception 
{
    selectSQL = "ORI_CARD_NO, " + "ORI_APPLY_NO, " + "ORI_ISSUE_DATE ";
    daoTable = "DBC_CARD";
    whereStr = "WHERE CARD_NO = ? " + "AND   p_seqno = ?";

    setString(1, hCardOldCardNo);
    setString(2, hCardPSeqno);

    int recCnt = selectTable();

    if (notFound.equals("Y"))
        return (2);

    hCardOriCardNo = getValue("ORI_CARD_NO");
    hCardOriApplyNo = getValue("ORI_APPLY_NO");
    hCardOriIssueDate = getValue("ORI_ISSUE_DATE");

    if (hCardOriCardNo.length() == 0)
        return (1);
    return (0);
}
// ************************************************************************
public void selectDbcCard1() throws Exception 
{
    extendField = "CRD1.";
    selectSQL = "CARD_NO," + "ISSUE_DATE, " + "OLD_CARD_NO ";
    daoTable = "DBC_CARD";
    whereStr = "WHERE p_seqno = ?  " + "ORDER BY ISSUE_DATE";

    setString(1, hCardPSeqno);
    int recCnt = selectTable();

    if (notFound.equals("Y")) {
        showLogMessage("I", "", "select DBC_CARD_1 error!");
        showLogMessage("I", "", "p_seqno=[" + hCardPSeqno + "]");
        comcr.errRtn("select_DBC_CARD_1 error!", "", comcr.hCallBatchSeqno);
    }

    showLogMessage("I", "", "卡號資訊錯誤 error!");
    showLogMessage("I", "", "p_seqno=[" + hCardPSeqno + "]");
    for (int inti = 0; inti < recCnt; inti++) {
        showLogMessage("I", "", "  card_no    =[" + getValue("CRD1.CARD_NO") + "]");
        showLogMessage("I", "", "  issue_date =[" + getValue("CRD1,ISSUE_DATE") + "]");
        showLogMessage("I", "", "  old_card_no=[" + getValue("CRD1,OLD_CARD_NO") + "]");
    }
}
// ************************************************************************
public int selectDbcCard3() throws Exception 
{
    extendField = "CRD3.";
    selectSQL = "CARD_NO," + "OLD_CARD_NO, " + "ISSUE_DATE ";
    daoTable = "DBC_CARD";
    whereStr = "WHERE p_seqno = ? " + "AND   ORI_CARD_NO = ? " + "AND   nvl(ISSUE_DATE,'x')  = ? " + "ORDER BY "
            + "CASE CURRENT_CODE " + "  WHEN '0' THEN '0' ELSE '1' " + "END DESC, " + "OPPOST_DATE DESC, "
            + "CASE OLD_CARD_NO " + "  WHEN '' THEN 1 ELSE 0 " + "END DESC ";

    setString(1, hCardPSeqno);
    setString(2, hCardOriCardNo);
    setString(3, hCardIssueDate);

    int recCnt = selectTable();

    if (notFound.equals("Y")) {
        showLogMessage("I", "", "select DBC_CARD_3 error!");
        showLogMessage("I", "", "p_seqno=[" + hCardPSeqno + "]");
        comcr.errRtn("select_DBC_CARD_3 error!", "", comcr.hCallBatchSeqno);
    }

    for (int inti = 0; inti < recCnt; inti++) {
        hMCardCardNo[inti] = getValue("CRD3.CARD_NO");
        hmCardOldCardNo[inti] = getValue("CRD3.OLD_CARD_NO");
        hmCardIssueDate[inti] = getValue("CRD3.ISSUE_DATE");

    }
    if (recCnt >= 2) {
        if (!hmCardOldCardNo[1].equals(hmCardOldCardNo[0]))
            if (!hMCardCardNo[1].equals(hmCardOldCardNo[0])) {
                showLogMessage("I", "", "無法辨別最新卡號 error!");
                showLogMessage("I", "", "p_seqno=[" + hCardPSeqno + "]");
                for (int inti = 0; inti < recCnt; inti++) {
                    showLogMessage("I", "", "  card_no    =[" + hMCardCardNo[inti] + "]");
                    showLogMessage("I", "", "  issue_date =[" + hmCardIssueDate[inti] + "]");
                    showLogMessage("I", "", "  old_card_no=[" + hmCardOldCardNo[inti] + "]");
                }
                return (1);
            }
    }
    hCardEndCardNo = hMCardCardNo[0];
    return (0);
}
// ************************************************************************
} // End of class FetchSample
