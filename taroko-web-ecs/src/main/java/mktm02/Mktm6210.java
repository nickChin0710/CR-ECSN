/***************************************************************************
*                                                                          *
*                              MODIFICATION LOG                            *
*                                                                          *
*    DATE    VERSION     AUTHOR                 DESCRIPTION                *
* ---------  --------  -----------    ------------------------------------ *
* 112/03/07  V1.00.00   Zuwei Su      Initial                              *
* 112/03/13  V1.00.01   Zuwei Su      測試問題修訂                              *
***************************************************************************/
package mktm02;

import java.util.ArrayList;
import java.util.HashMap;
import ofcapp.BaseEdit;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoCommon;

// ************************************************************************
public class Mktm6210 extends BaseEdit {

    private final String PROGNAME = "通路類別代碼維護程式 112/03/13  V1.00.01";
    busi.ecs.CommFunction comm = new busi.ecs.CommFunction();
    busi.ecs.CommRoutine comr = null;
    Mktm6210Func func = null;
    String rowid;
    String channelTypeId;
    String fstAprFlag = "";
    String orgTabName = "mkt_chantype_parm";
    String controlTabName = "";
    int qFrom = 0;
    String tranSeqStr = "";
    String batchNo = "";
    int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;
    int[] datachkCnt = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    String[] uploadFileCol = new String[50];
    String[] uploadFileDat = new String[50];
    String[] logMsg = new String[20];
    String upGroupType = "0";

    java.util.Map<String, String> txCodeSelMap = new java.util.HashMap<String, String>() {
        {
            put("1", "指定");
            put("2", "排除");
        }
    };
    java.util.Map<String, String> txDescTypMap = new java.util.HashMap<String, String>() {
        {
            put("A", "中文");
            put("B", "英文");
        }
    };

    // ************************************************************************
    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
        rc = 1;

        strAction = wp.buttonCode;
        if (eqIgno(wp.buttonCode, "X")) {/* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {/* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) {// -資料讀取-
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
            strAction = "A";
            wp.itemSet("aud_type", "A");
            insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
            strAction = "U3";
            updateFuncU3R();
        } else if (eqIgno(wp.buttonCode, "I")) {/* 單獨新鄒功能 */
            strAction = "I";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
            deleteFuncD3R();
        } else if (eqIgno(wp.buttonCode, "R2")) {// 明細查詢 -/
            strAction = "R2";
            dataReadR2();
        } else if (eqIgno(wp.buttonCode, "U2")) {/* 明細更新 */
            strAction = "U2";
            updateFuncU2();
        } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page */
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
            strAction = "";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "NILL")) {/* nothing to do */
            strAction = "";
            wp.listCount[0] = wp.itemBuff("ser_num").length;
        }

        dddwSelect();
        initButton();
    }

    // ************************************************************************
    @Override
    public void queryFunc() throws Exception {
        wp.whereStr = "WHERE 1=1 "
                + sqlCol(wp.itemStr("ex_channel_type_id"), "a.channel_type_id", "like%")
                + sqlChkEx(wp.itemStr("ex_apr_flag"), "2", "");

        // -page control-
        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();
    }

    // ************************************************************************
    @Override
    public void queryRead() throws Exception {
        if (wp.colStr("org_tab_name").length() > 0)
            controlTabName = wp.colStr("org_tab_name");
        else
            controlTabName = orgTabName;
        if (wp.itemStr("ex_apr_flag").equals("N"))
            controlTabName = orgTabName + "_t";

        wp.pageControl();

        wp.selectSQL = " "
                + "hex(a.rowid) as rowid, "
                + "nvl(a.mod_seqno,0) as mod_seqno, "
                + "a.channel_type_id as channel_type_id,"
                + "a.channel_type_desc as channel_type_desc,"
                + "a.crt_user,"
                + "a.crt_date,"
                + "a.apr_user,"
                + "a.apr_date";

        wp.daoTable = controlTabName + " a ";
        wp.whereOrder = " " + " order by a.channel_type_id";

        pageQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        // list_wkdata();
        wp.setPageValue();
  }

    // ************************************************************************
    @Override
    public void querySelect() throws Exception {
        fstAprFlag = wp.itemStr("ex_apr_flag");
        if (wp.itemStr("ex_apr_flag").equals("N"))
            controlTabName = orgTabName + "_t";

        rowid = itemKk("data_k1");
        qFrom = 1;
        dataRead();
    }

    // ************************************************************************
    @Override
    public void dataRead() throws Exception {
        if (controlTabName.length() == 0) {
            if (wp.colStr("control_tab_name").length() == 0)
                controlTabName = orgTabName;
            else
                controlTabName = wp.colStr("control_tab_name");
        } else {
            if (wp.colStr("control_tab_name").length() != 0)
                controlTabName = wp.colStr("control_tab_name");
        }
        wp.selectSQL = "hex(a.rowid) as rowid,"
                + " nvl(a.mod_seqno,0) as mod_seqno, "
                + "a.channel_type_id,"
                + "a.channel_type_desc,"
                + "a.crt_user,"
                + "a.crt_date,"
                + "a.apr_user,"
                + "a.apr_date";

        wp.daoTable = controlTabName + " a ";
        wp.whereStr = "where 1=1 ";
        if (qFrom == 0) {
            wp.whereStr = wp.whereStr + sqlCol(channelTypeId, "a.channel_type_id");
        } else if (qFrom == 1) {
            wp.whereStr = wp.whereStr + sqlRowId(rowid, "a.rowid");
        }

        pageSelect();
        if (sqlNotFind()) {
            return;
        }
        if (qFrom == 0) {
            wp.colSet("aud_type", "Y");
        } else {
            wp.colSet("aud_type", wp.itemStr("ex_apr_flag"));
            wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
        }
        listWkdata();
        checkButtonOff();
        channelTypeId = wp.colStr("channel_type_id");
        commfuncAudType("aud_type");
        dataReadR3R();
    }

    // ************************************************************************
    public void dataReadR3R() throws Exception {
        wp.colSet("control_tab_name", controlTabName);
        controlTabName = orgTabName + "_t";
        wp.selectSQL = "hex(a.rowid) as rowid,"
                + " nvl(a.mod_seqno,0) as mod_seqno, "
            	+ "a.aud_type as aud_type, "
                + "a.channel_type_id as channel_type_id,"
                + "a.channel_type_desc as channel_type_desc,"
                + "a.crt_user as crt_user,"
                + "a.crt_date as crt_date,"
                + "a.apr_user as apr_user,"
                + "a.apr_date as apr_date";

        wp.daoTable = controlTabName + " a ";
        wp.whereStr = "where 1=1 " + sqlCol(channelTypeId, "a.channel_type_id");

        pageSelect();
        if (sqlNotFind()) {
            wp.notFound = "";
            return;
        }
        wp.colSet("control_tab_name", controlTabName);
        checkButtonOff();
        commfuncAudType("aud_type");
        listWkdataAft();
    }

    // ************************************************************************
    void listWkdataAft() throws Exception {
        sqlParm.clear();
        String sql1 = "select "
                    + " count(*) as column_data_cnt "
                    + " from mkt_chantype_data_t "
                    + " where 1 = 1 " 
                    + sqlCol(wp.colStr("channel_type_id"), "channel_type_id");;
        sqlSelect(sql1);
        wp.colSet("ex_total_cnt", sqlStr("column_data_cnt"));
    }

    // ************************************************************************
    void listWkdata() throws Exception {
        sqlParm.clear();
        String sql1 = "select "
                    + " count(*) as column_data_cnt "
                    + " from mkt_chantype_data "
                    + " where 1 = 1 " 
                    + sqlCol(wp.colStr("channel_type_id"), "channel_type_id");;
        sqlSelect(sql1);
        wp.colSet("ex_total_cnt", sqlStr("column_data_cnt"));
    }

    // ************************************************************************
    public void deleteFuncD3R() throws Exception {
        qFrom = 0;
        channelTypeId = wp.itemStr("channel_type_id");
        fstAprFlag = wp.itemStr("fst_apr_flag");
        if (!wp.itemStr("aud_type").equals("Y")) {
            channelTypeId = wp.itemStr("channel_type_id");
            strAction = "D";
            deleteFunc();
            if (fstAprFlag.equals("Y")) {
                qFrom = 0;
                controlTabName = orgTabName;
            }
        } else {
            strAction = "A";
            wp.itemSet("aud_type", "D");
            insertFunc();
        }
        dataRead();
        wp.colSet("fst_apr_flag", fstAprFlag);
    }

    // ************************************************************************
    public void updateFuncU3R() throws Exception {
        qFrom = 0;
        channelTypeId = wp.itemStr("channel_type_id");
        fstAprFlag = wp.itemStr("fst_apr_flag");
        if (!wp.itemStr("aud_type").equals("Y")) {
            strAction = "U";
            updateFunc();
            if (rc == 1)
                dataReadR3R();
        } else {
            channelTypeId = wp.itemStr("channel_type_id");
            strAction = "A";
            wp.itemSet("aud_type", "U");
            insertFunc();
            if (rc == 1)
                dataRead();
        }
        wp.colSet("fst_apr_flag", fstAprFlag);
    }

    // ************************************************************************
    public void dataReadR2() throws Exception {
        String bnTable = "";

        if ((wp.itemStr("channel_type_id").length() == 0) || (wp.itemStr("aud_type").length() == 0)) {
            alertErr2("鍵值為空白或主檔未新增 ");
            return;
        }
        wp.selectCnt = 1;
        this.selectNoLimit();
        if ((wp.itemStr("aud_type").equals("Y")) || (wp.itemStr("aud_type").equals("D"))) {
            buttonOff("btnUpdate_disable");
            buttonOff("newDetail_disable");
            bnTable = "mkt_chantype_data";
        } else {
            wp.colSet("btnUpdate_disable", "");
            wp.colSet("newDetail_disable", "");
            bnTable = "mkt_chantype_data_t";
        }

        wp.selectSQL = "hex(a.rowid) as rowid, "
                + "ROW_NUMBER()OVER() as ser_num, "
                + "a.mod_seqno as mod_seqno, "
                + "a.channel_type_id,"
                + "a.txcode_sel,"
                + "a.tx_desc_type,"
                + "a.tx_desc_name,"
                + "a.mccc_sel,"
                + "a.mccc_code, "
                + "b.mcc_remark as mccc_code_desc, "
                + "a.mod_user as mod_user ";
        wp.daoTable = bnTable 
                + " a left join cca_mcc_risk b on a.mccc_code =  b.mcc_code ";
        wp.whereStr = "where 1=1" 
                + " and a.channel_type_id = ? ";
        wp.whereOrder = " order by a.txcode_sel,a.tx_desc_type,a.tx_desc_name,a.mccc_sel,a.mccc_code";

        pageQuery(new Object[] {wp.itemStr("channel_type_id")});
        wp.setListCount(1);
        wp.notFound = "";

        wp.colSet("ex_total_cnt", String.format("%d", wp.selectCnt));
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            wp.colSet(ii, "comm_txcode_sel", txCodeSelMap.get(wp.colStr(ii, "txcode_sel")));
            wp.colSet(ii, "comm_tx_desc_type", txDescTypMap.get(wp.colStr(ii, "tx_desc_type")));
            wp.colSet(ii, "comm_mccc_sel", txCodeSelMap.get(wp.colStr(ii, "mccc_sel")));
        }
    }

    // ************************************************************************
    public void updateFuncU2() throws Exception {
        Mktm6210Func func = new Mktm6210Func(wp);
        int llOk = 0, llErr = 0;

        String[] optData = wp.itemBuff("opt");
        String[] txcodeSelList = wp.itemBuff("txcode_sel");
        String[] txDescTypeList = wp.itemBuff("tx_desc_type");
        String[] txDescNameList = wp.itemBuff("tx_desc_name");
        String[] mcccSelList = wp.itemBuff("mccc_sel");
        String[] mcccCodeList = wp.itemBuff("mccc_code");

        wp.listCount[0] = txcodeSelList.length;
        wp.colSet("IND_NUM", "" + txcodeSelList.length);
        // -check duplication-

//        int del2Flag = 0;
//        for (int ll = 0; ll < txcodeSelList.length; ll++) {
//            del2Flag = 0;
//            wp.colSet(ll, "ok_flag", "");
//
//            for (int intm = ll + 1; intm < key1Data.length; intm++)
//                if ((key1Data[ll].equals(key1Data[intm]))
//                        && (key2Data[ll].equals(key2Data[intm]))) {
//                    for (int intx = 0; intx < optData.length; intx++) {
//                        if (optData[intx].length() != 0)
//                            if (((ll + 1) == Integer.valueOf(optData[intx]))
//                                    || ((intm + 1) == Integer.valueOf(optData[intx]))) {
//                                del2Flag = 1;
//                                break;
//                            }
//                    }
//                    if (del2Flag == 1)
//                        break;
//
//                    wp.colSet(ll, "ok_flag", "!");
//                    llErr++;
//                    continue;
//                }
//        }

//        if (llErr > 0) {
//            alertErr("資料值重複 : " + llErr);
//            return;
//        }

        // -delete no-approve-
        if (func.dbDeleteD2() < 0) {
            alertErr(func.getMsg());
            return;
        }

        // -insert-
        int deleteFlag = 0;
        for (int ll = 0; ll < txcodeSelList.length; ll++) {
            deleteFlag = 0;
            // KEY 不可同時為空字串
//            if ((empty(txcodeSelList[ll])) && (empty(key2Data[ll])))
//                continue;

            // -option-ON-
            for (int intm = 0; intm < optData.length; intm++) {
                if (optData[intm].length() != 0)
                    if ((ll + 1) == Integer.valueOf(optData[intm])) {
                        deleteFlag = 1;
                        break;
                    }
            }
            if (deleteFlag == 1)
                continue;

            func.varsSet("txcode_sel", txcodeSelList[ll]);
            func.varsSet("tx_desc_type", txDescTypeList[ll]);
            func.varsSet("tx_desc_name", txDescNameList[ll]);
            func.varsSet("mccc_sel", mcccSelList[ll]);
            func.varsSet("mccc_code", mcccCodeList[ll]);

            if (func.dbInsertI2() == 1)
                llOk++;
            else
                llErr++;

            // 有失敗rollback，無失敗commit
            sqlCommit(llOk > 0 ? 1 : 0);
        }
        alertMsg("資料存檔處理完成  成功(" + llOk + "), 失敗(" + llErr + ")");

        // SAVE後 SELECT
        dataReadR2();
    }

    // ************************************************************************
    public void saveFunc() throws Exception {
        Mktm6210Func func = new Mktm6210Func(wp);
        if (wp.respHtml.indexOf("_detl") > 0) {
            if (!wp.colStr("aud_type").equals("Y")) {
                listWkdataAft();
            }
        }

        rc = func.dbSave(strAction);
        if (rc != 1)
            alertErr2(func.getMsg());
        log(func.getMsg());
        this.sqlCommit(rc);
    }

    // ************************************************************************
    @Override
    public void initButton() {
        if ((wp.respHtml.indexOf("_detl") > 0) || (wp.respHtml.indexOf("_nadd") > 0)) {
            wp.colSet("btnUpdate_disable", "");
            wp.colSet("btnDelete_disable", "");
            this.btnModeAud();
        }
        int rr = 0;
        rr = wp.listCount[0];
        wp.colSet(0, "IND_NUM", "" + rr);
    }

    // ************************************************************************
    @Override
    public void dddwSelect() {
        String lsSql = "";
        try {
          if ((wp.respHtml.equals("mktm6210_mctd"))) {
              wp.initOption = "";
              wp.optionKey = "";
              this.dddwList("dddw_mcc_risk", "cca_mcc_risk", "trim(mcc_code)", "trim(mcc_remark)",
                  " where 1 = 1 ");
            }
        } catch (Exception ex) {
        }        
    }

    // ************************************************************************
    public String sqlChkEx(String exCol, String sqCond, String fileExt) {
        return "";
    }

    // ************************************************************************
    void commfuncAudType(String cde1) {
        if (cde1 == null || cde1.trim().length() == 0)
            return;
        String[] cde = {
                "Y", "A", "U", "D"
        };
        String[] txt = {
                "未異動", "新增待覆核", "更新待覆核", "刪除待覆核"
        };

        for (int ii = 0; ii < wp.selectCnt; ii++) {
            wp.colSet(ii, "comm_func_" + cde1, "");
            for (int inti = 0; inti < cde.length; inti++)
                if (wp.colStr(ii, cde1).equals(cde[inti])) {
                    wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
                    break;
                }
        }
    }

    // ************************************************************************
    // ************************************************************************
    public void checkButtonOff() throws Exception {
        if ((wp.colStr("aud_type").equals("Y")) || (wp.colStr("aud_type").equals("D"))) {
            buttonOff("uplmrcd_disable");
        } else {
            wp.colSet("uplmrcd_disable", "");
        }
        return;
    }

    // ************************************************************************
    @Override
    public void initPage() {
        buttonOff("btnmrcd_disable");
        buttonOff("uplmrcd_disable");

        return;
    }
    // ************************************************************************

} // End of class
