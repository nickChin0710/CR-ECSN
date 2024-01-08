package Mkt;
/**每月1日執行--
 * 2023-0619 V1.00.01   JH    ++updateCms_roadMaster(),loadCrd_card()
 * 2023-0519 V1.00.00   JH    initial
 * */
import Cms.CmsRdsParm;
import com.DataSet;
import com.Parm2sql;
import java.text.Normalizer;

public class MktE090 extends com.BaseBatch {
private final String PROGNAME = "卡友各項權益統計-道路救援  2023-0619 V1.00.01";
Cms.CmsRdsParm ooParm=null;
HH hh=new HH();
//-----------
class HH {
   String rowId = "";
   String cardNo ="";
   String groupCode ="";
   String rmStatus ="";
   String idPseqno ="";
   String currentCode ="0";
   //---
   String projNo ="";
   double purchAmt =0; //累積消費金額
   int purchCnt =0;  //累積消費次數
   double purchAmtLyy =0;  //上年累計消費金額
   //--
   String Pseqno ="";
   String acctType ="";
   String cardType ="";
   int useCnt =0;
   double rcvAnnualFee =0;
   int freeCnt =0;
   String matchFlag ="";
   //-roadMaster-
   String rmCarno ="";
   String rmCarmanname ="";
   String rmCarmanid ="";
   String rdsPcard ="";
   String outstandingYn ="";
   //--
   String rdStatus ="";
   String rdStopdate ="";
   String rdStoprsn ="";

   void initData() {
      rowId = "";
      cardNo ="";
      groupCode ="";
      rmStatus ="";
      idPseqno ="";
      currentCode ="";
      //---
      projNo ="";
      purchAmt =0;
      purchCnt =0;
      purchAmtLyy =0;
      //--
      Pseqno ="";
      acctType ="";
      cardType ="";
      useCnt =0;
      rcvAnnualFee =0;
      freeCnt =0;
      matchFlag ="";
      //--
      rmCarno ="";
      rmCarmanname ="";
      rmCarmanid ="";
      rdsPcard ="";
      outstandingYn ="";
      //--
      rdStatus ="";
      rdStopdate ="";
      rdStoprsn ="";
   }
}
//--
String h_procYM="", h_procYY="";
//=*****************************************************************************
public static void main(String[] args) {
   MktE090 proc = new MktE090();

//	proc.debug = true;
   proc.runCheck = true;
   proc.mainProcess(args);
   proc.systemExit(0);
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : MktE090 [busi_date, callbatch_seqno]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length()==8) {
         String sG_Args0 = args[0];
         hBusiDate= Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
      }
      callBatchSeqno(args[0]);
   }
   if (args.length == 2) {
      callBatchSeqno(args[1]);
   }

   dbConnect();
   callBatch(0, 0, 0);
   ooParm = new CmsRdsParm(getDBconnect(), getDBalias());

   String lsDD=commString.mid(hBusiDate,6,2);
   if (!eq(lsDD,"01")) {
      errmsg("每月1日執行, busi_date[%s]", hBusiDate);
      okExit(0);
   }

   h_procYM =commString.left(commDate.dateAdd(hBusiDate,0,-1,0),6);
   h_procYY =commString.left(h_procYM,4);
   printf("-- Process 統計年月=[%s], 統計年度>=[%s]", h_procYM, h_procYY);

   ooParm.hBusiDate =hBusiDate;
   ooParm.setLastYear("");
   ooParm.selectCmsRoadParm2();
   loadCrdCard();
   selectCmsRoadMaster();

   //--
   deleteCmsRightYear();
   selectCmsRightYearDtl();

   sqlCommit();

   dsCard.dataClear();
   dsCard =null;

   endProgram();
}
//---------
com.DataSet dsCard=new DataSet();
void loadCrdCard() throws Exception {
   sqlCmd ="select A.card_no, A.id_p_seqno, A.group_code, A.card_type"+
           ", A.p_seqno, A.acct_type, A.current_code"+
           ", B.rm_carno"+
//           " from crd_card A LEFT JOIN cms_roadMaster B"+
           " from crd_card A JOIN cms_roadMaster B"+
           " ON A.card_no=B.card_no AND B.id_p_seqno=A.id_p_seqno"+
           " where 1=1"+
           " order by A.card_no"
   ;
   sqlQuery(dsCard,"",null);
   if (sqlNrow <0) {
      sqlerr("load crd_card error");
      errExit(1);
   }
   dsCard.loadKeyData("card_no");
}
void selectCmsRoadMaster() throws Exception {
   sqlCmd = " select hex(rowid) as rowid"
           +", card_no, rm_status, rm_carno"+
           ", group_code, rm_carmanname, rm_carmanid"+
           ", id_p_seqno, rds_pcard, outstanding_yn"
           + " from cms_roadMaster"
           + " where rm_type='F'"
           + " and ( rm_status in ('1','2','3','4') "
           +"  or  (rm_status='0' and rm_reason='2') )"
           +" order by card_no"
   ;

   this.openCursor();
   while (fetchTable()) {
      totalCnt++;
      hh.initData();

      hh.rowId =colSs("rowid");
      hh.cardNo =colSs("card_no");
      hh.groupCode =colSs("group_code");
      hh.rmStatus =colSs("rm_status");
      hh.idPseqno =colSs("id_p_seqno");
      //-每月消費條件-
      hh.rmCarno =colSs("rm_carno");
      hh.rmCarmanname =colSs("rm_carmanname");
      hh.rmCarmanid =colSs("rm_carmanid");
      hh.rdsPcard =colSs("rds_pcard");
      hh.outstandingYn =colSs("outstanding_yn");

      ooParm.hhInit();
      int liRC=ooParm.checkRoadParm(hh.cardNo);
      //-符合-
      if (liRC ==0) {
//         printf(" parm.OK; cardNo[%s], projNo[%s], condCard[%s]"
//                 , hh.card_no, ooParm.hh_proj_no, ooParm.hh_condCard);
         hh.freeCnt =1;
         hh.matchFlag ="Y";
         hh.projNo =ooParm.hhProjNo;
         hh.purchAmt =ooParm.hhPurchAmt;
         hh.purchCnt =ooParm.hhPurchCnt;
      }
      hh.groupCode =ooParm.hhGroupCode;
      hh.cardType =ooParm.hhCardType;
      hh.Pseqno =ooParm.hhPseqno;
      hh.acctType =ooParm.hhAcctType;
      hh.outstandingYn =ooParm.hhOutstandingYn;
      //--
      selectBilMchtApplyTmp();
      //--
      selectCycAfee();
      //--
      if (liRC !=0) {
         selectMktPostConsume();
      }
      //---
      updateCmsRightYearDtl();
      //-<>0.不符合,停用-
      if (liRC !=0 && eq(hh.rmStatus,"0")) continue;
      //-0.符合,啟用,有效卡-
      selectCrdCard(hh.cardNo);
      if (liRC ==0 && !eq(hh.rmStatus,"0") && eq(hh.currentCode,"0")) continue;
      insertCmsRoadDetail(liRC);
      updateCmsRoadMaster(liRC);
   }
   closeCursor();
}
//---------
void selectCmsRightYearDtl() throws Exception {
   long llTotCnt=0;

   sqlCmd ="select card_no"+
           ", max(proj_code) as proj_no"+
           ", sum(free_cnt) as free_cnt"+
           ", sum(use_cnt) as use_cnt"+
           ", sum(curr_month_amt) as purch_amt"+
           ", sum(curr_month_cnt) as purch_cnt"+
           ", sum(rcv_annual_fee) as rcv_annual_fee"+
           " from cms_right_year_dtl"+
           " where item_no ='14'"+
           " and acct_month like ?"+
           " group by card_no"+
           " order by card_no"
           ;

   ppp(1, h_procYY+"%");
   this.openCursor();
   while (fetchTable()) {
      llTotCnt++;
      hh.initData();

      hh.cardNo =colSs("card_no");
      hh.projNo =colSs("proj_no");
      hh.freeCnt =colInt("free_cnt");
      hh.useCnt =colInt("use_cnt");
      hh.purchAmt =colNum("purch_amt");
      hh.purchCnt =colInt("purch_cnt");
      hh.rcvAnnualFee =colNum("rcv_annual_fee");

      selectCrdCard(hh.cardNo);
      if (empty(hh.idPseqno)) {
         printf("卡號不存在, kk[%s]", hh.cardNo);
         continue;
      }

      insertCmsRightYear();
   }

   closeCursor();
   printf("-- 年度統計筆數[%s]", llTotCnt);
}
//--
int tiCard=-1;
void selectCrdCard(String a_cardNo) throws Exception {
   hh.idPseqno ="";
   hh.groupCode ="";
   hh.cardType ="";
   hh.acctType ="";
   hh.currentCode ="";
   hh.rmCarno ="";
   if (empty(a_cardNo)) return;

   int ll_nRow=dsCard.getKeyData(a_cardNo);
   if (ll_nRow <=0) return;
   hh.idPseqno =dsCard.colSs("id_p_seqno");
   hh.groupCode =dsCard.colSs("group_code");
   hh.cardType =dsCard.colSs("card_type");
   hh.acctType =dsCard.colSs("acct_type");
   hh.currentCode =dsCard.colSs("current_code");
   hh.rmCarno =dsCard.colSs("rm_carno");

//   if (tiCard <=0) {
//      sqlCmd ="select A.id_p_seqno, A.group_code, A.card_type"+
//              ", A.p_seqno, A.acct_type, A.current_code"+
//              ", B.rm_carno"+
//              " from crd_card A LEFT JOIN cms_roadMaster B"+
//              " ON A.card_no=B.card_no AND B.id_p_seqno=A.id_p_seqno"+
//              " where A.card_no =?"
//              ;
//      tiCard =ppStmtCrt("ti-card-S", "");
//   }
//
//   ppp(1, a_cardNo);
//   daoTid ="cc.";
//   sqlSelect(tiCard);
//   if (sqlNrow <=0) return;
//
//   hh.id_Pseqno =colSs("cc.id_p_seqno");
//   hh.group_code =colSs("cc.group_code");
//   hh.card_type =colSs("cc.card_type");
//   hh.acct_type =colSs("cc.acct_type");
//   hh.current_code =colSs("cc.current_code");
//   hh.rm_carno =colSs("cc.rm_carno");
}
//-------
void deleteCmsRightYear() throws Exception {
   sqlCmd ="delete cms_right_year"+
           " where acct_year =?"+
           " and item_no ='14' ";
   ppp(1, h_procYY);
   sqlExec(sqlCmd);
   if (sqlNrow <0) {
      sqlerr("delete cms_right_year");
      errExit(0);
   }
}
//--------
int tiApply=-1;
void selectBilMchtApplyTmp() throws Exception {
   if (tiApply <=0) {
      sqlCmd ="select count(*) as appl_cnt"+
              " from bil_mcht_apply_tmp"+
              " where card_no =?"+
              " and file_type ='05'"+
              " and crt_date between ? and ?"
              ;
      tiApply =ppStmtCrt("ti-apply-S","");
   }

   ppp(1, hh.cardNo);
   ppp(h_procYM+"01");
   ppp(h_procYM+"31");
   sqlSelect(tiApply);
   if (sqlNrow >0) {
      hh.useCnt =colInt("appl_cnt");
   }
}
//------
int tiAfee=-1;
void selectCycAfee() throws Exception {
   if (tiAfee <=0) {
      sqlCmd ="select rcv_annual_fee"+
              " from cyc_afee "+
              " where card_no =?"+
              " and fee_date between ? and ?"+
              " and rcv_annual_fee >0"+
              commSqlStr.rownum(1)
              ;
      tiAfee =ppStmtCrt("ti-afee-S","");
   }

   ppp(1, hh.cardNo);
   ppp(h_procYM+"01");
   ppp(h_procYM+"31");
   sqlSelect(tiAfee);
   if (sqlNrow >0) {
      hh.rcvAnnualFee =colNum("rcv_annual_fee");
   }
}
//--------
int tiPcons=-1;
void selectMktPostConsume() throws Exception {
   if (tiPcons <=0) {
      sqlCmd ="select " +
              " consume_bl_amt+consume_ca_amt+consume_it_amt" +
              "+consume_ao_amt+consume_id_amt+consume_ot_amt as consum_amt" +
              ", consume_bl_cnt+consume_ca_cnt+consume_it_cnt" +
              "+consume_ao_cnt+consume_id_cnt+consume_ot_cnt as consum_cnt"+
              " from mkt_post_consume"+
              " where card_no =?"+
              " and acct_month =?"
              ;
      tiPcons =ppStmtCrt("ti-pcons-S","");
   }

   ppp(1, hh.cardNo);
   ppp(h_procYM);
   sqlSelect(tiPcons);
   if (sqlNrow >0) {
      hh.purchAmt =colNum("consum_amt");
      hh.purchCnt =colInt("consum_cnt");
   }

}
//-----
com.Parm2sql ttRighU=null;
void updateCmsRightYearDtl() throws Exception {
   if (ttRighU ==null) {
      ttRighU = new Parm2sql();
      ttRighU.update("cms_right_year_dtl");
   }

   //000-k	acct_month          	VARCHAR (6,0)	帳務年月
   //001-k	id_p_seqno          	VARCHAR (10,0)	卡人流水號碼
   //002-k	card_no             	VARCHAR (19,0)	卡號
   //003-k	item_no             	VARCHAR (2,0)	權益類別
   ttRighU.aaa("proj_code", hh.projNo);     //VARCHAR (10,0)	適用專案
   ttRighU.aaa("use_cnt", hh.useCnt);  //INTEGER (4,0)	已使用次數
   //006	free_per_amt        	DECIMAL (12,0)	每次折抵金額
   ttRighU.aaa("curr_month_amt", hh.purchAmt);  //DECIMAL (12,0)	消費金額
   ttRighU.aaa("curr_month_cnt", hh.purchCnt);  //DECIMAL (12,0)	消費次數
   //009	platform_kind_amt   	DECIMAL (12,0)	排除一般消費金額
   //010	platform_kind_cnt   	DECIMAL (12,0)	排除一般消費次數
   //011	used_next_cnt       	INTEGER (4,0)	預支使用次數
   //012	gift_cnt            	INTEGER (4,0)	加贈次數
   //013	bonus_cnt           	INTEGER (4,0)	紅利兌換贈送次數
   ttRighU.aaa("rcv_annual_fee", hh.rcvAnnualFee);  //應收年費
   //015	crt_date            	VARCHAR (8,0)	建檔日期
   //016	crt_user            	VARCHAR (10,0)	建檔經辦
   //017	cal_seqno           	DECIMAL (10,0)	權益流水號
   //022	acct_type           	VARCHAR (2,0)	帳戶類別
   //023	card_type           	VARCHAR (2,0)	卡片種類
   //024	group_code          	VARCHAR (4,0)	團體代號
   ttRighU.aaa("free_type", "2");  //VARCHAR (1,0)	免費次數類別
   ttRighU.aaa("free_cnt", hh.freeCnt);  //INTEGER (4,0)	免費次數
   ttRighU.aaa("match_flag", hh.matchFlag);  //符合優惠註記
   ttRighU.aaaModxxx(hModUser, hModPgm);
   ttRighU.aaaWhere("where acct_month =?", h_procYM);
   ttRighU.aaaWhere(" and card_no =?", hh.cardNo);
   ttRighU.aaaWhere(" and item_no =?", "14");

   if (ttRighU.ti <=0) {
      ttRighU.ti =ppStmtCrt("tt-righ-U", ttRighU.getSql());
   }

   sqlExec(ttRighU.ti, ttRighU.getParms());
   if (sqlNrow <0) {
      sqlerr("update cms_right_year_dtl");
      return;
   }
   if (sqlNrow ==0) {
      insertCmsRightYearDtl();
   }

}

//------
com.Parm2sql ttRighA=null;
void insertCmsRightYearDtl() throws Exception {
   if (ttRighA ==null) {
      ttRighA = new Parm2sql();
      ttRighA.insert("cms_right_year_dtl");
   }

   ttRighA.aaa("acct_month", h_procYM);
   ttRighA.aaa("id_p_seqno", hh.idPseqno);
   ttRighA.aaa("card_no", hh.cardNo);
   ttRighA.aaa("item_no", "14");  //道路救援
   ttRighA.aaa("proj_code", hh.projNo);  //適用專案
   ttRighA.aaa("use_cnt", hh.useCnt);  //INTEGER (4,0)	已使用次數
//   ttRighA.aaa("free_per_amt        	DECIMAL (12,0)	每次折抵金額
   ttRighA.aaa("curr_month_amt", hh.purchAmt);  //DECIMAL (12,0)	消費金額
   ttRighA.aaa("curr_month_cnt", hh.purchCnt);  //DECIMAL (12,0)	消費次數
//   ttRighA.aaa("platform_kind_amt   	DECIMAL (12,0)	排除一般消費金額
//   ttRighA.aaa("platform_kind_cnt   	DECIMAL (12,0)	排除一般消費次數
//   ttRighA.aaa("used_next_cnt       	INTEGER (4,0)	預支使用次數
//   ttRighA.aaa("gift_cnt            	INTEGER (4,0)	加贈次數
//   ttRighA.aaa("bonus_cnt           	INTEGER (4,0)	紅利兌換贈送次數
   ttRighA.aaa("rcv_annual_fee", hh.rcvAnnualFee);  //DECIMAL (10,2)	應收年費
   ttRighA.aaa("crt_date", sysDate);  //VARCHAR (8,0)	建檔日期
   ttRighA.aaa("crt_user", hModUser);  //VARCHAR (10,0)	建檔經辦
//   ttRighA.aaa("cal_seqno           	DECIMAL (10,0)	權益流水號
   ttRighA.aaa("acct_type", hh.acctType);  //VARCHAR (2,0)	帳戶類別
   ttRighA.aaa("card_type", hh.cardType);   //VARCHAR (2,0)	卡片種類
   ttRighA.aaa("group_code", hh.groupCode);    //VARCHAR (4,0)	團體代號
   ttRighA.aaa("free_type", "2");    //VARCHAR (1,0)	免費次數類別
   ttRighA.aaa("free_cnt", hh.freeCnt);     //INTEGER (4,0)	免費次數
   ttRighA.aaa("match_flag", hh.matchFlag);  //VARCHAR (1,0)	符合優惠註記
   ttRighA.aaaModxxx(hModUser, hModPgm);

   if (ttRighA.ti <=0) {
      ttRighA.ti =ppStmtCrt("tt-Right-A",ttRighA.getSql());
   }

   sqlExec(ttRighA.ti, ttRighA.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert cms_right_year_dtl");
   }

}

//--------
com.Parm2sql ttAyear=null;
void insertCmsRightYear() throws Exception {
   if (ttAyear ==null) {
      ttAyear =new Parm2sql();
      ttAyear.insert("cms_right_year");
   }

   ttAyear.aaa("acct_year", h_procYY);  //VARCHAR (4,0)	帳務年度
   ttAyear.aaa("id_p_seqno", hh.idPseqno);  //卡人流水號碼
   ttAyear.aaa("card_no", hh.cardNo);  //V卡號
   ttAyear.aaa("acct_type", hh.acctType);  //帳戶類別
   ttAyear.aaa("card_type", hh.cardType);  //卡片種類
   ttAyear.aaa("group_code", hh.groupCode);  //團體代號
   ttAyear.aaa("item_no", "14");  //VARCHAR (2,0)	權益類別
   ttAyear.aaa("proj_code", hh.projNo);  //VARCHAR (10,0)	適用專案
   ttAyear.aaa("free_type", "2");  //VARCHAR (1,0)	免費次數類別
   ttAyear.aaa("free_cnt", hh.freeCnt);  //INTEGER (4,0)	免費次數
   ttAyear.aaa("use_cnt", hh.useCnt);  //INTEGER (4,0)	已使用次數
   //011	free_per_amt        	DECIMAL (12,0)	每次折抵金額
   //012	curr_proj_amt       	DECIMAL (12,0)	當年專案金額
   //013	last_proj_amt       	DECIMAL (12,0)	前年專案金額
   //014	last_year_consume   	DECIMAL (12,0)	前年消費金額
   ttAyear.aaa("curr_year_consume", hh.purchAmt);  //DECIMAL (12,0)	當年消費金額
   ttAyear.aaa("curr_year_cnt", hh.purchCnt);  //DECIMAL (12,0)	"當年消費次數   		"
   //017	last_year_cnt       	DECIMAL (12,0)	"前年消費次數 		"
   //018	platform_kind_amt   	DECIMAL (12,0)	當年排除一般消費金額
   //019	platform_kind_cnt   	DECIMAL (12,0)	當年排除一般消費次數
   //020	used_next_cnt       	INTEGER (4,0)	"預支使用次數 		"
   //021	gift_cnt            	INTEGER (4,0)	"加贈次數     		"
   //022	bonus_cnt           	INTEGER (4,0)	"紅利兌換贈送次數  	"
   //023	rcv_annual_fee      	DECIMAL (10,2)	應收年費
   ttAyear.aaa("rm_carno", hh.rmCarno);  //VARCHAR (10,0)	車號
   //025	free_cal_date       	VARCHAR (8,0)	權益計算日期
   //026	card_hldr_flag      	VARCHAR (1,0)	是否行員
   //027	debut_type          	VARCHAR (1,0)	卡友身分
   ttAyear.aaa("crt_date", hBusiDate);
   ttAyear.aaa("crt_user", hModUser);
   //030	cal_seqno           	DECIMAL (10,0)	權益流水號
   ttAyear.aaaModxxx(hModUser, hModPgm);

   if (ttAyear.ti <=0) {
      ttAyear.ti =ppStmtCrt("tt-year-A", ttAyear.getSql());
   }

   sqlExec(ttAyear.ti, ttAyear.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert cms_right_year");
   }
}
//===================
com.Parm2sql ttRdsDA=null;
void insertCmsRoadDetail(int liRC) throws Exception {
   if (ttRdsDA ==null) {
      ttRdsDA =new com.Parm2sql();
      ttRdsDA.insert("cms_roaddetail");
   }

   ////stop_reason:1.到期不續購,2.消費不足暫停服務,3.卡片已為無效卡,4.卡友來電要求停用
   //               ,5.金卡提升為白金卡,6.未達年度續用標準,7.無車號且非自動登錄卡,8更換卡號(停用此卡)
   //rdStatus=0,1
   //stopReason=2,3(),7(rds_pcard=A, rm_carno='')
   hh.rdStatus ="1";
   if (liRC==0 && !eq(hh.currentCode,"0")) {
      hh.rdStatus ="0";  //停用--
      hh.rdStopdate =sysDate;
      hh.rdStoprsn ="3";
   }
   else if (liRC==0 && eq(hh.rdsPcard,"A") && empty(hh.rmCarno)) {
      hh.rdStatus ="0";  //停用--
      hh.rdStopdate =sysDate;
      hh.rdStoprsn ="7";
   }
   else if (liRC !=0) {
      hh.rdStatus ="0";  //停用--
      hh.rdStopdate =sysDate;
      hh.rdStoprsn ="2";
   }

   int liSeqNo=ooParm.getrdSeqNo(sysDate);

   ttRdsDA.aaaYmd("rd_moddate");  //異動日期
   ttRdsDA.aaa("rd_seqno", liSeqNo);  //登錄序號
   ttRdsDA.aaa("rd_modtype", "B");  //異動來源         O:online, B:batch
   ttRdsDA.aaa("card_no", hh.cardNo);  //正卡卡號
   //004	new_card_no	VARCHAR (19,0)	新卡號
   ttRdsDA.aaa("rd_type", "F");	//救援類別         F:免費, E:自費
   ttRdsDA.aaa("appl_card_no", hh.cardNo);  //申請卡號
   ttRdsDA.aaa("group_code", hh.groupCode);  //團體代號
   ttRdsDA.aaa("rd_carno", hh.rmCarno);  //車號
   ttRdsDA.aaa("rd_carmanname", hh.rmCarmanname);  //車主姓名
   ttRdsDA.aaa("rd_carmanid", hh.rmCarmanid);  //車主身份証號
   //011	rd_newcarno	VARCHAR (10,0)	新車號
   //012	rd_htelno1	VARCHAR (5,0)	自宅電話1
   //013	rd_htelno2	VARCHAR (15,0)	自宅電話2
   //014	rd_htelno3	VARCHAR (6,0)	自宅電話3
   //015	rd_otelno1	VARCHAR (5,0)	公司電話1
   //016	rd_otelno2	VARCHAR (15,0)	公司電話2
   //017	rd_otelno3	VARCHAR (6,0)	公司電話3
   //018	cellar_phone	VARCHAR (30,0)	手機
   //019	rd_validdate	VARCHAR (8,0)	有效期限         YYMM
   ttRdsDA.aaa("rd_status", hh.rdStatus);  //	VARCHAR (1,0)	異動狀態 1:啟用, 2:變更車號, 0:停用
   //021	rd_payamt	DECIMAL (13,2)	自費金額
   //022	rd_payno	VARCHAR (12,0)	請款批號
   //023	rd_paydate	VARCHAR (8,0)	請款日期
   ttRdsDA.aaa("rd_stopdate", hh.rdStopdate);  //	VARCHAR (8,0)	停用日期
   ttRdsDA.aaa("rd_stoprsn", hh.rdStoprsn);  //	VARCHAR (10,0)	停用原因
   ttRdsDA.aaa("crt_user", hModPgm);  //登錄者
   ttRdsDA.aaa("crt_date", sysDate);  //登錄日期
   ttRdsDA.aaa("apr_user", hModUser);  //覆核主管
   ttRdsDA.aaaYmd("apr_date");  //覆核日期
   ttRdsDA.aaa("rd_senddate", "");  //	VARCHAR (8,0)	RD_SENDDATE
   ttRdsDA.aaa("rd_sendsts", "");  //緊急登錄	 VARCHAR (10,0)	RD_SENDSTS
   ttRdsDA.aaa("rd_sendyn", "");  //	VARCHAR (1,0)	RD_SENDYN
   //033	rd_sendadd	INTEGER (4,0)	RD_SENDADD
   //034	rd_sendstop	INTEGER (4,0)	RD_SENDSTOP
   ttRdsDA.aaa("proj_no", hh.projNo);  //	VARCHAR (10,0)	專案代號
   ttRdsDA.aaa("purch_amt", hh.purchAmt);  //	DECIMAL (11,0)	累積消費金額
   ttRdsDA.aaa("purch_cnt", hh.purchCnt);  //INTEGER (4,0)	累積消費次數
   ttRdsDA.aaa("purch_amt_lyy", hh.purchAmtLyy);  //DECIMAL (11,0)	上年累計消費金額
   //039	cardholder_type	VARCHAR (1,0)	卡人類別
   ttRdsDA.aaa("rds_pcard", hh.rdsPcard);  //免費道路救援類別
   ttRdsDA.aaa("id_p_seqno", hh.idPseqno);  //ID編號
   ttRdsDA.aaa("outstanding_yn", hh.outstandingYn);  //VARCHAR (1,0)	未繳最低應繳金額Y/N
   //047	outstanding_cond	VARCHAR (1,0)	未繳最低應繳金額不適用(參數)Y/N
   ttRdsDA.aaa("give_flag", "N");  //贈送註記
   ttRdsDA.aaaModxxx(hModUser, hModPgm);

   if (ttRdsDA.ti <=0) {
      ttRdsDA.ti =ppStmtCrt("tt-rdsd-A",ttRdsDA.getSql());
   }

//	debug=true;
//	dddSql(ttRdsDA.ti, ttRdsDA.getConvParm(false));
//	debug=false;

   sqlExec(ttRdsDA.ti, ttRdsDA.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert Cms_roaddetail error");
      errExit(1);
   }
}
//--------
com.Parm2sql ttRdsMU=null;
void updateCmsRoadMaster(int liRC) throws Exception {
   if (ttRdsMU ==null) {
      ttRdsMU =new Parm2sql();
      ttRdsMU.update("cms_roadMaster");
   }

   ttRdsMU.aaa("rm_status", hh.rdStatus);
   ttRdsMU.aaa("rm_moddate",sysDate);   //-x(8)  異動日期
   ttRdsMU.aaa("rm_reason", hh.rdStoprsn);   //-x(10)  異動原因
   ttRdsMU.aaa("outstanding_yn",hh.outstandingYn);
   ttRdsMU.aaa("give_flag", "N");
   ttRdsMU.aaaModxxx(hModUser, hModPgm);
   ttRdsMU.aaaWhere("where rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)", hh.rowId);

   if (ttRdsMU.ti <=0) {
      ttRdsMU.ti =ppStmtCrt("tt-rdsM-U",ttRdsMU.getSql());
   }

   sqlExec(ttRdsMU.ti, ttRdsMU.getParms());
   if (sqlNrow <=0) {
      sqlerr("update cms_roadMaster error");
      errExit(1);
   }
}

}
