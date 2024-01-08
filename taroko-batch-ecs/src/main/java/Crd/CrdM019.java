/****************************************************************************
*                                                                           *
*                              MODIFICATION LOG                             *
*                                                                           *
*     DATE     Version    AUTHOR                       DESCRIPTION          *
*  ---------  --------- ----------- --------------------------------------  *
*  109/01/15  V1.00.00    Rou       initial(Reference CrdM017 program)      *
*  109/12/23  V1.00.01   shiyuqi       updated for project coding standard   *
*  112/07/11  V1.00.02   Wilson     update crd_return_pp條件增加退卡編號                         *
*  112/10/17  V1.00.03   Wilson     調整為判斷退卡日期                                                                            *
*  112/12/27  V1.00.04   Wilson     調整為每月最後一天執行                                                                     *
****************************************************************************/

package Crd;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

/*PP卡效期到期處理*/
public class CrdM019 extends AccessDAO {
    public static final boolean debugMode = false;

    private String progname = "貴賓卡退卡180天批次停卡程式    112/12/27  V1.00.04 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 1;
    String hTempUser = "";

    String prgmId = "CrdM019";
    String hModUser = "";
    String hModPgm = "";
    String hCallBatchSeqno = "";
    String iFileName = "";
    String iPostDate = "";

    String hYesterday = "";
    String hBusinessDate = "";
    String hCrdpPpCardNo = "";
    String hCrdpReturnSeqno = "";
    String hCrdpRowid = "";

    int totCnt = 0;
    int recordCnt = 0;
    String hBusiBusinessDate = "";
    String hEndDate = "";
    String hOppostMonth = "";
    String hLastDay = "";

    // ********************************************************

    public int mainProcess(String[] args) {
        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();
            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }
            
            hModUser = comc.commGetUserID();
            
            if (args.length > 2) {
                String err1 = "CrdD049 [date] [seq_no]\n";
                comcr.errRtn("CrdD049 [date] [seq_no] ", "", comcr.hCallBatchSeqno);
            }

            if (args.length > 0) {
                if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                }
            }
            
            getBusinessDay();

            showLogMessage("I", "", "執行日期 = [" + hBusiBusinessDate + "]");
            
            hLastDay = hEndDate;
            
            if (!hBusiBusinessDate.equals(hLastDay)) {
        		showLogMessage("E", "", "執行日期不為該月最後一天,不執行此程式");
        		return 0;
            }
            
            //Get 180 days ago
//    		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//    		Date dateNow = sdf.parse(sysDate);
//    		Date getDate = new Date();
//    		Calendar calendar = Calendar.getInstance(); 
//    		calendar.setTime(dateNow);
//    		calendar.add(Calendar.DATE, -180);
//    		getDate = calendar.getTime();		
//    		String h180daysAgo = sdf.format(getDate);
//    		
//    		showLogMessage("I", "", "前180天 = [" + h180daysAgo + "]");
            
            showLogMessage("I", "", "前180天 = [" + hOppostMonth + "]");

            sqlCmd = "select ";
            sqlCmd += " b.pp_card_no, ";
            sqlCmd += " b.current_code, ";
            sqlCmd += " b.issue_date, ";
            sqlCmd += " change_date, ";
            sqlCmd += " b.change_date, ";
            sqlCmd += " a.return_type, ";
            sqlCmd += " a.proc_status, ";
            sqlCmd += " a.return_seqno ";
            sqlCmd += " from crd_return_pp a, crd_card_pp b ";
            sqlCmd += " where a.pp_card_no = b.pp_card_no ";
            sqlCmd += " and a.return_date like ? ";
            sqlCmd += " and b.current_code = '0' ";  
            sqlCmd += " and a.proc_status in ('1', '2', '7')";
            sqlCmd += " order by b.issue_date ";
            
            setString(1, hOppostMonth + '%');

            recordCnt = selectTable();
            for (int i = 0; i < recordCnt; i++) {
                hCrdpPpCardNo = getValue("pp_card_no", i);
                hCrdpReturnSeqno = getValue("return_seqno", i);
//                h_crdp_rowid = getValue("rowid", i);                
                totCnt++;
                showLogMessage("I", "", "Read Card no = [ " + hCrdpPpCardNo + " ]" + totCnt);
                
                updateCrdCardPp();
                updateCrdReturnPp();
            }

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    /***********************************************************************/
    void getBusinessDay() throws Exception {

    	sqlCmd = "select to_char(sysdate,'yyyymmdd') as business_date ";
        sqlCmd += " from ptr_businday ";
        recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? getValue("business_date")
                                 : hBusiBusinessDate;
        }
        
        sqlCmd = "select to_char(last_day(to_date(?,'yyyymmdd')),'yyyymmdd') h_end_date ";
        sqlCmd += "    , to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm') h_oppost_month ";
        sqlCmd += " from dual ";
        setString(1, hBusiBusinessDate);
        setString(2, hBusiBusinessDate);

        recordCnt = selectTable();
        if(recordCnt > 0) {
           hEndDate = getValue("h_end_date");  
           hOppostMonth = getValue("h_oppost_month");           
        }
    }

    /***********************************************************************/
    void updateCrdCardPp() throws Exception {
        daoTable   = "crd_card_pp ";
        updateSQL  = "current_code  	= '1', "; /* 一般申停 */
        updateSQL += "oppost_reason 	= 'ED', ";
        updateSQL += "oppost_date   	= ?, ";
        updateSQL += "mod_user      	= ?, ";
        updateSQL += "mod_time      	= sysdate, ";
        updateSQL += "mod_pgm       	= ? ";
        whereStr   = "where pp_card_no  = ? "; 
        setString(1, sysDate);
        setString(2, hModUser);
        setString(3, prgmId);
        setString(4, hCrdpPpCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_crd_card_pp  not found!", "", comcr.hCallBatchSeqno);
        }

        return;
    }
    /***********************************************************************/
    void updateCrdReturnPp() throws Exception {
        daoTable   = "crd_return_pp ";
        updateSQL += "proc_status   	= '4', ";
        updateSQL += "mod_user      	= 'batch', ";
        updateSQL += "mod_time      	= TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'), ";
        updateSQL += "mod_pgm       	= 'CrdM019' ";
        whereStr   = "where pp_card_no  = ? ";
        whereStr  += "and return_seqno = ? ";
        whereStr  += "and proc_status  in ('1', '2', '7')";
        setString(1, sysDate + sysTime);
        setString(2, hCrdpPpCardNo);
        setString(3, hCrdpReturnSeqno);
        updateTable();

        return;
    }
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CrdM019 proc = new CrdM019();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
