package Cms;
/**
 * 2023-0717   JH    ++hhAmtSumFlag,hhAcctMonth
 * 2023-0703   JH    mod: checkParm_detl
 * 2023-1226   JH    IT第一期才列入計算
 * */
import com.DataSet;
import java.sql.Connection;

public class CmsRdsParm extends com.BaseBatch {
//-----------
//public CmsRdsParm.HH hh=new CmsRdsParm.HH();
//public class HH {
public String hhRowId = "";
public String hhCardNo ="";
//   String rm_carno="";
public String hhGroupCode ="";
public String hhCardType ="";
//   String rm_carmanname="";
//   String rm_carmanid="";
public String hhRmStatus ="";
public String hhIdPseqno ="";
public String hhRdsPcard ="";
public String hhOutstandingYn ="";
public String hhCondCard ="";  //卡片條件符合--
//---
public String hhProjNo ="";
public double hhPurchAmt =0; //累積消費金額
public int hhPurchCnt =0;  //累積消費次數
public double hhPurchAmtLyy =0;  //上年累計消費金額
public double hhMaxPurchAmt =0; //最大消費金額--
//--
public String hhPseqno ="";
public String hhAcctType ="";
public String hhAmtSumFlag="";
public String hhAcctMonth="";
public int    hhYearType=0;

   public void hhInit() {
      hhRowId = "";
      hhCardNo ="";
//      rm_carno="";
      hhGroupCode ="";
      hhCardType ="";
//      rm_carmanname="";
//      rm_carmanid="";
      hhRmStatus ="";
      hhIdPseqno ="";
      hhRdsPcard ="";
      hhOutstandingYn ="";
      //---
      hhProjNo ="";
      hhPurchAmt =0;
      hhPurchCnt =0;
      hhPurchAmtLyy =0;
      hhMaxPurchAmt=0;
      //--
      hhPseqno ="";
      hhAcctType ="";
      hhAmtSumFlag="";
      hhAcctMonth ="";
      hhYearType =0;
   }
//}

//----------------
com.DataSet idsParm=new DataSet();
int iicheckRC=0;
int iiParmRR=-1;
String is_lastYyyy="";
//====================================
public void setLastYear(String asDate) {
   if (empty(asDate)) is_lastYyyy =commDate.dateAdd(hBusiDate, 0, -12, 0).substring(0, 4);
   else is_lastYyyy =commDate.dateAdd(asDate, 0, -12, 0).substring(0, 4);
}
//====================================
com.DataSet dsDetl=new DataSet();
public void selectCmsRoadParm2() throws Exception {
   sqlCmd ="select A.*"+
           ", (SELECT count(*) FROM cms_roadparm2_dtl WHERE proj_no=A.proj_no) AS detl_cnt"+
           "  from cms_roadparm2 A"+
           " where A.apr_date <>''"+
           " and (A.valid_end_date ='' or A.valid_end_date >?)"+
           " order by A.crt_date"
   ;

   sqlQuery(idsParm, sqlCmd, new Object[]{sysDate});
   if (sqlNrow <0) {
      printf(" sqlQuery error: cms_roadparm2");
      okExit(0);
   }
   if (idsParm.rowCount() <=0) {
      printf("無參數可查核: rdsm0050[免費使用道路救援參數維護]");
      okExit(0);
   }
   printf("免費使用道路救援參數: Cnt=[%s]", sqlNrow);
   //---------
   sqlCmd ="select proj_no,acct_type,card_type"+
           ", group_code, corp_no"+
           " from cms_roadparm2_dtl"+
           " where 1=1"+
           " order by proj_no, acct_type desc, card_type desc, group_code desc, corp_no desc"
           ;
   sqlQuery(dsDetl, "", null);
   if (dsDetl.rowCount() <=0) {
      printf(" 道路救援: 無參數明細(cms_roadparm2_dtl)");
   }
   dsDetl.loadKeyData_dupl("proj_no");
}

public int checkRoadParm(String aCardNo) throws Exception {
   //0.isOK, <>0.不符合
   iicheckRC=0;
   hhCondCard ="N";

   hhCardNo =aCardNo;
   selectCrdCard();
   if (iicheckRC !=0) return 1;
   int li_yearType=getYearType(hhPseqno);
   if (li_yearType !=1 && li_yearType !=2 && li_yearType !=3) {
      printf(" 卡號=%s, year-type[%s] not 1/2/3", hhCardNo, li_yearType);
      return 1;
   }
   hhYearType =li_yearType;

   int llNrow=idsParm.rowCount();
   for (int ll = 0; ll <llNrow ; ll++) {
      int liRc=0;
      iiParmRR =ll;
      idsParm.listCurr(ll);
      String ss="";
      hhPurchCnt =0;
      hhPurchAmt =0;
      hhProjNo ="";
      hhPurchAmtLyy =0;
      hhAmtSumFlag =idsParm.colSs("amt_sum_flag");

      //適用免費道路救援卡片類別: --
      if (idsParm.colInt("detl_cnt") >0) {
         liRc=checkParm_detl();
         if (liRc !=0) continue;
         hhCondCard ="Y";
      }
      //未繳最低應繳金額不適用
      ss=idsParm.colSs("outstanding_cond");
      if (eq(ss,"Y") && eq(hhOutstandingYn,"Y")) {
         continue;
      }
      //A. 首年核卡(全新卡友: 帳戶下之流通卡最早發卡日為本年度, 且前12個月無停卡記錄)--
      if (li_yearType ==1) {
         if (idsParm.colEq("fst_cond","Y")==false) continue;
      }
      else if (li_yearType ==2) {
         if (idsParm.colEq("lst_cond","Y")==false) continue;
         liRc =selectMktPostConsume();
         if (liRc !=0) continue;
      }
      else if (li_yearType ==3) {
         if (idsParm.colEq("cur_cond","Y")==false) continue;
      }
      liRc =selectBilBill(li_yearType);
      if (liRc !=0) continue;

      break;
   }
   if (empty(hhProjNo)) {
      iicheckRC=1;
      return 1;
   }

   return 0;
}
//---------
int tiYear=-1;
int getYearType(String aPseqno) throws Exception {
   if (empty(aPseqno)) return 0;
   int liRc=9;

   if (tiYear <=0) {
      sqlCmd ="select min(ori_issue_date) as issue_date, count(*) as xx_cnt0"+
              " from crd_card where p_seqno =? and current_code like ? "+
              " and oppost_date >=? and oppost_date <=?"
      ;
      tiYear =ppStmtCrt("ti_year-S","");
   }
   //A. 首年核卡(全新卡友: 帳戶下之流通卡最早發卡日為本年度, 且前12個月無停卡記錄)
   //B. 非首年核卡(帳戶之流通卡最早發卡日不為今年度)--
   //C. 首年核卡(非全新卡友: 帳戶下之流通卡最早發卡日在今年度)
   ppp(1,aPseqno);
   ppp("0");
   ppp("");
   ppp("");
   sqlSelect(tiYear);
   if (sqlNrow <=0) return 9;
   if (colNum("xx_cnt0")==0) return 5;  //無有效

   String ls_minIssueDate =colSs("issue_date");
   if (eq(commString.left(ls_minIssueDate,4),commString.left(sysDate,4))) {
      liRc =1;  //A.
   }
   else {
      liRc =2;  //B.
   }
   if (liRc ==1) {
      String ls_date1=commDate.dateAdd(ls_minIssueDate,0,-12,0);
      ppp(1,aPseqno);
      ppp("%");
      ppp(ls_date1);
      ppp(ls_minIssueDate);
      sqlSelect(tiYear);
      if (sqlNrow >0 && colInt("xx_cnt0")>0) {
         liRc=3;  //C.
      }
   }

   return liRc;
}

//--------
int tiCard=-1;
void selectCrdCard() throws Exception {
   if (tiCard <=0) {
      sqlCmd="select A.major_card_no, A.major_id_p_seqno, A.id_p_seqno"
              +", A.current_code, A.card_type, A.group_code"
              +", A.acct_type, A.p_seqno"
              +", decode(A.ori_issue_date,'',A.issue_date,A.ori_issue_date) as issue_date"
              +", B.int_rate_mcode "
              +", C.rds_pcard"
              +", (SELECT corp_no FROM crd_corp WHERE corp_p_seqno=A.corp_p_seqno LIMIT 1) as corp_no"
              +" from crd_card A join act_acno B on B.acno_p_seqno=A.acno_p_seqno"
              +" left join ptr_card_type C on C.card_type=A.card_type"
              +" where A.card_no =?"
      ;
      tiCard =ppStmtCrt("ti-card-S","");
   }

   ppp(1, hhCardNo);
   String cc=daoTid ="card.";
   sqlSelect(tiCard);
   if (sqlNrow <=0) {
      //printf("select crd_card N-find, kk[%s]", hhCardNo);
      iicheckRC=1;
      return;
   }

   hhPseqno =colSs(cc+"p_seqno");
   hhAcctType =colSs(cc+"acct_type");
   hhGroupCode =colSs(cc+"group_code");
   hhCardType =colSs(cc+"card_type");
   hhRdsPcard =colSs(cc+"rds_pcard");
   hhIdPseqno =colSs(cc+"id_p_seqno");

   hhOutstandingYn ="";
   if (colInt("card.int_rate_mcode") >0) {
      hhOutstandingYn ="Y";
   }
//   if (empty(hh_rdsPcard)) {
//      hh_rdsPcard =colSs("card.rds_pcard");
//   }

}

//-----
//int tiParmDtl=-1;
int checkParm_detl() throws Exception {
   String ls_projNo =idsParm.colSs("proj_no");
   String ls_acctType =colSs("card.acct_type");
   String ls_cardType =colSs("card.card_type");
   String ls_groupCode =colSs("card.group_code");
   String ls_corpNo =colSs("card.corp_no");
   //--
   int ll_ok=0, li_proj=0;
   if (dsDetl.getKeyData(ls_projNo) <=0) return 0;
   int llcurr=dsDetl.getCurrRow();
   for (int ll =llcurr; ll <dsDetl.rowCount() ; ll++) {
      dsDetl.listCurr(ll);
      String ls_proj=dsDetl.colSs("proj_no");
      if (!eq(ls_proj,ls_projNo)) break;
      if (!dsDetl.colEmpty("acct_type") && !dsDetl.colEq("acct_type",ls_acctType)) continue;
      if (!dsDetl.colEmpty("card_type") && !dsDetl.colEq("card_type",ls_cardType)) continue;
      if (!dsDetl.colEmpty("group_code") && !dsDetl.colEq("group_code",ls_groupCode)) continue;
      if (!dsDetl.colEmpty("corp_no") && !dsDetl.colEq("corp_no",ls_corpNo)) continue;

      ll_ok++;
      break;
   }
   if (ll_ok >0) return 0;

//   if (tiParmDtl <=0) {
//      sqlCmd ="select count(*) as xx_cnt"+
//              " from cms_roadparm2_dtl"+
//              " where proj_no =?"+
//              " and acct_type in ('',?)"+
//              " and card_type in ('',?)"
//              +" and group_code in ('',?)"
//              +" and corp_no in ('',?)"
//      ;
//      tiParmDtl =ppStmtCrt("ti-parmdtl-S","");
//   }
//   String lsProjNo =idsParm.colSs("proj_no");
//   ppp(1, lsProjNo);
//   ppp(colSs("card.acct_type"));
//   ppp(colSs("card.card_type"));
//   ppp(colSs("card.group_code"));
//   ppp(colSs("card.corp_no"));
//   sqlSelect(tiParmDtl);
//   if (sqlNrow <=0) return 1;
//   if (colInt("xx_cnt") >0) return 0;
   return 1;
}
//--
int tiPcons=-1;
int selectMktPostConsume() throws Exception {
   if (tiPcons <=0) {
      sqlCmd = " SELECT "
              + " sum(A.consume_bl_amt) as bl_amt"
              + ", sum(A.consume_it_amt) as it_amt "
              + ", sum(A.consume_id_amt) as id_amt "
              + ", sum(A.consume_ca_amt) as ca_amt "
              + ", sum(A.consume_ao_amt) as ao_amt "
              + ", sum(A.consume_ot_amt) as ot_amt "
              + " FROM    mkt_post_consume A"
              + " where  A.p_seqno =? and A.major_card_no like ? and A.card_no like ?"
              + " AND    A.acct_month between ? and ?" // :is_last_yyyy||'01' and :is_last_yyyy||'12'
      ;
      tiPcons =ppStmtCrt("ti-pcons-S","");
   }
   String lsCardNo="%", lsMcardNo="%";
   //String ls_sumFlag=idsParm.colSs("amt_sum_flag");
   if (eq(hhAmtSumFlag,"2")) {
      lsMcardNo =colSs("card.major_card_no");
   }
   else if (eq(hhAmtSumFlag,"3")) {
      lsCardNo = hhCardNo;
   }
   if (empty(is_lastYyyy)) {
      setLastYear("");
   }
   //-統計期間-end-
   hhAcctMonth =is_lastYyyy+"12";

   ppp(1, hhPseqno);
   ppp(lsMcardNo);
   ppp(lsCardNo);
   ppp(is_lastYyyy+"01");
   ppp(is_lastYyyy+"12");

   sqlSelect(tiPcons);
   if (sqlNrow <=0) return 1;

   double lmAmt=0;
   if (idsParm.colEq("lst_acct_code_bl","Y")) lmAmt +=colNum("bl_amt");
   if (idsParm.colEq("lst_acct_code_it","Y")) lmAmt +=colNum("it_amt");
   if (idsParm.colEq("lst_acct_code_ca","Y")) lmAmt +=colNum("ca_amt");
   if (idsParm.colEq("lst_acct_code_id","Y")) lmAmt +=colNum("id_amt");
   if (idsParm.colEq("lst_acct_code_ao","Y")) lmAmt +=colNum("ao_amt");
   if (idsParm.colEq("lst_acct_code_ot","Y")) lmAmt +=colNum("ot_amt");

   if (lmAmt <idsParm.colNum("lst_tol_amt")) return 1;

   hhPurchAmtLyy =lmAmt;

   return 0;
}
//-----
int tiBill=-1;
int selectBilBill(int aiYearType) throws Exception {
   if (tiBill <=0) {
      sqlCmd = " select "
              //+ " decode(A.acct_code,'IT',decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt),0),"
              + " a.acct_code , "
              + " case when A.sign_flag ='-' then a.dest_amt*-1 else a.dest_amt end as dest_amt , "
              +" decode(A.acct_code||a.install_curr_term,'IT1'" +
              ",(SELECT B.tot_amt FROM bil_contract B"+
              "  WHERE B.contract_no =A.contract_no AND B.refund_flag<>'Y'),0) AS it_tot_amt,"
              + " substr(a.mcht_no,1,15) as mcht_no , "
              + " a.post_date "
              + " from bil_bill A"
//              + " left join bil_contract B on "
//              + " a.contract_no = b.contract_no and a.contract_seq_no = b.contract_seq_no "
              + " where A.p_seqno = ?"
              +" and A.major_card_no like ?"
              +" and A.card_no like ?"
              //-消費資料 消費期間--
              +" and A.post_date >= ? and A.post_date < ?"
              +" and A.acct_code in (?,?,?,?,?,?)"
      ;
      tiBill =ppStmtCrt("ti-bill-S","");
   }
   String lsCardNo="%", lsMcardNo="%";
   //String ls_sumFlag=idsParm.colSs("amt_sum_flag");
   if (eq(hhAmtSumFlag,"2")) {
      lsMcardNo =colSs("card.major_card_no");
   }
   else if (eq(hhAmtSumFlag,"3")) {
      lsCardNo = hhCardNo;
   }
   String[] laAcctCode=new String[]{"x","x","x","x","x","x"};
   String lsDate1="";
   int liMM=0;
   double cond_lowAmt=0;
   double cond_totAmt=0;
   int cond_totRow=0;
   if (aiYearType ==1) {
      laAcctCode[0] =(idsParm.colEq("fst_acct_code_BL","Y")) ? "BL" : "x";
      laAcctCode[1] =(idsParm.colEq("fst_acct_code_CA","Y")) ? "CA" : "x";
      laAcctCode[2] =(idsParm.colEq("fst_acct_code_IT","Y")) ? "IT" : "x";
      laAcctCode[3] =(idsParm.colEq("fst_acct_code_ID","Y")) ? "ID" : "x";
      laAcctCode[4] =(idsParm.colEq("fst_acct_code_AO","Y")) ? "AO" : "x";
      laAcctCode[5] =(idsParm.colEq("fst_acct_code_OT","Y")) ? "OT" : "x";
      liMM =idsParm.colInt("fst_mm");
      lsDate1 =commDate.dateAdd(hBusiDate,0,0 - liMM,0);
      cond_lowAmt =idsParm.colNum("fst_one_low_amt");
      if (idsParm.colEq("fst_amt_cond","Y")) cond_totAmt=idsParm.colNum("fst_purch_amt");
      if (idsParm.colEq("fst_row_cond","Y")) cond_totRow=idsParm.colInt("fst_purch_row");
   }
   else if (aiYearType ==2) {
      laAcctCode[0] =(idsParm.colEq("lst_acct_code_BL","Y")) ? "BL" : "x";
      laAcctCode[1] =(idsParm.colEq("lst_acct_code_CA","Y")) ? "CA" : "x";
      laAcctCode[2] =(idsParm.colEq("lst_acct_code_IT","Y")) ? "IT" : "x";
      laAcctCode[3] =(idsParm.colEq("lst_acct_code_ID","Y")) ? "ID" : "x";
      laAcctCode[4] =(idsParm.colEq("lst_acct_code_AO","Y")) ? "AO" : "x";
      laAcctCode[5] =(idsParm.colEq("lst_acct_code_OT","Y")) ? "OT" : "x";
      liMM =idsParm.colInt("lst_mm");
      lsDate1 =commDate.dateAdd(hBusiDate,0,0 - liMM,0);
      cond_lowAmt =idsParm.colNum("lst_one_low_amt");
      if (idsParm.colEq("lst_amt_cond","Y")) cond_totAmt=idsParm.colNum("lst_purch_amt");
      if (idsParm.colEq("lst_row_cond","Y")) cond_totRow=idsParm.colInt("lst_purch_row");
   }
   else if (aiYearType ==3) {
      laAcctCode[0] =(idsParm.colEq("cur_acct_code_BL","Y")) ? "BL" : "x";
      laAcctCode[1] =(idsParm.colEq("cur_acct_code_CA","Y")) ? "CA" : "x";
      laAcctCode[2] =(idsParm.colEq("cur_acct_code_IT","Y")) ? "IT" : "x";
      laAcctCode[3] =(idsParm.colEq("cur_acct_code_ID","Y")) ? "ID" : "x";
      laAcctCode[4] =(idsParm.colEq("cur_acct_code_AO","Y")) ? "AO" : "x";
      laAcctCode[5] =(idsParm.colEq("cur_acct_code_OT","Y")) ? "OT" : "x";
      liMM =idsParm.colInt("cur_mm");
      lsDate1 =commDate.dateAdd(hBusiDate,0,0 - liMM,0);
      if (!eq(lsDate1.substring(0,4),hBusiDate.substring(0,4))) {
         lsDate1 =commString.left(hBusiDate,4)+"0101";  //當年度--
      }
      cond_lowAmt =idsParm.colNum("cur_one_low_amt");
      if (idsParm.colEq("cur_amt_cond","Y")) cond_totAmt=idsParm.colNum("cur_purch_amt");
      if (idsParm.colEq("cur_row_cond","Y")) cond_totRow=idsParm.colInt("cur_purch_row");
   }
   //-統計期間-end-
   hhAcctMonth =commString.left(hBusiDate,6);

   ppp(1, hhPseqno);
   ppp(lsMcardNo);
   ppp(lsCardNo);
   ppp(lsDate1);
   ppp(hBusiDate);
   ppp(laAcctCode[0]);
   ppp(laAcctCode[1]);
   ppp(laAcctCode[2]);
   ppp(laAcctCode[3]);
   ppp(laAcctCode[4]);
   ppp(laAcctCode[5]);

   daoTid ="bill.";
   sqlSelect(tiBill);
   int llNrow =sqlNrow;
   if (llNrow <=0) return 1;
   double lm_destAmt=0;
   int ll_billCnt=0;
   for (int ll = 0; ll <llNrow ; ll++) {
      double lmAmt =colNum(ll,"bill.dest_amt");
      if (colEq(ll,"bill.acct_code","IT")) lmAmt=colNum(ll,"bill.it_tot_amt");
      //-<=0不列入次數-
      if (lmAmt <=0) {
         lm_destAmt +=lmAmt;
         continue;
      }
      //-最大消費金額---
      if (lmAmt >hhMaxPurchAmt) hhMaxPurchAmt=lmAmt;
      if (lmAmt <cond_lowAmt) continue;
      String lsMchtNo=colSs(ll,"bill.mcht_no");
      if (selectMchtNo(aiYearType,lsMchtNo) !=0) continue;
      if (selectMchtGp(aiYearType,lsMchtNo) !=0) continue;
      lm_destAmt +=lmAmt;
      ll_billCnt++;
   }
   hhPurchAmt =lm_destAmt;
   hhPurchCnt =ll_billCnt;
   if (cond_totAmt >0 && lm_destAmt <cond_totAmt) return 1;
   if (cond_totRow >0 && ll_billCnt <cond_totRow) return 1;

   hhProjNo =idsParm.colSs("proj_no");
//   hhPurchAmt =lm_destAmt;
//   hhPurchCnt =ll_billCnt;
   return 0;
}
//---
int tiMchtGp=-1;
int selectMchtGp(int aiYearType, String aMchtNo) throws Exception {
   String lsCond=idsParm.colSs("fst_mcht_gp");
   if (eq(lsCond,"0")) return 0;

   String lsType="0"+aiYearType;
   if (aiYearType ==2) {
      lsCond=idsParm.colSs("lst_mcht_gp");
   }
   else if (aiYearType ==3) {
      lsCond=idsParm.colSs("cur_mcht_gp");
   }

   if (tiMchtGp <=0) {
      sqlCmd ="select count(*) as mchtgp_cnt"+
              " FROM mkt_mchtgp_data"+
              " where table_name ='MKT_MCHT_GP'"+
              " and data_code =?"+
              " and data_key in ( select data_code from cms_roadparm2_bn_data"+
              " where proj_no =? and data_type =? and type_desc='特店群組' )"+
              "";
      tiMchtGp =ppStmtCrt("ti-mchtGp-S","");
   }
   ppp(1,aMchtNo);
   ppp(idsParm.colSs("proj_no"));
   ppp(lsType);

   sqlSelect(tiMchtGp);
   int ll_mchtCnt=0;
   if (sqlNrow >0) ll_mchtCnt=colInt("mchtgp_cnt");
   if (eq(lsCond,"1") && ll_mchtCnt ==0) return 1;  //指定
   if (eq(lsCond,"2") && ll_mchtCnt >0) return 1;  //排除
   return 0;
}
//----
int tiMchtNo=-1;
int selectMchtNo(int aiYearType, String aMchtNo) throws Exception {
   String lsCond=idsParm.colSs("fst_mcht");
   String lsType="01";
   if (aiYearType ==2) {
      lsCond=idsParm.colSs("lst_mcht");
      lsType ="02";
   }
   else if (aiYearType ==3) {
      lsCond=idsParm.colSs("cur_mcht");
      lsType ="03";
   }
   if (eq(lsCond,"0")) return 0;

   if (tiMchtNo <=0) {
      sqlCmd ="select count(*) as mcht_cnt"+
              " from cms_roadparm2_bn_data"+
              " where proj_no =?"+
              " and data_type =? and type_desc='特店代號'"+
              " and data_code =?";
      tiMchtNo =ppStmtCrt("ti-mchtno-S","");
   }
   ppp(1, idsParm.colSs("proj_no"));
   ppp(lsType);
   ppp(aMchtNo);
   sqlSelect(tiMchtNo);
   int ll_mchtCnt=0;
   if (sqlNrow >0) ll_mchtCnt=colInt("mcht_cnt");
   if (eq(lsCond,"1") && ll_mchtCnt ==0) return 1;  //指定
   if (eq(lsCond,"2") && ll_mchtCnt >0) return 1;  //排除
   return 0;
}
//---------
int tiSeqno=-1;
public int getrdSeqNo(String aDate) throws Exception {
   if (tiSeqno <=0) {
      sqlCmd ="select max(rd_seqno) as xx_seqno"+
              " from cms_roaddetail"+
              " where rd_moddate =?"
      ;
      tiSeqno =ppStmtCrt("ti-seqno-S","");
   }
   String lsDate =nvl(aDate,sysDate);
   ppp(1, lsDate);
   sqlSelect(tiSeqno);
   if (sqlNrow <=0) return 1;

   return colInt("xx_seqno")+1;
}
//=========================
@Override
protected void dataProcess(String[] args) throws Exception {

}

//==================
public CmsRdsParm(Connection conn[], String[] dbAlias, String busiDate) throws Exception {
   dateTime();

   super.conn = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);
   setBusiDate(busiDate);
   if (empty(busiDate)) {
      selectPtrBusinday();
   }

   return;
}
public CmsRdsParm(Connection conn[], String[] dbAlias) throws Exception {
   dateTime();

   super.conn = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);

   selectPtrBusinday();

   return;
}
}
