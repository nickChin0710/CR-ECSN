/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  106/06/01  V1.00.00    David      program initial                                                                    *
 *  109/05/19  V1.00.01    Pino        for TCB layout                                                                    *
 *  109/07/03  V1.00.02    Zuwei     coding standard, rename field method & format                   *
 *  109/07/22  V1.00.04    shiyuqi    coding standard,                                                                *
 *  109/09/11  V1.00.05    JeffKung  change data source                                                            *
 *  109/10/19  V1.00.06    shiyuqi     updated for project coding standard                                   *
 *  111/05/16  V1.00.07    JeffKung  update for phaseIII VD回存                            
 *  111/11/21  V1.00.08    JeffKung  判斷檔案為空檔時,正常結束程式                              *
 *  112/07/19  V1.00.09    Alex      判斷DP01將回存結果更新回 rsk_problem   *
 ******************************************************************************/

package Dba;

import java.text.Normalizer;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;

public class DbaA010 extends AccessDAO {
  private final String progname = "VD回存回覆處理程式 112/07/19  V1.00.09";
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;

  private String hDaajModUser = "";
  private String hDaajModPgm = "";
  private String hDaajDeductProcCode = "";
  private double hDaajDeductAmt = 0;
  private String hDaajAcctNo = "";
  private String hDaajDeductSeq = "";

  private String hProcDate = "";
  private String hPreBusinessDate = "";
  
  private String hBusiBusinessDate = "";
  private String hCallBatchSeqno = "";
  private String hBusiVouchDate = "";
  private String temstr = "";
  private String   text = "";
  private byte[]   bytesArr = null;
  private String hTempReceiveDate = "";

  private int hDetlReceiveSucCnt = 0;
  private int hDetlReceiveFalCnt = 0;
  private String hDetlMediaSum = "";
  private int hDetlReceiveCnt = 0;
  private double hDetlReceiveAmt = 0;
  private double hDetlReceiveSucAmt = 0;
  private double hDetlReceiveFalAmt = 0;
  private String hDetlReceiveDate = "";
  private String hDetlCreateDate = "";
  private String hDetlDeductDate = "";
  private int hDetlSendCnt = 0;
  private String hDetlRowid = "";
  private String hReferenceNo = "";
  private String hBackStatus = "";
  
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

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

      hDaajModUser = comc.commGetUserID();
      hDaajModPgm = javaProgram;
      selectPtrBusinday();
      
      if (args.length != 0 && args.length != 1) {
          comc.errExit("Usage : DbaA010 [fileDate] ", "");
        }

      checkOpen(args);
      
      checkProcess();
      
      commitDataBase();
      
      selectDbaAcaj();
      
      showLogMessage("I", "", "程式執行結束");

      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }

  /*************************************************************************/
  int checkOpen(String[] args) throws Exception {

	  if (args.length == 0) {

      	hProcDate = hPreBusinessDate;
      	
      } else {

      	hProcDate = args[0];
         
      }

      temstr = String.format("%s/media/dba/VDREFUND_RSP.%8.8s", comc.getECSHOME() , hProcDate);
      showLogMessage("I", "", "檔案處理日期=[" +hProcDate + "]");
      
      return 0;

  }
  
	/**************************************************************************/
	void checkProcess() throws Exception {
		String stra = "";
		long failCnt = 0, successCnt = 0;
		int chkDeductCtl = 0;

		hDetlReceiveCnt = 0;
		hDetlReceiveAmt = 0;
		hDetlReceiveSucCnt = 0;
		hDetlReceiveSucAmt = 0;
		hDetlReceiveFalCnt = 0;
		hDetlReceiveFalAmt = 0;

		int readlen = 0;
		byte[] bytes = new byte[200];
		byte[] enter = new byte[1];
		if (openInputText(temstr, "MS950") == -1) {
			showLogMessage("I", "", String.format("[%s]目前無資料需處理", temstr));
			return;
		}

		while (true) {
			text = readTextFile(0);
			bytesArr = text.getBytes("MS950");
			
			//讀到檔尾,應該是空檔
			if ("Y".equals(endFile[0])) {
				showLogMessage("I", "", String.format("[%s]檔案為空檔", temstr));
				return;
			}

			// 首筆資料
			if (comc.subMS950String(bytesArr, 0, 1).equals("1")) {
				if (comc.subMS950String(bytesArr, 1, 3).equals("006") == false) {
					showLogMessage("I", "", String.format("該媒體非轉入之媒體, [%s]請檢核 !\n", text));
					break;
				}
				stra = comc.subMS950String(bytesArr, 4, 8);
				hDetlDeductDate = stra;
				chkDeductCtl = selectDbaDeductCtl();
				if (chkDeductCtl==1) {
					showLogMessage("E", "", String.format("該媒體已於[%s]轉入, 請檢核 !\n", hDetlReceiveDate));
					break;
				} else if (chkDeductCtl==2) {
					showLogMessage("E", "", String.format("無該媒體[%s]轉出紀錄, 請檢核 !\n", hDetlCreateDate));
					break;
				}
			}

			// 明細資料
			if (comc.subMS950String(bytesArr, 0, 1).equals("2")) { // 明細資料
				stra = comc.subMS950String(bytesArr, 27, 13);
				hDaajAcctNo = comc.rtrim(stra);

				stra = comc.subMS950String(bytesArr, 15, 12);
				hDaajDeductAmt = comc.str2double(comc.rtrim(stra));

				stra = comc.subMS950String(bytesArr, 81, 2);
				hDaajDeductProcCode = comc.rtrim(stra);

				stra = comc.subMS950String(bytesArr, 56, 15);
				hDaajDeductSeq = comc.rtrim(stra);

				if (hDaajDeductProcCode.equals("00")) {
					hDetlReceiveSucCnt++;
					hDetlReceiveSucAmt = hDetlReceiveSucAmt + hDaajDeductAmt;
				} else {
					hDetlReceiveFalCnt++;
					hDetlReceiveFalAmt = hDetlReceiveFalAmt + hDaajDeductAmt;
				}

				updateDbaAcaj();
			}
			
			//尾筆資料
			if (comc.subMS950String(bytesArr, 0, 1).equals("3")) {
				stra = comc.subMS950String(bytesArr, 12, 10);
				hDetlReceiveCnt = comc.str2int(stra);

				stra = comc.subMS950String(bytesArr, 22, 14);
				hDetlReceiveAmt = comc.str2double(stra);

				break;
			}

		}
		closeInputText(0);
		
		if (chkDeductCtl==0) {
			if ((hDetlReceiveCnt != hDetlReceiveSucCnt + hDetlReceiveFalCnt)
					|| (hDetlReceiveAmt != hDetlReceiveSucAmt + hDetlReceiveFalAmt)) {
				showLogMessage("I", "", String.format("媒體總筆數[%d]  成功[%d] 失敗[%d]!",
						hDetlReceiveCnt, hDetlReceiveSucCnt, hDetlReceiveFalCnt));
				showLogMessage("I", "", String.format("媒體總金額[%f]  成功[%f] 失敗[%f]!",
						hDetlReceiveAmt, hDetlReceiveSucAmt, hDetlReceiveFalAmt));
				showLogMessage("E", "", String.format("媒體明細與總筆數或金額不符, 請檢核 !"));
			} else if (hDetlSendCnt != hDetlReceiveCnt) {
				showLogMessage("I", "", String.format("h_detl_send_cnt[%d]", hDetlSendCnt));
				showLogMessage("I", "", String.format("h_detl_receive_cnt[%d]", hDetlReceiveCnt));
				showLogMessage("E", "", String.format("該媒體轉出與轉入之筆數不符, 請檢核 !"));
			} else {
				showLogMessage("I", "", String.format("媒體總筆數[%d]  成功[%d] 失敗[%d]!",
			              hDetlReceiveCnt, hDetlReceiveSucCnt, hDetlReceiveFalCnt));
			}
			
			updateDbaDeductCtl();
			
		}
	}

  /*************************************************************************/
  void selectPtrBusinday() throws Exception {
    hBusiBusinessDate = "";
    hBusiVouchDate = "";
    hPreBusinessDate = "";

    sqlCmd = "select business_date, to_char( to_date(business_date, 'yyyymmdd') - 1 DAYS , 'yyyymmdd') as prev_business_date , ";
    sqlCmd += "       vouch_date ";
    sqlCmd += "from   ptr_businday ";
    sqlCmd += "fetch first 1 rows only";

    selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    hBusiBusinessDate = getValue("business_date");
    hPreBusinessDate = getValue("prev_business_date");
    hBusiVouchDate = getValue("vouch_date");

  }

  /**************************************************************************/
  void updateDbaAcaj() throws Exception {
    daoTable = "dba_acaj ";
    updateSQL = "        deduct_proc_code = ?, ";
    updateSQL += "       deduct_proc_date = ?, ";
    updateSQL += "       deduct_proc_time = to_char(sysdate,'hh24miss'), ";
    updateSQL += "       proc_flag     = 'Y' , ";
    updateSQL += "       mod_pgm          = ?, ";
    updateSQL += "       mod_time         = sysdate ";
    whereStr = "where acct_no  = ? ";
    whereStr += "and   deduct_seq = ? ";
    whereStr += "and   deduct_proc_code='99' ";
    whereStr += "and   proc_flag = '0' ";

    setString(1, hDaajDeductProcCode);
    setString(2, hBusiBusinessDate);
    setString(3, javaProgram);
    setString(4, hDaajAcctNo);
    setString(5, hDaajDeductSeq);
    updateTable();
    if (notFound.equals("Y")) {
      showLogMessage("I", "","update_dba_acaj  not found! " +
          String.format("acct_no[%s] deduct_seq[%s]\n", hDaajAcctNo, hDaajDeductSeq));
    }

  }

  /**************************************************************************/
  void updateDbaDeductCtl() throws Exception {
    daoTable = "dba_deduct_ctl ";
    updateSQL = "receive_cnt  = ?, ";
    updateSQL += "receive_amt  = ?, ";
    updateSQL += "receive_suc_cnt = ?, ";
    updateSQL += "receive_suc_amt = ?, ";
    updateSQL += "receive_fal_cnt = ?, ";
    updateSQL += "receive_fal_amt = ?, ";
    updateSQL += "receive_date    = ?, ";
    updateSQL += "media_sum       = ?, ";
    updateSQL += "mod_time        = sysdate, ";
    updateSQL += "mod_pgm         = ? ";
    whereStr = "where  rowid   = ? ";

    int seq = 1;
    setInt(seq++, hDetlReceiveCnt);
    setDouble(seq++, hDetlReceiveAmt);
    setInt(seq++, hDetlReceiveSucCnt);
    setDouble(seq++, hDetlReceiveSucAmt);
    setInt(seq++, hDetlReceiveFalCnt);
    setDouble(seq++, hDetlReceiveFalAmt);
    setString(seq++, hBusiBusinessDate);
    setString(seq++, hDetlMediaSum);
    setString(seq++, javaProgram);
    setRowId(seq++, hDetlRowid);

    updateTable();

  }

  /**************************************************************************/
  int selectDbaDeductCtl() throws Exception {
    hDetlCreateDate = "";
    hDetlDeductDate = "";
    hDetlReceiveDate = "";
    hDetlSendCnt = 0;
    hDetlRowid = "";

    sqlCmd = "select crt_date, ";
    sqlCmd += "deduct_date, ";
    sqlCmd += "receive_date, ";
    sqlCmd += "send_cnt, ";
    sqlCmd += "rowid as rowid ";
    sqlCmd += "from   dba_deduct_ctl ";
    sqlCmd += "where  deduct_date = (select max(deduct_date) ";
    sqlCmd += "                      from   dba_deduct_ctl ";
    sqlCmd += "                      where  proc_type = 'B') ";
    sqlCmd += "and    proc_type = 'B' ";

    selectTable();
    if (notFound.equals("Y")) {
      return 2;
    }

    hDetlCreateDate = getValue("crt_date");
    hDetlDeductDate = getValue("deduct_date");
    hDetlReceiveDate = getValue("receive_date");
    hDetlSendCnt = getValueInt("send_cnt");
    hDetlRowid = getValue("rowid");

    
    if (hDetlReceiveDate.length() != 0)
      return (1);
    
    return (0);
  }
  
  /***********************************************************************/
  void selectDbaAcaj() throws Exception {
	  
	  sqlCmd = " select reference_no , deduct_proc_code from dba_acaj where adjust_type ='DP01' and deduct_proc_date = ? ";
	  setString(1,hBusiBusinessDate);
	  
	  openCursor();
	  
	  while(fetchTable()) {
		  hReferenceNo = "";
		  hDaajDeductProcCode = "";
		  hBackStatus = "";
		  
		  hReferenceNo = getValue("reference_no");
		  hDaajDeductProcCode = getValue("deduct_proc_code");
		  if("00".equals(hDaajDeductProcCode))
			  hBackStatus = "S";
		  else
			  hBackStatus = "F";
		  
		  updateRskProblem();
	  }
	  closeCursor();
  }
  
  void updateRskProblem() throws Exception {
	  
	  daoTable = "rsk_problem";
	  updateSQL = "back_status = ? , back_date = ? ";
	  whereStr = " where reference_no = ? and back_flag = 'Y' ";
	  
	  setString(1,hBackStatus);
	  setString(2,hBusiBusinessDate);
	  setString(3,hReferenceNo);
	  
	  updateTable();
	  
  }
  
  /***********************************************************************/
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    DbaA010 proc = new DbaA010();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

}
