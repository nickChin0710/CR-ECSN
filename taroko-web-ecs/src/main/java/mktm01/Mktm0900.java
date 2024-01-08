/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR                 DESCRIPTION                *
 * ---------  --------  -----------    ------------------------------------ *
 * 112/03/21  V1.00.00   Yang Han     Initial
 * 112/03/26  V1.00.01   machao       部分畫面調整                              *
 * 112/04/24  V1.00.02   yingdong     增修覆核相關欄位                   
 * 112/04/27  V1.00.03   machao       增覆核相關欄位                    *    *
 * 112/05/04  V1.00.04   Zuwei Su     覆核狀態不正確                    *    *
 ***************************************************************************/
package mktm01;

import mktm01.Mktm0900Func;
import ofcapp.BaseEdit;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoUpload;

// ************************************************************************
public class Mktm0900 extends BaseEdit {
    private final String PROGNAME = "稅務活動回饋參數維護 112/05/04   V1.00.04";
    String kk1, kk2, kk3, kk4;
    String km1, km2, km3, km4;
    String fstAprFlag = "";
    String orgTabName = "mkt_tax_parm";
    String controlTabName = "mkt_tax_parm";
    int qFrom = 0;
    int errorCnt = 0, recCnt = 0, notifyCnt = 0, colNum = 0;

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
        } else if (eqIgno(wp.buttonCode, "R")) {//-資料讀取-
            strAction = "R";
            dataRead();
        } else if (eqIgno(wp.buttonCode, "A")) {// 新增功能 -/
            strAction = "A";
            wp.itemSet("aud_type","A");
            insertFunc();
        } else if (eqIgno(wp.buttonCode, "U")) {/* 更新功能 */
        	 strAction = "U3";
             updateFuncU3R();
        } else if (eqIgno(wp.buttonCode, "D")) {/* 刪除功能 */
            deleteFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {/* 瀏覽功能 :skip-page*/
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {/* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "I")) {
            strAction = "I";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "L")) {/* 清畫面 */
            strAction = "";
            clearFunc();
        }


        dddwSelect();
        initButton();
    }

    // ************************************************************************
    @Override
    public void queryFunc() throws Exception {
        String exAcCode = wp.itemStr("ex_active_code");
        String txDateYear = wp.itemStr("ex_data_year");

        String lsWhere = "where 1=1";
        if (empty(exAcCode) == false) {
            lsWhere += " and active_code = :active_code_f";
            setString("active_code_f", exAcCode);
        }
        if (empty(txDateYear) == false) {
            lsWhere += " and left(purchase_date_s,4) = :purchase_date_f";
            setString("purchase_date_f", txDateYear);
        }

        //-page control-
        wp.whereStr = lsWhere;
        wp.setQueryMode();

        queryRead();
    }

    // ************************************************************************
    @Override
    public void queryRead() throws Exception {
        wp.colSet("fst_apr_flag", wp.itemStr("ex_apr_flag"));
        if (wp.itemStr("ex_apr_flag").equals("N"))
            controlTabName = orgTabName + "_t";

        wp.pageControl();

        wp.selectSQL = " "
                + "hex(a.rowid) as rowid, "
                + "a.active_code,"
                + "a.active_name,"
                + "a.active_type,"
                + "a.purchase_date_s,"
                + "a.purchase_date_e,"
                + "a.cal_def_date,"
                + "a.crt_user,"
                + "a.crt_date";

        wp.daoTable = controlTabName + " a "
        ;
        wp.whereOrder = " "
                + " order by a.active_code desc "
        ;

        pageQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }
        commfuncActType("active_type");
        wp.setPageValue();
    }

    // ************************************************************************
    @Override
    public void querySelect() throws Exception {
        fstAprFlag = wp.itemStr("fst_apr_flag");
        wp.colSet("ex_apr_flag", fstAprFlag);
        if (fstAprFlag.equals("N")) {
            controlTabName = orgTabName + "_t";
        }
        kk1 = itemKk("data_k1");
        dataRead();
    }

    // ************************************************************************
    @Override
    public void dataRead() throws Exception {

        wp.selectSQL =
                "hex(a.rowid) as rowid, "
                        + "a.active_code,"
                        + "a.active_name,"
                        + "a.active_type as kk_active_type,"
                        + "a.purchase_date_s as exDateS,"
                        + "a.purchase_date_e as exDateE,"
                        + "a.feedback_all_totcnt, "
                        + "a.feedback_emp_totcnt, "
                        + " a.feedback_nonemp_totcnt, "
                        + " a.feedback_peremp_cnt, "
                        + " a.feedback_pernonemp_cnt, "
                        + " a.feedback_id_type, "
                        + " a.gift_type, "
                        + " a.purchase_amt_s, "
                        + " a.purchase_amt_e, "
                        + "a.cal_def_date,"
                        + "a.mod_seqno,"
                        + "a.crt_user,"
                        + "a.crt_date,"
                        + "a.apr_user,"
                        + "a.apr_date";

        wp.daoTable = controlTabName + " a ";
        wp.whereStr = "where 1=1 ";
        wp.whereStr = wp.whereStr
                + sqlRowId(kk1, "a.rowid");

        pageSelect();
        if (sqlNotFind()) {
            return;
        }
        wp.colSet("aud_type", "Y");
        commfuncAudType("aud_type");
        dataReadR3R();
    }

    // ************************************************************************
    public void dataReadR3R() throws Exception {
      wp.colSet("control_tab_name", controlTabName);
      controlTabName = orgTabName + "_t";
//      controlTabName = orgTabName ;
      wp.selectSQL = "hex(a.rowid) as rowid, "
                      + "a.active_code,"
                      + "a.active_name,"
                      + "a.active_type as kk_active_type,"
                      + "a.purchase_date_s as exDateS,"
                      + "a.purchase_date_e as exDateE,"
                      + "a.feedback_all_totcnt, "
                      + "a.feedback_emp_totcnt, "
                      + " a.feedback_nonemp_totcnt, "
                      + " a.feedback_peremp_cnt, "
                      + " a.feedback_pernonemp_cnt, "
                      + " a.feedback_id_type, "
                      + " a.gift_type, "
                      + " a.purchase_amt_s, "
                      + " a.purchase_amt_e, "
                      + "a.cal_def_date,"
                      + "a.aud_type,"
                      + "a.mod_seqno,"
                      + "a.crt_user,"
                      + "a.crt_date,"
                      + "a.apr_user,"
                      + "a.apr_date";

      wp.daoTable = controlTabName + " a ";
      wp.whereStr = "where 1=1 " + sqlCol(wp.colStr("active_code"), "a.active_code");

      pageSelect();
      if (sqlNotFind()) {
        wp.notFound = "";
        return;
      }
      if (wp.respHtml.indexOf("_detl") > 0)
          wp.colSet("btnStore_disable","");
      wp.colSet("control_tab_name", controlTabName);
      commfuncAudType("aud_type");
    }
    
    // ************************************************************************
    public void updateFuncU3R() throws Exception {
      qFrom = 0;
      kk1 = itemKk("ROWID");
      km1 = wp.itemStr("active_code");
      fstAprFlag = wp.itemStr("ex_apr_flag");
      if (!wp.itemStr("aud_type").equals("Y")) {
        strAction = "U";
        updateFunc();
        if (rc == 1) {
            dataReadR3R();
          }
      } else {
        km1 = wp.itemStr("active_code");
        strAction = "A";
        wp.itemSet("aud_type", "U");
        insertFunc();
        if (rc == 1)
          dataRead();
      }
      wp.colSet("fst_apr_flag", fstAprFlag);
    }

    // ************************************************************************
    
    void commfuncActType(String cde1) {
        if (cde1 == null || cde1.trim().length() == 0)
            return;
        String[] cde = {"1", "2", "3", "4"};
        String[] txt = {"綜所稅", "地價稅", "牌照稅", "房屋稅"};

        for (int ii = 0; ii < wp.selectCnt; ii++) {
            wp.colSet(ii, "comm_func_" + cde1, "");
            for (int inti = 0; inti < cde.length; inti++)
                if (wp.colStr(ii, cde1).equals(cde[inti])) {
                    wp.colSet(ii, "commfunc_" + cde1, txt[inti]);
                    break;
                }
        }
    }

    //************************************************************************
    void commfuncAudType(String cde1) {
        if (cde1 == null || cde1.trim().length() == 0)
            return;
        String[] cde = {"Y", "A", "U", "D"};
        String[] txt = {"未異動", "新增待覆核", "更新待覆核", "刪除待覆核"};

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
    @Override
    public void saveFunc() throws Exception {
        Mktm0900Func func = new Mktm0900Func(wp);
        rc = func.dbSave(strAction);
        if (rc != 1) {
            alertErr2(func.getMsg());
        }
        sqlCommit(rc);
    }

    // ************************************************************************
    @Override
    public void initButton() {
        if ((wp.respHtml.indexOf("_detl") > 0)) {
            wp.colSet("btnUpdate_disable", "");
            wp.colSet("btnDelete_disable", "");
            btnModeAud();
        }
        int rr = 0;
        rr = wp.listCount[0];
        wp.colSet(0, "IND_NUM", "" + rr);
    }

    // ************************************************************************
    @Override
    public void dddwSelect() {
        String ls_sql = "";
        try {
            if ((wp.respHtml.equals("mktm0900"))) {
                wp.initOption = "--";
                wp.optionKey = "";
                if (wp.colStr("ex_active_code").length() > 0) {
                    wp.optionKey = wp.colStr("ex_active_code");
                }
                this.dddwList("dddw_active_code", "mkt_tax_parm", "trim(active_code)",
                        "trim(active_name)", " order by purchase_date_s desc");
            }
        } catch (Exception ex) {
        }
    }
}