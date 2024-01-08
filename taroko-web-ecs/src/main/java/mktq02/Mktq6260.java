/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 112/03/28  V1.00.00    Yang Bo              Program Initial              *
 * 112/04/10  V1.00.01    Yang Bo        .TXT download on popping window    *
 * 112/05/05  V1.00.02    Zuwei Su     產檔格式从PTR_SYS_PARM查询，檔名取PTR_SYS_PARM的欄位wf_value2，“優惠別”欄位值修訂    *
 * 112/05/08  V1.00.03    Zuwei Su     產檔格式選擇後自動帶出檔名，產生媒體檔檔名需替換yyyymm為實際年月   *
 * 112/08/10  V1.00.04    Grace        原wf_parm='OUTFILE_PARM', 改為'INOUTFILE_PARM'  *
 ***************************************************************************/
package mktq02;

import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Mktq6260 extends BaseEdit {
    private final String PROGNAME = "首刷禮活動回饋產檔作業 112/03/24 V1.00.00";
    private final String orgTabName = "mkt_fstp_carddtl";
    String controlTabName = "";
    busi.DataSet ds1 = new busi.DataSet();
    private int fo = 0;
    private static final String LINE_SEPARATOR = "\n";

    @Override
    public void actionFunction(TarokoCommon wr) throws Exception {
        super.wp = wr;
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
        } else if (eqIgno(wp.buttonCode, "MEDIAFILE")) {/* 產生媒體檔 */
            strAction = "U";
            mediaFileProcess();
        } else if (eqIgno(wp.buttonCode, "AJAX")) {/* AJAX */
            strAction = "ajax";
            String method = wp.itemStr("method");
            switch (method) {
                case "docname":
                    ajaxGetDocname();
                    break;
                default:
                    break;
            }
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
                + sqlCol(wp.itemStr("ex_active_code"), "a.active_code");

        //-page control-
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

        wp.selectSQL = " hex(a.rowid) as rowid, "
                + "a.active_code, "
                + "a.group_code, "
                //+ "isnull(decode(a.acct_type, '90', c.id_no, b.id_no), '') as id_no, "
                //+ "isnull(decode(a.acct_type, '90', c.chi_name, b.chi_name), '') as chi_name, "
                + "NVL(decode(a.acct_type, '90', c.id_no, b.id_no), '') as id_no, "
                + "NVL(decode(a.acct_type, '90', c.chi_name, b.chi_name), '') as chi_name, "
                + "a.card_no, "
                + "a.issue_date, "
                + "d.bill_sending_zip, "
                + "trim(d.bill_sending_addr1)||trim(d.bill_sending_addr2)||trim(d.bill_sending_addr3)||" +
                "trim(d.bill_sending_addr4)||trim(d.bill_sending_addr5) as bill_sending_addr, "
                + "d.e_mail_ebill, "
                + "b.cellar_phone, "
                + "a.dest_cnt, "
                + "a.dest_amt ";
        wp.daoTable = controlTabName + " a "
                + " left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
                + " left join dbc_idno c on a.id_p_seqno = c.id_p_seqno "
                + " left join act_acno d on a.p_seqno = d.p_seqno ";
        wp.whereOrder = " order by a.active_code, decode(a.acct_type, '90', c.id_no, b.id_no) ";

        pageQuery();
//        listWkdataQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

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
            if ((wp.respHtml.equals("mktq6260"))) {
                wp.initOption = "--";
                wp.optionKey = "";
                if (wp.colStr("ex_active_code").length() > 0) {
                    wp.optionKey = wp.colStr("ex_active_code");
                }
                this.dddwList("dddw_active_code", "mkt_fstp_parm", "trim(active_code)",
                        "trim(active_name)", "order by active_code");
                
                wp.initOption = "--";
                wp.optionKey = "";
                if (wp.colStr("ex_doc_type").length() > 0) {
                    wp.optionKey = wp.colStr("ex_doc_type");
                }
                this.dddwList("dddw_doc_type", "ptr_sys_parm", "trim(wf_key)",
                        "trim(wf_key||'_'|| wf_desc)", "where wf_parm = 'INOUTFILE_PARM' and wf_key like 'MKTFSTP%'");
            }
        } catch (Exception ignored) {
        }
    }

    public int queryCheck() {
        if (wp.itemStr("ex_active_code").length() == 0) {
            alertErr("活動代號不可空白");
            return (1);
        }
        if (wp.itemStr("ex_doc_type").length() == 0) {
            alertErr("產檔格式不可空白");
            return (1);
        }

        return (0);
    }

    public void listWkdataQuery() {
        String exDocType = wp.itemStr("ex_doc_type");
        wp.sqlCmd="Select WF_VALUE2 "
                + "From ptr_sys_parm "
                + "where 1=1 "
                + "and wf_parm = 'INOUTFILE_PARM' "
                + "and wf_key = ?"; // -- 為’產檔格式’ 欄位值
        pageSelect(new Object[] {exDocType});
        if (sqlRowNum > 0) {
            String wfvalue2 = wp.colStr("WF_VALUE2");
            wp.itemSet("bb_down_file_name", wfvalue2);
            wp.colSet("bb_down_file_name", wfvalue2);
        }
    }
    
    private void ajaxGetDocname() throws Exception {
        listWkdataQuery();
        wp.addJSON("bb_down_file_name", wp.itemStr("bb_down_file_name"));
    }

    // ************************************************************************
    public void mediaFileProcess() throws Exception {
        String exDoctype = wp.itemStr("ex_doc_type");
        String col1 = "";
        switch (exDoctype) {
            case "MKTFSTP10001":
                col1 = "COUPON1";
                break;
            case "MKTFSTP10002":
                col1 = "COUPON2";
                break;
            default:
                break;
        }
        String bbDownFileName = wp.itemStr("bb_down_file_name");
        wp.listCount[0] = wp.itemBuff("ser_num").length;

        if (bbDownFileName.length() == 0) {
            alertErr("尚未產生資料, 無法產生檔案");
            return;
        }

        if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
            return;
        }

        wp.dateTime();
        String fileName;
        int intk = bbDownFileName.lastIndexOf('.');
        if (intk >= 0) {
            fileName = bbDownFileName.substring(0, intk);
        } else {
            fileName = bbDownFileName + "_" + wp.sysDate + wp.sysTime;
        }
        String tmpFilename = fileName.toLowerCase();
        int idx = tmpFilename.lastIndexOf("_yyyymm");
        if (idx >= 0) {
            fileName = fileName.substring(0, idx) + "_" + wp.sysDate.substring(0,6) + fileName.substring(idx+7);
        }
        fileName = fileName + ".TXT";
        wp.colSet("zz_media_file", fileName);
        TarokoFileAccess tf = new TarokoFileAccess(wp);
        fo = tf.openOutputText(fileName, "MS950");

        if (fo == -1) {
            return;
        }

        if (wp.colStr("org_tab_name").length() > 0) {
            controlTabName = wp.colStr("org_tab_name");
        } else {
            controlTabName = orgTabName;
        }

        setSelectLimit(99999);
        String sqlStr = "select  "
                + "hex(a.rowid) as rowid, "
                + "a.active_code, "
                + "a.group_code, "
                //+ "isnull(decode(a.acct_type, '90', c.id_no, b.id_no), '') as id_no, "
                //+ "isnull(decode(a.acct_type, '90', c.chi_name, b.chi_name), '') as chi_name, "
                + "NVL(decode(a.acct_type, '90', c.id_no, b.id_no), '') as id_no, "
                + "NVL(decode(a.acct_type, '90', c.chi_name, b.chi_name), '') as chi_name, "
                + "a.card_no, "
                + "a.issue_date, "
                + "d.bill_sending_zip, "
                + "trim(d.bill_sending_addr1)||trim(d.bill_sending_addr2)||trim(d.bill_sending_addr3) as bill_sending_addr1, "
                + "trim(d.bill_sending_addr4)||trim(d.bill_sending_addr5) as bill_sending_addr2, "
                + "d.e_mail_ebill, "
                + "b.cellar_phone, "
                + "a.dest_cnt, "
                + "a.dest_amt "
                + " from " + controlTabName + " a "
                + " left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
                + " left join dbc_idno c on a.id_p_seqno = c.id_p_seqno "
                + " left join act_acno d on a.p_seqno = d.p_seqno "
                + " where 1 = 1 and a.active_code = ? "
                + " order by a.active_code, decode(a.acct_type, '90', c.id_no, b.id_no) ";

        busi.FuncBase fB = new busi.FuncBase();
        fB.setConn(wp);
        fB.setSelectLimit(0);
        ds1.colList = fB.sqlQuery(sqlStr, new Object[]{wp.itemStr("ex_active_code")});

        String outData;
        for (int inti = 0; inti < ds1.listRows(); inti++) {
            ds1.listFetch(inti);

            if (inti == 0) {
                outData = "";
                outData = outData + "優惠別,";
                outData = outData + "正卡ID,";
                outData = outData + "TYPE,";
                outData = outData + "信用卡卡號,";
                outData = outData + "開戶日,";
                outData = outData + "一般消費累積次數,";
                outData = outData + "一般消費累積金額,";
                outData = outData + "手機號碼,";
                outData = outData + "電子郵件,";
                outData = outData + "郵遞區號,";
                outData = outData + "地址1,";
                outData = outData + "地址2,";
                outData = outData + "持卡人姓名";
                tf.writeTextFile(fo, outData + LINE_SEPARATOR);
            }
            outData = "";
            outData = outData + col1 + ",";
            outData = outData + checkColumn("id_no");
            outData = outData + checkColumn("group_code");
            outData = outData + checkColumn("card_no");
            outData = outData + checkColumn("issue_date");
            outData = outData + checkColumn("dest_cnt");
            outData = outData + checkColumn("dest_amt");
            outData = outData + checkColumn("cellar_phone");
            outData = outData + checkColumn("e_mail_ebill");
            outData = outData + checkColumn("bill_sending_zip");
            outData = outData + checkColumn("bill_sending_addr1");
            outData = outData + checkColumn("bill_sending_addr2");
            outData = outData + ds1.colStr("chi_name");
            tf.writeTextFile(fo, outData + LINE_SEPARATOR);
        }
        tf.closeOutputText(fo);
        alertMsg("檔案 [" + fileName + "] 已經產生,累計下載 " + ds1.listRows() + " 筆!");

        wp.colSet("zz_full_media_file", " download="
                + fileName
                + " href=./WebData/work/"
                + fileName
                + "?response-content-type=application/octet-stream");
        wp.colSet("img_display", " src=images/downLoad.gif ");
    }

    // ************************************************************************
    public String checkColumn(String s1) {
        return ds1.colStr(s1) + ",";
    }
}
