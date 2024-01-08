/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  109/02/18  V1.00.00    Rou         program initial                         *
*  109/02/26  V1.00.00    Rou        二次修改                                                                                           *
*  109/05/07  V1.00.00    Rou        三次修改                                                                                           *
 * 109/07/04  V1.00.01    Zuwei     coding standard, rename field method & format                   *
 * 109/07/23  V1.00.02    shiyqui     coding standard, rename field method & format                   *
 * 109/09/30  V1.00.03    Wilson     無檔案不秀error、執行日期 = 系統日                                              *
*  109-10-19  V1.00.04    shiyuqi       updated for project coding standard     *
*  111/02/14  V1.00.05    Ryan      big5 to MS950                                           *
*  111/04/06  V1.00.06    Ryan      add insert cca_outgoing                                
*  111/06/08  V1.00.07    Ryan      add insertOnbat2ecs                        *        
******************************************************************************/

package Dbc;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.*;

import Cca.CcaOutGoing;

public class DbcD043 extends AccessDAO {
  private final String progname = "接收設一科分行合併註銷作業處理程式 111/06/08  V1.00.07";

  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommRoutine comr = null;
  CommCrdRoutine comcr = null;
  CcaOutGoing outGoing = null;

  final int  debug = 1;
  final int debugd = 1;
  int tmpInt = 0;
  long totalCnt = 0;
  String hTempUser = "";
  String hCardNo = "";
  String hType = "";

  String getFileName = "";
  String vfFile = "";
  int readCnt = 0;
  int errorTmp = 0;

  int recordCnt = 0;
  int actCnt = 0;
  int rptSeq1 = 0;
  int errCnt = 0;
  long hModSeqno = 0;
  String hModUser = "";
  String hCallBatchSeqno = "";
  String hBusiBusinessDate = "";
  String hCardModWs = "";
  String hCardModUser = "";
  String hCardModPgm = "";
  int hCount = 0;
  
  String hAcctType = "";
  String hAcnoPSeqno = "";
  String hIdPSeqno = "";
  String hLostFeeCode = "";

  protected final String dt1Str = "type, card_no, saving_actno, card_ref_num, cancel_date, filler";

  protected final int[] dt1Ength = {2, 19, 16, 2, 8, 53};

  protected String[] dt1 = new String[] {};
  // ************************************************************************

  public int mainProcess(String[] args) {
    try {
      dt1 = dt1Str.split(",");
      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================
      if (args.length > 2) {
        comc.errExit("Usage : DbcD043 [date] [callbatch_seqno]", "");
      }

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comr = new CommRoutine(getDBconnect(), getDBalias());
      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      outGoing = new CcaOutGoing(getDBconnect(), getDBalias());

      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

      String checkHome = comc.getECSHOME();
      if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6)
          .equals(comc.getSubString(checkHome, 0, 6))) {
        comcr.hCallBatchSeqno = "no-call";
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
      hCardModUser = hModUser;
      hCardModWs = "ECS_SERVER";
      hCardModPgm = javaProgram;

      if (args.length > 2) {
        String err1 = "DbcD043 [date] [seq_no]\n";
        comcr.errRtn("DbcD043 [date] [seq_no] ", "", comcr.hCallBatchSeqno);
      }

      getBusinessDay();
      if (args.length > 0) {
        if (args[0].length() == 8) {
          hBusiBusinessDate = args[0];
        }
      }

      showLogMessage("I", "", "執行 日期 = [" + hBusiBusinessDate + "]");

      selectFile();

      // ==============================================
      // 固定要做的
      if (comcr.hCallBatchSeqno.length() == 20)
        comcr.callbatch(1, 0, 1); // 1: 結束
      comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
      showLogMessage("I", "", comcr.hCallErrorDesc);
      commitDataBase();
      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }

  // =============================================================================
  void selectFile() throws Exception {
    String filepath = comc.getECSHOME() + "/media/crd/";
    filepath = Normalizer.normalize(filepath, java.text.Normalizer.Form.NFKD);
    List<String> listOfFiles = comc.listFS(filepath, "", "");
    if (listOfFiles.size() == 0) {
//      comcr.errRtn(String.format("[%s]無檔案可處理!!", filepath), "", hCallBatchSeqno);
    	showLogMessage("I", "", "無檔案可處理");
    }

    for (String file : listOfFiles) {
      getFileName = file;
      if (!file.substring(0, 3).equals("VF_")) {
        continue;
      }
      getFileName = file;
      vfFile = comc.getECSHOME() + "/media/crd/" + getFileName;
      vfFile = Normalizer.normalize(vfFile, java.text.Normalizer.Form.NFKD);
      showLogMessage("I", "", "檔案 ==> [ " + filepath + getFileName + " ] ");
      if (selectCrdFileCtl() > 0) // The file already exists
        continue;
      if (readFileData() == 1)
        continue;
    }
  }

  // ************************************************************************
  public int selectCrdFileCtl() throws Exception {
    selectSQL = "count(*) as all_cnt";
    daoTable = "crd_file_ctl";
    whereStr = "WHERE file_name  = ? ";

    setString(1, getFileName);
    int recCnt = selectTable();
    if (debugd == 1)
      showLogMessage("I", "", "  file_ctl =[" + getValueInt("all_cnt") + "]");
    if (getValueInt("all_cnt") > 0) {
      showLogMessage("D", "", " 此檔案已存在,不可重複轉入 =[" + getFileName + "]");
      return 1;
    }

    return 0;
  }

  // =============================================================================
  int readFileData() throws Exception {
    int fi = openInputText(vfFile, "MS950");
    if (fi == -1)
      return 1;

    while (true) {
      String rec = readTextFile(fi); // read file data
      if (rec.length() == 0) {
        if (endFile[fi].equals("Y"))
          break;
      }
      readCnt++;
      totalCnt++;
      if (rec.length() != 100 && rec.length() != 101) {
        showLogMessage("D", "", " 此檔案第 " + readCnt + " 筆資料長度不正確，讀取失敗 ! ");
        continue;
      }
      byte[] bt = rec.getBytes("MS950");
      moveData(processDataRecord(getFieldValue(rec, dt1Ength, rec.length()), dt1));
      outGoing.InsertCcaOutGoing(hCardNo, "1", sysDate, hType);
      processDisplay(1000);
    }

    closeInputText(fi);

    insertCrdFileCtl();

    renameFile(getFileName);

    readCnt = 0;

    return 0;
  }

  // ************************************************************************
  private int moveData(Map<String, Object> map) throws Exception {
    hType = (String) map.get("type");
    hCardNo = (String) map.get("card_no");
    selectDbcCard();

    return 0;
  }


  /***********************************************************************/
  void getBusinessDay() throws Exception {
    hBusiBusinessDate = "";
    sqlCmd = "select to_char(sysdate,'yyyymmdd') as business_date ";
    sqlCmd += " from ptr_businday ";
    tmpInt = selectTable();

    if (tmpInt > 0) {
      hBusiBusinessDate = getValue("business_date");
    }
  }

  /***********************************************************************/
  int selectDbcCard() throws Exception {

    sqlCmd = "select ";
    sqlCmd += "card_no,acct_type,p_seqno,id_p_seqno,lost_fee_code ";
    sqlCmd += " from dbc_card ";
    sqlCmd += "where card_no = ? ";
    setString(1, hCardNo);

    int recordCnt = selectTable();
    if (recordCnt > 0) {
      if (debug == 1)
        showLogMessage("I", "", "Read card = [" + hCardNo + "] is  vd card.");
      hAcctType = getValue("acct_type");
      hAcnoPSeqno = getValue("p_seqno");
      hIdPSeqno = getValue("id_p_seqno");
      hLostFeeCode = getValue("lost_fee_code");
      updateDbcCard();
    } else
      selectDbcEmboss();

    return recordCnt;
  }

  /***********************************************************************/
  void selectDbcEmboss() throws Exception {

    sqlCmd = "select card_no ";
    sqlCmd += "from dbc_emboss ";
    sqlCmd += "where card_no = ? ";
    sqlCmd += "and apply_source = 'P' ";
    setString(1, hCardNo);
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      showLogMessage("I", "", "Read card = [" + hCardNo + "] is  vd perfab card.");
      updateDbcEmboss();
    } else
      showLogMessage("I", "", "Read card = [" + hCardNo + "] is other card.");

    return;
  }

  /***********************************************************************/
  void updateDbcCard() throws Exception {

    daoTable = "dbc_card";
    updateSQL = " current_code  = '1',";
    updateSQL += " oppost_reason = ?,";
    updateSQL += " oppost_date   = ?,";
    updateSQL += " mod_user      = 'batch',";
    updateSQL += " mod_time      = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'),";
    updateSQL += " mod_pgm       = 'DbcD043'";
    whereStr = "where card_no      = ? ";
    setString(1, hType);
    setString(2, sysDate);
    setString(3, sysDate + sysTime);
    setString(4, hCardNo);
    updateTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("update_dbc_card not found!", hCardNo, comcr.hCallBatchSeqno);
      return;
    }
    insertOnbat2ecs();
  }

  /***********************************************************************/
  void updateDbcEmboss() throws Exception {

    daoTable = "dbc_emboss";
    updateSQL = " prefab_cancel_flag = 'Y',";
    updateSQL += " mod_time      = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS'),";
    updateSQL += " mod_pgm       = 'DbcD043'";
    whereStr = "where card_no  = ? ";
    setString(1, sysDate + sysTime);
    setString(2, hCardNo);
    updateTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("update_dbc_emboss not found!", hCardNo, comcr.hCallBatchSeqno);
    }
  }

  /************************************************************************/
  private Map processDataRecord(String[] row, String[] DT) throws Exception {
    Map<String, Object> map = new HashMap<>();
    int i = 0;
    int j = 0;
    for (String s : DT) {
      map.put(s.trim(), row[i]);
      i++;
    }
    return map;

  }

  /************************************************************************/
  public String[] getFieldValue(String rec, int[] parm, int length) {
    int x = 0;
    int y = 0;
    byte[] bt = null;
    String[] ss = new String[parm.length];
    try {
      bt = rec.getBytes("MS950");
    } catch (Exception e) {
      showLogMessage("I", "", comc.getStackTraceString(e));
    }
    for (int i : parm) {
      try {
        ss[y] = new String(bt, x, i, "MS950");
      } catch (Exception e) {
        showLogMessage("I", "", comc.getStackTraceString(e));
      }
      y++;
      x = x + i;
    }
    return ss;
  }

  // ************************************************************************
  public int insertCrdFileCtl() throws Exception {

    setValue("file_name", getFileName);
    setValueInt("record_cnt", (int) totalCnt);
    setValue("crt_date", sysDate);
    setValue("trans_in_date", sysDate);

    daoTable = "crd_file_ctl";

    insertTable();

    if (dupRecord.equals("Y")) {
      String err1 = "insert_crd_file_ctl  error[dupRecord]";
      String err2 = "";
      comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
    }

    return (0);
  }
  
//************************************************************************
	private void insertOnbat2ecs() throws Exception {

		setValue("trans_type", "6");
		setValue("card_no", hCardNo);
		setValue("to_which", "1");
		setValue("dog", sysDate + sysTime);
		setValue("acct_type", hAcctType);
		setValue("acno_p_seqno", hAcnoPSeqno);
		setValue("id_p_seqno", hIdPSeqno);
		setValue("opp_type", "1");
		setValue("proc_mode", "O");
		setValue("opp_reason", hType);
		setValue("opp_date", sysDate);
		setValue("is_renew", "N");
		setValueInt("curr_tot_lost_amt", 100);
		setValue("lost_fee_flag", hLostFeeCode);
		setValue("debit_flag", "Y");

		daoTable = "onbat_2ecs";

		insertTable();

		if (dupRecord.equals("Y")) {
			showLogMessage("I", "", String.format("insert onbat_2ecs error,card_no = [%s]", hCardNo));
		}
	}


  // ************************************************************************
  void renameFile(String filename) throws Exception {
    String tmpstr1 = vfFile;
    String tmpstr2 = comc.getECSHOME() + "/media/crd/backup/" + filename + "." + sysDate;
    if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
      showLogMessage("I", "", "ERROR : 檔案[" + filename + "]更名失敗!");
      return;
    }
    showLogMessage("I", "", "檔案 [" + filename + "] 已移至 [" + tmpstr2 + "]");
  }

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    DbcD043 proc = new DbcD043();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
  /***********************************************************************/
}
