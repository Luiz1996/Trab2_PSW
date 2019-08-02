package br.uem.din.bibliotec.config.dao;

import br.uem.din.bibliotec.config.conexao.Conexao;
import br.uem.din.bibliotec.config.model.Livro;
import br.uem.din.bibliotec.config.services.FormataData;
import br.uem.din.bibliotec.config.services.Email;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LivroDao {
    private Email email = new Email();
    private FormataData dtFormat =  new FormataData();

    //método de cadastramento de livro
    public int cadastrarLivro(Livro livro) throws SQLException {
        try {
            //realiza conexão com banco de dados
            Conexao con = new Conexao();
            Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            con.conexao.setAutoCommit(true);

            //realiza insert no banco e retorna mensagem de sucesso na cor verde
            st.executeUpdate("INSERT INTO `bibliotec`.`livro` (`codcatalogacao`, `numchamada`, `titulo`, `autor`, `editora`, `anolancamento`, `cidade`, `volume`, `ativo`, `datacad`, `disponibilidade`) VALUES ('"+livro.getCodcatalogacao()+"', '"+livro.getNumchamada()+"', '"+livro.getTitulo()+"', '"+livro.getAutor()+"', '"+livro.getEditora()+"', '"+livro.getAnolancamento()+"', '"+livro.getCidade()+"', "+livro.getVolume()+", '1', current_date(), '1');");

            st.close();
            con.conexao.close();

            return 1;
        } catch (SQLException e) {
            return 0;
        }
    }


    public List<Livro> consultarLivro(Livro livro, int soDisponiveis) throws SQLException {
        //realiza conexão com banco de dados
        Conexao con = new Conexao();
        con.conexao.setAutoCommit(true);
        Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

        //setando os valores obtidos no front-end para realizar busca no banco de dados
        livro.setEditora(livro.getTitulo());
        livro.setAutor(livro.getTitulo());

        //busca todas as informações de acordo com os dados fornecidos
        if(soDisponiveis == 0){
            st.execute("select * from `bibliotec`.`livro` where (titulo like \"%"+ livro.getTitulo() +"%\" or autor like \"%" + livro.getAutor() + "%\" or editora like \"%" + livro.getEditora() + "%\") and ativo = '1' order by 2;");
        }else{
            st.execute("select * from `bibliotec`.`livro` where (titulo like \"%"+ livro.getTitulo() +"%\" or autor like \"%" + livro.getAutor() + "%\" or editora like \"%" + livro.getEditora() + "%\") and ativo = '1' and disponibilidade = '1' order by 2;");
        }

        ResultSet rs = st.getResultSet();

        //declaração do arrayList para auxiliar na impressão da dataTable do consultar acervo do Visitante
        List<Livro> livros = new ArrayList<>();

        //obtendo os valores carregados no result set e carregado no arrayList
        while (rs.next()) {
            Livro livro_temporario = new Livro(rs.getInt("codlivro"),
                    rs.getString("codcatalogacao"),
                    rs.getString("numchamada"),
                    rs.getString("titulo"),
                    rs.getString("autor"),
                    rs.getString("editora"),
                    rs.getString("anolancamento"),
                    rs.getString("cidade"),
                    rs.getInt("volume"),
                    rs.getInt("ativo"),
                    "",
                    "",
                    "",
                    rs.getString("datacad"),
                    rs.getString("dataalt"),
                    rs.getString("disponibilidade")
                    );

            livros.add(livro_temporario);
        }

        //fechando as conexões em aberto para evitar locks infinitos no banco de dados
        st.close();
        rs.close();
        con.conexao.close();

        return livros;
    }

    public List<Livro> consultarLivroBibliotecario(Livro livro, int ativo) throws SQLException {
        //realizando a conexão com banco de dados
        Conexao con = new Conexao();
        con.conexao.setAutoCommit(true);
        Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

        System.out.println();

        //buscando as informações no banco de dados de acordo com o titulo do livro informado pelo usupario, essa busca possui diferencial da coluna Status (Ativo/Inativo)
        if(ativo == 0){
            st.execute("select l.codlivro, l.codcatalogacao, l.numchamada, l.titulo, l.autor, l.editora, l.anolancamento, l.cidade, l.volume, l.ativo, case when l.ativo = 1 then \" Ativo \" else \" Inativo \" end as status, datacad, dataalt, case when disponibilidade = 1 then \" Sim \" when disponibilidade = 0 then \" Não \" end as disponibilidade  from livro l where (l.titulo like \"%"+ livro.getBusca().trim() +"%\" or l.autor like \"%" + livro.getBusca().trim() + "%\" or l.editora like \"%" + livro.getBusca().trim() + "%\") order by 1 ;");
        }else{
            st.execute("select l.codlivro, l.codcatalogacao, l.numchamada, l.titulo, l.autor, l.editora, l.anolancamento, l.cidade, l.volume, l.ativo, case when l.ativo = 1 then \" Ativo \" else \" Inativo \" end as status, datacad, dataalt, case when disponibilidade = 1 then \" Sim \" when disponibilidade = 0 then \" Não \" end as disponibilidade  from livro l where ativo = '1' order by 4 ;");
        }

        ResultSet rs = st.getResultSet();

        //declara o arrayList que será usado no dataTable do Bibliotecário
        List<Livro> livros = new ArrayList<>();

        //carregando o arrayList com os valores obtidos no resultSet
        while (rs.next()) {
            Livro livro_temporario = new Livro
                (   rs.getInt("codlivro"),
                    rs.getString("codcatalogacao"),
                    rs.getString("numchamada"),
                    rs.getString("titulo"),
                    rs.getString("autor"),
                    rs.getString("editora"),
                    rs.getString("anolancamento"),
                    rs.getString("cidade"),
                    rs.getInt("volume"),
                    rs.getInt("ativo"),
                    rs.getString("status"),
                   "",
                   "",
                   dtFormat.formatadorDatasBrasil(rs.getString("datacad")),
                   dtFormat.formatadorDatasBrasil(rs.getString("dataalt")),
                   rs.getString("disponibilidade")
                );

            livros.add(livro_temporario);
        }

        //fechando todas as conexões para evitar lock nas tabelas do banco de dados
        st.close();
        rs.close();
        con.conexao.close();

        return livros;
    }

    public int deletarLivro(Livro livro){
        try {
            //abre conexão com banco de dados
            Conexao con = new Conexao();
            Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            con.conexao.setAutoCommit(true);

            //validando se o livro possui alguma reserva ou empréstimos em vigor
            st.execute( "SELECT \n" +
                            "    COALESCE(l.usuariores, 0) AS ha_reserva,\n" +
                            "    COALESCE(MAX(e.ativo),0) AS ha_emp\n" +
                            "FROM\n" +
                            "    livro l\n" +
                            "LEFT JOIN\n" +
                            "    emprestimo e ON e.codlivro = l.codlivro\n" +
                            "WHERE\n" +
                            "    l.codlivro = '" + livro.getCodlivro() + "';");

            ResultSet rs = st.getResultSet();

            int ha_reserva = 0, ha_emp = 0;
            while(rs.next()){
                ha_reserva = rs.getInt("ha_reserva");
                ha_emp = rs.getInt("ha_emp");
            }

            //se possuir emprestimo ou reserva em vigor, a deleção é abortada
            if(ha_reserva > 0){
                return -1;
            }

            if(ha_emp > 0){
                return -2;
            }

            //executa a EXCLUSÃO LÓGICA do livro no banco de dados, ou seja, ativo recebe 0
            st.executeUpdate("UPDATE `bibliotec`.`livro` SET `ativo` = '0', dataalt = current_date(), disponibilidade = '0' WHERE (`codlivro` =" + livro.getCodlivro() + ");");

            //fecha conexões
            st.close();
            con.conexao.close();

            return 1;
        } catch (SQLException e) {
            return 0;
        }
    }

    public int editarLivro(Livro livro){
        Integer codlivro = 0;

        try {
            //realiza conexão com banco de dados
            Conexao con = new Conexao();
            Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            con.conexao.setAutoCommit(true);

            st.execute( "SELECT \n" +
                            "    COALESCE(l.codlivro, 0) AS codlivro\n" +
                            "FROM\n" +
                            "    livro l\n" +
                            "WHERE\n" +
                            "    l.codlivro = '" + livro.getCodlivro() + "';");

            ResultSet rs = st.getResultSet();

            while(rs.next()){
                codlivro = rs.getInt("codlivro");
            }

            //valida se o código do livro não foi fornecido ou se foi fornecido um código inválido
            if(codlivro == 0){
                return -1;
            }

            st.executeUpdate("UPDATE `bibliotec`.`livro` \n" +
                                "SET \n" +
                                "    `codcatalogacao` = '"+livro.getCodcatalogacao().trim()+"',\n" +
                                "    `numchamada` = '"+livro.getNumchamada().trim()+"',\n" +
                                "    `titulo` = '"+livro.getTitulo().trim()+"',\n" +
                                "    `autor` = '"+livro.getAutor().trim()+"',\n" +
                                "    `editora` = '"+livro.getEditora().trim()+"',\n" +
                                "    `anolancamento` = '"+livro.getAnolancamento().trim()+"',\n" +
                                "    `cidade` = '"+livro.getCidade().trim()+"',\n" +
                                "    `volume` = '"+livro.getVolume()+"',\n" +
                                "    `ativo` = '"+livro.getAtivo()+"',\n" +
                                "    `dataalt` = current_date(),\n" +
                                "    `disponibilidade` = '"+livro.getDisponibilidade().trim()+"'\n" +
                                "WHERE\n" +
                                "    (`codlivro` = '"+livro.getCodlivro()+"');");

            //fechando as conexões para evitar lock
            st.close();
            con.conexao.close();

            return 1;
        } catch (SQLException e) {
            return 0;
        }
    }

    public int cadastrarReserva(Livro livro){
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        String login = (String)session.getAttribute("usuario");
        int haReservas = 0;

        try {
            //realiza conexão com banco de dados
            int codUsuarioLocal = 0;
            String emailres = "", usuariores = "", titulores = "", datares = "";
            Conexao con = new Conexao();
            Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            con.conexao.setAutoCommit(true);
            ResultSet rs = null;

            st.execute("select\n" +
                            "\tcount(e.codemprestimo) as qtde_atrasado\n" +
                            "from\n" +
                            "\temprestimo e\n" +
                            "inner join\n" +
                            "\tusuarios   u on u.codusuario = e.codusuario\t\n" +
                            "where\n" +
                            "\te.ativo = '1' and\n" +
                            "\te.datadev < current_date() and\n" +
                            "    u.usuario = '" + login + "';");
            rs = st.getResultSet();

            //varificando se o usuário possui empréstimos em atraso
            while(rs.next()){
                if(rs.getInt("qtde_atrasado") > 0){
                    return -2;
                }
            }

            //obtendo codusuario que está efetuando a reserva
            st.execute( "SELECT \n" +
                            "    codusuario\n" +
                            "FROM\n" +
                            "    `bibliotec`.`usuarios`\n" +
                            "WHERE\n" +
                            "    usuario = '" + login + "';");
            rs = st.getResultSet();

            while(rs.next()){
                codUsuarioLocal = rs.getInt("codusuario");
            }

            haReservas = verificaExistenciaReserva(codUsuarioLocal, livro.getCodlivro());

            if(haReservas == 0) {

                //criando nova reserva
                st.execute( "select\n" +
                                "    l.disponibilidade\n" +
                                "from\n" +
                                "\tlivro l\n" +
                                "where\n" +
                                "\tl.codlivro = '" + livro.getCodlivro() + "';");
                rs = st.getResultSet();
                while (rs.next()) {
                    livro.setDisponibilidade(rs.getString("disponibilidade").trim());
                }

                if (livro.getDisponibilidade().equals("1".trim())) {
                    st.executeUpdate("update livro l set l.datares = current_date(), l.usuariores = '" + codUsuarioLocal + "' where l.codlivro = '" + livro.getCodlivro() + "';");
                } else {
                    //se o livro já estiver emprestado mas sem reserva, precisamos obter a data de devolção do emprestimo atual para poder criar nova reserva
                    //trazendo data de devolução do emprestimo atual e somando D+1
                    //se o empréstimo estiver atrasado, então soma-se 30 dias da data de devolução
                    st.execute( "SELECT \n" +
                                    "    MAX(e.codemprestimo) AS codemprestimo, \n" +
                                    "    case\n" +
                                    "\t\t    when e.datadev < current_date() then DATE_ADD(e.datadev ,INTERVAL 30 DAY)\n" +
                                    "        else DATE_ADD(e.datadev ,INTERVAL 1 DAY)\n" +
                                    "    end as datares    \n" +
                                    "FROM\n" +
                                    "    emprestimo e\n" +
                                    "WHERE\n" +
                                    "    e.ativo = '1' AND e.codlivro = '" + livro.getCodlivro() + "';");

                    rs = st.getResultSet();
                    while (rs.next()) {
                        livro.setDatares(rs.getString("datares").trim());
                    }

                    st.executeUpdate("update livro l set l.datares = '" + livro.getDatares() + "', l.usuariores = '" + codUsuarioLocal + "' where l.codlivro = '" + livro.getCodlivro() + "';");
                }

                st.execute( "select\n" +
                                "\tu.email,\n" +
                                "    u.nome,\n" +
                                "    l.titulo,\n" +
                                "    l.datares\n" +
                                "from\n" +
                                "\tusuarios u\n" +
                                "left join\n" +
                                "\tlivro    l on l.usuariores = u.codusuario\n" +
                                "where\n" +
                                "\tl.codlivro = '" + livro.getCodlivro() + "';\t");

                rs = st.getResultSet();

                while (rs.next()) {
                    emailres = rs.getString("email");
                    usuariores = rs.getString("nome");
                    titulores = rs.getString("titulo");
                    datares = dtFormat.formatadorDatasBrasil(rs.getString("datares"));
                }

                //Enviando e-mail confirmando reserva realizada com sucesso
                email.setAssunto("Reserva de Livro - Biblioteca X");
                email.setEmailDestinatario(emailres.trim());
                email.setMsg("Olá " + usuariores + ", <br><br> A sua reserva para o livro <b>'" + titulores + "'</b> foi efetuada com sucesso.<br><br>Data de Retirada: <b>" + datares + "</b>.");
                email.enviarGmail();
            }

            st.close();
            con.conexao.close();
        } catch (SQLException e) {
            haReservas = -1;
        }
        return haReservas;
    }

    public int verificaExistenciaReserva(int codusuario, int codlivro){
        try {
            //realiza conexão com banco de dados
            Conexao con = new Conexao();
            Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            con.conexao.setAutoCommit(true);

            //se tentar cadastrar reserva e o livro já estiver emprestado pelo mesmo usuário, então o sistema barra!
            st.execute( "SELECT \n" +
                            "    COALESCE(u.codusuario,0) as codusuario\n" +
                            "FROM\n" +
                            "    emprestimo e\n" +
                            "        INNER JOIN\n" +
                            "    usuarios u ON u.codusuario = e.codusuario\n" +
                            "WHERE\n" +
                            "    e.codusuario = '" + codusuario + "' AND e.codlivro = '" + codlivro + "'\n" +
                            "        AND e.ativo = '1';");
            ResultSet rs = st.getResultSet();

            int codUsuarioEmp = 0;
            while(rs.next()){
                codUsuarioEmp = rs.getInt("codusuario");
            }

            //usuário já está com livro emprestado
            if(codUsuarioEmp != 0){
                return 3;
            }

            //consultar se livro já possui reserva para ele mesmo ou outra pessoa
            st.execute( "SELECT \n" +
                            "    COALESCE(l.usuariores,0) AS usuariores,\n" +
                            "    CASE\n" +
                            "        WHEN current_date() <= l.datares \n" +
                            "\t\t\t      THEN 'Reservado'\n" +
                            "\t\t\t      ELSE 'Não Reservado'\n" +
                            "    END AS status\n" +
                            "FROM\n" +
                            "    `bibliotec`.`livro` l\n" +
                            "WHERE\n" +
                            "    l.codlivro = '" + codlivro + "';");

            rs = st.getResultSet();

            int usuario_reserva = 0;
            String status = "".trim();
            while(rs.next()){
                usuario_reserva = (rs.getInt("usuariores"));
                status = (rs.getString("status").trim());
            }

            //usuario que está reservando já está com reserva para este livro
            if(usuario_reserva == codusuario){
                return 1;
            }

            //livro encontra-se reservado para outro usuario
            if(status.equals("Reservado".trim())){
                return 2;
            }

        } catch (SQLException e) {
            System.out.println("Falha ao consultar existencia de reserva!");
        }
        return 0;
    }

    public List<Livro> consultaMinhasReservas(){
        List<Livro> livros = new ArrayList<Livro>();
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        String login = (String)session.getAttribute("usuario");

        try {
            //realiza conexão com banco de dados
            Conexao con = new Conexao();
            Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            con.conexao.setAutoCommit(true);
            ResultSet rs = null;

            st.execute(  "select\n" +
                            "\tl.titulo,\n" +
                            "    l.autor,\n" +
                            "    l.editora,\n" +
                            "    l.anolancamento,\n" +
                            "    l.volume,\n" +
                            "    l.datares,\n" +
                            "    l.codlivro\n" +
                            "from\n" +
                            "\tlivro    l\n" +
                            "left join\n" +
                            "\tusuarios u on u.codusuario = l.usuariores\n" +
                            "where\n" +
                            "\t  u.usuario = '"+login+"' and\n" +
                            "    l.datares >= current_date();");

            rs = st.getResultSet();

            //carregando o arrayList com os valores obtidos no resultSet
            while (rs.next()) {
                Livro livro_temporario = new Livro(
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("editora"),
                        rs.getString("anolancamento"),
                        rs.getInt("volume"),
                        dtFormat.formatadorDatasBrasil(rs.getString("datares")),
                        rs.getInt("codlivro")
                        );
                livros.add(livro_temporario);
            }
        }catch (Exception e){
            System.out.println("Dados informados são invalidos, falha na consulta!");
        }
        return livros;
    }

    public int cancelarReserva(Livro livro){
        String emailres = "", usuariores = "", titulores = "";

        try {
            //realiza conexão com banco de dados
            Conexao con = new Conexao();
            Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            con.conexao.setAutoCommit(true);

            st.execute("SELECT \n" +
                            "    u.email, u.nome, l.titulo\n" +
                            "FROM\n" +
                            "    usuarios u\n" +
                            "        LEFT JOIN\n" +
                            "    livro l ON l.usuariores = u.codusuario\n" +
                            "WHERE\n" +
                            "    l.codlivro = '"+livro.getCodlivro()+"';");

            ResultSet rs = st.getResultSet();

            while(rs.next()){
                emailres = rs.getString("email");
                usuariores = rs.getString("nome");
                titulores = rs.getString("titulo");
            }

            //cancelando a reserva selecionada
            st.executeUpdate("update `bibliotec`.`livro` l set l.datares = null, l.usuariores = null where l.codlivro = '"+livro.getCodlivro()+"';");

            //Enviando e-mail de confirmação de cancelamento de reserva
            email.setAssunto("Cancelamento de Reserva - Biblioteca X");
            email.setEmailDestinatario(emailres);
            email.setMsg("Olá "+usuariores+", <br><br> A reserva do livro <b>'"+titulores+"'</b> foi cancelada com sucesso.");
            email.enviarGmail();

            return 1;
        }catch (Exception e){
            return 0;
        }
    }

    public int carregarDadosLivro(Livro livro){
        try {
            //realiza conexão com banco de dados
            Conexao con = new Conexao();
            Statement st = con.conexao.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            con.conexao.setAutoCommit(true);

            st.execute("SELECT L.* FROM `bibliotec`.`livro` L WHERE L.CODLIVRO = '" + livro.getCodlivro() + "';");
            ResultSet rs = st.getResultSet();

            while(rs.next()){
                livro.setTitulo(rs.getString("titulo").trim());
                livro.setAutor(rs.getString("autor").trim());
                livro.setEditora(rs.getString("editora").trim());
                livro.setAnolancamento(rs.getString("anolancamento").trim());
                livro.setCidade(rs.getString("cidade").trim());
                livro.setVolume(rs.getInt("volume"));
                livro.setCodcatalogacao(rs.getString("codcatalogacao").trim());
                livro.setNumchamada(rs.getString("numchamada").trim());
                livro.setAtivo(rs.getInt("ativo"));
                livro.setDisponibilidade(rs.getString("disponibilidade").trim());
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
        return 1;
    }
}
