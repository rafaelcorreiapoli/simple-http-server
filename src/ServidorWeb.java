import java.net.*;
import java.io.*;
import java.util.*;

public class ServidorWeb {
    public static void main(String[] args) {
        int porta; 		//porta aonde o servidor vai aguardar por pedido de conexao.
        ServerSocket sw; 	//Socket servidor
        Socket cw;
        porta = 8080; // escolhendo a porta para o Socket
        try {
            sw = new ServerSocket(porta); // Criar o socket Servidor
            while(true) {
                cw = sw.accept(); // Deixar o socket Servidor aceitando conexões
                ConexaoWeb conexaoWeb = new ConexaoWeb(cw); // Tratar o pedido de conexão
                conexaoWeb.start(); //  Trocar informações com o cliente
            }
        }
        catch(IOException e) {
            System.err.println(e.toString());
        }
    }
}