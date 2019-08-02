function validaEmail(email) {
    var regex = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
    if (!regex.test(email)) {
        alert("O e-mail inserido é inválido!");
        document.getElementById("Usuario-Email").value = "";
        document.getElementById("Usuario-Email").focus();
    }
}

function validaCpf(cpf) {
    cpf = cpf.replace('.',"");
    cpf = cpf.replace('.',"");
    cpf = cpf.replace('-',"");

    var numeros, digitos, soma, i, resultado, digitos_iguais;
    digitos_iguais = 1;
    if (cpf.length < 11){
        alert("O CPF informado é inválido!");
        document.getElementById("Usuario-Cpf").value = "";
        document.getElementById("Usuario-Cpf").focus();
        return;
    }
    for (i = 0; i < cpf.length - 1; i++)
        if (cpf.charAt(i) != cpf.charAt(i + 1))
        {
            digitos_iguais = 0;
            break;
        }
    if (!digitos_iguais)
    {
        numeros = cpf.substring(0,9);
        digitos = cpf.substring(9);
        soma = 0;
        for (i = 10; i > 1; i--)
            soma += numeros.charAt(10 - i) * i;
        resultado = soma % 11 < 2 ? 0 : 11 - soma % 11;
        if (resultado != digitos.charAt(0)){
            alert("O CPF informado é inválido!");
            document.getElementById("Usuario-Cpf").value = "";
            document.getElementById("Usuario-Cpf").focus();
            return;
        }
        numeros = cpf.substring(0,10);
        soma = 0;
        for (i = 11; i > 1; i--)
            soma += numeros.charAt(11 - i) * i;
        resultado = soma % 11 < 2 ? 0 : 11 - soma % 11;
        if (resultado != digitos.charAt(1)){
            alert("O CPF informado é inválido!");
            document.getElementById("Usuario-Cpf").value = "";
            document.getElementById("Usuario-Cpf").focus();
            return;
        }
        return true;
    }
    else{
        alert("O CPF informado é inválido!");
        document.getElementById("Usuario-Cpf").value = "";
        document.getElementById("Usuario-Cpf").focus();
        return;
    }
}

function validaEstado(estado){
    if(estado == 'UF'){
        alert("Por favor, selecione um Estado/UF.")
    }
}

function validaCodUsuario(cod){
    if(cod <= 0 || cod == ""){
        /*alert("Por favor, informar um Id válido!");*/
        document.getElementById("IdUsuario-codUsuario").value = "";
        document.getElementById("IdUsuario-codUsuario").focus();
    }
}

