
package cmsm03;

/**
 * 新貴通卡/龍騰卡請款匯入作業
 * 2021-0111:  Tom Hsu    Cmsp3150.java Version2
 * 匯入txt檔 *
 */

import busi.FuncAction;
import taroko.com.TarokoMail;
//import taroko.com.passwordAuthentication;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.mail.Session;

import org.apache.poi.util.SystemOutLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import Dxc.Util.SecurityUtil;
import ofcapp.BaseAction;
import ofcapp.InfaceExcel;
import taroko.com.TarokoFileAccess;
import taroko.com.TarokoParm;

import java.io.FileInputStream;


@SuppressWarnings({"unchecked", "deprecation"})
public class Cmsp3150 extends BaseAction {
    taroko.base.CommDate zzdate = new taroko.base.CommDate();


    SimpleDateFormat sdf = null;
    InputStream inTxtFile = null;
    int file_total_cnt = 0;
    int ilTotCnt = 0;
    int rr = -1;
    int rc = 1;
    String filePath = "";
    String lineLength = "";
    String inputFile = "";
    String wb = "";
    String ss = "";
    String ex_vip_kind = "";
    String ex_bin_type = "";
    String zz_file_name = "";
    String id_p_seqno = "", id_no1 = "", eng_name1 = "";
    String lsPpcardNo = "";

    String id_no = "";

    int detai;
    int rowCnt = 0;
    int llOk = 0;
    int ii = 0;
    int data_seqno = 0;
    int errCode = 0;
    int rowCount = 0;
    int impSuccess = 0, impFail = 0;

    //Sendmail 的參數
    String mailServer = "";
    String portNo = "";
    String from = "";
    String to = "";
    String subject = "";
    String bodyText = "";
    String attachFile = "";
    String e_mail = "";
    String sendMail = "";
    String sendEmail = "";
    String TarokoMail1 = "";
    String username = "";
    String holderAmt = "";
    String togetAmt = "";
    String pp_card_no = "";
    String err_code = "";
    int feePerHolder = 0, feePerGuest = 0, totalFee = 0;
    int chargGuestValue = 0, freeUseCnt = 0, totChargGuest;
    int chCostAmt = 0, guestCostAmt = 0;
    double liFree = 0, liUse = 0;
    double lmChAmt = 0, lmGuestAmt = 0;
    String chVisits = "";
    String guestsCount = "";
    String dataHead = "";
    static BufferedWriter writerError = null; // insert/update 有誤將 msg 及完整資料寫入
    static OutputStreamWriter oswError = null;

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

        } else if (eqIgno(wp.buttonCode, "D")) {
            /* 刪除功能 */
            saveFunc();
        } else if (eqIgno(wp.buttonCode, "M")) {
            /* 瀏覽功能 :skip-page */
            queryRead();
        } else if (eqIgno(wp.buttonCode, "S")) {
            /* 動態查詢 */
            querySelect();
        } else if (eqIgno(wp.buttonCode, "UPLOAD")) {
            procFunc();
        } else if (eqIgno(wp.buttonCode, "L")) {
            /* 清畫面 */
            strAction = "";
            clearFunc();
        } else if (eqIgno(wp.buttonCode, "C")) {
            // -資料處理-
            procaFunc();
        } else if (eqIgno(wp.buttonCode, "C1")) {
            // -資料處理-
            procaFunc();
        }

    }


    @Override
    public void dddwSelect() {
        // TODO Auto-generated method stub

    }


    @Override
    public void queryFunc() throws Exception {


        if (itemIsempty("zz_file_name")) {
            alertErr2("輸入檔名不可空白");
            return;
        }

        if (inTxtFile == null) {
            String filePath = TarokoParm.getInstance().getDataRoot() + "/upload/" + wp.itemStr("zz_file_name");
            filePath = SecurityUtil.verifyPath(filePath);
            inTxtFile = new FileInputStream(filePath);
        }

        if (wp.itemStr("zz_file_name").endsWith(".txt") || wp.itemStr("zz_file_name").endsWith(".TXT")) {
        } else {
            // 檢核輸入的附檔名是否為.txt 或TXT
            alertErr2("輸入附檔檔名應該為.txt 或TXT");
            return;
        }


        if (wp.itemEq("ex_bin_type", "M")) {
            ex_bin_type = "M";
        } else if (wp.itemEq("ex_bin_type", "V")) {
            ex_bin_type = "V";
        } else if (wp.itemEq("ex_bin_type", "J")) {
            ex_bin_type = "J";
        }
        if (wp.itemEq("ex_vip_kind", "1")) {
            ex_vip_kind = "1";
        } else if (wp.itemEq("ex_vip_kind", "2")) {
            ex_vip_kind = "2";
        }

        switch (wp.itemStr("ex_vip_kind")) {
            case "1": {
                if ((wp.itemStr("zz_file_name").substring(0, 6).equals("PPDATA")) == true) {
                    fileDataPPCOUNT1();
                }

                if ((wp.itemStr("zz_file_name").substring(0, 6).equals("PPDATA")) == false) {
                    errmsg("檔名錯誤，檔名必須是PPDATA開頭");

                    return;
                }
                break;
            }

            case "2": {
                if ((wp.itemStr("zz_file_name").substring(0, 14).equals("GUEST_68861403")) == true) {
                    fileDataGUESTCOUNT1();
                }

                if ((wp.itemStr("zz_file_name").substring(0, 14).equals("GUEST_68861403")) == false) {
                    errmsg("檔名錯誤，檔名必須是GUEST_68861403開頭");
                    return;
                }
                break;
            }
        }
    }


//@Override

    public void procaFunc() throws Exception {
    }


    //新貴通卡計算上傳檔案的筆數,check dataHead 是否是正確的1或2或3
    void fileDataPPCOUNT1() throws Exception {
        wp.pageControl();


        int llErr = 0;
        TarokoFileAccess tf = new TarokoFileAccess(wp);

        String inputFile = wp.itemStr("zz_file_name");

        int fi = tf.openInputText(inputFile, "MS950");

        if (fi == -1) {
            return;
        }

        // int fileErr = tf.openOutputText(inputFile + ".err", "UTF-8");
        int fileErr = tf.openOutputText(inputFile + ".err", "MS950");

        cmsm03.Cmsp3150Func func = new cmsm03.Cmsp3150Func();

        func.setConn(wp);
        wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));


        while (true) {
            String wpa = tf.readTextFile(fi);

            if (tf.endFile[fi].equals("Y")) {
                break;
            } else {
                dataHead = wpa.substring(0, 1).trim();
                System.out.println("dataHead" + dataHead);
                if (dataHead.equals("2")) {
                    //計算除了頭尾兩筆以外,真正要Parsing 的筆數
                    rowCnt = rowCnt + 1;
                }
                if ((dataHead.equals("1") || dataHead.equals("2") || dataHead.equals("3")) == false) {
                    //計算dataHead 不是1,2,3 的筆數
                    rowCount = rowCount + 1;
                    //如果有一筆的dataHead 不是1,2,3,即顯示資料錯誤,程式結束,資料不存入資料庫
                    errmsg("資料內容格式不正確,匯入失敗");
                    return;
                }
            }

        }
        if (rowCount == 0) {
            //如果dataHead 全部是1,2,3 則進行後續的Parsing,資料檢核及資料謝入等相關作業
            fileDataImpPPDATA1();
        }
        //列印這個上傳檔案的筆數,除了頭尾兩筆以外,真正要Parsing 的筆數
        System.out.println("rowCnt" + rowCnt);
    }


    //新貴通卡上傳資料的Parsing 及相關的檢查項目
    void fileDataImpPPDATA1() throws Exception {

        wp.pageControl();

        int llErr = 0;

        TarokoFileAccess tf = new TarokoFileAccess(wp);

        //String inputFile = wp.dataRoot + "/upload/" + ("zz_file_name");
        String inputFile = wp.itemStr("zz_file_name");

        int fi = tf.openInputText(inputFile, "MS950");

        if (fi == -1) {
            return;
        }

        int fileErr = tf.openOutputText(inputFile + ".err", "MS950");

        cmsm03.Cmsp3150Func func = new cmsm03.Cmsp3150Func();

        func.setConn(wp);


        wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));

        int llOK = 0;
        int llCnt = 0;
        ii = 0;
        detai = 0;
        String crtDate = "";
        String ppCardNo = "";
        String idPSeqnoa = "";
        String idNoa = "";
        String errCode = "";
        String errDesc = "";
        String uploadDateTrans = "";
        String totCount = "";
        String idNoCodea = "";
        String inPersonCount = "";

        while (true) {
            //while (llCnt<4) {
            String wpa = tf.readTextFile(fi);

            if (tf.endFile[fi].equals("Y")) {

                break;
            } else {
                dataHead = wpa.substring(0, 1);
            }
            if (wpa.length() < 2) {
                file_total_cnt++;
            }


            wp.colSet(llCnt, "imp_file_name", wp.itemStr("zz_file_name"));
            if (dataHead.equals("1")) {
                wp.colSet(llCnt, "tot_count", wpa.substring(1, 11).trim());
                totCount = wpa.substring(1, 11).trim();
                wp.colSet(llCnt, "total_use_count", totCount);
                inPersonCount = wpa.substring(21, 26).trim();
                System.out.println("inPersonCount" + inPersonCount);
                wp.colSet(llCnt, "in_person_count", inPersonCount);
                wp.colSet(llCnt, "uoload_date", wpa.substring(26, 33).trim());
                crtDate = (Integer.parseInt(wpa.substring(26, 29)) + 1911) + wpa.substring(29, 31) + wpa.substring(31, 33);
                uploadDateTrans = (Integer.parseInt(wpa.substring(26, 29)) + 1911) + wpa.substring(29, 31) + wpa.substring(31, 33);
                wp.colSet(llCnt, "uoload_date_trans", (Integer.parseInt(wpa.substring(26, 29)) + 1911) + wpa.substring(29, 31) + wpa.substring(31, 33));
                wp.colSet(llCnt, "crt_date", (Integer.parseInt(wpa.substring(26, 29)) + 1911) + wpa.substring(29, 31) + wpa.substring(31, 33));
                wp.colSet(llCnt, "file_type", wpa.substring(33, 34).trim());

                String sql2 = " select id_p_seqno from cms_ppcard_visit where 1=1 and vip_kind='1' and crt_date = ? ";
                // sqlSelect(sql2, new Object[] {upload_DateTrans[0]});
                sqlSelect(sql2, new Object[]{uploadDateTrans});

                if (sqlRowNum > 0) {
                    alertErr2("PP卡請款檔重覆轉入失敗");
                    return;
                }


                System.out.println("Integer.parseInt(totCount)\n" + Integer.parseInt(totCount));
                System.out.println("rowCnt\n" + rowCnt);
                if ((Integer.parseInt(totCount) == rowCnt) == false) {
                    errmsg("檔案內筆數與明細筆數不同,匯入失敗\n" + rowCnt);
                    return;
                }

                String sql8 = "select " + " count(*) as allCount ,max(data_seqno) as li_data_seqno " + " from cms_ppcard_visit "
                        + " where crt_date =?" + " and from_type = '2' "
                        + " and bin_type = ? ";
                sqlSelect(sql8, new Object[]{uploadDateTrans, ex_bin_type});
                System.out.println("(sqlStr(li_data_seqno))" + (sqlStr("li_data_seqno")));
                data_seqno = Integer.parseInt(sqlStr("li_data_seqno")) + 1;
            }

            if (dataHead.equals("2")) {
                ppCardNo = wpa.substring(1, 19).trim();
                wp.colSet(llCnt, "pp_card_no", ppCardNo);
                ppCardNo = wpa.substring(1, 19).trim();
                wp.colSet(llCnt, "ch_ename", wpa.substring(19, 37).trim());
                wp.colSet(llCnt, "room_name", wpa.substring(37, 47).trim());
                wp.colSet(llCnt, "iso_conty", wpa.substring(47, 57).trim());
                wp.colSet(llCnt, "visit_date", (Integer.parseInt(wpa.substring(57, 60)) + 1911) + wpa.substring(60, 62) + wpa.substring(62, 64));
                //使用日期轉換為西元年,以存入資料庫
                wp.colSet(llCnt, "vip_use_date_trans", (Integer.parseInt(wpa.substring(57, 60)) + 1911) + wpa.substring(60, 62) + wpa.substring(62, 64));
                wp.colSet(llCnt, "ch_visits", wpa.substring(64, 66).trim());
                wp.colSet(llCnt, "guests_count", wpa.substring(66, 68).trim());
                wp.colSet(llCnt, "total_visits", wpa.substring(68, 70).trim());
                //wp.colSet( llCnt,"pymt_cond",wpa.substring(68,70).trim());
                if (wpa.length() > 71) {
                    wp.colSet(llCnt, "pp_id", wpa.substring(70, 80).trim());
                }
                if (wpa.length() > 81) {
                    wp.colSet(llCnt, "ppcard_credit_card_no", wpa.substring(80, 96).trim());
                }
                if (wpa.length() > 96) {
                    wp.colSet(llCnt, "free_tot_count", wpa.substring(96, 101).trim());
                }
                if (wpa.length() > 101) {
                    wp.colSet(llCnt, "pymt_tot_count", wpa.substring(101, 106).trim());
                }
                if (wpa.length() > 106) {
                    wp.colSet(llCnt, "pymt_person_count", wpa.substring(106, 111).trim());
                }
                if (wpa.length() > 111) {
                    wp.colSet(llCnt, "pymt_fail_count", wpa.substring(111, 116).trim());
                }
                if (wpa.length() > 116) {
                    wp.colSet(llCnt, "pymt_fail_person_count", wpa.substring(116, 121).trim());
                }
                if (wpa.length() > 121) {
                    wp.colSet(llCnt, "pymt_fail_tot_count", wpa.substring(121, 126).trim());
                }


                errCode = "0";
                wp.colSet(llCnt, "data_seqno", "" + data_seqno);

                String sql3 = " select id_p_seqno as id_p_seqno  from crd_card_pp where 1=1 and vip_kind='1' and pp_card_no = ? ";
                sqlSelect(sql3, new Object[]{ppCardNo});

                if (sqlRowNum == 0) {
                    errCode = "02";
                    errDesc = "pp卡卡卡號" + ppCardNo + "不存在";
                }

                //check pp_no 是否存在
                // sqlSelect(sql4, new Object[] {sqlStr("id_p_seqno")});
                String sql4 = " select a.id_p_seqno as id_p_seqno,b.id_no as id_no1,a.eng_name as eng_name1,b.id_no_code as id_no_code from crd_card_pp a, crd_idno b  where 1=1 and a.vip_kind='1' "
                        + " and b.id_p_seqno=a.id_p_seqno and a.pp_card_no = ?  ";
                // String sql4 = " select a.id_p_seqno as id_p_seqno,b.id_no as id_no1,b.eng_name as eng_name1,b.id_no_code as id_no_code from crd_card_pp a join crd_idno b on b.id_p_seqno=a.id_p_seqno where 1=1 and a.vip_kind='1' "
                //	+"and a.pp_card_no = ?  ";
                sqlSelect(sql4, new Object[]{ppCardNo});

                if (errCode.equals("02")) {
                    idPSeqnoa = "";
                    idNoa = "";
                    idNoCodea = "";
                } else {
                    idPSeqnoa = sqlStr("id_p_seqno");
                    idNoa = sqlStr("id_no1");
                    idNoCodea = sqlStr("id_no_code");
                }

                //check 持卡人ID_P_SEQNO是否存在
                if (sqlStr("id_p_seqno").isEmpty()) {
                    if ((errCode.length() > 1) == false) {
                        errCode = "03";
                        errDesc = "持卡人ID_P_SEQNO不存在";
                    }
                }

                //check 持卡人英文姓名是否相同
                if (wpa.substring(19, 37).trim().length() > 0) {
                    //檢核txt的英文姓名是否有值,有值:需再檢核與【CRD_CARD_PP】ENG_NAME是否相同
                    if (wpa.substring(19, 37).trim().equals(sqlStr("eng_name1")) == false) {
                        if ((errCode.length() > 1) == false) {

                            errCode = "05";
                            errDesc = "持卡人英文姓名不同";
                        }
                    }
                }


                //check 使用次數 & 攜帶人數同時＜=0(or空白)
                if ((Integer.parseInt(wpa.substring(64, 66).trim()) == 0) && (Integer.parseInt(wpa.substring(64, 66).trim()) == 0)) {
                    if ((errCode.length() > 1) == false) {
                        errCode = "07";
                        errDesc = "使用次數 & 攜帶人數同時＜=0(or空白),匯入失敗";
                    }
                }

                //check 持卡人ID是否存在
                if ((wpa.substring(70, 80).trim()).length() > 0) {
                    //檢核txt的持卡人(IDIN-PP-ID)是否有值,有值:需再檢核與【CRD_IDNO】的ID_NO是否相同，不同:寫入 ”持卡人ID不存在”訊息。
                    if (wpa.substring(70, 80).trim().equals(sqlStr("id_no1")) == false) {
                        if ((errCode.length() > 1) == false) {
                            errCode = "04";
                            errDesc = "持卡人ID不存在";
                        }
                    }
                }


                //check crd_no 是否存在
                if ((wpa.substring(80, 96).trim()).length() > 0) {
                    //檢核txt的信用卡卡號是否有值,有值:以該卡號讀取【CRD_CARD】的CARD_NO是否存在
                    String sql5 = " select * from crd_card where 1=1 and card_no = ? ";
                    sqlSelect(sql5, new Object[]{wpa.substring(80, 96).trim()});
                    if (sqlRowNum == 0) {
                        if ((errCode.length() > 1) == false) {
                            errCode = "06";
                            errDesc = "信用卡卡號" + wpa.substring(80, 96).trim() + "不存在";
                        }
                    }
                }


                //擷取新貴通卡卡友自費金額(ch_cost_amt),非卡友自費金額 ,單價(同行)( guest_cost_amt):同行旅客金額(TOGET_AMT)等相關資料
                //以貴賓卡卡號讀取【crd_card_pp】join【MKT_PPCARD_ISSUE 新貴通貴賓室發行參數檔】
                if (ex_vip_kind.equals("1")) {
                    String sql6 = " select * from crd_card_pp a, mkt_ppcard_issue b"
                            + "    where 1=1 and  b.bin_type=a.bin_type and b.group_code=a.group_code and b.vip_kind=a.vip_kind and b.vip_kind='1' and a.pp_card_no = ? ";
                    sqlSelect(sql6, new Object[]{ppCardNo});
                    if (sqlRowNum > 0) {
                        String sql7 = " select b.holder_amt as holderAmt,b.toget_amt as togetAmt from crd_card_pp a,mkt_ppcard_issue b  where 1=1 and b.vip_kind='1' "
                                + " and a.bin_type=b.bin_type and a.group_code=b.group_code and a.vip_kind=b.vip_kind and a.pp_card_no = ? ";
                        sqlSelect(sql7, new Object[]{ppCardNo});
                        chCostAmt = Integer.parseInt(sqlStr("holderAmt"));
                        wp.colSet(llCnt, "ch_cost_amt", chCostAmt);
                        guestCostAmt = Integer.parseInt(sqlStr("togetAmt"));
                        wp.colSet(llCnt, "guest_cost_amt", guestCostAmt);
                    } else {
                        chCostAmt = 0;
                        guestCostAmt = 0;
                        wp.colSet(llCnt, "ch_cost_amt", 0);
                        wp.colSet(llCnt, "guest_cost_amt", 0);
                    }
                }

                if (wpa.substring(96, 101).trim().length() > 0) {
                    //fee_per_holder= (ch_visits - free_use_cnt)* ch_cost_amt
                    feePerHolder = (Integer.parseInt(wpa.substring(64, 66).trim()) - Integer.parseInt(wpa.substring(96, 101).trim())) * chCostAmt;
                } else {
                    feePerHolder = (Integer.parseInt(wpa.substring(64, 66).trim()) - 0) * chCostAmt;
                }

                if ((feePerHolder > 0) == false) {
                    feePerHolder = 0;
                }
                if (wpa.substring(96, 101).trim().length() > 0) {
                    //如果免收費使用次數>0, 則fee_per_guest= pymt-person-count * guest_cost_amt
                    feePerGuest = ((Integer.parseInt(wpa.substring(106, 111).trim())) * guestCostAmt);
                } else {
                    //如果免收費使用次數=0 ,則fee_per_guest=0
                    feePerGuest = 0;
                }

                totalFee = feePerHolder + feePerGuest;
                totChargGuest = (Integer.parseInt(wpa.substring(64, 66).trim())) - freeUseCnt;

                //相乘數值如有其中一個值是0，程式需判斷不進行運算，避免出現exception，該欄填入0
                if (((feePerHolder != 0) && (chCostAmt != 0)) || (((Integer.parseInt(wpa.substring(66, 68).trim())) != 0) && (guestCostAmt != 0))) {
                    //可收取的客人價值(charg_guest_value)= (使用次數-免費使用次數)*(卡友單次自費金額)+攜帶人數*同行旅客金額

                    chargGuestValue = ((feePerHolder) + ((Integer.parseInt(wpa.substring(66, 68).trim())) * guestCostAmt));
                    //  charg_guest_value=(( (Integer.parseInt(wpa.substring(64, 66).trim())- freeUseCnt) * chCostAmt));
                } else {
                    chargGuestValue = 0;
                }

                wp.colSet(llCnt, "feePerHolder", feePerHolder);
                wp.colSet(llCnt, "fee_per_guest", feePerGuest);
                wp.colSet(llCnt, "total_fee", totalFee);
                wp.colSet(llCnt, "tot_charg_guest", totChargGuest);
                wp.colSet(llCnt, "charg_guest_value", chargGuestValue);
                if (chargGuestValue > 0) {
                    wp.colSet(llCnt, "charg_guest_value", chargGuestValue);
                } else {
                    wp.colSet(llCnt, "charg_guest_value", 0);
                }

                wp.colSet(llCnt, "crt_date", crtDate);
                wp.colSet(llCnt, "terminal_no", "");
                wp.colSet(llCnt, "city", "");
                wp.colSet(llCnt, "bin_type", wp.itemStr("ex_bin_type"));
                wp.colSet(llCnt, "id_p_seqno", sqlStr("id_p_seqno"));
                wp.colSet(llCnt, "id_no", sqlStr("id_no1"));
                wp.colSet(llCnt, "id_no_code", sqlStr("id_no_code"));
                wp.colSet(llCnt, "imp_file_name", wp.itemStr("zz_file_name"));
                wp.colSet(llCnt, "free_use_cnt", "0");
                wp.colSet(llCnt, "crt_user", wp.loginUser);
                if (wp.itemEq("ex_bin_type", "M")) {
                    wp.colSet(llCnt, "deal_type", "MC Platinum");
                    wp.colSet(llCnt, "associate_code", "700013");
                    wp.colSet(llCnt, "ica_no", "3768");
                }
                wp.colSet(llCnt, "from_type", "2");
                wp.colSet(llCnt, "bank_name", "Taiwan Cooperative Bank");

                //if (wpa.substring(0, 1). equals ("2") ){
                //將errCode 及errDesc 寫入colSet 以便insert 進入資料庫
                if (errCode.length() > 1) {
                    wp.colSet(llCnt, "err_code", errCode);
                    wp.colSet(llCnt, "err_desc", errDesc);
                    impFail = impFail + 1;
                } else {
                    wp.colSet(llCnt, "err_code", "00");
                    wp.colSet(llCnt, "err_desc", "成功");
                    impSuccess = impSuccess + 1;
                }

                data_seqno++;
                llCnt++;
                detai++;
                ii++;
            }
        }
        tf.closeOutputText(fileErr);
        tf.closeInputText(fi);

        //insert 進資料庫
        procFunc();

        return;
    }


    //龍騰卡計算上傳檔案的筆數,check dataHead 是否是正確的1或2或3,另外檢查三種情況,若不符合就誘發mail 通知卡部
    void fileDataGUESTCOUNT1() throws Exception {
        wp.pageControl();

        int llErr = 0;
        String bankNo = "";
        String buttomBankNo = "";
        String totUploadNum = "";
        String guestUploadDate = "";
        int llCnt = 0;

        TarokoFileAccess tf = new TarokoFileAccess(wp);

        String inputFile = wp.itemStr("zz_file_name");

        int fi = tf.openInputText(inputFile, "MS950");


        if (fi == -1) {
            return;
        }

        int fileErr = tf.openOutputText(inputFile + ".err", "MS950");

        Cmsp3150Func func = new Cmsp3150Func();

        func.setConn(wp);

        wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));

        // MAIL 相關程式,email通知信用卡部經辦。暫時先讀取【PTR_SENDER_LIST】where DEPT_NO=’ZZ’的EMAIL欄的值，且STOP_FLAG=N(啟用)，才能發出信件。目前先以Tom 的mail做測試

        String sql5 = "select email as e_mail1 from ptr_sender_list where dept_no='ZZ'  and STOP_FLAG='N'";
        sqlSelect(sql5);

//    TarokoMail tarokoMail1=new TarokoMail();
//		            mailServer = "smtp.gmail.com";
//		          portNo = "25";
        // portNo = "587";

        //to = "shyu.tsanggour38@gmail.com";
        //to = "tom.hsu@dxc.com";
        to = sqlStr("e_mail1");

        subject = "龍騰卡請款記錄檔,匯入失敗\n";
        // bodyText = "檢核檔案內[傳檔日期]與檔案名稱日期不同";
        //attachFile = wp.itemStr("zz_file_name");;
        // attachFile = "test mail";
        // username="shyu.tsanggour38@gmail.com";
        // password="Q1835aa$";

//		          tarokoMail1.setMailServer(mailServer);
//		          tarokoMail1.setPortNo(portNo);
//		          tarokoMail1.setFrom(from);
//    tarokoMail1.setTo(to);
//    tarokoMail1.setSubject(subject);
        // tarokoMail1.setBodyText(bodyText);
        // tarokoMail1.setAttachFile(attachFile);

        // MAIL 相關程式


        while (true) {
            String wpb = tf.readTextFile(fi);
            if (tf.endFile[fi].equals("Y")) {
                break;
            } else {
                //arrCount=arrCount+1;
                dataHead = (wpb.substring(0, 1)).trim();
                if (dataHead.equals("2")) {
                    //計算除了頭尾兩筆以外,真正要Parsing 的筆數
                    rowCnt = rowCnt + 1;
                }
                if ((dataHead.equals("1") || dataHead.equals("2") || dataHead.equals("3")) == false) {
                    //計算dataHead 不是1,2,3 的筆數
                    rowCount = rowCount + 1;
                    //如果有一筆的dataHead 不是1,2,3,即顯示資料錯誤,程式結束,資料不存入資料庫
                    errmsg("資料內容格式不正確,匯入失敗");
                    return;
                }
                if (dataHead.equals("1")) {
                    wp.colSet(llCnt, "bank_no", wpb.substring(1, 11));
                    bankNo = wpb.substring(1, 11);
                    wp.colSet(llCnt, "bank_no", bankNo);
                    guestUploadDate = wpb.substring(11, 19).trim();

                    if (inputFile.substring(15, 23).equals(guestUploadDate) == false) {
                        alertErr2("龍騰卡請款記錄檔,滙入失敗,檢核檔案內[傳檔日期]與檔案名稱日期不同");
                        //Email 卡部
                        bodyText = "傳檔日期與檔案案名稱日期不同,匯入失敗,成功0筆";
//            tarokoMail1.setBodyText(bodyText);
//            sendEmailForTest(tarokoMail1);
                        return;
                    }

                    String sql1 = " select *  from cms_ppcard_visit where 1=1 and vip_kind='2' and crt_date = ? and VIP_KIND='2' ";
                    sqlSelect(sql1, new Object[]{guestUploadDate});

                    if (sqlRowNum > 0) {
                        alertErr2("龍騰卡請款檔重覆轉入失敗");
                        return;
                    }

                }

                if (dataHead.equals("3")) {
                    buttomBankNo = wpb.substring(1, 11);
                    totUploadNum = wpb.substring(11, 21).trim();
                    System.out.println("totUploadNum" + Integer.parseInt(totUploadNum));
                    System.out.println("rowCnt" + rowCnt);

                    if ((Integer.parseInt(totUploadNum) == rowCnt) == false) {
                        alertErr2("龍騰卡請款記錄檔,匯入失敗,檔案內筆數與明細筆數不同,,匯入失敗");
                        //sendmail 卡部
                        bodyText = "總筆數與實際資料筆數不同,匯入失敗,成功0筆";
//            tarokoMail1.setBodyText(bodyText);
//            sendEmailForTest(tarokoMail1);
                        return;
                    }

                    //	判斷銀行代號是否相同
                    if ((buttomBankNo.equals(bankNo)) == false) {
                        alertErr2("龍騰卡請款記錄檔,匯入失敗,檔案內首筆與尾部銀行代號不同,匯入失敗");
                        //sendmail 卡部
                        bodyText = "首筆與尾筆銀行代號不同,匯入失敗";
//            tarokoMail1.setBodyText(bodyText);
//            sendEmailForTest(tarokoMail1);
                        return;
                    }

                }
            }
            if (wpb.length() < 2) {
                file_total_cnt++;
            }
        }
        if ((rowCount == 0) && ((Integer.parseInt(totUploadNum) == rowCnt) == true) && ((buttomBankNo.equals(bankNo)) == true)) {
            fileDataImpGUEST();
        }
        //計算這個上傳檔案的筆數,除了頭尾兩筆以外,真正要Parsing 的筆數
        System.out.println("rowCnt" + rowCnt);
    }


    private void sendEmailForTest(TarokoMail tarokoMail1) throws Exception {
        tarokoMail1.sendEmail();
    }


    //龍騰卡上傳資料的Parsing 及相關的檢查項目
    void fileDataImpGUEST() throws Exception {
        wp.pageControl();

        System.out.println("\n\n\nfileTEST 程式正在進行1\n\n\n");
        int ll = 0;
        int llErr = 0;
        String id_p_seqno = "", id_no1 = "", eng_name1 = "", id_p_seqno1 = "";
        TarokoFileAccess tf = new TarokoFileAccess(wp);

        //String inputFile = wp.dataRoot + "/upload/" + ("zz_file_name");
        String inputFile = wp.itemStr("zz_file_name");
        // String inputFile = wp.itemStr("zz_file_name");
        int fi = tf.openInputText(inputFile, "UTF-8");

        if (fi == -1) {
            return;
        }

        int fileErr = tf.openOutputText(inputFile + ".err", "UTF-8");

        cmsm03.Cmsp3150Func func = new cmsm03.Cmsp3150Func();
        func.setConn(wp);

        wp.itemSet("imp_file_name", wp.colStr("zz_file_name"));

        int llOK = 0;
        int llCnt = 0;
        int data_seqno = 0;
        int freeUseCnt = 0;
        String bankNo = "";
        String guestUploadDate = "";
        String guestPpcardNo = "";
        String guestNum = "";
        String guestPymtCount = "";
        String guestTotUseCount = "";
        String guestConsumerDate = "";
        String guestPpId = "";
        String guestName = "";
        String isoCountry = "";
        String errCode = "";
        String errDesc = "";
        String buttomBankNo = "";
        String totUploadNum = "";
        String idPSeqnoa = "";
        String idNoa = "";
        String idNoCodea = "";

        while (true) {

            String wpb = tf.readTextFile(fi);
            if (tf.endFile[fi].equals("Y")) {
                break;
            }
            if (ss.length() < 2) {
                file_total_cnt++;
            }
            //取資料每筆的表頭,判斷是1或2或3
            dataHead = wpb.substring(0, 1);

            if (dataHead.equals("1")) {
                wp.colSet(llCnt, "bank_no", wpb.substring(1, 11));
                bankNo = wpb.substring(1, 11);
                wp.colSet(llCnt, "bank_no", bankNo);
                guestUploadDate = wpb.substring(11, 19).trim();

                // wp.itemSet("id_p_seqno", lsIdPSeqno);
                //抓取資料庫dataseq 的最大值,以便決定下一個dataseq 的序號
                String sql8 = "select " + " count(*) as allCount ,max(data_seqno) as li_data_seqno " + " from cms_ppcard_visit "
                        + " where crt_date =?" + " and from_type = '2' "
                        + " and bin_type = ? ";
                sqlSelect(sql8, new Object[]{guestUploadDate, ex_bin_type});
                data_seqno = Integer.parseInt(sqlStr("li_data_seqno")) + 1;

            }


            if (dataHead.equals("2")) {
                guestPpcardNo = wpb.substring(1, 17).trim();
                wp.colSet(llCnt, "pp_card_no", wpb.substring(1, 17));
                guestConsumerDate = wpb.substring(17, 25);
                wp.colSet(llCnt, "visit_date", guestConsumerDate);
                guestNum = wpb.substring(25, 27).trim();
                wp.colSet(llCnt, "ch_visits", guestNum);
                guestPymtCount = wpb.substring(27, 29).trim();
                wp.colSet(llCnt, "guests_count", guestPymtCount);
                guestTotUseCount = wpb.substring(29, 32).trim();
                wp.colSet(llCnt, "total_visits", guestTotUseCount);
                guestName = wpb.substring(32, 52).trim();
                wp.colSet(llCnt, "ch_ename", guestName);
                isoCountry = wpb.substring(52, 72).trim();
                wp.colSet(llCnt, "iso_conty", isoCountry);
                if (wpb.length() > 73) {
                    guestPpId = wpb.substring(72, 82).trim();
                    wp.colSet(llCnt, "guest_pp_id", guestPpId);
                }


                //check 龍騰卡卡卡號，是否空白(或null)
                errCode = "0";
                if (guestPpcardNo.isEmpty() == true) {
                    errCode = "01";
                    errDesc = "龍騰卡卡號空白(或null),匯入失敗";
                }

                //check ppcard 是否存在
                String sql2 = " select *  from crd_card_pp where 1=1 and vip_kind='2' and pp_card_no = ? ";

                //   String sql2 = " select a.id_p_seqno as id_p_seqno,b.id_no as id_no1,b.eng_name as eng_name1,b.id_no_code as id_no_code from crd_card_pp a join crd_idno b on b.id_p_seqno=a.id_p_seqno where 1=1 and a.vip_kind='2' "
                //		+"and a.pp_card_no = ?  ";
                sqlSelect(sql2, new Object[]{guestPpcardNo});

                if (sqlRowNum == 0) {
                    if ((errCode.length() > 1) == false) {
                        errCode = "02";
                        errDesc = "龍騰卡卡卡號" + guestPpcardNo + "不存在";
                    }

                }


                String sql3 = " select a.id_p_seqno as id_p_seqno,b.id_no as id_no1,a.eng_name as eng_name1,b.id_no_code as id_no_code from crd_card_pp a join crd_idno b on b.id_p_seqno=a.id_p_seqno where 1=1 and a.vip_kind='2' "
                        + "and a.pp_card_no = ?  ";

                sqlSelect(sql3, new Object[]{guestPpcardNo});
                if (errCode.equals("02")) {
                    idPSeqnoa = "";
                    idNoa = "";
                    idNoCodea = "";
                } else {
                    idPSeqnoa = sqlStr("id_p_seqno");
                    idNoa = sqlStr("id_no1");
                    idNoCodea = sqlStr("id_no_code");
                }
                if (sqlStr("id_p_seqno").isEmpty()) {
                    if ((errCode.length() > 1) == false) {
                        errCode = "03";
                        errDesc = "持卡人ID_P_SEQNO不存在";
                    }
                }

                if ((guestPpId.trim()).length() > 0) {
                    if (guestPpId.equals(sqlStr("id_no1")) == false) {
                        if ((errCode.length() > 1) == false) {
                            errCode = "04";
                            errDesc = "持卡人ID不存在";
                        }

                    }
                }
                if (guestName.length() > 0) {
                    //檢核txt的[客戶姓名]是否有值,有值:需再檢核與【CRD_CARD_PP】ENG_NAME是否相同
                    if (guestName.trim().equals(sqlStr("eng_name1")) == false) {
                        if ((errCode.length() > 1) == false) {
                            errCode = "05";
                            errDesc = "持卡人英文姓名不同";
                        }
                    }
                }

                if ((Integer.parseInt(guestPymtCount) == 0) && (Integer.parseInt(guestNum) == 0)) {
                    if ((errCode.length() > 1) == false) {
                        errCode = "07";
                        errDesc = "使用次數 & 攜帶人數同時＜=0(or空白),匯入失敗";
                    }
                }

                wp.colSet(llCnt, "crt_date", guestUploadDate);
                wp.colSet(llCnt, "bin_type", wp.itemStr("ex_bin_type"));
                wp.colSet(llCnt, "data_seqno", "" + data_seqno);
                wp.colSet(llCnt, "id_p_seqno", idPSeqnoa);
                wp.colSet(llCnt, "id_no", idNoa);
                wp.colSet(llCnt, "id_no_code", idNoCodea);
                wp.colSet(llCnt, "terminal_no", "");
                wp.colSet(llCnt, "city", "");
                wp.colSet(llCnt, "imp_file_name", wp.itemStr("zz_file_name"));
                wp.colSet(llCnt, "free_use_cnt", "0");

                if (wp.itemEq("ex_bin_type", "M")) {
                    wp.colSet(llCnt, "deal_type", "MC Platinum");
                    wp.colSet(llCnt, "associate_code", "700013");
                    wp.colSet(llCnt, "ica_no", "3768");
                }
                wp.colSet(llCnt, "from_type", "2");
                wp.colSet(llCnt, "bank_name", "Taiwan Cooperative Bank");

                wp.colSet(llCnt, " item_no", "10");
                if (errCode.length() > 1) {
                    //將errCode 及errDesc 寫入colSet 以便insert 進入資料庫
                    wp.colSet(llCnt, "err_Code", errCode);
                    wp.colSet(llCnt, "err_desc", errDesc);
                    impFail = impFail + 1;
                } else {
                    wp.colSet(llCnt, "err_code", "00");
                    wp.colSet(llCnt, "err_desc", "成功");
                    impSuccess = impSuccess + 1;
                }


                //擷取新龍騰卡卡卡友自費金額(ch_cost_amt),非卡友自費金額 ,單價(同行)( guest_cost_amt):同行旅客金額(TOGET_AMT)等相關資料
                //以貴賓卡卡號讀取【crd_card_pp】join【MKT_PPCARD_ISSUE 新貴通貴賓室發行參數檔】
                if (ex_vip_kind.equals("2")) {

                    String sql6 = " select * from crd_card_pp a, mkt_ppcard_issue b"
                            + "    where 1=1 and  b.bin_type=a.bin_type and b.group_code=a.group_code and b.vip_kind=a.vip_kind and b.vip_kind='2' and a.pp_card_no = ? ";
                    sqlSelect(sql6, new Object[]{guestPpcardNo});
                    System.out.println("sqlRowNum" + sqlRowNum);
                    if (sqlRowNum > 0) {
                        String sql7 = " select b.holder_amt as holderAmt,b.toget_amt as togetAmt from crd_card_pp a,mkt_ppcard_issue b  where 1=1 and b.vip_kind='2' "
                                + " and a.bin_type=b.bin_type and a.group_code=b.group_code and a.vip_kind=b.vip_kind and a.pp_card_no = ? ";
                        sqlSelect(sql7, new Object[]{guestPpcardNo});
                        wp.colSet(llCnt, "ch_cost_amt", sqlStr("holderAmt"));
                        chCostAmt = Integer.parseInt(sqlStr("holderAmt"));
                        wp.colSet(llCnt, "guest_cost_amt", sqlStr("togetAmt"));
                        guestCostAmt = Integer.parseInt(sqlStr("togetAmt"));
                    } else {
                        chCostAmt = 0;
                        guestCostAmt = 0;
                        wp.colSet(llCnt, "ch_cost_amt", 0);
                        wp.colSet(llCnt, "guest_cost_amt", 0);
                    }
                }
                //取free_cnt

                freeUseCnt = 0;
                System.out.println("guestNUm\n" + Integer.parseInt(guestNum));

                feePerHolder = ((Integer.parseInt(guestNum) - freeUseCnt)) * chCostAmt;
                //feePerGuest= (Integer.parseInt(guestPymtCount)) * guestCostAmt;
                feePerGuest = 0;
                totalFee = feePerHolder + feePerGuest;
                totChargGuest = (Integer.parseInt(guestNum)) - freeUseCnt;

                //相乘數值如有其中一個值是0，程式需判斷不進行運算，避免出現exception，該欄填入0
                if ((((feePerHolder != 0) && (chCostAmt != 0))) || (((Integer.parseInt(guestPymtCount)) != 0) && (guestCostAmt != 0))) {//
                    //  charge_guest_value=(( (Integer.parseInt(vipUseCount[detai])) * guestCostAmt) + ((vipPersonCount[detai]) * guestCostAmt ));
                    //可收取的客人價值(charg_guest_value)= (使用次數-免費使用次數)*(卡友單次自費金額)+攜帶人數*同行旅客金額
                    chargGuestValue = (((feePerHolder) + (Integer.parseInt(guestPymtCount)) * guestCostAmt));
                    // System.out.println("charge_guest_value"+chargGuestValue);
                } else {
                    chargGuestValue = 0;
                }

                wp.colSet(llCnt, "fee_per_holder", feePerHolder);
                wp.colSet(llCnt, "fee_per_guest", feePerGuest);

                wp.colSet(llCnt, "total_fee", totalFee);
                wp.colSet(llCnt, "tot_charg_guest", totChargGuest);
                if (chargGuestValue > 0) {
                    wp.colSet(llCnt, "charg_guest_value", chargGuestValue);
                } else {
                    wp.colSet(llCnt, "charg_guest_value", 0);
                }


                llCnt++;
                data_seqno++;
                ii++;

            }
        }
        //insert 進資料庫
        procFunc();
        return;
    }


    void queryAfter() {

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
        // TODO Auto-generated method stub

    }


    @Override

    public void procFunc() throws Exception {

        int llOK = 0, llErr = 0;
        cmsm03.Cmsp3150Func func = new cmsm03.Cmsp3150Func();

        func.setConn(wp);
        for (int ii = 0; ii < rowCnt; ii++) {
            if (func.insertVist(ii) == 1) {
                llOK++;
            } else {
                //wp.colSet(ii, "db_errmsg", func.getMsg());
                wp.colSet(ii, "err_desc", func.getMsg());
                llErr++;
                listErrData(ii);

            }
            if (llOK > 0) {
                sqlCommit(1);
            } else {
                sqlCommit(-1);
            }
        }


        wp.logSql = true;

        System.out.println("資料匯入完成 , 成功:" + llOK + " 失敗:" + llErr);
        alertMsg("資料寫入資料庫 , 成功:" + llOK + " 失敗:" + llErr);
        alertMsg("資料匯入完成 , 成功:" + impSuccess + " 失敗:" + impFail);
        return;

    }

    void listErrData(int ll) {
        rr++;

        wp.colSet(rr, "er_crt_date", wp.colStr(ll, "crt_date"));
        wp.colSet(rr, "er_bin_type", wp.colStr(ll, "bin_type"));
        wp.colSet(rr, "er_data_seqno", wp.colStr(ll, "data_seqno"));
        wp.colSet(rr, "er_pp_card_no", wp.colStr(ll, "pp_card_no"));
        wp.colSet(rr, "er_from_type", wp.colStr(ll, "from_type"));
        wp.colSet(rr, "er_terminal_no", wp.colStr(ll, "terminal_no"));
        wp.colSet(rr, "er_city", wp.colStr(ll, "city"));
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
        wp.colSet(rr, "er_total_fee", wp.colStr(ll, "vvvvvvvvvvvvvvvvb"));
        wp.colSet(rr, "er_total_free_guests", wp.colStr(ll, "total_free_guests"));
        wp.colSet(rr, "er_free_guests_value", wp.colStr(ll, "free_guests_value"));
        wp.colSet(rr, "er_tot_charg_guest", wp.colStr(ll, "tot_charg_guest"));
        wp.colSet(rr, "er_charg_guest_value", wp.colStr(ll, "charg_guest_value"));
        wp.colSet(rr, "er_billing_region", wp.colStr(ll, "billing_region"));
        wp.colSet(rr, "er_errmsg", wp.colStr(ll, "db_errmsg"));
    }


    @Override
    public void initButton() {
        // TODO Auto-generated method stub

    }


    @Override
    public void initPage() {
        // TODO Auto-generated method stub

    }

}





