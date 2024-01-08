/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/08/21  V1.00.01   JeffKung   program initial                           *
 ******************************************************************************/

package Bil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class BilA010 extends AccessDAO {
    private String progname = "更新DEBT專款專用註記作業   112/08/21 V1.00.01 ";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;
    String hCallErrorDesc = "";
    int tmpInt = 0;
    String hTempUser = "";

    String prgmId   = "BilA010";
    String prgmName = "更新DEBT專款專用註記作業";
    long   hModSeqno = 0;
    String hModUser = "";
    String hModTime = "";
    String hModPgm = "";
    
    String hBusinessDate = "";
    String hSystemDate = "";

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

            hTempUser = "";

            commonRtn();

            if(args.length >= 1)
            {
                if(args[0].length() == 8)
                {
                    hBusinessDate = args[0];
                }
            }
            showLogMessage("I", "", "Process_date = " + hBusinessDate);

            int updCnt = updateActDebt();

            showLogMessage("I", "", String.format("異動專款專用總筆數=[%d]", updCnt));
            
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
    int updateActDebt() throws Exception
    {
    	int updCnt = 0;
    	
        sqlCmd  = "update act_debt ";
        sqlCmd += " set spec_flag = 'Y' ";
        sqlCmd += "where post_date = ? ";
        sqlCmd += "  and contract_no in ";
        sqlCmd += "      (select contract_no from bil_contract where spec_flag = 'Y') ";
        
        setString(1, hBusinessDate);
        
        updCnt = updateTable();

        return updCnt;
        
    }
    
    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        BilA010 proc = new BilA010();
        int retCode  = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
    /***********************************************************************/
}
