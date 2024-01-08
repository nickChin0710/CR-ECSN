/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  108/01/02  V1.01.00  Brian       program initial                           *
*  109/09/22  V1.01.01  Brian       森淼要求新增撈取條件1. 排除ich_card裡面的lock_flag = 'Y' 2. 前1000筆 *
*  109/10/08  V1.01.02  Brian       森淼調整select_ich_black_log()SQL邏輯同步                  *
*  109/11/29  V1.01.03  yanghan       修改了變量名稱和方法名稱                                                                  *
*  112/10/11  V1.01.04  Wilson      檔案只產生1000筆資料、有產生檔案的資料才異動報送日期時間           *
*  113/01/05  V1.01.05  Wilson      調整讀取資料條件                                                                                      *
******************************************************************************/

package Ich;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class IchF003 extends AccessDAO {
    private String progname = "金融機構鎖卡名單(B03B)產生  113/01/05  V1.01.05";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId   = "";
    String rptName = "";
    int rptSeq = 0;
    
    int    debug = 0;
    String hTempUser = "";
    String hTnlgNotifyDate = "";
    String hBusiBusinessDate = "";
    String hIcdrMediaCreateDate = "";
    String hIcdrMediaCreateTime = "";
    String hIcdrRowid = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    String tmpstr1 = "";
    String hTfinFileIden = "B03B";
    

    String hIcdrIchCardNo = "";
    String hIcdrProcType = "";
    String hIcdrProcRsn = "";
    

    int forceFlag = 0;
    int totCnt    = 0;
    int hTnlgRecordCnt = 0;
    String hHash   = "";
    String allData = "";

    buf1 detailSt = new buf1();
    String out = "";
    
    List<String> ichCardNoArr = new ArrayList<String>();
//************************************************************************************
public int mainProcess(String[] args)
{
 try
  {
   // ====================================
   // 固定要做的
   dateTime();
   setConsoleMode("Y");
   javaProgram = this.getClass().getName();
   showLogMessage("I", "", javaProgram + " " + progname);
   // =====================================
   if (args.length != 0 && args.length != 1 && args.length != 2) {
       comc.errExit("Usage : IchF003 [notify_date] [force_flag (Y/N)]", "");
   }

   // 固定要做的

   if (!connectDataBase()) {
       comc.errExit("connect DataBase error", "");
   }

   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

   String checkHome = comc.getECSHOME();
   if (comcr.hCallBatchSeqno.length() > 6) {
       if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
           comcr.hCallBatchSeqno = "no-call";
       }
   }

   comcr.hCallRProgramCode = this.getClass().getName();
   String hTempUser = "";
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

   
   hTnlgNotifyDate = "";
   forceFlag = 0;
   if (args.length == 1) {
       if ((args[0].length() == 1) && (args[0].equals("Y")))
           forceFlag = 1;
       if (args[0].length() == 8)
           hTnlgNotifyDate = args[0];
   }
   if (args.length == 2) {
       hTnlgNotifyDate = args[0];
       if (args[1].equals("Y"))
           forceFlag = 1;
   }
   selectPtrBusinday();

   tmpstr1 = String.format("BRQA_%3.3s_%8.8s_%4.4s",comc.ICH_BANK_ID3,hTnlgNotifyDate
                                                            ,hTfinFileIden);
   showLogMessage("I", "", "Process date=["+forceFlag+"]"+hTnlgNotifyDate+","+tmpstr1);

   hTnlgFileName = tmpstr1;

   if (forceFlag == 0) {
       if (selectIchNotifyLoga() != 0) {
           String errMsg = String.format("select_ich_notify_log_a error !");
           comcr.errRtn(errMsg, "",comcr.hCallBatchSeqno);
       }
   } else {
       updateIchBlackLoga();
   }

   fileOpen();

   selectIchBlackLKog();

   if(totCnt > 0)
     {
      hHash  = comc.encryptSHA(allData, "SHA-1", "big5");
      tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%-40.40s\r\n",hTfinFileIden
              ,"01", "0001", comc.ICH_BANK_ID3, String.format("%08d",totCnt) + "A", hHash);

      lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
     }

   hTnlgRecordCnt = totCnt;

   fileClose();


   comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
   showLogMessage("I", "", comcr.hCallErrorDesc);

   if (comcr.hCallBatchSeqno.length() == 20)    comcr.callbatch(1, 0, 1); // 1: 結束
   
   finalProcess();
   return 0;
  } catch (Exception ex) 
      { expMethod = "mainProcess"; expHandle(ex); return exceptExit; 
      }
}
/***********************************************************************/
void selectPtrBusinday() throws Exception {
    
    hBusiBusinessDate = "";
    sqlCmd = "select business_date,";
    sqlCmd += "to_char(sysdate,'yyyymmdd') h_icdr_media_create_date,";
    sqlCmd += "to_char(sysdate,'hh24miss') h_icdr_media_create_time ";
    sqlCmd += " from ptr_businday  ";
    sqlCmd += " fetch first 1 rows only ";
    int recordCnt = selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_businday not found!", "",comcr.hCallBatchSeqno);
    }
    if (recordCnt > 0) {
        hBusiBusinessDate     = getValue("business_date");
        hTnlgNotifyDate = hTnlgNotifyDate.length() == 0 ? hBusiBusinessDate : hTnlgNotifyDate;
        hIcdrMediaCreateDate = getValue("h_icdr_media_create_date");
        hIcdrMediaCreateTime = getValue("h_icdr_media_create_time");
    }

}
/***********************************************************************/
int selectIchNotifyLoga() throws Exception {
        hTnlgMediaCreateDate = "";
        hTnlgFtpSendDate = "";

        sqlCmd  = "select media_create_date,";
        sqlCmd += " ftp_send_date ";
        sqlCmd += "  from ich_notify_log  ";
        sqlCmd += " where file_name = ? ";
        setString(1, hTnlgFileName);
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            return 0;
        }
        if (recordCnt > 0) {
            hTnlgMediaCreateDate = getValue("media_create_date");
            hTnlgFtpSendDate     = getValue("ftp_send_date");
        }

        if (hTnlgFtpSendDate.length() != 0) {
            String stderr = String.format("通知檔 [%s] 已FTP至ICH , 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        if (hTnlgMediaCreateDate.length() != 0) {
            String stderr = String.format("製卡回饋檔 [%s] 已產生, 不可重複執行 , 請通知相關人員處理(error)\n", hTnlgFileName);
            showLogMessage("I", "", stderr);
            return (1);
        }
        return (0);
    }

    /***********************************************************************/
    void updateIchBlackLoga() throws Exception {
        daoTable  = "ich_black_log";
        updateSQL = "proc_flag       = 'N'";
        whereStr  = "where file_name = ? ";
        setString(1, hTnlgFileName);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", hTnlgFileName
                                                              , comcr.hCallBatchSeqno);
        }

    }
/***********************************************************************/
void fileOpen() throws Exception 
{
  String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
  temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
  out = temstr1;


  selectSQL  = " count(*)   as all_cnt ";
  daoTable   = " ich_card_parm b ";
  whereStr   = " where 1=1               ";

  int recCnt = selectTable();

  hHash = "0000000000000000000000000000000000000000";
  tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%-40.40s\r\n",hTfinFileIden,"01"
          ,"0001",comc.ICH_BANK_ID3,"00000000A",hHash);

  lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));
}
/***********************************************************************/
void fileClose() throws Exception {
    comc.writeReport(out, lpar, "big5", false);
}
/***********************************************************************/
void selectIchBlackLKog() throws Exception 
{
        sqlCmd = "   SELECT ich_card_no, rowid1 , from_mark as order_seq ";
        sqlCmd += "   FROM (SELECT a.ich_card_no, ";
        sqlCmd += "                row_number () OVER (PARTITION BY a.ich_card_no ORDER BY a.crt_date DESC) AS SEQ, ";
        sqlCmd += "                a.rowid AS rowid1 , ";
        sqlCmd += "                a.from_mark ";
        sqlCmd += "           FROM ich_black_log a ";
        sqlCmd += "          WHERE 1 = 1 ";
        sqlCmd += "          AND PROC_FLAG = 'N' ";
        sqlCmd += "                AND a.ich_card_no NOT IN (SELECT ich_card_no ";
        sqlCmd += "                                            FROM ich_card ";
        sqlCmd += "                                           WHERE lock_flag = 'Y') ";
        sqlCmd += "         ORDER BY crt_date DESC, crt_time DESC)  ";
        sqlCmd += "  WHERE SEQ = '1' ";
        sqlCmd += " order by 3 Asc , 1 Asc ";
        openCursor();
        while (fetchTable()) {
            hIcdrIchCardNo     = getValue("ich_card_no");
            hIcdrProcType    = "I";
            hIcdrProcRsn   = "I8";
            hIcdrRowid       = getValue("rowid1");
            
            String dupFlag = "N";

            for(int i = 0; i < ichCardNoArr.size(); i++) {
                if(hIcdrIchCardNo.equals(ichCardNoArr.get(i))) {
                	dupFlag = "Y";
                	continue;
                }
            }

            if(dupFlag.equals("Y")) {
            	continue;
            }
            
            ichCardNoArr.add(hIcdrIchCardNo);
            
            //檔案只產生1000筆資料
            if(totCnt >= 1000) {
            	updateIchBlackLog(2);
            	continue;
            }

            writeRtn();
			updateIchBlackLog(1);
            insertIchB03bLock();
            
            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("Process records =[%d]\n", totCnt));

        }
        closeCursor();
}

/***********************************************************************/
void insertIchB03bLock() throws Exception 
{
		setValue("ich_card_no", hIcdrIchCardNo);
		setValue("proc_type", hIcdrProcType);
		setValue("proc_rsn", hIcdrProcRsn);
		setValue("media_create_date", hIcdrMediaCreateDate);
		setValue("media_create_time", hIcdrMediaCreateTime);
		setValue("file_name", hTnlgFileName);
		setValue("proc_flag", "Y");
		setValue("mod_pgm", javaProgram);
		setValue("mod_time", sysDate + sysTime);
		
        daoTable   = "ich_b03b_lock";
		insertTable();
		
		 if (dupRecord.equals("Y")) {
			 daoTable   = "ich_b03b_lock";
			 updateSQL = " proc_type = ?, ";
			 updateSQL += " proc_rsn  = ?, ";
			 updateSQL += " media_create_date = ?, ";
			 updateSQL += " media_create_time = ?, ";
			 updateSQL += " file_name = ?, ";
			 updateSQL += " proc_flag = 'Y', ";
			 updateSQL += " mod_pgm = ?, ";
			 updateSQL += " mod_time = sysdate ";
			 whereStr = " where ich_card_no = ? ";
			 setString(1, hIcdrProcType);
			 setString(2, hIcdrProcRsn);
			 setString(3, hIcdrMediaCreateDate);
			 setString(4, hIcdrMediaCreateTime);
			 setString(5, hTnlgFileName);
			 setString(6, javaProgram);
			 setString(7, hIcdrIchCardNo);
			 updateTable();
     
  }
		
}

/***********************************************************************/
void writeRtn() throws Exception 
{

        String tmpstr = "";

        detailSt = new buf1();

        detailSt.type = "D";

        tmpstr = String.format("%-16.16s"  , hIcdrIchCardNo);
        detailSt.ichCardNo      = tmpstr;

        tmpstr = String.format("%-1.1s"  , hIcdrProcType);
        detailSt.procType   = tmpstr;

        tmpstr = String.format("%-2.2s"  , hIcdrProcRsn);
        detailSt.procRsn       = tmpstr;

        detailSt.space    = String.format("%-29.29s"  , " ");

        tmpstr = String.format("%-14.14s", sysDate+sysTime  );
        detailSt.sysDatetime  = tmpstr;

        detailSt.fillerEnd    = "\r\n";

        String buf = detailSt.allText();
        lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

        allData += buf;

if(debug==1) 
   showLogMessage("I", "", "  Write =["+buf+"]"+ buf.getBytes("big5").length+","
                                          + allData.getBytes("big5").length);

        return;
}
/***********************************************************************/
void updateIchBlackLog(int idx) throws Exception 
{
        daoTable   = "ich_black_log";
        
        updateSQL = " proc_flag         = 'Y',";
        
        if(idx == 1) {
            updateSQL += " media_crt_date = ?,";
            updateSQL += " media_crt_time = ?,";
            updateSQL += " file_name      = ?,";
        }

        updateSQL += " mod_pgm           = ?,";
        updateSQL += " mod_time          = sysdate";
//        whereStr   = "where rowid        = ? ";
        whereStr   = "where proc_flag = 'N' and ich_card_no = ? ";
        
        int i = 1;
        
        if(idx == 1) {
            setString(i++, hIcdrMediaCreateDate);
            setString(i++, hIcdrMediaCreateTime);
            setString(i++, hTnlgFileName);
        }

        setString(i++, javaProgram);
//        setRowId( i++, hIcdrRowid);
        setString(i++, hIcdrIchCardNo);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "ich_card_no = " + hIcdrIchCardNo, comcr.hCallBatchSeqno);
        }

}
/***********************************************************************/
public static void main(String[] args) throws Exception 
{
  IchF003 proc = new IchF003();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
}
/***********************************************************************/
    class buf1 {
        String type;
        String ichCardNo;
        String procType;
        String procRsn;
        String space;
        String sysDatetime;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(type, 1);
            rtn += fixLeft(ichCardNo, 16);
            rtn += fixLeft(procType, 1);
            rtn += fixLeft(procRsn, 2);
            rtn += fixLeft(space, 29);
            rtn += fixLeft(sysDatetime, 14);
            rtn += fixLeft(fillerEnd, 2);
            return rtn;
        }

        String fixLeft(String str, int len) throws UnsupportedEncodingException {
            int size = (Math.floorDiv(len, 100) + 1) * 100;
            String spc = "";
            for (int i = 0; i < size; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = str + spc;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);

            return new String(vResult, "MS950");
        }
    }

}
