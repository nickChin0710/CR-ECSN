package Rsk;
/**
 * 2022-0519 V1.03.00   JH    9399: VD無銷帳鍵值
 * 2022-0113 V1.02.00   JH    9213: I3啟帳R045[3,8]
 * 2021-1027 V1.01.00   JH    8940: memo3
 * 2021-0127 V1.00.00   JH    h_gsvh_mod_ws
 * 2020-1211 V1.00.00   JH    comcr_menoInit
 * 2020-0915 V1.00.00   JH    comcrDetailVouch()
 * 2020-0521 V1.00.00   JH    curr_code_gl
 * 2020-0430 V1.00.00   JH    modify
 * ??????????????????????????????????
 * clo_result:08,52,91: 未指定套號
 *
 * 2019-0705 V1.00.00   JH    合併啟帳
 * */

import com.CommCrdRoutine;
import com.SqlParm;
import com.BaseBatch;

public class RskP003 extends BaseBatch {
private final String PROGNAME = "風管會計分錄啟帳處理  2022-0519 V1.03.00";
CommCrdRoutine comcr = null;
//=============================================================================
hdata.RskProblem hPrbl=new hdata.RskProblem();

//---------------------------------------------
private final String isGlDept="";
private String isGlDate="";
private final String isRefNo="";
private String isGlCurr="";
private String isStdCd="";
private String isGlCode="";
//-------------------------------------------------
private boolean ibMerge=false;
boolean ibOversea=false;
//private String kk_std_vouch="";
private String kkGlmemo3="";
private double kkMchtRepay=0;
private String kkCurrCode="";
private int kkSeqno=0;
private double kkCloseFee=0;
private double kkPrbAmt=0;
boolean kkOversea =false;
private String hIdno="";
int iiSysVouch=0;
//----------------------------------------------
SqlParm ttAcct=null;
com.SqlParm ttVou=new com.SqlParm();
private int tiProblemM=-1;
private int tiSysvouch=-1;
private final int tiPrbl2=-1;
private int tiPrbl1=-1;
private int tiCurrcode=-1;
private int tiCurrcode2=-1;
private int tiAcctlogU=-1;
private int tiMemo3=-1;
int tiIdno=-1, tiIdno2=-1;

int commit=1;

//=****************************************************************************
public static void main(String[] args) {
   RskP003 proc = new RskP003();
   //proc.debug =true;
   proc.mainProcess(args);
   proc.systemExit();
}

@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length>1) {
      printf("Usage : RskP003 [busi_date]");
      errExit(1);
   }

   if (args.length==1) {
      setBusiDate(hBusiDate);
   }
   dbConnect();

   //--
   comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
   comcr.hGsvhModPgm =hModPgm;
   comcr.hGsvhModWs ="RSK_P003R0";

   selectRskAcctLogM();
   selectRskAcctLog();

   sqlCommit(commit);
   endProgram();
}

//=****************************************************************************
@Override
protected void selectPtrBusinday() throws Exception {
   sqlCmd ="select business_date, vouch_date"
         +" from ptr_businday"
         +" where 1=1"
         +commSqlStr.rownum(1);
   sqlSelect();
   if (sqlNrow<=0) {
      errmsg("讀取營業日失敗");
      return;
   }

   if (empty(hBusiDate)) {
      hBusiDate = colSs("business_date");
   }
   isGlDate =colSs("vouch_date");

   printf("-->營業日=[%s], 會計起帳日=[%s]",hBusiDate,isGlDate);
}
int comcrDetailVouch(String aAcno, int aiSeqno, double amAmt, String aCurr) throws Exception {
   comcr.hVoucSysRem =hPrbl.referenceNo+"|"+isStdCd;
   int liRc =comcr.detailVouch(aAcno, aiSeqno, amAmt,aCurr);
   comcrMemoInit();
   return liRc;
}
void comcrMemoInit() throws Exception {
   comcr.hVoucIdNo ="";
   comcr.hGsvhMemo1 ="";
   comcr.hGsvhMemo2 ="";
   comcr.hGsvhMemo3 ="";
   comcr.hVoucSysRem ="";
}

//=****************************************************************************
void selectRskAcctLogM() throws Exception {
   ibMerge =true;
   ddd("-->合併啟帳------");

   //-合併啟帳-
   sqlCmd = "select A.std_vouch_cd,"
         + " B.prb_glmemo3,"
         + " B.curr_code, max(A.user_deptno) as user_deptno,"
         + " sum(uf_dc_amt2(B.prb_amount,B.dc_prb_amount)) as dc_prb_amount,"
         + " sum(uf_dc_amt2(B.mcht_repay,B.dc_mcht_repay)) as dc_mcht_repay,"
         + " sum(B.mcht_close_fee) as mcht_close_fee,"
         + " sum(decode(A.oversea_fee_flag,'Y',1,0)) as oversea_cnt,"
         + " count(*) as xx_cnt"
         + " from rsk_acct_log A join rsk_problem B on B.ctrl_seqno=A.ctrl_seqno and B.reference_no=A.reference_no"
         + " where 1=1"
         + " and A.table_id='PRBL' and A.vouch_merge_flag='Y'"
         + " and A.std_vouch_cd<>'' "
         + " and A.vouch_flag ='Y' and A.vouch_proc_flag<>'Y' "
         + " and A.rsk_status ='801'"
         + " and B.prb_glmemo3<>''"
         + " group by A.std_vouch_cd, B.prb_glmemo3, B.curr_code"
         + " having count(*) >1 "
         + " order by A.std_vouch_cd, B.prb_glmemo3, B.curr_code"
   ;

   this.openCursor();
   while (this.fetchTable()) {
      if (colInt("xx_cnt") <2)
         continue;

      kkGlmemo3 =colSs("prb_glmemo3");
      kkCurrCode =colSs("curr_code");
      kkPrbAmt =colNum("dc_prb_amount");
      kkMchtRepay =colNum("dc_mcht_repay");
      kkCloseFee =colNum("mcht_close_fee");
      kkOversea =(colInt("oversea_cnt")>0);
      kkSeqno =0;
      isStdCd =colSs("std_vouch_cd");
      isGlCode =getisGlCode(colSs("user_deptno"));
      if (empty(isGlCode)) {
         printf("無法取得起帳部門代碼, dept_no=" + colSs("user_deptno"));
         continue;
      }

      selectGenSysVouch(isStdCd);
      if (iiSysVouch <=0) continue;

      selectPtrCurrcode(kkCurrCode);
      //-會計啟動-
      comcr.startVouch(isGlCode,isStdCd);
      comcr.hGsvhRefNo ="";
      comcr.hVoucIdNo ="";

      if (tiProblemM <=0) {
         String sql1 ="select A.ctrl_seqno, A.mchtCloseFee, A.dest_amt,"
               +" A.card_no, A.debit_flag, A.reference_no,"
               +" uf_dc_amt2(A.dest_amt,A.dc_dest_amt) as dc_dest_amt,"
               +" uf_dc_amt2(A.prb_amount,A.dc_prb_amount) as dc_prb_amount,"
               +" uf_dc_curr(A.curr_code) as curr_code,"
               +" uf_dc_amt2(A.mcht_repay,A.dc_mcht_repay) as dc_mcht_repay,"
               +" A.prb_glmemo3,"
               +" uf_idno_id2(A.id_p_seqno,A.debit_flag) as db_idno,"
               +" A.clo_result,"
               +" B.oversea_fee_flag,"
               +" hex(B.rowid) as acct_rowid"
               +" from rsk_problem A join rsk_acct_log B on A.ctrl_seqno=B.ctrl_seqno and A.reference_no=B.reference_no"
               +" where B.vouch_flag ='Y' and B.table_id='PRBL'"
               +" and B.std_vouch_cd =?"
               +" and B.vouch_proc_flag <>'Y'"
               +" and B.rsk_status ='801' "
               +" and A.prb_glmemo3 =? and A.curr_code =?"
               +" order by A.ctrl_seqno";
         tiProblemM =ppStmtCrt("ti_problem-M",sql1);
      }
      ppp(1,isStdCd);
      ppp(kkGlmemo3);
      ppp(kkCurrCode);

      daoTid ="prbl.";
      sqlSelect(tiProblemM);
      int liNrow=sqlNrow;
      //-----------------------------------------
      for(int ll=0; ll<liNrow; ll++) {
         totalCnt++;

         hPrbl.initData();
         hPrbl.ctrlSeqno =colSs(ll,"prbl.ctrl_seqno");
         hPrbl.referenceNo =colSs(ll,"prbl.reference_no");
         hPrbl.mchtCloseFee =colNum(ll,"prbl.mcht_close_fee");
         hPrbl.destAmt =colNum(ll,"prbl.dest_amt");
         hPrbl.debitFlag =colNvl(ll,"prbl.debit_flag","N");
         hPrbl.cardNo =colSs(ll,"prbl.card_no");
         hPrbl.dcDestAmt =colNum(ll,"prbl.dc_dest_amt");
         hPrbl.dcPrbAmount =colNum(ll,"prbl.dc_prb_amount");
         hPrbl.currCode =colSs(ll,"prbl.curr_code");
         hPrbl.dcMchtRepay =colNum(ll,"prbl.dc_mcht_repay");
         hPrbl.prbGlmemo3 =colSs(ll,"prbl.prb_glmemo3");
         hPrbl.cloResult =colSs(ll,"prbl.clo_result");
         ibOversea =colEq(ll,"prbl.oversea_fee_flag","Y");
         hIdno =getIdno(hPrbl.cardNo,hPrbl.debitFlag);

         if (eq(isStdCd,"R021"))
            R021Detail();
         else if (eq(isStdCd,"R023"))
            R023Detail();
         else if (eq(isStdCd,"R028"))
            R028Detail();
         else if (eq(isStdCd,"R041"))
            R041Detail();
         else if (eq(isStdCd,"R044"))
            R044Detail();
         else if (eq(isStdCd,"R045"))
            R045Detail();
         else continue;

         updateRskAcctLog(colSs(ll,"prbl.acct_rowid"));
      }

      if (eq(isStdCd,"R021"))
         R021Total();
      else if (eq(isStdCd,"R023"))
         R023Total();
      else if (eq(isStdCd,"R028"))
         R028Total();
      else if (eq(isStdCd,"R041"))
         R041Total();
      else if (eq(isStdCd,"R044"))
         R044Total();
      else if (eq(isStdCd,"R045"))
         R045Total();
      else continue;

      //-合併完成-
      this.sqlCommit(1);
   }  //while

   closeCursor();
}
void mergeVouchDetail() throws Exception {
   comcrMemoInit();
   switch (isStdCd) {
      case "R021":
         R021Detail(); break;
      case "R023":
         R023Detail(); break;
      case "R028":
         R028Detail(); break;
      case "R041":
         R041Detail(); break;
      case "R044":
         R044Detail(); break;
      case "R045":
         R045Detail(); break;
      case "R019":
      case "R022":
         ComglDetail(); break;
   }

}
void mergeVouchTotal() throws Exception {
   switch (isStdCd) {
      case "R021":
      R021Total(); break;
      case "R023":
      R023Total(); break;
      case "R028":
      R028Total(); break;
      case "R041":
      R041Total(); break;
      case "R044":
      R044Total(); break;
      case "R045":
      R045Total(); break;
      case "R019":
      case "R022":
      ComglTotal(); break;
   }
}
//=****************************************************************************
void selectRskAcctLog() throws Exception {
   ibMerge =false;
   ddd("-->單一啟帳------");
   //-單一啟帳-
   daoTable ="select_rsk_acct_log";
   sqlCmd="SELECT A.*"
         +" , hex(rowid) as rowid"
         +" from rsk_acct_log A"
         +" where vouch_flag ='Y'"
         +" and std_vouch_cd<>'' "
         +" and vouch_proc_flag <>'Y' "
         +" order by ctrl_seqno, table_id"
   ;

   //	this.fetchExtend ="acct.";
//   this.openCursor();
//   while (this.fetchTable()) {
   daoTid ="acct.";
   sqlSelect();
   int llNrow=sqlNrow;
   for (int ii = 0; ii <llNrow ; ii++) {
      this.totalCnt++;

      isStdCd =colSs(ii,"acct.std_vouch_cd");
      ibOversea =colEq(ii,"acct.oversea_fee_flag","Y");
      isGlCode =getisGlCode(colSs(ii,"acct.user_deptno"));
      String lsReferNo =colSs(ii,"acct.reference_no");
      String lsRowid =colSs(ii,"acct.rowid");
      String lsRskStatus=colSs(ii,"acct.rsk_status");
      String lsCtrlSeqno=colSs(ii,"acct.ctrl_seqno");

      if (empty(isGlCode)) {
         printf("無法取得起帳部門代碼, dept_no="+colSs(ii,"acct.user_deptno"));
         continue;
      }

      //-問交第一次結案-
      if (eq(lsRskStatus,"801")) {
         selectRskProblem(lsCtrlSeqno,lsReferNo, 1);
         if (rc !=0)
            return;
      }
      else continue;
//      else if (col_eq("rsk_status","802")) {
//         //-二次結案-
//         selectRskProblem(lsReferNo, 2);
//         if (rc !=0)
//            return;
//      }

      kkSeqno=0;
      insertGenVouch();

      updateRskAcctLog(lsRowid);
      if (debug) {}
      else {
         this.sqlCommit(1);
      }
   }

//   closeCursor();
}

//=****************************************************************************
void insertGenVouch() throws Exception {
   rc =0;
   //ddd("-->ctrl_seqno[%s], glmemo3[%s], std_cd[%s]",hPrbl.ctrl_seqno, hPrbl.prb_glmemo3,isStdCd);

   if (empty(isStdCd))
      return;

   boolean lbDebit=commSqlStr.isDebit(hPrbl.debitFlag);
   //-起始-
   comcr.startVouch(isGlCode, isStdCd);
   comcr.hVoucIdNo =hIdno;
   comcr.hGsvhRefNo =hPrbl.referenceNo;

   if (eqIgno(isStdCd,"R019")) {
      //19
      ComglVouch(2);
   }
   else if (eqIgno(isStdCd,"R021")) {
      R021Vouch();
   }
   else if (eqIgno(isStdCd,"R022")) {
      //'15','16','17','65','66','67'
      ComglVouch(2);
   }
   else if (eqIgno(isStdCd,"R023")) {
      //'18','20'
      R023Vouch();
   }
   //	else if (eq_igno(isStdCd,"R024")) {
   //		//'21': wf_R024_rskok()
   //		vouch_R024();
   //	}
   else if (eqIgno(isStdCd,"R028")) {
      //51,52
      R028Vouch();
   }
   else if (eqIgno(isStdCd,"R041")) {
      //01,02	不合格帳單 本行損失
      R041Vouch();
   }
   //	else if (eq_igno(isStdCd,"R042")) {
   //		// wf_r042_rskok()
   //	}
   else if (eqIgno(isStdCd,"R044")) {
      //'06'
      R044Vouch();
   }
   else if (eqIgno(isStdCd,"R045")) {
      //07.有借貸差額
      R045Vouch();
   }
   else if (lbDebit && eqIgno(isStdCd,"R-D0")) {
      //[D0]
      ComglVouch(2);
   }
   else if (lbDebit && eqIgno(isStdCd,"R-D1")) {
      RD1Vouch();
   }
   else if (lbDebit && eqIgno(isStdCd,"R-D2")) {
      RD2Vouch();
   }
   else if (lbDebit && eqIgno(isStdCd,"R-D3")) {
      RD3Vouch();
   }
   //	else if (lbDebit && eq_igno(isStdCd,"R-D7")) {
   //		//[D7] wf_RD7_rskok()
   //	}
   else if (lbDebit && eqIgno(isStdCd,"R-D8")) {
      //D8,DA
      RD8Vouch();
   }
   else if (lbDebit && eqIgno(isStdCd,"R-D9")) {
      //[D9]
      RD9Vouch();
   }
   else if (lbDebit && eqIgno(isStdCd,"R-N9")) {
      //[N9]
      ComglVouch(2);
   }
}

//=****************************************************************************
String getisGlCode(String a_deptno) throws Exception {
   sqlCmd="select gl_code"
         +" from ptr_dept_code"
         +" where 1=1"
         +commSqlStr.col(a_deptno,"dept_code")
         +commSqlStr.rownum(1)
   ;
   sqlSelect();
   if (sqlNrow<=0) {
      printf("select ptr_dept_code error; kk="+a_deptno);
      return "";
   }
   if (colEmpty("gl_code")) {
      errmsg("部門.GL_CODE is empty");
   }
   return colSs("gl_code");

}
//int select_gl_seqno(String a_deptno) throws Exception{
//   String lsisGlCode=getisGlCode(a_deptno);
//   if (empty(lsisGlCode)) {
//      return 0;
//   }
//   isGlDept ="UB"+lsisGlCode;
//
//   sqlCmd ="select max(substr(refno,4,3)) as max_refno"
//         +" from gen_vouch"
//         +" where tx_date =?"
//         +" and substr(refno,1,3) =?"
//   ;
//   sqlSelect("",new Object[]{
//         isGlDate,isGlDept
//   });
//   if (sqlNrow<=0) {
//      isRefNo =isGlDept+"001";
//   }
//   else isRefNo =isGlDept+commString.num_format(col_num("max_refno")+1,"000");
//   //refno	=Mid(refno,1,3)+String((Long(Mid(refno,4,3))+1),"000")
//   return 1;
//}
//=****************************************************************************
void selectGenSysVouch(String aStdCd) throws Exception {
   iiSysVouch =-1;
   if (tiSysvouch <=0) {
      sqlCmd ="select * "
            +" from gen_sys_vouch"
            +" where std_vouch_cd =?"
            +" order by dbcr_seq";
      tiSysvouch =ppStmtCrt("tiSysvouch","");
   }
   daoTid ="svou.";
   sqlSelect(tiSysvouch,new Object[]{aStdCd});

   iiSysVouch =sqlNrow;
   if (commString.ssIn(aStdCd,"R021,R023,R041,R044,R-D1,R-D2,R-D3,R-D8,R-D9")) {
      if (iiSysVouch ==4) return;
   }
   else if (commString.ssIn(aStdCd,"R028") ) {
      if (iiSysVouch ==7) return;
   }
   else if (commString.ssIn(aStdCd,"R045") ) {
      if (iiSysVouch ==8) return;
   }
   else {
      if (iiSysVouch >=2) return;
   }

   errmsg("系統自動起帳會計分錄錯誤, [%s]-[%s]",aStdCd,sqlNrow);
   iiSysVouch = -1;
   return;
}
//=****************************************************************************
void selectRskProblem(String aCtrlSeq, String aRefno, int aiClo) throws Exception {
   //一次結案--
   if (aiClo==1 && tiPrbl1 <=0) {
      sqlCmd ="select ctrl_seqno, mcht_close_fee, dest_amt,"
            +" debit_flag, card_no, reference_no,"
            +" uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt,"
            +" uf_dc_amt2(prb_amount,dc_prb_amount) as dc_prb_amount,"
            +" uf_dc_curr(curr_code) as curr_code,"
            +" uf_dc_amt2(mcht_repay,dc_mcht_repay) as dc_mcht_repay,"
            +" prb_glmemo3,"
            +" uf_idno_id2(id_p_seqno,debit_flag) as db_idno,"
            +" clo_result,"
            +" '' as xxx"
            +" from rsk_problem"
            +" where ctrl_seqno =?"
            +" and reference_no =?"
      //+" and   card_no =?"
      ;
      daoTable ="rsk_problem-S1";
      tiPrbl1 =ppStmtCrt("ti-prbl1","");
   }
   //二次結案--
//   if (aiClo==2 && tiPrbl2 <=0) {
//      sqlCmd ="select ctrl_seqno, dest_amt, "
//            +" mchtCloseFee_2 as mchtCloseFee,"
//            +" debit_flag, card_no, reference_no,"
//            +" uf_dc_amt2(dest_amt,dc_dest_amt) as dc_dest_amt, "
//            +" uf_dc_amt2(prb_amount,dc_prb_amount) as dc_prb_amount, "
//            +" uf_dc_curr(curr_code) as curr_code, "
//            +" uf_dc_amt2(mcht_repay_2,dcMchtRepay_2) as dcMchtRepay, "
//            +" prb_glmemo3_2 as prb_glmemo3,"
//            +" uf_idno_id2(id_p_seqno,debit_flag) as db_idno,"
//            +" clo_result_2 as clo_result,"
//            +" '' as xxx"
//            +" from rsk_problem"
//            +" where ctrl_seqno =?"
//            +" and   reference_no =?"
//      ;
//      daoTable ="rsk_problem-S1";
//      tiPrbl2 =ppStmt_crt("ti-prbl-2","");
//   }

   daoTid ="prbl.";
   ppp(1,aCtrlSeq);
   ppp(aRefno);
   if (aiClo==1) {
      sqlSelect(tiPrbl1);
   }
//   else if (aiClo==2) {
//      sqlSelect(tiPrbl2);
//   }
   if (sqlNrow<=0) {
      errmsg("查無問交資料, [%s]", aCtrlSeq);
      return;
   }
   hPrbl.initData();
   hPrbl.ctrlSeqno =colSs("prbl.ctrl_seqno");
   hPrbl.mchtCloseFee =colNum("prbl.mcht_close_fee");
   hPrbl.destAmt =colNum("prbl.dest_amt");
   hPrbl.debitFlag =colNvl("prbl.debit_flag","N");
   hPrbl.cardNo =colSs("prbl.card_no");
   hPrbl.referenceNo =colSs("prbl.reference_no");
   hPrbl.dcDestAmt =colNum("prbl.dc_dest_amt");
   hPrbl.dcPrbAmount =colNum("prbl.dc_prb_amount");
   hPrbl.currCode =colSs("prbl.curr_code");
   hPrbl.dcMchtRepay =colNum("prbl.dc_mcht_repay");
   hPrbl.prbGlmemo3 =colSs("prbl.prb_glmemo3");
   hPrbl.cloResult =colSs("prbl.clo_result");
   //hIdno =colSs("prbl.db_idno");
   hIdno =getIdno(hPrbl.cardNo,hPrbl.debitFlag);

   selectPtrCurrcode(hPrbl.currCode);
}

String getIdno(String asCardNo, String asDebit) throws Exception {
   String lsIdno="";
   if (tiIdno <=0) {
      sqlCmd ="select uf_idno_id(major_id_p_seqno) as xx_idno"+
            ", uf_corp_no(corp_p_seqno) as xx_corp, acno_flag"+
            " from crd_card where card_no =?";
      tiIdno =ppStmtCrt("ti-idno","");
   }
   if (tiIdno2 <=0) {
      sqlCmd ="select uf_vd_idno_id(id_p_seqno) as xx_idno"+
            ", '' as xx_corp, '1' as acno_flag"+
            " from dbc_card where card_no =?";
      tiIdno2 =ppStmtCrt("ti-idno-2","");
   }

   ppp(1,asCardNo);
   if (eq(asDebit,"Y")) {
      sqlSelect(tiIdno2);
   }
   else {
      sqlSelect(tiIdno);
   }
   if (sqlNrow >0) {
      if (colEq("acno_flag","Y"))
         lsIdno =colSs("xx_corp");
      else lsIdno =colSs("xx_idno");
   }

   return lsIdno;

}
//=****************************************************************************
void selectPtrCurrcode(String a_curr_code) throws Exception {

   if (tiCurrcode <=0) {
      sqlCmd ="select curr_code_gl"
            +" from ptr_currcode"
            +" where curr_code =?"
            +commSqlStr.rownum(1)
      ;
      tiCurrcode =ppStmtCrt("tiCurrcode","");
   }
   this.sqlSelect(tiCurrcode,new Object[]{a_curr_code});
   if (sqlNrow<=0) {
      isGlCurr ="00";
   }
   isGlCurr =colSs("curr_code_gl");

}
//=****************************************************************************
String wfVouchMemo3(int rr) throws Exception {
   //-合併-
   if (ibMerge && rr==0 && noEmpty(kkGlmemo3)) {
      return kkGlmemo3;
   }

   String lsDbcr =colSs(rr,"svou.dbcr");
   String lsAcNo =colSs(rr,"svou.ac_no");
   String lsCloResult =hPrbl.cloResult;
   //--系統列問交之銷帳鍵值--
   //-94-038:08,06取問交結案之銷帳鍵值[借方]-
   //-JH(B96-093):借方科目, 結案理由加D7-
   if (eq(lsDbcr,"D") && eq(lsAcNo,"24817000")) {
      if (strIN(lsCloResult,"|06|07|18|20|51|D1|D3|D7|D8|DA")) {
         return hPrbl.prbGlmemo3;
      }
   }

   String lsMemo3="";
   //-JH(B96-093):貸方科目-
   String ssAcno="|14815601";
   if (eqIgno(hPrbl.debitFlag,"Y")) {
      ssAcno ="|14816203|14816205";
   }
   if (eq(lsDbcr,"C") && strIN(lsAcNo,ssAcno)) {
      lsMemo3 =selectGenMemo3(hPrbl.referenceNo,lsAcNo);
   }
   //NOT[(lsCloResult = '18' or lsCloResult = '20') and lsDbcr = 'D']
   if (empty(lsMemo3) && !(eq(lsDbcr,"D") && strIN(lsCloResult,"|18,20"))) {
      lsMemo3 =selectGenMemo3(hPrbl.referenceNo,lsAcNo);
   }
   if (noEmpty(lsMemo3))
      return lsMemo3;

   //-標準Memo3:get memo3 之銷帳類別
   //--Return: "".none, 1.卡號, 2.ID+身份證字號, 3.套號
   String lsMemo3Kind="";
   sqlCmd ="select nvl(memo3_flag,'N') as memo3_flag"
         +", nvl(dr_flag,'N') as dr_flag"
         +", nvl(cr_flag,'N') as cr_flag"
         +", nvl(memo3_kind,'') as memo3_kind"
         +" from gen_acct_m"
         +" where 1=1"
         +commSqlStr.col(lsAcNo,"ac_no");
   this.sqlSelect();
   if (sqlNrow>0) {
      if (colEq("memo3_flag","Y")==false)
    	  lsMemo3Kind ="";
      else if (eq(lsDbcr,"D") && colEq("cr_flag","Y"))    //-貸方待沖傳票註記-
    	  lsMemo3Kind =colSs("memo3_kind");
      else if (eq(lsDbcr,"C") && colEq("dr_flag","Y"))    //-借方待沖傳票註記-
    	  lsMemo3Kind =colSs("memo3_kind");
   }

   //---on-line 銷帳鍵值--
   if (eq(lsMemo3Kind,"1")) {
      //-卡號-
      lsMemo3 = hPrbl.cardNo;
   }
   else if (eq(lsMemo3Kind,"2")) {
      //--ID
      lsMemo3 = "ID :"+hIdno;
   }
   else if (eq(lsMemo3Kind,"3")) {
      //--套號--
      lsMemo3 = this.isGlDate.substring(6,8)+this.isRefNo+commString.numFormat(rr+1,"00");
   }

   return lsMemo3;
}

//=****************************************************************************
String wfVouchMemo2(String aAcno) throws Exception {
   //	ls_real_cardno = Trim(dw_data.object.real_card_no[L])
   //			ls_acno		=Trim(iuo_comgl.ids_data2.object.ac_no[1])
   if (strIN(isStdCd,"|R044,R045,R023,R028,R-D1,R-D3,R-D7,R-D8")) {
      if (eq(aAcno,"24817000")) {
         return hPrbl.cardNo;
      }
   }
   else if (eq(isStdCd,"R-D6")) {
      if (eq(aAcno,"14816201"))
         return hPrbl.cardNo;
   }
   return "";
}
//=****************************************************************************
void R021Detail() throws Exception {
//-4-
   double lmAmt =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =lmFee * 0.05;
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   int rr=1;
   int liSeqno =rr+1;
   String lsAcNo =colSs(1,"svou.ac_no");
   lmAmt =lmAmt +lmFee;
   if (lmAmt==0) return;

   comcr.hGsvhMemo3 =wfVouchMemo3(rr);
   comcr.hGsvhCurr =isGlCurr;
   //--
   if (comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode) != 0) {
      errmsg("detail_vouch error[R021]-%s",liSeqno);
      errExit(1);
   }
}
void R021Total() throws Exception {
   double lmAmt =kkMchtRepay;
   double lmFee =kkOversea ? 0 : kkCloseFee;
   double lmTax =lmFee * 0.05;
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-合併1-row-
      if (rr==1) continue;
      //-3-row-
      if (rr==2 && lmFee==0) continue;
      //-4-row-
      if (rr==3 && lmTax==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      if (comcrDetailVouch(lsAcNo, liSeqno, lmAmt, kkCurrCode) != 0) {
         errmsg("total_vouch error[R021]-%s",liSeqno);
         errExit(1);
      }
   }
}
void R021Vouch() throws Exception {
   //-問交結案-
   rc =0;
   selectGenSysVouch("R021");
   if (iiSysVouch !=4) {
      errmsg("系統自動起帳會計分錄錯誤, [R021]");
      return;
   }

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =lmFee * 0.05;
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      if (rr==2 && lmFee==0) continue;
      if (rr==3 && lmTax==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt =0;
      if (rr ==0) {
         lmAmt =lmRepay;
      }
      else if (rr ==1) {
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      if (comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode) != 0) {
         errmsg("insert gen_vouch error[R021]-%s",liSeqno);
         errExit(1);
      }
   }
}
//=****************************************************************************
void R023Detail() throws Exception {
   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =lmFee * 0.05;
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   int rr=1;
   double lmAmt=0;
   String lsAcNo =colSs(1,"svou.ac_no");
   lmAmt =lmRepay+lmFee;
   if (lmAmt==0) return;

   //--
   int liSeqno =rr+1;
   comcr.hGsvhMemo3 =wfVouchMemo3(rr);
   comcr.hGsvhCurr =isGlCurr;

   if (comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode) != 0) {
      errmsg("detail_vouch error[R023]-%s",liSeqno);
      errExit(1);
   }
}
void R023Total() throws Exception {
   double lmAmt =kkMchtRepay;
   double lmFee =kkOversea ? 0 : kkCloseFee;
   double lmTax =lmFee * 0.05;
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   lmAmt +=lmFee;
   if (lmAmt==0) return;

   for(int rr=0; rr<4; rr++) {
      if (rr ==1) continue;  //detail
      if (rr==2 && lmFee==0) continue;
      if (rr==3 && lmTax==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      //--
      if (comcrDetailVouch(lsAcNo, liSeqno, lmAmt, kkCurrCode) != 0) {
         errmsg("tot.vouch error[R023]-%s",liSeqno);
         errExit(1);
      }
   }
}
void R023Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R023");
   if (iiSysVouch <=0)
      return;

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =lmFee * 0.05;
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      if (rr==2 && lmFee==0) continue;
      if (rr==3 && lmTax==0) continue;

      double lmAmt=0;
      String lsAcNo =colSs(rr,"svou.ac_no");
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =lmRepay;
      }
      else if (rr ==1) {
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;
      //--
      if (comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode) != 0) {
         errmsg("insert gen_vouch error[R023]-%s",liSeqno);
         errExit(1);
      }
   }
}

//=****************************************************************************
void R028Detail() throws Exception {
   /*
 03     14815601   3   應收款 信用卡問題款項發卡 ID+身分證號
 04     45020000   4   雜項收入
 05     24912400   5   其他應付款 銷項稅額
   * */
   double lmAmt =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   double lmDiffAmt =lmAmt +lmFee - hPrbl.dcPrbAmount;
   double lmDiffTax =commString.numScale(lmDiffAmt * 0.05,0);
   //14815601   3   應收款 信用卡問題款項發卡 ID+身分證號
   int rr=2;
   String lsAcNo =colSs(rr,"svou.ac_no");
   lmAmt =hPrbl.dcPrbAmount;  //--問交金額--

   int liSeqno =rr+1;
   comcr.hGsvhMemo3 =wfVouchMemo3(rr);
   comcr.hGsvhCurr =isGlCurr;

   int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
   if (liRc != 0) {
      errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
      return;
   }
   //45020000   4   雜項收入
   lmAmt =lmDiffAmt - lmDiffTax;  //--收入--
   if (lmAmt >0) {
      rr=3;
      lsAcNo =colSs(rr,"svou.ac_no");
      liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
   //24912400   5   其他應付款 銷項稅額
   lmAmt =lmDiffTax;    //--收入稅額--
   if (lmAmt >0) {
      rr=4;
      lsAcNo =colSs(rr,"svou.ac_no");
      liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
void R028Total() throws Exception {
   //-iiSysVouch=7-
/*
1   24817000   應付款 其他 YYYYMMDDUBOOOO
2   55030700   雜項支出 其他
6   40010706   代理收付手續費信用卡業務收入
7   24912400   其他應付款 銷項稅額
* **/
   double lmPrbAmt =kkPrbAmt;
   double lmRepay =kkMchtRepay;
   double lmFee =kkOversea ? 0 : kkCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   double lmDiffAmt =lmRepay +lmFee - lmPrbAmt;
   double lmDiffTax =commString.numScale(lmDiffAmt * 0.05,0);

   double lmAmt=0;
   for(int rr=0; rr<7; rr++) {
      comcrMemoInit();

      if (rr==2 || rr==3 || rr==4) continue;
      //-7.row-
      if (rr==6 && lmTax==0) continue;
      //-6.row-
      if (rr==5 && lmFee==0) continue;
      if (rr==1 && lmDiffAmt==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         comcr.hGsvhMemo3 =kkGlmemo3;
         lmAmt =kkMchtRepay;  //--特店退回金額--
      }
      else if (rr ==1) {
         lmAmt =0 - lmDiffAmt;   //--損失--
      }
      else if (rr ==5) {
         lmAmt =lmFee - lmTax;
         comcr.hGsvhMemo1 =hPrbl.cardNo;
      }
      else if (rr ==6) {
         lmAmt =lmTax;
         comcr.hGsvhMemo1 =hPrbl.cardNo;
      }
      if (lmAmt ==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, kkCurrCode);
      if (liRc != 0) {
         errmsg("R028Total[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
void R028Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R028");
   if (iiSysVouch !=7) {
      return;
   }

   double lmPrbAmt =hPrbl.dcPrbAmount;
   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   double lmDiffAmt =lmRepay +lmFee - lmPrbAmt;
   double lmDiffTax =commString.numScale(lmDiffAmt * 0.05,0);

//   //-起始-
//   comcr.start_vouch(isGlCode, isStdCd);
   for(int rr=0; rr<7; rr++) {
      //-7.row-
      if (rr==6 && lmTax==0) continue;
      //-6.row-
      if (rr==5 && lmFee==0) continue;
      //--check 損失/收入--
      //-收入-write row(4,5)--
      if (lmDiffAmt>0) {
         if (rr==1) continue;
         if (rr==4 && lmDiffTax==0) continue;
      }
      //-損失-write row(2)
      if (lmDiffAmt<0 && (rr==3 || rr==4)) continue;
      if (lmDiffAmt==0 && (rr==1 || rr==3 || rr==4)) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         comcr.hGsvhMemo3 =hPrbl.prbGlmemo3;
         lmAmt =lmRepay;  //--特店退回金額--
      }
      else if (rr ==1) {
         lmAmt =0 - lmDiffAmt;   //--損失--
      }
      else if (rr ==2) {
         lmAmt =lmPrbAmt;  //--問交金額--
      }
      else if (rr ==3) {
         lmAmt =lmDiffAmt - lmDiffTax;  //--收入--
      }
      else if (rr ==4) {
         lmAmt =lmDiffTax;    //--收入稅額--
      }
      else if (rr ==5) {
         lmAmt =lmFee - lmTax;
         comcr.hGsvhMemo1 =hPrbl.cardNo;
      }
      else if (rr ==6) {
         lmAmt =lmTax;
         comcr.hGsvhMemo1 =hPrbl.cardNo;
      }
      if (lmAmt ==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
//=****************************************************************************
void R041Detail() throws Exception {
   rc = 0;
   //-vouch=4-
   //C	2	14815601.應收款 信用卡問題款項發卡 ID+身分證號 	卡號			2
   double lmRepay = hPrbl.dcMchtRepay;
   double lmFee = ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax = commString.numScale(lmFee * 0.05, 0);
   //-tw2dc-
   lmFee = convTw2dc(lmFee);
   lmTax = convTw2dc(lmTax);

   int rr =1;
   String lsAcNo =colSs(rr,"svou.ac_no");
   double lmAmt=0;
   lmAmt =lmRepay+lmFee;
   if (lmAmt ==0) return;

   int liSeqno =rr+1;
   comcr.hGsvhMemo3 =wfVouchMemo3(rr);
   comcr.hGsvhCurr =isGlCurr;

   int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
   if (liRc != 0) {
      errmsg("detail_vouch[%s]-%s error", isStdCd, liSeqno);
      return;
   }
}
void R041Total() throws Exception {
   rc = 0;
   //-vouch=4-
/*
D	1	55030700[雜項支出 其他]	C2-不合格帳單損失結	案
D	3	40010706[代理收付手續費信用卡業務收入]	卡號	手續費	90其他加減項
D	4	24912400[其他應付款 銷項稅額]	卡號	銷項稅額
* */

   double lmRepay = kkMchtRepay;
   double lmFee = kkOversea ? 0 : kkCloseFee;
   double lmTax = commString.numScale(lmFee * 0.05, 0);
   //-tw2dc-
   lmFee = convTw2dc(lmFee);
   lmTax = convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      if (rr==1) continue;
      //-3-row-
      if (rr==2 && lmFee==0) continue;
      //-4-row-
      if (rr==3 && lmTax==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =ibMerge ? kkMchtRepay : lmRepay;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee -lmTax; //手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax; //稅額
      }
      if (lmAmt ==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, kkCurrCode);
      if (liRc != 0) {
         errmsg("total_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
void R041Vouch() throws Exception {
   rc = 0;
   selectGenSysVouch("R041");
   if (iiSysVouch != 4) {
      return;
   }

   double lmRepay = hPrbl.dcMchtRepay;
   double lmFee = ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax = commString.numScale(lmFee * 0.05, 0);
   //-tw2dc-
   lmFee = convTw2dc(lmFee);
   lmTax = convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-3-row-
      if (rr==2 && lmFee==0) continue;
      //-4-row-
      if (rr==3 && lmTax==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =lmRepay;
      }
      else if (rr ==1) {
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee -lmTax; //手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax; //稅額
      }
      if (lmAmt ==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
//=****************************************************************************
void R044Detail() throws Exception {
   rc =0;
//C	2	14815601[應收款 信用卡問題款項發卡]	C/B成功
   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   int rr=1;
   String lsAcNo=colSs(rr,"svou.ac_no");
   double lmAmt=0;
   lmAmt =lmRepay+lmFee;
   if (lmAmt==0) return;

   int liSeqno =rr+1;
   comcr.hGsvhMemo3 =wfVouchMemo3(rr);
   comcr.hGsvhCurr =isGlCurr;

   int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
   if (liRc != 0) {
      errmsg("detail_vouch[%s]-%s error", isStdCd, liSeqno);
      return;
   }
}
void R044Total() throws Exception {
   rc =0;
/*
D  1  24817000 應付款 其他 YYYYMMDDUBOOOO
D	3	40010706[代理收付手續費信用卡業務收入]	卡號	手續費	90其他加減項
D	4	24912400[  其他應付款 銷項稅額 ]	卡號	銷項稅額
* */
   double lmRepay =kkMchtRepay;
   double lmFee =kkOversea ? 0 : kkCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-合併1-row-
      if (rr==1) continue;
      if (rr==2 && lmFee==0) continue;
      if (rr==3 && lmTax==0) continue;

      String lsAcNo=colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==0){
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =lmRepay;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error[total]", isStdCd, liSeqno);
         return;
      }
   }
}
void R044Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R044");
   if (iiSysVouch !=4) {
      return;
   }

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-3-row-
      if (rr==2 && lmFee==0) continue;
      //-4-row-
      if (rr==3 && lmTax==0) continue;

      String lsAcNo=colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==0){
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =lmRepay;
      }
      else if (rr ==1) {
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
//=****************************************************************************
void R045Detail() throws Exception {
   rc =0;
/*
C	3	14815601_應收款 信用卡問題款項發卡	不合格帳單
C	4	45020000_雜項收入	不合格帳單
C	5	24912400_其他應付款 銷項稅額	不合格帳單		9308
* */
   double lmPrbAmt =hPrbl.dcPrbAmount;
   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   double lmDiffAmt =lmRepay +lmFee - lmPrbAmt;
   double lmDiffTax =commString.numScale(lmDiffAmt * 0.05,0);

   int llNrow=iiSysVouch;
   for(int rr=0; rr<llNrow; rr++) {
      comcrMemoInit();
      //-I3[啟3,8]-
      if (eq(hPrbl.cloResult,"I3")) {
         if (rr!=2 && rr!=7) continue;
      }
      else {
         if (rr==7) continue;
      }

      //-借方-
      if (rr==0 || rr==1 || rr==5 || rr==6 || rr==7) continue;
      //--check 損失/收入--
      //-收入-write row(4,5)--
      if (lmDiffAmt>0) {
         if (rr==4 && lmDiffTax==0) continue;
      }
      //-損失-write row(2)
      if (lmDiffAmt<0 && (rr==3 || rr==4)) continue;
      if (lmDiffAmt==0 && (rr==3 || rr==4)) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==2) {
         lmAmt =lmPrbAmt;
      }
      else if (rr ==3) {
         lmAmt =lmDiffAmt - lmDiffTax;
      }
      else if (rr ==4) {
         lmAmt =lmDiffTax;
      }
      else if (rr ==7) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmPrbAmt;
      }

      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error[detail]", isStdCd, liSeqno);
         return;
      }
   }
}
void R045Total() throws Exception {
   rc =0;
/*
D	1	24817000_應付款 其他	負項不合格結案
D	2	55030700_雜項支出  其他	C2-不合格帳單
C	3	14815601_應收款 信用卡問題款項發卡	不合格帳單
C	4	45020000_雜項收入	不合格帳單
C	5	24912400_其他應付款 銷項稅額	不合格帳單		9308
D	6	40010706_代理收付手續費 代理費收入 信用卡業務收入	卡號	手續費	90其他加減項
D	7	24912400_其他應付款 銷項稅額	卡號	銷項稅額
* */

   double lmPrbAmt =kkPrbAmt;
   double lmRepay =kkMchtRepay;
   double lmFee =kkOversea ? 0 : kkCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   double lmDiffAmt =lmRepay +lmFee - lmPrbAmt;
   double lmDiffTax =commString.numScale(lmDiffAmt * 0.05,0);

   int llNrow=iiSysVouch;
   for(int rr=0; rr<llNrow; rr++) {
      comcrMemoInit();
      //-20220112: I3[啟3,8]-
      if (eq(hPrbl.cloResult,"I3")) {
         if (rr!=2 && rr!=7) continue;
      }
      else {
         if (rr==7) continue;
      }

      //-貸方-
      if (rr==2 || rr==3 || rr==4) continue;
      if (rr==6 && lmTax==0) continue;
      if (rr==5 && lmFee==0) continue;
      //--check 損失/收入--
      //-收入-write row(4,5)--
      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =lmRepay;
      }
      else if (rr ==1) {
         lmAmt =0 - lmDiffAmt;
      }
      else if (rr ==5) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;
      }
      else if (rr ==6) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      else if (rr ==7) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmPrbAmt;
      }

      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error[total]", isStdCd, liSeqno);
         return;
      }
   }
}
void R045Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R045");
   if (iiSysVouch !=8) {
      return;
   }

   double lmPrbAmt =hPrbl.dcPrbAmount;
   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   double lmDiffAmt =lmRepay +lmFee - lmPrbAmt;
   double lmDiffTax =commString.numScale(lmDiffAmt * 0.05,0);

   int llNrow=iiSysVouch;
   for(int rr=0; rr<llNrow; rr++) {
      comcrMemoInit();
      //-I3結案, 啟帳3,8-
      if (eq(hPrbl.cloResult,"I3")) {
         if (rr!=2 && rr!=7) continue;
      }
      else {
         //不是I3, 不啟8--
         if (rr==7) continue;
      }

      //-7.row-
      if (rr==6 && lmTax==0) continue;
      //-6.row-
      if (rr==5 && lmFee==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =lmRepay;
      }
      else if (rr ==1) {
         lmAmt =0 - lmDiffAmt;
      }
      else if (rr ==2) {
         lmAmt =lmPrbAmt;
      }
      else if (rr ==3) {
         lmAmt =lmDiffAmt - lmDiffTax;
      }
      else if (rr ==4) {
         lmAmt =lmDiffTax;
      }
      else if (rr ==5) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;
      }
      else if (rr ==6) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      else if (rr ==7) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmPrbAmt;
      }
      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
//=****************************************************************************
void RD1Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R-D1");
   if (iiSysVouch !=4) {
      return;
   }

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-合併1-row-
      if (ibMerge && rr==0 && kkSeqno!=0)
         continue;
      //-3-row-
      if (rr==2 && lmFee==0) continue;
      //-4-row-
      if (rr==3 && lmTax==0 ) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt =0;
      if (rr ==0){
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =(ibMerge ? kkMchtRepay : lmRepay);
      }
      else if (rr ==1) {
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;      //-銷項稅額-
      }
      if (lmAmt==0) continue;

      int liSeqno =++kkSeqno;
      comcr.hGsvhMemo3 = wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
//=****************************************************************************
void RD2Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R-D2");
   if (iiSysVouch !=4) {
      return;
   }

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-合併1-row-
      if (ibMerge && rr==0 && kkSeqno!=0)
         continue;
      //-3-row-
      if (rr==2 && lmFee==0) continue;
      //-4-row-
      if (rr==3 && lmTax==0) continue;

      String lsAcNo=colSs(rr,"svou.ac_no");
      double lmAmt =0;
      if (rr ==0){
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =ibMerge ? kkMchtRepay : lmRepay;
      }
      else if (rr ==1) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;      //-銷項稅額-
      }
      if (lmAmt ==0) continue;

      int liSeqno =++kkSeqno;
      comcr.hGsvhMemo3 = wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
//=****************************************************************************
void RD3Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R-D3");
   if (iiSysVouch !=4) {
      return;
   }

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-合併1-row-
      if (ibMerge && rr==0 && kkSeqno!=0)
         continue;
      //-3-row-
      if (rr==2 && lmFee==0) continue;
      //-4-row-
      if (rr==3 && lmTax==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt =0;
      if (rr ==0){
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =(ibMerge ? kkMchtRepay : lmRepay);

      }
      else if (rr ==1) {
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;      //-銷項稅額-
      }
      if (lmAmt ==0) continue;

      int liSeqno =++kkSeqno;
      comcr.hGsvhMemo3 = wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
//=****************************************************************************
void RD8Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R-D8");
   if (iiSysVouch !=4) {
      return;
   }

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      String lsAcNo =colSs(rr, "svou.ac_no");
      double lmAmt =0;

      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =ibMerge ? kkMchtRepay : lmRepay;
      }
      else if (rr ==1) {
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;   //手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;   //-銷項稅額-
      }
      if (lmAmt ==0) continue;

      int liSeqno = ++kkSeqno;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}

void RD9Vouch() throws Exception {
   rc =0;
   selectGenSysVouch("R-D9");
   if (iiSysVouch !=4) {
      return;
   }

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-合併1-row-
      if (ibMerge && rr==0 && kkSeqno!=0)
         continue;
      if (rr==2 && lmFee==0) continue;
      if (rr==3 && lmTax==0) continue;

      String lsAcNo =colSs(rr, "svou.ac_no");
      double lmAmt =0;
      if (rr ==0) {
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =(ibMerge ? kkMchtRepay : lmRepay);
      }
      else if (rr ==1) {
         lmAmt =lmRepay+lmFee;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;   //手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;   //-銷項稅額-
      }
      if (lmAmt ==0) continue;

      int liSeqno =++kkSeqno;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}
//=****************************************************************************
void ComglDetail() throws Exception {
   rc =0;

   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   int rr=1;
   String lsAcNo=colSs(rr,"svou.ac_no");
   double lmAmt=0;
   lmAmt =lmRepay+lmFee;
   if (lmAmt==0) return;

   int liSeqno =rr+1;
   comcr.hGsvhMemo3 =wfVouchMemo3(rr);
   comcr.hGsvhCurr =isGlCurr;

   int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
   if (liRc != 0) {
      errmsg("ComglDetail[%s]-%s error", isStdCd, liSeqno);
      return;
   }
}
void ComglTotal() throws Exception {
   rc =0;
   double lmRepay =kkMchtRepay;
   double lmFee =kkOversea ? 0 : kkCloseFee;
   double lmTax =commString.numScale(lmFee * 0.05,0);
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   for(int rr=0; rr<4; rr++) {
      comcrMemoInit();

      //-合併1-row-
      if (rr==1) continue;
      if (rr==2 && lmFee==0) continue;
      if (rr==3 && lmTax==0) continue;

      String lsAcNo=colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr ==0){
         comcr.hGsvhMemo2 =wfVouchMemo2(lsAcNo);
         lmAmt =lmRepay;
      }
      else if (rr ==2) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmFee - lmTax;	//手續費
      }
      else if (rr ==3) {
         comcr.hGsvhMemo1 =hPrbl.cardNo;
         lmAmt =lmTax;
      }
      if (lmAmt==0) continue;

      int liSeqno =rr+1;
      comcr.hGsvhMemo3 =wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;

      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error[total]", isStdCd, liSeqno);
         return;
      }
   }
}

void ComglVouch(int rr_vouch) throws Exception {
   //-問交結案-
   rc =0;
   selectGenSysVouch(isStdCd);
   int ll_row =iiSysVouch;
   if (ll_row<=0 || ll_row!=rr_vouch) {
      errmsg("系統自動起帳會計分錄錯誤, [%s][%s]",isStdCd,rr_vouch);
      return;
   }

   double lmPrbAmt =hPrbl.dcPrbAmount;
   double lmRepay =hPrbl.dcMchtRepay;
   double lmFee =ibOversea ? 0 : hPrbl.mchtCloseFee;
   double lmTax =lmFee * 0.05;
   //-tw2dc-
   lmFee =convTw2dc(lmFee);
   lmTax =convTw2dc(lmTax);

   for (int rr=0; rr<rr_vouch; rr++) {
      comcrMemoInit();

      if (rr==2 && lmFee==0) continue;
      if (rr==3 && lmTax==0) continue;

      String lsAcNo =colSs(rr,"svou.ac_no");
      double lmAmt=0;
      if (rr==0) {
         lmAmt = lmRepay;
      }
      else if (rr==1) {
         lmAmt =lmRepay + lmFee;
      }
      if (rr==2)
         lmAmt =(lmFee -lmTax);
      else if (rr==3)
         lmAmt =lmTax;
      if (lmAmt ==0) continue;

      int liSeqno = rr+1;
      comcr.hGsvhMemo3 = wfVouchMemo3(rr);
      comcr.hGsvhCurr =isGlCurr;
      int liRc = comcrDetailVouch(lsAcNo, liSeqno, lmAmt, hPrbl.currCode);
      if (liRc != 0) {
         errmsg("insert Gen_vouch[%s]-%s error", isStdCd, liSeqno);
         return;
      }
   }
}

private String selectGenMemo3(String a_ref_no, String a_ac_no) throws Exception {
   if (tiMemo3 <=0) {
      sqlCmd = "SELECT memo3 FROM gen_memo3"
            + " WHERE ref_no =? and ac_no =?"
            + commSqlStr.rownum(1);
      daoTable = "gen_memo3-S";
      tiMemo3 = ppStmtCrt("ti-memo3", "");
   }
   ppp(1,a_ref_no);
   ppp(a_ac_no);
   sqlSelect(tiMemo3);
   if (sqlNrow <0) {
      sqlerr("select gen_memo3 error, kk[%s]",hPrbl.referenceNo);
      errExit(1);
   }
   if (sqlNrow ==0)
      return "";
   return colSs("memo3");
}

void updateRskAcctLog(String a_rowid) throws Exception {
   if (tiAcctlogU <=0) {
      sqlCmd ="update rsk_acct_log set"
            +" vouch_proc_flag ='Y'"
            +", vouch_proc_date ="+commSqlStr.sysYYmd
            +", vouch_merge_flag =?"
            +", gl_memo3 =?"
            +","+modxxxSet()
            +" where rowid =?"
      ;
      tiAcctlogU = ppStmtCrt("tiAcctlogU","");
   }

   if (ibMerge) {
      ppp(1,"Y");
      ppp(2,kkGlmemo3);
   }
   else {
      ppp(1,"");
      ppp(2,"");
   }
   this.setRowId(3,a_rowid);

   sqlExec(tiAcctlogU);
   if (sqlNrow<=0) {
      errmsg("update rsk_acct_log error; kk="+colSs("A.ctrl_seqno"));
   }
}

double convTw2dc(double amt1) throws Exception {
   if (amt1==0)
      return 0;
   //	conv_tw2dc(hPrbl.curr_code
   //			,hPrbl.dest_amt,hPrbl.dc_dest_amt,lmFee);
   String ls_curr =hPrbl.currCode;
   if (empty(ls_curr) || eqIgno(ls_curr,"901")) {
      return amt1;
   }
   if (hPrbl.destAmt==0 || hPrbl.dcDestAmt==0)
      return amt1;

   double lmAmt =(amt1 * hPrbl.dcDestAmt) / hPrbl.destAmt;

   if (tiCurrcode2 <=0) {
      sqlCmd ="select nvl(curr_amt_dp,0) as amt_dp"
            +" from ptr_currcode"
            +" where curr_code =?";
      tiCurrcode2 =ppStmtCrt("ti-currcode-2","");
   }
   sqlSelect(tiCurrcode2,new Object[]{ls_curr});
   if (sqlNrow<=0)
      return commString.numScale(lmAmt,0);
   return commString.numScale(lmAmt, colInt("amt_dp"));
}

}
