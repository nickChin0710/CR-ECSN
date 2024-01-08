/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/08/29  V1.00.01   JeffKung     program initial                        *
*****************************************************************************/
package Tmp;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommTxInf;

public class TmpB015 extends AccessDAO {
	private final String progname = "處理上線一次性異動資料程式  112/08/29 V1.00.01";

	CommCrd comc = new CommCrd();
	CommFunction comm = new CommFunction();
	CommCrdRoutine comcr = null;

	long   hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    
    String hBusinessDate = "";
    String hSystemDate = "";
    String hProcFlag = "";

    // *********************************************************
    public int mainProcess(String[] args)
    {
        try
        {
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            commonRtn();

            if(args.length == 2)
            {
                if(args[0].length() == 8)
                {
                    hBusinessDate = args[0];
                }
                
                if(args[1].length() == 1) {
                	hProcFlag = args[1];
                }
            } else {
            	return 0;
            }
            showLogMessage("I", "", "Process_date = " + hBusinessDate);

            if ("0".equals(hProcFlag) || "1".equals(hProcFlag)) {
            	int updCnt = updateBilBill();
            	showLogMessage("I", "", String.format("1.異動RskControlSeqno總筆數=[%d]", updCnt));
            }
            
            if ("0".equals(hProcFlag) || "2".equals(hProcFlag)) {
            	int updCnt = updatePtrGroupCode();
            	showLogMessage("I", "", String.format("2.異動AssignInstallment總筆數=[%d]", updCnt));
            }
            
            if ("0".equals(hProcFlag) || "3".equals(hProcFlag)) {
            	int updCnt = updateBilCurpost();
            	showLogMessage("I", "", String.format("3.異動CurrPostFlag總筆數=[%d]", updCnt));
            }
            
            if ("0".equals(hProcFlag) || "4".equals(hProcFlag)) {
            	int updCnt = updateBilContract();
            	showLogMessage("I", "", String.format("4.異動bilContract總筆數=[%d]", updCnt));
            }

            
            finalProcess();
            return 0;
        } catch (Exception ex)
        { expMethod = "mainProcess"; expHandle(ex); return exceptExit; }
    }
    
    /***********************************************************************/
    void commonRtn() throws Exception
    {
        sqlCmd = "select business_date,to_char(sysdate,'yyyymmdd') h_system_date ";
        sqlCmd += "  from ptr_businday ";
        int recordCnt = selectTable();
        if(recordCnt > 0) {
            hBusinessDate = getValue("business_date");
            hSystemDate   = getValue("h_system_date");
        }

        hModUser = comc.commGetUserID();
        hModTime = hSystemDate;
    }
    
    /***********************************************************************/
    int updateBilBill() throws Exception
    {
    	int updCnt = 0;

        sqlCmd  = "update bil_bill a ";
        sqlCmd += " set a.rsk_post = 'O', ";
        sqlCmd += " a.rsk_ctrl_seqno = ";
        sqlCmd += " (select ctrl_seqno from rsk_problem b where b.reference_no = a.reference_no) ";
        sqlCmd += "where a.reference_no in ";
        sqlCmd += "      (select reference_no from rsk_problem where add_user = 'system') ";

        updCnt = updateTable();

        return updCnt;
        
    }
    
    /***********************************************************************/
    int updatePtrGroupCode() throws Exception
    {
    	int updCnt = 0;

        sqlCmd  = "update ptr_group_code ";
        sqlCmd += " set auto_installment = 'N', ";
        sqlCmd += "     assign_installment = 'Y' ";
        sqlCmd += "where 1=1 ";

        updCnt = updateTable();

        return updCnt;
        
    }
    
    int updateBilCurpost() throws Exception
    {
    	int totalUpdCnt = 0;
    	int updCnt = 0;

    	while(true) {
            sqlCmd  = "update bil_curpost ";
            sqlCmd += " set curr_post_flag = 'Y' ";
            sqlCmd += "where 1=1 ";
            sqlCmd += "and this_close_date = ? ";
            sqlCmd += "and curr_post_flag <> 'Y' ";
            sqlCmd += "fetch first 10000 rows only";

            setString(1, hBusinessDate);
            updCnt = updateTable();
            totalUpdCnt += updCnt;
            if (updCnt == 0) {
            	break;
            }

    	}

        return totalUpdCnt;
        
    }
    
    int updateBilContract() throws Exception
    {
    	int totalUpdCnt = 0;
    	int updCnt = 0;

    	//第一筆
    	sqlCmd  = "update bil_contract ";
        sqlCmd += " set year_fees_rate = 12.85 , ";
        sqlCmd += "     trans_rate = 12.85 , ";
        sqlCmd += "     first_post_kind = '0'  ";
        sqlCmd += "where card_no = '4907061300952106' ";
        sqlCmd += "and   mcht_no = '106000000005' ";
        sqlCmd += "and   all_post_flag = 'N' ";
        
        updCnt = updateTable();
        totalUpdCnt += updCnt;
        
        //第二筆
        sqlCmd  = "update bil_contract ";
        sqlCmd += " set year_fees_rate = 7.85 , ";
        sqlCmd += "     trans_rate = 7.85 , ";
        sqlCmd += "     first_post_kind = '0'  ";
        sqlCmd += "where card_no = '4907061300954102' ";
        sqlCmd += "and   mcht_no = '106000000005' ";
        sqlCmd += "and   all_post_flag = 'N' ";
        
        updCnt = updateTable();
        totalUpdCnt += updCnt;
        
        //第三筆
        sqlCmd  = "update bil_contract ";
        sqlCmd += " set year_fees_rate = 12.85 , ";
        sqlCmd += "     trans_rate = 12.85 , ";
        sqlCmd += "     first_post_kind = '0'  ";
        sqlCmd += "where card_no = '4907061300955109' ";
        sqlCmd += "and   mcht_no = '106000000005' ";
        sqlCmd += "and   all_post_flag = 'N' ";
        
        updCnt = updateTable();
        totalUpdCnt += updCnt;

        /*因為長循規則的卡號無法建立成功, 改成一般卡號
        //第四筆
        sqlCmd  = "update bil_contract ";
        sqlCmd += " set year_fees_rate = 11.6 , ";
        sqlCmd += "     trans_rate = 11.6 , ";
        sqlCmd += "     first_post_kind = '0'  ";
        sqlCmd += "where card_no = '3567430077375101' ";
        sqlCmd += "and   mcht_no = '106000000005' ";
        sqlCmd += "and   all_post_flag = 'N' ";
        
        updCnt = updateTable();
        totalUpdCnt += updCnt;
        */
        
        return totalUpdCnt;
        
    }
    
	public static void main(String[] args) {
		TmpB015 proc = new TmpB015();
		int retCode = proc.mainProcess(args);
		System.exit(retCode);
	}

}
