
 var messageChange="",newWindow="",ajaxFlag="",logoutFlag="";
 var levelNum=0,treeWid=0,preLevel=0;

 var respLevel="",origSeq="",origMesg="",pageType="";
 var applHost="",applPort="",applName="",appProtocol="";
 var packageName="",javaName="",methodName="",queryOnly="",queryFirst="";

 var loginUser="0000",menuCode="",menuSeq="",authData="",requHtml="",respHtml="",levelCode="",hideTabs="",csrfValue="";
 var actionCode="",funCode="",pageButton="",respCode="",totalPage="",totalRows="",pageRows="",currPage="",currRows="";
 var browserType="",menuDesc="",errField="",respMesg="";

 var hideData="";

 var popupWindow = null;

 var deleteConfirm_show="Y";
 var idno_queryReason="XX";
 var user_deptno="";

 var initHtml  ="";
 var activePnt = -1;    // 控制 ACTIVE MENUS TABS
 var maxLevel  = 5;     // 最多 LEVEL 數目
 var maxTabs   = 7;     // 最多 MENU TABS 數目
 var tabsInd   = 0;     // 紀錄 ACTIVE MENU TABS 指標
 var tabsCount = 0;     // 紀錄 MENU TABS 數目
 var menuTabLevel = []; // 紀錄 MENU TABS (ACTIVE LEVEL)
 var tabsName     = []; // 紀錄 MENU TABS (FRAME NAME)
 var tabsiDesc    = []; // 紀錄 MENU TABS 程式說明資料
 var tabsSeq      = new Object(); // MENU TABS SEQ
 var winParm      = new Array();
 
 var hideField    = new Array();

 function createHideData()
 {
    try {
      user_deptno =document.forms["buttonForm"]["top_user_deptno"].value;
    } catch (err) {
      user_deptno ='';
    }
    try {
      idno_queryReason =document.forms["buttonForm"]["query_reason"].value;
    } catch (err) {
      idno_queryReason ='';
    }

    errField="";
    menuDesc="";
    respCode      =  "00";
    respMesg      =  "";

    hideField[0]  = loginUser;
    hideField[1]  = menuCode;
    hideField[2]  = menuSeq;
    hideField[3]  = requHtml;
    hideField[4]  = respHtml;
    hideField[5]  = levelCode;
    hideField[6]  = hideTabs;
    hideField[7]  = csrfValue;
    hideField[8]  = actionCode;
    hideField[9]  = funCode;
    hideField[10] = pageButton;
    hideField[11] = respCode;
    hideField[12] = ""+totalPage;
    hideField[13] = ""+totalRows;
    hideField[14] = ""+pageRows;
    hideField[15] = ""+currPage;
    hideField[16] = ""+currRows;
    hideField[17] = browserType;
    hideField[18] = menuDesc;
    hideField[19] = errField;
    hideField[20] = respMesg;
    hideField[21] = idno_queryReason;
    hideField[22] = user_deptno;
    hideField[23] = authData;

    hideData = hideField[0];
    for( var i=1; i<hideField.length; i++ ) {
         if ( hideField[i].length == 0 )
            { hideField[i]=" "; }
         hideData = hideData + "#" + hideField[i];
       }

    hideData = Base64.encode(hideData);
    return true;
  }

 function splitHideData()
 {
    if ( hideData.length == 0 )
       { return; }

    var nullValue="";
    cvtHide   = Base64.decode(hideData);

    hideField = cvtHide.split("#");
    var userId ="";
    userId        =  hideField[0];
    menuCode      =  hideField[1];
    menuSeq       =  hideField[2];
    requHtml      =  hideField[3];
    respHtml      =  hideField[4];
    levelCode     =  hideField[5];
    nullValue     =  hideField[6];
    csrfValue     =  hideField[7];
    actionCode    =  hideField[8];
    funCode       =  hideField[9];
    pageButton    =  hideField[10];
    respCode      =  hideField[11];
    totalPage     =  hideField[12];
    totalRows     =  hideField[13];
    pageRows      =  hideField[14];
    currPage      =  hideField[15];
    currRows      =  hideField[16];
    browserType   =  hideField[17];
    menuDesc      =  hideField[18];
    errField      =  hideField[19];
    respMesg      =  hideField[20];
    nullValue     =  hideField[21];
    nullValue     =  hideField[22];
    authData      =  hideField[23];

    if ( userId.length > 0 )
       { loginUser = userId; }

    origSeq  = menuSeq;
    menuCode = "";
    return true;
  }


