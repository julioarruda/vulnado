$(document).ready(function(){
  $("#btn-register").click(function(){
    var username = $('#inputUsername').val();
    var password = $('#inputPassword').val();
    
    // Basic validation
    if (!username || !password) {
      alert("Por favor, preencha todos os campos");
      return;
    }
    
    var payload = {username: username, password: password};

    $.ajax({
      type: 'POST',
      url: "http://localhost:8080/register",
      data: JSON.stringify(payload),
      dataType: "json",
      contentType: "application/json"
    })
    .fail(function(data){
      var errorMessage = "Erro no cadastro";
      if (data.responseJSON && data.responseJSON.message) {
        errorMessage = data.responseJSON.message;
      } else if (data.responseText) {
        errorMessage = data.responseText;
      }
      alert(errorMessage);
    })
    .done(function(data){
      localStorage.jwt = data.token;
      var username = JSON.parse(atob(data.token.split('.')[1]))['sub'];
      localStorage.username = username;
      alert("Cadastro realizado com sucesso!");
      window.location.replace("index.html");
    })
  })
});