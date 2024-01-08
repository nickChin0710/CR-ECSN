connect to cr;

DROP TABLE "ECSCRDB "."CCA_IMS_LOG";

--1013 

DROP TABLE   "ECSCRDB "."CCA_AUTH_BITDATA";
CREATE TABLE "ECSCRDB "."CCA_AUTH_BITDATA"  (
		  "TX_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "TX_TIME" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "TX_DATETIME" TIMESTAMP , 
		  "AUTH_SEQNO" VARCHAR(12 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "CARD_NO" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "MESSAGE_HEAD1" VARCHAR(3 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "MESSAGE_HEAD2" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "MESSAGE_HEAD3" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "MESSAGE_HEAD4" VARCHAR(3 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "MESSAGE_HEAD5" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "MESSAGE_HEAD6" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "TRANS_TYPE" VARCHAR(4 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BITMAP" VARCHAR(128 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT2_ACCT_NO" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT3_PROC_CODE" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT4_TRAN_AMT" VARCHAR(12 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT7_DATE_TIME" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT11_AUDIT_NO" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT12_LOCAL_TIME" VARCHAR(12 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT13_LOCAL_DATE" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT15_SETL_DATE" VARCHAR(4 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT17_CAP_DATE" VARCHAR(4 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT18_MCHT_CAT_CODE" VARCHAR(4 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT22_POS_ENTRYMODE" VARCHAR(3 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT25_POS_CONDMODE" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT26_PIN_LEN" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT28_TRANS_FEE" VARCHAR(9 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT31_TID" VARCHAR(30 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT32_CODE" VARCHAR(13 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT35_TRACK_II" VARCHAR(37 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT37_REF_NO" VARCHAR(12 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT38_APPR_CODE" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT39_ADJ_CODE" VARCHAR(3 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT41_TERM_ID" VARCHAR(16 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT43_MCHT_LOC" VARGRAPHIC(40 CODEUNITS16) NOT NULL WITH DEFAULT '' , 
		  "BIT42_CARD_ACCEPTOR_CODE" VARCHAR(15 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT43_CARD_ACCEPTOR_NAME" VARCHAR(60 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT44_ADD_RESP_CODE" VARCHAR(27 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT45_TRACK_I" VARCHAR(100 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT48_ADD_DATA" VARCHAR(79 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT49_TRANS_CUR_CODE" VARCHAR(3 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT50_CURR_SETL" VARCHAR(3 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT52_PIN_DATA" VARCHAR(32 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT53_SECURITY_INFO" VARCHAR(16 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT60_POS_INFO" VARCHAR(61 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT61_OTHER_DATA" VARCHAR(22 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT62_POSTAL_CODE" VARCHAR(13 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT63_POS_ADD_DATA" VARCHAR(3 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT66_SETL_CODE" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT70_NETWORK" VARCHAR(3 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT73_ACT_DATE" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT74_NO_CREDIT" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT75_REV_NO_CREDIT" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT76_NO_DEBITS" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT77_REV_NO_DEBITS" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT78_NO_TRANSFER" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT79_REV_NO_TRANSFER" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT80_NO_INQURIES" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT81_NO_AUTH" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT86_AMT_CREDITS" VARCHAR(16 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT87_REV_AMT_CREDITS" VARCHAR(16 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT88_AMT_DEBITS" VARCHAR(16 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT89_REV_AMT_DEBITS" VARCHAR(16 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT90_ORG_DATA" VARCHAR(42 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT91_FILE_CODE" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT95_REPL_AMT" VARCHAR(42 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT97_NET_AMT" VARCHAR(17 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT99_SETL_INST" VARCHAR(13 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT101_FILE_NAME" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT120_MESS_DATA" VARCHAR(153 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT121_ISSUER" VARCHAR(60 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT122_OPEN_DATA" VARCHAR(101 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT123_ADDR_DATA" VARCHAR(153 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT125_SUPP_DATA" VARCHAR(12 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT126_CAF_DATA" VARCHAR(44 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT126_ATM_ADD_DATA" VARCHAR(800 OCTETS) NOT NULL WITH DEFAULT '' , 
		  "BIT127_REC_DATA" VARCHAR(200 OCTETS) NOT NULL WITH DEFAULT '' )   
		 IN "TB_CCA_TBL" INDEX IN "TB_CCA_IDX"  
		 ORGANIZE BY ROW; 

COMMENT ON TABLE "ECSCRDB "."CCA_AUTH_BITDATA" IS '授權交易記錄檔';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."AUTH_SEQNO" IS '授權流水號                        ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT101_FILE_NAME" IS '檔案名稱                           ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT11_AUDIT_NO" IS 'System Trace Audit Number         ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT120_MESS_DATA" IS '最大值153位                        ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT121_ISSUER" IS 'iso-121               (iso無此欄位)';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT122_OPEN_DATA" IS 'From Host Maintenance I nformation ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT123_ADDR_DATA" IS '線上鍵值交換                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT125_SUPP_DATA" IS '清算資料              (iso無此欄位)';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT126_ATM_ADD_DATA" IS 'ATM Additional Data                ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT126_CAF_DATA" IS 'CAF 檔案資料                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT127_REC_DATA" IS 'EDC 交易資料                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT12_LOCAL_TIME" IS '交易當地時間                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT13_LOCAL_DATE" IS '交易當地月日                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT15_SETL_DATE" IS '清算月日                          ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT17_CAP_DATE" IS '交易月日(Capture)                 ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT18_MCHT_CAT_CODE" IS '特約商店行業類別碼                ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT22_POS_ENTRYMODE" IS 'Point of Service Entry Mode       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT25_POS_CONDMODE" IS 'Point of Service Condition Mode   ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT26_PIN_LEN" IS 'PIN Code 長度         (iso無此欄位)';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT28_TRANS_FEE" IS '交易手續費金額                     ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT2_ACCT_NO" IS '卡號或帳號                        ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT31_TID" IS '(iso無此欄位)                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT32_CODE" IS '收單機構代碼                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT35_TRACK_II" IS 'TRACK_II 資料                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT37_REF_NO" IS '收行主機識別碼                     ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT38_APPR_CODE" IS '授權號碼                           ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT39_ADJ_CODE" IS 'ISO交易回覆碼                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT3_PROC_CODE" IS 'Processing Code                   ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT41_TERM_ID" IS 'card terminal ID                   ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT42_CARD_ACCEPTOR_CODE" IS '特店代號                           ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT43_CARD_ACCEPTOR_NAME" IS '端末設備所在地之資訊               ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT43_MCHT_LOC" IS '消費地資料                         ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT44_ADD_RESP_CODE" IS 'Additional Response Dat a          ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT45_TRACK_I" IS 'TRACK-I               (iso無此欄位)';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT48_ADD_DATA" IS 'ATM/POS/Net附加資料                ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT49_TRANS_CUR_CODE" IS 'Transaction Currency Co de         ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT4_TRAN_AMT" IS '消費金額(台幣)                    ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT50_CURR_SETL" IS '清算幣別              (iso無此欄位)';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT52_PIN_DATA" IS 'PIN 資料                           ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT53_SECURITY_INFO" IS '網路管理訊息                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT60_POS_INFO" IS 'POS終端機資料或0300                ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT61_OTHER_DATA" IS 'POS發卡行回覆資料                  ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT62_POSTAL_CODE" IS '特店郵遞區號                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT63_POS_ADD_DATA" IS 'POS Additional Data                ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT66_SETL_CODE" IS '對帳回覆訊息                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT70_NETWORK" IS '0800交易類別                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT73_ACT_DATE" IS '0300-檔案刪除日期                  ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT74_NO_CREDIT" IS 'Credit交易筆數                     ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT75_REV_NO_CREDIT" IS 'Credit沖消交易筆數                 ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT76_NO_DEBITS" IS 'Debit交易筆數                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT77_REV_NO_DEBITS" IS 'Debit沖消交易筆數                  ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT78_NO_TRANSFER" IS 'Transfer交易筆數                   ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT79_REV_NO_TRANSFER" IS 'Transfer沖消交易筆數               ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT7_DATE_TIME" IS '交易傳送日期時間                  ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT80_NO_INQURIES" IS '查詢交易筆數                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT81_NO_AUTH" IS '授權交易筆數                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT86_AMT_CREDITS" IS 'Credit金額總計                     ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT87_REV_AMT_CREDITS" IS 'Credit沖消金額總計                 ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT88_AMT_DEBITS" IS 'Debit金額總計                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT89_REV_AMT_DEBITS" IS 'Debit沖消金額總計                  ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT90_ORG_DATA" IS '原始交易資料                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT91_FILE_CODE" IS '檔案更新                           ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT95_REPL_AMT" IS '調整金額                           ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT97_NET_AMT" IS '清算淨額                           ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BIT99_SETL_INST" IS '清算銀行代碼                       ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."BITMAP" IS 'ISO8583 字串                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."CARD_NO" IS '卡號                              ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."MESSAGE_HEAD1" IS 'ISO-head-1                        ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."MESSAGE_HEAD2" IS 'ISO-head-2                        ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."MESSAGE_HEAD3" IS 'ISO-head-3                        ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."MESSAGE_HEAD4" IS 'ISO-head-4                        ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."MESSAGE_HEAD5" IS '交易發生端代碼                    ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."MESSAGE_HEAD6" IS '交易處理單位                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."TRANS_TYPE" IS 'ISO-交易類別                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."TX_DATE" IS '建檔日期                          ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."TX_DATETIME" IS '消費日期時間                      ';

COMMENT ON COLUMN "ECSCRDB "."CCA_AUTH_BITDATA"."TX_TIME" IS '建檔時間                          ';

CREATE INDEX "ECSCRDB "."CCA_AUTH_BITDATA_IDX01" ON "ECSCRDB "."CCA_AUTH_BITDATA" 
                   ("CARD_NO" ASC,
                   "TX_DATE" ASC,
                   "TX_TIME" ASC)
                   
                   COMPRESS NO 
                   INCLUDE NULL KEYS ALLOW REVERSE SCANS ;

CREATE INDEX "ECSCRDB "."CCA_AUTH_BITDATA_IDX02" ON "ECSCRDB "."CCA_AUTH_BITDATA" 
                   ("AUTH_SEQNO" ASC)
                   
                   COMPRESS NO 
                   INCLUDE NULL KEYS ALLOW REVERSE SCANS ;
				   
GRANT SELECT,INSERT,DELETE,UPDATE ON "ECSCRDB"."CCA_AUTH_BITDATA"  TO USER CRAP1, USER dcdbmod ;
GRANT SELECT  ON "ECSCRDB"."CCA_AUTH_BITDATA"  TO USER emdap41, USER emdap42 ;


--DROP INDEX ECSCRDB.WEB_SERVICE_LOG_PK;
--CREATE UNIQUE INDEX ECSCRDB.WEB_SERVICE_LOG_PK ON ECSCRDB.WEB_SERVICE_LOG (CORRELATION_ID,SYSTEM_NAME,FUNCTION_NAME);


--1026
DROP TABLE ECSCRDB.OEMPAY_CARD;

CREATE TABLE "ECSCRDB "."EPAY_CARD"  (
		  "V_CARD_NO" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT  , 
		  "CARD_NO" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT  , 
		  "STATUS_CODE" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT  , 
		  "CHANGE_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "CRT_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "NEW_END_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "WALLET_ID" VARCHAR(32 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SIR_USER" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SIR" VARCHAR(36 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SIR_EVENT" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT  , 
		  "EVENT_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "EVENT_TIME" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SIR_STATUS" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT  , 
		  "ID_P_SEQNO" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "ACNO_P_SEQNO" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SMS_FLAG" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SMS_VER_CODE" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT  , 
		  "ACTIVE_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SERVICE_STATUS" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SERVICE_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "NEW_SIR_USER" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "NEW_SIR" VARCHAR(36 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SUBJECT_CODE" VARGRAPHIC(15 CODEUNITS16) NOT NULL WITH DEFAULT '' , 
		  "REASON_CODE" VARGRAPHIC(15 CODEUNITS16) NOT NULL WITH DEFAULT '' , 
		  "EPAY_TYPE" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT  , 
		  "MOD_TIME" TIMESTAMP , 
		  "MOD_PGM" VARCHAR(20 OCTETS) NOT NULL WITH DEFAULT  )   
		 IN "TB_OTH_TBL" INDEX IN "TB_OTH_IDX"  
		 ORGANIZE BY ROW  ;

COMMENT ON TABLE "ECSCRDB "."EPAY_CARD" IS '虛擬卡主檔' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."ACNO_P_SEQNO" IS '帳戶流水號   ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."ACTIVE_DATE" IS '服務佈署日期 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."CARD_NO" IS '卡號         ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."CHANGE_DATE" IS '上次換卡日期 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."CRT_DATE" IS '鍵檔日期     ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."EPAY_TYPE" IS 'EPAY卡別 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."EVENT_DATE" IS '事件日期        MMDD for V / J / UD, MMDDYY for Master' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."EVENT_TIME" IS '事件時間     ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."ID_P_SEQNO" IS '帳戶流水號碼 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."MOD_PGM" IS '異動程式     ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."MOD_TIME" IS '異動時間     ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."NEW_END_DATE" IS '本卡有效迄日 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."NEW_SIR" IS '新SIR        ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."NEW_SIR_USER" IS '新產生SIR者  ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."REASON_CODE" IS '服務理由代碼 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SERVICE_DATE" IS '服務通知日期 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SERVICE_STATUS" IS '服務執行狀態 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SIR" IS 'SIR          ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SIR_EVENT" IS 'SIR 事件     ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SIR_STATUS" IS 'SIR 目前狀態 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SIR_USER" IS '產生SIR者    ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SMS_FLAG" IS 'SMS旗標      ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SMS_VER_CODE" IS '簡訊驗證瑪   ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."STATUS_CODE" IS '狀況代號        00:入/扣帳成功 01:存款不足 02:非委託代繳代發戶 03:中止委託代繳代發戶 04:存戶查核資料錯誤 05:無此帳號 06:帳號結清銷戶 07:存款遭強制執行無法代繳 98:其他' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."SUBJECT_CODE" IS '服務主體代碼 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."V_CARD_NO" IS '虛擬卡號     ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD"."WALLET_ID" IS 'WALLET_ID    ' ;

CREATE UNIQUE INDEX "ECSCRDB "."EPAY_CARD_UK" ON "ECSCRDB "."EPAY_CARD" 
		("V_CARD_NO" ASC)
		
		COMPRESS NO 
		INCLUDE NULL KEYS ALLOW REVERSE SCANS ;

CREATE INDEX "ECSCRDB "."EPAY_CARD_IDX_1" ON "ECSCRDB "."EPAY_CARD" 
		("CARD_NO" ASC)
		
		COMPRESS NO 
		INCLUDE NULL KEYS ALLOW REVERSE SCANS ;

CREATE INDEX "ECSCRDB "."EPAY_CARD_IDX_2" ON "ECSCRDB "."EPAY_CARD" 
		("CRT_DATE" ASC)
		
		COMPRESS NO 
		INCLUDE NULL KEYS ALLOW REVERSE SCANS ;
		
GRANT SELECT,INSERT,DELETE,UPDATE ON "ECSCRDB "."EPAY_CARD"  TO USER CRAP1, USER DCDBMOD ;
GRANT SELECT ON "ECSCRDB "."EPAY_CARD"  TO USER EMDAP41, USER EMDAP42 ;

--1028

DROP TABLE ECSCRDB.OEMPAY_APPLY_DATA;

CREATE TABLE "ECSCRDB "."EPAY_APPLY_DATA"  (
		  "CARD_NO" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT  , 
		  "WALLET_ID" VARCHAR(32 OCTETS) NOT NULL WITH DEFAULT  , 
		  "V_CARD_NO" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT  , 
		  "NEW_END_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "T_PAN_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "T_PAN_TIME" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT  , 
		  "RCV_STATUS" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SUBJECT_CODE" VARGRAPHIC(15 CODEUNITS16) NOT NULL WITH DEFAULT '' , 
		  "REASON_CODE" VARGRAPHIC(15 CODEUNITS16) NOT NULL WITH DEFAULT '' , 
		  "SIR_USER" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SIR" VARCHAR(36 OCTETS) NOT NULL WITH DEFAULT  , 
		  "ID_P_SEQNO" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "ACNO_P_SEQNO" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "OPT_FLAG" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT  , 
		  "OPT_VER_CODE" VARCHAR(4 OCTETS) NOT NULL WITH DEFAULT  , 
		  "OPT_VER_MINS" INTEGER NOT NULL WITH DEFAULT  , 
		  "OPT_VER_CNT" INTEGER NOT NULL WITH DEFAULT  , 
		  "OPT_VER_SEND_CNT" INTEGER NOT NULL WITH DEFAULT  , 
		  "OPT_VER_DATETIME" VARCHAR(14 OCTETS) NOT NULL WITH DEFAULT  , 
		  "OPT_S_DATETIME" VARCHAR(14 OCTETS) NOT NULL WITH DEFAULT  , 
		  "OPT_E_DATETIME" VARCHAR(14 OCTETS) NOT NULL WITH DEFAULT  , 
		  "SEND_OPT_FLAG" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT  , 
		  "IDNV_TYPE" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT  , 
		  "NEW_APPLY_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "RESET_PSWD_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "EMBOSS_HEAD" VARGRAPHIC(500 CODEUNITS16) NOT NULL WITH DEFAULT '' , 
		  "EMBOSS_DATA" VARGRAPHIC(500 CODEUNITS16) NOT NULL WITH DEFAULT '' , 
		  "CRT_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "CRT_TIME" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT  , 
		  "BATCH_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "BATCH_TIME" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT  , 
		  "PROC_FLAG" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT  , 
		  "MOD_TIME" TIMESTAMP , 
		  "MOD_PGM" VARCHAR(20 OCTETS) NOT NULL WITH DEFAULT  )   
		 IN "TB_OTH_TBL" INDEX IN "TB_OTH_IDX"  
		 ORGANIZE BY ROW  ;

COMMENT ON TABLE "ECSCRDB "."EPAY_APPLY_DATA" IS '虛擬卡申請紀錄檔' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."ACNO_P_SEQNO" IS '帳戶流水號             ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."BATCH_DATE" IS '批次日期               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."BATCH_TIME" IS '批次時間               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."CARD_NO" IS '卡號                   ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."CRT_DATE" IS '鍵檔日期               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."CRT_TIME" IS '鍵檔時間               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."EMBOSS_DATA" IS '凸字第四行             ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."EMBOSS_HEAD" IS '製卡檔表頭             ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."IDNV_TYPE" IS 'ID&V的事件類型         ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."ID_P_SEQNO" IS '帳戶流水號碼           ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."MOD_PGM" IS '異動程式               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."MOD_TIME" IS '異動時間               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."NEW_APPLY_DATE" IS '新申請卡片下載日期     ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."NEW_END_DATE" IS '虛擬卡號效期           ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."OPT_E_DATETIME" IS '驗證日期時間-迄        ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."OPT_FLAG" IS '發簡訊驗證旗標         ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."OPT_S_DATETIME" IS '驗證日期時間-起        ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."OPT_VER_CNT" IS '驗證瑪檢核次數         ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."OPT_VER_CODE" IS '簡訊驗證瑪             ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."OPT_VER_DATETIME" IS '驗證日期時間           ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."OPT_VER_MINS" IS '須完成驗證分鐘         ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."OPT_VER_SEND_CNT" IS '驗證瑪重送次數         ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."PROC_FLAG" IS '處理註記        1:成功 2:退件 3:分期 N:未處理' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."RCV_STATUS" IS '執行狀態               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."REASON_CODE" IS '理由代碼               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."RESET_PSWD_DATE" IS '重設卡片密碼日期       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."SEND_OPT_FLAG" IS '是否需發送OTP給使用者  ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."SIR" IS 'SIR NO                 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."SIR_USER" IS '產生SIR者              ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."SUBJECT_CODE" IS '主體代碼               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."T_PAN_DATE" IS '虛擬卡號產生日期       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."T_PAN_TIME" IS '虛擬卡號產生時間       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."V_CARD_NO" IS '虛擬卡號               ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_APPLY_DATA"."WALLET_ID" IS 'WALLET_ID              ' ;

CREATE UNIQUE INDEX "ECSCRDB "."EPAY_APPLY_DATA_UK" ON "ECSCRDB "."EPAY_APPLY_DATA" 
		("CARD_NO" ASC,
		 "WALLET_ID" ASC)
		
		COMPRESS NO 
		INCLUDE NULL KEYS ALLOW REVERSE SCANS ;

CREATE INDEX "ECSCRDB "."EPAY_APPLY_DATA_IDX_1" ON "ECSCRDB "."EPAY_APPLY_DATA" 
		("ACNO_P_SEQNO" ASC)
		
		COMPRESS NO 
		INCLUDE NULL KEYS ALLOW REVERSE SCANS ;

CREATE INDEX "ECSCRDB "."EPAY_APPLY_DATA_IDX_2" ON "ECSCRDB "."EPAY_APPLY_DATA" 
		("CRT_DATE" ASC)
		
		COMPRESS NO 
		INCLUDE NULL KEYS ALLOW REVERSE SCANS ;

GRANT SELECT,INSERT,DELETE,UPDATE ON "ECSCRDB "."EPAY_APPLY_DATA"  TO USER CRAP1, USER DCDBMOD ;
GRANT SELECT ON "ECSCRDB "."EPAY_APPLY_DATA"  TO USER EMDAP41, USER EMDAP42 ;

DROP TABLE ECSCRDB.OEMPAY_CARD_LOG;

CREATE TABLE "ECSCRDB "."EPAY_CARD_LOG"  (
		  "ID_P_SEQNO" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "ACNO_P_SEQNO" VARCHAR(10 OCTETS) NOT NULL WITH DEFAULT  , 
		  "CARD_NO" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT  , 
		  "V_CARD_NO" VARCHAR(19 OCTETS) NOT NULL WITH DEFAULT  , 
		  "PROG_CODE" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT  , 
		  "RESP_CODE" VARCHAR(1 OCTETS) NOT NULL WITH DEFAULT  , 
		  "REASON_DESC" VARGRAPHIC(60 CODEUNITS16) NOT NULL WITH DEFAULT '' , 
		  "EPAY_TYPE" VARCHAR(2 OCTETS) NOT NULL WITH DEFAULT  , 
		  "CRT_DATE" VARCHAR(8 OCTETS) NOT NULL WITH DEFAULT  , 
		  "CRT_TIME" VARCHAR(6 OCTETS) NOT NULL WITH DEFAULT  , 
		  "MOD_TIME" TIMESTAMP , 
		  "MOD_PGM" VARCHAR(20 OCTETS) NOT NULL WITH DEFAULT  )   
		 IN "TB_OTH_TBL" INDEX IN "TB_OTH_IDX"  
		 ORGANIZE BY ROW  ;

COMMENT ON TABLE "ECSCRDB "."EPAY_CARD_LOG" IS '虛擬卡紀錄檔' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."ACNO_P_SEQNO" IS '帳戶流水號     ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."CARD_NO" IS '卡號           ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."CRT_DATE" IS '鍵檔日期       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."CRT_TIME" IS '鍵檔時間       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."EPAY_TYPE" IS 'EPAY卡別 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."ID_P_SEQNO" IS '帳戶流水號碼   ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."MOD_PGM" IS '異動程式       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."MOD_TIME" IS '異動時間       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."PROG_CODE" IS '虛擬卡處理進度 ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."REASON_DESC" IS '原因說明       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."RESP_CODE" IS '交易結果       ' ;

COMMENT ON COLUMN "ECSCRDB "."EPAY_CARD_LOG"."V_CARD_NO" IS '虛擬卡號       ' ;

CREATE INDEX "ECSCRDB "."EPAY_CARD_LOG_IDX_1" ON "ECSCRDB "."EPAY_CARD_LOG" 
		("ID_P_SEQNO" ASC)
		
		COMPRESS NO 
		INCLUDE NULL KEYS ALLOW REVERSE SCANS ;

CREATE INDEX "ECSCRDB "."EPAY_CARD_LOG_IDX_2" ON "ECSCRDB "."EPAY_CARD_LOG" 
		("CARD_NO" ASC)
		
		COMPRESS NO 
		INCLUDE NULL KEYS ALLOW REVERSE SCANS ;


GRANT SELECT,INSERT,DELETE,UPDATE ON "ECSCRDB "."EPAY_CARD_LOG"  TO USER CRAP1, USER DCDBMOD ;
GRANT SELECT ON "ECSCRDB "."EPAY_CARD_LOG"  TO USER EMDAP41, USER EMDAP42 ;


terminate;
