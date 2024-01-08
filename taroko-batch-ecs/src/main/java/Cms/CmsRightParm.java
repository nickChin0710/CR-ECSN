package Cms;
/**
 * 2023-0704   JH    dsDetl.loadKeyData()
 * 2023-0608   JH    上一年度00月(含)至本年度12月底刷卡消費==本年度消費
 * */
import com.DataSet;
import java.sql.Connection;

@SuppressWarnings({"unchecked", "deprecation"})
public class CmsRightParm extends com.BaseBatch {
com.DataSet dsParm=new DataSet();
com.DataSet dsDetl=new DataSet();
public String isProcYymm="";
public String isAcctYymm="";
public String[] isCurrYm=new String[2];
public String[] isLastYm=new String[2];
//-HH----------
public String hhCardNo ="";
public String hhMajCardNo ="";
public String hhIdPseqno ="";
public String hhMajIdPseqno ="";
public String hhGroupCode ="";
public String hhCardType ="";
public String hhIssueDate ="";
public String hhItemNo ="";
public String hhProjCode ="";
public int hhFreeCnt =0;
public double hhFreePerAmt =0;
public double hhCurrProjAmt =0;
public double hhLastProjAmt =0;
public double hhCurrYearConsume =0;
public double hhLastYearConsume =0;
public int hhCurrYearCnt =0;
public int hhLastYearCnt =0;
public String hhDebutType ="";
public String hhPurchDate ="";
public double hhCurrMaxAmt =0;
public double hhCurrTotAmt =0;
public int hhCurrTotCnt =0;
public String hhConsumeType ="";
public String hhFreeType ="";
public String hhAcctType ="";
public String hhItType ="";
//--
public void initHh() {
   hhCardNo ="";
   hhMajCardNo ="";
   hhIdPseqno ="";
   hhMajIdPseqno ="";
   hhGroupCode ="";
   hhCardType ="";
   hhIssueDate ="";
   hhItemNo ="";
   hhProjCode ="";
   hhFreeCnt =0;
   hhFreePerAmt =0;
   hhCurrProjAmt =0;
   hhLastProjAmt =0;
   hhCurrYearConsume =0;
   hhLastYearConsume =0;
   hhCurrYearCnt =0;
   hhLastYearCnt =0;
   hhDebutType ="";
   hhPurchDate ="";
   hhCurrMaxAmt =0;
   hhCurrTotAmt =0;
   hhCurrTotCnt =0;
   hhConsumeType ="";
   hhFreeType ="";
   hhAcctType ="";
   hhItType ="";
}
//====================================================

public void setYymm() {
   isProcYymm =hBusiDate.substring(0,6);
   isAcctYymm =commDate.dateAdd(hBusiDate,0,-1,0).substring(0,6);
//   printf("-- 資料處理年月[%s], busiDate[%s], acctMonth[%s]", isProcYymm, hBusiDate, isAcctYymm);

   isCurrYm[0] =isAcctYymm.substring(0,4)+"01";
   isCurrYm[1] =isAcctYymm.substring(0,4)+"12";
   isLastYm[0] =commDate.dateAdd(isCurrYm[0],-1,0,0).substring(0,4)+"01";
   isLastYm[1] =isLastYm[0].substring(0,4)+"12";
}
//-----------
public void setDsParm(com.DataSet a_ds) {
   dsParm =a_ds;
}
public void setDsDetl(DataSet a_ds) {
   dsDetl =a_ds;
}
public void parmListCurr(int ll) {
   dsParm.listCurr(ll);
}
//---------
int tiCard=-1;
public void selectCrdCard(String a_cardNo) throws Exception {
   if (tiCard <=0) {
      sqlCmd ="select A.card_no, A.major_card_no, A.id_p_seqno, A.major_id_p_seqno"+
              ", A.group_code, A.card_type, A.acct_type"+
              ", A.issue_date"+
              " FROM crd_card A"+
              " WHERE A.current_code='0'"+
              " and A.card_no =?"
      ;
      tiCard =ppStmtCrt("ti-card","");
   }
   ppp(1, a_cardNo);
   sqlSelect(tiCard);
   if (sqlNrow <=0) return;
   hhCardNo =colSs("card_no");
   hhMajCardNo =colSs("major_card_no");
   hhIdPseqno =colSs("id_p_seqno");
   hhMajIdPseqno =colSs("major_id_p_seqno");
   hhGroupCode =colNvl("group_code","0000");
   hhCardType =colSs("card_type");
   hhAcctType =colSs("acct_type");
   hhIssueDate =colSs("issue_date");
   return;
}

public boolean hasProjCode(String a_cardNo) throws Exception {
   //-符合專案-
   if (empty(a_cardNo)) return false;
   selectCrdCard(a_cardNo);
   if (empty(hhCardNo)) return false;

   for (int ll = 0; ll <dsParm.rowCount() ; ll++) {
      dsParm.listCurr(ll);
      hhProjCode =dsParm.colSs("proj_code");
      hhItemNo =dsParm.colSs("item_no");
      boolean lb_acctType=false, lb_groupCard=false;
      if (dsParm.colEq("acct_type_flag","Y")) {
         if (!checkParmDetl("01", hhAcctType,"")) {
            continue;
         }
         lb_acctType =true;
      }
      if (dsParm.colEq("group_card_flag","Y")) {
         if (!checkParmDetl("02", hhGroupCode, hhCardType)) continue;
         lb_groupCard =true;
      }
      if (lb_acctType || lb_groupCard) return true;
   }
   return false;
}
//---------
public int checkProjCode(int ll_proj) throws Exception {
   //1.不符合, 0.符合, free_cnt>0可免費--
   dsParm.listCurr(ll_proj);
   hhProjCode = dsParm.colSs("proj_code");
   hhItemNo =dsParm.colSs("item_no");
   hhConsumeType =dsParm.colNvl("consume_type","0");
   hhItType =dsParm.colNvl("it_1_type","1");  //每期金額

   if(empty(hhProjCode))	return 1;
   checkDebutFlag();
   if (dsParm.colEmpty("ok_debut")) return 1;
   checkCardRight();

   return 0;
}
//==========================
void checkCardRight() throws Exception {
   if (dsParm.colEmpty("ok_debut"))
      return;

   hhDebutType = dsParm.colSs("ok_debut");
   hhFreeCnt =0;

   selectCardConsume(isCurrYm[0],isCurrYm[1], hhConsumeType);
   selectCardConsume(isLastYm[0],isLastYm[1], hhConsumeType);
   hhLastProjAmt = hhLastYearConsume;
   hhCurrProjAmt = hhCurrYearConsume;

   //當年消費門檻:
//   String lsprojYm1="", lsprojYm2="";
//   if (dsParm.colEq("curr_cond","Y") && dsParm.colNum("curr_pre_month")>0) {
//      lsprojYm1 =isLastYm[0].substring(0,4)+dsParm.colSs("curr_pre_month");
//      lsprojYm2 =isCurrYm[1];
//      hh_curr_proj_amt =selectCard_consume(lsprojYm1,lsprojYm2);
//   }

   cardFreeCnt1();
   if (hhFreeCnt >0) return;
   cardFreeCnt2();
   if (hhFreeCnt >0) return;
   cardFreeCnt3();
   if (hhFreeCnt >0) return;
   cardFreeCnt4();
   if (hhFreeCnt >0) return;
   cardFreeCnt6();
   if (hhFreeCnt >0) return;

   return;
}
//----------------
void cardFreeCnt1() throws Exception {
   //-0.不計算消費-
   //--消費金額累積方式: 0.不計算消費 1.By ID計算 2.正附卡合併計算 3.正附卡分開計算--
   String ls_consumType =dsParm.colNvl("consume_type","0");

   hhFreeCnt =0;
   //--不計算消費
   if(eq(ls_consumType,"0")){
      hhFreeCnt += dsParm.colInt("consume_00_cnt");
   }
   if (hhFreeCnt ==0)
      return;

   hhFreeType ="1";

   return;
}
void cardFreeCnt2() throws Exception {
   //當年消費門檻: (當年消費累積期間:前一年第  個月起+本年度); 累積刷卡金額達  元(含)以上，享有  次/日
   if ( !dsParm.colEq("curr_cond","Y"))
      return;
   //1.上一年度 N 月(含)至本年度12月底刷卡消費
   String ls_acctMonth1="", ls_acctMonth2="";
   double lm_currMinAmt=0;
   ls_acctMonth2 =isAcctYymm;
   if (dsParm.colEq("choose_cond","1")) {
      String ls_lastMM=commString.lpad(dsParm.colSs("curr_pre_month"),2,"0");
      //==00:只取本年度--
      if (eq(ls_lastMM,"00")) ls_acctMonth1 =commString.left(isAcctYymm,4)+"01";
      else ls_acctMonth1 =isLastYm[0].substring(0,4)+ls_lastMM;
   }
   else if (dsParm.colEq("choose_cond","2")) {
      //2.近 0 個月刷卡消費,單筆最低金額： 0 元(含)以上
      int li_mm =dsParm.colInt("last_mm");
      lm_currMinAmt=dsParm.colNum("curr_min_amt");
      ls_acctMonth1 =commDate.monthAdd(ls_acctMonth2,0 - li_mm);
   }

   //--消費金額累積方式: 0.不計算消費 1.By ID計算 2.正附卡合併計算 3.正附卡分開計算--
   String ls_consumType =dsParm.colNvl("consume_type","0");
   String ls_itType =dsParm.colNvl("it_1_type","1");
   selectBilBillCur(ls_acctMonth1,ls_acctMonth2,lm_currMinAmt,ls_itType,ls_consumType);

   if (hhCurrProjAmt <=0)
      return;

   if (dsParm.colEq("curr_amt_cond","Y") && hhCurrProjAmt >dsParm.colNum("curr_amt")) {
      hhFreeCnt =dsParm.colInt("curr_cnt");
   }
   else if (dsParm.colEq("curr_cnt_cond","Y") && hhCurrTotCnt >dsParm.colInt("curr_tot_cnt")) {
      hhFreeCnt =dsParm.colInt("curr_cnt");
   }
   if (hhFreeCnt <=0) return;

   hhFreeType ="2";
   //updateCms_right_cal("2");
}
//--------
void cardFreeCnt3() throws Exception {
   //前一年消費門檻(舊卡友)[3]
   if (dsParm.colEq("consume_type","0") || !dsParm.colEq("last_cond","Y"))
      return;
   if (hhLastProjAmt <=0)
      return;

   //--只有舊卡友有前一年消費可計算次數
   hhFreeCnt =0;

   double lm_max_amt=0;
   for(int ii=1;ii<=6;ii++){
      if(dsParm.colNum("last_amt"+ii)==0)	break;

      lm_max_amt = dsParm.colNum("last_amt"+ii);
      if(hhLastProjAmt >= lm_max_amt){
         hhFreeCnt = dsParm.colInt("last_cnt"+ii);
         continue;
      }
      break ;
   }
   if (hhFreeCnt <=0) return;

   hhFreeType ="3";
   //-get max_purch_date, curr_max_amt, curr_tot_cnt-
   //selectBil_bill_cur(isAcctYymm,isAcctYymm,0);
   //double lm_per_amt =lm_max_amt;  //Math.round(_max_consume_amt / _free_cnt);
   //updateCms_right_cal("3");
}
void cardFreeCnt4() throws Exception {
   if (dsParm.colEq("cond_per", "Y")==false) return;
   if (hhLastProjAmt <=0) return;

   //--刷卡金額每增加   元，再多享   次/日--
   hhFreeCnt =0;
   if (dsParm.colInt("per_cnt") <= 0) return;
   //--
   if (checkGroupCode05(hhGroupCode)==false)
      return;

   hhFreeCnt = (int) Math.floor(((hhLastProjAmt) / dsParm.colNum("per_amt"))
           * dsParm.colNum("per_cnt"));
   if (hhFreeCnt <=0) return;

   //--
   hhFreeType ="4";
}
void cardFreeCnt6() throws Exception {
//   　06.團體代號　   正卡  附卡，核卡後  日內刷團費或機票 (MCC Code)
//         消費金額: 1.單筆金額 2.累積金額 達  (含)元以上，享有 次/日
   //--核卡後幾日內消費機票團費
   if (dsParm.colEq("air_cond","Y")==false) return;

   if (checkParmDetl("06", hhGroupCode, hhCardType)==false) {
      return;
   }

   //--2.累積金額--
   if (dsParm.colEq("air_amt_type","2")) {
      double lm_totAmt= selectBillAmt();
      if (lm_totAmt >=dsParm.colNum("air_amt")) {
         hhFreeCnt =dsParm.colInt("air_cnt");
      }
   }
   else if (dsParm.colEq("air_amt_type","1")) {
      //1.單筆金額
      int li_totCnt= selectBillCnt();
      if (li_totCnt >0) {
         hhFreeCnt =dsParm.colInt("air_cnt");
      }
   }
   if (hhFreeCnt <=0) return;

   hhFreeType ="6";
}
//--------
boolean checkGroupCode05(String _code) throws Exception {
   //-(未指定表示不考慮團體代號)-
   if(empty(_code))	return false ;
   String ls_key= hhProjCode +"-"+ hhItemNo +"-05";
   //A.proj_code||'-'||A.item_no||'-'||A.data_type as kk_data"
   if (dsDetl.getKeyData(ls_key) <=0) {
      return true;
   }
   int ll_str =dsDetl.getCurrRow();
   int ll_ok=0;
   for(int ii=ll_str; ii<dsDetl.rowCount(); ii++) {
      dsDetl.listCurr(ii);
      if (!dsDetl.colEq("kk_data",ls_key)) break;

      String ls_code1 =dsDetl.colSs("data_code");
//      String ls_code2 =dsDetl.colSs("data_code2");
      if (!empty(ls_code1) && !eq(_code,ls_code1)) continue;
//      if (!empty(ls_code2) && !eq(_code2,ls_code2)) continue;

      ll_ok++;
      break;
   }

   return (ll_ok>0);
}
//--------
boolean checkParmDetl(String _type , String _code , String _code2) throws Exception {

   if(empty(_code) && empty(_code2))	return false ;
   String ls_key= hhProjCode +"-"+ hhItemNo +"-"+_type;
   if (dsDetl.getKeyData(ls_key)<=0) {
      return true;
   }

   int ll_ok=0;
   int ll_str =dsDetl.getCurrRow();
   for(int ii=ll_str; ii<dsDetl.rowCount(); ii++) {
      dsDetl.listCurr(ii);
      if (!dsDetl.colEq("kk_data",ls_key)) break;

      String ls_code1 =dsDetl.colSs("data_code");
      String ls_code2 =dsDetl.colSs("data_code2");
      if (!empty(ls_code1) && !eq(_code,ls_code1)) continue;
      if (!empty(ls_code2) && !eq(_code2,ls_code2)) continue;

      ll_ok++;
      break;
   }

   return (ll_ok>0);
}
//----------
void checkDebutFlag() throws Exception {
   //首年認定：檢核順序為新發卡→首辦卡→舊卡友--
   dsParm.colSet("ok_debut", "");
   String ls_debutFlag=dsParm.colNvl("debut_year_flag","0");
   if (eq(ls_debutFlag,"0")) {
      dsParm.colSet("ok_debut","0");
      return;
   }
   //--首年認定：檢核順序為新發卡→首辦卡→舊卡友 只送一個
   //if (lb_debut) continue;
   String ls_projCode =dsParm.colSs("proj_code");
   if(eq(ls_debutFlag,"1")){
      if(checkDebutCard("1",dsParm.colInt("debut_month1"),ls_projCode,"03")==false){
         return;
      }
   }
   else if(eq(ls_debutFlag,"2")){
      if(checkDebutCard("2",dsParm.colInt("debut_month2"),ls_projCode,"03")==false){
         return;
      }
   }
   else if(eq(ls_debutFlag,"3")) {
      //--舊卡友:持卡第二年起--
      int li_issue_yy =ss2int(hhIssueDate.substring(0,4));
      int li_sys_yy =ss2int(sysDate.substring(0,4));
      if (li_issue_yy >=li_sys_yy) return;
   }

   dsParm.colSet("ok_debut",ls_debutFlag);
}
//-----------
int tiDebut1=-1;
//int tiDebut2=-1;
boolean checkDebutCard(String _debutFlag , int ai_month , String _projCode , String _data_type) throws Exception {
   //首年認定：1. 新發卡:核卡日前  個月至本年度從未持有本行該群組任一信用卡
   // 2. 首辦卡:前一年度第  個月至本年度從未持有本行該群組任一信用卡
   String ls_yymm = "";

   if(eq(_debutFlag,"1"))
      ls_yymm = commDate.dateAdd(sysDate, 0,0 - ai_month, 0);
   else	if(eq(_debutFlag,"2"))	{
      ls_yymm =commDate.dateAdd(sysDate.substring(0,4),-1,ai_month,0);
   }

   if(eq(_debutFlag,"0"))	return true;
   String ls_supFlag="%";
   if (dsParm.colEq("debut_sup_flag_0","Y") && !dsParm.colEq("debut_sup_flag_1","Y"))
      ls_supFlag ="0";
   if (!dsParm.colEq("debut_sup_flag_0","Y") && dsParm.colEq("debut_sup_flag_1","Y"))
      ls_supFlag ="1";

   if (tiDebut1 <=0) {
      //首年認定：1. 新發卡:核卡日前  個月至本年度從未持有本行該群組任一信用卡
      sqlCmd = " select count(*) as debut_cnt1 "
              + " from crd_card A"
              + " where A.id_p_seqno = ? and A.issue_date >=?"+
              " and A.sup_flag like ?"+
              " AND CASE WHEN ?='1' THEN EXISTS "+
              " ( select 1 from cms_right_parm_detl where table_id ='RIGHT' and apr_flag = 'Y' "+
              " and proj_code = ? and data_type ='03' "+
              " and A.group_code like data_code||'%'"+
              " and A.card_type like data_code2||'%'"+
              " )"+
              " WHEN ?='2' THEN NOT EXISTS "+
              " ( select 1 from cms_right_parm_detl where table_id ='RIGHT' and apr_flag = 'Y' "+
              " and proj_code = ? and data_type ='03' "+
              " and A.group_code like data_code||'%'"+
              " and A.card_type like data_code2||'%'"+
              " )"+
              " else 0=0 end"
      ;
      tiDebut1 =ppStmtCrt("ti-debut-1",sqlCmd);
   }

   String ls_groupCond=dsParm.colNvl("debut_group_cond","0");
   ppp(1, hhIdPseqno);
   ppp(ls_yymm);
   ppp(ls_supFlag);
   ppp(ls_groupCond);  //-指定-
   ppp(_projCode);
   ppp(ls_groupCond);  //-指定/排除-
   ppp(_projCode);
   sqlSelect(tiDebut1);
   if (sqlNrow <=0) return false;
   if (colInt("debut_cnt1") >0) return false;
   return true;
}
//--------
int tiConsume1=-1;
void selectCardConsume(String a_ym1, String a_ym2, String a_consumType) throws Exception {
   double lm_amt=0;
   boolean lb_all=false;
//0.不計算消費 1.By 正卡ID計算 2.正附卡合併計算 3.正附卡分開計算
   if (tiConsume1 <=0) {
      sqlCmd ="select sum(consume_bl_amt - sub_bl_amt) as bl_amt"+
              ", sum(consume_ca_amt - sub_ca_amt) as ca_amt"+
              ", sum(consume_it_amt - sub_it_amt) as it_amt"+
              ", sum(consume_ao_amt - sub_ao_amt) as ao_amt"+
              ", sum(consume_id_amt - sub_id_amt) as id_amt"+
              ", sum(consume_ot_amt - sub_ot_amt) as ot_amt"+
              ", sum(consume_bl_cnt - sub_bl_cnt) as bl_cnt" +
              ", sum(consume_ca_cnt - sub_ca_cnt) as ca_cnt" +
              ", sum(consume_it_cnt - sub_it_cnt) as it_cnt" +
              ", sum(consume_ao_cnt - sub_ao_cnt) as ao_cnt" +
              ", sum(consume_id_cnt - sub_id_cnt) as id_cnt" +
              ", sum(consume_ot_cnt - sub_ot_cnt) as ot_cnt"+
              " from mkt_card_consume"+
              " where ori_card_no in ("+
              " select ori_card_no from crd_card where major_id_p_seqno =?"+
              " and id_p_seqno like ?"+
              " and major_card_no like ? and card_no like ?"+
              " )"+
              " and acct_month between ? and ?"
      ;
      tiConsume1 =ppStmtCrt("ti_consume-1","");
   }

   //0.不計算消費 1.By 正卡ID計算 2.正附卡合併計算 3.正附卡分開計算
   //String ls_consumType=dsParm.colSs("consume_type");
   ppp(1, hhMajIdPseqno);
   if (eq(a_consumType,"1")) {
      ppp("%");  //id_p_seqno
      ppp("%");  //major_card_no
      ppp("%");  //card_no
   }
   else if (eq(a_consumType,"2")) {
      ppp("%");  //id_p_seqno
      ppp(hhMajCardNo);  //major_card_no
      ppp("%");  //card_no
   }
   else if (eq(a_consumType,"3")) {
      ppp(hhIdPseqno);  //id_p_seqno
      ppp(hhMajCardNo);  //major_card_no
      ppp(hhCardNo);  //card_no
   }
   else if (eq(a_consumType,"0")) {
      ppp(hhIdPseqno);  //id_p_seqno
      ppp(hhMajCardNo);  //major_card_no
      ppp(hhCardNo);  //card_no
      lb_all=true;
   }
   ppp(a_ym1);
   ppp(a_ym2);
   sqlSelect(tiConsume1);
   if (sqlNrow <=0)
      return;

   int ll_cnt=0;
   if (lb_all || dsParm.colEq("consume_bl","Y")) {
      lm_amt += colNum("bl_amt");
      ll_cnt +=colInt("bl_cnt");
   }
   if (lb_all || dsParm.colEq("consume_ca","Y")) {
      lm_amt +=colNum("ca_amt");
      ll_cnt +=colInt("ca_cnt");
   }
   if (lb_all || dsParm.colEq("consume_it","Y")) {
      lm_amt +=colNum("it_amt");
      ll_cnt +=colInt("it_cnt");
   }
   if (lb_all || dsParm.colEq("consume_ao","Y")) {
      lm_amt +=colNum("ao_amt");
      ll_cnt +=colInt("ao_cnt");
   }
   if (lb_all || dsParm.colEq("consume_id","Y")) {
      lm_amt +=colNum("id_amt");
      ll_cnt +=colInt("id_cnt");
   }
   if (lb_all || dsParm.colEq("consume_ot","Y")) {
      lm_amt +=colNum("ot_amt");
      ll_cnt +=colInt("ot_cnt");
   }

   if (eq(a_ym1,isCurrYm[0])) {
      hhCurrYearConsume =lm_amt;
      hhCurrYearCnt =ll_cnt;
   }
   if (eq(a_ym1,isLastYm[0])) {
      hhLastYearConsume =lm_amt;
      hhLastYearCnt =ll_cnt;
   }

   return;
}
//---------
int tiBill=-1;
public void selectBilBillCur(String a_ym1, String a_ym2, double am_minAmt, String a_itType, String a_consumType) throws Exception {
   //簽帳款(BL),預借現金(CA),分期付款(IT),餘額代償(AO),代收款(ID),其他應收款(OT)
   //it_1_type: 1.每期金額, 2.總金額
   if (tiBill <=0) {
//              " sum(decode(acct_code,'BL',dest_amt,0)) as bl_amt"+
//              ", sum(decode(acct_code,'CA',dest_amt,0)) as ca_amt"+
//              ", sum(decode(acct_code,'AO',dest_amt,0)) as ao_amt"+
//              ", sum(decode(acct_code,'ID',dest_amt,0)) as id_amt"+
//              ", sum(decode(acct_code,'OT',dest_amt,0)) as ot_amt"+
//              ", sum(decode(acct_code,'IT', case when ? ='2' then"+
//              " decode(install_curr_term,1,((install_tot_term -1) * install_per_amt)+install_first_amt+install_fee,0) " +
//              " else dest_amt end,0)) as it_amt" +
//              ", max(purchase_date) as purch_date "+
//              ", max(dest_amt) as curr_max_amt "+
//              ", count(*) as curr_tot_cnt"+
//              ", sum(decode(acct_code,'BL',1,0)) as bl_cnt"+
//              ", sum(decode(acct_code,'CA',1,0)) as ca_cnt"+
//              ", sum(decode(acct_code,'AO',1,0)) as ao_cnt"+
//              ", sum(decode(acct_code,'ID',1,0)) as id_cnt"+
//              ", sum(decode(acct_code,'OT',1,0)) as ot_cnt"+
//              ", sum(decode(acct_code,'IT', case when ? ='2' then decode(install_curr_term,1,1,0) ELSE 0 END, 0)) AS it_cnt " +
      sqlCmd ="select "+
              " A.acct_code"+
              ", case when A.sign_flag ='-' then a.dest_amt*-1 else a.dest_amt end as dest_amt"+
              ", decode(A.acct_code||a.install_curr_term,'IT1'" +
              ",(SELECT B.tot_amt FROM bil_contract B" +
              "  WHERE B.contract_no =A.contract_no AND B.refund_flag<>'Y'),0) AS it_tot_amt"+
              ", A.purchase_date "+
              " from bil_bill A"+
              " where 1=1"+
              " and A.acct_code IN ('BL','CA','AO','IT','ID','OT')"+
              " and A.card_no in (select ori_card_no from crd_card"+
              " where major_id_p_seqno =? and id_p_seqno like ?"+
              " and major_card_no like ? and card_no like ?"+
              " )"+
              " and A.acct_month between ? and ?"+
              " and A.dest_amt >=?"
      ;
      tiBill =ppStmtCrt("ti-S-bill","");
   }
   String ls_itType =nvl(a_itType,"1");   //每期,總額--
   String ls_consumType =nvl(a_consumType,"0");
//   ppp(1,ls_itType);
//   ppp(ls_itType);
   ppp(1, hhMajIdPseqno);
   //0.不計算消費 1.By 正卡ID計算 2.正附卡合併計算 3.正附卡分開計算
   if (eq(ls_consumType,"1")) {
      ppp("%");  //id_p_seqno
      ppp("%");  //major_card_no
      ppp("%");  //card_no
   }
   else if (eq(ls_consumType,"2")) {
      ppp("%");  //id_p_seqno
      ppp(hhMajCardNo);  //major_card_no
      ppp("%");  //card_no
   }
   else if (eq(ls_consumType,"3")) {
      ppp(hhIdPseqno);  //id_p_seqno
      ppp(hhMajCardNo);  //major_card_no
      ppp(hhCardNo);  //card_no
   }
   else if (eq(ls_consumType,"0")) {
      ppp(hhIdPseqno);  //id_p_seqno
      ppp(hhMajCardNo);  //major_card_no
      ppp(hhCardNo);  //card_no
   }
   ppp(a_ym1);
   ppp(a_ym2);
   ppp(am_minAmt);

   daoTid ="bill.";
   sqlSelect(tiBill);
   if (sqlNrow <=0)
      return;

   double lm_amt=0, lm_totAmt=0;
   int ll_cnt=0;
   String ls_maxPurchDate="";
   double lm_maxAmt=0;
   for (int ll = 0; ll <sqlNrow ; ll++) {
      double lm_destAmt =colNum(ll,"bill.dest_amt");
      int li_dest=(lm_destAmt>0 ? 1 :0);
      String lsAccCode=colSs(ll,"bill.acct_code");

      if (dsParm.colEq("consume_bl","Y") && eq(lsAccCode,"BL")) {
         lm_amt +=lm_destAmt;
         ll_cnt +=li_dest;
      }
      if (dsParm.colEq("consume_ca","Y") && eq(lsAccCode,"CA")) {
         lm_amt +=lm_destAmt;
         ll_cnt +=li_dest;
      }
      if (dsParm.colEq("consume_it","Y") && eq(lsAccCode,"IT")) {
         if (eq(ls_itType,"2")) {
            lm_destAmt =colNum(ll,"bill.it_tot_amt");
            li_dest =(lm_destAmt>0 ? 1 :0);
         }
         lm_amt +=lm_destAmt;
         ll_cnt +=li_dest;
      }
      if (dsParm.colEq("consume_ao","Y") && eq(lsAccCode,"AO")) {
         lm_amt +=lm_destAmt;
         ll_cnt +=li_dest;
      }
      if (dsParm.colEq("consume_id","Y") && eq(lsAccCode,"ID")) {
         lm_amt +=lm_destAmt;
         ll_cnt +=li_dest;
      }
      if (dsParm.colEq("consume_ot","Y") && eq(lsAccCode,"OT")) {
         lm_amt +=lm_destAmt;
         ll_cnt +=li_dest;
      }

      if (lm_destAmt >lm_maxAmt) lm_maxAmt=lm_destAmt;
      String ls_purchDate =colSs(ll,"bill.purchase_date");
      if (ssComp(ls_purchDate,ls_maxPurchDate)>0) {
         ls_maxPurchDate =ls_purchDate;
      }
      lm_totAmt +=lm_destAmt;
   }

   hhCurrProjAmt =lm_amt;
   hhCurrTotCnt =ll_cnt;
   hhPurchDate =ls_maxPurchDate;
   hhCurrMaxAmt =lm_maxAmt;
   hhCurrTotAmt =lm_totAmt;
   return;
}
//----
int tiBillamt=-1;
double selectBillAmt() throws Exception {
   //消費金額: 1.單筆金額 2.累積金額 達 20000 (含)元以上，享有 2 次/日[6]
   double lm_amt=0;
   if (tiBillamt <=0) {
      String sql1 ="select sum(dest_amt) as bill_tot_amt from bil_bill"+
              " where purchase_date between ? and ?"+
              " and sign_flag ='+'"+
              " and card_no in (select ori_card_no from crd_card"+
              " where major_id_p_seqno =? and id_p_seqno like ?"+
              " and major_card_no like ? and card_no like ?"+
              " )"+
              " and mcht_category in ( select mcc_code from cms_mcc_group"+
              " where mcc_group in (SELECT data_code3 FROM cms_right_parm_detl"+
              " WHERE proj_code=? AND item_no=? AND apr_flag='Y' and data_type='06'"+
              " and ? like data_code||'%' and ? like data_code2||'%') )"
              ;
      tiBillamt =ppStmtCrt("ti-bill_amt",sql1);
   }
   String ls_date1 = commDate.dateAdd(hBusiDate,0,0,0 - dsParm.colInt("air_day"));
   String ls_sup=dsParm.colNvl("air_sup_flag_0","N")+dsParm.colNvl("air_sup_flag_1","N");
   ppp(1, ls_date1);
   ppp(hBusiDate);
   ppp(hhMajIdPseqno);
   if (eq(ls_sup,"YY")) {
      ppp("%");
      ppp(hhMajCardNo);
      ppp("%");
   }
   else if (eq(ls_sup,"YN")) {
      ppp("%");
      ppp(hhMajCardNo);
      ppp(hhCardNo);
   }
   else if (eq(ls_sup,"NY")) {
      ppp(hhIdPseqno);
      ppp("%");
      ppp(hhCardNo);
   }
   else if (eq(ls_sup,"NN")) {
      ppp(hhIdPseqno);
      ppp(hhMajCardNo);
      ppp(hhCardNo);
   }
   ppp(hhProjCode);
   ppp(hhItemNo);
   ppp(hhGroupCode);
   ppp(hhCardType);
   
   sqlSelect(tiBillamt);
   if (sqlNrow <=0) return 0;
   lm_amt =colNum("bill_tot_amt");

   return lm_amt;
}
//------
int tiBillcnt=-1;
int selectBillCnt() throws Exception {
   int ll_cnt=0;
   //消費金額: 1.單筆金額 2.累積金額 達 20000 (含)元以上，享有 2 次/日[6]
   if (tiBillcnt <=0) {
      String sql1 ="select count(*) as bill_tot_cnt from bil_bill"+
              " where purchase_date between ? and ?"+
              " and sign_flag ='+'"+
              " and card_no in (select ori_card_no from crd_card"+
              " where major_id_p_seqno =? and id_p_seqno like ?"+
              " and major_card_no like ? and card_no like ?"+
              " )"+
              " and mcht_category in ( select mcc_code from cms_mcc_group"+
              " where mcc_group in (SELECT data_code3 FROM cms_right_parm_detl"+
              " WHERE proj_code=? AND item_no=? AND apr_flag='Y' and data_type='06'"+
              " and ? like data_code||'%' and ? like data_code2||'%') )"+
              " and dest_amt >?"
              ;
      tiBillcnt =ppStmtCrt("ti-bill_cnt",sql1);
   }
   double lm_airAmt =dsParm.colNum("air_amt");
   String ls_date1 = commDate.dateAdd(hBusiDate,0,0,0 - dsParm.colInt("air_day"));
   String ls_sup=dsParm.colNvl("air_sup_flag_0","N")+dsParm.colNvl("air_sup_flag_1","N");
   ppp(1, ls_date1);
   ppp(hBusiDate);
   ppp(hhMajIdPseqno);
   if (eq(ls_sup,"YY")) {
      ppp("%");
      ppp(hhMajCardNo);
      ppp("%");
   }
   else if (eq(ls_sup,"YN")) {
      ppp("%");
      ppp(hhMajCardNo);
      ppp(hhCardNo);
   }
   else if (eq(ls_sup,"NY")) {
      ppp(hhIdPseqno);
      ppp("%");
      ppp(hhCardNo);
   }
   else if (eq(ls_sup,"NN")) {
      ppp(hhIdPseqno);
      ppp(hhMajCardNo);
      ppp(hhCardNo);
   }
   ppp(hhProjCode);
   ppp(hhItemNo);
   ppp(hhGroupCode);
   ppp(hhCardType);
   ppp(lm_airAmt);

   sqlSelect(tiBillcnt);
   if (sqlNrow <=0) return 0;
   ll_cnt =colInt("bill_tot_cnt");

   return ll_cnt;
}

//=============================================
@Override
protected void dataProcess(String[] args) throws Exception {

}

public CmsRightParm(Connection conn[], String[] dbAlias, String busiDate) throws Exception {
   dateTime();

   super.conn = conn;
   setDBalias(dbAlias);
   setSubParm(dbAlias);
   hBusiDate =busiDate;
   //selectPtrBusinday();

   return;
}
}
