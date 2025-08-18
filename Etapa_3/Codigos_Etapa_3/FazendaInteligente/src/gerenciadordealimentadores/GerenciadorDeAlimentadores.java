package gerenciadordealimentadores;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import org.json.JSONObject;
import util.ComunicadorDeNomes;


public class GerenciadorDeAlimentadores {

    private static final int PORTA = 12233;
    private static final int TOTAL_ALIMENTADORES = 10;
    //Define a porta em que o gerenciador escutará por conexões e o número de alimentadores simulados.

    public static void main(String[] args) {
        System.out.println("Gerenciador de Alimentadores iniciado na porta " + PORTA);
        
        ComunicadorDeNomes.registrar("gerenciador_alimentadores", "localhost:12233");
        //Registra o endereço deste gerenciador no Servidor de Nomes.

        // Executor para cada alimentador (1 a 10)
        Map<String, ExecutorService> alimentadores = new HashMap<>();
        for (int i = 1; i <= TOTAL_ALIMENTADORES; i++) {
            String nome = "alimentador" + i;
            alimentadores.put(nome, Executors.newSingleThreadExecutor());
            //Simula 10 alimentadores diferentes, cada um com sua própria fila de execução
        }

        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            while (true) {
                Socket cliente = servidor.accept();
                new Thread(new ManipuladorDeComando(cliente, alimentadores)).start();
                //Inicia um ServerSocket que escuta na porta definida.
                //Para cada conexão recebida (vinda da Central de Controle)
                //uma nova thread é criada para processar o comando recebido.
            }

        } catch (IOException e) {
            System.out.println(" Erro no Gerenciador de Alimentadores: " + e.getMessage());
        }
    }
}

class ManipuladorDeComando implements Runnable {
    private final Socket socket;
    private final Map<String, ExecutorService> alimentadores;

    public ManipuladorDeComando(Socket socket, Map<String, ExecutorService> alimentadores) {
        this.socket = socket;
        this.alimentadores = alimentadores;
        //Construtor inicializa os dados necessários para o processamento do comando.
    }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                //Abre fluxo de leitura da mensagem enviada pela Central de Controle.
        ) {
            String linha = entrada.readLine();
            if (linha != null) {
                JSONObject comando = new JSONObject(linha);
                String destino = comando.getString("destino");
                JSONObject conteudo = comando.getJSONObject("conteudo");
                //Converte a linha recebida para um objeto JSON.

                // Verifica se existe thread para o destino
                ExecutorService executor = alimentadores.get(destino);
                if (executor != null) {
                    executor.execute(() -> {
                        try {
                            System.out.println( destino.toUpperCase() + " alimentando o animal " +
                                    conteudo.getString("animal_id") + "...");
                            Thread.sleep(3000); // Simula tempo de alimentação
                            System.out.println(" Alimentacao concluida por " + destino.toUpperCase() + "\n");
                        } catch (InterruptedException e) {
                            System.out.println(" Interrupcao na alimentação: " + e.getMessage());
                        }
                    });
                } else {
                    System.out.println(" Destino nao reconhecido: " + destino);
                }
            }

        } catch (IOException e) {
            System.out.println(" Erro ao processar comando: " + e.getMessage());
        }
    }
}

