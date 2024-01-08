/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*  DATE      Version     AUTHOR              DESCRIPTION                      *
*  --------- ----------  ----------- ---------------------------------------- *
*  112/07/21 V1.01.02    Lai         Program initial                          *
*                                                                             *
******************************************************************************/
package Mkt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.BaseBatch;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;

public class MktE030 extends BaseBatch {
    private final String PROGNAME = "卡友各項權益統計-機場停車   112/07/21 V1.01.02";
    CommFunction   comm    = new CommFunction();
    CommCrd        commCrd = new CommCrd();
    CommCrdRoutine comcr;
    
    int     DEBUG   = 0;
    int     DEBUG_F = 0;
    int     TEST_0  = 0;

    String prgmId   = "MktE030";
    String rptName  = "機場停車";
    String rptId    = "MktE030R0";
    int    rptSeq   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();
    String ItemNo   = "09";

    private String busiDate    = "";
    private String busiMonth   = "";
    private String busiPreYear = "";
    private int    totCnt      = 0;
    private int    okCnt       = 0;
    private int    dtlCnt      = 0;
    private int    curCnt      = 0;
    private int    billCnt     = 0;

    double purchAmt      = 0; //累積消費金額
    int    purchCnt      = 0; //累積消費次數
    int    useCnt        = 0;
    int    freeCnt       = 0;
    double rcvAnnualFee  = 0;
    int    putBil        = 0;
    int    putDbb        = 0;
    String CardNo        = "";
    String IdPseqNo      = "";
    String ProjCode      = "";
    String AcctType      = "";
    String CardType      = "";
    String GroupCode     = "";
    String ConsumeType   = "";
    int    FreeCntBasic  = 0;
    int    UseCnt        = 0;

/*******************************************************************/
@Override
protected void dataProcess(String[] args) throws Exception {
}
/*******************************************************************/
public static void main(String[] args) throws Exception {
        MktE030 proc = new MktE030();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
}
/*******************************************************************/
@Override
public int mainProcess(String[] args) 
{
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
                commCrd.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            }
            
            if (!connectDataBase()) {
                commCrd.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallRProgramCode = javaProgram;
            comcr.callbatch(0, 0, 0);
            
            if (args.length >  2) 
                commCrd.errExit(String.format("Usage : MktE030 [BUSINESS_DATE][Y]"),"");
            
            if (args.length > 0) {
                busiDate  = args[0];
                busiMonth = commDate.dateAdd(busiDate, 0, -1, 0).substring(0, 6);
            } else {
                selectPtrBusinday();
            }

            busiPreYear = commDate.dateAdd(busiMonth, -1, 0, 0).substring(0, 4);

            if (!busiDate.substring(6).equals("01")) {
                commCrd.errExit(String.format("本程式只在每月1日執行[%s]", busiDate), "");
            }

            selectSQL = "  to_char(((to_date(?,'yyyymmdd')) - (dayofweek(to_date(?,'yyyymmdd'))-6)  days),'yyyymmdd') week_date_s "
                      + ", to_char(((to_date(?,'yyyymmdd')) - (dayofweek(to_date(?,'yyyymmdd'))-12) days),'yyyymmdd') week_date_e ";
            daoTable  = "dual  ";
            setString(1 , busiDate);
            setString(2 , busiDate);
            setString(3 , busiDate);
            setString(4 , busiDate);

            int recordCnt = selectTable();

            showLogMessage("I","", String.format("程式參數[%s][%s]",busiDate,getValue("week_date_s")));
            showLogMessage("I","", String.format("執行日期[%s]", sysDate));
            
            int cnt = selectCmsRightYearDtl();
            if (cnt > 0) {
                commCrd.errExit("本月("+busiMonth+")己產製名單,不可重覆執行", "");
            }

            // 刪除前24個月的數據
            deleteCmsRightYearDtl();

            selectCmsRightParm(busiDate);

            String filename = String.format("%s/reports/%s_%s.TXT",commCrd.getECSHOME(),rptId,sysDate);
            commCrd.writeReport(filename, lpar1);
          //comcr.insertPtrBatchRpt(lpar1);

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
            showLogMessage("I", "", "程式執行結束，檔案筆數[" + curCnt + "]");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
}
/*******************************************************************/
protected void selectPtrBusinday() throws Exception 
{
  busiDate = "";
  sqlCmd = " select business_date "
         + "      , to_char(add_months(to_date(business_date,'yyyymmdd'), -1),'yyyymm') h_prev_month " 
         + " from ptr_businday";

  selectTable();
  if (notFound.equals("Y")) {
      commCrd.errExit("select_ptr_businday not found!", "");
  }
  busiDate  = getValue("business_date");
  busiMonth = getValue("h_prev_month");
}
/*******************************************************************/
void initData() throws Exception {
   
    purchAmt      = 0; //累積消費金額
    purchCnt      = 0; //累積消費次數
    useCnt        = 0;
    rcvAnnualFee  = 0;
}
/*******************************************************************/
private int selectCmsRightYearDtl() throws Exception 
{

  sqlCmd = " select card_no from  cms_right_year_dtl "
         + "  where acct_month = ? and item_no = ? fetch FIRST 1 ROW ONLY ";

  setString(1, busiMonth); 
  setString(2, ItemNo); 

  int rtn =  selectTable();

  showLogMessage("I","","select cms_right_year_dtl cnt["+ rtn +"]" + busiMonth);
  return rtn;
}
/*******************************************************************/
// 刪除24個月前的數據
void deleteCmsRightYearDtl() throws Exception 
{
  String dateM = commDate.dateAdd(busiDate, 0, -24, 0).substring(0,6);
  showLogMessage("I", "", " 前24個月=[" + dateM + "]");

  daoTable  = "cms_right_year_dtl ";
  whereStr  = "where acct_month  <= ? ";

  setString(1, dateM); 

  int recCnt = deleteTable();

  showLogMessage("I","","delet cms_right_year_dtl cnt["+ recCnt +"]" +dateM);

}
/*******************************************************************/
// 2.6. 讀取【cms_right_parm 卡友權益資格參數主檔】
// where條件active_status=Y and apr_flag=Y and ITEM_NO=12 and介於權益專案生效日起迄(proj_date_s、proj_date_e)
private List<CmsRightParm> selectCmsRightParm(String date) throws Exception 
{
  String AA=daoTid = "parm1.";
  sqlCmd  = " select * from cms_right_parm ";
  sqlCmd += "  where active_status = 'Y' and apr_flag = 'Y' and item_no = ? ";
  sqlCmd += "    and decode(proj_date_s,'','20100101',proj_date_s) <= ? ";
  sqlCmd += "    and decode(proj_date_e,'','30000101',proj_date_e) >= ? ";
  ppp(1, ItemNo);
  ppp(2, date);
  ppp(3, date);
  sqlSelect();
  int ilSelectRow = sqlNrow;
  List<CmsRightParm> parmlist = new ArrayList<>();
  if (sqlNrow <= 0) {
      commCrd.errExit("select cms_right_parm not found!", "");
      errExit(1);
  }
if(DEBUG==1) showLogMessage("I", "", "Read Parm cnt=[" + ilSelectRow + "]");
  for (int ii = 0; ii < ilSelectRow; ii++) {
  
      CmsRightParm cmsRightParm = new CmsRightParm();
      parmlist.add(cmsRightParm);
      cmsRightParm.projCode     = colSs(ii,  AA+"proj_code");
      cmsRightParm.it1Type      = colSs(ii,  AA+"it_1_type");
      cmsRightParm.comsumeType  = colSs(ii,  AA+"consume_type");
      cmsRightParm.consumeBl    = colSs(ii,  AA+"consume_bl");
      cmsRightParm.consumeCa    = colSs(ii,  AA+"consume_ca");
      cmsRightParm.consumeIt    = colSs(ii,  AA+"consume_it");
      cmsRightParm.consumeAo    = colSs(ii,  AA+"consume_ao");
      cmsRightParm.consumeId    = colSs(ii,  AA+"consume_id");
      cmsRightParm.consumeOt    = colSs(ii,  AA+"consume_ot");
      cmsRightParm.currCond     = colSs(ii,  AA+"curr_cond");
      cmsRightParm.currMinAmt   = colNum(ii, AA+"curr_min_amt");
      cmsRightParm.currPreMonth = colSs(ii,  AA+"curr_pre_month");
      cmsRightParm.currAmt      = colNum(ii, AA+"curr_amt");
      cmsRightParm.currAmtCond  = colSs(ii,  AA+"curr_amt_cond");
      cmsRightParm.currCnt      = colInt(ii, AA+"curr_cnt");
      cmsRightParm.currCntCond  = colSs(ii,  AA+"curr_cnt_cond");
      cmsRightParm.currTotCnt   = colInt(ii, AA+"curr_tot_cnt");
      cmsRightParm.consume00Cnt = colInt(ii, AA+"consume_00_cnt");
      cmsRightParm.airCond      = colSs(ii,  AA+"air_cond");
      cmsRightParm.airSupFlag0  = colSs(ii,  AA+"air_sup_flag0");
      cmsRightParm.airSupFlag1  = colSs(ii,  AA+"air_sup_flag1");
      cmsRightParm.airDay       = colSs(ii,  AA+"air_day");
      cmsRightParm.airAmtType   = colSs(ii,  AA+"air_amt_type");
      cmsRightParm.aAirRight    = colSs(ii,  AA+"a_air_right");
      cmsRightParm.airAmt       = colNum(ii, AA+"air_amt");
      cmsRightParm.airCnt       = colInt(ii, AA+"air_cnt");
      cmsRightParm.lastMm       = colSs(ii,  AA+"last_mm");
      cmsRightParm.chooseCond   = colSs(ii,  AA+"choose_cond");
      cmsRightParm.consumeType  = colSs(ii,  AA+"consume_type");


      // 取得【cms_right_parm】join【cms_right_parm_detl】資料
      cmsRightParm.detlList = selectCmsRightParm_Detl(cmsRightParm.projCode);

      showLogMessage("I","","Main PROJ="+cmsRightParm.projCode+"["+cmsRightParm.chooseCond+"]"+cmsRightParm.consumeType+","+cmsRightParm.lastMm+","+cmsRightParm.currCnt+", dtlCnt="+dtlCnt);
      if (dtlCnt < 1 ) continue;

      // 取得一般消費期間與特殊消費期間
      getAcctMonth(cmsRightParm);
      // 取得符合條件卡號及計算消費、存入DB、寫入檔案
//    getCardNo();
      selectBilBill(cmsRightParm);
      if(putBil > 0)
         insertCmsRightYearDtl(cmsRightParm , 1);
      showLogMessage("I","", "main. Credit 筆數  total=["+totCnt+"] ok=["+okCnt+"]"+putBil);

      totCnt=0; okCnt=0;
      BillInfo billInfo = null;
      selectDbbBill(cmsRightParm);
      if(putDbb > 0)
         insertCmsRightYearDtl(cmsRightParm , 2);

      showLogMessage("I","", "Vd     筆數  total=["+totCnt+"] ok=["+okCnt+"]"+putDbb);
  }
  
if(DEBUG==1) showLogMessage("I", "", "Read Parm End");

  return parmlist;
}
/*******************************************************************/
private List<CmsRightParmDetl> selectCmsRightParm_Detl(String projCode) throws Exception 
{
  sqlCmd = " select table_id, proj_code, item_no, apr_flag, data_type, data_code, data_code2, data_code3 "
    + "  from cms_right_parm_detl "
    + " where item_no   = ? "
    + "   and table_id  = 'RIGHT' "
    + "   and proj_code = ? ";
  setString(1, ItemNo);
  setString(2, projCode);
  sqlSelect();

  dtlCnt        = sqlNrow;
  int selectRow = sqlNrow;
  showLogMessage("I","","  DTL cnt="+projCode+"["+dtlCnt+"]");
  List<CmsRightParmDetl> detlList = new ArrayList<>();
  if (sqlNrow <= 0) {
//    commCrd.errExit("select cms_right_parm_detl not found!", projCode);
  } else {
      for (int ii = 0; ii < selectRow; ii++) {
         CmsRightParmDetl detail = new CmsRightParmDetl();
         detail.dataTypeTmp  = colSs(ii, "data_type");
         detail.dataCodeTmp  = colSs(ii, "data_code");
         detail.dataCode2Tmp = colSs(ii, "data_code2");
         detail.dataCode3Tmp = colSs(ii, "data_code3");
         detlList.add(detail);
      }
  }
  
  return detlList;
}
/*******************************************************************/
// 2.8. 取得acct_month起迄值,計算消費期間
private void getAcctMonth(CmsRightParm parm) throws ParseException 
{
            
  if (eqIgno(parm.currCond, "Y")) {
      if (eqIgno(parm.chooseCond, "1")) {
          if (parm.currPreMonth.length() == 1) {
              parm.acctMonthS = busiPreYear + "0" + parm.currPreMonth;
          } else if (parm.currPreMonth.length() > 1) {
              parm.acctMonthS = busiPreYear + parm.currPreMonth;
          } else {
              parm.acctMonthS = busiPreYear + "01";
          }
          parm.acctMonthE = busiMonth.substring(0, 4) + "12";
      } 
      if (eqIgno(parm.chooseCond, "2")) {
          SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
          Calendar instance = Calendar.getInstance();
          instance.setTime(format.parse(busiDate));
          instance.add(Calendar.MONTH, -Integer.parseInt(parm.lastMm));
          parm.acctMonthS = format.format(instance.getTime()).substring(0, 6);
          parm.acctMonthE = busiMonth;
      }
  }
  if(parm.acctMonthS.substring(4,6).equals("00")) parm.acctMonthS = parm.acctMonthS.substring(0,4)+"01";

  showLogMessage("I","","  Cond=["+parm.chooseCond+"]"+parm.acctMonthS+","+parm.acctMonthE+","+parm.airCond+","+parm.currPreMonth);

  if (eqIgno(parm.airCond, "Y")) {
      SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
      Calendar instance   = Calendar.getInstance();
      instance.setTime(format.parse(busiDate));
      instance.add(Calendar.MONTH, -Integer.parseInt(parm.airDay));
      parm.specAcctMonthS = format.format(instance.getTime()).substring(0, 6);
      parm.specAcctMonthE = busiMonth;
  }

  showLogMessage("I", "", "  Month mon=["+parm.acctMonthS+"]"+parm.acctMonthE);
  showLogMessage("I", "", "        spe=["+parm.specAcctMonthS+"]"+parm.specAcctMonthE);
 
}
/*******************************************************************/
void selectCycAfee() throws Exception {
   
   sqlCmd = "select rcv_annual_fee "
          + " from cyc_afee "
          + " where card_no        = ? "
          + "   and fee_date between ? and ? "
          + "   and rcv_annual_fee > 0 "
          + commSqlStr.rownum(1)
          ;

   ppp(1, CardNo);
   ppp(2, busiMonth+"01");
   ppp(3, busiMonth+"31");
   sqlSelect();
   if (sqlNrow >0) {
      rcvAnnualFee   = colNum("rcv_annual_fee");
   }
}
/*******************************************************************/
void selectMktPostConsume() throws Exception {
   
      sqlCmd = "select " 
             + " consume_bl_amt+consume_ca_amt+consume_it_amt+ " 
             + " consume_ao_amt+consume_id_amt+consume_ot_amt as consum_amt " 
             + ",consume_bl_cnt+consume_ca_cnt+consume_it_cnt+ " 
             + " consume_ao_cnt+consume_id_cnt+consume_ot_cnt as consum_cnt "
             + " from mkt_post_consume"
             + " where card_no     = ? "
             + "   and acct_month  = ? ";
  

   ppp(1, CardNo);
   ppp(2, busiMonth);
   sqlSelect();
   if (sqlNrow >0) {
      purchAmt = colNum("consum_amt");
      purchCnt = colInt("consum_cnt");
   }

}
/*******************************************************************/
// 一般消費
private void selectBilBill(CmsRightParm parm) throws Exception 
{
if(TEST_0 ==1)  {parm.acctMonthS = "202106"; parm.acctMonthE = "202106";}

  sqlCmd = " select 1 as db_cnt, "
         + "    decode(a.acct_code,'IT', decode(?,'2', decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt), 0),decode(b.refund_apr_flag,'Y',0,a.dest_amt)), "
         + "          case when a.txn_code in ('06','25','27','28','29') "
         + "               then a.dest_amt*-1 else a.dest_amt end) db_amt "
         + ", c.card_no,c.group_code,c.p_seqno  ,c.acct_type,c.major_id_p_seqno,c.id_p_seqno"
         + ", c.major_card_no,c.old_card_no,c.current_code,c.card_type "
         + ", a.ecs_cus_mcht_no,a.mcht_category,a.purchase_date,a.dest_amt "
         + ", d.id_no, d.chi_name, d.cellar_phone, s.id_no as major_id "
         + "  from bil_bill a "
         + "  left join bil_contract b on b.contract_no  = decode(a.contract_no,'','x',a.contract_no) "
         +                          " and b.contract_seq_no = a.contract_seq_no "
         + "  left join crd_card c     on a.card_no    = c.card_no  "
         + "  left join crd_idno d     on d.id_p_seqno = c.id_p_seqno  "
         + "  left join crd_idno s     on s.id_p_seqno = c.major_id_p_seqno  "
         + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
         + "       and c.card_type = a.card_type "
         + "       and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                // 消費資料 六大本金類
         + "       and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
         +                                                          " decode( ?,'Y','IT','XX'), "
         +                                                          " decode( ?,'Y','ID','XX'), "
         +                                                          " decode( ?,'Y','CA','XX'), "
         +                                                          " decode( ?,'Y','AO','XX'), "
         +                                                          " decode( ?,'Y','OT','XX')) "
                // 消費資料 最低單筆金額
         + "       and (( a.txn_code in ('06', '25', '27', '28', '29') ) "
         + "              or ( a.txn_code not in ('06', '25', '27', '28', '29') and a.dest_amt >= ? )) "
                // 消費資料 消費期間
         + "       and a.acct_month  between ? and ? "

                // 2.10.3.  排除一般消費 - 排除 ECS_CUS_MCHT_NO 的筆數
         + "       and (ecs_cus_mcht_no = '' or (not exists ( select 1 from mkt_mcht_gp e "
         + "                     left join mkt_mchtgp_data f on e.mcht_group_id = f.data_key "
         + "                     where f.table_name    = 'MKT_MCHT_GP' "
         + "                       and f.data_key      = 'MKTR00001' "
         + "                       and e.platform_flag = '2' "
         + "                       and f.data_code    != '' "
         + "                       and f.data_code     = a.ecs_cus_mcht_no )))";
if(TEST_0==1)  // 202106
  {
   sqlCmd = sqlCmd + "     and a.card_no in ('5241701000930447','5245167700332886','5477670100368888','5560486810776197','4003538601975475','4003538601975475') ";
  }

  setString( 1, parm.it1Type);
  setString( 2, parm.comsumeType);
  setString( 3, parm.comsumeType);
  setString( 4, parm.consumeBl);
  setString( 5, parm.consumeCa);
  setString( 6, parm.consumeIt);
  setString( 7, parm.consumeAo);
  setString( 8, parm.consumeId);
  setString( 9, parm.consumeOt);
  setDouble(10, parm.currMinAmt);
  setString(11, parm.acctMonthS);
  setString(12, parm.acctMonthE);
  
 showLogMessage("I", "", "  Bill PAR="+parm.it1Type+","+parm.comsumeType+","+parm.consumeBl+
                    ","+parm.consumeCa+","+parm.consumeIt+","+parm.consumeAo+","+parm.consumeId+
     ","+parm.consumeOt+","+parm.currMinAmt+","+parm.acctMonthS+","+parm.acctMonthE+","+parm.projCode);
          
  if (parm.detlList.size() > 0 ) {
      // 特殊消費 MCC-Code
      sqlCmd  = sqlCmd + "    and ( 1=2 "
              + "        or exists ( select 1 from cms_right_parm_detl x "
              + "             where table_id  = 'RIGHT' "
              + "               and item_no   = ?    "
              + "               and data_type = '01' "
              + "               and proj_code = ? "
              + "               and data_code = decode(a.acct_type,'','x',a.acct_type) ) "
              + "        or exists ( select 1 from cms_right_parm_detl y "
              + "             where table_id   = 'RIGHT' "
              + "               and item_no    = ?    "
              + "               and data_type in ('02','03') "
              + "               and proj_code  = ? "
              + "               and data_code  = decode(a.group_code, '', 'x', a.group_code) "
              + "               and data_code2 = decode(a.card_type , '', 'x', a.card_type) ) "
              + "        or exists ( select 1 from cms_right_parm_detl z "
              + "             where table_id   = 'RIGHT' "
              + "               and item_no    = ?    "
              + "               and data_type  = '06' "
              + "               and proj_code  = ? "
              + "               and data_code  = decode(a.group_code, '', 'x', a.group_code) "
              + "               and data_code2 = decode(a.card_type , '', 'x', a.card_type) "
              + "               and data_code3 = decode(a.mcht_category, '', 'x', a.mcht_category) ) "
              + "        )";
  setString(13, ItemNo);
  setString(14, parm.projCode);
  setString(15, ItemNo);
  setString(16, parm.projCode);
  setString(17, ItemNo);
  setString(18, parm.projCode);
        }
  
  sqlCmd += " order by a.purchase_date ";
  
  String sqlCmd_tmp1 = sqlCmd;

//selectTable();
  int cursorIndex0 = openCursor();

if(DEBUG==1) showLogMessage("I", "", "  Bill open end !");

  // 查詢CMS_RIGHT_PARAM_DETL無數據，無需比對CARD_NO各項值
  double destAmtSum      = 0d;
  double maxDestAmt      = -1d;
  int    bilCurrTotCnt   = 0;
  String maxPurchaseDate = "";
  boolean destAmtGtCurrMinAmt = false;
if(DEBUG_F==1) showLogMessage("I","","  ** Min="+parm.currMinAmt + ","+sqlNrow);
  while (fetchTable()) {
      billCnt++;
      double destAmt = getValueDouble("db_amt");
      int dbCnt      = getValueInt("db_cnt");
      destAmtSum    += destAmt;
      totCnt        += dbCnt;
      bilCurrTotCnt += dbCnt;
      maxDestAmt = Math.max(destAmt, maxDestAmt);
      if (destAmt > 0) {
          maxPurchaseDate = getValue("purchase_date");
      }
      if (destAmt >= parm.currMinAmt) {
          destAmtGtCurrMinAmt = true;
      }
  }
  closeCursor(cursorIndex0);
if(DEBUG==1) showLogMessage("I","","  -- Amt[" + destAmtGtCurrMinAmt+"]"+parm.currAmtCond+","+destAmtSum+","+parm.currAmt+", CNT="+billCnt);

  sqlCmd = sqlCmd_tmp1;
  setString( 1, parm.it1Type);
  setString( 2, parm.comsumeType);
  setString( 3, parm.comsumeType);
  setString( 4, parm.consumeBl);
  setString( 5, parm.consumeCa);
  setString( 6, parm.consumeIt);
  setString( 7, parm.consumeAo);
  setString( 8, parm.consumeId);
  setString( 9, parm.consumeOt);
  setDouble(10, parm.currMinAmt);
  setString(11, parm.acctMonthS);
  setString(12, parm.acctMonthE);
  if (parm.detlList.size() > 0 ) {
      setString(13, ItemNo);
      setString(14, parm.projCode);
      setString(15, ItemNo);
      setString(16, parm.projCode);
      setString(17, ItemNo);
      setString(18, parm.projCode);
  }
if(DEBUG==1) showLogMessage("I", "", "  Bill open 2   !");
  int cursorIndex = openCursor();

  while (fetchTable()) {
      double destAmt = getValueDouble("db_amt");
      int    dbCnt   = getValueInt("db_cnt");
//    String purchaseDate = getValue("purchase_date");
      curCnt++;
      if (curCnt % 100000 == 0 || curCnt == 1)
          showLogMessage("I", "", String.format("Data Process record=[%d]\n", curCnt));

      if (destAmtGtCurrMinAmt) {
                // 如果最大的一筆消費大於currMinAmt則必定符合
                if ((eqIgno(parm.currAmtCond, "Y") && (destAmtSum >= parm.currAmt)) ||
                    (eqIgno(parm.currCntCond, "Y") && totCnt >= parm.currTotCnt)) {
                    BillInfo billInfo = null;
                    String key = "";
                    if ("1".equals(parm.consumeType)) {
                        key = getValue("major_id_p_seqno");
                    }
                    if ("2".equals(parm.consumeType)) {
                        key = getValue("id_p_seqno");
                    }
                    if ("3".equals(parm.consumeType)) {
                        key = getValue("card_no");
                    }
                    if ("0".equals(parm.consumeType)) { 
                        key = getValue("id_p_seqno");
                    }
                    billInfo = parm.billInfoMap.get(key);
                    if (billInfo == null) {
                        billInfo = new BillInfo();
                        parm.billInfoMap.put(key, billInfo);
                        putBil++;
if(DEBUG_F==1) showLogMessage("I","","  888 bil put=["+key+"]"+billInfo.bilCardNo+","+parm.consumeType);
                    }
                    
                    if ("3".equals(parm.consumeType)) { 
                        billInfo.bilCardNo = getValue("card_no");
                    }
                    // 如果是id_p_seqno，存一筆,但cardNo要存入最後消費日的卡號,且消費金額是大於0的那一筆
                    if (!"3".equals(parm.consumeType) && getValueDouble("db_amt") > 0) { 
                        billInfo.bilCardNo = getValue("card_no");
                    }
                    CardNo = billInfo.bilCardNo;
                    billInfo.bilCurrTotCnt   += dbCnt;
                    billInfo.bilPurchaseDate  = maxPurchaseDate;
                    billInfo.chiName          = getValue("chi_name");
                    billInfo.cellarPhone      = getValue("cellar_phone");
                    billInfo.crdAcctType      = getValue("acct_type");
                    billInfo.crdIdPSeqno      = getValue("id_p_seqno");
                    billInfo.crdMajorIdPSeqno = getValue("major_id_p_seqno");
                    billInfo.crdMajorId       = getValue("major_id");
                    billInfo.crdPSeqno        = getValue("p_seqno");
                    billInfo.crdGroupCode     = getValue("group_code");
                    billInfo.crdAcctType      = getValue("acct_type");
                    billInfo.crdCardType      = getValue("card_type");
                    billInfo.crdCurrentCode   = getValue("current_code");
                    billInfo.crdOldCardNo     = getValue("old_card_no");
                    billInfo.crdMajorCardNo   = getValue("major_card_no");
                    billInfo.destAmtSum      += destAmt;
                    billInfo.freeCntBasic     = parm.currCnt;
                    // consumeType=0,FREE_CNT_BASIC=1,各項金額還是要算出來
                    if("0".equals(parm.consumeType)) { 
                       billInfo.freeCntBasic = parm.consume00Cnt;
                    }
                    billInfo.freeCntRaise    = 0;
                    billInfo.idNo            = getValue("id_no");
                    billInfo.maxDestAmt      = Math.max(destAmt, billInfo.maxDestAmt);
if(DEBUG_F==1) showLogMessage("I","","   888 bil no="+billInfo.bilCardNo+","+getValue("id_no"));
                }
            }
        }
     closeCursor(cursorIndex);
}
/*******************************************************************/
// 一般消費
private void selectDbbBill(CmsRightParm parm) throws Exception 
{
if(TEST_0 ==1)  {parm.acctMonthS = "202106"; parm.acctMonthE = "202106";}

  sqlCmd = " select 1 as db_cnt, "
         + "    decode(a.acct_code,'IT', decode(?,'2', decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt), 0),decode(b.refund_apr_flag,'Y',0,a.dest_amt)), "
         + "          case when a.txn_code in ('06','25','27','28','29') "
         + "               then a.dest_amt*-1 else a.dest_amt end) db_amt "
         + ", c.card_no,c.group_code,c.p_seqno  ,c.acct_type,c.major_id_p_seqno,c.id_p_seqno"
         + ", c.major_card_no,c.old_card_no,c.current_code "
         + ", a.mcht_category,a.purchase_date,a.dest_amt "
         + ", d.id_no, d.chi_name, d.cellar_phone, s.id_no as major_id "
         + "  from dbb_bill a "
         + "  left join bil_contract b on b.contract_no  = decode(a.contract_no,'','x',a.contract_no) "
         +                          " and b.contract_seq_no = a.contract_seq_no "
         + "  left join dbc_card c     on a.card_no    = c.card_no  "
         + "  left join dbc_idno d     on d.id_p_seqno = c.id_p_seqno  "
         + "  left join dbc_idno s     on s.id_p_seqno = c.major_id_p_seqno  "
         + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',c.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
         + "       and c.card_type = a.card_type "
         + "       and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                // 消費資料 六大本金類
         + "       and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
         +                                                          " decode( ?,'Y','IT','XX'), "
         +                                                          " decode( ?,'Y','ID','XX'), "
         +                                                          " decode( ?,'Y','CA','XX'), "
         +                                                          " decode( ?,'Y','AO','XX'), "
         +                                                          " decode( ?,'Y','OT','XX')) "
                // 消費資料 最低單筆金額
         + "       and (( a.txn_code in ('06', '25', '27', '28', '29') ) "
         + "              or ( a.txn_code not in ('06', '25', '27', '28', '29') and a.dest_amt >= ? )) "
                // 消費資料 消費期間
         + "       and a.acct_month  between ? and ? ";
if(TEST_0==1)  // 202106
  {
   sqlCmd = sqlCmd + "     and a.card_no in ('4213330005200361','4213330005200437','4213330005200593','4213330005214966','4213330005219569','4213330005221425') ";
  }

  setString( 1, parm.it1Type);
  setString( 2, parm.comsumeType);
  setString( 3, parm.comsumeType);
  setString( 4, parm.consumeBl);
  setString( 5, parm.consumeCa);
  setString( 6, parm.consumeIt);
  setString( 7, parm.consumeAo);
  setString( 8, parm.consumeId);
  setString( 9, parm.consumeOt);
  setDouble(10, parm.currMinAmt);
  setString(11, parm.acctMonthS);
  setString(12, parm.acctMonthE);
  
  if (parm.detlList.size() > 0 ) {
      // 特殊消費 MCC-Code
      sqlCmd  = sqlCmd + "    and ( 1=2 "
              + "        or exists ( select 1 from cms_right_parm_detl x "
              + "             where table_id  = 'RIGHT' "
              + "               and item_no   = ?    "
              + "               and data_type = '01' "
              + "               and proj_code = ? "
              + "               and data_code = decode(a.acct_type,'','x',a.acct_type) ) "
              + "        or exists ( select 1 from cms_right_parm_detl y "
              + "             where table_id   = 'RIGHT' "
              + "               and item_no    = ?    "
              + "               and data_type in ('02','03') "
              + "               and proj_code  = ? "
              + "               and data_code  = decode(a.group_code, '', 'x', a.group_code) "
              + "               and data_code2 = decode(a.card_type , '', 'x', a.card_type) ) "
              + "        or exists ( select 1 from cms_right_parm_detl z "
              + "             where table_id   = 'RIGHT' "
              + "               and item_no    = ?    "
              + "               and data_type  = '06' "
              + "               and proj_code  = ? "
              + "               and data_code  = decode(a.group_code, '', 'x', a.group_code) "
              + "               and data_code2 = decode(a.card_type , '', 'x', a.card_type) "
              + "               and data_code3 = decode(a.mcht_category, '', 'x', a.mcht_category) ) "
              + "        )";
  setString(13, ItemNo);
  setString(14, parm.projCode);
  setString(15, ItemNo);
  setString(16, parm.projCode);
  setString(17, ItemNo);
  setString(18, parm.projCode);
        }
  
  sqlCmd += " order by a.purchase_date ";
  
  String sqlCmd_tmp2 = sqlCmd;

if(DEBUG==1) showLogMessage("I", "", "  DBill open 1  !");
  int cursorIndex = openCursor();
if(DEBUG==1) showLogMessage("I", "", "  DBill open 1 END  !");


  // 查詢CMS_RIGHT_PARAM_DETL無數據，無需比對CARD_NO各項值
  double destAmtSum      = 0d;
  double maxDestAmt      = -1d;
  int    bilCurrTotCnt   = 0;
  String maxPurchaseDate = "";
  boolean destAmtGtCurrMinAmt = false;
if(DEBUG==1) showLogMessage("I", "", "  Dbb_Bill cnt=["+sqlNrow+"] Found="+notFound);
  while (fetchTable()) {
      double destAmt = getValueDouble("db_amt");
      int dbCnt      = getValueInt("db_cnt"   );
      destAmtSum    += destAmt;
      totCnt        += dbCnt;
      bilCurrTotCnt += dbCnt;
      maxDestAmt = Math.max(destAmt, maxDestAmt);
      if (destAmt > 0) {
          maxPurchaseDate = getValue("purchase_date");
      }
      if (destAmt >= parm.currMinAmt) {
          destAmtGtCurrMinAmt = true;
      }
  }
  closeCursor(cursorIndex);

  sqlCmd = sqlCmd_tmp2;
  setString( 1, parm.it1Type);
  setString( 2, parm.comsumeType);
  setString( 3, parm.comsumeType);
  setString( 4, parm.consumeBl);
  setString( 5, parm.consumeCa);
  setString( 6, parm.consumeIt);
  setString( 7, parm.consumeAo);
  setString( 8, parm.consumeId);
  setString( 9, parm.consumeOt);
  setDouble(10, parm.currMinAmt);
  setString(11, parm.acctMonthS);
  setString(12, parm.acctMonthE);
  if (parm.detlList.size() > 0 ) {
      setString(13, ItemNo);
      setString(14, parm.projCode);
      setString(15, ItemNo);
      setString(16, parm.projCode);
      setString(17, ItemNo);
      setString(18, parm.projCode);
  }
if(DEBUG==1) showLogMessage("I", "", "  DBill open 2   !");
  int cursorIndex0 = openCursor();
if(DEBUG==1) showLogMessage("I", "", "  DBill open 2  end !");

  curCnt=0;
  BillInfo billInfo = null;
  while (fetchTable()) {
      double destAmt      = getValueDouble("db_amt");
      int    dbCnt        = getValueInt("db_cnt");
//    String purchaseDate = getValue("purchase_date");
      curCnt++;
      if (curCnt % 100000 == 0 || curCnt == 1)
          showLogMessage("I", "", String.format("Data Process record=[%d]\n", curCnt));
      if (destAmtGtCurrMinAmt) {
                // 如果最大的一筆消費大於currMinAmt則必定符合
                if ((eqIgno(parm.currAmtCond, "Y") && (destAmtSum >= parm.currAmt)) ||
                    (eqIgno(parm.currCntCond, "Y") && totCnt >= parm.currTotCnt)) {
                 // BillInfo billInfo = null;
                    String key = "";
                    if ("1".equals(parm.consumeType)) {
                        key = getValue("major_id_p_seqno");
                    }
                    if ("2".equals(parm.consumeType)) {
                        key = getValue("id_p_seqno");
                    }
                    if ("3".equals(parm.consumeType)) {
                        key = getValue("card_no");
                    }
                    if ("0".equals(parm.consumeType)) { 
                        key = getValue("id_p_seqno");
                    }
                    billInfo = parm.billInfoMap.get(key);
                    if (billInfo == null) {
                        billInfo = new BillInfo();
                        parm.billInfoMap.put(key, billInfo);
                        putDbb++;
if(DEBUG==1) showLogMessage("I","","   888 dbb put=["+key+"]"+billInfo.bilCardNo+","+parm.consumeType);
                    }
                    
                    if ("3".equals(parm.consumeType)) { 
                        billInfo.bilCardNo = getValue("card_no");
                    }
                    // 如果是id_p_seqno，存一筆,但cardNo要存入最後消費日的卡號,且消費金額是大於0的那一筆
                    if (!"3".equals(parm.consumeType) && getValueDouble("db_amt") > 0) { 
                        billInfo.bilCardNo = getValue("card_no");
                    }
                    billInfo.bilCurrTotCnt   += dbCnt;
                    billInfo.bilPurchaseDate  = maxPurchaseDate;
                    billInfo.chiName          = getValue("chi_name");
                    billInfo.cellarPhone      = getValue("cellar_phone");
                    billInfo.crdIdPSeqno      = getValue("id_p_seqno");
                    billInfo.crdMajorIdPSeqno = getValue("major_id_p_seqno");
                    billInfo.crdMajorId       = getValue("major_id");
                    billInfo.crdPSeqno        = getValue("p_seqno");
                    billInfo.crdGroupCode     = getValue("group_code");
                    billInfo.crdAcctType      = getValue("acct_type");
                    billInfo.crdCardType      = getValue("card_type");
                    billInfo.crdCurrentCode   = getValue("current_code");
                    billInfo.crdOldCardNo     = getValue("old_card_no");
                    billInfo.crdMajorCardNo   = getValue("major_card_no");
                    billInfo.destAmtSum      += destAmt;
                    billInfo.freeCntBasic     = parm.currCnt;
                    // consumeType=0,FREE_CNT_BASIC=1,各項金額還是要算出來
                    if("0".equals(parm.consumeType)) { 
                       billInfo.freeCntBasic = parm.consume00Cnt;
                    }
                    billInfo.freeCntRaise    = 0;
                    billInfo.idNo            = getValue("id_no");
                    billInfo.maxDestAmt      = Math.max(destAmt, billInfo.maxDestAmt);
                }
            }
        }
     closeCursor(cursorIndex0);
}
/*******************************************************************/
private void insertCmsRightYearDtl(CmsRightParm parm,int idx) throws Exception 
{

  for (BillInfo billInfo : parm.billInfoMap.values()) {
      String buf = "";

      if(billInfo.bilCardNo.length() < 16)  continue;
      if(billInfo.idNo.length()      < 10)  continue;

      initData();

      selectCycAfee();
      selectMktPostConsume();
      selectBilMchtApplyTmp();

      CardNo        = billInfo.bilCardNo;
      IdPseqNo      = billInfo.crdIdPSeqno;
      ProjCode      = parm.projCode;
      setValue("acct_month"            , busiMonth);
      setValue("id_p_seqno"            , billInfo.crdIdPSeqno);
      setValue("card_no"               , billInfo.bilCardNo);
      setValue("item_no"               , ItemNo);                    //道路救援
      setValue("proj_code"             , parm.projCode);             //適用專案
      setValueInt("use_cnt"            , UseCnt);                    //INTEGER (4,0)   已使用次數 來自請款檔(實際使用)
//    setValueDouble("free_per_amt     , 0         );                //DECIMAL (12,0)  每次折抵金額
      setValueDouble("curr_month_amt"  , purchAmt);                  //DECIMAL (12,0)  消費金額
      setValueDouble("curr_month_cnt"  , purchCnt);                  //DECIMAL (12,0)  消費次數
      setValueDouble("rcv_annual_fee"  , rcvAnnualFee);              //DECIMAL (10,2)  應收年費
//    setValueDouble("platform_kind_amt,                );
//    setValueDouble("platform_kind_cnt,                );           //DECIMAL (12,0)  排除一般消費次數
//    setValueInt("used_next_cnt       ,                );           //INTEGER (4,0)   預支使用次數
//    setValueInt("gift_cnt            ,                );           //INTEGER (4,0)   加贈次數
//    setValueInt("bonus_cnt           ,                );           //INTEGER (4,0)   紅利兌換贈送次數
      setValue("crt_date"              , sysDate);  
      setValue("crt_user"              , hModUser); 
//    setValueDouble("cal_seqno        ,             );              //DECIMAL (10,0)  權益流水號
      setValue("acct_type"             , billInfo.crdAcctType);      //VARCHAR (2,0)   帳戶類別
      setValue("card_type"             , billInfo.crdCardType);      //VARCHAR (2,0)   卡片種類
      setValue("group_code"            , billInfo.crdGroupCode);     //VARCHAR (4,0)   團體代號
      setValue("free_type"             , "2");                       //VARCHAR (1,0)   免費次數類別
      FreeCntBasic  = parm.currCnt;
      if("0".equals(parm.consumeType)) FreeCntBasic = parm.consume00Cnt;
      setValueInt("free_cnt"           , FreeCntBasic);              //INTEGER (4,0)   免費次數
      setValue("match_flag"            , "N");                       //VARCHAR (1,0)   符合優惠註記
      setValue("mod_pgm"               , javaProgram);           
      setValue("mod_time"              ,  sysDate + sysTime);    

      daoTable = "cms_right_year_dtl";
      
      int rtn = insertTable();
      if(rtn > 0) {
         okCnt++;
      }
      
if(DEBUG_F==1) showLogMessage("I", "", " ** insert =[" + billInfo.bilCardNo + "]"+billInfo.idNo+","+billInfo.bilCardNo.length()+", OK="+okCnt+", RTN="+rtn);
      if (dupRecord.equals("Y")) {
  //      comcr.errRtn("insert cms_ticket_card  error!", billInfo.bilCardNo, parm.projCode);
          showLogMessage("I", "", "    ** insert dupRecord 1=["+billInfo.bilCardNo+"]"+parm.projCode);
      }
      else {
            insertCmsRightYear();
            int tCnt = parm.consumeType.equals("0") ? parm.consume00Cnt : parm.currCnt;
            buf = String.format("A,%6.6s,%4.4s,%6.6s,%01d", billInfo.bilCardNo.substring(10,16),
                   billInfo.idNo.substring( 6,10), busiDate.substring(0,6) , tCnt );
            lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));
      }
  }
}
/*******************************************************************/
private void insertCmsRightYear() throws Exception 
{
      setValue("acct_year"             , busiMonth.substring(0,4));
      setValue("id_p_seqno"            , IdPseqNo);
      setValue("card_no"               , CardNo);
      setValue("item_no"               , ItemNo); 
      setValue("proj_code"             , ProjCode);
      setValueInt("use_cnt"            , UseCnt);              //INTEGER (4,0)   已使用次數 來自請款檔(實際使用)
//    setValueDouble("free_per_amt     , 0         );          //DECIMAL (12,0)  每次折抵金額
      setValueDouble("curr_year_consume", purchAmt);           //DECIMAL (12,0)  消費金額
      setValueDouble("curr_year_cnt"   , purchCnt);            //DECIMAL (12,0)  消費次數
      setValueDouble("rcv_annual_fee"  , rcvAnnualFee);        //DECIMAL (10,2)  應收年費
//    setValueDouble("platform_kind_amt,                );
//    setValueDouble("platform_kind_cnt,                );     //DECIMAL (12,0)  排除一般消費次數
//    setValueInt("used_next_cnt       ,                );     //INTEGER (4,0)   預支使用次數
//    setValueInt("gift_cnt            ,                );     //INTEGER (4,0)   加贈次數
//    setValueInt("bonus_cnt           ,                );     //INTEGER (4,0)   紅利兌換贈送次數
      setValue("crt_date"              , sysDate);  
      setValue("crt_user"              , hModUser); 
//    setValueDouble("cal_seqno        ,         );            //DECIMAL (10,0)  權益流水號
      setValue("acct_type"             , AcctType);            //VARCHAR (2,0)   帳戶類別
      setValue("card_type"             , CardType);            //VARCHAR (2,0)   卡片種類
      setValue("group_code"            , GroupCode);           //VARCHAR (4,0)   團體代號
      setValue("free_type"             , "2");                 //VARCHAR (1,0)   免費次數類別
      setValueInt("free_cnt"           , FreeCntBasic);        //INTEGER (4,0)   免費次數
      setValue("match_flag"            , "N");                 //VARCHAR (1,0)   符合優惠註記
      setValue("mod_pgm"               , javaProgram);           
      setValue("mod_time"              ,  sysDate + sysTime);    

      daoTable = "cms_right_year";
      
      int rtn = insertTable();
      if(rtn > 0) {
         okCnt++;
      }
      
      if (dupRecord.equals("Y")) {
          updateCmsRightYear();
      }
}
/*******************************************************************/
private void updateCmsRightYear() throws Exception 
{
    updateSQL  = "  curr_year_consume = curr_year_consume  + ? "
               + ", curr_year_cnt     = curr_year_cnt  + ? "
               + ", rcv_annual_fee    = rcv_annual_fee + ? ";
    daoTable   = "cms_right_year ";
    whereStr   = "WHERE acct_year  = ? "
               + "  and id_p_seqno = ? "
               + "  and card_no    = ? "
               + "  and item_no    = ? "
               + "  and proj_code  = ? ";
    int idx_f = 1;
    setDouble(idx_f++, purchAmt);
    setDouble(idx_f++, purchCnt);
    setDouble(idx_f++, rcvAnnualFee);
    setString(idx_f++, busiMonth.substring(0,4) );
    setString(idx_f++, IdPseqNo     );
    setString(idx_f++, CardNo       );
    setString(idx_f++, ItemNo       );
    setString(idx_f++, ProjCode     );
    int recCnt = updateTable();

    if (notFound.equals("Y")) {
        comcr.errRtn("update cms_right_year error!", CardNo+","+ProjCode , hCallBatchSeqno);
    }

    return;

}
/*******************************************************************/
private void selectBilMchtApplyTmp() throws Exception 
{
  sqlCmd = " select count(*) as appl_cnt "
         + "   from bil_mcht_apply_tmp "
         + "  where card_no   = ? "
         + "    and file_type = '05' "
         + "    and crt_date between ? and ? ";

  setString(1 , CardNo);
  setString(2 , busiMonth+"01");
  setString(3 , busiMonth+"31");

  selectTable();
  if (notFound.equals("Y")) {
//    commCrd.errExit("select_ptr_businday not found!", "");
  }
  UseCnt  =colInt("appl_cnt");

}
/*******************************************************************/
    class CmsRightParm {
        String projCode = "";
        String it1Type = "";
        String comsumeType = "";
        String consumeBl = "";
        String consumeCa = "";
        String consumeIt = "";
        String consumeAo = "";
        String consumeId = "";
        String consumeOt = "";
        String currCond = "";
        double currMinAmt = 0;
        String currPreMonth = "";
        double currAmt = 0;
        String currAmtCond = "";
        int    currCnt = 0;
        String currCntCond = "";
        int    currTotCnt = 0;
        int    consume00Cnt;
        String airCond = "";
        String airSupFlag0 = "";
        String airSupFlag1 = "";
        String airDay = "";
        String airAmtType = "";
        String aAirRight  = "";
        double airAmt = 0;
        int    airCnt = 0;
        String lastMm = "";
        String chooseCond = "";
        String consumeType = "";
        List<CmsRightParmDetl> detlList;
        
        // acct month
        String acctMonthE = "";
        String acctMonthS = "";
        String specAcctMonthE = "";
        String specAcctMonthS = "";
        
        // result
        Map<String, BillInfo> billInfoMap = new HashMap<>();
    }
    class CmsRightParmDetl {
        String dataTypeTmp;
        String dataCodeTmp;
        String dataCode2Tmp;
        String dataCode3Tmp;
    }
    class BillInfo {
        int    freeCntBasic = 0;
        int    freeCntRaise = 0;
        String bilCardNo = "";
        String crdGroupCode = "";
        String crdAcctType  = "";
        String crdCardType  = "";
        String crdMajorCardNo = "";
        String crdCurrentCode = "";
        String crdOldCardNo;
        String bilPurchaseDate = "";
        String crdIdPSeqno = "";
        String crdPSeqno   = "";
        double maxDestAmt  = 0d;
        double destAmtSum  = 0d;
        int    bilCurrTotCnt = 0;
        String chiName = "";
        String cellarPhone = "";
        String idNo = "";
        String crdMajorIdPSeqno = "";
        String crdMajorId       = "";
    }
}
/*******************************************************************/
