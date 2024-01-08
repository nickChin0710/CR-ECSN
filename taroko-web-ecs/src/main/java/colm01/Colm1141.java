/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                  DESCRIPTION                 *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/02/21  V1.00.00    Yang Bo                program init                 *
 *  112/03/11  V1.00.01    Sunny                  調整部分欄位顯示                                *
 ******************************************************************************/
package colm01;

import ofcapp.BaseAction;
import taroko.com.TarokoPDF;

public class Colm1141 extends BaseAction {
    private String lsWhere = "";
    private final String PROGNAME = "colm1141";

    @Override
    public void userAction() throws Exception {
        if (eqIgno(wp.buttonCode, "X")) {
            /* 轉換顯示畫面 */
            strAction = "new";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "Q")) {
            /* 查詢功能 */
            strAction = "Q";
            queryFunc();
        } else if (eqIgno(wp.buttonCode, "R")) {
            // -資料讀取-
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {
            /* 新增功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {
            /* 更新功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page */
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "C")) {
            // -資料處理-
            procFunc();
        } else if (eqIgno(wp.buttonCode, "XLS")) { // -Excel-
            strAction = "XLS";
//            xlsPrint();
        } else if (eqIgno(wp.buttonCode, "PDF")) { // -PDF-
            strAction = "PDF";
            pdfPrint();
        }
    }

    @Override
    public void dddwSelect() {

    }

    @Override
    public void queryFunc() throws Exception {
   	
//    	exLastStatusFlag = wp.itemStr("ex_last_status_flag");
//    	    if("Y".equals(exLastStatusFlag)) {
//    	      wp.colSet("DEFAULT_CHK", "checked");
//    	    }
    	    
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        if (!getWhereStr()) {
            return;
        }

        wp.selectSQL = " hex(a.rowid) as SER_NUM, a.file_date, b.id_no, b.chi_name, a.liad_type, a.liad_status, " +
                " a.status_date, a.tans_type, a.apply_date ";
        wp.daoTable += " col_liad_renewliqui a " +
                " inner join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
        wp.whereOrder += " order by apply_date asc ";

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr("查無資料");
            return;
        }
        listWkdata(wp.selectCnt);
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
    public void procFunc() throws Exception {

    }

    @Override
    public void initButton() {

    }

    @Override
    public void initPage() {

    }

    boolean getWhereStr() {
        sqlParm.clear();
        String exId = wp.itemStr("ex_id");
        String exFileDateS = wp.itemStr("ex_file_date_s");
        String exFileDateE = wp.itemStr("ex_file_date_e");
        String exLiadType = wp.itemStr("ex_liad_type");
        String exApplyDateS = wp.itemStr("ex_apply_date_s");
        String exApplyDateE = wp.itemStr("ex_apply_date_e");
        String exLiadStatus = wp.itemStr("ex_liad_status");
        String exStatusDateS = wp.itemStr("ex_status_date_s");
        String exStatusDateE = wp.itemStr("ex_status_date_e");

        if (empty(exId) && empty(exFileDateS) && empty(exFileDateE) && empty(exApplyDateS) && empty(exApplyDateE) &&
                empty(exLiadStatus) && empty(exStatusDateS) && empty(exStatusDateE)) {
            alertErr("查詢條件不可全部空白");
            return false;
        }

        lsWhere = " where 1 = 1 ";
        if (!empty(exId)) {
            lsWhere += " and b.id_no = ? ";
            setString(exId);
        }
        if (!empty(exFileDateS)) {
            lsWhere += " and a.file_date >= ? ";
            setString(exFileDateS);
        }
        if (!empty(exFileDateE)) {
            lsWhere += " and a.file_date <= ? ";
            setString(exFileDateE);
        }
        if (!empty(exLiadType)) {
            if (eqIgno(exLiadType, "0")) {
                lsWhere += " and a.liad_type in ('3', '4') ";
            } else if (eqIgno(exLiadType, "1")) {
                lsWhere += " and a.liad_type = '3' ";
            } else if (eqIgno(exLiadType, "2")) {
                lsWhere += " and a.liad_type = '4' ";
            }
        }
        if (!empty(exApplyDateS)) {
            lsWhere += " and a.apply_date >= ? ";
            setString(exApplyDateS);
        }
        if (!empty(exApplyDateE)) {
            lsWhere += " and a.apply_date <= ? ";
            setString(exApplyDateE);
        }
        if (!empty(exLiadStatus)) {
            lsWhere += " and a.liad_status = ? ";
            setString(exLiadStatus);
        }
        if (!empty(exStatusDateS)) {
            lsWhere += " and a.status_date >= ? ";
            setString(exStatusDateS);
        }
        if (!empty(exStatusDateE)) {
            lsWhere += " and a.status_date <= ? ";
            setString(exStatusDateE);
        }

        wp.whereStr = lsWhere;
        return true;
    }

    void listWkdata(int selectCnt) throws Exception {
        String[] cde = new String[]{};
        String[] txt = new String[]{};
        String[] TypeCde = new String[]{};
        String[] TypeTxt = new String[]{};
        for (int ii = 0; ii < selectCnt; ii++) {
            String liadType = wp.colStr(ii, "liad_type");
            String liadStatus = wp.colStr(ii, "liad_status");
            
            TypeCde = new String[]{"3", "4"};  
            TypeTxt = new String[]{"3.更生", "4.清算"}; 
            
            if (eqIgno(liadType, "3")) {            	         
            	cde = new String[]{"1", "2", "3", "4", "5", "6", "7"};
                txt = new String[]{"1.更生開始", "2.更生撤回", "3.更生認可", "4.更生履行完畢", "5.更生裁定免責", "6.更生調查程序", "7.更生駁回"};
            } else if (eqIgno(liadType, "4")) {
//                cde = new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};
//                txt = new String[]{"1.清算程序開始", "2.清算程序終止", "3.清算程序開始同時終止", "4.清算撤銷免責", "5.清算調查程序", "6.清算駁回", "7.清算撤回", "8.清算復權"};                   
              cde = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
              txt = new String[]{"A.清算程序開始", "B.清算程序終止", "C.清算程序開始同時終止", "D.清算撤銷免責", "E.清算調查程序", "F.清算駁回", "G.清算撤回", "H.清算復權"};
            	
            }
            wp.colSet(ii, "liad_type", commString.decode(liadType, TypeCde, TypeTxt));
            wp.colSet(ii, "liad_status", commString.decode(liadStatus, cde, txt));

            String tansType = wp.colStr(ii, "tans_type");
            cde = new String[]{"A", "C", "D"};
            txt = new String[]{"A.新增", "C.異動", "D.刪除"};
            wp.colSet(ii, "m_code", commString.decode(tansType, cde, txt));
            wp.colSet(ii, "wk_id_cname", wp.colStr(ii, "id_no") + "_" + wp.colStr(ii, "chi_name"));
        }
    }

    void pdfPrint() throws Exception {
        if (!getWhereStr()) {
            wp.respHtml = "TarokoErrorPDF";
            return;
        }
        wp.reportId = PROGNAME;
        wp.pageRows = 9999;

        wp.colSet("reportName", PROGNAME.toUpperCase());
        wp.colSet("loginUser", wp.loginUser);
        queryFunc();
        // list_wkdata 之後才有值..
//        String strRenewLiquSts = "";
//        if (empty(wp.itemStr("exRenewLiquSts")) == false) {
//            String strIdKey = "";
//            String strIdCode = "";
//            strIdKey = wp.itemStr("exRenewLiquSts").substring(0, 1);
//            strIdCode = wp.itemStr("exRenewLiquSts").substring(2);
//            strRenewLiquSts =
//
//
//                    "    更生/清算進度: " + commString.decode(strIdKey + "_" + strIdCode, strCde2, strTxt2);
//            // wf_ColLiabIdtabList(strIdCode,strIdKey);
//        }
//        String cond1 = "收件日期: " + commString.strToYmd(wp.itemStr("exRecvDateS")) + " -- "
//                + commString.strToYmd(wp.itemStr("exRecvDateE")) + strRenewLiquSts;
//        wp.colSet("cond_1", cond1);

        TarokoPDF pdf = new TarokoPDF();
        wp.fileMode = "N";
        pdf.excelTemplate = PROGNAME + ".xlsx";
        pdf.sheetNo = 0;
        pdf.pageCount = 28;
        pdf.procesPDFreport(wp);
        pdf = null;
    }
}
