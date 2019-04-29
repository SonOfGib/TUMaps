<?php
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        echo "Get is not supported.";
        exit();
    }
    $Email = $_POST["email"];
    $Name = $_POST["username"];
    $Password = $_POST["password"];
    $ConfirmPassword = $_POST["confPassword"];
    $Icon = '';
    $RealName ='';
    if(isset($_POST["fullName"])){
		$RealName = $_POST["fullName"];
	}
	if(isset($_POST["iconUrl"])){
		$Icon = $_POST["iconUrl"];
	}
    
    if($Password !== $ConfirmPassword){
        echo "Passwords do not match!";
        exit();
    }
    else{
        //hash password
        $passhash = password_hash($Password, PASSWORD_DEFAULT);
       
        $conn = mysqli_connect('localhost', 'root', 'TempleOwls2020', 'wiki_data');
        $query = "SELECT * FROM user WHERE username='$Name';";
        $result = mysqli_query($conn, $query);
        if($result){
            if(mysqli_num_rows($result) > 0){
                echo "user name in use!";
                exit();
            }
            else{
               
                $query = "INSERT INTO `user`(`username`, `password`, `fullName`, `email`, `userIcon`)
                VALUES ('$Name', '$passhash', '$RealName', '$Email', '$Icon');";
                $result = mysqli_query($conn, $query);
				if(!$result){
                    echo "Failed to register!";
                    echo "Error: ".mysqli_error($conn);
                    exit();
                }
                else{
                    //User was inserted, get the id so we can insert into the other dbs.
                    $id = $conn->insert_id;
                    //we need to insert into members the uid and gid.
                    $queryGroupname="SELECT id FROM `groups` WHERE groupName='user';";
                    $result = mysqli_query($conn, $queryGroupname);
                    if($result){
                        $gid = mysqli_fetch_assoc($result)['id'];
                        $queryInsert = "INSERT INTO `member` (uid, gid) VALUES ($id, $gid);";
                        $result = mysqli_query($conn, $queryInsert);
                        if($result){
                            //Spit back userid
                            echo "".$id;
                            exit();
                        }
                        else{
                            echo "Failed to insert member";
                            exit();
                        }
                    }
                    else{
                        echo "Failed to fetch gid";
                        exit();
                    }
                }
            }
        }
    }
?>
