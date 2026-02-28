import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BatRunner {

    public static void main(String[] args) throws IOException {

        // Thư mục chứa start.bat
        Path workingDir = Paths.get(".").toAbsolutePath().normalize();

        // Đường dẫn start.bat
        Path start = workingDir.resolve("start.bat");
        Path kill = workingDir.resolve("kill.bat");
        Path restart = workingDir.resolve("restart.bat");

        // Cách gọi GIỐNG double-click nhất trên Windows
        try {
            batRun(start);
            batRun(kill);
            batRun(restart);
        } catch (Exception e) {
            System.out.println("[ERRO] Error happened:")
        }
    }
    private static void batRun(Path file) throws Exception {

        System.out.println("[INFO] Start file: " + file.toAbsolutePath());

        String fileName = "start.bat";

        String cmd = "cmd";
        String close = "/c";
        
        Path workingDir = Paths.get(".").toAbsolutePath().normalize();
        Path file = workingDir.resolve(fileName);
        String command = file.toAbsolutePath().toString();
        
        // toAbsolutePath() return C:\apps\runner\start.bat
        // toString() return "C:\apps\runner\start.bat" -> can use it now

        new ProcessBuilder(cmd, close,command)
            .directory(workingDir.toFile())
            .inheritIO()
            .start();
            
        System.out.println("[INFO] Start file ran");
    }
}
