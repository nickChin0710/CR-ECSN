/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/05/24  V1.00.00    Ryan     program initial                           *
 *  112/06/12  V1.00.01    Ryan     有輸入分析日期就不帶feedback_dd                  *
 *  112/10/16  V1.00.02    Zuwei Su 依mktm0850之 ‘分析日期’ 異動, 同步異動’當期帳單(年月)’                  *
 ******************************************************************************/

package Mkt;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommString;

public class MktC205 extends AccessDAO {

    private String progname = "通路活動-異動按月回饋之下次分析日期 112/10/16 V1.00.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate comDate = new CommDate();
    CommString comStr = new CommString();
    CommCrdRoutine comcr = null;

    String hModUser = "";
    String hCallBatchSeqno = "";
    String hModPgm = "";
    String hCallRProgramCode = "";

    
    /********TABLE:mkt_channel_parm*************/
    String hActiveCode = "";
    String hMyttModUser = "";
    String hMyttModPgm = "";
    String hBusinessDate = "";
    String parmDate1 = "";
    String tmpstr = "";
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
                comc.errExit("connect DataBase error", "");
            }

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = hCallBatchSeqno;
            comcr.hCallRProgramCode = javaProgram;

            comcr.callbatch(0, 0, 0);

            hModUser = comc.commGetUserID();
            hMyttModUser = hModUser;
            hMyttModPgm = javaProgram;
            
            selectPtrBusinday();
            
            if(args.length >= 1) {
            	if(new CommFunction().checkDateFormat(args[0], "yyyyMMdd")) {
            		 parmDate1 = args[0];
            	}else {
            		 hActiveCode = args[0];
            	}
            }
            if(args.length == 2) {
                if ( ! new CommFunction().checkDateFormat(args[1], "yyyyMMdd")) {
                	exceptExit = 0;
                    showLogMessage("E", "", String.format("分析日期,日期格式[%s]錯誤", args[0]));
                    return 0;
                }
                parmDate1 = args[1];
            }
            
            showLogMessage("I", "", String.format("本日營業日期[%s]", hBusinessDate));
            showLogMessage("I", "", String.format("參數活動代號[%s]", hActiveCode));
            showLogMessage("I", "", String.format("參數分析日期[%s]", parmDate1));

            selectMktChannelParm();

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
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
    void selectMktChannelParm() throws Exception {
    	int index = 1;
        sqlCmd = "select ";
        sqlCmd += " active_code,cal_def_date,feedback_dd,accumulate_term_sel ";
        sqlCmd += " from mkt_channel_parm ";
        sqlCmd += "  where feedback_cycle = 'M' ";
        if(comStr.empty(parmDate1)) {
        	  sqlCmd += "  and feedback_dd = ? ";
              setString(index++, comStr.right(hBusinessDate, 2));
        }
        if(!comStr.empty(hActiveCode)) {
        	sqlCmd += "  and active_code = ? ";  
        	setString(index++, hActiveCode);
        }
        int cursorIndex = openCursor();
        while(fetchTable(cursorIndex)) {
        	initData();
        	String activeCode = getValue("active_code");
          	String calDefDate = getValue("cal_def_date");
        	String feedbackDd = getValue("feedback_dd");
        	String accumulateTermSel = getValue("accumulate_term_sel");
        	updateMktChannelParm(activeCode,calDefDate,feedbackDd,accumulateTermSel);
            totalCount++;
            if (totalCount % 1000 == 0) {
                commitDataBase();
                showLogMessage("I", "", String.format("   Processed [%d] Records", totalCount));
            }
        }
        closeCursor(cursorIndex);
    }
  
    
    void updateMktChannelParm(String activeCode,String calDefDate ,String feedbackDd,String accumulateTermSel) throws Exception {
    	String businessDateNext1 = comDate.monthAdd(hBusinessDate,1)+feedbackDd;
		daoTable = "mkt_channel_parm";
		updateSQL = " cal_def_date =  ? ";
        updateSQL += " ,acct_month =  ? ";
		updateSQL += " ,mod_pgm =  ? ";
		updateSQL += " ,mod_time =  sysdate ";
		whereStr = "where active_code = ?  ";
		String newCalDefDate = !comStr.empty(parmDate1) ? parmDate1 : businessDateNext1;
		setString(1, newCalDefDate);
		// 判讀’消費累計基礎’欄位值=’2’ (當期帳單)時, 依 ’分析日期’ 欄位值, 取得其年月帶入’當期帳單(年月)’ 欄位;否則, 置入空白
		if ("2".equals(accumulateTermSel)) {
		    setString(2, newCalDefDate.substring(0, 6));
		} else {
		    setString(2, "");
		}
		setString(3, javaProgram);
		setString(4, activeCode);
		updateTable();

		if ("Y".equals(notFound)) {
			showLogMessage("I", "", String.format("update mkt_channel_parm not found, active_code[%s]", activeCode));
		}
    }
    
    public void selectPtrBusinday() throws Exception
    {
     daoTable  = "PTR_BUSINDAY";
     whereStr  = "FETCH FIRST 1 ROW ONLY";

     int recordCnt = selectTable();

     if ( notFound.equals("Y") )
        {
         showLogMessage("I","","select ptr_businday error!" );
         exitProgram(1);
        }
     hBusinessDate =  getValue("BUSINESS_DATE");
    }
    
    void initData() {

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        MktC205 proc = new MktC205();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
