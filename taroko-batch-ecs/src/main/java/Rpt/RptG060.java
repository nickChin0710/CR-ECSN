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
 ******************************************************************************/

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;
import com.BaseBatch;

public class RptG060 extends BaseBatch {
private String progname = "定期檢視及調整持卡人利率異動表(CRM73)  112/08/18 V1.00.02";
CommFunction comm = new CommFunction();
CommCrdRoutine comcr =null;
CommString comms = new CommString();

private int pageCnt = 0 ;
List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
int reportSeq = 0;
private String reportId = "RptG060";
private String reportName = "定期檢視及調整持卡人利率異動表(CRM73)";
private int indexCnt = 0;

// ---------------------------------------------------------------------------
private String hIdNo = "";
private String hChiName = "";
private double hRcRateAfter = 0;
private double hRcRateBefroe = 0;
private double hRcRateOri = 0;
private String hUpDown = "";
private String hUpDownFlag = "";

//-----------------------------------------------------------------------------
private String hAcctMonth = "";

//=****************************************************************************
public static void main(String[] args) {
	RptG060 proc = new RptG060();
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

	hAcctMonth = getAcctMonth();
	
	String parm1 = "";
	if (args.length == 1) {
		parm1 = args[0];
		if (parm1.length() != 6 || !commDate.isDate(parm1+"01")) {
			showLogMessage("I", "", "請傳入參數合格值yyyymm[" + parm1 + "]");
			rc = 1;
			systemExit();
		}
		hAcctMonth = parm1;
	}
	
	showLogMessage("I", "", "執行日期 : [" + hAcctMonth + "]");
	loadPtrRcrate();
	selectCrdCorrelate();
	
	if (lpar1.size() == 0) {
		printHeader();
	}
	if(lpar1.size()>0){
		comcr.insertPtrBatchRpt(lpar1);
	}
	lpar1.clear();
	
	sqlCommit(1);
	endProgram();
}


void selectCrdCorrelate() throws Exception {
	sqlCmd = " select c.id_no,c.chi_name,a.rc_rate_after,a.rc_rate_befroe,a.rc_rate_ori,a.up_down,a.up_down_flag ";
	sqlCmd += " from cyc_diff_rcrate a left join act_acno b on a.ACNO_P_SEQNO = b.ACNO_P_SEQNO ";
	sqlCmd += " left join crd_idno c on b.id_p_seqno = c.id_p_seqno ";
	sqlCmd += " where a.is_conform = 'Y' and a.acct_month = ? ";
	sqlCmd += " order by a.acno_p_seqno ";
	setString(1,hAcctMonth);
	this.openCursor();
	while (this.fetchTable()) {
		resetData();
		totalCnt ++;
		hIdNo = getValue("id_no");//ID
		hChiName = getValue("chi_name");//姓名
		hRcRateAfter = getValueDouble("rc_rate_after");//異動前利率
		hRcRateBefroe = getValueDouble("rc_rate_befroe");//異動後利率
		hRcRateOri = getValueLong("rc_rate_ori");//原利率
		hUpDown = getValue("up_down");//類別
		hUpDownFlag = getValue("up_down_flag");//註記

		if (indexCnt == 0)
			printHeader();

		indexCnt++;

		printDetail();
	
		if (indexCnt > 56) {
			indexCnt = 0;
		}
		
		if(totalCnt % 5000 == 0) {
			printf("已處理[" + totalCnt + "]筆");
		}
	}
	printFooter();
	closeCursor();
}

void resetData() {
	hIdNo = "";
	hChiName = "";
	hRcRateAfter = 0;
	hRcRateBefroe = 0;
	hRcRateOri = 0;
	hUpDown = "";
	hUpDownFlag = "";

}

String getAcctMonth() throws Exception {
	sqlCmd = "select max(ACCT_MONTH) as max_acct_month from CYC_DIFF_RCRATE where IS_CONFORM = 'Y' ";
	selectTable();
	return getValue("max_acct_month");
}

void loadPtrRcrate() throws Exception {
	extendField = "rcrate.";
	daoTable = "ptr_rcrate";
	sqlCmd = "SELECT rcrate_year,rcrate_day ";
	sqlCmd += "FROM ptr_rcrate ";
	int cnt = loadTable();
	setLoadData("rcrate.rcrate_day");
	showLogMessage("I", "", "loadPtrRcrate cnt : [" + cnt + "]");
}


// ---------------------------------------------------------------------------
void printHeader() throws Exception {
	pageCnt++;
	StringBuffer tt = new StringBuffer();
	commString.insertCenter(tt, "合作金庫商業銀行", 130);
	printAdd(tt.toString());
		
	tt =new StringBuffer();
	commString.insertCenter(tt, reportName, 130);
	printAdd(tt.toString());	

	tt =new StringBuffer();
	commString.insert(tt, "報表編號:RptG060", 1);
	commString.insert(tt, "頁    次:", 119);	
	commString.insert(tt, commString.int2Str(pageCnt), 128);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, "正卡人持卡人ID", 1);
	commString.insert(tt, "正卡人姓名", 21);
	commString.insert(tt, "異動前利率", 51);
	commString.insert(tt, "異動後利率", 71);
	commString.insert(tt, "原利率", 91);
	commString.insert(tt, "類別", 111);
	commString.insert(tt, "註記", 131);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, commString.rpad("=", 136 ,"="), 1);
	printAdd(tt.toString());
}

void printDetail() throws Exception {
	indexCnt++;
	setValue("rcrate.rcrate_day",String.format("%.3f", hRcRateAfter));
	getLoadData("rcrate.rcrate_day");
	String rcrateYearAfter = getValue("rcrate.rcrate_year");

	setValue("rcrate.rcrate_day",String.format("%.3f", hRcRateBefroe));
	getLoadData("rcrate.rcrate_day");
	String rcrateYearBefroe = getValue("rcrate.rcrate_year");
	
	setValue("rcrate.rcrate_day",String.format("%.3f", hRcRateOri));
	getLoadData("rcrate.rcrate_day");
	String rcrateYearOri = getValue("rcrate.rcrate_year");

	StringBuffer tt = new StringBuffer();
	commString.insert(tt, hIdNo, 1);
	commString.insert(tt, ssFmt(hChiName), 21);
	commString.insert(tt, rcrateYearAfter, 51);
	commString.insert(tt, rcrateYearBefroe, 71);
	commString.insert(tt, rcrateYearOri, 91);	
	commString.insert(tt, comms.decode(hUpDown, ",U,D", ",1,2"), 111);	
	commString.insert(tt, hUpDownFlag, 131);
	printAdd(tt.toString());
}

void printFooter() throws Exception {
	StringBuffer tt = new StringBuffer();
	commString.insert(tt, commString.rpad("=", 136 ,"="), 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "備註 : ", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "1、目前本行信用卡循環信用利率共分十一級，一~十一級利率為4.15%、5.1%、5.4%、6.35%、8.1%、9.35%、11.1%、12.35%、13.1%、14.35%、14.75%。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "2、異動前利率:依信用卡行為人模型評等(全部全金融機構)及持卡年限權數，得到相應之利率級數，決定其適用之循環信用基準利率。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "3、異動後利率:係指採以「異動前利率」為基準利率，再依持卡人繳款情形辦理調升或調降利率後，作為最終適用之循環信用利率。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "4、原利率:係指審視當下之卡片原利率。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "類別:", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "1、最近3個月帳單有一期(含)以上有逾期繳款者，利率調升三級，最高不得高於14.75%。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "2、最近6個月帳單無逾期繳款紀錄者，利率調降一級，最低不得低於8.10%。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "註記:", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "信評結果與持卡人於本行繳款情形為反向相關時，處理方式如下:", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "A、屬類別1者，異動後利率<原利率時，採以原利率辦理。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "B、屬類別2者，異動後利率>原利率時，採以原利率辦理。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "異動前利率及原利率皆低於8.10%之處理方式:", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "C、屬類別2者，異動前利率<=原利率時，採以原利率辦理。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "D、屬類別2者，異動前利率>原利率時，異動後利率可低於8.10%", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "其他例外:", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "E、類別屬1者，異動前利率=14.75%且原利率<14.75%時，以原利率調升三級辦理。", 6);
	printAdd(tt.toString());
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

}
