/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  111/04/06  V1.00.00    ryan     program initial                           *
 ******************************************************************************/

package Dbc;


import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

import Cca.CcaOutGoing;

/*設一科未啟用註銷作業補INSERT CCA_OUTGOING程式*/
public class DbcD141 extends AccessDAO {
    private String progname = "設一科未啟用註銷作業補INSERT CCA_OUTGOING程式  111/04/06 V1.00.00";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommCrdRoutine comcr = null;
    CcaOutGoing outGoing = null;
    private int totalCnt = 0;
    private int argCnt = 0;
    private String strDate = "";
    private String hCardNo = "";
    private String hCurrentCode = "";
    private String hOppostReason = "";
    private String hOppostDate = "";
	
    public int mainProcess(String[] args) {

        try {

            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);
            // =====================================
            argCnt = args.length;
            if(argCnt==1) {
            	strDate = args[0];
            }
            
            // 固定要做的

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            outGoing = new CcaOutGoing(getDBconnect(), getDBalias());
            
            selectCcaoOutgoing();
            
            showLogMessage("I", "", String.format("程式執行結束,筆數=[%d]", totalCnt));
            
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
	void selectCcaoOutgoing() throws Exception {

		sqlCmd = " select card_no,current_code,oppost_reason,oppost_date from crd_card where oppost_reason = 'C4' ";
		if(argCnt==1) {
			sqlCmd += " and oppost_date = ? ";
		}
		sqlCmd += " UNION ";
		sqlCmd += " select card_no,current_code,oppost_reason,oppost_date from dbc_card where oppost_reason = 'VD'  ";
		if(argCnt==1) {
			sqlCmd += " and oppost_date = ? ";
		}
		if(argCnt==1) {
			setString(1,strDate);
			setString(2,strDate);
		}

		this.openCursor();

		while (fetchTable()) {
			hCardNo = getValue("card_no");
			hCurrentCode = getValue("current_code");
			hOppostReason = getValue("oppost_reason");
			hOppostDate = getValue("oppost_date");
			
			if(outGoing.InsertCcaOutGoing(hCardNo, hCurrentCode, hOppostDate, hOppostReason)==1)
				totalCnt++;
			commitDataBase();
		}
		this.closeCursor();
	}


    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        DbcD141 proc = new DbcD141();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
