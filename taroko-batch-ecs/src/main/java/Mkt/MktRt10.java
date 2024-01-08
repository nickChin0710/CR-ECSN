package Mkt;
/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/11/14  V1.00.00    ryan     program initial                            *
 ******************************************************************************/

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.CommCrdRoutine;
import com.CommFunction;
import com.BaseBatch;

public class MktRt10 extends BaseBatch {
private String progname = "紅利點數兌換刷卡金明細表  112/11/14 V1.00.00";
CommFunction comm = new CommFunction();
CommCrdRoutine comcr =null;

private int pageCnt = 0 ;
List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
int reportSeq = 0;
private String reportId = "MktRt10";
private String reportName = "紅利點數兌換刷卡金明細表";
private int indexCnt = 0;

// ---------------------------------------------------------------------------
private String hMajorIdNo = "";
private String hTranDate = "";
private String hAccountingDate = "";
private long hBegTranBp = 0;
private long hBegTranAmt = 0;
private long hSumBegTranBp = 0;
private long hSumBegTranAmt = 0;
private String hComment = "";


//-----------------------------------------------------------------------------
private String hBusinessDate = "";

//=****************************************************************************
public static void main(String[] args) {
	MktRt10 proc = new MktRt10();
	proc.mainProcess(args);
	proc.systemExit();
}

// ---------------------------------------------------------------------------
@Override
protected void dataProcess(String[] args) throws Exception {
	dspProgram(progname);


	dbConnect();
	comcr =new com.CommCrdRoutine(getDBconnect(), getDBalias());

	hBusinessDate = comcr.getBusiDate();
	String parm1 = "";
	if (args.length == 1) {
		parm1 = args[0];
		if (parm1.length() != 8 || !commDate.isDate(parm1)) {
			showLogMessage("I", "", "請傳入參數合格值yyyymmdd[" + parm1 + "]");
			rc = 1;
			systemExit();
		}
		hBusinessDate = parm1;
	}

	showLogMessage("I", "", "執行日期 : [" + hBusinessDate + "]");
	selectBonusDtl();
	
	if (lpar1.size() == 0) {
		printHeader();
		printFooter(true);
	}
	if(lpar1.size()>0){
		comcr.insertPtrBatchRpt(lpar1);
	}
	lpar1.clear();
	
	sqlCommit(1);
	endProgram();
}


void selectBonusDtl() throws Exception {
	sqlCmd = " select * from ( ";
    sqlCmd += " select ( SELECT cc.ID_NO FROM crd_idno cc WHERE cc.ID_P_SEQNO = a.ID_P_SEQNO FETCH FIRST 1 ROW ONLY) as major_id_no , ";
    sqlCmd += " a.tran_date, ";
    sqlCmd += " '' as accounting_date , ";
    sqlCmd += " a.beg_tran_bp  , ";
    sqlCmd += " '' as comment , ";//--備註
    sqlCmd += " a.mod_time ";
    sqlCmd += " from mkt_bonus_dtl a ";
    sqlCmd += " where a.tran_pgm in ('TCSRDA40','ECSCDA40','mktp0270') ";
    sqlCmd += " and a.acct_date = ? ";
    sqlCmd += " union all ";
    sqlCmd += " select (SELECT cc.ID_NO FROM dbc_idno cc WHERE cc.ID_P_SEQNO = a.ID_P_SEQNO FETCH FIRST 1 ROW ONLY) as major_id_no , ";
    sqlCmd += " a.tran_date , ";
    sqlCmd += " '' as accounting_date , ";
    sqlCmd += " a.beg_tran_bp  , ";
    sqlCmd += " 'visa debit card ' as comment , ";
    sqlCmd += " a.mod_time ";
    sqlCmd += " from dbm_bonus_dtl a ";
    sqlCmd += " where ";
    sqlCmd += " a.tran_pgm in ('TCSRDA40','ECSCDA40','mktp0270') ";
    sqlCmd += " and a.acct_date = ? ";
    sqlCmd += " ) aa order by major_ID_NO , mod_time ";
    setString(1,hBusinessDate);
    setString(2,hBusinessDate);
	this.openCursor();
	while (this.fetchTable()) {
		resetData();
		totalCnt ++;
		hMajorIdNo = getValue("major_id_no");//-- 正卡人ID
		hTranDate = getValue("tran_date");//-- 交易日期
		hAccountingDate = getValue("accounting_date");//-- 入帳日期
		hBegTranBp = getValueLong("beg_tran_bp");//-- 折抵紅利點數
		hBegTranAmt = new BigDecimal(hBegTranBp).divide(BigDecimal.valueOf(1000))
				.multiply(BigDecimal.valueOf(60)).setScale(0, BigDecimal.ROUND_DOWN).longValue();//-- 點數兌換刷卡金 (計算 a.beg_tran_bp /1000 * 60 取整數  )
		hComment = getValue("comment");//備註

		hSumBegTranBp += hBegTranBp;
		hSumBegTranAmt += hBegTranAmt;
		
		if (indexCnt == 0)
			printHeader();

		indexCnt++;

		printDetail();

		if (indexCnt > 39) {
			indexCnt = 0;
			printFooter(false);
		}
		
		if(totalCnt % 5000 == 0) {
			printf("已處理[" + totalCnt + "]筆");
		}
	}
	closeCursor();
	if(indexCnt > 0 )
		printFooter(true);
	
}

void resetData() {
	hMajorIdNo = "";
	hTranDate = "";
	hAccountingDate = "";
	hBegTranBp = 0;
	hBegTranAmt = 0;
	hComment = "";
}

// ---------------------------------------------------------------------------
void printHeader() throws Exception {
	pageCnt++;
	StringBuffer tt = new StringBuffer();
	commString.insertCenter(tt, "合作金庫商業銀行", 130);
	printAdd(tt.toString());
		
	tt =new StringBuffer();
	commString.insert(tt, "分行代號 : 3144 信用卡部", 1);
	commString.insert(tt, reportName, 54);
	commString.insert(tt, "保存年限:五年", 119);
	printAdd(tt.toString());	

	tt =new StringBuffer();
	commString.insert(tt, "報表編號:CRD120", 1);
	commString.insert(tt, "科目代號 :", 21);
	commString.insert(tt, getTwYMD(hBusinessDate), 54);
	commString.insert(tt, hBusinessDate+sysTime, 92);
	commString.insert(tt, "頁    次:", 119);	
	commString.insert(tt, commString.int2Str(pageCnt), 128);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, "正卡人ID", 1);
	commString.insert(tt, "交易日期", 21);
	commString.insert(tt, "入帳日期", 41);
	commString.insert(tt, "折抵紅利點數", 61);
	commString.insert(tt, "點數兌換刷卡金", 81);
	commString.insert(tt, "備註", 101);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, commString.rpad("=", 136 ,"="), 1);
	printAdd(tt.toString());
}

void printDetail() throws Exception {
	indexCnt++;
	StringBuffer tt = new StringBuffer();
	commString.insert(tt, hMajorIdNo, 1);
	commString.insert(tt, hTranDate, 21);
	commString.insert(tt, hAccountingDate, 41);
	commString.insert(tt, String.format("%,d", hBegTranBp), 61);
	commString.insert(tt, String.format("%,d", hBegTranAmt), 81);	
	commString.insert(tt, hComment, 101);
	printAdd(tt.toString());
}

void printFooter(boolean printFlag) throws Exception {
	StringBuffer tt = new StringBuffer();
	commString.insert(tt, commString.rpad("=", 136 ,"="), 1);
	printAdd(tt.toString());
	if(printFlag) {
		tt =new StringBuffer();
		commString.insert(tt, "小計", 1);
		commString.insert(tt, String.format("%,d", hSumBegTranBp), 61);
		commString.insert(tt, String.format("%,d", hSumBegTranAmt), 81);
		printAdd(tt.toString());
	}
	tt =new StringBuffer();
	commString.insert(tt, "說明:", 1);
	commString.insert(tt, "1.當期帳單無帳款者,則兌換刷卡金額將保留至次期帳單有消費款項時進行折抵。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "2.折抵點數須以千點為單位,每1000點折抵新台幣60元。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "3.點數一經折抵則不能取消或要求退還現金。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "4.點數兌換刷卡金每月結帳日才進行帳單折抵入帳。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "5.紅利點數兌換刷卡金, 採ID歸戶兌換 ,  信用卡+Visa Debit 金融卡 折抵點數為千點倍數。", 6);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, "製表單位:資訊部", 1);
	commString.insert(tt, "經辦:", 30);
	commString.insert(tt, "核章:", 65);
	commString.insert(tt, "Form:MRGS21", 119);
	printAdd(tt.toString());
	tt =new StringBuffer();
	commString.insert(tt, " ", 1);
	printAdd(tt.toString());
}

void printAdd(String buff) throws Exception {
   reportSeq++;
   lpar1.add(comcr.putReport(reportId, reportName,"", reportSeq, "0", buff));
}

 String getTwYMD(String date) throws ParseException {
	 SimpleDateFormat df1 = new SimpleDateFormat("yyyymmdd");
	 SimpleDateFormat df2 = new SimpleDateFormat("中華民國 yyy年 mm月 dd日");
	 Calendar cal = Calendar.getInstance();
	 cal.setTime(df1.parse(date));
	 cal.add(Calendar.YEAR, -1911);
	 return df2.format(cal.getTime());
 }

}
