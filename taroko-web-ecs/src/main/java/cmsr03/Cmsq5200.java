/******************************************************************************
 *                                                                             *
 *                              MODIFICATION LOG                               *
 *                                                                             *
 *     DATE     Version    AUTHOR                  DESCRIPTION                 *
 *  ---------  --------- ----------- ----------------------------------------  *
 *  112/10/11  V1.00.00  Zuwei Su    Initial                                   *
 ******************************************************************************/
package cmsr03;

import java.util.LinkedHashMap;
import java.util.Map;
import ofcapp.BaseAction;

public class Cmsq5200 extends BaseAction {
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

        wp.sqlCmd = " SELECT id_no,chi_name,card_no,current_code,acct_month,tot_amt,use_month,free_tot_cnt,send_date"
                + " FROM mkt_thsr_pickup_list ";
        wp.sqlCmd += lsWhere;
        wp.sqlCmd += " ORDER BY use_month,id_no ";

        pageQuery();

        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr("查無資料");
            return;
        }
        int totalFreeTotCnt = 0;
        Map<String, String> currentCodeMape = new LinkedHashMap() {
            {
                put("0", "0:正常");
                put("1", "1:一般停用");
                put("2", "2:掛失");
                put("3", "3:強停");
                put("4", "4:其他");
                put("5", "5:偽卡");
            }
        };
        for (int i = 0; i < sqlRowNum; i++) {
            int freeTotCnt = wp.colInt(i, "free_tot_cnt");
            totalFreeTotCnt += freeTotCnt;
            // 控管碼:依代碼顯示代碼加中文. 0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡
            String currentCode = wp.colStr("current_code");
            String currentCodeDesc = currentCodeMape.get(currentCode);
            String sendDate = wp.colStr("send_date");
            if (currentCodeDesc == null) {
                currentCodeDesc = "";
            }
            wp.colSet("current_code_desc", currentCodeDesc);
            wp.colSet("send_date_desc", "Y");
            if (sendDate == null || sendDate.trim().length() == 0) {
                wp.colSet("send_date_desc", "N");
            }
        }
        wp.colSet("total_free_tot_cnt", String.valueOf(totalFreeTotCnt));
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
            lsWhere += " and use_month = ? ";
            setString(exUseMonth);
        } else {
            alertErr2("查詢年月不可為空");
            return false;
        }
        if (!empty(exId)) {
            if (exId.length() != 10) {
                alertErr("身分證號長度必須為10位");
                return false;
            } else {
                lsWhere += " and id_no = ? ";
                setString(exId);
            }
        }
        if (!empty(exCardNo)) {
            lsWhere += " and card_no = ? ";
            setString(exCardNo);
        }

        wp.whereStr = lsWhere;
        return true;
    }
}
