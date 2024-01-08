/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  109/07/10 V1.00.00   Shiyuqi    program initial                            *
*  109/07/11 V1.00.01   Shiyuqi    bug修改                                    *
*  109/07/13 V1.00.01   Shiyuqi    檔名修改                                   *
*  112/04/19 V1.01.01   Lai        調整程式每日檢核名單卡況                   *
*  112/07/18 V1.01.02   Lai        modify ftp                                 *
*                                                                             *
******************************************************************************/
package Bil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import com.*;

public class BilN008 extends AccessDAO {
  private String progname = "市區免費停車名單-每日  112/04/19 V1.01.01";
  private String prgmId = "BilN008";
  CommFunction    comm  = new CommFunction();
  CommCrd         comc  = new CommCrd();
  CommCrdRoutine  comcr = null;
  CommBonus       comb  = null;

  private String hTempUser = "";
  private int DEBUG        = 0;
  
  private String rptName1  = "TWPARKD8";
  private int    rptSeq1   = 0;
  private List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
  private String rptName2  = "TWPARKD9";
  private int    rptSeq2   = 0;
  private List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();

  private String tmpStr    = "";
  private String buf       = "";
  private String stderr    = "";
  private long   hModSeqno = 0;
  private String hCallBatchSeqno = "";
  private String hBusinessDate   = "";
  private String hSystemDate     = "";
  private String hBusinessPrevMonth = "";

  private String hCardCardNo     = "";
  private String hCurrentCode    = "";
  private String hIdNo           = "";
  private String hRowId          = "";
  private String hNewCardNo      = "";
  private String hNewMajorCardNo = "";
  private String hNewcurrentcode = "";
  private String hFileName1      = "";
  private String hFileName2      = "";
  private int    totCnt = 0;
  // ***********************************************************

  public int mainProcess(String[] args) {
    try {

      // ====================================
      // 固定要做的
      dateTime();
      setConsoleMode("Y");
      javaProgram = this.getClass().getName();
      showLogMessage("I", "", javaProgram + " " + progname);
      // =====================================
      if (args.length > 2) {
        comc.errExit("Usage : BilN008 [YYYYMM] [batch_seq]", "");
      }

      // 固定要做的

      if (!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }

      comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
      comb  = new CommBonus(getDBconnect(), getDBalias());

      comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

      String checkHome = comc.getECSHOME();
      if (comcr.hCallBatchSeqno.length() > 6) {
        if (comc.getSubString(comcr.hCallBatchSeqno, 0, 6).equals(comc.getSubString(checkHome, 0, 6))) {
          comcr.hCallBatchSeqno = "no-call";
        }
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

      if (args.length >  0) {
          hBusinessDate = "";
          if(args[0].length() == 8) {
             hBusinessDate    = args[0];
            } 
      }

      showLogMessage("I","",String.format("參數日期=[%s]\n",hBusinessDate));
      commonRtn();

      selectBilDodoDtlTemp();
      selectBilDodoDtlTempAdd(8);
      selectBilDodoDtlTempAdd(9);
      
      String filenames = "";
      hFileName1 = String.format("%s-%s.txt", rptName1 , hBusinessDate);
      filenames  = String.format("%s/media/bil/%s", comc.getECSHOME(),hFileName1);
      filenames  = Normalizer.normalize(filenames, java.text.Normalizer.Form.NFKD);
      comc.writeReport(filenames, lpar1);

      hFileName2 = String.format("%s-%s.txt", rptName2 , hBusinessDate);
      filenames  = String.format("%s/media/bil/%s", comc.getECSHOME(),hFileName2);
      filenames = Normalizer.normalize(filenames, java.text.Normalizer.Form.NFKD);
      comc.writeReport(filenames, lpar2);

      ftpRtn(hFileName1);
      ftpRtn(hFileName2);

      // ==============================================
      // 固定要做的
      comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "][" + totCnt + "]";
      showLogMessage("I", "", comcr.hCallErrorDesc);
      if(comcr.hCallBatchSeqno.length() == 20)
         comcr.callbatch(1, 0, 1); // 1: 結束
      finalProcess();
      return 0;
    } catch (Exception ex) {
      expMethod = "mainProcess";
      expHandle(ex);
      return exceptExit;
    }
  }
  /***********************************************************************/
  private void commonRtn() throws Exception {
    sqlCmd  = "select business_date,";
    sqlCmd += "substr(to_char(add_months(to_date(business_date,'yyyymmdd'),-1) ,'yyyymmdd'),1,6) h_prev_month ";
    sqlCmd += " from ptr_businday ";
    int recordCnt = selectTable();
    if (recordCnt > 0) {
      hBusinessPrevMonth = getValue("h_prev_month");
      hBusinessDate      = hBusinessDate.length() == 0 ? getValue("business_date")
                         : hBusinessDate;
    } else {
      comcr.errRtn("select_ptr_businday not found!", "", comcr.hCallBatchSeqno);
    }
 
    sqlCmd  = "select to_char(sysdate,'yyyymmdd') h_system_date ";
    sqlCmd += ", substr(to_char(add_months(to_date(?,'yyyymmdd'),-1) ,'yyyymmdd'),1,6) h_prev_month ";
    sqlCmd += " from dual ";
    setString(1, hBusinessDate);
    recordCnt = selectTable();
    if (recordCnt > 0) {
      hSystemDate        = getValue("h_system_date");
      hBusinessPrevMonth = getValue("h_prev_month");
    }

    showLogMessage("I","",String.format("處理日期=[%s][%s]\n",hBusinessDate,hBusinessPrevMonth));

    hModSeqno = comcr.getModSeq();
  }
  /***********************************************************************/
  private void iniRtn() throws Exception {
    hCardCardNo     = "";
    hCurrentCode    = "";
    hIdNo           = "";
    hRowId          = "";
    hNewCardNo      = "";
    hNewMajorCardNo = "";
    hNewcurrentcode = "";
  }
  /***********************************************************************/
  private void selectBilDodoDtlTemp() throws Exception {

    fetchExtend = "temp.";
    sqlCmd  = "select ";
    sqlCmd += "  a.card_no";
    sqlCmd += ", b.id_no";
    sqlCmd += ", c.current_code";
    sqlCmd += ", a.rowid  as rowid ";
    sqlCmd += " from crd_card c, crd_idno b, bil_dodo_dtl_temp a ";
    sqlCmd += "where b.id_p_seqno   = a.id_p_seqno ";
    sqlCmd += "  and c.card_no      = a.card_no    ";
    sqlCmd += "  and a.current_code = '0' "; //where 條件current_code=0
//  sqlCmd += "  and c.current_code = '0' "; //current_code=0,此筆跳過,繼續下一筆檢核.不異動DB 
    sqlCmd += "  and a.aud_type     = 'A' ";
    sqlCmd += "  and (a.acct_month   = ?  or use_month  = ?) ";
    sqlCmd += "order by a.card_no, b.id_no ";
    setString(1, hBusinessPrevMonth);
    setString(2, hBusinessDate.substring(0,6));

    openCursor();

    while (fetchTable()) {
      iniRtn();
      hCardCardNo     = getValue("temp.card_no");
      hCurrentCode    = getValue("temp.current_code");
      hIdNo           = getValue("temp.id_no");
      hRowId          = getValue("temp.rowid");
      
      totCnt++;
      if(totCnt % 10000 == 0 || totCnt == 1)
         showLogMessage("I", "", String.format("Main Process record=[%d]", totCnt));
      if(DEBUG == 1) showLogMessage("I", "", "Read Card="+hCardCardNo + ","+hCurrentCode+", Cnt="+ totCnt);
      if(hCurrentCode.compareTo("0") ==0 )  continue;
         
      updateBilDodoDtlTemp();

      if(selectOldCrdCard() > 0)
         insertBilDodoDtlTemp();

    }

    closeCursor();
  }
  /***********************************************************************/
  private void selectBilDodoDtlTempAdd(int idx) throws Exception {

    writeHead(idx);

    fetchExtend = "file.";
    sqlCmd  = "select ";
    sqlCmd += "  a.card_no";
    sqlCmd += ", b.id_no";
    sqlCmd += ", c.current_code";
    sqlCmd += ", a.rowid  as rowid ";
    sqlCmd += " from crd_card c, crd_idno b, bil_dodo_dtl_temp a ";
    sqlCmd += "where b.id_p_seqno   = a.id_p_seqno ";
    sqlCmd += "  and c.card_no      = a.card_no    ";
    sqlCmd += "  and a.current_code = '0' ";
    if(idx == 8) sqlCmd += "  and a.aud_type     = 'A' ";
    else         sqlCmd += "  and a.aud_type     = 'D' ";
    sqlCmd += "  and (a.acct_month   = ?  or use_month  = ?) ";
    sqlCmd += "  and to_char(a.mod_time,'yyyymmdd')     = ? ";
    sqlCmd += "order by a.card_no, b.id_no ";
    setString(1, hBusinessPrevMonth);
    setString(2, hBusinessDate.substring(0,6));
    setString(3, hBusinessDate);

    openCursor();

    while (fetchTable()) {
      iniRtn();
      hCardCardNo     = getValue("file.card_no");
      hCurrentCode    = getValue("file.current_code");
      hIdNo           = getValue("file.id_no");
      hRowId          = getValue("file.rowid");
      
      totCnt++;
      if(totCnt % 1000 == 0 || totCnt == 1)
         showLogMessage("I", "", String.format("Main Process record=[%d]", totCnt));
      if(DEBUG == 1) showLogMessage("I", "", "Read Card idx="+idx+"," + hCardCardNo + ","+hCurrentCode+", Cnt="+ totCnt);

      writeDtl(idx);
    }

    writeTail(idx);

    closeCursor();
  }
  /***********************************************************************/
  private void writeHead(int idx) throws Exception {
    buf = "";
    buf += fixLeft("1"         ,  1);
    buf += fixLeft("="         ,  1);
    buf += fixLeft(hSystemDate ,  8);
    buf += fixLeft("="         ,  1);
    if(idx == 8)  buf += fixLeft("TcbNew"    ,  6);
    else          buf += fixLeft("TcbDel"    ,  6);
    tmpStr = String.format("%18.18s"," ");
    buf += fixLeft(tmpStr      , 18);

    if(idx == 8)  
       lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1,"0", buf+"\r"));
    else
       lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2,"0", buf+"\r"));
  }
  /***********************************************************************/
  private void writeDtl(int idx) throws Exception {
    buf = "";
    buf += fixLeft("2"         ,  1);
    buf += fixLeft("="         ,  1);
    buf += fixLeft(hIdNo       , 16);
    buf += fixLeft("="         ,  1);
    buf += fixLeft(hCardCardNo , 16);

    if(idx == 8)  
       lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1,"0", buf+"\r"));
    else
       lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2,"0", buf+"\r"));
  }
  /***********************************************************************/
  private void writeTail(int idx) throws Exception {
    buf = "";
    buf += fixLeft("3"         ,  1);
    buf += fixLeft("="         ,  1);
    tmpStr = String.format("%07d",totCnt);
    buf += fixLeft(tmpStr      ,  7);
    tmpStr = String.format("%26.26s"," ");
    buf += fixLeft(tmpStr      , 26);

    if(idx == 8)  
       lpar1.add(comcr.putReport(rptName1, rptName1, sysDate, ++rptSeq1,"0", buf+"\r"));
    else
       lpar2.add(comcr.putReport(rptName2, rptName2, sysDate, ++rptSeq2,"0", buf+"\r"));
  }
  /***********************************************************************/
  private int selectOldCrdCard() throws Exception {
    hNewCardNo    = "";
    hNewMajorCardNo = "";
    hNewcurrentcode = "";
    extendField = "card.";
    sqlCmd  = "select current_code ";
    sqlCmd += "     , card_no    ";
    sqlCmd += "     , major_card_no    ";
    sqlCmd += "  from crd_card   ";
    sqlCmd += " where old_card_no = ?  ";
    setString(1, hCardCardNo);

    int cnt = selectTable();
    if(cnt > 0) {
       hNewCardNo      = getValue("card.card_no");
       hNewMajorCardNo = getValue("card.major_card_no");
       hNewcurrentcode = getValue("card.current_code");
    }
    if(DEBUG == 1) showLogMessage("I", "", "  OLD Card="+hNewCardNo);
    return cnt;
  }
  /***********************************************************************/
  private void insertBilDodoDtlTemp() throws Exception {
    sqlCmd  = " insert into bil_dodo_dtl_temp ( ";
    sqlCmd += " create_date   ,create_time     ,id_p_seqno,card_no ,car_hours   ,tx_date";
    sqlCmd += ",action_cd     ,send_mcht       ,aud_type  ,crt_user,mod_user    ,mod_time";
    sqlCmd += ",mod_pgm       ,id_no           ,group_code,major_id,current_code,acno_p_seqno";
    sqlCmd += ",major_card_no ,major_id_p_seqno,acct_month,use_month,old_card_no ,purchase_date";
    sqlCmd += ",data_from     ,free_cnt        ,chi_name  ,curr_max_amt,curr_tot_cnt,tot_amt";
    sqlCmd += ",consume_method,send_date) ";
    sqlCmd += " SELECT ";
    sqlCmd += " ?         ,?               ,id_p_seqno,?           ,car_hours ,tx_date";
    sqlCmd += ",action_cd ,send_mcht       ,'A'       ,crt_user    ,mod_user  ,to_date(?,'yyyymmdd') ";
    sqlCmd += ",?         ,id_no           ,group_code,major_id    ,?         ,acno_p_seqno";
    sqlCmd += ",?         ,major_id_p_seqno,acct_month,use_month   ,?         ,purchase_date";
    sqlCmd += ",data_from ,free_cnt        ,chi_name  ,curr_max_amt,curr_tot_cnt,tot_amt";
    sqlCmd += ",consume_method,? ";
    sqlCmd += "  from bil_dodo_dtl_temp ";
    sqlCmd += " where rowid  = ? ";
    setString(1, sysDate);
    setString(2, sysTime);
    setString(3, hNewCardNo);
    setString(4, hBusinessDate);
    setString(5, prgmId);
    setString(6, hNewcurrentcode);
    setString(7, hNewMajorCardNo);
    setString(8, hCardCardNo);
    setString(9, sysDate);
    setRowId(10, hRowId);

    insertTable();
    if(dupRecord.equals("Y")) {
  //if(DEBUG==0) comcr.err_rtn("insert_bil_dodo_dtl_temp duplicate!", "", hCardCardNo);
    	comcr.errRtn("insert_bil_dodo_dtl_temp duplicate!", "", hCardCardNo);
    }

  }
  /***********************************************************************/
  private void updateBilDodoDtlTemp() throws Exception {
    updateSQL  = " current_code = ?   , "
               + " aud_type     = 'D' , "
               + " send_date    = ?   , "
               + " mod_pgm      = ? , "
               + " mod_time     = to_date(?,'yyyymmdd') ";
    daoTable   = "bil_dodo_dtl_temp";
    whereStr   = " where rowid  = ? ";
    setString(1, hCurrentCode);
    setString(2, sysDate);
    setString(3, prgmId);
    setString(4, hBusinessDate);
    setRowId( 5, hRowId);
   
    updateTable();
    if(notFound.equals("Y")) {
       comcr.errRtn("update_bil_dodo_dtl_temp not found!", hRowId, hCardCardNo);
    }
  }
  /***********************************************************************/
  private void ftpRtn(String hFileNameI) throws Exception {
    int    errCode  = 0;
    String temstr1  = "";
    String temstr2  = "";
    String procCode = "";
    String hOwsWfValue3 = "";

    sqlCmd  = "select wf_value2 ";
    sqlCmd += " from ptr_sys_parm  ";
    sqlCmd += "where wf_parm = 'SYSPARM'  ";
    sqlCmd += "  and wf_key  = 'CITY_PARK_ZIP_PWD' ";
    selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_sys_parm not found!", "CITY_PARK_ZIP_PWD", comcr.hCallBatchSeqno);
    }
    hOwsWfValue3 = getValue("wf_value2");

    /*** PKZIP 壓縮 ***/
    temstr1 = String.format("%s/media/bil/%s",comc.getECSHOME(), hFileNameI);
    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    temstr2 = String.format("%s.zip",temstr1);
    String filename = temstr1;
    String hPasswd  = hOwsWfValue3;
    String zipFile  = temstr2;
    int tmpInt = comm.zipFile(filename, zipFile, hPasswd);
    if(tmpInt != 0) {
       comcr.errRtn(String.format("無法壓縮檔案[%s]", filename),"", hCallBatchSeqno);
    }

    comc.chmod777(zipFile);

    CommFTP       commFTP = new CommFTP(getDBconnect()    , getDBalias());
    CommRoutine      comr = new CommRoutine(getDBconnect(), getDBalias());

    String  hEflgRefIpCode  = "NCR2TCB";
    commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");
    commFTP.hEflgSystemId   = "NCR2TCB";
    commFTP.hEflgGroupId    = "0000";
    commFTP.hEflgSourceFrom = "EcsFtpBil";
    commFTP.hEflgModPgm     = this.getClass().getName();
    commFTP.hEriaLocalDir   = String.format("%s/media/bil", comc.getECSHOME());
    System.setProperty("user.dir", commFTP.hEriaLocalDir);
    filename  = String.format("%s.zip", hFileNameI);
    procCode  = String.format("put %s", filename);
    showLogMessage("I", "", procCode + ", " + hEflgRefIpCode + " 開始上傳....");
    errCode   = commFTP.ftplogName(hEflgRefIpCode, procCode);
    if (errCode != 0) {
      stderr = String.format("ftp_rtn=[%s]傳檔錯誤 err_code[%d]\n", procCode, errCode);
      showLogMessage("I", "", stderr);
    }
    else
    {
     backFile(filename);
    }
  }
/***************************************************************************/
void backFile(String filename) throws Exception {
   String tmpstr1 = String.format("%s/media/bil/%s", comc.getECSHOME(), filename);
   String tmpstr2 = String.format("%s/media/bil/backup/%s_%s",comc.getECSHOME(),filename,sysDate);

   if (comc.fileRename(tmpstr1, tmpstr2) == false) {
       showLogMessage("I", "", "ERROR : 檔案["+tmpstr1+" to "+tmpstr2+"]備份失敗!");
       return;
   }

   comc.fileDelete(tmpstr1);
   showLogMessage("I", "", "檔案 [" +tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
}
/***************************************************************************/
  private String fixLeft(String str, int len) throws UnsupportedEncodingException {
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
  /***********************************************************************/
  public static void main(String[] args) throws Exception {
    BilN008 proc = new BilN008();
    int retCode = proc.mainProcess(args);
    proc.programEnd(retCode);
  }
  /***********************************************************************/
}
