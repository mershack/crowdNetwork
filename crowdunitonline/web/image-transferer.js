function ImageTransferer(div, viewerName, w, h, tch, tcv)
{
	this.div = div;
	this.viewerName = viewerName;

	var tch = tch;
	var tcv = tcv;
	var width = w;
	var height = h;
	var tw = w / tch;
	var th = h / tcv;

	this.images = null;

	this.cnt = 0;
	this.loaded = 0;

	var thisObj = this;

	var desiredWidth = w;
	var desiredHeight = h;

	var resizing = true;





//asking for a init on the server ////////////////////

	var url = "ViewerCanvas?page=resize&viewerName=" + viewerName
                        + "&width=" + width + "&height=" + height;


	var xmlhttp = new XMLHttpRequest();
	xmlhttp.onreadystatechange = function()
	{
		if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
		{
			resizing = false;
			setInterval(thisObj.imageUpdate, 20);
		}
	}
	xmlhttp.open("GET", url, true);
	xmlhttp.send(null);
 

	this.resize = resize;
	function resize(w,h)
	{
		//has to divide by tch,tcv
		while (w % tch != 0) w--;
		while (h % tcv != 0) h--;

		desiredWidth = w;
		desiredHeight = h;
	}


	function actualResize(w,h)
	{
		resizing = true;

                var url = "ViewerCanvas?page=resize&viewerName=" + thisObj.viewerName
                        + "&width=" + w + "&height=" + h;


                var xmlhttp = new XMLHttpRequest();
		xmlhttp.onreadystatechange = function()
		{
			if (xmlhttp.readyState === 4 && xmlhttp.status === 200)
			{
				width = w;
				height = h;
				tw = w / tch;
				th = h / tcv;
				resizing = false;				
			}
		}
                xmlhttp.open("GET", url, true);
                xmlhttp.send(null);
	}




//image updating functions 


    this.imageUpdate = imageUpdate;
    function imageUpdate() {

	if (resizing)
		return;

        if (thisObj.images !== null)
            return;
      

        if (thisObj.viewerName === "") {
            return;
        }

	if (desiredWidth != width || desiredHeight != height)
	{
		actualResize(desiredWidth, desiredHeight);
		return;
	}

        thisObj.images = [];
        thisObj.cnt++;
        thisObj.loaded = 0;

        for (var i = 0; i < tcv; i++)
        {
            var rowImages = [];

            for (var j = 0; j < tch; j++)
            {
                var url = "ViewerCanvas?page=imageUpdate&viewerName=" + thisObj.viewerName
                        + "&tileX=" + j + "&tileY=" + i + "&diff=1&r=" + thisObj.cnt + ((new Date()).getTime());

                var image = new Image();
                rowImages.push(image);

                var imtr = thisObj;

                image.onload = function() {
                    imtr.loaded++;
                    imtr.fimloaded();

                };
                image.src = url;

            }
            thisObj.images.push(rowImages);
        }
    }


    this.divMoved = divMoved;
    function divMoved()
    {
	var divPos = getPos(this.div);		
	for (var i=0; i<this.div.children.length; i++)
                this.div.children[i].style.left = this.div.children[i].imageOffsetX + divPos.x + "px";	
             
    }

	


    this.fimloaded = fimloaded;
    function fimloaded() {

        if (this.loaded === tch * tcv)
        {
            if (this.images[0][0].width === 1 && this.images[0][1].width == 1 && this.images[1][0].width == 1 && this.images[1][1].width == 1)
            {
                this.images = null;
                return;
            }
            else if (this.images[0][0].width !== tw && this.images[0][1].width !== tw && this.images[1][0].width !== tw && this.images[1][1].width !== tw)  //this.tw is the width of a tile, it used to be 500 which was half of a thousand
            {
		if (this.images[0][0].width != 1 && this.images[0][1].width != 1 && this.images[1][0].width != 1 && this.images[1][1].width != 1)
		{
		//alert(this.images[0][0].width + " " + this.images[0][1].width + " " + this.images[1][0].width + " " + this.images[1][1].width + " " + tw);
                var last;
                while (last = this.div.lastChild)
                   this.div.removeChild(last);
		}
		
            }

	var divPos = getPos(this.div);	
	divPos.y = 0;

	var images = this.images;
	this.images = null;
      
        for (var i = 0; i < images.length; i++)
                for (var j = 0; j < images[i].length; j++)
                {
                   
                    this.div.appendChild(images[i][j]);

			images[i][j].style.position = "absolute";

			if (images[i][j].width < tw)
			{
                    		images[i][j].style.width = tw + "px";
                    		images[i][j].style.height = th + "px";
                   		images[i][j].style.top = i * th + divPos.y + "px";
                   		images[i][j].style.left = j * tw + divPos.x + "px";
				images[i][j].imageOffsetX =  j * tw;
			}
			else
			{
				images[i][j].style.top = i * images[i][j].height + divPos.y + "px";
                   		images[i][j].style.left = j * images[i][j].width + divPos.x + "px";
				images[i][j].imageOffsetX =  j * images[i][j].width;
			}
                    

                }

	 this.images = null;
       }
    }


}



