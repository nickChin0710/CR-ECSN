/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  109/05/04  V1.00.00   JustinWu     program initial                          
*  109/06/09  V1.00.01   JustinWu     if there is an given argument, use it as the value of chg_date*
*  109-07-03  V1.00.02   shiyuqi       updated for project coding standard     *
*  109-07-22  V1.00.03   yanghan       修改了字段名称                                                                                   *
*  109/08/25  V1.00.04   Wilson        加入order by                           *
*  109-09-03  V1.00.05   JustinWu     lineSeparator -> 0D0A                  * 
*  109/09/05  V1.00.06    yanghan     fix code scan issue                    * 
*  109/09/14  V1.00.06    Zuwei       code scan issue                        * 
*  109/09/14  V1.00.07   Wilson       新增procFTP                             *   
*  109/09/29  V1.00.08   Wilson       InfCrb04 -> InfC004                    * 
*  109/09/30  V1.00.09   JustinWu    每次都產檔                                                                                           *
*  109/10/14  V1.00.10   Wilson      LOCAL_FTP_PUT -> NCR2TCB                *
*  109-10-19  V1.00.11    shiyuqi       updated for project coding standard  *
*  109-11-11  V1.00.12   tanwei        updated for project coding standard   *
*  110-02-01  V1.00.13   Alex         改用營業日								 *
*  110/10/01  V1.00.14   Wilson       將mainProcess private改 public           *
*  112/03/29  V1.00.15   Ryan         比對參數日期若相同讀出全檔資料                                                   *
*  112/05/02  V1.00.16   Wilson       讀取凍結&特指的資料                                                                       *
*  113/01/03  V1.00.17   Wilson       調整讀取全檔邏輯                                                                              *
*****************************************************************************/
package Inf;

import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommRoutine;

import Dxc.Util.SecurityUtil;


public class InfC004 extends AccessDAO {
  private final String progname = "產生送CRDB 04停卡或撤銷時異動卡況檔程式 113/01/03 V1.00.17";
  private String prgmId = "InfC004";
  CommCrdRoutine comcr = null;
  CommCrd comc = new CommCrd();
  CommDate  commDate = new CommDate();
  CommFTP commFTP = null;
  CommRoutine comr = null;
  
  String tempUser = "";
  String filePath1 = "";
  String busiDate = "";
  String wfValue = "";
  String hOpenDate = "";
  int totCnt = 0;
  
  private final String filePathFromDb = "media/crdb";
  private final String fileName = "CRU23B1_TYPE_04_";

  public int mainProcess(String[] args) {
    String callBatchSeqno = "";

    try {
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
      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
      tempUser = comcr.callbatchStart(args, comc.getECSHOME(), javaProgram, callBatchSeqno);
      // =====================================      
      selectPtrSysParm();
      showLogMessage("E", "", String.format("PTR_SYS_PARM讀取參數日期為[ %s]", wfValue));
      readSysdate();
      String searchDate = (args.length == 0) ? "" : args[0].trim();
      if(searchDate.isEmpty())
    	  searchDate = hOpenDate ;
      List<ChgObj04> chgObjList = findChgObjList(searchDate);

		// get the fileFolderPath such as C:\EcsWeb\media\crdb
		String filePath = getFilePath(comc.getECSHOME(), filePathFromDb, fileName + searchDate + ".txt");						
		filePath1 = fileName + searchDate + ".txt";

		// open CRU23B1_TYPE_04_yyyymmdd.txt file
		int outputFile = openOutputText(filePath);
		if (outputFile == -1) {
			showLogMessage("E", "", String.format("此路徑或檔案不存在: %s", fileName));
			return -1;
		}

		String outputString = "";
		if (chgObjList != null) {
			outputString = getOutputString(chgObjList);
		}

		boolean isWriteOk = writeTextFile(outputFile, outputString);
		if (!isWriteOk) {
			throw new Exception("writeTextFile Exception");
		}

		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameFile1(filePath1);

      
      showLogMessage("I", "", "執行結束");
      comcr.hCallErrorDesc = "程式執行結束";
      comcr.callbatchEnd();
      return 0;
    } catch (Exception e) {
      expMethod = "mainProcess";
      expHandle(e);
      return exceptExit;
    } finally {
      finalProcess();
    }
  }

  /**
   * 找出前一日有異動卡況的資料
   * 
   * @param searchDate
   * @return ChgObjList: 有異動的資料
   * @throws Exception
   */
  private List<ChgObj04> findChgObjList(String searchDate) throws Exception {
    System.out.print(searchDate);
    
    if(!wfValue.equals(searchDate)) {
        sqlCmd = "  select card_no,acct_type,chg_data_old,chg_data,mod_time "; 
        sqlCmd += "   from cms_chgcolumn_log ";
        sqlCmd += "  where lower(chg_column) = 'current_code' ";
    	sqlCmd += "    and chg_date = ? ";			    	
        sqlCmd += " union ";
        sqlCmd += " select a.card_no,b.acct_type,decode(a.logic_del,'N','0','T') as chg_data_old ";
        sqlCmd += "        ,decode(a.logic_del,'N','T','0') as chg_data,a.mod_time ";
        sqlCmd += "   from cca_special_visa a,cca_card_base b ";
        sqlCmd += "  where a.card_no = b.card_no ";
		sqlCmd += "    and a.chg_date = ? ";
        sqlCmd += " union ";
        sqlCmd += " select b.card_no,a.acct_type,decode(a.log_type,'3','0','T') as chg_data_old ";
        sqlCmd += "        ,decode(a.log_type,'3','T','0') as chg_data,a.mod_time ";
        sqlCmd += "   from rsk_acnolog a,crd_card b ";
        sqlCmd += "  where a.acno_p_seqno = b.acno_p_seqno ";
        sqlCmd += "    and b.current_code = '0' ";
        sqlCmd += "    and a.log_type in('3','4') ";
		sqlCmd += "    and a.log_date = ? ";
        sqlCmd += " union ";
        sqlCmd += " select b.card_no,a.acct_type,decode(a.log_type,'3','0','T') as chg_data_old ";
        sqlCmd += "        ,decode(a.log_type,'3','T','0') as chg_data,a.mod_time ";
        sqlCmd += "   from rsk_acnolog a,dbc_card b ";
        sqlCmd += "  where a.acno_p_seqno = b.p_seqno ";
        sqlCmd += "    and b.current_code = '0' ";
        sqlCmd += "    and a.log_type in('3','4') ";
		sqlCmd += "    and a.log_date = ? ";
        sqlCmd += " order by mod_time ";
        setString(1, searchDate);
		setString(2, searchDate);
		setString(3, searchDate);
		setString(4, searchDate); 
    }
    else {
    	showLogMessage("I", "", "參數日期比對相同，讀全檔資料");
    	
        sqlCmd = "  select a.card_no, ";
        sqlCmd += "        a.acct_type, ";
        sqlCmd += "        '' as chg_data_old, ";
        sqlCmd += "        decode(a.current_code,'0',decode(b.spec_status||c.block_reason1||c.block_reason2||c.block_reason3||c.block_reason4||c.block_reason5,'','0','T'),a.current_code) as chg_data ";
        sqlCmd += "   from crd_card a, cca_card_base b,cca_card_acct c ";
        sqlCmd += "  where a.card_no = b.card_no ";
        sqlCmd += "    and a.acno_p_seqno = c.acno_p_seqno ";
        sqlCmd += " union ";
        sqlCmd += " select a.card_no, ";
        sqlCmd += "        a.acct_type, ";
        sqlCmd += "        '' as chg_data_old, ";
        sqlCmd += "        decode(a.current_code,'0',decode(b.spec_status||c.block_reason1||c.block_reason2||c.block_reason3||c.block_reason4||c.block_reason5,'','0','T'),a.current_code) as chg_data ";
        sqlCmd += "   from dbc_card a, cca_card_base b,cca_card_acct c ";
        sqlCmd += "  where a.card_no = b.card_no ";
        sqlCmd += "    and a.p_seqno = c.acno_p_seqno ";
    }
    		
    int selectCount = selectTable();

    if (selectCount == 0) {
      return null;
    }

    List<ChgObj04> chgObjList = new LinkedList<ChgObj04>();
    ChgObj04 chgObj = null;

    for (int i = 0; i < selectCount; i++) {
      chgObj = new ChgObj04();
      totCnt++;
      
      if(totCnt % 50000 == 0 || totCnt == 1)
          showLogMessage("I","",String.format(" Process 1 record=[%d]\n", totCnt));

      chgObj.cardNo = getValue("card_no", i);
      chgObj.acctType = getValue("acct_type", i);
      chgObj.chgDataOld = getValue("chg_data_old", i);
      chgObj.chgData = getValue("chg_data", i);
      chgObj.idNo = getIdno( chgObj.cardNo, chgObj.acctType);

      chgObjList.add(chgObj);
    }

    return chgObjList;
  }
  
  void selectPtrSysParm() throws Exception {
	  extendField = "PARM.";
	  sqlCmd = "SELECT WF_VALUE FROM PTR_SYS_PARM WHERE WF_KEY = 'INFC004'";
	  selectTable();
	  wfValue = getValue("PARM.WF_VALUE");
  }
  
	
  public void readSysdate() throws Exception {
	  sqlCmd = " select to_char(sysdate-1,'yyyymmdd') as sysdate1 ";
	  sqlCmd += "from dual";		
	  selectTable();
	  hOpenDate = getValue("sysdate1");
	}
  
  /**
   * select crd_idno或dbc_idno得到ID_NO
   * 
   * @param idPSeqno
   * @param debitFlag
   * @param chgTable
   * @param chgCol
   * @return
   * @throws Exception
   */
  private String getIdno(String cardNo, String acctType)
      throws Exception {
    
    sqlCmd = " select id_no ";

    if ("90".equals(acctType)) {
    	sqlCmd += " from dbc_idno ";
    	sqlCmd += " where id_p_seqno = (select id_p_seqno from dbc_card where card_no = ?)  ";
    } else {
    	sqlCmd += " from crd_idno ";
    	sqlCmd += " where id_p_seqno = (select major_id_p_seqno from crd_card where card_no = ?) ";
    }

    setString(1, cardNo);

    int selectCount = selectTable();

    if (selectCount == 0) {
      throw new Exception(
          String.format("找不到acctType = %s 以及cardNo = %s 的id_no", acctType, cardNo));
    }

    return getValue("id_no");
  }

  /**
   * get file folder path by the project path and the file path selected from database
   * 
   * @param projectPath
   * @param filePathFromDb
   * @param fileNameAndTxt
   * @return
   * @throws Exception
   */
  private String getFilePath(String projectPath, String filePathFromDb, String fileNameAndTxt)
      throws Exception {
    String fileFolderPath = null;

    if (filePathFromDb.isEmpty() || filePathFromDb == null) {
      throw new Exception("file path selected from database is error");
    }
    String[] arrFilePathFromDb = filePathFromDb.split("/");

    projectPath=SecurityUtil.verifyPath(projectPath);
    fileFolderPath = Paths.get(projectPath).toString();
    for (int i = 0; i < arrFilePathFromDb.length; i++) {
      fileFolderPath = Paths.get(fileFolderPath, arrFilePathFromDb[i]).toString();
    }

    fileFolderPath=SecurityUtil.verifyPath(fileFolderPath);
    fileNameAndTxt=SecurityUtil.verifyPath(fileNameAndTxt);
    return Paths.get(fileFolderPath, fileNameAndTxt).toString();
  }

  /**
   * 產生output字串
   * 
   * @param chgObjList
   * @return
   * @throws UnsupportedEncodingException
   */
  private String getOutputString(List<ChgObj04> chgObjList) throws UnsupportedEncodingException {
	byte[] lineSeparatorArr = {'\r','\n'}; //0D0A
	String nextLine = new String(lineSeparatorArr,"MS950");
	
    StringBuilder sb = new StringBuilder();

    for (ChgObj04 chgObj : chgObjList) {
      sb.append("04");

      sb.append(comc.fixLeft(chgObj.cardNo, 16));
      sb.append(comc.fixLeft(chgObj.idNo, 11));
      sb.append(comc.fixLeft(chgObj.chgDataOld, 1));
      sb.append(comc.fixLeft(chgObj.chgData, 1));
      sb.append(comc.fixLeft("", 119));

      sb.append(nextLine);
    }

    return sb.toString();
  }

  public static void main(String[] args) {
    InfC004 proc = new InfC004();
    int retCode = proc.mainProcess(args);
    System.exit(retCode);
  }
  
  /***********************************************************************/
	void procFTP() throws Exception {
		  commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	      commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	      commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
	      commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	      commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	      commFTP.hEflgModPgm = javaProgram;
	      

	      // System.setProperty("user.dir",commFTP.h_eria_local_dir);
	      showLogMessage("I", "", "mput " + filePath1 + " 開始傳送....");
	      int errCode = commFTP.ftplogName("NCR2TCB", "mput " + filePath1);
	      
	      if (errCode != 0) {
	          showLogMessage("I", "", "ERROR:無法傳送 " + filePath1 + " 資料"+" errcode:"+errCode);
	          insertEcsNotifyLog(filePath1);          
	      }
	  }
	
	/****************************************************************************/
	  public int insertEcsNotifyLog(String fileName) throws Exception {
	      setValue("crt_date", sysDate);
	      setValue("crt_time", sysTime);
	      setValue("unit_code", comr.getObjectOwner("3", javaProgram));
	      setValue("obj_type", "3");
	      setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
	      setValue("notify_name", "媒體檔名:" + fileName);
	      setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
	      setValue("notify_desc2", "");
	      setValue("trans_seqno", commFTP.hEflgTransSeqno);
	      setValue("mod_time", sysDate + sysTime);
	      setValue("mod_pgm", javaProgram);
	      daoTable = "ecs_notify_log";

	      insertTable();

	      return (0);
	  }

	  /****************************************************************************/
		void renameFile1(String removeFileName) throws Exception {
			String tmpstr1 = comc.getECSHOME() + "/media/crdb/" + removeFileName;
			String tmpstr2 = comc.getECSHOME() + "/media/crdb/backup/" + removeFileName;
			
			if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
				showLogMessage("I", "", "ERROR : 檔案[" + removeFileName + "]更名失敗!");
				return;
			}
			showLogMessage("I", "", "檔案 [" + removeFileName + "] 已移至 [" + tmpstr2 + "]");
		}
  
}


class ChgObj04 {
  String cardNo = "";
  String acctType = "";
  String chgDataOld = "";
  String chgData = "";
  String idNo = "";
}

