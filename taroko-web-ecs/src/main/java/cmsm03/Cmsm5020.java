package cmsm03;
/** 新貴通卡請款匯入作業
 * 2023-0511:  Zuwei Su    copy from mega
 * 2019-0614:  JH    p_xxx >>acno_pxxx
 * V.2018-0911.alex
 */

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoParm;

public class Cmsm5020 extends BaseAction implements InfaceExcel {
    taroko.base.CommDate commDate = new taroko.base.CommDate();
//    XSSFWorkbook wb = null;
//    XSSFSheet sheet = null;
//    XSSFRow row = null;
//    XSSFRow dummyRow = null;
//    XSSFCell cell = null;

    HSSFWorkbook hwb = null;
    HSSFSheet hsheet = null;
    HSSFRow hrow = null;
    HSSFRow hdummyRow = null;
    HSSFCell hcell = null;
    SimpleDateFormat sdf = null;

    InputStream inExcelFile = null;

    int ilTotCnt = 0;
    int rr = -1;

    @Override
    public void userAction() throws Exception {
        strAction = wp.buttonCode;
        switch (wp.buttonCode) {
//            case "C": // 新貴匯入--
//                doImport();
//                break;
            case "C2": // --
                doImportC2();
                break;
            default:
                defaultAction();
        }
    }

    @Override
    public void dddwSelect() {
        try {
//            wp.initOption = "--";
            wp.optionKey = "28940383";
            if (wp.colStr("ex_corp_no").length() > 0) {
                wp.optionKey = wp.colStr("ex_corp_no");
            }
            dddwList("dddw_corp_no", "ptr_sys_idtab", "wf_id", "wf_desc",
                    "where wf_type ='SUPPLIER' order by wf_id,wf_desc");
        } catch (Exception ex) {
        }
    }

//    void doImport() throws Exception {
//        int liSheetNo = 0;
//        if (wp.itemEmpty("zz_file_name")) {
//            alertErr("匯入檔名 不可空白");
//            return;
//        }
//
//        if (inExcelFile == null) {
//            inExcelFile = new FileInputStream(TarokoParm.getInstance().getDataRoot()
//                    + "/upload/"
//                    + wp.itemStr("zz_file_name"));
//            wb = new XSSFWorkbook(inExcelFile);
//        }
//
//        // --從 0 開始
//        // li_sheet_no = (int) (wp.item_num("ex_sheet") -1);
//        liSheetNo = 0;
//
//        sheet = wb.getSheetAt(liSheetNo);
//        Iterator rowIterator = sheet.rowIterator();
//
//        int liExcelRows = 0, liCnt = -1, liSerNum = 0;
//        String liSeqno = "";
//        String lsPpCardNo = "", lsVisitDate = "";
//        while (rowIterator.hasNext()) {
//            row = (XSSFRow) rowIterator.next();
//            liExcelRows++;
//
//            // if(li_excel_rows==1 || li_excel_rows < wp.item_num("ex_row1")) continue;
//            //
//            // if(!wp.item_empty("ex_row2") && wp.item_num("ex_row2")!=0){
//            // if(li_excel_rows>wp.item_num("ex_row2")) break;
//            // }
//
//            cell = row.getCell(0);
//            if (cell == null)
//                continue;
//            if (cell.getCellTypeEnum() != CellType.NUMERIC)
//                continue;
//            liSeqno = "" + cell.getNumericCellValue();
//            if (wp.itemEq("ex_bin_type", "M")) {
//                cell = row.getCell(5);
//            } else if (wp.itemEq("ex_bin_type", "V")) {
//                cell = row.getCell(1);
//            }
//            lsPpCardNo = cell.getStringCellValue();
//
//            if (empty(liSeqno) && empty(lsPpCardNo))
//                continue;
//
//            if (!isNumber(liSeqno) || !isNumber(lsPpCardNo))
//                continue;
//
//            liCnt++;
//            liSerNum++;
//
//            if (liSerNum < 10) {
//                wp.colSet(liCnt, "ser_num", "0" + liSerNum);
//            } else {
//                wp.colSet(liCnt, "ser_num", "" + liSerNum);
//            }
//
//            wp.colSet(liCnt, "crt_date", getSysDate());
//            wp.colSet(liCnt, "bin_type", wp.itemStr("ex_bin_type"));
//            wp.colSet(liCnt, "data_seqno", "" + liSeqno);
//            wp.colSet(liCnt, "pp_card_no", lsPpCardNo);
//            wp.colSet(liCnt, "from_type", "2");
//            wp.colSet(liCnt, "terminal_no", "");
//            wp.colSet(liCnt, "use_city", "");
//            wp.colSet(liCnt, "imp_file_name", wp.itemStr("zz_file_name"));
//            wp.colSet(liCnt, "free_use_cnt", "0");
//            wp.colSet(liCnt, "ch_cost_amt", "0");
//            wp.colSet(liCnt, "guest_cost_amt", "0");
//            wp.colSet(liCnt, "crt_user", wp.loginUser);
//
//            for (int k = 0; k < row.getLastCellNum(); k++) {
//                if (k == 0) {
//                    sheet.setColumnWidth(0, 0);
//                }
//
//                String cellValue = "";
//                cell = row.getCell(k);
//                if (cell == null) {
//                    continue;
//                }
//
//                if (cell.getCellTypeEnum() == CellType.STRING) {
//                    cellValue = cell.getStringCellValue();
//                } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
//                    cellValue = "" + cell.getNumericCellValue();
//                }
//
//                if (wp.itemEq("ex_bin_type", "M")) {
//                    // --wp.col_set(li_cnt,"", cellValue);
//                    if (k == 1) {
//                        wp.colSet(liCnt, "bank_name", cellValue);
//                    } else if (k == 2) {
//                        wp.colSet(liCnt, "deal_type", cellValue);
//                    } else if (k == 3) {
//                        wp.colSet(liCnt, "associate_code", cellValue);
//                    } else if (k == 4) {
//                        wp.colSet(liCnt, "ica_no", cellValue);
//                    } else if (k == 6) {
//                        wp.colSet(liCnt, "cardholder_ename", cellValue);
//                    } else if (k == 7) {
//                        if (cell.getCellTypeEnum() != CellType.NUMERIC)
//                            continue;
//                        double value = cell.getNumericCellValue();
//                        Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
//                        sdf = new SimpleDateFormat("yyyyMMdd");
//                        lsVisitDate = sdf.format(date);
//                        wp.colSet(liCnt, "visit_date", lsVisitDate);
//                        // wp.col_set(li_cnt,"visit_date", wk_visit_date(cellValue));
//                    } else if (k == 8) {
//                        wp.colSet(liCnt, "lounge_name", cellValue);
//                    } else if (k == 9) {
//                        wp.colSet(liCnt, "lounge_code", cellValue);
//                    } else if (k == 10) {
//                        wp.colSet(liCnt, "domestic_int", cellValue);
//                    } else if (k == 11) {
//                        wp.colSet(liCnt, "iso_conty", cellValue);
//                    } else if (k == 12) {
//                        wp.colSet(liCnt, "iso_conty_code", cellValue);
//                    } else if (k == 13) {
//                        wp.colSet(liCnt, "cardholder_visits", cellValue);
//                    } else if (k == 14) {
//                        wp.colSet(liCnt, "guests_count", cellValue);
//                    } else if (k == 15) {
//                        wp.colSet(liCnt, "total_visits", cellValue);
//                    } else if (k == 16) {
//                        wp.colSet(liCnt, "batch_no", cellValue);
//                    } else if (k == 17) {
//                        wp.colSet(liCnt, "voucher_no", cellValue);
//                    } else if (k == 18) {
//                        wp.colSet(liCnt, "mc_billing_region", cellValue);
//                    } else if (k == 19) {
//                        wp.colSet(liCnt, "curr_code", cellValue);
//                    } else if (k == 20) {
//                        wp.colSet(liCnt, "fee_per_holder", cellValue);
//                    } else if (k == 21) {
//                        wp.colSet(liCnt, "fee_per_guest", cellValue);
//                    } else if (k == 22) {
//                        wp.colSet(liCnt, "total_fee", cellValue);
//                    } else if (k == 23) {
//                        wp.colSet(liCnt, "total_free_guests", cellValue);
//                    } else if (k == 24) {
//                        wp.colSet(liCnt, "free_guests_value", cellValue);
//                    } else if (k == 25) {
//                        wp.colSet(liCnt, "tot_charg_guest", cellValue);
//                    } else if (k == 26) {
//                        wp.colSet(liCnt, "charg_guest_value", cellValue);
//                    } else if (k == 27) {
//                        wp.colSet(liCnt, "billing_region", cellValue);
//                    }
//                } else if (wp.itemEq("ex_bin_type", "V")) {
//                    if (k == 2) {
//                        wp.colSet(liCnt, "cardholder_ename", cellValue);
//                    } else if (k == 10) {// --ori:7
//                        if (cell.getCellTypeEnum() != CellType.NUMERIC)
//                            continue;
//                        double value = cell.getNumericCellValue();
//                        Date date = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value);
//                        sdf = new SimpleDateFormat("yyyyMMdd");
//                        lsVisitDate = sdf.format(date);
//                        wp.colSet(liCnt, "visit_date", lsVisitDate);
//                    } else if (k == 3) {
//                        wp.colSet(liCnt, "lounge_name", cellValue);
//                    } else if (k == 8) {// --ori:6
//                        wp.colSet(liCnt, "iso_conty", cellValue);
//                    } else if (k == 11) {// --ori:8
//                        wp.colSet(liCnt, "cardholder_visits", cellValue);
//                    } else if (k == 12) {// --ori:9
//                        wp.colSet(liCnt, "guests_count", cellValue);
//                    } else if (k == 13) {// --ori:10
//                        wp.colSet(liCnt, "total_visits", cellValue);
//                    } else if (k == 17) {// --ori:11
//                        wp.colSet(liCnt, "voucher_no", cellValue);
//                    } else if (k == 5) {// --ori:4
//                        wp.colSet(liCnt, "terminal_no", cellValue);
//                    } else if (k == 7) {// --ori:5
//                        wp.colSet(liCnt, "use_city", cellValue);
//                    }
//                }
//
//            }
//        }
//
//        inExcelFile.close();
//        inExcelFile = null;
//        ilTotCnt = liSerNum;
//        // wp.listCount[0] = li_ser_num;
//
//        wp.logSql = false;
//        queryAfter();
//        wp.logSql = true;
//
//        procFunc();
//
//    }

    void doImportC2() throws Exception {
        if (wp.itemEmpty("zz_file_name")) {
            alertErr("上傳檔名: 不可空白");
            return;
        }
        if (wp.iempty("ex_corp_no")) {
            alertErr("廠商統編: 不可空白");
            return;
        }

        TarokoFileAccess tf = new TarokoFileAccess(wp);
        String exCorpNo = wp.itemStr("ex_corp_no");
        String inputFile = wp.itemStr("zz_file_name");
        String filename = inputFile.substring(0, inputFile.length() - 4);
        filename = filename.substring(0, filename.length() - 9);
        if (!inputFile.toLowerCase().endsWith(".xls")) {
            alertErr("副檔名錯誤!! 需選取xls檔");
            return;
        }
        if (!filename.startsWith("合作金庫機場接送請款")) {
            alertErr("檔名錯誤，檔名必須是“合作金庫機場接送請款”開頭");
            return;
        }
        String ym = filename.substring("合作金庫機場接送請款".length());
        if (ym.length() != 6 || !ym.equals(commDate.monthAdd(ym,0))) {
            alertErr("檔名年月錯誤");
            return;
        }
        if (checkFileExists(inputFile)) {
            alertErr("檔案重覆匯入, 不可匯入");
            return;       
        }

        if (inExcelFile == null) {
            String filePath = TarokoParm.getInstance().getDataRoot()
                    + "/upload/"
                    + inputFile;
//            filePath = SecurityUtil.verifyPath(filePath);
            inExcelFile = new FileInputStream(filePath);
            hwb = new HSSFWorkbook(inExcelFile);
        }

        // --從 0 開始
        // li_sheet_no = (int) (wp.item_num("ex_sheet") -1);
        int liSheetNo = 0;

        hsheet = hwb.getSheetAt(liSheetNo);
        Iterator rowIterator = hsheet.rowIterator();

        int liExcelRows = 0, liCnt = -1, liSerNum = 0;
        String procFlag = "";
        String seqno = "", serviceNo = "", serviceName = "", chiName = "", cardNo = "", purchaseDate = "", serviceArea = "", station = "", serviceItem = "", addAmt = "", baseAmt = "";

        // int file_err = tf.openOutputText(inputFile + ".err", "UTF-8");

        cmsm03.Cmsm5020Func func = new cmsm03.Cmsm5020Func();
        func.setConn(wp);

        wp.itemSet("imp_file_name", inputFile);

        int llOk = 0, llCnt = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        while (rowIterator.hasNext()) {
            hrow = (HSSFRow)rowIterator.next();
            if (hrow == null) {
                break;
            }
            llCnt++;
            if (llCnt == 1)
                continue;
            
            for (int k = 0; k < hrow.getLastCellNum(); k++) {
                String cellValue = "";
                hcell = hrow.getCell(k);
                if (hcell == null) {
                    continue;
                }

                if (hcell.getCellTypeEnum() == CellType.STRING) {
                    cellValue = hcell.getStringCellValue();
                } else if (hcell.getCellTypeEnum() == CellType.NUMERIC) {
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(hcell)) {
                        java.util.Date date =
                                org.apache.poi.ss.usermodel.DateUtil.getJavaDate(hcell
                                        .getNumericCellValue());
                        cellValue = sdf.format(date);
                    } else {
                        cellValue = NumberToTextConverter.toText(hcell.getNumericCellValue());
                    }
                }
                if (k == 0 && NumberUtils.toDouble(cellValue, -1) == -1) {
                    break;
                }
                switch (k) {
                    case 0:
                        seqno = cellValue;
                        break;
                    case 1:
                        serviceNo = cellValue;
                        break;
                    case 2:
                        serviceName = cellValue;
                        break;
                    case 3:
                        chiName = cellValue;
                        break;
                    case 4:
                        cardNo = cellValue;
                        break;
                    case 5:
                        purchaseDate = cellValue;
                        if (purchaseDate == null) {
                            purchaseDate = "";
                        }
                        break;
                    case 6:
                        serviceArea = cellValue;
                        break;
                    case 7:
                        station = cellValue;
                        break;
                    case 8:
                        serviceItem = cellValue;
                        break;
                    case 9:
                        addAmt = cellValue;
                        break;
                    case 10:
                        baseAmt = cellValue;
                        break;
                    default:
                        break;
                }
            }

            if (empty(seqno) && empty(cardNo))
                continue;

            if (!isNumber(seqno) || !isNumber(cardNo)) {
                break;
            }

            liCnt++;
            liSerNum++;

            if (liSerNum < 10) {
                wp.colSet(liCnt, "ser_num", "0" + liSerNum);
            } else {
                wp.colSet(liCnt, "ser_num", "" + liSerNum);
            }
            
            procFlag = "Y";
            if (purchaseDate.length() != 10) {
                // 消費日期錯誤
                procFlag = "4";
            } else {
                purchaseDate = purchaseDate.replaceAll("/", "");
                // 是否存在【crd_card】
                selectCrdCard(cardNo);
                if (sqlNotFind()) {
                    procFlag = "1";
                } else {
                    wp.itemSet("C2.id_p_seqno", wp.colStr("id_p_seqno"));
                    wp.itemSet("C2.major_id_p_seqno", wp.colStr("major_id_p_seqno"));
                    wp.itemSet("C2.acct_type", wp.colStr("acct_type"));
                    wp.itemSet("C2.acno_p_seqno", wp.colStr("acno_p_seqno"));
                    wp.itemSet("C2.major_card_no", wp.colStr("major_card_no"));
                    wp.itemSet("C2.group_code", wp.colStr("group_code"));
                    wp.itemSet("C2.stmt_cycle", wp.colStr("stmt_cycle"));
                    
                    if (!wp.colStr("current_code").equals("0")) {
                        // 本卡是無效卡
                        procFlag = "10";
                    } else {
                        selectCmsAirportList(cardNo, ym);
                        if (sqlNotFind()) {
                            // 卡號不存在優惠名單
                            procFlag = "A";
                        } else if (wp.colStr("mod_type").equals("D")) {
                            procFlag = "B";
                        }
                    }
                }
            }

            wp.itemSet("C2.proc_flag", procFlag);
            wp.itemSet("C2.file_name", filename + ".xls");
            wp.itemSet("C2.service_no", serviceNo);
            wp.itemSet("C2.service_name", serviceName + "_" + serviceArea + "_" + station + "_" + serviceItem);
            wp.itemSet("C2.chi_name", chiName);
            wp.itemSet("C2.card_no", cardNo);
            wp.itemSet("C2.purchase_date", purchaseDate);
            wp.itemSet("C2.purchase_date_e", purchaseDate);
            wp.itemSet("C2.service_area", serviceArea);
            wp.itemSet("C2.service_item", serviceItem);
            wp.itemSet("C2.add_amt", addAmt);
            wp.itemSet("C2.base_amt", baseAmt);
            wp.itemSet("C2.product_no", "08");
            wp.itemSet("C2.project_no", "機場接送");
            wp.itemSet("C2.vd_flag", "N");
            wp.itemSet("C2.file_type", "01");
//            wp.itemSet("C2.crd_date", this.busiDate());
//            wp.itemSet("C2.crd_time", "N");

            if (func.dbInsertC2() == 1) {
                llOk++;
            }
        }

        wp.notFound="";
        if (llOk > 0) {
            sqlCommit(1);
        } else {
            sqlCommit(-1);
        }

        inExcelFile.close();
        inExcelFile = null;
//        tf.deleteFile(inputFile);

        alertMsg("資料匯入處理筆數: "
                + (llCnt - 1)
                + ", 成功筆數="
                + llOk
                + "; 匯入批號="
                + func.isBatchNo);
        wp.colSet("zz_file_name", "");
        return;
    }

    private boolean checkFileExists(String filename) {
        wp.sqlCmd = "select 1 from bil_mcht_apply_tmp where FILE_NAME = ?";
        pageSelect(new Object[] {
                filename
        });
        if (sqlNotFind()) {
            return false;
        }
        return true;
    }
    
    private void selectCrdCard(String cardNo) {
        String sql = "select id_p_seqno, major_id_p_seqno, acct_type,acno_p_seqno, major_card_no"
                + ", group_code,stmt_cycle,card_no, current_code, current_code "
                + "from crd_card where card_no = ? ";
        sqlSelect(sql, new Object[] {cardNo});
    }
    
    // 請款檔的YYYYMM
    private void selectCmsAirportList(String cardNo, String ym) {
        String sql = "select mod_type "
                + "from cms_airport_list where USE_MONTH=? and MOD_TYPE in ('A', 'U','D') and CARD_NO=? ";
        sqlSelect(sql, new Object[] {ym, cardNo});
    }

    @Override
    public void queryFunc() throws Exception {

    }

//    void queryAfter() throws Exception {
//        double lmChAmt = 0, lmGuestAmt = 0;
//        String lsPpcardNo = "";
//        int llErr = 0, liFree = 0, liUse = 0;
//        busi.func.UcPpcard ucpp = new busi.func.UcPpcard();
//        ucpp.setConn(wp);
//
//        for (int ii = 0; ii < ilTotCnt; ii++) {
//
//            lsPpcardNo = wp.colStr(ii, "pp_card_no");
//            // if(ucpp.get_Cardholder(ls_ppcard_no)!=1){
//            // wp.col_set(ii,"proc_flag", "N");
//            // wp.col_set(ii,"db_errmsg", ucpp.getMsg());
//            // ll_err++;
//            // continue;
//            // }
//
//            String lsCardNo = ucpp.getCardNo(lsPpcardNo);
//            if (empty(lsCardNo)) {
//                llErr++;
//                wp.colSet(ii, "proc_flag", "N");
//                wp.colSet(ii, "db_errmsg", ucpp.getMsg());
//                continue;
//            }
//            wp.colSet(ii, "card_no", lsCardNo);
//            wp.colSet(ii, "id_no", ucpp.idNo);
//            wp.colSet(ii, "id_no_code", ucpp.idnoCode);
//            wp.colSet(ii, "id_p_seqno", ucpp.idPseqno);
//            wp.colSet(ii, "mcht_no", ucpp.mchtNo);
//
//            // --check free use
//            liFree = ucpp.getFreeCnt(lsPpcardNo, wp.colStr(ii, "visit_date"));
//            liUse = (int) wp.colNum("cardholder_visits");
//
//            if (liFree >= liUse) {
//                wp.colSet(ii, "free_use_cnt", liUse);
//            } else {
//                wp.colSet(ii, "free_use_cnt", liFree);
//            }
//
//            if (ucpp.getVmjAmt(wp.colStr(ii, "bin_type")) == 1) {
//                wp.colSet(ii, "ch_cost_amt", ucpp.imHolderAmt);
//                wp.colSet(ii, "guest_cost_amt", ucpp.imGuestAmt);
//            }
//
//            lmChAmt = (wp.colNum(ii, "cardholder_visits") - wp.colNum(ii, "free_use_cnt"))
//                    * wp.colNum(ii, "ch_cost_amt ");
//            wp.colSet(ii, "wk_ch_amt", lmChAmt);
//
//            lmGuestAmt = wp.colNum("guest_cost_amt") * wp.colNum("guests_count");
//            wp.colSet(ii, "wk_guest_amt", lmGuestAmt);
//        }
//    }

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

        Cmsm5020Func func = new Cmsm5020Func();
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
//        int llOk = 0, llErr = 0;
//
//        cmsm03.Cmsm5020Func func = new cmsm03.Cmsm5020Func();
//        func.setConn(wp);
//
//        for (int ii = 0; ii < ilTotCnt; ii++) {
//            if (wp.colEq(ii, "proc_flag", "N") == false) {
//                int liRc = func.insertVist(ii);
//                if (liRc == 1) {
//                    llOk++;
//                    continue;
//                }
//                wp.colSet(ii, "db_errmsg", func.getMsg());
//            }
//            // -error-
//            llErr++;
//            listErrData(ii);
//        }
//        if (llOk > 0) {
//            sqlCommit(1); // one-time-commit
//        }
//
//        // wp.listCount[0] = ll_err;
//        wp.setListSernum(0, "", llErr);
//        alertMsg("資料匯入完成 , 成功:" + llOk + " 失敗:" + llErr);
//
//        if (llErr > 0) {
//            try {
//                log("xlsFunction: started--------");
//                wp.reportId = "cmsm5020";
//                wp.colSet("user_id", wp.loginUser);
//                TarokoExcel xlsx = new TarokoExcel();
//                wp.fileMode = "Y";
//                xlsx.excelTemplate = "cmsp3140.xlsx";
//                wp.pageRows = 9999;
//                xlsx.processExcelSheet(wp);
//                xlsx.outputExcelUrl();
//                String ls_url = wp.linkURL;
//                xlsx = null;
//                wp.colSet("url_err_file", ls_url);
//                wp.colSet("err_file", wp.exportFile);
//                wp.linkURL = "";
//                wp.linkMode = "";
//                log("xlsFunction: ended-------------");
//            } catch (Exception ex) {
//                wp.expMethod = "xlsPrint";
//                wp.expHandle(ex);
//            }
//        }
    }

//    void listErrData(int ll) {
//        rr++;
//
//        wp.colSet(rr, "er_crt_date", wp.colStr(ll, "crt_date"));
//        wp.colSet(rr, "er_bin_type", wp.colStr(ll, "bin_type"));
//        wp.colSet(rr, "er_data_seqno", wp.colStr(ll, "data_seqno"));
//        wp.colSet(rr, "er_pp_card_no", wp.colStr(ll, "pp_card_no"));
//        wp.colSet(rr, "er_from_type", wp.colStr(ll, "from_type"));
//        wp.colSet(rr, "er_terminal_no", wp.colStr(ll, "terminal_no"));
//        wp.colSet(rr, "er_city", wp.colStr(ll, "use_city"));
//        wp.colSet(rr, "er_imp_file_name", wp.colStr(ll, "imp_file_name"));
//        wp.colSet(rr, "er_free_use_cnt", wp.colStr(ll, "free_use_cnt"));
//        wp.colSet(rr, "er_ch_cost_amt", wp.colStr(ll, "ch_cost_amt"));
//        wp.colSet(rr, "er_guest_cost_amt", wp.colStr(ll, "guest_cost_amt"));
//        wp.colSet(rr, "er_crt_user", wp.colStr(ll, "crt_user"));
//        wp.colSet(rr, "er_id_no", wp.colStr(ll, "id_no"));
//        wp.colSet(rr, "er_id_no_code", wp.colStr(ll, "id_no_code"));
//        wp.colSet(rr, "er_id_p_seqno", wp.colStr(ll, "id_p_seqno"));
//        wp.colSet(rr, "er_mcht_no", wp.colStr(ll, "mcht_no"));
//        wp.colSet(rr, "er_card_no", wp.colStr(ll, "card_no"));
//        wp.colSet(rr, "er_bank_name", wp.colStr(ll, "bank_name"));
//        wp.colSet(rr, "er_deal_type", wp.colStr(ll, "deal_type"));
//        wp.colSet(rr, "er_associate_code", wp.colStr(ll, "associate_code"));
//        wp.colSet(rr, "er_ica_no", wp.colStr(ll, "ica_no"));
//        wp.colSet(rr, "er_cardholder_ename", wp.colStr(ll, "cardholder_ename"));
//        wp.colSet(rr, "er_visit_date", wp.colStr(ll, "visit_date"));
//        wp.colSet(rr, "er_lounge_name", wp.colStr(ll, "lounge_name"));
//        wp.colSet(rr, "er_lounge_code", wp.colStr(ll, "lounge_code"));
//        wp.colSet(rr, "er_domestic_int", wp.colStr(ll, "domestic_int"));
//        wp.colSet(rr, "er_iso_conty", wp.colStr(ll, "iso_conty"));
//        wp.colSet(rr, "er_iso_conty_code", wp.colStr(ll, "iso_conty_code"));
//        wp.colSet(rr, "er_cardholder_visits", wp.colStr(ll, "cardholder_visits"));
//        wp.colSet(rr, "er_guests_count", wp.colStr(ll, "guests_count"));
//        wp.colSet(rr, "er_total_visits", wp.colStr(ll, "total_visits"));
//        wp.colSet(rr, "er_batch_no", wp.colStr(ll, "batch_no"));
//        wp.colSet(rr, "er_voucher_no", wp.colStr(ll, "voucher_no"));
//        wp.colSet(rr, "er_mc_billing_region", wp.colStr(ll, "mc_billing_region"));
//        wp.colSet(rr, "er_curr_code", wp.colStr(ll, "curr_code"));
//        wp.colSet(rr, "er_fee_per_holder", wp.colStr(ll, "fee_per_holder"));
//        wp.colSet(rr, "er_fee_per_guest", wp.colStr(ll, "fee_per_guest"));
//        wp.colSet(rr, "er_total_fee", wp.colStr(ll, "total_fee"));
//        wp.colSet(rr, "er_total_free_guests", wp.colStr(ll, "total_free_guests"));
//        wp.colSet(rr, "er_free_guests_value", wp.colStr(ll, "free_guests_value"));
//        wp.colSet(rr, "er_tot_charg_guest", wp.colStr(ll, "tot_charg_guest"));
//        wp.colSet(rr, "er_charg_guest_value", wp.colStr(ll, "charg_guest_value"));
//        wp.colSet(rr, "er_billing_region", wp.colStr(ll, "billing_region"));
//        wp.colSet(rr, "er_errmsg", wp.colStr(ll, "db_errmsg"));
//    }

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
}
