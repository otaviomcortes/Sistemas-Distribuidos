package nomeservidor;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;

public class ServidorDeNomes {

    private static final int PORTA = 11000;
    private static final Map<String, String> registros = new HashMap<>();
    //Escuta conexões na porta 11000 e mantém um Map para armazenar pares nome -> endereço (localhost:porta).

    public static void main(String[] args) {
        System.out.println("️Servidor de Nomes iniciado na porta " + PORTA);

        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            while (true) {
                Socket cliente = servidor.accept();
                new Thread(() -> processarRequisicao(cliente)).start();
            }
            //O servidor aceita conexões de clientes (ex: Central, sensores, alimentadores)
            //inicia uma thread para processar a requisição de forma independente (concorrente).
        } catch (IOException e) {
            System.out.println(" Erro no servidor de nomes: " + e.getMessage());
        }
    }

    private static void processarRequisicao(Socket socket) {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String linha = entrada.readLine();
            JSONObject requisicao = new JSONObject(linha);
            //Recebe uma linha (mensagem JSON) enviada por um cliente e converte em objeto JSONObject.

            String tipo = requisicao.getString("tipo");

            if (tipo.equals("registro")) {
                String nome = requisicao.getString("nome");
                String endereco = requisicao.getString("endereco");
                registros.put(nome, endereco);
                System.out.println(" Registrado: " + nome + " -> " + endereco);
                saida.println("{\"status\": \"registrado\"}");
                //O par nome -> endereço é salvo no Map, e uma confirmação é enviada ao cliente.
            } else if (tipo.equals("consulta")) {
                String nome = requisicao.getString("nome");
                String endereco = registros.getOrDefault(nome, "nao_encontrado");
                JSONObject resposta = new JSONObject();
                resposta.put("endereco", endereco);
                saida.println(resposta.toString());
                //O servidor retorna o endereço se encontrar o nome; caso contrário, retorna "nao_encontrado".
            }

        } catch (IOException e) {
            System.out.println(" Erro ao processar requisição: " + e.getMessage());
        }
    }
}
