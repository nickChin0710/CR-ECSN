/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/04/14 V1.01.01  Lai         program initial                            *
*  112/05/10 V1.01.02  Alex        移除HDR檔案                                *
*  112/05/11 V1.01.03  Lai         出檔改為 UTF-8(CARDID.TXT)                 *
*  112/06/06 V1.00.04  Alex        增加FTP至指定資料夾                                                                          * 
******************************************************************************/
package Rsk;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.sql.Connection;
import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;

public class RskP194 extends AccessDAO {
    private String PROGNAME = "CARDID 給債管系統  112/06/06 V1.01.04";
    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;
    CommRoutine    comr  = null;
    CommFTP commFTP = null;
    
    int    DEBUG     = 0;
    String h_temp_user = "";

    int    Report_Page_Line = 45;
    String prgmId    = "RskP194";

    String filename  = "";
//    String rptId_r1  = "CARDID_YYMMDD.HDR";
//    String rptName1  = "CARDID 給債管系統-HEAD";
    int    page_cnt1 = 0, line_cnt1 = 0;
    int    rptSeq1   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String rptId_r2  = "CARDID_YYMMDD.DAT";
    String rptName2  = "CARDID 給債管系統-DATA";
    int    page_cnt2 = 0, line_cnt2 = 0;
    int    rptSeq2   = 0;
    List<Map<String, Object>> lpar2 = new ArrayList<Map<String, Object>>();

    String buf     = "";
    long   tot_cnt = 0;
    long   rec_cnt = 0;
    long   write_cnt = 0;

    String h_busi_business_date = "";
    String h_call_batch_seqno   = "";
    String h_chi_yymmdd         =  "";
    String h_beg_date           =  "";
    String h_end_date           =  "";
    String h_beg_date_bil       =  "";
    String h_end_date_bil       =  "";

    String card_card_no         = "";
    String card_current_code    = "";
    String card_issue_date      = "";
    String card_corp_no         = "";
    String card_group_code      = "";
    String card_bin_type        = "";
    String acno_acct_key        = "";
    String acct_card_indicator  = "";
    double acct_jrnl_bal        = 0;

    String temp_sort_key        = "";
    String temp_name            = "";

    String tmp     = "";
    String tmpstr  = "";
    String tmpstr1 = "";

    buft htail = new buft();
    buf1 data  = new buf1();
/***********************************************************************/
public int mainProcess(String[] args) 
{
 try 
   {
    // ====================================
    // 固定要做的
    dateTime();
    setConsoleMode("Y");
    javaProgram = this.getClass().getName();
    showLogMessage("I", "", javaProgram + " " + PROGNAME + " Args=["+args.length+"]");
 
    // 固定要做的
    if(!connectDataBase()) {
        comc.errExit("connect DataBase error", "");
      }
    // =====================================
    if(args.length > 3) {
       comc.errExit("Usage : RskP194 [yyyymmdd] [seq_no] ", "");
      }
 
    if(comm.isAppActive(javaProgram))
       comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
 
    comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
    comr  = new CommRoutine(getDBconnect()   , getDBalias());
 
    h_call_batch_seqno = args.length > 0 ? args[args.length - 1] : "";
    if(comc.getSubString(h_call_batch_seqno,0,8).equals(comc.getSubString(comc.getECSHOME(),0,8)))
      { h_call_batch_seqno = "no-call"; 
      }
 
    String checkHome = comc.getECSHOME();
    if(h_call_batch_seqno.length() > 6) {
       if(comc.getSubString(h_call_batch_seqno,0,6).equals(comc.getSubString(checkHome,0,6))) 
         {
          comcr.hCallBatchSeqno = "no-call";
         }
      }

    comcr.hCallRProgramCode = javaProgram;
    h_temp_user = "";
    if (comcr.hCallBatchSeqno.length() == 20) {
        comcr.callbatch(0, 0, 1);
        selectSQL = " user_id ";
        daoTable = "ptr_callbatch";
        whereStr = "WHERE batch_seqno   = ?  ";

        setString(1, comcr.hCallBatchSeqno);
        int recCnt = selectTable();
        h_temp_user = getValue("user_id");
    }
    if (h_temp_user.length() == 0) {
        h_temp_user = comc.commGetUserID();
    }

    if (args.length >  0) {
        h_busi_business_date = "";
        if(args[0].length() == 8) {
           h_busi_business_date = args[0];
          } else {
           String ErrMsg = String.format("指定營業日[%s]", args[0]);
          
          }
    }
    select_ptr_businday();
 
    select_crd_card();
 
//    rptId_r1 = String.format("CARDID_%s.HDR",h_busi_business_date.substring(2));

//    filename = String.format("%s/media/rsk/%s" , comc.getECSHOME(), rptId_r1);
//    filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
//    write_head();
//    comcr.insertPtrBatchRpt(lpar1);
//    comc.writeReport(filename, lpar1);

//    rptId_r2 = String.format("CARDID_%s.DAT",h_busi_business_date.substring(2));
    rptId_r2 = String.format("CARDID.TXT");
    filename = String.format("%s/media/rsk/%s" , comc.getECSHOME(), rptId_r2);
    filename = Normalizer.normalize(filename, java.text.Normalizer.Form.NFKD);
    comcr.insertPtrBatchRpt(lpar2);
    comc.writeReport(filename, lpar2 , "UTF8");
    
    //--傳檔
  	commFTP = new CommFTP(getDBconnect(), getDBalias());
  	comr = new CommRoutine(getDBconnect(), getDBalias());
  	procFTP();
  	renameFile();
    
    // ==============================================
    // 固定要做的
    comcr.hCallErrorDesc = "程式執行結束,筆數=[" + tot_cnt + "]";
    showLogMessage("I", "", comcr.hCallErrorDesc);
    if (comcr.hCallBatchSeqno.length() == 20)   comcr.callbatch(1, 0, 1); // 1: 結束

    finalProcess();
    return 0;
  } catch (Exception ex) { expMethod = "mainProcess"; expHandle(ex); return exceptExit;
                         }
}

void procFTP() throws Exception {
	//--DAT
	commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
	commFTP.hEflgSystemId = "ELOAN_FTP_PUT"; /* 區分不同類的 FTP 檔案-大類 (必要) */
	commFTP.hEriaLocalDir = String.format("%s/media/rsk", comc.getECSHOME());
	commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
	commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
	commFTP.hEflgModPgm = javaProgram;

	// System.setProperty("user.dir",commFTP.h_eria_local_dir);
//	showLogMessage("I", "", "mput " + isFileName+"_"+commString.right(sysDate,6)+".DAT" + " 開始傳送....");
//	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + isFileName+"_"+commString.right(sysDate,6)+".DAT");
	showLogMessage("I", "", "mput " + rptId_r2 + " 開始傳送....");
	int errCode = commFTP.ftplogName("ELOAN_FTP_PUT", "mput " + rptId_r2);

	if (errCode != 0) {
//		showLogMessage("I", "", "ERROR:無法傳送 " + isFileName+"_"+commString.right(sysDate,6)+".DAT" + " 資料" + " errcode:" + errCode);
//		insertEcsNotifyLog(isFileName+"_"+commString.right(sysDate,6)+".DAT");
		showLogMessage("I", "", "ERROR:無法傳送 " + rptId_r2 + " 資料" + " errcode:" + errCode);
		insertEcsNotifyLog(rptId_r2);
	}
}

//=====================
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

//=====================
void renameFile() throws Exception {
	String tmpstr3 = String.format("%s/media/rsk/%s", comc.getECSHOME(), rptId_r2);
	String tmpstr4 = String.format("%s/media/rsk/backup/%s", comc.getECSHOME(), rptId_r2+"_"+sysDate);

	if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
//		showLogMessage("I", "", "ERROR : 檔案[" + isFileName+"_"+commString.right(sysDate,6)+".DAT" + "]更名失敗!");
		showLogMessage("I", "", "ERROR : 檔案[" + rptId_r2 + "]更名失敗!");
		return;
	}
//	showLogMessage("I", "", "檔案 [" + isFileName+"_"+commString.right(sysDate,6)+".DAT" + "] 已移至 [" + tmpstr4 + "]");
	showLogMessage("I", "", "檔案 [" + rptId_r2 + "] 已移至 [" + tmpstr4 + "]");
	
}

// ************************************************************************
//void  write_head() throws Exception
//{
//  dateTime();
//
////CARDID_211105.DAT               20211105002021111514320000003564
//  buf = "";
//  buf = comcr.insertStr(buf, rptId_r2                           ,  1);
//  buf = comcr.insertStr(buf, h_busi_business_date               , 33);
//  buf = comcr.insertStr(buf, "00"                               , 41);
//  buf = comcr.insertStr(buf, sysDate+sysTime                    , 43);
//  tmp = String.format("%08d", write_cnt);
//  buf = comcr.insertStr(buf, tmp                                , 57);
//  lpar1.add(comcr.putReport(rptId_r1, rptName1, sysDate, ++rptSeq1, "0", buf+"\r"));
//}
// ************************************************************************
public int  select_ptr_businday() throws Exception 
{

   sqlCmd  = "select to_char(sysdate,'yyyymmdd') as business_date";
   sqlCmd += "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
   sqlCmd += " from ptr_businday ";
   int recordCnt = selectTable();
   if (notFound.equals("Y")) {
       comcr.errRtn("select ptr_businday not found!", "", h_call_batch_seqno);
   }
   if (recordCnt > 0) {
       h_busi_business_date = h_busi_business_date.length() == 0 ? getValue("business_date")
                            : h_busi_business_date;
   }

   sqlCmd  = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm')||'01' h_beg_date_bil ";
   sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date_bil ";
   sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')||'01' h_beg_date ";
   sqlCmd += "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date ";
   sqlCmd += " from dual ";
   setString(1, h_busi_business_date);
   setString(2, h_busi_business_date);
   setString(3, h_busi_business_date);
   setString(4, h_busi_business_date);

   recordCnt = selectTable();
   if(recordCnt > 0) {
      h_beg_date     = getValue("h_beg_date");
      h_end_date     = getValue("h_end_date");
      h_beg_date_bil = getValue("h_beg_date_bil");
      h_end_date_bil = getValue("h_end_date_bil");
     }

// h_chi_yymmdd = getValue("h_chi_yymmdd");
   h_chi_yymmdd = String.format("%07d",comcr.str2long(h_busi_business_date)-19110000);
   showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]" , h_busi_business_date
           , h_chi_yymmdd, h_beg_date, h_end_date , h_beg_date_bil, h_end_date_bil));
   return 0;
}
/***********************************************************************/
void select_crd_card() throws Exception 
{
  String tmp      = "";
        
  selectSQL = " a.card_no             ,a.current_code        , "
            + " d.acct_jrnl_bal       ,a.corp_no             , "
            + " a.group_code          ,c.acct_key            , "
            + " a.bin_type            ,b.card_indicator        ";
  daoTable = "act_acct d, act_acno c, ptr_acct_type  b, crd_card a";
  whereStr = "where 1=1 "
           + "  and b.acct_type    = a.acct_type  "
           + "  and c.acno_p_seqno = a.acno_p_seqno "
           + "  and d.p_seqno      = a.p_seqno    ";
if(DEBUG==1)
  {
   whereStr   = whereStr + "     and a.card_no in ('5241700000113111','5245167700526180','5468580005755827','4563028600770962','3565537110152438','4003538500059629') ";
  }
   whereStr   = whereStr + "order by b.card_indicator, decode(b.card_indicator,'1',c.acct_key,a.corp_no)  ";
  
//setString(1 , h_beg_date);

  openCursor();

  while (fetchTable()) {
     init_rtn();
     rec_cnt++;

     card_card_no        = getValue("card_no"     );
     card_current_code   = getValue("current_code");
     card_corp_no        = getValue("corp_no");
     card_group_code     = getValue("group_code"  );
     card_bin_type       = getValue("bin_type"    );
     acno_acct_key       = getValue("acct_key"   );
     acct_card_indicator = getValue("card_indicator");
     acct_jrnl_bal       = getValueDouble("acct_jrnl_bal");

     if(rec_cnt % 100000 == 0 || rec_cnt == 1)
        showLogMessage("I","",String.format("RskP194 Process 1 record=[%d]\n", rec_cnt));

if(DEBUG==1) showLogMessage("I","","Read card="+card_card_no+" C="+card_current_code +
" B="+acct_jrnl_bal+ " Cnt="+tot_cnt+","+rec_cnt+","+acno_acct_key+","+acct_card_indicator);

     if(card_current_code.compareTo("0") !=0 && acct_jrnl_bal < 1)   continue;
        
     tot_cnt++;
        
     write_file();

    }
}
/***********************************************************************/
void init_rtn() throws Exception 
{
     card_card_no         = "";
     card_current_code    = "";
     card_issue_date      = "";
     card_corp_no         = "";
     card_group_code      = "";
     card_bin_type        = "";
     acct_card_indicator  = "";
     acct_jrnl_bal        = 0;
     acno_acct_key        = "";
}
/***********************************************************************/
void write_file() throws Exception 
{

  tmpstr  = String.format("%s",acno_acct_key.substring(0,10));
  if(acct_card_indicator.compareTo("1") !=0)   
     tmpstr  = String.format("%s",card_corp_no);

  if(temp_sort_key.compareTo(tmpstr) ==0)   return;

  write_cnt++;
  buf = "";
  buf = String.format("%-7.7s,%-10.10s", h_chi_yymmdd, tmpstr);
  lpar2.add(comcr.putReport(rptId_r2, rptName2, sysDate, ++rptSeq2, "0", buf+"\r"));

  temp_sort_key = tmpstr;

  return;
}
/************************************************************************/
public static void main(String[] args) throws Exception 
{
       RskP194 proc = new RskP194();
       int  retCode = proc.mainProcess(args);
       proc.programEnd(retCode);
}
/************************************************************************/
  class buft 
    {
        String filler01;
        String group_code;
        String issue_v_cnt;
        String issue_m_cnt;
        String len;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(filler01     ,  24+1);
            rtn += fixLeft(issue_v_cnt  ,  10+1);
            rtn += fixLeft(issue_m_cnt  ,  10+1);
//          rtn += fixLeft(len, 1);
            return rtn;
        }

        
    }
  class buf1 
    {
        String name;
        String group_code;
        String bin_type;
        String issue_v_cnt;
        String issue_m_cnt;
        String issue_sum;
        String allText() throws UnsupportedEncodingException {
            String rtn = "";
            rtn += fixLeft(name         ,  20+1);
            rtn += fixLeft(group_code   ,  4);
            rtn += fixLeft(issue_v_cnt  ,  10+1);
            rtn += fixLeft(issue_m_cnt  ,  10+1);
            rtn += fixLeft(issue_sum    ,  10+1);
 //         rtn += fixLeft(len          ,  1);
            return rtn;
        }

       
    }
String fixLeft(String str, int len) throws UnsupportedEncodingException {
        int size = (Math.floorDiv(len, 100) + 1) * 100;
        String spc = "";
        for (int i = 0; i < size; i++)    spc += " ";
        if (str == null)                  str  = "";
        str = str + spc;
        byte[] bytes = str.getBytes("MS950");
        byte[] vResult = new byte[len];
        System.arraycopy(bytes, 0, vResult, 0, len);

        return new String(vResult, "MS950");
    }

}
