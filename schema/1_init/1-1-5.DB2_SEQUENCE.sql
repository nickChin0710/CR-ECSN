CONNECT TO CR;

SET CURRENT SCHEMA = "ECSCRDB";


---------------------------------
-- DDL Statements for Sequences
---------------------------------


CREATE SEQUENCE "ECSCRDB "."BIL_CONTRACTSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."BIL_POSTSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."CCA_VDTXN_SEQNO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."CMS_CASE" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."COL_MODSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."CTI_TXNSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."DBA_TXNSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_ACNO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_CARD_ACCT_IDX" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_CORP" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE NO CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_CRET_ACTNO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_DBMSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_DEBT_ACTNO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_DEBT_CRET_ACTNO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_DEBT_IDNO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_IDNO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_JRNLSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_MODSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_STOP" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."ECS_TRACE_NO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."MKT_MODSEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."RSK_CTRLSEQNO" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."SEQ_AUTH_SEQNO" AS BIGINT
	MINVALUE 1 MAXVALUE 999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."SEQ_CALLBATCH" AS BIGINT
	MINVALUE 1 MAXVALUE 99999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."SEQ_IBM_OUTGOING" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."SEQ_OUTGOING" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."SEQ_SEND_IBMSEQNO" AS BIGINT
	MINVALUE 2 MAXVALUE 999999
	START WITH 2 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;


CREATE SEQUENCE "ECSCRDB "."TSCC_DATASEQ" AS BIGINT
	MINVALUE 1 MAXVALUE 9999999999
	START WITH 1 INCREMENT BY 1
	NO CACHE CYCLE NO ORDER;




GRANT USAGE ON SEQUENCE "ECSCRDB "."BIL_CONTRACTSEQ"    TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."BIL_POSTSEQ"        TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."CCA_VDTXN_SEQNO"    TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."CMS_CASE"           TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."COL_MODSEQ"         TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."CTI_TXNSEQ"         TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."DBA_TXNSEQ"         TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_ACNO"           TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_CARD_ACCT_IDX"  TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_CORP"           TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_CRET_ACTNO"     TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_DBMSEQ"         TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_DEBT_ACTNO"     TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_DEBT_CRET_ACTNO"TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_DEBT_IDNO"      TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_IDNO"           TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_JRNLSEQ"        TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_MODSEQ"         TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_STOP"           TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."ECS_TRACE_NO"       TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."MKT_MODSEQ"         TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."RSK_CTRLSEQNO"      TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."SEQ_AUTH_SEQNO"     TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."SEQ_CALLBATCH"      TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."SEQ_IBM_OUTGOING"   TO USER crap1, USER dcdbmod; 
GRANT USAGE ON SEQUENCE "ECSCRDB "."SEQ_OUTGOING"       TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."SEQ_SEND_IBMSEQNO"  TO USER crap1, USER dcdbmod;
GRANT USAGE ON SEQUENCE "ECSCRDB "."TSCC_DATASEQ"       TO USER crap1, USER dcdbmod;



COMMIT WORK;

CONNECT RESET;

TERMINATE;






