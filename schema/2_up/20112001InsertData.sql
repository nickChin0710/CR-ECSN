connect to cr;

INSERT INTO ECSCRDB.CNV_CONFIG (PROGRAM_NAME,INPUT_FILEPATH,INPUT_FILENAME,OUTPUT_FILEPATH,OUTPUT_FILENAME) VALUES 
('UpdCrdCard','/crdataupload/','CARDOPEN.txt','/crdataupload/','')
,('CnvAdjLmtAmt','/crdataupload/','adjlmtamt_20201012.txt','/crdataupload/','')
,('CnvIcud','/crdataupload/','M00600000.ICACTQND.09101601','/crdataupload/','')
,('CnvIcud2','/crdataupload/','M00600000.ICCACQND.09101601','/crdataupload/','')
;

terminate;
