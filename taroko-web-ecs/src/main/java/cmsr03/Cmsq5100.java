/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                  DESCRIPTION                 *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/10/08  V1.00.00  Zuwei Su    Initial                                   *
 ******************************************************************************/
package cmsr03;

import java.util.LinkedHashMap;
import java.util.Map;
import ofcapp.BaseAction;

public class Cmsq5100 extends BaseAction {
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
    public void dddwSelect() {}

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

        wp.sqlCmd = " SELECT a.chi_name,a.major_id,a.card_no,a.old_card_no,"
                + " a.bin_type,a.preferential,a.mod_type,a.purchase_date,a.free_tot_cnt,a.TOT_AMT,c.id_no "
                + " FROM cms_airport_list a "
                + " LEFT JOIN crd_idno c ON a.id_p_seqno=c.id_p_seqno             ";
        wp.sqlCmd += lsWhere;
        wp.sqlCmd += " ORDER BY use_month,id_no ";

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr("查無資料");
            return;
        }
        int totalFreeTotCnt = 0;
        Map<String, Integer> preferentialMap = new LinkedHashMap<>();
        for (int i = 0; i < sqlRowNum; i++) {
            String preferential = wp.colStr(i, "preferential").trim();
            int freeTotCnt = wp.colInt(i, "free_tot_cnt");
            totalFreeTotCnt += freeTotCnt;
            if (preferentialMap.containsKey(preferential)) {
                preferentialMap.put(preferential, preferentialMap.get(preferential) + freeTotCnt);
            } else {
                preferentialMap.put(preferential, freeTotCnt);
            }
        }
        wp.colSet("total_free_tot_cnt", String.valueOf(totalFreeTotCnt));
        String preferentialInfo = "";
        for (Map.Entry<String, Integer> entry : preferentialMap.entrySet()) {
            preferentialInfo += "　　優惠別" + entry.getKey() + "：　　" + String.format("%,d", entry.getValue()) + " 次";
        }
        wp.colSet("preferential_info", preferentialInfo);
    }

    @Override
    public void querySelect() throws Exception {}

    @Override
    public void dataRead() throws Exception {}

    @Override
    public void saveFunc() throws Exception {}

    @Override
    public void procFunc() throws Exception {}

    @Override
    public void initButton() {}

    @Override
    public void initPage() {}

    boolean getWhereStr() {
        String exUseMonth = wp.itemStr("ex_use_month");
        String exCardNo = wp.itemStr("ex_card_no");
        String exId = wp.itemStr("ex_id_no");
        String exChiName = wp.itemStr("ex_chi_name");

        lsWhere = " where 1 = 1 ";
        if (!empty(exUseMonth)) {
            lsWhere += " and a.use_month = ? ";
            setString(exUseMonth);
        } else {
            alertErr2("查詢年月不可為空");
            return false;
        }
        if (!empty(exChiName)) {
            lsWhere += " and a.chi_name like '%?%' ";
            setString(exChiName);
        }
        if (!empty(exId)) {
            if (exId.length() != 10) {
                alertErr("身分證號長度必須為10位");
                return false;
            } else {
                lsWhere += " and a.id_no = ? ";
                setString(exId);
            }
        }
        if (!empty(exCardNo)) {
            lsWhere += " and a.card_no = ? ";
            setString(exCardNo);
        }

        wp.whereStr = lsWhere;
        return true;
    }
}
