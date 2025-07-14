package sensorambiental;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SensorAmbiental {

    public static void main(String[] args) {
        final String HOST = "localhost";
        final int PORTA = 12345;

        try (Socket cliente = new Socket(HOST, PORTA);
             PrintWriter saida = new PrintWriter(new OutputStreamWriter(cliente.getOutputStream()), true)) {

            System.out.println("Sensor Ambiental conectado a Central de Controle.");

            // Simulando envio periódico de dados
            for (int i = 0; i < 10; i++) { // envia 10 vezes e encerra
                String sensorId = "sensor";
                int temperatura = (int) (20 + Math.random() * 10); // valor entre 20 e 30
                int umidade = (int) (40 + Math.random() * 20);     // valor entre 40 e 60

                String mensagem = sensorId + i + ":temperatura=" + temperatura + ";umidade=" + umidade;
                saida.println(mensagem);

                System.out.println("Dados enviados: " + mensagem);
                Thread.sleep(5000); // espera 5 segundos para próxima leitura
            }

            System.out.println("Sensor Ambiental encerrando conexão.");

        } catch (IOException | InterruptedException e) {
            System.out.println("Erro no Sensor Ambiental: " + e.getMessage());
        }
    }
}
