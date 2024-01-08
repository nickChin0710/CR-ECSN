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

public class MktRt15 extends BaseBatch {
private String progname = "紅利點數線上折抵刷卡消費明細表  112/11/14 V1.00.00";
CommFunction comm = new CommFunction();
CommCrdRoutine comcr =null;

private int pageCnt = 0 ;
List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
int reportSeq = 0;
private String reportId = "MktRt15";
private String reportName = "紅利點數線上折抵刷卡消費明細表";
private int indexCnt = 0;

// ---------------------------------------------------------------------------
private String hTranDate = "";
private String hMajorIdNo = "";
private String hMajorCardNo = "";
private long hBegTranBp = 0;
private long hBegTranAmt = 0;
private long hSumBegTranBp = 0;
private long hSumBegTranAmt = 0;

//-----------------------------------------------------------------------------
private String hBusinessDate = "";

//=****************************************************************************
public static void main(String[] args) {
	MktRt15 proc = new MktRt15();
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
	sqlCmd = " select a.tran_date , ";
    sqlCmd += " (SELECT cc.ID_NO FROM crd_idno cc WHERE cc.ID_P_SEQNO = a.ID_P_SEQNO ";
    sqlCmd += " FETCH FIRST ROW ONLY) as major_ID_NO , ";
    sqlCmd += " a.major_card_no , ";
    sqlCmd += " a.beg_tran_bp  , ";
    sqlCmd += " a.mod_time ";
    sqlCmd += " from mkt_bonus_dtl a ";
    sqlCmd += " where ";
    sqlCmd += " a.tran_pgm in ('BilO101','BilO102','BilO105') ";
    sqlCmd += " and a.acct_date = ? ";
    sqlCmd += " order by major_ID_NO , a.major_card_no  , a.mod_time ";
    setString(1,hBusinessDate);
	this.openCursor();
	while (this.fetchTable()) {
		resetData();
		totalCnt ++;
		hTranDate = getValue("tran_date");//-- 交易日期
		hMajorIdNo = getValue("major_id_no");//-- 正卡人ID
		hMajorCardNo = getValue("major_card_no");//-- 信用卡號
		hBegTranBp = getValueLong("beg_tran_bp");//-- 折抵紅利點數
		hBegTranAmt = new BigDecimal(hBegTranBp).divide(BigDecimal.valueOf(1000))
				.multiply(BigDecimal.valueOf(60)).setScale(0, BigDecimal.ROUND_DOWN).longValue();//-- 點數兌換刷卡金 (計算 a.beg_tran_bp /1000 * 60 取整數  )

		hSumBegTranBp += hBegTranBp;
		hSumBegTranAmt += hBegTranAmt;
		
		if (indexCnt == 0)
			printHeader();

		indexCnt++;

		printDetail();

		if (indexCnt > 55) {
			indexCnt = 0;
			printFooter(false);
		}
		
		if(totalCnt % 5000 == 0) {
			printf("已處理[" + totalCnt + "]筆");
		}
	}
	closeCursor();
	if(indexCnt > 0)
		printFooter(true);
	
}

void resetData() {
	hTranDate = "";
	hMajorIdNo = "";
	hMajorCardNo = "";
	hBegTranBp = 0;
	hBegTranAmt = 0;
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
	commString.insert(tt, "報表編號:CRD120C", 1);
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
	commString.insert(tt, "交易日期", 1);
	commString.insert(tt, "正卡人ID", 31);
	commString.insert(tt, "信用卡號", 61);
	commString.insert(tt, "折抵點數", 91);
	printAdd(tt.toString());

	tt =new StringBuffer();
	commString.insert(tt, commString.rpad("=", 136 ,"="), 1);
	printAdd(tt.toString());
}

void printDetail() throws Exception {
	indexCnt++;
	StringBuffer tt = new StringBuffer();
	commString.insert(tt, hTranDate, 1);
	commString.insert(tt, hMajorIdNo, 31);
	commString.insert(tt, hMajorCardNo, 61);
	commString.insert(tt, String.format("%,d", hBegTranBp), 91);
	printAdd(tt.toString());
}

void printFooter(boolean printFlag) throws Exception {
	StringBuffer tt = new StringBuffer();
	commString.insert(tt, commString.rpad("=", 136 ,"="), 1);
	printAdd(tt.toString());
	if(printFlag) {
		tt = new StringBuffer();
		commString.insert(tt, "小計", 1);
		commString.insert(tt, String.format("%,d", hSumBegTranBp), 91);
		commString.insert(tt, String.format("%,d", hSumBegTranAmt), 121);
		printAdd(tt.toString());
		tt = new StringBuffer();
		commString.insert(tt, "說明:", 1);
		commString.insert(tt, "每60點折抵1元。", 6);
		printAdd(tt.toString());
		tt = new StringBuffer();
		commString.insert(tt, "製表單位:資訊部", 1);
		commString.insert(tt, "經辦:", 30);
		commString.insert(tt, "核章:", 65);
		commString.insert(tt, "Form:MRGS21", 119);
		printAdd(tt.toString());
		tt = new StringBuffer();
		commString.insert(tt, " ", 1);
		printAdd(tt.toString());
	}
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
