<?php
    //creatorId -1 is seeded location!
    
    $baseURL = "http://ec2-34-203-104-209.compute-1.amazonaws.com/dokuwiki";
    //$baseURL = "localhost/doku";
    //Create Page url= name with spaces removed and to lowercase (if name already exists don't allow input)
    //Get Lat, Lng, Uid, name, pageName
    //Create page @ url with xmlrpc
    //Have templated text to insert into page for user.
    //Auth cookie prob needed too.
//    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
//        echo "Get is not supported.";
//        exit();
//    }
    
    //all we gotta do is return a big ol list of location objects. ez pz.
    $conn = mysqli_connect('localhost', 'root', 'TempleOwls2020', 'wiki_data');
    //$conn = mysqli_connect('localhost', 'admin', 'TempleOwls2020', 'wiki_data');
    $query = "SELECT l.id, l.name, l.latitude, l.longitude, l.creatorId, p.url
    FROM locations AS l 
    INNER JOIN page AS p 
    ON l.pageId = p.id;";
    $request = mysqli_query($conn, $query);
    $rows = array();
    while($r = mysqli_fetch_assoc($request)) {
        $rows[] = $r;
    }
    print json_encode($rows);

?>
