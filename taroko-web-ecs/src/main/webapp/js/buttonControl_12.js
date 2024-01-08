
function menuControl(linkParm)
 { 
    if ( (tabsCount+1) > maxTabs && document.buttonForm.M_TABS.checked == true )
       { alert('超過 MENU TABS 數目 !!'); return; }

    var httpRequest = new XMLHttpRequest();
    if ( !httpRequest ) {
        alert('Cannot create an XMLHTTP instance');
        return false;
      }

    actionCode ="";
    linkData = linkParm.split(",");
    if ( linkData[1].length == 0)  {
         alert('menu select error');
         return;
    }
    menuSeq    = linkData[0];
    authData   = linkData[1];
    menuSubmit();
    return;
 }

function menuSubmit()
{
    resetCondition();

    menuCode = "S";
    createHideData();
    tabsCount++;
    var ck_seq = tabsSeq[menuSeq];
    if ( ck_seq == null )
       { ck_seq = 1; }
    else
       { ck_seq++;   }

    if ( document.buttonForm.M_TABS.checked == false )
       { tabsCount= 1; ck_seq= 1; }
    createIframe("M","",menuSeq+"-"+ck_seq);
    tabsSeq[menuSeq] = ck_seq;
    menuCode="";
    messageChange ="Y";
    startWaiting();
    return;
}

function processLogout()
 {
   logoutFlag = "Y";
   getSystemUrl();
 }

// AJAX 取得 appProtocol,applHost,applPort,applName
function getSystemUrl() {

    menuCode = "Y";
    createHideData();
    menuCode = "";
	if ( logoutFlag == "Y" )
		{ logoutResponse(); }
    else
		{ processNewWindow(); }
    //var servurl = "MainControl?HIDE=" + hideData;
    //var httpRequest = new XMLHttpRequest();
    //if ( !httpRequest ) {
    //    alert('Cannot create an XMLHTTP instance');
    //    return false;
    //  }
    //httpRequest.onreadystatechange = function () {  servResponse(httpRequest);  };
    //httpRequest.open('POST', servurl, true);
    //httpRequest.send(null);
    return;
 }

 function servResponse(httpRequest)
 {
    if ( httpRequest.readyState == 4 && httpRequest.status == 200 ) {
         var cvtData = [];
         var ajaxData = httpRequest.responseText;
         cvtData  = ajaxData.split(",");
         // 2020-09-30 Justin: http and https
         appProtocol = cvtData[0]; 
         applHost = cvtData[1]; 
         applPort = cvtData[2];
         applName = cvtData[3];
         if ( logoutFlag == "Y" )
            { logoutResponse(); }
         else
         	  { processNewWindow(); }
     }
   
   return;
 }

 function logoutResponse() {
 	
    menuCode = "O";
    createHideData();
    // 2020-09-30 Justin: http and https
	// 2022-03-16 Justin: use location.href instead of ajax
    window.setTimeout("location.href='" + location.href + "?HIDE=" + hideData + "';", 50);
    return;
 }

function startWaiting()
{
    spinner.stop();
    target1 = document.getElementById("mainPage");
    spinner = new Spinner(opts).spin(target1);
}

function enterKey(event)
{
    if (event.keyCode != 13) {
        return;
    }
    return submitControl('X');
}

function popupBlock(blockId) {
	
    disableEffect();
    var k = levelNum + (tabsInd * maxLevel);
    dataFrame.frames[k].document.getElementById(blockId).style.display = 'block';
    return;
}

function hideBlock(blockId)
{
    var k = levelNum + (tabsInd * maxLevel);
    var disElmnt = dataFrame.frames[k].document.getElementById("disDiv");
    if (disElmnt != null) {
        disElmnt.style.display = 'none';
    }
    dataFrame.frames[k].document.getElementById(blockId).style.display = 'none';
    return;
}

function setBlockValue(setField, parmField)
{
   var k = levelNum + (tabsInd * maxLevel);
   var setElements  = dataFrame.frames[k].document.getElementsByName(setField);
   var parmElements = dataFrame.frames[k].document.getElementsByName(parmField);
   setElements[0].value = parmElements[0].value;
   return true;
}

function openWindow(parmCode, parmHtml, parmName, windowSize)
{
	 winParm[0]=parmCode;
	 winParm[1]=parmHtml;
	 winParm[2]=parmName;
	 winParm[3]=windowSize;
	 getSystemUrl();
   return;
}

// popop 上傳檔案網頁
function processNewWindow()
{
    var parmCode   = winParm[0];
    var parmHtml   = winParm[1];
    var parmName   = winParm[2];
    var windowSize = winParm[3];    
    var parmString = "";
    var parm = new Array();
    parm = parmName.split(",");    
    var k = levelNum + (tabsInd * maxLevel);
    for (var i = 0; i < parm.length; i++)  {
        var aElements = dataFrame.frames[k].document.getElementsByName(parm[i]);
        var parmValue = aElements[0].value;
        if (parm[i].length > 0) {
            parmString = parmString + "&" + parm[i] + "=" + parmValue;
        }
    }    
    var svAction = actionCode;
    var svRequ = requHtml;
    var svResp = respHtml;

    actionCode = parmCode;
    requHtml   = parmHtml;
    respHtml   = parmHtml;    
    if ( parmHtml == "TarokoUpload" )
       { menuCode  = "US"; }    
    createHideData();
    actionCode = svAction;
    requHtml = svRequ;
    respHtml = svResp;    
    // 2020-09-30 Justin: http and https
	// 2022-03-16 Justin: use location.href instead of ajax
    var url = "MainControl?HIDE=" + hideData + parmString;
    var windowName = parm[0];
    
    menuCode = "";
    createHideData();
    k = levelNum + (tabsInd * maxLevel);    
    dataFrame.frames[k].document.dataForm.HIDE.value = hideData;    
    popupWindow = window.open(url, "SS", windowSize, titlebar = 'no');
    popupWindow.focus();
    
    return;
}

function escapeHTML(str) {
     str = str + "''";
     var out = "''";
     for(var i=0; i<str.length; i++) {
         if(str[i] === '<') {
             out += '&lt;';
         } else if(str[i] === '>') {
             out += '&gt;';
         } else if(str[i] === "'") {
             out += '&#39;';
         } else if(str[i] === '"') {
             out += '&quot;';
         } else {
             out += str[i];
         }
     }
     return out;
}

function disableEffect()
{
    var k = levelNum + (tabsInd * maxLevel);
    var disElmnt = dataFrame.frames[k].document.getElementById("disDiv");
    if (disElmnt != null) {
        disElmnt.style.display = 'block';
        var elmnt = dataFrame.frames[k].document.getElementsByTagName("html");
        disElmnt.style.height = elmnt[0].scrollHeight;
        disElmnt.style.width = elmnt[0].scrollWidth;
    }
}

function normalEffect()
 {
    var k = levelNum + (tabsInd * maxLevel);
    var disElmnt = dataFrame.frames[k].document.getElementById("disDiv");
    if ( disElmnt != null ) {
         disElmnt.style.display = 'none';
       }
 }

function bodyFocus()
{
    if (popupWindow == null || popupWindow.closed) {
        return;
    }
    popupWindow.focus();
}

function getWindowValue(parmField, parmValue)
{
    var k = levelNum + (tabsInd * maxLevel);
    var aElements = window.dataFrame.frames[k].document.getElementsByName(parmField);
    aElements[0].value = parmValue;
}

function submitControl(parmFunc)
{
    if (parmFunc == "D" && deleteConfirm_show == 'Y' ) {
        if (confirm("確定要刪除 !!") == false) {
            return false;
        }
    }
    deleteConfirm_show ="Y";

    actionCode = parmFunc;
    if (parmFunc != "B") {
        funCode = parmFunc;
    }

    hideData = "";
    ajaxFlag = "";
    document.buttonForm.MESG_DATA.value = "";

    var k = parseInt(levelCode);
    k = k + (tabsInd * maxLevel);
    if ( !dataFrame.frames[k].validateInput() ) {
         return  false;
     }

    var j = parseInt(respLevel);
    j = j + (tabsInd * maxLevel);
    var targetFrame  = dataFrame.frames[j].name;

    var waitFlag = "";

    if ( parmFunc.indexOf("PDF")>=0 || parmFunc.indexOf("XLS")>=0 || parmFunc.indexOf("DL")>=0) {
         dataFrame.frames[k].document.dataForm.target = "_blank";
    }
    else {
         dataFrame.frames[k].document.dataForm.target = targetFrame;
         waitFlag = "Y";
    }

    //alert('submit tabs '+tabsInd+' level '+levelCode+' curr '+k+' resp '+j+' '+targetFrame);
    if ( menuSeq.length >= 4 && menuSeq != origSeq )
       { menuCode = "S2"; }
    else
    	 { menuCode = "S3"; }
    createHideData();
    //origSeq = menuSeq;
    dataFrame.frames[k].document.dataForm.HIDE.value = hideData;
    dataFrame.frames[k].document.dataForm.action = "MainControl";
    dataFrame.frames[k].document.dataForm.submit();

    if (waitFlag == "Y") {
        startWaiting();
    }

    return false;
}

function pageControl(parmFunc)
{
    if ( !checkPage() ) {
        return false;
    }

    if (parmFunc == "CP") {
        currPage = document.buttonForm.CURR_PAGE.value;
    }

    funCode = parmFunc;
    pageRows = document.buttonForm.PAGE_ROWS.value;
    respHtml   = requHtml;

    submitControl("B");
    return;
}

function checkPage()
{
  if (isNumber(document.buttonForm.CURR_PAGE.value))
    {
        if (!isNumber(document.buttonForm.PAGE_ROWS.value))
        {
            alert("每頁筆數格式錯誤");
            return false;
        }
        if (document.buttonForm.PAGE_ROWS.value > 100)
        {
            alert("每頁最多顯示100筆");
            return false;
        }
    } else
    {
        alert("頁數格式輸入錯誤");
        return false;
    }
    return true;
}

function dispMessage()
{
    if (origMesg.length > 0) {
        respMesg = origMesg;
        origMesg = "";
    }

    if (respMesg.length < 2) {
        mesgDiv.style.visibility = "hidden";
    }
    else {
        document.buttonForm.MESG_DATA.value = respMesg;
        mesgDiv.style.visibility = "visible";
        if (actionCode == "B" || actionCode == "S") {
            document.buttonForm.MESG_DATA.setAttribute("size", "60");
        }
        else {
            document.buttonForm.MESG_DATA.setAttribute("size", "80");
        }
    }

    return true;
 }

 function refreshButton2(parmLevel)
 {
    preLevel  = menuTabLevel[tabsInd];

    var homeCode = parmLevel;
    levelCode = parmLevel;
    respLevel = parmLevel;
    levelNum  = parseInt(respLevel);
    var k = levelNum + (tabsInd * maxLevel);

    hideData = "";
    var checkHide = dataFrame.frames[k].document.dataForm.HIDE.value;

    if ( checkHide.length == 0 ) {
         alert("hide data error");
    } else {
         hideData = dataFrame.frames[k].document.dataForm.HIDE.value;
    }

    if ( hideData.length >= 20 ) {
         splitHideData();
         if ( respCode != "00" && preLevel != levelNum ) {
              origMesg  = respMesg;
              parmLevel = ""+preLevel;
              k = preLevel + (tabsInd * maxLevel);
              hideData = dataFrame.frames[k].document.dataForm.HIDE.value;
              splitHideData();
              respMesg = origMesg;
        }
    }

    levelCode = parmLevel;
    respLevel = parmLevel;
    levelNum  = parseInt(respLevel);

    document.buttonForm.PAGE_ROWS.value = pageRows;
    document.buttonForm.CURR_PAGE.value = currPage;
    document.buttonForm.PAGE_STAT.value = "總共 " + (totalPage) + " 頁 " + (totalRows) + " 筆";
    document.buttonForm.MESG_DATA.value = "";

    pageDiv.style.visibility = "hidden";
    
    if ( actionCode.charAt(0) == "L" )
       { }
    else
    if ( actionCode.charAt(0) == "B" || actionCode.charAt(0) == "S" )
       {
         if ( pageRows < 999 && levelCode == "0" ) {
              pageDiv.style.visibility = "visible";
          }
       }
    else
    if ( pageRows < 999 && totalPage > 0 && levelCode == "0" ) 
       { pageDiv.style.visibility = "visible"; }

    mandatoryControl();
    dispMessage();

    hideTabs = tabsName[tabsInd];
    if ( messageChange == "Y" )
       { tabsiDesc[tabsInd] = menuDesc; }
    programMessage();
    menuDesc = "";
    messageChange="";

    if ( errField.length > 2 ) {
         respControl();
    }

    frameControl(levelCode);
    window.focus();
    createHideData();
    spinner.stop();
    respHtml  =  "";
    return;
 }

function programMessage()
 {  
    var mycel = document.getElementById("showProgram");
    myceltext = mycel.childNodes.item(0);
    myceltext.data = tabsiDesc[tabsInd];

    if ( tabsCount == 0 )
       { document.buttonForm.MESG_DATA.value = ""; }
 
    return;
 }

function stopWaiting()
 {
    spinner.stop();
    return;
 }

function dataFormControl(k)
{
    respMesg = "";
    origMesg = "";
    return;
}

function mandatoryControl()
 {
    var types = new Array("input", "select", "textarea");
    var aElements = null;
    var setRequ = "";
    var n = parseInt(levelCode);
    n =  n + (tabsInd * maxLevel);

    for (var i = 0; i < types.length; i++)
    {
        aElements = window.dataFrame.frames[n].document.dataForm.getElementsByTagName(types[i]);
        for (var j = 0; j < aElements.length; j++)
        {
            setRequ = aElements[j].getAttribute("zRequire");
            if (setRequ == null) {
                setRequ = "N";
            }
            setRequ = setRequ.toLocaleLowerCase();
            if ( setRequ=="k" || setRequ=="c" || setRequ=="yy" ||
                    setRequ.toLocaleLowerCase()=="y" ) {
               aElements[j].style.background = "#ffffce";
            } //"yellow";

        }  // end of for loop TagName
    }  // end of for loop types
    return;
 }  // end of mandatoryControl

function respControl()
 {
    var types = new Array("input", "select", "textarea");
    var respField = new Array();
    var aElements = null;
    var li = 0;

    respField = errField.split(",");
    errField = "";
    for (var i = 0; i < types.length; i++)
    {
        if (ajaxFlag == "Y") {
            li = levelNum;
        }
        else {
            li = preLevel;
        }

        li = li + (tabsInd * maxLevel);
        ajaxFlag = "";
        aElements = window.dataFrame.frames[li].document.dataForm.getElementsByTagName(types[i]);
        for (var j = 0; j < aElements.length; j++)
        {
            var inType = aElements[j].getAttribute("type");
            if (inType == "hidden") {
                continue;
            }
            if (inType == "checkbox" || inType == "radio") {
                if (aElements[j].checked != true) {
                    continue;
                }
            }
            var mapName = aElements[j].getAttribute("name");

            for (var n = 0; n < respField.length; n++) {
                if (mapName == respField[n]) {
                    aElements[j].style.background = "pink";
                }
            }
        }
    }
    return;
 }  // end of respControl

function light_img(obj)
{
    obj.style.filter = "alpha(opacity=100)";
}

function dark_img(obj)
{
    obj.style.filter = "alpha(opacity=50)";
}

function initIframe()
 {
    tabsName=[];
    menuTabLevel=[];
    tabsSeq=[];

    for ( var i=0; i< maxTabs; i++)
        { tabsiDesc[i] =""; }
    
    programMessage();
    btnPage_hide();
    spinner.stop();
    return;
 }

 function createIframe(ckCode,parmURL,fName)
 {
    try {
        if ( tabsCount == 1 ) {
             for ( var i=0; i < tabsName.length; i++ )
                 { removeIframe(tabsName[i]); }
             initIframe();
          }

        tabsInd =0;
        if ( tabsCount > 1 )
           { tabsInd = tabsCount -1; }

        activePnt = -1;
        tabsName.push(fName);
        menuTabLevel.push(0);

        if ( ckCode == "M" ) {
        	   hideTabs = tabsName[tabsInd];
             createHideData();
           	 parmURL = "MainControl?HIDE="+ hideData; 
          }

        for ( var  i = 0; i < maxLevel; i++) {
              var  ifs = dataFrame.document.createElement("IFRAME");
              ifs.setAttribute("name", tabsName[tabsInd]+"#"+i);
              ifs.setAttribute("id", tabsName[tabsInd]+"#"+i);
              ifs.setAttribute("frameborder", "0");
              ifs.setAttribute("width", "100%");
              if ( i == 0 ) {
                   ifs.setAttribute("height", "100%");
                   ifs.setAttribute("src", parmURL);
                 }
              else {
                   ifs.setAttribute("height", "0%");
                 }
              dataFrame.document.body.appendChild(ifs);
            }

         if ( tabsCount > 0 )
            { menuTabs_control(); }
      }
    catch( err )
      { alert('createIframe error'); }

   return;
 }

 function removeIframe(removeName)
 {
   try {
         for ( var i = 0; i < maxLevel; i++ ) {
               var elem  = dataFrame.document.getElementById(removeName+"#"+i);
               elem.parentNode.removeChild(elem);
           }
      }
   catch( err )
      { alert('removeIframe error'); }

   return;
 }

 function frameIndex()
 {
    return levelNum + (tabsInd * maxLevel)
 }
 
 function frameControl(parmCode) {
    try {
          levelCode = parmCode;
          levelNum  = parseInt(levelCode);

          if ( levelCode > "4" ) {
               levelCode = "4";
          }

          if ( levelCode == "0"  ) {
             frameSizeControl("100%,0,0,0,0");
          }
          else if (levelCode == "1") {
             frameSizeControl("0,100%,0,0,0");
          }
          else if (levelCode == "2") {
              frameSizeControl("0,0,100%,0,0");
          }
          else if (levelCode == "3") {
              frameSizeControl("0,0,0,100%,0");
          }
          else if (levelCode == "4") {
              frameSizeControl("0,0,0,0,100%");
          }

    } catch (err) {
        alert('frameControl error');
    }

    return;
 }

 function frameSizeControl(parmdata)
 {
    try {
         var parm = parmdata.split(",");
         for ( var i=0; i < tabsName.length; i++ )
             {
               for ( var j=0; j< maxLevel; j++ )
                   {
                     var  disFrame1 = dataFrame.document.getElementById(tabsName[i]+"#"+j);
                     if ( disFrame1 != null )
                        { disFrame1.style.display = "none";  }
                   }
             }

         for ( var i=0; i < maxLevel; i++)
             {
               if ( parm[i] != "0" ) {
                    var  disFrame2 = dataFrame.document.getElementById(tabsName[tabsInd]+"#"+i);
                    if ( disFrame2 != null )
                       {
                         disFrame2.style.display = "";
                         disFrame2.height = parm[i];
                         menuTabLevel[tabsInd] =  i;
                         //alert('tabs '+tabsInd+' level '+levelCode+' '+(tabsName[tabsInd]+"#"+i));
                       }
                 }
             }
        }
   catch( err )
        { alert('frameSizeControl error'); }
   return;
 }


function upperLevel_0()
{
    levelCode = "0";
    respLevel = "0";
    refreshButton2("0");

    return;
}
function upperLevel()
{
    var cvtCode = "";

    levelNum = parseInt(levelCode);
    cvtCode = "" + levelNum;
    levelNum = levelNum - 1;
    if (levelNum < 0) {
        levelNum = 0;
    }

    cvtCode = "" + levelNum;

    refreshButton2(cvtCode);
    return;
}

function closeDetail()
{
    var k = levelNum + (tabsInd * maxLevel);
    dataFrame.frames[k].document.body.innerHTML = "";
    var cvtCode = "" + preLevel;
    refreshButton2(cvtCode);

    return;
}


function autoTab(original, destination)
{
    if (original.getAttribute && original.value.length == original.getAttribute("maxlength"))
    {
        destination.focus();
    }
}

function resetCondition()
{
    origMesg = "";
    levelCode = "0";
    levelNum = 0;
    preLevel = 0;
    menuCode = "";
    totalPage = 0;
    totalRows = 0;
    pageRows = 20;
    currPage = 0;
    currRows = 0;
    return;
}

function highlightText()
{
    var k = levelNum + (tabsInd * maxLevel);
    var inobj = dataFrame.frames[k].document.dataForm.getElementsByTagName('input');
    for (var j = 0; j < inobj.length; j++)
    {
        var inType = inobj[j].getAttribute("type");
        if (inType != "text") {
            continue;
        }
        inobj[j].onfocus = function () {
            this.select();
        }
    }
}

function getBrowserType()
{
    var browserName = "", browserAgent = "";

    browserName = navigator.appName;
    browserAgent = navigator.userAgent;

    if ( browserName.length >= 8 ) {
        if (browserName.substring(0, 8) == "Netscape") {
            if (browserAgent.indexOf("Firefox") == -1) {
                browserType = "Chrome";
            }
            else {
                browserType = "Firefox";
            }
        }
    }

    if ( browserName.length >= 9) {
         if ( browserName.substring(0, 9) == "Microsoft") {
              browserType = "IE";
        }
    }
    return;
}

function btnPage_hide()
{
    pageDiv.style.visibility = "hidden";
}

function scrollFunction() {

	var k = levelNum + (tabsInd * maxLevel);
  if (dataFrame.frames[k].document.body.scrollTop > 50 || dataFrame.frames[k].document.documentElement.scrollTop > 50) {
    dataFrame.frames[k].document.getElementById("topBtn").style.display = "block";
  } else {
    dataFrame.frames[k].document.getElementById("topBtn").style.display = "none";
  }
}

function topFunction() {

  var k = levelNum + (tabsInd * maxLevel);
  dataFrame.frames[k].document.body.scrollTop = 0;
  dataFrame.frames[k].document.documentElement.scrollTop = 0;
}