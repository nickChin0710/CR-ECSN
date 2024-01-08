/**************************************************************************************
 *                                                                                    *
 *                              MODIFICATION LOG                                      *
 *                                                                                    *
 *     DATE     Version    AUTHOR                       DESCRIPTION                   *
 *  ---------  --------- ----------- -------------------------------------------------*
 *  111/03/31  V1.00.00    Ryan      program initial                                  *
 *  111/04/06  V1.00.01    Justin    fix Portability Flaw: Locale Dependent Comparison*
 *  111/04/12  V1.00.02    Ryan      新增 code處裡                                                                                                             *
 *  111/04/13  V1.00.03    Ryan      cardCurrentCode = 0  才處裡                                                                    *
 *  111/04/15  V1.00.04    Ryan      NCCC格式修正                                                                                                             *
 *  111/04/18  V1.00.05    Ryan      org = 106,206,306才處理                          *
 *  111/04/22  V1.00.06    Ryan      up F code                                        *
 *  111/08/24  V1.00.07    Ryan      add T code                                        *
 *  111/08/26  V1.00.08    Ryan      票證current_code = 2 才處理                                                                       *
 *  111/10/21  V1.00.09    Ryan      T code上特指部分修改為需要送黑名單               *
 **************************************************************************************/

package Crd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommRoutine;

import Cca.CcaOutGoing;
import Dxc.Util.SecurityUtil;

import com.CommDate;
import com.CommFTP;

public class CrdG008 extends AccessDAO {
	private final String progname = "補etabs停掛卡資料程式  111/10/21 V1.00.09";
	CommFunction comm = new CommFunction();
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate commDate = new CommDate();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CcaOutGoing ccaOutGoing = null;
	protected com.CommString zzstr = new com.CommString();
	String modUser = "";

	String fileNameTxt = "BLOCK_";
	String fileNameTxt2 = "BLOCK_";
	ArrayList<String> dataList = new ArrayList<String>();
	HashMap<Integer,String> dataListOk = new HashMap<Integer,String>();
	private int totalCnt = 0;
	private int cardNotFoundCnt = 0;
	private int blockCodeSkipCnt = 0;
	private int outgoingSkipCnt = 0;
	private int CurrentCodeSkipCnt = 0;
	private int orgSkipCnt = 0;

	private boolean ibDebit = false;
	private String lineLength = "";
	private String fileCardNo = "";
	private String fileCode = "";
	private String fileReasonCode = "";
	private String fileDate = "";
	private String hCurrentCode = "";
	private String hOppostReason = "";
	private String hOppostDate = "";
	private String hBinType = "";
	private String hNegDelDate = "";
	private String hLostFeeCode = "";
	private String hExcepFlag = "";
	private String hNegReason = "";
	private String hFiscReason = "";
	private String hVmjReason = "";
	private double hCardAcctIdx = 0;
	private String hCardType = "";
	private String hGroupCode = "";
	private String hAcctType = "";
	private String hAcnoPSeqno = "";
	private String hIdPSeqno = "";
	private String hSysDate = "";
	private String hElectronicCode = "";
	private String hElectronicCardNo = "";
	private String hNewEndDate = "";
	private String cardCurrentCode = "";
	private String fileOrg = "";
	private int isUpdate = 0;

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

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			modUser = comc.commGetUserID();
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());
			ccaOutGoing = new CcaOutGoing(getDBconnect(), getDBalias());
			
			commitDataBase();
			
			selectPtrBusinday();
			if(args.length == 1) {
				hSysDate = args[0];
			}

			if(readFile() == 1) {
				renameFile();
				outPutFile();
				procFTP();
				renameFile2();

				showLogMessage("I", "","");
				showLogMessage("I", "",String.format("CAR_CARD NOT FOUND CNT = [%s]", cardNotFoundCnt));
				showLogMessage("I", "",String.format("BLOCK CODE <> (A,F,L,S,O,B,E,X) SKIP CNT = [%s]", blockCodeSkipCnt));
				showLogMessage("I", "",String.format("ORG <> (106,206,306) SKIP CNT = [%s]", orgSkipCnt));
				showLogMessage("I", "",String.format("CURRENT_CODE <> 0 , SKIP CNT = [%s]", CurrentCodeSkipCnt));
				showLogMessage("I", "",String.format("CCA_OUTGOING NOT INSERT CNT = [%s]", outgoingSkipCnt));
				showLogMessage("I", "",String.format("TOTAL PROCESSED CNT = [%s]", totalCnt));
			}

			commitDataBase();
			
			// ==============================================
			// 固定要做的
			showLogMessage("I", "", "執行結束");
			finalProcess();
			ccaOutGoing.finalCnt2();
			return 0;
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return exceptExit;
		}
	}
	

	/***********************************************************************/
	public void selectPtrBusinday() throws Exception {
		sqlCmd = "select business_date from ptr_businday";
		int recordCnt = selectTable();
		hSysDate = getValue("business_date");
	}

	/***********************************************************************/
	int readFile() throws Exception {
		System.out.println("====Start Read File ====");
		BufferedReader br = null;
		try {
			String hSysDate1 = String.format("%s.TXT", Integer.parseInt(hSysDate) - 19110000);
			String hSysDate2 = String.format("%s_UPDATE.TXT", Integer.parseInt(hSysDate) - 19110000);
			fileNameTxt = fileNameTxt + hSysDate1;
			fileNameTxt2 = fileNameTxt2 + hSysDate2;
			String tmpStr = String.format("%s/media/crd/%s", comc.getECSHOME(), fileNameTxt);
			String tempPath = SecurityUtil.verifyPath(tmpStr);
			FileInputStream fis = new FileInputStream(new File(tempPath));
			br = new BufferedReader(new InputStreamReader(fis, "MS950"));

			System.out.println("  tempPath = [" + tempPath + "]");

		} catch (FileNotFoundException exception) {
			System.out.println("bufferedReader exception: " + exception.getMessage());
			return -1;
		}

		while ((lineLength = br.readLine()) != null) {
			isUpdate++;
			dataListOk.put(isUpdate-1, "N,");
			initData();
			if(getFileData() != 1) {
				continue;
			}
			if(!cardCurrentCode.equals("0")) {
				CurrentCodeSkipCnt ++;
				showLogMessage("I", "", String.format("current_code <> 0 ,card_no = [%s]", fileCardNo));
				continue;
			}
			selectOpptypeReason(hOppostReason);
			updateCrdCard(fileCardNo);
			insertCcaOpposition(fileCardNo);
			insertOnbat2ecs(fileCardNo);
			oppoNegReq(fileCardNo);
			oppoFiscReq(fileCardNo);
			if(hBinType.equals("J")) {
				oppoJcbReq(fileCardNo);
			}
			if(hBinType.equals("V")) {
				oppoVisaReq(fileCardNo);
			}
			if(hBinType.equals("M")) {
				oppoMaster2Req(fileCardNo);
			}
			if(hCurrentCode.equals("2")) {
				if(hElectronicCode.equals("01")) {
					oppoTscReq(fileCardNo);
				}
				if(hElectronicCode.equals("02")) {
					oppoIpsReq(fileCardNo);
				}
				if(hElectronicCode.equals("03")) {
					oppoIchReq(fileCardNo);
				}
			}
			totalCnt++;
			processDisplay(500);
			commitDataBase();
			dataListOk.put(isUpdate-1, "Y,");
		}
		br.close();
		return 1;
	}

	/*************************************************************************/
	int getFileData() throws Exception {
		String wordsStr = new String(lineLength.getBytes("MS950"), "MS950");
		String[] wordsArray = wordsStr.split(",");
		dataList.add(wordsStr);
		
		if(wordsArray.length<7) {
			return 0;
		}
		for (int i = 0; i < wordsArray.length; i++) {

			if (StringUtils.isNotBlank(wordsArray[i]) && wordsArray[i].toUpperCase(Locale.TAIWAN).equals("NULL")) {
				wordsArray[i] = "";
			}

		}
		fileOrg = wordsArray[0].trim();
		fileCardNo = wordsArray[3].trim();
		fileCode = wordsArray[4].trim();
		fileReasonCode = wordsArray[5].trim();
		fileDate = wordsArray[6].trim();
		hOppostDate = fileDate;

		if(zzstr.pos(",106,206,306", fileOrg)<=0) {
			orgSkipCnt++;
			showLogMessage("I", "", String.format("ORG <> (,106,206,306) ,card_no = [%s]", fileCardNo));
			return -1;
		}

		if(zzstr.empty(fileCode)) {
			return -1;
		}
		if(zzstr.pos(",A,F,L,S,O,B,E,X,T", fileCode)<=0) {
			blockCodeSkipCnt ++;
			showLogMessage("I", "", String.format("block code <> (A,F,L,S,O,B,E,X,T) ,card_no = [%s]", fileCardNo));
			return -1;
		}
		
		if(fileDate.length()==6) {
			if(fileDate.equals("000000")) {
				hOppostDate = "";
			}else {
				String yyDate = zzstr.mid(fileDate, 0,2);
				if(yyDate.compareTo("30")<0) {
					hOppostDate = "20" + fileDate;
				}else {
					hOppostDate = "19" + fileDate;
				}
			}
		}
		
		if(selectCardData(fileCardNo) != 1) {
			cardNotFoundCnt ++;
			return -1;
		}
		
		if (fileCode.equals("A")) {
			hCurrentCode = "1";
			hOppostReason = "A2";
		}
//		if (fileCode.equals("F")) {
//			hCurrentCode = "5";
//			hOppostReason = "N1";
//		}
		//20220422 update F code
		if (fileCode.equals("F")) {
			switch(fileReasonCode) {
			case "FM":
				hOppostReason = "M1";
				hCurrentCode = "5";
				break;
			case "FN":
				hOppostReason = "N1";
				hCurrentCode = "5";
				break;
			case "FO":
				hOppostReason = "AK";
				hCurrentCode = "5";
				break;
			default:
				hOppostReason = "M2";
				hCurrentCode = "5";
				break;
			}
		}
		if (fileCode.equals("L")) {
			hCurrentCode = "2";
			hOppostReason = "C1";
		}
		if (fileCode.equals("S")) {
			hCurrentCode = "2";
			hOppostReason = "S0";
		}
		if (fileCode.equals("O")) {
			hCurrentCode = "5";
			hOppostReason = "O1";
		}
		//20220412 add
		if (fileCode.equals("B")) {
			switch(fileReasonCode) {
			case "0":
			case "00":
				hOppostReason = "J2";
				hCurrentCode = "3";
				break;
			case "2":
			case "02":
				hOppostReason = "H1";
				hCurrentCode = "3";
				break;
			case "3":
			case "03":
				hOppostReason = "Z2";
				hCurrentCode = "3";
				break;
			case "4":
			case "04":
				hOppostReason = "U1";
				hCurrentCode = "3";
				break;
			case "5":
			case "05":
				hOppostReason = "B5";
				hCurrentCode = "3";
				break;
			case "6":
			case "06":
				hOppostReason = "B6";
				hCurrentCode = "3";
				break;
			default:
				hOppostReason = "30";
				hCurrentCode = "3";
				break;
			}
		}
		if (fileCode.equals("E")) {
			switch(fileReasonCode) {
			case "01":
				hOppostReason = "AP";
				hCurrentCode = "1";
				break;
			case "02":
				hOppostReason = "T1";
				hCurrentCode = "3";
				break;
			case "03":
				hOppostReason = "F1";
				hCurrentCode = "4";
				break;
			case "04":
				hOppostReason = "E4";
				hCurrentCode = "4";
				break;
			case "05":
				hOppostReason = "A1";
				hCurrentCode = "1";
				break;
			case "06":
				hOppostReason = "B3";
				hCurrentCode = "1";
				break;
			case "07":
				hOppostReason = "AA";
				hCurrentCode = "1";
				break;
			case "08":
				hOppostReason = "B4";
				hCurrentCode = "1";
				break;
			case "09":
				hOppostReason = "11";
				hCurrentCode = "1";
				break;
			case "10":
				hOppostReason = "AX";
				hCurrentCode = "1";
				break;
			case "11":
				hOppostReason = "EB";
				hCurrentCode = "4";
				break;
			case "12":
				hOppostReason = "C2";
				hCurrentCode = "4";
				break;
			case "13":
				hOppostReason = "ED";
				hCurrentCode = "4";
				break;
			case "14":
				hOppostReason = "EE";
				hCurrentCode = "4";
				break;
			case "15":
				hOppostReason = "R1";
				hCurrentCode = "1";
				break;
			case "44":
				hOppostReason = "AY";
				hCurrentCode = "1";
				break;
			default:
				hOppostReason = "10";
				hCurrentCode = "1";
				break;
			}
		}
		if (fileCode.equals("X")) {
			hCurrentCode = "1";
			hOppostReason = "AX";
		}
		//20220824 add T code 只上特指 不做停掛
		//2022-10-21 V1.00.09 Ryan  T code上特指部分修改為需要送黑名單
		if (fileCode.equals("T")) {
			if(zzstr.empty(fileReasonCode)) {
				boolean updateResult = updateSpecStatus(fileCardNo,hNewEndDate);
				if (updateResult) {
					boolean result = true;
					result = getSpecReason(hBinType, hCardType);
					if(result)
						insertCcaSpeHis(fileCardNo, hBinType ,hNewEndDate ,hOppostReason ,hNegReason);
					if(result) {
						insertRskAcnolog("C", fileCardNo, hNewEndDate); 
						dataListOk.put(isUpdate-1, "Y,");
						totalCnt++;
						commitDataBase();
					}
					if(result) {
						ccaOutGoing.InsertCcaOutGoingBlock(fileCardNo, hCurrentCode, sysDate, "09");
					}
				}
			}
			return 0;
		}

		return 1;
	}

	/**
	 * @throws Exception
	 ***********************************************************************/
	public boolean isDebitcard(String cardNo) throws Exception {
		String lsCardNo = cardNo;
		if (lsCardNo.length() < 6)
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
	int selectCardData(String cardNo) throws Exception {
		ibDebit = isDebitcard(fileCardNo);
		if (ibDebit) {
			sqlCmd = "select C.bin_type , C.current_code ,uf_date_add(C.new_end_date,0,0,1) as neg_del_date ,C.lost_fee_code ,C.card_type ,C.group_code ,C.acct_type "
					+ " ,C.p_seqno as acno_p_seqno ,C.id_p_seqno ,C.electronic_code ,C.new_end_date "
					+ " from dbc_idno A, dba_acno B, dbc_card C " + " where card_no = ? "
					+ " and A.id_p_seqno = C.id_p_seqno and B.p_seqno = C.p_seqno ";
		} else {
			sqlCmd = "select C.bin_type , C.current_code ,uf_date_add(C.new_end_date,0,0,1) as neg_del_date ,C.lost_fee_code ,C.card_type ,C.group_code ,C.acct_type "
					+ " ,C.acno_p_seqno ,C.id_p_seqno ,C.electronic_code ,C.new_end_date " 
					+ " from crd_idno A, act_acno B, crd_card C "
					+ " where card_no = ? " + " and A.id_p_seqno = C.id_p_seqno and B.acno_p_seqno = C.acno_p_seqno ";
		}

		setString(1, cardNo);
		int recordCnt = selectTable();

		if (recordCnt <= 0) {
			showLogMessage("I", "", String.format("select CRD[DBC]_CARD not found,card_no = [%s]", cardNo));
			return -1;
		}
		hBinType = getValue("bin_type");
		hNegDelDate = getValue("neg_del_date");
		hLostFeeCode = getValue("lost_fee_code");
		hCardType = getValue("card_type");
		hGroupCode = getValue("group_code");
		hAcctType = getValue("acct_type");
		hAcnoPSeqno = getValue("acno_p_seqno");
		hIdPSeqno = getValue("id_p_seqno");
		hElectronicCode = getValue("electronic_code");
		hNewEndDate = getValue("new_end_date");
		cardCurrentCode = getValue("current_code");
		return 1;
	}

	/**
	 * @throws Exception
	 ***********************************************************************/
	void updateCrdCard(String aCardNo) throws Exception {
		if (ibDebit == false) {
			daoTable = "crd_card";
		} else {
			daoTable = "dbc_card";
		}

		updateSQL = " current_code = ? , " + " oppost_date = ? , " + " oppost_reason = ? , "
				+ " mod_pgm = 'CrdG008' , mod_user = ? , mod_time = sysdate ";
		whereStr = " where card_no = ? ";
		
		setString(1, hCurrentCode);
		setString(2, hOppostDate);
		setString(3, hOppostReason);
		setString(4, modUser);
		setString(5, aCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update CRD[DBC]_CARD not found,card_no = [%s]", aCardNo));
		}

		wfDelSpecial(aCardNo);
	}

	/**
	 * @throws Exception
	 ***********************************************************************/
	void wfDelSpecial(String aCardNo) throws Exception {

		sqlCmd = "select spec_flag,card_acct_idx from cca_card_base" + " where card_no = ?";
		setString(1, aCardNo);
		int recordCnt = selectTable();
		if (recordCnt <= 0)
			return;

		hCardAcctIdx = getValueDouble("card_acct_idx");
		// -無特指, 偽卡停用-
		if (getValue("spec_flag").equals("Y"))
			return;

		// --
		daoTable = "cca_card_base";
		updateSQL = " spec_status =''" + ", spec_flag ='N'" + ", spec_mst_vip_amt =0" + ", spec_del_date =''"
				+ ", spec_remark =''" + ", mod_pgm = 'CrdG008' , mod_user = ? ";
		whereStr = " where card_no = ?";
		setString(1, modUser);
		setString(2, aCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update CCA_CARD_BASE not found,card_no = [%s]", aCardNo));
			return;
		}

		daoTable = "cca_special_visa";
		whereStr = " where card_no = ? ";
		setString(1, aCardNo);
		deleteTable();
		if (notFound.equals("Y")) {
//			showLogMessage("I", "", String.format("delete cca_special_visa not found,card_no = [%s]", aCardNo));
			return;
		}

		// -insert cca_spec_his-
		setValue("log_date", sysDate);
		setValue("log_time", sysTime);
		setValue("card_no", aCardNo);
		setValue("bin_type", hBinType);
		setValue("from_type", "1");
		setValue("aud_code", "D");
		setValue("pgm_id", "CrdG008");
		setValue("log_user", modUser);
		daoTable = "cca_spec_his";
		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", String.format("insert cca_spec_his error,card_no = [%s]", aCardNo));
			return;
		}
	}

	/**
	 * @throws Exception
	 ***********************************************************************/
	void insertCcaOpposition(String aCardNo) throws Exception {
		daoTable = "cca_opposition";
		updateSQL = "from_type = '1' ";
		updateSQL += ",oppo_type = ? ";
		updateSQL += ",oppo_status = ? ";
		updateSQL += ",oppo_user = ? ";
		updateSQL += ",oppo_date = ? ";
		updateSQL += ",oppo_time = to_char(sysdate,'hh24miss') ";
		updateSQL += ",neg_del_date = ? ";
		updateSQL += ",renew_flag = 'N' ";
		updateSQL += ",cycle_credit = '' ";
		updateSQL += ",opp_remark = '' ";
		updateSQL += ",mail_branch = '' ";
		updateSQL += ",lost_fee_flag = ? ";
		updateSQL += ",excep_flag = ? ";
		updateSQL += ",except_proc_flag = '0' ";
		updateSQL += ",neg_resp_code = '' ";
		updateSQL += ",visa_resp_code = '' ";
		updateSQL += ",mst_reason_code = ? ";
		updateSQL += ",vis_reason_code = ? ";
		updateSQL += ",fisc_reason_code = ? ";
		updateSQL += ",curr_tot_tx_amt = 0 ";
		updateSQL += ",curr_tot_cash_amt = 0 ";
		updateSQL += ",bank_acct_no = '' ";
		updateSQL += ",logic_del = '' ";
		updateSQL += ",logic_del_date = '' ";
		updateSQL += ",logic_del_time = '' ";
		updateSQL += ",logic_del_user = '' ";
		updateSQL += ",chg_date = ? ";
		updateSQL += ",chg_time = to_char(sysdate,'hh24miss') ";
		updateSQL += ",chg_user = ? ";
		updateSQL += ",mod_user = ? ";
		updateSQL += ",mod_pgm = 'CrdG008' ";
		whereStr = "where card_no = ? ";
		setString(1, hCurrentCode);
		setString(2, hOppostReason);
		setString(3, modUser);
		setString(4, hOppostDate);
		setString(5, hNegDelDate);
		setString(6, hLostFeeCode);
		setString(7, hExcepFlag);
		setString(8, hNegReason);
		setString(9, hVmjReason);
		setString(10, hFiscReason);
		setString(11, hOppostDate);
		setString(12, modUser);
		setString(13, modUser);
		setString(14, aCardNo);
		updateTable();
		if (notFound.equals("Y")) {
			setValue("card_no", aCardNo);
			setValueDouble("card_acct_idx", hCardAcctIdx);
			setValue("debit_flag", (ibDebit == true) ? "Y" : "N");
			setValue("card_type", hCardType);
			setValue("bin_type", hBinType);
			setValue("group_code", hGroupCode);
			setValue("from_type", "1");
			setValue("oppo_type", hCurrentCode);
			setValue("oppo_status", hOppostReason);
			setValue("oppo_user", modUser);
			setValue("oppo_date", hOppostDate);
			setValue("oppo_time", sysTime);
			setValue("neg_del_date", hNegDelDate);
			setValue("renew_flag", "N");
			setValue("cycle_credit", "");
			setValue("opp_remark", "");
			setValue("mail_branch", "");
			setValue("lost_fee_flag", hLostFeeCode);
			setValue("excep_flag", hExcepFlag);
			setValue("except_proc_flag", "0");
			setValue("neg_resp_code", "");
			setValue("visa_resp_code", "");
			setValue("mst_reason_code", hNegReason);
			setValue("vis_reason_code", hVmjReason);
			setValue("fisc_reason_code", hFiscReason);
			setValueDouble("curr_tot_tx_amt", 0);
			setValueDouble("curr_tot_cash_amt", 0);
			setValue("bank_acct_no", "");
			setValue("crt_date", sysDate);
			setValue("crt_time", sysTime);
			setValue("crt_user", modUser);
			setValue("mod_time", sysDate + sysTime);
			setValue("mod_user", modUser);
			setValue("mod_pgm", "CrdG008");
			setValueInt("mod_seqno", 1);
			daoTable = "cca_opposition";

			insertTable();

			if (dupRecord.equals("Y")) {
				showLogMessage("I", "", String.format("insert cca_opposition error,card_no = [%s]", aCardNo));
			}
		}

	}

	/**
	 * @throws Exception
	 ***********************************************************************/
	void selectOpptypeReason(String lsOppo) throws Exception {

		sqlCmd = "select neg_opp_reason, fisc_opp_code, vis_excep_code, mst_auth_code, jcb_excp_code"
				+ " from cca_opp_type_reason" + " where 1=1 " + " and opp_status = ?";
		setString(1, lsOppo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hExcepFlag = "Y";
//			hNegReason = getValue("neg_opp_reason");
			hFiscReason = getValue("fisc_opp_code");
			hNegReason = hFiscReason;
			if (hBinType.equals("V")) {
				hVmjReason = getValue("vis_excep_code");
				if(hNegReason.equals("Q")) {
					hNegReason = "U";
				}
			}
			if (hBinType.equals("M")) {
				hVmjReason = getValue("mst_auth_code");
				if(hNegReason.equals("Q")) {
					hNegReason = "U";
				}
			}
			if (hBinType.equals("J")) {
				hVmjReason = getValue("jcb_excp_code");
				if(hNegReason.equals("L")) {
					hNegReason = "41";
		    	}
		    	if(hNegReason.equals("S")) {
		    		hNegReason = "43";
		    	}
		    	if(hNegReason.equals("C")) {
		    		hNegReason = "04";
		    	}
		    	if(hNegReason.equals("F")) {
		    		hNegReason = "07";
		    	}
		    	if(hNegReason.equals("U")) {
		    		hNegReason = "05";
		    	}
		    	if(hNegReason.equals("R")) {
		    		hNegReason = "01";
		    	}
		    	if(hNegReason.equals("Q")) {
		    		hNegReason = "05";
		    	}
			}
		} else {
			hExcepFlag = "N";
			hNegReason = "";
			hFiscReason = "";
			hVmjReason = "";
		}

	}

	/**
	 * @throws Exception
	 *********************************************************************/
	void insertOnbat2ecs(String aCardNo) throws Exception {

		setValue("card_no", aCardNo);
		if (hCurrentCode.equals("3")) {
			setValue("oppo_type", "5");
		} else {
			setValue("oppo_type", "6");
		}

		setValue("to_which", "1");
		setValue("dog", sysDate + sysTime);
		setValue("proc_mode", "O");
		setValue("card_no", aCardNo);
		setValue("acct_type", hAcctType);
		setValue("acno_p_seqno", hAcnoPSeqno);
		setValue("id_p_seqno", hIdPSeqno);
		setValue("opp_type", hCurrentCode);
		setValue("opp_reason", hOppostReason);
		setValue("opp_date", hOppostDate);
		setValue("is_renew", "N");
		setValueInt("curr_tot_lost_amt", 100);
		setValue("mail_branch", "");
		setValue("lost_fee_flag", hLostFeeCode);
		setValue("debit_flag", (ibDebit == true) ? "Y" : "N");

		daoTable = "onbat_2ecs";

		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", String.format("insert onbat_2ecs error,card_no = [%s]", aCardNo));
		}
	}
	
	/**
	 * @throws Exception *********************************************************************/
	void oppoNegReq(String cardNo) throws Exception {
		insertCcaOutgoing(cardNo,"NCCC",hNegReason,"");
	}
	
	/**
	 * @throws Exception *********************************************************************/
	void oppoFiscReq(String cardNo) throws Exception {
		insertCcaOutgoing(cardNo,"FISC",hFiscReason,"");
	}
	
	/**
	 * @throws Exception *********************************************************************/
	void oppoVisaReq(String cardNo) throws Exception {
		if(!zzstr.empty(hVmjReason))
			insertCcaOutgoing(cardNo,"VISA",hVmjReason,"         ");
	}
	
	/**
	 * @throws Exception *********************************************************************/
	void oppoJcbReq(String cardNo) throws Exception {
		if(!zzstr.empty(hVmjReason))
			insertCcaOutgoing(cardNo,"JCB",hVmjReason,"00000");
	}

	/**
	 * @throws Exception *********************************************************************/
	void oppoMaster2Req(String cardNo) throws Exception {
		if(!zzstr.empty(hVmjReason))
			insertCcaOutgoing(cardNo,"MASTER2",hVmjReason,"");
	}

	/**
	 * @throws Exception *********************************************************************/
	void oppoTscReq(String cardNo) throws Exception {
		sqlCmd = "select tsc_card_no ";
		sqlCmd += " from tsc_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and card_no = ? ";
		if (ibDebit) {
			sqlCmd = "select tsc_card_no ";
			sqlCmd += " from tsc_vd_card where new_end_date > to_char(sysdate,'yyyymm') ";
			sqlCmd += " and vd_card_no = ? ";
		}
		sqlCmd += " and current_code = '2' ";
		setString(1, cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("tsc_card_no");
			insertCcaOutgoing(cardNo,"TSCC","","");
		}
	}
	
	
	/**
	 * @throws Exception *********************************************************************/
	void oppoIpsReq(String cardNo) throws Exception {
		sqlCmd = "select ips_card_no " + " from ips_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and current_code = '2' ";
		sqlCmd += " and card_no = ? ";
		setString(1, cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("ips_card_no");
			insertCcaOutgoing(cardNo,"IPASS","","");
		}
	}
	
	
	/**
	 * @throws Exception *********************************************************************/
	void oppoIchReq(String cardNo) throws Exception {
		sqlCmd = "select ich_card_no " + " from ich_card where new_end_date > to_char(sysdate,'yyyymm') ";
		sqlCmd += " and current_code = '2' ";
		sqlCmd += " and card_no = ? ";
		setString(1, cardNo);
		int recordCnt = selectTable();
		if (recordCnt > 0) {
			hElectronicCardNo = getValue("ich_card_no");
			insertCcaOutgoing(cardNo,"ICASH","","");
		}
	}
	

	/***********************************************************************/
	void insertCcaOutgoing(String cardNo , String keyValue , String reasonCode , String vmjRegnData) throws Exception {
		if(hOppostDate.compareTo("20220320")<=0) {
			outgoingSkipCnt ++;
			return;
		}

		setValue("card_no", cardNo);
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
		setValue("reason_code", reasonCode);//
		setValue("del_date", hNegDelDate);
		setValue("bank_acct_no", "");
		setValue("vmj_regn_data", vmjRegnData);//
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
		setValue("mod_pgm", "CrdG008");
		setValueInt("mod_seqno", 1);
		
		daoTable = "cca_outgoing";

		insertTable();
		
		if (dupRecord.equals("Y")) {
			comcr.errRtn(String.format("insert cca_outgoing error,card_no = [%s]", cardNo), "", "");
//			showLogMessage("I", "", String.format("insert cca_outgoing error,card_no = [%s]", cardNo));
		}
	}
	
	void outPutFile() throws Exception {
		int outPutFile = openOutputText(comc.getECSHOME() + "/media/crd/" + fileNameTxt2, "MS950");
		if(outPutFile<0) {
			comcr.errRtn("更新"+fileNameTxt2+"檔案失敗", "", "");
			return;
		}
		for(int i = 0 ; i < dataList.size() ; i++) {
			writeTextFile(outPutFile, dataListOk.get(i).toString() + dataList.get(i).toString() + "\r\n");
		}
		
		closeOutputText(outPutFile);
	}
	
	/***********************************************************************/
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2EMP"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crd", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + fileNameTxt2 + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2EMP", "mput " + fileNameTxt2);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + fileNameTxt2 + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(fileNameTxt2);
		}
	}
	
	/************************************************************************/
	public void renameFile() throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/crd/" + fileNameTxt;
		String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + fileNameTxt;

		
		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileNameTxt + "]更名失敗!" + tmpstr2);
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileNameTxt + "] 已移至 [" + tmpstr2 + "]");

	}
	
	/************************************************************************/
	public void renameFile2() throws Exception {
		String tmpstr1 = comc.getECSHOME() + "/media/crd/" + fileNameTxt2;
		String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + fileNameTxt2;

		
		if (comc.fileMove(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + fileNameTxt2 + "]更名失敗!" + tmpstr2);
			return;
		}
		showLogMessage("I", "", "檔案 [" + fileNameTxt2 + "] 已移至 [" + tmpstr2 + "]");

	}
	
	/***********************************************************************/
	public int insertEcsNotifyLog(String fileName) throws Exception {
		setValue("crt_date", sysDate);
		setValue("crt_time", sysTime);
		setValue("unit_code", comr.getObjectOwner("3", javaProgram));
		setValue("obj_type", "3");
		setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_name", "媒體檔名:" + fileName);
		setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
		setValue("notify_desc2", "");
		setValue("trans_seqno", commFTP.hEflgTransSeqno);
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		daoTable = "ecs_notify_log";

		insertTable();

		return (0);
	}
	
	
	/****************************************************************************/

	private boolean updateSpecStatus(String tmpCardNo ,String newEndDate) throws Exception {

		daoTable = "CCA_CARD_BASE";
		updateSQL =  " SPEC_FLAG = 'Y' ,";
		updateSQL += " SPEC_STATUS  = ? ,";
		updateSQL += " SPEC_DATE  = ? ,";
		updateSQL += " SPEC_TIME  = ? ,";
		updateSQL += " SPEC_USER  = ? ,";
		updateSQL += " SPEC_DEL_DATE  = ? ,";  
		updateSQL += " MOD_USER  = ? ,";
	    updateSQL += " MOD_TIME = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
	    updateSQL += " MOD_PGM  = ? ";
	    whereStr   = " where CARD_NO = ? ";
	    setString(1, "09");
	    setString(2, sysDate);
	    setString(3, sysTime);
	    setString(4, "CrdG008");
	    setString(5, newEndDate);
	    setString(6, "CrdG008");
	    setString(7, sysDate + sysTime);
	    setString(8, "CrdG008");
	    setString(9, tmpCardNo);

		updateTable();

		if (notFound.equals("Y")) {
			showLogMessage("I", "", String.format("update CCA_CARD_BASE not found, card_no[%s]",tmpCardNo));
			return false;
		}
		
		return true;
	}
	
	/***********************************************************************/
	private boolean getSpecReason(String binType, String tmpCardType) throws Exception {
		sqlCmd =  "SELECT VISA_REASON, MAST_REASON , JCB_REASON , SEND_IBM , NEG_REASON ";
		sqlCmd += "from cca_spec_code ";
		sqlCmd += "where spec_code = '09' ";
		int cnt = selectTable();

		if (cnt > 0) {
			switch (binType) {
			case "V":
				hOppostReason = getValue("VISA_REASON");
				break;
			case "M":
				hOppostReason = getValue("MAST_REASON");
				break;
			case "J":
				hOppostReason = getValue("JCB_REASON");
				break;
			}
			
			if ("VD".equals(tmpCardType)) {
				hNegReason = getValue("SEND_IBM");
			} else {
				hNegReason = getValue("NEG_REASON");
			}
			hFiscReason = hNegReason;
		}else {
			showLogMessage("I", "", String.format("SELECT cca_spec_code not found, where spec_code = '09' "));
			return false;
		}
		return true;
	}
	
	/****************************************************************************/

	private boolean insertCcaSpeHis(String tmpCardNo, String binType ,String newEndDate ,String specOutgoReason ,String specNegReason) throws Exception {
		setValue("log_date", sysDate);
		setValue("log_time", sysTime);
		setValue("card_no", tmpCardNo);
		setValue("bin_type", binType);
		setValue("from_type", "2");
		setValue("spec_status", "09");
		setValue("spec_del_date", newEndDate);
		setValue("spec_outgo_reason", specOutgoReason);
		setValue("spec_neg_reason", specNegReason);
		setValue("aud_code", "A");
		setValue("pgm_id", "CrdG008");
		setValue("log_user", "batch");
		daoTable = "cca_spec_his";
		int insertCnt = insertTable();
		if (insertCnt <= 0) {
			showLogMessage("I", "", String.format("update cca_spec_his not found, card_no[%s]",tmpCardNo));
			return false;
		}

		return true;

	}
	
	/****************************************************************************/

	private boolean insertRskAcnolog(String kindFlag, String tmpCardNo, String newEndDate) throws Exception {
		RskAcnologObj obj = getCcaCardBase(tmpCardNo, newEndDate);
		if (obj == null) {
			showLogMessage("W", "", "insertRskAcnolog: unable to getRskAcnologObj, card_no[" + tmpCardNo + "]");
			return false;
		}
		setValue("kind_flag", kindFlag);
		setValue("card_no", obj.cardNo);
		setValue("acno_p_seqno", obj.acnoPSeqno);
		setValue("acct_type", obj.acctType);
		setValue("id_p_seqno", obj.idPSeqno);
		setValue("corp_p_seqno", obj.corpPSeqno);
		setValue("log_date", sysDate);
		setValue("log_mode", "1");
		setValue("log_type", obj.logType);
		setValue("log_reason", obj.logReason);
		setValue("log_remark", obj.logRemark);
		setValue("fit_cond", obj.fitCond);
		setValue("block_reason", obj.blockReason);
		setValue("spec_status", obj.specStatus);
		setValue("spec_del_date", obj.specDelDate);
		setValue("sms_flag", obj.smsFlag);
		setValue("user_dept_no", obj.userDeptNo);
		setValue("send_ibm_flag", "");
		setValue("send_ibm_date", "");
		setValue("apr_flag", "Y");
		setValue("apr_date", sysDate);
		setValue("mod_user", "CrdG008");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", "CrdG008");
		daoTable = "RSK_ACNOLOG";
		int insertCnt = insertTable();
		if (insertCnt <= 0) {
			showLogMessage("I", "", String.format("insert RSK_ACNOLOG error, card_no[%s]",tmpCardNo));
			return false;
		}

		return true;
	}

	/****************************************************************************/
	private RskAcnologObj getCcaCardBase(String tmpCardNo, String newEndDate) throws Exception {
		RskAcnologObj obj = null;
		
		sqlCmd =  "SELECT acno_p_seqno, acct_type , id_p_seqno , corp_p_seqno , spec_remark, spec_dept_no ";
		sqlCmd += "from CCA_CARD_BASE ";
		sqlCmd += "where card_no = ? ";
		setString(1, tmpCardNo);

		int cnt = selectTable();
		if (cnt > 0) {
			obj = new RskAcnologObj();
			obj.cardNo = tmpCardNo;
			obj.acnoPSeqno = getValue("acno_p_seqno");
			obj.acctType = getValue("acct_type");
			obj.idPSeqno = getValue("id_p_seqno");
			obj.corpPSeqno = getValue("corp_p_seqno");
			obj.logType = "6";
			obj.logReason = "A";
			obj.fitCond = "Y";
			obj.logRemark = getValue("spec_remark");
			obj.blockReason = "";
			obj.specStatus = "09";
			obj.specDelDate = newEndDate;
			obj.smsFlag = "";
			obj.userDeptNo = getValue("spec_dept_no");
		}
		return obj;
	}
	
	/***********************************************************************/
	public void initData() {
		
		fileCardNo = "";
		fileCode = "";
		fileDate = "";
		hCurrentCode = "";
		hOppostReason = "";
		hOppostDate = "";
		hBinType = "";
		hNegDelDate = "";
		hLostFeeCode = "";
		hExcepFlag = "";
		hNegReason = "";
		hFiscReason = "";
		hVmjReason = "";
		hCardAcctIdx = 0;
		hCardType = "";
		hGroupCode = "";
		hAcctType = "";
		hAcnoPSeqno = "";
		hIdPSeqno = "";
		hSysDate = "";
		hElectronicCode = "";
		hElectronicCardNo = "";
		hNewEndDate = "";
		cardCurrentCode = "";
	}
	
    class RskAcnologObj{
    	String kindFlag = "";
    	String cardNo = "";
    	String acnoPSeqno = "";
    	String acctType = "";
    	String idPSeqno = "";
    	String corpPSeqno = "";
    	String logDate = "";
    	String logMode = "";
    	String logType = "";
    	String logReason = "";
    	String fitCond = "";
    	String logRemark = "";
    	String blockReason = "";
    	String specStatus = "";
    	String specDelDate = "";
    	String smsFlag = "";
    	String userDeptNo = "";
    }

	/***********************************************************************/
	public static void main(String[] args) throws Exception {
		CrdG008 proc = new CrdG008();
		int retCode = proc.mainProcess(args);
		proc.programEnd(retCode);
	}
}
