/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-08-29  V1.00.00     Ryan     initial                                            *
* 111-08-31  V1.00.01     Ryan     移除 A.MOD_PGM <> 'ccam2012                          *
 **************************************************************************************/

package Tmp;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommDate;


public class TmpC003 extends AccessDAO {
	private final String progname = "補寫因CcaOutGoing、CrdG008該寫未寫入outgoing的票證掛失資料處理程式 111/08/31 V.00.01";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	String modUser = "";
	int n = 0;	
	int totalCnt = 0;
	boolean ibDebit = true;
	private String hCardNo = "";
	private String hBinType = "";
	private String hNegDelDate = "";
	private String hElectronicCode = "";
	private String hNewEndDate = "";
	private String hCurrentCode = "";
	private String hElectronicCardNo = "";
	private String hOppostDate = "";
	private String hOppostReason = "";

	public int mainProcess(String[] args) {

		try {
			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);
			// =====================================

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			showLogMessage("I", "","-->connect DB: " + getDBalias()[0]);

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			selectCcaOpposition();
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理結果 ,筆數 = %s", totalCnt));
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
	
	
	void selectCcaOpposition() throws Exception {

		sqlCmd = " SELECT A.CARD_NO " + 
				"FROM CCA_OPPOSITION A,CRD_CARD B " + 
				"WHERE A.CARD_NO = B.CARD_NO " + 
				"AND A.OPPO_TYPE = '2' " + 
				"AND B.ELECTRONIC_CODE = '01' " + 
//				"AND A.MOD_PGM <> 'ccam2012' " + 
				"AND A.CARD_NO NOT IN (SELECT CARD_NO FROM CCA_OUTGOING WHERE KEY_VALUE = 'TSCC') " + 
				"AND A.CARD_NO IN (SELECT CARD_NO FROM TSC_CARD WHERE CURRENT_CODE='2') " + 
				"UNION " + 
				"SELECT A.CARD_NO " + 
				"FROM CCA_OPPOSITION A,CRD_CARD B " + 
				"WHERE A.CARD_NO = B.CARD_NO " + 
				"AND A.OPPO_TYPE = '2' " + 
				"AND B.ELECTRONIC_CODE = '02' " + 
//				"AND A.MOD_PGM <> 'ccam2012' " + 
				"AND A.CARD_NO NOT IN (SELECT CARD_NO FROM CCA_OUTGOING WHERE KEY_VALUE = 'IPASS') " + 
				"AND A.CARD_NO IN (SELECT CARD_NO FROM IPS_CARD WHERE CURRENT_CODE='2') " + 
				"UNION " + 
				"SELECT A.CARD_NO " + 
				"FROM CCA_OPPOSITION A,CRD_CARD B " + 
				"WHERE A.CARD_NO = B.CARD_NO " + 
				"AND A.OPPO_TYPE = '2' " + 
				"AND B.ELECTRONIC_CODE = '03' " + 
//				"AND A.MOD_PGM <> 'ccam2012' " + 
				"AND A.CARD_NO NOT IN (SELECT CARD_NO FROM CCA_OUTGOING WHERE KEY_VALUE = 'ICASH') " + 
				"AND A.CARD_NO IN (SELECT CARD_NO FROM ICH_CARD WHERE CURRENT_CODE='2') " + 
				"UNION " + 
				"SELECT A.CARD_NO " + 
				"FROM CCA_OPPOSITION A,DBC_CARD B " + 
				"WHERE A.CARD_NO = B.CARD_NO " + 
				"AND A.OPPO_TYPE = '2' " + 
				"AND B.ELECTRONIC_CODE = '01' " + 
//				"AND A.MOD_PGM <> 'ccam2012' " + 
				"AND A.CARD_NO NOT IN (SELECT CARD_NO FROM CCA_OUTGOING WHERE KEY_VALUE = 'TSCC') " + 
				"AND A.CARD_NO IN (SELECT VD_CARD_NO FROM TSC_VD_CARD WHERE CURRENT_CODE='2') " ;
		
		this.openCursor();

		while (fetchTable()) {
			initData();
			hCardNo = getValue("card_no");
			n = selectCardData();
			if(n == -1) {
				continue;
			}
			if(hElectronicCode.equals("01")) {
				oppoTscReq();
			}
			if(hElectronicCode.equals("02")) {
				oppoIpsReq();
			}
			if(hElectronicCode.equals("03")) {
				oppoIchReq();
			}
			totalCnt ++;
			commitDataBase();
		}
		this.closeCursor();
	}


	/***********************************************************************/
	void insertCcaOutgoing(String keyValue) throws Exception {

		setValue("card_no", hCardNo);
		setValue("key_value", keyValue);
		setValue("key_table", "OPPOSITION");
		setValue("bitmap", "");
		setValue("act_code", "1");
		setValue("proc_flag", "1");
		setValue("send_times", "1");
		setValue("proc_date", sysDate);
		setValue("proc_time", sysTime);
		setValue("proc_user", modUser);
		setValue("data_from", "1");
		setValue("resp_code", "");
		setValue("data_type", "OPPO");
		setValue("bin_type", hBinType);
		setValue("reason_code", "");//
		setValue("del_date", hNegDelDate);
		setValue("bank_acct_no", "");
		setValue("vmj_regn_data", "");//
		setValue("vip_amt", "0");
		setValue("electronic_card_no", hElectronicCardNo);
		setValue("current_code", hCurrentCode);
		setValue("new_end_date", hNewEndDate);
		setValue("oppost_date", hOppostDate);
		setValue("oppost_reason", hOppostReason);
		setValue("v_card_no", "");
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("crt_user", modUser);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		setValueInt("mod_seqno", 1);
		
		daoTable = "cca_outgoing";

		insertTable();
		
		if (dupRecord.equals("Y")) {
			comcr.errRtn(String.format("insert cca_outgoing error,card_no = [%s]", hCardNo), "", "");
		}
	}
	
	private boolean isDebitcard(String cardNo) throws Exception {
		if (cardNo.length() < 6)
			return false;

		sqlCmd = "select count(*) as xx_cnt" + " from ptr_bintable"
				+ " where ? between rpad(bin_no||bin_no_2_fm,16,'0') and rpad(bin_no||bin_no_2_to,16,'9')"
				+ " and debit_flag ='Y'";
		setString(1, cardNo);
		int recordCnt = selectTable();
		if (recordCnt <= 0)
			return false;

		if (getValueDouble("xx_cnt") > 0)
			return true;

		return false;
	}
	
	/*************************************************************************/
	int selectCardData() throws Exception {
		ibDebit = isDebitcard(hCardNo);
		if (ibDebit) {
			sqlCmd = "select C.bin_type , C.current_code ,uf_date_add(C.new_end_date,0,0,1) as neg_del_date "
					+ " ,C.id_p_seqno ,C.electronic_code ,C.new_end_date ,C.oppost_date ,C.oppost_reason "
					+ " from dbc_idno A, dba_acno B, dbc_card C " + " where card_no = ? "
					+ " and A.id_p_seqno = C.id_p_seqno and B.p_seqno = C.p_seqno ";
		} else {
			sqlCmd = "select C.bin_type , C.current_code ,uf_date_add(C.new_end_date,0,0,1) as neg_del_date "
					+ " ,C.electronic_code ,C.new_end_date ,C.oppost_date ,C.oppost_reason " 
					+ " from crd_idno A, act_acno B, crd_card C "
					+ " where card_no = ? " + " and A.id_p_seqno = C.id_p_seqno and B.acno_p_seqno = C.acno_p_seqno ";
		}

		setString(1, hCardNo);
		int recordCnt = selectTable();

		if (recordCnt <= 0) {
			showLogMessage("I", "", String.format("select CRD[DBC]_CARD not found,card_no = [%s]", hCardNo));
			return -1;
		}
		hBinType = getValue("bin_type");
		hNegDelDate = getValue("neg_del_date");
		hElectronicCode = getValue("electronic_code");
		hNewEndDate = getValue("new_end_date");
		hCurrentCode = getValue("current_code");
		hOppostDate = getValue("oppost_date");
		hOppostReason = getValue("oppost_reason");
		return 1;
	}
	
	/***********************************************************************/
	void oppoTscReq() throws Exception {
		sqlCmd = "select tsc_card_no ";
		sqlCmd += " from tsc_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and card_no = ? ";
		if (ibDebit) {
			sqlCmd = "select tsc_card_no ";
			sqlCmd += " from tsc_vd_card where new_end_date > to_char(sysdate,'yyyymm') ";
			sqlCmd += " and vd_card_no = ? ";
		}
		sqlCmd += " and current_code = '2' ";
		setString(1, hCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("tsc_card_no");
			insertCcaOutgoing("TSCC");
		}
	}
	
	
	/***********************************************************************/
	void oppoIpsReq() throws Exception {
		sqlCmd = "select ips_card_no " + " from ips_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and current_code = '2' ";
		sqlCmd += " and card_no = ? ";
		setString(1, hCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("ips_card_no");
			insertCcaOutgoing("IPASS");
		}
	}
	
	
	/***********************************************************************/
	void oppoIchReq() throws Exception {
		sqlCmd = "select ich_card_no " + " from ich_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and current_code = '2' ";
		sqlCmd += " and card_no = ? ";
		setString(1, hCardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("ich_card_no");
			insertCcaOutgoing("ICASH");
		}
	}
	
	/***********************************************************************/
	public void initData() {
		hCardNo = "";
		hBinType = "";
		hNegDelDate = "";
		hElectronicCode = "";
		hNewEndDate = "";
		hCurrentCode = "";
		hElectronicCardNo = "";
		hOppostDate = "";
		hOppostReason = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		TmpC003 proc = new TmpC003();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
