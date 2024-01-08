package Tmp;

/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
* 112/02/04  V0.00.01     JeffKung  initial                                  *
* 112/08/24  V0.00.02     JeffKung  update 1599 no fee                       *
*****************************************************************************/

import com.CommCrd;
import com.CommCrdRoutine;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.plexus.util.StringUtils;

import com.AccessDAO;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class TmpM011 extends AccessDAO {
	private String PROGNAME = "年費參數初始設定處理 112/08/24 V0.00.02";
	CommFunction comm = new CommFunction();
	CommString zzstr = new CommString();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	CommCrd comc = new CommCrd();
	CommCrdRoutine comcr = null;
	CommDate zzdate = new CommDate();

	String modUser = "";
	String hBusiBusinessDate = "";

	// for cyc_anul_gp
	String cntCond = "N";
	String cntSel = "2";
	int cntAccumlateCnt = 0;
	String cntMajorFlag = "N";
	String cntSubFlag = "N";
	String cntMajorSub = "N";

	String amtCond = "N";
	double amtAccumlateAmt = 0.0;
	String amtMajorFlag = "N";
	String amtSubFlag = "N";
	String amtMajorSub = "N";

	String emailNopaperFlag = "N";
	String minorHalfFlag = "N";
	String gCondFlag = "N";
	double gAccumlateAmt = 0.0;
	String hCondFlag = "N";
	double hAccumlateAmt = 0.0;

	public void mainProcess(String[] args) {
		try {
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + PROGNAME);
			// =====================================
			if (args.length != 0) {
				comc.errExit("Usage : TmpM010 ", "");
			}

			// 固定要做的

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}

			comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
			commFTP = new CommFTP(getDBconnect(), getDBalias());
			comr = new CommRoutine(getDBconnect(), getDBalias());

			selectPtrBusinday();

			processPtrGroupCard();

			processCycAnulGp();

			finalProcess();
		} catch (Exception ex) {
			expMethod = "mainProcess";
			expHandle(ex);
			return;
		}

	}

	void selectPtrBusinday() throws Exception {
		hBusiBusinessDate = "";

		sqlCmd = "select business_date ";
		sqlCmd += " from ptr_businday  ";
		sqlCmd += " fetch first 1 rows only ";
		int recordCnt = selectTable();
		if (notFound.equals("Y")) {
			comcr.errRtn("select_ptr_businday not found!", "", "");
		}
		if (recordCnt > 0) {
			hBusiBusinessDate = getValue("business_date");
		}

	}

	/***********************************************************************/
	void processPtrGroupCard() throws Exception {

		String groupCode = "";
		String cardType = "";
		String rowId = "";
		int firstFee = 0;
		int otherFee = 0;
		double supRate = 0.0;

		sqlCmd = " select ";
		sqlCmd += " group_code,card_type, ";
		sqlCmd += " rowid as rowid ";
		sqlCmd += " from ptr_group_card ";
		sqlCmd += " where 1=1 ";

		openCursor();
		while (fetchTable()) {
			groupCode = getValue("group_code");
			cardType = getValue("card_type");
			rowId = getValue("rowid");
			
			String[] noAfee= new String[] {"1611","1612","1672","1675","1687","1601","1604","1644",
					"5397","5398","5399","5546","5703",
					"1599","3750","3701","3702","3704","3713","3751","3752","3760","3770","3790",
					"3780","3781","3782","3783"};
			String[] specAFee= new String[] {"1677","1679","1680","1682","1683","1686","1688","1689",
					"1690","1691","1692","1655","1656","1893","1894"};
			
			if (ArrayUtils.contains(noAfee, getValue("group_code"))) {
				firstFee = 0;
				otherFee = 0;
				supRate = 0.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("VC".equals(cardType) || "VG".equals(cardType) || "MC".equals(cardType) || "MO".equals(cardType)
					|| "MG".equals(cardType) || "MQ".equals(cardType) || "UD".equals(cardType) || "JC".equals(cardType)
					|| "JG".equals(cardType) || "VD".equals(cardType)) {
				firstFee = 0;
				otherFee = 0;
				supRate = 0.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("1601".equals(groupCode) && "VP".equals(cardType)) { // 聯電聯名白金卡
				firstFee = 0;
				otherFee = 0;
				supRate = 0.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("1604".equals(groupCode) && "VP".equals(cardType)) { // 心兒白金卡
				firstFee = 0;
				otherFee = 0;
				supRate = 0.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("1644".equals(groupCode) && "MP".equals(cardType)) { // 靈鷲山白金卡
				firstFee = 0;
				otherFee = 0;
				supRate = 0.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("VP".equals(cardType) || "VR".equals(cardType) || // 白金卡
					"MP".equals(cardType) || "MR".equals(cardType) || "JP".equals(cardType)) {
				firstFee = 0;
				otherFee = 1200;
				supRate = 50.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if (ArrayUtils.contains(specAFee, getValue("group_code"))) {  //特別的聯名卡
				firstFee = 0;
				otherFee = 1200;
				supRate = 50.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("VS".equals(cardType) || "VT".equals(cardType) || // 御璽鈦金卡
					"MS".equals(cardType) || "MT".equals(cardType) || "JT".equals(cardType)) {
				firstFee = 0;
				otherFee = 1800;
				supRate = 50.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("VB".equals(cardType) || "VE".equals(cardType) || // 商務卡
					"MB".equals(cardType) || "ME".equals(cardType)) {
				firstFee = 0;
				otherFee = 1000;
				supRate = 0.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("MI".equals(cardType)) { // 世界卡
				firstFee = 0;
				otherFee = 10000;
				supRate = 0.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("VI".equals(cardType) && "1622".equals(groupCode)) { // VISA無限金鑽卡
				firstFee = 0;
				otherFee = 15000;
				supRate = 0.0;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			} else if ("VI".equals(cardType)) { // 無限卡
				firstFee = 0;
				otherFee = 8000;
				supRate = 62.5;
				updatePtrGroupCard(firstFee, otherFee, supRate, rowId);
			}

		}

		closeCursor();

	}

	void updatePtrGroupCard(int firstFee, int otherFee, double supRate, String rowId) throws Exception {

		daoTable = "ptr_group_card";
		updateSQL = "first_fee = ? ,";
		updateSQL += " other_fee = ? ,";
		updateSQL += " sup_rate = ? ";
		whereStr = "where rowid = ? ";
		setInt(1, firstFee);
		setInt(2, otherFee);
		setDouble(3, supRate);
		setRowId(4, rowId);
		updateTable();
		if (notFound.equals("Y")) {
			showLogMessage("E", "", String.format("ptr_group_card異動失敗,groupCode=[%s]", getValue("group_code")));
		}

	}

	/***********************************************************************/
	void processCycAnulGp() throws Exception {

		String groupCode = "";
		String cardType = "";
		String rowId = "";

		sqlCmd = " select ";
		sqlCmd += " group_code,card_type, ";
		sqlCmd += " rowid as rowid ";
		sqlCmd += " from ptr_group_card ";
		sqlCmd += " where 1=1 ";

		openCursor();
		while (fetchTable()) {
			groupCode = getValue("group_code");
			cardType = getValue("card_type");
			rowId = getValue("rowid");

			cntCond = "N";
			cntSel = "2";
			cntAccumlateCnt = 0;
			cntMajorFlag = "N";
			cntSubFlag = "N";
			cntMajorSub = "N";

			amtCond = "N";
			amtAccumlateAmt = 0.0;
			amtMajorFlag = "N";
			amtSubFlag = "N";
			amtMajorSub = "N";

			emailNopaperFlag = "N";
			minorHalfFlag = "N";
			gCondFlag = "N";
			gAccumlateAmt = 0.0;
			hCondFlag = "N";
			hAccumlateAmt = 0.0;
			

			if ("VC".equals(cardType) || "VG".equals(cardType) || "MC".equals(cardType) || "MO".equals(cardType)
					|| "MG".equals(cardType) || "MQ".equals(cardType) || "UD".equals(cardType) || "JC".equals(cardType)
					|| "JG".equals(cardType) || "VD".equals(cardType)) {
				continue;
			}

			if ("1601".equals(groupCode) && "VP".equals(cardType)) { // 聯電聯名白金卡
				continue;
			}

			if ("1604".equals(groupCode) && "VP".equals(cardType)) { // 心兒白金卡
				continue;
			}

			if ("1644".equals(groupCode) && "MP".equals(cardType)) { // 靈鷲山白金卡
				continue;
			}
			
			if ("1687".equals(groupCode) ) { // 中科大聯名卡免年費
				continue;
			}
			
			if ("1672".equals(groupCode) ) { // 北商大聯名卡免年費
				continue;
			}
			
			if ("1675".equals(groupCode) ) { // 北商大聯名卡免年費
				continue;
			}
			
			if ("1611".equals(groupCode) ) { // 嘉南藥理大學御璽聯名卡免年費
				continue;
			}
			
			if ("1612".equals(groupCode) ) { // 喜鴻假期聯名卡個人卡免年費
				continue;
			}
			
			String[] noAfee= new String[] {"1611","1612","1672","1675","1687","1601","1604","1644",
					"5397","5398","5399","5546","5703",
					"1599","3750","3701","3702","3704","3713","3751","3752","3760","3770","3790",
					"3780","3781","3782","3783"};
			if (ArrayUtils.contains(noAfee, groupCode)) {
				continue;
			}
			
			String[] specAFee= new String[] {"1677","1679","1680","1682","1683","1686","1688","1689",
					"1690","1691","1692","1655","1656","1893","1894"};
			
			if (ArrayUtils.contains(specAFee, groupCode)) {
				cntCond = "Y";
				cntSel = "2";
				cntAccumlateCnt = 1;
				cntMajorFlag = "N";
				cntSubFlag = "N";
				cntMajorSub = "Y";

				amtCond = "N";
				amtAccumlateAmt = 0.0;
				amtMajorFlag = "N";
				amtSubFlag = "N";
				amtMajorSub = "N";

				minorHalfFlag = "N";

				gCondFlag = "N";
				gAccumlateAmt = 0.0;
				hCondFlag = "N";
				hAccumlateAmt = 0.0;

				insertCycAnulGp();
				continue;
			}

			if ("VP".equals(cardType) || "VR".equals(cardType) || // 白金卡
					"MP".equals(cardType) || "MR".equals(cardType) || "JP".equals(cardType)) {
				cntCond = "Y";
				cntSel = "2";
				cntAccumlateCnt = 1;
				cntMajorFlag = "N";
				cntSubFlag = "N";
				cntMajorSub = "Y";

				amtCond = "N";
				amtAccumlateAmt = 0.0;
				amtMajorFlag = "N";
				amtSubFlag = "N";
				amtMajorSub = "N";

				minorHalfFlag = "N";

				gCondFlag = "N";
				gAccumlateAmt = 0.0;
				hCondFlag = "N";
				hAccumlateAmt = 0.0;

				insertCycAnulGp();
				continue;
			}

			if ("VS".equals(cardType) || "VT".equals(cardType) || // 御璽鈦金卡
					"MS".equals(cardType) || "MT".equals(cardType)) {
				cntCond = "Y";
				cntSel = "2";
				cntAccumlateCnt = 8;
				cntMajorFlag = "N";
				cntSubFlag = "N";
				cntMajorSub = "Y";

				amtCond = "Y";
				amtAccumlateAmt = 80000.0;
				amtMajorFlag = "N";
				amtSubFlag = "N";
				amtMajorSub = "Y";

				minorHalfFlag = "Y";

				gCondFlag = "N";
				gAccumlateAmt = 0.0;
				hCondFlag = "N";
				hAccumlateAmt = 0.0;

				insertCycAnulGp();
				continue;
			}

			if ("JT".equals(cardType)) { // JCB晶緻卡
				cntCond = "Y";
				cntSel = "2";
				cntAccumlateCnt = 6;
				cntMajorFlag = "N";
				cntSubFlag = "N";
				cntMajorSub = "Y";

				amtCond = "Y";
				amtAccumlateAmt = 60000.0;
				amtMajorFlag = "N";
				amtSubFlag = "N";
				amtMajorSub = "Y";

				minorHalfFlag = "Y";

				gCondFlag = "N";
				gAccumlateAmt = 0.0;
				hCondFlag = "N";
				hAccumlateAmt = 0.0;

				insertCycAnulGp();
				continue;
			}

			if ("VB".equals(cardType) || "VE".equals(cardType) || // 商務卡
					"MB".equals(cardType) || "ME".equals(cardType)) {
				cntCond = "Y";
				cntSel = "2";
				cntAccumlateCnt = 2;
				cntMajorFlag = "Y";
				cntSubFlag = "Y";
				cntMajorSub = "N";

				amtCond = "Y";
				amtAccumlateAmt = 100000.0;
				amtMajorFlag = "N";
				amtSubFlag = "N";
				amtMajorSub = "Y";

				minorHalfFlag = "N";

				gCondFlag = "N";
				gAccumlateAmt = 0.0;
				hCondFlag = "N";
				hAccumlateAmt = 0.0;

				insertCycAnulGp();
				continue;
			}

			if ("MI".equals(cardType)) { // 世界卡
				cntCond = "N";
				cntSel = "2";
				cntAccumlateCnt = 0;
				cntMajorFlag = "N";
				cntSubFlag = "N";
				cntMajorSub = "N";

				amtCond = "Y";
				amtAccumlateAmt = 200000.0;
				amtMajorFlag = "N";
				amtSubFlag = "N";
				amtMajorSub = "Y";

				minorHalfFlag = "Y";

				gCondFlag = "Y";
				gAccumlateAmt = 80000.0;
				hCondFlag = "N";
				hAccumlateAmt = 0.0;

				insertCycAnulGp();
				continue;
			}

			if ("VI".equals(cardType) && "1622".equals(groupCode)) { // VISA無限金鑽卡
				cntCond = "N";
				cntSel = "2";
				cntAccumlateCnt = 0;
				cntMajorFlag = "N";
				cntSubFlag = "N";
				cntMajorSub = "N";

				amtCond = "Y";
				amtAccumlateAmt = 300000.0;
				amtMajorFlag = "N";
				amtSubFlag = "N";
				amtMajorSub = "Y";

				minorHalfFlag = "N";

				gCondFlag = "N";
				gAccumlateAmt = 0.0;
				hCondFlag = "N";
				hAccumlateAmt = 0.0;

				insertCycAnulGp();
				continue;
			}

			if ("VI".equals(cardType)) { // 無限卡
				cntCond = "N";
				cntSel = "2";
				cntAccumlateCnt = 0;
				cntMajorFlag = "N";
				cntSubFlag = "N";
				cntMajorSub = "N";

				amtCond = "Y";
				amtAccumlateAmt = 200000.0;
				amtMajorFlag = "N";
				amtSubFlag = "N";
				amtMajorSub = "Y";

				minorHalfFlag = "Y";

				gCondFlag = "Y";
				gAccumlateAmt = 80000.0;
				hCondFlag = "Y";
				hAccumlateAmt = 60000.0;

				insertCycAnulGp();
				continue;
			}

		}

		closeCursor();

	}

	/***********************************************************************/
	void insertCycAnulGp() throws Exception {

		daoTable = "cyc_anul_gp";
		setValue("group_code", getValue("group_code"));
		setValue("card_type", getValue("card_type"));
		setValueInt("card_fee", 0);
		setValueInt("sup_card_fee", 0);
		setValue("mer_cond", "N");
		setValue("mer_bl_flag", "N");
		setValue("mer_ca_flag", "N");
		setValue("mer_it_flag", "N");
		setValue("mer_ao_flag", "N");
		setValue("mer_id_flag", "N");
		setValue("mer_ot_flag", "N");
		setValue("major_flag", "N");
		setValue("sub_flag", "N");
		setValue("major_sub", "N");
		setValue("a_merchant_sel", "0");
		setValue("a_mcht_group_sel", "0");

		setValue("cnt_select", cntSel);
		setValue("cnt_cond", cntCond);
		setValueInt("month_cnt", 0);
		setValueInt("accumlate_cnt", cntAccumlateCnt);
		setValue("cnt_bl_flag", "Y");
		setValue("cnt_ca_flag", "Y");
		setValue("cnt_it_flag", "Y");
		setValue("cnt_ao_flag", "Y");
		setValue("cnt_id_flag", "Y");
		setValue("cnt_ot_flag", "Y");
		setValue("cnt_major_flag", cntMajorFlag);
		setValue("cnt_sub_flag", cntSubFlag);
		setValue("cnt_major_sub", cntMajorSub);
		setValue("b_mcc_code_sel", "0");
		setValue("b_merchant_sel", "0");
		setValue("b_mcht_group_sel", "0");

		setValueDouble("accumlate_amt", amtAccumlateAmt);
		setValue("amt_cond", amtCond);
		setValue("amt_bl_flag", "Y");
		setValue("amt_ca_flag", "Y");
		setValue("amt_it_flag", "Y");
		setValue("amt_ao_flag", "Y");
		setValue("amt_id_flag", "Y");
		setValue("amt_ot_flag", "Y");
		setValue("amt_major_flag", amtMajorFlag);
		setValue("amt_sub_flag", amtSubFlag);
		setValue("amt_major_sub", amtMajorSub);
		setValue("c_mcc_code_sel", "0");
		setValue("c_merchant_sel", "0");
		setValue("c_mcht_group_sel", "0");

		setValue("mcode", "");
		
		//申辦電子帳單免年費的groupCode
		String[] ebillGroupCode= new String[] {"1657","1693","1614","1615","1684","1685","1616","1694",
				       "1681","1613","1655","1656","1683","1686","1688","1689","1690","1691","1692"};
		if (ArrayUtils.contains(ebillGroupCode, getValue("group_code"))) {
			emailNopaperFlag = "Y";
		}
		
		setValue("email_nopaper_flag", emailNopaperFlag);

		setValue("miner_half_flag", minorHalfFlag);
		setValue("g_cond_flag", gCondFlag);
		setValueDouble("g_accumlate_amt", gAccumlateAmt);
		setValue("h_cond_flag", hCondFlag);
		setValueDouble("h_accumlate_amt", hAccumlateAmt);

		setValue("crt_date", hBusiBusinessDate);
		setValue("crt_user", "system");
		setValue("apr_date", hBusiBusinessDate);
		setValue("apr_user", "system");
		setValue("apr_flag", "Y");
		setValue("mod_user", "system");
		setValue("mod_time", sysDate + sysTime);
		setValue("mod_pgm", javaProgram);
		setValueInt("mod_seqno", 0);

		insertTable();
		if (dupRecord.equals("Y")) {
			showLogMessage("E", "",
					String.format("Insert cyc_anul_gp duplicate, groupCode=[%s]", getValue("group_code")));
		}

	}

	public static void main(String[] args) throws Exception {
		TmpM011 proc = new TmpM011();
		proc.mainProcess(args);
		return;
	}
	// ************************************************************************

}
