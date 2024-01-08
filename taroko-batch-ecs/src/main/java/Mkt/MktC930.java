/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/07/06  V1.00.00   	Ryan     	program initial                            *
 *  112/07/11  V1.00.01    	Ryan     	調整 selectActChkautopay sql                 *
 *  112/07/26  V1.00.02    	Grace Huang MktCashbackDtl.fund_code='0076000001'   *
 *  112/12/04  V1.00.03 	Zuwei Su    errExit改為 show message & exit program  *  
 *                                                                             *
 ******************************************************************************/

package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class MktC930 extends AccessDAO {

    private String progname = "數存戶自動扣繳成功 回饋寫檔處理 112/07/11 V1.00.03";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate comDate = new CommDate();
    CommString comStr = new CommString();
    CommCrdRoutine comcr = null;

    String pSeqno = "";
    double sumTransactionAmt = 0;
    String idPSeqno = "";
    String acctType = ""; 
    
    String hModUser = "";
    String hModPgm = "";
    String hBusinessDate = "";
    String hBusinessMonth = "";
    String fundName = "";
    int totalCount = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================

            // 固定要做的

            if (!connectDataBase()) {
//                comc.errExit("connect DataBase error", "");
                showLogMessage("I", "", "connect DataBase error");
                return -1;
            }
            //暫remark, 因.44執行issue (20230731, grace, 未上版, 待8/11後觀察)
            /*
    		if (comm.isAppActive(javaProgram)) {
				showLogMessage("I", "", "本程式已有另依程式啟動中, 不執行..");
				return (0);
			}
			*/
            showLogMessage("I", "", "-- [7/31測試] remark 本程式已有另依程式啟動中.......");
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            hModUser = comc.commGetUserID();
            
            selectPtrBusinday();
            selectPtrPayment();

            String parmDate1 = "";
            if(args.length == 1) {
                if ( ! new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
                	exceptExit = 0;
                    showLogMessage("E", "", String.format("分析日期,日期格式[%s]錯誤", args[0]));
                    return 0;
                }
                parmDate1 = args[0];
                hBusinessDate = parmDate1;
                hBusinessMonth = comDate.monthAdd(parmDate1, -1);
            }
            
            showLogMessage("I", "", String.format("輸入參數日期[%s]", parmDate1));
            showLogMessage("I", "", String.format("本日營業日期[%s]", hBusinessDate));
            showLogMessage("I", "", String.format("本日營業前一個月日期[%s]", hBusinessMonth));

            selectActChkautopay();

            // ==============================================
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
    void selectActChkautopay() throws Exception {
        sqlCmd = "select a.p_seqno, a.dc_min_pay, sum(a.transaction_amt) as sum_transaction_amt, count(*) ";
        sqlCmd += " from act_chkautopay a, crd_card b";
        sqlCmd += " where a.id_p_seqno = b.id_p_seqno ";
        sqlCmd += " and substr(a.enter_acct_date,1,6) = ? ";//營業日年月之上個月
        sqlCmd += " and b.group_code in ('1616','1657','1655','1656','1693') ";//團體代號
        sqlCmd += " and b.current_code = '0' ";//正常卡
        sqlCmd += " and b.curr_code = '901' ";//台幣
        sqlCmd += " and a.is_digital_acno = 'Y' ";//數存戶註記
        sqlCmd += " and a.status_code <> '99' ";//99:表新增, 即transaction_amt=ori_transaction_amt
        sqlCmd += " and a.from_mark='01' ";//01.自動扣繳;  02:花農
        sqlCmd += " group by a.p_seqno,a.dc_min_pay ";
        sqlCmd += " having sum(a.transaction_amt) > a.dc_min_pay ";//表自動扣繳, 交易金額
        setString(1,hBusinessMonth);
        int cursorIndex = openCursor();
        while(fetchTable(cursorIndex)) {
        	initData();
            pSeqno = getValue("p_seqno");
          	sumTransactionAmt = getValueDouble("sum_transaction_amt");
          	selectActAcno();
        	insertMktCashbackDtl(pSeqno,sumTransactionAmt);
            totalCount++;
            if (totalCount % 2000 == 0) {
                commitDataBase();
                showLogMessage("I", "", String.format("   Processed [%d] Records", totalCount));
            }
        }
        closeCursor(cursorIndex);
    }
  
    
    void selectActAcno() throws Exception {
    	sqlCmd = "select id_p_seqno,acct_type from act_acno where p_seqno = ? ";
    	setString(1,pSeqno);
    	int n = selectTable();
    	if(n > 0) {
    		idPSeqno = getValue("id_p_seqno");
    		acctType = getValue("acct_type");
    	}
    }
    
    void selectPtrPayment() throws Exception {
    	sqlCmd = "select bill_desc from ptr_payment where payment_type = '0076' ";
    	int n = selectTable();
    	if(n > 0) {
    		fundName = getValue("bill_desc");
    	}
    }
    
    void insertMktCashbackDtl(String pSeqno,double sumTransactionAmt) throws Exception {
		setValue("TRAN_DATE",sysDate);
		setValue("TRAN_TIME",sysTime);
		//setValue("FUND_CODE","0076");	
		setValue("FUND_CODE","0076000001");		//需存在於ptr_fundp
		setValue("FUND_NAME",fundName);
		setValue("P_SEQNO",pSeqno);
		setValue("ID_P_SEQNO",idPSeqno);
		setValue("ACCT_TYPE",acctType);
		setValue("TRAN_CODE","1");
		setValue("TRAN_PGM",javaProgram);
		setValueLong("BEG_TRAN_AMT",5);
		setValueLong("END_TRAN_AMT",5);
		setValue("RES_S_MONTH","");
		setValueLong("RES_TRAN_AMT",0);
		setValueInt("RES_TOTAL_CNT",0);
		setValueInt("RES_TRAN_CNT",0);
		setValue("RES_UPD_DATE","");
		setValue("ACCT_MONTH",comStr.left(hBusinessDate, 6));
		setValue("EFFECT_E_DATE","");
		setValue("TRAN_SEQNO","");
		setValue("PROC_MONTH","");
		setValue("ACCT_DATE",hBusinessDate);
		setValue("MOD_DESC","數存戶自行扣");
		setValue("MOD_MEMO","mktC930");
		setValue("MOD_REASON","");
		setValue("CASE_LIST_FLAG","");
		setValue("EFFECT_FLAG","");
		setValue("REMOVE_DATE","");
		setValue("CRT_DATE",sysDate);
		setValue("CRT_USER",javaProgram);
		setValue("APR_DATE",sysDate);
		setValue("APR_USER",javaProgram);
		setValue("APR_FLAG","Y");
		setValue("MOD_USER",javaProgram);
		setValue("MOD_TIME",sysDate+sysTime);
		setValue("MOD_PGM",javaProgram);
		setValueInt("MOD_SEQNO",1);
    	daoTable = "MKT_CASHBACK_DTL";
	
    	try {
    		insertTable();
    	}catch(Exception ex) {
    	      showLogMessage("I", "", String.format("insert MKT_CASHBACK_DTL err ,errmsg = [%s]", ex.getMessage()));
    	}
    }
    
    public void selectPtrBusinday() throws Exception
    {
    	
     sqlCmd = "select business_date ,to_char(to_date(business_date,'yyyymmdd') - 1 months ,'yyyymm') as business_month ";
     sqlCmd += "from ptr_businday fetch first 1 row only ";
     
     selectTable();

     if ( notFound.equals("Y") )
        {
         showLogMessage("I","","select ptr_businday error!" );
         exitProgram(1);
        }
     hBusinessDate =  getValue("business_date");
     hBusinessMonth =  getValue("business_month");
    }
    
    void initData() {
		pSeqno = "";
		sumTransactionAmt = 0;
		idPSeqno = "";
		acctType = "";
    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC930 proc = new MktC930();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
