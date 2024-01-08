/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                  DESCRIPTION                 *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/02/27  V1.00.00    Yang Bo                program init                 *
 *  112/03/02  V1.00.01    Yang Bo         update feedback_type input type     *
 *  112/03/10  V1.00.02    Yang Bo         fix feedback_type query error       *
 ******************************************************************************/
package mktq02;

import ofcapp.BaseAction;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Mktq6250 extends BaseAction {
    private String lsWhere = "";

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
        }
    }

    @Override
    public void dddwSelect() {
    }

    @Override
    public void queryFunc() throws Exception {
        wp.setQueryMode();
        queryRead();
    }

    @Override
    public void queryRead() throws Exception {
        wp.pageControl();

        if (!getWhereStr()) {
            return;
        }

        wp.selectSQL = " hex(a.rowid) as SER_NUM, a.id_no, b.chi_name, a.id_p_seqno, a.feedback_date_hilai, " +
                " a.feedback_date_kana, a.feedback_date_golden_diamond, a.feedback_date_other ";
        wp.daoTable += " mkt_fstp_carddtl_h a " +
                " inner join crd_idno b on a.id_p_seqno = b.id_p_seqno ";
        wp.whereOrder += " order by a.id_no asc ";

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr("查無資料");
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
    public void procFunc() throws Exception {
    }

    @Override
    public void initButton() {
    }

    @Override
    public void initPage() {
    }

    boolean getWhereStr() {
        String exId = wp.itemStr("ex_id");
        String[] exFeedbackTypeList = wp.itemBuff("ex_feedback_type");
        String exFeedbackDateS = wp.itemStr("ex_feedback_date_s");
        String exFeedbackDateE = wp.itemStr("ex_feedback_date_e");

        if (empty(exId) && empty(exFeedbackDateS) && empty(exFeedbackDateE) && eqIgno(exFeedbackTypeList[0], "")) {
            alertErr("查詢條件不可全部空白");
            return false;
        }

        lsWhere = " where 1 = 1 ";
        if (!empty(exId)) {
            if (exId.length() != 10) {
                alertErr("身分證號長度必須為8");
                return false;
            } else {
                lsWhere += " and a.id_no = ? ";
                setString(exId);
            }
        }
        
        if (!eqIgno(exFeedbackTypeList[0], "")) {
            lsWhere += " and ( ";
            for (int i = 0; i < exFeedbackTypeList.length; i++) {
                String exFeedbackType = exFeedbackTypeList[i];
                if (empty(exFeedbackDateS)) {
                    exFeedbackDateS = "00000000";
                }
                if (empty(exFeedbackDateE)) {
                    exFeedbackDateE = "99999999";
                }
    
                if (eqIgno("0", exFeedbackType)) {
                    lsWhere += " (a.feedback_date_hilai >= ? and a.feedback_date_hilai <= ?) " +
                            " or (a.feedback_date_kana >= ? and a.feedback_date_kana <= ?) " +
                            " or (a.feedback_date_golden_diamond >= ? and a.feedback_date_golden_diamond <= ?) " +
                            " or (a.feedback_date_other >= ? and a.feedback_date_other <= ? ) ";
                    setString(exFeedbackDateS);
                    setString(exFeedbackDateE);
                    setString(exFeedbackDateS);
                    setString(exFeedbackDateE);
                    setString(exFeedbackDateS);
                    setString(exFeedbackDateE);
                    setString(exFeedbackDateS);
                    setString(exFeedbackDateE);
                    break;
                } else if (eqIgno("1", exFeedbackType)) {
                    lsWhere += " (a.feedback_date_hilai >= ? ";
                    lsWhere += " and a.feedback_date_hilai <= ? ) ";
                    setString(exFeedbackDateS);
                    setString(exFeedbackDateE);
                } else if (eqIgno("2", exFeedbackType)) {
                    lsWhere += " (a.feedback_date_kana >= ? ";
                    lsWhere += " and a.feedback_date_kana <= ? ) ";
                    setString(exFeedbackDateS);
                    setString(exFeedbackDateE);
                } else if (eqIgno("3", exFeedbackType)) {
                    lsWhere += " (a.feedback_date_golden_diamond >= ? ";
                    lsWhere += " and a.feedback_date_golden_diamond <= ? ) ";
                    setString(exFeedbackDateS);
                    setString(exFeedbackDateE);
                } else if (eqIgno("4", exFeedbackType)) {
                    lsWhere += " (a.feedback_date_other >= ? ";
                    lsWhere += " and a.feedback_date_other <= ? ) ";
                    setString(exFeedbackDateS);
                    setString(exFeedbackDateE);
                }

                if (i < exFeedbackTypeList.length - 1) {
                    lsWhere += " or ";
                }
                wp.setValue("ex_feedback_type" + "-" + exFeedbackType, "checked", 0);
            }
            lsWhere += " ) ";
        }

        wp.whereStr = lsWhere;
        return true;
    }
}
