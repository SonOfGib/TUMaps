<?php
    include('vendor/autoload.php');
    use PhpXmlRpc\Value;
    use PhpXmlRpc\Request;
    use PhpXmlRpc\Client;
    
    $username = $_POST["username"];
    $password = $_POST["password"];

    $conn = mysqli_connect('localhost', 'root', 'TempleOwls2020', 'wiki_data');
    $query = "SELECT * FROM user WHERE username='$username';";
    $result = mysqli_query($conn, $query);
    if(!$conn){
        echo "Failed to connect!"."\nConnection failed: ".
		mysqli_connect_error();;
        exit();
    }
    if($result){
        $loggedin = verifyLogin($result, $password);
        if(!$loggedin){
            echo 0;
            exit();
        }
        else{
            //Get the login cookie from dokuwiki
            $loggedin = loginDoku($username, $password);
            if($loggedin){
                $row = mysqli_fetch_assoc($result);
                $imageUrl = $row["userIcon"];
                if($imageUrl != null && $imageUrl !== ""){
                    $fp = fopen($name, 'rb');
                    // send the right headers
                    header("Content-Type: image/png");
                    header("Content-Length: " . filesize($name));
                    fpassthru($fp);
                    exit();
            }
            else{
                //header("Location: doku/doku.php");
                exit();
            }
            }
            else{
                echo "doku login failed!";
                exit();
            }
        }
    }
    else{
        echo "No result";
        exit();
    }
    function verifyLogin($result, $Password){
        if(mysqli_num_rows($result) > 0){	//check to make sure we got a hit
            $row=mysqli_fetch_assoc($result);
            $Passhash = $row["password"];
            if(password_verify($Password,$Passhash)){
                return true;
            }
            return false;
        }
    }
    function loginDoku($username, $password){
        // create a new client instance
        $client = new Client('doku/lib/exe/xmlrpc.php', 'localhost', 80);
         
        // enable debugging to see more infos :-) (well, not for production code)
        $client->setDebug(0);
         
        $authReq = new Request("dokuwiki.login");
        $authReq->addParam(new Value("$username", "string"));
        $authReq->addParam(new Value("$password", "string"));
        
        $response = $client->send($authReq);
        if($response == false){
            return false;
        }
        if(!$response->faultCode()){
            $cookies = $response->cookies();
            foreach($cookies as $name => $cookie){
                //echo $name."\r\n", $cookie['value']."\r\n", strtotime('+1 days')."\r\n", '/doku/'."\r\n", null."\r\n";
                setCookie($name, $cookie['value'], null, '/doku/');
            }
            return true;
        }
    }

?>
