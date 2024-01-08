/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/10  V1.00.00   Ryan     program initial                            *		
*  112/04/11  V1.00.01   Ryan     調整附卡人資訊不帶acno資料                                            *			
*  112/04/20  V1.00.02   Ryan     【指定參數日期 or執行日期 (如searchDate)】-1。          *		
*  112/05/05  V1.00.03   Ryan     修改帶入的參數邏輯                                                         *	
*  112/06/14  V1.00.04   Sunny    調整檔名為CCTCUS1X                           *	
*  112/06/17  V1.00.05   Sunny    調整檔名日期為YYYYMMDD                        *
*  112/07/07  V1.00.06   Sunny    修正一般卡正卡人與附卡人重複問題，修正商務卡重覆問題(個人與公司)調整id_no欄位從10為11長* 
*  112/07/07  V1.00.06   Sunny    修改身份證字號長度為X(11)，調整同InfR002的BUG，正卡人檢核有正卡   *
*  112/07/07  V1.00.06   Sunny    接受DM(ACCEPT_DM)改為拒絕行銷(MARKET_AGREE_BASE)-0,1,2 *	
*  112/07/19  V1.00.07   Sunny    調整產生檔案OUTPUT_BUFF_SIZE的大小*		
*  112/08/22  V1.00.08   Ryan     各處理的主要SQL改為讀取前一日-本日區間							
*  112/08/23  V1.00.09   Ryan     增加acct_type
*  112/09/11  V1.00.10   Sunny    來源讀取增加拒絕行銷(MARKET_AGREE_BASE)-0,1,2 ,生日6碼前面補0*	
*  112/09/15  V1.00.11   Ryan     公司SQL調整LINE_OF_CREDIT_AMT AS MAX_LINE_OF_CREDIT_AMT*
*  112/09/16  V1.00.12   Sunny    調整配偶生日(補0)、自扣帳號(補0)、分期註記、住家及公司電話、關掉debug*
*  112/09/21  V1.00.13   Sunny    刪除不必要的程式，如舊的可用餘額處理(效能太差)          *          															  
*****************************************************************************/
package Inf;

import java.math.BigDecimal;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;

import Cca.CalBalance;
import Cca.CalBalanceBatch;

public class InfS002 extends AccessDAO {
	public static final boolean DEBUG_MODE = false;
	private static final int OUTPUT_BUFF_SIZE = 10000;
	private final String progname = "產生送客服批次檔案-客戶資料異動檔 112/09/21  V1.00.13";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String DATA_FORM = "CCTCUS1X";
	private final static String COL_SEPERATOR = "\006";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	
	CommCrd commCrd = new CommCrd();
	CommDate commDate = new CommDate();
	CommCol commCol = null;
	CommString commStr = new CommString();
	CommTxInf commTxInf = null;
	CalBalance calBalance = null;
	CalBalanceBatch calBalanceBatch = null;
	String businessDate = "";
	
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
			calBalanceBatch = new CalBalanceBatch(getDBconnect(), getDBalias());
			// =====================================
			
			// get searchDate
			String searchDate ="",paramet2 = "";
			//若參數值為all (均小寫)，則代表執行產生全檔，若無值，則表產生異動檔。
			String parm1 = (args.length>=1 && args[0].length()==8) ? args[0].trim() 
					: (args.length>=1 && "all".equals(args[0])?args[0].trim() :"");
			showLogMessage("I", "", String.format("程式參數1 = [%s]", parm1));
	
			String parm2 = (args.length==2 && args[1].length()==8) ? args[1].trim() 
					: (args.length==2 && "all".equals(args[1])?args[1].trim() :"");
			showLogMessage("I", "", String.format("程式參數2 = [%s]", parm2));
			
			if(parm1.equals("all")) {
				searchDate = parm2;
				paramet2 = parm1;
			}else {
				searchDate = parm1;
				paramet2 = parm2;
			}
			searchDate = getProgDate(searchDate, "D");
			String searchDate2 = searchDate;
			//日期減一天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);

			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
			businessDate = searchDate;
			
			// convert YYYYMMDD into YYMMDD
			// String fileNameSearchDate = searchDate.substring(2);

			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, searchDate, CommTxInf.DAT_EXTENSION);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			if(checkOpenFile(fileFolder, datFileName) != 1) {
				comc.errExit("產生HDR檔錯誤!", "");
			}
			
			//debug message
			if(DEBUG_MODE)
			{
			 showLogMessage("I", "", "【Debug模式】DEBUG_MODE = true......");
			}
			
			showLogMessage("I", "", "開始產生.DAT檔......");
			// 產生主要檔案 .DAT 
			int dataCount = 0;
			try {	
				//處理持有一般信用卡之卡人資料(106+ID)正卡
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理一般卡--個人正卡......");
				selectInfS002Data106Id(paramet2,searchDate,searchDate2);				
				dataCount = generateDatFile(fileFolder, datFileName ,searchDate,"106");
				//處理持有一般信用卡之卡人資料(106+ID)純附卡人
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理一般卡--個人附卡......");
				selectInfS002DataSup1(paramet2,searchDate,searchDate2);
				dataCount += generateDatFile(fileFolder, datFileName ,searchDate,"sup1");
				//處理VD卡人資料
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理VD卡人資料......");
				selectInfS002Data206Vd(paramet2,searchDate,searchDate2);
				dataCount += generateDatFile(fileFolder, datFileName ,searchDate,"206");
				//處理商務卡--個人 
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理商務卡--個人 ......");
				selectInfS002Data306Id(paramet2,searchDate,searchDate2);
				dataCount += generateDatFile(fileFolder, datFileName ,searchDate,"306");
				//處理商務卡—公司資料
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理商務卡—公司資料......");
				selectInfS002DataCorp(paramet2,searchDate,searchDate2);
				dataCount += generateDatFile(fileFolder, datFileName ,searchDate,"corp");
				
				if(dataCount > 0)
					showLogMessage("I", "", String.format("產生.DAT檔完成！，共產生%d筆資料", dataCount));

			}finally {
				closeBinaryOutput();
			}

			dateTime(); // update the system date and time
			boolean isGenerated = commTxInf.generateTxtCrmHdr(fileFolder, datFileName, searchDate, sysDate, sysTime.substring(0,4), dataCount);
			if (isGenerated == false) {
				return 0;
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

	
	int checkOpenFile(String fileFolder, String datFileName) throws Exception {
		
		String datFilePath = Paths.get(fileFolder, datFileName).toString();
		boolean isOpen = openBinaryOutput(datFilePath);
		if (isOpen == false) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
			return -1;
		}
		return 1;
	}
	
	/** 
	 * @param fileFolder 檔案的資料夾路徑
	 * @param datFileName .dat檔的檔名
	 * @return the number of rows written. If the returned value is -1, it means the path or the file does not exist. 
	 * @throws Exception
	 */
	private int generateDatFile(String fileFolder, String datFileName ,String searchDate2 ,String type) throws Exception {
		int rowCount = 0;
		int countInEachBuffer = 0; // use this for writing the bytes on the file if it meets a specified value

		StringBuffer sb = new StringBuffer();
		
		while (fetchTable()) {
			InfS002Data infS002Data = getInfData();
			if("106".equals(type)) {
				selectActAcctCurr106(infS002Data,type); //僅一般卡(不含雙幣)
			}			
//			selectActAcctCurr(infS002Data); //一般卡(不含雙幣)及商務卡
			String rowOfDAT = getRowOfDAT(infS002Data,type);
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
		closeCursor();

		// write the rest of bytes on the file
		if (countInEachBuffer > 0) {
			showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
			byte[] tmpBytes = sb.toString().getBytes();
			writeBinFile(tmpBytes, tmpBytes.length);
		}

		if (rowCount == 0) {
			showLogMessage("I", "", "無資料可寫入.DAT檔");
		} else {
			showLogMessage("I", "", String.format("已產生%d筆資料", rowCount));
		}

		return rowCount;
	}
	

/****
 	1_博士 --> 3
	2_碩士 --> 3
	3_大學 --> 2
	4_專科 --> 2
	5_高中高職 --> 1
	6_其他 --> 1"
 * ****/
//	void changeEducation(InfS002Data infS002Data) {
//		String chgEducation = infS002Data.education;
//		if (commStr.pos(",1,2", infS002Data.education) > 0) {
//			chgEducation = "3";
//			return;
//		}
//		if (commStr.pos(",3,4", infS002Data.education) > 0) {
//			chgEducation = "2";
//			return;
//		}
//		if (commStr.pos(",5,6", infS002Data.education) > 0) {
//			chgEducation = "1";
//		}
//		infS002Data.education = chgEducation;
//	}
	
	double numDiv(double num1 , double num2 , int scale) {
		if(num1==0 || num2 ==0)
			return 0;
		return new BigDecimal(num1).divide(BigDecimal.valueOf(num2), scale, BigDecimal.ROUND_DOWN).doubleValue();
	}
	
	
	/*********
	 * 
	 * @param code1 (4)
	 * @param code2 (10)
	 * @param code3 (6)
	 * @return
	 */
	String formatPhone(String code1 ,String code2 , String code3) {
		StringBuffer bf = new StringBuffer();
		code1 = commStr.left(code1, 4);
		code2 = commStr.left(code2, 10);
		code3 = commStr.left(code3, 6);
		//bf.append("(").append(code1.replace("-", "")).append(")-").append(code2).append("#").append(code3);
		bf.append("").append(code1).append(code2).append(code3); //20230916 電話的三個欄位格式調整為直接串起來
		//20230916 如果為空值，預設帶000-0
		if (bf.toString().equals("")) bf.append("000-0");
		return bf.toString();
	}
	
	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfS002Data infS002Data,String type) throws Exception {
		StringBuffer sb = new StringBuffer();

		//教育程度
//		changeEducation(infS002Data);
		
		//可用額度
		Long[] balanceAmts = new Long[2];
		balanceAmts[0] = Long.valueOf(0);
		balanceAmts[1] = Long.valueOf(0);

		//使用批次已計算好的額度進行處理
		if("106".equals(type)) {
			balanceAmts = calBalanceBatch.batchIdNoBalance(infS002Data.idCorpNo);
		}
		
		if("306".equals(type)) {
			balanceAmts = calBalanceBatch.batchTotalCorpBalanceById(infS002Data.idCorpNo);
		}
		if("corp".equals(type)) {
			balanceAmts = calBalanceBatch.batchTotalCorpBalanceByCorp(infS002Data.idCorpNo);
		}
		
		//將可用額度轉成整數
		long col35Convert = balanceAmts[0].longValue();
		long col33Convert = balanceAmts[1].longValue();
		
		//debug時再打開
		//showLogMessage("I", "", String.format("查詢可用額度"+type+"=>IdCorpNo[%s]",infS002Data.idCorpNo+","+col35Convert));
		//showLogMessage("I", "", String.format("預借現金餘額"+type+"=>IdCorpNo[%s]",infS002Data.idCorpNo+","+col33Convert));		
		
		sb.append(commCrd.fixLeft(infS002Data.idCorpNo, 11)); //1	身份證號	X(11) //20230707 修正為x(11)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.chiName, 30));//2	   中文姓名	  X(30)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS002Data.nation, 1));//3	國籍別	X(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.engName, 30));//4	英文姓名	X(30)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.sex, 1));//5	性別	9(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS002Data.marriage, 1));//6	婚姻狀況	9(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%07d",commStr.ss2int(commDate.toTwDate(infS002Data.birthday))), 7));//7	客戶生日	9(07)--> 不足7碼前補0
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.classCode, 2));//8	客戶類別	X(02)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.education, 1));//9	教育程度	X(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		
//		if(DEBUG_MODE){
//			//String hGannualIncome=commCrd.fixRight(commStr.numFormat((int)numDiv(infS002Data.annualIncome,10000,0), "#0"), 4);
//			String hGannualIncome=commCrd.fixRight(String.format("%04d",(int)numDiv(infS002Data.annualIncome,10000,0), "#0"), 4);
//			showLogMessage("I", "", String.format("ID[%s]......", infS002Data.idCorpNo));
//			showLogMessage("I", "", String.format("年收入[%s]......", hGannualIncome));
//		}
		//sb.append(commCrd.fixRight(commStr.numFormat((int)numDiv(infS002Data.annualIncome,10000,0), "#0"), 4));//10	年收入	X(04)
		sb.append(commCrd.fixRight(String.format("%04d",(int)numDiv(infS002Data.annualIncome,10000,0), "#0"), 4));//10	年收入	X(04) -->不足4碼前補0
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.spouseName, 30));  //11	配偶姓名	X(30)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.spouseIdNo, 24));  //12	配偶編號	X(24)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%07d",commStr.ss2int(commDate.toTwDate(infS002Data.spouseBirthday))), 7));  //13	配偶生日	9(07)--> 20230915  不足7碼補0
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		//20230916 住家電話格式調整直接串接
		sb.append(commCrd.fixLeft(formatPhone(infS002Data.homeAreaCode1,infS002Data.homeTelNo1,infS002Data.homeTelExt1), 24));  //14	住家電話	X(24)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.cellarPhone, 15));  //15	手機一	X(15)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 15));  //16	手機二	X(15)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		//20230916 公司電話格式調整直接串接
		sb.append(commCrd.fixLeft(formatPhone(infS002Data.officeAreaCode1,infS002Data.officeTelNo1,infS002Data.officeTelExt1), 24));  //17	公司電話	X(24)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.eMailAddr, 55));   //18	E_MAIL	X(55) 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.residentZip, 6));  //19	戶籍地郵遞區號	X(06)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infS002Data.residentAddr, 25), 50));  //20	戶籍地一	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infS002Data.residentAddr, 25,25), 50));  //21	戶籍地二	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.mailZip, 6));  //22	居住地郵遞區號	X(06)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infS002Data.mailAddr, 25), 50));  //23	居住地一	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infS002Data.residentAddr, 25,25), 50));  //24	居住地二	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.companyZip, 6));  //25	公司郵遞區號	X(06)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infS002Data.companyAddr, 25), 50));  //26	公司地址一	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infS002Data.companyAddr, 25,25), 50));  //27	公司地址二	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("000%s", infS002Data.billApplyFlag), 4));  //28	郵寄地址	X(04)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.businessCode, 4));  //29	行業別	X(04)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.companyName, 30));  //30	服務單位	X(30)	
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.jobPosition, 15));  //31	客戶職稱	X(15)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%s", infS002Data.maxLineOfCreditAmtCash), 10));  //32	預現額度	9(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%s", col33Convert), 10));  //33	預現餘額	X(10) 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%s", infS002Data.lineOfCreditAmt), 10));  //34	信用卡額度	9(10)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%s", col35Convert), 10));  //35	可用額度	X(10) CalBalance.idNoBalance(id_no) 商務卡個人totalCorpBalanceById(String idNo) 商務卡公司totalCorpBalanceByCorp
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
	    //36扣繳帳號
		if(!commStr.empty(infS002Data.autopayAcctNo) && !String.format("%015d", 0).equals(infS002Data.autopayAcctNo)){ // 1代表有自動轉帳 0無自動轉帳
		  sb.append(commCrd.fixLeft(String.format("%015d", Long.parseLong(infS002Data.autopayAcctNo)), 15));  //36	扣繳帳號	X(15)
		  infS002Data.acctStatus38 = "1"; 
		}
		else
		{
			 sb.append(commCrd.fixRight(String.format("%015d", 0), 15));  //36	扣繳帳號	X(15)
			 infS002Data.acctStatus38 = "0"; 
		}
		if(DEBUG_MODE){
			//String hGannualIncome=commCrd.fixRight(commStr.numFormat((int)numDiv(infS002Data.annualIncome,10000,0), "#0"), 4);
			String hautopayAcctNo=commCrd.fixLeft(infS002Data.autopayAcctNo, 15);
			String hautopayAcctNo2=commCrd.fixRight(String.format("%015d",commStr.ss2int(infS002Data.autopayAcctNo.trim())), 15);
			showLogMessage("I", "","========================================================");
			showLogMessage("I", "", String.format("ID[%s]......", infS002Data.idCorpNo));
			showLogMessage("I", "", String.format("autopayAcctNo_after[%s]......", hautopayAcctNo));
			//showLogMessage("I", "", String.format("autopayAcctNo2_after[%s]......", hautopayAcctNo2));
		}
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("2".equals(infS002Data.autopayDcIndicator)?"1":"1".equals(infS002Data.autopayDcIndicator)?"2":"0", 1));  //37	扣繳額度	X(01) 0.不扣繳 1最低(系統為2) 2全額(系統為1)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		//sb.append(commCrd.fixLeft(infS002Data.acctStatus38, 1));  //38	繳費方式	X(01)
//		sb.append(commCrd.fixLeft(commStr.pos(",1,2", infS002Data.acctStatus38)>0?"0":"1", 1));  //38	繳費方式 0：可循環 1：ㄧ次繳清
		sb.append(commCrd.fixLeft(infS002Data.acctStatus38, 1));  //38	自動轉帳 : 1代表有自動轉帳 0無自動轉帳
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS002Data.stmtCycle, 2));  //39	帳單結帳日	9(02)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS002Data.market_agree_base, 1));  //40	接受DM	9(01)-->20230707 改為拒絕行銷 0不同意 1同意共銷 2同意共享
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commDate.toTwDate(infS002Data.modDate), 7));  //41	維護日期	9(07)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS002Data.org, 3)); //42	銀行別	9(03)  
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.graduationElementarty, 20));  //43	畢業國小	X(20)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.instFlag.equals("Y")?"1":" ", 1));  //44	同意分期註記	X(1)-->20230915 Y同意 N不同意，將Y轉1，N或其他值帶空白
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infS002Data.autopayAcctBank, 3));  //45	ACH扣繳帳號之銀行代碼	X(3)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.billSendingZip, 6));  //46	其他地郵遞區號	X(06)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infS002Data.billSendingAddr, 25), 50));  //47	其他地址一	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infS002Data.billSendingAddr, 25,25), 50));  //48	其他地址二	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infS002Data.acctType, 2));  //51	帳戶類別	X(02)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	InfS002Data getInfData() throws Exception {
		InfS002Data infS002Data = new InfS002Data();
		infS002Data.org = getValue("org"); //sunny add
		infS002Data.pSeqno = getValue("P_SEQNO");
		infS002Data.idCorpNo = getValue("ID_CORP_NO");
		infS002Data.lineOfCreditAmt = getValueLong("MAX_LINE_OF_CREDIT_AMT");
		infS002Data.maxLineOfCreditAmtCash = getValueLong("MAX_LINE_OF_CREDIT_AMT_CASH");
		infS002Data.chiName = getValue("CHI_NAME");
		infS002Data.nation = getValue("NATION");
		infS002Data.sex = getValue("SEX");
		infS002Data.marriage = getValue("MARRIAGE");
		infS002Data.birthday = getValue("BIRTHDAY");
		infS002Data.classCode = getValue("CLASS_CODE");
		infS002Data.education = getValue("EDUCATION");
		infS002Data.annualIncome = getValueDouble("ANNUAL_INCOME");
		infS002Data.spouseName = getValue("SPOUSE_NAME");
		infS002Data.spouseIdNo = getValue("SPOUSE_ID_NO");
		infS002Data.spouseBirthday = getValue("SPOUSE_BIRTHDAY");
		infS002Data.homeAreaCode1 = getValue("HOME_AREA_CODE1");
		infS002Data.homeTelNo1 = getValue("HOME_TEL_NO1");
		infS002Data.homeTelExt1 = getValue("HOME_TEL_EXT1");
		infS002Data.cellarPhone = getValue("CELLAR_PHONE");
		infS002Data.officeAreaCode1 = getValue("OFFICE_AREA_CODE1");
		infS002Data.officeTelNo1 = getValue("OFFICE_TEL_NO1");
		infS002Data.officeTelExt1 = getValue("OFFICE_TEL_EXT1");
		infS002Data.eMailAddr = getValue("E_MAIL_ADDR");
		infS002Data.residentZip = getValue("RESIDENT_ZIP");
		infS002Data.residentAddr = getValue("RESIDENT_ADDR");
		infS002Data.mailZip = getValue("MAIL_ZIP");
		infS002Data.mailAddr = getValue("MAIL_ADDR");
		infS002Data.companyZip = getValue("COMPANY_ZIP");
		infS002Data.companyAddr = getValue("COMPANY_ADDR");
		infS002Data.billApplyFlag = getValue("BILL_APPLY_FLAG");
		infS002Data.businessCode = getValue("BUSINESS_CODE");
		infS002Data.companyName = getValue("COMPANY_NAME");
		infS002Data.jobPosition = getValue("JOB_POSITION");
		infS002Data.acctStatus38 = getValue("ACCT_STATUS_38");
		infS002Data.acctStatus47 = getValue("ACCT_STATUS_47");
		infS002Data.stmtCycle = getValue("STMT_CYCLE");
		infS002Data.market_agree_base = getValue("market_agree_base");    //20230707 改為拒絕行銷 0不同意 1同意共銷 2同意共享
		infS002Data.modDate = getValue("MOD_DATE");
		infS002Data.billSendingZip = getValue("BILL_SENDING_ZIP");
		infS002Data.billSendingAddr = getValue("BILL_SENDING_ADDR");
		infS002Data.engName = getValue("ENG_NAME");
		infS002Data.autopayAcctNo = getValue("AUTOPAY_ACCT_NO");
		infS002Data.autopayDcIndicator = getValue("AUTOPAY_DC_INDICATOR");
		infS002Data.dcEndBalOp901 = getValueDouble("DC_OP_BAL_901");
		infS002Data.dcEndBalOp840 = getValueDouble("DC_OP_BAL_840");
		infS002Data.dcEndBalOp392 = getValueDouble("DC_OP_BAL_392");
		infS002Data.corpPSeqno = getValue("CORP_P_SEQNO");
		//新增
		infS002Data.graduationElementarty = getValue("GRADUATION_ELEMENTARTY");
		infS002Data.instFlag = getValue("INST_FLAG");
		infS002Data.autopayAcctBank = getValue("AUTOPAY_ACCT_BANK");
		infS002Data.acctType = getValue("ACCT_TYPE");
		return infS002Data;
	}
	
	/***
	 * 查詢一般卡(個人正卡)是否有正卡
	 * **/
	//AND EXISTS (SELECT 1 FROM crd_card c WHERE c.P_SEQNO=a.p_seqno AND acct_type='01' AND SUP_FLAG='0')	
	void selectCrdCardSup0Cnt(InfS002Data infS002Data,String type) throws Exception {
		if(!"106".equals(type)&&!"sup1".equals(type)) {
			return;
		}
		extendField = "CRD_CARD.";
		sqlCmd += " select sum(decode(sup_flag,'0',1,0)) as CARD_SUP0_CNT ,sum(decode(sup_flag,'1',1,0)) as CARD_SUP1_CNT ";
		sqlCmd += " FROM CRD_CARD ";
		sqlCmd += " WHERE ACCT_TYPE= '01' AND P_SEQNO = ? ";
		setString(1,infS002Data.pSeqno);
		selectTable();
		
		infS002Data.card_sup0_cnt = getValueDouble("CRD_CARD.CARD_SUP0_CNT"); //持有正卡數
		infS002Data.card_sup1_cnt = getValueDouble("CRD_CARD.CARD_SUP1_CNT"); //持有附卡數
	}
	
	/***
	 * 處理一般卡--個人正卡(106雙幣扣繳及溢繳金額資訊)
	 * **/
	void selectActAcctCurr106(InfS002Data infS002Data,String type) throws Exception {
		extendField = "ACT_ACAG_CURR.";
//		if("106".equals(type)) {
//			sqlCmd = " SELECT A.P_SEQNO,trim(RIGHT(A.AUTOPAY_ACCT_NO,15)) AS AUTOPAY_ACCT_NO,A.AUTOPAY_DC_INDICATOR,LEFT(A.AUTOPAY_ACCT_BANK,3) AS AUTOPAY_ACCT_BANK ";
//		}else if("306".equals(type)) {
//			sqlCmd = " SELECT '' AS AUTOPAY_ACCT_NO ,'' AS AUTOPAY_DC_INDICATOR ,'' AS AUTOPAY_ACCT_BANK ";
//		}else {
//			return;
//		}
		sqlCmd = " SELECT A.P_SEQNO,trim(RIGHT(A.AUTOPAY_ACCT_NO,15)) AS AUTOPAY_ACCT_NO,A.AUTOPAY_DC_INDICATOR,LEFT(A.AUTOPAY_ACCT_BANK,3) AS AUTOPAY_ACCT_BANK ";
		sqlCmd += ",'' as DC_OP_BAL_901 , '' as DC_OP_BAL_840 , '' DC_OP_BAL_392 ";
		sqlCmd += " FROM ACT_ACCT_CURR A ";
		sqlCmd += " WHERE A.ACCT_TYPE= '01' AND A.CURR_CODE='901' AND A.P_SEQNO = ? ";
		setString(1,infS002Data.pSeqno);
		int recordCnt = selectTable();
	
		if (recordCnt > 0) {
		infS002Data.autopayAcctNo = getValue("ACT_ACAG_CURR.AUTOPAY_ACCT_NO");
		infS002Data.autopayDcIndicator = getValue("ACT_ACAG_CURR.AUTOPAY_DC_INDICATOR");
		infS002Data.dcEndBalOp901 = getValueDouble("ACT_ACAG_CURR.DC_OP_BAL_901");
		infS002Data.dcEndBalOp840 = getValueDouble("ACT_ACAG_CURR.DC_OP_BAL_840");
		infS002Data.dcEndBalOp392 = getValueDouble("ACT_ACAG_CURR.DC_OP_BAL_392");
		infS002Data.autopayAcctBank = getValue("ACT_ACAG_CURR.AUTOPAY_ACCT_BANK");
				
		
//		showLogMessage("I", "", String.format("autopayAcctNo_before[%s]......", infS002Data.autopayAcctNo));
		
		if(infS002Data.autopayAcctNo.length()==0){		
		infS002Data.autopayAcctNo = String.format("%015d", 0);		
		//showLogMessage("I", "", String.format("autopayAcctNo_補0[%s]......", infS002Data.autopayAcctNo));
	    }
		
		if(DEBUG_MODE){
//	    if(infS002Data.autopayAcctNo.length()==0){
			showLogMessage("I", "","讀取selectActAcctCurr106==============================");
		showLogMessage("I", "", String.format("ID[%s]......", infS002Data.idCorpNo));
		showLogMessage("I", "", String.format("pSeqno[%s]......", infS002Data.pSeqno));
		showLogMessage("I", "", String.format("type[%s]......", type));
		showLogMessage("I", "", String.format("autopayAcctNo_before[%s]......", infS002Data.autopayAcctNo));
		showLogMessage("I", "", String.format("autopayDcIndicator[%s]......", infS002Data.autopayDcIndicator));
//	    }
		}
		}
	}
	
	
	/***
	 *可能會遇到多筆的問題，不採用 
	 * 處理帳戶層一般卡及商務卡自動扣繳資訊(不含雙幣，僅109)
	 * **/
	void selectActAcctCurr(InfS002Data infS002Data) throws Exception {
		extendField = "ACT_ACAG_CURR.";
		sqlCmd = " SELECT A.P_SEQNO,RIGHT(A.AUTOPAY_ACCT_NO,15) AS AUTOPAY_ACCT_NO,A.AUTOPAY_DC_INDICATOR,LEFT(A.AUTOPAY_ACCT_BANK,3) AS AUTOPAY_ACCT_BANK ";
		sqlCmd += " FROM ACT_ACCT_CURR A ";
		sqlCmd += " WHERE A.ACCT_TYPE= ? AND A.CURR_CODE='901' AND A.P_SEQNO = ? ";
		setString(1,infS002Data.acctType);
		setString(2,infS002Data.pSeqno);
		//setString(1,);
		selectTable();

		infS002Data.autopayAcctBank = getValue("ACT_ACAG_CURR.AUTOPAY_ACCT_BANK");
		infS002Data.autopayAcctNo = getValue("ACT_ACAG_CURR.AUTOPAY_ACCT_NO");
		infS002Data.autopayAcctNo = String.format("%015d", infS002Data.autopayAcctNo); //未滿15碼前補0
		infS002Data.autopayDcIndicator = getValue("ACT_ACAG_CURR.AUTOPAY_DC_INDICATOR");
		
//		showLogMessage("I", "", String.format("[%s]......", infS002Data.autopayAcctNo));
//				
//		if(infS002Data.autopayAcctNo.equals("")){
//			showLogMessage("I", "", String.format("autopayAcctNo_before[%s]......", infS002Data.autopayAcctNo));
//			infS002Data.autopayAcctNo = String.format("%015d", infS002Data.autopayAcctNo);
//		}
		
		if(DEBUG_MODE){
//	    if(infS002Data.autopayAcctNo.length()==0){
		showLogMessage("I", "","讀取selectActAcctCurr====================================");
		showLogMessage("I", "", String.format("ID[%s]......", infS002Data.idCorpNo));
		showLogMessage("I", "", String.format("pSeqno[%s]......", infS002Data.pSeqno));
		showLogMessage("I", "", String.format("acct_type[%s]......", infS002Data.acctType));
		showLogMessage("I", "", String.format("autopayAcctNo_before[%s]......", infS002Data.autopayAcctNo));
		showLogMessage("I", "", String.format("autopayDcIndicator[%s]......", infS002Data.autopayDcIndicator));
		showLogMessage("I", "","========================================================");
//		}
	    }
	}
//	

	/*******
	 * 處理一般卡--個人正卡
	 * 以卡人檔為基礎01、取得帳戶檔有01的資料
	 * *****/
	private void selectInfS002Data106Id(String paramet2,String searchDate , String searchDateEnd) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT B.ID_NO as id_corp_no,A.P_SEQNO,DECODE(A.ACCT_TYPE,'01','106','') AS org,A.LINE_OF_CREDIT_AMT as MAX_LINE_OF_CREDIT_AMT,A.LINE_OF_CREDIT_AMT_CASH as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,decode(A.BILL_APPLY_FLAG,'','0',A.BILL_APPLY_FLAG) as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,A.ACCT_STATUS as ACCT_STATUS_38,A.ACCT_STATUS as ACCT_STATUS_47,A.STMT_CYCLE,B.ACCEPT_DM,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,A.BILL_SENDING_ZIP ")
		.append(" ,(A.BILL_SENDING_ADDR1 || A.BILL_SENDING_ADDR2 || A.BILL_SENDING_ADDR3 || A.BILL_SENDING_ADDR4 || A.BILL_SENDING_ADDR5) AS BILL_SENDING_ADDR ")
		.append(" ,(SELECT ENG_NAME FROM CRD_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,'' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR , '' as CORP_P_SEQNO ")
		.append(" ,B.GRADUATION_ELEMENTARTY,B.INST_FLAG,'' as AUTOPAY_ACCT_BANK ")//新增
		.append(" ,A.ACCT_TYPE ")//20230823新增
		.append(" FROM CRD_IDNO B ")
		.append(" LEFT JOIN ACT_ACNO A ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" WHERE A.CARD_INDICATOR='1' ")
		.append(" AND A.ACCT_TYPE = '01' ")
		.append(" AND EXISTS (SELECT 1 FROM crd_card c WHERE c.P_SEQNO=a.p_seqno AND c.acct_type='01' AND c.SUP_FLAG='0') ");//20230707 add 須要有正卡
		if(!"all".equals(paramet2)) {
			sb.append(" AND ((to_char(A.MOD_TIME,'yyyymmdd') >= ? AND to_char(A.MOD_TIME,'yyyymmdd') <= ? ) ");
			sb.append(" OR (to_char(B.MOD_TIME,'yyyymmdd') >= ? AND to_char(B.MOD_TIME,'yyyymmdd') <= ? )) ");
			setString(1,searchDate);
			setString(2,searchDateEnd);
			setString(3,searchDate);
			setString(4,searchDateEnd);
		}
		if(DEBUG_MODE){
			  //sb.append(" AND LEFT(B.ID_NO,1)='H' ");
			  //sb.append(" AND B.ID_NO='AA40202118' ");
			  sb.append(" AND A.AUTOPAY_ACCT_NO<>'' ");
			//sb.append(" AND B.INST_FLAG='Y' ");  
			sb.append(" limit 50 ");
		      //sb.append(" limit 10 ");
			}
		sqlCmd = sb.toString();
		openCursor();
	}
	
	/*******
	 * 處理一般卡--個人附卡 (無acno資料)
	 * *****/
	private void selectInfS002DataSup1(String paramet2,String searchDate,String searchDateEnd) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT B.ID_NO as id_corp_no,'' as P_SEQNO,'106' AS org,'0' as MAX_LINE_OF_CREDIT_AMT,'0' as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,'0' as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,'' as ACCT_STATUS_38,'' as ACCT_STATUS_47,'01' as STMT_CYCLE,B.ACCEPT_DM,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,'' as BILL_SENDING_ZIP ")
		.append(" ,'' AS BILL_SENDING_ADDR ")
		.append(" ,(SELECT ENG_NAME FROM CRD_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,'' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR , '' as CORP_P_SEQNO ")
		.append(" ,B.GRADUATION_ELEMENTARTY,B.INST_FLAG,'' as AUTOPAY_ACCT_BANK ")//新增
		.append(" ,'01' as ACCT_TYPE ")//20230823新增
		.append(" FROM CRD_IDNO B ")
		.append(" INNER JOIN ( select ID_P_SEQNO,sum(decode(sup_flag,'0',1,0)) as card_sup0_cnt ,sum(decode(sup_flag,'1',1,0)) as card_sup1_cnt ")
		.append(" from crd_card WHERE ACCT_TYPE = '01' group by ID_P_SEQNO ) C ON B.ID_P_SEQNO = C.ID_P_SEQNO ")
		.append(" and c.card_sup0_cnt = 0 AND c.card_sup1_cnt > 0 ");
		if(!"all".equals(paramet2)) {
			sb.append(" AND to_char(B.MOD_TIME,'yyyymmdd') >= ? AND  to_char(B.MOD_TIME,'yyyymmdd') <= ? ");
			setString(1,searchDate);
			setString(2,searchDateEnd);
		}
		if(DEBUG_MODE){
			  //sb.append(" AND LEFT(B.ID_NO,1)='H' ");
			  //sb.append(" AND B.ID_NO='AA40202118' ");
		      sb.append(" limit 10 ");
			}
		sqlCmd = sb.toString();
		openCursor();
	}
	
	/*******
	 * 處理商務卡--個人
	 * *****/
	private void selectInfS002Data306Id(String paramet2,String searchDate ,String searchDateEnd) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT * FROM ( SELECT ROW_NUMBER() OVER(PARTITION BY b.id_no ORDER BY b.id_no DESC) AS Row_num ")
		.append(" ,decode(a.ACCT_TYPE,'03','306','06','306','') as org,a.CORP_P_SEQNO,a.ACNO_FLAG ")
		//.append(" ,'306' as org,a.CORP_P_SEQNO,a.ACNO_FLAG ")
		.append(" ,c.MAX_LINE_OF_CREDIT_AMT,c.MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.ID_NO as id_corp_no,A.P_SEQNO,A.LINE_OF_CREDIT_AMT,A.LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,decode(A.BILL_APPLY_FLAG,'','0',A.BILL_APPLY_FLAG) as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,'' as ACCT_STATUS_38,A.ACCT_STATUS as ACCT_STATUS_47,'' as STMT_CYCLE,B.ACCEPT_DM,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,A.BILL_SENDING_ZIP ")
		.append(" ,(A.BILL_SENDING_ADDR1 || A.BILL_SENDING_ADDR2 || A.BILL_SENDING_ADDR3 || A.BILL_SENDING_ADDR4 || A.BILL_SENDING_ADDR5) AS BILL_SENDING_ADDR ")
		.append(" ,(SELECT ENG_NAME FROM CRD_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,'' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR ")
		.append(" ,B.GRADUATION_ELEMENTARTY,B.INST_FLAG,'' as AUTOPAY_ACCT_BANK ")//新增
		.append(" ,A.ACCT_TYPE ")//20230823新增
		.append(" FROM crd_idno b ")
		.append(" LEFT JOIN act_acno a ON a.id_p_seqno = b.id_p_seqno ")
		.append(" LEFT JOIN ( SELECT ID_P_SEQNO,max(LINE_OF_CREDIT_AMT) AS max_line_of_credit_amt,max(LINE_OF_CREDIT_AMT_CASH) AS MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" FROM act_acno WHERE CARD_INDICATOR='2' and acno_flag <> '2' GROUP BY ID_P_SEQNO ) c ON a.ID_P_SEQNO = c.ID_P_SEQNO ")
		.append(" WHERE a.CARD_INDICATOR='2' AND a.ACNO_FLAG <> '2' AND a.ACCT_TYPE IN ('03','06') ");
		if(!"all".equals(paramet2)) {
			sb.append(" AND ((to_char(A.MOD_TIME,'yyyymmdd') >= ? AND to_char(A.MOD_TIME,'yyyymmdd') <= ? ) ");
			sb.append(" OR (to_char(B.MOD_TIME,'yyyymmdd') >= ? AND to_char(B.MOD_TIME,'yyyymmdd') <= ? )) ");
			setString(1,searchDate);
			setString(2,searchDateEnd);
			setString(3,searchDate);
			setString(4,searchDateEnd);
		}
		sb.append(") WHERE Row_num=1 ");
		if(DEBUG_MODE){
			  //sb.append(" AND LEFT(B.ID_NO,1)='H' ");
			  //sb.append(" AND B.ID_NO='AA40202118' ");
              sb.append(" limit 10 ");
			}
		sqlCmd = sb.toString();
		openCursor();
	}
	
	/*******
	 * 處理VD卡--個人資料
	 * *****/
	private void selectInfS002Data206Vd(String paramet2,String searchDate ,String searchDateEnd) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT * FROM ( SELECT ROW_NUMBER() OVER(PARTITION BY b.id_no ORDER BY b.id_no,nvl(c.current_cnt,0) desc,a.MOD_TIME DESC) AS Row_num ")
		//.append(" ,B.ID_NO as id_corp_no,A.P_SEQNO,DECODE(A.ACCT_TYPE,'90','206','') AS org,0 as MAX_LINE_OF_CREDIT_AMT,0 as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.ID_NO as id_corp_no,A.P_SEQNO,'206' AS org,0 as MAX_LINE_OF_CREDIT_AMT,0 as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,'' as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,'' as ACCT_STATUS_38,A.ACCT_STATUS as ACCT_STATUS_47,A.STMT_CYCLE,B.ACCEPT_DM,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,'' BILL_SENDING_ZIP ")
		.append(" ,'' AS BILL_SENDING_ADDR, '' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR ,'' as CORP_P_SEQNO ")
		//.append(" ,(SELECT ENG_NAME FROM DBC_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,B.ENG_NAME ") //讀卡人層即可
		.append(" ,nvl(c.current_cnt,0) AS current_cnt ")
		.append(" ,B.GRADUATION_ELEMENTARTY,B.INST_FLAG,'' as AUTOPAY_ACCT_BANK ")//新增
		.append(" ,A.ACCT_TYPE ")//20230823新增
		.append(" FROM dbc_idno B ")
		.append(" LEFT JOIN dba_acno A ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN (SELECT p_seqno,count(*) AS current_cnt FROM dbc_card WHERE CURRENT_CODE='0' GROUP BY p_seqno) c ON a.P_SEQNO=c.p_seqno ")
		.append(" WHERE A.CARD_INDICATOR='1' ")
		.append(" AND A.ACCT_TYPE = '90'  ");
		//.append(" AND A.ACCT_TYPE = '90' ) WHERE Row_num=1 limit 1000");
		if(!"all".equals(paramet2)) {
			sb.append(" AND ((to_char(A.MOD_TIME,'yyyymmdd') >= ? AND to_char(A.MOD_TIME,'yyyymmdd') <= ? ) ");
			sb.append(" OR (to_char(B.MOD_TIME,'yyyymmdd') >= ? AND to_char(B.MOD_TIME,'yyyymmdd') <= ? )) ");
			setString(1,searchDate);
			setString(2,searchDateEnd);
			setString(3,searchDate);
			setString(4,searchDateEnd);
		}
		sb.append(") WHERE Row_num=1 ");
		if(DEBUG_MODE){
			  //sb.append(" AND LEFT(B.ID_NO,1)='H' ");
			  //sb.append(" AND B.ID_NO='AA40202118' ");
		      sb.append(" limit 10 ");
			}
		sqlCmd = sb.toString();
		openCursor();
	}
	
	/*******
	 * 處理商務卡—公司資料
	 * *****/
	private void selectInfS002DataCorp(String paramet2,String searchDate,String searchDateEnd) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT decode(a.ACCT_TYPE,'03','306','06','306','') as org, b.corp_no as id_corp_no,B.CHI_NAME ,'' as nation ,b.eng_name ")
		.append(" ,'' as sex, '' as MARRIAGE, '' as BIRTHDAY, '' as CLASS_CODE, '' as EDUCATION, '' as ANNUAL_INCOME, '' as SPOUSE_NAME ")
		.append(" ,'' as SPOUSE_ID_NO, '' as SPOUSE_BIRTHDAY, '' as HOME_AREA_CODE1, '' as HOME_TEL_NO1, '' as HOME_TEL_EXT1, '' as CELLAR_PHONE ")
		.append(" ,b.CORP_P_SEQNO,b.E_MAIL_ADDR ")
		//.append(" ,b.REG_ZIP as RESIDENT_ZIP ,(B.REG_ADDR1 || B.REG_ADDR2 || B.REG_ADDR3 || B.REG_ADDR4 || B.REG_ADDR5) AS RESIDENT_ADDR  ")
		//.append(" ,'' as MAIL_ZIP,'' AS MAIL_ADDR, B.COMM_ZIP as COMPANY_ZIP ")
		//.append(" ,(B.COMM_ADDR1 || B.COMM_ADDR2 || B.COMM_ADDR3 || B.COMM_ADDR4 || B.COMM_ADDR5) AS COMPANY_ADDR,'0000' as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,b.REG_ZIP as COMPANY_ZIP ,(B.REG_ADDR1 || B.REG_ADDR2 || B.REG_ADDR3 || B.REG_ADDR4 || B.REG_ADDR5) AS COMPANY_ADDR  ")
		.append(" ,'' as RESIDENT_ZIP,'' AS RESIDENT_ADDR, B.COMM_ZIP as MAIL_ZIP ")
		.append(" ,(B.COMM_ADDR1 || B.COMM_ADDR2 || B.COMM_ADDR3 || B.COMM_ADDR4 || B.COMM_ADDR5) AS MAIL_ADDR,'0000' as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,'' as COMPANY_NAME,'' as JOB_POSITION,A.LINE_OF_CREDIT_AMT_CASH as MAX_LINE_OF_CREDIT_AMT_CASH,C.sum_line_of_credit_amt as MAX_LINE_OF_CREDIT_AMT,'' as AUTOPAY_ACCT_NO ")
		.append(" ,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,'' as AUTOPAY_DC_INDICATOR, A.acct_status as ACCT_STATUS_38,A.acct_status as ACCT_STATUS_47, '' as STMT_CYCLE, '' as ACCEPT_DM, '' as BILL_SENDING_ZIP ")
		.append(" ,'' AS BILL_SENDING_ADDR, '' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 ")
		.append(" ,B.CORP_TEL_ZONE1 as OFFICE_AREA_CODE1, B.CORP_TEL_NO1 as OFFICE_TEL_NO1, B.CORP_TEL_EXT1 as OFFICE_TEL_EXT1 ")
		.append(" ,'' AS GRADUATION_ELEMENTARTY,'' AS INST_FLAG,'' as AUTOPAY_ACCT_BANK ")//新增
		.append(" ,A.ACCT_TYPE ")//20230823新增
		.append(" FROM crd_corp b ")
		.append(" INNER JOIN (SELECT * FROM act_acno WHERE CARD_INDICATOR='2' and acno_flag='2') A ON a.corp_p_seqno = b.corp_p_seqno ")
		.append(" LEFT JOIN ( SELECT corp_p_seqno,sum(LINE_OF_CREDIT_AMT) AS sum_line_of_credit_amt,sum(line_of_credit_amt_cash) AS sum_line_of_credit_amt_cash ")
		.append(" FROM act_acno WHERE CARD_INDICATOR='2' and acno_flag='2'GROUP BY corp_p_seqno) C ON a.corp_p_seqno =c.corp_p_seqno");
		if(!"all".equals(paramet2)) {
			sb.append(" AND ((to_char(A.MOD_TIME,'yyyymmdd') >= ? AND to_char(A.MOD_TIME,'yyyymmdd') <= ? ) ");
			sb.append(" OR (to_char(B.MOD_TIME,'yyyymmdd') >= ? AND to_char(B.MOD_TIME,'yyyymmdd') <= ? )) ");
			setString(1,searchDate);
			setString(2,searchDateEnd);
			setString(3,searchDate);
			setString(4,searchDateEnd);
		}
		if(DEBUG_MODE){
			  //sb.append(" AND LEFT(B.ID_NO,1)='H' ");
			  //sb.append(" AND B.ID_NO='AA40202118' ");
		      sb.append(" limit 10 ");
			}
		sqlCmd = sb.toString();
		openCursor();
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
		InfS002 proc = new InfS002();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class InfS002Data{
	String pSeqno = "";
	String idCorpNo = "";
	String chiName = "";
	String nation = "";
	String engName = "";
	String sex = "";
	String marriage = "";
	String birthday = "";
	String classCode = "";
	String education = "";
	double annualIncome = 0;
	String spouseName = "";
	String spouseIdNo = "";
	String spouseBirthday = "";
	String homeAreaCode1 = "";
	String homeTelNo1 = "";
	String homeTelExt1 = "";
	String cellarPhone = "";
	String officeAreaCode1 = "";
	String officeTelNo1 = "";
	String officeTelExt1 = "";
	String eMailAddr = "";
	String residentZip = "";
	String residentAddr = "";
	String mailZip = "";
	String mailAddr = "";
	String companyZip= "";
	String companyAddr = "";
	String billApplyFlag = "0";
	String businessCode= "";
	String companyName= "";
	String jobPosition= "";
	long maxLineOfCreditAmtCash = 0;
	//double lineOfCreditAmt = 0;
	long lineOfCreditAmt = 0;
	String idNoBalance = "";
//	String autopayAcctNo = "";
	String autopayAcctNo = String.format("%015d", 0);
	String autopayDcIndicator = "";
	String acctStatus38 = "";
	String acctStatus47 = "";
	String stmtCycle = "";
	//String acceptDm = ""; 取消
	String market_agree_base= ""; //拒絕行銷 0不同意 1同意共銷 2同意共享
	String modDate = "";
	String org = "";
	String billSendingZip = "";
	String billSendingAddr = "";
	double dcEndBalOp901 = 0;
	double dcEndBalOp840 = 0; 
	double dcEndBalOp392 = 0; 
	String corpPSeqno = "";
	//新增
	String graduationElementarty = "";
	String instFlag = "";
	String autopayAcctBank = "";
	String acctType = "";
	double card_sup0_cnt=0;
	double card_sup1_cnt=0;
}




