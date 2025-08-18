package sensoresambientais;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import util.ComunicadorDeNomes;

public class SensoresAmbientais {
   
    private static final int QUANTIDADE_SENSORES = 10;
    //Simula o uso de 10 sensores ambientais diferentes
    
    public static void main(String[] args) {
        System.out.println("Iniciando sensores ambientais...");

        
        String endereco = ComunicadorDeNomes.consultar("central");
        if (endereco == null || endereco.equals("nao_encontrado")) {
            System.out.println(" Endereço da central não encontrado no servidor de nomes.");
            return;
        }
        // Consulta o endereço da central no servidor de nomes

        String[] partes = endereco.split(":");
        String host = partes[0];
        int porta = Integer.parseInt(partes[1]);
        //Extrai o host e a porta do endereço retornado, preparando-se para abrir a conexão via socket.
        
        ExecutorService pool = Executors.newFixedThreadPool(QUANTIDADE_SENSORES);
        Random random = new Random();
        //Cria um pool de threads fixo, permitindo simular sensores em paralelo.
        //Random é usado para simular os dados coletados por cada sensor.
        
        for (int i = 1; i <= QUANTIDADE_SENSORES; i++) {
            String idSensor = "ambiente" + i;
            //Loop para criar os 10 sensores com identificadores únicos como ambiente1, ambiente2 etc.

            final String finalIdSensor = idSensor; // Necessário para lambda
            pool.execute(() -> {
                try (Socket socket = new Socket(host, porta);
                     PrintWriter saida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {
                    //Abre uma conexão com a Central usando socket TCP.
                    
                    System.out.println("Sensor Ambiental " + finalIdSensor + " conectado a Central de Controle.");

                    while (true) {
                        double temperatura = 18.0 + (random.nextDouble() * 22.0); // 18°C a 40°C
                        double umidade = 30.0 + (random.nextDouble() * 60.0);     // 30% a 90%
                        //Gera valores aleatórios simulando leituras reais de sensores físicos.

                        JSONObject mensagem = new JSONObject();
                        mensagem.put("tipo", "evento");
                        mensagem.put("origem", finalIdSensor);
                        mensagem.put("destino", "central");
                        mensagem.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                        JSONObject conteudo = new JSONObject();
                        conteudo.put("temperatura", String.format("%.1f", temperatura));
                        conteudo.put("umidade", String.format("%.1f", umidade));

                        mensagem.put("conteudo", conteudo);

                        saida.println(mensagem.toString());

                        System.out.println("Sensor Ambiental " + finalIdSensor + " enviou: " + mensagem.toString());
                        //Envia a mensagem para a Central e imprime no console para acompanhamento.

                        Thread.sleep(10000 + random.nextInt(5000)); // 10 a 15 segundos
                    }

                } catch (Exception e) {
                    System.out.println("Erro no sensor " + finalIdSensor + ": " + e.getMessage());
                }
            });
        }
    }
}
