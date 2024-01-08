/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/03/09  V1.00.00    JustinWu     program initial                          *
*  109/05/22  V1.00.01    JustinWu     proc_code = '' -> proc_code in ('','0')
*  109/07/23  V1.00.02    shiyuqi      coding standard, rename field method & format   
*  109-09-24  V1.00.04    JustinWu     move class into loop     
*  109-10-19  V1.00.05    shiyuqi      updated for project coding standard     *
*  111/09/22  V1.00.06    JeffKung     mainProcess set to public method     *
*                                      substring使用共用function comc.getSubString 
*****************************************************************************/
package Bil;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommTxBill;

public class BilE111 extends AccessDAO {
    private  final String progname = "FISC請款檔分派處理程式-ICFXGQB磁條卡交易 111/09/22   V1.00.06";
    CommCrdRoutine  comcr = null;
    CommCrd comc = new CommCrd();
    CommTxBill commTxBill = null;

	public int mainProcess(String[] args) {
	     String filePath="";
	     String fileName="";
	     String fileDate="";
	     String bankNo="";
	     String hCallBatchSeqno = "";
	     int successCnt = 0;
        try {
        	int rtn;
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
            commTxBill = new CommTxBill(getDBconnect(), getDBalias());
            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, hCallBatchSeqno);
            //=====================================

            // select FISC's bank number
            bankNo = commTxBill.getFiscBankNoFromPtrSysParm();
            
			
			selectSQL = ""
	        		+ "file_name, "
	        		+ "file_date, "
	        		+ "local_dir as file_path, "
	        		+ "rowid as rowid ";
	        daoTable = " ecs_ftp_log ";
	        whereStr = " where 1=1 "
	        		+ " and system_id='FISC_FTP' " 
	        		+ " and source_from='FISC_FCARD' "
	        		+ " and group_id in ('ICTA11', 'ICTD11') "
	        		+ " and trans_mode='RECV' "
	        		+ " and trans_resp_code = 'Y' "
	        		+ " and proc_code in ('', '0') "; 

			openCursor();
			while (fetchTable()) {
				BilE112 bil112 = new BilE112();	
				BilE113 bil113 = new BilE113();	
				
				// example: fileName : ICFXGQBA : F00600000.ICFXGQBA.YYMMDDNN.txt
				fileDate = getValue("file_date");
				filePath = getValue("file_path");
				fileName = getValue("file_name");
				
				if( ! comc.getSubString(fileName,1, 9).equals(bankNo) ) {
					commTxBill.printDebugString(String.format("%s並非F00600000字頭!!", fileName));
					continue;
				}
				
				if( comc.getSubString(fileName,1, 18).equals(bankNo + ".ICFXGQBD") ) {
					// fileDate:傳檔日期FILE_DATE
					// filePath: ECS_FTP_LOG.local_dir
					// fileName:通知檔案名稱FILE_NAME
					// comcr.h_call_batch_seqno
					String[] newArgs = { fileDate, filePath, fileName, comcr.hCallBatchSeqno };
					rtn = bil112.mainProcess(newArgs);
					if (rtn < 0) {
						return rtn;
					}
					rtn = bil113.mainProcess(newArgs);
					if (rtn < 0) {
						return rtn;
					}
					String fileNameA = comc.getSubString(fileName,0, 17) + "A" + comc.getSubString(fileName,18);
					commTxBill.updateProcCodeFromecsFtpLog(javaProgram, fileName, fileNameA);
					successCnt++;
				}
			}
			if(successCnt == 0) {
				showLogMessage("I", "", String.format("%s：沒有須要新增的檔案", progname,successCnt));
			}else {
				showLogMessage("I", "", String.format("%s：新增%s個檔案成功", progname,successCnt));
			}
			showLogMessage("I", "", "執行結束");
			comcr.hCallErrorDesc = "程式執行結束";
            comcr.callbatchEnd();
			return 0;
		} catch (Exception e) {
			expMethod = "mainProcess";  
            expHandle(e); 
            return  exceptExit; 
		} finally {
			finalProcess();
		}
	}

	public static void main(String[] args) {
		BilE111 proc = new BilE111();
        int  retCode = proc.mainProcess(args);
        System.exit(retCode);
	}



}
