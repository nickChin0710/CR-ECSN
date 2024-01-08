/***************************************************************************
 *                                                                          *
 *                              MODIFICATION LOG                            *
 *                                                                          *
 *    DATE    VERSION     AUTHOR               DESCRIPTION                  *
 * ---------  --------  -----------    ------------------------------------ *
 * 112/05/06  V1.00.00   Yang Bo             Program Initial                *
 * 112/05/10  V1.00.01   Yang Bo             Fix Error                      *
 * 112/05/12  V1.00.02   Ryan                新增5個檔案格式                                                                                                                             *
 * 112/08/15  V1.00.03   Grace               原wf_parm='OUTFILE_PARM'改為'INOUTFILE_PARM'          *
 *                                           isnull(), 改為NVL()                  *
 * 112/08/31  V1.00.04   Grace               queryRead() 增cal_def_date條件                                                                             *                                                                                                                                    
 ***************************************************************************************************/
package mktq01;

import java.util.Locale;

import ofcapp.BaseEdit;
import taroko.base.CommDate;
import taroko.com.TarokoCommon;
import taroko.com.TarokoFileAccess;

public class Mktq0865 extends BaseEdit {
    private final String orgTabName = "mkt_channel_list";
	private static final String MKTCHAN20001 = "TCB_LUGANGMAZU_YYYYMMDD.TXT";
	private static final String MKTCHAN20002 = "TCB_LUGANGMAZU_YYYYMMDD_MOBILE.TXT";
	private static final String MKTCHAN30003 = "R_HILAIGIFT_YYYYMM.TXT";
	private static final String MKTCHAN30004 = "R_HILAIWORLD_YYYYMM.TXT";
	private static final String MKTCHAN30001 = "R_WORLDBRD_YYYYMM.TXT";
	private final static String COL_SEPERATOR = ",";
	private final static String LINE_SEPERATOR = System.lineSeparator();
    private String controlTabName = "";
    busi.DataSet ds1 = new busi.DataSet();
    int fo = 0;
    TarokoFileAccess tf;
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
                + sqlCol(wp.itemStr("ex_active_code"), "a.active_code");

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
                + "a.active_code, "
                + "d.active_name, "
                + "NVL(decode(a.acct_type, '90', c.id_no, b.id_no), '') as id_no, "
                + "NVL(decode(a.acct_type, '90', c.chi_name, b.chi_name), '') as chi_name, "
                + "a.bonus_pnt,"
                + "a.bonus_date,"
                + "a.fund_amt,"
                + "a.fund_date,"
                + "a.gift_int,"
                + "a.lottery_int, "
                + "a.lottery_type, "
                + "a.proc_date, "
                + "a.acct_type,"
                + "a.vd_flag,"
                + "a.id_p_seqno";
        wp.daoTable = controlTabName + " a "
                + " left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
                + " left join dbc_idno c on a.id_p_seqno = c.id_p_seqno "
                + " left join mkt_channel_parm d on a.active_code = d.active_code "
        		+ "                            and  a.cal_def_date = d.cal_def_date ";

        wp.whereOrder = " "
                + " order by a.active_code,decode(a.acct_type,'90',c.id_no,b.id_no)";

        pageQuery();
        wp.setListCount(1);
        if (sqlNotFind()) {
            alertErr(appMsg.errCondNodata);
            return;
        }

        commLotteryType("comm_lottery_type");
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
    public void initPage() {
        wp.colSet("sys_date", wp.sysDate);
        wp.colSet("bb_down_file_name", wp.itemStr("bb_down_file_name"));
    }

    @Override
    public void dddwSelect() {
        try {
            if ((wp.respHtml.equals("mktq0865"))) {
                wp.initOption = "--";
                wp.optionKey = wp.colStr("ex_active_code");
                this.dddwList("dddw_active_code",
                        " select distinct trim(a.active_code) as db_code, trim(a.active_code||'-'||b.active_name) as db_desc "
                                + " from mkt_channel_list a, mkt_channel_parm b "
                                + " where a.active_code = b.active_code order by 1");

                wp.initOption = "--";
                wp.optionKey = wp.colStr("ex_doc_type");
                this.dddwList("dddw_doc_type",
                        " select distinct trim(case when wf_value != '' then wf_value"
                                + " when wf_value2 != '' then wf_value2"
                                + " when wf_value3 != '' then wf_value3"
                                + " else wf_value4 end) as db_code, trim(wf_key || '_' || wf_desc) as db_desc "
                                + " from ptr_sys_parm "
                                + " where 1=1 and wf_parm = 'INOUTFILE_PARM' and wf_key like 'MKTCHAN%' ");
            }
        } catch (Exception ex) {
        }
    }

    private int queryCheck() {
        if ((itemKk("ex_active_code").length() == 0)) {
            alertErr("活動代號不可空白");
            return (1);
        }

        if ((itemKk("ex_doc_type").length() == 0)) {
            alertErr("產檔格式不可空白");
            return (1);
        }

        return (0);
    }

    public void mediafileProcess() throws Exception {
        if (wp.colStr("org_tab_name").length() > 0) {
            controlTabName = wp.colStr("org_tab_name");
        } else {
            controlTabName = orgTabName;
        }

        wp.listCount[0] = wp.itemBuff("ser_num").length;

        if (wp.itemStr("bb_down_file_name").length() == 0) {
            alertErr("尚未產生資料, 無法產生檔案 ");
            return;
        }

        if (!checkApprove(wp.itemStr("approval_user"), wp.itemStr("approval_passwd"))) {
            return;
        }

        wp.dateTime();
        String exDocType = wp.itemStr("ex_doc_type").toUpperCase(Locale.TAIWAN); 
        String oriFileName = wp.itemStr("bb_down_file_name");
        
        if(checkFile(oriFileName)==-1) {
        	return;
        }

        setSelectLimit(99999);
        String sqlStr;
        sqlStr = "select "
                + "NVL(decode(a.acct_type, '90', c.id_no, b.id_no), '') as id_no, "
                + "NVL(decode(a.acct_type, '90', c.chi_name, b.chi_name), '') as chi_name, "
                + "d.bill_sending_zip,"
                + "(d.bill_sending_addr1||d.bill_sending_addr2||d.bill_sending_addr3||d.bill_sending_addr4||d.bill_sending_addr5) as bill_sending_addr,"
                + "NVL(decode(a.acct_type, '90', c.cellar_phone, b.cellar_phone), ''), "
                + "a.card_no,(substring(a.card_no,1,6)||'******'||substring(a.card_no,13)) as hi_card_no, "
                + "b.birthday,b.chi_name,d.bill_sending_zip,b.cellar_phone, "
                + "(select sum(fund_amt) from mkt_channel_list where card_no = a.card_no) as sum_fund_amt, "
                +"(d.bill_sending_addr1 || d.bill_sending_addr2 || d.bill_sending_addr3 || d.bill_sending_addr4 || d.bill_sending_addr5) as bill_sending_addr, "
                + "right(e.group_code,3) as group_code_type,a.gift_int,a.gift_amt "
                + "from " + controlTabName + " a "
                + "left join crd_idno b on a.id_p_seqno = b.id_p_seqno "
                + "left join dbc_idno c on a.id_p_seqno = c.id_p_seqno "
                + "left join act_acno d on a.p_seqno = d.p_seqno "
                + "left join crd_card e on a.card_no = e.card_no "
                + "WHERE 1=1 "
                + "  and a.active_code = ? "
                + "  order by a.active_code,decode(a.acct_type, '90', c.id_no, b.id_no) ";
        busi.FuncBase fB = new busi.FuncBase();
        fB.setConn(wp);
        fB.setSelectLimit(0);
        ds1.colList = fB.sqlQuery(sqlStr, new Object[]{wp.itemStr("ex_active_code")});
        sqlParm.clear();
        
        textFileWrite(exDocType);

        tf.closeOutputText(fo);
        alertMsg("檔案 [" + oriFileName + "] 已經產生,累計下載 " + ds1.listRows() + " 筆!");

        wp.colSet("zz_full_media_file", " download="
                + oriFileName
                + " href=./WebData/work/"
                + oriFileName
                + "?response-content-type=application/octet-stream");
        wp.colSet("img_display", " src=images/downLoad.gif height=\"30\" ");
    }

    public String checkColumn(String s1) {
        return ds1.colStr(s1) + ",";
    }

    // ************************************************************************
    public void commLotteryType(String s1) {
        String[] cde = {"1", "2", "3"};
        String[] txt = {"抽獎名單", "豐富點數", "一般名單"};
        for (int ii = 0; ii < wp.selectCnt; ii++) {
            for (int inti = 0; inti < cde.length; inti++) {
                String s2 = s1.substring(5);
                if (wp.colStr(ii, s2).equals(cde[inti])) {
                    wp.colSet(ii, s1, txt[inti]);
                    break;
                }
            }
        }
    }
    
    private int checkFile(String oriFileName){
        tf = new TarokoFileAccess(wp);
        fo = tf.openOutputText(oriFileName, "MS950");
        wp.colSet("zz_media_file", oriFileName);
        return fo;
    }
    
    private void textFileWrite(String exDocType) throws Exception {
        switch(exDocType) {
        case MKTCHAN20001:
        	textlayoutMktchan20001();
        	break;
        case MKTCHAN20002:
        	textlayoutMktchan20002();
        	break;
        case MKTCHAN30003:
        case MKTCHAN30004:
        	textlayoutMktchan30003();
        	break;
        case MKTCHAN30001:
        	textlayoutMktchan30001();
        	break;
        default:
        	textLayoutOther();
        }
    }
    
    private void textLayoutOther() throws Exception {
    	StringBuffer bf = new StringBuffer();
    	bf.append("持卡人姓名")
    	.append(COL_SEPERATOR)
    	.append("持卡人ID")
    	.append(COL_SEPERATOR)
    	.append("郵遞區號")
    	.append(COL_SEPERATOR)
    	.append("通訊地址")
    	.append(COL_SEPERATOR)
    	.append("手機號碼")
    	.append(LINE_SEPERATOR);
    	tf.writeTextFile(fo, bf.toString());
    	
    	 while (ds1.listNext()) {
    		bf = new StringBuffer();
    		bf.append(ds1.colStr("chi_name"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("id_no"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_zip"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_addr"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("cellar_phone"))
    		.append(LINE_SEPERATOR);
    		tf.writeTextFile(fo, bf.toString());
        }
    }
    
    private void textlayoutMktchan20001() throws Exception {
    	StringBuffer bf = new StringBuffer();
    	bf.append("信用卡卡號")
    	.append(COL_SEPERATOR)
    	.append("民國出生年月日")
    	.append(COL_SEPERATOR)
    	.append("持卡人姓名")
    	.append(COL_SEPERATOR)
    	.append("帳單郵遞區號")
    	.append(COL_SEPERATOR)
    	.append("信用卡帳單地址")
    	.append(LINE_SEPERATOR);
    	tf.writeTextFile(fo, bf.toString());
    	
    	 while (ds1.listNext()) {
    		bf = new StringBuffer();
    		bf.append(ds1.colStr("hi_card_no"))
    		.append(COL_SEPERATOR)
    		.append(toTwDate(ds1.colStr("birthday")))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("chi_name"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_zip"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_addr"))
    		.append(LINE_SEPERATOR);
    		tf.writeTextFile(fo, bf.toString());
        }
    }
    
    private void textlayoutMktchan20002() throws Exception {
    	StringBuffer bf = new StringBuffer();
    	bf.append("信用卡卡號")
    	.append(COL_SEPERATOR)
    	.append("民國出生年月日")
    	.append(COL_SEPERATOR)
    	.append("持卡人姓名")
    	.append(COL_SEPERATOR)
    	.append("帳單郵遞區號")
    	.append(COL_SEPERATOR)
    	.append("信用卡帳單地址")
    	.append(COL_SEPERATOR)
    	.append("手機號碼")
    	.append(COL_SEPERATOR)
    	.append("年度累積金額")
    	.append(LINE_SEPERATOR);
    	tf.writeTextFile(fo, bf.toString());
    	
    	 while (ds1.listNext()) {
    		bf = new StringBuffer();
    		bf.append(ds1.colStr("card_no"))
    		.append(COL_SEPERATOR)
    		.append(toTwDate(ds1.colStr("birthday")))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("chi_name"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_zip"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_addr"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("cellar_phone"))
    		.append(COL_SEPERATOR)
    		.append(String.format("%013.0f", ds1.colNum("sum_fund_amt")*100))
    		.append(LINE_SEPERATOR);
    		tf.writeTextFile(fo, bf.toString());
        }
    }
    
    private void textlayoutMktchan30003() throws Exception {
    	StringBuffer bf = new StringBuffer();
    	bf.append("持卡人ID")
    	.append(COL_SEPERATOR)
    	.append("Type")
    	.append(COL_SEPERATOR)
    	.append("信用卡卡號")
    	.append(COL_SEPERATOR)
    	.append("漢來美食累積次數")
    	.append(COL_SEPERATOR)
    	.append("漢來美食累積金額")
    	.append(COL_SEPERATOR)
    	.append("手機號碼 ")
    	.append(COL_SEPERATOR)
    	.append("電子郵件")
    	.append(COL_SEPERATOR)
    	.append("郵遞區號")
    	.append(COL_SEPERATOR)
    	.append("持卡人姓名")
    	.append(COL_SEPERATOR)
    	.append("帳單地址")
    	.append(LINE_SEPERATOR);
    	tf.writeTextFile(fo, bf.toString());
    	
    	 while (ds1.listNext()) {
    		bf = new StringBuffer();
    		bf.append(ds1.colStr("id_no"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("group_code_type"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("card_no"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colInt("gift_int"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colNum("gift_amt"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("cellar_phone"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("e_mail_addr"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_zip"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("chi_name"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_addr"))
    		.append(LINE_SEPERATOR);
    		tf.writeTextFile(fo, bf.toString());
        }
    }
    
    private void textlayoutMktchan30001() throws Exception {
    	StringBuffer bf = new StringBuffer();
    	bf.append("持卡人ID")
    	.append(COL_SEPERATOR)
    	.append("Type")
    	.append(COL_SEPERATOR)
    	.append("信用卡卡號")
    	.append(COL_SEPERATOR)
    	.append("手機號碼 ")
    	.append(COL_SEPERATOR)
    	.append("郵遞區號")
    	.append(COL_SEPERATOR)
    	.append("持卡人姓名")
    	.append(COL_SEPERATOR)
    	.append("帳單地址")
    	.append(COL_SEPERATOR)
    	.append("出生日期")
    	.append(LINE_SEPERATOR);
    	tf.writeTextFile(fo, bf.toString());
    	
    	 while (ds1.listNext()) {
    		bf = new StringBuffer();
    		bf.append(ds1.colStr("id_no"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("group_code_type"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("card_no"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("cellar_phone"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_zip"))
    		.append(COL_SEPERATOR)
       		.append(ds1.colStr("chi_name"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("bill_sending_addr"))
    		.append(COL_SEPERATOR)
    		.append(ds1.colStr("birthday"))
    		.append(LINE_SEPERATOR);
    		tf.writeTextFile(fo, bf.toString());
        }
    }
    
    private String toTwDate(String strName) {
        String lsDate = strName.trim();
        if (lsDate.length() != 8)
          return lsDate;

        return String.format("%03d%s", (Integer.parseInt(lsDate.substring(0, 4)) - 1911),lsDate.substring(4, 8));
      }
}
