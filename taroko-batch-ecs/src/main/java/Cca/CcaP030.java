/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-07-28  V1.00.00  Alex        initial                                  *
 *  112-08-02  V1.00.01  Alex        檔名加上.dat
 *  2023-0908 V1.00.03  JH    review
 *  2023-1127 V1.00.04  JH    runDD=24
 *****************************************************************************/
package Cca;

import com.*;

public class CcaP030 extends BaseBatch {
private final String progname = "負餘額送簡訊和Email 2023-1127 V1.00.04";
CommCrd comc = new CommCrd();
CommDate commDate = new CommDate();
CommFTP commFTP = null;
CommRoutine comr = null;

private String fileName = "";
private int iiFileNum = 0;
private final static String COL_SEPERATOR = "|&";

String idPSeqno = "";
String acctType = "";
String pSeqno = "";
String idNo = "";
String chiName = "";
String eMailAddr = "";
String cellarPhone = "";

//--簡訊
String msgSeqno = "";
String msgDept = "";
String msgId = "";
String msgUserId = "";
String msgDesc = "";
String smsDate = "";
String smsTime = "140000";

public static void main(String[] args) {
   CcaP030 proc = new CcaP030();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : CcaP030 [business_date]");
      okExit(0);
   }

   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }
   dbConnect();

   String ls_busiDD = commString.right(hBusiDate, 2);
   String ls_runDD = get_runDD(hModPgm);
   if (empty(ls_runDD)) ls_runDD="24";
   if (!eq(ls_busiDD,ls_runDD)) {
      printf("不是每月 %s 日 不執行此程式", ls_runDD);
      endProgram();
      return;
   }

   smsDate = commDate.dateAdd(sysDate, 0, 0, 1);
   fileName = "CARDM10__"+hBusiDate+".dat";

   checkOpen();

   //--取得簡訊
   getSmsMsgId();

   procData();
   sqlCommit();

   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());
   procFTP();
   //--BackUp File
   renameFile();

   endProgram();
}
//===================
String get_runDD(String a_pgm) throws Exception {
   String ls_runDD="";
   sqlCmd ="select id_code as run_dd"
       +" from ptr_sys_idtab"
       +" where wf_type ='BATCH_RUN_DD' "
       +" and upper(wf_id) =?";
   ppp(1,a_pgm.toUpperCase());
   sqlSelect();
   if (sqlNrow >0) {
      ls_runDD =colSs("run_dd");
   }
   return ls_runDD;
}

void procData() throws Exception {

   sqlCmd = " select id_p_seqno , acct_type , p_seqno "
       +" from act_acct "
       +" where acct_jrnl_bal <= -131 "
   +" and not exists (select 1 from crd_card where current_code ='0' AND p_seqno=act_acct.p_seqno)"
//       +" and p_seqno not in (select distinct p_seqno "
//       +" from crd_card where current_code ='0') "
   ;
/*
select id_p_seqno , acct_type , p_seqno
from act_acct A
 where A.acct_jrnl_bal <= -131
 and not exists (select 1 from crd_card where current_code ='0' AND p_seqno=A.p_seqno)
;
select A.id_p_seqno , A.acct_type, A.p_seqno
 , B.id_no, B.chi_name, B.e_mail_addr, B.cellar_phone
 from act_acct A  JOIN crd_idno B ON A.id_p_seqno=B.id_p_seqno
 where A.acct_jrnl_bal <= -131
 and A.p_seqno not in (select DISTINCT p_seqno from crd_card where current_code ='0')
 ;
* */
   openCursor();

   while (fetchTable()) {
      totalCnt++;
      initData();

      idPSeqno = colSs("id_p_seqno");
      acctType = colSs("acct_type");
      pSeqno = colSs("p_seqno");

      //--取得基本資料
      getBaseData();

      if (noEmpty(eMailAddr)) {
         writeEmailText();
      }

      if (cellarPhone.length() == 10) {
         sendSms();
      }

   }
   closeCursor();
   closeOutputText(iiFileNum);
}

void writeEmailText() throws Exception {
   String newLine = "\r\n";
   StringBuffer sb = new StringBuffer();
   sb.append(comc.fixLeft("00", 2));
   sb.append(COL_SEPERATOR);
   sb.append(comc.fixLeft(idNo, 11));
   sb.append(COL_SEPERATOR);
   sb.append(comc.fixLeft("02", 2));
   sb.append(COL_SEPERATOR);
   sb.append(comc.fixLeft(eMailAddr, 30));
   sb.append(COL_SEPERATOR);
   sb.append(comc.fixLeft("合作金庫信用卡負餘額通知", 40));
   sb.append(COL_SEPERATOR);
   sb.append(comc.fixLeft("親愛的"+chiName+"先生（小姐）您好：", 50));
   sb.append(comc.fixLeft(COL_SEPERATOR, 19));
   sb.append(comc.fixLeft(" ", 60));
   sb.append(newLine);
   sb.append(comc.fixLeft("01", 2));
   sb.append(COL_SEPERATOR);
   sb.append("感謝您對合作金庫之支持與愛護，經查您所持有的本行信用卡「已停用」，惟信用卡帳上尚有負餘額，");
   sb.append("<p>");
   sb.append("該款項如屬您自行存入之溢繳款或退貨款（紅利積點產生之負餘額非屬溢繳款），請速洽本行客服中心，辦理退款事宜，以維護您的權益。");
   sb.append(newLine);
   writeTextFile(iiFileNum, sb.toString());
}

void sendSms() throws Exception {
   //--get msg_seqno
   getMsgSeqno();
//   insertSmsMsgDtl();
   insertSms_msg_dtl();
}

int tidSmsMsgId = -1;

void getSmsMsgId() throws Exception {
   if (tidSmsMsgId <= 0) {
      sqlCmd = " select msg_dept , msg_id , msg_userid "
          +" , msg_desc "
          +" from sms_msg_id "
          +" where msg_pgm = 'CCAP030' ";
      tidSmsMsgId = ppStmtCrt("ti-S-smsMsgId", "");
   }

   sqlSelect(tidSmsMsgId);
   if (sqlNrow <= 0) {
      return;
   }

   msgDept = colSs("msg_dept");
   msgId = colSs("msg_id");
   msgUserId = colSs("msg_userid");
   msgDesc = colSs("msg_desc");
}
//------------------
com.Parm2sql ttAsms=null;
void insertSms_msg_dtl() throws Exception {
   String tmp = "";
   tmp = msgUserId+","+msgId+","+cellarPhone;

   if (ttAsms ==null) {
      ttAsms =new com.Parm2sql();
      ttAsms.insert("sms_msg_dtl");
   }
   ttAsms.aaa("msg_seqno"                  , msgSeqno);    //-簡訊流水號--
   ttAsms.aaa("msg_dept"                   , msgDept);    //-簡訊部門--
   ttAsms.aaa("msg_userid"                 , msgUserId);    //-簡訊發送者--
   ttAsms.aaa("msg_pgm"                    , "CCAP030");    //-簡訊程式代號--
   ttAsms.aaa("id_p_seqno"                 , idPSeqno);    //-帳戶流水號碼--
   ttAsms.aaa("p_seqno"                    , pSeqno);    //-帳號流水號--
   ttAsms.aaa("id_no"                      , idNo);    //-持卡者ID--
   ttAsms.aaa("acct_type"                  , acctType);    //-帳戶帳號類別碼--
//   ttAsms.aaa("card_no"                    , hh.card_no);    //-卡號--
   ttAsms.aaa("msg_id"                     , msgId);    //-簡訊代號--
   ttAsms.aaa("cellar_phone"               , cellarPhone);    //-行動電話--
   ttAsms.aaa("cellphone_check_flag"       , "Y");    //-行動電話旗標 Y=有行動電話,N=無行動電話,--
   ttAsms.aaa("chi_name"                   , chiName);    //-持卡者姓名--
//   ttAsms.aaa("ex_id"                      , hh.ex_id);    //-說明代碼--
   ttAsms.aaa("msg_desc"                   , tmp);    //-簡訊內容--
//   ttAsms.aaa("min_pay"                    , hh.min_pay);    //-對帳單上最低應繳款--
   ttAsms.aaa("add_mode"                   , "B");    //-新增模式--
//   ttAsms.aaa("resend_flag"                , hh.resend_flag);    //-重新傳送旗標 Y=重發 N=未發--
   ttAsms.aaa("send_flag"                  , "Y");    //-傳送旗標 Y=重發 N=未發--
   ttAsms.aaa("proc_flag"                  , "N");    //-處理註記--
//   ttAsms.aaa("sms24_flag"                 , hh.sms24_flag);    //-即時簡訊註記--
   ttAsms.aaa("crt_date"                   , sysDate);    //-鍵檔日期--
   ttAsms.aaa("crt_user"                   , "ecs");    //-鍵檔人員--
   ttAsms.aaa("apr_date"                   , sysDate);    //-主管覆核日期--
   ttAsms.aaa("apr_user"                   , "ecs");    //-主管覆核人員--
   ttAsms.aaa("apr_flag"                   , "Y");    //-主管覆核註記--
   ttAsms.aaa("booking_date"               , smsDate);    //-預約簡訊發送日期--
   ttAsms.aaa("booking_time"               , smsTime);    //-預約簡訊發送時間--
   ttAsms.aaaModxxx(hModUser, hModPgm);

   if (ttAsms.ti <=0) {
      ttAsms.ti =ppStmtCrt("ttAsms", ttAsms.getSql());
   }

   sqlExec(ttAsms.ti, ttAsms.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert sms_msg_dtl error");
      errExit(1);
   }
}
//--------
void insertSmsMsgDtl() throws Exception {
   String tmp = "";
   tmp = msgUserId+","+msgId+","+cellarPhone;
   daoTable = "sms_msg_dtl";
   setValue("msg_seqno", msgSeqno);
   setValue("msg_dept", msgDept);
   setValue("msg_userid", msgUserId);
   setValue("msg_pgm", "CCAP030");
   setValue("id_p_seqno", idPSeqno);
   setValue("p_seqno", pSeqno);
   setValue("id_no", idNo);
   setValue("acct_type", acctType);
   setValue("msg_id", msgId);
   setValue("msg_desc", tmp);
   setValue("cellar_phone", cellarPhone);
   setValue("cellphone_check_flag", "Y");
   setValue("chi_name", chiName);
   setValue("add_mode", "B");
   setValue("send_flag", "Y");
   setValue("crt_date", sysDate);
   setValue("crt_user", "ecs");
   setValue("apr_date", sysDate);
   setValue("apr_user", "ecs");
   setValue("apr_flag", "Y");
   setValue("booking_date", smsDate);
   setValue("booking_time", smsTime);
   insertTable();
}

void initData() {
   idPSeqno = "";
   acctType = "";
   pSeqno = "";
   idNo = "";
   chiName = "";
   eMailAddr = "";
   cellarPhone = "";
}

int tidCrdIdno = -1;

void getBaseData() throws Exception {
   if (tidCrdIdno <= 0) {
      sqlCmd = " select id_no , chi_name , e_mail_addr , cellar_phone "
          +" from crd_idno where id_p_seqno = ? ";
      tidCrdIdno = ppStmtCrt("ti-S-crdIdno", "");
   }

   setString(1, idPSeqno);

   sqlSelect(tidCrdIdno);
   if (sqlNrow <= 0) {
      return;
   }

   idNo = colSs("id_no");
   chiName = colSs("chi_name");
   eMailAddr = colSs("e_mail_addr");
   cellarPhone = colSs("cellar_phone");

   return;
}

int tidDual = -1;
void getMsgSeqno() throws Exception {
   if (tidDual <= 0) {
      sqlCmd = " select lpad(to_char(ecs_modseq.nextval),10,'0') as msg_seqno from dual ";
      tidDual = ppStmtCrt("ti-S-dual", "");
   }

   sqlSelect(tidDual);
   if (sqlNrow <= 0) {
      return;
   }

   msgSeqno = colSs("msg_seqno");

   return;
}

//--產生檔案
void checkOpen() throws Exception {
   String lsFile = String.format("/cr/ecs/media/cca/%s", fileName);

   iiFileNum = openOutputText(lsFile, "MS950");
   if (iiFileNum < 0) {
      showLogMessage("I", "", "CARDM10 產檔失敗 !");
      okExit(0);
   }

   return;
}

//--FTP
void procFTP() throws Exception {
   commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = String.format("%s/media/cca", comc.getECSHOME());
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   // System.setProperty("user.dir",commFTP.h_eria_local_dir);
   showLogMessage("I", "", "mput "+fileName+" 開始傳送....");
   int errCode = commFTP.ftplogName("NCR2TCB", "mput "+fileName);
   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 "+fileName+" 資料"+" errcode:"+errCode);
      insertEcsNotifyLog(fileName);
   }
}

public int insertEcsNotifyLog(String fileName) throws Exception {
   setValue("crt_date", sysDate);
   setValue("crt_time", sysTime);
   setValue("unit_code", comr.getObjectOwner("3", javaProgram));
   setValue("obj_type", "3");
   setValue("notify_head", "無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_name", "媒體檔名:"+fileName);
   setValue("notify_desc1", "程式 "+javaProgram+" 無法 FTP 傳送 "+fileName+" 資料");
   setValue("notify_desc2", "");
   setValue("trans_seqno", commFTP.hEflgTransSeqno);
   setValue("mod_time", sysDate+sysTime);
   setValue("mod_pgm", javaProgram);
   daoTable = "ecs_notify_log";
   insertTable();
   return (0);
}

void renameFile() throws Exception {
   String tmpstr3 = String.format("%s/media/cca/%s", getEcsHome(), fileName);
   String tmpstr4 = String.format("%s/media/cca/backup/%s", getEcsHome(), fileName+"_"+sysDate);

   if (comc.fileRename2(tmpstr3, tmpstr4) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+fileName+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+fileName+"] 已移至 ["+tmpstr4+"]");
}

}
