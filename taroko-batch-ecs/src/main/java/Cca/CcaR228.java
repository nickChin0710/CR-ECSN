/*****************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 *     DATE     Version    AUTHOR                       DESCRIPTION           *
 *  ---------  --------- ----------- ---------------------------------------- *
 *  112-08-07  V1.00.00  Alex        initial                                  *
 *  2023-1127 V1.00.02  jh    runDD=24
 *****************************************************************************/
package Cca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.BaseBatch;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFTP;
import com.CommFunction;
import com.CommRoutine;
import com.CommString;

public class CcaR228 extends BaseBatch {
private final String progname = "負餘額報表產生 2023-1127 V1.00.02";
CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
CommString commString = new CommString();
CommDate commDate = new CommDate();
CommFTP commFTP = null;
CommRoutine comr = null;
CommCrdRoutine comcr = null;

String idPSeqno = "";
String acctType = "";
String pSeqno = "";
String idNo = "";
String chiName = "";
String eMailAddr = "";
String cellarPhone = "";
String cardNo = "";
String oppostDate = "";
double acctJrnlBal = 0;

int sendEmailCnt = 0;
int sendSmsCnt = 0;

int indexCnt = 0;
int pageCnt = 0;

String rptName = "已停用卡片負餘額通知明細表（簡訊、EMAIL）";
String rptId = "CRM228";
int rptSeq = 0;
List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
String buf = "";
String tmp = "";
String szTmp = "";
String hChiDate = "";

public static void main(String[] args) {
   CcaR228 proc = new CcaR228();
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);

   int liArg = args.length;
   if (liArg > 1) {
      printf("Usage : CcaR228 [business_date]");
      okExit(0);
   }

   dbConnect();
   if (liArg == 1) {
      this.setBusiDate(args[0]);
   }

   dateTime();
   String ls_busiDD = commString.right(hBusiDate, 2);
   String ls_runDD = get_runDD(hModPgm);
   if (empty(ls_runDD)) ls_runDD="24";
   if (!eq(ls_busiDD,ls_runDD)) {
      printf("不是每月 %s 日 不執行此程式", ls_runDD);
      endProgram();
      return;
   }

   hChiDate = commDate.toTwDate(hBusiDate);

   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

   procData();

   if (pageCnt > 0) {
      comcr.insertPtrBatchRpt(lpar);
   }

   endProgram();
}

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

   sqlCmd = " select id_p_seqno , acct_type , p_seqno , acct_jrnl_bal from act_acct "
       +" where acct_jrnl_bal <= -131 "
       +" AND NOT EXISTS (SELECT 1 FROM crd_card "
       +" WHERE current_code='0' AND acno_p_seqno=act_acct.p_seqno)"
//       +" and p_seqno not in (select acno_p_seqno from crd_card where current_code ='0') "
   ;

   openCursor();

   printHeader();

   while (fetchTable()) {
      totalCnt++;
      initData();

      idPSeqno = colSs("id_p_seqno");
      acctType = colSs("acct_type");
      pSeqno = colSs("p_seqno");
      acctJrnlBal = colNum("acct_jrnl_bal");
//      //--取得基本資料
//      getBaseData();
//      //--取最後一張卡
//      getLastCardNo();
      selectCardIdno();

      if (empty(eMailAddr) == false)
         sendEmailCnt++;

      if (cellarPhone.length() == 10)
         sendSmsCnt++;

      //分頁控制
      if (indexCnt > 25) {
         lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", "##PPP"));
         printHeader();
         indexCnt = 0;
      }
      printDetail();

   }
   printFooter();
   closeCursor();
}

void printFooter() {

   buf = "";
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   for (int i = 0; i < 126; i++)
      buf += "-";
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   buf = comcr.insertStr(buf, "合  計：發送戶數：簡訊", 4);
   buf = comcr.insertStr(buf, sendSmsCnt+"／Ｅ－ＭＡＩＬ", 30);
   buf = comcr.insertStr(buf, sendEmailCnt+"", 50);
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
}

void printDetail() throws Exception {
   indexCnt++;
   szTmp = comcr.commFormat("z,3z,3z,3#", acctJrnlBal);
   //card_no,id_no,file_date,dest_amt,branch,dest_amt,purchase_date,txn_code
   buf = "";
   buf = comcr.insertStr(buf, chiName, 1);

   buf = comcr.insertStr(buf, cardNo, 10);
   buf = comcr.insertStr(buf, szTmp, 26);
   buf = comcr.insertStr(buf, commDate.dspDate(oppostDate), 42);
   buf = comcr.insertStr(buf, cellarPhone, 56);
   buf = comcr.insertStr(buf, eMailAddr, 71);
   buf = comcr.insertStr(buf, commDate.dspDate(hBusiDate), 111);
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

}

void printHeader() {

   pageCnt++;

   buf = "";
   buf = comcr.insertStr(buf, "分行代號:", 1);
   buf = comcr.insertStr(buf, "3144 信用卡部", 11);
   buf = comcr.insertStrCenter(buf, rptName, 132);
   buf = comcr.insertStr(buf, "保存年限:", 111);
   buf = comcr.insertStr(buf, "五年", 121);
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   buf = comcr.insertStr(buf, "報表代號:", 1);
   buf = comcr.insertStr(buf, rptId, 11);
   tmp = String.format("%s年%2.2s月%2.2s日", hChiDate.substring(0, 3)
       , hChiDate.substring(3, 5), hChiDate.substring(5));
   buf = comcr.insertStrCenter(buf, "中華民國"+tmp, 132);
   buf = comcr.insertStr(buf, "頁    次:", 111);
   szTmp = String.format("%4d", pageCnt);
   buf = comcr.insertStr(buf, szTmp, 121);
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   buf = comcr.insertStr(buf, "姓名", 1);
   buf = comcr.insertStr(buf, "卡號", 14);
   buf = comcr.insertStr(buf, "帳上餘額", 31);
   buf = comcr.insertStr(buf, "卡片停用日期", 41);
   buf = comcr.insertStr(buf, "手機", 56);
   buf = comcr.insertStr(buf, "EMAIL", 71);
   buf = comcr.insertStr(buf, "發送日      ", 111);
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));

   buf = "";
   buf = comcr.insertStr(buf, "=======", 1);
   buf = comcr.insertStr(buf, "================", 10);
   buf = comcr.insertStr(buf, "========", 31);
   buf = comcr.insertStr(buf, "============", 41);
   buf = comcr.insertStr(buf, "==========", 56);
   buf = comcr.insertStr(buf, "====================================", 71);
   buf = comcr.insertStr(buf, "==========", 111);
   lpar.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf));
}
//-----------
int tiCardIdNo=-1;
void selectCardIdno() throws Exception {
   if (tiCardIdNo <=0) {
      sqlCmd ="SELECT A.card_no , A.oppost_date"
          +", B.id_no, B.chi_name, B.e_mail_addr, B.cellar_phone"
//          +", A.sup_flag, A.p_seqno, A.acno_p_seqno"
          +" FROM crd_card A JOIN crd_idno B ON B.id_p_seqno=A.id_p_seqno"
          +" WHERE A.acno_p_seqno =? "
          +" AND A.current_code <> '0' "
          +" ORDER BY A.oppost_date DESC "
          +" FETCH FIRST 1 ROWS ONLY"
          ;
      tiCardIdNo =ppStmtCrt("tiCardIdNo","");
   }
   ppp(1, pSeqno);

   sqlSelect(tiCardIdNo);
   if (sqlNrow <=0) return;

   cardNo = colSs("card_no");
   oppostDate = colSs("oppost_date");
   idNo = colSs("id_no");
   chiName = colSs("chi_name");
   eMailAddr = colSs("e_mail_addr");
   cellarPhone = colSs("cellar_phone");
}
//-----------
int tidCrdIdno = -1;
void getBaseData() throws Exception {
   if (tidCrdIdno <= 0) {
      sqlCmd = " select id_no, chi_name "
          +", e_mail_addr, cellar_phone "
          +" from crd_idno "
          +" where id_p_seqno = ? ";
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

int tidCrdCard = -1;
void getLastCardNo() throws Exception {
   if (tidCrdCard <= 0) {
      sqlCmd = " select card_no , oppost_date from crd_card where acno_p_seqno = ? and current_code <> '0' order by oppost_date Desc fetch first 1 rows only ";
      tidCrdCard = ppStmtCrt("ti-S-crdCard", "");
   }

   setString(1, pSeqno);

   sqlSelect(tidCrdCard);
   if (sqlNrow <= 0) {
      return;
   }

   cardNo = colSs("card_no");
   oppostDate = colSs("oppost_date");

   return;
}

void initData() {
   idPSeqno = "";
   acctType = "";
   pSeqno = "";
   idNo = "";
   chiName = "";
   eMailAddr = "";
   cellarPhone = "";
   acctJrnlBal = 0;
   cardNo = "";
   oppostDate = "";
}


}
