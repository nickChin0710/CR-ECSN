package Cms;
/**
 * 2023-1226 V1.00.01   JH    優惠金額:price_cond,price
 * */
import com.DataSet;
import com.Parm2sql;

import java.text.Normalizer;

@SuppressWarnings({"unchecked", "deprecation"})
public class CmsC008 extends com.BaseBatch {
private final String PROGNAME = "貴賓室免費使用條件檢核處理  2023-1226 V1.00.01";
private final String whereRowid=" where rowid =CAST(HEXTORAW(?) AS VARCHAR(2000) FOR BIT DATA)";
HH hh=new HH();
//-----------
class HH {
   String rowid = "";
   String id_pseqno = "";
   String card_no = "";
   String item_no = "";
   String vip_kind = "";
   String visit_date = "";
   String visit_seqno = "";
   int ch_visits = 0;
   int guests_count = 0;
   int free_use_cnt = 0;
   int guest_free_cnt = 0;
   double fee_per_holder = 0;
   double fee_per_guest = 0;
   String free_proc_result = "";
   String free_proc_code = "";
   String mcht_no = "";
   double total_fee = 0;
   double total_fee_guest = 0;
   //--
   String acct_type = "";
   String p_seqno = "";
   String group_code="";

   void init_data() {
      rowid = "";
      id_pseqno = "";
      card_no = "";
      item_no = "";
      vip_kind = "";
      visit_date = "";
      visit_seqno = "";
      ch_visits = 0;
      guests_count = 0;
      free_use_cnt = 0;
      guest_free_cnt = 0;
      fee_per_holder = 0;
      fee_per_guest = 0;
      free_proc_result = "";
      free_proc_code = "";
      mcht_no = "";
      total_fee = 0;
      total_fee_guest = 0;
      //--
      acct_type = "";
      p_seqno = "";
      group_code ="";
   }
}

//=========================================
public static void main(String[] args) {
   CmsC008 proc = new CmsC008();
//	proc.debug = true;
   proc.mainProcess(args);
   proc.systemExit();
}
@Override
protected void dataProcess(String[] args) throws Exception {
   dspProgram(PROGNAME);

   if (args.length > 2) {
      printf("Usage : CmsC008 [busi_date(08), callbatch_seqno]");
      okExit(0);
   }

   if (args.length >= 1) {
      if (args[0].length()==8) {
         String sG_Args0 = args[0];
         hBusiDate = Normalizer.normalize(sG_Args0, java.text.Normalizer.Form.NFKD);
      }
      callBatchSeqno(args[0]);
   }
   if (args.length == 2) {
      callBatchSeqno(args[1]);
   }

   dbConnect();
   callBatch(0, 0, 0);

   //-調整每月25日執行-
   int liDD =ss2int(commString.right(hBusiDate,2));
   if (liDD !=25) {
      printf("此作業每月 25 日執行, busi_date[%s]",hBusiDate);
      okExit(0);
   }

   selectPtr_billType();
   selectCms_ppcard_visit();

   sqlCommit();
   endProgram();
}
//--------
com.DataSet dsBtype=new DataSet();
void selectPtr_billType() throws Exception {
   sqlCmd ="SELECT decode(bill_type,'INPP','1','INDR','2',bill_type) AS vip_kind"
       +", txn_code, acct_code, exter_desc AS bill_desc"
       +" FROM ptr_billtype "
       +" WHERE 1=1 "
       +" AND bill_type IN ('INDR','INPP') "
       +" ORDER BY 1"
       ;
   sqlQuery(dsBtype,"",null);
}
//--------
void selectCms_ppcard_visit() throws Exception {
   sqlCmd ="select hex(rowid) as rowid"+
           ", crt_date||'-'||data_seqno as visit_seqno"+
           ", pp_card_no, id_p_seqno, card_no, item_no, vip_kind"+
           ", visit_date, ch_visits, guests_count"+
           ", free_use_cnt, guest_free_cnt"+
           ", fee_per_holder, fee_per_guest, total_fee, total_free_guests"+
           ", tot_charg_guest, charg_guest_value"+
           ", use_flag, use_date, free_cnt, expn_cnt"+
           ", guest_cost_amt, mcht_no"+
       ", (SELECT group_code FROM crd_card WHERE card_no=cms_ppcard_visit.card_no) AS group_code"+
           " from cms_ppcard_visit"+
           " where use_flag <>'Y'"+
           " and err_code ='00'"+
           " and (ch_visits+guests_count-free_use_cnt-guest_free_cnt)>0"+
           " AND card_no <>''"+
           " order by visit_date, card_no"
           ;
   openCursor();
   while(fetchTable()) {
      totalCnt++;
      hh.init_data();
      hh.rowid =colSs("rowid");
      hh.visit_seqno =colSs("visit_seqno");
      hh.card_no =colSs("card_no");
      hh.id_pseqno =colSs("id_p_seqno");
      hh.item_no =colSs("item_no");
      hh.vip_kind =colSs("vip_kind");
      hh.visit_date =colSs("visit_date");
      hh.ch_visits =colInt("ch_visits");
      hh.guests_count =colInt("guests_count");
      hh.free_use_cnt =colInt("free_use_cnt");
      hh.guest_free_cnt =colInt("guest_free_cnt");
      hh.fee_per_holder =colNum("fee_per_holder");
      hh.fee_per_guest =colNum("fee_per_guest");
      hh.mcht_no =colSs("mcht_no");
      hh.group_code =colSs("group_code");

      checkFreeUseCnt();
      //-產生費用-
      selectCrd_card();
      hh.total_fee=(hh.ch_visits - hh.free_use_cnt) * hh.fee_per_holder;
      hh.total_fee_guest =(hh.guests_count - hh.guest_free_cnt) * hh.fee_per_guest;
      if (hh.total_fee <0) hh.total_fee=0;
      if (hh.total_fee_guest <0) hh.total_fee_guest=0;
      if (hh.total_fee >0 || hh.total_fee_guest >0) {
         insertBil_sysexpn();
      }

      updateCms_ppcard_visit();
   }

}
//---------
int tiCal=-1;
void checkFreeUseCnt() throws Exception {
   if (tiCal <=0 ) {
      sqlCmd = "select hex(A.rowid) as rowid"+
              ", A.proj_code, A.free_cnt, A.use_cnt, A.use_month" +
              ", B.use_cnt_cond, B.use_max_cnt" +
              " from cms_right_cal A left join cms_right_parm B" +
              "   on A.item_no=B.item_no and A.proj_code=B.proj_code" +
              " where 1=1" +
              " and A.card_no =?" +
              " and A.item_no =?" +
              " and A.use_month like ?" +  //本年度可用--
              " and A.free_cnt >A.use_cnt" +
              " order by A.use_month"
      ;
      tiCal =ppStmtCrt("ti-S-cal","");
   }

   String ls_useMM =hh.visit_date.substring(0,4);
   ppp(1, hh.card_no);
   ppp(hh.item_no);
   ppp(ls_useMM+"%");
   daoTid ="cal.";
   sqlSelect(tiCal);
   if (sqlNrow <=0) {
      hh.free_proc_code ="01";
      hh.free_proc_result ="無免費可用次數";
      return;
   }
   int llNrow=sqlNrow;
   int li_visit=hh.ch_visits;
   int li_guest=hh.guests_count;
   for (int ll = 0; ll <llNrow ; ll++) {
      String ls_useCond=colNvl("cal.use_cnt_cond","Y");
      int li_calFreeCnt =colInt("cal.cal_free_cnt");
      int li_calUseCnt =colInt("cal.cal_use_cnt");
      int li_useCnt =totalUseCnt(ls_useCond);
      int li_maxCnt=colInt("cal.use_max_cnt");
      while (li_useCnt >=li_maxCnt) continue;
      boolean lb_updateCal=false;
      //-cardHolder-
      while (li_visit>0 && (li_useCnt <li_maxCnt) && li_calUseCnt<li_calFreeCnt) {
         li_visit--;
         hh.free_use_cnt++;
         li_useCnt++;
         li_calUseCnt++;
         lb_updateCal =true;
      }
      //-guest-
      while (li_useCnt <li_maxCnt && li_guest>0 && li_calUseCnt<li_calFreeCnt) {
         li_guest--;
         hh.guest_free_cnt++;
         li_useCnt++;
         li_calUseCnt++;
         lb_updateCal =true;
      }
      if (lb_updateCal) {
         updateCms_right_cal(colSs("cal.rowid"), li_calUseCnt);
      }
      if (li_visit <=0 && li_guest<=0) break;
   }
   if (li_visit>0 || li_guest >0) {
      hh.free_proc_code ="03";
      hh.free_proc_result ="免費次數不足";
   }

}
//---------------
int tiMax=-1;
int totalUseCnt(String a_useCond) throws Exception {
   if (tiMax <=0) {
      sqlCmd ="select sum(free_use_cnt) as use_free, sum(guest_free_cnt) guest_free"+
              " from cms_ppcard_visit"+
              " where 1=1"+
              " and card_no in (select ori_card_no from crd_card where card_no=?)"+
              " and item_no =?"+
              " and visit_date between ? and ?"+
              "";
      tiMax =ppStmtCrt("ti-S-max","");
   }
   String ls_date2=hh.visit_date;
   String ls_date1="";
   if (eq(a_useCond,"Y")) ls_date1=commString.left(hh.visit_date,4)+"0101";
   else if (eq(a_useCond,"M")) ls_date1=commString.left(hh.visit_date,6)+"01";
   else if (eq(a_useCond,"S")) {
      ls_date1 =commString.left(hh.visit_date,4);
      int MM=ss2int(commString.mid(hh.visit_date,4,2));
      if (MM>=1 && MM<=3) ls_date1 +="0101";
      else if (MM>=4 && MM<=6) ls_date1 +="0401";
      else if (MM>=7 && MM<=9) ls_date1 +="0701";
      else if (MM>=10 && MM<=12) ls_date1 +="1001";
   }
   ppp(1, hh.card_no);
   ppp(hh.item_no);
   ppp(ls_date1);
   ppp(ls_date2);
   daoTid ="max.";
   sqlSelect(tiMax);
   if (sqlNrow <=0) return 0;
   int li_useCnt=colInt("max.use_free")+colInt("max.guest_cnt");
//   if (li_useCnt >= ai_maxCnt) {
////      hh.free_proc_code ="02";
////      hh.free_proc_result ="使用次數已超過["+li_useCnt+">"+ai_maxCnt+"]";
//      return 1;
//   }
   return li_useCnt;
}
//===============
int tiCard=-1;
void selectCrd_card() throws Exception {
   //006	holder_amt          	//-INTEGER (4,0)  卡人自費金額
   //007	toget_amt           	//-INTEGER (4,0)  同行旅客金額
   if (tiCard <=0) {
      sqlCmd ="select A.acct_type, A.p_seqno"+
              ", B.holder_amt, B.toget_amt"+
              " from crd_card A left join mkt_ppcard_issue B" +
              "   on A.bin_type=B.bin_type and A.group_code=B.group_code"+
              " where A.card_no =?";
      tiCard =ppStmtCrt("ti-S-card","");
   }

   ppp(1, hh.card_no);
   sqlSelect(tiCard);
   if (sqlNrow >0) {
      hh.acct_type =colSs("acct_type");
      hh.p_seqno =colSs("p_seqno");
      if (hh.fee_per_holder ==0) hh.fee_per_holder=colNum("holder_amt");
      if (hh.fee_per_guest ==0) hh.fee_per_guest=colNum("toget_amt");
   }
}
//======================
com.Parm2sql ttUvisit=null;
void updateCms_ppcard_visit() throws Exception {
   if (ttUvisit ==null) {
      ttUvisit =new Parm2sql();
      ttUvisit.update("cms_ppcard_visit");
   }

//   double lm_totFee =(hh.ch_visits - hh.free_use_cnt) * hh.fee_per_holder;
//   double lm_totFeeGues =(hh.guests_count - hh.guest_free_cnt) * hh.fee_pre_guest;
//   if (lm_totFee <0) lm_totFee=0;
//   if (lm_totFeeGues <0) lm_totFeeGues=0;

   int ll_freeCnt =hh.free_use_cnt+hh.guest_free_cnt;
   int ll_expnCnt =hh.ch_visits+hh.guests_count - hh.free_use_cnt - hh.guest_free_cnt;
   if (ll_freeCnt <0) ll_freeCnt=0;
   if (ll_expnCnt <0) ll_expnCnt=0;

   String ls_procResult="";
   if (noEmpty(hh.free_proc_code)) {
      ls_procResult =hh.free_proc_code+hh.free_proc_result;
   }

   ttUvisit.aaa("fee_per_holder", hh.fee_per_holder);
   ttUvisit.aaa("fee_per_guest", hh.fee_per_guest);
   ttUvisit.aaa("total_fee", hh.total_fee_guest);
   ttUvisit.aaa("total_free_guests", hh.total_fee_guest);
   ttUvisit.aaa("free_use_cnt", hh.free_use_cnt);
   ttUvisit.aaa("guest_free_cnt", hh.guest_free_cnt);
   ttUvisit.aaa("free_cnt", ll_freeCnt);
   ttUvisit.aaa("expn_cnt", ll_expnCnt);
   ttUvisit.aaa("free_proc_result", ls_procResult);
   ttUvisit.aaa("use_flag", "Y");
   ttUvisit.aaa("use_date", hBusiDate);
   ttUvisit.aaaModxxx(hModUser, hModPgm);
   ttUvisit.aaaWhere(whereRowid, hh.rowid);

   if (ttUvisit.ti <=0) {
      ttUvisit.ti =ppStmtCrt("tt-U-visit", ttUvisit.getSql());
   }

   sqlExec(ttUvisit.ti, ttUvisit.getParms());
   if (sqlNrow <=0) {
      sqlerr("update cms_ppcard_visit error");
      okExit(0);
   }

}
//---------
Parm2sql ttUcal=null;
void updateCms_right_cal(String _rowid, int _useCnt) throws Exception {
   if (ttUcal ==null) {
      ttUcal =new Parm2sql();
      ttUcal.update("cms_right_cal");
   }
   ttUcal.aaa("use_cnt", _useCnt);
   ttUcal.aaaWhere(whereRowid, _rowid);
   if (ttUcal.ti <=0) {
      ttUcal.ti =ppStmtCrt("tt-U-cal","");
   }

   sqlExec(ttUcal.ti);
   if (sqlNrow <=0) {
      sqlerr("update cms_right_cal error, kk[%s,%s]", hh.card_no, hh.item_no);
      okExit(0);
   }

}
//-----------------
int tiRdetl=-1;
double getPriceCond() throws Exception {
   if (empty(hh.group_code)) return 0;
   
   double lm_price=0;
   String aaGroupCode=""+
       "'1880','1881','1882','1890','1891','1892','1893','1894','1630','1631',"+
       "'1640','1641','1642','1644','1650','1651','1652','1654','1655','1656',"+
       "'1657','1660','1661','1662','1663','1670','1671','1672','6673','6674',"+
       "'1675','1676','1677','1678','1679','1680','1681','1682','1683','1684',"+
       "'1685','1686','1687','1688','1689','1690','1691','1692','1693','1694',"+
       "'1600','1601','1602','1603','1604','1605','1607','1608','1610','1611',"+
       "'1612','1613','1614','1615','1616','1618','1620','1621','1622','1653',"+
       "'3700','3720','3770','3780','3781','3782','3783','3790','5397','5398',"+
       "'5399','5546','5703','3790','3701','3702','5703','3704','3713','3750',"
       ;
   //-不是指定優惠金額---
   if (aaGroupCode.indexOf(hh.group_code) <0) return 0;
   if (tiRdetl <=0) {
      sqlCmd ="SELECT A.proj_code, A.item_no, A.price_cond, A.price "+
          " FROM cms_right_parm A "+
          " WHERE 1=1 "+
          " AND A.apr_flag ='Y' AND A.price_cond ='Y'"+
          " AND A.item_no =? "+
          " AND NOT EXISTS (SELECT 1 FROM CMS_RIGHT_PARM_DETL "+
          " WHERE proj_code=A.proj_code AND item_no=A.item_no "+
          " AND data_type='06' AND data_code=?)"
          ;
      tiRdetl =ppStmtCrt("ti-Rdetl","");
   }
   ppp(1, hh.item_no);
   ppp(hh.group_code);
   sqlSelect(tiRdetl);
   if (sqlNrow <=0) return 0;

   lm_price =colNum("price");
   return lm_price;
}
//-----------
Parm2sql ttAexp=null;
void insertBil_sysexpn() throws Exception {
   if (ttAexp ==null) {
      ttAexp =new Parm2sql();
      ttAexp.insert("bil_sysexp");
   }
   String ls_billType="OKOL";
   String ls_txnCode="HC";
   String ls_billDesc="貴賓室使用費用";
//   String ls_srcType="TC";
   for (int ll = 0; ll <dsBtype.rowCount() ; ll++) {
      dsBtype.listCurr(ll);
      if (dsBtype.colEq("vip_kind",hh.vip_kind) ==false) continue;
      ls_billType =dsBtype.colSs("bill_type");
      ls_txnCode =dsBtype.colSs("txn_code");
      ls_billDesc =dsBtype.colSs("bill_desc");
      break;
   }
   //-優惠價-
   double lm_srcAmt =getPriceCond();
   if (lm_srcAmt <=0) {
      lm_srcAmt =hh.total_fee+hh.total_fee_guest;
   }

   ttAexp.aaa("card_no", hh.card_no);
   ttAexp.aaa("acct_type", hh.acct_type);
   ttAexp.aaa("p_seqno", hh.p_seqno);
   ttAexp.aaa("bill_type"          , ls_billType);
   ttAexp.aaa("txn_code"           , ls_txnCode);
   ttAexp.aaa("purchase_date"      , hh.visit_date);
   ttAexp.aaa("src_type"           , "TC");  //???
   ttAexp.aaa("mcht_no"            , hh.mcht_no);  //???
   ttAexp.aaa("dest_curr"          , "901");
   ttAexp.aaa("src_amt"            , lm_srcAmt);
   ttAexp.aaa("bill_desc"          , ls_billDesc);
   ttAexp.aaa("post_flag"          , "N");
   ttAexp.aaa("curr_code"          , "901");
   ttAexp.aaa("ref_key"            , hh.visit_seqno);
   ttAexp.aaa("dc_dest_amt"        , hh.total_fee+hh.total_fee_guest);
   ttAexp.aaaModxxx(hModUser, hModPgm);

   if (ttAexp.ti <=0) {
      ttAexp.ti =ppStmtCrt("tt-A-exp", ttAexp.getSql());
   }

   sqlExec(ttAexp.ti, ttAexp.getParms());
   if (sqlNrow <=0) {
      sqlerr("insert bil_sysexp error");
      okExit(0);
   }
}

}
