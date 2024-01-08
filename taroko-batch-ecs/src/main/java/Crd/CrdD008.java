/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/06/28  V1.01.01  Lai        Initial          old: crd_d008             *
* 109/12/18  V1.00.02    shiyuqi       updated for project coding standard   *
* 111/12/13  V1.00.03  Wilson     子卡額度不限附卡且不需更新                                                                      *
* 112/03/01  V1.00.04  Wilson     mark update org_risk_bank_no               *                                                                     *
*****************************************************************************/
package Crd;

import com.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD008 extends AccessDAO {
    private String progname = "額度檢核作業        112/03/01  V1.00.04 ";
    private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug   = 1;
    int debugD = 1;

    String checkHome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;

    String mbosBatchno = "";
    double mbosRecno = 0;
    String mbosSupFlag = "";
    String mbosPmId = "";
    String mbosPmIdCode = "";
    String mbosPmBirthday = "";
    String mbosCorpActFlag = "";
    String mbosCorpNo = "";
    String mbosCorpPSeqno = "";
    String mbosRiskBankNo = "";
    String mbosOrgRiskBankNo = "";
    int mbosCeditLmt = 0;
    int mbosAuthCreditLmt = 0;
    int mbosOrgIndivCrdLmt = 0;
    String mbosAcctType = "";
    String mbosRowid = "";
    String paccCardIndicator = "";
    int mbosMainCreditLmt = 0;
    int mbosIndivCrdLmt = 0;
    int mbosOrgCashLmt = 0;

    String idnoIdPSeqno = "";
    String acnoPSeqno = "";
    String acnoRiskBankNo = "";

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        CrdD008 proc = new CrdD008();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            checkHome = comc.getECSHOME();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length > 3) {
                String err1 = "CrdD008  [seq_no]\n";
                String err2 = "CrdD008  [seq_no]";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr.hCallBatchSeqno = hCallBatchSeqno;

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }
            comcr.hCallRProgramCode = this.getClass().getName();
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.hCallParameterData = javaProgram;
                for (int i = 0; i < args.length; i++) {
                    comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
                }
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

            // showLogMessage("I","", "批號=" + h_batchno + " 製卡來源=" +
            // h_emboss_source);

            dateTime();
            selectPtrBusinday();

            totalCnt = 0;

            selectCrdEmboss();

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
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
public void selectPtrBusinday() throws Exception {
        selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("BUSINESS_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");
    }
// ************************************************************************
public void selectCrdEmboss() throws Exception 
{

        selectSQL = "   a.batchno               " + " , a.recno                 " 
                  + " , a.sup_flag              " + " , a.pm_id                 " 
                  + " , a.pm_id_code            " + " , a.pm_birthday           "
                  + " , a.risk_bank_no          " + " , a.corp_no               " 
                  + " , a.credit_lmt            " + " , a.auth_credit_lmt       " 
                  + " , a.org_indiv_crd_lmt     " + " , a.acct_type             "
                  + " , decode(a.corp_act_flag,'','N',a.corp_act_flag) " 
                  + " , b.card_indicator        "
                  + " , a.rowid      as rowid ";
        daoTable  = "crd_emboss a,ptr_acct_type b ";
        whereStr  = "where a.in_main_date  = ''  " 
                  + "  and a.to_nccc_date  = ''  "
                  + "  and b.acct_type     = a.acct_type ";

        openCursor();

        while (fetchTable()) {
            initRtn();

            mbosBatchno           = getValue("batchno");
            mbosRecno             = getValueDouble("recno");
            mbosSupFlag          = getValue("sup_flag");
            mbosPmId             = getValue("pm_id");
            mbosPmIdCode        = getValue("pm_id_code");
            mbosPmBirthday       = getValue("pm_birthday");
            mbosRiskBankNo      = getValue("risk_bank_no");
            mbosCorpNo           = getValue("corp_no");
            mbosCorpActFlag     = getValue("corp_act_flag");
            mbosAcctType         = getValue("acct_type");
            mbosCeditLmt        = getValueInt("credit_lmt");
            mbosAuthCreditLmt   = getValueInt("auth_credit_lmt");
            mbosOrgIndivCrdLmt = getValueInt("org_indiv_crd_lmt");
            paccCardIndicator    = getValue("card_indicator");
            mbosRowid             = getValue("rowid");

            totalCnt++;
            processDisplay(5000); // every nnnnn display message
            if (debug == 1) {
                showLogMessage("I", "", "  888 Card=[" + mbosBatchno + "]");
                showLogMessage("I", "", "  888   id=[" + mbosPmId + "]");
                showLogMessage("I", "", "  888 c_amt=[" + mbosCeditLmt + "]");
            }

            tmpInt = selectCrdIdno();
            if (debug == 1)
                showLogMessage("I", "", "  888 idno=[" + tmpInt + "]");
            if (tmpInt == 0) // 存在
            {
                if (paccCardIndicator.equals("2")) {
                    selectActAcno1();
                    selectActAcno2();
                } else {
                    selectActAcno();
                }
                acnoPSeqno         = getValue("acno_p_seqno");
                acnoRiskBankNo    = getValue("acno_risk_bank_no");
                mbosOrgCashLmt    = getValueInt("combo_cash_limit");
                mbosMainCreditLmt = getValueInt("line_of_credit_amt");

                tmpInt = selectCrdCard();
                if (tmpInt != 0) {
                    mbosAuthCreditLmt = mbosCeditLmt;
                } else {
                    if (mbosMainCreditLmt > mbosCeditLmt) {
                        mbosAuthCreditLmt = mbosMainCreditLmt;
                    } else {
                        mbosAuthCreditLmt = mbosCeditLmt;
                    }
                }
                mbosOrgRiskBankNo = acnoRiskBankNo;
            } else {
                mbosOrgRiskBankNo = "";
                mbosMainCreditLmt = 0;
                mbosAuthCreditLmt = mbosCeditLmt;
                if (paccCardIndicator.equals("1"))
                    mbosOrgRiskBankNo = mbosRiskBankNo;
            }
            if (debug == 1)
                showLogMessage("I", "", "  888 card_ind=[" + paccCardIndicator + "]");

//20221213子卡額度不限附卡且不需更新            
//            if (paccCardIndicator.equals("1") && mbosSupFlag.equals("1") && (mbosOrgIndivCrdLmt > 0)) {
//            	mbosIndivCrdLmt = mbosOrgIndivCrdLmt;
//            	  
//            	if (mbosAuthCreditLmt < mbosOrgIndivCrdLmt) {
//            		mbosIndivCrdLmt = mbosAuthCreditLmt;
//            	}            		                  
//            }

            if (mbosOrgRiskBankNo.length() < 1)
                mbosOrgRiskBankNo = mbosRiskBankNo;

            updateCrdEmboss();
        }

    }

    // ************************************************************************
    public int selectCrdIdno() throws Exception {
        selectSQL = "id_p_seqno    ";
        daoTable = "crd_idno      ";
        whereStr = "where id_no         = ? " + "  and birthday      = ? " 
                 + "fetch first 1 row only";

        setString(1, mbosPmId);
        setString(2, mbosPmBirthday);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        idnoIdPSeqno = getValue("id_p_seqno");

        return (0);
    }

    // ************************************************************************
    public int selectActAcno() throws Exception {
        selectSQL = "line_of_credit_amt   , " + "combo_cash_limit     , " // **
                                                                          // 本來就是
                                                                          // '1'
                  + "acno_p_seqno              , " + "risk_bank_no as acno_risk_bank_no ";
        daoTable  = "act_acno      ";
        whereStr  = "where id_p_seqno   = ? " 
                  + "  and acct_type    = ? ";

        setString(1, idnoIdPSeqno);
        setString(2, mbosAcctType);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int selectActAcno1() throws Exception {
        selectSQL = "a.risk_bank_no  as acno_risk_bank_no ";
        daoTable = "crd_corp b , act_acno a     ";
        whereStr = "where a.id_p_seqno   = '' " 
                 + "  and a.acct_type    = ?  " 
                 + "  and b.corp_no      = ?  "
                 + "  and a.corp_p_seqno = b.corp_p_seqno  ";

        setString(1, mbosAcctType);
        setString(2, mbosCorpNo);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int selectActAcno2() throws Exception {
        selectSQL = "a.line_of_credit_amt   , " + "a.combo_cash_limit     , " 
                  + "a.acno_p_seqno                ";
        daoTable  = "crd_corp b , act_acno a    ";
        whereStr  = "where a.id_p_seqno    = ? " 
                  + "  and a.acct_type     = ? " 
                  + "  and b.corp_no       = ? "
                  + "  and a.corp_p_seqno  = b.corp_p_seqno "
                  + "  and decode(a.corp_act_flag,'','N',a.corp_act_flag) = ? ";

        setString(1, idnoIdPSeqno);
        setString(2, mbosAcctType);
        setString(3, mbosCorpNo);
        setString(4, mbosCorpActFlag);

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int selectCrdCard() throws Exception {

        selectSQL = "card_no  ";
        daoTable = "crd_card            ";
        whereStr = "WHERE acno_p_seqno      = ?  " 
                 + "  and id_p_seqno   = ?  " 
                 + "  and current_code = '0' ";

        setString(1, acnoPSeqno);
        setString(2, idnoIdPSeqno);

        int recCnt = selectTable();

        if (notFound.equals("Y")) {
            return (1);
        }

        return (0);
    }

    // ************************************************************************
    public int updateCrdEmboss() throws Exception {
        if (debug == 1)
            showLogMessage("I", "", " upd emboss=[" + mbosAuthCreditLmt + "]" + mbosMainCreditLmt);

        updateSQL = "auth_credit_lmt     =  ? , " + "main_credit_lmt     =  ? , " 
//                + "indiv_crd_lmt       =  ? , " 
        		  + "org_cash_lmt        =  ? , " 
//                 + "org_risk_bank_no    =  ? , " 
        		  + "mod_pgm             =  ? , "
                  + "mod_time            = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
        daoTable  = "crd_emboss";
        whereStr  = "where rowid   = ? ";

        setInt(1, mbosAuthCreditLmt);
        setInt(2, mbosMainCreditLmt);
//        setInt(3, mbosIndivCrdLmt);
        setInt(3, mbosOrgCashLmt);
//        setString(4, mbosOrgRiskBankNo);
        setString(4, javaProgram);
        setString(5, sysDate + sysTime);
        setRowId( 6, mbosRowid);

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error[notFind]";
            String err2 = "";
            comcr.errRtn(err1, err2, hCallBatchSeqno);
        }

        return 0;
    }

    // ************************************************************************
    public void initRtn() throws Exception {

        mbosBatchno = "";
        mbosRecno = 0;
        mbosSupFlag = "";
        mbosPmId = "";
        mbosPmIdCode = "";
        mbosPmBirthday = "";
        mbosCorpActFlag = "";
        mbosCorpNo = "";
        mbosCorpPSeqno = "";
        mbosRiskBankNo = "";
        mbosOrgRiskBankNo = "";
        mbosCeditLmt = 0;
        mbosAuthCreditLmt = 0;
        mbosOrgIndivCrdLmt = 0;
        mbosAcctType = "";
        mbosRowid = "";
        paccCardIndicator = "";
        mbosMainCreditLmt = 0;
        mbosIndivCrdLmt = 0;
        mbosOrgCashLmt = 0;

        idnoIdPSeqno = "";
        paccCardIndicator = "";
        acnoPSeqno = "";
    }
    // ************************************************************************

} // End of class FetchSample
