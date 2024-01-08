/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version       AUTHOR                 DESCRIPTION                *
* --------- --------- ------------ ----------------------------------------- *
* 112/03/17 V1.00.00  Yang, Bo     Program initial	                     *
* 112/06/29 V1.00.01  Kirin        del data_type = '06'                      *
* 112/06/30 V1.00.02  Zuwei Su     改為openCursor方式讀取資料                *
* 112/07/04 V1.00.03  Zuwei Su     按照CmsC005調整處理邏輯                   *
* 112/07/13 V1.01.02  Lai          add cms_mcc_group                         *
* 112/07/19 V1.01.04  Lai          modify ftp                                *
* 112/08/18 V1.02.01  Lai          modify error                              *
* 112/08/30 V1.01.03  Lai          add 一般消費 check cms_right_parm_detl       *
* 112/09/07 V1.02.02  Lai          mark zip                                  *
* 112/09/11 V1.02.03  Lai          modify input parm  by acct_month          *
* 112/09/25 V1.02.04  Lai          Add TCBAIRPORT_PARK_3144.TXT              *
* 112/11/08 V1.02.05  Lai          modify reset totCnt1 ,delete useacct_month*
* 112/11/16 V1.00.06  Kirin        showmess call CmsRightParm                *       
* 112/11/22 V1.00.07  Kirin        移除 comc.errExit--> return code非0         *
* 112/12/28 V1.00.08  Ryan         busiMonth,acctMonthE 拿掉-1 ,移除1日執行檢核                        *
*****************************************************************************/
package Cms;

import com.BaseBatch;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommFunction;
import com.*;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmsC004 extends BaseBatch {
    private final String PROGNAME = "機場停車名單程式    112/11/08 V1.02.05";
    CommFunction    comm = new CommFunction();
    CommCrd         comc = new CommCrd();
    CommCrdRoutine comcr;
    
    Document document   = new Document();
    String rptName1     = "TCBAIRPORT_PARK";
    int    rptSeq1      = 0;
    String text1        = "";
    String outFileName1 = "";
    List<Map<String, Object>> lpar1 = new ArrayList<>();

    String rptName2     = "TCBAIRPORT_PARK_3144";
    int    rptSeq2      = 0;
    String text2        = "";
    String outFileName2 = "";
    List<Map<String, Object>> lpar2 = new ArrayList<>();

    int DEBUG   = 0;
    int DEBUG_F = 0;

    // BusinessDate
    private String busiDate = "";
    private String busiMonth = "";
    private String busiUseMonth = "";
    private String busiPreYear = "";
    private int totCnt  = 0;
    private int totCnt1 = 0;
    private int totCnt2 = 0;
    private int okCnt   = 0;
    String stderr    = "";

    @Override
    protected void dataProcess(String[] args) throws Exception {
    }

    public static void main(String[] args) throws Exception {
        CmsC004 proc = new CmsC004();
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
               // comc.errExit("Error!! Someone is running this program now!!!", "Please wait a moment to run again!!");
            	showLogMessage("Error!! Someone is running this program now!!!", "", "Please wait a moment to run again!!");
        		return 0; 
            }
            printf("Usage : CmsC004 [BUSINESS_DATE] [計算當月(YYYYMM)]");
            
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallRProgramCode = javaProgram;
            comcr.callbatch(0, 0, 0);
            
            if (args.length == 1) {
                busiDate  = args[0];
//                busiMonth = commDate.dateAdd(busiDate, 0,-1, 0).substring(0, 6);
                busiMonth = busiDate.substring(0, 6);
            } else {
                selectPtrBusinday();
            }
            busiUseMonth = busiDate.substring(0, 6);
            busiPreYear  = commDate.dateAdd(busiMonth, -1, 0, 0).substring(0, 4);

            
            showLogMessage("I", "", String.format("程式營業日參數[%s],Acct_M=[%s],Use_m=[%s]"
                              , busiDate, busiMonth, busiUseMonth));
            showLogMessage("I", "", String.format("執行日期(system_date)=[%s]", sysDate));
            showLogMessage("I","", "CmsC004 call CmsRightParm ["+ busiDate +"] initialization success.....");
            
//            if (!busiDate.substring(6).equals("01")) {
//               // comc.errExit(String.format("本程式只在每月1日執行[%s]", busiDate), "");
//        		showLogMessage("I", "", "本程式只在每月1日執行, 本日非執行日!! process end....");
//        		return 0;
//            }

            // 刪除前24個月的數據
            deleteCmsAirportList();

            int cnt = selectCmsAirportList();
            if (cnt > 0) {
                //comc.errExit("本月["+sysDate.substring(0, 6)+"]己產製名單,不可重覆執行", "");
            	showLogMessage("本月[%s", sysDate.substring(0, 6), "]己產製名單,不可重覆執行");
        		return 0;
            }

            selectCmsRightParm(busiDate);

            outPutTextFile();

            if (!lpar1.isEmpty()) {
                comc.writeReport(outFileName1, lpar1, "MS950");
                lpar1.clear();
            }

            if (!lpar2.isEmpty()) {
                comc.writeReport(outFileName2, lpar2, "MS950");
                lpar1.clear();
            }

            ftpRtn(rptName1 + ".TXT" , 1);
            ftpRtn(rptName2 + ".TXT" , 2);

            showLogMessage("I","",String.format("筆數  tot_count=[%d],ok_cnt=[%d]", totCnt, okCnt));

            // ==============================================
            // 固定要做的
            comcr.callbatch(1, 0, 0);
            showLogMessage("I", "", "執行結束");
            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }
/*******************************************************************************/
@Override
protected void selectPtrBusinday() throws Exception {
    busiDate = "";
    sqlCmd = " select business_date, " 
//           + " to_char(add_months(to_date(business_date, 'yyyymmdd'), -1), 'yyyymm') as h_prev_month, "
    	   + " left(business_date,6) as h_prev_month, "
           + " to_char(add_months(to_date(business_date, 'yyyymmdd'), -1), 'yyyy') as h_curr_year, "
           + " to_char(add_years(add_months(to_date(business_date, 'yyyymmdd'), -1), -1), 'yyyy') as h_prev_year "
           + " from ptr_businday";

    selectTable();
    if (notFound.equals("Y")) {
        comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
    }
    busiDate = getValue("business_date");
    busiMonth = getValue("h_prev_month");
//  busiYear = getValue("h_curr_year");
    busiPreYear = getValue("h_prev_year");
}
/*****************************************************************************************/
    private int selectCmsAirportList() throws Exception {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(" SELECT ");
        sb.append("     * ");
        sb.append(" FROM ");
        sb.append("     cms_airport_park_list ");
        sb.append(" WHERE ");
        sb.append("     acct_month like ?||'%'  and item_no = '09' ");

        sqlCmd = sb.toString();
        setString(1, sysDate.substring(0, 6)); 
        return selectTable();
    }

    // 刪除
    private void deleteCmsAirportList() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.add(Calendar.YEAR, -2);
        java.util.Date y = c.getTime();
        String yearmonth = format.format(y);
        sqlCmd = " delete cms_airport_park_list where crt_date <= ? ";
        ppp(1, yearmonth);
        sqlExec(sqlCmd);
        if (sqlNrow < 0) {
            errmsg("delete cms_airport_park_list error");
            //errExit(1);
            errExit(0);
        }
        sqlCmd = " delete cms_airport_park_list  where acct_month = ? and item_no = '09' ";
        ppp(1, busiMonth);
        showLogMessage("I", "", "======= DELETE AIR curr = [" + busiMonth + "]");
        sqlExec(sqlCmd);
    }

    void selectCmsRightParm(String date) throws Exception {
        sqlCmd = " select * from cms_right_parm ";
        sqlCmd += " where active_status = 'Y' and apr_flag = 'Y' and item_no = '09' ";
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
            cmsRightParm.airCond     = colSs(ii, "air_cond");
            cmsRightParm.airSupFlag0 = colSs(ii, "air_sup_flag0");
            cmsRightParm.airSupFlag1 = colSs(ii, "air_sup_flag1");
            cmsRightParm.airDay = colSs(ii, "air_day");
            cmsRightParm.airAmtType = colSs(ii, "air_amt_type");
            cmsRightParm.airAmt = colNum(ii, "air_amt");
            cmsRightParm.airCnt = colInt(ii, "air_cnt");
            cmsRightParm.lastMm = colSs(ii, "last_mm");
            cmsRightParm.chooseCond = colSs(ii, "choose_cond");
            cmsRightParm.consumeType = colSs(ii, "consume_type");
            cmsRightParm.groupCardFlag  = colSs(ii, "group_card_flag");
            cmsRightParm.acctTypeFlag   = colSs(ii, "acct_type_flag");
            cmsRightParm.debutGroupCond = colSs(ii, "debut_group_cond");
 showLogMessage("I","","Get PARM=["+cmsRightParm.projCode +"] AIR="+ cmsRightParm.airCond+","+cmsRightParm.currCond+","+colSs(ii, "group_card_flag")+","+colSs(ii, "acct_type_flag")+","+colSs(ii, "debut_group_cond"));
            // 取得【cms_right_parm】join【cms_right_parm_detl】資料
            // cmsRightParm.detlList = selectCmsRightParmDetl(cmsRightParm.projCode);
            // 取得一般消費期間與特殊消費期間
            getAcctMonth(cmsRightParm);
            // 一般消費
            if (eqIgno(cmsRightParm.currCond, "Y")) {
            //  if(DEBUG==1) countBilBill(cmsRightParm);
                selectBilBill(cmsRightParm);
            }
            // 特殊消費
            if (eqIgno(cmsRightParm.airCond, "Y")) {
                selectBilBill2(cmsRightParm);
            }
            
            insertCmsAirportParkList(cmsRightParm);
            // 寫入存檔
            writeDocument(cmsRightParm);
        }
        
//        return parmlist;
    }

    private List<CmsRightParmDetl> selectCmsRightParmDetl(String projCode) throws Exception {
        sqlCmd = " select table_id, apr_flag, data_type, data_code, data_code2, data_code3 "
                + " from cms_right_parm_detl "
                + " where item_no = '09' "
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

    // 取得acct_month起迄值,計算消費期間
    private void getAcctMonth(CmsRightParm parm) throws ParseException {
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
                parm.acctMonthE = busiMonth;
            } 
            if (eqIgno(parm.chooseCond, "2")) {
                parm.acctMonthS = commDate.monthAdd(busiMonth, -Integer.parseInt(parm.lastMm));
                parm.acctMonthE = parm.acctMonthS;
            }
        }
        // 特殊消費區間
        if (eqIgno(parm.airCond, "Y")) {
            parm.specAcctMonthS = commDate.dateAdd(busiDate, 0, 0, -Integer.parseInt(parm.airDay));
            parm.specAcctMonthE = busiMonth.substring(0,6);
        }

        if(parm.specAcctMonthS.length() > 6) parm.specAcctMonthS = parm.specAcctMonthS.substring(0,6);
        if(parm.specAcctMonthE.length() > 6) parm.specAcctMonthE = parm.specAcctMonthE.substring(0,6);
        showLogMessage("I","","  Get Month=["+parm.acctMonthS+"]"+parm.acctMonthE+","+parm.currCond+","+parm.chooseCond+", SPEC="+parm.specAcctMonthS+","+parm.specAcctMonthE);

    }

    // 測試
    private void countBilBill(CmsRightParm parm) throws Exception {
        sqlCmd  = " select count(*) cnt_all "
                + "  from bil_bill a "
                + "  left join bil_contract b on b.contract_no = decode(a.contract_no, '', 'x', a.contract_no) and b.contract_seq_no = a.contract_seq_no "
                + "  left join crd_card c on a.card_no = c.card_no  "
                + "  left join crd_idno d on d.id_p_seqno = c.id_p_seqno  "
                + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
                + "   and c.card_type = a.card_type "
                + "   and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                // 消費資料 六大本金類
                + "   and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
                +                                                      " decode( ?,'Y','IT','XX'), "
                +                                                      " decode( ?,'Y','ID','XX'), "
                +                                                      " decode( ?,'Y','CA','XX'), "
                +                                                      " decode( ?,'Y','AO','XX'), "
                +                                                      " decode( ?,'Y','OT','XX')) "
                // 消費資料 最低單筆金額
                + "   and ((a.txn_code in ('06', '25', '27', '28', '29') ) or "
                + "        (a.txn_code not in ('06', '25', '27', '28', '29') and a.dest_amt >= ? )) "
                // 消費資料 消費期間
                + "   and decode(a.acct_month, '', 'x', a.acct_month) between ? and ? "
                // 排除一般消費 - 排除 ECS_CUS_MCHT_NO 的筆數
                + "   and not exists ( select 1 from mkt_mcht_gp a "
                + "                      left join mkt_mchtgp_data b on a.mcht_group_id = b.data_key "
                + "                     where b.table_name = 'MKT_MCHT_GP' "
                + "                       and b.data_key = 'MKTR00001' "
                + "                       and a.platform_flag = '2' "
                + "                       and b.data_code != '' "
                + "                       and b.data_code = a.ecs_cus_mcht_no )";

        int idx_c = 1;
        setString(idx_c++, parm.comsumeType);
        setString(idx_c++, parm.comsumeType);
        setString(idx_c++, parm.consumeBl);
        setString(idx_c++, parm.consumeCa);
        setString(idx_c++, parm.consumeIt);
        setString(idx_c++, parm.consumeAo);
        setString(idx_c++, parm.consumeId);
        setString(idx_c++, parm.consumeOt);
        setDouble(idx_c++, parm.currMinAmt);
        setString(idx_c++, parm.acctMonthS);
        setString(idx_c++, parm.acctMonthE);

       selectTable();

       int temp_int = getValueInt("cnt_all");
      showLogMessage("I","","  open 1  COUNT=["+ temp_int +"]");
    }

    // 一般消費
    private void selectBilBill(CmsRightParm parm) throws Exception {
        sqlCmd = " select 1 as db_cnt, "
                + "    decode(a.acct_code,'IT', "
                + "          decode(?,'2', "
                + "                 decode(a.install_curr_term,1, "
                + "                        decode(b.refund_apr_flag,'Y',0,b.tot_amt), "
                + "                        0), "
                + "                 decode(b.refund_apr_flag,'Y',0,a.dest_amt)), "
                + "          case when a.txn_code in ('06','25','27','28','29') "
                + "               then a.dest_amt*-1 else a.dest_amt end) db_amt "
                + ", c.card_no,c.group_code,c.card_type,c.acct_type,c.major_id_p_seqno,c.id_p_seqno"
                + ", c.major_card_no,c.old_card_no,c.current_code,c.eng_name,c.bin_type "
                + ", a.ecs_cus_mcht_no,a.mcht_category,a.purchase_date,a.reference_no "
                + ", d.id_no, d.chi_name, d.cellar_phone "
                + "  from bil_bill a "
                + "  left join bil_contract b on b.contract_no = decode(a.contract_no, '', 'x', a.contract_no) and b.contract_seq_no = a.contract_seq_no "
                + "  left join crd_card c on a.card_no = c.card_no  "
                + "  left join crd_idno d on d.id_p_seqno = c.id_p_seqno  "
                + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
                + "   and c.card_type = a.card_type "
                + "   and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                // 消費資料 六大本金類
                + "   and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
                +                                                      " decode( ?,'Y','IT','XX'), "
                +                                                      " decode( ?,'Y','ID','XX'), "
                +                                                      " decode( ?,'Y','CA','XX'), "
                +                                                      " decode( ?,'Y','AO','XX'), "
                +                                                      " decode( ?,'Y','OT','XX')) "
                // 消費資料 最低單筆金額
                + "   and ((a.txn_code in ('06', '25', '27', '28', '29') ) or "
                + "        (a.txn_code not in ('06', '25', '27', '28', '29') and a.dest_amt >= ? )) "
                // 消費資料 消費期間
                + "   and decode(a.acct_month, '', 'x', a.acct_month) between ? and ? "
                // 排除一般消費 - 排除 ECS_CUS_MCHT_NO 的筆數
                + "   and not exists ( select 1 from mkt_mcht_gp a "
                + "                      left join mkt_mchtgp_data b on a.mcht_group_id = b.data_key "
                + "                     where b.table_name = 'MKT_MCHT_GP' "
                + "                       and b.data_key = 'MKTR00001' "
                + "                       and a.platform_flag = '2' "
                + "                       and b.data_code != '' "
                + "                       and b.data_code = a.ecs_cus_mcht_no )";
        setString(1, parm.it1Type);
        setString(2, parm.comsumeType);
        setString(3, parm.comsumeType);
        setString(4, parm.consumeBl);
        setString(5, parm.consumeCa);
        setString(6, parm.consumeIt);
        setString(7, parm.consumeAo);
        setString(8, parm.consumeId);
        setString(9, parm.consumeOt);
        setDouble(10, parm.currMinAmt);
        setString(11, parm.acctMonthS);
        setString(12, parm.acctMonthE);

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
        

        // 查詢CMS_RIGHT_PARAM_DETL無數據，無需比對CARD_NO各項值
        double destAmtSum = 0d;
        double maxDestAmt = 0d;
        int bilCurrTotCnt = 0;
        String maxPurchaseDate = "";
        String crdCardNo = "";
        String crdIdNo = "";
        String chiName = "";
        String engName = "";
        String cellarPhone = "";
        String crdIdPSeqno = "";
        String crdGroupCode = "";
        String crdCurrentCode = "";
        String crdOldCardNo = "";
        String crdMajorCardNo = "";
        String crdMajorIdPSeqNo = "";
        String crdBinType = "";
        String oldKey = "";
        totCnt1 = 0;
      showLogMessage("I","","  open 1  date="+ parm.acctMonthS+","+parm.acctMonthE+","+parm.currMinAmt);
  // showLogMessage("I","","  8888888 1 =["+sqlCmd + "]");
        int cursorIndex1 = openCursor();
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
            totCnt1++;
            if(totCnt1 % 100000 == 0 || totCnt1 == 1)
               showLogMessage("I","","Data Process C004-1 record=["+totCnt1+"]"+key+","+oldKey);

            if (!key.equals(oldKey)) {
                if (!oldKey.equals("")) {
                    setBillInfo(parm, destAmtSum, maxDestAmt, bilCurrTotCnt, maxPurchaseDate,
                          crdCardNo, crdIdNo, chiName, engName, cellarPhone, crdIdPSeqno, crdGroupCode,
                          crdCurrentCode, crdOldCardNo, crdMajorCardNo, crdMajorIdPSeqNo, crdBinType, oldKey);
                }
                
                destAmtSum = 0;
                maxDestAmt = 0;
                bilCurrTotCnt = 0;
                maxPurchaseDate = "";
                crdCardNo = "";
                crdIdNo = "";
                chiName = "";
                engName = "";
                cellarPhone = "";
                crdIdPSeqno = "";
                crdGroupCode = "";
                crdCurrentCode = "";
                crdOldCardNo = "";
                crdMajorCardNo = "";
                crdMajorIdPSeqNo = "";
                crdBinType = "";
                oldKey = key;
            }
            
            double destAmt = getValueDouble("db_amt");
            int    dbCnt   = getValueInt("db_cnt");
            destAmtSum    += destAmt;
            maxDestAmt     = Math.max(maxDestAmt, destAmt);
            totCnt        += dbCnt;
            bilCurrTotCnt += dbCnt;
            if (destAmt > 0) {
                maxPurchaseDate = getValue("purchase_date");
                crdCardNo = getValue("card_no");
                crdIdNo = getValue("id_no");
                chiName = getValue("chi_name");
                engName = getValue("eng_name");
                cellarPhone = getValue("cellar_phone");
                crdIdPSeqno = getValue("id_p_seqno");
                crdGroupCode = getValue("group_code");
                crdCurrentCode = getValue("current_code");
                crdOldCardNo = getValue("old_card_no");
                crdMajorCardNo = getValue("major_card_no");
                crdMajorIdPSeqNo = getValue("major_id_p_seqno");
                crdBinType = getValue("bin_type");
if(DEBUG_F==1) showLogMessage("I","","  Read 1 card_no="+crdCardNo+","+crdBinType+","+getValue("reference_no"));
            }
        }
        // 處理最後一個Key
        if (!oldKey.equals("")) {
            setBillInfo(parm, destAmtSum, maxDestAmt, bilCurrTotCnt, maxPurchaseDate,
                    crdCardNo, crdIdNo, chiName, engName, cellarPhone, crdIdPSeqno, crdGroupCode,
                    crdCurrentCode, crdOldCardNo, crdMajorCardNo, crdMajorIdPSeqNo, crdBinType, oldKey);
        }

        closeCursor(cursorIndex1);
    }

    private void setBillInfo(CmsRightParm parm, double destAmtSum, double maxDestAmt,
            int bilCurrTotCnt, String maxPurchaseDate, String crdCardNo, String crdIdNo,
            String chiName, String engName, String cellarPhone, String crdIdPSeqno, String crdGroupCode,
            String crdCurrentCode, String crdOldCardNo, String crdMajorCardNo, String crdMajorIdPSeqno, String crdBinType, String oldKey)
            throws Exception {
        // 合計金額或合計筆數滿足
        if ((eqIgno(parm.currAmtCond, "Y") && (destAmtSum >= parm.currAmt)) ||
            (eqIgno(parm.currCntCond, "Y") && totCnt >= parm.currTotCnt)) {
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
            billInfo.engName = engName;
            billInfo.cellarPhone = cellarPhone;
            billInfo.crdIdPSeqno = crdIdPSeqno;
            billInfo.crdGroupCode = crdGroupCode;
            billInfo.crdCurrentCode = crdCurrentCode;
            billInfo.crdOldCardNo = crdOldCardNo;
            billInfo.crdMajorCardNo = crdMajorCardNo;
            billInfo.crdMajorIdPSeqno = crdMajorIdPSeqno;
            billInfo.crdBinType = crdBinType;
            billInfo.destAmtSum += destAmtSum;
            billInfo.freeCntBasic = parm.currCnt;
            // consumeType=0,FREE_CNT_BASIC=1,各項金額還是要算出來
            if ("0".equals(parm.consumeType)) { 
                billInfo.freeCntBasic = parm.consume00Cnt;
            }
            billInfo.idNo = crdIdNo;
            billInfo.maxDestAmt = Math.max(maxDestAmt, billInfo.maxDestAmt);
if(DEBUG_F==1) showLogMessage("I","","    SET  card_no="+billInfo.bilCardNo+","+billInfo.crdBinType);
        }
    }

    // 特殊消費
    private void selectBilBill2(CmsRightParm parm) throws Exception {
        sqlCmd = " select 1 as db_cnt, "
                + "    decode(a.acct_code,'IT', "
                + "          decode(?,'2', "
                + "                 decode(a.install_curr_term,1, "
                + "                        decode(b.refund_apr_flag,'Y',0,b.tot_amt), "
                + "                        0), "
                + "                 decode(b.refund_apr_flag,'Y',0,a.dest_amt)), "
                + "          case when a.txn_code in ('06','25','27','28','29') "
                + "               then a.dest_amt*-1 else a.dest_amt end) db_amt "
                + ", c.card_no,c.group_code,c.card_type,c.acct_type,c.major_id_p_seqno,c.id_p_seqno"
                + ", c.major_card_no,c.old_card_no,c.current_code,c.eng_name,c.bin_type "
                + ", a.ecs_cus_mcht_no,a.mcht_category,a.purchase_date,a.reference_no "
                + ", d.id_no, d.chi_name, d.cellar_phone "
                + "  from bil_bill a "
                + "  left join bil_contract b on b.contract_no = decode(a.contract_no, '', 'x', a.contract_no) and b.contract_seq_no = a.contract_seq_no "
                + "  left join crd_card c on a.card_no = c.card_no  "
                + "  left join crd_idno d on d.id_p_seqno = c.id_p_seqno  "
                + " where decode(cast(? as varchar(10)),'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(cast(? as varchar(10)),'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
                + "   and c.card_type = a.card_type "
                + "   and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                // 消費資料 六大本金類
                + "   and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
                +                                                      " decode( ?,'Y','IT','XX'), "
                +                                                      " decode( ?,'Y','ID','XX'), "
                +                                                      " decode( ?,'Y','CA','XX'), "
                +                                                      " decode( ?,'Y','AO','XX'), "
                +                                                      " decode( ?,'Y','OT','XX')) "
                // 消費資料 消費期間
                + "   and  a.acct_month between ? and ? ";

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
        setString(idx_f++, parm.specAcctMonthS);
        setString(idx_f++, parm.specAcctMonthE);
        
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
                                          + "   and item_no   = '09' "
                                          + "   and data_type = '01' "
                                          + "   and proj_code = ? "
                                          + "   and data_code = a.acct_type )) )"
                + "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '09' "
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
                                          + "   and item_no    = '09' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type ) ))"
                + "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '09' "
                                          + "   and data_type  = '06' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and data_code2 = a.card_type     "
                                          + "   and (data_code3 = decode(a.mcht_category,'','x',a.mcht_category) "
                                          + "   or data_code3 in (select v.mcc_group from cms_mcc_group v where v.mcc_code = a.mcht_category) )) )"
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

        // 查詢CMS_RIGHT_PARAM_DETL無數據，無需比對CARD_NO各項值
        double destAmtSum = 0d;
        double maxDestAmt = 0d;
        int bilCurrTotCnt = 0;
        String maxPurchaseDate = "";
        String crdCardNo = "";
        String crdIdNo = "";
        String chiName = "";
        String engName = "";
        String cellarPhone = "";
        String crdIdPSeqno = "";
        String crdGroupCode = "";
        String crdCurrentCode = "";
        String crdOldCardNo = "";
        String crdMajorCardNo = "";
        String crdMajorIdPSeqno = "";
        String crdBinType = "";
        String oldKey = "";
        totCnt2 = 0;
        showLogMessage("I","","  open 2  ="+parm.specAcctMonthS+","+parm.specAcctMonthE+","+parm.comsumeType);
//  showLogMessage("I","","  8888888 2 =["+sqlCmd + "]");
        if(parm.specAcctMonthS.length() < 1) parm.specAcctMonthS = "202001"; 
        if(parm.specAcctMonthE.length() < 1) parm.specAcctMonthE = "299901"; 
        int cursorIndex2 = openCursor();
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
            totCnt2++;
            if(totCnt2 % 100000 == 0 || totCnt2 == 1)
               showLogMessage("I","","Data Process C004-2 record=["+totCnt2+"]"+key+","+oldKey);
            if (!key.equals(oldKey)) {
                if (!oldKey.equals("")) {
                    setBillInfo2(parm, destAmtSum,maxDestAmt,bilCurrTotCnt,maxPurchaseDate,crdCardNo,
                            crdIdNo,chiName,engName,cellarPhone,crdIdPSeqno,crdGroupCode,crdCurrentCode,
                            crdOldCardNo, crdMajorCardNo, crdMajorIdPSeqno, crdBinType, oldKey);
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
                crdBinType = "";
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
                crdMajorIdPSeqno = getValue("major_id_p_seqno");
                crdBinType = getValue("bin_type");
if(DEBUG_F==1) showLogMessage("I","","  Read 2 card_no="+crdCardNo+","+crdBinType+","+getValue("reference_no"));
            }
        }
        // 處理最後一個Key
        if (!oldKey.equals("")) {
            setBillInfo2(parm, destAmtSum, maxDestAmt, bilCurrTotCnt, maxPurchaseDate, crdCardNo,
                    crdIdNo, chiName, engName, cellarPhone, crdIdPSeqno, crdGroupCode, crdCurrentCode,
                    crdOldCardNo, crdMajorCardNo, crdMajorIdPSeqno, crdBinType, oldKey);
        }

        closeCursor(cursorIndex2);
    }

    private void setBillInfo2(CmsRightParm parm, double destAmtSum, double maxDestAmt,
            int bilCurrTotCnt, String maxPurchaseDate, String crdCardNo, String crdIdNo, String chiName, String engName, String cellarPhone,
            String crdIdPSeqno, String crdGroupCode, String crdCurrentCode, String crdOldCardNo,
            String crdMajorCardNo, String crdMajorIdPSeqno, String crdBinType, String oldKey) throws Exception {
        // 合計金額或合計筆數滿足
        if ((eqIgno(parm.airAmtType, "1") && (maxDestAmt >= parm.airAmt)) 
                || (eqIgno(parm.airAmtType, "2") && destAmtSum >= parm.airAmt)) {
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
            billInfo.engName = engName;
            billInfo.cellarPhone = cellarPhone;
            billInfo.crdIdPSeqno = crdIdPSeqno;
            billInfo.crdGroupCode = crdGroupCode;
            billInfo.crdCurrentCode = crdCurrentCode;
            billInfo.crdOldCardNo = crdOldCardNo;
            billInfo.crdMajorCardNo = crdMajorCardNo;
            billInfo.crdMajorIdPSeqno = crdMajorIdPSeqno;
            billInfo.crdBinType = crdBinType;
            billInfo.destAmtSum += destAmtSum;
            billInfo.freeCntRaise = parm.airCnt;
            billInfo.idNo = crdIdNo;
            billInfo.maxDestAmt = Math.max(maxDestAmt, billInfo.maxDestAmt);
        }
    }

    private void insertCmsAirportParkList(CmsRightParm parm) throws Exception {
        for (BillInfo billInfo : parm.billInfoMap.values()) {
            daoTable = "cms_airport_park_list";
            extendField = daoTable + ".";
            setValue(extendField+"crt_date", sysDate);
            setValue(extendField+"acct_month", busiMonth);
            setValue(extendField+"use_month" , busiUseMonth);
            setValue(extendField+"cal_ym", busiMonth.substring(0,4));
            setValue(extendField+"change_class", "A");
            setValue(extendField+"preferential", "");
            setValue(extendField+"data_from", "2");
            setValue(extendField+"item_no", "09");
            setValue(extendField+"id_no", billInfo.idNo);
            setValue(extendField+"card_no", billInfo.bilCardNo);
            setValue(extendField+"major_card_no", billInfo.crdMajorCardNo);
            setValue(extendField+"car_no", "");
            setValueInt(extendField+"free_cnt_basic", billInfo.freeCntBasic);
            setValueInt(extendField+"free_cnt_raise", billInfo.freeCntRaise);
            setValueInt(extendField+"free_tot_cnt", billInfo.freeCntBasic + billInfo.freeCntRaise);
            setValue(extendField+"eng_name", billInfo.engName);
            setValue(extendField+"chi_name", billInfo.chiName);
            setValue(extendField+"id_p_seqno", billInfo.crdIdPSeqno);
            setValueDouble(extendField+"curr_max_amt", billInfo.maxDestAmt);
            setValueInt(extendField+"curr_tot_cnt", billInfo.bilCurrTotCnt);
            setValueDouble(extendField+"tot_amt", billInfo.destAmtSum);
            setValue(extendField+"proj_code", parm.projCode);
            setValue(extendField+"mod_time", sysDate + sysTime);
            setValue(extendField+"mod_pgm", "CmsC004");
            setValue(extendField+"major_id_p_seqno", billInfo.crdMajorIdPSeqno);
            setValue(extendField+"consume_type", parm.consumeType);
            setValue(extendField+"process_flag", "Y");
            setValue(extendField+"send_date", sysDate);
            setValue(extendField+"bin_type", billInfo.crdBinType);
            setValue(extendField+"purchase_date", billInfo.bilPurchaseDate);
    
            if (insertTable() > 0) {
                okCnt++;
            }
    
            if (dupRecord.equals("Y")) {
                comcr.errRtn("insert_cms_airport_park_list error! duplicate.", "", parm.projCode + "|" + billInfo.bilCardNo + "|" + billInfo.crdIdPSeqno);
            }
        }
    }

    private void writeDocument(CmsRightParm parm) throws UnsupportedEncodingException {
        for (BillInfo billInfo : parm.billInfoMap.values()) {
            document.outclass  = "A";
            document.outType   = billInfo.crdBinType;
            document.filler08  = "********";
            document.outCardNo = billInfo.bilCardNo.substring(8);
            document.outPurchaseDate = comc.getSubString(billInfo.bilPurchaseDate,4,8);
            document.outFreeCount  = String.valueOf(billInfo.freeCntBasic + billInfo.freeCntRaise);
            document.outlastCardNo = "        ";
            document.outName = billInfo.chiName.length() > 0
                    ? (billInfo.chiName.charAt(0) + "＊" + (billInfo.chiName.length() > 2 ? billInfo.chiName.substring(2) : ""))
                    : "";
    
            text1 = document.allText();
            lpar1.add(comcr.putReport("", "", sysDate, ++rptSeq1, "0", text1));

            document.filler08  = billInfo.bilCardNo.substring(0,8);
            text2 = document.allText();
            lpar2.add(comcr.putReport("", "", sysDate, ++rptSeq2, "0", text2));
if(DEBUG_F==1) showLogMessage("I","","  Write  card_no="+billInfo.bilCardNo+","+billInfo.crdBinType);
        }
    }
/******************************************************************************************/
private void outPutTextFile() {
        String hNcccFilename1 = rptName1 + ".TXT";
        showLogMessage("I", "", "Output Filename = [" + hNcccFilename1 + "]");
        outFileName1 = String.format("%s/media/cms/%s", comc.getECSHOME(), hNcccFilename1);
        outFileName1 = Normalizer.normalize(outFileName1, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", "Output Filepath = [" + outFileName1 + "]");

        String hNcccFilename2= rptName2 + ".TXT";
        showLogMessage("I", "", "Output Filename = [" + hNcccFilename2 + "]");
        outFileName2 = String.format("%s/media/cms/%s", comc.getECSHOME(), hNcccFilename2);
        outFileName2 = Normalizer.normalize(outFileName2, java.text.Normalizer.Form.NFKD);
        showLogMessage("I", "", "Output Filepath = [" + outFileName2 + "]");
}
/***********************************************************************/
private void ftpRtn(String hFileNameI,int idx) throws Exception {
    int    errCode  = 0;
    String temstr1  = "";
    String temstr2  = "";
    String procCode = "";
    String hOwsWfValue2 = "";

    sqlCmd  = "select wf_value2 ";
    sqlCmd += " from ptr_sys_parm  ";
    sqlCmd += "where wf_parm = 'SYSPARM'  ";
    sqlCmd += "  and wf_key  = 'CITY_PARK_ZIP_PWD' ";
    selectTable();
    if (notFound.equals("Y")) {
      comcr.errRtn("select_ptr_sys_parm not found!", "CITY_PARK_ZIP_PWD", comcr.hCallBatchSeqno);
    }
    hOwsWfValue2 = getValue("wf_value2");

    /*** PKZIP 壓縮 ***/
    temstr1 = String.format("%s/media/cms/%s",comc.getECSHOME(), hFileNameI);
    temstr1 = Normalizer.normalize(temstr1, java.text.Normalizer.Form.NFKD);
    String filename = temstr1;
/*
    temstr2 = String.format("%s.zip",temstr1);
    String filename = temstr1;
    String hPasswd  = hOwsWfValue2;
    String zipFile  = temstr2;
    int tmpInt = comm.zipFile(filename, zipFile, hPasswd);
    if(tmpInt != 0) {
       comcr.errRtn(String.format("無法壓縮檔案[%s]", filename),"", hCallBatchSeqno);
    }
    comc.chmod777(zipFile);
*/

    CommFTP       commFTP = new CommFTP(getDBconnect()    , getDBalias());
    CommRoutine      comr = new CommRoutine(getDBconnect(), getDBalias());

    String  hEflgRefIpCode  = "";
    if(idx == 1)
      { hEflgRefIpCode        = "NCR2TCB"; 
        commFTP.hEflgSystemId = "NCR2TCB";
      }
    else 
      { hEflgRefIpCode        = "CREDITCARD"; 
        commFTP.hEflgSystemId = "CREDITCARD";
      }
    commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ");
    commFTP.hEflgGroupId    = "0000";
    commFTP.hEflgSourceFrom = "EcsFtp";
    commFTP.hEflgModPgm     = this.getClass().getName();
    commFTP.hEriaLocalDir   = String.format("%s/media/cms", comc.getECSHOME());
    System.setProperty("user.dir", commFTP.hEriaLocalDir);
 // filename  = String.format("%s.zip", hFileNameI);
    filename  = String.format("%s"    , hFileNameI);
    procCode  = String.format("put %s", filename);
    showLogMessage("I", "", procCode + ", " + hEflgRefIpCode + " 開始上傳....");
    errCode   = commFTP.ftplogName(hEflgRefIpCode, procCode);
    if (errCode != 0) {
      stderr = String.format("ftp_rtn=[%s]傳檔錯誤 err_code[%d]\n", procCode, errCode);
      showLogMessage("I", "", stderr);
    }
    else
    {
     backFile(filename);
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
/******************************************************************************************/
    class Document {
        String outclass;
        String outType;
        String filler08;
        String outCardNo;
        String outPurchaseDate;
        String outFreeCount;
        String outlastCardNo;
        String outName;

        String allText() throws UnsupportedEncodingException {
            String rtn = "";

            rtn += fixRight(outclass, 1, " ");
            rtn += new String(",".getBytes("MS950"), "MS950");
            rtn += fixRight(outType, 1, " ");
            rtn += new String(",".getBytes("MS950"), "MS950");
            rtn += fixRight(filler08, 8, " ");
            rtn += fixRight(outCardNo, 8, " ");
            rtn += new String(",".getBytes("MS950"), "MS950");
            rtn += fixRight(outPurchaseDate, 4, " ");
            rtn += new String(",".getBytes("MS950"), "MS950");
            rtn += fixLeft(outFreeCount, 4, "0");
            rtn += new String(",".getBytes("MS950"), "MS950");
            rtn += fixRight(outlastCardNo, 8, " ");
            rtn += new String(",".getBytes("MS950"), "MS950");
            rtn += fixRight(outName, 10, " ");
            return rtn;
        }

        String fixRight(String str, int len, String sup) throws UnsupportedEncodingException {
            if (str == null) {
                str = "";
            }

            int length = str.length();
            if (length > len) {
                str = str.substring(0, len);
            }

            for (int i = 0; i < len - length; i++) {
                str += sup;
            }

            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);

            return new String(vResult, "MS950");
        }

        String fixLeft(String str, int len, String sup) throws UnsupportedEncodingException {
            String tmp = "";
            if (str == null) {
                str = "";
            }

            int length = str.length();
            if (length > len) {
                str = str.substring(0, len);
            }

            for (int i = 0; i < len - length; i++) {
                tmp += sup;
            }

            str = tmp + str;
            byte[] bytes = str.getBytes("MS950");
            byte[] vResult = new byte[len];
            System.arraycopy(bytes, 0, vResult, 0, len);

            return new String(vResult, "MS950");
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
        String crdMajorIdPSeqno = "";
        String crdBinType = "";
        String crdCurrentCode = "";
        String crdOldCardNo;
        String bilPurchaseDate = "";
        String crdIdPSeqno = "";
        double maxDestAmt = 0d;
        double destAmtSum = 0d;
        int bilCurrTotCnt = 0;
        String chiName = "";
        String engName = "";
        String cellarPhone = "";
        String idNo = "";
    }
}
