/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*     DATE     Version    AUTHOR                       DESCRIPTION           *
*  ---------  --------- ----------- ---------------------------------------- *
*  112/04/12  V1.00.01    Alex      program initial
 *  2023-1206 V1.00.02     JH    file_name: date-1天
*****************************************************************************/
package Inf;

import com.BaseBatch;
import com.CommCrd;
import com.CommFTP;
import com.CommRoutine;

public class InfC060 extends BaseBatch {
	private final String progname = "產生送CRDB 60 異動全戶停用註記 2023-1206 V1.00.02";
	CommCrd comc = new CommCrd();
	CommFTP commFTP = null;
	CommRoutine comr = null;
	String isFileName = "";
	private int ilFile60;
	String isProcDate = "";
	
	String majorId = "";
	String majorIdPSeqno = "";
	String acctType = "";
	String corpPSeqno = "";
	String corpNo = "";
	String fileId = "";
	
	public static void main(String[] args) {
		InfC060 proc = new InfC060();
		proc.mainProcess(args);
		proc.systemExit();
	}
	
	@Override
	protected void dataProcess(String[] args) throws Exception {
		dspProgram(progname);
		dateTime();
		int liArg = args.length;
		if (liArg > 1) {
			printf("Usage : InfC060 [proc_date]");
			okExit(0);
		}

      dbConnect();
      if (liArg == 1) {
         if (commDate.isDate(args[0])) {
            isProcDate = args[0];
         }
      }
		if (empty(isProcDate)) {
         isProcDate =commDate.dateAdd(sysDate,0,0,-1);
      }
		isFileName = "CRU23B1_TYPE_60_" +isProcDate+ ".txt";
      printf("Process: proc_date[%s], file_name[%s]"
          , isProcDate, isFileName);

      checkOpen();
		selectDataType60();
		closeOutputText(ilFile60);

		commFTP = new CommFTP(getDBconnect(), getDBalias());
		comr = new CommRoutine(getDBconnect(), getDBalias());
		procFTP();
		renameFile();

		sqlCommit();
		endProgram();

	}
	
	void selectDataType60() throws Exception {

		sqlCmd =" select uf_idno_id(major_id_p_seqno) as major_id"
          +", major_id_p_seqno , acct_type , corp_p_seqno "
          +", decode(corp_p_seqno,'',uf_corp_no(corp_p_seqno)) as corp_no"
          +" from crd_card "
          +" where 1=1 and oppost_date = ? "
      ;
//		sqlCmd += " uf_corp_no(corp_p_seqno) as corp_no ";

		setString(1, isProcDate);

      fetchExtend = "type60.";
      printf("Cursor.open .....");
		openCursor();
		while (fetchTable()) {
		   totalCnt++;
		   dspProcRow(5000);

			initData();
			majorId = colSs("type60.major_id");
			majorIdPSeqno = colSs("type60.major_id_p_seqno");
			acctType = colSs("type60.acct_type");
			corpPSeqno = colSs("type60.corp_p_seqno");
			corpNo = colSs("type60.corp_no");
			if(checkCard() == false)
				continue;					
			
			if(eq("01",acctType)) {
				if(fileId.indexOf(majorId)>=0)
					continue ;
			}	else	{
				if(fileId.indexOf(corpNo)>=0)
					continue ;
			}
			
			writeTextFile60();
		}
		closeCursor();
      printf("Cursor.close rows[%s]", totalCnt);
	}
	
	boolean checkCard() throws Exception {
		
		String sql1 = "";
		
		if(eq("01",acctType)) {
			sql1 = "select count(*) as db_cnt from crd_card where current_code ='0' and id_p_seqno = ? ";
			setString(1,majorIdPSeqno);
		}	else	{
			sql1 = "select count(*) as db_cnt from crd_card where current_code ='0' and corp_p_seqno = ? ";
			setString(1,corpPSeqno);
		}
		
		sqlSelect(sql1);
		if(sqlNrow > 0) {
			if(colNum("db_cnt") > 0)
				return false;
		}
		
		return true;
	}
	
	void writeTextFile60() throws Exception {
		StringBuffer tempBuf = new StringBuffer();
		String newLine = "\r\n";		
		tempBuf.append("60"); // --代碼 固定 60		
		if("01".equals(acctType)) {
			tempBuf.append(comc.fixLeft(majorId, 10)); // --主卡ID
			fileId += majorId+"|";
		}	else	{
			tempBuf.append(comc.fixLeft(corpNo, 10)); // --主卡ID
			fileId += corpNo+"|";
		}
		tempBuf.append("Y");				
		tempBuf.append(comc.fixLeft("", 137)); // --保留 119
		tempBuf.append(newLine);

		this.writeTextFile(ilFile60, tempBuf.toString());
	}
	
	void checkOpen() throws Exception {
		String lsTemp = "";
		lsTemp = String.format("%s/media/crdb/%s", comc.getECSHOME(), isFileName);
		ilFile60 = openOutputText(lsTemp, "MS950");
		if (ilFile60 < 0) {
			printf("CRU23B1-TYPE-60 產檔失敗 ! ");
			errExit(1);
		}
	}
	
	void procFTP() throws Exception {
		commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
		commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
		commFTP.hEriaLocalDir = String.format("%s/media/crdb", comc.getECSHOME());
		commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
		commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
		commFTP.hEflgModPgm = javaProgram;

		// System.setProperty("user.dir",commFTP.h_eria_local_dir);
		showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
		int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

		if (errCode != 0) {
			showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料" + " errcode:" + errCode);
			insertEcsNotifyLog(isFileName);
		}
	}

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

	void renameFile() throws Exception {
		String tmpstr1 = String.format("%s/media/crdb/%s", getEcsHome(), isFileName);
		String tmpstr2 = String.format("%s/media/crdb/backup/%s", getEcsHome(), isFileName);

		if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
			showLogMessage("I", "", "ERROR : 檔案[" + isFileName + "]更名失敗!");
			return;
		}
		showLogMessage("I", "", "檔案 [" + isFileName + "] 已移至 [" + tmpstr2 + "]");
	}
	
	void initData() {
		majorId = "";
	}
	
}
