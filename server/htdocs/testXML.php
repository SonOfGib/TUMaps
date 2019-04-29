<?php
include('vendor/autoload.php');
use PhpXmlRpc\Value;
use PhpXmlRpc\Request;
use PhpXmlRpc\Client;
// create a new client instance
$client = new Client('doku/lib/exe/xmlrpc.php', 'localhost', 80);
 
// enable debugging to see more infos :-) (well, not for production code)
$client->setDebug(1);
 
$authReq = new Request("dokuwiki.login");
$authReq->addParam(new Value("sean", "string"));
$authReq->addParam(new Value("test", "string"));
 
 
 
 
// create the XML message to send
$request = new Request('wiki.putPage');
$request->addParam(new Value("pageNameAdded", "string"));
$request->addParam(new Value("Uh, let me be clear.", "string"));
$request->addParam(new Value(array("args" => new Value(true, "boolean")), "struct"));

// send the message and wait for response
$response = $client->send($authReq);
if($response == false) die('error');
if(!$response->faultCode()){
    $cookies = $response->cookies();
    $keys = array_keys($cookies);
    for($i=0; $i < sizeof($cookies); $i++){
        $temp =  $cookies[$keys[$i]];
        $val = $temp['value'];
        echo "$val", PHP_EOL;
        $client->setCookie($keys[$i], $val);
    }
    //echo "$cookies";
    $response = $client->send($request);
    if($response == false) die('error');
    if(!$response->faultCode()){
        // seems good. Now do whatever you want with the data
       $value = $response->value();
       $text = $value->serialize();
        echo "$text";
    }
}



?>