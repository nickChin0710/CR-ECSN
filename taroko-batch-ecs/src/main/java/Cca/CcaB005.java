/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
* 112-03-15  V1.00.00     Ryan     initial                                            *
* 112-08-16  V1.00.01     Wilson   讀取簡訊參數調整                                                                                                               *   
* 112-10-24  V1.00.02     Wilson   WHERE條件增加PIN_BLOCK <> ''                          *
* 112-11-07  V1.00.03     Wilson   改為判斷製卡日(新製卡)                                                                                                      *
* 112-12-05  V1.00.04     Wilson   CRT_USER寫入CCA_CARD_OPEN的OPEN_USER                 *
 **************************************************************************************/

package Cca;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.tcb.ap4.tool.Decryptor;

import bank.Auth.HpeUtil;
import bank.Auth.HSM.HsmUtil;

import com.CommDate;


public class CcaB005 extends AccessDAO {
	private final String progname = "開卡發送預借現金密碼通知簡訊處理程式 112/12/05 V.00.04";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommString comStr = new CommString();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommRoutine comr = null;
	String modUser = "";
	String modPgm = "CcaB005";
	int n = 0;	
	int totalCnt = 0;
	private String rowid = "";
	private String cardNo = "";
	private String binType = "";
	private String pvki = "";
	private String pinBlock = "";
	private double cardAcctIdx = 0;
	private String acnoPSeqno = "";
	private String idPSeqno = "";
	private String birthday = "";
	private String chiName = "";
	private String cellarPhone = "";
	private String telHome = "";
	private String telOffi = "";
	private String openUser = "";
    private String msgSeqNo = "";
    private int respStatus = 0;
    private String pvvHide = "";
    private String ccaPvv = "";
	private String ecsPvk1 = "";
	private String ecsPvk2 = "";
	private String pwSmsPostFlag = "";
	
	private String wfValue = "";
	private String visaPvka = "";
	private String visaPvkb = "";
	private String masterPvka = "";
	private String masterPvkb = "";
	private String jcbPvka = "";
	private String jcbPvkb = "";
	private int racalPort = 0;
    private String racalServer = "";
    private int racalPort1 = 0;
    private String racalServer1 = "";

    private String msgId = "";
    private String msgDesc = "";
    private String msgDescOut = "";
	private String msgDescLog = "";
    private String smsIP = "";
    private String smsUser = "";

    Socket socket = null;
    
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

			selectPtrHsmKeys();
			
            if (connectRacal() != 0) {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                comc.errExit("connect_racal error", "");
            }
			
			//讀取發送預借現金密碼簡訊起始時間
			selectPtrSysParm();
			
	 		//--取得 User、IP
	 		selectPtrSysParm2();
	 		
	 		//--取得簡訊內容
	 		getSmsContent();
			
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
	
    int connectRacal() {
        try {
            racalServer = racalServer1;
            racalPort = racalPort1;

            String host = racalServer1;
            int port = racalPort1;

            socket = new Socket(host, port);
        } catch (IOException e) {
            showLogMessage("I", "", "RACAL CONNECT error !!");
            return 1;
        }
        showLogMessage("I", "", "RACAL CONNECT ok !!");
        return (0);
    }
	
	/***
	 * 讀取發送預借現金密碼簡訊起始時間
	 */
	void selectPtrSysParm() throws Exception {
		extendField = "PTR_SYS_PARM1.";
		sqlCmd = " SELECT WF_VALUE ";
		sqlCmd += " FROM PTR_SYS_PARM ";
		sqlCmd += " WHERE WF_PARM = 'SYSPARM' ";
		sqlCmd += " AND WF_KEY = 'CCAB005' ";
		int n = selectTable();
		if(n>0) {
			wfValue = getValue("PTR_SYS_PARM1.WF_VALUE");
		}
	}
	
	/***
	 * 取得 User、IP
	 */
	void selectPtrSysParm2() throws Exception {
		extendField = "PTR_SYS_PARM2.";
		sqlCmd = " select wf_value , wf_value3 ";
		sqlCmd += " from ptr_sys_parm ";
		sqlCmd += " where 1=1 and wf_parm = 'SMS_CONNECT' and wf_key = 'SMS_URL' ";
		int n = selectTable();
		if(n<0) {
			showLogMessage("I", "", "取得 User、IP 失敗,");
		}
		smsIP = getValue("PTR_SYS_PARM2.wf_value");		
		smsUser = getValue("PTR_SYS_PARM2.wf_value3");
	}
	
	/***
	 * 讀取PTR_HSM_KEYS的PVKa、PVKb
	 */
	void selectPtrHsmKeys() throws Exception {
		sqlCmd = " SELECT VISA_PVKA,VISA_PVKB,MASTER_PVKA,MASTER_PVKB,JCB_PVKA,JCB_PVKB,HSM_PORT1,HSM_IP_ADDR1 ";
		sqlCmd += " FROM PTR_HSM_KEYS ";
		sqlCmd += " WHERE HSM_KEYS_ORG = '00000000' ";
		int n = selectTable();
		if(n>0) {
			visaPvka = getValue("VISA_PVKA");
			visaPvkb = getValue("VISA_PVKB");
			masterPvka = getValue("MASTER_PVKA");
			masterPvkb = getValue("MASTER_PVKB");
			jcbPvka = getValue("JCB_PVKA");
			jcbPvkb = getValue("JCB_PVKB");
	        racalPort1 = getValueInt("HSM_PORT1");
	        racalServer1 = getValue("HSM_IP_ADDR1");
		}
	}
	
	/****
	 * 取得簡訊內容
	 * @return
	 * @throws Exception
	 */
    int getSmsContent() throws Exception {

  	   sqlCmd = " select msg_desc , msg_id from sms_msg_id where msg_pgm = 'CCAB005' ";
  	   int n = selectTable();
  	   if(n <=0) {
  		  showLogMessage("I", "", " 查無簡訊參數 : CCAB005");
  		  return 1;
  	   }
  	   msgId = getValue("msg_id");
  	   msgDesc = getValue("msg_desc");
  	 
  	   return 0;
  	   	   	   
     }
	
	void getPVK(String binType){
		switch (binType) {
		case "V":
			ecsPvk1 = visaPvka;
			ecsPvk2 = visaPvkb;
			break;
		case "M":
			ecsPvk1 = masterPvka;
			ecsPvk2 = masterPvkb;
			break;
		case "J":
			ecsPvk1 = jcbPvka;
			ecsPvk2 = jcbPvkb;
			break;
		default:
			ecsPvk1 = "";
			ecsPvk2 = "";
			break;
		}
	}

	/*****
	 * CALL HSM(hsmCommandNG)將CRD_CARD.PIN_BLOCK產生還原PIN 
	 * @param cardNo
	 * @param pinBlock
	 * @return
	 * @throws Exception 
	 */
    int callRacalNG(String cardNo,String pinBlock) throws Exception {
        String sLResult = "";

        HsmUtil lHsmUtil = new HsmUtil(racalServer, racalPort);
        cardNo = String.format("%12.12s", comStr.mid(cardNo,3));
        try {

            sLResult = lHsmUtil.hsmCommandNG(cardNo, pinBlock);
            
            if ("00".equals(comStr.mid(sLResult, 0,2))) {
                showLogMessage("I", "", " call hsmCommandNG 成功，Result== " + sLResult.substring(2, sLResult.length()) + "]");
            } else {
                showLogMessage("I", "", " call hsmCommandNG 失敗，Result== " + sLResult + "]");
                return 1;
            }

        } catch (Exception e) {
            showLogMessage("I", "", " call hsmCommandNG Error HsmUtil !");
        }
 
        String smsPin = comStr.mid(sLResult, 2,4);
        
        //--加密
 	    pvvHide = HpeUtil.transPasswd(0, smsPin);
        return 0;
    }
    
    /****
     * CALL HSM(hsmCommandDG)用CRD_CARD.PIN_BLOCK產生PVV
     * @param ecsPvk1
     * @param ecsPvk2
     * @param pinBlock
     * @param cardNo
     * @param pvki
     * @return
     * @throws IOException
     */
    int callRacalDG(String ecsPvk1 ,String ecsPvk2 ,String pinBlock ,String cardNo ,String pvki) throws IOException {
        String sLResult = "";

        HsmUtil lHsmUtil = new HsmUtil(racalServer, racalPort);
        cardNo = String.format("%12.12s", comStr.mid(cardNo,3));
       
        try {

            sLResult = lHsmUtil.hsmCommandDG(ecsPvk1+ecsPvk2, pinBlock, cardNo, pvki);

            if ("00".equals(sLResult.substring(0, 2))) {
                showLogMessage("I", "", " call hsmCommandDG 成功，Result== " + sLResult.substring(2, sLResult.length()) + "]");
            } else {
                showLogMessage("I", "", " call hsmCommandDG 失敗，Result== " + sLResult + "]");
                return 1;
            }

        } catch (Exception e) {
            showLogMessage("I", "", " call hsmCommandDG Error HsmUtil !");
        }
        
        ccaPvv = comStr.mid(sLResult, 2,4);
        return 0;
    }
    
    void sendSms() throws Exception {
   	   //--替換變數           
   	   msgDescOut = msgDesc.replace("<#0>", ccaPvv);   	   
   	   msgDescOut = msgDescOut.replace("<#1>", comc.getSubString(cardNo, 12, 16));
    	
       pwSmsPostFlag = "N";
 	   String confPath = getEcsAcdpPath();
 	   Properties props = new Properties();
 		try (FileInputStream fis = new FileInputStream(confPath);) {
 			props.load(fis);
 			fis.close();
 		} catch (Exception e) {
 			showLogMessage("I", "", " sms load properties error");
 			return;
 		}
 		String pawd = props.getProperty("cr.sms").trim();
 		//--解密
 		Decryptor decrptor = new Decryptor();
 		try {
 			pawd = decrptor.doDecrypt(pawd);
 		} catch(Exception e) {
 			showLogMessage("I", "", " pawd error");
 			return;
 		}	   
 		
 		//--發送簡訊
 		CcaSmsSend24 sms = new CcaSmsSend24();
 		try {
 			sms.setName(chiName);
 			sms.setPhoneNumber(cellarPhone);
 			sms.setSmsBody(msgDescOut);
 			sms.setUserName(smsUser);
 			sms.setUserPd(pawd);
 			sms.setUrl(smsIP);
 			if (sms.sendSms() ==-1) {
 				showLogMessage("I", "", "發送簡訊失敗");
 				return;
 			}	else	{
 				msgSeqNo = sms.getMsgSeqNo();
 				respStatus = sms.getReptStatus();
 			}
 		} catch(Exception e) {
 			showLogMessage("I", "", "發送簡訊失敗");
		    return;
 		}
 		
 		pwSmsPostFlag = "Y";
    }
 
	/*********
	 * 讀取新卡已開卡且有申請預借現金密碼的資料
	 * @throws Exception
	 */
	void selectCcaOpposition() throws Exception {
		sqlCmd = " SELECT A.ROWID AS ROWID,A.CARD_NO,B.BIN_TYPE,B.PVKI,B.PIN_BLOCK,C.CARD_ACCT_IDX,B.ACNO_P_SEQNO, ";
		sqlCmd += " B.ID_P_SEQNO,D.BIRTHDAY,D.CHI_NAME,D.CELLAR_PHONE, ";
		sqlCmd += " D.HOME_AREA_CODE1||'-'||D.HOME_TEL_NO1||'-'||D.HOME_TEL_EXT1 AS TEL_HOME, ";
		sqlCmd += " D.OFFICE_AREA_CODE1||'-'||D.OFFICE_TEL_NO1||'-'||D.OFFICE_TEL_EXT1 AS TEL_OFFI, ";
		sqlCmd += " A.OPEN_USER ";
		sqlCmd += " FROM CCA_CARD_OPEN A,CRD_CARD B,CCA_CARD_BASE C,CRD_IDNO D ";
		sqlCmd += " WHERE A.CARD_NO = B.CARD_NO ";
		sqlCmd += " AND A.CARD_NO = C.CARD_NO ";
		sqlCmd += " AND B.ID_P_SEQNO = D.ID_P_SEQNO ";		
		sqlCmd += " AND B.REISSUE_DATE = '' ";
		sqlCmd += " AND B.CHANGE_DATE = '' ";
		sqlCmd += " AND B.APPLY_ATM_FLAG = 'Y' ";
		sqlCmd += " AND B.PIN_BLOCK <> '' ";
		sqlCmd += " AND A.PW_SMS_POST_FLAG <> 'Y' ";
		sqlCmd += " AND B.ORI_ISSUE_DATE >= ? ";
		setString(1,wfValue);
		
		this.openCursor();

		while (fetchTable()) {
			initData();
			rowid = getValue("ROWID");
			cardNo = getValue("CARD_NO");
			binType = getValue("BIN_TYPE");
			pvki = getValue("PVKI");
			pinBlock = getValue("PIN_BLOCK");
			cardAcctIdx = getValueDouble("CARD_ACCT_IDX");
			acnoPSeqno = getValue("ACNO_P_SEQNO");
			idPSeqno = getValue("ID_P_SEQNO");
			birthday = getValue("BIRTHDAY");
			chiName = getValue("CHI_NAME");
			cellarPhone = getValue("CELLAR_PHONE");
			telHome = getValue("TEL_HOME");
			telOffi = getValue("TEL_OFFI");
			openUser = getValue("OPEN_USER");
			
			getPVK(binType);
			if(callRacalNG(cardNo,pinBlock)==1)
				continue;
		
			if(callRacalDG(ecsPvk1,ecsPvk2,pinBlock,cardNo,pvki)==1)
				continue;

			updateCcaCardBase();
			updateCrdCard();
			sendSms();
			updateCcaCardOpen();
			insertCcaMsgLog();
			
			
			totalCnt ++;
			commitDataBase();
		}
		this.closeCursor();
	}

	/****
	 * PVV寫入CCA_CARD_BASE
	 * @throws Exception
	 */
	void updateCcaCardBase() throws Exception {
		daoTable = "CCA_CARD_BASE";
		updateSQL = " PVV = ? ";
		whereStr = " WHERE CARD_NO = ? ";
		setString(1, pvvHide);
		setString(2, cardNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update CCA_CARD_BASE not found,card_no = [%s]", cardNo));
		}
	}
	
	/****
	 * 將CRD_CARD. PASSWD_ERR_COUNT 預借現金密碼錯誤次數歸零
	 * @throws Exception
	 */
	void updateCrdCard() throws Exception {
		daoTable = "CRD_CARD";
		updateSQL = " PASSWD_ERR_COUNT = 0 ";
		whereStr = " WHERE CARD_NO = ? ";
		setString(1, cardNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update CRD_CARD not found,card_no = [%s]", cardNo));
		}
	}
	
	/*****
	 * 發送簡訊成功，寫入授權簡訊記錄檔CCA_MSG_LOG
	 * @throws Exception 
	 */
	void insertCcaMsgLog() throws Exception {
		if("N".equals(pwSmsPostFlag)) {
			return;
		}
		
	   	   //--替換變數           
		msgDescLog = msgDesc.replace("<#0>", "####");   	   
		msgDescLog = msgDescLog.replace("<#1>", comc.getSubString(cardNo, 12, 16));
		
		extendField = "CCA_MSG_LOG.";
		setValue("CCA_MSG_LOG.TX_DATE",sysDate);
		setValue("CCA_MSG_LOG.TX_TIME",sysTime);
		setValue("CCA_MSG_LOG.CARD_NO",cardNo);
		setValueDouble("CCA_MSG_LOG.CARD_ACCT_IDX",cardAcctIdx);
		setValue("CCA_MSG_LOG.ACNO_P_SEQNO",acnoPSeqno);
		setValue("CCA_MSG_LOG.ID_P_SEQNO",idPSeqno);
		setValue("CCA_MSG_LOG.MSG_TYPE","OPEN");
		setValue("CCA_MSG_LOG.BIRTHDAY",birthday);
		setValue("CCA_MSG_LOG.CHI_NAME",chiName);
		setValue("CCA_MSG_LOG.CELLAR_PHONE",cellarPhone);
		setValue("CCA_MSG_LOG.SEND_DATE",sysDate);
		setValue("CCA_MSG_LOG.PROC_CODE","0");
		setValue("CCA_MSG_LOG.APPR_PWD",msgSeqNo);
		setValue("CCA_MSG_LOG.SMS_CONTENT",msgDescLog);
		setValue("CCA_MSG_LOG.CRT_DATE",sysDate);
		setValue("CCA_MSG_LOG.MOD_PGM",modPgm);
		setValue("CCA_MSG_LOG.MOD_TIME",sysDate+sysTime);
		setValue("CCA_MSG_LOG.MSG_ID",msgId);
		setValue("CCA_MSG_LOG.CRT_USER",openUser);
		setValue("CCA_MSG_LOG.TEL_NO_H",comStr.mid(telHome, 0,20));
		setValue("CCA_MSG_LOG.TEL_NO_O",comStr.mid(telOffi, 0,20));
		daoTable = "CCA_MSG_LOG";
		int n = insertTable();
		if(n <= 0) {
			showLogMessage("I", "", String.format("insert CCA_MSG_LOG error,card_no = [%s]", cardNo));
		}
	}
	
	void updateCcaCardOpen() throws Exception {
		daoTable = "CCA_CARD_OPEN";
		updateSQL = " PW_SMS_POST_FLAG = ? ";
		updateSQL += " ,PW_SMS_POST_TIME = ? ";
		updateSQL += " ,MOD_TIME = timestamp_format(?,'yyyymmddhh24miss') ";
		updateSQL += " ,MOD_PGM = ? ";
		whereStr = " WHERE ROWID = ? ";
		setString(1, pwSmsPostFlag);
		setString(2, sysDate+sysTime);
		setString(3, sysDate+sysTime);
		setString(4, modPgm);
		setRowId(5, rowid);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update CCA_CARD_OPEN not found,card_no = [%s]", cardNo));
		}
	}
	
	/***********************************************************************/
	public void initData() {
		cardNo = "";
		cardNo = "";
		binType = "";
		pvki = "";
		pinBlock = "";
		cardAcctIdx = 0;
		acnoPSeqno = "";
		idPSeqno = "";
		birthday = "";
		chiName = "";
		cellarPhone = "";
		telHome = "";
		telOffi = "";
		rowid = "";
		
	    msgSeqNo = "";
	    respStatus = 0;
	    pvvHide = "";
	    ccaPvv = "";
		ecsPvk1 = "";
		ecsPvk2 = ""; 
	}

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CcaB005 proc = new CcaB005();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
