/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE     Version    AUTHOR                       DESCRIPTION            *
*  ---------  --------- ----------- ----------------------------------------  *
*  112/03/21  V1.00.00  Zuwei Su    program initial，增加bill查詢條件，處理邏輯修改*
*  112/06/05  V1.00.01  Yang Bo     update  修正error return, 補充缺失字段    *
*  112/06/30  V1.00.02  Zuwei Su    bill資料讀取改為cursor方式，處理邏輯調整  *
*  112/07/19  V1.00.03  Zuwei Su    增列檔案處理                              *
*  112/07/21  V1.00.04  Zuwei Su    起訖月計算調整，只計算current_code=0的卡，刪除13月前和當月資料 *
*  112/08/21  V1.00.05  Zuwei Su    卡號中間4碼改成*號，年月轉民國年月        *
*  112/08/22  V1.00.06  Zuwei Su    執行前先刪除當月，關閉Debug訊息，執行再增顯示處理年月 *
*  112/11/09  V1.01.01  Lai         modify selectBilBill  add parm            *
*  112/11/22 V1.00.07  Kirin        移除 comc.errExit                              
*  112/12/28  V1.00.08   Ryan       hPrevMonth, 拿掉-1                         *                                             *
******************************************************************************/
package Cms;

import com.*;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.*;

public class CmsC006 extends BaseBatch {
    private final String PROGNAME = "高鐵接送名單程式   112/12/28  V1.01.08";
    CommFunction    comm = new CommFunction();
    CommCrd      commCrd = new CommCrd();
    CommDate    commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommFTP      commFTP = null;
    
    int    DEBUG   = 0;
    int    DEBUG_F = 0;
    int  DEBUG_DAT = 0;

    private static final String CMS_FOLDER = "/media/cms";
    private static final String FTP_FOLDER = "/crdatacrea/NCR2TCB/";
    
    private String hBusiBusinessDate;
    private String hBusiBusinessTwDate;
    private String hCallBatchSeqno;
    private String hBusinessMonth;
    private String hPrevMonth;
//    private String hCurrYear;
    private String hPrevYear;
    private int totCnt = 0;
    private int okCnt = 0;
    private StringBuilder buf = new StringBuilder(10240);

    @Override
    protected void dataProcess(String[] args) throws Exception {
    }

    public int mainProcess(String[] args) {
        try {
            int liArg = args.length;
            if (liArg > 1) {
                printf("Usage : CmsC006 [yyyymmdd]");
                exitProgram(1);
            }
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME);
            // =====================================
            if (comm.isAppActive(javaProgram)) {
               // commCrd.errExit("Error!! Someone is running this program now!!!",
               //         "Please wait a moment to run again!!");
                showLogMessage("I", "Error!! Someone is running this program now!!!", "Please wait a moment to run again!!  process end...." );
        		return 0;
            }
            if (!connectDataBase()) {
                commCrd.errExit("connect DataBase error", "");
            }
            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comcr.hCallRProgramCode = javaProgram;
            comcr.callbatch(0, 0, 0);

            if (liArg == 1) {
                hBusiBusinessDate = args[0];
                hBusinessMonth = hBusiBusinessDate.substring(0, 6);
//                hPrevMonth = commDate.monthAdd(hBusiBusinessDate, -1);
                hPrevMonth = hBusinessMonth;
            } else {
                selectPtrBusinday();
                hBusinessMonth = hBusiBusinessDate.substring(0, 6);
            }
            hPrevYear = commDate.dateAdd(hPrevMonth, -1, 0, 0).substring(0,4);
            if (!hBusiBusinessDate.endsWith("01")) {
                //commCrd.errExit("程式每月1號執行, 不是1號,程式結束", "");
            	showLogMessage("I", "", "程式每月1號執行, 不是1號,程式結束!! process end....");
        		return 0;
            }
            hBusiBusinessTwDate = commDate.toTwDate(hBusiBusinessDate);
            hBusiBusinessTwDate = hBusiBusinessTwDate.substring(0, hBusiBusinessTwDate.length() - 2);

            showLogMessage("I","", String.format("程式參數[%s]", hBusiBusinessDate));
            showLogMessage("I","", String.format("執行日期[%s]", sysDate));

            // 刪除前24個月的數據
            deleteMktThsrPickupList();
            // 2.6. 讀取【cms_right_parm 卡友權益資格參數主檔】where條件active_status=Y and apr_flag=Y and
            // ITEM_NO=16 and 介於權益專案生效日起迄(proj_date_s、proj_date_e)
            List<CmsRightParm> parmlist = selectCmsRightParm(hBusiBusinessDate);

            String str600 = String.format("%s/media/cms/TCBTHSRCAR_%8.8s.txt", commCrd.getECSHOME(),
                    sysDate);
            str600 = Normalizer.normalize(str600, java.text.Normalizer.Form.NFKD);
            String pk600 = String.format("%s/media/cms/TCBTHSRCAR_%8.8s.zip", commCrd.getECSHOME(),
                    sysDate);
            pk600 = Normalizer.normalize(pk600, java.text.Normalizer.Form.NFKD);

//            int tmpCount = 0;
//            int tmpCount1 = 0;
            int out = -1;
            String parmChPasswd = "";
            
            out = openOutputText(str600, "MS950");
            if (out == -1) {
                comcr.errRtn(str600, "檔案開啓失敗！", hCallBatchSeqno);
            }

            /* report head */
//            String buf = String.format("1,%8.8s,MEGABANK%s", hSystemDate, "");
//            writeTextFile(out, buf + "\n");

//            selectMktDownData();

            /* report tail */
            fillFileStr(parmlist);
            writeTextFile(out, buf.toString());
            showLogMessage("I", "",
                    String.format("筆數  tot_count=[ %d ],ok_count=[ %d ]", totCnt, okCnt));
            /*
             * if(tmp_count == 0 ) { fprintf(fptr3,"本日無資料\n"); }
             */
            closeOutputText(out);
            /*** PKZIP 壓縮 ***/
            String filename = str600;
            String hPasswd = parmChPasswd;
            String zipFile = pk600;
            int tmpInt = comm.zipFile(filename, zipFile, hPasswd);
            if (tmpInt != 0) {
                showLogMessage("I", "", String.format("無法壓縮檔案[%s]", filename));
            }

            /* FTP to ftp_server */
            String fmtFilename = filename.substring(filename.lastIndexOf("/") + 1);
            procFTP(fmtFilename);
            renameFile(fmtFilename);

            /* RM -f 刪除csv */
            showLogMessage("I", "", String.format("刪除 str=[%s]", str600));
            commCrd.fileDelete(str600);

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

    @Override
    protected void selectPtrBusinday() throws Exception {
        hBusiBusinessDate = "";
        sqlCmd = " select business_date, "
//               + " to_char(add_months(to_date(business_date, 'yyyymmdd'), -1), 'yyyymm') as h_prev_month, "
			   + " left(business_date,6) as h_prev_month, "
			   + " to_char(add_months(to_date(business_date, 'yyyymmdd'), -1), 'yyyy') as h_curr_year, "
               + " to_char(add_years(add_months(to_date(business_date, 'yyyymmdd'), -1), -1), 'yyyy') as h_prev_year "
               + " from ptr_businday";

        selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select_ptr_businday not found!", "", hCallBatchSeqno);
        }
        hBusiBusinessDate = getValue("business_date");
        hPrevMonth        = getValue("h_prev_month");
        hPrevYear         = getValue("h_prev_year");
//      hCurrYear = getValue("h_curr_year");
    }

    // 刪除
    private void deleteMktThsrPickupList() throws Exception {
        String yearmonth = commDate.monthAdd(hBusiBusinessDate, -13);
        showLogMessage("I", "", "delete mkt_thsr_pickup_list crt_date <= " + yearmonth );
        sqlCmd = " delete mkt_thsr_pickup_list where crt_date <= ? ";
        setString(1, yearmonth);
        
        int rc = executeSqlCommand(sqlCmd);
        if (rc < 0) {
            errmsg("delete mkt_thsr_pickup_list error");
            //errExit(1);
            errExit(0);
        }
            

        showLogMessage("I", "", "delete mkt_thsr_pickup_list acct_month= " + hPrevMonth);
        sqlCmd = " delete mkt_thsr_pickup_list where acct_month = ? ";
        setString(1, hPrevMonth);
        
            rc = executeSqlCommand(sqlCmd);
        if (rc < 0) {
            errmsg("delete mkt_thsr_pickup_list error");
            //errExit(1);
            errExit(0);
        }
    }

    // 2.6. 讀取【cms_right_parm 卡友權益資格參數主檔】
    // where條件active_status=Y and apr_flag=Y and ITEM_NO=16 and介於權益專案生效日起迄(proj_date_s、proj_date_e)
    private List<CmsRightParm> selectCmsRightParm(String date) throws Exception {
        sqlCmd = " select * from cms_right_parm ";
        sqlCmd += " where active_status = 'Y' and apr_flag = 'Y' and item_no = '16' ";
        sqlCmd += " and decode(proj_date_s,'','20100101',proj_date_s) <= ? ";
        sqlCmd += " and decode(proj_date_e,'','30000101',proj_date_e) >= ? ";
        ppp(1, date);
        ppp(2, date);
        sqlSelect();
        int ilSelectRow = sqlNrow;
        List<CmsRightParm> parmlist = new ArrayList<>();
        if (sqlNrow <= 0) {
            comcr.errRtn("參數=0筆,程式結束", "", comcr.hCallBatchSeqno);
        } else {
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

  showLogMessage("I","","Get PARM=["+cmsRightParm.projCode +"] AIR="+ cmsRightParm.airCond+","+cmsRightParm.currCond+",ACCT="+cmsRightParm.chooseCond+","+cmsRightParm.consumeType+","+cmsRightParm.airAmtType);

                // 取得【cms_right_parm】join【cms_right_parm_detl】資料
                cmsRightParm.detlList = selectCmsRightParmDetl(cmsRightParm.projCode);
                // 取得一般消費期間與特殊消費期間
                getAcctMonth(cmsRightParm);
                // 取得符合條件卡號及計算消費、存入DB、寫入檔案
//                getCardNo();
                if (eqIgno(cmsRightParm.currCond, "Y")) {
                    selectBilBill(cmsRightParm);
                }
//                if ("Y".equals(cmsRightParm.airCond)) {
//                    selectBilBill2(cmsRightParm);
//                }
                
                insertMktThsrPickupList(cmsRightParm);
            }
        }
        
        return parmlist;
    }

    private List<CmsRightParmDetl> selectCmsRightParmDetl(String projCode) throws Exception {
        sqlCmd = " select table_id, apr_flag, data_type, data_code, data_code2, data_code3 "
                + " from cms_right_parm_detl "
                + " where item_no   = '16' "
                + "   and table_id  = 'RIGHT' "
                + "   and proj_code = ? ";
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
        if (eqIgno(parm.currCond, "Y")) {
            if (eqIgno(parm.chooseCond, "1")) {
                if (parm.currPreMonth.length() == 1) {
                    parm.acctMonthS = hPrevYear + "0" + parm.currPreMonth;
                } else if (parm.currPreMonth.length() > 1) {
                    parm.acctMonthS = hPrevYear + parm.currPreMonth;
                } else {
                    parm.acctMonthS = hPrevYear + "01";
                }
                parm.acctMonthE = hPrevMonth; // hBusiBusinessDate.substring(0, 4) + "12";
            } 
            if (eqIgno(parm.chooseCond, "2")) {
                parm.acctMonthS = commDate.monthAdd(hPrevMonth, 1-Integer.parseInt(parm.lastMm));
                parm.acctMonthE = hPrevMonth;
            }
        }

        if (eqIgno(parm.airCond, "Y")) {
            parm.specAcctMonthS = commDate.monthAdd(hBusiBusinessDate, -Integer.parseInt(parm.airDay));
            parm.specAcctMonthE = hPrevMonth;
        }
        showLogMessage("I","","  Get Month=["+parm.acctMonthS+"]"+parm.acctMonthE+","+parm.currCond+","+parm.chooseCond+",lastM="+parm.lastMm);
        showLogMessage("I","","  Get SPEC =["+parm.specAcctMonthS+"]"+parm.specAcctMonthE);
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
                + ", c.card_no,c.group_code,c.card_type,c.acct_type,c.major_id_p_seqno,c.id_p_seqno, c.major_card_no, c.current_code "
                + ", a.ecs_cus_mcht_no,a.mcht_category,a.purchase_date "
                + ", d.id_no, d.chi_name "
                + "  from bil_bill a "
                + "  left join bil_contract b on b.contract_no = decode(a.contract_no, '', 'x', a.contract_no) and b.contract_seq_no = a.contract_seq_no "
                + "  left join crd_card c on a.card_no = c.card_no  "
                + "  left join crd_idno d on d.id_p_seqno = c.id_p_seqno  "
                + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
                + "       and c.card_type    = a.card_type "
                + "       and c.current_code = '0' "
                + "       and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                // 消費資料 六大本金類
                + "       and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
                + "                                                       decode( ?,'Y','IT','XX'), "
                + "                                                       decode( ?,'Y','ID','XX'), "
                + "                                                       decode( ?,'Y','CA','XX'), "
                + "                                                       decode( ?,'Y','AO','XX'), "
                + "                                                       decode( ?,'Y','OT','XX')) "
                // 消費資料 最低單筆金額
                + "       and (( a.txn_code in ('06', '25', '27', '28', '29') ) "
                + "              or ( a.txn_code not in ('06', '25', '27', '28', '29') and a.dest_amt >= ? )) "
                // 消費資料 消費期間
                + "       and a.acct_month between ? and ? "
                // 排除一般消費 - 排除 ECS_CUS_MCHT_NO 的筆數
                + "       and not exists ( select 1 from mkt_mcht_gp a "
                + "                     left join mkt_mchtgp_data b on a.mcht_group_id = b.data_key "
                + "                     where b.table_name    = 'MKT_MCHT_GP' "
                + "                       and b.data_key      = 'MKTR00001' "
                + "                       and a.platform_flag = '2' "
                + "                       and b.data_code    != '' "
                + "                       and b.data_code     = a.ecs_cus_mcht_no )";
if(DEBUG_DAT == 1) sqlCmd += "     AND  a.group_code = '1620'  and a.card_type = 'VI' and a.card_no in ('4258700005159104')";

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
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '16' "
                                          + "   and data_type  = '01' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.acct_type )) )"
                + "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '16' "
                                          + "   and data_type  = '02' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and data_code2 = a.card_type )) )"
                + "    and ( '0' = ? or ('1' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '16' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type ) )"
                                 + " or ('2' = ? and not exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '16' "
                                          + "   and data_type  = '03' "
                                          + "   and proj_code  = ? "
                                          + "   and data_code  = a.group_code "
                                          + "   and decode(data_code2,'',a.card_type,data_code2) = a.card_type ) ))"
                + "    and ('Y' != ? or ('Y' = ? and exists ( select 1 from cms_right_parm_detl a "
                                          + " where table_id   = 'RIGHT' "
                                          + "   and item_no    = '16' "
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
        
//        selectTable();
//
//        if (notFound.equals("Y")) {
//            printf(" -- selectBilBill not found [projCode]: '" + parm.projCode + "' -- ");
//            return;
//        }

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
            billInfo.bilCurrTotCnt += bilCurrTotCnt;
            billInfo.bilPurchaseDate = maxPurchaseDate;
            billInfo.chiName = chiName;
            billInfo.cellarPhone = cellarPhone;
            billInfo.crdIdPSeqno = crdIdPSeqno;
            billInfo.crdGroupCode = crdGroupCode;
            billInfo.crdCurrentCode = crdCurrentCode;
            billInfo.crdOldCardNo = crdOldCardNo;
            billInfo.crdMajorCardNo = crdMajorCardNo;
            billInfo.destAmtSum = destAmtSum;
            billInfo.freeCntBasic = parm.currCnt;
            // consumeType=0,FREE_CNT_BASIC=1,各項金額還是要算出來
            if ("0".equals(parm.consumeType)) { 
                billInfo.freeCntBasic = 1;
            }
            billInfo.freeCntRaise = 0;
            billInfo.idNo = crdIdNo;
            billInfo.maxDestAmt = maxDestAmt;
        }
    }

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
                + ", c.card_no,c.group_code,c.card_type,c.acct_type,c.major_id_p_seqno,c.id_p_seqno,c.sup_flag, c.current_code "
                + ", a.ecs_cus_mcht_no,a.mcht_category,a.purchase_date,a.reference_no, "
                + ", d.id_no, d.chi_name "
                + "  from bil_bill a "
                + "  left join bil_contract b on b.contract_no = decode(a.contract_no, '', 'x', a.contract_no) and b.contract_seq_no = a.contract_seq_no "
                + "  left join crd_card c on a.card_no = c.card_no  "
                + "  left join crd_idno d on d.id_p_seqno = c.id_p_seqno  "
                + " where decode(?,'1',c.major_id_p_seqno,'2',c.id_p_seqno,'3',c.card_no,'0','0') = decode(?,'1',a.major_id_p_seqno,'2',a.id_p_seqno,'3',a.card_no,'0','0')  "
                + "       and c.card_type = a.card_type "
                + "       and c.current_code = '0' "
                + "       and decode(c.group_code, '', '0000', c.group_code) = decode(a.group_code, '', '0000', a.group_code) "
                // 消費資料 六大本金類
                + "       and decode(a.acct_code, '', '  ', a.acct_code) in (decode( ?,'Y','BL','XX'), "
                + "                                                       decode( ?,'Y','IT','XX'), "
                + "                                                       decode( ?,'Y','ID','XX'), "
                + "                                                       decode( ?,'Y','CA','XX'), "
                + "                                                       decode( ?,'Y','AO','XX'), "
                + "                                                       decode( ?,'Y','OT','XX')) "
                // 消費資料 最低單筆金額
//                + "       and (( a.txn_code in ('06', '25', '27', '28', '29') ) "
//                + "              or ( a.txn_code not in ('06', '25', '27', '28', '29')  "
////                + "                 and a.dest_amt >= ? ) "
//                + "                 ) )"
                // 消費資料 消費期間
                + "       and substr(decode(a.purchase_date, '', 'x', a.purchase_date), 1, 6) between ? and ? "
            //  + "       and a.acct_month between ? and ? "
                // 排除一般消費 - 排除 ECS_CUS_MCHT_NO 的筆數
                + "       and not exists ( select 1 from mkt_mcht_gp a "
                + "                     left join mkt_mchtgp_data b on a.mcht_group_id = b.data_key "
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
//        setDouble(10, parm.currMinAmt);
        setString(10, parm.specAcctMonthS);
        setString(11, parm.specAcctMonthE);
        
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
        sqlCmd += "    and ( 1=2 "
                + "        or exists ( select 1 from cms_right_parm_detl a "
                + "                   where table_id = 'RIGHT' "
                + "                     and item_no = '16' "
                + "                     and data_type = '01' "
                + "                     and proj_code = ? "
                + "                     and data_code = decode(a.acct_type, '', 'x', a.acct_type) ) "
                + "        or exists ( select 1 from cms_right_parm_detl a "
                + "                   where table_id = 'RIGHT' "
                + "                     and item_no = '16' "
                + "                     and data_type in ('02','03') "
                + "                     and proj_code = ? "
                + "                     and data_code = decode(a.group_code, '', 'x', a.group_code) "
                + "                     and data_code2 = decode(a.card_type, '', 'x', a.card_type) ) "
                + "        or exists ( select 1 from cms_right_parm_detl a "
                + "                   where table_id = 'RIGHT' "
                + "                     and item_no = '16' "
                + "                     and data_type = '06' "
                + "                     and proj_code = ? "
                + "                     and data_code = decode(a.group_code, '', 'x', a.group_code) "
                + "                     and data_code2 = decode(a.card_type, '', 'x', a.card_type) "
                + "                     and data_code3 = decode(a.mcht_category, '', 'x', a.mcht_category) ) "
                + "        )";
        setString(12, parm.projCode);
        setString(13, parm.projCode);
        setString(14, parm.projCode);

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
        
//        selectTable();
//
//        if (notFound.equals("Y")) {
//            printf(" -- selectBilBill2 not found [projCode]: '" + parm.projCode + "' -- ");
//            return;
//        }

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
        showLogMessage("I","","  open 2   Mon="+parm.projCode+","+ parm.acctMonthS+","+parm.acctMonthE);
        int cursorIndex = openCursor();
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
            if (!key.equals(oldKey)) {
                if (!oldKey.equals("")) {
                    setBillInfo2(parm, destAmtSum, maxDestAmt, bilCurrTotCnt, maxPurchaseDate, crdCardNo,
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
if(DEBUG_F==1) showLogMessage("I","","    CARD1=["+ getValue("card_no")+"] REF="+getValue("reference_no")+","+getValueDouble("db_amt"));
            }
        }
        // 處理最後一個Key
        if (!oldKey.equals("")) {
            setBillInfo2(parm, destAmtSum, maxDestAmt, bilCurrTotCnt, maxPurchaseDate, crdCardNo,
                    crdIdNo, chiName, cellarPhone, crdIdPSeqno, crdGroupCode, crdCurrentCode,
                    crdOldCardNo, crdMajorCardNo, oldKey);
        }

        closeCursor(cursorIndex);
    }

    private void setBillInfo2(CmsRightParm parm, double destAmtSum, double maxDestAmt,
            int bilCurrTotCnt, String maxPurchaseDate, String crdCardNo, String crdIdNo, String chiName, String cellarPhone,
            String crdIdPSeqno, String crdGroupCode, String crdCurrentCode, String crdOldCardNo,
            String crdMajorCardNo, String oldKey) throws Exception {
        // 合計金額或合計筆數滿足
        //  以下2選1進行檢核,不符合,此筆排除(跳過)
        //  如果AIR_AMT_TYPE=1,檢核單筆(任一筆)消費金額:      dest_amt >=air_amt符合
        //如果AIR_AMT_TYPE=2,檢核加總消費金額: sum(dest_amt)>=air_amt符合
        if ((eqIgno(parm.airAmtType, "1") && (maxDestAmt >= parm.airAmt)) 
                || (eqIgno(parm.airAmtType, "2") && destAmtSum >= parm.airAmt)) {
            // 保存上一個key對應的對象值
            BillInfo billInfo = parm.billInfoMap.get(oldKey);
            billInfo = parm.billInfoMap.get(oldKey);
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

    private void insertMktThsrPickupList(CmsRightParm parm) throws Exception {
        for (BillInfo billInfo : parm.billInfoMap.values()) {
            daoTable = "mkt_thsr_pickup_list";
            extendField = daoTable + ".";
            setValue(extendField+"crt_date", sysDate);
            setValue(extendField+"item_no", "16");
            setValue(extendField+"id_no", billInfo.idNo); // 填入【CRD_IDNO】ID_NO
            setValue(extendField+"card_no", billInfo.bilCardNo); // 填入【bil_bill】CARD_NO
            setValue(extendField+"major_card_no", billInfo.crdMajorCardNo); // 填入【crd_card】的MAJOR_CARD_NO
            setValue(extendField+"mod_type", "A");
            setValue(extendField+"acct_month", hPrevMonth); // 填入【bil_bill】ACCT_MONTH起迄值的迄值
            setValue(extendField+"use_month" , hBusiBusinessDate.substring(0,6)); // 填入business_date年月(YYYYMM)
//        setValue(extendField+"old_card_no", ""); // OLD_CARD_NO:不用填
            setValue(extendField+"current_code", billInfo.crdCurrentCode); //【crd_card】CURRENT_CODE
            setValue(extendField+"purchase_date", billInfo.bilPurchaseDate); //填入【bil_bill】PURCHASE_DATE最後(新)一筆的消費日期 ,且金額>0 max(purchase_date)
            setValue(extendField+"data_from", "2"); //固定寫入2
            setValueInt(extendField+"free_cnt_basic", billInfo.freeCntBasic); //參考第2.7.4項
            setValueInt(extendField+"free_cnt_raise", billInfo.freeCntRaise); //參考第2.8.3項
            setValueInt(extendField+"free_tot_cnt", billInfo.freeCntBasic + billInfo.freeCntRaise); //填入FREE_CNT_basic+ FREE_CNT_raise
            setValue(extendField+"chi_name", billInfo.chiName); //填入【CRD_IDNO】 CHI_NAME
            setValue(extendField+"id_p_seqno", billInfo.crdIdPSeqno); //填入CRD_CARD.ID_P_SEQ
            setValueDouble(extendField+"curr_max_amt", billInfo.maxDestAmt); //填入max(BIL_BILL.DEST_AMT) --寫入單筆最大金額
            setValueInt(extendField+"curr_tot_cnt", billInfo.bilCurrTotCnt); //填入BIL_BILL符合條件的總筆數
            setValueDouble(extendField+"tot_amt", billInfo.destAmtSum); //填入BIL_BILL的sum(db_amt)金額  
            setValue(extendField+"proj_code", parm.projCode); //填入CMS_RIGHT_PARM.PROJ_CODE
            setValue(extendField+"consume_type", parm.consumeType); //取自【cms_right_parm】CONSUME_TYPE
            setValue(extendField+"send_date", sysDate); //寫入系統日期.
            setValue(extendField+"mod_time", sysDate + sysTime); //填入 系統日期 
            setValue(extendField+"mod_pgm", javaProgram); //填入javaProgram
if(DEBUG_F==1) showLogMessage("I","","    INSERT=["+ billInfo.bilCardNo+"] AMT="+billInfo.destAmtSum);
            
            if (insertTable() > 0) {
                okCnt++;
            }
            
            if (dupRecord.equals("Y")) {
                comcr.errRtn("mkt_thsr_pickup_list duprecord error!", "", parm.projCode + "|" + billInfo.bilCardNo + "|" + billInfo.crdIdPSeqno);
            }
        }
    }

    private void fillFileStr(List<CmsRightParm> parmList) throws Exception {
        String tmp = ""; 
        for (CmsRightParm parm : parmList) {
            for (BillInfo billInfo : parm.billInfoMap.values()) {
                String cardNo = billInfo.bilCardNo;
                tmp = cardNo.substring(0, 6) + "******" + cardNo.substring(12);
                buf.append(commCrd.fixLeft(tmp, 16));
                buf.append(",");
                buf.append(commCrd.fixLeft(hBusiBusinessTwDate, 5));
                buf.append(",");
                tmp = String.format("%04d", billInfo.freeCntBasic + billInfo.freeCntRaise);
                buf.append(commCrd.fixLeft(tmp, 4));
                buf.append(",");
                buf.append(commCrd.fixLeft(billInfo.chiName, 10));
                buf.append("\n");
            }
        }
        
    }
    
/*******************************************************************/
    private void procFTP(String fmtFilename) throws Exception {
        commFTP = new CommFTP(getDBconnect(), getDBalias());
        CommRoutine comr = new CommRoutine(getDBconnect(), getDBalias());
        
        commFTP.hEflgTransSeqno = comr.getSeqno("ECS_MODSEQ"); /* 串聯 log 檔所使用 鍵值 (必要) */
        commFTP.hEflgSystemId = "NCR2TCB"; /* 區分不同類的 FTP 檔案-大類 (必要) */
        commFTP.hEriaLocalDir = commCrd.getECSHOME()+CMS_FOLDER;//fileName;
        commFTP.hEflgGroupId = "000000"; /* 區分不同類的 FTP 檔案-次分類 (非必要) */
        commFTP.hEflgSourceFrom = "EcsFtp"; /* 區分不同類的 FTP 檔案-細分類 (非必要) */
        commFTP.hEflgModPgm = javaProgram;
        
        showLogMessage("I", "", "mput " + fmtFilename + " 開始傳送....");
        int errCode = commFTP.ftplogName("NCR2TCB", "mput " + fmtFilename);
        
        if (errCode != 0) {
            showLogMessage("I", "", "ERROR:無法傳送 " + fmtFilename + " 資料"+" errcode:"+errCode);
        //  commFTP.insertEcsNotifyLog(fmtFilename, "3", javaProgram, sysDate, sysTime);          
            insertEcsNotifyLog(fmtFilename);
        }
    }

    private void renameFile(String fmtFilename) throws Exception {
        String tmpstr1 = String.format("%s%s/%s", commCrd.getECSHOME(), CMS_FOLDER, fmtFilename);
        String tmpstr2 =
                String.format("%s%s/backup/%s", commCrd.getECSHOME(), CMS_FOLDER, fmtFilename);

        if (commCrd.fileCopy(tmpstr1, tmpstr2) == false) {
            showLogMessage("I", "", "ERROR : 檔案[" + fmtFilename + "]備份失敗!");
            return;
        }
        commCrd.fileDelete(tmpstr1);
        showLogMessage("I", "", "檔案 [" + fmtFilename + "] 已移至 [" + tmpstr2 + "]");
    }
//=====================
public int insertEcsNotifyLog(String fileName) throws Exception {
        setValue("crt_date", sysDate);
        setValue("crt_time", sysTime);
        setValue("unit_code", "C006");
        setValue("obj_type", "3");
        setValue("notify_head", "無法 FTP 傳送 " + fileName + " 資料");
        setValue("notify_name", "媒體檔名:" + fileName);
        setValue("notify_desc1", "程式 " + javaProgram + " 無法 FTP 傳送 " + fileName + " 資料");
        setValue("notify_desc2", "");
        setValue("trans_seqno", commFTP.hEflgTransSeqno);
        setValue("mod_time", sysDate + sysTime);
        setValue("mod_pgm", javaProgram);
        daoTable = "ecs_notify_log";

        insertTable();

        return (0);
}
//=====================
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

    /***********************************************************************/
    public static void main(String[] args) throws Exception {
        CmsC006 proc = new CmsC006();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}
