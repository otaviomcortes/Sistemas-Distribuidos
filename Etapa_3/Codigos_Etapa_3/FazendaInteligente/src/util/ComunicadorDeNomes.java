package util;

import java.io.*;
import java.net.Socket;
import org.json.JSONObject;

public class ComunicadorDeNomes {

    private static final String HOST = "localhost";
    private static final int PORTA = 11000;
    //Endereço fixo do Servidor de Nomes usado por todos os componentes para registrar ou consultar nomes.

    // Envia uma mensagem de registro ao servidor de nomes
    public static void registrar(String nome, String endereco) {
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            //Abre conexão com o servidor de nomes e configura os canais de envio (saida) e recebimento (entrada).
        ) {
            JSONObject registro = new JSONObject();
            registro.put("tipo", "registro");
            registro.put("nome", nome);
            registro.put("endereco", endereco);
            //Cria uma mensagem JSON para registrar o nome lógico associado ao endereço (ex: "central" -> "localhost:11115").

            saida.println(registro.toString());

            String resposta = entrada.readLine();
            System.out.println(" Registro: " + resposta);
            //Lê a resposta e exibe no console.

        } catch (IOException e) {
            System.out.println("Erro ao registrar no servidor de nomes: " + e.getMessage());
        }
    }

    // Consulta o endereço de outro serviço
    public static String consultar(String nome) {
        try (
            Socket socket = new Socket(HOST, PORTA);
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            //Abre conexão com o servidor de nomes e configura os canais de envio e leitura.
        ) {
            JSONObject consulta = new JSONObject();
            consulta.put("tipo", "consulta");
            consulta.put("nome", nome);
            //Prepara a requisição de consulta para um determinado nome lógico

            saida.println(consulta.toString());

            String resposta = entrada.readLine();
            JSONObject json = new JSONObject(resposta);
            return json.getString("endereco");
            //Lê a resposta e extrai o campo "endereco" (pode retornar "nao_encontrado").

        } catch (IOException e) {
            System.out.println(" Erro ao consultar servidor de nomes: " + e.getMessage());
            return null;
        }
    }
}
