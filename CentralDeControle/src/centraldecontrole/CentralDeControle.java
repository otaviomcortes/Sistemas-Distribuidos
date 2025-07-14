package centraldecontrole;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CentralDeControle {

    public static void main(String[] args) {
        final int PORTA = 12345;

        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            System.out.println("Central de Controle iniciada na porta " + PORTA);

            while (true) {
                // Aguarda a conexão de um cliente (sensor)
                Socket cliente = servidor.accept();
                System.out.println("Novo sensor conectado: " + cliente.getInetAddress().getHostAddress());

                // Cria uma thread para tratar a conexão
                Thread sensorThread = new Thread(() -> tratarSensor(cliente));
                sensorThread.start();
            }

        } catch (IOException e) {
            System.out.println("Erro na Central de Controle: " + e.getMessage());
        }
    }

    // Método para processar dados de um sensor conectado
    private static void tratarSensor(Socket cliente) {
        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(cliente.getInputStream()))
        ) {
            String mensagem;
            while ((mensagem = entrada.readLine()) != null) {
                System.out.println("Dados recebidos do sensor: " + mensagem);
                // Aqui futuramente podemos tomar decisões, enviar comandos etc.
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler dados do sensor: " + e.getMessage());
        } finally {
            try {
                cliente.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar conexão com o sensor.");
            }
        }
    }
}
