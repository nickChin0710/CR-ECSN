package cmsm03;
/**
 * 新貴通卡請款匯入作業 2019-0614: JH p_xxx >>acno_pxxx V.2018-0911.alex
 * 新貴通卡請款匯入作業 2023-1119: Zuwei Su 新模板格式
 */

import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import taroko.base.CommDate;
import taroko.base.CommString;
import taroko.com.TarokoExcel;
import taroko.com.TarokoParm;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

public class Cmsm5030 extends BaseAction implements InfaceExcel {
    CommDate commDate = new CommDate();
    CommString commStr = new CommString();
    XSSFWorkbook wb = null;
    XSSFSheet sheet = null;
    XSSFRow row = null;
    XSSFRow dummyRow = null;
    XSSFCell cell = null;
    SimpleDateFormat sdf = null;

    // InputStream inExcelFile = null;

    int ilTotCnt = 0;
    int rr = -1;

    @Override
    public void userAction() throws Exception {
        strAction = wp.buttonCode;
        switch (wp.buttonCode) {
            case "C": // 新貴匯入--
                doImport();
                break;
            // case "C2": // --
            // doImportC2();
            // break;
            default:
                defaultAction();
        }
    }

    @Override
    public void dddwSelect() {
        // TODO Auto-generated method stub

    }

    void doImport() throws Exception {
        int liSheetNo = 0;
        String exBinType = wp.itemStr("ex_bin_type");
        String exFilePcode = wp.itemStr("ex_file_pcode");
        String oFilename = wp.itemStr("zz_file_name");
        String zzFileName = oFilename;
        if (zzFileName.length() == 0) {
            alertErr("匯入檔名 不可空白");
            return;
        }
        if (!zzFileName.toLowerCase().endsWith(".xlsx")) {
            alertErr("excle檔(xlsx),才可以匯入");
            return;
        }
        zzFileName = zzFileName.substring(0, zzFileName.length() - "_20231119.xlsx".length()) + ".xlsx";
        String lfilename = zzFileName.toLowerCase();
        String ym = null;
        switch (exBinType) {
            case "J":
                // jcb_yyyyMM.xlsx
                if (lfilename.startsWith("jcb_")) {
                    ym = lfilename.substring(4, lfilename.length() - 5);
                }
                break;
            case "V":
                // visa_yyyyMM.xlsx
                if (lfilename.startsWith("visa_")) {
                    ym = lfilename.substring(5, lfilename.length() - 5);
                }
                break;
            case "M":
                // master__yyyyMM.xlsx
                if (lfilename.startsWith("master_")) {
                    ym = lfilename.substring(7, lfilename.length() - 5);
                }
                break;
            default:
                break;
        }
        if (ym == null || ym.length() != 6 || !commDate.isDate(ym + "01")) {
            alertErr("卡別與檔名不符");
            return;
        }

        if (exFilePcode.length() == 0) {
            alertErr("檔案密碼必輸入");
            return;
        }

        String checkSql = "select imp_file_name,vip_kind "
                + "from cms_ppcard_visit "
                + "where vip_kind='1' and lower(imp_file_name)=lower(?)";
        sqlSelect(checkSql, new Object[] {zzFileName});
        if (sqlRowNum > 0) {
            alertErr("檔名重複,無法匯入");
            return;
        }

        try (InputStream inExcelFile = new FileInputStream(
                TarokoParm.getInstance().getDataRoot() + "/upload/" + oFilename)) {
            // --從 0 開始
            // li_sheet_no = (int) (wp.item_num("ex_sheet") -1);
            liSheetNo = 0;

            wb = (XSSFWorkbook)WorkbookFactory.create(inExcelFile, exFilePcode);
            sheet = wb.getSheetAt(liSheetNo);
            String firstValue = readCellValueString(sheet.getRow(0).getCell(0));
            if (!firstValue.equalsIgnoreCase("item")) {
                alertErr("資料格式錯誤,無法匯入失敗");
                return;
            }

            Iterator rowIterator = sheet.rowIterator();
            int liExcelRows = 0, liCnt = -1, liSerNum = 0;
            String liSeqno = "";
            String lsPpCardNo = "", lsVisitDate = "";
            while (rowIterator.hasNext()) {
                row = (XSSFRow) rowIterator.next();
                liExcelRows++;
                if (liExcelRows == 1) {
                    continue;
                }
                cell = row.getCell(0);
                String item = readCellValueString(cell);
                if (cell == null || !isNumber(item)) {
                    break;
                }
                liCnt++;
                liSerNum++;
                
                wp.colSet(liCnt, "crt_date", getSysDate());
                wp.colSet(liCnt, "bin_type", exBinType);
                wp.colSet(liCnt, "data_seqno", item);
                wp.colSet(liCnt, "vip_kind", "1");
                wp.colSet(liCnt, "from_type", "2");
                wp.colSet(liCnt, "imp_file_name", zzFileName);
                wp.colSet(liCnt, "crt_user", wp.loginUser);

                switch (exBinType) {
                    case "J":
                        wp.colSet(liCnt, "err_code", "00");
                        wp.colSet(liCnt, "bank_name", readCellValueString(row.getCell(1)));
                        wp.colSet(liCnt, "pp_card_no", readCellValueString(row.getCell(2)));
                        wp.colSet(liCnt, "lounge_name", readCellValueString(row.getCell(3)));
                        wp.colSet(liCnt, "lounge_code", readCellValueString(row.getCell(4)));
                        wp.colSet(liCnt, "iso_conty", readCellValueString(row.getCell(5)));
                        wp.colSet(liCnt, "ch_visits", readCellValueString(row.getCell(7)));
                        wp.colSet(liCnt, "guests_count", readCellValueString(row.getCell(8)));
                        wp.colSet(liCnt, "total_visits", readCellValueString(row.getCell(9)));
                        wp.colSet(liCnt, "voucher_no", readCellValueString(row.getCell(11)));
                        break;
                    case "M":
                        wp.colSet(liCnt, "err_code", "00");
                        wp.colSet(liCnt, "bank_name", readCellValueString(row.getCell(1)));
                        wp.colSet(liCnt, "pp_card_no", readCellValueString(row.getCell(5)));
                        wp.colSet(liCnt, "ch_ename", readCellValueString(row.getCell(6)));
                        wp.colSet(liCnt, "visit_date", readCellValueString(row.getCell(7)));
                        wp.colSet(liCnt, "lounge_name", readCellValueString(row.getCell(8)));
                        wp.colSet(liCnt, "lounge_code", readCellValueString(row.getCell(9)));
                        wp.colSet(liCnt, "domestic_int", readCellValueString(row.getCell(10)));
                        wp.colSet(liCnt, "iso_conty", readCellValueString(row.getCell(11)));
                        wp.colSet(liCnt, "iso_conty_code", readCellValueString(row.getCell(12)));
                        wp.colSet(liCnt, "ch_visits", readCellValueString(row.getCell(13)));
                        wp.colSet(liCnt, "guests_count", readCellValueString(row.getCell(14)));
                        wp.colSet(liCnt, "total_visits", readCellValueString(row.getCell(15)));
                        wp.colSet(liCnt, "batch_no", readCellValueString(row.getCell(16)));
                        wp.colSet(liCnt, "voucher_no", readCellValueString(row.getCell(17)));
                        wp.colSet(liCnt, "mc_billing_region", readCellValueString(row.getCell(18)));
                        wp.colSet(liCnt, "curr_code", readCellValueString(row.getCell(19)));
                        wp.colSet(liCnt, "fee_per_holder", readCellValueString(row.getCell(20)));
                        wp.colSet(liCnt, "fee_per_guest", readCellValueString(row.getCell(21)));
                        wp.colSet(liCnt, "total_fee", readCellValueString(row.getCell(22)));
                        wp.colSet(liCnt, "total_free_guests", readCellValueString(row.getCell(23)));
                        wp.colSet(liCnt, "free_guests_value", readCellValueString(row.getCell(24)));
                        wp.colSet(liCnt, "tot_charg_guest", readCellValueString(row.getCell(25)));
                        wp.colSet(liCnt, "charg_guest_value", readCellValueString(row.getCell(26)));
                        wp.colSet(liCnt, "billing_region", readCellValueString(row.getCell(27)));
                        break;
                    case "V":
                        wp.colSet(liCnt, "err_code", "00");
                        wp.colSet(liCnt, "bank_name", "Taiwan Cooperative Bank 425870");
                        wp.colSet(liCnt, "pp_card_no", readCellValueString(row.getCell(1)));
                        wp.colSet(liCnt, "ch_ename", readCellValueString(row.getCell(2)));
                        wp.colSet(liCnt, "visit_date", readCellValueString(row.getCell(10)));
                        wp.colSet(liCnt, "lounge_name", readCellValueString(row.getCell(3)));
                        wp.colSet(liCnt, "lounge_code", readCellValueString(row.getCell(4)));
                        wp.colSet(liCnt, "iso_conty", readCellValueString(row.getCell(8)));
                        wp.colSet(liCnt, "ch_visits", readCellValueString(row.getCell(11)));
                        wp.colSet(liCnt, "guests_count", readCellValueString(row.getCell(12)));
                        wp.colSet(liCnt, "total_visits", readCellValueString(row.getCell(13)));
                        wp.colSet(liCnt, "voucher_no", readCellValueString(row.getCell(17)));
                        wp.colSet(liCnt, "fee_per_holder", readCellValueString(row.getCell(14)));
                        wp.colSet(liCnt, "fee_per_guest", readCellValueString(row.getCell(15)));
                        wp.colSet(liCnt, "total_fee", readCellValueString(row.getCell(16)));
                        
                        wp.colSet(liCnt, "billing_region", readCellValueString(row.getCell(9))); 
                        wp.colSet(liCnt, "terminal_no", readCellValueString(row.getCell(5)));       
                        wp.colSet(liCnt, "use_city", readCellValueString(row.getCell(7)));   
                        break;

                    default:
                        break;
                }
                String visitDate = wp.colStr(liCnt, "visit_date");
                if (visitDate.length() > 8) {
                    visitDate = new SimpleDateFormat("yyyyMMdd").format(new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH).parse(visitDate));
                    wp.colSet(liCnt, "visit_date", visitDate);
                }
                if (wp.colEmpty(liCnt, "pp_card_no")) {
                    wp.colSet(liCnt, "err_code", "01");
                    wp.colSet(liCnt, "free_proc_result", "PP卡號空白");
                } else {
                    // Priority Pass ID是否存在【crd_card_pp】
                    selectPpCardInfo(wp.colStr(liCnt, "pp_card_no"));
                    if (sqlRowNum == 0) {
                        wp.colSet(liCnt, "err_code", "02");
                        wp.colSet(liCnt, "free_proc_result", "卡號:" + wp.colStr(liCnt, "pp_card_no") + "不存在");
                    } else {
                        wp.colSet(liCnt, "id_p_seqno", wp.colStr("pp_id_p_seqno"));
                        wp.colSet(liCnt, "id_no", wp.colStr("pp_id_no"));
                        wp.colSet(liCnt, "id_no_code", "pp_id_no_code");
                    }
                }
                // Total Visits (JCB)
                // Total Visit Count<=0 (mster)
                // No of Total Visits<=0 (visa)
                if (wp.colEmpty(liCnt, "total_visits") || wp.colEq(liCnt, "total_visits", "0")) {
                    wp.colSet(liCnt, "err_code", "07");
                    wp.colSet(liCnt, "free_proc_result", "使用次數 & 攜帶人數同時＜=0(or空白)");
                }
                ilTotCnt = liCnt+1;
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }

        procFunc();
    }
    
    private void selectPpCardInfo(String ppCardNo) {
        String sql = "select distinct crd_card_pp.id_p_seqno as pp_id_p_seqno"
                + ",crd_idno.ID_NO as pp_id_no"
                + ",crd_idno.ID_NO_CODE as pp_id_no_code "
                + " from crd_card_pp "
                + " left join crd_idno on crd_idno.id_p_seqno = crd_card_pp.id_p_seqno "
                + " where crd_card_pp.VIP_KIND = '1' "
                + " and crd_card_pp.card_no = ?";
        sqlSelect(sql, new Object[] {ppCardNo});
    }

    @Override
    public void queryFunc() throws Exception {

    }

    @Override
    public void queryRead() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void querySelect() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void dataRead() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveFunc() throws Exception {
        if (wp.iempty("ex_file_type") || wp.iempty("ex_batch_no")) {
            alertErr("[檔案類別, 匯入批號] 不可空白");
            return;
        }

        Cmsm5030Func func = new Cmsm5030Func();
        func.setConn(wp);
        // func.actionInit(strAction);

        switch (strAction) {
            case "A":
                rc = func.dbInsert();
                break;
            case "U":
                rc = func.dbUpdate();
                break;
            case "D":
                rc = func.dbDelete();
                break;
        }
        this.sqlCommit(rc);

        if (rc != 1) {
            alertErr(func.getMsg());
            return;
        }
        alertMsg(func.getMsg());
    }

    @Override
    public void procFunc() throws Exception {
        int llOk = 0, llErr = 0;

        Cmsm5030Func func = new Cmsm5030Func();
        func.setConn(wp);

        for (int ii = 0; ii < ilTotCnt; ii++) {
            int liRc = func.insertVist(ii);
            if (liRc == 1) {
                llOk++;
            }
            if (wp.colEq(ii, "error_code", "00") == false) {
                wp.colSet(ii, "db_errmsg", func.getMsg());
                // -error-
                llErr++;
                listErrData(ii);
            }
        }
        if (llOk > 0) {
            sqlCommit(1); // one-time-commit
        }

        // wp.listCount[0] = ll_err;
        wp.setListSernum(0, "", llErr);
        alertMsg("資料匯入完成 , 成功:" + llOk + " 失敗:" + llErr);

        if (llErr > 0) {
            try {
                log("xlsFunction: started--------");
                wp.reportId = "cmsm5030";
                wp.colSet("user_id", wp.loginUser);
                TarokoExcel xlsx = new TarokoExcel();
                wp.fileMode = "Y";
                xlsx.excelTemplate = "cmsm5030.xlsx";
                wp.pageRows = 9999;
                xlsx.processExcelSheet(wp);
                xlsx.outputExcelUrl();
                String lsUrl = wp.linkURL;
                xlsx = null;
                wp.colSet("url_err_file", lsUrl);
                wp.colSet("err_file", wp.exportFile);
                wp.linkURL = "";
                wp.linkMode = "";
                log("xlsFunction: ended-------------");
            } catch (Exception ex) {
                wp.expMethod = "xlsPrint";
                wp.expHandle(ex);
            }
        }
    }

    void listErrData(int ll) {
        rr++;

        wp.colSet(rr, "er_crt_date", wp.colStr(ll, "crt_date"));
        wp.colSet(rr, "er_bin_type", wp.colStr(ll, "bin_type"));
        wp.colSet(rr, "er_data_seqno", wp.colStr(ll, "data_seqno"));
        wp.colSet(rr, "er_pp_card_no", wp.colStr(ll, "pp_card_no"));
        wp.colSet(rr, "er_from_type", wp.colStr(ll, "from_type"));
        wp.colSet(rr, "er_terminal_no", wp.colStr(ll, "terminal_no"));
        wp.colSet(rr, "er_city", wp.colStr(ll, "use_city"));
        wp.colSet(rr, "er_imp_file_name", wp.colStr(ll, "imp_file_name"));
        wp.colSet(rr, "er_free_use_cnt", wp.colStr(ll, "free_use_cnt"));
        wp.colSet(rr, "er_ch_cost_amt", wp.colStr(ll, "ch_cost_amt"));
        wp.colSet(rr, "er_guest_cost_amt", wp.colStr(ll, "guest_cost_amt"));
        wp.colSet(rr, "er_crt_user", wp.colStr(ll, "crt_user"));
        wp.colSet(rr, "er_id_no", wp.colStr(ll, "id_no"));
        wp.colSet(rr, "er_id_no_code", wp.colStr(ll, "id_no_code"));
        wp.colSet(rr, "er_id_p_seqno", wp.colStr(ll, "id_p_seqno"));
        wp.colSet(rr, "er_mcht_no", wp.colStr(ll, "mcht_no"));
        wp.colSet(rr, "er_card_no", wp.colStr(ll, "card_no"));
        wp.colSet(rr, "er_bank_name", wp.colStr(ll, "bank_name"));
        wp.colSet(rr, "er_deal_type", wp.colStr(ll, "deal_type"));
        wp.colSet(rr, "er_associate_code", wp.colStr(ll, "associate_code"));
        wp.colSet(rr, "er_ica_no", wp.colStr(ll, "ica_no"));
        wp.colSet(rr, "er_cardholder_ename", wp.colStr(ll, "cardholder_ename"));
        wp.colSet(rr, "er_visit_date", wp.colStr(ll, "visit_date"));
        wp.colSet(rr, "er_lounge_name", wp.colStr(ll, "lounge_name"));
        wp.colSet(rr, "er_lounge_code", wp.colStr(ll, "lounge_code"));
        wp.colSet(rr, "er_domestic_int", wp.colStr(ll, "domestic_int"));
        wp.colSet(rr, "er_iso_conty", wp.colStr(ll, "iso_conty"));
        wp.colSet(rr, "er_iso_conty_code", wp.colStr(ll, "iso_conty_code"));
        wp.colSet(rr, "er_cardholder_visits", wp.colStr(ll, "cardholder_visits"));
        wp.colSet(rr, "er_guests_count", wp.colStr(ll, "guests_count"));
        wp.colSet(rr, "er_total_visits", wp.colStr(ll, "total_visits"));
        wp.colSet(rr, "er_batch_no", wp.colStr(ll, "batch_no"));
        wp.colSet(rr, "er_voucher_no", wp.colStr(ll, "voucher_no"));
        wp.colSet(rr, "er_mc_billing_region", wp.colStr(ll, "mc_billing_region"));
        wp.colSet(rr, "er_curr_code", wp.colStr(ll, "curr_code"));
        wp.colSet(rr, "er_fee_per_holder", wp.colStr(ll, "fee_per_holder"));
        wp.colSet(rr, "er_fee_per_guest", wp.colStr(ll, "fee_per_guest"));
        wp.colSet(rr, "er_total_fee", wp.colStr(ll, "total_fee"));
        wp.colSet(rr, "er_total_free_guests", wp.colStr(ll, "total_free_guests"));
        wp.colSet(rr, "er_free_guests_value", wp.colStr(ll, "free_guests_value"));
        wp.colSet(rr, "er_tot_charg_guest", wp.colStr(ll, "tot_charg_guest"));
        wp.colSet(rr, "er_charg_guest_value", wp.colStr(ll, "charg_guest_value"));
        wp.colSet(rr, "er_billing_region", wp.colStr(ll, "billing_region"));
        wp.colSet(rr, "er_errmsg", wp.colStr(ll, "free_proc_result"));
    }

    @Override
    public void initButton() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initPage() {
        // TODO Auto-generated method stub

    }

    @Override
    public void xlsPrint() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void logOnlineApprove() throws Exception {
        // TODO Auto-generated method stub

    }

    public String readCellValueString(Cell cell) {
        String value = "";
        if (null == cell) {
            value = "";
        } else {
            switch (cell.getCellTypeEnum()) {
                case NUMERIC: // 数字
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        java.util.Date date = org.apache.poi.ss.usermodel.DateUtil
                                .getJavaDate(cell.getNumericCellValue());
                        value = new SimpleDateFormat("yyyyMMdd").format(date);
                    } else {
                        value = NumberToTextConverter.toText(cell.getNumericCellValue());
                    }
                    break;
                case STRING: // 字符串
                    value = cell.getStringCellValue().trim();
                    break;
                case BOOLEAN: // Boolean
                    value = String.valueOf(cell.getBooleanCellValue());
                    break;
                case FORMULA: // 公式
                    value = cell.getCellFormula();
                    break;
                case BLANK: // 空值
                    value = "";
                    break;
                case ERROR: // 故障
                    value = "";
                    break;
                default:
                    value = "";
                    break;
            }
        }

        return value;
    }
}
