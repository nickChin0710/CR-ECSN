/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-11-13  V1.00.01    tanwei    updated for project coding standard       *
*                                                                             *
******************************************************************************/

package Tsc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

import bank.AuthIntf.AuthData;
import bank.AuthIntf.AuthGateway;

/*代行授權送CCAS佔額處理程式*/
public class TscB040 extends AccessDAO {
    private final String progname = "代行授權送CCAS佔額處理程式   109/11/13 V1.00.01";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hTempLocalTime = "";
    String hCardCardNo = "";
    String hCardNewEndDate = "";
    String hCardTransCvv2 = "";
    double hTacsTransAmount = 0;
    String hTacsRetrRefNo = "";
    String hTacsRowid = "";
    String hTacsCcasRespCode = "";
    String hTacsProcFlag = "";
    String hOwsmWfValue = "";
    String hOwsmWfValue2 = "";
    int totalCnt = 0;
    int tranCnt = 0;
    int cmdInt = 0;
    String tmpstr = "";

    Isobuf ccassndData, ccasrcvData;

    public int mainProcess(String[] args) throws IOException {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0) {
                comc.errExit("Usage : TscB040 ", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hBusiBusinessDate = "";
            selectPtrBusinday();
            selectPtrSysParm();

            selectTscActauthCcas();

            showLogMessage("I", "", String.format("累計恢復 [%d] 筆", totalCnt));

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
    void selectPtrBusinday() throws Exception {
        sqlCmd = "select business_date,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_temp_local_time ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                    : hBusiBusinessDate;
            hTempLocalTime = getValue("h_temp_local_time");
        }

    }

    /***********************************************************************/
    void selectPtrSysParm() throws Exception {
        hOwsmWfValue = "";
        hOwsmWfValue2 = "";
        sqlCmd = "select wf_value,";
        sqlCmd += "wf_value2 ";
        sqlCmd += " from ptr_sys_parm  ";
        sqlCmd += "where wf_parm = 'SYSPARM'  ";
        sqlCmd += "and wf_key = 'CCASLINK' ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_sys_parm not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hOwsmWfValue = getValue("wf_value");
            hOwsmWfValue2 = getValue("wf_value2");
        }

    }

    /***********************************************************************/
    void selectTscActauthCcas() throws Exception {
        String tmpstr = "";

        sqlCmd = "select ";
        sqlCmd += "c.card_no,";
        sqlCmd += "c.new_end_date,";
        sqlCmd += "c.trans_cvv2,";
        sqlCmd += "a.trans_amount,";
        sqlCmd += "a.retr_ref_no,";
        sqlCmd += "a.rowid as rowid ";
        sqlCmd += "from crd_card c,tsc_card b,tsc_actauth_ccas a ";
        sqlCmd += "where c.card_no = b.card_no ";
        sqlCmd += "and b.tsc_card_no = a.tsc_card_no ";
        sqlCmd += "and a.proc_flag != 'Y' ";
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hCardCardNo = getValue("card_no", i);
            hCardNewEndDate = getValue("new_end_date", i);
            hCardTransCvv2 = getValue("trans_cvv2", i);
            hTacsTransAmount = getValueDouble("trans_amount", i);
            hTacsRetrRefNo = getValue("retr_ref_no", i);
            hTacsRowid = getValue("rowid", i);

            totalCnt++;

//            set_ccas_data();
            cmdInt = callCcas(4);

            hTacsProcFlag = "Y";
            if (cmdInt != 0) {
                tmpstr = String.format("%1d", cmdInt);
                hTacsProcFlag = tmpstr;
            }

            updateTscActauthCcas();
            commitDataBase();

        }
    }

    /***********************************************************************/
//    void set_ccas_data() throws Exception {
//        String ccas_tmpstr = "";
//
//        ccassnd_data = new ISOBUF();
//        ccassnd_data.type_flag = " ";
//        ccassnd_data.card_no = h_card_card_no;
//        ccassnd_data.expire_date = h_card_new_end_date;
//        if (h_card_trans_cvv2.length() != 0) {
//            ccassnd_data.cvv2 = comc.trans_passwd(1, h_card_trans_cvv2);
//        }
//        ccas_tmpstr = String.format("4100");
//        ccassnd_data.mcc_code = ccas_tmpstr;
//        ccassnd_data.mcht_no = "6666600000     ";
//        ccassnd_data.local_time = h_temp_local_time;
//        ccas_tmpstr = String.format("0%09.0f00", h_tacs_trans_amount);
//        ccassnd_data.trans_amt = ccas_tmpstr;
//        tmpstr = String.format("%010.0f", (double) comcr.GetModSeq());
//        ccassnd_data.auth_no = tmpstr.substring(4);
//        ccassnd_data.org_ref_no = "000123456789";
//        ccassnd_data.len = "Y";
//    }

    /***********************************************************************/
    void updateTscActauthCcas() throws Exception {
        daoTable = "tsc_actauth_ccas";
        updateSQL = "ccas_resp_code = ?,";
        updateSQL += " proc_flag  = ?,";
        updateSQL += " proc_date  = to_char(sysdate,'yyyymmdd'),";
        updateSQL += " mod_time  = sysdate,";
        updateSQL += " mod_pgm  = ?";
        whereStr = "where rowid   = ? ";
        setString(1, hTacsCcasRespCode);
        setString(2, hTacsProcFlag);
        setString(3, javaProgram);
        setRowId(4, hTacsRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_tsc_actauth_ccas not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    int callCcas(int hInt) throws Exception {
        String tmpstr = "";
        int rc = 0;

        AuthGateway authGatewayTest = new AuthGateway();
        AuthData lAuthData = new AuthData();

//        if (h_card_trans_cvv2.length() != 0) {
//            ccassnd_data.cvv2 = comc.trans_passwd(1, h_card_trans_cvv2);
//        }

        lAuthData.setCardNo(hCardCardNo);
        lAuthData.setExpireDate(hCardNewEndDate); /** YYYYMMDD */
        if (hCardTransCvv2.length() != 0) {
            String ccasTmpstr = comc.transPasswd(1, hCardTransCvv2);
            lAuthData.setCvv2(ccasTmpstr);
        }
        lAuthData.setLocalTime(hTempLocalTime);
        lAuthData.setMccCode("4100");
        lAuthData.setMchtNo("6666600000     ");
        tmpstr = String.format("%010.0f", (double) comcr.getModSeq());
        lAuthData.setOrgAuthNo(tmpstr.substring(4));
        lAuthData.setOrgRefNo("000123456789");
        lAuthData.setTransAmt(String.format("0%09.0f00", hTacsTransAmount));
        tmpstr = String.format("%1.1d", hInt);
        lAuthData.setTransType(
                tmpstr); /* 1: regular 2:refund 3:reversal 4:代行 */
        lAuthData.setTypeFlag(" "); /* I: install M: mail C:公用 */

        String slTranxResult = authGatewayTest.startProcess(lAuthData, hOwsmWfValue, hOwsmWfValue2);

        if (debug == 1)
            showLogMessage("I", "", "  888 call case rep buf= " + slTranxResult);

        System.out.println("Terminated");

        splitBuf1(tmpstr);
        tmpstr = slTranxResult.substring(0, 2);
        for (int inta1 = tmpstr.length() - 1; inta1 >= 0; inta1--) {
            if (tmpstr.toCharArray()[inta1] != ' ')
                break;
        }
        hTacsCcasRespCode = tmpstr;
        if (!ccasrcvData.respCode.equals("00")) {
            return (1);
        }
        return (0);
    }


    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        TscB040 proc = new TscB040();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    /***********************************************************************/
    class Isobuf {
        String transType;
        String typeFlag;
        String cardNo;
        String expireDate;
        String transAmt;
        String mccCode;
        String mchtNo;
        String localTime;
        String orgAuthNo;
        String orgRefNo;
        String cvv2;
        String authNo;
        String respCode;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += comc.fixLeft(transType, 1);
            rtn += comc.fixLeft(typeFlag, 1);
            rtn += comc.fixLeft(cardNo, 19);
            rtn += comc.fixLeft(expireDate, 8);
            rtn += comc.fixLeft(transAmt, 12);
            rtn += comc.fixLeft(mccCode, 4);
            rtn += comc.fixLeft(mchtNo, 15);
            rtn += comc.fixLeft(localTime, 14);
            rtn += comc.fixLeft(orgAuthNo, 6);
            rtn += comc.fixLeft(orgRefNo, 12);
            rtn += comc.fixLeft(cvv2, 4);
            rtn += comc.fixLeft(authNo, 6);
            rtn += comc.fixLeft(respCode, 2);
            rtn += comc.fixLeft(len, 1);
            return rtn;
        }
    }

    void splitBuf1(String str) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes("MS950");
        ccasrcvData.transType = comc.subMS950String(bytes, 0, 1);
        ccasrcvData.typeFlag = comc.subMS950String(bytes, 1, 1);
        ccasrcvData.cardNo = comc.subMS950String(bytes, 2, 19);
        ccasrcvData.expireDate = comc.subMS950String(bytes, 21, 8);
        ccasrcvData.transAmt = comc.subMS950String(bytes, 29, 12);
        ccasrcvData.mccCode = comc.subMS950String(bytes, 41, 4);
        ccasrcvData.mchtNo = comc.subMS950String(bytes, 45, 15);
        ccasrcvData.localTime = comc.subMS950String(bytes, 60, 14);
        ccasrcvData.orgAuthNo = comc.subMS950String(bytes, 74, 6);
        ccasrcvData.orgRefNo = comc.subMS950String(bytes, 80, 12);
        ccasrcvData.cvv2 = comc.subMS950String(bytes, 92, 4);
        ccasrcvData.authNo = comc.subMS950String(bytes, 96, 6);
        ccasrcvData.respCode = comc.subMS950String(bytes, 102, 2);
        ccasrcvData.len = comc.subMS950String(bytes, 104, 1);
    }

}
