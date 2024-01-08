/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/06  V1.00.00   Ryan     program initial                            *
*  112/03/14  V1.00.01   Wilson   產檔路徑調整                                                                                                *
*  112/03/21  V1.00.02   Wilson   mark procFTP                               *
*  112/03/22  V1.00.03   Ryan     85欄位與86欄位交換                         *
*  112/04/14  V1.00.04   Ryan     可用餘額邏輯調整                                                                                         *
*  112/05/10  V1.00.05   Ryan     調整執行效能                                                                                                  *
*  112/05/21  V1.00.06   Wilson   日期減一天                                                                                                    *
*  112/05/25  V1.00.07   Ryan   格式長度修正                                                                                                     *
*  112/06/01  V1.00.08   Ryan     修正帶錯欄位 curr_code                                
*  112/07/06  V1.00.09   Ryan     調整基本率、服務費欄位                                                                               *
*  112/07/06  V1.00.10   Ryan     銀行別增加判斷606、607                          *
*  112/07/07  V1.00.11   Wilson   雙幣卡改成出2筆                                                                                          *
*  112/07/13  V1.00.12   Ryan     調整85,86欄位                                                                                              *
*  112/07/17  V1.00.13   Wilson   更正欄位名稱                                                                                                *
*  112/07/26  V1.00.14   Ryan     調整基本率,保費,年費欄位                                                                          *
*  112/08/04  V1.00.15   Ryan     調整86,92,96欄位 ,金額欄位前面不要補0 ,修改25 26 78 79 83 84 91 92 93 94 96 長度為14, 修正25~40金額欄位,修正54基本率欄位   *
*  112/08/04  V1.00.16   Wilson   調整讀取雙幣卡邏輯                                                                                    *
*  112/08/08  V1.00.17   Ryan     selectActAcagCurr8 若thisAcctMonth不足6碼 return 0   *       
*  112/08/16  V1.00.18   Ryan     讀取act_debt的acct_month都拿掉 ,改為curr_code ,END_BAL 改為 DC_END_BAL  *     
*  112/08/21  V1.00.19   Ryan     STMT_CYCLE原讀CRD_CARD 改讀ACT_ACNO/DBA_ACNO  *          
*  112/08/23  V1.00.20   Ryan     修正正卡人ID  *                          
*  112/08/25  V1.00.21   Ryan     保費固定放0          *                
*  112/08/30  V1.00.22   Ryan     調整90,91,92欄位          *                                        
*  112/09/22  V1.00.23   Ryan     增加DBUG設定          *
*  112/09/24  V1.00.24   Wilson   增加原始卡號欄位 
*  112/09/26  V1.00.25   Sunny    selectActAcagCurr,selectBilContract load table加order by       
*  112/11/27  V1.00.26   Ryan      調整效能 *                                                                                                                           
*  112/11/28  V1.00.27   Ryan     add selectCcaBalance
*****************************************************************************/
package Inf;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;
import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

import Cca.CalBalance;

public class InfR002 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生送CRM卡片相關資料檔程式 112/11/28  V1.00.27";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String DATA_FORM = "CCTCRD";
	private final static String COL_SEPERATOR = "\006";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	private final static boolean DBUG = false;
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CalBalance calBalance = null;
	String hBusinessDate = "";
	public int mainProcess(String[] args) {

		try {
			CommCrd comc = new CommCrd();

			// ====================================
			// 固定要做的
			dateTime();
			setConsoleMode("Y");
			javaProgram = this.getClass().getName();
			showLogMessage("I", "", javaProgram + " " + progname);

			if (!connectDataBase()) {
				comc.errExit("connect DataBase error", "");
			}
			
			commCol = new CommCol(getDBconnect(), getDBalias());
			commTxInf = new CommTxInf(getDBconnect(), getDBalias());
			calBalance = new CalBalance(getDBconnect(), getDBalias());
			// =====================================
			
			// get searchDate
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			searchDate = getProgDate(searchDate, "D");
			
			//日期減一天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);

			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
			hBusinessDate = searchDate;
			
			// convert YYYYMMDD into YYMMDD
			String fileNameSearchDate = searchDate.substring(2);

			
			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			// 產生主要檔案 .DAT 
			int dataCount = generateDatFile(fileFolder, datFileName ,searchDate);

			dateTime(); // update the system date and time
			boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), dataCount);
			if (isGenerated == false) {
				comc.errExit("產生HDR檔錯誤!", "");
			}
			
			// 先傳*.DAT檔再傳*.HDR檔
			String hdrFileName = datFileName.replace(CommTxInf.DAT_EXTENSION, CommTxInf.HDR_EXTENSION);  
			
			// run FTP
			procFTP(fileFolder, datFileName ,hdrFileName);

			showLogMessage("I", "", "執行結束");
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";
			expHandle(e);
			return exceptExit;
		} finally {
			finalProcess();
		}
	}

	/**
	 * generate a .Dat file
	 * @param fileFolder 檔案的資料夾路徑
	 * @param datFileName .dat檔的檔名
	 * @return the number of rows written. If the returned value is -1, it means the path or the file does not exist. 
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String datFileName ,String searchDate2) throws Exception {
		selectPtrWorkday();
		selectCrdCardPp();
		selectActDebt1();
		selectActDebt2();
		selectActDebt3();
		selectActDebt4();
		selectCycBillExt();
		selectBilContract();
		selectActAcagCurr();
		selectCrdBalance();
		selectCcaBalance();
		selectInfR002Data();
		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value
		try {	
			StringBuffer sb = new StringBuffer();
			showLogMessage("I", "", "開始產生.DAT檔......");
			while (fetchTable()) {
				InfR002Data infR002Data = getInfData();
				String rowOfDAT = getRowOfDAT(infR002Data);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes("MS950");
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}
			this.closeCursor();
			// write the rest of bytes on the file 
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes("MS950");
				writeBinFile(tmpBytes, tmpBytes.length);
			}
			
			if (rowCount == 0) {
				showLogMessage("I", "", "無資料可寫入.DAT檔");
			}else {
				showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", rowCount));
			}
			
		}finally {
			closeBinaryOutput();
		}
		
		return rowCount;
	}
	
	void selectPtrWorkday() throws Exception {
	    daoTable    = "PTR_WORKDAY";
		extendField = "workday.";
		sqlCmd = " select stmt_cycle,this_acct_month,this_lastpay_date ";
		sqlCmd += " from PTR_WORKDAY ";
	    int n = loadTable();
	    setLoadData("workday.stmt_cycle");
	    showLogMessage("I", "","selectPtrWorkday 取得= [" +n+ "]筆");
	}
	
	void getPtrWorkday(InfR002Data infR002Data) throws Exception{
		setValue("workday.stmt_cycle",infR002Data.stmtCycle);
        getLoadData("workday.stmt_cycle");
		infR002Data.thisAcctMonth = getValue("workday.this_acct_month");
//        infR002Data.thisAcctMonth = "202301";//TEST
		infR002Data.thisLastpayDate = getValue("workday.this_lastpay_date");
	}
	
	void getBankType(InfR002Data infR002Data) {
		String bankType = "";
//		a.若(1)讀取到的GROUP_CODE=6673、6674  放106
		if(commStr.pos(",6673,6674", infR002Data.groupCode)>0) {
			if(infR002Data.currCode.equals("840")) {
				bankType = "606";
        	}
        	else if(infR002Data.currCode.equals("392")) {
        		bankType = "607";
        	}
        	else {
        		bankType = "106";
        	}           
		}
//		c.若不等於a、b :  
//		(c1)若(1)讀取到的ACCT_TYPE=01  放106
//		(c2)若(1)讀取到的ACCT_TYPE=90  放206
//		(c3)若(1)讀取到的ACCT_TYPE=03、06  放306
		if(commStr.empty(bankType)) {
			if("01".equals(infR002Data.acctType)) {
				bankType = "106";
			}
			if("90".equals(infR002Data.acctType)) {
				bankType = "206";
			}
			if(commStr.pos(",03,06", infR002Data.acctType)>0) {
				bankType = "306";
			}
		}
		infR002Data.bankType = bankType;
	}
	
	void getControlCode(InfR002Data infR002Data) {
		String controlCode = "",controlCodeDate = "";
		if(!"0".equals(infR002Data.currentCode)) {
			switch(infR002Data.currentCode + infR002Data.oppostReason) {
			case "1A2":
				controlCode = "A";
				break;
			case "3J2":
				controlCode = "B";
				break;
			case "110":
				controlCode = "E";
				break;
			case "5M2":
				controlCode = "F";
				break;
			case "4S1":
				controlCode = "G";
				break;
			case "1AL":
				controlCode = "K";
				break;
			case "2C1":
				controlCode = "L";
				break;
			case "4MO":
				controlCode = "M";
				break;
			case "1AJ":
				controlCode = "N";
				break;
			case "5O1":
				controlCode = "O";
				break;
			case "2S0":
				controlCode = "S";
				break;
			case "1AX":
				controlCode = "X";
				break;
			default:
				controlCode = "Z";
				break;
			}
		}else {
			if(commStr.empty(infR002Data.blockReason1 + infR002Data.blockReason2 
					+ infR002Data.blockReason3 + infR002Data.blockReason4 + infR002Data.blockReason5)) {
				controlCode = "T";
			}
		}
		
		infR002Data.controlCode = controlCode;
		
		if(commStr.empty(controlCode)) {
			controlCodeDate = "";
		}else if("T".equals(controlCode)){
			if(!commStr.empty(infR002Data.specStatus))
				controlCodeDate = infR002Data.specDate;
		}else{
			controlCodeDate = infR002Data.oppostDate;
		}
		infR002Data.controlCodeDate = commDate.toTwDate(controlCodeDate);
	}
	
	void getLateDate(InfR002Data infR002Data) {
		String[] lateDate = new String[3];
		lateDate[0] = infR002Data.issueDate;
		lateDate[1] = infR002Data.reissueDate;
		lateDate[2] = infR002Data.changeDate;
		Arrays.parallelSort(lateDate);
		
		infR002Data.lateDate = commDate.toTwDate(lateDate[2]);
	}
	
	void selectCrdCardPp() throws Exception {
	    daoTable    = "CRD_CARD_PP";
		extendField = "cardpp.";
		sqlCmd = " select ID_P_SEQNO,PP_CARD_NO,VALID_TO ";
		sqlCmd += " from CRD_CARD_PP ";
		sqlCmd += " WHERE VIP_KIND = '1'  ";
		sqlCmd += " ORDER BY ISSUE_DATE ";
	    int n = loadTable();
	    setLoadData("cardpp.ID_P_SEQNO");
	    showLogMessage("I", "","selectCrdCardPp 取得= [" +n+ "]筆");
	}
	
	void getCrdCardPp(InfR002Data infR002Data) throws Exception {
		setValue("cardpp.ID_P_SEQNO",infR002Data.idPSeqno);
        getLoadData("cardpp.ID_P_SEQNO");
		infR002Data.ppCardNo = getValue("cardpp.PP_CARD_NO");
		infR002Data.valitTo = getValue("cardpp.VALID_TO");
	}
	
	void get24MonthProfile(InfR002Data infR002Data) throws Exception {
		for (int i = 1; i <= 24; i++) {
			String paymentRate = commStr.decode(getValue(String.format("payment_rate%s", i))
					,",0A,0E,0E,0C" ,",B,Z,0,1" );
			if(commStr.ss2int(paymentRate)>=2)
				paymentRate = String.format("%s",commStr.ss2int(paymentRate)-1);
			infR002Data.monthProfile24 += paymentRate;
		}
	}
	
	void selectCycBillExt() throws Exception {
	    daoTable    = "CYC_BILL_EXT";
		extendField = "billExt.";
		sqlCmd = " select * from (select P_SEQNO,ACCT_YEAR,CURR_CODE,sum(INTEREST_AMT_01 + INTEREST_AMT_02 + INTEREST_AMT_03 + INTEREST_AMT_04 + INTEREST_AMT_05 ";
		sqlCmd += " + INTEREST_AMT_06 + INTEREST_AMT_07 + INTEREST_AMT_08 + INTEREST_AMT_09 + INTEREST_AMT_10 ";
		sqlCmd += " + INTEREST_AMT_11 + INTEREST_AMT_12 ) as SUM_INTEREST_AMT ";
		sqlCmd += " from CYC_BILL_EXT ";
		sqlCmd += " GROUP BY P_SEQNO,ACCT_YEAR,CURR_CODE) AA ";
		sqlCmd += " where AA.SUM_INTEREST_AMT > 0 ";
	    int n = loadTable();
	    setLoadData("billExt.P_SEQNO,billExt.ACCT_YEAR,billExt.CURR_CODE");
	    showLogMessage("I", "","selectCycBillExt 取得= [" +n+ "]筆");
	}
	
	void getCycBillExt(InfR002Data infR002Data) throws Exception {
		setValue("billExt.P_SEQNO", infR002Data.pSeqno);
		setValue("billExt.ACCT_YEAR", commStr.left(hBusinessDate, 4));
		setValue("billExt.CURR_CODE", infR002Data.currCode);
        getLoadData("billExt.P_SEQNO,billExt.ACCT_YEAR,billExt.CURR_CODE");
		infR002Data.sumInterestAmt = getValueDouble("billExt.SUM_INTEREST_AMT");
	}
	
	/***
	   * 可用餘額
	 * @param infR002Data
	 * @return
	 * @throws Exception
	 */
	long getCrdBalance(InfR002Data infR002Data) throws Exception {
	
		if("N".equals(infR002Data.vdFlag)){
			if("Y".equals(infR002Data.tmpSonCardFlag)) {
				setValue("BALANCE_CAL1.CARD_NO", infR002Data.cardNo);
				getLoadData("BALANCE_CAL1.CARD_NO");
				return getValueLong("BALANCE_CAL1.CARD_AMT_BALANCE");
			}else {
				setValue("BALANCE_CAL2.ACNO_P_SEQNO", infR002Data.acnoPSeqno);
				getLoadData("BALANCE_CAL2.ACNO_P_SEQNO");
				return getValueLong("BALANCE_CAL2.ACCT_AMT_BALANCE");
			}	
		}else {
			return 0;
		}
	}
	
	void selectCrdBalance() throws Exception {
	    daoTable    = "CCA_CARD_BALANCE_CAL";
	    extendField = "BALANCE_CAL1.";
	    sqlCmd = " SELECT CARD_AMT_BALANCE,CARD_NO FROM CCA_CARD_BALANCE_CAL ";
	    int n = loadTable();
	    setLoadData("BALANCE_CAL1.CARD_NO");
	    showLogMessage("I", "","selectCrdBalance 取得= [" +n+ "]筆");
	}
	
	void selectCcaBalance() throws Exception {
	    daoTable    = "CCA_ACCT_BALANCE_CAL";
	    extendField = "BALANCE_CAL2.";
	    sqlCmd = " SELECT ACCT_AMT_BALANCE,ACNO_P_SEQNO FROM CCA_ACCT_BALANCE_CAL ";
	    int n = loadTable();
	    setLoadData("BALANCE_CAL2.ACNO_P_SEQNO");
	    showLogMessage("I", "","selectCcaBalance 取得= [" +n+ "]筆");
	}
	
	void selectActDebt1() throws Exception {
	    daoTable    = "act_debt1";
	    extendField = "debt1.";
//	    sqlCmd = " select acct_month,p_seqno,card_no,sum(end_bal) as sum_end_bal ";
	    sqlCmd = " select p_seqno,card_no,curr_code,sum(dc_end_bal) as sum_end_bal ";
		sqlCmd += " from act_debt ";
//		sqlCmd += " WHERE acct_code in('CA','CF') group by acct_month,p_seqno,card_no ";
		sqlCmd += " WHERE acct_code in('CA','CF') group by p_seqno,card_no,curr_code ";
	    int n = loadTable();
//	    setLoadData("debt1.acct_month,debt1.p_seqno,debt1.card_no");
	    setLoadData("debt1.p_seqno,debt1.card_no,debt1.curr_code");
	    showLogMessage("I", "","selectActDebt1 取得= [" +n+ "]筆");
	}
	
	void getActDebt1(InfR002Data infR002Data) throws Exception{
//		setValue("debt1.acct_month",infR002Data.thisAcctMonth);
		setValue("debt1.curr_code",infR002Data.currCode);
		setValue("debt1.p_seqno",infR002Data.pSeqno);
		setValue("debt1.card_no",infR002Data.cardNo);
//		getLoadData("debt1.acct_month,debt1.p_seqno,debt1.card_no");
		getLoadData("debt1.p_seqno,debt1.card_no,debt1.curr_code");
		infR002Data.sumEndBal = getValueDouble("debt1.sum_end_bal");
	}
	
	void selectActDebt2() throws Exception {
	    daoTable    = "act_debt2";
	    extendField = "debt2.";
		sqlCmd = " select card_no,curr_code,sum(dc_end_bal) as sum_end_bal ";
		sqlCmd += " from act_debt ";
		sqlCmd += " WHERE acct_code in('AF') group by card_no ,curr_code";
	    int n = loadTable();
	    setLoadData("debt2.card_no,debt2.curr_code");
	    showLogMessage("I", "","selectActDebt2 取得= [" +n+ "]筆");
	}
	
	void getActDebt2(InfR002Data infR002Data) throws Exception{
		setValue("debt2.card_no",infR002Data.cardNo);
		setValue("debt2.curr_code",infR002Data.currCode);
		getLoadData("debt2.card_no,debt2.curr_code");
		infR002Data.sumEndBal2 = getValueDouble("debt2.sum_end_bal");
	}
	
	 public int selectActDebt3() throws Exception
	  {
	    daoTable    = "act_debt3";
	    extendField = "debt3.";
//		sqlCmd = " select a.card_no,a.acct_code,a.acct_month,sum(a.end_bal) as debt_acct_sum ";
	    sqlCmd = " select a.card_no,a.acct_code,curr_code,sum(a.dc_end_bal) as debt_acct_sum ";
		sqlCmd += " from act_debt a , ptr_actcode b ";
		sqlCmd += " WHERE a.acct_code = b.acct_code ";
		sqlCmd += " and a.acct_code in ('RI','PN','PF','AF','CF','CI','CC','LF','SF') ";
//		sqlCmd += " group by a.card_no ,a.acct_code ,a.acct_month ";
		sqlCmd += " group by a.card_no ,a.acct_code ,a.curr_code ";
	    int n = loadTable();
//	    setLoadData("debt3.card_no,debt3.acct_code,debt3.acct_month");
	    setLoadData("debt3.card_no,debt3.acct_code,debt3.curr_code");
	    showLogMessage("I", "","selectActDebt3 取得= [" +n+ "]筆");
	    return n;
	  }
	 
	
	double getActDebt3(InfR002Data infR002Data , String acctCode) throws Exception {
		setValue("debt3.card_no",infR002Data.cardNo);
		setValue("debt3.acct_code",acctCode);
//		setValue("debt3.acct_month",infR002Data.thisAcctMonth);
		setValue("debt3.curr_code",infR002Data.currCode);
//		getLoadData("debt3.card_no,debt3.acct_code,debt3.acct_month");
		getLoadData("debt3.card_no,debt3.acct_code,debt3.curr_code");
		
		return getValueDouble("debt3.debt_acct_sum");	
	}
	
	void selectActDebt4() throws Exception {
	    daoTable    = "act_debt4";
	    extendField = "debt4.";
//		sqlCmd = " select a.card_no,a.acct_month ,sum(decode(b.interest_method,'Y',a.end_bal,0)) as debt_acct_sum ";
	    sqlCmd = " select a.card_no ,a.curr_code ,sum(a.dc_end_bal) as debt_acct_sum ";
		sqlCmd += " from act_debt a, ptr_actcode b ";
		sqlCmd += " where a.acct_code = b.acct_code ";
		sqlCmd += " and a.acct_code not in('CA','CF','DP') ";
//		sqlCmd += " group by a.card_no ,a.acct_month";
		sqlCmd += " group by a.card_no ,a.curr_code ";
	    int n = loadTable();
//	    setLoadData("debt4.card_no,debt4.acct_month");
	    setLoadData("debt4.card_no,debt4.curr_code");
	    showLogMessage("I", "","selectActDebt4 取得= [" +n+ "]筆");
	}
	
	double getActDebt4(InfR002Data infR002Data) throws Exception {
		setValue("debt4.card_no",infR002Data.cardNo);
//		setValue("debt4.acct_month",infR002Data.thisAcctMonth);
		setValue("debt4.curr_code",infR002Data.currCode);
//        getLoadData("debt4.card_no,debt4.acct_month");
		getLoadData("debt4.card_no,debt4.curr_code");
		return infR002Data.ttlAmtBal = getValueDouble("debt4.debt_acct_sum");
	}
	
	
	Double sub(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.subtract(b2).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();

	}
	
	
	void selectBilContract() throws Exception {
	    daoTable    = "bil_contract";
	    extendField = "contract.";
		sqlCmd = " select card_no,sum((unit_price*(install_tot_term-install_curr_term)+remd_amt+decode(install_curr_term,0,first_remd_amt,0))) as sum_bil_contract ";
		sqlCmd += " from bil_contract ";
		sqlCmd += "  where install_tot_term != install_curr_term and contract_kind = 1  ";
		sqlCmd += "  and post_cycle_dd > 0  group by card_no";
		sqlCmd += " order by card_no";
	    int n = loadTable();
	    setLoadData("contract.card_no");
	    showLogMessage("I", "","selectBilContract 取得= [" +n+ "]筆");

	}
	
	void getBilContract(InfR002Data infR002Data) throws Exception{
		setValue("contract.card_no",infR002Data.cardNo);
        getLoadData("contract.card_no");
		infR002Data.sumBilContract = getValueDouble("contract.sum_bil_contract");
	}
	
	void selectActAcagCurr() throws Exception {
	    daoTable    = "act_acag_curr";
	    extendField = "acagcurr.";
		sqlCmd = " select curr_code,p_seqno,dc_pay_amt,acct_month ";
		sqlCmd += " from act_acag_curr ";
		sqlCmd += " order by curr_code,p_seqno,acct_month ";
	    int n = loadTable();
	    setLoadData("acagcurr.curr_code,acagcurr.p_seqno,acagcurr.acct_month");
	    setLoadData("acagcurr.curr_code,acagcurr.p_seqno");
	    showLogMessage("I", "","selectActAcagCurr 取得= [" +n+ "]筆");
		
	}
	
	double getDcPayAmt(InfR002Data infR002Data , int month) throws Exception{
		String acctMonth = commDate.monthAdd(infR002Data.thisAcctMonth, month);
		setValue("acagcurr.curr_code",infR002Data.currCode);
		setValue("acagcurr.p_seqno",infR002Data.pSeqno);
		setValue("acagcurr.acct_month",acctMonth);
        getLoadData("acagcurr.curr_code,acagcurr.p_seqno,acagcurr.acct_month");
		return getValueDouble("acagcurr.dc_pay_amt");
	}
	
//	double selectActAcagCurr8(InfR002Data infR002Data) throws Exception {
//		if(infR002Data.thisAcctMonth.length()<6) {
//			showLogMessage("I", "", String.format("this_acct_month[%s] 格式不正確 , p_seqno[%s] ,acct_type[%s] ,id_no[%s] 查無此stmt_cycle[%s]"
//					+ " ,跳過selectActAcagCurr8(function的作業)不處理!! ", infR002Data.thisAcctMonth,infR002Data.pSeqno,infR002Data.acctType,infR002Data.idNo,infR002Data.stmtCycle));
//			return 0.0;
//		}
//		extendField = "act_acag_curr8.";
//		sqlCmd = " select  SUM(DC_PAY_AMT) as SUM_DC_PAY_AMT ";
//		sqlCmd += " from act_acag_curr ";
//		sqlCmd += " where acct_month <= ? ";
//		sqlCmd += " and curr_code = ? and p_seqno = ? ";
//		setString(1,commDate.monthAdd(infR002Data.thisAcctMonth, -8));
//		setString(2,infR002Data.currCode);
//		setString(3,infR002Data.pSeqno);
//		selectTable();
//
//		return getValueDouble("act_acag_curr8.SUM_DC_PAY_AMT");
//	}
	
	double getActAcagCurr8(InfR002Data infR002Data) throws Exception {
		if(infR002Data.thisAcctMonth.length()<6) {
			showLogMessage("I", "", String.format("this_acct_month[%s] 格式不正確 , p_seqno[%s] ,acct_type[%s] ,id_no[%s] 查無此stmt_cycle[%s]"
					+ " ,跳過selectActAcagCurr8(function的作業)不處理!! ", infR002Data.thisAcctMonth,infR002Data.pSeqno,infR002Data.acctType,infR002Data.idNo,infR002Data.stmtCycle));
			return 0.0;
		}
		String acctMonth = commDate.monthAdd(infR002Data.thisAcctMonth, -8);
		setValue("acagcurr.curr_code",infR002Data.currCode);
		setValue("acagcurr.p_seqno",infR002Data.pSeqno);
        int n = getLoadData("acagcurr.curr_code,acagcurr.p_seqno");
        double sumDcPayAmt = 0;
        for(int i=0;i<n;i++) {
        	if(acctMonth.compareTo(getValue("acagcurr.acct_month",i))>=0)
        		sumDcPayAmt = add(sumDcPayAmt,getValueDouble("acagcurr.dc_pay_amt",i));
        }
        
		return sumDcPayAmt;
	}
	
    Double add(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.add(b2).doubleValue();

	}

	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfR002Data infR002Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		//取得THIS_ACCT_MONTH,THIS_LASTPAY_DATE
		getPtrWorkday(infR002Data);
		//取得銀行別  
		getBankType(infR002Data);
		//取得控管碼,控管碼日期  
		getControlCode(infR002Data);
		//取得最近製卡日  
		getLateDate(infR002Data);
		//取得PP_CARD_NO,VALIT_TO
		getCrdCardPp(infR002Data);
		//取得24 Month Profile
		get24MonthProfile(infR002Data);
		//取得本年度利息累計
		getCycBillExt(infR002Data);
		//取得預借現金現欠餘額,含利息及費用
		getActDebt1(infR002Data);
		//一般消費現欠餘額,含利息及費用
		getActDebt4(infR002Data);
		//分期餘額 
		getBilContract(infR002Data);
		//年費
		getActDebt2(infR002Data);
		//可用餘額
		long availableBalance = getCrdBalance(infR002Data);

		sb.append(commCrd.fixLeft(infR002Data.idNo, 16)); //身分證號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.cardNo, 16));//卡號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.bankType, 3));//銀行別
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infR002Data.groupCode, 1,3), 3));//卡別
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.0f",("Y".equals(infR002Data.tmpSonCardFlag)?infR002Data.tmpIndivCrdLmt:infR002Data.tmpLineOfCreditAmt)), 9));//卡片額度
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infR002Data.newEndDate, 4,2) + commStr.mid(infR002Data.newEndDate, 2,2), 4));//卡片有效期限
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 2));//卡片序號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.controlCode, 1));//控管碼
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.controlCodeDate, 7));//控管碼日期   
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.regBankNo, 5));  //分行代號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commDate.toTwDate(infR002Data.oriIssueDate), 7));  //開戶日 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.tmpMajorRelation, 2));  //主卡關係 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%d",infR002Data.highestConsumeAmt), 9));  //最高消費紀錄
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.pos(",1,2", infR002Data.acctStatus)>0?"0":"1", 1));  //繳款方式 0：可循環 1：ㄧ次繳清
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.autopayAcctNo, 16));  //轉帳帳號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("2".equals(infR002Data.autopayIndicator)?"10":"20", 2));  //扣繳額度  10最低 20全額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.empty(infR002Data.autopayAcctNo)?"0":"1", 1));  //自動轉帳 0: 不自動轉帳 1: 自動轉帳
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%d", availableBalance), 10));   //可用餘額 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%d", infR002Data.tmpLineOfCreditAmtCash), 9));  //預現額度
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.tmpAcctNo, 16));  //金融卡帳號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 16));  //催收帳號 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.lateDate, 7));  //最近製卡日
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.ppCardNo, 16));  //PP卡卡號 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infR002Data.valitTo, 2,4), 5));  //PP卡有效期限
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f", getDcPayAmt(infR002Data,0)), 14));  //逾期末繳款紀錄25 逾期未繳記錄-本期應繳金額 長度11改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getDcPayAmt(infR002Data,-1)), 14));  //逾期末繳款紀錄26 逾期未繳記錄-上期應繳金額  長度11改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("01", 2));  //逾期末繳款紀錄27 逾期未繳記錄- 上期應繳
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getDcPayAmt(infR002Data,-2)), 14));  //逾期末繳款紀錄28 逾期未繳記錄-逾30天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("02", 2));  //逾期末繳款紀錄29 逾期未繳記錄-逾30天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getDcPayAmt(infR002Data,-3)), 14));  //逾期末繳款紀錄30 逾期未繳記錄-逾60天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("03", 2));  //逾期末繳款紀錄31 逾期未繳記錄-逾60天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getDcPayAmt(infR002Data,-4)), 14));  //逾期末繳款紀錄32 逾期未繳記錄-逾90天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("04", 2));  //逾期末繳款紀錄33 逾期未繳記錄-逾90天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getDcPayAmt(infR002Data,-5)), 14));  //逾期末繳款紀錄34 逾期未繳記錄-逾120天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("05", 2));  //逾期末繳款紀錄35 逾期未繳記錄-逾120天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getDcPayAmt(infR002Data,-6)), 14));  //逾期末繳款紀錄36 逾期未繳記錄-逾150天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("06", 2));  //逾期末繳款紀錄37 逾期未繳記錄-逾150天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getDcPayAmt(infR002Data,-7)), 14));  //逾期末繳款紀錄38 逾期未繳記錄-逾180天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("07", 2));  //逾期末繳款紀錄39 逾期未繳記錄-逾180天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getActAcagCurr8(infR002Data)), 14));  //逾期末繳款紀錄40 逾期未繳記錄-逾210天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("08", 2));  //逾期末繳款紀錄41 逾期未繳記錄-逾210天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.monthProfile24, 24)); //24 Month Profile   
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.newCardNo, 16));  //新卡卡號 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(!commStr.empty(infR002Data.newCardNo)?commDate.toTwDate(infR002Data.reissueDate):"", 7));  //轉卡生效日期 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記45
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記46
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記47
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記48
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記49
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記50
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記51
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記52
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //費用免除註記53
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.valueOf(new BigDecimal(infR002Data.rcrateYear.toString()).divide(BigDecimal.valueOf(100)).doubleValue()), 7));  //基本率 54
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //基本率調幅率
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 9));  //基本額度 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //率 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //率調幅率
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 9));  //額度 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //率 60
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //率調幅率
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //調整註記 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",infR002Data.sumInterestAmt), 14));  //本年度利息累計63 CYC_BILL_EXT.ACCT_YEAR(當年) sum(INTEREST_AMT_1~12)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 14));  //去年度利息累計
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //基本率(卡片利率) 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //基本率調幅率 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 9));  //基本額度
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //率68
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //率調幅率
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 9));  //額度
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //率
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 7));  //率調幅率
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 1));  //調整註記
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",infR002Data.sumInterestAmt), 14));  //本年度利息累計74 CYC_BILL_EXT.ACCT_YEAR(當年) sum(INTEREST_AMT_1~12)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 14));  //去年度利息累計
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 2));  //優惠辦法
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 4));  //優惠截止日
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.2f", 0.0), 14));  //刷卡消費疑異帳款 長度9改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.2f", 0.0), 14));  //預借現金欵異帳款 長度9改14    
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 13));  //當期預借利息
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 13));  //當期刷卡利息
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("", 9));  //當期累計總利息
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.2f", 0.0), 14));  //83 長度11改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("%.2f", 0.0), 14));  //前年累計利息 長度11改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",infR002Data.sumEndBal), 14));  //預借現金現欠餘額,含利息及費用 原86欄位
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",infR002Data.ttlAmtBal), 15));  //一般消費現欠餘額,含利息及費用  原85欄位
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 76));  //FILLER
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",infR002Data.ttlAmtBal), 14));  //同85
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",infR002Data.sumEndBal), 14));  //同86
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",doubleAdd(getActDebt3(infR002Data,"RI"),getActDebt3(infR002Data,"CI"))), 14));  //購貨循環息90 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",doubleAdd(getActDebt3(infR002Data,"PF"),getActDebt3(infR002Data,"CC"))), 14));  //服務費 長度7改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",doubleAdd(doubleAdd(getActDebt3(infR002Data,"PN"),getActDebt3(infR002Data,"LF")),getActDebt3(infR002Data,"SF"))), 14));  //雜項費用 長度7改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",0.0), 14));  //保費 長度7改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",infR002Data.sumEndBal2), 14));  //年費 長度7改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight("0", 14));  //預借現金循環息
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",getActDebt3(infR002Data,"CF")), 14));  //服務費 長度7改14
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.thisLastpayDate, 8)); //繳款截止日 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.majorIdNo, 11));  //正卡人ID  
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.bankType, 3));  //正卡人ORG正卡人帳戶類別 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.pos(",03,06", infR002Data.acctType)>0?infR002Data.tmpCorpNo:"", 16));  //公司統編
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.pos(",03,06", infR002Data.acctType)>0?infR002Data.bankType:"", 3));  //公司ORG公司帳戶類別100
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%.2f",(",606,607".indexOf(infR002Data.bankType)>0)?0.00:infR002Data.sumBilContract), 14));  //分期餘額 外幣不會有分期餘額 606 607
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.groupCode, 4));  //團體代碼
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.currentCode, 1));  //卡片狀況 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.oppostReason, 2));  //停用原因碼 105
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.acctStatus, 1));  //歸戶狀態 106
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR002Data.majorOriCardNo, 16));  //原始卡號 107
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	InfR002Data getInfData() throws Exception {
		InfR002Data infR002Data = new InfR002Data();
		infR002Data.idNo = getValue("ID_NO");
		infR002Data.idPSeqno = getValue("ID_P_SEQNO");
		infR002Data.cardNo = getValue("CARD_NO");
		infR002Data.groupCode = getValue("GROUP_CODE");
		infR002Data.acctType = getValue("ACCT_TYPE");
		infR002Data.tmpSonCardFlag = getValue("TMP_SON_CARD_FLAG");
		infR002Data.tmpIndivCrdLmt = getValueDouble("TMP_INDIV_CRD_LMT");
		infR002Data.tmpLineOfCreditAmt = getValueDouble("TMP_LINE_OF_CREDIT_AMT");
		infR002Data.newEndDate = getValue("NEW_END_DATE");
		infR002Data.currentCode = getValue("CURRENT_CODE");
		infR002Data.oppostReason = getValue("OPPOST_REASON");
		infR002Data.oppostDate = getValue("OPPOST_DATE");
		infR002Data.specStatus = getValue("SPEC_STATUS");
		infR002Data.specDate = getValue("SPEC_DATE");
		infR002Data.blockReason1 = getValue("BLOCK_REASON1");
		infR002Data.blockReason2 = getValue("BLOCK_REASON2");
		infR002Data.blockReason3 = getValue("BLOCK_REASON3");
		infR002Data.blockReason4 = getValue("BLOCK_REASON4");
		infR002Data.blockReason5 = getValue("BLOCK_REASON5");
		infR002Data.blockDate = getValue("BLOCK_DATE");
		infR002Data.regBankNo = getValue("REG_BANK_NO");
		infR002Data.oriIssueDate = getValue("ORI_ISSUE_DATE");
		infR002Data.tmpMajorRelation = getValue("TMP_MAJOR_RELATION");
		infR002Data.highestConsumeAmt= getValueLong("HIGHEST_CONSUME_AMT");
		infR002Data.tmpLineOfCreditAmtCash = getValueLong("TMP_LINE_OF_CREDIT_AMT_CASH");
		infR002Data.tmpAcctNo = getValue("TMP_ACCT_NO");
		infR002Data.issueDate = getValue("ISSUE_DATE");
		infR002Data.reissueDate= getValue("REISSUE_DATE");
		infR002Data.changeDate= getValue("CHANGE_DATE");
		infR002Data.newCardNo= getValue("NEW_CARD_NO");
		infR002Data.reissueReason = getValue("REISSUE_REASON");
		infR002Data.majorIdPSeqno = getValue("MAJOR_ID_P_SEQNO");
		infR002Data.tmpCorpNo = getValue("TMP_CORP_NO");
		infR002Data.acctStatus = getValue("ACCT_STATUS");
		infR002Data.autopayAcctNo = getValue("AUTOPAY_ACCT_NO");
		infR002Data.autopayIndicator = getValue("AUTOPAY_INDICATOR");
		infR002Data.pSeqno = getValue("P_SEQNO");
		infR002Data.stmtCycle = getValue("STMT_CYCLE");
		infR002Data.acnoPSeqno = getValue("ACNO_P_SEQNO");
		infR002Data.vdFlag = getValue("VD_FLAG");
		infR002Data.thisAcctMonth = getValue("THIS_ACCT_MONTH");
		infR002Data.thisLastpayDate = getValue("THIS_LASTPAY_DATE");
		infR002Data.currCode = getValue("CURR_CODE");
		infR002Data.rcrateYear = getValueDouble("RCRATE_YEAR");
		infR002Data.majorIdNo = getValue("MAJOR_ID_NO");
		infR002Data.majorOriCardNo = getValue("MAJOR_ORI_CARD_NO");
		return infR002Data;
	}

	private void selectInfR002Data() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT B.ID_NO,A.ID_P_SEQNO,A.CARD_NO,A.GROUP_CODE,A.ACCT_TYPE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG, ")
		.append(" A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT,A.NEW_END_DATE, ")
		.append(" A.CURRENT_CODE,A.OPPOST_REASON,A.OPPOST_DATE,D.SPEC_STATUS,D.SPEC_DATE,E.BLOCK_REASON1,E.BLOCK_REASON2, ")
		.append(" E.BLOCK_REASON3,E.BLOCK_REASON4,E.BLOCK_REASON5,E.BLOCK_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION, ")
		.append(" A.HIGHEST_CONSUME_AMT,C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		.append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,A.MAJOR_ID_P_SEQNO,A.CORP_NO AS TMP_CORP_NO ,C.ACCT_STATUS, ")
		.append(" C.AUTOPAY_ACCT_NO,C.AUTOPAY_INDICATOR,A.P_SEQNO,C.STMT_CYCLE,A.ACNO_P_SEQNO,'N' AS VD_FLAG ,F.CURR_CODE ,C.RCRATE_YEAR, ")
		.append(" G.ID_NO AS MAJOR_ID_NO, ")
		.append(" H.ORI_CARD_NO AS MAJOR_ORI_CARD_NO ");
		for (int i = 1; i <= 24; i++) {
			sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		}
		sb.append(" FROM CRD_CARD A ")
		.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
		.append(" LEFT JOIN CCA_CARD_BASE D ON A.CARD_NO = D.CARD_NO ")
		.append(" LEFT JOIN CCA_CARD_ACCT E ON A.ACNO_P_SEQNO = E.ACNO_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACCT_CURR F ON A.P_SEQNO = F.P_SEQNO AND F.CURR_CODE = '901' ")
		.append(" LEFT JOIN CRD_IDNO G ON A.MAJOR_ID_P_SEQNO = G.ID_P_SEQNO ")
		.append(" LEFT JOIN CRD_CARD H ON A.MAJOR_CARD_NO = H.CARD_NO ")
		.append(" WHERE A.CURR_CODE = '901' ");
		sb.append(" UNION ALL ")		
		.append(" SELECT B.ID_NO,A.ID_P_SEQNO,A.CARD_NO,A.GROUP_CODE,A.ACCT_TYPE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG, ")
		.append(" A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT,A.NEW_END_DATE, ")
		.append(" A.CURRENT_CODE,A.OPPOST_REASON,A.OPPOST_DATE,D.SPEC_STATUS,D.SPEC_DATE,E.BLOCK_REASON1,E.BLOCK_REASON2, ")
		.append(" E.BLOCK_REASON3,E.BLOCK_REASON4,E.BLOCK_REASON5,E.BLOCK_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION, ")
		.append(" A.HIGHEST_CONSUME_AMT,C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		.append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,A.MAJOR_ID_P_SEQNO,A.CORP_NO AS TMP_CORP_NO ,C.ACCT_STATUS, ")
		.append(" C.AUTOPAY_ACCT_NO,C.AUTOPAY_INDICATOR,A.P_SEQNO,C.STMT_CYCLE,A.ACNO_P_SEQNO,'N' AS VD_FLAG ,F.CURR_CODE ,C.RCRATE_YEAR, ")
		.append(" G.ID_NO AS MAJOR_ID_NO, ")
		.append(" H.ORI_CARD_NO AS MAJOR_ORI_CARD_NO ");
		for (int i = 1; i <= 24; i++) {
			sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		}
		sb.append(" FROM CRD_CARD A ")
		.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
		.append(" LEFT JOIN CCA_CARD_BASE D ON A.CARD_NO = D.CARD_NO ")
		.append(" LEFT JOIN CCA_CARD_ACCT E ON A.ACNO_P_SEQNO = E.ACNO_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACCT_CURR F ON A.P_SEQNO = F.P_SEQNO AND F.CURR_CODE IN ('901','392') ")
		.append(" LEFT JOIN CRD_IDNO G ON A.MAJOR_ID_P_SEQNO = G.ID_P_SEQNO ")
		.append(" LEFT JOIN CRD_CARD H ON A.MAJOR_CARD_NO = H.CARD_NO ")
		.append(" WHERE A.CURR_CODE = '392' ");
		sb.append(" UNION ALL ")		
		.append(" SELECT B.ID_NO,A.ID_P_SEQNO,A.CARD_NO,A.GROUP_CODE,A.ACCT_TYPE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG, ")
		.append(" A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT,A.NEW_END_DATE, ")
		.append(" A.CURRENT_CODE,A.OPPOST_REASON,A.OPPOST_DATE,D.SPEC_STATUS,D.SPEC_DATE,E.BLOCK_REASON1,E.BLOCK_REASON2, ")
		.append(" E.BLOCK_REASON3,E.BLOCK_REASON4,E.BLOCK_REASON5,E.BLOCK_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION, ")
		.append(" A.HIGHEST_CONSUME_AMT,C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		.append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,A.MAJOR_ID_P_SEQNO,A.CORP_NO AS TMP_CORP_NO ,C.ACCT_STATUS, ")
		.append(" C.AUTOPAY_ACCT_NO,C.AUTOPAY_INDICATOR,A.P_SEQNO,C.STMT_CYCLE,A.ACNO_P_SEQNO,'N' AS VD_FLAG ,F.CURR_CODE ,C.RCRATE_YEAR, ")
		.append(" G.ID_NO AS MAJOR_ID_NO, ")
		.append(" H.ORI_CARD_NO AS MAJOR_ORI_CARD_NO ");
		for (int i = 1; i <= 24; i++) {
			sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		}
		sb.append(" FROM CRD_CARD A ")
		.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
		.append(" LEFT JOIN CCA_CARD_BASE D ON A.CARD_NO = D.CARD_NO ")
		.append(" LEFT JOIN CCA_CARD_ACCT E ON A.ACNO_P_SEQNO = E.ACNO_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACCT_CURR F ON A.P_SEQNO = F.P_SEQNO AND F.CURR_CODE IN ('901','840') ")
		.append(" LEFT JOIN CRD_IDNO G ON A.MAJOR_ID_P_SEQNO = G.ID_P_SEQNO ")
		.append(" LEFT JOIN CRD_CARD H ON A.MAJOR_CARD_NO = H.CARD_NO ")
		.append(" WHERE A.CURR_CODE = '840' ");
		sb.append(" UNION ALL ")		
		.append(" SELECT B.ID_NO,A.ID_P_SEQNO,A.CARD_NO,A.GROUP_CODE,A.ACCT_TYPE,''AS TMP_SON_CARD_FLAG,0 AS TMP_INDIV_CRD_LMT, ")
		.append(" 0 AS TMP_LINE_OF_CREDIT_AMT,A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_REASON,A.OPPOST_DATE,D.SPEC_STATUS, ")
		.append(" D.SPEC_DATE,E.BLOCK_REASON1,E.BLOCK_REASON2,E.BLOCK_REASON3,E.BLOCK_REASON4,E.BLOCK_REASON5,E.BLOCK_DATE, ")
		.append(" A.REG_BANK_NO,A.ORI_ISSUE_DATE,'' AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT,0 AS TMP_LINE_OF_CREDIT_AMT_CASH, ")//排除呆帳
		.append(" A.ACCT_NO AS TMP_ACCT_NO,A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,A.MAJOR_ID_P_SEQNO,'' AS TMP_CORP_NO ,C.ACCT_STATUS, ")
		.append(" C.AUTOPAY_ACCT_NO,C.AUTOPAY_INDICATOR,A.P_SEQNO,C.STMT_CYCLE,'' ACNO_P_SEQNO,'Y' AS VD_FLAG ,'901' as CURR_CODE , 0 RCRATE_YEAR,")
		.append(" B.ID_NO as MAJOR_ID_NO,A.ORI_CARD_NO AS MAJOR_ORI_CARD_NO ");
		for (int i = 1; i <= 24; i++) {
			sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		}
		sb.append(" FROM DBC_CARD A ")
		.append(" LEFT JOIN DBC_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN DBA_ACNO C ON A.P_SEQNO = C.P_SEQNO ")
		.append(" LEFT JOIN CCA_CARD_BASE D ON A.CARD_NO = D.CARD_NO ")
		.append(" LEFT JOIN CCA_CARD_ACCT E ON A.P_SEQNO = E. ACNO_P_SEQNO ");
	    if(DBUG)
	    	sb.append(" fetch first 100 rows only ");
		sqlCmd = sb.toString();
		openCursor();
	}
	
	Double doubleAdd(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.add(b2).doubleValue();

	}
	
	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "CRM"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("CRM", ftpCommand);

		if (errCode != 0) {
			showLogMessage("I", "", String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
			commFTP.insertEcsNotifyLog(datFileName, "3", javaProgram, sysDate, sysTime);
			commFTP.insertEcsNotifyLog(hdrFileName, "3", javaProgram, sysDate, sysTime);
		}
	}

	void copyFile(String datFileName1, String fileFolder1 ,String datFileName2, String fileFolder2) throws Exception {
		String tmpstr1 = Paths.get(fileFolder1, datFileName1).toString();
		String tmpstr2 = Paths.get(fileFolder2, datFileName2).toString();

		if (commCrd.fileCopy(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + datFileName2 + "]copy失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + tmpstr1 + "] 已copy至 [" + tmpstr2 + "]");
	}

	public static void main(String[] args) {
		InfR002 proc = new InfR002();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class InfR002Data{
	String idNo = "";
	String idPSeqno = "";
	String cardNo = "";
	String groupCode = "";
	String acctType = "";
	String tmpSonCardFlag = "";
	double tmpIndivCrdLmt = 0;
	double tmpLineOfCreditAmt = 0;
	String newEndDate = "";
	String currentCode = "";
	String oppostReason = "";
	String oppostDate = "";
	String specStatus = "";
	String specDate = "";
	String blockReason1 = "";
	String blockReason2 = "";
	String blockReason3 = "";
	String blockReason4 = "";
	String blockReason5 = "";
	String blockDate = "";
	String regBankNo = "";
	String oriIssueDate = "";
	String tmpMajorRelation = "";
	long highestConsumeAmt= 0;
	long tmpLineOfCreditAmtCash = 0;
	String tmpAcctNo = "";
	String issueDate = "";
	String reissueDate= "";
	String changeDate= "";
	String newCardNo= "";
	String reissueReason = "";
	String majorIdPSeqno = "";
	String tmpCorpNo = "";
	String pSeqno = "";
	String acnoPSeqno = "";
	String vdFlag = "";

	String bankType = "";
	String controlCode = "";
	String controlCodeDate = "";
	String acctStatus = "";
	String autopayAcctNo = "";
	String autopayIndicator = "";
	String lateDate = "";
	String ppCardNo = "";
	String valitTo = "";
	String monthProfile24 = ""; 
	Double rcrateYear = Double.valueOf(0.00);
	double sumInterestAmt = 0;
	double sumEndBal = 0;
	double sumEndBal2 = 0;
	double ttlAmtBal = 0;
	String stmtCycle = "";
	String thisAcctMonth = "";
	String thisLastpayDate = "";
	double sumBilContract = 0;
	String currCode = "";
	String majorIdNo = "";
	String majorOriCardNo = "";
	
}




