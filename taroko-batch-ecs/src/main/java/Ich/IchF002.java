/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  107/12/25  V1.01.00  Lai         program initial                           *
*  109/11/29  V1.01.02  yanghan       修改了變量名稱和方法名稱                                                                              *
*  112/05/24  V1.01.03  Wilson      修正header帶入下一個檔案問題                                                         *
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

public class IchF002 extends AccessDAO {
    private String progname = "製卡回饋檔(B02B)產生  112/05/24 V1.01.03";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    String rptId   = "";
    String rptName = "";
    int rptSeq = 0;
    
    int    debug = 1;
    String hTempUser = "";
    String hTnlgNotifyDate   = "";
    String hBusiBusinessDate = "";
    String hIcrdCardCode         = ""; 
    String hIcdrMediaCreateDate = "";
    String hIcdrMediaCreateTime = "";
    String hIcdrSearchCode     = "";
    String hIcdrIssueStatus    = "";
    String hIcdrRespCodeIssue = "";
    String hIcdrIssueDate      = "";
    String hIcdrIssueTime      = "";
    String hIcdrIcSeqIcah     = "";
    String hIcdrFormatVer      = "";
    String hIcdrIchCardNo     = "";
    String hIcdrIsamUseCnt    = "";
    String hIcdrRowid           = "";
    String hTnlgFileName = "";
    String hTnlgMediaCreateDate = "";
    String hTnlgFtpSendDate = "";
    String tmpstr1 = "";
    String fileSeq = "";
    String hTfinFileIden = "B02B";

    int forceFlag = 0;
    int totCnt    = 0;
    int hTnlgRecordCnt = 0;
    String hHash   = "";
    String allData = "";

    buf1 detailSt = new buf1();
    String out = "";
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
       comc.errExit("Usage : IchF002 [notify_date] [force_flag (Y/N)]", "");
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

   if (forceFlag == 0) {
       if (selectIchNotifyLoga() != 0) {
           String errMsg = String.format("select_ich_notify_log_a error !");
           comcr.errRtn(errMsg, "",comcr.hCallBatchSeqno);
       }
   } else {
       updateIchB02bFbacka();
   }

   selectIchCardParm();

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
    void updateIchB02bFbacka() throws Exception {
        daoTable  = "ich_b02b_fback";
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
// file_seq = "01";
   tmpstr1 = String.format("FC_%9.9s_%8.8s",hIcrdCardCode ,hTnlgNotifyDate);
   showLogMessage("I", "", " Process date=["+hTnlgNotifyDate+"]"+tmpstr1);

   hTnlgFileName = tmpstr1;

  String temstr1 = String.format("%s/media/ich/%s", comc.getECSHOME(), hTnlgFileName);
  temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
  out = temstr1;


  selectSQL  = " count(*)   as all_cnt  ";
  daoTable   = " ich_b02b_fback a ";
  whereStr   = " where 1=1              ";
  whereStr  += "   and (a.proc_flag = 'N' or decode(a.proc_flag,'', 'N',a.proc_flag) = 'N') ";
  whereStr  += "   and  a.ich_card_no  like ? || '%' ";
  setString(1, hIcrdCardCode);
  
  int recCnt = selectTable();

  hHash = "0000000000000000000000000000000000000000";
  tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%18.18s%-40.40s\r\n",hTfinFileIden,"01"
          ,"0001",comc.ICH_BANK_ID3,String.format("%08d",getValueInt("all_cnt"))+"B"," ",hHash);

  lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", tmpstr1));
}
/***********************************************************************/
void fileClose() throws Exception {
    comc.writeReport(out, lpar, "big5", false);
    lpar.clear();
}
/***********************************************************************/
void selectIchCardParm() throws Exception 
{
  sqlCmd  = "select ";
  sqlCmd += " card_code  ";
  sqlCmd += " from ich_card_parm ";
  sqlCmd += "where 1 = 1         ";
  sqlCmd += "order by card_code   ";
  int recordCnt = selectTable();
  for (int i = 0; i < recordCnt; i++) {
      hIcrdCardCode   = getValue("card_code", i);
      
if(debug==1) showLogMessage("I", "", "Process card_code=["+i+"]"+hIcrdCardCode);

      fileOpen();
	  totCnt = 0;
      selectIchB02bFback();
      if(totCnt > 0)
        {
         hHash  = comc.encryptSHA(allData, "SHA-1", "big5");
		 showLogMessage("I", "", "alldata = ["+allData+"]" );
		 allData = "";
         tmpstr1 = String.format("H%4.4s%2.2s%4.4s%3.3s%9.9s%18.18s%-40.40s\r\n",hTfinFileIden
             ,"01", "0001", comc.ICH_BANK_ID3, String.format("%08d",totCnt) + "B", " ", hHash);
   
         lpar.set(0, comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "1", tmpstr1));
        }

      insertIchNotifyLog();

      fileClose();
  }
}
/***********************************************************************/
void selectIchB02bFback() throws Exception 
{
        hTnlgRecordCnt = 0;
        sqlCmd  = "select ";
        sqlCmd += "a.search_code    ,";
        sqlCmd += "a.issue_status   ,";
        sqlCmd += "a.resp_code_issue,";
        sqlCmd += "a.issue_date     ,";
        sqlCmd += "a.issue_time     ,";
        sqlCmd += "a.ic_seq_icah    ,";
        sqlCmd += "a.format_ver     ,";
        sqlCmd += "a.ich_card_no    ,";
        sqlCmd += "a.isam_use_cnt   ,";
        sqlCmd += "a.rowid as rowid1  ";
        sqlCmd += " from ich_b02b_fback a ";
        sqlCmd += "where 1=1 ";
        sqlCmd += "  and (a.proc_flag = 'N' or decode(a.proc_flag,'', 'N',a.proc_flag) = 'N') ";
        sqlCmd += "  and a.ich_card_no  like ? || '%' ";
        setString(1, hIcrdCardCode);
        openCursor();
        while (fetchTable()) {
            hIcdrSearchCode     = getValue("search_code");
            hIcdrIssueStatus    = getValue("issue_status");
            hIcdrRespCodeIssue = getValue("resp_code_issue");
            hIcdrIssueDate      = getValue("issue_date");
            hIcdrIssueTime      = getValue("issue_time");
            hIcdrIcSeqIcah     = getValue("ic_seq_icah");
            hIcdrFormatVer      = getValue("format_ver");
            hIcdrIchCardNo     = getValue("ich_card_no");
            hIcdrIsamUseCnt    = getValue("isam_use_cnt");
            hIcdrRowid           = getValue("rowid1");

            hTnlgRecordCnt++;
            totCnt++;
            if (totCnt % 1000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format(" Process records =[%d]\n", totCnt));

if(debug==1) showLogMessage("I", "", " Detl Cnt=["+totCnt+"]"+hIcdrRespCodeIssue);

            writeRtn();

            updateIchB02bFback();

        } 
        closeCursor();
}
/***********************************************************************/
void writeRtn() throws Exception 
{

        String tmpstr = "";

        detailSt = new buf1();

        detailSt.type = "D";

        tmpstr = String.format("%-6.6s"  , hIcdrSearchCode);
        detailSt.searchCode       = tmpstr;

        detailSt.filler1          = " ";

        tmpstr = String.format("%-6.6s"  , hIcdrIssueStatus);
        detailSt.issueStatus      = tmpstr;

        detailSt.filler2          = " ";

        tmpstr = String.format("%5.5s"   , hIcdrRespCodeIssue);
        detailSt.respCodeIssue   = tmpstr;

        detailSt.filler3          = " ";

        //tmpstr = String.format("%-10.10s", h_icdr_issue_date.substring(0,4)+"-"
        //               +h_icdr_issue_date.substring(4,6)+"-"+h_icdr_issue_date.substring(6,8));
		tmpstr = String.format("%-10.10s", hIcdrIssueDate);
        detailSt.issueDate        = tmpstr;

        detailSt.filler4          = " ";

        //tmpstr = String.format("%-8.8s",   h_icdr_issue_time.substring(0,2)+":"
        //               +h_icdr_issue_time.substring(2,4)+":"+h_icdr_issue_date.substring(4,6));
		
		tmpstr = String.format("%-8.8s", hIcdrIssueTime);
        detailSt.issueTime        = tmpstr;

        detailSt.filler5          = " ";

        tmpstr = String.format("%-14.14s", hIcdrIcSeqIcah);
        detailSt.icSeqIcah    = tmpstr;

        tmpstr = String.format("%8.8s"   , hIcdrFormatVer);
        detailSt.formatVer   = tmpstr;

        tmpstr = String.format("%-16.16s", hIcdrIchCardNo);
        detailSt.ichCardNo    = tmpstr;

        tmpstr = String.format("%2.2s"   , hIcdrIsamUseCnt  );
        detailSt.isamUseCnt     = tmpstr;

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
void updateIchB02bFback() throws Exception 
{
        daoTable   = "ich_b02b_fback";
        updateSQL  = " media_create_date = ?,";
        updateSQL += " media_create_time = ?,";
        updateSQL += " file_name         = ?,";
        updateSQL += " proc_flag         = 'Y',";
        updateSQL += " mod_pgm           = ?,";
        updateSQL += " mod_time          = sysdate";
        whereStr   = "where rowid        = ? ";
        setString(1, hIcdrMediaCreateDate);
        setString(2, hIcdrMediaCreateTime);
        setString(3, hTnlgFileName);
        setString(4, javaProgram);
        setRowId( 5, hIcdrRowid);
        updateTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("update_" + daoTable + " not found!", "",comcr.hCallBatchSeqno);
        }

}
/***********************************************************************/
void insertIchNotifyLog() throws Exception 
{
        setValue("file_iden"        , "B02B");
        setValue("file_name"        , hTnlgFileName);
        setValue("tran_type"        , "I");
        setValue("notify_date"      , hTnlgNotifyDate);
        setValue("notify_time"      , sysTime);
        setValueInt("notify_seq"    , 1);
        setValue("media_create_date", sysDate);
        setValue("media_create_time", sysTime);
        setValueInt("record_cnt"    , hTnlgRecordCnt);
        setValueInt("record_succ"   , hTnlgRecordCnt);
        setValue("mod_pgm"          , javaProgram);
        setValue("mod_time"         , sysDate + sysTime);
        daoTable = "ich_notify_log";
        
        insertTable();
        if (dupRecord.equals("Y")) {
//          comcr.err_rtn("insert_" + daoTable + " duplicate!", "",comcr.h_call_batch_seqno);
        }
}
/***********************************************************************/
public static void main(String[] args) throws Exception 
{
  IchF002 proc = new IchF002();
  int  retCode = proc.mainProcess(args);
  proc.programEnd(retCode);
}
/***********************************************************************/
    class buf1 {
        String type;
        String searchCode;
        String filler1;
        String issueStatus;
        String filler2;
        String respCodeIssue;
        String filler3;
        String issueDate;
        String filler4;
        String issueTime;
        String filler5;
        String icSeqIcah;
        String formatVer;
        String ichCardNo;
        String isamUseCnt;
        String fillerEnd;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(type            ,  1);
            rtn += fixLeft(searchCode     ,  6);
            rtn += fixLeft(filler1        ,  1);
            rtn += fixLeft(issueStatus    ,  6);
            rtn += fixLeft(filler2        ,  1);
            rtn += fixLeft(respCodeIssue ,  5);
            rtn += fixLeft(filler3        ,  1);
            rtn += fixLeft(issueDate      , 10);
            rtn += fixLeft(filler4        ,  1);
            rtn += fixLeft(issueTime      ,  8);
            rtn += fixLeft(filler5        ,  1);
            rtn += fixLeft(icSeqIcah     , 14);
            rtn += fixLeft(formatVer      ,  8);
            rtn += fixLeft(ichCardNo     , 16);
            rtn += fixLeft(isamUseCnt    ,  2);
            rtn += fixLeft(fillerEnd      ,  2);
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
            byte[] bytes = str.getBytes("big5");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);

            return new String(vResult, "big5");
        }
        String fixRight(String str, int len) throws UnsupportedEncodingException {
            String spc = "";
            for (int i = 0; i < 100; i++)
                spc += " ";
            if (str == null)
                str = "";
            str = spc + str;
            byte[] bytes = str.getBytes("big5");
            int offset = bytes.length - len;
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, offset, vResult, 0, len);
            return new String(vResult, "big5");
        }
    }


}
