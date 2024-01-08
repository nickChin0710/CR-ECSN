/*****************************************************************************
*                                                                            *
*                              MODIFICATION LOG                              *
*                                                                            *
*    DATE    Version    AUTHOR              DESCRIPTION                      *
*  --------  -------------------  ------------------------------------------ *
* 106/08/08  V1.01.01  Lai        Initial                                    *
* 109/12/19  V1.00.02  shiyuqi    updated for project coding standard        *
* 112/12/03  V1.00.03  Wilson     調整為判斷card_item                           *
* 112/12/14  V1.00.04  Wilson     insert crd_whtrans增加覆核欄位                                               *
*****************************************************************************/
package Crd;

import com.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class CrdD012 extends AccessDAO {
    private String progname = "製卡 sucess 系統自動扣除空白卡樣  112/12/14  V1.00.04 ";
    private Map<String, Object> resultMap;

    CommFunction   comm  = new CommFunction();
    CommCrd        comc  = new CommCrd();
    CommCrdRoutine comcr = null;

    int debug = 0;

    String checkHome = "";
    String hCallErrorDesc = "";
    String hCallBatchSeqno = "";
    String hCallRProgramCode = "";
    String hTempUser = "";
    String hBusiBusinessDate = "";
    String hBusiChiDate = "";
    int totalCnt = 0;
    String tmpChar1 = "";
    String tmpChar = "";
    double tmpDoub = 0;
    long tmpLong = 0;
    int tmpInt = 0;

    String mbosCardType = "";
    String mbosGroupCode = "";
    String mbosUnitCode = "";
    String mbosEmbossSource = "";
    String mbosEmbossReason = "";
    String mbosCardNo = "";
    String mbosElectronicCode = "";
    String mbosIcFlag = "";
    String unitCardItem  = "";
    String mbosRowid = "";
    String hWhtrWarehouseNo = "";
    String hWhtrCardItem = "";
    String hWhtrPlace = "";

    String hRealUnitcode = "";
    double hCardQty = 0;
    String hKeyUnitcode = "";
    String hKeyCardtype = "";
    String hKeyGroupcode = "";
    String hKeyIcFlag = "";
    String hkeyCardItem = "";
    String hBatchno1 = "";
    String hBatchno2 = "";
    String hWhtrYear = "";
    double hPreTotalBal = 0;
    double hAllTotalBal = 0;
    int hWhtrMonth = 0;
    int seqNo = 0;

// ************************************************************************
public static void main(String[] args) throws Exception 
{
        CrdD012 proc = new CrdD012();
        int retCode = proc.mainProcess(args);
        proc.programEnd(retCode);
}
// ************************************************************************
public int mainProcess(String[] args) 
{
        try {

            dateTime();
            setConsoleMode("Y");
            javaProgram = this.getClass().getName();
            checkHome = comc.getECSHOME();
            showLogMessage("I", "", javaProgram + " " + progname);

            if (args.length > 2) {
                String err1 = "CrdD012  [seq_no]\n";
                String err2 = "CrdD012  [seq_no]";
                System.out.println(err1);
                comc.errExit(err1, err2);
            }

            if (!connectDataBase()) {
                comc.errExit("connect DataBase error", "");
            }

            comcr = new CommCrdRoutine(getDBconnect(), getDBalias());

            comcr.hCallBatchSeqno = args.length > 0 ? args[args.length - 1] : "";

            String checkHome = comc.getECSHOME();

            if (comcr.hCallBatchSeqno.length() > 6) {
                if (comcr.hCallBatchSeqno.substring(0, 6).equals(checkHome.substring(0, 6))) {
                    comcr.hCallBatchSeqno = "no-call";
                }
            }
            comcr.hCallRProgramCode = this.getClass().getName();
            hTempUser = "";
            if (comcr.hCallBatchSeqno.length() == 20) {
                comcr.hCallParameterData = javaProgram;
                for (int i = 0; i < args.length; i++) {
                    comcr.hCallParameterData = comcr.hCallParameterData + " " + args[i];
                }
                comcr.callbatch(0, 0, 1);
                selectSQL = " user_id ";
                daoTable = "ptr_callbatch";
                whereStr = "WHERE batch_seqno   = ?  ";

                setString(1, comcr.hCallBatchSeqno);
                int recCnt = selectTable();
                hTempUser = getValue("user_id");
            }
            if (hTempUser.length() == 0) {
                hTempUser = comc.commGetUserID();
            }

            // showLogMessage("I","", "批號=" + h_batchno + " 製卡來源=" +
            // h_emboss_source);

            dateTime();
            selectPtrBusinday();

            totalCnt = 0;

            selectCrdEmboss();

            comcr.hCallErrorDesc = "程式執行結束,筆數=[" + totalCnt + "]";
            showLogMessage("I", "", comcr.hCallErrorDesc);

            if (comcr.hCallBatchSeqno.length() == 20)
                comcr.callbatch(1, 0, 1); // 1: 結束

            finalProcess();
            return 0;
        }

        catch (Exception ex) {
            expMethod = "mainProcess";
            expHandle(ex);
            return exceptExit;
        }

} // End of mainProcess
// ************************************************************************
    public void selectPtrBusinday() throws Exception {
        selectSQL = "business_date   , " + "to_char(sysdate,'yyyymmdd')    as SYSTEM_DATE ";
        daoTable = "PTR_BUSINDAY";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        int recordCnt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_ptr_businday error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hBusiBusinessDate = getValue("BUSINESS_DATE");
        long hLongChiDate = Long.parseLong(hBusiBusinessDate) - 19110000;
        hBusiChiDate = Long.toString(hLongChiDate);

        showLogMessage("I", "", "本日營業日 : [" + hBusiBusinessDate + "] [" + hBusiChiDate + "]");

        selectSQL = "to_char(to_number(to_char(sysdate,'yyyy')) - 1911)||'00001' as batchno1 , "
                + "to_char(to_number(to_char(sysdate,'yyyy')) - 1911)||'99999' as batchno2   ";
        daoTable = "DUAL ";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_dual 1       error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hBatchno1 = getValue("batchno1");
        hBatchno2 = getValue("batchno2");

        selectSQL = "to_char(sysdate,'yyyy')  as year1 , " + "to_number(to_char(sysdate,'mm')) as month1 ";
        daoTable = "DUAL ";
        whereStr = "FETCH FIRST 1 ROW ONLY";

        tmpInt = selectTable();

        if (notFound.equals("Y")) {
            String err1 = "select_dual 1       error!";
            String err2 = "";
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        hWhtrYear = getValue("year1");
        hWhtrMonth = getValueInt("month1");

        showLogMessage("I", "", "本日營業日份 : [" + hWhtrYear + "] [" + hWhtrMonth + "]");
    }
// ************************************************************************
public void selectCrdEmboss() throws Exception 
{
  selectSQL = "   a.card_type             " 
            + " , a.group_code            "
            + " , decode(a.unit_code   ,'','0000',a.unit_code)   as unit_code " 
            + " , a.emboss_source         "
            + " , a.emboss_reason         " 
            + " , a.card_no               "
            + " , decode(a.electronic_code,'','01',a.electronic_code)  as electronic_code "
            + " , decode(a.ic_flag        ,'','N' ,a.ic_flag  )        as ic_flag  "
            + " , b.card_item "
            + " , a.rowid      as rowid ";
  daoTable = "crd_emboss a, crd_item_unit b ";
  whereStr = "where a.unit_code = b.unit_code " 
		   + "  and a.in_main_date  <> '' "
           + "  and a.in_main_error  = '0' " 
           + "  and a.online_mark    = '0' "
           + "  and a.warehouse_date = ''  "
           + "order by b.card_item ";

  int recordCnt = selectTable();
  for(int i = 0; i < recordCnt ; i++) {
            initRtn();

            mbosCardType = getValue("card_type", i);
            mbosGroupCode = getValue("group_code", i);
            mbosUnitCode = getValue("unit_code", i);
            mbosEmbossSource = getValue("emboss_source", i);
            mbosEmbossReason = getValue("emboss_reason", i);
            mbosCardNo = getValue("card_no", i);
            mbosElectronicCode = getValue("electronic_code", i);
            mbosIcFlag = getValue("ic_flag", i);
            unitCardItem = getValue("card_item", i);
            mbosRowid = getValue("rowid", i);

            totalCnt++;
            processDisplay(5000); // every nnnnn display message
            if (debug == 1) {
                showLogMessage("I", "", "  888 Card=[" + mbosEmbossSource + "]");
                showLogMessage("I", "", "  888   id=[" + mbosCardNo + "]");
            }

            /*** get ptr_card_card.unit_code 2002/08/16 ***/
            hRealUnitcode = mbosUnitCode;
//          if (mbos_unit_code.equals("0000") && mbos_emboss_source.compareTo("2") > 0) 
// no use      {tmp_int = get_ptr_group_card(); }

            /*
             * showLogMessage("I",""," unit = " + h_key_unitcode + ","
             * +h_real_unitcode ); showLogMessage("I",""," type = " +
             * h_key_cardtype + "," +mbos_card_type );
             * showLogMessage("I",""," ic   = " + h_key_ic_flag + ","
             * +mbos_ic_flag );
             */

            /*--break----------------------------------------------------*/
            if(!hkeyCardItem.equals(unitCardItem))
              {
               if(hCardQty > 0)
                 {
                  tmpInt = crdWhtransInsert(1);
                  if (tmpInt == 0)
                      tmpInt = crdWarehouseUpdate(1);
/*  lai test
     tmp_int = 0;
*/
                  if (tmpInt == 0) { commitDataBase(); }
                  else {
                      rollbackDataBase();
                      showLogMessage("I", "", "***  ROLLBACK 1****");
                  }
                 }
               hKeyUnitcode = hRealUnitcode;
               hKeyCardtype = mbosCardType;
               hKeyGroupcode = mbosGroupCode;
               hKeyIcFlag = mbosIcFlag;
               hkeyCardItem = unitCardItem;
               hCardQty = 0;
              }
     hCardQty++;
     updateCrdEmboss();
    }

    /*-- last record --------------------------------------------------*/
    if(hCardQty > 0)
      {
       tmpInt = crdWhtransInsert(2);
       if(tmpInt == 0)   tmpInt = crdWarehouseUpdate(2);

       if (tmpInt == 0) { commitDataBase(); }
       else {
             rollbackDataBase();
             showLogMessage("I", "", "***  ROLLBACK 2****");
            }
      }
}
// ************************************************************************
/* Insert 卡片庫存異動檔 */
// ************************************************************************
public int crdWhtransInsert(int idx) throws Exception 
{

if(debug == 1) showLogMessage("I", "", " insert whtrans pre=[" + hPreTotalBal + "]" +idx);
        /*-- get batch-no -------------------------------------------------*/
        selectSQL = "to_char(to_number(max(warehouse_no)) + 1) as max_no";
        daoTable = "crd_whtrans   ";
        whereStr = "where warehouse_no between ? and ? ";

        setString(1, hBatchno1);
        setString(2, hBatchno2);

        tmpInt = selectTable();

        hWhtrWarehouseNo = getValue("max_no");
        if (hWhtrWarehouseNo.length() < 2) {
            hWhtrWarehouseNo = hBatchno1;
        }

        // showLogMessage("I",""," get crd_whtrans max =[" + h_whtr_warehouse_no
        // + "]");

        /*-- get card_item  -------------------------------------------------*/
        selectSQL = "a.card_item  , new_vendor , " 
                  + " mku_vendor  , chg_vendor , default_place ";
        daoTable  = "crd_card_item b, crd_item_unit a ";
        whereStr  = "where a.card_item = b.card_item " 
                  + "  and a.card_item = ?  "; 

        setString(1, hkeyCardItem);

        tmpInt = selectTable();

        /*
         * lai test if ( notFound.equals("Y") ) {
         * showLogMessage("I",""," get crd_item_unit error =[" + h_batchno1 +"]"
         * + h_batchno2); return(1); }
         */

        hWhtrCardItem = getValue("card_item");
        switch (mbosEmbossSource.trim()) {
        case "1":
            hWhtrPlace = getValue("new_vendor");
            break; // * 新製卡
        case "3":
        case "4":
            hWhtrPlace = getValue("chg_vendor");
            break; // * 續卡(換卡)
        default:
            hWhtrPlace = getValue("mku_vendor");
            break; // * 重製卡(補發卡)
        }
        hWhtrPlace = getValue("default_place");
        if (debug == 1)
            showLogMessage("I", "", " insert whtr=[" + hWhtrCardItem + "]" + hCardQty);

        crdWarehouseSelect(0);

        setValue("warehouse_no"    , hWhtrWarehouseNo);
        setValue("card_item"       , hWhtrCardItem);
        setValue("warehouse_date"  , sysDate);
        setValue("card_type"       , hKeyCardtype);
//      setValue("group_code"      , h_key_groupcode);
        setValue("unit_ocde"       , hKeyUnitcode);
        setValue("tns_type"        , "2");
        setValue("place"           , hWhtrPlace);
        setValueDouble("prev_total", hAllTotalBal);
        setValueDouble("use_total" , hCardQty);
        setValue("trans_reason"    , "1");
        setValue("apr_flag"        , "Y");
        setValue("apr_user"        , javaProgram);
        setValue("apr_date"        , sysDate);
        setValue("crt_date"        , sysDate);
        setValue("crt_user"        , "SYSTEM");
        setValue("mod_time"        , sysDate + sysTime);
        setValue("mod_pgm"         , javaProgram);

        daoTable = "crd_whtrans ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_whtrans   error[dupRecord]=";
            String err2 = hKeyUnitcode;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
}
/*************************************************************************/
/* Insert 卡片庫存異動檔 */
// ************************************************************************
public int crdWarehouseInsert() throws Exception 
{
if(debug == 1) showLogMessage("I", "", " insert ware=[" + hWhtrCardItem + "]");

        setValue("wh_year"  , hWhtrYear);
        setValue("card_item", hWhtrCardItem);
        setValue("place"    , hWhtrPlace);
        setValue("lot_no"   , sysDate+"01");
        setValue("mod_time" , sysDate + sysTime);
        setValue("mod_pgm"  , javaProgram);

        daoTable = "crd_warehouse ";

        insertTable();

        if (dupRecord.equals("Y")) {
            String err1 = "insert_crd_warehouse error[dupRecord]=";
            String err2 = hWhtrCardItem;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

        return (0);
}
/*************************************************************************/
public int crdWarehouseSelect(int idx) throws Exception 
{
  selectSQL = "  sum(pre_total   "
            + "  + ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
            + "      in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
            + "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
            + "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12)) all_total_bal ";
  daoTable = "crd_warehouse ";
  whereStr = "where wh_year    =  ? " 
           + "  and card_item  =  ? " 
           + "  and place      =  ? ";

  setString(1, hWhtrYear);
  setString(2, hWhtrCardItem);
  setString(3, hWhtrPlace);

  tmpInt = selectTable();

  if (debug == 1)
      showLogMessage("I", "", " select whtr=[" + tmpInt + "]");
  hAllTotalBal = getValueDouble("all_total_bal");

  return (0);
}
/*************************************************************************/
/* Update 卡片庫存檔 */
// ************************************************************************
public int crdWarehouseUpdate(int idx) throws Exception 
{
  double remainCardQty = hCardQty;
  if (debug == 1)
      showLogMessage("I", "", " update whtr=[" + idx        + "]" + hBatchno1);
  double hWareOutQty01 = 0;
  double hWareOutQty02 = 0;
  double hWareOutQty03 = 0;
  double hWareOutQty04 = 0;
  double hWareOutQty05 = 0;
  double hWareOutQty06 = 0;
  double hWareOutQty07 = 0;
  double hWareOutQty08 = 0;
  double hWareOutQty09 = 0;
  double hWareOutQty10 = 0;
  double hWareOutQty11 = 0;
  double hWareOutQty12 = 0;
  String hWareRowid     = "";
  String hWareLotNo    = "";
  double hWareItemAmt  = 0;
  double tmpQty = 0;
    

  seqNo = 0;
      
/* brain */
  selectSQL = "out_qty01  , out_qty02   , out_qty03   , out_qty04   ,"
            + "out_qty05  , out_qty06   , out_qty07   , out_qty08   ,"
            + "out_qty09  , out_qty10   , out_qty11   , out_qty12   ," 
            + "  pre_total   "
            + "  + ( in_qty01+ in_qty02+ in_qty03+ in_qty04+ in_qty05+ in_qty06 + "
            + "      in_qty07+ in_qty08+ in_qty09+ in_qty10+ in_qty11+ in_qty12)  "
            + "  - (out_qty01+out_qty02+out_qty03+out_qty04+out_qty05+out_qty06 + "
            + "     out_qty07+out_qty08+out_qty09+out_qty10+out_qty11+out_qty12) h_pre_total_bal,"
            + "item_amt   , lot_no      , rowid   as rowid    ";
  daoTable = "crd_warehouse ";
  whereStr = "where wh_year    =  ? " 
           + "  and card_item  =  ? " 
           + "  and place      =  ? "
           + "order by lot_no ";

  setString(1, hWhtrYear);
  setString(2, hWhtrCardItem);
  setString(3, hWhtrPlace);

  tmpInt = selectTable();

  if (debug == 1)
      showLogMessage("I", "", " select whtr=[" + tmpInt + "]");

  if (notFound.equals("Y")) {
      tmpInt = crdWarehouseInsert();
      return (1);
  }
if(debug ==1)  showLogMessage("I", "", " select ware=" + hWhtrCardItem + "," + hWhtrPlace);
  for(int i = 0; i < tmpInt; i++) {
      hWareLotNo    = getValue("lot_no",i); 
      hWareOutQty01 = getValueDouble("out_qty01",i);
      hWareOutQty02 = getValueDouble("out_qty02",i);
      hWareOutQty03 = getValueDouble("out_qty03",i);
      hWareOutQty04 = getValueDouble("out_qty04",i);
      hWareOutQty05 = getValueDouble("out_qty05",i);
      hWareOutQty06 = getValueDouble("out_qty06",i);
      hWareOutQty07 = getValueDouble("out_qty07",i);
      hWareOutQty08 = getValueDouble("out_qty08",i);
      hWareOutQty09 = getValueDouble("out_qty09",i);
      hWareOutQty10 = getValueDouble("out_qty10",i);
      hWareOutQty11 = getValueDouble("out_qty11",i);
      hWareOutQty12 = getValueDouble("out_qty12",i);
      hWareItemAmt  = getValueDouble("item_amt",i);
      hPreTotalBal = getValueDouble("h_pre_total_bal",i);
      hWareRowid     = getValue("rowid",i);
if(debug == 1) showLogMessage("I", "", "    whtr lot=" + hWareLotNo + "," +i+","+ hPreTotalBal + " remain=" + remainCardQty);

      tmpQty = hPreTotalBal;  /* 該批號剩餘庫存 */
      if(i+1 == tmpInt) /* 最後一筆批號全加 */
        {
         tmpQty = remainCardQty;
        }
      else
        {
         if(hPreTotalBal < 1)      continue;    // 出庫
         if(remainCardQty < tmpQty) {
             tmpQty = remainCardQty;
             remainCardQty = 0;
         } else
             remainCardQty = remainCardQty - tmpQty;
        }
      
      
      switch (hWhtrMonth) {
      case 1:
          hWareOutQty01 += tmpQty;
          break;
      case 2:
          hWareOutQty02 += tmpQty;
          break;
      case 3:
          hWareOutQty03 += tmpQty;
          break;
      case 4:
          hWareOutQty04 += tmpQty;
          break;
      case 5:
          hWareOutQty05 += tmpQty;
          break;
      case 6:
          hWareOutQty06 += tmpQty;
          break;
      case 7:
          hWareOutQty07 += tmpQty;
          break;
      case 8:
          hWareOutQty08 += tmpQty;
          break;
      case 9:
          hWareOutQty09 += tmpQty;
          break;
      case 10:
          hWareOutQty10 += tmpQty;
          break;
      case 11:
          hWareOutQty11 += tmpQty;
          break;
      case 12:
          hWareOutQty12 += tmpQty;
          break;
      default:
          return (1);
      }

      updateSQL = "out_qty01 =  ? , " + "out_qty02 =  ? , " + "out_qty03 =  ? , "
                + "out_qty04 =  ? , " + "out_qty05 =  ? , " + "out_qty06 =  ? , "
                + "out_qty07 =  ? , " + "out_qty08 =  ? , " + "out_qty09 =  ? , "
                + "out_qty10 =  ? , " + "out_qty11 =  ? , " + "out_qty12 =  ? , "
                + "mod_pgm   =  ? , " + "mod_time  = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
      daoTable = "crd_warehouse ";
      whereStr = "where rowid   = ? ";

      setDouble(1, hWareOutQty01);
      setDouble(2, hWareOutQty02);
      setDouble(3, hWareOutQty03);
      setDouble(4, hWareOutQty04);
      setDouble(5, hWareOutQty05);
      setDouble(6, hWareOutQty06);
      setDouble(7, hWareOutQty07);
      setDouble(8, hWareOutQty08);
      setDouble(9, hWareOutQty09);
      setDouble(10, hWareOutQty10);
      setDouble(11, hWareOutQty11);
      setDouble(12, hWareOutQty12);
      setString(13, javaProgram);
      setString(14, sysDate + sysTime);
      setRowId( 15, hWareRowid);

      updateTable();

      if(notFound.equals("Y")) {
          String err1 = "update_crd_warehouse error[notFind]" + hWhtrCardItem;
          String err2 = hWhtrCardItem;
          comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
      }
      
      insertCrdWhtxDtl(hWareLotNo , tmpQty, hWareItemAmt); 
      
      if(remainCardQty == 0) break;

  }

  return (0);
}

/****************************************************************/
public void insertCrdWhtxDtl(String iLotNo,double tmpQty,double iItemAmt) throws Exception
{
    
    seqNo++;
    setValueInt("seq_no"       , seqNo);
    setValue("warehouse_no"    , hWhtrWarehouseNo);
    setValue("card_item"       , hWhtrCardItem);
    setValue("lot_no"          , iLotNo );
    setValueDouble("item_amt"  , iItemAmt);
    setValue("warehouse_date"  , sysDate);
//    setValue("card_type"       , h_key_cardtype);
//    setValue("group_code"      , h_key_groupcode);
//    setValue("unit_ocde"       , h_key_unitcode);
    setValue("tns_type"        , "2");
    setValue("place"           , hWhtrPlace);
    setValueDouble("prev_total", hPreTotalBal);
    setValueDouble("use_total" , tmpQty);
//    setValue("trans_reason"    , "1");
//    setValue("crt_date"        , sysDate);
//    setValue("crt_user"        , "SYSTEM");
//    setValue("mod_time"        , sysDate + sysTime);
//    setValue("mod_pgm"         , javaProgram);

    daoTable = "crd_whtx_dtl";

    insertTable();
    // TODO Auto-generated method stub
    
}
// ************************************************************************
public int updateCrdEmboss() throws Exception 
{
        if (debug == 1)
            showLogMessage("I", "", " upd emboss=[" + "]");

        if (!hRealUnitcode.equals(mbosUnitCode)) {
            updateSQL = "warehouse_date  =  ? , " + "unit_code           =  ? , "  
                      + "mod_pgm         =  ? , "
                      + "mod_time        = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable  = "crd_emboss";
            whereStr  = "where rowid     = ? ";

            setString(1, sysDate);
            setString(2, hRealUnitcode);
            setString(3, javaProgram);
            setString(4, sysDate + sysTime);
            setRowId(5, mbosRowid);
        } else {
            updateSQL = "warehouse_date    =  ? , " 
                      + "mod_pgm           =  ? , "
                      + "mod_time          = TIMESTAMP_FORMAT( ?,'YYYYMMDDHH24MISS') ";
            daoTable = "crd_emboss";
            whereStr = "where rowid   = ? ";

            setString(1, sysDate);
            setString(2, javaProgram);
            setString(3, sysDate + sysTime);
            setRowId(4, mbosRowid);
        }

        updateTable();

        if (notFound.equals("Y")) {
            String err1 = "update_crd_emboss    error[notFind]" + mbosCardNo;
            String err2 = mbosCardNo;
            comcr.errRtn(err1, err2, comcr.hCallBatchSeqno);
        }

  return 0;
}
// ************************************************************************
public void initRtn() throws Exception 
{

        mbosCardType = "";
        mbosGroupCode = "";
        mbosUnitCode = "";
        mbosEmbossSource = "";
        mbosEmbossReason = "";
        mbosCardNo = "";
        mbosElectronicCode = "";
        mbosIcFlag = "";
        unitCardItem = "";
        mbosRowid = "";
        hWhtrWarehouseNo = "";
        hWhtrCardItem = "";
        hWhtrPlace = "";

        hRealUnitcode = "";
}
// ************************************************************************
} 
