package centraldecontrole;

import util.ComunicadorDeNomes;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import org.json.JSONObject;

public class CentralDeControle {

    private static final int PORTA_RECEBIMENTO = 11115;
    private static final String NOME_GERENCIADOR = "gerenciador_alimentadores";
    //A Central escuta na porta 11115 e se comunica com o Gerenciador registrado com esse nome.

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        //Cria um pool dinâmico de threads para lidar com múltiplas conexões simultâneas de sensores.

        ComunicadorDeNomes.registrar("central", "localhost:" + PORTA_RECEBIMENTO);
        //Registra a Central no servidor de nomes para que sensores possam descobrir seu endereço via nomeação.

        try (ServerSocket servidor = new ServerSocket(PORTA_RECEBIMENTO)) {
            System.out.println("Central de Controle iniciada na porta " + PORTA_RECEBIMENTO);

            while (true) {
                Socket cliente = servidor.accept();
                pool.execute(new ManipuladorDeCliente(cliente));
            }
            //A cada nova conexão dos sensores, cria uma thread ManipuladorDeCliente para processar a mensagem.

        } catch (IOException e) {
            System.out.println("❌ Erro na Central de Controle: " + e.getMessage());
        }
    }

    //Classe auxiliar para lidar com mensagens recebidas de sensores.
    static class ManipuladorDeCliente implements Runnable {
        private final Socket socket;

        public ManipuladorDeCliente(Socket socket) {
            this.socket = socket;
            //Construtor que recebe o socket do sensor.
        }

        @Override
        public void run() {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String linha;
                //Lê o conteúdo enviado pelo sensor via socket.
                while ((linha = entrada.readLine()) != null) {
                    JSONObject mensagem = new JSONObject(linha);
                    String tipo = mensagem.getString("tipo");
                    String origem = mensagem.getString("origem");
                    JSONObject conteudo = mensagem.getJSONObject("conteudo");
                    //Interpreta a mensagem recebida como JSON.

                    System.out.println("📥 Mensagem recebida de " + origem + ":\n" + mensagem.toString(2));

                    if (tipo.equals("evento") && origem.startsWith("gado")) {
                        String animalIdStr = conteudo.getString("animal_id").replace("gado", "");
                        int animalId = Integer.parseInt(animalIdStr);

                        double temperatura = Double.parseDouble(conteudo.getString("temperatura_corporal").replace(",", "."));
                        if (temperatura > 39.5) {
                            System.out.println(" ALERTA: TEMPERATURA ELEVADA!!!");
                        }
                        //Extrai o ID do animal, converte temperatura e verifica alerta de febre.

                        if (conteudo.getString("cocho").equalsIgnoreCase("vazio")) {
                            int alimentadorIndex = (animalId - 1) / 100 + 1;
                            String destino = "alimentador" + alimentadorIndex;
                            //Se o cocho estiver vazio, calcula qual alimentador deve ser acionado
                            //(com base no ID do animal dividido em faixas de 100).
                            //Animal de ID = 1 até o 100, vai ser alimentado pelo alimentador 1
                            //Animal de ID = 101 até 200, vai ser alimentado pelo alimentador 2

                            JSONObject comando = new JSONObject();
                            comando.put("tipo", "comando");
                            comando.put("origem", "central");
                            comando.put("destino", destino);
                            comando.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                            JSONObject acao = new JSONObject();
                            acao.put("acao", "alimentar");
                            acao.put("animal_id", conteudo.getString("animal_id"));
                            comando.put("conteudo", acao);
                            //Monta uma mensagem do tipo "comando" pedindo ao alimentador alimentar o animal correspondente.

                            // Consulta IP:porta do Gerenciador via servidor de nomes
                            String enderecoGerenciador = ComunicadorDeNomes.consultar(NOME_GERENCIADOR);
                            if (enderecoGerenciador != null && !enderecoGerenciador.equals("nao_encontrado")) {
                                String[] partes = enderecoGerenciador.split(":");
                                try (Socket socketSaida = new Socket(partes[0], Integer.parseInt(partes[1]));
                                     PrintWriter saida = new PrintWriter(new OutputStreamWriter(socketSaida.getOutputStream()), true)) {

                                    saida.println(comando.toString());
                                    System.out.println("Comando enviado ao " + destino + ": " + comando.toString() + "\n");

                                } catch (IOException e) {
                                    System.out.println(" Falha ao conectar com Gerenciador: " + e.getMessage());
                                }
                            } else {
                                System.out.println("Gerenciador de Alimentadores nao encontrado no servidor de nomes.");
                            }
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("❌ Erro ao ler do cliente: " + e.getMessage());
            }
        }
    }
}
