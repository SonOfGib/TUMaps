<?php
    include('vendor/autoload.php');
    include('templateFile.php');
    use PhpXmlRpc\Value;
    use PhpXmlRpc\Request;
    use PhpXmlRpc\Client;
    $baseURL = "http://ec2-34-203-104-209.compute-1.amazonaws.com/dokuwiki";
    //$baseURL = "localhost/doku";
    //Create Page url= name with spaces removed and to lowercase (if name already exists don't allow input)
    //Get Lat, Lng, Uid, name, pageName
    //Create page @ url with xmlrpc
    //Have templated text to insert into page for user.
    //Auth cookie prob needed too.
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        echo "Get is not supported.";
        exit();
    }
    //check if we got an auth cookie first.
    $authCookie = FALSE;
    foreach ($_COOKIE as $key=>$val){
        //echo $key.' is '.$val."<br>";
        if (preg_match('/\bDW/', $key)) {
            $authCookie = TRUE;
        }
    }
    if(!$authCookie){
        echo "Not authorized!";
        exit();
    }
    $lat = (float) $_POST['lat']; //sketch
    $lng = (float) $_POST['lng'];
    $uid = $_POST['uid'];
    $locName = $_POST['locationName']; //strip whitespace and lowercase this to get url.
    $temp = preg_replace('/\s*/', '', $locName);
    $url = strtolower($temp);          //add the base url in here too. or at least the namespace
    $realUrl = $baseURL."/locations/".$url;
    
    $conn = mysqli_connect('localhost', 'root', 'TempleOwls2020', 'wiki_data');
    $query = "SELECT * FROM page WHERE url='$realUrl';";
    $result = mysqli_query($conn, $query);
    if(mysqli_num_rows($result) == 0){
        //url/name not already in use, we good.
        $insertPageQ = "INSERT INTO page (url) VALUES ('$realUrl');";
        $result = mysqli_query($conn, $insertPageQ);
        if($result){
            $pid = $conn->insert_id; //pageid get    
            //create location
            $insertLocationQ = "INSERT INTO `locations`(`name`, `pageId`, `latitude`, `longitude`, `creatorId`)
            VALUES ('$locName', $pid, $lat, $lng, $uid)";
            $result = mysqli_query($conn, $insertLocationQ);
            if($result){
                $insertRevisionQ = "INSERT INTO `revision`(`pageId`, `authorId`)
            VALUES ($pid, $uid)";
                $result = mysqli_query($conn, $insertRevisionQ);
                echo "".($result == TRUE ? "yay" : "boo");
            }
            
            //add page to wiki @ namespace
            // create a new client instance
            $client = new Client('dokuwiki/lib/exe/xmlrpc.php', 'localhost', 80);
            //$client = new Client('doku/lib/exe/xmlrpc.php', 'localhost', 80);

            //give the client our cookies
            $client->setDebug(1);
            foreach ($_COOKIE as $key=>$val){
                $client->setCookie($key, $val);
            }
            // create the XML message to send
            $request = new Request('wiki.putPage');
            $request->addParam(new Value("locations:$url", "string"));
            //put that content in there boi
            $content = TEMPLATE;
            $content = str_replace("<Location name>","$locName",$content);
            $content = str_replace("<latitude>","$lat",$content);
            $content = str_replace("<longitude>","$lng",$content);
            $request->addParam(new Value($content));
            $request->addParam(new Value(array("args" => new Value(true, "boolean")), "struct"));

            // send the message and wait for response
            $response = $client->send($request);
            if($response == false) die('error');
            if(!$response->faultCode()){
               $value = $response->value();
               $text = $value->serialize();
                echo "$text"; //this ought be a boolean
            }
        }
        else{
            echo "nope";
        }
    }
    else{
        echo "nope, one already there";
    }
    
?>
