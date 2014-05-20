	function getPos(el) {
    
    		for (var lx=0, ly=0;
        		 el != null;
         		lx += el.offsetLeft, ly += el.offsetTop, el = el.offsetParent);
    		return {x: lx,y: ly};
	}

	    function sendCommand(theUrl, returnFunction)
	    {
             	 	var xmlhttp = new XMLHttpRequest();

			if (returnFunction != null && returnFunction !== "undefined")
			{	
				xmlhttp.onreadystatechange = function()
				{
					if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
					{
						returnFunction(xmlhttp.response);
					}
				}
			}	
                	xmlhttp.open("GET", theUrl, true);
                	xmlhttp.send(null);
	    }

function getWindowSize()
{

	var winW = 630, winH = 460;
	if (document.body && document.body.offsetWidth) {
 		winW = document.body.offsetWidth;
 		winH = document.body.offsetHeight;
	}
	if (document.compatMode=='CSS1Compat' &&  document.documentElement && document.documentElement.offsetWidth ) {
 		winW = document.documentElement.offsetWidth;
 		winH = document.documentElement.offsetHeight;
	}
	if (window.innerWidth && window.innerHeight) {
 		winW = window.innerWidth;
 		winH = window.innerHeight;
	}

	return {width: winW,height: winH};
}

function getUrlParameter( name ){
name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");  
var regexS = "[\\?&]"+name+"=([^&#]*)";  
var regex = new RegExp( regexS );  
var results = regex.exec( window.location.href ); 
 if( results == null )    return null;  
else    return unescape(results[1]);}





//var sourceFiles = new Array();
//var fileID = 0;
//var count = 0;
var current;

// for Properties
var propID = 1;
var hash = {};
hash['first'] = 0;
var viewerIndex;

/* 
 * creates a new XMLHttpRequest object which is the backbone of AJAX  
 * or returns false if the browser doesn't support it 
 */
function getXMLHttpRequest() {
    var xmlHttpReq;
    // to create XMLHttpRequest object in non-Microsoft browsers  
    if (window.XMLHttpRequest) {
        xmlHttpReq = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        try {
            //to create XMLHttpRequest object in later versions of Internet Explorer  
            xmlHttpReq = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (exp1) {
            try {
                //to create XMLHttpRequest object in later versions of Internet Explorer  
                xmlHttpReq = new ActiveXObject("Microsoft.XMLHTTP");
            } catch (exp2) {
                //xmlHttpReq = false;  
                alert("Exception in getXMLHttpRequest()!");
            }
        }
    }
    return xmlHttpReq;
}

/* 
 * AJAX call starts with this function 
 */
function makeRequest(thepage) {
    // alert(thepage);
    var xmlHttpRequest = getXMLHttpRequest();

    if (thepage === 'getDataSourceNames' || thepage === 'viewDataSourceNames') {
        
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=getDataSourceNames", true);
        xmlHttpRequest.send(null);

    } else if (thepage === 'createViewer') {
        var vindex = get("viewerlist").selectedIndex;
        var voptions = get("viewerlist").options;
        var type = voptions[vindex].text;
        var dindex = get("datalist").selectedIndex;
        var doptions = get("datalist").options;
        var data = doptions[dindex].text;
        var dataSourceName = document.getElementById("DS" + dindex).value;

        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=" + thepage + "&type="
                + type + "&data=" + data + "&dataSourceName=" + dataSourceName, true);
        xmlHttpRequest.send(null);

    } else if (thepage === 'views') {
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=getviewerfact", true);
        xmlHttpRequest.send(null);

    } else if (thepage === 'currentviewers') {
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=getcurrviewers", true);
        xmlHttpRequest.send(null);

    } else if (thepage === 'currentLinks') {
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, 'linkViewers');
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=getCurrentLinks", true);
        xmlHttpRequest.send(null);

    } else if (thepage === 'currentViewersLink1') {
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=getcurrviewers", true);
        xmlHttpRequest.send(null);

    } else if (thepage === 'currentViewersLink2') {
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=getcurrviewers", true);
        xmlHttpRequest.send(null);

    }  else if (thepage === 'linkViewers') {
        var v1index = get("vlist1").selectedIndex;
        var voptions = get("vlist1").options;
        var first = voptions[v1index].text;
        var farray = first.split(":");
        var findex = farray[0];
        var v2index = get("vlist2").selectedIndex;
        var doptions = get("vlist2").options;
        var second = doptions[v2index].text;
        var sarray = second.split(":");
        var sindex = sarray[0];
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=" + thepage + "&first=" + findex + "&second=" + sindex, true);
        xmlHttpRequest.send(null);


    } else if (thepage === 'dataFactories') {
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=" + thepage, true);
        xmlHttpRequest.send(null);
    } else if (thepage === 'dataFactoryProperties') {
        var dataFactoryType = document.getElementById("dataFactoryType").value;
        var dataSourceName = document.getElementById("dataSourceName").value
        var url = "VizOnlineServlet?page=" + thepage + "&dataFactoryType=" + dataFactoryType;
        url += "&dataSourceName=" + dataSourceName;
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);

        xmlHttpRequest.open("GET", url, true);
        xmlHttpRequest.send(null);
    }
    else if (thepage === 'properties') {

        //alert("In properties");
        //alert(document.getElementById("viewerName").value)

        //make sure the viewer name is set for the viewer
        var viewerName = "";
        var test = function() {
            viewerName = document.getElementById("viewerName").value;
            if (viewerName !== "") {

                // alert(viewerName);

                var url = "VizOnlineServlet?page=" + thepage
                        + "&viewerName=" + viewerName;

                //alert(url);

                xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
                xmlHttpRequest.open("GET", url, true);
                xmlHttpRequest.send(null);



                clearInterval(id);
            }
        };
        var id = setInterval(test, 100);
    } else if (thepage === 'dataSourceIndex') {
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=" + thepage, false);
        xmlHttpRequest.send(null);
    }


    else {
        xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, thepage);
        xmlHttpRequest.open("GET", "VizOnlineServlet?page=" + thepage, true);
        //xmlHttpRequest.setRequestHandler("Content-Type", "application/x-www-form-urlencoded");  
        //alert("inside makeRequest()!");
        xmlHttpRequest.send(null);
        //alert("sent!");
    }
}

/* 
 * Returns a function that waits for the state change in XMLHttpRequest 
 */
function getReadyStateHandler(xmlHttpRequest, thepage) {
    // an anonynous function returned  
    // it listens to the XMLHttpRequest instance  
    return function() {
        if (xmlHttpRequest.readyState === 4) {
            if (xmlHttpRequest.status === 200) {
                if (thepage === 'data') {
                    getDatasets(xmlHttpRequest.responseText);
                }
                else if (thepage === 'views') {
                    getViewers(xmlHttpRequest.responseText);
                }
                else if (thepage === 'currentviewers') {
                    getCurrentViewers(xmlHttpRequest.responseText);
                }
                else if (thepage === 'currentViewersLink1') {
                    getCurrentViewersLink1(xmlHttpRequest.responseText);
                }
                else if (thepage === 'currentViewersLink2') {
                    getCurrentViewersLink2(xmlHttpRequest.responseText);
                }
                else if (thepage === 'viewDataSourceNames') {

                    getDataForViewer(xmlHttpRequest.responseText);
                    //alert(xmlHttpRequest.responseText);
                }
                else if (thepage === 'home') {
                    document.getElementById("epar").innerHTML = xmlHttpRequest.responseText;
                }
                else if (thepage === 'properties') {
                    //alert(xmlHttpRequest.responseText)
                    //document.getElementById("properties").innerHTML = xmlHttpRequest.responseText;

                    var div = get("properties");

                    addProperties(div, xmlHttpRequest.responseText);
                }
                else if (thepage === 'createViewer') {
                    document.getElementById("currviewers").innerHTML = "";
                    makeRequest('currentviewers');
                    //viewerRequest(xmlHttpRequest.responseText);
                }
                else if (thepage === 'delViewer') {
                    document.getElementById("currviewers").innerHTML = "";
                    makeRequest('currentviewers');
                    //viewerRequest(xmlHttpRequest.responseText);
                }
                else if (thepage === 'launchViewer') {

                    var response = xmlHttpRequest.responseText.split(";");  //e.g viewer.html;name-of-viewer

                    var url = getURL() + response[0];

                    var win = window.open(url, '_blank');

                    win.focus();

                    var test = function() {
                        var viewerName = win.document.getElementById("viewerName");
                        var factoryItemName = win.document.getElementById("factoryItemName");
                        if (viewerName) {
                            viewerName.value = response[1];
                            factoryItemName.value = response[1];
                            clearInterval(id);
                        }
                    };
                    var id = setInterval(test, 100);


                }
                else if (thepage === 'upload') {
                    //NB the response will be the file name if the upload was successful otherwise it will be empty
                    xmlHttpRequest.responseText;
                    
                    if (xmlHttpRequest.responseText !== "") {
                        alert("File:  " + xmlHttpRequest.responseText + " was uploaded successfully");
                    } else {
                        alert("Please choose a valid file");
                    }

                } else if (thepage === 'download') {
                    downloadData(xmlHttpRequest.responseText);
                } else if (thepage === 'getDataSourceNames') {
                    document.getElementById("cdatas").innerHTML = "";
                    getDatasets(xmlHttpRequest.responseText);
                } else if (thepage === 'deleteDataSource') {
                    document.getElementById("cdatas").innerHTML = "";
                    document.getElementById("dataFactoryProperties").innerHTML = "";
                    makeRequest('getDataSourceNames');

                } else if (thepage === 'linkViewers') {
                    document.getElementById("currlinks").innerHTML = "";
                    currentLinks(xmlHttpRequest.responseText);


                } else if (thepage === 'dataFactories') {
                    //we will be creating a combo box options for the datafactories
                    var dataSplit = (xmlHttpRequest.responseText).split(",");

                    var dataFactories = document.getElementById("dataFactories");

                    for (var i = 0; i < dataSplit.length; i++) {

                        var opt = document.createElement('option');
                        opt.setAttribute("value", dataSplit[i]);
                        opt.innerHTML = dataSplit[i];

                        dataFactories.appendChild(opt);
                    }

                } else if (thepage === 'dataFactoryProperties') {
                    var div = get('dataFactoryProperties');
                    addProperties(div, xmlHttpRequest.responseText);
                    makeRequest('getDataSourceNames');
                }
                else if (thepage === 'dataSourceIndex') {
                    document.getElementById("dataSourceIndex").value = xmlHttpRequest.responseText;
                } else if (thepage === 'datasetProperties') {
                    var div = get('dataFactoryProperties');

                    document.getElementById("datasetprops").value = "true"; //to ensure this addproperties is coing from datasetprops

                    addProperties(div, xmlHttpRequest.responseText);

                    document.getElementById("datasetprops").value = "false"; //reset it back to false

                }

                else {
                    //alert("Http error " + xmlHttpRequest.status + ":" + xmlHttpRequest.statusText);
                }
            }
        }
    };
}


//FUNCTIONS TO CREATE NESTED DIVS
function popUp() {

    document.getElementById("cdata").style.display = 'block';
    var name = document.getElementById("dname").value;
    var x = document.getElementById("Title");
    x.innerHTML = "Properties for: " + name;
}

function closePopup() {
    document.getElementById('cdata').style.display = 'none';
}

function get(id) {
    return document.getElementById(id);
}

//Method to clean the name on the label of the properties
function getName(fullname) {
    var tempNameArr = fullname.split(".");
    if (tempNameArr.length > 1)
        return tempNameArr[1];
    else
        return tempNameArr[0];
}


function getDatasets(datalist) {
    if (datalist === "") {
        get("cdatas").innerHTML = "No Datasets available";
    } else {
        //Format: datasource0,file1.txt; datasource1,file2.txt
        var dataArray = datalist.split(";");
        var fileName, dataSourceName;
        for (var i = 0; i < dataArray.length; i++) {
            //var dataArray = datalist2[i].split(",");
            // for (var i = 0; i < dataArray.length; i++) {
            dataSourceName = dataArray[i];
            //fileName = dataArray[1];

            //NB: changing everything to be based on the dataSourceName, there wouldn't be the need for the fileName again
            var par = document.createElement('p');
            par.setAttribute('id', 'par' + i);
            par.setAttribute('name', dataSourceName);
            par.setAttribute('class', 'listpara');
            par.textContent = dataSourceName;
            document.getElementById("cdatas").appendChild(par);

            //hiddenInput for DataSourceName
            var hiddenInput = document.createElement('input');
            hiddenInput.setAttribute("type", "hidden");
            hiddenInput.setAttribute("id", "DS" + i);
            hiddenInput.setAttribute("value", dataSourceName);
            document.getElementById("cdatas").appendChild(hiddenInput);


            //Delete button 
            var but = document.createElement('button');
            but.setAttribute('id', i);
            but.setAttribute('name', dataSourceName);
            but.setAttribute('style', 'float: right');
            but.innerHTML = 'Delete';
            but.setAttribute('class', 'small button blue');
            var theSet = document.getElementById('par' + i).getAttribute('name').toString();
            but.setAttribute('onclick', 'deleteDataSource(' + i + ')');
            document.getElementById('par' + i).appendChild(but);

            //viewDataProperties Button
            var dataPropBut = document.createElement('button');
            dataPropBut.setAttribute('class', 'small button blue');
            dataPropBut.innerHTML = "View Properties";
            dataPropBut.setAttribute('style', 'float: right');
            dataPropBut.setAttribute("onclick", "getDatasetProperties(" + i + ")");
            document.getElementById('par' + i).appendChild(dataPropBut);

            //}
        }
    }
}

function getDatasetProperties(id) {

    var dataSourceName = document.getElementById('DS' + id).value;
    var url = "VizOnlineServlet?page=datasetProperties&dataSourceName=" + dataSourceName;

    var xmlHttpRequest = getXMLHttpRequest();
    xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, "datasetProperties");
    xmlHttpRequest.open("GET", url, true);
    xmlHttpRequest.send(null);

}


function getDataForViewer(datalist) {
    if (datalist === "") {
        get("viewers").innerHTML = "No Datasets available";
    } else {

        //Format: datasource0; datasource1
        var dataArray = datalist.split(";");
        var dataSourceName;
        for (var i = 0; i < dataArray.length; i++) {
            //var dataArray = datalist2[i].split(",");

            dataSourceName = dataArray[i];
            //fileName = dataArray[1];

            var list = get("datalist");
            var option = document.createElement('option');
            option.setAttribute('value', dataSourceName);
            option.text = dataSourceName;
            list.add(option, null);


            //hiddenInput for DataSourceName
            var hiddenInput = document.createElement('input');
            hiddenInput.setAttribute("type", "hidden");
            hiddenInput.setAttribute("id", "DS" + i);
            hiddenInput.setAttribute("value", dataSourceName);


            document.getElementById("viewers").appendChild(hiddenInput);

        }
        get("viewers").appendChilds(list);
    }
}

function getViewers(datalist) {

    if (datalist === "") {
        get("viewers").innerHTML = "No Viewers available";
    } else {
        var dataArray = datalist.split(",");
        for (var i = 0; i < dataArray.length; i++) {

            var list = get("viewerlist");
            current = dataArray[i];
            var option = document.createElement('option');
            option.setAttribute('value', current);
            option.text = current;
            list.add(option, null);
        }
        get("viewers").appendChilds(list);
    }
}

function getCurrentViewers(datalist) {
    if (datalist === "No Content") {
        get("currviewers").innerHTML = "No Viewers available";
    } else {
        var dataArray = datalist.split(",");
        for (var i = 0; i < dataArray.length; i++) {
            current = dataArray[i];
            var nameArray = current.split(":");
            var currIndex = nameArray[0];
            var currName = nameArray[1];
            var printNum = parseInt(currIndex, 10);
            printNum++;
            var par = document.createElement('p');
            par.setAttribute('id', 'v' + currIndex);
            par.setAttribute('name', currIndex);
            par.setAttribute('class', 'listpara');
            par.textContent = printNum + ": " + currName;
            document.getElementById("currviewers").appendChild(par);
            var but1 = document.createElement('button');
            but1.setAttribute('id', 'b' + currIndex);
            but1.setAttribute('name', currIndex);
            but1.setAttribute('style', 'float: right');
            but1.innerHTML = 'Launch';
            but1.setAttribute('class', 'small button blue');
            var theViewer = document.getElementById('v' + currIndex).getAttribute('name');
            but1.setAttribute('onclick', 'launchRequest("' + currName + '")');
            var but2 = document.createElement('button');
            but2.setAttribute('id', 'bdel' + currIndex);
            but2.setAttribute('name', currIndex);
            but2.setAttribute('style', 'float: right');
            but2.innerHTML = 'Delete';
            but2.setAttribute('class', 'small button blue');
            but2.setAttribute('onclick', 'delVRequest("' + theViewer + '")');
            document.getElementById('v' + currIndex).appendChild(but2);
            document.getElementById('v' + currIndex).appendChild(but1);


        }
    }
}

function getCurrentViewersLink1(datalist) {
    if (datalist === "No Content") {
        get("links").innerHTML = "No Viewers available";
    } else {
        var dataArray = datalist.split(",");
        for (var i = 0; i < dataArray.length; i++) {
            var list = get("vlist1");
            current = dataArray[i];
            var option = document.createElement('option');
            option.setAttribute('value', current);
            option.text = current;
            list.add(option, null);
        }
        get("links").appendChilds(list);
    }
}

function getCurrentViewersLink2(datalist) {
    if (datalist === "No Content") {
        get("links").innerHTML = "No Viewers available";
    } else {
        var dataArray = datalist.split(",");
        for (var i = 0; i < dataArray.length; i++) {
            var list2 = get("vlist2");
            current = dataArray[i];
            var option = document.createElement('option');
            option.setAttribute('value', current);
            option.text = current;
            list2.add(option, null);
        }
        get("links").appendChilds(list2);
    }
}

function deleteDataSource(id) {
    //  alert(id);

    /// var fileName = document.getElementById('par' + id).getAttribute('name').toString();
    //alert(fileName);
    var dataSourceName = document.getElementById('DS' + id).value;
       
    //delete the dataSource
    xmlHttpRequest = getXMLHttpRequest();
    xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, "deleteDataSource");
    xmlHttpRequest.open("GET", "VizOnlineServlet?page=deleteDataSource&dataSourceName=" + dataSourceName, true);
    xmlHttpRequest.send(null);
    
    //delete uploaded file also
    var xmlHttpRequest = getXMLHttpRequest();
    xmlHttpRequest.open("GET", "Uploads?page=deleteFile&dataSourceName=" + dataSourceName, true);
    xmlHttpRequest.send(null);


    

}

function launchRequest(viewerName) {

    var xmlHttpRequest = getXMLHttpRequest();
    xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, "launchViewer");

    var url = "VizOnlineServlet?page=viewerLaunch&viewerName=" + viewerName;

    xmlHttpRequest.open("GET", url, true);

    xmlHttpRequest.send(null);
}

function delVRequest(index) {
    var xmlHttpRequest = getXMLHttpRequest();
    xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, "delViewer");
    xmlHttpRequest.open("GET", "VizOnlineServlet?page=delViewer&index=" + index, true);
    xmlHttpRequest.send(null);
}

function currentLinks(datalist) {
    if (datalist === "No Content") {
        get("currlinks").innerHTML = "No Links available";
    } else {
        var dataArray = datalist.split(",");
        for (var i = 0; i < dataArray.length; i++) {
            current = dataArray[i];
            var linkArray = current.split(":");
            var currIndex = linkArray[0];
            var firstView = linkArray[1];
            var secondView = linkArray[2];
            var par = document.createElement('p');
            par.setAttribute('id', 'l' + currIndex);
            par.setAttribute('name', currIndex);
            par.setAttribute('class', 'listpara');
            par.textContent = "Viewer: " + firstView + " - Viewer: " + secondView;
            document.getElementById("currlinks").appendChild(par);
            var but1 = document.createElement('button');
            but1.setAttribute('id', 'b' + currIndex);
            but1.setAttribute('name', currIndex);
            but1.setAttribute('style', 'float: right');
            but1.innerHTML = 'Unlink';
            but1.setAttribute('class', 'small button blue');
            var theLink = document.getElementById('l' + currIndex).getAttribute('name');
            but1.setAttribute('onclick', 'unlinkViewers("' + theLink + '")');
            document.getElementById('l' + currIndex).appendChild(but1);
        }
    }
}

function unlinkViewers(index) {
    var xmlHttpRequest = getXMLHttpRequest();
    xmlHttpRequest.onreadystatechange = getReadyStateHandler(xmlHttpRequest, "linkViewers");
    xmlHttpRequest.open("GET", "VizOnlineServlet?page=unlinkViewers&index=" + index, true);
    xmlHttpRequest.send(null);

}

function getURL() {
    var arr = window.location.href.split("/");
    delete arr[arr.length - 1];
    return arr.join("/");
}
