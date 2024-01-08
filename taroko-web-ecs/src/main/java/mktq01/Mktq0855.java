/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR               DESCRIPTION                  *
 * ---------  --------  -----------    ------------------------------------ *
 * 112/04/18  V1.00.00   Yang Bo             Program Initial                *
 * 112/04/25  V1.00.01   Machao          明細增’通路活動群組說明’ 欄位    *
 * 112/05/09  V1.00.02   Yang Bo                增回饋類別欄位              *
 * 112/05/16  V1.00.03   Grace Huang     增一"回饋類別" 選項, 3.擇一回饋    *
 * 112/08/29  V1.00.04   Bo Yang         媒體檔依通路活動群組代號篩選後產生 *
 * 112/09/15  V1.00.05   Machao         下拉列表調整                        *
 ***************************************************************************/
package mktq01;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Mktq0855 extends BaseEdit {
    private final String orgTabName = "mkt_channelgp_list";
    private String controlTabName = "";
    busi.DataSet ds1 = new busi.DataSet();
    String newLine = "\n";
    int fo = 0;

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
        } else if (eqIgno(wp.buttonCode, "R")) {//-資料讀取-
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "MEDIAFILE")) {/* 產生媒體檔 */
            strAction = "U";
            mediafileProcess();
        } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
            strAction = "A";
            insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {/*  更新功能 */
            strAction = "U";
            updateFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
            deleteFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page*/
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

    @Override
    public void queryFunc() throws Exception {
        if (queryCheck() != 0) {
            return;
        }

        wp.whereStr = "WHERE 1=1 "
                + sqlCol(wp.itemStr("ex_active_group_id"), "a.active_group_id")
                + sqlCol(wp.itemStr("ex_id_no"), "a.id_no");

        wp.queryWhere = wp.whereStr;
        wp.setQueryMode();

        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        if (wp.colStr("org_tab_name").length() > 0) {
            controlTabName = wp.colStr("org_tab_name");
        } else {
            controlTabName = orgTabName;
        }

        wp.pageControl();

        wp.selectSQL = " "
                + "a.active_group_id, "
                + "c.active_group_desc, "
                + "a.feedback_type, "
                + "a.id_no, "
                + "b.chi_name, "
                + "a.fund_amt,"
                + "a.fund_date";
        wp.daoTable = controlTabName + " a "
                + " left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
                + " left join mkt_channelgp_parm c on a.active_group_id = c.active_group_id";
        wp.whereOrder = " order by a.active_group_id";

        pageQuery();
        listWkdataQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        commFeedbackType("feedback_type");
        wp.setPageValue();
    }

    @Override
    public void querySelect() throws Exception {
    }

    @Override
    public void dataRead() throws Exception {
    }

    @Override
    public void saveFunc() throws Exception {
    }

    @Override
    public void initButton() {
    }

    @Override
    public void dddwSelect() {
        try {
            if ((wp.respHtml.equals("mktq0855"))) {
                wp.initOption = "--";
                wp.optionKey = wp.colStr("ex_active_group_id");
                this.dddwList("dddw_active_group_id", "mkt_channelgp_parm", "trim(active_group_id)",
                        "trim(active_group_desc)", " where 1 = 1 ");
            }
        } catch (Exception ex) {
        }
    }

    private int queryCheck() {
        if ((itemKk("ex_active_group_id").length() == 0) && (itemKk("ex_id_no").length() == 0)) {
            alertErr("通路活動群組與身份證號二者不可同時空白");
            return (1);
        }

        return (0);
    }

    private void listWkdataQuery() {
        wp.itemSet("bb_down_file_name", wp.itemStr("ex_active_group_id") + "_" + wp.sysDate + wp.sysTime + ".txt");
        wp.colSet("bb_down_file_name", wp.itemStr("ex_active_group_id") + "_" + wp.sysDate + wp.sysTime + ".txt");
    }

    public void mediafileProcess() throws Exception {
        if (wp.colStr("org_tab_name").length() > 0) {
            controlTabName = wp.colStr("org_tab_name");
        } else {
            controlTabName = orgTabName;
        }

        wp.listCount[0] = wp.itemBuff("ser_num").length;

        if (wp.itemStr("bb_down_file_name").length() == 0) {
            alertErr("尚未產生資料, 無法產生檔案");
            return;
        }

        if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
            return;
        }

        wp.dateTime();
        String oriFileName = wp.itemStr("bb_down_file_name");
        String fileName;
        int intk = oriFileName.lastIndexOf('.');
        if (intk >= 0) {
            fileName = oriFileName.substring(0, intk) + "_" + wp.sysDate + wp.sysTime + oriFileName.substring(intk);
        } else {
            fileName = wp.itemStr("bb_down_file_name") + "_" + wp.sysDate + wp.sysTime;
        }
        fileName = fileName + ".csv";
        wp.colSet("zz_media_file", fileName);
        TarokoFileAccess tf = new TarokoFileAccess(wp);
        fo = tf.openOutputText(fileName, "MS950");

        if (fo == -1) {
            return;
        }

        String outData;

        setSelectLimit(99999);
        String sqlStr;
        sqlStr = "select a.active_group_id, "
                + " a.id_no, "
                + " b.chi_name, "
                + " a.fund_amt,"
                + " a.fund_date"
                + " from " + controlTabName + " a "
                + " left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
                + " where a.active_group_id = ? "
                + " order by a.active_group_id ";

        busi.FuncBase fB = new busi.FuncBase();
        fB.setConn(wp);
        fB.setSelectLimit(0);
        ds1.colList = fB.sqlQuery(sqlStr, new Object[]{wp.itemStr("ex_active_group_id")});
        sqlParm.clear();

        for (int inti = 0; inti < ds1.listRows(); inti++) {
            ds1.listFetch(inti);

            if (inti == 0) {
                outData = "";
                outData = outData + "通路活動群組,";
                outData = outData + "身分證號,";
                outData = outData + "姓名,";
                outData = outData + "現金回饋金額,";
                outData = outData + "現金回饋日期";
                tf.writeTextFile(fo, outData + newLine);
            }
            outData = "";
            outData = outData + checkColumn("active_group_id");
            outData = outData + checkColumn("id_no");
            outData = outData + checkColumn("chi_name");
            outData = outData + checkColumn("fund_amt");
            outData = outData + ds1.colStr("fund_date");
            tf.writeTextFile(fo, outData + newLine);
        }
        tf.closeOutputText(fo);
        alertMsg("檔案 [" + fileName + "] 已經產生,累計下載 " + ds1.listRows() + " 筆!");

        wp.colSet("zz_full_media_file", "href=./WebData/work/" + fileName + "");
        wp.colSet("img_display", " src=images/downLoad.gif height=\"30\" ");
    }

    public String checkColumn(String s1) {
        return ds1.colStr(s1) + ",";
    }

    private void commFeedbackType(String cde1) {
        if (cde1 == null || cde1.trim().length() == 0) {
            return;
        }
        String[] cde = {"1", "2", "3"};
        String[] txt = {"擇優回饋", "回饋上限", "擇一回饋"};

        for (int ii = 0; ii < wp.selectCnt; ii++) {
            wp.colSet(ii, "comm_" + cde1, "");
            for (int inti = 0; inti < cde.length; inti++) {
                if (wp.colStr(ii, cde1).equals(cde[inti])) {
                    wp.colSet(ii, "comm_" + cde1, txt[inti]);
                    break;
                }
            }
        }
    }
}
