package Cms;
/**
 * 每日收全鋒文字檔產生處理程式(batch)
 * 2020-02   Rou    Initial
 * 109/12/07  V1.00.01    shiyuqi       updated for project coding standard   *
 * 2023-0504  V1.00.02		JH		modify:
 * 2023-1013  V1.00.03     JH    檔名:TMSRCVD1.txt 改為 006TMSR_YYYYMM.TXT
 * 2023-1018  V1.00.04     JH    data length >=30
 * 2023-1103  V1.00.05     Zuwei Su  弱掃Issue
 */

import com.*;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Random;

public class CmsB012 extends BaseBatch {
private String progname = "每日收全鋒文字檔產生處理程式(batch)  2023-1018  V1.00.04";
CommFunction comm = new CommFunction();
CommCrd comc = new CommCrd();
//CommRoutine comr = null;
CommCrdRoutine comcr = null;
//=============================================
//hdata.CmsRoadmaster hRoad =new hdata.CmsRoadmaster();
//hdata.CmsRoaddetail hRode =new hdata.CmsRoaddetail();
hdata.EcsRefIpAddr hEria = new hdata.EcsRefIpAddr();
HH hh = new HH();

//-----------
class HH {
   //-Txt-
   String diff_no = "";
   String deal_no = "";
   String rds_pcard = "";
   String tx_card_no = "";
   String tx_idno = "";
   String car_no = "";
   String shift_date = "";
   String chi_name = "";
   //---------
   String err_code = "";
   String err_reason = "";
   //----
   String card_no = "";
   String id_no = "";
   String id_pseqno = "";
   String current_code = "";
   String group_code = "";
   String maj_id_Pseqno = "";
   String maj_card_no = "";
   String acno_Pseqno = "";
   String acct_month="";

   void init_data() {
      diff_no = "";
      deal_no = "";
      rds_pcard = "";
      tx_card_no = "";
      tx_idno = "";
      car_no = "";
      shift_date = "";
      chi_name = "";
      //---------
      err_code = "00";
      err_reason = "";
      //----
      card_no = "";
      id_no = "";
      id_pseqno = "";
      current_code = "";
      group_code = "";
      maj_id_Pseqno = "";
      maj_card_no = "";
      acno_Pseqno = "";
      acct_month="";
   }
}

//--
//String getFileName = "";
String fileName="";
String fileType = "";
String txtFile = "";
String zipFile = "";
String hDiffNo = "";
String hDealNo = "";
String hRdsPcard = "";
String hIdNo = "";
String hCardNo = "";
String hCarNo = "";
String hShiftDate = "";
String hChiName = "";
int hCmsRLogSeqno;

String hCrdCardCardNo = "";
String hCrdCardIdPSeqno = "";
String hCrdCardGroupCode = "";

String hCrdIdnoIdNo = "";
String hCrdIdnoF3IdNo = "";
String hCrdIdnoB4IdNo = "";
long tempLong;
int commit = 1;
int errorTmp = 0;
int readCnt = 0;
protected final String dt1Str = "diff_no, deal_no, rds_pcard, id_no, card_no, car_no, shift_date, chi_name";
protected final int[] dt1Length = {1, 1, 1, 10, 9, 8, 8, 12};
protected String[] dt1 = new String[]{};
//-------
int iiSeqNo = 0;
int iiSeqNoLog = 0;
String pp_acctMonth ="";
String is_fileYYYYMM="";
//=*****************************************************************************
public static void main(String[] args) {
   CmsB012 proc = new CmsB012();
   proc.mainProcess(args);
   proc.systemExit(0);
}

//==============================================================================
@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(progname);
   dt1 = dt1Str.split(",");
   int liArg = args.length;
   if (liArg > 2) {
      printf("Usage : CmsB012 [acctMonth,callbatch_seqno]");
      okExit(0);
   }

   if (liArg >0 && args[0].length() ==6) {
      if (commDate.isDate(args[0]+"01")) {
         pp_acctMonth =args[0];
      }
   }
   if (liArg > 0) {
      callBatchSeqno(args[liArg-1]);
   }

   printf("input Param.acctMonth=[%s]", pp_acctMonth);

   dbConnect();
   callBatch(0, 0, 0);

   getRdSeqno();
   selectInputFile();

   sqlCommit(commit);
   endProgram();
}

//=============================================================================
void getRdSeqno() throws Exception {
   sqlCmd = "select max(rd_seqno) as rd_seqno"+
       " from cms_roaddetail"+
       " where rd_moddate =?"
   ;
   ppp(1, sysDate);
   sqlSelect(sqlCmd);
   if (sqlNrow <= 0) {
      iiSeqNo = 0;
   } else {
      iiSeqNo = colInt("rd_seqno");
   }
   //--
   sqlCmd = "select max(seqno) as log_seqno"+
       " from cms_roadmaster_log"+
       " where moddate =?"
   ;
   ppp(1, sysDate);
   sqlSelect(sqlCmd);
   if (sqlNrow <= 0) {
      iiSeqNoLog = 0;
   } else {
      iiSeqNoLog = colInt("log_seqno");
   }

}

void selectInputFile() throws Exception {
   String filepath = comc.getECSHOME()+"/media/cms/";
   filepath = Normalizer.normalize(filepath, java.text.Normalizer.Form.NFKD);
   List<String> listOfFiles = comc.listFS(filepath, "", "");
   if (listOfFiles.size() == 0) {
	   printf(String.format("[%s]無檔案可處理!!", filepath));
	   return;
   }
   //TMSRCVD1.txt 改為 006TMSR_YYYYMM.TXT--
   for (String ls_file : listOfFiles) {
//      if (!file.substring(0, 8).equals("TMSRCVD1"))
//         continue;
      String ss=commString.left(ls_file,7);
      if (!eqIgno(ss,"006TMSR")) continue;
      fileName =ls_file;
      fileType = commString.right(ls_file, 4);
      is_fileYYYYMM =commString.mid(ls_file,8,6);
      printf("檔案 ==> [   "+filepath+ls_file+" ] ");
      if (eq(fileType,".zip")) {
         printf("----------"+" 此檔案類型為zip "+"----------");
         /* PKZIP 解壓縮 */
         zipFile = comc.getECSHOME()+"/media/cms/"+ls_file;
         zipFile = Normalizer.normalize(zipFile, java.text.Normalizer.Form.NFKD);
         printf(String.format("解壓縮 = [%s]", zipFile));
         printf("----------"+" 解壓縮中... "+"----------");
         comm.unzipFile(zipFile, filepath, "");
         txtFile = comc.getECSHOME()+"/media/cms/"+ls_file.replaceAll(".zip",".txt");
         txtFile = Normalizer.normalize(txtFile, java.text.Normalizer.Form.NFKD);
         renameFileZip(ls_file);
         checkOpen(txtFile);
      } else if (eq(fileType,".txt")) {
         printf("----------"+" 此檔案類型為txt "+"----------");
         txtFile = comc.getECSHOME()+"/media/cms/"+ls_file;
         txtFile = Normalizer.normalize(txtFile, java.text.Normalizer.Form.NFKD);
         checkOpen(txtFile);
      } else {
//         printf("----------"+" 無檔案可處理 "+"----------");
         printf(" 檔案[%s]不是zip or txt", ls_file);
         continue;
      }
      renameFile(ls_file.replaceAll(".zip",".txt"));
   }
}
//=============================================================================
int checkOpen(String a_txtFile) throws Exception {
   int fi = openInputText(a_txtFile);
   if (fi <0) {
      printf(" can not open file[%s]",a_txtFile);
      return 1;
   }
   //-acct_month:006TMSR_YYYYMM.TXT--
   String ls_acctMonth="";
   if (!empty(pp_acctMonth)) ls_acctMonth=pp_acctMonth;
   else {
      ls_acctMonth =is_fileYYYYMM;
      if (!commDate.isDate(ls_acctMonth+"01")) ls_acctMonth="";
   }
   if (empty(ls_acctMonth)) ls_acctMonth =commString.left(hBusiDate,6);

   while (readCnt < 999999) {
      String rec = readTextFile(fi); //read file data
      int recLen = commString.bbLen(rec);
      if (recLen == 0) {
         if (endFile[fi].equals("Y"))
            break;
      }
      readCnt++;
      //-取消長度-
      if (recLen <30) {
         printf(" 此檔案第 "+readCnt+" 筆資料長度[%s]不正確，存檔失敗 ", recLen);
         continue;
      }
//      if (recLen == 50) {
//         if (!rec.substring(0, 1).equals("2")) {
//            printf(" 此檔案第 "+readCnt+" 筆區別碼不正確，存檔失敗 "); //diff_no need be 2
//            continue;
//         }
//      } else if (recLen == 51) {
//         if (!rec.substring(1, 2).equals("2")) {
//            printf(" 此檔案第 "+readCnt+" 筆區別碼不正確，存檔失敗 "); //diff_no need be 2
//            continue;
//         }
//      } else {
//         printf(" 此檔案第 "+readCnt+" 筆資料長度不正確，存檔失敗 ");
//         continue;
//      }

//        	byte[] bt = rec.getBytes("MS950");
//        	int temp = moveData(processDataRecord(getFieldValue(rec, dt1Length, rec.length()), dt1));
//        	if (errorTmp == 1)
//        		return errorTmp;
//        	else
//        		totalCnt++;
      totalCnt++;
      processDisplay(1000);
      hh.init_data();
      hh.acct_month =ls_acctMonth;
      text2Field(rec);
      //-2.明細--
      if (!eq(hh.diff_no,"2")) continue;
      textCheck();

      if (empty(hh.err_code) || eq(hh.err_code, "00")) {
         insertCmsRoaddetail();
         insertCmsRoadmaster();
         insertCmsRoadList();
      }
      else if (eq(hh.err_code,"11")) {
         //-卡號["+hh.card_no+"]己存在道路救援主檔-
         insertCmsRoadList();
      }
      insertCmsRoadmasterLog();
   }
   closeInputText(fi);

   sqlCommit();
   return 0;
}

void text2Field(String aTxt) throws Exception {
//	protected final String dt1Str = "diff_no, deal_no, rds_pcard, id_no, card_no, car_no, shift_date, chi_name";
//	protected final int[] dt1Length = { 1, 1, 1, 10, 9, 8, 8, 12};
   String[] aaTxt = new String[]{aTxt, ""};
   hh.diff_no = commString.bbToken(aaTxt, 1).trim();
   hh.deal_no = commString.bbToken(aaTxt, 1).trim();
   hh.rds_pcard = commString.bbToken(aaTxt, 1).trim();
   hh.tx_idno = commString.bbToken(aaTxt, 10).trim();
   hh.tx_card_no = commString.bbToken(aaTxt, 9).trim();
   hh.car_no = commString.bbToken(aaTxt, 8).trim();
   hh.shift_date = commString.bbToken(aaTxt, 8).trim();
   hh.chi_name = commString.bbToken(aaTxt, 12).trim();
}

void textCheck() throws Exception {
   hh.err_code = "00";
   rc = -1;
   if (empty(hh.tx_card_no)) {
      hh.err_code = "01";
      hh.err_reason = "卡號空白";
      return;
   }
   if (empty(hh.tx_idno)) {
      hh.err_code = "02";
      hh.err_reason = "ID空白";
      return;
   }
   if (empty(hh.car_no)) {
      hh.err_code = "03";
      hh.err_reason = "車號空白";
      return;
   }

   selectCrdCardIdno();
//	selectCrdIdno();
   if (empty(hh.id_pseqno)) {
      hh.err_code = "06";
      hh.err_reason = "ID["+hh.tx_idno+"]不存在卡人檔";
      return;
   }
//	selectCrdCard();
   if (empty(hh.card_no)) {
      hh.err_code = "04";
      hh.err_reason = "卡號["+hh.tx_card_no+"]不存在卡檔";
      return;
   }
   if (!eq(hh.current_code, "0")) {
      hh.err_code = "05";
      hh.err_reason = "卡號["+hh.card_no+"]不是有效卡";
      return;
   }
   if (commString.ssIn(hh.rds_pcard, ",I,V,P,L") == false) {
      hh.err_code = "07";
      hh.err_reason = "優惠別不是=I,V,P,L";
      return;
   }
   int llCnt = selectCmsRoadmaster();
   if (llCnt > 0) {
      hh.err_code = "11";
      hh.err_reason = "卡號["+hh.card_no+"]己存在道路救援主檔";
   }

   //
   //	hDealNo = (String) map.get("deal_no");
   //	showLogMessage("D", "", "交易別 =  [" + hDealNo + "] ");
   //	if (!hDealNo.trim().equals("N")) {
   //		showLogMessage("D", "", "交易別不為N !  存檔失敗 ! ! ! ");
   //		errorTmp = insertCmsRoadmasterLog(4);
   //		return errorTmp;
   //	}
   //
   //	sqlCmd  = "select substr(card_no, 8) card_no_s, card_no, id_p_seqno, group_code ";
   //	sqlCmd += "from crd_card ";
   //	sqlCmd += "where current_code = '0' ";
   //	sqlCmd += "and substr(card_no, 8) = ? ";
   //	setString(1, hCardNo);
   //	recordCnt = selectTable();
   //	if(recordCnt > 0)  {
   //		hCrdCardCardNo = getValue("card_no");
   //		hCrdCardIdPSeqno = getValue("id_p_seqno");
   //		hCrdCardGroupCode = getValue("group_code");
   //	}
   //	else {
   //		showLogMessage("D", "", "卡號後9碼:" + hCardNo + "不存在 !");
   //		errorTmp = insertCmsRoadmasterLog(5);
   //		return errorTmp;
   //	}
   //
   //	sqlCmd  = "select substr(id_no, 1, 3) f_3_id_no, substr(id_no, 7, 4) b_4_id_no, id_no, id_p_seqno  ";
   //	sqlCmd += "from crd_idno ";
   //	sqlCmd += "where id_p_seqno = ? ";
   //	setString(1, hCrdCardIdPSeqno);
   //	if (selectTable() > 0) {
   //		hCrdIdnoF3IdNo = getValue("f_3_id_no");
   //		hCrdIdnoB4IdNo = getValue("b_4_id_no");
   //		hCrdIdnoIdNo = getValue("id_no");
   //		if (hIdNo.substring(0, 3).equals(hCrdIdnoF3IdNo) && hIdNo.substring(6, 10).equals(hCrdIdnoB4IdNo))
   //			checkData();
   //		else{
   //			showLogMessage("D", "", "持卡人不存在卡人檔 ! !");
   //			errorTmp = insertCmsRoadmasterLog(6);
   //			return errorTmp;
   //		}
   //	}
   //    return 0;
}
//--------------
int tiRdsM = -1;
int selectCmsRoadmaster() throws Exception {
   if (tiRdsM <= 0) {
      sqlCmd = "select count(*) rds_cnt"+
          " from cms_roadmaster"+
          " where card_no =?"+
          "";
      tiRdsM = ppStmtCrt("ti-rdsm-S", "");
   }

   ppp(1, hh.card_no);
   sqlSelect(tiRdsM);
   if (sqlNrow <= 0) return 0;
   return colInt("rds_cnt");
}

int tiCard = -1;

void selectCrdCardIdno() throws Exception {
   if (tiCard <= 0) {
      sqlCmd = "select "+
          " A.card_no, A.current_code, A.group_code, A.id_p_seqno"+
          ", A.major_card_no, A.major_id_p_seqno, A.acno_p_seqno"+
          ", B.id_no, B.chi_name"+
          " from crd_idno B left join crd_card A ON A.id_p_seqno=B.id_p_seqno"+
          " where 1=1"+
          " AND B.id_no LIKE ?"+
          " and A.card_no like ?"+
          " order by A.current_code, B.id_no, B.id_no_code"+
          "";
      tiCard = ppStmtCrt("ti-card-S", "");
   }
   String lsIdno = commString.left(hh.tx_idno, 3)+"%"+commString.right(hh.tx_idno, 4);
   ppp(1, lsIdno);
   ppp("%"+hh.tx_card_no);
   sqlSelect(tiCard);
   if (sqlNrow <= 0) {
      return;
   }
   for (int ll = 0; ll < sqlNrow; ll++) {
      hh.id_pseqno = colSs(ll, "id_p_seqno");
      hh.id_no = colSs(ll, "id_no");

      String lsCardNo9 = commString.right(colSs(ll, "card_no"), 9);
      if (eq(hh.tx_card_no, lsCardNo9)) {
         hh.card_no = colSs(ll, "card_no");
         hh.current_code = colSs(ll, "current_code");
         hh.group_code = colSs(ll, "group_code");
         hh.maj_card_no = colSs("major_card_no");
         hh.maj_id_Pseqno = colSs("major_id_p_seqno");
         hh.acno_Pseqno = colSs("acno_p_seqno");
         break;
      }
   }
}

//************************************************************************
void renameFileZip(String filename) throws Exception {
    SecureRandom rand = new SecureRandom();
   StringBuffer buf = new StringBuffer();
   for (int i = 0; i <= 3; i++)
      buf.append(rand.nextInt(10));

   String zipFile1 = zipFile;
   String zipFile2 = comc.getECSHOME()+"/media/cms/backup/"+filename+"."+sysDate+buf;
   if (comc.fileRename2(zipFile1, zipFile2) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+filename+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+filename+"] 已移至 ["+zipFile2+"]");
   return;

}

//************************************************************************
void renameFile(String filename) throws Exception {
    SecureRandom rand = new SecureRandom();
   StringBuffer buf = new StringBuffer();
   for (int i = 0; i <= 3; i++)
      buf.append(rand.nextInt(10));

   String tmpstr1 = txtFile;
   String tmpstr2 = comc.getECSHOME()+"/media/cms/backup/"+filename+"."+sysDate+buf;
   if (comc.fileRename2(tmpstr1, tmpstr2) == false) {
      showLogMessage("I", "", "ERROR : 檔案["+filename+"]更名失敗!");
      return;
   }
   showLogMessage("I", "", "檔案 ["+filename+"] 已移至 ["+tmpstr2+"]");
}

//************************************************************************
com.Parm2sql ttRdsLA = null;
void insertCmsRoadmasterLog() throws Exception {
   if (ttRdsLA == null) {
      ttRdsLA = new Parm2sql();
      ttRdsLA.insert("cms_roadmaster_log");
   }

   String lsCardNo = hh.card_no;
   if (empty(hh.card_no)) lsCardNo = hh.tx_card_no;
   String lsIdno = (empty(hh.id_no) ? hh.tx_idno : hh.id_no);

   iiSeqNoLog++;

   ttRdsLA.aaaYmd("moddate");  //	VARCHAR (8,0)	異動日期
   ttRdsLA.aaa("seqno"           , iiSeqNoLog);   //	DECIMAL (9,0)	序號
   ttRdsLA.aaa("card_no"         , lsCardNo);  //VARCHAR (19,0)	卡號
   ttRdsLA.aaa("rds_pcard"       , hh.rds_pcard);  //VARCHAR (1,0)	免費道路救援 A:自動登錄+免費; I:50公里免費拖吊; P:30公里免費拖吊 L-租賃車
   ttRdsLA.aaa("rm_carno"        , hh.car_no);  //VARGRAPH(10,0)	車號
   ttRdsLA.aaa("rm_carmanname"   , hh.chi_name);  //VARGRAPH(20,0)	車主姓名
   ttRdsLA.aaa("rm_carmanid"     , lsIdno);  //VARCHAR (20,0)	身份証號
   ttRdsLA.aaa("id_p_seqno"      , hh.id_pseqno);   //VARCHAR (10,0)	卡人流水號碼
   ttRdsLA.aaa("err_reason"      , hh.err_reason);  //	VARGRAPH(100,0)	失敗原因
   ttRdsLA.aaaDtime("mod_time");  //TIMESTMP(10,6)	異動時間
   ttRdsLA.aaa("mod_pgm"         , hModPgm);  //VARCHAR (20,0)	異動程式
   ttRdsLA.aaa("err_code"        , hh.err_code);  //	VARCHAR (2,0)	錯誤碼
   ttRdsLA.aaa("imp_file_name"   ,fileName);   //	VARCHAR (50,0)	匯入檔名
   ttRdsLA.aaa("mod_type", "B");  //	VARCHAR (1,0)	異動類別

   if (ttRdsLA.ti <= 0) {
      ttRdsLA.ti = ppStmtCrt("tt-rdsL-A", ttRdsLA.getSql());
   }

   sqlExec(ttRdsLA.ti, ttRdsLA.getParms());
   if (sqlNrow <= 0) {
      sqlerr("insert cms_roadmaster_log error");
      okExit(0);
   }
}

//-----------
com.Parm2sql ttListA = null;
void insertCmsRoadList() throws Exception {
   if (ttListA == null) {
      ttListA = new Parm2sql();
      ttListA.insert("cms_roadlist");
   }

   ttListA.aaa("major_card_no", hh.maj_card_no);  //VARCHAR (19,0)	正卡卡號
   ttListA.aaa("card_no"      , hh.card_no);  //VARCHAR (19,0)	卡號
   ttListA.aaa("major_id_p_seqno", hh.maj_id_Pseqno);  //VARCHAR (10,0)	正卡ID流水號
   ttListA.aaa("acno_p_seqno" , hh.acno_Pseqno);  //VARCHAR (10,0)	帳戶流水號
   ttListA.aaa("proj_no"      , "");  //VARCHAR (10,0)	專案代號
   //005	year_type           	VARCHAR (1,0)	消費年度類別
   ttListA.aaa("rm_status"    , "1");  //VARCHAR (1,0)	道路救援狀態
   //007	proc_flag           	VARCHAR (1,0)	處理註記       1:成功 2:退件 3:分期 N:未處理
   //008	proc_date           	VARCHAR (8,0)	處理日期
   ttListA.aaa("purch_amt"    , 0);  //DECIMAL (14,3)	上年度累積消費額
   //010	purch_row           	DECIMAL (14,3)	上年度累積消費筆數
   ttListA.aaa("tol_amt"      , 0);  //DECIMAL (14,3)	消費總金額
   ttListA.aaa("curr_tot_cnt" , 0);  //INTEGER (4,0)	"累積消費筆數 	"
   //012	end_card_no         	VARCHAR (19,0)	最後卡號
   ttListA.aaa("id_p_seqno"   , hh.id_pseqno);  //VARCHAR (10,0)	"卡人流水號碼 	"
   ttListA.aaa("rds_pcard"    , hh.rds_pcard);  //VARCHAR (1,0)	免費道路救援類別
   //017	current_code        	VARCHAR (1,0)	狀態碼
   //018	issue_date          	VARCHAR (8,0)	核卡日期
   ttListA.aaa("rm_carmanid"  , hh.id_no);  //VARCHAR (20,0)	車主身份証號
   ttListA.aaa("rm_carno"     , hh.car_no);  //VARCHAR (10,0)	車號
   ttListA.aaa("mod_type", "N");  //VARCHAR (1,0)	"異動類別    		"
   ttListA.aaa("acct_month"   , hh.acct_month);  //帳務年月
   //023	old_card_no         	VARCHAR (19,0)	舊卡卡號
   //024	purchase_date       	VARCHAR (8,0)	"消費日期			"
   //025	data_from           	VARCHAR (1,0)	資料來源
   //026	free_cnt            	INTEGER (4,0)	"免費次數 		"
   ttListA.aaa("chi_name"     , hh.chi_name);  //VARGRAPH(50,0)	"中文姓名 	    "
   //028	curr_max_amt        	DECIMAL (9,0)	"單筆最大金額 	"
   //030	amt_sum_flag        	VARCHAR (1,0)	消費金額累積方式
   ttListA.aaa("send_date"    , sysDate);  //VARCHAR (8,0)	名單寄送日期
   ttListA.aaa("crt_user"     , hModUser);  //VARCHAR (10,0)	建置使用者
   ttListA.aaa("crt_date"     , sysDate);  //ARCHAR (8,0)	建置日期
   ttListA.aaa("give_flag"    , "N");  //VARCHAR (1,0)	贈送註記
   ttListA.aaa("group_code"   , hh.group_code);  //VARCHAR (4,0)	團體代號
   ttListA.aaa("mod_user"     , hModUser);  //VARCHAR (10,0)	異動使用者
   ttListA.aaaDtime("mod_time");  //TIMESTMP(10,6)	異動時間
   ttListA.aaa("mod_pgm"      , hModPgm);  //VARCHAR (20,0)	異動程式

   if (ttListA.ti <= 0) {
      ttListA.ti = ppStmtCrt("tt-list-A", ttListA.getSql());
   }

   sqlExec(ttListA.ti, ttListA.getParms());
   if (sqlNrow <= 0) {
      printf("insert cms_roadlist error, kk[%s,%s]",hh.card_no,hh.id_no);
//      errExit(1);
   }
}

//************************************************************************
void updateCmsRoadmasterLog() throws Exception {

   updateSQL = " card_no = '',";
   updateSQL += " rds_pcard  = '',";
   updateSQL += " rm_carno = '',";
   updateSQL += " rm_carmanname        = '',";
   updateSQL += " rm_carmanid   = '',";
   updateSQL += " id_p_seqno   = '',";
   updateSQL += " mod_time     = TIMESTAMP_FORMAT(?,'YYYYMMDDHH24MISS'),";
   updateSQL += " mod_pgm       = ''";
   daoTable = "cms_roadmaster_log";
   setString(1, sysDate+sysTime);
   updateTable();
   if (notFound.equals("Y")) {
      comcr.errRtn("update_cms_roadmaster_log not found!", "", hCallBatchSeqno);
   }

}

//************************************************************************
private int moveData(Map<String, Object> map) throws Exception {
   int recordCnt;

   hRdsPcard = (String) map.get("rds_pcard");
   hChiName = (String) map.get("chi_name");
   hShiftDate = (String) map.get("shift_date");

   hIdNo = (String) map.get("id_no");
   showLogMessage("D", "", "身分證號 =  [ "+hIdNo+"] ");
   if (hIdNo.trim().equals("")) {
      showLogMessage("D", "", "身分證號不可為空白 !  存檔失敗 ! ! ! ");
      insertCmsRoadmasterLog();
      return errorTmp;
   }

   hCardNo = (String) map.get("card_no");
   showLogMessage("D", "", "卡號 =  [ "+hCardNo+"] ");
   if (hCardNo.trim().equals("")) {
      showLogMessage("D", "", "卡號不可為空白 !   存檔失敗 ! ! ! ");
//		errorTmp = insertCmsRoadmasterLog(2);
      return errorTmp;
   }

   hCarNo = (String) map.get("car_no");
   showLogMessage("D", "", "車號 =  [ "+hCarNo+"] ");
   if (hCarNo.trim().equals("")) {
      showLogMessage("D", "", "車號不可為空白 !   存檔失敗 ! ! ! ");
//		errorTmp = insertCmsRoadmasterLog(3);
      return errorTmp;
   }

   hDealNo = (String) map.get("deal_no");
   showLogMessage("D", "", "交易別 =  ["+hDealNo+"] ");
   if (!hDealNo.trim().equals("N")) {
      showLogMessage("D", "", "交易別不為N !  存檔失敗 ! ! ! ");
//		errorTmp = insertCmsRoadmasterLog(4);
      return errorTmp;
   }

   sqlCmd = "select substr(card_no, 8) card_no_s, card_no, id_p_seqno, group_code ";
   sqlCmd += "from crd_card ";
   sqlCmd += "where current_code = '0' ";
   sqlCmd += "and substr(card_no, 8) = ? ";
   setString(1, hCardNo);
   recordCnt = selectTable();
   if (recordCnt > 0) {
      hCrdCardCardNo = getValue("card_no");
      hCrdCardIdPSeqno = getValue("id_p_seqno");
      hCrdCardGroupCode = getValue("group_code");
   } else {
      showLogMessage("D", "", "卡號後9碼:"+hCardNo+"不存在 !");
      insertCmsRoadmasterLog();
      return errorTmp;
   }

   sqlCmd = "select substr(id_no, 1, 3) f_3_id_no, substr(id_no, 7, 4) b_4_id_no, id_no, id_p_seqno  ";
   sqlCmd += "from crd_idno ";
   sqlCmd += "where id_p_seqno = ? ";
   setString(1, hCrdCardIdPSeqno);
   if (selectTable() > 0) {
      hCrdIdnoF3IdNo = getValue("f_3_id_no");
      hCrdIdnoB4IdNo = getValue("b_4_id_no");
      hCrdIdnoIdNo = getValue("id_no");
      if (hIdNo.substring(0, 3).equals(hCrdIdnoF3IdNo) && hIdNo.substring(6, 10).equals(hCrdIdnoB4IdNo))
         checkData();
      else {
         showLogMessage("D", "", "持卡人不存在卡人檔 ! !");
//			errorTmp = insertCmsRoadmasterLog(6);
         return errorTmp;
      }
   }
   return 0;
}

//************************************************************************
int checkData() throws Exception {
   sqlCmd = "select a.card_no  ";
   sqlCmd += "from cms_roadmaster a, cms_roaddetail b ";
   sqlCmd += "where a.card_no = ? ";
   sqlCmd += "and a.card_no = b.card_no ";
   setString(1, hCrdCardCardNo);
   if (selectTable() > 0) {
      showLogMessage("D", "", "Table cms_roadmaster,cms_roaddetail 已存在此卡號，不可存入 ! !");
      return errorTmp = 1;
   } else {
      insertCmsRoadmaster();
      insertCmsRoaddetail();
   }
   return 0;
}

com.Parm2sql ttRdsMA = null;

void insertCmsRoadmaster() throws Exception {
   if (ttRdsMA == null) {
      ttRdsMA = new Parm2sql();
      ttRdsMA.insert("cms_roadmaster");
   }

   ttRdsMA.aaa("card_no", hh.card_no);  //正卡卡號
   ttRdsMA.aaa("rm_type", "F");  //道路救援類別
   ttRdsMA.aaa("rm_carno", hh.car_no);  //車號
   ttRdsMA.aaa("group_code", hh.group_code);  //團體代號
   ttRdsMA.aaa("rm_carmanname", hh.chi_name);  //車主姓名
   ttRdsMA.aaa("rm_carmanid", hh.id_no);  //車主身份証號
//006	rm_oldcarno	VARCHAR (10,0)	舊車號        
//007	rm_htelno1	VARCHAR (5,0)	自宅電話1     
//008	rm_htelno2	VARCHAR (15,0)	自宅電話2     
//009	rm_htelno3	VARCHAR (6,0)	自宅電話3     
//010	rm_otelno1	VARCHAR (5,0)	公司電話1     
//011	rm_otelno2	VARCHAR (15,0)	公司電話2     
//012	rm_otelno3	VARCHAR (6,0)	公司電話3     
//013	cellar_phone	VARCHAR (30,0)	手機          
   ttRdsMA.aaa("rm_status", "1");  //	VARCHAR (1,0)	目前狀態
//015	rm_validdate	VARCHAR (8,0)	有效期限      
   ttRdsMA.aaa("rm_moddate", sysDate);  //	VARCHAR (8,0)	異動日期
//017	rm_reason	VARCHAR (10,0)	異動原因      
//018	rm_payno	VARCHAR (14,0)	請款批號      
//019	rm_payamt	DECIMAL (13,2)	自費金額      
//020	rm_paydate	VARCHAR (8,0)	請款日期      
   ttRdsMA.aaa("crt_user", hModPgm);  //登錄者
   ttRdsMA.aaa("crt_date", hh.shift_date);  //登錄日期
   ttRdsMA.aaa("apr_user", hModUser);  //覆核主管
   ttRdsMA.aaaYmd("apr_date");  //覆核日期
//025	never_check	VARCHAR (1,0)	never check   
   ttRdsMA.aaa("rds_pcard", hh.rds_pcard);  //免費道路救援類別
   ttRdsMA.aaa("id_p_seqno", hh.id_pseqno);  //ID編號
//032	outstanding_yn	VARCHAR (1,0)	未繳最低應繳金額Y/N
   ttRdsMA.aaa("give_flag", "N");  //贈送註記
   ttRdsMA.aaa("imp_file_name", fileName);  //匯入檔名
   ttRdsMA.aaaModxxx(hModUser, hModPgm);

   if (ttRdsMA.ti <= 0) {
      ttRdsMA.ti = ppStmtCrt("tt-rdsM-A", ttRdsMA.getSql());
   }

   sqlExec(ttRdsMA.ti, ttRdsMA.getParms());

   if (sqlNrow <= 0) {
      sqlerr("insert cms_roadmaster error");
      errExit(1);
   }
}

//************************************************************************
com.Parm2sql ttRdsDA = null;

void insertCmsRoaddetail() throws Exception {
   if (ttRdsDA == null) {
      ttRdsDA = new com.Parm2sql();
      ttRdsDA.insert("cms_roaddetail");
   }

   iiSeqNo++;

   ttRdsDA.aaaYmd("rd_moddate");  //異動日期
   ttRdsDA.aaa("rd_seqno", iiSeqNo);  //登錄序號
   ttRdsDA.aaa("rd_modtype", "B");  //異動來源         O:online, B:batch
   ttRdsDA.aaa("card_no", hh.card_no);  //正卡卡號
   //004	new_card_no	VARCHAR (19,0)	新卡號
   ttRdsDA.aaa("rd_type", "F");   //救援類別         F:免費, E:自費
   ttRdsDA.aaa("appl_card_no", hh.card_no);  //申請卡號
   ttRdsDA.aaa("group_code", hh.group_code);  //團體代號
   ttRdsDA.aaa("rd_carno", hh.car_no);  //車號
   ttRdsDA.aaa("rd_carmanname", hh.chi_name);  //車主姓名
   ttRdsDA.aaa("rd_carmanid", hh.id_no);  //車主身份証號
   //011	rd_newcarno	VARCHAR (10,0)	新車號
   //012	rd_htelno1	VARCHAR (5,0)	自宅電話1
   //013	rd_htelno2	VARCHAR (15,0)	自宅電話2
   //014	rd_htelno3	VARCHAR (6,0)	自宅電話3
   //015	rd_otelno1	VARCHAR (5,0)	公司電話1
   //016	rd_otelno2	VARCHAR (15,0)	公司電話2
   //017	rd_otelno3	VARCHAR (6,0)	公司電話3
   //018	cellar_phone	VARCHAR (30,0)	手機
   //019	rd_validdate	VARCHAR (8,0)	有效期限         YYMM
   ttRdsDA.aaa("rd_status", "1");  //	VARCHAR (1,0)	異動狀態         1:啟用, 2:變更車號, 0:停用
   //021	rd_payamt	DECIMAL (13,2)	自費金額
   //022	rd_payno	VARCHAR (12,0)	請款批號
   //023	rd_paydate	VARCHAR (8,0)	請款日期
   //024	rd_stopdate	VARCHAR (8,0)	停用日期
   //025	rd_stoprsn	VARCHAR (10,0)	停用原因
   ttRdsDA.aaa("crt_user", hModPgm);  //登錄者
   ttRdsDA.aaa("crt_date", hh.shift_date);  //登錄日期
   ttRdsDA.aaa("apr_user", hModUser);  //覆核主管
   ttRdsDA.aaaYmd("apr_date");  //覆核日期
   ttRdsDA.aaa("rd_senddate", sysDate);  //	VARCHAR (8,0)	RD_SENDDATE
   ttRdsDA.aaa("rd_sendsts", "N");  //緊急登錄	 VARCHAR (10,0)	RD_SENDSTS
   ttRdsDA.aaa("rd_sendyn", "N");  //	VARCHAR (1,0)	RD_SENDYN
   //033	rd_sendadd	INTEGER (4,0)	RD_SENDADD
   //034	rd_sendstop	INTEGER (4,0)	RD_SENDSTOP
   //035	proj_no	VARCHAR (10,0)	專案代號
   //036	purch_amt	DECIMAL (11,0)	累積消費金額
   //037	purch_cnt	INTEGER (4,0)	累積消費次數
   //038	purch_amt_lyy	DECIMAL (11,0)	上年累計消費金額
   //039	cardholder_type	VARCHAR (1,0)	卡人類別
   ttRdsDA.aaa("rds_pcard", hh.rds_pcard);  //免費道路救援類別
   ttRdsDA.aaa("id_p_seqno", hh.id_pseqno);  //ID編號
   //046	outstanding_yn	VARCHAR (1,0)	未繳最低應繳金額Y/N
   //047	outstanding_cond	VARCHAR (1,0)	未繳最低應繳金額不適用(參數)Y/N
   ttRdsDA.aaa("give_flag", "N");  //贈送註記
   ttRdsDA.aaaModxxx(hModUser, hModPgm);

   if (ttRdsDA.ti <= 0) {
      ttRdsDA.ti = ppStmtCrt("tt-rdsd-A", ttRdsDA.getSql());
   }

//	debug=true;
//	dddSql(ttRdsDA.ti, ttRdsDA.getConvParm(false));
//	debug=false;

   sqlExec(ttRdsDA.ti, ttRdsDA.getParms());
   if (sqlNrow <= 0) {
      sqlerr("insert Cms_roaddetail error");
      errExit(1);
   }

}

//************************************************************************
private Map processDataRecord(String[] row, String[] dt) throws Exception {
   Map<String, Object> map = new HashMap<>();
   int i = 0;
   int j = 0;
   for (String s : dt) {
      map.put(s.trim(), row[i]);
      i++;
   }
   return map;

}

//************************************************************************
public String[] getFieldValue(String rec, int[] parm, int length) {
   int x = 0;
   int y = 0;
   if (length == 51)
      x = 1;
   else
      x = 0;
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
      x = x+i;
   }
   return ss;
}

//=============================================================================
void ftpScript() throws Exception {

}

}
