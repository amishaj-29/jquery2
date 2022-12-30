
alert();
$(document).ready(function (e) {
    alert();
    e.preventDefault();
    // on click Sign In Button checks that username =='admin' and password == 'password'
    $("#login").click(function () {
      if (
        $("#loginusername").val() == "amishajain291099@gmail.com" &&
        $("#loginpassword").val() == "password"
      ) {
        $("#contact_form").hide();
        $("#second").append(
          "<p>Welcome User...!</p> <br/><input type='button' id='logout' value='Log Out' />"
        );
      } else {
        alert("Please try again");
      }

      $("#logout").click(function () {
        $("form")[0].reset();
        $("#contact_form").show();
        $("#second").hide();
      });
    });
  });