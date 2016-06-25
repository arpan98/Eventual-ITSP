// TODO: Needs to be fixed according to the new database

var form = document.forms[0];
var seq= "~!#";                 //QR Code Seperator
var duplicate=false;
var result =[];
  
  function linkcopy()
  {
    var copyTextareaBtn = document.querySelector('#copylink');
    copyTextareaBtn.addEventListener('click', function(event) {
    var copyTextarea = document.querySelector('#linkaddress');
    copyTextarea.select();

      try {
        var successful = document.execCommand('copy');
        var msg = successful ? 'successful' : 'unsuccessful';
        console.log('Copying text command was ' + msg);
      } catch (err) {
        console.log('Oops, unable to copy');
      }
    });
  }


  function displaylink(objectId)
  {
    document.getElementById("linkaddress").value = "http://www.EVENTual.com/" + objectId;
  }

  /*
  function OnPageLoad(){
    var url = window.location.href;
    console.log(url);
    var substring1 = "title=";
    var substring2 = "&description";
    if(url.indexOf(substring1)>-1 && url.indexOf(substring2)>-1){
      var t = url.substring(url.indexOf(substring1)+6,url.indexOf(substring2))
      console.log(t);
      console.log(oTitle);
      if(oTitle==t){
        displaylink(oId);
      }
    }
  }
  */

  function CheckDuplicate(title,description,allday,start,end,location,startyear,startmonth,startdate,starthour,startminute) {
    var EventData = Parse.Object.extend("EventData");

    /*Query by checking title and start times */
    var query = new Parse.Query(EventData);
    query.equalTo("title",title);
    query.equalTo("startyear",startyear);
    query.equalTo("startmonth",startmonth);
    query.equalTo("startdate",startdate);
    query.equalTo("starthour",starthour);
    query.equalTo("startminute",startminute);
    query.equalTo("location",location);

    /* Query first gives first object in query result list
        Use query.find for getting result list */
    query.find({
      success: function(object) {


        if(object!=undefined) {
          console.log(object);
          displaylink(object.id);
          duplicate=true;
          console.log("Inside function - " + duplicate + " " + object.id);
          var section = document.getElementById('result');
          section.style.visibility = "visible";
          $(qrcode).empty()
          $(qrcode).qrcode({
          "size": 300,
          "color": "#3a3",
          "render": "image",
          "background": "white",
          "text": "EVENTualQR"+seq+title+seq+description+seq+String(allday)+seq+start+seq+end+seq+location+seq
           });
        }
        else {
          duplicate=false;
          console.log(duplicate);
        }
      },
      error: function(error) {
        console.log("Error in query");
      }
    })
  }





  function savedata () {

  /*Disable save button to avoid repeatedly pressing */
  document.getElementById("Save").disabled = true; 

  setTimeout(function(){
            document.getElementById("Save").disabled = false; 
      }, 4000)
  
  var title = document.getElementsByName('title')[0].value;
  var description = document.getElementsByName('description')[0].value;  
  var allday = document.getElementsByName('allday')[0].checked;
  var location = document.getElementsByName('location')[0].value;
  //var timezone = document.getElementsByName('timezone')[0].value;
  if (allday==true)
  {
    var start = document.getElementsByName('date1')[0].value;
    var end = document.getElementsByName('date2')[0].value;
    var startyear = start.substring(6,10);
    var startmonth = start.substring(3,5);
    var starthour = "";
    var startminute = "";
    var startdate = start.substring(0,2);
    var endyear = end.substring(6,10);
    var endmonth = end.substring(3,5);
    var endhour = "";
    var endminute = "";
    var enddate = end.substring(0,2);
  }
  else
  {
    var start = document.getElementsByName('start')[0].value;
    var startmonth = start.substring(3,5);
    var startyear = start.substring(6,10);
    var starthour = start.substring(11,13);
    var startminute = start.substring(14,16);
    var startdate = start.substring(0,2);
    var end = document.getElementsByName('end')[0].value;
    var endmonth = end.substring(3,5);
    var endyear = end.substring(6,10);
    var endhour = end.substring(11,13);
    var endminute = end.substring(14,16);
    var enddate = end.substring(0,2);
  }
  if (!title)
    title="(No Title)";
  if (!description)
    description=" ";
  if (!location)
    location=" ";

  duplicate=false;

  Parse.initialize("09X6fff2y4FsZgQwBLe3LaVOX3QVNNwZe0eOGbBb",
    "aOKlDa7EsSrw583r8Whc1nCA2doEIh8iF9NILWO9");
  
  CheckDuplicate(title,description,allday,start,end,location,startyear,startmonth,startdate,starthour,startminute);
  /* Set a timeout so that query in checkduplicate function
    can finish */
  setTimeout(function(){
    console.log("function call - " +duplicate);
    if(duplicate==false) {
    var EventData = Parse.Object.extend("EventData");
    var eventdata = new EventData();
    var oId="";
    var oTitle="";
        eventdata.save({title: title, description : description , startdate: startdate, startmonth: startmonth, startyear: startyear, enddate: enddate, endmonth: endmonth, endyear: endyear, endhour: endhour, endminute: endminute, starthour: starthour, startminute: startminute, allday: allday, location: location
                      }).then(function(object) {
        oId=object.id;
        oTitle=object.title;
        displaylink(oId);
        console.log(oId);
    });

  /*QR Code Generator Jquery
    No debugging needed, directly copy-pasted */
  $(qrcode).empty()
  $(qrcode).qrcode({
      "size": 300,
      "color": "#3a3",
      "render": "image",
      "background": "white",
      "text": "EVENTualQR"+seq+title+seq+description+seq+String(allday)+seq+start+seq+end+seq+location+seq
    });

  var section = document.getElementById('result');
  section.style.visibility = "visible";

  console.log(title);
  console.log(startdate);
  console.log(startmonth);
  console.log(startyear);
  console.log(starthour);
  console.log(startminute);
  console.log(enddate);
  console.log(endmonth);
  console.log(endyear);
  console.log(endhour);
  console.log(endminute);
   
}
else 
{
    document.getElementById("Save").disabled = true; 
    setTimeout(function(){
          document.getElementById("Save").disabled = false; 
    }, 4000)
     
    alert("Event already exists!");
    }
  },3000)
  
}

/* It sends image url to hyperlink for downloading QR code */
function prepHref(linkElement) {
    
    linkElement.href =$('#qrcode').children('img').attr('src');
    linkElement.download = qrcodedownload;
}

function searchcheck()
{ 


    var l = document.getElementById('locationcheckbox').checked;
    var t = document.getElementById('titlecheckbox').checked;
    var d = document.getElementById('datetimecheckbox').checked;
    var a = document.getElementById('allday').checked;

    Parse.initialize("09X6fff2y4FsZgQwBLe3LaVOX3QVNNwZe0eOGbBb",
    "aOKlDa7EsSrw583r8Whc1nCA2doEIh8iF9NILWO9");
    var EventData = Parse.Object.extend("EventData");
    /*Query by checking title and start times */
    var query = new Parse.Query(EventData);

    if(t)
      {
        if (!document.getElementsByName('title')[0].value)
        query.equalTo("title","(No Title)");

        else
          {
            query.equalTo("title",document.getElementsByName('title')[0].value);
          }
      }
      
    if(l)
      {
        if (!document.getElementsByName('location')[0].value)
        query.equalTo("location","");

        else
          {
            query.equalTo("location",document.getElementsByName('location')[0].value);
          }
      }
    if(d)
      {
      if(a)
          { 
            var start = document.getElementsByName('date1')[0].value;
            
              
              var startyear = start.substring(6,10);
              var startmonth = start.substring(3,5);
              var starthour = "";
              var startminute = "";
              var startdate = start.substring(0,2);
              query.equalTo("startyear",startyear);
              query.equalTo("startmonth",startmonth);
              query.equalTo("startdate",startdate);
              query.equalTo("allday",a);
            
          }
      else
          {  
             var start = document.getElementsByName('start')[0].value;
                          
              var startmonth = start.substring(3,5);
              var startyear = start.substring(6,10);
              var starthour = start.substring(11,13);
              var startminute = start.substring(14,16);
              var startdate = start.substring(0,2);
              query.equalTo("startyear",startyear);
              query.equalTo("startmonth",startmonth);
              query.equalTo("startdate",startdate);
              query.equalTo("starthour",starthour);
              query.equalTo("startminute",startminute);
              query.equalTo("allday",a);
          }
      }
    if (!t && !l && !d)
      alert('At least one search parameter should be selected.');
    else if (d&&((!a && !document.getElementsByName('start')[0].value) || (a && !document.getElementsByName('date1')[0].value)))
      alert('Specify a start Date/Time.');
    else          //If query is valid
      {
            /* Query first gives first object in query result list
            Use query.find for getting result list */
        query.find({
        success: function(results) {
          $('#example').dataTable().api().clear().draw();
          var object;
          for (var i = 0; i < results.length; i++) {
            object = results[i];
            result[i]=results[i];
            if (object!=undefined){
            addobject(object,i);
            document.getElementById('nomatch').style.visibility = "hidden";
            
            }
          }
          if (object!=undefined)
            displayobject(object);
          else
            {
              $('#example').dataTable().api().clear().draw();
              duplicate=false;
              console.log(duplicate);
              document.getElementById('nomatch').style.visibility = "visible";
              document.getElementById('result').style.visibility= "hidden";
              document.getElementById('table').style.visibility= "hidden";
              document.getElementById('alldayon1').style.visibility = "hidden";
            document.getElementById('alldayoff1').style.visibility = "hidden";

              }
        },
        error: function(error) {
          alert("Error: " + error.code + " " + error.message);
        }
      });
    }

}


function addobject(object,i)
{
    var title = String(object.get("title"));
    var description = String(object.get("description"));
    var allday = String(object.get("allday"));
    var location = String(object.get("location"));

    var start ="";
    var end= "";
    
    if (object.get("allday")==true)
    {
   

      start = String(object.get("startdate")+"-"+object.get("startmonth")+"-"+object.get("startyear"));
      end =String(object.get("enddate")+"-"+object.get("endmonth")+"-"+object.get("endyear"));

    }

    else
    {

      start = String(object.get("startdate")+"-"+object.get("startmonth")+"-"+object.get("startyear")+" "+object.get("starthour")+":"+object.get("startminute"));
      end =String(object.get("enddate")+"-"+object.get("endmonth")+"-"+object.get("endyear")+" "+object.get("endhour")+":"+object.get("endminute"));
  
    }
  

    $('#example').dataTable().api().row.add([i+1,title, description, location, String(allday), start, end,object.id]).draw();
    window.scrollTo(0, 900);
}

function displayobject(object)
{

  
  document.getElementsByName('title1')[0].value =String(object.get("title"));
  document.getElementsByName('description1')[0].value=String(object.get("description"));
  document.getElementsByName('start1')[0].value=String(object.get("startdate")+"-"+object.get("startmonth")+"-"+object.get("startyear")+" "+object.get("starthour")+":"+object.get("startminute"));
  document.getElementsByName('end1')[0].value=String(object.get("enddate")+"-"+object.get("endmonth")+"-"+object.get("endyear")+" "+object.get("endhour")+":"+object.get("endminute"));
  document.getElementsByName('date11')[0].value=String(object.get("startdate")+"-"+object.get("startmonth")+"-"+object.get("startyear"));
  document.getElementsByName('date22')[0].value=String(object.get("enddate")+"-"+object.get("endmonth")+"-"+object.get("endyear"));
  document.getElementsByName('location1')[0].value=String(object.get("location"));
  var start ="";
  var end= "";
 
  if (object.get("allday")==true)
    {
      document.getElementsByName('allday1')[0].checked="checked";
      document.getElementsByName('allday1')[0].checked=true;
      document.getElementById('alldayon1').style.visibility = "hidden";
      document.getElementById('alldayoff1').style.visibility = "visible";
      start = document.getElementsByName('date11')[0].value;
      end =document.getElementsByName('date22')[0].value;

    }
  else if (object.get("allday")==false)
    {
      document.getElementById('alldayoff1').style.visibility = "hidden";
      document.getElementById('alldayon1').style.visibility = "visible";
      start = document.getElementsByName('start1')[0].value;
      end =document.getElementsByName('end1')[0].value;
      
    }

  var title = document.getElementsByName('title1')[0].value;
  var description = document.getElementsByName('description1')[0].value;  
  var allday1 = document.getElementsByName('allday1')[0].checked;
  var location = document.getElementsByName('location1')[0].value;
  

    displaylink(object.id);
    duplicate=true;
    console.log("Inside function - " + duplicate + " " + object.id);
    document.getElementById('result').style.visibility= "visible";
    document.getElementById('table').style.visibility= "visible";
    $(qrcode).empty()
    $(qrcode).qrcode({
    "size": 300,
    "color": "#3a3",
    "render": "image",
    "background": "white",
    "text": "EVENTualQR"+seq+title+seq+description+seq+String(allday)+seq+start+seq+end+seq+location+seq
     });
}


function tableclick (data)
{
  var x= data[0]-1;
  displayobject(result[x]);
  window.scrollTo(0, 180);
}