package sensoresdogado;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.json.JSONObject;
import util.ComunicadorDeNomes;

public class SensoresDoGado {

    private static final int[] IDS_ANIMAIS = {1, 110, 220, 330, 440, 550, 660, 770, 880, 990};
    //Simula a definição dos IDs dos animais monitorados. Cada sensor estará vinculado a um animal.

    public static void main(String[] args) {
        System.out.println("Iniciando sensores de gado...");

        // Consulta o endereço da central via servidor de nomes
        String endereco = ComunicadorDeNomes.consultar("central");
        if (endereco == null || endereco.equals("nao_encontrado")) {
            System.out.println("Endereço da central não encontrado no servidor de nomes.");
            return;
        }

        String[] partes = endereco.split(":");
        String host = partes[0];
        int porta = Integer.parseInt(partes[1]);
        //Extrai o host e a porta do endereço obtido para criar a conexão com a Central.

        for (int i = 0; i < IDS_ANIMAIS.length; i++) {
            SensorThread sensor = new SensorThread(IDS_ANIMAIS[i], host, porta);
            sensor.run(); // Executa sequencialmente e não de maneira paralela.
            //Para cada animal, cria uma instância de SensorThread e a executa.

            try {
                Thread.sleep(20000); // Aguarda 20 segundos antes do próximo sensor
            } catch (InterruptedException e) {
                System.out.println("Erro no atraso entre sensores: " + e.getMessage());
            }
        }

        System.out.println("Todos os sensores enviaram suas informações.");
    }
}

class SensorThread implements Runnable {
    //Representa um único sensor de gado. Implementa Runnable para possibilitar
    //execução em thread, mesmo que usada aqui de forma sequencial.

    private final int animalId;
    private final String host;
    private final int porta;
    private final Random random = new Random();

    public SensorThread(int animalId, String host, int porta) {
        this.animalId = animalId;
        this.host = host;
        this.porta = porta;
        //Construtor que recebe o ID do animal e as informações da Central.
    }

    @Override
    public void run() {
        try (
            Socket socket = new Socket(host, porta);
            PrintWriter saida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
            //Estabelece conexão com a Central de Controle via socket.
        ) {
            String idSensor = "gado" + animalId;
            double tempCorporal = 37.5 + (random.nextDouble() * 3.0);
            int posX = random.nextInt(100);
            int posY = random.nextInt(100);
            String cocho = random.nextBoolean() ? "cheio" : "vazio";
            //Gera dados simulados de temperatura corporal, posição e situação do cocho.

            JSONObject mensagem = new JSONObject();
            mensagem.put("tipo", "evento");
            mensagem.put("origem", idSensor);
            mensagem.put("destino", "central");
            mensagem.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            JSONObject conteudo = new JSONObject();
            conteudo.put("animal_id", idSensor);
            conteudo.put("temperatura_corporal", String.format("%.1f", tempCorporal));
            conteudo.put("localizacao", "(" + posX + "," + posY + ")");
            conteudo.put("cocho", cocho);

            mensagem.put("conteudo", conteudo);
            //Dados do sensor são encapsulados em "conteudo" dentro da mensagem
            //respeitando o padrão definido na arquitetura por mensagem.

            saida.println(mensagem.toString());
            System.out.println(" Mensagem enviada do " + idSensor + ": " + mensagem.toString());

        } catch (Exception e) {
            System.out.println(" Erro no Sensor " + animalId + ": " + e.getMessage());
        }
    }
}
