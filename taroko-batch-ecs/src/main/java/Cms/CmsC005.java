/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*   DATE    Version       AUTHOR                 DESCRIPTION                  *
* --------- ---------   ----------- ----------------------------------------- *
* 112/03/30 V1.00.00    Zuwei Su    Program initial                           *
* 112/05/31 V1.00.00    Kirin       change item_no=15                            *
* 112/06/28 V1.00.02    Zuwei Su    一般計算區間邏輯調整，是否判斷data_type=02,03的值必須group_card_flag=Y，data_type=06暫時不過濾，一般消費特殊消費分開處理，處理邏輯調整            *
* 112/06/30 V1.00.03    Zuwei Su    setBillInfo cardNo和idNo參數傳入，一般消費執行加入currCond=Y判斷*
* 112/07/21 V1.01.01    Lai         modify insert 調整                                *
* 112/08/14 V1.01.02    Lai         modify 2.	同一月份,可重覆執行-先delete               *
* 112/08/30 V1.01.03    Lai         add 一般消費 check cms_right_parm_detl             * 
* 112/09/05 V1.01.04    Lai         add showMessage                                  * 
* 112/09/11 V1.01.05    Lai         modify input parm by acct_month                  *
* 112/11/08 V1.01.06    Lai         modify chooseCond "2" -> acctMonthS              *
* 112/11/16 V1.00.07    Kirin       showmess call CmsRightParm                       *    
* 112/11/22 V1.00.08    Kirin       非指定執行日return 0 ,移除 comc.errExit & 非sysDate    *
* 112/12/28 V1.00.09    Ryan        busiMonth,acctMonthE 拿掉-1                         *
**************************************************************************************/
package Cms;

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

public class CmsC005 extends BaseBatch {
    private final String PROGNAME = "高鐵升等產生名單程式   112/12/28 V1.00.09";
    CommFunction    comm = new CommFunction();
    CommCrd      commCrd = new CommCrd();
    CommCrdRoutine comcr;
    
    int    DEBUG   = 0;
    int    DEBUG_F = 0;
    int  DEBUG_DAT = 0;

    private String busiDate = "";
    private String busiMonth = "";
    private String busiUseMonth = "";
    private String busiPreYear = "";
    private int totCnt = 0;
    private int totCnt9= 0;
    private int totCnt8= 0;
    private int okCnt = 0;

    String maxPurDate = "";
/*******************************************************************/
    @Override
    protected void dataProcess(String[] args) throws Exception {
    }

    public static void main(String[] args) throws Exception {
        CmsC005 proc = new CmsC005();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }

    @Override
    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
                //commCrd.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            	showLogMessage("I", "Error!! Someone is running this program now!!!", "Please wait a moment to run again!!  process end...." );
        		return 0;
            }
            
//          if (!sysDate.endsWith("01")) {
//             commCrd.errExit(String.format("本程式只在每月4日執行[%s]", sysDate), "");
//          }
            printf("Usage : CmsC005 [BUSINESS_DATE] [計算當月(YYYYMM)]");
            
            if (!connectDataBase()) {
                commCrd.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallRProgramCode = javaProgram;
            comcr.callbatch(0, 0, 0);
            
            if (args.length == 1) {
                busiDate = args[0];
//                busiMonth = commDate.dateAdd(busiDate, 0,-1, 0).substring(0, 6);
                busiMonth = busiDate.substring(0, 6);
            } else {
                selectPtrBusinday();
            }
            
          if (!busiDate.endsWith("01")) {
             //commCrd.errExit(String.format("本程式只在每月4日執行[%s]", sysDate), "");
     	     showLogMessage("I", "", "本程式只在每月1日執行, 本日非執行日!! process end....營業日=" + busiDate);
   		    return 0;
          }
            
            busiUseMonth = busiDate.substring(0, 6);
            busiPreYear  = commDate.dateAdd(busiMonth, -1, 0, 0).substring(0, 4);

            showLogMessage("I","","程式參數="+busiDate+" Acct_mon="+busiMonth+" Use_mon="+busiUseMonth);
            showLogMessage("I", "", String.format("執行日期[%s]", sysDate));
            showLogMessage("I","", "CmsC005 call CmsRightParm ["+ busiDate +"] initialization success.....");
            
            // 刪除前13個月的數據
            deleteMktThsrUpgradeList();

            int cnt = selectMktThsrUpgradeList();
            if (cnt > 0) {
                //commCrd.errExit("本月己產製名單,不可重覆執行", "");
                showLogMessage("I", "", "本月己產製名單,不可重覆執行" );
        		 return 0;
            }

            selectCmsRightParm(busiDate);

//            outPutTextFile();
//
//            if (!lpar1.isEmpty()) {
//                comc.writeReport(outFileName, lpar1, "MS950");
//                lpar1.clear();
//            }

            showLogMessage("I", "", String.format("筆數  tot_count=[ %d ],ok_count=[ %d ]", totCnt, okCnt));

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
            showLogMessage("I", "", "執行結束 ="+totCnt9+","+totCnt8);
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    protected void selectPtrBusinday() throws Exception {
        busiDate = "";
        sqlCmd = " select business_date, " +
//                " to_char(add_months(to_date(business_date, 'yyyymmdd'), -1), 'yyyymm') as h_prev_month " +
				" left(business_date,6) as h_prev_month " +
                " from ptr_businday";

        selectTable();
        if (notFound.equals("Y")) {
            //commCrd.errExit("select_ptr_businday not found!", "");
            showLogMessage("I", "", "select_ptr_businday not found!" );
            return ;
        }
        busiDate = getValue("business_date");
        busiMonth = getValue("h_prev_month");
    }

    private int selectMktThsrUpgradeList() throws Exception {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(" SELECT ");
        sb.append("     * ");
        sb.append(" FROM ");
        sb.append("     MKT_THSR_UPGRADE_LIST ");
        sb.append(" WHERE ");
        sb.append("     (SEND_DATE IS NULL OR SEND_DATE = '') ");
        sb.append("     AND ACCT_MONTH = ? "); // 上個月
        sb.append("     AND USE_MONTH <= ? and item_no = '15'"); // business_date的年月 or 傳入YYYYMM

        sqlCmd = sb.toString();
        setString(1, busiMonth); 
        setString(2, busiMonth);
        return selectTable();
    }

/**************************************************************************************/
    // 刪除13個月前的數據
void deleteMktThsrUpgradeList() throws Exception {
        String date = commDate.dateAdd(busiDate, 0, -13, 0);
        sqlCmd = " delete MKT_THSR_UPGRADE_LIST " + " where CRT_DATE <= ? ";
        ppp(1, date);
        sqlExec(sqlCmd);
        if (sqlNrow < 0) {
            errmsg("delete MKT_THSR_UPGRADE_LIST error");
            //errExit(1);
            errExit(0);
        }

  sqlCmd = " delete mkt_thsr_upgrade_list  where acct_month like ? || '%' and item_no = '15' ";
//  ppp(1, sysDate.substring(0, 6));
  ppp(1, busiMonth);
if(DEBUG==1) showLogMessage("I", "", "======= DELETE AIR curr = [" + busiMonth  + "]");
  sqlExec(sqlCmd);
}
/*******************************************************************/
void selectMaxPurDate(String iCardNo, String iMon1, String iMon2, String iPar1, String iPar2, String iPar3, String iPar4, String iPar5, String iPar6) throws Exception
{
  sqlCmd = " select max(purchase_date) as max_purchase_date "
         + "      , sum(case when a.txn_code in ('06','25','27','28','29') "
         + "                 then a.dest_amt*-1 else a.dest_amt end) db_amt_P "
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
      //commCrd.errExit("selectR bil max purchase_date not found!", iCardNo);
  	  showLogMessage("I", "", "selectR bil max purchase_date not found!" + iCardNo );
	  return ;
  }
  maxPurDate = getValue("max_purchase_date");

//if(DEBUG_F==1) showLogMessage("I","","     select max=["+iCardNo+"]"+iMon1+","+iMon2+","+maxPurDate);
}
/**************************************************************************************/
    // 2.6. 讀取【cms_right_parm 卡友權益資格參數主檔】
    // where條件active_status=Y and apr_flag=Y and ITEM_NO=15 and介於權益專案生效日起迄(proj_date_s、proj_date_e)
    private List<CmsRightParm> selectCmsRightParm(String date) throws Exception {
        sqlCmd = " select * from cms_right_parm ";
        sqlCmd += " where active_status = 'Y' and apr_flag = 'Y' and item_no = '15' ";
        sqlCmd += " and decode(proj_date_s,'','20100101',proj_date_s) <= ? ";
        sqlCmd += " and decode(proj_date_e,'','30000101',proj_date_e) >= ? ";
        ppp(1, date);
        ppp(2, date);
        sqlSelect();
        int ilSelectRow = sqlNrow;
        List<CmsRightParm> parmlist = new ArrayList<>();
        if (sqlNrow <= 0) {
            printf("select cms_right_parm not found");
            //errExit(1);
            errExit(0);
        }
        for (int ii = 0; ii < ilSelectRow; ii++) {
            CmsRightParm cmsRightParm = new CmsRightParm();
            parmlist.add(cmsRightParm);
            cmsRightParm.projCode = colSs(ii, "proj_code");
            cmsRightParm.it1Type = colSs(ii, "it_1_type");
            cmsRightParm.comsumeType = colSs(ii, "consume_type");
            cmsRightParm.consumeBl = colSs(ii, "consume_bl");
            cmsRightParm.consumeCa = colSs(ii, "consume_ca");
            cmsRightParm.consumeIt = colSs(ii, "consume_it");
            cmsRightParm.consumeAo = colSs(ii, "consume_ao");
            cmsRightParm.consumeId = colSs(ii, "consume_id");
            cmsRightParm.consumeOt = colSs(ii, "consume_ot");
            cmsRightParm.currCond = colSs(ii, "curr_cond");
            cmsRightParm.currMinAmt = colNum(ii, "curr_min_amt");
            cmsRightParm.currPreMonth = colSs(ii, "curr_pre_month");
            cmsRightParm.currAmt = colNum(ii, "curr_amt");
            cmsRightParm.currAmtCond = colSs(ii, "curr_amt_cond");
            cmsRightParm.currCnt = colInt(ii, "curr_cnt");
            cmsRightParm.currCntCond = colSs(ii, "curr_cnt_cond");
            cmsRightParm.currTotCnt = colInt(ii, "curr_tot_cnt");
            cmsRightParm.consume00Cnt = colInt(ii, "consume_00_cnt");
            cmsRightParm.airCond = colSs(ii, "air_cond");
            cmsRightParm.airSupFlag0 = colSs(ii, "air_sup_flag0");
            cmsRightParm.airSupFlag1 = colSs(ii, "air_sup_flag1");
            cmsRightParm.airDay = colSs(ii, "air_day");
            cmsRightParm.airAmtType = colSs(ii, "air_amt_type");
            cmsRightParm.airAmt = colNum(ii, "air_amt");
            cmsRightParm.airCnt = colInt(ii, "air_cnt");
            cmsRightParm.lastMm = colSs(ii, "last_mm");
            cmsRightParm.chooseCond  = colSs(ii, "choose_cond");
            cmsRightParm.consumeType = colSs(ii, "consume_type");
            cmsRightParm.groupCardFlag  = colSs(ii, "group_card_flag");
            cmsRightParm.acctTypeFlag   = colSs(ii, "acct_type_flag");
            cmsRightParm.debutGroupCond = colSs(ii, "debut_group_cond");
      showLogMessage("I","","Get PARM=["+cmsRightParm.projCode +"] AIR="+ cmsRightParm.airCond+","+cmsRightParm.currCond+",ACCT="+cmsRightParm.acctTypeFlag+","+cmsRightParm.groupCardFlag+","+cmsRightParm.debutGroupCond+",IT="+cmsRightParm.it1Type);
            
            // 取得【cms_right_parm】join【cms_right_parm_detl】資料
            // cmsRightParm.detlList = selectCmsRightParmDetl(cmsRightParm.projCode);
            // 取得一般消費期間與特殊消費期間
            getAcctMonth(cmsRightParm);
            // 取得符合條件卡號及計算消費、存入DB、寫入檔案
            
            // 一般消費
            if (eqIgno(cmsRightParm.currCond, "Y")) {
                selectBilBill(cmsRightParm);
            }
            // 特殊消費
            if (eqIgno(cmsRightParm.airCond, "Y")) {
                   selectBilBill2(cmsRightParm);
            }
            
            insertMktThsrUpgradeList(cmsRightParm);
        }
        
        return parmlist;
    }

    private List<CmsRightParmDetl> selectCmsRightParmDetl(String projCode) throws Exception {
        sqlCmd = " select table_id, item_no, apr_flag, data_type, data_code, data_code2, data_code3 "
                + " from cms_right_parm_detl "
                + " where item_no = '15' "
                + " and table_id = 'RIGHT' "
                + " and proj_code = ? ";
        setString(1, projCode);
        sqlSelect();
        int selectRow = sqlNrow;
        List<CmsRightParmDetl> detlList = new ArrayList<>();
        if (sqlNrow <= 0) {
            printf("select cms_right_parm_detl not found");
        } else {
            for (int ii = 0; ii < selectRow; ii++) {
                CmsRightParmDetl detail = new CmsRightParmDetl();
                detail.dataTypeTmp = colSs(ii, "data_type");
                detail.dataCodeTmp = (colSs(ii, "data_code"));
                detail.dataCode2Tmp = (colSs(ii, "data_code2"));
                detail.dataCode3Tmp = (colSs(ii, "data_code3"));
                detlList.add(detail);
            }
        }
        
        return detlList;
    }

    // 2.8. 取得acct_month起迄值,計算消費期間
    private void getAcctMonth(CmsRightParm parm) throws ParseException {
      showLogMessage("I","","  Get ACCT=["+parm.currCond +"] choose="+ parm.chooseCond+", Pre="+parm.currPreMonth);
        if (eqIgno(parm.currCond, "Y")) {
            if (eqIgno(parm.chooseCond, "1")) {
                if (parm.currPreMonth.length() == 1) {
                    parm.acctMonthS = busiPreYear + "0" + parm.currPreMonth;
                } else if (parm.currPreMonth.length() > 1) {
                    parm.acctMonthS = busiPreYear + parm.currPreMonth;
                } else {
                    parm.acctMonthS = busiPreYear + "01";
                }
//                parm.acctMonthE = busiMonth.substring(0, 4) + "12";
                // acct_month 迄值=上個月(yyyyMM)
//                parm.acctMonthE = commDate.monthAdd(busiMonth, -1);
                    parm.acctMonthE = busiMonth;
            } 
            if (eqIgno(parm.chooseCond, "2")) {
                parm.acctMonthS = commDate.monthAdd(busiUseMonth, -Integer.parseInt(parm.lastMm));
                parm.acctMonthE = parm.acctMonthS;
            }
            if(parm.acctMonthS.substring(4,6).equals("00")) 
               parm.acctMonthS = parm.acctMonthS.substring(0,4)+"01";
if(DEBUG==1) showLogMessage("I","","  Get Month=["+parm.acctMonthS+"]"+parm.acctMonthE+","+parm.currCond+","+parm.chooseCond);
        }

        // 特殊消費區間
        if (eqIgno(parm.airCond, "Y")) {
            parm.specAcctMonthS = commDate.dateAdd(busiDate, 0, 0, -Integer.parseInt(parm.airDay));
            parm.specAcctMonthS = parm.specAcctMonthS.substring(0,6);
            parm.specAcctMonthE = busiMonth;
        }
if(DEBUG==1) showLogMessage("I","","  Get SPEC =["+parm.specAcctMonthS+"]"+parm.specAcctMonthE);

    }

    // 一般消費
    private void selectBilBill(CmsRightParm parm) throws Exception {
        sqlCmd = " select 1 as db_cnt, "
                + "    decode(a.acct_code,'IT', decode(?,'2', decode(a.install_curr_term,1, "
                + "                        decode(b.refund_apr_flag,'Y',0,b.tot_amt), 0), "
                + "                 decode(b.refund_apr_flag,'Y',0,a.dest_amt)), "
                + "          case when a.txn_code in ('06','25','27','28','29') "
                + "               then a.dest_amt*-1 else a.dest_amt end) db_amt "
                + ", c.card_no,c.group_code,c.card_type,c.acct_type,c.major_id_p_seqno,c.id_p_seqno"
                + ",c.major_card_no,c.old_card_no,c.current_code,a.reference_no "
                + ", a.ecs_cus_mcht_no,a.mcht_category,a.purchase_date "
                + ", d.id_no, d.chi_name, d.cellar_phone "
                + "  from bil_bill a "
                + "  left join bil_contract b on b.contract_no = decode(a.contract_no, '', 'x', a.contract_no) and b.contract_seq_no = a.contract_seq_no "
                + "  left join crd_card c on a.card_no = c.card_no  "
                + "  left join crd_idno d on d.id_p_seqno = c.id_p_seqno  "
                + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
                + "   and c.card_type    = a.card_type "
                + "   and c.current_code = '0' "
                + "   and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                // 消費資料 六大本金類
                + "   and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
                +                                                      " decode( ?,'Y','IT','XX'), "
                +                                                      " decode( ?,'Y','ID','XX'), "
                +                                                      " decode( ?,'Y','CA','XX'), "
                +                                                      " decode( ?,'Y','AO','XX'), "
                +                                                      " decode( ?,'Y','OT','XX')) "
                // 消費資料 最低單筆金額
                + "   and (( a.txn_code in ('06', '25', '27', '28', '29') ) or "
                + "        ( a.txn_code not in ('06', '25', '27', '28', '29') and a.dest_amt >= ? )) "
                // 消費資料 消費期間
                + "       and a.acct_month between ? and ? "
                // 排除一般消費 - 排除 ECS_CUS_MCHT_NO 的筆數
                + "   and not exists ( select 1 from mkt_mcht_gp a "
                + "                      left join mkt_mchtgp_data b on a.mcht_group_id = b.data_key "
                + "                     where b.table_name = 'MKT_MCHT_GP' "
                + "                       and b.data_key = 'MKTR00001' "
                + "                       and a.platform_flag = '2' "
                + "                       and b.data_code != '' "
                + "                       and b.data_code = a.ecs_cus_mcht_no )";
        sqlCmd += "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id  = 'RIGHT' "
                                          + "   and item_no   = '15' "
                                          + "   and data_type = '01' "
                                          + "   and proj_code = ? "
                                          + "   and data_code = a.acct_type )) )"
                + "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '15' "
                                          + "   and data_type  = '02' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and data_code2 = a.card_type )) )"
                + "    and ( '0' = ? or ('1' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '15' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type ) )"
                                 + " or ('2' = ? and not exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '15' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type ) ))"
                + " ";
if(DEBUG_DAT == 1) sqlCmd += "     AND  a.group_code = '1620'  and a.card_type = 'VI' and a.card_no in ('5452020006862114')";

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
/* lai
*/

        if ("1".equals(parm.consumeType)) {
            sqlCmd += " order by c.major_id_p_seqno,a.purchase_date ";
        }
        if ("2".equals(parm.consumeType)) {
            sqlCmd += " order by c.id_p_seqno,a.purchase_date ";
        }
        if ("3".equals(parm.consumeType)) {
            sqlCmd += " order by c.card_no,a.purchase_date ";
        }
        if ("0".equals(parm.consumeType)) { 
            sqlCmd += " order by c.id_p_seqno,a.purchase_date ";
        }
        
if(DEBUG==1)   showLogMessage("I","","   Read Bill-1=["+parm.acctTypeFlag+"]"+parm.groupCardFlag+","+parm.projCode+","+parm.debutGroupCond+","+parm.currMinAmt+","+parm.acctMonthS+","+parm.acctMonthE);

        // 查詢CMS_RIGHT_PARAM_DETL無數據，無需比對CARD_NO各項值
        double destAmtSum = 0d;
        double maxDestAmt = 0d;
        int bilCurrTotCnt = 0;
        String maxPurchaseDate = "";
        String crdCardNo = "";
        String crdIdNo = "";
        String chiName = "";
        String cellarPhone = "";
        String crdIdPSeqno = "";
        String crdGroupCode = "";
        String crdCurrentCode = "";
        String crdOldCardNo = "";
        String crdMajorCardNo = "";
        String oldKey = "";
        showLogMessage("I","","  open 1   Mon="+parm.acctMonthS+","+parm.acctMonthE);
        int cursorIndex = openCursor();
        showLogMessage("I","","  open 1 end  ");
        while(fetchTable()) {
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
            totCnt9++;
            if(totCnt9 % 100000 == 0 || totCnt9 == 1)
               showLogMessage("I", "", String.format("Data Process C005-1 record=[%d]\n", totCnt9));

            if (!key.equals(oldKey)) {
                if (!oldKey.equals("")) {
                    setBillInfo(parm, destAmtSum, maxDestAmt, bilCurrTotCnt, maxPurchaseDate,
                            crdCardNo, crdIdNo, chiName, cellarPhone, crdIdPSeqno, crdGroupCode,
                            crdCurrentCode, crdOldCardNo, crdMajorCardNo, oldKey);
                }
                
                destAmtSum = 0;
                maxDestAmt = 0;
                bilCurrTotCnt = 0;
                maxPurchaseDate = "";
                crdCardNo = "";
                crdIdNo = "";
                chiName = "";
                cellarPhone = "";
                crdIdPSeqno = "";
                crdGroupCode = "";
                crdCurrentCode = "";
                crdOldCardNo = "";
                crdMajorCardNo = "";
                oldKey = key;
            }
            
            double destAmt = getValueDouble("db_amt");
            int dbCnt = getValueInt("db_cnt");
            destAmtSum += destAmt;
            maxDestAmt = Math.max(maxDestAmt, destAmt);
            totCnt += dbCnt;
            bilCurrTotCnt += dbCnt;
            if (destAmt > 0) {
                maxPurchaseDate = getValue("purchase_date");
                crdCardNo = getValue("card_no");
                crdIdNo = getValue("id_no");
                chiName = getValue("chi_name");
                cellarPhone = getValue("cellar_phone");
                crdIdPSeqno = getValue("id_p_seqno");
                crdGroupCode = getValue("group_code");
                crdCurrentCode = getValue("current_code");
                crdOldCardNo = getValue("old_card_no");
                crdMajorCardNo = getValue("major_card_no");
if(DEBUG_F==1) showLogMessage("I","","    CARD1=["+ getValue("card_no")+"] G="+getValue("group_code")+","+ getValue("card_type")+","+ getValue("acct_type")+",REF="+getValue("reference_no")+","+getValueDouble("db_amt")+","+destAmt+","+destAmtSum);
            }
        }
        // 處理最後一個Key
        if (!oldKey.equals("")) {
            setBillInfo(parm, destAmtSum, maxDestAmt, bilCurrTotCnt, maxPurchaseDate,
                    crdCardNo, crdIdNo, chiName, cellarPhone, crdIdPSeqno, crdGroupCode,
                    crdCurrentCode, crdOldCardNo, crdMajorCardNo, oldKey);
        }

        closeCursor(cursorIndex);
    }

    private void setBillInfo(CmsRightParm parm, double destAmtSum, double maxDestAmt,
            int bilCurrTotCnt, String maxPurchaseDate, String crdCardNo, String crdIdNo,
            String chiName, String cellarPhone, String crdIdPSeqno, String crdGroupCode,
            String crdCurrentCode, String crdOldCardNo, String crdMajorCardNo, String oldKey)
            throws Exception {
        // 合計金額或合計筆數滿足
        if ((eqIgno(parm.currAmtCond, "Y") && (destAmtSum >= parm.currAmt)) 
                || (eqIgno(parm.currCntCond, "Y") && totCnt >= parm.currTotCnt)) {
            // 保存上一個key對應的對象值
            BillInfo billInfo = parm.billInfoMap.get(oldKey);
            if (billInfo == null) {
                billInfo = new BillInfo();
                parm.billInfoMap.put(oldKey, billInfo);
            }
            
            if ("3".equals(parm.consumeType)) { 
                billInfo.bilCardNo = crdCardNo;
            }
            // 如果是id_p_seqno，存一筆,但cardNo要存入最後消費日的卡號,且消費金額是大於0的那一筆
            if (!"3".equals(parm.consumeType)) { 
                billInfo.bilCardNo = crdCardNo;
            }
            String iMon1 = parm.acctMonthS;
            String iMon2 = parm.acctMonthE;
            String iPar1 = parm.consumeBl;
            String iPar2 = parm.consumeCa;
            String iPar3 = parm.consumeIt;
            String iPar4 = parm.consumeAo;
            String iPar5 = parm.consumeId;
            String iPar6 = parm.consumeOt;
            selectMaxPurDate(crdCardNo,iMon1,iMon2,iPar1,iPar2,iPar3,iPar4,iPar5,iPar6);
            billInfo.bilCurrTotCnt += bilCurrTotCnt;
        //  billInfo.bilPurchaseDate = maxPurchaseDate;
            billInfo.bilPurchaseDate = maxPurDate;
            billInfo.chiName = chiName;
            billInfo.cellarPhone = cellarPhone;
            billInfo.crdIdPSeqno = crdIdPSeqno;
            billInfo.crdGroupCode = crdGroupCode;
            billInfo.crdCurrentCode = crdCurrentCode;
            billInfo.crdOldCardNo = crdOldCardNo;
            billInfo.crdMajorCardNo = crdMajorCardNo;
            billInfo.destAmtSum += destAmtSum;
            billInfo.freeCntBasic = parm.currCnt;
            // consumeType=0,FREE_CNT_BASIC=1,各項金額還是要算出來
            if ("0".equals(parm.consumeType)) { 
                billInfo.freeCntBasic = parm.consume00Cnt;
            }
            billInfo.idNo = crdIdNo;
            billInfo.maxDestAmt = Math.max(maxDestAmt, billInfo.maxDestAmt);
        }
    }

    // 特殊消費
    private void selectBilBill2(CmsRightParm parm) throws Exception 
    {
        if(parm.specAcctMonthS.length() == 0 && parm.specAcctMonthE.length() == 0 ) return;

        sqlCmd = " select 1 as db_cnt "
                + ",  decode(a.acct_code,'IT', decode(?,'2',  decode(a.install_curr_term,1, decode(b.refund_apr_flag,'Y',0,b.tot_amt), 0), decode(b.refund_apr_flag,'Y',0,a.dest_amt)), case when a.txn_code in ('06','25','27','28','29') then a.dest_amt*-1 else a.dest_amt end) db_amt "
                + ", c.card_no,c.group_code,c.card_type,c.acct_type,c.major_id_p_seqno,c.id_p_seqno"
                + ", c.major_card_no,c.old_card_no,c.current_code "
                + ", a.ecs_cus_mcht_no,a.mcht_category,a.purchase_date "
                + ", d.id_no, d.chi_name, d.cellar_phone "
                + "  from bil_bill a "
                + "  left join bil_contract b on b.contract_no = decode(a.contract_no, '', 'x', a.contract_no) and b.contract_seq_no = a.contract_seq_no "
                + "  left join crd_card c on a.card_no = c.card_no  "
                + "  left join crd_idno d on d.id_p_seqno = c.id_p_seqno  "
                + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
                + "       and acct_month between ? and ? "
                + "       and c.card_type = a.card_type "
                + "       and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                + "       and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
                                                                      + " decode( ?,'Y','IT','XX'), "
                                                                      + " decode( ?,'Y','ID','XX'), "
                                                                      + " decode( ?,'Y','CA','XX'), "
                                                                      + " decode( ?,'Y','AO','XX'), "
                                                                      + " decode( ?,'Y','OT','XX')) ";
        int idx_f = 1;
        setString(idx_f++, parm.it1Type);
        setString(idx_f++, parm.comsumeType);
        setString(idx_f++, parm.comsumeType);
        setString(idx_f++, parm.specAcctMonthS);
        setString(idx_f++, parm.specAcctMonthE);
        setString(idx_f++, parm.consumeBl);
        setString(idx_f++, parm.consumeCa);
        setString(idx_f++, parm.consumeIt);
        setString(idx_f++, parm.consumeAo);
        setString(idx_f++, parm.consumeId);
        setString(idx_f++, parm.consumeOt);
        
        // 正卡 / 附卡
        if (eqIgno(parm.airSupFlag0, "Y") || eqIgno(parm.airSupFlag1, "Y")) {
            sqlCmd += "    and (1=2 ";
            if (eqIgno(parm.airSupFlag0, "Y")) {
                sqlCmd += "    or c.SUP_FLAG='0' ";
            }
            if (eqIgno(parm.airSupFlag1, "Y")) {
                sqlCmd += "    or c.SUP_FLAG='1' ";
            }
            sqlCmd += "    ) ";
        }
        // 特殊消費 MCC-Code
        sqlCmd += "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id  = 'RIGHT' "
                                          + "   and item_no   = '15' "
                                          + "   and data_type = '01' "
                                          + "   and proj_code = ? "
                                          + "   and data_code = a.acct_type )) ) "
                + "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '15' "
                                          + "   and data_type  = '02' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and data_code2 = a.card_type )) )"
                + "    and ( '0' = ? or ('1' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '15' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type ))"
                                 + " or ('2' = ? and not exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '15' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type )) )"
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

        if ("1".equals(parm.consumeType)) {
            sqlCmd += " order by c.major_id_p_seqno,a.purchase_date ";
        }
        if ("2".equals(parm.consumeType)) {
            sqlCmd += " order by c.id_p_seqno,a.purchase_date ";
        }
        if ("3".equals(parm.consumeType)) {
            sqlCmd += " order by c.card_no,a.purchase_date ";
        }
        if ("0".equals(parm.consumeType)) { 
            sqlCmd += " order by c.id_p_seqno,a.purchase_date ";
        }

if(DEBUG==1)   showLogMessage("I","","   Read C005-2=["+parm.comsumeType+"]["+parm.specAcctMonthS+"]"+parm.specAcctMonthE);

        // 查詢CMS_RIGHT_PARAM_DETL無數據，無需比對CARD_NO各項值
        double destAmtSum = 0d;
        double maxDestAmt = 0d;
        int bilCurrTotCnt = 0;
        String maxPurchaseDate = "";
        String crdCardNo = "";
        String crdIdNo = "";
        String chiName = "";
        String cellarPhone = "";
        String crdIdPSeqno = "";
        String crdGroupCode = "";
        String crdCurrentCode = "";
        String crdOldCardNo = "";
        String crdMajorCardNo = "";
        String oldKey = "";
        showLogMessage("I","","  open 2   Mon="+parm.acctMonthS+","+parm.acctMonthE);
        int cursorIndex1 = openCursor();
        showLogMessage("I","","  open 2 end  ");
        while (fetchTable()) {
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
            totCnt8++;
            if(totCnt8 % 100000 == 0 || totCnt8 == 1)
               showLogMessage("I", "", String.format("Data Process C005-2 record=[%d]\n", totCnt8));

            if (!key.equals(oldKey)) {
                if (!oldKey.equals("")) {
                    setBillInfo2(parm, destAmtSum, maxDestAmt, bilCurrTotCnt,maxPurchaseDate,crdCardNo,
                            crdIdNo, chiName, cellarPhone, crdIdPSeqno, crdGroupCode, crdCurrentCode,
                            crdOldCardNo, crdMajorCardNo, oldKey);
                }

                destAmtSum = 0;
                maxDestAmt = 0;
                bilCurrTotCnt = 0;
                maxPurchaseDate = "";
                crdCardNo = "";
                crdIdNo = "";
                chiName = "";
                cellarPhone = "";
                crdIdPSeqno = "";
                crdGroupCode = "";
                crdCurrentCode = "";
                crdOldCardNo = "";
                crdMajorCardNo = "";
                oldKey = key;
            }

            double destAmt = getValueDouble("db_amt");
            int dbCnt = getValueInt("db_cnt");
            destAmtSum += destAmt;
            maxDestAmt = Math.max(maxDestAmt, destAmt);
            totCnt += dbCnt;
            bilCurrTotCnt += dbCnt;
            if (destAmt > 0) {
                maxPurchaseDate = getValue("purchase_date");
                crdCardNo = getValue("card_no");
                crdIdNo = getValue("id_no");
                chiName = getValue("chi_name");
                cellarPhone = getValue("cellar_phone");
                crdIdPSeqno = getValue("id_p_seqno");
                crdGroupCode = getValue("group_code");
                crdCurrentCode = getValue("current_code");
                crdOldCardNo = getValue("old_card_no");
                crdMajorCardNo = getValue("major_card_no");
if(DEBUG_F==1) showLogMessage("I","","    CARD2=["+ crdCardNo+"] G="+getValue("group_code")+","+ getValue("card_type")+","+ getValue("acct_type")+",REF="+getValue("reference_no")+","+destAmt+","+destAmtSum);
            }
        }
        // 處理最後一個Key
        if (!oldKey.equals("")) {
            setBillInfo2(parm, destAmtSum, maxDestAmt, bilCurrTotCnt, maxPurchaseDate, crdCardNo,
                    crdIdNo, chiName, cellarPhone, crdIdPSeqno, crdGroupCode, crdCurrentCode,
                    crdOldCardNo, crdMajorCardNo, oldKey);
        }

        closeCursor(cursorIndex1);
    }

    private void setBillInfo2(CmsRightParm parm, double destAmtSum, double maxDestAmt,
            int bilCurrTotCnt, String maxPurchaseDate, String crdCardNo, String crdIdNo, String chiName, String cellarPhone,
            String crdIdPSeqno, String crdGroupCode, String crdCurrentCode, String crdOldCardNo,
            String crdMajorCardNo, String oldKey) throws Exception {
        // 合計金額或合計筆數滿足
        if ((eqIgno(parm.airAmtType, "1") && (maxDestAmt >= parm.airAmt)) || 
            (eqIgno(parm.airAmtType, "2") &&  destAmtSum >= parm.airAmt )) {
            // 保存上一個key對應的對象值
            BillInfo billInfo = parm.billInfoMap.get(oldKey);
            if (billInfo == null) {
                billInfo = new BillInfo();
                parm.billInfoMap.put(oldKey, billInfo);
            }
            
            if ("3".equals(parm.consumeType)) { 
                billInfo.bilCardNo = crdCardNo;
            }
            // 如果是id_p_seqno，存一筆,但cardNo要存入最後消費日的卡號,且消費金額是大於0的那一筆
            if (!"3".equals(parm.consumeType)) { 
                billInfo.bilCardNo = crdCardNo;
            }
            billInfo.bilCurrTotCnt += bilCurrTotCnt;
            billInfo.bilPurchaseDate = maxPurchaseDate;
            billInfo.chiName = chiName;
            billInfo.cellarPhone = cellarPhone;
            billInfo.crdIdPSeqno = crdIdPSeqno;
            billInfo.crdGroupCode = crdGroupCode;
            billInfo.crdCurrentCode = crdCurrentCode;
            billInfo.crdOldCardNo = crdOldCardNo;
            billInfo.crdMajorCardNo = crdMajorCardNo;
            billInfo.destAmtSum += destAmtSum;
            billInfo.freeCntRaise = parm.airCnt;
            billInfo.idNo = crdIdNo;
            billInfo.maxDestAmt = Math.max(maxDestAmt, billInfo.maxDestAmt);
        }
    }

    private void insertMktThsrUpgradeList(CmsRightParm parm) throws Exception {
        for (BillInfo billInfo : parm.billInfoMap.values()) {
            daoTable = "mkt_thsr_upgrade_list";
            extendField = daoTable + ".";
            setValue(extendField+"crt_date", sysDate);
            setValue(extendField+"item_no", "15");
            setValue(extendField+"id_no", billInfo.idNo); // 填入【CRD_IDNO】ID_NO
            setValue(extendField+"group_code", billInfo.crdGroupCode); // 填入【crd_card】GROUP_CODE
            setValue(extendField+"card_no", billInfo.bilCardNo); // 填入【bil_bill】最後一筆消費日期的CARD_NO,且金額>0
            setValue(extendField+"major_card_no", billInfo.crdMajorCardNo); // 填入【crd_card】的MAJOR_CARD_NO
            setValue(extendField+"mod_type", "A");
            setValue(extendField+"acct_month", busiMonth);   // 填入【bil_bill】ACCT_MONTH起迄值的迄值
            setValue(extendField+"use_month" , busiUseMonth);// 填入business_date年月(YYYYMM)
            setValue(extendField+"curr_month", busiMonth);  
if(DEBUG_F==1) showLogMessage("I","","    INSERT=["+ billInfo.bilCardNo+"] AMT="+billInfo.destAmtSum);
//        setValue(extendField+"old_card_no", ""); // OLD_CARD_NO:不用填
            setValue(extendField+"current_code", billInfo.crdCurrentCode); //【crd_card】CURRENT_CODE
            setValue(extendField+"purchase_date", billInfo.bilPurchaseDate); //填入【bil_bill】PURCHASE_DATE最後(新)一筆的消費日期 ,且金額>0 max(purchase_date)
            setValue(extendField+"data_from", "2"); //固定寫入2
            setValueInt(extendField+"free_cnt_basic", billInfo.freeCntBasic); //如果CONSUME_TYPE<>0參考第2.10.4項. 如果CONSUME_TYPE=0,填入【CMS_RIGHT_PARM】CONSUME_00_CNT的值
            setValueInt(extendField+"free_cnt_raise", billInfo.freeCntRaise); //填入FREE_CNT_basic
            setValueInt(extendField+"free_tot_cnt", billInfo.freeCntBasic + billInfo.freeCntRaise); //填入FREE_CNT_basic+ FREE_CNT_raise
            setValue(extendField+"chi_name", billInfo.chiName); //填入【CRD_IDNO】 CHI_NAME
            setValue(extendField+"cellar_phone", billInfo.cellarPhone); //填入【CRD_IDNO】 CELLAR_PHONE
            setValue(extendField+"id_p_seqno", billInfo.crdIdPSeqno); //填入CRD_CARD.ID_P_SEQ
            setValueDouble(extendField+"curr_max_amt", billInfo.maxDestAmt); //填入max(BIL_BILL.DEST_AMT) --寫入單筆最大金額
            setValueInt(extendField+"curr_tot_cnt", billInfo.bilCurrTotCnt); //填入BIL_BILL符合條件的總筆數
            setValueDouble(extendField+"tot_amt", billInfo.destAmtSum); //填入BIL_BILL的sum(db_amt)金額  
            setValue(extendField+"proj_code", parm.projCode); //填入CMS_RIGHT_PARM.PROJ_CODE
            setValue(extendField+"consume_type", parm.consumeType); //取自【cms_right_parm】CONSUME_TYPE
            setValue(extendField+"send_date", ""); //寫入系統日期.
            setValue(extendField+"mod_time", sysDate + sysTime); //填入 系統日期 
            setValue(extendField+"mod_pgm", javaProgram); //填入javaProgram
            
//            debugInsert = "Y";
            if (insertTable() > 0) {
                okCnt++;
            }
            
            if (dupRecord.equals("Y")) {
                comcr.errRtn("MKT_THSR_UPGRADE_LIST error! duplicate.", "", parm.projCode + "|" + billInfo.bilCardNo + "|" + billInfo.crdIdPSeqno);
            }
        }
    }
    
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
        int currCnt = 0;
        String currCntCond = "";
        int currTotCnt = 0;
        int consume00Cnt;
        String airCond = "";
        String airSupFlag0 = "";
        String airSupFlag1 = "";
        String airDay = "";
        String airAmtType = "";
        double airAmt = 0;
        int airCnt = 0;
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
        int freeCntBasic = 0;
        int freeCntRaise = 0;
        String bilCardNo = "";
        String crdGroupCode = "";
        String crdMajorCardNo = "";
        String crdCurrentCode = "";
        String crdOldCardNo;
        String bilPurchaseDate = "";
        String crdIdPSeqno = "";
        double maxDestAmt = 0d;
        double destAmtSum = 0d;
        int bilCurrTotCnt = 0;
        String chiName = "";
        String cellarPhone = "";
        String idNo = "";
    }
}
