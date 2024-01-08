/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/28  V1.00.00   Ryan     program initial                            *
*  112/04/13  V1.00.01   Wilson   調整雙幣卡產生邏輯                                                            *
*  112/04/14  V1.00.02   Ryan     可用餘額邏輯調整                                                               *
*  112/05/08  V1.00.03   Wilson   合併InfS003、新增變更ID註記、判斷參數產生全檔                *
*  112/06/01  V1.00.04   Ryan     修正帶錯欄位 curr_code                         *
*  112/06/16  V1.00.05   Wilson   日期減一日                                                                         *
*  112/06/17  V1.00.06   Wilson   檔名日期yyyymmdd                              *
*  112/08/03  V1.00.07   Wilson   調整讀取雙幣卡邏輯                                                             *  
*  112/08/07  V1.00.08   Wilson   調整寫檔筆數                                                                      *   
*  112/08/28  V1.00.09   Wilson   增加帳戶類別欄位                                                                 *       
*  112/09/22  V1.00.10   Ryan     修正this_acct_month無值問題,增加DEBUG設定                  *    
*  112/09/23  V1.00.13   Sunny    調整逾期未繳次數01-08均調整為00,格式調整14.2f   *  
*  112/09/25  V1.00.14   Ryan     調整部分欄位前面補0           *  
*  112/09/26  V1.00.15   Ryan     調整效能                                                                             *  
*  112/09/26  V1.00.16   Wilson   調整新卡卡號、公司統編處理邏輯                                            *
*  112/09/26  V1.00.17   Sunny    selectActAcagCurr load table加order by       *
*  112/09/27  V1.00.18   Wilson   增加正附卡註記、正卡身分證號欄位                                                          *
*  112/09/27  V1.00.19   Ryan     調整繳款方式,扣繳額度,自動轉帳,24 Month Profile欄位              *
*  112/09/28  V1.00.20   Ryan     調整本年度利息累計 & 去年度利息累計 & 當期刷卡利息 & 當期累計總利息              *
*  112/10/25  V1.00.21   Ryan     調整  公司統編88 補0                                                                           *
*  112/10/26  V1.00.22   Wilson   調整刷卡本金、預現本金讀取邏輯                                                              *
*  112/11/03  V1.00.23   Wilson   調整利率讀取邏輯                                                                                         *
*  112/11/27  V1.00.24   Ryan     調整效能                                                                                   
*  112/11/28  V1.00.25   Ryan     add selectCcaBalance      *
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

public class InfS004 extends AccessDAO {
	private static final int OUTPUT_BUFF_SIZE = 100000;
	private final String progname = "產生送客服中心卡片資料異動檔程式 112/11/28 V1.00.25";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String DATA_FORM = "CCTCRD1X";
	private final static String COL_SEPERATOR = "\006";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	private final static boolean DEBUG = false;
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CalBalance calBalance = null;
	String businessDate = "";
	String wfValue = "";
	
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
			businessDate = searchDate;
			
			// convert YYYYMMDD into YYMMDD
//			String fileNameSearchDate = searchDate.substring(2);
			
			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, searchDate, CommTxInf.DAT_EXTENSION);
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
		
		selectPtrSysParm();
		selectPtrWorkday();
		 if(DEBUG) showLogMessage("I", "", "讀取 selectActAcagCurr()");
		selectActAcagCurr(); //20230924 add
		 if(DEBUG) showLogMessage("I", "", "讀取 selectInfS004Data()");
		selectCrdCardPp2();
		selectTscCard();
		selectIpsCard();
		selectIchCard();
		selectCcaSpecCode();
		selectCcaOppTypeReason();
		selectActDebt();
		selectCrdBalance();
		selectCcaBalance();
		selectCycAbem();
		selectCycBillExt();
		selectInfS004Data(); 
		
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
				InfS004Data infS004Data = getInfData();
				String rowOfDAT = getRowOfDAT(infS004Data);
				sb.append(rowOfDAT);
				rowCount++;
				countInEachBuffer++;
				if (countInEachBuffer == OUTPUT_BUFF_SIZE) {
					showLogMessage("I", "", String.format("將第%d到%d筆資料寫入檔案", rowCount - OUTPUT_BUFF_SIZE, rowCount));
					byte[] tmpBytes = sb.toString().getBytes();
					writeBinFile(tmpBytes, tmpBytes.length);
					sb = new StringBuffer();
					countInEachBuffer = 0;
				}
			}
			
			// write the rest of bytes on the file 
			if (countInEachBuffer > 0) {
				showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
				byte[] tmpBytes = sb.toString().getBytes();
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
	
	void getLateDate(InfS004Data infS004Data) {
		String[] lateDate = new String[3];
		lateDate[0] = infS004Data.issueDate;
		lateDate[1] = infS004Data.reissueDate;
		lateDate[2] = infS004Data.changeDate;
		Arrays.parallelSort(lateDate);
		
		infS004Data.lateDate = commDate.toTwDate(lateDate[2]);
	}
	
	/***
	 * 讀取PP卡資料(限信用卡)
	 * @param infS004Data
	 * @throws Exception
	 */
//	void selectCrdCardPp(InfS004Data infS004Data) throws Exception {
//		extendField = "CRD_CARD_PP.";
//		sqlCmd = " select PP_CARD_NO,VALID_TO ";
//		sqlCmd += " from CRD_CARD_PP ";
//		sqlCmd += " WHERE VIP_KIND = '1' AND ID_P_SEQNO = ? ";
//		sqlCmd += " ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY ";
//		setString(1,infS004Data.idPSeqno);
//		selectTable();
//
//		infS004Data.ppCardNo = getValue("CRD_CARD_PP.PP_CARD_NO");
//		infS004Data.validTo = getValue("CRD_CARD_PP.VALID_TO");
//	}
	
	/***
	 * 讀取PP卡資料(限信用卡)
	 * 讀取龍騰卡資料(限信用卡)    
	 * @param infS004Data
	 * @throws Exception
	 */
	void selectCrdCardPp2() throws Exception {
		daoTable    = "CRD_CARD_PP";
		extendField = "CRD_CARD_PP2.";
		sqlCmd = " select ID_P_SEQNO,PP_CARD_NO ,VALID_TO ,VIP_KIND";
		sqlCmd += " from CRD_CARD_PP ";
		sqlCmd += " WHERE VIP_KIND in ('1','2') ";
		int n = loadTable();
		setLoadData("CRD_CARD_PP2.ID_P_SEQNO");
		showLogMessage("I", "","selectCrdCardPp2  取得= [" +n+ "]筆");
	}	
	
	void getCrdCardPp2(InfS004Data infS004Data) throws Exception {
		setValue("CRD_CARD_PP2.ID_P_SEQNO",infS004Data.idPSeqno);
		int n = getLoadData("CRD_CARD_PP2.ID_P_SEQNO");
		for(int i = 0;i<n;i++) {
			if("1".equals(getValue("CRD_CARD_PP2.VIP_KIND",i))) {
				infS004Data.ppCardNo = getValue("CRD_CARD_PP2.PP_CARD_NO",i);
				infS004Data.validTo = getValue("CRD_CARD_PP2.VALID_TO",i);
			}
			if("2".equals(getValue("CRD_CARD_PP2.VIP_KIND",i))) {
				infS004Data.dpCardNo = getValue("CRD_CARD_PP2.PP_CARD_NO",i);
				infS004Data.dpValidTo = getValue("CRD_CARD_PP2.VALID_TO",i);
			}
		}
	}
	
	void selectTscCard() throws Exception {
		daoTable    = "TSC_CARD";
		extendField = "TSC_CARD.";
		sqlCmd = " select CARD_NO,TSC_CARD_NO AS ELEC_CARD_NO,AUTOLOAD_FLAG ";
		sqlCmd += " from TSC_CARD ";
		int n = loadTable();
		setLoadData("TSC_CARD.CARD_NO");
		showLogMessage("I", "","selectTscCard 取得= [" +n+ "]筆");

	}	
	
	void getTscCard(InfS004Data infS004Data) throws Exception {
		setValue("TSC_CARD.CARD_NO",infS004Data.cardNo);
		int n = getLoadData("TSC_CARD.CARD_NO");
		infS004Data.elecCarNo = getValue("TSC_CARD.ELEC_CARD_NO");
		infS004Data.autoloadFlag = getValue("TSC_CARD.AUTOLOAD_FLAG");
	}	
	
	void selectIpsCard() throws Exception {
		daoTable = "IPS_CARD";
		extendField = "IPS_CARD.";
		sqlCmd = " select CARD_NO,IPS_CARD_NO AS ELEC_CARD_NO,AUTOLOAD_FLAG ";
		sqlCmd += " from IPS_CARD ";
		int n = loadTable();
		setLoadData("IPS_CARD.CARD_NO");
		showLogMessage("I", "","selectIpsCard 取得= [" +n+ "]筆");
	}	
	
	void getIpsCard(InfS004Data infS004Data) throws Exception {
		setValue("IPS_CARD.CARD_NO",infS004Data.cardNo);
		int n = getLoadData("IPS_CARD.CARD_NO");
		infS004Data.elecCarNo = getValue("IPS_CARD.ELEC_CARD_NO");
		infS004Data.autoloadFlag = getValue("IPS_CARD.AUTOLOAD_FLAG");
	}	
	
	void selectIchCard() throws Exception {
		daoTable = "ICH_CARD";
		extendField = "ICH_CARD.";
		sqlCmd = " select CARD_NO,ICH_CARD_NO AS ELEC_CARD_NO,AUTOLOAD_FLAG ";
		sqlCmd += " from ICH_CARD ";
		int n = loadTable();
		setLoadData("ICH_CARD.CARD_NO");
		showLogMessage("I", "","selectIchCard 取得= [" +n+ "]筆");
	}	
	
	void getIchCard(InfS004Data infS004Data) throws Exception {
		setValue("ICH_CARD.CARD_NO",infS004Data.cardNo);
		int n = getLoadData("ICH_CARD.CARD_NO");
		infS004Data.elecCarNo = getValue("ICH_CARD.ELEC_CARD_NO");
		infS004Data.autoloadFlag = getValue("ICH_CARD.AUTOLOAD_FLAG");
	}	
	
	void selectTscVdCard(InfS004Data infS004Data) throws Exception {
		extendField = "TSC_VD_CARD.";
		sqlCmd = " select TSC_CARD_NO AS ELEC_CARD_NO,AUTOLOAD_FLAG ";
		sqlCmd += " from TSC_VD_CARD ";
		sqlCmd += " WHERE VD_CARD_NO = ? ";
		sqlCmd += " ORDER BY CRT_DATE FETCH FIRST 1 ROWS ONLY ";
		setString(1,infS004Data.cardNo);
		selectTable();

		infS004Data.elecCarNo = getValue("TSC_VD_CARD.ELEC_CARD_NO");
		infS004Data.autoloadFlag = getValue("TSC_VD_CARD.AUTOLOAD_FLAG");
	}	
	
	/***
	 * 刷卡消費疑異帳款 ,預借現金疑異帳款
	 * @param infS004Data
	 * @throws Exception
	 */
	void selectCycAbem() throws Exception{
		daoTable = "CYC_ABEM";
		extendField = "CYC_ABEM.";
		sqlCmd = " select '01' as cycle_type ,P_SEQNO,sum(decode(ACCT_CODE,'CA',DC_DEST_AMT,'CF',DC_DEST_AMT,0)) as sum_dc_dest_amt1 ";
		sqlCmd += " ,sum(decode(ACCT_CODE,'CA',0,'CF',0,DC_DEST_AMT)) as sum_dc_dest_amt2 ";
		sqlCmd += " from CYC_ABEM_01 ";
		sqlCmd += " where print_type = '08' ";
		sqlCmd += " group by P_SEQNO ";
		sqlCmd += " UNION ALL ";
		sqlCmd = " select '20' as cycle_type ,P_SEQNO,sum(decode(ACCT_CODE,'CA',DC_DEST_AMT,'CF',DC_DEST_AMT,0)) as sum_dc_dest_amt1 ";
		sqlCmd += " ,sum(decode(ACCT_CODE,'CA',0,'CF',0,DC_DEST_AMT)) as sum_dc_dest_amt2 ";
		sqlCmd += " from CYC_ABEM_20 ";
		sqlCmd += " where print_type = '08' ";
		sqlCmd += " group by P_SEQNO ";
		sqlCmd += " UNION ALL ";
		sqlCmd = " select '25' as cycle_type ,P_SEQNO,sum(decode(ACCT_CODE,'CA',DC_DEST_AMT,'CF',DC_DEST_AMT,0)) as sum_dc_dest_amt1 ";
		sqlCmd += " ,sum(decode(ACCT_CODE,'CA',0,'CF',0,DC_DEST_AMT)) as sum_dc_dest_amt2 ";
		sqlCmd += " from CYC_ABEM_25 ";
		sqlCmd += " where print_type = '08' ";
		sqlCmd += " group by P_SEQNO ";
		int n = loadTable();
		setLoadData("CYC_ABEM.P_SEQNO,CYC_ABEM.cycle_type");
		showLogMessage("I", "","selectCycAbem 取得= [" +n+ "]筆");
	}
	
	
	void getCycAbem(InfS004Data infS004Data) throws Exception{
		if(commStr.empty(infS004Data.stmtCycle))
			return;
		setValue("CYC_ABEM.P_SEQNO",infS004Data.pSeqno);
		setValue("CYC_ABEM.cycle_type",infS004Data.stmtCycle);
		getLoadData("CYC_ABEM.P_SEQNO,CYC_ABEM.cycle_type");
		infS004Data.sumDcDestAmt1 = getValueDouble("CYC_ABEM.sum_dc_dest_amt1");
		infS004Data.sumDcDestAmt2 = getValueDouble("CYC_ABEM.sum_dc_dest_amt2");
	}
	
	
	/***
	 * 讀取票證相關資料
	 * @param infS004Data
	 * @throws Exception
	 */
	void getElectronicData(InfS004Data infS004Data) throws Exception {
		if("N".equals(infS004Data.vdFlag)){
			if("01".equals(infS004Data.electronicCode)) {
				getTscCard(infS004Data);
			}
			if("02".equals(infS004Data.electronicCode)) {
				getIpsCard(infS004Data);
			}
			if("03".equals(infS004Data.electronicCode)) {
				getIchCard(infS004Data);
			}
		}
		if("Y".equals(infS004Data.vdFlag)){
			if("01".equals(infS004Data.electronicCode)) {
				selectTscVdCard(infS004Data);
			}
		}
	}

	/***
	 * 讀取特指原因說明((1)讀取到的SPEC_STATUS有值才做)
	 * @param infS004Data
	 * @throws Exception
	 */
	void selectCcaSpecCode() throws Exception {
		daoTable = "CCA_SPEC_CODE";
		extendField = "CCA_SPEC_CODE.";
		sqlCmd = " SELECT SPEC_CODE,SPEC_DESC ";
		sqlCmd += " from CCA_SPEC_CODE ";
		sqlCmd += " WHERE SPEC_TYPE = '2' ";
		int n = loadTable();
		setLoadData("CCA_SPEC_CODE.SPEC_CODE");
		showLogMessage("I", "","selectCcaSpecCode 取得= [" +n+ "]筆");
	}
	
	void getCcaSpecCode(InfS004Data infS004Data) throws Exception {
		if(commStr.empty(infS004Data.specStatus) || "ID".equals(infS004Data.specStatus))
			return;
		setValue("CCA_SPEC_CODE.SPEC_CODE",infS004Data.specStatus);
		getLoadData("CCA_SPEC_CODE.SPEC_CODE");

		infS004Data.specDesc = getValue("CCA_SPEC_CODE.SPEC_DESC");
	}
	
	/***
	 * 讀取停掛原因說明((1)讀取到的OPPOST_REASON有值才做)
	 * @param infS004Data
	 * @throws Exception
	 */
	void selectCcaOppTypeReason() throws Exception {
		daoTable = "CCA_SPEC_CODE";
		extendField = "CCA_SPEC_CODE.";
		sqlCmd = " SELECT OPP_STATUS,OPP_REMARK ";
		sqlCmd += " FROM CCA_OPP_TYPE_REASON ";
		int n = loadTable();
		setLoadData("CCA_SPEC_CODE.OPP_STATUS");
		showLogMessage("I", "","selectCcaOppTypeReason 取得= [" +n+ "]筆");
	}
	
	void getCcaOppTypeReason(InfS004Data infS004Data) throws Exception {
		if(commStr.empty(infS004Data.oppostReason))
			return;
		setValue("CCA_SPEC_CODE.OPP_STATUS",infS004Data.oppostReason);
		getLoadData("CCA_SPEC_CODE.OPP_STATUS");

		infS004Data.oppRemark = getValue("CCA_SPEC_CODE.OPP_REMARK");
	}
	
	Double sub(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.subtract(b2).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();

	}

	/***
	 * 取得銀行別
	 * @param infS004Data
	 */
	void getBankType(InfS004Data infS004Data) {
		String bankType = "";
//		a.若(1)讀取到的GROUP_CODE=6673、6674  放106
		if(commStr.pos(",6673,6674", infS004Data.groupCode)>0) {
			if(infS004Data.tmpCurrCode.equals("840")) {
				bankType = "606";
			}
			else if(infS004Data.tmpCurrCode.equals("392")) {
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
			if("01".equals(infS004Data.acctType)) {
				bankType = "106";
			}
			if("90".equals(infS004Data.acctType)) {
				bankType = "206";
			}
			if(commStr.pos(",03,06", infS004Data.acctType)>0) {
				bankType = "306";
			}
		}
		infS004Data.bankType = bankType;
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
	
	void getPtrWorkday(InfS004Data infS004Data) throws Exception{
		setValue("workday.stmt_cycle",infS004Data.stmtCycle);
        getLoadData("workday.stmt_cycle");
        infS004Data.thisAcctMonth = getValue("workday.this_acct_month");
        infS004Data.thisLastpayDate = getValue("workday.this_lastpay_date");
	}
	
	/***
	 * 最低應繳餘額
	 * @param infR002Data
	 * @param month
	 * @return
	 * @throws Exception
	 */
	/*
	double selectActAcagCurr(InfS004Data infS004Data,int month) throws Exception {
		if(commStr.empty(infS004Data.thisAcctMonth))
			return 0;
		extendField = "act_acag_curr.";
		sqlCmd = " select DC_PAY_AMT ";
		sqlCmd += " from act_acag_curr ";
		sqlCmd += " where acct_month = to_char(add_months(to_date(?||'01','yyyymmdd'), ?), 'yyyymm') ";
		sqlCmd += " and curr_code = ? and p_seqno = ? ";
		setString(1,infS004Data.thisAcctMonth);
		setInt(2,month);
		setString(3,infS004Data.tmpCurrCode);
		setString(4,infS004Data.pSeqno);
		selectTable();

		return getValueDouble("act_acag_curr.DC_PAY_AMT");
	}*/
	
	/***
	 * 最低應繳餘額
	 * @param infR002Data
	 * @param month
	 * @return
	 * @throws Exception
	 */
	
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
	
	double getDcPayAmt(InfS004Data infS004Data , int month) throws Exception{
		String acctMonth = commDate.monthAdd(infS004Data.thisAcctMonth, month);
		setValue("acagcurr.curr_code",infS004Data.tmpCurrCode);
		setValue("acagcurr.p_seqno",infS004Data.pSeqno);
		setValue("acagcurr.acct_month",acctMonth);
        getLoadData("acagcurr.curr_code,acagcurr.p_seqno,acagcurr.acct_month");
		return getValueDouble("acagcurr.dc_pay_amt");
	}
	
	/***
	 * 08最低應繳餘額
       ---剩餘的ACCT_MONTH金額都要加在這一欄
	 * @param infR002Data
	 * @return
	 * @throws Exception
	 */
	double getDcPayAmt8(InfS004Data infS004Data) throws Exception{
		double sumDcPayAmt = 0;
		String acctMonth = commDate.monthAdd(infS004Data.thisAcctMonth, -8);
		setValue("acagcurr.curr_code",infS004Data.tmpCurrCode);
		setValue("acagcurr.p_seqno",infS004Data.pSeqno);
        int resultCnt = getLoadData("acagcurr.curr_code,acagcurr.p_seqno");
        for(int i =0 ;i<resultCnt;i++) {
        	if(acctMonth.compareTo(getValue("acagcurr.acct_month",i)) >= 0) {
        		sumDcPayAmt = add(sumDcPayAmt,getValueDouble("acagcurr.dc_pay_amt",i));
        	}
        }
		return sumDcPayAmt;
	}
	
    Double add(Double v1, Double v2) {

		BigDecimal b1 = new BigDecimal(v1.toString());

		BigDecimal b2 = new BigDecimal(v2.toString());

		return b1.add(b2).doubleValue();

	}

	/***
	 * 08最低應繳餘額
       ---剩餘的ACCT_MONTH金額都要加在這一欄
	 * @param infR002Data
	 * @return
	 * @throws Exception
	 */
//	double selectActAcagCurr8(InfS004Data infS004Data) throws Exception {
//		if(commStr.empty(infS004Data.thisAcctMonth))
//			return 0;
//		extendField = "act_acag_curr8.";
//		sqlCmd = " select  SUM(DC_PAY_AMT) as SUM_DC_PAY_AMT ";
//		sqlCmd += " from act_acag_curr ";
//		sqlCmd += " where acct_month <= to_char(add_months(to_date(?||'01','yyyymmdd'), -8), 'yyyymm') ";
//		sqlCmd += " and curr_code = ? and p_seqno = ? ";
//		setString(1,infS004Data.thisAcctMonth);
//		setString(2,infS004Data.tmpCurrCode);
//		setString(3,infS004Data.pSeqno);
//		selectTable();
//
//		return getValueDouble("act_acag_curr8.SUM_DC_PAY_AMT");
//	}

	void get24MonthProfile(InfS004Data infS004Data) throws Exception {
		for (int i = 1; i <= 24; i++) {
			String paymentRate = commStr.decode(getValue(String.format("PAYMENT_RATE%s", i))
					,",0A,0B,0C,0D,0E" ,",B,B,1,1,0" );
			if(commStr.pos(",B,B,1,1,0", paymentRate) < 0)
				paymentRate = "2";
			infS004Data.monthProfile24 += paymentRate;
		}
	}

	void selectCycBillExt() throws Exception {
	    daoTable    = "CYC_BILL_EXT";
		extendField = "billExt.";
		sqlCmd = " select P_SEQNO,ACCT_YEAR,CURR_CODE,INTEREST_AMT_01,INTEREST_AMT_02,INTEREST_AMT_03,INTEREST_AMT_04,INTEREST_AMT_05 ";
		sqlCmd += " ,INTEREST_AMT_06,INTEREST_AMT_07,INTEREST_AMT_08,INTEREST_AMT_09,INTEREST_AMT_10 ";
		sqlCmd += " ,INTEREST_AMT_11,INTEREST_AMT_12 ";
		sqlCmd += " from CYC_BILL_EXT ";
		sqlCmd += " where (INTEREST_AMT_01 + INTEREST_AMT_02 + INTEREST_AMT_03 + INTEREST_AMT_04 + INTEREST_AMT_05 ";
		sqlCmd += " + INTEREST_AMT_06 + INTEREST_AMT_07 + INTEREST_AMT_08 + INTEREST_AMT_09 + INTEREST_AMT_10 ";
		sqlCmd += " + INTEREST_AMT_11 + INTEREST_AMT_12) > 0 ";
	    int n = loadTable();
	    setLoadData("billExt.P_SEQNO,billExt.CURR_CODE");
	    showLogMessage("I", "","selectCycBillExt 取得= [" +n+ "]筆");
	}
	
	/***
	 * 本年度利息累計 & 去年度利息累計 & 當期刷卡利息 & 當期累計總利息
	 * @param infS004Data
	 * @throws Exception
	 */
	void getCycBillExt(InfS004Data infS004Data) throws Exception {
		String thisYear = commStr.left(infS004Data.thisAcctMonth, 4);
		String lastYear = commStr.left(commDate.dateAdd(infS004Data.thisAcctMonth, -1, 0, 0), 4);
		String thisMonth = commStr.right(infS004Data.thisAcctMonth, 2);
		if(commStr.empty(thisMonth)) {
			return;
		}		
		setValue("billExt.P_SEQNO",infS004Data.idPSeqno);
		setValue("billExt.CURR_CODE",infS004Data.tmpCurrCode);
		double thisYearInterestAmt = 0;
		double lastYearInterestAmt = 0;
		double thisMonthInterestAmt = 0;
		int n = getLoadData("billExt.P_SEQNO,billExt.CURR_CODE");
		for(int i=0;i<n;i++) {
			if(thisYear.equals(getValue("billExt.ACCT_YEAR",i))) {
				for(int ii = 1 ; ii <= 12 ; ii++ ) {
					thisYearInterestAmt += getValueDouble(String.format("INTEREST_AMT_%02d", ii),i);
				}
				thisMonthInterestAmt += getValueDouble(String.format("INTEREST_AMT_%s", thisMonth),i);
			}
			if(lastYear.equals(getValue("billExt.ACCT_YEAR",i))) {
				for(int ii = 1 ; ii <= 12 ; ii++ ) {
					lastYearInterestAmt += getValueDouble(String.format("INTEREST_AMT_%02d", ii),i);
				}
			}
		}
		infS004Data.thisYearInterestAmt = thisYearInterestAmt;
		infS004Data.lastYearInterestAmt = lastYearInterestAmt;
		infS004Data.thisMonthInterestAmt = thisMonthInterestAmt;
	}
	
	
	void selectActDebt() throws Exception {
		daoTable = "ACT_DEBT";
		extendField = "ACT_DEBT.";
		sqlCmd = " select p_seqno,curr_code,acct_code,sum(dc_end_bal) as sum_end_bal ";
		sqlCmd += "  from act_debt ";
		sqlCmd += " where acct_code in('CA','CF','CA','CF','DP') ";
		sqlCmd += " group by p_seqno,curr_code,acct_code  ";
		int n = loadTable();
		setLoadData("ACT_DEBT.P_SEQNO,ACT_DEBT.acct_code");
		showLogMessage("I", "","selectActDebt 取得= [" +n+ "]筆");
	}
	
	/***
	 * 預現本金
	 * @param infS004Data
	 * @throws Exception
	 */
	void getActDebt(InfS004Data infS004Data) throws Exception {
		setValue("ACT_DEBT.P_SEQNO",infS004Data.pSeqno);
		setValue("ACT_DEBT.acct_code","CA");
		int n1 = getLoadData("ACT_DEBT.P_SEQNO");
		double sumEndBal = 0;
		for(int i = 0; i<n1;i++) {
			sumEndBal += getValueDouble("ACT_DEBT.sum_end_bal",i);
		}
		
		setValue("ACT_DEBT.P_SEQNO",infS004Data.pSeqno);
		setValue("ACT_DEBT.acct_code","CF");
		int n2 = getLoadData("ACT_DEBT.P_SEQNO");
		for(int i = 0; i<n2;i++) {
			sumEndBal += getValueDouble("ACT_DEBT.sum_end_bal",i);
		}

		infS004Data.sumEndBal = sumEndBal;
	}
	
	/***
	 * 刷卡本金
	 * @param infS004Data
	 * @throws Exception
	 */
	void getActDebt2(InfS004Data infS004Data) throws Exception {
		double sumEndBal = 0;
		setValue("ACT_DEBT.P_SEQNO",infS004Data.pSeqno);
		setValue("ACT_DEBT.acct_code","DP");
		int n1 = getLoadData("ACT_DEBT.P_SEQNO");
		for(int i = 0; i<n1;i++) {
			sumEndBal += getValueDouble("ACT_DEBT.sum_end_bal",i);
		}
		sumEndBal += infS004Data.sumEndBal;
		
		infS004Data.sumEndBal2 = sumEndBal;
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
	
	/***
	   * 可用餘額
	 * @param infS004Data
	 * @return
	 * @throws Exception
	 */
	long getCrdBalance(InfS004Data infS004Data) throws Exception {
		
		if("N".equals(infS004Data.vdFlag)){
			if("Y".equals(infS004Data.tmpSonCardFlag)) {
				setValue("BALANCE_CAL1.CARD_NO", infS004Data.cardNo);
				getLoadData("BALANCE_CAL1.CARD_NO");
				return getValueLong("BALANCE_CAL1.CARD_AMT_BALANCE");
			}else {
				setValue("BALANCE_CAL2.ACNO_P_SEQNO", infS004Data.acnoPSeqno);
				getLoadData("BALANCE_CAL2.ACNO_P_SEQNO");
				return getValueLong("BALANCE_CAL2.ACCT_AMT_BALANCE");
			}	
		}else {
			return 0;
		}
	}
	
	
	String getBillApplyFlag(InfS004Data infS004Data) {
		switch(infS004Data.billApplyFlag) {
		case"1":
			return "0001";
		case"2":
			return "0002";
		case"3":
			return "0003";
		case"4":
			return "0004";
		}
		
		return "0000";
	}

	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfS004Data infS004Data) throws Exception {
		StringBuffer sb = new StringBuffer();
		//取得thisAcctMonth
		getPtrWorkday(infS004Data);
		//讀取PP卡資料(限信用卡)
//		selectCrdCardPp(infS004Data);
		//讀取龍騰卡資料 & PP卡資料 (限信用卡)
		getCrdCardPp2(infS004Data);
		//讀取票證相關資料
		getElectronicData(infS004Data);
		//讀取特指原因說明((1)讀取到的SPEC_STATUS有值才做)
		getCcaSpecCode(infS004Data); 
		//讀取停掛原因說明((1)讀取到的OPPOST_REASON有值才做)
		getCcaOppTypeReason(infS004Data);
		//取得銀行別
		getBankType(infS004Data);
		//取得最近製卡日
		getLateDate(infS004Data);
		//取得24 Month Profile
		get24MonthProfile(infS004Data);
		//預現本金
		getActDebt(infS004Data);
		//刷卡本金 & 預現本金
	    getActDebt2(infS004Data);
		//可用餘額
		long availableBalance = getCrdBalance(infS004Data);
		//刷卡消費疑異帳款 & 預借現金疑異帳款
		getCycAbem(infS004Data);
		//本年度利息累計 & 去年度利息累計 & 當期刷卡利息 & 當期累計總利息
 		getCycBillExt(infS004Data);
		
		sb.append(commCrd.fixLeft(infS004Data.idNo, 16)); //身分證號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.cardNo, 16));//卡號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.bankType, 3));//銀行別
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infS004Data.groupCode, 1,3), 3));//卡別
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(10,("Y".equals(infS004Data.tmpSonCardFlag)?infS004Data.tmpIndivCrdLmt:infS004Data.tmpLineOfCreditAmt)), 10));//卡片額度
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infS004Data.newEndDate, 4,2) + commStr.mid(infS004Data.newEndDate, 2,2), 4));//卡片有效期限
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));//卡片序號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.currentCode, 1));//卡片狀況
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,commDate.toTwDate(infS004Data.oppostDate)), 7));//卡片狀況異動日  
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(5,infS004Data.regBankNo), 5));  //分行代號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,commDate.toTwDate(infS004Data.oriIssueDate)), 7));  //開戶日 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.tmpMajorRelation, 2));  //主卡關係 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.highestConsumeAmt), 14));  //最高消費紀錄
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("1".equals(infS004Data.rcUseIndicator)?"1":"2", 1));  //繳款方式(1:可循環, 2:一次繳清 )
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(16,infS004Data.autopayAcctNoCurr), 16));  //轉帳帳號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("1".equals(infS004Data.autopayIndicator)?"20":"2".equals(infS004Data.autopayIndicator)?"10":"", 2));  //扣繳額度  10最低 20全額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.empty(infS004Data.autopayAcctNo)?"0":"1", 1));  //自動轉帳 0: 不自動轉帳 1: 自動轉帳
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(10,availableBalance), 10));   //可用餘額 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(10, infS004Data.tmpLineOfCreditAmtCash), 10));  //預現額度
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(16,!"901".equals(infS004Data.tmpCurrCode)?infS004Data.tmpCurrChangeAccout:infS004Data.tmpAcctNo), 16));  //金融卡帳號
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(16,""), 16));  //催收帳號 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,infS004Data.lateDate), 7));  //最近製卡日
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("N".equals(infS004Data.vdFlag)?infS004Data.ppCardNo:"", 16));  //PP卡卡號 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("N".equals(infS004Data.vdFlag)?commStr.mid(infS004Data.validTo, 2,4):"", 5));  //PP卡有效期限
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
	    if(DEBUG)showLogMessage("I", "", "逾期欄位處理開始 ID["+infS004Data.idNo+"]");
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt(infS004Data,0)), 14));  //逾期末繳款紀錄25 逾期未繳記錄-本期應繳金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt(infS004Data,-1)), 14));  //逾期末繳款紀錄26 逾期未繳記錄-上期應繳金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		// 20230923 sunny 逾期未繳次數，客服要求一律放00(CRM-InfR002卡片檔的作法仍維持01-08)
		sb.append(commCrd.fixLeft("00", 2));  //逾期未繳款紀錄27 逾期未繳記錄- 上期應繳
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt(infS004Data,-2)), 14));		//逾期末繳款紀錄28 逾期未繳記錄-逾30天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));  //逾期未繳款紀錄29 逾期未繳記錄-逾30天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt(infS004Data,-3)), 14));	//逾期末繳款紀錄30 逾期未繳記錄-逾60天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));  //逾期未繳款紀錄31 逾期未繳記錄-逾60天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt(infS004Data,-4)), 14));  //逾期末繳款紀錄32 逾期未繳記錄-逾90天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));  //逾期未繳款紀錄33 逾期未繳記錄-逾90天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt(infS004Data,-5)), 14));  //逾期末繳款紀錄34 逾期未繳記錄-逾120天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));  //逾期未繳款紀錄35 逾期未繳記錄-逾120天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt(infS004Data,-6)), 14));  //逾期末繳款紀錄36 逾期未繳記錄-逾150天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));  //逾期未繳款紀錄37 逾期未繳記錄-逾150天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt(infS004Data,-7)), 14));  //逾期末繳款紀錄38 逾期未繳記錄-逾180天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));  //逾期未繳款紀錄39 逾期未繳記錄-逾180天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%014.2f", getDcPayAmt8(infS004Data)), 14)); ;  //逾期末繳款紀錄40 逾期未繳記錄-逾210天金額
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));  //逾期未繳款紀錄41 逾期未繳記錄-逾210天
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		if(DEBUG)showLogMessage("I", "", "逾期欄位處理結束");
		sb.append(commCrd.fixLeft(infS004Data.monthProfile24, 24)); //24 Month Profile   
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.newCardNo.length() == 0 ? infS004Data.oldCardNo : infS004Data.newCardNo, 16));  //新卡卡號  
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,!commStr.empty(infS004Data.newCardNo)?commDate.toTwDate(infS004Data.reissueDate):""), 7));  //轉卡生效日期 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記45
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記46
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記47
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記48
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記49
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記50
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記51
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記52
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //費用免除註記53
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //基本率 54
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //基本率調幅率55
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(10,""), 10));  //基本額度 56
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //率 57
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //率調幅率58
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(10,""), 10));  //額度 59
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //率 60
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //率調幅率61
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //調整註記 62
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.thisYearInterestAmt), 14));  //本年度利息累計63 CYC_BILL_EXT.ACCT_YEAR(當年) sum(INTEREST_AMT_1~12)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.lastYearInterestAmt), 14));  //去年度利息累計64
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat2(7,String.valueOf(new BigDecimal(infS004Data.rcrateYear.toString()).divide(BigDecimal.valueOf(100)).doubleValue())), 7));  //基本率(卡片利率) 65
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //基本率調幅率 66
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(10,""), 10));  //預現 -基本額度67
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //預現 - 2ND 率68
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //預現 - 2ND率調幅率69
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(10,""), 10));  //預現 - 2ND額度70
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //預現 - 3RD 率71
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,""), 7));  //預現 - 3RD 率調幅率72
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0", 1));  //預現 -調整註記73
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.thisYearInterestAmt), 14));  //本年度利息累計74 CYC_BILL_EXT.ACCT_YEAR(當年) sum(INTEREST_AMT_1~12)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.lastYearInterestAmt), 14));  //去年度利息累計
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("00", 2));  //優惠辦法76
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("0000", 4));  //優惠截止日77
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.sumDcDestAmt2), 14));  //刷卡消費疑異帳款78  
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.sumDcDestAmt1), 14));  //預借現金?異帳款   79  
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,0.00), 14));  //當期預借利息80
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.thisMonthInterestAmt), 14));  //當期刷卡利息81
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.thisMonthInterestAmt), 14));  //當期累計總利息82
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,0.00), 14));  //83
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,0.00), 14));  //前年累計利息84
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(14,infS004Data.sumEndBal), 14));  //預現本金85
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(15,infS004Data.sumEndBal2), 15));  //刷卡本金86
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(7,commDate.toTwDate(infS004Data.birthday)), 7));  //客戶生日 87
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(strFormat(16,infS004Data.tmpCorpNo + "00"), 16));  //公司統編88
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.maintainDate, 10));  //最近維護日 89
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infS004Data.chiName, 15),30));  //客戶姓名90 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.dpCardNo,16));  //龍騰卡卡號91
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infS004Data.dpValidTo, 2,4), 5));  //龍騰卡有效期限92
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.elecCarNo, 16));  //票證卡號93
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.autoloadFlag, 1));  //自動加值註記94
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.groupCode, 4));  //團體代碼95
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(strFormat(3,!"0060567".equals(infS004Data.autopayAcctBank)?commStr.left(infS004Data.autopayAcctNoCurr, 3):"006"), 3));  //ACH扣繳銀行代碼96
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.specStatus, 2)); //卡特指 97
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("ID".equals(infS004Data.specStatus)?"ID已變更":!commStr.empty(infS004Data.specStatus)?infS004Data.specDesc:"", 120)); //卡特指說明 98
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.oppostReason, 2));  //停掛原因  99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(!commStr.empty(infS004Data.oppostReason)?infS004Data.oppRemark:"", 120));  //停掛原因說明 100
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(getBillApplyFlag(infS004Data), 4));  //郵寄地址旗標 101
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.billSendingZip, 6));  //其他地址郵遞區號102
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(String.format("%s%s", infS004Data.billSendingAddr1 , infS004Data.billSendingAddr2), 25), 50));  //其他地址一103
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(String.format("%s%s%s", infS004Data.billSendingAddr3 , infS004Data.billSendingAddr4 , infS004Data.billSendingAddr5), 25), 50));  //其他地址二104
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.chgIdFlag, 1));  //變更ID註記  105
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.acctType, 2));  //帳戶類別  106		
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.supFlag, 1));  //正附卡註記  107
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS004Data.majorIdNo, 10));  //正卡身分證號  108
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	InfS004Data getInfData() throws Exception {
		InfS004Data infS004Data = new InfS004Data();
		infS004Data.idNo = getValue("ID_NO");
		infS004Data.pSeqno = getValue("P_SEQNO");
		infS004Data.idPSeqno = getValue("ID_P_SEQNO");
		infS004Data.cardNo = getValue("CARD_NO");
		infS004Data.groupCode = getValue("GROUP_CODE");
		infS004Data.acctType = getValue("ACCT_TYPE");
		infS004Data.tmpSonCardFlag = getValue("TMP_SON_CARD_FLAG");
		infS004Data.tmpIndivCrdLmt = getValueLong("TMP_INDIV_CRD_LMT");
		infS004Data.tmpLineOfCreditAmt = getValueLong("TMP_LINE_OF_CREDIT_AMT");
		infS004Data.newEndDate = getValue("NEW_END_DATE");
		infS004Data.currentCode = getValue("CURRENT_CODE");
		infS004Data.oppostDate = getValue("OPPOST_DATE");
		infS004Data.regBankNo = getValue("REG_BANK_NO");
		infS004Data.oriIssueDate = getValue("ORI_ISSUE_DATE");
		infS004Data.tmpMajorRelation = getValue("TMP_MAJOR_RELATION");
		infS004Data.highestConsumeAmt= getValueDouble("HIGHEST_CONSUME_AMT");
		infS004Data.tmpLineOfCreditAmtCash = getValueLong("TMP_LINE_OF_CREDIT_AMT_CASH");
		infS004Data.tmpCurrCode = getValue("TMP_CURR_CODE");
		infS004Data.tmpCurrChangeAccout = getValue("TMP_CURR_CHANGE_ACCOUT");
		infS004Data.tmpAcctNo = getValue("TMP_ACCT_NO");
		infS004Data.issueDate = getValue("ISSUE_DATE");
		infS004Data.reissueDate= getValue("REISSUE_DATE");
		infS004Data.changeDate= getValue("CHANGE_DATE");
		infS004Data.newCardNo= getValue("NEW_CARD_NO");
		infS004Data.birthday= getValue("BIRTHDAY");
		infS004Data.tmpCorpNo = getValue("TMP_CORP_NO");
		infS004Data.maintainDate = getValue("MAINTAIN_DATE");
		infS004Data.chiName = getValue("CHI_NAME");
		infS004Data.electronicCode = getValue("ELECTRONIC_CODE");
		infS004Data.specStatus = getValue("SPEC_STATUS");
		infS004Data.oppostReason = getValue("OPPOST_REASON");
		infS004Data.billApplyFlag = getValue("BILL_APPLY_FLAG");
		infS004Data.billSendingZip = getValue("BILL_SENDING_ZIP");
		infS004Data.billSendingAddr1 = getValue("BILL_SENDING_ADDR1");
		infS004Data.billSendingAddr2 = getValue("BILL_SENDING_ADDR2");
		infS004Data.billSendingAddr3 = getValue("BILL_SENDING_ADDR3");
		infS004Data.billSendingAddr4 = getValue("BILL_SENDING_ADDR4");
		infS004Data.billSendingAddr5 = getValue("BILL_SENDING_ADDR5");
		infS004Data.reissueReason = getValue("REISSUE_REASON");
		infS004Data.acctStatus = getValue("ACCT_STATUS");
		infS004Data.autopayAcctNo = getValue("AUTOPAY_ACCT_NO");
		infS004Data.autopayIndicator = getValue("AUTOPAY_INDICATOR");
		infS004Data.autopayAcctNoCurr = getValue("AUTOPAY_ACCT_NO_CURR");
		infS004Data.autopayAcctBank = getValue("AUTOPAY_ACCT_BANK");
		infS004Data.acnoPSeqno = getValue("ACNO_P_SEQNO");
		infS004Data.chgIdFlag = getValue("CHG_ID_FLAG");
		infS004Data.vdFlag = getValue("VD_FLAG");
		infS004Data.stmtCycle = getValue("STMT_CYCLE");
		infS004Data.oldCardNo= getValue("OLD_CARD_NO");
		infS004Data.supFlag = getValue("SUP_FLAG");
		infS004Data.majorIdNo = getValue("MAJOR_ID_NO");
		infS004Data.rcUseIndicator = getValue("RC_USE_INDICATOR");
		infS004Data.rcrateYear = getValueDouble("RCRATE_YEAR");
		return infS004Data;
	}

	private void selectInfS004Data() throws Exception {
		StringBuffer sb = new StringBuffer();
		int j = 1;
		//信用卡(台幣)
		sb.append(" SELECT C.STMT_CYCLE,B.ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		.append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT,")
		.append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT, ")
		.append(" A.COMBO_ACCT_NO AS TMP_ACCT_NO,A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,A.CRT_DATE AS MAINTAIN_DATE, ")
		.append(" B.CHI_NAME,A.ELECTRONIC_CODE,E.SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3, ")
		.append(" C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		.append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		.append(" F.ID_NO AS MAJOR_ID_NO ")
		.append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		for (int i = 1; i <= 24; i++) {
			sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		}
		sb.append(" FROM CRD_CARD A ")
		.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE = '901' ")
		.append(" LEFT JOIN CCA_CARD_BASE E ON A.CARD_NO = E.CARD_NO ")
		.append(" LEFT JOIN CRD_IDNO F ON A.MAJOR_ID_P_SEQNO = F.ID_P_SEQNO ")
		.append(" WHERE A.CURR_CODE = '901' ");
		if(!wfValue.equals(businessDate)) {	
			sb.append(" AND ( to_char(A.MOD_TIME,'yyyymmdd') = ? ")
			.append(" OR to_char(B.MOD_TIME,'yyyymmdd') = ? OR to_char(C.MOD_TIME,'yyyymmdd') = ? ")
			.append(" OR to_char(D.MOD_TIME,'yyyymmdd') = ? OR E.SPEC_DATE = ? ) ");
			
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
	    }
		if(DEBUG)
		   {
			 sb.append(" and B.ID_NO in ('P228282170','AA40202118','A133300729','A137602540',"
			 		+ "'A150782822','A138982735','A158152266','A152232092','A132022865','AC68900764') ");
			 
//			  sb.append(" and B.ID_NO in ('P228282170','F860309182','A162900804','A111890864','A179102643','A150782822',"
//			  		+ "'A141292031','A102302271','A199080395','A183060946','A160002183') ");			    
			//sb.append(" fetch first 100 rows only ");
		   }		
		sb.append(" UNION ")
		//信用卡(日幣)
		.append(" SELECT C.STMT_CYCLE,B.ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		.append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT,")
		.append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT, ")
		.append(" A.COMBO_ACCT_NO AS TMP_ACCT_NO,A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,A.CRT_DATE AS MAINTAIN_DATE, ")
		.append(" B.CHI_NAME,A.ELECTRONIC_CODE,E.SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3, ")
		.append(" C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		.append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		.append(" F.ID_NO AS MAJOR_ID_NO ")
		.append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		for (int i = 1; i <= 24; i++) {
			sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		}
		sb.append(" FROM CRD_CARD A ")
		.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE in ('901','392') ")
		.append(" LEFT JOIN CCA_CARD_BASE E ON A.CARD_NO = E.CARD_NO ")
		.append(" LEFT JOIN CRD_IDNO F ON A.MAJOR_ID_P_SEQNO = F.ID_P_SEQNO ")
		.append(" WHERE A.CURR_CODE = '392' ");
		if(!wfValue.equals(businessDate)) {	
			sb.append(" AND ( to_char(A.MOD_TIME,'yyyymmdd') = ? ")
			.append(" OR to_char(B.MOD_TIME,'yyyymmdd') = ? OR to_char(C.MOD_TIME,'yyyymmdd') = ? ")
			.append(" OR to_char(D.MOD_TIME,'yyyymmdd') = ? OR E.SPEC_DATE = ? ) ");
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
		}
		sb.append(" UNION ")
		//信用卡(美金)
		.append(" SELECT C.STMT_CYCLE,B.ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		.append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT,")
		.append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT, ")
		.append(" A.COMBO_ACCT_NO AS TMP_ACCT_NO,A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,A.CRT_DATE AS MAINTAIN_DATE, ")
		.append(" B.CHI_NAME,A.ELECTRONIC_CODE,E.SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3, ")
		.append(" C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		.append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		.append(" F.ID_NO AS MAJOR_ID_NO ")
		.append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		for (int i = 1; i <= 24; i++) {
			sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		}
		sb.append(" FROM CRD_CARD A ")
		.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE in ('901','840') ")
		.append(" LEFT JOIN CCA_CARD_BASE E ON A.CARD_NO = E.CARD_NO ")
		.append(" LEFT JOIN CRD_IDNO F ON A.MAJOR_ID_P_SEQNO = F.ID_P_SEQNO ")
		.append(" WHERE A.CURR_CODE = '840' ");
		if(!wfValue.equals(businessDate)) {	
			sb.append(" AND ( to_char(A.MOD_TIME,'yyyymmdd') = ? ")
			.append(" OR to_char(B.MOD_TIME,'yyyymmdd') = ? OR to_char(C.MOD_TIME,'yyyymmdd') = ? ")
			.append(" OR to_char(D.MOD_TIME,'yyyymmdd') = ? OR E.SPEC_DATE = ? ) ");
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
		}		
		sb.append(" UNION ")
		//VD卡
		.append(" SELECT C.STMT_CYCLE,B.ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,'' AS TMP_SON_CARD_FLAG,0 AS TMP_INDIV_CRD_LMT,0 AS TMP_LINE_OF_CREDIT_AMT,A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO, ")
		.append(" A.ORI_ISSUE_DATE,'' AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT,0 AS TMP_LINE_OF_CREDIT_AMT_CASH,'901' AS TMP_CURR_CODE,'' AS TMP_CURR_CHANGE_ACCOUT,A.ACCT_NO AS TMP_ACCT_NO, ")
		.append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,'' AS TMP_CORP_NO,A.CRT_DATE AS MAINTAIN_DATE,B.CHI_NAME,A.ELECTRONIC_CODE,D.SPEC_STATUS,")
		.append(" A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		.append(" 'Y' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,'' as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,'' AUTOPAY_ACCT_BANK,'' ACNO_P_SEQNO,'' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		.append(" B.ID_NO AS MAJOR_ID_NO ")
		.append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,0.0 AS RCRATE_YEAR ");
		for (int i = 1; i <= 24; i++) {
			sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		}
		sb.append(" FROM DBC_CARD A ")
		.append(" LEFT JOIN DBC_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN DBA_ACNO C ON A.P_SEQNO = C.P_SEQNO ")
		.append(" LEFT JOIN CCA_CARD_BASE D ON A.CARD_NO = D.CARD_NO ");
		if(!wfValue.equals(businessDate)) {
			sb.append(" AND ( to_char(A.MOD_TIME,'yyyymmdd') = ? OR to_char(B.MOD_TIME,'yyyymmdd') = ? ")
			.append(" AND to_char(C.MOD_TIME,'yyyymmdd') = ? OR D.SPEC_DATE = ? ) ");
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
		}
		if(!wfValue.equals(businessDate)) {		
			sb.append(" UNION ")
			//信用卡(台幣)變更ID(舊ID)
		    .append(" SELECT C.STMT_CYCLE,E.OLD_ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		    .append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT, ")
		    .append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		    .append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,E.CHG_DATE AS MAINTAIN_DATE, ")
		    .append(" B.CHI_NAME,A.ELECTRONIC_CODE,'ID' AS SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		    .append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'Y' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		    .append(" F.ID_NO AS MAJOR_ID_NO ")
			.append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		    for (int i = 1; i <= 24; i++) {
			    sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		    }
		    sb.append(" FROM CRD_CARD A ")
			.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE = '901' ")
			.append(" LEFT JOIN CRD_CHG_ID E ON B.ID_NO = E.ID_NO AND E.CHG_DATE = ? ")
			.append(" LEFT JOIN CRD_IDNO F ON A.MAJOR_ID_P_SEQNO = F.ID_P_SEQNO ")
			.append(" WHERE A.CURR_CODE = '901' ")
			.append(" UNION ")
			//信用卡(日幣)變更ID(舊ID)
		    .append(" SELECT C.STMT_CYCLE,E.OLD_ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		    .append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT, ")
		    .append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		    .append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,E.CHG_DATE AS MAINTAIN_DATE, ")
		    .append(" B.CHI_NAME,A.ELECTRONIC_CODE,'ID' AS SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		    .append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'Y' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		    .append(" F.ID_NO AS MAJOR_ID_NO ")
		    .append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		    for (int i = 1; i <= 24; i++) {
			    sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		    }
		    sb.append(" FROM CRD_CARD A ")
			.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE IN ('901','392') ")
			.append(" LEFT JOIN CRD_CHG_ID E ON B.ID_NO = E.ID_NO AND E.CHG_DATE = ? ")
			.append(" LEFT JOIN CRD_IDNO F ON A.MAJOR_ID_P_SEQNO = F.ID_P_SEQNO ")
			.append(" WHERE A.CURR_CODE = '392' ")
			.append(" UNION ")
			//信用卡(美金)變更ID(舊ID)
			.append(" SELECT C.STMT_CYCLE,E.OLD_ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		    .append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT, ")
		    .append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		    .append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,E.CHG_DATE AS MAINTAIN_DATE, ")
		    .append(" B.CHI_NAME,A.ELECTRONIC_CODE,'ID' AS SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		    .append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'Y' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		    .append(" F.ID_NO AS MAJOR_ID_NO ")
		    .append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		    for (int i = 1; i <= 24; i++) {
			    sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		    }
		    sb.append(" FROM CRD_CARD A ")	
			.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE IN ('901','840') ")
			.append(" LEFT JOIN CRD_CHG_ID E ON B.ID_NO = E.ID_NO AND E.CHG_DATE = ? ")
			.append(" LEFT JOIN CRD_IDNO F ON A.MAJOR_ID_P_SEQNO = F.ID_P_SEQNO ")
			.append(" WHERE A.CURR_CODE = '840' ")
		    .append(" UNION ")
		    //信用卡(台幣)變更ID(新ID)
		    .append(" SELECT C.STMT_CYCLE,B.ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		    .append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT, ")
		    .append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		    .append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,E.CHG_DATE AS MAINTAIN_DATE, ")
		    .append(" B.CHI_NAME,A.ELECTRONIC_CODE,F.SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		    .append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		    .append(" G.ID_NO AS MAJOR_ID_NO ")
		    .append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		    for (int i = 1; i <= 24; i++) {
			    sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		    }
		    sb.append(" FROM CRD_CARD A ")
			.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE = '901' ")
			.append(" LEFT JOIN CRD_CHG_ID E ON B.ID_NO = E.ID_NO ")
			.append(" LEFT JOIN CCA_CARD_BASE F ON A.CARD_NO = F.CARD_NO AND E.CHG_DATE = ? ")
			.append(" LEFT JOIN CRD_IDNO G ON A.MAJOR_ID_P_SEQNO = G.ID_P_SEQNO ")
			.append(" WHERE A.CURR_CODE = '901' ")
		    .append(" UNION ")
		    //信用卡(日幣)變更ID(新ID)
		    .append(" SELECT C.STMT_CYCLE,B.ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		    .append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT, ")
		    .append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		    .append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,E.CHG_DATE AS MAINTAIN_DATE, ")
		    .append(" B.CHI_NAME,A.ELECTRONIC_CODE,F.SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		    .append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		    .append(" G.ID_NO AS MAJOR_ID_NO ")
		    .append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		    for (int i = 1; i <= 24; i++) {
			    sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		    }
		    sb.append(" FROM CRD_CARD A ")
			.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE IN ('901','392') ")
			.append(" LEFT JOIN CRD_CHG_ID E ON B.ID_NO = E.ID_NO ")
			.append(" LEFT JOIN CCA_CARD_BASE F ON A.CARD_NO = F.CARD_NO AND E.CHG_DATE = ? ")
			.append(" LEFT JOIN CRD_IDNO G ON A.MAJOR_ID_P_SEQNO = G.ID_P_SEQNO ")
			.append(" WHERE A.CURR_CODE = '392' ")
			.append(" UNION ")
			//信用卡(美金)變更ID(新ID)
			.append(" SELECT C.STMT_CYCLE,B.ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,A.SON_CARD_FLAG AS TMP_SON_CARD_FLAG,A.INDIV_CRD_LMT AS TMP_INDIV_CRD_LMT,C.LINE_OF_CREDIT_AMT AS TMP_LINE_OF_CREDIT_AMT, ")
		    .append(" A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO,A.ORI_ISSUE_DATE,A.MAJOR_RELATION AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT, ")
		    .append(" C.LINE_OF_CREDIT_AMT_CASH AS TMP_LINE_OF_CREDIT_AMT_CASH,D.CURR_CODE AS TMP_CURR_CODE,D.CURR_CHANGE_ACCOUT AS TMP_CURR_CHANGE_ACCOUT,A.COMBO_ACCT_NO AS TMP_ACCT_NO, ")
		    .append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,A.CORP_NO AS TMP_CORP_NO,E.CHG_DATE AS MAINTAIN_DATE, ")
		    .append(" B.CHI_NAME,A.ELECTRONIC_CODE,F.SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		    .append(" 'N' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,D.AUTOPAY_ACCT_NO as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,D.AUTOPAY_ACCT_BANK,A.ACNO_P_SEQNO,'' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		    .append(" G.ID_NO AS MAJOR_ID_NO ")
		    .append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,C.RCRATE_YEAR ");
		    for (int i = 1; i <= 24; i++) {
			    sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		    }
		    sb.append(" FROM CRD_CARD A ")
			.append(" LEFT JOIN CRD_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACNO C ON A.ACNO_P_SEQNO = C.ACNO_P_SEQNO ")
			.append(" LEFT JOIN ACT_ACCT_CURR D ON A.P_SEQNO = D.P_SEQNO AND D.CURR_CODE IN ('901','840') ")
			.append(" LEFT JOIN CRD_CHG_ID E ON B.ID_NO = E.ID_NO ")
			.append(" LEFT JOIN CCA_CARD_BASE F ON A.CARD_NO = F.CARD_NO AND E.CHG_DATE = ? ")
			.append(" LEFT JOIN CRD_IDNO G ON A.MAJOR_ID_P_SEQNO = G.ID_P_SEQNO ")
			.append(" WHERE A.CURR_CODE = '840' ")
			.append(" UNION ")
			//VD卡變更ID(舊ID)
		    .append(" SELECT C.STMT_CYCLE,D.ID,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,'' AS TMP_SON_CARD_FLAG,0 AS TMP_INDIV_CRD_LMT,0 AS TMP_LINE_OF_CREDIT_AMT,A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO, ")
		    .append(" A.ORI_ISSUE_DATE,'' AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT,0 AS TMP_LINE_OF_CREDIT_AMT_CASH,'901'AS TMP_CURR_CODE,'' AS TMP_CURR_CHANGE_ACCOUT,A.ACCT_NO AS TMP_ACCT_NO, ")
		    .append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,'' AS TMP_CORP_NO,D.CRT_DATE AS MAINTAIN_DATE,B.CHI_NAME,A.ELECTRONIC_CODE, ")
		    .append(" 'ID' AS SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		    .append(" 'Y' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,'' as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,'' AUTOPAY_ACCT_BANK,'' ACNO_P_SEQNO,'Y' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		    .append(" D.ID AS MAJOR_ID_NO ")
		    .append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,0.0 AS RCRATE_YEAR ");
		    for (int i = 1; i <= 24; i++) {
			    sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		    }
		    sb.append(" FROM DBC_CARD A ")
			.append(" LEFT JOIN DBC_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
			.append(" LEFT JOIN DBA_ACNO C ON A.P_SEQNO = C.P_SEQNO ")
			.append(" LEFT JOIN DBC_CHG_ID D ON B.ID_NO = D.AFT_ID AND D.CRT_DATE = ? ")
		    .append(" UNION ")
		    //VD卡變更ID(新ID)
		    .append(" SELECT C.STMT_CYCLE,B.ID_NO,A.CARD_NO,A.ACCT_TYPE,A.GROUP_CODE,'' AS TMP_SON_CARD_FLAG,0 AS TMP_INDIV_CRD_LMT,0 AS TMP_LINE_OF_CREDIT_AMT,A.NEW_END_DATE,A.CURRENT_CODE,A.OPPOST_DATE,A.REG_BANK_NO, ")
		    .append(" A.ORI_ISSUE_DATE,'' AS TMP_MAJOR_RELATION,A.HIGHEST_CONSUME_AMT,0 AS TMP_LINE_OF_CREDIT_AMT_CASH,'901'AS TMP_CURR_CODE,'' AS TMP_CURR_CHANGE_ACCOUT,A.ACCT_NO AS TMP_ACCT_NO, ")
		    .append(" A.ISSUE_DATE,A.REISSUE_DATE,A.CHANGE_DATE,A.NEW_CARD_NO,A.REISSUE_REASON,B.BIRTHDAY,'' AS TMP_CORP_NO,D.CRT_DATE AS MAINTAIN_DATE,B.CHI_NAME,A.ELECTRONIC_CODE, ")
		    .append(" E.SPEC_STATUS,A.OPPOST_REASON,C.BILL_APPLY_FLAG,C.BILL_SENDING_ZIP,C.BILL_SENDING_ADDR1,C.BILL_SENDING_ADDR2,C.BILL_SENDING_ADDR3,C.BILL_SENDING_ADDR4,C.BILL_SENDING_ADDR5, ")
		    .append(" 'Y' AS VD_FLAG,A.ID_P_SEQNO,C.ACCT_STATUS,'' as AUTOPAY_ACCT_NO_CURR,A.P_SEQNO,'' AUTOPAY_ACCT_BANK,'' ACNO_P_SEQNO,'' AS CHG_ID_FLAG,A.OLD_CARD_NO,A.SUP_FLAG, ")
		    .append(" B.ID_NO AS MAJOR_ID_NO ")
		    .append(" ,C.RC_USE_INDICATOR ,C.AUTOPAY_INDICATOR ,C.AUTOPAY_ACCT_NO ,0.0 AS RCRATE_YEAR ");
		    for (int i = 1; i <= 24; i++) {
			    sb.append(String.format(" ,C.PAYMENT_RATE%s ", i));
		    }
		    sb.append(" FROM DBC_CARD A ")
			.append(" LEFT JOIN DBC_IDNO B ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
			.append(" LEFT JOIN DBA_ACNO C ON A.P_SEQNO = C.P_SEQNO ")
			.append(" LEFT JOIN DBC_CHG_ID D ON B.ID_NO = D.AFT_ID AND D.CRT_DATE = ? ")
			.append(" LEFT JOIN CCA_CARD_BASE E ON A.CARD_NO = E.CARD_NO ")
		    ;
		    if(DEBUG)
		    {
		    	//sb.append(" fetch first 100 rows only ");
		    }
		    
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
			setString(j++,businessDate);
		}		
		
		sqlCmd = sb.toString();	
		
		if(wfValue.equals(businessDate)) {
			showLogMessage("I", "", "參數日期比對相同，讀全檔資料");
		}
		
		//debug message
		if(DEBUG)
		{
		 showLogMessage("I", "", "【Debug模式】DEBUG = true......");
		}

		openCursor();
	}
	
	  void selectPtrSysParm() throws Exception {
		  extendField = "PARM.";
		  sqlCmd = "SELECT WF_VALUE FROM PTR_SYS_PARM WHERE WF_KEY = 'INFS004'";
		  selectTable();
		  wfValue = getValue("PARM.WF_VALUE");
	  }
	
	void procFTP(String fileFolder, String datFileName, String hdrFileName) throws Exception {
		CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
		CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

		commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NEWCENTER"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = fileFolder;
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// 先傳送CR_STATUS_YYMMDD.DAT，再傳送CR_STATUS_YYMMDD.HDR
		String ftpCommand = String.format("mput %s | mput %s", datFileName, hdrFileName);

		showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
		int errCode = commFTP.ftplogName("NEWCENTER", ftpCommand);

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
		InfS004 proc = new InfS004();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
	
    private String strFormat(int i ,Object obj) {
        
    	String str = String.valueOf(obj);
    	if(i == 0) 
    		return str;
    	
    	String strFormat = "";
    	
    	if(commStr.isNumber(str) == false) 
    		strFormat = commStr.lpad(str,i,"0");
    	else    		
    		strFormat = str.contains(".")?String.format("%0"+i+".2f", commStr.ss2Num(str))
    			:String.format("%0"+i+"d", str2Long(str));
    	return strFormat;
    }
    
   private String strFormat2(int i ,Object obj) {
        
    	String str = String.valueOf(obj);
    	if(i == 0) 
    		return str;
    	
    	String strFormat = "";

    		strFormat = commStr.lpad(str,i,"0");
 
    	return strFormat;
    }
    
    private long str2Long(String param) {
        try {
          param = param.trim().replaceAll(",", "");
          if (commStr.empty(param) || !commStr.isNumber(param)) {
            return 0;
          }
          return Long.parseLong(param.trim());
        } catch (Exception ex) {
          return 0;
        }
     }
}

class InfS004Data{
	String vdFlag = "";
	String pSeqno = "";
	String idNo = "";
	String idPSeqno = "";
	String cardNo = "";
	String ppCardNo = "";
	String validTo = "";
	String dpCardNo = "";
	String dpValidTo = "";
	String electronicCode = "";
	String elecCarNo = "";
	String autoloadFlag = "";
	String specDesc = "";
	String oppRemark = "";
	String tmpCurrCode = "";
	String tmpCurrChangeAccout = "";
	String birthday = "";
	String maintainDate = "";
	String chiName = "";
	String billApplyFlag = "";
	String billSendingZip = "";
	String billSendingAddr1 = "";
	String billSendingAddr2 = "";
	String billSendingAddr3 = "";
	String billSendingAddr4 = "";
	String billSendingAddr5 = "";	
	String acctStatus = "";
	String autopayAcctNo = "";
	String autopayAcctNoCurr = "";
	String autopayIndicator = "";
	String groupCode = "";
	String acctType = "";
	String tmpSonCardFlag = "";
	long tmpIndivCrdLmt = 0;
	long tmpLineOfCreditAmt = 0;
	String newEndDate = "";
	String currentCode = "";
	String oppostReason = "";
	String oppostDate = "";
	String specStatus = "";
	String regBankNo = "";
	String oriIssueDate = "";
	String tmpMajorRelation = "";
	double highestConsumeAmt= 0;
	long tmpLineOfCreditAmtCash = 0;
	String tmpAcctNo = "";
	String issueDate = "";
	String reissueDate= "";
	String changeDate= "";
	String newCardNo= "";
	String reissueReason = "";
	String tmpCorpNo = "";
	String autopayAcctBank = "";
	String bankType = "";
	String controlCode = "";
	String controlCodeDate = "";
	String lateDate = "";
	String acnoPSeqno = "";
	String chgIdFlag = "";
	String stmtCycle = "";
	String oldCardNo = "";
	String supFlag = "";
	String majorIdNo = "";
	String rcUseIndicator = "";

	String monthProfile24 = ""; 
	Double rcrateYear = Double.valueOf(0.00);
	double sumEndBal = 0;
	double sumEndBal2 = 0;
	double ttlAmtBal = 0;
	String thisAcctMonth = "";
	String thisLastpayDate = "";
	double sumBilContract = 0;
	double sumDcDestAmt1 = 0;
	double sumDcDestAmt2 = 0;
	double thisYearInterestAmt = 0;
	double lastYearInterestAmt = 0;
	double thisMonthInterestAmt = 0;
}




