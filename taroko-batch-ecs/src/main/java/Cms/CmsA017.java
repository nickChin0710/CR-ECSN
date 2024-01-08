package Cms;
/**
 * 2023-0925  V1.00.05     JH    getRdsPcard()
 * 2023-0717  V1.00.04     JH    ftp+backup
 * 2023-0714  V1.00.03     JH    cms_roadlist.dupl不傳送--
 * 2023-0621  V1.00.02     JH    cms_roadlist.mod_type
 * 2023-0615  V1.00.01     JH    modify
 * 2023-0508  V1.00.00     JH    initial
 * 2023-1213  V1.00.06     JH    ++未登錄的頂級有效卡
 * */

import com.CommFTP;
import com.CommRoutine;
import com.Parm2sql;
import java.text.Normalizer;

public class CmsA017 extends com.BaseBatch {
private final String PROGNAME = "道路救援產生名單檔-異動(006NOREG)  2023-1213  V1.00.06";
CommFTP commFTP = null;
CommRoutine comr = null;

CmsA017.HH hh=new CmsA017.HH();
//-----------
class HH {
   String rowid="";
//   String mod_type="";
   String rds_pcard="";
   String id_no="";
   String card_no="";
   String car_no="";
   String old_car_no="";
   String idno_name="";
   String id_pseqno="";
   //-------
   String rd_moddate="";
   double rd_seqno=0;
   String rd_modtype="";
   String rd_status;
   String rd_sendsts="";
   String rd_sendYn="";
   //--
   String maj_id_pseqno="";
   String maj_card_no ="";
   String acno_pseqno ="";
   String group_code ="";
   //--
   String give_flag="";
   String proj_no="";
   double purch_amt=0;
   int    purch_cnt=0;
   double purch_amt_lyy=0;

   void initData() {
      rowid="";
//      mod_type="";
      rds_pcard="";
      id_no="";
      card_no="";
      car_no="";
      old_car_no="";
      idno_name="";
      id_pseqno="";
      //-------
      rd_moddate="";
      rd_seqno=0;
      rd_modtype="";
      rd_status="";
      rd_sendsts="";
      rd_sendYn="";
      //--
      maj_id_pseqno="";
      maj_card_no ="";
      acno_pseqno ="";
      group_code ="";
      //--
      give_flag="";
      proj_no="";
      purch_amt=0;
      purch_cnt=0;
      purch_amt_lyy=0;
   }
}
//--
String isFileName="006NOREG";
int iiFileNum=-1;
//=*****************************************************************************
public static void main(String[] args) {
   CmsA017 proc = new CmsA017();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 1) {
      printf("Usage : CmsA017 [busi_date(08)]");
      okExit(0);
   }

   if (args.length >= 1) {
      setBusiDate(args[0]);
   }

   dbConnect();

   //========
   fileOpen();

   selectCmsRoadDetail();
   sqlCommit();
   //-未登錄報送---
   selectCrdCard_group();

   //-Footer------
   String lsTxt="3"+
       String.format("%09d",il_sendCnt)+
       commString.bbFixlen(sysDate,8)+
       commString.space(32)+
       newLine;
   writeTextFile(iiFileNum,lsTxt);

   closeOutputText(iiFileNum);
   printf(" 產生筆數[%s]", il_sendCnt);

   //-FTP+backup--
   procFTP();
   fileBackup(isFileName,"media/cms","media/cms/backup");
   printf(" file[%s] backup to [%s]", isFileName,"media/cms/backup");

   endProgram();
}
//======================
int il_sendCnt=0;
void selectCmsRoadDetail() throws Exception {
   printf("異動報送處理.....");

   sqlCmd = " select hex(rowid) as rowid"
           +", rd_moddate, rd_seqno, rd_modtype"
           +", card_no, id_p_seqno, new_card_no, appl_card_no"
           +", rd_carno, rd_newcarno, rd_carmanname, rd_carmanid"
           +", rd_status, rds_pcard, give_flag"
           +", proj_no, purch_amt, purch_cnt, purch_amt_lyy"
           + " from cms_roaddetail"
           + " where 1=1"
           + " and rd_senddate ='' "
           +" and rd_status in ('0','1','2','3')";
   //TTT--
   //sqlCmd +=" and card_no in ('3567430072135112','3567430151206107')";
   sqlCmd +=" order by rd_moddate, rd_seqno";
   //('0停用','1新增車號','2變更車號','3取消車號','4未啟用')--

   this.openCursor();
   int ll_procCnt=0;
   while (fetchTable()) {
      totalCnt++;
      ll_procCnt++;
      hh.initData();

      hh.rowid =colSs("rowid");
      hh.rd_moddate =colSs("rd_moddate");
      hh.rd_seqno =colNum("rd_seqno");
      hh.rd_modtype =colSs("rd_modtype");
      hh.card_no =colSs("card_no");
      hh.car_no =colSs("rd_carno").toUpperCase();
      String lsNewCarNo =colSs("rd_newcarno").toUpperCase();
      if (!empty(lsNewCarNo) && !eq(hh.car_no,lsNewCarNo)) {
         hh.old_car_no =hh.car_no;
         hh.car_no =lsNewCarNo;
      }
      hh.id_no =colSs("rd_carmanid");
      hh.idno_name =colSs("rd_carmanname");
      hh.id_pseqno =colSs("id_p_seqno");
      hh.rd_status =colSs("rd_status");
      hh.rds_pcard =colSs("rds_pcard");
      hh.give_flag =colNvl("give_flag","N");
      hh.proj_no =colSs("proj_no");
      hh.purch_amt =colNum("purch_amt");
      hh.purch_cnt =colInt("purch_cnt");
      hh.purch_amt_lyy =colNum("purch_amt_lyy");

      int liRC=selectCmsRoadMaster();
      if (liRC !=0) {
         hh.rd_sendYn ="1";  //與Master狀態不符--
         hh.rd_sendsts ="";
         updateCmsRoadDetail();
         continue;
      }
      //('0停用','1新增車號','2變更車號','3取消車號','4未啟用')--
      if (eq(hh.rd_status,"0")) {
         hh.rd_sendYn ="Y";
         hh.rd_sendsts ="D";
      }
      else if (eq(hh.rd_status,"1")) {
         hh.rd_sendYn ="Y";
         hh.rd_sendsts ="N";
      }
      else if (eq(hh.rd_status,"2") || eq(hh.rd_status,"3")) {
         hh.rd_sendYn ="Y";
         hh.rd_sendsts ="R";
      }

      liRC =selectCrdCard();
      if (liRC !=0) {
         hh.rd_sendYn ="3";  //卡號不存在--
         hh.rd_sendsts ="";
         updateCmsRoadDetail();
         continue;
      }

      updateCmsRoadDetail();
      liRC =insertCmsRoadList();
      if (liRC !=0) continue;

      String lsRdsPcard=getRdsPcard();

      String lsIdno =commString.left(hh.id_no,3)+"***"+commString.right(hh.id_no,4);
      String lsCardNo =commString.right(hh.card_no,9);
      String lsChiName =getIdnoName();
      il_sendCnt++;
      String lsTxt="2"
              +commString.rpad(hh.rd_sendsts,1)   //交易別N,R,D
              +commString.rpad(lsRdsPcard,1)   //優惠別
              +commString.bbFixlen(lsIdno,10)   //持卡人ID
              +commString.bbFixlen(lsCardNo,9)   //卡號
              +commString.bbFixlen(hh.car_no,8)   //車號
              +commString.bbFixlen(sysDate,8)   //批次系統日
              +commString.bbFixlen(lsChiName,12)   //持卡人姓名
              +newLine;
      writeTextFile(iiFileNum,lsTxt);

   }
   closeCursor();
   printf("異動報送處理, proc_cnt[%s]", ll_procCnt);

}
//==============================
void selectCrdCard_group() throws Exception {
   printf("未登錄的頂級有效卡處理.....");
   sqlCmd = "select b.rds_pcard"
       +", substr(c.id_no,1,3)||'***'||right(c.id_no,4) id_no"
       +", right(a.card_no,9) AS card_no9"
       +", substr(c.chi_name,1,12) AS chi_name "
       +" from crd_card A "
       +" left join crd_idno C on A.id_p_seqno=C.id_p_seqno "
       +" left join ptr_card_type B on A.card_type =B.card_type"
       +" where A.current_code='0' "
       +" and B.rds_pcard ='I' "
       +" and A.group_code in ('1620','1621','1622','1630','1631') "
       +" AND NOT EXISTS (SELECT 1 FROM cms_roadmaster WHERE card_no=A.card_no) "
       +" ORDER BY A.card_no"
       ;
   //--and a.card_no not in (select card_no from cms_roadmaster)

   this.openCursor();
   int ll_procCnt=0;
   while (fetchTable()) {
      totalCnt++;
      ll_procCnt++;
      processDisplay(5000);

      String lsRdsPcard =colSs("rds_pcard");
      String lsIdno =colSs("id_no");
      String lsCardNo =colSs("card_no9");
      String lsChiName =colSs("chi_name");

      il_sendCnt++;
      String lsTxt="2"
          +commString.rpad("N",1)   //交易別N,R,D
          +commString.rpad(lsRdsPcard,1)   //優惠別
          +commString.bbFixlen(lsIdno,10)   //持卡人ID
          +commString.bbFixlen(lsCardNo,9)   //卡號
          +commString.bbFixlen("",8)   //車號
          +commString.bbFixlen(sysDate,8)   //批次系統日
          +commString.bbFixlen(lsChiName,12)   //持卡人姓名
          +newLine;
      writeTextFile(iiFileNum,lsTxt);
   }
   closeCursor();
   printf("未登錄的頂級有效卡處理, 筆數=[%s]", ll_procCnt);

}
//==========================================================
int tiMast=-1;
int selectCmsRoadMaster() throws Exception {
   if (tiMast <=0) {
      sqlCmd ="select rm_status, rm_carmanid, rm_carno "
              +" from cms_roadmaster"
              +" where card_no =? and rm_type='F'"
              +" and upper(rm_carno) =?"
      ;
      tiMast =ppStmtCrt("ti-Mast-S","");
   }

   ppp(1, hh.card_no);
   ppp(2, hh.car_no);
   sqlSelect(tiMast);
   if (sqlNrow <=0) {
      printf(" select cms_roadmaster N-find, kk[%s,%s]", hh.card_no,hh.car_no);
      return 1;
   }
   String lsStatus =colSs("rm_status");
   if (!eq(hh.rd_status,lsStatus)) {
      return 1;
   }
   String lsManId=colSs("rm_carmanid");
   if (!eq(hh.id_no, lsManId)) {
      return 1;
   }
   String lsCarNo=colSs("rm_carno").toUpperCase();
   if (!eq(hh.car_no, lsCarNo)) {
      return 1;
   }

   return 0;
}

//int tiList=-1;
//int selectCmsRoadList() throws Exception {
//   if (tiList <=0) {
//      sqlCmd ="select mod_type "
//              +" from cms_roadlist"
//              +" where card_no =?"
//              +" order by crt_date desc, mod_time desc"
//              +commSqlStr.rownum(1)
//      ;
//      tiList =ppStmtCrt("ti-list-S","");
//   }
//
//   ppp(1, hh.card_no);
//   sqlSelect(tiList);
//   if (sqlNrow <=0) {
//      return 0;
//   }
//   String lsModType =colSs("mod_type");
//   //-停用-
//   if (commString.ssIn(lsModType,",D")) {
//      return 0;
//   }
//   //-全峰啟用中-
//   return 1;
//}

int tiCard=-1;
int selectCrdCard() throws Exception {
   if (tiCard <=0) {
      sqlCmd = "select major_card_no, major_id_p_seqno"
              + ", acno_p_seqno, current_code, issue_date"
              + ", group_code"
              + " from crd_card"
              + " where card_no =?"
      ;
      tiCard = ppStmtCrt("ti-card-S", "");
   }
   ppp(1, hh.card_no);
   sqlSelect(tiCard);
   if (sqlNrow <=0) {
      printf("select crd_card N-find, kk[%s]", hh.card_no);
      return 1;
   }

   hh.maj_id_pseqno=colSs("major_id_p_seqno");
   hh.maj_card_no =colSs("major_card_no");
   hh.acno_pseqno =colSs("acno_p_seqno");
   hh.group_code =colSs("group_code");
   return 0;
}

int tiIdno=-1;
String getIdnoName() throws Exception {
   String lsName=hh.idno_name.replaceAll("　","");
   if (!empty(lsName)) {
      return lsName;
   }

   if (tiIdno <=0) {
      sqlCmd ="select chi_name"+
              " from crd_idno"+
              " where id_p_seqno =?";
      tiIdno =ppStmtCrt("ti-idno-S","");
   }

   ppp(1, hh.id_pseqno);
   sqlSelect(tiIdno);
   if (sqlNrow >0) {
      lsName =colSs("chi_name").replaceAll("　","");
   }
   return lsName;
}

int tiCtype=-1;
String getRdsPcard() throws Exception {
   String lsCarNo =hh.car_no.replaceAll("-","");
   //-L.租賃車-
   if (lsCarNo.length()==6) {
      byte[] cc=lsCarNo.getBytes();
      if (cc[0]==cc[1] || cc[4]==cc[5]) {
         return "L";
      }
   }
   if (hh.car_no.length()==7) {
      byte[] cc=hh.car_no.getBytes();
      if (cc[0]=='R') {
         return "L";
      }
   }

   if (!empty(hh.rds_pcard))
      return hh.rds_pcard;

   if (tiCtype <=0) {
      sqlCmd ="select A.rds_pcard"+
              " from ptr_card_type A join crd_card B on A.card_type=B.card_type"+
              " where B.card_no =?"+
              commSqlStr.rownum(1);
      tiCtype =ppStmtCrt("ti-ctype-S","");
   }

   ppp(1, hh.card_no);
   sqlSelect(tiCtype);
   if (sqlNrow <=0) {
      return "";
   }
   hh.rds_pcard =colSs("rds_pcard");
   return hh.rds_pcard;
}

//====================================================

com.Parm2sql ttDetlU=null;
int updateCmsRoadDetail() throws Exception {
   if (ttDetlU ==null) {
      ttDetlU =new Parm2sql();
      ttDetlU.update("cms_roaddetail");
   }

   ttDetlU.aaa("rd_senddate",sysDate);
   ttDetlU.aaa("rd_sendsts",hh.rd_sendsts);
   ttDetlU.aaa("rd_sendyn",hh.rd_sendYn);
   ttDetlU.aaaDtime("mod_time");
   ttDetlU.aaa("mod_pgm", hModPgm);
   ttDetlU.aaaWhere("where rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)", hh.rowid);

   if (ttDetlU.ti <=0) {
      ttDetlU.ti =ppStmtCrt("tt-detl-U",ttDetlU.getSql());
   }

   sqlExec(ttDetlU.ti, ttDetlU.getParms());
   if (sqlNrow <=0) {
      sqlerr("update cms_roadDetail error");
      okExit(0);
   }
   return 0;
}

com.Parm2sql ttListA=null;
int insertCmsRoadList() throws Exception {
   if (ttListA ==null) {
      ttListA =new Parm2sql();
      ttListA.insert("cms_roadlist");
   }

//   String ls_modType="A";
//   if (eq(hh.rd_status,"0")) ls_modType="D";
   String ls_dataFrom="1";
   if (eq(hh.rd_modtype,"B")) ls_dataFrom="2";

   ttListA.aaa("crt_date", sysDate);  //ARCHAR (8,0)	建置日期
   ttListA.aaa("card_no", hh.card_no);  //VARCHAR (19,0)	卡號
   ttListA.aaa("mod_type", hh.rd_sendsts);  //VARCHAR (1,0)	"異動類別    		"
   ttListA.aaa("major_card_no", hh.maj_card_no);  //VARCHAR (19,0)	正卡卡號
   ttListA.aaa("major_id_p_seqno", hh.maj_id_pseqno);  //VARCHAR (10,0)	正卡ID流水號
   ttListA.aaa("acno_p_seqno", hh.acno_pseqno);  //VARCHAR (10,0)	帳戶流水號
   ttListA.aaa("proj_no", hh.proj_no);  //VARCHAR (10,0)	專案代號
   //005	year_type           	VARCHAR (1,0)	消費年度類別
   ttListA.aaa("rm_status", hh.rd_status);  //VARCHAR (1,0)	道路救援狀態
   //007	proc_flag           	VARCHAR (1,0)	處理註記       1:成功 2:退件 3:分期 N:未處理
   //008	proc_date           	VARCHAR (8,0)	處理日期
   ttListA.aaa("purch_amt", hh.purch_amt_lyy);  //DECIMAL (14,3)	上年度累積消費額
   //010	purch_row           	DECIMAL (14,3)	上年度累積消費筆數
   ttListA.aaa("tol_amt", hh.purch_amt);  //DECIMAL (14,3)	消費總金額
   ttListA.aaa("curr_tot_cnt", hh.purch_cnt);  //INTEGER (4,0)	"累積消費筆數 	"
   //012	end_card_no         	VARCHAR (19,0)	最後卡號
   ttListA.aaa("id_p_seqno", hh.id_pseqno);  //VARCHAR (10,0)	"卡人流水號碼 	"
   ttListA.aaa("rds_pcard", hh.rds_pcard);  //VARCHAR (1,0)	免費道路救援類別
   //017	current_code        	VARCHAR (1,0)	狀態碼
   //018	issue_date          	VARCHAR (8,0)	核卡日期
   ttListA.aaa("rm_carmanid", hh.id_no);  //VARCHAR (20,0)	車主身份証號
   ttListA.aaa("rm_carno", hh.car_no);  //VARCHAR (10,0)	車號
   //022	acct_month          	VARCHAR (6,0)	帳務年月
   //023	old_card_no         	VARCHAR (19,0)	舊卡卡號
   //024	purchase_date       	VARCHAR (8,0)	"消費日期			"
   ttListA.aaa("data_from", ls_dataFrom);  //資料來源:1.online, 2.Batch--
   //026	free_cnt            	INTEGER (4,0)	"免費次數 		"
   ttListA.aaa("chi_name", hh.idno_name);  //VARGRAPH(50,0)	"中文姓名 	    "
   //028	curr_max_amt        	DECIMAL (9,0)	"單筆最大金額 	"
   //030	amt_sum_flag        	VARCHAR (1,0)	消費金額累積方式
   ttListA.aaa("send_date", sysDate);  //VARCHAR (8,0)	名單寄送日期
   ttListA.aaa("crt_user", hModUser);  //VARCHAR (10,0)	建置使用者
   ttListA.aaa("give_flag", hh.give_flag);  //VARCHAR (1,0)	贈送註記
   ttListA.aaa("group_code", hh.group_code);  //VARCHAR (4,0)	團體代號
   ttListA.aaa("mod_user", hModUser);  //VARCHAR (10,0)	異動使用者
   ttListA.aaaDtime("mod_time");  //TIMESTMP(10,6)	異動時間
   ttListA.aaa("mod_pgm", hModPgm);  //VARCHAR (20,0)	異動程式

   if (ttListA.ti <=0) {
      ttListA.ti =ppStmtCrt("tt-list-A",ttListA.getSql());
   }

   sqlExec(ttListA.ti, ttListA.getParms());
   if (sqlNrow==0 && sqlDuplRecord) {
      printf("insert cms_roadlist dupl, kk[%s,%s]",hh.card_no,hh.rd_sendsts);
      return 1;
   }
   if (sqlNrow <0) {
      sqlerr("insert cms_roadlist error, kk[%s,%s]",hh.card_no,hh.rd_sendsts);
      okExit(0);
   }

   return 0;
}
//===================================
void fileOpen() throws Exception {
   //-006NOREG-
   String lsPath= getEcsHome() + "/media/cms/" + isFileName;
   printf("open out-file [%s]", lsPath);
   iiFileNum =openOutputText(lsPath);
   if (iiFileNum <0) {
      errmsg("在程式執行目錄下沒有權限讀寫資料, file[%s]",lsPath);
      okExit(0);
   }
}
//================
private void procFTP() throws Exception {
   commFTP = new CommFTP(getDBconnect(), getDBalias());
   comr = new CommRoutine(getDBconnect(), getDBalias());

   //comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
   commFTP.hEflgTransSeqno = ecsModSeq(10);
   commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
   commFTP.hEriaLocalDir = getEcsHome() + "/media/cms" ;//fileName;
   commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
   commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
   commFTP.hEflgModPgm = javaProgram;

   showLogMessage("I", "", "mput " + isFileName + " 開始傳送....");
   int errCode = commFTP.ftplogName("NCR2TCB", "mput " + isFileName);

   if (errCode != 0) {
      showLogMessage("I", "", "ERROR:無法傳送 " + isFileName + " 資料"+" errcode:"+errCode);
      commFTP.insertEcsNotifyLog(isFileName, "3", javaProgram, sysDate, sysTime);
   }
}

}
