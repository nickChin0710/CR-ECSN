/******************************************************************************
*                                                                             *
*                              MODIFICATION LOG                               *
*                                                                             *
*     DATE    Version    AUTHOR                       DESCRIPTION             *
*  --------- --------- ----------- -----------------------------------------  *
*  112/07/25 V1.01.01  Zuwei Su    program initial                            *
*  112/08/23 V1.01.02  Wilson      BIL_BILL改讀POST_DATE                       *
******************************************************************************/
package Crd;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.AccessDAO;
import com.CommCrd;
import com.CommCrdRoutine;
import com.CommDate;
import com.CommFunction;
import com.CommRoutine;

public class CrdR237 extends AccessDAO {
    private final String PROGNAME = "產生合庫發卡業務統計月報表(萬事達卡國際組織)程式  112/08/23 V1.01.02";
    CommFunction comm = new CommFunction();
    CommCrd comc = new CommCrd();
    CommDate commDate = new CommDate();
    CommCrdRoutine comcr = null;
    CommRoutine comr = null;

    int DEBUG = 1;
    String hTempUser = "";

    int reportPageLine = 45;
    String prgmId = "CrdR237";

    String rptIdR1 = "CRM237";
    String rptName1 = "合庫發卡業務統計月報表(萬事達卡國際組織)";
    int pageCnt1 = 0, lineCnt1 = 0;
    int rptSeq1 = 0;
    List<Map<String, Object>> lpar1 = new ArrayList<Map<String, Object>>();

    String buf = "";
    int totCnt = 0;
    int totCnt1 = 0;

    String hBusiBusinessDate = "";
    String hFirstDay = "";
    String hCallBatchSeqno = "";
    String hChiYymmdd = "";
    String tmp = "";

    List<String> nameList =
            Arrays.asList("卡娜赫拉", "i運動卡", "I享樂卡", "世界卡", "漢來世界卡", "雙幣(美金)", "雙幣(日圓)", "其他大合約卡");
    Map<String, PurchaseData> billMap = new HashMap<>();

    /***********************************************************************/
    public int mainProcess(String[] args) {
        try {
            // ====================================
            // 固定要做的
            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            showLogMessage("I", "", javaProgram + " " + PROGNAME + " Args=[" + args.length + "]");

            // 固定要做的
            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }
            // =====================================
            if (args.length > 3) {
                comc.errExit("Usage : CrdR237 [yyyymmdd] [seq_no] ", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());
            comr = new CommRoutine(getDBconnect(), getDBalias());

            hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";
            if (comc.getSubString(hCallBatchSeqno, 0, 8)
                    .equals(comc.getSubString(comc.getECSHOME(), 0, 8))) {
                hCallBatchSeqno = "no-call";
            }

            String checkHome = comc.getECSHOME();
            if (hCallBatchSeqno.length() > 6) {
                if (comc.getSubString(hCallBatchSeqno, 0, 6)
                        .equals(comc.getSubString(checkHome, 0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }

            comcr.hCallRProgramCode = javaProgram;
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            if (args.length > 0) {
                hBusiBusinessDate = "";
                if (args[0].length() == 8) {
                    hBusiBusinessDate = args[0];
                } else {
                    String errMsg = String.format("指定營業日[%s]", args[0]);
                    comcr.errRtn(errMsg, "營業日長度錯誤[yyyymmdd], 請重新輸入!", hCallBatchSeqno);
                }
            }
            selectPtrBusinday();
            
            if (!hBusiBusinessDate.equals(hFirstDay)) {
        		showLogMessage("E", "", "今日不為該月第一天,不執行此程式");
        		return 0;
            }

            selectBilBill();

            if (totCnt > 0) {
                headFile();
                writeFile();
                tailFile();
            }

            // 改為線上報表
            // String filename = String.format("%s/reports/%s.txt", comc.getECSHOME(), prgmId);
            // filename = Normalizer.normalize(filename, Normalizer.Form.NFKD);
            // comc.writeReport(filename, lpar1);
            comcr.insertPtrBatchRpt(lpar1);

            // ==============================================
            // 固定要做的
            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);
            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        } catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }
    }

    // ************************************************************************
    public int selectPtrBusinday() throws Exception {

        sqlCmd = "select to_char(sysdate,'yyyymmdd') as business_date";
        sqlCmd +=
                "     , substr((to_char(sysdate, 'yyyy')-1911)||to_char(sysdate, 'mmdd'), 1, 7) as h_chi_yymmdd ";
        sqlCmd += " from ptr_businday ";
        int recordCnt = selectTable();
        if (notFound.equals("Y")) {
            comcr.errRtn("select ptr_businday not found!", "", hCallBatchSeqno);
        }
        if (recordCnt > 0) {
            hBusiBusinessDate = hBusiBusinessDate.length() == 0
                    ? commDate.dateAdd(getValue("business_date"), 0, 0, 1)
                    : hBusiBusinessDate;
        }
        
        hFirstDay = hBusiBusinessDate.substring(0, 6) + "01";

//        sqlCmd = "select to_char(add_months(to_date(?,'yyyymmdd'),-6),'yyyymm')||'01' h_beg_date_bil ";
//        sqlCmd +=
//                "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date_bil ";
//        sqlCmd += "     , to_char(add_months(to_date(?,'yyyymmdd'),-1),'yyyymm')||'01' h_beg_date ";
//        sqlCmd +=
//                "     , to_char(add_months(last_day(to_date(?,'yyyymmdd')),-1),'yyyymmdd') h_end_date ";
//        sqlCmd += " from dual ";
//        setString(1, hBusiBusinessDate);
//        setString(2, hBusiBusinessDate);
//        setString(3, hBusiBusinessDate);
//        setString(4, hBusiBusinessDate);
//
//        recordCnt = selectTable();
//        if (recordCnt > 0) {
//            hBegDate = getValue("h_beg_date");
//            hEndDate = getValue("h_end_date");
//            hBegDateBil = getValue("h_beg_date_bil");
//            hEndDateBil = getValue("h_end_date_bil");
//        }
//
                    hChiYymmdd = commDate.toTwDate(hBusiBusinessDate);
                    showLogMessage("I", "", String.format("營業日=[%s]", hBusiBusinessDate));
//        showLogMessage("I", "", String.format("營業日=[%s][%s][%s][%s][%s][%s]", hBusiBusinessDate,
//                hChiYymmdd, hBegDate, hEndDate, hBegDateBil, hEndDateBil));
        return 0;
    }

    /***********************************************************************/
    void selectBilBill() throws Exception {
        sqlCmd = " SELECT                                                                                        "
                + "     BIL_BILL.GROUP_CODE,                                                                      "
                + "     BIL_BILL.ACCT_CODE,                                                                       "
                + "     BIL_BILL.SETTL_FLAG,                                                                      "
                + "     COUNT(1) AS CNT,                                                                          "
                + "     SUM(DECODE(BIL_BILL.SIGN_FLAG, '+', BIL_BILL.DEST_AMT, BIL_BILL.DEST_AMT *-1)) AS AMT     "
                + " FROM                                                                                          "
                + "     BIL_BILL                                                                                  "
                + " INNER JOIN CRD_CARD ON                                                                        "
                + "     BIL_BILL.CARD_NO = CRD_CARD.CARD_NO                                                       "
                + " WHERE                                                                                         "
                + "     BIL_BILL.ACCT_CODE IN ('BL', 'ID', 'IT', 'AO', 'OT', 'CA')                                "
                + "     AND SUBSTRING(BIL_BILL.POST_DATE,1,6) = ?                                                               "
                + "     AND CRD_CARD.CURRENT_CODE = '0'                                                           "
                + "     AND CRD_CARD.GROUP_CODE IN ('1630', '1631', '1672', '6673', '6674' , '1675',              "
                + " '1676', '1677', '1678', '1679', '1680', '1681', '1682', '1683',                               "
                + " '1684', '1685', '1686', '1687', '1688', '1689', '1690', '1691',                               "
                + " '1692', '1693', '1694', '1654', '1655', '1656','1657')                                        "
                + " GROUP BY BIL_BILL.GROUP_CODE,BIL_BILL.ACCT_CODE,BIL_BILL.SETTL_FLAG                           ";
        String lastMonth = commDate.monthAdd(hBusiBusinessDate, -1);
        setString(1, lastMonth);
        // init all name purchase data
        for (String name : nameList) {
            PurchaseData data = new PurchaseData();
            data.name = name;
            billMap.put(name, data);
        }
        openCursor();
        while (fetchTable()) {
            String groupCode = getValue("GROUP_CODE");
            String acctCode = getValue("ACCT_CODE");
            String settlFlag = getValue("SETTL_FLAG");
            int cnt = getValueInt("CNT");
            double amt = getValueDouble("AMT");
            totCnt += cnt;
            
            String name = "";
            switch (groupCode) {
                case "1684":
                case "1685":
                case "1694":
                    name = "卡娜赫拉";
                    break;
                case "1693":
                    name = "i運動卡";
                    break;
                case "1657":
                    name = "I享樂卡";
                    break;
                case "1630":
                    name = "世界卡";
                    break;
                case "1631":
                    name = "漢來世界卡";
                    break;
                case "6673":
                    name = "雙幣(美金)";
                    break;
                case "6674":
                    name = "雙幣(日圓)";
                    break;
                case "1672": 
                case "1675": 
                case "1676": 
                case "1677": 
                case "1678": 
                case "1679": 
                case "1680": 
                case "1681": 
                case "1682": 
                case "1683": 
                case "1686": 
                case "1687": 
                case "1688": 
                case "1689": 
                case "1690": 
                case "1691": 
                case "1692": 
                case "1654": 
                case "1655": 
                case "1656": 
                    name = "其他大合約卡";
                    break;

                default:
                    break;
            }
            
            PurchaseData data = billMap.get(name);
            if (data == null) {
                data = new PurchaseData();
                billMap.put(name, data);
            }
            // 當月消費金額消費筆數(國內一般消費)ACCT_CODE為BL、ID、IT、AO、OT且SETTL_FLAG不為0的資料的PURCHASE_AMT加總,資料筆數加總
            if (Arrays.asList("BL","ID","IT","AO","OT").contains(acctCode) && !"0".equals(settlFlag)) {
                data.domesticFee += amt;
                data.domesticCnt += cnt;
            }
            // 當月消費金額消費筆數(國外一般消費)ACCT_CODE為BL、ID、IT、AO、OT且SETTL_FLAG為0的資料的PURCHASE_AMT加總,資料筆數加總
            if (Arrays.asList("BL","ID","IT","AO","OT").contains(acctCode) && "0".equals(settlFlag)) {
                data.abroadFee += amt;
                data.abroadCnt += cnt;
            }
            // 當月消費金額消費筆數(國內預借現金)ACCT_CODE為CA且SETTL_FLAG不為0的資料的PURCHASE_AMT加總,資料筆數加總
            if (Arrays.asList("CA").contains(acctCode) && !"0".equals(settlFlag)) {
                data.domesticPreFee += amt;
                data.domesticPreCnt += cnt;
            }
            // 當月消費金額消費筆數(國外預借現金)ACCT_CODE為CA且SETTL_FLAG為0的資料的PURCHASE_AMT加總,資料筆數加總
            if (Arrays.asList("CA").contains(acctCode) && "0".equals(settlFlag)) {
                data.abroadPreFee += amt;
                data.abroadPreCnt += cnt;
            }

            if (DEBUG == 1) {
                showLogMessage("I", "",
                        "Read acct_month="
                                + lastMonth
                                + " group_code="
                                + groupCode
                                + " name="
                                + name
                                + " data="
                                + data
                                + " Cnt="
                                + totCnt);
            }

            if (totCnt % 5000 == 0 || totCnt == 1)
                showLogMessage("I", "", String.format("R065 Process 1 record=[%d]\n", totCnt));


            if (totCnt == 1)
                headFile();

            if (DEBUG == 1) {
                showLogMessage("I", "", "   888 load=" + totCnt);
            }
        }

        closeCursor();
    }


    /***********************************************************************/
    void headFile() throws Exception {
        String temp = "";

        pageCnt1++;
        if (pageCnt1 > 1)
            lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "##PPP"));

        buf = "";
        buf = comcr.insertStr(buf, "分行代號: " + "3144 信用卡部", 1);
        buf = comcr.insertStr(buf, "" + rptName1, 50);
        buf = comcr.insertStr(buf, "保存年限: 二年", 100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));

        buf = "";
        tmp = String.format("%3.3s年%2.2s月%2.2s日", hChiYymmdd.substring(0, 3),
                hChiYymmdd.substring(3, 5), hChiYymmdd.substring(5));
        buf = comcr.insertStr(buf, "報表代號: CRM237     科目代號:", 1);
        buf = comcr.insertStr(buf, "中華民國 " + tmp, 50);
        temp = String.format("%4d", pageCnt1);
        buf = comcr.insertStr(buf, "頁    次:" + temp, 100);
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf + "\r"));

        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", "\r"));

        lineCnt1 = 4;
    }

    /***********************************************************************/
    void tailFile() throws UnsupportedEncodingException {
        // buf = "";
        // lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        //
        // htail.fileValue = "備 註: １、本表為ＯＲＧ１０６下所有ＴＹＰＥ，排除ＴＹＰＥ為５９９、９９７、９９８者";
        // buf = htail.allText();
        // lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        //
        // htail.fileValue = " ２、流通卡為截至目前未停用之卡片，即控管碼不為Ａ、Ｂ、Ｅ、Ｆ、Ｋ、Ｌ、Ｍ、Ｎ、Ｏ、Ｓ、Ｘ者";
        // buf = htail.allText();
        // lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        //
        // htail.fileValue = " ３、有效卡為最近６個月有消費紀錄，且控管碼不為Ａ、Ｂ、Ｅ、Ｆ、Ｋ、Ｌ、Ｍ、Ｎ、Ｏ、Ｓ、Ｘ者";
        // buf = htail.allText();
        // lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));

    }

    /***********************************************************************/
    void writeFile() throws Exception {
        if (lineCnt1 > reportPageLine) {
            headFile();
        }
        if (DEBUG == 1) {
            showLogMessage("I", "",
                    " write=" + billMap);
        }

        buf = "    （二）當月消費金額（單位：新台幣/元）";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "             項目                     一般消費                        預借現金";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "     產品名稱                   國內            國外            國內            國外";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lineCnt1 += 3;
        for (String name : nameList) {
            PurchaseData data = billMap.get(name);
            if (data == null) {
                buf = name;
            } else {
                buf = data.feeText();
            }
            lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
            lineCnt1 += 1;
        }
        buf = "    （二）當月消費筆數";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "             項目                     一般消費                        預借現金";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        buf = "     產品名稱                   國內            國外            國內            國外";
        lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
        lineCnt1 += 3;
        for (String name : nameList) {
            PurchaseData data = billMap.get(name);
            if (data == null) {
                buf = name;
            } else {
                buf = data.cntText();
            }
            lpar1.add(comcr.putReport(rptIdR1, rptName1, sysDate, ++rptSeq1, "0", buf));
            lineCnt1 = lineCnt1 + 1;
        }

        return;
    }

    /************************************************************************/
    class PurchaseData {
        String name; // 產品名稱
        double domesticFee = 0; // 國內一般消費 - 當月消費金額
        double abroadFee = 0; // 國外一般消費 - 當月消費金額
        double domesticPreFee = 0; // 國內預借現金 - 當月消費金額
        double abroadPreFee = 0; // 國外預借現金 - 當月消費金額
        int domesticCnt = 0; // 國內一般消費 - 當月消費筆數
        int abroadCnt = 0; // 國外一般消費 - 當月消費筆數
        int domesticPreCnt = 0; // 國內預借現金 - 當月消費筆數
        int abroadPreCnt = 0; // 國外預借現金 - 當月消費筆數

        String feeText() throws UnsupportedEncodingException {
            String rtn = "    ";
            rtn += comc.fixLeft(name, 16);
            if (domesticFee > 0) {
                rtn += comc.fixRight(String.format("%,14d", (int)domesticFee), 16);
            } else {
                rtn += comc.fixRight("", 16);
            }
            if (abroadFee > 0) {
                rtn += comc.fixRight(String.format("%,14d", (int)abroadFee), 16);
            } else {
                rtn += comc.fixRight("", 16);
            }
            if (domesticPreFee > 0) {
                rtn += comc.fixRight(String.format("%,14d", (int)domesticPreFee), 16);
            } else {
                rtn += comc.fixRight("", 16);
            }
            if (abroadPreFee > 0) {
                rtn += comc.fixRight(String.format("%,14d", (int)abroadPreFee), 16);
            } else {
                rtn += comc.fixRight("", 16);
            }
            return rtn;
        }
        String cntText() throws UnsupportedEncodingException {
            String rtn = "    ";
            rtn += comc.fixLeft(name, 16);
            if (domesticCnt > 0) {
                rtn += comc.fixRight(String.format("%,14d", domesticCnt), 16);
            } else {
                rtn += comc.fixRight("", 16);
            }
            if (abroadCnt > 0) {
                rtn += comc.fixRight(String.format("%,14d", abroadCnt), 16);
            } else {
                rtn += comc.fixRight("", 16);
            }
            if (domesticPreCnt > 0) {
                rtn += comc.fixRight(String.format("%,14d", domesticPreCnt), 16);
            } else {
                rtn += comc.fixRight("", 16);
            }
            if (abroadPreCnt > 0) {
                rtn += comc.fixRight(String.format("%,14d", abroadPreCnt), 16);
            } else {
                rtn += comc.fixRight("", 16);
            }
            return rtn;
        }

        @Override
        public String toString() {
            return "PurchaseData [name="
                    + name
                    + ", domesticFee="
                    + domesticFee
                    + ", abroadFee="
                    + abroadFee
                    + ", domesticPreFee="
                    + domesticPreFee
                    + ", abroadPreFee="
                    + abroadPreFee
                    + ", domesticCnt="
                    + domesticCnt
                    + ", abroadCnt="
                    + abroadCnt
                    + ", domesticPreCnt="
                    + domesticPreCnt
                    + ", abroadPreCnt="
                    + abroadPreCnt
                    + "]";
        }
    }

    /************************************************************************/
    public static void main(String[] args) throws Exception {
        CrdR237 proc = new CrdR237();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
    }
}