/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                       DESCRIPTION            *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  109/04/17  V1.00.00    Rou           program initial                         *
 *  109/06/30  V1.00.01    Wilson       status_id一律 = "1"                       *
 *  109-07-03  V1.00.02    shiyuqi      updated for project coding standard     *
 *  109/07/23  V1.00.03    shiyuqi      coding standard, rename field method & format  
 *  109/08/26  V1.00.04    Wilson      檔案路徑修改                            *
 *  109/09/14  V1.00.05    JustinWu   所有txt: insert crd_file_ctl, 移動至backup。日期最新txt: 執行所有動作 *
 *  109/09/15  V1.00.06    JustinWu   取檔名的1~19 =>1~21當作查詢file_name開頭, move the files that have already processed to backup directory
 *  109-10-19  V1.00.07    shiyuqi       updated for project coding standard     *
 *  109/11/04  V1.00.08    Wilson       新增處理staff_flag = ''的資料             *
 *  109/11/04  V1.00.09    Wilson       mark showLogMessage                    *
 *  111/11/21  V1.00.10    Ryan       修正substring避免長度不夠使程式當掉                    *
 *  112/04/07  V1.00.11    Wilson     insert crd_employee add unit_no          * 
 *  112/08/13  V1.00.12    Wilson     update add staff_br_no                   *         
 ******************************************************************************/

package Crd;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.text.Normalizer;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.CommString;

/* 讀取TCB員工檔作業 */
public class CrdF028 extends AccessDAO {

  public static final boolean DEBUG_MODE = false;

  private final String progname = "讀取TCB員工檔作業  112/08/13 V1.00.12";
  CommFunction comm = new CommFunction();
  CommCrd comc = new CommCrd();
  CommCrdRoutine comcr = null;
  CommString comStr = new CommString();
  String prgmId = "CrdF028";
  String hCallBatchSeqno = "";
  String hEmplEmployNo = "";
  String hEmplChiName = "";
  String hEmplId = "";
  String hEmplIdCode = "";
  String hEmplAcctNo = "";
  String hEmplAccountingNo = "";
  String hEmplConnectNo = "";
  String fileName = "";
  String hIdnoNo = "";
  String empConnectNo = "";

  String temstr2 = "";
  int tmp = 0;
  int errorFileCnt = 0;
  int maxDate = 0, tmpDate = 0;
  int emplExist;

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
      if (args.length != 0 && args.length != 1) {
        comcr.errRtn("Usage : CrdF028 [seqno]", "", hCallBatchSeqno);
      }

      Queue<String> fileNameQueue = fileOpen();
      int theSizeOfFileName = fileNameQueue.size();
      
      if(theSizeOfFileName > 0) {
    	  fileName = fileNameQueue.peek();
          showLogMessage("I", "", "process file = [" + fileName + "]");
          
          deleteCrdEmployee();
          updateCrdIdno();
          updateDbcIdno();
          readFile();
          
          for (int i = 0; i < theSizeOfFileName; i++) {
        	  fileName = fileNameQueue.remove();
        	  insertCrdFileCtl();
              removeFile(fileName);
          }
          
      }else {
    	  showLogMessage("I", "", "無檔案需處理,更新員工註記為空白的資料");
           
          sqlCmd = "select ";         
          sqlCmd += "id_no ";
          sqlCmd += "from crd_idno ";
          sqlCmd += "where staff_flag = '' ";
          sqlCmd += "union ";
          sqlCmd += "select ";
          sqlCmd += "id_no ";
          sqlCmd += "from dbc_idno ";
          sqlCmd += "where staff_flag = '' ";
          int recordCnt = selectTable();
          for (int i = 0; i < recordCnt; i++) {

        	  hIdnoNo = getValue("id_no", i);
        	  
           	  emplExist = selectCrdEmployee();
        	  
        	  updateCrdIdno2();
        	  updateDbcIdno2();        	  
          }
      }

      // ==============================================
      // 固定要做的
      comcr.hCallProcDesc = "執行結束";
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
  void deleteCrdEmployee() throws Exception {
    daoTable = "crd_employee";
    deleteTable();
  }

  /***********************************************************************/
  void readFile() throws Exception {
    String str600 = "";

    temstr2 = String.format("%s/media/crd/%s", comc.getECSHOME(), fileName);
    int f = openInputText(temstr2);
    if (f == -1) {
      comcr.errRtn("檔案不存在：" + fileName, "", hCallBatchSeqno);
    }
    closeInputText(f);

    int br = openInputText(temstr2, "MS950");
    if (br == -1) {
      comcr.callbatch(1, 100001, 0);
      comcr.errRtn(String.format("[%s]目前無資料需處理", fileName), "", hCallBatchSeqno);
    }
    while (true) {
      str600 = readTextFile(br);
      if (endFile[br].equals("Y"))
        break;

      // if (str600.length() < 43)
      // continue;

      hEmplIdCode = "0";
      hEmplAcctNo = str600.substring(0, 13);
//      showLogMessage("I", "", "acct_no = [" + hEmplAcctNo + "]");
      hEmplEmployNo = hEmplAcctNo.substring(7);
      hEmplId = str600.substring(13, 23);
//      showLogMessage("I", "", "id = [" + hEmplId + "]");
      hEmplAccountingNo = str600.substring(23, 29);
      hEmplConnectNo = str600.substring(29, 33);
      hEmplChiName = str600.substring(33);
      insertCrdEmployee();
      updateCrdIdno1();
      updateDbcIdno1();
    }
    closeInputText(br);
  }

  /***********************************************************************/
  void updateCrdIdno() throws Exception {

    daoTable = "crd_idno";
    updateSQL = "staff_flag = 'N',";
    updateSQL += "staff_br_no = '' ";
    whereStr = "where decode(staff_flag,'','Y',staff_flag) = 'Y' ";
    updateTable();
  }

  /***********************************************************************/
  void updateDbcIdno() throws Exception {

    daoTable = "dbc_idno";
    updateSQL = "staff_flag = 'N',";
    updateSQL += "staff_br_no = '' ";
    whereStr = "where decode(staff_flag,'','Y',staff_flag) = 'Y' ";
    updateTable();
  }

  /***********************************************************************/
  void updateCrdIdno1() throws Exception {

    daoTable = "crd_idno";
    updateSQL = "staff_flag = 'Y',";
    updateSQL += "staff_br_no = ? ";
    whereStr = "where id_no = ? ";
    setString(1, hEmplConnectNo);
    setString(2, hEmplId);
    updateTable();
  }

  /***********************************************************************/
  void updateDbcIdno1() throws Exception {

    daoTable = "dbc_idno";
    updateSQL = "staff_flag = 'Y',";
    updateSQL += "staff_br_no = ? ";
    whereStr = "where id_no = ? ";
    setString(1, hEmplConnectNo);
    setString(2, hEmplId);
    updateTable();
  }

  /***********************************************************************/  
  public int selectCrdEmployee() throws Exception {

	  empConnectNo = "";
	  
	  sqlCmd = "select ";         
      sqlCmd += "connect_no ";
      sqlCmd += "from crd_employee ";
      sqlCmd += "where id = ? ";
      setString(1, hIdnoNo);
      int recordCnt = selectTable();
      
      if(recordCnt > 0) {
    	  empConnectNo = getValue("connect_no");
    	  return(1); 
      }            
    	  
      return(0);         
  }

/***********************************************************************/
  void updateCrdIdno2() throws Exception {

	    daoTable = "crd_idno";
	    updateSQL = "staff_flag = decode(cast(? as varchar(10)),1,'Y','N'), ";
	    updateSQL += "staff_br_no = ? ";
	    whereStr = "where id_no = ? ";
	    setInt(1, emplExist);
	    setString(2, empConnectNo);
	    setString(3, hIdnoNo);
	    updateTable();
	  }

  /***********************************************************************/
  void updateDbcIdno2() throws Exception {

	    daoTable = "dbc_idno";
	    updateSQL = "staff_flag = decode(cast(? as varchar(10)),1,'Y','N'), ";
	    updateSQL += "staff_br_no = ? ";
	    whereStr = "where id_no = ? ";
	    setInt(1, emplExist);
	    setString(2, empConnectNo);
	    setString(3, hIdnoNo);
	    updateTable();
	  }

  /***********************************************************************/
  void insertCrdEmployee() throws Exception {
    setValue("employ_no", hEmplEmployNo.toUpperCase());
    setValue("chi_name", hEmplChiName);
    setValue("id", hEmplId);
    setValue("id_code", hEmplIdCode);
    setValue("acct_no", hEmplAcctNo);
    setValue("unit_no", hEmplConnectNo);
    setValue("accounting_no", hEmplAccountingNo);
    setValue("connect_no", hEmplConnectNo);
    setValue("status_id", "1");
    setValue("crt_date", sysDate);
    setValue("mod_pgm", javaProgram);
    setValue("mod_time", sysDate + sysTime);
    daoTable = "crd_employee";
    insertTable();
    if (dupRecord.equals("Y"))
      comcr.errRtn("insert_crd_employee a not found!", "", hCallBatchSeqno);
  }

  /***********************************************************************/
  /*   111/11/21  V1.00.10    Ryan       修正substring避免長度不夠使程式當掉                    */
  Queue<String> fileOpen() throws Exception {
	// fileNameQueue: 裡面元素會排序從大到小日期 
	Queue<String> fileNameQueue = new PriorityQueue<String>(new Comparator<String>() {
		@Override
		public int compare(String str1, String str2) {
			return str2.substring(13, 21).compareTo(str1.substring(13, 21));
		}
	});

    String tmpstr = String.format("%s/media/crd", comc.getECSHOME());
    tmpstr = Normalizer.normalize(tmpstr, java.text.Normalizer.Form.NFKD);
    List<String> listOfFiles = comc.listFS(tmpstr, "", "");
    
    for (String file : listOfFiles) {
      if (!comStr.mid(file, 0,13).equals("tcb_employee_"))
        continue;
      if (file.length() != 25)
        continue;
      fileName = file;
      showLogMessage("I", "", "read file = [" + fileName + "]");
      
      if (selectCrdFileCtl() == 1) {
        errorFileCnt++;
        removeFile(fileName);  // Justin Wu: if the file has already been processed, move this file to backup directory
        continue;
      }
      
      fileNameQueue.add(fileName);
      
    }
    
    return fileNameQueue;

  }

  // 抓取最新日期的檔案
  /***********************************************************************/
  String getNewMonth(int getCnt, String[] saveCnt) throws Exception {

    if (getCnt > 1) {
      for (int j = 0; j < getCnt; j++) {
        tmpDate = Integer.parseInt(saveCnt[j].substring(19, 21));
        if (maxDate < tmpDate) {
          maxDate = tmpDate;
          fileName = saveCnt[j];
        }
      }
    } else
      fileName = saveCnt[0];
    return fileName;
  }

  /***********************************************************************/
  int selectCrdFileCtl() throws Exception {

    String fileNameDate = fileName.substring(0, 21);   // 取檔名的1~19 =>1~21當作查詢file_name開頭
    sqlCmd = "select file_name, crt_date ";
    sqlCmd += "from crd_file_ctl ";
    sqlCmd += "where file_name like ? ";
    setString(1, fileNameDate + "%");
    int recordCnt = selectTable();
    if (recordCnt > 0) {
//    	//2020-09-14 Justin Wu
//      // String getCrtDate = getValue("crt_date").substring(0, 6);
//      String beforeDate = String.valueOf(Integer.parseInt(sysDate.substring(0, 6)) - 1); // 取得前一個月的月份
//      // if (sysDate.substring(0, 6).equals(getCrtDate)) {
//      // showLogMessage("I", "", "Error : file = ["+ fileName +"] 因該月份檔案已存在，不可新增 !");
//      // return 1;
//      // }
//      if (sysDate.substring(0, 6).equals(beforeDate)) {
//        return 2;
//      }
    	 showLogMessage("I", "", "Error : file = ["+ fileName +"]，該檔案已處理 !");
    	 return 1;
    }
    return 0;
  }

  /***********************************************************************/
  public void removeFile(String removeFileName) throws Exception {
    String fileStr1 = comc.getECSHOME() + "/media/crd/" + removeFileName;
    String fileStr2 = comc.getECSHOME() + "/media/crd/backup/" + removeFileName + "." + sysDate;

    if (comc.fileRename2(fileStr1, fileStr2) == false) {
      showLogMessage("I", "", "Error : File = [" + removeFileName + "] rename fail!");
      return;
    }
    showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + fileStr2 + "]");
  }

  /***********************************************************************/
  public int insertCrdFileCtl() throws Exception {

    setValue("file_name", fileName);
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

  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    CrdF028 proc = new CrdF028();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }

}
