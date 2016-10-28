import java.net.*;
import java.io.*;
import java.util.*;

public class ConexaoWeb extends Thread {
    private static String DEFAULT_INDEX = "index.html";
    private static String SERVER_NAME = "Java sucks";
    private static String METHOD_GET = "GET";
    private static String USERNAME = "admin";
    private static String PASSWORD = "password";
    private Socket socket; 					//socket que vai tratar com o cliente.
    private DataOutputStream log;

    // Construtor do Socket
    ConexaoWeb(Socket s) {
        this.socket = s;
        try {
            this.log = new DataOutputStream(new FileOutputStream("WebLog.txt",true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private String getDateString() {
        return new Date().toString();
    }

    /**
     * Responder Not found
     */
    private void notFound(DataOutputStream os) {
        try {
            os.writeBytes("HTTP/1.1 404 Not Found\n");
            os.writeBytes("Date: " + this.getDateString() + "\n");
            os.writeBytes("Server: " + SERVER_NAME + "\n");
            os.writeBytes("\n");
            os.writeBytes("<html><head><title>Error</title></head><body><p>Not found:(</p></body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Responder Unauthorized
     */
    private void unauthorized(DataOutputStream os) {
        try {
            os.writeBytes("HTTP/1.1 401 Unauthorized\n");
            os.writeBytes("Date: " + this.getDateString() + "\n");
            os.writeBytes("Server: " + SERVER_NAME + "\n");
            os.writeBytes("WWW-Authenticate: Basic realm=”System Administrator”\n");
            os.writeBytes("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verificar se credencial enviada é válida
     */
    private boolean authUser(String base64) {
        String encoded;
        encoded = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());
        System.out.println("encoded " + encoded);
        return encoded.equals(base64);
    }

    public void run() {
        String metodo=""; 				//String que vai guardar o metodo HTTP requerido
        String ct; 					//String que guarda o tipo de arquivo: text/html;image/gif....
        String versao = ""; 			//String que guarda a versao do Protocolo.
        File arquivo; 				//Objeto para os arquivos que vao ser enviados.
        String nomeArquivo; 				//String para o nome do arquivo.
        String raiz = System.getProperty("user.dir") + "/site/"; 				//String para o diretorio raiz.
        String inicio;				//String para guardar o inicio da linha
        String senhaUser="";		//String para armazenar o nome e a senha do usuario
        String serverName = "Trabalho de Redes";
        String base64auth = "";
        Date now = new Date();

        BufferedReader is = null;
        DataOutputStream os = null;

        try {
            /**
             * Inicializando input e output
             */
            os = new DataOutputStream(this.socket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            /**
             * Ler metodo HTTP
             */
            String line;
            line = is.readLine();
            StringTokenizer st = new StringTokenizer(line);
            metodo = st.nextToken();
            nomeArquivo = st.nextToken();
            // servir index.html caso nome do arquivo == "/"
            nomeArquivo = nomeArquivo.endsWith("/") ? DEFAULT_INDEX : nomeArquivo;
            versao = st.nextToken();


            log.writeBytes(this.socket.getInetAddress().getHostAddress() + " " + this.socket.getInetAddress().getHostName() + " - [" + now + "]\n \"" + line + "\""); // escrevendo no Log

            /**
             * Caso o método seja GET
             */
            if (metodo.equals(METHOD_GET)) {
                String nextLine;
                while (!(nextLine = is.readLine()).isEmpty()) {
                    /**
                     * Encontrar Header "Host"
                     * Os hosts deveriam ser configurados em arquivo de configuração por exemplo httpd.conf
                     * Para simplificar, este webserver não tem este arquivo de configuração e apenas responderá
                     * pelo virtual host "localhost2", servindo o conteúdo da pasta "site2"
                     */
                    if (nextLine.startsWith(("Host: "))) {
                        System.out.println(nextLine);
                        StringTokenizer stNextLine = new StringTokenizer(nextLine);
                        String key = stNextLine.nextToken();
                        String host = stNextLine.nextToken();
                        System.out.println("host " + host);
                        if (host.equals("localhost2")) {
                            raiz = System.getProperty("user.dir") + "/site2/";
                        }
                    }

                    /**
                     * Verificar se no request há cabeçalho de autorização e guarda-lo na variável base64auth
                     */
                    if (nextLine.startsWith("Authorization: ")) {
                        System.out.println(nextLine);
                        StringTokenizer stNextLine = new StringTokenizer(nextLine);
                        String key = stNextLine.nextToken();
                        String param1 = stNextLine.nextToken();
                        base64auth = stNextLine.nextToken();

                        System.out.println("Base64 Auth: " + base64auth);
                    }
                }

                /**
                 * Se o request tenta acessar algo na pasta /protegido
                 */
                if (nomeArquivo.startsWith(("/protegido"))) {

                    if (!base64auth.equals("")) {
                        if (this.authUser(base64auth)) {
                           // É uma credencial válida
                            System.out.println("AUTH OK!");
                        } else {
                            // A credencial não é válida, respondo Unauthorized
                            System.out.println("auth failed");
                            this.unauthorized(os);
                            throw new Exception("auth failed");
                        }
                    } else {
                        // Se não enviou autorização nos headers, já respondo Unauthorized
                        System.out.println("no base64 found");
                        this.unauthorized(os);
                        throw new Exception("auth failed");
                    }
                }

                System.out.println("===METHOD GET===");
                /**
                 * Ler arquivo pedido
                 */
                ct = this.tipoArquivo(nomeArquivo);
                arquivo = new File(raiz, nomeArquivo);
                FileInputStream fis = new FileInputStream(arquivo);
                byte[] dado = new byte[(int) arquivo.length()];
                fis.read(dado);

                /**
                 * Responder OK, enviando o conteudo do arquivo
                 */
                os.writeBytes("HTTP/1.1 200 OK\n");
                os.writeBytes("Date: " + now.toString() + "\n");
                os.writeBytes("Server: " + serverName + "\n");
                os.writeBytes("Connection: close" + "\n");
                os.writeBytes("Content-type: " + ct + "\n");
                os.writeBytes("\n");
                os.write(dado);

            }

        } catch(FileNotFoundException e)
        {
            System.err.println("File does not exists");
            this.notFound(os);
        } catch(IOException e)
        {
            System.err.println(e.toString());
        } catch (Exception e) {
            e.toString();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                    System.out.println("closing os");
                }
                if (this.log != null){
                    this.log.close();
                    System.out.println("closing log");
                }
            } catch (IOException e) {

            }

        }

        //Fecha o socket.
        try {
            socket.close();
        }
        catch(IOException e) {
        }
    }

//Funcao que retorna o tipo do arquivo.

    public String tipoArquivo(String nome) {
        if(nome.endsWith(".html") || nome.endsWith(".htm")) return "text/html";
        else if(nome.endsWith(".txt") || nome.endsWith(".java")) return "text/plain";
        else if(nome.endsWith(".gif")) return "image/gif";
        else if(nome.endsWith(".class"))  return "application/octed-stream";
        else if( nome.endsWith(".jpg") || nome.endsWith(".jpeg") ) return "image/jpeg";
        else return "text/plain";
    }
}










