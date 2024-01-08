/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/07/19  V1.00.01    JeffKung  program initial                           *
*  112/10/19  V1.00.02    Kirin     where add promote_dept                    *
******************************************************************************/

package Mkt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

/*產生花蓮第二信用合作社聯名卡企業回饋金明細表*/
public class MktRM18J extends AccessDAO {
    private String progname = "產生花蓮第二信用合作社聯名卡企業回饋金明細表程式  112/10/19  V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    String prgmId = "MktRM18J";
    String prgmName = "花蓮第二信用合作社聯名卡企業回饋金明細表";
    
    String rptNameM18J = "花蓮第二信用合作社聯名卡企業回饋金明細表";
    String rptIdM18J = "CRM18J";
    int rptSeqM18J = 0;
    int pageCntM18J = 0;
    List<Map<String, Object>> lparM18J = new ArrayList<Map<String, Object>>();
    
    String buf = "";
    String szTmp = "";
    String stderr = "";
    String hCallBatchSeqno = "";

    String hIdnoChiName = "";
    String hPrintName = "";
    String hRptName = "";
    String hBusinssDate = "";
    
    CommDate commDate = new CommDate();
    String dataMonth = "";
    String hBusDateTw = "";
    String hBusDateTwYear = "";
    String hBusDateMonth = "";
    String hBusDateDay = "";
    String sysTwDate = StringUtils.leftPad(commDate.twDate(), 7, "0");

    int totalCnt = 0;
    int realCnt = 0;
    
    double totalAmt = 0;
    double totalFeedbackAmt = 0;
    double deptAmt = 0;
    double deptFeedbackAmt = 0;

    int lineCnt = 0;
    
    Map<String, String> hm = null;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();
            
            showLogMessage("I", "", "營業日期=[" + hBusinssDate + "]");
            
            if (args.length == 1 && args[0].length() == 8) {
            	hBusinssDate = args[0];
            } else {
            	if (!"02".equals(comc.getSubString(hBusinssDate,6))) {
            		showLogMessage("I", "", "每月2日執行, 本日非執行日!!");
            		return 0;
            	}
            }
            
            dataMonth = comm.lastMonth(hBusinssDate, 1);
            showLogMessage("I", "", "資料年月=[" + dataMonth + "]");
            
            //轉換民國年月日
            hBusDateTw = StringUtils.leftPad(commDate.toTwDate(hBusinssDate), 7, "0");
            hBusDateTwYear = hBusDateTw.substring(0, hBusDateTw.length() - 4);
            hBusDateMonth = hBusDateTw.substring(hBusDateTw.length() - 4).substring(0, 2);
            hBusDateDay = hBusDateTw.substring(hBusDateTw.length() - 2);

            showLogMessage("I", "", "程式開始執行......");

    		selectBilFiscdtl();

            showLogMessage("I", "", "程式執行結束,筆數=[" + totalCnt + "],符合消費筆數:["+ realCnt+"]");

            commitDataBase();
            
            // ==============================================
            // 固定要做的
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void commonRtn() throws Exception {
        hBusinssDate = "";
        sqlCmd = "select business_date ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", "");
        }
        if (recordCnt > 0) {
            hBusinssDate = getValue("business_date");
        }
    }

    /***********************************************************************/
    void selectBilFiscdtl() throws Exception {

    	sqlCmd =  "select b.ecs_platform_kind, b.ecs_sign_code,b.source_curr,b.source_amt, ";
    	sqlCmd += "       round(b.dest_amt) as dest_amt, b.dest_curr, c.card_no,";
    	sqlCmd += "       c.group_code,c.promote_dept,b.mcc_code,b.mcht_chi_name,mcht_eng_name ";
		sqlCmd += "from bil_fiscdtl b ";
		sqlCmd += "join crd_card c on b.ecs_real_card_no = c.card_no ";
		sqlCmd += "               and c.group_code = '1688' ";  //花蓮二信聯名卡
		sqlCmd += "where b.ecs_tx_code in ('05','06','25','26') ";
		sqlCmd += " and  b.batch_date like ? "; 
		sqlCmd += " and  substr(b.mcc_code,1,1) <> '9' ";   
		sqlCmd += " and length(promote_dept)=7  ";   //花蓮二信分社7碼 
		sqlCmd += "order by promote_dept,card_no ";
		
		setString(1,dataMonth+"%");

		String keepPromoteDept = "XXXX";
		String chkTxnType = "";
		String acctNo = "";
        String[] arraySkipCode= new String[] 
        		{"f1","G1","G2","d1","M1","b1","e1","V1","V2",
        		 "V3","V4","V5","V6","FL","CL","10","11","12",
        		 "13","14","20","21","22","23","24","25"};

		int cursorIndex = openCursor();
		while (fetchTable(cursorIndex)) {

            totalCnt++;
            
            if (totalCnt % 5000 == 0 ) {
                showLogMessage("I", "", "Current Process record=" + totalCnt);
            }

            chkTxnType = getValue("ecs_platform_kind");
            //排除交易踢除
			if (ArrayUtils.contains(arraySkipCode, chkTxnType)) {
				continue;
			}
			
            if (keepPromoteDept.equals(getValue("promote_dept")) == false) {
            	if (realCnt == 0) {
            		acctNo = getAcctNo();
            		printHeaderM18J();
            	} else {
            		printDetailM18J(keepPromoteDept,acctNo);
            	}
                keepPromoteDept = getValue("promote_dept");
                deptAmt = 0;
            }

            if ("-".equals(getValue("ecs_sign_code"))) {
            	deptAmt = deptAmt - getValueDouble("dest_amt");
            } else {
            	deptAmt = deptAmt + getValueDouble("dest_amt");
            }
            
        	realCnt++;
           
        }

        if (realCnt != 0) {
    		printDetailM18J(keepPromoteDept,acctNo);
        	printFooterM18J();
        }
            
    }

    /**
     * @throws Exception 
     * @throws UnsupportedEncodingException *********************************************************************/
    void printHeaderM18J() throws UnsupportedEncodingException, Exception {
    	pageCntM18J++;
        
        buf = "";
        buf = comcr.insertStr(buf, "分行代號:  3144信用卡部" ,  1);
        buf = comcr.insertStr(buf, ""              + rptNameM18J             , 35);
        buf = comcr.insertStr(buf, "保存年限: 二年"                             ,97);
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));
        
        buf = "";
        String strDate = String.format("%3.3s年%2.2s月%2.2s日", hBusDateTwYear,hBusDateMonth, hBusDateDay);
        buf = comcr.insertStr(buf, "報表代號: CRM18J     科目代號:"            ,  1);
        buf = comcr.insertStr(buf, "中華民國 " + strDate                      , 46);
        buf = comcr.insertStr(buf, "頁    次:" + String.format("%4d", pageCntM18J) ,97);
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));

        buf = "";
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));
        
        buf = "";
        buf = comcr.insertStr(buf, "花蓮二信帳戶             消費總額             0.2% 回饋金       分社代號        分社中文名稱", 1);
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));

        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "=";
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));

    }

    /**
     * @throws Exception *********************************************************************/
    void printFooterM18J() throws Exception {
    	
        buf = "";
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));

        buf = "";
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));

        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft("小計:", 13));    
        sb.append(comc.fixLeft("", 4));
        szTmp = comcr.commFormat("3z,3z,3z,3z", totalAmt);
        sb.append(comc.fixLeft(szTmp, 16));
        sb.append(comc.fixLeft(" ", 11));
        szTmp = comcr.commFormat("z,3z,3z,3z", totalFeedbackAmt);
        sb.append(comc.fixLeft(szTmp, 14));
        buf = sb.toString();
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));
        
        buf = "";
        for (int i = 0; i < 132; i++)
            buf += "-";
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));

        
        buf = "";
        buf = comcr.insertStr(buf, "說明：新增一般消費金額計算方式 :TXCode:40+42-41 惟需排除各類稅款、規費、學雜費及代收公共事業、富邦壽", 1);
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));

        buf = "";
        buf = comcr.insertStr(buf, "      保險專案及電子票證自動加值等所產生帳款。 ", 1);
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));
    
        comcr.insertPtrBatchRpt(lparM18J);

    }

    /***********************************************************************/
    void printDetailM18J(String keepPromoteDept,String acctNo) throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(comc.fixLeft(acctNo, 16));    
        sb.append(comc.fixLeft(" ", 1));
        szTmp = comcr.commFormat("3z,3z,3z,3z", deptAmt);
        sb.append(comc.fixLeft(szTmp, 16));
        sb.append(comc.fixLeft(" ", 11));
        long deptFeedbackAmt = Math.round(deptAmt * 0.002);
        szTmp = comcr.commFormat("z,3z,3z,3z", deptFeedbackAmt);
        sb.append(comc.fixLeft(szTmp, 14));
        sb.append(comc.fixLeft(" ", 6));
        sb.append(comc.fixLeft(keepPromoteDept, 7));
        sb.append(comc.fixLeft(" ", 9));
        String deptName = getDeptName(keepPromoteDept);
        sb.append(comc.fixLeft(deptName, 12));
        
        buf = sb.toString();
        lparM18J.add(comcr.putReport(rptIdM18J, rptNameM18J, sysDate, ++rptSeqM18J, "0", buf));

        totalAmt = totalAmt + deptAmt;
        totalFeedbackAmt = totalFeedbackAmt + deptFeedbackAmt ;

    }
    
    /**********************************************************************/
    String getDeptName(String keepPromoteDept) throws Exception {
    	String deptName = "無名社";
    	
        sqlCmd = "select member_name ";
        sqlCmd += "from mkt_member_dtl  ";
        sqlCmd += "where member_corp_no =  ";
        sqlCmd += "   nvl((select member_corp_no from ptr_group_code where group_code = ? ),'XXXXXXXX') ";
        sqlCmd += "and branch = ? ";
        setString(1, getValue("group_code"));
        setString(2, keepPromoteDept);
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	deptName = getValue("member_name");
        }
        
        return deptName;
    }
    
    /**********************************************************************/
    String getAcctNo() throws Exception {
    	String acctNo = "";
    	
        sqlCmd = "select acct_no ";
        sqlCmd += "from mkt_member  ";
        sqlCmd += "where member_corp_no =  ";
        sqlCmd += "   nvl((select member_corp_no from ptr_group_code where group_code = ? ),'XXXXXXXX') ";
        setString(1, getValue("group_code"));
        int tmpInt = selectTable();
        if (tmpInt > 0) {
        	acctNo = getValue("acct_no");
        }
        
        return acctNo;
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktRM18J proc = new MktRM18J();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
