/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 111-05-30  V1.00.01     Ryan     initial                                            *
* 111-07-29  V1.00.02     Ryan     initial              修改CCA_OPPOSITION WHERE條件             *
* 111-09-30  V1.00.03     Ryan     initial              增加 update fisc_reason_code 以避免送NCCC交易時失敗
*                                                                                                                                                        新增processMajorCard() 處理附卡停掛送outgoing             *
* 112-03-13  V1.00.04     Ryan                   CCA_OPPOSITION 不需要update mod_pgm                                                                                                                                               
 **************************************************************************************/

package Cca;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommDate;


public class CcaB004 extends AccessDAO {
	private final String progname = "網銀及Etabs停掛資料寫入outgoing處理程式 111/09/30 V.00.03";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	CcaOutGoing ccaOutGoing = null;
	String modUser = "";
	int n = 0;	
	int totalCnt = 0;
	int majorCardTotalCnt = 0;
	boolean ibDebit = true;
	private String cardNo = "";
	private String oppoType = "";
	private String oppoDate = "";
	private String oppoStatus = "";

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
			ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
			
			selectCcaOpposition();
			commitDataBase();
			showLogMessage("I", "", String.format("程式處理結果 ,major card處理筆數 = %s", majorCardTotalCnt));
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

		sqlCmd = " select CARD_NO ";
		sqlCmd += " ,OPPO_TYPE ";
		sqlCmd += " ,OPPO_STATUS ";
		sqlCmd += " ,OPPO_DATE ";
		sqlCmd += " ,MOD_PGM ";
		sqlCmd += " from CCA_OPPOSITION ";
		sqlCmd += " WHERE (CRT_USER IN ('ECSCDA39','Etb0001') OR ((CRT_USER NOT IN ('ECSCDA39','Etb0001')) AND (MOD_PGM IN ('ECSCDA39','Etb0001')))) AND MOD_USER <> 'CcaB004' ";
		
		this.openCursor();

		while (fetchTable()) {
			initData();
			cardNo = getValue("card_no");
			oppoDate = getValue("oppo_date");
			oppoStatus = getValue("oppo_status");
			ibDebit = isDebitcard(cardNo);
			n = selectCardData(cardNo);
			if(n == -1) {
				continue;
			}
	
			n = ccaOutGoing.InsertCcaOutGoing(cardNo, oppoType, oppoDate, oppoStatus);
			if(n == 1) {
				updateCcaOpposition();
				totalCnt ++;
			}
			commitDataBase();
			
			processMajorCard();
		}
		this.closeCursor();
	}

	/**
	* @ClassName: CcaB004
	* @Description: 增加 update fisc_reason_code 以避免送NCCC交易時失敗
	* @Description: CCA_OPPOSITION 不需要update mod_pgm     
	* @Copyright : Copyright (c) DXC Corp. 2023. All Rights Reserved.
	* @Company: DXC Team.
	* @author Ryan
	* @version V1.00.03, Sep 30, 2022
	* @version V1.00.04, Mar 13, 2023
	*/
	void updateCcaOpposition() throws Exception {
		daoTable = "CCA_OPPOSITION";
		updateSQL = " MOD_USER = 'CcaB004' ,mod_time = sysdate  ";
		updateSQL += " ,fisc_reason_code = nvl((select fisc_opp_code from cca_opp_type_reason where opp_status = ? ),'') ";
		whereStr = " where card_no = ? ";
		setString(1, oppoStatus);
		setString(2, cardNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update CCA_OPPOSITION not found,card_no = [%s]", cardNo));
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
	
	private int selectCardData(String cardNo) throws Exception {
		if (ibDebit) {
			sqlCmd = "select current_code ,oppost_reason  "
					+ " from dbc_card  " + " where card_no = ? ";
		} else {
			sqlCmd = "select current_code ,oppost_reason  "
					+ " from crd_card  " + " where card_no = ? ";
		}

		setString(1, cardNo);
		int recordCnt = selectTable();

		if (recordCnt <= 0) {
			showLogMessage("I", "", String.format("select CRD[DBC]_CARD not found,card_no = [%s]", cardNo));
			return -1;
		}
		oppoType =  getValue("current_code");

		return 1;
	}
	
	/**
	* @ClassName: CcaB004
	* @Description: 新增processMajorCard() 處理附卡停掛送outgoing
	* @Copyright : Copyright (c) DXC Corp. 2022. All Rights Reserved.
	* @Company: DXC Team.
	* @author Ryan
	* @version V1.00.03, Sep 30, 2022
	*/
	private void processMajorCard() throws Exception {
		extendField = "OPPO.";
		if (ibDebit) {
			sqlCmd = "select b.card_no ,b.oppo_status ,b.oppo_date ,b.oppo_type "
					+ " from dbc_card a join cca_opposition b on a.card_no = b.card_no " 
					+ " where a.major_card_no = ? and a.sup_flag='1' and b.oppo_date = ? and b.mod_pgm = 'TR_CRD_SUP_STOP' ";
		} else {
			sqlCmd = "select b.card_no ,b.oppo_status ,b.oppo_date ,b.oppo_type "
					+ " from crd_card a join cca_opposition b on a.card_no = b.card_no " 
					+ " where a.major_card_no = ? and a.sup_flag='1' and b.oppo_date = ? and b.mod_pgm = 'TR_CRD_SUP_STOP' ";
		}
		
		setString(1, cardNo);
		setString(2, oppoDate);
		int recordCnt = selectTable();
		
		for(int i = 0 ; i < recordCnt ; i++) {
			cardNo = getValue("OPPO.card_no",i);
			oppoDate = getValue("OPPO.oppo_date",i);
			oppoStatus = getValue("OPPO.oppo_status",i);
			oppoType = getValue("OPPO.oppo_type",i);
			n = ccaOutGoing.InsertCcaOutGoing(cardNo, oppoType, oppoDate, oppoStatus);
			if(n == 1) {
				updateCcaOpposition();
				majorCardTotalCnt ++;
			}
			commitDataBase();
		}
		
	}
	
	/***********************************************************************/
	public void initData() {
		cardNo = "";
		oppoType = "";
		oppoDate = "";
		oppoStatus = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CcaB004 proc = new CcaB004();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
