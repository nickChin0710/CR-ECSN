/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version   AUTHOR               DESCRIPTION                      *
* ---------  -------------------  ------------------------------------------ *
* 108/06/04  V1.01.01  phopho     Initial                                    *
*  109/12/15  V1.00.01    shiyuqi       updated for project coding standard   *
*  112/11/02  V1.00.02    Ryan       增加參數[N】天前的資料   *
*  112/11/13  V1.00.03    Ryan       deleteActDebtCancel 修改為update_date < 參數日期   *
*  112/11/26  V1.00.04    Sunny      調整顯示執行的訊息                                                     *
*****************************************************************************/
package Col;

import com.*;

public class ColC041 extends AccessDAO {
    private String progname = "shell結束前執行act_debt_cancel清檔 112/11/26  V1.00.04 ";

    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommString  comms = new CommString();
    long totalCnt = 0;

    // ************************************************************************

    public static void main(String[] args) throws Exception {
        ColC041 proc = new ColC041();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    // ************************************************************************

    public int mainProcess(String[] args) {
        try {
            dateTime();
            setConsoleMode("N");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            
            if(args.length == 0 || !comms.isNumber(args[0])) {
                showLogMessage("I","","請輸入參數:");
            	showLogMessage("I","","[PARM 1 : 清除【N】天前的資料。]");
            	return 0;
            }
            
            deleteActDebtCancel(args[0]);

            showLogMessage("I", "", "清除筆數 : [" + totalCnt + "] 筆");

            // ==============================================
            showLogMessage("I", "", "程式執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    } // End of mainProcess
    // ************************************************************************

    void deleteActDebtCancel(String param) throws Exception {
    	showLogMessage("I","",String.format("參數1 : [%s]", param));
    	if(comms.ss2int(param) > 0) {
    	    CommDate  commd = new CommDate();
      		String businday = getBusiDate();
     		showLogMessage("I","",String.format("營業日期 : [%s]", businday));

           	whereStr = " where update_date < ? ";
          	setString(1,commd.dateAdd(businday, 0, 0, comms.ss2int(param) * -1));
          	
          	showLogMessage("I","",String.format("清除此日期之前的資料 : [%s]", commd.dateAdd(businday, 0, 0, comms.ss2int(param) * -1)));
        }
    	daoTable = "act_debt_cancel";
        totalCnt = deleteTable();
    }

}