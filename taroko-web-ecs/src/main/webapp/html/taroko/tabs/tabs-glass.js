//*******************************
//   Deluxe Tabs Data File
//   http://deluxe-tabs.com
//*******************************
var key="153b1109extg";
//------- Common -------
var bblankImage = "html/taroko/tabs/img/blank.gif";
var bselectedItem = 0;

//------- Menu -------
var bmenuOrientation = 0;
var bmenuWidth = "0";
var bmenuHeight = "22";
var bmenuBorderWidth = 0;
var bmenuBorderStyle = "";
var bmenuBackImage = "";

//------- Tab Mode -------

var tabMode = 0;
var bselectedSmItem = -1;
var bsmHeight = "10";
var bsmBackColor = "#FFFFFF";
var bsmBorderBottomDraw = 1;
var bsmBorderWidth = 0;
var bsmBorderStyle = "solid";
var bsmBorderColor = "#91A7B4";
var bsmItemAlign = "center";
var bsmItemSpacing = 1;
var bsmItemPadding = 0;
var bitemTarget = "_blank";

//------- Menu Positioning -------
var babsolute = 0;
var bleft = 120;
var btop = 120;
var bfloatable = 1;
var bfloatIterations = 6;

//------- Font -------
var bfontStyle = ["bold 8pt Tahoma"];
var bfontColor = ["#ffffff","","#ffffff"];
var bfontDecoration = ["none","none","none"];

//------- Items -------
var bbeforeItemSpace = 0;
var bafterItemSpace = 0;
var bitemBorderStyle = ["","",""];
var bitemBorderWidth = 0;
var bitemSpacing = 0;
var bitemPadding = 0;
var browSpace = 0;
var bitemAlign = "center";
var bitemCursor = "default";

//------- Item Images -------
var bitemBackImage = ["html/taroko/tabs/img/style01_n_back.gif","html/taroko/tabs/img/style01_o_back.gif","html/taroko/tabs/img/style01_s_back.gif"];
var bbeforeItemImage = ["","",""];
var bbeforeItemImageW = 13;
var bbeforeItemImageH = 22;
var bafterItemImage = ["","",""];
var bafterItemImageW = 13;
var bafterItemImageH = 22;

//------- Colors -------
var bmenuBackColor = "";
var bmenuBorderColor = "";
var bitemBorderColor = ["","",""];
var bitemBackColor = ["#24B327","#83D83D","#B79A15"];

//------- Icons -------
var biconWidth = 16;
var biconHeight = 16;
var biconAlign = "left";

//------- Separators -------
var bseparatorWidth = 7;

//------- Visual Effects -------
var transition = 24;
var btransOptions = "";
var transDuration = 400;

// bitemBackImageSpec syntax:
// bitemBackImageSpec=normal-normal,normal-over,normal-selected,over-normal,over-selected,selected-normal,selected-over
var back_nn="html/taroko/tabs/img/style01_nn_center.gif,";
var back_no="html/taroko/tabs/img/style01_no_center.gif,";
var back_ns="html/taroko/tabs/img/style01_ns_center.gif,";
var back_on="html/taroko/tabs/img/style01_on_center.gif,";
var back_os="html/taroko/tabs/img/style01_os_center.gif,";
var back_sn="html/taroko/tabs/img/style01_sn_center.gif,";
var back_so="html/taroko/tabs/img/style01_so_center.gif";
var backSpec = back_nn+back_no+back_ns+back_on+back_os+back_sn+back_so;

var bstyles =
[
  ["bitemWidth=22","bitemBackImageSpec="+backSpec],
  ["bbeforeItemImage=html/taroko/tabs/img/style01_n_left.gif,html/taroko/tabs/img/style01_o_left.gif,html/taroko/tabs/img/style01_s_left.gif"],
  ["bafterItemImage=html/taroko/tabs/img/style01_n_right.gif,html/taroko/tabs/img/style01_o_right.gif,html/taroko/tabs/img/style01_s_right.gif"],
];

var bmenuItems =
[
  ["TAB_DESC","TAB_CONTENT",,,,,"TAB_INDEX"],
  ["-",,,,,,"0"],
];

dtabs_init();
