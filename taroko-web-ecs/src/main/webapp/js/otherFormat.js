function emailFormat(emailAddRess)
 {
   if ( emailAddRess.length === 0 )
      { return true; }
   var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
   if( reg.test(emailAddRess) === false )
     { return false; }
   return true;
 }

function checkCreditCard (cardnumber)
{
   if ( cardnumber.length === 0 )
      { return true; }
   if ( cardnumber.length < 13 )
      { return false; }

  var cardname="";
  // Array to hold the permitted card characteristics
  var cards = new Array();

  //  Define the cards we support.  You may add addtional card types as follows.

  //  Name:         As in the selection box of the form - must be same as user's
  //  Length:       List of possible valid lengths of the card number for the card
  //  prefixes:     List of possible prefixes for the card
  //  checkdigit:   Boolean to say whether there is a check digit

  cards [0]  = { name: "Visa",        length: "13,16",   prefixes: "4",checkdigit: true };
  cards [1]  = { name: "MasterCard",  length: "16",      prefixes: "51,52,53,54,55",checkdigit: true };
  cards [2]  = { name: "DinersClub",  length: "14,16",   prefixes: "36,54,55",checkdigit: true };
  cards [3]  = { name: "CarteBlanche",length: "14",      prefixes: "300,301,302,303,304,305",checkdigit: true };
  cards [4]  = { name: "AmEx",        length: "15",      prefixes: "34,37",checkdigit: true };
  cards [5]  = { name: "Discover",    length: "16",      prefixes: "6011,622,64,65",checkdigit: true };
  cards [6]  = { name: "JCB",         length: "16",      prefixes: "35",checkdigit: true };
  cards [7]  = { name: "enRoute",     length: "15",      prefixes: "2014,2149",checkdigit: true };
  cards [8]  = { name: "Solo",        length: "16,18,19",prefixes: "6334,6767",checkdigit: true };
  cards [9]  = { name: "Switch",      length: "16,18,19",prefixes: "4903,4905,4911,4936,564182,633110,6333,6759",checkdigit: true };
  cards [10] = { name: "Maestro",     length: "12,13,14,15,16,18,19",prefixes: "5018,5020,5038,6304,6759,6761,6762,6763",checkdigit: true };
  cards [11] = { name: "VisaElectron",length: "16",      prefixes:  "4026,417500,4508,4844,4913,4917",checkdigit: true };
  cards [12] = { name: "LaserCard",   length: "16,17,18,19",prefixes: "6304,6706,6771,6709",checkdigit: true };

  // Ensure that the user has provided a credit card number
  if (cardnumber.length < 12)  {
     return false;
  }

  if ( cardnumber.substring(0,1) === "4" )
     { cardname = "visa";  }
  else
  if ( cardnumber.substring(0,1) === "5" )
     { cardname = "MasterCard";  }
  else
  if ( (cardnumber.substring(0,2) === "34" || cardnumber.substring(0,2) === "37") && cardname.length === 15 )
     { cardname = "AmEx";  }
  else
  if ( cardnumber.substring(0,1) === "3" )
     { cardname = "JCB";   }

  // Establish card type
  var cardType = -1;
  for ( var i=0; i<cards.length; i++ ) {

    // See if it is this card (ignoring the case of the string)
    if (cardname.toLowerCase() === cards[i].name.toLowerCase()) {
      cardType = i;
      break;
    }
  }

  // If card type not found, report an error
  if (cardType === -1) {
     return false;
  }

  // Now remove any spaces from the credit card number

  // Check that the number is numeric
  var cardNo = cardnumber;
  var cardexp = /^[0-9]{13,19}$/;
  if (!cardexp.exec(cardNo))  {
     return false;
  }

  // Now check the modulus 10 check digit - if required
  if (cards[cardType].checkdigit) {
    var checksum = 0;                                  // running checksum total
    var mychar = "";                                   // next char to process
    var j = 1;                                         // takes value of 1 or 2

    // Process each digit one by one starting at the right
    var calc;
    for (i = cardNo.length - 1; i >= 0; i--) {

      // Extract the next digit and multiply by 1 or 2 on alternative digits.
      calc = Number(cardNo.charAt(i)) * j;

      // If the result is in two digits add 1 to the checksum total
      if (calc > 9) {
        checksum = checksum + 1;
        calc = calc - 10;
      }

      // Add the units element to the checksum total
      checksum = checksum + calc;

      // Switch the value of j
      if (j ===1) {j = 2;} else {j = 1;};
    }

    // All done - if checksum is divisible by 10, it is a valid modulus 10.
    // If not, report an error.
    if (checksum % 10 !== 0)  {
     return false;
    }
  }

  // The following are the card-specific checks we undertake.
  var LengthValid = false;
  var PrefixValid = false;
  var undefined;

  // We use these for holding the valid lengths and prefixes of a card type
  var prefix  = new Array();
  var lengths = new Array();

  // Load an array with the valid prefixes for this card
  prefix = cards[cardType].prefixes.split(",");

  // Now see if any of them match what we have in the card number
  for (i=0; i<prefix.length; i++) {
    var exp = new RegExp ("^" + prefix[i]);
    if (exp.test (cardNo)) PrefixValid = true;
  }

  // If it isn't a valid prefix there's no point at looking at the length
  if (!PrefixValid) {
     return false;
  }

  // See if the length is valid for this card
  lengths = cards[cardType].length.split(",");
  for (j=0; j<lengths.length; j++) {
    if (cardNo.length === lengths[j]) LengthValid = true;
  }

  // See if all is OK by seeing if the length was valid. We only check the length if all else was
  // hunky dory.
  if (!LengthValid) {
     return false;
  };

  // The credit card is in the required format.
  return true;
 }
 
function idFormat(idStr)
 {
   if ( idStr.length === 0 )
      { return true; }
   if ( idStr.length !== 10 )
      { return false; }

   var letters = new Array('A', 'B', 'C', 'D',
       'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
       'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
       'X', 'Y', 'W', 'Z', 'I', 'O');

   var multiply = new Array(1, 9, 8, 7, 6, 5,4, 3, 2, 1);
   var nums = new Array(2);
   var firstChar="",firstNum="",lastNum="";
   var total = 0;
   // 第二個字為1或2，後面跟著8個數字，不分大小寫。
   var regExpID=/^[a-z](1|2)\d{8}$/i;
   if ( idStr.search(regExpID)===-1 )
      { alert("請仔細填寫身份證號碼");   return false;  }
   else
      {
        firstChar = idStr.charAt(0).toUpperCase();
        lastNum   = idStr.charAt(9);
      }

   for ( var i=0; i<26; i++ )
       {
         if ( firstChar === letters[i] )
            {
              firstNum = i + 10;
              nums[0] = Math.floor(firstNum / 10);
              nums[1] = firstNum - (nums[0] * 10);
              break;
            }
       }

   for( var i=0; i<multiply.length; i++ )
      {
        if (i<2)
           { total += nums[i] * multiply[i];  }
        else
           { total += parseInt(idStr.charAt(i-1)) * multiply[i];  }
      }

   if ( (10 - (total % 10) )!== lastNum)
      { return false; }

   return true;
 }
 