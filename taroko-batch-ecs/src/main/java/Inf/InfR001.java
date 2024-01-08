/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/03/14  V1.00.00   Ryan     program initial                            *
*  112/03/18  V1.00.01   Sunny    產檔路徑調整,調整部分欄位的處理                      *
*  112/03/21  V1.00.03   Sunny    可用餘額(公司)調整帶corpno, 停止ftp move動作            *
*  112/03/22  V1.00.04   Sunny    訂閱註記billApplyFlag預設為0,卡人等級調整                 *
*  112/03/24  V1.00.05   Ryan     調整可用餘額、預借現金餘額取得方式,limit&showlog關閉*
*  112/03/25  V1.00.06   Sunny    調整附卡人資訊不帶acno資料                                            *
*  112/04/04  V1.00.07   Sunny    修正selectInfR002Data306Id()處理，acno_flag<>2*
*  112/04/04  V1.00.07   Sunny    調整商務卡公司地址處理                                                     *					
*  112/04/11  V1.00.08   Ryan     附卡不讀act_acct_curr                        *		
*  112/04/20  V1.00.09   Ryan     【指定參數日期 or執行日期 (如searchDate)】-1。             *	
*  112/07/07  V1.00.10   Sunny    修正一般卡正卡人與附卡人重複問題，修正商務卡重覆問題(個人與公司)調整id_no欄位從10為11長* 
*  112/07/07  V1.00.10   Sunny    修改身份證字號長度為X(11)，調整同InfR002的BUG，正卡人檢核有正卡   *	
*  112/07/07  V1.00.10   Sunny    接受DM(ACCEPT_DM)改為拒絕行銷(MARKET_AGREE_BASE)-0,1,2 *
*  112/07/13  V1.00.11   Sunny    調整執行效能,增加帳戶類別, acct_type X(02)          
*  112/09/15  V1.00.12   Ryan     disable 調整為附卡人可用額度一律帶0              *
*  112/09/15  V1.00.12   Sunny    調整自扣帳號改右取15                          *			
*  112/09/18  V1.00.13   Ryan     恢復可用額度106邏輯                                                                         *				
*  112/09/20  V1.00.14   Ryan     執行前要增加處理檔案保留代數的判斷，刪除過去的檔案   *
*  112/09/21  V1.00.15   Sunny    刪除不必要的程式，如舊的可用餘額處理(效能太差)          *
*  112/10/24  V1.00.16   Sunny    增加程式參數檢核條件                                                 *                                           
*  112/11/14  V1.00.17   Ryan     修正changeEducation                                                 *                                                      																		  
*****************************************************************************/
package Inf;

import java.math.BigDecimal;
import java.nio.file.Paths;

import com.AccessDAO;
import com.CommCol;
import com.CommCrd;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.CommTxInf;
import com.ibm.msg.client.commonservices.Log.Log;

import Cca.CalBalance;
import Cca.CalBalanceBatch;
import java.text.DecimalFormat;
import java.util.Locale;

public class InfR001 extends AccessDAO {
    public static final boolean DEBUG_MODE = false;
	private static final int OUTPUT_BUFF_SIZE = 10000;
	private final String progname = "產生送CRM-客戶相關資料  112/11/14  V1.00.17";
	private static final String CRM_FOLDER = "/media/crm/";
	private static final String DATA_FORM = "CCTCUS";
	private final static String COL_SEPERATOR = "\006";
	private final static String LINE_SEPERATOR = System.lineSeparator();
	private final static int FILE_COUNT = 3;
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
			String searchDate = (args.length == 0) ? "" : args[0].trim();
			showLogMessage("I", "", String.format("程式參數1[%s]", searchDate));
			
			// 檢核參數的日期格式
		       if (args.length >= 1) {
		     	   showLogMessage("I", "", "PARM 1 : [無參數] 表示抓取businday ");
		    	   showLogMessage("I", "", "PARM 1 : [SYSDAY] 表示抓取系統日");
		    	   showLogMessage("I", "", "PARM 1 : [YYYYMMDD] 表示人工指定執行日");
				}
				if(args.length == 1) {
					if (!args[0].toUpperCase(Locale.TAIWAN).equals("SYSDAY")) {
							if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
				        showLogMessage("E", "", String.format("日期格式[%s]錯誤", args[0]));
				        return -1;
				    }
					}
				    searchDate = args[0];
				}
			 
			searchDate = getProgDate(searchDate, "D");

			//日期減一天
			searchDate = commDate.dateAdd(searchDate, 0, 0, -1);
			
			showLogMessage("I", "", String.format("執行日期[%s]", searchDate));
			businessDate = searchDate;
			
			// convert YYYYMMDD into YYMMDD
			String fileNameSearchDate = searchDate.substring(2);

			//保留N代刪除以前檔案
			commTxInf.deleteFile(FILE_COUNT, CRM_FOLDER, "CCTCUS_*.DAT","CCTCUS_*.HDR");
			
			// get the name and the path of the .DAT file
			String datFileName = String.format("%s_%s%s", DATA_FORM, fileNameSearchDate, CommTxInf.DAT_EXTENSION);
			String fileFolder =  Paths.get(commCrd.getECSHOME(),CRM_FOLDER).toString();
			
			if(checkOpenFile(fileFolder, datFileName) != 1) {
				comc.errExit("產生HDR檔錯誤!", "");
			}
			
			//debug message
			if(DEBUG_MODE)
			{
			 showLogMessage("I", "", "【Debug模式】DEBUG_MODE = true......");
			}

			showLogMessage("I", "", "");
			showLogMessage("I", "", "開始產生.DAT檔......");
			// 產生主要檔案 .DAT 
			int dataCount = 0;
			try {					
				//處理持有一般信用卡之卡人資料(106+ID)
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理持有一般信用卡之卡人資料......");
				selectInfR002Data106Id();
				dataCount = generateDatFile(fileFolder, datFileName ,searchDate,"106");
				//處理一般卡--個人附卡
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理一般卡--個人附卡......");
				selectInfR002DataSup1();
				dataCount += generateDatFile(fileFolder, datFileName ,searchDate,"sup1");
				//處理商務卡—個人資料(306+ID)
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理商務卡—個人資料(306+ID)......");
				selectInfR002Data306Id();
				dataCount += generateDatFile(fileFolder, datFileName ,searchDate,"306");
				//處理VD卡--個人資料
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理VD卡--個人資料......");
				selectInfR002Data206Vd();
				dataCount += generateDatFile(fileFolder, datFileName ,searchDate,"206");
				//處理商務卡—公司資料
				showLogMessage("I", "", "");
				showLogMessage("I", "", "開始處理商務卡—公司資料......");
				selectInfR002DataCorp();
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
			InfR001Data infR001Data = getInfData();
			if("106".equals(type)) {
				selectActAcctCurr106(infR001Data);
			}
			String rowOfDAT = getRowOfDAT(infR001Data,type);
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
		closeCursor();

		// write the rest of bytes on the file
		if (countInEachBuffer > 0) {
			showLogMessage("I", "", String.format("將剩下的%d筆資料寫入檔案", countInEachBuffer));
			byte[] tmpBytes = sb.toString().getBytes("MS950");
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
	void changeEducation(InfR001Data infR001Data) {
		String chgEducation = infR001Data.education;
		if (commStr.pos(",1,2", infR001Data.education) > 0) {
			chgEducation = "3";
		}
		if (commStr.pos(",3,4", infR001Data.education) > 0) {
			chgEducation = "2";
		}
		if (commStr.pos(",5,6", infR001Data.education) > 0) {
			chgEducation = "1";
		}
		infR001Data.education = chgEducation;
	}
	
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
		bf.append("(").append(code1.replace("-", "")).append(")-").append(code2).append("#").append(code3);
		return bf.toString();
	}
	
	/**
	 * @return String
	 * @throws Exception 
	 */
	private String getRowOfDAT(InfR001Data infR001Data,String type) throws Exception {
		StringBuffer sb = new StringBuffer();

		//教育程度
		changeEducation(infR001Data);
		
		//可用額度
		double col35 = 0;
//		DecimalFormat df = new DecimalFormat("0");
		Long[] balanceAmts = new Long[2];
		balanceAmts[0] = Long.valueOf(0);
		balanceAmts[1] = Long.valueOf(0);
		//20230915  disable 調整為附卡人可用額度一律帶0
//		if(commStr.pos(",106,sup1",type)>0) {
//			balanceAmts = calBalanceBatch.batchIdNoBalance(infR001Data.idCorpNo);
//		}

		//使用批次已計算好的額度進行處理
		if("106".equals(type)) {
			balanceAmts = calBalanceBatch.batchIdNoBalance(infR001Data.idCorpNo);
	    }
		
		if("306".equals(type)) {
			balanceAmts = calBalanceBatch.batchTotalCorpBalanceById(infR001Data.idCorpNo);
		}
		if("corp".equals(type)) {
			balanceAmts = calBalanceBatch.batchTotalCorpBalanceByCorp(infR001Data.idCorpNo);
		}
		
		//將可用額度轉成整數
		long col35Convert = balanceAmts[0].longValue();
		long col33Convert = balanceAmts[1].longValue();
		
		//debug時再打開
		//showLogMessage("I", "", String.format("查詢可用額度"+type+"=>IdCorpNo[%s]",infR001Data.idCorpNo+","+col35Convert));
		//showLogMessage("I", "", String.format("預借現金餘額"+type+"=>IdCorpNo[%s]",infR001Data.idCorpNo+","+col33Convert));		
		
		sb.append(commCrd.fixLeft(infR001Data.idCorpNo, 11)); //1	身份證號	X(10)==>20230707長度改為X(11)，因306+個人與306+公司，取前10碼會使CRM重複KEY值。
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.chiName, 30));//2	   中文姓名	  X(30)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.nation, 1));//3	國籍別	X(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.engName, 30));//4	英文姓名	X(30)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.sex, 1));//5	性別	9(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.marriage, 1));//6	婚姻狀況	9(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commDate.toTwDate(infR001Data.birthday), 7));//7	客戶生日	9(07)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.classCode, 2));//8	客戶類別	X(02)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.education, 1));//9	教育程度	X(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat((int)numDiv(infR001Data.annualIncome,10000,0), "#0"), 4));//10	年收入	X(04)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.spouseName, 30));  //11	配偶姓名	X(30)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.spouseIdNo, 18));  //12	配偶編號	X(18)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commDate.toTwDate(infR001Data.spouseBirthday), 7));  //13	配偶生日	9(07)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(formatPhone(infR001Data.homeAreaCode1,infR001Data.homeTelNo1,infR001Data.homeTelExt1), 24));  //14	住家電話	X(24)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.cellarPhone, 16));  //15	手機一	X(15)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft("", 15));  //16	手機二	X(15)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(formatPhone(infR001Data.officeAreaCode1,infR001Data.officeTelNo1,infR001Data.officeTelExt1), 24));  //17	公司電話	X(24)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.eMailAddr, 55));   //18	E_MAIL	X(55) 
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.residentZip, 6));  //19	戶籍地郵遞區號	X(06)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infR001Data.residentAddr, 25), 50));  //20	戶籍地一	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infR001Data.residentAddr, 25,25), 50));  //21	戶籍地二	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.mailZip, 6));  //22	居住地郵遞區號	X(06)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infR001Data.mailAddr, 25), 50));  //23	居住地一	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infR001Data.residentAddr, 25,25), 50));  //24	居住地二	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.companyZip, 6));  //25	公司郵遞區號	X(06)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infR001Data.companyAddr, 25), 50));  //26	公司地址一	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infR001Data.companyAddr, 25,25), 50));  //27	公司地址二	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(String.format("000%s", infR001Data.billApplyFlag), 4));  //28	郵寄地址	X(04)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.businessCode, 4));  //29	行業別	X(04)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.companyName, 30));  //30	服務單位	X(30)	
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.jobPosition, 15));  //31	客戶職稱	X(15)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%s", infR001Data.maxLineOfCreditAmtCash), 9));  //32	預現額度	9(09)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%s", col33Convert), 10));  //33	預現餘額	X(10)   先塞0，後續待ALEX的FUNCTION(待開發)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%s", infR001Data.lineOfCreditAmt), 9));  //34	信用卡額度	9(09)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(String.format("%s", col35Convert), 10));  //35	可用額度	X(10) CalBalance.idNoBalance(id_no) 商務卡個人totalCorpBalanceById(String idNo) 商務卡公司totalCorpBalanceByCorp
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.autopayAcctNo, 15));  //36	扣繳帳號	X(15)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.autopayDcIndicator, 1));  //37	扣繳額度	X(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		//sb.append(commCrd.fixLeft(infR001Data.acctStatus38, 1));  //38	繳費方式	X(01)
		sb.append(commCrd.fixLeft(commStr.pos(",1,2", infR001Data.acctStatus38)>0?"0":"1", 1));  //38	繳費方式 0：可循環 1：ㄧ次繳清
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infR001Data.stmtCycle, 2));  //39	帳單結帳日	9(02)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infR001Data.market_agree_base, 1));  //40	接受DM9(01)-->20230707 改為拒絕行銷 0不同意 1同意共銷 2同意共享
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commDate.toTwDate(infR001Data.modDate), 7));  //41	維護日期	9(07)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(infR001Data.org, 3)); //42	銀行別	9(03)  
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.billSendingZip, 6));  //43	其他地郵遞區號	X(06)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.left(infR001Data.billSendingAddr, 25), 50));  //44	其他地址一	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(commStr.mid(infR001Data.billSendingAddr, 25,25), 50));  //45	其他地址二	X(50)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infR001Data.dcEndBalOp901, "#0.00"), 14));  //46	歸戶層之溢付款金額 (台幣)	9(11).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.acctStatus47, 1));  //47	歸戶狀態	X(01)
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infR001Data.dcEndBalOp840, "#0.00"), 14));  //48	歸戶層之溢付款金額(雙幣美元)	9(11).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixRight(commStr.numFormat(infR001Data.dcEndBalOp392, "#0.00"), 14));  //49	歸戶層之溢付款金額(雙幣日元)	9(11).99
		sb.append(commCrd.fixLeft(COL_SEPERATOR, 1));
		sb.append(commCrd.fixLeft(infR001Data.acctType50, 2));  //50	帳戶類別	X(02)
		sb.append(LINE_SEPERATOR);
		return sb.toString();
	}
	
	InfR001Data getInfData() throws Exception {
		InfR001Data infR001Data = new InfR001Data();
		infR001Data.org = getValue("org"); //sunny add
		infR001Data.pSeqno = getValue("P_SEQNO");
		infR001Data.idCorpNo = getValue("ID_CORP_NO");
		infR001Data.lineOfCreditAmt = getValueLong("MAX_LINE_OF_CREDIT_AMT");
		infR001Data.maxLineOfCreditAmtCash = getValueLong("MAX_LINE_OF_CREDIT_AMT_CASH");
		infR001Data.chiName = getValue("CHI_NAME");
		infR001Data.nation = getValue("NATION");
		infR001Data.sex = getValue("SEX");
		infR001Data.marriage = getValue("MARRIAGE");
		infR001Data.birthday = getValue("BIRTHDAY");
		infR001Data.classCode = getValue("CLASS_CODE");
		infR001Data.education = getValue("EDUCATION");
		infR001Data.annualIncome = getValueDouble("ANNUAL_INCOME");
		infR001Data.spouseName = getValue("SPOUSE_NAME");
		infR001Data.spouseIdNo = getValue("SPOUSE_ID_NO");
		infR001Data.spouseBirthday = getValue("SPOUSE_BIRTHDAY");
		infR001Data.homeAreaCode1 = getValue("HOME_AREA_CODE1");
		infR001Data.homeTelNo1 = getValue("HOME_TEL_NO1");
		infR001Data.homeTelExt1 = getValue("HOME_TEL_EXT1");
		infR001Data.cellarPhone = getValue("CELLAR_PHONE");
		infR001Data.officeAreaCode1 = getValue("OFFICE_AREA_CODE1");
		infR001Data.officeTelNo1 = getValue("OFFICE_TEL_NO1");
		infR001Data.officeTelExt1 = getValue("OFFICE_TEL_EXT1");
		infR001Data.eMailAddr = getValue("E_MAIL_ADDR");
		infR001Data.residentZip = getValue("RESIDENT_ZIP");
		infR001Data.residentAddr = getValue("RESIDENT_ADDR");
		infR001Data.mailZip = getValue("MAIL_ZIP");
		infR001Data.mailAddr = getValue("MAIL_ADDR");
		infR001Data.companyZip = getValue("COMPANY_ZIP");
		infR001Data.companyAddr = getValue("COMPANY_ADDR");
		infR001Data.billApplyFlag = getValue("BILL_APPLY_FLAG");
		infR001Data.businessCode = getValue("BUSINESS_CODE");
		infR001Data.companyName = getValue("COMPANY_NAME");
		infR001Data.jobPosition = getValue("JOB_POSITION");
		infR001Data.acctStatus38 = getValue("ACCT_STATUS_38");
		infR001Data.acctStatus47 = getValue("ACCT_STATUS_47");
		infR001Data.stmtCycle = getValue("STMT_CYCLE");
		infR001Data.market_agree_base = getValue("MARKET_AGREE_BASE"); //從接受DM(ACCEPT_DM)改為拒絕行銷(MARKET_AGREE_BASE)-0,1,2
		infR001Data.modDate = getValue("MOD_DATE");
		infR001Data.billSendingZip = getValue("BILL_SENDING_ZIP");
		infR001Data.billSendingAddr = getValue("BILL_SENDING_ADDR");
		infR001Data.engName = getValue("ENG_NAME");
		infR001Data.autopayAcctNo = getValue("AUTOPAY_ACCT_NO");
		infR001Data.autopayDcIndicator = getValue("AUTOPAY_DC_INDICATOR");
		infR001Data.dcEndBalOp901 = getValueDouble("DC_OP_BAL_901");
		infR001Data.dcEndBalOp840 = getValueDouble("DC_OP_BAL_840");
		infR001Data.dcEndBalOp392 = getValueDouble("DC_OP_BAL_392");
		infR001Data.corpPSeqno = getValue("CORP_P_SEQNO");
		infR001Data.acctType50 = getValue("ACCT_TYPE");
		return infR001Data;
	}
	
	/***
	 * 處理一般卡--個人正卡(106雙幣扣繳及溢繳金額資訊)
	 * **/
	void selectActAcctCurr106(InfR001Data infR001Data) throws Exception {
		extendField = "ACT_ACAG_CURR.";
		sqlCmd = " SELECT A.P_SEQNO,trim(RIGHT(A.AUTOPAY_ACCT_NO,15)) AS AUTOPAY_ACCT_NO,A.AUTOPAY_DC_INDICATOR, ";
		sqlCmd += " NVL((SELECT DC_END_BAL_OP FROM ACT_ACCT_CURR WHERE CURR_CODE='901' AND P_SEQNO=A.P_SEQNO),0) AS DC_OP_BAL_901, ";
		sqlCmd += " NVL((SELECT DC_END_BAL_OP FROM ACT_ACCT_CURR WHERE CURR_CODE='840' AND P_SEQNO=A.P_SEQNO),0) AS DC_OP_BAL_840, ";
		sqlCmd += " NVL((SELECT DC_END_BAL_OP FROM ACT_ACCT_CURR WHERE CURR_CODE='392' AND P_SEQNO=A.P_SEQNO),0) AS DC_OP_BAL_392 ";
		sqlCmd += " FROM ACT_ACCT_CURR A WHERE A.ACCT_TYPE='01' AND A.CURR_CODE='901' AND A.P_SEQNO = ? ";
		setString(1,infR001Data.pSeqno);
		int recordCnt = selectTable();
		
		if (recordCnt > 0) {		
		infR001Data.autopayAcctNo = getValue("ACT_ACAG_CURR.AUTOPAY_ACCT_NO");
		infR001Data.autopayDcIndicator = getValue("ACT_ACAG_CURR.AUTOPAY_DC_INDICATOR");
		infR001Data.dcEndBalOp901 = getValueDouble("ACT_ACAG_CURR.DC_OP_BAL_901");
		infR001Data.dcEndBalOp840 = getValueDouble("ACT_ACAG_CURR.DC_OP_BAL_840");
		infR001Data.dcEndBalOp392 = getValueDouble("ACT_ACAG_CURR.DC_OP_BAL_392");
		}
	}
	
	/*******
	 * 處理一般卡--個人正卡
	 * 卡人等級(CLASS_CODE):先抓以卡人檔.信評等級(舊)為主，若卡人檔.信評等級(舊)無值，則改抓卡人檔.信評等級(新)。
	 * *****/
	private void selectInfR002Data106Id() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT B.ID_NO as id_corp_no,A.P_SEQNO,a.acct_type,DECODE(A.ACCT_TYPE,'01','106','') AS org,A.LINE_OF_CREDIT_AMT as MAX_LINE_OF_CREDIT_AMT,A.LINE_OF_CREDIT_AMT_CASH as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,decode(A.BILL_APPLY_FLAG,'','0',A.BILL_APPLY_FLAG) as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,A.ACCT_STATUS as ACCT_STATUS_38,A.ACCT_STATUS as ACCT_STATUS_47,A.STMT_CYCLE,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,A.BILL_SENDING_ZIP ")
		.append(" ,(A.BILL_SENDING_ADDR1 || A.BILL_SENDING_ADDR2 || A.BILL_SENDING_ADDR3 || A.BILL_SENDING_ADDR4 || A.BILL_SENDING_ADDR5) AS BILL_SENDING_ADDR ")
		.append(" , B.ENG_NAME ") //英文姓名讀卡人層即可
		//.append(" ,(SELECT ENG_NAME FROM CRD_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,'' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR , '' as CORP_P_SEQNO ")
		.append(" FROM CRD_IDNO B ")
		.append(" LEFT JOIN ACT_ACNO A ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		//.append(" WHERE A.CARD_INDICATOR='1' ")
		.append(" WHERE A.ACNO_FLAG = '1' ")
		.append(" AND EXISTS (SELECT 1 FROM crd_card c WHERE c.id_p_seqno=b.id_p_seqno and c.P_SEQNO=a.p_seqno and c.acno_flag = '1' and c.SUP_FLAG='0') "); //20230712 add 須要有正卡
		//.append(" AND EXISTS (SELECT 1 FROM crd_card c WHERE c.P_SEQNO=a.p_seqno AND acct_type='01' AND SUP_FLAG='0') ");//20230707 add 須要有正卡

		if(DEBUG_MODE)
		{
		  //sb.append(" AND LEFT(B.ID_NO,1)='H' ");
		  //sb.append(" AND B.ID_NO='H174490220' ");
			sb.append(" limit 5 ");
		}
		//.append(" AND A.ACCT_TYPE = '01' limit 1000");
		//.append(" AND A.ACCT_TYPE = '01' AND LEFT(B.ID_NO,1)='P'");
		sqlCmd = sb.toString();
		openCursor();
	}
	

	/*******
	 * 處理一般卡--個人附卡(備份，確定不用後再刪除)
	 * *****/
	/*
	private void selectInfR002DataSup1bak() throws Exception {
		StringBuffer sb = new StringBuffer();
		//sb.append(" SELECT B.ID_NO as id_corp_no,A.P_SEQNO,DECODE(A.ACCT_TYPE,'01','106','') AS org,A.LINE_OF_CREDIT_AMT as MAX_LINE_OF_CREDIT_AMT,A.LINE_OF_CREDIT_AMT_CASH as MAX_LINE_OF_CREDIT_AMT_CASH ")
		sb.append(" SELECT B.ID_NO as id_corp_no,A.P_SEQNO,'106' AS org,A.LINE_OF_CREDIT_AMT as MAX_LINE_OF_CREDIT_AMT,A.LINE_OF_CREDIT_AMT_CASH as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,decode(A.BILL_APPLY_FLAG,'','0',A.BILL_APPLY_FLAG) as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,A.ACCT_STATUS as ACCT_STATUS_38,A.ACCT_STATUS as ACCT_STATUS_47,A.STMT_CYCLE,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,A.BILL_SENDING_ZIP ")
		.append(" ,(A.BILL_SENDING_ADDR1 || A.BILL_SENDING_ADDR2 || A.BILL_SENDING_ADDR3 || A.BILL_SENDING_ADDR4 || A.BILL_SENDING_ADDR5) AS BILL_SENDING_ADDR ")
		.append(" ,(SELECT ENG_NAME FROM CRD_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,'' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR , '' as CORP_P_SEQNO ")
		.append(" FROM CRD_IDNO B ")
		.append(" LEFT JOIN ( select ID_P_SEQNO,sum(decode(sup_flag,'0',1,0)) as card_sup0_cnt ,sum(decode(sup_flag,'1',1,0)) as card_sup1_cnt ")
		.append(" from crd_card WHERE ACCT_TYPE = '01' group by ID_P_SEQNO ) C ON B.ID_P_SEQNO = C.ID_P_SEQNO ")
		.append(" LEFT JOIN ACT_ACNO A ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" WHERE A.CARD_INDICATOR='1' ")
		.append(" AND A.ACCT_TYPE = '01' and c.card_sup0_cnt = 0 AND c.card_sup1_cnt > 0 ");
		sqlCmd = sb.toString();
		openCursor();
	}
	*/
	
	/*******
	 * 處理一般卡--個人純附卡(無acno資料)
	 * *****/
	private void selectInfR002DataSup1() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT B.ID_NO as id_corp_no,'' as P_SEQNO,'01' as acct_type,'106' AS org,'0' as MAX_LINE_OF_CREDIT_AMT,'0' as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,'0' as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,'' as ACCT_STATUS_38,'' as ACCT_STATUS_47,'01' as STMT_CYCLE,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,'' as BILL_SENDING_ZIP ")
		.append(" ,'' AS BILL_SENDING_ADDR , ENG_NAME ") //英文姓名讀卡人層即可
		//.append(" ,(SELECT ENG_NAME FROM CRD_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,'' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR , '' as CORP_P_SEQNO ")
		.append(" FROM CRD_IDNO B ")
		.append(" INNER JOIN ( select ID_P_SEQNO,sum(decode(sup_flag,'0',1,0)) as card_sup0_cnt ,sum(decode(sup_flag,'1',1,0)) as card_sup1_cnt ")
		.append(" from crd_card WHERE ACCT_TYPE = '01' group by ID_P_SEQNO ) C ON B.ID_P_SEQNO = C.ID_P_SEQNO ")
		.append(" and c.card_sup0_cnt = 0 AND c.card_sup1_cnt > 0 ");
		// sunny test
		//.append(" AND LEFT(B.ID_NO,1)='H' ");
		//.append(" AND B.ID_NO='H174490220' ");
		
		if(DEBUG_MODE)
		{
		  //sb.append(" AND LEFT(B.ID_NO,1)='H' ");
		  //sb.append(" AND B.ID_NO='H174490220' ");
			sb.append(" limit 5 ");
		}
		sqlCmd = sb.toString();
		openCursor();
	}
	
	/*******
	 * 處理商務卡--個人
	 * *****/
	private void selectInfR002Data306Id() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT * FROM ( SELECT ROW_NUMBER() OVER(PARTITION BY b.id_no ORDER BY b.id_no DESC) AS Row_num ")
		.append(" ,a.acct_type,decode(a.ACCT_TYPE,'03','306','06','306','306') as org,a.CORP_P_SEQNO,a.ACNO_FLAG ")
		//.append(" ,'306' as org,a.CORP_P_SEQNO,a.ACNO_FLAG ")
		.append(" ,c.MAX_LINE_OF_CREDIT_AMT,c.MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.ID_NO as id_corp_no,A.P_SEQNO,A.LINE_OF_CREDIT_AMT,A.LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,decode(A.BILL_APPLY_FLAG,'','0',A.BILL_APPLY_FLAG) as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,'' as ACCT_STATUS_38,A.ACCT_STATUS as ACCT_STATUS_47,'' as STMT_CYCLE,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,A.BILL_SENDING_ZIP ")
		.append(" ,(A.BILL_SENDING_ADDR1 || A.BILL_SENDING_ADDR2 || A.BILL_SENDING_ADDR3 || A.BILL_SENDING_ADDR4 || A.BILL_SENDING_ADDR5) AS BILL_SENDING_ADDR ")
		.append(" , ENG_NAME ") //英文姓名讀卡人層即可
		//.append(" ,(SELECT ENG_NAME FROM CRD_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,'' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR ")
		.append(" FROM crd_idno b ")
		.append(" LEFT JOIN act_acno a ON a.id_p_seqno = b.id_p_seqno ")
		.append(" LEFT JOIN ( SELECT ID_P_SEQNO,max(LINE_OF_CREDIT_AMT) AS max_line_of_credit_amt,max(LINE_OF_CREDIT_AMT_CASH) AS MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" FROM act_acno WHERE CARD_INDICATOR='2' and acno_flag <> '2' GROUP BY ID_P_SEQNO ) c ON a.ID_P_SEQNO = c.ID_P_SEQNO ")
		.append(" WHERE a.CARD_INDICATOR='2' and a.acno_flag ='3' AND a.ACCT_TYPE IN ('03','06')) WHERE Row_num=1 ");
		
		if(DEBUG_MODE)
		{
			sb.append(" limit 5 ");
		}
		
		sqlCmd = sb.toString();
		openCursor();
	}
	
	/*******
	 * 處理VD卡--個人資料
	 * *****/
	private void selectInfR002Data206Vd() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT * FROM ( SELECT ROW_NUMBER() OVER(PARTITION BY b.id_no ORDER BY b.id_no,nvl(c.current_cnt,0) desc,a.MOD_TIME DESC) AS Row_num ")
		//.append(" ,B.ID_NO as id_corp_no,A.P_SEQNO,DECODE(A.ACCT_TYPE,'90','206','') AS org,0 as MAX_LINE_OF_CREDIT_AMT,0 as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.ID_NO as id_corp_no,A.P_SEQNO,a.acct_type,'206' AS org,0 as MAX_LINE_OF_CREDIT_AMT,0 as MAX_LINE_OF_CREDIT_AMT_CASH ")
		.append(" ,B.CHI_NAME,B.NATION,B.SEX,B.MARRIAGE,B.BIRTHDAY,DECODE(B.CREDIT_LEVEL_OLD,'',B.CREDIT_LEVEL_NEW,B.CREDIT_LEVEL_OLD) AS CLASS_CODE,B.EDUCATION,B.ANNUAL_INCOME,B.SPOUSE_NAME,B.SPOUSE_ID_NO ")
		.append(" ,B.SPOUSE_BIRTHDAY,B.HOME_AREA_CODE1,B.HOME_TEL_NO1,B.HOME_TEL_EXT1,B.CELLAR_PHONE,B.OFFICE_AREA_CODE1,B.OFFICE_TEL_NO1,B.OFFICE_TEL_EXT1 ")
		.append(" ,B.E_MAIL_ADDR,B.RESIDENT_ZIP,(B.RESIDENT_ADDR1 || B.RESIDENT_ADDR2 || B.RESIDENT_ADDR3 || B.RESIDENT_ADDR4 || B.RESIDENT_ADDR5) AS RESIDENT_ADDR ")
		.append(" ,B.MAIL_ZIP,(B.MAIL_ADDR1 || B.MAIL_ADDR2 || B.MAIL_ADDR3 || B.MAIL_ADDR4 || B.MAIL_ADDR5 ) AS MAIL_ADDR, B.COMPANY_ZIP ")
		.append(" ,(B.COMPANY_ADDR1 || B.COMPANY_ADDR2 || B.COMPANY_ADDR3 || B.COMPANY_ADDR4 || B.COMPANY_ADDR5) AS COMPANY_ADDR,'0000' as BILL_APPLY_FLAG,B.BUSINESS_CODE ")
		.append(" ,B.COMPANY_NAME,B.JOB_POSITION,'' as ACCT_STATUS_38,A.ACCT_STATUS as ACCT_STATUS_47,A.STMT_CYCLE,B.MARKET_AGREE_BASE,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,'' BILL_SENDING_ZIP ")
		.append(" ,'' AS BILL_SENDING_ADDR, '' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 , '' as AUTOPAY_ACCT_NO, '' as AUTOPAY_DC_INDICATOR ,'' as CORP_P_SEQNO ")
		//.append(" ,(SELECT ENG_NAME FROM DBC_CARD WHERE ID_P_SEQNO = B.ID_P_SEQNO ORDER BY ISSUE_DATE DESC FETCH FIRST 1 ROWS ONLY) AS ENG_NAME ")
		.append(" ,B.ENG_NAME ") //英文姓名讀卡人層即可
		.append(" ,nvl(c.current_cnt,0) AS current_cnt ")
		.append(" FROM dbc_idno B ")
		.append(" LEFT JOIN dba_acno A ON A.ID_P_SEQNO = B.ID_P_SEQNO ")
		.append(" LEFT JOIN (SELECT p_seqno,count(*) AS current_cnt FROM dbc_card WHERE CURRENT_CODE='0' GROUP BY p_seqno) c ON a.P_SEQNO=c.p_seqno ")
		.append(" WHERE A.CARD_INDICATOR='1' ")
		.append(" AND A.ACCT_TYPE = '90' ) WHERE Row_num=1 ");
		// sunny test
		//.append(" AND id_corp_no='H174490220' ");
		//.append(" AND LEFT(id_corp_no,1)='H' ");
		//.append(" AND A.ACCT_TYPE = '90' ) WHERE Row_num=1 limit 1000");
		if(DEBUG_MODE)
		{
		  // sunny test
		  //sb.append(" AND LEFT(id_corp_no,1)='H' ");
		  //sb.append(" AND id_corp_no='H174490220' ");
			sb.append(" limit 5 ");
		}
		sqlCmd = sb.toString();
		openCursor();
	}
	
	/*******
	 * 處理商務卡—公司資料
	 *  調整地址：
	 *  戶籍地址(RESIDENT_ADDR欄位預設為空)
	 *  公司登記地(REG_ADDR)OUTPUT時放在公司地址(COMPANY_ADDR)的欄位(非戶籍地)，
	 *  公司通訊地址(COMM_ADDR)OUTPUT時放在居住地址(MAIL_ADDR)。
	 * *****/
	private void selectInfR002DataCorp() throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT a.acct_type,decode(a.ACCT_TYPE,'03','306','06','306','') as org, b.corp_no as id_corp_no,B.CHI_NAME ,'' as nation ,b.eng_name ")
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
		.append(" ,TO_CHAR(B.MOD_TIME,'YYYYMMDD') AS MOD_DATE,'' as AUTOPAY_DC_INDICATOR, A.acct_status as ACCT_STATUS_38,A.acct_status as ACCT_STATUS_47, '' as STMT_CYCLE, '' as MARKET_AGREE_BASE, '' as BILL_SENDING_ZIP ")
		.append(" ,'' AS BILL_SENDING_ADDR, '' as DC_OP_BAL_901, '' as DC_OP_BAL_840, '' as DC_OP_BAL_392 ")
		.append(" ,B.CORP_TEL_ZONE1 as OFFICE_AREA_CODE1, B.CORP_TEL_NO1 as OFFICE_TEL_NO1, B.CORP_TEL_EXT1 as OFFICE_TEL_EXT1 ")
		.append(" FROM crd_corp b ")
		.append(" INNER JOIN (SELECT * FROM act_acno WHERE CARD_INDICATOR='2' and acno_flag='2') A ON a.corp_p_seqno = b.corp_p_seqno ")
		.append(" LEFT JOIN ( SELECT corp_p_seqno,sum(LINE_OF_CREDIT_AMT) AS sum_line_of_credit_amt,sum(line_of_credit_amt_cash) AS sum_line_of_credit_amt_cash ")
		.append(" FROM act_acno WHERE CARD_INDICATOR='2' and acno_flag='2'GROUP BY corp_p_seqno) C ON a.corp_p_seqno =c.corp_p_seqno");

		if(DEBUG_MODE)
		{
			//sb.append(" limit 5 ");
		}
		sqlCmd = sb.toString();
		openCursor();
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
		InfR001 proc = new InfR001();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}
}

class InfR001Data{
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
	String autopayAcctNo = "";
	String autopayDcIndicator = "";
	String acctStatus38 = "";
	String acctStatus47 = "";
	String stmtCycle = "";
	//String acceptDm = "";
	String market_agree_base="";
	String modDate = "";
	String org = "";
	String billSendingZip = "";
	String billSendingAddr = "";
	double dcEndBalOp901 = 0;
	double dcEndBalOp840 = 0; 
	double dcEndBalOp392 = 0; 
	String corpPSeqno = "";
	String acctType50 = "";
}




