/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/06/12  V1.00.01  Lai        Initial                                    *
* 106/11/27  V1.00.02  Yash       add callbatch                              *
* 107/10/12  V1.01.01  Lai        add pub_date                               *
* 107/11/30  V1.02.01  Lai        add sys_rem (=vouch_cd_kind)               *
* 108/05/09  V1.03.01  Lai        add SeqCallBat and BilA110-batch call batch*
* 109/08/14  V1.03.02  Brian      add isOnlineCallBatchActive()              *
* 110/02/08  V1.04.01  Lai        add non norm vouch(curr_code='SPE')        *
* 110/09/28  V1.04.02  Lai        M:8642 add increase_0_days                 *
* 111/05/04  V1.04.03  Lai        CR:1305                                    *
* 111/11/09  V1.04.04  Zuwei Su   sync from mega & coding standard update    *
* 111/11/10  V1.04.05  Zuwei Su   增補01/18   Justin fix Erroneous String Compare & BRNO 欄位設定值'3144' & vouchType轉中文抽取為公用function *
* 111/11/11  V1.04.06  Zuwei Su   BRNO 欄位設定值'109'改为'3144'             *
*****************************************************************************/
package com;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class CommCrdRoutine extends AccessDAO {

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();

    final int DEBUG_F = 0;

    String locDate  = "";
    String locTime  = "";
    String[] dbname = new String[10];
    public String hSystemVouchDate = "";
    public String hGsvhStdVouchCd = "";
    public String hGsvhMemo1 = "";
    public String hGsvhMemo2 = "";
    public String hGsvhMemo3 = "";
    public String hGsvhCurr     = "";
    public String hGsvhModPgm  = "";
    public String hGsvhModUser = "";
    public String hGsvhRefNo   = "";
    public String hGsvhModWs   = "";
    public String hGsvhDbcr     = "";
    public String hVoucIdNo     = "";
    public String hVoucIfrsFlag = "";
    public String hVoucRefno     = "";
    public String hVoucPostRefno= "";
    public String hVoucSysRem   = "";
    public Integer vouchPageCnt  = 0;
    public String reportId = "";
    public String reportName = "";
    public String pubDate  = "";
    public String pubTime  = "";
    public int rptSeq = 0;
    public List<Map<String, Object>> lpar = new ArrayList<Map<String, Object>>();
    // callbatch
    public String hCallBatchSeqno = "";
    public String hCallRProgramCode = "";
    public String hCallErrorCode = "";
    public String hCallParameter1 = "";
    public String hCallParameter2 = "";
    public String hCallErrorDesc = "";
    public String hCallProcDesc = "";
    public String hCallProcMark = "";
    public String hCallProcMark1 = "";
    public String hCallProcMark2 = "";
    public String hCallProgramCode = "";
    public String hCallStartDate = "";
    public String hCallStartTime = "";
    public String hCallParameterData = "";
    public String hCallUserId = "";
    public String hCallWorkstationName = "";
    public String hCallClientProgram = "";
    public String bankName = "合 作 金 庫 商 業 銀 行";

    public CommCrdRoutine(Connection conn[], String[] dbAlias) throws Exception {
        // TODO Auto-generated constructor stub
        super.conn = conn;
        setDBalias(dbAlias);
        setSubParm(dbAlias);

        dbname[0] = dbAlias[0];
        
        return;
    }

    // ********************************************************************************
    public int deletePtrBatchRpt(String prgmId, String dateI) throws Exception {
        daoTable = "ptr_batch_rpt";
        whereStr = "WHERE program_code  = ? " + "  and start_date    = ? ";

        setString(1, prgmId);
        setString(2, dateI);

        int recCnt = deleteTable();

        return 0;
    }

    // ********************************************************************************
    public int insertPtrBatchRpt(List<Map<String, Object>> lpar) throws Exception {
        int actCnt = 0;
        noTrim = "Y";
        dateTime();
        String tmpStr = sysDate + sysTime;
        for (int i = 0; i < lpar.size(); i++) {
            // String tmp_str = lpar.get(i).get("sysDate").toString();
            if (tmpStr.length() > 8) {
                setValue("start_date", tmpStr.substring(0, 8));
                setValue("start_time", tmpStr.substring(8));
            } else {
                setValue("start_date", tmpStr.substring(0));
                setValue("start_time", "");
            }
            setValue("program_code", lpar.get(i).get("prgmId").toString());
            setValue("rptname", lpar.get(i).get("prgmName").toString());
            setValue("seq", lpar.get(i).get("seq").toString());
            setValue("kind", lpar.get(i).get("kind").toString());
            setValue("report_content", lpar.get(i).get("content").toString());

            daoTable = "ptr_batch_rpt";
            insertTable();
            if (dupRecord.equals("Y")) {
                return 0;
            }
        }
        noTrim = "";
        return actCnt;
    }

    // ********************************************************************************
    public int insertPtrBatchMemo3(List<Map<String, Object>> lpar, int pageCnt) throws Exception {
        int actCnt = 0;
        noTrim = "Y";
        int pageSize = 27;
        int j = pageSize * (pageCnt - 1);
        // showLogMessage("I", "", "888 PAGE_CNT=" + page_cnt + ", from=" + j+
        // ",size=" + lpar.size());
  //    dateTime();
  //    String tmp_str = sysDate + sysTime;
        String tmpStr = locDate + locTime;
if(DEBUG_F == 1) showLogMessage("I", "", " Insert Time=["+tmpStr+"]" + lpar.size());

        for (int i = j; i < lpar.size(); i++) {
            if (tmpStr.length() > 8) {
                setValue("start_date", tmpStr.substring(0, 8));
                setValue("start_time", tmpStr.substring(8));
            } else {
                setValue("start_date", tmpStr.substring(0));
                setValue("start_time", "");
            }
            int lineCnt = i + 1;
            // showLogMessage("I", "", " 888 Line_CNT=" + line_cnt );
            setValue("program_code", lpar.get(i).get("prgmId").toString());
            setValue("rptname", lpar.get(i).get("prgmName").toString());
            // setValue("seq" , lpar.get(i).get("seq").toString());
            setValue("seq", lineCnt + " ");
            setValue("kind", lpar.get(i).get("kind").toString());
            setValue("txt_content", lpar.get(i).get("content").toString());

            daoTable = "ptr_batch_memo3";
            insertTable();
            if (dupRecord.equals("Y")) {
                return 0;
            }
        }
        noTrim = "";
        return actCnt;
    }

    // ********************************************************************************
    public void insertEcsReportLog(String runPgmid, String reportId, String reportName, String busiDate,
            String fileType, String pageVert) throws Exception {
        selectSQL = "NEXTVAL FOR ECS_MODSEQ AS MOD_SEQNO ";
        daoTable = "SYSIBM.SYSDUMMY1";
        selectTable();

        String output = String.format("%010.0f", getValueDouble("MOD_SEQNO"));

        dateTime();

        setValue("report_seq", output);
        setValue("busi_date", busiDate);
        setValue("run_pgmid", runPgmid);
        setValue("report_id", reportId);
        setValue("report_name", reportName);
        setValue("file_type", fileType);
        setValue("page_vert", pageVert);
        setValue("crt_date", sysDate);
        setValue("mod_time", sysDate + sysTime);

        daoTable = "ecs_report_log";
        insertTable();
        if (dupRecord.equals("Y")) {
            comc.errExit("insert_ecs_report_log  失敗", "");
        }
    }

// ********************************************************************************
public void insertBatch(String hCallBatchSeqno, String progID, String hSystemDate, String hErrorCode,
                         String hErrorDesc) throws Exception {

        dateTime();
//      setValue("BATCH_SEQNO"   , SeqCallBat());
        setValue("BATCH_SEQNO"   , hCallBatchSeqno);
        setValue("PROGRAM_CODE"  , progID);
        setValue("START_DATE"    , hSystemDate);
        setValue("START_TIME"    , sysTime);
        setValue("EXECUTE_DATE_S", hSystemDate);
        setValue("EXECUTE_DATE_E", sysTime);
        setValue("ERROR_CODE"    , hErrorCode);
        setValue("ERROR_DESC"    , hErrorDesc);
	setValue("PROCESS_FLAG"  , "Y");

        daoTable = "ptr_callbatch";
        insertTable();
        if (dupRecord.equals("Y")) {
            comc.errExit("insert_batch 失敗", "");
        }
    }
    // ======================================================================
    // function
    //

    public Map<String, Object> putReport(String prgmid, String prgmName, String sysDate, int seq, String knd,
            String ctn) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("prgmId", prgmid);
        temp.put("prgmName", prgmName);
        temp.put("sysDate", sysDate);
        temp.put("seq", seq);
        temp.put("kind", knd);
        temp.put("content", ctn);
        return temp;
    }

    /**
     * 判斷是否是空字串, 空白也算空字串
     */
    public boolean isBlank(String strVal) {
        if (strVal == null || "".equals(strVal.trim())) {
            return true;
        }
        return false;
    }

    public double round(double v, int scale) {
        if (scale < 0) {
            scale = 0;
        }
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public double round(String v, int scale) {
        if (scale < 0) {
            scale = 0;
        }
        return round(Double.parseDouble(v), scale);
    }
    
    /* 與insertStr相同，FOR弱掃 */
    public String addStr(String sbuf, String str, int ps) {
        String rtn = "";
        int len = 0;
        for (int i = 0; i < sbuf.length(); i++) {

            int acsii = sbuf.charAt(i);
            int n = (acsii < 0 || acsii > 128) ? 2 : 1;
            if (len + n >= ps)
                break;
            len += n;
            rtn += sbuf.charAt(i);
        }
        for (int i = len + 1; i < ps; i++)
            rtn += " ";
        rtn += str;

        return rtn;
    }
    
    public String insertStr(String sbuf, String str, int ps) {
        String rtn = "";
        int len = 0;
        for (int i = 0; i < sbuf.length(); i++) {

            int acsii = sbuf.charAt(i);
            int n = (acsii < 0 || acsii > 128) ? 2 : 1;
            if (len + n >= ps)
                break;
            len += n;
            rtn += sbuf.charAt(i);
        }
        for (int i = len + 1; i < ps; i++)
            rtn += " ";
        rtn += str;

        return rtn;
    }

    public String insertStrCenter(String sbuf, String str, int nWidth) {
        String rtn = sbuf;

        int len = 0;
        for (int i = 0; i < sbuf.length(); i++) {
            int acsii = sbuf.charAt(i);
            len += (acsii < 0 || acsii > 128) ? 2 : 1;
        }
        int nSbufWidth = len;

        len = 0;
        for (int i = 0; i < str.length(); i++) {
            int acsii = str.charAt(i);
            len += (acsii < 0 || acsii > 128) ? 2 : 1;
        }
        int nStrWidth = len;

        int nSpace = (nWidth - nStrWidth) / 2 - nSbufWidth;
        for (int i = 0; i < nSpace; i++)
            rtn += " ";
        rtn += str;

        return rtn;
    }

    public String commFormat(String fmt, double val) {
        int pt = fmt.indexOf(".");
        String nmb = "";
        String pot = "";
        if (pt >= 0) {
            nmb = fmt.substring(0, pt);
            pot = fmt.substring(pt + 1);
        } else {
            nmb = fmt;
        }
        int ptl = 0;
        int typ = fmt.indexOf("$") >= 0 ? 2 : 1;
        if (pot.length() > 0)
            ptl = str2int(pot.replace("$", "").replace("z", "").replace("#", ""));
        String rtn = formatNumber(val + "", typ, ptl);
        String[] len = nmb.replace("$", "").replace("z", "").replace("#", "").split(",");
        int lens = 0;
        for (int i = 0; i < len.length; i++)
            lens += str2int(len[i]) + 1;
        if (ptl > 0)
            lens += ptl + 1;

        return String.format("%" + lens + "s", rtn);
    }

    public String formatNumber(String val) {
        DecimalFormat dcfrt = new DecimalFormat("#,##0");
        String rtn = dcfrt.format(str2double(val));
        return rtn;
    }

    public String formatNumber(String val, int typ, int pot) {
        String fmt = "#,###";
        switch (typ) {
        case 1:
            fmt = "#,##0";
            break;
        case 2:
            fmt = "$#,##0";
            break;
        }
        if (pot > 0)
            fmt += "." + "00000".substring(0, pot);
        DecimalFormat dcfrt = new DecimalFormat(fmt);

        String rtn = dcfrt.format(str2double(val));
        return rtn;
    }

    public String strFixLen(String val, int len, int typ) {
        String bef = typ == 0 ? "                               " : "00000000000000000000000000000";
        String rtn = bef + val;
        return rtn.substring(rtn.length() - len);
    }

    // ************************************************************************
    public int str2int(String val) {
        int rtn = 0;
        try {
            rtn = Integer.parseInt(val.replaceAll(",", "").trim());
        } catch (Exception e) {
            rtn = 0;
        }
        return rtn;
    }

    // ************************************************************************
    public long str2long(String val) {
        long rtn = 0;
        try {
            rtn = Long.parseLong(val.replaceAll(",", "").trim());
        } catch (Exception e) {
            rtn = 0;
        }
        return rtn;
    }

    // ************************************************************************
    public double str2double(String val) {
        double rtn = 0;
        try {
            rtn = Double.parseDouble(val.replaceAll(",", ""));
        } catch (Exception e) {
            rtn = 0;
        }
        return rtn;
    }

    public Date str2Date(String val) {
        Date rtn = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            rtn = sdf.parse(formatDate(val, 4));
        } catch (Exception e) {
            rtn = null;
        }
        return rtn;
    }

    public boolean isDate(String val) {
        boolean rtn = true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            sdf.parse(formatDate(val, 4));
        } catch (Exception e) {
            rtn = false;
        }
        return rtn;
    }

    /**********************************************************************************
     * 1:YYYmmdd 2:YYY/mm/dd 3:yyyymmdd 4:yyyy/mm/dd 5:yyyy-mm-dd
     **********************************************************************************/
    public String formatDate(String dat, int tp) {
        String st, yy, mm, dd;

        if ((dat == null) || (dat.isEmpty()))
            return "";

        String[] datim = dat.split(" ");
        String tim = datim.length > 1 ? datim[1] : "";
        tim = tim.replace(":", "");

        dat = datim[0];
        st = dat.replace("-", "").replace("/", "");
        if (st.length() < 7 || st.length() > 8)
            return st;

        yy = left(st, st.length() - 4);
        mm = mid(st, st.length() - 3, 2);
        dd = right(st, 2);

        switch (tp) {
        case 1:
            if (yy.length() == 4)
                yy = (str2int(yy) - 1911) + "";
            st = yy + mm + dd;
            break;
        case 2:
            if (yy.length() == 4)
                yy = (str2int(yy) - 1911) + "";
            st = yy + "/" + mm + "/" + dd;
            break;
        case 3:
            if (yy.length() < 4)
                yy = (str2int(yy) + 1911) + "";
            st = yy + mm + dd;
            break;
        case 4:
            if (yy.length() < 4)
                yy = (str2int(yy) + 1911) + "";
            st = yy + "/" + mm + "/" + dd;
            break;
        case 5:
            if (yy.length() < 4)
                yy = (str2int(yy) + 1911) + "";
            st = yy + "-" + mm + "-" + dd;
            break;
        case 6:
            if (yy.length() == 4)
                yy = (str2int(yy) - 1911) + "";
            st = yy + "年" + mm + "月" + dd + "日";
            break;
        }

        return st;
    }

    public String left(String str, int ln) {
        if (ln < 1)
            return "";
        if (ln > str.length())
            return str;
        return str.substring(0, ln);
    }

    public String mid(String str, int ps, int ln) {
        if ((ln < 1) || (ps < 1))
            return "";
        if ((ps + ln) > str.length())
            return str.substring(ps - 1);
        return str.substring(ps - 1, ps - 1 + ln);
    }

    public String right(String str, int ln) {
        if (ln < 1)
            return "";
        if (ln > str.length())
            return str;
        return str.substring(str.length() - ln);
    }

    public int txtLen(String str) {
        int len = 0;
        for (int i = 0; i < str.length(); i++) {
            int acsii = str.charAt(i);
            len += (acsii < 0 || acsii > 128) ? 2 : 1;
        }
        return len;
    }

    // *********************************************************************************
    public String startVouch(String vouchType, String stdVouchCd) throws Exception {

        String temp = "";
        String hSystemVouchType = vouchType;
        hGsvhStdVouchCd = stdVouchCd;

        selectSQL = "vouch_date";
        daoTable = "ptr_businday";
        // whereStr = " where rownum <2";

        if (selectTable() > 0) {
            hSystemVouchDate = getValue("vouch_date");
        } else {
            showLogMessage("E", "", "select ptr_businday not found");
            return ("error");
        }

        int hSystemRefnoSeq = 0;

        dateTime();
        locDate  = sysDate;
        locTime  = sysTime;
if(DEBUG_F == 1) showLogMessage("I", "", " Insert Time start pub=["+pubDate+"]" + pubTime);
        if(pubDate.length() > 0)
          {
           locDate  = pubDate;
           locTime  = pubTime;
          }
if(DEBUG_F == 1) showLogMessage("I", "", " Insert Time start=["+locDate+"]" + locTime);

        selectSQL = "to_number(nvl(max(substr(refno,4,3)),0))  refno_seq";
        daoTable = "gen_vouch";
        whereStr = " where  tx_date=?";
        whereStr += " and  refno like 'UB'||?||'%'";
        setString(1, hSystemVouchDate);
        setString(2, hSystemVouchType);
        selectTable();
        if (getValueInt("refno_seq") > 0) {
            hSystemRefnoSeq = getValueInt("refno_seq");
        } else {
            hSystemRefnoSeq = 0;
        }

        if (hSystemRefnoSeq == 999) {

            selectSQL = " to_number(nvl(max(substr(refno,4,3)), 0))  refno_seq";
            daoTable = "gen_vouch";
            whereStr = " where  tx_date=?";
            whereStr += " and  refno like 'UB0'||'%'";
            setString(1, hSystemVouchDate);
      //    setString(2, h_system_vouch_type);
            int sqlca = selectTable();
                sqlca = getValueInt("refno_seq");
            hSystemRefnoSeq = getValueInt("refno_seq");
            if (sqlca == 0)        { hSystemRefnoSeq = 0; }
            hSystemVouchType = "0";
        }
        temp = String.format("UB%1.1s%03d", hSystemVouchType, (hSystemRefnoSeq + 1));
        hVoucRefno = temp;

        dateTime();

        return temp;

    }

    // *********************************************************************************
    public int detailVouch(String acNo, int seqNo, double amt) throws Exception {
        return detailVouch(acNo, seqNo, amt, "");
    }
    // *********************************************************************************
    public int detailVouch(String acNo, int seqNo, double amt, String currCode) throws Exception {
        String hEngName = "";
        String hBusinssChiDate = "";
        String hTempAcBriefName = "";
        String cDate = "";
        String tempX06 = "";
        String szTmp = "", szBuffer = "";
        String hMemo3FlagCom = "";
        String hVoucAcNo = "";
        double hVoucAmt = 0;
        int hGsvhDbcrSeq = 0;
        String hAccmMemo3Flag = "";
        String hAccmDrFlag = "";
        String hAccmCrFlag = "";
        String hVoucMemo1 = "";
        String hVoucMemo2 = "";
        String hVoucMemo3 = "";
  //    String h_gsvh_dbcr  = "";
        String blk          = "　";
        String normalFlag  = "Y";

        String rptId   = hGsvhModWs;
        String rptName = hGsvhModWs;

        if (DEBUG_F == 1) showLogMessage("I", "", "888 RPT_ID=[" + rptId + "]" + rptName );

        if(currCode.equals("") || currCode.equals("TWD"))  
           currCode = "901";

        if(currCode.equals("SPE"))  
          {
           currCode   = "901";
           normalFlag = "N";
          }
        if(hGsvhCurr.length() < 1)
          {
           selectSQL = "curr_code_gl";
           daoTable  = "ptr_currcode";
           whereStr  = " where curr_code  = ?";
           setString(1, currCode);
           if(selectTable() > 0)  
             {
              hGsvhCurr = getValue("curr_code_gl");
             }
          }
        hVoucAcNo = acNo;
        if (currCode.equals("") == false)
            hVoucAmt = commCurrAmt(currCode, amt, 0);
        else
            hVoucAmt = amt;
        hGsvhDbcrSeq = seqNo;
        hAccmMemo3Flag = "";
        hAccmDrFlag = "";
        hAccmCrFlag = "";

        selectSQL = "memo3_flag,";
        selectSQL += "DECODE(dr_flag,'','N',dr_flag) dr_flag,";
        selectSQL += "DECODE(cr_flag,'','N',cr_flag) cr_flag";
        daoTable = "gen_acct_m";
        whereStr = " where  ac_no = ?";
        setString(1, hVoucAcNo);
        if (selectTable() > 0) {
            hAccmMemo3Flag = getValue("memo3_flag");
            hAccmDrFlag = getValue("dr_flag");
            hAccmCrFlag = getValue("cr_flag");
        }

        if(normalFlag.compareTo("Y") == 0)
        {
         hGsvhDbcr = "";
         hVoucMemo1 = "";
         hVoucMemo2 = "";
         hVoucMemo3 = "";
         selectSQL = "dbcr,memo1, memo2,memo3";
         daoTable = "gen_sys_vouch";
         whereStr = " where  std_vouch_cd = ? and ac_no=? and dbcr_seq=?";
         setString(1, hGsvhStdVouchCd);
         setString(2, hVoucAcNo);
         setInt(3, hGsvhDbcrSeq);
         if (selectTable() > 0) {
             hGsvhDbcr  = getValue("dbcr");
             hVoucMemo1 = getValue("memo1");
             hVoucMemo2 = getValue("memo2");
             hVoucMemo3 = getValue("memo3");
         } else {
             showLogMessage("E", hVoucAcNo, "select gen_sys_vouch not found=" + hGsvhStdVouchCd + "," + hGsvhDbcrSeq);
             return (1);
         }
        }

        if (hGsvhMemo1.length() == 0) {
            hGsvhMemo1 = hVoucMemo1;
        }

        if (hGsvhMemo2.length() == 0) {
            hGsvhMemo2 = hVoucMemo2;
        }

        if ((hGsvhDbcr.equals("D") && hAccmCrFlag.equals("Y")) || 
            (hGsvhDbcr.equals("C") && hAccmDrFlag.equals("Y")) || hVoucAcNo.equals("60000300")) {
            if (hGsvhMemo3.length() == 0) {
                hGsvhMemo3 = hVoucMemo3;
            }
        }

        hGsvhDbcrSeq = 1;
        selectSQL = "max(seqno)+1 seq";
        daoTable = "gen_vouch";
        whereStr = " where  refno = ? and tx_date=? ";
        setString(1, hVoucRefno);
        setString(2, hSystemVouchDate);
        if (selectTable() > 0) {
            hGsvhDbcrSeq = getValueInt("seq");
            if (hGsvhDbcrSeq == 0)
                hGsvhDbcrSeq = 1;
        }

        setValue("BRNO"    , "3144");
        setValue("TX_DATE" , hSystemVouchDate);
        setValue("DEPT"    , "UB");
        setValue("DEPNO"   , "1");
        setValue("CURR"    , hGsvhCurr.equals("") ? "00" : hGsvhCurr);
     // setValue("CURR"    , h_gsvh_curr == "" ? "00" : h_gsvh_curr);
        setValue("REFNO"   , hVoucRefno);
        setValueInt("SEQNO", hGsvhDbcrSeq);
        setValue("AC_NO"   , hVoucAcNo);
        setValue("DBCR"    , hGsvhDbcr);
        setValueDouble("AMT" , hVoucAmt);
        setValue("ID_NO"     , hVoucIdNo);
        setValue("sys_rem"   , hVoucSysRem);
        setValue("CRT_USER"  , "SYSTEM");
        setValue("MEMO1"     , hGsvhMemo1);
        setValue("MEMO2"     , hGsvhMemo2);
        setValue("MEMO3"     , hGsvhMemo3);
        setValue("KEY_VALUE" , hGsvhMemo3);
        setValue("JRN_STATUS", "3");
        setValue("APR_USER"  , "system");
        setValue("IFRS_FLAG" , hVoucIfrsFlag);
        setValue("MOD_USER"  , "system");
        setValue("MOD_TIME"  , sysDate + sysTime);
        setValue("MOD_PGM"   , hGsvhModPgm);
        daoTable = "GEN_VOUCH";

        insertTable();

        hTempAcBriefName = "";
        hMemo3FlagCom = "";
        selectSQL = "ac_brief_name,memo3_flag";
        daoTable = "gen_acct_m";
        whereStr = " where  ac_no = ? ";
        setString(1, hVoucAcNo);
        if (selectTable() > 0) {
            hTempAcBriefName = getValue("ac_brief_name");
            hMemo3FlagCom = getValue("memo3_flag");
        }

if(DEBUG_F == 1) showLogMessage("I",""," 888 user1=["+hGsvhModUser+"]"+hGsvhRefNo.length()+",Rpt="+rptName);
        if ((((hGsvhDbcr.equals("D")) && (hAccmCrFlag.equals("Y")) ||
              (hGsvhDbcr.equals("C")) && (hAccmDrFlag.equals("Y"))) && hMemo3FlagCom.equals("Y")) || 
            (hVoucAcNo.equals("60000300"))) {
            if(hGsvhModUser.length() != 10 && hGsvhModUser.length() > 0) {
               rptName = hGsvhModUser;
               hGsvhModUser = "";
              }
            else
              {
               if(hGsvhRefNo.length() == 0)
                 {
                  hGsvhRefNo = hGsvhModUser;
                 }
              }
if(DEBUG_F == 1) showLogMessage("I",""," 888 user2=["+hGsvhModUser+"]"+hGsvhRefNo.length()+",Rpt="+rptName);

            setValue("BRNO"     , "3144");
            setValue("TX_DATE"  , hSystemVouchDate);
            setValue("DEPT"     , "UB");
            setValue("DEPNO"    , "1");
            setValue("CURR"     , hGsvhCurr);
            setValue("REFNO"    , hVoucRefno);
            setValueInt("SEQNO" , hGsvhDbcrSeq);
            setValue("AC_NO"    , hVoucAcNo);
            setValue("DBCR"     , hGsvhDbcr);
            setValueDouble("AMT", hVoucAmt);
            setValue("CRT_USER" , "SYSTEM");
            setValue("APR_USER" , "system");
            setValue("MEMO1"    , hGsvhMemo1);
            setValue("MEMO2"    , hGsvhMemo2);
            setValue("MEMO3"    , hGsvhMemo3);
            setValue("MOD_USER" , "system");
            setValue("MOD_TIME" , sysDate + sysTime);
            setValue("REF_NO"   , hGsvhRefNo);
            setValue("MOD_PGM"  , hGsvhModPgm);
            daoTable = "GEN_MEMO3";
            insertTable();

            vouchPageCnt++;
            // showLogMessage("I", "", "888 000 PAGE_CNT=" + vouch_page_cnt
            // +",SEQ="+rptSeq);
            szBuffer = "";
            szBuffer = insertStr(szBuffer, "報表名稱:", 1);
            szBuffer = insertStr(szBuffer, hGsvhModWs, 11);
            szBuffer = insertStr(szBuffer, "＊＊＊ Ｍ Ｅ Ｍ Ｏ ＊＊＊", 26);
            szBuffer = insertStr(szBuffer, "頁  次:", 58);
            szTmp = String.format("%4d", vouchPageCnt);
            szBuffer = insertStr(szBuffer, szTmp, 70);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));
        if (DEBUG_F == 1) showLogMessage("I", "", "888 report RPT_ID=[" + rptId + "]" + rptName );

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "   ", 40);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "待沖聯", 36);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "   ", 40);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            selectSQL = "substr(to_char(to_number(?)- 19110000,'0000000'),2,7) as vouch_date";
            daoTable = "ptr_businday";
            setString(1, hSystemVouchDate);
            if (selectTable() > 0) {
                hBusinssChiDate = getValue("vouch_date");
            }

            szBuffer = "";
            szBuffer = insertStr(szBuffer, "單  位:", 1);
            szBuffer = insertStr(szBuffer, "3144", 11);
            szBuffer = insertStr(szBuffer, "交易日期:", 58);

            cDate = hBusinssChiDate.substring(0, 3) + "年" + hBusinssChiDate.substring(3, 5) + "月"
                    + hBusinssChiDate.substring(5) + "日";
            szBuffer = insertStr(szBuffer, cDate, 68);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, "銷帳內容:", 1);
            szBuffer = insertStr(szBuffer, "交易序號:", 58);
            szBuffer = insertStr(szBuffer, hVoucRefno, 68);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "   ", 40);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "會計科子細目 :", 10);
            szBuffer = insertStr(szBuffer, hVoucAcNo, 25);
            szBuffer = insertStr(szBuffer, hTempAcBriefName, 35);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "摘        要 :", 10);
            szBuffer = insertStr(szBuffer, hGsvhMemo1, 25);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, hGsvhMemo2, 25);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, hGsvhMemo3, 25);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "借 貸 性 質  :", 10);
            if (hGsvhDbcr.equals("C"))
                szBuffer = insertStr(szBuffer, "DR  借方", 25);
            else
                szBuffer = insertStr(szBuffer, "CR  貸方", 25);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            selectSQL = "CURR_ENG_NAME";
            daoTable = "ptr_currcode";
            whereStr = " where  curr_code_gl = ?";
            setString(1, hGsvhCurr);
            if (selectTable() > 0) {
                hEngName = getValue("CURR_ENG_NAME");
            }

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "幣       別  :", 10);
            szBuffer = insertStr(szBuffer, hEngName, 25);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "金       額  :", 10);
          //DecimalFormat dFormat = new DecimalFormat("####,###,###.00"); 
            DecimalFormat dFormat = new DecimalFormat("####,###,##0.00");//for Mantis #8556
            szTmp = String.format("$" + dFormat.format(hVoucAmt));
            // szTmp=String.format("%,.2f", h_vouc_amt);
            szBuffer = insertStr(szBuffer, szTmp, 30);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, blk, 1);
            szBuffer = insertStr(szBuffer, "   ", 40);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "------------------------------------------------------------------------------";
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, "銷 帳 摘 要  :", 1);
            szBuffer = insertStr(szBuffer, hGsvhMemo3, 16);
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "------------------------------------------------------------------------------";
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "";
            szBuffer = insertStr(szBuffer, "經副襄理 :             主管 :    ", 1);
            szBuffer = insertStr(szBuffer, "覆核 :             經辦員 :", 50);

            String type = hVoucRefno.substring(2, 3);
            tempX06 = getVouchTypeCnName(type);

            szBuffer += tempX06;
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            szBuffer = "------------------------------------------------------------------------------";
            lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));

            int cnt_space = 27 * vouchPageCnt - rptSeq;
            // showLogMessage("I", "err_rtn :", "8888888 curr=" + rptSeq +",
            // ADD=" + cnt_space);
            for (int inti = 0; inti < cnt_space; inti++) {
                szBuffer = "";
                szBuffer = insertStr(szBuffer, blk, 1);
                lpar.add(putReport(rptId, rptName, sysDate + sysTime, rptSeq++, "0", szBuffer));
            }
            // showLogMessage("I", "", "888 111 insert curr all seq=" + rptSeq +
            // ", LPAR="+lpar.size());
            int actCnt = insertPtrBatchMemo3(lpar, vouchPageCnt);
        }

        hGsvhMemo1 = "";
        hGsvhMemo2 = "";
        hGsvhMemo3 = "";
        return (0);
    }

	public String getVouchTypeCnName(String vouchtype) {
		String vouchTypeCnName;
		switch (vouchtype) {
		case "0":
		    vouchTypeCnName = "共用";
		    break;
		case "1":
		    vouchTypeCnName = "作業";
		    break;
		case "2":
		    vouchTypeCnName = "作業";
		    break;
		case "3":
		    vouchTypeCnName = "風管";
		    break;
		case "4":
		    vouchTypeCnName = "催收";
		    break;
		case "5":
		    vouchTypeCnName = "發卡";
		    break;
		case "6":
		    vouchTypeCnName = "客服";
		    break;
		case "7":
		    vouchTypeCnName = "行銷";
		    break;
		case "8":
		    vouchTypeCnName = "授權";
		    break;
		default:
		    vouchTypeCnName = "共用";
		    break;
		}
		return vouchTypeCnName;
	}

    // ****************************************************************************************
    public double commCurrAmt(String currCode, double currAmt, int rnd) throws Exception {
        int hCurrAmtDp = 0;
        double newCurrAmt, addNum = 0, plusFlag = 1.0;
        long tempLong;
        double tempDouble = 1;

        if (currAmt < 0)
            plusFlag = -1.0;
        newCurrAmt = currAmt;
        sqlCmd = "SELECT curr_amt_dp FROM ptr_currcode WHERE  curr_code = ?";
        setString(1, currCode);
        int recordCnt = selectTable();
        if (notFound.equals("Y"))
            return currAmt;

        if (recordCnt > 0) {
            hCurrAmtDp = getValueInt("curr_amt_dp");
        }

        for (int inti = 0; inti < hCurrAmtDp; inti++)
            tempDouble = tempDouble * 10.0;
        newCurrAmt *= tempDouble;

        if (rnd > 0)
            addNum = 0.9 * plusFlag;
        else if (rnd == 0)
            addNum = 0.5 * plusFlag;
        else
            addNum = 0.0;

        tempLong = (long) (newCurrAmt + addNum);
        newCurrAmt = tempLong;
        tempDouble = 1;
        for (int inti = 0; inti < hCurrAmtDp; inti++)
            tempDouble = tempDouble / 10.0;
        newCurrAmt *= tempDouble;

        return newCurrAmt;
    }
/******************************************************************/
    public long getModSeq() throws Exception {
        selectSQL = "ecs_modseq.nextval AS MOD_SEQNO ";
        daoTable = "dual";
        selectTable();

        return (long) getValueDouble("MOD_SEQNO");
    }
/******************************************************************/
    public String seqCallBat() throws Exception {
        selectSQL = "substr(to_char(sysdate,'yyyy'),3,4) || substr(to_char(seq_callbatch.nextval,'00000000'),2,8)  AS CALL_SEQNO ";
        daoTable = "dual";
        selectTable();

        return getValue("CALL_SEQNO");
    }
/******************************************************************/
    public double getCOLSeq() throws Exception {
        selectSQL = "col_modseq.nextval AS COL_SEQNO ";
        daoTable = "dual";
        selectTable();
        return getValueDouble("COL_SEQNO");
    }

    // ****************************************************************************
    public void errRtn(String err1, String err2, String batchSeqno) throws Exception {
        String errMsg = String.format("err_rtn -> Error: [%s],[%s]\n", err1, err2);
        showLogMessage("I", "err_rtn :", errMsg + ":" + batchSeqno);

        hCallErrorDesc = err1;

        int tempSqlcode = 1;
        rollbackDataBase();

        if (batchSeqno.length() == 20)
            try {
                callbatch(1, tempSqlcode, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        // exitProgram(1);
        throw new Exception("DXC_NOSHOW_EXCEPTION : " + errMsg);
    }
  
    // ********************************************************************
    public String julDate(String juliYddd) throws Exception {
        String hJuliYddd;
        String hWestDate;
        String westDate = "";

        hJuliYddd = String.format("%4.4s", juliYddd);
        sqlCmd = "select case when  to_date(?,'yddd') > sysdate"
                + " then  to_char(add_months(to_date(?,'yddd'),-120),'yyyymmdd')"
                + " else  to_char(to_date(?,'yddd'),'yyyymmdd') end as h_west_date FROM   DUAL";
        setString(1, hJuliYddd);
        setString(2, hJuliYddd);
        setString(3, hJuliYddd);
        selectTable();

        hWestDate = getValue("h_west_date");
        westDate = String.format("%8.8s", hWestDate);
        return westDate;
    }

    public String getTmpUser(String batchSeqno) throws Exception {
        String hTempUser = "";
        sqlCmd = "select user_id ";
        sqlCmd += "from ptr_callbatch  ";
        sqlCmd += "where batch_seqno = ? ";
        setString(1, batchSeqno);
        int recordCnt = selectTable();
        if (recordCnt > 0) {
            hTempUser = getValue("user_id");
        }
        return hTempUser;
    }

    /***
     * 從id_p_seqno取idno
     * 
     * @param idPSeqno
     * @return
     * @throws Exception
     */
    public String ufIdnoId(String idPSeqno) throws Exception {
        sqlCmd = "select nvl(uf_idno_id(?), '') as id from sysibm.sysdummy1 ";
        setString(1, idPSeqno);
        if (selectTable() > 0)
            return getValue("id");
        return "";
    }

    /***
     * 從idno取id_p_seqno
     * 
     * @param idno
     * @return
     * @throws Exception
     */
    public String ufIdnoPseqno(String idno) throws Exception {
        sqlCmd = "select nvl(uf_idno_pseqno(?), '') as idpseqno from sysibm.sysdummy1 ";
        setString(1, idno);
        if (selectTable() > 0)
            return getValue("idpseqno");
        return "";
    }

    public String ufIdnoPseqno(String idno, String idNoCode) throws Exception {
        sqlCmd = "select id_p_seqno ";
        sqlCmd += "from crd_idno ";
        sqlCmd += "where id_no = ? and id_no_code = ? ";
        sqlCmd += "fetch first 1 rows only ";
        setString(1, idno);
        setString(2, idNoCode);
        if (selectTable() > 0)
            return getValue("id_p_seqno");
        return "";
    }

    public String ufAcnoKey(String pSeqno) throws Exception {
        sqlCmd = "select nvl(uf_acno_key(?), '') as acct_key from sysibm.sysdummy1 ";
        setString(1, pSeqno);
        if (selectTable() > 0)
            return getValue("acct_key");
        return "";
    }
/***********************************************************************/
    public String increaseDays(String oriDate, int addDays) throws Exception {
        int status = 0, needDays = 0;
        // int count_days=0;
        int calInt;
        String hCommTempHoliday = "";
        String hCommNewHoliday = "";
        String hCommHoliday = "";
        String newDate = "";

        if (addDays == 0) {
            newDate = String.format("%8.8s", oriDate);
            return newDate;
        }

        // if (val_date(ori_date)!=0) return(1);
        if (str2Date(oriDate) == null)
            return null;

        hCommTempHoliday = oriDate;
        if (addDays > 0) {
            calInt = 1;
        } else {
            calInt = -1;
        }

        while (status == 0) {
            sqlCmd = "select to_char(to_date(?,'yyyymmdd')+ ? days,'yyyymmdd') h_comm_new_holiday ";
            sqlCmd += "from dual";
            setString(1, hCommTempHoliday);
            setInt(2, calInt);
            if (selectTable() > 0) {
                hCommNewHoliday = getValue("h_comm_new_holiday");
            }

            sqlCmd = "select holiday from   ptr_holiday where  holiday = ?";
            setString(1, hCommNewHoliday);
            if (selectTable() > 0) {
                hCommHoliday = getValue("holiday");
                // count_days=count_days+cal_int;
            } else {
                needDays = needDays + calInt;
                if (needDays == addDays)
                    status = 1;
            }
            hCommTempHoliday = hCommNewHoliday;

        }
        newDate = String.format("%8.8s", hCommTempHoliday);
        return newDate;
    }
/***********************************************************************/
public String increase0Days(String oriDate) throws Exception 
{
        int status = 0, needDays = 0;
        int calInt;
        String hCommTempHoliday = "";
        String hCommNewHoliday = "";
        String hCommHoliday = "";
        String newDate = "";

        if (str2Date(oriDate) == null)
            return null;

        hCommTempHoliday = oriDate;
        hCommNewHoliday  = oriDate;

        calInt = 1;

        while (status == 0) 
          {
            sqlCmd = "select holiday from   ptr_holiday where  holiday = ?";
            setString(1, hCommNewHoliday);
            if (selectTable() > 0) {
                hCommHoliday = getValue("holiday");
            } else {
                    status = 1;
            }
            hCommTempHoliday = hCommNewHoliday;

            sqlCmd = "select to_char(to_date(?,'yyyymmdd')+ ? days,'yyyymmdd') h_comm_new_holiday ";
            sqlCmd += "from dual";
            setString(1, hCommTempHoliday);
            setInt(2, calInt);
            if (selectTable() > 0) {
                hCommNewHoliday = getValue("h_comm_new_holiday");
            }
          }
        newDate = String.format("%8.8s", hCommTempHoliday);

        return newDate;
}
/***********************************************************************/
    public int calDays(String bDate, String eDate) throws Exception {
        sqlCmd = "select (days(to_date(?,'yyyymmdd')) - days(to_date(?,'yyyymmdd'))) day_cnt from dual";
        setString(1, eDate);
        setString(2, bDate);
        selectTable();
        if (notFound.equals("Y")) {
            errRtn("cal_days not found!", "", "");
        }
        return getValueInt("day_cnt");
    }

    /***
     * comm_hi_cname
     * 
     * @param
     * @return
     * @throws Exception
     */
    public String commHiCname(String cname) throws Exception {
        int i, intj, sp = 0, ep = 0;

        i = intj = 0;

        byte[] bytes = cname.getBytes("MS950");
        
        while (i < bytes.length) {
            if (Byte.toUnsignedInt(bytes[i]) >= 129) {
                if ((Byte.toUnsignedInt(bytes[i]) == 161) && (Byte.toUnsignedInt(bytes[i]) == 64)) {
                } else {
                    i++;
                }
            }
            i++;
            intj++;
            if (intj == 1)
                sp = i;
            if (intj == 2)
                ep = i - 1;
            if (intj >= 2)
                break;
        }
        if (sp == ep) {
            if(ep < bytes.length)
                bytes[ep] = 'X';
            return new String(bytes , "MS950");
        } else {
            if(sp < bytes.length)
                bytes[sp] = (byte) 162;
            if(ep < bytes.length)
                bytes[ep] = (byte) ('X' + 142);
            return new String(bytes, "MS950");
        }

    }

    /***
     * 
     * @param putData
     * @return
     * @throws Exception
     */
    public String commHiCardno(String putData) throws Exception {
        String retData = comc.getSubString(putData, 0, 5) + "XXXXXX" + comc.getSubString(putData, 11);
        return retData;
    }

    /***
     * 
     * @param putData
     * @return
     * @throws Exception
     */
    public String commHiAcctno(String putData) throws Exception {
        String retData = comc.getSubString(putData, 0, 3) + "XXXX" + comc.getSubString(putData, 7);
        return retData;
    }

    /***
     * 
     * @param putData
     * @return
     * @throws Exception
     */
    public String commHiIdno(String putData) throws Exception {
        String retData = comc.getSubString(putData, 0, 2) + "XXXX" + comc.getSubString(putData, 6);
        return retData;
    }

    /***
     * 
     * @param putData
     * @return
     * @throws Exception
     */
    public String commHiEmail(String putData) throws Exception {
        String retData = "XXX" + comc.getSubString(putData, 3);
        return retData;
    }

    /***
     * 
     * @param putData
     * @return
     * @throws Exception
     */
    public String commHiTelno(String putData) throws Exception {
        String retData = comc.getSubString(putData, 0,4); 
        for(int i = 5; i<9 ; i++) {
            if (i>=putData.length()) break;
            retData += "X";
        }
        retData += comc.getSubString(putData, 8);
        return retData;
    }

    public String[] getIDInfo(String idPSeqno) {
        String[] rtn = new String[2];
        try {
            sqlCmd = "select id_no, id_no_code  from crd_idno  where id_p_seqno = ?";
            setString(1, idPSeqno);
            if (selectTable() > 0) {
                rtn[0] = getValue("id_no");
                rtn[1] = getValue("id_no_code");
            }
        } catch (Exception ex) {
        }

        return rtn;
    }

    /*************************************************************************/
    public String getTSCCSeq() throws Exception {
        String seqno = "";

        sqlCmd = "select tscc_dataseq.nextval as seqno from dual";
        if (selectTable() > 0) {
            seqno = getValue("seqno");
        } else {
            errRtn("select ecs_modseq error\n", "", "");
        }

        return (seqno);
    }

    /***************************************************************************/
    public void lpRtn(String lstr, String hSystemDateF) throws Exception {
        String hPrintName = "";
        String hRptName = lstr;

        sqlCmd = "select print_name " + "from bil_rpt_prt " + "where report_name like ? || '%' "
                + "fetch first 1 rows only ";
        setString(1, hRptName);
        if (selectTable() > 0) {
            hPrintName = getValue("print_name");
        }
        if (hPrintName.length() > 0) {
            String lpStr = String.format("lp -d %s %s/reports/%s_%s", hPrintName, comc.getECSHOME(), lstr,
                    hSystemDateF);
            // comc.systemCmd(lp_str);
        }
    }

    // *******************************************************************************
    /***
     * 
     * @param hTempType
     * @param errorCode
     * @param intCode
     * @return
     * @throws Exception
     */
    public int callbatch(int hTempType, int errorCode, int intCode) throws Exception {
        String tmpstr = "";

//test_select(1);
        if (DEBUG_F == 1)
            showLogMessage("I", "", "888 CALLBATCH=[" + hTempType + "]" + hCallBatchSeqno + "," + intCode + "," + hCallProgramCode + "," + hCallRProgramCode);

        if ((hCallBatchSeqno.length() != 20) || (hCallBatchSeqno.compareTo(comc.getECSHOME()) == 0)) {
            return (0);
        }
        dateTime();
        if (hCallRProgramCode.length() > 7) {
            if (hCallRProgramCode.substring(3, 4).equals("."))
                hCallRProgramCode = hCallRProgramCode.substring(4);
        }
        if (hCallProgramCode.length() > 7) {
            if (hCallProgramCode.substring(3, 4).equals("."))
                hCallProgramCode = hCallProgramCode.substring(4);
        }

        if ((intCode != 0) && (hCallRProgramCode.length() == 0)) {
            showLogMessage("I", "", "parameter error ,no r_program_id");
            return (1);
        }
        tmpstr = String.format("%06d", errorCode);
        hCallErrorCode = tmpstr;

if(DEBUG_F == 1) showLogMessage("I", "", "888 000 h_temp_type=" + hTempType + ",int_code=" + intCode);

        if (intCode == 0) {
            updatePtrCallbatch1(hTempType);
        } else {
            if (DEBUG_F == 1)
                showLogMessage("I", "", "888 h_temp_type=" + hTempType + "," + hCallRProgramCode);
            if (hTempType == 1) {
                updatePtrCallbatch2();
            } else {
            if (DEBUG_F == 1)
                showLogMessage("I", "", "888 001 h_temp_type=" + hTempType+ "," + hCallRProgramCode);
                if (hCallRProgramCode.length() == 0) {
                    updatePtrCallbatch1(hTempType);
                } else {
                    selectPtrCallbatch1();

            if (DEBUG_F == 1)
                showLogMessage("I", "", "888 333 pgm="+hCallProgramCode+","+hCallRProgramCode);
                    if (hCallProgramCode.compareTo(hCallRProgramCode) == 0) {
                        updatePtrCallbatch1(hTempType);
                    } else {
                        insertPtrCallbatch1();
                    }
                }
            }
        }
        if (DEBUG_F == 1)
            showLogMessage("I", "", "888 CALLBATCH Return");

        return 1;
    }

    // *************************************************************************
    public String callbatchStart(String[] args, String checkHome, String prog, String callBatchSeqno) throws Exception {
        hCallBatchSeqno = callBatchSeqno;
        hCallRProgramCode = prog;
        if (getSubString(hCallBatchSeqno, 0, 6).equals(getSubString(checkHome, 0, 6))) {
            hCallBatchSeqno = "no-call";
        }

        String hTempUser = "";
        if (hCallBatchSeqno.length() == 20) {
            hCallParameterData = hCallRProgramCode;
            for (int i = 0; i < args.length; i++) {
                hCallParameterData = hCallParameterData + " " + args[i];
            }
            callbatch(0, 0, 1);
            selectSQL = " user_id ";
            daoTable = "ptr_callbatch";
            whereStr = "WHERE batch_seqno   = ?  ";

            setString(1, hCallBatchSeqno);
            if (selectTable() > 0)
                hTempUser = getValue("user_id");
        }
        return hTempUser;
    }
    // *************************************************************************

    public void callbatchEnd() throws Exception {
        showLogMessage("I", "", hCallErrorDesc);

        if (hCallBatchSeqno.length() == 20)
            callbatch(1, 0, 1); // 1: 結束
    }

    // ************************************************************************
    public String getSubString(String str, int beginIndex, int endIndex) {
        if (beginIndex < 0)
            return "";
        if (endIndex < 0)
            return "";
        if (beginIndex >= endIndex)
            return "";
        if (str.length() > beginIndex) {
            int eIndex = Math.min(endIndex, str.length());
            return str.substring(beginIndex, eIndex);
        }
        return "";
    }

    // ************************************************************************
    public String getSubString(String str, int beginIndex) {
        if (str.length() > beginIndex) {
            return str.substring(beginIndex);
        }
        return "";
    }

    // *************************************************************************
    public int updatePtrCallbatch1(int hTempType) throws Exception {
if(DEBUG_F == 1)
 showLogMessage("I","","888 CALL update_ptr_1=["+hTempType+ "]"+hCallBatchSeqno+","+hCallRProgramCode);

        if(hTempType == 0)     hCallErrorCode = "";   //***  intial 放空值

        if (hCallRProgramCode.length() > 7) {
            if (hCallRProgramCode.substring(3, 4).equals("."))
                hCallRProgramCode = hCallRProgramCode.substring(4);
        }
        if (hCallProgramCode.length() > 7) {
            if (hCallProgramCode.substring(3, 4).equals("."))
                hCallProgramCode = hCallProgramCode.substring(4);
        }

        String hTtttRProgramCode = "";
        if(hCallRProgramCode.length() > 1)
          {hTtttRProgramCode = hCallRProgramCode;
          }
        if(hCallProgramCode.equals("BilA110") ) {
           if(hTempType == 0)     
             {
              hTtttRProgramCode = "";
             }
        }

        daoTable   = "ptr_callbatch";
        updateSQL  = " execute_date_s = decode(cast(? as int),0,to_char(sysdate,'yyyymmdd'),execute_date_s), ";
        updateSQL += " execute_time_s = decode(cast(? as int),0,to_char(sysdate,'hh24miss'),execute_time_s), ";
        updateSQL += " execute_date_e = decode(cast(? as int),0,execute_date_e,to_char(sysdate,'yyyymmdd')), ";
        updateSQL += " execute_time_e = decode(cast(? as int),0,execute_time_e,to_char(sysdate,'hh24miss')), ";
        updateSQL += " error_code     = ?,";
        updateSQL += " parameter1     = ?,";
        updateSQL += " parameter2     = ?,";
        updateSQL += " error_desc     = ?,";
        updateSQL += " proc_desc      = ?,";
        updateSQL += " proc_mark      = ?,";
        updateSQL += " proc_mark1     = ?,";
        updateSQL += " proc_mark2     = ?,";
	updateSQL += " process_flag   = 'Y', ";
        updateSQL += " parameter_data = decode(parameter_data,'',cast(? as varchar(300)),parameter_data),";
        updateSQL += " r_program_code = ? ";
        whereStr   = " where batch_seqno = ?  ";
        whereStr  += "   and error_desc  = '' ";

        setInt(1, hTempType);
        setInt(2, hTempType);
        setInt(3, hTempType);
        setInt(4, hTempType);
        setString(5, hCallErrorCode);
        setString(6, hCallParameter1);
        setString(7, hCallParameter2);
        setString(8, hCallErrorDesc);
        setString(9, hCallProcDesc);
        setString(10, hCallProcMark);
        setString(11, hCallProcMark1);
        setString(12, hCallProcMark2);
        setString(13, hCallParameterData);
//      setString(14, h_call_r_program_code);
        setString(14, hTtttRProgramCode);
        setString(15, hCallBatchSeqno);

        updateTable();

        if (notFound.equals("Y")) {
            String stderr = "update ptr_callbatch_1 " + " not found=" + hCallBatchSeqno;
            showLogMessage("I", "", stderr);
            return 1;
        }

        commitDataBase();

        return 0;
    }

    // *************************************************************************
    public int updatePtrCallbatch2() throws Exception {

        if (hCallRProgramCode.length() > 7) {
            if (hCallRProgramCode.substring(3, 4).equals("."))
                hCallRProgramCode = hCallRProgramCode.substring(4);
        }
        if (hCallProgramCode.length() > 7) {
            if (hCallProgramCode.substring(3, 4).equals("."))
                hCallProgramCode = hCallProgramCode.substring(4);
        }

if(DEBUG_F == 1)
   showLogMessage("I","","888 CALL update_ptr_2="+hCallBatchSeqno+" R_code="+hCallRProgramCode 
                        + ",P=" + hCallParameterData + ","+hCallProcDesc);

        daoTable   = "ptr_callbatch";
        updateSQL  = " execute_date_e = to_char(sysdate,'yyyymmdd'), ";
        updateSQL += " execute_time_e = to_char(sysdate,'hh24miss'),  ";
        updateSQL += " error_code     = ?,";
        updateSQL += " parameter1     = ?,";
        updateSQL += " parameter2     = ?,";
        updateSQL += " error_desc     = ?,";
        updateSQL += " proc_desc      = ?,";
        updateSQL += " proc_mark      = ?,";
        updateSQL += " proc_mark1     = ?,";
        updateSQL += " proc_mark2     = ?,";
	updateSQL += " process_flag   = 'Y', ";
        updateSQL += " parameter_data = decode(parameter_data,'',cast(? as varchar(300)),parameter_data) ";
        whereStr   = " where batch_seqno    = ?  ";
        whereStr  += "   and r_program_code = ?  ";
        whereStr  += "   and execute_date_e = '' ";

        setString(1, hCallErrorCode);
        setString(2, hCallParameter1);
        setString(3, hCallParameter2);
        setString(4, hCallErrorDesc);
        setString(5, hCallProcDesc);
        setString(6, hCallProcMark);
        setString(7, hCallProcMark1);
        setString(8, hCallProcMark2);
        setString(9, hCallParameterData);
        setString(10, hCallBatchSeqno);
        setString(11, hCallRProgramCode);

        updateTable();

        if (notFound.equals("Y")) {
            String stderr = "update ptr_callbatch_2 not found="+hCallBatchSeqno+"["+hCallRProgramCode+"]";
            showLogMessage("I", "", stderr);
            return 1;
        }
        commitDataBase();
        return 0;
    }

    // *************************************************************************
    public int insertPtrCallbatch1() throws Exception {
        dateTime();
        hCallStartTime = sysTime;
        if (DEBUG_F == 1)
            showLogMessage("I","","888 CALL insert_ptr_1="+sysDate+","+sysTime+","+hCallRProgramCode+","+hCallBatchSeqno);
        if (DEBUG_F == 1)
            showLogMessage("I","","888 CALL insert_ptr_1 start="+hCallStartDate+","+hCallParameterData);

        if (hCallRProgramCode.length() > 7) {
            if (hCallRProgramCode.substring(3, 4).equals("."))
                hCallRProgramCode = hCallRProgramCode.substring(4);
        }
        if (hCallProgramCode.length() > 7) {
            if (hCallProgramCode.substring(3, 4).equals("."))
                hCallProgramCode = hCallProgramCode.substring(4);
        }

        if (hCallStartDate.length() == 0) {
            hCallStartDate = sysDate;
            hCallStartTime = sysTime;
            if (hCallProgramCode.length() == 0) {
                hCallProgramCode = hCallRProgramCode;
            }
        }

//      setValue("batch_seqno"     , SeqCallBat());
        setValue("BATCH_SEQNO"     , hCallBatchSeqno);
        setValue("program_code"    , hCallProgramCode);
        setValue("r_program_code"  , hCallRProgramCode);
        setValue("start_date"      , hCallStartDate);
        setValue("start_time"      , hCallStartTime);
        setValue("execute_date_s"  , sysDate);
        setValue("execute_time_s"  , sysTime);
        setValue("parameter_data"  , hCallParameterData);
        setValue("user_id"         , hCallUserId);
        setValue("workstation_name", hCallWorkstationName);
        setValue("client_program"  , hCallClientProgram);
        setValue("process_flag"    , "Y");
        daoTable = "ptr_callbatch";
        insertTable();
        if (dupRecord.equals("Y")) {
            showLogMessage("I", "", "insert ptr_callbatch duplicate!");
            return 1;
        }

        commitDataBase();
        return 0;
    }

    // *************************************************************************
    public int selectPtrCallbatch1() throws Exception {

        if (DEBUG_F == 1)
            showLogMessage("I", "", "888 CALL SELECT_ptr_1 pgm="+hCallProgramCode);

        if (hCallRProgramCode.length() > 7) {
            if (hCallRProgramCode.substring(3, 4).equals("."))
                hCallRProgramCode = hCallRProgramCode.substring(4);
        }
        if (hCallProgramCode.length() > 7) {
            if (hCallProgramCode.substring(3, 4).equals("."))
                hCallProgramCode = hCallProgramCode.substring(4);
        }

        sqlCmd = "select ";
        sqlCmd += "program_code, ";
        sqlCmd += "start_date, ";
        sqlCmd += "start_time, ";
        sqlCmd += "parameter_data , ";
        sqlCmd += "user_id, ";
        sqlCmd += "workstation_name, ";
        sqlCmd += "client_program  ";
        sqlCmd += " from ptr_callbatch ";
        sqlCmd += "where batch_seqno    = ?  ";
        sqlCmd += "  and r_program_code = ''  ";

        setString(1, hCallBatchSeqno);

        selectTable();

        if (notFound.equals("Y")) {
            String stderr = "select ptr_callbatch_1 not found=" + hCallBatchSeqno;
            showLogMessage("I", "", stderr);
            return 1;
        }

        hCallProgramCode = getValue("program_code");
        hCallStartDate   = getValue("start_date");
        hCallStartTime   = getValue("start_time");
        // h_call_parameter_data = getValue("parameter_data");
        hCallUserId = getValue("user_id");
        hCallWorkstationName = getValue("workstation_name");
        hCallClientProgram = getValue("client_program");

        commitDataBase();
        return 0;
    }
    /***********************************************************************/
    public int testSelect(int idx) throws Exception 
    {
        sqlCmd  = "select count(*) as cnt_1";
        sqlCmd += "  from ptr_callbatch ";
        sqlCmd += " where batch_seqno    = ?  ";
        sqlCmd += "   and r_program_code = ''  ";
        setString(1, hCallBatchSeqno);

        selectTable();

        showLogMessage("I", "", "XXXX cnt=["+getValueInt("cnt_1")+",idx="+idx+","+hCallBatchSeqno);

        return 0;
    }
    /***********************************************************************/
    public String getIdPSeqno(String pSeqno) throws Exception {
        sqlCmd = "select id_p_seqno from act_acno where p_seqno=?";
        setString(1, pSeqno);
        selectTable();
        if (notFound.equals("Y")) {
            errRtn("select act_acno not found!", "", hCallBatchSeqno);
        }
        return getValue("id_p_seqno");
    }
    /***********************************************************************/
    /***
     * 
     * @param sDate  取得當月最後營業日
     * @param nDays  前後天數
     * @return
     * @throws Exception
     */
    public String getBusinday(String yyyymm, int inta) throws Exception {
        yyyymm = yyyymm.substring(0,6);
        
        String yyyymmdd = "";
        int inta1, inta2;
        int tYear, tMonth, tDay;

        String[] days = new String[33];
        for (int i = 0; i < days.length; i++) {
            days[i] = " ";// 33 spaces
        }

        if ((inta > 23) || (inta < -23)) {
            showLogMessage("I", "", String.format("Exceed max value 23\n"));
            return yyyymmdd;
        }
        String tYmd = String.format("%4.4s", yyyymm);
        tYear = str2int(tYmd);

        tYmd = String.format("%2.2s", yyyymm.substring(4));
        tMonth = str2int(tYmd);

        tDay = 31;
        if ((tMonth == 4) || (tMonth == 6) || (tMonth == 9) || (tMonth == 11))
            tDay = 30;
        if (tMonth == 2) {
            tDay = 28;
            int result_rem = tYear % 4;
            int res_rem = tYear % 400;
            if ((result_rem == 0) && (res_rem != 0))
                tDay = 29;
        }

        inta1 = tDay;
        for (inta2 = inta1 + 1; inta2 <= 32; inta2++)
            days[inta2] = "N";
        String hTempHoliday = yyyymm;

        sqlCmd = "  SELECT substr(holiday,7,2) as date  FROM   ptr_holiday where  holiday like ? ";
        setString(1, hTempHoliday + "%");
        int recordCnt = selectTable();
        for (int i = 0; i < recordCnt; i++) {

            days[str2int(getValue("date", i))] = "N";
        }

        inta2 = 0;
        if (inta > 0)
            for (inta1 = 1; inta1 < 32; inta1++) {
                if (days[inta1].equals(" "))
                    inta2++;
                if (inta == inta2)
                    break;
            }
        if (inta < 0)
            for (inta1 = 31; inta1 > 0; inta1--) {
                if (days[inta1].equals(" "))
                    inta2++;
                if (inta * -1 == inta2)
                    break;
            }
        yyyymmdd = String.format("%6.6s%02d", yyyymm, inta1);
        return yyyymmdd;
    }

    /**
     * @param programCode
     * @param rProgramCode
     * @param startDate
     * @return
     * @throws Exception
     *******************************************************/
    public boolean isOnlineCallBatchActive(String programCode, String rProgramCode, String startDate)
            throws Exception {

        sqlCmd = "select count(*) count ";
        sqlCmd += " from ptr_callbatch ";
        sqlCmd += "where start_date = ? ";
        sqlCmd += "  and program_code = ? ";
        if (programCode.equals(rProgramCode) == false)
            sqlCmd += "  and r_program_code = ? ";
        sqlCmd += "  and execute_date_e = '' ";
        setString(1, startDate);
        setString(2, programCode);
        if (programCode.equals(rProgramCode) == false)
            setString(3, rProgramCode);
        selectTable();
        if (getValueInt("count") > 1) {
            return true;
        }
        return false;
    }

    // ** END
    // ************************************************************************
}
