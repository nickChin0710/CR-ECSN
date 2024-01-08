/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
* DATE      Version     AUTHOR              DESCRIPTION                       *
* --------- ----------  ----------- ----------------------------------------- *
* 112/06/01 V1.01.01    Lai         Program initial                           *
* 112/07/11 V1.01.02    Lai         alter open cursor                         *
* 112/08/17 V1.02.03    Lai         error                                     *
* 112/08/30 V1.02.04    Lai         add 一般消費 check cms_right_parm_detl    *
* 112/09/11 V1.02.05    Lai         modify input parm  by acct_month          *
* 112/11/09 V1.02.06    kirin       fix use_month                             *
* 112/11/22 V1.00.07    Kirin       移除 comc.errExit                          *
* 112/12/28 V1.00.08    Ryan        busiMonth, 拿掉-1  ,移除1日執行檢核             *
* 112/12/29 V1.00.09    Kirin       符合參數金額才insert名單                        *    
******************************************************************************/
package Cms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.BaseBatch;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.*;

public class CmsC010 extends BaseBatch {
    private final String PROGNAME = "影城名單程式   112/12/28 V1.00.09";
    CommFunction   comm    = new CommFunction();
    CommCrd        comc    = new CommCrd();
    CommCrdRoutine comcr;
    
    int     DEBUG   = 0;
    int     DEBUG_F = 0;
    int     TEST_0  = 0;

    String prgmId   = "CmsC010";
    String rptName  = "影城名單";
    String rptId    = "A006_MOVIENETOUT";
    int    rptSeq   = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String stderr    = "";

    private String busiDate    = "";
    private String busiMonth   = "";
    private String busiUseMonth= "";
    private String busiPreYear = "";
    private int    totCnt      = 0;
    private int    okCnt       = 0;
    private int    dtlCnt      = 0;
    private int    curCnt      = 0;
    private int    billCnt     = 0;
    private int    billCnt1    = 0;

    String maxPurDate = "";

/*******************************************************************/
@Override
protected void dataProcess(String[] args) throws Exception {
}
/*******************************************************************/
public static void main(String[] args) throws Exception {
        CmsC010 proc = new CmsC010();
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
                //comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            	showLogMessage("Error!! Someone is running this program now!!!", "", "Please wait a moment to run again!!");
        		return 0; 
            }
            
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallRProgramCode = javaProgram;
            comcr.callbatch(0, 0, 0);
            
            if (args.length >  1) {
                //comc.errExit(String.format("Usage : CmsC010 [BUSINESS_DATE](計算當月)"),"");
                showLogMessage("Usage : CmsC010 [BUSINESS_DATE](計算當月)", "", "");
        		return 0;    
            }
            if (args.length == 1) {
                busiDate     = args[0];
//                busiMonth    = commDate.dateAdd(busiDate, 0,-1, 0).substring(0, 6);
                busiMonth = busiDate.substring(0, 6);
                showLogMessage("I", "", String.format("輸入參數營業日[%s]", busiDate));
            } else {
                selectPtrBusinday();
            }
            busiUseMonth = busiDate.substring(0, 6);
            busiPreYear  = commDate.dateAdd(busiMonth, -1, 0, 0).substring(0, 4);

            showLogMessage("I","","程式營業日參數="+busiDate+" Acct_mon="+busiMonth+" Use_mon="+busiUseMonth);
            showLogMessage("I","", String.format("執行日期-Sysdate=[%s]", sysDate));
            
//            if (!busiDate.substring(6).equals("01")) {
//               // comc.errExit(String.format("本程式只在每月1日執行[%s]", busiDate), "");
//                showLogMessage("I", "", "本程式只在每月1日執行, 本日非執行日!! process end...." );
//                return 0;
//            }
  //          busiUseMonth = commDate.dateAdd(busiDate, 0, 1, 0).substring(0, 6);

            selectSQL = "  to_char(((to_date(?,'yyyymmdd')) - (dayofweek(to_date(?,'yyyymmdd'))-6)  days),'yyyymmdd') week_date_s "
                      + ", to_char(((to_date(?,'yyyymmdd')) - (dayofweek(to_date(?,'yyyymmdd'))-12) days),'yyyymmdd') week_date_e ";
            daoTable  = "dual  ";
            setString(1 , busiDate);
            setString(2 , busiDate);
            setString(3 , busiDate);
            setString(4 , busiDate);

            int recordCnt = selectTable();

     //     int cnt = selectMktTicketCard();
     //     if (cnt > 0) comc.errExit("本月己產製名單,不可重覆執行", "");

            // 刪除前24個月的數據
            deleteMktTicketCard();

            selectCmsRightParm(busiDate);

            String filename = String.format("%s/media/cms/%s",comc.getECSHOME(),rptId);
            showLogMessage("I","", "Open file="+filename);
            comc.writeReport(filename, lpar1);
          //comcr.insertPtrBatchRpt(lpar1);

            ftpRtn(rptId);

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
      //    showLogMessage("I", "", "執行結束");
            comcr.hCallErrorDesc = "程式執行結束，檔案筆數[" + curCnt + "]";
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
//         + "      , to_char(add_months(to_date(business_date,'yyyymmdd'), -1),'yyyymm') h_prev_month " 
 		   + "      , left(business_date,6) h_prev_month " 
		   + " from ptr_businday";

  selectTable();
  if (notFound.equals("Y")) {
      comc.errExit("select_ptr_businday not found!", "");
  }
  busiDate  = getValue("business_date");
  busiMonth = getValue("h_prev_month");
}
/*******************************************************************/
void selectMaxPurDate(String iCardNo, String iMon1, String iMon2, String iPar1, String iPar2, String iPar3, String iPar4, String iPar5, String iPar6) throws Exception 
{
  sqlCmd = " select max(purchase_date) as max_purchase_date "
         + "      , sum(case when a.txn_code in ('06','25','27','28','29') "
         + "                 then a.dest_amt*-1 else a.dest_amt end) db_amt "
         + "      , count(*)  as db_cnt  "
         + "   from bil_bill a "
         + "  where a.acct_month between ? and ? "
         + "    and a.card_no    = ? "
         + "    and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
         +                                                       " decode( ?,'Y','IT','XX'), "
         +                                                       " decode( ?,'Y','ID','XX'), "
         +                                                       " decode( ?,'Y','CA','XX'), "
         +                                                       " decode( ?,'Y','AO','XX'), "
         +                                                       " decode( ?,'Y','OT','XX')) ";

  setString( 1, iMon1);
  setString( 2, iMon2);
  setString( 3, iCardNo);
  setString( 4, iPar1);
  setString( 5, iPar2);
  setString( 6, iPar3);
  setString( 7, iPar4);
  setString( 8, iPar5);
  setString( 9, iPar6);
  
  selectTable();
  if (notFound.equals("Y")) {
      comc.errExit("selectR bil max purchase_date not found!", iCardNo);
  }
  maxPurDate = getValue("max_purchase_date");

if(DEBUG_F==1)  showLogMessage("I", "", "     select max=["+iCardNo+"]"+iMon1+","+iMon2+","+maxPurDate);
}
/*******************************************************************/
private int selectMktTicketCard() throws Exception 
{

  StringBuilder sb = new StringBuilder(1024);
  sb.append(" SELECT * ");
  sb.append("   FROM mkt_ticket_card ");
  sb.append(" WHERE (SEND_DATE IS NULL OR SEND_DATE = '') ");
  sb.append("   AND ACCT_MONTH = ? "); // 上個月
  sb.append("   AND USE_MONTH <= ? "); // business_date的年月 or 傳入YYYYMM

  sqlCmd = sb.toString();
  setString(1, busiMonth); // USE_MONTH
  setString(2, busiMonth); // USE_MONTH

  return selectTable();
}
/*******************************************************************/
// 刪除24個月前的數據
void deleteMktTicketCard() throws Exception 
{
  String date = commDate.dateAdd(busiDate, 0, -24, 0);
  showLogMessage("I", "", " 前24個月=[" + date + "]");

  showLogMessage("I", "", "======= DELETE AIR 前24個月=[" + date + "]");
  sqlCmd = " delete mkt_ticket_card " + " where CRT_DATE <= ? and item_no = '12' ";
  ppp(1, date);

  sqlExec(sqlCmd);

  showLogMessage("I", "", "======= DELETE AIR curr = [" + busiMonth + "]");
  sqlCmd = " delete mkt_ticket_card   where acct_month like ? || '%' and item_no = '12' ";
 // ppp(1, sysDate.substring(0, 6));
  ppp(1, busiMonth);
  sqlExec(sqlCmd);

}
/*******************************************************************/
// 2.6. 讀取【cms_right_parm 卡友權益資格參數主檔】
// where條件active_status=Y and apr_flag=Y and ITEM_NO=12 and介於權益專案生效日起迄(proj_date_s、proj_date_e)
private List<CmsRightParm> selectCmsRightParm(String date) throws Exception 
{
  sqlCmd  = " select * from cms_right_parm ";
  sqlCmd += "  where active_status = 'Y' and apr_flag = 'Y' and item_no = '12' ";
  sqlCmd += "    and decode(proj_date_s,'','20100101',proj_date_s) <= ? ";
  sqlCmd += "    and decode(proj_date_e,'','30000101',proj_date_e) >= ? ";
  ppp(1, date);
  ppp(2, date);
  sqlSelect();
  int ilSelectRow = sqlNrow;
  List<CmsRightParm> parmlist = new ArrayList<>();
  if (sqlNrow <= 0) {
      comc.errExit("select cms_right_parm not found!", "");
      errExit(1);
  }
if(DEBUG==1) showLogMessage("I", "", " Read cnt=[" + ilSelectRow + "]");
  for (int ii = 0; ii < ilSelectRow; ii++) {
      CmsRightParm cmsRightParm = new CmsRightParm();
      parmlist.add(cmsRightParm);
      cmsRightParm.projCode     = colSs(ii, "proj_code");
      cmsRightParm.it1Type      = colSs(ii, "it_1_type");
      cmsRightParm.comsumeType  = colSs(ii, "consume_type");
      cmsRightParm.consumeBl    = colSs(ii, "consume_bl");
      cmsRightParm.consumeCa    = colSs(ii, "consume_ca");
      cmsRightParm.consumeIt    = colSs(ii, "consume_it");
      cmsRightParm.consumeAo    = colSs(ii, "consume_ao");
      cmsRightParm.consumeId    = colSs(ii, "consume_id");
      cmsRightParm.consumeOt    = colSs(ii, "consume_ot");
      cmsRightParm.currCond     = colSs(ii, "curr_cond");
      cmsRightParm.currMinAmt   = colNum(ii, "curr_min_amt");
      cmsRightParm.currPreMonth = colSs(ii, "curr_pre_month");
      cmsRightParm.currAmt      = colNum(ii, "curr_amt");
      cmsRightParm.currAmtCond  = colSs(ii, "curr_amt_cond");
      cmsRightParm.currCnt      = colInt(ii, "curr_cnt");
      cmsRightParm.currCntCond  = colSs(ii, "curr_cnt_cond");
      cmsRightParm.currTotCnt   = colInt(ii, "curr_tot_cnt");
      cmsRightParm.consume00Cnt = colInt(ii, "consume_00_cnt");
      cmsRightParm.airCond      = colSs(ii, "air_cond");
      cmsRightParm.airSupFlag0  = colSs(ii, "air_sup_flag0");
      cmsRightParm.airSupFlag1  = colSs(ii, "air_sup_flag1");
      cmsRightParm.airDay       = colSs(ii, "air_day");
      cmsRightParm.airAmtType   = colSs(ii, "air_amt_type");
      cmsRightParm.aAirRight    = colSs(ii, "a_air_right");
      cmsRightParm.airAmt       = colNum(ii, "air_amt");
      cmsRightParm.airCnt       = colInt(ii, "air_cnt");
      cmsRightParm.lastMm       = colSs(ii, "last_mm");
      cmsRightParm.chooseCond   = colSs(ii, "choose_cond");
      cmsRightParm.consumeType  = colSs(ii, "consume_type");
      cmsRightParm.groupCardFlag  = colSs(ii, "group_card_flag");
      cmsRightParm.acctTypeFlag   = colSs(ii, "acct_type_flag");
      cmsRightParm.debutGroupCond = colSs(ii, "debut_group_cond");

 showLogMessage("I","","Get PARM=["+cmsRightParm.projCode +"] AIR="+ cmsRightParm.airCond+","+cmsRightParm.currCond+","+colSs(ii, "group_card_flag")+","+colSs(ii, "acct_type_flag")+","+colSs(ii, "debut_group_cond"));

      // 取得【cms_right_parm】join【cms_right_parm_detl】資料
      cmsRightParm.detlList = selectCmsRightParmDetl(cmsRightParm.projCode);
      if (dtlCnt < 1 ) continue;

      // 取得一般消費期間與特殊消費期間
      getAcctMonth(cmsRightParm);
      // 取得符合條件卡號及計算消費、存入DB、寫入檔案
//    getCardNo();
      selectBilBill(cmsRightParm);
      insertMktTicketCard(cmsRightParm , 1);

      showLogMessage("I","", String.format(" Credit 筆數  tot_count=[%d],insert_count=[%d]",totCnt,okCnt));

      totCnt=0; okCnt=0;
      BillInfo billInfo = null;
      selectDbbBill(cmsRightParm);
      insertMktTicketCard(cmsRightParm , 2);

      showLogMessage("I","", String.format(" Vd     筆數  tot_count=[%d],insert_count=[%d]",totCnt,okCnt));
  }
  
  return parmlist;
}
/*******************************************************************/
private List<CmsRightParmDetl> selectCmsRightParmDetl(String projCode) throws Exception 
{
  sqlCmd = " select table_id, proj_code, item_no, apr_flag, data_type, data_code, data_code2, data_code3 "
    + "  from cms_right_parm_detl "
    + " where item_no   = '12' "
    + "   and table_id  = 'RIGHT' "
    + "   and proj_code = ? ";
  setString(1, projCode);
  sqlSelect();
  dtlCnt        = sqlNrow;
  int selectRow = sqlNrow;
  showLogMessage("I","","  DTL cnt="+projCode+"["+dtlCnt+"]");
  List<CmsRightParmDetl> detlList = new ArrayList<>();
  if (sqlNrow <= 0) {
//    comc.errExit("select cms_right_parm_detl not found!", projCode);
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
// 一般消費
private void selectBilBill(CmsRightParm parm) throws Exception 
{
if(TEST_0 ==1)  {parm.acctMonthS = "202106"; parm.acctMonthE = "202106";}

  sqlCmd = " select 1 as db_cnt, "
         + "    decode(a.acct_code,'IT', decode(?,'2', decode(a.install_curr_term,1,decode(b.refund_apr_flag,'Y',0,b.tot_amt), 0),decode(b.refund_apr_flag,'Y',0,a.dest_amt)), "
         + "          case when a.txn_code in ('06','25','27','28','29') "
         + "               then a.dest_amt*-1 else a.dest_amt end) db_amt "
         + ", c.card_no,c.group_code,c.p_seqno  ,c.acct_type,c.major_id_p_seqno,c.id_p_seqno"
         + ", c.major_card_no,c.old_card_no,c.current_code "
         + ", a.ecs_cus_mcht_no,a.mcht_category,a.purchase_date,a.dest_amt "
         + ", d.id_no, d.chi_name, d.birthday , s.id_no as major_id "
         + "  from bil_bill a "
         + "  left join bil_contract b on b.contract_no  = decode(a.contract_no,'','x',a.contract_no) "
         +                          " and b.contract_seq_no = a.contract_seq_no "
         + "  left join crd_card c     on a.card_no    = c.card_no  "
         + "  left join crd_idno d     on d.id_p_seqno = c.id_p_seqno  "
         + "  left join crd_idno s     on s.id_p_seqno = c.major_id_p_seqno  "
         + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
         + "       and c.current_code = '0'      "
         + "       and c.card_type    = a.card_type "
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

  int idx_f = 1;
  setString(idx_f++, parm.it1Type);
  setString(idx_f++, parm.comsumeType);
  setString(idx_f++, parm.comsumeType);
  setString(idx_f++, parm.consumeBl);
  setString(idx_f++, parm.consumeCa);
  setString(idx_f++, parm.consumeIt);
  setString(idx_f++, parm.consumeAo);
  setString(idx_f++, parm.consumeId);
  setString(idx_f++, parm.consumeOt);
  setDouble(idx_f++, parm.currMinAmt);
  setString(idx_f++, parm.acctMonthS);
  setString(idx_f++, parm.acctMonthE);
  
 showLogMessage("I", "", "  Bill PAR="+parm.it1Type+","+parm.comsumeType+","+parm.consumeBl+
                    ","+parm.consumeCa+","+parm.consumeIt+","+parm.consumeAo+","+parm.consumeId+
     ","+parm.consumeOt+","+parm.currMinAmt+","+parm.acctMonthS+","+parm.acctMonthE+","+parm.projCode);
          
  if (parm.detlList.size() > 0 ) {
      // 特殊消費 MCC-Code
       sqlCmd += "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id  = 'RIGHT' "
                                          + "   and item_no   = '12' "
                                          + "   and data_type = '01' "
                                          + "   and proj_code = ? "
                                          + "   and data_code = a.acct_type )) )"
                + "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '12' "
                                          + "   and data_type  = '02' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and data_code2 = a.card_type )) )"
                + "    and ( '0' = ? or ('1' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '09' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type ) )"
                                 + " or ('2' = ? and not exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '12' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type ) ))"
                + "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '12' "
                                          + "   and data_type  = '06' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and data_code2 = a.card_type     "
                                          + "   and data_code3 = decode(a.mcht_category,'','x',a.mcht_category) "
                                          + "   and data_code3 in (select v.mcc_group from cms_mcc_group v where v.mcc_code = a.mcht_category )) )"

                + " )";
        setString(idx_f++, parm.acctTypeFlag);
        setString(idx_f++, parm.acctTypeFlag);
        setString(idx_f++, parm.projCode);
        setString(idx_f++, parm.groupCardFlag);
        setString(idx_f++, parm.groupCardFlag);
        setString(idx_f++, parm.projCode);
        setString(idx_f++, parm.debutGroupCond);
        setString(idx_f++, parm.debutGroupCond);
        setString(idx_f++, parm.projCode);
        setString(idx_f++, parm.debutGroupCond);
        setString(idx_f++, parm.projCode);
        setString(idx_f++, parm.airCond);
        setString(idx_f++, parm.airCond);
        setString(idx_f++, parm.projCode);
        }
  
  sqlCmd += " order by a.card_no,a.purchase_date ";
  
// showLogMessage("I", "", "  BIL  SQL=["+sqlCmd+"] Code="+parm.projCode);

  String sqlCmd_tmp1 = sqlCmd;

   int cursorIndex0 =  openCursor();

if(DEBUG==1) showLogMessage("I", "", "  Bill open 1 end !");

  // 查詢CMS_RIGHT_PARAM_DETL無數據，無需比對CARD_NO各項值
  double destAmtSum      = 0d;
  double maxDestAmt      = -1d;
  int    bilCurrTotCnt   = 0;
  String maxPurchaseDate = "";
  boolean destAmtGtCurrMinAmt = false;
  double destAmt = 0;
  while (fetchTable()) {
      billCnt++;
      destAmt        = getValueDouble("db_amt");
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
if(DEBUG_F==1) showLogMessage("I","","   bil card=["+getValue("card_no")+"]"+getValue("purchase_date")+","+maxPurchaseDate+","+destAmt+","+billCnt);
  }
  closeCursor(cursorIndex0);

if(DEBUG==1) showLogMessage("I","","  -- Amt[" + destAmtGtCurrMinAmt+"]"+destAmt+",min="+parm.currMinAmt+","+parm.currAmtCond+","+destAmtSum+","+parm.currAmt+", CNT="+billCnt+",Pur="+maxPurchaseDate);

  sqlCmd = sqlCmd_tmp1;
  int idx_g = 1;
  setString(idx_g++, parm.it1Type);
  setString(idx_g++, parm.comsumeType);
  setString(idx_g++, parm.comsumeType);
  setString(idx_g++, parm.consumeBl);
  setString(idx_g++, parm.consumeCa);
  setString(idx_g++, parm.consumeIt);
  setString(idx_g++, parm.consumeAo);
  setString(idx_g++, parm.consumeId);
  setString(idx_g++, parm.consumeOt);
  setDouble(idx_g++, parm.currMinAmt);
  setString(idx_g++, parm.acctMonthS);
  setString(idx_g++, parm.acctMonthE);
  if (parm.detlList.size() > 0 ) {
        setString(idx_g++, parm.acctTypeFlag);
        setString(idx_g++, parm.acctTypeFlag);
        setString(idx_g++, parm.projCode);
        setString(idx_g++, parm.groupCardFlag);
        setString(idx_g++, parm.groupCardFlag);
        setString(idx_g++, parm.projCode);
        setString(idx_g++, parm.debutGroupCond);
        setString(idx_g++, parm.debutGroupCond);
        setString(idx_g++, parm.projCode);
        setString(idx_g++, parm.debutGroupCond);
        setString(idx_g++, parm.projCode);
        setString(idx_g++, parm.airCond);
        setString(idx_g++, parm.airCond);
        setString(idx_g++, parm.projCode);
  }
if(DEBUG==1) showLogMessage("I", "", "  Bill open 2   !");

  int cursorIndex = openCursor();
if(DEBUG==1) showLogMessage("I", "", "  Bill open 2 End  !");

  while (fetchTable()) {
      destAmt        = getValueDouble("db_amt");
      int    dbCnt   = getValueInt("db_cnt");
//    String purchaseDate = getValue("purchase_date");
      curCnt++;
if(DEBUG_F==1) showLogMessage("I","","   bil read 2="+getValue("card_no")+","+getValue("purchase_date")+","+curCnt);
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
                    }
                    
                    if ("3".equals(parm.consumeType)) { 
                        billInfo.bilCardNo = getValue("card_no");
                    }
                    // 如果是id_p_seqno，存一筆,但cardNo要存入最後消費日的卡號,且消費金額是大於0的那一筆
                    if (!"3".equals(parm.consumeType) && getValueDouble("db_amt") > 0) { 
                        billInfo.bilCardNo = getValue("card_no");
                    }

                    String iMon1 = parm.acctMonthS;
                    String iMon2 = parm.acctMonthE;
                    String iPar1 = parm.consumeBl;
                    String iPar2 = parm.consumeCa;
                    String iPar3 = parm.consumeIt;
                    String iPar4 = parm.consumeAo;
                    String iPar5 = parm.consumeId;
                    String iPar6 = parm.consumeOt;
                    selectMaxPurDate(getValue("card_no"),iMon1,iMon2,iPar1,iPar2,iPar3,iPar4,iPar5,iPar6);
                    billInfo.bilCurrTotCnt   += dbCnt;
                    billInfo.bilPurchaseDate  = maxPurDate; 
                    billInfo.chiName          = getValue("chi_name");
                    billInfo.birthday         = getValue("birthday");
                    billInfo.crdAcctType      = getValue("acct_type");
                    billInfo.crdIdPSeqno      = getValue("id_p_seqno");
                    billInfo.crdMajorIdPSeqno = getValue("major_id_p_seqno");
                    billInfo.crdMajorId       = getValue("major_id");
                    billInfo.crdPSeqno        = getValue("p_seqno");
                    billInfo.crdGroupCode     = getValue("group_code");
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
if(DEBUG_F==1) showLogMessage("I","","   bil put end="+billInfo.bilCardNo+",PUR="+maxPurDate);
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
         + ", d.id_no, d.chi_name, d.birthday    , s.id_no as major_id "
         + "  from dbb_bill a "
         + "  left join bil_contract b on b.contract_no  = decode(a.contract_no,'','x',a.contract_no) "
         +                          " and b.contract_seq_no = a.contract_seq_no "
         + "  left join dbc_card c     on a.card_no    = c.card_no  "
         + "  left join dbc_idno d     on d.id_p_seqno = c.id_p_seqno  "
         + "  left join dbc_idno s     on s.id_p_seqno = c.major_id_p_seqno  "
         + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',c.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
         + "       and c.current_code = '0'      "
         + "       and c.card_type    = a.card_type "
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
              + "               and item_no   = '12' "
              + "               and data_type = '01' "
              + "               and proj_code = ? "
              + "               and data_code = decode(a.acct_type,'','x',a.acct_type) ) "
              + "        or exists ( select 1 from cms_right_parm_detl y "
              + "             where table_id   = 'RIGHT' "
              + "               and item_no    = '12' "
              + "               and data_type in ('02','03') "
              + "               and proj_code  = ? "
              + "               and data_code  = decode(a.group_code, '', 'x', a.group_code) "
              + "               and data_code2 = decode(a.card_type , '', 'x', a.card_type) ) "
              + "        or exists ( select 1 from cms_right_parm_detl z "
              + "             where table_id   = 'RIGHT' "
              + "               and item_no    = '12' "
              + "               and data_type  = '06' "
              + "               and proj_code  = ? "
              + "               and data_code  = decode(a.group_code, '', 'x', a.group_code) "
              + "               and data_code2 = decode(a.card_type , '', 'x', a.card_type) "
              + "               and data_code3 in (select v.mcc_group from cms_mcc_group v where v.mcc_code = a.mcht_category) ) "
              + "        )";
  setString(13, parm.projCode);
  setString(14, parm.projCode);
  setString(15, parm.projCode);
        }
  
  sqlCmd += " order by a.card_no,a.purchase_date ";
  
// showLogMessage("I", "", "  DBB  SQL=["+sqlCmd+"] Found="+notFound);

 String sqlCmd_tmp2 = sqlCmd;

if(DEBUG==1) showLogMessage("I", "", "  DBill open 1  !");

  int cursorIndex = openCursor();
if(DEBUG==1) showLogMessage("I", "", "  DBill open 1 End=["+sqlNrow+"]");

  // 查詢CMS_RIGHT_PARAM_DETL無數據，無需比對CARD_NO各項值
  double destAmtSum      = 0d;
  double maxDestAmt      = -1d;
  int    bilCurrTotCnt   = 0;
  String maxPurchaseDate = "";
  boolean destAmtGtCurrMinAmt = false;
//for (int i = 0; i < sqlNrow; i++) {
  while (fetchTable()) {
      billCnt1++;
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
      setString(13, parm.projCode);
      setString(14, parm.projCode);
      setString(15, parm.projCode);
  }
if(DEBUG==1) showLogMessage("I", "", "  DBill open 2   !");
  int cursorIndex0 = openCursor();
if(DEBUG==1) showLogMessage("I", "", "  DBill open 2 end ["+destAmtGtCurrMinAmt+"]"+destAmtSum+","+parm.currAmt);
  curCnt=0;
  BillInfo billInfo = null;
//for (int i = 0; i < sqlNrow; i++) {
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
if(DEBUG_F==1) showLogMessage("I","","   888 dbb put=["+key+"]"+billInfo.bilCardNo+","+parm.consumeType);
                    }
                    
                    if ("3".equals(parm.consumeType)) { 
                        billInfo.bilCardNo = getValue("card_no");
                    }
                    // 如果是id_p_seqno，存一筆,但cardNo要存入最後消費日的卡號,且消費金額是大於0的那一筆
                    if (!"3".equals(parm.consumeType) && getValueDouble("db_amt") > 0) { 
                        billInfo.bilCardNo = getValue("card_no");
                    }
                    
                    String iMon1 = parm.acctMonthS;
                    String iMon2 = parm.acctMonthE;
                    String iPar1 = parm.consumeBl;
                    String iPar2 = parm.consumeCa;
                    String iPar3 = parm.consumeIt;
                    String iPar4 = parm.consumeAo;
                    String iPar5 = parm.consumeId;
                    String iPar6 = parm.consumeOt;
                    selectMaxPurDate(getValue("card_no"),iMon1,iMon2,iPar1,iPar2,iPar3,iPar4,iPar5,iPar6);
                    billInfo.bilCurrTotCnt   += dbCnt;
                    billInfo.bilPurchaseDate  = maxPurDate;
                    billInfo.chiName          = getValue("chi_name");
                    billInfo.birthday         = getValue("birthday");
                    billInfo.crdAcctType      = getValue("acct_type");
                    billInfo.crdIdPSeqno      = getValue("id_p_seqno");
                    billInfo.crdMajorIdPSeqno = getValue("major_id_p_seqno");
                    billInfo.crdMajorId       = getValue("major_id");
                    billInfo.crdPSeqno        = getValue("p_seqno");
                    billInfo.crdGroupCode     = getValue("group_code");
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
private void insertMktTicketCard(CmsRightParm parm,int idx) throws Exception 
{

  for (BillInfo billInfo : parm.billInfoMap.values()) {
      String buf = "";

if(DEBUG_F==1) showLogMessage("I", "", " ** insert =[" + billInfo.bilCardNo + "]"+billInfo.idNo+","+billInfo.bilPurchaseDate);
      if(billInfo.bilCardNo.length() < 16)  continue;
      if(billInfo.idNo.length()      < 10)  continue;
      if(billInfo.destAmtSum < parm.currAmt )  continue;
if(DEBUG_F==1)  showLogMessage("billInfo.destAmtSum ", ":", billInfo.destAmtSum  + " parm.currAmtd=" + parm.currAmt);
      
      daoTable = "mkt_ticket_card";
      setValue("debt_flag"       , idx == 1  ? "N" : "Y" );
      setValue("crt_date"        , sysDate);
      setValue("send_date"       , sysDate);
      setValue("week_date_s"     , getValue("week_date_s"));
      setValue("week_date_e"     , getValue("week_date_e"));
      setValue("card_no"         , billInfo.bilCardNo);   //bil_bill 最後一筆消費日期的CARD_NO,且金額>0
      setValue("card_no_6"       , billInfo.bilCardNo.substring(10,16));  
      setValue("id_p_seqno"      , billInfo.crdIdPSeqno); //CRD_CARD.ID_P_SEQ
      setValue("acct_type"       , billInfo.crdAcctType); //CRD_CARD.acct_type   
      setValue("p_seqno"         , billInfo.crdPSeqno);   //CRD_CARD.ID_P_SEQ
      setValue("vd_used_cnt"     , "0");                  
      setValue("mod_time"        , sysDate + sysTime);    // 系統日期 
      setValue("mod_pgm"         , javaProgram); //javaProgram

      setValue("item_no"         , "12");
      setValue("id_no"           , billInfo.idNo);             // 【CRD_IDNO】ID_NO
      setValue("major_card_no"   , billInfo.crdMajorCardNo);   // 【crd_card】MAJOR_CARD_NO
      setValue("major_id_p_seqno", billInfo.crdMajorIdPSeqno); //CRD_CARD.MAJOR_ID_P_SEQ
      setValue("major_id"        , billInfo.crdMajorId);       // 【CRD_IDNO】ID_NO
      setValue("mod_type"        , "A");
      setValue("acct_month"      , busiMonth);                 // 【bil_bill】ACCT_MONTH起迄值的迄值
      setValue("curr_month"      , busiMonth);                 // 【bil_bill】ACCT_MONTH起迄值的迄值
      setValue("use_month"       , busiUseMonth);              // business_date年月(YYYYMM)
//    setValue("old_card_no"     , ""); // OLD_CARD_NO:不用填
      setValue("current_code"    , billInfo.crdCurrentCode);   //【crd_card】CURRENT_CODE
      setValue("purchase_date"   , billInfo.bilPurchaseDate);  //【bil_bill】PURCHASE_DATE最後(新)一筆的消費日期 ,且金額>0 max(purchase_date)
      setValue("data_from"       , "2");                       //固定寫入2
      setValue("chi_name"        , billInfo.chiName);          //【CRD_IDNO】 CHI_NAME
      setValueDouble("curr_max_amt", billInfo.maxDestAmt);     //max(BIL_BILL.DEST_AMT)-寫入單筆最大金額
      setValueInt("curr_tot_cnt"   , billInfo.bilCurrTotCnt);  //BIL_BILL符合條件的總筆數
      setValue("proj_code "      , parm.projCode);             //CMS_RIGHT_PARM.PROJ_CODE
      setValue("consume_type"    , parm.consumeType);          //取自【cms_right_parm】CONSUME_TYPE
      setValue("group_code"      , billInfo.crdGroupCode);     //【CRD_CARD】 GROUP_CODE
           
      setValueInt("free_cnt_basic", billInfo.freeCntBasic); //如果CONSUME_TYPE<>0參考第2.10.4項. 如果CONSUME_TYPE=0,【CMS_RIGHT_PARM】CONSUME_00_CNT的值
      setValueInt("free_cnt_raise", billInfo.freeCntRaise); //FREE_CNT_basic
      setValueInt("free_tot_cnt"  , billInfo.freeCntBasic + billInfo.freeCntRaise); //FREE_CNT_basic+ FREE_CNT_raise
      setValueDouble("tot_amt"    , billInfo.destAmtSum);   //BIL_BILL的sum(db_amt)金額  
      
      insertTable();
      okCnt++;
      
      if (dupRecord.equals("Y")) {
  //      comcr.errRtn("insert cms_ticket_card  error!", billInfo.bilCardNo, parm.projCode);
          showLogMessage("I", "", " ** insert dupRecord [" + billInfo.bilCardNo + "]"+billInfo.idNo+","+busiMonth);
      }
      else {
            int tCnt = parm.consumeType.equals("0") ? parm.consume00Cnt : parm.currCnt;
            buf = String.format("A,%7.7s,%8.8s,%6.6s,%01d", comc.getSubString(billInfo.idNo,3,10), 
                   billInfo.birthday , busiUseMonth , tCnt );
            lpar1.add(comcr.putReport(rptId, rptName, sysDate, ++rptSeq, "0", buf+"\r"));
      }
/*
2.15.1.	交易別： A 新增； D 刪除； R 補換發卡
2.15.2.	卡號末六碼
2.15.3.	持卡人 ID 末四碼
2.15.4.	可使用年月 YYYYMM (寫入USE_MONTH)
2.15.5.	可使用次數
*/
  }
}
/***********************************************************************/
private void ftpRtn(String hFileNameI) throws Exception {
    int    errCode  = 0;
    String temstr1  = "";
    String temstr2  = "";
    String procCode = "";

    // 這個檔不需要壓縮

    CommFTP       commFTP = new CommFTP(getDBconnect()    , getDBalias());
    CommRoutine      comr = new CommRoutine(getDBconnect(), getDBalias());

    String  hEflgRefIpCode  = "NCR2TCB";
    commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");
    commFTP.hEflgSystemId   = "NCR2TCB";
    commFTP.hEflgGroupId    = "0000";
    commFTP.hEflgSourceFrom = "EcsFtpBil";
    commFTP.hEflgModPgm     = this.getClass().getName();
    commFTP.hEriaLocalDir   = String.format("%s/media/cms", comc.getECSHOME());
    System.setProperty("user.dir", commFTP.hEriaLocalDir);
    procCode  = String.format("put %s", hFileNameI);
    showLogMessage("I", "", procCode + ", " + hEflgRefIpCode + " 開始上傳....");
    errCode   = commFTP.ftplogName(hEflgRefIpCode, procCode);
    if (errCode != 0) {
      stderr = String.format("ftp_rtn=[%s]傳檔錯誤 err_code[%d]\n", procCode, errCode);
      showLogMessage("I", "", stderr);
    }
    else
    {
     backFile(hFileNameI);
    }
}
/***************************************************************************/
void backFile(String filename) throws Exception {
   String tmpstr1 = String.format("%s/media/cms/%s", comc.getECSHOME(), filename);
   String tmpstr2 = String.format("%s/media/cms/backup/%s_%s",comc.getECSHOME(),filename,sysDate);

   if (comc.fileRename(tmpstr1, tmpstr2) == false) {
       showLogMessage("I", "", "ERROR : 檔案["+tmpstr1+" to "+tmpstr2+"]備份失敗!");
       return;
   }

   comc.fileDelete(tmpstr1);
   showLogMessage("I", "", "檔案 [" +tmpstr1 + "] 已移至 [" + tmpstr2 + "]");
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
        String groupCardFlag = "";
        String acctTypeFlag  = "";
        String debutGroupCond = "";

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
        String crdMajorCardNo = "";
        String crdCurrentCode = "";
        String crdOldCardNo;
        String bilPurchaseDate = "";
        String crdIdPSeqno = "";
        String crdAcctType = "";
        String crdPSeqno   = "";
        double maxDestAmt  = 0d;
        double destAmtSum  = 0d;
        int    bilCurrTotCnt = 0;
        String chiName = "";
        String birthday = "";
        String idNo = "";
        String crdMajorIdPSeqno = "";
        String crdMajorId       = "";
    }
}
/*******************************************************************/
