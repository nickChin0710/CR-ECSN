/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 112-06-26    V1.00.00     Ryan     initial                                            *
* 112-08-16    V1.00.01     Wilson   增加簡訊預計發送日期時間                                                                                            *
* 112-09-08    V1.00.02     Wilson   調整簡訊內容                                                                                                                  *
 **************************************************************************************/

package Crd;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommDate;


public class CrdF083 extends AccessDAO {
	private final String progname = "COMBO卡續卡換卡簡訊通知寫入簡訊發送檔處理程式 112/09/08 V.00.02";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString commStr = new CommString();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	String modUser = "";
	String modPgm = "";
	String modTime = "";
	int totalCnt = 0;
	private String hMsgDept = "";
	private String hMsgUserid = "";
	private String hMsgId = "";
	private String hMsgDesc = "";
	private String hIdPSeqno = "";
	private String hPSeqno = "";
	private String hIdNo = "";
	private String hAcctType = "";
	private String hCardNo = "";
	private String hCellarPhone = "";
	private String hCellphoneCheckFlag = "";
	private String hChiName = "";
	private String hMsgSeqno = "";
	
	private String busDate = "";
	private String sendDate = "";
	private String sendTime = "";
	

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
			modPgm = javaProgram;
			modTime = sysDate + sysTime;
			comr = new CommRoutine(getDBconnect(), getDBalias());
			
			busDate = getBusiDate();
			String parm1 = "";
			if(args.length == 1) {
				if(!commDate.isDate(args[0])) {
					comc.errExit("參數日期格式輸入錯誤", "");
				}
				parm1 = args[0];
				busDate = parm1;
			}
			showLogMessage("I", "", String.format("輸入參數日期 = [%s]", parm1));
			showLogMessage("I", "", String.format("取得營業日 = [%s]", busDate));
			
			String procDay = getProcDay();

			if(!procDay.equals(commStr.right(busDate, 2))) {
				showLogMessage("I", "", String.format("本日[%s]非執行日[%s]", busDate,procDay));
				return 0;
			}
			
			sendDate = getSendDay();
			sendTime = "1600";
			
			showLogMessage("I", "", String.format("簡訊發送日期 = [%s],時間 = [%s]", sendDate,sendTime));
			
			selectSmsMsgId();
			selectCrdEmboss();
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
	
	/**
	 * 判斷26是不是假日
	 * @throws Exception *
	 * 
	 */
	private String getProcDay() throws Exception {
		int procDay = 26;
		while(true) {
			sqlCmd = "select HOLIDAY from PTR_HOLIDAY where HOLIDAY = ? ";
			setString(1,commStr.left(busDate, 6)+procDay);
			selectTable();
			if("Y".equals(notFound)) {
				break;
			}
			procDay--;
		}
		return String.format("%02d", procDay);
	}
	
	private String getSendDay() throws Exception {
		String tmpSendDate = busDate;
				
		while(true) {
			tmpSendDate = comcr.increaseDays(tmpSendDate,1);
			
			sqlCmd = "select HOLIDAY from PTR_HOLIDAY where HOLIDAY = ? ";
			setString(1,tmpSendDate);
			selectTable();
			if("Y".equals(notFound)) {
				break;
			}			
		}
		return tmpSendDate;
	}
	
	/***
	 * 讀取簡訊參數
	 * @throws Exception
	 */
	void selectSmsMsgId() throws Exception {
		sqlCmd = " SELECT MSG_DEPT ";
		sqlCmd += " ,MSG_USERID ";
		sqlCmd += " ,MSG_ID ";
		sqlCmd += " ,MSG_DESC ";
		sqlCmd += " FROM SMS_MSG_ID ";
		sqlCmd += " WHERE MSG_PGM = 'CRDF083' ";
		int n = selectTable();

		if(n > 0) {
			initData();
			hMsgDept = getValue("MSG_DEPT");
			hMsgUserid = getValue("MSG_USERID");
			hMsgId = getValue("MSG_ID");
			hMsgDesc = getValue("MSG_DESC");
		}

	}

	/***
	 * 讀取當日COMBO卡續卡成功的資料
	 * @throws Exception
	 */
	void selectCrdEmboss() throws Exception {
		sqlCmd = " SELECT B.ID_P_SEQNO ";
		sqlCmd += " ,B.P_SEQNO ";
		sqlCmd += " ,C.ID_NO ";
		sqlCmd += " ,B.ACCT_TYPE ";
		sqlCmd += " ,B.CARD_NO ";
		sqlCmd += " ,C.CELLAR_PHONE ";
		sqlCmd += " ,DECODE(C.CELLAR_PHONE,'','N','Y') AS CELLPHONE_CHECK_FLAG ";
		sqlCmd += " ,C.CHI_NAME ";
		sqlCmd += " FROM CRD_EMBOSS A,CRD_CARD B,CRD_IDNO C ";
		sqlCmd += " WHERE A.CARD_NO = B.CARD_NO ";
		sqlCmd += " AND B.ID_P_SEQNO = C.ID_P_SEQNO ";
		sqlCmd += " AND B.COMBO_INDICATOR = 'Y' ";
		sqlCmd += " AND A.EMBOSS_SOURCE IN ('3','4') ";
		sqlCmd += " AND SUBSTRING(A.OLD_END_DATE,1,6) = ? ";
		setString(1,comc.getSubString(busDate, 0, 6));
		this.openCursor();

		while (fetchTable()) {
			initData();
			hIdPSeqno = getValue("ID_P_SEQNO");
			hPSeqno = getValue("P_SEQNO");
			hIdNo = getValue("ID_NO");
			hAcctType = getValue("ACCT_TYPE");
			hCardNo = getValue("CARD_NO");
			hCellarPhone = getValue("CELLAR_PHONE");
			hCellphoneCheckFlag = getValue("CELLPHONE_CHECK_FLAG");
			hChiName = getValue("CHI_NAME");
			getsMsgSeqno();
			insertSmsMsgDtl();
			commitDataBase();
		}
		this.closeCursor();
	}
	
	/**
	 * @throws Exception *****************************************************************************************/
	void getsMsgSeqno() throws Exception {
		extendField = "seqno.";
		sqlCmd = " select lpad(to_char(ecs_modseq.nextval),10,'0') as sms_seqno from SYSIBM.SYSDUMMY1 ";
		selectTable();
		hMsgSeqno =  getValue("seqno.sms_seqno");
	}
	
	void insertSmsMsgDtl() throws Exception {
		
		String expireMonth = comc.getSubString(busDate, 0, 6);
						
		extendField = "DTL.";
		daoTable = "SMS_MSG_DTL";
		setValue("DTL.MSG_SEQNO", hMsgSeqno);
		setValue("DTL.MSG_DEPT", hMsgDept);
		setValue("DTL.MSG_USERID", hMsgUserid);
		setValue("DTL.MSG_PGM", "CRDF083");
		setValue("DTL.ID_P_SEQNO", hIdPSeqno);
		setValue("DTL.P_SEQNO", hPSeqno);
		setValue("DTL.ID_NO", hIdNo);
		setValue("DTL.ACCT_TYPE", hAcctType);
		setValue("DTL.CARD_NO", hCardNo);
		setValue("DTL.MSG_ID", hMsgId);
		setValue("DTL.CELLAR_PHONE", hCellarPhone);
		setValue("DTL.CELLPHONE_CHECK_FLAG", hCellphoneCheckFlag);
		setValue("DTL.CHI_NAME", hChiName);
		setValue("DTL.EX_ID", "");
		setValue("DTL.MSG_DESC", hMsgUserid + "," + hMsgId + "," + hCellarPhone +"," + String.format("%03d", commStr.ss2int(commStr.left(expireMonth, 4)) - 1911) +"," + commStr.right(expireMonth, 2));
//		setValue("DTL.MSG_DESC", hMsgDesc.replace("<#0>",String.format("%03d", commStr.ss2int(commStr.left(expireMonth, 4)) - 1911))
//				.replace("<#1>", commStr.right(expireMonth, 2)));
		setValue("DTL.MIN_PAY", "0");
		setValue("DTL.ADD_MODE", "B");
		setValue("DTL.RESEND_FLAG", "N");
		setValue("DTL.SEND_FLAG", "N");
		setValue("DTL.PRIOR_FLAG", "N");
		setValue("DTL.CREATE_TXT_DATE", "");
		setValue("DTL.CREATE_TXT_TIME", "");
		setValue("DTL.CHI_NAME_FLAG", "");
		setValue("DTL.PROC_FLAG", "N");
		setValue("DTL.SMS24_FLAG", "");
		setValue("DTL.BOOKING_DATE", sendDate);
		setValue("DTL.BOOKING_TIME", sendTime);
		setValue("DTL.APR_DATE", "");
		setValue("DTL.APR_USER", "");
		setValue("DTL.APR_FLAG", "N");
		setValue("DTL.CRT_DATE", sysDate);
		setValue("DTL.CRT_USER", modPgm);
		setValue("DTL.MOD_TIME",modTime);
		setValue("DTL.MOD_PGM", modPgm);
		setValue("DTL.MOD_USER", modPgm);
		setValue("DTL.MOD_SEQNO", "0");
		try {
			insertTable();
		} catch (Exception ex) {
			showLogMessage("E", "", "insert SMS_MSG_DTL error , CARD_NO = [" + hCardNo + "]");
			return;
		}

		totalCnt++;
	}
	
	
	/***********************************************************************/
	public void initData() {
		hIdPSeqno = "";
		hPSeqno = "";
		hIdNo = "";
		hAcctType = "";
		hCardNo = "";
		hCellarPhone = "";
		hCellphoneCheckFlag = "";
		hChiName = "";
		hMsgSeqno = "";
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CrdF083 proc = new CrdF083();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
