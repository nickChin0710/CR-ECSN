/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109/11/24  V1.00.01   shiyuqi    updated for project coding standard       *
*  112/03/03  V1.00.02    JeffKung  for TCB                                   *
******************************************************************************/

package Bil;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*持卡人轉換機制報送JCIC*/
public class BilA041 extends AccessDAO {
    private String progname = "持卡人轉換機制報送JCIC  112/03/03 V1.00.02 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;

    String prgmId = "BilA041";
    String prgmName = "持卡人轉換機制報送JCIC";
    String errMsg = "";
    long hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String hMCurpModPgm = "";
    String hMCurpModTime = "";
    String hMCurpModUser = "";
    long hMCurpModSeqno = 0;

    String hBusiBusinessDate = "";
    String hSystemChiDate = "";
    String hParmMonth = "";
    String hBusinssDate = "";
    String hSystemDate = "";
    String hSystemMmddyy = "";
    String hSystemYddd = "";
    String hSystemDateF = "";
    String hMTempTxType = "";
    String hMContRealCardNo = "";
    String hMContPurchaseDate = "";
    double hMContTotAmt = 0;
    double hMContQty = 0;
    long hMContInstallTotTerm = 0;
    double hMTempTransRate = 0;
    String hMIdPSeqno = "";
    String hMCardCorpNo = "";
    String hMCardCorpActFlag = "";
    String hML030OpenDate = "";
    String hML030CustId = "";
    String hL030OpenDate = "";
    String hL030CustId = "";
    int hTotTerm = 0;
    String hTempX10 = "";
    double tempDouble = 0;

    int totCnt = 0;
    int realCnt = 0;

    public int mainProcess(String[] args) {

        try {

        	dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length > 1) {
                comc.errExit("Usage : BilA041 yyyymm", "");
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            // comcr.callbatch(0, 0, 0);
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            hModUser = comc.commGetUserID();


            if (args.length == 1) {
                String temstr = args[0];
                temstr = Normalizer.normalize(temstr, java.text.Normalizer.Form.NFKD);
                hParmMonth = temstr;
            } else {
            	if (!hBusiBusinessDate.equals(comcr.increaseDays((comc.getSubString(hBusiBusinessDate,0,6)+"07"), -1)) ) {
            		//每月6日執行, 遇假日提前至前一個營業日
            		showLogMessage("I", "", "["+hBusiBusinessDate+"]非執行日期: 每月6日執行, 遇假日提前至前一個營業日!!");
            		return 0;
            	}
            }
            showLogMessage("I", "", String.format("****  Process Month=[%s]\n", hParmMonth));

            daoTable  = "bil_contract_jcic";
            whereStr  = "where tx_date like ? || '%'  ";
            whereStr += "  and mod_pgm   = ?  ";
            setString(1, hParmMonth);
            setString(2, prgmId);
            deleteTable();

            selectBilContract();

            //TCB沒有這個項目, 如果後續有資料要再重新測試
            //selectActLnf0300();

            showLogMessage("I", "", String.format("\n** bil_contract 總筆數=[%d],run=[%d]\n"
                    , totCnt, realCnt));
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
    void commonRtn() throws Exception {

        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            errMsg = "select_ptr_businday  False !";
            comcr.errRtn(errMsg, "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }

        hBusiBusinessDate = hBusinssDate;
        hSystemMmddyy = "";
        hSystemChiDate = "";
        hParmMonth = "";
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_system_date,";
        sqlCmd += "to_char(sysdate,'mmddyy') h_system_mmddyy,";
        sqlCmd += "to_char(sysdate,'YDDD') h_system_yddd,";
        sqlCmd += "to_char(sysdate,'yyyymmddhh24miss') h_system_date_f,";
        sqlCmd += "to_char(add_months(sysdate,-1),'yyyymm') h_parm_month ";
        sqlCmd += " from dual ";
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            errMsg = "select_dual False!";
            comcr.errRtn(errMsg, "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hSystemDate = getValue("h_system_date");
            hSystemMmddyy = getValue("h_system_mmddyy");
            hSystemYddd = getValue("h_system_yddd");
            hSystemDateF = getValue("h_system_date_f");
            hParmMonth = getValue("h_parm_month");
        }
    }

    /***********************************************************************/
    void selectBilContract() throws Exception {
    	daoTable  = "bil_contract";
    	
        sqlCmd = "select ";
        sqlCmd += "'K' as tx_type,";
        sqlCmd += "a.card_no,";
        sqlCmd += "a.purchase_date,";
        sqlCmd += "a.tot_amt,";
        sqlCmd += "a.qty,";
        sqlCmd += "a.install_tot_term,";
        sqlCmd += "a.trans_rate,";
        sqlCmd += "a.id_p_seqno,";
        sqlCmd += "c.corp_no,";
        sqlCmd += "c.corp_act_flag ";
        sqlCmd += " from crd_card c, bil_contract a, bil_merchant b ";
        sqlCmd += "Where a.purchase_date  like ? || '%' ";
        sqlCmd += "  and a.mcht_no      = b.mcht_no ";
        sqlCmd += "  and b.trans_flag   = 'Y' ";
        sqlCmd += "  and c.card_no      = a.card_no ";

        setString(1, hParmMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hMTempTxType = getValue("tx_type", i);
            hMContRealCardNo = getValue("card_no", i);
            hMContPurchaseDate = getValue("purchase_date", i);
            hMContTotAmt = getValueDouble("tot_amt", i);
            hMContQty = getValueDouble("qty", i);
            hMContInstallTotTerm = getValueLong("install_tot_term", i);
            hMTempTransRate = getValueDouble("trans_rate", i);
            hMIdPSeqno = getValue("id_p_seqno", i);
            hMCardCorpNo = getValue("corp_no", i);
            hMCardCorpActFlag = getValue("corp_act_flag", i);
            totCnt++;

            if (totCnt % 10000 == 0 || totCnt == 1) {
                showLogMessage("I", "", String.format("Process record=[%d]\n", totCnt));
            }

            realCnt++;

            insertBilContractJcic();
        }

    }

    /***********************************************************************/
    void insertBilContractJcic() throws Exception {
        tempDouble = hMContTotAmt * hMContQty;

        setValue("TX_CODE"      , "A");
        setValue("TX_DATE"      , hMContPurchaseDate);
        setValue("ID_P_SEQNO"   , hMIdPSeqno);
        setValue("TX_TYPE"      , hMTempTxType);
        setValueDouble("tot_amt", tempDouble);
        setValueDouble("tx_rate", hMTempTransRate);
        setValueLong("tot_term" , hMContInstallTotTerm);
        setValue("crt_user"     , hModUser);
        setValue("crt_date"     , hSystemDate);
        setValue("CONFIRM_FLAG" , "Y");
        setValue("POST_FLAG"    , "N");
        setValue("mod_user"     , hModUser);
        setValue("mod_time"     , sysDate + sysTime);
        setValue("mod_pgm"      , prgmId);
        daoTable = "bil_contract_jcic";
        insertTable();

    }

    /***********************************************************************/
    void selectActLnf0300() throws Exception {
        hML030OpenDate = "";
        hML030CustId = "";

        sqlCmd = "select ";
        sqlCmd += "open_date,";
        sqlCmd += "cust_id ";
        sqlCmd += "from act_lnf030 ";
        sqlCmd += "Where open_date  like ? || '%' ";
        sqlCmd += "group by open_date, ";
        sqlCmd += "cust_id ";
        setString(1, hParmMonth);
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {
            hML030OpenDate = getValue("open_date", i);
            hML030CustId = getValue("cust_id", i);

            selectActLnf020();
        }

    }

    /***********************************************************************/
    void selectActLnf020() throws Exception {

        totCnt++;

        if (totCnt % 10000 == 0) {
            showLogMessage("I", "", String.format("Process record=[%d]\n", totCnt));
        }

        realCnt++;

        selectActLnf030();

        selectActLnf033();

        hTempX10 = hL030CustId;

        hMContTotAmt = 0;
        sqlCmd = "select sum(fact_amt) h_m_cont_tot_amt ";
        sqlCmd += " from act_lnf020 Where cust_id   = ? and contract   in (select contract from act_lnf030 Where open_date  = ? and cust_id   = ?) ";
        setString(1, hL030CustId);
        setString(2, hL030OpenDate);
        setString(3, hL030CustId);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hMContTotAmt = getValueDouble("h_m_cont_tot_amt");
        }

        hMTempTxType = "B";
        hMContPurchaseDate = hL030OpenDate;
        hMContQty = 1;

        insertBilContractJcic();

    }

    /***********************************************************************/
    void selectActLnf030() throws Exception {
        hMContRealCardNo = "";
        hMTempTransRate = 0;

        sqlCmd = "select loan_no,";
        sqlCmd += "int_rate_6 ";
        sqlCmd += " from act_lnf030  ";
        sqlCmd += "where open_date  = ?  ";
        sqlCmd += "and cust_id   = ?  ";
        sqlCmd += "and int_rate_6  in ( select max(int_rate_6) from act_lnf030 where open_date  = ?  ";
        sqlCmd += "and cust_id   = ?)  ";
        setString(1, hL030OpenDate);
        setString(2, hL030CustId);
        setString(3, hL030OpenDate);
        setString(4, hL030CustId);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hMContRealCardNo = getValue("loan_no");
            hMTempTransRate = getValueDouble("int_rate_6");
        }

        hMContInstallTotTerm = hTotTerm;
    }

    /***********************************************************************/
    void selectActLnf033() throws Exception {
        hTotTerm = 0;
        sqlCmd = "select tot_term ";
        sqlCmd += " from act_lnf033  ";
        sqlCmd += "where loan_no = ?  ";
        setString(1, hMContRealCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTotTerm = getValueInt("tot_term");
        }

        hMContInstallTotTerm = hTotTerm;
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA041 proc = new BilA041();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
