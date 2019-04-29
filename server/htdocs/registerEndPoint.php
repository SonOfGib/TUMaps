<?php
    
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
       
        $conn = mysqli_connect('localhost', 'admin', 'TempleOwls2020', 'wiki_data');
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
                    echo "Registered!";
                    exit();
                }
                
            }
        }
    }
?>