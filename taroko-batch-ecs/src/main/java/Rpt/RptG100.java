package Rpt;
/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/07/26  V1.00.00    ryan     program initial                            *
 *  112/08/18  V1.00.01    ryan     增加備註說明,金額合計                                                                                  *
 *  112/08/18  V1.00.02    ryan     處理中文姓名因亂碼導致報表位移問題                                                        *
 *  112/12/11  V1.00.03    ryan     新增RCRD87、RCRD87A報表                                                                *
 *  112/12/12  V1.00.04    ryan     調整RCRD87、RCRD87A報表                                                                *
 *  112/12/22  V1.00.05    ryan     調整RCRD87、RCRD87A報表文字                                                         *
 *  112/12/28  V1.00.06    ryan     調整RCRD87、RCRD87A報表文字                                                         *
 ******************************************************************************/

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;
import com.BaseBatch;
import com.CommCrd;

public class RptG100 extends BaseBatch {
private String progname = "信用卡利害關係人授信明細表(CRD87)  112/12/28 V1.00.06";
CommFunction comm = new CommFunction();
CommCrdRoutine comcr =null;
CommDate commd = new CommDate();
CommCrd comc = new CommCrd();
CommString coms = new CommString();

private int pageCnt = 0 ;
private int pageTxtCnt = 0 ;
private int pageTxt2Cnt = 0 ;
List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
int reportSeq = 0;
private final String reportId = "RptG100";
private final String FILE_PATH = "/media/act/";
private final String FILE_NAME_RCRD87 = "RCRD87_yyyymmdd.1.TXT";
private final String FILE_NAME_RCRD87A = "RCRD87A_yyyymmdd.1.TXT";
private final String LINE_SEPERATOR = System.lineSeparator();
private final int OUTPUT_BUFF_SIZE = 66;
private final String reportName = "信用卡利害關係人授信明細表(CRD87)";
private final String reportName1 = "信用卡利害關係人授信明細表(RCRD87)　　";
private final String reportName2 = "信用卡利害關係人授信異常明細表(RCRD87A)";
private int indexCnt = 0;
private int indexTxtCnt = 0;
private int indexTxt2Cnt = 0;
// ---------------------------------------------------------------------------
private String hPSeqno = "";
private String hRegBankNo = "";
private String hChiName = "";
private String hCorrelateId = "";
private String hRelateStatus = "";
private long hLineOfCreditAmt = 0;
private double hRcrateYear = 0;
private double hDebtAcctSum = 0;
private double hTolDebtAcctSum = 0;
private String hRcUseIndicator = "";
private String hRcUseSDate = "";
private String hRcUseEDate = "";
private String hPaymentType = "";
private String hRemark = "";

//-----------------------------------------------------------------------------
private String hBusinessDate = "";

//=****************************************************************************
public static void main(String[] args) {
	RptG100 proc = new RptG100();
//	proc.debug = true;
	proc.mainProcess(args);
	proc.systemExit();
}

// ---------------------------------------------------------------------------
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);


	dbConnect();
	comcr =new com.CommCrdRoutine(getDBconnect(), getDBalias());
	
//	callBatch(0, 0, 0);

	if (args.length > 0 && args[0].length() == 8) {
		setBusiDate(args[0]);
	}

	hBusinessDate = comcr.getBusiDate();
	selectCrdCorrelate();
	
	if (lpar1.size() == 0) {
		printHeader();
	}
	if(lpar1.size()>0){
		comcr.insertPtrBatchRpt(lpar1);
	}
	lpar1.clear();
	
	procFTP(FILE_NAME_RCRD87,FILE_NAME_RCRD87A);
	backup(FILE_NAME_RCRD87);
	backup(FILE_NAME_RCRD87A);
	
	sqlCommit(1);
	endProgram();
}


void selectCrdCorrelate() throws Exception {
	StringBuffer sbTxt = new StringBuffer();
	StringBuffer sbTxt2 = new StringBuffer();
	Map colMap = new HashMap();
	List<Map> colList = new ArrayList<Map>();
	int fo = openFile1();
	int fo2 = openFile2();
	if(fo == -1 || fo2 == -1) {
		return;
	}
	double hTolDebtAcctSum4Txt = 0;
	double hTolDebtAcctSum4Txt2 = 0;
	String hRegBankNoTmp = "";
	sqlCmd = " select * from ( "
			+ " select a.correlate_id , a.p_seqno ,UF_CHI_NAME(a.correlate_id) as chi_name , "
			+ " a.relate_status , b.line_of_credit_amt , b.rcrate_year , b.rc_use_indicator , "
			+ " b.rc_use_s_date , b.rc_use_e_date , "
			+ " (select reg_bank_no  from crd_card where p_seqno = a.p_seqno "
			+ " order by ori_issue_date desc fetch first 1 rows only) as reg_bank_no "
			+ " from crd_correlate a left join act_acno b on a.p_seqno = b.acno_p_seqno "
			+ " where a.bk_flag = 'Y'  and a.correlate_id_code = '' and b.acct_type='01' "
			+ " ) aa "
			+ "  where aa.reg_bank_no != '' "
//			+ " and aa.line_of_credit_amt > 0 "//test
			+ " order by aa.reg_bank_no,aa.correlate_id ";
//			+ " fetch first 200 rows only";//test

	this.openCursor();
	while (this.fetchTable()) {
		resetData();
		totalCnt ++;
		hPSeqno = getValue("p_seqno");
		hChiName = getValue("chi_name");//姓名
		hCorrelateId = getValue("correlate_id");//id
		hRelateStatus = getValue("relate_status");//利害關係情形
		hLineOfCreditAmt = getValueLong("line_of_credit_amt");//信用額度
		hRcrateYear = getValueDouble("rcrate_year");//	年利率
		hRcUseIndicator = getValue("rc_use_indicator");
		hRcUseSDate = getValue("rc_use_s_date");
		hRcUseEDate = getValue("rc_use_e_date");

		hRegBankNo = selectCrdCard();//營業單位
		hDebtAcctSum = selectActDebt();//循環信用餘額
		
		//繳款方式
		hPaymentType = commString.decode(hRcUseIndicator, ",1,2,3", ",0,2,1");
		if("2".equals(hPaymentType) ) {
			if( hBusinessDate.compareTo(hRcUseSDate) >= 0  
					&& hBusinessDate.compareTo(hRcUseEDate) <= 0) {
				hPaymentType = "0";
			}else {
				hPaymentType = "1";
			}
		}
			
		if(hDebtAcctSum >= 200000) {
			hRemark = "*";
			colMap = new HashMap();
			colMap.put("chi_name", hChiName);
			colMap.put("correlate_id", hCorrelateId);
			colMap.put("relate_status", hRelateStatus);
			colMap.put("line_of_credit_amt", hLineOfCreditAmt);
			colMap.put("rcrate_year", hRcrateYear);
			colMap.put("reg_bank_no", hRegBankNo);
			colMap.put("debt_acct_sum", hDebtAcctSum);
			colMap.put("payment_type", hPaymentType);
			colList.add(colMap);
		}
		

		if (indexCnt == 0) {
			printHeader();
		}
		
		indexCnt++;
		indexTxtCnt++;
		
		printDetail();

		if (indexCnt > 55) {
			indexCnt = 0;
		}
		
		//RCRD87_yyyymmdd.TXT
		if(indexTxtCnt == 1 ||  indexTxtCnt % OUTPUT_BUFF_SIZE == 0) {
			pageTxtCnt ++;
		}
		
		if(!hRegBankNo.equals(hRegBankNoTmp) || indexTxtCnt % OUTPUT_BUFF_SIZE == 0){
			if(!hRegBankNo.equals(hRegBankNoTmp) && commString.empty(hRegBankNoTmp) == false ) {
				pageTxtCnt = 1;
				sbTxt.append(printFooterTxt(hTolDebtAcctSum4Txt));
				indexTxtCnt += 15;
				hTolDebtAcctSum4Txt = 0;
			}
			indexTxtCnt = 0;
			sbTxt.append(printHeaderTxt(pageTxtCnt,"RCRD87",reportName1,hRegBankNo));
			indexTxtCnt += 8;
			hRegBankNoTmp = hRegBankNo;
		}
		
		sbTxt.append(printDetailTxt());
		hTolDebtAcctSum4Txt += hDebtAcctSum;

		if(totalCnt % 5000 == 0) {
			printf("已處理[" + totalCnt + "]筆");
		}
	}
	closeCursor();
	printFooter();
	if(indexTxtCnt > 0) {
		sbTxt.append(printFooterTxt(hTolDebtAcctSum4Txt));
		writeTxt(fo,sbTxt);
	}

	//RCRD87A_yyyymmdd.TXT
	colList.sort(Comparator.comparing(o -> o.get("correlate_id").toString()));
	for(int i = 0; i<colList.size() ;i++) {
		hChiName = colList.get(i).get("chi_name").toString();
		hCorrelateId = colList.get(i).get("correlate_id").toString();
		hRelateStatus = colList.get(i).get("relate_status").toString();
		hLineOfCreditAmt = str2Long(colList.get(i).get("line_of_credit_amt").toString());
		hRcrateYear = coms.ss2Num(colList.get(i).get("rcrate_year").toString());
		hRegBankNo = colList.get(i).get("reg_bank_no").toString();
		hDebtAcctSum = coms.ss2Num(colList.get(i).get("debt_acct_sum").toString());
		hPaymentType = colList.get(i).get("payment_type").toString();

	
		if(hDebtAcctSum >= 200000) {
			hRemark = "*";
			indexTxt2Cnt++;
			if (indexTxt2Cnt == 1 || indexTxt2Cnt % OUTPUT_BUFF_SIZE == 0) {
				pageTxt2Cnt++;
				sbTxt2.append(printHeaderTxt(pageTxt2Cnt,"RCRD87A",reportName2,"3144"));
				indexTxt2Cnt += 8;
			}
			sbTxt2.append(printDetailTxt());
			hTolDebtAcctSum4Txt2 += hDebtAcctSum;
		}
	}
	
	if(indexTxt2Cnt > 0) {
		sbTxt2.append(printFooterTxt(hTolDebtAcctSum4Txt2));
		writeTxt(fo2,sbTxt2);
	}

	closeBinaryOutput2(fo);
	closeBinaryOutput2(fo2);
}

long str2Long(String str) {
	try {
		return Long.parseLong(str);
	}catch(Exception ex) {
		return 0;
	}
}

void resetData() {
	hPSeqno = "";
	hRegBankNo = "";
	hChiName = "";
	hCorrelateId = "";
	hRelateStatus = "";
	hLineOfCreditAmt = 0;
	hRcrateYear = 0;
	hDebtAcctSum = 0;
	hRcUseIndicator = "";
	hRcUseSDate = "";
	hRcUseEDate = "";
	hPaymentType = "";
	hRemark = "";
}

double selectActDebt() throws Exception {
	sqlCmd = " select sum(decode(b.interest_method,'Y',a.end_bal,0)) as debt_acct_sum "
			+ " from act_debt a, ptr_actcode b, ptr_workday c "
			+ " where a.p_seqno = ? "
			+ " and a.acct_code = b.acct_code "
			+ " and a.stmt_cycle = c.stmt_cycle "
			+ " and a.acct_month <= c.this_acct_month ";

	setString(1,hPSeqno);
	selectTable();
	if (sqlNrow > 0) {
		return getValueDouble("debt_acct_sum");
	}
	return 0;
}

String selectCrdCard() throws Exception {
	extendField = "card.";
	sqlCmd = " select " + " reg_bank_no " + " from crd_card " + " where p_seqno = ? "
			+ " order by ori_issue_date desc fetch first 1 rows only ";
	setString(1,hPSeqno);
	selectTable();

	if (sqlNrow>0)
		 return getValue("card.reg_bank_no");
	return "";
}

// ---------------------------------------------------------------------------
void printHeader() throws Exception {
	pageCnt++;
	StringBuffer tt = new StringBuffer();
	commString.insertCenter(tt, "合作金庫商業銀行", 130);
	printAdd(tt.toString());
		
	tt =new StringBuffer();
	commString.insertCenter(tt, "信用卡利害關係人授信明細表(CRD87)", 130);
	printAdd(tt.toString());	

	tt =new StringBuffer();
	commString.insert(tt, "報表編號:RptG100", 1);
	commString.insert(tt, "頁    次:", 119);	
	commString.insert(tt, commString.int2Str(pageCnt), 128);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, "統計日期:", 1);
	commString.insert(tt, commDate.dspDate(hBusiDate), 10);
	commString.insert(tt, "列印日期:", 119);
	commString.insert(tt, commDate.dspDate(sysDate), 128);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, "營業單位", 1);
	commString.insert(tt, "姓名", 15);
	commString.insert(tt, "ID", 30);
	commString.insert(tt, "利害關係情形", 45);
	commString.insert(tt, "信用額度", 60);
	commString.insert(tt, "全行總循環信用餘額", 75);
	commString.insert(tt, "年利率", 90);
	commString.insert(tt, "動用循環方式", 105);
	commString.insert(tt, "備註", 120);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, commString.rpad("=", 136 ,"="), 1);
	printAdd(tt.toString());
}


void printDetail() throws Exception {
	String temp = "";
	indexCnt++;
	StringBuffer tt = new StringBuffer();
	commString.insert(tt, hRegBankNo, 1);
	commString.insert(tt, ssFmt(hChiName), 15);
	commString.insert(tt, hCorrelateId, 30);
	commString.insert(tt, hRelateStatus, 45);
	commString.insert(tt, commString.lpad(String.format("%,d", hLineOfCreditAmt),13), 60);	
	temp = String.format("%,.2f", hDebtAcctSum);
	commString.insert(tt, commString.lpad(temp, 13), 75);	
	temp = String.format("%,.3f", hRcrateYear);
	commString.insert(tt, commString.lpad(temp, 11), 90);
	commString.insert(tt, hPaymentType, 105);
	commString.insert(tt, hRemark, 120);
	printAdd(tt.toString());
	hTolDebtAcctSum += hDebtAcctSum;
}

void printFooter() throws Exception {
	StringBuffer tt = new StringBuffer();
	commString.insert(tt, commString.rpad("=", 136 ,"="), 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "合計", 1);
	commString.insert(tt, "循環信用餘額總計（不含負數餘額）：", 40);
	commString.insert(tt, String.format("%,.2f", hTolDebtAcctSum), 78);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "註（１）：利害關係情形：１．本行員工　２．本行負責人　３．本行主要股東　４．與本行負責人或辦理授信職員有利害關係者　５．本行持有實收資本額百分之", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "三以上企業。", 11);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "註（２）：動用循環方式：０：可動用循環信用　１：不可動用循環信用。", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "註（３）：全行總循環信用餘額：指逾繳款截止日（約每月２０日）仍未繳納之金額，故本欄位於繳款截止日前並非循環信用餘額。每月２０日列印之報表", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "中，對於「全行總循環信用餘額」逾２０萬元者（備註欄位顯示*），應請其立即還款。若未能還款至２０萬元以下，經辦人員應將其卡片辦理", 11);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "暫時停止使用。", 11);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "註（４）：業績單位不同者，則歸屬於最近申辦之營業單位。", 1);
	printAdd(tt.toString());
}


//RCRD87_yyyymmdd.TXT
String printHeaderTxt(int pageCnt,String fileNo ,String fileName ,String regBankNo) throws Exception {
	StringBuffer tt = new StringBuffer();
	tt.append(comc.fixLeft(String.format("%-10s%-16s%-7s%-45s%22s", regBankNo,fileNo,commd.toTwDate(hBusiDate),fileName,"N"), 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft(comc.fixRight("合作金庫商業銀行", 70),132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft(comc.fixRight(fileName, 80),132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("報表編號:RptG100", 66));
	tt.append(comc.fixRight("頁    次:", 66));
	tt.append(comc.fixLeft(commString.int2Str(pageCnt), 128));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("統計日期:", 10));
	tt.append(comc.fixLeft(commDate.dspDate(hBusiDate), 56));
	tt.append(comc.fixRight("列印日期:", 56));
	tt.append(comc.fixRight(commDate.dspDate(sysDate), 11));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft(" ", 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("營業單位", 14));
	tt.append(comc.fixLeft("姓名", 14));
	tt.append(comc.fixLeft("ID", 14));
	tt.append(comc.fixLeft("利害關係情形", 18));
	tt.append(comc.fixLeft("信用額度", 14));
	tt.append(comc.fixLeft("全行總循環信用餘額", 22));
	tt.append(comc.fixLeft("年利率", 14));
	tt.append(comc.fixLeft("動用循環方式", 14));
	tt.append(comc.fixLeft("備註", 14));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft(commString.rpad("=", 132 ,"="), 132));
	tt.append(LINE_SEPERATOR);

	return tt.toString();
}

String printDetailTxt() throws Exception {
	String temp = "";
	StringBuffer tt = new StringBuffer();
	tt.append(comc.fixLeft("  "+hRegBankNo, 14));
	tt.append(comc.fixLeft(ssFmt(hChiName), 14));
	tt.append(comc.fixLeft(hCorrelateId, 14));
	tt.append(comc.fixLeft("      "+hRelateStatus, 18));
	tt.append(comc.fixLeft(commString.lpad(String.format("%,d", hLineOfCreditAmt),13), 14));
	temp = String.format("%,.2f", hDebtAcctSum);
	tt.append(comc.fixLeft(commString.lpad(temp, 13), 22));
	temp = String.format("%,.3f", hRcrateYear);
	tt.append(comc.fixLeft(commString.lpad(temp, 11), 14));
	tt.append(comc.fixLeft("      "+hPaymentType, 14));
	tt.append(comc.fixLeft("  "+hRemark, 14));
	tt.append(LINE_SEPERATOR);
	return tt.toString();
}

String printFooterTxt(double hTolDebtAcctSum4Txt) throws Exception {
	StringBuffer tt = new StringBuffer();
	tt.append(comc.fixLeft(commString.rpad("=", 132 ,"="), 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("合計", 40));
	tt.append(comc.fixLeft(String.format("循環信用餘額總計（不含負數餘額）：%s",commString.lpad(String.format("%,.2f", hTolDebtAcctSum4Txt), 13)), 92));
	tt.append(LINE_SEPERATOR);
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("註（１）：利害關係情形：１．本行員工　２．本行負責人　３．本行主要股東　４．與本行負責人或辦理授信職員有利害關係者　５．本行持有實收", 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft(comc.fixRight("資本額百分之三以上企業。", 34), 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("註（２）：動用循環方式：０：可動用循環信用　１：不可動用循環信用。", 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("註（３）：全行總循環信用餘額：指逾繳款截止日（約每月２０日）仍未繳納之金額，故本欄位於繳款截止日前並非循環信用餘額。每月２０日", 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft(comc.fixRight("列印之報表。", 16), 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("中，對於「全行總循環信用餘額」逾２０萬元者（備註欄位顯示*），應請其立即還款。若未能還款至２０萬元以下，經辦人員應將其卡片辦理暫時停", 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft(comc.fixRight("止使用。", 18), 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("註（４）：業績單位不同者，則歸屬於最近申辦之營業單位。", 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft(commString.rpad("=", 132 ,"="), 132));
	tt.append(LINE_SEPERATOR);
	tt.append(comc.fixLeft("製表單位：資訊部　　　　　　　 經辦　　　　　　　　　襄理　　　　　　　　　 副理　　　　　　　　　經理", 132));
	tt.append(LINE_SEPERATOR);
	tt.append(LINE_SEPERATOR);
	return tt.toString();
}

void printAdd(String buff) throws Exception {
   reportSeq++;
   lpar1.add(comcr.putReport(reportId, reportName,"", reportSeq, "0", buff));
}

 String ssFmt(String str){
	 String ss = "";
	  for(char c:str.toCharArray()){
		  if((int)c>57000 && (int)c<65281) {
			  ss += "  ";
			  continue;
		  }
		  ss += String.valueOf(c);
	  }
	  return ss;
 }
 
 int openFile1() throws Exception {
	 String fileName = FILE_NAME_RCRD87.replace("yyyymmdd", hBusinessDate);
	 String datFilePath = Paths.get(comc.getECSHOME(),FILE_PATH, fileName).toString();
     int isOpen= openBinaryOutput2(datFilePath);
     if (isOpen == -1) {
         showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
         return -1;
     }
     return isOpen;
 }
 
 int openFile2() throws Exception {
	 String fileName = FILE_NAME_RCRD87A.replace("yyyymmdd", hBusinessDate);
	 String datFilePath = Paths.get(comc.getECSHOME(),FILE_PATH, fileName).toString();
     int isOpen= openBinaryOutput2(datFilePath);
     if (isOpen == -1) {
         showLogMessage("E", "", String.format("此路徑或檔案不存在[%s]", datFilePath));
         return -1;
     }
     return isOpen;
 }
 
 void writeTxt(int fo,StringBuffer sb) throws Exception {
     byte[] tmpBytes = sb.toString().getBytes("MS950");
     writeBinFile2(fo,tmpBytes, tmpBytes.length);
 }
 
 void procFTP(String datFileName , String datFileName2) throws Exception {
	 String fileName = datFileName.replace("yyyymmdd", hBusinessDate);
	 String fileName2 = datFileName2.replace("yyyymmdd", hBusinessDate);
	 String fileFolder = Paths.get(comc.getECSHOME(),FILE_PATH).toString();
     CommFTP commFTP = new CommFTP(getDBconnect(), getDBalias());
     CommRoutine commRoutine = new CommRoutine(getDBconnect(), getDBalias());

     commFTP.hEflgTransSeqno = commRoutine.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
     commFTP.hEflgSystemId = "BREPORT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
     commFTP.hEriaLocalDir = fileFolder;
     commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
     commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
     commFTP.hEflgModPgm = javaProgram;

     String ftpCommand = String.format("mput %s | mput %s ", fileName , fileName2);

     showLogMessage("I", "", String.format("開始執行FTP指令[%s]......", ftpCommand));
     int errCode = commFTP.ftplogName("BREPORT", ftpCommand);

     if (errCode != 0) {
         showLogMessage("I", "",
                 String.format("ERROR:執行FTP指令[%s]發生錯誤, errcode[%s]", ftpCommand, errCode));
     }
 }
 
 private void backup(String removeFileName) throws Exception {
	 String fileName = removeFileName.replace("yyyymmdd", hBusinessDate);
	 String tmpstr1 = Paths.get(comc.getECSHOME(),FILE_PATH, fileName).toString();
     String backupFilename = String.format("%s.%s", fileName,sysDate + sysTime);
     String tmpstr2 = Paths.get(comc.getECSHOME(),FILE_PATH,"/backup/",backupFilename).toString();

     if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
         showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
         return;
     }
     showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
 }

}
