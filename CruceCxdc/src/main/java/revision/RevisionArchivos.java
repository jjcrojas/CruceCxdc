package revision;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RevisionArchivos {
    public static void main(String[] args) {
        // URL de conexión a Teradata
        String url = "jdbc:teradata://10.40.176.8/DATABASE=prod_dwh_sgp,USER=jcrojas,PASSWORD=JulPipe06*";

        // Cargar el driver JDBC de Teradata
        try {
            Class.forName("com.teradata.jdbc.TeraDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC de Teradata no encontrado");
            e.printStackTrace();
            return;
        }

        // Establecer la conexión
        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                System.out.println("Conectado a Teradata exitosamente");
                // Aquí puedes realizar operaciones en la base de datos
            }
        } catch (SQLException e) {
            System.out.println("Error al conectarse a Teradata");
            e.printStackTrace();
        }
    }
}
