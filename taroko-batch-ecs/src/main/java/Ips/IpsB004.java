/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  106/06/01  V1.00.00    Edson     program initial                           *
*  109-12-14  V1.00.01    tanwei      updated for project coding standard     *
*  112-05-23  V1.00.02    Alex      修改出一卡通拒絕代行名單條件                                                        *
******************************************************************************/

package Ips;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/*拒絕代行資料處理程式*/
public class IpsB004 extends AccessDAO {
    private String progname = "拒絕代行資料處理程式  112/05/23 V1.00.02";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommString commstring = new CommString();
    CommCrdRoutine comcr = null;

    String hCallBatchSeqno = "";

    String hBusiBusinessDate = "";
    String hB2i4CrtDate = "";
    String hB2i4CrtTime = "";
    String hIardIpsCardNo = "";
    String hIardCardNo = "";
    String hAcnoPSeqno = "";
    String hAcnoIdPSeqno = "";
    String hAcnoIntRateMcode = "";
    String hIsBlockReason = "";
    String hIardRowid = "";
    String hB2i4FromRsn = "";
    String hCardCurrentCode = "";
    String hCardSpecStatus = "";
    String hAcctSpecStatus = "";    
    int tempInt = 0;
    String hIpscMcodeCond = "";
    String hIpscPaymentRate = "";    
    String hIpscImpListCond = "";
    double tempDouble = 0;
    String newStopDate = "";
    int totCnt = 0;
    int insertCnt = 0;

    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            if (args.length != 0 && args.length != 1) {
                comc.errExit("Usage : IpsB004 [business_date]", "");
            }

            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            if (args.length == 1)
                if (args[0].length() == 8)
                    hBusiBusinessDate = args[0];

            selectPtrBusinday();
            int rtn = selectIpsCommParm();
            if (rtn == 0)
                selectIpsCard();

            showLogMessage("I", "", String.format("Process records = [%d][%d]\n", totCnt, insertCnt));

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
    void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = hBusiBusinessDate.length() == 0 ? sysDate : hBusiBusinessDate;
        sqlCmd = "select to_char(sysdate,'yyyymmdd') h_b2i4_crt_date,";
        sqlCmd += "to_char(sysdate,'hh24miss') h_b2i4_crt_time ";
        sqlCmd += " from ptr_businday  ";
        sqlCmd += "fetch first 1 rows only ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hB2i4CrtDate = getValue("h_b2i4_crt_date");
            hB2i4CrtTime = getValue("h_b2i4_crt_time");
        }

    }

    /***********************************************************************/
    int selectIpsCommParm() throws Exception {
        hIpscMcodeCond = "";
        hIpscPaymentRate = "";        
        hIpscImpListCond = "";
        sqlCmd = "select mcode_cond,";
        sqlCmd += "payment_rate,";        
        sqlCmd += "imp_list_cond ";
        sqlCmd += " from ips_comm_parm  ";
        sqlCmd += "where parm_type ='REJ_AUTH' ";
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hIpscMcodeCond    = getValue("mcode_cond");
            hIpscPaymentRate  = getValue("payment_rate");           
            hIpscImpListCond = getValue("imp_list_cond");
        } else {
            return 1403;
        }
        return 0;
    }

    /***********************************************************************/
    void selectIpsCard() throws Exception {
        int rtn = 0;

        updateIpsCard0();

        sqlCmd = "select ";
        sqlCmd += "a.ips_card_no,";
        sqlCmd += "a.card_no,";
        sqlCmd += "c.acno_p_seqno,";
        sqlCmd += "c.id_p_seqno,";        
        sqlCmd += "c.int_rate_mcode,";
        sqlCmd += "a.rowid as rowid, ";
        sqlCmd += "b.current_code  ";
        sqlCmd += "from act_acno c, crd_card b , ips_card a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "and a.standin_flag = 'Y' ";
        sqlCmd += "and a.no_standin_date = '' ";
        sqlCmd += "and a.blacklt_date  = '' ";
        sqlCmd += "and a.lock_date  = '' ";
        sqlCmd += "and a.return_date  = '' ";
        sqlCmd += "and a.autoload_clo_date = '' ";
        sqlCmd += "and a.new_end_date > ? ";
        sqlCmd += "and b.card_no  = a.card_no ";
        sqlCmd += "and c.acno_p_seqno  = b.acno_p_seqno ";
        setString(1, hBusiBusinessDate);
        openCursor();
        while (fetchTable()) {
            hIardIpsCardNo = getValue("ips_card_no");
            hIardCardNo = getValue("card_no");
            hAcnoPSeqno = getValue("acno_p_seqno");
            hAcnoIdPSeqno = getValue("id_p_seqno");
            hCardCurrentCode = getValue("current_code");
            hAcnoIntRateMcode = getValue("int_rate_mcode");
            hIardRowid = getValue("rowid");

            hIsBlockReason = "";
            hCardSpecStatus = "";
            hAcctSpecStatus = "";
            
            selectCcaCardAcct();
            selectCcaCardBase();

            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("crd Process record=[%d]\n", totCnt));

            //--無效卡送拒絕代行 , from_mark : 0
            if("0".equals(hCardCurrentCode) == false) {            	
            	hB2i4FromRsn = "0";
            	insertIpsB2i004Log();
                updateIpsCard();
                continue;
            }
            
            //--有凍結碼
            if(hIsBlockReason.isEmpty() == false) {
            	hB2i4FromRsn = "1";
            	insertIpsB2i004Log();
                updateIpsCard();
                continue;
            }
            
            //--有特指
            if(hAcctSpecStatus.isEmpty() == false || hCardSpecStatus.isEmpty() == false) {
            	hB2i4FromRsn = "2";
            	insertIpsB2i004Log();
                updateIpsCard();
                continue;
            }
            
            //--人工指定
            if (hIpscImpListCond.equals("Y")) {
                rtn = chkIpsCommData();/* 人工 */
                if (rtn > 0) {
                    hB2i4FromRsn = "3";
                    insertIpsB2i004Log();
                    updateIpsCard();
                    continue;
                }
            }
            
            //--mCode 
            if (hIpscMcodeCond.equals("Y")) {
            	if(commstring.ss2int(hAcnoIntRateMcode) >= commstring.ss2int(hIpscPaymentRate)) {
            		hB2i4FromRsn = "4";
                    insertIpsB2i004Log();
                    updateIpsCard();
                    continue;
            	}
            }
            
            if(totCnt % 5000 == 0)
            	commitDataBase();
            
        }
        closeCursor();
    }   
    
    /***********************************************************************/
    void selectCcaCardAcct() throws Exception {
    	
    	sqlCmd = " select block_reason1||block_reason2||block_reason3||block_reason4||block_reason5 as acct_block_code , "
    		   + " uf_spec_status(spec_status,spec_del_date) as acct_spec_status from cca_card_acct "
    		   + " where acno_p_seqno = ? " 
    		   ;
    	
    	setString(1,hAcnoPSeqno);
    	int recordCnt = selectTable();
    	
    	if(recordCnt > 0) {
    		hIsBlockReason = getValue("acct_block_code");
    		hAcctSpecStatus = getValue("acct_spec_status");
    	}
    	
    }
    
    /***********************************************************************/
    void selectCcaCardBase() throws Exception {
    	
    	sqlCmd = " select uf_spec_status(spec_status,spec_del_date) as card_spec_status from cca_card_base "
    		   + " where card_no = ? ";
    	
    	setString(1,hIardCardNo);
    	
    	int recordCnt = selectTable();
    	
    	if(recordCnt > 0) {
    		hIsBlockReason = getValue("acct_block_code");
    		hCardSpecStatus = getValue("card_spec_status");
    	}
    }
    
    /***********************************************************************/
    int selectActAcctSum() throws Exception {
        tempDouble = 0;

        sqlCmd = "select nvl(sum(unbill_end_bal+billed_end_bal),0) temp_double ";
        sqlCmd += " from act_acct_sum  ";
        sqlCmd += "where acct_code in ('BL','CA','ID','IT','AO','OT','DB','CB')  ";
        sqlCmd += "and p_seqno = ? ";
        setString(1, hAcnoPSeqno);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_act_acct_sum not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            tempDouble = getValueDouble("temp_double");
        }

        return 0;
    }

    /***********************************************************************/
    int chkIpsCommData() throws Exception {
        tempInt = 0;
        sqlCmd = "select count(*) temp_int ";
        sqlCmd += " from ips_comm_data  ";
        sqlCmd += "where parm_type = 'REJ_AUTH'  ";
        sqlCmd += "  and data_type = '01'  ";
        sqlCmd += "  and data_code = ? ";
        setString(1, hIardIpsCardNo);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            tempInt = getValueInt("temp_int");
        }

        return (tempInt);
    }

    /***********************************************************************/
    int insertIpsB2i004Log() throws Exception {

        insertCnt++;

        setValue("crt_date"   , hB2i4CrtDate);
        setValue("crt_time"   , hB2i4CrtTime);
        setValue("ips_card_no", hIardIpsCardNo);
        setValue("card_no"    , hIardCardNo);
        setValue("from_mark"  , "2");
        setValue("from_rsn"   , hB2i4FromRsn);
        setValue("proc_flag"  , "N");
        setValue("mod_pgm"    , javaProgram);
        setValue("mod_time"   , sysDate + sysTime);
        daoTable = "ips_b2i004_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            return (1);
        }

        return (0);
    }

    /***********************************************************************/
    void updateIpsCard0() throws Exception {

        daoTable   = "ips_card";
        updateSQL  = " standin_flag    = 'Y' ,";
        updateSQL += " no_standin_date = '', ";
        updateSQL += " mod_pgm         = ? ";
        setString(1, javaProgram);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
        }
    }

    /***********************************************************************/
    void updateIpsCard() throws Exception {
        daoTable   = "ips_card";
        updateSQL  = " standin_flag    = 'N' ,";
        updateSQL += " no_standin_date = ? ,";
        updateSQL += " mod_pgm         = ? ,";
        updateSQL += " mod_time        = sysdate";
        whereStr   = "where rowid      = ? ";
        setString(1, hBusiBusinessDate);
        setString(2, javaProgram);
        setRowId(3, hIardRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_ips_card not found!", "", hCallBatchSeqno);
        }

    }

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        IpsB004 proc = new IpsB004();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
